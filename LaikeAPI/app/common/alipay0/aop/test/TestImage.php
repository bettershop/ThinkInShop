<?php

/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */

namespace app\common\alipay0\aop\test;

use think\facade\Db;
use app\common\LKTConfigInfo;
use app\common\LaiKeLogUtils;

use app\common\alipay0\aop\AlipayMobilePublicMultiMediaClient;
use app\common\alipay0\aop\AopClient;
use app\common\alipay0\aop\request\AlipayTradePrecreateRequest;
use app\common\alipay0\aop\request\AlipayTradeWapPayRequest;
use app\common\alipay0\aop\request\AlipayTradeAppPayRequest;
use app\common\alipay0\aop\request\AlipayTradeCreateRequest;
use app\common\alipay0\aop\request\AlipaySystemOauthTokenRequest;
use app\common\alipay0\aop\request\AlipayUserInfoShareRequest;

header("Content-type: text/html; charset=gbk");

/**
 *
 * @author wangYuanWai
 * @version $Id: Test.hp, v 0.1 Aug 6, 2014 4:20:17 PM yikai.hu Exp $
 */
class TestImage
{
	public $partner_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCitD16CypwZILTpdJL8nPV9rVFHYf5UWa/URNX6469mbQLpWfjKM/VSWRXsNVGSM3itOO/KG2Pw4x5g9xjH6iaE4LlaidjBIPpifISSlnpbyi4HxQTZYgMPv/TuiWofUN5kcwg/KQAQxB2OwTOeFu2i3LhqSCDmv6koTvHW15/hQIDAQAB";
	public $alipay_public_key   = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDIgHnOn7LLILlKETd6BFRJ0GqgS2Y3mn1wMQmyh9zEyWlz5p1zrahRahbXAfCfSqshSNfqOmAQzSHRVjCqjsAw1jyqrXaPdKBmr90DIpIxmIyKXv4GGAkPyJ/6FTFY99uhpiq0qadD/uSzQsefWo0aTvP/65zi3eof7TcZ32oWpwIDAQAB";
	//公用变量
	public $serverUrl = 'http://publicexprod.d5336aqcn.alipay.net/chat/multimedia.do'; //'http://publicexprod.d5336aqcn.alipay.net/chat/multimedia.do';//'http://i.com/works/photo-sdk/_data/1.jpg';//"http://i.com/works/photo-sdk/_data/publicexprod.php";//"http://publicexprod.d5336aqcn.alipay.net/chat/multimedia.do";
	public $appId = "2013121100055554";

	public $partner_private_key = 'MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKK0PXoLKnBkgtOl0kvyc9X2tUUdh/lRZr9RE1frjr2ZtAulZ+Moz9VJZFew1UZIzeK0478obY/DjHmD3GMfqJoTguVqJ2MEg+mJ8hJKWelvKLgfFBNliAw+/9O6Jah9Q3mRzCD8pABDEHY7BM54W7aLcuGpIIOa/qShO8dbXn+FAgMBAAECgYA8+nQ380taiDEIBZPFZv7G6AmT97doV3u8pDQttVjv8lUqMDm5RyhtdW4n91xXVR3ko4rfr9UwFkflmufUNp9HU9bHIVQS+HWLsPv9GypdTSNNp+nDn4JExUtAakJxZmGhCu/WjHIUzCoBCn6viernVC2L37NL1N4zrR73lSCk2QJBAPb/UOmtSx+PnA/mimqnFMMP3SX6cQmnynz9+63JlLjXD8rowRD2Z03U41Qfy+RED3yANZXCrE1V6vghYVmASYsCQQCoomZpeNxAKuUJZp+VaWi4WQeMW1KCK3aljaKLMZ57yb5Bsu+P3odyBk1AvYIPvdajAJiiikRdIDmi58dqfN0vAkEAjFX8LwjbCg+aaB5gvsA3t6ynxhBJcWb4UZQtD0zdRzhKLMuaBn05rKssjnuSaRuSgPaHe5OkOjx6yIiOuz98iQJAXIDpSMYhm5lsFiITPDScWzOLLnUR55HL/biaB1zqoODj2so7G2JoTiYiznamF9h9GuFC2TablbINq80U2NcxxQJBAMhw06Ha/U7qTjtAmr2qAuWSWvHU4ANu2h0RxYlKTpmWgO0f47jCOQhdC3T/RK7f38c7q8uPyi35eZ7S1e/PznY=';

