<?php
namespace app\common\Plugin;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Plugin\Plugin;
use app\common\Plugin\MchPublicMethod;
use app\common\LaiKeLogUtils;

use app\admin\model\PreSellConfigModel;
use app\admin\model\PreSellGoodsModel;
use app\admin\model\CartModel;
use app\admin\model\BuyAgainModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProductListModel;
use app\admin\model\PreSellRecordModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\UserAddressModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\MchModel;

class Presell_order
{
    // 获取插件状态
    public function is_Plugin($store_id)
    {
        $r0 = PreSellConfigModel::where(['store_id'=>$store_id])->field('is_open')->select()->toArray();
        if ($r0)
        {
            $is_display = $r0[0]['is_open'];
        }
        else
        {
            $is_display = 2;
        }
        return $is_display;
    }

    // 添加插件设置
    public function add($store_id)
    {
        $data = array('store_id'=>$store_id);
        Db::name('lkt_pre_sell_config')->save($data);
        return;
    }

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
        $buy_type = addslashes(Request::post('buy_type'));//提交状态 1是再次购买 空是正常提交
        $canshu = addslashes(Request::post('canshu'));//参数

        $lktlog = new LaiKeLogUtils();
        list($product, $shop_list, $cart_id) = Tools::productHandle($product1, $cart_id);

        $products_total = 0;
        // 支付状态
        $payment = Tools::getPayment($store_id);
        // 用户基本信息
        list($user_money, $enterless, $password_status) = Tools::userInfo($user, $store_id, $user_id,$lktlog);

        //2.区分购物车结算和立即购买---列出选购商品
        $products = Tools::products_list($store_id,$cart_id, $product, 'PS',$buy_type);
        //3.列出商品数组-计算总价和优惠，运费
        $products_data = Tools::get_products_data($store_id,$products, $products_total, 'PS');
        $product_id = $products_data['product_id'][0]; // 商品ID
        $product_class = $products_data['product_class']; // 商品分类
        $products_freight = $products_data['products_freight']; // 运费数据
        $products = $products_data['products']; // 商品数据
        $products_total = $products_data['products_total']; // 商品总价
        $is_distribution = $products[0]['list'][0]['is_distribution'];

        $sellMap = array();
        $r_pre_con = PreSellConfigModel::where(['store_id'=>$store_id])->field('balance_desc,deposit_desc')->select()->toArray();
        $sellMap['balanceDesc'] = $r_pre_con[0]['balance_desc']; // 订货预售说明
        $sellMap['depositDesc'] = $r_pre_con[0]['deposit_desc']; // 订金预售说明

        $r_pre = PreSellGoodsModel::where(['product_id'=>$product_id])->field('sell_type as sellType,deposit')->select()->toArray();
        $sellMap['sellType'] = $r_pre[0]['sellType']; // 预售类型 1.订金模式 2.订货模式
        $deposit_1 = $r_pre[0]['deposit']; // 单个商品订金金额
        $sellType = $r_pre[0]['sellType']; // 单个商品订金金额

