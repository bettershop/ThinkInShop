<?php

namespace app\common\GETUI;
require_once('../app/common/GETUI/igetui/template/notify/IGt.Notify.php');
require_once('../app/common/GETUI/IGt.Push.php');
require_once('../app/common/GETUI/igetui/IGt.AppMessage.php');
require_once('../app/common/GETUI/igetui/IGt.TagMessage.php');
require_once('../app/common/GETUI/igetui/IGt.APNPayload.php');
require_once('../app/common/GETUI/igetui/template/IGt.BaseTemplate.php');
require_once('../app/common/GETUI/IGt.Batch.php');
require_once('../app/common/GETUI/igetui/utils/AppConditions.php');
require_once('../app/common/GETUI/igetui/template/notify/IGt.Notify.php');
require_once('../app/common/GETUI/igetui/IGt.MultiMedia.php');
require_once('../app/common/GETUI/payload/VOIPPayload.php');
// require_once (dirname(dirname(dirname(__FILE__))).'/config/push_config.php');
use think\facade\Db;

use app\admin\model\ConfigModel;
use app\admin\model\SystemMessageModel;
use app\admin\model\UserModel;
/**
 * Class LaikePushAction 推送类
 */
class LaikePushTools  {

    /**
     * 日志记录内容
     */
    const loggerFilepath = '../runtime/log/push/push.log';

    /**
     * 是否需要推送
     */
    public function pushOrNot(){
        return UNIPUSHON;// 默认不开启false
    }


    /**
     * 小程序消息推送
     */
    public function getAccessToken($appID, $appSerect) {
        $url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" . $appID . "&secret=" . $appSerect;
        // 时效性7200秒实现
        // 1.当前时间戳
        $currentTime = time();
        // 2.修改文件时间
        $fileName = "accessToken";
        // 文件名
        if (is_file($fileName)) {
            $modifyTime = filemtime($fileName);
            if (($currentTime - $modifyTime) < 7200) {
                // 可用, 直接读取文件的内容
                $accessToken = file_get_contents($fileName);
                return $accessToken;
            }
        }
        // 重新发送请求
        $result = $this -> httpsRequest($url);
        $jsonArray = json_decode($result, true);
        // 写入文件
        $accessToken = $jsonArray['access_token'];
        file_put_contents($fileName, $accessToken);
        return $accessToken;
    }
    public function Send_Prompt($appid, $appsecret, $form_id, $openid, $page, $send_id, $o_data) {

        $AccessToken = $this -> getAccessToken($appid, $appsecret);
        $url = 'https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token=' . $AccessToken;
        $data = json_encode(array('access_token' => $AccessToken, 'touser' => $openid, 'template_id' => $send_id, 'form_id' => $form_id, 'page' => $page, 'data' => $o_data));
        $da = $this -> httpsRequest($url, $data);
        return $da;
    }
    public function httpsRequest($url, $data = null) {
        // 1.初始化会话
        $ch = curl_init();
        // 2.设置参数: url + header + 选项
        // 设置请求的url
        curl_setopt($ch, CURLOPT_URL, $url);
        // 保证返回成功的结果是服务器的结果
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        //这个是重点。
        if (!empty($data)) {
            // 发送post请求
            curl_setopt($ch, CURLOPT_POST, 1);
            // 设置发送post请求参数数据
            curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
        }
        // 3.执行会话; $result是微信服务器返回的JSON字符串
        $result = curl_exec($ch);
        // 4.关闭会话
        curl_close($ch);
        return $result;
    }
    /**
     * 小程序消息推送  end
     */


    /**
     * 单对象消息推送
     * @param $cid
     * @param $messageContent
     */
    public function singlePush($cid,$messageContent,$config_array){

        $APPKEY = $config_array['push_Appkey'];
        $APPID = $config_array['push_Appid'];
        $MASTERSECRET = $config_array['push_MasterECRET'];

        $this->log('===>'.$APPKEY);
        $this->log('===>'.$APPID);
        $this->log('===>'.$MASTERSECRET);
        $igt = new IGeTui(HOST,$APPKEY,$MASTERSECRET);
        $template = $this->smsTemplate($messageContent,$config_array);
        //个推信息体
        $message = new IGtSingleMessage();
        $message->set_isOffline(true);//是否离线
        $message->set_offlineExpireTime(3600*12*1000);//离线时间
        $message->set_data($template);//设置推送消息类型
        //接收方
        $target = new IGtTarget();
        $target->set_appId($APPID);
        $target->set_clientId($cid);
        try {
            $rep = $igt->pushMessageToSingle($message, $target);
        }catch(RequestException $e){
            $requstId =$e->getRequestId();
            $rep = $igt->pushMessageToSingle($message, $target,$requstId);
        }
    }

