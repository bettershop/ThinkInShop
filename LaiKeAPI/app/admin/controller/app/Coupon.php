<?php
namespace app\admin\controller\app;

use think\facade\Db;
use think\facade\Request;

use app\common\LaiKeLogUtils;
use app\common\Plugin\Plugin;
use app\common\Plugin\CouponPublicMethod;
use app\common\Plugin\MchPublicMethod;

use app\admin\model\UserModel;
use app\admin\model\MchModel;

class Coupon
{
    // 领券中心
    public function index()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $type = addslashes(Request::param('type')); // 类型 1.平台优惠券 2.店铺优惠券
        $page = addslashes(Request::param('page')); // 加载次数
        $pagesize = addslashes(Request::param('pageSize')); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : 10;

        $user_id = '';
        if ($access_id != '')
        {
            // 查询用户id
            $user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
            if ($user)
            {
                $user_id = $user[0]['user_id'];
            }
        }

        $data0 = array('store_id'=>$store_id,'user_id'=>$user_id,'type'=>$type,'page'=>$page,'pagesize'=>$pagesize);
        $coupon0 = new CouponPublicMethod();
        $data = $coupon0->mobile_terminal_coupon_center($data0);

        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 点击领取
    public function receive()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $id = addslashes(Request::param('id')); // 活动id

