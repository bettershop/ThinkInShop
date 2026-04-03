<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cache;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Plugin\Plugin;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;
use app\common\Plugin\PluginUtils;
use app\common\Plugin\CouponPublicMethod;
use app\common\GETUI\LaikePushTools;
use app\common\Plugin\MchPublicMethod;
use app\common\WXTopicMsgUtils;
use app\common\Commission;
use app\common\third\logistics\LogisticsTool;
use app\common\Plugin\RefundUtils;
use app\common\alipay0\aop\test\AlipayReturn;
use app\common\DeliveryHelper;
use app\common\Plugin\LivingPublic;

use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\OrderConfigModel;
use app\admin\model\AdminModel;
use app\admin\model\ProductListModel;
use app\admin\model\NoticeModel;
use app\admin\model\ConfigModel;
use app\admin\model\UserModel;
use app\admin\model\UserFirstModel;
use app\admin\model\UserDistributionModel;
use app\admin\model\MchBrowseModel;
use app\admin\model\ExpressModel;
use app\admin\model\ConfigureModel;
use app\admin\model\CartModel;
use app\admin\model\MchModel;
use app\admin\model\ServiceAddressModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\ReturnRecordModel;
use app\admin\model\ReturnGoodsModel;
use app\admin\model\BuyAgainModel;
use app\admin\model\RecordModel;
use app\admin\model\StockModel;
use app\admin\model\AuctionProductModel;
use app\admin\model\SecondsRecordModel;
use app\admin\model\DistributionConfigModel;
use app\admin\model\IntegralConfigModel;
use app\admin\model\SecondsConfigModel;
use app\admin\model\DistributionGradeModel;
use app\admin\model\DistributionRecordModel;
use app\admin\model\SupplierModel;
use app\admin\model\SupplierOrderFrightModel;
use app\admin\model\SupplierAccountLogModel;


/**
 * 功能：用户订单类
 * 修改人：PJY
 */
class Order extends BaseController
{   
    /**
     * 订单下单日志
     * @var string
     */
    static $ORDER_PAYMENT_LOG_PATH = "common/order_payment.log";

    // 确认订单页码
    public function settlement()
    {
        //1.列出基础数据
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $this->store_id = $store_id;
        $this->store_type = $store_type;
        
        $language = trim($this->request->post('language')); // 语言
        $access_id = trim($this->request->post('access_id')); // 授权id
        $this->language = $language;
        $this->access_id = $access_id;
        
        $product1 = addslashes($this->request->post('product'));//  商品数组--------['pid'=>66,'cid'=>88]
        $cart_id = addslashes(trim($this->request->post('cart_id')));  //购物车id-- 12,13,123,
        $vipSource = addslashes($this->request->post('vipSource')); // 1.会员
        // $coupon_id = addslashes($this->request->post('coupon_id')); // 优惠券ID
        $type = addslashes($this->request->post('type'));// PS.预售
        $product_type = addslashes($this->request->post('product_type'));//产品类型，JP-竞拍商品,KJ-砍价商品
        $order_type = addslashes($this->request->post('order_type'));//产品类型，JP-竞拍商品,KJ-砍价商品

        $pluginName = 'NormalOrder';
        if($type == 'PS')
        {
            $pluginName = 'Presell_order';
        }
        elseif($type == 'FX')
        {
            $pluginName = 'Distribution';
        }
        elseif($type == 'MS' || $product_type == 'MS')
        {
            $pluginName = 'SecondsPublic';
        }
        elseif($type == 'JP' || $product_type == 'JP' || $order_type == 'JP')
        {
            $pluginName = 'Auction';
        }
        elseif($type == 'FS' || $product_type == 'FS')
        {
            $pluginName = 'FlashSalePublic';
        }
        elseif ($type == 'VI' || $product_type == 'VI' || $order_type == 'VI')
        { // 虚拟商品
            $pluginName = 'Virtual_order';
        }
        elseif ($type == 'ZB' || $product_type == 'ZB')
        { // 直播订单
            $pluginName = 'LivingPublic';
        }

        if($vipSource == 1)
        {
            $pluginName = 'Members';
        }

        $this->user = $this->user_list;
        $user_id = $this->user_list['user_id'];

        if (empty($pluginName))
        {
            ob_clean();
            $message = Lang('Request error');
            return output(ERROR_CODE_WLYC,$message);
        }
        
        $pluginObj = PluginUtils::getPlugin($pluginName);

        if (method_exists($pluginObj, 'settlement'))
        {
            $pluginObj->settlement($this);
            exit;
        }
        else
        {
            ob_clean();
            $message = Lang('tools.15');
            return output(ERROR_CODE_WLYC,$message);
        }
    }

    // 生成订单
    public function payment()
    {   
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $this->store_id = $store_id;
        $this->store_type = $store_type;
        $language = trim($this->request->post('language')); // 语言
        $access_id = trim($this->request->post('access_id')); // 授权id
        $this->language = $language;
        $this->access_id = $access_id;

        $vipSource = addslashes($this->request->post('vipSource')); // 1.会员
        $payTarget = addslashes($this->request->post('payTarget')); // 1.定金 2.尾款 3.全款
        
        $user_id = $this->user_list['user_id'];
        $this->user = $this->user_list;
        LaiKeLogUtils::log(self::$ORDER_PAYMENT_LOG_PATH, '下单开始');
        
        $type = trim($this->request->post('type')) ? $this->request->post('type') : 'GM'; // 订单类型
        $order_type = addslashes($this->request->post('order_type'));//产品类型，JP-竞拍商品
        if($order_type == 'JP')
        {
            $type = 'JP';
        }
        PluginUtils::invokeMethod($type, 'payment', $this);
        LaiKeLogUtils::log(self::$ORDER_PAYMENT_LOG_PATH, '下单结束');
    }