	public $format = "json";
	public $charset = "GBK";

	function __construct()
	{
	}

	// 支付宝app支付
	public static function load($sNo, $total, $title, $store_id,$type='alipay') 
	{
		$log = new LaiKeLogUtils();
		$log->log('alipay/aliminipay.log', "支付宝app支付：" . $store_id);
		$config = LKTConfigInfo::getPayConfig($store_id, 'alipay');

		if (empty($config)) 
		{
			$log->log('alipay/aliminipay.log', "执行日期：" . date('Y-m-d H:i:s') . "\n支付暂未配置，无法调起支付！\n\n");
			return 'file';
		}

		$aop = new AopClient ();
		$aop->gatewayUrl = 'https://openapi.alipay.com/gateway.do';
		$aop->appId = $config['appid'];
		$aop->rsaPrivateKey = urldecode($config['rsaPrivateKey']);
		$aop->alipayrsaPublicKey = $config['alipayrsaPublicKey'];
		$aop->apiVersion = '1.0';
		$aop->signType = 'RSA2';
		$aop->postCharset='UTF-8';
		$aop->format='json';

		$object = array();
		$object['out_trade_no'] = $sNo;
		$object['total_amount'] = round($total, 2);
		$object['subject'] = $title;
		$object['product_code'] = 'QUICK_MSECURITY_PAY';
        // $object['time_expire'] = date("Y-m-d H:i:s");

		$json = json_encode($object);
		$request = new AlipayTradeAppPayRequest();
		$request->setNotifyUrl($config['notify_url']);
		$request->setBizContent($json);

		$result = $aop->sdkExecute($request); 
		$log->log('alipay/aliminipay.log', "result：" . json_encode($result));

		// $responseNode = str_replace(".", "_", $request->getApiMethodName()) . "_response";
		// $log->log('alipay/aliminipay.log', "responseNode：" . $responseNode);
		
		// $resultCode = $result->$responseNode->code;
		// $log->log('alipay/aliminipay.log', "resultCode：" . $resultCode);
		// if(!empty($resultCode)&&$resultCode == 10000)
		// {
		// 	$data = $result;
		// } 
		// else 
		// {
			$data = $result;
		// }

		$log->log('alipay/aliminipay.log', "【app调用支付】执行日期：" . date('Y-m-d H:i:s') . "\n" . json_encode($data) . "\n\n");

		return $data;
	}

	// 支付宝二维码支付
	public static function qrAlipay($sNo, $total, $title, $store_id)
	{
		$config = LKTConfigInfo::getPayConfig($store_id, 'pc_alipay');
		$log = new LaiKeLogUtils();

		$log->log('alipay/aliminipay.log', "【PC调用支付】支付配置：" . json_encode($config));
		if (empty($config)) 
		{
			$log->log('alipay/aliminipay.log', "执行日期：" . date('Y-m-d H:i:s') . "\n支付暂未配置，无法调起支付！\n\n");
			return 'file';
		}

		$aop = new AopClient();
		$aop->gatewayUrl = 'https://openapi.alipay.com/gateway.do';
		$aop->appId = $config['appid'];
		$aop->rsaPrivateKey = urldecode($config['rsaPrivateKey']);
		$aop->alipayrsaPublicKey = $config['alipayrsaPublicKey'];
		$aop->apiVersion = '1.0';
		$aop->signType = 'RSA2';
		$aop->postCharset = 'UTF-8';
		$aop->format = 'json';

		$object = array('out_trade_no' => $sNo, 'total_amount' => round($total, 2), 'subject' => $title);

		$json = json_encode($object);
		
		$log->log('alipay/aliminipay.log', "【PC调用支付】传参：" . $json);
		$request = new AlipayTradePrecreateRequest();
		$request->setNotifyUrl($config['notify_url']);
		$request->setBizContent($json);
		$result = $aop->execute($request);
		
		$responseNode = str_replace(".", "_", $request->getApiMethodName()) . "_response";
		$resultCode = $result->$responseNode->code;
		if (!empty($resultCode) && $resultCode == 10000) 
		{
			$code_url = $result->$responseNode->qr_code;
			$data['code_url'] = $code_url;
		} 
		else 
		{
			$data = $result;
		}

		$log->log('alipay/aliminipay.log', "【PC调用支付】执行日期：" . date('Y-m-d H:i:s') . "\n" . json_encode($data) . "\n\n");

		return $data;
	}

