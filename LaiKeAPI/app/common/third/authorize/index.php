<?php
require_once "WXBizMsgCrypt.php";

class authorize
{

    private static $token          = 'openlaiketui';
    private static $encodingAesKey = 'nQvXVx3QB3gNf8RXpvZ3bHHTxmUEv0lnu3jQNpvq7LJ';
    private static $appId          = 'wxec35e18c814da497';

    //获取ticket
    public static function ticket()
    {
        echo 'ticket';
        $pc = new WXBizMsgCrypt($this->$token, $this->encodingAesKey, $this->appId);

        //接收微信传来参数
        $timeStamp  = empty($_GET['timeStamp']) ? "" : trim($_GET['timeStamp']);
        $nonce      = empty($_GET['nonce']) ? "" : trim($_GET['nonce']);
        $msg_sign   = empty($_GET['msg_signature']) ? "" : trim($_GET['msg_signature']);
        $encryptMsg = file_get_contents('php://input');

        $msg = '';

        //解密xml中的数据，获取
        $errCode = $pc->decryptMsg($msg_sign, $timeStamp, $nonce, $encryptMsg, $msg);
        if ($errCode == 0) {

            $config_0 = array();
            $data     = self::_xmlToArr($msg);
            if (isset($data['ComponentVerifyTicket'])) {

                $component_verify_ticket = $data['ComponentVerifyTicket'];
                $create_time             = date("Y-m-d H:i:s");
            }
            $db  = DBAction::getInstance();
            $sql = "insert into lkt_third (ticket,ticket_create_time) values('$component_verify_ticket','$create_time')";
            $res = $db->insert($sql);

            echo 'success';
        } else {

            echo '解密失败' . $errCode;
        }

    }

    public static function _xmlToArr($xml)
    {
        echo 'xml';
        self::ceshi();
        $res = @simplexml_load_string($xml, null, LIBXML_NOCDATA);
        $res = json_decode(json_encode($res), true);
        return $res;
    }
    private static function ceshi()
    {
        self::ceshi1();
        echo 'ceshi';
    }
    private static function ceshi1()
    {
        echo 'ceshi1';
    }
    //获取component_access_token
    public static function get_component_access_token()
    {

        $db         = DBAction::getInstance();
        $sql        = "select token_time,token from lkt_third where id = 1";
        $res        = $db->select($sql);
        $token_time = $res[0]->token_time;
        $token      = $res[0]->token;

        $differ_time = self::differ_time($token_time);
        if (empty($token_time) || empty($token) || $differ_time > 7200) {

            $token = self::refresh_component_access_token();
        }

        return $token;

    }

    //刷新过期token
    private static function refresh_component_access_token()
    {

        $db  = DBAction::getInstance();
        $sql = "select appid,appsecret,ticket from lkt_third where id = 1";
        $res = $db->select($sql);

    }
    //计算时间只差
    private static function differ_time($time)
    {

        $now_time   = time();
        $token_time = strtotime($time);
        $differ     = $now_time - $token_time;

        return $differ;
    }

}

authorize::_xmlToArr();
