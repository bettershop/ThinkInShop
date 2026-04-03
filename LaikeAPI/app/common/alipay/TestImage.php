<?php

/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
namespace app\common\alipay;

require_once "AopClient.php";
require_once "AlipayTradeAppPayRequest.php";
require_once "AlipayTradeCreateRequest.php";
require_once "AlipaySystemOauthTokenRequest.php";
require_once "AlipayOpenAppQrcodeCreateRequest.php";
require_once "AlipayTools.class.php";
require_once "AlipayTradePrecreateRequest.php";
// require_once "../app/common/LKTConfigInfo.php";
// require_once '../app/common/LaiKeLogUtils.php';
require_once "../app/common/phpqrcode.php";

use think\facade\Db;
use app\common\LKTConfigInfo;
use app\common\LaiKeLogUtils;

/**
 * 支付宝
 * @author axiu
 * @version $Id: Test.hp, v 0.1 Aug 6, 2014 4:20:17 PM yikai.hu Exp $
 */

class TestImage
{

    /**

     * @param sNo
     * @param $total
     * @param $title
     * @param $appid
     * @param $store_id
     * @param string $type
     * @return string

     */

	public static function load($sNo, $total, $title, $appid,$store_id,$type='alipay') 
    {
        $config = LKTConfigInfo::getPayConfig($store_id,$type);
        $log = new LaiKeLogUtils();

        if (empty($config)) 
        {
            $log->log('alipay/load.log',"执行日期：".date('Y-m-d H:i:s')."支付暂未配置，无法调起支付！");
            return 'file';
        }
        $aop = new AopClient;
        $aop->gatewayUrl = "https://openapi.alipay.com/gateway.do";
        $aop->appId = $config['appid'];
        $aop->rsaPrivateKey = $config['rsaPrivateKey'];
        $aop->alipayrsaPublicKey = $config['alipayrsaPublicKey'];
        $aop->format = "json";
        $aop->charset="UTF-8";
        $aop->signType = $config['signType'];

        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        $request = new AlipayTradeAppPayRequest();
        //SDK已经封装掉了公共参数，这里只需要传入业务参数
        $bizcontent = "{\"subject\": \"".$title."\","
                        . "\"out_trade_no\": \"".$sNo."\","
                        . "\"total_amount\": \"".$total."\","
                        . "\"product_code\":\"QUICK_MSECURITY_PAY\""
                        . "}";
        $request->setNotifyUrl($config['notify_url']);
        $request->setBizContent($bizcontent);

        //这里和普通的接口调用不同，使用的是sdkExecute
        $response = $aop->sdkExecute($request,$aop);

        $log->log('alipay/load.log',"【APP调用支付】执行日期：".date('Y-m-d H:i:s').json_encode($response));

        //htmlspecialchars是为了输出到页面时防止被浏览器将关键参数html转义，实际打印到日志以及http传输不会有这个问题
        return $response;//就是orderString 可以直接给客户端请求，无需再做处理。
	}

