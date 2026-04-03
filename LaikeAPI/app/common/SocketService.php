<?php
require_once 'RedisClusters.php';

set_time_limit(0);
error_reporting(E_ALL);

// ===== 基础常量 =====
define('LISTEN_ADDR', '0.0.0.0');
define('LISTEN_PORT', 18000);
define('MAX_CLIENT', 2000);
define('CLIENT_TIMEOUT', 180);
define('PID_FILE', '/var/run/laike_socket.pid');

// ===== EINTR 兼容 =====
if (!defined('SOCKET_EINTR')) {
    define('SOCKET_EINTR', 4);
}
if (!defined('SOCKET_EAGAIN')) {
    define('SOCKET_EAGAIN', 11);
}
if (!defined('SOCKET_EWOULDBLOCK')) {
    define('SOCKET_EWOULDBLOCK', 11);
}

// ===== 单实例保障 =====
if (file_exists(PID_FILE)) {
    $pid = (int)trim(@file_get_contents(PID_FILE));
    if ($pid && function_exists('posix_kill') && @posix_kill($pid, 0)) {
        fwrite(STDERR, "SocketService already running, pid={$pid}\n");
        exit(1);
    }
}
file_put_contents(PID_FILE, getmypid());

register_shutdown_function(function () {
    @unlink(PID_FILE);
});

// ===== 启动 =====
$service = new SocketService(LISTEN_ADDR, LISTEN_PORT);
$service->run();

class SocketService
{
    private $address;
    private $port;
    private $server;
    private $clients = []; // client_key => info
    private $redis;

    public function __construct($address, $port)
    {
        $this->address = $address;
        $this->port = $port;

        $this->redis = new RedisClusters();
        $this->redis->connect();
    }

    private function initServer()
    {
        $sock = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
        socket_set_option($sock, SOL_SOCKET, SO_REUSEADDR, 1);

        if (!@socket_bind($sock, $this->address, $this->port)) {
            $err = socket_last_error($sock);
            error_log("socket_bind failed: " . socket_strerror($err));
            exit(1); // 🔥 生产必须退出
        }

        socket_listen($sock, 128);
        socket_set_nonblock($sock);

        echo "listen on {$this->address}:{$this->port}\n";
        $this->server = $sock;
    }

    public function run()
    {
        $this->initServer();

        while (true) {
            try {
                $read = [$this->server];
                foreach ($this->clients as $c) {
                    $read[] = $c['socket'];
                }

                $write = $except = null;
                $n = @socket_select($read, $write, $except, 1);

                if ($n === false) {
                    if (socket_last_error() === SOCKET_EINTR) {
                        continue;
                    }
                }

                foreach ($read as $sock) {
                    if ($sock === $this->server) {
                        $this->acceptClient();
                    } else {
                        $this->recvClient($sock);
                    }
                }

                $this->cleanupTimeout();

            } catch (Throwable $e) {
                error_log("SocketService error: " . $e->getMessage());
            }

            usleep(10000);
        }
    }

    private function acceptClient()
    {
        if (count($this->clients) >= MAX_CLIENT) {
            $tmp = @socket_accept($this->server);
            if ($tmp) socket_close($tmp);
            return;
        }

        $client = @socket_accept($this->server);
        if (!$client) return;

        socket_set_nonblock($client);
        socket_set_option($client, SOL_SOCKET, SO_KEEPALIVE, 1);

        $buffer = '';
        while (true) {
            $chunk = '';
            $bytes = @socket_recv($client, $chunk, 2048, 0);
            if ($bytes === false) {
                $err = socket_last_error($client);
                if (in_array($err, [SOCKET_EAGAIN, SOCKET_EWOULDBLOCK, SOCKET_EINTR], true)) {
                    continue;
                }
                @socket_close($client);
                return;
            }
            if ($bytes === 0) {
                @socket_close($client);
                return;
            }

            $buffer .= $chunk;
            if (strpos($buffer, "\r\n\r\n") !== false) break;
        }

        $clientKey = $this->handshake($client, $buffer);

        $this->clients[$clientKey] = [
            'socket' => $client,
            'recv_buf' => '',
            'last_active' => time(),
            'key' => null
        ];
    }

    private function recvClient($sock)
    {
        $key = $this->findClientKey($sock);
        if (!$key) return;

        $buf = '';
        $bytes = @socket_recv($sock, $buf, 2048, 0);
        if ($bytes === false) {
            $err = socket_last_error($sock);
            if (in_array($err, [SOCKET_EAGAIN, SOCKET_EWOULDBLOCK, SOCKET_EINTR], true)) {
                return;
            }
            $this->closeClient($key);
            return;
        }
        if ($bytes === 0) {
            $this->closeClient($key);
            return;
        }

        $this->clients[$key]['recv_buf'] .= $buf;
        $this->clients[$key]['last_active'] = time();

        while (true) {
            $msg = $this->decodeFrame($this->clients[$key]['recv_buf']);
            if ($msg === null) break;

            $event = $this->doEvents($msg);
            if ($event && isset($event['client_key'])) {
                $ck = $event['client_key'];
                if (isset($this->clients[$ck])) {
                    $this->send($this->clients[$ck]['socket'], json_encode($event['msg']));
                    if (isset($event['msg2'])) {
                        $this->send($this->clients[$ck]['socket'], json_encode($event['msg2']));
                    }
                }
            }
        }
    }

