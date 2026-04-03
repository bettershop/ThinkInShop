<?php

require_once "NuomiRsaSign.php";
require_once "./webapp/lib/LKTConfigInfo.class.php";
require_once './webapp/lib/LaiKeLogUtils.class.php';

/**
 *
 * @author axiu
 * @version $Id: Test.hp, v 0.1 Aug 6, 2014 4:20:17 PM yikai.hu Exp $
 */
class Baidupay{

        public static function refund($orderId,$baiduId,$out_trade_no,$totalFee,$appid,$store_id,$type='baidu_pay') {

                $config = LKTConfigInfo::getPayConfig($store_id,$type);

                $log = new LaiKeLogUtils('common/return.log');

                if (empty($config)) {

                        $log -> customerLog("执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置，无法退款！\r\n");

                        return 'file';
                }

                // 取消核销
                // $requesturl = 'https://nop.nuomi.com/nop/server/rest';
                // $data = array(
                //         'method' => 'nuomi.cashier.syncorderstatus',
                //         'orderId' => $orderId,
                //         'userId' => $baiduId,
                //         'type' => 3,
                //         'appKey' => $config['appkey']
                // );

                // $data['rsaSign'] = NuomiRsaSign::genSignWithRsa($data, $config['rsaPrivateKey']);

                // $re = self::http_request($requesturl,$data);


                // 申请退款
                $requesturl = 'https://nop.nuomi.com/nop/server/rest';
                $dataa = array(
                        'method' => urlencode('nuomi.cashier.applyorderrefund'),
                        'orderId' => $orderId,
                        'userId' => $baiduId,
                        'refundType' => 1,// 退款类型：1：用户发起退款；2：业务方客服退款；3：业务方服务异常退款
                        'refundReason' => urlencode("充值未到账"),// 业务方发起退款的原因
                        'tpOrderId' => $out_trade_no,
                        'appKey' => $config['appkey'],
                        'applyRefundMoney' => intval(floatval($totalFee)*100),
                        'bizRefundBatchId' => $out_trade_no
                );

                $dataa['rsaSign'] = NuomiRsaSign::genSignWithRsa($dataa, $config['rsaPrivateKey']);

                $resp = self::http_request($requesturl,$dataa);

                //var_dump($dataa,json_decode($resp));die;

                $log -> customerLog("执行日期：".date('Y-m-d H:i:s')."\n".$resp."\r\n");

                $res = (array)json_decode($resp);

                if ($res['msg'] == 'success') {
                        return $res['msg'];
                }

                return 'file';

        }

        //curl请求
        public static function http_request($url,$data = null,$headers=array())
        {
                $curl = curl_init();
                if( count($headers) >= 1 ){
                    curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);
                }
                curl_setopt($curl, CURLOPT_URL, $url);

                curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, FALSE);
                curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, FALSE);

                if (!empty($data)){
                    curl_setopt($curl, CURLOPT_POST, 1);
                    curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
                }
                curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
                $output = curl_exec($curl);
                curl_close($curl);
                return $output;
        }


}