    /**
     * @param $out_trade_no
     * @param $total_amount
     * @param $subject
     * @param $appid
     * @param $store_id
     * @param string $type
     * @return mixed|SimpleXMLElement|string|提交表单HTML文本
     */
    public static function mobile_web($out_trade_no, $total_amount, $subject,$store_id,$type='alipay') 
    {
        $config = LKTConfigInfo::getPayConfig($store_id,$type);
        require_once('AlipayTradeService.php');
        require_once('AlipayTradeWapPayContentBuilder.php');
        $log = new LaiKeLogUtils();
        if (empty($config)) 
        {
            $log->log('alipay/h5.log',"执行日期：".date('Y-m-d H:i:s')."支付暂未配置，无法调起支付！");
            return 'file';
        }
        $aop = new AopClient ();
        //小程序appid
        $aop->appId = $config["appid"];
        //私钥 PKCS1,2048位
        $aop->rsaPrivateKey = $config['rsaPrivateKey'];
        //支付宝公钥对应的 支付宝公钥从开放平台获取 （非应用公钥）
        $aop->alipayrsaPublicKey = $config['alipayrsaPublicKey'];
        $aop->format = "json";
        $aop->charset="UTF-8";
        $aop->signType = "RSA2";
        $aop->postCharset='UTF-8';
        $aop->apiVersion = '1.0';
        $request = new AlipayTradeWapPayRequest();
        $bizcontent = "{\"subject\": \"".$subject."\","
            . "\"out_trade_no\": \"".$out_trade_no."\","
            . "\"total_amount\": \"".$total_amount."\","
            . "\"product_code\":\"QUICK_MSECURITY_PAY\""
            . "}";
        $request->setBizContent($bizcontent);
        $request->setNotifyUrl($config['notify_url']);
        $order_type = substr($out_trade_no, 0, 2);
        $h5domain = LKTConfigInfo::getH5Domain($store_id)."pages/pay/payResult?sNo=".$out_trade_no."&payment_money=".$total_amount."&order_type=".$order_type."&isH5=true";
        $request->setReturnUrl($h5domain);
        $result = $aop->pageExecute($request);

        $log->log('alipay/h5.log',"【H5调用支付】执行日期：".date('Y-m-d H:i:s').json_encode($result));

        return $result;//就是orderString 可以直接给客户端请求，无需再做处理。
    }

    /**

     * 支付宝小程序支付

     * @param $sNo

     * @param $total

     * @param $title

     * @param $appid

     * @param $store_id

     * @param string $type

     * @throws Exception

     */

    public static function loadMPAlipay($sNo, $total, $title, $appid,$store_id,$alimp_auth_code,$type='alipay_minipay') 
    {
        $config = LKTConfigInfo::getPayConfig($store_id,$type);
        $log = new LaiKeLogUtils();

        if (empty($config)) 
        {
            $log->log('alipay/aliminipay.log',"执行日期：".date('Y-m-d H:i:s')."支付暂未配置，无法调起支付！");
            return 'file';
        }

        $aop = new AopClient ();
        //小程序appid
        $aop->appId = $config["appid"];
        //私钥 PKCS1,2048位
        $aop->rsaPrivateKey = $config['rsaPrivateKey'];
        //支付宝公钥对应的 支付宝公钥从开放平台获取 （非应用公钥）
        $aop->alipayrsaPublicKey = $config['alipayrsaPublicKey'];
        $aop->format = "json";
        $aop->charset="UTF-8";
        $aop->signType = "RSA2";
        $aop->apiVersion = '1.0';

        $buyerid = null;
        $log->log('alipay/aliminipay.log',"支付宝小程序授权码为:".$alimp_auth_code);

        if(!empty($alimp_auth_code))
        {
            $request1 = new AlipaySystemOauthTokenRequest();
            $request1->setGrantType("authorization_code");
            $request1->setCode($alimp_auth_code);
            $result1 = $aop->execute($request1);
            $buyerid = $result1->alipay_system_oauth_token_response->user_id;
            $log->log('alipay/aliminipay.log',"获取用户userid成功:".$buyerid."行号:".__LINE__);
        }
        else
        {
            $log->log('alipay/aliminipay.log',"支付宝小程序授权码为空！");
            return null;
        }

        $request = new AlipayTradeCreateRequest ();
        $request->setBizContent("{" .
            "\"out_trade_no\":\"".$sNo."\"," .
            "\"total_amount\": \"".$total."\",".
            "\"buyer_id\":\"".$buyerid."\"," .
            "\"subject\":\"".$title."\"" .
            "  }");

        //设置回调地址
        $request->setNotifyUrl($config['notify_url']);
        $result = $aop->execute($request);
        $responseNode = str_replace(".", "_", $request->getApiMethodName()) . "_response";

        $log->log('alipay/aliminipay.log',"支付宝小程序支付结果:".json_encode($result));

        return $result->$responseNode;
    }



