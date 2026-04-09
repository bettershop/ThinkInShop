<?php

namespace app\admin\controller\app;

use think\facade\Db;
use app\common\OSSCommon;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\LKTConfigInfo;
use app\common\ReceiveGoodsUtils;
use app\common\Plugin\PluginUtils;
use app\common\Plugin\CouponPublicMethod;
use app\common\Plugin\MchPublicMethod;
use app\common\Plugin\FlashSalePublic;
use app\common\wxpayv2\wxpay;

use app\admin\model\GuideModel;
use app\admin\model\CustomerModel;
use app\admin\model\OrderConfigModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\ProductListModel;
use app\admin\model\ConfigureModel;
use app\admin\model\StockModel;
use app\admin\model\ConfigModel;
use app\admin\model\TaobaoModel;
use app\admin\model\TaobaoWorkModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\PluginsModel;
use app\admin\model\SupplierModel;
use app\admin\model\SupplierOrderFrightModel;
use app\admin\model\SupplierAccountLogModel;
use app\admin\model\MchModel;
use app\admin\model\IntegralConfigModel;

class Test
{
    public function test2()
    {
        $res = Tools::verifyToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE3NjgyODQxMDcsImV4cCI6MTc2ODM3MDUwNywic3RvcmVfdHlwZSI6InVzZXIxIiwianRpIjoiNmRhMmQxOTZjZjIyNDNhNTkwZmM0ZTNmNGVkMTc5M2YiLCJhdSI6ImMzNzgyNzNmNzc4Nzc4OGFlY2ZjZmVmMGM5M2VhZDMwOTcwZTIyNzFlMzA1NTY0MGFmODhhM2YxMjVlMG");
        var_dump($res);
    }
    
    public function index()
    {
        $tres = ConfigModel::order('store_id', 'asc')->field('store_id')->select()->toArray();
        // 处理优惠券问题
        $coupon = new CouponPublicMethod();
        $coupon_arr = $coupon->timing();

        // 提取码修改
        $mch = new MchPublicMethod();
        $mch->timing();

        // 定时修改提取码问题
        $mch->up_extraction_code();
        // 营业状态
        $mch->Business_status();

        $this->testLog(__METHOD__ . ":" . __LINE__ . "定时任务开启");
        foreach ($tres as $tkey => $tvalue)
        {
            $this->store_id = $tvalue['store_id'];
            if (!$this->getStoreStatus($this->store_id))
            {
                $this->testLog(__METHOD__ . "-->" . __LINE__ . "店铺 [ $this->store_id ] 状态已经锁定或删除、回收");
                continue;
            }
            
            $mch->statisticsMch($this->store_id);
            
            // if(date("Y-m-d 00:00:00") <= date("Y-m-d H:i:s") && date("Y-m-d H:i:s") <= date("Y-m-d 00:05:00"))
            // {
            //     // 获取微信物流公司
            //     $this->Obtain_WeChat_logistics_company();
            // }
            
            // // 查询订单发货状态
            // $this->Get_WeChat_order_status();

            // 删除过期订单
            $this->order_failure();

            //自动收货
            $this->ok_Order();

            //换货自动收货
            $this->ok_re_Order();

            // 修改买家能否提醒发货按钮
            $this->remind_deliver();

            // // 检查商品状态(JAVA没有这个功能)
            // $this->product_status();

            // 消息定时清除
            $this->message_day();

            // 自动好评
            $this->auto_good_comment();

            //支付密码次数重置
            $this->login_num_resetting();

            //商家订单结算
            $this->order_settlement();
            
            // 售后时间判断
            $this->return_time();

            //查询插件安装记录动态调用插件的定时任务方法
            $res = PluginsModel::where(['status'=>1])->group('plugin_code')->field('plugin_code')->select()->toArray();
            foreach ($res as $item)
            {   
                if ($item['plugin_code'] != 'coupon' && $item['plugin_code'] != 'mch' && $item['plugin_code'] != 'diy' && $item['plugin_code'] != 'prize')
                {
                    PluginUtils::invokeMethod($item['plugin_code'], 'dotask', $this);
                }
            }
            
        }
        echo '执行完成。';
        exit;
    }
   
