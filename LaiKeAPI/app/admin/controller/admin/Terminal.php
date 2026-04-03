<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\GETUI\LaikePushTools;

use app\admin\model\BannerModel;
use app\admin\model\EditionModel;
use app\admin\model\NoticeModel;
use app\admin\model\ConfigModel;
use app\admin\model\GuideModel;

/**
 * 功能：PC端轮播图
 * 修改人：PJY
 */
class Terminal extends BaseController
{   
    // 获取终端配置
    public function index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $type = addslashes(trim($this->request->param('type')));

        if($type == 2)
        {
            $appInfo = array();
            $r0 = EditionModel::where(['store_id'=>$store_id])->select()->toArray();
            if ($r0)
            {
                $appInfo = $r0[0];
            }

            $app_domain_name = '';
            $push_Appkey = '';
            $push_Appid = '';
            $push_MasterECRET = '';
            $android_download_link = '';
            $ios_download_link = '';
            $r1 = ConfigModel::where(['store_id'=>$store_id])->field('app_domain_name,push_Appkey,push_Appid,push_MasterECRET,android_download_link,ios_download_link')->select()->toArray();
            if ($r1)
            {
                $app_domain_name = $r1[0]['app_domain_name']; // APP域名
                $push_Appkey = $r1[0]['push_Appkey'];
                $push_Appid = $r1[0]['push_Appid'];
                $push_MasterECRET = $r1[0]['push_MasterECRET'];
                $android_download_link = $r1[0]['android_download_link']; // 安卓下载地址
                $ios_download_link = $r1[0]['ios_download_link']; // IOS下载地址
            }

            $guide_list = array();
            $r2 = GuideModel::where(['store_id'=>$store_id])->order('sort','asc')->field('id,image,type,sort,add_date')->select()->toArray();
            if ($r2)
            {
                foreach($r2 as $k => $v)
                {
                    $v['image'] = ServerPath::getimgpath($v['image'], $store_id);
                    $guide_list[] = $v;
                }
            }

            $data = array('appInfo'=>$appInfo,'edition'=>$app_domain_name,'push_Appkey'=>$push_Appkey,'push_Appid'=>$push_Appid,'push_MasterECRET'=>$push_MasterECRET,'android_download_link'=>$android_download_link,'ios_download_link'=>$ios_download_link,'guide_list'=>$guide_list,'guide_total'=>count($guide_list));
        }
        else
        {
            $appid = ''; // 小程序id
            $appsecret = ''; // 小程序密钥
            $official_account_appid = ''; // 公众号appID
            $official_account_appsecret = ''; // 公众号app密钥
            $Hide_your_wallet = false; // 是否隐藏钱包 0.不隐藏 1.隐藏
            $app_title = ''; // 首页标题
            $app_logo = ''; // 授权登录logo
            
            $r0 = ConfigModel::where(['store_id'=>$store_id])->field('id,appid,appsecret,official_account_appid,official_account_appsecret,Hide_your_wallet,app_title,app_logo')->select()->toArray();
            if ($r0)
            {
                $appid = $r0[0]['appid']; // 小程序id
                $appsecret = $r0[0]['appsecret']; // 小程序密钥
                $official_account_appid = $r0[0]['official_account_appid']; // 公众号appID
                $official_account_appsecret = $r0[0]['official_account_appsecret']; // 公众号app密钥
                $Hide_your_wallet = $r0[0]['Hide_your_wallet']; // 是否隐藏钱包 0.不隐藏 1.隐藏
                $app_title = $r0[0]['app_title']; // 首页标题
                $app_logo = $r0[0]['app_logo']; // 授权登录logo
            }
            if ($appsecret != '')
            {
                $appsecret = $this->maskValue($appsecret, 4, 4);
            }
            if ($official_account_appsecret != '')
            {
                $official_account_appsecret = $this->maskValue($official_account_appsecret, 4, 4);
            }

            $weiXinInfo = array('id'=>'','store_id'=>'','pay_success'=>'','order_delivery'=>'','delivery'=>'','refund_res'=>'','update_time'=>'');
            $r1 = NoticeModel::where(['store_id'=>$store_id])->select()->toArray();
            if ($r1)
            {
                $weiXinInfo = $r1[0];
            }

            $guide_list = array();
            $r2 = GuideModel::where(['store_id'=>$store_id])->order('sort','asc')->field('id,image,type,sort,add_date')->select()->toArray();
            if ($r2)
            {
                foreach($r2 as $k => $v)
                {
                    $v['image'] = ServerPath::getimgpath($v['image'], $store_id);
                    $guide_list[] = $v;
                }
            }

            $data = array('Hide_your_wallet'=>$Hide_your_wallet,'appId'=>$appid,'appSecret'=>$appsecret,'official_account_appid'=>$official_account_appid,'official_account_appsecret'=>$official_account_appsecret,'edition'=>'','appTitle'=>$app_title,'appLogo'=>$app_logo,'guide_list'=>$guide_list,'guide_total'=>count($guide_list),'weiXinInfo'=>$weiXinInfo);
        }

        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // APP提交（新）
    public function saveApp()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

    	$push_Appkey = addslashes($this->request->param('pushAppkey')); // 推送Appkey
    	$push_Appid = addslashes($this->request->param('pushAppid')); // 推送Appid
    	$push_MasterECRET = addslashes($this->request->param('pushMasterEcret')); // 推送秘钥

