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
use app\common\PC_Tools;

use app\admin\model\UserModel;
use app\admin\model\MchModel;

/**
 * 功能：移动端店鋪优惠券
 * 修改人：DHB
 */
class Appcoupon 
{
    // 领券中心
    public function Index()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $type = addslashes(Request::param('type')); // 类型 1.平台优惠券 2.店铺优惠券
        $mcnName = addslashes(Request::param('mcnName')); // 店铺名称
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

        $data0 = array('store_id'=>$store_id,'user_id'=>$user_id,'type'=>$type,'page'=>$page,'pagesize'=>$pagesize,'mcnName'=>$mcnName);
        $coupon0 = new CouponPublicMethod();
        $data = $coupon0->mobile_terminal_coupon_center($data0);

        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 点击领取
    public function Receive()
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

    // 店铺主页获取优惠券活动
    public function MchCoupon()
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
        $Plugin_arr1 = $Plugin->Get_plugin_entry($store_id);

        $coupon = new CouponPublicMethod();
        $coupon_arr = array();
        foreach ($Plugin_arr1 as $k => $v)
        {
            if ($k == 'coupon' && $v == 1)
            {
                $status = $coupon->Get_plugin_status_mch($store_id);

                if($status == 1)
                { // 店铺看见优惠券
                    $coupon_arr = $coupon->mch_coupon($store_id, $user_id, $shop_id,1);
                    array_multisort(array_column($coupon_arr, 'point_type'), SORT_ASC, $coupon_arr);
                }
                else
                { // 店铺看不见优惠券
                    $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID

                    if($SelfOperatedStore_id == $shop_id)
                    { // 如果是该店铺是自营店
                        $coupon_arr = $coupon->mch_coupon($store_id, $user_id, $shop_id,1);
                        array_multisort(array_column($coupon_arr, 'point_type'), SORT_ASC, $coupon_arr);
                    }
                }
            }
        }
        $data = array('list' => $coupon_arr);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 我的优惠券
    public function MyCoupon()
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

        $isCouponShow = false;
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
                $data = array('list' => $list['list'],'isCouponShow' => $list['isCouponShow'],'useNum'=>$useNum,'wsy_num'=>$list['wsy_num'],'ysy_num'=>$list['ysy_num'],'ygq_num'=>$list['ygq_num']);
            }
            else if($type == 2)
            {
                $overdueNum = count($list['list']);
                $data = array('list' => $list['list'],'isCouponShow' => $list['isCouponShow'],'overdueNum'=>$overdueNum,'wsy_num'=>$list['wsy_num'],'ysy_num'=>$list['ysy_num'],'ygq_num'=>$list['ygq_num']);
            }
            else
            {
                $data = array('list' => $list['list'],'isCouponShow' => $list['isCouponShow'],'wsy_num'=>$list['wsy_num'],'ysy_num'=>$list['ysy_num'],'ygq_num'=>$list['ygq_num']);
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
    public function BatchDel()
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

        $sql_0 = "select * from lkt_coupon where id in ($ids)";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            foreach($r_0 as $k_0 => $v_0)
            {
                $id_0 = $v_0['id'];
                $sql_1 = "select * from lkt_coupon_sno as a left join lkt_order as b on a.sNo = b.sNo where b.status = 0 and a.coupon_id = '$id_0' ";
                $r_1 = Db::query($sql_1);
                if($r_1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除优惠券id为 ' . $ids . ' 的信息成功';
                    $this->Log($Log_content);
                    $message = Lang('coupon.38');
                    return output(200,$message);
                }
            }
        }

        $sql0_update = array('recycle'=>1);
        $r0 = Db::name('coupon')->where('id','in',$ids)->update($sql0_update);
        if ($r0 > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除优惠券id为 ' . $ids . ' 的信息成功';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除优惠券失败！参数:'.$ids;
            $this->Log($Log_content);
            $message = Lang('coupon.34');
            return output(109,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("plugin/Appcoupon.log",$Log_content);
        return;
    }
}
