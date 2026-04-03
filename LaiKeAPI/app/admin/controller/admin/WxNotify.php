<?php
namespace app\admin\controller\admin;
date_default_timezone_set('Asia/Chongqing');
set_time_limit(0);
use app\common;

require_once(MO_LIB_DIR.'/WxPayPubHelper/WxPayPubHelper.php');
require_once(MO_LIB_DIR.'/WxPayPubHelper/log_.php');
use app\common\Plugin\PluginUtils;
use app\common\Plugin\LaiKeLogUtils;
/**
 * 功能：测试类类
 * 修改人：PJY
 */
class WxNotify
{

    public function apply()
    {	
		//存储微信的回调
		$xml = PHP_VERSION <= 5.6 ? $GLOBALS['HTTP_RAW_POST_DATA'] : file_get_contents('php://input');
		$notify = new \Notify_pub();
		if (empty($xml))
		{
		    echo "error";
		    exit;
		}
		$log = new \Log_();
		$log_name = './notify_url.log';//log文件路径
		$log->log_result($log_name, __LINE__ . "【微信接收到的notify通知】:\n" . $xml . "\r\n");
		$dxml = $notify->xmlToArray($xml);
		$log->log_result($log_name, __LINE__ . "=====" . json_encode($dxml) . "\r\n");

		$trade_no = $dxml['out_trade_no'];
		$openid = $dxml['openid'];
		if (empty($trade_no))
		{
		    $log->log_result($log_name, __LINE__ . "回调失败信息: \n 支付订单号：$trade_no 为空 \r\n");
		    echo 'error';
		    exit;
		}

		$mch_id = $dxml['mch_id'];
		$appid = $dxml['appid'];
		$type = substr($trade_no, 0, 2);
		$log->log_result($log_name, __LINE__ . "【微信接收到的notify通知Type $type  】:\n" . $xml . "\r\n");
		$config = PluginUtils::invokeMethod($type,'toCheck',array($trade_no, $log, $log_name));
		$notify = new \Notify_pub($config);
		$notify->saveData($xml);

		//验证签名，并回应微信。
		if ($notify->checkSign() == FALSE)
		{
		    $notify->setReturnParameter("return_code", "FAIL");//返回状态码
		    $notify->setReturnParameter("return_msg", "签名失败");//返回信息
		    echo 'error';
		    exit;
		}
		else
		{
		    $notify->setReturnParameter("return_code", "SUCCESS");//设置返回码
		}
		$returnXml = $notify->returnXml();
		$log->log_result($log_name,__LINE__ . "====" . json_encode($returnXml));

		echo $returnXml;
		$log_name = './' . $appid . '_notify_url.log';//log文件路径
		$log->log_result($log_name,__LINE__ . "【微信接收到的notify通知】:\n" . $xml . "\n");

		if ($notify->checkSign() == TRUE)
		{
		    if ($notify->data["return_code"] == "FAIL")
		    {
		        $log->log_result($log_name, "【微信通信出错】:\n" . $xml . "\n");    
		        echo 'error';
		        exit;
		    }
		    elseif ($notify->data["result_code"] == "FAIL")
		    {
		        $log->log_result($log_name, "【微信业务出错】:\n" . $xml . "\n");
		        echo 'error';
		        exit;
		    }
		    else
		    {   
		        $trade_no = $notify->data["out_trade_no"];
		        $transaction_id = $notify->data["transaction_id"];
		        $total_fee = $notify->data["total_fee"];
		        $type = substr($trade_no, 0, 2);
		        $data['log'] = $log;
		        $data['log_name'] = $log_name;
		        $data['total'] = $total_fee;
		        $data['trade_no'] = $trade_no;
		        $data['transaction_id'] = $transaction_id;
		        $data['openid'] = $openid;
		        $log->log_result($log_name,__LINE__ . "订单类型：" . $type);
		        $log->log_result($log_name,__LINE__ . "传参：" . json_encode($data));

		        PluginUtils::invokeMethod($type,'paycb',$data);
		    }
		}
		else
		{
		    LaiKeLogUtils::log('common/afck.log','3==>'.$notify->checkSign());
		    $log->log_result($log_name, "【签名验证结果 fail】:\n" . $notify->checkSign() . "\n");
		    echo 'error';
		    exit;
		}
		
    }

}
