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
use app\common\Plugin\CouponPublicMethod;
use app\common\Plugin\MchPublicMethod;
use app\common\DeliveryHelper;
use app\common\ReceiveGoodsUtils;
use app\common\Order;
use app\common\LKTConfigInfo;


use app\admin\model\AdminModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\CartModel;
use app\admin\model\BuyAgainModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProductListModel;
use app\admin\model\StockModel;
use app\admin\model\UserFirstModel;
use app\admin\model\MemberConfigModel;

class Margin_order 
{

    public function uninstall(...$context)
    {

    }

    public function install(...$context)
    {

    }
    
    // 确认订单页面
    public function settlement(&...$context)
    {
        
    }

    // 生成订单
    public function payment(&...$context)
    {
        
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

        $sql = "select order_type,data from lkt_order_data where trade_no = '$trade_no'";
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
        $rew = json_decode($r[0]['data'],true);
        $store_id = $rew['storeId'];
        // $store_id = $r[0]['store_id'];
        $pay_type = $r[0]['order_type'];
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
        
        $log->log_result($log_name, '开始');
        $log->log_result($log_name, $total);
        $log->log_result($log_name, $trade_no);
        if (empty($trade_no) || $total <= 0)
        {
            $log->log_result($log_name, "普通订单回调失败信息: \n 订单：$trade_no 支付金额：$total \r\n");
            ob_clean();
            echo 'error';
            exit;
        }
        
        $sql = "select * from lkt_order_data where trade_no = '$trade_no'";
        $r = Db::query($sql);
        $log->log_result($log_name, 'sql：' . $sql);
        $log->log_result($log_name, json_encode($r));
        if ($r)
        {
            $status = $r[0]['status'];
            $rew = json_decode($r[0]['data'],true);
            $store_id = $rew['storeId'];
            if ($status < 1)
            {   
                $this->up_order($store_id, $total, $trade_no);
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
        $user_id = $action->user_id;
        $payment_money = $action->payment_money;
        $sNo = $action->sNo;

        $this->up_order($store_id, $payment_money, $sNo);
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

        $total = $payment_money;
        return array($sNo, $total);
    }

    // 变更订单信息
    public function up_order($store_id, $payment_money, $sNo)
    {
        $time = date("Y-m-d H:i:s");
        Db::startTrans();

        //获取订单信息
        $sql = "select * from lkt_order_data where trade_no = '$sNo'";
        $r = Db::query($sql);
        LaiKeLogUtils::log('app/Margin_order.log', __METHOD__ . ":" . __LINE__ . json_encode($r));
        if($r)
        {
            $data = json_decode($r[0]['data'],true);
            $mch_id = $data['mchId'];
            $promise_amt = $data['paymentAmt'];
            $pay_type = $data['pay'];

            $sql_where1 = array('trade_no'=>$sNo);
            $sql_update1 = array('status'=>1);
            $r1 = Db::name('order_data')->where($sql_where1)->update($sql_update1);
            if($r1 < 0)
            {
                Db::rollback();
                $this->Log(__METHOD__ . ":" . __LINE__ . "变更临时订单状态失败 sql:".json_encode($sql_where1));
                echo json_encode(array('code' => 400, 'message' => '操作失败!', 'line' => __LINE__));
                exit;
            }

            $sql_4 = "select id from lkt_mch_promise where mch_id = '$mch_id' order by add_date desc limit 1 ";
            $r_4 = Db::query($sql_4);
            if($r_4)
            {
                $id = $r_4[0]['id'];

                $sql_3_where = array('id'=>$id,'mch_id'=>$mch_id);
                $sql_3_update = array('orderNo'=>$sNo,'promise_amt'=>$promise_amt,'status'=>1,'pay_type'=>$pay_type,'is_return_pay'=>0,'add_date'=>$time);
                $r_3 = Db::name('mch_promise')->where($sql_3_where)->update($sql_3_update);
            }
            else
            {
                $id = Tools::getUid();
                $sql2 = array('id'=>$id,'orderNo'=>$sNo,'mch_id'=>$mch_id,'promise_amt'=>$promise_amt,'status'=>1,'pay_type'=>$pay_type,'is_return_pay'=>0,'add_date'=>$time);
                $r2 = Db::name('mch_promise')->insertGetId($sql2);
                if($r2 < 1)
                {
                    Db::rollback();
                    $this->Log(__METHOD__ . ":" . __LINE__ . "添加保证金记录失败！参数:".json_encode($sql2));
                    echo json_encode(array('code' => 400, 'message' => '操作失败!', 'line' => __LINE__));
                    exit;
                }
            }
            
            $sql_3 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'money'=>$promise_amt,'type'=>1,'status'=>1,'add_date'=>$time);
            $r_3 = Db::name('promise_record')->insert($sql_3);

            Db::commit();
        }
        else
        {
            Db::rollback();
            $this->Log(__METHOD__ . ":" . __LINE__ . "参数错误！sql:".$sql);
        }
    }

    /**
     * 发货
     * @param mixed ...$context
     * @return mixed|void
     */
    public function deliveryGood(...$context)
    {
        
    }

    /**
     * 收货
     * @param mixed ...$context
     * @return mixed|void
     */
    public function receive_good(...$context)
    {
        
    }

    /**
     * 退款页面（后台退款查看、移动端和PC店铺退款页面）
     * @param mixed ...$context
     * @return mixed|void
     * @throws Exception
     */
    public function refund_page(...$context)
    {
        
    }

    /**
     * 退款
     * @param mixed ...$context
     * @return mixed|void
     * @throws Exception
     */
    public function refund(...$context)
    {
        
    }

    /**
     * 订单详情(后台、PC店铺)
     * @param mixed ...$context
     * @return mixed|void
     * @throws Exception
     */
    public function order_details(...$context)
    {
        
    }

    /**
     * 订单详情(移动端)
     * @param mixed ...$context
     * @return mixed|void
     * @throws Exception
     */
    public function app_order_details(...$context)
    {
        
    }

    /**
     * 订单详情(移动端店铺)
     * @param mixed ...$context
     * @return mixed|void
     * @throws Exception
     */
    public function mch_order_details(...$context)
    {
        
    }

    /**
     * 订单列表
     * @param mixed ...$context
     * @return mixed|void
     */
    public function order_index(...$context)
    {
        
    }
    
    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/Margin_order.log",$Log_content);
        return;
    }
}
