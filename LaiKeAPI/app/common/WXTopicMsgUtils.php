<?php
namespace app\common;
use app\common;
use think\facade\Db;
use app\common\third\authorize\Third;
use app\common\LaiKeLogUtils;

use app\admin\model\NoticeModel;
use app\admin\model\ConfigModel;
/**
 * LKT 微信订阅消息帮助类
 */
class WXTopicMsgUtils{

    /**
     * @param $store_id
     */
    public static function getTmpIds($store_id){
        $res = NoticeModel::where('store_id',$store_id)->select()->toArray();
        if(count($res) > 0){
            return $res[0];
        }else{
            LaiKeLogUtils::lktLog(__METHOD__.'-->'.__LINE__."未配置微信订阅消息模板");
            return null;
        }
    }

    /**
     * 下单成功
     */
    public static function orderSuccess($orderData){
        try{
            $params = new \stdClass();
            $params->touser	 = $orderData['touser'];
            $tempsInfo = self::getTmpIds($orderData['store_id']);
            if($tempsInfo == null){
                $sid = $orderData['store_id'];
                LaiKeLogUtils::lktLog(__METHOD__.'-->'.__LINE__."商城【 $sid 】未配置微信订阅消息模板");
                return ;
            }
            $params->template_id = $tempsInfo['pay_success'];
            $params->page = $orderData['page'];
            $data = new \stdClass();
            $params->data = $data;

            //订单编号
            $obj = new \stdClass();
            $obj->value = $orderData['character_string1'];
            $data->character_string1 = $obj;

            //商品名称
            $obj = new \stdClass();


            if( mb_strlen($orderData['thing3']) < 20 )
            {
                $obj->value = $orderData['thing3'];
            }else if(mb_strlen($orderData['thing3']) >= 20){
                $obj->value = substr($orderData['thing3'],0,15).'...';
            }else{
                $obj->value = '商品名称';
            }

            $data->thing3 =  $obj;

            //支付金额
            $obj = new \stdClass();
            $obj->value = $orderData['amount4'];
            $data->amount4 = $obj;

            //支付时间
            $obj = new \stdClass();
            $obj->value = $orderData['date2'];
            $data->date2 = $obj;

            self::sendTopicMsg($orderData['store_id'],$params);

        }catch (Exception $e){
            throw $e;
        }
    }

    /**
     * 发货
     */
    public static function deliveryOrder($deliveryOrderData){
        try{
            $params = new \stdClass();
            $params->touser	 = $deliveryOrderData['touser'];
            $tempsInfo = self::getTmpIds($deliveryOrderData['store_id']);
            if($tempsInfo == null){
                $sid = $deliveryOrderData['store_id'];
                LaiKeLogUtils::lktLog(__METHOD__.'-->'.__LINE__."商城【 $sid 】未配置微信订阅消息模板");
                return ;
            }
            $params->template_id = $tempsInfo['order_delivery'];
            $params->page = $deliveryOrderData['page'];
            $data = new \stdClass();
            $params->data = $data;

            //温馨提示
            $obj = new \stdClass();
            $obj->value = $deliveryOrderData['thing6'];
            $data->thing6 = $obj;

            //订单号
            $obj = new \stdClass();
            $obj->value = $deliveryOrderData['character_string1'];
            $data->character_string1 =  $obj;

            //商品名称
            $obj = new \stdClass();
            if( mb_strlen($deliveryOrderData['thing2']) < 20 )
            {
                $obj->value = $deliveryOrderData['thing2'];
            }else if(mb_strlen($deliveryOrderData['thing2']) >= 20){
                $obj->value = substr($deliveryOrderData['thing2'],0,15).'...';
            }else{
                $obj->value = '商品名称';
            }
            $data->thing2 = $obj;

            //快递类型
            $obj = new \stdClass();
            $obj->value = $deliveryOrderData['phrase3'];
            $data->phrase3 = $obj;

            //快递单号
            $obj = new \stdClass();
            $obj->value = $deliveryOrderData['character_string4'];
            $data->character_string4 = $obj;
            self::sendTopicMsg($deliveryOrderData['store_id'],$params);
        }catch (Exception $e){
            throw $e;
        }
    }