    public function miaosha_ok()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->post('store_type'));
        // 商城ID
        $sNo = trim($this->request->post('sNo'));
        $user_id = $this->user_list['user_id'];

        $r0 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id,'status'=>1])->select()->toArray();
        // 判断订单是否存在、有效
        if ($r0)
        {
            $msg_title = "【" . $sNo . "】秒杀成功！";
            $msg_content = Lang('order.13');

            /**买家付款成功通知*/
            $pusher = new LaikePushTools();
            $pusher->pushMessage($user_id,$msg_title, $msg_content, $store_id, '');
        }
        exit;
    }

    /**
     * 小程序订阅消息
     * 0.温馨提醒、订单编号、商品名称、订单金额、下单时间
     * 1.订单编号、商品名称、支付金额、支付时间
     * 2.订单编号、商品名称、签收时间
     */
    public function mini_msg($openid, $status, $p_name, $order)
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->post('store_type'));
        $language = trim($this->request->post('language')); // 语言


        $log = new LaiKeLogUtils();

        $page = 'pages/index/index';
        $time = date('Y-m-d h:i:s', time());

        $no_r = NoticeModel::where('store_id',$store_id)->select()->toArray();
        if (!$no_r || empty($openid))
        {
            return;                                                      
        }
        if ($status == 0)
        {
            $template_id = $no_r[0]['no_buy'];
            $keyword1 = array('value' => "您有一个订单待付款，快去结算吧！");
            $keyword2 = array('value' => $order->sNo);
            $keyword3 = array('value' => $p_name);
            $keyword4 = array('value' => $order->z_price);
            $keyword5 = array('value' => $order->add_time);
            $o_data = array('thing4' => $keyword1, 'character_string5' => $keyword2, 'thing1' => $keyword3, 'number2' => $keyword4, 'date6' => $keyword5);
        }
        else if ($status == 1)
        {
            $template_id = $no_r[0]['buy'];
            $keyword1 = array('value' => $order->sNo);
            $keyword2 = array('value' => $p_name);
            $keyword3 = array('value' => $order->z_price);
            $keyword4 = array('value' => $order->pay_time);
            $o_data = array('character_string1' => $keyword1, 'thing3' => $keyword2, 'amount4' => $keyword3, 'date2' => $keyword4);
        }
        else if ($status == 2)
        {
            $template_id = $no_r[0]['get'];
            $keyword1 = array('value' => $order['sNo']);
            $keyword2 = array('value' => $p_name);
            $keyword3 = array('value' => $order['time']);
            $o_data = array('character_string1' => $keyword1, 'thing2' => $keyword2, 'time3' => $keyword3);
        }
        //拼成规定的格式
        $r = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r)
        {
            $appid = $r[0]['appid'];
            $appsecret = $r[0]['appsecret'];
            $pusher = new LaikePushTools();
            $res = $pusher->Send_Prompt($appid, $appsecret, $openid, $page, $template_id, $o_data);
            $res = (array)json_decode($res);
            $log->log("common/mini_msg.log",__METHOD__ . ":" . __LINE__ . "小程序消息推送:" . json_encode($res));
            if ($res['errcode'] != 0)
            {
                $log->log("common/mini_msg.log",__METHOD__ . ":" . __LINE__ . "小程序消息推送失败:" . $res['errmsg']);
            }
            return;
        }
        return;
    }

    // 查询订单-优化后
    public function index0()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id

        $keyword = trim($this->request->param('ordervalue'))?trim($this->request->param('ordervalue')):(trim($this->request->param('keyword'))?trim($this->request->param('keyword')):trim($this->request->param('condition'))); // 商品名称/订单号
        $order_type = $this->request->param('queryOrderType'); // 类型 payment：待付款 send：待发货 receipt：待收货 evaluete：待评价
        $type = trim($this->request->param('order_type')); // 订单类型

        $page = trim($this->request->param('page')); // 页数
        $pagesize = trim($this->request->param('pageSize')); // 每页多少条数据
        $type = $type ? $type : 'GM';
        $pagesize = $pagesize ? $pagesize : '10';

        $user_id = $this->user_list['user_id'];

        $pagestart = 0;
        if (!empty($page) || $page != '')
        {
            $pagestart = ($page - 1) * 10;
        }

        $array = array('store_id'=>$store_id,'keyword'=>$keyword,'type'=>$type,'order_type'=>$order_type,'pagestart'=>$pagestart,'pagesize'=>$pagesize,'userid'=>$user_id);
        $data = DeliveryHelper::user_order_index0($array);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 查询订单
    public function index()
    {
        $store_id = trim($this->request->param('store_id'));

        $this->store_id = $store_id;
        $access_id = trim($this->request->param('access_id')); // 授权id
        $keyword = trim($this->request->param('ordervalue'))?trim($this->request->param('ordervalue')):(trim($this->request->param('keyword'))?trim($this->request->param('keyword')):trim($this->request->param('condition'))); // 商品名称/订单号

        // if($keyword != '')
        // {
        //     $type = substr($keyword, 0, 2);
        // }
        // else
        // {
        //     $type = trim($this->request->param('order_type')); // 订单类型
        //     if(trim($this->request->param('type')) != '')
        //     {
        //         $type = trim($this->request->param('type'));
        //     }
        // }
        $type = trim($this->request->param('order_type')); // 订单类型
        if(trim($this->request->param('type')) != '')
        {
            $type = trim($this->request->param('type'));
        }

        $this->keyword = $keyword;
        $this->type = $type?$type:'GM';

        $page = trim($this->request->param('page'))?$this->request->param('page'):$this->request->param('pageNo'); // 页数
        $this->page = $page;
        $lktlog = new LaiKeLogUtils();
        $this->lktlog = $lktlog;

        // 获取信息
        $order_type = $this->request->param('queryOrderType') ? $this->request->param('queryOrderType') : false; // 类型
        $this->order_type = $order_type;
        if (!empty($page) || $page != '')
        {

            $pagestart = ($page - 1) * 10;
        }
        else
        {
            $pagestart = 0;
        }
        $this->pagestart = $pagestart;

        $order_failure = 2;
        $company = "hour";
        $r_2 = OrderConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r_2)
        {
            $order_failure = $r_2[0]['order_failure']; // 订单失效
            // if ($r_2[0]['company'] == '天')
            // {
            //     $company = "day";
            // }
            // else
            // {
            //     $company = "hour";
            // }
        }

        $this->order_failure = $order_failure;
        $this->company = $company;
        $userid = $this->user_list['user_id'];
        $this->userid = $userid;
        $this->Servertype = 'user';
        $user_id = $this->user_list['user_id'];
        $this->user = $this->user_list;

        $data = PluginUtils::invokeMethod('','order_index',$this);

        $order = $data['order'];
        $order_failure = $data['order_failure'];
        $company = $data['company'];
        $order_num = $data['order_num'];
        $res_order = $data['res_order'];
        $isDistribution = $data['isDistribution'];
        ob_clean();
        if(empty($order))
        {
            $order = array();
        }
        $message = Lang('Success');
        return output(200,$message,array('order' => $order, 'order_failure' => $order_failure, 'company' => $company,'order_num'=>$order_num,'res_order'=>$res_order,'isDistribution'=>$isDistribution));
    }

    // 订单详情确认收货
    public function recOrder()
    {
        $this->receivetype = 'detail';
        //统一走到普通订单的收货逻辑里面和其他的插件逻辑高度相似 ；订单类型留空 默认获取到的是 normal_order插件
        //todo 前端直接传入订单类型
        PluginUtils::invokeMethod('', 'receive_good', $this);
    }

    /*
     *  订单列表确认收货
     */
    public function ok_Order()
    {
        $this->receivetype = 'list';
        //统一走到普通订单的收货逻辑里面和其他的插件逻辑高度相似 ；订单类型留空 默认获取到的是 normal_order插件
        //todo 前端直接传入订单类型
        PluginUtils::invokeMethod('', 'receive_good', $this);
    }

    // 查看物流
    public function logistics()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->post('store_type'));
        $store_id = trim($this->request->param('store_id'));
        $access_id = trim($this->request->post('access_id')); // 授权id

        // 获取信息
        $id = trim($this->request->post('id'));// 订单号
        $details = $this->request->post('details'); // 订单详情id
        $o_source = trim($this->request->post('o_source'));// 来源 1订单 2售后

        // 根据微信id,查询用户id
        $access = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])
                            ->field('user_id')
                            ->select()
                            ->toArray();
        if (!empty($access))
        {
            if($o_source == 1)
            {
                $array = array('store_id'=>$store_id,'sNo'=>$id,'o_source'=>$o_source);
                $list = PC_Tools::View_logistics($array);

                $data = array('list'=>$list);
                $message = Lang('Success');
                return output(200,$message,$data);
            }
            else
            {
                $array = array('store_id'=>$store_id,'sNo'=>$id,'o_source'=>$o_source);
                $list = PC_Tools::View_logistics($array);

                $data = array('list'=>$list);
                $message = Lang('Success');
                return output(200,$message,$data);
            } 
        }
        else
        {
            ob_clean();
            $message = Lang('Illegal invasion');
            return output(400,$message);
        }
    }

    // 取消订单
    public function removeOrder()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        //事务开启
        Db::startTrans();

        // 获取信息
        $access_id = trim($this->request->post('access_id')); // 授权id
        $id = trim($this->request->post('order_id'));// 订单id
        $lktlog = new LaiKeLogUtils();
        // 根据微信id,查询用户id
        $access = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('money')->select()->toArray();
        if (!empty($access))
        {
            $user_id = $this->user_list['user_id'];
            $money = $access[0]['money'];
            // 根据订单id,查询订单列表(订单号)
            $r = OrderModel::where(['store_id'=>$store_id,'id'=>$id,'user_id'=>$user_id])
                          ->field('sNo,status,z_price,otype,mch_id,coupon_id,subtraction_id')
                          ->select()
                          ->toArray();
            if ($r)
            {
                $sNo = $r[0]['sNo']; // 订单号
                $status = $r[0]['status']; // 订单状态
                $z_price = $r[0]['z_price']; // 订单总价
                $mch_id = explode(',',trim($r[0]['mch_id'],',')); // 店铺id
                $coupon_id = $r[0]['coupon_id']; // 优惠券id
                $subtraction_id = $r[0]['subtraction_id']; // 满减活动ID
                $otype = $r[0]['otype'];
                if ($otype == 'vipzs')
                {
                    $up_user_frist_sql = UserFirstModel::where(['sNo'=>$sNo,'store_id'=>$store_id])->find();
                    $up_user_frist_sql->is_use = 0;
                    $up_user_frist_sql->sNo = '';
                    $rr = $up_user_frist_sql->save();
                    if (!$rr)
                    {
                        $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改用户会员第一次开会员信息失败！sNo:" . $sNo);
                    }
                }
                else if ($otype == 'FS')
                {
                    $sql_f = "update lkt_flashsale_record set is_delete = 1 where sNo = '$sNo' ";
                    $r_f = Db::execute($sql_f);
                    if($r_f)
                    {
                        $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "删除限时折扣记录失败！sNo:" . $sNo);
                    }
                }
                switch ($status)
                {
                    case 0:
                        $event = $user_id . '取消订单号为' . $sNo . '的订单';
                        break;
                    case 1:
                        $sql = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->find();
                        $sql->money = Db::raw('money + '.$z_price);
                        $r_r = $sql->save();
                        if (!$r_r)
                        {
                            $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "余额修改失败！user_id:" . $user_id);
                        }

                        $event = $user_id . '取消订单号为' . $sNo . '的订单';
                        $sql = new RecordModel();
                        $sql->store_id = $store_id;
                        $sql->user_id = $user_id;
                        $sql->money = $z_price;
                        $sql->oldmoney = $money;
                        $sql->add_date = date("Y-m-d H:i:s");
                        $sql->event = $event;
                        $sql->type = 23;
                        $ist_res = $sql->save();
                        if ($sql->id < 1)
                        {
                            $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "添加记录失败！user_id:" . $user_id);
                        }
                        break;
                    case 2:
                        ob_clean();
                        $message = Lang('order.15');
                        return output(ERROR_CODE_DDYFHBNJXXG,$message);
                        break;
                    default:
                        break;
                }

                $sql = OrderModel::where(['store_id'=>$store_id,'id'=>$id,'user_id'=>$user_id])->find();
                $sql->status = 7;
                $sql->cancel_method = 1;
                $r1 = $sql->save();
                if (!$r1)
                {
                    $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改订单状态失败！id:" . $id);
                }
                $sql = "update lkt_order_details set r_status = 7 where store_id = '$store_id' and r_sNo = '$sNo'";
                $r2 = Db::execute($sql);
                if (!$r2)
                {
                    $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改订单状态失败！sql:" . $sql);
                }

                if ($r2 && $r1)
                {
                    if($coupon_id != '' && $coupon_id != '0,0')
                    {
                        if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
                        {
                            $coupon = new CouponPublicMethod();
                            $coupon_list = explode(',',$coupon_id); // 订单详情使用的优惠券ID字符串 转数组
                            foreach ($coupon_list as $k_coupon => $v_coupon)
                            {
                                if($v_coupon != 0)
                                { // 使用了优惠券
                                    $r_coupon0 = $coupon->update_coupon($store_id, $user_id, $v_coupon,0);
                                    if ($r_coupon0 == 2)
                                    {
                                        //回滚删除已经创建的订单
                                        Db::rollback();
                                        ob_clean();
                                        $message = Lang('Abnormal business');
                                        return output(ERROR_CODE_YWYC,$message);
                                    }
                                    $coupon_Log_content = '会员' . $user_id . '取消订单号为' . $sNo . '时,修改优惠券ID为' . $v_coupon . '的状态！';
                                    $coupon->couponLog($v_coupon, $coupon_Log_content);
        
                                    $r_coupon1 = $coupon->coupon_sno($store_id, $user_id, $v_coupon,$sNo,'update');
                                    if ($r_coupon1 == 2)
                                    {
                                        //回滚删除已经创建的订单
                                        Db::rollback();
                                        ob_clean();
                                        $message = Lang('order.16');
                                        return output(ERROR_CODE_XGSB,$message);
                                    }
                                }
                            }
                        }
                    }

                    if ($otype == 'ZB')
                    {
                        $sql_1 = "select d.*,p.id as Goods_id from lkt_order_details as d left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id left join lkt_living_product as l on d.living_room_id = l.living_id and c.id = l.config_id where d.store_id = '$store_id' and d.r_sNo = '$sNo' and l.recycle = 0 ";
                        $r_1 = Db::query($sql_1);
                        foreach ($r_1 as $k => $v)
                        {
                            $pid = $v['Goods_id'];
                            $Goods_num = $v['num'];
                            $attribute_id = $v['sid'];
                            $living_room_id = $v['living_room_id'];

                            $array = array('store_id'=>$store_id,'user_id'=>$user_id,'living_id'=>$living_room_id,'sNo'=>$sNo,'pro_id'=>$pid,'config_id'=>$attribute_id,'num'=>$Goods_num,'source'=>1);
                            $res_stock = LivingPublic::Return_inventory($array);
                        }
                    }
                    else
                    {
                        $sql_1 = "select d.*,p.id as Goods_id from lkt_order_details as d left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where d.store_id = '$store_id' and d.r_sNo = '$sNo'";
                        $r_1 = Db::query($sql_1);
                        foreach ($r_1 as $k => $v)
                        {
                            $pid = $v['Goods_id'];
                            $Goods_num = $v['num'];
                            $attribute_id = $v['sid'];

                            $array = array('store_id'=>$store_id,'supplier_id'=>$v['supplier_id'],'pid'=>$pid,'sid'=>$attribute_id,'num'=>$Goods_num,'type'=>3,'user_id'=>$user_id);
                            $res_stock = PC_Tools::Modify_inventory($array);
                            if($res_stock == -1)
                            {
                                Db::rollback();
                                $message = Lang('operation failed');
                                echo json_encode(array('code' => 109, 'message' => $message));
                                exit;
                            }
                        }
                        
                        //如果为竞拍商品则将竞拍记录中的订单编号置为NULL并删除订单
                        $jp_type = substr($sNo, 0, 2);
                        if ($jp_type == 'JP')
                        {
                            $sql_0 = AuctionProductModel::where(['store_id'=>$store_id,'trade_no'=>$sNo])->find();
                            $sql_0->trade_no = null;
                            $res_0 = $sql_0->save();

                            $sNo = $r[0]['sNo']; // 订单号
                            $sql_1 = "update lkt_order_details set r_status = 7 where store_id = '$store_id' and r_sNo = '$sNo'";
                            $res_1 = Db::execute($sql_1);
                            if (!$res_1)
                            {
                                $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改订单详情状态失败！sql:" . $sql);
                            }
                            $sql_2 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->find();
                            $sql_2->status = 7;
                            $res_2 = $sql_2->save();
                            if (!$res_2)
                            {
                                $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改订单状态失败！sNo:" . $sNo);
                            }
                            if (!$res_0  || !$res_1 || !$res_2)
                            {
                                Db::rollback();

                                ob_clean();
                                $message = Lang('order.17');
                                return output(ERROR_CODE_CZSB,$message);
                            }
                        }
                        if ($otype == "MS")
                        {
                            //如果是秒杀订单 还原秒杀数量 ， 和redis
                            $ms_record_res = SecondsRecordModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id])->select()->toArray();
                            if (!empty($ms_record_res))
                            {

                                $activity_id = $ms_record_res[0]['activity_id'];
                                $pro_id = $ms_record_res[0]['pro_id'];
                                $attr_id = $ms_record_res[0]['attr_id'];
                                $num = $ms_record_res[0]['num'];
                                // 移除秒杀订单  删除秒杀记录
                                $up_ms_record = SecondsRecordModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id])->find();
                                $up_ms_record->is_delete = 1;
                                $up_ms_record->save();
                                //回滚库存
                                $up_sec_num_sql = "update lkt_seconds_pro set num = num + 1 where store_id = '$store_id' and activity_id = $activity_id and attr_id = $attr_id ";
                                $rrr = Db::execute($up_sec_num_sql);
                                if (!$rrr)
                                {
                                    $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改秒杀商品库存失败！sql:" . $sql);
                                }

                            }
                        }
                        else if ($otype == "IN")
                        {   
                            $sql_in_where = array('store_id' => $store_id,'id' => $r_1[0]['p_id']);
                            $sql_in_update = array('num' => Db::raw('num+'.$Goods_num));
                            $r_in = Db::name('integral_goods')->where($sql_in_where)->update($sql_in_update);
                        }
                        
                        if ($otype == 'PS')
                        {
                            $sql_se_where = array('product_id' => $pid);
                            $sql_se_update = array('surplus_num' => Db::raw('surplus_num+'.$Goods_num));
                            $r_se = Db::name('pre_sell_goods')->where($sql_se_where)->update($sql_se_update);
                        }
                    }

                    foreach ($mch_id as $k => $v)
                    {
                        $message_4 = "用户还需再考虑考虑，关闭了".$sNo."的订单！";
                        $message_logging_list4 = array('store_id'=>$store_id,'mch_id'=>$v,'gongyingshang'=>0,'type'=>4,'parameter'=>$sNo,'content'=>$message_4);
                        PC_Tools::add_message_logging($message_logging_list4);
                    }

                    Db::commit();
                    ob_clean();
                    $message = Lang('Success');
                    return output(200,$message);
                }
                else
                {
                    Db::rollback();
                    ob_clean();
                    $message = Lang('Abnormal business');
                    return output(ERROR_CODE_YWYC,$message);
                }
            }
            else
            {
                Db::rollback();
                ob_clean();
                $message = Lang('Parameter error');
                return output(ERROR_CODE_CSCW,$message);
            }
        }
        else
        {
            Db::rollback();

            ob_clean();
            $message = Lang('Illegal invasion');
            return output(ERROR_CODE_FFRQ,$message);
        }
        return;
    }

    // 订单详情-优化后
    public function order_details0()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->post('store_type'));
        $access_id = trim($this->request->post('access_id'));
        // 获取信息
        $id = trim($this->request->post('order_id')); // 订单id

        $array = array('store_id'=>$store_id,'access_id'=>$access_id,'id'=>$id);
        $data = DeliveryHelper::app_order_details0($array);
        return;
    }

    // 订单详情
    public function order_details()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->post('store_type'));
        $access_id = trim($this->request->post('access_id'));
        // 获取信息
        $id = trim($this->request->post('order_id')); // 订单id
        $type = trim($this->request->post('type')); // 订单类型
        $lktlog = new LaiKeLogUtils();

        $this->lktlog = $lktlog;
        $this->store_id = $store_id;
        $this->access_id = $access_id;
        $this->id = $id;
        $this->user = $this->user_list;
        $this->userid = $this->user_list['user_id'];
        PluginUtils::invokeMethod('','app_order_details',$this);

        return;
    }

    /**
     * 售后详情数据
     */
    function Returndetail()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $id = trim($this->request->post('id')); // 售后订单id 
        $pid = trim($this->request->post('pid'));
        $details_id = trim($this->request->post('orderDetailId'));//订单详情id
        $goodsInfo = array(); // 商品信息
        $AddPurchase = array();
        if($id)
        {
            //查询详情订单信息
            $sel_sql = "select a.*,b.p_name,b.num,b.size,b.p_price,c.img,d.id as Goods_id,d.mch_id from lkt_return_order as a left join lkt_order_details as b on a.p_id = b.id left join lkt_configure as c on b.sid = c.id left join lkt_product_list as d on c.pid = d.id where a.store_id = $store_id and a.id = $id";
            $res = Db::query($sel_sql);
            if(empty($res))
            {
                $message = Lang('Parameter error');
                echo json_encode(array('code' => ERROR_CODE_CSCW, 'message' => $message));
                exit;
            }
            $details_id = $res[0]['p_id'];//订单详情ID
            $pid = $res[0]['Goods_id'];
            $mch_id = $res[0]['mch_id'];
            $sNo = $res[0]['sNo'];
            $goodsInfo['goodsName'] = $res[0]['p_name'];
            $goodsInfo['id'] = $res[0]['Goods_id'];
            $goodsInfo['img'] = ServerPath::getimgpath($res[0]['img'], $store_id);
            $goodsInfo['num'] = $res[0]['num'];
            $goodsInfo['price'] = $res[0]['p_price'];
            $goodsInfo['size'] = $res[0]['size'];
            $mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name,logo,head_img')->select()->toArray();
            $goodsInfo['mchId'] = $mch_id;
            $goodsInfo['mchName'] = $mch[0]['name'];
            $goodsInfo['mchLogo'] = ServerPath::getimgpath($mch[0]['logo'], $store_id);
            $goodsInfo['headimg'] = ServerPath::getimgpath($mch[0]['head_img'], $store_id);
        }
        else
        {
            //查询详情订单信息
            $sel_sql = "select a.*,b.p_name,b.num,b.size,b.p_price,c.img,d.id as Goods_id,d.mch_id from lkt_return_order as a right join lkt_order_details as b on a.p_id = b.id left join lkt_configure as c on b.sid = c.id left join lkt_product_list as d on c.pid = d.id where a.store_id = $store_id and b.id = $details_id order by a.id desc";
            $res = Db::query($sel_sql);
            if(empty($res))
            {
                $message = Lang('Parameter error');
                echo json_encode(array('code' => ERROR_CODE_CSCW, 'message' => $message));
                exit;
            }
            $id = $res[0]['id'];
            $pid = $res[0]['Goods_id'];
            $mch_id = $res[0]['mch_id'];
            $sNo = $res[0]['sNo'];
            $otype = substr($sNo, 0, 2);

            $goodsInfo['goodsName'] = $res[0]['p_name'];
            $goodsInfo['id'] = $res[0]['Goods_id'];
            $goodsInfo['img'] = ServerPath::getimgpath($res[0]['img'], $store_id);
            $goodsInfo['num'] = $res[0]['num'];
            $goodsInfo['price'] = $res[0]['p_price'];
            $goodsInfo['size'] = $res[0]['size'];
            $mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name,logo,head_img')->select()->toArray();
            $goodsInfo['mchId'] = $mch_id;
            $goodsInfo['mchName'] = $mch[0]['name'];
            $goodsInfo['mchLogo'] = ServerPath::getimgpath($mch[0]['logo'], $store_id);
            $goodsInfo['headimg'] = ServerPath::getimgpath($mch[0]['head_img'], $store_id);

            if($otype == 'FS')
            { // 限时折扣
                $sql_d = "select b.p_name as goodsName,d.id,c.img,b.num,b.p_price as price,b.size from lkt_order_details as b left join lkt_configure as c on b.sid = c.id left join lkt_product_list as d on c.pid = d.id where b.store_id = '$store_id' and r_sNo = '$sNo' and b.is_addp = 1 ";
                $r_d = Db::query($sql_d);
                if($r_d)
                {
                    foreach($r_d as $k_d => $v_d)
                    {
                        $v_d['img'] = ServerPath::getimgpath($v_d['img'], $store_id);
                        $v_d['mchId'] = $mch_id;
                        $v_d['mchName'] = $mch[0]['name'];
                        $v_d['mchLogo'] = ServerPath::getimgpath($mch[0]['logo'], $store_id);
                        $v_d['headimg'] = ServerPath::getimgpath($mch[0]['head_img'], $store_id);

                        $AddPurchase[] = $v_d;
                    }
                }
            }
        }
        $sql_o = "select currency_symbol,exchange_rate,currency_code from lkt_order where store_id = '$store_id' and sNo = '$sNo' ";
        $r_o = Db::query($sql_o);
        if($r_o)
        {
            $currency_symbol = $r_o[0]['currency_symbol'];
            $exchange_rate = $r_o[0]['exchange_rate'];
            $currency_code = $r_o[0]['currency_code'];
        }
        
        $send_info = array(); // 买家回寄物流信息
        $return_info = array(); // 商家退换物流信息

        //查询买家回寄信息
        $return_res = ReturnGoodsModel::where(['oid'=>$details_id,'store_id'=>$store_id])->select()->toArray();
        if (!empty($return_res))
        {
            $send_info = $return_res[0];

            if (count($return_res) > 1)
            {
                //查询卖家退换信息
                $return_info = $return_res[1];
            }
        }

        $info = array();

        //退款信息
        $info['re_time'] = $res[0]['re_time'];//申请时间
        $price = $res[0]['real_money'];//退款金额
        $info['p_price'] = $price;//退款金额
        $info['r_content'] = $res[0]['r_content'];//拒绝原因
        if ($info['p_price'] == '0.00' || $info['p_price'] == 0)
        {
            $info['p_price'] = $res[0]['re_apply_money'];
        }
        $info['re_type'] = $res[0]['re_type'];//售后类型1
        $info['p_name'] = $res[0]['p_name'];//售后商品名称
        $info['r_sNo'] = $res[0]['sNo'];//售后订单
        $info['type'] = $res[0]['r_type'];//售后类型
        $info['content'] = $res[0]['content'];//退货原因
        $info['r_write_off_num'] = $res[0]['r_write_off_num']; // 虚拟商品退款核销次数
        $re_photo = unserialize($res[0]['re_photo']);//凭证
        if($res[0]['r_type'] == 15)
        {
            $info['content'] = '极速退款'; // 退货原因
        }

        $info['re_photo'] = array();
        if (!empty($re_photo))
        {
            foreach ($re_photo as $k => $v)
            {
                $info['re_photo'][$k] = ServerPath::getimgpath($v, $store_id); // 获取图片路径
            }
        }
        
        // 根据商品id，查询商品信息
        $r0 = ProductListModel::where(['store_id'=>$store_id,'id'=>$pid])->field('mch_id,gongyingshang,product_title')->select()->toArray();
        if ($r0)
        {
            if($r0[0]['gongyingshang'] != 0)
            {
                $r1 = SupplierModel::where(['store_id'=>$store_id,'id'=>$r0[0]['gongyingshang']])->field('province,city,area,address,contacts,cpc,contact_phone')->select()->toArray();
                if($r1)
                {
                    $array_address = array('cpc'=>$r1[0]['cpc'],'sheng'=>$r1[0]['province'],'shi'=>$r1[0]['city'],'xian'=>$r1[0]['area'],'address'=>$r1[0]['address'],'code'=>'');
                    $address = PC_Tools::address_translation($array_address);
                    $name = $r1[0]['contacts'];
                    $phone = $r1[0]['contact_phone'];
                }
                else
                {
                    ob_clean();
                    $message = Lang('Illegal invasion');
                    return output(ERROR_CODE_FFRQ,$message);
                }
            }
            else
            {
                $mch_id = $r0[0]['mch_id']; // 店铺ID
                // 根据店铺ID，查询管理员信息
                $r1 = AdminModel::where(['store_id'=>$store_id,'shop_id'=>$mch_id])->field('id')->select()->toArray();
                if ($r1)
                { // 存在，代表是自营商品
                    // 获取信息
                    $r_1 = ServiceAddressModel::where(['store_id'=>$store_id,'uid'=>'admin','is_default'=>1])->field('address_xq,name,tel')->select()->toArray();
                    $address = $r_1[0]['address_xq'];
                    $name = $r_1[0]['name'];
                    $phone = $r_1[0]['tel'];
                }
                else
                {
                    $r1 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('realname,cpc,tel,sheng,shi,xian,address,code')->select()->toArray();
                    if ($r1)
                    {
                        $array_address = array('cpc'=>$r1[0]['cpc'],'sheng'=>$r1[0]['sheng'],'shi'=>$r1[0]['shi'],'xian'=>$r1[0]['xian'],'address'=>$r1[0]['address'],'code'=>$r1[0]['code']);
                        $address = PC_Tools::address_translation($array_address);
                        $name = $r1[0]['realname'];
                        $phone = $r1[0]['tel'];
                    }
                    else
                    {
                        ob_clean();
                        $message = Lang('Illegal invasion');
                        return output(ERROR_CODE_FFRQ,$message);
                    }
                }
            }
            $record = array();
            //查询售后记录
            $record_res = ReturnRecordModel::where(['store_id'=>$store_id,'p_id'=>$id])->select()->toArray();
            if (!empty($record_res))
            {
                foreach ($record_res as $k => $v)
                {
                    if ($k < count($record_res) - 1)
                    {
                        $record[$k] = $v;
                    }
                }
            }
        }
        else
        {
            ob_clean();
            $message = Lang('order.20');
            return output(ERROR_CODE_SPXXCW,$message);
        }

        $store_info = array(); // 售后地址
        $store_info['address'] = $address;
        $store_info['name'] = $name;
        $store_info['phone'] = $phone;
        ob_clean();
        $message = Lang('Success');
        $data = array('info' => $info, 'store_info' => $store_info, 'record' => $record, 'send_info' => $send_info,'goodsInfo'=>$goodsInfo, 'return_info' => $return_info,'addGoodsList'=>$AddPurchase);
        return output(200,$message,$data);
    }
    // 撤销审核
    public function Cancellation_of_application()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->post('store_type'));
        $id = trim($this->request->post('id'));

        $userid = $this->user_list['user_id'];
        $sqlcc = ReturnOrderModel::where(['store_id'=>$store_id,'user_id'=>$userid,'id'=>$id])->find();
        $r = $sqlcc->delete();
        if($r)
        {
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $message = Lang('Busy network');
            return output(ERROR_CODE_CZSB,$message);
        }
    }

    function base64_image_content($base64_image_content, $path)
    {
        //匹配出图片的格式
        if (preg_match('/^(data:\s*image\/(\w+);base64,)/', $base64_image_content, $result))
        {
            $type = $result[2];
            $new_file = $path . "/" . date('Ymd', time()) . "/";
            if (!file_exists($new_file))
            {
                //检查是否有该文件夹，如果没有就创建，并给予最高权限
                mkdir($new_file, 0700);
            }
            $new_file = $new_file . time() . ".{$type}";
            if (file_put_contents($new_file, base64_decode(str_replace($result[1], '', $base64_image_content))))
            {
                return '/' . $new_file;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    // 退货信息
    public function ReturnDataList()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));

        $keyword = trim($this->request->post('keyword'));
        $order_type = trim($this->request->post('order_type'));
        $type = trim($this->request->post('type'));
        $pages = trim($this->request->post('page'));
        $pagesize = trim($this->request->post('pagesize'));
        $pagesize = $pagesize ? $pagesize : '10';
        if ($pages)
        {
            $start = ($pages - 1) * 10;
        }
        else
        {
            $start = 0;
        }

        $condition = '';
        if(!empty($keyword))
        {
            $keyword_0 = Tools::FuzzyQueryConcatenation($keyword);
            $condition = " and (a.sNo like $keyword_0 or b.p_name like $keyword_0)";
        }

        if($type == 'PS')
        {
            $condition .= " and b.r_sNo like '%PS%' ";
        }
        else if($type == 'FS')
        {
            $condition .= " and b.r_sNo like '%FS%' ";
        }
        else if($type == 'ZB')
        {
            $condition .= " and b.r_sNo like '%ZB%' ";
        }
        else
        {
            if($order_type != '')
            {
                $condition .= " and b.r_sNo like '%$order_type%' ";
            }
            else
            {
                $condition .= " and (b.r_sNo like '%GM%' or b.r_sNo like '%PT%' or b.r_sNo like '%VI%') ";
            }
        }
        $userid = $this->user_list['user_id'];
        $sql = "select count(a.id) as num from lkt_return_order as a left join lkt_order_details as b on a.p_id = b.id left join lkt_configure as c on b.sid = c.id left join lkt_product_list as d on c.pid = d.id 
                where a.store_id = '$store_id' $condition and a.user_id = '$userid' and a.re_type > 0";

        $res_num = Db::query($sql);
        $count = is_array($res_num)?$res_num[0]['num']:0;
        $list = array();
        if($count > 0)
        {
            // $sqlcc = "select a.*,b.id as detailId,b.p_id as goodsId,b.p_name,b.p_price,b.num,b.size,d.id as pro_id,c.unit from lkt_return_order as a left join lkt_order_details as b on a.p_id = b.id left join lkt_configure as c on b.sid = c.id left join lkt_product_list as d on c.pid = d.id where a.store_id = '$store_id' and a.user_id = '$userid' $condition and a.re_type > 0 order by case   
            //                       when a.r_type='1' then 1
            //                       when a.r_type='11' then 2
            //                       when a.r_type='0' then 3
            //                       when a.r_type='3' then 4
            //                       when a.r_type='2' then 5
            //                       when a.r_type='5' then 6
            //                       when a.r_type='8' then 7
            //                       when a.r_type='10' then 8
            //                       when a.r_type='4' then 9
            //                       when a.r_type='9' then 10
            //                       when a.r_type='12' then 11
            //                   end limit $start,$pagesize";
            $sqlcc = "select a.*,b.id as detailId,b.p_id as goodsId,b.p_name,b.p_price,b.num,b.size,d.id as pro_id,c.unit from lkt_return_order as a left join lkt_order_details as b on a.p_id = b.id left join lkt_configure as c on b.sid = c.id left join lkt_product_list as d on c.pid = d.id where a.store_id = '$store_id' and a.user_id = '$userid' $condition and a.re_type > 0 order by a.re_time desc limit $start,$pagesize";
            $listcc = Db::query($sqlcc);
            if ($listcc)
            {
                $times = 0;
                foreach ($listcc as $k => $v)
                {
                    $v['p_price'] = round($v['p_price'],2);
                    $p_id = $v['pro_id'];
                    $r_sNo = $v['sNo'];
                    $r = OrderModel::where(['store_id'=>$store_id,'sNo'=>$r_sNo])->field('id,currency_symbol,exchange_rate,currency_code')->select()->toArray();
                    $order_id = $r[0]['id'];
                    $v['currency_symbol'] = $r[0]['currency_symbol'];
                    $v['exchange_rate'] = $r[0]['exchange_rate'];
                    $v['currency_code'] = $r[0]['currency_code'];
                    //插件商品时取商品表ID
                    if($order_type == 'MS')
                    {
                        $v['pid'] = $p_id;
                    }
                    $arr = $v;
                    // 根据产品id,查询产品列表 (产品图片)
                    $rrr = ProductListModel::where(['store_id'=>$store_id,'id'=>$p_id])->field('imgurl,mch_id')->select()->toArray();
                    if ($rrr)
                    {
                        $arr['attrImg'] = $rrr[0]['imgurl'];
                        $url = ServerPath::getimgpath($rrr[0]['imgurl'], $store_id); // 拼图片路径
                        $mch_id = $rrr[0]['mch_id'];
                    }

                    $arr['shop_id'] = 0;
                    $arr['shop_name'] = '';
                    $arr['shop_logo'] = '';
                    $arr['headImg'] = '';
                    if (!empty($mch_id))
                    {
                        $r0 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('id,name,logo,head_img')->select()->toArray();
                        if ($r0)
                        {
                            $arr['shop_id'] = $r0[0]['id'];
                            $arr['shop_name'] = $r0[0]['name'];
                            $arr['shop_logo'] = ServerPath::getimgpath($r0[0]['logo']);
                            $arr['headImg'] = ServerPath::getimgpath($r0[0]['head_img']);
                        }
                    }

                    $arr['order_id'] = $order_id;
                    $arr['imgurl'] = $url;
                    if ($v['r_type'] == 0)
                    {
                        $arr['prompt'] = Lang('order.33');
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 1)
                    {
                        $r1 = ServiceAddressModel::where(['store_id'=>$store_id,'uid'=>'admin'])->select()->toArray();
                        $buyer['tel'] = $r1[0]['tel'];
                        $buyer['name'] = $r1[0]['name'];
                        $buyer['address_xq'] = $r1[0]['address_xq'];
                        $arr['prompt'] = Lang('order.5');
                        if($v['re_type'] == 3)
                        {
                            $arr['prompt'] = Lang('order.7');
                        }
                        $arr['buyer'] = $buyer;
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 2)
                    {
                        $arr['prompt'] = Lang('order.6');
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 3)
                    {
                        // $arr['prompt'] = Lang('order.7');
                        $arr['prompt'] = Lang('order.5');
                        if ($v['re_type'] == 3)
                        {
                            $arr['prompt'] = Lang('order.7');
                        }
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 4)
                    {
                        $arr['prompt'] = Lang('order.9');
                        $arr['buyer'] = '';
                        $arr['return_state'] = '退货退款';
                    }
                    else if ($v['r_type'] == 5)
                    {
                        $arr['prompt'] = Lang('order.6');
                        if ($v['re_type'] == 3)
                        {
                            $arr['prompt'] = Lang('order.11');
                        }
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 8 && $v['r_content'] == '已收货')
                    {
                        $arr['prompt'] = Lang('order.10');
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 8 && $v['r_content'] != '已收货')
                    {
                        $arr['prompt'] = Lang('order.6');
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 9)
                    {
                        $arr['prompt'] = Lang('order.9');
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 10)
                    {
                        $arr['prompt'] = Lang('order.6');
                        if($v['re_type'] == 3)
                        {
                            $arr['prompt'] = Lang('order.34');
                        }
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 11)
                    {
                        $arr['prompt'] = Lang('order.7');
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 12)
                    {
                        $arr['prompt'] = Lang('order.12');
                        if($v['re_type'] == 3)
                        {
                            $arr['prompt'] = Lang('order.32');
                        }
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 13)
                    {
                        $arr['prompt'] = Lang('order.9');
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    else if ($v['r_type'] == 15)
                    {
                        $arr['prompt'] = Lang('order.9');
                        $arr['buyer'] = '';
                        $arr['return_state'] = '';
                    }
                    $product[$times] = $arr;
                    $times++;
                }
                $list = $product;
            }
        }
        ob_clean();
        $message = Lang('Success');
        return output(200,$message,array('list' =>$list, 'count' => $count));
    }

    // 储存快递回寄信息
    public function back_send()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $store_id = trim($this->request->param('store_id'));
        $language = trim($this->request->post('language')); // 语言


        // 获取信息
        $kdcode = trim($this->request->post('kdcode')); // 快递单号
        $kdname = trim($this->request->post('kdname')); // 快递名称
        $lxdh = trim($this->request->post('lxdh')); // 寄件人电话
        $lxr = trim($this->request->post('lxr')); // 寄件人
        $id = trim($this->request->post('id')); // 售后订单id
        $access_id = trim($this->request->post('access_id')); // 授权id
        $lktlog = new LaiKeLogUtils();

        $userid = $this->user_list['user_id'];
        $res_d = ReturnOrderModel::where(['store_id'=>$store_id,'id'=>$id])->field('p_id,re_type,sNo,pid,sid')->select()->toArray();
        $oid = $res_d[0]['p_id'];
        $re_type = $res_d[0]['re_type'];
        $sNo = $res_d[0]['sNo'];
        $pid = $res_d[0]['pid'];
        $sid = $res_d[0]['sid'];
        $sql = new ReturnGoodsModel();
        $sql->store_id = $store_id;
        $sql->name = $lxr;
        $sql->tel = $lxdh;
        $sql->express = $kdname;
        $sql->express_num = $kdcode;
        $sql->uid = $userid;
        $sql->user_id = $userid;
        $sql->oid = $oid;
        $sql->add_data = date('Y-m-d H:i:s');
        $sql->re_id = $id;
        $sql->save();
        $rid = $sql->id;
        if ($rid < 1)
        {
            $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "添加回退物品信息失败！re_id:" . $id);
        }
        $sql = ReturnOrderModel::where(['store_id'=>$store_id,'id'=>$id])->find();
        $sql->r_type = 3;
        $r = $sql->save();
        if ($r)
        {   
            $r01 = ExpressModel::where('kuaidi_name',$kdname)->select()->toArray();

            //添加售后记录
            $sql_r = new ReturnRecordModel();
            $sql_r->user_id = $userid;
            $sql_r->store_id = $store_id;
            $sql_r->re_type = $re_type;
            $sql_r->r_type = 3;
            $sql_r->sNo = $sNo;
            $sql_r->money = 0;
            $sql_r->product_id = $pid;
            $sql_r->express_id = $r01[0]['id'];
            $sql_r->courier_num = $kdcode;
            $sql_r->attr_id = $sid;
            $sql_r->re_time = date('Y-m-d H:i:s');
            // $sql_r->p_id = $oid;
            $sql_r->p_id = $id;
            $sql_r->save();
            ob_clean();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改售后订单失败！id:" . $id);
            $sql = ReturnOrderModel::where(['store_id'=>$store_id,'id'=>$id])->find();
            $sql->r_type = 1;
            $r = $sql->save();
            if (!$r)
            {
                $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改售后订单失败！id:" . $id);
            }
            $sql = ReturnGoodsModel::where(['store_id'=>$store_id,'id'=>$rid])->find();
            $sql->delete();

            ob_clean();
            $message = Lang('Busy network');
            return output(ERROR_CODE_WLFMQSHZS,$message);
        }

    }

    // 返回快递信息
    public function see_send()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->post('store_type'));
        $store_id = trim($this->request->param('store_id'));
        $language = trim($this->request->post('language')); // 语言

        $access_id = trim($this->request->post('access_id')); // 授权id
        $pid = trim($this->request->post('pid'));

        // 根据商品id，查询商品信息
        $r0 = ProductListModel::where(['store_id'=>$store_id,'id'=>$pid])->field('mch_id,gongyingshang')->select()->toArray();
        if ($r0)
        {   
            if($r0[0]['gongyingshang'] != 0)
            {
                $r1 = SupplierModel::where(['store_id'=>$store_id,'id'=>$r0[0]['gongyingshang']])->field('province,city,area,address,contacts,contact_phone')->select()->toArray();
                if($r1)
                {
                    $address = $r1[0]['province'].$r1[0]['city'].$r1[0]['area'].$r1[0]['address'];
                    $name = $r1[0]['contacts'];
                    $phone = $r1[0]['contact_phone'];
                }
                else
                {
                    ob_clean();
                    $message = Lang('Illegal invasion');
                    return output(ERROR_CODE_FFRQ,$message);
                }
            }
            else
            {
                $mch_id = $r0[0]['mch_id']; // 店铺ID
                // 根据店铺ID，查询管理员信息
                $r1 = AdminModel::where(['store_id'=>$store_id,'shop_id'=>$mch_id])->field('id')->select()->toArray();
                if ($r1)
                {
                    // 存在，代表是自营商品
                    // 获取信息
                    $r_1 = ServiceAddressModel::where(['store_id'=>$store_id,'uid'=>'admin','is_default'=>1,'type'=>2])
                                            ->field('sheng,shi,xian,address_xq,name,tel')
                                            ->select()
                                            ->toArray();
                    $address = $r_1[0]['address_xq'];
                    $name = $r_1[0]['name'];
                    $phone = $r_1[0]['tel'];
                }
                else
                {
                    $r1 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('cpc,sheng,shi,xian,realname,tel,address,code')->select()->toArray();
                    if ($r1)
                    {
                        $array_address = array('cpc'=>$r1[0]['cpc'],'sheng'=>$r1[0]['sheng'],'shi'=>$r1[0]['shi'],'xian'=>$r1[0]['xian'],'address'=>$r1[0]['address'],'code'=>$r1[0]['code']);
                        $address = PC_Tools::address_translation($array_address);
                        $name = $r1[0]['realname'];
                        $phone = $r1[0]['tel'];
                    }
                    else
                    {
                        ob_clean();
                        $message = Lang('Illegal invasion');
                        return output(ERROR_CODE_FFRQ,$message);
                    }
                }
            }
            
        }
        else
        {
            ob_clean();
            $message = Lang('order.20');
            return output(ERROR_CODE_SPXXCW,$message);
        }

        $r_2 = ExpressModel::where(['recycle'=>0,'is_open'=>1])->select()->toArray();
        if ($r_2)
        {
            ob_clean();
            $message = Lang('Success');
            return output(200,$message,array('address' => $address, 'name' => $name, 'phone' => $phone, 'express' => $r_2));
        }
        else
        {
            ob_clean();
            $message = Lang('Busy network');
            return output(ERROR_CODE_WLFMQSHZS,$message);
        }
    }

    // 删除订单
    public function del_order()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->post('store_type'));
        $id = trim($this->request->post('order_id'));// 订单id
        $lktlog = new LaiKeLogUtils();

        $user_id = $this->user_list['user_id'];
        // 根据订单id,查询订单列表(订单号)
        $r = OrderModel::where(['store_id'=>$store_id,'id'=>$id,'user_id'=>$user_id])->field('sNo')->select()->toArray();
        if ($r)
        {
            $sNo = $r[0]['sNo']; // 订单号
            $sql1 = "update lkt_order_details set recycle = 2 where store_id = '$store_id' and r_sNo = '$sNo'";
            $r1 = Db::execute($sql1);

            $sql2 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->find();
            $sql2->recycle = 2;
            $r2 = $sql2->save();

            if ($r1 && $r2)
            {
                ob_clean();
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改订单状态失败！sql:" . $sql1);
                ob_clean();
                $message = Lang('Abnormal business');
                return output(ERROR_CODE_YWYC,$message);
            }
        }
        else
        {
            ob_clean();
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW,$message);
        }
    }

    // 再次购买
    public function buy_again()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $id = trim($this->request->post('id'));// 订单id

        $r = OrderModel::where(['store_id'=>$store_id,'id'=>$id])->select()->toArray();
        if ($r)
        {
            $list = array();
            $user_id = $r[0]['user_id'];
            $sNo = $r[0]['sNo'];
            $otype = $r[0]['otype'];
            $rr = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->field('p_id,num,sid')->select()->toArray();
            foreach ($rr as $k => $v)
            {
                $list[$k] = $v;
            }

            $cart_type = 1; // 可以添加购物车
            if($otype == 'VI')
            {
                $pro_type = 1;
            }
            else
            {
                $pro_type = 0;
            }
            foreach ($list as $ke => $va)
            {
                $sql = "select b.num from lkt_product_list as a left join lkt_configure as b on a.id = b.pid where a.store_id = '$store_id' and a.status = 2 and a.mch_status = 2 and b.pid = '".$va['p_id']."' and b.id = '".$va['sid']."' and a.recycle = 0";
                $rrr = Db::query($sql);
                if ($rrr)
                {
                    $num = $rrr[0]['num'];
                    if ($num >= $va['num'])
                    {
                        $cart_type = 1; // 可以添加购物车
                    }
                    else
                    {
                        ob_clean();
                        $message = Lang('product.1');
                        return output(ERROR_CODE_SPKCBZ,$message);
                    }
                }
                else
                {
                    ob_clean();
                    $message = Lang('order.14');
                    return output(ERROR_CODE_SPYXJ,$message);
                }
            }
            if ($cart_type == 1)
            {
                foreach ($list as $key => $val)
                {
                    $sql = new BuyAgainModel();
                    $sql->store_id = $store_id;
                    $sql->user_id = $user_id;
                    $sql->Goods_id = $val['p_id'];
                    $sql->Goods_num = $val['num'];
                    $sql->Create_time = date("Y-m-d H:i:s");
                    $sql->Size_id = $val['sid'];
                    $sql->pro_type = $pro_type;
                    $id1 = $sql->save();
                    if (!$id1)
                    {
                        LaiKeLogUtils::lktLog(__METHOD__ . ":" . __LINE__ . "添加购物车失败！");
                    }
                    $cart_id[] = $sql->id;
                }
            }
            $cart_id = implode(",", $cart_id);
            ob_clean();
            $message = Lang('Success');
            return output(200,$message,array('cart_id' => $cart_id,'pro_type'=>$pro_type));
        }
        else
        {
            ob_clean();
            $message = Lang($db, $store_id,$access_id,$language, 'Parameter_error');
            return output(400,$message);
        }
    }

    // 点击退货后，进入的页面
    public function return_method()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->post('store_type'));
        $order_details_id = $this->request->post('order_details_id');// 订单详情id

        $user_id = $this->user_list['user_id'];
        $refund_price = 0;
        $after_discount = 0;
        $list = array();
        $freight = 0;
        $typeArr = array();
        $r_status = array();
        $refund = true; // 申请售后
        $refundAmt = false; // 仅退款 只有待发货才有仅退款
        $refundGoods = false; // 换货 未发货没有换货
        $refundGoodsAmt = false; // 退货退款 未发货没有退货退款
        $refund_only_status = false; // 仅退款状态
        $return_refund_status = false; // 退货退款状态
        $exchange_goods_status = false; // 换货状态
        $Have_you_ever_exchanged_goods = false; // 是否换过货

        $status = false;
        if (!empty($order_details_id))
        {
            if (is_array($order_details_id))
            { // 是数组
                foreach ($order_details_id as $key => $value)
                {
                    $typeArr[$key] = $value;
                }
            }
            else if (is_string($order_details_id))
            { // 是字符串
                $typestr = trim($order_details_id, ','); // 移除两侧的逗号
                $typeArr = explode(',', $typestr); // 字符串打散为数组
            }
        }

        $tui_num = count($typeArr);

        foreach ($typeArr as $k => $v)
        {
            $sql = "select d.id,d.user_id,p.id as p_id,d.p_name,d.p_price,d.after_discount,d.num,d.unit,d.r_status,d.r_sNo,d.freight,d.size,d.sid,d.exchange_num,d.write_off_num,d.after_write_off_num,p.write_off_settings,p.is_appointment,d.is_addp,d.p_integral,d.score_deduction as scoreDeduction from lkt_order_details as d left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where d.store_id = '$store_id' and d.id = '$v' and d.is_addp = 0";
            $r = Db::query($sql);
            if ($r)
            {
                if($user_id != $r[0]['user_id'])
                {
                    $message = Lang('Success');
                    return output(ERROR_CODE_bsdqyhdsj,$message,array());
                }
                $p_id = $r[0]['p_id'];
                $r_status[] = $r[0]['r_status'];
                $r_sNo = $r[0]['r_sNo'];
                $attribute_id = $r[0]['sid'];
                $exchange_num = $r[0]['exchange_num'];
                $refund_price += Tools::get_order_pro_price($store_id, $v); // 获取该订单详情商品支付金额
                $after_discount += $r[0]['after_discount'];
                
                $r3 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$r_sNo])->field('z_price,self_lifting,otype,currency_code,currency_symbol,exchange_rate')->select()->toArray();
                if ($r3)
                {
                    $z_price = $r3[0]['z_price'];
                    $self_lifting = $r3[0]['self_lifting'];
                    $orderType = $r3[0]['otype'];
                    $currency_code = $r3[0]['currency_code'];
                    $currency_symbol = $r3[0]['currency_symbol'];
                    $exchange_rate = $r3[0]['exchange_rate'];
                }
                $r[0]['currency_code'] = $currency_code;
                $r[0]['currency_symbol'] = $currency_symbol;
                $r[0]['exchange_rate'] = $exchange_rate;
                if($orderType == 'IN')
                {
                    $r[0]['scoreDeduction'] = $r[0]['p_integral'];
                }

                $sql_o = "select count(id) as d_num from lkt_order_details where r_sNo = '$r_sNo' ";
                $r_o = Db::query($sql_o);
                if($r_o)
                {
                    if($tui_num == $r_o[0]['d_num'])
                    { 
                        // 申请售后数量 等于 订单详情数量
                        $refund_price += $r[0]['freight']; // 退还运费
                        $after_discount += $r[0]['freight'];
                    }
                }
                
                //判断是否已经有过售后
                $res = ReturnOrderModel::where(['sNo'=>$r_sNo,'p_id'=>$v])->where('re_type','in','1,2')->where('r_type','not in','2,5,8,10')->select()->toArray();
                if(empty($res))
                {
                    $rr = ConfigureModel::where(['id'=>$attribute_id,'pid'=>$p_id])->select()->toArray();
                    if ($rr)
                    {
                        $r[0]['image'] = ServerPath::getimgpath($rr[0]['img']); // 图片
                        $r[0]['imgurl'] = ServerPath::getimgpath($rr[0]['img']); // 图片
                    }
                }

                if($exchange_num < 2)
                {
                    $list[] = $r[0];
                }
                $res__ = ReturnOrderModel::where(['sNo'=>$r_sNo,'p_id'=>$v])->where('re_type','3')->select()->toArray();
                if($res__)
                {
                    $Have_you_ever_exchanged_goods = true;
                }
            }
        }

        $self_lifting = '0';

        if($self_lifting == '0' || $self_lifting == 2)
        {
            if(in_array(1,$r_status) && $r3[0]['otype'] != 'JP' && $r3[0]['otype'] != 'KJ')
            {
                $refund_only_status = true; // 可以退款
            }
            else
            {
                if($exchange_num == 0 && $r3[0]['otype'] != 'JP' && $r3[0]['otype'] != 'KJ')
                {
                    $refund_only_status = true; // 可以退款
                    $return_refund_status = true; // 可以退货退款
                    $exchange_goods_status = true; // 可以换货
                }
                else
                {   
                    $exchange_goods_status = true; // 可以换货
                }
            }
        }
        else
        {
            $refund_only_status = true; // 可以退款
        }
        if($orderType == 'PS')
        {
            if(!$Have_you_ever_exchanged_goods)
            {
                if(in_array(1,$r_status))
                {
                    $refund_only_status = true; // 可以退款
                }
                else if(in_array(2,$r_status))
                {
                    $refund_only_status = true; // 可以退款
                    $return_refund_status = true; // 可以退货退款
                }
            }
        }
        else if($orderType == 'VI')
        {
            if($r[0]['write_off_settings'] == 1)
            { // 线下核销
                $refund_price = 0;
                $z_write_off_num = $r[0]['write_off_num'] + $r[0]['after_write_off_num'];
                if($z_write_off_num != 0)
                {
                    $refund_price = round($z_price / $z_write_off_num * $r[0]['write_off_num'],2);
                    $after_discount = round($z_price / $z_write_off_num * $r[0]['write_off_num'],2);
                }
            }
            // else
            // { // 无需核销
            //     $refund_price = round($z_price,2);
            // }
        }
        else if($orderType == 'ZB')
        {
            $refund_price   = Tools::get_order_pro_price($store_id, $typeArr[0]) + $r[0]['freight'];
            $after_discount = Tools::get_order_pro_price($store_id, $typeArr[0]) + $r[0]['freight'];
        }
        else if($orderType == 'FS')
        {
            $refund_price = $z_price;
            $after_discount = $z_price;
            $sql_d = "select d.id,p.id as p_id,d.p_name,d.p_price,d.num,d.unit,d.r_status,d.r_sNo,d.freight,d.size,d.sid,d.exchange_num,d.write_off_num,d.after_write_off_num,p.write_off_settings,p.is_appointment from lkt_order_details as d left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where d.store_id = '$store_id' and d.r_sNo = '$r_sNo' and d.is_addp = 1";
            $r_d = Db::query($sql_d);
            if($r_d)
            {
                foreach($r_d as $k_d => $v_d)
                {
                    $p_id = $v_d['p_id'];
                    $attribute_id = $v_d['sid'];
                    $r_d1 = ConfigureModel::where(['id'=>$attribute_id,'pid'=>$p_id])->select()->toArray();
                    if ($r_d1)
                    {
                        $v_d['image'] = ServerPath::getimgpath($r_d1[0]['img']); // 图片
                    }

                    $list[] = $v_d;
                }
            }
        }

        foreach ($r_status as $k => $v)
        {
            if ($v == 2)
            {
                $status = true;
                break;
            }
        }

        $data = array('orderType'=>$orderType,'refund_price' => $refund_price,'self_lifting' => $self_lifting, 'list' => $list, 'status' => $status,'refundAmt' => $refund_only_status, 'refundGoods' => $exchange_goods_status, 'refundGoodsAmt' => $return_refund_status, 'currency_code' => $currency_code, 'currency_symbol' => $currency_symbol, 'exchange_rate' => $exchange_rate);
        ob_clean();
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 退货申请
    public function ReturnData()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        // 获取信息
        $access_id = trim($this->request->post('access_id')); // 授权id
        $order_details_id = $this->request->post('order_details_id'); // 订单详情id
        $refund_amount = $this->request->post('refund_amount'); // 退货金额
        $content = urldecode($this->request->post('explain')); // 退货原因
        $re_apply_money = $this->request->post('refund_apply_money'); // 用户申请退款金额
        $type = $this->request->post('type'); // 退货类型
        $upload_z_num = trim($this->request->post('upload_z_num')); // 循环总次数
        $upload_num = trim($this->request->post('upload_num')); // 上传次数
        $lktlog = new LaiKeLogUtils();
        $re_photo = '';// 照片凭证
        $time = date('Y-m-d H:i:s');

        $user_id = $this->user_list['user_id'];

        $Tools = new Tools($store_id, 1);
        $image_arr = array();
        $image_array = array();

        if (empty($upload_z_num) && empty($upload_num))
        {
            // 没有上传图片

        }
        else
        {
            // 有上传图片
            // 查询配置表信息
            $r = ConfigModel::where('store_id',$store_id)->select()->toArray();
            $uploadImg = $r[0]['uploadImg'];
            $uploadImg_domain = $r[0]['uploadImg_domain'];
            $upserver = !empty($r) ? $r[0]['upserver'] : '2';   //如果没有设置配置则默认用阿里云
            // 图片上传位置
            if (empty($uploadImg))
            {
                $uploadImg = "./images";
            }
            if (!empty($_FILES))
            {          //如果图片不为空
                if ($upserver == '2')
                {
                    $imgURL_name = ServerPath::file_OSSupload($store_id, $store_type);
                }
                else
                {
                    $imgURL_name = ServerPath::file_upload($store_id, $uploadImg, $uploadImg_domain, $store_type);
                }
                $image_arr1 = array('image' => $imgURL_name, 'call_num' => $upload_num); // 图片数组
                $cache = array('user_id' => $user_id, 'order_details_id' => $order_details_id, 'image_arr' => $image_arr1); // 缓存数组
            }
            if ($upload_num + 1 != $upload_z_num)
            {
                $res = $Tools->generate_session($cache, 2);

                ob_clean();
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $rew = $Tools->obtain_session($user_id, 2, $order_details_id);
                if ($rew != '')
                {
                    $image_arr2 = json_decode($rew, true);
                    if (count($image_arr2) == count($image_arr2, 1))
                    {
                        $image_arr[] = $image_arr2;
                        $image_arr[] = $image_arr1;
                    }
                    else
                    {
                        foreach ($image_arr2 as $k => $v)
                        {
                            $image_arr[] = (array)$v;
                        }
                        array_push($image_arr, $image_arr1);
                    }
                }
                else
                {
                    $image_arr[] = $image_arr1;
                }
            }
            foreach ($image_arr as $ke => $va)
            {
                $image_array[$ke] = $va['image'];
            }
        }

        $array = array('store_id'=>$store_id,'user_id'=>$user_id,'order_details_id'=>$order_details_id,'refund_amount'=>$refund_amount,'re_apply_money'=>$re_apply_money,'content'=>$content,'type'=>$type,'image_array'=>$image_array);
        RefundUtils::Return_Request($array);
        return;
    }

    public function a_array_unique($array)
    {
        //写的比较好（写方法）
        $out = array(); //定义变量out为一个数组
        foreach ($array as $key => $value)
        {
            //将$array数组按照$key=>$value的样式进行遍历
            if (!in_array($value, $out))
            {
                //如果$value不存在于out数组中，则执行
                $out[$key] = $value; //将该value值存入out数组中
            }
        }
        return $out; //最后返回数组out
    }

    // 提醒发货
    public function delivery_delivery()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->post('store_type'));

        $access_id = trim($this->request->post('access_id')); // 授权id
        $order_id = trim($this->request->post('order_id')); // 订单id
        $user_id = $this->user_list['user_id'];
        $lktlog = new LaiKeLogUtils();

        $order = OrderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'id'=>$order_id,'delivery_status'=>0])->field('otype,delivery_status,mch_id,sNo,is_lssued,supplier_id')->select()->toArray();
        if ($order)
        {   
            $remind0 = 0;
            $remind = 0;
            $otype = $order[0]['otype'];
            $is_lssued = $order[0]['is_lssued'];
            $supplier_id = $order[0]['supplier_id'];
            if($otype == 'GM' || $otype == 'FX' || $otype == 'JP' || $otype == 'FS' || $otype == 'ZB' || $otype == 'PT')
            {
                $r_config = OrderConfigModel::where(['store_id'=>$store_id])->field('remind')->select()->toArray();
                if($r_config)
                {
                    $remind0 = $r_config[0]['remind'];
                }
                
                $remind = date("Y-m-d H:i:s",strtotime("+$remind0 hour",time()));
            }
            if (!empty($order[0]['mch_id']))
            {
                $mch_id = $order[0]['mch_id'];
                $mch_id = substr($mch_id, 1, strlen($mch_id) - 2);
                if($otype == 'IN')
                {
                    $Self_operated_store_id = PC_Tools::SelfOperatedStore($store_id);
                    $mch_config = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$Self_operated_store_id])->field('deliver_remind')->select()->toArray();
                    $remind0 = $mch_config[0]['deliver_remind'];
                    $remind = date("Y-m-d H:i:s",strtotime("+$remind0 seconds",time()));
                }
                elseif($otype == 'MS')
                {
                    $mch_config = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('deliver_remind')->select()->toArray();
                    $remind0 = $mch_config[0]['deliver_remind'];
                    $remind = date("Y-m-d H:i:s",strtotime("+$remind0 seconds",time()));
                }

                if($is_lssued == 1)
                {
                    $message_3 = "订单".$order[0]['sNo']."的用户已经迫不及待想要收到宝贝了，请前往订单列表发货！";
                    $message_logging_list3 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>$supplier_id,'type'=>3,'parameter'=>$order[0]['sNo'],'content'=>$message_3);
                    PC_Tools::add_message_logging($message_logging_list3);
                }
                else
                {
                    $message_3 = "订单".$order[0]['sNo']."的用户已经迫不及待想要收到宝贝了，请前往订单列表发货！";
                    $message_logging_list3 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>3,'parameter'=>$order[0]['sNo'],'content'=>$message_3);
                    PC_Tools::add_message_logging($message_logging_list3);
                }

                $mch = MchModel::where(['id'=>$mch_id,'store_id'=>$store_id])->field('user_id')->select()->toArray();
                $mch_user = $mch[0]['user_id'];
                // $msg_title = "【" . $order[0]['sNo'] . "】订单提醒发货！";
                $msg_title = Lang("message.1");
                $msg_content = Lang("order.2");

                //给用户发送消息
                $pusher = new LaikePushTools();
                $pusher->pushMessage($mch_user, $msg_title, $msg_content, $store_id, 'admin');
            }
            $sql_u = OrderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'id'=>$order_id])->find();
            $sql_u->delivery_status = 1;
            $sql_u->readd = 0;
            $sql_u->remind = $remind;
            $r_u = $sql_u->save();
            if (!$r_u)
            {
                $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改订单提醒状态失败！id:" . $order_id);
            }

            ob_clean();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            ob_clean();
            $message = Lang('order.3');
            return output(400,$message);
        }
    }

    /**
     * [calculate_order description]
     * <p>Copyright (c) 2018-2019</p>
     * <p>Company: www.laiketui.com</p>
     * @Author  苏涛
     * @param 计算整个订单实付
     * @version 2.0
     * @date    2018-12-29T18:17:40+0800
     */
    public function calculate_order($db, $sNo, $z_price, $spz_price)
    {
        //判断单个商品退款是否有使用优惠
        $sql_id = "select m.num,m.p_price from lkt_order_details AS m where m.r_sNo = '$sNo' and m.r_status = '3' ";
        $variable = $db->select($sql_id);
        $price = 0;
        foreach ($variable as $key => $value)
        {
            $num = $value->num;
            $p_price = $value->p_price * $num;
            //计算实际支付金额
            $price += number_format($z_price / $spz_price * $p_price, 2, ".", "");
        }
        return $price;
    }

    public function wxss($user_id, $cdata)
    {
        $db = $this->db;
        $store_id = $this->store_id;
        $store_type = $this->store_type;
        $time = date('Y-m-d h:i:s', time());

        $sql_openid = "select wx_id,user_name from lkt_user where store_id = '$this->store_id' and user_id = '$user_id'";
        $res_openid = $this->db->select($sql_openid);
        $openid = $res_openid[0]->wx_id;
        $user_name = $res_openid[0]->user_name;

        //实例化
        $Tools = new Tools($db, $store_id, $store_type);
        $data = array();

        $data['page'] = 'pages/coupon/index?currentTab=1';
        $data['template_id'] = 'receive';
        $data['openid'] = $openid;

        $o_data = array();
        //设置消息数组
        $o_data['keyword1'] = array('value' => $user_name, "color" => "#173177");
        $o_data['keyword2'] = array('value' => '系统', "color" => "#173177");
        $o_data['keyword3'] = array('value' => $cdata->name, "color" => "#173177");
        $o_data['keyword4'] = array('value' => '我的优惠券', "color" => "#173177");
        $o_data['keyword5'] = array('value' => $cdata->add_time, "color" => "#173177");
        $o_data['keyword6'] = array('value' => $cdata->end_time, "color" => "#173177");
        $o_data['keyword7'] = array('value' => $cdata->money, "color" => "#173177");
        $o_data['keyword8'] = array('value' => $cdata->limit, "color" => "#173177");

        $data['o_data'] = $o_data;
        $tres = $Tools->send_notice($data, 'wx');

        return $tres ? ($tres->errcode == 'ok' ? true : false) : false;

    }

    /**
     * <p>Copyright (c) 2018-2019</p>
     * <p>Company: www.laiketui.com</p>
     * @Author  熊孔钰
     * @return  分销
     * $dis_0->商品id     $dis_1->订单号
     * @version 2.0
     * @date    2019-03-27T15:58:30+0800
     */
    public function order_distribution($dis_0, $dis_1)
    {
        $comm = new Commission();
        $store_id = $this->store_id;
        $time = date('Y-m-d h:i:s', time());

        $dis_sql_0 = "select a.*,d.price,b.num,b.user_id,c.z_price,d.costprice 
                        from lkt_distribution_goods a 
                        left join lkt_order_details b on a.s_id=b.sid
                        left join lkt_order c on b.r_sNo = c.sNo
                        left join lkt_configure d on a.s_id = d.id 
                        where a.store_id = '$store_id' and b.p_id = '$dis_0' and c.sNo='$dis_1' and a.recycle = 0 limit 0,1";
        $dis_res_0 = Db::query($dis_sql_0);

        if (!$dis_res_0)
        {
            return true;
        }

        $dis_5 = $dis_res_0[0]['user_id'];//用户id

        $dis_15 = intval($dis_res_0[0]['uplevel']);//商品绑定等级

        $distribution_rule = $dis_res_0[0]['distribution_rule'];//商品分佣规则
        //获取分销设置
        $res_config = DistributionConfigModel::where('store_id',$store_id)->select()->toArray();
        $config = json_decode($res_config[0]['sets'],true);
        $calculation = $config['c_yjjisuan'];
        //计算分润基值
        switch ($calculation) {
            case '1':   //利润
                $dis_2 = floatval($dis_res_0[0]['price'] - $dis_res_0[0]['costprice']) * intval($dis_res_0[0]['num']);
                break;
            case '2':   //规格售价
                $dis_2 = floatval($dis_res_0[0]['price']) * intval($dis_res_0[0]['num']);
                break;
            case '3':   //PV值
                $dis_2 = floatval($dis_res_0[0]['pv']) * intval($dis_res_0[0]['num']);
                break;
            default:    //订单成交价
                $dis_2 = floatval($dis_res_0[0]['z_price']);
                break;
        }

        if ($dis_2 > 0)
        {
            $dis_4 = $config['c_cengji'];//分销层级
            $dis_12 = $config['c_neigou'];//是否内购 1是 0否
            if ($dis_4 < 1)
            {
                return true;
            }

            $dis_6 = array();//所有等级相应应得奖励
            $dis_6['level0']['comm1'] = 0; // 默认直推
            $dis_6['level0']['comm2'] = 0; // 默认间推
            $dis_6['level0']['different'] = 0; // 默认等级级差奖
            $dis_6['level0']['sibling'] = 0; // 默认等级平级奖

            // 分销等级相应佣金计算
            $dis_res_2 = DistributionGradeModel::where('store_id',$store_id)->field('id,sets')->select()->toArray();
            
            if($distribution_rule == 1)
            { // 等级规则
                if($dis_res_2)
                {
                    foreach ($dis_res_2 as $k => $v)
                    {
                        $dis_7 = json_decode($v['sets'],true);//等级配置
                        if ($dis_7['different_type'] == 1)
                        {//佣金计算单位为元时
                            $dis_6['level' . $v['id']]['different'] = $dis_7['different'];//计算级差
                        }
                        else
                        {//佣金计算单位为百分比%时
                            $dis_6['level' . $v['id']]['different'] = round($dis_7['different'] * $dis_2 *0.01, 2);//计算级差
                        }
                        if ($dis_7['sibling_type'] == 1)
                        {//佣金计算单位为元时
                            $dis_6['level' . $v['id']]['sibling'] = $dis_7['sibling'];//计算平级
                        }
                        else
                        {//佣金计算单位为百分比%时
                            $dis_6['level' . $v['id']]['sibling'] = round($dis_7['sibling'] * $dis_2 *0.01, 2);//计算平级
                        }

                        if ($dis_7['direct_m_type'] == 1)
                        {//佣金计算单位为元时
                            $dis_6['level' . $v['id']]['comm1'] = $dis_7['direct_m'];//直推分销奖
                        }
                        else
                        {//佣金计算单位为百分比%时
                            $dis_6['level' . $v['id']]['comm1'] = round($dis_7['direct_m'] * $dis_2 *0.01, 2);//直推分销奖
                        }
                        if ($dis_7['indirect_m_type'] == 1)
                        {//佣金计算单位为元时
                            $dis_6['level' . $v['id']]['comm2'] = $dis_7['indirect_m'];//间推分销奖
                        }
                        else
                        {//佣金计算单位为百分比%时
                            $dis_6['level' . $v['id']]['comm2'] = round($dis_7['indirect_m'] * $dis_2 *0.01, 2);//间推分销奖
                        }
                    }
                }
            }
            else
            { // 自定义
                if($dis_res_2)
                {
                    foreach ($dis_res_2 as $k => $v)
                    {
                        $dis_7 = json_decode($v['sets'],true);//等级配置
                        if ($dis_7['different_type'] == 1)
                        {//佣金计算单位为元时
                            $dis_6['level' . $v['id']]['different'] = $dis_7['different'];//计算级差
                        }
                        else
                        {//佣金计算单位为百分比%时
                            $dis_6['level' . $v['id']]['different'] = round($dis_7['different'] * $dis_2 *0.01, 2);//计算级差
                        }
                        if ($dis_7['sibling_type'] == 1)
                        {//佣金计算单位为元时
                            $dis_6['level' . $v['id']]['sibling'] = $dis_7['sibling'];//计算平级
                        }
                        else
                        {//佣金计算单位为百分比%时
                            $dis_6['level' . $v['id']]['sibling'] = round($dis_7['sibling'] * $dis_2 *0.01, 2);//计算平级
                        }
                    }

                    $rules_set = json_decode($dis_res_0[0]['rules_set'],true);
                    foreach ($rules_set as $k => $v)
                    {
                        if($v['direct_mode_type'] == 0)
                        { // 百分比
                            $dis_6['level' . $v['id']]['comm1'] = round($v['direct_m'] * $dis_2 * 0.01, 2);//直推分销奖
                        }
                        else
                        { // 元
                            $dis_6['level' . $v['id']]['comm1'] = $v['direct_m'];//直推分销奖
                        }
                        if($v['indirect_mode_type'] == 0)
                        { // 百分比
                            $dis_6['level' . $v['id']]['comm2'] = round($v['indirect_m'] * $dis_2 * 0.01, 2);//间推分销奖
                        }
                        else
                        { // 元
                            $dis_6['level' . $v['id']]['comm2'] = $v['indirect_m'];//间推分销奖
                        }
                    }
                }
            }

            // //分销等级相应佣金计算
            // $dis_res_2 = DistributionGradeModel::where('store_id',$store_id)->field('id,sets')->select()->toArray();
            // $dis_6 = array();//所有等级相应应得奖励
            // $dis_6['level0']['different'] = 0;//默认等级级差奖
            // $dis_6['level0']['sibling'] = 0;//默认等级平级奖
            // if($distribution_rule == 1)
            // {
            //     $dis_6['level0']['comm1'] = 0;//默认直推
            //     $dis_6['level0']['comm2'] = 0;//默认间推
            // }
            // else
            // {   
            //     $rules_set = json_decode($dis_res_0[0]['rules_set'],true);
            //     if ($rules_set['direct_type'] == 1)
            //     {//佣金计算单位为元时
            //         $dis_6['level0']['comm1'] = $rules_set['direct_m'];//默认直推
            //     }
            //     else
            //     {//佣金计算单位为百分比%时
            //         $dis_6['level0']['comm1'] = round($rules_set['direct_m'] * $dis_2 *0.01, 2);//默认直推
            //     }
            //     if ($rules_set['indirect_type'] == 1)
            //     {//佣金计算单位为元时
            //         $dis_6['level0']['comm2'] = $rules_set['indirect_m'];//默认间推
            //     }
            //     else
            //     {//佣金计算单位为百分比%时
            //         $dis_6['level0']['comm2'] = round($rules_set['indirect_m'] * $dis_2 *0.01, 2);//默认间推
            //     }
            // }
            // //循环所有等级
            // foreach ($dis_res_2 as $k => $v)
            // {
            //     $dis_7 = json_decode($v['sets'],true);//等级配置
            //     if ($dis_7['different_type'] == 1)
            //     {//佣金计算单位为元时
            //         $dis_6['level' . $v['id']]['different'] = $dis_7['different'];//计算级差
            //     }
            //     else
            //     {//佣金计算单位为百分比%时
            //         $dis_6['level' . $v['id']]['different'] = round($dis_7['different'] * $dis_2 *0.01, 2);//计算级差
            //     }
            //     if ($dis_7['sibling_type'] == 1)
            //     {//佣金计算单位为元时
            //         $dis_6['level' . $v['id']]['sibling'] = $dis_7['sibling'];//计算平级
            //     }
            //     else
            //     {//佣金计算单位为百分比%时
            //         $dis_6['level' . $v['id']]['sibling'] = round($dis_7['sibling'] * $dis_2 *0.01, 2);//计算平级
            //     }
            //     if($distribution_rule == 1)
            //     {
            //         if ($dis_7['direct_m_type'] == 1)
            //         {//佣金计算单位为元时
            //             $dis_6['level' . $v['id']]['comm1'] = $dis_7['direct_m'];//直推分销奖
            //         }
            //         else
            //         {//佣金计算单位为百分比%时
            //             $dis_6['level' . $v['id']]['comm1'] = round($dis_7['direct_m'] * $dis_2 *0.01, 2);//直推分销奖
            //         }
            //         if ($dis_7['indirect_m_type'] == 1)
            //         {//佣金计算单位为元时
            //             $dis_6['level' . $v['id']]['comm2'] = $dis_7['indirect_m'];//间推分销奖
            //         }
            //         else
            //         {//佣金计算单位为百分比%时
            //             $dis_6['level' . $v['id']]['comm2'] = round($dis_7['indirect_m'] * $dis_2 *0.01, 2);//间推分销奖
            //         }
            //     }
            //     else
            //     {
            //         $rules_set = json_decode($dis_res_0[0]['rules_set'],true);
            //         if ($rules_set['direct_type'] == 1)
            //         {//佣金计算单位为元时
            //             $dis_6['level' . $v['id']]['comm1'] = $rules_set['direct_m'];//直推分销奖
            //         }
            //         else
            //         {//佣金计算单位为百分比%时
            //             $dis_6['level' . $v['id']]['comm1'] = round($rules_set['direct_m'] * $dis_2 *0.01, 2);//直推分销奖
            //         }
            //         if ($rules_set['indirect_type'] == 1)
            //         {//佣金计算单位为元时
            //             $dis_6['level' . $v['id']]['comm2'] = $rules_set['indirect_m'];//间推分销奖
            //         }
            //         else
            //         {//佣金计算单位为百分比%时
            //             $dis_6['level' . $v['id']]['comm2'] = round($rules_set['indirect_m'] * $dis_2 *0.01, 2);//间推分销奖
            //         }
            //     }   
            // }
            //查询推荐人
            $dis_sql_3 = "select b.pid,b.level,a.Referee from lkt_user a left join lkt_user_distribution b on a.user_id=b.user_id where a.store_id = '$store_id' and a.user_id = '$dis_5'";
            $dis_res_3 = Db::query($dis_sql_3);
            if ($dis_res_3)
            {
                //如果分销表用户不存在先创建用户
                if (empty($dis_res_3[0]['pid']))
                {
                    $comm->create_level($dis_5, 0, $dis_res_3[0]['Referee'], $store_id);
                }
                //用户推荐人不存在时，推荐人默认为第一个分销商
                if (empty($dis_res_3[0]['Referee']))
                {
                    $res = UserDistributionModel::where('store_id',$store_id)->field('user_id')->order('rt', 'desc')->select()->toArray();
                    $dis_res_3[0]['Referee'] = $res[0]['user_id'];
                }

                $dis_8 = !empty($dis_res_3[0]['pid']) ? $dis_res_3[0]['pid'] : $dis_res_3[0]['Referee'];//推荐人id
                $dis_9 = intval($dis_res_3[0]['level']) > 0 ? intval($dis_res_3[0]['level']) : 0;//会员分销等级
                $i = 1;
                $dis_21 = 0;//已经获取的团队业绩奖金
                $dis_18 = 0;//已被获取的级差奖金额
                if ($dis_8 == $dis_5)
                {//当购买人id等于推荐人id时推荐人id为空
                    $dis_8 = '';
                }

                //判断内购
                if ($dis_12 == 2)
                {
                    //查询自己等级
                    $dis_res_5 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$dis_5])->field('level')->select()->toArray();
                    //购买人等级  如果分销等级为空 那么分销等级为商品绑定的分销等级
                    $dis_13 = empty($dis_res_5[0]['level']) ? 0 : intval($dis_res_5[0]['level']);
                    $dis_14 = $dis_6['level' . $dis_13]['comm1'];//用户为分销商时应获得的佣金
                    if ($dis_14 > 0)
                    {
                        $dis_11 = "用户" . $dis_5 . "获得" . $dis_14 . "内购佣金";
                        $dis_sql_6 = new DistributionRecordModel();
                        $dis_sql_6->store_id = $store_id;
                        $dis_sql_6->user_id = $dis_5;
                        $dis_sql_6->from_id = $dis_5;
                        $dis_sql_6->money = $dis_14;
                        $dis_sql_6->sNo = $dis_1;
                        $dis_sql_6->level = $i;
                        $dis_sql_6->event = $dis_11;
                        $dis_sql_6->type = 1;
                        $dis_sql_6->add_date = date('Y-m-d H:i:s');
                        $dis_sql_6->genre = 1;//分佣类型 1直推分佣2间推分佣3平级奖4级差奖
                        $dis_sql_6->save();
                        $dis_res_6 = $dis_sql_6->id;
                        if ($dis_res_6 < 1)
                        {
                            return false;
                        }
                    }
                }
                //当推荐人存在时
                if ($dis_8 && !empty($dis_8))
                {
                    //while循环查询上级并计算佣金
                    while ($i <= $dis_4)
                    {
                        // 查询用户推荐人，分销商等级
                        $dis_res_4 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$dis_8])->field('pid,level')->select()->toArray();
                        $dis_9_ = empty($dis_res_4[0]['level']) ? 0 : intval($dis_res_4[0]['level']); // 上级分销商等级
                        if (!empty($dis_9_) && $dis_9_ > 0)
                        {
                            if (!isset($dis_6['level' . $dis_9_]['comm' . $i]))
                            {
                                break;
                            }
                            $dis_10 = $dis_6['level' . $dis_9_]['comm' . $i];//用户应得层级佣金
                            //当佣金大于零时插入佣金记录
                            if ($dis_10 > 0)
                            {   
                                $dis_11 = "用户" . $dis_8 . "获得" . $dis_10 ."佣金";
                                $dis_sql_6 = new DistributionRecordModel();
                                $dis_sql_6->store_id = $store_id;
                                $dis_sql_6->user_id = $dis_8;
                                $dis_sql_6->from_id = $dis_5;
                                $dis_sql_6->money = $dis_10;
                                $dis_sql_6->sNo = $dis_1;
                                $dis_sql_6->level = $i;
                                $dis_sql_6->event = $dis_11;
                                $dis_sql_6->type = 1;
                                $dis_sql_6->add_date = date('Y-m-d H:i:s');
                                $dis_sql_6->genre = $i;//分佣类型 1直推分佣2间推分佣
                                $dis_sql_6->save();
                                $dis_res_6 = $dis_sql_6->id;
                                if ($dis_res_6 < 1)
                                {
                                    return false;
                                }
                            }

                            if($dis_9_ == $dis_9)
                            { // 当上级分销商等级 等于 当前分销商等级 拿平级奖
                                $dis_98 = $dis_6['level' . $dis_9_]['sibling'];
                                //当佣金大于零时插入佣金记录
                                if ($dis_98 > 0)
                                {   
                                    $dis_11 = "用户" . $dis_8 . "获得" . $dis_98 . "佣金";
                                    $dis_sql_6 = new DistributionRecordModel();
                                    $dis_sql_6->store_id = $store_id;
                                    $dis_sql_6->user_id = $dis_8;
                                    $dis_sql_6->from_id = $dis_5;
                                    $dis_sql_6->money = $dis_98;
                                    $dis_sql_6->sNo = $dis_1;
                                    $dis_sql_6->level = $i;
                                    $dis_sql_6->event = $dis_11;
                                    $dis_sql_6->type = 1;
                                    $dis_sql_6->add_date = date('Y-m-d H:i:s');
                                    $dis_sql_6->genre = 3;//分佣类型 1直推分佣2间推分佣3平级奖
                                    $dis_sql_6->save();
                                    $dis_res_6 = $dis_sql_6->id;
                                    if ($dis_res_6 < 1)
                                    {
                                        return false;
                                    }
                                }
                            }
                            else
                            {
                                if($dis_9_ > $dis_9)
                                { // 当上级分销商等级 大于 当前分销商等级 拿平级奖
                                    //计算级差奖  等级相应级差奖-已被领取级差奖
                                    $dis_19 = floatval($dis_6['level' . $dis_9_]['different']) - floatval($dis_18);
                                    $dis_97 = $dis_19 < 0 ? 0 : $dis_19;//层级佣金+级差奖，当获取的级差奖小于或等于0时，级差奖为0
                                    //当佣金大于零时插入佣金记录
                                    if ($dis_97 > 0)
                                    {   
                                        $dis_11 = "用户" . $dis_8 . "获得" . $dis_97 . "佣金";
                                        $dis_sql_6 = new DistributionRecordModel();
                                        $dis_sql_6->store_id = $store_id;
                                        $dis_sql_6->user_id = $dis_8;
                                        $dis_sql_6->from_id = $dis_5;
                                        $dis_sql_6->money = $dis_97;
                                        $dis_sql_6->sNo = $dis_1;
                                        $dis_sql_6->level = $i;
                                        $dis_sql_6->event = $dis_11;
                                        $dis_sql_6->type = 1;
                                        $dis_sql_6->add_date = date('Y-m-d H:i:s');
                                        $dis_sql_6->genre = 4;//4级差奖
                                        $dis_sql_6->save();
                                        $dis_res_6 = $dis_sql_6->id;
                                        if ($dis_res_6 < 1)
                                        {
                                            return false;
                                        }
                                    }
                                    $dis_18 = floatval($dis_6['level' . $dis_9_]['different']) > floatval($dis_18) ? $dis_6['level' . $dis_9_]['different'] : $dis_18;//当可获取级差奖大于已领取级差奖金时更新已领取奖金  
                                }
                            }
                        }

                        if ($dis_res_4 && !empty($dis_res_4[0]['pid']))
                        {
                            //当用户推荐人存在时  继续循环
                            $dis_9 = $dis_9_; // 当前人的等级
                            $dis_8 = $dis_res_4[0]['pid']; // 当前人的上级ID
                            $i++;
                        }
                        else
                        {
                            // 当用户推荐人不存在时，结束循环
                            break;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * 订单拆分后的消息推送和消息记录
     * @param $sNo
     * @param $store_id
     * @param $user_id
     * @param $db
     * @param $shop_id
     */
    public function orderMessage($sNo, $store_id, $user_id,$shop_id)
    {
        
        if(count($shop_id) == 1)
        {
            $r1 = OrderModel::where(['sNo'=>$sNo])->field('z_price,sNo,mch_id')->select()->toArray();
        }
        else
        {
            $r1 = OrderModel::where(['p_sNo'=>$sNo])->field('z_price,sNo,mch_id,status')->select()->toArray();
        }
        if ($r1)
        {

            foreach ($r1 as $ke => $va)
            {
                $r_sNo = $va['sNo'];
                $z_price = $va['z_price'];

                $msg_title = "【" . $r_sNo . "】订单支付成功！";
                $msg_content = Lang("order.13");

                /**买家付款成功通知(站内信)*/
                $pusher = new LaikePushTools();
                $pusher->pushMessage($user_id,$msg_title, $msg_content, $store_id, '');

                // 微信订阅消息发送
                $msgres = new \stdClass();
                $msgres->uid = Tools::get_openid($store_id, $user_id);
                $msgres->sNo = $r_sNo;
                $ordersinfo = OrderDetailsModel::where(['r_sNo'=>$r_sNo])->select()->toArray();
                if (count($ordersinfo) > 0)
                {
                    $msgres->p_name = $ordersinfo[0]['p_name'];
                }
                else
                {
                    $msgres->p_name = '默认商品名';
                }
                $msgres->amount4 = $z_price;
                $this->sendWXTopicMsg($msgres, $store_id);

                $mch_id = $va['mch_id'];
                $mch_id = substr($mch_id, 1, -1);
                $r0 = MchModel::where('id',$mch_id)->field('user_id')->select()->toArray();
                $msg_title = Lang("pay.11");

                $pusher->pushMessage($r0[0]['user_id'], $msg_title, "订单号【" . $r_sNo . "】,请发货", $store_id, '');
            }
        }
    }

    /**
     * 发送微信订阅消息
     * @param $msgres
     * @param $store_id
     * @throws Exception
     */
    public function sendWXTopicMsg($msgres, $store_id)
    {
        try
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '推送微信小程序订阅消息开始 ';
            LaiKeLogUtils::lktLog($Log_content);
            $orderData = array();
            //用户
            $orderData['touser'] = $msgres->uid;
            //跳转地址
            $orderData['page'] = 'pages/index/index';
            //商城ID
            $orderData['store_id'] = $store_id;
            //订单号
            $orderData['character_string1'] = $msgres->sNo;
            //商品名称
            $orderData['thing3'] = $msgres->p_name;
            //支付金额
            $orderData['amount4'] = $msgres->amount4;
            //支付时间
            $time = date("Y-m-d H:i", time());
            $orderData['date2'] = $time;
            WXTopicMsgUtils::orderSuccess($orderData);
            $Log_content = __METHOD__ . '->' . __LINE__ . '推送微信小程序订阅消息结束 ';
            LaiKeLogUtils::lktLog($Log_content);
        } catch (Exception $e)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '推送微信小程序订阅消息失败 ';
            LaiKeLogUtils::lktLog($Log_content);
            LaiKeLogUtils::lktLog(__METHOD__ . '->' . __LINE__ . $e->getMessage());
        }
    }

    // 查看提货码
    public function see_extraction_code()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $id = trim($this->request->post('order_id'));// 订单id

        $user_id = $this->user_list['user_id'];
        $sql1 = "select a.z_price,a.sNo,a.status,a.mch_id,a.extraction_code,a.extraction_code_img,b.p_id,b.p_price,b.num,b.size,b.sid,a.otype from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.id = '$id' and a.user_id = '$user_id'";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            $rew = explode(',', $r1[0]['extraction_code']); // 提现码

            $status = $r1[0]['status']; // 订单状态
            $z_price = $r1[0]['z_price']; // 订单状态
            $sNo = $r1[0]['sNo']; // 订单状态
            $otype = $r1[0]['otype']; // 订单状态

            if ($status == 2)
            {
                if (count($rew) != 3)
                {
                    // 店铺
                    $mch = new MchPublicMethod();
                    $shop = $mch->Settlement2($store_id, $id);
                    $extraction_code1 = $shop['extraction_code'];
                    $extraction_code2 = explode(',', $extraction_code1);
                    $extraction_code = $extraction_code2[0];
                    $extraction_code_img = $shop['extraction_code_img'];
                }
                else
                {
                    if ($rew[2] <= time())
                    { // 提货码有效时间 小于等于 当前时间
                        // 店铺
                        $mch = new MchPublicMethod();
                        $shop = $mch->Settlement2($store_id, $id);
                        $extraction_code1 = $shop['extraction_code'];
                        $extraction_code2 = explode(',', $extraction_code1);
                        $extraction_code = $extraction_code2[0];
                        $extraction_code_img = $shop['extraction_code_img'];
                    }
                    else
                    {
                        $extraction_code = $rew[0]; // 提现码
                        $extraction_code_img = $r1[0]['extraction_code_img']; // 提现码二维码
                    }
                }
            }
            else
            {
                $extraction_code = $rew[0]; // 提现码
                $extraction_code_img = $r1[0]['extraction_code_img']; // 提现码二维码
            }
            
            $array = array('store_id'=>$store_id,'store_type'=>$store_type,'id'=>$id,'extraction_code'=>$extraction_code,'shop_id'=>0,'mch_store_id'=>0);
            $data = DeliveryHelper::Self_pickup_order_to_obtain_goods($array);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            ob_clean();
            $message = Lang('Illegal invasion');
            return output(ERROR_CODE_FFRQ,$message);
        }

    }

    /**
     * @param $user
     * @param $store_id
     * @param $user_id
     * @param $db
     * @param LaiKeLogUtils $lktlog
     * @return array
     */
    public function userInfo($user, $store_id, $user_id,LaiKeLogUtils $lktlog)
    {
        $user_money = $user['money']; // 用户余额
        $user_score = $user['score']; // 用户积分
        $user_password = $user['password']; // 支付密码
        $login_num = $user['login_num']; // 支付密码错误次数
        $verification_time = $user['verification_time']; // 支付密码验证时间
        $verification_time = date('Y-m-d H:i:s', strtotime('+1 day', strtotime($verification_time)));
        $time = date('Y-m-d H:i:s', time());

        if ($login_num == 5)
        {
            if ($time < $verification_time)
            {
                $enterless = false;
            }
            else
            {
                $sql = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->find();
                $sql->login_num = 0;
                $r = $sql->save();
                if (!$r)
                {
                    $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改用户信息失败！user_id:" . $user_id);
                }
                $enterless = true;
            }
        }
        else
        {
            $enterless = true;
        }

        if ($user_password != '')
        {
            $password_status = 1;
        }
        else
        {
            $password_status = 0;
        }
        return array($user_money, $enterless, $password_status);
    }

    // 支付方法
    public function get_config()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        // 接收参数
        $url = addslashes(trim($this->request->param('url'))); // 链接
        $type = addslashes(trim($this->request->param('type'))); // 支付类名称

        // 返回参数
        $res = array(
            'config' => '',
            'url' => urlencode($url),
        );
        if (!empty($type))
        {
            $sql2 = "select config_data from lkt_payment as p left join lkt_payment_config as c on c.pid = p.id where c.store_id='$store_id' and c.status = 1  and p.class_name = '$type' ";
            $res2 = Db::query($sql2);
            if ($res2)
            {
                $res['config'] = json_decode($res2[0]['config_data']);
            }
        }
        $message = Lang('Success');
        return output(200,$message,$res);
    }

    // 查看是否超出范围
    public function isOutOfRange()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $address_id = trim($this->request->post('address_id'));// 地址id
        $productsInfo = trim($this->request->post('productsInfo'));// 商品数据
        
        $productsInfo1 = htmlspecialchars_decode($productsInfo);
        $productsInfo2 = json_decode(stripslashes(html_entity_decode($productsInfo1)),true); // 字符串打散为数组

        $user_id = $this->user_list['user_id'];
        $pid = $productsInfo2['pid']; // 商品ID
        $no_delivery_list = array();

        $sql0 = "select freight from lkt_product_list where id = '$pid' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $freight_id = $r0[0]['freight'];

            $sql1 = "select no_delivery from lkt_freight where store_id = '$store_id' and id = '$freight_id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $no_delivery = $r1[0]['no_delivery']; // 不配送

                if($no_delivery != '')
                {
                    $no_delivery = json_decode($no_delivery,true); // 不配送
                    foreach($no_delivery as $k1 => $v1)
                    {
                        if(!in_array($v1,$no_delivery_list))
                        {
                            $no_delivery_list[] = $v1;
                        }
                    }
                }
            }
        }

        $sql_0 = "select * from lkt_user_address where store_id = '$store_id' and uid = '$user_id' and id = '$address_id' ";
        $r_0 = Db::query($sql_0);
        if ($r_0) 
        {
            $addx = $r_0[0]['sheng'].'-'.$r_0[0]['city'].'-'.$r_0[0]['quyu'];
            if(in_array($addx,$no_delivery_list))
            {
                $message = Lang('nomal_order.2');
                echo json_encode(array('code' => ERROR_CODE_QXZSHDZ, 'message' => $message, 'data'=>array('isOk' => false)));
                exit;
            }
        }
        $data = array('isOk' => true);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 获取支付方式
    public function getPayment()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $address_id = trim($this->request->post('address_id'));// 地址id
        
        // 支付状态
        $payment = Tools::getPayment($store_id);

        $message = Lang('Success');
        return output(200,$message,$payment);
    }

    // 核销记录
    public function get_write_record()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $id = trim($this->request->param('id')); // 订单详情ID
        
        $total = 0;
        $list1 = array();
        $list2 = array();
        $list = array();
        $sql0 = "select count(a.id) as total from lkt_write_record as a left join lkt_order_details as c on a.p_id = c.id left join lkt_mch_store as b on c.mch_store_write_id = b.id where a.p_id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select a.write_time,a.write_code,a.p_id,a.status,b.name from lkt_write_record as a left join lkt_order_details as c on a.p_id = c.id left join lkt_mch_store as b on c.mch_store_write_id = b.id where a.p_id = '$id' and a.write_code != '' ";
        $r1 = Db::query($sql1);
        if($r0)
        {
            $list1 = $r1;
        }

        $sql2 = "select r_write_off_num as refund_num,re_money as refund_price,re_time as write_time,r_type from lkt_return_order where p_id = '$id' order by re_time asc ";
        $r2 = Db::query($sql2);
        if($r2)
        {
            foreach($r2 as $k2 => $v2)
            {
                if($v2['r_type'] == 4 || $v2['r_type'] == 9 || $v2['r_type'] == 15  || $v2['r_type'] == 13)
                {
                    $status = 4;
                }
                else if($v2['r_type'] == 2 || $v2['r_type'] == 8 || $v2['r_type'] == 10)
                {
                    $status = 5;
                }
                else
                {
                    $status = 2;
                }
                $list2[] = array('refund_num'=>$v2['refund_num'],'refund_price'=>$v2['refund_price'],'write_time'=>$v2['write_time'],'status'=>$status);
            }
        }

        $result = array_merge($list1, $list2);
        $list = Tools::arraySort($result,'write_time','asc');

        $data = array('list'=>$list,'num'=>$total);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 上传凭证
    public function upload_credentials()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));

        $sNo = trim($this->request->param('sNo')); // 订单号
        $voucher = trim($this->request->param('voucher')); // 凭证

        $sql0 = "select id from lkt_order where sNo = '$sNo' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            if($voucher == '')
            {
                $message = Lang('order.39');
                return output(109,$message);
            }
            $sql1 = "update lkt_order set voucher = '$voucher',review_status = 1 where sNo = '$sNo' ";
            $r1 = Db::query($sql1);
            if($r1 > 0)
            {
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $message = Lang('Busy network');
                return output(109,$message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }
}
