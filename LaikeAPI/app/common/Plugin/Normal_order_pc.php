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
use app\common\DeliveryHelper;
use app\common\ReceiveGoodsUtils;
use app\common\Plugin\MchPublicMethod;
use app\common\Plugin\CouponPublicMethod;

use app\admin\model\AdminModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\CartModel;
use app\admin\model\BuyAgainModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProductListModel;
use app\admin\model\StockModel;
use app\admin\model\UserFirstModel;
use app\admin\model\MemberConfigModel;
use app\admin\model\SupplierOrderFrightModel;

class Normal_order_pc 
{
    public function settlement(&...$context)
    {   
        $action = $context[0];
        //1.列出基础数据
        $user = $action->user;
        $user_id = $user['user_id'];
        $store_id = $action->store_id;
        $store_type = $action->store_type;

        $pay_type = addslashes(trim(Request::post('pay_type'))); // 支付方式
        $product1 = addslashes(Request::post('product'));//  商品数组--------['pid'=>66,'cid'=>88]
        $cart_id = addslashes(trim(Request::post('cart_id')));  //购物车id-- 12,13,123,
        $address_id = Request::post('address_id'); //  地址id
        $vipSource = Request::post('vipSource')?Request::post('vipSource'):0;
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
        $payment = Tools::getPayment($store_id,2);
        $defaultpayment = Tools::getPayment($store_id,1);

        // 用户基本信息
        list($user_money, $enterless, $password_status) = Tools::userInfo($user, $store_id, $user_id,$lktlog);

        //2.区分购物车结算和立即购买---列出选购商品
        $products = Tools::products_list($store_id,$cart_id, $product, $product_type,$buy_type);

        $products_total = 0;
        //3.列出商品数组-计算总价和优惠，运费
        $products_data = Tools::get_products_data($store_id,$products, $products_total, $product_type);
        $product_id = $products_data['product_id'];
        $product_class = $products_data['product_class'];
        $products_freight = $products_data['products_freight'];
        $products = $products_data['products'];
        $products_total = $products_data['products_total'];
        $is_distribution = $products[0]['list'][0]['is_distribution'];
        $is_supplier_pro = $products_data['is_supplier_pro']; // true:不是供应商商品  false:供应商商品
        $no_delivery_str = '';
        if($address_id == '')
        { // 获取不配送省的名称
            $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);
        }

