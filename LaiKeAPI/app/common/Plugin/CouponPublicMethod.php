<?php
namespace app\common\Plugin;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;
use app\common\ExcelUtils;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Jurisdiction;

use app\admin\model\CouponConfigModel;
use app\admin\model\CouponActivityModel;
use app\admin\model\CouponModel;
use app\admin\model\ProductListModel;
use app\admin\model\OrderModel;
use app\admin\model\CouponSnoModel;
use app\admin\model\MchModel;
use app\admin\model\ProductClassModel;
use app\admin\model\BrandClassModel;
use app\admin\model\UserModel;
use app\admin\model\ConfigureModel;
use app\admin\model\AdminModel;

class CouponPublicMethod
{
    // 获取插件状态
    public function is_Plugin($store_id)
    {
        $r0 = CouponConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('is_status')->select()->toArray();
        if ($r0)
        {
            $is_display = $r0[0]['is_status'];
        }
        else
        {
            $is_display = 2;
        }
        return $is_display;
    }

    // 添加插件设置
    public function add($store_id)
    {
        $data = ['store_id' => $store_id,'mch_id'=>0];
        Db::name('lkt_coupon_config')->save($data);
        return;
    }

    // 删除插件设置
    public function del($store_id)
    {
        Db::table('lkt_coupon_config')->where('store_id',$store_id)->delete();
        return;
    }

    // 前端首页
    public function test($store_id,$mch_id = 0)
    {
        $time = date('Y-m-d H:i:s'); // 当前时间

        $is_status = 0; // 状态 0：未启用 1：启用
        // 查询优惠券设置
        $r0 = CouponConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('is_status,is_show')->select()->toArray();
        if ($r0)
        {
            $is_status = $r0[0]['is_status']; // 状态 0：未启用 1：启用
            $is_show = $r0[0]['is_show']; // 店铺优惠券设置 0：未启用 1：启用
            if($is_status == 1)
            {
                if($mch_id != 0)
                {
                    $is_status = $is_show;
                }
            }
        }

        // 查询该商城所有启用/禁用的优惠券
        $r2 = CouponActivityModel::where(['store_id'=>$store_id,'recycle'=>0])->whereIn('status','1,2')->field('id,end_time')->order('start_time','desc')->select()->toArray();
        if ($r2)
        {
            foreach ($r2 as $k => $v)
            {
                $id = $v['id']; // 优惠券活动id
                $end_time = $v['end_time']; // 活动结束时间
                if ($end_time != null && $end_time <= $time)
                {
                    // 过期,根据活动id修改活动状态
                    $r2_2 = CouponActivityModel::where(['store_id'=>$store_id,'recycle'=>0,'id'=>$id])->find();
                    $r2_2->status = 3;
                    $r2_2->save();
                }
            }
        }
        return $is_status;
    }