        $no_delivery_str = '';
        if($address_id == '')
        { // 获取不配送省的名称
            $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);
        }

        //查询默认地址order_details
        $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);
        $addemt = $address ? 1 : 0; // 收货地址状态

        $products_data0 = Tools::get_products_data0($store_id, $products,$products_total, $user_id);
        $products = $products_data0['products'];
        $products_total = $products_data0['products_total'];
        //4.计算运费
        $freight = Tools::get_freight($products_freight, $products, $address, $store_id, 'PS');
        $products = $freight['products'];
        $yunfei = $freight['yunfei'];
        
        $shop_list = array();
        // 店铺
        $mch = new MchPublicMethod();
        $shop = $mch->Settlement($store_id, $products,'','');
        $shop_status = $shop['shop_status'];
        $time = date('Y-m-d H:i:s');
        if (count($products) == 1)
        {
            $shop_list = $mch->Settlement1($store_id,$products, '');
        }

        $preferential_amount = 0;
        $grade_rate_amount = 0;
        $deposit = 0;
        foreach ($products as $k1 => $v1)
        {
            $grade_rate_amount += $v1['grade_rate_amount'];
            if($sellType == 1)
            {
                foreach($v1['list'] as $k2 => $v2)
                {
                    $balance = $v2['price'] - $deposit_1; // 单个商品尾款
                    $deposit = $deposit_1 * $v2['num']; // 订金金额
                    $sellMap['deposit'] = $deposit_1 * $v2['num']; // 订金金额
                    $sellMap['balance'] = $balance * $v2['num']; // 尾款金额
                }
            }
        }
        if($sellType == 1)
        {
            $total = $deposit; // 商品总价-平台优惠+总运费
        }
        else
        {
            $total = $products_total - $preferential_amount + $yunfei; // 商品总价-平台优惠+总运费
        }

        $grade_rate_amount = round($grade_rate_amount, 2);
        $total = round($total, 2);

        //5.返回数据
        ob_clean();
      
        $data = array('addemt' => $addemt,'address' => $address,'enterless' => $enterless,'freight' => $yunfei,'grade_rate_amount'=>$grade_rate_amount,'is_distribution' => $is_distribution,'password_status' => $password_status,'payment' => $payment,'preferential_amount'=>$preferential_amount,'products' => $products,'products_total' => $products_total,'sellMap'=>$sellMap,'shop_list' => $shop_list,'shop_status' => $shop_status,'total' => $total,'user_money' => $user_money);
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
        // 1.开启事务
        $product1 = addslashes(Request::post('product'));//  商品数组--------['pid'=>66,'cid'=>88]
        $cart_id = addslashes(trim(Request::post('cart_id')));  // 购物车id-- 12,13,123,
        $address_id = Request::post('address_id'); //  地址id
        $remarks = trim(Request::post('remarks')); //  订单备注
        $buy_type = addslashes(Request::post('buy_type'));//提交状态 1是再次购买 空是正常提交
        $pay_type = addslashes(trim(Request::post('pay_type'))); // 支付方式
        $payTarget = addslashes(trim(Request::post('payTarget'))); // 1.定金 2.尾款 3.全款
        $currency_code_0 = trim(Request::param('currency_code')); // ISO货币代码(如USD)
        $currency_symbol_0 = trim(Request::param('currency_symbol')); // 货币符号($)
        $exchange_rate_0 = trim(Request::param('exchange_rate')); // 汇率

        if($remarks != '')
        {
            $remarks_ = htmlspecialchars_decode($remarks); // 将特殊的 HTML 实体转换回普通字符
            $remarks_ = json_decode($remarks_, true);
            $remarks = $remarks_[0];
        }
        
        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>$currency_id));
        $currency_symbol = $userCurrency[0]['currency_symbol']; // 货币符号($)
        $exchange_rate = $userCurrency[0]['exchange_rate'];
        $currency_code = $userCurrency[0]['currency_code'];

        if($currency_symbol_0 != $currency_symbol || $currency_code_0 != $currency_code || $exchange_rate_0 != $exchange_rate)
        {
            $message = Lang('nomal_order.8');
            echo json_encode(array('code' => ERROR_CODE_hbhlygb, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }

        $products_total = 0;
        $total = 0.00;
        $yunfei = 0;

        $product = '';
        $lktlog = new LaiKeLogUtils();
        list($product, $shop_list, $cart_id) = Tools::productHandle($product1, $cart_id);

        //2.区分购物车结算和立即购买---列出选购商品
        $products = Tools::products_list($store_id, $cart_id, $product, 'PS',$buy_type);
        if(empty($address_id))
        {
            $message = Lang('nomal_order.1');
            echo json_encode(array('code' => ERROR_CODE_QXZSHDZ, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }
        //3.列出商品数组-计算总价和优惠，运费
        $products_data = Tools::get_products_data($store_id,$products, $products_total, 'PS');
        $product_id = $products_data['product_id'][0];
        $product_class = $products_data['product_class'];
        $products_freight = $products_data['products_freight'];
        $products = $products_data['products'];
        $products_total = $products_data['products_total'];
        $spz_price = $products_data['products_total'];

        $r_pre = PreSellGoodsModel::where(['product_id'=>$product_id])->field('sell_type as sellType,deposit')->select()->toArray();
        $deposit_1 = $r_pre[0]['deposit']; // 单个商品订金金额
        $sellType = $r_pre[0]['sellType']; // 单个商品订金金额

        // 获取不配送省的名称
        $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);
        if($no_delivery_str != '')
        {
            $no_delivery_list = json_decode($no_delivery_str,true);
        }
        if ($address_id && $no_delivery_list) 
        {
            $r0d = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id,'id'=>$address_id])->select()->toArray();
            if ($r0d) 
            {
                $addx = $r0d[0]['sheng'].'-'.$r0d[0]['city'].'-'.$r0d[0]['quyu'];
                if(in_array($addx,$no_delivery_list))
                {
                    $message = Lang('nomal_order.2');
                    echo json_encode(array('code' => ERROR_CODE_QXZSHDZ, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }
            }
        }
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
        $freight = Tools::get_freight($products_freight, $products, $address, $store_id, 'PS');
        // 存储运费数据
        $products = $freight['products'];
        $yunfei = $freight['yunfei'];
        // 定义初始化数据
        $z_num = 0;
        
        $Toosl = new Tools(1,1);
        $sNo = $Toosl->Generate_order_number('PS', 'sNo'); // 生成订单号
        $real_sno = $Toosl->Generate_order_number('PS', 'real_sno'); // 生成支付订单号

        $mch_id = '';
        $remarks_0 = array();
        $remarks_status = false;

        $total = $products_total + $yunfei; // 商品总价-+总运费

        $order_failure_time = Tools::Obtain_expiration_time(array('store_id'=>$store_id,'otype'=>'PS'));

        Db::startTrans();

        if($payTarget == 1)
        { // 定金
            $deposit = 0;
            foreach ($products as $k => $v)
            { 
                $mch_id = ',' . $v['shop_id'] . ',';

                foreach ($v['list'] as $key => $value)
                {
                    $pdata = (object)$value;
                    $pid = $value['pid'];
                    $cid = $value['cid'];
                    $num = $value['num'];
                    $product_title = addslashes($value['product_title']);
                    $freight_price = floor(isset($value['freight_price']) ? $value['freight_price'] : 0);
                    $z_num += $pdata->num;

                    $order_details_info = array("add_time"=>time(),"after_discount"=>$pdata->amount_after_discount,"coupon_id"=>"0","exchange_num"=>0,"express"=>0,"freight"=>$freight_price,"invoice"=>0,"manual_offer"=>0,"num"=>$pdata->num,"p_id"=>$pid,"p_name"=>$product_title,"p_price"=>$pdata->price,"r_sNo"=>$sNo,"r_status"=>0,"recycle"=>0,"settlement_type"=>0,"sid"=>$cid,"size"=>$pdata->size,"store_id"=>$store_id,"unit"=>$pdata->unit,"user_id"=>$user_id);

                    $r0 = ConfigureModel::where(['id'=>$cid,'pid'=>$pid])->field('num,min_inventory')->select()->toArray();
                    $total_num = $r0[0]['num'];
                    $min_inventory = $r0[0]['min_inventory'];
                    //非会员特惠商品才减库存
                    // 销量+1 库存-1
                    $sql_del1 = array('volume'=>Db::raw('volume + '.$num),'num'=>Db::raw('num - '.$num));
                    $res_del1 = Db::name('product_list')->where('id', $pid)->update($sql_del1);
                    if (!$res_del1)
                    {
                        $this->log(__METHOD__ . ":" . __LINE__ . "修改商品库存失败！pid:" . $pid);

                        Db::rollback();
                        ob_clean();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }

                    $sql_del2 = array('num'=>Db::raw('num - '.$num));
                    $res_del2 = Db::name('configure')->where('id', $cid)->update($sql_del2);
                    if (!$res_del2)
                    {
                        $this->log(__METHOD__ . ":" . __LINE__ . "修改商品属性库存失败！cid:" . $cid);
                        Db::rollback();
                        ob_clean();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }
                    $content = $user_id . '生成订单所需' . $num;
                    $sql_stock = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>1,'user_id'=>$user_id,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content);
                    $r_stock = Db::name('stock')->insert($sql_stock);

                    if($total_num - $num <= $min_inventory)
                    {   
                        $content1 = '预警';
                        $sql_stock1 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>$min_inventory,'type'=>2,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content1);
                        $r_stock1 = Db::name('stock')->insert($sql_stock1);
                        
                        $message_9 = "商品ID为".$pid."的商品库存不足，请尽快补充库存";
                        $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>$mch_id1,'type'=>9,'parameter'=>$cid,'content'=>$message_9);
                        PC_Tools::add_message_logging($message_logging_list9);
                    }

                    $balance = ($value['price'] - $deposit_1) * $value['num']; // 尾款金额
                    $deposit = $deposit_1 * $value['num']; // 订金金额
                }
            }
            $order_info = array("add_time"=>time(),"address"=>$address_xq,'code'=>$code,"allow"=>0,"bargain_id"=>0,"comm_discount"=>1,"consumer_money"=>0,"coupon_activity_name"=>"","coupon_id"=>"0","coupon_price"=>0,"delivery_status"=>0,"drawid"=>0,"extraction_code"=>"","extraction_code_img"=>"","grade_rate"=>$grade_rate,"is_anonymous"=>0,"is_put"=>0,"manual_offer"=>0.00,"mch_id"=>$mch_id,'cpc'=>$cpc,"mobile"=>$mobile,"name"=>$name,"num"=>$num,"offset_balance"=>0.00,"operation_type"=>1,"otype"=>"PS","p_sNo"=>"","pay"=>$pay_type,"pick_up_store"=>0,"preferential_amount"=>0,"readd"=>0,"real_sno"=>$real_sno,"recycle"=>0,"red_packet"=>"0","reduce_price"=>0,"remark"=>"","remarks"=>$remarks,"sNo"=>$sNo,"self_lifting"=>0,"settlement_status"=>0,'shop_cpc'=>$shop_cpc,"sheng"=>$sheng,"shi"=>$shi,"single_store"=>0,"source"=>$store_type,"spz_price"=>$spz_price,"status"=>0,"store_id"=>$store_id,"subtraction_id"=>0,"user_id"=>$user_id,"xian"=>$xian,"z_freight"=>$yunfei,"z_price"=>$total,"zhekou"=>0,'currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code);
            
            $sql_1 = array('store_id'=>$store_id,'user_id'=>$user_id,'product_id'=>$pid,'attr_id'=>$cid,'price'=>0,'deposit'=>$deposit,'balance'=>$balance,'num'=>$z_num,'sNo'=>$sNo,'is_deposit'=>'0','is_balance'=>'0','is_refund'=>0,'is_delete'=>0,'add_time'=>date("Y-m-d H:i:s"),'pay_balance_time'=>NULL,'order_info'=>json_encode($order_info),'order_details_info'=>json_encode($order_details_info));
            $r_1 = Db::name('pre_sell_record')->insertGetId($sql_1);
            if($r_1 > 0)
            {
                $arr = array('sNo' => $sNo, 'total' => round($deposit,2),'orderTime'=>date("Y-m-d H:i:s"));
                ob_clean();
                Db::commit();
                $message = Lang("Success");
                echo json_encode(array('code' => 200, 'data' => $arr,'message'=>$message));
                exit;
            }
            else
            {
                $this->log(__METHOD__ . ":" . __LINE__ . "添加预售记录失败！参数:" . json_encode($sql_1));
                //回滚删除已经创建的订单
                Db::rollback();
                ob_clean();
                $message = Lang('nomal_order.6');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }
        }
        else if($payTarget == 2)
        { // 尾款

        }
        else if($payTarget == 3)
        { // 全款
            foreach ($products as $k => $v)
            {   
                $mch_id .= $v['shop_id'] . ',';
                $mch_id1 = $v['shop_id'];

                //如果是多店铺，添加一条购买记录
                $mch = new MchPublicMethod();
                $sql = $mch->addMchBrowse($mch_id, $store_id, $mch_id1, $user_id); // 生成订单号

                // 循环商品数据
                foreach ($v['list'] as $key => $value)
                {
                    $pdata = (object)$value;
                    $pid = $value['pid'];
                    $cid = $value['cid'];
                    $num = $value['num'];
                    $product_title = addslashes($value['product_title']);
                    $freight_price = floor(isset($value['freight_price']) ? $value['freight_price'] : 0);
                    //TODO 这个没有用到
                    // 循环插入订单附表 ，添加不同的订单详情
                    $sql_d = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pdata->pid,'p_name'=>$product_title,'p_price'=>$pdata->price,'num'=>$pdata->num,'unit'=>$pdata->unit,'r_sNo'=>$sNo,'add_time'=>date("Y-m-d H:i:s"),'r_status'=>0,'size'=>$pdata->size,'sid'=>$pdata->cid,'freight'=>$freight_price,'coupon_id'=>0,'after_discount'=>$pdata->amount_after_discount);
                    $beres = Db::name('order_details')->insertGetId($sql_d);
                    if ($beres < 1)
                    { // 添加失败
                        $this->log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！sql:" . $sql_d);
                        // 回滚事件，给提示
                        Db::rollback();
                        ob_clean();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }

                    $z_num += $pdata->num;
                    if ($cart_id != '' && $buy_type != 1)
                    { // 删除对应购物车内容
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
                    
                    $r0 = ConfigureModel::where(['id'=>$cid,'pid'=>$pid])->field('num,min_inventory')->select()->toArray();
                    $total_num = $r0[0]['num'];
                    $min_inventory = $r0[0]['min_inventory'];
                    //非会员特惠商品才减库存
                    // 销量+1 库存-1
                    $sql_del1 = array('volume'=>Db::raw('volume + '.$num),'num'=>Db::raw('num - '.$num));
                    $res_del1 = Db::name('product_list')->where('id', $pid)->update($sql_del1);
                    if (!$res_del1)
                    {
                        $this->log(__METHOD__ . ":" . __LINE__ . "修改商品库存失败！pid:" . $pid);

                        Db::rollback();
                        ob_clean();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }

                    $sql_del2 = array('num'=>Db::raw('num - '.$num));
                    $res_del2 = Db::name('configure')->where('id', $cid)->update($sql_del2);
                    if (!$res_del2)
                    {
                        $this->log(__METHOD__ . ":" . __LINE__ . "修改商品属性库存失败！cid:" . $cid);
                        Db::rollback();
                        ob_clean();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }
                    $content = $user_id . '生成订单所需' . $num;
                    $sql_stock = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>1,'user_id'=>$user_id,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content);
                    $r_stock = Db::name('stock')->insert($sql_stock);

                    if($total_num - $num <= $min_inventory)
                    {   
                        $content1 = '预警';
                        $sql_stock1 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>$min_inventory,'type'=>2,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content1);
                        $r_stock1 = Db::name('stock')->insert($sql_stock1);
                        
                        $message_9 = "商品ID为".$pid."的商品库存不足，请尽快补充库存";
                        $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>$mch_id1,'type'=>9,'parameter'=>$cid,'content'=>$message_9);
                        PC_Tools::add_message_logging($message_logging_list9);
                    }
                }
            }

            $mch_id = rtrim($mch_id, ',');
            $mch_id = ',' . $mch_id . ',';

            $sql_o = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'num'=>$z_num,'z_price'=>$total,'sNo'=>$sNo,'shop_cpc'=>$shop_cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_xq,'code'=>$code,'remark'=>'','pay'=>$pay_type,'add_time'=>date("Y-m-d H:i:s"),'status'=>0,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>'','spz_price'=>$spz_price,'reduce_price'=>0,'coupon_price'=>0,'source'=>$store_type,'otype'=>'PS','mch_id'=>$mch_id,'p_sNo'=>'','bargain_id'=>0,'comm_discount'=>1,'remarks'=>$remarks,'real_sno'=>$real_sno,'self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','grade_rate'=>$grade_rate,'z_freight'=>$yunfei,'preferential_amount'=>0,'single_store'=>0,'order_failure_time'=>$order_failure_time,'currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code);
            $r_o = Db::name('order')->insertGetId($sql_o);
            if ($r_o > 0)
            {
                $sql_1 = array('store_id'=>$store_id,'user_id'=>$user_id,'product_id'=>$pid,'attr_id'=>$cid,'price'=>0,'num'=>$z_num,'sNo'=>$sNo,'is_refund'=>0,'is_delete'=>0,'add_time'=>date("Y-m-d H:i:s"));
                $r_1 = Db::name('pre_sell_record')->insertGetId($sql_1);
              
                $sql_2 = array('surplus_num'=>Db::raw('surplus_num - '.$num));
                $r_2 = Db::name('pre_sell_goods')->where('product_id', $product_id)->update($sql_2);
 
                $arr = array('sNo' => $sNo, 'total' => round($total,2), 'order_id' => $r_o,'order_type'=>'PS','orderTime'=>date("Y-m-d H:i:s"));
                ob_clean();
                Db::commit();
                $message = Lang("Success");
                echo json_encode(array('code' => 200, 'data' => $arr,'message'=>$message));
                exit;
            }
            else
            {
                $this->log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数:" . json_encode($sql_o));
                //回滚删除已经创建的订单
                Db::rollback();
                ob_clean();
                $message = Lang('nomal_order.6');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }
        }
    }

    // 余额支付
    public function walletcb(...$context)
    {
        $action = $context[0];
        $store_id = $action->store_id;
        $user_id = $action->user_id;
        $payment_money = $action->payment_money;
        $sNo = $action->sNo;
        $action->gndd($store_id, $payment_money, $sNo, $user_id,'PS');
    }

    /**
     * 支付前(余额支付除外)
     * @param mixed ...$context
     * @return mixed|void
     */
    public function preparePay(...$context)
    {
        $action = $context[0];
        $db = $action->db;
        $store_id = $action->store_id;
        $request = $action->getContext()->getRequest();
        $sNo = $action->sNo; // 订单号
        $payment_money = $action->payment_money; // 支付金额
        $type = $action->type; // 支付类型
        $order_types = $action->order_types; // 订单类型

        // 根据商城ID、订单号、用户，查询预售记录
        $r_pre_sell_record = PreSellRecordModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id])->field('is_deposit')->select()->toArray();
        if($r_pre_sell_record)
        { // 存在
            $is_deposit = $r_pre_sell_record[0]['is_deposit'];
            $order_info = json_decode($r_pre_sell_record[0]['order_info'],true);

            if($is_deposit == '')
            { // 订货模式
                $real_sno = Tools::order_number($order_types);
                $up_orderType_Res = Db::name('order')->where(['store_id'=>$store_id,'sNo'=>$sNo])->update(['pay'=>$type,'real_sno'=>$real_sno]);
            }
            else if($is_deposit == 0)
            { // 未支付定金
                $real_sno = $sNo;
            }
            else if($is_deposit == 1)
            { // 未支付尾款
                $real_sno = $order_info['real_sno'];
                $up_orderType_Res = Db::name('order')->where(['store_id'=>$store_id,'sNo'=>$sNo])->update(['pay'=>$type]);
            }
        }
        else
        { // 不存在（订货模式）
            $real_sno = Tools::order_number($order_types);
            $up_orderType_Res = Db::name('order')->where(['store_id'=>$store_id,'sNo'=>$sNo])->update(['pay'=>$type,'real_sno'=>$real_sno]);
        }

        $total = $payment_money;
        return array($real_sno, $total);
    }

    /**
     * 回调参数校验
     * @param mixed ...$context
     * @return mixed|void
     */
    public function toCheck(&...$context)
    {
        $this->log(__METHOD__ . ":" . __LINE__ . "支付回调开始" );
        $this->log(__METHOD__ . ":" . __LINE__ . "参数：" . json_encode($context));

        $db = $context[0][0];
        $trade_no = $context[0][1];
        $log = $context[0][2];
        $log_name = $context[0][3];

        // 根据订单号，查询预售记录
        $r_pre_sell_record = PreSellRecordModel::where(['sNo'=>$trade_no])->field('store_id,is_deposit,order_info')->select()->toArray();
        if($r_pre_sell_record)
        { // 存在
            $is_deposit = $r_pre_sell_record[0]['is_deposit'];

            if($is_deposit == '')
            { // 订货模式
                $r = OrderModel::where(['real_sno|sNo'=>$trade_no])->field('store_id,pay')->select()->toArray();
                $this->log(__METHOD__ . ":" . __LINE__ . "支付单号：" . $trade_no);
                if (!$r)
                {
                    $this->log(__METHOD__ . ":" . __LINE__ . "预售订单回调失败，支付单号：" . $trade_no . "没有查询到订单信息");
                    ob_clean();
                    echo 'error';
                    exit;
                }
                $store_id = $r[0]['store_id'];
                $pay_type = $r[0]['pay'];
            }
            else if($is_deposit == 0)
            { // 未支付定金
                $order_info = json_decode($r_pre_sell_record[0]['order_info'],true);
                $store_id = $r_pre_sell_record[0]['store_id'];
                $pay_type = $order_info[0]['pay'];
            }
            else if($is_deposit == 1)
            { // 未支付尾款
                $r = OrderModel::where(['real_sno|sNo'=>$trade_no])->field('store_id,pay')->select()->toArray();
                $this->log(__METHOD__ . ":" . __LINE__ . "支付单号：" . $trade_no);
                if (!$r)
                {
                    $this->log(__METHOD__ . ":" . __LINE__ . "预售订单回调失败，支付单号：" . $trade_no . "没有查询到订单信息");
                    ob_clean();
                    echo 'error';
                    exit;
                }
                $store_id = $r[0]['store_id'];
                $pay_type = $r[0]['pay'];
            }
        }
        else
        { // 不存在（定金模式支付尾款）
            $r = OrderModel::where(['real_sno|sNo'=>$trade_no])->field('store_id,pay')->select()->toArray();
            $this->log(__METHOD__ . ":" . __LINE__ . "支付单号：" . $trade_no);
            if (!$r)
            {
                $this->log(__METHOD__ . ":" . __LINE__ . "预售订单回调失败，支付单号：" . $trade_no . "没有查询到订单信息");
                ob_clean();
                echo 'error';
                exit;
            }
            $store_id = $r[0]['store_id'];
            $pay_type = $r[0]['pay'];
        }

        if ($pay_type == 'tt_alipay')
        {
            $pay_type = 'alipay';
        }

        $config = LKTConfigInfo::getPayConfig($store_id, $pay_type);
        if (empty($config))
        {
            $this->log(__METHOD__ . ":" . __LINE__ . "预售订单执行日期：" . date('Y-m-d H:i:s') . "支付暂未配置，商城ID：" . $store_id . "支付类型：" . $pay_type . "无法调起支付！");
            return 'file';
        }

        $this->log(__METHOD__ . ":" . __LINE__ . "支付配置参数：" . json_encode($config));
        return $config;
    }

    /**
     * @inheritDoc
     */
    public function paycb(&...$context)
    {
        $data = $context[0];
        $db = $data->db;
        $log = $data->log;
        $log_name = $data->log_name;
        $total = $data->total;
        $trade_no = $data->trade_no;
        $time = date("Y-m-d H:i:s");

        if (empty($trade_no) || $total <= 0)
        {
            $this->Log(__METHOD__ . ":" . __LINE__ . "预售订单回调失败，订单：" . $trade_no . "支付金额：" . $total );
            ob_clean();
            echo 'error';
            exit;
        }

        // 根据订单号，查询预售记录
        $r_pre_sell_record = PreSellRecordModel::where(['sNo'=>$sNo])->field('id,store_id,is_deposit,order_info,order_details_info')->select()->toArray();
        if($r_pre_sell_record)
        { // 存在
            $pre_sell_record_id = $r_pre_sell_record[0]['id'];
            $is_deposit = $r_pre_sell_record[0]['is_deposit'];
            if($is_deposit == '')
            { // 订货模式
                $r = OrderModel::where(['trade_no'=>$trade_no])->select()->toArray();
                $this->Log(__METHOD__ . ":" . __LINE__ . "支付单号：" . $trade_no);
                if ($r)
                {
                    $status = $r[0]->status;
                    if ($status < 1)
                    {
                        $order = new order;
                        $order->up_order((array)$r[0]);
                        $this->Log(__METHOD__ . ":" . __LINE__ . "预售订单data：" . json_encode((array)$r[0]));
                    }
                }
            }
            else if($is_deposit == 0)
            { // 未支付定金
                $order_info = json_decode($r_pre_sell_record[0]['order_info'],true);
                $order_details_info = json_decode($r_pre_sell_record[0]['order_details_info'],true);

                $PC_Tools = new PC_Tools($store_id,1);
                $sNo = $PC_Tools->order_number('PS', 'sNo'); // 生成订单号

                $sql_o = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$order_info['name'],'mobile'=>$order_info['mobile'],'num'=>$order_info['num'],'z_price'=>$order_info['z_price'],'sNo'=>$sNo,'sheng'=>$order_info['sheng'],'shi'=>$order_info['shi'],'xian'=>$order_info['xian'],'address'=>$order_info['address'],'remark'=>$order_info['remark'],'pay'=>$order_info['pay'],'add_time'=>date("Y-m-d H:i:s"),'status'=>0,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>'','spz_price'=>$order_info['spz_price'],'reduce_price'=>0,'coupon_price'=>0,'source'=>$order_info['source'],'otype'=>'PS','mch_id'=>$order_info['mch_id'],'p_sNo'=>'','bargain_id'=>0,'comm_discount'=>1,'remarks'=>$order_info['remarks'],'real_sno'=>$order_info['real_sno'],'self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','grade_rate'=>$order_info['grade_rate'],'z_freight'=>$order_info['z_freight'],'preferential_amount'=>0,'single_store'=>0,'currency_symbol'=>$order_info['currency_symbol'],'exchange_rate'=>$order_info['exchange_rate'],'currency_code'=>$order_info['currency_code']);
                $r_o = Db::name('order')->insertGetId($sql_o);
                if($r_o > 0)
                {
                    $sql_d = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$order_details_info['p_id'],'p_name'=>$order_details_info['p_name'],'p_price'=>$order_details_info['p_price'],'num'=>$order_details_info['num'],'unit'=>$order_details_info['unit'],'r_sNo'=>$sNo,'add_time'=>date("Y-m-d H:i:s"),'r_status'=>0,'size'=>$order_details_info['size'],'sid'=>$order_details_info['sid'],'freight'=>$order_details_info['freight'],'coupon_id'=>0,'after_discount'=>$order_details_info['after_discount']);
                    $r_d = Db::name('order_details')->insertGetId($sql_d);
                    if($r_d > 0)
                    {
                        $sql1_where = array('id'=>$pre_sell_record_id);
                        $sql1_update = array('price'=>Db::raw('price+'.$payment_money),'is_deposit'=>1,'sNo'=>$sNo);
                        $r1 = Db::name('pre_sell_record')->where($sql1_where)->update($sql1_update);
                        
                        $message = Lang('Success');
                        return output(200, $message);
                    }
                    else
                    { // 回滚删除已经创建的订单
                        $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数:" . json_encode($sql_d));
                        ob_clean();
                        $message = Lang('nomal_order.6');
                        return output(400, $message);
                    }
                }
                else
                { // 回滚删除已经创建的订单
                    $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数:" . json_encode($sql_o));
                    ob_clean();
                    $message = Lang('nomal_order.6');
                    return output(400, $message);
                }
            }
            else if($is_deposit == 1)
            { // 未支付尾款
                // 修改预售记录信息
                $sql1_where = array('id'=>$pre_sell_record_id);
                $sql1_update = array('price'=>Db::raw('price+'.($total * 0.01)),'is_balance'=>1,'pay_balance_time'=>$time);
                $r1 = Db::name('pre_sell_record')->where($sql1_where)->update($sql1_update);

                $r = OrderModel::where(['trade_no'=>$trade_no])->select()->toArray();
                $this->log(__METHOD__ . ":" . __LINE__ . "支付单号：" . $trade_no);
                if ($r)
                {
                    $status = $r[0]->status;
                    if ($status < 1)
                    {
                        $order = new order;
                        $order->up_order((array)$r[0]);
                        $this->Log(__METHOD__ . ":" . __LINE__ . "预售订单data：" . json_encode((array)$r[0]));
                    }
                }
            }
        }
        else
        { // 不存在（定金模式支付尾款）
            // 根据支付单号，查询订单信息
            $r = OrderModel::where(['trade_no'=>$trade_no])->select()->toArray();
            $this->log(__METHOD__ . ":" . __LINE__ . "支付单号：" . $trade_no);
            if ($r)
            {
                $sNo = $r[0]->sNo; // 订单号
                $status = $r[0]->status;

                // 根据订单号，修改预售记录信息
                $sql1_where = array('sNo'=>$sNo);
                $sql1_update = array('price'=>Db::raw('price+'.($total * 0.01)),'is_balance'=>1,'pay_balance_time'=>$time);
                $r1 = Db::name('pre_sell_record')->where($sql1_where)->update($sql1_update);

                if ($status < 1)
                {
                    $order = new order;
                    $order->up_order((array)$r[0]);
                    $this->Log(__METHOD__ . ":" . __LINE__ . "预售订单data：" . json_encode((array)$r[0]));
                }
            }
        }

        echo json_encode(array('code' => 200, 'message' => "success"));
        exit;
    }
    
    // 定时任务
    public function dotask($params)
    {
        $store_id = $params->store_id;
        $now = date("Y-m-d H:i:s"); //当前时间戳

        $this->order_failure($store_id); // 删除订单过期

        $this->seckill_status($store_id); // 自动下架商品

        $this->order_settlement($store_id); // 商家订单结算
        
        return;
    }
    
    // 删除过期未支付尾款预售订单
    public function order_failure($store_id)
    {
        $time0 = date("Y-m-d H:i:s"); // 当前时间
        $order_failure = 1;
        $r_config = PreSellConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if($r_config)
        {
            $order_failure = (int)($r_config[0]['order_failure'] / 3600); // 订单失效 (单位 秒)
        }

        $time = date("Y-m-d H:i:s", strtotime("-$order_failure hour")); // 订单过期删除时间

        // 根据用户id，订单为未付款，订单添加时间 小于 未付款订单保留时间,查询订单表
        $r0 = OrderModel::where(['store_id'=>$store_id,'status'=>0,'otype'=>'PS'])->where('add_time','<',$time)->select()->toArray();
        if($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $user_id = $v['user_id']; // user_id
                $sNo = $v['sNo']; // 订单号
                $psNo = $v['p_sNo']; // 父订单号
                $otype = $v['otype'];//订单类型
                
                $sql_0 = "select b.sell_type,b.balance_pay_time from lkt_pre_sell_record as a left join lkt_pre_sell_goods as b on a.product_id = b.product_id where a.store_id = '$store_id' and a.sNo = '$sNo' and a.user_id = '$user_id' ";
                $r_0 = Db::query($sql_0);
                $sell_type = $r_0[0]['sell_type']; // 预售类型   1.订金模式   2.订货模式
                $balance_pay_time = $r_0[0]['balance_pay_time']; // 尾款支付日期
                
                if($sell_type == 1)
                { // 定金模式
                    if($time0 >= $balance_pay_time)
                    { // 当前时间 >= 尾款支付日期（交易失败）
                        $sql_where_o = array('store_id'=>$store_id,'status'=>0,'sNo'=>$sNo);
                        $r_o = Db::name('order')->where($sql_where_o)->update(['status'=>'7']);
                        $this->Log(__METHOD__ . '->' . __LINE__ . '订单更新为关闭状态！参数：' . json_encode($sql_where_o));
        
                        // 根据用户id、订单未付款、添加时间小于前天时间,就删除订单详情信息
                        $r1 = OrderDetailsModel::where(['store_id'=>$store_id,'r_status'=>0,'r_sNo'=>$sNo])->where('add_time','<',$time)->select()->toArray();
                        if ($r1)
                        {
                            foreach ($r1 as $k1 => $v1)
                            {
                                // 删除订单详情信息
                                $sql_where2 = array('store_id'=>$store_id,'id'=>$v1['id']);
                                $r2 = Db::name('order_details')->where($sql_where2)->update(['r_status'=>'7']);
        
                                $sql_where3 = array('store_id'=>$store_id,'id'=>$v1['p_id']);
                                $r3 = Db::name('product_list')->where($sql_where3)->update(['num'=>Db::raw('num+'.$v1['num']),'volume'=>Db::raw('volume-'.$v1['num'])]);
        
                                $sql_where4 = array('id'=>$v1['sid']);
                                $r4 = Db::name('configure')->where($sql_where4)->update(['num'=>Db::raw('num+'.$v1['num'])]);
        
                                $sql_where5 = array('product_id'=>$v1['p_id']);
                                $r5 = Db::name('pre_sell_goods')->where($sql_where5)->update(['surplus_num'=>Db::raw('surplus_num+'.$v1['num'])]);
                            }
                        }
                    }
                }
                else
                { // 订货模式
                    $sql_where_o = array('store_id'=>$store_id,'status'=>0,'sNo'=>$sNo);
                    $r_o = Db::name('order')->where($sql_where_o)->update(['status'=>'7']);
                    $this->Log(__METHOD__ . '->' . __LINE__ . '订单更新为关闭状态！参数：' . json_encode($sql_where_o));
    
                    // 根据用户id、订单未付款、添加时间小于前天时间,就删除订单详情信息
                    $r1 = OrderDetailsModel::where(['store_id'=>$store_id,'r_status'=>0,'r_sNo'=>$sNo])->where('add_time','<',$time)->select()->toArray();
                    if ($r1)
                    {
                        foreach ($r1 as $k1 => $v1)
                        {
                            // 删除订单详情信息
                            $sql_where2 = array('store_id'=>$store_id,'id'=>$v1['id']);
                            $r2 = Db::name('order_details')->where($sql_where2)->update(['r_status'=>'7']);
    
                            $sql_where3 = array('store_id'=>$store_id,'id'=>$v1['p_id']);
                            $r3 = Db::name('product_list')->where($sql_where3)->update(['num'=>Db::raw('num+'.$v1['num']),'volume'=>Db::raw('volume-'.$v1['num'])]);
    
                            $sql_where4 = array('id'=>$v1['sid']);
                            $r4 = Db::name('configure')->where($sql_where4)->update(['num'=>Db::raw('num+'.$v1['num'])]);
    
                            $sql_where5 = array('product_id'=>$v1['p_id']);
                            $r5 = Db::name('pre_sell_goods')->where($sql_where5)->update(['surplus_num'=>Db::raw('surplus_num+'.$v1['num'])]);
                        }
                    }
                }
            }
        }
    }

    //自动下架商品
    public function seckill_status($store_id)
    {   
        $lktlog = new LaiKeLogUtils();
        $time = date('Y-m-d H:i:s');

        $sql0 = "select * from lkt_product_list where store_id = '$store_id' and recycle = 0 and is_presell = 1 and status = 2 ";
        $r0 = Db::query($sql0);
        if($r0)
        {   
            foreach ($r0 as $k0 => $v0)
            {   
                Db::startTrans();
                $id = $v0['id'];
                $starttime = $v0['upper_shelf_time'];//上架时间
                $rr = 0;
                $rp0 = PreSellGoodsModel::where(['product_id'=>$id])->field('*')->select()->toArray();
                if ($rp0) 
                {
                    $sell_type = $rp0[0]['sell_type'];
                    //定金
                    if($sell_type == 1)
                    {
                        if($time > $rp0[0]['balance_pay_time']) 
                        {
                            // 根据商品ID，修改商品数据
                            $rr = Db::name('product_list')->where(['id' => $id])->update(['status' => 3]);
                        }
                    }
                    //订货
                    if($sell_type == 2)
                    {
                        $endDay = $rp0[0]['end_day'];
                        if($starttime) 
                        {
                            $deadline = date('Y-m-d H:i:s', strtotime($starttime . ' +'.$endDay.' day'));
                            if($time > $deadline) 
                            {
                                // 根据商品ID，修改商品数据
                                $rr = Db::name('product_list')->where(['id' => $id])->update(['status' => 3]);
                            }
                        }
                    }
                }              
                if($rr < 0)
                {
                    Db::rollback();
                    $this->Log(__METHOD__ . '->' . __LINE__ . '修改商品状态失败！id:' . $id);
                }
                Db::commit();
            }
            
        }
        return;
    }

    // 商家订单结算
    public function order_settlement($store_id)
    {   
        $lktlog = new LaiKeLogUtils();
        $time0 = date('Y-m-d H:i:s');

        $order_after = 0;
        $r = PreSellConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if($r)
        {
            $order_after = $r[0]['order_after'];
        }
        $time = date("Y-m-d H:i:s",strtotime("-$order_after day"));
        Db::startTrans();

        $sql_m = "select z_price,mch_id,sNo,id,supplier_id,Dividend_status,otype,transaction_id,pay,trade_no from lkt_order where store_id = '$store_id' and status = 5 and recycle = 0 and arrive_time <= '$time' and settlement_status = 0 and otype = 'PS' ";
        $res_m = Db::query($sql_m);
        if($res_m)
        {
            foreach ($res_m as $key => $value) 
            {
                $sNo = $value['sNo'];
                $mch_id = substr($value['mch_id'], 1, -1);
                $money = $value['z_price']; // 订单金额
                $otype = $value['otype']; // 订单类型
                $transaction_id = $value['transaction_id']; // 微信返回支付单号唯一标识
                $pay = $value['pay']; // 支付方式
                $trade_no = $value['trade_no']; // 支付单号

                $res = ReturnOrderModel::where(['sNo'=>$value['sNo']])->whereIn('r_type','0,1,3')->where('re_type','<>',3)->field('id')->select()->toArray();
                if(empty($res))
                {
                    if($value['z_price'] > 0)
                    {
                        $sql_u_where = array('store_id'=>$store_id,'id'=>$mch_id);
                        // 根据店铺ID，店铺商户余额减少，可取现金额增加
                        $sql_u_update = array('account_money'=>Db::raw('account_money-'.$money),'cashable_money'=>Db::raw('cashable_money+'.$money)); //49566
                        $res_u = Db::name('mch')->where($sql_u_where)->update($sql_u_update);
                        if($res_u < 0)
                        {   
                            Db::rollback();
                            $this->Log(__METHOD__ . ":" . __LINE__ . "订单结算到账失败！参数:" . json_encode($sql_u_where));
                        }
                        else
                        {
                            $r9 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('account_money,integral_money')->select()->toArray();
                            $account_money = $r9[0]['account_money'];
                            $integral_money = $r9[0]['integral_money'];
                            //添加店铺入账记录
                            $data10 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'price'=>$money,'integral'=>0,'integral_money'=>$integral_money,'account_money'=>$account_money,'type'=>1,'addtime'=>date("Y-m-d H:i:s"),'remake'=>$sNo,'status'=>1);
                            $r10 = Db::name('mch_account_log')->insert($data10);
                            if ($r10 <= 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '添加入驻商户账户收支记录失败！sql:'.json_encode($data10);
                                $this->Log($Log_content);
                                Db::rollback();
                            }
                            
                            $sql_where = array('store_id'=>$store_id,'r_sNo'=>$value['sNo'],'r_status'=>5);
                            $sql_update = array('settlement_type'=>1);
                            $res = Db::name('order_details')->where($sql_where)->update($sql_update);
                            if($res < 0)
                            {   
                                Db::rollback();
                                $this->Log(__METHOD__ . ":" . __LINE__ . "订单结算失败！参数:" . json_encode($sql_where));
                            }
                            else
                            {
                                $this->Log(__METHOD__ . ":" . __LINE__ . "订单结算成功！参数:" . json_encode($sql_where));
                            }
                        }   
                    }

                    //更新订单结算状态
                    $sql_where = array('store_id'=>$store_id,'sNo'=>$sNo);
                    $sql_update = array('settlement_status'=>1);
                    $res_o = Db::name('order')->where($sql_where)->update($sql_update);
                    if(!$res_o)
                    {
                        Db::rollback();
                        $this->Log(__METHOD__ . ":" . __LINE__ . "订单结算状态修改失败！参数:sNo" . $sNo);
                    }
                }
            }
        }
        Db::commit();

        return;
    }

    // 获取插件状态
    public function Get_plugin_status($store_id)
    {
        $is_status = 0;
        $is_status = Db::name('pre_sell_config')->where(['store_id'=>$store_id])->value('is_open');
      
        return $is_status;
    }
    
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/presell_order.log",$Log_content);
        return;
    }
}
