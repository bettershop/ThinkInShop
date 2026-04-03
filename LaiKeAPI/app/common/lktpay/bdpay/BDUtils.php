<?php
namespace app\common\lktpay\bdpay;

use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\third\authorize\Third;
/**
 * 百度帮助类
 */
class BDUtils
{
    /**
     * 获取百度用户openid
     * @param $bdAppid
     * @param $bdAppSecret
     * @param $bd_authcode
     * @return mixed
     */
    public static function getBDOpenId($bdAppid, $bdAppSecret, $bd_authcode){

        $data = "code=".$bd_authcode."&client_id=".$bdAppid."&sk=".$bdAppSecret;
        LaiKeLogUtils::lktLog(__METHOD__ . '->' . __LINE__ . " 获取百度openid参数:" . $data);
        $url= 'https://openapi.baidu.com/nalogin/getSessionKeyByCode';
        $info = Third::https_post($url, $data, 1,null);
        $openinfo = json_decode($info);

        if (!empty($openinfo->error) && !empty($openinfo->error_description)) {
            echo json_encode(array("code" => $openinfo->error, "msg" => $openinfo->error_description));
            exit;
        }

        LaiKeLogUtils::lktLog(__METHOD__ . '->' . __LINE__ . " 百度openidInfo:" . json_encode($openinfo));
        $bd_openid = $openinfo->openid;
        if (empty($bd_openid)) {
            echo json_encode(array("code" => 501, "msg" => "openid empty!"));
            exit;
        }

        LaiKeLogUtils::lktLog(__METHOD__ . '->' . __LINE__ . " 百度id:" . $bd_openid);
        return $bd_openid;
    }



}