    private function cleanupTimeout()
    {
        $now = time();
        foreach ($this->clients as $key => $c) {
            if ($now - $c['last_active'] > CLIENT_TIMEOUT) {
                $this->closeClient($key);
            }
        }
    }

    private function closeClient($key)
    {
        if (!isset($this->clients[$key])) return;
        @socket_close($this->clients[$key]['socket']);
        if (!empty($this->clients[$key]['key'])) {
            $this->redis->remove($this->clients[$key]['key']);
        }
        unset($this->clients[$key]);
    }

    private function findClientKey($sock)
    {
        foreach ($this->clients as $k => $c) {
            if ($c['socket'] === $sock) return $k;
        }
        return null;
    }

    private function handshake($client, $buffer)
    {
        preg_match("/Sec-WebSocket-Key: (.*)\r\n/", $buffer, $m);
        $key = trim($m[1] ?? '');
        $accept = base64_encode(pack('H*', sha1($key . '258EAFA5-E914-47DA-95CA-C5AB0DC85B11')));

        $resp = "HTTP/1.1 101 Switching Protocols\r\n"
            . "Upgrade: websocket\r\n"
            . "Connection: Upgrade\r\n"
            . "Sec-WebSocket-Accept: {$accept}\r\n\r\n";

        socket_write($client, $resp);

        $clientKey = md5(uniqid('', true));
        $this->redis->set($clientKey, 1);

        $this->send($client, json_encode([
            'type' => 'handShake',
            'msg' => '握手成功',
            'client_key' => $clientKey
        ]));

        return $clientKey;
    }

    private function decodeFrame(&$buffer)
    {
        $bufLen = strlen($buffer);
        if ($bufLen < 2) return null;

        $b1 = ord($buffer[0]);
        $b2 = ord($buffer[1]);
        $opcode = $b1 & 0x0F;
        $isMasked = ($b2 & 0x80) === 0x80;
        $payloadLen = $b2 & 0x7F;
        $offset = 2;

        if ($payloadLen === 126) {
            if ($bufLen < $offset + 2) return null;
            $payloadLen = unpack('n', substr($buffer, $offset, 2))[1];
            $offset += 2;
        } elseif ($payloadLen === 127) {
            if ($bufLen < $offset + 8) return null;
            $ext = unpack('N2', substr($buffer, $offset, 8));
            $payloadLen = ($ext[1] << 32) | $ext[2];
            $offset += 8;
        }

        $mask = '';
        if ($isMasked) {
            if ($bufLen < $offset + 4) return null;
            $mask = substr($buffer, $offset, 4);
            $offset += 4;
        }

        if ($bufLen < $offset + $payloadLen) return null;

        $payload = (string)substr($buffer, $offset, $payloadLen);
        $buffer = (string)substr($buffer, $offset + $payloadLen);

        // 客户端主动关闭连接
        if ($opcode === 0x8) {
            return '';
        }

        // 客户端 ping：忽略 payload，上层按心跳业务处理
        if ($opcode === 0x9 || $opcode === 0xA) {
            return '';
        }

        if ($isMasked) {
            $decoded = '';
            for ($i = 0; $i < $payloadLen; $i++) {
                $decoded .= $payload[$i] ^ $mask[$i % 4];
            }
            return $decoded;
        }

        return $payload;
    }

    private function send($sock, $msg)
    {
        $frame = $this->encodeFrame($msg);
        @socket_write($sock, $frame, strlen($frame));
    }

    private function encodeFrame($msg)
    {
        $len = strlen($msg);
        if ($len < 126) return "\x81" . chr($len) . $msg;
        if ($len < 65536) return "\x81" . chr(126) . pack("n", $len) . $msg;
        $high = ($len >> 32) & 0xFFFFFFFF;
        $low = $len & 0xFFFFFFFF;
        return "\x81" . chr(127) . pack("NN", $high, $low) . $msg;
    }