    	$android_download_link = addslashes($this->request->param('android_download_link')); // 安卓下载地址
    	$ios_download_link = addslashes($this->request->param('ios_download_link')); // IOS下载地址

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        // 更新
        $sql1_where = array('store_id'=>$store_id);
        $sql1_update = array('push_Appkey'=>$push_Appkey,'push_Appid'=>$push_Appid,'push_MasterECRET'=>$push_MasterECRET,'modify_date'=>$time,'android_download_link'=>$android_download_link,'ios_download_link'=>$ios_download_link);
        $r1 = Db::name('config')->where($sql1_where)->update($sql1_update);
        if ($r1 == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了APP配置信息失败',2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改APP配置信息失败！参数:'.json_encode($sql1_where);
            $this->Log($Log_content);
            $message = Lang('terminal.4');
            return output(109,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了APP配置信息成功！',2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改了APP配置信息成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 小程序提交
    public function saveWeiXinApp()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $app_title = addslashes($this->request->param('appTitle')); // 首页标题
        $app_logo = addslashes($this->request->param('appLogo')); // 授权登录logo

        $appid = addslashes($this->request->param('appId')); // 小程序id
        $appsecret = addslashes($this->request->param('appSecret')); // 小程序密钥
        $official_account_appid = addslashes($this->request->param('official_account_appid')); // 公众号appID
        $official_account_appsecret = addslashes($this->request->param('official_account_appsecret')); // 公众号app密钥

        $pay_success = addslashes($this->request->param('paySuccess')); // 支付成功通知
        $delivery = addslashes($this->request->param('delivery')); // 支付成功通知 订单发货通知
        $refund_res = addslashes($this->request->param('refund_res')); // 退款通知

        $Hide_your_wallet = addslashes($this->request->param('hideWallet')); // 是否隐藏钱包 0.不隐藏 1.隐藏

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        if ($app_title == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 小程序首页标题不能为空!';
            $this->Log($Log_content);
            $message = Lang('terminal.9');
            return output(109,$message);
        }
        if ($app_logo == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 小程序授权登录logo不能为空!';
            $this->Log($Log_content);
            $message = Lang('terminal.10');
            return output(109,$message);
        }

        if ($appid == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 小程序id不能为空!';
            $this->Log($Log_content);
            $message = Lang('terminal.7');
            return output(109,$message);
        }
        if ($appsecret == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 小程序密钥不能为空!';
            $this->Log($Log_content);
            $message = Lang('terminal.8');
            return output(109,$message);
        }

        $masked_appsecret = $this->isMaskedValue($appsecret);
        $masked_official_secret = $this->isMaskedValue($official_account_appsecret);
        if ($masked_appsecret || $masked_official_secret)
        {
            $existing_secrets = ConfigModel::where(['store_id'=>$store_id])->field('appsecret,official_account_appsecret')->select()->toArray();
            if ($existing_secrets)
            {
                if ($masked_appsecret)
                {
                    $appsecret = $existing_secrets[0]['appsecret'];
                }
                if ($masked_official_secret)
                {
                    $official_account_appsecret = $existing_secrets[0]['official_account_appsecret'];
                }
            }
        }

        if($Hide_your_wallet == 'true')
        {
            $Hide_your_wallet = 1;
        }
        if($Hide_your_wallet == 'false')
        {
            $Hide_your_wallet = 0;
        }

        $sql_config = "select id from lkt_config where store_id = '$store_id' ";
        $r_config = Db::query($sql_config);
        if($r_config)
        {
            // 更新
            $sql1_where = array('store_id'=>$store_id);
            $sql1_update = array('appid'=>$appid,'appsecret'=>$appsecret,'official_account_appid'=>$official_account_appid,'official_account_appsecret'=>$official_account_appsecret,'Hide_your_wallet'=>$Hide_your_wallet,'modify_date'=>$time,'app_title'=>$app_title,'app_logo'=>$app_logo);
            $r1 = Db::name('config')->where($sql1_where)->update($sql1_update);
            if ($r1 == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了微信小程的序配置信息失败',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改小程序终端设置失败！参数:'.json_encode($sql1_where);
                $this->Log($Log_content);
                $message = Lang('terminal.4');
                return output(109,$message);
            }
        }
        else
        {
            // 添加系统设置
            $sql1 = array('store_id'=>$store_id,'upserver'=>2,'modify_date'=>$time,'uploadImg'=>'./images/','user_id'=>'user','wx_name'=>'user','appid'=>$appid,'appsecret'=>$appsecret,'official_account_appid'=>$official_account_appid,'official_account_appsecret'=>$official_account_appsecret,'Hide_your_wallet'=>$Hide_your_wallet,'modify_date'=>$time,'app_title'=>$app_title,'app_logo'=>$app_logo);
            $r1 = Db::name('config')->insert($sql1);
            if($r1 < 1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加系统设置失败';
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('shop.15');
                return output(109, $message);
            }
        }

        $r2_1 = NoticeModel::where(['store_id'=>$store_id])->field('id')->select()->toArray();
        if($r2_1)
        {
            $sql2_where = array('store_id'=>$store_id);
            $sql2_update = array('pay_success'=>$pay_success,'delivery'=>$delivery,'refund_res'=>$refund_res);
            $r2 = Db::name('notice')->where($sql2_where)->update($sql2_update);
            if($r2 == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了微信小程的序配置信息失败',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改小程序终端设置失败！参数:'.json_encode($sql2_where);
                $this->Log($Log_content);
                $message = Lang('terminal.4');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了微信小程的序配置信息',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改小程序终端设置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $sql2 = array('store_id'=>$store_id,'pay_success'=>$pay_success,'delivery'=>$delivery,'refund_res'=>$refund_res,'update_time'=>$time);
            $r2 = Db::name('notice')->insert($sql2);
            if($r2 > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了微信小程的序配置信息失败',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 添加小程序终端设置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了微信小程的序配置信息',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 添加小程序终端设置失败！参数:'.json_encode($sql2);
                $this->Log($Log_content);
                $message = Lang('terminal.6');
                return output(109,$message);
            }
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/terminal.log",$Log_content);
        return;
    }
}