	// 支付宝H5支付
	public static function mobile_web($out_trade_no, $total_amount, $subject, $store_id, $type = 'alipay',$remarks = '')
	{
		$config = LKTConfigInfo::getPayConfig($store_id, $type);
		$log = new LaiKeLogUtils();

		if (empty($config)) 
		{
			$log->log('alipay/aliminipay.log', "执行日期：" . date('Y-m-d H:i:s') . "\n支付暂未配置，无法调起支付！\n\n");
			return 'file';
		}

		$aop = new AopClient();
		$aop->gatewayUrl = 'https://openapi.alipay.com/gateway.do';
		$aop->appId = $config['appid'];
		$aop->rsaPrivateKey = urldecode($config['rsaPrivateKey']);
		$aop->alipayrsaPublicKey = $config['alipayrsaPublicKey'];
		$aop->apiVersion = '1.0';
		$aop->signType = 'RSA2';
		$aop->postCharset = 'UTF-8';
		$aop->format = 'json';

		/******必传参数******/
		$object = array();
		//商户订单号，商家自定义，保持唯一性
		$object['out_trade_no'] = $out_trade_no;
		//支付金额，最小值0.01元
		$object['total_amount'] = round($total_amount, 2);
		//订单标题，不可使用特殊符号
		$object['subject'] = $subject;

		/******可选参数******/
		//手机网站支付默认传值FAST_INSTANT_TRADE_PAY
		$object['product_code'] = 'QUICK_MSECURITY_PAY';
		// $object['time_expire'] = date("Y-m-d H:i:s");

		$json = json_encode($object);
		$request = new AlipayTradeWapPayRequest();
		//异步接收地址，仅支持http/https，公网可访问
		$request->setNotifyUrl($config['notify_url']);
		//同步跳转地址，仅支持http/https

		$otype = substr($out_trade_no, 0, 2);
		$setReturnUrl = "https://tp.dev.laiketui.net/H5/#/pages/pay/PayResults";
		if($otype == 'CZ')
		{
			$time = date("Y-m-d H:i:s");

			$setReturnUrl = "https://tp.dev.laiketui.net/H5/#/pagesB/myWallet/rechargeSuccess?money=" . round($total_amount, 2) . "&time=" . $time . "&remarks=" . $remarks . '&type=' . $type . '&mylei=1';
		}
		$request->setReturnUrl($setReturnUrl); // 支付宝返回路径

		$request->setBizContent($json);
		$result = $aop->pageExecute($request);
		
		// $responseNode = str_replace(".", "_", $request->getApiMethodName()) . "_response";
		// $resultCode = $result->$responseNode->code;
		// if (!empty($resultCode) && $resultCode == 10000) 
		// {
		// 	$data = $result;
		// } 
		// else 
		// {
			$data = $result;
		// }
		$log->log('alipay/aliminipay.log', "【H5调用支付】执行日期：" . date('Y-m-d H:i:s') . "\n" . json_encode($data) . "\n\n");

		return $data;
	}

