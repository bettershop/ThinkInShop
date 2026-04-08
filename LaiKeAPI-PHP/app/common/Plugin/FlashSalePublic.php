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
use app\common\Jurisdiction;

use app\admin\model\AdminModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\CartModel;
use app\admin\model\BuyAgainModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProductListModel;
use app\admin\model\StockModel;
use app\admin\model\UserFirstModel;
use app\admin\model\UserModel;
use app\admin\model\MchModel;
use app\admin\model\FlashsaleConfigModel;
use app\admin\model\FlashsaleLabelModel;
use app\admin\model\FlashsaleProModel;
use app\admin\model\FlashsaleActivityModel;
use app\admin\model\FlashsaleRecordModel;
use app\admin\model\FlashsaleAddgoodsModel;
use app\admin\model\SystemMessageModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\ProductClassModel;
use app\admin\model\BrandClassModel;


//限时折扣公共文件
class FlashSalePublic 
{
    // 获取插件状态
    public function is_Plugin($store_id)
    {
        $r0 = FlashsaleConfigModel::where('store_id', $store_id)->field('is_open')->select()->toArray();
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

    public function settlement(&...$context)
    {   
        $action = $context[0];
        //1.列出基础数据
        $user = $action->user;
        $user_id = $user['user_id'];
        $store_id = $action->store_id;
        $product1 = addslashes(Request::param('product'));//  商品数组--------['pid'=>66,'cid'=>88]
        $cart_id = addslashes(trim(Request::param('cart_id')));  //购物车id-- 12,13,123,
        $address_id = Request::param('address_id'); //  地址id
        $shop_address_id = Request::param('shop_address_id'); //  门店地址id
        $product_type = addslashes(Request::param('product_type'));//产品类型，JP-竞拍商品,KJ-砍价商品
        $grade_l = addslashes(Request::param('grade_l'));//会员特惠 兑换券级别
        $add_good = addslashes(Request::param('add_good'));//  加购商品 --------12,123
        $canshu = addslashes(Request::param('canshu'));//参数

        $add_good = htmlspecialchars_decode($add_good);
        $add_good = stripslashes(html_entity_decode($add_good)); // 字符串打散为数组
        $lktlog = new LaiKeLogUtils();

        list($product, $shop_list, $cart_id) = Tools::productHandle($product1, $cart_id);
        $products_total = 0;

        // 支付状态
        $payment = Tools::getPayment($store_id);
        //用户基本信息
        list($user_money, $enterless, $password_status) = Tools::userInfo($user, $store_id, $user_id,$lktlog);
        //2.区分购物车结算和立即购买---列出选购商品
        $sec_id = addslashes(Request::param('fs_id'));

        $products = Tools::products_list($store_id,$cart_id, $product, $product_type);
        
        //3.列出商品数组-计算总价和优惠，运费

        $products_data =Tools::get_products_data($store_id, $products, $products_total,'FS');
       
        $product_id = $products_data['product_id'];
        $product_class = $products_data['product_class'];
        $products_freight = $products_data['products_freight'];
        $products = $products_data['products'];
        $products_total = $products_data['products_total'];

        $no_delivery_str = '';
        if($address_id == '')
        { // 获取不配送省的名称
            $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);
        }

        //查询默认地址order_details
        $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);
        $addemt = $address ? 1 : 0; // 收货地址状态
        
