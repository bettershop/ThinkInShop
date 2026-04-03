<?php
/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */

require_once "AopClient.php";
require_once "AlipayTradeRefundRequest.php";
require_once "../app/common/LKTConfigInfo.php";
require_once '../app/common/LaiKeLogUtils.php';

/**
 *
 * @author axiu
 * @version $Id: Test.hp, v 0.1 Aug 6, 2014 4:20:17 PM yikai.hu Exp $
 */
class Alipay
{

    public static function refund($out_trade_no,$totalFee,$appid,$store_id,$id,$type='alipay') 
    {
        $out_request_no = $out_trade_no.$id;
        // require_once($appid . '_AliPay.Config.php');
        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        $log = new LaiKeLogUtils();

        if (empty($config)) 
        {
            $log->log("alipay/return.log","执行日期：".date('Y-m-d H:i:s')."支付暂未配置，无法退款！");
            return 'file';
        }

        $aop = new AopClient();
        $aop->gatewayUrl = 'https://openapi.alipay.com/gateway.do';
        $aop->appId = $config['appid'];
        $aop->rsaPrivateKey = $config['rsaPrivateKey'];
        $aop->alipayrsaPublicKey = $config['alipayrsaPublicKey'];
        $aop->apiVersion = '1.0';
        $aop->signType = $config['signType'];
        $aop->postCharset="UTF-8";
        $aop->format="json";
        $request = new AlipayTradeRefundRequest ();
        $request->setBizContent("{" .
                "\"out_trade_no\":\"".$out_trade_no."\"," .
                "\"refund_amount\":\"".$totalFee."\"," .
                "\"out_request_no\":\"".$out_request_no."\" ".
        "}");
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

}