	// 统一收单交易创建接口
	public static function loadMPAlipay($sNo, $total, $title, $appid,$store_id,$alimp_auth_code,$type='alipay_minipay')
	{
		$config = LKTConfigInfo::getPayConfig($store_id, $type);
		$log = new LaiKeLogUtils();

		if (empty($config)) 
		{
			$log->log('alipay/aliminipay.log', "执行日期：" . date('Y-m-d H:i:s') . "\n支付暂未配置，无法调起支付！\n\n");
			return 'file';
		}

		$aop = new AopClient ();
		$aop->gatewayUrl = 'https://openapi.alipay.com/gateway.do';
		$aop->appId = $config['appid'];
		$aop->rsaPrivateKey = urldecode($config['rsaPrivateKey']);
		$aop->alipayrsaPublicKey = $config['alipayrsaPublicKey'];
		$aop->apiVersion = '1.0';
		$aop->signType = 'RSA2';
		$aop->postCharset = 'UTF-8';
		$aop->format='json';

        $buyerid = null;
		$log->log('alipay/aliminipay.log',"支付宝小程序授权码为：".$alimp_auth_code);

        if(!empty($alimp_auth_code))
        {
            $request1 = new AlipaySystemOauthTokenRequest();
            $request1->setGrantType("authorization_code");
            $request1->setCode($alimp_auth_code);
            $result1 = $aop->execute($request1);
            $buyerid = $result1->alipay_system_oauth_token_response->user_id;
            $log->log('alipay/aliminipay.log',"获取用户userid成功：".$buyerid."行号:".__LINE__);
        }
        else
        {
            $log->log('alipay/aliminipay.log',"支付宝小程序授权码为空！");
            return null;
        }

		$object = array();
		$object['out_trade_no'] = $sNo;
		$object['total_amount'] = round($total, 2);
		$object['subject'] = $title;
		$object['buyer_id'] = $buyerid; // 买家支付宝用户ID。
		// $object['timeout_express'] = '10m'; // 订单相对超时时间。从交易创建时间开始计算。

		$json = json_encode($object);
		$request = new AlipayTradeCreateRequest();
		$request->setNotifyUrl($config['notify_url']);
		$request->setBizContent($json);
		$result = $aop->execute ( $request); 

		$responseNode = str_replace(".", "_", $request->getApiMethodName()) . "_response";
		// $resultCode = $result->$responseNode->code;
		// if(!empty($resultCode)&&$resultCode == 10000)
		// {
		// 	echo "成功";
		// } 
		// else 
		// {
		// 	$data = $result;
		// }

		$log->log('alipay/aliminipay.log', "【小程序调用支付】执行日期：" . date('Y-m-d H:i:s') . "\n" . json_encode($responseNode) . "\n\n");

		// return $data;
		return $result->$responseNode;
	}


	//获取令牌
    public static function getToken($store_id,$code)
    {
        $config = LKTConfigInfo::getPayConfig($store_id,'alipay');
        $log = new LaiKeLogUtils();
        if (empty($config)) 
        {
            $log -> log('alipay/alipay.log',"执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置，无法调起授权！\n\n");
            return false;
        }
        
        $aop = new AopClient ();
        $aop->gatewayUrl = 'https://openapi.alipay.com/gateway.do';
        //小程序appid
        $aop->appId = $config['appid'];
        //私钥 PKCS1,2048位
        $aop->rsaPrivateKey = urldecode($config['rsaPrivateKey']);
        //支付宝公钥对应的 支付宝公钥从开放平台获取 （非应用公钥）
        $aop->alipayrsaPublicKey = urldecode($config['alipayrsaPublicKey']);
        $aop->apiVersion = '1.0';
        $aop->signType = 'RSA2';
        $aop->postCharset='UTF-8';
        $aop->format='json';
        $request = new AlipaySystemOauthTokenRequest();
        $request->setCode($code);
        $request->setGrantType("authorization_code");
        $responseResult = $aop->execute($request);
        if(isset($responseResult->alipay_system_oauth_token_response))
        {   
            $response = $responseResult->alipay_system_oauth_token_response;
            return $response->access_token;
        }
        else
        {   

            file_put_contents('alipay_res.php', print_r($responseResult,true));
            return false;
        }
    }

    //获取用户信息
    public static function getUser($store_id,$accessToken)
    {   

        $config = LKTConfigInfo::getPayConfig($store_id,'alipay');
        $log = new LaiKeLogUtils();
        if (empty($config)) 
        {
            $log -> log('alipay/alipay.log',"执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置，无法调起授权！\n\n");
            return false;
        }
        $aop = new AopClient ();
        $aop->gatewayUrl = 'https://openapi.alipay.com/gateway.do';
        //小程序appid
        $aop->appId = $config['appid'];
        //私钥 PKCS1,2048位
        $aop->rsaPrivateKey = urldecode($config['rsaPrivateKey']);
        //支付宝公钥对应的 支付宝公钥从开放平台获取 （非应用公钥）
        $aop->alipayrsaPublicKey = urldecode($config['alipayrsaPublicKey']);
        $aop->apiVersion = '1.0';
        $aop->signType = 'RSA2';
        $aop->postCharset='UTF-8';
        $aop->format='json';
        $request = new AlipayUserInfoShareRequest();
        $result = $aop->execute($request,$accessToken);
        if(isset($result->alipay_user_info_share_response))
        {   
            $response = $result->alipay_user_info_share_response;
            return $response;
        } 
        else 
        {   
            file_put_contents('alipayuser.php', print_r($result,true));
            return false;
        }
    }
}
