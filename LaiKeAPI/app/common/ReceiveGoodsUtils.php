<?php

namespace app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\Commission;
use app\common\Plugin\MchPublicMethod;
use app\common\Plugin\IntegralPublicMethod;
use app\common\Plugin\Auction;
use app\common\Tools;
use app\common\PC_Tools;

use app\admin\model\OrderConfigModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\UserModel;
use app\admin\model\DistributionConfigModel;
use app\admin\model\PluginsModel;

class ReceiveGoodsUtils
{
    // 定时任务收货
    public static function timeReceive($store_id)
    {
        Db::startTrans();
        $code = true;
        $time = date('Y-m-d H:i:s');

        $r = OrderConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if ($r)
        {
            $auto_the_goods = $r[0]['auto_the_goods'];
        }
        else
        {
            $auto_the_goods = 7;
        }

        $after_discount = 0;
        $sql01 = "select d.id,d.r_sNo,d.deliver_time,d.user_id,d.store_id,d.p_id,d.p_price,d.num,d.commission,d.after_discount,d.freight 
        from lkt_order_details as d left join lkt_order as o on d.r_sNo = o.sNo
        where d.store_id = '$store_id' and d.r_status = '2' and o.otype in ('GM','FX') and date_add(d.deliver_time, interval $auto_the_goods day) < now()";
        $rew = Db::query($sql01);
        if (!empty($rew))
        {
            $ordersNo = array();
            foreach ($rew as $key => $value)
            {
                $user_id = $value['user_id'];
                $sNo = $value['r_sNo'];
                $after_discount += $value['after_discount'] + $value['freight'];

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
                $r_2 = Db::name('order')->where($sql_2)->update(['status'=>'5','arrive_time'=>$time]);
                if ($r_2 < 0)
                {
                    $code = false;
                    break;
                }
                
                $type = substr($sNo, 0, 2);
                //判断分销插件是否安装
                if($type == 'FX')
                {
                    $res_plu = PluginsModel::where(['plugin_code'=>'distribution','status'=>'1','flag'=>'0'])->select()->toArray();
                    if($res_plu)
                    {   
                        //获取分销配置
                        $res_con = DistributionConfigModel::where(['store_id'=>$store_id])->select()->toArray();
                        if($res_con && $res_con[0]['status'] == 1)
                        {
                            $sql_o = "select a.p_price,a.num,c.uplevel from lkt_order_details as a left join lkt_product_list as b on b.id = a.p_id left join lkt_distribution_goods as c on a.p_id = c.p_id and a.sid = c.s_id where a.store_id = '$store_id' and a.r_sNo = '$sNo'";
                            $res_o = Db::query($sql_o);
                            if($res_o)
                            {
                                foreach ($res_o as $key => $value) 
                                {
                                    $p_price = $value['p_price']; // 单价
                                    $num = $value['num']; // 数量
                                    $spz_price = round($p_price * $num,2);
                                    $sets = json_decode($res_con[0]['sets'],true);
                                    $c_pay = $sets['c_pay'];
                                    //分销
                                    $comm = new Commission();
                                    if ($c_pay == 2)
                                    {   
                                        $comm->uplevel($store_id, $user_id);
                                        $comm->putcomm($store_id, $sNo, $spz_price);
                                    }
                                    if($value['uplevel'] > 0 )
                                    {
                                        $comm->straight_up($store_id, $user_id,$value['uplevel']);
                                    } 
                                }
                            }
                        }  
                    }
                }
                //佣金发放  end
            }

            self::timeReceiveLog(__METHOD__ . '->' . __LINE__ . " 订单自动收货店铺结算开始");
            self::timeReceiveLog("自动动收货订单号：" . json_encode($ordersNo));
            $mch = new MchPublicMethod();
            if (count($ordersNo) > 0)
            {
                foreach ($ordersNo as $k => $val)
                {
                    self::timeReceiveLog(__METHOD__ . '->' . __LINE__ . " $val 订单自动收货店铺结算开始");

                    $res = OrderModel::where(['sNo'=>$val])->field('sNo,id,user_id,store_id,z_price,allow,mch_id,otype,grade_score,score_deduction')->select()->toArray();
                    $z_price = 0;
                    $allow = 0;
                    if (count($res) > 0)
                    {
                        if($res[0]['z_price'] != '')
                        {
                            $z_price = $res[0]['z_price'];
                        }
                        if($res[0]['score_deduction'] > 0)
                        { // 使用了积分抵扣
                            $z_price = $after_discount;
                        }
                        $allow = $res[0]['allow'];
                        $user_id = $res[0]['user_id'];
                        $otype = $res[0]['otype'];
                        $mch_id = trim($res[0]['mch_id'],',');
                        if($otype == 'ZB')
                        {
                            $z_price = $res[0]['z_price'] - $res[0]['commission'];
                        }
                        if ($code)
                        {
                            $message_6 = "订单" . $sNo . "，用户已确定收货！";
                            $message_logging_list6 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>6,'parameter'=>$sNo,'content'=>$message_6);
                            PC_Tools::add_message_logging($message_logging_list6);
                        }

                        //积分发放
                        if($otype == 'GM' && $res[0]['grade_score'] == 0)
                        {
                            //判断积分商城插件是否安装
                            $sql_in = " select * from lkt_plugins where plugin_code = 'integral' and status = 1 and flag = 0 ";
                            $res_in = Db::query($sql_in);
                            if($res_in)
                            {   
                                // 积分发放
                                $integral = new IntegralPublicMethod();
                                $integral->issueIntegral($store_id, $val, $user_id,0);
                            }
                        }
                    }
                    self::timeReceiveLog(__METHOD__ . '->' . __LINE__ . " 订单 $val 总价：$z_price allow: $allow ");
                    $mch->parameter($store_id, $val, $z_price, $allow);
                    self::timeReceiveLog(__METHOD__ . '->' . __LINE__ . " $val 订单自动收货店铺结算结束");
                }
            }
        }