    // ===== 原业务逻辑
    private function doEvents($msg)
    {
        // ===== 兼容被 stringify 包了一层的 JSON =====
        if (is_string($msg)) {
            $msg = trim($msg);

            // 情况："{\"type\":\"heartbeat\"}"
            if (strlen($msg) > 2 && $msg[0] === '"' && $msg[strlen($msg) - 1] === '"') {
                $inner = json_decode($msg, true);
                if (is_string($inner)) {
                    $msg = $inner;
                }
            }
        }

        if (!is_string($msg) || $msg === '') {
            return null;
        }

        $recv_msg = json_decode($msg, true);
        if (!is_array($recv_msg)) {
            error_log("Invalid JSON: " . substr($msg, 0, 200));
            return null;
        }

        if (!isset($recv_msg['type'])) {
            error_log("Missing type field: " . json_encode($recv_msg));
            return null;
        }

        $response = [];

        // ===== 登录 =====
        if ($recv_msg['type'] === 'login') {
            if (!isset($recv_msg['send_id'], $recv_msg['is_mch_send'], $recv_msg['client_key'])) {
                error_log("Login missing fields");
                return null;
            }

            $is_mch_send = $recv_msg['is_mch_send'];
            $send_id = $recv_msg['send_id'];
            $client_key = $recv_msg['client_key'];

            $KEY = $send_id . '_' . $is_mch_send;

            $this->redis->set($KEY, $client_key);
            $this->clients[$client_key]['key'] = $KEY;

            return [
                'msg' => [
                    'type' => 'login',
                    'message' => '您已登录！'
                ],
                'key' => $KEY,
                'client_key' => $client_key
            ];
        }

        // ===== 消息发送 =====
        if ($recv_msg['type'] === 'message') {
            if (!isset(
                $recv_msg['send_id'],
                $recv_msg['receive_id'],
                $recv_msg['is_mch_send'],
                $recv_msg['content']
            )) {
                error_log("Message missing fields");
                return null;
            }

            $is_mch_send = $recv_msg['is_mch_send'];
            $receive_id = $recv_msg['receive_id'];
            $send_id = $recv_msg['send_id'];

            $mch_id = $receive_id;
            $user_id = $send_id;
            $is_online = 0;
            $no_read_num = 0;

            if ($is_mch_send == 1) {
                $is_mch_send = 0;
                $mch_id = $send_id;
                $user_id = $receive_id;
            } else {
                $is_mch_send = 1;
            }

            $KEY = $receive_id . '_' . $is_mch_send;
            $client_key = $this->redis->get($KEY);

            if ($client_key && isset($this->clients[$client_key])) {
                $is_online = 1;
                $no_read_num = 1;

                return [
                    'msg' => [
                        'add_date' => date('Y-m-d H:i:s'),
                        'content' => $recv_msg['content'],
                        'is_mch_send' => $recv_msg['is_mch_send'],
                        'img' => $recv_msg['img'] ?? '',
                        'nike_name' => $recv_msg['nike_name'] ?? '',
                        'send_id' => $send_id,
                        'receive_id' => $receive_id
                    ],
                    'msg2' => [
                        'content' => $recv_msg['content'],
                        'headimgurl' => $recv_msg['img'] ?? '',
                        'user_name' => $recv_msg['nike_name'] ?? '',
                        'name' => $recv_msg['nike_name'] ?? '',
                        'user_id' => $user_id,
                        'mch_id' => $mch_id,
                        'is_online' => $is_online,
                        'no_read_num' => $no_read_num
                    ],
                    'type' => 'message',
                    'key' => $KEY,
                    'client_key' => $client_key
                ];
            }

            return null;
        }

        // ===== 关闭 / 退出 =====
        if ($recv_msg['type'] === 'quit') {
            if (!isset($recv_msg['send_id'], $recv_msg['receive_id'], $recv_msg['is_mch_send'])) {
                error_log("Quit missing fields");
                return null;
            }

            $send_id = $recv_msg['send_id'];
            $receive_id = $recv_msg['receive_id'];
            $is_mch_send = $recv_msg['is_mch_send'];

            $KEY = $receive_id . '_' . $is_mch_send;
            $client_key = $this->redis->get($KEY);

            if ($client_key) {
                unset($this->clients[$client_key]);
                $this->redis->remove($KEY);

                return [
                    'msg' => [
                        'type' => 'quit',
                        'add_date' => date('Y-m-d H:i:s'),
                        'is_mch_send' => $is_mch_send,
                        'img' => $recv_msg['img'] ?? '',
                        'nike_name' => $recv_msg['nike_name'] ?? '',
                        'send_id' => $send_id
                    ],
                    'key' => $KEY,
                    'client_key' => $client_key
                ];
            }

            return null;
        }

        // ===== 心跳 =====
        if ($recv_msg['type'] === 'heartbeat') {
            if (!isset($recv_msg['receive_id'], $recv_msg['is_mch_send'])) {
                return null;
            }

            $KEY = $recv_msg['receive_id'] . '_' . $recv_msg['is_mch_send'];
            $client_key = $this->redis->get($KEY);

            if ($client_key) {
                return [
                    'msg' => [
                        'type' => 'heartbeat',
                        'message' => '心跳成功！'
                    ],
                    'key' => $KEY,
                    'client_key' => $client_key
                ];
            }

            return null;
        }

        return null;
    }

}