    /**
     * 退款
     */
    public static function refundOrder($refundData){
        try{
            $params = new \stdClass();
            $params->touser	 = $refundData['touser'];
            $tempsInfo = self::getTmpIds($refundData['store_id']);
            if($tempsInfo == null){
                $sid = $refundData['store_id'];
                LaiKeLogUtils::lktLog(__METHOD__.'-->'.__LINE__."商城【 $sid 】未配置微信订阅消息模板");
                return ;
            }
            $params->template_id = $tempsInfo['refund_res'];
            $params->page = $refundData['page'];;
            $data = new \stdClass();
            $params->data = $data;

            //退款金额
            $obj = new \stdClass();
            $obj->value = $refundData['amount2'];
            $data->amount2 = $obj;

            //申请时间
            $obj = new \stdClass();
            $obj->value = $refundData['date3'];
            $data->date3 =  $obj;

            //订单号
            $obj = new \stdClass();
            $obj->value =$refundData['character_string4'];
            $data->character_string4 = $obj;

            //商品名称
            $obj = new \stdClass();

            if( mb_strlen($refundData['thing5']) < 20 )
            {
                $obj->value = $refundData['thing5'];
            }else if(mb_strlen($refundData['thing5']) >= 20){
                $obj->value = substr($refundData['thing5'],0,15).'...';
            }else{
                $obj->value = '商品名称';
            }

            $data->thing5 = $obj;

            //退款状态
            $obj = new \stdClass();
            $obj->value = $refundData['thing6'];
            $data->thing6 = $obj;

            self::sendTopicMsg($refundData['store_id'],$params);

        }catch (Exception $e){
            throw $e;
        }
    }


    /**
     * 发送订阅消息
     * @param $store_id     商城ID
     * @param $data         发送内容(包括发给的用户openid和模板id)
     */
    public static function sendTopicMsg($store_id,$data){
        LaiKeLogUtils::lktLog("==发送订阅消息开始==");
        try{
            $r0 = ConfigModel::where('store_id',$store_id)->field('appid,appsecret')->select()->toArray();
            $appid = "";
            $appsecret = "";
            if ($r0) {
                $appid = $r0[0]['appid']; // 小程序唯一标识
                $appsecret = $r0[0]['appsecret']; // 小程序的 app secret
            } else {
                $message = Lang('login.0');
                return output(ERROR_CODE_QXSZJCPZ,$message);
            }

            LaiKeLogUtils::lktLog("=>小程序appid".$appid);
            LaiKeLogUtils::lktLog("=>小程序appsecret".$appsecret);


            $url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=$appid&secret=$appsecret";
            $accessTokenResult = Third::https_get($url);
            $access_tokenInfo  = json_decode($accessTokenResult);
            $access_token = null;
            if (is_object($access_tokenInfo) && isset($access_tokenInfo->access_token)) {
                $access_token = $access_tokenInfo->access_token;
            }
            LaiKeLogUtils::lktLog("=>小程序access_token".$access_token);
            if (empty($access_token)) {
                LaiKeLogUtils::lktLog(__METHOD__.'->'.__LINE__." 获取令牌失败，响应: ".$accessTokenResult);
                echo json_encode(array('code'=>228,'message'=>'获取令牌失败!'));
                exit;
            }

            $url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=$access_token";
            $data->access_token = $access_token;
            LaiKeLogUtils::lktLog(__METHOD__.'->'.__LINE__."==发送订阅消息开始，参数: ".json_encode($data));
            $result = Third::https_post($url,json_encode($data),$type = 1,$header=null);
            LaiKeLogUtils::lktLog(__METHOD__.'->'.__LINE__."==发送订阅消息结束，结果: ".$result);
            $resultObj = json_decode($result);
            if (!is_object($resultObj)) {
                LaiKeLogUtils::lktLog("==发送订阅消息结束，失败==返回结果为空或非JSON");
                LaiKeLogUtils::lktLog("原始返回: ".$result);
                return;
            }

            if (isset($resultObj->errmsg) && $resultObj->errmsg == 'ok') {
                LaiKeLogUtils::lktLog("==发送订阅消息结束，成功==");
                LaiKeLogUtils::lktLog(json_encode($resultObj));
            } else {
                LaiKeLogUtils::lktLog("==发送订阅消息结束，失败==");
                LaiKeLogUtils::lktLog(json_encode($resultObj));
            }

        }catch (Exception $e){
            LaiKeLogUtils::lktLog("==发送订阅消息结束，失败==".$e->getMessage());
        }
    }
}

?>