        if ($code)
        { // 如果批量执行没出错则提交，否则就回滚
            Db::commit();
        }
        else
        {
            Db::rollback();
        }
    }

    //订单列表收货
    public static function listReceive($obj)
    {
        $store_id = trim(Request::param('store_id'));

        // 获取信息
        $access_id = trim(Request::post('access_id')); // 授权id
        $order_id = trim(Request::post('order_id')); // 订单id
        $r_type = trim(Request::post('r_type')); // 退货状态
        $lktlog = new LaiKeLogUtils();

        if($order_id == '')
        {
            // $action = $obj[0];
            // $order_id = $action->order_id;
            $order_id = $obj['order_id'];
        }

        $time = date('Y-m-d H:i:s');
        // 根据授权id,查询用户id
        $access = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if (empty($access))
        {
            $message = Lang('Illegal invasion');
            echo json_encode(array('code' => ERROR_CODE_FFRQ, 'message' => $message));
            exit;
        }

        Db::startTrans();
        //售后商品回寄收货
        if ($r_type == 11)
        {
            //查询订单信息
            $resz = OrderDetailsModel::where('id',$order_id)->field('sNo')->select()->toArray();
            if (!empty($resz))
            {

                $ret = array();
                $ret['code'] = 500;
                $ret['message'] = Lang('order.18');

                $sql_1 = "update lkt_order_details set r_type = 12,r_status = 5  where store_id = '$store_id' and id = '$order_id'";
                $res = $db->update($sql_1);
                $sql_1 = OrderDetailsModel::where(['store_id'=>$store_id,'id'=>$order_id])->find();
                $sql_1->r_type = 12;
                $sql_1->r_status = 5;
                $res = $sql_1->save();
                if (!$res)
                {
                    $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "确认收货失败！id:" . $order_id);
                    //失败
                    echo json_encode($ret);
                    Db::rollback();
                    exit;
                }
                else
                {
                    //成功
                    $ret['code'] = 200;
                    $ret['message'] = Lang('Goods_received_successfully');
                    echo json_encode($ret);
                    Db::commit();
                    exit;
                }

                $otype = $resz[0]['otype'];
                $sNo = $resz[0]['sNo'];
                if ($otype == 'pt' || $otype == 'PT')
                {
                    $sql_2 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->find();
                    $sql_2->status = 5;
                    $sql_2->save();
                }
            }

        }

        $user_id = $access[0]['user_id'];
        $r = OrderModel::where(['store_id'=>$store_id,'id'=>$order_id,'user_id'=>$user_id])
                    ->field('sNo,z_price,otype,allow,mch_id,grade_score,score_deduction')
                    ->select()
                    ->toArray();
        if (!$r || empty($r))
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => ERROR_CODE_CSCW, 'message' => $message));
            exit();
        }
        $sNo = $r[0]['sNo'];
        //积分
        $allow = $r[0]['allow'];
        $z_price = $r[0]['z_price'];
        $otype = $r[0]['otype'];
        $score_deduction = $r[0]['score_deduction'];
        $mch_id = trim($r[0]['mch_id'],',');

        $type = substr($sNo, 0, 2);//获取订单号前两位字母（类型）

        $rr = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo,'user_id'=>$user_id])
                            ->field('id,r_status,p_price,num,p_id,sid,arrive_time,commission,after_discount,freight')
                            ->select()
                            ->toArray();
        if (!$rr || empty($rr))
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => ERROR_CODE_CSCW, 'message' => $message));
            exit();
        }
        $arrive_time = $rr[0]['arrive_time'];
        $after_discount = 0;

        foreach ($rr as $k => $v)
        {
            $id = $v['id'];
            $moneyy = floatval($v['p_price']) * intval($v['num']);

            if ($v['r_status'] == 2)
            {
                $after_discount += $v['after_discount'] + $v['freight'];
                $sql_1 = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo,'id'=>$id])->find();
                $sql_1->r_status = 5;
                $sql_1->arrive_time = $time;
                $r_1 = $sql_1->save();
                //判断分销插件是否安装
                if($type == 'FX')
                {
                    //获取分销配置
                    $res_con = DistributionConfigModel::where('store_id',$store_id)->select()->toArray();
                    if($res_con && $res_con[0]['status'] == 1)
                    {
                        $sql_o = "select a.p_price,a.num,c.uplevel,d.z_price from lkt_order_details as a left join lkt_product_list as b on b.id = a.p_id left join lkt_distribution_goods as c on a.p_id = c.p_id and a.sid = c.s_id left join lkt_order as d on a.r_sNo = d.sNo where a.store_id = '$store_id' and a.r_sNo = '$sNo'";
                        $res_o = Db::query($sql_o);
                        if($res_o)
                        {
                            foreach ($res_o as $key => $value) {
                                $p_price = $value['p_price'];//单价
                                $num = $value['num'];//数量
                                $z_price = $value['z_price'];//订单金额
                                $sets = json_decode($res_con[0]['sets'],true);
                                $c_pay = $sets['c_pay'];
                                //分销
                                $comm = new Commission();
                                if ($c_pay == 2)
                                {   
                                    $comm->uplevel($store_id, $user_id);
                                    $comm->putcomm($store_id, $sNo, $z_price);
                                }
                                if($value['uplevel'] > 0 )
                                {
                                    $comm->straight_up($store_id, $user_id,$value['uplevel']);
                                } 
                            }
                        }
                    } 
                }
            }
            else if ($v['r_status'] == 4)
            {
                $sql_1 = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo,'id'=>$id])->find();
                $sql_1->r_status = 5;
                $sql_1->r_type = 8;
                $sql_1->r_content = '已收货';
                $r_1 = $sql_1->save();
                if (!$r_1)
                {
                    $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改订单信息失败！id:" . $id);
                }
            }
        }

        $sql_2 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->find();
        $sql_2->status = 5;
        $sql_2->arrive_time = $time;
        $r_2 = $sql_2->save();
        if (!$r_2)
        {
            Db::rollback();
            $message = Lang('Busy network');
            echo json_encode(array('code' => ERROR_CODE_WLFMQSHZS, 'message' => $message, 'data'=>array('err' => $sNo)));
            exit();
        }

        //积分发放
        if($otype == 'GM' && $r[0]['grade_score'] == 0)
        {
            //判断积分商城插件是否安装
            $sql_in = " select * from lkt_plugins where plugin_code = 'integral' and status = 1 and flag = 0 ";
            $res_in = Db::query($sql_in);
            if($res_in)
            {   
                // 积分发放
                $integral = new IntegralPublicMethod();
                $integral->issueIntegral($store_id, $sNo, $user_id,0);
            }
        }

        if($otype == 'ZB')
        {
            $z_price = $z_price - $rr[0]['commission'];
        }

        $coupon_list = '';
        $mch = new MchPublicMethod();
        if ($otype != 'vipzs')
        {
            // if($arrive_time == '' || $arrive_time == NULL || $arrive_time == '0000-00-00 00:00:00')
            // { // 已收货
            // $mch->parameter($store_id, $sNo, $z_price, $allow);
            // }
            if($otype == 'JP')
            {
                $auction = new Auction();
                $auction->order_settlement($store_id, $sNo);
            }
            else
            {
                if($score_deduction > 0)
                {
                    $z_price = $after_discount;
                }
                $mch->parameter($store_id, $sNo, $z_price, $allow);
            }
        }

        $message_6 = "订单" . $sNo . "，用户已确定收货！";
        $message_logging_list6 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>6,'parameter'=>$sNo,'content'=>$message_6);
        PC_Tools::add_message_logging($message_logging_list6);

        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data'=>array('coupon_list' => $coupon_list)));
        exit();
    }
    
    // 日志
    public static function timeReceiveLog($Log_content)
    {
        $time = date("Y-m-d");
        $lktlog = new LaiKeLogUtils();

        $lktlog->log("app/test/".$time.".log",$Log_content);
        return;
    }
}