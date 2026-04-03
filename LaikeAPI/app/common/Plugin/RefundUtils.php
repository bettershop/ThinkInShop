<?php
namespace app\common\Plugin;

// require_once "../app/common/alipay/return.php";

use think\facade\Db;

use app\common\GETUI\LaikePushTools;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Plugin\Plugin;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;
use app\common\WXTopicMsgUtils;
use app\common\wxpayv2\wxpay;
use app\common\alipay0\aop\test\AlipayReturn;
use app\common\Jurisdiction;
use app\common\Plugin\LivingPublic;
use app\common\LKTConfigInfo;
use app\common\PayPalService;

use app\admin\model\ReturnGoodsModel;
use app\admin\model\ReturnRecordModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\ServiceAddressModel;
use app\admin\model\CustomerModel;
use app\admin\model\MchModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\ConfigureModel;
use app\admin\model\UserModel;
use app\admin\model\ExpressModel;
use app\admin\model\DistributionRecordModel;
use app\admin\model\SignRecordModel;
use app\admin\model\SupplierModel;
use app\admin\model\SupplierAccountLogModel;
use app\admin\model\SupplierOrderFrightModel;
use app\admin\model\RecordModel;

class RefundUtils
{
    // 退款页面（后台退款查看、移动端和PC店铺退款页面）
    public static function refund_page($obj)
    {
        $store_id = $obj->store_id;
        $id = $obj->id;

        // 根据产品id，查询产品产品信息
        $sql = "select b.id,b.pid,b.r_type,b.r_content,b.audit_time,b.re_apply_money,b.re_money,b.re_photo,b.re_time,b.re_type,b.real_money,b.sNo,b.sid,c.size,b.user_id,b.content,b.store_id,b.p_id,c.p_name,c.p_price,c.num,fig.img,c.after_discount,c.deliver_time,c.freight,lpl.id as goodsId,a.z_price,lm.id as mchId,lm.name as mchName,lm.logo,b.re_id  
        from lkt_return_order as b
        left join lkt_order_details as c on b.p_id = c.id
        left join lkt_order as a on c.r_sNo = a.sNo 
        left join lkt_configure as fig on b.sid = fig.id
        LEFT JOIN lkt_product_list as lpl on lpl.id = fig.pid
        LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id
        where b.id = '$id'";
        $r = Db::query($sql);

        $oid = $r[0]['p_id']; // 订单详情ID
        $sNo = $r[0]['sNo']; // 订单号
        $r[0]['imgurl'] = ServerPath::getimgpath($r[0]['img'], $store_id);
        $r[0]['z_price'] = round(($r[0]['p_price'] * $r[0]['num']) + $r[0]['freight'],2); // 小计
        $r[0]['total'] = round($r[0]['after_discount'] + $r[0]['freight'],2); // 合计
        $r[0]['after_discount'] = round(($r[0]['p_price'] * $r[0]['num']) - $r[0]['after_discount'],2); // 优惠金额
        $r[0]['freight'] = round($r[0]['freight'],2);
        $r[0]['p_price'] = round($r[0]['p_price'],2);
        $r[0]['rtype'] = $r[0]['r_type'];
        
        $re_photo = !empty($r[0]['re_photo']) ? unserialize($r[0]['re_photo']) : array();
        $imgs = array();
        if (!empty($re_photo))
        {
            foreach ($re_photo as $k => $v)
            {
                $imgs[] = ServerPath::getimgpath($v, $store_id);
            }
        }

        $rdata = array();
        $r_type = $r[0]['r_type'];
        if ($r_type == 3 || $r_type == 11 || $r_type == 12)
        {
            $ro = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid,'id'=>$id])->select()->toArray();
            if ($ro)
            {
                $rdata = $ro;
            }
        }

        if($r_type == 1)
        {
            if($r[0]['re_type'] == 3)
            {
                $r[0]['prompt'] = '换货中';  //同意并让用户寄回
            }
            else
            {
                $r[0]['prompt'] = '退款中';  //同意并让用户寄回
            }
        }
        else if($r_type == 2)
        {
            $r[0]['prompt'] = '退款失败';
        }
        else if($r_type == 3)
        {
            if($r[0]['re_type'] == 3)
            {
                $r[0]['prompt'] = '换货中';
            }
            else
            {
                $r[0]['prompt'] = '退款中';
            }
        }
        else if($r_type == 4)
        {
            $r[0]['prompt'] = '退款成功';
        }
        else if($r_type == 5)
        {
            if($r[0]['re_type'] == 3)
            {
                $r[0]['prompt'] = '换货失败';
            }
            else
            {
                $r[0]['prompt'] = '退款失败';
            }
        }
        else if($r_type == 8)
        {
            $r[0]['prompt'] = '退款失败';
        }
        else if($r_type == 9)
        {
            $r[0]['prompt'] = '退款成功';
        }
        else if($r_type == 10)
        {
            $r[0]['prompt'] = '换货失败';
        }
        else if($r_type == 11)
        {
            $r[0]['prompt'] = '换货中';
        }
        else if($r_type == 12)
        {
            $r[0]['prompt'] = '换货成功';
        }
        else if($r_type == 13)
        {
            $r[0]['prompt'] = '退款成功';
        }
        else
        {
            $r[0]['prompt'] = '审核中';
        }

        $record = array();
        $examineInfo = array();
        //查询售后记录
        $record_res = ReturnRecordModel::where(['p_id'=>$id,'store_id'=>$store_id,'sNo'=>$sNo])->order('id','desc')->select()->toArray();
        if (!empty($record_res))
        {   
            foreach ($record_res as $k => $v) 
            {
                $record_res[$k]['money'] = (float)$v['money'];
                if($v['r_type'] == 0)
                {
                    $examineInfo[$k]['applyTime'] = $v['re_time'];
                }
                else
                {
                    $examineInfo[$k]['applyTime'] = null;
                }
                $examineInfo[$k]['examineResultTime'] = null;
                $examineInfo[$k]['reBackNo'] = null;
                $examineInfo[$k]['reBackTime'] = null;
                $examineInfo[$k]['refuseText'] = "";
                $examineInfo[$k]['returnBackNo'] = null;
                $examineInfo[$k]['returnMoney'] = null;
                if($v['r_type'] == 0)
                {
                    $examineInfo[$k]['examineResult'] = '审核中';
                }
                elseif($v['r_type'] == 4 || $v['r_type'] == 9 || $v['r_type'] == 13)
                {
                    $examineInfo[$k]['examineResult'] = '退款成功';
                    $examineInfo[$k]['examineResultTime'] = $v['re_time'];
                    $examineInfo[$k]['returnMoney'] = $v['money'];
                }
                elseif($v['r_type'] == 2 || $v['r_type'] == 5 || $v['r_type'] == 8 || $v['r_type'] == 10)
                {
                    $examineInfo[$k]['examineResult'] = '审核拒绝';
                    $examineInfo[$k]['examineResultTime'] = $v['re_time'];
                    $examineInfo[$k]['refuseText'] = $r[0]['r_content'];
                }
                elseif($v['r_type'] == 3)
                {
                    $examineInfo[$k]['examineResult'] = '商品审核中';
                    $express_id = $v['express_id'];//快递公司编号
                    $r01 = ExpressModel::where('id',$express_id)->select()->toArray();
                    $kuaidi_name = $r01[0]['kuaidi_name'];//快递公司名字
                    $examineInfo[$k]['reBackNo'] = $v['courier_num']."(".$kuaidi_name.")";
                    $examineInfo[$k]['reBackTime'] = $v['re_time'];

                }
                elseif($v['r_type'] == 1)
                {
                    $examineInfo[$k]['examineResult'] = '审核通过';
                    $examineInfo[$k]['examineResultTime'] = $v['re_time'];
                }
                elseif($v['r_type'] == 11)
                {
                    $examineInfo[$k]['examineResult'] = '商品审核通过';
                    $examineInfo[$k]['examineResultTime'] = $v['re_time'];
                    $examineInfo[$k]['reBackTime'] = $v['re_time'];
                    $express_id = $v['express_id'];//快递公司编号
                    $r01 = ExpressModel::where('id',$express_id)->select()->toArray();
                    $kuaidi_name = $r01[0]['kuaidi_name'];//快递公司名字
                    $examineInfo[$k]['returnBackNo'] = $v['courier_num']."(".$kuaidi_name.")";

                }
            }
            $record = $record_res;
        }

        $send_info = array();
        $return_info = array();

        //查询买家回寄信息
        $r1 = ReturnGoodsModel::where(['store_id'=>$store_id,'oid'=>$oid])->select()->toArray();
        if (!empty($r1))
        {
            $send_info[] = $r1[0];

            if (count($r1) > 1)
            {
                //查询卖家退换信息
                $return_info[] = $r1[1];
            }
        }

        $data = array("rdata" => $rdata, "record" => $record, "list" => $r[0], "imgs" => $imgs, "examineInfo" => $examineInfo, 'send_info' => $send_info, 'return_info' => $return_info);
        return $data;
    }

    // 售后审核
    public static function refund($obj)
    {
        header('Access-Control-Allow-Origin: *');
        header('Content-type: text/plain');
        $store_id = $obj['store_id'];
        $admin_name = $obj['admin_name'];
        $id = $obj['id']; // 售后订单id
        $m = $obj['m']; // 退货类型
        $text = $obj['text']; // 拒绝理由
        $price = $obj['price']; // 退款金额
        $shop_id = $obj['mch_id']; // 传过来的店铺ID

        $Tools = new Tools( $store_id, 1);
        $time = date("Y-m-d H:i:s");
        $lktlog = new LaiKeLogUtils();
        $JurisdictionAction = new Jurisdiction();

        $shop_id0 = '';
        if(isset($obj['shop_id']))
        {
            $shop_id0 = $obj['shop_id'];
        }
        $operator_id = 0;
        if(isset($obj['operator_id']))
        {
            $operator_id = $obj['operator_id'];
        }
        $operator = '';
        if(isset($obj['operator']))
        {
            $operator = $obj['operator'];
        }
        $source = 1;
        if(isset($obj['source']))
        {
            $source = $obj['source'];
        }

        $is_it_manual = 1; // 是否是人工 0.系统自动 1.人工
        if($source == 100)
        { // 定时任务过来的
            $is_it_manual = 0;
        }
        
        //判断是否设置售后地址
        $r0 = ServiceAddressModel::where(['store_id'=>$store_id,'uid'=>'admin','is_default'=>1])->field('address_xq,name,tel')->select()->toArray();
        if (empty($r0))
        {
            // 给出提示设置售后地址
            $Log_content = __METHOD__ . '->' . __LINE__ . '请设置售后地址！';
            $lktlog->log("common/return.log",$Log_content);
            
            $message = Lang('return.8');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }

        $mobile = '';
        //获取电商平台管理员信息
        $res_c = CustomerModel::where(['id'=>$store_id,'recycle'=>0])->field('mobile')->select()->toArray();
        if($res_c)
        {
            $mobile = $res_c[0]['mobile'];
        }

        //开启事务
        Db::startTrans();

        /*----------- 进入订单详情把未读状态改成已读状态，已读状态的状态不变 -------*/
        $sql1 = "select m.p_name,a.readd,a.id,a.user_id,a.mch_id,m.r_sNo,a.p_sNo,a.status,a.real_sno,b.re_type,m.express_id,b.p_id,b.pid,b.sid,a.supplier_id,m.freight,a.otype,m.after_write_off_num,b.r_write_off_num,m.num,m.living_room_id from lkt_order as a ,lkt_order_details AS m,lkt_return_order as b where a.store_id = '$store_id' and a.sNo = m.r_sNo and b.id = '$id' and b.p_id = m.id";
        $r1 = Db::query($sql1);
        if(empty($r1))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '售后订单ID错误！参数:'.$id;
            $lktlog->log("common/return.log",$Log_content);
            $message = Lang('return.9');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }
        if ($r1[0]['readd'] == 0)
        {
            $id01 = $r1[0]['id'];

            $r2 = Db::name('order')->where('id', $id01)->update(['readd' => '1']);
            if ($r2 > 0)
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改订单详情已读状态成功！");
            }
            else
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改订单详情已读状态失败！");
            }

            $message_logging_list_2 = array('store_id'=>$store_id,'mch_id'=>trim($r1[0]['mch_id'],','),'parameter'=>$id,'type'=>2);
            PC_Tools::message_pop_up($message_logging_list_2);
            PC_Tools::message_read($message_logging_list_2);
        }
        $user_id = $r1[0]['user_id'];
        $order_product_name = $r1[0]['p_name']; // 商品名称
        $sNo = $r1[0]['r_sNo']; // 订单编号
        $mch_id = $r1[0]['mch_id']; // 订单所属店铺
        $mch_id = trim($mch_id,','); // 订单所属店铺
        $real_sno = $r1[0]['real_sno'];// 支付订单号
        $p_sNo = $r1[0]['p_sNo']; // 父订单号
        $re_type = $r1[0]['re_type']; // 退款类型 1:退货退款  2:退款 3:售后
        $res = 1;
        $oid = $r1[0]['p_id']; // 订单详情ID
        $pid = $r1[0]['pid'];//商品id
        $sid = $r1[0]['sid'];//属性id
        $num = $r1[0]['num']; // 商品数量
        $supplier_id = $r1[0]['supplier_id'];//供应商id
        $freight = $r1[0]['freight'];//运费
        $otype = $r1[0]['otype']; // 订单类型
        $after_write_off_num = $r1[0]['after_write_off_num']; // 虚拟商品已核销次数
        $r_write_off_num = $r1[0]['r_write_off_num']; // 虚拟商品退款核销次数
        $z_write_off_num = $after_write_off_num + $r_write_off_num; // 总核销次数
        $living_room_id = $r1[0]['living_room_id']; // 直播间ID
        if($shop_id != 0)
        { // 当不是后台操作时
            if($shop_id != $mch_id)
            { // 传过来的店铺ID 与 订单所属店铺ID 不一致
                $Log_content = __METHOD__ . '->' . __LINE__ . '订单所属店铺ID为' . $mch_id . '与传过来的店铺ID' . $shop_id . '不一致！';
                $lktlog->log("common/return.log",$Log_content);
                Db::rollback();
                $message = Lang('return.10');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }

        $mch_account_money = 0; // 店铺金额
        $mch_name = '';
        $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('account_money,name')->select()->toArray();
        if($r_mch)
        {
            $mch_account_money = $r_mch[0]['account_money']; // 店铺金额
            $mch_name = $r_mch[0]['name'];//店铺名称
        }

        // 不在退货退款状态
        // 0:审核中
        // 1:同意并让用户寄回
        // 2:拒绝(退货退款)
        // 3:用户已快递
        // 4:收到寄回商品,同意并退款
        // 5:拒绝并退回商品
        // 8:拒绝(退款)
        // 9:同意并退款
        // 10:拒绝(售后)
        // 11:同意并且寄回商品
        // 12:售后结束
        // 13:人工售後完成
        if ($m == 1 || $m == 4 || $m == 9 || $m == 13)
        {   
            if($m == 1)
            { // 同意并让用户寄回 
                if($supplier_id)
                {
                    $r_return_order = Db::name('return_order')->where('id', $id)->update(['r_type'=>$m,'real_money'=>$price,'audit_time'=>$time,'is_agree'=>1]);
                }
                else
                {
                    $r_return_order = Db::name('return_order')->where('id', $id)->update(['r_type'=>$m,'real_money'=>$price,'audit_time'=>$time]);
                } 
            }
            else
            {
                $real_money = $price;
                if($m == 4)
                { // 收到寄回商品,同意并退款
                    $r_return_order = Db::name('return_order')->where('id', $id)->update(['r_type'=>$m,'real_money'=>$real_money,'audit_time'=>$time,'is_agree'=>1]);
                }
                else
                {
                    $r_return_order = Db::name('return_order')->where('id', $id)->update(['r_type'=>$m,'real_money'=>$real_money,'audit_time'=>$time]);
                }
            }

            if ($r_return_order > 0)
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改订单详情成功！");
                if($m == 1)
                {
                    $return_record_array = array('store_id'=>$store_id,'user_id'=>$user_id,'re_type'=>$re_type,'r_type'=>$m,'sNo'=>$sNo,'money'=>0,'product_id'=>$pid,'attr_id'=>$sid,'re_time'=>$time,'p_id'=>$id,'is_it_manual'=>$is_it_manual);
                    self::add_return_record($return_record_array); // 添加售后记录
                }
            }
            else
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改订单详情失败！");
            }

            //同意退款
            if ($m == 9 || $m == 4 || $m == 13)
            {
                $sql_id = "select a.baiduId,a.orderId,a.offset_balance,m.id as oid,a.id,a.trade_no,m.num,a.sNo,a.pay,a.z_price,a.old_total,a.user_id,a.coupon_id,a.allow,a.spz_price,a.reduce_price,a.coupon_price,a.coupon_id,a.subtraction_id,a.p_sNo,m.p_name,m.p_price,a.consumer_money,a.mch_id,m.express_id,m.freight,a.real_sno,b.re_apply_money,a.old_freight,a.z_freight,m.arrive_time,m.coupon_id as coupon_id0,a.otype,a.status,m.supplier_settlement,m.supplier_id,m.mch_store_write_id,m.write_time,m.write_time_id,p.write_off_settings,p.is_appointment,b.r_write_off_num,m.r_status,a.supplier_id,m.is_addp,m.actual_total,m.after_discount
                            from lkt_order as a 
                            LEFT JOIN lkt_order_details AS m ON a.sNo = m.r_sNo 
                            left join lkt_product_list as p on m.p_id = p.id
                            right join lkt_return_order as b on b.p_id = m.id
                            where a.store_id = '$store_id' and b.id = '$id'";
                $order_res = Db::query($sql_id);
                if ($order_res)
                {
                    $pay = $order_res[0]['pay']; // 支付方式
                    $coupon_id0 = $order_res[0]['coupon_id0']; // 订单详情使用的优惠券ID
                    $o_d_id = $order_res[0]['oid']; // 订单详情ID
                    $offset_balance = $order_res[0]['offset_balance']; // 抵扣余额
                    $t1t = true;
                    $z_price = $order_res[0]['z_price'];// 订单总价
                    $old_total = $order_res[0]['old_total'];// 历史总价
                    $z_freight = $order_res[0]['z_freight'];// 订单总运费
                    $old_freight = $order_res[0]['old_freight'];// 历史总运费
                    $supplier_id = $order_res[0]['supplier_id'];// 供应商ID
                    $supplier_settlement = $order_res[0]['supplier_settlement'];//供应商结算金额
                    $mch_store_write_id = $order_res[0]['mch_store_write_id']; // 预约门店id
                    $write_time = $order_res[0]['write_time']; // 虚拟商品预约时间
                    $write_time_id = $order_res[0]['write_time_id']; // 预约时段id
                    $write_off_settings = $order_res[0]['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
                    $is_appointment = $order_res[0]['is_appointment']; // 预约时间设置 1.无需预约下单 2.需要预约下单
                    $r_write_off_num = $order_res[0]['r_write_off_num']; // 虚拟商品退款核销次数
                    $freight = $order_res[0]['freight']; // 该订单详情运费
                    $is_addp = $order_res[0]['is_addp']; // 是否加购
                    $actual_total = $order_res[0]['actual_total']; // 该详单抵扣金额
                    if($old_total == '0.00' || $old_total == 0)
                    {
                        $Retrieve_historical_amounts_array = array('store_id'=>$store_id,'p_sNo'=>$p_sNo,'z_price'=>$z_price,'z_freight'=>$z_freight);
                        $Historical_Amount = self::Retrieve_historical_amounts($Retrieve_historical_amounts_array);
                        $old_total = $Historical_Amount['old_total'];
                        $old_freight = $Historical_Amount['old_freight'];
                    }
                    $w_pay = $old_total - $offset_balance; // 其它支付所支付金额
                    
                    // 填写金额
                    if (empty($price))
                    {
                        if($otype == 'VI')
                        { // 虚拟商品 
                            $price = round($z_price / $z_write_off_num * $r_write_off_num,2); // 订单总价 / 总核销次数 * 退款核销次数
                        }
                        else
                        {
                            $price = Tools::get_order_pro_price($store_id,$o_d_id); // 获取该订单详情商品支付金额
                        }
                    }

                    // 组合支付
                    if ($offset_balance > 0)
                    {
                        $return_user_money = number_format($offset_balance / $z_price * $price, 2, ".", ""); // 抵扣余额 / 订单总价 * 退款金额
                        $t1t = false;
                        $price = number_format(($price - $return_user_money) / $z_price * $price, 2, ".", "");
                    }

                    $should_price = Tools::get_order_pro_price($store_id,$o_d_id); // 获取该订单详情商品支付金额
                    if($otype == 'FS')
                    { // 限时折扣订单
                        $max_price = $z_price;
                    }
                    else
                    {
                        $max_price = $should_price + $freight; // 获取该订单详情商品支付金额 + 该订单详情运费 = 该订单详情最大退款金额
                    }
                    if($price > $max_price)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '实退金额有误！参数:'.$price;
                        $lktlog->log("common/return.log",$Log_content);
                        $message = Lang('return.11');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }

                    $pay_array = array('store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo,'pay'=>$pay,'price'=>$price,'p_name'=>$order_product_name,'mch_name'=>$mch_name,'t1t'=>$t1t,'real_sno'=>$real_sno,'r_order_id'=>$id,'order_details_id'=>$o_d_id,'w_pay'=>$w_pay,'admin_name'=>$admin_name);
                    $msg_content = self::Refund_method($pay_array); // 退款方式

                    if($pay == 'wallet_pay')
                    {
                        $str_a = "a.store_id,a.user_id,a.otype,a.sNo,a.mch_id,a.old_total,a.z_price,a.z_freight,a.old_freight,a.p_sNo,a.offset_balance,a.supplier_id";
                        $str_b = "b.id as order_details_id,b.r_status,b.living_room_id,b.p_id,b.sid,b.num,b.p_name,b.actual_total,b.after_discount,b.freight,b.coupon_id,b.mch_store_write_id,b.write_time,b.write_time_id,b.is_addp,b.supplier_settlement,b.p_integral";
                        $str_c = "c.re_type,c.audit_status,c.content,c.r_write_off_num";
                        $str_p = "p.write_off_settings,p.is_appointment";
                        $str = $str_a . ',' . $str_b . ',' . $str_c . ',' . $str_p;

                        $sql0 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo left join lkt_product_list as p on b.p_id = p.id left join lkt_return_order as c on b.id = c.p_id where a.sNo = '$sNo' and c.id = '$id' ";
                        $r0 = Db::query($sql0);
                        $r0[0]['mch_name'] = $mch_name;
                        
                        $Refund_callback = array('trade_no'=>$real_sno,'id'=>$id,'price'=>$price,'list'=>$r0);
                        $data = self::After_sales_review_callback($Refund_callback);
                        if($data['code'] != 200)
                        {
                            $res = 0;
                        }
                        else
                        {
                            $res = 1;
                        }
                    }
                    else
                    {
                        $r_return_order = Db::name('return_order')->where('id', $id)->update(['audit_status'=>2]);

                        $res = 1;
                    }
                }
                else
                {
                    $res = 0;
                }
            }
        }
        // 11:同意并且寄回商品
        else if ($m == 11)
        {
            //售后店家寄回商品
            $express = $obj['express_id']; // 快递公司编号
            $courier_num = $obj['courier_num'];// 快递单号
            //快递添加一条数据
            $sel_user_info = "SELECT o.user_id,o.name,o.mobile,od.re_photo FROM lkt_return_order as od LEFT JOIN lkt_order as o on o.sNo = od.sNo where od.id = $id and od.store_id = $store_id";
            $user_info = Db::query($sel_user_info);
            $lxr = $user_info[0]['name'];
            $lxdh = $user_info[0]['mobile'];
            $userid = $user_info[0]['user_id'];
            $re_photo = $user_info[0]['re_photo'];

            //查询快递名称
            $express_res = ExpressModel::where(['id'=>$express])->select()->toArray();
            $kdname = $express_res[0]['kuaidi_name'];

            $sql = array('store_id'=>$store_id,'name'=>$lxr,'tel'=>$lxdh,'express'=>$kdname,'express_num'=>$courier_num,'user_id'=>$userid,'oid'=>$oid,'add_data'=>$time,'re_id'=>$id);
            $rid = Db::name('return_goods')->insertGetId($sql);
            if ($rid > 0)
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "新增售后记录成功！");
            }
            else
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "新增售后记录失败！参数:" . json_encode($sql));
            }

            $up_order_d_sql = array('r_type'=>11,'audit_time'=>$time);
            $up_order_d_sql_where = array('store_id'=>$store_id,'id'=>$id);
            $res = Db::name('return_order')->where($up_order_d_sql_where)->update($up_order_d_sql);
            if ($res > 0)
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "售后订单更新信息成功！");
            }
            else
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "售后订单更新信息失败！参数:" . json_encode($up_order_d_sql_where));
            }

            $up_order_o_sql = array('express_id'=>$express,'courier_num'=>$courier_num);
            $up_order_o_sql_where = array('store_id'=>$store_id,'id'=>$oid);
            $res_o = Db::name('order_details')->where($up_order_o_sql_where)->update($up_order_o_sql);
            if ($res_o > 0)
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "订单物流信息成功！");
            }
            else
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "售后订单更新信息失败！参数:" . json_encode($up_order_o_sql_where));
            }

            if ($res && $rid && $res_o)
            {   
                $return_record_array = array('store_id'=>$store_id,'user_id'=>$user_id,'re_type'=>$re_type,'r_type'=>$m,'sNo'=>$sNo,'money'=>0,'re_photo'=>$re_photo,'product_id'=>$pid,'attr_id'=>$sid,'express_id'=>$express,'courier_num'=>$courier_num,'re_time'=>$time,'p_id'=>$id,'is_it_manual'=>$is_it_manual);
                self::add_return_record($return_record_array); // 添加售后记录
                //执行成功
                Db::commit();
                $message = Lang('Success');
                echo json_encode(array('code' => '200', 'message' => $message));
                exit;
            }
            else
            {
                //执行失败
                Db::rollback();
                $message = Lang('operation failed');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }
        else if ($m == 16)
        {
            $up_order_d_sql = array('r_type'=>16,'audit_time'=>$time);
            $up_order_d_sql_where = array('store_id'=>$store_id,'id'=>$id);
            $res = Db::name('return_order')->where($up_order_d_sql_where)->update($up_order_d_sql);
            if ($res > 0)
            {
                $return_record_array = array('store_id'=>$store_id,'user_id'=>$user_id,'re_type'=>$re_type,'r_type'=>$m,'sNo'=>$sNo,'money'=>0,'product_id'=>$pid,'attr_id'=>$sid,'re_time'=>$time,'p_id'=>$id,'is_it_manual'=>$is_it_manual);
                self::add_return_record($return_record_array); // 添加售后记录
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "售后订单更新信息成功！");
                //执行成功
                Db::commit();
                $message = Lang('Success');
                echo json_encode(array('code' => '200', 'message' => $message));
                exit;
            }
            else
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "售后订单更新信息失败！参数:" . json_encode($up_order_d_sql_where));
                //执行失败
                Db::rollback();
                $message = Lang('operation failed');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }
        else
        {
            // 0:审核中 2:拒绝(退货退款) 3:用户已快递 5：拒绝并退回商品 8:拒绝(退款)10:拒绝(售后)
            $text = htmlentities($text);
            $sql_id = "select a.id,a.otype,a.trade_no,a.sNo,a.pay,a.z_price,a.user_id,a.source 
                        from lkt_order as a 
                        LEFT JOIN lkt_return_order AS m ON a.sNo = m.sNo 
                        where a.store_id = '$store_id' and m.id = '$id'";
            $order_res = Db::query($sql_id);
            $sNo = $order_res[0]['sNo'];
            $otype = $order_res[0]['otype'];
            $z_price = $order_res[0]['z_price'];
            $user_id = $order_res[0]['user_id'];

            // 根据订单号,修改订单详情状态------------还原子订单状态
            $sql_d = array('r_type'=>$m,'r_content'=>$text,'audit_time'=>$time);
            $sql_d_where = array('store_id'=>$store_id,'id'=>$id);
            $res = Db::name('return_order')->where($sql_d_where)->update($sql_d);
            if ($res > 0)
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "还原订单成功！");
            }
            else
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "还原订单失败！参数:" . json_encode($sql_d_where));
            }
            
            $return_record_array = array('store_id'=>$store_id,'user_id'=>$user_id,'re_type'=>$re_type,'r_type'=>$m,'sNo'=>$sNo,'money'=>0,'product_id'=>$pid,'attr_id'=>$sid,'content'=>$text,'re_time'=>$time,'p_id'=>$id,'is_it_manual'=>$is_it_manual);
            self::add_return_record($return_record_array); // 添加售后记录

            if($otype == 'VI')
            {
                $sql_d = "update lkt_order_details set write_off_num = '$r_write_off_num' where r_sNo = '$sNo' ";
                $r_d = Db::execute($sql_d);
            }
        }

        if ($res)
        {
            if (!empty($text))
            {
                $msg_title = Lang("return.5");
                $msg_content = "失败：您申请的退货/退款被拒绝!原因：" . $text;
            }
            else
            {
                $msg_title = Lang("return.6");
                $msg_content = Lang('return.7');
            }

            //发送微信小程序的推送消息
            $openid = Tools::get_openid($store_id, $user_id);
            if($openid)
            {
                $msgres = new \stdClass();
                $msgres->uid = $openid;
                $msgres->amount2 = $price;//退款金额
                $msgres->date3 = $time;//申请时间
                $msgres->sNo = $sNo;//订单号
                $msgres->p_name = $order_product_name;//商品名称
                $msgres->thing6 = $msg_content;//状态
                self::sendWXTopicMsg($msgres, $store_id);
            }

            /**退货/退款通知 /GETUI/LaikePushTools.class.php */
            $pusher = new LaikePushTools();
            $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, $admin_name);

            Db::commit();
            if ($m == 2 || $m == 5 || $m == 8 || $m == 10)
            {
                $event = '拒绝了订单详情ID：' . $id . '，的售后申请';
            }
            else
            {
                $event = '通过了订单详情ID：' . $id . '，的售后申请';
            }
            $JurisdictionAction->admin_record($store_id, $operator, $event,2,$source,$shop_id0,$operator_id);
            if($source == 100)
            {
                return;
            }
            $message = Lang('Success');
            echo json_encode(array('code' => '200', 'message' => $message));
            exit;
        }
        else
        {
            Db::rollback();

            if ($m == 2 || $m == 5 || $m == 8 || $m == 10)
            {
                $event = '拒绝了订单详情ID：' . $id . '，的售后申请失败';
            }
            else
            {
                $event = '通过了订单详情ID：' . $id . '，的售后申请失败';
            }
            $JurisdictionAction->admin_record($store_id, $operator, $event,2,$source,$shop_id0,$operator_id);
            if($source == 100)
            {
                return;
            }
            $message = Lang('operation failed');
            echo json_encode(array('code' => 400, 'message' => $message));
            exit;
        }
    }

    // 余额退款
    public static function return_user_money($store_id,$user_id, $price,$sNo,$title_name = '',$mch_name = '')
    {
        $lktlog = new LaiKeLogUtils();

        // 根据商城ID、买家用户ID，查询买家余额
        $r1 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                        ->field('money')
                        ->select()
                        ->toArray();
        $money = $r1[0]['money'];
        // 修改买家用户余额

        $res = Db::name('user')->where(['store_id'=>$store_id,'user_id'=>$user_id])->update(['money'=>Db::raw('money+' . $price)]);
        //判断是否添加成功
        if ($res > 0)
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改金额成功！");
        }
        else
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改金额失败！");
        }

        $otype = substr($sNo, 0, 2);
        if($otype == 'PR')
        {
            return 1;
        }
        
        if($otype == 'ZB')
        {
            $array = array('store_id'=>$store_id,'money'=>$price,'user_money'=>$money,'type'=>4,'money_type'=>1,'money_type_name'=>20,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>$title_name,'activity_code'=>'','mch_name'=>$mch_name,'withdrawal_fees'=>'','withdrawal_method'=>'');
        }
        else if($otype == 'PT' || $otype == 'KT')
        {
            $array = array('store_id'=>$store_id,'money'=>$price,'user_money'=>$money,'type'=>4,'money_type'=>1,'money_type_name'=>11,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>$title_name,'activity_code'=>'','mch_name'=>$mch_name,'withdrawal_fees'=>'','withdrawal_method'=>'');
        }
        else
        {
            $array = array('store_id'=>$store_id,'money'=>$price,'user_money'=>$money,'type'=>4,'money_type'=>1,'money_type_name'=>4,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>$title_name,'activity_code'=>'','mch_name'=>$mch_name,'withdrawal_fees'=>'','withdrawal_method'=>'');
        }
        $details_id = PC_Tools::add_Balance_details($array);
        // 添加买家退款日志
        $event = $user_id . '退款' . $price . '元余额';
        $sqll3 = array('store_id'=>$store_id,'user_id'=>$user_id,'money'=>$price,'oldmoney'=>$money,'event'=>$event,'type'=>5,'details_id'=>$details_id);
        $rr = Db::name('record')->insert($sqll3);
        if ($rr > 0)
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "新增记录成功！");
        }
        else
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "新增记录失败！");
        }
        return 1;
    }

    // 发送退款消息包括通过和拒绝
    public static function sendWXTopicMsg($msgres, $store_id)
    {
        $lktlog = new LaiKeLogUtils();
        try
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "推送微信小程序订阅消息开始");

            $refundData = array();
            //用户
            $refundData['touser'] = $msgres->uid;
            //跳转地址
            $refundData['page'] = 'pages/index/index';
            //退款金额
            $refundData['amount2'] = $msgres->amount2;
            //申请时间
            $refundData['date3'] = $msgres->date3;
            //订单号
            $refundData['character_string4'] = $msgres->sNo;
            //商品名称
            $refundData['thing5'] = $msgres->p_name;
            //状态
            $refundData['thing6'] = $msgres->thing6;
            //商城ID
            $refundData['store_id'] = $store_id;
            WXTopicMsgUtils::refundOrder($refundData);

            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "推送微信小程序订阅消息结束");
        } 
        catch (Exception $e)
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "推送微信小程序订阅消息失败");
        }
    }

    // 售后订单列表(后台、PC店铺)
    public static function After_sales_list($array)
    {
        $store_id = $array['store_id'];
        $store_type = $array['store_type'];
        $sNo = $array['sNo'];
        $mch_name = $array['mch_name'];
        $re_type = $array['re_type'];
        $r_type = $array['r_type'];
        $startdate = $array['startdate'];
        $enddate = $array['enddate'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $mch_id = $array['mch_id'];

        $otype = 'GM';
        if(isset($array['otype']))
        {
            $otype = $array['otype'];
        }
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " b.store_id = '$store_id' and a.supplier_id = 0 and b.r_type != 100 ";
        if($store_type == 8)
        { // 管理后台
            // $condition .= " and a.mch_id = ',$mch_id,' ";
            if($otype == 'GM')
            {
                $condition .= " and (a.otype = 'GM' or a.otype = 'VI') ";
            }
            else
            {
                $condition .= " and a.otype = '$otype' ";
            }
        }
        else
        {
            $condition .= " and a.mch_id = ',$mch_id,' and lpl.gongyingshang = 0 and a.otype = '$otype' ";
        }

        if ($sNo != '')    
        {
            $condition .= " and b.sNo like '%$sNo%' ";
        }

        if ($mch_name != '')
        {   
            $condition .= " and lm.name like '%$mch_name%' ";
        }

        if($re_type == 1)
        {
            $condition .= ' and b.re_type = 1 ';
        }
        else if($re_type == 2)
        {
            $condition .= ' and b.re_type = 2 ';
        }
        else if($re_type == 3)
        {
            $condition .= ' and b.re_type = 3 ';
        }

        if (($r_type == 7 && is_numeric($r_type)) || ($r_type == 0 && is_numeric($r_type)))// 待审核    
        {    
            $condition .= " and b.r_type = 0 ";    
        }    
        else if ($r_type == 1)//退款中    
        {
            $condition .= " and b.r_type in (1,3) and b.re_type = 1 ";    
        }    
        else if ($r_type == 2)//退款成功   
        {    
            $condition .= " and b.r_type in (4,9,13,15) ";    
        }    
        else if ($r_type == 3) // 退款失败    
        {    
            $condition .= " and b.r_type in(2,5,8) and b.re_type != 3 ";
        }
        else if ($r_type == 4) // 换货中
        {
            $condition .= " and b.r_type in(1,3,11) and  b.re_type = 3 ";
        }
        else if ($r_type == 5) // 换货成功
        {
            $condition .= " and b.r_type = '12' ";
        }
        else if($r_type == 6) //换货失败
        {
            $condition .= " and b.r_type in(5,10) and  b.re_type = 3 ";
        }

        if ($startdate != '')
        {
            $condition .= "and b.re_time >= '$startdate' ";
        }

        if ($enddate != '')
        {
            $condition .= "and b.re_time <= '$enddate' ";
        }

        $con = '';
        foreach ($_GET as $key => $value001)
        {
            $con .= "&$key=$value001";
        }

        $total = 0;
        $sql11 = "select ifnull(count(1),0) as num from lkt_return_order as b
                left join lkt_order_details as c on b.p_id = c.id
                left join lkt_order as a on c.r_sNo = a.sNo 
                left join lkt_configure as fig on b.sid = fig.id
                LEFT JOIN lkt_product_list as lpl on lpl.id = fig.pid
                LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id
                where $condition order by b.r_type asc,b.re_time desc  ";
        $r11 = Db::query($sql11);
        if($r11)
        {
            $total = $r11[0]['num'];
        }

        $list = array();
        if($total > 0)
        {
            $str = " b.id,b.pid,b.r_type,b.re_apply_money,b.re_money,b.re_photo,b.re_time,b.re_type,b.content,b.real_money,b.sNo,b.sid,b.user_id,b.audit_time,c.size,c.p_id,c.p_name,c.p_price,c.num,c.freight,a.z_price,lpl.is_distribution,fig.img,c.unit,lpl.imgurl,lm.id as mch_id,lm.name as mchName,c.after_discount,c.after_write_off_num,b.r_write_off_num,fig.write_off_num,lpl.write_off_settings,a.otype,a.currency_symbol,a.exchange_rate,a.currency_code,lpl.product_title as goodsName,c.p_integral,c.score_deduction ";

            $sql = "select $str from lkt_return_order as b
                    left join lkt_order_details as c on b.p_id = c.id
                    left join lkt_order as a on c.r_sNo = a.sNo 
                    left join lkt_configure as fig on b.sid = fig.id
                    LEFT JOIN lkt_product_list as lpl on lpl.id = fig.pid
                    LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id
                    where $condition order by b.r_type asc,b.re_time desc limit $start,$pagesize ";
            $list = Db::query($sql);
            if($list)
            {
                foreach ($list as $key => $value) 
                {
                    $shop_id = $value['mch_id']; // 店铺ID
                    $otype = $value['otype']; // 订单类型
                    $z_price = $value['z_price']; // 订单金额
                    $refund_price = 0;
                    if($otype == 'VI')
                    {
                        if($value['write_off_settings'] == 1)
                        { // 线下核销
                            $z_write_off_num = $value['r_write_off_num'] + $value['after_write_off_num'];
                            if($z_write_off_num != 0)
                            {
                                $refund_price = round($z_price / $z_write_off_num * $value['r_write_off_num'],2);
                            }
                        }
                        else
                        { // 无需核销
                            $refund_price = round($z_price,2);
                        }
                        $list[$key]['re_money'] = $refund_price;
                    }
                    else if($otype == 'IN')
                    {
                        $list[$key]['score_deduction'] = $value['p_integral'];
                    }
                    $list[$key]['imgurl'] = ServerPath::getimgpath($value['img'],$store_id);
                    
                    $list[$key]['prompt'] = Tools::ObtainAfterSalesStatus($value['re_type'],$value['r_type']);

                    $list[$key]['real_money'] = number_format($value['real_money'],2);
                    if($value['real_money'] > 0)
                    {
                        $list[$key]['realAmtName'] = '';
                    }
                    else
                    {
                        $list[$key]['realAmtName'] = '未退款';
                    }

                    switch ($value['re_type']) {
                        case '1':
                            $list[$key]['returnTypeName'] = '退货退款';
                            break;
                        case '2':
                            $list[$key]['returnTypeName'] = '仅退款';
                            break;
                        default:
                            $list[$key]['returnTypeName'] = '换货';
                            break;
                    }

                    $list[$key]['p_price'] = (float)$value['p_price'];
                    $list[$key]['isExamine'] = false;
                    $list[$key]['isManExamine'] = false;
                    if($value['r_type'] == 0 || $value['r_type'] == 3)
                    {
                        $list[$key]['isExamine'] = true;
                    }
                    if($value['r_type'] == 5 || $value['r_type'] == 2)
                    {
                        $list[$key]['isManExamine'] = true;
                    }

                    $isStoreMch = false;
                    if($shop_id == $mch_id)
                    {
                        $isStoreMch = true;
                    }
                    $list[$key]['isStoreMch'] = true;

                    if($otype == 'PT')
                    {
                        $list[$key]['goodsImgUrl'] = ServerPath::getimgpath($value['img'],$store_id);
                        $p_id = $value['p_id'];
                        $list[$key]['teamPrice'] = '';
                        $list[$key]['teamNum'] = 0;
                        $sql_open = "select team_num as teamNum from lkt_group_open where id = '$p_id' ";
                        $r_open = Db::query($sql_open);
                        if($r_open)
                        {
                            $list[$key]['teamNum'] = $r_open[0]['teamNum'];
                        }

                        $list[$key]['groupType'] = $list[$key]['teamNum'] . "人团";
                    }
                    $list[$key]['orderno'] = $value['sNo'];
                    $list[$key]['userId'] = $value['user_id'];
                    $list[$key]['re_id'] = $value['id'];
                }
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 查看售后详情(后台、PC店铺)
    public static function After_sales_details($array)
    {
        $store_id = $array['store_id'];
        $id = $array['id'];

        $list = array();
        
        $sql = "select b.id,b.pid,b.r_type,b.re_apply_money,b.re_money,b.re_photo,b.re_time,b.re_type,b.real_money,b.sNo,b.sid,c.size,b.user_id,b.content,b.store_id,b.p_id,lm.name as mchName,c.p_name,c.p_price,c.num,lpl.is_distribution,fig.img,b.audit_time,c.after_discount,c.deliver_time,c.freight,lpl.id as goodsId,a.z_price,a.drawid,a.otype,b.r_write_off_num,c.write_off_num,lpl.write_off_settings,c.p_id as superId,a.currency_symbol,a.exchange_rate,c.p_integral,c.score_deduction,a.name,a.cpc,a.mobile,a.sheng,a.shi,a.xian,a.address,a.code  
                from lkt_return_order as b
                left join lkt_order_details as c on b.p_id = c.id
                left join lkt_order as a on c.r_sNo = a.sNo 
                left join lkt_configure as fig on b.sid = fig.id
                LEFT JOIN lkt_product_list as lpl on lpl.id = fig.pid
                LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id
                where b.id = '$id' and c.is_addp = 0 ";
        $res = Db::query($sql);
        if($res)
        {
            $sNo = $res[0]['sNo'];
            $otype = $res[0]['otype'];
            $user_id = $res[0]['user_id'];
            $currency_symbol = $res[0]['currency_symbol'];
            $exchange_rate = $res[0]['exchange_rate'];
            $res[0]['imgurl'] = ServerPath::getimgpath($res[0]['img'],$store_id);

            $array_address = array('cpc'=>$res[0]['cpc'],'sheng'=>$res[0]['sheng'],'shi'=>$res[0]['shi'],'xian'=>$res[0]['xian'],'address'=>$res[0]['address'],'code'=>$res[0]['code']);
            $address_xq = PC_Tools::address_translation($array_address);
            $res[0]['address'] = $address_xq;
            $res[0]['mobile'] = '+' . $res[0]['cpc'] . $res[0]['mobile'];

            $user_mobile = '';
            $sql_u = "select cpc,mobile from lkt_user where user_id = '$user_id' ";
            $r_u = Db::query($sql_u);
            if($r_u)
            {
                $user_mobile = '+' . $r_u[0]['cpc'] . $r_u[0]['mobile'];
            }
            $res[0]['user_mobile'] = $user_mobile;

            $res[0]['prompt'] = Tools::ObtainAfterSalesStatus($res[0]['re_type'],$res[0]['r_type']);
            if($res[0]['r_type'] == 16 && $res[0]['re_type'] == 1)
            {
                $res[0]['prompt'] = "审核中";
            }

            if($res[0]['r_type'] == 15)
            {
                $res[0]['content'] = '极速退款'; 
            }
            $res[0]['p_price'] = number_format($res[0]['p_price'],2);
            $res[0]['rtype'] = $res[0]['r_type'];
            //小计
            $res[0]['z_price'] = number_format(($res[0]['p_price']*$res[0]['num'])+$res[0]['freight'],2);
            //合计
            $res[0]['total'] = number_format($res[0]['after_discount']+$res[0]['freight'],2);
            //优惠金额
            if($otype != 'VI')
            {
                $res[0]['discountAmount'] = number_format(($res[0]['p_price']*$res[0]['num'])-$res[0]['after_discount'],2);
            }
            else
            {
                $res[0]['discountAmount'] = number_format($res[0]['after_discount'],2);
            }
            $re_photo = !empty($res[0]['re_photo']) ? unserialize($res[0]['re_photo']) : array();
            $imgs = array();
            if (!empty($re_photo))
            {
                foreach ($re_photo as $k => $v)
                {
                    $imgs[] = ServerPath::getimgpath($v, $store_id);
                }
            }
            $oid = $res[0]['p_id'];
            $rdata = array();
            $r_type = $res[0]['r_type'];
            if ($r_type == 3 || $r_type == 11 || $r_type == 12)
            {
                $ro = ReturnOrderModel::where(['store_id'=>$store_id,'pid'=>$oid,'re_id'=>$id])->select()->toArray();
                if ($ro)
                {
                    $rdata = $ro;
                }
            }

            $record = array();
            $examineInfo = array();
            //查询售后记录
            $record_res = ReturnRecordModel::where(['p_id'=>$id,'store_id'=>$store_id])->order('id','desc')->select()->toArray();
            if (!empty($record_res))
            {   
                foreach ($record_res as $k => $v) 
                {
                    $record_res[$k]['money'] = (float)$v['money'];
                    if($v['r_type'] == 0)
                    {
                        $examineInfo[$k]['applyTime'] = $v['re_time'];
                    }
                    else
                    {
                        $examineInfo[$k]['applyTime'] = null;
                    }
                    $examineInfo[$k]['examineResultTime'] = null;
                    $examineInfo[$k]['reBackNo'] = null;
                    $examineInfo[$k]['reBackTime'] = null;
                    $examineInfo[$k]['refuseText'] = "";
                    $examineInfo[$k]['returnBackNo'] = null;
                    $examineInfo[$k]['returnMoney'] = $v['real_money'];
                    if($v['r_type'] == 0)
                    {
                        $examineInfo[$k]['examineResult'] = '审核中';
                    }
                    elseif($v['r_type'] == 4 || $v['r_type'] == 9 || $v['r_type'] == 13 || $v['r_type'] == 15)
                    {
                        $examineInfo[$k]['examineResult'] = '退款成功';
                        $examineInfo[$k]['examineResultTime'] = $v['re_time'];
                        $examineInfo[$k]['returnMoney'] = $v['money'];
                    }
                    elseif($v['r_type'] == 2 || $v['r_type'] == 5 || $v['r_type'] == 8 || $v['r_type'] == 10)
                    {
                        $examineInfo[$k]['examineResult'] = '审核拒绝';
                        $examineInfo[$k]['examineResultTime'] = $v['re_time'];
                    }
                    elseif($v['r_type'] == 3)
                    {
                        $examineInfo[$k]['examineResult'] = '商品审核中';
                        $express_id = $v['express_id'];//快递公司编号
                        $r01 = ExpressModel::where('id',$express_id)->select()->toArray();
                        $kuaidi_name = $r01[0]['kuaidi_name'];//快递公司名字
                        $examineInfo[$k]['reBackNo'] = $v['courier_num']."(".$kuaidi_name.")";
                        $examineInfo[$k]['reBackTime'] = $v['re_time'];
                    }
                    elseif($v['r_type'] == 1)
                    {
                        $examineInfo[$k]['examineResult'] = '审核通过';
                        $examineInfo[$k]['examineResultTime'] = $v['re_time'];
                    }
                    elseif($v['r_type'] == 11)
                    {
                        $examineInfo[$k]['examineResult'] = '商品审核通过';
                        $examineInfo[$k]['examineResultTime'] = $v['re_time'];
                        $examineInfo[$k]['reBackTime'] = $v['re_time'];
                        $express_id = $v['express_id'];//快递公司编号
                        $r01 = ExpressModel::where('id',$express_id)->select()->toArray();
                        $kuaidi_name = $r01[0]['kuaidi_name'];//快递公司名字
                        $examineInfo[$k]['returnBackNo'] = $v['courier_num']."(".$kuaidi_name.")";
                    }
                    elseif($v['r_type'] == 15)
                    {
                        $examineInfo[$k]['examineResult'] = '退款成功';
                        $examineInfo[$k]['examineResultTime'] = $v['re_time'];
                    }
                    elseif($v['r_type'] == 16)
                    {
                        $examineInfo[$k]['examineResult'] = '审核通过';
                        $examineInfo[$k]['examineResultTime'] = $v['re_time'];
                    }
                }
                $record = $record_res;
            }

            $AddPurchase = array();
            if($otype == 'PT')
            {
                $sql_0 = "select a.price,b.team_num from lkt_group_open_record as a left join lkt_group_open as b on a.open_id = b.id where a.sNo = '$sNo' ";
                $r_0 = Db::query($sql_0);
                if($r_0)
                {
                    $res[0]['teamNum'] = $r_0[0]['team_num'];
                    $res[0]['teamPrice'] = $r_0[0]['price'];
                }
            }
            else if($otype == 'FS')
            {
                $sql_d = "select a.z_price,d.id as goodsId,b.p_name,b.size,b.num,b.p_price,c.img as imgurl from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo left join lkt_configure as c on b.sid = c.id left join lkt_product_list as d on c.pid = d.id where b.store_id = '$store_id' and b.r_sNo = '$sNo' and b.is_addp = 1 ";
                $r_d = Db::query($sql_d);
                if($r_d)
                {
                    $res[0]['total'] = $r_d[0]['z_price'];
                    foreach($r_d as $k_d => $v_d)
                    {
                        $v_d['imgurl'] = ServerPath::getimgpath($v_d['imgurl'],$store_id);
                        $v_d['z_price'] = number_format(($v_d['p_price'] * $v_d['num']),2);

                        $AddPurchase[] = $v_d;
                    }
                }

                $res[0]['total'] = $res[0]['re_money'];
            }
            else if($otype == 'IN')
            {
                $res[0]['score_deduction'] = $res[0]['p_integral'];
                $res[0]['allow'] = $res[0]['p_integral'];
            }

            $refuseButton = true;
            if($r_type == 16)
            {
                $refuseButton = false;
            }

            $list['examineInfo'] = $examineInfo;
            $list['rdata'] = $rdata;
            $list['record'] = $record;
            $list['list'] = $res[0];
            $list['addGoodsList'] = $AddPurchase;
            $list['imgs'] = $imgs;
            $list['refuseButton'] = $refuseButton;
            $list['returnBtn'] = false;
            $list['selectBtn'] = false;
            $list['currency_symbol'] = $currency_symbol;
            $list['exchange_rate'] = $exchange_rate;

        }
        return $list;
    }
    
    // 退款申请
    public static function Return_Request($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $user_id = $array['user_id'];
        $order_details_id = $array['order_details_id']; // 订单详情ID   
        $refund_amount = $array['refund_amount']; // 因该退款金额
        $re_apply_money = $array['re_apply_money']; // 用户申请退款金额   
        $content = $array['content']; // 退货原因   
        $type = $array['type']; // 退货类型   
        $image_array = $array['image_array']; // 上传图片   

        $lktlog = new LaiKeLogUtils();
        $Tools = new Tools($store_id, 1);
        $time = date('Y-m-d H:i:s');
        $p_name = array();
        $re_photo = "";
        $typeArr = array();
        if (!empty($order_details_id))
        {
            if (is_array($order_details_id))
            { // 是数组
                foreach ($order_details_id as $key => $value)
                {
                    $typeArr[$key] = $value;
                }
            }
            else if (is_string($order_details_id))
            { // 是字符串
                $typestr = trim($order_details_id, ','); // 移除两侧的逗号
                $typeArr = explode(',', $typestr); // 字符串打散为数组
            }
        }

        $tui_num = count($typeArr);
        $RefundType = $type;

        Db::startTrans();
        $refund_price = 0;
        foreach ($typeArr as $k => $v)
        {
            // 根据商城ID、user_id、订单详情ID
            $r = OrderDetailsModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'id'=>$v,'is_addp'=>0])->field('id,p_id,p_name,p_price,num,unit,r_sNo,freight,size,sid,r_status,re_photo,sid,supplier_settlement,write_off_num,coupon_id,living_room_id')->select()->toArray();
            if ($r)
            {
                $id = $r[0]['id']; // 订单ID
                $sNo = $r[0]['r_sNo']; // 订单号
                $p_name[] = $r[0]['p_name']; // 商品名称
                $num = $r[0]['num']; // 数量
                $freight = $r[0]['freight'];
                $r_status = $r[0]['r_status'];
                $attr_id = $r[0]['sid'];
                $pro_id = $r[0]['p_id'];
                $write_off_num = $r[0]['write_off_num']; // 虚拟商品待核销次数  
                $coupon_id = $r[0]['coupon_id']; // 优惠券ID
                $living_room_id = $r[0]['living_room_id']; // 直播间ID

                $order_res = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->field('old_total,z_price,spz_price,mch_id,supplier_id,status,pay,otype,real_sno,z_freight,old_freight,p_sNo,offset_balance,self_lifting,currency_symbol,exchange_rate,currency_code')->select()->toArray();
                $old_total = $order_res[0]['old_total'];// 历史总价
                $z_price = $order_res[0]['z_price'];
                $spz_price = $order_res[0]['spz_price'];
                $mch_id = trim($order_res[0]['mch_id'],','); // 店铺id
                $supplier_id = $order_res[0]['supplier_id']; // 供应商ID
                $status = $order_res[0]['status'];
                $pay = $order_res[0]['pay']; // 支付方式
                $otype = $order_res[0]['otype']; // 订单类型
                $real_sno = $order_res[0]['real_sno']; // 支付订单号
                $z_freight = $order_res[0]['z_freight'];// 总运费
                $old_freight = $order_res[0]['old_freight'];// 历史运费
                $p_sNo = $order_res[0]['p_sNo']; // 父订单号
                $offset_balance = $order_res[0]['offset_balance']; // 抵扣余额
                $self_lifting = $order_res[0]['self_lifting']; // 自提 0.配送 1.自提 2.商家自配 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
                $currency_symbol = $order_res[0]['currency_symbol']; 
                $exchange_rate = $order_res[0]['exchange_rate']; 
                $currency_code = $order_res[0]['currency_code']; 

                $refund_price = Tools::get_order_pro_price($store_id, $v); // 获取该订单详情商品支付金额

                $sql_o = "select count(id) as d_num from lkt_order_details where r_sNo = '$sNo' ";
                $r_o = Db::query($sql_o);
                if($r_o)
                {
                    if($tui_num == $r_o[0]['d_num'])
                    { // 申请售后数量 等于 订单详情数量
                        $refund_price += $r[0]['freight']; // 退还运费
                    }
                }

                if(($type == 2 && $otype == 'GM' && $status == 1 && $self_lifting != 1) || ($type == 2 && $otype == 'ZB' && $status == 1) || ($type == 2 && $otype == 'GM' && $status == 2 && $self_lifting == 1) || ($type == 2 && $otype == 'IN' && $status == 1))
                { // (退款 并且 实物订单 并且 待发货) 或者 （退款 并且 直播订单 并且 待发货） 满足直接退款
                    $array = array('store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo,'order_details_id'=>$id,'type'=>$type,'content'=>$content,'attr_id'=>$attr_id,'pro_id'=>$pro_id,'num'=>$num,'refund_price'=>$refund_price,'re_apply_money'=>$re_apply_money,'refund_amount'=>$refund_amount,'r_status'=>$r_status,'freight'=>$freight,'p_name'=>$r[0]['p_name'],'supplier_id'=>$supplier_id,'coupon_id'=>$coupon_id,'living_room_id'=>$living_room_id,'pay'=>$pay,'real_sno'=>$real_sno,'z_price'=>$z_price,'old_total'=>$old_total,'z_freight'=>$z_freight,'old_freight'=>$old_freight,'offset_balance'=>$offset_balance,'p_sNo'=>$p_sNo,'otype'=>$otype,'mch_id'=>$mch_id);

                    self::Direct_refund($array);
                    $RefundType = 15;
                }
                else
                { // 走申请售后流程
                    // 判断是否已经有过售后
                    $res = ReturnOrderModel::where(['sNo'=>$sNo,'p_id'=>$v])->where('re_type','in','1,2')->where('r_type','not in','2,5,8,10')->select()->toArray();
                    if($res)
                    {
                        $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "重复售后，生成售后订单失败！sNo:" . $sNo);
                        Db::rollback();
                        ob_clean();
                        $message = Lang('Abnormal business');
                        echo json_encode(array('code' => ERROR_CODE_YWYC, 'message' => $message));
                        exit;
                    }
                    
                    $return_order_array = array('store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo,'p_id'=>$id,'re_type'=>$type,'content'=>$content,'re_time'=>$time,'r_type'=>0,'sid'=>$attr_id,'pid'=>$pro_id,'re_money'=>$refund_price,'re_apply_money'=>$refund_price);
                    if (count($image_array) > 0)
                    {
                        $re_photo = serialize($image_array);
                        $return_order_array['re_photo'] = $re_photo;
                    }
                    if($otype == 'VI')
                    { // 虚拟订单
                        $return_order_array['r_write_off_num'] = $write_off_num;
                        // 根据订单详情ID，修改虚拟商品待核销次数
                        $sql_d = "update lkt_order_details set write_off_num = 0 where id = '$id' ";
                        $r_d = Db::execute($sql_d);
                    }
                    else if($otype == 'FS')
                    {
                        $return_order_array['re_money'] = $z_price;
                        $return_order_array['re_apply_money'] = $z_price;
                    }
                    $r_order_id = self::add_return_order($return_order_array); // 添加售后订单

                    $return_record_array = array('store_id'=>$store_id,'user_id'=>$user_id,'re_type'=>$type,'r_type'=>0,'sNo'=>$sNo,'money'=>$refund_price,'re_photo'=>$re_photo,'product_id'=>$pro_id,'attr_id'=>$attr_id,'re_time'=>$time,'explain'=>$content,'p_id'=>$r_order_id);
                    self::add_return_record($return_record_array); // 添加售后记录

                    $message_2 = "订单 ".$sNo."已申请退款（或者退货退款或者换货），请前往退货列表中及时处理！";
                    $message_logging_list2 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>$supplier_id,'type'=>2,'parameter'=>$r_order_id,'content'=>$message_2);
                    PC_Tools::add_message_logging($message_logging_list2);
                }
            }
            else
            {
                $message = Lang('Success');
                echo json_encode(array('code' => ERROR_CODE_bsdqyhdsj, 'message' => $message));
                exit;
            }
        }

        if($type == 2 && $otype == 'GM')
        { // 退款状态
        }
        else
        {
            $rrr = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->field('id,re_apply_money')->select()->toArray();
            $order_details_num = count($rrr);

            $re_apply_money = $rrr[0]['re_apply_money']; // 用户申请退款金额

            if ($re_apply_money * 100 < $refund_amount * 100 && $re_apply_money * 100 / 100 > 0)
            { // 用户申请退款金额 小于 因该退款金额 并且 用户申请退款金额 大于 0
                $refund_amount = $re_apply_money;
            }

            if ($type == 1)
            { // 退货退款
                $_type = 29;
            }
            else if ($type == 2)
            { // 仅退款
                $_type = 28;
            }
            else
            { // 售后
                $_type = 30;
            }
            //存入当前订单改变之前的状态
            $json_str = array();
            $json_str['r_sNo'] = $sNo;
            $json_str['r_status'] = $r_status;
            $json_str['order_details_id'] = $order_details_id;
            $json_str = json_encode($json_str);

            $instsql = new RecordModel();
            $instsql->store_id = $store_id;
            $instsql->user_id = $user_id;
            $instsql->event = $json_str;
            $instsql->type = $_type;
            $instsql->save();
            $rr = $instsql->id;
            if ($rr < 1)
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "添加记录失败！sql:" . $json_str);
            }
        }
        Db::commit();
        $res = $Tools->del_session($user_id, 2, $order_details_id);
        $data = array('product_title' => $p_name, 'sNo' => $sNo, 'time' => $time, 'refund_amount' => round($refund_amount), 'currency_symbol' => $currency_symbol, 'exchange_rate' => $exchange_rate, 'currency_code' => $currency_code,'RefundType'=>$RefundType);
        ob_clean();
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message, 'data' => $data));
        exit;
    }

    // 添加售后订单
    public static function add_return_order($array)
    {
        $lktlog = new LaiKeLogUtils();
        $r_order_id = Db::name('return_order')->insertGetId($array);
        if ($r_order_id < 1)
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "生成售后订单失败！参数:" . json_encode($array));
            Db::rollback();
            ob_clean();
            $message = Lang('Abnormal business');
            return output(ERROR_CODE_YWYC,$message);
        }
        
        return $r_order_id;
    }

    // 添加售后记录
    public static function add_return_record($array)
    {
        $lktlog = new LaiKeLogUtils();
        $rr2 = Db::name('return_record')->insertGetId($array);
        if ($rr2 < 1)
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "添加售后记录失败！参数:" . json_encode($array));
        }
        
        return;
    }

    // 添加供应商收支记录表
    public static function add_supplier_account_log($array)
    {
        $lktlog = new LaiKeLogUtils();
        $r = Db::name('supplier_account_log')->insertGetId($array);
        if ($r < 1)
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "添加供应商收支记录失败！参数:" . json_encode($array));
        }
        
        return $r;
    }

    // 未发货直接退款
    public static function Direct_refund($array)
    {
        $lktlog = new LaiKeLogUtils();
        $time = date("Y-m-d H:i:s");
        $store_id = $array['store_id']; // 商城ID   
        $user_id = $array['user_id'];
        $sNo = $array['sNo']; // 订单号
        $order_details_id = $array['order_details_id']; // 订单详情ID   
        $type = $array['type']; // 退款类型 1:退货退款  2:退款 3:换货
        $content = $array['content']; // 退货原因
        $attr_id = $array['attr_id']; // 属性ID
        $pro_id = $array['pro_id']; // 商品ID
        $num = $array['num']; // 商品数量
        $refund_price = $array['refund_price']; // 理论退款金额
        $re_apply_money = $array['re_apply_money']; // 用户申请退款金额
        $refund_amount = $array['refund_amount']; // 退货金额
        $r_status = $array['r_status']; // 订单详情状态
        $freight = $array['freight']; // 订单详情运费
        $p_name = $array['p_name']; // 商品名称
        $supplier_id = $array['supplier_id'];//供应商id
        $coupon_id0 = $array['coupon_id']; // 订单详情使用的优惠券ID
        $living_room_id = $array['living_room_id']; // 直播间ID
        $pay = $array['pay']; // 支付方式
        $real_sno = $array['real_sno']; // 支付订单号
        $z_price = $array['z_price'];// 订单总价
        $old_total = $array['old_total'];// 历史总价
        $z_freight = $array['z_freight'];// 总运费
        $old_freight = $array['old_freight'];// 历史运费
        $offset_balance = $array['offset_balance']; // 抵扣余额
        $p_sNo = $array['p_sNo']; // 父订单号
        $otype = $array['otype']; // 订单类型
        $mch_id = $array['mch_id']; // 订单所属店铺

        $t1t = true;
        $re_photo = '';// 照片凭证

        if($old_total == '0.00' || $old_total == 0)
        {
            $Retrieve_historical_amounts_array = array('store_id'=>$store_id,'p_sNo'=>$p_sNo,'z_price'=>$z_price,'z_freight'=>$z_freight);
            $Historical_Amount = self::Retrieve_historical_amounts($Retrieve_historical_amounts_array);
            $old_total = $Historical_Amount['old_total'];
            $old_freight = $Historical_Amount['old_freight'];
        }

        if($pay == 'offline_support')
        { // 线下支付
            $message = Lang('order.38');
            echo json_encode(array('code'=>109,'message'=>$message));
            exit;
        }

        $w_pay = $old_total - $offset_balance; // 其它支付所支付金额

        $mch_account_money = 0; // 店铺金额
        $mch_name = '';
        $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('account_money,name')->select()->toArray();
        if($r_mch)
        {
            $mch_account_money = $r_mch[0]['account_money']; // 店铺金额
            $mch_name = $r_mch[0]['name']; // 店铺名称
        }

        // 组合支付
        if ($offset_balance > 0)
        {
            $return_user_money = number_format($offset_balance / $z_price * $re_apply_money, 2, ".", "");
            $t1t = false;
            $price = number_format(($re_apply_money - $return_user_money) / $z_price * $re_apply_money, 2, ".", "");
        }

        $price = round($refund_price,2); // 退款金额

        $return_order_array = array('store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo,'p_id'=>$order_details_id,'re_type'=>$type,'content'=>$content,'re_time'=>$time,'r_type'=>15,'sid'=>$attr_id,'pid'=>$pro_id,'re_money'=>$price,'real_money'=>$price,'re_apply_money'=>$price,'audit_status'=>1);
        if($otype == 'VI')
        { // 虚拟订单
            $return_order_array['r_write_off_num'] = $write_off_num;
            // 根据订单详情ID，修改虚拟商品待核销次数
            $sql_d = "update lkt_order_details set write_off_num = 0 where id = '$id' ";
            $r_d = Db::execute($sql_d);
        }
        $r_order_id = self::add_return_order($return_order_array); // 添加售后订单

        $pay_array = array('store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo,'pay'=>$pay,'price'=>$price,'p_name'=>$p_name,'mch_name'=>$mch_name,'t1t'=>$t1t,'real_sno'=>$real_sno,'r_order_id'=>$r_order_id,'order_details_id'=>$order_details_id,'w_pay'=>$w_pay,'admin_name'=>'');
        $msg_content = self::Refund_method($pay_array);

        if($pay == 'wallet_pay')
        {
            $str_a = "a.store_id,a.user_id,a.otype,a.sNo,a.mch_id,a.old_total,a.z_price,a.z_freight,a.old_freight,a.p_sNo,a.offset_balance,a.supplier_id";
            $str_b = "b.id as order_details_id,b.r_status,b.living_room_id,b.p_id,b.sid,b.num,b.p_name,b.actual_total,b.after_discount,b.freight,b.coupon_id,b.mch_store_write_id,b.write_time,b.write_time_id,b.is_addp,b.supplier_settlement,b.p_integral";
            $str_c = "c.re_type,c.audit_status,c.content,c.r_write_off_num";
            $str_p = "p.write_off_settings,p.is_appointment";
            $str = $str_a . ',' . $str_b . ',' . $str_c . ',' . $str_p;

            $sql0 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo left join lkt_product_list as p on b.p_id = p.id left join lkt_return_order as c on b.id = c.p_id where a.sNo = '$sNo' and c.id = '$r_order_id' ";
            $r0 = Db::query($sql0);
            $r0[0]['mch_name'] = $mch_name;
            
            $Refund_callback = array('trade_no'=>$real_sno,'id'=>$r_order_id,'price'=>$price,'list'=>$r0);
            $data = self::Quick_refund_callback($Refund_callback);
        }

        return;
    }

    // 退款方式
    public static function Refund_method($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $user_id = $array['user_id'];
        $sNo = $array['sNo']; // 订单号
        $pay = $array['pay']; // 支付方式
        $price = $array['price']; // 退款金额
        $p_name = $array['p_name']; // 商品名称
        $mch_name = $array['mch_name']; // 店铺名称
        $t1t = $array['t1t']; // true:不是组合支付  false:时组合支付
        $real_sno = $array['real_sno']; // 支付订单号
        $r_order_id = $array['r_order_id']; // 售后ID
        $order_details_id = $array['order_details_id']; // 订单详情ID 
        $w_pay = $array['w_pay']; // 其它支付所支付金额
        $admin_name = $array['admin_name']; // 操作人名称

        $lktlog = new LaiKeLogUtils();
        $appid = '';
        $msg_title = '';
        $msg_content = '';
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
                if ($t1t)
                {
                    $res = RefundUtils::return_user_money($store_id,$user_id, $price, $sNo,$p_name,$mch_name);
                }
                break;
            case 'aliPay' :
            case 'alipay' :
            case 'pc_alipay' :
            case 'alipay_mobile' :
            case 'alipay_minipay' :
                // 支付宝小程序退款 //支付宝手机支付//支付宝扫码支付
                $zfb_res = AlipayReturn::refund($real_sno, $price, $store_id, $r_order_id, $pay);
                $Log_content = __METHOD__ . '->' . __LINE__ . '支付宝退款结果：' . $zfb_res;
                $lktlog->log("common/return.log",$Log_content);
                if ($zfb_res != 'success')
                {   
                    if($zfb_res == '商家余额不足！' && !empty($mobile))
                    {
                        $array_code = array('cpc'=>$cpc,'mobile'=>$mobile,'type'=>1,'type1'=>10,'bizparams'=>array("sNo" => $sNo));
                        $Tools->generate_code($array_code);
                    }  
                    Db::rollback();
                    echo json_encode(array('code' => 109, 'message' => $zfb_res));
                    exit;
                }
                break;
            case 'app_wechat' :
            case 'mini_wechat' :
            case 'pc_wechat' :
            case 'H5_wechat' :
            case 'jsapi_wechat' :
                //微信公众号 微信小程序支付 微信APP支付.
                $wxtk_res = wxpay::wxrefundapi($real_sno, $real_sno . $order_details_id, $price, $w_pay, $store_id, $pay);
                if ($wxtk_res['result_code'] != 'SUCCESS')
                {
                    Db::rollback();
                    if ($wxtk_res['err_code_des'] == '基本账户余额不足，请充值后重新发起')
                    {
                        $msg_title = Lang('Account_balance_reminder');
                        $msg_content = "账户余额不足，订单【".$sNo."】自动退款失败。请尽快登陆平台完成处理！";

                        $sql_admin = "select b.user_id from lkt_admin as a left join lkt_mch as b on a.shop_id = b.id where a.store_id = '$store_id' and a.type = 1 and a.recycle = 0 ";
                        $r_admin = Db::query($sql_admin);
                        if($r_admin)
                        {
                            $user_id_admin = $r_admin[0]['user_id'];
                        }
                        $pusher = new LaikePushTools();
                        $pusher->pushMessage($user_id_admin, $msg_title, $msg_content, $store_id, $admin_name);

                        $message = Lang('return.12');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }

                    $message = Lang('return.0');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
                break;
            case 'paypal' :
                $config = LKTConfigInfo::getPayConfig($store_id, 'paypal');
                $client_id = $config['client_id'];
                $client_secret = $config['client_secret'];

                $sql_pay = "select capture_id from lkt_order where sNo = '$sNo' ";
                $r_pay = Db::query($sql_pay);
                $capture_id = $r_pay[0]['capture_id'];

                //微信公众号 微信小程序支付 微信APP支付.
                $data = array('client_id' => $client_id,'client_secret' => $client_secret,'capture_id'=>$capture_id);
                $paypal_res = PayPalService::refund($data);

                $Log_content = __METHOD__ . '->' . __LINE__ . '贝宝退款结果：' . json_encode($paypal_res);
                $lktlog->log("common/return.log",$Log_content);
                if ($paypal_res['status'] != 'COMPLETED')
                {
                    Db::rollback();
                    $message = Lang('return.0');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
                else
                {
                    $refund_Id = $paypal_res['id'];

                    $sql_r = "update lkt_return_order set refund_Id = '$refund_Id' where id = '$r_order_id' ";
                    $r_r = Db::execute($sql_r);
                }
                break;
            case 'baidu_pay' :
            default:
                echo json_encode(array('code' => 109, 'message' => $pay . '支付方式不存在！'));
                exit;
        }

        return $msg_content;
    }

    // 处理优惠券
    public static function handle_coupon($array)
    {
        $store_id = $array['store_id'];
        $user_id = $array['user_id'];
        $sNo = $array['sNo'];
        $p_sNo = $array['p_sNo'];
        $order_details_id = $array['order_details_id'];
        $coupon_id0 = $array['coupon_id0'];

        $lktlog = new LaiKeLogUtils();
        if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
        {
            require_once('../app/common/Plugin/CouponPublicMethod.php');
            $coupon = new CouponPublicMethod();
            $coupon_list = explode(',',$coupon_id0); // 订单详情使用的优惠券ID字符串 转数组

            foreach ($coupon_list as $k => $v)
            {
                if($v != 0)
                { // 使用了优惠券
                    if($k == 0 || ($k == 1 && $p_sNo == ''))
                    { // 使用了店铺优惠券 或 (使用了平台优惠券 并且 不是跨店铺订单)
                        // 根据商城ID、订单号、店铺优惠券ID，查询不是这个订单详情的数据
                        $r_c_0 = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->where('id','<>',$order_details_id)->whereLike('coupon_id','%'.$v.'%')->field('id,r_status')->select()->toArray();
                        if($r_c_0)
                        { // 存在(该订单里，还有其它详情使用了这张店铺优惠券)
                            $num_c_0 = count($r_c_0); // 该订单里，有多少详情使用了这张店铺优惠券
                            $num_c_0_0 = 0; // 该订单里，使用了这张店铺优惠券,并退款或退货退款成功的数量
                            foreach ($r_c_0 as $k0 => $v0)
                            {
                                if($v0['r_status'] == 7)
                                {
                                    $num_c_0_0++;
                                }
                            }
                            if($num_c_0_0 == $num_c_0)
                            { // 该订单，使用了这张店铺优惠券的订单商品都退款或退款退款成功
                                $r_coupon1 = $coupon->coupon_sno($store_id, $user_id, $v,$sNo,'update');
                                if ($r_coupon1 == 2)
                                {
                                    //回滚删除已经创建的订单
                                    Db::rollback();
                                    ob_clean();
                                    $message = Lang('Failed_to_add_coupon_associated_data');
                                    echo json_encode(array('code' => 109, 'message' => $message));
                                    exit;
                                }
                                $r = $coupon->update_coupon($store_id, $user_id, $v,0);
                                if ($r == 2)
                                {
                                    $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改优惠卷状态失败！");
                                }
                                else
                                {
                                    $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改优惠卷状态成功！");
                                }
                            }
                        }
                        else
                        { // 不存在(该订单里，没有其它详情使用了这张店铺优惠券)，还原店铺优惠券
                            $r_coupon1 = $coupon->coupon_sno($store_id, $user_id, $v,$sNo,'update');
                            if ($r_coupon1 == 2)
                            {
                                //回滚删除已经创建的订单
                                Db::rollback();
                                ob_clean();
                                $message = Lang('Failed_to_add_coupon_associated_data');
                                echo json_encode(array('code' => 109, 'message' => $message));
                                exit;
                            }
                            $r = $coupon->update_coupon($store_id, $user_id, $v,0);
                            if ($r == 2)
                            {
                                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改优惠卷状态失败！");
                            }
                            else
                            {
                                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改优惠卷状态成功！");
                            }
                        }
                    }
                }
            }
        }
        return;
    }

    // 获取历史金额
    public static function Retrieve_historical_amounts($array)
    {
        $store_id = $array['store_id'];
        $p_sNo = $array['p_sNo'];
        $z_price = $array['z_price'];
        $z_freight = $array['z_freight'];

        $old_total = $array['z_price'];
        $old_freight = $array['z_freight'];
        if($p_sNo != '')
        {
            $res_p = OrderModel::where(['store_id'=>$store_id,'sNo'=>$p_sNo,'status'=>7])->field('z_price,z_freight')->select()->toArray();
            if($res_p)
            {
                $old_total = $res_p[0]['z_price'];
                $old_freight = $res_p[0]['z_freight'];
            }
        }

        $data = array('old_total'=>$old_total,'old_freight'=>$old_freight);
        return $data;
    }

    // 判断订单状态
    public static function Determine_the_status_of_the_order($array)
    {
        $store_id = $array['store_id'];
        $sNo = $array['sNo'];
        $old_total = $array['old_total'];
        $supplier_id = $array['supplier_id'];
        $lktlog = new LaiKeLogUtils();

        $score_deduction = 0;
        $res_o = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->field('id,user_id,r_status,score_deduction')->select()->toArray();
        if (!empty($res_o))
        {
            $user_id = $res_o[0]['user_id'];
            $v_status = '';
            $status_identical = true;
            foreach ($res_o as $k2 => $v2)
            {
                $score_deduction += $v2['score_deduction'];
                if ($v_status == '')
                {
                    $v_status = $v2['r_status'];
                }
                else
                {
                    if ($v_status != $v2['r_status'])
                    {
                        $status_identical = false;
                    }
                }
            }

            // 如果订单下面的商品都处在同一状态,那就改订单状态为已完成
            if ($status_identical)
            {
                $ss = $res_o[0]['r_status'];
                $sql_u = array('status'=>$ss);
                $sql_u_where = array('store_id'=>$store_id,'sNo'=>$sNo);
                $r_u = Db::name('order')->where($sql_u_where)->update($sql_u);
                if ($r_u > 0)
                {
                    $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改订单状态成功！");
                }
                else
                {
                    $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改订单状态失败！参数:" . json_encode($sql_u_where));
                }
                if($ss == 7)
                {
                    //获取订单剩余金额
                    $res_om = Db::name('order')->where($sql_u_where)->field('z_price,mch_id')->select()->toArray();
                    $z_money = $res_om[0]['z_price']; // 整个订单退款后，剩余的金额
                    if($z_money > 0 && $supplier_id == 0)
                    {
                        $mch_id = substr($res_om[0]['mch_id'], 1, -1);
                        $sql_u_where = array('store_id'=>$store_id,'id'=>$mch_id);
                        $sql_u_update = array('account_money'=>Db::raw('account_money-'.$z_money),'cashable_money'=>Db::raw('cashable_money+'.$z_money));
                        $res_u = Db::name('mch')->where($sql_u_where)->update($sql_u_update);
                        if($res_u < 0)
                        {   
                            Db::rollback();
                            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "订单结算到账失败！条件参数:" . json_encode($sql_u_where) . "；修改参数:" . json_encode($sql_u_update));
                        }
                    }
                    //更新订单结算状态
                    $sql_o = OrderModel::where('sNo',$sNo)->find();
                    $sql_o->settlement_status = 1;
                    $sql_o->cancel_method = 2;
                    $sql_o->arrive_time = date('Y-m-d H:i:s');
                    $res_o = $sql_o->save();
                    if(!$res_o)
                    {
                        Db::rollback();
                        $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "订单结算状态修改失败！参数:sNo" . $sNo);
                    }

                    if($score_deduction > 0)
                    {
                        $sql_score_where = array('store_id'=>$store_id,'user_id'=>$user_id);
                        $sql_score_update = array('score'=>Db::raw('score+'.$score_deduction));
                        $r_score = Db::name('user')->where($sql_score_where)->update($sql_score_update);

                        $event = '订单：' . $sNo . '退款成功，返回' . $score_deduction . '积分';
                        $sqll = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$score_deduction,'record'=>$event,'sign_time'=>date('Y-m-d H:i:s'),'type'=>14,'recovery'=>0,'sNo'=>$sNo);
                        $rr = Db::name('sign_record')->insert($sqll);
                        if ($rr < 1)
                        {
                            Db::rollback();
                            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . ' 添加积分记录表失败！数据：' . json_encode($sqll));
                        }
                    }
                }
            }
        }
        return;
    }

    // 取消订单
    public static function CancellationOfOrder($array)
    {
        $store_id = $array['store_id'];
        $shop_id = $array['mch_id'];
        $sNo = $array['sNo'];
        $operator_source = $array['operator_source'];

        $time = date("Y-m-d H:i:s");
        Db::startTrans();
        $p_name = array();
        $order_res = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->field('user_id,old_total,z_price,mch_id,supplier_id,status,pay,otype,real_sno,z_freight,old_freight,p_sNo,offset_balance')->select()->toArray();
        if($order_res)
        {
            $user_id = $order_res[0]['user_id'];// user_id
            $old_total = $order_res[0]['old_total'];// 历史总价
            $z_price = $order_res[0]['z_price'];
            $mch_id = trim($order_res[0]['mch_id'],','); // 店铺id
            $supplier_id = $order_res[0]['supplier_id']; // 供应商ID
            $status = $order_res[0]['status'];
            $pay = $order_res[0]['pay']; // 支付方式
            $otype = $order_res[0]['otype']; // 订单类型
            $real_sno = $order_res[0]['real_sno']; // 支付订单号
            $z_freight = $order_res[0]['z_freight'];// 总运费
            $old_freight = $order_res[0]['old_freight'];// 历史运费
            $p_sNo = $order_res[0]['p_sNo']; // 父订单号
            $offset_balance = $order_res[0]['offset_balance']; // 抵扣余额
            
            if($operator_source != 1)
            {
                if($mch_id != $shop_id)
                {
                    $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "该订单所属店铺ID与传过来的不一致！sNo:" . $sNo . ";mch_id:" . $mch_id);
                    Db::rollback();
                    ob_clean();
                    $message = Lang('Abnormal business');
                    echo json_encode(array('code' => ERROR_CODE_YWYC, 'message' => $message));
                    exit;
                }
            }

            // 根据商城ID、user_id、订单详情ID
            $r1 = OrderDetailsModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'r_sNo'=>$sNo,'is_addp'=>0])->field('id,p_id,p_name,p_price,num,unit,r_sNo,freight,size,sid,r_status,re_photo,sid,supplier_settlement,write_off_num,coupon_id,living_room_id,after_write_off_num')->select()->toArray();
            if($r1)
            {
                foreach($r1 as $k => $v)
                {
                    $id = $v['id']; // 订单ID
                    $p_name[] = $v['p_name']; // 商品名称
                    $num = $v['num']; // 数量
                    $freight = $v['freight'];
                    $r_status = $v['r_status'];
                    $attr_id = $v['sid'];
                    $pro_id = $v['p_id'];
                    $write_off_num = $v['write_off_num']; // 虚拟商品待核销次数
                    $coupon_id = $v['coupon_id']; // 优惠券ID
                    $living_room_id = $v['living_room_id']; // 直播间ID

                    $refund_price = 0;
                    if($otype == 'VI')
                    { // 虚拟订单
                        $sql_p = "select write_off_settings from lkt_product_list where id = '$pro_id' ";
                        $r_p = Db::query($sql_p);
                        if($r_p[0]['write_off_settings'] == 1)
                        { // 线下核销
                            $refund_price = 0;
                            $z_write_off_num = $write_off_num + $v['after_write_off_num'];
                            if($z_write_off_num != 0)
                            {
                                $refund_price = round($z_price / $z_write_off_num * $write_off_num,2);
                            }
                        }
                        else
                        { // 无需核销
                            $refund_price = round($z_price,2);
                        }
                    }
                    else if($otype == 'FS')
                    {
                        $refund_price = round($z_price,2);
                    }
                    else
                    {
                        $refund_price = Tools::get_order_pro_price($store_id, $id); // 获取该订单详情商品支付金额
                        $refund_price += $v['freight']; // 退还运费
                    }

                    if($status == 1 && $otype == 'GM')
                    { // 待发货 并且 普通订单
                        $content = "商家主动取消订单，款项原路返回。";

                        $array = array('store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo,'order_details_id'=>$id,'type'=>2,'content'=>$content,'attr_id'=>$attr_id,'pro_id'=>$pro_id,'num'=>$num,'refund_price'=>$refund_price,'re_apply_money'=>$refund_price,'refund_amount'=>$refund_price,'r_status'=>$r_status,'freight'=>$freight,'p_name'=>$v['p_name'],'supplier_id'=>$supplier_id,'coupon_id'=>$coupon_id,'living_room_id'=>$living_room_id,'pay'=>$pay,'real_sno'=>$real_sno,'z_price'=>$z_price,'old_total'=>$old_total,'z_freight'=>$z_freight,'old_freight'=>$old_freight,'offset_balance'=>$offset_balance,'p_sNo'=>$p_sNo,'otype'=>$otype,'mch_id'=>$mch_id);

                        self::Direct_refund($array);
                    }
                }
            }
        }

        Db::commit();
        $data = array('product_title' => $p_name, 'sNo' => $sNo, 'time' => $time, 'refund_amount' => $refund_price);
        ob_clean();
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message, 'data' => $data));
        exit;
    }

    // 订单退款
    public static function OrderRefund($array)
    {
        $lktlog = new LaiKeLogUtils();
        $time = date("Y-m-d H:i:s");
        $out_biz_no = $array['out_biz_no']; // 支付单号 拼 售后ID
        $trade_no = $array['trade_no']; // 支付单号
        $refund_fee = $array['refund_fee']; // 退款金额
        $price = $array['refund_fee']; // 退款金额
        $id = 0;

        Db::startTrans();

        // 找到搜索字符串的位置
        $position = strpos($out_biz_no, $trade_no);
        if ($position !== false) 
        {
            // 计算起始位置（跳过搜索字符串本身）
            $start = $position + strlen($trade_no);
            
            // 截取从起始位置到末尾的字符串
            $remaining = substr($out_biz_no, $start);
            
            // 去除左侧空格并获取第一个单词
            $id = strtok(trim($remaining), ' '); // 售后ID
        }

        $str_a = "a.store_id,a.user_id,a.otype,a.sNo,a.mch_id,a.old_total,a.z_price,a.z_freight,a.old_freight,a.p_sNo,a.offset_balance,a.supplier_id";
        $str_b = "b.id as order_details_id,b.r_status,b.living_room_id,b.p_id,b.sid,b.num,b.p_name,b.actual_total,b.after_discount,b.freight,b.coupon_id,b.mch_store_write_id,b.write_time,b.write_time_id,b.is_addp,b.supplier_settlement,b.p_integral";
        $str_c = "c.re_type,c.audit_status,c.content,c.r_write_off_num";
        $str_p = "p.write_off_settings,p.is_appointment";
        $str = $str_a . ',' . $str_b . ',' . $str_c . ',' . $str_p;

        $sql0 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo left join lkt_product_list as p on b.p_id = p.id left join lkt_return_order as c on b.id = c.p_id where a.real_sno = '$trade_no' and c.id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $store_id = $r0[0]['store_id']; // 商城ID
            $audit_status = $r0[0]['audit_status']; // 审核状态 0.正常 1.极速退款等待回调 2.不是极速退款等待回调
            $old_total = $r0[0]['old_total']; // 订单历史总价
            $p_sNo = $r0[0]['p_sNo']; // 父订单号
            $z_price = $r0[0]['z_price']; // 订单总价
            $z_freight = $r0[0]['z_freight']; // 订单运费

            if($old_total == '0.00' || $old_total == 0)
            {
                $Retrieve_historical_amounts_array = array('store_id'=>$store_id,'p_sNo'=>$p_sNo,'z_price'=>$z_price,'z_freight'=>$z_freight);
                $Historical_Amount = self::Retrieve_historical_amounts($Retrieve_historical_amounts_array);
                $r0[0]['old_total'] = $Historical_Amount['old_total'];
                $r0[0]['old_freight'] = $Historical_Amount['old_freight'];
            }
            
            $Refund_callback = array('trade_no'=>$trade_no,'id'=>$id,'price'=>$price,'list'=>$r0);

            if($audit_status == 1)
            { // 极速退款等待回调
                $data = self::Quick_refund_callback($Refund_callback);
            }
            else if($audit_status == 2)
            { // 不是极速退款等待回调
                $data = self::After_sales_review_callback($Refund_callback);
            }

            if($data['code'] != 200)
            {
                Db::rollback();
                return;
            }
        }
        Db::commit();
        return;
    }

    // 极速退款回调
    public static function Quick_refund_callback($array)
    {
        $lktlog = new LaiKeLogUtils();
        $time = date("Y-m-d H:i:s");

        $trade_no = $array['trade_no']; // 支付单号
        $id = $array['id']; // 售后ID
        $price = $array['price']; // 退款金额
        $r0 = $array['list'];

        $store_id = $r0[0]['store_id']; // 商城ID
        $user_id = $r0[0]['user_id']; // user_id
        $otype = $r0[0]['otype']; // 订单类型
        $sNo = $r0[0]['sNo']; // 订单号
        $old_total = $r0[0]['old_total']; // 订单历史总价
        $z_price = $r0[0]['z_price']; // 订单总价
        $old_freight = $r0[0]['old_freight']; // 订单历史运费
        $z_freight = $r0[0]['z_freight']; // 订单运费
        $p_sNo = $r0[0]['p_sNo']; // 父订单号
        $offset_balance = $r0[0]['offset_balance']; // 抵扣金额
        $supplier_id = $r0[0]['supplier_id']; // 供应商ID
        $order_details_id = $r0[0]['order_details_id']; // 订单详情ID
        $living_room_id = $r0[0]['living_room_id']; // 订单类型
        $pid = $r0[0]['p_id']; // 商品ID
        $sid = $r0[0]['sid']; // 属性ID
        $num = $r0[0]['num']; // 数量
        $p_name = $r0[0]['p_name']; // 商品名称
        $freight = $r0[0]['freight']; // 订单详情运费
        $coupon_id0 = $r0[0]['coupon_id']; // 订单详情使用的优惠券ID
        $type = $r0[0]['re_type']; // 退款类型 1:退货退款  2:退款 3:换货
        $content = $r0[0]['content']; // 售后原因
        $p_integral = $r0[0]['p_integral']; // 商品积分

        $user = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('money,grade,score')->select()->toArray();
        $total_score = $user[0]['score']; // 当前积分

        $mch_id = trim(',',$r0[0]['mch_id']);
        $mch_name = '';
        $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name')->select()->toArray();
        if($r_mch)
        {
            $mch_name = $r_mch[0]['name'];//店铺名称
        }

        if ($offset_balance > 0)
        {
            $res = RefundUtils::return_user_money($store_id,$user_id, $price, $sNo,$p_name,$mch_name);
        }

        if($otype == 'ZB')
        {
            $array = array('store_id'=>$store_id,'user_id'=>$user_id,'living_id'=>$living_room_id,'sNo'=>$sNo,'pro_id'=>$pid,'config_id'=>$sid,'num'=>$num,'source'=>2);
            $res_stock = LivingPublic::Return_inventory($array);
        }
        else if($otype == 'IN')
        {
            $sql_where2 = array('id' => $pid);
            $sql_update2 = array('num' => Db::raw('num+'.$num));
            $r2 = Db::name('integral_goods')->where($sql_where2)->update($sql_update2);
        }
        else
        {
            $array = array('store_id'=>$store_id,'supplier_id'=>$supplier_id,'pid'=>$pid,'sid'=>$sid,'num'=>$num,'type'=>2);
            $res_stock = PC_Tools::Modify_inventory($array);
            if($res_stock == 1)
            {
                Db::rollback();
                $message = Lang('operation failed');
                $rew = array('code' => 109, 'message' => $message);
                return $rew;
            }
        }

        //发送微信小程序的推送消息
        $openid = Tools::get_openid($store_id, $user_id);
        if($openid)
        {
            $msgres = new \stdClass();
            $msgres->uid = $openid;
            $msgres->amount2 = $price;//退款金额
            $msgres->date3 = $time;//申请时间
            $msgres->sNo = $sNo;//订单号
            $msgres->p_name = $p_name;//商品名称
            $msgres->thing6 = '';//状态
            self::sendWXTopicMsg($msgres, $store_id);
        }

        //修改订单状态为关闭
        $sql = array('r_status'=>7,'audit_time'=>$time);
        $sql_where = array('store_id'=>$store_id,'id'=>$order_details_id);
        $res1 = Db::name('order_details')->where($sql_where)->update($sql);
        if ($res1 > 0)
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "关闭订单成功！");
            //修改主订单数据
            $sql_o = array('z_price'=>Db::raw('z_price-'.$price),'z_freight'=>Db::raw('z_freight-'.$freight),'old_total'=>$old_total,'old_freight'=>$old_freight);
            $sql_o_where = array('store_id'=>$store_id,'sNo'=>$sNo);
            $res_o = Db::name('order')->where($sql_o_where)->update($sql_o);
            if ($res_o > 0)
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改订单数据成功！");
            }
            else
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改订单数据失败！参数：" . json_encode($sql_o_where));
                $message = Lang('return.0');
                $rew = array('code' => 109, 'message' => $message);
                return $rew;
            }

            $sql_o = array('consumer_money'=>Db::raw('consumer_money-'.$price));
            if($otype == 'IN')
            {
                $sql_o['score'] = Db::raw('score+'.$p_integral);

                $event = '退还积分';
                $sql_s_r = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$p_integral,'total_score'=>$total_score,'record'=>$event,'type'=>14,'sign_time'=>$time,'recovery'=>0,'sNo'=>$sNo);
                $r_s_r = Db::name('sign_record')->insert($sql_s_r);
            }
            $sql_o_where = array('store_id'=>$store_id,'user_id'=>$user_id);
            $res_o = Db::name('user')->where($sql_o_where)->update($sql_o);
        }
        else
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "关闭订单失败！参数:" . json_encode($sql_where));
            $message = Lang('return.0');
            $rew = array('code' => 109, 'message' => $message);
            return $rew;
        }

        $Determine_the_status_of_the_order_array = array('store_id'=>$store_id,'sNo'=>$sNo,'old_total'=>$old_total,'supplier_id'=>$supplier_id);
        self::Determine_the_status_of_the_order($Determine_the_status_of_the_order_array);

        if ($coupon_id0 != '' && $coupon_id0 != '0,0')
        { // 当订单详情使用了优惠券
            $handle_coupon_list = array('store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo,'p_sNo'=>$p_sNo,'order_details_id'=>$order_details_id,'coupon_id0'=>$coupon_id0);
            self::handle_coupon($handle_coupon_list); // 处理优惠券
        }

        $return_record_array = array('store_id'=>$store_id,'user_id'=>$user_id,'re_type'=>$type,'r_type'=>15,'sNo'=>$sNo,'money'=>$price,'real_money'=>$price,'re_photo'=>'','product_id'=>$pid,'attr_id'=>$sid,'re_time'=>$time,'explain'=>$content,'p_id'=>$id);
        self::add_return_record($return_record_array); // 添加售后记录

        $rew = array('code' => 200, 'message' => '');
        return $rew;
    }

    // 售后审核回调
    public static function After_sales_review_callback($array)
    {
        $lktlog = new LaiKeLogUtils();
        $time = date("Y-m-d H:i:s");
        $is_it_manual = 1; // 是否是人工 0.系统自动 1.人工

        $trade_no = $array['trade_no']; // 支付单号
        $id = $array['id']; // 售后ID
        $price = $array['price']; // 退款金额
        $r0 = $array['list'];

        $store_id = $r0[0]['store_id']; // 商城ID
        $user_id = $r0[0]['user_id']; // user_id
        $otype = $r0[0]['otype']; // 订单类型
        $sNo = $r0[0]['sNo']; // 订单号
        $old_total = $r0[0]['old_total']; // 订单历史总价
        $z_price = $r0[0]['z_price']; // 订单总价
        $old_freight = $r0[0]['old_freight']; // 订单历史运费
        $z_freight = $r0[0]['z_freight']; // 订单运费
        $p_sNo = $r0[0]['p_sNo']; // 父订单号
        $supplier_id = $r0[0]['supplier_id']; // 供应商ID
        $order_details_id = $r0[0]['order_details_id']; // 订单详情ID
        $supplier_settlement = $r0[0]['supplier_settlement'];//供应商结算金额
        $mch_store_write_id = $r0[0]['mch_store_write_id']; // 预约门店id
        $write_time = $r0[0]['write_time']; // 虚拟商品预约时间
        $write_time_id = $r0[0]['write_time_id']; // 预约时段id
        $write_off_settings = $r0[0]['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
        $is_appointment = $r0[0]['is_appointment']; // 预约时间设置 1.无需预约下单 2.需要预约下单
        $r_write_off_num = $r0[0]['r_write_off_num']; // 虚拟商品退款核销次数
        $living_room_id = $r0[0]['living_room_id']; // 订单类型
        $pid = $r0[0]['p_id']; // 商品ID
        $sid = $r0[0]['sid']; // 属性ID
        $num = $r0[0]['num']; // 数量
        $p_name = $r0[0]['p_name']; // 商品名称
        $actual_total = $r0[0]['actual_total']; // 该详单抵扣金额
        $freight = $r0[0]['freight']; // 订单详情运费
        $offset_balance = $r0[0]['offset_balance']; // 抵扣余额
        $coupon_id0 = $r0[0]['coupon_id']; // 订单详情使用的优惠券ID
        $is_addp = $r0[0]['is_addp']; // 是否加购
        $re_type = $r0[0]['re_type']; // 退款类型 1:退货退款  2:退款 3:售后
        $write_off_settings = $r0[0]['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
        $is_appointment = $r0[0]['is_appointment']; // 预约时间设置 1.无需预约下单 2.需要预约下单
        $p_integral = $r0[0]['p_integral']; // 商品积分

        $user = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('money,grade,score')->select()->toArray();
        $total_score = $user[0]['score']; // 当前积分

        if($re_type == 1)
        { // 退货退款
            $m = 4;
        }
        else if($re_type == 2)
        { // 退款
            $m = 9;
        }

        $mch_id = trim($r0[0]['mch_id'],',');
        $mch_account_money = 0; // 店铺金额
        $mch_name = '';
        $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('account_money,name')->select()->toArray();
        if($r_mch)
        {
            $mch_account_money = $r_mch[0]['account_money']; // 店铺金额
            $mch_name = $r_mch[0]['name'];//店铺名称
        }

        $should_price = Tools::get_order_pro_price($store_id,$order_details_id); // 获取该订单详情商品支付金额
        if($otype == 'FS')
        { // 限时折扣订单
            $max_price = $z_price;
        }
        else
        {
            $max_price = $should_price + $freight; // 获取该订单详情商品支付金额 + 该订单详情运费 = 该订单详情最大退款金额
        }

        $s_money = 0;
        if($actual_total > 0)
        { // 使用了积分抵扣
            $s_money = $r0[0]['after_discount'] + $freight - $price;// 剩余未退款金额 = 优惠后金额 + 运费 - 退款金额
        }
        else
        {
            $s_money = $max_price - $price;// 剩余未退款金额 = 该订单详情最大退款金额 - 退款金额
        }

        // 组合支付余额退款
        if ($offset_balance > 0)
        {
            $res = RefundUtils::return_user_money($store_id,$user_id, $price, $sNo,$p_name,$mch_name);
        }

        $tk_price = 0;
        $account_money = 0; // 店铺余额
        $surplus_account_money = 0; // 供应商余额
        if($supplier_id != 0)
        { // 供应商订单
            $t_mch_type = 5;
            // 根据供应商ID，查询供应商信息
            $res_sup = SupplierModel::where(['store_id'=>$store_id,'id'=>$supplier_id])->select()->toArray();
            if($res_sup)
            {
                $surplus_account_money = $res_sup[0]['surplus_balance']; // 供应商余额
            }
            if($r0[0]['r_status'] > 2)
            { // 已经收货（因为已经确认收货，钱已经到店铺余额）
                if($price > $mch_account_money)
                { // 退款金额 > 店铺金额
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店铺金额不足，退款失败！';
                    $lktlog->log("common/return.log",$Log_content);
                    $message = Lang('return.13');
                    $rew = array('code' => 109, 'message' => $message);
                    return $rew;
                }
                $account_money = $mch_account_money - $price - $s_money; // 店铺余额 = 店铺余额 - 退款金额 - 剩余未退款金额
                $status_mch_account_log = 2; // 出账
                $tk_price = $price + $s_money; // 退款金额 + 剩余未退款金额

                $supplier_settlement = $s_money; // 供应商余额入账金额 = 剩余未退款金额
            }
            else
            { // 还未收货（因为还未收货，钱还未到店铺余额）
                $supplier_settlement = $s_money; // 供应商余额入账金额 = 剩余未退款金额
            }

            if($supplier_settlement > 0)
            {
                $sql_supplier1 = "update lkt_supplier set surplus_balance = surplus_balance + '$supplier_settlement' where store_id = '$store_id' and id = '$supplier_id' ";
                $r_supplier1 = Db::execute($sql_supplier1);
                if($r_supplier1 < 1)
                {
                    $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "供应商余额修改失败！参数:supplier_id" . $supplier_id);
                    $message = Lang('return.0');
                    $rew = array('code' => 109, 'message' => $message);
                    return $rew;
                }
                $supplier_account_log_array = array('store_id'=>$store_id,'supplier_id'=>$supplier_id,'amount'=>$supplier_settlement,'account_money'=>$surplus_account_money,'status'=>1,'type'=>2,'remake'=>$sNo,'remark'=>'退款成功，退还剩余未退款金额给供应商','addtime'=>$time);
                $res2 = self::add_supplier_account_log($supplier_account_log_array);
                if($res2 < 1)
                {
                    $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "供应商添加资金记录失败！参数:supplier_id" . $supplier_id);
                    $message = Lang('return.0');
                    $rew = array('code' => 109, 'message' => $message);
                    return $rew;
                }
            }
        }
        else
        { // 店铺订单
            $t_mch_type = 2;
            if($r0[0]['r_status'] > 2)
            { // 已经收货（因为已经确认收货，钱已经到店铺余额）
                if($price > $mch_account_money)
                { // 退款金额 > 店铺金额
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店铺金额不足，退款失败！';
                    $lktlog->log("common/return.log",$Log_content);
                    $message = Lang('return.13');
                    $rew = array('code' => 109, 'message' => $message);
                    return $rew;
                }
                $account_money = $mch_account_money - $price; // 店铺余额 = 店铺余额 - 退款金额
                $status_mch_account_log = 2;
                $tk_price = $price;
            }
            else
            { // 还未收货（因为还未收货，钱还未到店铺余额）
                $account_money = $mch_account_money + $s_money; // 店铺余额 + 剩余未退款金额
                $status_mch_account_log = 1;
                $tk_price = $s_money;
            }
        }

        if($tk_price > 0)
        {
            $sql5_update = array('account_money'=>$account_money);
            $sql5_where = array('store_id'=>$store_id,'id'=>$mch_id);
            $r5 = Db::name('mch')->where($sql5_where)->update($sql5_update);
            if ($r5 > 0)
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改店铺金额成功！");
            }
            else
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改店铺金额失败！条件参数：" . json_encode($sql5_where) . "修改参数：" . json_encode($sql5_update));
                $message = Lang('return.0');
                $rew = array('code' => 109, 'message' => $message);
                return $rew;
            }

            // 添加一条退款记录
            $sql6 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'price'=>$tk_price,'account_money'=>$mch_account_money,'status'=>$status_mch_account_log,'type'=>$t_mch_type,'addtime'=>$time,'remake'=>$sNo);
            $r6 = Db::name('mch_account_log')->insert($sql6);
            if ($r6 > 0)
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "新增退款记录成功！");
            }
            else
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "新增退款记录失败！参数：" . json_encode($sql6));
                $message = Lang('return.0');
                $rew = array('code' => 109, 'message' => $message);
                return $rew;
            }
        }

        if($otype == 'ZB')
        {
            $array = array('store_id'=>$store_id,'user_id'=>$user_id,'living_id'=>$living_room_id,'sNo'=>$sNo,'pro_id'=>$pid,'config_id'=>$sid,'num'=>$num,'source'=>2);
            $res_stock = LivingPublic::Return_inventory($array);
        }
        else if($otype == 'IN')
        {
            $sql_where2 = array('id' => $pid);
            $sql_update2 = array('num' => Db::raw('num+'.$num));
            $r2 = Db::name('integral_goods')->where($sql_where2)->update($sql_update2);
        }
        else
        {
            $array = array('store_id'=>$store_id,'supplier_id'=>$supplier_id,'pid'=>$pid,'sid'=>$sid,'num'=>$num,'type'=>2);
            $res_stock = PC_Tools::Modify_inventory($array);
            if($res_stock == 1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改库存失败！';
                $lktlog->log("common/return.log",$Log_content);
                $message = Lang('operation failed');
                $rew = array('code' => 109, 'message' => $message);
                return $rew;
            }
        }

        //发送微信小程序的推送消息
        $openid = Tools::get_openid($store_id, $user_id);
        if($openid)
        {
            $msgres = new \stdClass();
            $msgres->uid = $openid;
            $msgres->amount2 = $price;//退款金额
            $msgres->date3 = $time;//申请时间
            $msgres->sNo = $sNo;//订单号
            $msgres->p_name = $p_name;//商品名称
            $msgres->thing6 = '';//状态
            self::sendWXTopicMsg($msgres, $store_id);
        }

        $should_price = Tools::get_order_pro_price($store_id,$order_details_id); // 获取该订单详情商品支付金额

        //修改订单状态为关闭
        $sql = array('r_status'=>7,'audit_time'=>$time);
        $sql_where = array('store_id'=>$store_id,'id'=>$order_details_id);
        $res1 = Db::name('order_details')->where($sql_where)->update($sql);
        if ($res1 > 0)
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "关闭订单成功！");
            //修改主订单数据
            if($price > $should_price)
            { // 退款金额 > 获取该订单详情商品支付金额 (退了运费)
                $sql_o = array('z_price'=>Db::raw('z_price-'.$price),'z_freight'=>Db::raw('z_freight-'.$freight),'old_total'=>$old_total,'old_freight'=>$old_freight);
            }
            else
            { // (没有退了运费)
                $sql_o = array('z_price'=>Db::raw('z_price-'.$price),'old_total'=>$old_total,'old_freight'=>$old_freight);
            }
            $sql_o_where = array('store_id'=>$store_id,'sNo'=>$sNo);
            $res_o = Db::name('order')->where($sql_o_where)->update($sql_o);
            if ($res_o > 0)
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改订单数据成功！");
            }
            else
            {
                $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改订单数据失败！参数：" . json_encode($sql_o_where));
                $message = Lang('return.0');
                $rew = array('code' => 109, 'message' => $message);
                return $rew;
            }

            $sql_o = array('consumer_money'=>Db::raw('consumer_money-'.$price));
            if($otype == 'IN')
            {
                $sql_o['score'] = Db::raw('score+'.$p_integral);

                $event = '退还积分';
                $sql_s_r = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$p_integral,'total_score'=>$total_score,'record'=>$event,'type'=>14,'sign_time'=>$time,'recovery'=>0,'sNo'=>$sNo);
                $r_s_r = Db::name('sign_record')->insert($sql_s_r);
            }
            $sql_o_where = array('store_id'=>$store_id,'user_id'=>$user_id);
            $res_o = Db::name('user')->where($sql_o_where)->update($sql_o);
        }
        else
        {
            $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "关闭订单失败！参数:" . json_encode($sql_where));
            $message = Lang('return.0');
            $rew = array('code' => 109, 'message' => $message);
            return $rew;
        }

        if($otype == 'FS')
        {
            // 根据商城ID、订单号、加购商品，查询详单
            $sql_d = "select id,supplier_id,p_id,sid,num from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and is_addp = 1 ";
            $r_d = Db::query($sql_d);
            if($r_d)
            {
                foreach($r_d as $k_d => $v_d)
                {
                    $array = array('store_id'=>$store_id,'supplier_id'=>$v_d['supplier_id'],'pid'=>$v_d['p_id'],'sid'=>$v_d['sid'],'num'=>$v_d['num'],'type'=>2);
                    $res_stock = PC_Tools::Modify_inventory($array);
                    if($res_stock == 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改库存失败！';
                        $lktlog->log("common/return.log",$Log_content);
                        $message = Lang('operation failed');
                        $rew = array('code' => 109, 'message' => $message);
                        return $rew;
                    }

                    $sql = array('r_status'=>7,'audit_time'=>$time);
                    $sql_where = array('store_id'=>$store_id,'id'=>$v_d['id']);
                    $res1 = Db::name('order_details')->where($sql_where)->update($sql);
                }
            }
        }

        $Determine_the_status_of_the_order_array = array('store_id'=>$store_id,'sNo'=>$sNo,'old_total'=>$old_total,'supplier_id'=>$supplier_id);
        self::Determine_the_status_of_the_order($Determine_the_status_of_the_order_array);

        if($otype == 'FX')
        {   
            //获取所有该订单关联的发放佣金
            $res0 = DistributionRecordModel::where(['type'=>1,'status'=>1,'r_sNo'=>$sNo])->field('user_id,money,genre,from_id,level')->select()->toArray();
            if($res0)
            {
                foreach ($res0 as $key => $value) 
                {
                    $userId = $value['user_id'];
                    $money = $value['money'];
                    $genre = $value['money'];
                    $from_id = $value['from_id'];
                    $level = $value['level'];
                    //退佣金
                    $sql_t = array('commission'=>Db::raw('commission-'.$money),'accumulative'=>Db::raw('accumulative-'.$money));
                    $sql_t_where = array('store_id'=>$store_id,'user_id'=>$userId);
                    $res_t = Db::name('user_distribution')->where($sql_t_where)->update($sql_t);
                    if($res_t < 0)
                    {
                        $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "关闭订单失败:扣除佣金失败！参数:" . json_encode($sql_t_where));
                        $message = Lang('return.0');
                        $rew = array('code' => 109, 'message' => $message);
                        return $rew;
                    }
                    //写日志
                    $event = $sNo.'退款，扣除'.$money.'佣金';

                    $sql_w = array('store_id'=>$store_id,'user_id'=>$userId,'from_id'=>$from_id,'money'=>$money,'sNo'=>$sNo,'level'=>$level,'event'=>$event,'type'=>9,'status'=>1,'genre'=>$genre);
                    $res_w = Db::name('distribution_record')->insert($sql_w);
                    if($res_w < 1)
                    {
                        $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "关闭订单失败:扣除佣金日志记录失败！参数:" . json_encode($sql_w));
                        $message = Lang('return.0');
                        $rew = array('code' => 109, 'message' => $message);
                        return $rew;
                    }
                }  
            }
        }
        elseif($otype == 'GM')
        {
            //查询是否有冻结积分
            $res_in = SignRecordModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'type'=>13,'recovery'=>0])->select()->toArray();
            if($res_in)
            {
                $userId = $res_in[0]['user_id'];
                $sign_id = $res_in[0]['id'];
                $scor = $res_in[0]['sign_score'];
                //扣除用户冻结积分
                $sql_score_where = array('store_id'=>$store_id,'user_id'=>$userId);
                $sql_score_update = array('lock_score'=>Db::raw('lock_score-'.$scor),'score'=>Db::raw('score-'.$scor));
                $r_score = Db::name('user')->where($sql_score_where)->update($sql_score_update);
                if($r_score < 0)
                {
                    $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "关闭订单失败:扣除冻结积分日志记录失败！参数:" . json_encode($sql_score_where));
                    $message = Lang('return.0');
                    $rew = array('code' => 109, 'message' => $message);
                    return $rew;
                }
                //增加回退记录
                $event = $userId . '退还冻结' . $scor . '积分';
                $sqll = array('store_id'=>$store_id,'user_id'=>$userId,'sign_score'=>$scor,'record'=>$event,'sign_time'=>date("Y-m-d H:i:s"),'type'=>14,'recovery'=>0,'sNo'=>$sNo);
                $rr = Db::name('sign_record')->insert($sqll);
                if ($rr < 1)
                {
                    $lktlog->log("common/return.log",__METHOD__ . ":" . __LINE__ . "修改积分记录失败！user_id:" . $userId);
                    $message = Lang('return.0');
                    $rew = array('code' => 109, 'message' => $message);
                    return $rew;
                }
            }
        }

        if ($coupon_id0 != '' && $coupon_id0 != '0,0')
        { // 当订单详情使用了优惠券
            $handle_coupon_list = array('store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo,'p_sNo'=>$p_sNo,'order_details_id'=>$order_details_id,'coupon_id0'=>$coupon_id0);
            self::handle_coupon($handle_coupon_list); // 处理优惠券
        }

        $return_record_array = array('store_id'=>$store_id,'user_id'=>$user_id,'re_type'=>$re_type,'r_type'=>$m,'sNo'=>$sNo,'money'=>$price,'product_id'=>$pid,'attr_id'=>$sid,'re_time'=>$time,'p_id'=>$id,'is_it_manual'=>$is_it_manual);
        self::add_return_record($return_record_array); // 添加售后记录

        if($otype == 'VI')
        {
            $sql_write_record = "select id from lkt_write_record where s_no = '$sNo' ";
            $r_write_record = Db::query($sql_write_record);
            if($r_write_record)
            { // 已经核销过

            }
            else
            {
                if($write_off_settings == 1)
                { // 线下核销
                    if($is_appointment == 2)
                    { // 需要预约下单
                        $write_time_list = explode(' ',$write_time);
                        $write_time_0 = $write_time_list[0]; // 获取预约日期
                        $write_time_1 = explode('-',$write_time_list[1]);
                        $write_time_2 = $write_time_0 . ' ' . $write_time_1[1] . ':00'; // 拼接预约时间

                        if($write_time_2 >= $time)
                        { // 拼接预约时间 >= 当前时间 (还未过预约时间，还原预约次数)
                            $sql_m1 = "select * from lkt_mch_store_write where store_id = '$store_id' and mch_store_id = '$mch_store_write_id' and id = '$write_time_id' ";
                            $r_m1 = Db::query($sql_m1);
                            if($r_m1)
                            {
                                $start_time2 = date("Y-m-d",strtotime($r_m1[0]['start_time'])); // 开始预约日期
                                $seconds = strtotime($write_time_0) - strtotime($start_time2); // 两个时间戳之差
                                $days = ceil($seconds / 3600 / 24);    // 计算天数，向上取整
                                $off_num = $r_m1[0]['off_num'];
                                $off_num_0 = explode(',',$off_num); // 已预约核销次数
                                $off_num_0[$days] = $off_num_0[$days] - 1;
                                $off_num_1 = implode(',',$off_num_0);

                                $sql_m2 = "update lkt_mch_store_write set off_num = '$off_num_1' where store_id = '$store_id' and mch_store_id = '$mch_store_write_id' and id = '$write_time_id' ";
                                $r_m2 = Db::execute($sql_m2);
                            }
                        }
                    }
                }
            }
        }
        else if($otype == 'FS')
        {
            if($is_addp == 0)
            {
                $sql_f = "update lkt_flashsale_record set is_delete = 1 where sNo = '$sNo' ";
                $r_f = Db::execute($sql_f);
                if($r_f < 1)
                {
                    $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "删除限时折扣记录失败！sNo:" . $sNo);
                    $message = Lang('return.0');
                    $rew = array('code' => 109, 'message' => $message);
                    return $rew;
                }
            }
        }

        $rew = array('code' => 200, 'message' => '');
        return $rew;
    }
}
