<?php

namespace app\common\Plugin;
use think\facade\Db;
use app\admin\model\PluginsModel;


class Plugin
{
    // 获取商城所有插件
    public function get_Plugin($store_id)
    {
        $list = array();

        $sql = "select tt.* from (select status,flag,plugin_code,row_number () over (partition by plugin_code) as top from lkt_plugins where store_id = '$store_id' ORDER BY id ) as tt where tt.top < 2";
        $r = Db::query($sql);
        if($r)
        {
            foreach ($r as $k => $v)
            {
                $list[] = $v;
            }
        }
        return $list;
    }
    
    // 获取有权限的插件
    public function get_enabled_plugins($store_id)
    {
        $list = array();
        $sql = "select tt.* from (select status,plugin_code,row_number () over (partition by plugin_code) as top from lkt_plugins where store_id = '$store_id' and flag = 0 ORDER BY id ) as tt where tt.top < 2";
        $r = Db::query($sql);
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $plugin_code = $v['plugin_code'];
                $Identification = $this->Identification($plugin_code);
                
                $list[$Identification] = $v['status'];
            }
        }

        return $list;
    }
    
    // 获取插件
    public function is_Plugin($store_id)
    {
        $list = array();
        $r = PluginsModel::where('flag', 0)
                            ->where('status', 1)
                            ->group('plugin_code')
                            ->field('plugin_code as Identification')
                            ->select()
                            ->toArray();
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $Identification = $v['Identification'];
                $Identification = $this->Identification($Identification);
                
                if(file_exists(MO_LIB_DIR.'/Plugin/'.$Identification.'.php'))
                {
                    if($Identification == 'MchPublicMethod')
                    {
                        $Identification0 = new MchPublicMethod();
                    }
                    else if($Identification == 'sign')
                    {
                        $Identification0 = new sign();
                    }
                    else if($Identification == 'CouponPublicMethod')
                    {
                        $Identification0 = new CouponPublicMethod();
                    }
                    elseif ($Identification == 'IntegralPublicMethod') 
                    {
                        $Identification0 = new IntegralPublicMethod();
                    }
                    elseif ($Identification == 'Presell_order') 
                    {
                        $Identification0 = new Presell_order();
                    }
                    elseif($Identification == 'Distribution')
                    {
                        $Identification0 = new Distribution();
                    }
                    elseif($Identification == 'SecondsPublic')
                    {
                        $Identification0 = new SecondsPublic();
                    }
                    elseif($Identification == 'Auction')
                    {
                        $Identification0 = new Auction();
                    }
                    elseif($Identification == 'Members')
                    {
                        $Identification0 = new Members();
                    }

                    if (method_exists($Identification0, 'is_Plugin'))
                    {
                        $Identification_list = $Identification0->is_Plugin($store_id);
                        $list[$Identification] = $Identification_list;
                    }
                }
            }
        }

        return $list;
    }

    public function front_Plugin($store_id)
    {
        // $list = $this->is_Plugin($store_id); // 查询有哪些插件
        $list = $this->get_enabled_plugins($store_id); // 获取有权限的插件
        return $list;
    }

    public function Identification($Identification)
    {
        if($Identification == 'mch')
        {
            $Identification = 'MchPublicMethod';
        }
        else if($Identification == 'sign')
        {
            $Identification = 'sign';
        }
        else if($Identification == 'coupon')
        {
            $Identification = 'CouponPublicMethod';
        }
        else if($Identification == 'integral')
        {
            $Identification = 'IntegralPublicMethod';
        }
        else if($Identification == 'presell')
        {
            $Identification = 'Presell_order';
        }
        else if($Identification == 'distribution')
        {
            $Identification = 'Distribution';
        }
        else if($Identification == 'seconds')
        {
            $Identification = 'SecondsPublic';
        }
        else if($Identification == 'auction')
        {
            $Identification = 'Auction';
        }
        else if($Identification == 'member')
        {
            $Identification = 'Members';
        }
        else if($Identification == 'go_group')
        {
            $Identification = 'Go_groupPublicMethod';
        }
        else if($Identification == 'flashsale')
        {
            $Identification = 'FlashSalePublic';
        }
        else if($Identification == 'living')
        {
            $Identification = 'LivingPublic';
        }
        return $Identification;
    }

    public function pro_Plugin($store_id,$active = 1)
    {
        $Plugin_arr = array();
        $Plugin_arr[] = array('name' => '正价', 'value' => 1, 'status' => false);
        $list = $this->front_Plugin($store_id);
        foreach ($list as $k => $v)
        {
            if ($k == 'go_group' && $v == 1)
            {
                $Plugin_arr[] = array('name' => '拼团', 'value' => 2, 'status' => false);
            }
            else if ($k == 'bargain' && $v == 1)
            {
                $Plugin_arr[] = array('name' => '砍价', 'value' => 3, 'status' => false);
            }
            else if ($k == 'auction' && $v == 1)
            {
                $Plugin_arr[] = array('name' => '竞拍', 'value' => 4, 'status' => false);
            }

            else if ($k == 'integral' && $v == 1)
            {
                $Plugin_arr[] = array('name' => '积分', 'value' => 7, 'status' => false);
            }
            else if ($k == 'seconds' && $v == 1)
            {
                $Plugin_arr[] = array('name' => '秒杀', 'value' => 8, 'status' => false);
            }
        }

        foreach ($Plugin_arr as $k => $v)
        {
            if($active == $v['value'])
            {
                $Plugin_arr[$k]['status'] = true;
            }
        }
        return $Plugin_arr;
    }

    public function is_Plugin1($store_id, $type, $active = '', $distributor_id = '')
    {
        $res = array();
        $res1 = '';
        $res2 = '';
        $list = $this->is_Plugin($store_id); // 查询有哪些插件

        foreach ($list as $k => $v)
        {
            if ($v != 2)
            {
                if(file_exists('../app/common/Plugin/'.$k.'.php'))
                {
                    // 当存在插件数据
                    if($k == 'MchPublicMethod')
                    {
                        $Identification = new MchPublicMethod();
                    }
                    else if($k == 'sign')
                    {
                        $Identification = new sign();
                    }
                    else if($k == 'CouponPublicMethod')
                    {
                        $Identification = new CouponPublicMethod();
                    }
                    if (method_exists($Identification, $type))
                    {
                        $Identification_list = $Identification->$type($active);
                        $res1 .= $Identification_list;
                    }
                }
            }
        }
        $res['res1'] = $res1;
        $res['res2'] = $res2;
        return $res;
    }

    // 检验插件还是否存在
    public function add($store_id, $role, $menu_list)
    {
        $menu_list_0 = array();
        if($role != '')
        {
            $sql0_0 = "select a.module from lkt_core_menu as a left join lkt_role_menu as b on a.id = b.menu_id where role_id = '$role'";

            $r0_0 = Db::query($sql0_0);
            if ($r0_0)
            {
                foreach ($r0_0 as $k => $v)
                {
                    if ($v['module'] != '')
                    {
                        $menu_list_1[] = $v['module'];
                    }
                }
                $menu_list_0 = array_unique($menu_list_1);
            }
        }

        $r = PluginsModel::where(['flag'=>0,'status'=>1])->order('plugin_code','asc')->field('plugin_code as Identification')->select()->toArray();
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                if (!in_array($v['Identification'], $menu_list_0) && in_array($v['Identification'], $menu_list))
                {
                    $Identification = $v['Identification'];
                    $Identification = $this->Identification($Identification);

                    if(file_exists('../app/common/Plugin/'.$Identification.'.php'))
                    {
                        if($Identification == 'MchPublicMethod')
                        {
                            $Identification0 = new MchPublicMethod();
                        }
                        else if($Identification == 'sign')
                        {
                            $Identification0 = new sign();
                        }
                        else if($Identification == 'CouponPublicMethod')
                        {
                            $Identification0 = new CouponPublicMethod();
                        }
                        elseif ($Identification == 'IntegralPublicMethod') 
                        {
                            $Identification0 = new IntegralPublicMethod();
                        }
                        elseif ($Identification == 'Presell_order') 
                        {
                            $Identification0 = new Presell_order();
                        }
                        elseif($Identification == 'Distribution')
                        {
                            $Identification0 = new Distribution();
                        }
                        elseif($Identification == 'SecondsPublic')
                        {
                            $Identification0 = new SecondsPublic();
                        }
                        elseif($Identification == 'Auction')
                        {
                            $Identification0 = new Auction();
                        }
                        elseif($Identification == 'Members')
                        {
                            $Identification0 = new Members();
                        }
                        
                        if (method_exists($Identification, 'add'))
                        { // 添加插件设置
                            $Identification->add($store_id);
                        }
                    }
                }
            }
        }
        return;
    }

    // 检验插件还是否存在
    public function inspect($store_id, $menu_list)
    {
        $list = $this->is_Plugin($store_id); // 查询有哪些插件
        foreach ($list as $k => $v)
        {
            if ($v != 2)
            {
                // 当存在插件数据
                if (!in_array($k, $menu_list))
                { // 插件标识 不存在 菜单标识里 （代表取消该插件）
                    if(file_exists('../app/common/Plugin/'.$k.'.php'))
                    {
                        switch ($k) 
                        {
                          case 'auction':
                            $Identification = new auction();
                            break;
                          
                          default:
                            $Identification = new NormalOrder();
                            break;
                        }
                        if (method_exists($Identification, 'del'))
                        { // 删除插件设置
                            $Identification->del($store_id);
                        }
                    }
                }
            }
        }
        return;
    }
    
    // 修改插件
    public function Modifying_plugins($store_id, $menu_list)
    {
        $str = '';
        $list = $this->get_Plugin($store_id); // 查询有哪些插件

        foreach ($list as $k => $v)
        {
            if($v['plugin_code'] == 'mch')
            { // 店铺
                $str = "stores";
            }
            else if($v['plugin_code'] == 'member')
            { // 会员
                $str = "member";
            }
            else if($v['plugin_code'] == 'coupon')
            { // 优惠券
                $str = "coupons";
            }
            else if($v['plugin_code'] == 'seconds')
            { // 秒杀
                $str = "seckill";
            }
            else if($v['plugin_code'] == 'integral')
            { // 积分
                $str = "integralMall";
            }
            else if($v['plugin_code'] == 'presell')
            { // 预售
                $str = "preSale";
            }
            else if($v['plugin_code'] == 'distribution')
            { // 分销
                $str = "distribution";
            }
            else if($v['plugin_code'] == 'diy')
            { // diy
                $str = "template";
            }
            else if($v['plugin_code'] == 'auction')
            { // 竞拍
                $str = "auction";
            }
            else if($v['plugin_code'] == 'go_group')
            { // 拼团
                $str = "group";
            }

            if(in_array($str,$menu_list))
            { // 存在插件
                $update = " flag = 0 ";
            }
            else
            { // 不存在插件
                $update = " flag = 1 ";
            }
            $sql = "update lkt_plugins set $update where store_id = '$store_id' and plugin_code = '".$v['plugin_code']."' ";
            $r = Db::execute($sql);
        }
        return;
    }

    // 获取用户能否看见插件入口
    public function Get_plugin_entry($store_id)
    {
        $list = array('auction' => 0,'coupon' => 0,'distribution' => 0,'diy' => 0,'go_group' => 0,'integral' => 0,'mch' => 0,'member' => 0,'presell' => 0,'seconds' => 0,'sigin' => 0,'flashsale' => 0,'living' => 0);

        $sql = "select tt.* from (select status,plugin_code,row_number () over (partition by plugin_code) as top from lkt_plugins where store_id = '$store_id' and flag = 0 ORDER BY id ) as tt where tt.top < 2";
        $r = Db::query($sql);
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $plugin_code = $v['plugin_code'];
                $plugin_name = $this->Identification($plugin_code);
                if(file_exists('../app/common/Plugin/'.$plugin_name.'.php'))
                {
                    $Identification = '';
                    // 当存在插件数据
                    if($plugin_name == 'CouponPublicMethod')
                    { // 优惠券
                        $Identification = new CouponPublicMethod();
                    }
                    else if($plugin_name == 'Go_groupPublicMethod')
                    { // 拼团
                        $Identification = new Go_groupPublicMethod();
                    }
                    else if($plugin_name == 'IntegralPublicMethod')
                    { // 积分商城
                        $Identification = new IntegralPublicMethod();
                    }
                    else if($plugin_name == 'Presell_order')
                    { // 预售
                        $Identification = new Presell_order();
                    }
                    else if($plugin_name == 'Distribution')
                    { // 分销
                        $Identification = new Distribution();
                    }
                    else if($plugin_name == 'SecondsPublic')
                    { // 秒杀
                        $Identification = new SecondsPublic();
                    }
                    else if($plugin_name == 'Auction')
                    { // 竞拍
                        $Identification = new Auction();
                    }
                    else if($plugin_name == 'FlashSalePublic')
                    { // 限时折扣
                        $Identification = new FlashSalePublic();
                    }
                    else if($plugin_name == 'Members')
                    { // 会员
                        $Identification = new Members();
                    }
                    else if($plugin_name == 'LivingPublic')
                    { // 直播
                        $Identification = new LivingPublic();
                    }
                    if($Identification != '')
                    {
                        if (method_exists($Identification, 'Get_plugin_status'))
                        {
                            $list[$plugin_code] = $Identification->Get_plugin_status($store_id);
                        }
                    }
                    else
                    {
                        if($plugin_code == 'mch' && $v['status'] == 1)
                        {
                            $list[$plugin_code] = 1;
                        }
                        else
                        {
                            $list[$plugin_code] = 0;
                        }
                    }
                }
            }
        }

        return $list;
    }
}
