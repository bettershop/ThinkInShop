<?php

namespace app\common\Plugin;
use think\facade\Db;
use think\facade\Request;
use think\facade\Cache;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Plugin\Plugin;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;
use app\common\GETUI\LaikePushTools;
use app\common\CouponPublicMethod;
use app\common\Plugin\MchPublicMethod;
use app\common\DeliveryHelper;
use app\common\ReceiveGoodsUtils;
use app\common\Order;
use app\common\LKTConfigInfo;
use app\common\Jurisdiction;
use app\common\Product;

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
use app\admin\model\SecondsActivityModel;
use app\admin\model\SecondsLabelModel;
use app\admin\model\SecondsConfigModel;
use app\admin\model\SecondsRecordModel;
use app\admin\model\SystemMessageModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\UserAddressModel;


//秒杀公共文件
class SecondsPublic 
{
    // 获取插件状态
    public function is_Plugin($store_id)
    {
        $r0 = SecondsConfigModel::where('store_id', $store_id)->field('is_open')->select()->toArray();
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
    
    public function uninstall(...$context)
    {

    }

    public function install(...$context)
    {

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

        $canshu = addslashes(Request::param('canshu'));//参数
        $lktlog = new LaiKeLogUtils();
        list($product, $shop_list, $cart_id) = Tools::productHandle($product1, $cart_id);
        $products_total = 0;

        // 支付状态
        $payment = Tools::getPayment($store_id);
        //用户基本信息
        list($user_money, $enterless, $password_status) = Tools::userInfo($user, $store_id, $user_id,$lktlog);
        //2.区分购物车结算和立即购买---列出选购商品
        $sec_id = addslashes(Request::param('sec_id'));


        $products = Tools::products_list($store_id,$cart_id, $product, $product_type);

        //3.列出商品数组-计算总价和优惠，运费
        $products_data =Tools::get_products_data($store_id, $products, $products_total,'MS');
        
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
        $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id,'MS');
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
        
        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        //查询配置
        $sec_c = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
        $buy_num = $sec_c[0]['buy_num'];//秒杀商品默认限购数量

        //查询出秒杀价格
        $sql_sec = "select a.*,b.mch_id from lkt_seconds_activity as a left join lkt_seconds_label as b on a.label_id = b.id where a.store_id = '$store_id' and a.id = '$sec_id' and a.is_delete = 0 and a.isshow = 1 and a.status = 2";
        $price_res = Db::query($sql_sec);
        if (!empty($price_res))
        {
            $pro_arr = explode(',', $product);
            $num = $pro_arr[2];
            //获取价格
            $configure = ConfigureModel::where(['id'=>$pro_arr[1],'pid'=>$pro_arr[0]])->field('price')->select()->toArray();
            $price = $configure[0]['price'];//售价
            $price_type = $price_res[0]['price_type'];//秒杀价格单位 0=百分比 1=固定值
            $seconds_price = $price_res[0]['seconds_price'];
            $mch_id = $price_res[0]['mch_id'];
            if($price_type == 1)
            {
                $products_total = round($seconds_price * $num,2);
            }
            else
            {
                $products_total = round($seconds_price * $price * 0.01 * $num,2);
            }
            $remainingTime = strtotime($price_res[0]['endtime'])* 1000;
            //判断购买条件
            
            //查询用户已经购买数量
            $buyNum = 0;
            $sql_num = "select ifnull(sum(d.num),0) as num from lkt_order_details as d left join lkt_order as o on d.r_sNo = o.sNo where o.otype = 'MS' and o.store_id = '$store_id' and d.p_id = '$sec_id' and o.status != 7 and o.user_id = '$user_id' ";
            $res_num = Db::query($sql_num);
            if($res_num)
            {
                $buyNum = $res_num[0]['num'];
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
        $products[0]['list'][0]['attrPrice'] = floatval($price);
        $products[0]['list'][0]['price'] = floatval($seconds_price);
        $products[0]['list'][0]['price_type'] = $price_type;
        if($price_type == 1)
        {
            $products[0]['list'][0]['secPrice'] = floatval($seconds_price);
        }
        else
        {
            $products[0]['list'][0]['secPrice'] = round($seconds_price * $price * 0.01,2);
        }
        $products[0]['list'][0]['sec_id'] = intval($sec_id);
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

        $discount = 1;
        $grade_rate = 1;

        $total = $products_total * $grade_rate + $yunfei; // 商品总价 - 自动满减 + 运费 - 优惠券金额    实际付款金额
        $total = round($total, 2);
        $grade_rate_amount = round($products_total * (1- $grade_rate), 2);

        //5.返回数据
        $data = array('grade_rate_amount'=>$grade_rate_amount,'payment' => $payment, 'activity_id' => 0, 'products' => $products, 'is_distribution' => $is_distribution, 'discount' => $discount, 'products_total' => $products_total, 'freight' => $yunfei, 'total' => $total, 'user_money' => $user_money, 'address' => $address, 'addemt' => $addemt, 'password_status' => $password_status, 'enterless' => $enterless, 'shop_status' => $shop_status, 'shop_list' => $shop_list, 'grade_rate' => $grade_rate,'remainingTime'=>$remainingTime);
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
        $type = trim(Request::param('type')) ? Request::param('type') : 'MS'; // 订单类型
        $cart_id = addslashes(trim(Request::param('cart_id')));  //购物车id-- 12,13,123,
        $grade_l = addslashes(Request::param('grade_l'));//会员特惠 兑换券级别
        $coupon_id = trim(Request::param('coupon_id')); // 优惠券id
        $allow = trim(Request::param('allow'))?trim(Request::param('allow')):0; // 用户使用积分
        $address_id = Request::param('address_id'); //  地址id
        $shop_address_id = Request::param('shop_address_id')?trim(Request::param('shop_address_id')):0; //  门店地址id
        $fullName = Request::param('fullName')?trim(Request::param('fullName')):''; //  门店地址id
        $fullPhone = Request::param('fullPhone')?trim(Request::param('fullPhone')):''; //  门店地址id
        $remarks = trim(Request::param('remarks')); //  订单备注
        $store_type = addslashes(trim(Request::param('store_type')));
        $pay_type = addslashes(trim(Request::param('pay_type')));//
        $sec_id = addslashes(trim(Request::param('sec_id')));//

        // $remarks = htmlspecialchars_decode($remarks); // 将特殊的 HTML 实体转换回普通字符
        // $remarks = json_decode($remarks, true);

        if(empty($address_id) && empty($shop_address_id))
        {
            $message = Lang('nomal_order.1');
            echo json_encode(array('code' => ERROR_CODE_QXZSHDZ, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }
        $products_total = 0;
        $total = 0.00;
        $yunfei = 0;

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
        $no_delivery_list = array();
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
        $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id,'MS');
        if($address)
        {
            $cpc = $address['cpc'];
            $shop_cpc = $cpc;
            $mobile = $address['tel'];
            $name = $address['name'];
            $sheng = $address['sheng'];
            $shi = $address['city'];
            $xian = $address['quyu'];
            $address_xq = $address['address'];  
            $code = $address['code'];
        }

        // 6.计算运费
        $freight = Tools::get_freight($products_freight, $products, $address,$store_id, $type);
        $products = $freight['products'];
        // 存储运费数据
        if(isset($shop_address_id) && $shop_address_id != 0)
        {
            if($fullName != '')
            {
                $name = $fullName;
            }
            if($fullPhone != '')
            {
                $mobile = $fullPhone;
            }
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
        // 因为秒杀商品只有一个商品
        $sec_id = addslashes(trim(Request::param('sec_id')));
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
        $discount = 1;
        $remarks_0 = array();
        //获取商品id
        $pro_id = $product_id[0];

        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        //查询秒杀设置
        $sec_c = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
        $buy_num = $sec_c[0]['buy_num'];//秒杀商品默认限购数量
        
        //查询出秒杀价格
        $price_res = SecondsActivityModel::where(['store_id'=>$store_id,'id'=>$sec_id,'is_delete'=>0,'isshow'=>1])->select()->toArray();
        if (!empty($price_res))
        {
            $pro_arr = json_decode($product1,true);
            $num = $pro_arr[2]['num'];
            $label_id = $price_res[0]['label_id'];
            if($num > $price_res[0]['num'])
            {
                $message = Lang('tools.12');
                echo json_encode(array('code' => ERROR_CODE_LBYSPKCBZ, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }
            $la = SecondsLabelModel::where(['store_id'=>$store_id,'id'=>$label_id])->select()->toArray();
            $mch_id = $la[0]['mch_id'];
            
            //查询用户已经购买数量
            $buyNum = 0;
            $sql_num = "select ifnull(sum(d.num),0) as num from lkt_order_details as d left join lkt_order as o on d.r_sNo = o.sNo where o.otype = 'MS' and o.store_id = '$store_id' and d.p_id = '$sec_id' and o.status != 7 and o.user_id = '$user_id' ";
            $res_num = Db::query($sql_num);
            if($res_num)
            {
                $buyNum = $res_num[0]['num'];
            }
            if(($buyNum + $num) > $buy_num)
            {
                $message = Lang('sec.10');
                echo json_encode(array('code' => ERROR_CODE_CSYC, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            } 
            //获取价格
            $configure = ConfigureModel::where(['id'=>$pro_arr[1]['cid'],'pid'=>$pro_arr[0]['pid']])->field('price')->select()->toArray();
            $price = $configure[0]['price'];//售价
            $price_type = $price_res[0]['price_type'];//秒杀价格单位 0=百分比 1=固定值
            $seconds_price = $price_res[0]['seconds_price'];
            if($price_type == 1)
            {
                $products_total = round($seconds_price * $num,2);
                $secPrice = round($seconds_price,2);
            }
            else
            {
                $products_total = round($seconds_price * $price * 0.01 * $num,2) ;
                $secPrice = round($seconds_price * $price * 0.01,2);
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
            // if($remarks)
            // {
            //     $remarks_0[$mch_id1] = $remarks[$k];
            //     if($remarks[$k] != '')
            //     {
            //         $remarks_status = true;
            //     }
            // }
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
                $freight_price = $shop_address_id ? 0 : $freight_price;
                $sql_d = new OrderDetailsModel();
                $sql_d->store_id = $store_id;
                $sql_d->user_id = $user_id;
                $sql_d->p_id = $sec_id;
                $sql_d->p_name = $product_title;
                $sql_d->p_price = $secPrice;
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

                $z_num += $pdata->num;
            }
        }
        $mch_id = rtrim($mch_id, ',');
        $mch_id = ',' . $mch_id . ',';
        $bargain_order_no = '';
        $bargain_id = 0;
        $grade_rate = 1;

        $total = ($products_total * $grade_rate) + $yunfei; // 商品总价 * 分销折扣 - 自动满减 + 运费 - 优惠券金额     实际付款金额
        if ($total <= 0)
        {
            $total = 0.01;
        }
        // if($remarks_status)
        // {
        //     $remarks = serialize($remarks_0);
        // }
        // else
        // {
        //     $remarks = '';
        // }
        
        $otype = "MS";

        $order_failure_time = Tools::Obtain_expiration_time(array('store_id'=>$store_id,'otype'=>$otype));

        $sql_o = new OrderModel();
        $sql_o->store_id = $store_id;
        $sql_o->user_id = $user_id;
        $sql_o->name = $name;
        $sql_o->cpc = $cpc;
        $sql_o->mobile = $mobile;
        $sql_o->num = $z_num;
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
        $sql_o->comm_discount = $discount;
        $sql_o->remarks = $remarks;
        $sql_o->real_sno = $real_sno;
        $sql_o->self_lifting = $shop_status;
        $sql_o->extraction_code = $extraction_code;
        $sql_o->extraction_code_img = $extraction_code_img;
        $sql_o->grade_rate = $grade_rate;
        $sql_o->z_freight = $yunfei;
        $sql_o->preferential_amount = 0;
        $sql_o->single_store = $shop_address_id;
        $sql_o->order_failure_time = $order_failure_time;
        $sql_o->save();
        $r_o = $sql_o->id;
        if ($r_o > 0)
        {
            //添加秒杀记录
            $istmssql = new SecondsRecordModel();
            $istmssql->store_id = $store_id;
            $istmssql->user_id = $user_id;
            $istmssql->activity_id = $sec_id;//秒杀活动id
            $istmssql->sec_id = $sec_id;//秒杀活动id
            $istmssql->time_id = 0;
            $istmssql->pro_id = $pro_arr[0]['pid'];//商品id
            $istmssql->attr_id = $pro_arr[1]['cid'];//规格id
            $istmssql->price = $products_total;
            $istmssql->num = $z_num;
            $istmssql->is_delete = 0;
            $istmssql->sNo = $sNo;
            $istmssql->add_time = date('Y-m-d H:i:s');
            $istmssql->save();
            $record_res = $istmssql->id;
            if ($record_res < 1)
            {
                $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "添加秒杀记录失败！product:" . $product1);
            }
            
            $sql_a = SecondsActivityModel::where(['store_id'=>$store_id,'id'=>$sec_id,'is_delete'=>0,'isshow'=>1])->find();
            $sql_a->num = Db::raw('num - '.$z_num);
            $res_a = $sql_a->save();
            if (!$res_a)
            {
                $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改秒杀商品库存失败！product:" . $product1);
            }

            $r0 = ConfigureModel::where(['id'=>$pro_arr[1],'pid'=>$pro_arr[0]])->field('num')->select()->toArray();
            $total_num = $r0[0]['num'];

            // 生成库存记录
            $content = $user_id . '生成订单所需' . $num;
            $StockModel = new StockModel();
            $StockModel->store_id = $store_id;
            $StockModel->product_id = $pro_arr[0]['pid'];
            $StockModel->attribute_id = $pro_arr[1]['cid'];
            $StockModel->total_num = $total_num;
            $StockModel->flowing_num = $num;
            $StockModel->type = 1;
            $StockModel->user_id = $user_id;
            $StockModel->add_date = date("Y-m-d H:i:s");
            $StockModel->content = $content;
            $StockModel->save();

            //返回
            Db::commit();
            $arr = array('sNo' => $sNo, 'total' => sprintf("%.2f",$total), 'order_id' => $r_o,'order_type'=>'MS','orderTime'=>date("Y-m-d H:i:s"));
            ob_clean();
            $message = Lang("Success");
            echo json_encode(array('code' => 200, 'data' => $arr,'message'=>$message));
            exit;
        }
        else
        {
            $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "添加秒杀订单失败！product:" . $product1);
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
        $this->seckill_status($store_id);//自动开启 、 结束活动
        $this->seckill_start_notice($store_id);//秒杀开启通知

        $this->order_failure($store_id);// 删除过期订单
        $this->ok_Order($store_id);//自动收货
        $this->auto_good_comment($store_id);//自动评价
        $this->order_settlement($store_id);//商家订单结算
    }

    //自动开启 、 结束活动
    public function seckill_status($store_id)
    {   
        $lktlog = new LaiKeLogUtils();
        $time = date('Y-m-d H:i:s');

        $sql0 = "select a.* from lkt_seconds_activity as a left join lkt_seconds_label as b on a.label_id = b.id where a.store_id = '$store_id' and a.is_delete = 0 and b.recovery = 0 and status != 3";
        $r0 = Db::query($sql0);
        if($r0)
        {   
            foreach ($r0 as $k0 => $v0)
            {   
                Db::startTrans();
                $id = $v0['id'];
                $starttime = $v0['starttime'];
                $endtime = $v0['endtime'];
                $activity_id = $v0['id'];
                $goods_id = $v0['goodsId'];
                $attr_id = $v0['attr_id'];
                $num = $v0['num'];//剩余总库存
                $sql1 = SecondsActivityModel::find($id);
                if($endtime <= $time)
                { // 已结束
                    $sql1->status = 3;
                    //剩余库存回滚
                    $sql_p = ProductListModel::find($goods_id);
                    if($sql_p)
                    {
                        $sql_p->num = Db::raw('num + '.$num);
                        $res = $sql_p->save();
                        if(!$res)
                        {
                            Db::rollback();
                            $lktlog->log("common/SecondsPublic.log",__METHOD__ . ":" . __LINE__ . "修改商品总库存失败！goods_id:" . $goods_id);
                        }
                    }

                    $sql_c = ConfigureModel::find($attr_id);
                    if($sql_c)
                    {
                        $sql_c->num = Db::raw('num + '.$num);
                        $res_c = $sql_c->save();
                        if(!$res_c)
                        {
                            Db::rollback();
                            $lktlog->log("common/SecondsPublic.log",__METHOD__ . ":" . __LINE__ . "修改商品规格库存失败！attr_id:" . $attr_id);
                        }
                    }
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
                    $lktlog->log("common/SecondsPublic.log",__METHOD__ . ":" . __LINE__ . "修改活动状态失败！id:" . $id);
                }
                Db::commit();
            }
            
        }
        return;
    }

    //秒杀开启通知
    public function seckill_start_notice($store_id)
    {   
        $lktlog = new LaiKeLogUtils();
        $time = date('Y-m-d H:i:s');

        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        // 获取店铺秒杀配置
        $config = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('remind,is_open')->select()->toArray();

        //查询所有未通知的未开始的秒杀活动店铺
        $sql0 = "select distinct b.mch_id from lkt_seconds_activity as a left join lkt_seconds_label as b on a.label_id = b.id where a.store_id = '$store_id' and a.is_delete = 0 and b.recovery = 0 and a.status = 1 and a.isNotice = 0";
        $res0 = Db::query($sql0);
        if($res0)
        {   
            Db::startTrans();
            foreach ($res0 as $key => $value) 
            {
                $mch_id = $value['mch_id'];
                if($config && $config[0]['is_open'] == 1)
                {   
                    //秒杀活动提醒推送设置变更为可设多次
                    $remind = $config[0]['remind'];
                    $remindArr = explode(',', $remind);
                    rsort($remindArr);
                    foreach ($remindArr as $k1 => $val) 
                    {
                        //查询该店铺下的需要提醒的活动
                        $sql_s = "select a.id from lkt_seconds_activity as a left join lkt_seconds_label as b on a.label_id = b.id where a.store_id = '$store_id' and a.is_delete = 0 and b.recovery = 0 and a.status = 1 and a.isNotice = 0 and b.mch_id = '$mch_id' and date_sub(a.starttime,interval ".$val." second) <= '$time' ";
                        $res_s = Db::query($sql_s);
                        if($res_s)
                        {
                            foreach ($res_s as $k => $v) 
                            {
                                $activity_id = $v['id'];
                                $sql_a = SecondsActivityModel::find($activity_id);
                                $sql_a->isNotice = 1;
                                $res_a = $sql_a->save();
                                if(!$res_a)
                                {
                                    Db::rollback();
                                    $lktlog->log("common/SecondsPublic.log",__METHOD__ . ":" . __LINE__ . "修改提醒的活动状态失败！activity_id:" . $activity_id);
                                }
                            }
                        }
                        // 查询所有会员插入秒杀通知
                        $user_arr = UserModel::where(['store_id'=>$store_id,'is_lock'=>0])->field('user_id')->select()->toArray();
                        if($user_arr)
                        {
                            foreach ($user_arr as $k => $v) 
                            {
                                $user_id = $v['user_id'];
                                $insert_sql = new SystemMessageModel();
                                $insert_sql->store_id = $store_id;
                                $insert_sql->senderid = 'admin';
                                $insert_sql->recipientid = $user_id;
                                $insert_sql->title = '有秒杀活动开始啦！';
                                $insert_sql->content = '秒杀活动马上要开始了';
                                $insert_sql->time = $time;
                                $insert_sql->type = 1;
                                $insert_sql->save();
                                $flage = $insert_sql->id;
                                if($flage < 1)
                                {
                                    Db::rollback();
                                    $lktlog->log("common/SecondsPublic.log",__METHOD__ . ":" . __LINE__ . "站内信添加失败！user_id:" . $user_id);
                                }
                            }
                        }
                    }
                }
            }
            Db::commit();
        }
        return;
    }

    //删除过期订单
    public function order_failure($store_id)
    {   
        $lktlog = new LaiKeLogUtils();

        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        //获取店铺积分商城配置
        $res_c = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('order_failure')->select()->toArray();
        $order_failure = $res_c ? $res_c[0]['order_failure'] : 3600; // 未付款订单保留时间

        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'MS' and o.store_id = '$store_id' and o.status = 0 and o.recycle = 0";
        $res_m = Db::query($sql_m);
        if($res_m)
        {
            foreach ($res_m as $key => $value) 
            {
                $mch_id = $value['mch_id'];
                
                if($order_failure != 0)
                {
                    $time01 = date("Y-m-d H:i:s", strtotime("-$order_failure seconds")); // 订单过期删除时间

                    // 根据用户id，订单为未付款，订单添加时间 小于 未付款订单保留时间,查询订单表
                    $r0 = OrderModel::where(['store_id'=>$store_id,'status'=>0])->where('add_time','<',$time01)->where('mch_id',','.$mch_id.',')->whereIn('otype','MS')->select()->toArray();
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

                                    $ms_record_res = SecondsRecordModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->select()->toArray();
                                    $activity_id = $ms_record_res[0]['activity_id'];

                                    // 移除秒杀订单  删除秒杀记录
                                    $up_ms_record = SecondsRecordModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->find();
                                    $up_ms_record->is_delete = 1;
                                    $up_ms_record->save();

                                    $sql_where5 = array('id'=>$activity_id,'store_id'=>$store_id);
                                    $r4 = Db::name('seconds_activity')->where($sql_where5)->update(['num'=>Db::raw('num+'.$v1['num'])]);
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
        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        //获取店铺积分商城配置
        $res_c = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('auto_the_goods')->select()->toArray();
        $auto_the_goods = $res_c ? $res_c[0]['auto_the_goods']/(3600*24) : 7; // 未付款订单保留时间

        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'MS' and o.store_id = '$store_id' and o.status = 2 and o.recycle = 0";
        $res_m = Db::query($sql_m);
        if($res_m)
        {
            Db::startTrans();
            foreach ($res_m as $ke => $v) 
            {
                $mch_id = $v['mch_id'];

                $code = true;
                $time = date('Y-m-d H:i:s');
                $sql01 = "select d.id,d.r_sNo,d.deliver_time,d.user_id,d.store_id,d.p_id,d.p_price,d.num,o.z_price 
                from lkt_order_details as d left join lkt_order as o on d.r_sNo = o.sNo
                where d.store_id = '$store_id' and d.r_status = '2' and o.otype = 'MS' and o.mch_id = ',".$mch_id.",' and date_add(d.deliver_time, interval $auto_the_goods day) < now()";
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
                            $mch = new mchPublicMethod();
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
        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        //获取店铺积分商城配置
        $res_c = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('auto_good_comment_day,auto_good_comment_content')->select()->toArray();
        $auto_good_comment_day = $res_c ? $res_c[0]['auto_good_comment_day']/(3600*24) : 0; // 未付款订单保留时间
        $auto_good_comment_content = $res_c ? $res_c[0]['auto_good_comment_content']:''; // 自动好评内容
        $add_time = date('Y-m-d H:i:s');

        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'MS' and o.store_id = '$store_id' and o.status = 5 and o.recycle = 0 GROUP BY p.mch_id ";
        $res_m = Db::query($sql_m);
        if($res_m)
        {
            foreach ($res_m as $key => $value) 
            {
                $mch_id = $value['mch_id'];
                
                if ($auto_good_comment_day > 0)
                { // 设置了天数自动好评
                    // 查询所有没有评论的订单商品
                    $time = date("Y-m-d H:i:s",strtotime("-5 minutes"));
                    $sql = "SELECT
                                a.id,a.r_sNo,a.user_id,a.p_id,a.sid 
                            FROM
                                lkt_order_details a
                                left join lkt_order o on a.r_sNo = o.sNo
                            WHERE
                                a.r_status = 5 
                                AND a.store_id = $store_id 
                                AND a.r_sNo like 'MS%' 
                                AND o.mch_id = ',".$mch_id.",'
                                AND date_add(a.arrive_time, interval $auto_good_comment_day day) <= now()
                                AND date_add(a.arrive_time, interval $auto_good_comment_day day) > '$time'
                                and a.id not in (select order_detail_id from lkt_comments )
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
        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        //获取店铺积分商城配置
        $res_c = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('order_after')->select()->toArray();
        $order_after = $res_c ? $res_c[0]['order_after']/(3600*24) : 0; // 未付款订单保留时间

        $time = date("Y-m-d H:i:s",strtotime("-$order_after day"));

        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'MS' and o.store_id = '$store_id' and o.status = 5 and o.recycle = 0";
        $res_m = Db::query($sql_m);
        if($res_m)
        {   
            Db::startTrans();
            foreach ($res_m as $k => $v) 
            {
                $mch_id = $v['mch_id'];
        
                $sql_m = "select a.after_discount,a.freight,b.mch_id,a.id,b.sNo from lkt_order_details as a left join lkt_order as b on a.r_sNo = b.sNo where b.store_id = '$store_id' and a.r_status in (3,5) and a.arrive_time <= '$time' and a.settlement_type = 0 and b.otype = 'MS' and b.mch_id = ',".$mch_id.",' ";
                $res_m = Db::query($sql_m);
                if($res_m)
                {
                    foreach ($res_m as $key => $value) 
                    {   
                        $sNo = $value['sNo'];
                        $res = ReturnOrderModel::where(['p_id'=>$value['id']])->whereIn('r_type','0,1,3')->where('re_type','<>',3)->field('id')->select()->toArray();
                        if(empty($res))
                        {
                            if($value['after_discount'] > 0)
                            {
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
                                        $lktlog->log("common/SecondsPublic.log","\r\n订单结算到账失败！参数:" . json_encode($sql_u_where) . "\r\n");
                                    }
                                    else
                                    {
                                        $sql_where = array('store_id'=>$store_id,'id'=>$value['id']);
                                        $sql_update = array('settlement_type'=>1);
                                        $res = Db::name('order_details')->where($sql_where)->update($sql_update);
                                        if($res < 0)
                                        {   
                                            Db::rollback();
                                            $lktlog->log("common/SecondsPublic.log","\r\n订单结算失败！参数:" . json_encode($sql_where) . "\r\n");
                                        }
                                    }
                                }
                            }

                            // 更新订单结算状态
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
            Db::commit();
        }
    }

    // 获取插件状态
    public function Get_plugin_status($store_id)
    {
        $is_status = 0;
        $shop_id = Db::name('admin')->where(['store_id'=>$store_id,'recycle'=>0,'type'=>1])->value('shop_id');
        $is_status = Db::name('seconds_config')->where(['store_id'=>$store_id,'mch_id'=>$shop_id])->value('is_open');
      
        return $is_status;
    }

    // 获取秒杀设置
    public function getSecConfig($array)
    {
        $store_id = $array['store_id'];

        $mch_id = PC_Tools::SelfOperatedStore($store_id);

        $isClose = false;
        //判断是否有开启的标签
        $res_l = SecondsLabelModel::where(['store_id'=>$store_id,'is_show'=>1])->select()->toArray();
        if(empty($res_l))
        {
            $isClose = true;
        }
        $list = '';
        $res = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $remind = $value['remind'];
                //多次消息提醒处理 '1200,600'
                if(strrpos($remind, ',') !== false)
                {
                    $remindArr = explode(',', $remind);
                    rsort($remindArr);
                    foreach ($remindArr as $k => $val) 
                    {
                        $remindArr[$k] = $val / 60;
                    }
                    $remind = implode(',', $remindArr);
                }
                else
                {
                    $remind = $value['remind'] / 60;
                }
                $res[$key]['remind'] = (string)$remind;
            }
            $list = $res[0];
        }

        $data = array('isClose'=>$isClose,'res'=>$list);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data'=>$data));
        exit;
    }

    // 秒杀设置
    public function setSecConfig($array)
    {
        $store_id = $array['store_id'];
        $shop_id = $array['mch_id'];
        $is_open = $array['is_open']; // 是否开启 1是 0否
        $buy_num = $array['buy_num']; // 限购数量
        $is_herald = $array['is_herald']; // 是否开启预告
        $remind = $array['remind']; // 秒杀提醒（单位：分钟）
        $package_settings = $array['package_settings']; // 是否开启包邮设置
        $heraldTime = $array['heraldTime']; // 预告时间 单位 小时
        $goodsNum = $array['goodsNum']; // 满足包邮条件的商品数量
        $auto_the_goods = $array['auto_the_goods']; // 自动收货时间 (天)
        $order_failure = $array['order_failure']; // 订单失效时间 (小时)
        $order_after = $array['order_after']; // 订单售后时间 (天)
        $deliver_remind = $array['deliver_remind']; // 提醒限制 (天)
        $auto_good_comment_day = $array['auto_good_comment_day']; // 自动好评时间 (天)
        $auto_good_comment_content = $array['auto_good_comment_content']; // 自动好评内容
        $rule = $array['rule']; // 秒杀规则
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();

        $mch_id = PC_Tools::SelfOperatedStore($store_id);

        if(empty($buy_num))
        {
        	$message = Lang("sec.0");
            return output(ERROR_CODE_QSRXGSL,$message);
        }

        if($is_herald == 1)
        {
        	if(empty($heraldTime))
        	{
        		$message = Lang("sec.1");
            	return output(ERROR_CODE_QSRYGSJ,$message);
        	}
        	else
        	{
        		$heraldTime = $heraldTime * 60 * 60;
        	}
        }
        //增加可多次推送消息
        if($remind)
        {
            $remindArr = explode(',', $remind);
            rsort($remindArr);
            foreach ($remindArr as $key => $value) 
            {
                $remindArr[$key] = $value * 60;
            }
            $remind = implode(',', $remindArr);
        }
        
        $res_i = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
        if($res_i)
        {	
        	$sql = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->find();
        	$sql->is_open = $is_open;
        	$sql->buy_num = $buy_num;
        	$sql->is_herald = $is_herald;
        	$sql->remind = $remind;
        	$sql->package_settings = $package_settings;
        	$sql->heraldTime = $heraldTime;
        	$sql->same_piece = $goodsNum;
        	$sql->auto_the_goods = $auto_the_goods;
        	$sql->order_failure = $order_failure;
        	$sql->order_after = $order_after;
        	$sql->deliver_remind = $deliver_remind;
        	$sql->auto_good_comment_day = $auto_good_comment_day;
            $sql->auto_good_comment_content = $auto_good_comment_content;
        	$sql->rule = $rule;
        	$res = $sql->save();
        	if(!$res)
        	{
                $Jurisdiction->admin_record($store_id, $operator, '修改了秒杀插件的配置信息失败',2,$operator_source,$shop_id,$operator_id);
        		$message = Lang("Modification failed");
            	return output(ERROR_CODE_MSPZCW,$message);
        	}
            $Jurisdiction->admin_record($store_id, $operator, '修改了秒杀插件的配置信息',2,$operator_source,$shop_id,$operator_id);
        }
        else
        {
        	$sql = new SecondsConfigModel();
            $sql->store_id = $store_id;
        	$sql->is_open = $is_open;
        	$sql->buy_num = $buy_num;
        	$sql->is_herald = $is_herald;
        	$sql->remind = $remind;
        	$sql->package_settings = $package_settings;
        	$sql->heraldTime = $heraldTime;
        	$sql->same_piece = $goodsNum;
        	$sql->auto_the_goods = $auto_the_goods;
        	$sql->order_failure = $order_failure;
        	$sql->order_after = $order_after;
        	$sql->deliver_remind = $deliver_remind;
        	$sql->auto_good_comment_day = $auto_good_comment_day;
            $sql->auto_good_comment_content = $auto_good_comment_content;
        	$sql->rule = $rule;
        	$sql->mch_id = $mch_id;
        	$sql->save();
        	$res = $sql->id;
        	if($res < 1)
        	{
                $Jurisdiction->admin_record($store_id, $operator, '添加了秒杀插件的配置信息失败',1,$operator_source,$shop_id,$operator_id);
        		$message = Lang("operation failed");
            	return output(ERROR_CODE_MSPZCW,$message);
        	}
            $Jurisdiction->admin_record($store_id, $operator, '添加了秒杀插件的配置信息',1,$operator_source,$shop_id,$operator_id);
        }
        
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 获取可添加商品列表
    public function index($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页数据
        $operator_source = $array['operator_source']; // 操作人来源

        $pagesize = $pagesize ? $pagesize : '10';
        $start = 0;

        if ($page)
        {
            $page = $page;
            $start = ($page - 1) * $pagesize;
        }

        $list = array();
        $total = SecondsLabelModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recovery'=>0])->count();
        $res = SecondsLabelModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recovery'=>0])
                                ->order('sort','desc')
                                ->limit($start,$pagesize)
                                ->select()
                                ->toArray();
        if($res)
        {
            $list = $res;
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 添加标签
    public function addLabel($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 秒杀标签ID
        $name = $array['name']; // 标签名称
        $title = $array['title']; // 副标题
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        $Self_operated_store_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID

        if($operator_source == 1)
        {
            $shop_id = 0;
        }
        else
        {
            $shop_id = $mch_id;
        }

        if(empty($name))
        {
            $message = Lang("sec.2");
            echo json_encode(array('code' => ERROR_CODE_BQMCBNWK,'message' => $message));
            exit;
        }

        // 获取秒杀配置
        $res_con = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$Self_operated_store_id])->select()->toArray();
        if(empty($res_con))
        {
            $message = Lang("sec.5");
            echo json_encode(array('code' => ERROR_CODE_ZBDMSPZ,'message' => $message));
            exit;
        }

        if($id)
        {
            //判断名称是否重复
            $qname = SecondsLabelModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'recovery'=>0])
                                        ->where('id','<>',$id)
                                        ->select()
                                        ->toArray();
            if($qname)
            {
                $message = Lang("sec.3");
                echo json_encode(array('code' => ERROR_CODE_BQYCZ,'message' => $message));
                exit;
            }

            $sql = SecondsLabelModel::find($id);
            $sql->name = $name;
            $sql->title = $title;
            $res = $sql->save();
            if($res < 0)
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "修改秒杀标签失败！标签名称：" . $name);
                $Jurisdiction->admin_record($store_id, $operator, "修改秒杀标签失败！标签名称：" . $name,2,$operator_source,$shop_id,$operator_id);
                $message = Lang("Modification failed");
                echo json_encode(array('code' => ERROR_CODE_MSPZCW,'message' => $message));
                exit;
            }
            $this->Log(__METHOD__ . ":" . __LINE__ . "修改秒杀标签成功！标签名称：" . $name);
            $Jurisdiction->admin_record($store_id, $operator, '修改了标签名称：'.$name,2,$operator_source,$shop_id,$operator_id);
        }
        else
        {
            //判断名称是否重复
            $qname = SecondsLabelModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'recovery'=>0])->select()->toArray();
            if($qname)
            {
                $message = Lang("sec.3");
                echo json_encode(array('code' => ERROR_CODE_BQYCZ,'message' => $message));
                exit;
            }

            $id = Tools::getUid();
            $max = SecondsLabelModel::max('sort');
            $sort = $max + 1;
            $sql = new SecondsLabelModel();
            $sql->name = $name;
            $sql->title = $title;
            $sql->sort = $sort;
            $sql->add_date = date("Y-m-d H:i:s");
            $sql->update_date = date("Y-m-d H:i:s");
            $sql->mch_id = $mch_id;
            $sql->store_id = $store_id;
            $sql->id = $id;
            $res = $sql->save();
            if(!$res)
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加秒杀标签失败！标签名称：" . $name);
                $Jurisdiction->admin_record($store_id, $operator, '添加秒杀标签失败！',1,$operator_source,$shop_id,$operator_id);
                $message = Lang("operation failed");
                echo json_encode(array('code' => ERROR_CODE_MSPZCW,'message' => $message));
                exit;
            }
            $this->Log(__METHOD__ . ":" . __LINE__ . "添加秒杀标签成功！标签名称：" . $name);
            $Jurisdiction->admin_record($store_id, $operator, '添加了标签名称：'.$name,1,$operator_source,$shop_id,$operator_id);
        }

        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 秒杀标签显示开关
    public function labelSwitch($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 秒杀标签ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        if($operator_source == 1)
        {
            $shop_id = 0;
        }
        else
        {
            $shop_id = $mch_id;
        }

        //获取当前状态
        $res_l = SecondsLabelModel::where('id',$id)->field('name,is_show')->select()->toArray();
        $name = $res_l[0]['name'];
        $o_isshow = $res_l[0]['is_show'];

        $sql = SecondsLabelModel::find($id);
        if($o_isshow == 1)
        {
            $sql->is_show = 0;
        }
        else
        {
            $sql->is_show = 1;
        }
        $res = $sql->save();
        if(!$res)
        {
            $Jurisdiction->admin_record($store_id, $operator, '将标签名称：'.$name.'，进行了显示开关操作失败',2,$operator_source,$shop_id,$operator_id);
            $message = Lang("operation failed");
            echo json_encode(array('code' => ERROR_CODE_SCSPSB,'message' => $message));
            exit;
        }
        $Jurisdiction->admin_record($store_id, $operator, '将标签名称：'.$name.'，进行了显示开关操作',2,$operator_source,$shop_id,$operator_id);

        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 秒杀标签上下移动
    public function sortMove($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $move_id = $array['move_id']; // 被下移id
        $move_id1 = $array['move_id1']; // 上移id
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        if($operator_source == 1)
        {
            $shop_id = 0;
        }
        else
        {
            $shop_id = $mch_id;
        }

        $name = '';
        $sql = "select name from lkt_seconds_label where store_id = '$store_id' and id = '$move_id1' ";
        $r = Db::query($sql);
        if($r)
        {
            $name = $r[0]['name'];
        }

        //获取被下移标签原排序号
        $res = SecondsLabelModel::where('id',$move_id)->field('sort')->select()->toArray();
        if($res)
        {
            $sort = $res[0]['sort'];
        }
        else
        {
            $message = Lang("Parameter error");
            echo json_encode(array('code' => ERROR_CODE_CSCW,'message' => $message));
            exit;
        }
        //获取上移标签原排序号
        $res1 = SecondsLabelModel::where('id',$move_id1)->field('sort')->select()->toArray();
        if($res1)
        {
            $sort1 = $res1[0]['sort'];
        }
        else
        {
            $message = Lang("Parameter error");
            echo json_encode(array('code' => ERROR_CODE_CSCW,'message' => $message));
            exit;
        }
        Db::startTrans();
        //交换排序号
        $sql = SecondsLabelModel::find($move_id);
        $sql->sort = $sort1;
        $res = $sql->save();
        if(!$res)
        {
            Db::rollcack();
            $Jurisdiction->admin_record($store_id, $operator, '上下移了标签名称：'.$name.'失败',2,$operator_source,$shop_id,$operator_id);
            $message = Lang("operation failed");
            echo json_encode(array('code' => ERROR_CODE_CZSB,'message' => $message));
            exit;
        }
        $sql1 = SecondsLabelModel::find($move_id1);
        $sql1->sort = $sort;
        $res1 = $sql1->save();
        if(!$res1)
        {
            Db::rollcack();
            $Jurisdiction->admin_record($store_id, $operator, '上下移了标签名称：'.$name.'失败',2,$operator_source,$shop_id,$operator_id);
            $message = Lang("operation failed");
            echo json_encode(array('code' => ERROR_CODE_CZSB,'message' => $message));
            exit;
        }

        $Jurisdiction->admin_record($store_id, $operator, '上下移了标签名称：'.$name,2,$operator_source,$shop_id,$operator_id);
        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 秒杀标签上下移动
    public function top($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 标签id
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        if($operator_source == 1)
        {
            $shop_id = 0;
        }
        else
        {
            $shop_id = $mch_id;
        }

        $name = '';
        $sql = "select name from lkt_seconds_label where store_id = '$store_id' and id = '$id' ";
        $r = Db::query($sql);
        if($r)
        {
            $name = $r[0]['name'];
        }

        //获取当前最大排序
        $max_sort = SecondsLabelModel::max('sort');
        $sql = SecondsLabelModel::where(['store_id'=>$store_id,'id'=>$id])->find();
        $sql->sort = (int)$max_sort + 1;
        $res = $sql->save();
        if(!$res)
        {
            $Jurisdiction->admin_record($store_id, $operator, '置顶了标签名称：'.$name.'失败',2,$operator_source,$shop_id,$operator_id);
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
        $Jurisdiction->admin_record($store_id, $operator, '置顶了标签名称：'.$name,2,$operator_source,$shop_id,$operator_id);

        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 删除标签
    public function delLabel($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 秒杀标签ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        if($operator_source == 1)
        {
            $shop_id = 0;
        }
        else
        {
            $shop_id = $mch_id;
        }
        //判断是否有显示的标签
        $res_s = SecondsLabelModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'is_show'=>1])
                                    ->where('id','<>',$id)
                                    ->select()
                                    ->toArray();
        if(empty($res_s))
        {
            $message = Lang("sec.6");
            echo json_encode(array('code' => ERROR_CODE_SCSBBQBXYYYGWXSZT,'message' => $message));
            exit;
        }

        //判断当前标签是否显示
        $res_i = SecondsLabelModel::where('id',$id)->field('name,is_show')->select()->toArray();
        if($res_i)
        {
            $name = $res_i[0]['name'];
            $is_show = $res_i[0]['is_show'];
            if($is_show == 1)
            {
                $message = Lang("sec.7");
                echo json_encode(array('code' => ERROR_CODE_XSZDBQBNXG,'message' => $message));
                exit;
            }
        }
        else
        {
            $message = Lang("Parameter error");
            echo json_encode(array('code' => ERROR_CODE_CSCW,'message' => $message));
            exit;
        }

        Db::startTrans();

        $sql = SecondsLabelModel::find($id);
        $sql->recovery = 1;
        $res = $sql->save();
        if(!$res)
        {
            Db::rollcack();
            $Jurisdiction->admin_record($store_id, $operator, '删除了标签名称：'.$name.'失败',3,$operator_source,$shop_id,$operator_id);
            $message = Lang("sec.7");
            return output(ERROR_CODE_SCSPBQSB,$message);
        }

        $sql_s = SecondsActivityModel::where(['store_id'=>$store_id,'label_id'=>$id,'is_delete'=>0])->select()->toArray();
        if($sql_s)
        {
            foreach($sql_s as $k => $v)
            {
                $activityID = $v['id'];
                $goodsId = $v['goodsId'];
                $attr_id = $v['attr_id'];
                $p_num = $v['num'];

                $sql = SecondsActivityModel::find($activityID);
                $sql->is_delete = 1;
                $sql->update_date = date("Y-m-d H:i:s");
                $res = $sql->save();
                if(!$res)
                {   
                    Db::rollcack();
                    $Jurisdiction->admin_record($store_id, $operator, '删除了标签ID：'.$label_id.'，里面的商品ID：'.$goodsId.'失败',3,$operator_source,$shop_id,$operator_id);
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_SCSPSB,$message);
                }

                //商品库存回退
                $sql_p = "update lkt_product_list set num = num + '$p_num' where store_id = '$store_id' and id = '$goodsId' ";
                $res_p = Db::execute($sql_p);
                if($res_p == -1)
                {
                    Db::rollcack();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '商品库存回退失败！'.$goodsId;
                    $this->Log($Log_content);
                    $Jurisdiction->admin_record($store_id, $operator, '删除了标签ID：'.$label_id.'，里面的商品ID：'.$goodsId.'失败',3,$operator_source,$shop_id,$operator_id);
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_CZSB,$message);
                }

                $sql_c = "update lkt_configure set num = num + '$p_num' where id = '$attr_id' ";
                $res_c = Db::execute($sql_c);
                if($res_c == -1)
                {
                    Db::rollcack();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '库存回退失败！'.$attr_id;
                    $this->Log($Log_content);
                    $Jurisdiction->admin_record($store_id, $operator, '删除了标签ID：'.$label_id.'，里面的商品ID：'.$goodsId.'失败',3,$operator_source,$shop_id,$operator_id);
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_CZSB,$message);
                }
            }
        }

        $Jurisdiction->admin_record($store_id, $operator, '删除了标签名称：'.$name,3,$operator_source,$shop_id,$operator_id);

        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 获取可添加商品列表(新)
    public function getProList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $label_id = $array['label_id']; // 标签id
        $class_id = $array['class_id']; // 分类id
        $brand_id = $array['brand_id']; // 品牌id
        $pro_name = $array['pro_name']; // 商品名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页数据
        $operator_source = $array['operator_source']; // 操作人来源

        $pagesize = $pagesize ? $pagesize : '10';
        $start = 0;

        if ($page)
        {
            $page = $page;
            $start = ($page - 1) * $pagesize;
        }

        $ids = '';
        //获取当前标签下还在进行中的商品
        $res_g = SecondsActivityModel::where(['is_delete'=>0])->where('status','<>',3)->field('goodsId')->select()->toArray();
        if($res_g)
        {
            foreach ($res_g as $key => $value) 
            {
                $ids .= $value['goodsId'].',';
            }
            $ids = rtrim($ids,',');
        }

        $condition = " d.recycle = 0 and p.recycle = 0 and p.mch_id= '$mch_id' and p.store_id = '$store_id' and p.status = 2 and p.is_presell = 0 and p.gongyingshang = 0 and exists(select 1 from lkt_configure where recycle=0 and pid=p.id and num>0) ";

        if($label_id != '')
        {
            $condition .= " and d.id not in (select attr_id from lkt_seconds_activity where label_id = '$label_id' and is_delete = 0 and status != 3) ";
        }
        if($class_id != '')
        {
            $condition .= " and p.product_class like '%-$class_id-%' ";
        }
        if($brand_id != '')
        {
            $condition .= " and b.brand_id = '$brand_id' ";
        }
        if($pro_name != '')
        {
            $pro_name_00 = Tools::FuzzyQueryConcatenation($pro_name);
            $condition .= " and p.product_title like $pro_name_00 ";
        }

        $total = 0;
        $list = array();

        $sql_num = "select ifnull(count(p.id),0) as num from lkt_configure as d left join lkt_product_list as p on d.pid = p.id left join lkt_mch as m on p.mch_id = m.id left join lkt_brand_class as b on p.brand_id = b.brand_id left join lkt_product_class as c on SUBSTRING_INDEX(SUBSTRING_INDEX(p.product_class, '-', 2),'-', - 1) = c.cid where $condition";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        $sql = "select p.id,p.imgurl,p.product_title as goodsName,b.brand_name as brandName,c.pname as className,m.name as mchName,d.id as attrId,d.price,d.num as stockNum,d.attribute from lkt_configure as d left join lkt_product_list as p on d.pid = p.id left join lkt_mch as m on p.mch_id = m.id left join lkt_brand_class as b on p.brand_id = b.brand_id left join lkt_product_class as c on SUBSTRING_INDEX(SUBSTRING_INDEX(p.product_class, '-', 2),'-', - 1) = c.cid where $condition order by p.add_date desc limit $start,$pagesize";
        $res = Db::query($sql);
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $pid = $value['id'];
                $res[$key]['imgUrl'] = ServerPath::getimgpath($value['imgurl'], $store_id);
                $res[$key]['price'] = (float)$value['price'];

                $attribute_array = array('store_id'=>$store_id,'attribute'=>$value['attribute']);
                $attribute = Product::AttributeProcessing($attribute_array); // 属性处理

                $res[$key]['attribute'] = trim($attribute,';'); // 属性处理
            }
            $list = $res;
        }

        $data = array('total'=>$total,'res'=>$list);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 秒杀标签商品列表
    public function secLabelGoodsList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $label_id = $array['label_id']; // 标签id
        $status = $array['status']; // 0未开始1进行中2已结束
        $name = $array['name']; // 商品名称
        $startdate = $array['startdate']; // 开始时间
        $enddate = $array['enddate']; // 结束时间
        $secGoodsId = $array['secGoodsId']; // 商品活动id
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页数据
        $operator_source = $array['operator_source']; // 操作人来源

        $pagesize = $pagesize ? $pagesize : '10';
        $start = 0;
        if ($page)
        {
            $page = $page;
            $start = ($page - 1) * $pagesize;
        }

        $total = 0;
        $list = array();

        $condition = " a.store_id = '$store_id' and a.label_id = '$label_id' and a.goodsId = b.id and b.recycle = 0 and a.attr_id = co.id and co.recycle = 0 and b.mch_id=c.id and a.is_delete = 0 and b.brand_id=d.brand_id and lab.id = a.label_id and lab.recovery = 0 and substring_index(substring_index(b.product_class, '-', 2), '-', -1)=e.cid and c.id = '$mch_id'";

        if(strlen($status))
        {
            switch ($status) 
            {
                case '1':
                    $condition .= " and a.status = 2 ";
                    break;

                case '2':
                    $condition .= " and a.status = 3 ";
                    break;

                default:
                    $condition .= " and a.status = 1 ";
                    break;
            }
        }

        if($name)
        {
            $name_00 = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and (b.product_title like $name_00 or b.id = '$name') ";
        }

        if ($startdate != '')
        {
            $condition .= " and a.starttime >= '$startdate 00:00:00' ";
        }

        if ($enddate != '')
        {
            $condition .= " and a.starttime <= '$enddate 00:00:00' ";
        }

        if($secGoodsId != '')
        {
            $condition .= " and a.id = '$secGoodsId' ";
        }

        $sql_num = "select ifnull(count(a.id),0) as num from lkt_seconds_activity a,lkt_seconds_label lab,lkt_product_list b,lkt_configure co,lkt_mch c,lkt_brand_class d,lkt_product_class e where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        $sql = "select a.id,b.id goodsId,b.product_title,b.initial,b.volume,c.name mchName,d.brand_name brandName,e.pname className,a.num stockNum,b.num,a.max_num, a.starttime,a.endtime,a.isshow is_show,a.seconds_price,a.price_type,a.status,b.imgurl,co.price,co.attribute from lkt_seconds_activity a,lkt_seconds_label lab,lkt_product_list b,lkt_configure co,lkt_mch c,lkt_brand_class d,lkt_product_class e where $condition order by a.update_date desc limit $start,$pagesize";
        $res = Db::query($sql);
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $status = $value['status'];
                switch ($status) 
                {
                    case '2':
                        $res[$key]['statusName'] = '进行中';
                        break;

                    case '3':
                        $res[$key]['statusName'] = '已结束';
                        break;

                    default:
                        $res[$key]['statusName'] = '未开始';
                        break;
                }

                $res[$key]['imgUrl'] = ServerPath::getimgpath($value['imgurl'], $store_id);
                $goodsId = $value['goodsId'];

                $res[$key]['goodsPrice'] = (float)$value['price'];

                $attribute_array = array('store_id'=>$store_id,'attribute'=>$value['attribute']);
                $attribute = Product::AttributeProcessing($attribute_array); // 属性处理
                $res[$key]['attribute'] = trim($attribute,';'); // 属性处理

                if($value['price_type'] == 1)
                {
                    $res[$key]['secPrice'] = floatval($value['seconds_price']);
                }
                else
                {
                    $res[$key]['secPrice'] = round($value['price'] * $value['seconds_price'] *0.01,2);
                }
                $res[$key]['seconds_price'] = $res[$key]['price'] = (float)$value['seconds_price'];
            }
            $list = $res;
        }

        $data = array('total'=>$total,'goodsList'=>$list);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 添加商品
    public function addPro($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $label_id = $array['label_id']; // 秒杀标签ID
        $id = $array['id']; // 秒杀商品ID
        $price_type = $array['price_type']; // 秒杀价格单位 0=百分比 1=固定值
        $seconds_price = $array['seconds_price']; // 秒杀价格
        $starttime = $array['starttime']; // 开始时间
        $endtime = $array['endtime']; // 结束时间
        $goods_json = $array['goods_json']; // 商品数据
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();

        $label_name = '';
        $sql = "select name from lkt_seconds_label where id = '$label_id' ";
        $r = Db::query($sql);
        if($r)
        {
            $label_name = $r[0]['name'];
        }

        if(strtotime($endtime) < time())
        {
            $message = Lang("sec.4");
            echo json_encode(array('code' => ERROR_CODE_JSSJBNXYDQSJ,'message' => $message));
            exit;
        }

        if($id)
        {
            $res_a = SecondsActivityModel::where(['store_id'=>$store_id,'label_id'=>$label_id,'id'=>$id])->select()->toArray();
            $goodsId = $res_a[0]['goodsId'];

            Db::startTrans();

            $sql = SecondsActivityModel::where(['store_id'=>$store_id,'label_id'=>$label_id,'id'=>$id])->find();
            $sql->price_type = $price_type;
            $sql->seconds_price = $seconds_price;
            $sql->starttime = $starttime;
            $sql->endtime = $endtime;
            $sql->update_date = date("Y-m-d H:i:s");
            $res = $sql->save();
            if(!$res)
            {
                Db::rollcack();
                $Log_content = __METHOD__ . '->' . __LINE__ . '修改活动失败！'.$goods_json;
                $this->Log($Log_content);
                $Jurisdiction->admin_record($store_id, $operator, '将标签名称：'.$label_name.',修改了秒杀商品，商品ID：'.$goodsId.'失败',2,$operator_source,$mch_id,$operator_id);
                $message = Lang("Modification failed");
                echo json_encode(array('code' => ERROR_CODE_XGSB,'message' => $message));
                exit;
            }

            $Jurisdiction->admin_record($store_id, $operator, '将标签名称：'.$label_name.',修改了秒杀商品，商品ID：'.$goodsId,2,$operator_source,$mch_id,$operator_id);
            Db::commit();
        }
        else
        {
            Db::startTrans();
            $goods_arr = json_decode(urldecode($goods_json),true);
            foreach ($goods_arr as $k => $v) 
            {
                $goodsId = $v['goodsId'];
                $attr_id = $v['attrId'];
                $num = $v['goodsNum'];

                $sql_c = "select num from lkt_configure where id = '$attr_id' ";
                $r_c = Db::query($sql_c);
                if($r_c)
                {
                    if($v['goodsNum'] == $r_c[0]['num'])
                    {
                        $num = floor($r_c[0]['num'] / 2);
                    }
                }

                //添加活动
                $sql = new SecondsActivityModel();
                $sql->store_id = $store_id;
                $sql->goodsId = $goodsId;
                $sql->label_id = $label_id;
                $sql->attr_id = $attr_id;
                $sql->seconds_price = $seconds_price;
                $sql->price_type = $price_type;
                $sql->num = $num;
                $sql->max_num = $num;
                $sql->starttime = $starttime;
                $sql->endtime = $endtime;
                $sql->create_date = date("Y-m-d H:i:s");
                $sql->update_date = date("Y-m-d H:i:s");
                $sql->save();
                $res = $sql->id;
                if($res < 1)
                {
                    Db::rollcack();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '添加活动失败！'.$goods_json;
                    $this->Log($Log_content);
                    $Jurisdiction->admin_record($store_id, $operator, '将标签名称：'.$label_name.',添加了秒杀商品，商品ID：'.$goodsId.'失败',1,$operator_source,$mch_id,$operator_id);
                    $message = Lang("operation failed");
                    echo json_encode(array('code' => ERROR_CODE_CZSB,'message' => $message));
                    exit;
                }
                //商品库存预扣
                $sql_p = ProductListModel::where(['store_id'=>$store_id,'id'=>$goodsId])->find();
                $sql_p->num = Db::raw('num - '.$num);
                $res_p = $sql_p->save();
                if(!$res_p)
                {
                    Db::rollcack();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '添加活动失败！'.$goods_json;
                    $this->Log($Log_content);
                    $Jurisdiction->admin_record($store_id, $operator, '将标签名称：'.$label_name.',添加了秒杀商品，商品ID：'.$goodsId.'失败',1,$operator_source,$mch_id,$operator_id);
                    $message = Lang("operation failed");
                    echo json_encode(array('code' => ERROR_CODE_CZSB,'message' => $message));
                    exit;
                }

                $seconds_key = 'LAIKE_MS_'.$store_id.'_'.$goodsId.'_'.$attr_id;
                $old_num = Cache::get($seconds_key);
                $new_num = $old_num + $num;
                Cache::delete($seconds_key);
                Cache::set($seconds_key, $new_num);
                //规格库存预扣
                $sql_c = ConfigureModel::find($attr_id);
                $sql_c->num = Db::raw('num - '.$num);
                $res_c = $sql_c->save();
                if(!$res_c)
                {
                    Db::rollcack();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '修改活动失败！'.$goods_json;
                    $this->Log($Log_content);
                    $Jurisdiction->admin_record($store_id, $operator, '将标签名称：'.$label_name.',添加了秒杀商品，商品ID：'.$goodsId.'失败',1,$operator_source,$mch_id,$operator_id);
                    $message = Lang("Modification failed");
                    echo json_encode(array('code' => ERROR_CODE_XGSB,'message' => $message));
                    exit;
                }

                $Jurisdiction->admin_record($store_id, $operator, '将标签名称：'.$label_name.',添加了秒杀商品，商品ID：'.$goodsId,1,$operator_source,$mch_id,$operator_id);
            }
            Db::commit();
        }

        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 标签商品显示开关
    public function labelGoodsSwitch($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 秒杀标签ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        if($operator_source == 1)
        {
            $shop_id = 0;
        }
        else
        {
            $shop_id = $mch_id;
        }

        //获取当前状态
        $res_l = SecondsActivityModel::where('id',$id)->field('isshow')->select()->toArray();
        $o_isshow = $res_l[0]['isshow'];

        $sql = SecondsActivityModel::find($id);
        if($o_isshow == 1)
        {
        	$sql->isshow = 0;
        }
        else
        {
        	$sql->isshow = 1;
        }
        $res = $sql->save();
        if(!$res)
        {
            $Jurisdiction->admin_record($store_id, $operator, '将标签商品ID：'.$id.'，进行了显示开关操作失败',2,$operator_source,$shop_id,$operator_id);
        	$message = Lang("operation failed");
            echo json_encode(array('code' => ERROR_CODE_SCSPSB,'message' => $message));
            exit;
        }
        $Jurisdiction->admin_record($store_id, $operator, '将标签商品ID：'.$id.'，进行了显示开关操作',2,$operator_source,$shop_id,$operator_id);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 删除商品
    public function delPro($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 秒杀标签ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        if($operator_source == 1)
        {
            $shop_id = 0;
        }
        else
        {
            $shop_id = $mch_id;
        }
        $ids = explode(',',$id);

        Db::startTrans();
        foreach ($ids as $key => $value) 
        {
            $sql_s = SecondsActivityModel::where(['store_id'=>$store_id,'id'=>$value,'is_delete'=>0])->select()->toArray();
            if($sql_s)
            {
                $p_num = $sql_s[0]['num'];
                $goodsId = $sql_s[0]['goodsId'];
                $label_id = $sql_s[0]['label_id'];
                $attr_id = $sql_s[0]['attr_id'];
                //商品库存回退
                $sql_p = ProductListModel::where(['store_id'=>$store_id,'id'=>$goodsId])->find();
                $sql_p->num = Db::raw('num + '.$p_num);
                $res_p = $sql_p->save();
                if(!$res_p)
                {
                    Db::rollcack();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '商品库存回退失败！'.$goodsId;
                    $this->Log($Log_content);
                    $Jurisdiction->admin_record($store_id, $operator, '删除了标签ID：'.$label_id.'，里面的商品ID：'.$goodsId.'失败',3,$operator_source,$shop_id,$operator_id);
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_CZSB,$message);
                }

                $sql_c = ConfigureModel::find($attr_id);
                $sql_c->num = Db::raw('num + '.$p_num);
                $res_c = $sql_c->save();
                if(!$res_c)
                {
                    Db::rollcack();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '库存回退失败！'.$attr_id;
                    $this->Log($Log_content);
                    $Jurisdiction->admin_record($store_id, $operator, '删除了标签ID：'.$label_id.'，里面的商品ID：'.$goodsId.'失败',3,$operator_source,$shop_id,$operator_id);
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_CZSB,$message);
                }

                $sql = SecondsActivityModel::find($value);
                $sql->is_delete = 1;
                $sql->update_date = date("Y-m-d H:i:s");
                $res = $sql->save();
                if(!$res)
                {   
                    Db::rollcack();
                    $Jurisdiction->admin_record($store_id, $operator, '删除了标签ID：'.$label_id.'，里面的商品ID：'.$goodsId.'失败',3,$operator_source,$shop_id,$operator_id);
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_SCSPSB,$message);
                }
                $Jurisdiction->admin_record($store_id, $operator, '删除了标签ID：'.$label_id.'，里面的商品ID：'.$goodsId,3,$operator_source,$shop_id,$operator_id);
            }
            else
            {
                Db::rollcack();
                $Log_content = __METHOD__ . '->' . __LINE__ . '删除活动失败！id'.$value;
                $this->Log($Log_content);
                $message = Lang("operation failed");
                return output(ERROR_CODE_SCSPSB,$message);
            }   
        }
        Db::commit();

        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 获取秒杀记录列表
    public function getSecRecord($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $user = $array['user']; // 用户id、名称
        $goods_name = $array['goods_name']; // 商品名称
        $startdate = $array['startdate']; // 开始时间
        $enddate = $array['enddate']; // 结束时间
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页数据
        $operator_source = $array['operator_source']; // 操作人来源

        $pagesize = $pagesize ? $pagesize : '10';
        $start = 0;
        if ($page)
        {
            $page = $page;
            $start = ($page - 1) * $pagesize;
        }

        $total = 0;
        $list = array();

        $condition = " o.status != 0 and sr.is_delete = 0 and sr.store_id = '$store_id' and o.mch_id = ',".$mch_id.",' ";
        if($user != '')
        {
            $user_00 = Tools::FuzzyQueryConcatenation($user);
            $condition .= " and (o.user_id = '$user' or u.user_name like $user_00) ";
        }

        if($goods_name != '')
        {
            $goods_name_00 = Tools::FuzzyQueryConcatenation($goods_name);
            $condition .= " and pl.product_title like $goods_name_00 ";
        }

        if ($startdate != '')
        {
            $condition .= " and sr.add_time >= '$startdate' ";
        }

        if ($enddate != '')
        {
            $condition .= " and sr.add_time <= '$enddate' ";
        }

        $sql_num = "select ifnull(count(sr.id),0) as num FROM lkt_seconds_record AS sr LEFT JOIN lkt_seconds_activity as sp on sr.sec_id = sp.id LEFT JOIN lkt_product_list AS pl ON sr.pro_id = pl.id LEFT JOIN lkt_configure as c on c.id = sr.attr_id LEFT JOIN lkt_user AS u ON sr.user_id = u.user_id LEFT JOIN lkt_order as o on sr.sNo = o.sNo where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        $sql = "select sr.*, pl.product_title,cast(u.user_name as char) user_name, sp.starttime,sp.endtime,c.attribute,sr.pro_id,c.img attrImg from lkt_seconds_record AS sr LEFT JOIN lkt_seconds_activity as sp on sr.sec_id = sp.id LEFT JOIN lkt_product_list AS pl ON sr.pro_id = pl.id LEFT JOIN lkt_configure as c on c.id = sr.attr_id LEFT JOIN lkt_user AS u ON sr.user_id = u.user_id LEFT JOIN lkt_order as o on sr.sNo = o.sNo where $condition order by sr.add_time desc limit $start,$pagesize";
        $res = Db::query($sql);
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $res[$key]['imgUrl'] = ServerPath::getimgpath($value['attrImg'], $store_id);

                $attribute_array = array('store_id'=>$store_id,'attribute'=>$value['attribute']);
                $res[$key]['attribute'] = Product::AttributeProcessing($attribute_array); // 属性处理
            }
            $list = $res;
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 删除秒杀记录
    public function delSecRecord($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 记录id
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        if($operator_source == 1)
        {
            $shop_id = 0;
        }
        else
        {
            $shop_id = $mch_id;
        }

        $sql = SecondsRecordModel::find($id);
        $sql->is_delete = 1;
        $res = $sql->save();
        if(!$res)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了秒杀记录ID：'.$id.'失败',3,$operator_source,$shop_id,$operator_id);
            $message = Lang("sec.8");
            return output(ERROR_CODE_SCJLSB,$message);
        }
        $Jurisdiction->admin_record($store_id, $operator, '删除了秒杀记录ID：'.$id,3,$operator_source,$shop_id,$operator_id);

        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("plugin/SecondsPublic.log",$Log_content);
        return;
    }
}
