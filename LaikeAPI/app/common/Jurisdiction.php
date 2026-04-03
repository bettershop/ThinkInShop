<?php

namespace app\common;
use think\facade\Db;
use app\common\Tools;

use app\admin\model\AdminRecordModel;
use app\admin\model\AdminModel;
use app\admin\model\CoreMenuModel;

class Jurisdiction
{
    // 管理员记录
    function admin_record($store_id, $admin_name, $event, $type,$source = 1,$mch_id = 0,$operator_id = 0)
    {
        $time = date("Y-m-d H:i:s");
        if($admin_name != '')
        {
            $data = array('store_id'=>$store_id,'admin_name'=>$admin_name,'event'=>$event,'type'=>$type,'add_date'=>$time,'source'=>$source,'mch_id'=>$mch_id,'operator_id'=>$operator_id);
            Db::name('admin_record')->save($data);
        }
        return;
    }

    // 管理员记录
    function Administrator_Records($array)
    {
        $time = date("Y-m-d H:i:s");
        $store_id = $array['store_id'];
        $admin_name = $array['admin_name'];
        $event = $array['event'];
        $type = $array['type'];
        $mch_id = $array['mch_id'];
        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }
        $user_id = '';
        if(isset($array['user_id']))
        {
            $user_id = $array['user_id'];
        }

        if($source == 2)
        {
            $data = array('store_id'=>$store_id,'admin_name'=>$user_id,'event'=>$event,'type'=>$type,'add_date'=>$time,'source'=>$source,'mch_id'=>$mch_id);
        }
        else
        {
            $data = array('store_id'=>$store_id,'admin_name'=>$admin_name,'event'=>$event,'type'=>$type,'add_date'=>$time,'source'=>$source,'mch_id'=>0);
        }
        Db::name('admin_record')->save($data);
        return;
    }

    // 按钮权限
    function Jurisdiction($store_id, $admin_name, $admin_type1, $url)
    {
        if ($admin_type1 == 0)
        {
            if($store_id)
            {
                $r = AdminModel::where(['store_id'=>$store_id,'type'=>1,'recycle'=>0])->select()->toArray();
            }
            else
            {
                $r = AdminModel::where('name',$admin_name)->select()->toArray();
            }
        }
        else
        {
            $r = AdminModel::where(['name'=>$admin_name,'store_id'=>$store_id])->select()->toArray();
        }
        $admin_type = $r[0]['admin_type']; // 管理类型
        $role = explode(',', $r[0]['role']); // 角色

        if ($admin_type != 1)
        {
            $permission_1 = array();
            $permission = array();
            foreach ($role as $k => $v)
            {
                $sql1 = "select b.id,b.type,b.url from lkt_role_menu as a left join lkt_core_menu as b on a.menu_id = b.id where a.role_id = '$v' and b.recycle = 0 ";
                $r1 = Db::query($sql1);
                if ($r1)
                {
                    foreach ($r1 as $k1 => $v1)
                    {
                        if ($v1['type'] != '0')
                        {
                            $v1['title_name'] = Tools::header_data_dictionary('导航栏', $v1['type']);
                            $role_list1[] = $v1;
                        }
                        $permission_1[] = $v1['id'];
                    }
                }
            }
            foreach ($permission_1 as $ke => $va)
            {
                $rrr = CoreMenuModel::where('id',$va)->field('url')->select()->toArray();
                if ($rrr)
                {
                    if ($rrr[0]['url'] != '')
                    {
                        $permission[] = $rrr[0]['url'];
                    }
                }
            }
        }
        if ($r[0]['admin_type'] != 1 && !in_array($url, $permission))
        {
            return 0;
        }
        else
        {
            return 1;
        }

    }
}

?>