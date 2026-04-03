<?php
namespace app\common\alipay;
require_once "AlipaySystemOauthTokenRequest.php";
require_once "AopClient.php";
use app\common\LKTConfigInfo;

class AlipayTools
{
    /**
     *  获取支付宝小程序用户userid
     * @param $store_id
     * @param $alimp_auth_code
     * @return SimpleXMLElement
     */
    public static function  getAliUserId($store_id,$alimp_auth_code){
        try{
            $config = LKTConfigInfo::getPayConfig($store_id,"alipay_minipay");
            if (empty($config)) {
                throw new Exception('获取支付宝小程序配置信息失败', 101);
            }
            $aop = new AopClient ();
            $aop->appId = $config["appid"];
            $aop->rsaPrivateKey = $config['rsaPrivateKey'];
            $aop->alipayrsaPublicKey = $config['alipayrsaPublicKey'];
            $aop->format = "json";
            $aop->charset="UTF-8";
            $aop->signType = "RSA2";
            $aop->apiVersion = '1.0';
            $request = new AlipaySystemOauthTokenRequest();
            $request->setGrantType("authorization_code");
            $request->setCode($alimp_auth_code);
            $result = $aop->execute($request);
            $user_id = $result->alipay_system_oauth_token_response->user_id;
            return $user_id;
        }catch (Exception $exception){
            throw $exception;
        }

    }

}