        if (empty($access_id))
        {
            $message = Lang('Please_log_in');
            return output(404,$message);
        }
        // 查询用户id
        $user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if ($user)
        {
            $user_id = $user[0]['user_id']; // 用户id
            $coupon = new CouponPublicMethod();
            $res = $coupon->receive($store_id, $user_id, $id);

            $data = array('money' => $res);
            $message = Lang('Success');
            return output(200, $message,$data);
        }
        else
        {
            $message = Lang('Please_log_in');
            return output(404,$message);
        }
    }

    // // 获取商品可用优惠券活动
    // public function pro_coupon()
    // {
    //     $store_id = addslashes(trim($_POST['store_id']));
    //     $store_type = addslashes(trim($_POST['store_type'])); // 来源
    //     $access_id = addslashes(trim($_POST['access_id'])); // 授权id
    //     $language = addslashes(trim($_POST['language'])); // 语言
    //     $pro_id = addslashes(trim($_POST['pro_id'])); // 商品id

    //     $user_id = '';
    //     if (!empty($access_id))
    //     {
    //         $user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])
    //                         ->field('user_id')
    //                         ->select()
    //                         ->toArray();
    //         if ($user)
    //         {
    //             $user_id = $user[0]['user_id'];
    //         }
    //     }
    //     $coupon = new CouponPublicMethod();
    //     $coupon_arr = $coupon->pro_coupon($store_id, $user_id, $pro_id);

    //     array_multisort(array_column($coupon_arr, 'point_type'), SORT_ASC, $coupon_arr);

    //     $data = array('list' => $coupon_arr);
    //     $message = Lang('Success');
    //     return output(200, $message,$data);
    // }

    // 店铺主页获取优惠券活动
    public function mch_coupon()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id
        
        $shop_id = addslashes(Request::param('shop_id')); // 店铺id

        $user_id = '';

        if (!empty($access_id))
        {
            // 查询用户id
            $user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
            if ($user)
            {
                $user_id = $user[0]['user_id'];
            }
        }

        $Plugin = new Plugin();
        $Plugin_arr1 = $Plugin->front_Plugin($store_id);

        $coupon = new CouponPublicMethod();
        $coupon_arr = array();
        foreach ($Plugin_arr1 as $k => $v)
        {
            if ($k == 'CouponPublicMethod' && $v == 1)
            {
                $coupon_arr = $coupon->mch_coupon($store_id, $user_id, $shop_id,1);
                array_multisort(array_column($coupon_arr, 'point_type'), SORT_ASC, $coupon_arr);
            }
        }
        $data = array('list' => $coupon_arr);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 我的优惠券
    public function mycoupon()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id
        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID

        $type = addslashes(Request::param('type')); // 0.未使用 1.已使用 2.已过期

        if (empty($access_id))
        {
            $message = Lang('Please_log_in');
            return output(404, $message);
        }

        // 根据微信id,查询用户id
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if ($r)
        {
            $user_id = $r[0]['user_id'];

            $coupon = new CouponPublicMethod();
            $list = $coupon->mycoupon($store_id, $user_id, $type);
            
            if($type == 1)
            {
                $useNum = count($list['list']);
                $data = array('list' => $list['list'],'useNum'=>$useNum);
            }
            else if($type == 2)
            {
                $overdueNum = count($list['list']);
                $data = array('list' => $list['list'],'overdueNum'=>$overdueNum);
            }
            else
            {
                $data = array('list' => $list['list']);
            }
            
            $message = Lang('Success');
            return output(200, $message,$data);
        }
        else
        {
            $message = Lang('Please_log_in');
            return output(404, $message);
        }
    }
    
    // 我的优惠券-删除
    public function batchDel()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $ids = addslashes(Request::param('ids')); // 优惠券ID

        if (empty($access_id))
        {
            $message = Lang('Please_log_in');
            return output(404, $message);
        }
        
        $sql0_update = array('recycle'=>1);
        $r0 = Db::name('coupon')->where('id','in',$ids)->update($sql0_update);
        if ($r0 > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除优惠券id为 ' . $ids . ' 的信息成功';
            $this->mchLog($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除优惠券失败！参数:'.$ids;
            $this->mchLog($Log_content);
            $message = Lang('coupon.34');
            return output(109,$message);
        }
    }

    // 店铺-优惠券列表
    public function mch_index()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID
        $page = addslashes(Request::param('page')); // 加载次数
        $pagesize = addslashes(Request::param('pagesize')); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r_user)
        {
            $user_id = $r_user[0]['user_id'];
            $admin_name = $r_user[0]['zhanghao'];
        }
        else
        {
            $message = Lang('Please_log_in');
            return output(404,$message);
        }

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$mch_id);

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'hid'=>0,'name'=>'','activity_type'=>'','status'=>'','page'=>$page,'pagesize'=>$pagesize);
        $coupon = new CouponPublicMethod();
        $data = $coupon->store_coupons($array);

        $message = Lang('Success');
        return output('200', $message,$data);
    }

    // 店铺-添加优惠券页面
    public function add_page()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID

        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r_user)
        {
            $user_id = $r_user[0]['user_id'];
            $admin_name = $r_user[0]['zhanghao'];
        }
        else
        {
            $message = Lang('Please_log_in');
            return output(404,$message);
        }
        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$mch_id);

        $coupon = new CouponPublicMethod();
        $data = $coupon->add_store_coupons_page($store_id,$mch_id);

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 店铺-添加优惠券
    public function add()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id
        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID

        $issue_unit = addslashes(Request::param('issueUnit')); // 发行单位 0=商城 1=店铺
        $activity_type = addslashes(Request::param('activityType')); // 活动类型
        $name = addslashes(Request::param('name')); // 活动名称
        $money = addslashes(Request::param('money')); // 优惠券面值
        $discount = addslashes(Request::param('discount')); // 折扣值
        $consumption_threshold_type = addslashes(Request::param('consumption_threshold_type')); // 消费门口  1.无门槛  2.有门槛
        $z_money = addslashes(Request::param('zmoney')); // 满多少
        $type = addslashes(Request::param('type')); // 优惠券使用范围
        $receive_type = addslashes(Request::param('receiveType')); // 领取方式 0=手动领取 1=系统赠送
        $circulation = addslashes(Request::param('circulation')); // 发行数量
        $start_time = addslashes(Request::param('start_time')); // 活动开始时间
        $end_time = addslashes(Request::param('end_time')); // 活动结束时间
        $receive = addslashes(Request::param('limitCount')); // 领取限制
        $Instructions = addslashes(Request::param('instructions')); // 使用说明
        $day = addslashes(Request::param('day')); // 有效时间
        $menu_list = addslashes(Request::param('menuList')); // 商品名称已选项
        $class_list = addslashes(Request::param('classList')); // 选中的商品分类ID数据

        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r_user)
        {
            $user_id = $r_user[0]['user_id'];
            $admin_name = $r_user[0]['zhanghao'];
        }
        else
        {
            $message = Lang('Please_log_in');
            return output(404,$message);
        }

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$mch_id);

        $data0 = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'id'=>'','issue_unit'=>$issue_unit,'activity_type'=>$activity_type,'name'=>$name,'money'=>$money,'discount'=>$discount,'consumption_threshold_type'=>$consumption_threshold_type,'z_money'=>$z_money,'type'=>$type,'receive_type'=>$receive_type,'circulation'=>$circulation,'end_time'=>$end_time,'receive'=>$receive,'Instructions'=>$Instructions,'day'=>$day,'menu_list'=>$menu_list,'class_list'=>$class_list);

        $coupon = new CouponPublicMethod();
        $data = $coupon->add_store_coupons($data0);

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 店铺-编辑优惠券页面
    public function modify_page()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id
        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID
        
        $id = addslashes(Request::param('id')); // 优惠券id

        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r_user)
        {
            $user_id = $r_user[0]['user_id'];
            $admin_name = $r_user[0]['zhanghao'];
        }
        else
        {
            $message = Lang('Please_log_in');
            return output(404,$message);
        }

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$mch_id);

        $coupon = new CouponPublicMethod();
        $data = $coupon->modify_store_coupons_page($store_id,$id);

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 店铺-编辑优惠券
    public function modify()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id
        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID

        $id = addslashes(Request::param('id')); // 活动id
        $issue_unit = addslashes(Request::param('issueUnit')); // 发行单位 0=商城 1=店铺
        $activity_type = addslashes(Request::param('activityType')); // 活动类型
        $name = addslashes(Request::param('name')); // 活动名称
        $money = addslashes(Request::param('money')); // 优惠券面值
        $discount = addslashes(Request::param('discount')); // 折扣值
        $consumption_threshold_type = addslashes(Request::param('consumption_threshold_type')); // 消费门口  1.无门槛  2.有门槛
        $z_money = addslashes(Request::param('zmoney')); // 满多少
        $type = addslashes(Request::param('type')); // 优惠券使用范围
        $receive_type = addslashes(Request::param('receiveType')); // 领取方式 0=手动领取 1=系统赠送
        $circulation = addslashes(Request::param('circulation')); // 发行数量
        $start_time = addslashes(Request::param('start_time')); // 活动开始时间
        $end_time = addslashes(Request::param('end_time')); // 活动结束时间
        $receive = addslashes(Request::param('limitCount')); // 领取限制
        $Instructions = addslashes(Request::param('instructions')); // 使用说明
        $day = addslashes(Request::param('day')); // 有效时间
        $menu_list = addslashes(Request::param('menuList')); // 商品名称已选项
        $class_list = addslashes(Request::param('classList')); // 选中的商品分类ID数据

        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r_user)
        {
            $user_id = $r_user[0]['user_id'];
            $admin_name = $r_user[0]['zhanghao'];
        }
        else
        {
            $message = Lang('Please_log_in');
            return output(404,$message);
        }

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$mch_id);

        $data0 = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'id'=>$id,'issue_unit'=>$issue_unit,'activity_type'=>$activity_type,'name'=>$name,'money'=>$money,'discount'=>$discount,'consumption_threshold_type'=>$consumption_threshold_type,'z_money'=>$z_money,'type'=>$type,'receive_type'=>$receive_type,'circulation'=>$circulation,'end_time'=>$end_time,'receive'=>$receive,'Instructions'=>$Instructions,'day'=>$day,'menu_list'=>$menu_list,'class_list'=>$class_list);

        $coupon = new CouponPublicMethod();
        $data = $coupon->modify_store_coupons($data0);

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 店铺-领取记录
    public function see_coupon()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id
        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID

        $hid = addslashes(Request::param('id')); // 优惠券id
        $page = addslashes(Request::param('page')); // 加载次数
        $pagesize = addslashes(Request::param('pagesize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10';
        
        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r_user)
        {
            $user_id = $r_user[0]['user_id'];
            $admin_name = $r_user[0]['zhanghao'];
        }
        else
        {
            $message = Lang('Please_log_in');
            return output(404,$message);
        }

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$mch_id);

        $data0 = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'hid'=>$hid,'page'=>$page,'pagesize'=>$pagesize);
        $coupon = new CouponPublicMethod();
        $data = $coupon->see_coupon($data0);

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 店铺-使用记录
    public function mchUseRecord()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id
        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID

        $hid = addslashes(Request::param('id')); // 优惠券id
        $page = addslashes(Request::param('page')); // 加载次数
        $pagesize = addslashes(Request::param('pagesize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10';
        
        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r_user)
        {
            $user_id = $r_user[0]['user_id'];
            $admin_name = $r_user[0]['zhanghao'];
        }
        else
        {
            $message = Lang('Please_log_in');
            return output(404,$message);
        }

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$mch_id);

        $data0 = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'hid'=>$hid,'page'=>$page,'pagesize'=>$pagesize);
        $coupon = new CouponPublicMethod();
        $data = $coupon->mchUseRecord($data0);

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 店铺-删除优惠券活动
    public function del()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id
        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID

        $id = addslashes(Request::param('id')); // 优惠券id

        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r_user)
        {
            $user_id = $r_user[0]['user_id'];
            $admin_name = $r_user[0]['zhanghao'];
        }
        else
        {
            $message = Lang('Please_log_in');
            return output(404,$message);
        }

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$mch_id);

        $coupon = new CouponPublicMethod();
        $data = $coupon->del_mch_coupon($store_id,$admin_name,$mch_id,$id);

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 获取分类
    public function fenlei()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id
        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID

        $coupon = new CouponPublicMethod();
        $coupon->mch_fenlei($store_id,$mch_id);
    }

    // 获取商品
    public function product()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID
        $product_title = addslashes(Request::param('name')); // 商品名称
        $page = addslashes(Request::param('page')); // 页码

        $coupon = new CouponPublicMethod();
        $coupon->mch_product($store_id,$mch_id,$product_title,$page);
    }

    // 店铺日志
    public function mchLog($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/coupon.log",$Log_content);

        return;
    }
}

?>