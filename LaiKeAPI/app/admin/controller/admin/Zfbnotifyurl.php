<?php

namespace app\admin\controller\admin;

date_default_timezone_set('Asia/Chongqing');
set_time_limit(0);

use app\common;

require_once(MO_LIB_DIR . '/alipay0/log_.php');

use app\common\Plugin\PluginUtils;
use app\common\Plugin\LaiKeLogUtils;
use app\common\alipay0\aop\AopClient;
use app\common\Plugin\RefundUtils;

/**
 * 功能：支付宝回调
 * 修改人：dHb
 */
class Zfbnotifyurl
{
    public function apply()
    {
        $log = new \Log_();
        $log_name = './zfbnotify_url.log'; //log文件路径

        $log->log_result($log_name, "【支付宝接收到的notify通知】：" . json_encode($_POST));
        $log->log_result($log_name, "【支付宝接收到的notify通知】：" . json_encode($_GET));
        $data = $_POST;
        if (empty($data)) 
        {
            $data = $_GET;
        }

        // $asd = '{"gmt_create":"2025-05-15 17:37:17","charset":"UTF-8","gmt_payment":"2025-05-15 17:37:18","seller_email":"18974896358@163.com","notify_time":"2025-05-15 17:38:49","subject":"\u9c9c\u798f\u6797\u6b63\u5b97\u56db\u5ddd\u65b0\u9c9c\u751c","gmt_refund":"2025-05-15 17:38:48.536","sign":"JfEYf0xauX4i3kYaV1tDZHHpWYYzIYYD+GatCxtfaFMbq1qK+CXjKxgO9BSGtVG41bRLgw2UAXYpWd0VX\/edMpzZT0R7OnyeQASsxbkYfdMEIg\/VFwZbdnoyIJJJNdrZlbJYkD5DUNq8pn7zIh6XK91glImOCYnCbZtEBPzgcjbNt3MyLWVz4402H27Ve7oAt6jWB+FDalm9lpX5jRm2KlnUQNvOb5qgiZZDrR4Z1ptimc0D4ue54rRsZwDA4NYWryTXILNLYIWascjEL52eOhBqf0uIAH1cWWQfNWpRvCdAwz5nReti0FKECHbeaMUMBOmfu7p8V9khGwDKFNypEA==","buyer_id":"2088602239987604","out_biz_no":"GM250515053708104477599","version":"1.0","notify_id":"2025051501222173848087601418874823","notify_type":"trade_status_sync","out_trade_no":"GM250515053708104477","total_amount":"0.01","refund_fee":"0.01","trade_status":"TRADE_CLOSED","trade_no":"2025051522001487601437036408","auth_app_id":"2021001171664275","gmt_close":"2025-05-15 17:38:48","buyer_logon_id":"274***@qq.com","app_id":"2021001171664275","sign_type":"RSA2","seller_id":"2088031650461695"}';
        // $data = json_decode($asd,true);
        if (empty($data))
        {
            echo "error";
            exit;
        }

        // if(isset($data['out_biz_no']) && isset($data['refund_fee']) && isset($data['gmt_refund']))
        // {
        //     $log->log_result($log_name, "【支付宝退款回调】：" . json_encode($data));
            
        //     $out_biz_no = $data['out_biz_no'];
        //     $trade_no = $data['out_trade_no'];
        //     $refund_fee = $data['refund_fee'];

        //     $type = substr($trade_no, 0, 2);

        //     $array = array('out_biz_no'=>$out_biz_no,'trade_no'=>$trade_no,'refund_fee'=>$refund_fee);
        //     $config = RefundUtils::OrderRefund($array);
        //     echo "success";
        // }
        // else
        // {
            $log->log_result($log_name, "【支付宝支付回调】：" . json_encode($data));

            $trade_no = $data['out_trade_no'];

            $type = substr($trade_no, 0, 2);

            $config = PluginUtils::invokeMethod($type, 'toCheck', array($trade_no, $log, $log_name));

            $log->log_result($log_name, "【回调参数校验】：" . json_encode($config));
            $aop = new AopClient;
            $aop->alipayrsaPublicKey = $config['alipayrsaPublicKey'];
            $aop->appId = $config['appid'];
            $aop->private_key = $config['rsaPrivateKey'];
            $aop->alipay_public_key = $config['alipayrsaPublicKey'];
            $aop->signtype = $config['signType'];

            //此处验签方式必须与下单时的签名方式一致
            $result = $aop->rsaCheckV1($data, $aop->alipayrsaPublicKey, "RSA2");

            if ($result) 
            {
                //验证成功
                $log->log_result($log_name, "【支付宝接收到的notify通知】success：" . json_encode($result));
                //获取支付宝的通知返回参数，可参考技术文档中服务器异步通知参数列表
                //商户订单号
                $out_trade_no = $data['out_trade_no'];
                $total = $data["total_amount"];
                //支付宝交易号
                $trade_no = $data['trade_no'];
                //且交易状态为支付成功
                if ($data['trade_status'] == 'TRADE_SUCCESS') 
                {
                    $log->log_result($log_name, "==>" . $out_trade_no);
                    $type = substr($out_trade_no, 0, 2);
    
                    $zfbdata = array('log'=>$log,'log_name'=>$log_name,'total'=>$total,'trade_no'=>$out_trade_no,'source'=>'alipay');
    
                    PluginUtils::invokeMethod($type, 'paycb', $zfbdata);
                    echo "success";
                } 
                else 
                {
                    //支付宝服务器返回的其他状态
                    $log->log_result($log_name, "支付宝非正常返回状态结果：" . $data['trade_status']);
                    //验证失败
                    echo "fail";
                }
            } 
            else 
            {
                $log->log_result($log_name, "【支付宝】：验签失败！");
                $log->log_result($log_name, "【接收到的notify通知】error：" . json_encode($result));
                //验证失败
                echo "fail";
            }
        // }
    }
}
