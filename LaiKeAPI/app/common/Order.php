<?php

namespace app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\GETUI\LaikePushTools;
use app\common\Commission;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Plugin\Auction;
use app\common\Plugin\IntegralPublicMethod;
use app\common\DeliveryHelper;
use app\common\Plugin\MchPublicMethod;

use app\admin\model\DistributionConfigModel;
use app\admin\model\UserDistributionModel;
use app\admin\model\OrderModel;
use app\admin\model\DistributionGradeModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\OrderDataModel;
use app\admin\model\UserModel;
use app\admin\model\MchModel;
use app\admin\model\DistributionRecordModel;
use app\admin\model\PluginsModel;
use app\admin\model\PreSellRecordModel;
use app\admin\model\AuctionPromiseModel;

class Order
{   
    // 付款后修改订单状态,并修改商品库存-----计算分销
    public function up_order($data)
    {
        $file = 'log.txt';
        file_put_contents($file, '开始修改订单', FILE_APPEND);

        //1.开启事务
        Db::startTrans();
        $time = date("Y-m-d H:i:s");

        //2.数据准备
        $sNo = $data['sNo']; // 订单号
        $user_id = $data['user_id']; // 微信id
        $z_price = $data['z_price']; // 订单金额
        $trade_no = $data['trade_no']; // 微信支付单号
        $transaction_id = $data['transaction_id']; // 微信返回支付单号唯一标识
        $dividend_status = $data['dividend_status']; // 分账状态 0.不分账 1.分账
        $pay = $data['pay'];
        $store_id = $data['store_id'];
        $spz_price = $data['spz_price'];
        $self_lifting = $data['self_lifting']; // 自提 0.配送 1.自提 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
        $mobile = $data['mobile'];
        $otype = $data['otype'];
        $allow = $data['allow'];
        $drawid = $data['drawid'];
        $mch_id = trim($data['mch_id'],',');

        $offset_balance = $data['offset_balance'];

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
            if ($self_lifting == '1' || $self_lifting == '3')
            { // 自提 或 虚拟订单需要线下核销
                $sql_oo_update = array('z_price'=>Db::raw('offset_balance+'.$z_price),'status'=>2,'pay_time'=>$time,'trade_no'=>$trade_no,'transaction_id'=>$transaction_id,'dividend_status'=>$dividend_status);
            }
            else if ($self_lifting == '4')
            { // 虚拟订单无需线下核销
                $sql_oo_update = array('z_price'=>Db::raw('offset_balance+'.$z_price),'status'=>5,'pay_time'=>$time,'trade_no'=>$trade_no,'transaction_id'=>$transaction_id,'dividend_status'=>$dividend_status);
            }
            else
            { // 配送
                $sql_oo_update = array('z_price'=>Db::raw('offset_balance+'.$z_price),'status'=>1,'pay_time'=>$time,'trade_no'=>$trade_no,'transaction_id'=>$transaction_id,'dividend_status'=>$dividend_status);
            }
            $r = Db::name('order')->where($sql_oo_where)->update($sql_oo_update);
            if ($r < 1)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 更新订单状态为待发货失败！条件：' . json_encode($sql_oo_where) . "；修改参数：" . json_encode($sql_oo_update);
                $this->Log($Log_content);
                exit;
            }
        }
        else
        {
            $sNo_list[] = $sNo; 
        }

        $score_deduction = 0;
        foreach($sNo_list as $k => $v)
        {
            $r2 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$v,'user_id'=>$user_id])->select()->toArray();
            $self_lifting = $r2[0]['self_lifting']; // 自提 0.配送 1.自提 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
            $score_deduction += $r2[0]['score_deduction']; // 积分支付抵扣

            $sql_oo_where = array('store_id'=>$store_id,'sNo'=>$v);
            if ($self_lifting == '1' || $self_lifting == '3')
            { // 自提 或 虚拟订单需要线下核销
                $sql_oo_update = array('status'=>2,'pay_time'=>$time,'trade_no'=>$trade_no,'transaction_id'=>$transaction_id,'dividend_status'=>$dividend_status);
            }
            else if ($self_lifting == '4')
            { // 虚拟订单无需线下核销
                $sql_oo_update = array('status'=>5,'pay_time'=>$time,'trade_no'=>$trade_no,'transaction_id'=>$transaction_id,'dividend_status'=>$dividend_status);
            }
            else
            { // 配送
                $sql_oo_update = array('status'=>1,'pay_time'=>$time,'trade_no'=>$trade_no,'transaction_id'=>$transaction_id,'dividend_status'=>$dividend_status);
            }
            $r = Db::name('order')->where($sql_oo_where)->update($sql_oo_update);
            if ($r < 1)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 更新订单状态为待发货失败！条件：' . json_encode($sql_oo_where) . "；修改参数：" . json_encode($sql_oo_update);
                $this->Log($Log_content);
                exit;
            }

            $sql_od_where = array('store_id'=>$store_id,'r_sNo'=>$v);
            if ($self_lifting == '1' || $self_lifting == '3')
            { // 自提 或 虚拟订单需要线下核销
                $sql_od_update = array('r_status'=>2);
            }
            else if ($self_lifting == '4')
            { // 虚拟订单无需线下核销
                $sql_od_update = array('r_status'=>5);
            }
            else
            { // 配送
                $sql_od_update = array('r_status'=>1);
            }
            $r = Db::name('order_details')->where($sql_od_where)->update($sql_od_update);
            if ($r < 0)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 更新订单详情状态为待发货失败！条件：' . json_encode($sql_od_where) . "；修改参数：" . json_encode($sql_od_update);
                $this->Log($Log_content);
                exit;
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
            $z_price0 = $r0[0]['z_price']; // 订单金额
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
            $sqll = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$score_deduction,'record'=>$event,'type'=>1,'sign_time'=>$time,'recovery'=>0);
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
            //是否付款后发放积分
            $sql_give = " select * from lkt_integral_config where store_id = '$store_id' and give_status = 1 and mch_id = '$mch_id' ";
            $res_give = Db::query($sql_give);
            if($res_give)
            {
                //积分发放
                $integral = new IntegralPublicMethod();
                $integral->issueIntegral($store_id, $sNo, $user_id); 
            }
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
            $sqll = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$scor,'record'=>$event,'sign_time'=>$now,'type'=>8,'recovery'=>0,'sNo'=>$sNo,'frozen_time'=>date("Y-m-d H:i:s",strtotime("+$ams_time day")));
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
            if(!$r_user)
            { // 回滚删除已经创建的订单
                Db::rollback();
                file_put_contents($file, '更新积分失败SQL:' . $sql_user_update, FILE_APPEND);
                exit;
            }

            // 添加操作记录
            $event = $user_id . '使用了' . $allow . '积分';
            $sql_sign_record = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$allow,'record'=>$event,'type'=>1,'sign_time'=>$time,'recovery'=>0);
            $r_sign_record = Db::name('sign_record')->insert($sql_sign_record);
            if ($r_sign_record < 1)
            {
                Db::rollback();
                ob_clean();
                file_put_contents($file, '添加记录失败SQL:' . $sql_sign_record, FILE_APPEND);
                exit;
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
                            file_put_contents($file, '更新开团状态为拼团成功SQL:' . $sql_pt2, FILE_APPEND);
                            exit;
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
            $sql_s = "select a.session_id,d.id,a.goods_id,a.attr_id,d.user_id,c.mch_id,c.commission,d.promise,d.trade_no from lkt_auction_product a LEFT JOIN lkt_auction_session b on a.session_id=b.id LEFT JOIN lkt_auction_special c on b.special_id=c.id LEFT JOIN lkt_auction_promise d on d.special_id = c.id where c.store_id = '$store_id' and a.user_id = '$user_id' and d.user_id = '$user_id' and a.sNo = '$sNo' and d.is_pay = 1 ";
            $res_s = Db::query($sql_s);
            if($res_s)
            {
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
                    $trade_no = $res_s[0]['trade_no']; // 支付单号

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

        file_put_contents($file, '更新订单状态为待发货结束:' . $sNo, FILE_APPEND);

        //下单的时候已经对库存进行了增减操作

        // $sql_id = "select * from lkt_order where sNo = '$order_id' ";
        // $r_id = $db->select($sql_id);
        // $id = $r_id['0']->id; // 订单id
        // $this->store_id = $r_id['0']->store_id;
        // $otype = $r_id[0]->otype; //订单类型
        // $mobile = $r_id[0]->mobile; //电话
        // $Tools = new Tools($db, $this->store_id);
    }

    //充值成功金额增加
    public function cz($data, $cmoney, $trade_no)
    {
        //开启事务
        Db::startTrans();

        $user_id = $data['user_id'];
        $store_id = $data['store_id'];
        $remarks = $data['remarks'];
        
        $this->Log('user_id：'. $user_id);
        $this->Log('商城ID：'. $store_id);
        $this->Log('支付金额：'. $cmoney);
        $this->Log('订单号：'. $trade_no);

        $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>1,'id'=>0));
        $currency_symbol = $userCurrency[0]['currency_symbol'];
        $exchange_rate = $userCurrency[0]['exchange_rate'];
        $currency_code = $userCurrency[0]['currency_code'];

        $user_money = 0;
        $sql_u = "select money from lkt_user where store_id = '$store_id' and user_id = '$user_id' ";
        $r_u = Db::query($sql_u);
        if($r_u)
        {
            $user_money = $r_u[0]['money'];
        }
        $pay_type = '';
        $type_name = '';
        $sql_d = "select pay_type from lkt_order_data where trade_no = '$trade_no' ";
        $r_d = Db::query($sql_d);
        if($r_d)
        {
            $pay_type = $r_d[0]['pay_type'];

            $sql_p = "select name from lkt_payment where class_name = '$pay_type' ";
            $r_p = Db::query($sql_p);
            $type_name = $r_p[0]['name'];
        }

        // 根据微信id,修改用户余额
        $r = Db::name('user')->where(array('store_id'=>$store_id,'user_id'=>$user_id))->update(array('money'=>Db::raw('money+'.$cmoney)));
        $this->Log('修改用户余额：'. $r);
        //修改订单临时信息表状态
        $res_o = Db::name('order_data')->where(array('trade_no'=>$trade_no))->update(array('status'=>1));
        $this->Log('修改订单临时信息表状态：'. $res_o);

        $array = array('store_id'=>$store_id,'money'=>$cmoney,'user_money'=>$user_money,'type'=>1,'money_type'=>1,'money_type_name'=>1,'record_notes'=>$remarks,'type_name'=>$type_name,'s_no'=>$trade_no,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'','currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code);
        $details_id = PC_Tools::add_Balance_details($array);

        $event = $user_id . '充值了' . $cmoney . '余额';
        $sqll = "insert into lkt_record (store_id,user_id,money,oldmoney,event,type,details_id) values ('$store_id','$user_id','$cmoney','$cmoney','$event',1,'$details_id')";
        $this->Log('sql：'. $sqll);
        $rr = Db::execute($sqll);
        $this->Log('添加：'. $rr);

        $msg_title = Lang('user.54');
        $msg_content = "您充值的" . $cmoney . "余额已到账，快去购物吧！";

        /**充值到账通知*/
        $pusher = new LaikePushTools();
        $pusher->pushMessage($user_id,$msg_title, $msg_content, $store_id, '');

        Db::commit();
        return $r;
    }

    // 日志
    public function Log($Log_content)
    {
        $time = date("Y-m-d");
        $lktlog = new LaiKeLogUtils();

        $lktlog->log("app/common/commission.log",$Log_content);
        return;
    }
}
