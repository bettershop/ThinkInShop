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
use app\common\Order;

use app\admin\model\AdminModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\CartModel;
use app\admin\model\BuyAgainModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProductListModel;
use app\admin\model\StockModel;
use app\admin\model\UserFirstModel;
use app\admin\model\DistributionConfigModel;
use app\admin\model\OrderConfigModel;
use app\admin\model\DistributionRecordModel;
use app\admin\model\UserDistributionModel;
use app\admin\model\DistributionGradeModel;

class Distribution 
{

    // 获取插件状态
    public function is_Plugin($store_id)
    {
        $r0 = DistributionConfigModel::where('store_id', $store_id)->field('status')->select()->toArray();
        if ($r0)
        {
            $is_display = $r0[0]['status'];
        }
        else
        {
            $is_display = 2;
        }
        return $is_display;
    }
    
    // 确认订单页面
    public function settlement(&...$context)
    {
        $action = $context[0];
        //1.列出基础数据
        $user = $action->user;
        $user_id = $user['user_id'];
        $store_id = $action->store_id;
        $store_type = $action->store_type;

        $product1 = addslashes(Request::post('product'));//  商品数组--------['pid'=>66,'cid'=>88]
        $cart_id = addslashes(trim(Request::post('cart_id')));  //购物车id-- 12,13,123,
        $address_id = Request::post('address_id'); //  地址id
        $shop_address_id = Request::post('shop_address_id'); //  门店地址id
        $product_type = addslashes(Request::post('product_type'));//产品类型，JP-竞拍商品,KJ-砍价商品
        $buy_type = addslashes(Request::post('buy_type')) ? addslashes(Request::post('buy_type')) : 0;//提交状态 1是再次购买 空是正常提交
        $canshu = addslashes(Request::post('canshu'));//参数
        $coupon_id = trim(Request::post('coupon_id')); // 优惠券id

        // $product1 = "";
        // $cart_id = "5410,5411,5417,5420";
        $lktlog = new LaiKeLogUtils();
        // 处理商品
        list($product, $shop_list, $cart_id) = Tools::productHandle($product1, $cart_id);

        // 支付状态
        $payment = Tools::getPayment($store_id);

        // 用户基本信息
        list($user_money, $enterless, $password_status) = Tools::userInfo($user, $store_id, $user_id,$lktlog);

        //2.区分购物车结算和立即购买---列出选购商品
        $products = Tools::products_list($store_id,$cart_id, $product, $product_type,$buy_type);
        $p_id = $products[0]['pid'];
        $s_id = $products[0]['cid'];

        $products_total = 0;
        //3.列出商品数组-计算总价和优惠，运费
        $products_data = Tools::get_products_data($store_id,$products, $products_total, $product_type);
        $product_id = $products_data['product_id'];
        $product_class = $products_data['product_class'];
        $products_freight = $products_data['products_freight'];
        $products = $products_data['products'];
        $products_total = $products_data['products_total'];
        $is_supplier_pro = $products_data['is_supplier_pro']; // true:不是供应商商品  false:供应商商品
        $is_distribution = 1;

        $no_delivery_str = '';
        if($address_id == '')
        { // 获取不配送省的名称
            $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);
        }