    public function getStoreStatus($store_id)
    {
        $res = CustomerModel::where(['id'=>$store_id,'recycle'=>0,'status'=>0])->select()->toArray();
        return count($res) > 0 ? true : false;
    }

    // 删除订单过期
    public function order_failure()
    {
        $store_id = $this->store_id;

        $r = OrderConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        $order_failure = $r ? $r[0]['order_failure'] : 1; // 未付款订单保留时间
        $unit = $r ? $r[0]['unit'] : 1; // 未付款订单保留时间单位
        if ($order_failure != 0)
        {
            $time01 = date("Y-m-d H:i:s", strtotime("-$order_failure hour")); // 订单过期删除时间

            // 根据用户id，订单为未付款，订单添加时间 小于 未付款订单保留时间,查询订单表
            $r0 = OrderModel::where(['store_id'=>$store_id,'status'=>0])->where('add_time','<',$time01)->whereIn('otype',('GM,FX,FS,ZB'))->select()->toArray();
            if ($r0)
            { // 有数据，循环查询优惠券id,修改优惠券状态
                foreach ($r0 as $k => $v)
                {
                    $coupon_id0 = $v['coupon_id'];  // 优惠券id
                    $user_id = $v['user_id']; // user_id
                    $sNo = $v['sNo']; // 订单号
                    $psNo = $v['p_sNo']; // 父订单号
                    $otype = $v['otype'];//订单类型

                    if ($otype == 'FS')
                    {
                        $sql_f = "update lkt_flashsale_record set is_delete = 1 where sNo = '$sNo' ";
                        $r_f = Db::execute($sql_f);
                        if($r_f)
                        {
                            $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "删除限时折扣记录失败！sNo:" . $sNo);
                        }
                    }
                    
                    if ($coupon_id0 != '' && $coupon_id0 != '0,0')
                    { // 当订单详情使用了优惠券
                        if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
                        {
                            $coupon = new CouponPublicMethod();
                            $coupon_list = explode(',',$coupon_id0); // 订单详情使用的优惠券ID字符串 转数组

                            foreach ($coupon_list as $k => $v)
                            {
                                if($v != 0)
                                { // 使用了优惠券
                                    $r_coupon1 = $coupon->coupon_sno($store_id, $user_id, $v,$sNo,'update');
                                    if ($r_coupon1 == 2)
                                    {
                                        $this->testLog(__METHOD__ . '->' . __LINE__ . '修改优惠卷状态失败！');
                                    }
                                    $r = $coupon->update_coupon($store_id, $user_id, $v,0);
                                    if ($r == 2)
                                    {
                                        $this->testLog(__METHOD__ . '->' . __LINE__ . '修改优惠卷状态失败！');
                                    }
                                    else
                                    {
                                        $this->testLog(__METHOD__ . '->' . __LINE__ . '修改优惠卷状态成功！');
                                    }
                                }
                            }
                        }
                    }

                    $sql_where_o = array('store_id'=>$store_id,'status'=>0,'sNo'=>$sNo);
                    $r_o = Db::name('order')->where($sql_where_o)->update(['status'=>'7']);
                    $this->testLog(__METHOD__ . '->' . __LINE__ . '订单更新为关闭状态！参数：' . json_encode($sql_where_o));

                    //清除對應訂單分銷記錄
                    $r_fx = Db::table('lkt_distribution_record')->where('sNo',$sNo)->delete();

                    // 根据用户id、订单未付款、添加时间小于前天时间,就删除订单详情信息
                    $r1 = OrderDetailsModel::where(['store_id'=>$store_id,'r_status'=>0,'r_sNo'=>$sNo])->where('add_time','<',$time01)->select()->toArray();
                    if ($r1)
                    {
                        foreach ($r1 as $k1 => $v1)
                        {
                            //删除订单详情信息
                            $sql_where2 = array('store_id'=>$store_id,'id'=>$v1['id']);
                            $r2 = Db::name('order_details')->where($sql_where2)->update(['r_status'=>'7']);

                            $sql_where3 = array('store_id'=>$store_id,'id'=>$v1['p_id']);
                            $r3 = Db::name('product_list')->where($sql_where3)->update(['num'=>Db::raw('num+'.$v1['num'])]);

                            $sql_where4 = array('id'=>$v1['sid']);
                            $r4 = Db::name('configure')->where($sql_where4)->update(['num'=>Db::raw('num+'.$v1['num'])]);
                        }
                    }
                }
            }
        }
    }

    // 确认收货
    public function ok_Order()
    {
        ReceiveGoodsUtils::timeReceive($this->store_id);
    }

    // 换货自动收货
    public function ok_re_Order()
    {
        $store_id = $this->store_id;

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
        $sql = "update lkt_return_order set r_type = 12 where re_type = 3 and r_type = 11 and store_id = '$store_id' and date_add(audit_time, interval $auto_the_goods day) < now()";
        $res = Db::execute($sql);
        if($res < 0)
        {
            $this->testLog("更新换货订单失败" . $sql);
        }
    }

    // 修改买家能否提醒发货按钮
    public function remind_deliver()
    {
        $store_id = $this->store_id;

        $time = date('Y-m-d H:i:s');

        $r0 = OrderModel::where(['store_id'=>$store_id,'delivery_status'=>1])->field('id,delivery_status,readd,remind,otype')->select()->toArray();
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $id = $v['id']; // 订单ID
                $otype = $v['otype'];
                $remind = $v['remind']; // 提醒发货时间间隔
                if($otype == 'IN')
                {
                    $Self_operated_store_id = PC_Tools::SelfOperatedStore($store_id);
                    $mch_config = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$Self_operated_store_id])->field('deliver_remind')->select()->toArray();
                    $deliver_remind = $mch_config[0]['deliver_remind'];
                    if($deliver_remind != 0)
                    {
                        if ($remind != '' || $remind != null)
                        {
                            if ($remind <= $time)
                            { // 当前时间大于等于 提醒发货时间间隔
                                Db::name('order')->where(['store_id'=>$store_id,'id'=>$id])->update(['delivery_status'=>'0','readd'=>'0','remind'=>null]);
                            }
                        }
                    }
                }
                else
                {
                    if ($remind != '' || $remind != null)
                    {
                        if ($remind <= $time)
                        { // 当前时间大于等于 提醒发货时间间隔
                            Db::name('order')->where(['store_id'=>$store_id,'id'=>$id])->update(['delivery_status'=>'0','readd'=>'0','remind'=>null]);
                        }
                    }
                }
            }
        }
    }

    // 检查商品库存
    public function product_status()
    {
        $time = date('Y-m-d H:i:s');

        // 查询所有上架商品信息
        $r0 = ProductListModel::where(['recycle'=>0,'status'=>2,'num'=>0])->field('id,store_id,num,min_inventory,status')->select()->toArray();
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $pid = $v['id']; // 商品id
                $store_id = $v['store_id']; // 商城id
                $num = $v['num']; // 商品库存
                $min_inventory = $v['min_inventory']; // 预警数量
                $status = $v['status']; // 状态
                if ($num == 0)
                { // 当库存为0 并且商品还为上架状态
                    // 根据商品id，修改商品状态（下架）
                    Db::name('product_list')->where(['id'=>$pid])->update(['status'=>3]);
                }
                // 根据商品id,查询属性信息
                $r_s = ConfigureModel::where(['pid'=>$pid])->field('id,num')->select()->toArray();
                if ($r_s)
                {
                    foreach ($r_s as $k1 => $v1)
                    {
                        $configure_id = $v1['id']; // 属性id
                        if ($v1['num'] <= $min_inventory)
                        { // 当该属性剩余库存低于等于预警数量
                            // 根据商品id、属性id、预警类型，查询库存记录表
                            $r4 = StockModel::where(['type'=>2,'product_id'=>$pid,'attribute_id'=>$configure_id])->field('id')->select()->toArray();
                            if (empty($r4))
                            { // 不存在，表示还没有添加预警信息
                                $content1 = '预警';
                                $sql5 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$configure_id,'type'=>2,'add_date'=>$time,'content'=>$content1);
                                Db::name('stock')->insert($sql5);
                            }
                        }
                    }
                }
            }
        }
    }

    // 消息定时清除
    public function message_day()
    {
        $store_id = $this->store_id;

        $r = ConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if ($r)
        {
            $message_day = $r[0]['message_day'];  // 消息保留天数
            if ($message_day > 0)
            {
                $nowtime = date("Y-m-d H:i:s"); //当前时间
                $todata = date('Y-m-d H:i:s', strtotime($nowtime . '- ' . intval($message_day) . 'day')); //到期时间

                Db::table('lkt_system_message')->where('store_id',$store_id)->where('time','<',$todata)->delete();
            }
        }
    }

    // 自动好评
    public function auto_good_comment()
    {
        $store_id = $this->store_id;
        $add_time = date('Y-m-d H:i:s');

        $auto_good_comment_day = 0;
        //查询是否开启自动好评
        $auto_good_comment_day_arr = OrderConfigModel::where(['store_id'=>$store_id])->field('auto_good_comment_day,auto_comment_content')->select()->toArray();
        if (!empty($auto_good_comment_day_arr))
        {
            $auto_good_comment_day = $auto_good_comment_day_arr[0]['auto_good_comment_day'];
            $auto_comment_content = $auto_good_comment_day_arr[0]['auto_comment_content'];
        }

        if ($auto_good_comment_day > 0)
        { // 设置了天数自动好评
            // 查询所有没有评论的订单商品
            $time = date("Y-m-d H:i:s",strtotime("-5 minutes"));
            $sql = "SELECT
                        a.id,a.r_sNo,a.user_id,a.p_id,a.sid 
                    FROM
                        lkt_order_details a
                        LEFT JOIN lkt_comments b 
                        ON  a.user_id = b.uid 
                        AND a.id = b.order_detail_id 
                        AND b.uid IS NULL
                    WHERE
                        a.r_status = 5 
                        AND a.store_id = $store_id 
                        AND a.r_sNo like 'GM%' 
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
                    
                    $comment = $auto_comment_content?$auto_comment_content:Lang('comments.0');
                    $start = 5;
                    $anonymous = 0;

                    $sql_d = array('store_id'=>$store_id,'oid'=>$sNo,'uid'=>$user_id,'pid'=>$goodsId,'attribute_id'=>$attribute_id,'content'=>$comment,'CommentType'=>$start,'anonymous'=>$anonymous,'add_time'=>$add_time,'order_detail_id'=>$order_detail_id);
                    Db::name('comments')->insert($sql_d);
                }
            }
        }
    }

    // 支付密码次数重置
    public function login_num_resetting()
    {
        $store_id = $this->store_id;
        $time = date("Y-m-d 00:00:00");

        $res = Db::name('user')->where('store_id', $store_id)->where('verification_time','<=', $time)->where('login_num', '>',0)->update(['login_num' => 0]);
        if ($res < 0)
        {
            $this->testLog("重置支付密码次数失败！" );
        }
    }

    // 商家订单结算
    public function order_settlement()
    {
        $store_id = $this->store_id;

        // 获取分账状态
        $profit_sharing = PC_Tools::Obtain_the_splitting_status(array('store_id'=>$store_id));

        Db::startTrans();
        //获取订单售后期
        $res_c = OrderConfigModel::where(['store_id'=>$store_id])->field('order_after')->select()->toArray();
        if($res_c)
        {
            $order_after = $res_c[0]['order_after'];
        }
        else 
        {
            $order_after = 0;
        }
    
        $time = date("Y-m-d H:i:s",strtotime("-$order_after day"));
        
        $sql_m = "select z_price,mch_id,sNo,id,supplier_id,dividend_status,otype,transaction_id,pay,trade_no from lkt_order where store_id = '$store_id' and status = 5 and arrive_time <= '$time' and settlement_status = 0 and otype in ('GM','FX','ZB') ";
        // $sql_m = "select z_price,mch_id,sNo,id,supplier_id,Dividend_status,otype,transaction_id,pay,trade_no from lkt_order where id = '165'  ";
        $res_m = Db::query($sql_m);
        if($res_m)
        {
            foreach ($res_m as $key => $value) 
            {
                $mch_id = substr($value['mch_id'], 1, -1);
                $money = $value['z_price']; // 订单金额
                $supplier_id = $value['supplier_id']; // 供应商ID
                $dividend_status = $value['dividend_status']; // 分账状态 0.不分账 1.分账
                $otype = $value['otype']; // 订单类型
                $transaction_id = $value['transaction_id']; // 微信返回支付单号唯一标识
                $pay = $value['pay']; // 支付方式
                $trade_no = $value['trade_no']; // 支付单号

                $res = ReturnOrderModel::where(['sNo'=>$value['sNo']])->whereIn('r_type','0,1,3')->where('re_type','<>',3)->field('id')->select()->toArray();
                if(empty($res))
                {   
                    if($profit_sharing == 'Y' && $dividend_status == 1 && $supplier_id == 0 && $otype == 'GM' && $pay == 'mini_wechat')
                    { // 分账开关开启 并且 需要分账 并且 不是供应商订单 并且 是普通订单 并且 是小程序支付
                        $res_array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'money'=>$money);
                        $res_date = PC_Tools::Obtain_ledger_data($res_array);

                        if($res_date['accounts_set'] != '' && $res_date['receivers'] != array())
                        { // 满足分账需求
                            $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'accounts_set'=>$res_date['accounts_set'],'transaction_id'=>$transaction_id,'out_order_no'=>$trade_no,'receivers'=>$res_date['receivers'],'With_proportion'=>$res_date['With_proportion'],'pay'=>$pay,'sNo'=>$value['sNo'],'money'=>$money);
                            $rew = wxpay::Divide_accounts($array);
                            if($rew != 1)
                            {
                                Db::rollback();
                                $this->testLog(__METHOD__ . ":" . __LINE__ . "分账失败！");
                            }
                        }
                        else
                        { // 不满足分账需求，走普通结算流程
                            if($value['z_price'] > 0)
                            {
                                $sql_u_where = array('store_id'=>$store_id,'id'=>$mch_id);
                                // 根据店铺ID，店铺商户余额减少，可取现金额增加
                                $sql_u_update = array('account_money'=>Db::raw('account_money-'.$money),'cashable_money'=>Db::raw('cashable_money+'.$money)); //49566
                                $res_u = Db::name('mch')->where($sql_u_where)->update($sql_u_update);
                                if($res_u < 0)
                                {   
                                    Db::rollback();
                                    $this->testLog(__METHOD__ . ":" . __LINE__ . "订单结算到账失败！参数:" . json_encode($sql_u_where));
                                }
                                else
                                {
                                    $sql_where = array('store_id'=>$store_id,'r_sNo'=>$value['sNo'],'r_status'=>5);
                                    $sql_update = array('settlement_type'=>1);
                                    $res = Db::name('order_details')->where($sql_where)->update($sql_update);
                                    if($res < 0)
                                    {   
                                        Db::rollback();
                                        $this->testLog(__METHOD__ . ":" . __LINE__ . "订单结算失败！参数:" . json_encode($sql_where));
                                    }
                                    else
                                    {
                                        $this->testLog(__METHOD__ . ":" . __LINE__ . "订单结算成功！参数:" . json_encode($sql_where));
                                    }
                                }   
                            }

                            //获取供应商价总计
                            $supplier_settlement = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$value['sNo'],'r_status' => 5])->sum('supplier_settlement');
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
                                $this->testLog(__METHOD__ . ":" . __LINE__ . "订单结算状态修改失败！参数:id" . $id);
                            }
                        }
                    }
                    else
                    {
                        if($value['z_price'] > 0)
                        {
                            $sql_u_where = array('store_id'=>$store_id,'id'=>$mch_id);
                            // 根据店铺ID，店铺商户余额减少，可取现金额增加
                            $sql_u_update = array('account_money'=>Db::raw('account_money-'.$money),'cashable_money'=>Db::raw('cashable_money+'.$money)); //49566
                            $res_u = Db::name('mch')->where($sql_u_where)->update($sql_u_update);
                            if($res_u < 0)
                            {   
                                Db::rollback();
                                $this->testLog(__METHOD__ . ":" . __LINE__ . "订单结算到账失败！参数:" . json_encode($sql_u_where));
                            }
                            else
                            {
                                $sql_where = array('store_id'=>$store_id,'r_sNo'=>$value['sNo'],'r_status'=>5);
                                $sql_update = array('settlement_type'=>1);
                                $res = Db::name('order_details')->where($sql_where)->update($sql_update);
                                if($res < 0)
                                {   
                                    Db::rollback();
                                    $this->testLog(__METHOD__ . ":" . __LINE__ . "订单结算失败！参数:" . json_encode($sql_where));
                                }
                                else
                                {
                                    $this->testLog(__METHOD__ . ":" . __LINE__ . "订单结算成功！参数:" . json_encode($sql_where));
                                }
                            }   
                        }

                        //获取供应商价总计
                        $supplier_settlement = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$value['sNo'],'r_status' => 5])->sum('supplier_settlement');
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
                            $this->testLog(__METHOD__ . ":" . __LINE__ . "订单结算状态修改失败！参数:id" . $id);
                        }

                        if( $otype == 'ZB')
                        { // 直播订单
                            $sNo_0_0 = $value['sNo'];
                            $sql_0_0 = "select living_room_id,anchor_id,commission from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo_0_0' ";
                            $r_0_0 = Db::query($sql_0_0);
                            if($r_0_0)
                            {
                                $living_room_id_0_0 = $r_0_0[0]['living_room_id']; // 直播间ID
                                $anchor_id_0_0 = $r_0_0[0]['anchor_id']; // 主播user_id
                                $commission_0_0 = $r_0_0[0]['commission']; // 佣金
                                
                                $sql_0_1 = "update lkt_living_commission set status = '100' where store_id = '$store_id' and living_id = '$living_room_id_0_0' and s_no = '$sNo_0_0'";
                                $r_0_1 = Db::execute($sql_0_1);
                                if($r_0_1 < 1)
                                {
                                    Db::rollback();
                                    $this->testLog(__METHOD__ . ":" . __LINE__ . "修改直播佣金表失败！sql:" . $sql_0_1);
                                }
    
                            }
                        }
                    }
                }
            }
        }

        Db::commit();
        return;
    }

    //供应商结算
    public function supplier_settlement($sNo,$money,$supplier_id,$mch_id)
    {
        $store_id = $this->store_id;
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
                $this->testLog(__METHOD__ . ":" . __LINE__ . "供应商余额修改失败！参数:supplier_id" . $supplier_id);
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
                $this->testLog(__METHOD__ . ":" . __LINE__ . "供应商添加资金记录失败！参数:supplier_id" . $supplier_id);
            }
            //更新店铺数据
            $sql3_where = array('store_id'=>$store_id,'id'=>$mch_id);
            $sql3_update = array('cashable_money'=>Db::raw('cashable_money-'.$money));
            $res3 = Db::name('mch')->where($sql3_where)->update($sql3_update);
            if($res3 < 0)
            {   
                Db::rollback();
                $this->testLog(__METHOD__ . ":" . __LINE__ . "订单结算到账失败！参数:" . json_encode($sql3_where));
            } 

            $r3 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('account_money,integral_money')->select()->toArray();
            if(!$r3)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . "查询店铺不存在 mchid $mch_id !";
                $this->testLog($Log_content);
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
                $this->testLog($Log_content);
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
                    $this->testLog(__METHOD__ . ":" . __LINE__ . "供应商运费余额修改失败！参数:supplier_id" . $supplier_id);
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
                    $this->testLog(__METHOD__ . ":" . __LINE__ . "供应商添加运费资金记录失败！参数:supplier_id" . $supplier_id);
                }

                //更新运费订单结算状态
                $res7 = SupplierOrderFrightModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'is_settlement'=>0])->update(['is_settlement'=>1]);
                if(!$res7)
                {
                    Db::rollback();
                    $this->testLog(__METHOD__ . ":" . __LINE__ . "更新运费订单结算状态失败！参数:sNo" . $sNo);
                }

                //更新店铺数据
                $sql8_where = array('store_id'=>$store_id,'id'=>$mch_id);
                $sql8_update = array('cashable_money'=>Db::raw('cashable_money-'.$supplier_freight));
                $res8 = Db::name('mch')->where($sql8_where)->update($sql8_update);
                if($res8 < 0)
                {   
                    Db::rollback();
                    $this->testLog(__METHOD__ . ":" . __LINE__ . "订单结算到账失败！参数:" . json_encode($sql8_where));
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
                    $this->testLog($Log_content);
                    Db::rollback();
                }
            }

        }
    }

    // 日志
    public function testLog($Log_content)
    {
        $time = date("Y-m-d");
        $lktlog = new LaiKeLogUtils();

        $lktlog->log("app/test/".$time.".log",$Log_content);
        return;
    }

    // 获取微信物流公司
    public function Obtain_WeChat_logistics_company()
    {
        $store_id = $this->store_id;

        $Tools = new Tools($store_id, 1);

        $kuaidi = array();
        $sql_e = "select id,kuaidi_name from lkt_express ";
        $r_e = Db::query($sql_e);
        if($r_e)
        {
            foreach($r_e as $k_e => $v_e)
            {
                $kuaidi[$v_e['id']] = $v_e['kuaidi_name'];
            }
        }
        
        $access_token = $Tools->get_access_token($store_id);

        if($access_token != '')
        {
            $url = "https://api.weixin.qq.com/cgi-bin/express/delivery/open_msg/get_delivery_list?access_token=" . $access_token;
            $res = $Tools->https_post($url);
            $data = json_decode($res,true);
            if($data['errcode'] == 0)
            {
                $delivery_list = $data['delivery_list'];

                foreach($delivery_list as $k => $v)
                {
                    $id = array_search($v['delivery_name'], $kuaidi);
                    if ($id !== false) 
                    {
                        $delivery_id = $v['delivery_id'];
                        $sql1 = "update lkt_express set wx_delivery_id = '$delivery_id' where id = '$id' ";
                        $r1 = Db::execute($sql1);
                    } 
                }
            }
        }

        return;
    }

    // 查询订单发货状态
    public function Get_WeChat_order_status()
    {
        $store_id = $this->store_id;
        $Tools = new Tools($store_id, 1);

        $access_token = $Tools->get_access_token($store_id);

        if($access_token != '')
        {
            $url = "https://api.weixin.qq.com/wxa/sec/order/get_order?access_token=" . $access_token;

            $config = LKTConfigInfo::getPayConfig($store_id,'mini_wechat');
            if($config != array())
            {
                $merchant_id = $config['mch_id'];
            }

            // 根据商城ID、微信小程序支付、待收货、配送订单
            $sql0 = "select real_sno from lkt_order where store_id = '$store_id' and pay = 'mini_wechat' and self_lifting = 0 ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                foreach($r0 as $k0 => $v0)
                {
                    $real_sno = $v0['real_sno'];
                    $array = array(
                        'merchant_id' => $merchant_id, // 支付下单商户的商户号，由微信支付生成并下发。
                        'merchant_trade_no' => $real_sno // 商户系统内部订单号，只能是数字、大小写字母`_-*`且在同一个商户号下唯一。
                    );

                    $res = $Tools->https_post($url,json_encode($array));
                    $data = json_decode($res,true);
                    if($data['errcode'] == 0)
                    {
                        $order = $data['order'];
                        $order_state = $order['order_state']; // 订单状态枚举：(1) 待发货；(2) 已发货；(3) 确认收货；(4) 交易完成；(5) 已退款。
                        if($order_state >= 3)
                        {
                            $sql1 = "update lkt_order set wx_order_status = '$order_state' where store_id = '$store_id' and pay = 'mini_wechat' and real_sno = '$real_sno' ";
                            $r1 = Db::execute($sql1);
                        }
                    }
                }
            }
        }

        return;
    }

    // 售后时间判断
    public function return_time()
    {
        $store_id = $this->store_id;
        $day = 1;
        $time = date("Y-m-d H:i:s");

        // 根据商城ID、退货退款、未结束售后，查询退款信息
        $sql0 = "select * from lkt_return_order where store_id = '$store_id' and re_type = 1 and r_type in (0,1,3) ";
        $r0 = Db::query($sql0);
        if($r0)
        { // 存在
            foreach($r0 as $k0 => $v0)
            {
                $id = $v0['id']; // 申请售后ID
                $re_time = $v0['re_time']; // 申请售后时间
                $audit_time = $v0['audit_time']; // 审核时间

                $array = array();
                $array['store_id'] = $store_id;
                $array['admin_name'] = '';
                $array['id'] = $id;
                $array['text'] = ''; // 拒绝理由
                $array['price'] = $v0['re_apply_money']; // 用户申请退款金额
                $array['mch_id'] = 0; // 店铺ID
                $array['express_id'] = ''; // 快递公司编号
                $array['courier_num'] = ''; // 快递单号
                $array['shop_id'] = 0; 
                $array['operator_id'] = 0; 
                $array['operator'] = ''; 
                $array['source'] = 100; 

                if($v0['r_type'] == 0)
                { // 店主未审核
                    $time0 = date("Y-m-d H:i:s",strtotime("+$day day",strtotime($re_time))); // 店主审核截止时间
                    if($time >= $time0)
                    { // 当前时间 超过或等于 店主审核截止时间
                        $array['m'] = 1; // 改为同意并让用户寄回
                    }
                }
                else if($v0['r_type'] == 1)
                { // 同意并让用户寄回
                    $time0 = date("Y-m-d H:i:s",strtotime("+$day day",strtotime($audit_time))); // 用户回寄截止时间
                    if($time >= $time0)
                    { // 当前时间 超过或等于 用户回寄截止时间
                        $array['m'] = 2; // 改为拒绝(退货退款)
                        $array['text'] = '未及时回寄，默认拒绝'; // 拒绝理由
                    }
                }
                else if($v0['r_type'] == 3)
                { // 用户已快递
                    $time0 = date("Y-m-d H:i:s",strtotime("+$day day",strtotime($re_time))); // 店主审核截止时间
                    if($time >= $time0)
                    { // 当前时间 超过或等于 店主审核截止时间
                        $array['m'] = 4; // 改为收到寄回商品,同意并退款
                    }
                }

                if(isset($array['m']))
                {
                    PluginUtils::invokeMethod('','refund',$array);
                }
            }
        }
    }
}
