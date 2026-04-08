<?php
namespace app\admin\controller\app;

use app\BaseController;
use think\facade\Db;
use think\facade\Cache;
use app\common\Commission;
use app\common\Plugin\PluginUtils;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\wxpayv2\wxpay;
use app\common\LKTConfigInfo;
use app\common\lktpay\ttpay\TTUtils;
use app\common\Baidu_pay\NuomiRsaSign;
use app\common\LaiKeLogUtils;
use app\common\alipay0\aop\test\TestImage;
use app\common\Plugin\Auction;
use app\common\Plugin\IntegralPublicMethod;
use app\common\GETUI\LaikePushTools;
use app\common\DeliveryHelper;
use app\common\Plugin\MchPublicMethod;
use app\common\PayPalService;

use app\admin\model\OrderDetailsModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDataModel;
use app\admin\model\MchModel;
use app\admin\model\PluginsModel;
use app\admin\model\UserRuleModel;
use app\admin\model\UserModel;
use app\admin\model\PreSellRecordModel;
use app\admin\model\DistributionConfigModel;
use app\admin\model\MemberConfigModel;
use app\admin\model\AuctionPromiseModel;

class Pay extends BaseController
{

    public $appid;
    public $mch_id;
    public $appsecret;
    public $ip = '120.76.189.152';
    public $mch_key;

    public function index()
    {
        header('Access-Control-Allow-Origin: *');
        header('Content-type: text/plain');

        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        
        $type = trim($this->request->param('type')); // 支付类型
        $title = trim($this->request->param('title')); // 
        $remarks = trim($this->request->param('remarks')); // 订单备注
        $grade_l = trim($this->request->param('grade_l')); // 会员特惠 兑换券级别
        $code = trim($this->request->param('code')); // 会员特惠 兑换券级别
        $order_list = trim($this->request->param('order_list')); // 订单信息
        $order_list = htmlspecialchars_decode($order_list);
        $order_list = json_decode($order_list, true);
        
        if(isset($title) && $title != '')
        {
            $title = $this->strFilter($title);
            $long = mb_strlen($title);
            if($long > 10)
            {
                $title = mb_substr($title, 0, 10);
            }
        }

        $this->user = $this->user_list;
        $this->store_id = $store_id;
        $this->store_type = $store_type;
        $this->language = $language;
        $this->access_id = $access_id;
        $this->type = $type;
        $this->remarks = $remarks;
        $this->grade_l = $grade_l;
        $this->code = $code;
        $this->order_list = $order_list;
        $or_type = '';

        if (!empty($order_list))
        {
            $sNo = $order_list['sNo'];
            if(isset($order_list['order_type']))
            {
                $or_type = $order_list['order_type'];
            }
            else
            {
                $or_type = substr($sNo, 0, 2);
            }
        }
        else
        {
            $sNo = addslashes(trim($this->request->param('sNo'))); // 订单sNo
        }

        if (empty($sNo))
        {
            ob_clean();
            $message = Lang('pay.0');
            return output(400, $message);
        }

        $this->or_type = $or_type;
        $this->sNo = $sNo;
        //付款金额
        $payment_money = trim($this->request->param('total')); // 余额抵扣金额
        $payment_money = $payment_money ? $payment_money : trim($this->request->param('payment_money'));
        if(!$payment_money)
        {
            $payment_money = $order_list['total'];
        }

        if($or_type == 'YS')
        {
            $payment_money = $order_list['total'];
        }
        $this->payment_money = $payment_money;
        if ($payment_money && !$grade_l)
        {
            if (!is_numeric($payment_money) || $payment_money < 0)
            {
                ob_clean();
                $message = Lang('pay.1');
                return output(400, $message);
            }
        }

        $get = cache($sNo); // 根据订单号，查询缓存
        if($get)
        { // 存在缓存
            $message = Lang('pay.13');
            return output(109, $message);
        }
        else
        { // 不存在缓存，添加缓存
            cache($sNo,1,180); // 订单号为键名，设置为缓存3分钟
        }

        $this->Log(__METHOD__ . '->' . __LINE__ . '支付方式：'.$type);
        if ($type)
        {
            $type2 = $type;
            if ($type == 'H5_wechat')
            {
                $type2 = 'H5_wechat';
            }
            else if ($type == 'alipay_minipay'  || $type == 'aliPay' || $type == 'tt_alipay')
            {
                $type2 = 'alipay';
            }

            $order_types = substr($sNo, 0, 2);
            if($or_type == 'YS')
            {
                $order_types = $or_type;
            }
            $this->order_types = $order_types;
            $sql2 = "select config_data from lkt_payment as p left join lkt_payment_config as c on c.pid = p.id where c.status = 1 and p.class_name = '$type2' and  c.store_id = '$store_id' ";
            $res2 = Db::query($sql2);
            if ($res2)
            {
                //支付前
                $ret = PluginUtils::invokeMethod($order_types, 'preparePay', $this);
                $this->Log(__METHOD__ . '->' . __LINE__ . json_encode($ret));
                //查找支付方式数据
                $this->config_data = json_decode($res2[0]['config_data']);
                //具体执行支付方式
                if (!empty($ret) && count($ret) == 2)
                {
                    if ($type == 'offline_payment')
                    {
                        $message = Lang('Success');
                        return output(200, $message);
                    }
                    $data = $this->$type((float)$ret[1], $title, $ret[0], $type);
                    $message = Lang('Success');
                    return output(200, $message,$data);
                }
            }
            else
            {
                if ($type == 'wallet_pay')
                {
                    $data = $this->$type($payment_money, $title);
                    $message = Lang('Success');
                    return output(200, $message,$data);
                }
                else if ($type == 'offline_payment')
                {
                    $message = Lang('Success');
                    return output(200, $message);
                }
                else
                {
                    ob_clean();
                    $message = Lang('pay.2');
                    return output(400, $message);
                }
            }
        }
        else
        {
            ob_clean();
            $message = Lang('pay.3');
            return output(400, $message);
        }
    }

    /**
     * [wallet_pay description]
     * <p>Copyright (c) 2018-2019</p>
     * <p>Company: www.laiketui.com</p>
     * @Author  苏涛
     * @return  余额支付
     * @version 2.0
     * @date    2019-01-18T22:22:09+0800
     */
    public function wallet_pay()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id

        $type = trim($this->request->param('type')); // 支付类型
        $order_list = trim($this->request->param('order_list')); // 订单信息
        $sNo = addslashes(trim($this->request->param('sNo'))); // 订单sNo
        $remarks = trim($this->request->param('remarks')); // 订单备注
        $payment_money = trim($this->request->param('payment_money')); // 支付金额
        $parameter = trim($this->request->param('parameter')); // 参数
        $payTarget = trim($this->request->param('payTarget')); // 1.定金 2.尾款 3.全款
        
        $or_type = substr($sNo, 0, 2);

        if($order_list != '')
        {
            $order_list = htmlspecialchars_decode($order_list);
            $order_list = json_decode($order_list, true);
            $payment_money = $order_list['total'];
        }
        else
        {
            $sql_o = "select z_price from lkt_order where store_id = '$store_id' and sNo = '$sNo' and otype = '$or_type' ";
            $r_o = Db::query($sql_o);
            if($r_o)
            {
                $payment_money = $r_o[0]['z_price'];
            }
        }

