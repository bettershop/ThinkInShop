<?php
namespace app\common\Plugin;
use think\facade\Db;
use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\GETUI\LaikePushTools;
use app\common\DeliveryHelper;
use app\common\ServerPath;
use app\common\wxpayv2\wxpay;
use app\common\alipay0\aop\test\AlipayReturn;
use app\common\Plugin\RefundUtils;
use app\common\LKTConfigInfo;
use app\common\Jurisdiction;

use app\admin\model\OrderConfigModel;
use app\admin\model\MchModel;
use app\admin\model\UserModel;
use app\admin\model\UserRuleModel;
use app\admin\model\UserGradeModel;
use app\admin\model\ProductListModel;
use app\admin\model\ConfigureModel;
use app\admin\model\FreightModel;
use app\admin\model\GroupConfigModel;
use app\admin\model\GroupActivityModel;
use app\admin\model\GroupGoodsModel;
use app\admin\model\ProductClassModel;
use app\admin\model\BrandClassModel;
use app\admin\model\PaymentModel;
use app\admin\model\ExpressModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\OrderModel;
use app\admin\model\GroupOrderConfigModel;
use app\admin\model\ReturnOrderModel;

class Go_groupPublicMethod 
{
    // 确认订单页面
    public function settlement(&...$context)
    {
        $action = $context[0];
        //1.列出基础数据
        $user = $action->user;
        $user_id = $user['user_id'];
        $user_score = $user['score'];
        $store_id = $action->store_id;
        $store_type = $action->store_type;

        $address_id = addslashes(trim(Request::param('address_id'))); // 地址ID
        $productsInfo = addslashes(trim(Request::param('productsInfo'))); // 参数  {"pid":"4906","cid":1870,"num":2,"acId":"1674334035863666688"}
        $order_type = addslashes(trim(Request::param('order_type'))); // 类型

        $productsInfo1 = htmlspecialchars_decode($productsInfo);
        $productsInfo2 = json_decode(stripslashes(html_entity_decode($productsInfo1)),true); // 字符串打散为数组
        if($address_id == 'undefined')
        {
            $address_id = '';
        }
        $pid = $productsInfo2['pid']; // 商品ID
        $cid = $productsInfo2['cid']; // 属性ID 
        $num = $productsInfo2['num']; // 开团人数
        $acId = ''; // 活动ID
        $openId = ''; // 开团ID
        if(isset($productsInfo2['acId']))
        {
            $acId = $productsInfo2['acId']; // 活动ID
        }

        if(isset($productsInfo2['openId']))
        {
            $openId = $productsInfo2['openId']; // 开团ID
            $sql_0 = "select activity_id from lkt_group_open where id = '$openId' ";
            $r_0 = Db::query($sql_0);
            $acId = $r_0[0]['activity_id'];
        }

        $sql_config = "select goods_limit,team_limit from lkt_group_activity where store_id = '$store_id' and id = '$acId' ";
        $r_config = Db::query($sql_config);
        if($r_config)
        {
            $goods_limit = $r_config[0]['goods_limit']; // 同一商品参团限制开关
            $team_limit = $r_config[0]['team_limit']; // 团长是否可参团
            if($goods_limit == 1)
            { // 只能参团一次
                if($openId == '')
                { // 开团
                    if($team_limit == 1)
                    { // 团长可以参团
                        // 根据活动ID、用户user_id、商品ID，查询参团数据
                        $sql_ = "select b.id from lkt_group_open as a left join lkt_group_open_record as b on a.id = b.open_id where a.activity_id = '$acId' and b.user_id = '$user_id' and a.goods_id = '$pid' ";
                        $r_ = Db::query($sql_);
                        if($r_)
                        { // 存在
                            $message = Lang('go_group.14');
                            echo json_encode(array('code' => 109, 'message' => $message));
                            exit;
                        }
                    }
                }
                else
                { // 参团
                    // 根据活动ID、用户user_id、商品ID，查询参团数据
                    $sql_ = "select b.id from lkt_group_open as a left join lkt_group_open_record as b on a.id = b.open_id where a.activity_id = '$acId' and b.user_id = '$user_id' and a.goods_id = '$pid' ";
                    $r_ = Db::query($sql_);
                    if($r_)
                    { // 存在
                        $message = Lang('go_group.14');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                }
            }
        }
        else
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }

        // 支付状态
        $payment = Tools::getPayment($store_id);

        $lktlog = new LaiKeLogUtils();
        // 用户基本信息
        list($user_money, $enterless, $password_status) = Tools::userInfo($user, $store_id, $user_id,$lktlog);

        $products = $this->products_list($store_id,$pid, $cid);

        $products_total = 0;

        $products_data = $this->get_products_data($store_id,$productsInfo2, $products_total);
        $products_total = $products_data['products_total'];
        $products = $products_data['products'];

        $no_delivery_str = '';
        if($address_id == '')
        { // 获取不配送省的名称
            $no_delivery_str = $this->No_distribution_Province($store_id, $products);
        }

        // 查询默认地址order_details
        $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);

        $addemt = $address ? 1 : 0; // 收货地址状态
        if($address == array())
        {
            $address = array('id'=>null,'store_id'=>null,'name'=>null,'tel'=>null,'sheng'=>null,'city'=>null,'quyu'=>null,'address'=>null,'address_xq'=>null,'code'=>null,'uid'=>null,'is_default'=>null);
        }

        //4.计算运费
        $freight = $this->get_freight($store_id,$products, $address);

        $total = round(($products_total + $freight),2); // 商品总价+总运费