        //查询默认地址order_details
        $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);
        $addemt = $address ? 1 : 0; // 收货地址状态
        if(!$address)
        {
            $address = array(
                'address' => '',
                'address_xq' => '',
                'city' => '',
                'quyu' => '',
                'sheng' => '',
                'name' => '',
                'tel' => '',
            );
        }

        $address_list = Tools::find_address_list($store_id, $user_id,$no_delivery_str);

        $products_data0 = Tools::get_products_data0($store_id, $products,$products_total, $user_id,$vipSource);
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
        // $coupon_id_list = array(); // 优惠ID数组
        $coupon_id0 = '';
        // $coupon_id_0 = '';
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
        $is_subtraction = 0; // 是否有满减
        $reduce_money = 0; // 满减金额
        $reduce_name = ''; // 满减名称
        $coupon_money = 0; // 优惠券金额
        $coupon_name = ''; // 优惠券名称
        $coupon_status = false;
        $mch_preferential_amount = 0;
        if(!$is_supplier_pro)
        {
            $canshu = 'true';
        }
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
            if($zifuchuan == '0' || $zifuchuan == '' || $zifuchuan == 'no_discount')
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
            $discount_list[] = array('coupon_id' => '0', 'coupon_name' => $message_0,'coupon_status'=>true,'discount_type'=>'no_discount','money'=>0);
        }

        $discount = 1;
        $coupon_num1 = 0; // 平台优惠类型的数量
        if(count($discount_list) > 0)
        {
            $coupon_num1 = (count($discount_list)-1);
        }
        $preferential_amount = 0;
        $grade_rate_amount = 0;

        if($canshu != 'false')
        {
            if($zifuchuan == 'coupon')
            {
                foreach ($coupon_list as $ke => $va)
                {
                    if($va['coupon_status'] == 1)
                    {
                        $preferential_amount = $va['money'];  // 平台优惠金额
                        if ($va['activity_type'] == 1)
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
                        else
                        {
                            $yunfei = 0;
                            foreach ($products as $k1 => $v1)
                            {
                                $yunfei = $yunfei + $v1['freight_price'];
                            }
                        }
                    }
                }
            }
            else
            {
                $yunfei = 0;
                foreach ($products as $k1 => $v1)
                {
                    $yunfei = $yunfei + $v1['freight_price'];
                }
            }
        }
        else
        {
            foreach ($coupon_list as $ke => $va)
            {
                if($va['coupon_status'] == 1)
                {
                    $preferential_amount = $va['money'];  // 平台优惠金额
                    if ($va['activity_type'] == 1)
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
                    else
                    {
                        $yunfei = 0;
                        foreach ($products as $k1 => $v1)
                        {
                            $yunfei = $yunfei + $v1['freight_price'];
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
        
        $grade_rate_amount = round($grade_rate_amount, 2);
        $total = round($total, 2);
        $total = $total - $grade_rate_amount;
        if(($total == '0.00' && $pay_type != 'wallet_pay') || $total == 0 && $pay_type != 'wallet_pay')
        {
            $total = '0.01';
        }
        $total_discount = round($mch_preferential_amount,2) + round($preferential_amount,2) + round($grade_rate_amount,2);

        //5.返回数据
        ob_clean();
        if($canshu == 'true' )
        {
            $coupon_id = $coupon_id_0 . ',0' ;
        }
        $data = array('addemt' => $addemt,'address' => $address,'address_list'=>$address_list,'coupon_id' => $coupon_id,'coupon_list'=>$discount_list,'coupon_money' => $coupon_money,'coupon_name' => $coupon_name,'coupon_num'=>$coupon_num1,'coupon_status'=>$coupon_status,'discount' => $discount,'enterless' => $enterless,'freight' => $yunfei,'grade_rate' => $grade_rate,'grade_rate_amount'=>$grade_rate_amount,'is_distribution' => $is_distribution,'is_subtraction' => $is_subtraction,'mch_preferential_amount'=>$mch_preferential_amount,'password_status' => $password_status,'payment' => $payment,'preferential_amount'=>$preferential_amount,'products' => $products,'products_total' => $products_total,'reduce_money' => $reduce_money,'reduce_name' => $reduce_name,'shop_list' => $shop_list,'shop_status' => $shop_status,'status' => 1,'total' => $total,'total_discount'=>$total_discount,'user_money' => $user_money,'is_supplier_pro'=>$is_supplier_pro,'defaultpayment'=>$defaultpayment);
        $message = Lang("Success");
        echo json_encode(array('code' => 200,'data'=>$data,'message'=>$message));
        exit;
    }

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
        $type = trim(Request::post('type')) ? Request::post('type') : 'GM'; // 订单类型
        $address_id = Request::post('address_id'); //  地址id
        $coupon_id = trim(Request::post('coupon_id')); // 优惠券id
        $pay_type = addslashes(trim(Request::post('pay_type'))); // 支付方式
        $vipSource = Request::post('vipSource')?Request::post('vipSource'):0;
        $buy_type = addslashes(Request::post('buy_type')) ? addslashes(Request::post('buy_type')) : 0; // 提交状态 1是再次购买 空是正常提交
        $shop_address_id = Request::post('shop_address_id',0)?Request::post('shop_address_id',0):0; //  门店地址id
        $remarks = trim(Request::post('remarks')); //  订单备注
        $fullName = trim(Request::post('fullName')); //  收货人
        $fullcpc = trim(Request::post('fullcpc')); //  收货人电话区号
        $fullPhone = trim(Request::post('fullPhone')); //  收货人电话
        $currency_code_0 = trim(Request::param('currency_code')); // ISO货币代码(如USD)
        $currency_symbol_0 = trim(Request::param('currency_symbol')); // 货币符号($)
        $exchange_rate_0 = trim(Request::param('exchange_rate')); // 汇率

        $remarks = htmlspecialchars_decode($remarks); // 将特殊的 HTML 实体转换回普通字符
        $remarks = json_decode($remarks, true);

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>$currency_id));
        $currency_symbol = $userCurrency[0]['currency_symbol'];
        $exchange_rate = $userCurrency[0]['exchange_rate'];
        $currency_code = $userCurrency[0]['currency_code'];

        if($currency_symbol_0 != $currency_symbol || $currency_code_0 != $currency_code || $exchange_rate_0 != $exchange_rate)
        {
            $message = Lang('nomal_order.8');
            echo json_encode(array('code' => ERROR_CODE_hbhlygb, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }

        // $product1 = "";
        // $cart_id = "5410,5411,5417,5420";

        $products_total = 0;
        $total = 0.00;
        $yunfei = 0;

        $product = '';
        list($product, $shop_list, $cart_id) = Tools::productHandle($product1, $cart_id);

        //2.区分购物车结算和立即购买---列出选购商品
        $products = Tools::products_list($store_id, $cart_id, $product, $type,$buy_type);
        if(empty($address_id) && empty($shop_address_id))
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
        $cpc = $fullcpc?$fullcpc:$address['cpc'];
        $shop_cpc = $cpc;
        $mobile = $fullPhone?$fullPhone:$address['tel'];
        $name = $fullName?$fullName:$address['name'];
        $sheng = isset($address['sheng'])?$address['sheng']:'';
        $shi = isset($address['city'])?$address['city']:'';
        $xian = isset($address['quyu'])?$address['quyu']:'';
        $address_xq = isset($address['address'])?$address['address']:'';
        $code = isset($address['code'])?$address['code']:'';

        $products_data0 = Tools::get_products_data0($store_id, $products,$products_total, $user_id,$vipSource);
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
        $discount = 1;
        // 店铺
        $otype = 'GM';
        $order_status = 0;
        $shop_status = 0;
        $extraction_code = '';
        $extraction_code_img = '';

        $give_id = 0; // 赠品ID
        $subtraction_id = 0; // 满减ID
        $reduce_money = 0; // 满减优惠金额
        $reduce_name_array = ''; // 满减名称
        $coupon_money = 0; // 优惠券优惠金额
        $mch_preferential_amount = 0;

        $mch = new MchPublicMethod();
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
        foreach ($platform_discount_id_list as $k => $v)
        {
            if($k != $platform_discount_num)
            {
                $coupon_id_list[] = $v;
            }
            $coupon_id0 = implode(',',$coupon_id_list); // 优惠券使用
        }

        if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
        {
            $coupon = new CouponPublicMethod();
            $mch_coupon_list = $coupon->settlement_store_coupons($store_id, $user_id, $products,$coupon_id0,'');
            $products = $mch_coupon_list['products'];
            $mch_preferential_amount = $mch_coupon_list['preferential_amount']; // 店铺优惠之和
            $coupon_money = $mch_preferential_amount;
        }
        $preferential_amount = 0;
    
        if($zifuchuan != '0')
        {
            // 优惠券--插件
            if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
            {
                $coupon = new CouponPublicMethod();
                $coupon_list_0 = $coupon->settlement_platform_coupons($store_id, $user_id, $products,$coupon_id0);
                $products = $coupon_list_0['products'];
                $coupon_list = $coupon_list_0['list'];
                foreach ($coupon_list as $k => $v)
                {
                    if($v['coupon_status'] == 1)
                    {
                        $preferential_amount = $v['money'];  // 平台优惠金额
                        $coupon_money = $coupon_money + $v['money'];
                        if ($v['activity_type'] == 1)
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
                        else
                        {
                            $yunfei = 0;
                            foreach ($products as $k1 => $v1)
                            {
                                $yunfei = $yunfei + $v1['freight_price'];
                            }
                        }
                    }
                }
            }
        }
        else
        {
            $yunfei = 0;
            foreach ($products as $k1 => $v1)
            {
                $yunfei = $yunfei + $v1['freight_price'];
            }
        }
        
        $total = $products_total - $mch_preferential_amount - $preferential_amount + $yunfei; // 商品总价-店铺优惠之和-平台优惠+总运费
        $grade_rate_amount = 0;//会员折扣优惠金额
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
                $gongyingshang = $value['gongyingshang'];
                $z_num += $value['num'];
                $product_title = addslashes($value['product_title']);
                $freight_price = floor(isset($value['freight_price']) ? $value['freight_price'] : 0);
                // 循环插入订单附表 ，添加不同的订单详情
                $freight_price = $shop_address_id ? 0 : $freight_price;
                
                $supplier_freight_price = floor(isset($value['supplier_freight_price']) ? $value['supplier_freight_price'] : 0);
                // 循环插入订单附表 ，添加不同的订单详情
                $supplier_freight_price = $shop_address_id ? 0 : $supplier_freight_price;

                $sql_insert = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pid,'p_name'=>$product_title,'p_price'=>$value['price'],'num'=>$num,'unit'=>$value['unit'],'r_sNo'=>$sNo,'add_time'=>date("Y-m-d H:i:s"),'r_status'=>$order_status,'size'=>$value['size'],'sid'=>$cid,'freight'=>$freight_price,'coupon_id'=>$value['coupon_id'],'after_discount'=>$value['amount_after_discount'],'supplier_settlement'=>$value['supplier_settlement'],'supplier_id'=>$value['gongyingshang']);
                $beres = Db::name('order_details')->insertGetId($sql_insert);
                if ($beres < 1)
                { // 添加失败
                    $this->log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数：" . json_encode($sql_insert));
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }

                if($value['supplier_settlement'] > 0 && $supplier_freight_price > 0)
                {
                    $sql_sf = new SupplierOrderFrightModel();
                    $sql_sf->store_id = $store_id;
                    $sql_sf->sNo = $sNo;
                    $sql_sf->total_fright = 0;
                    $sql_sf->supplier_id = $value['gongyingshang'];
                    $sql_sf->add_date = date("Y-m-d H:i:s");
                    $sql_sf->detail_id = $beres;
                    $sql_sf->freight = $supplier_freight_price;
                    $sql_sf->save();
                    $res_sf = $sql_sf->id;
                    if($res_sf < 1)
                    {
                        $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单供应商运费数据失败！参数detail_id：" . $beres);
                        Db::rollback();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }
                }
               
                if ($cart_id != '' && $buy_type != 1)
                {
                    // 删除对应购物车内容
                    $res_del = CartModel::where(['store_id'=>$store_id,'Goods_id'=>$pid,'user_id'=>$user_id,'Size_id'=>$cid])->delete();
                    if ($res_del < 1)
                    {
                        Db::rollback();
                        ob_clean();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }
                }
                elseif($cart_id != '' && $buy_type == 1)
                {   
                    $res_del = BuyAgainModel::where(['store_id'=>$store_id,'Goods_id'=>$pid,'user_id'=>$user_id,'Size_id'=>$cid])->delete();
                    // 删除对应再次购买购物车内容
                    if ($res_del < 1)
                    {
                        Db::rollback();
                        ob_clean();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }
                }

                $array = array('store_id'=>$store_id,'supplier_id'=>$value['gongyingshang'],'pid'=>$pid,'sid'=>$cid,'num'=>$num,'type'=>1,'user_id'=>$user_id);
                $res_stock = PC_Tools::Modify_inventory($array);
                if($res_stock == 1)
                {
                    Db::rollback();
                    $message = Lang('operation failed');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
            }
            $grade_rate_amount += $v['grade_rate_amount'];
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
        
        $total = $total - $grade_rate_amount;
        if(($total == '0.00' && $pay_type != 'wallet_pay') || $total == 0 && $pay_type != 'wallet_pay')
        {
            $total = '0.01';
        }
        $grade_score = 0;
        //判断插件是否开启
        if(file_exists('../app/common/Plugin/Members.php'))
        {   
            //判断用户是否为会员
            $grade = $user['grade'];
            $is_out = $user['is_out'];
            $birthday = $user['birthday'];
            $r_g = MemberConfigModel::where(['store_id'=>$store_id,'is_open'=>1])->field('birthday_open,points_multiple')->select()->toArray();
            if($r_g)
            {
                if($r_g[0]['birthday_open'] == 1)
                {   
                    $time = date("m-d");
                    $riqi = date("m-d", strtotime($birthday));
                    if ($riqi == $time)
                    {
                        $grade_score = floor($total * $r_g[0]['points_multiple']);
                    }
                }
            }
        }

        $order_failure_time = Tools::Obtain_expiration_time(array('store_id'=>$store_id,'otype'=>$otype));

        $sql_order_insert = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'num'=>$z_num,'z_price'=>$total,'sNo'=>$sNo,'shop_cpc'=>$shop_cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_xq,'code'=>$code,'remark'=>'','pay'=>$pay_type,'add_time'=>date("Y-m-d H:i:s"),'status'=>$order_status,'coupon_id'=>$coupon_id,'subtraction_id'=>$subtraction_id,'consumer_money'=>0,'coupon_activity_name'=>$reduce_name_array,'spz_price'=>$spz_price,'reduce_price'=>$reduce_money,'coupon_price'=>$coupon_money,'source'=>$store_type,'otype'=>$otype,'mch_id'=>$mch_id,'p_sNo'=>'','bargain_id'=>0,'comm_discount'=>$discount,'remarks'=>$remarks,'real_sno'=>$real_sno,'self_lifting'=>$shop_status,'extraction_code'=>$extraction_code,'extraction_code_img'=>$extraction_code_img,'grade_rate'=>$grade_rate,'grade_fan'=>$grade_rate_amount,'z_freight'=>$yunfei,'preferential_amount'=>$preferential_amount,'single_store'=>$shop_address_id,'grade_score'=>$grade_score,'order_failure_time'=>$order_failure_time,'currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code);
        $r_o = Db::name('order')->insertGetId($sql_order_insert);
        if ($r_o > 0)
        {
            if ($give_id != 0)
            { // 满赠商品
                $r_o2 = ConfigureModel::where('id',$give_id)->field('pid,num,min_inventory')->select()->toArray();
                $pid_2 = $r_o2[0]['pid'];
                $num_2 = $r_o2[0]['num'];
                $min_inventory_2 = $r_o2[0]['min_inventory'];

                // 根据商品ID，修改商品库存
                $r_o0 = Db::name('product_list')->where('id',$pid_2)->update(['num' =>  Db::raw('num - 1')]);
                // 库存-1
                if ($r_o0 <= 0)
                {
                    $this->log(__METHOD__ . ":" . __LINE__ . "生成订单时,修改赠品商品库存信息失败！pid:" . $pid_2);
                    
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }
                
                // 根据属性ID，修改属性库存
                $r_o1 = Db::name('configure')->where('id',$give_id)->update(['num' =>  Db::raw('num - 1')]);
                if ($r_o1 <= 0)
                {
                    $this->log(__METHOD__ . ":" . __LINE__ . "生成订单时,修改赠品商品库存信息失败！sql:" . $sql_o1);
                    
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }

                $content = $user_id . '生成订单赠送商品';
                $sql_stock_insert = array('store_id'=>$store_id,'product_id'=>$pid_2,'attribute_id'=>$give_id,'total_num'=>$num_2,'flowing_num'=>1,'type'=>1,'user_id'=>$user_id,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content);
                Db::name('stock')->insert($sql_stock_insert);

                if($num_2 - 1 <= $min_inventory_2)
                {
                    $content1 = '预警';
                    // 在库存记录表里，添加一条预警信息
                    $sql_stock_insert1 = array('store_id'=>$store_id,'product_id'=>$pid_2,'attribute_id'=>$give_id,'total_num'=>$num_2,'flowing_num'=>$min_inventory_2,'type'=>2,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content1);
                    Db::name('stock')->insert($sql_stock_insert1);

                    $message_9 = "商品ID为" . $pid_2 . "的商品库存不足，请尽快补充库存";
                    $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>9,'parameter'=>$give_id,'content'=>$message_9);
                    PC_Tools::add_message_logging($message_logging_list9);
                }
            }

            if ($coupon_id0)
            {
                if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
                {
                    $coupon = new CouponPublicMethod();
                    $r_coupon0 = $coupon->update_coupon($store_id, $user_id, $coupon_id0,2);
                    if ($r_coupon0 == 2)
                    {
                        //回滚删除已经创建的订单
                        Db::rollback();
                        ob_clean();
                        $message = Lang('nomal_order.4');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }
                    $r_coupon1 = $coupon->coupon_sno($store_id, $user_id, $coupon_id0,$sNo,'add');
                    if ($r_coupon1 == 2)
                    {
                        //回滚删除已经创建的订单
                        Db::rollback();
                        ob_clean();
                        $message = Lang('nomal_order.5');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }
                    $coupon_Log_content = '会员' . $user_id . ',订单号' . $sNo . '使用优惠券ID为' . $coupon_id0;
                    $coupon->couponLog($coupon_id0, $coupon_Log_content);
                    
                }
            }

            Db::commit();

            $mch_id_str = trim($mch_id,',');
            $mch_id_list = explode(',',$mch_id_str);
            foreach($mch_id_list as $k_mch_id_list => $v_mch_id_list)
            {
                $message_5 = "您来新订单了，订单为".$sNo."，请及时处理！";
                $message_logging_list5 = array('store_id'=>$store_id,'mch_id'=>$v_mch_id_list,'gongyingshang'=>0,'type'=>5,'parameter'=>$sNo,'content'=>$message_5);
                PC_Tools::add_message_logging($message_logging_list5);
            }

            $arr = array('sNo' => $sNo, 'total' => $total, 'order_id' => $r_o,'order_type'=>'GM','orderTime'=>date("Y-m-d H:i:s"));
            $message = Lang("Success");
            echo json_encode(array('code' => 200, 'data' => $arr,'message'=>$message));
            exit;
        }
        else
        {
            $this->log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数：" . json_encode($sql_order_insert));
            if ($coupon_id0)
            {
                if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
                {
                    $coupon = new CouponPublicMethod();
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

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("mall/NormalOrderPc.log",$Log_content);
        return;
    }

}
