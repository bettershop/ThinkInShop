<?php
namespace app\common\Plugin;
use think\facade\Db;
use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\GETUI\LaikePushTools;
use app\common\DeliveryHelper;
use app\common\Order;
use app\common\ServerPath;
use app\common\Jurisdiction;

use app\admin\model\AuctionConfigModel;
use app\admin\model\AuctionSpecialModel;
use app\admin\model\AuctionRecordModel;
use app\admin\model\AuctionSessionModel;
use app\admin\model\AuctionProductModel;
use app\admin\model\CustomerModel;
use app\admin\model\UserAddressModel;
use app\admin\model\MchModel;
use app\admin\model\AuctionPromiseModel;
use app\admin\model\OrderDataModel;
use app\admin\model\UserModel;
use app\admin\model\OrderModel;
use app\admin\model\UserRuleModel;
use app\admin\model\UserGradeModel;
use app\admin\model\AuctionRemindModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProductClassModel;
use app\admin\model\PaymentModel;
use app\admin\model\ExpressModel;

class Auction 
{
    // 获取插件状态
    public function is_Plugin($store_id)
    {
        $r0 = AuctionConfigModel::where(['store_id'=>$store_id])->field('is_open')->select()->toArray();
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
    
    // 判断竞拍插件是否开启
    public function test($store_id)
    {
        $res = AuctionConfigModel::where('store_id', $store_id)
                                ->field('is_open')
                                ->select()->toArray();
        if ($res)
        {
            $is_open = $res[0]['is_open'];
        }
        else
        {
            $is_open = 0;
        }
        return $is_open;
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

        $address_id = Request::post('addressId'); //  地址id
        $auction_id = addslashes(Request::post('mainId')); // 竞拍商品ID
        $lktlog = new LaiKeLogUtils();

        // 支付状态
        $payment = Tools::getPayment($store_id);
        $user_score = $user['score']; // 用户积分

        // 用户基本信息
        list($user_money, $enterless, $password_status) = Tools::userInfo($user, $store_id, $user_id,$lktlog);

        $products = Tools::products_JP_list($store_id, $auction_id); //商品数组（pid,cid,num)
        $products_total = 0;

        $products_data = Tools::get_products_data($store_id,$products, $products_total,'JP');
        $products_freight = $products_data['products_freight'];
        $products = $products_data['products'];

        $no_delivery_str = '';
        if($address_id == '')
        { // 获取不配送省的名称
            $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);
        }

        //查询默认地址order_details
        $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);
        $addemt = $address ? 1 : 0; // 收货地址状态

        //4.计算运费
        $freight = Tools::get_freight($products_freight, $products, $address, $store_id, 'JP');
        $products = $freight['products'];
        $yunfei = $freight['yunfei'];
        
        $res = AuctionProductModel::where(['id'=>$auction_id])->field('price,mark_up_amt,session_id,status,starting_amt')->select()->toArray();
        $products_total = $res[0]['price']; // 当前出价
        $market_price = $res[0]['mark_up_amt']; // 加价幅度

        $products_0 = array();
        $products_0['id'] = $auction_id;
        $products_0['goods_id'] = $products[0]['pid'];
        $products_0['attr_id'] = $products[0]['cid'];
        $products_0['num'] = 1;
        $products_0['freight'] = $products[0]['freight'];
        $products_0['imgurl'] = $products[0]['img'];
        $products_0['product_title'] = $products[0]['product_title'];
        $products_0['mch_id'] = $products[0]['mch_id'];
        $products_0['size'] = $products[0]['size'];
        $products_0['volume'] = $products[0]['volume'];
        $products_0['weight'] = $products[0]['weight'];
        $products_0['goodsPrice'] = round($res[0]['price'],2);
        $products_0['sessionId'] = $res[0]['session_id'];
        $products_0['status'] = $res[0]['status'];
        $products_0['startingAmt'] = round($res[0]['starting_amt'],2);
        $products_0['user_id'] = $user_id;

        $mch_id = $products[0]['mch_id'];

        $r_mch = MchModel::where(['id'=>$mch_id])->field('name as shop_name,head_img as shop_logo')->select()->toArray();
        $products_0['shop_name'] = $r_mch[0]['shop_name'];
        $products_0['shop_logo'] = $r_mch[0]['shop_logo'];

        // 计算会员特惠
        $grade = $this->user_grade('JP', $products_total, $user_id, $store_id);
        $grade_rate = floatval($grade['rate']);
        $grade_rate_amount = round($products_total-($products_total * $grade_rate),2);

        // 计算竞拍价+运费
        $total = $products_total * $grade_rate + $yunfei; //竞拍商品不支持，优惠券，满减，红包
        $total = round($total,2);

        $data = array('payment' => $payment,'addemt' => $addemt,'address' => $address,'enterless' => $enterless,'freight' => $yunfei,'money' => round($products_total,2),'password_status' => $password_status,'products' => $products_0,'total' => $total,'user_money' => $user_money,'user_score' => $user_score);
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

        $address_id = Request::post('address_id') ? Request::post('address_id') : Request::post('addressId'); //  地址id
        $auction_id = addslashes(Request::post('mainId')); // 竞拍商品ID
        $time = date("Y-m-d H:i:s");

        $products = Tools::products_JP_list($store_id, $auction_id); //商品数组（pid,cid,num)

        $products_data = Tools::get_products_data($store_id,$products, 0,'JP');
        $products_freight = $products_data['products_freight'];
        $products = $products_data['products'];

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