        //查询默认地址order_details
        $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);
        $addemt = $address ? 1 : 0; // 收货地址状态

        $products_data0 = Tools::get_products_data0($store_id, $products,$products_total, $user_id);
        $grade_rate = $products_data0['grade_rate'];
        $products = $products_data0['products'];
        $products_total = $products_data0['products_total'];

        //4.计算运费
        $freight = Tools::get_freight($products_freight, $products, $address, $store_id, $product_type);
        $products = $freight['products'];

        $yunfei = 0;
        if(isset($shop_address_id) && $shop_address_id != '')
        {
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
        $shop_status = 0;
        // 店铺
        $mch = new MchPublicMethod();
        $shop = $mch->Settlement($store_id, $products,'',$shop_address_id);
        // $shop_status = $shop['shop_status'];
        $time = date('Y-m-d H:i:s');
        if (count($products) == 1)
        {
            $shop_list = $mch->Settlement1($store_id,$products, $shop_address_id);
        }

        $discount_list = array(); // 平台优惠数组
        $subtraction_id = 0;
        // $coupon_id_list = array(); // 优惠ID数组
        $coupon_id0 = '';
        // $coupon_id_0 = '';
        $platform_discount_id_list = explode(',',$coupon_id);
        $platform_discount_num = count($platform_discount_id_list) - 1; // 最后一调数据的键名
        $platform_discount_num1 = count($platform_discount_id_list) - 2; // 平台优惠的健名
        $zifuchuan = $platform_discount_id_list[$platform_discount_num]; // 优惠类型

        // 满减--插件
        $auto_jian = null;
        $is_subtraction = 0; // 是否有满减
        $reduce_money = 0; // 满减金额
        $reduce_name = ''; // 满减名称
        $coupon_money = 0; // 优惠券金额
        $coupon_name = ''; // 优惠券名称
        $coupon_status = false;
        $coupon_num = 0;

        $gift_list = array();

        $message_0 = Lang('nomal_order.0');
        $discount = 100;
        $level = 0;
        //查询用户分销等级对应折扣
        $sql = "select a.level,b.sets,b.discount from lkt_user_distribution as a left join lkt_distribution_grade as b on a.level = b.id where a.store_id = '$store_id' and a.user_id = '$user_id' and a.level > 0";
        $res = Db::query($sql);
        if ($res)
        {
            $level = $res[0]['level']; // 购买人分销等级
        }

        $sql_goods = "select distribution_rule,rules_set from lkt_distribution_goods where store_id = '$store_id' and p_id = '$p_id' and s_id = '$s_id' and recycle = 0 ";
        $r_goods = Db::query($sql_goods);
        if($r_goods)
        {
            if($r_goods[0]['distribution_rule'] == 1)
            {
                if ($res)
                {
                    $sets = json_decode($res[0]['sets'],true);
                    if($sets['zhekou'] == 1)
                    {
                        $discount = floatval($res[0]['discount']);
                    }   
                }
            }
            else
            {
                $rules_set = json_decode($r_goods[0]['rules_set'],true);
                if($level != 0)
                {
                    foreach ($rules_set as $k_set => $v_set)
                    {
                        if($v_set['id'] == $level)
                        {
                            $discount = $v_set['diy_discount'];
                        }
                    }
                }
            }
        }

        $total = round($products_total * $discount * 0.01,2) ; // 商品总价 * 分销折扣 + 运费   实际付款金额
        $total_discount = round($products_total * (100 - $discount),2);//总优惠金额
        $total = $total + $yunfei;
        $total = round($total,2);

        //5.返回数据
        ob_clean();
        $data = array('addemt' => $addemt,'address' => $address,'coupon_id' => $coupon_id,'coupon_list'=>$discount_list,'coupon_money' => $coupon_money,'coupon_name' => $coupon_name,'coupon_num'=>$coupon_num,'coupon_status'=>$coupon_status,'discount' => $discount,'enterless' => $enterless,'freight' => $yunfei,'grade_rate' => $grade_rate,'grade_rate_amount'=>0,'is_distribution' => $is_distribution,'is_subtraction' => $is_subtraction,'mch_preferential_amount'=>0,'password_status' => $password_status,'payment' => $payment,'preferential_amount'=>0,'products' => $products,'products_total' => $products_total,'reduce_money' => $reduce_money,'reduce_name' => $reduce_name,'shop_list' => $shop_list,'shop_status' => $shop_status,'is_supplier_pro'=>$is_supplier_pro,'status' => 1,'total' => $total,'total_discount'=>$total_discount,'user_money' => $user_money);
        $message = Lang("Success");
        echo json_encode(array('code' => 200,'data'=>$data,'message'=>$message));
        exit;
    }

    // 生成订单
    public function payment(&...$context)
    {
        // 2.数据准备
        $action = $context[0];
        //1.列出基础数据
        $user = $action->user;
        $user_id = $user['user_id'];
        $store_id = $action->store_id;
        $store_type = $action->store_type;
        $access_id = $action->access_id;

        $product1 = addslashes(Request::post('product'));//  商品数组--------['pid'=>66,'cid'=>88]
        $cart_id = addslashes(trim(Request::post('cart_id')));  // 购物车id-- 12,13,123,
        $type = trim(Request::post('type')) ? Request::post('type') : 'FX'; // 订单类型
        $address_id = Request::post('address_id'); //  地址id
        $coupon_id = trim(Request::post('coupon_id')); // 优惠券id
        $pay_type = addslashes(trim(Request::post('pay_type'))); // 支付方式
        $buy_type = addslashes(Request::post('buy_type')) ? addslashes(Request::post('buy_type')) : 0; // 提交状态 1是再次购买 空是正常提交
        $shop_address_id = Request::post('shop_address_id',0)?Request::post('shop_address_id',0):0; //  门店地址id
        $remarks = trim(Request::post('remarks')); //  订单备注

        $remarks = htmlspecialchars_decode($remarks); // 将特殊的 HTML 实体转换回普通字符
        $remarks = json_decode($remarks, true);

        // $product1 = "";
        // $cart_id = "5410,5411,5417,5420";

        $products_total = 0;
        $total = 0.00;
        $yunfei = 0;

        $product = '';
        list($product, $shop_list, $cart_id) = Tools::productHandle($product1, $cart_id);

        //2.区分购物车结算和立即购买---列出选购商品
        $products = Tools::products_list($store_id, $cart_id, $product, $type,$buy_type);
        $p_id = $products[0]['pid'];
        $s_id = $products[0]['cid'];
        if(empty($address_id))
        {
            $message = Lang('nomal_order.1');
            echo json_encode(array('code' => ERROR_CODE_QXZSHDZ, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }

        //3.列出商品数组-计算总价和优惠，运费
        $products_data = Tools::get_products_data($store_id,$products, $products_total, $type);
        $product_id = $products_data['product_id'];
        $product_class = $products_data['product_class'];
        $products_freight = $products_data['products_freight'];
        $products = $products_data['products'];
        $products_total = $products_data['products_total'];
        $spz_price = $products_data['products_total'];

        // 获取不配送省的名称
        $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);

        //查询默认地址order_details
        $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);
        $cpc = $address['cpc'];
        $shop_cpc = $cpc;
        $mobile = $address['tel'];
        $name = $address['name'];
        $sheng = $address['sheng'];
        $shi = $address['city'];
        $xian = $address['quyu'];
        $address_xq = $address['address'];
        $code = $address['code'];

        $products_data0 = Tools::get_products_data0($store_id, $products,$products_total, $user_id);
        $grade_rate = $products_data0['grade_rate'];
        $products = $products_data0['products'];
        $products_total = $products_data0['products_total'];

        // 6.计算运费
        $freight = Tools::get_freight($products_freight, $products, $address, $store_id, $type);
        $products = $freight['products'];

        if(isset($shop_address_id) && $shop_address_id != 0)
        {
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

        // 定义初始化数据
        $z_num = 0;
        // 店铺
        $otype = 'FX';
        $order_status = 0;
        $shop_status = 0;
        $extraction_code = '';
        $extraction_code_img = '';

        $give_id = 0; // 赠品ID
        $subtraction_id = 0; // 满减ID
        $reduce_money = 0; // 满减优惠金额
        $reduce_name_array = ''; // 满减名称
        $coupon_money = 0; // 优惠券优惠金额
        $coupon_status = 0;

        $mch = new mchPublicMethod();
        if ($shop_address_id)
        {
            //自提
            $shop = $mch->Settlement($store_id, $products, 'payment',$shop_address_id);
            $shop_cpc = $shop['cpc'];
            $sheng = $shop['sheng'];
            $shi = $shop['shi'];
            $xian = $shop['xian'];
            $address_xq = $shop['address'];
            $code = $shop['code'];
            $shop_status = $shop['shop_status'];
            $extraction_code = $shop['extraction_code'];
            $extraction_code_img = $shop['extraction_code_img'];
            $yunfei = 0;
        }
        
        $Toosl = new Tools(1,1);
        $sNo = $Toosl->Generate_order_number($type, 'sNo'); // 生成订单号
        $real_sno = $Toosl->Generate_order_number($type, 'real_sno'); // 生成支付订单号
        
        $mch_id = '';
        $remarks_0 = array();
        $remarks_status = false;

        $coupon_id_list = array();
        $coupon_id0 = 0;
        $subtraction_id = 0;
        $platform_discount_id_list = explode(',',$coupon_id);
        $platform_discount_num = count($platform_discount_id_list) - 1; // 最后一调数据的键名
        $platform_discount_num1 = count($platform_discount_id_list) - 2; // 平台优惠的健名
        $zifuchuan = $platform_discount_id_list[$platform_discount_num]; // 优惠类型

        $preferential_amount = 0;
        $mch_preferential_amount = 0;
        $discount = 100;
        $level = 0;
        //查询用户分销等级对应折扣
        $sql = "select a.level,b.sets,b.discount from lkt_user_distribution as a left join lkt_distribution_grade as b on a.level = b.id where a.store_id = '$store_id' and a.user_id = '$user_id' and a.level > 0";
        $res = Db::query($sql);
        if ($res)
        {
            $level = $res[0]['level']; // 购买人分销等级
        }

        $sql_goods = "select distribution_rule,rules_set from lkt_distribution_goods where store_id = '$store_id' and p_id = '$p_id' and s_id = '$s_id' and recycle = 0 ";
        $r_goods = Db::query($sql_goods);
        if($r_goods)
        {
            if($r_goods[0]['distribution_rule'] == 1)
            {
                if ($res)
                {
                    $sets = json_decode($res[0]['sets'],true);
                    if($sets['zhekou'] == 1)
                    {
                        $discount = floatval($res[0]['discount']);
                    }   
                }
            }
            else
            {
                $rules_set = json_decode($r_goods[0]['rules_set'],true);
                if($level != 0)
                {
                    foreach ($rules_set as $k_set => $v_set)
                    {
                        if($v_set['id'] == $level)
                        {
                            $discount = $v_set['diy_discount'];
                        }
                    }
                }
            }
        }

        $total = round($products_total * $discount * 0.01,2) + $yunfei ; // 商品总价 * 分销折扣 + 运费      实际付款金额
        if ($total <= 0)
        {
            $total = 0.01;
        }

        Db::startTrans();
        $coupon_id = $coupon_id0;
        foreach ($products as $k => $v)
        {   
            $mch_id .= $v['shop_id'] . ',';
            $mch_id1 = $v['shop_id'];
            if($remarks)
            {
                $remarks_0[$mch_id1] = $remarks[$k];
                if($remarks[$k] != '')
                {
                    $remarks_status = true;
                }
            }
            //如果是多店铺，添加一条购买记录
            $mch->addMchBrowse($mch_id, $store_id, $mch_id1, $user_id);

            // 循环商品数据
            foreach ($v['list'] as $key => $value)
            {
                $pid = $value['pid'];
                $cid = $value['cid'];
                $num = $value['num'];
                $z_num += $value['num'];
                $product_title = addslashes($value['product_title']);
                $freight_price = floor(isset($value['freight_price']) ? $value['freight_price'] : 0);
                // 循环插入订单附表 ，添加不同的订单详情
                $freight_price = $shop_address_id ? 0 : $freight_price;
                
                $sql_insert = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pid,'p_name'=>$product_title,'p_price'=>$value['price'],'num'=>$num,'unit'=>$value['unit'],'r_sNo'=>$sNo,'add_time'=>date("Y-m-d H:i:s"),'r_status'=>$order_status,'size'=>$value['size'],'sid'=>$cid,'freight'=>$freight_price,'after_discount'=>round($value['price'] * $num * $discount,2));
                $beres = Db::name('order_details')->insertGetId($sql_insert);
                if ($beres < 1)
                { // 添加失败
                    $this->log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数：" . json_encode($sql_insert));
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }

                // if ($cart_id != '' && $buy_type != 1)
                // {
                //     // 删除对应购物车内容
                //     $res_del = CartModel::where(['store_id'=>$store_id,'Goods_id'=>$pid,'user_id'=>$user_id,'Size_id'=>$cid])->delete();
                //     if ($res_del < 1)
                //     {
                //         Db::rollback();
                //         ob_clean();
                //         $message = Lang('nomal_order.3');
                //         echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                //         exit;
                //     }
                // }
                // elseif($cart_id != '' && $buy_type == 1)
                // {   
                //     $res_del = BuyAgainModel::where(['store_id'=>$store_id,'Goods_id'=>$pid,'user_id'=>$user_id,'Size_id'=>$cid])->delete();
                //     // 删除对应再次购买购物车内容
                //     if ($res_del < 1)
                //     {
                //         Db::rollback();
                //         ob_clean();
                //         $message = Lang('nomal_order.3');
                //         echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                //         exit;
                //     }
                // }

                // 根据商品ID、属性ID，查询属性数据
                $r0 = ConfigureModel::where(['id'=>$cid,'pid'=>$pid])->field('num,min_inventory')->select()->toArray();
                $total_num = $r0[0]['num']; // 剩余库存
                $min_inventory = $r0[0]['min_inventory']; // 预警值

                // 根据商品ID，修改商品库存
                $res_del1 = Db::name('product_list')->where('id',$pid)->update(['num' =>  Db::raw('num - '.$num)]);
                if ($res_del1 <= 0)
                {
                    $this->log(__METHOD__ . ":" . __LINE__ . "修改商品库存失败！pid:" . $pid);
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }

                // 根据属性ID，修改属性库存
                $res_del2 = Db::name('configure')->where('id',$cid)->update(['num' =>  Db::raw('num - '.$num)]);
                if ($res_del2 <= 0)
                {
                    $this->log(__METHOD__ . ":" . __LINE__ . "修改商品属性库存失败！cid:" . $cid);
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }
                
                $content = $user_id . '生成订单所需' . $num;
                $sql_stock_insert = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>1,'user_id'=>$user_id,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content);
                Db::name('stock')->insert($sql_stock_insert);

                if($total_num - $num <= $min_inventory)
                {   
                    $content1 = '预警';
                    $sql_stock_insert1 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>$min_inventory,'type'=>2,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content1);
                    Db::name('stock')->insert($sql_stock_insert1);

                    $message_9 = "商品ID为".$pid."的商品库存不足，请尽快补充库存";
                    $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>$mch_id1,'type'=>9,'parameter'=>$cid,'content'=>$message_9);
                    PC_Tools::add_message_logging($message_logging_list9);
                }
            }
        }

        if($remarks_status)
        {
            $remarks = serialize($remarks_0);
        }
        else
        {
            $remarks = '';
        }

        $mch_id = rtrim($mch_id, ',');
        $mch_id = ',' . $mch_id . ',';

        $order_failure_time = Tools::Obtain_expiration_time(array('store_id'=>$store_id,'otype'=>$otype));

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>$currency_id));
        $currency_symbol = $userCurrency[0]['currency_symbol'];
        $exchange_rate = $userCurrency[0]['exchange_rate'];
        $currency_code = $userCurrency[0]['currency_code'];

        $sql_order_insert = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'num'=>$z_num,'z_price'=>$total,'sNo'=>$sNo,'shop_cpc'=>$shop_cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_xq,'code'=>$code,'remark'=>'','pay'=>$pay_type,'add_time'=>date("Y-m-d H:i:s"),'status'=>$order_status,'coupon_id'=>$coupon_id,'subtraction_id'=>$subtraction_id,'consumer_money'=>0,'coupon_activity_name'=>$reduce_name_array,'spz_price'=>$spz_price,'reduce_price'=>$reduce_money,'coupon_price'=>$coupon_money,'source'=>$store_type,'otype'=>$otype,'mch_id'=>$mch_id,'p_sNo'=>'','bargain_id'=>0,'comm_discount'=>$discount,'remarks'=>$remarks,'real_sno'=>$real_sno,'self_lifting'=>$shop_status,'extraction_code'=>$extraction_code,'extraction_code_img'=>$extraction_code_img,'grade_rate'=>$grade_rate,'z_freight'=>$yunfei,'preferential_amount'=>$preferential_amount,'single_store'=>$shop_address_id,'order_failure_time'=>$order_failure_time,'currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code);
        $r_o = Db::name('order')->insertGetId($sql_order_insert);
        if ($r_o > 0)
        {   
            // 分销
            $comm = $action->order_distribution($pid,$sNo);
            if ($comm == false)
            {
                Db::rollback();
                $lktlog->customerLog(__METHOD__ . ":" . __LINE__ . "分销记录创建失败！" . $sNo);
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => 400, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            Db::commit();
            $arr = array('sNo' => $sNo, 'total' => $total, 'order_id' => $r_o,'order_type'=>'FX','orderTime'=>date("Y-m-d H:i:s"));
            $message = Lang("Success");
            echo json_encode(array('code' => 200, 'data' => $arr,'message'=>$message));
            exit;
        }
        else
        {
            $this->log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数：" . json_encode($sql_order_insert));
            if ($coupon_id0)
            {
                if(file_exists('../app/common/CouponPublicMethod.php'))
                {
                    $coupon = new coupon();
                    $coupon_Log_content = '会员' . $user_id . '使用领取优惠券ID为' . $coupon_id0 . '生成订单失败！';
                    $coupon->couponLog($coupon_id0, $coupon_Log_content);
                }
            }
            //回滚删除已经创建的订单
            Db::rollback();
            $message = Lang('nomal_order.6');
            echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }
    }

    /**
     * 回调参数校验
     * @param mixed ...$context
     * @return mixed|void
     */
    public function toCheck(&...$context)
    {
        LaiKeLogUtils::log('common/zfjc.log', '开始');
        LaiKeLogUtils::log('common/zfjc.log', json_encode($context));
        $trade_no = $context[0][0];
        $log = $context[0][1];
        $log_name = $context[0][2];
        $sql = "select store_id,pay from lkt_order where (real_sno='$trade_no' or sNo='$trade_no')";
        $r = Db::query($sql);
        LaiKeLogUtils::log('common/zfjc.log', $sql);
        if (!$r)
        {
            LaiKeLogUtils::log('common/zfjc.log', 'error');
            $log->log_result($log_name, "普通订单回调失败信息: \n 支付订单号：$trade_no 没有查询到订单信息 \r\n");
            ob_clean();
            echo 'error';
            exit;
        }
        $store_id = $r[0]['store_id'];
        $pay_type = $r[0]['pay'];
        if ($pay_type == 'tt_alipay')
        {
            $pay_type = 'alipay';
        }
        $config = LKTConfigInfo::getPayConfig($store_id, $pay_type);
        if (empty($config))
        {
            LaiKeLogUtils::log('common/zfjc.log', 'file');
            $log->log_result($log_name, "普通订单执行日期：" . date('Y-m-d H:i:s') . "\n支付暂未配置 商城ID：$store_id ，支付类型：$pay_type ，无法调起支付！\r\n");
            return 'file';
        }

        LaiKeLogUtils::log('common/zfjc.log', '==><==' . json_encode($config));
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
        $transaction_id = '';
        if(isset($context[0]['transaction_id']))
        {
            $transaction_id = $context[0]['transaction_id'];
        }

        if (empty($trade_no) || $total <= 0)
        {
            $log->log_result($log_name, "普通订单回调失败信息: \n 订单：$trade_no 支付金额：$total \r\n");
            ob_clean();
            echo 'error';
            exit;
        }

        $type = substr($trade_no, 0, 2);
        $log->log_result($log_name, "【trade_no1】''''$trade_no:\n\n");
        $sql = "select sNo,z_price from lkt_order where real_sno='$trade_no'";
        $r = Db::query($sql);
        $trade_no = $r[0]['sNo'];
        $type = substr($trade_no, 0, 2);
        $z_price = $r[0]['z_price'];
        if (floatval($z_price) != floatval($total))
        {
            Db::execute("update lkt_order set z_price='$total' where real_sno='$trade_no'");
            $log->log_result($log_name, "【普通订单付款金额有误】:\n 应付金额为$z_price \n");
        }

        $sql = "select * from lkt_order where sNo='$trade_no'";
        $r = Db::query($sql);
        if ($r)
        {
            $status = $r[0]['status'];
            if ($status < 1)
            {
                $order = new order;
                $order->up_order((array)$r[0]);
                $log->log_result($log_name, "【普通订单data】:\n" . json_encode((array)$r[0]) . "\n");
            }
        }
    }

    /**
     * @inheritDoc
     */
    public function walletcb(...$context)
    {
        $action = $context[0];

        $store_id = $action->store_id;
        $payment_money = $action->payment_money;
        $order_types = $action->order_types;
        $sNo = $action->sNo;
        $user_id = $action->user_id;

        $action->gndd($store_id, $payment_money, $sNo, $user_id,$order_types);
        return;
    }

    /**
     * 支付前
     * @param mixed ...$context
     * @return mixed|void
     */
    public function preparePay(...$context)
    {
        $action = $context[0];

        $store_id = $action->store_id;
        $sNo = $action->sNo;
        $payment_money = $action->payment_money;
        $type = $action->type;
        $order_types = $action->order_types;
        $real_sno = Tools::order_number($order_types);

        $up_orderType_Res = Db::name('order')->where(['store_id'=>$store_id,'sNo'=>$sNo])->update(['pay'=>$type,'real_sno'=>$real_sno]);

        $total = $payment_money;
        return array($real_sno, $total);
    }

    //定时任务
    public function dotask($params)
    {
        $store_id = $params->store_id;
        $lktlog = new LaiKeLogUtils();
        //获取订单售后期
        $res_c = OrderConfigModel::where('store_id',$store_id)->field('order_after')->select()->toArray();
        if($res_c)
        {
            $order_after = $res_c[0]['order_after'];
        }
        else
        {
            $order_after = 0;
        }
        $time = date("Y-m-d H:i:s",strtotime("-$order_after day"));

        //获取未结算订单
        $sql_o = "select a.sNo from lkt_order as a left join lkt_return_order as b on a.sNo = b.sNo where a.store_id = '$store_id' and a.otype = 'FX' and a.status = 5 and a.commission_type = 0 and a.arrive_time <= '$time' and b.sNo is null
            union all    
            select a.sNo from lkt_order as a left join lkt_return_order as b on a.sNo = b.sNo where a.store_id = '$store_id' and a.otype = 'FX' and a.status = 5 and a.commission_type = 0 and a.arrive_time <= '$time' and b.r_type not in(0,1,3) and b.re_type !=3";
        $res_o = Db::query($sql_o);
        if($res_o)
        {
            foreach ($res_o as $key => $value) {
                $sNo = $value['sNo'];
                //获取该订单已发放的佣金
                $res_r = DistributionRecordModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'type'=>1,'status'=>1])->select()->toArray();
                if($res_r)
                {   
                    Db::startTrans();
                    foreach ($res_r as $k => $v) 
                    {
                        $money = $v['money'];
                        $user_id = $v['user_id'];

                        $sql0 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->find();
                        $sql0->tx_commission = Db::raw('tx_commission + '.$money);
                        $res0 = $sql0->save();
                        if(!$res0)
                        {
                            Db::rollback();
                            $lktlog->log("common/distribution.log",__METHOD__ . ":" . __LINE__ . "发放可提现佣金失败！user_id:" . $user_id.'-'.$money);
                        } 
                    }
                    //更新订单状态
                    $sql1 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->find();
                    $sql1->commission_type = 1;
                    $res1 = $sql1->save();
                    if(!$res1)
                    {
                        Db::rollback();
                        $lktlog->log("common/distribution.log",__METHOD__ . ":" . __LINE__ . "变更订单佣金结算状态失败！sNo:" . $sNo);
                    }
                    Db::commit();
                }
            }
        }
    }

    // 获取插件状态
    public function Get_plugin_status($store_id)
    {
        $is_status = 0;
        $is_status = Db::name('distribution_config')->where(['store_id'=>$store_id])->value('status');
      
        return $is_status;
    }

    // 获取分销等级数据
    public static function get_distribution_level($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $user_id = $array['user_id']; // 用户ID

        $level = 0;
        $direct_m_type = 1; // 直推分销比例发放模式 0.百分比 1.固定金额
        $direct_m = 0; // 直推分销奖
        $discount = 100; // 折扣
        if($user_id != '')
        {
            $sql0 = "select a.level,b.sets,b.discount from lkt_user_distribution as a left join lkt_distribution_grade as b on a.level = b.id where a.store_id = '$store_id' and a.user_id = '$user_id' and a.level > 0";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $level = $r0[0]['level'];
                $sets = json_decode($r0[0]['sets'],true);
                $direct_m_type = $sets['direct_m_type']; // 直推分销比例发放模式 0.百分比 1.固定金额
                $direct_m = $sets['direct_m']; // 直推分销奖
                if($sets['zhekou'] == 1)
                {
                    $discount = $r0[0]['discount'];
                }
            }
            else
            {
                $r0 = DistributionGradeModel::where('store_id',$store_id)->field('id,sets')->order('id','desc')->limit(0,1)->select()->toArray();
                if($r0)
                {
                    $level = $r0[0]['id'];
                    $sets = json_decode($r0[0]['sets'],true);
                    $direct_m_type = $sets['direct_m_type'];
                    $direct_m = $sets['direct_m'];
                }
            }
        }
        else
        {
            $r0 = DistributionGradeModel::where('store_id',$store_id)->field('id,sets')->order('id','desc')->limit(0,1)->select()->toArray();
            if($r0)
            {
                $level = $r0[0]['id'];
                $sets = json_decode($r0[0]['sets'],true);
                $direct_m_type = $sets['direct_m_type'];
                $direct_m = $sets['direct_m'];
            }
        }

        $data = array('level'=>$level,'discount'=>$discount,'direct_m_type'=>$direct_m_type,'direct_m'=>$direct_m);
        return $data;
    }

    // 获取分销
    public static function get_distribution($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $user_id = $array['user_id']; // 用户ID
        $price = $array['price']; // 售价
        $distribution_rule = $array['distribution_rule']; // 分佣规则设置 1.等级 2.自定义
        $rules_set = $array['rules_set']; // 自定义数据
        $level = $array['level']; // 分销等级
        $discount = $array['discount']; // 折扣
        $direct_m_type = $array['direct_m_type']; // 直推分销比例发放模式 0.百分比 1.固定金额
        $direct_m = $array['direct_m']; // 直推分销奖

        $fx_price = 0; // 分享赚
        if($distribution_rule == 1)
        { // 分销等级规则
            if($direct_m_type == 0)
            { // 百分比
                $fx_price = round($price * $direct_m * 0.01,2);
            }
            else
            { // 固定金额
                $fx_price = floatval($direct_m);
            }
        }
        else
        {
            $rules_set = json_decode($rules_set,true);
            foreach($rules_set as $k_1 => $v_1)
            {
                if($v_1['id'] == $level)
                {
                    $direct_m = $v_1['direct_m'];
                    if($v_1['direct_mode_type'] == 0)
                    { // 百分比
                        $fx_price = round($price * $direct_m * 0.01,2);
                    }
                    else
                    { // 固定值
                        $fx_price = $direct_m;
                    }
                    $discount = $v_1['diy_discount'];
                }
            }
        }    

        $data = array('discount'=>$discount,'fx_price'=>$fx_price);
        return $data;
    }
    
    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/NormalOrder.log",$Log_content);
        return;
    }
}