        $user_id = $this->user_list['user_id'];
        $user = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('money,grade,score')->select()->toArray();
        $user_money = $user[0]['money'];
        $grade = $user[0]['grade']; // 会员级别 0--普通会员 
        $total_score = $user[0]['score'];
        
        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>$currency_id));
        $currency_symbol = $userCurrency[0]['currency_symbol'];
        $exchange_rate = $userCurrency[0]['exchange_rate'];
        $currency_code = $userCurrency[0]['currency_code'];

        $time = date("Y-m-d H:i:s");
        Db::startTrans();

        if ($user_money >= $payment_money)
        {
            $sql_where = array('store_id'=>$store_id,'user_id'=>$user_id);
            $sql_update = array('money'=>Db::raw('money-'.$payment_money));
            $r = Db::name('user')->where($sql_where)->where('money','>',0)->update($sql_update);
            if($r < 0)
            { // 回滚删除已经创建的订单
                $this->log(__METHOD__ . ":" . __LINE__ . "修改用户余额失败！where参数:" . json_encode($sql_where) . ";update参数：" . json_encode($sql_update));
                Db::rollback();
                ob_clean();
                $message = Lang('operation ailed');
                return output(400, $message);
            }

            if($or_type == 'DJ')
            { // 会员
                $r_mc = OrderDataModel::where(['order_type'=>'DJ','trade_no'=>$sNo])->field('data')->select()->toArray();
                if($r_mc)
                {   
                    $info = json_decode($r_mc[0]['data'],true);

                    $price_mc = $info['amount'];
                    $type_mc = $info['memberType'];
                    $start_time_mc = $info['startTime'];
                    $end_time_mc = $info['endTime'];
                    $coupon_id_mc = $info['couponId'];

                    $sql_update_mc = array('grade_m'=>$type_mc,'is_box'=>1,'grade_add'=>$start_time_mc,'grade_end'=>$end_time_mc,'grade'=>1,'is_out'=>0);
                    $sql_where_mc = array('store_id'=>$store_id,'user_id'=>$user_id);
                    $r_mc0 = Db::name('user')->where($sql_where_mc)->update($sql_update_mc);
                    if($r_mc0 <= 0)
                    {
                        //回滚删除已经创建的订单
                        Db::rollback();
                        ob_clean();
                        $message = Lang('operation failed');
                        return output(400, $message);
                    }
                    $points = 0;
                    $r_config_mc = MemberConfigModel::where('store_id',$store_id)->field('open_config,bonus_points_open,bonus_points_config')->select()->toArray();
                    if($r_config_mc)
                    {
                        $open_config_mc = json_decode($r_config_mc[0]['open_config'],true); // 开通设置
                        $bonus_points_open_mc = $r_config_mc[0]['bonus_points_open']; // 会员赠送积分开关 0.关 1.开
                        $bonus_points_config_mc = json_decode($r_config_mc[0]['bonus_points_config'],true); // 会员赠送积分设置
                    
                        foreach($open_config_mc as $k_0_mc => $v_0_mc)
                        {
                            if($type_mc == ($k_0_mc+1))
                            {
                                // $openMethod_mc = $v_0_mc['openMethod'];
                                $points = $v_0_mc['points'];
                            }
                        }
                        // foreach($bonus_points_config_mc as $k_1_mc => $v_1_mc)
                        // {
                        //     if($openMethod_mc == $v_1_mc['openMethod'])
                        //     {
                        //         $points = $v_1_mc['points'];
                        //     }
                        // }

                        // if($bonus_points_open_mc == 1)
                        // {
                            $sql_update_mc1 = array('score'=>Db::raw('score+'.$points));
                            $sql_where_mc1 = array('store_id'=>$store_id,'user_id'=>$user_id);
                            $r_mc1 = Db::name('user')->where($sql_where_mc1)->update($sql_update_mc1);

                            //写入积分日志
                            $event = $user_id . '通过会员套餐获得' . $points . '积分';
                            $sql_3_mc = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$points,'total_score'=>$total_score,'record'=>$event,'sign_time'=>$time,'type'=>11,'recovery'=>0);
                            $r_3_mc = Db::name('sign_record')->insert($sql_3_mc);
                            if($r_3_mc < 0)
                            {
                                Db::rollback();
                                ob_clean();
                                $message = Lang('operation failed');
                                return output(400, $message);
                            }
                        // }
                    }

                    $array = array('store_id'=>$store_id,'money'=>$price_mc,'user_money'=>$user_money,'type'=>9,'money_type'=>2,'money_type_name'=>9,'record_notes'=>'','type_name'=>'wallet_pay','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'','currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code);
                    $details_id = PC_Tools::add_Balance_details($array);

                    //插入余额记录
                    $sql_1_mc = array('store_id'=>$store_id,'user_id'=>$user_id,'money'=>$price_mc,'oldmoney'=>$user_money,'event'=>'充值会员','type'=>4,'details_id'=>$details_id);
                    $r_1_mc = Db::name('record')->insert($sql_1_mc);
                    if($r_1_mc < 0)
                    {
                        Db::rollback();
                        ob_clean();
                        $message = Lang('operation failed');
                        return output(400, $message);
                    }
                    
                    if ($coupon_id_mc)
                    {
                        if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
                        {
                            bind('coupon','app\common\Plugin\CouponPublicMethod');
                            $r_coupon0 = app('coupon')->update_coupon($store_id, $user_id, $coupon_id_mc,2);
                            if ($r_coupon0 == 2)
                            {
                                //回滚删除已经创建的订单
                                Db::rollback();
                                ob_clean();
                                $message = Lang('nomal_order.4');
                                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                                exit;
                            }
                            
                            $r_coupon1 = app('coupon')->coupon_sno($store_id, $user_id, $coupon_id_mc,$sNo,'add');
                            if ($r_coupon1 == 2)
                            {
                                //回滚删除已经创建的订单
                                Db::rollback();
                                ob_clean();
                                $message = Lang('nomal_order.5');
                                echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                                exit;
                            }
                            $coupon_Log_content = '会员' . $user_id . ',订单号' . $sNo . '使用优惠券ID为' . $coupon_id_mc;
                            app('coupon')->couponLog($coupon_id_mc, $coupon_Log_content);
                        }
                    }

                    //修改状态
                    $sql_d = OrderDataModel::where(['order_type'=>'DJ','trade_no'=>$sNo])->find();
                    $sql_d->status = 1;
                    $res_d = $sql_d->save();
                    if(!$res_d)
                    {
                        Db::rollback();
                        ob_clean();
                        $message = Lang('operation failed');
                        return output(400, $message);
                    }
                }

                if($grade == 1)
                {
                    $msg_title = Lang("member.13");
                    $msg_content = Lang("member.14");
                }
                else
                {
                    $msg_title = Lang("member.11");
                    $msg_content = Lang("member.12");
                }

                //给用户发送消息
                $pusher = new LaikePushTools();
                $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, 'admin');

                Db::commit();
                $message = Lang('Success');
                return output(200, $message);
            }
            else if($or_type == 'JB')
            { // 竞拍保
                $array = array('store_id'=>$store_id,'money'=>$payment_money,'user_money'=>$user_money,'type'=>8,'money_type'=>2,'money_type_name'=>18,'record_notes'=>'','type_name'=>'竞拍活动','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'','currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code);
                $details_id = PC_Tools::add_Balance_details($array);

                $event = $user_id . '使用了' . $payment_money . '元余额';
                $sqll = array('store_id'=>$store_id,'user_id'=>$user_id,'money'=>$payment_money,'oldmoney'=>$user_money,'event'=>$event,'type'=>4,'details_id'=>$details_id);
                $rr = Db::name('record')->insert($sqll);

                $sql_update_auction_promise = array('is_pay'=>1);
                $sql_where_auction_promise = array('store_id'=>$store_id,'trade_no'=>$sNo);
                $r_auction_promise = Db::name('auction_promise')->where($sql_where_auction_promise)->update($sql_update_auction_promise);
                if($r_auction_promise <= 0)
                {
                    //回滚删除已经创建的订单
                    Db::rollback();
                    ob_clean();
                    $message = Lang('operation failed');
                    return output(400, $message);
                }
                else
                {
                    $sql_update_order_data = array('status'=>1);
                    $sql_where_order_data = array('trade_no'=>$sNo);
                    $r_order_data = Db::name('order_data')->where($sql_where_order_data)->update($sql_update_order_data);
                    Db::commit();
                    $data = array('payment'=>$payment_money);
                    $message = Lang('Success');
                    return output(200, $message,$data);
                }
            }
            else
            {   
                if($or_type == 'PS')
                { // 预售
                    if($payTarget == 3)
                    {
                        $array = array('store_id'=>$store_id,'money'=>$payment_money,'user_money'=>$user_money,'type'=>7,'money_type'=>2,'money_type_name'=>4,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                    }
                    else
                    {
                        $array = array('store_id'=>$store_id,'money'=>$payment_money,'user_money'=>$user_money,'type'=>8,'money_type'=>2,'money_type_name'=>8,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                    }
                }
                else if($or_type == 'ZB')
                { // 直播订单
                    $array = array('store_id'=>$store_id,'money'=>$payment_money,'user_money'=>$user_money,'type'=>7,'money_type'=>2,'money_type_name'=>20,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                }
                else if($or_type == 'FX')
                { // 分稍订单
                    $array = array('store_id'=>$store_id,'money'=>$payment_money,'user_money'=>$user_money,'type'=>7,'money_type'=>2,'money_type_name'=>14,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                }
                else if($or_type == 'KT' || $or_type == 'PT')
                { // 拼团订单
                    $array = array('store_id'=>$store_id,'money'=>$payment_money,'user_money'=>$user_money,'type'=>7,'money_type'=>2,'money_type_name'=>11,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                }
                else if($or_type == 'VI')
                { // 虚拟订单
                    $array = array('store_id'=>$store_id,'money'=>$payment_money,'user_money'=>$user_money,'type'=>7,'money_type'=>2,'money_type_name'=>19,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                }
                else if($or_type == 'MS')
                { // 秒杀订单
                    $array = array('store_id'=>$store_id,'money'=>$payment_money,'user_money'=>$user_money,'type'=>7,'money_type'=>2,'money_type_name'=>15,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                }
                else if($or_type == 'IN')
                { // 积分订单
                    $array = array('store_id'=>$store_id,'money'=>$payment_money,'user_money'=>$user_money,'type'=>7,'money_type'=>2,'money_type_name'=>16,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                }
                else if($or_type == 'FS')
                { // 限时折扣订单
                    $array = array('store_id'=>$store_id,'money'=>$payment_money,'user_money'=>$user_money,'type'=>7,'money_type'=>2,'money_type_name'=>17,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                }
                else
                { // 普通订单
                    $array = array('store_id'=>$store_id,'money'=>$payment_money,'user_money'=>$user_money,'type'=>7,'money_type'=>2,'money_type_name'=>4,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                }
                $array['currency_symbol'] = $currency_symbol;
                $array['exchange_rate'] = $exchange_rate;
                $array['currency_code'] = $currency_code;
                $details_id = PC_Tools::add_Balance_details($array);
                
                $event = $user_id . '使用了' . $payment_money . '元余额';
                $sqll = array('store_id'=>$store_id,'user_id'=>$user_id,'money'=>$payment_money,'oldmoney'=>$user_money,'event'=>$event,'type'=>4,'details_id'=>$details_id);
                $rr = Db::name('record')->insert($sqll);
                
                $this->store_id = $store_id;
                $this->user_id = $user_id;
                $this->payment_money = $payment_money;
                $this->sNo = $sNo;
                $this->order_types = $or_type;

                PluginUtils::invokeMethod($or_type, 'walletcb', $this);

                Db::commit();
                $message = Lang('Success');
                if($or_type == 'KT')
                {
                    $data = array('payment' => $payment_money);
                    return output(200, $message,$data);
                }
                else
                {
                    return output(200, $message);
                }
            }
        }
        else
        {
            Db::rollback();
            ob_clean();
            $message = Lang('pay.4');
            return output(400, $message);
        }
    }

    // 付款成功
    public function gndd($store_id, $payment_money, $sNo, $user_id,$order_type='')
    {
        Db::startTrans();
        $time = date("Y-m-d H:i:s");

        $user = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('money,grade,score')->select()->toArray();
        $total_score = $user[0]['score'];

        if($order_type =='PS')
        {
            $r_pre_sell_record = PreSellRecordModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id])->field('id,is_deposit,order_info,order_details_info')->select()->toArray();
            if($r_pre_sell_record)
            {
                $pre_sell_record_id = $r_pre_sell_record[0]['id'];
                $is_deposit = $r_pre_sell_record[0]['is_deposit'];
                $order_info = json_decode($r_pre_sell_record[0]['order_info'],true);
                $order_details_info = json_decode($r_pre_sell_record[0]['order_details_info'],true);
                if($is_deposit == '')
                { // 订货模式
                    $sql_oo_where = array('store_id'=>$store_id,'sNo'=>$sNo);
                    $sql_oo_update = array('status'=>1,'pay'=>'wallet_pay','pay_time'=>$time);
                    $r = Db::name('order')->where($sql_oo_where)->update($sql_oo_update);
                    if ($r < 1)
                    {
                        //回滚删除已经创建的订单
                        ob_clean();
                        Db::rollback();
                        $message = Lang('operation failed');
                        return output(400, $message);
                    }

                    $sql_o = array('r_status'=>1);
                    $r = Db::name('order_details')->where(['store_id'=>$store_id,'r_sNo'=>$sNo])->update($sql_o);
                    if ($r < 0)
                    {
                        //回滚删除已经创建的订单
                        Db::rollback();
                        ob_clean();
                        $message = Lang('operation failed');
                        return output(400, $message);
                    }
                    
                    $sql1_where = array('id'=>$pre_sell_record_id);
                    $sql1_update = array('price'=>Db::raw('price+'.$payment_money));
                    $r1 = Db::name('pre_sell_record')->where($sql1_where)->update($sql1_update);
                }
                else if($is_deposit == 0)
                { // 定金模式（支付定金）
                    $Toosl = new Tools($store_id,1);
                    $sNo = $Toosl->Generate_order_number('PS', 'sNo'); // 生成订单号
                    
                    if(isset($order_info['remarks'][0]))
                    {
                        $remarks = $order_info['remarks'][0];
                    }
                    else
                    {
                        $remarks = '';
                    }

                    $sql_o = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$order_info['name'],'mobile'=>$order_info['mobile'],'num'=>$order_info['num'],'z_price'=>$order_info['z_price'],'sNo'=>$sNo,'sheng'=>$order_info['sheng'],'shi'=>$order_info['shi'],'xian'=>$order_info['xian'],'address'=>$order_info['address'],'remark'=>$order_info['remark'],'pay'=>'wallet_pay','add_time'=>date("Y-m-d H:i:s"),'status'=>0,'coupon_id'=>0,'subtraction_id'=>0,'consumer_money'=>0,'coupon_activity_name'=>'','spz_price'=>$order_info['spz_price'],'reduce_price'=>0,'coupon_price'=>0,'source'=>$order_info['source'],'otype'=>'PS','mch_id'=>$order_info['mch_id'],'p_sNo'=>'','bargain_id'=>0,'comm_discount'=>1,'remarks'=>$remarks,'real_sno'=>$order_info['real_sno'],'self_lifting'=>0,'extraction_code'=>'','extraction_code_img'=>'','grade_rate'=>$order_info['grade_rate'],'z_freight'=>$order_info['z_freight'],'preferential_amount'=>0,'single_store'=>0);
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
                        }
                        else
                        { // 回滚删除已经创建的订单
                            $this->log(__METHOD__ . ":" . __LINE__ . "添加订单详情失败！参数:" . json_encode($sql_d));
                            Db::rollback();
                            ob_clean();
                            $message = Lang('nomal_order.6');
                            return output(400, $message);
                        }
                    }
                    else
                    { // 回滚删除已经创建的订单
                        $this->log(__METHOD__ . ":" . __LINE__ . "添加订单失败！参数:" . json_encode($sql_o));
                        Db::rollback();
                        ob_clean();
                        $message = Lang('nomal_order.6');
                        return output(400, $message);
                    }
                }
                else if($is_deposit == 1)
                { // 定金模式（支付尾款）
                    $sql_oo_where = array('store_id'=>$store_id,'sNo'=>$sNo);
                    $sql_oo_update = array('status'=>1,'pay'=>'wallet_pay','pay_time'=>$time,'remind'=>$time);
                    $r = Db::name('order')->where($sql_oo_where)->update($sql_oo_update);
                    if ($r < 1)
                    {
                        //回滚删除已经创建的订单
                        ob_clean();
                        Db::rollback();
                        $message = Lang('operation failed');
                        return output(400, $message);
                    }

                    $sql_o = array('r_status'=>1);
                    $r = Db::name('order_details')->where(['store_id'=>$store_id,'r_sNo'=>$sNo])->update($sql_o);
                    if ($r < 0)
                    {
                        //回滚删除已经创建的订单
                        Db::rollback();
                        ob_clean();
                        $message = Lang('operation failed');
                        return output(400, $message);
                    }

                    $sql1_where = array('id'=>$pre_sell_record_id);
                    $sql1_update = array('price'=>Db::raw('price+'.$payment_money),'is_balance'=>1,'pay_balance_time'=>$time);
                    $r1 = Db::name('pre_sell_record')->where($sql1_where)->update($sql1_update);
                }
            }
            
            Db::commit();
            return;
        }
        else
        {
            $r2 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id])->select()->toArray();
            if ($r2)
            {
                $z_price = $r2[0]['z_price'];
                $spz_price = $r2[0]['spz_price'];
                $self_lifting = $r2[0]['self_lifting']; // 自提 0.配送 1.自提 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
                $mobile = $r2[0]['mobile'];
                $otype = $r2[0]['otype']; // 订单类型
                $allow = $r2[0]['allow']; // 积分
                $drawid = $r2[0]['drawid']; // 活动ID
                $mch_id = trim($r2[0]['mch_id'],',');
            }
            else
            {
                Db::rollback();
                ob_clean();
                $message = Lang('Parameter error');
                return output(400, $message);
            }

            $is_integral = false; // 没有积分插件
            $ams_time = 7;
            // 判断积分商城插件是否安装
            $sql_in = " select * from lkt_plugins where store_id = '$store_id' and plugin_code = 'integral' and flag = 0 ";
            $res_in = Db::query($sql_in);
            if($res_in)
            {
                $is_integral = true; // 有积分插件
            }

            $now = date("Y-m-d H:i:s"); // 当前时间
            $date = date("m-d"); // 获取月和日
            $Is_it_a_birthday_on_the_same_day = false; // 今天不过生日
            $scor = 0; // 发放积分
            if($is_integral)
            {
                // 根据商城ID、user_id、是会员、生日当天，查询用户是否过生日
                $sql_l = "select birthday from lkt_user where store_id = '$store_id' and user_id = '$user_id' and grade_end > '$now' and date_format(birthday,'%m-%d') = '$date'";
                $res_l = Db::query($sql_l);
                if($res_l)
                { // 当天过生日
                    $Is_it_a_birthday_on_the_same_day = true; // 今天过生日

                    $sql_config = "select birthday_open,points_multiple from lkt_member_config where store_id = $store_id and is_open = 1";
                    $r_config = Db::query($sql_config);
                    if($r_config)
                    {
                        if($r_config[0]['birthday_open'] == 1)
                        { // 会员生日特权开关 0.关 1.开
                            $scor = $z_price * $r_config[0]['points_multiple']; // 订单金额 * 积分倍数
                        }
                    }
                }
            }

            $sNo_list = array(); 
            // 根据父订单号，查询订单
            $sql__0 = "select sNo from lkt_order where store_id = '$store_id' and p_sNo = '$sNo' ";
            $r__0 = Db::query($sql__0);
            if($r__0)
            { // 有，支付单号是主订单
                foreach($r__0 as $k__0 => $v__0)
                {
                    $sNo_list[] = $v__0['sNo']; 
                }

                $sql_oo_where = array('store_id'=>$store_id,'sNo'=>$sNo);
                if ($self_lifting == '1')
                { // 自提
                    $sql_oo_update = array('z_price'=>Db::raw('offset_balance+'.$z_price),'status'=>2,'pay_time'=>$time);
                }
                else if ($self_lifting == '3')
                { // 虚拟订单需要线下核销
                    $sql_oo_update = array('z_price'=>Db::raw('offset_balance+'.$z_price),'status'=>8,'pay_time'=>$time);
                }
                else if ($self_lifting == '4')
                { // 虚拟订单无需线下核销
                    $sql_oo_update = array('z_price'=>Db::raw('offset_balance+'.$z_price),'status'=>5,'pay_time'=>$time);
                }
                else
                { // 配送
                    $sql_oo_update = array('z_price'=>Db::raw('offset_balance+'.$z_price),'status'=>1,'pay_time'=>$time);
                }
                $r = Db::name('order')->where($sql_oo_where)->update($sql_oo_update);
                if ($r < 1)
                {
                    Db::rollback();
                    file_put_contents($file, '更新订单状态为待发货SQL:' . $sql_oo_update, FILE_APPEND);
                    exit;
                }
            }
            else
            {
                $sNo_list[] = $sNo; 
            }

            $score_deduction = 0;
            if ((int)$z_price == (int)$payment_money)
            {
                foreach($sNo_list as $k => $v)
                {
                    $r2 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$v,'user_id'=>$user_id])->select()->toArray();
                    $self_lifting = $r2[0]['self_lifting']; // 自提 0.配送 1.自提 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
                    $score_deduction += $r2[0]['score_deduction']; // 积分支付抵扣

                    $sql_oo_where = array('store_id'=>$store_id,'sNo'=>$v);
                    if ($self_lifting == '1' )
                    { // 自提 
                        $sql_oo_update = array('status'=>2,'pay'=>'wallet_pay','pay_time'=>$time);
                    }
                    else if ($self_lifting == '3')
                    { // 虚拟订单需要线下核销
                        $sql_oo_update = array('status'=>8,'pay'=>'wallet_pay','pay_time'=>$time);
                    }
                    else if ($self_lifting == '4')
                    { // 虚拟订单无需线下核销
                        $sql_oo_update = array('status'=>5,'pay'=>'wallet_pay','pay_time'=>$time);
                    }
                    else
                    { // 配送
                        $sql_oo_update = array('status'=>1,'pay'=>'wallet_pay','pay_time'=>$time);
                    }
                    $r = Db::name('order')->where($sql_oo_where)->update($sql_oo_update);
                    if ($r < 1)
                    {
                        Db::rollback();
                        $message = Lang('operation failed');
                        return output(400, $message);
                    }

                    if ($self_lifting == '1' )
                    { // 自提
                        $sql_o = array('r_status'=>2);
                    }
                    else if ($self_lifting == '3')
                    { // 虚拟订单需要线下核销
                        $sql_o = array('r_status'=>8);
                    }
                    else if ($self_lifting == '4')
                    { // 虚拟订单无需线下核销
                        $sql_o = array('r_status'=>5);
                    }
                    else
                    { // 配送
                        $sql_o = array('r_status'=>1);
                    }
                    $r = Db::name('order_details')->where(['store_id'=>$store_id,'r_sNo'=>$v])->update($sql_o);
                    if ($r < 0)
                    {
                        Db::rollback();
                        $message = Lang('operation failed');
                        return output(400, $message);
                    }

                    $sql1 = "select a.id,d.id as p_id,d.supplier_superior,b.num,a.offset_balance,b.supplier_id,b.living_room_id,b.sid from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo left join lkt_configure as c on b.sid = c.id left join lkt_product_list as d on c.pid = d.id where a.store_id = $store_id and b.r_sNo = '$v'";
                    $r1 = Db::query($sql1);
                    if ($r1)
                    {   
                        $payment_money = $r1[0]['offset_balance'];
                        foreach ($r1 as $k1 => $v1)
                        {   
                            $pid0 = $v1['p_id'];
                            $num = $v1['num'];
                            $supplier_superior = $v1['supplier_superior'];
                            $living_id = $v1['living_room_id'];
                            $sid = $v1['sid'];
                            if($supplier_superior != 0)
                            { // 供应商商品
                                // 更新销量
                                Db::name('product_list')->where(['store_id'=>$store_id,'supplier_superior'=>$supplier_superior])->update(['real_volume' =>  Db::raw('real_volume + '.$num)]);

                                // 更新供应商商品销量
                                Db::name('product_list')->where(['store_id'=>$store_id,'id'=>$supplier_superior])->update(['real_volume' =>  Db::raw('real_volume + '.$num)]);
                            }
                            else
                            { // 不是供应商商品
                                // 更新商品销量
                                Db::name('product_list')->where(['store_id'=>$store_id,'id'=>$pid0])->update(['real_volume' =>  Db::raw('real_volume + '.$num)]);
                            }

                            if($otype == 'ZB')
                            {
                                $sql_living_product = "update lkt_living_product set xl_num = xl_num + '$num' where store_id = '$store_id' and living_id = '$living_id' and pro_id = '$pid0' and config_id = '$sid' ";
                                $r_living_product = Db::execute($sql_living_product);

                                $id6 = '"' . $pid0 . '"';
                                $sql6 = " update lkt_product_list set real_volume = real_volume + '$num' where commodity_str like '%$id6%' ";
                                $r6 = Db::execute($sql6);
                            }
                        }
                    }

                    $r0 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$v,'user_id'=>$user_id])->select()->toArray();
                    $mch_id = $r0[0]['mch_id']; // 店铺ID字符串
                    $mch_id = substr($mch_id, 1, -1);

                    $message_1 = "订单".$v."已支付成功，正等待发货中，请及时发货！";
                    $message_logging_list1 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>1,'parameter'=>$v,'content'=>$message_1);
                    PC_Tools::add_message_logging($message_logging_list1);
                }
                if($score_deduction > 0)
                { // 使用了积分抵扣
                    $sql_score_where = array('store_id'=>$store_id,'user_id'=>$user_id);
                    $sql_score_update = array('score'=>Db::raw('score-'.$score_deduction));
                    $r_score = Db::name('user')->where($sql_score_where)->update($sql_score_update);
                    if ($r_score == -1)
                    { // 回滚删除已经创建的订单
                        Db::rollback();
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 更新用户积分失败！条件：' . json_encode($sql_score_where) . "；修改参数：" . json_encode($sql_score_update);
                        $this->Log($Log_content);
                        exit;
                    }

                    $event = $user_id . '使用了' . $score_deduction . '积分';
                    $sqll = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$score_deduction,'total_score'=>$total_score,'record'=>$event,'type'=>1,'sign_time'=>$time,'recovery'=>0,'sNo'=>$sNo);
                    $rr = Db::name('sign_record')->insert($sqll);
                    if ($rr < 1)
                    {
                        Db::rollback();
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加积分记录表失败！数据：' . json_encode($sqll);
                        $this->Log($Log_content);
                        exit;
                    }
                }
                if($is_integral)
                {
                    //积分发放
                    $integral = new IntegralPublicMethod();
                    $integral->issueIntegral($store_id, $sNo, $user_id);
                }
                if($scor > 0 && $Is_it_a_birthday_on_the_same_day)
                { // 今天过生日 并且 发放积分大于0
                    $sql_score_where = array('store_id'=>$store_id,'user_id'=>$user_id);
                    $sql_score_update = array('score'=>Db::raw('score+'.$scor));
                    // $sql_score_update = array('lock_score'=>Db::raw('lock_score+'.$scor),'score'=>Db::raw('score+'.$scor));
                    $r_score = Db::name('user')->where($sql_score_where)->update($sql_score_update);
                    if ($r_score == -1)
                    { // 回滚删除已经创建的订单
                        Db::rollback();
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 更新用户积分失败！条件：' . json_encode($sql_score_where) . "；修改参数：" . json_encode($sql_score_update);
                        $this->Log($Log_content);
                        exit;
                    }

                    $event = $user_id . '会员生日特权奖励获得' . $scor . '积分';
                    $sqll = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$scor,'total_score'=>$total_score,'record'=>$event,'sign_time'=>$now,'type'=>8,'recovery'=>0,'sNo'=>$sNo,'frozen_time'=>date("Y-m-d H:i:s",strtotime("+$ams_time day")));
                    $rr = Db::name('sign_record')->insert($sqll);
                    if ($rr < 1)
                    {
                        Db::rollback();
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加积分记录表失败！数据：' . json_encode($sqll);
                        $this->Log($Log_content);
                        exit;
                    }
                }
                //判断分销插件是否安装
                if($otype == 'FX')
                {
                    $res_plu = PluginsModel::where(['store_id'=>$store_id,'plugin_code'=>'distribution','status'=>1,'flag'=>0])->select()->toArray();
                    if($res_plu)
                    {   
                        //获取分销配置
                        $res_con = DistributionConfigModel::where(['store_id'=>$store_id])->select()->toArray();
                        if($res_con && $res_con[0]['status'] == 1)
                        {
                            $sql_o = "select a.p_price,a.num,c.uplevel,d.z_price from lkt_order_details as a left join lkt_product_list as b on b.id = a.p_id left join lkt_distribution_goods as c on a.p_id = c.p_id and a.sid = c.s_id left join lkt_order as d on a.r_sNo = d.sNo where b.store_id = '$store_id' and a.r_sNo = '$sNo' and c.recycle = 0";
                            $res_o = Db::query($sql_o);
                            if($res_o)
                            {
                                foreach ($res_o as $key => $value) 
                                {
                                    $p_price = $value['p_price']; // 单价
                                    $num = $value['num']; // 数量
                                    $z_price = $value['z_price']; // 订单金额
                                    $sets = json_decode($res_con[0]['sets'],true);
                                    $c_pay = $sets['c_pay'];
                                    //分销
                                    $comm = new Commission();
                                    if ($c_pay == 1)
                                    {   
                                        $comm->uplevel( $store_id, $user_id);
                                        $comm->putcomm( $store_id, $sNo, $z_price);
                                    }
                                    if($value['uplevel'] > 0 )
                                    {
                                        $comm->straight_up( $store_id, $user_id,$value['uplevel']);
                                    }
                                }
                            }
                        }  
                    }
                }
                else if($otype == 'IN')
                {
                    $sql_user_where = array('store_id'=>$store_id,'user_id'=>$user_id);
                    $sql_user_update = array('score'=>Db::raw('score-'.$allow));
                    $r_user = Db::name('user')->where($sql_user_where)->update($sql_user_update);
                    if($r_user < 0)
                    { // 回滚删除已经创建的订单
                        Db::rollback();
                        ob_clean();
                        $message = Lang('operation failed');
                        return output(400, $message);
                    }

                    // 添加操作记录
                    $event = $user_id . '使用了' . $allow . '积分';
                    $sql_sign_record = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$allow,'total_score'=>$total_score,'record'=>$event,'type'=>1,'sign_time'=>$time,'recovery'=>0,'sNo'=>$sNo);
                    $r_sign_record = Db::name('sign_record')->insert($sql_sign_record);
                    if ($r_sign_record < 1)
                    {
                        Db::rollback();
                        ob_clean();
                        $message = Lang('operation failed');
                        return output(400, $message);
                    }
                }
                else if($otype == 'PT')
                {
                    $sql_pt0 = "select a.id,a.team_num from lkt_group_open as a left join lkt_group_open_record as b on a.id = b.open_id where a.activity_id = '$drawid' and a.recycle = 0 and b.user_id = '$user_id' and b.sno = '$sNo' ";
                    $r_pt0 = Db::query($sql_pt0);
                    if($r_pt0)
                    {
                        $open_id = $r_pt0[0]['id'];
                        $team_num = $r_pt0[0]['team_num'];

                        $sql_pt1 = "select count(id) as total from lkt_group_open_record where open_id = '$open_id' and recycle = 0 ";
                        $r_pt1 = Db::query($sql_pt1);
                        if($r_pt1)
                        {
                            if($team_num == $r_pt1[0]['total'])
                            { // 当 团队数量 == 参团数量 拼团成功
                                $sql_pt2 = "update lkt_group_open set status = 1 where id = '$open_id' ";
                                $r_pt2 = Db::execute($sql_pt2);
                                if ($r_pt2 < 0)
                                {
                                    Db::rollback();
                                    $message = Lang('operation failed');
                                    return output(400, $message);
                                }

                                $msg_title = Lang("go_group.12");
                                $msg_content = Lang("go_group.13");
                                $pusher = new LaikePushTools();

                                $sql_pt3 = "select user_id from lkt_group_open_record where open_id = '$open_id' and recycle = 0 ";
                                $r_pt3 = Db::query($sql_pt3);
                                if($r_pt3)
                                {
                                    foreach($r_pt3 as $k_3 => $v_3)
                                    {
                                        $user_id = $v_3['user_id'];
                                        $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, '');
                                    }
                                }
                            }
                        }
                    }
                }
                else if($otype == 'JP')
                {
                    $auction = new Auction();
                    //付款成功退回保证金
                    //根据订单号查询专场保证金已缴纳
                    $sql_s = "select a.session_id,d.id,a.goods_id,a.attr_id,d.user_id,c.mch_id,c.commission,d.promise,d.trade_no,b.special_id from lkt_auction_product a LEFT JOIN lkt_auction_session b on a.session_id=b.id LEFT JOIN lkt_auction_special c on b.special_id=c.id LEFT JOIN lkt_auction_promise d on d.special_id = c.id where c.store_id = '$store_id' and a.user_id = '$user_id' and d.user_id = '$user_id' and a.sNo = '$sNo' and d.is_pay = 1 ";
                    $res_s = Db::query($sql_s);
                    if($res_s)
                    {
                        $special_id = $res_s[0]['special_id'];//专场ID
                        $session_id = $res_s[0]['session_id'];//场次ID
                        //查看该用户在当前场次下是否拍下多个商品且还有订单未付款
                        $sql_g = "select p.id from lkt_auction_product p left join lkt_order o on p.sNo = o.sNo where p.session_id='$session_id' and p.user_id='$user_id' and p.sNo != '$sNo' and o.status = 0 and p.recovery = 0 ";
                        $res_g = Db::query($sql_g);
                        if(!$res_g)
                        { 
                            $mch_id_0 = $res_s[0]['mch_id']; // 店铺ID
                            $mch_account_money = 0; // 店铺金额
                            $mch_name = '';
                            $cpc = '';
                            $mobile = '';
                            $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id_0])->field('account_money,name,cpc,tel')->select()->toArray();
                            if($r_mch)
                            {
                                $mch_account_money = $r_mch[0]['account_money']; // 店铺金额
                                $mch_name = $r_mch[0]['name'];//店铺名称
                                $cpc = $r_mch[0]['cpc']; // 区号
                                $mobile = $r_mch[0]['tel']; // 店铺联系电话
                            }

                            $promise_id_0 = $res_s[0]['id']; // 用户id
                            $user_id_0 = $res_s[0]['user_id']; // 用户id
                            $promise_0 = $res_s[0]['promise']; // 保证金
                            $trade_no = $res_s[0]['trade_no']; // 保证金支付单号

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
                            $Tools = new Tools( $store_id, 1);
                            //不同支付方式判断
                            switch ($pay)
                            {
                                case 'wallet_pay' :
                                    //钱包
                                    $res = $auction->wallet($store_id,$user_id_0, $promise_0, $trade_no,$special_id);
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
                                            $msg_title = Lang('Account_balance_reminder');
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

                                            $message = Lang('Insufficient_balance_of_merchant_refund_failed');
                                            echo json_encode(array('code' => 109, 'message' => $message));
                                            exit;
                                        }

                                        $message = Lang('Refund_failed');
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
                            $sql_update3 = array('is_back'=>1);
                            $r3 = Db::name('auction_promise')->where($sql_where3)->update($sql_update3);
                            if ($r3 > 0)
                            {
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
                        //扣除库存
                        $pid = $res_s[0]['goods_id'];//商品ID
                        $cid = $res_s[0]['attr_id'];//属性ID
                        $mch_id1 = Db::name('product_list')->where('id', $pid)->value('mch_id');
                        $auction->deduction_inventory($store_id,$pid,$cid,$user_id,$mch_id1,1);
                    }
                }

                // if($otype == 'VI')
                // {
                //     $array = array('store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo);
                //     PluginUtils::invokeMethod('VI', 'leave_Settlement', $array);
                // }

                if($self_lifting == '4')
                {
                    $mch = new MchPublicMethod();
                    $mch->parameter($store_id, $sNo, $z_price, 0);
                }
                $array = array('store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo);
                DeliveryHelper::leave_Settlement($array);

                Db::commit();

                $Tools = new Tools($store_id, 1);
                $Tools->orderMessage($sNo, $store_id, $user_id,$sNo_list);
                return;
            }
            else
            {
                $sql_o_where = array('store_id'=>$store_id,'sNo'=>$sNo);
                $sql_o_update = array('offset_balance'=>Db::raw('offset_balance+'.$payment_money),'z_price'=>Db::raw('z_price-'.$payment_money),'pay_time'=>$time);
                $r = Db::name('order')->where($sql_o_where)->where('z_price','>',$payment_money)->update($sql_o_update);
                if ($r == -1)
                {
                    Db::rollback();
                    ob_clean();
                    $message = Lang('operation failed');
                    return output(400, $message);
                }
                Db::commit();
                return;
            }
        }
    }

    // APP支付
    public function app_wechat($total, $title, $real_sno, $type)
    {
        $store_id = $this->store_id;
        $data = wxpay::payment_APP($real_sno,$store_id,$total,$title,$type);
        ob_clean();
        return $data;
    }

    // 公众号支付
    public function jsapi_wechat($total, $title, $real_sno, $type)
    {
        $store_id = $this->store_id;
        $user_id = $this->user_list['user_id'];
        $sNo = $this->sNo;
        $appid = $this->config_data->appid;
        $appsecret = $this->config_data->appsecret;
        $code = $this->code;

        if ($code)
        {
            $uurl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" . $appid . "&secret=" . $appsecret . "&code=" . $code . "&grant_type=authorization_code";
            $res = $this->getOpenid($uurl);
            $refresh_token = $res['refresh_token'];
            
            $uuurl = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=" . $appid . "&grant_type=refresh_token&refresh_token=" . $refresh_token;
            $res2 = $this->getOpenid($uuurl);
            
            $myfile = '../runtime/log/wechat/app.log';
            $text = "=================!!!!!!!!!!!!===============";
            $fp = fopen($myfile, "a");
            flock($fp, LOCK_EX);
            fwrite($fp, mb_convert_encoding($text, 'UTF-8', mb_detect_encoding($text)));
            fwrite($fp, mb_convert_encoding($text, 'UTF-8', mb_detect_encoding($uurl)));
            fwrite($fp, mb_convert_encoding($text, 'UTF-8', mb_detect_encoding($uuurl)));
            fwrite($fp, mb_convert_encoding($text, 'UTF-8', mb_detect_encoding(json_encode($res2))));
            fwrite($fp, mb_convert_encoding($text, 'UTF-8', mb_detect_encoding($text)));
            flock($fp, LOCK_UN);
            fclose($fp);
            $openid = $res2['openid'];
        }
        else
        {
            ob_clean();
            $message = Lang('pay.5');
            return output(400, $message);
        }

        $myfile = '../runtime/log/wechat/app.log';
        $text = "\r\n【code！！！】" . $store_id . $type . "\r\n\r\n";
        $fp = fopen($myfile, "a");
        flock($fp, LOCK_EX);
        fwrite($fp, mb_convert_encoding($text, 'UTF-8', mb_detect_encoding($text)));
        flock($fp, LOCK_UN);
        fclose($fp);
        $data = wxpay::payment_JSAPI($real_sno, $total, $openid, $appid, $store_id, $type, $title);
        ob_clean();
        return $data;
    }

    // H5支付
    public function H5_wechat($total, $title, $real_sno, $type)
    {
		$store_id = $this->store_id;
        $config = LKTConfigInfo::getPayConfig($store_id,$type);
        $ret = array();
        if(empty($config))
        {
            $ret['code']= 500;
            $ret['message']= '未配置微信H5支付';
            return output($ret['code'],$ret['message']);
        }

        $ret1 = wxpay::payment_H5($real_sno,$store_id,$type, $total, $title);
        if(empty($ret1))
        {
            $ret['code']= 500;
            $ret['message']= '操作失败';
            $ret['data']= null;
            return output($ret['code'],$ret['message']);
        }
        else
        {
            $ret['code']= 200;
            $ret['message']= '操作成功';
            $ret['data']= array("url"=>$ret1['mweb_url'],"pay_type"=>'H5_wechat',"mweb_url"=>$ret1['mweb_url'],
                    "prepayid"=>$ret1['prepayid'],
                    "referer"=>parse_url($config['notify_url'])['scheme']."://".parse_url($config['notify_url'])['host']);
        }
        ob_clean();
        // $this->output($ret['code'],$ret['message'],$ret['data']);
        return $ret['data'];
    }

    // 小程序支付
    public function mini_wechat($total, $title, $real_sno, $type)
    {
        $store_id = $this->store_id;
        $user_id = $this->user_list['user_id'];
        $sNo = $this->sNo;
        $appid = $this->config_data->appid;
        $appsecret = $this->config_data->appsecret;
        $code = $this->code;

        if ($code)
        {
            $code_open = Tools::code_open($appid, $appsecret, $code); // 微信id
            if (!isset($code_open['openid']))
            {
                ob_clean();
                return output(400, $code_open);
            }
            $openid = $code_open['openid'];
        }
        else
        {
            ob_clean();
            $message = Lang('pay.5');
            return output(400, $message);
        }
        $data = wxpay::payment_JSAPI($real_sno, $total, $openid, $appid, $store_id, $type, $title);
        ob_clean();
        return $data;
        // $this->output(200,'',$data);
    }

    // 支付宝app支付
    public function alipay($total, $title, $real_sno, $type)
    {
        $store_id = $this->store_id;
        $user_id = $this->user_list['user_id'];
        $sNo = $this->sNo;
        $appid = $this->config_data->appid;
        $log = new LaiKeLogUtils();
        $log->log('alipay/aliminipay.log', "支付宝app支付！");
        
        $data = TestImage::load($real_sno, $total, $title, $store_id, $type);
        
		$log->log('alipay/aliminipay.log', "【app调用支付】最后执行日期：" . date('Y-m-d H:i:s') . "\n" . json_encode($data) . "\n\n");
        return $data;
    }

    // 支付宝web支付
    public function alipay_mobile($total, $title, $real_sno, $type)
    {
        $store_id = $this->store_id;
        $user_id = $this->user_list['user_id'];
        $sNo = $this->sNo;
        $appid = $this->config_data->appid;
        $remarks = $this->remarks;
        
        $data = TestImage::mobile_web($real_sno, $total, $title, $store_id, $type,$remarks);
        return $data;
    }

    // 支付宝小程序支付
    public function alipay_minipay($total, $title, $real_sno, $type)
    {
        $store_id = $this->store_id;
        $sNo = $this->sNo;
        $appid = $this->config_data->appid;
        $alimp_authcode = addslashes(trim($this->request->post('alimp_authcode'))); // 阿里授权code

        $data = TestImage::loadMPAlipay($real_sno, $total, $title, $appid, $store_id, $type, $alimp_authcode);
        $tno = $this->trimall($data->trade_no);
        ob_clean();
        return array('tno'=>"s" . $tno);
    }

    // 头条小程序之支付宝app支付
    public function tt_alipay($total, $title, $real_sno, $type)
    {
        $store_id = $this->store_id;
        $config = LKTConfigInfo::getPayConfig($store_id, $type);

        $log = new LaiKeLogUtils();

        if (empty($config))
        {
            $log->log('pay/tt_alipay.log',__METHOD__ . '->' . __LINE__ . " 头条小程序支付暂未配置，无法调起支付！");
            ob_clean();
            return output(502,"头条小程序支付暂未配置，无法调起支付！");
        }
        $ttAppid = $config['ttAppid'];
        $ttAppSecret = $config['ttAppSecret'];
        $ttshid = $config['ttshid'];
        $ttpayappid = $config['ttpayappid'];
        $ttpaysecret = $config['ttpaysecret'];
        $ttzfbnotifycburl = $config['notify_url'];

        $riskIp = '120.76.189.152';
        $riskIp = '127.0.0.1';
        $valid_time = "7200";

        $tt_authcode = addslashes(trim($this->request->post('tt_authcode')));//授权code
        //1.获取openid
        $tt_openid = TTUtils::getTTOpenId($ttAppid, $ttAppSecret, $tt_authcode);

        //2:请求头条
        $result = TTUtils::ttCreatOrder($total, $title, $real_sno, $tt_openid, $ttshid, $valid_time, $ttzfbnotifycburl, $riskIp, $ttpayappid, $ttpaysecret);
        $log->log('pay/tt_alipay.log',__METHOD__ . '->' . __LINE__ . " 头条响应结果:" . $result);

        $ttResultObj = json_decode($result);
        $ttResponseObj = $ttResultObj->response;
        $ttPayCode = $ttResponseObj->code;
        if ($ttPayCode != '10000')
        {
            $log->log('pay/tt_alipay.log',__METHOD__ . '->' . __LINE__ . " 支付失败:" . $result);
            ob_clean();
            return output(502,$ttResultObj->msg);
        }
        $tttradeno = $ttResponseObj->trade_no;
        $log->log('pay/tt_alipay.log',__METHOD__ . '->' . __LINE__ . " 头条响应订单号码:" . $tttradeno);

        //3:支付宝信息
        $zfbAPPurl = $this->preAlipay($total, $title, $real_sno, "alipay");

        $log->log('pay/tt_alipay.log',__METHOD__ . '->' . __LINE__ . " 支付宝响应头条支付结果:" . $zfbAPPurl);

        //4:组织结果给前端
        $responseToFrontData = TTUtils::getTTPayCondition($total, $ttpayappid, $tttradeno, $ttshid, $tt_openid, $zfbAPPurl, $ttpaysecret);
        ob_clean();
        $message = Lang('pay.6');
        return output(200,$message, $responseToFrontData);
    }

    // 百度小程序支付
    public function baidu_pay($total, $title, $real_sno, $type)
    {
        $store_id = $this->store_id;
        $config = LKTConfigInfo::getPayConfig($store_id, $type);
        $log = new LaiKeLogUtils();

        if (empty($config))
        {
            $log->log('pay/baidu_pay.log',__METHOD__ . '->' . __LINE__ . " 百度小程序支付暂未配置，无法调起支付！");
            ob_clean();
            $message = Lang('pay.7');
            return output(400,$message);
        }

        $requestParamsArr['dealId'] = $config['dealId'];
        $requestParamsArr['appKey'] = $config['appkey'];
        $requestParamsArr['totalAmount'] = intval(floatval($total) * 100);// 精确到分
        $requestParamsArr['tpOrderId'] = $real_sno;
        // $requestParamsArr['bizInfo'] = '';

        $rsaPrivateKeyStr = $config['rsaPrivateKey'];
        $rsaPublicKeyStr = $config['rsaPublicKey'];

        /**
         * 第一部分：生成签名
         */
        $rsaSign = NuomiRsaSign::genSignWithRsa($requestParamsArr, $rsaPrivateKeyStr);
        $requestParamsArr['sign'] = $rsaSign;
        $log->log('pay/baidu_pay.log',__METHOD__ . '->' . __LINE__ . " 百度支付签名获取：" . $rsaSign);

        $requestParamsArr['rsaSign'] = $rsaSign;
        $requestParamsArr['dealTitle'] = $title;
        $requestParamsArr['signFieldsRange'] = 1;// 对appKey+dealId+tpOrderId+totalAmount进行RSA加密后的签名，防止订单被伪造

        /**
         * 第二部分：组织结果给前端
         */
        ob_clean();
        $message = Lang('pay.6');
        return output(200,$message,$requestParamsArr);
    }

    public function strFilter($str)
    {
        $str = str_replace('`', '', $str);
        $str = str_replace('·', '', $str);
        $str = str_replace('~', '', $str);
        $str = str_replace('!', '', $str);
        $str = str_replace('！', '', $str);
        $str = str_replace('@', '', $str);
        $str = str_replace('#', '', $str);
        $str = str_replace('$', '', $str);
        $str = str_replace('￥', '', $str);
        $str = str_replace('%', '', $str);
        $str = str_replace('^', '', $str);
        $str = str_replace('……', '', $str);
        $str = str_replace('&', '', $str);
        $str = str_replace('*', '', $str);
        $str = str_replace('(', '', $str);
        $str = str_replace(')', '', $str);
        $str = str_replace('（', '', $str);
        $str = str_replace('）', '', $str);
        $str = str_replace('-', '', $str);
        $str = str_replace('_', '', $str);
        $str = str_replace('——', '', $str);
        $str = str_replace('+', '', $str);
        $str = str_replace('=', '', $str);
        $str = str_replace('|', '', $str);
        $str = str_replace('\\', '', $str);
        $str = str_replace('[', '', $str);
        $str = str_replace(']', '', $str);
        $str = str_replace('【', '', $str);
        $str = str_replace('】', '', $str);
        $str = str_replace('{', '', $str);
        $str = str_replace('}', '', $str);
        $str = str_replace(';', '', $str);
        $str = str_replace('；', '', $str);
        $str = str_replace(':', '', $str);
        $str = str_replace('：', '', $str);
        $str = str_replace('\'', '', $str);
        $str = str_replace('"', '', $str);
        $str = str_replace('“', '', $str);
        $str = str_replace('”', '', $str);
        $str = str_replace(',', '', $str);
        $str = str_replace('，', '', $str);
        $str = str_replace('<', '', $str);
        $str = str_replace('>', '', $str);
        $str = str_replace('《', '', $str);
        $str = str_replace('》', '', $str);
        $str = str_replace('.', '', $str);
        $str = str_replace('。', '', $str);
        $str = str_replace('/', '', $str);
        $str = str_replace('、', '', $str);
        $str = str_replace('?', '', $str);
        $str = str_replace('？', '', $str);
        return trim($str);
    }

    /**
     *  作用：通过curl向微信提交code，以获取openid
     */
    public function getOpenid($url)
    {
        //初始化curl
        $ch = curl_init();
        //设置超时
        curl_setopt($ch, CURLOPT_TIMEOUT, 30);
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, FALSE);
        curl_setopt($ch, CURLOPT_HEADER, FALSE);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
        //运行curl，结果以jason形式返回
        $res = curl_exec($ch);
        curl_close($ch);
        //取出openid
        $data = json_decode($res, true);

        return $data;
    }

    public function trimall($str)
    {
        $qian = array(" ", "　", "\t", "\n", "\r");
        return str_replace($qian, '', $str);
    }

    // 生成订单号
    public function order_number($type, $text = 'sNo')
    {
        if ($type == 'PS')
        {
            $pay = 'PS';
        }
        else
        {
            $pay = 'GM';
        }
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

    // paypal支付
    public function paypal($total, $title, $real_sno, $type)
    {
        $store_id = $this->store_id;
        $user = $this->user;
        $access_id = $user['access_id'];
        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $log = new LaiKeLogUtils();

        $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>$currency_id));
        $currency = $userCurrency[0]['currency_code'];

        $total = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$total));

        $config = LKTConfigInfo::getPayConfig($store_id, $type);
        if (empty($config))
        {
            $log->log('pay/baidu_pay.log',__METHOD__ . '->' . __LINE__ . " 百度小程序支付暂未配置，无法调起支付！");
            ob_clean();
            $message = Lang('pay.7');
            return output(400,$message);
        }

        $client_id = $config['client_id'];
        $client_secret = $config['client_secret'];
        // $notify_url = $config['notify_url'];

        $sql = "select * from lkt_config where store_id = '$store_id'";
        $r = Db::query($sql);
        if (!empty($r[0]['H5_domain']))
        {
            $H5_domain = $r[0]['H5_domain'];
        }

        $order_types = substr($real_sno, 0, 2);
        if($order_types == 'CZ')
        {
            $cancel_url = $H5_domain."/#/pagesB/myWallet/recharge?cide=1";
            $return_url = $H5_domain."/#/pagesB/myWallet/rechargeSuccess?mylei=1&type=paypal&_type=paypal&real_sno=".$real_sno."&total=".$total;
        }
        elseif($order_types == 'DJ')
        {
            $cancel_url = $H5_domain."/#/pagesB/myWallet/recharge?cide=1";
            $return_url = $H5_domain."/#/pagesA/vipClub/vipClub?real_sno=".$real_sno."&total=".$total;
        }
        else
        {
            $cancel_url = $H5_domain."/#/pages/shell/shell";
            $return_url = $H5_domain."/#/pagesE/pay/PayResults";
        }

        $paypal = new PayPalService($client_id,$client_secret);
        $data = array('order_types'=>$order_types,'client_id'=>$client_id,'client_secret'=>$client_secret,'real_sno'=>$real_sno,'amount'=>$total,'currency'=>$currency,'cancel_url'=>$cancel_url,'return_url'=>$return_url);

        $order = $paypal->createOrder($data);

        ob_clean();
        return $order;
    }

    // 捕获订单的付款
    public function capture()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));

        $orderId = trim($this->request->param('orderId'));
        $sNo = trim($this->request->param('sNo')); // 订单号

        $config = LKTConfigInfo::getPayConfig($store_id, 'paypal');
        $client_id = $config['client_id'];
        $client_secret = $config['client_secret'];

        $order_types = substr($sNo, 0, 2);
        $pay = "";
        if($order_types == 'CZ' || $order_types == 'DJ')
        {
            $trade_no = $sNo;
            $sql = "select data from lkt_order_data where trade_no = '$sNo' and status = 0";
            $r = Db::query($sql);
            if($r)
            {
                $data_1 = unserialize($r[0]['data']);
                $paypal_id = $data_1['paypal_id'];
            }
        }
        else
        {
            //判断订单是不是贝宝支付
            $sql = "select pay,paypal_id,real_sno,z_price from lkt_order where store_id = '$store_id' and sNo = '$sNo' and status = 0";
            $r = Db::query($sql);
            if($r)
            {
                $trade_no = $r[0]['real_sno'];
                $paypal_id = $r[0]['paypal_id'];
            } 
        }

        $paypal = new PayPalService($client_id,$client_secret);
        $array = array('client_id' => $client_id,'client_secret' => $client_secret,'paypal_id'=>$orderId);
        $order = $paypal->capture($array);

        $total_fee = $order['purchase_units'][0]['payments']['captures'][0]['amount']['value'];
        $capture_Id = $order['purchase_units'][0]['payments']['captures'][0]['id'];
        if($order_types == 'CZ' || $order_types == 'DJ')
        {
            $data_1['capture_id'] = $capture_Id;
            $data_2 = serialize($data_1);
            $sql_u = "update lkt_order_data set data = '$data_2' where trade_no = '$sNo' ";
            $r_u = Db::execute($sql_u);
        }
        else
        {
            $sql_u = "update lkt_order set capture_id = '$capture_Id' where store_id = '$store_id' and sNo = '$sNo' ";
            $r_u = Db::execute($sql_u);
        }

        $data['log'] = new LaiKeLogUtils();
        $data['log_name'] = "app/PayPal.log";
        $data['total'] = $total_fee;
        $data['trade_no'] = $trade_no;

        PluginUtils::invokeMethod($order_types,'paycb',$data);

        ob_clean();
        $message = Lang('Success');
        return output(200,$message,$order);
    }

    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/pay.log",$Log_content);
        return;
    }
}

?>