    /**
     * 单条信息推送
     * @param $user_id
     * @param $db
     * @param $msg_title
     * @param $msg_content
     */
    public function pushMessage($user_id, $msg_title, $msg_content,$store_id,$admin_name='')
    {
        $is_push = 0;
        $push_Appkey = '';
        $push_Appid = '';
        $push_MasterECRET = '';
        $config_array = array();
        $r_config = ConfigModel::where(['store_id'=>$store_id])->field('is_push,push_Appkey,push_Appid,push_MasterECRET')->select()->toArray();
        if($r_config)
        {
            $is_push = $r_config[0]['is_push'];
            $push_Appkey = $r_config[0]['push_Appkey'];
            $push_Appid = $r_config[0]['push_Appid'];
            $push_MasterECRET = $r_config[0]['push_MasterECRET'];
        }

        $config_array = array('push_Appkey'=>$push_Appkey,'push_Appid'=>$push_Appid,'push_MasterECRET'=>$push_MasterECRET);
        //给用户发送消息
        $sql = new SystemMessageModel();
        $sql->store_id = $store_id;
        $sql->senderid = $admin_name;
        $sql->recipientid = $user_id;
        $sql->title = $msg_title;
        $sql->content = $msg_content;
        $sql->time = date('Y-m-d H:i:s');
        $sql->save();
        if($is_push == 0)
        {
            $this->log("系统没有开启推送功能.");
            return;
        }

        // //是否已经开启unipush推送功能
        // $pushon = $this->pushOrNot();

        // $userInfors = UserModel::where('user_id',$user_id)->select()->toArray();
        // if ($userInfors) 
        // {
        //     $cid = $userInfors[0]['clientid'];
        //     if($cid) 
        //     {
        //         $messageContent = '{"title":"' . $msg_title . '","content":"' . $msg_content . '","payload":"' . $msg_content . '"}';
        //         $this->singlePush($cid, $messageContent,$config_array);
        //         $this->log($messageContent."pushMessage发送".$user_id."成功,cid:".$cid);
        //     } 
        //     else 
        //     {
        //         //若用户无cid则无法推送消息
        //         $this->log("用户id:".$user_id."没有推送ID，无法推送消息[".$msg_content."].请登录后获取clientid.".__LINE__);
        //     }
        // }
        // else 
        // {
        //     //查不到用户数据
        //     $this->log("经查系统无此用户user_id:".$user_id."；所以无法推送消息[".$msg_content."].");
        // }
        
    }

    /**
     * @return IGtTransmissionTemplate 模板
     */
    public function smsTemplate($messageContent,$config_array){
        $APPKEY = $config_array['push_Appkey'];
        $APPID = $config_array['push_Appid'];
        $MASTERSECRET = $config_array['push_MasterECRET'];

        $template =  new IGtTransmissionTemplate();
        $template->set_appId($APPID);//应用appid
        $template->set_appkey($APPKEY);//应用appkey
        $template->set_transmissionType(TRANSMISSIONTYPE);//透传消息类型
        $template->set_transmissionContent($messageContent);//透传内容
        $lktmessageContent = htmlspecialchars_decode($messageContent);
        $lktmessageContent = json_decode($lktmessageContent);
        $smsMessage = new SmsMessage();
        $smsContent = array();
        $smsMessage->setSmsContent($smsContent);
        $smsMessage->setSmsTemplateId("1a0ad952756f4c679ca67f008bf37b5e");
        $smsMessage->setOfflineSendtime(5000000);
        $template->setSmsInfo($smsMessage);
        //APN推送
        $apn = new IGtAPNPayload();
        $alertmsg=new DictionaryAlertMsg();
        $alertmsg->body=$lktmessageContent->content;
        $alertmsg->actionLocKey="ActionLockey";
        $alertmsg->locKey="LocKey";
        $alertmsg->locArgs=array("locargs");
        $alertmsg->launchImage="launchimage";
        //IOS8.2 支持
        $alertmsg->title=$lktmessageContent->title;
        $alertmsg->titleLocKey="TitleLocKey";
        $alertmsg->titleLocArgs=array("TitleLocArg");
        $apn->alertMsg=$alertmsg;
        $apn->badge=0;
        $apn->sound="";
        $apn->add_customMsg("payload",$messageContent);
        $apn->add_customMsg("fromServer","1");
        $apn->contentAvailable=1;
        $apn->category="ACTIONABLE";
        $template->set_apnInfo($apn);
        return $template;
    }

    /**
     * 获取用户状态 在线 离线
     */
    public function getUserStatus() {
        $igt = new IGeTui(HOST,APPKEY,MASTERSECRET);
        $rep = $igt->getClientIdStatus(APPID,CID);
    }

    /**
     * 写日志函数
     * @param $msg  日志内容
     */
    public  function log($msg){
        $fp = fopen(LaikePushTools::loggerFilepath,'a+');
        flock($fp,LOCK_EX);
        fwrite($fp,"日期：".date("Y-m-d H:i:s")."\r\n".$msg."\r\n");
        flock($fp,LOCK_UN);
        fclose($fp);
    }

}
?>