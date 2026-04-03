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
use app\common\LKTConfigInfo;


use app\admin\model\AdminModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\CartModel;
use app\admin\model\BuyAgainModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProductListModel;
use app\admin\model\StockModel;
use app\admin\model\UserFirstModel;
use app\admin\model\UserAddressModel;
use app\admin\model\MemberConfigModel;
use app\admin\model\SupplierOrderFrightModel;

class Virtual_order 
{

    public function uninstall(...$context)
    {

    }

    public function install(...$context)
    {

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
        $scoreDeduction = trim(Request::post('scoreDeduction')) ? trim(Request::post('scoreDeduction')) : 0; // 抵扣积分
        
        if($vipSource == 1)
        {
            $canshu = 'true';
        }
        // $product1 = "";
        // $cart_id = "8,9,10";
        $lktlog = new LaiKeLogUtils();
        // 处理商品
        list($product, $shop_list, $cart_id) = Tools::productHandle($product1, $cart_id);

        // 支付状态
        $payment = Tools::getPayment($store_id,2);
        $defaultpayment = Tools::getPayment($store_id,1);

        // 用户基本信息
        list($user_money, $enterless, $password_status) = Tools::userInfo($user, $store_id, $user_id,$lktlog);

        $deduction_list = PC_Tools::determine_whether_there_is_a_deduction_of_points($store_id, $user_id,$scoreDeduction,'');
        $scoreDeductionValue = $deduction_list['scoreDeductionValue']; // 是否有积分抵扣
        $score = $deduction_list['user_score']; // 用户积分
        $scoreDeductionPrice_all = $deduction_list['deductible_amount_all']; // 最大抵扣金额
        $scoreDeductionPrice = $deduction_list['deductible_amount']; // 本次抵扣金额
        $score_ratio = $deduction_list['score_ratio']; // 抵扣比例
        $scoreDeductionValue = false;

        //2.区分购物车结算和立即购买---列出选购商品
        $products = Tools::products_list($store_id,$cart_id, $product, $product_type,$buy_type);

        $products_total = 0;
        //3.列出商品数组-计算总价和优惠，运费
        $products_data = Tools::get_products_data($store_id,$products, $products_total, $product_type,$shop_address_id);
        $product_id = $products_data['product_id'];
        $product_class = $products_data['product_class'];
        $products_freight = $products_data['products_freight'];
        $products = $products_data['products'];
        $products_total = $products_data['products_total'];
        $is_distribution = $products[0]['list'][0]['is_distribution'];
        $is_supplier_pro = $products_data['is_supplier_pro']; // true:不是供应商商品  false:供应商商品
        $address_status = $products_data['address_status']; // 预约时间设置 1.无需预约下单 2.需要预约下单
        $mchStoreList = $products_data['mchStoreList']; // 门店数据

        // $no_delivery_str = '';
        // if($address_id == '')
        // { // 获取不配送省的名称
        //     $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);
        // }

        // //查询默认地址order_details
        // $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);
        // $addemt = $address ? 1 : 0; // 收货地址状态
        // if(!$address)
        // {
            $address = array(
                'address' => '',
                'address_xq' => '',
                'city' => '',
                'quyu' => '',
                'sheng' => '',
                'name' => '',
                'tel' => '',
            );
        // }

        //计算会员折扣价格
        $products_data0 = Tools::get_products_data0($store_id, $products,$products_total, $user_id,$vipSource);
        $grade_rate = $products_data0['grade_rate'];
        $products = $products_data0['products'];
        $products_total = $products_data0['products_total'];

        //4.计算运费
        $freight = Tools::get_freight($products_freight, $products, $address, $store_id, $product_type);
        $products = $freight['products'];

        $yunfei = 0;
        // if(isset($shop_address_id) && $shop_address_id != '')
        // {
        //     foreach ($products as $k => $v)
        //     {
        //         $products[$k]['product_total'] = $v['product_total'] - $v['freight_price'];
        //         $products[$k]['freight_price'] = 0;
        //     }
        // }
        // else
        // {
        //     $yunfei = $freight['yunfei'];
        // }
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
        $preferential_amount = 0;   //优惠金额
        $grade_rate_amount = 0;     //会员折扣金额

        if($canshu != 'false')
        {
            if($zifuchuan == 'coupon')
            {
                foreach ($coupon_list as $ke => $va)
                {
                    if($va['coupon_status'] == 1)
                    {
                        $preferential_amount = $va['money'];  // 平台优惠金额
                        //优惠券类型1：免邮
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

        $Calculate_the_deduction_data_for_the_detailed_invoice = array('products' => $products,'products_total' => $products_total,'scoreDeductionPrice'=>$scoreDeductionPrice,'scoreDeductionpoints'=>$scoreDeduction);
        $products = PC_Tools::Calculate_the_deduction_data_for_the_detailed_invoice($Calculate_the_deduction_data_for_the_detailed_invoice);

        $total = $products_total - $mch_preferential_amount - $preferential_amount + $yunfei; // 商品总价-店铺优惠之和-平台优惠+总运费
        if(($total == '0.00' && $pay_type == '') || $total == 0 && $pay_type == '')
        {
            $total = '0.01';
        }
        $grade_rate_amount = number_format($grade_rate_amount, 2);

        $total1 = $total;
        $total = $total - $scoreDeductionPrice; // 支付金额 = 支付金额 - 抵扣金额
        $total = round($total, 2);

        $total_discount = round($mch_preferential_amount,2) + round($preferential_amount,2) + round($grade_rate_amount,2);  //优惠金额

        //5.返回数据
        ob_clean();
        if($canshu == 'true' )
        {
            $coupon_id = $coupon_id_0 . ',0' ;
        }
        if($mchStoreList == array())
        {
            $data = array('address_status'=>$address_status,'coupon_id' => $coupon_id,'coupon_list'=>$discount_list,'coupon_money' => $coupon_money,'coupon_name' => $coupon_name,'coupon_num'=>$coupon_num1,'coupon_status'=>$coupon_status,'discount' => $discount,'enterless' => $enterless,'freight' => $yunfei,'grade_rate' => $grade_rate,'grade_rate_amount'=>$grade_rate_amount,'is_distribution' => $is_distribution,'is_subtraction' => $is_subtraction,'is_supplier_pro'=>$is_supplier_pro,'mch_preferential_amount'=>$mch_preferential_amount,'password_status' => $password_status,'payment' => $payment,'preferential_amount'=>$preferential_amount,'products' => $products,'products_total' => round($products_total,2),'reduce_money' => $reduce_money,'reduce_name' => $reduce_name,'shop_list' => $shop_list,'shop_status' => $shop_status,'status' => 1,'total' => $total,'total_discount'=>$total_discount,'user_money' => $user_money,'scoreDeductionValue'=>$scoreDeductionValue,'score'=>$score,'scoreRatio'=>$score_ratio,'scoreDeduction'=>$scoreDeduction,'scoreDeductionPrice'=>$scoreDeductionPrice,'scoreDeductionPrice_all'=>$scoreDeductionPrice_all,'total1'=>$total1,'defaultpayment'=>$defaultpayment);
        }
        else
        {
            $data = array('address_status'=>$address_status,'coupon_id' => $coupon_id,'coupon_list'=>$discount_list,'coupon_money' => $coupon_money,'coupon_name' => $coupon_name,'coupon_num'=>$coupon_num1,'coupon_status'=>$coupon_status,'discount' => $discount,'enterless' => $enterless,'freight' => $yunfei,'grade_rate' => $grade_rate,'grade_rate_amount'=>$grade_rate_amount,'is_distribution' => $is_distribution,'is_subtraction' => $is_subtraction,'is_supplier_pro'=>$is_supplier_pro,'mch_preferential_amount'=>$mch_preferential_amount,'password_status' => $password_status,'payment' => $payment,'preferential_amount'=>$preferential_amount,'products' => $products,'products_total' => round($products_total,2),'reduce_money' => $reduce_money,'reduce_name' => $reduce_name,'shop_list' => $shop_list,'shop_status' => $shop_status,'status' => 1,'total' => $total,'total_discount'=>$total_discount,'user_money' => $user_money,'mchStoreList'=>$mchStoreList,'scoreDeductionValue'=>$scoreDeductionValue,'score'=>$score,'scoreRatio'=>$score_ratio,'scoreDeduction'=>$scoreDeduction,'scoreDeductionPrice'=>$scoreDeductionPrice,'scoreDeductionPrice_all'=>$scoreDeductionPrice_all,'total1'=>$total1,'defaultpayment'=>$defaultpayment);
        }
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
        $type = trim(Request::post('type')) ? Request::post('type') : 'VI'; // 订单类型
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
        $write_time_id = trim(Request::post('mchStoreWrite')); // 门店核销时间段ID
        $store_write_time = trim(Request::post('mchStoreWriteTime')); //  时间段
        $scoreDeduction = trim(Request::post('scoreDeduction')) ? trim(Request::post('scoreDeduction')) : 0; // 抵扣积分
        $scoreRatio = trim(Request::post('scoreRatio')) ? trim(Request::post('scoreRatio')) : ''; // 抵扣比例
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
        // $cart_id = "8,9,10";

        $products_total = 0;
        $total = 0.00;
        $yunfei = 0;

        $product = '';
        list($product, $shop_list, $cart_id) = Tools::productHandle($product1, $cart_id);

        $deduction_list = PC_Tools::determine_whether_there_is_a_deduction_of_points($store_id, $user_id,$scoreDeduction,$scoreRatio);
        $score = $deduction_list['user_score']; // 用户积分
        $scoreDeductionPrice = $deduction_list['deductible_amount']; // 本次抵扣金额

        //2.区分购物车结算和立即购买---列出选购商品
        $products = Tools::products_list($store_id, $cart_id, $product, $type,$buy_type);

        //3.列出商品数组-计算总价和优惠，运费
        $products_data = Tools::get_products_data($store_id,$products, $products_total, $type,$shop_address_id);
        $product_id = $products_data['product_id'];
        $product_class = $products_data['product_class'];
        $products_freight = $products_data['products_freight'];
        $products = $products_data['products'];
        $products_total = $products_data['products_total'];
        $spz_price = $products_data['products_total'];
        $is_supplier_pro = $products_data['is_supplier_pro'];
        $address_status = $products_data['address_status']; // 1.不需要选择核销地址 2.需要选择核销地址
        $mchStoreList = $products_data['mchStoreList']; // 门店数据
        $total_p = $products_total;

        $products_data0 = Tools::get_products_data0($store_id, $products,$products_total, $user_id,$vipSource);
        $grade_rate = $products_data0['grade_rate'];
        $products = $products_data0['products'];
        $products_total = $products_data0['products_total'];

        $address = array(
            'address' => '',
            'address_xq' => '',
            'city' => '',
            'quyu' => '',
            'sheng' => '',
            'name' => '',
            'tel' => '',
        );
        // 6.计算运费
        $freight = Tools::get_freight($products_freight, $products, $address, $store_id, $type);
        $products = $freight['products'];

        // 定义初始化数据
        $z_num = 0;
        $discount = 1;
        // 店铺
        $otype = 'VI';
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

        $name = '';
        $cpc = '86';
        $shop_cpc = '86';
        $mobile = '';
        $sheng = '';
        $shi = '';
        $xian = '';
        $address_xq = '';
        $extraction_code = '';
        $extraction_code_img = '';
        $write_time = "";

        $mch = new mchPublicMethod();        
        if($address_status == 1)
        { // 不需要选择核销地址
            $shop_status = 4; // 自提 0.配送 1.自提 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
            $shop = $mch->Settlement_0();
            $extraction_code = $shop['extraction_code'];
            $extraction_code_img = $shop['extraction_code_img'];

            if(count($products) == 1)
            { // 单店铺
                // if(count($products[0]['list']) == 1 && $products[0]['list'][0]['write_off_settings'] == 1)
                if($products[0]['list'][0]['write_off_settings'] == 1)
                { // 单商品 && 线下核销
                    $shop_status = 3; // 自提 0.配送 1.自提 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
                }
            }
        }
        else
        { // 需要选择核销地址（不是从购物车过来）
            $shop_status = 3; // 自提 0.配送 1.自提 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销

            $shop = $mch->Settlement($store_id, $products, 'payment',$shop_address_id);
            $name = $shop['name'];
            $shop_cpc = $shop['cpc'];
            $mobile = $shop['tel'];
            $sheng = $shop['sheng'];
            $shi = $shop['shi'];
            $xian = $shop['xian'];
            $address_xq = $shop['address'];
            $extraction_code = $shop['extraction_code'];
            $extraction_code_img = $shop['extraction_code_img'];

            if($shop_status == 3)
            {
                $mch_Store_List = $mchStoreList[0]['date'][$store_write_time];
                foreach($mch_Store_List as $k_Store_List => $v_Store_List)
                {
                    if($v_Store_List['w_id'] == $write_time_id)
                    {
                        $write_time = $store_write_time . ' ' . $v_Store_List['sort'] . '-' . $v_Store_List['endTime'];
                    }
                }

                $sql_mch_store_write = "select start_time,end_time,off_num from lkt_mch_store_write where store_id = '$store_id' and mch_store_id = '$shop_address_id' and id = '$write_time_id' ";
                $r_mch_store_write = Db::query($sql_mch_store_write);
                if($r_mch_store_write)
                {
                    $start_time2 = strtotime(date("Y-m-d",strtotime($r_mch_store_write[0]['start_time']))); // 开始预约日期
                    $end_time2 = strtotime($store_write_time); // 预约日期
                    $off_num = $r_mch_store_write[0]['off_num'];
                    $off_num_0 = explode(',',$off_num); // 已预约核销次数
                    $diff_days = (int)($end_time2 - $start_time2) / 86400;
                    
                    $off_num_0[$diff_days] = $off_num_0[$diff_days] + 1;
                    $off_num = implode(',',$off_num_0);

                    $sql_mch_store_write1 = "update lkt_mch_store_write set off_num = '$off_num' where store_id = '$store_id' and mch_store_id = '$shop_address_id' and id = '$write_time_id' ";
                    $r_mch_store_write1 = Db::execute($sql_mch_store_write1);
                    if ($r_mch_store_write1 < 1)
                    { // 添加失败
                        $this->Log(__METHOD__ . ":" . __LINE__ . "修改虚拟商品订单核销表失败！sql：" . $sql_mch_store_write1);
                        Db::rollback();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }
                }
            }
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
            $free_shipping = $mch_coupon_list['free_shipping']; // 免邮金额之和
            $coupon_money = $mch_preferential_amount;
        }
        $preferential_amount = 0;
    
        if($zifuchuan != '0')
        {
            // 优惠券--插件
            if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
            {
                $coupon = new CouponPublicMethod();
                $coupon_list_0 = $coupon->settlement_platform_coupons($store_id, $user_id, $products,$coupon_id0,'');
                $products = $coupon_list_0['products'];
                $coupon_list = $coupon_list_0['list'];
                foreach ($coupon_list as $k => $v)
                {
                    if($v['coupon_status'] == 1)
                    {
                        $preferential_amount = $v['money'];  // 平台优惠金额
                        $coupon_money = $coupon_money + $v['money'];
                        //优惠券类型1：免邮
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

        $Calculate_the_deduction_data_for_the_detailed_invoice = array('products' => $products,'products_total' => $products_total,'scoreDeductionPrice'=>$scoreDeductionPrice,'scoreDeductionpoints'=>$scoreDeduction);
        $products = PC_Tools::Calculate_the_deduction_data_for_the_detailed_invoice($Calculate_the_deduction_data_for_the_detailed_invoice);

        $total = $products_total - $mch_preferential_amount - $preferential_amount + $yunfei; // 商品总价-店铺优惠之和-平台优惠+总运费
        if($total < $scoreDeductionPrice)
        {
            Db::rollback();
            $message = Lang('operation failed');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }
        if(($total == '0.00' && $pay_type == '') || $total == 0 && $pay_type == '')
        {
            $total = '0.01';
        }
        $total = round($total,2);
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
                $write_off_num = $value['write_off_num'];
                $z_num += $value['num'];
                $product_title = addslashes($value['product_title']);
                $freight_price = floor(isset($value['freight_price']) ? $value['freight_price'] : 0);
                // 循环插入订单附表 ，添加不同的订单详情
                $freight_price = $shop_address_id ? 0 : $freight_price;

                $supplier_freight_price = floor(isset($value['supplier_freight_price']) ? $value['supplier_freight_price'] : 0);
                // 循环插入订单附表 ，添加不同的订单详情
                $supplier_freight_price = $shop_address_id ? 0 : $supplier_freight_price;

                $store_coupon_price = $value['discount']; // 该商品使用了多少店铺优惠金额
                $platform_coupon_price = $value['membership_price'] * $num - $value['amount_after_discount'] - $store_coupon_price; // 会员价 * 数量 - 优惠后金额 - 该商品使用店铺优惠金额 = 该商品使用了多少平台优惠金额

                $sql_insert = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pid,'p_name'=>$product_title,'p_price'=>$value['price'],'num'=>$num,'unit'=>$value['unit'],'r_sNo'=>$sNo,'add_time'=>date("Y-m-d H:i:s"),'r_status'=>$order_status,'size'=>$value['size'],'sid'=>$cid,'freight'=>$freight_price,'coupon_id'=>$value['coupon_id'],'after_discount'=>$value['amount_after_discount'],'supplier_settlement'=>$value['supplier_settlement'],'supplier_id'=>$value['gongyingshang'],'write_off_num'=>$write_off_num,'after_write_off_num'=>0,'mch_store_write_id'=>$shop_address_id,'write_time'=>$write_time,'write_time_id'=>$write_time_id,'store_coupon_price'=>$store_coupon_price,'platform_coupon_price'=>$platform_coupon_price,'mch_id'=>$mch_id1,'actual_total'=>$value['scoreDeductionPrice'],'score_deduction'=>$value['scoreDeductionpoints']);
                $beres = Db::name('order_details')->insertGetId($sql_insert);
                if ($beres < 1)
                { // 添加失败
                    $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数：" . json_encode($sql_insert));
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
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

        if($vipSource == 1)
        {
            $total = $total - $grade_rate_amount;
        }
        
        $total_discount = $total_p - $products_total;

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
                 
                if($r_g[0]['birthday_open'] == 1 && $grade == 1 && $is_out == 0 && $vipSource == 1)
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

        $sql_order_insert = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'num'=>$z_num,'z_price'=>$total,'sNo'=>$sNo,'shop_cpc'=>$shop_cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_xq,'remark'=>'','pay'=>$pay_type,'add_time'=>date("Y-m-d H:i:s"),'status'=>$order_status,'coupon_id'=>$coupon_id,'subtraction_id'=>$subtraction_id,'consumer_money'=>0,'coupon_activity_name'=>$reduce_name_array,'spz_price'=>$spz_price,'reduce_price'=>$reduce_money,'coupon_price'=>$coupon_money,'source'=>$store_type,'otype'=>$otype,'mch_id'=>$mch_id,'p_sNo'=>'','bargain_id'=>0,'comm_discount'=>$discount,'remarks'=>$remarks,'real_sno'=>$real_sno,'self_lifting'=>$shop_status,'extraction_code'=>$extraction_code,'extraction_code_img'=>$extraction_code_img,'grade_rate'=>$grade_rate,'grade_fan'=>$grade_rate_amount,'z_freight'=>$yunfei,'preferential_amount'=>$preferential_amount,'single_store'=>$shop_address_id,'pick_up_store'=>$shop_address_id,'store_write_time'=>$store_write_time,'grade_score'=>$grade_score,'order_failure_time'=>$order_failure_time,'score_deduction'=>$scoreDeduction,'currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code);
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
                    $this->Log(__METHOD__ . ":" . __LINE__ . "生成订单时,修改赠品商品库存信息失败！pid:" . $pid_2);
                    
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }
                
                // 根据属性ID，修改属性库存
                $r_o1 = Db::name('configure')->where('id',$give_id)->update(['num' =>  Db::raw('num - 1')]);
                if ($r_o1 <= 0)
                {
                    $this->Log(__METHOD__ . ":" . __LINE__ . "生成订单时,修改赠品商品库存信息失败！sql:" . $sql_o1);
                    
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }

                $content = $user_id . '生成订单赠送商品';
                $sql_stock_insert = array('store_id'=>$store_id,'product_id'=>$pid_2,'attribute_id'=>$give_id,'total_num'=>$num_2,'flowing_num'=>1,'type'=>1,'user_id'=>$user_id,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content);
                Db::name('stock')->insert($sql_stock_insert);

                if($num_2 - 1 < $min_inventory_2)
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

            // $array = array('store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo);
            // $this->leave_Settlement($array);

            $arr = array('sNo' => $sNo, 'total' => $total, 'order_id' => $r_o,'order_type'=>'GM','orderTime'=>date("Y-m-d H:i:s"),'total_discount'=>$total_discount);
            $message = Lang("Success");
            echo json_encode(array('code' => 200, 'data' => $arr,'message'=>$message));
            exit;
        }
        else
        {
            $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数：" . json_encode($sql_order_insert));
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

    // 拆分订单
    public function leave_Settlement($array)
    {
        $store_id = $array['store_id'];
        $user_id = $array['user_id'];
        $sNo = $array['sNo'];

        $lktlog = new LaiKeLogUtils();
        $Toosl = new Tools(1,1);

        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1,'recycle'=>0])->field('shop_id')->select()->toArray();
        $admin_mch_id = $r_admin[0]['shop_id'];

        $type = substr($sNo, 0, 2);//获取订单号前两位字母（类型）
        //事务开启
        Db::startTrans();
        try
        {
            // 查询刚刚生成的订单
            $r0 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id])->select()->toArray();
            $data = (array)$r0[0];
            $mch_id = $r0[0]['mch_id']; // 店铺ID字符串
            $mch_id = substr($mch_id, 1, -1);
            unset($data['id']);

            $grade_rate = $r0[0]['grade_rate']; // 会员折扣
            $remarks = array();
            if($r0[0]['remarks'] != '')
            {
                $remarks = unserialize($r0[0]['remarks']); // 订单状态
            }

            $sql1 = "select * from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                if(count($r1) > 1)
                { // 详单数大于1（需要拆单）
                    $data['p_sNo'] = $sNo;
                    foreach($r1 as $k1 => $v1)
                    {
                        $order_details_id = $v1['id'];
                        $sNo1 = $Toosl->Generate_order_number($type, 'sNo'); // 生成订单号
                        $data['sNo'] = $sNo1;
                        $p_id = $v1['p_id']; // 商品ID
                        $data['num'] = $v1['num']; // 商品数量
                        $data['spz_price'] = $v1['p_price'] * $v1['num']; // 商品总价
                        $data['z_freight'] = $v1['freight']; // 运费
                        $data['manual_offer'] = $v1['manual_offer']; // 代课下单手动优惠金额
                        $coupon_id = $v1['coupon_id']; // 优惠券ID
                        $data['coupon_id'] = $v1['coupon_id']; // 优惠券ID
                        $data['coupon_price'] = $v1['store_coupon_price'] + $v1['platform_coupon_price']; // 优惠金额 =  店铺优惠券优惠金额 + 平台优惠券优惠金额
                        $data['platform_coupon_price'] = $v1['platform_coupon_price']; // 平台优惠券优惠金额

                        $z_price_0 = round($data['spz_price'] * $grade_rate * 0.1, 2); // 会员价
                        $z_price_0 = ($z_price_0 - $data['coupon_price']);
                        $data['z_price'] = $z_price_0 + $v1['freight'] - $v1['manual_offer']; // 订单总价
                        if($data['z_price'] == '0.00' || $data['z_price'] == 0)
                        {
                            $data['z_price'] = '0.01';
                        }

                        $self_lifting = 4; // 虚拟订单无需线下核销
                        $sql2 = "select mch_id,write_off_settings from lkt_product_list where id = '$p_id' ";
                        $r2 = Db::query($sql2);
                        if($r2)
                        {
                            $mch_id = $r2[0]['mch_id'];
                            $data['mch_id'] = ',' . $r2[0]['mch_id'] . ',';
                            if($r2[0]['write_off_settings'] == 1)
                            { // 线下核销
                                $self_lifting = 3; // 虚拟订单需要线下核销
                            }
                            
                            $sql_mch = "select name,tel from lkt_mch where id = '$mch_id' ";
                            $r_mch = Db::query($sql_mch);
                            if($r_mch)
                            {
                                $name = $r_mch[0]['name'];
                                $tel = $r_mch[0]['tel'];
                            }
                        }

                        $data['name'] = $name; // 店铺名称
                        $data['mobile'] = $tel; // 店铺联系方式
                        $data['self_lifting'] = $self_lifting; // 平台优惠券优惠金额

                        //更新详单订单号
                        if($self_lifting == 3)
                        {
                            $r3 = Db::name('order_details')->where(['store_id'=>$store_id,'id'=>$order_details_id])->update(['r_sNo' => $sNo1,'r_status'=>8]);

                            $data['status'] = 8;
                        }
                        else
                        {
                            $r3 = Db::name('order_details')->where(['store_id'=>$store_id,'id'=>$order_details_id])->update(['r_sNo' => $sNo1]);

                            $data['status'] = 5;
                        }
                        if (!$r3)
                        {
                            $this->Log(__METHOD__ . ":" . __LINE__ . "修改订单号失败！order_details_id:" . $order_details_id);
                        }

                        if($coupon_id != '' && $coupon_id != '0')
                        { // 参与优惠券
                            $coupon = new CouponPublicMethod();
                            $r_coupon1 = $coupon->coupon_sno($store_id, $user_id, $coupon_id,$sNo1,'add');
                            if ($r_coupon1 == 2)
                            {
                                //回滚删除已经创建的订单
                                Db::rollback();
                                $message = Lang('order.1');
                                return output(ERROR_CODE_TJYHQGLDDSJSB,$message);
                            }
                        }

                        $data['remarks'] = '';
                        if(count($remarks) > 0)
                        {
                            if($remarks[$v] != '')
                            {
                                $remark = array('0'=>$remarks[$mch_id]);
                                $data['remarks'] = serialize($remark);
                            }
                        }
                        
                        $mch = new mchPublicMethod();        
                        $shop = $mch->Settlement_0();
                        $extraction_code = $shop['extraction_code'];
                        $extraction_code_img = $shop['extraction_code_img'];
                        $data['extraction_code'] = $extraction_code;
                        $data['extraction_code_img'] = $extraction_code_img;

                        $r_attribute = OrderModel::create($data); // 框架模型-新增-静态方法
                        if ($r_attribute->id < 1)
                        {
                            $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！");
                        }

                        $message_5 = "您来新订单了，订单为".$sNo1."，请及时处理！";
                        $message_logging_list5 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>5,'parameter'=>$sNo1,'content'=>$message_5);
                        PC_Tools::add_message_logging($message_logging_list5);
                    }

                    $sql4 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id])->find();
                    $sql4->status = 7;
                    $sql4->recycle = 1;
                    $r4 = $sql4->save();
                    if (!$r4)
                    {
                        $this->Log(__METHOD__ . ":" . __LINE__ . "修改订单状态失败！sNo:" . $sNo);
                    }
                }
                else
                { // 当为单店铺商品
                    $id = $r1[0]['id']; // 订单详情ID
                    $p_id = $r1[0]['p_id']; // 商品ID

                    $self_lifting = 4; // 虚拟订单无需线下核销
                    $sql2 = "select mch_id,write_off_settings from lkt_product_list where id = '$p_id' ";
                    $r2 = Db::query($sql2);
                    if($r2)
                    {
                        $mch_id = $r2[0]['mch_id'];
                        $data['mch_id'] = ',' . $r2[0]['mch_id'] . ',';
                        if($r2[0]['write_off_settings'] == 1)
                        { // 线下核销
                            $self_lifting = 3; // 虚拟订单需要线下核销
                        }
                        
                        $sql_mch = "select name,tel from lkt_mch where id = '$mch_id' ";
                        $r_mch = Db::query($sql_mch);
                        if($r_mch)
                        {
                            $name = $r_mch[0]['name'];
                            $tel = $r_mch[0]['tel'];
                        }
                    }

                    if($self_lifting == 3)
                    {
                        $sql3 = "update lkt_order_details set r_status = 8 where id = '$id' ";
                        $r3 = Db::execute($sql3);
                        if (!$r3)
                        {
                            $this->Log(__METHOD__ . ":" . __LINE__ . "修改订单详情失败！sql:" . $sql3);
                        }

                        $sql4 = "update lkt_order set name = '$name',mobile = '$tel',self_lifting = '$self_lifting',status = 8 where store_id = '$store_id' and sNo = '$sNo' ";
                    }
                    else
                    {
                        $sql4 = "update lkt_order set name = '$name',mobile = '$tel',self_lifting = '$self_lifting' where store_id = '$store_id' and sNo = '$sNo' ";
                    }
                    $r4 = Db::execute($sql4);
                    if (!$r4)
                    {
                        $this->Log(__METHOD__ . ":" . __LINE__ . "修改订单状态失败！sNo:" . $sNo);
                    }
                    
                    $message_5 = "您来新订单了，订单为".$sNo."，请及时处理！";
                    $message_logging_list5 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>5,'parameter'=>$sNo,'content'=>$message_5);
                    PC_Tools::add_message_logging($message_logging_list5);
                }
            }
            
            Db::commit();
            return ;
        }
        catch (\Exception $e) 
        {
            // 回滚事务
            Db::rollback();
            $Log_content = $e->getMessage();
            $this->Log(__METHOD__ . ":" . __LINE__ . $Log_content);
            $message = Lang('operation failed');
            return output(ERROR_CODE_SQSB,$message);
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

        $Dividend_status = 0;
        $res = cache('Divide_accounts_list_'.$trade_no);
        if($res == 'Y')
        {
            $Dividend_status = 1;
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
            $r[0]['transaction_id'] = $transaction_id;
            $r[0]['Dividend_status'] = $Dividend_status;
            if ($status < 1)
            {
                $order = new Order;
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

    /**
     * 发货
     * @param mixed ...$context
     * @return mixed|void
     */
    public function deliveryGood(...$context)
    {
        $action = $context[0];
        $dt = $action->delivery_type;
        $source = 0;
        if(isset($action->source))
        {
            $source = $action->source;
        }
        if ($dt == 'admin')
        {
            if($source == 1 || $source == 12)
            { // 后台 或 供应商端
                DeliveryHelper::adminDelivery_x($action);
            }
            else
            {
                DeliveryHelper::adminDelivery($action);
            }
        }
        else
        {
            if($source == 1)
            {
                DeliveryHelper::frontDelivery_x($action);
            }
            else
            {
                DeliveryHelper::frontDelivery($action);
            }
        }
    }

    /**
     * 收货
     * @param mixed ...$context
     * @return mixed|void
     */
    public function receive_good(...$context)
    {
        $action = $context[0];
        $rt = $action->receivetype;
        switch ($rt)
        {
            case  'list':
                ReceiveGoodsUtils::listReceive($action);
                break;
            case 'detail':
                ReceiveGoodsUtils::detailReceive($action);
                break;
        }
    }

    /**
     * 退款页面（后台退款查看、移动端和PC店铺退款页面）
     * @param mixed ...$context
     * @return mixed|void
     * @throws Exception
     */
    public function refund_page(...$context)
    {
        $action = $context[0];
        $data = RefundUtils::refund_page($action);

        return $data;
    }

    /**
     * 退款
     * @param mixed ...$context
     * @return mixed|void
     * @throws Exception
     */
    public function refund(...$context)
    {
        $action = $context[0];
        RefundUtils::refund($action);
    }

    /**
     * 订单详情(后台、PC店铺)
     * @param mixed ...$context
     * @return mixed|void
     * @throws Exception
     */
    public function order_details(...$context)
    {
        $action = $context[0];
        $list = DeliveryHelper::order_details($action);

        return $list;
    }

    /**
     * 订单详情(移动端)
     * @param mixed ...$context
     * @return mixed|void
     * @throws Exception
     */
    public function app_order_details(...$context)
    {
        $action = $context[0];
        DeliveryHelper::app_order_details($action);
    }

    /**
     * 订单详情(移动端店铺)
     * @param mixed ...$context
     * @return mixed|void
     * @throws Exception
     */
    public function mch_order_details(...$context)
    {
        $action = $context[0];
        DeliveryHelper::mch_order_details($action);
    }

    /**
     * 订单列表
     * @param mixed ...$context
     * @return mixed|void
     */
    public function order_index(...$context)
    {
        $action = $context[0];
        $rt = $action->Servertype;
        switch ($rt)
        {
            case  'pc': // 后台
                $res = DeliveryHelper::order_index($action);
                break;
            case 'a_mch': // 移动端店铺
                $res = DeliveryHelper::a_mch_order_index($action);
                break;
            case 'b_mch': // PC店铺
                $res = DeliveryHelper::b_mch_order_index($action);
                break;
            case 'MchSon': // PC门店端
                $res = DeliveryHelper::MchSon_order_index($action);
                break;
            case 'user': // 用户
                $res = DeliveryHelper::user_order_index($action);
                break;
        }
        return $res;
    }
    
    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/Virtual_order.log",$Log_content);
        return;
    }
}