        //查询出折扣价格
        $sql_sec = "select a.*,b.mch_id,b.starttime,b.endtime from lkt_flashsale_activity as a left join lkt_flashsale_label as b on a.label_id = b.id where a.store_id = '$store_id' and a.id = '$sec_id' and a.is_delete = 0 and a.status = 2 and b.status = 2 ";
        $price_res = Db::query($sql_sec);
        $discount = 10;          
        if (!empty($price_res))
        {
            $pro_arr = explode(',', $product);
            $num = $pro_arr[2];
            //获取价格
            $configure = ConfigureModel::where(['id'=>$pro_arr[1],'pid'=>$pro_arr[0]])->field('price')->select()->toArray();
            $price = $configure[0]['price'];//售价

            $discount = $price_res[0]['discount'];//限时折扣
            $fsPrice = round($discount * $price * 0.1,2);//限时折扣价格
            $products_total = round($fsPrice * $num,2);//商品总价
            $mch_id = $price_res[0]['mch_id'];
            
            $preferential_amount = 0;   //折扣优惠金额 计算规则
           
            $remainingTime = strtotime($price_res[0]['endtime'])* 1000;
            //判断购买条件
            $buy_num = $price_res[0]['buylimit'];//购买上限
            //查询用户已经购买数量
            $buyNum = 0;
            $sql_num = "select * from lkt_flashsale_record where store_id = '$store_id' and user_id = '$user_id' and is_delete = 0 and activity_id = '$sec_id' and pro_id = ".$pro_arr[0]." and attr_id = " . $pro_arr[1];
            $res_num = Db::query($sql_num);
            if($res_num)
            {
                $buyNum = count($res_num);
            }
            if(($buyNum + $num) > $buy_num)
            {
                $message = Lang('sec.10');
                echo json_encode(array('code' => ERROR_CODE_CSYC, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }    
        }
        else
        {   
            $message = Lang('sec.9');
            echo json_encode(array('code' => ERROR_CODE_CSYC, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }

        $is_distribution = $products[0]['list'][0]['is_distribution'];

        //4.计算运费
        $freight = Tools::get_freight($products_freight, $products, $address, $store_id, $product_type);
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
        $products = $freight['products'];
        // $products[0]['list'][0]['attrPrice'] = floatval($price);
        $products[0]['list'][0]['price'] = sprintf("%.2f",$discount * $price * 0.1);

        // $products[0]['list'][0]['fsPrice'] = round($discount * $price * 0.1,2);
        // $products[0]['list'][0]['fs_id'] = intval($sec_id);
        $products[0]['list'][0]['store_id'] = intval($store_id);
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
        //加购
        $add_amount = 0;//加购商品金额
        if($add_good)
        {
            $addProducts = json_decode($add_good,true);
            foreach ($addProducts as $key => $value) 
            {
                $jid = $value['id'];
                $jnum = $value['num'];
                $res_a = FlashsaleAddgoodsModel::where(['store_id'=>$store_id,'id'=>$jid,'is_delete'=>0])->select()->toArray();
                if($res_a)
                {
                    $add_amount += floatval($res_a[0]['price']) * $jnum;
                }
            }
        }
        $grade_rate = 1;

        $total = $products_total * $grade_rate + $yunfei - $preferential_amount + $add_amount; // 商品总价 - 自动满减 + 运费 + 加购金额    实际付款金额
        $total = round($total, 2);
        $grade_rate_amount = round($products_total * (1- $grade_rate), 2);
        $products_total = $products_total + $add_amount;//插件商品总价+加购商品金额
        //5.返回数据
        $data = array('grade_rate_amount'=>$grade_rate_amount,'payment' => $payment, 'products' => $products, 'is_distribution' => $is_distribution, 'discount' => floatval($discount), 'products_total' => $products_total, 'freight' => $yunfei, 'total' => $total, 'user_money' => $user_money, 'address' => $address, 'addemt' => $addemt, 'password_status' => $password_status, 'enterless' => $enterless, 'shop_status' => $shop_status, 'shop_list' => $shop_list, 'grade_rate' => $grade_rate * 10,'remainingTime'=>$remainingTime,'preferential_amount'=>$preferential_amount,'add_amount'=>$add_amount);
        $message = Lang('Success');
        echo json_encode(array('code'=>200,'message'=>$message,'data'=>$data));
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

        $product1 = Request::param('product');//  商品数组--------['pid'=>66,'cid'=>88]
        $product = '';
        $lktlog = new LaiKeLogUtils();
        $products_total = 0;
        $type = trim(Request::param('type')) ? Request::param('type') : 'FS'; // 订单类型
        $cart_id = addslashes(trim(Request::param('cart_id')));  //购物车id-- 12,13,123,
        $grade_l = addslashes(Request::param('grade_l'));//会员特惠 兑换券级别
        $coupon_id = trim(Request::param('coupon_id')); // 优惠券id
        $allow = trim(Request::param('allow'))?trim(Request::param('allow')):0; // 用户使用积分
        $address_id = Request::param('address_id'); //  地址id
        $shop_address_id = Request::param('shop_address_id')?trim(Request::param('shop_address_id')):0; //  门店地址id
        $remarks = trim(Request::param('remarks')); //  订单备注
        $store_type = addslashes(trim(Request::param('store_type')));
        $pay_type = addslashes(trim(Request::param('pay_type')));//
        $sec_id = addslashes(trim(Request::param('fs_id')));//
        $add_good = addslashes(Request::param('add_good'));//  加购商品 --------12,123

        $remarks = htmlspecialchars_decode($remarks); // 将特殊的 HTML 实体转换回普通字符
        $remarks = json_decode($remarks, true);

        $add_good = htmlspecialchars_decode($add_good);
        $add_good = stripslashes(html_entity_decode($add_good)); // 字符串打散为数组
        if(empty($address_id))
        {
            $message = Lang('nomal_order.1');
            echo json_encode(array('code' => ERROR_CODE_QXZSHDZ, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }
        $products_total = 0;
        $after_discount = 0;
        $total = 0.00;
        $yunfei = 0;
        $add_amount = 0;//加购商品金额
        $preferential_amount = 0;//销售策略规划优惠金额

        $product = '';
        list($product, $shop_list, $cart_id) = Tools::productHandle($product1, $cart_id);
        // 3.区分购物车结算和立即购买---列出选购商品

        $products = Tools::products_list($store_id,$cart_id, $product, $type);

        // 5.列出商品数组-计算总价和优惠，拿商品运费ID
        $products_data = Tools::get_products_data($store_id, $products, $products_total, $type);
        // 存储信息
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

        // 6.计算运费
        $freight = Tools::get_freight($products_freight, $products, $address,$store_id, $type);
        // 存储运费数据
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
        $products = $freight['products'];
        // 因为秒杀商品只有一个商品
        $sec_id = addslashes(trim(Request::param('fs_id')));
        //如果为0元订单，则订单状态为 1-已发货
        if ($grade_l && ($yunfei == 0))
        {
            $order_status = 1;
        }
        else
        {
            $order_status = 0;
        }
        // 定义初始化数据
        $z_num = 0;
        $z_num_r = 0;
        $discount = 10;
        $remarks_0 = array();
        //获取商品id
        $pro_id = $product_id[0];
        
        $time = date("Y-m-d H:i:s");
        //查询出折扣价格
        $sql_sec = "select a.*,b.mch_id,b.starttime,b.endtime from lkt_flashsale_activity as a left join lkt_flashsale_label as b on a.label_id = b.id where a.store_id = '$store_id' and a.id = '$sec_id' and a.is_delete = 0 and a.status = 2";
        $price_res = Db::query($sql_sec);
        if (!empty($price_res))
        {
            $endtime = $price_res[0]['endtime'];
            if($endtime <= $time)
            {
                $message = Lang('flashsale.1');
                echo json_encode(array('code' => ERROR_CODE_CSYC, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            $pro_arr = explode(',', $product);
            $num = $pro_arr[2];
            //获取价格
            $configure = ConfigureModel::where(['id'=>$pro_arr[1],'pid'=>$pro_arr[0]])->field('price')->select()->toArray();
            $price = $configure[0]['price'];//售价

            $discount = $price_res[0]['discount'];//限时折扣
            $fsPrice = round($discount * $price * 0.1,2);//限时折扣价格
            $products_total = round($fsPrice * $num,2);//限时折扣购买的商品总价
            
            $mch_id = $price_res[0]['mch_id'];
            
            $preferential_amount = 0;   //销售策略优惠金额 计算规则
            
            $remainingTime = strtotime($price_res[0]['endtime'])* 1000;
            //判断购买条件
            $buy_num = $price_res[0]['buylimit'];//购买上限
            //查询用户已经购买数量
            $buyNum = 0;
            $sql_num = "select * from lkt_flashsale_record where store_id = '$store_id' and user_id = '$user_id' and is_delete = 0 and activity_id = '$sec_id' and pro_id = ".$pro_arr[0]." and attr_id = " . $pro_arr[1];
            $res_num = Db::query($sql_num);
            if($res_num)
            {
                $buyNum = count($res_num);
            }
            if(($buyNum + $num) > $buy_num)
            {
                $message = Lang('sec.10');
                echo json_encode(array('code' => ERROR_CODE_CSYC, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }    
        }
        else
        {   
            $message = Lang('Parameter error');
            echo json_encode(array('code' => ERROR_CODE_CSYC, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }

        // 店铺
        $mch = new MchPublicMethod();
        if ($shop_address_id)
        {
            $shop = $mch->Settlement($store_id, $products, 'payment');
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
        else
        {
            $shop_status = 0;
            $extraction_code = '';
            $extraction_code_img = '';
        }
        Db::startTrans();
        // 判断是否使用优惠价 0 未使用，1使用
        // 优惠券--插件
        $order_status = 0;
        $give_id = 0; // 赠品ID
        $reduce_name_array = ''; // 满减名称
        $coupon_money = 0;
        $coupon_status = 0;
        $coupon_id_list = array();
        $coupon_id0 = 0;
        $subtraction_id = 0;
        $reduce_money = 0;
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

        $Toosl = new Tools(1,1);
        $sNo = $Toosl->Generate_order_number($type, 'sNo'); // 生成订单号
        $real_sno = $Toosl->Generate_order_number($type, 'real_sno'); // 生成支付订单号
        $remarks_status = false;
        $data = array();
        $mch_id = '';
        //循环订单商品
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
            $sql = $mch->addMchBrowse($mch_id, $store_id, $mch_id1, $user_id,$lktlog);

            //循环商品数据
            foreach ($v['list'] as $key => $value)
            {   
                $pdata = (object)$value;
                $pid = $value['pid'];
                $cid = $value['cid'];
                $num = $value['num'];
                //查询商品金额
                $product_title = addslashes($value['product_title']);
                $freight_price = floor(isset($value['freight_price']) ? $value['freight_price'] : 0);
                $sql_d = new OrderDetailsModel();
                $sql_d->store_id = $store_id;
                $sql_d->user_id = $user_id;
                $sql_d->p_id = $pdata->pid;
                $sql_d->p_name = $product_title;
                $sql_d->p_price = $fsPrice;
                $sql_d->num = $pdata->num;
                $sql_d->unit = $pdata->unit;
                $sql_d->r_sNo = $sNo;
                $sql_d->add_time = date("Y-m-d H:i:s");
                $sql_d->r_status = $order_status;
                $sql_d->size = $pdata->size;
                $sql_d->sid = $pdata->cid;
                $sql_d->freight = $freight_price;
                $sql_d->after_discount = $products_total;
                $sql_d->save();
                $beres = $sql_d->id;
                // 如果添加失败
                if ($beres < 1)
                {
                    $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "添加订单详情失败！sql:" . $sql_d);
                    // 回滚事件，给提示
                    Db::rollback();
                    ob_clean();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }
                // 根据商品ID、属性ID，查询属性数据
                $r0 = ConfigureModel::where(['id'=>$cid,'pid'=>$pid])->field('num,min_inventory')->select()->toArray();
                $total_num = $r0[0]['num']; // 剩余库存
                $min_inventory = $r0[0]['min_inventory']; // 预警值

                // 根据商品ID，修改商品库存
                $res_del1 = Db::name('product_list')->where('id',$pid)->update(['num' =>  Db::raw('num - '.$num)]);
                if ($res_del1 <= 0)
                {
                    $this->Log(__METHOD__ . ":" . __LINE__ . "修改商品库存失败！pid:" . $pid);
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }

                // 根据属性ID，修改属性库存
                $res_del2 = Db::name('configure')->where('id',$cid)->update(['num' =>  Db::raw('num - '.$num)]);
                if ($res_del2 <= 0)
                {
                    $this->Log(__METHOD__ . ":" . __LINE__ . "修改商品属性库存失败！cid:" . $cid);
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }
                
                $content = $user_id . '生成限时折扣订单所需' . $num;
                $sql_stock_insert = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>1,'user_id'=>$user_id,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content);
                Db::name('stock')->insert($sql_stock_insert);

                if($total_num - $num <= $min_inventory)
                {   
                    $content1 = '预警';
                    $sql_stock_insert1 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>$min_inventory,'type'=>2,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content1);
                    Db::name('stock')->insert($sql_stock_insert1);

                    $message_9 = "商品ID为".$pid."的商品库存不足，请尽快补充库存";
                    $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>$mch_id1,'gongyingshang'=>0,'type'=>9,'parameter'=>$cid,'content'=>$message_9);
                    PC_Tools::add_message_logging($message_logging_list9);
                }

                $z_num += $pdata->num;
                $z_num_r += $pdata->num;
            }
            //加购商品
            if($add_good)
            {
                $addProducts = json_decode($add_good,true);
                foreach ($addProducts as $key => $value) 
                {
                    $jid = $value['id'];
                    $num = $value['num'];
                    $res_a = FlashsaleAddgoodsModel::where(['store_id'=>$store_id,'id'=>$jid,'is_delete'=>0])->select()->toArray();
                    if($res_a)
                    {
                        $addPrice = floatval($res_a[0]['price']);
                        $addPrice1 = floatval($res_a[0]['price']) * $num;
                        $add_amount += floatval($res_a[0]['price']) * $num;

                        $mch_id1 = $res_a[0]['mch_id'];
                        $p_id = $value;
                        $pid = $res_a[0]['goods_id'];
                        $cid = $res_a[0]['attr_id'];
                        $size = '';
                        //查询商品金额
                        $product_title = Db::name('product_list')->where('id', $res_a[0]['goods_id'])->value('product_title');
                        $res_c = ConfigureModel::where(['id'=>$cid,'recycle'=>0])->field('attribute,pid as goodsId,id,num,price,unit')->select()->toArray();
                        if($res_c)
                        {
                            $attribute = unserialize($res_c[0]['attribute']);                           
                            foreach ($attribute as $ka => $va)
                            {
                                if (strpos($ka, '_LKT_') !== false)
                                {
                                    $ke = substr($ka, 0, strrpos($ka, "_LKT"));
                                    $va = substr($va, 0, strrpos($va, "_LKT"));
                                    $size .= $ke . ":" . $va . ";";
                                }
                                else
                                {
                                    $size .= $ka . ":" . $va . ";";
                                }
                            }
                        }
                        $sql_d = new OrderDetailsModel();
                        $sql_d->store_id = $store_id;
                        $sql_d->user_id = $user_id;
                        $sql_d->p_id = $pid;
                        $sql_d->p_name = $product_title;
                        $sql_d->p_price = $addPrice;
                        $sql_d->num = $num;
                        $sql_d->unit = $res_c[0]['unit'];
                        $sql_d->r_sNo = $sNo;
                        $sql_d->add_time = date("Y-m-d H:i:s");
                        $sql_d->r_status = $order_status;
                        $sql_d->size = $size;
                        $sql_d->sid = $cid;
                        $sql_d->freight = 0;
                        $sql_d->after_discount = $addPrice1;
                        $sql_d->is_addp = 1;//加购商品
                        $sql_d->save();
                        $beres = $sql_d->id;
                        // 如果添加失败
                        if ($beres < 1)
                        {
                            $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "添加订单详情失败！sql:" . $sql_d);
                            // 回滚事件，给提示
                            Db::rollback();
                            ob_clean();
                            $message = Lang('nomal_order.3');
                            echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                            exit;
                        }
                        // 根据商品ID、属性ID，查询属性数据
                        $r0 = ConfigureModel::where(['id'=>$cid,'pid'=>$pid])->field('num,min_inventory')->select()->toArray();
                        $total_num = $r0[0]['num']; // 剩余库存
                        $min_inventory = $r0[0]['min_inventory']; // 预警值

                        // 根据商品ID，修改商品库存
                        $res_del1 = Db::name('product_list')->where('id',$pid)->update(['num' =>  Db::raw('num - '.$num)]);
                        if ($res_del1 <= 0)
                        {
                            $this->Log(__METHOD__ . ":" . __LINE__ . "修改商品库存失败！pid:" . $pid);
                            Db::rollback();
                            $message = Lang('nomal_order.3');
                            echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                            exit;
                        }

                        // 根据属性ID，修改属性库存
                        $res_del2 = Db::name('configure')->where('id',$cid)->update(['num' =>  Db::raw('num - '.$num)]);
                        if ($res_del2 <= 0)
                        {
                            $this->Log(__METHOD__ . ":" . __LINE__ . "修改商品属性库存失败！cid:" . $cid);
                            Db::rollback();
                            $message = Lang('nomal_order.3');
                            echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                            exit;
                        }
                        
                        $content = $user_id . '生成限时折扣订单所需' . $num;
                        $sql_stock_insert = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>1,'user_id'=>$user_id,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content);
                        Db::name('stock')->insert($sql_stock_insert);

                        if($total_num - $num <= $min_inventory)
                        {   
                            $content1 = '预警';
                            $sql_stock_insert1 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>$min_inventory,'type'=>2,'add_date'=>date("Y-m-d H:i:s"),'content'=>$content1);
                            Db::name('stock')->insert($sql_stock_insert1);

                            $message_9 = "商品ID为".$pid."的商品库存不足，请尽快补充库存";
                            $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>$mch_id1,'gongyingshang'=>0,'type'=>9,'parameter'=>$cid,'content'=>$message_9);
                            PC_Tools::add_message_logging($message_logging_list9);
                        }
                        $z_num += $num;
                    }
                }
            }
        }
        $mch_id = rtrim($mch_id, ',');
        $mch_id = ',' . $mch_id . ',';
        $bargain_order_no = '';
        $bargain_id = 0;
        $grade_rate = 1;

        // $total = ($products_total * $grade_rate) + $yunfei; // 商品总价 * 分销折扣 - 自动满减 + 运费 - 优惠券金额     实际付款金额
        $total = $products_total * $grade_rate + $yunfei - $preferential_amount + $add_amount; // 商品总价 - 自动满减 + 运费 + 加购金额    实际付款金额
        $total = round($total, 2);
        $products_total = $products_total + $add_amount;//插件商品总价+加购商品金额
        if ($total <= 0)
        {
            $total = 0.01;
        }
        if($remarks_status)
        {
            $remarks = serialize($remarks_0);
        }
        else
        {
            $remarks = '';
        }

        $otype = "FS";

        $order_failure_time = Tools::Obtain_expiration_time(array('store_id'=>$store_id,'otype'=>$otype));

        $sql_o = new OrderModel();
        $sql_o->store_id = $store_id;
        $sql_o->user_id = $user_id;
        $sql_o->name = $name;
        $sql_o->cpc = $cpc;
        $sql_o->mobile = $mobile;
        $sql_o->num = $z_num;
        $sql_o->old_total = $total;
        $sql_o->z_price = $total;
        $sql_o->sNo = $sNo;
        $sql_o->shop_cpc = $shop_cpc;
        $sql_o->sheng = $sheng;
        $sql_o->shi = $shi;
        $sql_o->xian = $xian;
        $sql_o->address = $address_xq;
        $sql_o->code = $code;
        $sql_o->remark = ' ';
        $sql_o->pay = $pay_type;
        $sql_o->add_time = date("Y-m-d H:i:s");
        $sql_o->order_failure_time = $order_failure_time;
        $sql_o->status = $order_status;
        $sql_o->subtraction_id = $subtraction_id;
        $sql_o->consumer_money = $allow;
        $sql_o->coupon_activity_name = $reduce_name_array;
        $sql_o->spz_price = $products_total;
        $sql_o->reduce_price = $reduce_money;
        $sql_o->coupon_price = $coupon_money;
        $sql_o->source = $store_type;
        $sql_o->otype = $otype;
        $sql_o->mch_id = $mch_id;
        $sql_o->p_sNo = '';
        $sql_o->bargain_id = 0;
        $sql_o->comm_discount = 0;
        $sql_o->remarks = $remarks;
        $sql_o->real_sno = $real_sno;
        $sql_o->self_lifting = $shop_status;
        $sql_o->extraction_code = $extraction_code;
        $sql_o->extraction_code_img = $extraction_code_img;
        $sql_o->grade_rate = $grade_rate;
        $sql_o->old_freight = $yunfei;
        $sql_o->z_freight = $yunfei;
        $sql_o->preferential_amount = $preferential_amount;
        $sql_o->single_store = $shop_address_id;
        $sql_o->save();
        $r_o = $sql_o->id;

        if ($r_o > 0)
        {
            //添加秒杀记录
            $istmssql = new FlashsaleRecordModel();
            $istmssql->store_id = $store_id;
            $istmssql->user_id = $user_id;
            $istmssql->activity_id = $sec_id;//秒杀活动id
            $istmssql->sec_id = $sec_id;//秒杀活动id
            $istmssql->time_id = 0;
            $istmssql->pro_id = $pro_arr[0];//商品id
            $istmssql->attr_id = $pro_arr[1];//规格id
            $istmssql->price = $products_total;
            $istmssql->num = $z_num_r;
            $istmssql->is_delete = 0;
            $istmssql->sNo = $sNo;
            $istmssql->add_time = date('Y-m-d H:i:s');
            $istmssql->save();
            $record_res = $istmssql->id;
            if ($record_res < 1)
            {
                $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "添加限时折扣记录失败！product:" . $product1);
            }

            // //减少活动库存
            // $sql_pro = FlashsaleProModel::where(['activity_id'=>$sec_id,'attr_id'=>$pro_arr[1],'is_delete'=>0,'store_id'=>$store_id])->find();
            // $sql_pro->num = Db::raw('num - '.$z_num);
            // $res_del1 = $sql_pro->save();
            // if (!$res_del1)
            // {
            //     $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改限时折扣商品库存失败！product:" . $product1);
            // }
            
            // $sql_a = FlashsaleActivityModel::where(['store_id'=>$store_id,'id'=>$sec_id,'is_delete'=>0,'isshow'=>1])->find();
            // $sql_a->num = Db::raw('num - '.$z_num);
            // $res_a = $sql_a->save();
            // if (!$res_a)
            // {
            //     $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改限时折扣商品库存失败！product:" . $product1);
            // }

            // $r0 = ConfigureModel::where(['id'=>$pro_arr[1],'pid'=>$pro_arr[0]])->field('num')->select()->toArray();
            // $total_num = $r0[0]['num'];

            // // 生成库存记录
            // $content = $user_id . '生成限时折扣订单所需' . $num;
            // $StockModel = new StockModel();
            // $StockModel->store_id = $store_id;
            // $StockModel->product_id = $pro_arr[0];
            // $StockModel->attribute_id = $pro_arr[1];
            // $StockModel->total_num = $total_num;
            // $StockModel->flowing_num = $num;
            // $StockModel->type = 1;
            // $StockModel->user_id = $user_id;
            // $StockModel->add_date = date("Y-m-d H:i:s");
            // $StockModel->content = $content;
            // $StockModel->save();

            //返回
            Db::commit();
            $arr = array('sNo' => $sNo, 'total' => sprintf("%.2f",$total), 'order_id' => $r_o,'order_type'=>'FS','orderTime'=>date("Y-m-d H:i:s"));
            ob_clean();
            $message = Lang("Success");
            echo json_encode(array('code' => 200, 'data' => $arr,'message'=>$message));
            exit;
        }
        else
        {
            $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "添加限时折扣订单失败！product:" . $product1);
            //回滚删除已经创建的订单
            Db::rollback();
            ob_clean();
            $message = Lang('nomal_order.6');
            echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }
    }

    public static function array_key_remove($arr, $key)
    {
        if (!array_key_exists($key, $arr))
        {
            return $arr;
        }
        $keys = array_keys($arr);
        $index = array_search($key, $keys);
        if ($index !== false)
        {
            array_splice($arr, $index, 1);
        }
        return $arr;
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
		$log->log_result($log_name, __LINE__ . "查询限时折扣订单：" . $sql );
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
		$log->log_result($log_name, __LINE__ . "查询支付参数：" . json_encode($config) );
        if (empty($config))
        {
            LaiKeLogUtils::log('common/zfjc.log', 'file');
            $log->log_result($log_name, "普通订单执行日期：" . date('Y-m-d H:i:s') . "\n支付暂未配置 商城ID：$store_id ，支付类型：$pay_type ，无法调起支付！\r\n");
            return 'file';
        }

        LaiKeLogUtils::log('common/zfjc.log', '==><==' . json_encode($config));
        $log->log_result($log_name,  __LINE__ . " oever ");
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
        if (empty($trade_no) || $total <= 0)
        {
            $log->log_result($log_name,__LINE__ .  "普通订单回调失败信息: \n 订单：$trade_no 支付金额：$total \r\n");
            ob_clean();
            echo 'error';
            exit;
        }
        
        $type = substr($trade_no, 0, 2);
        $log->log_result($log_name,__LINE__ .  "【trade_no1】''''$trade_no:\n\n");
        $sql = "select sNo,z_price from lkt_order where real_sno='$trade_no'";
        $r = Db::query($sql);
        $trade_no = $r[0]['sNo'];
        $type = substr($trade_no, 0, 2);
        $z_price = $r[0]['z_price'];
        // if (floatval($z_price) != floatval($total))
        // {
        //     Db::execute("update lkt_order set z_price='$total' where real_sno='$trade_no'");
        //     $log->log_result($log_name, "【普通订单付款金额有误】:\n 应付金额为$z_price \n");
        // }
        $sql = "select * from lkt_order where sNo='$trade_no'";
        $r = Db::query($sql);
        $log->log_result($log_name,__LINE__ .  "【sql】''''$sql:\n\n");
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
        $this->seckill_status($store_id);//自动开启 、 结束活动

        // $this->order_failure($store_id);// 删除过期订单
        $this->ok_Order($store_id);//自动收货
        $this->auto_good_comment($store_id);//自动评价
        $this->order_settlement($store_id);//商家订单结算
    }

    //自动开启 、 结束活动
    public function seckill_status($store_id)
    {   
        $lktlog = new LaiKeLogUtils();
        $time = date('Y-m-d H:i:s');

        $sql0 = "select * from lkt_flashsale_label where store_id = '$store_id' and recovery = 0 and status != 3 ";
        $r0 = Db::query($sql0);
        if($r0)
        {   
            foreach ($r0 as $k0 => $v0)
            {   
                Db::startTrans();
                $id = $v0['id'];
                $starttime = $v0['starttime'];
                $endtime = $v0['endtime'];
                $sql1 = FlashsaleLabelModel::find($id);
                if($endtime <= $time)
                { // 已结束
                    $sql1->status = 3;
                }
                else if($starttime <= $time && $endtime > $time)
                { // 进行中
                    $sql1->status = 2;
                }
                else
                { // 未开始
                    $sql1->status = 1;
                }
                $res1 = $sql1->save();
                if(!$res1)
                {
                    Db::rollback();
                    $lktlog->log("common/FlashSalePublic.log",__METHOD__ . ":" . __LINE__ . "修改活动状态失败！id:" . $id);
                }
                Db::commit();
            }
            
        }

        $sql1 = "select * from lkt_flashsale_activity where store_id = '$store_id' and is_delete = 0 and status != 3 ";
        $r1 = Db::query($sql1);
        if($r1)
        {   
            foreach ($r1 as $k1 => $v1)
            {   
                Db::startTrans();
                $id1 = $v1['id'];
                $starttime1 = $v1['starttime'];
                $endtime1 = $v1['endtime'];
                if($endtime <= $time)
                { // 已结束
                    $status = 3;
                }
                else if($starttime <= $time && $endtime > $time)
                { // 进行中
                    $status = 2;
                }
                else
                { // 未开始
                    $status = 1;
                }

                $sql2 = "update lkt_flashsale_activity set status = '$status' where id = '$id1' ";
                $r2 = Db::execute($sql2);
                if($r2 == -1)
                {
                    Db::rollback();
                    $lktlog->log("common/FlashSalePublic.log",__METHOD__ . ":" . __LINE__ . "修改活动商品状态失败！id:" . $id1);
                }
                Db::commit();
            }
        }
        return;
    }

    //删除过期订单
    public function order_failure($store_id)
    {   
        $lktlog = new LaiKeLogUtils();
        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'FS' and o.store_id = '$store_id' and o.status = 0 and o.recycle = 0";
        $res_m = Db::query($sql_m);
        if($res_m)
        {
            foreach ($res_m as $key => $value) 
            {
                $mch_id = $value['mch_id'];
                //获取店铺积分商城配置
                $res_c = FlashsaleConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('order_failure')->select()->toArray();
                $order_failure = $res_c ? $res_c[0]['order_failure'] : 3600; // 未付款订单保留时间
                if($order_failure != 0)
                {
                    $time01 = date("Y-m-d H:i:s", strtotime("-$order_failure seconds")); // 订单过期删除时间

                    // 根据用户id，订单为未付款，订单添加时间 小于 未付款订单保留时间,查询订单表
                    $r0 = OrderModel::where(['store_id'=>$store_id,'status'=>0])->where('add_time','<',$time01)->where('mch_id',','.$mch_id.',')->whereIn('otype','FS')->select()->toArray();
                    if ($r0)
                    { // 有数据，循环查询优惠券id,修改优惠券状态
                        foreach ($r0 as $k => $v)
                        {
                            $coupon_id0 = $v['coupon_id'];  // 优惠券id
                            $user_id = $v['user_id']; // user_id
                            $sNo = $v['sNo']; // 订单号
                            $psNo = $v['p_sNo']; // 父订单号
                            $otype = $v['otype'];//订单类型

                            $sql_where_o = array('store_id'=>$store_id,'status'=>0,'sNo'=>$sNo);
                            $r_o = Db::name('order')->where($sql_where_o)->update(['status'=>'7']);
                            $lktlog->log("common/SecondsPublic.log","\r\n订单更新为关闭状态！参数：" . json_encode($sql_where_o) . "\r\n");

                            // 根据用户id、订单未付款、添加时间小于前天时间,就删除订单详情信息
                            // $r1 = OrderDetailsModel::where(['store_id'=>$store_id,'r_status'=>0,'r_sNo'=>$sNo])->where('add_time','<',$time01)->select()->toArray();
                            $sql1 = "select d.*,p.id as pro_id from lkt_order_details as d left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where d.r_sNo = '$sNo' and d.r_status = 0 and d.store_id = '$store_id' and d.add_time < '$time01'";
                            $r1 = Db::query($sql1);
                            if ($r1)
                            {
                                foreach ($r1 as $k1 => $v1)
                                {   
                                    //删除订单详情信息
                                    $sql_where2 = array('store_id'=>$store_id,'id'=>$v1['id']);
                                    $r2 = Db::name('order_details')->where($sql_where2)->update(['r_status'=>'7']);

                                    $ms_record_res = FlashsaleRecordModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->select()->toArray();
                                    $activity_id = $ms_record_res[0]['activity_id'];

                                    // 移除秒杀订单  删除秒杀记录
                                    $up_ms_record = FlashsaleRecordModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->find();
                                    $up_ms_record->is_delete = 1;
                                    $up_ms_record->save();
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    //自动收货
    public function ok_Order($store_id)
    {   
        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'FS' and o.store_id = '$store_id' and o.status = 2 and o.recycle = 0";
        $res_m = Db::query($sql_m);
        if($res_m)
        {
            Db::startTrans();
            foreach ($res_m as $ke => $v) 
            {
                $mch_id = $v['mch_id'];
                //获取店铺积分商城配置
                $res_c = FlashsaleConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('auto_the_goods')->select()->toArray();
                $auto_the_goods = $res_c ? $res_c[0]['auto_the_goods']/(3600*24) : 7; // 自动收货时间

                $code = true;
                $time = date('Y-m-d H:i:s');
                $sql01 = "select d.id,d.r_sNo,d.deliver_time,d.user_id,d.store_id,d.p_id,d.p_price,d.num,o.z_price 
                from lkt_order_details as d left join lkt_order as o on d.r_sNo = o.sNo
                where d.store_id = '$store_id' and d.r_status = '2' and o.otype = 'FS' and o.mch_id = ',".$mch_id.",' and date_add(d.deliver_time, interval $auto_the_goods day) < now()";
                $rew = Db::query($sql01);
                if (!empty($rew))
                {
                    $ordersNo = array();
                    foreach ($rew as $key => $value)
                    {
                        $user_id = $value['user_id'];
                        $sNo = $value['r_sNo'];
                        $z_price = $value['z_price'];
                        if (!in_array($sNo, $ordersNo))
                        {
                            array_push($ordersNo, $sNo);
                        }

                        $sql_1 = array('store_id'=>$store_id,'r_sNo'=>$sNo,'r_status'=>2);
                        $r_1 = Db::name('order_details')->where($sql_1)->update(['r_status'=>'5','arrive_time'=>$time]);
                        if ($r_1 < 0)
                        {
                            $code = false;
                            break;
                        }

                        $sql_2 = array('store_id'=>$store_id,'sNo'=>$sNo);
                        $r_2 = Db::name('order')->where($sql_2)->update(['status'=>'5']);
                        if ($r_2 < 0)
                        {
                            $code = false;
                            break;
                        }

                        $message_6 = "订单".$sNo."，用户已确定收货！";
                        $message_logging_list6 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>6,'parameter'=>$sNo,'content'=>$message_6);
                        PC_Tools::add_message_logging($message_logging_list6);

                        if($z_price > 0)
                        {
                            $mch = new MchPublicMethod();
                            $mch->parameter($store_id, $sNo, $z_price, 0);
                        }
                    }
                }
                if (!$code)
                { // 如果批量执行没出错则提交，否则就回滚
                    Db::rollback();
                }
            }
            Db::commit();
        }   
    }

    //自动评价
    public function auto_good_comment($store_id)
    {   
        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'FS' and o.store_id = '$store_id' and o.status = 5 and o.recycle = 0";
        $res_m = Db::query($sql_m);
        if($res_m)
        {
            foreach ($res_m as $key => $value) 
            {
                $mch_id = $value['mch_id'];
                //获取店铺积分商城配置
                $res_c = FlashsaleConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('auto_good_comment_day,auto_good_comment_content')->select()->toArray();
                $auto_good_comment_day = $res_c ? $res_c[0]['auto_good_comment_day']/(3600*24) : 0; // 未付款订单保留时间
                $auto_good_comment_content = $res_c ? $res_c[0]['auto_good_comment_content']:''; // 自动好评内容
                $add_time = date('Y-m-d H:i:s');
                if ($auto_good_comment_day > 0)
                { // 设置了天数自动好评
                    // 查询所有没有评论的订单商品
                    $time = date("Y-m-d H:i:s",strtotime("-5 minutes"));
                    $sql = "SELECT
                                a.id,a.r_sNo,a.user_id,a.p_id,a.sid 
                            FROM
                                lkt_order_details a
                                left join lkt_order o on a.r_sNo = o.sNo
                                LEFT JOIN lkt_comments b 
                                ON  a.user_id = b.uid 
                                AND a.id = b.order_detail_id 
                                AND b.uid IS NULL
                            WHERE
                                a.r_status = 5 
                                AND a.store_id = $store_id 
                                AND a.r_sNo like 'FS%' 
                                AND o.mch_id = ',".$mch_id.",'
                                AND date_add(a.arrive_time, interval $auto_good_comment_day day) <= now()
                                AND date_add(a.arrive_time, interval $auto_good_comment_day day) > '$time'
                                ";
                    $no_comment_arr = Db::query($sql);
                    // 创建好评
                    if (!empty($no_comment_arr))
                    {
                        foreach ($no_comment_arr as $k => $nv)
                        {
                            $sNo = $nv['r_sNo'];
                            $user_id = $nv['user_id'];
                            // $goodsId = $nv['p_id'];
                            $order_detail_id = $nv['id'];
                            $attribute_id = $nv['sid'];

                            $pro = ConfigureModel::where(['id'=>$attribute_id])->field('pid')->select()->toArray();
                            $goodsId = $pro[0]['pid'];
                            
                            $comment = $auto_good_comment_content?$auto_good_comment_content:Lang('comments.0');
                            $start = 5;
                            $anonymous = 0;

                            $sql_d = array('store_id'=>$store_id,'oid'=>$sNo,'uid'=>$user_id,'pid'=>$goodsId,'attribute_id'=>$attribute_id,'content'=>$comment,'CommentType'=>$start,'anonymous'=>$anonymous,'add_time'=>$add_time,'order_detail_id'=>$order_detail_id);
                            Db::name('comments')->insert($sql_d);
                        }
                    }
                }

            }
        }   
    }

    //商家订单结算
    public function order_settlement($store_id)
    {   
        $lktlog = new LaiKeLogUtils();
        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'FS' and o.store_id = '$store_id' and o.status = 5 and o.recycle = 0";
        $res_m = Db::query($sql_m);
        if($res_m)
        {   
            Db::startTrans();
            foreach ($res_m as $k => $v) 
            {
                $mch_id = $v['mch_id'];
                //获取店铺积分商城配置
                $res_c = FlashsaleConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('order_after')->select()->toArray();
                $order_after = $res_c ? $res_c[0]['order_after']/(3600*24) : 0; // 订单售后时间 (单位秒)

                $time = date("Y-m-d H:i:s",strtotime("-$order_after day"));
        
                $sql_m = "select a.after_discount,a.freight,a.r_sNo,b.mch_id,a.id from lkt_order_details as a left join lkt_order as b on a.r_sNo = b.sNo where b.store_id = '$store_id' and a.r_status in (3,5) and a.arrive_time <= '$time' and a.settlement_type = 0 and b.otype = 'FS' and b.mch_id = ',".$mch_id.",' ";
                $res_m = Db::query($sql_m);
                if($res_m)
                {
                    foreach ($res_m as $key => $value) 
                    {   
                        $res = ReturnOrderModel::where(['p_id'=>$value['id']])->whereIn('r_type','0,1,3')->where('re_type','<>',3)->field('id')->select()->toArray();
                        if(empty($res))
                        {
                            if($value['after_discount'] > 0)
                            {
                                $sNo = $value['r_sNo'];
                                $after_discount = $value['after_discount'];
                                $freight = $value['freight'];
                                $mch_id = substr($value['mch_id'], 1, -1);
                                $money = round($after_discount+$freight,2);

                                if($money > 0)
                                {   
                                    $sql_u_where = array('store_id'=>$store_id,'id'=>$mch_id);
                                    $sql_u_update = array('cashable_money'=>Db::raw('cashable_money+'.$money));
                                    $res_u = Db::name('mch')->where($sql_u_where)->update($sql_u_update);
                                    if($res_u < 0)
                                    {   
                                        Db::rollback();
                                        $lktlog->log("common/FlashSalePublic.log","\r\n订单结算到账失败！参数:" . json_encode($sql_u_where) . "\r\n");
                                    }
                                    else
                                    {
                                        $sql_where = array('store_id'=>$store_id,'id'=>$value['id']);
                                        $sql_update = array('settlement_type'=>1);
                                        $res = Db::name('order_details')->where($sql_where)->update($sql_update);
                                        if($res < 0)
                                        {   
                                            Db::rollback();
                                            $lktlog->log("common/FlashSalePublic.log","\r\n订单结算失败！参数:" . json_encode($sql_where) . "\r\n");
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
                    }
                }
            }
            Db::commit();
        }
    }

    // 获取插件状态
    public function Get_plugin_status($store_id)
    {
        $is_status = 0;
        // $shop_id = Db::name('admin')->where(['store_id'=>$store_id,'recycle'=>0,'type'=>1])->value('shop_id');
        // $is_status = Db::name('flashsale_config')->where(['store_id'=>$store_id,'mch_id'=>$shop_id])->value('is_open');
        $is_status = Db::name('flashsale_config')->where(['store_id'=>$store_id,'mch_id'=>0])->value('is_open');
      
        return $is_status;
    }

    // 获取限时设置
    public static function getFsConfig($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $pro_name = $array['pro_name']; // 商品名称
        $source = $array['source']; // 来源 1.后台 2.PC店铺端 3.移动店铺端
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页多少条数据
        
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $con = " f.store_id = '$store_id' and f.mch_id = '$mch_id' and f.is_delete = 0 ";
        if($pro_name != '')
        {
            $con .= " and b.product_title like '%$pro_name%' ";
        }

        $list = array();
        $total = 0;

        $sql0 = "select tt.* from (select a.id,row_number () over (PARTITION BY a.id) AS top from lkt_flashsale_addgoods as f left join lkt_configure as a on f.attr_id = a.id left join lkt_product_list as b on a.pid = b.id left join lkt_mch as d on b.mch_id = d.id left join lkt_brand_class as c on b.brand_id = c.brand_id left join lkt_product_class as e on SUBSTRING_INDEX(SUBSTRING_INDEX(b.product_class, '-', 2),'-', - 1) = e.cid where $con ) as tt where tt.top < 2";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = count($r0);
        }

        $str = " f.id,f.store_id,f.goods_id,f.attr_id,f.goods_price,f.price as aprice,f.num,a.img,a.attribute,a.price,a.num as stockNum,b.product_title as goodsName,c.brand_name as brandName,e.pname as className,row_number () over (PARTITION BY a.id) AS top ";
        if($source = 2)
        {
            $sql1 = "select tt.* from (select $str from lkt_flashsale_addgoods as f left join lkt_configure as a on f.attr_id = a.id left join lkt_product_list as b on a.pid = b.id left join lkt_mch as d on b.mch_id = d.id left join lkt_brand_class as c on b.brand_id = c.brand_id left join lkt_product_class as e on SUBSTRING_INDEX(SUBSTRING_INDEX(b.product_class, '-', 2),'-', - 1) = e.cid where $con ) as tt where tt.top < 2";
        }
        else
        {
            $sql1 = "select tt.* from (select $str from lkt_flashsale_addgoods as f left join lkt_configure as a on f.attr_id = a.id left join lkt_product_list as b on a.pid = b.id left join lkt_mch as d on b.mch_id = d.id left join lkt_brand_class as c on b.brand_id = c.brand_id left join lkt_product_class as e on SUBSTRING_INDEX(SUBSTRING_INDEX(b.product_class, '-', 2),'-', - 1) = e.cid where $con ) as tt where tt.top < 2 limit $start,$pagesize";
        }
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v) 
            {
                $v['mch_id'] = $mch_id;
                $v['imgUrl'] = ServerPath::getimgpath($v['img'],$store_id);
                $specifications = '';
                if($v['attribute'] != '')
                {
                    $attribute = unserialize($v['attribute']);
                    if (count($attribute) > 0)
                    {
                        foreach($attribute as $ke => $va)
                        {
                            if(strpos($ke, '_LKT_') !== false)
                            {
                                $ke = substr($ke, 0, strrpos($ke, "_LKT"));
                                $va = substr($va, 0, strrpos($va, "_LKT"));
                            }
                            $specifications .= $ke . ':' . $va . ',';
                        }
                    }
                }
                $v['attribute'] = rtrim($specifications, ",");
                $v['price'] = floatval($v['price']);
                $v['num'] = intval($v['stockNum']);

                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        echo json_encode(array('code'=>200,'message'=>$message,'data'=>$data));
        exit;
    }

    // 获取商品规格信息
    public static function getProAttrList($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $proNotInId = $array['proNotInId']; // 标签id
        $class_id = $array['class_id']; // 分类id
        $brand_id = $array['brand_id']; // 品牌id
        $pro_name = $array['pro_name']; // 商品名称
        $source = $array['source']; // 来源 1.后台 2.PC店铺端 3.移动店铺端
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $attr_id_str = '';
        $attr_id_list = array();
        $sql_0 = "select attr_id from lkt_flashsale_addgoods where store_id = '$store_id' and mch_id = '$mch_id' and is_delete = 0 ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            foreach($r_0 as $k_0 => $v_0)
            {
                $attr_id_list[] = $v_0['attr_id'];
            }
            $attr_id_str = implode(',',$attr_id_list);
        }

        $condition = " b.store_id = '$store_id' and b.status = 2 and b.active = 1 and b.commodity_type = 0 and a.recycle = 0 and d.id = '$mch_id' and b.is_presell = 0 and b.gongyingshang = 0 and a.num > 0 ";
        if($proNotInId != '')
        {
            $condition .= " and a.id not in ($proNotInId) ";
        }
        if($class_id != '')
        {
            $condition .= " and b.product_class like '%-$class_id-%' ";
        }
        if($brand_id != '')
        {
            $condition .= " and b.brand_id = '$brand_id' ";
        }
        if($pro_name != '')
        {
            $condition .= " and (b.product_title like '%$pro_name%' or a.id like '%$pro_name%') ";
        }
        if($attr_id_str != '')
        {
            $condition .= " and a.id not in ($attr_id_str) ";
        }

        $total = 0;
        $list = array();

        $sql_num = "select count(1) as num from (select a.*,row_number () over (PARTITION BY a.id) AS top from lkt_configure as a left join lkt_product_list as b on a.pid = b.id left join lkt_mch as d on b.mch_id = d.id where $condition order by b.add_date) as tt where tt.top<2";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        $str = " a.id as attrId,a.attribute,a.img as imgUrl,a.price,a.num as stockNum,b.id,b.product_title as goodsName,c.brand_name as brandName,e.pname as className,d.name as mchName,row_number () over (PARTITION BY a.id) AS top ";
        $sql = "select tt.* from (select $str from lkt_configure as a left join lkt_product_list as b on a.pid = b.id left join lkt_mch as d on b.mch_id = d.id left join lkt_brand_class as c on b.brand_id = c.brand_id left join lkt_product_class as e on SUBSTRING_INDEX(SUBSTRING_INDEX(b.product_class, '-', 2),'-', - 1) = e.cid where $condition order by b.add_date) as tt where tt.top<2 limit $start,$pagesize";
        $res = Db::query($sql);
        if($res)
        {
            foreach ($res as $k => $v) 
            {
                $v['imgUrl'] = ServerPath::getimgpath($v['imgUrl'],$store_id);
                $attribute = unserialize($v['attribute']);
                $specifications = '';
                if($v['attribute'] != '')
                {
                    $attribute = unserialize($v['attribute']);
                    if (count($attribute) > 0)
                    {
                        foreach($attribute as $ke => $va)
                        {
                            if(strpos($ke, '_LKT_') !== false)
                            {
                                $ke = substr($ke, 0, strrpos($ke, "_LKT"));
                                $va = substr($va, 0, strrpos($va, "_LKT"));
                            }
                            $specifications .= $ke . ':' . $va . ',';
                        }
                    }
                }
                $v['attribute'] = rtrim($specifications, ",");
                $v['price'] = floatval($v['price']);

                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'res'=>$list);
        $message = Lang('Success');
        echo json_encode(array('code'=>200,'message'=>$message,'data'=>$data));
        exit;
    }

    // 限时设置
    public static function setFsConfig($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $goods_json = $array['goods_json'];
        $operator = $array['operator']; // 操作人
        $source = $array['source']; // 来源
        $id = '';
        if(isset($array['id']))
        {
            $id = $array['id'];
        }
        
        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();
        //加购商品
        $goods_arr = json_decode(urldecode($goods_json),true);

        if($source == 2)
        {
            $sql0 = "update lkt_flashsale_addgoods set is_delete = 1 where store_id = '$store_id' and mch_id = '$mch_id' ";
            $r0 = Db::execute($sql0);
        }

        if($goods_arr != array())
        {
            foreach ($goods_arr as $k => $v) 
            {
                if(isset($v['aprice']))
                {
                    $aprice = $v['aprice'];//加购价
                }
                else
                {
                    $message = Lang("flashsale.2");
                    echo json_encode(array('code'=>109,'message'=>$message));
                    exit;
                }
                if($id == '')
                {
                    $attrId = $v['attrId'];//属性ID
                    $sql1 = "select * from lkt_configure where id = '$attrId' and recycle = 0 ";
                    $r1 = Db::query($sql1);
                    if($r1)
                    {
                        $sql_insert = array('store_id'=>$store_id,'mch_id'=>$mch_id,'goods_id'=>$r1[0]['pid'],'attr_id'=>$attrId,'num'=>$r1[0]['num'],'max_num'=>$r1[0]['num'],'goods_price'=>$r1[0]['price'],'price'=>$aprice,'status'=>2,'add_time'=>$time);
                        $r2 = Db::name('flashsale_addgoods')->insertGetId($sql_insert);
                        if($r2 < 1)
                        {
                            $Jurisdiction->admin_record($store_id, $operator, '添加了限时折扣商品ID：'.$attrId.'失败',1,$source,$mch_id); 
                            $Log_content = __METHOD__ . '->' . __LINE__ . '添加加购失败！'.$goods_json;
                            self::Log($Log_content);
                            $message = Lang("operation failed");
                            echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                            exit;
                        }
                        $Jurisdiction->admin_record($store_id, $operator, '添加了限时折扣商品ID：'.$attrId,1,$source,$mch_id); 
                    }
                }
                else
                {
                    $sql2 = "update lkt_flashsale_addgoods set price = '$aprice' where store_id = '$store_id' and mch_id = '$mch_id' and id = '$id' ";
                    $r2 = Db::execute($sql2);
                    if($r2 == -1)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '修改了限时折扣加购商品ID：'.$id.'失败',2,$source,$mch_id); 
                        $Log_content = __METHOD__ . '->' . __LINE__ . '修改加购失败！'.$goods_json;
                        self::Log($Log_content);
                        $message = Lang("operation failed");
                        echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                        exit;
                    }
                }
            }
        }

        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message));
        exit;
    }

    // 移除加购商品
    public static function deleteFsConfig($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id'];
        $operator = $array['operator']; // 操作人
        $source = $array['source']; // 来源
        
        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        $sql0 = "update lkt_flashsale_addgoods set is_delete = 1 where store_id = '$store_id' and mch_id = '$mch_id' and id = '$id' ";
        $r0 = Db::execute($sql0);
        if($r0 < 1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '移除加购商品ID：'.$id.'失败',3,$source,$mch_id); 
            $Log_content = __METHOD__ . '->' . __LINE__ . '移除加购商品失败！ID：'.$id;
            self::Log($Log_content);
            $message = Lang("operation failed");
            echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
            exit;
        }

        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message));
        exit;
    }

    // 限时折扣活动列表
    public static function FsList($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $status = $array['status']; // 活动状态
        $name = $array['name']; // 活动名称
        $startdate = $array['startdate']; // 开始时间
        $enddate = $array['enddate']; // 结束时间
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页多少条数据
        $source = $array['source']; // 来源
        $pagesize = $pagesize ? $pagesize : '10';
        
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " store_id = '$store_id' and mch_id = '$mch_id' and recovery = 0 ";
        if($status)
        {
            $condition .= " and status = '$status' ";
        }
        if($name)
        {
            $condition .= " and (name like '%$name%' or id = '$name') ";
        }
        
        if ($startdate != '')
        {
            $condition .= " and starttime >= '$startdate' ";
        }
        if ($enddate != '')
        {
            $condition .= " and endtime <= '$enddate' ";
        }
        $total = 0;
        $list = array();

        $sql0 = "select count(id) as total from lkt_flashsale_label where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select * from lkt_flashsale_label where $condition order by sort desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v) 
            {
                $goodsNum = FlashsaleActivityModel::where(['store_id'=>$store_id,'label_id'=>$v['id'],'is_delete'=>0])->count();
                $v['goodsNum'] = intval($goodsNum);
                $v['discount'] = floatval($v['discount']);
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message,'data'=>$data));
        exit;
    }
    
    // 获取商品
    public static function getProList($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $class_id = $array['class_id']; // 分类ID
        $brand_id = $array['brand_id']; // 品牌ID
        $pro_name = $array['pro_name']; // 商品名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页多少条数据
        $source = $array['source']; // 来源
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        //初始化分页查询条件
        $condition = " a.store_id = '$store_id' and a.recycle = 0 and c.recycle = 0 and a.mch_status = 2 and a.status = 2 and a.commodity_type = '0' and a.mch_id = '$mch_id' and a.is_presell = 0 and a.gongyingshang = 0 and a.num > 0 ";
        
        if($class_id != '')
        {
            $condition .= " and a.product_class like '%-$class_id-%' ";
        }
        if($brand_id != '')
        {
            $condition .= " and a.brand_id = '$brand_id' ";
        }
        if($pro_name != '')
        {
            $condition .= " and (a.product_title like '%$pro_name%' or a.id = '$pro_name') ";
        }
        
        $list = array();
        $total = 0;

        $sql0 = "select count(1) as total from (select row_number () over (PARTITION BY a.id) AS top from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id left join lkt_configure as c on a.id = c.pid where $condition ) as tt where top < 2 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select tt.* from (select a.id,a.product_title as goodsName,a.product_class,a.brand_id,a.mch_id,c.id as attrId,a.num,a.imgurl as imgUrl,c.price,c.attribute,c.num as stockNum,b.name as mchName,row_number () over (PARTITION BY a.id) AS top from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id left join lkt_configure as c on a.id = c.pid where $condition order by id asc) as tt where top < 2 limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach($r1 as $k => $v)
            {
                $class_list = explode('-',trim($v['product_class'],'-'));
                $class_num = count($class_list) - 1;
                $class_id1 = $class_list[$class_num];

                $v['imgUrl'] = ServerPath::getimgpath($v['imgUrl'], $store_id);
                $v['goods_id'] = $v['id'];
                $v['className'] = '';
                $r2 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$class_id1])->field('pname')->select()->toArray();
                if($r2)
                {
                    $v['className'] = $r2[0]['pname'];
                }
                $v['brandName'] = '';
                $r3 = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0,'brand_id'=>$v['brand_id']])->field('brand_name')->select()->toArray();
                if($r3)
                {
                    $v['brandName'] = $r3[0]['brand_name'];
                }
                // 查询商品库存
                $res_n = ConfigureModel::where(['pid'=>$v['id'],'recycle'=>0])->field('SUM(num) as num')->select()->toArray();
                $v['stockNum'] = $res_n[0]['num'];
                $list[] = $v;
            }
        }

        $data = array('res'=>$list,'total'=>$total);
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message,'data'=>$data));
        exit;
    }

    // 添加活动
    public static function addLabel($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id']; // 活动ID
        $name = $array['name']; // 活动名称
        $start_date = $array['start_date']; // 开始时间
        $end_date = $array['end_date']; // 结束时间
        $discount = $array['discount']; // 折扣
        $buylimit = $array['buylimit']; // 购买上限
        $content = $array['content']; // 活动说明
        $goods_json = $array['goods_json']; // 商品数据
        $operator = $array['operator']; // 操作人
        $source = $array['source']; // 来源
        
        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();
        
        if(empty($name))
        {
            $message = Lang("sec.2");
            echo json_encode(array('code'=>ERROR_CODE_BQMCBNWK,'message'=>$message));
            exit;
        }

        if($id)
        {
            $res = FlashsaleLabelModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recovery'=>0])->where('id','<>',$id)->where('starttime','<',$end_date)->where('endtime','>',$start_date)->select()->toArray();
        }
        else
        {
            $res = FlashsaleLabelModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recovery'=>0])->where('starttime','<',$end_date)->where('endtime','>',$start_date)->select()->toArray();
        }
        if($res)
        {
            $message = Lang("flashsale.0");
            echo json_encode(array('code'=>ERROR_CODE_BQYCZ,'message'=>$message));
            exit;
        }

        Db::startTrans();
        $label_id = 0;
        if($id)
        {
            $label_id = $id;
            $sql0 = "update lkt_flashsale_label set name = '$name',starttime = '$start_date',endtime = '$end_date',discount = '$discount',buylimit = '$buylimit',content = '$content',update_date = '$time' where id = '$id' ";
            $r0 = Db::execute($sql0);
            if($r0 < 0)
            {
                Db::rollcack();
                $Jurisdiction->admin_record($store_id, $operator, '编辑了限时活动ID：'.$label_id.'失败',2,$source,$mch_id);   
                $Log_content = __METHOD__ . '->' . __LINE__ . '编辑了限时活动ID：'.$label_id.'失败';
                self::Log($Log_content);
                $message = Lang("Modification failed");
                echo json_encode(array('code'=>ERROR_CODE_MSPZCW,'message'=>$message));
                exit;
            }
            
            $Jurisdiction->admin_record($store_id, $operator, '编辑了限时活动ID：'.$label_id,2,$source,$mch_id);      
        }
        else
        {
            if($start_date <= $time)
            {
                $message = Lang("sysNotice.4");
                echo json_encode(array('code'=>ERROR_CODE_BQYCZ,'message'=>$message));
                exit;
            }

            $sql1 = "select max(sort) as sort from lkt_flashsale_label where store_id = '$store_id' ";
            $r1 = Db::query($sql1);
            $sort = $r1[0]['sort'] + 1;

            $sql_insert = array('store_id'=>$store_id,'name'=>$name,'starttime'=>$start_date,'endtime'=>$end_date,'discount'=>$discount,'buylimit'=>$buylimit,'sort'=>$sort,'content'=>$content,'add_date'=>$time,'update_date'=>$time,'mch_id'=>$mch_id);
            $label_id = Db::name('flashsale_label')->insertGetId($sql_insert);
            if($label_id < 1)
            {
                Db::rollcack();
                $Jurisdiction->admin_record($store_id, $operator, '添加了限时活动ID：'.$label_id.'失败',1,$source,$mch_id);   
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加了限时活动ID：'.$label_id.'失败';
                self::Log($Log_content);
                $message = Lang("operation failed");
                echo json_encode(array('code'=>ERROR_CODE_MSPZCW,'message'=>$message));
                exit;
            }
            $Jurisdiction->admin_record($store_id, $operator, '添加了限时活动ID：'.$label_id,1,$source,$mch_id);  
        }

        $pro_list = array();
        $sql_0 = "select goods_id from lkt_flashsale_activity where store_id = '$store_id' and label_id = '$label_id' ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            foreach ($r_0 as $k_0 => $v_0) 
            {
                $pro_list[] = $v_0['goods_id'];
            }
        }

        $goods_arr = json_decode(urldecode($goods_json),true);
        foreach ($goods_arr as $k => $v) 
        {
            $attrId = $v['attrId'];
            if(isset($v['discount']))
            {
                $discount1 = $v['discount'];
            }
            else
            {
                $discount1 = $discount;
            }
            if(isset($v['buylimit']))
            {
                $buylimit1 = $v['buylimit'];
            }
            else
            {
                $buylimit1 = $buylimit;
            }

            $sql3 = "select * from lkt_configure where id = '$attrId' ";
            $r3 = Db::query($sql3);
            if($r3)
            {
                $goodsId = $r3[0]['pid'];

                if(in_array($goodsId,$pro_list))
                { // 存在修改
                    $sql_update4 = array('name'=>$name,'discount'=>$discount1,'buylimit'=>$buylimit1,'starttime'=>$start_date,'endtime'=>$end_date,'update_date'=>$time);
                    $sql_where4 = array('store_id'=>$store_id,'goods_id'=>$goodsId,'label_id'=>$label_id);
                    $res = Db::name('flashsale_activity')->where($sql_where4)->update($sql_update4);
                    if($res == -1)
                    {
                        Db::rollcack();
                        $Jurisdiction->admin_record($store_id, $operator, '编辑了限时活动ID：'.$label_id.'失败',2,$source,$mch_id);  
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . '添加活动失败！'.$goods_json;
                        self::Log($Log_content);
                        $message = Lang("operation failed");
                        echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                        exit;
                    }
                }
                else
                { // 不存在添加
                    $sql_insert4 = array('store_id'=>$store_id,'goods_id'=>$goodsId,'label_id'=>$label_id,'name'=>$name,'discount'=>$discount1,'buylimit'=>$buylimit1,'starttime'=>$start_date,'endtime'=>$end_date,'create_date'=>$time);
                    $res = Db::name('flashsale_activity')->insertGetId($sql_insert4);
                    if($res < 1)
                    {
                        Db::rollcack();
                        $Jurisdiction->admin_record($store_id, $operator, '添加了限时活动ID：'.$label_id.'失败',1,$source,$mch_id);   
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . '添加活动失败！'.$goods_json;
                        self::Log($Log_content);
                        $message = Lang("operation failed");
                        echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                        exit;
                    }
                }

                foreach($pro_list as $k_ => $v_)
                {
                    if($goodsId == $v_)
                    {
                        unset($pro_list[$k_]);
                    }
                }
            }
        }

        if($pro_list != array())
        {
            foreach($pro_list as $k_ => $v_)
            {
                $sql6 = "update lkt_flashsale_activity set is_delete = 1 where goods_id = '$v_' ";
                $r6 = Db::execute($sql6);
            }
        }

        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message));
        exit;
    }

    // 活动商品列表
    public static function fsLabelGoodsList($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $label_id = $array['label_id']; // 活动ID

        $fsName = ''; //活动名称
        $discount = '';  //折扣
        $buylimit = '';  //购买上限
        $starttime = ''; //开始时间
        $endtime = '';  //结束时间
        $content = '';  //说明
        $res_l = flashsaleLabelModel::where(['store_id'=>$store_id,'id'=>$label_id])->select()->toArray();
        if($res_l)
        {
            $fsName = $res_l[0]['name']; //活动名称
            $discount = $res_l[0]['discount'];  //折扣
            $buylimit = $res_l[0]['buylimit'];  //购买上限
            $starttime = $res_l[0]['starttime']; //开始时间
            $endtime = $res_l[0]['endtime'];  //结束时间
            $content = $res_l[0]['content'];  //活动说明
        }

        $condition = " a.store_id = '$store_id' and a.label_id = '$label_id' and a.goods_id = b.id and b.recycle = 0 and b.mch_id=c.id and a.is_delete = 0 and b.brand_id=d.brand_id and lab.id = a.label_id and lab.recovery = 0 and substring_index(substring_index(b.product_class, '-', 2), '-', -1)=e.cid and c.id = '$mch_id'";

        $list = array();
        $str = " a.id,a.goods_id,a.discount,a.buylimit,a.status,b.product_title as goodsName,b.imgurl,a.num,d.brand_name brandName,e.pname className,row_number () over (PARTITION BY a.id) AS top ";
        $sql = "select tt.* from (select $str from lkt_flashsale_activity a,lkt_flashsale_label lab,lkt_product_list b,lkt_mch c,lkt_brand_class d,lkt_product_class e where $condition order by a.update_date desc) as tt where top < 2";
        $res = Db::query($sql);
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $value['imgUrl'] = ServerPath::getimgpath($value['imgurl'], $store_id);
                $goodsId = $value['goods_id'];
                //获取商品价格
                $att = ConfigureModel::where(['pid'=>$goodsId,'recycle'=>0])->field('price,id')->select()->toArray();
                // 查询商品库存
                $res_n = ConfigureModel::where(['pid'=>$goodsId,'recycle'=>0])->field('SUM(num) as num')->select()->toArray();

                $value['stockNum'] = floatval($res_n[0]['num']);
                $value['goodsPrice'] = floatval($att[0]['price']);
                $value['price'] = floatval($att[0]['price']);
                $value['attrId'] = $att[0]['id'];
                $value['discount'] = floatval($value['discount']);
                $value['fsGoodsId'] = $value['id'];
                $value['goodId'] = $goodsId;

                $list[] = $value;
            }
        }

        $data = array('id' => $label_id,'name' =>$fsName,'title' =>$fsName,'discount' => floatval($discount),'buylimit'=>$buylimit,'starttime'=>$starttime,'endtime'=>$endtime,'content'=>$content,'goodsList'=>$list);
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message,'data'=>$data));
        exit;
    }

    // 删除活动
    public static function delLabel($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id']; // 活动ID
        $operator = $array['operator']; // 操作人
        $source = $array['source']; // 来源
        
        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();
        
        $sql0 = "update lkt_flashsale_label set recovery = 1 where id = '$id' ";
        $r0 = Db::execute($sql0);
        if($r0 == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了限时活动ID：'.$id.'失败',3,$source,$mch_id);    
            $message = Lang("sec.7");
            return output(ERROR_CODE_SCSPBQSB,$message);
        }

        $Jurisdiction->admin_record($store_id, $operator, '删除了限时活动ID：'.$id,3,$source,$mch_id);      
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message));
        exit;
    }

    // 日志
    public static function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("plugin/flashsale.log",$Log_content);
        return;
    }
}
