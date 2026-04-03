<?php
namespace app\common\Plugin;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Plugin\Plugin;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;
use app\common\GETUI\LaikePushTools;
use app\common\Plugin\CouponPublicMethod;
use app\common\Plugin\MchPublicMethod;
use app\common\DeliveryHelper;
use app\common\ReceiveGoodsUtils;
use app\common\LKTConfigInfo;
use app\common\Order;

use app\admin\model\MemberConfigModel;
use app\admin\model\UserModel;

class Members
{   
    // 获取插件状态
    public function is_Plugin($store_id)
    {

        $r0 = MemberConfigModel::where('store_id',$store_id)->field('is_open')->select()->toArray();
        if ($r0)
        {
            $is_open = $r0[0]['is_open'];
        }
        else
        {
            $is_open = 0;
        }
        return $is_open;
    }

    /**
     * 确认订单详情页
     * @return void
     */
    public function settlement(&...$context)
    {
        $action = $context[0];
        //1.列出基础数据
        $user = $action->user;
        $user_id = $user['user_id'];
        $store_id = $action->store_id;
        $store_type = $action->store_type;

        $product1 = addslashes(Request::param('product'));//  商品数组--------['pid'=>66,'cid'=>88]
        $cart_id = addslashes(trim(Request::param('cart_id')));  //购物车id-- 12,13,123,
        $address_id = Request::param('address_id'); //  地址id
        $vipSource = Request::param('vipSource')?Request::param('vipSource'):0;
        $shop_address_id = Request::param('shop_address_id'); //  门店地址id
        $product_type = addslashes(Request::param('product_type'));//产品类型，JP-竞拍商品,KJ-砍价商品
        $buy_type = addslashes(Request::param('buy_type')) ? addslashes(Request::param('buy_type')) : 0;//提交状态 1是再次购买 空是正常提交
        $canshu = addslashes(Request::param('canshu'));//参数
        $coupon_id = trim(Request::param('coupon_id')); // 优惠券id
        $grade_l = addslashes(Request::param('grade_l'));   //会员特惠 兑换券级别
        if($canshu == 'false')
        {
            $canshu = 'true';
        }
        else
        {
            $canshu = 'false'; 
        }

        $lktlog = new LaiKeLogUtils("app/order.log");
        list($product, $shop_list, $cart_id) = Tools::productHandle($product1, $cart_id);
        $products_total = 0;
        // 支付状态
        $payment = Tools::getPayment($store_id);
        // 用户基本信息
        list($user_money, $enterless, $password_status) = Tools::userInfo($user, $store_id, $user_id,$lktlog);

        //2.区分购物车结算和立即购买---列出选购商品
        $products = Tools::products_list($store_id,$cart_id, $product, $product_type,$buy_type);

        //其他限制-------待处理
        //3.列出商品数组-计算总价和优惠，运费
        $products_data = Tools::get_products_data($store_id,$products, $products_total, $product_type);
        $product_id = $products_data['product_id'];
        $product_class = $products_data['product_class'];
        $products_freight = $products_data['products_freight'];
        $products = $products_data['products'];
        $products_total = $products_data['products_total'];
        $total_p = $products_total;

        $is_distribution = $products[0]['list'][0]['is_distribution'];
        $no_delivery_list = array();
        $no_delivery_str = '';
        if($address_id == '')
        {
            // 获取不配送省的名称
            $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);
        }

