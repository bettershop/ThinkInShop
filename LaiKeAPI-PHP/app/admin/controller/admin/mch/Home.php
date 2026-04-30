<?php
namespace app\admin\controller\admin\mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;

/**
 * 功能：店铺设置
 * 修改人：DHB
 */
class Home extends BaseController
{
    // 店铺信息
    public function select_language()
    {
        $store_id = addslashes(safe_trim($this->request->param('storeId')));
        $store_type = addslashes(safe_trim($this->request->param('storeType')));
        $access_id = addslashes(safe_trim($this->request->param('accessId')));

        $language = addslashes(safe_trim($this->request->param('language')));
        $language = Tools::get_lang($language);
        $res = cache($access_id);
        $user_id = $res['user_id'];
        
        $sql0 = "update lkt_user set lang = '$language' where user_id = '$user_id' ";
        $r0 = Db::execute($sql0);
        if($r0 == -1)
        {
            $message = Lang('Modification failed');
            return output(109, $message);
        }
        else
        {
            $message = Lang('Success');
            return output('200', $message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/mch/set.Home",$Log_content);
        return;
    }

    // 获取前端基础信息配置
    public function GetBasicConfiguration()
    {
        $store_id = addslashes(safe_trim($this->request->param('store_id')));
        if($store_id == '')
        {
            $store_id = addslashes(safe_trim($this->request->param('storeId')));
        }

        if($store_id == '')
        {
            $message = Lang("Parameter error");
            return output(ERROR_CODE_CSCW, $message);
        }

        // 对齐 Java admin.mch.home.GetBasicConfiguration -> getFrontConfig(vo,1)
        $cache_key = 'app_basic_configuration:' . intval($store_id);
        $cache_data = cache($cache_key);
        if(is_array($cache_data))
        {
            $message = Lang('Success');
            return output(200, $message, $cache_data);
        }

        $list = array(
            'h5_domain' => '',
            'messageSaveDay' => 0,
            'appLoginValid' => 0,
            'watermark_name' => '',
            'watermark_url' => '',
            'logon_logo' => '',
            'copyright_information' => '',
            'record_information' => '',
            'link_to_landing_page' => '',
            'store_name' => '',
            'app_logo' => '',
            'html_icon' => ''
        );

        // 基础配置在 lkt_config 表，避免访问 lkt_system_configuration 中不存在的字段
        $r = Db::name('config')
            ->where('store_id', $store_id)
            ->field('H5_domain as h5_domain,message_day as messageSaveDay,exp_time as appLoginValid,watermark_name,watermark_url,logo as logon_logo,copyright_information,record_information,link_to_landing_page,company as store_name,app_logo,logo1 as html_icon')
            ->find();
        if($r)
        {
            $list = array_merge($list, $r);
        }

        $rsc = Db::name('system_configuration')->field('admin_default_portrait')->where('store_id', 0)->find();
        if($rsc)
        {
            $list['admin_default_portrait'] = $rsc['admin_default_portrait'];
        }

        $store_logo = Db::name('customer')->where('id', $store_id)->value('merchant_logo');
        if($store_logo !== null)
        {
            $list['store_logo'] = $store_logo;
        }

        $data = array('list' => $list);
        cache($cache_key, $data, 300);

        $message = Lang('Success');
        return output(200, $message, $data);
    }
}

