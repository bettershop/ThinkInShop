<?php
namespace app\common\Plugin;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\GETUI\LaikePushTools;
use app\common\Plugin\MchPublicMethod;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Jurisdiction;
use app\common\ProductClass;
use app\common\ProductBrand;
use app\common\Product;
use app\common\ServerPath;
use app\common\Order;
use app\common\LKTConfigInfo;

use app\admin\model\IntegralConfigModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\UserModel;
use app\admin\model\SignRecordModel;
use app\admin\model\ScoreOverModel;
use app\admin\model\MchModel;
use app\admin\model\IntegralGoodsModel;
use app\admin\model\OrderModel;
use app\admin\model\ConfigureModel;
use app\admin\model\AdminModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\ProductListModel;

class IntegralPublicMethod
{
    // 获取插件状态
    public function is_Plugin($store_id)
    {
        $r0 = IntegralConfigModel::where('store_id', $store_id)->field('status')->select()->toArray();
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
            $log->log_result($log_name, "积分订单回调失败信息: \n 支付订单号：$trade_no 没有查询到订单信息 \r\n");
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
            $log->log_result($log_name, "积分订单执行日期：" . date('Y-m-d H:i:s') . "\n支付暂未配置 商城ID：$store_id ，支付类型：$pay_type ，无法调起支付！\r\n");
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
            $log->log_result($log_name, "积分订单回调失败信息: \n 订单：$trade_no 支付金额：$total \r\n");
            ob_clean();
            echo 'error';
            exit;
        }

        $dividend_status = 0;
        $res = cache('Divide_accounts_list_'.$trade_no);
        if($res == 'Y')
        {
            $dividend_status = 1;
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
            $log->log_result($log_name, "【积分订单付款金额有误】:\n 应付金额为$z_price \n");
        }
        $sql = "select * from lkt_order where sNo='$trade_no'";
        $r = Db::query($sql);
        if ($r)
        {
            $status = $r[0]['status'];
            $r[0]['transaction_id'] = $transaction_id;
            $r[0]['dividend_status'] = $dividend_status;
            if ($status < 1)
            {
                $order = new Order;
                $order->up_order((array)$r[0]);
                $log->log_result($log_name, "【积分订单data】:\n" . json_encode((array)$r[0]) . "\n");
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

    // 发放积分
    public function issueIntegral($store_id, $val, $user_id,$source = 1)
    {   
        $lktlog = new LaiKeLogUtils();
        //获取订单信息
        $r = OrderModel::where(['store_id'=>$store_id,'sNo'=>$val])->select()->toArray();
        $z_price = $r[0]['z_price'];
        $otype = $r[0]['otype'];
        $mch_id = trim($r[0]['mch_id'],',');
        Db::startTrans();
        // 积分发放
        $score = 0;
        $release_time = 0;
        if($otype == 'GM' && $r[0]['grade_score'] == 0)
        {
            // 判断积分商城插件是否安装
            $sql_in = " select * from lkt_plugins where store_id='$store_id' and plugin_code = 'integral' and flag = 0 ";
            $res_in = Db::query($sql_in);
            if($res_in)
            {   
                $shop_id = PC_Tools::SelfOperatedStore($store_id);

                $sql_c = "select * from lkt_integral_config where store_id='$store_id' and mch_id = '$shop_id'";
                $res_c = Db::query($sql_c);
                if($res_c)
                {
                    $proportion_of_gifts = $res_c[0]['proportion']; // 赠送比例 为自营店配置的比例
                    
                    $release_time = $res_c[0]['give_status']; // 发放时间 0.收货后 1.付款后
                    $ams_time = $res_c[0]['ams_time'];
                    $score = floor($z_price * $proportion_of_gifts / 100);
                }
            }
            if($score > 0)
            {
                $sign_record = Db::name('sign_record')->where(['store_id'=> $store_id,'user_id'=>$user_id,'sign_score'=>$score,'type'=>13,'sNo'=>$val])->field('id')->select()->toArray();
                if(!$sign_record)
                {
                    if($release_time == 1 && $source == 1)
                    { // 付款后 并且 确认付款后
                        $sign_type = 8; // 8:会员购物积分
                        $sql_score_where = array('store_id'=>$store_id,'user_id'=>$user_id);
                        $sql_score_update = array('score'=>Db::raw('score+'.$score));
                        $r_score = Db::name('user')->where($sql_score_where)->update($sql_score_update);
                        if ($r_score == -1)
                        { // 回滚删除已经创建的订单
                            Db::rollback();
                            ob_clean();
                            $lktlog->log("common/IntegralPublicMethod.log",__METHOD__ . ":" . __LINE__ . "修改积分记录失败！user_id:" . $user_id);
                        }

                        $event = $user_id . '购物获得' . $score . '积分';
                        $sqll = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$score,'record'=>$event,'sign_time'=>date("Y-m-d H:i:s"),'type'=>$sign_type,'recovery'=>0,'sNo'=>$val);
                        $rr = Db::name('sign_record')->insert($sqll);
                        if ($rr < 1)
                        {
                            Db::rollback();
                            ob_clean();
                            $lktlog->log("common/IntegralPublicMethod.log",__METHOD__ . ":" . __LINE__ . "修改积分记录失败！user_id:" . $user_id);
                        }
                    }
                    else if($release_time == 0 && $source == 0)
                    { // 收货后 并且 确认收货后
                        $sign_type = 13; // 13:冻结积分
                        $sql_score_where = array('store_id'=>$store_id,'user_id'=>$user_id);
                        $sql_score_update = array('lock_score'=>Db::raw('lock_score+'.$score),'score'=>Db::raw('score+'.$score));
                        $r_score = Db::name('user')->where($sql_score_where)->update($sql_score_update);
                        if ($r_score == -1)
                        { // 回滚删除已经创建的订单
                            Db::rollback();
                            ob_clean();
                            $lktlog->log("common/IntegralPublicMethod.log",__METHOD__ . ":" . __LINE__ . "修改积分记录失败！user_id:" . $user_id);
                        }

                        $event = $user_id . '购物获得' . $score . '积分';
                        $sqll = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$score,'record'=>$event,'sign_time'=>date("Y-m-d H:i:s"),'type'=>$sign_type,'recovery'=>0,'sNo'=>$val,'frozen_time'=>date("Y-m-d H:i:s",strtotime("+$ams_time day")));
                        $rr = Db::name('sign_record')->insert($sqll);
                        if ($rr < 1)
                        {
                            Db::rollback();
                            ob_clean();
                            $lktlog->log("common/IntegralPublicMethod.log",__METHOD__ . ":" . __LINE__ . "修改积分记录失败！user_id:" . $user_id);
                        }
                    }
                }
            }
        }
        Db::commit();
    }