        $data = array('payment'=>$payment,'user_money'=>$user_money,'user_score'=>$user_score,'enterless'=>$enterless,'password_status'=>$password_status,'freight'=>$freight,'addemt'=>$addemt,'address'=>$address,'money'=>$products_total,'products'=>$products,'total'=>$total);
        $message = Lang("Success");
        echo json_encode(array('code' => 200,'data'=>$data,'message'=>$message));
        exit;
    }

    // 拼团下单
    public function payment(&...$context)
    {
        $action = $context[0];
        //1.列出基础数据
        $user = $action->user;
        $user_id = $user['user_id'];
        $user_score = $user['score'];
        $store_id = $action->store_id;
        $store_type = $action->store_type;
        $Is_it_temporary = $action->Is_it_temporary; // false：不是临时订单   true：是临时订单

        $address_id = addslashes(trim(Request::param('address_id'))); // 地址ID
        $productsInfo = addslashes(trim(Request::param('productsInfo'))); // 参数  {"pid":"4906","cid":1870,"num":2,"acId":"1674334035863666688"}
        $remarks = addslashes(trim(Request::param('remarks'))); // 备注
        $order_type = addslashes(trim(Request::param('pay_type'))); // 类型

        $productsInfo1 = htmlspecialchars_decode($productsInfo);
        $productsInfo2 = json_decode(stripslashes(html_entity_decode($productsInfo1)),true); // 字符串打散为数组

        $pid = $productsInfo2['pid']; // 商品ID
        $cid = $productsInfo2['cid']; // 属性ID 
        $num = $productsInfo2['num']; // 开团人数
        $acId = ''; // 活动ID
        $openId = ''; // 开团ID
        if(isset($productsInfo2['acId']))
        {
            $acId = $productsInfo2['acId']; // 活动ID
        }

        if(isset($productsInfo2['openId']))
        {
            $openId = $productsInfo2['openId']; // 开团ID

            $sql_0 = "select activity_id from lkt_group_open where id = '$openId' ";
            $r_0 = Db::query($sql_0);
            $acId = $r_0[0]['activity_id'];
        }

        $openDiscount = 0;
        $canDiscount = 0;
        $sql_config = "select goods_limit,team_limit,team_rule,end_date from lkt_group_activity where store_id = '$store_id' and id = '$acId' ";
        $r_config = Db::query($sql_config);
        if($r_config)
        {
            $goods_limit = $r_config[0]['goods_limit']; // 同一商品参团限制开关
            $end_date = $r_config[0]['end_date']; // 拼团活动结束时间
            $team_limit = $r_config[0]['team_limit']; // 团长是否可参团
            $team_rule = json_decode($r_config[0]['team_rule'],true);
            foreach($team_rule as $k_rule => $v_rule)
            {
                if($num == $v_rule['num'])
                {
                    $openDiscount = $v_rule['openDiscount'] / 100;
                    $canDiscount = $v_rule['canDiscount'] / 100;
                }
            }
        }
        else
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }

        $time = date("Y-m-d H:i:s");

        $product = $this->products_list($store_id,$pid, $cid);

        $total = 0;
        $products_total = 0;

        $products_data = $this->get_products_data($store_id,$productsInfo2, $products_total);
        $products_total = $products_data['products_total'];
        $products = $products_data['products'];

        $Tools = new Tools( $store_id, 1);

        $mch_id = $products['mch_id'];
        $attr_price = $products['price'];

        $mch_id_str = ',' . $mch_id . ',';
        $r_config = GroupConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('end_time')->select()->toArray();
        if($r_config)
        { // 设置了拼团设置
            $daojishi = $r_config[0]['end_time'];
            $end_time = date("Y-m-d H:i:s",strtotime("+ $daojishi second",time()));
            if($end_date <= $end_time)
            { // 拼团活动结束时间 <= 拼团结束时间
                $end_time = $end_date;
            }
        }
        else
        { // 没有设置拼团设置
            $message = Lang('Abnormal business');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }

        $pay = 'wallet_pay';

        $order_failure_time = Tools::Obtain_expiration_time(array('store_id'=>$store_id,'otype'=>'PT'));

        Db::startTrans();
        if($Is_it_temporary)
        {
            if(isset($productsInfo2['pay']))
            {
                $pay = $productsInfo2['pay']; // 支付方式
            }
            $no_delivery_str = $this->No_distribution_Province($store_id, $products);

            // 查询默认地址order_details
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
    
            //4.计算运费
            $freight = $this->get_freight($store_id,$products, $address);

            $total = round(($products_total + $freight),2); // 商品总价+总运费
            
            $trade_no = $Tools->Generate_order_number('PT', 'real_sno'); // 生成支付订单号

            if($openId == '')
            { // 开团
                if($team_limit == 1)
                { // 团长可以参团
                    $this->Deduction_of_inventory($store_id,$pid,$cid,$user_id,$mch_id); // 扣除库存
                }
                $data = json_encode(array('acId'=>$acId,'z_price'=>$total,'store_id'=>$store_id,'num'=>$num,'pay'=>$pay,'pid'=>$pid,'userId'=>$user_id,'cid'=>$cid,'address_id'=>$address_id,'remarks'=>$remarks,'store_type'=>$store_type));
            }
            else
            { // 参团
                $this->Deduction_of_inventory($store_id,$pid,$cid,$user_id,$mch_id); // 扣除库存

                $data = json_encode(array('z_price'=>$total,'store_id'=>$store_id,'openId'=>$openId,'num'=>$num,'pay'=>$pay,'pid'=>$pid,'userId'=>$user_id,'cid'=>$cid,'address_id'=>$address_id,'remarks'=>$remarks,'store_type'=>$store_type));
            }
            $sql0 = "insert into lkt_order_data(trade_no,order_type,data,addtime,status,pay_type) value ('$trade_no','PT','$data','$time',0,'$pay')";
            $r0 = Db::execute($sql0);
            if($r0 > 0)
            {
                Db::commit();
                $data = array('sNo' => $trade_no,'total' => round($total,2));
                $message = Lang("Success");
                echo json_encode(array('code' => 200,'data'=>$data,'message'=>$message));
                exit;
            }
            else
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！sql：" . $sql0);
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }
        }
        else
        {
            if($openId == '')
            { // 开团ID为空（开团）
                $sNo0 = $Tools->Generate_order_number('KT', 'sNo'); // 生成拼团成功后的订单号
                $real_sno0 = $Tools->Generate_order_number('KT', 'real_sno'); // 生成支付订单号

                if($team_limit == 1)
                { // 团长可以参团
                    $no_delivery_str = $this->No_distribution_Province($store_id, $products);
                    
                    // 查询默认地址order_details
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
            
                    //4.计算运费
                    $freight = $this->get_freight($store_id,$products, $address);

                    $total = round(($products_total + $freight),2); // 商品总价+总运费

                    $sNo_0 = $Tools->Generate_order_number('PT', 'sNo'); // 生成支付订单号
                    $real_sno_0 = $Tools->Generate_order_number('PT', 'real_sno'); // 生成支付订单号
                    // 添加拼团订单
                    $sql_0 = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'num'=>1,'z_price'=>$total,'sNo'=>$sNo_0,'shop_cpc'=>$shop_cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_xq,'code'=>$code,'remark'=>'','pay'=>'','add_time'=>$time,'status'=>0,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>0,'spz_price'=>$products_total,'reduce_price'=>0,'coupon_price'=>0,'source'=>$store_type,'otype'=>'PT','mch_id'=>$mch_id_str,'bargain_id'=>0,'comm_discount'=>0,'remarks'=>$remarks,'real_sno'=>$real_sno_0,'self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','grade_rate'=>'0','grade_fan'=>'','z_freight'=>$freight,'preferential_amount'=>0,'single_store'=>0,'grade_score'=>0,'p_sNo'=>'','drawid'=>$acId,'order_failure_time'=>$order_failure_time);
                    $r_0 = Db::name('order')->insertGetId($sql_0);
                    if ($r_0 < 1)
                    { // 添加失败
                        $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数：" . json_encode($sql_0));
                        Db::rollback();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }
                    // 添加拼团订单详情
                    $sql_1 = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pid,'p_name'=>$products['product_title'],'p_price'=>$products_total,'num'=>1,'unit'=>$products['unit'],'r_sNo'=>$sNo_0,'add_time'=>$time,'r_status'=>0,'size'=>$products['size'],'sid'=>$cid,'freight'=>$freight,'coupon_id'=>0,'after_discount'=>$products_total);
                    $r_1 = Db::name('order_details')->insert($sql_1);
                    if ($r_1 < 1)
                    { // 添加失败
                        $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数：" . json_encode($sql_1));
                        Db::rollback();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }

                    // 添加开团信息
                    $sql0 = array('activity_id'=>$acId,'goods_id'=>$pid,'attr_id'=>$cid,'user_id'=>$user_id,'team_num'=>$num,'team_commission'=>0,'status'=>0,'sno'=>$sNo0,'is_settlement'=>0,'add_date'=>$time,'end_date'=>$end_time);
                    $r0 = Db::name('group_open')->insertGetId($sql0);
                    if($r0 < 1)
                    {
                        $this->Log(__METHOD__ . ":" . __LINE__ . "添加开团信息失败！参数：" . json_encode($sql0));
                        Db::rollback();
                        $message = Lang('nomal_order.6');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }

                    // 添加开团详情信息
                    $sql1 = array('open_id'=>$r0,'goods_id'=>$pid,'attr_id'=>$cid,'attr_price'=>$attr_price,'price'=>$products_total,'user_id'=>$user_id,'sno'=>$sNo_0,'add_date'=>$time);
                    $r1 = Db::name('group_open_record')->insert($sql1);
                    if ($r1 < 1)
                    { // 添加失败
                        $this->Log(__METHOD__ . ":" . __LINE__ . "添加开团详情信息失败！参数：" . json_encode($sql1));
                        Db::rollback();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }

                    $sql2 = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pid,'p_name'=>$products['product_title'],'p_price'=>$products_total,'num'=>1,'unit'=>$products['unit'],'r_sNo'=>$sNo0,'add_time'=>$time,'r_status'=>0,'size'=>$products['size'],'sid'=>$cid,'freight'=>$freight,'coupon_id'=>0,'after_discount'=>$products_total);
                    $r2 = Db::name('order_details')->insert($sql2);
                    if ($r2 < 1)
                    { // 添加失败
                        $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数：" . json_encode($sql2));
                        Db::rollback();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }

                    $sql3 = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'num'=>1,'z_price'=>$total,'sNo'=>$sNo0,'shop_cpc'=>$shop_cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_xq,'code'=>$code,'remark'=>'','pay'=>'','add_time'=>$time,'status'=>0,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>0,'spz_price'=>$products_total,'reduce_price'=>0,'coupon_price'=>0,'source'=>$store_type,'otype'=>'KT','mch_id'=>$mch_id_str,'bargain_id'=>0,'comm_discount'=>0,'remarks'=>$remarks,'real_sno'=>$real_sno0,'self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','grade_rate'=>'0','grade_fan'=>'','z_freight'=>$freight,'preferential_amount'=>0,'single_store'=>0,'grade_score'=>0,'p_sNo'=>$sNo_0,'drawid'=>$acId,'order_failure_time'=>$order_failure_time);
                    $r3 = Db::name('order')->insertGetId($sql3);
                    if ($r3 < 1)
                    { // 添加失败
                        $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数：" . json_encode($sql3));
                        Db::rollback();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }

                    $this->Deduction_of_inventory($store_id,$pid,$cid,$user_id,$mch_id); // 扣除库存
                }
                else
                { // 团长不可以参团
                    $team_commission = round(($products['price'] * $canDiscount * $openDiscount),2);
                    // 添加开团信息
                    $sql0 = array('activity_id'=>$acId,'goods_id'=>$pid,'attr_id'=>$cid,'user_id'=>$user_id,'team_num'=>$num,'team_commission'=>$team_commission,'status'=>0,'sno'=>$sNo0,'is_settlement'=>0,'add_date'=>$time,'end_date'=>$end_time);
                    $r0 = Db::name('group_open')->insertGetId($sql0);
                    if($r0 < 1)
                    {
                        $this->Log(__METHOD__ . ":" . __LINE__ . "添加开团信息失败！参数：" . json_encode($sql0));
                        Db::rollback();
                        $message = Lang('nomal_order.6');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }

                    $sql2 = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pid,'p_name'=>$products['product_title'],'p_price'=>0,'num'=>1,'unit'=>$products['unit'],'r_sNo'=>$sNo0,'add_time'=>$time,'r_status'=>1,'size'=>$products['size'],'sid'=>$cid,'freight'=>0,'coupon_id'=>0,'after_discount'=>0);
                    $r2 = Db::name('order_details')->insert($sql2);
                    if ($r2 < 1)
                    { // 添加失败
                        $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数：" . json_encode($sql2));
                        Db::rollback();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }

                    $sql3 = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>'','mobile'=>'','num'=>1,'z_price'=>0,'sNo'=>$sNo0,'sheng'=>'','shi'=>'','xian'=>'','address'=>'','remark'=>'','pay'=>'','add_time'=>$time,'status'=>1,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>0,'spz_price'=>0,'reduce_price'=>0,'coupon_price'=>0,'source'=>$store_type,'otype'=>'KT','mch_id'=>$mch_id_str,'bargain_id'=>0,'comm_discount'=>0,'remarks'=>$remarks,'real_sno'=>$real_sno0,'self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','grade_rate'=>'0','grade_fan'=>'','z_freight'=>0,'preferential_amount'=>0,'single_store'=>0,'grade_score'=>0,'p_sNo'=>'','drawid'=>$acId,'order_failure_time'=>$order_failure_time);
                    $r3 = Db::name('order')->insertGetId($sql3);
                    if ($r3 < 1)
                    { // 添加失败
                        $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数：" . json_encode($sql3));
                        Db::rollback();
                        $message = Lang('nomal_order.3');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }
                }
                Db::commit();
                $data = array('order_id' => $r3,'sNo' => $sNo0,'total' => round($total,2),'orderTime'=>$time,'openId'=>$r0);
                $message = Lang("Success");
                echo json_encode(array('code' => 200,'data'=>$data,'message'=>$message));
                exit;
            }
            else
            { // 开团ID不为空（参团）
                $no_delivery_str = $this->No_distribution_Province($store_id, $products);

                // 查询默认地址order_details
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
        
                //4.计算运费
                $freight = $this->get_freight($store_id,$products, $address);

                $total = round(($products_total + $freight),2); // 商品总价+总运费

                $sNo_0 = $Tools->Generate_order_number('PT', 'sNo'); // 生成支付订单号
                $real_sno_0 = $Tools->Generate_order_number('PT', 'real_sno'); // 生成支付订单号
                // 添加拼团订单
                $sql_0 = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'num'=>1,'z_price'=>$total,'sNo'=>$sNo_0,'shop_cpc'=>$shop_cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_xq,'code'=>$code,'remark'=>'','pay'=>'','add_time'=>$time,'status'=>0,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>0,'spz_price'=>$products_total,'reduce_price'=>0,'coupon_price'=>0,'source'=>$store_type,'otype'=>'PT','mch_id'=>$mch_id_str,'bargain_id'=>0,'comm_discount'=>0,'remarks'=>$remarks,'real_sno'=>$real_sno_0,'self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','grade_rate'=>'0','grade_fan'=>'','z_freight'=>$freight,'preferential_amount'=>0,'single_store'=>0,'grade_score'=>0,'p_sNo'=>'','drawid'=>$acId,'order_failure_time'=>$order_failure_time);
                $r_0 = Db::name('order')->insertGetId($sql_0);
                if ($r_0 < 1)
                { // 添加失败
                    $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数：" . json_encode($sql_0));
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }
                // 添加拼团订单详情
                $sql_1 = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pid,'p_name'=>$products['product_title'],'p_price'=>$products_total,'num'=>1,'unit'=>$products['unit'],'r_sNo'=>$sNo_0,'add_time'=>$time,'r_status'=>0,'size'=>$products['size'],'sid'=>$cid,'freight'=>$freight,'coupon_id'=>0,'after_discount'=>$products_total);
                $r_1 = Db::name('order_details')->insert($sql_1);
                if ($r_1 < 1)
                { // 添加失败
                    $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数：" . json_encode($sql_1));
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }

                // 添加开团详情信息
                $sql1 = array('open_id'=>$openId,'goods_id'=>$pid,'attr_id'=>$cid,'attr_price'=>$attr_price,'price'=>$products_total,'user_id'=>$user_id,'sno'=>$sNo_0,'add_date'=>$time);
                $r1 = Db::name('group_open_record')->insert($sql1);
                if ($r1 < 1)
                { // 添加失败
                    $this->Log(__METHOD__ . ":" . __LINE__ . "添加开团详情信息失败！参数：" . json_encode($sql1));
                    Db::rollback();
                    $message = Lang('nomal_order.3');
                    echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                    exit;
                }

                $this->Deduction_of_inventory($store_id,$pid,$cid,$user_id,$mch_id); // 扣除库存

                Db::commit();
                $data = array('order_id' => $r_0,'sNo' => $sNo_0,'total' => round($total,2),'orderTime'=>$time,'openId'=>$openId);
                $message = Lang("Success");
                echo json_encode(array('code' => 200,'data'=>$data,'message'=>$message));
                exit;
            }
        }
    }

    // 查询商品是否有库存
    public function products_list($store_id,$pid, $cid)
    {
        $products = [];

        $r0 = ProductListModel::where('id',$pid)->field('status')->select()->toArray();
        if ($r0)
        {
            $status = $r0[0]['status'];
            if ($status == 1)
            {
                $message = Lang('tools.13');
                echo json_encode(array('code' => ERROR_CODE_SPWSJ, 'message' => $message));
                exit;
            }
            $r1 = ConfigureModel::where(['pid'=>$pid,'id'=>$cid])->field('num')->select()->toArray();
            if ($r1)
            {
                $num = $r1[0]['num'];
                if ($num < 1)
                {
                    $message = Lang('tools.14');
                    echo json_encode(array('code' => ERROR_CODE_KCBZ, 'message' => $message));
                    exit;
                }
                $products = array('pid' => $pid, 'cid' => $cid, 'num' => 1);
            }
            else
            {
                $message = Lang('Parameter error');
                echo json_encode(array('code' => ERROR_CODE_SPCSCW, 'message' => $message));
                exit;
            }
        }
        else
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => ERROR_CODE_SPCSCW, 'message' => $message));
            exit;
        }

        if (empty($products))
        {
            $message = Lang('tools.12');
            echo json_encode(array('code' => ERROR_CODE_LBYSPKCBZ, 'message' => $message));
            exit;
        }

        return $products;
    }

    // 订单商品数据分组处理
    public function get_products_data($store_id,$productsInfo, $products_total)
    {
        $pid = $productsInfo['pid']; // 商品ID
        $cid = $productsInfo['cid']; // 属性ID 
        $num = $productsInfo['num']; // 开团人数

        $products = array();
        $acId = ''; // 活动ID
        $openId = ''; // 开团ID
        if(isset($productsInfo['acId']))
        {
            $acId = $productsInfo['acId']; // 活动ID
        }

        if(isset($productsInfo['openId']))
        {
            $openId = $productsInfo['openId']; // 开团ID

            $sql_0 = "select activity_id from lkt_group_open where id = '$openId' ";
            $r_0 = Db::query($sql_0);
            $acId = $r_0[0]['activity_id'];

            $products['openId'] = $openId;
        }

        $products['acId'] = $acId;
        $products['goodsId'] = $pid;
        $products['attrId'] = $cid;
        $products['num'] = 1;
        $sql0 = "select status,team_rule,start_date,end_date,team_limit from lkt_group_activity where store_id = '$store_id' and id = '$acId' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $products['status'] = $r0[0]['status']; // 拼团状态 0=未开始 1=拼团中 2=已结束
            $products['start_date'] = $r0[0]['start_date']; // 开始时间
            $products['end_date'] = $r0[0]['end_date']; // 结束时间
            $products['team_limit'] = $r0[0]['team_limit']; // 团长是否可参团

            $team_rule = json_decode($r0[0]['team_rule'],true); // 团长参团规则
        }

        $sql1 = "select id from lkt_group_goods where activity_id = '$acId' and goods_id = '$pid' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $products['groupGoodsId'] = $r1[0]['id'];
        }

        $sql1 = "select m.product_title,m.weight,m.weight_unit,m.mch_id,m.status,m.freight,m.content,c.img as attrImg,c.attribute,c.num as stockNum,c.price,c.unit from lkt_product_list AS m LEFT JOIN lkt_configure AS c ON m.id = c.pid  where m.store_id = '$store_id' and c.num > 0 and m.status = 2 and m.id = '$pid' and c.id = '$cid'";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $mch_id = $r1[0]['mch_id'];
            $price = $r1[0]['price'];
            $attribute = unserialize($r1[0]['attribute']);
            $products['product_title'] = $r1[0]['product_title'];
            $products['weight'] = $r1[0]['weight'];
            $products['weight_unit'] = $r1[0]['weight_unit'];
            $products['mch_id'] = $r1[0]['mch_id'];
            $products['goodsStatus'] = $r1[0]['status'];
            $products['freight'] = $r1[0]['freight'];
            $products['content'] = $r1[0]['content'];
            $products['imgurl'] = ServerPath::getimgpath($r1[0]['attrImg']); /* 拼接图片链接*/
            $products['price'] = round($r1[0]['price'],2);
            $products['unit'] = $r1[0]['unit'];

            $size = '';
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
            $products['size'] = $size;

            if ($mch_id != 0)
            {
                $mch_list = PC_Tools::StoreData($store_id,$mch_id,'2,3');
                $products['collection_num'] = $mch_list['collection_num']; // 收藏数量
                $products['quantity_on_sale'] = $mch_list['quantity_on_sale']; // 在售数量
                $products['quantity_sold'] = $mch_list['quantity_sold']; // 已售数量
                
                $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name,logo,head_img')->select()->toArray();
                if ($r_mch)
                {
                    $products['mchName'] = $r_mch[0]['name']; // 店铺名称
                    $products['shop_name'] = $r_mch[0]['name']; // 店铺名称
                    $products['mchImg'] = $r_mch[0]['head_img']; // 店铺头像
                    $products['shop_logo'] = ServerPath::getimgpath($r_mch[0]['logo'],$store_id); // logo
                }
            }

            foreach($team_rule as $k => $v)
            {
                $man = $v['num'];
                $openDiscount = $v['openDiscount']; // 开团比例
                $canDiscount = $v['canDiscount']; // 参团比例
                if($man == $num)
                {
                    if($openId == '')
                    { // 开团
                        $products_total = round(($price * $openDiscount / 100),2);
                    }
                    else
                    { // 参团
                        $products_total = round(($price * $canDiscount / 100),2);
                    }
                }
            }
        }

        return array('products' => $products,'products_total' => $products_total);
    }

    // 获取不配送省的名称
    public static function No_distribution_Province($store_id,$products)
    {
        $mch_id = $products['mch_id'];
        $freight_id = $products['freight'];
        $no_delivery_list = array();
        $r0 = FreightModel::where(['store_id'=>$store_id,'id'=>$freight_id,'mch_id'=>$mch_id])->field('no_delivery')->select()->toArray();
        if($r0)
        {
            $no_delivery = $r0[0]['no_delivery']; // 不配送

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

        return json_encode($no_delivery_list);
    }

    // 获取运费
    public function get_freight($store_id,$products, $address)
    {
        $yunfei = 0;
        $city = '';
        // 防止空地址报错
        if (!empty($address))
        {
            $sheng = $address['sheng'];
            $city = $address['city'];
            $quyu = $address['quyu'];
            $city = $sheng . '-' . $city . '-' . $quyu; 
        }

        $freight_id = $products['freight'];
        $weight = $products['weight'];
        $weight_unit = $products['weight_unit'];

        $DefaultState = true; // 需要获取默认运费

        $r_1 = FreightModel::where(['store_id'=>$store_id,'id'=>$freight_id])->select()->toArray();
        if ($r_1)
        {
            $type = $r_1[0]['type']; // 类型 0:件 1:重量
            $rule_1 = $r_1[0]['freight']; // 运费规则
            $default_freight = json_decode($r_1[0]['default_freight'],true); // 默认运费规则

            $rule_2 = array();
            if($rule_1 != '')
            {
                $rule_2 = json_decode($rule_1,true); // 运费规则
            }

            foreach ($rule_2 as $key => $value)
            { // 循环指定运费规则
                $name = explode(',',$value['name']); // 指定运费地区
                if(in_array($city,$name))
                { // 收货地址，存在指定运费地区
                    $DefaultState = false; // 不用获取默认运费
                    $yfmb = $this->SpecifiedFreight($weight,$weight_unit,$type,$value);
                    
                    $yunfei += $yfmb;
                    break;
                }
            }

            if($DefaultState)
            {
                $yfmb = $this->DefaultFreight($weight,$weight_unit,$type,$default_freight);
                $yunfei += $yfmb;
            }
        }
        return $yunfei;
    }

    // 获取指定运费规则运费
    public function SpecifiedFreight($weight,$weight_unit,$type,$f_list)
    {
        $merge_num = 1;
        if($weight_unit == 'g')
        {
            $weight = $weight / 1000;
        }
        $First_article = $f_list['one']; // 首件\首重
        $First_article_freight = $f_list['freight']; // 首件\首重运费
        $Continuation = $f_list['two']; // 续件\续重
        $Continuation_freight = $f_list['Tfreight']; // 续件\续重运费

        $yfmb = 0;
        if($type == 1)
        { // 重量
            $z_weight = round(round($weight,2) * (int)$merge_num,2);
            if($z_weight <= $First_article)
            { // 商品重量 <= 首重重量
                $yfmb = $First_article_freight;
            }
            else
            {
                $yfmb = $First_article_freight;
                $weight_0 = $z_weight - $First_article; // 商品重量 - 首件重量 = 剩余商品重量
                if($weight_0 <= $Continuation)
                { // 剩余商品重量 <= 续重运费
                    $yfmb += $Continuation_freight;
                }
                else
                {
                    $num_1 = ceil($weight_0 / $Continuation); // 多余重量 / 续件重量（向上取整）
                    $yfmb += $Continuation_freight * $num_1;
                }
            }
        }
        else
        {
            if($merge_num <= $First_article)
            { // 商品购买数量 <= 首件数量
                $yfmb = $First_article_freight;
            }
            else
            {
                $yfmb = $First_article_freight;
                $num_0 = (int)$merge_num - (int)$First_article; // 商品购买数量 - 首件数量 = 剩余商品数量

                if($num_0 <= $Continuation)
                { // 剩余商品数量 <= 续件数量
                    $yfmb += $Continuation_freight;
                }
                else
                {
                    $num_1 = ceil((int)$num_0 / (int)$Continuation); // 多余数量 / 续件数量（向上取整）
                    $yfmb += $Continuation_freight * $num_1;
                }
            }
        }

        return $yfmb;
    }

    // 获取默认运费规则运费
    public function DefaultFreight($weight,$weight_unit,$type,$default_freight)
    {
        $merge_num = 1;
        if($weight_unit == 'g')
        {
            $weight = $weight / 1000;
        }
        $First_article = $default_freight['num1']; // 首件\首重
        $First_article_freight = $default_freight['num2']; // 首件\首重运费
        $Continuation = $default_freight['num3']; // 续件\续重
        $Continuation_freight = $default_freight['num4']; // 续件\续重运费

        $yfmb = 0;
        if($type == 1)
        { // 重量
            $z_weight = round(round($weight,2) * (int)$merge_num,2);
            if($z_weight <= $First_article)
            { // 商品重量 <= 首重重量
                $yfmb = $First_article_freight;
            }
            else
            {
                $yfmb = $First_article_freight;
                $weight_0 = $z_weight - $First_article; // 商品重量 - 首件重量 = 剩余商品重量
                if($weight_0 <= $Continuation)
                { // 剩余商品重量 <= 续重运费
                    $yfmb += $Continuation_freight;
                }
                else
                {
                    $num_1 = ceil($weight_0 / $Continuation); // 多余重量 / 续件重量（向上取整）
                    $yfmb += $Continuation_freight * $num_1;
                }
            }
        }
        else
        {
            if($merge_num <= $First_article)
            { // 商品购买数量 <= 首件数量
                $yfmb = $First_article_freight;
            }
            else
            {
                $yfmb = $First_article_freight;
                $num_0 = (int)$merge_num - (int)$First_article; // 商品购买数量 - 首件数量 = 剩余商品数量
                if($num_0 <= $Continuation)
                { // 剩余商品数量 <= 续件数量
                    $yfmb += $Continuation_freight;
                }
                else
                {
                    $num_1 = ceil((int)$num_0 / (int)$Continuation); // 多余数量 / 续件数量（向上取整）
                    $yfmb += $Continuation_freight * $num_1;
                }
            }
        }
        return $yfmb;
    }
    
    // 扣除库存
    public function Deduction_of_inventory($store_id,$pid,$cid,$user_id,$mch_id)
    {
        $time = date("Y-m-d H:i:s");
        // 根据商品ID、属性ID，查询属性数据
        $r4 = ConfigureModel::where(['id'=>$cid,'pid'=>$pid])->field('num,min_inventory')->select()->toArray();
        $total_num = $r4[0]['num']; // 剩余库存
        $min_inventory = $r4[0]['min_inventory']; // 预警值

        // 根据商品ID，修改商品库存
        $r5 = Db::name('product_list')->where('id',$pid)->update(['num' => Db::raw('num - 1')]);
        if ($r5 <= 0)
        {
            $this->Log(__METHOD__ . ":" . __LINE__ . "修改商品库存失败！pid:" . $pid);
            Db::rollback();
            $message = Lang('nomal_order.3');
            echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }

        // 根据属性ID，修改属性库存
        $r6 = Db::name('configure')->where('id',$cid)->update(['num' => Db::raw('num - 1')]);
        if ($r6 <= 0)
        {
            $this->Log(__METHOD__ . ":" . __LINE__ . "修改商品属性库存失败！cid:" . $cid);
            Db::rollback();
            $message = Lang('nomal_order.3');
            echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }

        $content = $user_id . '生成【拼团】订单所需1';
        $sql_stock_insert = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>1,'type'=>1,'user_id'=>$user_id,'add_date'=>$time,'content'=>$content,'mch_id'=>$mch_id);
        $r_stock = Db::name('stock')->insert($sql_stock_insert);

        if($total_num - 1 <= $min_inventory)
        {
            $content1 = '预警';
            $sql_stock_insert1 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>$min_inventory,'type'=>2,'add_date'=>$time,'content'=>$content1,'mch_id'=>$mch_id);
            $r_stock1 = Db::name('stock')->insert($sql_stock_insert1);

            $message_9 = "商品ID为".$pid."的商品库存不足，请尽快补充库存";
            $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>9,'parameter'=>$cid,'content'=>$message_9);
            PC_Tools::add_message_logging($message_logging_list9);
        }

        return;
    }

    // 回滚库存
    public function Rolling_back_inventory($store_id,$pid,$cid)
    {
        $time = date("Y-m-d H:i:s");

        $res = 1;
        // 根据商品ID、属性ID，查询属性数据
        $r4 = ConfigureModel::where(['id'=>$cid,'pid'=>$pid])->field('num,min_inventory')->select()->toArray();
        $total_num = $r4[0]['num']; // 剩余库存

        // 根据商品ID，修改商品库存
        $r5 = Db::name('product_list')->where('id',$pid)->update(['num' => Db::raw('num + 1')]);
        if ($r5 <= 0)
        {
            $res = 0;
        }

        // 根据属性ID，修改属性库存
        $r6 = Db::name('configure')->where('id',$cid)->update(['num' => Db::raw('num + 1')]);
        if ($r6 <= 0)
        {
            $res = 0;
        }

        $content = '拼团失败，回退库存1';
        $sql_stock_insert = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$cid,'total_num'=>$total_num,'flowing_num'=>1,'type'=>0,'add_date'=>$time,'content'=>$content);
        $r_stock = Db::name('stock')->insert($sql_stock_insert);

        return $res;
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

        $sql = "select data,pay_type from lkt_order_data where trade_no = '$trade_no' ";
        $r = Db::query($sql);
        if (!$r)
        {
            $this->Log(__METHOD__ . ":" . __LINE__ . "查询临时订单失败！sql：" . $sql);
            ob_clean();
            echo 'error';
            exit;
        }
        $data = json_decode($r[0]['data'],true);
        $pay_type = $r[0]['pay_type'];

        $store_id = $data['store_id'];
        if ($pay_type == 'tt_alipay')
        {
            $pay_type = 'alipay';
        }
        $config = LKTConfigInfo::getPayConfig($store_id, $pay_type);
        if (empty($config))
        {
            $this->Log(__METHOD__ . ":" . __LINE__ . "普通订单执行日期：" . date('Y-m-d H:i:s') . "支付暂未配置 商城ID：" . $store_id . " ，支付类型：" . $pay_type . "，无法调起支付！");
            return 'file';
        }
        $this->Log(__METHOD__ . ":" . __LINE__ . '==><==' . json_encode($config));
        $this->Log(__METHOD__ . ":" . __LINE__ . 'oever');
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
            $this->Log(__METHOD__ . ":" . __LINE__ . "普通订单回调失败信息: \n 订单：$trade_no 支付金额：$total \r\n");
            ob_clean();
            echo 'error';
            exit;
        }
        
        $type = substr($trade_no, 0, 2);
        $this->Log(__METHOD__ . ":" . __LINE__ . "【trade_no1】''''$trade_no:\n\n");

        $sql = "select data,pay_type from lkt_order_data where trade_no = '$trade_no' ";
        $r = Db::query($sql);
        $data = json_decode($r[0]['data'],true);
        $this->Log(__METHOD__ . ":" . __LINE__ . "订单参数：" . $r[0]['data']);

        $sql1 = "select id from lkt_order where real_sno = '$trade_no' ";
        $r1 = Db::query($sql1);
        if($r1)
        {

        }
        else
        {
            if(isset($data['acId']))
            { // 开团
                $this->Opening_a_group($data,$trade_no);
            }
            else if(isset($data['openId']))
            { // 参团
                $this->Offered($data,$trade_no);
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
        $time = date("Y-m-d H:i:s");

        if($order_types == 'KT')
        {
            Db::startTrans();

            $sql0 = "select p_sNo from lkt_order where store_id = '$store_id' and user_id = '$user_id' and sNo = '$sNo' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $p_sNo = $r0[0]['p_sNo'];
            }

            $sql1 = "update lkt_order set status = 1,pay = 'wallet_pay',pay_time = '$time' where store_id = '$store_id' and user_id = '$user_id' and sNo = '$sNo' ";
            $r1 = Db::execute($sql1);
            if ($r1 <= 0)
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "修改订单状态败！sql：" . $sql1);
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            $sql2 = "update lkt_order_details set r_status = 1 where store_id = '$store_id' and user_id = '$user_id' and r_sNo = '$sNo' ";
            $r2 = Db::execute($sql2);
            if ($r2 <= 0)
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "修改订单详情状态败！sql：" . $sql2);
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            $sql3 = "update lkt_order set status = 1,pay = 'wallet_pay',pay_time = '$time' where store_id = '$store_id' and user_id = '$user_id' and sNo = '$p_sNo' ";
            $r3 = Db::execute($sql3);
            if ($r3 <= 0)
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "修改订单状态败！sql：" . $sql3);
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            $sql4 = "update lkt_order_details set r_status = 1 where store_id = '$store_id' and user_id = '$user_id' and r_sNo = '$p_sNo' ";
            $r4 = Db::execute($sql4);
            if ($r4 <= 0)
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "修改订单详情状态败！sql：" . $sql4);
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            Db::commit();
            return;
        }
        else
        {
            $action->gndd($store_id, $payment_money, $sNo, $user_id,$order_types);
            return;
        }
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
        // $real_sno = Tools::order_number($order_types);
        $real_sno = $sNo;

        $total = $payment_money;
        return array($real_sno, $total);
    }

    // 开团
    public function Opening_a_group($data,$trade_no)
    {
        $time = date("Y-m-d H:i:s");
        
        $store_id = $data['store_id']; // 商城ID
        $store_type = $data['store_type']; // 来源
        $total = $data['z_price']; // 订单价
        $num = $data['num']; // 团队人数
        $pay = $data['pay']; // 支付方式
        $pid = $data['pid']; // 商品ID
        $cid = $data['cid']; // 属性ID
        $user_id = $data['userId']; // 用户user_id
        $acId = $data['acId']; // 拼团活动ID
        $address_id = $data['address_id']; // 拼团活动ID
        $remarks = $data['remarks']; // 备注
        
        $sql_config = "select team_limit from lkt_group_activity where store_id = '$store_id' and id = '$acId' ";
        $r_config = Db::query($sql_config);
        if($r_config)
        {
            $team_limit = $r_config[0]['team_limit']; // 团长是否可参团
        }
        else
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }

        $productsInfo = array('pid'=>$pid,'cid'=>$cid,'num'=>$num,'acId'=>$acId);

        $products_total = 0;
        $products_data = $this->get_products_data($store_id,$productsInfo, $products_total);
        $products_total = $products_data['products_total'];
        $products = $products_data['products'];

        $mch_id = $products['mch_id'];
        $mch_id0 = ',' . $mch_id . ',';
        $attr_price = $products['price'];

        $r_config = GroupConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('end_time')->select()->toArray();
        if($r_config)
        { // 设置了拼团设置
            $daojishi = $r_config[0]['end_time'];
            $end_time = date("Y-m-d H:i:s",strtotime("+ $daojishi second",time()));
        }
        else
        { // 没有设置拼团设置
            $message = Lang('Abnormal business');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }

        $Tools = new Tools( $store_id, 1);
        $sNo0 = $Tools->Generate_order_number('KT', 'sNo'); // 生成拼团成功后的订单号
        $real_sno0 = $trade_no;

        $order_failure_time = Tools::Obtain_expiration_time(array('store_id'=>$store_id,'otype'=>'PT'));

        $this->Log(__METHOD__ . ":" . __LINE__ . "生成订单号：" . $sNo0);
        Db::startTrans();

        if($team_limit == 1)
        { // 团长可以参团
            $no_delivery_str = $this->No_distribution_Province($store_id, $products);

            // 查询默认地址order_details
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
    
            //4.计算运费
            $freight = $this->get_freight($store_id,$products, $address);

            $sNo_0 = $Tools->Generate_order_number('PT', 'sNo'); // 生成支付订单号
            $real_sno_0 = $Tools->Generate_order_number('PT', 'real_sno'); // 生成支付订单号
            // 添加拼团订单
            $sql_0 = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'num'=>1,'z_price'=>$total,'sNo'=>$sNo_0,'shop_cpc'=>$shop_cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_xq,'code'=>$code,'remark'=>'','pay'=>$pay,'add_time'=>$time,'status'=>1,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>0,'spz_price'=>$products_total,'reduce_price'=>0,'coupon_price'=>0,'source'=>$store_type,'otype'=>'PT','mch_id'=>$mch_id0,'bargain_id'=>0,'comm_discount'=>0,'remarks'=>$remarks,'real_sno'=>$real_sno_0,'self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','grade_rate'=>'0','grade_fan'=>'','z_freight'=>$freight,'preferential_amount'=>0,'single_store'=>0,'grade_score'=>0,'p_sNo'=>'','drawid'=>$acId,'order_failure_time'=>$order_failure_time);
            $r_0 = Db::name('order')->insertGetId($sql_0);
            if ($r_0 < 1)
            { // 添加失败
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数：" . json_encode($sql_0));
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }
            // 添加拼团订单详情
            $sql_1 = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pid,'p_name'=>$products['product_title'],'p_price'=>$products_total,'num'=>1,'unit'=>$products['unit'],'r_sNo'=>$sNo_0,'add_time'=>$time,'r_status'=>1,'size'=>$products['size'],'sid'=>$cid,'freight'=>$freight,'coupon_id'=>0,'after_discount'=>$products_total);
            $r_1 = Db::name('order_details')->insert($sql_1);
            if ($r_1 < 1)
            { // 添加失败
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数：" . json_encode($sql_1));
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            // 添加开团信息
            $sql0 = array('activity_id'=>$acId,'goods_id'=>$pid,'attr_id'=>$cid,'user_id'=>$user_id,'team_num'=>$num,'team_commission'=>0,'status'=>0,'sno'=>$sNo0,'is_settlement'=>0,'add_date'=>$time,'end_date'=>$end_time);
            $r0 = Db::name('group_open')->insertGetId($sql0);
            if($r0 < 1)
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加开团信息失败！参数：" . json_encode($sql0));
                Db::rollback();
                $message = Lang('nomal_order.6');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            // 添加开团详情信息
            $sql1 = array('open_id'=>$r0,'goods_id'=>$pid,'attr_id'=>$cid,'attr_price'=>$attr_price,'price'=>$products_total,'user_id'=>$user_id,'sno'=>$sNo_0,'add_date'=>$time);
            $r1 = Db::name('group_open_record')->insert($sql1);
            if ($r1 < 1)
            { // 添加失败
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加开团详情信息失败！参数：" . json_encode($sql1));
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            $sql2 = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pid,'p_name'=>$products['product_title'],'p_price'=>$products_total,'num'=>1,'unit'=>$products['unit'],'r_sNo'=>$sNo0,'add_time'=>$time,'r_status'=>1,'size'=>$products['size'],'sid'=>$cid,'freight'=>$freight,'coupon_id'=>0,'after_discount'=>$products_total);
            $r2 = Db::name('order_details')->insert($sql2);
            if ($r2 < 1)
            { // 添加失败
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数：" . json_encode($sql2));
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            $sql3 = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'num'=>1,'z_price'=>$total,'sNo'=>$sNo0,'shop_cpc'=>$shop_cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_xq,'code'=>$code,'remark'=>'','pay'=>$pay,'add_time'=>$time,'status'=>1,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>0,'spz_price'=>$products_total,'reduce_price'=>0,'coupon_price'=>0,'source'=>$store_type,'otype'=>'KT','mch_id'=>$mch_id0,'bargain_id'=>0,'comm_discount'=>0,'remarks'=>$remarks,'real_sno'=>$real_sno0,'self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','grade_rate'=>'0','grade_fan'=>'','z_freight'=>$freight,'preferential_amount'=>0,'single_store'=>0,'grade_score'=>0,'p_sNo'=>$sNo_0,'drawid'=>$acId,'order_failure_time'=>$order_failure_time);
            $r3 = Db::name('order')->insertGetId($sql3);
            if ($r3 < 1)
            { // 添加失败
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数：" . json_encode($sql3));
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }
        }
        else
        { // 团长不可以参团
            // 添加开团信息
            $sql0 = array('activity_id'=>$acId,'goods_id'=>$pid,'attr_id'=>$cid,'user_id'=>$user_id,'team_num'=>$num,'team_commission'=>0,'status'=>0,'sno'=>$sNo0,'is_settlement'=>0,'add_date'=>$time,'end_date'=>$end_time);
            $r0 = Db::name('group_open')->insertGetId($sql0);
            if($r0 < 1)
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加开团信息失败！参数：" . json_encode($sql0));
                Db::rollback();
                $message = Lang('nomal_order.6');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            $sql2 = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pid,'p_name'=>$products['product_title'],'p_price'=>0,'num'=>1,'unit'=>$products['unit'],'r_sNo'=>$sNo0,'add_time'=>$time,'r_status'=>1,'size'=>$products['size'],'sid'=>$cid,'freight'=>0,'coupon_id'=>0,'after_discount'=>0);
            $r2 = Db::name('order_details')->insert($sql2);
            if ($r2 < 1)
            { // 添加失败
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数：" . json_encode($sql2));
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }

            $sql3 = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>'','mobile'=>'','num'=>1,'z_price'=>0,'sNo'=>$sNo0,'sheng'=>'','shi'=>'','xian'=>'','address'=>'','remark'=>'','pay'=>$pay,'add_time'=>$time,'status'=>0,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>0,'spz_price'=>0,'reduce_price'=>0,'coupon_price'=>0,'source'=>$store_type,'otype'=>'KT','mch_id'=>$mch_id0,'bargain_id'=>0,'comm_discount'=>0,'remarks'=>$remarks,'real_sno'=>$real_sno0,'self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','grade_rate'=>'0','grade_fan'=>'','z_freight'=>0,'preferential_amount'=>0,'single_store'=>0,'grade_score'=>0,'p_sNo'=>'','drawid'=>$acId,'order_failure_time'=>$order_failure_time);
            $r3 = Db::name('order')->insertGetId($sql3);
            if ($r3 < 1)
            { // 添加失败
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数：" . json_encode($sql3));
                Db::rollback();
                $message = Lang('nomal_order.3');
                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                exit;
            }
        }
        Db::commit();
        return;
    }

    // 参团
    public function Offered($data,$trade_no)
    {
        $time = date("Y-m-d H:i:s");
        
        $store_id = $data['store_id']; // 商城ID
        $store_type = $data['store_type']; // 来源
        $total = $data['z_price']; // 订单价
        $num = $data['num']; // 团队人数
        $pay = $data['pay']; // 支付方式
        $pid = $data['pid']; // 商品ID
        $cid = $data['cid']; // 属性ID
        $user_id = $data['userId']; // 用户user_id
        $openId = $data['openId']; // 开团ID
        $address_id = $data['address_id']; // 拼团活动ID
        $remarks = $data['remarks']; // 备注

        $sql_0 = "select activity_id,team_num from lkt_group_open where id = '$openId' ";
        $r_0 = Db::query($sql_0);
        $acId = $r_0[0]['activity_id'];
        $team_num = $r_0[0]['team_num']; // 成团人数

        $Offered_num = 1;
        $user_id_array = array();
        $sql_pt1 = "select user_id from lkt_group_open_record where open_id = '$openId' and recycle = 0 ";
        $r_pt1 = Db::query($sql_pt1);
        if($r_pt1)
        {
            $Offered_num = $Offered_num + count($r_pt1); // 已经参团人数
            $user_id_array = $r_pt1;
        }
        $user_id_array[] = array('user_id'=>$user_id);
        
        $this->Log(__METHOD__ . ":" . __LINE__ . "成团人数：" . $team_num);
        $this->Log(__METHOD__ . ":" . __LINE__ . "已参团人数：" . $Offered_num);
        $this->Log(__METHOD__ . ":" . __LINE__ . "已参团人员：" .  json_encode($user_id_array));

        $productsInfo = array('pid'=>$pid,'cid'=>$cid,'num'=>$num,'openId'=>$openId);

        $products_total = 0;
        $products_data = $this->get_products_data($store_id,$productsInfo, $products_total);
        $products_total = $products_data['products_total'];
        $products = $products_data['products'];

        $mch_id = $products['mch_id'];
        $mch_id0 = ',' . $mch_id . ',';
        $attr_price = $products['price'];

        $no_delivery_str = $this->No_distribution_Province($store_id, $products);

        // 查询默认地址order_details
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

        //4.计算运费
        $freight = $this->get_freight($store_id,$products, $address);

        $Tools = new Tools( $store_id, 1);
        $sNo_0 = $Tools->Generate_order_number('PT', 'sNo'); // 生成支付订单号
        $real_sno_0 = $trade_no;

        $order_failure_time = Tools::Obtain_expiration_time(array('store_id'=>$store_id,'otype'=>'PT'));

        Db::startTrans();

        // 添加拼团订单
        $sql_0 = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'num'=>1,'z_price'=>$total,'sNo'=>$sNo_0,'shop_cpc'=>$shop_cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_xq,'code'=>$code,'remark'=>'','pay'=>$pay,'add_time'=>$time,'status'=>1,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>0,'spz_price'=>$products_total,'reduce_price'=>0,'coupon_price'=>0,'source'=>$store_type,'otype'=>'PT','mch_id'=>$mch_id0,'bargain_id'=>0,'comm_discount'=>0,'remarks'=>$remarks,'real_sno'=>$real_sno_0,'self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','grade_rate'=>'0','grade_fan'=>'','z_freight'=>$freight,'preferential_amount'=>0,'single_store'=>0,'grade_score'=>0,'p_sNo'=>'','drawid'=>$acId,'order_failure_time'=>$order_failure_time);
        $r_0 = Db::name('order')->insertGetId($sql_0);
        if ($r_0 < 1)
        { // 添加失败
            $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数：" . json_encode($sql_0));
            Db::rollback();
            $message = Lang('nomal_order.3');
            echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }

        // 添加拼团订单详情
        $sql_1 = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pid,'p_name'=>$products['product_title'],'p_price'=>$products_total,'num'=>1,'unit'=>$products['unit'],'r_sNo'=>$sNo_0,'add_time'=>$time,'r_status'=>1,'size'=>$products['size'],'sid'=>$cid,'freight'=>$freight,'coupon_id'=>0,'after_discount'=>$products_total);
        $r_1 = Db::name('order_details')->insert($sql_1);
        if ($r_1 < 1)
        { // 添加失败
            $this->Log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数：" . json_encode($sql_1));
            Db::rollback();
            $message = Lang('nomal_order.3');
            echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }

        // 添加开团详情信息
        $sql1 = array('open_id'=>$openId,'goods_id'=>$pid,'attr_id'=>$cid,'attr_price'=>$attr_price,'price'=>$products_total,'user_id'=>$user_id,'sno'=>$sNo_0,'add_date'=>$time);
        $r1 = Db::name('group_open_record')->insert($sql1);
        if ($r1 < 1)
        { // 添加失败
            $this->Log(__METHOD__ . ":" . __LINE__ . "添加开团详情信息失败！参数：" . json_encode($sql1));
            Db::rollback();
            $message = Lang('nomal_order.3');
            echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
            exit;
        }

        if($team_num == $Offered_num)
        { // 当 团队数量 == 参团数量 拼团成功
            $sql_pt2 = "update lkt_group_open set status = 1 where id = '$openId' ";
            $r_pt2 = Db::execute($sql_pt2);
            $this->Log(__METHOD__ . ":" . __LINE__ . "拼团成功sql：" . $sql_pt2);
            $this->Log(__METHOD__ . ":" . __LINE__ . "拼团成功结果：" . $r_pt2);
            if ($r_pt2 < 0)
            {
                Db::rollback();
                $message = Lang('operation failed');
                return output(400, $message);
            }

            $msg_title = Lang("go_group.12");
            $msg_content = Lang("go_group.13");
            $pusher = new LaikePushTools();

            foreach($user_id_array as $k_3 => $v_3)
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "发送消息人员：" .  $v_3['user_id']);
                $pusher->pushMessage( $v_3['user_id'], $msg_title, $msg_content, $store_id, '');
                $this->Log(__METHOD__ . ":" . __LINE__ . "！！！！！！" );
            }
        }
        Db::commit();
        return;
    }

    // 会员特惠折扣计算
    public function user_grade($type, $product_total, $user_id, $store_id)
    {
        //会员特惠支持商品标识
        $flag = '';
        //商品
        if ($type == 'GM')
        { // 普通
            $flag = 1;
        }
        else if ($type == 'PT')
        { // 拼团
            $flag = 2;
        }
        else if ($type == 'KJ')
        { // 砍价
            $flag = 3;
        }
        else if ($type == 'JP')
        { // 竞拍
            $flag = 4;
        }
        else if ($type == 'FX')
        { // 分销
            $flag = 5;
        }
        else if ($type == 'MS')
        { // 秒杀
            $flag = 6;
        }
        else if ($type == 'TH')
        { // 会员特惠
            $flag = 7;
        }

        $active = array();
        //插叙可支持的特惠商品或活动
        $res = UserRuleModel::where(['store_id'=>$store_id])->field('active')->select()->toArray();
        if ($res)
        {
            $active = explode(',', $res[0]['active']); //可支持活动数组
        }

        $can = false; //能否支持会员特惠
        if ($flag)
        {
            if (in_array($flag, $active))
            {
                $can = true;
            }
            else
            {
                $can = false;
            }
        }
        else
        {
            $can = false;
        }

        $now = date("Y-m-d H:i:s");
        if ($can == true)
        {
            // 可以使用会员特惠
            $sql_0 = array('store_id'=>$store_id,'user_id'=>$user_id);
            $res_0 = UserModel::where($sql_0)->where('grade_end','>',$now)->field('grade')->select()->toArray();
            if ($res_0)
            {
                $grade = $res_0[0]['grade'];

                $sql_1 = array('store_id'=>$store_id,'id'=>$grade);
                $res_1 = UserGradeModel::where($sql_0)->field('rate')->select()->toArray();
                if ($res_1)
                {
                    $rate = floatval($res_1[0]['rate']);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 查询会员级别失败！参数:' . json_encode($sql_1);
                    $this->Log($Log_content);
                    $rate = 10;
                }
            }
            else
            {   
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 查询用户的会员等级失败！参数:' . json_encode($sql_0);
                $this->Log($Log_content);
                $rate = 10; //折扣
            }
            $total = $product_total * $rate;
        }
        else
        { // 不可以使用会员特惠
            $rate = 10;
            $total = $product_total;
        }

        $arr = array();
        $arr['total'] = $total;
        $arr['rate'] = $rate / 10;
        //放回打折后商品总价
        return $arr;
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

    // 定时任务
    public function dotask($params)
    {
        $store_id = $params->store_id;
        $now = date("Y-m-d H:i:s"); //当前时间戳

        $this->Team_up_begins($store_id); // 拼团开始

        $this->End_of_group_assembly($store_id); // 拼团结束

        $this->Team_failure($store_id); // 拼团失败

        $this->Temporary_orders($store_id); // 清除临时订单

        $this->Commission_distribution($store_id); // 佣金发放

        $this->order_settlement($store_id); // 商家订单结算

        return;
    }

    // 拼团开始
    public function Team_up_begins($store_id)
    {
        $time = date("Y-m-d H:i:s"); //当前时间戳

        // 查询未开始的拼团活动
        $sql0 = "select id,start_date from lkt_group_activity where store_id = '$store_id' and status = 0 and recycle = 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $id = $v0['id'];
                if($v0['start_date'] <= $time)
                { // 开始时间 <= 当前时间
                    $sql1 = "update lkt_group_activity set status = 1 where id = '$id' ";
                    $r1 = Db::execute($sql1);
                    if($r1 <= 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改拼团状态失败！sql：'.$sql1;
                        $this->Log($Log_content);
                    }
                }
            }
        }

        return;
    }

    // 拼团结束
    public function End_of_group_assembly($store_id)
    {
        $time = date("Y-m-d H:i:s"); //当前时间戳

        // 查询未开始的拼团活动
        $sql0 = "select id,end_date from lkt_group_activity where store_id = '$store_id' and status = 1 and recycle = 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $id = $v0['id'];
                if($v0['end_date'] <= $time)
                { // 结束时间 >= 当前时间
                    Db::startTrans();
                    $sql1 = "update lkt_group_activity set status = 2,is_show = 0 where id = '$id' ";
                    $r1 = Db::execute($sql1);
                    if($r1 <= 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改拼团状态失败！sql：'.$sql1;
                        $this->Log($Log_content);
                        Db::rollback();
                        continue;
                    }

                    // 根据拼团活动ID、拼团中，查询开团数据
                    $sql2 = "select id,sno,goods_id,attr_id from lkt_group_open where status = 0 and activity_id = '$id' ";
                    $r2 = Db::query($sql2);
                    if($r2)
                    {
                        foreach($r2 as $k2 => $v2)
                        {
                            $open_id = $v2['id']; // 开团ID
                            $sno = $v2['sno']; // 开团订单

                            $res = $this->Team_failure_0($store_id,$open_id,$sno);
                            if($res != 1)
                            {
                                Db::rollback();
                                continue;
                            }
                        }
                    }
                    Db::commit();
                }
            }
        }

        return;
    }

    // 拼团失败
    public function Team_failure($store_id)
    {
        $time = date("Y-m-d H:i:s"); //当前时间戳

        $Tools = new Tools( $store_id, 1);
        // 查询状态还在拼团中，且拼团结束时间 <= 当前时间
        $sql0 = "select id,sno,goods_id,attr_id from lkt_group_open where status = 0 and end_date <= '$time' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $open_id = $v0['id']; // 开团ID
                $sno = $v0['sno']; // 开团订单

                $res = $this->Team_failure_0($store_id,$open_id,$sno);
            }
        }
        
        return;
    }

    // 回滚参团订单
    public function Team_failure_0($store_id,$open_id,$sno)
    {
        Db::startTrans();
        $time = date("Y-m-d H:i:s");
        $sql0_0 = "update lkt_group_open set status = 2,team_commission = 0,update_date = '$time' where id = '$open_id' ";
        $r0_0 = Db::execute($sql0_0);
        if($r0_0 <= 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改拼团状态失败！sql：'.$sql0_0;
            $this->Log($Log_content);
            Db::rollback();
            return 0;
        }

        $sql0_1 = "update lkt_order set status = 7 where sNo = '$sno' ";
        $r0_1 = Db::execute($sql0_1);
        if($r0_1 <= 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改开团订单状态失败！sql：'.$sql0_1;
            $this->Log($Log_content);
            Db::rollback();
            return 0;
        }

        $sql0_2 = "update lkt_order_details set r_status = 7 where r_sNo = '$sno' ";
        $r0_2 = Db::execute($sql0_2);
        if($r0_2 <= 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改开团订单详情状态失败！sql：'.$sql0_2;
            $this->Log($Log_content);
            Db::rollback();
            return 0;
        }

        $sql1 = "select sno from lkt_group_open_record where open_id = '$open_id' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k1 => $v1)
            {
                $sno1 = $v1['sno']; // 参团订单号

                $sql1_0 = "select a.user_id,a.z_price,a.pay,a.real_sno,b.id as oid,b.p_id,b.sid,b.p_name,a.mch_id from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.sNo = '$sno1' and a.status = 1  ";
                $r1_0 = Db::query($sql1_0);
                if($r1_0)
                {
                    $user_id = $r1_0[0]['user_id'];
                    $price = $r1_0[0]['z_price'];
                    $pay = $r1_0[0]['pay']; // 支付方式
                    $p_sNo = $r1_0[0]['real_sno']; // 支付订单号
                    $o_d_id = $r1_0[0]['oid']; // 订单详情ID
                    $pid = $r1_0[0]['p_id']; // 订单详情ID
                    $cid = $r1_0[0]['sid']; // 订单详情ID
                    $p_name = $r1_0[0]['p_name']; // 商品名称
                    $mch_id = trim($r1_0[0]['mch_id'],','); // 店铺ID

                    $mch_name = 0;
                    $cpc = '';
                    $mobile = '';
                    $sql_m = "select name,cpc,tel from lkt_mch where id = '$mch_id' ";
                    $r_m = Db::query($sql_m);
                    if($r_m)
                    {
                        $mch_name = $r_m[0]['name'];
                        $cpc = $r_mch[0]['cpc']; // 区号
                        $mobile = $r_mch[0]['tel']; // 店铺联系电话
                    }

                    $appid = '';
                    $pay_config = Tools::get_pay_config($pay);
                    if ($pay_config)
                    {
                        $appid = $pay_config['appid'];
                    }
                    
                    if ($pay == 'tt_alipay')
                    {
                        $pay = 'aliPay';
                    }

                    if ($pay == 'baidu_pay')
                    {
                        $pay = 'wallet_pay';
                    }
                    //不同支付方式判断
                    switch ($pay)
                    {
                        case 'wallet_pay' :
                            //钱包
                            $res = RefundUtils::return_user_money($store_id,$user_id, $price, $sno1,$p_name,$mch_name);
                            break;
                        case 'aliPay' :
                        case 'alipay' :
                        case 'pc_alipay' :
                        case 'alipay_mobile' :
                        case 'alipay_minipay' :
                            // 支付宝小程序退款 //支付宝手机支付//支付宝扫码支付
                            $zfb_res = AlipayReturn::refund($p_sNo, $price, $store_id, $o_d_id, $pay);
                            $Log_content = __METHOD__ . '->' . __LINE__ . '支付宝退款结果：' . $zfb_res;
                            $this->Log("common/return.log",$Log_content);
                            if ($zfb_res != 'success')
                            {   
                                if($zfb_res == '商家余额不足！' && !empty($mobile))
                                {
                                    $array_code = array('cpc'=>$cpc,'mobile'=>$mobile,'type'=>1,'type1'=>10,'bizparams'=>array("sNo" => $sno1));
                                    $Tools->generate_code($array_code);
                                }  
                                Db::rollback();
                                echo json_encode(array('code' => 109, 'message' => $zfb_res));
                                exit;
                            }
                            else
                            {   
                                $array_code = array('cpc'=>$cpc,'mobile'=>$mobile,'type'=>1,'type1'=>9,'bizparams'=>array("sNo" => $trade_no,"mch_name"=>$mch_name,"money"=>$promise_0));
                                $Tools->generate_code($array_code);
                            }
                            break;
                        case 'app_wechat' :
                        case 'mini_wechat' :
                        case 'pc_wechat' :
                        case 'H5_wechat' :
                        case 'jsapi_wechat' :
                            
                            //微信公众号 微信小程序支付 微信APP支付.
                            $wxtk_res = wxpay::wxrefundapi($p_sNo, $p_sNo . $o_d_id, $price, $price, $store_id, $pay);
                            if ($wxtk_res['result_code'] != 'SUCCESS')
                            {
                                Db::rollback();
                                if ($wxtk_res['err_code_des'] == '基本账户余额不足，请充值后重新发起')
                                {
                                    $msg_title = Lang('go_group.9');
                                    $msg_content = "账户余额不足，订单【".$sno1."】自动退款失败。请尽快登陆平台完成处理！";

                                    $sql_admin = "select b.user_id from lkt_admin as a left join lkt_mch as b on a.shop_id = b.id where a.store_id = '$store_id' and a.type = 1 and a.recycle = 0 ";
                                    $r_admin = Db::query($sql_admin);
                                    if($r_admin)
                                    {
                                        $user_id_admin = $r_admin[0]['user_id'];
                                    }
                                    if(!empty($mobile))
                                    {
                                        $array_code = array('cpc'=>$cpc,'mobile'=>$mobile,'type'=>1,'type1'=>10,'bizparams'=>array("sNo" => $trade_no));
                                        $Tools->generate_code($array_code);
                                    }

                                    $pusher = new LaikePushTools();
                                    $pusher->pushMessage($user_id_admin, $msg_title, $msg_content, $store_id, '');

                                    $message = Lang('return.12');
                                    echo json_encode(array('code' => 109, 'message' => $message));
                                    exit;
                                }

                                $message = Lang('return.0');
                                echo json_encode(array('code' => 109, 'message' => $message));
                                exit;
                            }
                            else
                            {   
                                $array_code = array('cpc'=>$cpc,'mobile'=>$mobile,'type'=>1,'type1'=>9,'bizparams'=>array("sNo" => $trade_no,"mch_name"=>$mch_name,"money"=>$promise_0));
                                $Tools->generate_code($array_code);
                            }
                            break;
                        case 'baidu_pay' :
                        default:
                            echo json_encode(array('code' => 109, 'message' => $pay . '支付方式不存在！'));
                            exit;
                    }
                }

                $sql1_1 = "update lkt_order set status = 7 where sNo = '$sno1' ";
                $r1_1 = Db::execute($sql1_1);
                if($r1_1 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改参团订单状态失败！sql：'.$sql1_1;
                    $this->Log($Log_content);
                    Db::rollback();
                    return 0;
                }

                $sql1_2 = "update lkt_order_details set r_status = 7 where r_sNo = '$sno1' ";
                $r1_2 = Db::execute($sql1_2);
                if($r1_2 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改参团订单详情状态失败！sql：'.$sql1_2;
                    $this->Log($Log_content);
                    Db::rollback();
                    return 0;
                }

                $res = $this->Rolling_back_inventory($store_id,$pid,$cid);
                if($res != 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改商品库存失败！属性ID：'.$cid;
                    $this->Log($Log_content);
                    Db::rollback();
                    return 0;
                }

                $msg_title = Lang("go_group.10");
                $msg_content = Lang("go_group.11");
                $pusher = new LaikePushTools();
                $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, '');
            }
        }

        Db::commit();
        return 1;
    }

    // 清除临时订单
    public function Temporary_orders($store_id)
    {
        $time = date("Y-m-d H:i:s"); //当前时间戳

        $r = OrderConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        $order_failure = $r ? $r[0]['order_failure'] : 1; // 未付款订单保留时间
        $time01 = date("Y-m-d H:i:s", strtotime("-$order_failure hour",time())); // 订单过期删除时间

        $data = ',"store_id":' . $store_id . ',';

        // 根据PT订单、未付款、过了付款时间，查询临时订单
        $sql0 = "select id,data from lkt_order_data where status = 0 and order_type = 'PT' and data like '%$data%' and addtime <= '$time01' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                sleep(0.01);
                Db::startTrans();
                $id = $v0['id'];
                $data = json_decode($v0['data'],true);
                $pid = $data['goods_id']; // 商品ID
                $cid = $data['attr_id']; // 属性ID

                $sql1 = "delete from lkt_order_data where id = '$id' ";
                $r1 = Db::execute($sql1);
                if($r1 < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除临时订单失败！sql：'.$sql1;
                    $this->Log($Log_content);
                    Db::rollback();
                    continue;
                }

                $res = $this->Rolling_back_inventory($store_id,$pid,$cid);
                if($res != 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改商品库存失败！属性ID：'.$cid;
                    $this->Log($Log_content);
                    Db::rollback();
                    continue;
                }
                Db::commit();
            }
        }
        
        return;
    }

    // 佣金发放
    public function Commission_distribution($store_id)
    {
        $time0 = date("Y-m-d H:i:s");
        // 根据拼团成功、未结算，查询开团信息
        $sql0 = "select * from lkt_group_open where status = 1 and is_settlement = 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach ($r0 as $k0 => $v0)
            {
                Db::startTrans();
                $id = $v0['id'];
                $sno = $v0['sno'];
                $user_id = $v0['user_id']; // 开团user_id
                $team_num = $v0['team_num']; // 团队人数
                $team_commission = $v0['team_commission']; // 团长佣金

                // 根据订单结算状态、开团ID，查询已经结算的订单
                $sql_1 = "select o.id from lkt_order as o left join lkt_group_open_record as b on o.sNo = b.sno where o.store_id = '1' and o.settlement_status = 1 and b.open_id = '$id' ";
                $r_1 = Db::query($sql_1);
                if(count($r_1) == $team_num)
                { // 参团订单全部结算
                    // 根据订单详情结算状态、开团ID，查询已经结算的订单详情
                    $sql_2 = "select a.id from lkt_order_details as a left join lkt_group_open_record as b on a.r_sNo = b.sno where a.store_id = '$store_id' and a.settlement_type = 1 and b.open_id = '$id' ";
                    $r_2 = Db::query($sql_2);
                    if($r_2)
                    { // 存在已结算的订单详情
                        $sql1 = "update lkt_group_open set is_settlement = 1 where id = '$id' ";
                        $r1 = Db::execute($sql1);
                        if($r1 < 0)
                        {
                            Db::rollback();
                            $lktlog->log(__METHOD__ . ":" . __LINE__ . "发放团长佣金失败！sql：" .$sql1);
                        }

                        $sql2 = "select money from lkt_user where user_id = '$user_id' ";
                        $r2 = Db::query($sql2);
                        if($r2)
                        {
                            $oldmoney = $r2[0]['money'];
                        }

                        $money = $team_commission + $oldmoney;
                        $sql3 = "update lkt_user set money = '$money' where user_id = '$user_id' ";
                        $r3 = Db::execute($sql3);
                        if($r3 < 0)
                        {
                            Db::rollback();
                            $lktlog->log(__METHOD__ . ":" . __LINE__ . "发放团长佣金失败！user_id:" . $user_id.'-'.$team_commission);
                        }

                        $array = array('store_id'=>$store_id,'money'=>$team_commission,'user_money'=>$oldmoney,'type'=>3,'money_type'=>1,'money_type_name'=>3,'record_notes'=>'','type_name'=>'wallet_pay','s_no'=>$sno,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                        $details_id = PC_Tools::add_Balance_details($array);

                        $event = '系统充值' . abs($team_commission) . '余额';
                        $sql4 = "insert into lkt_record(store_id,user_id,money,oldmoney,add_date,event,type,details_id) value ('$store_id','$user_id','$team_commission','$oldmoney','$time0','$event',14,'$details_id')";
                        $r4 = Db::execute($sql4);
                        if($r3 < 0)
                        {
                            Db::rollback();
                            $lktlog->log(__METHOD__ . ":" . __LINE__ . "发放团长佣金失败！sql:" . $sql4);
                        }
                    }
                    else
                    { // 参团订单全部退款
                        $sql1 = "update lkt_group_open set is_settlement = 1,team_commission = 0 where id = '$id' ";
                        $r1 = Db::execute($sql1);
                        if($r1 < 0)
                        {
                            Db::rollback();
                            $lktlog->log(__METHOD__ . ":" . __LINE__ . "发放团长佣金失败！sql：" .$sql1);
                        }
                    }
                }

                Db::commit();
            }
        }
    }

    // 商家订单结算
    public function order_settlement($store_id)
    {
        Db::startTrans();

        $order_after = 0;
        // 获取订单售后期
        $sql_c = "select order_after from lkt_group_order_config where store_id = '$store_id' ";
        $r_c = Db::query($sql_c);
        if($r_c)
        {
            $order_after = $r_c[0]['order_after'];
        }
    
        $time = date("Y-m-d H:i:s",strtotime("-$order_after day",time()));
        
        $sql_m = "select z_price,mch_id,sNo,id,supplier_id from lkt_order where store_id = '$store_id' and status = 5 and arrive_time <= '$time' and settlement_status = 0 and otype in ('PT') ";
        $res_m = Db::query($sql_m);
        if($res_m)
        {
            foreach ($res_m as $key => $value) 
            {
                $res = ReturnOrderModel::where(['sNo'=>$value['sNo']])->whereIn('r_type','0,1,3')->where('re_type','<>',3)->field('id')->select()->toArray();
                if(empty($res))
                {   
                    //获取供应商价总计
                    $supplier_settlement = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$value['sNo'],'r_status' => 5])->sum('supplier_settlement');
                    if($value['z_price'] > 0)
                    {
                        $money = $value['z_price'];
                        $mch_id = substr($value['mch_id'], 1, -1);
                        $sql_u_where = array('store_id'=>$store_id,'id'=>$mch_id);
                        $sql_u_update = array('account_money'=>Db::raw('account_money-'.$money),'cashable_money'=>Db::raw('cashable_money+'.$money)); //49566
                        $res_u = Db::name('mch')->where($sql_u_where)->update($sql_u_update);
                        if($res_u < 0)
                        {   
                            Db::rollback();
                            $this->Log(__METHOD__ . ":" . __LINE__ . "订单结算到账失败！参数:" . json_encode($sql_u_where));
                        }
                        else
                        {
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
                    if($supplier_settlement > 0)
                    {
                        $this->supplier_settlement($value['sNo'],$supplier_settlement,$value['supplier_id'],$mch_id);
                    }
                    //更新订单结算状态
                    $id = $value['id'];
                    $sql_o = OrderModel::find($id);
                    $sql_o->settlement_status = 1;
                    $res_o = $sql_o->save();
                    if(!$res_o)
                    {
                        Db::rollback();
                        $this->Log(__METHOD__ . ":" . __LINE__ . "订单结算状态修改失败！参数:id" . $id);
                    }
                }
            }
        }        
        Db::commit();
    }

    // 供应商结算
    public function supplier_settlement($store_id,$sNo,$money,$supplier_id,$mch_id)
    {
        //获取供应商信息
        $res_sup = SupplierModel::where(['store_id'=>$store_id,'id'=>$supplier_id])->select()->toArray();
        if($res_sup)
        {
            //供应商余额
            $account_money = $res_sup[0]['surplus_balance'];
            //更新供应商数据
            $sql1 = SupplierModel::find($supplier_id);
            $sql1->surplus_balance = Db::raw('surplus_balance + '.$money);
            $res1 = $sql1->save();
            if(!$res1)
            {
                Db::rollback();
                $this->Log(__METHOD__ . ":" . __LINE__ . "供应商余额修改失败！参数:supplier_id" . $supplier_id);
            }
            //添加记录
            $sql2 = new SupplierAccountLogModel();
            $sql2->store_id = $store_id;
            $sql2->supplier_id = $supplier_id;
            $sql2->amount = $money;
            $sql2->account_money = $account_money;
            $sql2->status = 1;
            $sql2->type = 1;
            $sql2->remake = $sNo;
            $sql2->remark = '订单总供货价';
            $sql2->addtime = date('Y-m-d H:i:s');
            $sql2->save();
            $res2 = $sql2->id;
            if($res2 < 1)
            {
                Db::rollback();
                $this->Log(__METHOD__ . ":" . __LINE__ . "供应商添加资金记录失败！参数:supplier_id" . $supplier_id);
            }
            //更新店铺数据
            $sql3_where = array('store_id'=>$store_id,'id'=>$mch_id);
            $sql3_update = array('cashable_money'=>Db::raw('cashable_money-'.$money),'account_money'=>Db::raw('account_money-'.$money));
            $res3 = Db::name('mch')->where($sql3_where)->update($sql3_update);
            if($res3 < 0)
            {   
                Db::rollback();
                $this->Log(__METHOD__ . ":" . __LINE__ . "订单结算到账失败！参数:" . json_encode($sql3_where));
            } 

            $r3 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('account_money,integral_money')->select()->toArray();
            if(!$r3)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . "查询店铺不存在 mchid $mch_id !";
                $this->Log($Log_content);
                Db::rollback();
               
            }

            $account_money = $r3[0]['account_money'];
            $integral_money = $r3[0]['integral_money'];
            //添加记录
            $data4 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'price'=>$money,'integral'=>0,'integral_money'=>$integral_money,'account_money'=>$account_money,'type'=>5,'addtime'=>date("Y-m-d H:i:s"),'remake'=>$sNo,'status'=>2);
            $r4 = Db::name('mch_account_log')->insert($data4);
            if ($r4 <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加入驻商户账户收支记录失败！sql:'.json_encode($data4);
                $this->Log($Log_content);
                Db::rollback();
            }
            
            //运费结算
            $supplier_freight = SupplierOrderFrightModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'is_settlement'=>0])->sum('freight');
            if($supplier_freight > 0)
            {
                //更新供应商数据
                $sql5 = SupplierModel::find($supplier_id);
                $sql5->surplus_balance = Db::raw('surplus_balance + '.$supplier_freight);
                $res5 = $sql5->save();
                if(!$res5)
                {
                    Db::rollback();
                    $this->Log(__METHOD__ . ":" . __LINE__ . "供应商运费余额修改失败！参数:supplier_id" . $supplier_id);
                }
                //添加记录
                $sql6 = new SupplierAccountLogModel();
                $sql6->store_id = $store_id;
                $sql6->supplier_id = $supplier_id;
                $sql6->amount = $supplier_freight;
                $sql6->account_money = $account_money;
                $sql6->status = 1;
                $sql6->type = 5;
                $sql6->remake = $sNo;
                $sql6->remark = '订单运费';
                $sql6->addtime = date('Y-m-d H:i:s');
                $sql6->save();
                $res6 = $sql6->id;
                if($res6 < 1)
                {
                    Db::rollback();
                    $this->Log(__METHOD__ . ":" . __LINE__ . "供应商添加运费资金记录失败！参数:supplier_id" . $supplier_id);
                }

                //更新运费订单结算状态
                $res7 = SupplierOrderFrightModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'is_settlement'=>0])->update(['is_settlement'=>1]);
                if(!$res7)
                {
                    Db::rollback();
                    $this->Log(__METHOD__ . ":" . __LINE__ . "更新运费订单结算状态失败！参数:sNo" . $sNo);
                }

                //更新店铺数据
                $sql8_where = array('store_id'=>$store_id,'id'=>$mch_id);
                $sql8_update = array('cashable_money'=>Db::raw('cashable_money-'.$supplier_freight),'account_money'=>Db::raw('account_money-'.$supplier_freight));
                $res8 = Db::name('mch')->where($sql8_where)->update($sql8_update);
                if($res8 < 0)
                {   
                    Db::rollback();
                    $this->Log(__METHOD__ . ":" . __LINE__ . "订单结算到账失败！参数:" . json_encode($sql8_where));
                } 

                $r9 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('account_money,integral_money')->select()->toArray();

                $account_money = $r9[0]['account_money'];
                $integral_money = $r9[0]['integral_money'];
                //添加记录
                $data10 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'price'=>$supplier_freight,'integral'=>0,'integral_money'=>$integral_money,'account_money'=>$account_money,'type'=>5,'addtime'=>date("Y-m-d H:i:s"),'remake'=>$sNo,'status'=>2);
                $r10 = Db::name('mch_account_log')->insert($data10);
                if ($r10 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '添加入驻商户账户收支记录失败！sql:'.json_encode($data10);
                    $this->Log($Log_content);
                    Db::rollback();
                }
            }

        }
    }

    // 获取拼团设置信息
    public function GetConfig($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺

        $config = array();
        $config['endTime'] = 3600;
        $config['id'] = '';
        $config['joinLimit'] = '1';
        $config['openLimit'] = '1';
        $config['rule'] = '';
        
        $r_config = GroupConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
        if($r_config)
        {
            $config['endTime'] = $r_config[0]['end_time'];
            $config['id'] = $r_config[0]['id'];
            $config['joinLimit'] = $r_config[0]['join_limit'];
            $config['openLimit'] = $r_config[0]['open_limit'];
            $config['rule'] = $r_config[0]['rule_content'];

            if($source != 2)
            {
                $config['endTime'] = $config['endTime'] / 3600;
            }
        }

        $data = array('config'=>$config);
        return $data;
    }

    // 拼团设置
    public function SetConfig($array)
    {
        $Jurisdiction = new Jurisdiction();
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $end_time = $array['end_time']; // 成团倒计时 0=不计时
        $open_limit = $array['open_limit']; // 开团限制数量 0=不限制
        $join_limit = $array['join_limit']; // 参团限制数量 0=不限制
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺
        
        $shop_id = '';
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }

        $time = date("Y-m-d H:i:s");

        if($source != 2)
        {
            $end_time = $end_time * 3600;
        }

        $r_config = GroupConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
        if($r_config)
        { // 修改
            $sql_where = array('store_id'=>$store_id,'mch_id'=>$mch_id);
            $sql_update = array('end_time'=>$end_time,'open_limit'=>$open_limit,'join_limit'=>$join_limit);
            $r = Db::name('group_config')->where($sql_where)->update($sql_update);
            if($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了拼团设置信息失败',2,$source,$shop_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 店主' . $mch_id . '修改拼团设置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('修改失败！');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了拼团设置信息',2,$source,$shop_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ .  ' 店主' . $mch_id . '修改拼团设置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,  'message' => $message));
                exit;
            }
        }
        else
        { // 添加
            $sql_insert = array('store_id'=>$store_id,'mch_id'=>$mch_id,'end_time'=>$end_time,'open_limit'=>$open_limit,'join_limit'=>$join_limit,'rule_content'=>'','add_date'=>$time);
            $r = Db::name('group_config')->insert($sql_insert);
            if($r > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了拼团设置信息',2,$source,$shop_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ .  ' 店主' . $mch_id . '添加拼团设置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,  'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了拼团设置信息失败',2,$source,$shop_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 店主' . $mch_id . '添加拼团设置失败！参数：' . json_encode($sql_insert);
                $this->Log($Log_content);
                $message = Lang('添加失败！');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
        }
    }

    // 获取商品
    public function GoodsList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
       
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺
        if($source == 2)
        {
            $activityId = $array['activityId']; // 拼团活动ID
            $classId = $array['classId']; // 商品分类ID
            $goodsBrandId = $array['goodsBrandId']; // 商品品牌ID
        }
        else if($source == 3)
        {
            $requestType = $array['requestType']; // 1.选中的商品 2.未选中的商品
            $goodsId = $array['goodsId']; // 商品ID
        }
        else
        {
            $activityId = $array['activityId']; // 拼团活动ID
            $classId = $array['classId']; // 商品分类ID
            $goodsBrandId = $array['goodsBrandId']; // 商品品牌ID
        }
        $goodsName = $array['goodsName']; // 商品名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页数据

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        
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

        $condition = "a.store_id = '$store_id' and a.recycle = 0 and a.is_presell = 0 and a.commodity_type = '0' and a.gongyingshang = '0' and a.status = 2 and a.mch_id = '$mch_id' and a.mch_status = 2 and a.num > 0 ";
  
        if($source == 2)
        { // 店铺PC端
            if($activityId != 0 && $activityId != '')
            {
                $str_list = array();
                $sql_p = "select goods_id from lkt_group_goods where activity_id = '$activityId' and recycle = 0 ";
                $r_p = Db::query($sql_p);
                if($r_p)
                {
                    foreach($r_p as $k_p => $v_p)
                    {
                        $str_list[] = $v_p['goods_id'];
                    }
                }
                $str = implode(',',$str_list);

                $condition .= " and id in ($str) ";
            }

            if ($classId != 0 && $classId != '')
            {
                $Tools = new Tools($store_id, 1);
                $product_class1 = $Tools->str_option( $classId);

                $condition .= " and a.product_class like '%$product_class1%' ";
            }    

            if ($goodsBrandId != 0 && $goodsBrandId != '')
            {
                $condition .= " and a.brand_id = '$goodsBrandId' ";
            }

            if ($goodsName != '')
            {
                if(strpos($goodsName," "))
                {
                    $product_title1 = explode(' ',$goodsName);

                    $condition1 = ' and (';
                    foreach ($product_title1 as $k => $v)
                    {
                        if($v)
                        {
                            $condition1 .= " a.product_title like '%$v%' or a.id = '$v' or ";
                        }
                    }

                    $condition1 = substr($condition1,0,strlen($condition1)-3);
                    $condition1 .= ' )';
                    $condition .= $condition1;
                }
                else
                {
                    $condition .= " and (a.product_title like '%$goodsName%' or a.id = '$goodsName' ) ";
                }
            }
        }
        else if($source == 3)
        { // 移动端店铺
            if($requestType == 1)
            {
                if($goodsId != '')
                {
                    $condition .= " and a.id in (" . $goodsId . ") ";
                }
                else 
                {
                    $condition .= " and a.id = 0 ";
                }
            }
            else
            {
                if($goodsId != '')
                {
                    $condition .= " and a.id not in (" . $goodsId . ") ";
                }
            }
            if ($goodsName != '')
            {
                if(strpos($goodsName," "))
                {
                    $product_title1 = explode(' ',$goodsName);

                    $condition1 = ' and (';
                    foreach ($product_title1 as $k => $v)
                    {
                        if($v)
                        {
                            $condition1 .= " a.product_title like '%$v%' ";
                        }
                    }

                    $condition1 = substr($condition1,0,strlen($condition1)-3);
                    $condition1 .= ' )';
                    $condition .= $condition1;
                }
                else
                {
                    $condition .= " and a.product_title like '%$goodsName%' ";
                }
            }
        }
        else
        {
            if($activityId != 0 && $activityId != '')
            {
                $str_list = array();
                $sql_p = "select goods_id from lkt_group_goods where activity_id = '$activityId' and recycle = 0 ";
                $r_p = Db::query($sql_p);
                if($r_p)
                {
                    foreach($r_p as $k_p => $v_p)
                    {
                        $str_list[] = $v_p['goods_id'];
                    }
                }
                $str = implode(',',$str_list);

                $condition .= " and id in ($str) ";
            }

            if ($classId != 0 && $classId != '')
            {
                $Tools = new Tools($store_id, 1);
                $product_class1 = $Tools->str_option( $classId);

                $condition .= " and a.product_class like '%$product_class1%' ";
            }    

            if ($goodsBrandId != 0 && $goodsBrandId != '')
            {
                $condition .= " and a.brand_id = '$goodsBrandId' ";
            }

            if ($goodsName != '')
            {
                if(strpos($goodsName," "))
                {
                    $product_title1 = explode(' ',$goodsName);

                    $condition1 = ' and (';
                    foreach ($product_title1 as $k => $v)
                    {
                        if($v)
                        {
                            $condition1 .= " a.product_title like '%$v%' or a.id = '$v' or ";
                        }
                    }

                    $condition1 = substr($condition1,0,strlen($condition1)-3);
                    $condition1 .= ' )';
                    $condition .= $condition1;
                }
                else
                {
                    $condition .= " and (a.product_title like '%$goodsName%' or a.id = '$goodsName' ) ";
                }
            }
        }
        
        $list = array();
        $total = 0;

        $sql0 = "select count(a.id) as total from lkt_product_list as a where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $str = " a.id,a.imgurl as imgUrl,a.product_title as name,a.product_class,a.brand_id,a.num as stockNum ";

        $sql1 = "select $str from lkt_product_list as a where $condition order by a.id desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $pid = $v['id'];
                $class = $v['product_class'];
                $bid = $v['brand_id'];
                $v['imgUrl'] = ServerPath::getimgpath($v['imgUrl'],$store_id);

                $pname = array_key_exists($class, $product_class_arr) ? $product_class_arr[$class]:'顶级'; // 分类名称
                $brand_name = array_key_exists($bid, $brand_class_arr) ? $brand_class_arr[$bid]:'暂无'; // 品牌名称
                $v['className'] = $pname;
                $v['brandName'] = $brand_name;

                $present_price = 0;
                $r_s = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('min(price) as price')->select()->toArray();
                if ($r_s)
                {
                    $present_price = round($r_s[0]['price'],2);
                }

                $v['price'] = $present_price;

                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 拼团活动
    public function Index($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $status = $array['status'];
        $name = $array['name'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        
        $condition = " store_id = '$store_id' and mch_id = '$mch_id' and recycle = 0 ";
        if($status != '')
        {
            $condition .= " and status = '$status' ";
        }

        if($name != '')
        {
            $condition .= " and (name like '%$name%' or id like '%$name%' ) ";
        }

        $list = array();
        $total = 0;

        $sql0 = "select count(id) as total from lkt_group_activity where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $str = " id,name,start_date as startDate,end_date as endDate,is_show as isShow,status ";

        $sql1 = "select $str from lkt_group_activity where $condition order by add_date desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                if($v['status'] == 1)
                {
                    $v['statusName'] = '进行中';
                }
                else if($v['status'] == 2)
                {
                    $v['statusName'] = '已结束';
                }
                else
                {
                    $v['statusName'] = '未开始';
                }

                $v['startDate'] = date("Y-m-d",strtotime($v['startDate']));
                $v['endDate'] = date("Y-m-d",strtotime($v['endDate']));

                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 添加/编辑拼团活动
    public function AddGroupActivity($array)
    {
        $Jurisdiction = new Jurisdiction();
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 活动ID
        $goodsIds = $array['goodsIds']; // 商品ID
        $name = $array['name']; // 活动名称
        $goods_limit = $array['goods_limit']; // 同一商品参团限制开关
        $team_limit = $array['team_limit']; // 团长是否可参团
        $start_date = $array['start_date']; // 开始时间
        $end_date = $array['end_date']; // 结束时间
        $groupRule = $array['groupRule']; // 团长参团设置
        $is_show = $array['is_show']; // 是否显示
        $is_custom = $array['is_custom']; // 时间是否自定义 0不是 1是
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺

        $shop_id = '';
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }

        $start_date = date('Y-m-d H:i:s', strtotime($start_date));
        $end_date = date('Y-m-d H:i:s', strtotime($end_date));

        Db::startTrans();
        $time = date("Y-m-d H:i:s");

        $r_config = GroupConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('end_time')->select()->toArray();
        if(!$r_config)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请先设置拼团设置！';
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('go_group.0');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }

        if($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 活动名称不能为空！';
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('go_group.1');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }
        else
        {
            if($id == '')
            {
                $r0 = GroupActivityModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0,'name'=>$name])->field('id')->select()->toArray();
            }
            else
            {
                $r0 = GroupActivityModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 活动名称请勿重复！';
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('go_group.2');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
        }

        $groupRule_list = json_decode($groupRule,true);
        foreach ($groupRule_list as $k_list => $v_list)
        {
            if($v_list['openDiscount'] >= $v_list['canDiscount'])
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .  $v_list['num'] . '人团 参团优惠必须小于团长优惠！';
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('go_group.8');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
        }

        if($start_date >= $end_date)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 拼团活动时间错误！';
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('go_group.3');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }

        if($start_date > $time)
        {
            $status = 0;
            $is_show = 0;
        }
        else if($start_date <= $time && $time < $end_date)
        {
            $status = 1;
        }
        else
        {
            $status = 2;
        }

        if($goodsIds == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择商品！';
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('go_group.4');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }
        else
        {
            $p_list = explode(',',$goodsIds);
            
            $sql1 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0);
            $r1 = ProductListModel::where($sql1)->whereIn('id',$goodsIds)->field('id')->select()->toArray();
            if(!$r1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品ID错误！参数：' . json_encode($sql1);
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('Parameter error');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
        }

        if($id == '')
        {
            $sql_insert = array('store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'status'=>$status,'start_date'=>$start_date,'end_date'=>$end_date,'team_rule'=>$groupRule,'team_limit'=>$team_limit,'goods_limit'=>$goods_limit,'add_date'=>$time,'is_show'=>$is_show,'is_custom'=>$is_custom);
            $r = Db::name('group_activity')->insertGetId($sql_insert);
            if($r > 0)
            {
                foreach($p_list as $k => $v)
                {
                    $sql_g_insert = array('activity_id'=>$r,'goods_id'=>$v,'add_date'=>$time);
                    $r_g = Db::name('group_goods')->insert($sql_g_insert);
                    if($r_g < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 店主' . $mch_id . '添加拼团商品失败！参数：' . json_encode($sql_g_insert);
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('go_group.5');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }
                }
                
                $Log_content = __METHOD__ . '->' . __LINE__ .  ' 店主' . $mch_id . '添加拼团活动成功！';
                $this->Log($Log_content);
                Db::commit();
                $Jurisdiction->admin_record($store_id, $operator, '添加了拼团活动ID：'.$r,1,$source,$shop_id,$operator_id);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,  'message' => $message));
                exit;
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 店主' . $mch_id . '添加拼团活动失败！参数：' . json_encode($sql_insert);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '添加了拼团活动ID：'.$r.'失败',1,$source,$shop_id,$operator_id);
                $message = Lang('go_group.5');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
        }
        else
        {
            $sql_where = array('id'=>$id,'store_id'=>$store_id,'mch_id'=>$mch_id);
            $sql_update = array('name'=>$name,'status'=>$status,'start_date'=>$start_date,'end_date'=>$end_date,'team_rule'=>$groupRule,'team_limit'=>$team_limit,'goods_limit'=>$goods_limit,'is_show'=>$is_show,'is_custom'=>$is_custom);
            $r = Db::name('group_activity')->where($sql_where)->update($sql_update);
            if($r == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 店主' . $mch_id . '修改拼团活动失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '编辑了拼团活动ID：'.$id.'失败',2,$source,$shop_id,$operator_id);
                $message = Lang('go_group.6');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
            else
            {
                Db::table('lkt_group_goods')->where('activity_id',$id)->delete();

                foreach($p_list as $k => $v)
                {
                    $sql_g_insert = array('activity_id'=>$id,'goods_id'=>$v,'add_date'=>$time);
                    $r_g = Db::name('group_goods')->insert($sql_g_insert);
                    if($r_g < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 店主' . $mch_id . '添加拼团商品失败！参数：' . json_encode($sql_g_insert);
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('go_group.6');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }
                }

                $Log_content = __METHOD__ . '->' . __LINE__ .  ' 店主' . $mch_id . '修改拼团活动成功！';
                $this->Log($Log_content);
                Db::commit();
                $Jurisdiction->admin_record($store_id, $operator, '编辑了拼团活动ID：'.$id,2,$source,$shop_id,$operator_id);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,  'message' => $message));
                exit;
            }
        }
    }

    // 查看详情
    public function GroupActivityById($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 活动ID

        $data = array();
        
        $field = " id,name,start_date as startDate,end_date as endDate,team_rule as rule,team_limit as teamLimit,goods_limit as goodsLimit,is_show,is_custom as isCustom ";
        $sql0 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0,'id'=>$id);
        $r0 = GroupActivityModel::where($sql0)->field($field)->select()->toArray();
        if($r0)
        {
            $r0[0]['rule'] = json_decode($r0[0]['rule'],true);

            $goodsIdList = array();
            $sql1 = array('activity_id'=>$id,'recycle'=>0);
            $r1 = GroupGoodsModel::where($sql1)->field('goods_id')->select()->toArray();
            if($r1)
            {
                foreach($r1 as $k1 => $v1)
                {
                    $goodsIdList[] = $v1['goods_id'];
                }
            }
            $r0[0]['goodsIdList'] = $goodsIdList;
            $data = $r0[0];
        }

        return $data;
    }

    // 开始/结束
    public function StartGroup($array)
    {
        $Jurisdiction = new Jurisdiction();
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 活动ID
        $type = $array['type']; // 1.开始 0.结束
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺
        $time = date("Y-m-d H:i:s");

        $shop_id = '';
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }

        if($type == 1)
        {
            $sql_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0,'id'=>$id);
            $sql_update = array('status'=>1,'start_date'=>$time);
            $r = Db::name('group_activity')->where($sql_where)->update($sql_update);
            if($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '将拼团活动ID：'.$id.'，进行了开始操作失败',2,$source,$shop_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 店主' . $mch_id . '修改拼团活动失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('go_group.6');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '将拼团活动ID：'.$id.'，进行了开始操作',2,$source,$shop_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ .  ' 店主' . $mch_id . '修改拼团活动成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,  'message' => $message));
                exit;
            }
        }
        else
        {
            $sql_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0,'id'=>$id);
            $sql_update = array('status'=>2,'end_date'=>$time,'is_show'=>0);
            $r = Db::name('group_activity')->where($sql_where)->update($sql_update);
            if($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '将拼团活动ID：'.$id.'，进行了结束操作失败',2,$source,$shop_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 店主' . $mch_id . '修改拼团活动失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('go_group.6');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
            else
            {
                // 根据拼团活动ID、拼团中，查询开团数据
                $sql2 = "select id,sno,goods_id,attr_id from lkt_group_open where status = 0 and activity_id = '$id' ";
                $r2 = Db::query($sql2);
                if($r2)
                {
                    foreach($r2 as $k2 => $v2)
                    {
                        $open_id = $v2['id']; // 开团ID
                        $sno = $v2['sno']; // 开团订单

                        $res = $this->Team_failure_0($store_id,$open_id,$sno);
                        if($res != 1)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 店主' . $mch_id . '修改拼团活动失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                            $this->Log($Log_content);
                            $message = Lang('go_group.6');
                            echo json_encode(array('code' => 109,  'message' => $message));
                            exit;
                        }
                    }
                }
                
                $Jurisdiction->admin_record($store_id, $operator, '将拼团活动ID：'.$id.'，进行了结束操作',2,$source,$shop_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ .  ' 店主' . $mch_id . '修改拼团活动成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,  'message' => $message));
                exit;
            }
        }
    }

    // 删除
    public function DelGroup($array)
    {
        $Jurisdiction = new Jurisdiction();
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 活动ID
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺

        $shop_id = '';
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }

        Db::startTrans();
        $id_list = explode(',',$id);

        if(count($id_list) > 0)
        {
            foreach($id_list as $k => $v)
            {
                $sql_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0,'id'=>$v);
                $sql_update = array('recycle'=>1);
                $r = Db::name('group_activity')->where($sql_where)->update($sql_update);
                if($r < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 店主' . $mch_id . '删除拼团活动失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                    $this->Log($Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '删除了拼团活动ID：'.$v.'失败',3,$source,$shop_id,$operator_id);
                    $message = Lang('go_group.7');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
                
                $Jurisdiction->admin_record($store_id, $operator, '删除了拼团活动ID：'.$v,3,$source,$shop_id,$operator_id);
            }
        }
        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200,  'message' => $message));
        exit;
    }

    // 是否显示
    public function GroupIsShowSwitch($array)
    {
        $Jurisdiction = new Jurisdiction();
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 活动ID
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺

        $shop_id = '';
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }

        $sql_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0,'id'=>$id);

        $r0 = GroupActivityModel::where($sql_where)->field('is_show')->select()->toArray();
        if($r0)
        {
            if($r0[0]['is_show'] == 1)
            {
                $sql_update = array('is_show'=>0);
            }
            else
            {
                $sql_update = array('is_show'=>1);
            }
            $r = Db::name('group_activity')->where($sql_where)->update($sql_update);
            if($r < 1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '将拼团活动ID：'.$id.'，进行了是否显示操作失败',2,$source,$shop_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 店主' . $mch_id . '修改拼团活动失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('go_group.6');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
        }

        $Jurisdiction->admin_record($store_id, $operator, '将拼团活动ID：'.$id.'，进行了是否显示操作',2,$source,$shop_id,$operator_id);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,  'message' => $message));
        exit;
    }

    // 开团记录
    public function OpenRecordList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $activityId = $array['activityId']; // 拼团活动ID
        $status = $array['status']; // 商品拼团状态 0=拼团中 1=拼团成功 2=拼团失败
        $teamName = $array['teamName']; // 团长名称
        $key = $array['key']; // 商品名称、商品ID
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页数据
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " b.store_id = '$store_id' and b.mch_id = '$mch_id' and a.recycle = 0 ";
        if($activityId != '')
        {
            $condition .= " and a.activity_id = '$activityId' ";
        }

        if($status != '')
        {
            $condition .= " and a.status = '$status' ";
        }

        if($teamName != '')
        {
            $condition .= " and u.user_name like '%$teamName%' ";
        }

        if($key != '')
        {
            $condition .= " and (p.id = '$key' or p.product_title like '%$key%') ";
        }

        $list = array();
        $total = 0;

        $sql0 = "select count(a.id) as total from lkt_group_open as a left join lkt_group_activity as b on a.activity_id = b.id left join lkt_user as u on a.user_id = u.user_id left join lkt_product_list as p on a.goods_id = p.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }
        $str = " a.id,a.activity_id as acId,a.goods_id as goodsId,a.attr_id,a.status,a.add_date as openStartDate,a.team_num as groupType,a.team_commission as teamPrice,b.team_limit as teamLimit,b.start_date as startDate,b.end_date as endDate,p.product_title as name,p.imgurl as imgUrl,u.user_name as teamName ";

        $sql1 = "select $str from lkt_group_open as a left join lkt_group_activity as b on a.activity_id = b.id left join lkt_user as u on a.user_id = u.user_id left join lkt_product_list as p on a.goods_id = p.id where $condition order by a.add_date desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $v['imgUrl'] = ServerPath::getimgpath($v['imgUrl'], $store_id);
                if($v['status'] == 1)
                {
                    $v['statusName'] = '拼团成功';
                }
                else if($v['status'] == 2)
                {
                    $v['statusName'] = '拼团失败';
                }
                else
                {
                    $v['statusName'] = '拼团中';
                }

                $v['endDate'] = date("Y-m-d",strtotime($v['endDate']));
                $acId = $v['acId'];
                $groupType = $v['groupType'];
                $v['groupType'] = $v['groupType'] . '人团';
                $v['openEndDate'] = $v['endDate'];

                $goodsId = $v['goodsId'];
                $attr_id = $v['attr_id'];

                $v['price'] = 0;
                $sql2 = "select price from lkt_configure where id = '$attr_id' and pid = '$goodsId' ";
                $r2 = Db::query($sql2);
                if($r2)
                {
                    $v['price'] = round($r2[0]['price'],2);
                }

                if($v['teamPrice'] == '0.00')
                {
                    $sql_0 = "select team_rule from lkt_group_activity where store_id = '$store_id' and id = '$acId' ";
                    $r_0 = Db::query($sql_0);
                    $team_rule = json_decode($r_0[0]['team_rule'],true);
                    foreach($team_rule as $k_rule => $v_rule)
                    {
                        $man = $v_rule['num'];
                        $openDiscount = round(($v_rule['openDiscount'] / 100),2);
                        if($man == $groupType)
                        {
                            $v['teamPrice'] = number_format (($v['price'] * $openDiscount),2);
                        }
                    }
                }

                if($source == 3)
                {
                    $v['stringPrice'] = $v['price'];
                    $v['stringTeamPrice'] = $v['teamPrice'];
                }

                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 开团详情
    public function OpenRecordDetailList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 拼团活动ID
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页数据
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " a.recycle = 0 and a.open_id = '$id' and u.store_id = '$store_id' ";

        $list = array();
        $total = 0;

        $sql0 = "select count(a.id) as total from lkt_group_open_record as a left join lkt_user as u on a.user_id = u.user_id left join lkt_product_list as p on a.goods_id = p.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $str = " a.id,a.open_id,a.goods_id as goodsId,a.attr_id,a.attr_price as price,a.price as joinPrice,a.user_id,a.add_date as joinDate,p.product_title as name,p.imgurl as imgUrl,u.user_name as userName ";

        $sql1 = "select $str from lkt_group_open_record as a left join lkt_user as u on a.user_id = u.user_id left join lkt_product_list as p on a.goods_id = p.id where $condition order by a.add_date desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $num = count($r1) - 1;
            foreach($r1 as $k => $v)
            {
                $v['imgUrl'] = ServerPath::getimgpath($v['imgUrl'], $store_id);
                $open_id = $v['open_id'];
                $goodsId = $v['goodsId'];
                $attr_id = $v['attr_id'];
                $v['stringJoinPrice'] = $v['joinPrice']; // 零售价
                $v['stringPrice'] = $v['price']; // 参团金额
                $v['price'] = round($v['price'],2);
                $v['joinPrice'] = round($v['joinPrice'],2); // 参团金额

                $attribute_str = '';
                $sql_configure = "select attribute from lkt_configure where id = '$attr_id' and pid = '$goodsId' ";
                $r_configure = Db::query($sql_configure);
                if($r_configure)
                {
                    $attribute = unserialize($r_configure[0]['attribute']);
                    foreach ($attribute as $k_attribute => $v_attribute)
                    {
                        if (strpos($k_attribute, '_LKT_') !== false)
                        {
                            $k_attribute = substr($k_attribute, 0, strrpos($k_attribute, "_LKT"));
                            $v_attribute = substr($v_attribute, 0, strrpos($v_attribute, "_LKT"));
                        }
                        $attribute_str .= $k_attribute . ":" . $v_attribute . ',';
                    }
                    $attribute_str = rtrim($attribute_str, ',');
                }
                $v['attrName'] = $attribute_str;

                $v['type'] = '团员';

                $sql2 = "select user_id from lkt_group_open where id = '$open_id' ";
                $r2 = Db::query($sql2);
                if($r2[0]['user_id'] == $v['user_id'])
                {
                    $v['type'] = '团长';
                }

                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 参团记录
    public function JoinRecordList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $status = $array['status']; // 商品拼团状态 0=拼团中 1=拼团成功 2=拼团失败
        $userName = $array['userName']; // 用户名称
        $teamName = $array['teamName']; // 团长名称
        $key = $array['key']; // 商品名称、商品ID
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页数据
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " p.store_id = '$store_id' and p.mch_id = '$mch_id' and a.recycle = 0 ";

        if($status != '')
        {
            $condition .= " and b.status = '$status' ";
        }

        if($userName != '')
        {
            $condition .= " and u.user_name like '%$userName%' ";
        }
        if($teamName != '')
        {
            $condition .= " and u1.user_name like '%$teamName%' ";
        }

        if($key != '')
        {
            $condition .= " and (p.id = '$key' or p.product_title like '%$key%') ";
        }

        $list = array();
        $total = 0;

        $sql0 = "select count(a.id) as total from lkt_group_open_record as a left join lkt_group_open as b on a.open_id = b.id RIGHT join lkt_user as u1 on b.user_id = u1.user_id left join lkt_user as u on a.user_id = u.user_id left join lkt_product_list as p on a.goods_id = p.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $str = " a.id,a.goods_id as goodsId,a.attr_id,a.attr_price as price,a.price as joinPrice,a.add_date as joinDate,b.team_num as groupType,b.status,p.product_title as name,p.imgurl as imgUrl,u1.user_name as teamName,u.user_name as userName ";

        $sql1 = "select $str from lkt_group_open_record as a left join lkt_group_open as b on a.open_id = b.id RIGHT join lkt_user as u1 on b.user_id = u1.user_id left join lkt_user as u on a.user_id = u.user_id left join lkt_product_list as p on a.goods_id = p.id where $condition order by a.add_date desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $v['imgUrl'] = ServerPath::getimgpath($v['imgUrl'], $store_id);
                $goodsIda = $v['goodsId'];
                $attr_id = $v['attr_id'];
                $v['price'] = round($v['price'],2);
                $v['joinPrice'] = round($v['joinPrice'],2);

                $attribute_str = '';
                $sql_configure = "select attribute from lkt_configure where id = '$attr_id' and pid = '$goodsIda' ";
                $r_configure = Db::query($sql_configure);
                if($r_configure)
                {
                    $attribute = unserialize($r_configure[0]['attribute']);
                    foreach ($attribute as $k_attribute => $v_attribute)
                    {
                        if (strpos($k_attribute, '_LKT_') !== false)
                        {
                            $k_attribute = substr($k_attribute, 0, strrpos($k_attribute, "_LKT"));
                            $v_attribute = substr($v_attribute, 0, strrpos($v_attribute, "_LKT"));
                        }
                        $attribute_str .= $k_attribute . ":" . $v_attribute . ',';
                    }
                    $attribute_str = rtrim($attribute_str, ',');
                }
                $v['attrName'] = $attribute_str;
                $v['groupType'] = $v['groupType'] . '人团';

                if($v['status'] == 1)
                {
                    $v['statusName'] = '拼团成功';
                }
                else if($v['status'] == 2)
                {
                    $v['statusName'] = '拼团失败';
                }
                else
                {
                    $v['statusName'] = '拼团中';
                }

                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 删除参团记录
    public function DelJoinRecord($array)
    {
        $Jurisdiction = new Jurisdiction();
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // 拼团活动ID
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺

        $shop_id = '';
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }

        $sql0 = "select d.user_name,b.activity_id from lkt_group_open_record as a left join lkt_group_open as b on a.open_id = b.id RIGHT JOIN lkt_user as d on a.user_id = d.user_id where a.id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $user_name = $r0[0]['user_name'];
            $activity_id = $r0[0]['activity_id'];
        }
        else
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }

        $sql_where = array('id'=>$id);
        $sql_update = array('recycle'=>1);
        $r = Db::name('group_open_record')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了拼团活动ID：'.$activity_id.'，用户名称：'.$user_name.'的参团记录',3,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除参团记录成功！ID为 ' . $id;
            $this->Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array('code' => 200,  'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了拼团活动ID：'.$activity_id.'，用户名称：'.$user_name.'的参团记录失败',3,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除参团记录失败！ID为 ' . $id;
            $this->Log($Log_content);
            $message = Lang('Abnormal business');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }
    }

    // 拼团订单
    public function OrderIndex($array)
    {
        $store_id = $array['store_id'];
        $shop_id = $array['mch_id'];
        $selfLifting = $array['selfLifting']; // 9.成功 21.失败
        $keyWord = $array['keyWord']; // PC店铺端：订单号/姓名/电话/用户ID；移动端店铺：订单号
        $status = $array['status']; // 状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭
        $startDate = $array['startDate']; // 开始时间
        $endDate = $array['endDate']; // 结束时间
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页数据
        $source = $array['source']; // 1.平台 2.PC店铺 3.移动端店铺

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $condition = " o.store_id = '$store_id' and lu.store_id = '$store_id' and o.self_lifting = 0 and o.otype = 'PT' and o.status != 0 ";
        if($shop_id != 0 && $shop_id != '')
        {
            $condition .= " and m.id = '$shop_id' ";
        }

        if($source == 2)
        {
            $condition .= " and o.recycle not in (1,3) ";
            if ($keyWord != '')
            {   
                $condition .= " and (o.sNo like '%$keyWord%' or o.p_sNo like '%$keyWord%' or o.name like '%$keyWord%' or o.mobile like '%$keyWord%' or o.user_id = '$keyWord') ";
            }
        
            if ($startDate != '')
            {
                $condition .= " and o.add_time >= '$startDate' ";
            }
            if ($endDate != '')
            {
                $condition .= " and o.add_time <= '$endDate' ";
            }
        }
        else if($source == 3)
        {
            $condition .= " and o.recycle not in (1,3) ";
            if($selfLifting == 9)
            {
                $condition .= " AND EXISTS (SELECT x.id FROM lkt_group_open x INNER JOIN lkt_group_open_record y ON x.id = y.open_id WHERE y.sno = o.sno AND x.STATUS = 1 ) ";
            }
            else
            {
                $condition .= " AND EXISTS (SELECT x.id FROM lkt_group_open x INNER JOIN lkt_group_open_record y ON x.id = y.open_id WHERE y.sno = o.sno AND x.STATUS = 2 ) ";
            }

            if ($keyWord != '')
            {   
                $condition .= " and (o.sNo like '%$keyWord%' or o.p_sNo like '%$keyWord%') ";
            }
        }
        else
        {
            $condition .= " and o.recycle != 1 ";
            if ($keyWord != '')
            {   
                $condition .= " and (o.sNo like '%$keyWord%' or o.p_sNo like '%$keyWord%' or o.name like '%$keyWord%' or o.mobile like '%$keyWord%' or o.user_id = '$keyWord') ";
            }
        
            if ($startDate != '')
            {
                $condition .= " and o.add_time >= '$startDate' ";
            }
            
            if ($endDate != '')
            {
                $condition .= " and o.add_time <= '$endDate' ";
            }
        }
       
        if($status != '')
        {
            $condition .= " and o.status = '$status' ";
        }
        
        $list = array();
        $total = 0;

        $payments_type = array();
        //支付方式
        $payments = PaymentModel::order('sort','desc')->select()->toArray();
        foreach ($payments as $keyp => $valuep)
        {
            $payments_type[$valuep['class_name']] = $valuep['name'];
        }

        $str = " o.id,o.user_id as userId,o.name as userName,o.mobile,o.num,o.sNo,o.z_price as orderPrice,o.sheng,o.shi,o.xian,o.address,o.pay,o.add_time as createDate,o.status,o.otype,o.allow,o.real_sno as mchOrderNo,o.operation_type,o.self_lifting,d.id as detailId,d.p_name as goodsName,d.num as needNum,d.after_discount,d.size as attrStr,d.courier_num,d.freight,attr.pid as goodsId,attr.img as goodsImgUrl,attr.price as goodsPrice,m.name as shopName,row_number () over (PARTITION BY o.sNo) AS top ";

        if($source == 3)
        {
            $sql0 = "select ifnull(count(a.sNo),0) as num from (select o.sNo
                    from lkt_order as o 
                    left join lkt_user as lu on o.user_id = lu.user_id 
                    right join lkt_order_details as d on o.sNo = d.r_sNo
                    RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                    right join lkt_product_list as p on p.id = attr.pid
                    right join lkt_mch as m on p.mch_id = m.id
                    where $condition group by o.sNo) a ";

            $sql1 = "select tt.* from (select $str 
                    from lkt_order as o 
                    left join lkt_user as lu on o.user_id = lu.user_id 
                    right join lkt_order_details as d on o.sNo = d.r_sNo
                    RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                    RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                    right join lkt_mch as m on p.mch_id = m.id
                    where $condition ) AS tt WHERE tt.top < 2 order by tt.createDate desc limit $start,$pagesize";
        }
        else
        {
            $condition .= " and ao.status != 0 ";
            $sql0 = "select ifnull(count(a.sNo),0) as num from (select o.sNo
                    from lkt_order as o 
                    left join lkt_group_open_record as ar on o.sNo = ar.sno 
                    right join lkt_group_open as ao on ar.open_id = ao.id
                    left join lkt_user as lu on o.user_id = lu.user_id 
                    right join lkt_order_details as d on o.sNo = d.r_sNo
                    RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                    right join lkt_product_list as p on p.id = attr.pid
                    right join lkt_mch as m on p.mch_id = m.id
                    where $condition group by o.sNo) a ";

            $sql1 = "select tt.* from (select $str 
                    from lkt_order as o 
                    left join lkt_group_open_record as ar on o.sNo = ar.sno 
		            right join lkt_group_open as ao on ar.open_id = ao.id
                    left join lkt_user as lu on o.user_id = lu.user_id 
                    right join lkt_order_details as d on o.sNo = d.r_sNo
                    RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                    RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                    right join lkt_mch as m on p.mch_id = m.id
                    where $condition ) AS tt WHERE tt.top < 2 order by tt.createDate desc limit $start,$pagesize";
        }
        
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }
        
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $sNo = $v['sNo'];
                if ($sNo !== false)
                {
                    $message_logging_list_1 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$sNo,'type'=>1);
                    PC_Tools::message_pop_up($message_logging_list_1);
                    PC_Tools::message_read($message_logging_list_1);
                    $message_logging_list_3 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$sNo,'type'=>3);
                    PC_Tools::message_pop_up($message_logging_list_3);
                    PC_Tools::message_read($message_logging_list_3);
                    $message_logging_list_5 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$sNo,'type'=>5);
                    PC_Tools::message_pop_up($message_logging_list_5);
                    PC_Tools::message_read($message_logging_list_5);

                    if($v['status'] == 5)
                    {
                        $message_logging_list_6 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$sNo,'type'=>6);
                        PC_Tools::message_pop_up($message_logging_list_6);
                        PC_Tools::message_read($message_logging_list_6);
                    }
                    if($v['status'] == 7)
                    {
                        $message_logging_list_4 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$sNo,'type'=>4);
                        PC_Tools::message_pop_up($message_logging_list_4);
                        PC_Tools::message_read($message_logging_list_4);
                    }
                }

                $r1[$k]['logistics_type'] = false;
                $sql_express_delivery = "select * from lkt_express_delivery where store_id = '$store_id' and sNo = '$sNo' and subtable_id != 0";
                $r_express_delivery = Db::query($sql_express_delivery);
                if($r_express_delivery)
                {
                    $r1[$k]['logistics_type'] = true;
                }

                $r1[$k]['haveReturnOrder'] = 0; // 0.没有售后未结束(可以发货)       1.有售后未结束(不可以发货)   

                $sql_r = "select * from lkt_return_order where sNo = '$sNo' and r_type not in (2,4,8,9,10,12) ";
                $r_r = Db::query($sql_r);
                if($r_r)
                {
                    $r1[$k]['haveReturnOrder'] = 1; // 0.没有售后未结束(可以发货)       1.有售后未结束(不可以发货)   
                }

                $r1[$k]['orderno'] = $v['sNo'];
                $r1[$k]['addressInfo'] = $v['sheng'] . $v['shi'] . $v['xian'] . $v['address'];
                $r1[$k]['mchName'] = $v['shopName'];
                $r1[$k]['goodsImgUrl'] = ServerPath::getimgpath($v['goodsImgUrl'], $store_id);
                $r1[$k]['orderDetailIds'] = $v['detailId'];
                $r1[$k]['orderAllow'] = $v['allow'];

                $freight = 0;
                
                $pay = $v['pay'];
                if (array_key_exists($pay, $payments_type))
                {
                    $r1[$k]['payName'] = $payments_type[$pay];
                }
                else
                {
                    $r1[$k]['payName'] = '钱包';
                }

                $r1[$k]['groupType'] = "人团";
                $r1[$k]['groupOrderStatus'] = "0";
                $r1[$k]['endDate'] = "";
                $sql2 = "select ao.team_num,ao.status,ao.end_date,ao.update_date from lkt_group_open_record as ar left join lkt_group_open as ao on ar.open_id = ao.id where ar.sno = '$sNo' ";
                $r2 = Db::query($sql2);
                if($r2)
                {
                    $r1[$k]['groupType'] = $r2[0]['team_num'] . "人团";
                    $r1[$k]['groupOrderStatus'] = $r2[0]['status'];
                    $r1[$k]['endDate'] = $r2[0]['update_date'];
                }

                if($v['status'] == 7)
                {
                    $sql_r = "select audit_time from lkt_return_order where sNo = '$sNo' and p_id = " . $v['detailId'];
                    $r_r = Db::query($sql_r);
                    if($r_r)
                    {
                        $r1[$k]['endDate'] = $r_r[0]['audit_time'];
                    }

                    $sql_r1 = "select sum(real_money) as real_money from lkt_return_order where sNo = '$sNo'";
                    $r_r1 = Db::query($sql_r1);
                    if($r_r1)
                    {
                        $r1[$k]['return_money'] = $r_r1[0]['real_money'];
                    }
                }

                $sqldt = "select lpl.imgurl,lpl.product_title,lpl.product_number,lod.p_price,lod.unit,lod.num,lod.size,lod.p_id,lod.courier_num,lod.express_id,lod.freight,lpl.brand_id ,lm.name as mchname,lod.r_status,lod.id
                        from lkt_order_details as lod 
                        left JOIN lkt_configure attr ON attr.id=lod.sid
                        left join lkt_product_list as lpl on lpl.id=attr.pid 
                        LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id 
                        where r_sNo= '$sNo' ";
                $products = Db::query($sqldt);
                if ($products)
                {
                    $courier_num = array();       // 快递单号
                    foreach ($products as $kd => $vd)
                    {
                        $freight += $vd['freight'];

                        $vd['imgurl'] = ServerPath::getimgpath($vd['imgurl'], $store_id);
                        if ($vd['express_id'])
                        {
                            $exper_id = $vd['express_id'];
                            $r03 = ExpressModel::where('id',$exper_id)->select()->toArray();
                            $courier_num[] = $vd['courier_num'] . "  (" . $r03[0]['kuaidi_name'] . ")"; // 快递单号
                        }
                        $products[$kd] = $vd;

                        $r_status = $vd['r_status']; // 订单详情状态
                        if($r_status != 7)
                        {
                            $res_o = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$v['sNo'],'r_status'=>$r_status])->count();
                        }
                        else
                        {
                            $res_o = 0;
                        }
                        $res_d = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$v['sNo'],'r_status'=>7])->count();
                        $res_s = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$v['sNo']])->count();
                        // 如果订单下面的商品都处在同一状态,那就改订单状态为已完成
                        if (($res_o + $res_d) == $res_s)
                        {
                            //如果订单数量相等 则修改父订单状态
                            $condition = "";
                            $sql = OrderModel::where(['store_id'=>$store_id,'sNo'=>$v['sNo']])->find();
                            $sql->status = $r_status;
                            if($r_status == 5 && empty($v['arrive_time']))
                            {
                                $sql->arrive_time = date("Y-m-d H:i:s");
                            }
                            $sql->save();
                        }
                    }
                    $courier_num_arr = array();
                    $expressStr = ',';
                    foreach ($courier_num as $kkk => $vvv)
                    {
                        if (!in_array($vvv, $courier_num_arr))
                        {
                            $courier_num_arr[] = $vvv;
                            $expressStr .= $vvv.',';
                        }
                    }
                    $r1[$k]['goodsNum'] = count($products);
                    $r1[$k]['expressList'] = $courier_num_arr;
                    $r1[$k]['expressStr'] = $expressStr;
                    
                    switch ($v['status'])
                    {
                        case 0 :
                            $r1[$k]['status'] = '待付款';
                            break;
                        case 1 :
                            $r1[$k]['status'] = '待发货';
                            break;
                        case 2 :
                            $r1[$k]['status'] = '待收货';
                            break;
                        case 5 :
                            $r1[$k]['status'] = '订单完成';
                            break;
                        case 7 :
                            $r1[$k]['status'] = '订单关闭';
                            break;
                    }
                    $r1[$k]['otype'] = '拼团';
                    switch ($v['self_lifting']) 
                    {
                        case '1':
                            $r1[$k]['selfLiftingName'] = '自提';
                            break;
                        
                        default:
                            $r1[$k]['selfLiftingName'] = '快递';
                            break;
                    }

                    $r1[$k]['freight'] = $freight;
                    $list[] = $r1[$k];
                }
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("plugin/Go_group.log",$Log_content);
        return;
    }
    
    // 获取插件状态（用户）
    public function Get_plugin_status($store_id)
    {
        $is_status = 0;
        $r0 = GroupOrderConfigModel::where(['store_id'=>$store_id])->field('is_open')->select()->toArray();
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
        $sql0 = "select status from lkt_plugins where store_id = '$store_id' and plugin_code = 'go_group' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $status = $r0[0]['status'];
        }
        
        return $status;
    }
}