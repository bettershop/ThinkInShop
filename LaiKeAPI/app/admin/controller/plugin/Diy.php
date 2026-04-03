<?php
namespace app\admin\controller\plugin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use think\facade\Cookie;

use app\admin\model\DiyModel;
use app\admin\model\ConfigModel;

class Diy 
{
    // diy
    public function index()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $language = trim(Request::param('language')); // 语言

        $value = '';
        $tab_bar = '';
        $tabber_info = '';
        $r0 = DiyModel::where(['store_id'=>$store_id,'is_del'=>0,'status'=>1])->field('value,tab_bar,tabber_info')->select()->toArray();
        if($r0)
        {
            $value = $r0[0]['value'];
            $tab_bar = $r0[0]['tab_bar'];
            $tabber_info = $r0[0]['tabber_info'];
        }

        $appTitle = '';
        $r_config = ConfigModel::where('store_id', $store_id)->field('logo1,app_title')->select()->toArray();
        if ($r_config)
        {
            $appTitle = $r_config[0]['app_title'];
        }
        $data = array('data'=>$value,'tab_bar'=>$tab_bar,'tabber_info'=>$tabber_info,'appTitle' => $appTitle );
        $message = Lang("Success");
        return output(200,$message,$data);
    }
}

