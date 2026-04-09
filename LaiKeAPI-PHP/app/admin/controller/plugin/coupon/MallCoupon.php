<?php
namespace app\admin\controller\plugin\coupon;

use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Plugin\CouponPublicMethod;
use app\common\LaiKeLogUtils;

use app\admin\model\UserModel;

/**
 * 功能：PC商城优惠券
 * 修改人：DHB
 */
class MallCoupon 
{
    // 获取优惠券活动
    public function index()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言

        $type = addslashes(Request::param('type')); // 类型 1.平台优惠券 2.店铺优惠券
        $mcnName = addslashes(Request::param('mcnName')); // 店铺名称
        $page = addslashes(trim(Request::param('page'))); // 加载次数
        $user_id = '';
        if ($access_id != '')
        {
            // 查询用户id
            $r0_0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
            if ($r0_0)
            {
                $user_id = $r0_0[0]['user_id'];
            }
        }

        $coupon = new CouponPublicMethod();
        $data = $coupon->pc_coupon_center($store_id, $user_id,$page,$type,$mcnName);

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
            $message = Lang('Please log in');
            return output(404,$message);
        }
        // 查询用户id
        $r0_0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if ($r0_0)
        {
            $user_id = $r0_0[0]['user_id'];

            $coupon = new CouponPublicMethod();
            $res = $coupon->receive($store_id, $user_id, $id);

            $message = Lang('Success');
            return output(200,$message, $res);
        }
        else
        {
            $message = Lang('Please log in');
            return output(404,$message);
        }
    }

    // 我的优惠券
    public function myCoupon()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言
        $type = addslashes(trim(Request::param('type'))); // 0：未使用 1：已使用 2：已过期

        if (empty($access_id))
        {
            $message = Lang('Please log in');
            return output(404,$message);
        }

        // 根据微信id,查询用户id
        $r0_0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if ($r0_0)
        {
            $user_id = $r0_0[0]['user_id'];

            $coupon = new CouponPublicMethod();
            $list = $coupon->mycoupon($store_id, $user_id, $type);

            if($type == 1)
            {
                $total = $list['ysy_num'];
            }
            else if($type == 2)
            {
                $total = $list['ygq_num'];
            }
            else
            {
                $total = $list['wsy_num'];
            }
            $data = array('wsy_num'=>$list['wsy_num'],'ysy_num'=>$list['ysy_num'],'ygq_num'=>$list['ygq_num'],'list'=>$list['list'],'total'=>$total);
            $message = Lang('Success');
            return output(200,$message, $data);
        }
        else
        {
            $message = Lang('Please log in');
            return output(404,$message);
        }
    }
    
    // 商品详情获取商品可用优惠券活动
    public function proCoupon()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言
        $pro_id = addslashes(trim(Request::param('goodsId'))); // 商品ID
        $user_id = '';

        if ($access_id != '')
        {
            // 查询用户id
            $r0_0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
            if ($r0_0)
            {
                $user_id = $r0_0[0]['user_id'];
            }
        }

        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->mch_pro_coupon($store_id, $user_id, $pro_id);

        if(count($coupon_arr) > 0)
        {
            array_multisort(array_column($coupon_arr, 'point_type'), SORT_ASC, $coupon_arr);
        }

        $message = Lang('Success');
        return output(200,$message, $coupon_arr);
    }

    // 店铺获取商品可用优惠券活动
    public function mchCoupon()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言
        
        $shop_id = addslashes(trim(Request::param('mchId'))); // 店铺ID
        $user_id = '';
        if ($access_id != '')
        {
            // 查询用户id
            $r0_0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
            if ($r0_0)
            {
                $user_id = $r0_0[0]['user_id'];
            }
        }

        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->mch_coupon($store_id, $user_id, $shop_id,1);

        if(count($coupon_arr) > 0)
        {
            array_multisort(array_column($coupon_arr, 'point_type'), SORT_ASC, $coupon_arr);
        }
        
        $data = array('list'=>$coupon_arr);
        $message = Lang('Success');
        return output(200,$message, $data);
    }
}

?>