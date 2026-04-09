<?php

namespace app\admin\controller\app;

use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\LKTConfigInfo;
use app\common\Plugin\RefundUtils;
use app\common\PayPalService;
use app\common\alipay0\aop\test\AlipayReturn;
use app\common\wxpayv2\wxpay;

use app\admin\model\CustomerModel;
use app\admin\model\ConfigModel;

class RefundTest
{
    public function index()
    {
        $tres = ConfigModel::order('store_id', 'asc')->field('store_id')->select()->toArray();

        $this->Log(__METHOD__ . ":" . __LINE__ . "定时任务开启");
        foreach ($tres as $tkey => $tvalue)
        {
            $this->store_id = $tvalue['store_id'];
            if (!$this->getStoreStatus($this->store_id))
            {
                $this->Log(__METHOD__ . "-->" . __LINE__ . "店铺 [ $this->store_id ] 状态已经锁定或删除、回收");
                continue;
            }

            // 获取售后详情
            $this->get_refund();
        }
        echo '执行完成。';
        exit;
    }
   
    public function getStoreStatus($store_id)
    {
        $res = CustomerModel::where(['id'=>$store_id,'recycle'=>0,'status'=>0])->select()->toArray();
        return count($res) > 0 ? true : false;
    }

    // 获取售后详情
    public function get_refund()
    {
        $store_id = $this->store_id;
        
        $str_a = "a.store_id,a.user_id,a.otype,a.sNo,a.mch_id,a.old_total,a.z_price,a.z_freight,a.old_freight,a.p_sNo,a.offset_balance,a.supplier_id,a.real_sno,a.pay";
        $str_b = "b.id as order_details_id,b.r_status,b.living_room_id,b.p_id,b.sid,b.num,b.p_name,b.actual_total,b.after_discount,b.freight,b.coupon_id,b.mch_store_write_id,b.write_time,b.write_time_id,b.is_addp,b.supplier_settlement";
        $str_c = "c.id,c.re_type,c.audit_status,c.content,c.r_write_off_num,c.re_money,c.refund_Id";
        $str_p = "p.write_off_settings,p.is_appointment";
        $str = $str_a . ',' . $str_b . ',' . $str_c . ',' . $str_p;

        $sql0 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo left join lkt_product_list as p on b.p_id = p.id left join lkt_return_order as c on b.id = c.p_id where a.store_id = '$store_id' and a.pay != 'wallet_pay' and c.audit_status != 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach ($r0 as $k => $v)
            {
                Db::startTrans();

                $store_id = $v['store_id']; // 商城ID
                $audit_status = $v['audit_status']; // 审核状态 0.正常 1.极速退款等待回调 2.不是极速退款等待回调
                $old_total = $v['old_total']; // 订单历史总价
                $p_sNo = $v['p_sNo']; // 父订单号
                $z_price = $v['z_price']; // 订单总价
                $z_freight = $v['z_freight']; // 订单运费
                $id = $v['id']; // 售后ID
                $real_sno = $v['real_sno']; // 支付单号
                $price = $v['re_money']; // 退款金额
                $pay = $v['pay']; // 支付方式

                if($pay == 'paypal')
                {
                    $config = LKTConfigInfo::getPayConfig($store_id, 'paypal');
                    $client_id = $config['client_id'];
                    $client_secret = $config['client_secret'];

                    //微信公众号 微信小程序支付 微信APP支付.
                    $data = array('client_id' => $client_id,'client_secret' => $client_secret,'refund_Id'=>$v['refund_Id']);
                    $paypal_res = PayPalService::GetRefund($data);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '贝宝退款详情：' . json_encode($paypal_res);
                    $this->Log($Log_content);
                    if ($paypal_res['status'] != 'COMPLETED')
                    {
                        Db::rollback();
                        continue;
                    }
                }
                else if($pay == 'aliPay' || $pay == 'alipay' || $pay == 'pc_alipay' || $pay == 'alipay_mobile' || $pay == 'alipay_minipay')
                {
                    $zfb_res = AlipayReturn::get_refund($real_sno, $store_id, $id, $pay);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '支付宝退款详情：' . $zfb_res;
                    $this->Log($Log_content);
                    if ($zfb_res != 'success')
                    {
                        Db::rollback();
                        continue;
                    }
                }
                else if($pay == 'app_wechat' || $pay == 'mini_wechat' || $pay == 'pc_wechat' || $pay == 'H5_wechat' || $pay == 'jsapi_wechat')
                {
                    //微信公众号 微信小程序支付 微信APP支付.
                    $wxtk_res = wxpay::wxrefundqueryapi($real_sno, $real_sno . $order_details_id, $store_id, $pay);
                    if ($wxtk_res['result_code'] != 'SUCCESS')
                    {
                        Db::rollback();
                        continue;
                    }
                }
                else
                {
                    Db::commit();
                    continue;
                }

                if($old_total == '0.00' || $old_total == 0)
                {
                    $Retrieve_historical_amounts_array = array('store_id'=>$store_id,'p_sNo'=>$p_sNo,'z_price'=>$z_price,'z_freight'=>$z_freight);
                    $Historical_Amount = RefundUtils::Retrieve_historical_amounts($Retrieve_historical_amounts_array);
                    $v['old_total'] = $Historical_Amount['old_total'];
                    $v['old_freight'] = $Historical_Amount['old_freight'];
                }

                $list[0] = $v;
                $Refund_callback = array('trade_no'=>$real_sno,'id'=>$id,'price'=>$price,'list'=>$list);

                if($audit_status == 1)
                { // 极速退款等待回调
                    $data = RefundUtils::Quick_refund_callback($Refund_callback);
                }
                else if($audit_status == 2)
                { // 不是极速退款等待回调
                    $data = RefundUtils::After_sales_review_callback($Refund_callback);
                }

                if($data['code'] != 200)
                {
                    Db::rollback();
                    continue;
                }

                $sql_r = "update lkt_return_order set audit_status = '0' where id = '$id' ";
                $r_r = Db::execute($sql_r);

                Db::commit();
                continue;
            }
        }
    }


    // 日志
    public function Log($Log_content)
    {
        $time = date("Y-m-d");
        $lktlog = new LaiKeLogUtils();

        $lktlog->log("app/RefundTest/".$time.".log",$Log_content);
        return;
    }
}

