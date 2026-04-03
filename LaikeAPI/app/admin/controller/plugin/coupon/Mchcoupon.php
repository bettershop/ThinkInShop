<?php
namespace app\admin\controller\plugin\coupon;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\Plugin\Plugin;
use app\common\Plugin\CouponPublicMethod;
use app\common\ExcelUtils;
use app\common\PC_Tools;

use app\admin\model\MchConfigModel;
use app\admin\model\MchModel;
use app\admin\model\CouponConfigModel;
use app\admin\model\CouponSnoModel;
use app\admin\model\OrderModel;
use app\admin\model\CouponActivityModel;

/**
 * 功能：PC店鋪优惠券
 * 修改人：DHB
 */
class Mchcoupon extends BaseController
{
    // 店铺主页是否显示优惠券
    public function IsOpenCoupon()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id,is_open_coupon')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id
        if($r_mch[0]['is_open_coupon'] == 1)
        {
            $is_open_coupon = 0;
        }
        else
        {
            $is_open_coupon = 1;
        }

        $sql_where_0 = array('store_id'=>$store_id,'id'=>$mch_id);
        $sql_update_0 = array('is_open_coupon'=>$is_open_coupon);
        $r_0 = Db::name('mch')->where($sql_where_0)->update($sql_update_0);
        if($r_0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $admin_name . ' 修改店铺主页是否显示优惠券开关成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array('code' => 200, 'message' => $message));
            exit;
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $admin_name . ' 修改店铺主页是否显示优惠券开关失败！';
            $this->Log($Log_content);
            $message = Lang('coupon.13');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }
    }

    // 优惠券列表
    public function Index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $storeType = addslashes(trim($this->request->param('storeType')));
        $accessId = addslashes(trim($this->request->param('accessId')));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $hid = addslashes($this->request->param('hid')); // 优惠券活动ID
        $name = addslashes($this->request->param('name')); // 优惠券名称
        $activity_type = addslashes($this->request->param('activityType')); // 活动类型
        $receive_type = addslashes($this->request->param('receiveType')); // 领取方式 0=手动领取 1=系统赠送
        $status = $this->request->param('isOverdue'); // 是否过期
        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10';
 
        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'hid'=>$hid,'name'=>$name,'issueUnit'=>'','activity_type'=>$activity_type,'receive_type'=>$receive_type,'status'=>$status,'page'=>$page,'pagesize'=>$pagesize,'exportType'=>$exportType);
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->store_coupons($array);
        return;
    }

    // 获取优惠券类型
    public function AddPage()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $storeType = addslashes(trim($this->request->param('storeType')));
        $accessId = addslashes(trim($this->request->param('accessId')));
        $mch_id = addslashes(trim($this->request->param('mchId')));
 
        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];

        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->add_store_coupons_page($store_id,$mch_id);
        return;
    }

    // 获取商品分类
    public function Fenlei()
    {
        $store_id = addslashes($this->request->param('storeId'));

        $coupon = new CouponPublicMethod();
        $list = $coupon->get_class($store_id,0,'');

        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 获取商品
    public function GetAssignGoods()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $mch_id = addslashes(trim($this->request->param('mchId')));

        $product_title = addslashes($this->request->param('product_title')); // 关键词
        $page = addslashes($this->request->param('page')); // 页码
        $pagesize = addslashes($this->request->param('pagesize')); // 每页显示多少条数据
        $page = $page ? $page : 1;

        $mch_id = PC_Tools::SelfOperatedStore($store_id);

        $coupon = new CouponPublicMethod();
        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'product_title'=>$product_title,'page'=>$page,'pagesize'=>$pagesize);
        $data = $coupon->get_mch_goods($array);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加/编辑优惠券
    public function Add()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$storeType = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));

    	$mch_id = addslashes($this->request->param('mchId')); // 店铺ID
    	$id = addslashes($this->request->param('id')); // 优惠券活动ID
    	$issue_unit = addslashes($this->request->param('issueUnit')); // 发行单位 0=商城 1=店铺
    	$activity_type = addslashes($this->request->param('activityType')); // 活动类型
    	$name = addslashes($this->request->param('name')); // 活动名称
    	$money = addslashes($this->request->param('money')); // 优惠券面值
    	$discount = addslashes($this->request->param('discount')); // 折扣值
    	$consumption_threshold_type = addslashes($this->request->param('consumption_threshold_type')); // 消费门口  1.无门槛  2.有门槛
    	$z_money = addslashes($this->request->param('zmoney')); // 满多少
    	$type = addslashes($this->request->param('type')); // 优惠券使用范围
    	$receive_type = addslashes($this->request->param('receiveType')); // 领取方式 0=手动领取 1=系统赠送
    	$circulation = addslashes($this->request->param('circulation')); // 发行数量
    	$end_time = addslashes($this->request->param('end_time')); // 活动结束时间
    	$receive = addslashes($this->request->param('limitCount')); // 领取限制
    	$Instructions = addslashes($this->request->param('instructions')); // 使用说明
    	$day = addslashes($this->request->param('day')); // 有效时间
    	$menu_list = addslashes($this->request->param('menuList')); // 选中的商品ID数据
    	$class_list = addslashes($this->request->param('classList')); // 选中的商品分类ID数据

    	$cover_map = addslashes($this->request->param('cover_map')); // 优惠券图片

        $admin_name = $this->user_list['name'];
        $shop_id = cache($accessId.'_'.$storeType);
        $operator = cache($accessId.'_uid');

        if($id != '' && $id != 0)
        {
            $data0 = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'id'=>$id,'issue_unit'=>$issue_unit,'activity_type'=>$activity_type,'name'=>$name,'money'=>$money,'discount'=>$discount,'consumption_threshold_type'=>$consumption_threshold_type,'z_money'=>$z_money,'type'=>$type,'receive_type'=>$receive_type,'circulation'=>$circulation,'end_time'=>$end_time,'receive'=>$receive,'Instructions'=>$Instructions,'day'=>$day,'menu_list'=>$menu_list,'class_list'=>$class_list,'cover_map'=>$cover_map,'shop_id'=>$shop_id,'operator'=>$operator,'source'=>2);

            $coupon = new CouponPublicMethod();
            $data = $coupon->modify_store_coupons($data0);
        }
        else
        {
            $data0 = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'id'=>$id,'issue_unit'=>$issue_unit,'activity_type'=>$activity_type,'name'=>$name,'money'=>$money,'discount'=>$discount,'consumption_threshold_type'=>$consumption_threshold_type,'z_money'=>$z_money,'type'=>$type,'receive_type'=>$receive_type,'circulation'=>$circulation,'end_time'=>$end_time,'receive'=>$receive,'Instructions'=>$Instructions,'day'=>$day,'menu_list'=>$menu_list,'class_list'=>$class_list,'cover_map'=>$cover_map,'shop_id'=>$shop_id,'operator'=>$operator,'source'=>2);

            $coupon = new CouponPublicMethod();
            $data = $coupon->add_store_coupons($data0);
        }

        $message = Lang('Success');
        return output(200, $message,$data);
    }
    
    // 编辑优惠券页面
    public function ModifyPage()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $hid = addslashes($this->request->param('hid')); // 优惠券活动ID

        $user_id = $this->user_list['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'hid'=>$hid,'name'=>'','issueUnit'=>'','activity_type'=>'','receive_type'=>'','status'=>'','page'=>1,'pagesize'=>10,'exportType'=>'');
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->store_coupons($array);
        return;
    }

    // 是否显示
    public function ActivityisDisplay()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $coupon_activity_id = addslashes($this->request->param('hid')); // 优惠券活动ID
        
        $shop_id = cache($accessId.'_'.$storeType);
        $operator = cache($accessId.'_uid');

        $array = array(
            'store_id'=>$store_id,
            'coupon_activity_id'=>$coupon_activity_id,
            'shop_id'=>$shop_id,
            'operator'=>$operator,
            'source'=>2,
        );
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->is_display($array);
    }

    // 获取用户信息
    public function GetGiveUserInfo()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $coupon_activity_str = addslashes($this->request->param('hid')); // 优惠券活动ID
        $name = addslashes($this->request->param('name')); // 会员ID/会员昵称/手机号
        $page = addslashes($this->request->param('pageNo')); /// 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10'; // 每页显示多少条数据

        $array = array(
            'store_id'=>$store_id,
            'coupon_activity_str'=>$coupon_activity_str,
            'name'=>$name,
            'page'=>$page,
            'pagesize'=>$pagesize,
        );
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->get_user($array);
    }

    // 赠送
    public function ReceiveUserCoupon()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $coupon_activity_str = addslashes($this->request->param('hid')); // 优惠券活动ID
        $user_str = addslashes($this->request->param('userIds')); // 用户ID字符串

        $admin_name = $this->user_list['name'];

        $shop_id = cache($accessId.'_'.$storeType);
        $operator = cache($accessId.'_uid');
        
        $array = array(
            'store_id'=>$store_id,
            'coupon_activity_str'=>$coupon_activity_str,
            'user_str'=>$user_str,
            'admin_name'=>$admin_name,
            'shop_id'=>$shop_id,
            'operator'=>$operator,
            'source'=>2,
        );
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->gift_user($array);
    }

    // 领取记录
    public function SeeCouponLogger()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $hid = addslashes($this->request->param('hid')); // 优惠券活动ID
        $keyWord = addslashes($this->request->param('keyWord')); // 会员昵称/订单编号
        $type = addslashes($this->request->param('type')); // 优惠券类型 1=免邮 2=满减 3=折扣 4=会员赠送
        $status = addslashes($this->request->param('status')); // 类型 0:未使用 1:使用中 2:已使用 3:已过期
        $page = addslashes($this->request->param('pageNo')); /// 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10'; // 每页显示多少条数据

        $user_id = $this->user_list['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'hid'=>$hid,'keyWord'=>$keyWord,'type'=>$type,'status'=>$status,'page'=>$page,'pagesize'=>$pagesize,'exportType'=>$exportType);
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->seeCouponLogger($array);
        return;
    }

    // 赠送记录
    public function SeeGiveCouponLogger()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $coupon_activity_id = addslashes($this->request->param('hid')); // 优惠券活动ID
        $keyWord = addslashes($this->request->param('keyWord')); // 会员昵称/订单编号
        $type = addslashes($this->request->param('type')); // 优惠券类型 1=免邮 2=满减 3=折扣 4=会员赠送
        $state = addslashes($this->request->param('state')); // 类型 0:未使用 1:使用中 2:已使用 3:已过期
        $page = addslashes($this->request->param('pageNo')); /// 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10'; // 每页显示多少条数据

        $user_id = $this->user_list['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'coupon_activity_id'=>$coupon_activity_id,'keyWord'=>$keyWord,'type'=>$type,'state'=>$state,'page'=>$page,'pagesize'=>$pagesize,'exportType'=>$exportType);
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->seeGiveCouponLogger($array);
        return;
    }

    // 删除优惠券
    public function DelCoupon()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $id = addslashes($this->request->param('id')); // 优惠券活动ID

        $user_id = $this->user_list['user_id'];
        $shop_id = cache($accessId.'_'.$storeType);
        $operator = cache($accessId.'_uid');

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id,'shop_id'=>$shop_id,'operator'=>$operator,'source'=>2);
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->delCoupon($array);
        return;
    }

    // 批量删除优惠券
    public function BatchDel()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $id = addslashes($this->request->param('ids')); // 优惠券活动ID

        $user_id = $this->user_list['user_id'];
        $shop_id = cache($accessId.'_'.$storeType);
        $operator = cache($accessId.'_uid');

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id,'shop_id'=>$shop_id,'operator'=>$operator,'source'=>2);
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->batchDel($array);
        return;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("plugin/Mchcoupon.log",$Log_content);
        return;
    }
}