    //定时任务
    public function dotask($params)
    {   
        $store_id = $params->store_id;
        $this->issue($store_id);//发放收货后冻结积分
        $this->exceed($store_id);//过期积分
        
        $this->order_failure($store_id);// 删除过期订单
        $this->ok_Order($store_id);//自动收货
        $this->auto_good_comment($store_id);//自动评价
        $this->order_settlement($store_id);//商家订单结算
        $this->Message_notification($store_id); // 消息通知
    }

    // 消息通知
    public function Message_notification($store_id)
    {
        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        // 查询积分过期配置
        $res = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('overdue_time')->select()->toArray();
        $days = $res ? $res[0]['overdue_time'] : 0;
        $end_time = date("Y-m-d H:i:s", strtotime("-$days seconds")); // 过期结束时间
        $start_time = date("Y-m-d H:i:s", strtotime("-1 day",strtotime($end_time))); // 过期时间前一天

        $sql0 = "select * from lkt_sign_record where store_id = '$store_id' and type in (0,2,4,6,8,9,12,14) and recovery = 0 and sign_time >= '$start_time' and sign_time < '$end_time' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k => $v)
            {
                $user_id = $v['user_id'];
                $sign_score = $v['sign_score'];
                $sign_time = $v['sign_time'];
                $sign_time1 = date("Y年m月d日", strtotime("+$days seconds",strtotime($sign_time))); // 过期时间前一天

                if($sign_score > 0)
                {
                    // 10.发送消息给用户
                    $msg_title = "您有积分即将过期，请尽快使用";
                    $msg_content = "您好，您有" . $sign_score . "积分将于" . $sign_time1 . "过期，请尽快使用！";
                    // 11.给用户发送消息
                    $pusher = new LaikePushTools();
                    $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, '');
                }
            }
        }
    }

    //发放收货后冻结积分
    public function issue($store_id)
    {   
        $lktlog = new LaiKeLogUtils();
        $time = date('Y-m-d H:i:s');
        // 根据商城ID、冻结积分，最后冻结时间，查询积分记录
        $res = SignRecordModel::where(['store_id'=>$store_id,'type'=>13,'recovery'=>0])->where('frozen_time','<',$time)->select()->toArray();
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $sNo = $value['sNo'];
                // 判断订单是否进入售后
                $res_r = ReturnOrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])
                                        ->where('r_type','not in','2,5,8,10')
                                        ->select()
                                        ->toArray();
                if(empty($res_r))
                {
                    Db::startTrans();
                    $score = $value['sign_score'];
                    $id = $value['id'];
                    $user_id = $value['user_id'];

                    $sql2 = "select * from lkt_sign_record where store_id = '$store_id' and user_id = '$user_id' and sNo = '$sNo' and type = 14 and recovery = 0 ";
                    $r2 = Db::query($sql2);
                    if(!$r2)
                    {
                        // 更新积分
                        $sql0 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->where('lock_score','>=',$score)->find();
                        if($sql0)
                        {
                            $sql0->lock_score = Db::raw('lock_score - '.$score);
                            $res0 = $sql0->save();
                            if(!$res0)
                            {
                                Db::rollback();
                                $lktlog->log("common/IntegralPublicMethod.log",__METHOD__ . ":" . __LINE__ . "发放冻结积分失败！user_id:" . $user_id.'-'.$score);
                            }

                            //增加回退记录
                            $scor = $value['sign_score'];
                            $event = $user_id . '退还冻结' . $scor . '积分';
                            $sqll = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$scor,'record'=>$event,'sign_time'=>date("Y-m-d H:i:s"),'type'=>14,'recovery'=>0,'sNo'=>$sNo);
                            $rr = Db::name('sign_record')->insert($sqll);
                            if ($rr < 1)
                            {
                                Db::rollback();
                                $lktlog->log("common/IntegralPublicMethod.log",__METHOD__ . ":" . __LINE__ . "修改积分记录失败！user_id:" . $userId);
                            }
                        }
                    }
                    Db::commit();
                }
            }
        }
    }

    //积分过期
    public function exceed($store_id)
    {
        $lktlog = new LaiKeLogUtils();
        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        // 查询积分过期配置
        $res = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('overdue_time')->select()->toArray();
        $days = $res ? $res[0]['overdue_time']/(60*60*24) : 0;

        if ($days > 0)
        {
            $end_ = date("Y-m-d H:i:s", strtotime("-$days day")); //有效日期
            // 查询积分大于0并且未被冻结的用户
            $users = UserModel::where(['store_id'=>$store_id,'is_lock'=>0])->where('score','>',0)->field('user_id,score,lock_score')->select()->toArray();
            foreach ($users as $k => $v)
            {

                $user_id = $v['user_id']; // 用户id
                $score = $v['score']; // 原积分
                $lock_score = $v['lock_score'];//冻结积分
                $old_score = $v['score'] - $v['lock_score'];//可用积分


                // 1.查询出上次计算过期积分是什么时候
                $r1 = ScoreOverModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->order('add_time','desc')->limit(0,1)->select()->toArray();
                $c1 = '';
                $last_pay = !empty($r1) ? $r1[0]['last_pay'] : 0; // 已计算花费积分
                if (!empty($r1))
                {
                    $c1 = " and sign_time>'" . $r1[0]['count_time'] . "' ";
                }

                // 2.查询出上次计算到这次计算过期的积分
                $sql = "select sum(sign_score) as sum from lkt_sign_record where store_id='$store_id' and user_id='$user_id' and sign_time<'$end_' $c1 and type in (0,2,4,6,8,9,12,14)";
                $r2 = Db::query($sql);
                $end_sum = $r2 ? intval($r2[0]['sum']) : 0;

                // 3.查询累计花费积分
                $r3 = SignRecordModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->where('type','in','1,3,5,7')->sum('sign_score');
                $pay_sum = $r3 ? intval($r3) : 0;

                //4.待计算花费积分 = 累计花费积分-已计算花费积分
                $can_pay = intval($pay_sum) - intval($last_pay);

                // 5.如果过期积分大于零 进行后续操作
                if ($end_sum > 0)
                {
                    // 6.可过期积分 = 过期积分-待计算花费积分  最小为0
                    $can_end = intval($end_sum) - intval($can_pay);
                    $real_end = $can_end > 0 ? $can_end : $end_sum; // 当可过期积分小于或等于0时  可过期积分为过期积分
                    if ($can_end > 0)
                    { // 当可过期积分大于0时 说明过期积分可以抵消待计算花费积分
                        $new_last_pay = intval($last_pay) + intval($can_pay); // 新的已计算花费积分 = 已计算花费积分+待计算花费积分
                    }
                    else
                    { // 反之说明说明过期积分无法抵消待计算花费积分 只能抵消过期积分相应的花费积分
                        $new_last_pay = intval($last_pay) + intval($end_sum); // 新的已计算花费积分 = 已计算花费积分+过期积分
                    }

                    Db::startTrans();

                    if ($real_end > 0)
                    {
                        $real_end = $real_end > $old_score ? $old_score : $real_end;
                        // 7.用户积分减少
                        $sql = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->where('score','>=',$real_end)->find();
                        $sql->score = Db::raw('score-'.$real_end);
                        $r = $sql->save();
                        if (!$r)
                        {
                            $lktlog->log("common/IntegralPublicMethod.log","\r\n会员【" . $user_id . "】积分过期处理失败！过期积分：" . $real_end . "\r\n");
                            Db::rollback();
                            continue;
                        }

                        // 8.插入积分记录
                        $event = "会员" . $user_id . "有" . $real_end . "积分已过期！";
                        $sql0 =  new SignRecordModel();
                        $sql0->store_id = $store_id;
                        $sql0->user_id = $user_id;
                        $sql0->sign_score = $real_end;
                        $sql0->record = $event;
                        $sql0->sign_time = date("Y-m-d H:i:s");
                        $sql0->type = 10;
                        $sql0->save();
                        $res = $sql0->id;
                        if ($res < 1)
                        {
                            $lktlog->log("common/IntegralPublicMethod.log","\r\n会员【" . $user_id . "】积分过期记录失败！过期积分：" . $real_end . "\r\n");
                            Db::rollback();
                            continue;
                        }
                    }
                    // 9.插入积分过期记录
                    $now_score = intval($score) - intval($real_end);
                    $sql1 = new ScoreOverModel();
                    $sql1->store_id = $store_id;
                    $sql1->user_id = $user_id;
                    $sql1->old_score = (int)$score;
                    $sql1->now_score = $now_score;
                    $sql1->last_pay = $new_last_pay;
                    $sql1->count_time = $end_;
                    $sql1->add_time = date("Y-m-d H:i:s");
                    $sql1->save();
                    $r1 = $sql1->id;
                    if ($r1 < 1)
                    {
                        $lktlog->log("common/IntegralPublicMethod.log","\r\n会员【" . $user_id . "】积分过期记录失败！过期积分：" . $real_end . "\r\n");
                        Db::rollback();
                        continue;
                    }
                    DB::commit();

                    // 10.发送消息给用户
                    $msg_title = "您有积分已经过期！";
                    $msg_content = "您有" . $real_end . "积分已经过期了！";
                    // 11.给用户发送消息
                    $pusher = new LaikePushTools();
                    $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, '');
                    // 12.插入记录
                    $lktlog->log("common/IntegralPublicMethod.log","\r\n会员【" . $user_id . "】积分过期处理成功！过期积分：" . $end_sum . "\r\n");
                }
            }
        }
    }

    //删除过期订单
    public function order_failure($store_id)
    {   
        $lktlog = new LaiKeLogUtils();

        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        //获取积分商城配置
        $res_c = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('order_failure')->select()->toArray();
        $order_failure = $res_c ? $res_c[0]['order_failure'] : 3600; // 未付款订单保留时间

        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'IN' and o.store_id = '$store_id' and o.status = 0 and o.recycle = 0";
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
                    $r0 = OrderModel::where(['store_id'=>$store_id,'status'=>0])->where('add_time','<',$time01)->where('mch_id',','.$mch_id.',')->whereIn('otype','IN')->select()->toArray();
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
                            $lktlog->log("common/IntegralPublicMethod.log","\r\n订单更新为关闭状态！参数：" . json_encode($sql_where_o) . "\r\n");

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

                                    $sql_where3 = array('store_id'=>$store_id,'id'=>$v1['pro_id']);
                                    $r3 = Db::name('product_list')->where($sql_where3)->update(['num'=>Db::raw('num+'.$v1['num'])]);

                                    $sql_where4 = array('id'=>$v1['sid']);
                                    $r4 = Db::name('configure')->where($sql_where4)->update(['num'=>Db::raw('num+'.$v1['num'])]);

                                    $sql_where5 = array('id'=>$v1['p_id']);
                                    $r4 = Db::name('integral_goods')->where($sql_where5)->update(['num'=>Db::raw('num+'.$v1['num'])]);
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
        //获取积分商城配置
        $res_c = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('auto_the_goods')->select()->toArray();
        $auto_the_goods = $res_c ? $res_c[0]['auto_the_goods']/(3600*24) : 7; // 未付款订单保留时间

        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'IN' and o.store_id = '$store_id' and o.status = 2 and o.recycle = 0";
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
                where d.store_id = '$store_id' and d.r_status = '2' and o.otype = 'IN' and o.mch_id = ',".$mch_id.",' and date_add(d.deliver_time, interval $auto_the_goods day) < now()";
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
        //获取积分商城配置
        $res_c = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('auto_good_comment_day,auto_good_comment_content')->select()->toArray();
        $auto_good_comment_day = $res_c ? $res_c[0]['auto_good_comment_day']/(3600*24) : 0; // 未付款订单保留时间
        $auto_good_comment_content = $res_c ? $res_c[0]['auto_good_comment_content']:''; // 自动好评内容

        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'IN' and o.store_id = '$store_id' and o.status = 5 and o.recycle = 0";
        $res_m = Db::query($sql_m);
        if($res_m)
        {
            foreach ($res_m as $key => $value) 
            {
                $mch_id = $value['mch_id'];
            
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
                                AND a.r_sNo like 'IN%' 
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

    // 商家订单结算
    public function order_settlement($store_id)
    {   
        $lktlog = new LaiKeLogUtils();

        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        //获取积分商城配置
        $res_c = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('order_after')->select()->toArray();
        $order_after = $res_c ? $res_c[0]['order_after']/(3600*24) : 0; // 未付款订单保留时间
        
        $time = date("Y-m-d H:i:s",strtotime("-$order_after day"));

        //获取店铺
        $sql_m = "select p.mch_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on c.pid = p.id where o.otype = 'IN' and o.store_id = '$store_id' and o.status = 5 and o.recycle = 0";
        $res_m = Db::query($sql_m);
        if($res_m)
        {   
            Db::startTrans();
            foreach ($res_m as $k => $v) 
            {
                $mch_id = $v['mch_id'];
        
                $sql_m = "select a.after_discount,a.freight,a.r_sNo,b.mch_id,a.id from lkt_order_details as a left join lkt_order as b on a.r_sNo = b.sNo where b.store_id = '$store_id' and a.r_status in (3,5) and a.arrive_time <= '$time' and a.settlement_type = 0 and b.otype = 'IN' and b.mch_id = ',".$mch_id.",' ";
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
                                    $sql_u_update = array('account_money'=>Db::raw('account_money-'.$money),'cashable_money'=>Db::raw('cashable_money+'.$money));
                                    $res_u = Db::name('mch')->where($sql_u_where)->update($sql_u_update);
                                    if($res_u < 0)
                                    {   
                                        Db::rollback();
                                        $lktlog->log("common/IntegralPublicMethod.log","\r\n订单结算到账失败！参数:" . json_encode($sql_u_where) . "\r\n");
                                    }
                                    else
                                    {
                                        $sql_where = array('store_id'=>$store_id,'id'=>$value['id']);
                                        $sql_update = array('settlement_type'=>1);
                                        $res = Db::name('order_details')->where($sql_where)->update($sql_update);
                                        if($res < 0)
                                        {   
                                            Db::rollback();
                                            $lktlog->log("common/IntegralPublicMethod.log","\r\n订单结算失败！参数:" . json_encode($sql_where) . "\r\n");
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
        $shop_id = Db::name('admin')->where(['store_id'=>$store_id,'recycle'=>0,'type'=>1])->value('shop_id');
        $is_status = Db::name('integral_config')->where(['store_id'=>$store_id,'mch_id'=>$shop_id])->value('status');
      
        return $is_status;
    }

    // 获取积分配置信息
    public function getConfigInfo($array)
    {
        $store_id = $array['store_id'];

        $mch_id = PC_Tools::SelfOperatedStore($store_id); // 获取自营店ID

        $config = array(
            'ams_time' => 0,
            'auto_good_comment_content' => '',
            'auto_good_comment_day' => 0,
            'auto_the_goods' => 0,
            'bg_img' => '',
            'content' => '',
            'deliver_remind' => 0,
            'give_status' => 0,
            'order_after' => 0,
            'order_failure' => 0,
            'overdue_time' => 0,
            'package_settings' => 0,
            'proportion' => 0,
            'same_piece' => 0,
            'status' => 0,
        );
        $r_config = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
        if($r_config)
        {
            $r_config[0]['scoreRatio'] = $r_config[0]['score_ratio'];
            $config = $r_config[0];
        }

        $data = array('config'=>$config);
        $message = Lang("Success");
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 添加/编辑配置信息
    public function addConfigInfo($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $status = $array['status']; // 插件状态 0关闭 1开启
        $proportion = $array['proportion']; // 积分比例(购物赠送积分=购物交易金额*赠送比例)
        $give_status = $array['give_status']; // 发放状态(0=收货后 1=付款后)
        $score_ratio = $array['score_ratio']; // 积分兑换比例
        $ams_time = $array['ams_time']; // 收货后多少天返回积分，至 x 失效
        $overdue_time = $array['overdue_time']; // 积分有效时间从积分获取后开始计算，至 x 失效
        $package_settings = $array['package_settings']; // 多件包邮设置 0.未开启 1.开启
        $same_piece = $array['same_piece']; // 同件n件包邮
        $auto_the_goods = $array['auto_the_goods']; // 自动收货时间
        $order_failure = $array['order_failure']; // 订单失效 (单位 秒)
        $order_after = $array['order_after']; // 订单售后时间 (单位秒)
        $deliver_remind = $array['deliver_remind']; // 提醒发货限制 间隔(单位 秒)
        $auto_good_comment_day = $array['auto_good_comment_day']; // 自动评价设置几后自动好评
        $auto_good_comment_content = $array['auto_good_comment_content']; // 自动好评内容
        $content = $array['content']; // 规则设置
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();

        if($status == 1)
        {
            if($proportion == '')
            {
                $message = Lang("integral.0");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            
            if($overdue_time == '')
            {
                $message = Lang("integral.1");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }

        if($package_settings == 1)
        {
            if($same_piece == '')
            {
                $message = Lang("integral.2");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }

        if($auto_the_goods == '')
        {
            $message = Lang("integral.3");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        if($order_failure == '')
        {
            $message = Lang("integral.4");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        if($order_after == '')
        {
            $message = Lang("integral.5");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($deliver_remind == '')
        {
            $message = Lang("integral.6");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($auto_good_comment_day == '')
        {
            $message = Lang("integral.7");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        $mch_id = PC_Tools::SelfOperatedStore($store_id); // 获取自营店ID

        $r_config = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
        if($r_config)
        {
            $sql_where = array('store_id'=>$store_id,'mch_id'=>$mch_id);
            $sql_update = array('status'=>$status,'proportion'=>$proportion,'give_status'=>$give_status,'score_ratio'=>$score_ratio,'ams_time'=>$ams_time,'overdue_time'=>$overdue_time,'package_settings'=>$package_settings,'same_piece'=>$same_piece,'auto_the_goods'=>$auto_the_goods,'order_failure'=>$order_failure,'order_after'=>$order_after,'deliver_remind'=>$deliver_remind,'auto_good_comment_day'=>$auto_good_comment_day,'auto_good_comment_content'=>$auto_good_comment_content,'content'=>$content);
            $r = Db::name('integral_config')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了积分商城插件的配置信息失败',2,$operator_source,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改积分商城设置失败！参数:'.json_encode($sql_where);
                $this->Log($Log_content);
                $message = Lang('integral.8');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了积分商城插件的配置信息',2,$operator_source,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改积分商城设置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
        else
        {
            $sql = array('store_id'=>$store_id,'status'=>$status,'proportion'=>$proportion,'give_status'=>$give_status,'score_ratio'=>$score_ratio,'ams_time'=>$ams_time,'overdue_time'=>$overdue_time,'package_settings'=>$package_settings,'same_piece'=>$same_piece,'auto_the_goods'=>$auto_the_goods,'order_failure'=>$order_failure,'order_after'=>$order_after,'deliver_remind'=>$deliver_remind,'auto_good_comment_day'=>$auto_good_comment_day,'auto_good_comment_content'=>$auto_good_comment_content,'content'=>$content,'mch_id'=>$mch_id);
            $r = Db::name('integral_config')->insertGetId($sql);
            if ($r > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了积分商城插件的配置信息',1,$operator_source,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加积分商城设置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了积分商城插件的配置信息失败',1,$operator_source,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加积分商城设置失败！参数:'.json_encode($sql);
                $this->Log($Log_content);
                $message = Lang('integral.9');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }
    }

    // 积分商品列表
    public function GetIntegralList($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id']; // 积分商品ID
        $goodsName = $array['goodsName']; // 商品编号、商品名称
        $mchName = $array['mchName']; // 店铺名称
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

        if($operator_source == 1)
        { // 管理后台
            $plugin = Db::name('plugins')->where(['store_id'=>$store_id,'plugin_code'=>'integral'])->value('status');
            $condition = " a.store_id = '$store_id' and b.is_delete = 0 and a.recycle = 0 ";
            //店铺未授权时只查自营店数据
            if ($plugin == 0)
            {
                $condition .= " and a.mch_id = '$mch_id' ";
            }
        }
        else
        {
            $condition = " a.store_id = '$store_id' and b.is_delete = 0 and a.recycle = 0 and a.mch_id = '$mch_id'";
        }

        if ($goodsName != '')
        {
            $goodsName_00 = Tools::FuzzyQueryConcatenation($goodsName);
            $condition .= " and (a.product_title like $goodsName_00 or b.goods_id = '$goodsName') ";
        }

        if($id != '')
        {
            $condition .= " and b.id = '$id' ";
        }

        if($mchName != '')
        {
            $mchName_00 = Tools::FuzzyQueryConcatenation($mchName);
            $condition .= " and exists(select 1 from lkt_mch where recovery=0 and id=a.mch_id and name like $mchName_00) ";
        }
        
        $sql0 = "select ifnull(count(b.id),0) as num from lkt_integral_goods as b left join lkt_product_list as a on b.goods_id = a.id where $condition";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        $sql = "select b.id,b.store_id,b.goods_id,b.attr_id as attrId,b.integral,b.money,b.num,b.max_num,b.sort,b.is_delete,b.add_time,b.update_time,a.imgurl,a.product_title,a.product_class,a.brand_id,a.mch_id,a.initial,b.status,a.volume,a.real_volume from lkt_integral_goods as b left join lkt_product_list as a on b.goods_id=a.id where $condition order by b.sort desc limit $start,$pagesize";
        $r = Db::query($sql);
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $pid = $v['goods_id'];
                $cid = $v['attrId'];
                $class0 = trim($v['product_class'],'-');
                $class1 = explode('-',$class0);
                $class2 = count($class1) - 1;
                $class = $class1[$class2];
                $bid = $v['brand_id'];
                $mch_id1 = $v['mch_id'];
                $v['attrNum'] = $v['num'];

                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);

                $class_array = array('store_id'=>$store_id,'cid'=>$class);
                $v['pname'] = ProductClass::GetClassName($class_array);
                
                $brand_array = array('store_id'=>$store_id,'bid'=>$bid);
                $v['brand_name'] = ProductBrand::GetBrandName($brand_array);

                $r_s = ConfigureModel::where(['id'=>$cid,'recycle'=>0])->field('*')->select()->toArray();
                if($r_s)
                {
                    $v['imgurl'] = ServerPath::getimgpath($r_s[0]['img'], $store_id);
                    $v['price'] = (float)$r_s[0]['price'];

                    $attribute_array = array('store_id'=>$store_id,'attribute'=>$r_s[0]['attribute']);
                    $v['attrName'] = Product::AttributeProcessing($attribute_array);
                }

                $v['mchName'] = '';
                $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id1])->field('name')->select()->toArray();
                if($r_mch)
                {
                    $v['mchName'] = $r_mch[0]['name'];
                }
                $v['money'] = (float)$v['money'];
                $v['sales_volume'] = (float)$v['volume'] + (float)$v['real_volume'];
                $list[] = $v;
            }
        }
        
        $data = array('num'=>$total,'list'=>$list);
        $message = Lang("Success");
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 获取商品列表
    public function GetProList($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $myClass = $array['myClass']; // 分类ID
        $myBrand = $array['myBrand']; // 品牌ID
        $proName = $array['proName']; // 商品名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据
        $operator_source = $array['operator_source']; // 操作人来源

        $list = array();
        $total = 0;
        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }        

        $where_str = " b.store_id = '$store_id' and b.status = 2 and a.recycle = 0 and b.recycle = 0 and a.num > 0 and b.mch_id = '$mch_id' and b.is_presell = 0 and b.gongyingshang = 0 and b.commodity_type = 0 ";
        if (!empty($myClass))
        {
            $where_str .= " and b.product_class like '%-$myClass-%' ";
        }

        if (!empty($myBrand))
        {
            $where_str .= " and b.brand_id = '$myBrand' ";
        }

        if (!empty($proName))
        {
            $proName_00 = Tools::FuzzyQueryConcatenation($proName);
            $where_str .= " and b.product_title like $proName_00 ";
        }

        $goods_id = '';
        $r_integral = IntegralGoodsModel::where(['store_id'=>$store_id,'is_delete'=>0])->field('goods_id,attr_id')->select()->toArray();
        if($r_integral)
        {
            foreach($r_integral as $k => $v)
            {
                if($v['attr_id'])
                {
                    $goods_id .= $v['attr_id'] . ',';
                }
            }
            $goods_id = trim($goods_id,',');

            $where_str .= " and a.id not in($goods_id) ";
        }

        $sql0 = "select ifnull(count(1),0) as total from (select a.*,b.product_title,d.name as mch_name,row_number () over (PARTITION BY a.id) AS top from lkt_configure as a left join lkt_product_list as b on a.pid = b.id left join lkt_mch as d on b.mch_id = d.id where $where_str ) as tt where tt.top<2";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql = "select tt.* from (select a.*,a.num as stockNum,b.product_class,b.brand_id,b.product_title as goodsName,c.brand_name as brandName,e.pname as className,d.name as mchName,row_number () over (PARTITION BY a.id) AS top from lkt_configure as a left join lkt_product_list as b on a.pid = b.id left join lkt_mch as d on b.mch_id = d.id left join lkt_brand_class as c on b.brand_id = c.brand_id left join lkt_product_class as e on SUBSTRING_INDEX(SUBSTRING_INDEX(b.product_class, '-', 2),'-', - 1) = e.cid where $where_str order by a.id desc) as tt where tt.top<2 limit $start,$pagesize";
        $r = Db::query($sql);
        if($r)
        {
            foreach($r as $k => $v)
            {
                $pid = $v['pid'];
                $class0 = trim($v['product_class'],'-');
                $class1 = explode('-',$class0);
                $class2 = count($class1) - 1;
                $class = $class1[$class2];
                $bid = $v['brand_id'];
                $v['imgUrl'] = ServerPath::getimgpath($v['img'], $store_id);

                $class_array = array('store_id'=>$store_id,'cid'=>$class);
                $v['className'] = ProductClass::GetClassName($class_array);
                
                $brand_array = array('store_id'=>$store_id,'bid'=>$bid);
                $v['brandName'] = ProductBrand::GetBrandName($brand_array);

                $v['price'] = (float)$v['price'];
                
                $attribute_array = array('store_id'=>$store_id,'attribute'=>$v['attribute']);
                $v['attrName'] = Product::AttributeProcessing($attribute_array);

                $v['attrId'] = $v['id'];
                $v['id'] = $pid;
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'res'=>$list);
        $message = Lang("Success");
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 添加/编辑积分商品
    public function addIntegral($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id']; // 积分商品ID
        $goodsid = $array['goodsid']; // 商品ID
        $integral = $array['integral']; // 积分
        $money = $array['money']; // 金额
        $stockNum = $array['stockNum']; // 数量
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $time = date("Y-m-d H:i:s");
        Db::startTrans();
        $attrId = 0;
        $addNum = 0;
        $addIntegralVoList = '';
        if($operator_source == 3)
        { // 移动端店铺
            $addNum = $array['addNum']; // 增加库存
            $addIntegralVoList = $array['addIntegralVoList']; // 商品信息

            if($addIntegralVoList != '')
            { // 添加
                $goods_list = json_decode($addIntegralVoList,true);
                foreach ($goods_list as $key => $value) 
                {
                    $goodsid = $value['goodsid']; // 商品ID
                    $attrId = $value['attrId']; // 属性ID
                    $integral = 0;
                    $money = 0;
                    $stockNum = $value['stockNum'];
                    if(isset($value['integral']))
                    {
                        $integral = $value['integral'];
                    }
                    if(isset($value['money']))
                    {
                        $money = $value['money'];
                    }
                    $array1 = array('goodsid'=>$goodsid,'integral'=>$integral,'attrId'=>$attrId,'money'=>$money,'stockNum'=>$stockNum);
                    $this->DataJudgment($array1);

                    $array2 = array('store_id'=>$store_id,'goodsid'=>$goodsid,'attrId'=>$attrId,'integral'=>$integral,'money'=>$money,'stockNum'=>$stockNum,'mch_id'=>$mch_id,'operator_id'=>0,'operator'=>'','operator_source'=>$operator_source);
                    $this->AddPointBasedProducts($array2);
                }
                Db::commit();
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
            else
            { // 编辑
                $attrId = $array['attrId']; // 规格
                $stockNum = (int)$stockNum + (int)$addNum;

                $array1 = array('goodsid'=>$goodsid,'integral'=>$integral,'attrId'=>$attrId,'money'=>$money,'stockNum'=>$stockNum);
                $this->DataJudgment($array1);

                $array2 = array('store_id'=>$store_id,'id'=>$id,'goodsid'=>$goodsid,'attrId'=>$attrId,'integral'=>$integral,'money'=>$money,'stockNum'=>$stockNum,'mch_id'=>$mch_id,'operator_id'=>$operator_id,'operator'=>$operator,'operator_source'=>$operator_source);
                $this->EditPointBasedProducts($array2);
                Db::commit();
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
        else
        {
            $attrId = $array['attrId']; // 规格

            $array1 = array('goodsid'=>$goodsid,'integral'=>$integral,'attrId'=>$attrId,'money'=>$money,'stockNum'=>$stockNum);
            $this->DataJudgment($array1);

            if($id != 0 && $id != '')
            {
                $array2 = array('store_id'=>$store_id,'id'=>$id,'goodsid'=>$goodsid,'attrId'=>$attrId,'integral'=>$integral,'money'=>$money,'stockNum'=>$stockNum,'mch_id'=>$mch_id,'operator_id'=>$operator_id,'operator'=>$operator,'operator_source'=>$operator_source);
                $this->EditPointBasedProducts($array2);
                Db::commit();
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
            else
            {
                $array2 = array('store_id'=>$store_id,'goodsid'=>$goodsid,'attrId'=>$attrId,'integral'=>$integral,'money'=>$money,'stockNum'=>$stockNum,'mch_id'=>$mch_id,'operator_id'=>$operator_id,'operator'=>$operator,'operator_source'=>$operator_source);
                $this->AddPointBasedProducts($array2);
                Db::commit();
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
    }

    // 添加、编辑积分商品数据判断
    public function DataJudgment($array)
    {
        $goodsid = $array['goodsid'];
        $integral = $array['integral'];
        $attrId = $array['attrId'];
        $money = $array['money'];
        $stockNum = $array['stockNum'];

        if($goodsid == '' || $goodsid == 0)
        {
            $message = Lang("integral.15");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($integral == '' || $integral == 0)
        {
            $message = Lang("integral.16");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        $r_s = ConfigureModel::where(['pid'=>$goodsid,'recycle'=>0])->field('min(price) as price')->select()->toArray();
        $price = $r_s[0]['price'];

        if($money >= $price)
        {
            $message = Lang("integral.17");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if($stockNum == '')
        {
            $message = Lang("integral.18");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        return;
    }

    // 添加积分商品
    public function AddPointBasedProducts($array)
    {
        $store_id = $array['store_id'];
        $goodsid = $array['goodsid'];
        $attrId = $array['attrId'];
        $integral = $array['integral'];
        $money = $array['money'];
        $stockNum = $array['stockNum'];
        $mch_id = $array['mch_id']; // 店铺ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        $r0 = IntegralGoodsModel::where(['store_id'=>$store_id,'is_delete'=>0])->order('sort','desc')->limit(0,1)->field('sort')->select()->toArray();
        $sort = !empty($r0) ? intval($r0[0]['sort']) + 1 : 1;

        $sql = array('store_id'=>$store_id,'goods_id'=>$goodsid,'attr_id'=>$attrId,'integral'=>$integral,'money'=>$money,'num'=>$stockNum,'max_num'=>$stockNum,'sort'=>$sort,'add_time'=>$time,'status'=>2);
        $r = Db::name('integral_goods')->insertGetId($sql);
        if ($r > 0)
        {
            // 商品库存预扣
            $sql_p = ProductListModel::where(['store_id'=>$store_id,'id'=>$goodsid])->find();
            $sql_p->num = Db::raw('num - '.$stockNum);
            $res_p = $sql_p->save();
            if(!$res_p)
            {
                Db::rollcack();
                $Log_content = __METHOD__ . '->' . __LINE__ . '修改库存失败！'.$goodsid;
                $this->Log($Log_content);
                $message = Lang("Modification failed");
                echo json_encode(array('code' => ERROR_CODE_XGSB,'message' => $message));
                exit;
            }
            // 规格库存预扣
            $sql_c = ConfigureModel::find($attrId);
            $sql_c->num = Db::raw('num - '.$stockNum);
            $res_c = $sql_c->save();
            if(!$res_c)
            {
                Db::rollcack();
                $Log_content = __METHOD__ . '->' . __LINE__ . '修改规格库存失败！'.$attrId;
                $this->Log($Log_content);
                $message = Lang("Modification failed");
                echo json_encode(array('code' => ERROR_CODE_XGSB,'message' => $message));
                exit;
            }
            
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加积分商品成功！';
            $this->Log($Log_content);
            $Jurisdiction->admin_record($store_id, $operator, '添加了积分商品ID：'.$goodsid,1,$operator_source,$mch_id,$operator_id);
        }
        else
        {
            Db::rollcack();
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加积分商品失败！参数:'.json_encode($sql);
            $this->Log($Log_content);
            $Jurisdiction->admin_record($store_id, $operator, '添加了积分商品失败',1,$operator_source,$mch_id,$operator_id);
            $message = Lang('integral.20');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        return;
    }

    // 编辑积分商品
    public function EditPointBasedProducts($array)
    {
        $store_id = $array['store_id'];
        $id = $array['id'];
        $goodsid = $array['goodsid'];
        $attrId = $array['attrId'];
        $integral = $array['integral'];
        $money = $array['money'];
        $stockNum = $array['stockNum'];
        $mch_id = $array['mch_id']; // 店铺ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $sql_update = array('integral'=>$integral,'attr_id'=>$attrId,'money'=>$money,'num'=>$stockNum,'max_num'=>$stockNum,'update_time'=>$time);
        $r = Db::name('integral_goods')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改积分商品失败！参数:'.json_encode($sql_where);
            $this->Log($Log_content);
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '修改了积分商品ID：'.$goodsid.'失败',2,$operator_source,$mch_id,$operator_id);
            $message = Lang('integral.19');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改积分商品成功！';
            $this->Log($Log_content);
            $Jurisdiction->admin_record($store_id, $operator, '修改了积分商品ID：'.$goodsid,2,$operator_source,$mch_id,$operator_id);
        }
        return;
    }

    // 添加库存
    public function addStock($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $proId = $array['proId']; // 积分商品ID
        $num = $array['num']; // 数量
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        $time = date("Y-m-d H:i:s");
        
        $r0 = IntegralGoodsModel::where(['id'=>$proId])->field('goods_id,attr_id,num,max_num')->select()->toArray();
        if($r0)
        {
            $num0 = $r0[0]['num'];
            $max_num0 = $r0[0]['max_num'];
            $goodsid = $r0[0]['goods_id'];
            $attrId = $r0[0]['attr_id'];
        }
        else
        {
            $message = Lang("Parameter error");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        if(is_numeric($num))
        {
            if($num != intval($num))
            {
                $message = Lang("integral.21");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            $r01 = ConfigureModel::where(['id'=>$attrId])->field('num')->select()->toArray();
            if($r01 && $num > $r01[0]['num'])
            {
                $message = Lang("integral.22");
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }
        else
        {
            $message = Lang("integral.23");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        
        $sql_where = array('store_id'=>$store_id,'id'=>$proId);
        $sql_update = array('num'=>Db::raw('num+'.$num),'max_num'=>Db::raw('max_num+'.$num),'update_time'=>$time);
        $r = Db::name('integral_goods')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改积分商品失败！参数:'.json_encode($sql_where);
            $this->Log($Log_content);
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '添加了积分商品ID：'.$proId.','.$num.' 个库存失败',2,$operator_source,$mch_id,$operator_id);
            $message = Lang('integral.24');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            //商品库存预扣
            $sql_p = ProductListModel::where(['store_id'=>$store_id,'id'=>$goodsid])->find();
            $sql_p->num = Db::raw('num - '.$num);
            $res_p = $sql_p->save();
            if(!$res_p)
            {
                Db::rollcack();
                $Jurisdiction->admin_record($store_id, $operator, '添加了积分商品ID：'.$proId.','.$num.' 个库存失败',2,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . '修改库存失败！'.$goodsid;
                $this->Log($Log_content);
                $message = Lang("Modification failed");
                echo json_encode(array('code' => ERROR_CODE_XGSB,'message' => $message));
                exit;
            }
            //规格库存预扣
            $sql_c = ConfigureModel::find($attrId);
            $sql_c->num = Db::raw('num - '.$num);
            $res_c = $sql_c->save();
            if(!$res_c)
            {
                Db::rollcack();
                $Jurisdiction->admin_record($store_id, $operator, '添加了积分商品ID：'.$proId.','.$num.' 个库存失败',2,$operator_source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . '修改规格库存失败！'.$attrId;
                $this->Log($Log_content);
                $message = Lang("Modification failed");
                echo json_encode(array('code' => ERROR_CODE_XGSB,'message' => $message));
                exit;
            }
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改积分商品成功！';
            $this->Log($Log_content);
            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator, '添加了积分商品ID：'.$proId.','.$num.' 个库存',2,$operator_source,$mch_id,$operator_id);
            $message = Lang('Success');
            echo json_encode(array('code' => 200,'message' => $message));
            exit;
        }
    }

    // 置顶
    public function top($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id']; // 积分商品ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        
        $r0 = IntegralGoodsModel::where(['store_id'=>$store_id,'is_delete'=>0])->where('id','<>',$id)->order('sort','desc')->limit(0,1)->field('sort')->select()->toArray();
        $sort = !empty($r0) ? intval($r0[0]['sort']) + 1 : 1;

        $r0 = IntegralGoodsModel::where(['store_id'=>$store_id,'id'=>$id])->select()->toArray();
        if($r0)
        {
            $time = date("Y-m-d H:i:s");
        
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('sort'=>$sort,'update_time'=>$time);
            $r = Db::name('integral_goods')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 置顶积分商品失败！参数:'.json_encode($sql_where);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '置顶了积分商品ID：'.$id.' 失败',2,$operator_source,$mch_id,$operator_id);
                $message = Lang('integral.25');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 置顶积分商品成功！';
                $this->Log($Log_content);
                Db::commit();
                $Jurisdiction->admin_record($store_id, $operator, '置顶了积分商品ID：'.$id,2,$operator_source,$mch_id,$operator_id);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
        else
        {
            $message = Lang("Parameter error");
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
    }

    // 上下架
    public function onAndOff($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $id = $array['id']; // 积分商品ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        $time = date("Y-m-d H:i:s");

        $old_status = Db::name('integral_goods')->where('id', $id)->value('status');
        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $sql_update = array('status'=>2,'update_time'=>$time);
        //下架的时候改上架，其它都上架
        if($old_status == 2)
        {
            $sql_update = array('status'=>3,'update_time'=>$time);
        }     
        $r = Db::name('integral_goods')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 积分商品上下架失败！参数:'.json_encode($sql_where);
            $this->Log($Log_content);
            $Jurisdiction->admin_record($store_id, $operator, '上/下架了积分商品ID：'.$id.'失败',2,$operator_source,$mch_id,$operator_id);
            $message = Lang('integral.26');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 积分商品上下架成功！';
            $this->Log($Log_content);
            $Jurisdiction->admin_record($store_id, $operator, '上/下架了积分商品ID：'.$id,2,$operator_source,$mch_id,$operator_id);
            $message = Lang('Success');
            echo json_encode(array('code' => 200,'message' => $message));
            exit;
        }
    }

    // 删除积分商品
    public function del($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $ids = $array['ids']; // 积分商品ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $operator_source = $array['operator_source']; // 操作人来源

        $Jurisdiction = new Jurisdiction();
        $time = date("Y-m-d H:i:s");

        $shop_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID

        // 获取店铺积分商城配置
        $res_c = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->field('order_after')->select()->toArray();
        $order_after = $res_c ? $res_c[0]['order_after']/(3600*24) : 0; // 订单售后时间
        $order_after = date("Y-m-d H:i:s",strtotime("-$order_after day"));
        
        $id_list = explode(',',$ids);
        foreach($id_list as $k => $v)
        {
            $r01 = OrderDetailsModel::where(['p_id'=>$v])->where('r_sNo','like', 'IN%')->where('r_status','<>', 7)->select()->toArray();

            $sql_s = IntegralGoodsModel::where(['store_id'=>$store_id,'id'=>$v,'is_delete'=>0])->select()->toArray();
            if($sql_s)
            {
                $p_num = $sql_s[0]['num'];
                $goodsId = $sql_s[0]['goods_id'];
                $attr_id = $sql_s[0]['attr_id'];

                $sql_m = "select a.after_discount,a.freight,b.mch_id,a.id from lkt_order_details as a left join lkt_order as b on a.r_sNo = b.sNo where b.store_id = '$store_id' and a.r_status in (3,5) and a.arrive_time <= '$order_after' and a.settlement_type = 0 and b.otype = 'IN' and a.p_id = '$v' ";
                $res_m = Db::query($sql_m);
                if($r01 && $res_m)
                {
                    $message = Lang("integral.27");
                    echo json_encode(array('code' => 109,'message' => $message));
                    exit;
                }
                
                //商品库存回退
                $sql_p = ProductListModel::where(['store_id'=>$store_id,'id'=>$goodsId])->find();
                $sql_p->num = Db::raw('num + '.$p_num);
                $res_p = $sql_p->save();
                if(!$res_p)
                {
                    Db::rollcack();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '商品库存回退失败！'.$goodsId;
                    $this->Log($Log_content);
                    $message = Lang("operation failed");
                    echo json_encode(array('code' => ERROR_CODE_CZSB,'message' => $message));
                    exit;
                }
                //查询商品信息
                $sql_c = ConfigureModel::find($attr_id);
                $sql_c->num = Db::raw('num + '.$p_num);
                $res_c = $sql_c->save();
                if(!$res_c)
                {
                    Db::rollcack();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '库存回退失败！'.$attr_id;
                    $this->Log($Log_content);
                    $message = Lang("operation failed");
                    echo json_encode(array('code' => ERROR_CODE_CZSB,'message' => $message));
                    exit;
                }

                $sql_where = array('store_id'=>$store_id,'id'=>$v);
                $sql_update = array('is_delete'=>1,'update_time'=>$time);
                $r = Db::name('integral_goods')->where($sql_where)->update($sql_update);
                if ($r == -1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除积分商品失败！参数:'.json_encode($sql_where);
                    $this->Log($Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '删除了积分商品ID：'.$v.'失败',3,$operator_source,$mch_id,$operator_id);
                    $message = Lang('integral.28');
                    echo json_encode(array('code' => 109,'message' => $message));
                    exit;
                }

                $Jurisdiction->admin_record($store_id, $operator, '删除了积分商品ID：'.$v,3,$operator_source,$mch_id,$operator_id);
            }
        }
        $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除积分商品成功！';
        $this->Log($Log_content);
        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 兑换记录
    public function getRecords($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $proId = $array['proId']; // 积分商品ID
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据
        $operator_source = $array['operator_source']; // 操作人来源

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $get_pro = PC_Tools::get_pro($store_id);
        $product_class_arr = $get_pro['product_class_arr'];

        $total = 0;
        $list = array();
        
        $str = " a.goods_id,a.attr_id as attrId,a.integral,a.money,a.status,d.size as attrName,d.add_time,c.price,c.img,c.num as attrNum,c.attribute,p.product_title,p.product_class,p.initial,o.user_id,o.mobile,o.remark,o.sNo ";
        $sql0 = "select d.id from lkt_integral_goods as a left join lkt_order_details as d on a.id = d.p_id left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on a.goods_id = p.id left join lkt_order as o on d.r_sNo = o.sNo where a.store_id = '$store_id' and a.id = '$proId' and o.otype = 'IN' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = count($r0);
        }

        $sql1 = "select $str from lkt_integral_goods as a left join lkt_order_details as d on a.id = d.p_id left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on a.goods_id = p.id left join lkt_order as o on d.r_sNo = o.sNo where a.store_id = '$store_id' and a.id = '$proId' and o.otype = 'IN' order by d.add_time limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k1 => $v1)
            {
                $class = $v1['product_class'];
                $v1['imgurl'] = ServerPath::getimgpath($v1['img'],$store_id);

                // 分类名称
                $pname = array_key_exists($class, $product_class_arr) ? $product_class_arr[$class]:'顶级';
                $v1['pname'] = $pname;
                $list[] = $v1;
            }
        }

        $data = array('data'=>$list,'total'=>$total);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("common/Plugin/IntegralPublicMethod.log",$Log_content);
        return;
    }
}