    // 获取店铺优惠券状态
    public function Get_store_coupon_status($store_id,$mch_id)
    {
        $status = $this->Get_plugin_status_mch($store_id); // 店铺是否看见该插件

        $is_open_coupon = 0; // 是否开启店铺主页领卷人口 1.开启 0.关闭
        if($status != 1)
        { // 店铺不能看见该插件
            $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID

            if($SelfOperatedStore_id == $mch_id)
            {
                $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id,'recovery'=>0])->field('is_open_coupon')->select()->toArray();
                if($r_mch)
                {
                    $is_open_coupon = $r_mch[0]['is_open_coupon']; // 是否开启店铺主页领卷人口 1.开启 0.关闭
                }
            }
        }
        else
        {
            $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id,'recovery'=>0])->field('is_open_coupon')->select()->toArray();
            if($r_mch)
            {
                $is_open_coupon = $r_mch[0]['is_open_coupon']; // 是否开启店铺主页领卷人口 1.开启 0.关闭
            }
        }

        return $is_open_coupon;
    }

    // 移动端-领券中心
    public function mobile_terminal_coupon_center($array)
    {
        $store_id = $array['store_id'];
        $user_id = $array['user_id'];
        $type = $array['type'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $mcnName = "";
        if(isset($array['mcnName']))
        {
            $mcnName = $array['mcnName'];
        }

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $end = $start + $pagesize;

        $is_status = $this->Get_plugin_status($store_id); // 用户是否看见该插件
       
        $status = $this->Get_plugin_status_mch($store_id); // 店铺是否看见该插件

        $mch_list = array();
        $mch_str = '';
        $mch_id = '0';
        if($mcnName == '')
        {
            // 查询商城里的所有店铺
            $r_mch = MchModel::where(['store_id'=>$store_id,'review_status'=>1,'is_lock'=>0,'recovery'=>0])->field('id,is_open_coupon')->select()->toArray();
        }
        else
        {
            $mcnName_00 = Tools::FuzzyQueryConcatenation($mcnName);
            $sql_mch = "select id,is_open_coupon from lkt_mch where store_id = '$store_id' and review_status = 1 and is_lock = 0 and recovery = 0 and name like $mcnName_00 ";
            $r_mch = Db::query($sql_mch);
        }
        if($r_mch)
        {
            foreach ($r_mch as $k => $v)
            {
                if($v['is_open_coupon'] == 1)
                {
                    $mch_list[] = $v['id'];
                }
            }
        }

        $total = 0;
        $list = array();
        if($type == 1)
        { // 平台优惠券
            $sql_where = array('store_id'=>$store_id,'recycle'=>0,'mch_id'=>0,'status'=>1,'is_display'=>1,'receive_type'=>0);
            $field_str = "id,store_id,mch_id,name,activity_type,grade_id,money,discount,z_money,shopping,free_mail_task,circulation,num,receive,start_time,end_time,type,skip_type,status,add_time,recycle,day,is_display,issue_unit,receive_type,is_auto_push,Instructions,cover_map";
            // 根据商城ID，查询当页平台优惠券
            $r0 = CouponActivityModel::where($sql_where)->where('activity_type','<>','4')->field('count(id) as total')->select()->toArray();
            
            $r1 = CouponActivityModel::where($sql_where)->where('activity_type','<>','4')->field($field_str)->select()->toArray();
        }
        else
        { // 店铺优惠券
            if($is_status != 1)
            { // 用户不可以看见优惠券插件
                $data = array('total'=>$total,'list'=>$list);
                return $data;
            }

            $sql_where = array('store_id'=>$store_id,'recycle'=>0,'status'=>1,'is_display'=>1,'receive_type'=>0);
            $field_str = "id,store_id,mch_id,name,activity_type,grade_id,money,discount,z_money,shopping,free_mail_task,circulation,num,receive,start_time,end_time,type,skip_type,status,add_time,recycle,day,is_display,issue_unit,receive_type,is_auto_push,Instructions,cover_map";

            if($status == 1)
            { // 店铺可以看见优惠券插件
                if($mch_list != array())
                {
                    $mch_str = implode(',',$mch_list);
                }
            }
            else
            { // 店铺不可以看见优惠券插件
                $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID
                if(in_array($SelfOperatedStore_id,$mch_list))
                { // 自营店开启了 店铺领券入口
                    $mch_str = $SelfOperatedStore_id;
                }
            }

            if($mch_str != '')
            {
                $mch_id = $mch_str;

                $r0 = CouponActivityModel::where($sql_where)->where('activity_type','<>','4')->whereIn('mch_id',$mch_id)->field('count(id) as total')->select()->toArray();

                $r1 = CouponActivityModel::where($sql_where)->where('activity_type','<>','4')->whereIn('mch_id',$mch_id)->field($field_str)->select()->toArray();
            }
            else
            {
                $data = array('total'=>$total,'list'=>$list);
                return $data;
            }
        }
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        if($r1)
        {
            $arr_list = $this->coupon_center($store_id,$user_id,$r1);
            if($arr_list != array())
            {
                foreach($arr_list as $k => $v)
                {
                    if($start <= $k && $k < $end)
                    {
                        $list[] = $v;
                    }
                }
            }
        }
        
        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // PC端-首页
    public function pc_coupon($store_id, $user_id)
    {
        $time = date('Y-m-d H:i:s');  // 当前时间

        $status = $this->Get_plugin_status_mch($store_id); // 店铺是否看见该插件

        $mch_list = array();
        $mch_str = '';
        $mch_id = '0';
        // 查询商城里的所有店铺
        $r_mch = MchModel::where(['store_id'=>$store_id,'review_status'=>1,'is_lock'=>0,'recovery'=>0])->field('id,is_open_coupon')->select()->toArray();
        if($r_mch)
        {
            foreach ($r_mch as $k => $v)
            {
                if($v['is_open_coupon'] == 1)
                {
                    $mch_list[] = $v['id'];
                }
            }
        }

        if($status == 1)
        { // 店铺可以看见优惠券插件
            if($mch_list != array())
            {
                $mch_str = implode(',',$mch_list);
            }
        }
        else
        {
            $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID
            if(in_array($SelfOperatedStore_id,$mch_list))
            {
                $mch_str = $SelfOperatedStore_id;
            }
        }

        if($mch_str != '')
        {
            $mch_id = $mch_id . ',' . $mch_str;
        }

        // 查询该商城所有未开启的优惠券
        $r0_0 = CouponActivityModel::where(['store_id'=>$store_id,'recycle'=>0,'status'=>0,'is_display'=>1])->order('start_time','desc')->field('id,start_time')->select()->toArray();
        if ($r0_0)
        {
            foreach ($r0_0 as $k => $v)
            {
                $id = $v['id']; // 优惠券活动id
                $start_time1 = $v['start_time']; // 活动开始时间
                if ($start_time1 <= $time)
                { // 当优惠券活动开始时间 <= 当前时间
                    // 修改优惠券活动状态(开启状态)
                    $sql0_1_update = array('status'=>1);
                    $sql0_1_where = array('store_id'=>$store_id,'id'=>$id,'recycle'=>0);
                    Db::name('coupon_activity')->where($sql0_1_where)->update($sql0_1_update);
                    
                    $Log_content = '开启优惠券活动，ID为' . $id . '！';
                    $this->couponLog($id, $Log_content);
                }
            }
        }

        // 根据商城ID，查询当页的优惠券信息
        $r0 = CouponActivityModel::where(['store_id'=>$store_id,'recycle'=>0,'status'=>1,'is_display'=>1,'receive_type'=>0])->where('activity_type','<>',4)->whereIn('mch_id',$mch_id)->select()->toArray();

        $list = array();
        $arr_list = $this->coupon_center($store_id,$user_id,$r0);
        if($arr_list != array())
        {
            foreach($arr_list as $k => $v)
            {
                if(0 <= $k && $k < 8)
                {
                    $list[] = $v;
                }
            }
        }
        return $list;
    }

    // PC端-领券中心
    public function pc_coupon_center($store_id, $user_id,$page,$type,$mcnName)
    {
        $time = date('Y-m-d H:i:s');  // 当前时间
        $start = 0;
        $end = 10;
        if ($page)
        {
            $start = ($page - 1) * $end;
        }
        $end = $start + $end;


        $is_status = $this->Get_plugin_status($store_id); // 用户是否看见该插件
       
        $status = $this->Get_plugin_status_mch($store_id); // 店铺是否看见该插件

        $mch_list = array();
        $mch_str = '';
        $mch_id = '0';
        if($mcnName == '')
        {
            // 查询商城里的所有店铺
            $r_mch = MchModel::where(['store_id'=>$store_id,'review_status'=>1,'is_lock'=>0,'recovery'=>0])->field('id,is_open_coupon')->select()->toArray();
        }
        else
        {
            $mcnName_00 = Tools::FuzzyQueryConcatenation($mcnName);
            $sql_mch = "select id,is_open_coupon from lkt_mch where store_id = '$store_id' and review_status = 1 and is_lock = 0 and recovery = 0 and name like $mcnName_00 ";
            $r_mch = Db::query($sql_mch);
        }
        if($r_mch)
        {
            foreach ($r_mch as $k => $v)
            {
                if($v['is_open_coupon'] == 1)
                {
                    $mch_list[] = $v['id'];
                }
            }
        }
        
        $total = 0;
        $list = array();

        if($type == 1)
        { // 平台优惠券
            $sql_where = array('store_id'=>$store_id,'recycle'=>0,'mch_id'=>0,'status'=>1,'is_display'=>1,'receive_type'=>0);
            $field_str = "id,store_id,mch_id,name,activity_type,grade_id,money,discount,z_money,shopping,free_mail_task,circulation,num,receive,start_time,end_time,type,skip_type,status,add_time,recycle,day,is_display,issue_unit,receive_type,is_auto_push,Instructions,cover_map";
            // 根据商城ID，查询当页平台优惠券
            $r_total = CouponActivityModel::where($sql_where)->where('activity_type','<>','4')->field('count(id) as total')->select()->toArray();
            
            $r0 = CouponActivityModel::where($sql_where)->where('activity_type','<>','4')->field($field_str)->order('add_time','desc')->select()->toArray();
        }
        else
        { // 店铺优惠券
            if($is_status != 1)
            { // 用户不可以看见优惠券插件
                $data = array('total'=>$total,'list'=>$list);
                return $data;
            }

            $sql_where = array('store_id'=>$store_id,'recycle'=>0,'status'=>1,'is_display'=>1,'receive_type'=>0);
            $field_str = "id,store_id,mch_id,name,activity_type,grade_id,money,discount,z_money,shopping,free_mail_task,circulation,num,receive,start_time,end_time,type,skip_type,status,add_time,recycle,day,is_display,issue_unit,receive_type,is_auto_push,Instructions,cover_map";

            if($status == 1)
            { // 店铺可以看见优惠券插件
                if($mch_list != array())
                {
                    $mch_str = implode(',',$mch_list);
                }
            }
            else
            { // 店铺不可以看见优惠券插件
                $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID
                if(in_array($SelfOperatedStore_id,$mch_list))
                { // 自营店开启了 店铺领券入口
                    $mch_str = $SelfOperatedStore_id;
                }
            }
            
            if($mch_str != '')
            {
                $mch_id = $mch_str;

                $r_total = CouponActivityModel::where($sql_where)->where('activity_type','<>','4')->whereIn('mch_id',$mch_id)->field('count(id) as total')->select()->toArray();

                $r0 = CouponActivityModel::where($sql_where)->where('activity_type','<>','4')->whereIn('mch_id',$mch_id)->field($field_str)->order('add_time','desc')->select()->toArray();
            }
            else
            {
                $data = array('total'=>$total,'list'=>$list);
                return $data;
            }
        }

        $total = $r_total[0]['total'];

        $arr_list = $this->coupon_center($store_id,$user_id,$r0);
        if($arr_list != array())
        {
            foreach($arr_list as $k => $v)
            {
                if($start <= $k && $k < $end)
                {
                    $list[] = $v;
                }
            }
        }
        $data = array('list'=>$list,'total'=>$total);
        return $data;
    }

    // 领券中心数据处理
    public function coupon_center($store_id,$user_id,$list)
    {
        $time = date('Y-m-d H:i:s');  // 当前时间
        $arr = array();
        if ($list)
        {
            foreach ($list as $k => $v)
            {
                $activity_id = $v['id']; // 优惠券活动id
                $mch_id = $v['mch_id']; // 店铺id
                $activity_type = $v['activity_type']; // 优惠券类型
                $z_money = floatval($v['z_money']); // 满多少
                $v['money'] = floatval($v['money']); // 面值
                $v['discount'] = floatval($v['discount']); // 折扣值
                $v['z_money'] = floatval($v['z_money']); // 满多少
                $receive = $v['receive']; // 领取限制
                $num = $v['num']; // 剩余个数
                
                $v['cover_map'] = ServerPath::getimgpath($v['cover_map'], $store_id);

                $v['issue_number_type'] = 2;
                $v['issue_number_type_str'] = $v['circulation'];
                if($v['circulation'] >= 9999999)
                {
                    $v['issue_number_type'] = 1;
                    $v['issue_number_type_str'] = "不限制";
                }
                
                if($v['start_time'] != null)
                {
                    $v['start_time'] = date('Y-m-d', strtotime($v['start_time']));
                }
                else
                {
                    $v['start_time'] = '';
                }
                $end_time = $v['end_time']; // 活动结束时间
                if($v['end_time'] != null)
                {
                    
                    $v['end_time'] = date('Y-m-d', strtotime($v['end_time']));
                }
                else
                {
                    $v['end_time'] = '';
                }
                
                if($mch_id != 0)
                {
                    $r0 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name')->select()->toArray();
                    if($r0)
                    {
                        $v['name'] = '[' . $r0[0]['name'] . ']' . $v['name'];
                    }
                }
                if ($z_money != 0)
                {
                    $v['limit'] = '满' . $z_money . '可用'; // 限制
                }
                else
                {
                    $v['limit'] = '无限制'; // 限制
                }
                // 判断活动是否过期
                if (empty($v['day']) && $end_time != null && $end_time <= $time)
                {
                    // 过期,根据活动id修改活动状态
                    $data0_0 = array('store_id'=>$store_id,'recycle'=>0,'id'=>$activity_id);
                    $sql0_0 = CouponActivityModel::where($data0_0)->find();
                    $sql0_0->status = 3;
                    $sql0_0->save();
                }
                else
                {
                    if ($user_id == '')
                    { // 未登录
                        $v['point'] = '立即领取';
                        $v['point_type'] = 1;
                        $arr[] = (array)$v;
                    }
                    else
                    { // 已登录
                        if ($num > 0)
                        { // 还有剩下优惠券没领取
                            // $sql_where = array('store_id'=>$store_id,'user_id'=>$user_id,'recycle'=>0,'free_or_not'=>0,'hid'=>$activity_id);
                            $sql_where = array('store_id'=>$store_id,'user_id'=>$user_id,'free_or_not'=>0,'hid'=>$activity_id);
                            // 根据优惠券活动id、用户id，查询用户领取的优惠券
                            $r1 = CouponModel::where($sql_where)->field('id')->select()->toArray();
                            if ($r1)
                            { // 已领取优惠券
                                if($receive > count($r1))
                                { // 领取限制 > 领取数量
                                    $v['point'] = '立即领取';
                                    $v['point_type'] = 1;
                                }
                                else
                                {
                                    // 根据优惠券活动id、用户id，查询用户未使用的优惠券
                                    $r2 = CouponModel::where($sql_where)->whereIn('type','0,1')->field('id')->select()->toArray();
                                    if($r2)
                                    { // 存在未使用的优惠券
                                        $v['point'] = '去使用';
                                        $v['point_type'] = 2;
                                    }
                                    else
                                    { // 不存在未使用的优惠券
                                        $v['point'] = '已使用';
                                        $v['point_type'] = 4;
                                    }
                                }
                            }
                            else
                            { // 未领取优惠券
                                $v['point'] = '立即领取';
                                $v['point_type'] = 1;
                            }
                        }
                        else
                        { // 优惠券已经领完
                            $v['point'] = '已抢光';
                            $v['point_type'] = 3;
                        }
                        $arr[] = $v;
                    }
                }
            }
        }

        $arr_1 = array();
        $arr_2 = array();
        $arr_3 = array();
        $arr_4 = array();
        foreach ($arr as $k_a => $v_a)
        {
            if($v_a['point_type'] == 1)
            {
                $arr_1[] = $v_a;
            }
            else if($v_a['point_type'] == 2)
            {
                $arr_2[] = $v_a;
            }
            else if($v_a['point_type'] == 3)
            {
                $arr_3[] = $v_a;
            }
            else 
            {
                $arr_4[] = $v_a;
            }
        }

        $arr = array_merge($arr_1,$arr_2,$arr_3,$arr_4);

        // array_multisort(array_column($arr, 'point_type'), SORT_ASC, $arr);

        return $arr;
    }

    // PC端-商品详情获取商品可用优惠券活动
    public function mch_pro_coupon($store_id, $user_id, $pro_id)
    {
        $time = date('Y-m-d H:i:s'); // 当前时间
        $arr = array();
        $list = array();
        // 根据商品ID，查询分类和店铺ID
        $r0 = ProductListModel::where(['store_id'=>$store_id,'id'=>$pro_id])->field('product_class,mch_id')->select()->toArray();
        if ($r0)
        {
            $mch_id = $r0[0]['mch_id']; // 店铺ID
            $product_class = $r0[0]['product_class']; // 分类
            $product_class = trim($product_class, '-');
            $product_class_list1 = explode('-', $product_class);

            $str = "id,store_id,mch_id,name,activity_type,grade_id,money,discount,z_money,shopping,free_mail_task,circulation,num,receive,type,skip_type,status,add_time,recycle,day,Instructions,is_display,issue_unit,receive_type,is_auto_push,end_time";
            // 根据店铺ID，查询店铺优惠券
            $r1 = CouponActivityModel::where(['store_id'=>$store_id,'recycle'=>0,'status'=>1,'is_display'=>1,'mch_id'=>$mch_id])->order('add_time','desc')->field($str)->select()->toArray();
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    $activity_id = $v['id']; // 优惠券活动id
                    $activity_type = $v['activity_type']; // 优惠券类型
                    $z_money = floatval($v['z_money']); // 满多少
                    $receive = $v['receive']; // 领取限制
                    $num = $v['num']; // 剩余个数

                    $v['expirationDate'] = "不限制";
                    if($v['end_time'] == null)
                    {
                        $v['end_time'] = '';
                        if($v['day'] > 0)
                        {
                            $v['expirationDate'] = "领取后" . $v['day'] . "天失效";
                        }
                    }
                    else
                    {
                        $v['expirationDate'] = date("Y-m-d",strtotime($v['end_time']));
                    }
                    
                    if($mch_id != 0)
                    {
                        $r_0 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name')->select()->toArray();
                        if($r_0)
                        {
                            $v['coupon_name'] = '[' . $r_0[0]['name'] . ']' . $v['name'];
                        }
                    }

                    if ($z_money != 0)
                    {
                        $v['limit'] = '满' . $z_money . '可用'; // 限制
                    }
                    else
                    {
                        $v['limit'] = '无限制'; // 限制
                    }
                    $v['point'] = '';
                    
                    if ($user_id == '')
                    {
                        $v['point'] = '立即领取';
                        $v['point_type'] = 1;
                    }
                    else
                    {
                        if ($num > 0)
                        {
                            $r3 = CouponModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'free_or_not'=>0,'hid'=>$activity_id])->field('count(id) as num')->select()->toArray();
                            if ($r3[0]['num'] > 0)
                            {
                                // 已领取
                                if ($receive > $r3[0]['num'])
                                {
                                    if ($num > 0)
                                    {
                                        $v['point'] = '立即领取';
                                        $v['point_type'] = 1;
                                    }
                                    else
                                    {
                                        $v['point'] = '去使用';
                                        $v['point_type'] = 2;
                                    }
                                }
                                else
                                {
                                    $v['point'] = '去使用';
                                    $v['point_type'] = 2;
                                }
                            }
                            else
                            {
                                // 还有剩下优惠券没领取
                                $v['point'] = '立即领取';
                                $v['point_type'] = 1;
                            }
                        }
                        else
                        {
                            $v['point'] = '已抢光';
                            $v['point_type'] = 3;
                        }
                    }

                    if ($activity_type != 4)
                    {
                        if ($v['type'] == 1)
                        { // 全部商品
                            if ($v['point'] != '已抢光')
                            {
                                $arr[] = (array)$v;
                            }
                        }
                        else if ($v['type'] == 2)
                        { // 指定商品
                            $product_list = unserialize($v['product_id']);
                            $product_list = explode(',', $product_list); // 优惠券绑定的商品id
                            if (in_array($pro_id, $product_list))
                            { // 当优惠券绑定的商品分类ID存在于商品分类数组里
                                if ($v['point'] != '已抢光')
                                {
                                    $arr[] = (array)$v;
                                }
                            }
                        }
                        else if ($v['type'] == 3)
                        {
                            $product_class_list = unserialize($v['product_class_id']);
                            $product_class_list = explode(',', $product_class_list); // 优惠券绑定的商品分类id
                            foreach ($product_class_list as $key => $val)
                            { // 当优惠券绑定的商品分类ID存在于商品分类数组里
                                if (in_array($val, $product_class_list1))
                                {
                                    if ($v['point'] != '已抢光')
                                    {
                                        $arr[] = (array)$v;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        $arr = Tools::assoc_unique($arr,'id');

        foreach($arr as $k => $v)
        {
            if($v['point_type'] == 1)
            {
                $list[] = $v;
            }
        }
        return $list;
    }

    // 点击立即领取
    public function receive($store_id, $user_id, $id)
    {
        $time = date("Y-m-d H:i:s");
        $res = '';
        // 根据商城ID、优惠券ID、活动状态，查询优惠券活动
        $r0 = CouponActivityModel::where(['store_id'=>$store_id,'recycle'=>0,'id'=>$id])->select()->toArray();
        if ($r0)
        {
            $mch_id = $r0[0]['mch_id']; // 店铺ID
            $name = $r0[0]['name']; // 优惠券名称
            $receive = $r0[0]['receive']; // 领取限制
            $num = $r0[0]['num']; // 剩余数量
            $end_time = $r0[0]['end_time']; // 活动结束时间
            $status = $r0[0]['status']; // 活动状态
            $day = $r0[0]['day']; // 活动状态

            $r0_0 = CouponModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'free_or_not'=>0,'hid'=>$id])->field('count(id) as num')->select()->toArray();
            $num0_0 = $r0_0[0]['num'];
            if ($receive > $num0_0)
            { // 当领取限制 大于 当前领取数量
                if ($status == 1)
                { // 正在进行中
                    if ($num > 0)
                    { // 有剩余数量
                        // 根据活动id,修改活动信息
                        $data1 = array('store_id'=>$store_id,'recycle'=>0,'id'=>$id);
                        $sql1 = CouponActivityModel::where($data1)->find();
                        $sql1->num =  Db::raw('num - 1');
                        $r1 = $sql1->save();
                        if (!$r1)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '领取优惠券活动ID为' . $id . '时，修改优惠券活动剩余量失败！';
                            $this->couponLog($id, $Log_content);
                            $message = Lang('Parameter error');
                            echo json_encode(array('code' => 115, 'message' => $message));
                            exit;
                        }

                        if($end_time == null)
                        { // 不是指定过期时间
                            if($day > 0)
                            { // 领取后多久后失效
                                $end_time = date("Y-m-d H:i:s",strtotime("+ $day day",time()));
                            }
                        }

                        // 在优惠券表里添加一条数据
                        $data2 = array('store_id' => $store_id,'mch_id' => $mch_id,'user_id' => $user_id,'add_time' => $time,'expiry_time' => $end_time,'hid' => $id);
                        $r2 = Db::name('coupon')->insert($data2);
                        if ($r2 == -1)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '领取优惠券活动ID为' . $id . '时，添加失败！';
                            $this->couponLog($id, $Log_content);
                            $message = Lang('Parameter error');
                            echo json_encode(array('code' => 115, 'message' => $message));
                            exit;
                        }
                        $res = '您领取了' . $name . '优惠券！';
                        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '领取优惠券活动ID为' . $id;
                        $this->couponLog($id, $Log_content);
                    }
                    else
                    { // 没有剩余数量
                        $res = '您来晚了！';
                        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '领取优惠券活动ID为' . $id . '时,来晚了！';
                        $this->couponLog($id, $Log_content);
                    }
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '领取优惠券活动ID为' . $id . '时，活动已结束！';
                    $this->couponLog($id, $Log_content);
                    $message = Lang('coupon.0');
                    echo json_encode(array('code' => 222, 'message' => $message));
                    exit;
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '领取优惠券活动ID为' . $id . '时，已达领取限制！';
                $this->couponLog($id, $Log_content);
                $message = Lang('Parameter error');
                echo json_encode(array('code' => 115, 'message' => $message . 3));
                exit;
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '领取优惠券活动ID为' . $id . '时，ID参数错误！';
            $this->couponLog($id, $Log_content);
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }
        return $res;
    }

    // 我的优惠券
    public function mycoupon($store_id, $user_id, $type)
    {
        $time = date('Y-m-d H:i:s'); // 当前时间
        $data = array();
        $list = array();
        $list1 = array();
        $list2 = array();
        $list3 = array();

        $isCouponShow = false;
        $r = CouponConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->select()->toArray();
        if($r)
        {
            if($r[0]['is_status'] == 1)
            {
                $isCouponShow = true;
            }
        }

        // 根据用户id,查询优惠券表
        $r0 = CouponModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'status'=>0,'recycle'=>0])->order('type','asc')->order('add_time','asc')->select()->toArray();
        
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $id = $v['id']; // 优惠券id
                $hid = $v['hid']; // 活动id
                if($v['add_time'] != null)
                {
                    $v['add_time'] = date('Y-m-d', strtotime($v['add_time']));
                }
                else
                {
                    $v['add_time'] = '';
                }
                if($v['expiry_time'] != null)
                {
                    $expiry_time1 = $v['expiry_time'] ; // 优惠券到期时间
                    $v['expiry_time'] = date('Y-m-d', strtotime($v['expiry_time']));
                }
                else
                {
                    $v['expiry_time'] = '';
                    $expiry_time1 = '';
                }
                $expiry_time = $v['expiry_time']; // 优惠券到期时间
                
                // 根据活动id,查询活动信息
                $r0_1 = CouponActivityModel::where(['store_id'=>$store_id,'id'=>$hid])->select()->toArray();
                if ($r0_1)
                {
                    $mch_id = $r0_1[0]['mch_id']; // 店铺ID
                    $v['mch_id'] = $r0_1[0]['mch_id']; // 店铺ID
                    $v['name'] = $r0_1[0]['name']; // 活动名称
                    $v['activity_type'] = $r0_1[0]['activity_type']; // 类型
                    $v['money'] = floatval($r0_1[0]['money']);
                    $v['discount'] = floatval($r0_1[0]['discount']);
                    $v['Instructions'] = $r0_1[0]['Instructions'];
                    $z_money = floatval($r0_1[0]['z_money']);

                    if($mch_id != 0)
                    {
                        // 根据店铺ID，查询店铺信息
                        $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name')->select()->toArray();
                        if($r_mch)
                        {
                            $v['name'] = '[' . $r_mch[0]['name'] . ']' . $v['name'];
                        }
                    }

                    if ($z_money != 0)
                    {
                        $v['limit'] = '满' . $z_money . '可用'; // 限制
                    }
                    else
                    {
                        $v['limit'] = '无限制'; // 限制
                    }
                }
                if ($expiry_time1 != '' && $expiry_time1 < $time && $v['type'] != 2)
                { // 已过期
                    // 根据用户id,修改优惠券表的状态
                    $sql = CouponModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'id'=>$id])->where('type','<>','2')->find();
                    $sql->type = '3';
                    $sql->save();
                    $v['type'] = 3;
                }

                if ($v['type'] == 0)
                { // 未使用
                    $v['point'] = '去使用';
                    $list1[] = $v;
                }
                else if ($v['type'] == 1)
                { // 使用中
                    // 根据用户ID、优惠券ID，查询未完成订单
                    $r_1 = OrderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'coupon_id'=>$v['id']])->whereNotIn('status','6,7')->field('id')->select()->toArray();
                    if ($r_1)
                    { // 存在
                        $v['point'] = '已使用';
                        $list2[] = $v;
                    }
                    else
                    { // 不存在
                        $v['point'] = '去使用';
                        $list1[] = $v;
                    }
                }
                else if ($v['type'] == 2)
                { // 已使用
                    $v['point'] = '已使用';
                    $list2[] = $v;
                }
                else if ($v['type'] == 3)
                { // 已过期
                    $v['point'] = '已过期';
                    $list3[] = $v;
                }
            }
        }

        $wsy_num = count($list1);
        $ysy_num = count($list2);
        $ygq_num = count($list3);
        
        if ($type == '0')
        {
            $list = $list1;
        }
        else if ($type == '1')
        {
            $list = $list2;
        }
        else if ($type == '2')
        {
            $list = $list3;
        }
        $data = array('list'=>$list,'wsy_num'=>$wsy_num,'ysy_num'=>$ysy_num,'ygq_num'=>$ygq_num,'isCouponShow'=>$isCouponShow);
        // $data = array('list'=>$list);
        return $data;
    }

    // 店铺获取商品可用优惠券活动
    public function mch_coupon($store_id, $user_id, $mch_id,$type)
    {
        $time = date('Y-m-d H:i:s'); // 当前时间
        $arr = array();
        $sql_where = array('store_id'=>$store_id,'recycle'=>0,'mch_id'=>$mch_id,'status'=>1,'is_display'=>1);
        $field_str = "id,store_id,mch_id,name,activity_type,grade_id,money,discount,z_money,shopping,free_mail_task,circulation,num,receive,start_time,end_time,type,skip_type,status,add_time,recycle,day,is_display,issue_unit,receive_type,is_auto_push,Instructions";
        // 根据店铺ID，查询店铺正常的优惠券
        $r0 = CouponActivityModel::where($sql_where)->order('start_time','desc')->field($field_str)->select()->toArray();
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $activity_id = $v['id']; // 优惠券活动id
                $activity_type = $v['activity_type']; // 优惠券类型
                $v['money'] = round($v['money']); // 优惠券金额
                $v['discount'] = round($v['discount']); // 折扣
                $money = $v['money']; // 优惠券金额
                $discount = $v['discount']; // 折扣
                $z_money = floatval($v['z_money']); // 满多少
                $receive = $v['receive']; // 领取限制
                $num = $v['num']; // 剩余个数
                
                if($v['start_time'] != null)
                {
                    $v['start_time'] = date('Y-m-d', strtotime($v['start_time']));
                }
                else
                {
                    $v['start_time'] = '';
                }
                $end_time = $v['end_time']; // 活动结束时间
                if($v['end_time'] != null)
                {
                    $v['end_time'] = date('Y-m-d', strtotime($v['end_time']));
                    $v['expirationDate'] = $v['end_time'] . ' 到期';
                }
                else
                {
                    $v['end_time'] = '';
                    $v['expirationDate'] = '该劵永久有效';
                }

                if($mch_id != 0)
                {
                    // 根据店铺ID，查询店铺信息
                    $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name')->select()->toArray();
                    if($r_mch)
                    {
                        $v['coupon_name'] = '[' . $r_mch[0]['name'] . ']' . $v['name'];
                    }
                }

                if ($z_money != 0)
                {
                    $v['limit'] = '满' . $z_money . '可用'; // 限制
                }
                else
                {
                    $v['limit'] = '无限制'; // 限制
                }
                $v['point'] = '';
                // 判断活动是否过期
                if ($end_time != null && $end_time <= $time)
                { // 过期,根据活动id修改活动状态
                    $sql = CouponActivityModel::where(['store_id'=>$store_id,'recycle'=>0,'id'=>$activity_id])->find();
                    $sql->status = '3';
                    $sql->save();
                }
                else
                {
                    if ($user_id == '')
                    {
                        if($type == 1)
                        { // 店铺主页
                            $v['point'] = '立即领取';
                        }
                        else
                        { // 购物车
                            $v['point'] = '领取';
                        }
                        $v['point_type'] = 1;
                    }
                    else
                    {
                        if ($num > 0)
                        {
                            $sql_where = array('store_id'=>$store_id,'user_id'=>$user_id,'recycle'=>0,'free_or_not'=>0,'hid'=>$activity_id);

                            $r3 = CouponModel::where($sql_where)->field('count(id) as num')->select()->toArray();
                            if ($r3[0]['num'] > 0)
                            { // 已领取
                                if ($receive > $r3[0]['num'])
                                { // 领取限制 > 领取数量
                                    if($type == 1)
                                    { // 店铺主页
                                        $v['point'] = '立即领取';
                                    }
                                    else
                                    { // 购物车
                                        $v['point'] = '领取';
                                    }
                                    $v['point_type'] = 1;
                                }
                                else
                                { // 领取限制 = 领取数量
                                    if($type == 1)
                                    { // 店铺主页
                                        $v['point'] = '可用商品';
                                    }
                                    else
                                    { // 购物车
                                        $v['point'] = '已领取';
                                    }
                                    $v['point_type'] = 2;
                                }
                            }
                            else
                            { // 还有剩下优惠券没领取
                                if($type == 1)
                                { // 店铺主页
                                    $v['point'] = '立即领取';
                                }
                                else
                                { // 购物车
                                    $v['point'] = '领取';
                                }
                                $v['point_type'] = 1;
                            }
                        }
                        else
                        { // 剩余个数为0
                            $v['point'] = '已抢光';
                            $v['point_type'] = 3;
                        }
                    }
                }
                if ($activity_type != 4)
                {
                    $arr[] = $v;
                }
            }
        }
        return $arr;
    }

    // 店铺优惠券
    public function store_coupons($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $hid = $array['hid'];
        $name = $array['name'];
        $activity_type = $array['activity_type'];
        $status = $array['status'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $time = date('Y-m-d H:i:s'); // 当前时间
        $issueUnit = '';
        if(isset($array['issueUnit']))
        {
            $issueUnit = $array['issueUnit'];
        }
        $receive_type = '';
        if(isset($array['receive_type']))
        {
            $receive_type = $array['receive_type'];
        }

        $exportType = '';
        if(isset($array['exportType']))
        {
            $exportType = $array['exportType'];
        }

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $shop_id = $r_admin[0]['shop_id']; // 店铺id

        $coupon_type_list = array();
        $r_config = CouponConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('coupon_type')->select()->toArray();
        if ($r_config)
        {
            $coupon_type = explode(",", $r_config[0]['coupon_type']); // 优惠券类型
            foreach ($coupon_type as $k => $v)
            {
                if ($v == '1')
                {
                    $coupon_type_list[] = '免邮券';
                }
                else if ($v == '2')
                {
                    $coupon_type_list[] = '满减券';
                }
                else if ($v == '3')
                {
                    $coupon_type_list[] = '折扣券';
                }
            }
        }

        $data_condition = " store_id = '$store_id' and recycle = 0 ";
        
        if($hid != '' && $hid != 0)
        {
            $data_condition .= " and id = '$hid' ";
        }

        if ($activity_type != '' && $activity_type != 0)
        {
            $data_condition .= " and activity_type = '$activity_type' ";
        }
        if($receive_type != '')
        {
            $data_condition .= " and receive_type = '$receive_type' ";
        }

        if($mch_id != '' && $mch_id != 0)
        {
            $data_condition .= " and mch_id = '$mch_id' ";

            $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('is_open_coupon')->select()->toArray();
            $isOpenCoupon = $r_mch[0]['is_open_coupon']; // 是否开启店铺主页领卷人口
        }
        else
        {
            $shop_id0 = '0,' . $shop_id; 
            $data_condition .= " and mch_id in ($shop_id0) ";
        }

        if($issueUnit == '0')
        {
            $data_condition .= " and issue_unit = 0 ";
        }
        else if($issueUnit == '1')
        {
            $data_condition .= " and issue_unit in (1,2) ";
        }

        if ($name != '')
        {
            $name_00 = Tools::FuzzyQueryConcatenation($name);
            $data_condition .= " and name like $name_00 ";
        }
        if($status != '')
        {
            if ($status == 1)
            {
                $data_condition .= " and status != 3 ";
            }
            else
            {
                $data_condition .= " and status = 3 ";
            }
        }

        $list = array();
        $total = 0;
        $sql0 = "select count(id) as total from lkt_coupon_activity where $data_condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $field_str = "id,store_id,mch_id,name,activity_type,grade_id,money,discount,consumption_threshold_type,z_money,shopping,free_mail_task,circulation,num,receive,start_time,end_time,type,skip_type,status,add_time,recycle,day,is_display,issue_unit,receive_type,is_auto_push,Instructions,product_class_id,product_id,cover_map";
        
        $sql1 = "select $field_str from lkt_coupon_activity where $data_condition order by add_time desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $id = $v['id']; // 活动id
                $activity_type1 = $v['activity_type']; // 活动类型
                $type1 = $v['type']; // 活动类型
                $v['discount'] = floatval($v['discount']);
                $v['cover_map'] = ServerPath::getimgpath($v['cover_map'], $store_id);

                $v['couponType'] = $v['activity_type'];
                $v['pickup_type'] = $v['receive_type'];
                $v['money'] = round($v['money'],2);
                $v['z_money'] = round($v['z_money'],2);
                $v['value'] = 0;
                if ($activity_type1 == 1)
                {
                    $v['activity_type'] = '免邮券';
                }
                else if ($activity_type1 == 2)
                {
                    $v['activity_type'] = '满减券';
                    $v['value'] = $v['money'];
                }
                else if ($activity_type1 == 3)
                {
                    $v['activity_type'] = '折扣券';
                    $v['value'] = $v['discount'];
                }
                else if ($activity_type1 == 4)
                {
                    $v['activity_type'] = '会员赠券';
                }
                
                if ($type1 == 1)
                {
                    $v['type'] = '全部商品';
                }
                else if ($type1 == 2)
                {
                    $v['type'] = '指定商品';
                    if($hid != '' && $hid != 0)
                    {
                        $v['goodsIdList'] = $this->get_goods($store_id,$v['mch_id'],unserialize($v['product_id']));
                    }
                }
                else if ($type1 == 3)
                {
                    $v['type'] = '指定分类';
                    if($hid != '' && $hid != 0)
                    {
                        $v['classIdList'] = $this->get_class($store_id,0,unserialize($v['product_class_id']));
                    }
                }
                else if ($type1 == 4)
                {
                    $v['type'] = '充值会员';
                }
                if($v['start_time'] == null)
                {
                    $v['start_time'] = '';
                }
                
                $v['date_type'] = 1; // 不限制
                $v['expirationDate'] = "不限制";

                if($v['end_time'] == null)
                {
                    $v['end_time'] = '';
                    if($v['day'] > 0)
                    {
                        $v['date_type'] = 3; // 设置领取多久后失效
                        $v['expirationDate'] = "领取后" . $v['day'] . "天失效";
                    }
                }
                else
                {
                    $v['date_type'] = 2; // 设置指定时间
                    $v['expirationDate'] = date("Y-m-d",strtotime($v['end_time']));
                }
                
                $v['isEnd'] = false;
                $v['isEndStr'] = "否";
                if($v['status'] == 3)
                {
                    $v['isEnd'] = true;
                    $v['isEndStr'] = "是";
                }
                else
                {
                    // 当前时间大于活动结束时间 只对固定结束日期的券（day<=0）才判过期
                    if ( $v['day'] <= 0 && $v['end_time'] != '' && $v['end_time'] <= $time)
                    {
                        // 根据id,修改活动状态
                        $sql3_where = array('store_id'=>$store_id,'id'=>$id);
                        $sql3_update = array('status'=>3);
                        Db::name('coupon_activity')->where($sql3_where)->update($sql3_update);
                        $v['status'] = 3;
                        $v['isEnd'] = true;
                        $v['isEndStr'] = "是";
                    }
                }

                $r4 = CouponModel::where(['store_id'=>$store_id,'hid'=>$id])->whereIn('type','0,1')->field('id')->select()->toArray();
                if ($r4)
                {
                    $v['del_status'] = 1;
                }
                else
                {
                    $v['del_status'] = 2;
                }
                $v['issueUnit'] = "商城";
                if($v['issue_unit'] == 1 || $v['issue_unit'] == 2)
                {
                    $v['issueUnit'] = "自营店";
                }
                $v['issue_number_type'] = 2;
                $v['issue_number_type_str'] = $v['circulation'];
                $v['issuer'] = $v['issue_unit'];
                if($v['circulation'] >= 9999999)
                {
                    $v['issue_number_type'] = 1;
                    $v['issue_number_type_str'] = "不限制";
                }

                $v['numStr'] = $v['num'];
                // if($v['num'] >= 9999999)
                // {
                //     $v['numStr'] = "无门槛";
                // }

                if($v['consumption_threshold_type'] == 1)
                {
                    $v['consumption_threshold_type_str'] = '无门槛';
                }
                else
                {
                    $v['consumption_threshold_type_str'] = round($v['z_money'],2);
                }

                // $v['consumption_threshold_type'] = 1;
                // $v['consumption_threshold_type_str'] = '无门槛';
                // if($v['z_money'] >= 0)
                // {
                //     $v['consumption_threshold_type'] = 2;
                //     $v['consumption_threshold_type_str'] = round($v['z_money'],2);
                // }

                $list[] = $v;
            }
        }
        
        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => 'id',
                1 => '优惠券名称',
                2 => '发行单位',
                3 => '优惠券类型',
                4 => '可用范围',
                5 => '发行数量',
                6 => '面值/折扣',
                7 => '消费门槛',
                8 => '过期时间',
                9 => '是否过期',
                10 => '发行时间'
            );
            $exportExcel_list = array();

            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['id'],
                        $v['name'],
                        $v['issueUnit'],
                        $v['activity_type'],
                        $v['type'],
                        $v['issue_number_type_str'],
                        $v['value'],
                        $v['consumption_threshold_type_str'],
                        $v['expirationDate'],
                        $v['isEndStr'],
                        $v['add_time']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '优惠券列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }
        
        if($mch_id != '' && $mch_id != 0)
        {
            $data = array('coupon_type' => $coupon_type_list, 'list' => $list, 'total' => $total,'isOpenCoupon' => $isOpenCoupon);
        }
        else
        {
            $data = array('coupon_type' => $coupon_type_list, 'list' => $list, 'total' => $total);
        }
        $message = Lang('Success');
        echo json_encode(array('code' => '200', 'message' => $message, 'data' => $data));
        exit;
    }

    // 获取店铺商品
    public function get_mch_goods($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $productTitle = $array['product_title'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $product_class_arr = array();
        //分类下拉选择
        $r_class = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0])->order('sort','desc')->field('cid,pname')->select()->toArray();
        if($r_class)
        {
            foreach ($r_class as $key => $value)
            {
                $c = '-' . $value['cid'] . '-';
                $product_class_arr[$c] = $value['pname'];
                //循环第一层
                $r_e = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$value['cid']])->order('sort','desc')->field('cid,pname')->select()->toArray();
                if ($r_e)
                {
                    foreach ($r_e as $ke => $ve)
                    {
                        $cone = $c . $ve['cid'] . '-';
                        $product_class_arr[$cone] = $ve['pname'];

                        //循环第二层
                        $r_t = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$ve['cid']])->order('sort','desc')->field('cid,pname')->select()->toArray();
                        if ($r_t)
                        {
                            foreach ($r_t as $k => $v)
                            {
                                $ctow = $cone . $v['cid'] . '-';
                                $product_class_arr[$ctow] = $v['pname'];
                            }
                        }
                    }
                }
            }
        }
        
        $brand_class_arr = array();
        //品牌下拉选择
        $r_brand_class = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0])->select()->toArray();
        if($r_brand_class)
        {
            foreach ($r_brand_class as $key => $value)
            {
                $key0 = $value['brand_id'];
                $brand_class_arr[$key0] = $value['brand_name'];
            }
        }

        $condition = "a.store_id = '$store_id' and a.recycle = 0 and a.is_presell = 0 and a.commodity_type = '0' and a.mch_id = '$mch_id' ";
        if($productTitle)
        {
            $productTitle_0 = Tools::FuzzyQueryConcatenation($productTitle);
            $condition .= " and a.product_title like $productTitle_0 ";
        }

        $list = array();
        $total = 0;
        $sql0 = "select count(a.id) as total from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $str = "a.id,a.store_id,a.product_number,a.commodity_type,a.product_title,a.subtitle,a.label,a.scan,a.product_class,a.imgurl,a.content,a.richList,a.sort,a.add_date,a.upper_shelf_time,a.volume,a.initial,a.s_type,a.num,a.min_inventory,a.status,a.brand_id,a.is_distribution,a.is_default_ratio,a.keyword,a.weight,a.freight,a.is_zhekou,a.separate_distribution,a.recycle,a.gongyingshang,a.is_hexiao,a.active,a.mch_id,a.mch_status,a.search_num,a.publisher,a.is_zixuan,a.source,a.comment_num,a.cover_map,a.class_sort,a.display_position_sort,a.is_presell,a.show_adr,b.name as NAME";
        $sql1 = "select $str from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort asc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $class = $v['product_class'];
                $bid = $v['brand_id'];
                $v['p_name'] = array_key_exists($class, $product_class_arr) ? $product_class_arr[$class]:'顶级'; // 分类名称
                $v['brand_name'] = array_key_exists($bid, $brand_class_arr) ? $brand_class_arr[$bid]:'暂无'; // 品牌名称
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id);
                $v['receiving_form'] = '1';

                $pid = $v['id'];
                $r_s = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('min(price) as price')->select()->toArray();
                if ($r_s)
                {
                    $v['price'] = round($r_s[0]['price'],2);
                }
                $list[] = $v;
            }
        }

        $data = array('list' => $list,'total' => $total);
        $message = Lang('Success');
        echo json_encode(array('code' => '200', 'message' => $message, 'data' => $data));
        exit;
    }
    
    // 获取商品
    public function get_goods($store_id,$mch_id,$product_id)
    {
        $list = array();
        $field_str = "id,store_id,product_number,commodity_type,product_title,subtitle,label,scan,product_class,imgurl,content,richList,sort,add_date,upper_shelf_time,volume,initial,s_type,num,min_inventory,status,brand_id,is_distribution,is_default_ratio,keyword,weight,weight_unit,freight,is_zhekou,separate_distribution,recycle,gongyingshang,is_hexiao,active,mch_id,mch_status,search_num,show_adr,publisher,is_zixuan,source,comment_num,cover_map,class_sort,display_position_sort,is_presell,receiving_form";
        if($mch_id != '' && $mch_id != 0)
        {
            $sql0 = array('store_id'=>$store_id,'recycle'=>0,'mch_id'=>$mch_id,'mch_status'=>2,'active'=>1,'status'=>2,'is_presell'=>0);
        }
        else
        {
            $sql0 = array('store_id'=>$store_id,'recycle'=>0,'mch_status'=>2,'active'=>1,'status'=>2,'is_presell'=>0);
        }
        if($product_id != '')
        {
            $r0 = ProductListModel::where($sql0)->whereIn('id',$product_id)->order('sort','desc')->select()->toArray();
        }
        else
        {
            $r0 = ProductListModel::where($sql0)->order('sort','desc')->field($field_str)->select()->toArray();
        }
        if($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                $pid = $v['id'];
                $v['price'] = 0;

                $r1 = ConfigureModel::where(['pid'=>$pid])->field('max(price) as price')->select()->toArray();
                if($r1)
                {
                    $v['price'] = round($r1[0]['price'],2);
                }
                $list[] = $v;
            }
        }
        return $list;
    }
    
    // 获取商品分类
    public function get_class($store_id,$cid,$product_class_id)
    {
        $sql0 = array('store_id'=>$store_id,'recycle'=>0,'sid'=>$cid);
        if($product_class_id != '')
        {
            $r0 = ProductClassModel::where($sql0)->whereIn('cid',$product_class_id)->order('sort','desc')->select()->toArray();
        }
        else
        {
            $r0 = ProductClassModel::where($sql0)->order('sort','desc')->select()->toArray();
        }
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $cid = $v['cid'];
                $child = $this->get_class_lower($store_id,$cid,$product_class_id);
                $list[] = array('cid' => $v['cid'], 'cname' => $v['pname'], 'level' => $v['level'], 'sid' => $v['sid'], 'child' => $child);
            }
        }
        return $list;
    }
    
    // 根据上级分类ID，查询所有下级分类
    public function get_class_lower($store_id,$cid,$product_class_id)
    {
        $list = array();
        $child = array();
        $sql0 = array('store_id'=>$store_id,'recycle'=>0,'sid'=>$cid);
        if($product_class_id != '')
        {
            $r0 = ProductClassModel::where($sql0)->whereIn('cid',$product_class_id)->order('sort','desc')->select()->toArray();
        }
        else
        {
            $r0 = ProductClassModel::where($sql0)->order('sort','desc')->select()->toArray();
        }
        if($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $cid = $v['cid'];
                $child = $this->get_class_lower($store_id,$cid,$product_class_id);
                $list[] = array('cid'=>$v['cid'],'cname'=>$v['pname'],'level'=>$v['level'], 'sid' => $v['sid'],'child'=>$child);
            }
        }

        return $list;
    }

    // 添加店铺优惠券页面
    public function add_store_coupons_page($store_id,$mch_id)
    {
        $coupon_type_list = array();
        $limit_type = 0;

        $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id);
        
        $r0 = CouponConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('coupon_type,limit_type')->select()->toArray();
        if ($r0)
        {
            $limit_type = $r0[0]['limit_type'];
            $coupon_type = explode(",", $r0[0]['coupon_type']); // 优惠券类型
            foreach ($coupon_type as $k => $v)
            {
                if ($v == '1')
                {
                    if($mch_id == 0 || $SelfOperatedStore_id == $mch_id)
                    {
                        $coupon_type_list[] = array('value'=>$v,'name'=>'免邮券');
                    }
                }
                else if ($v == '2')
                {
                    $coupon_type_list[] = array('value'=>$v,'name'=>'满减券');
                }
                else if ($v == '3')
                {
                    $coupon_type_list[] = array('value'=>$v,'name'=>'折扣券');
                }
            }
        }

        $typeList = array();
        if($mch_id == 0 || $SelfOperatedStore_id == $mch_id)
        {
            $typeList[] = array('key'=>'1','value'=>'免邮券');
        }
        $typeList[] = array('key'=>'2','value'=>'满减券');
        $typeList[] = array('key'=>'3','value'=>'折扣券');

        $data = array('coupon_type_list'=>$coupon_type_list,'limit_type'=>$limit_type,'typeList'=>$typeList);
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message, 'data' => $data));
        exit;
    }

    // 添加优惠券
    public function add_store_coupons($data)
    {
        $Jurisdiction = new Jurisdiction();

        $time = date('Y-m-d H:i:s');
        $store_id = $data['store_id'];
        $mch_id = $data['mch_id'];
        $issue_unit = $data['issue_unit'];
        $activity_type = $data['activity_type'];
        $name = $data['name'];
        $money = $data['money'];
        $discount = $data['discount'];
        // $consumption_threshold_type = $data['consumption_threshold_type'];
        $z_money = $data['z_money'];
        if($z_money == '')
        {
            $consumption_threshold_type = 1;
        }
        else
        {
            $consumption_threshold_type = 2;
        }
        $type = $data['type'];
        $receive_type = $data['receive_type'];
        $circulation = $data['circulation'];
        $end_time = $data['end_time'];
        if($receive_type == 1)
        {
            $receive = 1;
        }
        else
        {
            $receive = $data['receive'];
        }
        $Instructions = $data['Instructions'];
        $day = $data['day'];
        $menu_list = $data['menu_list'];
        $class_list = $data['class_list'];
        $cover_map = $data['cover_map'];

        $shop_id = '';
        if(isset($data['shop_id']))
        {
            $shop_id = $data['shop_id'];
        }
        $operator_id = 0;
        if(isset($data['operator_id']))
        {
            $operator_id = $data['operator_id'];
        }
        $operator = '';
        if(isset($data['operator']))
        {
            $operator = $data['operator'];
        }
        $source = '';
        if(isset($data['source']))
        {
            $source = $data['source'];
        }

        if ($activity_type == '' || $activity_type == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择优惠券类型';
            $this->coupon_set_Log($Log_content);
            $message = Lang('coupon.1');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }
        else
        {
            if ($name == '')
            {
                $message = Lang('coupon.2');
                echo json_encode(array('code' => 115, 'message' => $message));
                exit;
            }

            if ($cover_map == '')
            {
                // $message = Lang('coupon.39');
                // echo json_encode(array('code' => 115, 'message' => $message));
                // exit;
            }
            else
            {
                $cover_map = preg_replace('/.*\//', '', $cover_map);
            }

            if ($activity_type == 1)
            {
                $circulation = $this->circulation($circulation);
                if($z_money > 0)
                {
                    $this->Verification($z_money);
                }
                $money = 0;
                $discount = 0;
            }
            else if ($activity_type == 2)
            {
                $circulation = $this->circulation($circulation);
                if ($money == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 优惠券面值不能为空';
                    $this->coupon_set_Log($Log_content);
                    $message = Lang('coupon.3');
                    echo json_encode(array('code' => 115, 'message' => $message));
                    exit;
                }
                else
                {
                    if (is_numeric($money))
                    {
                        $money = round($money, 2);
                        if ($money <= 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 优惠券面值不能小于等于0';
                            $this->coupon_set_Log($Log_content);
                            $message = Lang('coupon.4');
                            echo json_encode(array('code' => 115, 'message' => $message));
                            exit;
                        }
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 优惠券面值请填写数字';
                        $this->coupon_set_Log($Log_content);
                        $message = Lang('coupon.5');
                        echo json_encode(array('code' => 115, 'message' => $message));
                        exit;
                    }
                }
                if($z_money > 0)
                {
                    $this->Verification($z_money, $money);
                }
                $discount = 0;
            }
            else if ($activity_type == 3)
            {
                $circulation = $this->circulation($circulation);
                if ($discount == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 折扣值不能为空';
                    $this->coupon_set_Log($Log_content);
                    $message = Lang('coupon.6');
                    echo json_encode(array('code' => 115, 'message' => $message));
                    exit;
                }
                else
                {
                    if (is_numeric($discount))
                    {
                        $discount = round($discount, 2);
                        if ($discount <= 0 || $discount >= 10)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 折扣值不能不正确';
                            $this->coupon_set_Log($Log_content);
                            $message = Lang('coupon.7');
                            echo json_encode(array('code' => 115, 'message' => $message));
                            exit;
                        }
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 折扣值请填写数字';
                        $this->coupon_set_Log($Log_content);
                        $message = Lang('coupon.8');
                        echo json_encode(array('code' => 115, 'message' => $message));
                        exit;
                    }
                }
                if($z_money > 0)
                {
                    $this->Verification($z_money,'');
                }
                $money = 0;
            }
        }

        // 检查产品标题是否重复
        $r0 = CouponActivityModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'recycle'=>0])->field('id')->select()->toArray();
        if ($r0 && count($r0) > 0)
        {
            $message = Lang('coupon.9');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }
        $product_id = '';
        $product_class_id = '';
        if ($type == 2)
        {
            if ($menu_list == '')
            {
                $message = Lang('coupon.10');
                echo json_encode(array('code' => 115, 'message' => $message));
                exit;
            }
            else
            {
                $product_id = serialize($menu_list);
            }
        }
        else if ($type == 3)
        {
            if ($class_list == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择商品分类';
                $this->coupon_set_Log($Log_content);
                $message = Lang('coupon.11');
                echo json_encode(array('code' => 115, 'message' => $message));
                exit;
            }
            else
            {
                $product_class_id = serialize($class_list);
            }
        }

        $time1 = $this->time($end_time, $time, $activity_type);
        $end_time = $time1['end_time'];
        $receive = $this->receive_limit($receive);

        if($issue_unit == 1)
        {
            $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
            $mch_id = $r_admin[0]['shop_id']; // 店铺id
        }
        if($end_time == '')
        {
            $data_sql = array('store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'activity_type'=>$activity_type,'grade_id'=>0,'money'=>$money,'discount'=>$discount,'consumption_threshold_type'=>$consumption_threshold_type,'z_money'=>$z_money,'circulation'=>$circulation,'num'=>$circulation,'receive'=>$receive,'type'=>$type,'product_class_id'=>$product_class_id,'product_id'=>$product_id,'add_time'=>$time,'status'=>1,'skip_type'=>1,'url'=>'','day'=>$day,'Instructions'=>$Instructions,'issue_unit'=>$issue_unit,'receive_type'=>$receive_type,'is_auto_push'=>$receive_type,'cover_map'=>$cover_map);
        }
        else
        {
            $data_sql = array('store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'activity_type'=>$activity_type,'grade_id'=>0,'money'=>$money,'discount'=>$discount,'consumption_threshold_type'=>$consumption_threshold_type,'z_money'=>$z_money,'circulation'=>$circulation,'num'=>$circulation,'receive'=>$receive,'end_time'=>$end_time,'type'=>$type,'product_class_id'=>$product_class_id,'product_id'=>$product_id,'add_time'=>$time,'status'=>1,'skip_type'=>1,'url'=>'','day'=>$day,'Instructions'=>$Instructions,'issue_unit'=>$issue_unit,'receive_type'=>$receive_type,'is_auto_push'=>$receive_type,'cover_map'=>$cover_map);
        }
        $r = Db::name('coupon_activity')->insertGetId($data_sql);
        if ($r)
        {
            $Jurisdiction->admin_record($store_id, $operator, '添加了优惠券ID：'.$r,1,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加优惠券活动成功！';
            $this->coupon_set_Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array('code' => 200, 'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '添加了优惠券ID：'.$r.'失败',1,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加优惠券活动失败！';
            $this->coupon_set_Log($Log_content);
            $message = Lang('coupon.12');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }
    }

    // 编辑店铺优惠券页面
    public function modify_store_coupons_page($store_id,$id)
    {
        $mch_id = 0;
        $data = array();
        $coupon_type_list = array();

        $r0 = CouponConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('limit_type,coupon_type')->select()->toArray();
        if ($r0)
        {
            $limit_type = $r0[0]['limit_type'];
            $coupon_type = explode(",", $r0[0]['coupon_type']); // 优惠券类型
            foreach ($coupon_type as $k => $v)
            {
                if ($v == '2')
                {
                    $coupon_type_list[] = array('value'=>$v,'name'=>'满减券') ;
                }
                else if ($v == '3')
                {
                    $coupon_type_list[] = array('value'=>$v,'name'=>'折扣券') ;
                }
            }

            $field_str = "id,store_id,mch_id,name,activity_type,grade_id,money,consumption_threshold_type,discount,z_money,shopping,free_mail_task,circulation,num,receive,start_time,end_time,type,product_class_id,product_id,skip_type,status,add_time,recycle,day,Instructions as instructions,is_display,issue_unit,receive_type,is_auto_push,cover_map";
            $r1 = CouponActivityModel::where('id',$id)->field($field_str)->select()->toArray();
            $r1[0]['money'] = round($r1[0]['money'],2); // 优惠券面值
            $r1[0]['discount'] = round($r1[0]['discount'],2); // 折扣值
            $r1[0]['z_money'] = round($r1[0]['z_money'],2); // 消费满多少
            $r1[0]['add_time'] = strtotime($r1[0]['add_time']);

            $r1[0]['limitCount'] = $r1[0]['receive'];
            $r1[0]['coupon_type'] = $coupon_type_list;
            $r1[0]['cover_map'] = ServerPath::getimgpath($r1[0]['cover_map'], $store_id);

            if($r1[0]['start_time'] == null)
            {
                $r1[0]['start_time'] = '';
            }
            if($r1[0]['end_time'] == null)
            {
                $r1[0]['end_time'] = '';
            }

            $type = $r1[0]['type']; // 优惠券使用范围 1：全部商品 2:指定商品 3：指定分类
            $product_class_id = $r1[0]['product_class_id']; // 活动指定商品分类id
            $product_id = $r1[0]['product_id']; // 活动指定商品id

            $product_class_name1 = '';
            $product_name = '';
            if($type == 2)
            {
                if ($product_id != '')
                {
                    $product_id = unserialize($product_id);
                    $product_id_list = explode(',', $product_id);
                    foreach ($product_id_list as $k => $v)
                    {
                        $r2 = ProductListModel::where('id',$v)->field('product_title')->select()->toArray();
                        if ($r2)
                        {
                            $product_name .= $r2[0]['product_title'] . ',';
                        }
                    }
                    $product_name = trim($product_name, ',');
                }
            }
            else
            {
                $product_class_name = unserialize($product_class_id);
                $product_class_name_list = explode(',', $product_class_name);
                
                if(count($product_class_name_list) > 0)
                {
                    foreach ($product_class_name_list as $k => $v)
                    {
                        $r3 = ProductClassModel::where('cid',$v)->field('pname')->select()->toArray();
                        if($r3)
                        {
                            $product_class_name1 .= $r3[0]['pname'] . ',';
                        }
                    }
                    $product_class_name1 = trim($product_class_name1,',');
                }
            }

            $r1[0]['product_name'] = $product_name;
            $r1[0]['product_class_name1'] = $product_class_name1;

            $data = $r1[0];
        }
        
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message, 'data' => $data));
        exit;
    }

    // 编辑店铺优惠券
    public function modify_store_coupons($data)
    {
        $Jurisdiction = new Jurisdiction();
        $time = date('Y-m-d H:i:s');

        $store_id = $data['store_id'];
        $mch_id = $data['mch_id'];
        $id = $data['id'];
        $name = $data['name'];
        $activity_type = $data['activity_type'];
        $money = $data['money'];
        $discount = $data['discount'];
        // $consumption_threshold_type = $data['consumption_threshold_type'];
        $z_money = $data['z_money'];
        if($z_money == '')
        {
            $consumption_threshold_type = 1;
        }
        else
        {
            $consumption_threshold_type = 2;
        }
        $Instructions = $data['Instructions'];
        $cover_map = $data['cover_map'];
        
        $shop_id = '';
        if(isset($data['shop_id']))
        {
            $shop_id = $data['shop_id'];
        }
        $operator_id = 0;
        if(isset($data['operator_id']))
        {
            $operator_id = $data['operator_id'];
        }
        $operator = '';
        if(isset($data['operator']))
        {
            $operator = $data['operator'];
        }
        $source = '';
        if(isset($data['source']))
        {
            $source = $data['source'];
        }

        if ($activity_type == '' || $activity_type == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择优惠券类型';
            $this->coupon_set_Log($Log_content);
            $message = Lang('coupon.1');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }
        else
        {
            if ($cover_map == '')
            {
                // $message = Lang('coupon.39');
                // echo json_encode(array('code' => 115, 'message' => $message));
                // exit;
            }
            else
            {
                $cover_map = preg_replace('/.*\//', '', $cover_map);
            }

            if ($activity_type == 2)
            {
                if ($money == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 优惠券面值不能为空';
                    $this->coupon_set_Log($Log_content);
                    $message = Lang('coupon.3');
                    echo json_encode(array('code' => 115, 'message' => $message));
                    exit;
                }
                else
                {
                    if (is_numeric($money))
                    {
                        $money = round($money, 2);
                        if ($money <= 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 优惠券面值不能小于等于0';
                            $this->coupon_set_Log($Log_content);
                            $message = Lang('coupon.4');
                            echo json_encode(array('code' => 115, 'message' => $message));
                            exit;
                        }
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 优惠券面值请填写数字';
                        $this->coupon_set_Log($Log_content);
                        $message = Lang('coupon.5');
                        echo json_encode(array('code' => 115, 'message' => $message));
                        exit;
                    }
                }
                if($z_money > 0)
                {
                    $this->Verification($z_money, $money);
                }
                $discount = 0;
            }
            else if ($activity_type == 3)
            {
                if ($discount == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 折扣值不能为空';
                    $this->coupon_set_Log($Log_content);
                    $message = Lang('coupon.6');
                    echo json_encode(array('code' => 115, 'message' => $message));
                    exit;
                }
                else
                {
                    if (is_numeric($discount))
                    {
                        $discount = round($discount, 2);
                        if ($discount <= 0 || $discount >= 10)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 折扣值不能不正确';
                            $this->coupon_set_Log($Log_content);
                            $message = Lang('coupon.7');
                            echo json_encode(array('code' => 115, 'message' => $message));
                            exit;
                        }
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 折扣值请填写数字';
                        $this->coupon_set_Log($Log_content);
                        $message = Lang('coupon.8');
                        echo json_encode(array('code' => 115, 'message' => $message));
                        exit;
                    }
                }
                if($z_money > 0)
                {
                    $this->Verification($z_money, '');
                }
                $money = 0;
            }
        }

        // 检查产品标题是否重复
        $r0 = CouponActivityModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'recycle'=>0])->where('id','<>',$id)->field('id')->select()->toArray();
        if ($r0 && count($r0) > 0)
        {
            $message = Lang('coupon.9');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }

        $r0 = CouponActivityModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id])->field('money,end_time')->select()->toArray();
        $money1 = $r0[0]['money']; // 原面值

        $sql_where_0 = array('store_id'=>$store_id,'id'=>$id,'mch_id'=>$mch_id);
        $sql_update_0 = array('name'=>$name,'money'=>$money,'discount'=>$discount,'Instructions'=>$Instructions,'add_time'=>$time,'cover_map'=>$cover_map);
        $r_0 = Db::name('coupon_activity')->where($sql_where_0)->update($sql_update_0);
        if($r_0)
        {
            $sql_where_1 = array('store_id'=>$store_id,'hid'=>$id);
            $r1_1 = CouponModel::where($sql_where_1)->field('id')->select()->toArray();
            if($r1_1)
            {
                if ($money1 != $money)
                {
                    $sql_update1_1 = array('money'=>$money);
                    $r_1 = Db::name('coupon')->where($sql_where_1)->update($sql_update1_1);
                    if(!$r_1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改优惠券活动的优惠券失败！条件参数：'.json_encode($sql_where_1).'；修改参数：' . json_encode($sql_update1_1);
                        $this->coupon_set_Log($Log_content);
                        $message = Lang('coupon.13');
                        echo json_encode(array('code' => 115, 'message' => $message));
                        exit;
                    }
                }
            }
            $Jurisdiction->admin_record($store_id, $operator, '修改了优惠券ID：'.$id,2,$source,$shop_id,$operator_id);

            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改优惠券活动id为 ' . $id . ' 成功';
            $this->coupon_set_Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array('code' => 200, 'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了优惠券ID：'.$id.'失败',2,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改优惠券活动失败！条件参数：'.json_encode($sql_where_0).'；修改参数：' . json_encode($sql_update_0);
            $this->coupon_set_Log($Log_content);
            $message = Lang('coupon.13');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }
    }

    // 查询店铺优惠券领取信息
    public function see_coupon($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $hid = $array['hid'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $list = array();

        $manNum = 0; // 总计人数
        $sql_0 = "select tt.* from (select id,row_number () over (partition by user_id) as top from lkt_coupon where store_id = '$store_id' and recycle = 0 and mch_id = '$mch_id' and free_or_not = 0 and hid = '$hid') as tt where tt.top<2 ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $manNum = count($r_0);
        }

        $autoManNum = 0; // 自动发放人数
        $sql_1 = "select tt.* from (select id,row_number () over (partition by user_id) as top from lkt_coupon where store_id = '$store_id' and recycle = 0 and mch_id = '$mch_id' and free_or_not = 1 and hid = '$hid') as tt where tt.top<2 ";
        $r_1 = Db::query($sql_1);
        if($r_1)
        {
            $autoManNum = count($r_1);
        }
        
        $condition = " a.store_id = '$store_id' and a.recycle = 0 and a.hid = '$hid' and b.mch_id = '$mch_id' ";
        $condition1 = " a.store_id = '$store_id' and a.recycle = 0 and a.hid = '$hid' and b.mch_id = '$mch_id' ";
   
        $time = date('Y-m-d H:i:s'); // 当前时间
        $total = 0; // 领取总数
        $sql0 = "select a.id from lkt_coupon as a LEFT JOIN lkt_coupon_activity as b ON a.hid = b.id left join lkt_user as d on a.user_id = d.user_id where $condition";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = count($r0);
        }

        $Number_of_recipients = 0;
        $sql1_0 = "select tt.* from (select a.id,a.hid,a.user_id,a.add_time,a.expiry_time,a.type,b.name,b.activity_type,b.money,b.discount,c.sNo,row_number () over (PARTITION BY a.user_id) as top from lkt_coupon as a LEFT JOIN lkt_coupon_activity as b ON a.hid = b.id left join lkt_order as c ON a.id = c.coupon_id left join lkt_user as d on a.user_id = d.user_id where $condition1) as tt where tt.top < 2 ";
        $r1_0 = Db::query($sql1_0);
        if($r1_0)
        {
            $Number_of_recipients = count($r1_0);
        }

        $sql1 = "select tt.* from (select a.id,a.hid,a.user_id,a.add_time,a.expiry_time,a.type,b.name,b.activity_type,b.money,b.discount,c.sNo,row_number () over (PARTITION BY a.user_id) as top from lkt_coupon as a LEFT JOIN lkt_coupon_activity as b ON a.hid = b.id left join lkt_order as c ON a.id = c.coupon_id left join lkt_user as d on a.user_id = d.user_id where $condition1) as tt where tt.top < 2 order by tt.add_time desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $id = $v['id']; // 优惠券id
                $hid = $v['hid']; // 优惠券活动id
                $userid = $v['user_id']; // 用户id
                $v['money'] = round($v['money'],2);
                $v['discount'] = round($v['discount'],2);
                if($v['expiry_time'] == null)
                {
                    $v['expiry_time'] = '';
                }
                
                $expiry_time = $v['expiry_time']; // 到期时间
                // 当前时间大于活动结束时间,优惠券已过期
                if ($expiry_time != null && $time > $expiry_time)
                {
                    $sql3 = CouponModel::where(['store_id'=>$store_id,'id'=>$id])->find();
                    $sql3->type = '3';
                    $sql3->save();
                    $v['status'] = 3;
                }

                $v['user_name'] = '';
                $v['headimgurl'] = '';
                $r5 = UserModel::where(['user_id'=>$userid])->field('user_name,headimgurl')->select()->toArray();
                if ($r5)
                {
                    $v['user_name'] = $r5[0]['user_name'];
                    $v['headimgurl'] = $r5[0]['headimgurl'];
                }
                $v['receive'] = 0;
                $r6 = CouponModel::where(['store_id'=>$store_id,'user_id'=>$userid,'hid'=>$hid])->select()->toArray();
                if($r6)
                {
                    $v['receive'] = count($r6);
                }

                $list[] = $v;
            }
        }

        $data = array('list' => $list, 'manNum' => $manNum, 'total' => $total,'autoManNum'=>$autoManNum,'Number_of_recipients'=>$Number_of_recipients);

        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message,'data' => $data));
        exit;
    }
    
    // 查询店铺优惠券使用信息
    public function mchUseRecord($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $hid = $array['hid'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $list = array();
        $total = 0;
        $manNum = 0;

        $sql_0 = "select tt.* from (select a.id,a.hid,a.user_id,a.add_time,a.expiry_time,a.type,b.name,b.activity_type,b.money,b.discount,b.receive,row_number () over (partition by a.user_id) as top from lkt_coupon as a LEFT JOIN lkt_coupon_activity as b ON a.hid = b.id left join lkt_coupon_sno as c ON a.id = c.coupon_id left join lkt_user as d on a.user_id = d.user_id where a.store_id = '$store_id' and a.recycle = 0 and b.mch_id = '$mch_id' and a.hid = '$hid' and a.type = 2) as tt where tt.top<2 ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $manNum = count($r_0);
        }
        
        $condition = " a.store_id = '$store_id' and a.recycle = 0 and a.hid = '$hid' and b.mch_id = '$mch_id' and a.type = 2 ";
   
        $time = date('Y-m-d H:i:s'); // 当前时间
        $sql0 = "select a.id from lkt_coupon as a LEFT JOIN lkt_coupon_activity as b ON a.hid = b.id LEFT JOIN lkt_coupon_sno as s ON a.id = s.coupon_id left join lkt_order as c ON s.sNo = c.sNo where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = count($r0);
        }

        $sql1 = "select s.sNo as orderNo,s.add_date as receiveDate,s.user_id from lkt_coupon as a LEFT JOIN lkt_coupon_activity as b ON a.hid = b.id LEFT JOIN lkt_coupon_sno as s ON a.id = s.coupon_id left join lkt_order as c ON s.sNo = c.sNo where $condition order by s.add_date desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $userid = $v['user_id']; // 用户id
                
                $receiveNum = 0;
                $r2 = CouponModel::where(['store_id'=>$store_id,'user_id'=>$userid,'hid'=>$hid])->select()->toArray();
                if($r2)
                {
                    $receiveNum = count($r2);
                }
                $v['receiveNum'] = $receiveNum;

                $useNum = 0;
                $r3 = CouponModel::where(['store_id'=>$store_id,'type'=>2,'hid'=>$hid])->field('count(id) as total')->select()->toArray();
                if($r3)
                {
                    $useNum = $r3[0]['total'];
                }
                $v['useNum'] = $useNum;
                
                $r_user = UserModel::where(['store_id'=>$store_id,'user_id'=>$userid])->field('user_name')->select()->toArray();
                $v['userName'] = $r_user[0]['user_name'];
                $list[] = $v;
            }
        }

        $data = array('resultList' => $list, 'manNum' => $manNum, 'total' => $total);

        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message,'data' => $data));
        exit;
    }

    // // 删除店铺优惠券活动
    // public function del_mch_coupon($store_id,$admin_name,$mch_id,$id)
    // {
    //     // 根据优惠券活动ID，删除优惠券活动
    //     $sql0_update = array('recycle'=>1);
    //     $sql0_where = array('mch_id'=>$mch_id,'id'=>$id);
    //     $r0 = Db::name('coupon_activity')->where($sql0_where)->update($sql0_update);
    //     if ($r0 > 0)
    //     {
    //         $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除优惠券活动id为 ' . $id . ' 的信息成功';
    //         $this->coupon_set_Log($Log_content);
    //         $message = Lang('Success');
    //         echo json_encode(array('code' => 200,'message' => $message));
    //         exit;
    //     }
    //     else
    //     {
    //         $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除优惠券活动失败！参数:'. json_encode($sql0_where);
    //         $this->coupon_set_Log($Log_content);
    //         $message = Lang('Busy network');
    //         echo json_encode(array('code' => 109, 'message' => $message));
    //         exit;
    //     }
    // }

    // 获取分类
    public function mch_fenlei($store_id,$mch_id)
    {
        $product_class = array();
        $product_class1 = array();
        $product_class_0 = array();
        // 查询店铺商品
        $r0_0 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'mch_status'=>2,'recycle'=>0,'active'=>1])->order('sort','desc')->field('product_class')->select()->toArray();
        if($r0_0)
        {
            foreach ($r0_0 as $k => $v)
            {
                $product_class_1 = explode('-',trim($v['product_class'],'-')); // 商品分类
                foreach ($product_class_1 as $k1 => $v1)
                {
                    if(!in_array($v1,$product_class_0) && $v1 != '')
                    { // 不存在
                        $product_class_0[] = $v1;
                    }
                }
            }
        }
        // 查询平台一级商品分类
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'sid'=>0,'recycle'=>0])->order('sort','desc')->field('cid,pname,level')->select()->toArray();
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $cid0 = $v['cid']; // 一级分类ID
                if(in_array($cid0,$product_class_0))
                { // 存在(该分类ID被店铺使用过)
                    // 根据一级分类查询二级分类
                    $r1 = ProductClassModel::where(['store_id'=>$store_id,'sid'=>$v['cid'],'recycle'=>0])->order('sort','desc')->field('cid,pname,level')->select()->toArray();
                    if ($r1)
                    {
                        $product_class[] = array('id' => $v['cid'], 'pId' => 0, 'name' => $v['pname'], 'open' => true, 'level' => $v['level']);
                        foreach ($r1 as $k1 => $v1)
                        {
                            $cid1 = $v1['cid']; // 二级分类ID
                            if(in_array($cid1,$product_class_0))
                            { // 存在(该分类ID被店铺使用过)
                                // 根据二级分类查询三级分类
                                $r2 = ProductClassModel::where(['store_id'=>$store_id,'sid'=>$v1['cid'],'recycle'=>0])->order('sort','desc')->field('cid,pname,level')->select()->toArray();
                                if ($r2)
                                {
                                    $product_class[] = array('id' => $v1['cid'], 'pId' => $v['cid'], 'name' => $v1['pname'], 'open' => true, 'level' => $v1['level']);
                                    foreach ($r2 as $k2 => $v2)
                                    {
                                        $cid2 = $v2['cid']; // 三级分类ID
                                        if(in_array($cid2,$product_class_0))
                                        { // 存在(该分类ID被店铺使用过)
                                            $product_class[] = array('id' => $v2['cid'], 'pId' => $v1['cid'], 'name' => $v2['pname'], 'level' => $v2['level']);
                                        }
                                    }
                                }
                                else
                                {
                                    $product_class[] = array('id' => $v1['cid'], 'pId' => $v['cid'], 'name' => $v1['pname'], 'level' => $v1['level']);
                                }
                            }
                        }
                    }
                    else
                    {
                        $product_class[] = array('id' => $v['cid'], 'pId' => 0, 'name' => $v['pname'], 'level' => $v['level']);
                    }
                }
            }
        }

        if (count($product_class) != 0)
        {
            foreach ($product_class as $k => $v)
            {
                $product_class1[] = $v;
            }
        }
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'data' => $product_class1,'message' => $message));
        exit;
    }

    // 商品
    public function mch_product($store_id,$mch_id,$product_title,$page)
    {
        $start = 0;
        if($page)
        {
            $start = ($page - 1) * 10;
        }
        $list = array();

        $query_content = "a.id,a.product_title,a.imgurl,a.num as stockNum,a.status,a.subtitle,a.volume,a.s_type,a.product_class,a.brand_id,a.freight,a.keyword,a.mch_id,c.id as cid,min(c.price) over (partition by c.pid) as price,c.yprice,c.img,c.num,c.attribute,row_number () over (partition by c.pid) as top,m.id as mchId,m.name as mch_name,m.logo,m.is_open";
        if ($product_title != '')
        {
            $product_title_00 = Tools::FuzzyQueryConcatenation($product_title);
            $query_criteria = "a.store_id = '$store_id' and a.status = 2 and a.mch_status = 2 and a.recycle = 0 and a.active = 1 and c.recycle = 0 and a.mch_id = '$mch_id' and a.is_presell = 0 and a.product_title like $product_title_00 ";
        }
        else
        {
            $query_criteria = "a.store_id = '$store_id' and a.status = 2 and a.mch_status = 2 and a.recycle = 0 and a.active = 1 and c.recycle = 0 and a.mch_id = '$mch_id' and a.is_presell = 0 ";
        }

        $sql0 = "select tt.* from (select $query_content from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where $query_criteria order by a.class_sort desc,a.upper_shelf_time desc) as tt where tt.top<2 LIMIT $start,10";
        $r0 = Db::query($sql0);
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $pid = $v['id'];
                $v['pid'] = $v['id'];
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id);
                $v['price'] = round($v['price'],2);
                $v['yprice'] = round($v['yprice'],2);
                $list[] = $v;
            }
        }
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'data' => $list,'message' => $message));
        exit;
    }

    // 验证发行量(添加)
    public function circulation($circulation)
    {
        if ($circulation == '')
        {
            $circulation = 999999999;
        }
        else
        {
            if (!is_numeric($circulation) || strpos($circulation, ".") !== false)
            {
                $message = Lang('coupon.15');
                echo json_encode(array('code' => 115, 'message' => $message));
                exit;
            }
            else
            {
                if ($circulation < 0)
                {
                    $message = Lang('coupon.16');
                    echo json_encode(array('code' => 115, 'message' => $message));
                    exit;
                }
                else if ($circulation == 0)
                {
                    $circulation = 999999999;
                }
            }
        }
        return $circulation;
    }

    // 验证使用门槛
    public function Verification($z_money, $money = '')
    {
        if ($money == '')
        {
            if ($z_money == '')
            {
                $message = Lang('coupon.18');
                echo json_encode(array('code' => 115, 'message' => $message));
                exit;
            }
            else
            {
                if (is_numeric($z_money))
                {
                    $z_money = round($z_money, 2);
                    if ($z_money < 0)
                    {
                        $message = Lang('coupon.19');
                        echo json_encode(array('code' => 115, 'message' => $message));
                        exit;
                    }
                }
                else
                {
                    $message = Lang('coupon.20');
                    echo json_encode(array('code' => 115, 'message' => $message));
                    exit;
                }
            }
        }
        else
        {
            if ($z_money == '')
            {
                $message = Lang('coupon.18');
                echo json_encode(array('code' => 115, 'message' => $message));
                exit;
            }
            else
            {
                if (is_numeric($z_money))
                {
                    $z_money = round($z_money, 2);
                    if ($z_money < 0 || $z_money != 0 && $z_money <= $money)
                    {
                        $message = Lang('coupon.19');
                        echo json_encode(array('code' => 115, 'message' => $message));
                        exit;
                    }
                }
                else
                {
                    $message = Lang('coupon.20');
                    echo json_encode(array('code' => 115, 'message' => $message));
                    exit;
                }
            }
        }
    }

    // 验证时间
    public function time($end_time, $time, $activity_type)
    {
        if ($activity_type == 4)
        {
            $start_time = $time;
            $end_time = date('Y-m-d H:i:s', strtotime('+10 year', strtotime($time)));
        }
        else
        {
            if ($end_time == '')
            {
                
            }
            else
            {
                // if ($end_time == date("Y-m-d 00:00:00", strtotime($end_time)))
                // {
                //     $end_time = date("Y-m-d 23:59:59", strtotime($end_time));
                // }
                // else
                // {
                //     $end_time = $end_time;
                // }
                
                if ($time >= $end_time)
                {
                    $message = Lang('coupon.24');
                    echo json_encode(array('code' => 115, 'message' => $message));
                    exit;
                }
                else 
                {
                    if (date("Y-m-d", strtotime($time)) == date("Y-m-d", strtotime($end_time)))
                    {
                        // $end_time = date("Y-m-d 00:00:00", strtotime("+1 day",strtotime($end_time)));
                        $message = Lang('coupon.37');
                        echo json_encode(array('code' => 115, 'message' => $message));
                        exit;
                    }
                }
            }
        }
        $time = array('end_time' => $end_time);
        return $time;
    }

    // 验证领取限制
    public function receive_limit($receive)
    {
        if ($receive == '')
        {
            $message = Lang('coupon.25');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }
        else
        {
            if (!is_numeric($receive) || strpos($receive, ".") !== false)
            {
                $message = Lang('coupon.26');
                echo json_encode(array('code' => 115, 'message' => $message));
                exit;
            }
            else
            {
                if ($receive <= 0)
                {
                    $message = Lang('coupon.27');
                    echo json_encode(array('code' => 115, 'message' => $message));
                    exit;
                }
            }
        }
        return $receive;
    }

    // 是否显示
    public function is_display($array)
    {
        $Jurisdiction = new Jurisdiction();

        $store_id = $array['store_id'];
        $coupon_activity_id = $array['coupon_activity_id'];

        $shop_id = '';
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }
        $operator_id = 0;
        if(isset($data['operator_id']))
        {
            $operator_id = $data['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = '';
        if(isset($array['source']))
        {
            $source = $array['source'];
        }

        $r = CouponActivityModel::where(['store_id'=>$store_id,'id'=>$coupon_activity_id])->field('is_display')->select()->toArray();
        if($r[0]['is_display'] == 1)
        {
            $is_display = 0;
        }
        else
        {
            $is_display = 1;
        }

        $sql_update = array('is_display'=>$is_display);
        $sql_where = array('id'=>$coupon_activity_id);
        $r0 = Db::name('coupon_activity')->where($sql_where)->update($sql_update);
        if($r0 > 0)
        {
            $Jurisdiction->admin_record($store_id, $operator, '将优惠券ID：'.$coupon_activity_id.'，进行了是否显示操作',2,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . '修改优惠券成功！';
            $this->coupon_set_Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array('code' => 200,'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '将优惠券ID：'.$coupon_activity_id.'，进行了是否显示操作失败',2,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . '修改优惠券失败！';
            $this->coupon_set_Log($Log_content);
            $message = Lang('coupon.13');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
    }

    // 获取用户信息
    public function get_user($array)
    {
        $store_id = $array['store_id'];
        $coupon_activity_str = $array['coupon_activity_str'];
        $name = $array['name'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $condition = " store_id = $store_id and user_id not in (select main_id from lkt_user_authority where type=1) ";

        $user_id_str = '';
        $r0 = CouponModel::where(['store_id'=>$store_id,'free_or_not'=>1,'hid'=>$coupon_activity_str])->field('user_id')->select()->toArray();
        if($r0)
        {
            foreach ($r0 as $k0 => $v)
            {
                $user_id_str .= "'" . $v['user_id'] . "',";
            }
            $user_id_str = trim($user_id_str,',');
        }
        
        if($user_id_str != '')
        {
            $condition .= " and user_id not in ($user_id_str) ";
        }
        if($name)
        {
            $name_00 = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and (user_id like $name_00 or user_name like $name_00 or mobile like $name_00) ";
        }
        $total = 0;
        // 查询用户总数
        $sql2 = "select count(id) as total from lkt_user where $condition ";
        $r2 = Db::query($sql2);
        if($r2)
        {
            $total = $r2[0]['total'];
        }

        $user_lsit = array();
        
        $sql3 = "select id,user_id,user_name,zhanghao,mobile,grade from lkt_user where $condition order by Register_data desc LIMIT $start,$pagesize";
        $r3 = Db::query($sql3);
        if($r3)
        {
            foreach ($r3 as $k3 => $v3)
            {
                $v3['grade_name'] = '普通会员';
                $user_lsit[] = $v3;
            }
        }
        $data = array('total'=>$total,'userList'=>$user_lsit);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'data' => $data,'message' => $message));
        exit;
    }

    // 赠送优惠券
    public function gift_user($array)
    {
        $Jurisdiction = new Jurisdiction();
        $store_id = $array['store_id'];
        $coupon_activity_str = $array['coupon_activity_str'];
        $user_str = $array['user_str'];

        $shop_id = '';
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }
        $operator_id = 0;
        if(isset($data['operator_id']))
        {
            $operator_id = $data['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = '';
        if(isset($array['source']))
        {
            $source = $array['source'];
        }

        $time = date("Y-m-d H:i:s");
        Db::startTrans();

        if($user_str == '')
        {
            $message = Lang('coupon.35');
            echo json_encode(array('code' => 115,'message' => $message));
            exit;
        }
        $user_list = explode(',',$user_str);
        $num = count($user_list);

        // 根据商城ID、优惠券ID、活动状态，查询优惠券活动
        $r0 = CouponActivityModel::where(['store_id'=>$store_id,'recycle'=>0,'id'=>$coupon_activity_str])->select()->toArray();
        if ($r0)
        {
            $id = $r0[0]['id']; // 优惠券ID
            $activity_type = $r0[0]['activity_type']; // 优惠券类型
            $mch_id = $r0[0]['mch_id']; // 店铺ID
            $s_num = $r0[0]['num']; // 剩余数量
            $end_time = $r0[0]['end_time']; // 活动结束时间
            if($s_num < $num)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '优惠券剩余数量不足！';
                $this->couponLog($id, $Log_content);
                $message = Lang('coupon.36');
                echo json_encode(array('code' => 115,'message' => $message));
                exit;
            }

            foreach ($user_list as $k_user => $v_user)
            {
                // 根据用户ID,查询user_id
                $r1 = UserModel::where(['store_id'=>$store_id,'user_id'=>$v_user])->field('user_id,mobile')->select()->toArray();
                $user_id = $r1[0]['user_id']; // 用户user_id
                $mobile = $r1[0]['mobile']; // 手机号
                // 在优惠券表里添加一条数据
                $sql2 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'user_id'=>$user_id,'add_time'=>$time,'expiry_time'=>$end_time,'hid'=>$id,'free_or_not'=>1);
                $r2 = Db::name('coupon')->insertGetId($sql2);
                if ($r2 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '赠送' . $user_id . '优惠券活动ID为' . $id . '时,添加用户优惠券失败！';
                    $this->couponLog($id, $Log_content);
                    Db::rollback();
                    $message = Lang('Unknown error');
                    echo json_encode(array('code' => 115,'message' => $message));
                    exit;
                }
                // 在赠送优惠券记录表里添加一条数据
                $sql3 = array('store_id'=>$store_id,'coupon_activity_id'=>$id,'coupon_id'=>$r2,'user_id'=>$user_id,'mobile'=>$mobile,'activity_type'=>$activity_type,'add_date'=>$time);
                $r3 = Db::name('coupon_presentation_record')->insert($sql3);
                if ($r3 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '赠送' . $user_id . '优惠券活动ID为' . $id . '时,添加赠送优惠券记录失败！';
                    $this->couponLog($id, $Log_content);
                    Db::rollback();
                    $message = Lang('Unknown error');
                    echo json_encode(array('code' => 115,'message' => $message));
                    exit;
                }

                $Jurisdiction->admin_record($store_id, $operator, '将优惠券ID：'.$coupon_activity_str.' 赠送给了用户ID：'.$v_user,2,$source,$shop_id,$operator_id);
            }
            // 修改优惠券活动剩余库存
            $sql0_0_update = array('num'=>Db::raw('num-'.$num));
            $sql0_0_where = array('store_id'=>$store_id,'id'=>$id,'recycle'=>0);
            $r0_0 = Db::name('coupon_activity')->where($sql0_0_where)->update($sql0_0_update);
            if($r0_0 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '赠送优惠券时,修改优惠券活动失败！sql:' . $sql0_0;
                $this->couponLog($id, $Log_content);
                Db::rollback();
                $message = Lang('Unknown error');
                echo json_encode(array('code' => 115,'message' => $message));
                exit;
            }
        }

        $Log_content = __METHOD__ . '->' . __LINE__ . '赠送优惠券成功！';
        $this->coupon_set_Log($Log_content);
        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 领取记录
    public function seeCouponLogger($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $hid = $array['hid'];
        $keyWord = $array['keyWord'];
        $type = $array['type'];
        $status = $array['status'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $time = date('Y-m-d H:i:s'); // 当前时间

        $exportType = '';
        if(isset($array['exportType']))
        {
            $exportType = $array['exportType'];
        }

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $list = array();
        $condition = " a.store_id = '$store_id' and a.free_or_not = 0 and a.hid = '$hid' and b.mch_id in ($mch_id)  ";
        if ($keyWord != '')
        {
            $keyWord_00 = Tools::FuzzyQueryConcatenation($keyWord);
            $condition .= " and (d.user_name like $keyWord_00 or c.sNo like $keyWord_00 or a.id = '$keyWord')";
        }
        
        if ($type != '')
        {
            $condition .= " and b.activity_type = '$type' ";
        }
        
        if ($status != '')
        {
            $condition .= " and a.type = '$status'";
        }
        $manNum = 0;
        $person = 0;
        $sql_0 = "select tt.* from (select a.id,a.hid,a.user_id,a.add_time,a.expiry_time,a.type,b.name,b.activity_type,b.money,b.discount,b.receive,row_number () over (partition by a.user_id) as top from lkt_coupon as a LEFT JOIN lkt_coupon_activity as b ON a.hid = b.id left join lkt_coupon_sno as c ON a.id = c.coupon_id left join lkt_user as d on a.user_id = d.user_id where $condition) as tt where tt.top<2 ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $person = count($r_0);
        }
        
        $sql_1 = "select tt.* from (select a.id,row_number () over (partition by a.id) as top from lkt_coupon as a LEFT JOIN lkt_coupon_activity as b ON a.hid = b.id left join lkt_coupon_sno as c ON a.id = c.coupon_id left join lkt_user as d on a.user_id = d.user_id where $condition) as tt where tt.top<2 ";
        $r_1 = Db::query($sql_1);
        if($r_1)
        {
            $manNum = count($r_1);
        }
        
        $total = 0;
        $time = date('Y-m-d H:i:s'); // 当前时间
        $sql0 = "select a.id from lkt_coupon as a LEFT JOIN lkt_coupon_activity as b ON a.hid = b.id left join lkt_coupon_sno as c ON a.id = c.coupon_id left join lkt_user as d on a.user_id = d.user_id where $condition order by a.add_time desc";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = count($r0);
        }

        $sql1 = "select a.id,a.hid,a.user_id,a.add_time,a.expiry_time,a.type,a.status,b.name,b.activity_type,b.money,b.discount,b.receive from lkt_coupon as a LEFT JOIN lkt_coupon_activity as b ON a.hid = b.id left join lkt_coupon_sno as c ON a.id = c.coupon_id left join lkt_user as d on a.user_id = d.user_id where $condition order by a.add_time desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $activity_type = $v['activity_type'];
                $id = $v['id']; // 优惠券id
                $userid = $v['user_id']; // 用户id
                $expiry_time = $v['expiry_time']; // 到期时间

                $v['money'] = round($v['money'],2);
                $v['discount'] = round($v['discount'],2);

                // 当前时间大于活动结束时间,优惠券已过期
                if ($time > $expiry_time)
                {
                    $sql2_update = array('status'=>3);
                    $sql2_where = array('store_id'=>$store_id,'id'=>$id);
                    Db::name('coupon_activity')->where($sql2_where)->update($sql2_update);
                    $v['status'] = 3;
                }

                $v['user_name'] = '';
                $v['headimgurl'] = '';
                $r4 = UserModel::where(['user_id'=>$userid])->field('user_name,headimgurl')->select()->toArray();
                if ($r4)
                {
                    $v['user_name'] = $r4[0]['user_name'];
                    $v['headimgurl'] = $r4[0]['headimgurl'];
                }
                
                $v['typeName'] = '';
                if($activity_type == 1)
                {
                    $v['typeName'] = '免邮券';
                }
                else if($activity_type == 2)
                {
                    $v['typeName'] = '满减券';
                }
                else if($activity_type == 3)
                {
                    $v['typeName'] = '折扣券';
                }

                $v['orderList'] = array(); // 列表显示订单号
                if($v['type'] != 0)
                {
                    // 根据优惠券ID，查询关联订单号
                    $r2 = CouponSnoModel::where(['store_id'=>$store_id,'coupon_id'=>$id,'recycle'=>0])->field('sNo')->select()->toArray();
                    if($r2)
                    {
                        if(count($r2) > 1)
                        { // 拆过订单
                            foreach ($r2 as $k2 => $v2)
                            {
                                $sNo2 = $v2['sNo']; // 关联订单号
                                // 根据订单号，查询子订单
                                $r3 = OrderModel::where(['store_id'=>$store_id,'p_sNo'=>$sNo2])->field('id')->select()->toArray();
                                if($r3)
                                { // 存在，有子订单

                                }
                                else
                                { // 不存在，表示该订单号是子订单
                                    $v['orderList'][] = $v2['sNo']; // 列表显示订单号
                                }
                            }
                        }
                        else
                        { // 一个订单
                            $v['orderList'][] = $r2[0]['sNo']; // 列表显示订单号
                        }
                    }
                }
                $list[] = $v;
            }
        }

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '优惠券ID',
                1 => '用户ID',
                2 => '用户名称',
                3 => '优惠券类型',
                4 => '面值/折扣',
                5 => '状态',
                6 => '关联订单号',
                7 => '领取时间',
                8 => '过期时间',
            );
            $exportExcel_list = array();

            if ($list != array())
            {
                foreach ($list as $k => $v)
                {
                    if($v['type'] == 1)
                    {
                        $v['type_name'] = '使用中';
                    }
                    else if($v['type'] == 2)
                    {
                        $v['type_name'] = '已使用';
                    }
                    else if($v['type'] == 3)
                    {
                        $v['type_name'] = '已过期';
                    }
                    else 
                    {
                        $v['type_name'] = '未使用';
                    }
                    $v['sNo'] = "";
                    if($v['orderList'] != array())
                    {
                        $v['sNo'] = implode(',',$v['orderList']);
                    }
                    $exportExcel_list[] = array(
                        $v['id'],
                        $v['user_id'],
                        $v['user_name'],
                        $v['typeName'],
                        $v['money'],
                        $v['type_name'],
                        $v['sNo'],
                        $v['add_time'],
                        $v['expiry_time']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '领取记录列表');
                exit;
            }
            else
            {
                ExcelUtils::exportExcel($exportExcel_list, $titles, '领取记录列表');
                exit;
            }
        }

        $data = array('list' => $list, 'total' => $total, 'manNum' => $manNum, 'person' => $person);
        $message = Lang('Success');
        echo json_encode(array('code' => '200', 'message' => $message, 'data' => $data));
        exit;
    }

    // 赠送记录
    public function seeGiveCouponLogger($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $coupon_activity_id = $array['coupon_activity_id'];
        $keyWord = $array['keyWord'];
        $type = $array['type'];
        $state = $array['state'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $time = date('Y-m-d H:i:s'); // 当前时间

        $exportType = '';
        if(isset($array['exportType']))
        {
            $exportType = $array['exportType'];
        }

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $con = " a.store_id = '$store_id' and a.coupon_activity_id = '$coupon_activity_id' ";
        if($keyWord)
        {
            $keyWord_00 = Tools::FuzzyQueryConcatenation($keyWord);
            $con .= " and (d.user_name like $keyWord_00 or c.sNo like $keyWord_00 or a.id = '$keyWord') ";
        }
        
        if($type)
        {
            $con .= " and a.activity_type = '$type' ";
        }
        
        if($state != '')
        {
            $con .= " and b.type = '$state' ";
        }

        $list = array();
        $total = 0;
        $sql0 = "select count(b.id) as total from lkt_coupon_presentation_record as a left join lkt_coupon as b on a.coupon_id = b.id left join lkt_coupon_sno as c on a.coupon_id = c.coupon_id left join lkt_user as d on a.user_id = d.user_id where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $str = "a.id,a.coupon_id,a.user_id,a.mobile,a.activity_type,a.add_date,b.expiry_time,b.type,d.user_name,row_number () over (partition by b.id) as top";
        $sql1 = "select tt.* from (select $str from lkt_coupon_presentation_record as a left join lkt_coupon as b on a.coupon_id = b.id left join lkt_coupon_sno as c on a.coupon_id = c.coupon_id left join lkt_user as d on a.user_id = d.user_id where $con) as tt where tt.top<2 order by tt.add_date desc ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $activity_type = $v['activity_type'];
                $coupon_id = $v['coupon_id'];
                $v['orderList'] = array(); // 列表显示订单号
                if($v['type'] != 0)
                {
                    // 根据优惠券ID，查询关联订单号
                    $r2 = CouponSnoModel::where(['store_id'=>$store_id,'coupon_id'=>$coupon_id,'recycle'=>0])->field('sNo')->select()->toArray();
                    if($r2)
                    {
                        if(count($r2) > 1)
                        { // 拆过订单
                            foreach ($r2 as $k2 => $v2)
                            {
                                $sNo2 = $v2->sNo; // 关联订单号
                                // 根据订单号，查询子订单
                                $r3 = OrderModel::where(['store_id'=>$store_id,'p_sNo'=>$sNo2])->field('id')->select()->toArray();
                                if($r3)
                                { // 存在，有子订单
                                    
                                }
                                else
                                { // 不存在，表示该订单号是子订单
                                    $v['orderList'][] = $v2['sNo']; // 列表显示订单号
                                }
                            }
                        }
                        else
                        { // 一个订单
                            $v['orderList'][] = $r2[0]['sNo']; // 列表显示订单号
                        }
                    }
                }
                
                $v['money'] = 0;
                $v['discount'] = 0;
                $v['z_money'] = 0;
                $r4 = CouponActivityModel::where(['store_id'=>$store_id,'id'=>$coupon_activity_id])->field('money,discount,z_money')->select()->toArray();
                if($r4)
                {
                    $v['money'] = round($r4[0]['money'],2);
                    $v['discount'] = round($r4[0]['discount'],2);
                    $v['z_money'] = round($r4[0]['z_money'],2);
                }
                $v['typeName'] = '';
                if($activity_type == 1)
                {
                    $v['typeName'] = '免邮券';
                }
                else if($activity_type == 2)
                {
                    $v['typeName'] = '满减券';
                }
                else if($activity_type == 3)
                {
                    $v['typeName'] = '折扣券';
                }
                $list[] = $v;
            }
        }
        $data = array('list' => $list, 'total' => $total);
        $message = Lang('Success');
        echo json_encode(array('code' => '200', 'message' => $message, 'data' => $data));
        exit;
    }

    // 删除优惠券
    public function delCoupon($array)
    {
        $Jurisdiction = new Jurisdiction();
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id'];

        $shop_id = '';
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }
        $operator_id = 0;
        if(isset($data['operator_id']))
        {
            $operator_id = $data['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = '';
        if(isset($array['source']))
        {
            $source = $array['source'];
        }

        // 根据优惠券活动ID，删除优惠券活动
        $sql0 = "update lkt_coupon_activity set recycle = 1 where id = '$id' and mch_id in ($mch_id) ";
        $r0 = Db::execute($sql0);
        if ($r0 > 0)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了优惠券ID：'.$id,2,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除优惠券活动id为 ' . $id . ' 的信息成功';
            $this->coupon_set_Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array('code' => '200', 'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了优惠券ID：'.$id.'失败',2,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除优惠券活动失败！参数:' . $id;
            $this->coupon_set_Log($Log_content);
            $message = Lang('coupon.34');
            echo json_encode(array('code' => '109', 'message' => $message));
            exit;
        }
    }

    // 批量删除优惠券
    public function batchDel($array)
    {
        $Jurisdiction = new Jurisdiction();
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id'];
        
        $shop_id = '';
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }
        $operator_id = 0;
        if(isset($data['operator_id']))
        {
            $operator_id = $data['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = '';
        if(isset($array['source']))
        {
            $source = $array['source'];
        }

        // 根据优惠券活动ID，删除优惠券活动
        $sql0 = "update lkt_coupon_activity set recycle = 1 where id in ($id) and mch_id in ($mch_id) ";
        $r0 = Db::execute($sql0);
        if ($r0 > 0)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了优惠券ID：'.$id,3,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除优惠券活动id为 ' . $id . ' 的信息成功';
            $this->coupon_set_Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array('code' => '200', 'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了优惠券ID：'.$id.'失败',2,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除优惠券活动失败！参数:'.$id;
            $this->coupon_set_Log($Log_content);
            $message = Lang('coupon.34');
            echo json_encode(array('code' => '109', 'message' => $message));
            exit;
        }
    }

    // 店铺注销
    public function cancellation_shop($store_id, $mch_id)
    {
        Db::startTrans();
        
        $r0 = Db::name('coupon_activity')->where(['store_id' => $store_id,'mch_id'=>$mch_id])->update(['recycle'=>1]);
        if($r0 == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 店铺注销时，删除优惠券活动失败！sql:'.$sql0;
            $this->coupon_set_Log($Log_content);
            Db::rollback();
            $message = Lang('Busy network');
            echo json_encode(array('code' => 115,'message' => $message));
            exit;
        }

        $r1 = Db::name('coupon')->where(['store_id' => $store_id,'mch_id'=>$mch_id])->update(['recycle'=>1]);
        if($r1 == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 店铺注销时，删除优惠券失败！sql:'.$sql1;
            $this->coupon_set_Log($Log_content);
            Db::rollback();
            $message = Lang('Busy network');
            echo json_encode(array('code' => 115,'message' => $message));
            exit;
        }
        Db::commit();
        return;
    }

    // 修改优惠券状态
    public function update_coupon($store_id, $user_id, $coupon_id,$type)
    {
        Db::startTrans();

        $coupon_id_list = explode(',', $coupon_id); // 优惠券ID数组
        foreach ($coupon_id_list as $k => $v)
        {
            if($v != 0)
            {
                $sql0 = array('type'=>$type);
                $sql0_where = array('store_id'=>$store_id,'user_id'=>$user_id,'id'=>$v);
                $r0 = Db::name('coupon')->where($sql0_where)->update($sql0);
                if ($r0 == -1)
                {
                    //回滚删除已经创建的订单
                    Db::rollback();
                    return 2;
                    exit;
                }
            }
        }
        Db::commit();
        return 1;
        exit;
    }

    // 添加关联订单信息
    public function coupon_sno($store_id, $user_id, $coupon_id,$sNo,$type)
    {
        $coupon_id_list = explode(',', $coupon_id); // 优惠券ID数组
        foreach ($coupon_id_list as $k => $v)
        {
            if($v != 0)
            {
                if($type == 'add')
                {
                    $sql0 = new CouponSnoModel();
                    $sql0->store_id = $store_id;
                    $sql0->user_id = $user_id;
                    $sql0->coupon_id = $v;
                    $sql0->sNo = $sNo;
                    $sql0->recycle =0;
                    $sql0->add_date = date('Y-m-d H:i:s');
                    $r0 = $sql0->save();
                    if($r0 <= 0)
                    {
                        $coupon_Log_content = __METHOD__ . ":" . __LINE__.'添加优惠券关联信息失败！sql:' . $sql0 ;
                        $this->coupon_set_Log($coupon_Log_content);
                        return 2;
                        exit;
                    }
                }
                else
                {
                    $r0 = CouponSnoModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'coupon_id'=>$v])->update(['recycle'=>1]);
                    if($r0 <= 0)
                    {
                        $coupon_Log_content = __METHOD__ . ":" . __LINE__.'修改优惠券关联信息失败！sql:' . Db::getLastSQl() ;
                        $this->coupon_set_Log($coupon_Log_content);
                        return 2;
                        exit;
                    }
                }
            }
        }
        $coupon_Log_content = '添加优惠券关联信息成功！' ;
        $this->coupon_set_Log($coupon_Log_content);
        return 1;
        exit;
    }

    // 拆订单
    public function leave_Settlement($store_id, $user_id, $sNo)
    {
        $coupon_list = array();
        $list = array();
        $r0 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id])->select()->toArray();
        if($r0)
        {
            $mch_id = $r0[0]['mch_id']; // 店铺ID字符串
            $coupon_id = $r0[0]['coupon_id']; // 优惠券ID
            $grade_rate = round($r0[0]['grade_rate'] * 0.1, 2); // 会员折扣

            $mch_id = substr($mch_id, 1, -1); // 去掉字符串第一个字符和最后一个字符
            $shop_id = explode(',', $mch_id); // 店铺id字符串
            if(count($shop_id) > 1)
            { // 跨店铺
                if($coupon_id != '' && $coupon_id != '0')
                {
                    $coupon_id_list = explode(',', $coupon_id); // 优惠券ID数组
                    $coupon_id_num = count($coupon_id_list) - 1; // 最后一个优惠券ID健名
                    $coupon_id0 = $coupon_id_list[$coupon_id_num]; // 最后一个优惠券ID(平台发行优惠券)
                    $sum_of_subtotal = 0; // 小计之和

                    foreach ($coupon_id_list as $k => $v)
                    {
                        $preferential_amount = 0; // 优惠金额
                        $mch_spz_price = 0; // 店铺商品总价

                        if($coupon_id_num != $k)
                        { // 当前健名 != 最后一个优惠券ID健名 （店铺发行优惠券）
                            if($v != 0)
                            {
                                // 根据商城ID、用户ID、店铺优惠券ID，查询优惠券信息
                                $sql1 = "select b.mch_id,b.activity_type,b.type,b.money,b.discount,b.product_class_id,b.product_id from lkt_coupon as a left join lkt_coupon_activity as b on a.hid = b.id where a.store_id = '$store_id' and a.user_id = '$user_id' and a.id = '$v' ";
                                $r1 = Db::query($sql1);
                                if($r1)
                                {
                                    $mch_id = $r1[0]['mch_id']; // 店铺ID
                                    $activity_type = $r1[0]['activity_type']; // 优惠券类型
                                    $type = $r1[0]['type']; // 优惠券类型
                                    $money = $r1[0]['money']; // 面值
                                    $discount = floatval($r1[0]['discount']); // 折扣值
                                    $product_class_id = $r1[0]['product_class_id']; // 分类id
                                    $product_id1 = $r1[0]['product_id']; // 商品id

                                    $product_id1 = unserialize($product_id1);
                                    $product_id1 = rtrim($product_id1, ','); // 去除字符串最后一个逗号
                                    $product_id_list = explode(",", $product_id1); // 字符串转数组

                                    $product_class_id = unserialize($product_class_id);
                                    $product_class_id = rtrim($product_class_id, ','); // 去除字符串最后一个逗号
                                    $product_class_list = explode(",", $product_class_id); // 字符串转数组

                                    $accord_with = 0; // 符合优惠券的商品总价

                                    // 根据商城ID、订单号、店铺ID，查询订单详情
                                    $sql2 = "select a.id,a.p_id,a.p_price,a.num,a.freight,b.product_class from lkt_order_details as a left join lkt_product_list as b on a.p_id = b.id where a.store_id = '$store_id' and a.r_sNo = '$sNo' and b.mch_id = '$mch_id'";
                                    $r2 = Db::query($sql2);
                                    if($r2)
                                    {
                                        foreach ($r2 as $k2 => $v2)
                                        {
                                            if($type == 1)
                                            {
                                                $accord_with += ($v2['p_price'] * $grade_rate) * $v2['num']; // 符合优惠券的商品总价
                                            }
                                            else if($type == 2)
                                            {
                                                if(in_array($v2['p_id'],$product_id_list))
                                                {
                                                    $accord_with += ($v2['p_price'] * $grade_rate) * $v2['num']; // 符合优惠券的商品总价
                                                }
                                            }
                                            else if($type == 3)
                                            {
                                                foreach ($product_class_list as $k_0 => $v_0)
                                                {
                                                    if (strpos($v2['product_class'], $v_0) !== false)
                                                    { // 符合
                                                        $accord_with += ($v2['p_price'] * $grade_rate) * $v2['num']; // 符合优惠券的商品总价
                                                    }
                                                }
                                            }

                                            $mch_spz_price += ($v2['p_price'] * $grade_rate) * $v2['num']; // 符合优惠券的商品总价
                                        }
                                    }
                                    $zong_money = round(floor($accord_with * $discount*100)/100/10,2); // 打完折扣后的金额

                                    if($zong_money == 0)
                                    { // 不是打折优惠
                                        $zong_money1 = 0;
                                    }
                                    else
                                    { // 是打折优惠
                                        $zong_money1 = $accord_with - $zong_money;
                                    }

                                    if($activity_type == 2)
                                    { // 满减券
                                        $preferential_amount = round($money,2); // 店铺优惠金额
                                    }
                                    else if($activity_type == 3)
                                    { // 折扣券
                                        $preferential_amount = round($zong_money1,2); // 店铺优惠金额
                                    }
                                }
                            }
                            else
                            {
                                $mch_id = $shop_id[$k];
                                $sql2 = "select a.id,a.p_id,a.p_price,a.num,a.freight from lkt_order_details as a left join lkt_product_list as b on a.p_id = b.id where a.store_id = '$store_id' and a.r_sNo = '$sNo' and b.mch_id = " . $shop_id[$k] ;
                                $r2 = Db::query($sql2);
                                if($r2)
                                {
                                    foreach ($r2 as $k2 => $v2)
                                    {
                                        $mch_spz_price += ($v2['p_price'] * $grade_rate) * $v2['num']; // 符合优惠券的商品总价
                                    }
                                }
                            }
                            $list[$k]['coupon_id'] = $v; // 优惠ID
                            $list[$k]['mch_id'] = $mch_id; // 店铺ID
                            $list[$k]['preferential_amount'] = $preferential_amount; // 店铺优惠金额
                            $list[$k]['subtotal'] = $mch_spz_price - $preferential_amount; // 店铺小计
                            $sum_of_subtotal += $mch_spz_price - $preferential_amount; // 小计之和
                        }
                    }

                    $platform_preferential_amount = 0;
                    if($coupon_id0 != 0)
                    {
                        // 根据商城ID、用户ID、平台优惠券ID，查询优惠券信息
                        $sql2 = "select b.activity_type,b.money,b.discount from lkt_coupon as a left join lkt_coupon_activity as b on a.hid = b.id where a.store_id = '$store_id' and a.user_id = '$user_id' and a.id = '$coupon_id0' ";
                        $r2 = Db::query($sql2);
                        if($r2)
                        {
                            $activity_type = $r2[0]['activity_type']; // 优惠券类型
                            $money = $r2[0]['money']; // 面值
                            $discount = floatval($r2[0]['discount']); // 折扣值
                            $zong_money = round(floor($sum_of_subtotal * $discount*100)/100/10,2); // 打完折扣后的金额
                            if($zong_money == 0)
                            { // 不是打折优惠
                                $zong_money1 = 0;
                            }
                            else
                            { // 是打折优惠
                                $zong_money1 = $sum_of_subtotal - $zong_money;
                            }

                            if($activity_type == 2)
                            { // 满减券
                                $platform_preferential_amount = round($money,2);
                            }
                            else if($activity_type == 3)
                            { // 折扣券
                                $platform_preferential_amount = round($zong_money1,2);
                            }

                            if($platform_preferential_amount > $sum_of_subtotal)
                            { // 优惠金额 > 店铺金额之和
                                $platform_preferential_amount = $sum_of_subtotal;
                            }
                        }
                    }

                    foreach ($list as $k => $v)
                    {
                        $coupon_list[$k]['coupon_id'] = $v['coupon_id'] . ',' . $coupon_id0;
                        $coupon_list[$k]['mch_id'] = $v['mch_id'];
                        $coupon_list[$k]['mah_coupon_amount'] = $v['preferential_amount'];
                        $coupon_list[$k]['preferential_amount'] = $v['preferential_amount'] + round($v['subtotal'] / $sum_of_subtotal * $platform_preferential_amount,2); // 订单优惠金额 = 店铺优惠金额+(店铺小计/小计之和*平台优化金额)
                    }
                }
            }
        }
        return $coupon_list;
    }

    // 优惠券日志
    public function couponLog($id, $Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        if ($id != 0)
        {
            $lktlog->log("app/coupon.log",$Log_content);
        }
        return;
    }

    // 优惠券日志
    public function coupon_set_Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/coupon.log",$Log_content);
        return;
    }
    
    // 进入结算页面-获取每个店铺可使用的优惠券
    public function settlement_store_coupons($store_id, $user_id, $products,$coupon_str,$canshu)
    {
        $list = array();
        $preferential_amount = 0;
        $coupon_id0 = 0;
        $coupon_id_list0 = array();
        $coupon_id_list = array();
        $free_shipping = 0; // 免邮金额
        foreach ($products as $k => $v)
        {
            $mch_id = $v['shop_id']; // 店铺ID
            $mch_product_total = $v['product_total'] - $v['freight_price']; // 商品总价
            $mch_product_class = array(); // 购买商品的分类信息
            $mch_product_id = array(); // 购买商品的商品ID
            $mch_yunfei = 0; // 店铺总运费
            foreach ($v['list'] as $ke => $va) 
            {
                $v['list'][$ke]['discount'] = 0;
                $v['list'][$ke]['coupon_id'] = 0;
                $mch_product_class[] = $va['product_class']; // 购买商品的分类信息
                $mch_product_id[] = $va['pid']; // 购买商品的商品ID
                $mch_yunfei += $va['freight_price'];
            }

            $coupon_id = '0';
            if($coupon_str != '')
            {
                $coupon_id_list0 = explode(',',$coupon_str);
                $coupon_id = $coupon_id_list0[$k];
                $coupon_num = count($coupon_id_list0)-1;
                $coupon_id0 = $coupon_id_list0[$coupon_num];//优惠券ID
            }
            $coupon_id_list[$k] = $coupon_id;
            $coupon_list = $this->get_store_coupons($store_id, $user_id, $mch_id, $v['list'],$mch_product_total, $mch_product_class, $mch_product_id, $mch_yunfei, $coupon_id,$canshu);

            $products[$k]['list'] = $coupon_list['products_list'];
            $products[$k]['coupon_list'] = $coupon_list['list'];
 
            if(count($coupon_list['list']) > 0)
            {
                foreach ($coupon_list['list'] as $ke => $va)
                {
                    $shop_subtotal = $v['freight_price'];
                    if($va['coupon_status'])
                    {
                        if($va['activityType'] == 1)
                        {
                            $products[$k]['freight_price'] = 0;
                            $shop_subtotal = 0;
                            $free_shipping = $free_shipping + $mch_yunfei;
                        }
                        $coupon_id_list[$k] = $va['coupon_id'];
                        $products[$k]['shop_subtotal'] = round($shop_subtotal + $va['shop_subtotal'],2);
                        $preferential_amount = $preferential_amount + $va['money'];
                    }
                }
            }
            else
            {
                $products[$k]['shop_subtotal'] = round($mch_product_total + $v['freight_price'],2);
            }
        }

        $coupon_id_list[] = $coupon_id0;

        $list['coupon_id'] = implode(',',$coupon_id_list);
        $list['products'] = $products;
        $list['preferential_amount'] = $preferential_amount; // 店铺优惠之和
        $list['free_shipping'] = $free_shipping;
        return $list;
    }

    // 获取用户满足条件的店铺优惠券
    public function get_store_coupons($store_id, $user_id, $mch_id, $products_list,$mch_product_total, $mch_product_class, $mch_product_id, $mch_yunfei, $coupon_id,$canshu)
    {
        $access_id = '';
        $language = '';
        $time = date('Y-m-d H:i:s');  // 当前时间
        $arr = array();
        $arr1 = array();
        // 查询该用户有多少使用中的优惠券属于该店铺
        $sql0 = "select a.id,a.expiry_time from lkt_coupon as a left join lkt_coupon_activity as b on a.hid = b.id where a.store_id = '$store_id' and a.user_id = '$user_id' and b.mch_id = '$mch_id' and a.type = 1 and a.status = 0 and a.recycle = 0";
        $r0 = Db::query($sql0);
        if ($r0)
        { // 存在 使用中 的优惠券
            foreach ($r0 as $k => $v)
            { // 循环判断 优惠券是否绑定
                $id = $v['id']; // 优惠券id
                $expiry_time = $v['expiry_time']; // 优惠券到期时间
                // 根据优惠券id,查询订单表(查看优惠券是否绑定)
                $r_order = OrderModel::where(['store_id'=>$store_id,'coupon_id'=>$id])->where('status','not in','6,7')->field('id')->select()->toArray();
                if (empty($r_order))
                {
                    // 没有数据,表示优惠券没绑定
                    if ($expiry_time != null && $expiry_time <= $time)
                    { // 当前时间 >= 优惠券到期时间
                        $r_coupon = Db::name('coupon')->where('id', $id)->update(['type'=>'3']);
                        if (!$r_coupon)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '优惠券ID为' . $id . '修改为已过期失败！';
                            $this->couponLog($id, $Log_content);
                        }
                    }
                    else
                    {
                        $r_coupon = Db::name('coupon')->where('id', $id)->update(['type'=>'0']);
                        if (!$r_coupon)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '优惠券ID为' . $id . '修改为已过期失败！';
                            $this->couponLog($id, $Log_content);
                        }
                    }
                }
            }
        }

        $sql1 = "select a.id,a.expiry_time,b.name,b.activity_type,b.money,b.discount,b.z_money,b.type,b.product_class_id,b.product_id from lkt_coupon as a left join lkt_coupon_activity as b on a.hid = b.id where a.store_id = '$store_id' and b.store_id = '$store_id' and a.user_id = '$user_id' and a.type = 0 and a.status = 0 and a.recycle = 0 and b.mch_id = '$mch_id' order by b.money desc,a.expiry_time asc ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $id = $v['id']; // 优惠券id
                $expiry_time = $v['expiry_time']; // 优惠券到期时间
                if($expiry_time != null && $expiry_time <= $time)
                {
                    $r_coupon = Db::name('coupon')->where('id', $id)->update(['type'=>'3']);
                    if (!$r_coupon)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '优惠券ID为' . $id . '修改为已过期失败！';
                        $this->couponLog($id, $Log_content);
                    }
                }
                else
                {
                    $name = $v['name']; // 优惠券名称
                    $activity_type = $v['activity_type']; // 优惠券类型
                    $money = $v['money']; // 面值
                    $discount = floatval($v['discount']); // 折扣值
                    $z_money = $v['z_money']; // 满多少
                    $type = $v['type']; // 优惠券使用范围
                    $product_class_id = $v['product_class_id']; // 分类id
                    $product_id1 = $v['product_id']; // 商品id
                    // 优惠券类型4：会员赠送
                    if($activity_type == 4)
                    {
                        if($money == 0 && $discount != 0)
                        { // 当会员赠券为折扣时
                            $list0 = $this->coupon_screening0($type,$v,$product_id1,$mch_product_id,$product_class_id,$mch_product_class);
                        }
                        else
                        {
                            $list0 = $this->coupon_screening0($type,$v,$product_id1,$mch_product_id,$product_class_id,$mch_product_class);
                        }
                    }
                    else
                    {
                        if($mch_yunfei == 0 && $activity_type == 1)
                        { // 原本运费为0 并且 在使用中的优惠券为免邮券
                            $list0 = '';
                        }
                        else
                        {
                            $list0 = $this->coupon_screening0($type,$v,$product_id1,$mch_product_id,$product_class_id,$mch_product_class);
                        }
                    }

                    $mch_product_total0 = 0; // 优化
                    if($list0 != '')
                    {
                        foreach ($products_list as $k1 => $v1)
                        {
                            if(in_array($v1['pid'],$list0['accord_with']))
                            {
                                $mch_product_total0 += $v1['membership_price'] * $v1['num']; // 会员价 * 数量 = 店铺商品小计
                            }
                        }

                        $list = $this->coupon_screening1($mch_product_total0,$z_money,$money,$list0);

                        if($list != '' )
                        {
                            $zong_money = round(floor($mch_product_total0 * $discount*100)/100/10,2);
                            if($zong_money == 0)
                            {
                                $zong_money1 = 0;
                            }
                            else
                            {
                                $zong_money1 = $mch_product_total0 - $zong_money;
                            }
                            if($activity_type == 1) //优惠券类型1：免邮
                            {
                                $list['money'] = '0'; // 优惠券金额
                                $list['coupon_name'] = $name;
                            }
                            else if($activity_type == 2) //优惠券类型2：满减
                            {
                                $list['coupon_name'] = $name . ':优惠' . $list['money'] . '金额';
                            }
                            else if($activity_type == 3) //优惠券类型3：折扣
                            {
                                $list['money'] = round($zong_money1,2);
                                $list['coupon_name'] = $name . ':优惠' . $zong_money1 . '金额(' . $discount . '折)';
                            }
                            else if($activity_type == 4) //优惠券类型4：会员赠送
                            {
                                if($money == 0 && $discount != 0)
                                { // 当会员赠券为折扣时
                                    $list['coupon_name'] = $name . ':优惠' . $zong_money1 . '金额(' . $discount . '折)';
                                    $list['money'] = round($zong_money1,2);
                                }
                                else
                                {
                                    $list['coupon_name'] = $name . ':优惠' . $list['money'] . '金额';
                                }
                            }
                            $list['discount'] = $discount;
                            $list['mch_product_total'] = $mch_product_total0; // 店铺商品小计
                            $arr1[] = $list;
                        }
                    }
                }
            }
        }

        $arr1 = isset($arr1) ? $arr1:array();
        $message_0 = Lang('nomal_order.0');

        if(count($arr1) > 0)
        {
            $money_list = array();
            foreach ($arr1 as $k => $v)
            {
                $money_list[$k] = $v['money'];
            }
            array_multisort($money_list,SORT_DESC,$arr1);

            $arr1[] = array('id'=>'0','coupon_name'=>$message_0,'money'=>0,'discount'=>0,'accord_with'=>array(),'activity_type'=>'','mch_product_total'=>0,'z_money'=>'');
            // 设置默认优惠券
            $r_coupon3 = Db::name('coupon')->where('id', $arr1[0]['id'])->update(['type'=>'1']);
            if(!$r_coupon3)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员'.$user_id.'优惠券ID为'.$id.'修改为使用中失败！';
                $this->couponLog($id,$Log_content);
            }
            foreach ($arr1 as $k => $v)
            {
                $coupon_status = false;
                if($coupon_id != '' && $coupon_id != '0')
                {
                    if($coupon_id == $v['id'])
                    {
                        $coupon_status = true;
                    }
                }
                else
                { // 当还未选择优惠券
                    if($canshu == 'false')
                    { // 设置默认优惠券
                        if($k == 0)
                        {
                            $coupon_status = true;
                        }
                    }
                    else
                    { // 不使用优惠券
                        if($coupon_id == $v['id'])
                        {
                            $coupon_status = true;
                        }
                    }
                }
                $arr[$k]['coupon_id'] = $v['id']; // 优惠券ID
                $arr[$k]['activityType'] = $v['activity_type']; // 优惠券类型
                $arr[$k]['money'] = round($v['money'],2); // 优惠金额
                $arr[$k]['discount'] = round($v['discount'],2); // 优惠折扣
                if($v['z_money'] == '')
                {
                    $arr[$k]['limitMoney'] = '';
                }
                else
                { 
                    $arr[$k]['limitMoney'] = round($v['z_money'],2);
                }
                $arr[$k]['coupon_name'] = $v['coupon_name']; // 优惠券名称
                $arr[$k]['coupon_status'] = $coupon_status;
                $arr[$k]['shop_subtotal'] = round($mch_product_total - $v['money'],2); // 店铺小计
                $arr[$k]['accord_with'] = $v['accord_with'];
                $arr[$k]['mch_product_total'] = round($v['mch_product_total'],2); // 优惠券金额
            }
        }

        foreach ($arr as $k => $v)
        {
            if($v['coupon_status'])
            {
                if(count($v['accord_with']) > 0)
                {
                    foreach ($v['accord_with'] as $k1 => $v1)
                    {
                        foreach ($products_list as $k2 => $v2)
                        {
                            if($v1 == $v2['pid'])
                            {
                                $single_commodity = $v2['membership_price'] * $v2['num']; // 会员价 * 数量 = 该商品总价
                                $products_list[$k2]['discount'] = round($single_commodity / $v['mch_product_total'] * $v['money'],2); // 该商品总价 / 店铺商品小计 * 优惠券金额 = 该商品使用店铺优惠金额
                                $products_list[$k2]['amount_after_discount'] = $single_commodity - round($single_commodity / $v['mch_product_total'] * $v['money'],2); // 该商品总价 - 该商品使用店铺优惠金额 = 优惠后金额
                                $products_list[$k2]['coupon_id'] = $coupon_id;
                            }
                        }
                    }
                }
            }
        }

        $data = array('products_list'=>$products_list,'list'=>$arr);
        return $data;
    }

    // 优惠券筛选-优惠券可用范围判断(第一轮)
    public function coupon_screening0($type, $v, $product_id1, $product_id, $product_class_id, $product_class)
    {
        $list = '';
        if ($type == 1)
        { // 全部商品
            $v['accord_with'] = $product_id; // 符合
            $list = (array)$v;
        }
        else if ($type == 2)
        {
            $product_status = $this->product_accord0($v,$product_id1, $product_id);
            if ($product_status['product_status'] != 0)
            { // 符合
                $list = (array)$product_status['list'];
            }
        }
        else if ($type == 3)
        {
            $calss_status = $this->calss_accord0($v,$product_class_id, $product_class,$product_id);
            if ($calss_status['calss_status'] != 0)
            { // 符合
                $list = (array)$calss_status['list'];
            }
        }
        return $list;
    }

    // 验证店铺商品是否符合条件(只要有一件满足，优惠券就显示)
    public function product_accord0($v,$product_id1, $product_id)
    { 
        // (优惠券指定商品ID,购买商品ID)
        $product_id1 = unserialize($product_id1);
        $product_id1 = rtrim($product_id1, ','); // 去除字符串最后一个逗号
        $product_id_list = explode(",", $product_id1); // 字符串转数组
        $product_status = 0; // 商品状态 0:代表购买商品 不符合 优惠券指定 商品
        $v['accord_with'] = array(); // 存储符合的商品ID

        foreach ($product_id as $va)
        {
            if (in_array($va, $product_id_list))
            {
                $v['accord_with'][] = $va; // 符合
                $product_status = 1; // 商品状态 1:代表购买商品 符合 优惠券指定 商品
            }
        }

        $data = array('product_status'=>$product_status,'list'=>$v);
        return $data;
    }

    // 验证店铺商品分类是否符合条件(只要有一件满足，优惠券就显示)
    public function calss_accord0($v,$product_class_id, $product_class,$product_id)
    { 
        // (优惠券指定分类ID,购买商品的分类ID)
        $product_class_id = unserialize($product_class_id);
        $product_class_id = rtrim($product_class_id, ','); // 去除字符串最后一个逗号
        $product_class_list = explode(",", $product_class_id); // 字符串转数组
        $calss_status = 0; // 商品状态 0:代表购买商品 不符合 优惠券指定 商品
        $v['accord_with'] = array(); // 存储符合的商品ID

        foreach ($product_class as $ke => $va)
        {
            foreach ($product_class_list as $ke1 => $va1)
            {
                $count = count($product_class_list) - 1;
                $va1 = '-' . $va1 . '-';
                if (strpos($va, $va1) !== false)
                { // 符合
                    $v['accord_with'][] = $product_id[$ke];
                    $calss_status = 1;
                }
            }
        }
        
        $data = array('calss_status'=>$calss_status,'list'=>$v);

        return $data;
    }

    // 优惠券筛选-优惠券消费门槛判断(第二轮)
    public function coupon_screening1($mch_product_total,$z_money,$money,$list)
    {
        if($z_money == 0)
        { // 当无限制时
            if($money >= $mch_product_total)
            { // 优惠券金额 > 商品总价
                $list = '';
            }
            else
            {
                $list['money'] = $money;
            }
        }
        else
        { // 当有限制时
            if($mch_product_total < $z_money)
            { // 商品总价<使用限制
                $list = '';
            }
        }
        return $list;
    }

    // 进入结算页面-获取平台可使用的优惠券
    public function settlement_platform_coupons($store_id, $user_id, $products,$coupon_str,$canshu='')
    {
        $time = date('Y-m-d H:i:s');  // 当前时间

        $arr = array();
        $arr1 = array();
        $discount = 0;
        $shop_subtotal = 0; // 店铺总价之和
        $coupon_id = '0';
        $product_class = array(); // 购买商品的分类信息
        $product_id = array(); // 购买商品的商品ID
        $yunfei = 0; // 店铺总运费
        if($coupon_str != '')
        {
            $coupon_list = explode(',',$coupon_str);
            $coupon_num = count($coupon_list)-1;
            $coupon_id = $coupon_list[$coupon_num];
        }
        foreach ($products as $k => $v)
        {
            foreach ($v['list'] as $ke => $va) 
            {
                $product_class[] = $va['product_class']; // 购买商品的分类信息
                $product_id[] = $va['pid']; // 购买商品的商品ID
                $yunfei += $va['freight_price'];
            }
            if(count($v['coupon_list']) > 0)
            {
                foreach ($v['coupon_list'] as $ke => $va)
                {
                    if($va['coupon_status'])
                    {
                        $shop_subtotal = round($shop_subtotal + $va['shop_subtotal'],2); // 店铺总价之和 + 本店铺合计
                    }
                }
            }
            else
            {
                $shop_subtotal = round($shop_subtotal + ($v['product_total'] - $v['freight_price']),2); // 店铺总价之和 + （本店铺商品总价 - 本店铺运费）
            }
        }

        // 查询使用中的优惠券
        $sql0 = "select a.id,a.hid,a.expiry_time from lkt_coupon as a left join lkt_coupon_activity as b on a.hid = b.id where a.store_id = '$store_id' and b.store_id = '$store_id' and a.user_id = '$user_id' and a.type = 1 and a.status = 0 and a.recycle = 0 and b.mch_id = 0 ";
        $r0 = Db::query($sql0);
        if ($r0)
        { // 存在 使用中 的优惠券
            foreach ($r0 as $k => $v)
            { // 循环判断 优惠券是否绑定
                $id = $v['id']; // 优惠券id
                if($v['expiry_time'] == null)
                {
                    $v['expiry_time'] = '';
                }
                $expiry_time = $v['expiry_time']; // 优惠券到期时间
                
                // 根据优惠券id,查询订单表(查看优惠券是否绑定)
                $r_order = OrderModel::where(['store_id'=>$store_id,'coupon_id'=>$id])->whereNotIn('status','6,7')->field('id')->select()->toArray();
                if (empty($r_order))
                {
                    // 没有数据,表示优惠券没绑定
                    if ($expiry_time != '' && $expiry_time <= $time)
                    { // 当前时间 >= 优惠券到期时间
                        $r_coupon = Db::name('coupon')->where('id', $id)->update(['type'=>'3']);
                        if (!$r_coupon)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '优惠券ID为' . $id . '修改为已过期失败！';
                            $this->couponLog($id, $Log_content);
                        }
                    }
                    else
                    {
                        $r_coupon = Db::name('coupon')->where('id', $id)->update(['type'=>'0']);
                        if (!$r_coupon)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '优惠券ID为' . $id . '修改为已过期失败！';
                            $this->couponLog($id, $Log_content);
                        }
                    }
                }
            }
        }

        $sql1 = "select a.id,a.expiry_time,b.name,b.activity_type,b.money,b.discount,b.z_money,b.type,b.product_class_id,b.product_id from lkt_coupon as a left join lkt_coupon_activity as b on a.hid = b.id where a.store_id = '$store_id' and b.store_id = '$store_id' and a.user_id = '$user_id' and a.type = 0 and a.status = 0 and a.recycle = 0 and b.mch_id = '0' order by b.money desc,a.expiry_time asc ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $id = $v['id']; // 优惠券id
                if($v['expiry_time'] == null)
                {
                    $v['expiry_time'] = '';
                }
                $expiry_time = $v['expiry_time']; // 优惠券到期时间
                if ($expiry_time != '' && $expiry_time <= $time)
                {   
                    $r_coupon = Db::name('coupon')->where('id', $id)->update(['type'=>'3']);
                    if(!$r_coupon)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '会员'.$user_id.'优惠券ID为'.$id.'修改为已过期失败！';
                        $this->couponLog($id,$Log_content);
                    }
                }
                else
                {
                    $name = $v['name']; // 优惠券名称
                    $activity_type = $v['activity_type']; // 优惠券类型
                    $money = $v['money']; // 面值
                    $discount = floatval($v['discount']); // 折扣值
                    $z_money = $v['z_money']; // 满多少
                    $type = $v['type']; // 优惠券使用范围
                    $product_class_id = $v['product_class_id']; // 分类id
                    $product_id1 = $v['product_id']; // 商品id
                    $zong_money = round(floor($shop_subtotal * $discount*100)/100/10,2); // 优惠价格
                    if($zong_money == 0)
                    {
                        $zong_money1 = 0;
                    }
                    else
                    {
                        $zong_money1 = round($shop_subtotal - $zong_money,2);
                    }
                    if($activity_type == 1) //优惠券类型1：免邮
                    {
                        $v['money'] = '0'; // 优惠券金额
                        $v['coupon_name'] = $name;
                    }
                    else if($activity_type == 2) //优惠券类型2：满减
                    {
                        $v['money'] = round($v['money'],2);
                        $v['coupon_name'] = $name . ':优惠' . $money . '金额';
                    }
                    else if($activity_type == 3) //优惠券类型3：折扣
                    {
                        $v['coupon_name'] = $name . ':优惠' . $zong_money1 . '金额(' . $discount . '折)';
                        $v['money'] = round($zong_money1,2);
                    }
                    if($activity_type == 4) //优惠券类型4：会员赠送
                    {
                        if($money == 0 && $discount != 0)
                        { // 当会员赠券为折扣时
                            $v['coupon_name'] = $name . ':优惠' . $zong_money1 . '金额(' . $discount . '折)';
                            $v['money'] = round($zong_money1,2);
                            $list = $this->coupin_list(3,$type,$shop_subtotal,$z_money,$money,$v,$product_id1,$product_id,$product_class_id,$product_class);
                        }
                        else
                        {
                            $v['money'] = round($v['money'],2);
                            $v['coupon_name'] = $name . ':优惠' . $money . '金额';
                            $list = $this->coupin_list(2,$type,$shop_subtotal,$z_money,$money,$v,$product_id1,$product_id,$product_class_id,$product_class);
                        }
                    }
                    else
                    {
                        if($yunfei == 0 && $activity_type == 1)
                        { // 原本运费为0 并且 在使用中的优惠券为免邮券
                            $list = '';
                        }
                        else
                        {
                            $list = $this->coupin_list($activity_type,$type,$shop_subtotal,$z_money,$money,$v,$product_id1,$product_id,$product_class_id,$product_class);
                        }
                    }
                    if($list != '' )
                    {
                        $arr1[] = $list;
                    }
                }
            }
        }
        $arr1 = isset($arr1) ? $arr1:array();

        $p_discount = 0;
        if(count($arr1) > 0)
        {
            $money_list = array();
            foreach ($arr1 as $k => $v)
            {
                $money_list[$k] = $v['money'];
            }
            array_multisort($money_list, SORT_DESC, $arr1);
            foreach ($arr1 as $k => $v)
            {
                $coupon_status = false;
                if($coupon_id != '' && $coupon_id != '0')
                {
                    if($coupon_id == $v['id'])
                    {
                        $coupon_status = true;
                        Db::name('coupon')->where(['store_id'=>$store_id,'id'=>$coupon_id])->update(['type'=>'1']);
                    }
                }
                else
                {
                    if($canshu == 'false')
                    { // 设置默认优惠券
                        if($k == 0)
                        {
                            $coupon_status = true;
                            Db::name('coupon')->where(['store_id'=>$store_id,'id'=>$coupon_id])->update(['type'=>'1']);
                        }
                    }
                    else
                    { // 不使用优惠券
                        if($coupon_id == $v['id'])
                        {
                            $coupon_status = true;
                        }
                    }
                }

                $arr[$k]['coupon_id'] = $v['id'];
                $arr[$k]['discount_type'] = 'coupon';
                $arr[$k]['activity_type'] = $v['activity_type'];
                $arr[$k]['money'] = $v['money'];
                $arr[$k]['coupon_name'] = $v['coupon_name'];
                $arr[$k]['coupon_status'] = $coupon_status;
                $arr[$k]['shop_subtotal'] = round($shop_subtotal - $v['money'],2); // 店铺小计之和 - 平台优惠金额 = 总的小计
                if($coupon_status)
                {
                    $p_discount = $v['money'];
                }
            }
        }

        foreach ($products as $k => $v)
        {
            foreach ($v['list'] as $ke => $va) 
            {
                if(count($arr1) > 0)
                {
                    $single_commodity_discount = round($va['amount_after_discount']/$shop_subtotal * $p_discount,2); // 该使用店铺优惠之后的金额 / 总的小计 * 平台优惠金额 = 该商品使用了平台优惠金额
                    $products[$k]['list'][$ke]['amount_after_discount'] = $va['amount_after_discount'] - $single_commodity_discount; // 该商品优惠后金额 = 该使用店铺优惠之后的金额 - 该商品使用了平台优惠金额
                }

                $products[$k]['list'][$ke]['coupon_id'] .= ',' . $coupon_id;
            }
        }

        $data = array('products'=>$products,'list'=>$arr);
        return $data;
    }

    // 验证优惠券是否符合（进入确认订单页面）
    public function coupin_list($activity_type, $type, $zong, $z_money, $money, $v, $product_id1, $product_id, $product_class_id, $product_class)
    {
        $list = '';
        if ($type == 1)
        { // 全部商品
            if ($z_money == 0)
            {
                if ($activity_type == 2)
                {
                    if ($zong > $money)
                    {
                        $v['money'] = $v['money']; // 优惠券金额
                    }
                    else
                    {
                        $v['money'] = $zong; // 优惠券金额
                    }
                }
                $list = (array)$v;
            }
            else
            {
                if ($zong >= $z_money)
                { // 商品总价 >= 优惠券满多少
                    $list = (array)$v;
                }
            }
        }
        else if ($type == 2)
        {
            $product_status = $this->product_accord($product_id1, $product_id);
            if ($product_status != 0)
            { // 符合
                if ($z_money == 0)
                {
                    if ($zong > $money)
                    {
                        $v['money'] = $v['money']; // 优惠券金额
                    }
                    else
                    {
                        $v['money'] = $zong; // 优惠券金额
                    }
                    $list = (array)$v;
                }
                else
                {
                    if ($zong >= $z_money)
                    { // 商品总价 >= 优惠券满多少
                        $list = (array)$v;
                    }
                }
            }
        }
        else if ($type == 3)
        {
            $calss_status = $this->calss_accord($product_class_id, $product_class);
            if ($calss_status != 0)
            { // 符合
                if ($z_money == 0)
                {
                    if ($zong > $money)
                    {
                        $v['money'] = $v['money']; // 优惠券金额
                    }
                    else
                    {
                        $v['money'] = $zong; // 优惠券金额
                    }
                    $list = (array)$v;
                }
                else
                {
                    if ($zong >= $z_money)
                    { // 商品总价 >= 优惠券满多少
                        $list = (array)$v;
                    }
                }
            }
        }
        return $list;
    }

    // 验证商品是否符合条件(只要有一件不满足，优惠券就不显示)
    public function product_accord($product_id1, $product_id)
    { 
        // (优惠券指定商品ID,购买商品ID)
        $product_id1 = unserialize($product_id1);
        $product_id1 = rtrim($product_id1, ','); // 去除字符串最后一个逗号
        $product_id_list = explode(",", $product_id1); // 字符串转数组
        $product_status = 1; // 商品状态 1:代表购买商品 符合 优惠券指定 商品
        foreach ($product_id as $va)
        {
            if (in_array($va, $product_id_list))
            {
                continue;
            }
            else
            {
                $product_status = 0; // 商品状态 0:代表购买商品 不符合 优惠券指定 商品
                break;
            }
        }
        return $product_status;
    }

    // 验证商品分类是否符合条件(只要有一件不满足，优惠券就不显示)
    public function calss_accord($product_class_id, $product_class)
    {
        $product_class_id = unserialize($product_class_id);
        $product_class_id = rtrim($product_class_id, ','); // 去除字符串最后一个逗号
        $product_class_list = explode(",", $product_class_id); // 字符串转数组
        $calss_status = 1; // 商品属于优惠券指定的分类

        foreach ($product_class as $va)
        {
            foreach ($product_class_list as $ke1 => $va1)
            {
                $count = count($product_class_list) - 1;
                if (strpos($va, '-'.$va1.'-') !== false)
                {
                    $calss_status = 1;
                    break;
                }
                else
                {
                    if ($count <= $ke1)
                    {
                        $calss_status = 0; // 商品状态 0:代表购买商品 不符合 优惠券指定 商品
                        break 2;
                    }
                }
            }
        }
        return $calss_status;
    }

    // 定时处理
    public function timing()
    {
        $time = date('Y-m-d H:i:s'); // 当前时间

        $r0 = CouponActivityModel::where('status','<>',3)->field('id,status,start_time,end_time,day')->select()->toArray();
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                if ($v['status'] == 1 || $v['status'] == 2)
                {
                    if ($v['day'] <= 0 && $v['end_time'] != null && $v['end_time'] <= $time)
                    {
                        $r2 = Db::name('coupon_activity')->where(['id'=>$v['id']])->update(['status'=>'3']);
                        $r3 = Db::name('coupon')->where(['hid'=>$v['id']])->update(['type'=>'3']);
                    }
                }
            }
        }

        $r4 = CouponModel::whereIn('type','0,1')->field('id,expiry_time')->select()->toArray();
        if ($r4)
        {
            foreach ($r4 as $ke => $va)
            {
                if ($va['expiry_time'] != null && $va['expiry_time'] <= $time)
                {
                    $r5 = Db::name('coupon')->where(['id'=>$va['id']])->update(['type'=>'3']);
                }
            }
        }
        
        // 系统赠送
        $r_0 = CouponActivityModel::where(['receive_type'=>1,'recycle'=>0])->where('status','<>',3)->field('id,store_id,activity_type,mch_id,receive,end_time,add_time,num')->select()->toArray();
        if($r_0)
        {
            foreach ($r_0 as $k_0 => $v_0)
            {
                $id = $v_0['id'];
                $store_id = $v_0['store_id'];
                $activity_type = $v_0['activity_type'];
                $mch_id = $v_0['mch_id'];
                $add_time = $v_0['add_time'];
                $end_time = $v_0['end_time'];
                $receive = $v_0['receive'];
                $num = $v_0['num'];
                $num_0 = 0;

                $r_1 = UserModel::where(['store_id'=>$store_id])->where('Register_data', '<', $add_time)->field('user_id,mobile')->select()->toArray();
                if($r_1)
                {
                    foreach ($r_1 as $k_1 => $v_1)
                    {
                        $user_id = $v_1['user_id']; // 用户user_id
                        $mobile = $v_1['mobile']; // 手机号
                        
                        if($mobile == null)
                        {
                            $mobile = '';
                        }
                        $r_2 = CouponModel::where(['store_id'=>$store_id,'hid'=>$id,'user_id'=>$user_id])->field('id')->select()->toArray();
                        if(!$r_2)
                        {
                            for($i=0;$i<$receive;$i++)
                            {
                                if($num_0 < $num)
                                {
                                    // 在优惠券表里添加一条数据
                                    $sql_3 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'user_id'=>$user_id,'add_time'=>$time,'expiry_time'=>$end_time,'hid'=>$id,'free_or_not'=>1);
                                    $r_3 = Db::name('coupon')->insertGetId($sql_3);

                                    // 在赠送优惠券记录表里添加一条数据
                                    $sql_4 = array('store_id'=>$store_id,'coupon_activity_id'=>$id,'coupon_id'=>$r_3,'user_id'=>$user_id,'mobile'=>$mobile,'activity_type'=>$activity_type,'add_date'=>$time);
                                    $r_4 = Db::name('coupon_presentation_record')->insert($sql_4);
                                    
                                    $i++;
                                    $num_0++;
                                }
                                else
                                {
                                    break;
                                }
                            }
                        }
                    }
                }

                // 修改优惠券活动剩余库存
                $sql0_0_update = array('num'=>Db::raw('num-'.$num_0));
                $sql0_0_where = array('store_id'=>$store_id,'id'=>$id,'recycle'=>0);
                $r0_0 = Db::name('coupon_activity')->where($sql0_0_where)->update($sql0_0_update);
            }
        }

        return;
    }

    // 获取插件状态（用户）
    public function Get_plugin_status($store_id)
    {
        $is_status = 0; // 用户端插件状态 0.关闭 1.开启
        $r0 = CouponConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('is_status')->select()->toArray();
        if ($r0)
        {
            $is_status = $r0[0]['is_status'];
        }
        
        return $is_status;
    }

    // 获取插件状态（店铺）
    public function Get_plugin_status_mch($store_id)
    {
        $status = 0; // 店铺端插件状态 0.关闭 1.开启
        $sql0 = "select status from lkt_plugins where store_id = '$store_id' and plugin_code = 'coupon' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $status = $r0[0]['status'];
        }
        
        return $status;
    }
}
