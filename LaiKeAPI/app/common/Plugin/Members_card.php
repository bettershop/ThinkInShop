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
use app\common\LKTConfigInfo;

use app\admin\model\OrderDataModel;
use app\admin\model\MemberConfigModel;
use app\admin\model\ConfigureModel;

class Members_card
{
    /**
     * 回调参数校验
     * @param mixed ...$context
     * @return mixed|void
     */
    public function toCheck(&...$context)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("common/members_card.log",__METHOD__ . ":" . __LINE__ . "开始！");
        $lktlog->log("common/members_card.log",__METHOD__ . ":" . __LINE__ . json_encode($context));
        $trade_no = $context[0][0];

        $r_mc = OrderDataModel::where(['trade_no'=>$trade_no])->field('data,pay_type')->select()->toArray();
        LaiKeLogUtils::log('common/zfjc.log', '支付单号：' . $trade_no);
        if (!$r_mc)
        {
            $lktlog->log("common/members_card.log",__METHOD__ . ":" . __LINE__ . "会员订单回调失败！支付订单号：" . $trade_no . '没有查询到订单信息！');
            ob_clean();
            echo 'error';
            exit;
        }
        $info = json_decode($r_mc[0]['data'],true);

        $store_id = $info['storeId'];
        $pay_type = $r_mc[0]['pay_type'];
        if ($pay_type == 'tt_alipay')
        {
            $pay_type = 'alipay';
        }
        $config = LKTConfigInfo::getPayConfig($store_id, $pay_type);
        if (empty($config))
        {
            $lktlog->log("common/members_card.log",__METHOD__ . ":" . __LINE__ . "会员订单执行日期：" . date('Y-m-d H:i:s') . "支付暂未配置 商城ID：".$store_id." ，支付类型：".$pay_type." ，无法调起支付！");
            return 'file';
        }

        return $config;
    }

    /**
     * 支付回调
     * @inheritDoc
     */
    public function paycb(&...$context)
    {   
        $lktlog = new LaiKeLogUtils();
        $total = $context[0]['total'];
        $trade_no = $context[0]['trade_no'];
        $sNo = $context[0]['trade_no'];
        $time = date("Y-m-d H:i:s");
        
        if (empty($trade_no) || $total <= 0)
        {
            $lktlog->log("common/members_card.log",__METHOD__ . ":" . __LINE__ . "会员订单回调失败信息：订单：".$trade_no." 支付金额：".$total);
            ob_clean();
            echo 'error';
            exit;
        }
        $r_mc = OrderDataModel::where(['order_type'=>'DJ','trade_no'=>$trade_no])->field('data')->select()->toArray();
        if($r_mc)
        {   
            Db::startTrans();
            $info = json_decode($r_mc[0]['data'],true);

            $price_mc = $info['amount'];
            $type_mc = $info['memberType'];
            $start_time_mc = $info['startTime'];
            $end_time_mc = $info['endTime'];
            $coupon_id_mc = $info['couponId'];
            $store_id = $info['storeId'];
            $user_id = $info['userId'];

            $sql_update_mc = array('grade_m'=>$type_mc,'is_box'=>1,'grade_add'=>$start_time_mc,'grade_end'=>$end_time_mc,'grade'=>1,'is_out'=>0);
            $sql_where_mc = array('store_id'=>$store_id,'user_id'=>$user_id);
            $r_mc0 = Db::name('user')->where($sql_where_mc)->update($sql_update_mc);
            if($r_mc0 <= 0)
            {
                //回滚删除已经创建的订单
                Db::rollback();
                ob_clean();
                $message = Lang('operation ailed');
                return output(400, $message);
            }
            $points = 0;
            $r_config_mc = MemberConfigModel::where('store_id',$store_id)->field('open_config,bonus_points_open,bonus_points_config')->select()->toArray();
            if($r_config_mc)
            {
                $open_config_mc = json_decode($r_config_mc[0]['open_config'],true);
                $bonus_points_open_mc = $r_config_mc[0]['bonus_points_open'];
                $bonus_points_config_mc = json_decode($r_config_mc[0]['bonus_points_config'],true);

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
                    $sql_3_mc = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$points,'record'=>$event,'sign_time'=>$time,'type'=>11,'recovery'=>0);
                    $r_3_mc = Db::name('sign_record')->insert($sql_3_mc);
                    if($r_3_mc < 0)
                    {
                        Db::rollback();
                        ob_clean();
                        $message = Lang('operation ailed');
                        return output(400, $message);
                    }
                // }
            }
            
            if ($coupon_id_mc)
            {
                if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
                {
                    $coupon = new CouponPublicMethod();
                    $r_coupon0 = $coupon->update_coupon($store_id, $user_id, $coupon_id_mc,2);
                    if ($r_coupon0 == 2)
                    {
                        //回滚删除已经创建的订单
                        Db::rollback();
                        ob_clean();
                        $message = Lang('nomal_order.4');
                        echo json_encode(array('code' => ERROR_CODE_XDSB, 'message' => $message, 'data'=>array('line' => __LINE__)));
                        exit;
                    }
                    $r_coupon1 = $coupon->coupon_sno($store_id, $user_id, $coupon_id_mc,$sNo,'add');
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
                    $coupon->couponLog($coupon_id, $coupon_Log_content);
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
                $message = Lang('operation ailed');
                return output(400, $message);
            }

            // 根据订单类型、用户user_id、订单号，查询其它订单
            $sql_0 = "select id from lkt_order_data where order_type = 'DJ' and status = 1 and data like '%$user_id%' and trade_no != '$sNo' ";
            $r_0 = Db::query($sql_0);
            if($r_0)
            { // 续费
                $msg_title = Lang("member.13");
                $msg_content = Lang("member.14");
            }
            else
            { // 开通
                $msg_title = Lang("member.11");
                $msg_content = Lang("member.12");
            }

            $pusher = new LaikePushTools();
            $pusher->pushMessage($user_id,$msg_title, $msg_content, $store_id, '');
            
            $lktlog->log("common/members_card.log",__METHOD__ . ":" . __LINE__ . '完成');
            Db::commit();
        }
        else
        {
            $lktlog->log("common/members_card.log",__METHOD__ . ":" . __LINE__ . "【会员订单不存在】：订单号为：".$trade_no);
            ob_clean();
            echo 'error';
            exit;
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
        $sNo = $action->sNo;
        $payment_money = $action->payment_money;
        $remarks = $action->remarks;
        $real_sno = $sNo;
        //余额充值 和 会员等级充值
        $total = $payment_money;

        return array($real_sno,$total);
    }
}
