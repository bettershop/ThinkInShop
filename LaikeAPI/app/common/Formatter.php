<?php
namespace app\common;

class Formatter
{
    /**
     * Generate a random BASE62 string aka `nonce`, similar as `random_bytes`.
     *
     * @param int $size - Nonce string length, default is 32.
     *
     * @return string - base62 random string.
     */
    public static function nonce(int $size = 32): string
    {
        if ($size < 1) {
            throw new Exception('Size must be a positive integer.');
        }
        return implode('', array_map(static function(string $c): string {
            return '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'[ord($c) % 62];
        }, str_split(random_bytes($size))));
    }

    /**
     * Retrieve the current `Unix` timestamp.
     *
     * @return int - Epoch timestamp.
     */
    public static function timestamp(): int
    {
        return time();
    }

    /**
     * Formatting for the heading `Authorization` value.
     *
     * @param string $mchid - The merchant ID.
     * @param string $nonce - The Nonce string.
     * @param string $signature - The base64-encoded `Rsa::sign` ciphertext.
     * @param string $timestamp - The `Unix` timestamp.
     * @param string $serial - The serial number of the merchant public certification.
     *
     * @return string - The APIv3 Authorization `header` value
     */
    public static function authorization(string $mchid, string $nonce, string $signature, string $timestamp, string $serial): string
    { 
        $token = sprintf('mchid="%s",nonce_str="%s",timestamp="%d",serial_no="%s",signature="%s"',
        $mchid, $nonce, $timestamp, $serial, $signature);
        return $token; 
    }

    /**
     * Formatting this `HTTP::request` for `Rsa::sign` input.
     *
     * @param string $method - The HTTP verb, must be the uppercase sting.
     * @param string $uri - Combined string with `URL::pathname` and `URL::search`.
     * @param string $timestamp - The `Unix` timestamp, should be the one used in `authorization`.
     * @param string $nonce - The `Nonce` string, should be the one used in `authorization`.
     * @param string $body - The playload string, HTTP `GET` should be an empty string.
     *
     * @return string - The content for `Rsa::sign`
     */
    public static function request(string $method, string $uri, string $timestamp, string $nonce, string $body = ''): string
    {
        return static::joinedByLineFeed($method, $uri, $timestamp, $nonce, $body);
    }

    /**
     * Formatting this `HTTP::response` for `Rsa::verify` input.
     *
     * @param string $timestamp - The `Unix` timestamp, should be the one from `response::headers[Wechatpay-Timestamp]`.
     * @param string $nonce - The `Nonce` string, should be the one from `response::headers[Wechatpay-Nonce]`.
     * @param string $body - The response payload string, HTTP status(`201`, `204`) should be an empty string.
     *
     * @return string - The content for `Rsa::verify`
     */
    public static function response(string $timestamp, string $nonce, string $body = ''): string
    {
        return static::joinedByLineFeed($timestamp, $nonce, $body);
    }

    /**
     * Joined this inputs by for `Line Feed`(LF) char.
     *
     * @param string|float|int|bool $pieces - The scalar variable(s).
     *
     * @return string - The joined string.
     */
    public static function joinedByLineFeed(...$pieces): string
    {
        return implode("\n", array_merge($pieces, ['']));
    }

    /**
     * Sort an array by key with `SORT_STRING` flag.
     *
     * @param array<string, string|int> $thing - The input array.
     *
     * @return array<string, string|int> - The sorted array.
     */
    public static function ksort(array $thing = []): array
    {
        ksort($thing, SORT_STRING);

        return $thing;
    }

    /**
     * Like `queryString` does but without the `sign` and `empty value` entities.
     *
     * @param array<string, string|int|null> $thing - The input array.
     *
     * @return string - The `key=value` pair string whose joined by `&` char.
     */
    public static function queryStringLike(array $thing = []): string
    {
        $data = [];

        foreach ($thing as $key => $value) {
            if ($key === 'sign' || is_null($value) || $value === '') {
                continue;
            }
            $data[] = implode('=', [$key, $value]);
        }

        return implode('&', $data);
    }
    public static function curlPostWithWx($params,$authorization,$url,$serial)
    {
        $paramsString = json_encode($params);
        // 初始化curl
        $ch = curl_init();
        // 设置超时
        curl_setopt($ch, CURLOPT_TIMEOUT, 60);
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, FALSE);
        curl_setopt($ch, CURLOPT_HEADER, FALSE);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
        // post数据
        curl_setopt($ch, CURLOPT_POST, 1);
        // post的变量
        curl_setopt($ch, CURLOPT_POSTFIELDS, $paramsString);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array(
                'Content-Type: application/json; charset=utf-8',
                'Content-Length: ' . strlen($paramsString),
                'Authorization: ' . "WECHATPAY2-SHA256-RSA2048 " . $authorization,
                'Wechatpay-Serial: '. $serial,
                'Accept: application/json',
                'User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36'
            )
        );
        // 运行curl，结果以jason形式返回
        $res = curl_exec($ch);
        curl_close($ch);
        // 取出数据
        $data = json_decode($res, true);
        return $data;
    }

    /**
     * get请求
     * @param $url
     * @param $authorization
     * @return mixed
     */
    public static function curlGetWithWx($url, $authorization,$params=[])
    {
        $paramsString = json_encode($params);
        // 初始化curl
        $ch = curl_init();
        // 设置超时
        curl_setopt($ch, CURLOPT_TIMEOUT, 60);
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, FALSE);
        curl_setopt($ch, CURLOPT_HEADER, FALSE);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array(
                'Authorization: ' . "WECHATPAY2-SHA256-RSA2048 " . $authorization,
                'Accept: application/json',
                'User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36'
            )
        );
        // 运行curl，结果以jason形式返回
        $res = curl_exec($ch);
        curl_close($ch);
        // 取出数据
        $data = json_decode($res, true);
        //返回
        return $data;
    }
    
    public static function getAuthorization($url, $body,$mch_id, $priKey, $serial, $http_method = "POST")
    {
        // Authorization: <schema> <token>
        $url_parts = parse_url($url);
        $canonical_url = ($url_parts['path'] . (!empty($url_parts['query']) ? "?${url_parts['query']}" : ""));
        $timestamp = static::timestamp();
        $nonce = static::nonce();

        $message = static::request($http_method, $canonical_url, $timestamp, $nonce, json_encode($body));

        openssl_sign($message, $raw_sign, $priKey, 'SHA256');
        $sign = base64_encode($raw_sign);
       
        return static::authorization($mch_id, $nonce, $sign, $timestamp, $serial);
    }
 }  