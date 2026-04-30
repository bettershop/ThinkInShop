<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;

use app\admin\model\SystemConfigurationModel;
use app\admin\model\CustomerModel;
/**
 * 功能：PC店鋪获取字典信息
 * 修改人：DHB
 */
class Home 
{
    // 获取数据字典里的数据
    public function GetDictionaryInfo()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$store_type = addslashes(Request::param('storeType'));
    	$access_id = addslashes(Request::param('accessId'));

    	$key = addslashes(Request::param('key')); // 分类ID
    	$pageSize = addslashes(Request::param('pageSize')); // 分类ID

        $sql_unit = "select a.*,b.name,b.status as dicStatus from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.recycle = 0 and b.name = '$key' and a.status = 1";
        $r_unit = Db::query($sql_unit);
        if ($r_unit)
        {
            foreach ($r_unit as $k => $v)
            {
                $unit[] = $v;
            }
        }

        $data = array('list'=>$unit,'total'=>count($unit));
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 获取地址
    public function GetRegion()
    {
        $store_id = addslashes(Request::param('storeId'));
        $store_type = addslashes(Request::param('storeType'));
        $access_id = addslashes(Request::param('accessId'));

        $level = addslashes(Request::param('level')); // 级别
        $sid = addslashes(Request::param('sid')); // 上级ID

        $list = array();
        $list1 = array();
        $str = "id,district_name as districtName,district_pid as districtPid,district_show_order as districtShowOrder,district_level as districtLevel,district_childcount as district_ChildCount,district_delete as districtDelete,district_num as districtNum,district_country_num as districtCountryNum";
        if(isset($sid) && $sid != 0 && $sid != '')
        {
            $r = Db::table('lkt_map')->where(['district_level'=>$level,'district_pid'=>$sid])->field($str)->select()->toArray();
        }
        else
        {
            $r = Db::table('lkt_map')->where('district_level', $level)->field($str)->select()->toArray();
        }
        if($r)
        {
            foreach ($r as $k => $v)
            {
                foreach ($v as $k1 => $v1)
                {
                    $k1 = lcfirst($k1);
                    $list[$k][$k1] = $v1;
                }
            }
        }

        $message = Lang('Success');
        return output(200,$message,$list);
    }

    //获取系统配置
    public function Info()
    {
        $config = Tools::Get_system_settings();

        $message = Lang('Success');
        return output(200,$message,array('config'=>$config));
    }

    //获取商城下拉
    public function storeList()
    {
        $list = array();
        $res = CustomerModel::where(['status'=>0,'recycle'=>0])->field('id,name')->order('is_default','desc')->select()->toArray();
        if($res)
        {
            $list = $res;
        }
        $message = Lang('Success');
        return output(200,$message,array('list'=>$list));
    }

    // 获取前端基础信息配置（商家后台）
    public function GetBasicConfiguration()
    {
        $store_id = addslashes(safe_trim(Request::param('store_id')));
        if($store_id == '')
        {
            $store_id = addslashes(safe_trim(Request::param('storeId')));
        }

        if($store_id == '')
        {
            $message = Lang("Parameter error");
            return output(ERROR_CODE_CSCW,$message);
        }

        // 与 app.index.GetBasicConfiguration 对齐，复用同一缓存与返回结构
        $cache_key = 'app_basic_configuration:' . intval($store_id);
        $cache_data = cache($cache_key);
        if(is_array($cache_data))
        {
            $message = Lang('Success');
            return output(200,$message,$cache_data);
        }

        $list = array();
        $r = Db::name('system_configuration')
            ->where('store_id', $store_id)
            ->field('H5_domain as h5_domain,message_day as messageSaveDay,exp_time as appLoginValid,watermark_name,watermark_url,logo as logon_logo,copyright_information,record_information,link_to_landing_page,company as store_name,app_logo,logo1 as html_icon')
            ->find();
        if($r)
        {
            $list = $r;
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
        return output(200,$message,$data);
    }
}
