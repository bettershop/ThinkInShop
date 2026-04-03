<?php
namespace app\admin\controller\plugin\coupon;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\Plugin\CouponPublicMethod;
use app\common\GETUI\LaikePushTools;
use app\common\ExcelUtils;

use app\admin\model\CouponConfigModel;
use app\admin\model\CouponActivityModel;
use app\admin\model\CouponModel;
use app\admin\model\ProductClassModel;
use app\admin\model\ProductListModel;
use app\admin\model\BrandClassModel;
use app\admin\model\ConfigureModel;
use app\admin\model\UserModel;
use app\admin\model\CouponSnoModel;
use app\admin\model\OrderModel;
/**
 * 功能：优惠券
 * 修改人：DHB
 */
class Admincoupon extends BaseController
{   
    // 获取优惠券设置（插件配置-优惠券设置页面）
    public function GetCouponConfigInfo()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $typeList = array();
        $list = array();
        $r = CouponConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->select()->toArray();
        if ($r)
        {
            $r[0]['storeSwitchs'] = $r[0]['is_show'];
            $list = $r;
            $coupon_type = explode(',', $r[0]['coupon_type']);
            foreach($coupon_type as $k => $v)
            {
                if($v == 1)
                {
                    $typeList[$v] = '免邮券';
                }
                else if($v == 2)
                {
                    $typeList[$v] = '满减券';
                }
                else if($v == 3)
                {
                    $typeList[$v] = '折扣券';
                }
            }
        }
        $data = array('data'=>$list[0],'typeList'=>$typeList);
        $message = Lang('Success');
        return output('200',$message,$data);
    }
    
    // 获取优惠券设置
    public function GetCouponTypeList()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $typeList = array();
        $typeList[] = array('key'=>'1','value'=>'免邮券');
        $typeList[] = array('key'=>'2','value'=>'满减券');
        $typeList[] = array('key'=>'3','value'=>'折扣券');

        $data = array('typeList'=>$typeList);
        $message = Lang('Success');
        return output('200',$message,$data);
    }

    // 设置优惠券设置（插件配置-优惠券设置保存）
    public function AddCouponConfig()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));
        
        $is_status = addslashes($this->request->param('isOpen')); // 是否开启优惠券开关 0.关闭 1.开启
        $coupon_del = addslashes($this->request->param('isAutoClearCoupon'))?addslashes($this->request->param('isAutoClearCoupon')):1; // 是否自动清除过期优惠券
        $coupon_day = addslashes($this->request->param('autoClearCouponDay'))?addslashes($this->request->param('autoClearCouponDay')):1; // 优惠券删除天数
        $activity_del = addslashes($this->request->param('isAutoClearaAtivity'))?addslashes($this->request->param('isAutoClearaAtivity')):1; // 是否自动清除过期活动
        $activity_day = addslashes($this->request->param('autoClearaAtivityDay'))?addslashes($this->request->param('autoClearaAtivityDay')):1; // 优惠券活动删除天数
        $limit_type = addslashes($this->request->param('limitType'))?addslashes($this->request->param('limitType')):1; // 限领设置
        $coupon_type = addslashes($this->request->param('couponType')); // 优惠券类型设置

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");
        if ($coupon_del == 1)
        {
            if ($coupon_day <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 优惠券过期删除天数不正确';
                $this->Log($Log_content);
                $message = Lang('coupon.28');
                return output(109,$message);
            }
        }
        if ($activity_del == 1)
        {
            if ($activity_day <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 优惠券活动过期删除天数不正确';
                $this->Log($Log_content);
                $message = Lang('coupon.29');
                return output(109,$message);
            }
        }
        if($coupon_type)
        {
            
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择优惠券类型';
            $this->Log($Log_content);
            $message = Lang('coupon.1');
            return output(109,$message);
        }

        $r = CouponConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->select()->toArray();
        if ($r)
        {
            $sql_where = array('store_id'=>$store_id,'mch_id'=>0);
            $sql_update = array('is_status'=>$is_status,'coupon_del'=>$coupon_del,'coupon_day'=>$coupon_day,'activity_del'=>$activity_del,'activity_day'=>$activity_day,'limit_type'=>$limit_type,'coupon_type'=>$coupon_type,'modify_date'=>$time);
            $r_1 = Db::name('coupon_config')->where($sql_where)->update($sql_update);
            if ($r_1 == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了卡券插件的配置信息失败',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改优惠券设置失败！参数:'.json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('coupon.30');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了卡券插件的配置信息',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改优惠券设置成功';
                $this->Log($Log_content);
                $message = Lang('coupon.31');
                return output(200,$message);
            }
        }
        else
        {
            $sql = array('store_id'=>$store_id,'mch_id'=>0,'is_status'=>$is_status,'coupon_del'=>$coupon_del,'coupon_day'=>$coupon_day,'activity_del'=>$activity_del,'activity_day'=>$activity_day,'limit_type'=>$limit_type,'coupon_type'=>$coupon_type,'modify_date'=>$time);
            $r_1 = Db::name('coupon_config')->insert($sql);
            if ($r_1 == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了卡券插件的配置信息失败',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加优惠券设置失败！参数:'.json_encode($sql);
                $this->Log($Log_content);
                $message = Lang('coupon.32');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了卡券插件的配置信息',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加优惠券设置成功';
                $this->Log($Log_content);
                $message = Lang('coupon.33');
                return output(200,$message);
            }
        }
    }

    // 优惠券列表
    public function GetCouponCardInfo()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $mch_id = addslashes($this->request->param('mchId')); // 店铺ID
        $hid = addslashes($this->request->param('hid')); // 优惠券活动ID
        $name = addslashes($this->request->param('name')); // 优惠券名称
        $issueUnit = addslashes($this->request->param('issueUnit')); // 发型单位
        $activity_type = addslashes($this->request->param('activityType')); // 活动类型
        $receive_type = addslashes($this->request->param('receiveType')); // 领取方式 0=手动领取 1=系统赠送
        $status = $this->request->param('isOverdue'); // 是否过期
        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'hid'=>$hid,'name'=>$name,'issueUnit'=>$issueUnit,'activity_type'=>$activity_type,'receive_type'=>$receive_type,'status'=>$status,'page'=>$page,'pagesize'=>$pagesize,'exportType'=>$exportType);
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->store_coupons($array);
        return;
    }

    // 获取商品分类
    public function GetAssignGoodsClass()
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

        $product_title = addslashes($this->request->param('name')); // 关键词
        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页显示多少条数据
        $page = $page ? $page : 1;

        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        
        $coupon = new CouponPublicMethod();
        $list = $coupon->get_goods($store_id,$mch_id,'');
        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加/编辑优惠券
    public function AddCoupon()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));

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

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if($id != '' && $id != 0)
        {
            $r0 = CouponActivityModel::where(['store_id'=>$store_id,'id'=>$id])->field('mch_id')->select()->toArray();
            if($r0)
            {
                $mch_id = $r0[0]['mch_id'];
            }
            
            $data0 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id,'issue_unit'=>$issue_unit,'activity_type'=>$activity_type,'name'=>$name,'money'=>$money,'discount'=>$discount,'consumption_threshold_type'=>$consumption_threshold_type,'z_money'=>$z_money,'type'=>$type,'receive_type'=>$receive_type,'circulation'=>$circulation,'end_time'=>$end_time,'receive'=>$receive,'Instructions'=>$Instructions,'day'=>$day,'menu_list'=>$menu_list,'class_list'=>$class_list,'cover_map'=>$cover_map,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);

            $coupon = new CouponPublicMethod();
            $data = $coupon->modify_store_coupons($data0);
        }
        else
        {
            $data0 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id,'issue_unit'=>$issue_unit,'activity_type'=>$activity_type,'name'=>$name,'money'=>$money,'discount'=>$discount,'consumption_threshold_type'=>$consumption_threshold_type,'z_money'=>$z_money,'type'=>$type,'receive_type'=>$receive_type,'circulation'=>$circulation,'end_time'=>$end_time,'receive'=>$receive,'Instructions'=>$Instructions,'day'=>$day,'menu_list'=>$menu_list,'class_list'=>$class_list,'cover_map'=>$cover_map,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);

            $coupon = new CouponPublicMethod();
            $data = $coupon->add_store_coupons($data0);
        }

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 是否显示
    public function ActivityisDisplay()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $coupon_activity_id = addslashes($this->request->param('hid')); // 优惠券活动ID

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array(
            'store_id'=>$store_id,
            'coupon_activity_id'=>$coupon_activity_id,
            'shop_id'=>0,
            'operator'=>$operator,
            'source'=>1,
        );
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->is_display($array);
    }

    // 获取用户信息
    public function GetGiveUserInfo()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

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
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $coupon_activity_str = addslashes($this->request->param('hid')); // 优惠券活动ID
        $user_str = addslashes($this->request->param('userIds')); // 用户ID字符串

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array(
            'store_id'=>$store_id,
            'coupon_activity_str'=>$coupon_activity_str,
            'user_str'=>$user_str,
            'shop_id'=>0,
            'operator'=>$operator,
            'source'=>1,
        );
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->gift_user($array);
    }

    // 领取记录
    public function SeeCouponLogger()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $hid = addslashes($this->request->param('hid')); // 优惠券活动ID
        $keyWord = addslashes($this->request->param('keyWord')); // 会员昵称/订单编号
        $type = addslashes($this->request->param('type')); // 优惠券类型 1=免邮 2=满减 3=折扣 4=会员赠送
        $status = addslashes($this->request->param('status')); // 类型 0:未使用 1:使用中 2:已使用 3:已过期
        $page = addslashes($this->request->param('pageNo')); /// 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10'; // 每页显示多少条数据

        $shop_id = PC_Tools::SelfOperatedStore($store_id);

        $mch_id = '0,' . $shop_id; 

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'hid'=>$hid,'keyWord'=>$keyWord,'type'=>$type,'status'=>$status,'page'=>$page,'pagesize'=>$pagesize,'exportType'=>$exportType);
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->seeCouponLogger($array);
        return;
    }

    // 赠送记录
    public function SeeGiveCouponLogger()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $coupon_activity_id = addslashes($this->request->param('hid')); // 优惠券活动ID
        $keyWord = addslashes($this->request->param('keyWord')); // 会员昵称/订单编号
        $type = addslashes($this->request->param('type')); // 优惠券类型 1=免邮 2=满减 3=折扣 4=会员赠送
        $state = addslashes($this->request->param('state')); // 类型 0:未使用 1:使用中 2:已使用 3:已过期
        $page = addslashes($this->request->param('pageNo')); /// 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10'; // 每页显示多少条数据
        
        $shop_id = PC_Tools::SelfOperatedStore($store_id);
        $mch_id = '0,' . $shop_id; 

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'coupon_activity_id'=>$coupon_activity_id,'keyWord'=>$keyWord,'type'=>$type,'state'=>$state,'page'=>$page,'pagesize'=>$pagesize,'exportType'=>$exportType);
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->seeGiveCouponLogger($array);
        return;
    }

    // 删除优惠券
    public function DelCoupon()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $id = addslashes($this->request->param('hid')); // 优惠券活动ID

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $shop_id = PC_Tools::SelfOperatedStore($store_id);
        $mch_id = '0,' . $shop_id; 

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->delCoupon($array);
        return;
    }

    // 批量删除优惠券
    public function BatchDel()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $id = addslashes($this->request->param('ids')); // 优惠券活动ID

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $shop_id = PC_Tools::SelfOperatedStore($store_id);
        $mch_id = '0,' . $shop_id; 

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->batchDel($array);
        return;
    }

    // 获取自营店商品
    public function GetSpecifiedGoodsInfo()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $productTitle = addslashes($this->request->param('productTitle')); // 商品名称
        $block_id = $this->request->param('blockId');//楼层id
        $brand_id = $this->request->param('brandId');//品牌id
        $cid = $this->request->param('cid');//分类id
        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : 10;

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $mch_id = PC_Tools::SelfOperatedStore($store_id);

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
        if($brand_id)
        {
            $condition .= " and a.brand_id = '$brand_id' ";
        }
        if($cid)
        {
            $condition .= " and a.product_class like '%-$cid-%' ";
        }
        if($block_id)
        {
            //获取楼层商品id
            $sql_bp = "select b.product_id from lkt_block_home a INNER JOIN lkt_block_product b ON a.id = b.main_id where a.store_id = '$store_id' and a.id = '$block_id' and a.recycle  = 0";
            $res_bp = Db::query($sql_bp);
            if($res_bp)
            {   
                $res_a = array();
                foreach ($res_bp as $key => $value) 
                {
                    $res_a[] = $value['product_id'];
                }
                $bp_arr = implode(",",$res_a);
                $condition .= " and a.id not in ($bp_arr) ";
            }
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
        return output(200,$message, $data);
    }
    
    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("plugin/Admincoupon.log",$Log_content);
        return;
    }
}
