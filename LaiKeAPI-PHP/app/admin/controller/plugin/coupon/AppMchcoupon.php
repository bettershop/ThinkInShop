<?php
namespace app\admin\controller\plugin\coupon;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\Plugin\Plugin;
use app\common\Plugin\CouponPublicMethod;
use app\common\Plugin\MchPublicMethod;

use app\admin\model\UserModel;
use app\admin\model\MchModel;

/**
 * 功能：移动端店鋪优惠券
 * 修改人：DHB
 */
class AppMchcoupon extends BaseController
{
    // 店铺-优惠券列表
    public function MchIndex()
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
    public function AddPage()
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
    public function Add()
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

    	$cover_map = addslashes($this->request->param('cover_map')); // 优惠券图片

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

        $data0 = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'id'=>'','issue_unit'=>$issue_unit,'activity_type'=>$activity_type,'name'=>$name,'money'=>$money,'discount'=>$discount,'consumption_threshold_type'=>$consumption_threshold_type,'z_money'=>$z_money,'type'=>$type,'receive_type'=>$receive_type,'circulation'=>$circulation,'end_time'=>$end_time,'receive'=>$receive,'Instructions'=>$Instructions,'day'=>$day,'menu_list'=>$menu_list,'class_list'=>$class_list,'cover_map'=>$cover_map);

        $coupon = new CouponPublicMethod();
        $data = $coupon->add_store_coupons($data0);

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 店铺-编辑优惠券页面
    public function ModifyPage()
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
    public function Modify()
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

    	$cover_map = addslashes($this->request->param('cover_map')); // 优惠券图片

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

        $data0 = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'id'=>$id,'issue_unit'=>$issue_unit,'activity_type'=>$activity_type,'name'=>$name,'money'=>$money,'discount'=>$discount,'consumption_threshold_type'=>$consumption_threshold_type,'z_money'=>$z_money,'type'=>$type,'receive_type'=>$receive_type,'circulation'=>$circulation,'end_time'=>$end_time,'receive'=>$receive,'Instructions'=>$Instructions,'day'=>$day,'menu_list'=>$menu_list,'class_list'=>$class_list,'cover_map'=>$cover_map);

        $coupon = new CouponPublicMethod();
        $data = $coupon->modify_store_coupons($data0);

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 店铺-领取记录
    public function SeeCoupon()
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
    public function MchUseRecord()
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

    // 获取分类
    public function Fenlei()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id
        $mch_id = addslashes(Request::param('mch_id')); // 店铺ID

        $coupon = new CouponPublicMethod();
        $coupon->mch_fenlei($store_id,$mch_id);
    }

    // 获取商品
    public function Product()
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

    // 店铺-删除优惠券活动
    public function Del()
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

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id,'shop_id'=>$mch_id,'operator'=>'','source'=>3);
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->delCoupon($array);
        // $coupon = new CouponPublicMethod();
        // $data = $coupon->del_mch_coupon($store_id,$admin_name,$mch_id,$id);

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("plugin/AppMchcoupon.log",$Log_content);
        return;
    }
}