    public static function getcode($url,$query_param, $uploadImg,$store_id,$describe=''){



        $config = LKTConfigInfo::getPayConfig($store_id,'alipay_minipay');

        $log = new LaiKeLogUtils('alipay/aliminipay.log');

        if (empty($config)) {

            $log -> customerLog("执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置，无法调起支付！\n\n");

            return 'file';

        }



        $aop = new AopClient ();

        $aop->gatewayUrl = 'https://openapi.alipay.com/gateway.do';

        $aop->appId = $config["appid"];

        $aop->rsaPrivateKey = $config['rsaPrivateKey'];

        $aop->alipayrsaPublicKey=$config['alipayrsaPublicKey'];

        $aop->apiVersion = '1.0';

        $aop->signType = 'RSA2';

        $aop->postCharset='UTF-8';

        $aop->format='json';

        $request = new AlipayOpenAppQrcodeCreateRequest ();

        $request->setBizContent("{" .

        "\"url_param\":\"".$url."\"," .

        "\"query_param\":\"".$query_param."\"," .

        "\"describe\":\"".$describe."\"" .

        "  }");

        $result = $aop->execute ( $request); 



        $responseNode = str_replace(".", "_", $request->getApiMethodName()) . "_response";

        $resultCode = $result->$responseNode->code;

        if(!empty($resultCode)&&$resultCode == 10000){

            return $result->$responseNode->qr_code_url;

        } else {

            return 'file';

        }

    }



    public static function qrAlipay($sNo, $total, $title,$store_id)
    {
        $config = LKTConfigInfo::getPayConfig($store_id,'pc_alipay');

        $log = new LaiKeLogUtils();

        if (empty($config)) 
        {
            $log -> log('alipay/aliminipay.log',"执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置，无法调起支付！\n\n");
            return 'file';
        }

        $aop = new AopClient ();

        $aop->gatewayUrl = 'https://openapi.alipay.com/gateway.do';
        $aop->appId = $config["appid"];
        $aop->rsaPrivateKey = $config['rsaPrivateKey'];
        $aop->alipayrsaPublicKey=$config['alipayrsaPublicKey'];
        $aop->apiVersion = '1.0';
        $aop->signType = 'RSA2';
        $aop->postCharset='UTF-8';

        $request = new AlipayTradePrecreateRequest ();
        $request->setNotifyUrl($config['notify_url']);
        $request->setBizContent("{" .
        "\"out_trade_no\":\"".$sNo."\"," .
        "\"total_amount\":\"".$total."\"," .
        "\"subject\":\"".$title."\"" .
        "  }");
        $aop->format='json';
       
        $result = $aop->execute ($request);

        $responseNode = str_replace(".", "_", $request->getApiMethodName()) . "_response";
        $resultCode = $result->$responseNode->code;

        if(!empty($resultCode)&&$resultCode == 10000){

            $uploadImg = dirname(MO_WEBAPP_DIR) . '/code_img' ; // 图片上传位置

            if (is_dir($uploadImg) == '')

            { // 如果文件不存在

                mkdir($uploadImg); // 创建文件

            }



            $uploadImg = dirname(MO_WEBAPP_DIR) . '/code_img/'.date('Y-m-d') . '/' ; // 图片上传位置

            if (is_dir($uploadImg) == '')

            { // 如果文件不存在

                mkdir($uploadImg); // 创建文件

            }

            $code_url = $result->$responseNode->qr_code;

            // $imgURL_name = time() . mt_rand(1, 1000) . '.png';

            // $uploadImg1 = 'code_img/'.date('Y-m-d') . '/' . $imgURL_name;

            // app('QRcode')::png($code_url,$uploadImg1);

            // $QR = $uploadImg1; //已经生成的原始二维码图片文件

            // imagepng($QR, 'qrcode.png');       

            // imagedestroy($QR);

            $data['code_url'] = $code_url;

        } else {

            $data = $result;

        }

        $log -> log('alipay/aliminipay.log',"【PC调用支付】执行日期：".date('Y-m-d H:i:s')."\n".json_encode($data)."\n\n");

        return $data;

    }

}