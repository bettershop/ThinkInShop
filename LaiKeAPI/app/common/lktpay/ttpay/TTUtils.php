<?php
namespace app\common\lktpay\ttpay;

use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\LKTConfigInfo;
/**
 * 头条帮助类
 */
class TTUtils
{
    /**
     * 头条支付签名
     * @param $data
     * @param $ttpaysecret
     * @return mixed
     */
    public static function ttSignData($data, $ttpaysecret)
    {
        ksort($data);
        $var = '';
        foreach ($data as $key => $value) {
            $var .= $key . '=' . $value . '&';
        }
        $var = trim($var, '&');
        LaiKeLogUtils::lktLog(__METHOD__ . '->' . __LINE__ . " 排序后拼接:" . $var);
        $c = $var . $ttpaysecret;
        LaiKeLogUtils::lktLog(__METHOD__ . '->' . __LINE__ . " 签名前字符串:" . $c);
        $data['sign'] = strtolower(md5($c));
        LaiKeLogUtils::lktLog(__METHOD__ . '->' . __LINE__ . " 签名字符串:" . $data['sign']);
        ksort($data);
        return $data;
    }

    /**
     * 获取头条用户openid
     * @param $ttAppid
     * @param $ttAppSecret
     * @param $tt_authcode
     * @return mixed
     */
    public static function getTTOpenId($ttAppid, $ttAppSecret, $tt_authcode)
    {
        $log = new LaiKeLogUtils();

        $ttparams = array(
            "appid" => $ttAppid,
            "secret" => $ttAppSecret,
            "code" => $tt_authcode
        );
        $log->log('TTUtils/getTTOpenId.log',__METHOD__ . '->' . __LINE__ . " 获取头条openid参数:" . json_encode($ttparams));

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, "https://developer.toutiao.com/api/apps/jscode2session?" . http_build_query($ttparams));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
        $ttinfo = curl_exec($ch);
        curl_close($ch);
        $ttopeninfo = json_decode($ttinfo);

        if (!empty($ttopeninfo->error) && !empty($ttopeninfo->message)) 
        {
            echo json_encode(array("code" => $ttopeninfo->error, "msg" => $ttopeninfo->message));
            exit;
        }

        $log->log('TTUtils/getTTOpenId.log',__METHOD__ . '->' . __LINE__ . " 头条openidInfo:" . $ttinfo);
        $tt_openid = $ttopeninfo->openid;
        if (empty($tt_openid)) 
        {
            echo json_encode(array("code" => 501, "msg" => "openid为空!"));
            exit;
        }

        $log->log('TTUtils/getTTOpenId.log',__METHOD__ . '->' . __LINE__ . " 头条id:" . $tt_openid);
        return $tt_openid;
    }

    /**
     * 获取头条支付条件
     * @param $total
     * @param $ttpayappid
     * @param $tttradeno
     * @param $ttshid
     * @param $tt_openid
     * @param $zfbAPPurl
     * @param $signFields
     * @param $ttpaysecret
     * @param $riskIp
     * @return array
     */
    public static function getTTPayCondition($total, $ttpayappid, $tttradeno, $ttshid, $tt_openid, $zfbAPPurl, $ttpaysecret,$riskIp)
    {
        $responseToFrontData = array(
            "app_id" => $ttpayappid,
            "method" => "tp.trade.confirm",
            "sign" => "",
            "sign_type" => "MD5",
            "timestamp" => time() . "",
            "trade_no" => $tttradeno,
            "merchant_id" => $ttshid,
            "uid" => $tt_openid,
            "total_amount" => $total * 100,
            "pay_channel" => 'ALIPAY_NO_SIGN',
            "pay_type" => 'ALIPAY_APP',
            "risk_info" => json_encode(array('ip' => $riskIp)),
            "params" => json_encode(array("url" => $zfbAPPurl))
        );

        $signFields["app_id"] = $responseToFrontData["app_id"];
        $signFields["sign_type"] = $responseToFrontData["sign_type"];
        $signFields["timestamp"] = $responseToFrontData["timestamp"];
        $signFields["trade_no"] = $responseToFrontData["trade_no"];
        $signFields["merchant_id"] = $responseToFrontData["merchant_id"];
        $signFields["uid"] = $responseToFrontData["uid"];
        $signFields["total_amount"] = $responseToFrontData["total_amount"];
        $signFields["params"] = $responseToFrontData["params"];
        $signFields = self::ttSignData($signFields, $ttpaysecret);
        $responseToFrontData["sign"] = $signFields["sign"];
        return $responseToFrontData;
    }

    /**
     * 请求头条小程序后台输出头条订单号
     * @param $total
     * @param $title
     * @param $real_sno
     * @param $tt_openid
     * @param $ttshid
     * @param $valid_time
     * @param $ttzfbnotifycburl
     * @param $riskIp
     * @param $ttpayappid
     * @param $data
     * @param $ttpaysecret
     * @return bool|string
     */
    public function ttCreatOrder($total, $title, $real_sno, $tt_openid, $ttshid, $valid_time, $ttzfbnotifycburl, $riskIp, $ttpayappid, $ttpaysecret)
    {
        $log = new LaiKeLogUtils();
        $biz_content = array(
            'out_order_no' => $real_sno,
            'uid' => $tt_openid,
            'merchant_id' => $ttshid,
            'total_amount' => $total * 100,
            'currency' => 'CNY',
            'subject' => $title,
            'body' => $real_sno,
            'trade_time' => time(),
            'valid_time' => $valid_time,
            'notify_url' => $ttzfbnotifycburl,
            'risk_info' => array('ip' => $riskIp)
        );
        $log->log('TTUtils/ttCreatOrder.log',__METHOD__ . '->' . __LINE__ . " $biz_content:" . json_encode($biz_content));

        $biz_content = json_encode($biz_content);
        $data['app_id'] = $ttpayappid;
        $data['biz_content'] = $biz_content;
        $data['charset'] = 'utf-8';
        $data['method'] = 'tp.trade.create';
        $data['format'] = 'JSON';
        $data['sign_type'] = 'MD5';
        $data['timestamp'] = "" . time();
        $data['version'] = '1.0';

        $data = self::ttSignData($data, $ttpaysecret);

        $vars = 'app_id=' . $data['app_id']
            . '&biz_content=' . $data['biz_content']
            . '&charset=' . $data['charset']
            . '&method=' . $data['method']
            . '&format=' . $data['format']
            . '&sign=' . $data['sign']
            . '&sign_type=' . $data['sign_type']
            . '&timestamp=' . $data['timestamp']
            . '&version=' . $data['version'];

        $url = 'https://tp-pay.snssdk.com/gateway';
        //进行post请求
        $log->log('TTUtils/ttCreatOrder.log',__METHOD__ . '->' . __LINE__ . " 头条请求参数:" . json_encode($data));
        $headers = array("Content-Type: application/x-www-form-urlencoded");
        $result = Third::https_post($url, $vars, 1, $headers);
        return $result;
    }




}