        $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);
        if(!$address)
        {
            $message = Lang('nomal_order.1');
            echo json_encode(array('code' => 220,'message'=>$message));
            exit;
        }
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
        $freight = Tools::get_freight($products_freight, $products, $address, $store_id, 'JP');
        $products = $freight['products'];
        $yunfei = $freight['yunfei'];

        $res = AuctionProductModel::where(['id'=>$auction_id])->field('price,mark_up_amt,session_id,status,starting_amt')->select()->toArray();
        $products_total = $res[0]['price']; // 当前出价

        // 计算会员特惠
        $grade = $this->user_grade('JP', $products_total, $user_id, $store_id);
        $grade_rate = floatval($grade['rate']);
        $grade_rate_amount = round($products_total-($products_total * $grade_rate),2);

        // 计算竞拍价+运费
        $total = $products_total * $grade_rate + $yunfei; //竞拍商品不支持，优惠券，满减，红包
        $total = round($total,2);
        
        $Tools = new Tools( $store_id, 1);
        $real_sno = $Tools->Generate_order_number('JP', 'real_sno'); // 生成支付订单号

        $r0 = AuctionProductModel::where(['id'=>$auction_id,'user_id'=>$user_id])->field('sNo')->select()->toArray();
        if($r0)
        {
            $sNo = $r0[0]['sNo'];

            $r1 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->field('id,z_price')->select()->toArray();
            $order_id = $r1[0]['id'];
            $z_price = $r1[0]['z_price'];

            $sql_where = array('store_id'=>$store_id,'sNo'=>$sNo);
            $sql_update = array('name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'shop_cpc'=>$shop_cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_xq,'code'=>$code,'remind'=>$time,'real_sno'=>$real_sno,'source'=>$store_type);
            $r = Db::name('order')->where($sql_where)->update($sql_update);
        }

        $data = array('order_id' => $order_id,'sNo' => $sNo,'total' => $total,'orderTime'=>$time);
        $message = Lang("Success");
        echo json_encode(array('code' => 200,'data'=>$data,'message'=>$message));
        exit;
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
            $log->log_result($log_name, "竞拍订单回调失败信息: \n 订单：$trade_no 支付金额：$total \r\n");
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
            $log->log_result($log_name, "【竞拍订单付款金额有误】:\n 应付金额为$z_price \n");
        }
        $sql = "select * from lkt_order where sNo='$trade_no'";
        $r = Db::query($sql);
        if ($r)
        {
            $status = $r[0]['status'];
            if ($status < 1)
            {       
                $order = new Order();
                $order->up_order((array)$r[0]);
                $log->log_result($log_name, "【竞拍订单data】:\n" . json_encode((array)$r[0]) . "\n");
            }
        }
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

        $this->OpenSpecial($store_id); // 开启专场

        $this->OpenSession($store_id); // 开启场次

        $this->CloseSession($store_id); // 关闭场次

        $this->CloseSpecial($store_id); // 关闭专场

        $this->order_failure($store_id); // 删除订单过期

        return;
    }

    // 开启专场
    public function OpenSpecial($store_id)
    {
        $time = date("Y-m-d H:i:s"); //当前时间戳

        // 根据专场开始时间，查询未开始专场 
        $r0 = AuctionSpecialModel::where(['store_id'=>$store_id,'recovery'=>0,'status'=>1])->where('start_date','<=',$time)->field('id')->select()->toArray();
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $id0 = $v0['id']; // 专场ID
                // 修改专场为进行中
                $sql0_1 = array('id'=>$id0,'recovery'=>0,'status'=>1);
                $r0_1 = Db::name('auction_special')->where($sql0_1)->update(['status' => 2]);
                if($r0_1 < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 专场未开始改为进行中失败！参数:'.json_encode($sql0_1);
                    $this->Log($Log_content);
                }
                // 修改场次为进行中
                $sql0_2 = array('special_id'=>$id0,'recovery'=>0,'status'=>1);
                $r0_2 = Db::name('auction_session')->where($sql0_2)->where('start_date','<=',$time)->update(['status' => 2]);
                if($r0_2 < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 场次未开始改为进行中失败！参数:'.json_encode($sql0_2);
                    $this->Log($Log_content);
                }
                
                $r1 = AuctionRemindModel::where(['special_id'=>$id0])->field('user_id')->select()->toArray();
                if($r1)
                {
                    foreach($r1 as $k1 => $v1)
                    {
                        $user_id = $v1['user_id'];

                        $pusher = new LaikePushTools();
                        $pusher->pushMessage($user_id,'竞拍活动开始啦！', '竞拍活动马上要开始了', $store_id, '');
                    }
                }
            }
        }
        return;
    }

    // 开启场次
    public function OpenSession($store_id)
    {
        $time = date("Y-m-d H:i:s"); //当前时间戳

        // 查询进行中的专场  
        $r0 = AuctionSpecialModel::where(['store_id'=>$store_id,'recovery'=>0,'status'=>2])->field('id')->select()->toArray();
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $id = $v0['id']; // 专场ID
                // 修改专场为进行中
                $sql1 = array('special_id'=>$id,'recovery'=>0,'status'=>1);
                $r1 = Db::name('auction_session')->where($sql1)->where('start_date','<=',$time)->update(['status' => 2]);
                if($r1 < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 场次未开始改为进行中失败！参数:'.json_encode($sql1);
                    $this->Log($Log_content);
                }
            }
        }
        return;
    }

    // 关闭场次
    public function CloseSession($store_id)
    {
        $low_pepole = 1;
        $r_config = AuctionConfigModel::where(['store_id'=>$store_id])->field('low_pepole')->select()->toArray();
        if($r_config)
        {
            $low_pepole = $r_config[0]['low_pepole'];
        }
        $time = date("Y-m-d H:i:s"); // 当前时间
        // 查询已到期且状态还是进行中的场次
        $r0 = AuctionSessionModel::where(['recovery'=>0,'status'=>2])->where('end_date','<=',$time)->field('id')->select()->toArray();
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $session_id = $v0['id']; // 场次ID
                // 根据场次ID，查询竞拍商品
                $r1 = AuctionProductModel::where(['session_id'=>$session_id,'recovery'=>0])->field('id')->select()->toArray();
                if($r1)
                {
                    foreach($r1 as $k1 => $v1)
                    {
                        $auction_id = $v1['id']; // 竞拍商品ID
                        // 根据竞拍商品ID，查询出价记录
                        // $r2 = AuctionRecordModel::where(['auction_id'=>$auction_id])->field('user_id')->limit(1)->order('price','desc')->select()->toArray();
                        // 根据竞拍商品ID，查询出价记录
                        $sql2 = "select tt.* from (select user_id,max(price) over (partition by user_id) as price,row_number () over (partition by user_id) as top from lkt_auction_record where auction_id = '$auction_id' order by price desc ) as tt where tt.top < 2 ";
                        $r2 = Db::query($sql2);
                        if($r2)
                        { // 存在，有人出价（修改该商品的最终得主）
                            $user_id = $r2[0]['user_id']; // 出价最高者
                            $sql_update = array('user_id'=>$user_id);
                            //未达到最低开拍人数（该商品流拍）
                            if(count($r2) < $low_pepole)
                            {
                                $sql_update = array('status'=>3);
                            }
                            $sql_where = array('session_id'=>$session_id,'recovery'=>0,'id'=>$auction_id);
                            $r3 = Db::name('auction_product')->where($sql_where)->update($sql_update);
                            if($r3 < 1)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改最终得主失败！参数:' . json_encode($sql_where) . '---' .  json_encode($sql_update);
                                $this->Log($Log_content);
                            }
                        }
                        else
                        { // 没人出价（该商品流拍）
                            $sql_update = array('user_id'=>'','status'=>3,'sNo'=>'');
                            $sql_where = array('session_id'=>$session_id,'recovery'=>0,'id'=>$auction_id);
                            $r3 = Db::name('auction_product')->where($sql_where)->update($sql_update);
                            if($r3 < 1)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改竞拍商品状态失败！参数:' . json_encode($sql_where) . '---' .  json_encode($sql_update);
                                $this->Log($Log_content);
                            }
                        }
                    }
                }

                // 修改场次为已结束
                $sql4 = array('id'=>$session_id);
                $r4 = Db::name('auction_session')->where($sql4)->update(['status' => 3]);
                if($r4 < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 场次进行中改为已结束失败！参数:'.json_encode($sql4);
                    $this->Log($Log_content);
                }
            }
        }
        return;
    }

    // 专场结束
    public function CloseSpecial($store_id)
    {
        $time = date("Y-m-d H:i:s"); // 当前时间
        $refund_data = array();
        $Tools = new Tools( $store_id, 1);
        Db::startTrans();

        $low_pepole = 1;
        $r_config = AuctionConfigModel::where(['store_id'=>$store_id])->field('low_pepole')->select()->toArray();
        if($r_config)
        {
            $low_pepole = $r_config[0]['low_pepole'];
        }

        // 查询到期且状态还是进行中的专场
        $r0 = AuctionSpecialModel::where(['store_id'=>$store_id,'recovery'=>0,'status'=>2])->where('end_date','<=',$time)->field('id,mch_id')->select()->toArray();
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $special_id = $v0['id']; // 专场ID
                $mch_id = $v0['mch_id']; // 店铺ID
                $mch_str = ',' . $v0['mch_id'] . ','; // 店铺ID

                $winner_array = array(); // 得主数组(该专场，竞拍成功的得主)
                // 根据专场ID，查询竞拍信息
                $sql1 = "select a.id as session_id,b.id as auction_id,b.price,b.attr_id,b.goods_id from lkt_auction_session as a left join lkt_auction_product as b on a.id = b.session_id where a.special_id = '$special_id' and a.recovery = 0 and b.recovery = 0 ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    foreach($r1 as $k1 => $v1)
                    {
                        $session_id = $v1['session_id']; // 场次ID
                        $auction_id = $v1['auction_id']; // 竞拍商品ID
                        $price = $v1['price']; // 当前价格
                        $attr_id = $v1['attr_id']; // 属性ID
                        $goods_id = $v1['goods_id']; // 商品ID
                        $shop_id = $mch_id;//订单生成的店铺id根据商品计算
                        $shop_str = ',' . $mch_id . ','; // 店铺ID

                        // 根据商品ID、属性ID，查询商品数据
                        $sql1_0 = "select a.product_title,c.unit,c.attribute,a.mch_id from lkt_product_list as a left join lkt_configure as c on a.id = c.pid where c.pid = '$goods_id' and c.id = '$attr_id' ";
                        $r1_0 = Db::query($sql1_0);
                        if($r1_0)
                        {
                            $shop_id = $r1_0[0]['mch_id'];//生成订单的店铺id
                            $shop_str = ',' . $r1_0[0]['mch_id'] . ','; // 店铺ID
                            $p_name = $r1_0[0]['product_title'];
                            $unit = $r1_0[0]['unit'];
                            $specifications = '';
                            if($r1_0[0]['attribute'] != '')
                            {
                                $attribute = unserialize($r1_0[0]['attribute']);
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
                            $size = rtrim($specifications, ",");
                        }

                        $z_freight = 0;

                        // 根据竞拍商品ID，查询出价记录
                        $sql2 = "select tt.* from (select user_id,max(price) over (partition by user_id) as price,row_number () over (partition by user_id) as top from lkt_auction_record where auction_id = '$auction_id' order by price desc ) as tt where tt.top < 2 ";
                        $r2 = Db::query($sql2);
                        if($r2)
                        { // 存在出价的人
                            if(count($r2) >= $low_pepole)
                            { // 出价人数 >= 最低开拍人数 （竞拍有效）
                                $user_id = $r2[0]['user_id']; // 得主
                                if(!isset($winner_array[$user_id]))
                                { // 不存在
                                    $winner_array[] = $user_id;
                                }

                                $products = Tools::products_JP_list($store_id, $auction_id);

                                $products_total = 0;
                                $products_data = Tools::get_products_data($store_id,$products, $products_total, 'JP');//获取商品信息，运费信息
                                $products_freight = $products_data['products_freight'];
                                $products = $products_data['products'];

                                // 获取不配送省的名称
                                $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);

                                //查询默认地址order_details
                                $address = Tools::find_address($store_id, $user_id,$no_delivery_str, '');
                                if($address == array())
                                {
                                    $address['tel'] = '';
                                    $address['name'] = '';
                                    $address['sheng'] = '';
                                    $address['city'] = '';
                                    $address['quyu'] = '';
                                    $address['address'] = '';
                                }
                                $mobile = $address['tel'];
                                $name = $address['name'];
                                $sheng = $address['sheng'];
                                $shi = $address['city'];
                                $xian = $address['quyu'];
                                $address_x = $address['address'];

                                $freight = Tools::get_freight($products_freight, $products, $address, $store_id,'JP');
                                $z_freight = $freight['yunfei'];

                                $z_price = $price + $z_freight;

                                $sNo = $Tools->Generate_order_number('JP', 'sNo'); // 生成订单号
                                $real_sno = $Tools->Generate_order_number('JP', 'real_sno'); // 生成支付订单号

                                $order_failure_time = Tools::Obtain_expiration_time(array('store_id'=>$store_id,'otype'=>'JP'));

                                $sql4 = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'mobile'=>$mobile,'num'=>1,'old_total'=>0,'z_price'=>$z_price,'sNo'=>$sNo,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address_x,'remark'=>'','pay'=>'','add_time'=>$time,'status'=>0,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>'','drawid'=>0,'otype'=>'JP','ptcode'=>'','pid'=>'','ptstatus'=>'','groupman'=>'','refundsNo'=>'','trade_no'=>'','is_anonymous'=>0,'logistics_service'=>'','overall_evaluation'=>'','spz_price'=>$price,'reduce_price'=>0,'coupon_price'=>0,'red_packet'=>0,'allow'=>0,'parameter'=>'','source'=>1,'delivery_status'=>0,'readd'=>0,'offset_balance'=>0,'mch_id'=>$shop_str,'zhekou'=>0,'grade_rate'=>0,'grade_score'=>'','grade_fan'=>'','p_sNo'=>$auction_id,'bargain_id'=>0,'comm_discount'=>0,'real_sno'=>$real_sno,'orderId'=>'','baiduId'=>'','remarks'=>'','self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','is_put'=>0,'z_freight'=>$z_freight,'manual_offer'=>0,'preferential_amount'=>0,'recycle'=>0,'single_store'=>0,'pick_up_store'=>0,'platform_activities_id'=>0,'commission_type'=>0,'settlement_status'=>0,'operation_type'=>1,'order_failure_time'=>$order_failure_time);
                                $r4 = Db::name('order')->insert($sql4);
                                
                                $sql5 = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$auction_id,'p_name'=>$p_name,'p_price'=>$price,'num'=>1,'unit'=>$unit,'r_sNo'=>$sNo,'add_time'=>$time,'r_status'=>0,'content'=>'','reason'=>'','re_type'=>0,'re_apply_money'=>'','re_money'=>'','real_money'=>0,'re_photo'=>'','r_content'=>'','r_type'=>100,'express_id'=>'','courier_num'=>'','express_type'=>1,'freight'=>$z_freight,'size'=>$size,'sid'=>$attr_id,'invoice'=>0,'express'=>0,'exchange_num'=>0,'coupon_id'=>0,'manual_offer'=>0,'after_discount'=>$price,'mch_id'=>'','settlement_type'=>0,'recycle'=>0);
                                $r5 = Db::name('order_details')->insert($sql5);
                                if($r4 > 0 && $r5 > 0)
                                {
                                    $sql_update = array('status'=>2,'sNo'=>$sNo,'user_id'=>$user_id);
                                    $sql_where = array('session_id'=>$session_id,'recovery'=>0,'id'=>$auction_id);
                                    $r = Db::name('auction_product')->where($sql_where)->update($sql_update);
                                }
                                else
                                {
                                    Db::rollback();
                                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 场次进行中改为已结束失败！订单参数:'.json_encode($sql4).'; 订单详情参数:'.json_encode($sql5);
                                    $this->Log($Log_content);
                                    return;
                                }
                            }
                        }

                        // 修改场次为已结束
                        $sql3 = array('id'=>$session_id,'status'=>2,'recovery'=>0);
                        $r3 = Db::name('auction_session')->where($sql3)->update(['status' => 3]);
                        if($r3 == -1)
                        {
                            Db::rollback();
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 场次进行中改为已结束失败！参数:'.json_encode($sql3);
                            $this->Log($Log_content);
                            return;
                        }
                    }
                }

                // 根据商城ID、专场ID，查询已缴纳保证金的用户
                $promiseUser = AuctionPromiseModel::where(['store_id'=>$store_id,'special_id'=>$special_id,'is_pay'=>1,'recovery'=>0])->field('user_id')->select()->toArray();
                if($promiseUser)
                {
                    foreach ($promiseUser as $kpu => $vpu)
                    {
                        $user_id2 = $vpu['user_id'];
                        if(!in_array($user_id2,$winner_array))
                        { // 该用户，在该专场没有一件商品竞拍成功（退还保证金）
                            $refund_data[] = array('special_id'=>$special_id,'user_id'=>$user_id2,'mch_id'=>$mch_id);
                        }
                    }
                }
            
                // 修改专场为已结束
                $sql4 = array('id'=>$special_id,'recovery'=>0,'status'=>2);
                $r4 = Db::name('auction_special')->where($sql4)->update(['status' => 3]);
                if($r4 < 1)
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 专场进行中改为已结束失败！参数:'.json_encode($sql4);
                    $this->Log($Log_content);
                    return;
                }
            }
        }
        
        if($refund_data != array())
        { // 有需要退还保证金的数据
            foreach($refund_data as $k => $v)
            {
                $special_id_0 = $v['special_id']; // 专场ID
                $mch_id_0 = $v['mch_id']; // 店铺ID
                $user_id_0 = $v['user_id']; // user_id

                $mch_name = '';
                $cpc = '';
                $mobile = '';
                $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id_0])->field('account_money,name,cpc,tel')->select()->toArray();
                if($r_mch)
                {
                    $mch_name = $r_mch[0]['name'];//店铺名称
                    $cpc = $r_mch[0]['cpc']; // 区号
                    $mobile = $r_mch[0]['tel']; // 店铺联系电话
                }

                $r_0 = AuctionPromiseModel::where(['store_id'=>$store_id,'special_id'=>$special_id_0,'user_id'=>$user_id_0,'is_pay'=>1])->field('id,user_id,promise,trade_no')->select()->toArray();
                if($r_0)
                {
                    foreach($r_0 as $k_0 => $v_0)
                    {
                        $promise_id_0 = $v_0['id']; // 用户id
                        $user_id_0 = $v_0['user_id']; // 用户id
                        $promise_0 = $v_0['promise']; // 保证金
                        $trade_no = $v_0['trade_no']; // 支付单号

                        $r_4 = OrderDataModel::where(['trade_no'=>$trade_no])->field('pay_type')->select()->toArray();
                        $pay = $r_4[0]['pay_type'];

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
                                $res = $this->wallet($store_id,$user_id_0, $promise_0, $trade_no,$special_id_0);
                                break;
                            case 'aliPay' :
                            case 'alipay' :
                            case 'pc_alipay' :
                            case 'alipay_mobile' :
                            case 'alipay_minipay' :
                                // 支付宝小程序退款 //支付宝手机支付//支付宝扫码支付
                                $zfb_res = app('Alipay')::refund($trade_no, $promise_0, $appid, $store_id, $pay, $promise_id_0);
                                if ($zfb_res != 'success')
                                {   
                                    if($zfb_res == '商家余额不足！' && !empty($mobile))
                                    {
                                        $array_code = array('cpc'=>$cpc,'mobile'=>$mobile,'type'=>1,'type1'=>10,'bizparams'=>array("sNo" => $trade_no));
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
                                $wxtk_res = wxpay::wxrefundapi($trade_no, $trade_no . $promise_id_0, $promise_0, 0, $store_id, $pay);
                                if ($wxtk_res['result_code'] != 'SUCCESS')
                                {
                                    Db::rollback();
                                    if ($wxtk_res['err_code_des'] == '基本账户余额不足，请充值后重新发起')
                                    {
                                        $msg_title = Lang('auction.46');
                                        $msg_content = "账户余额不足，订单【".$trade_no."】自动退款失败。请尽快登陆平台完成处理！";

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

                                        $message = Lang('auction.47');
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
                                echo $pay . '支付方式不存在！';
                                exit;
                        }
                        
                        $sql_where3 = array('store_id'=>$store_id,'user_id'=>$user_id_0,'trade_no'=>$trade_no);
                        $sql_update3 = array('is_back'=>1,'back_time'=>$time);
                        $r3 = Db::name('auction_promise')->where($sql_where3)->update($sql_update3);
                        if ($r3 > 0)
                        {
                            $msg_title = Lang("auction.48");
                            $msg_content = Lang("auction.49");
                            $pusher = new LaikePushTools();
                            $pusher->pushMessage($user_id_0, $msg_title, $msg_content, $store_id, '');

                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改竞拍保证金表成功！';
                            $this->Log($Log_content);
                        }
                        else
                        {
                            Db::rollback();
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改竞拍保证金表失败！条件参数:'.json_encode($sql_where3).'；修改参数:'.json_encode($sql_update3);
                            $this->Log($Log_content);
                            return;
                        }
                    }
                }
            }
        }

        Db::commit();

        return;
    }

    // 删除订单过期
    public function order_failure($store_id)
    {
        $time0 = date("Y-m-d H:i:s"); // 当前时间

        $order_failure = 0;
        $r_config = AuctionConfigModel::where('store_id',$store_id)->select()->toArray();
        if($r_config)
        {
            $order_failure = $r_config[0]['order_failure'];
        }

        $time = date("Y-m-d H:i:s", strtotime("-$order_failure hour")); // 订单过期删除时间
        Db::startTrans();
        // 根据用户id，订单为未付款，查询订单
        $r0 = OrderModel::where(['store_id'=>$store_id,'status'=>0,'otype'=>'JP'])->where('add_time','<',$time0)->select()->toArray();
        if($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $user_id = $v['user_id']; // user_id
                $sNo = $v['sNo']; // 订单号
                $psNo = $v['p_sNo']; // 父订单号
                $otype = $v['otype'];//订单类型

                $session_id = Db::name('auction_product')->where('sNo', $sNo)->value('session_id'); // 根据竞拍商品订单查场次id

                $special_id = Db::name('auction_session')->where('id', $session_id)->value('special_id'); // 根据场次id查专场id

                // 根据专场ID，查询专场结束时间
                $end_date1 = Db::name('auction_special')->where(['store_id'=>$store_id,'id'=>$special_id])->value('end_date');
                $end_date1 = strtotime($end_date1) + $order_failure;
                $payEndDate = date("Y-m-d H:i:s",$end_date1); // 该专场订单最后支付时间

                if($payEndDate <= $time0)
                { // 当过了专场订单最后支付时间
                    // 流拍
                    $sql_where_jp = array('sNo'=>$sNo);
                    $r_jp = Db::name('auction_product')->where($sql_where_jp)->update(['status'=>'3','sNo'=>'']);
                    if ($r_jp < 0)
                    {
                        $this->Log(__METHOD__ . '->' . __LINE__ . '更新竞拍订单失败！参数：' . json_encode($sql_where_jp));
                    }

                    //扣除保证金 /专场结束，订单付款倒计时结束后，未付款的用户扣除保证金给店铺
                    $sql_update6 = array('is_deduction'=>1,'is_back'=>0,'back_time'=>$time0);
                    $sql_where6 = array('store_id'=>$store_id,'user_id'=>$user_id,'special_id'=>$special_id);
                    $r6 = Db::name('auction_promise')->where($sql_where6)->update($sql_update6);
                    $this->Log(__METHOD__ . '->' . __LINE__ . '订单更新保证金状态！参数：' . json_encode($sql_where6));
                    if($r6 > 0)
                    {
                        $promise_price = Db::name('auction_promise')->where($sql_where6)->value('promise');
                        $mch_id = Db::name('auction_special')->where('id', $special_id)->value('mch_id');//根据专场id查店铺id
                        //更新店铺数据
                        $sql8_where = array('store_id'=>$store_id,'id'=>$mch_id);
                        $sql8_update = array('cashable_money'=>Db::raw('cashable_money+'.$promise_price),'account_money'=>Db::raw('account_money+'.$promise_price));
                        $res8 = Db::name('mch')->where($sql8_where)->update($sql8_update);
                        if($res8 < 0)
                        {   
                            Db::rollback();
                            $this->Log(__METHOD__ . ":" . __LINE__ . "保证金到账失败！参数:" . json_encode($sql8_where));
                        } 

                        $r9 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('account_money,integral_money')->select()->toArray();

                        $account_money = $r9[0]['account_money'];
                        $integral_money = $r9[0]['integral_money'];
                        //添加记录
                        $data10 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'price'=>$promise_price,'integral'=>0,'integral_money'=>$integral_money,'account_money'=>$account_money,'type'=>4,'addtime'=>date("Y-m-d H:i:s"),'remake'=>$sNo,'status'=>1);
                        $r10 = Db::name('mch_account_log')->insert($data10);
                        if ($r10 <= 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '添加入驻商户账户收支记录失败！sql:'.json_encode($data10);
                            $this->Log($Log_content);
                            Db::rollback();
                        }
                    }
                    
                    $sql_where_o = array('store_id'=>$store_id,'status'=>0,'sNo'=>$sNo);
                    $r_o = Db::name('order')->where($sql_where_o)->update(['status'=>'7']);
                    $this->Log(__METHOD__ . '->' . __LINE__ . '订单更新为关闭状态！参数：' . json_encode($sql_where_o));

                    $sql_where2 = array('store_id'=>$store_id,'r_sNo'=>$sNo);
                    $r2 = Db::name('order_details')->where($sql_where2)->update(['r_status'=>'7']);
                    $this->Log(__METHOD__ . '->' . __LINE__ . '订单详情更新为关闭状态！参数：' . json_encode($sql_where2));
                }
            }
        }
        Db::commit();
        return;
    }

    // 生成订单号
    public function order_number($pay, $text = 'sNo')
    {
        $sNo = $pay . date("ymdhis") . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9);
        $res = OrderModel::where($text,$sNo)->select()->toArray();
        if ($res)
        {
            $this->order_number($pay, $text);
        }
        else
        {
            return $sNo;
        }
    }

    // 退还余额
    public function wallet($store_id,$user_id, $price,$trade_no,$special_id)
    {
        // 根据商城ID、买家用户ID，查询买家余额
        $r0 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('money')->select()->toArray();
        $money = $r0[0]['money'];

        // 修改买家用户余额
        $sql_where = array('store_id'=>$store_id,'user_id'=>$user_id);
        $sql_update = array('money'=>Db::raw('money+'.$price));
        $r1 = Db::name('user')->where($sql_where)->update($sql_update);
        if ($r1 > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改金额成功！';
            $this->Log($Log_content);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改金额失败！条件参数:'.json_encode($sql_where).'；修改参数:'.json_encode($sql_update);
            $this->Log($Log_content);
        }

        $array = array('store_id'=>$store_id,'money'=>$price,'user_money'=>$money,'type'=>12,'money_type'=>1,'money_type_name'=>5,'record_notes'=>'','type_name'=>'竞拍保证金退款','s_no'=>$trade_no,'title_name'=>'','activity_code'=>$special_id,'mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
        $details_id = PC_Tools::add_Balance_details($array);

        // 添加买家退款日志
        $event = '系统退还' . $price . '元竞拍保证金';
        $sql2 = array('store_id'=>$store_id,'user_id'=>$user_id,'money'=>$price,'oldmoney'=>$money,'event'=>$event,'type'=>27,'details_id'=>$details_id);
        $r2 = Db::name('record')->insert($sql2);
        if ($r2 > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 新增记录成功！';
            $this->Log($Log_content);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 新增记录失败！参数:'.json_encode($sql2);
            $this->Log($Log_content);
        }
        
        return 1;
    }

    //商家订单结算
    public function order_settlement($store_id,$sNo)
    {   
        if(!$sNo)
        {
            return;
        }
        $lktlog = new LaiKeLogUtils();
        //获取店铺
        $sql_m = "select o.mch_id from lkt_order_details as d left join lkt_order as o on o.sNo = d.r_sNo where o.otype = 'JP' and o.store_id = '$store_id' and o.recycle = 0 and o.sNo = '$sNo' ";
        $res_m = Db::query($sql_m);
        if($res_m)
        {   
            Db::startTrans();
            foreach ($res_m as $k => $v) 
            {
                $mch_id = substr($v['mch_id'], 1, -1); //订单结算金额的商家（商品的店铺）
        
                $sql_m = "select a.after_discount,a.freight,b.mch_id,a.id,a.r_sNo from lkt_order_details as a left join lkt_order as b on a.r_sNo = b.sNo where b.store_id = '$store_id' and a.r_status in (3,5) and a.settlement_type = 0 and b.otype = 'JP' and b.mch_id = ',".$mch_id.",' ";
                $res_m = Db::query($sql_m);
                if($res_m)
                {
                    foreach ($res_m as $key => $value) 
                    {   
                        if($value['after_discount'] > 0)
                        {
                            $sNo = $value['r_sNo'];
                            $after_discount = $value['after_discount'];
                            $freight = $value['freight'];
                            $money = round($after_discount+$freight,2);

                            //查询是否报名专场
                            $sql_s = "select c.mch_id,c.commission from lkt_auction_product a LEFT JOIN lkt_auction_session b on a.session_id=b.id LEFT JOIN lkt_auction_special c on b.special_id=c.id where c.store_id = '$store_id' and c.type = 3 and a.sNo = '$sNo' ";
                            $res_s = Db::query($sql_s);
                            $commission = 0;//结算佣金
                            $shop_id = $mch_id;
                            if ($res_s) 
                            {
                                $commission = round(($res_s[0]['commission']/100)*$money,2); //结算佣金
                                $money = $money - $commission;//店铺结算金额
                                $shop_id = $res_s[0]['mch_id'];//佣金结算店铺（发布专场的店铺）
                            }
                            if($money > 0)
                            {   
                                $sql_u_where = array('store_id'=>$store_id,'id'=>$mch_id);
                                $sql_u_update = array('account_money'=>Db::raw('account_money-'.$money),'cashable_money'=>Db::raw('cashable_money+'.$money));
                                $res_u = Db::name('mch')->where($sql_u_where)->update($sql_u_update);
                                if($res_u < 0)
                                {   
                                    Db::rollback();
                                    $lktlog->log("common/Auction.log","\r\n订单结算到账失败！参数:" . json_encode($sql_u_where) . "\r\n");
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
                                    $sql_where = array('store_id'=>$store_id,'id'=>$value['id']);
                                    $sql_update = array('settlement_type'=>1);
                                    $res = Db::name('order_details')->where($sql_where)->update($sql_update);
                                    if($res < 0)
                                    {   
                                        Db::rollback();
                                        $lktlog->log("common/Auction.log","\r\n订单结算失败！参数:" . json_encode($sql_where) . "\r\n");
                                    }
                                    else
                                    {
                                        if($commission > 0)
                                        {
                                            $sql_u_where1 = array('store_id'=>$store_id,'id'=>$shop_id);
                                            $sql_u_update1 = array('cashable_money'=>Db::raw('cashable_money+'.$commission));
                                            $res_u1 = Db::name('mch')->where($sql_u_where1)->update($sql_u_update1);
                                            if($res_u1 < 0)
                                            {   
                                                Db::rollback();
                                                $lktlog->log("common/Auction.log","\r\n订单结算佣金到账失败！参数:" . json_encode($sql_u_update1) . "\r\n");
                                            }
                                            else
                                            {
                                                $r9 = MchModel::where(['store_id'=>$store_id,'id'=>$shop_id])->field('account_money,integral_money')->select()->toArray();
                                                $account_money = $r9[0]['account_money'];
                                                $integral_money = $r9[0]['integral_money'];
                                                //添加店铺入账记录佣金
                                                $data10 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'price'=>$commission,'integral'=>0,'integral_money'=>$integral_money,'account_money'=>$account_money,'type'=>1,'addtime'=>date("Y-m-d H:i:s"),'remake'=>$sNo,'status'=>1);
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
            Db::commit();
        }
        return;
    }

    // 扣除库存
    public function deduction_inventory($store_id,$pid,$cid,$user_id,$mch_id,$num = 1)
    {
        $time = date("Y-m-d H:i:s");
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
        
        $content = $user_id . '付款【竞拍】订单所需' . $num;
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
        return;
    }

    // 获取插件状态（用户）
    public function Get_plugin_status($store_id)
    {
        $is_status = 0;
        $is_status = Db::name('auction_config')->where(['store_id'=>$store_id])->value('is_open');
      
        return $is_status;
    }

    // 获取插件状态（店铺）
    public function Get_plugin_status_mch($store_id)
    {
        $status = 0; // 店铺端插件状态 0.关闭 1.开启
        $sql0 = "select status from lkt_plugins where store_id = '$store_id' and plugin_code = 'auction' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $status = $r0[0]['status'];
        }
        
        return $status;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("plugin/auction_auction.log",$Log_content);
        return;
    }

    // 后台关闭订单（后台关闭竞拍订单只有未付款订单可以关闭）
    public function closeOrder($array)
    {
        $store_id = $array['store_id'];
        $sNo = $array['sNo'];
        $user_id = $array['user_id'];

        $return_status = true; // 退还保证金状态 false:不退 true:退还

        $session_id = Db::name('auction_product')->where('sNo', $sNo)->value('session_id'); // 根据竞拍商品订单查场次id

        $special_id = Db::name('auction_session')->where('id', $session_id)->value('special_id'); // 根据场次id查专场id

        $sql_special = "select mch_id from lkt_auction_special where id = '$special_id' ";
        $r_special = Db::query($sql_special);
        if($r_special)
        {
            $mch_id = $r_special[0]['mch_id'];
        }

        // 根据专场ID、订单所属人，查询该专场里的其它属于该用户的订单
        $sql0 = "select b.sNo from lkt_auction_session as a left join lkt_auction_product as b on a.id = b.session_id where a.special_id = '$special_id' and b.user_id = '$user_id' and b.sNo != '$sNo' ";
        $r0 = Db::query($sql0);
        if($r0)
        { // 存在
            foreach($r0 as $k0 => $v0)
            {
                $sNo0 = $v0['sNo']; // 订单号

                // 根据商城ID、用户、订单号、待付款，查询订单
                $sql1 = "select * from lkt_order where store_id = '$store_id' and user_id = '$user_id' and sNo = '$sNo0' and status = 0 ";
                $r1 = Db::query($sql1);
                if($r1)
                { // 该用户在专场，还存在待付款订单，保证金还不能退还
                    $return_status = false; // 退还保证金状态 false:不退 true:退还
                }
            }
        }

        if($return_status)
        {
            $array = array('store_id'=>$store_id,'user_id'=>$user_id,'mch_id'=>$mch_id,'special_id'=>$special_id);
            $this->Refund_of_deposit($array);
        }

        return;
    }

    // 退还保证金
    public function Refund_of_deposit($array)
    {
        $store_id = $array['store_id'];
        $user_id = $array['user_id'];
        $mch_id = $array['mch_id'];
        $special_id = $array['special_id'];

        $time = date("Y-m-d H:i:s"); // 当前时间
        $mch_name = '';
        $cpc = '';
        $mobile = '';
        $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('account_money,name,cpc,tel')->select()->toArray();
        if($r_mch)
        {
            $mch_name = $r_mch[0]['name']; // 店铺名称
            $cpc = $r_mch[0]['cpc']; // 区号
            $mobile = $r_mch[0]['tel']; // 店铺联系电话
        }

        $r_0 = AuctionPromiseModel::where(['store_id'=>$store_id,'special_id'=>$special_id,'user_id'=>$user_id,'is_pay'=>1])->field('id,user_id,promise,trade_no')->select()->toArray();
        if($r_0)
        {
            foreach($r_0 as $k_0 => $v_0)
            {
                $promise_id_0 = $v_0['id']; // 保证金ID
                $user_id_0 = $v_0['user_id']; // 用户ID
                $promise_0 = $v_0['promise']; // 保证金
                $trade_no = $v_0['trade_no']; // 支付单号

                $r_4 = OrderDataModel::where(['trade_no'=>$trade_no])->field('pay_type')->select()->toArray();
                $pay = $r_4[0]['pay_type'];

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
                        $res = $this->wallet($store_id,$user_id_0, $promise_0, $trade_no,$special_id);
                        break;
                    case 'aliPay' :
                    case 'alipay' :
                    case 'pc_alipay' :
                    case 'alipay_mobile' :
                    case 'alipay_minipay' :
                        // 支付宝小程序退款 //支付宝手机支付//支付宝扫码支付
                        $zfb_res = app('Alipay')::refund($trade_no, $promise_0, $appid, $store_id, $pay, $promise_id_0);
                        if ($zfb_res != 'success')
                        {
                            if($zfb_res == '商家余额不足！' && !empty($mobile))
                            {
                                $array_code = array('cpc'=>$cpc,'mobile'=>$mobile,'type'=>1,'type1'=>10,'bizparams'=>array("sNo" => $trade_no));
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
                        // 微信公众号 微信小程序支付 微信APP支付.
                        $wxtk_res = wxpay::wxrefundapi($trade_no, $trade_no . $promise_id_0, $promise_0, 0, $store_id, $pay);
                        if ($wxtk_res['result_code'] != 'SUCCESS')
                        {
                            Db::rollback();
                            if ($wxtk_res['err_code_des'] == '基本账户余额不足，请充值后重新发起')
                            {
                                $msg_title = Lang('auction.46');
                                $msg_content = "账户余额不足，订单【".$trade_no."】自动退款失败。请尽快登陆平台完成处理！";

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

                                $message = Lang('auction.47');
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
                        echo $pay . '支付方式不存在！';
                        exit;
                }
                
                $sql_where3 = array('store_id'=>$store_id,'user_id'=>$user_id_0,'trade_no'=>$trade_no);
                $sql_update3 = array('is_back'=>1,'back_time'=>$time0);
                $r3 = Db::name('auction_promise')->where($sql_where3)->update($sql_update3);
                if ($r3 > 0)
                {
                    $msg_title = Lang("auction.48");
                    $msg_content = Lang("auction.49");
                    $pusher = new LaikePushTools();
                    $pusher->pushMessage($user_id_0, $msg_title, $msg_content, $store_id, '');

                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改竞拍保证金表成功！';
                    $this->Log($Log_content);
                }
                else
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改竞拍保证金表失败！条件参数:'.json_encode($sql_where3).'；修改参数:'.json_encode($sql_update3);
                    $this->Log($Log_content);
                    return;
                }
            }
        }
    }

    // 获取专场列表(管理后台)
    public function GetSpecialSessionList($array)
    {
        $store_id = $array['store_id'];
        $shop_id = $array['shop_id']; // 平台自营店ID
        $id = $array['id'];  // ID
        $key = $array['key']; // ID/专场名称/店铺名称
        $status = $array['status']; // 状态类型
        $type = $array['type']; // 专场类型
        $startDate = $array['startDate']; // 开始时间
        $endDate = $array['endDate']; // 结束时间
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $plugin = Db::name('plugins')->where(['store_id'=>$store_id,'plugin_code'=>'auction'])->value('status');
        //初始化分页查询条件
        $condition = "where a.store_id = '$store_id' and b.store_id = '$store_id' and a.recovery = 0 ";

        //店铺未授权时只查自营店数据
        if ($plugin == 0)
        {
            $condition .= " and a.mch_id = '$shop_id' ";
        }

        if($id != '')
        {
            $condition .= " and a.id = '$id'";
        }

        if ($key != '')
        {
            $key_0 = Tools::FuzzyQueryConcatenation($key);
            $condition .= " and (a.id = '$key' or a.name like $key_0 or b.name like $key_0) ";
        }

        if ($status != '')
        { // 1=未开始 2=进行中 3=已结束
            $condition .= " and a.status = '$status'";
        }

        if ($type != '')
        { // 专场类型 1=店铺专场 2=普通专场 3=报名专场
            $condition .= " and a.type = '$type'";
        }

        if ($startDate != '')
        {
            $condition .= " and a.start_date >= '$startDate' ";
        }

        if ($endDate != '')
        {
            $condition .= " and a.end_date <= '$endDate' ";
        }

        $list = array();
        $total = 0;

        $sql0 = "select ifnull(count(a.id),0) as num from lkt_auction_special as a left join lkt_mch as b on a.mch_id = b.id " . $condition ;
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        $sql1 = "select a.id,a.name,a.type,a.unit,a.number,a.mark_up_amt,a.promise_amt,a.sign_end_date,a.start_date,a.end_date,a.img,a.commission,a.content,a.is_show as isShow,a.status,b.name as mchName from lkt_auction_special as a left join lkt_mch as b on a.mch_id = b.id " . $condition . " order by a.add_date desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $special_id = $v['id'];
                $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
                if($v['status'] == 1)
                {
                    $v['statusName'] = '未开始';
                }
                else if($v['status'] == 2)
                {
                    $v['statusName'] = '进行中';
                }
                else 
                {
                    $v['statusName'] = '已结束';
                }
                $v['specialType'] = $v['type'];
                $v['sessionNum'] = 0;
                $auction_num = 0;
                $r2 = AuctionSessionModel::where(['recovery'=>0,'special_id'=>$special_id])->field('id')->select()->toArray();
                if($r2)
                {
                    $v['sessionNum'] = count($r2);
                    foreach ($r2 as $k2 => $v2)
                    {
                        $session_id = $v2['id'];
                        $r3 = AuctionProductModel::where(['recovery'=>0,'session_id'=>$session_id])->field('id')->select()->toArray();
                        if($r3)
                        {
                            $auction_num = $auction_num + count($r3);
                        }
                    }
                }

                $v['auction_num'] = $auction_num;
                $list[] = $v;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 添加专场(管理后台)
    public function AddSpecialSession($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 平台自营店ID
        $id = $array['id'];  // ID
        $name = $array['name']; // 专场名称
        $type = $array['type']; // 专场类型 1=店铺专场 2=普通专场 3=报名专场
        $unit = $array['unit']; // 起拍价默认单位 1=固定金额 2=价格百分比
        $number = $array['number']; // 起拍价默认值
        $mark_up_amt = $array['mark_up_amt']; // 默认加价金额
        $commission = $array['commission']; // 佣金
        $promise_amt = $array['promise_amt']; // 保证金金额
        $sign_end_date = $array['sign_end_date']; // 报名截至时间
        $start_date = $array['start_date']; // 开始时间
        $end_date = $array['end_date']; // 结束时间
        $img = $array['img']; // 专场图片
        $content = $array['content']; // 专场预告
        
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源
        
        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        if($name == '')
        {
            $message = Lang("auction.13");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            if($id == '')
            {
                $r0 = AuctionSpecialModel::where(['store_id'=> $store_id,'recovery'=>0,'name'=>$name])->field('id')->select()->toArray();
            }
            else
            {
                $r0 = AuctionSpecialModel::where(['store_id'=> $store_id,'recovery'=>0,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            if($r0)
            {
                $message = Lang("auction.14");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }

        if (is_numeric($number))
        {
            if ($number <= 0)
            {
                $message = Lang("auction.15");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        } 
        else 
        {
            $message = Lang("auction.16");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if (is_numeric($mark_up_amt))
        {
            if ($mark_up_amt <= 0)
            {
                $message = Lang("auction.17");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        } 
        else 
        {
            $message = Lang("auction.18");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($type == 3)
        {
            if($commission == '')
            {
                $message = Lang("auction.19");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }

            if($sign_end_date == '')
            {
                $message = Lang("auction.22");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }

        if (is_numeric($promise_amt))
        {
            if ($promise_amt <= 0)
            {
                $message = Lang("auction.20");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        } 
        else 
        {
            $message = Lang("auction.21");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($start_date == '')
        {
            $message = Lang("auction.23");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($start_date <= $time)
        {
            $message = Lang("auction.40");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($end_date == '')
        {
            $message = Lang("auction.24");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($start_date >= $end_date)
        {
            $message = Lang("auction.25");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($img == '')
        {
            $message = Lang("auction.26");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        $img = preg_replace('/.*\//', '', $img);

        if($content == '')
        {
            $message = Lang("auction.27");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($id == '')
        {
            if($type == 2)
            {
                $sql = array('store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'type'=>$type,'unit'=>$unit,'number'=>$number,'mark_up_amt'=>$mark_up_amt,'promise_amt'=>$promise_amt,'start_date'=>$start_date,'end_date'=>$end_date,'img'=>$img,'content'=>$content,'add_date'=>$time);
            }
            else
            {
                $sql = array('store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'type'=>$type,'unit'=>$unit,'number'=>$number,'mark_up_amt'=>$mark_up_amt,'commission'=>$commission,'promise_amt'=>$promise_amt,'sign_end_date'=>$sign_end_date,'start_date'=>$start_date,'end_date'=>$end_date,'img'=>$img,'content'=>$content,'add_date'=>$time);
            }
            $r = Db::name('auction_special')->insertGetId($sql);
            if ($r > 0)
            {
                if($type == 3)
                {
                    $name0 = substr(uniqid(mt_rand(), true),0,19);
                    $sql0 = array('special_id'=>$r,'name'=>$name0,'start_date'=>$start_date,'end_date'=>$end_date,'status'=>1,'is_show'=>1,'recovery'=>0,'add_date'=>$time);
                    $r0 = Db::name('auction_session')->insert($sql0);
                }
                
                $Jurisdiction->admin_record($store_id, $operator, '添加了专场ID：'.$r,1,$operator_source,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加专场成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了专场名称：'.$name.'失败',1,$operator_source,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加专场失败！参数:'.json_encode($sql);
                $this->Log($Log_content);
                $message = Lang('auction.28');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }
        else
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            if($type == 2)
            {
                $sql_update = array('name'=>$name,'type'=>$type,'unit'=>$unit,'number'=>$number,'mark_up_amt'=>$mark_up_amt,'promise_amt'=>$promise_amt,'start_date'=>$start_date,'end_date'=>$end_date,'img'=>$img,'content'=>$content,'update_date'=>$time);
            }
            else
            {
                $sql_update = array('name'=>$name,'type'=>$type,'unit'=>$unit,'number'=>$number,'mark_up_amt'=>$mark_up_amt,'commission'=>$commission,'promise_amt'=>$promise_amt,'sign_end_date'=>$sign_end_date,'start_date'=>$start_date,'end_date'=>$end_date,'img'=>$img,'content'=>$content,'update_date'=>$time);
            }
            $r = Db::name('auction_special')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了专场ID：'.$id.' 的信息失败',2,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改专场失败！参数:'.json_encode($sql_where);
                $this->Log($Log_content);
                $message = Lang('auction.29');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了专场ID：'.$id.' 的信息',2,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改专场成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
    }

    // 专场是否显示(管理后台、PC店铺)
    public function SwitchSpecialSession($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id'];  // 专场ID
        
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源
        
        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        $r0 = AuctionSpecialModel::where(['id'=>$id])->field('is_show')->select()->toArray();
        if($r0)
        {
            $is_show = $r0[0]['is_show']; // 是否显示

            $sql_where = array('id'=>$id);
            if($is_show == 1)
            {
                $sql_update = array('is_show'=>0,'update_date'=>$time);
            }
            else
            {
                $sql_update = array('is_show'=>1,'update_date'=>$time);
            }
            $r = Db::name('auction_special')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '将专场ID：'.$id.' 进行了是否显示操作失败',2,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改专场是否显示失败！参数:'.json_encode($sql_where);
                $this->Log($Log_content);
                $message = Lang('auction.29');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '将专场ID：'.$id.' 进行了是否显示操作',2,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改专场是否显示成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200, 'message' => $message));
                exit;
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 参数错误！';
            $this->Log($Log_content);
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }
    }

    // 删除专场
    public function DelSpecialSession($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id'];  // 专场ID
        
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源
        
        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();
        
        Db::startTrans();
        $sql0 = "select * from lkt_auction_special where store_id = '$store_id' and id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $type = $r0[0]['type']; // 专场类型 1=店铺专场 2=普通专场 3=报名专场

            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('recovery'=>1,'update_date'=>$time);
            $r = Db::name('auction_special')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除专场失败！参数:'.json_encode($sql_where);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '删除了专场ID：'.$id.' 失败',3,$operator_source,$mch_id,$operator_id);
                $message = Lang('auction.30');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
            
            if($type == 3)
            { // 报名专场
                $r0 = AuctionSessionModel::where(['special_id'=> $id,'recovery'=>0])->field('id')->select()->toArray();
                if($r0)
                {
                    $session_id = $r0[0]['id'];

                    $sql1 = "select b.id from lkt_auction_product as b left join lkt_product_list as a on a.id = b.goods_id where b.session_id = '$session_id' and b.recovery = 0 and a.mch_id = '$mch_id' ";
                    $r1 = Db::query($sql1);
                    if($r1)
                    {
                        foreach($r1 as $k1 => $v1)
                        {
                            $acId = $v1['id'];

                            $sql_where2 = array('id'=>$acId);
                            $sql_update2 = array('recovery'=>1);
                            $r2 = Db::name('auction_product')->where($sql_where2)->update($sql_update2);
                            if ($r2 == -1)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 移除竞拍商品失败！条件参数:'.json_encode($sql_where2).'；修改参数:'.json_encode($sql_update2);
                                $this->Log($Log_content);
                                Db::rollback();
                                $Jurisdiction->admin_record($store_id, $operator, '取消了专场ID：'.$id.'，的报名失败',3,$operator_source,$mch_id,$operator_id);
                                $message = Lang('auction.39');
                                echo json_encode(array('code' => 109, 'message' => $message));
                                exit;
                            }
                            else
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 移除竞拍商品成功！';
                                $this->Log($Log_content);
                            }
                        }
                    }
                }

                Db::commit();
                $Jurisdiction->admin_record($store_id, $operator, '取消了专场ID：'.$id.'，的报名',3,$operator_source,$mch_id,$operator_id);
                $message = Lang('Success');
                echo json_encode(array('code' => 200, 'message' => $message));
                exit;
            }
            else
            {
                $sql_where1 = array('special_id'=>$id);
                $sql_update1 = array('recovery'=>1,'update_date'=>$time);
                $r1 = Db::name('auction_session')->where($sql_where1)->update($sql_update1);

                $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除专场成功！';
                $this->Log($Log_content);
                Db::commit();
                $Jurisdiction->admin_record($store_id, $operator, '删除了专场ID：'.$id,3,$operator_source,$mch_id,$operator_id);
                $message = Lang('auction.45');
                echo json_encode(array('code' => 200, 'message' => $message));
                exit;
            }
        }
        else
        {
            Db::rollback();
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }
    }

    // 获取专场信息(管理后台)
    public function listSpecial($array)
    {
        $store_id = $array['store_id'];

        $list = array();
        $r = AuctionSpecialModel::where(['store_id'=> $store_id,'recovery'=>0,'type'=>2])->field('id,name,unit,number,mark_up_amt,start_date,end_date')->select()->toArray();
        if($r)
        {
            $list = $r;
        }

        $data = array('list'=>$list);

        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message, 'data' => $data));
        exit;       
    }

    // 查看场次(管理后台)
    public function ViewSessions($array)
    {
        $store_id = $array['store_id'];
        $id = $array['id'];  // ID
        $specialId = $array['specialId'];  // 专场ID
        $key = $array['key']; // ID/专场名称/店铺名称
        $status = $array['status']; // 状态类型
        $startDate = $array['startDate']; // 开始时间
        $endDate = $array['endDate']; // 结束时间
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = "where a.store_id = '$store_id' and b.recovery = 0 ";
        
        if($id != '')
        {
            $condition .= " and b.id = '$id' ";
        }
        
        if($specialId)
        {
            $condition .= " and b.special_id = '$specialId' ";
        }
        if ($key != '')
        {
            $key_0 = Tools::FuzzyQueryConcatenation($key);
            $condition .= " and (b.id = '$key' or b.name like $key_0 ) ";
        }
        if ($status != '')
        { // 1=未开始 2=进行中 3=已结束
            $condition .= " and b.status = '$status'";
        }

        $goodsList = array();
        $list = array();
        $total = 0;

        $sql0 = "select ifnull(count(a.id),0) as num from lkt_auction_special as a left join lkt_auction_session as b on a.id = b.special_id " . $condition ;
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        $sql1 = "select a.id as specialId,a.name,a.img,a.promise_amt,a.start_date as specialStartDate,a.end_date as specialEndDate,a.unit,a.number,a.mark_up_amt,a.content,b.id,b.name as sessionName,b.start_date,b.end_date,b.status,b.is_show,b.add_date from lkt_auction_special as a left join lkt_auction_session as b on a.id = b.special_id " . $condition . " order by a.status asc , a.id desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $id = $v['id'];
                $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
                if($v['status'] == 1)
                {
                    $v['statusName'] = '未开始';
                }
                else if($v['status'] == 2)
                {
                    $v['statusName'] = '进行中';
                }
                else 
                {
                    $v['statusName'] = '已结束';
                }
              
                $v['goodsNum'] = 0;
                if($id != '')
                {
                    $r2 = AuctionProductModel::where(['session_id'=> $id,'recovery'=>0])->field('id,goods_id,attr_id,mark_up_amt,starting_amt')->select()->toArray();
                    if($r2)
                    {
                        $v['goodsNum'] = count($r2);
                        foreach ($r2 as $k2 => $v2)
                        {
                            $goods_id = $v2['goods_id'];
                            $attr_id = $v2['attr_id'];
                            
                            $sql3 = "select a.id,a.product_title as goodsName,a.product_class,c.img,c.price,c.attribute from lkt_product_list as a left join lkt_configure as c on a.id = c.pid where a.store_id = '$store_id' and c.id = '$attr_id' and c.pid = '$goods_id' ";
                            $r3 = Db::query($sql3);
                            $product_class = $r3[0]['product_class'];
                            $imgUrl = ServerPath::getimgpath($r3[0]['img'], $store_id);
                            
                            $specifications = '';
                            if($r3[0]['attribute'] != '')
                            {
                                $attribute = unserialize($r3[0]['attribute']);
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
                            $specifications = rtrim($specifications, ",");
                            
                            $className = '';
                            $class_list = explode('-',trim($r3[0]['product_class'],'-'));
                            $class_num = count($class_list) - 1;
                            $class_id = $class_list[$class_num];
    
                            $r4 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$class_id])->field('pname')->select()->toArray();
                            if($r4)
                            {
                                $className = $r4[0]['pname'];
                            }
                            
                            $goodsList[] = array('id'=>$v2['id'],'attrId'=>$attr_id,'goodsName'=>$r3[0]['goodsName'],'img'=>$r3[0]['img'],'imgUrl'=>$imgUrl,'markUpAmt'=>$v2['mark_up_amt'],'price'=>$r3[0]['price'],'startingAmt'=>$v2['starting_amt'],'attribute'=>$specifications,'className'=>$className);
                        }
                    }
                    
                    $v['goodsList'] = $goodsList;
                }

                $list[] = $v;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 添加场次(管理后台)
    public function AddSession($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 平台自营店ID
        $id = $array['id'];  // 场次ID
        $specialId = $array['specialId']; // 专场ID
        $title = $array['title']; // 场次名称
        $startDate = $array['startDate']; // 开始时间
        $endDate = $array['endDate']; // 结束时间
        $attrIds = $array['attrIds']; // 商品数据
        
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源
        
        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        if($title == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 场次名称不能为空！';
            $this->Log($Log_content);
            $message = Lang('auction.31');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            if($id != '')
            {
                $r_0 = AuctionSessionModel::where(['special_id'=> $specialId,'recovery'=>0,'name'=>$title])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            else
            {
                $r_0 = AuctionSessionModel::where(['special_id'=> $specialId,'recovery'=>0,'name'=>$title])->field('id')->select()->toArray();
            }
            if($r_0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 场次名称已存在！';
                $this->Log($Log_content);
                $message = Lang('auction.32');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }

        $r_1 = AuctionSpecialModel::where(['id'=> $specialId,'recovery'=>0])->field('status,start_date,end_date')->select()->toArray();
        if($r_1)
        {
            $status = $r_1[0]['status']; // 1=未开始 2=进行中 3=已结束
            $start_date = $r_1[0]['start_date']; // 专场开始时间
            $end_date = $r_1[0]['end_date']; // 专场结束时间
            if($status == 2)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 参数错误！';
                $this->Log($Log_content);
                $message = Lang('auction.50');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 参数错误！';
            $this->Log($Log_content);
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($startDate < $start_date || $endDate > $end_date)
        { // 场次开始时间 < 专场开始时间 || 场次结束时间 > 专场结束时间
            $Log_content = __METHOD__ . '->' . __LINE__ .' 时间有误！';
            $this->Log($Log_content);
            $message = Lang('auction.33');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        $list = json_decode($attrIds,true);
        if($list == array())
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 请选择商品！';
            $this->Log($Log_content);
            $message = Lang('auction.34');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        foreach($list as $k => $v)
        {
            if(count($v) != 3)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 请完善数据！';
                $this->Log($Log_content);
                $message = Lang('auction.35');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }
        $time = date("Y-m-d H:i:s");

        if($startDate < $time)
        { // 场次开始时间 < 当前时间
            $status = 2;
        }
        else
        {
            $status = 1;
        }

        if($endDate < $time)
        { // 场次结束时间 < 当前时间
            $status = 3;
        }

        if($id != '')
        {
            $sql_where0 = array('id'=>$id,'special_id'=>$specialId);
            $sql_update0 = array('name'=>$title,'start_date'=>$startDate,'end_date'=>$endDate,'status'=>$status);
            $r0 = Db::name('auction_session')->where($sql_where0)->update($sql_update0);
            if ($r0 == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了专场ID：'.$specialId.' 里面的场次ID：'.$id.' 信息失败',2,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改场次失败！条件参数:'.json_encode($sql_where0).'；修改参数:'.json_encode($sql_update0);
                $this->Log($Log_content);
                $message = Lang('auction.36');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            else
            {
                Db::table('lkt_auction_product')->where(['session_id'=>$id,'recovery'=>0])->delete();
                foreach($list as $k => $v)
                {
                    $cid = $v['attrId']; // 属性ID
                    $startingAmt = $v['startingAmt']; // 起拍价
                    $markUpAmt = $v['markUpAmt']; // 加价幅度

                    $r_2 = ConfigureModel::where(['id'=> $cid,'recycle'=>0])->field('pid,price')->select()->toArray();
                    if($r_2)
                    {
                        $pid = $r_2[0]['pid']; // 商品ID
                        $goods_price = $r_2[0]['price']; // 商品价格

                        $sql1 = array('session_id'=>$id,'goods_id'=>$pid,'attr_id'=>$cid,'starting_amt'=>$startingAmt,'mark_up_amt'=>$markUpAmt,'goods_price'=>$goods_price,'price'=>$startingAmt,'user_id'=>'','status'=>0,'sNo'=>'','is_show'=>1,'recovery'=>0,'add_date'=>$time);
                        $r1 = Db::name('auction_product')->insert($sql1);
                    }
                }

                $Jurisdiction->admin_record($store_id, $operator, '修改了专场ID：'.$specialId.' 里面的场次ID：'.$id.' 信息',2,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改场次成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
        else
        {
            $sql0 = array('special_id'=>$specialId,'name'=>$title,'start_date'=>$startDate,'end_date'=>$endDate,'status'=>$status,'is_show'=>1,'recovery'=>0,'add_date'=>$time);
            $r0 = Db::name('auction_session')->insertGetId($sql0);
            if($r0 > 0)
            {
                foreach($list as $k => $v)
                {
                    $cid = $v['attrId']; // 属性ID
                    $startingAmt = $v['startingAmt']; // 起拍价
                    $markUpAmt = $v['markUpAmt']; // 加价幅度

                    $r_2 = ConfigureModel::where(['id'=> $cid,'recycle'=>0])->field('pid,price')->select()->toArray();
                    if($r_2)
                    {
                        $pid = $r_2[0]['pid']; // 商品ID
                        $goods_price = $r_2[0]['price']; // 商品价格

                        $sql1 = array('session_id'=>$r0,'goods_id'=>$pid,'attr_id'=>$cid,'starting_amt'=>$startingAmt,'mark_up_amt'=>$markUpAmt,'goods_price'=>$goods_price,'price'=>$startingAmt,'user_id'=>'','status'=>0,'sNo'=>'','is_show'=>1,'recovery'=>0,'add_date'=>$time);
                        $r1 = Db::name('auction_product')->insert($sql1);
                    }
                }

                $Jurisdiction->admin_record($store_id, $operator, '将专场ID：'.$specialId.' 里面添加了场次，场次ID：'.$r0,1,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加场次成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '将专场ID：'.$specialId.' 里面添加了场次，场次ID：'.$r0.' 失败',1,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加场次失败！参数:'.json_encode($sql0);
                $this->Log($Log_content);
                $message = Lang('auction.37');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }
    }

    // 场次是否显示(管理后台)
    public function SwitchSession($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 平台自营店ID
        $id = $array['id'];  // 场次ID
        
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源
        
        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        $r0 = AuctionSessionModel::where(['id'=> $id])->field('is_show')->select()->toArray();
        if($r0)
        {
            $is_show = $r0[0]['is_show']; // 是否显示
            
            $sql_where = array('id'=>$id);
            if($is_show == 1)
            {
                $sql_update = array('is_show'=>0,'update_date'=>$time);
            }
            else
            {
                $sql_update = array('is_show'=>1,'update_date'=>$time);
            }
            $r = Db::name('auction_session')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '将场次ID：'.$id.' 进行了是否显示操作失败',2,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改场次是否显示失败！参数:'.json_encode($sql_where);
                $this->Log($Log_content);
                $message = Lang('auction.36');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '将场次ID：'.$id.' 进行了是否显示操作',2,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改场次是否显示成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 参数错误！';
            $this->Log($Log_content);
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
    }

    // 删除场次(管理后台)
    public function DelSession($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 平台自营店ID
        $id = $array['id'];  // 场次ID

        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        $r0 = AuctionSessionModel::where(['id'=> $id])->field('status,special_id')->select()->toArray();
        if($r0)
        {
            $specialId = $r0[0]['special_id']; // 专场ID
            if($r0[0]['status'] == 2)
            {
                $message = Lang('auction.38');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            $sql_where = array('id'=>$id);
            $sql_update = array('recovery'=>1,'update_date'=>$time);
            $r = Db::name('auction_session')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '删除了专场ID：'.$specialId.' 里面的场次ID：'.$id.' 失败',3,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除场次失败！参数:'.json_encode($sql_where);
                $this->Log($Log_content);
                $message = Lang('auction.36');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            else
            {
                $sql_where1 = array('session_id'=>$id);
                $sql_update1 = array('recovery'=>1);
                $r1 = Db::name('auction_product')->where($sql_where1)->update($sql_update1);
                
                $Jurisdiction->admin_record($store_id, $operator, '删除了专场ID：'.$specialId.' 里面的场次ID：'.$id,3,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除场次成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 参数错误！';
            $this->Log($Log_content);
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
    }
    
    // 查看拍品(管理后台)
    public function LookGoodsList($array)
    {
        $store_id = $array['store_id'];
        $specialId = $array['specialId']; // 专场ID
        $sessionId = $array['sessionId']; // 场次ID
        $goodsName = $array['goodsName']; // 拍品名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        if($specialId != '')
        {
            $sessionId = '';
            $r_0 = AuctionSessionModel::where(['special_id'=> $specialId,'recovery'=>0])->field('id')->select()->toArray();
            if($r_0)
            {
                $sessionId = $r_0[0]['id'];
            }
        }

        $condition = " a.recovery = 0 ";
        if ($sessionId != '')
        {
            $condition .= " and a.session_id = '$sessionId' ";
        }

        if ($goodsName != '')
        {
            $goodsName_0 = Tools::FuzzyQueryConcatenation($goodsName);
            $condition .= " and b.product_title like $goodsName_0 ";
        }
       
        $list = array();
        $total = 0;

        $sql0 = "select ifnull(count(a.id),0) as num from lkt_auction_product as a left join lkt_product_list as b on a.goods_id = b.id left join lkt_configure as c on a.attr_id = c.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        $sql1 = "select a.id,a.starting_amt as startingAmt,a.mark_up_amt as markUpAmt,b.product_title as goodsName,b.product_class,c.img,c.price as goodsPrice,c.attribute from lkt_auction_product as a left join lkt_product_list as b on a.goods_id = b.id left join lkt_configure as c on a.attr_id = c.id where $condition order by id asc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k1 => $v1)
            {
                $class_list = explode('-',trim($v1['product_class'],'-'));
                $class_num = count($class_list) - 1;
                $class_id = $class_list[$class_num];

                $v1['img'] = ServerPath::getimgpath($v1['img'], $store_id);
                $specifications = '';
                if($v1['attribute'] != '')
                {
                    $attribute = unserialize($v1['attribute']);
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
                $v1['attribute'] = rtrim($specifications, ",");
                $v1['className'] = '';
                $r2 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$class_id])->field('pname')->select()->toArray();
                if($r2)
                {
                    $v1['className'] = $r2[0]['pname'];
                }
                $list[] = $v1;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 缴纳记录(管理后台、PC店铺)
    public function GetPromiseList($array)
    {
        $store_id = $array['store_id'];
        $specialId = $array['specialId']; // 专场ID
        $key = $array['key']; // 用户ID/用户名称/联系电话
        $type = $array['type']; // 操作类型 0.待缴纳 1.已缴纳 2.已退还
        $start_date = $array['start_date']; // 开始时间
        $end_date = $array['end_date']; // 结束时间
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $specialName = '';
        $r_0 = AuctionSessionModel::where(['id'=> $specialId])->field('name')->select()->toArray();
        if($r_0)
        {
            $specialName = $r_0[0]['name'];
        }

        //初始化分页查询条件
        $condition = " a.store_id = '$store_id' and a.special_id = '$specialId' and a.recovery = 0 ";
        if ($key != '')
        {
            $key_0 = Tools::FuzzyQueryConcatenation($key);
            $condition .= " and (a.user_id = '$key' or b.user_name like $key_0 or b.mobile like $key_0 ) ";
        }

        if ($type == 1)
        { // 已缴纳
            $condition .= " and a.is_pay = 1 and a.is_back = '' ";
        }
        else if($type == 2)
        { // 已退还
            $condition .= " and a.is_pay = 1 and a.is_back = 1 ";
        }
        else if($type == 0)
        { // 待缴纳
            $condition .= " and a.is_pay = 0 ";
        }

        if ($start_date != '')
        {
            $condition .= " and a.add_time >= '$start_date' ";
        }

        if ($end_date != '')
        {
            $condition .= " and a.add_time <= '$end_date' ";
        }
        
        $list = array();
        $total = 0;

        $sql0 = "select a.id from lkt_auction_promise as a left join lkt_user as b on a.user_id = b.user_id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = count($r0);
        }

        $sql1 = "select a.id,a.user_id,a.promise,a.add_time,a.is_pay,a.is_back,a.is_deduction,b.user_name,b.mobile from lkt_auction_promise as a left join lkt_user as b on a.user_id = b.user_id where $condition order by a.id desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach($r1 as $k => $v)
            {
                $v['specialName'] = $specialName;
                if($v['is_pay'] == 0)
                {
                    $v['typeName'] = '待缴纳';
                }
                else
                {
                    if($v['is_back'] == 1)
                    {
                        $v['typeName'] = '已退还';
                    }
                    else if($v['is_deduction'] == 1)
                    {
                        $v['typeName'] = '已扣除';
                    }
                    else
                    {
                        $v['typeName'] = '已缴纳';
                    }
                }
                $list[] = $v;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 删除保证金(管理后台)
    public function DelPromise($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id']; // 保证金ID

        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();

        $sql0 = "select special_id,user_id from lkt_auction_promise where id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $special_id = $r0[0]['special_id'];
            $user_id = $r0[0]['user_id'];
        }

        $sql_where = array('id'=>$id);
        $sql_update = array('recovery'=>1);
        $r = Db::name('auction_promise')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了专场ID：'.$special_id.' 里面的用户ID：'.$user_id.' 的缴纳记录失败',3,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除保证金失败！参数:'.json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('删除保证金失败！');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了专场ID：'.$special_id.' 里面的用户ID：'.$user_id.' 的缴纳记录',3,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除保证金成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array('code' => 200,'message' => $message));
            exit;
        }
    }

    // 获取商品(管理后台)
    public function GetProductList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $classId = $array['classId']; // 分类ID
        $key = $array['key']; // ID/场次名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        
        //初始化分页查询条件
        $condition = " a.store_id = '$store_id' and a.recycle = 0 and c.recycle = 0 and a.mch_status = 2 and a.status = 2 and a.commodity_type = '0' and a.mch_id = '$mch_id' and a.is_presell = 0 ";
        
        if ($classId != 0 && $classId != '')
        {
            $Tools = new Tools($store_id, 1);
            $product_class1 = $Tools->str_option( $classId);
            $condition .= " and a.product_class like '%$product_class1%' ";
        }
        if ($key != '')
        {
            if(strpos($key," "))
            {
                $key1 = explode(' ',$key);

                $condition1 = ' and (';
                foreach ($key1 as $k => $v)
                {
                    if($v)
                    {
                        $v_0 = Tools::FuzzyQueryConcatenation($v);
                        $condition1 .= " a.product_title like $v_0 or c.id = '$v' or ";
                    }
                }

                $condition1 = substr($condition1,0,strlen($condition1)-3);
                $condition1 .= ' )';
                $condition .= $condition1;
            }
            else
            {
                $key_0 = Tools::FuzzyQueryConcatenation($key);
                $condition .= " and (a.product_title like $key_0 or c.id = '$key' ) ";
            }
        }
        
        $list = array();
        $total = 0;

        $sql0 = "select count(a.id) as total from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id left join lkt_configure as c on a.id = c.pid where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select a.id,a.product_title as goodsName,a.product_class,a.mch_id,c.id as attrId,c.img as imgUrl,c.price,c.attribute,c.num as stockNum,b.name as mchName from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id left join lkt_configure as c on a.id = c.pid where $condition order by id asc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach($r1 as $k => $v)
            {
                $class_list = explode('-',trim($v['product_class'],'-'));
                $class_num = count($class_list) - 1;
                $class_id = $class_list[$class_num];

                $v['imgUrl'] = ServerPath::getimgpath($v['imgUrl'], $store_id);
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
                $v['className'] = '';
                $r2 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$class_id])->field('pname')->select()->toArray();
                if($r2)
                {
                    $v['className'] = $r2[0]['pname'];
                }
                $list[] = $v;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 移除商品(管理后台)
    public function DelGoods($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $acId = $array['acId']; // 分类ID

        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();

        $sql0 = "select b.special_id from lkt_auction_product as a left join lkt_auction_session as b on a.session_id = b.id where a.id = '$acId' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $special_id = $r0[0]['special_id'];
        }

        $sql_where0 = array('id'=>$acId);
        $sql_update0 = array('recovery'=>1);
        $r0 = Db::name('auction_product')->where($sql_where0)->update($sql_update0);
        if ($r0 == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '将专场ID：'.$special_id.' 的商品ID：'.$acId.' 进行了移除失败',2,$operator_source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 移除竞拍商品失败！条件参数:'.json_encode($sql_where0).'；修改参数:'.json_encode($sql_update0);
            $this->Log($Log_content);
            $message = Lang('auction.39');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '将专场ID：'.$special_id.' 的商品ID：'.$acId.' 进行了移除',2,$operator_source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 移除竞拍商品成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array('code' => 200,'message' => $message));
            exit;
        }
    }

    // 出价详情(管理后台、PC店铺)
    public function BidList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $acId = $array['acId']; // 竞拍商品ID
        $key = $array['key']; // ID/用户ID/用户名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据
        
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        
        //初始化分页查询条件
        $condition = " a.store_id = '$store_id' and a.auction_id = '$acId' ";
        if ($key != '')
        {
            $key_0 = Tools::FuzzyQueryConcatenation($key);
            $condition .= " and (a.id = '$key' or a.user_id = '$key' or b.user_name like $key_0 ) ";
        }
        
        $list = array();
        $total = 0;

        $sql0 = "select count(a.id) as total from lkt_auction_record as a left join lkt_user as b on a.user_id = b.user_id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select a.id,a.user_id as userId,a.price,a.add_time,b.user_name as userName from lkt_auction_record as a left join lkt_user as b on a.user_id = b.user_id where $condition order by a.id desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach($r1 as $k => $v)
            {
                $v['isTop'] = false;
                if($start == 0 && $k == 0)
                {
                    $v['isTop'] = true;
                }
                $list[] = $v;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 报名专场列表(PC店铺、移动端店铺)
    public function signList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $key = $array['key']; // ID/专场名称
        $status = $array['status']; // 1=未开始 2=进行中 3=已结束
        $startDate = $array['startDate']; // 开始时间
        $endDate = $array['endDate']; // 结束时间
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据
        $operator_source = $array['operator_source']; // 操作人来源
        
        $time = date("Y-m-d H:i:s");
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        // //初始化分页查询条件
        $condition = " a.store_id = '$store_id' and a.recovery = 0 and a.type = 3 and a.is_show = 1 ";

        $list = array();
        $total = 0;
        if($operator_source == 2)
        {
            if ($key != '')
            {
                $key_0 = Tools::FuzzyQueryConcatenation($key);
                $condition .= " and (a.id = '$key' or a.name like $key_0) ";
            }
            if ($status != '')
            { // 1=未开始 2=进行中 3=已结束
                $condition .= " and a.status = '$status'";
            }
            if ($startDate != '')
            {
                $condition .= " and a.start_date >= '$startDate' ";
            }
            if ($endDate != '')
            {
                $condition .= " and a.end_date <= '$endDate' ";
            }

            $sql0 = "select count(a.id) as total from lkt_auction_special a WHERE $condition
                and EXISTS(select distinct a1.id from lkt_auction_special a1 LEFT JOIN lkt_auction_session b1 ON a1.id=b1.special_id LEFT JOIN lkt_auction_product c1 ON c1.session_id = b1.id LEFT JOIN lkt_product_list d1 ON d1.id=c1.goods_id 
                WHERE a1.recovery=a.recovery and c1.recovery=a.recovery and a1.store_id = a.store_id and a1.type = a.type and d1.mch_id = '$mch_id' and a1.id=a.id) ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $str = "a.id,a.name as specialName,a.start_date as startDate,a.end_date as endDate,a.sign_end_date as signEndDate,a.status";
            $sql1 = "select $str from lkt_auction_special a WHERE $condition
                and EXISTS(select distinct a1.id from lkt_auction_special a1 LEFT JOIN lkt_auction_session b1 ON a1.id=b1.special_id LEFT JOIN lkt_auction_product c1 ON c1.session_id = b1.id LEFT JOIN lkt_product_list d1 ON d1.id=c1.goods_id 
                WHERE a1.recovery=a.recovery and c1.recovery=a.recovery and a1.store_id = a.store_id and a1.type = a.type and d1.mch_id = '$mch_id' and a1.id=a.id) order by a.add_date desc limit $start,$pagesize ";
            $r1 = Db::query($sql1);
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    $special_id = $v['id'];
                    if($v['status'] == 1)
                    {
                        $v['statusName'] = '未开始';
                    }
                    else if($v['status'] == 2)
                    {
                        $v['statusName'] = '进行中';
                    }
                    else 
                    {
                        $v['statusName'] = '已结束';
                    }
                    $v['auctionNum'] = 0;
                    $sql2 = "select count(p.id) as num from lkt_auction_session as b left join lkt_auction_product as p on b.id = p.session_id where b.special_id = '$special_id' and b.recovery = 0 and p.recovery = 0 ";
                    $r2 = Db::query($sql2);
                    if($r2)
                    {
                        $v['auctionNum'] = $r2[0]['num'];
                    }
                    $list[] = $v;
                }
            }
        }
        else
        {
            $str = "a.id as specialId,a.name,a.img,a.commission,a.sign_end_date,a.status,a.unit,a.number as startingAmt,a.mark_up_amt as markUpAmt,a.add_date";
            $condition = " a.store_id = '$store_id' and a.recovery = 0 and a.type = 3 and a.is_show = 1 ";
            if($status == 1)
            { // 未报名
                $condition .= " and a.status = 1 and a.sign_end_date > '$time' ";
                $sql0 = "select count(a.id) as total from lkt_auction_special a WHERE $condition   
                and not EXISTS(select distinct a1.id from lkt_auction_special a1 LEFT JOIN lkt_auction_session b1 ON a1.id=b1.special_id LEFT JOIN lkt_auction_product c1 ON c1.session_id = b1.id 
                LEFT JOIN lkt_product_list d1 ON d1.id=c1.goods_id WHERE a1.recovery=a.recovery 
                and c1.recovery=a.recovery and a1.store_id = a.store_id and a1.type = a.type and d1.mch_id = '$mch_id' and a1.id=a.id) ";

                $sql1 = "select $str from lkt_auction_special a WHERE $condition  
                and not EXISTS(select distinct a1.id from lkt_auction_special a1 LEFT JOIN lkt_auction_session b1 ON a1.id=b1.special_id LEFT JOIN lkt_auction_product c1 ON c1.session_id = b1.id 
                LEFT JOIN lkt_product_list d1 ON d1.id=c1.goods_id WHERE a1.recovery=a.recovery 
                and c1.recovery=a.recovery and a1.store_id = a.store_id and a1.type = a.type and d1.mch_id = '$mch_id' and a1.id=a.id) order by a.add_date desc limit $start,$pagesize ";
            }
            else if($status == 2)
            { // 已报名
                $sql0 = "select count(a.id) as total from lkt_auction_special a WHERE $condition
                and EXISTS(select distinct a1.id from lkt_auction_special a1 LEFT JOIN lkt_auction_session b1 ON a1.id=b1.special_id LEFT JOIN lkt_auction_product c1 ON c1.session_id = b1.id LEFT JOIN lkt_product_list d1 ON d1.id=c1.goods_id 
                WHERE a1.recovery=a.recovery and c1.recovery=a.recovery and a1.store_id = a.store_id and a1.type = a.type and d1.mch_id = '$mch_id' and a1.id=a.id) ";

                $sql1 = "select $str from lkt_auction_special a WHERE $condition
                and EXISTS(select distinct a1.id from lkt_auction_special a1 LEFT JOIN lkt_auction_session b1 ON a1.id=b1.special_id LEFT JOIN lkt_auction_product c1 ON c1.session_id = b1.id LEFT JOIN lkt_product_list d1 ON d1.id=c1.goods_id 
                WHERE a1.recovery=a.recovery and c1.recovery=a.recovery and a1.store_id = a.store_id and a1.type = a.type and d1.mch_id = '$mch_id' and a1.id=a.id) order by a.add_date desc limit $start,$pagesize ";
            }
            else
            { // 已结束
                $condition .= " and a.sign_end_date < '$time' ";
                $sql0 = "select count(a.id) as total from lkt_auction_special a WHERE $condition ";

                $sql1 = "select $str from lkt_auction_special a WHERE $condition order by a.add_date desc limit $start,$pagesize ";
            }
            $r0 = Db::query($sql0);
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $r1 = Db::query($sql1);
            if ($r1)
            {
                foreach($r1 as $k => $v)
                {
                    $special_id = $v['specialId'];
                    $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
                    $goodsNum = 0;

                    $r2 = AuctionSessionModel::where(['special_id'=> $special_id,'recovery'=>0])->field('id')->select()->toArray();
                    if($r2)
                    {
                        foreach($r2 as $k2 => $v2)
                        {
                            $session_id = $v2['id'];

                            $r3 = AuctionProductModel::where(['session_id'=> $session_id,'recovery'=>0])->field('count(id) as total')->select()->toArray();
                            if($r3)
                            {
                                $goodsNum += $r3[0]['total'];
                            }
                        }
                    }
                    $v['goodsNum'] = $goodsNum;

                    $list[] = $v;
                }
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 查看拍品(PC店铺、移动端店铺)
    public function specialGoodsList($array)
    {
        $store_id = $array['store_id'];
        $specialId = $array['specialId']; // 专场ID
        $name = $array['name']; // 拍品名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据
        $operator_source = $array['operator_source']; // 操作人来源

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $total = 0;
        $list = array();

        $condition = " b.recovery = 0 ";
        if($name != '')
        {
            $name_0 = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and a.product_title like $name_0 ";
        }

        $r0 = AuctionSessionModel::where(['special_id'=> $specialId,'recovery'=>0])->field('id,status')->select()->toArray();
        if($r0)
        {
            foreach($r0 as $k => $v)
            {
                $session_id = $v['id'];
                $sessionStatus = $v['status'];

                $condition .= " and b.session_id = '$session_id' ";

                $sql_total = "select b.id,b.attr_id as attrId,b.goods_price as goodsPrice,b.price,b.starting_amt as startingAmt,b.mark_up_amt as markUpAmt,a.product_title as goodsName,c.img,c.attribute from  lkt_auction_product as b left join lkt_product_list as a on a.id = b.goods_id left join lkt_configure as c on b.attr_id = c.id where $condition ";
                $r_total = Db::query($sql_total);
                if($r_total)
                {
                    $total = count($r_total);
                }

                $sql1 = "select b.id,b.attr_id as attrId,b.goods_price as goodsPrice,b.price,b.starting_amt as startingAmt,b.mark_up_amt as markUpAmt,a.product_title as goodsName,c.img,c.attribute from  lkt_auction_product as b left join lkt_product_list as a on a.id = b.goods_id left join lkt_configure as c on b.attr_id = c.id where $condition order by b.add_date desc limit $start,$pagesize ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    foreach($r1 as $k1 => $v1)
                    {
                        $v1['img'] = ServerPath::getimgpath($v1['img'], $store_id);
                        $v1['price'] = round($v1['price'],2);
                        $v1['startingAmtOld'] = $v1['startingAmt'];
                        $v1['sessionStatus'] = $sessionStatus;
                        
                        $specifications = '';
                        if($v1['attribute'] != '')
                        {
                            $attribute = unserialize($v1['attribute']);
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
                        $v1['attribute'] = rtrim($specifications, ",");

                        $list[] = $v1;
                    }
                }
            }
        }

        if($operator_source == 2)
        {
            $data = array('list'=>$list,'total'=>$total);
        }
        else
        {
            $data = array('goods'=>$list,'total'=>$total);
        }
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 报名(移动端店铺)
    public function sign($array)
    {
        $store_id = $array['store_id'];
        $specialId = $array['specialId']; // 专场ID
        $attrIds = $array['attrIds']; // 商品数据
        
        $r0 = AuctionSessionModel::where(['special_id'=> $specialId,'recovery'=>0])->field('id')->select()->toArray();
        if($r0)
        {
            $session_id = $r0[0]['id'];
        }
        else 
        {
            $message = Lang("Parameter error");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        $time = date("Y-m-d H:i:s");
        Db::startTrans();

        if($attrIds == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 请选择商品！';
            $this->Log($Log_content);
            $message = Lang('auction.34');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        $list = json_decode($attrIds,true);
        foreach($list as $k => $v)
        {
            if(!isset($v['attrId']) || !isset($v['startingAmt']) || !isset($v['markUpAmt']))
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 请完善数据！';
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('auction.35');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            if($v['startingAmt'] == '0')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 请完善数据！';
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('auction.35');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            if($v['markUpAmt'] == '0')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 请完善数据！';
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('auction.35');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }

            $cid = $v['attrId']; // 属性ID
            $startingAmt = $v['startingAmt']; // 起拍价
            $markUpAmt = $v['markUpAmt']; // 加价幅度

            $r_2 = ConfigureModel::where(['id'=> $cid,'recycle'=>0])->field('pid,price')->select()->toArray();
            if($r_2)
            {
                $pid = $r_2[0]['pid']; // 商品ID
                $goods_price = $r_2[0]['price']; // 商品价格

                $sql1 = array('session_id'=>$session_id,'goods_id'=>$pid,'attr_id'=>$cid,'starting_amt'=>$startingAmt,'mark_up_amt'=>$markUpAmt,'goods_price'=>$goods_price,'price'=>$startingAmt,'user_id'=>'','status'=>0,'sNo'=>'','is_show'=>1,'recovery'=>0,'add_date'=>$time);
                $r1 = Db::name('auction_product')->insert($sql1);
                if($r1 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 报名参加竞拍专场，添加商品失败！参数:'.json_encode($sql1);
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('auction.37');
                    echo json_encode(array('code' => 109,'message' => $message));
                    exit;
                }
            }
        }

        $Log_content = __METHOD__ . '->' . __LINE__ . ' 报名参加竞拍专场！专场ID:'.$specialId;
        $this->Log($Log_content);
        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 我的拍品(PC店铺、移动端店铺)
    public function MyItem($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $specialId = $array['specialId']; // 专场ID
        $name = $array['name']; // 拍品名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据
        $operator_source = $array['operator_source']; // 操作人来源
        
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $list = array();
        $total = 0;

        $r0 = AuctionSessionModel::where(['special_id'=>$specialId,'recovery'=>0])->field('id,status')->select()->toArray();
        if($r0)
        {
            $session_id = $r0[0]['id'];
            $sessionStatus = $r0[0]['status'];

            $condition = " b.session_id = '$session_id' and b.recovery = 0 and a.mch_id = '$mch_id' ";
            if($name != '')
            {
                $name_0 = Tools::FuzzyQueryConcatenation($name);
                $condition .= " and a.product_title like $name_0 ";
            }
            
            $sql1 = "select count(b.id) as num from lkt_auction_product as b left join lkt_product_list as a on a.id = b.goods_id left join lkt_configure as c on b.attr_id = c.id where $condition ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $total = $r1[0]['num'];
            }

            $sql2 = "select b.id,b.attr_id as attrId,b.goods_price as goodsPrice,b.starting_amt as startingAmt,b.mark_up_amt as markUpAmt,b.is_show as isShow,b.price,a.product_title as goodsName,c.img,c.attribute from lkt_auction_product as b left join lkt_product_list as a on a.id = b.goods_id left join lkt_configure as c on b.attr_id = c.id where $condition order by b.add_date desc limit $start,$pagesize ";
            $r2 = Db::query($sql2);
            if($r2)
            {
                foreach($r2 as $k2 => $v2)
                {
                    $v2['img'] = ServerPath::getimgpath($v2['img'], $store_id);
                    $specifications = '';
                    if($v2['attribute'] != '')
                    {
                        $attribute = unserialize($v2['attribute']);
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
                    $v2['attribute'] = rtrim($specifications, ",");

                    $v2['currentPrice'] = round($v2['price'],2);
                    $v2['price'] = round($v2['price'],2);
                    $v2['startingAmtOld'] = $v2['startingAmt'];
                    $v2['sessionStatus'] = $sessionStatus;
                    $list[] = $v2;
                }
            }
        }

        if($operator_source == 2)
        {
            $data = array('list'=>$list,'total'=>$total);
        }
        else
        {
            $data = array('goods'=>$list,'total'=>$total);
        }
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data'=>$data));
        exit;
    }

    // 移除竞拍商品(PC店铺、移动端店铺)
    public function removeAcGoods($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $acId = $array['acId']; // 竞拍商品ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();

        $sql1 = "select b.special_id from lkt_auction_product as a left join lkt_auction_session as b on a.session_id = b.id where a.id = '$acId' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $session_id = $r1[0]['special_id'];
        }
        else
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        $sql_where0 = array('id'=>$acId);
        $sql_update0 = array('recovery'=>1);
        $r0 = Db::name('auction_product')->where($sql_where0)->update($sql_update0);
        if ($r0 == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 移除竞拍商品失败！条件参数:'.json_encode($sql_where0).'；修改参数:'.json_encode($sql_update0);
            $this->Log($Log_content);
            $Jurisdiction->admin_record($store_id, $operator, '将专场ID：'.$session_id.'，的商品ID：'.$acId.',进行了移除失败',3,$operator_source,$mch_id,$operator_id);
            $message = Lang('auction.39');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 移除竞拍商品成功！';
            $this->Log($Log_content);
            $Jurisdiction->admin_record($store_id, $operator, '将专场ID：'.$session_id.'，的商品ID：'.$acId.',进行了移除',3,$operator_source,$mch_id,$operator_id);
            $message = Lang('Success');
            echo json_encode(array('code' => 200,'message' => $message));
            exit;
        }
    }

    // 店铺专场列表(PC店铺、移动端店铺)
    public function shopList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $key = $array['key']; // ID/专场名称
        $status = $array['status']; // 1=未开始 2=进行中 3=已结束
        $startDate = $array['startDate']; // 开始时间
        $endDate = $array['endDate']; // 结束时间
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据
       
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        // //初始化分页查询条件
        $condition = " a.store_id = '$store_id' and a.recovery = 0 and a.type = 1 and a.mch_id = '$mch_id' ";
        if ($key != '')
        {
            $key_0 = Tools::FuzzyQueryConcatenation($key);
            $condition .= " and (a.id = '$key' or a.name like $key_0) ";
        }
        if ($status != '')
        { // 1=未开始 2=进行中 3=已结束
            $condition .= " and a.status = '$status'";
        }
        if ($startDate != '')
        {
            $condition .= " and a.start_date >= '$startDate' ";
        }
        if ($endDate != '')
        {
            $condition .= " and a.end_date <= '$endDate' ";
        }
        $list = array();
        $total = 0;

        $sql0 = "select ifnull(count(a.id),0) as num from lkt_auction_special as a where " . $condition ;
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        $str = "a.id,a.name,a.img,a.commission,a.promise_amt as promiseAmt,a.start_date,a.end_date,a.sign_end_date as signEndDate,a.is_show as isShow,a.status,a.unit,a.number,a.mark_up_amt as markUpAmt,a.type as specialType";
        $sql1 = "select $str from lkt_auction_special as a where $condition order by a.add_date desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $special_id = $v['id'];
                $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
                $v['startDate'] = $v['start_date'];
                $v['endDate'] = $v['end_date'];

                if($v['status'] == 1)
                {
                    $v['statusName'] = '未开始';
                }
                else if($v['status'] == 2)
                {
                    $v['statusName'] = '进行中';
                }
                else 
                {
                    $v['statusName'] = '已结束';
                }
                $v['auctionNum'] = 0;
                $v['auction_num'] = 0;
                $sql2 = "select count(p.id) as num from lkt_auction_session as b left join lkt_auction_product as p on b.id = p.session_id where b.special_id = '$special_id' and b.recovery = 0 and p.recovery = 0 ";
                $r2 = Db::query($sql2);
                if($r2)
                {
                    $v['auctionNum'] = $r2[0]['num'];
                    $v['auction_num'] = $r2[0]['num'];
                }
                $v['isRemind'] = false;
                $list[] = $v;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 添加店铺专场(PC店铺、移动端店铺)
    public function addMySpecialList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id']; // 专场ID
        $name = $array['name']; // 专场名称
        $unit = $array['unit']; // 起拍价默认单位 1=固定金额 2=价格百分比
        $number = $array['number']; // 起拍价默认值
        $mark_up_amt = $array['mark_up_amt']; // 默认加价金额
        $promise_amt = $array['promise_amt']; // 保证金金额
        $start_date = $array['start_date']; // 开始时间
        $end_date = $array['end_date']; // 结束时间
        $img = $array['img']; // 专场图片
        $attrIds = $array['attrIds']; // 商品数据
        $content = $array['content']; // 专场预告
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源
        $time = date("Y-m-d H:i:s");

        $Jurisdiction = new Jurisdiction();
        Db::startTrans();

        if($name == '')
        {
            $message = Lang("auction.13");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            if($id == '')
            {
                $r0 = AuctionSpecialModel::where(['store_id'=> $store_id,'recovery'=>0,'name'=>$name])->field('id')->select()->toArray();
            }
            else
            {
                $r0 = AuctionSpecialModel::where(['store_id'=> $store_id,'recovery'=>0,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            if($r0)
            {
                $message = Lang("auction.14");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }

        if (is_numeric($number))
        {
            if ($number <= 0)
            {
                $message = Lang("auction.15");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        } 
        else 
        {
            $message = Lang("auction.16");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if (is_numeric($mark_up_amt))
        {
            if ($mark_up_amt <= 0)
            {
                $message = Lang("auction.17");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        } 
        else 
        {
            $message = Lang("auction.18");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if (is_numeric($promise_amt))
        {
            if ($promise_amt <= 0)
            {
                $message = Lang("auction.20");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        } 
        else 
        {
            $message = Lang("auction.21");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($start_date == '')
        {
            $message = Lang("auction.23");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($start_date <= $time)
        {
            $message = Lang("auction.40");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($end_date == '')
        {
            $message = Lang("auction.24");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($start_date >= $end_date)
        {
            $message = Lang("auction.25");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($img == '')
        {
            $message = Lang("auction.26");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        $img = preg_replace('/.*\//', '', $img);

        if($attrIds == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 请选择商品！';
            $this->Log($Log_content);
            $message = Lang('auction.34');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        $list = json_decode($attrIds,true);
        foreach($list as $k => $v)
        {
            if($operator_source == 2)
            {
                if(count($v) != 3)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ .' 请完善数据！';
                    $this->Log($Log_content);
                    $message = Lang('auction.35');
                    echo json_encode(array('code' => 109,'message' => $message));
                    exit;
                }
            }
            else
            {
                if(!isset($v['attrId']) || !isset($v['startingAmt']) || !isset($v['markUpAmt']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ .' 请完善数据！';
                    $this->Log($Log_content);
                    $message = Lang('auction.35');
                    echo json_encode(array('code' => 109,'message' => $message));
                    exit;
                }
                if($v['startingAmt'] == '0')
                {
                    $list[$k]['startingAmt'] = $number;
                }
                if($v['markUpAmt'] == '0')
                {
                    $list[$k]['markUpAmt'] = $mark_up_amt;
                }
            }
        }
        
        if($content == '')
        {
            $message = Lang("auction.27");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($id == '')
        {
            $sql = array('store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'type'=>1,'unit'=>$unit,'number'=>$number,'mark_up_amt'=>$mark_up_amt,'commission'=>'0.00','promise_amt'=>$promise_amt,'start_date'=>$start_date,'end_date'=>$end_date,'img'=>$img,'content'=>$content,'add_date'=>$time);
            $r = Db::name('auction_special')->insertGetId($sql);
            if ($r > 0)
            {
                $name0 = substr(uniqid(mt_rand(), true),0,19);
                $sql0 = array('special_id'=>$r,'name'=>$name0,'start_date'=>$start_date,'end_date'=>$end_date,'status'=>1,'is_show'=>1,'recovery'=>0,'add_date'=>$time);
                $r0 = Db::name('auction_session')->insertGetId($sql0);
                if($r0 > 0)
                {
                    foreach($list as $k => $v)
                    {
                        $cid = $v['attrId']; // 属性ID
                        $startingAmt = $v['startingAmt']; // 起拍价
                        $markUpAmt = $v['markUpAmt']; // 加价幅度

                        $r_2 = ConfigureModel::where(['id'=> $cid,'recycle'=>0])->field('pid,price')->select()->toArray();
                        if($r_2)
                        {
                            $pid = $r_2[0]['pid']; // 商品ID
                            $goods_price = $r_2[0]['price']; // 商品价格

                            $sql1 = array('session_id'=>$r0,'goods_id'=>$pid,'attr_id'=>$cid,'starting_amt'=>$startingAmt,'mark_up_amt'=>$markUpAmt,'goods_price'=>$goods_price,'price'=>$startingAmt,'user_id'=>'','status'=>0,'sNo'=>'','is_show'=>1,'recovery'=>0,'add_date'=>$time);
                            $r1 = Db::name('auction_product')->insert($sql1);
                            if($r1 <= 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加场次商品失败！参数:'.json_encode($sql1);
                                $this->Log($Log_content);
                                Db::rollback();
                                $message = Lang('auction.37');
                                echo json_encode(array('code' => 109,'message' => $message));
                                exit;
                            }
                        }
                    }

                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加专场成功！';
                    $this->Log($Log_content);
                    Db::commit();
                    $Jurisdiction->admin_record($store_id, $operator, '添加了专场ID：'.$r,1,$operator_source,$mch_id,$operator_id);
                    $message = Lang('auction.43');
                    echo json_encode(array('code' => 200,'message' => $message));
                    exit;
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加场次失败！参数:'.json_encode($sql0);
                    $this->Log($Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '添加了专场失败',1,$operator_source,$mch_id,$operator_id);
                    $message = Lang('auction.37');
                    echo json_encode(array('code' => 109,'message' => $message));
                    exit;
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加专场失败！参数:'.json_encode($sql);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '添加了专场失败',1,$operator_source,$mch_id,$operator_id);
                $message = Lang('auction.28');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }
        else
        {
            $sql_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
            $sql_update = array('name'=>$name,'type'=>1,'unit'=>$unit,'number'=>$number,'mark_up_amt'=>$mark_up_amt,'promise_amt'=>$promise_amt,'start_date'=>$start_date,'end_date'=>$end_date,'img'=>$img,'content'=>$content,'add_date'=>$time);
            $r = Db::name('auction_special')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改专场失败！条件参数:'.json_encode($sql_where).'；条件参数:'.json_encode($sql_update);
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('auction.29');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            else
            {
                $r0_0 = AuctionSessionModel::where(['special_id'=>$id])->field('id')->select()->toArray();
                $session_id = $r0_0[0]['id'];

                $sql_where0 = array('special_id'=>$id);
                $sql_update0 = array('start_date'=>$start_date,'end_date'=>$end_date);
                $r0 = Db::name('auction_session')->where($sql_where0)->update($sql_update0);
                if ($r0 == -1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改场次失败！条件参数:'.json_encode($sql_where0).'；修改参数:'.json_encode($sql_update0);
                    $this->Log($Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '修改了专场ID：'.$id.'，的信息失败',2,$operator_source,$mch_id,$operator_id);
                    $message = Lang('auction.36');
                    echo json_encode(array('code' => 109,'message' => $message));
                    exit;
                }
                else
                {
                    Db::table('lkt_auction_product')->where(['session_id'=>$session_id,'recovery'=>0])->delete();
                    foreach($list as $k => $v)
                    {
                        $cid = $v['attrId']; // 属性ID
                        $startingAmt = $v['startingAmt']; // 起拍价
                        $markUpAmt = $v['markUpAmt']; // 加价幅度

                        $r_2 = ConfigureModel::where(['id'=> $cid,'recycle'=>0])->field('pid,price')->select()->toArray();
                        if($r_2)
                        {
                            $pid = $r_2[0]['pid']; // 商品ID
                            $goods_price = $r_2[0]['price']; // 商品价格

                            $sql1 = array('session_id'=>$session_id,'goods_id'=>$pid,'attr_id'=>$cid,'starting_amt'=>$startingAmt,'mark_up_amt'=>$markUpAmt,'goods_price'=>$goods_price,'price'=>$startingAmt,'user_id'=>'','status'=>0,'sNo'=>'','is_show'=>1,'recovery'=>0,'add_date'=>$time);
                            $r1 = Db::name('auction_product')->insert($sql1);
                            if($r1 <= 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加场次商品失败！参数:'.json_encode($sql1);
                                $this->Log($Log_content);
                                Db::rollback();
                                $Jurisdiction->admin_record($store_id, $operator, '修改了专场ID：'.$id.'，的信息失败',2,$operator_source,$mch_id,$operator_id);
                                $message = Lang('auction.37');
                                echo json_encode(array('code' => 109,'message' => $message));
                                exit;
                            }
                        }
                    }
                }

                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改专场成功！';
                $this->Log($Log_content);
                Db::commit();
                $Jurisdiction->admin_record($store_id, $operator, '修改了专场ID：'.$id.'，的信息',2,$operator_source,$mch_id,$operator_id);
                $message = Lang('auction.44');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
    }

    // 编辑店铺专场页面(PC店铺、移动端店铺)
    public function specialDetail($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $specialId = $array['specialId']; // 专场ID
        $operator_source = $array['operator_source']; // 操作人来源
        $time = date("Y-m-d H:i:s");

        $list = array();
        $total = 0;

        if($operator_source == 2)
        {
            $field0 = "id as specialId,name,img,promise_amt as promiseAmt,start_date as specialStartDate,end_date as specialEndDate,is_show,status,unit,number as startAmt,mark_up_amt as markUpAmt,content,add_date";
        }
        else
        {
            $field0 = "name,img as imgUrl,promise_amt as promiseAmt,start_date as startDate,end_date as endDate,unit,number as startingAmt,mark_up_amt as markUpAmt,content";
        }
        
        $r0 = AuctionSpecialModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$specialId])->field($field0)->select()->toArray();
        if($r0)
        {
            foreach($r0 as $k => $v)
            {
                if($operator_source == 2)
                {
                    $v['specialImg'] = ServerPath::getimgpath($v['img'], $store_id);
                    $v['promise_amt'] = round($v['promiseAmt'],2);
                    $v['number'] = round($v['startAmt'],2);
                    $v['mark_up_amt'] = round($v['markUpAmt'],2);
                    if($v['status'] == 1)
                    {
                        $v['statusName'] = '未开始';
                    }
                    else if($v['status'] == 2)
                    {
                        $v['statusName'] = '进行中';
                    }
                    else 
                    {
                        $v['statusName'] = '已结束';
                    }
                    $v['goodsNum'] = 0;
                    $v['goodsList'] = array();
                    $r1 = AuctionSessionModel::where(['special_id'=>$specialId,'recovery'=>0])->field('id,name,start_date,end_date')->select()->toArray();
                    if($r1)
                    {
                        $session_id = $r1[0]['id'];
                        $v['id'] = $r1[0]['id'];
                        $v['sessionName'] = $r1[0]['name'];
                        $v['startDate'] = $r1[0]['start_date'];
                        $v['start_date'] = $r1[0]['start_date'];
                        $v['endDate'] = $r1[0]['end_date'];
                        $v['end_date'] = $r1[0]['end_date'];
                        
                        $sql2 = "select b.id,b.attr_id as attrId,b.goods_price as price,b.starting_amt as startingAmt,b.mark_up_amt as markUpAmt,a.product_title as goodsName,a.product_class,c.img,c.attribute from lkt_auction_product as b left join lkt_product_list as a on a.id = b.goods_id left join lkt_configure as c on b.attr_id = c.id where b.session_id = '$session_id' ";
                        $r2 = Db::query($sql2);
                        if($r2)
                        {
                            foreach($r2 as $k2 => $v2)
                            {
                                $v2['imgUrl'] = ServerPath::getimgpath($v2['img'], $store_id);
                                $specifications = '';
                                if($v2['attribute'] != '')
                                {
                                    $attribute = unserialize($v2['attribute']);
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
                                $v2['attribute'] = rtrim($specifications, ",");

                                $class_list = explode('-',trim($v2['product_class'],'-'));
                                $class_num = count($class_list) - 1;
                                $class_id = $class_list[$class_num];
                                $v2['className'] = '';
                                $r3 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$class_id])->field('pname')->select()->toArray();
                                if($r3)
                                {
                                    $v2['className'] = $r3[0]['pname'];
                                }
                                $v['goodsList'][] = $v2;
                            }
                        }
                    }
                }
                else
                {
                    $v['imgUrl'] = ServerPath::getimgpath($v['imgUrl'], $store_id);
                }
                
                $list[] = $v;
            }
        }

        $total = count($list);

        if($operator_source == 2)
        {
            $data = array('list'=>$list,'total'=>$total);
        }
        else
        {
            $data = $list[0];
        }
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 竞拍商品-是否显示(PC店铺)
    public function switchGoods($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id']; // 专场ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        $time = date("Y-m-d H:i:s");

        $r0 = AuctionProductModel::where(['id'=>$id])->field('is_show')->select()->toArray();
        if($r0)
        {
            $is_show = $r0[0]['is_show'];
            $sql_where = array('id'=>$id);
            if($is_show == 1)
            {
                $sql_update = array('is_show'=>0,'update_date'=>$time);
                $message = Lang('auction.42');
            }
            else
            {
                $sql_update = array('is_show'=>1,'update_date'=>$time);
                $message = Lang('auction.41');
            }
            $r1 = Db::name('auction_product')->where($sql_where)->update($sql_update);
            if($r1 > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改竞拍商品成功！';
                $this->Log($Log_content);
                $Jurisdiction->admin_record($store_id, $operator, '修改竞拍商品成功',2,$operator_source,$mch_id,$operator_id);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改竞拍商品失败！条件参数:'.json_encode($sql_where).'；条件参数:'.json_encode($sql_update);
                $this->Log($Log_content);
                $Jurisdiction->admin_record($store_id, $operator, '修改竞拍商品失败',2,$operator_source,$mch_id,$operator_id);
                $message = Lang('Modification failed');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }
    }
    
    // 查看其它拍品(PC店铺)
    public function OtherAuctionItems($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $specialId = $array['specialId']; // 专场ID
        $name = $array['name']; // 拍品名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据
        $operator_source = $array['operator_source']; // 操作人来源

        $list = array();
        $total = 0;

        $r0_0 = AuctionSessionModel::where(['special_id'=>$specialId])->field('id')->select()->toArray();
        if($r0_0)
        {
            $session_id = $r0_0[0]['id'];
        }
        else
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        $condition = " b.session_id = '$session_id' and b.recovery = 0 and a.mch_id != '$mch_id' ";
        if($name != '')
        {
            $name_0 = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and a.product_title like $name_0 ";
        }

        $sql0 = "select a.mch_id,b.id,b.goods_price as goodsPrice,b.price as currentPrice,b.starting_amt as startingAmt,b.mark_up_amt as markUpAmt,b.is_show as isShow,a.product_title as goodsName,c.img,c.attribute from  lkt_auction_product as b left join lkt_product_list as a on a.id = b.goods_id left join lkt_configure as c on b.attr_id = c.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k => $v)
            {
                $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
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
                $v['mchName'] = Db::name('mch')->where('id', $v['mch_id'])->value('name');
                $list[] = $v;
            }
        }

        $total = count($list);

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data'=>$data));
        exit;
    }

    // 竞拍订单(管理后台、PC店铺)
    public function GetOrderList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $keyWord = $array['keyWord']; // 订单号/姓名/电话/用户ID
        $mchName = $array['mchName']; // 店铺名称
        $status = $array['status']; // 状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭
        $startDate = $array['startDate']; // 开始时间
        $endDate = $array['endDate']; // 结束时间
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据
        $operator_source = $array['operator_source']; // 操作人来源

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " o.store_id = '$store_id' and lu.store_id = '$store_id' and o.recycle != 1 and o.otype = 'JP' ";
        if($mch_id != 0 && $mch_id != '')
        {
            $condition .= " and m.id = '$mch_id' ";
        }

        if ($keyWord != '')
        {   
            $keyWord_0 = Tools::FuzzyQueryConcatenation($keyWord);
            $condition .= " and (o.sNo like $keyWord_0 or o.p_sNo like $keyWord_0 or o.name like $keyWord_0 or o.mobile like $keyWord_0 or o.user_id = '$keyWord') ";
        }

        if ($mchName != '')
        {   
            $mchName_0 = Tools::FuzzyQueryConcatenation($mchName);
            $condition .= " and m.name like $mchName_0 ";
        }

        if($status != '')
        {
            $condition .= " and o.status = '$status' ";
        }

        if ($startDate != '')
        {
            $condition .= " and o.add_time >= '$startDate' ";
        }

        if ($endDate != '')
        {
            $condition .= " and o.add_time <= '$endDate' ";
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

        $sql0 = "select tt.* from (select o.id,row_number () over (PARTITION BY o.sNo) AS top  
                    from lkt_order as o 
                    left join lkt_user as lu on o.user_id = lu.user_id 
                    right join lkt_order_details as d on o.sNo = d.r_sNo
                    RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                    RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                    right join lkt_mch as m on p.mch_id = m.id
                    where $condition ) AS tt WHERE tt.top < 2";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = count($r0);
        }

        $sql1 = "select tt.* from (select o.real_sno as mchOrderNo,o.id,o.num,o.sNo,o.name,o.sheng,o.shi,o.xian,o.address,o.add_time as createDate,o.mobile,o.z_price as orderPrice,o.status,o.allow,o.drawid,o.otype,o.ptstatus,o.pay,lu.user_name as userName,o.user_id as userId,o.mch_id,m.id as shop_id,o.self_lifting,o.arrive_time,o.operation_type,d.size,d.after_discount,d.courier_num,row_number () over (PARTITION BY o.sNo) AS top  
            from lkt_order as o 
            left join lkt_user as lu on o.user_id = lu.user_id 
            right join lkt_order_details as d on o.sNo = d.r_sNo
            RIGHT JOIN lkt_configure attr ON attr.id=d.sid
            RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
            right join lkt_mch as m on p.mch_id = m.id
            where $condition ) AS tt WHERE tt.top < 2 order by tt.createDate desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $shop_id = $v['shop_id'];
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
                
                $freight = 0;
                $mch_id = $v['mch_id'];
                $mch_arr = explode(',', $mch_id);
                
                $r1[$k]['orderno'] = $v['sNo'];
                $r1[$k]['attrStr'] = $v['size'];
                $r1[$k]['freight'] = $freight;
                $r1[$k]['addressInfo'] = $v['sheng'] . $v['shi'] . $v['xian'] . $v['address'];
                $zqprice = 0;
                $order_id = $v['sNo'];
                $pay = $v['pay'];

                if (array_key_exists($pay, $payments_type))
                {
                    $r1[$k]['payName'] = $payments_type[$pay];
                }
                else
                {
                    $r1[$k]['payName'] = '钱包';
                }

                $user_id = $v['userId'];
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
                    $r1[$k]['detailFreight'] = $products[0]['freight'];
                    $r1[$k]['detailId'] = $products[0]['id'];
                    $r1[$k]['goodsImgUrl'] = ServerPath::getimgpath($products[0]['imgurl'], $store_id);
                    $r1[$k]['goodsName'] = $products[0]['product_title'];
                    $r1[$k]['goodsPrice'] = $products[0]['p_price'];
                    $r1[$k]['goodsNum'] = count($products);
                    $r1[$k]['expressList'] = $courier_num_arr;
                    $r1[$k]['expressStr'] = trim($expressStr,',');
                    $r1[$k]['mchName'] = $products[0]['mchname'];
                    $r1[$k]['shopName'] = $products[0]['mchname'];
                    $r1[$k]['needNum'] = $products[0]['num'];
                    $pt_status = '';
                    
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
                            $r1[$k]['status'] = '已完成';
                            break;
                        case 7 :
                            $r1[$k]['status'] = '已关闭';
                            break;
                    }

                    switch ($v['self_lifting']) {
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

        $data = array('list'=>$list,'total'=>$total);
        return $data;
    }

    // 订单结算列表(管理后台、PC店铺)
    public function settlementindex($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $search = $array['search']; // 订单号
        $mchName = $array['mchName']; // 店铺名称
        $status = $array['status']; // 状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭
        $startDate = $array['startDate']; // 开始时间
        $endDate = $array['endDate']; // 结束时间
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据
        $operator_source = $array['operator_source']; // 操作人来源

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $list = array();
        $total = 0;

        $condition = " and o.otype = 'JP' ";
        if($store_type == 7)
        { // PC店铺
            $condition .= " and o.recycle not in (1,3) ";
        }
        else if($store_type == 8)
        {
            $condition .= " and o.recycle != 1";
        }

        if($shop_id != 0 && $shop_id != '')
        {
            $condition .= " and m.id = '$shop_id' ";
        }

        if($search != '')
        {
            $search_0 = Tools::FuzzyQueryConcatenation($search);
            $condition .= " and (o.id = '$search' or o.sNo like $search_0) ";
        }

        if($mchName != '')
        {
            $mchName_0 = Tools::FuzzyQueryConcatenation($mchName);
            $condition .= " and m.name like $mchName_0 ";
        }

        if($status != '')
        {
            if($status == 1)
            {
                $condition .= " and o.status = 5 ";
            }
            else
            {
                $condition .= " and o.status in (1,2) ";
            }
        }
        else
        {
            $condition .= " and o.status in (1,2,5) ";
        }

        if ($startDate != '')
        {
            $condition .= " and o.arrive_time >= '$startDate' ";
        }

        if ($endDate != '')
        {
            $condition .= " and o.arrive_time <= '$endDate' ";
        }

        $sql0 = "select ifnull(count(a.sNo),0) as num from (select o.sNo from lkt_order as o left join lkt_user as lu on o.user_id = lu.user_id right join lkt_order_details as d on o.sNo = d.r_sNo right join lkt_configure as c on d.sid = c.id right join lkt_product_list as p on p.id=c.pid right join lkt_mch as m on p.mch_id = m.id where o.store_id = '$store_id' and lu.store_id = '$store_id' $condition group by o.sNo) a ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        $sql1 = "select tt.* from (select o.id,o.sNo,o.z_price,o.arrive_time,o.z_freight,d.freight,o.add_time,o.status,o.settlement_status,d.after_discount,m.name as shopName,row_number () over (partition by o.sNo) as top from lkt_order as o left join lkt_user as lu on o.user_id = lu.user_id right join lkt_order_details as d on o.sNo = d.r_sNo right join lkt_configure as c on d.sid = c.id right join lkt_product_list as p on p.id=c.pid right join lkt_mch as m on p.mch_id = m.id where o.store_id = '$store_id' and lu.store_id = '$store_id' $condition) as tt where tt.top<2 order by tt.add_time desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k1 => $v1)
            {
                $sNo = $v1['sNo'];
                if($v1['settlement_status'] == 1)
                {
                    $v1['status_name'] = '已结算';
                    if($v1['arrive_time'] == '0000-00-00 00:00:00')
                    {
                        $v1['arrive_time'] = '';
                    }
                }
                else
                {
                    $v1['status_name'] = '待结算';
                    $v1['arrive_time'] = '';
                }
                
                $v1['settlementPrice'] = $v1['after_discount']; // 结算金额

                //退款金额
                $sql_re = "select ifnull(sum(real_money),0) as total from lkt_return_order where re_type in (1,2) and r_type in (4,9) and sNo = '$sNo'";
                $res_re = Db::query($sql_re);
                $v1['return_money'] = $res_re[0]['total']; // 退单金额

                $list[] = $v1;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        return $data;
    }
}

