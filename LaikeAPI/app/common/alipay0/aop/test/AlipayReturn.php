<?php
/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */

namespace app\common\alipay0\aop\test;

use think\facade\Db;
use app\common\LKTConfigInfo;
use app\common\LaiKeLogUtils;

use app\common\alipay0\aop\AopClient;
use app\common\alipay0\aop\request\AlipayTradeRefundRequest;
use app\common\alipay0\aop\request\AlipayTradeFastpayRefundQueryRequest;

/**
 *
 * @author axiu
 * @version $Id: Test.hp, v 0.1 Aug 6, 2014 4:20:17 PM yikai.hu Exp $
 */
class AlipayReturn
{
    public static function refund($out_trade_no,$totalFee,$store_id,$id,$type='alipay') 
    {
        $out_request_no = $out_trade_no.$id;

        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        $log = new LaiKeLogUtils();

        if (empty($config)) 
        {
            $log->log("alipay/return.log","执行日期：".date('Y-m-d H:i:s')."支付暂未配置，无法退款！");
            return 'file';
        }

        $aop = new AopClient ();
        $aop->gatewayUrl = 'https://openapi.alipay.com/gateway.do';
        $aop->appId = $config['appid'];
        $aop->rsaPrivateKey = urldecode($config['rsaPrivateKey']);
        $aop->alipayrsaPublicKey = urldecode($config['alipayrsaPublicKey']);
        $aop->apiVersion = '1.0';
        $aop->signType = $config['signType'];
        $aop->postCharset = "UTF-8";
        $aop->format ='json';
        
        $object = array();
        $object['out_trade_no'] = $out_trade_no; // 设置商户订单号
        $object['refund_amount'] = round($totalFee, 2); // 设置退款金额
        $object['out_request_no'] = $out_request_no; // 设置退款请求号
        // $queryOptions = array();
        // $queryOptions[] = "refund_detail_item_list"; // 本次退款使用的资金渠道
        // $object['query_options'] = $queryOptions;

        $json = json_encode($object);

        $request = new AlipayTradeRefundRequest();
        $request->setBizContent($json);
        
        $result = $aop->execute($request); 
        
        $responseNode = str_replace(".", "_", $request->getApiMethodName()) . "_response";
        $resultCode = $result->$responseNode->code;
        $log->log("alipay/return.log","执行日期：".date('Y-m-d H:i:s').json_encode($result));

        if(!empty($resultCode)&&$resultCode == 10000)
        {
            $res = "success";
        } 
        else 
        {
            if($result->$responseNode->sub_code == 'ACQ.SELLER_BALANCE_NOT_ENOUGH')
            {
                $res = "商家余额不足！";
            }
            else
            {
                $res = '退款失败！';
            }
        }
        return $res;
    }
    
    // 查询退款详情
    public static function get_refund($out_trade_no,$store_id,$id,$type='alipay') 
    {
        $out_request_no = $out_trade_no.$id;

        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        $log = new LaiKeLogUtils();

        if (empty($config)) 
        {
            $log->log("alipay/return.log","执行日期：".date('Y-m-d H:i:s')."支付暂未配置，无法退款！");
            return 'file';
        }

        $aop = new AopClient ();
        $aop->gatewayUrl = 'https://openapi.alipay.com/gateway.do';
        $aop->appId = $config['appid'];
        $aop->rsaPrivateKey = urldecode($config['rsaPrivateKey']);
        $aop->alipayrsaPublicKey = urldecode($config['alipayrsaPublicKey']);
        $aop->signType = $config['signType'];
        $aop->postCharset = "UTF-8";
        $aop->format ='json';
        
        $object = array();
        $object['out_trade_no'] = $out_trade_no; // 设置商户订单号
        $object['out_request_no'] = $out_request_no; // 设置退款请求号

        $json = json_encode($object);

        $request = new AlipayTradeFastpayRefundQueryRequest();
        $request->setBizContent($json);
        
        $result = $aop->execute($request);

        $responseNode = str_replace(".", "_", $request->getApiMethodName()) . "_response";

        $resultCode = $result->$responseNode->code;

        $log->log("alipay/return.log","执行日期：".date('Y-m-d H:i:s').json_encode($result));

        if(!empty($resultCode)&&$resultCode == 10000)
        {
            $res = "success";
        } 
        else 
        {
            $res = '请求失败！';
        }
        return $res;
    }
}