        //查询默认地址order_details
        $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);
        $addemt = $address ? 1 : 0; // 收货地址状态

        // 会员折扣
        $products_data0 = Tools::get_products_data0($store_id, $products,$products_total, $user_id,'1');
        $grade_rate = $products_data0['grade_rate'];
        $products = $products_data0['products'];
        $products_total = $products_data0['products_total'];

        //4.计算运费
        $freight = Tools::get_freight($products_freight, $products, $address, $store_id, $product_type);
        $products = $freight['products'];

        if(isset($shop_address_id) && $shop_address_id != '')
        {
            $yunfei = 0;
            foreach ($products as $k => $v)
            {
                $products[$k]['product_total'] = $v['product_total'] - $v['freight_price'];
                $products[$k]['freight_price'] = 0;
            }
        }
        else
        {
            $yunfei = $freight['yunfei'];
        }
        $shop_list = array();
        // 店铺
        $mch = new MchPublicMethod();
        $shop = $mch->Settlement($store_id, $products,'',$shop_address_id);
        $shop_status = $shop['shop_status'];
        $time = date('Y-m-d H:i:s');
        if (count($products) == 1)
        {
            $shop_list = $mch->Settlement1($store_id,$products, $shop_address_id);
        }

        $discount_list = array(); // 平台优惠数组
        $subtraction_id = 0;
        $coupon_id_list = array(); // 优惠ID数组
        $coupon_id0 = '';
        $coupon_id_0 = '';
        $platform_discount_id_list = explode(',',$coupon_id);
        $platform_discount_num = count($platform_discount_id_list) - 1; // 最后一调数据的键名
        $platform_discount_num1 = count($platform_discount_id_list) - 2; // 平台优惠的健名
        $zifuchuan = $platform_discount_id_list[$platform_discount_num]; // 优惠类型
        foreach ($platform_discount_id_list as $k => $v)
        {
            if($k != $platform_discount_num)
            {
                $coupon_id_list[] = $v;
            }
            $coupon_id0 = implode(',',$coupon_id_list); // 优惠券使用
        }
        // 满减--插件
        $auto_jian = null;
        $is_subtraction = 0;
        $reduce_money = 0;
        $reduce_name = '';
        $coupon_money = 0;
        $coupon_name = '';
        $mch_preferential_amount = 0;

        $sql_admin = "select shop_id from lkt_admin where store_id = '$store_id' and type = 1 and recycle = 0 ";
        $r_admin = Db::query($sql_admin);
        $admin_mch_id = $r_admin[0]['shop_id'];

        // 优惠券--插件
        if(file_exists(MO_LIB_DIR.'/Plugin/CouponPublicMethod.php'))
        {
            $coupon = new CouponPublicMethod();
            // 查询店铺优惠券信息
            $mch_coupon_list = $coupon->settlement_store_coupons($store_id, $user_id, $products,$coupon_id0,$canshu);
            $coupon_id_0 = $mch_coupon_list['coupon_id'];
            $products = $mch_coupon_list['products'];
            $mch_preferential_amount = $mch_coupon_list['preferential_amount']; // 店铺优惠之和

            // 查询平台优惠券信息
            $coupon_list_0 = $coupon->settlement_platform_coupons($store_id, $user_id, $products,$coupon_id0,$canshu);
            $products = $coupon_list_0['products'];
            $coupon_list = $coupon_list_0['list'];
            if(count($coupon_list) > 0)
            {
                foreach ($coupon_list as $k => $v)
                {
                    $discount_list[] = $v;
                }
            }
        }

        $gift_list = array();

        $message_0 = Lang('nomal_order.0');
        if($canshu != 'false')
        {
            if($zifuchuan == '0' || $zifuchuan == '')
            {
                $discount_list[] = array('coupon_id' => '0', 'coupon_name' => $message_0,'coupon_status'=>true,'discount_type'=>'no_discount','money'=>0);
            }
            else
            {
                $discount_list[] = array('coupon_id' => '0', 'coupon_name' => $message_0,'coupon_status'=>false,'discount_type'=>'no_discount','money'=>0);
            }
        }
        else
        {
            $discount_list[] = array('coupon_id' => '0', 'coupon_name' => $message_0,'coupon_status'=>false,'discount_type'=>'no_discount','money'=>0);
        }

        $discount = 1;
        $coupon_num1 = 0; // 平台优惠类型的数量
        if(count($discount_list) > 0)
        {
            $coupon_num1 = (count($discount_list)-1);
        }
        $preferential_amount = 0;
        $grade_rate_amount = 0;

        if ($grade_l)
        { // 是商品兑换券
            $products_total = 0;
            $total = $yunfei; // 实际付款金额 = 运费
        }
        else
        {
            if($zifuchuan == 'coupon')
            {
                foreach ($coupon_list as $ke => $va)
                {
                    if($va['coupon_status'] == 1)
                    {
                        $preferential_amount = $va['money'];  // 平台优惠金额
                        if ($va['activity_type'] == 1 && $grade_l == 0)
                        {
                            $yunfei = 0;
                            foreach ($products as $k1 => $v1)
                            {
                                $products[$k1]['freight_price'] = 0;
                                foreach ($v1['list'] as $k2 => $v2)
                                {
                                    $products[$k1]['list'][$k2]['freight_price'] = 0;
                                }
                            }
                        }
                    }
                }
            }

            foreach ($products as $k1 => $v1)
            {
                $grade_rate_amount += $v1['grade_rate_amount'];
            }
            $total = $products_total - $mch_preferential_amount - $preferential_amount + $yunfei; // 商品总价-店铺优惠之和-平台优惠+总运费
        }

        $total = number_format($total - $grade_rate_amount,2);
        $grade_rate_amount = number_format($grade_rate_amount, 2);
        $total = number_format($total, 2);

        // $total_discount = $total_p - $products_total;
        $total_discount = number_format(round($mch_preferential_amount,2) + round($preferential_amount,2) + round($grade_rate_amount,2),2);  //优惠金额

        //5.返回数据
        ob_clean();
        if($canshu == 'true' )
        {
            $coupon_id = $coupon_id_0 . ',0' ;
        }
        $data = array('payment' => $payment, 'status' => 1, 'products' => $products, 'is_distribution' => $is_distribution, 'discount' => $discount,
            'products_total' => $products_total, 'freight' => $yunfei, 'total' => $total, 'user_money' => $user_money, 'address' => $address,
            'addemt' => $addemt, 'coupon_id' => $coupon_id, 'coupon_name' => $coupon_name, 'coupon_money' => $coupon_money, 'reduce_money' => $reduce_money,
            'password_status' => $password_status, 'enterless' => $enterless, 'reduce_name' => $reduce_name, 'is_subtraction' => $is_subtraction,
            'shop_status' => $shop_status, 'shop_list' => $shop_list, 'grade_rate' => $grade_rate,'coupon_list'=>$discount_list,'coupon_num'=>$coupon_num1,
            'mch_preferential_amount'=>$mch_preferential_amount,'preferential_amount'=>$preferential_amount,'grade_rate_amount'=>$grade_rate_amount,
            'total_discount'=>$total_discount,'gift_list'=>$gift_list,'admin_mch_id'=>$admin_mch_id);
        $message = Lang("Success");
        echo json_encode(array('code' => 200,'data'=>$data,'message'=>$message));
        exit;
    }

    /**
     * 回调参数校验
     * @param mixed ...$context
     * @return mixed|void
     */
    public function toCheck(&...$context)
    {   
        $trade_no = $context[0][0];
        $log = $context[0][1];
        $log_name = $context[0][2];
        $sql = "select data from lkt_order_data where  trade_no = '$trade_no'";
        $log->log_result($log_name, "sql： " . $sql);
        $r = Db::query($sql);
        $log->log_result($log_name, "查询结果： " . json_encode($r));
        if (!$r)
        {
            $log->log_result($log_name, "普通订单回调失败信息: \n 支付订单号：$trade_no 没有查询到订单信息 \r\n");
            ob_clean();
            echo 'error';
            exit;
        }
        $order_data = unserialize($r[0]['data']);
        $store_id = $order_data ? $order_data['store_id'] : 0;
        $pay_type = $order_data ? $order_data['pay'] : 0;
        if ($pay_type == 'tt_alipay')
        {
            $pay_type = 'alipay';
        }
        $log->log_result($log_name, "商城ID： " . $store_id);
        $log->log_result($log_name, "支付方式： " . $pay_type);
        $config = LKTConfigInfo::getPayConfig($store_id, $pay_type);
        $log->log_result($log_name, "支付数据： " . json_encode($config));
        if (empty($config))
        {
            $log->log_result($log_name, "普通订单执行日期：" . date('Y-m-d H:i:s') . "\n支付暂未配置 商城ID：$store_id ，支付类型：$pay_type ，无法调起支付！\r\n");
            return 'file';
        }

        $log->log_result($log_name, " oever ");
        return $config;
    }

    /**
     * @inheritDoc
     */
    public function paycb(&...$context)
    {   
        $log = $context[0]['log'];
        $log_name = $context[0]['log_name'];
        $total = $context[0]['total'];
        $trade_no = $context[0]['trade_no'];
        $source = $context[0]['source'];
        
        $log->log_result($log_name, "支付金额： " . $total);
        if (empty($trade_no) || $total <= 0)
        {
            $log->log_result($log_name, "普通订单回调失败信息: \n 订单：$trade_no 支付金额：$total \r\n");
            ob_clean();
            echo 'error';
            exit;
        }
        
        $type = substr($trade_no, 0, 2);
        $log->log_result($log_name, "【trade_no1】''''$trade_no:\n\n");
        $log->log_result($log_name, "【订单类型】''''$type:\n\n");
        // $sql = "select data from lkt_order_data where trade_no = '$trade_no'";
        // $r = Db::query($sql);
        // $data = unserialize($r[0]['data']);
        // $z_price = $data['total'];
        // if (floatval($z_price) != floatval($total))
        // {
        //     $data['total'] = $total;
        //     $data = serialize($data);
        //     $db->update("update lkt_order_data set data='$data' where trade_no='$trade_no'");
        //     $log->log_result($log_name, "【付款金额有误】:\n 应付金额为$z_price \n");
        // }
        if ($type == 'CZ')
        {
            //充值
            $dsql = "select data,status from lkt_order_data where trade_no = '$trade_no'";
            $dres = Db::query($dsql);
            $data = unserialize($dres[0]['data']);
            $log->log_result($log_name, "订单数据： " . json_encode($data));
            $rec = false;
            if ($dres)
            {
                $status = $dres[0]['status'];
                if($status == 0)
                {
                    $order = new Order;
                    if($source == 'alipay')
                    {
                        $cmoney = $total;
                    }
                    else
                    {
                        $cmoney = $total / 100;
                    }
                    
                    $log->log_result($log_name, "金额： " . $cmoney);
                    $log->log_result($log_name, "订单号： " . $trade_no);
                    $rec = $order->cz($data, $cmoney, $trade_no);
                }
            }
            $log->log_result($log_name, "【充值处理结果】:\n" . $rec . "\n");
        }
        else
        {
            $log->log_result($log_name, "【会员等级处理结果】:\n  $type 订单类型不存在 \n");
        }
    }

    /**
     * @inheritDoc
     */
    public function walletcb(...$context)
    {
        echo json_encode(array('code' => 0, 'message' => '操作有误！'));
        exit;
    }

    /**
     * 支付前
     * @param mixed ...$context
     * @return mixed|void
     */
    public function preparePay(...$context)
    {   
        $action = $context[0];

        $store_id = $action->store_id ;
        $sNo = $action->sNo;
        $payment_money = $action->payment_money;
        $type =  $action->type;
        $real_sno = $sNo;
        $order_types = $action->order_types ;
        $remarks = $action->remarks ;

        //余额充值 和 会员等级充值
        $total = $payment_money;
        if($order_types == 'CZ')
        {
            $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>1,'id'=>0));
            $currency_symbol = $userCurrency[0]['currency_symbol'];
            $exchange_rate = $userCurrency[0]['exchange_rate'];
            $currency_code = $userCurrency[0]['currency_code'];

            $array = array('order_id' => $sNo, 'user_id' => $action->user['user_id'], 'trade_no' => $real_sno, 'pay' => $type, 'total' => $total, 'store_id' => $store_id,'remarks'=>$remarks);
            $data = serialize($array);
            $sql = "insert into lkt_order_data(trade_no,order_type,data,addtime,pay_type,currency_symbol,exchange_rate,currency_code) values('$sNo','$order_types','$data',CURRENT_TIMESTAMP,'$type','$currency_symbol','$exchange_rate','$currency_code')";
            $rid = Db::execute($sql);
        }
        return array($real_sno,$total);
    }

    //定时任务
    public function dotask($params)
    {   
        $store_id = $params->store_id;
        $this->exceed($store_id);
        $this->remakeBox($store_id);
    }

    //过期
    public function exceed($store_id)
    {   
        $lktlog = new LaiKeLogUtils();
        $time = date('Y-m-d H:i:s');
        $res = UserModel::where(['store_id'=>$store_id,'grade'=>1,'is_out'=>0])->where('grade_end','<',$time)->select()->toArray();
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $user_id = $value['user_id'];
                $sql_u = UserModel::where('user_id',$user_id)->find();
                $sql_u->is_out = 1;
                $res_u = $sql_u->save();
                if(!$res_u)
                {
                    $lktlog->log("common/Members.log",__METHOD__ . ":" . __LINE__ . "修改会员状态失败！user_id:" . $user_id);
                }
            }
        }
        return;
    }

    //重制弹框
    public function remakeBox($store_id)
    {
        $lktlog = new LaiKeLogUtils();
        $time = date('H:i:s');
        if($time < "00:05:00")
        {
            $res = UserModel::where(['store_id'=>$store_id,'grade'=>1,'is_out'=>0,'is_box' => 0])->select()->toArray();
            if($res)
            {
                foreach ($res as $key => $value) 
                {
                    $user_id = $value['user_id'];
                    $sql_u = UserModel::find($user_id);
                    $sql_u->is_box = 1;
                    $res_u = $sql_u->save();
                    if(!$res_u)
                    {
                        $lktlog->log("common/Members.log",__METHOD__ . ":" . __LINE__ . "修改会员状态失败！user_id:" . $user_id);
                    }
                }
            }
        }
        
    }  
    
    // 获取插件状态（用户）
    public function Get_plugin_status($store_id)
    {
        $is_status = 0; // 用户端插件状态 0.关闭 1.开启

        $r0 = MemberConfigModel::where(['store_id'=>$store_id])->field('is_open')->select()->toArray();
        if ($r0)
        {
            $is_status = $r0[0]['is_open'];
        }
        
        return $is_status;
    }

    // 获取插件状态（店铺）
    public function Get_plugin_status_mch($store_id)
    {
        $status = 0; // 店铺端插件状态 0.关闭 1.开启
        $sql0 = "select status from lkt_plugins where store_id = '$store_id' and plugin_code = 'member' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $status = $r0[0]['status'];
        }
        
        return $status;
    }
}
