<?php

namespace app\common;
use app\common;
use think\facade\Db;
use app\common\Plugin_order;
use app\common\GETUI\LaikePushTools;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Plugin\Plugin;
use app\common\Jurisdiction;
use app\common\ServerPath;
use app\common\third\logistics\LogisticsTool;
use app\common\Plugin\MchPublicMethod;
use app\common\LaiKeLogUtils;
use app\common\Plugin\CouponPublicMethod;
use app\common\WebHook;

use app\admin\model\ReturnOrderModel;
use app\admin\model\RecordModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\OrderConfigModel;
use app\admin\model\OrderModel;
use app\admin\model\CommentsModel;
use app\admin\model\UserModel;
use app\admin\model\MchModel;
use app\admin\model\GroupProductModel;
use app\admin\model\GroupOpenModel;
use app\admin\model\GroupConfigModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProductListModel;
use app\admin\model\SecondsConfigModel;
use app\admin\model\SecondsRecordModel;
use app\admin\model\SecondsTimeModel;
use app\admin\model\PaymentModel;
use app\admin\model\ExpressModel;
use app\admin\model\IntegralGoodsModel;
use app\admin\model\IntegralConfigModel;
use app\admin\model\MchStoreModel;
use app\admin\model\PreSellGoodsModel;
use app\admin\model\PreSellRecordModel;
use app\admin\model\SecondsActivityModel;
use app\admin\model\AuctionProductModel;
use app\admin\model\AuctionConfigModel;
use app\admin\model\PreSellConfigModel;
use app\admin\model\FlashsaleActivityModel;
use app\admin\model\AdminModel;

class DeliveryHelper
{
    // 统一发货
    public static function UnifiedShipment($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $list = $array['list']; // 发货参数
        $operator_shop_id = $array['shop_id']; // 操作人店铺
        $operator = $array['operator']; // 操作人昵称
        $operator_id = $array['operator_id']; // 操作人ID
        $operator_source = $array['source']; // 来源

        // 线上宝塔也是一样
//         self::f17track($array);
//         return;

        $time = date('Y-m-d H:i:s');

        $date_list = json_decode($list,true);
        $type = $date_list['type']; // 发货类型 1普通发货 2电子面单 3商家配送
        $express_id = $date_list['expressid']; // 物流公司id
        $courier_num = $date_list['courierNum']; // 物流单号
        $psyInfo = $date_list['psyInfo']; // 配送员信息
        $date_list = $date_list['orderList']; // 订单信息

        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();

        Db::startTrans();

        $status_num = 0; // 未发货订单详情数量
        $shipping_list = array();

        if($date_list != array())
        {
            $d_id0 = $date_list[0]['detailId']; // 订单详情ID
            $sql0 = "select r_sNo,p_name from lkt_order_details where id = '$d_id0' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $sNo = $r0[0]['r_sNo'];
                $cargo = $r0[0]['p_name'];

                $sql2 = "select user_id,mobile,pay,real_sno,self_lifting from lkt_order where store_id = '$store_id' and sNo = '$sNo' ";
                $r2 = Db::query($sql2);
                if($r2)
                {
                    $pay = $r2[0]['pay']; // 支付方式
                    $real_sno = $r2[0]['real_sno']; // 调起支付所用订单号
                    $user_id = $r2[0]['user_id'];
                    $mobile = $r2[0]['mobile']; // 收货人联系方式
                    $self_lifting = $r2[0]['self_lifting'];

                    $length = strlen($mobile) - 4;
                    $str = '';
                    for($i=1;$i<=$length;$i++)
                    {
                        $str .= '*';
                    }
                    $receiver_contact = $str . substr($mobile, -4);
                }

                if($type == 1 || $type == 2)
                { // 普通发货 或者 电子面单
                    $wx_delivery_id = '';
                    if($express_id != '')
                    {
                        $sql_express = "select wx_delivery_id from lkt_express where id = '$express_id' ";
                        $r_express = Db::query($sql_express);
                        if($r_express)
                        {
                            $wx_delivery_id = $r_express[0]['wx_delivery_id'];
                        }
                    }
                    else
                    {
                        Db::rollback();
                        $message = Lang('mch.23');
                        echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                        exit;
                    }
                    // 根据订单号，查询还未发货的订单详情数量
                    $sql1 = "select id from lkt_order_details where r_sNo = '$sNo' and r_status = 1 ";
                    $r1 = Db::query($sql1);
                    if($r1)
                    {
                        $status_num = count($r1); // 未发货订单详情数量
                    }
                    if(count($date_list) > 1)
                    {
                        $cargo .= '等';
                    }

                    if($type == 1)
                    { // 普通发货
                        foreach($date_list as $k => $v)
                        {
                            $d_id = $v['detailId']; // 订单详情ID
                            $d_num = $v['num']; // 发货数量

                            $sql_0 = "select p_name,num,deliver_num,express_id,courier_num from lkt_order_details where id = '$d_id' and r_sNo = '$sNo' ";
                            $r_0 = Db::query($sql_0);
                            if($r_0)
                            {
                                $num = $r_0[0]['num'];
                                $deliver_num = $r_0[0]['deliver_num']; // 已发数量

                                $express_id_0 = '';
                                if($r_0[0]['express_id'] == '')
                                {
                                    $express_id_0 = $express_id;
                                }
                                else
                                {
                                    $express_id_0 = $r_0[0]['express_id'] . ',' . $express_id;
                                }
                                $courier_num_0 = '';
                                if($r_0[0]['courier_num'] == '')
                                {
                                    $courier_num_0 = $courier_num;
                                }
                                else
                                {
                                    $courier_num_0 = $r_0[0]['courier_num'] . ',' . $courier_num;
                                }

                                if($d_num == ($num - $deliver_num))
                                { // 发货数量 = 购买数量
                                    $con = " r_status = 2,express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' ";
                                    $status_num--;
                                }
                                else
                                {
                                    $con = " express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' ";
                                }

                                $sqld = "update lkt_order_details set $con where id = '$d_id' and r_status = '1'";
                                $rd = Db::execute($sqld);
                                if ($rd < 1)
                                {
                                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单详情状态失败！sql:" . $sqld);
                                    Db::rollback();
                                    $Jurisdiction->admin_record($store_id, $operator, ' 订单详情ID为' . $d_id . ' 的订单发货失败',7,$operator_source,$operator_shop_id,$operator_id);
                                    $message = Lang('admin_order.0');
                                    echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                                    exit;
                                }

                                $sql3 = "insert into lkt_express_delivery(store_id,sNo,order_details_id,express_id,courier_num,num,deliver_time) value ('$store_id','$sNo','$d_id','$express_id','$courier_num','$d_num','$time')";
                                $r3 = Db::execute($sql3);
                                if ($rd < 1)
                                {
                                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加快递记录表失败！sql:" . $sql3);
                                    Db::rollback();
                                    $message = Lang('admin_order.0');
                                    echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                                    exit;
                                }

                                $shipping_list[] = array('tracking_no'=>$courier_num,'express_company'=>$wx_delivery_id,'item_desc'=>$r_0[0]['p_name'],'contact'=>array('receiver_contact'=>$receiver_contact));
                            }
                        }

                        if($pay == 'mini_wechat')
                        {
                            $array = array('store_id'=>$store_id,'real_sno'=>$real_sno,'user_id'=>$user_id,'shipping_list'=>$shipping_list,'lktlog'=>$lktlog);
                            // 发货信息录入接口
                            self::Delivery_information_input_interface($array);
                        }
                    }
                    else
                    { // 电子面单
                        $array = array('store_id'=>$store_id,'sNo'=>$sNo,'express_id'=>$express_id,'cargo'=>$cargo,'source'=>$operator_source);
                        $data = self::Delivery_by_waybill($array);
                        if(isset($data['code']))
                        { // 面单发货失败
                            Db::rollback();
                            echo json_encode(array('code'=>109,'message'=>$data['message']));
                            exit;
                        }
                        $courier_num = $data['courier_num']; // 快递单号
                        $childNum = $data['childNum']; // 子单号
                        $returnNum = $data['returnNum']; // 回单号
                        $label = $data['label']; // 面单短链
                        $kdComOrderNum = $data['kdComOrderNum']; // 快递公司订单号
                        $subtable_id = $data['subtable_id']; // 快递公司订单号
                        $mch_id = $data['mch_id']; // 店铺ID
            
                        foreach($date_list as $k_list => $v_list)
                        {
                            $d_id = $v_list['detailId']; // 订单详情ID
                            $d_num = $v_list['num']; // 发货数量

                            $sql_0 = "select p_name,num,deliver_num,express_id,courier_num from lkt_order_details where id = '$d_id' and r_sNo = '$sNo' ";
                            $r_0 = Db::query($sql_0);
                            if($r_0)
                            {
                                $num = $r_0[0]['num'];
                                $deliver_num = $r_0[0]['deliver_num']; // 已发数量
            
                                $express_id_0 = '';
                                if($r_0[0]['express_id'] == '')
                                {
                                    $express_id_0 = $express_id;
                                }
                                else
                                {
                                    $express_id_0 = $r_0[0]['express_id'] . ',' . $express_id;
                                }
                                $courier_num_0 = '';
                                if($r_0[0]['courier_num'] == '')
                                {
                                    $courier_num_0 = $courier_num;
                                }
                                else
                                {
                                    $courier_num_0 = $r_0[0]['courier_num'] . ',' . $courier_num;
                                }
            
                                if($d_num == ($num - $deliver_num))
                                { // 发货数量 = 购买数量
                                    $con = " r_status = 2,express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' ";
                                    $status_num--;
                                }
                                else
                                {
                                    $con = " express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' ";
                                }
            
                                $sqld = "update lkt_order_details set $con where id = '$d_id' and r_status = '1'";
                                $rd = Db::execute($sqld);
                                if ($rd < 1)
                                {
                                    $Jurisdiction->admin_record($store_id, $operator, ' 面单发货,修改订单详情失败，ID为 ' . $d_id,2,$operator_source,$operator_shop_id,$operator_id);
                                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单详情状态失败！sql:" . $sqld);
                                    Db::rollback();
                                    $message = Lang('admin_order.0');
                                    echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                                    exit;
                                }
            
                                $sql3 = "insert into lkt_express_delivery(store_id,sNo,order_details_id,express_id,courier_num,num,deliver_time,childNum,returnNum,label,kdComOrderNum,is_status,subtable_id) value ('$store_id','$sNo','$d_id','$express_id','$courier_num','$d_num','$time','$childNum','$returnNum','$label','$kdComOrderNum',0,'$subtable_id')";
                                $r3 = Db::execute($sql3);
                                if ($rd < 1)
                                {
                                    $Jurisdiction->admin_record($store_id, $operator, ' 面单发货,添加快递记录表失败',2,$operator_source,$operator_shop_id,$operator_id);
                                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加快递记录表失败！sql:" . $sql3);
                                    Db::rollback();
                                    $message = Lang('admin_order.0');
                                    echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                                    exit;
                                }
                            }
                        }
                    }

                    if($status_num == 0)
                    { // 未发货订单详情数量 为 0 （该订单全部发货）
                        $sql = OrderModel::where('sNo',$sNo)->find();
                        $sql->status = 2;
                        $rl = $sql->save();
                        if (!$rl)
                        {
                            $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "发货失败！sNo:" . $sNo);
                            Db::rollback();
                            $Jurisdiction->admin_record($store_id, $operator, ' 将订单号为 ' . $sNo . ' 的订单发货失败',7,$operator_source,$operator_shop_id,$operator_id);
                            $message = Lang('admin_order.1');
                            echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                            exit;
                        }
                        else
                        {
                            $msg_title = "【" . $sNo . "】订单发货啦！";
                            $msg_content = Lang("admin_order.2");
                            
                            LaiKeLogUtils::lktLog($msg_title);
                            /**发货成功通知*/
                            $pusher = new LaikePushTools();
                            $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, $operator);

                            $msgsql = "select o.id,o.user_id,o.sNo,d.p_name,o.name,o.address from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.r_sNo ='$sNo'";
                            $msgres = Db::query($msgsql);
                            $msgres = (object)$msgres[0];
                            $openid = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('wx_id')->select()->toArray();
                            if(!empty($openid))
                            {
                                $msgres->uid = $openid[0]['wx_id'];
                                $compres = ExpressModel::where('id',$express_id)->field('kuaidi_name')->select()->toArray();
                                if (!empty($compres))
                                {
                                    $msgres->company = $compres[0]['kuaidi_name'];
                                }

                                if($msgres->uid)
                                {
                                    self::sendWXTopicMsg($msgres, $sNo, $courier_num, $store_id);
                                }
                            }
                        }
                    }
                }
                else
                { // 商家配送
                    $courier_name = $psyInfo['name'];
                    $phone = $psyInfo['tel'];
                    if($date_list != array())
                    {
                        $d_id0 = $date_list[0]['detailId']; // 订单详情ID
                        $sql0 = "select r_sNo from lkt_order_details where id = '$d_id0' ";
                        $r0 = Db::query($sql0);
                        if($r0)
                        {
                            $sNo = $r0[0]['r_sNo'];

                            $sql2 = "select user_id,mobile,pay,real_sno,self_lifting from lkt_order where store_id = '$store_id' and sNo = '$sNo' ";
                            $r2 = Db::query($sql2);
                            if($r2)
                            {
                                $pay = $r2[0]['pay']; // 支付方式
                                $real_sno = $r2[0]['real_sno']; // 调起支付所用订单号
                                $user_id = $r2[0]['user_id'];
                                $mobile = $r2[0]['mobile']; // 收货人联系方式
                                $self_lifting = $r2[0]['self_lifting'];

                                if($self_lifting == 2)
                                {
                                    $sql1 = "select store_self_delivery from lkt_order_details where r_sNo = '$sNo' and r_status = 1 ";
                                    $r1 = Db::query($sql1);
                                    if($r1)
                                    {
                                        $store_self_delivery = $r1[0]['store_self_delivery'];

                                        $sql2 = "select * from lkt_self_delivery_info where id = '$store_self_delivery' ";
                                        $r2 = Db::query($sql2);
                                        if($r2)
                                        {
                                            $sql3_0 = "update lkt_order set status = 2 where sNo = '$sNo' ";
                                            $r3_0 = Db::execute($sql3_0);

                                            $sql3_0 = "update lkt_order_details set r_status = 2,deliver_time = '$time' where r_sNo = '$sNo' ";
                                            $r3_0 = Db::execute($sql3_0);

                                            $sql3 = "update lkt_self_delivery_info set phone = '$phone',courier_name = '$courier_name' where id = '$store_self_delivery' ";
                                            $r3 = Db::execute($sql3);
                                            Db::commit();
                                        }
                                        else
                                        {
                                            Db::rollback();
                                            $Jurisdiction->admin_record($store_id, $operator, ' 商家配送失败，订单号为' . $sNo,7,$operator_source,$operator_shop_id,$operator_id);
                                            $message = Lang("Parameter error");
                                            echo json_encode(array('code' => 109, 'message' => $message));
                                            exit();
                                        }
                                    } 
                                }
                            }
                        }
                        else
                        {
                            $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "发货失败！订单详情ID:" . $d_id0);
                            Db::rollback();
                            $Jurisdiction->admin_record($store_id, $operator, ' 订单详情ID为' . $d_id0 . ' 的订单发货失败',7,$operator_source,$operator_shop_id,$operator_id);
                            $message = Lang('admin_order.1');
                            echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                            exit;
                        }
                    }
                }
                
                $Jurisdiction->admin_record($store_id, $operator, ' 将订单号为 ' . $sNo . ' 的订单发货 ',7,$operator_source,$operator_shop_id,$operator_id);
            }
            else
            {
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "发货失败！订单详情ID:" . $d_id0);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, ' 订单详情ID为' . $d_id0 . ' 的订单发货失败',7,$operator_source,$operator_shop_id,$operator_id);
                $message = Lang('admin_order.1');
                echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                exit;
            }
        }

        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message));
        exit;
    }

    // 17track发货
    public static function f17track($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $list = $array['list']; // 发货参数
        $operator_shop_id = $array['shop_id']; // 操作人店铺
        $operator = $array['operator']; // 操作人昵称
        $operator_id = $array['operator_id']; // 操作人ID
        $operator_source = $array['source']; // 来源

        $time = date('Y-m-d H:i:s');

        $date_list = json_decode($list,true);
        $type = $date_list['type']; // 发货类型 1普通发货 2电子面单 3商家配送
        $express_id = $date_list['expressid']; // 物流公司id
        $courier_num = $date_list['courierNum']; // 物流单号
        $psyInfo = $date_list['psyInfo']; // 配送员信息
        $date_list = $date_list['orderList']; // 订单信息

        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();

        Db::startTrans();

        $status_num = 0; // 未发货订单详情数量
        $mobile = '';

        if($date_list != array())
        {
            $d_id0 = $date_list[0]['detailId']; // 订单详情ID
            $sql0 = "select user_id,r_sNo,p_name from lkt_order_details where id = '$d_id0' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $user_id = $r0[0]['user_id'];
                $sNo = $r0[0]['r_sNo'];

                // 根据订单号，查询还未发货的订单详情数量
                $sql1 = "select id from lkt_order_details where r_sNo = '$sNo' and r_status = 1 ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    $status_num = count($r1); // 未发货订单详情数量
                }
                
                $sql1 = "select mobile from lkt_order where sNo = '$sNo' ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    $mobile = substr($r1[0]['mobile'], -4); // 联系人电话
                }

                foreach($date_list as $k => $v)
                {
                    $d_id = $v['detailId']; // 订单详情ID
                    $d_num = $v['num']; // 发货数量

                    $sql_0 = "select p_name,num,deliver_num,express_id,courier_num from lkt_order_details where id = '$d_id' and r_sNo = '$sNo' ";
                    $r_0 = Db::query($sql_0);
                    if($r_0)
                    {
                        $num = $r_0[0]['num'];
                        $deliver_num = $r_0[0]['deliver_num']; // 已发数量

                        $express_id_0 = '';
                        if($r_0[0]['express_id'] == '')
                        {
                            $express_id_0 = $express_id;
                        }
                        else
                        {
                            $express_id_0 = $r_0[0]['express_id'] . ',' . $express_id;
                        }
                        $courier_num_0 = '';
                        if($r_0[0]['courier_num'] == '')
                        {
                            $courier_num_0 = $courier_num;
                        }
                        else
                        {
                            $courier_num_0 = $r_0[0]['courier_num'] . ',' . $courier_num;
                        }

                        if($d_num == ($num - $deliver_num))
                        { // 发货数量 = 购买数量
                            $con = " r_status = 2,express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' ";
                            $status_num--;
                        }
                        else
                        {
                            $con = " express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' ";
                        }

                        $sqld = "update lkt_order_details set $con where id = '$d_id' and r_status = '1'";
                        $rd = Db::execute($sqld);
                        if ($rd < 1)
                        {
                            $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单详情状态失败！sql:" . $sqld);
                            Db::rollback();
                            $Jurisdiction->admin_record($store_id, $operator, ' 订单详情ID为' . $d_id . ' 的订单发货失败',7,$operator_source,$operator_shop_id,$operator_id);
                            $message = Lang('admin_order.0');
                            echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                            exit;
                        }

                        $sql3 = "insert into lkt_express_delivery(store_id,sNo,order_details_id,express_id,courier_num,num,deliver_time,logistics_method) value ('$store_id','$sNo','$d_id','$express_id','$courier_num','$d_num','$time',1)";
                        $r3 = Db::execute($sql3);
                        if ($rd < 1)
                        {
                            $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加快递记录表失败！sql:" . $sql3);
                            Db::rollback();
                            $message = Lang('admin_order.0');
                            echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                            exit;
                        }
                    }
                }

                if($status_num == 0)
                { // 未发货订单详情数量 为 0 （该订单全部发货）
                    $sql = OrderModel::where('sNo',$sNo)->find();
                    $sql->status = 2;
                    $rl = $sql->save();
                    if (!$rl)
                    {
                        $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "发货失败！sNo:" . $sNo);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, ' 将订单号为 ' . $sNo . ' 的订单发货失败',7,$operator_source,$operator_shop_id,$operator_id);
                        $message = Lang('admin_order.1');
                        echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                        exit;
                    }
                    else
                    {
                        $msg_title = "【" . $sNo . "】订单发货啦！";
                        $msg_content = Lang("admin_order.2");
                        
                        LaiKeLogUtils::lktLog($msg_title);
                        /**发货成功通知*/
                        $pusher = new LaikePushTools();
                        $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, $operator);

                        $msgsql = "select o.id,o.user_id,o.sNo,d.p_name,o.name,o.address from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.r_sNo ='$sNo'";
                        $msgres = Db::query($msgsql);
                        $msgres = (object)$msgres[0];
                        $openid = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('wx_id')->select()->toArray();
                        if(!empty($openid))
                        {
                            $msgres->uid = $openid[0]['wx_id'];
                            $compres = ExpressModel::where('id',$express_id)->field('kuaidi_name')->select()->toArray();
                            if (!empty($compres))
                            {
                                $msgres->company = $compres[0]['kuaidi_name'];
                            }

                            if($msgres->uid)
                            {
                                self::sendWXTopicMsg($msgres, $sNo, $courier_num, $store_id);
                            }
                        }
                    }
                }
            }

            WebHook::SendRequest($courier_num,$store_id,$express_id,$mobile);
        }

        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message));
        exit;
    }

    // 后台发货-按商品数量发货（管理后台）（新）
    public static function adminDelivery_x($array)
    {
        $store_id = $array['store_id'];
        $id = $array['id'];
        $express_id = $array['express_id'];
        $courier_num = $array['courier_num'];
        $operator_shop_id = $array['shop_id'];
        $operator = $array['operator'];
        $operator_source = $array['source'];
        $admin_list = $array['admin_list'];
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }

        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();

        $admin_name = '';
        if($operator_source == 1)
        {
            $admin_name = $admin_list['name'];
        }
        else if($operator_source == 12)
        {
            $admin_name = $admin_list['supplier_name'];
        }
        $time = date('Y-m-d H:i:s', time());

        Db::startTrans();

        $date_list = json_decode($id,true);
        $status_num = 0; // 未发货订单详情数量
        if($date_list != array())
        {
            $d_id0 = $date_list[0]['detailId']; // 订单详情ID
            $sql0 = "select r_sNo from lkt_order_details where id = '$d_id0' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $sNo = $r0[0]['r_sNo'];
                // 根据订单号，查询还未发货的订单详情数量
                $sql1 = "select id from lkt_order_details where r_sNo = '$sNo' and r_status = 1 ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    $status_num = count($r1); // 未发货订单详情数量
                }
            }

            $shipping_list = array();

            foreach($date_list as $k_list => $v_list)
            {
                $d_id = $v_list['detailId']; // 订单详情ID
                $d_num = $v_list['num']; // 发货数量
                $sql_p = "select o.id,o.user_id,o.sNo,o.pay,o.real_sno,o.mobile,d.p_name,d.p_id,d.sid,d.num,o.name,o.address,d.express_id,d.courier_num,d.deliver_num from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.id = '$d_id' ";
                $res_p = Db::query($sql_p);
                if($res_p)
                {
                    $pay = $res_p[0]['pay']; // 支付方式
                    $real_sno = $res_p[0]['real_sno']; // 调起支付所用订单号
                    $num = $res_p[0]['num'];
                    $user_id = $res_p[0]['user_id'];
                    $deliver_num = $res_p[0]['deliver_num']; // 已发数量
                    $mobile = $res_p[0]['mobile']; // 收货人联系方式

                    $length = strlen($mobile) - 4;
                    $str = '';
                    for($i=1;$i<=$length;$i++)
                    {
                        $str .= '*';
                    }
                    $receiver_contact = $str . substr($mobile, -4);

                    $express_id_0 = '';
                    if($res_p[0]['express_id'] == '')
                    {
                        $express_id_0 = $express_id;
                    }
                    else
                    {
                        $express_id_0 = $res_p[0]['express_id'] . ',' . $express_id;
                    }
                    $courier_num_0 = '';
                    if($res_p[0]['courier_num'] == '')
                    {
                        $courier_num_0 = $courier_num;
                    }
                    else
                    {
                        $courier_num_0 = $res_p[0]['courier_num'] . ',' . $courier_num;
                    }

                    if($d_num == ($num - $deliver_num))
                    { // 发货数量 = 购买数量
                        $con = " r_status = 2,express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' ";
                        $status_num--;
                    }
                    else
                    {
                        $con = " express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' ";
                    }

                    $sqld = "update lkt_order_details set $con where id = '$d_id' and r_status = '1'";
                    $rd = Db::execute($sqld);
                    if ($rd < 1)
                    {
                        $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单详情状态失败！sql:" . $sqld);
                        Db::rollback();
                        $message = Lang('admin_order.0');
                        echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                        exit;
                    }

                    $sql3 = "insert into lkt_express_delivery(store_id,sNo,order_details_id,express_id,courier_num,num,deliver_time) value ('$store_id','$sNo','$d_id','$express_id','$courier_num','$d_num','$time')";
                    $r3 = Db::execute($sql3);
                    if ($rd < 1)
                    {
                        $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加快递记录表失败！sql:" . $sql3);
                        Db::rollback();
                        $message = Lang('admin_order.0');
                        echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                        exit;
                    }

                    $wx_delivery_id = '';
                    $sql4 = "select wx_delivery_id from lkt_express where id = '$express_id' ";
                    $r4 = Db::query($sql4);
                    if($r4)
                    {
                        $wx_delivery_id = $r4[0]['wx_delivery_id'];
                    }

                    $shipping_list[] = array('tracking_no'=>$courier_num,'express_company'=>$wx_delivery_id,'item_desc'=>$res_p[0]['p_name'],'contact'=>array('receiver_contact'=>$receiver_contact));
                }
            }

            if($pay == 'mini_wechat')
            {
                $array = array('store_id'=>$store_id,'real_sno'=>$real_sno,'user_id'=>$user_id,'shipping_list'=>$shipping_list,'lktlog'=>$lktlog);
                // 发货信息录入接口
                self::Delivery_information_input_interface($array);
            }

            if($status_num == 0)
            { // 未发货订单详情数量 为 0 （该订单全部发货）
                $sql = OrderModel::where('sNo',$sNo)->find();
                $sql->status = 2;
                $rl = $sql->save();
                if (!$rl)
                {
                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "发货失败！sNo:" . $sNo);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, ' 将订单号为 ' . $sNo . ' 的订单发货失败',7,$operator_source,$operator_shop_id,$operator_id);
                    $message = Lang('admin_order.1');
                    echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                    exit;
                }
                else
                {
                    $msg_title = "【" . $sNo . "】订单发货啦！";
                    $msg_content = Lang("admin_order.2");
                    
                    LaiKeLogUtils::lktLog($msg_title);
                    /**发货成功通知*/
                    $pusher = new LaikePushTools();
                    $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, $admin_name);

                    $msgsql = "select o.id,o.user_id,o.sNo,d.p_name,o.name,o.address from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.r_sNo ='$sNo'";
                    $msgres = Db::query($msgsql);
                    $msgres = (object)$msgres[0];
                    $openid = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('wx_id')->select()->toArray();
                    if(!empty($openid))
                    {
                        $msgres->uid = $openid[0]['wx_id'];
                        $compres = ExpressModel::where('id',$express_id)->field('kuaidi_name')->select()->toArray();
                        if (!empty($compres))
                        {
                            $msgres->company = $compres[0]['kuaidi_name'];
                        }

                        if($msgres->uid)
                        {
                            self::sendWXTopicMsg($msgres, $sNo, $courier_num, $store_id);
                        }
                    }
                }
            }
            
            $Jurisdiction->admin_record($store_id, $operator, ' 将订单号为 ' . $sNo . ' 的订单发货 ',7,$operator_source,$operator_shop_id,$operator_id);
        }

        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message));
        exit;
    }

    // 后台发货-商家配送订单-发货
    public static function deliverySaveForStoreSelf($array)
    {
        $store_id = $array['store_id'];
        $sNo = $array['sNo'];
        $courier_name = $array['courier_name'];
        $phone = $array['phone'];
        $operator_shop_id = $array['shop_id'];
        $operator = $array['operator'];
        $operator_source = $array['source'];
        $admin_list = $array['admin_list'];
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }

        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();

        $admin_name = '';
        if($operator_source == 1)
        {
            $admin_name = $admin_list['name'];
        }
        else if($operator_source == 12)
        {
            $admin_name = $admin_list['supplier_name'];
        }
        $time = date('Y-m-d H:i:s', time());

        Db::startTrans();

        $self_lifting = 0;
        $r0 = OrderModel::where(['sNo'=>$sNo])->field('otype,self_lifting')->select()->toArray();
        if ($r0)
        {
            $otype = $r0[0]['otype'];
            $self_lifting = $r0[0]['self_lifting'];
        }

        if($self_lifting == 2)
        {
            $sql1 = "select store_self_delivery from lkt_order_details where r_sNo = '$sNo' and r_status = 1 ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $store_self_delivery = $r1[0]['store_self_delivery'];

                $sql2 = "select * from lkt_self_delivery_info where id = '$store_self_delivery' ";
                $r2 = Db::query($sql2);
                if($r2)
                {
                    $sql3_0 = "update lkt_order set status = 2 where sNo = '$sNo' ";
                    $r3_0 = Db::execute($sql3_0);

                    $sql3_0 = "update lkt_order_details set r_status = 2,deliver_time = '$time' where r_sNo = '$sNo' ";
                    $r3_0 = Db::execute($sql3_0);

                    $sql3 = "update lkt_self_delivery_info set phone = '$phone',courier_name = '$courier_name' where id = '$store_self_delivery' ";
                    $r3 = Db::execute($sql3);
                    Db::commit();
                }
                else
                {
                    Db::rollback();
                    $message = Lang("Parameter error");
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit();
                }
            } 
            else
            {
                Db::rollback();
                $message = Lang("Parameter error");
                echo json_encode(array('code' => 109, 'message' => $message));
                exit();
            }
        }

        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message));
        exit;
    }

    // 店铺发货-按商品数量发货(PC店铺、移动端店铺)（新）
    public static function frontDelivery_x($action)
    {
        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();
        $store_id = $action['store_id'] ;
        $user_id = $action['user_id'] ;
        $orderList_id = $action['orderList_id'] ;
        $sNo = $action['sNo'] ;
        $express_id = $action['express_id'] ;
        $courier_num = $action['courier_num'] ;

        $shop_id = '';
        if(isset($action['shop_id']))
        {
            $shop_id = $action['shop_id'];
        }
        $operator_id = 0;
        if(isset($action['operator_id']))
        {
            $operator_id = $action['operator_id'];
        }
        $operator = '';
        if(isset($action['operator']))
        {
            $operator = $action['operator'];
        }
        $source = 1;
        if(isset($action['source']))
        {
            $source = $action['source'];
        }
        $courier_name = '';
        if(isset($action['courier_name']))
        {
            $courier_name = $action['courier_name'];
        }
        $phone = '';
        if(isset($action['phone']))
        {
            $phone = $action['phone'];
        }

        Db::startTrans();
        $time = date('Y-m-d H:i:s', time());

        $self_lifting = 0;
        $r0 = OrderModel::where(['sNo'=>$sNo])->field('otype,self_lifting')->select()->toArray();
        if ($r0)
        {
            $otype = $r0[0]['otype'];
            $self_lifting = $r0[0]['self_lifting'];
        }
        
        if($self_lifting == 2)
        {
            $sql1 = "select store_self_delivery from lkt_order_details where r_sNo = '$sNo' and r_status = 1 ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $store_self_delivery = $r1[0]['store_self_delivery'];

                $sql2 = "select * from lkt_self_delivery_info where id = '$store_self_delivery' ";
                $r2 = Db::query($sql2);
                if($r2)
                {
                    $sql3_0 = "update lkt_order set status = 2 where sNo = '$sNo' ";
                    $r3_0 = Db::execute($sql3_0);

                    $sql3_0 = "update lkt_order_details set r_status = 2,deliver_time = '$time' where r_sNo = '$sNo' ";
                    $r3_0 = Db::execute($sql3_0);

                    $sql3 = "update lkt_self_delivery_info set phone = '$phone',courier_name = '$courier_name' where id = '$store_self_delivery' ";
                    $r3 = Db::execute($sql3);
                    Db::commit();
                }
                else
                {
                    Db::rollback();
                    $message = Lang("Parameter error");
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit();
                }
            } 
            else
            {
                Db::rollback();
                $message = Lang("Parameter error");
                echo json_encode(array('code' => 109, 'message' => $message));
                exit();
            }
        }
        else
        {
            $date_list = json_decode($orderList_id,true);
            $status_num = 0; // 未发货订单详情数量
            // 根据订单号，查询还未发货的订单详情数量
            $sql1 = "select id from lkt_order_details where r_sNo = '$sNo' and r_status = 1 ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $status_num = count($r1); // 未发货订单详情数量
            }

            if($date_list != array())
            {
                foreach($date_list as $k_list => $v_list)
                {
                    $d_id = $v_list['detailId']; // 订单详情ID
                    $d_num = $v_list['num']; // 发货数量

                    $sql_p = "select o.id,o.user_id,o.sNo,d.p_name,d.p_id,d.sid,d.num,o.name,o.address,d.express_id,d.courier_num,d.deliver_num from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.id = '$d_id' ";
                    $res_p = Db::query($sql_p);
                    if($res_p)
                    {
                        $num = $res_p[0]['num'];
                        $user_id = $res_p[0]['user_id'];
                        $deliver_num = $res_p[0]['deliver_num']; // 已发数量

                        $express_id_0 = '';
                        if($res_p[0]['express_id'] == '')
                        {
                            $express_id_0 = $express_id;
                        }
                        else
                        {
                            $express_id_0 = $res_p[0]['express_id'] . ',' . $express_id;
                        }
                        $courier_num_0 = '';
                        if($res_p[0]['courier_num'] == '')
                        {
                            $courier_num_0 = $courier_num;
                        }
                        else
                        {
                            $courier_num_0 = $res_p[0]['courier_num'] . ',' . $courier_num;
                        }

                        if($d_num == ($num - $deliver_num))
                        { // 发货数量 = 购买数量 - 已发数量
                            $con = " r_status = 2,express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = '$d_num' ";
                            $status_num--;
                        }
                        else
                        {
                            $con = " express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' ";
                        }

                        $sqld = "update lkt_order_details set $con where id = '$d_id' and r_status = '1'";
                        $rd = Db::execute($sqld);
                        if ($rd < 1)
                        {
                            Db::rollback();

                            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货失败,订单号为' . $sNo . '！';
                            $lktlog->log("app/mch.log",$Log_content);

                            $Jurisdiction->admin_record($store_id, $operator, '将订单详情ID：' . $d_id . '，进行了发货失败',2,$source,$shop_id,$operator_id);
                            
                            $message = Lang('Busy network');
                            echo json_encode(array('code' => 103, 'message' => $message));
                            exit();
                        }

                        $sql3 = "insert into lkt_express_delivery(store_id,sNo,order_details_id,express_id,courier_num,num,deliver_time) value ('$store_id','$sNo','$d_id','$express_id','$courier_num','$d_num','$time')";
                        $r3 = Db::execute($sql3);
                        if ($rd < 1)
                        {
                            $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加快递记录表失败！sql:" . $sql3);
                            Db::rollback();

                            $Jurisdiction->admin_record($store_id, $operator, '将订单详情ID：' . $d_id . '，进行了发货失败',2,$source,$shop_id,$operator_id);

                            $message = Lang('admin_order.0');
                            echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                            exit;
                        }
                    }
                }
            }

            if($status_num == 0)
            { // 未发货订单详情数量 为 0 （该订单全部发货）
                $sql = OrderModel::where('sNo',$sNo)->find();
                $sql->status = 2;
                $rl = $sql->save();
                if (!$rl)
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货失败,订单号为' . $sNo . '！';
                    $lktlog->log("app/mch.log",$Log_content);

                    $Jurisdiction->admin_record($store_id, $operator, '将订单号：' . $sNo . '，进行了发货失败',2,$source,$shop_id,$operator_id);

                    $message = Lang('Busy network');
                    echo json_encode(array('code' => 103, 'message' => $message));
                    exit();
                }
                else
                {
                    $msg_title = "【" . $sNo . "】订单发货啦！";
                    $msg_content = Lang("admin_order.2");
                    LaiKeLogUtils::lktLog($msg_title);
                    /**发货成功通知*/
                    $pusher = new LaikePushTools();
                    $pusher->pushMessage($user_id,$msg_title, $msg_content, $store_id, $user_id);

                    $msgsql = "select o.id,o.user_id,o.sNo,d.p_name,o.name,o.address from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.r_sNo ='$sNo'";
                    $msgres = Db::query($msgsql);
                    $msgres = (object)$msgres[0];

                    $openid = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('wx_id')->select()->toArray();
                    if(!empty($openid))
                    {
                        $msgres->uid = $openid[0]['wx_id'];
                        $compres = ExpressModel::where(['id'=>$express_id])->field('kuaidi_name')->select()->toArray();
                        if (!empty($compres))
                        {
                            $msgres->company = $compres[0]['kuaidi_name'];
                        }

                        if($msgres->uid)
                        {
                            // $action->sendWXTopicMsg($msgres, $sNo, $courier_num, $store_id);
                            self::sendWXTopicMsg($msgres, $sNo, $courier_num, $store_id);
                        }
                    }
                }
            }

            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货成功,订单号为' . $sNo . '！';
            $lktlog->log("app/mch.log",$Log_content);
            Db::commit();

            $Jurisdiction->admin_record($store_id, $operator, '将订单详情ID：' . $d_id . '，进行了发货',2,$source,$shop_id,$operator_id);
        }

        $message = Lang("Success");
        echo json_encode(array('code' => 200, 'message' => $message));
        exit();
    }

    // 订单列表（移动店铺端）
    public static function a_mch_order_index($action)
    {
        $store_id = $action['store_id'];
        $shop_id = $action['shop_id'];
        $order_type = $action['order_type'];
        $keyword = $action['keyword'];
        $page = $action['page'];
        $user_id = $action['user_id'];
        $start = $action['start'];
        $end = $action['end'];
        $platform_activities_id = $action['platform_activities_id'];
        $order_headr_type = '';
        if(isset($action['order_headr_type']))
        {
            $order_headr_type = $action['order_headr_type'];
        }
        $is_supplier = 0;
        if(isset($action['is_supplier']))
        {
            $is_supplier = $action['is_supplier'];
        }
        $list = array();
        $list1 = array();

        if($order_headr_type == 'VI')
        {
            $order_headr_type1 = 'VI';
        }
        else if($order_headr_type == 'ZB')
        {
            $order_headr_type1 = 'ZB';
        }
        else if($order_headr_type == 'FS')
        {
            $order_headr_type1 = 'FS';
        }
        else
        {
            $order_headr_type1 = 'GM';
        }

        $con_supplier = "";
        if($is_supplier == 1)
        { // 移动店铺端供应商
            $con_supplier = " and a.supplier_id != 0 ";
            $con_supplier1 = " and o.supplier_id != 0 ";
        }
        else
        { // 移动店铺端
            $con_supplier = " and a.supplier_id = 0 ";
            $con_supplier1 = " and o.supplier_id = 0 ";
        }
        
        if(empty($keyword))
        {
            $sql_1 = "select count(id) as payment_num from lkt_order as a where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and a.status = 0 and a.otype = '$order_headr_type1' $con_supplier ";

            $sql_2 = "select count(id) as send_num  from lkt_order as a where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and ((a.status = 1 and a.self_lifting = '0') or (a.status = 2 and a.self_lifting = '1')) and a.otype = '$order_headr_type1' $con_supplier ";

            $sql_3 = "select count(d.id) as return_num from lkt_return_order as d left join lkt_order_details as a on d.p_id = a.id left join lkt_product_list as b on a.p_id = b.id left join lkt_configure as c on a.sid = c.id where a.store_id = '$store_id' and b.mch_id = '$shop_id' and d.r_type in (0,1,3) and a.r_sNo like '$order_headr_type1%' $con_supplier ";
        }
        else
        {
            $keyword_0 = Tools::FuzzyQueryConcatenation($keyword);
            $sql_1 = "select count(id) as payment_num from lkt_order as a where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and a.status = 0 and a.sNo like $keyword_0 and a.otype = '$order_headr_type1' $con_supplier ";

            $sql_2 = "select count(id) as send_num from lkt_order as a where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and a.sNo like $keyword_0 and ((a.status = 1 and a.self_lifting = '0') or (a.status = 2 and a.self_lifting = '1')) and a.otype = '$order_headr_type1' $con_supplier ";

            $sql_3 = "select count(d.id) as return_num from lkt_return_order as d left join lkt_order_details as a on d.p_id = a.id left join lkt_product_list as b on a.p_id = b.id left join lkt_configure as c on a.sid = c.id where a.store_id = '$store_id' and b.mch_id = '$shop_id' and a.r_sNo like $keyword_0 and d.r_type in (0,1,3) and a.r_sNo like '$order_headr_type1%' $con_supplier ";
        }
        $r_1 = Db::query($sql_1);
        $payment_num = $r_1[0]['payment_num']; // 待支付订单数量

        $r_2 = Db::query($sql_2);
        $send_num = $r_2[0]['send_num']; // 待发货订单数量

        $r_3 = Db::query($sql_3);
        $return_num = $r_3[0]['return_num']; // 售后订单数量

        if ($order_type != 'return')
        {
            $res = " a.store_id = '$store_id' and a.status <> 13 and a.mch_id like '%,$shop_id,%' and a.recycle in (0,2) and a.otype = '$order_headr_type1' $con_supplier ";
            
            if(!empty($platform_activities_id))
            {
                $res .= " and a.platform_activities_id = '$platform_activities_id' "; // 平台活动管理订单
            }
            if ($order_type == 'payment')
            {
                $res .= " and a.status = 0 "; // 未付款
            }
            else if ($order_type == 'send')
            {
                $res .= " and ((a.status = 1 and a.self_lifting = '0') or (a.status = 2 and a.self_lifting = '1') or (a.status = 1 and a.self_lifting = '2')) "; // 未发货
            }
            else if ($order_type == 'receipt')
            {
                $res .= " and a.status = 8 "; // 待核销
            }
            if (!empty($keyword))
            {
                $res .= " and a.sNo like $keyword_0 ";
            }
            
            $str1 = "a.id,a.store_id,a.user_id,a.name,a.mobile,a.num,a.old_total,a.z_price,a.sNo,a.sheng,a.shi,a.xian,a.address,a.remark,a.pay,a.add_time,a.pay_time,a.status,a.coupon_id,a.subtraction_id,a.consumer_money,a.coupon_activity_name,a.drawid,a.otype,a.ptcode,a.refundsNo,a.trade_no,a.is_anonymous,a.spz_price,a.reduce_price,a.coupon_price,a.red_packet,a.allow,a.source,a.delivery_status,a.readd,a.remind,a.offset_balance,a.mch_id,a.zhekou,a.grade_rate,a.grade_fan,a.p_sNo,a.bargain_id,a.comm_discount,a.real_sno,a.remarks,a.self_lifting,a.extraction_code,a.extraction_code_img,a.is_put,a.z_freight,a.manual_offer,a.preferential_amount,a.recycle,a.single_store,a.pick_up_store,a.commission_type,a.settlement_status,a.operation_type,a.VerifiedBy,a.VerifiedBy_type,a.mch_recycle,a.store_recycle,a.user_recycle,a.old_freight,a.order_failure_time,a.store_write_time,a.transaction_id,a.is_lssued,a.supplier_id,a.currency_symbol,a.exchange_rate,a.currency_code";
            // 根据用户id和前台参数,查询订单表 (id、订单号、订单价格、添加时间、订单状态、优惠券id)
            $sql1 = "select $str1 from lkt_order as a where " . $res . " order by a.add_time desc LIMIT $start,$end";
            $r1 = Db::query($sql1);
            if ($r1)
            {
                $r1 = DeliveryHelper::a_array_unique($r1);
                foreach ($r1 as $k => $v)
                {
                    $sNo = $v['sNo'];
                    $otype = $v['otype'];
                    $order_mch_id = explode(',', trim($v['mch_id'], ','));
                    $z_spz_price = $v['spz_price']; // 商品总价
                    if($v['old_total'] == '0.00' || $v['old_total'] == '')
                    {
                        $r1[$k]['old_total'] = $v['z_price'];
                    }

                    $r1[$k]['sale_type'] = 0;
                    $r1[$k]['haveExpress'] = false;

                    if($v['status'] == 2)
                    {
                        $r1[$k]['haveExpress'] = true;
                    }
                    // $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                    // $r_order_details = Db::query($sql_order_details);
                    // if($r_order_details)
                    // {
                    //     $r1[$k]['haveExpress'] = true;
                    // }   

                    //判断有无订单售后未结束
                    $res_s = ReturnOrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->whereNotIn('r_type', '2,4,5,8,9,10,12')->field('count(id) as num')->select()->toArray();
                    if($res_s[0]['num'] > 0 )
                    {
                        $r1[$k]['sale_type'] = 1;
                    }
                    //判断订单是否全在售后且未结束
                    $res_d = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->where('r_status','<>', '7')->field('count(id) as num')->select()->toArray();
                    if($res_s[0]['num'] == $res_d[0]['num'])
                    {
                        $r1[$k]['sale_type'] = 2;
                    }

                    $res_o = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->select()->toArray();
                    foreach ($res_o as $kd => $vd) 
                    {
                        $r1[$k]['dId'] = $vd['id'];
                        $r1[$k]['after_write_off_num'] = $vd['after_write_off_num'];
                        $r1[$k]['mch_store_write_id'] = $vd['mch_store_write_id'];
                        $r1[$k]['p_id'] = $vd['p_id'];
                        $r1[$k]['p_price'] = $vd['p_price'];
                        $r1[$k]['platform_coupon_price'] = $vd['platform_coupon_price'];
                        $r1[$k]['store_coupon_price'] = $vd['store_coupon_price'];
                        $r1[$k]['write_off_num'] = $vd['write_off_num'];
                        $r1[$k]['write_time'] = $vd['write_time'];
                        $r_status = $vd['r_status']; // 订单详情状态

                        $p_id = $vd['p_id'];
                        $sql_p = "select write_off_settings,write_off_mch_ids from lkt_product_list where store_id = '$store_id' and id = '$p_id' ";
                        $r_p = Db::query($sql_p);
                        if($r_p)
                        {
                            $r1[$k]['write_off_settings'] = $r_p[0]['write_off_settings'];
                            $r1[$k]['write_off_mch_ids'] = $r_p[0]['write_off_mch_ids'];
                        }

                        $res_o0 = 0;
                        if($r_status != 7)
                        {
                            $res_o1 = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo,'r_status'=>$r_status])->where('r_status','<>', '-1')->field('count(id) as num')->select()->toArray();
                            $res_o0 = $res_o1[0]['num'];
                        }
                        
                        $res_d1 = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo,'r_status'=>7])->field('count(id) as num')->select()->toArray();
                        $res_d0 = $res_d1[0]['num'];

                        $res_s1 = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->field('count(id) as num')->select()->toArray();
                        $res_s0 = $res_s1[0]['num'];

                        // 如果订单下面的商品都处在同一状态,那就改订单状态为已完成
                        if (($res_o + $res_d) == $res_s )
                        {
                            //如果订单数量相等 则修改父订单状态
                            $condition_data = array('nastatusme' => $r_status);
                            if($r_status == 5 && empty($v['arrive_time']))
                            {
                                $condition_data['arrive_time'] = date("Y-m-d H:i:s");
                            }
                            //如果订单数量相等 则修改父订单状态
                            $r = Db::name('order')->where(['store_id'=>$store_id,'sNo'=>$sNo])->update($condition_data);
                        }
                    }

                    // $r1[$k]['time'] = date("Y-n-j", strtotime($v['add_time']));
                    $r1[$k]['time'] = date("Y-m-d", strtotime($v['add_time']));

                    $show_store_name = 1; // 不需要选择门店
                    if($v['single_store'] == 0)
                    {
                        $show_store_name = 2; // 需要选择门店
                    }
                    $r1[$k]['show_store_name'] = $show_store_name;

                    $supplierName = "";
                    $sql_supplier = "select supplier_name,contact_phone,province,city,area,address from lkt_supplier where id = " . $r1[$k]['supplier_id'];
                    $r_supplier = Db::query($sql_supplier);
                    if($r_supplier)
                    {
                        $supplierName = $r_supplier[0]['supplier_name'];
                    }

                    $str_1 = "a.id,a.p_id as goodsId,a.p_name,a.p_price,a.num,a.unit,a.r_status,a.re_type,a.r_type,a.freight,a.size,a.sid,b.id as commodityId,b.product_title,b.imgurl as goodsUrl,b.is_distribution,c.img,b.is_appointment,a.mch_store_write_id,a.platform_coupon_price,a.store_coupon_price,a.write_time,a.write_time_id,b.write_off_settings,b.write_off_mch_ids,b.commodity_type";
                    
                    if (count($order_mch_id) > 1 && $v['status'] == 0)
                    {
                        $r1[$k]['order_status'] = false;
                        $spz_price = 0; // 店铺商品总价
                        $z_freight = 0; // 店铺商品总运费价
                        //查询单个商品的价格，运费，数量
                        $sql_1 = "select $str_1 from lkt_order_details as a left join lkt_product_list as b on a.p_id = b.id left join lkt_configure as c on a.sid = c.id where a.r_sNo = '$sNo' and b.mch_id = '$shop_id'";
                        $r_1 = Db::query($sql_1);
                        if ($r_1)
                        {
                            foreach ($r_1 as $ke => $va)
                            {
                                $detailId = $va['id'];
                                $spz_price += $va['p_price'] * $va['num']; // 店铺商品总价
                                $z_freight += $va['freight'];  // 店铺商品总运费价
                                if ($va['img'])
                                {
                                    $r_1[$ke]['imgurl'] = ServerPath::getimgpath($va['img'], $store_id);
                                }
                                else
                                {
                                    $r_1[$ke]['imgurl'] = ServerPath::getimgpath($va['goodsUrl'], $store_id);
                                }
                                $r_1[$ke]['p_price'] = round($va['p_price'],2);
                                $r_1[$ke]['freight'] = round($va['freight'],2);
                                $r_1[$ke]['otype'] = $otype;
                                $r_1[$ke]['supplierName'] = $supplierName;
                                $r_1[$ke]['supplier_name'] = $supplierName;

                                $r_1[$ke]['isReturn'] = false;
                                $sql__ = "select a.id from lkt_return_order a,lkt_order_details b where a.p_id=b.id and a.store_id = '$store_id' and a.sNo = '$sNo' and a.p_id = '$detailId' and a.r_type in (4,9,13,15) and b.r_status = 7";
                                $r__ = Db::query($sql__);
                                if($r__)
                                {
                                    if($otype == 'ZB')
                                    {
                                        $r_1[$ke]['id'] = $r__[0]['id'];
                                    }
                                    $r_1[$ke]['isReturn'] = true;
                                }
                            }
                        }
                        $reduce_price = $spz_price / $z_spz_price * $v['reduce_price']; // 该店铺商品总价 除以 整个订单商品总价 乘以 优惠的满减金额
                        $coupon_price = $spz_price / $z_spz_price * $v['coupon_price']; // 该店铺商品总价 除以 整个订单商品总价 乘以 优惠的优惠券金额
                        $r1[$k]['reduce_price'] = $reduce_price;
                        $r1[$k]['coupon_price'] = $coupon_price;
                        //计算会员特惠
                        $plugin_order = new Plugin_order($store_id);
                        $grade = $plugin_order->user_grade('GM', $spz_price, $user_id, $store_id);
                        $grade_rate = floatval($grade['rate']);

                        $r1[$k]['z_price'] = number_format(($spz_price - $reduce_price - $coupon_price) * $grade_rate + $z_freight, 2);

                        $r1[$k]['list'] = $r_1;
                    }
                    else
                    {
                        $sql_1 = "select $str_1 from lkt_order_details as a left join lkt_configure as c on a.sid = c.id left join lkt_product_list as b on c.pid = b.id where a.r_sNo = '$sNo' and b.mch_id = '$shop_id'";
                        $r_1 = Db::query($sql_1);
                        if ($r_1)
                        {
                            foreach ($r_1 as $ke => $va)
                            {
                                $detailId = $va['id'];
                                $va['otype'] = $v['otype'];
                                if ($va['is_distribution'] == 1)
                                {
                                    $va['otype'] = "FX"; //订单类型
                                }
                                if ($va['img'])
                                {
                                    $r_1[$ke]['imgurl'] = ServerPath::getimgpath($va['img'], $store_id);
                                }
                                else
                                {
                                    $r_1[$ke]['imgurl'] = ServerPath::getimgpath($va['goodsUrl'], $store_id);
                                }
                                $r_1[$ke]['p_price'] = round($va['p_price'],2);
                                $r_1[$ke]['freight'] = round($va['freight'],2);
                                $r_1[$ke]['otype'] = $otype;
                                $r_1[$ke]['supplierName'] = $supplierName;
                                $r_1[$ke]['supplier_name'] = $supplierName;

                                $r_1[$ke]['isReturn'] = false;
                                $sql__ = "select a.id from lkt_return_order a,lkt_order_details b where a.p_id=b.id and a.store_id = '$store_id' and a.sNo = '$sNo' and a.p_id = '$detailId' and a.r_type in (4,9,13,15) and b.r_status = 7";
                                $r__ = Db::query($sql__);
                                if($r__)
                                {
                                    if($otype == 'ZB')
                                    {
                                        $r_1[$ke]['id'] = $r__[0]['id'];
                                    }
                                    $r_1[$ke]['isReturn'] = true;
                                }
                                $va['status'] = false;
                            }
                            $r1[$k]['list'] = $r_1;
                        }
                        else
                        {
                            $r1[$k]['list'] = array();
                        }
                        $r1[$k]['order_status'] = true;
                    }

                    $r1[$k]['logistics_type'] = false;
                    $sql_express_delivery = "select * from lkt_express_delivery where store_id = '$store_id' and sNo = '$sNo' and subtable_id != 0";
                    $r_express_delivery = Db::query($sql_express_delivery);
                    if($r_express_delivery)
                    {
                        $r1[$k]['logistics_type'] = true;
                    }

                    $self_lifting = $v['self_lifting']; // 自提 0.配送 1.自提 2.商家自配 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
                    if($self_lifting == 2)
                    { // 商家自配
                        if($v['status'] == 1)
                        { // 订单状态为待发货
                            $sql_self_delivery_info = "select a.id from lkt_self_delivery_info as a left join lkt_order_details as d on a.id = d.store_self_delivery where d.r_sNo = '$sNo' and (a.courier_name != null or a.courier_name != '') ";
                            $r_self_delivery_info = Db::query($sql_self_delivery_info);
                            if($r_self_delivery_info)
                            {
                                $r1[$k]['status'] = 2;
                            }
                        }
                    }
                }

                $list1 = $r1;
            }
        }
        else
        {
            $res = " a.store_id = '$store_id' and b.mch_id = '$shop_id' and o.otype = '$order_headr_type1' $con_supplier1 ";
            
            if (!empty($keyword))
            {
                $res .= " and a.r_sNo like $keyword_0 ";
            }

            $str1 = "d.id,d.user_id,d.sNo,d.re_type,d.re_apply_money,d.re_money,d.real_money,d.re_time,d.re_photo,d.r_type,d.sid,d.pid,a.p_name,a.p_price,a.num,a.unit,a.size,b.imgurl,b.is_distribution,c.img,a.after_discount,a.after_write_off_num,d.r_write_off_num,b.write_off_settings,c.write_off_num,o.currency_symbol,o.exchange_rate,o.currency_code ";
            $sql1 = "select $str1 from lkt_return_order as d left join lkt_order_details as a on d.p_id = a.id left join lkt_product_list as b on a.p_id = b.id left join lkt_configure as c on a.sid = c.id left join lkt_order as o on a.r_sNo = o.sNo where $res order by d.re_time desc,d.r_type asc limit $start,$end ";
            $r1 = Db::query($sql1);
            if ($r1)
            {
                $r1 = DeliveryHelper::a_array_unique($r1);
                foreach ($r1 as $k => $v)
                {
                    $v['p_price'] = round($v['p_price'],2);
                    $v['after_discount'] = round($v['after_discount'],2);
                    $sNo = $v['sNo'];
                    $re_type = $v['re_type'];
                    $r_type = $v['r_type'];
                    if ($v['img'])
                    {
                        $v['imgurl'] = ServerPath::getimgpath($v['img'], $store_id);
                    }
                    else
                    {
                        $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                    }

                    if($re_type == 1)
                    {
                        $v['type'] = '退货退款';
                    }
                    elseif($re_type == 2)
                    {
                        $v['type'] = '仅退款';
                    }
                    else
                    {
                        $v['type'] = '换货';
                    }

                    if(($r_type == 1 && $re_type == 1)|| ($r_type == 3 && $re_type == 1))
                    {
                        $v['prompt'] = '退款中';
                    }
                    elseif($r_type == 4 || $r_type == 9 || $r_type == 13 || $r_type == 15)
                    {
                        $v['prompt'] = '退款成功';
                    }
                    elseif(($r_type == 2 && $re_type != 3)|| ($r_type == 5 && $re_type != 3) || ($r_type == 8 && $re_type != 3))
                    {
                        $v['prompt'] = '退款失败';
                    }
                    elseif(($r_type == 1 && $re_type == 3)|| ($r_type == 3 && $re_type == 3) || ($r_type == 11 && $re_type == 3))
                    {
                        $v['prompt'] = '换货中';
                    }
                    elseif($r_type == 12)
                    {
                        $v['prompt'] = '换货成功';
                    }
                    elseif(($r_type == 5 && $re_type == 3)|| ($r_type == 10 && $re_type == 3))
                    {
                        $v['prompt'] = '换货失败';
                    }
                    else
                    {
                        $v['prompt'] = '待审核';
                    }
                    $str2 = "a.id,a.store_id,a.user_id,a.name,a.mobile,a.num,a.old_total,a.z_price,a.sNo,a.sheng,a.shi,a.xian,a.address,a.remark,a.pay,a.add_time,a.pay_time,a.status,a.coupon_id,a.subtraction_id,a.consumer_money,a.coupon_activity_name,a.drawid,a.otype,a.ptcode,a.refundsNo,a.trade_no,a.is_anonymous,a.spz_price,a.reduce_price,a.coupon_price,a.red_packet,a.allow,a.source,a.delivery_status,a.readd,a.remind,a.offset_balance,a.mch_id,a.zhekou,a.grade_rate,a.grade_fan,a.p_sNo,a.bargain_id,a.comm_discount,a.real_sno,a.remarks,a.self_lifting,a.extraction_code,a.extraction_code_img,a.is_put,a.z_freight,a.manual_offer,a.preferential_amount,a.recycle,a.single_store,a.pick_up_store,a.commission_type,a.settlement_status,a.operation_type,a.VerifiedBy,a.VerifiedBy_type,a.currency_symbol,a.exchange_rate,a.currency_code";
                    
                    $sql2 = "select $str2 from lkt_order as a where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and a.sNo  ='$sNo'";
                    $r2 = Db::query($sql2);
                    if ($r2)
                    {
                        $v['otype'] = $r2[0]['otype'];
                        $r2[0]['comm_discount'] = round($r2[0]['comm_discount'],2);
                        $r2[0]['coupon_price'] = round($r2[0]['coupon_price'],2);
                        $r2[0]['grade_rate'] = round($r2[0]['grade_rate'],2);
                        $r2[0]['manual_offer'] = round($r2[0]['manual_offer'],2);
                        $r2[0]['offset_balance'] = round($r2[0]['offset_balance'],2);
                        $r2[0]['old_total'] = round($r2[0]['old_total'],2);
                        $r2[0]['preferential_amount'] = round($r2[0]['preferential_amount'],2);
                        $r2[0]['reduce_price'] = round($r2[0]['reduce_price'],2);
                        $r2[0]['spz_price'] = round($r2[0]['spz_price'],2);
                        $r2[0]['z_freight'] = round($r2[0]['z_freight'],2);
                        $r2[0]['z_price'] = round($r2[0]['z_price'],2);
                        $r2[0]['zhekou'] = round($r2[0]['zhekou'],2);
                        if ($v['is_distribution'] == 1)
                        {
                            $v['otype'] = "FX"; //订单类型
                        }
                        // $r2[0]['time'] = date("Y-n-j", strtotime($v['re_time']));
                        $r2[0]['time'] = date("Y-m-d", strtotime($v['re_time']));
                        $r2[0]['Dafter_discount'] = $v['after_discount'];
                        $r2[0]['after_write_off_num'] = $v['after_write_off_num'];
                        $r2[0]['list'] = $v;
                        $list1[] = $r2[0];
                    }
                }
            }
        }
        if(count($list1) > 0)
        {
            foreach ($list1 as $key => $info)
            {
                $info0 = $info['time'];
                $result[$info0][] = $info;
            }
            
            $rew = 0;
            foreach ($result as $key => $value)
            {
    
                $list[$rew]['time'] = $key;
                $list[$rew]['res'] = $value;
                $rew = $rew + 1;
            }
        }
        
        $data = array('list' => $list, 'payment_num' => $payment_num, 'send_num' => $send_num, 'return_num' => $return_num);
        return $data;
    }

    // 订单详情(移动端店铺)
    public static function mch_order_details($action)
    {
        $store_id = $action['store_id'];
        $shop_id = $action['shop_id'];
        $user_id = $action['user_id'];
        $sNo = $action['sNo'];
        
        $show_write_store = 0; // 处理订单详情中是否显示适用核销门店
        $write_store_num = 0; // 可核销门店数量

        $appointment = array();
        $storeSelfInfo = array();
        $supplierInfo = array();

        $field1 = "id,store_id,user_id,name,mobile,num,old_total,z_price,sNo,cpc,sheng,shi,xian,address,remark,pay,add_time,pay_time,status,coupon_id,subtraction_id,consumer_money,coupon_activity_name,drawid,otype,ptcode,refundsNo,trade_no,is_anonymous,spz_price,reduce_price,coupon_price,red_packet,allow,source,delivery_status,readd,remind,offset_balance,mch_id,zhekou,grade_rate,grade_fan,p_sNo,bargain_id,comm_discount,real_sno,remarks,self_lifting,extraction_code,extraction_code_img,is_put,z_freight,manual_offer,preferential_amount,recycle,single_store,pick_up_store,commission_type,settlement_status,operation_type,VerifiedBy,VerifiedBy_type,mch_recycle,store_recycle,user_recycle,order_failure_time,store_write_time,transaction_id,otype,cancel_method,supplier_id,is_lssued,currency_symbol,exchange_rate,currency_code";
        $r1 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->whereLike('mch_id', '%,'.$shop_id.',%')->field($field1)->select()->toArray();
        if ($r1)
        {
            $order_mch_id = explode(',', trim($r1[0]['mch_id'], ','));

            $otype = $r1[0]['otype']; // 订单类型
            $z_spz_price = $r1[0]['spz_price']; // 商品总价
            $r1[0]['coupon_id'] = '0,0';
            $coupon_id = $r1[0]['coupon_id']; // 优惠券ID
            $subtraction_id = $r1[0]['subtraction_id'];// 满减活动ID
            $grade_rate = $r1[0]['grade_rate'];//会员等级折扣
            $r1[0]['comm_discount'] = round($r1[0]['comm_discount'],2);
            $r1[0]['reduce_price'] = round($r1[0]['reduce_price'],2);
            $r1[0]['coupon_price'] = round($r1[0]['coupon_price'],2);
            $r1[0]['grade_rate'] = round($r1[0]['grade_rate'],2);
            $r1[0]['manual_offer'] = round($r1[0]['manual_offer'],2);
            $r1[0]['offset_balance'] = round($r1[0]['offset_balance'],2);
            if($r1[0]['old_total'] == '0.00' || $r1[0]['old_total'] == '')
            {
                $r1[0]['old_total'] = number_format($r1[0]['z_price'],2);
            }
            else
            {
                $r1[0]['old_total'] = number_format($r1[0]['old_total'],2);
            }
            $r1[0]['preferential_amount'] = round($r1[0]['preferential_amount'],2);
            $r1[0]['z_price'] = number_format($r1[0]['z_price'],2);
            $r1[0]['zhekou'] = round($r1[0]['zhekou'],2);
            $r1[0]['coupon_money'] = $r1[0]['coupon_price'];
            $r1[0]['reduce_money'] = $r1[0]['reduce_price'];
            $r1[0]['grade_fan'] = 0;
            $r1[0]['haveExpress'] = false;
            $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
            $r_order_details = Db::query($sql_order_details);
            if($r_order_details)
            {
                $r1[0]['haveExpress'] = true;
            }
            
            $mch_coupon_price = $r1[0]['coupon_price'];// 优惠券金额
            $self_lifting = $r1[0]['self_lifting']; // 是否自提
            $preferential_amount = $r1[0]['preferential_amount']; // 优惠金额
            $single_store = $r1[0]['single_store']; // 下单门店

            if ($r1[0]['grade_rate'] != 0 && $r1[0]['subtraction_id'] == 0)
            {
                $mch_coupon_price = $mch_coupon_price - $preferential_amount;
            }

            $remarks = ''; //备注
            if($r1[0]['remarks'] != '')
            {
                if(Tools::is_serialized($r1[0]['remarks']))
                {
                    $remarks0 = unserialize($r1[0]['remarks']); // 订单备注
                    foreach ($remarks0 as $k1 => $v1)
                    {
                        $remarks = $v1;
                    }
                }
                else
                {
                    $remarks = $r1[0]['remarks'];
                }
            }
            $sale_type = 0;
            //判断有无订单售后未结束
            $res_s = ReturnOrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->whereNotIn('r_type', '2,4,5,8,9,10,12')->field('id')->select()->toArray();
            if($res_s)
            {
                $sale_type = 1;
            }
            //判断订单是否全在售后且未结束
            $res_d = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->where('r_status', '<>','7')->field('id')->select()->toArray();
            if(count($res_s) == count($res_d))
            {
                $sale_type = 2;
            }

            if ($r1[0]['readd'] == 0)
            {
                $up = Db::name('order')->where(['store_id' => $store_id,'sNo' => $sNo])->update(['readd' => '1']);

                $message_logging_list_1 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$sNo,'type'=>1);
                PC_Tools::message_pop_up($message_logging_list_1);
                PC_Tools::message_read($message_logging_list_1);
                $message_logging_list_3 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$sNo,'type'=>3);
                PC_Tools::message_pop_up($message_logging_list_3);
                PC_Tools::message_read($message_logging_list_3);
                $message_logging_list_5 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$sNo,'type'=>5);
                PC_Tools::message_pop_up($message_logging_list_5);
                PC_Tools::message_read($message_logging_list_5);
            }

            if($r1[0]['status'] == 5)
            {
                $message_logging_list_6 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$sNo,'type'=>6);
                PC_Tools::message_pop_up($message_logging_list_6);
                PC_Tools::message_read($message_logging_list_6);
            }

            $mobile = $r1[0]['mobile'];
            $r1[0]['mobile'] = $mobile;
            $arr = $r1[0];
            $arr['address'] = $r1[0]['address'];
            if ($self_lifting == '3')
            {
                $show_write_store = 1;
                if($single_store != 0)
                {
                    $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'id'=>$single_store])->select()->toArray();
                    if($r0_0)
                    {
                        $sql_order_details = "select write_time from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' ";
                        $r_order_details = Db::query($sql_order_details);
                        $write_time = $r_order_details[0]['write_time'];

                        $array_address = array('cpc'=>$r0_0[0]['cpc'],'sheng'=>$r0_0[0]['sheng'],'shi'=>$r0_0[0]['shi'],'xian'=>$r0_0[0]['xian'],'address'=>$r0_0[0]['address'],'code'=>$r0_0[0]['code']);
                        $address_xq = PC_Tools::address_translation($array_address);
                        
                        $appointment = array('name'=>$r0_0[0]['name'],'address'=>$address_xq,'time'=>$write_time);
                    }
                }
            }

            $arr['appointment'] = $appointment;

            $user_id1 = $r1[0]['user_id'];
            //收货人的cpc
            $arr['cpc'] = $r1[0]['cpc'];
            
            $r3 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id1])->field('user_name,cpc')->select()->toArray();
            $arr['user_name'] = $r3[0]['user_name'];
            
            
            $yunfei = 0; // 总运费
            $spz_price = 0; // 商品总价
            $grade_rate_amount = 0; // 会员优惠金额
            
            $str_4 = "a.id,a.p_id as goodsId,a.p_name,a.p_price,a.num,a.unit,a.r_status,a.re_type,a.r_type,a.freight,a.size,a.sid,b.id as commodityId,b.product_title,b.imgurl as goodsUrl,b.is_distribution,c.img,a.after_write_off_num,b.is_appointment,a.mch_store_write_id,a.platform_coupon_price,a.store_coupon_price,b.write_off_mch_ids,b.write_off_settings,a.write_off_num,a.write_time,a.write_time_id,a.arrive_time,a.anchor_id,a.living_room_id,a.after_discount";
            $sql4 = "select $str_4 from lkt_order_details as a left join lkt_configure as c on a.sid = c.id left join lkt_product_list as b on c.pid = b.id  where a.r_sNo = '$sNo' and b.mch_id = '$shop_id'";
            $r4 = Db::query($sql4);
            if ($r4)
            {
                $arr['dId'] = $r4[0]['id'];
                $arr['after_write_off_num'] = $r4[0]['after_write_off_num'];
                $arr['is_appointment'] = $r4[0]['is_appointment'];
                $arr['mch_store_write_id'] = $r4[0]['mch_store_write_id'];
                $arr['p_id'] = $r4[0]['goodsId'];
                $arr['p_price'] = $r4[0]['p_price'];
                $arr['platform_coupon_price'] = $r4[0]['platform_coupon_price'];
                $arr['store_coupon_price'] = $r4[0]['store_coupon_price'];
                $arr['write_off_mch_ids'] = $r4[0]['write_off_mch_ids'];
                $arr['write_off_settings'] = $r4[0]['write_off_settings'];
                $arr['write_off_num'] = $r4[0]['write_off_num'];
                $arr['write_time'] = $r4[0]['write_time'];
                $arr['arrive_time'] = $r4[0]['arrive_time'];
                if($r4[0]['write_off_mch_ids'] == 0)
                {
                    $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->select()->toArray();
                    $write_store_num = count($r0_0);
                }
                else
                {
                    $write_off_mch_ids = explode(',',$r4[0]['write_off_mch_ids']);
                    $write_store_num = count($write_off_mch_ids);
                }
                foreach ($r4 as $k => $v)
                {
                    $p_price = $v['p_price']; // 商品售价
                    $p_num = $v['num']; // 商品数量
                    $grade_rate_amount += round(($p_price - $p_price * $grade_rate) * $p_num,2);
                    $yunfei = $yunfei + $v['freight'];
                    $spz_price += $v['p_price'] * $v['num'];
                    $anchor_id = $v['anchor_id']; // 主播user_id
                    $living_room_id = $v['living_room_id']; // 直播间ID
                    $v['p_id'] = $v['goodsId']; // 商品ID
                    $v['refundShowBtn'] = false;
                    $v['returndId'] = 0;
                    $r_r = ReturnOrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'p_id'=>$v['id']])->order('re_time','desc')->field('id')->select()->toArray();
                    if($r_r)
                    {
                        $v['returndId'] = $r_r[0]['id'];
                        $v['refundShowBtn'] = true;
                    }

                    if ($v['img'])
                    {
                        $v['pic'] = ServerPath::getimgpath($v['img']);
                    }
                    else
                    {
                        $v['pic'] = ServerPath::getimgpath($v['imgurl']);
                    }
                    $v['p_price'] = round($v['p_price'],2);
                    $v['freight'] = round($v['freight'],2);

                    $v['status'] = 1; // 1.待使用 2.已用完
                    if($v['write_off_num'] == 0)
                    {
                        $v['status'] = 2; // 1.待使用 2.已用完
                    }

                    $v['is_write'] = 1; // 还未核销
                    if ($self_lifting == '3')
                    {
                        $sql_record = "select id from lkt_write_record where p_id = " . $r4[0]['id'];
                        $r_record = Db::query($sql_record);
                        if($r_record)
                        {
                            $v['is_write'] = 2; // 已核销
                        }
                    }

                    if($otype == 'ZB')
                    {
                        $sql_living = "select a.living_img,b.user_name from lkt_living_room as a left join lkt_user as b on a.user_id = b.user_id where a.store_id = '$store_id' and a.id = '$living_room_id' and a.user_id = '$anchor_id' ";
                        $r_living = Db::query($sql_living);
                        if($r_living)
                        {
                            $arr['live_img'] = $r_living[0]['living_img'];
                            $arr['anchor_name'] = $r_living[0]['user_name'];
                        }
                    }
                    $v['z_price'] = $v['after_discount'] + $v['freight'];
                    $res[$k] = $v;
                }
            }

            if (count($order_mch_id) > 1 && $r1[0]['status'] == 0)
            { // 当为跨店铺未付款订单时
                $reduce_price = $spz_price / $z_spz_price * $r1[0]['reduce_money']; // 该店铺商品总价 除以 整个订单商品总价 乘以 优惠的满减金额
                $coupon_price = $spz_price / $z_spz_price * $r1[0]['coupon_money']; // 该店铺商品总价 除以 整个订单商品总价 乘以 优惠的优惠券金额
                $r1[0]['reduce_money'] = $reduce_price;
                $r1[0]['coupon_money'] = $coupon_price;

                $arr['z_price'] = round(($spz_price - $reduce_price - $coupon_price) * $grade_rate + $yunfei, 2);
                $arr['order_status'] = false;
            }
            else
            {
                $arr['z_price'] = $r1[0]['z_price'];
                $arr['order_status'] = true;
            }

            $arr['spz_price'] = round($spz_price,2);
            $arr['list'] = $res;
            $arr['storeSelfInfo'] = $storeSelfInfo;
            $arr['z_freight'] = round($yunfei,2);
            $arr['grade_rate'] = round($grade_rate,2);
            $arr['coupon_price'] = $mch_coupon_price;
            $arr['preferential_amount'] = $preferential_amount;
            $arr['grade_rate_amount'] = $grade_rate_amount;
            $arr['remarks'] = $remarks;
            $arr['sale_type'] = $sale_type;

            if($r1[0]['supplier_id'] == 0)
            {
                $data = array('list' => $arr,'show_write_store'=>$show_write_store,'write_store_num'=>$write_store_num);
            }
            else
            {
                $sql_supplier = "select supplier_name,contact_phone,province,city,area,address from lkt_supplier where id = " . $r1[0]['supplier_id'];
                $r_supplier = Db::query($sql_supplier);
                if($r_supplier)
                {
                    $supplierName = $r_supplier[0]['supplier_name'];
                    $supplierPhone = $r_supplier[0]['contact_phone'];
                    $supplierAddress = $r_supplier[0]['province'] . $r_supplier[0]['city'] . $r_supplier[0]['area'] . $r_supplier[0]['address'];
                    $supplierInfo = array('orderTypeName'=>'供应商','supplierName'=>$supplierName,'supplierPhone'=>$supplierPhone,'supplierAddress'=>$supplierAddress);
                }

                $data = array('list' => $arr,'supplierInfo'=>$supplierInfo);
            }
            $message = Lang('Success');
            echo json_encode(array('code' => "200", 'data' => $data, 'message' => $message));
            exit;
        }
        else
        {
            $message = Lang('Illegal invasion');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }
    }

    // 移动端发货除拼团和砍价外的插件（旧）
    public static function frontDelivery($action)
    {
        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();
        $store_id = $action['store_id'] ;
        $user_id = $action['user_id'] ;
        $order_details_id = $action['order_details_id'] ;
        $con = $action['con'] ;
        $sNo = $action['sNo'] ;
        $len = $action['len'] ;
        $express_id = $action['express_id'] ;
        $courier_num = $action['courier_num'] ;
        $time = date('Y-m-d H:i:s', time());

        $shop_id = '';
        if(isset($action['shop_id']))
        {
            $shop_id = $action['shop_id'];
        }
        $operator_id = 0;
        if(isset($action['operator_id']))
        {
            $operator_id = $action['operator_id'];
        }
        $operator = '';
        if(isset($action['operator']))
        {
            $operator = $action['operator'];
        }
        $source = 1;
        if(isset($action['source']))
        {
            $source = $action['source'];
        }

        Db::startTrans();
        
        $con['r_status'] = 2;
        $rd = Db::name('order_details')->where(['r_status'=>'1','r_sNo'=>$sNo])->whereIn('id',$order_details_id)->update($con);
        if ($rd < 0)
        {
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货失败,订单号为' . $sNo . '！';
            $lktlog->log("app/mch.log",$Log_content);
            $message = Lang('Busy network');
            echo json_encode(array('code' => 103, 'message' => $message));
            exit();
        }
        $batchSend = false;
        $count = OrderDetailsModel::where(['r_sNo'=>$sNo])->whereIn('r_status','1,2')->field('id')->select()->toArray();
        // 所有为已发货状态的订单详情跟所选订单一样多的情况下为批量发货
        if (count($count) == $len)
        { //批量发货
            $batchSend = true;
        }

        //查询订单信息
        $sql_p = "select o.id,o.user_id,o.sNo,d.p_name,d.p_id,d.sid,d.num,o.name,o.address,d.id as d_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo where o.store_id = '$store_id' and d.r_sNo = '$sNo' ";
        $res_p = Db::query($sql_p);

        $curSendPos = 0; //统计当前发货次数
        foreach ($res_p as $key => $value)
        {
            $d_id = $value['d_id'];
            $p_name = $value['p_name'];
            $user_id = $value['user_id'];
            $address = $value['address'];
            $name = $value['name'];
            $order_id = $value['id'];
            $oid = $value['sNo'];
            $p_id = $value['p_id'];
            $sid = $value['sid'];
            $num = $value['num'];

            $sql3 = "insert into lkt_express_delivery(store_id,sNo,order_details_id,express_id,courier_num,num,deliver_time) value ('$store_id','$sNo','$d_id','$express_id','$courier_num','$num','$time')";
            $r3 = Db::execute($sql3);
            if ($rd < 1)
            {
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加快递记录表失败！sql:" . $sql3);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '将订单详情ID：' . $d_id . '，进行了发货失败',2,$source,$shop_id,$operator_id);
                $message = Lang('admin_order.0');
                echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                exit;
            }

            $upod0 = OrderDetailsModel::where(['r_sNo'=>$oid,'r_status'=>1])->field('id')->select()->toArray();
            $upod = count($upod0);
            $curSendPos = $curSendPos + 1;
            $sendFinish = (!$upod && !$batchSend) || ($batchSend && $curSendPos == $len);
            if ($sendFinish)
            {
                $rl = Db::name('order')->where(['sNo'=>$oid])->update(['status'=>2]);
                if ($rl < 0)
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '将订单详情ID：' . $d_id . '，进行了发货失败',2,$source,$shop_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货失败,订单号为' . $oid . '！';
                    $lktlog->log("app/mch.log",$Log_content);
                    $message = Lang('Busy network');
                    echo json_encode(array('code' => 103, 'message' => $message));
                    exit();
                }
                else
                {
                    $msg_title = "【" . $oid . "】订单发货啦！";
                    $msg_content = Lang("admin_order.2");

                    /**发货成功通知*/
                    $pusher = new LaikePushTools();
                    $pusher->pushMessage($user_id,$msg_title, $msg_content, $store_id, $user_id);

                    $msgsql = "select o.id,o.user_id,o.sNo,d.p_name,o.name,o.address from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.r_sNo ='$oid'";
                    $msgres = Db::query($msgsql);
                    $msgres = (object)$msgres[0];

                    $openid = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('wx_id')->select()->toArray();
                    if(!empty($openid))
                    {
                        $msgres->uid = $openid[0]['wx_id'];
                        $compres = ExpressModel::where(['id'=>$express_id])->field('kuaidi_name')->select()->toArray();
                        if (!empty($compres))
                        {
                            $msgres->company = $compres[0]['kuaidi_name'];
                        }
                    }
                }
            }
        }
        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货成功,订单号为' . $sNo . '！';
        $lktlog->log("app/mch.log",$Log_content);

        Db::commit();

        $Jurisdiction->admin_record($store_id, $operator, '将订单详情ID：' . $d_id . '，进行了发货',2,$source,$shop_id,$operator_id);

        $message = Lang("Success");
        echo json_encode(array('code' => 200, 'message' => $message));
        exit();
    }

    //管理端发货（旧）
    public static function adminDelivery($action)
    {
        $store_id = $action->store_id ;
        $store_type = $action->store_type ;
        $con = $action->con ;
        $id = $action->id;
        $len = $action->len ;
        $express_id = $action->express_id ;
        $courier_num = $action->courier_num ;
        $lktlog = new LaiKeLogUtils();
        $admin_list = $action->admin_list;
        $admin_name = $admin_list['name'];
        $JurisdictionAction = $action->JurisdictionAction;
        $time = date('Y-m-d H:i:s', time());

        $source = '';
        if($store_type == 8)
        { // 管理后台
            $source = 1;
        }
        else if($store_type == 7)
        { // PC店铺
            $source = 2;
        }

        Db::startTrans();
        $sqld = "update lkt_order_details set r_status= 2 $con where id in($id) and r_status = '1'";
        LaiKeLogUtils::lktLog($sqld . "=trade=" . __LINE__);
        $rd = Db::execute($sqld);
        if ($rd < 1)
        {
            $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单详情状态失败！sql:" . $sqld);
            Db::rollback();
            $message = Lang('admin_order.0');
            echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
            exit;
        }

        //查询订单信息
        $sql_p = "select o.id,o.user_id,o.sNo,d.p_name,d.p_id,d.sid,d.num,o.name,o.address,d.id as d_id from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.id in($id) ";
        $res_p = Db::query($sql_p);
        $count = 0;//统计详细订单记录数
        $batchSend = false;//是否批量发货
        $curSendPos = 0; //统计当前发货次数
        foreach ($res_p as $key => $value)
        {
            $d_id = $value['d_id'];
            $p_name = $value['p_name'];
            $user_id = $value['user_id'];
            $address = $value['address'];
            $name = $value['name'];
            $order_id = $value['id'];
            $sNo = $value['sNo'];
            $p_id = $value['p_id'];
            $sid = $value['sid'];
            $num = $value['num'];
            $curSendPos = $curSendPos + 1;
            if ($count == 0)
            {
                $count = OrderDetailsModel::where('r_sNo',$sNo)
                                        ->where('r_status','in','1,2')
                                        ->count();
                if ($count == $len)
                {
                    //批量发货
                    $batchSend = true;
                }
            }

            $sql3 = "insert into lkt_express_delivery(store_id,sNo,order_details_id,express_id,courier_num,num,deliver_time) value ('$store_id','$sNo','$d_id','$express_id','$courier_num','$num','$time')";
            $r3 = Db::execute($sql3);
            if ($rd < 1)
            {
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加快递记录表失败！sql:" . $sql3);
                Db::rollback();
                $message = Lang('admin_order.0');
                echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                exit;
            }

            //发货结束修改订单状态
            $upod = OrderDetailsModel::where(['r_status'=>1,'r_sNo'=>$sNo])->count();
            $sendFinish = (!$upod && !$batchSend) || ($batchSend && $curSendPos == $len);
            if ($sendFinish)
            {
                $sql = OrderModel::where('sNo',$sNo)->find();
                $sql->status = 2;
                $rl = $sql->save();
                if (!$rl)
                {
                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "发货失败！sNo:" . $sNo);
                    Db::rollback();
                    $message = Lang('admin_order.1');
                    echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                    exit;
                }
                else
                {
                    $msg_title = "【" . $sNo . "】订单发货啦！";
                    $msg_content = Lang("admin_order.2");
                    $JurisdictionAction->admin_record($store_id, $admin_name, ' 将订单号为 ' . $sNo . ' 的订单发货 ', 7);
                    LaiKeLogUtils::lktLog($msg_title);
                    /**发货成功通知*/
                    $pusher = new LaikePushTools();
                    $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, $admin_name);

                    $msgsql = "select o.id,o.user_id,o.sNo,d.p_name,o.name,o.address from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.r_sNo ='$sNo'";
                    $msgres = Db::query($msgsql);
                    $msgres = (object)$msgres[0];
                    $openid = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('wx_id')->select()->toArray();
                    if(!empty($openid))
                    {
                        $msgres->uid = $openid[0]['wx_id'];
                        $compres = ExpressModel::where('id',$express_id)->field('kuaidi_name')->select()->toArray();
                        if (!empty($compres))
                        {
                            $msgres->company = $compres[0]['kuaidi_name'];
                        }

                        if($msgres->uid)
                        {
                            $action->sendWXTopicMsg($msgres, $sNo, $courier_num, $store_id);
                        }
                    }

                }
            }
        }
        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message));
        exit;
    }

    // 订单列表-优化后（用户）
    public static function user_order_index0($array)
    {
        $lktlog = new LaiKeLogUtils();
        $time = date("Y-m-d H:i:s");

        $store_id = $array['store_id'];
        $keyword = $array['keyword'];
        $otype = $array['type'];
        $order_type = $array['order_type'];
        $pagestart = $array['pagestart'];
        $pagesize = $array['pagesize'];
        $userid = $array['userid'];

        $num_arr = [0, 1, 2, 5];
        $res_order = [];

        foreach ($num_arr as $key => $value)
        {
            if ($value == 5)
            {
                if($otype != '')
                {
                    $sql_otype = " and a.otype = '$otype' ";
                }
                $sql_order = "select tt.* from (SELECT a.id,row_number () over (PARTITION BY a.sNo) AS top  FROM lkt_order a RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE a.store_id = '$store_id' $sql_otype and a.user_id = '$userid' and a.status = 5 and a.recycle = 0 and a.sNo not in (select oid from lkt_comments where store_id = '$store_id' and uid = '$userid')) as tt where tt.top<2";
                $re = Db::query($sql_order);
                $res_order[$value] = count($re);
            }
            else
            {
                if($otype != '')
                {
                    $re = OrderModel::where(['store_id'=>$store_id,'status'=>$value,'user_id'=>$userid,'recycle'=>0,'otype'=>$otype])->count('id');
                }
                else
                {
                    $re = OrderModel::where(['store_id'=>$store_id,'status'=>$value,'user_id'=>$userid,'recycle'=>0])->count('id');
                }
                $res_order[$value] = $re;
            }
        }

        $res = " a.store_id = '$store_id' and a.recycle != 2 and a.user_id = '$userid' ";
        if ($order_type == 'payment')
        { // 未付款
            $res .= " and a.status = 0 ";
        }
        else if ($order_type == 'send')
        { // 未发货
            $res .= " and a.status = 1 ";
        }
        else if ($order_type == 'receipt')
        { // 待收货
            $res .= " and a.status = 2 ";
        }
        else if ($order_type == 'evaluete')
        { // 待评论
            $res .= "  and a.status = 5 and a.sNo not in (select oid from lkt_comments where store_id = '$store_id' and uid = '$userid') ";
        }
        if($otype != '')
        {
            $res .= " and a.otype = '$otype' ";
        }

        $order = array();
        $str = "a.id,a.sNo,a.status,a.add_time,a.mch_id,a.old_total,a.z_price,a.old_freight,a.z_freight,a.pay,a.otype,a.delivery_status,a.arrive_time,a.self_lifting,a.settlement_status,a.allow,a.num,a.voucher,a.review_status,a.reason_for_rejection";

        if (empty($keyword))
        {
            $str .= ",row_number () over (PARTITION BY a.sNo) AS top";
        }
        else
        {
            $keyword_0 = Tools::FuzzyQueryConcatenation($keyword);
            $res .= " and (a.sNo like $keyword_0 or b.p_name like $keyword_0 )  ";

            $str .= ",row_number () over (PARTITION BY a.sNo) AS top";
        }

        $sql0_0 = "select tt.* from(SELECT row_number () over (PARTITION BY a.sNo) AS top,a.add_time FROM lkt_order a RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo LEFT JOIN lkt_product_list as p on p.id = d.p_id WHERE $res ) as tt where tt.top < 2 order by tt.add_time desc ";
        $r0_0 = Db::query($sql0_0);
        $order_num = count($r0_0);

        $sql = "select tt.* from (SELECT $str
                FROM  lkt_order a
                RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo 
                LEFT JOIN lkt_product_list as p on p.id = d.p_id
                WHERE $res ) as tt where tt.top < 2
                order by tt.add_time desc LIMIT $pagestart,$pagesize";
        $r = Db::query($sql);
        if ($r)
        {
            $r = DeliveryHelper::a_array_unique($r);
            foreach ($r as $k => $v)
            {
                $sNo = $v['sNo']; // 订单号
                $v['z_price'] = (float)$v['z_price'];
                $v['z_freight'] = (float)$v['z_freight'];
                if($v['old_total'] == '0.00' || $v['old_total'] == '')
                {
                    $v['old_total'] = $v['z_price'];
                }
                if($v['old_freight'] == '0.00' || $v['old_freight'] == 0)
                {
                    $v['old_freight'] = $v['z_freight'];
                }

                $mch_id = trim($v['mch_id'],','); // 店主ID
                $mch_id_arr_ = explode(',', $mch_id);
                if (count($mch_id_arr_) > 1)
                { //是多店铺订单
                    $ismch = true;
                }
                else
                { //不是多店铺订单
                    $ismch = false;
                }

                $v['shop_id'] = 0; // 店铺ID
                $v['shop_name'] = ''; // 店铺名称
                $v['shopHead_img'] = ''; // 店铺头像
                $is_invoice = 0; // 店铺是否支持开票 0.否 1.是
                if (!empty($mch_id))
                {
                    $r0 = MchModel::where(['store_id'=>$store_id,'recovery'=>0])->where('id','=',$mch_id_arr_[0])->field('id,name,head_img,is_invoice')->select()->toArray();
                    if ($r0)
                    {
                        $v['shop_id'] = $r0[0]['id']; // 店铺ID
                        $v['shop_name'] = $r0[0]['name']; // 店铺名称
                        $v['shopHead_img'] = ServerPath::getimgpath($r0[0]['head_img'],$store_id); // 店铺头像
                        $is_invoice = $r0[0]['is_invoice']; // 店铺是否支持开票 0.否 1.是
                    }
                }

                $get_button_array = array('store_id'=>$store_id,'userid'=>$userid,'sNo'=>$sNo,'otype'=>$v['otype'],'pay'=>$v['pay'],'status'=>$v['status'],'review_status'=>$v['review_status'],'self_lifting'=>$v['self_lifting'],'delivery_status'=>$v['delivery_status'],'arrive_time'=>$v['arrive_time'],'settlement_status'=>$v['settlement_status'],'is_invoice'=>$is_invoice);
                $get_button_list = PC_Tools::get_order_button($get_button_array); // 获取订单按钮（申请售后和评论除外）

                $v['list'] = array();
                $sql1 = "select id,p_id,p_name,sid,size,p_price,num,unit,r_status,re_type,settlement_type from lkt_order_details where r_sNo = '$sNo' ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    foreach($r1 as $k1 => $v1)
                    {
                        $order_details_id = $v1['id']; // 订单详情ID
                        $p_id = $v1['p_id']; // 商品ID
                        $sid = $v1['sid']; // 属性ID

                        if($otype == 'PS')
                        { // 预售订单
                            $ps_list = array();
                            
                            $r_pre_sell_goods = PreSellGoodsModel::where('product_id',$p_id)->field('sell_type,deposit,delivery_time,deadline,balance_pay_time')->select()->toArray();
                            if($r_pre_sell_goods)
                            {
                                if($r_pre_sell_goods[0]['sell_type'] == 1)
                                {
                                    $balance_pay_time = $r_pre_sell_goods[0]['balance_pay_time'];
                                    $ps_list['sellType'] = $r_pre_sell_goods[0]['sell_type'];
                                    $ps_list['deliveryTime'] = $r_pre_sell_goods[0]['delivery_time'];
                                    $ps_list['deposit'] = round($r_pre_sell_goods[0]['deposit'],2);
                                    $ps_list['balance'] = round(($r_pre_sell_goods[0]['p_price'] - $r_pre_sell_goods[0]['deposit']),2);
                                    $ps_list['startTime'] = $r_pre_sell_goods[0]['balance_pay_time'];
                                    $ps_list['endTime'] = date("Y-m-d 23:59:59",strtotime($r_pre_sell_goods[0]['balance_pay_time']));
                                    $ps_list['canPay'] = false;
                                    if($ps_list['startTime'] <= $time && $time <= $ps_list['endTime'] )
                                    {
                                        $ps_list['canPay'] = true;
                                    }
                                }
                                else
                                {
                                    $ps_list['sellType'] = $r_pre_sell_goods[0]['sell_type'];
                                    $ps_list['deliveryTime'] = $r_pre_sell_goods[0]['delivery_time'];
                                    $ps_list['endTime'] = $r_pre_sell_goods[0]['deadline'];
                                }
                            }
                        }
                        else if($otype == 'IN')
                        {
                            $v1['integral'] = floatval($v['allow']) / floatval($v['sum']);
                        }

                        $sql2 = "select img from lkt_configure where id = '$sid' ";
                        $r2 = Db::query($sql2);
                        if($r2)
                        {
                            $v['imgurl'] = ServerPath::getimgpath($r2[0]['img'],$store_id); // 商品属性图
                        }

                        $v1['isreturn'] = 0; // 退款成功是否显示 0.不显示 1.显示
                        if ($v1['re_type'] > 0)
                        {
                            $v1['isreturn'] = 1;
                        }

                        if($ismch)
                        { // 多店铺
                            $sel_mch_sql = "SELECT m.id,m.name,m.head_img  from lkt_product_list as l LEFT JOIN lkt_mch as m on l.mch_id = m.id where l.id = '$p_id' ";
                            $mch_res = Db::query($sel_mch_sql);
                            $v1['shop_id'] = $mch_res[0]['id'];
                            $v1['shop_name'] = $mch_res[0]['name'];
                            $v1['shop_logo'] = ServerPath::getimgpath($mch_res[0]['head_img']);
                        }

                        $get_button_array = array('store_id'=>$store_id,'user_id'=>$userid,'id'=>$order_details_id,'sNo'=>$sNo,'otype'=>$v['otype'],'pay'=>$v['pay'],'p_id'=>$p_id,'sid'=>$sid,'self_lifting'=>$v['self_lifting'],'r_status'=>$v1['r_status'],'settlement_type'=>$v1['settlement_type']);
                        $get_order_details_button = PC_Tools::get_product_button($get_button_array); // 获取商品按钮

                        $get_button_array = array('otype'=>$v['otype'],'status'=>$v['status'],'self_lifting'=>$v['self_lifting'],'order_details_num'=>count($r1),'get_button_list'=>$get_button_list,'get_order_details_button'=>$get_order_details_button,'Number_of_cycles'=>$k1);
                        $get_order_button = PC_Tools::get_order_button_assistant($get_button_array); // 获取订单按钮（申请售后和评论）
                        $get_button_list = $get_order_button['get_button_list']; // 订单详情按钮数组
                        $v1['get_order_details_button'] = $get_order_button['get_order_details_button']; // 获取商品按钮

                        $v['list'][] = $v1;

                        $r_status = $v1['r_status']; // 订单详情状态
                        $res_o = 0;
                        if($r_status != 7)
                        {   
                            $res_o = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo,'r_status'=>$r_status])->where('r_status','<>','-1')->count();
                        }
    
                        $res_d = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo,'r_status'=>7])->count();

                        $res_s = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->count();

                        // 如果订单下面的商品都处在同一状态,那就改订单状态为已完成
                        if (($res_o + $res_d) == $res_s )
                        {
                            $condition = " status = '$r_status' ";
                            if($r_status == 5 && empty($v['arrive_time']))
                            {
                               $condition .= ",arrive_time = '$time' ";
                            }
                            //如果订单数量相等 则修改父订单状态
                            $sql = "update lkt_order set $condition where store_id = '$store_id' and sNo = '$sNo'";
                            $r = Db::execute($sql);
                            if ($r < 1)
                            {
                                $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改订单详情状态失败！sql:" . $sql);
                            }
                            if ($r_status > 0)
                            {
                                $status = $r_status;
                            }
                        }
                    }
                }
                $v['get_button_list'] = $get_button_list;

                $order[] = $v;
            }
        }

        $data = array('order' => $order, 'order_num'=>$order_num,'res_order'=>$res_order);
        return $data;
    }

    // 订单列表（用户）
    public static function user_order_index($action)
    {
        $store_id = $action->store_id;
        $keyword = $action->keyword;
        $page = $action->page;
        $lktlog = $action->lktlog;
        $order_type = $action->order_type;
        $pagestart = $action->pagestart;
        $order_failure = $action->order_failure;
        $company = $action->company;
        $userid = $action->userid;
        $type = $action->type;

        $isDistribution = 1; // 0.不是分销商 1是分销商
        $fx_level = Db::name('user_distribution')->where(['store_id'=>$store_id,'user_id'=>$userid])->value('level');
        if($fx_level > 0)
        {
            $isDistribution = 1; // 0.不是分销商 1是分销商
        }
        
        $time = date("Y-m-d H:i:s");
        $num_arr = [0, 1, 2, 5];
        $res_order = [];

        foreach ($num_arr as $key => $value)
        {
            if ($value == 1)
            {
                $re = OrderModel::where(['store_id'=>$store_id,'status'=>$value,'user_id'=>$userid,'recycle'=>0,'otype'=>$type])->count('id');
                $res_order[$value] = $re;
            }
            elseif ($value == 5)
            {
                if($type != '')
                {
                    $sql_otype = " and a.otype = '$type' ";
                }
                $sql_order = "select tt.* from (SELECT a.id,row_number () over (PARTITION BY a.sNo) AS top  FROM lkt_order a RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE a.store_id = '$store_id' $sql_otype and a.user_id = '$userid' and a.status = 5 and a.recycle = 0 and a.sNo not in (select oid from lkt_comments where store_id = '$store_id' and uid = '$userid')) as tt where tt.top<2";
                $re = Db::query($sql_order);
                $res_order[$value] = count($re);
            }
            else
            {    
                $re = OrderModel::where(['store_id'=>$store_id,'status'=>$value,'user_id'=>$userid,'otype'=>$type,'recycle'=>0])->count('id');
                $res_order[$value] = $re;
            }
        }
        $res = " a.store_id = '$store_id' and a.recycle != 2 ";
        if ($order_type == 'payment')
        {
            $res .= " and a.status = 0 "; // 未付款
        }
        else if ($order_type == 'send')
        {
            $res .= " and a.status = 1 "; // 未发货
        }
        else if ($order_type == 'receipt')
        {
            $res .= " and (a.status = 2 or a.status = 8) "; // 待收货
        }
        else if ($order_type == 'evaluete')
        {
            //$res = " and a.status = 3 "; // 待评论
        }
        if($type != '')
        {
            if($type == 'GM')
            {
                // $res .= " and a.otype in ('GM','FS') ";
                $res .= " and a.otype in ('GM') ";
            }
            else
            {
                $res .= " and a.otype = '$type' ";
            }
        }

        if($type == 'PS')
        {
            $company = "day";
            $r_config = PreSellConfigModel::where('store_id',$store_id)->field('order_failure')->select()->toArray();
            if ($r_config)
            {
                $order_failure = $r_config[0]['order_failure']; // 订单失效（秒）
            }
        }

        $order = array();
        $str = "a.allow,a.id,a.old_total,a.z_price,a.sNo,a.add_time,a.pay_time,a.arrive_time,a.pay,a.status,a.delivery_status,a.otype,a.mch_id,a.offset_balance,a.self_lifting,a.spz_price,a.z_freight,a.old_freight,a.single_store,a.settlement_status,a.num as sum,d.sid,p.is_distribution,a.real_sno,p.write_off_settings,d.store_self_delivery,a.currency_symbol,a.exchange_rate,a.currency_code,a.voucher,a.review_status,a.reason_for_rejection";
        if (empty($keyword))
        {
            $str .= ",row_number () over (PARTITION BY a.sNo) AS top";
            if ($order_type == 'evaluete')
            {
                $res .= " and a.user_id = '$userid' and a.status = 5 and a.sNo not in (select oid from lkt_comments where store_id = '$store_id' and uid = '$userid') ";

                $sql0_0 = "select tt.* from(SELECT row_number () over (PARTITION BY a.sNo) AS top,a.add_time FROM lkt_order a RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE $res ) as tt where tt.top <2 order by tt.add_time desc ";

                $sql = "select tt.* from (SELECT $str
                    FROM  lkt_order a
                    RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo 
                    LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE $res ) as tt where tt.top <2
                    order by tt.add_time desc LIMIT $pagestart,10";
            }
            else
            {
                $res .= " and a.user_id = '$userid' ";
                
                $sql0_0 = "select tt.* from(SELECT row_number () over (PARTITION BY a.sNo) AS top,a.add_time FROM lkt_order a RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE $res ) as tt where tt.top <2 order by tt.add_time desc ";

                // 根据用户id和前台参数,查询订单表 (id、订单号、订单价格、添加时间、订单状态、优惠券id)
                $sql = " select tt.* from ( SELECT $str
                    FROM  lkt_order a
                    RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo 
                    LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE $res ) as tt where tt.top<2
                    order by tt.add_time desc LIMIT $pagestart,10";
            }
        }
        else
        {
            $keyword_0 = Tools::FuzzyQueryConcatenation($keyword);
            $str .= ",row_number () over (PARTITION BY a.sNo) AS top";
            if ($order_type == 'evaluete')
            {
                $res .= " and a.user_id = '$userid' and a.status = 5 and a.sNo not in (select oid from lkt_comments where store_id = '$store_id' and uid = '$userid') and (a.sNo like $keyword_0 or d.p_name like $keyword_0 )  ";
                
                $sql0_0 = "select tt.* from(SELECT row_number () over (PARTITION BY a.sNo) AS top,a.add_time FROM lkt_order a RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE $res ) as tt where tt.top <2 order by tt.add_time desc ";

                $sql = "select tt.* from (SELECT $str
                    FROM  lkt_order a
                    RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo 
                    LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE $res ) as tt where tt.top <2
                    order by tt.add_time desc LIMIT $pagestart,10";
            }
            else
            {
                $res .= " and a.user_id = '$userid' and (a.sNo like $keyword_0 or d.p_name like $keyword_0 ) ";

                $sql0_0 = "select tt.* from(SELECT row_number () over (PARTITION BY a.sNo) AS top,a.add_time FROM lkt_order a RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE $res ) as tt where tt.top <2 order by tt.add_time desc ";

                $sql = " select tt.* from ( SELECT $str
                    FROM  lkt_order a
                    RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo 
                    LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE $res ) as tt where tt.top<2
                    order by tt.add_time desc LIMIT $pagestart,10";
            }
        }
        $r0_0 = Db::query($sql0_0);
        $order_num = count($r0_0);
        $r = Db::query($sql);
        if ($r)
        {
            $r = $action->a_array_unique($r);
            foreach ($r as $k => $v)
            {
                $rew = array();
                $rew['allow'] = $v['allow']; // 积分
                $rew['add_time'] = $v['add_time']; // 订单时间
                $rew['pay_time'] = $v['pay_time']; // 支付时间
                $rew['pay_type'] = $v['pay']; // 支付方式
                $rew['delivery_status'] = $v['delivery_status']; // 提醒状态
                $rew['id'] = $v['id']; // 订单id
                $rew['offset_balance'] = round($v['offset_balance'],2); //余额抵扣
                $rew['otype'] = $v['otype']; //订单类型
                $rew['z_price'] = (float)$v['z_price']; // 订单价格
                $rew['old_total'] = (float)$v['old_total']; // 订单价格
                $rew['old_freight'] = (float)$v['old_freight']; // 订单价格
                $rew['currency_symbol'] = $v['currency_symbol']; // 支付时货币符号
                $rew['currency_code'] = $v['currency_code']; // 支付时货币编码
                $rew['voucher'] = $v['voucher']; // 支付时货币编码
                $rew['review_status'] = $v['review_status']; // 支付时货币编码
                $rew['reason_for_rejection'] = $v['reason_for_rejection']; // 支付时货币编码
                if($v['exchange_rate'] == '')
                {
                    $exchange_rate = 1;
                }
                else
                {
                    $exchange_rate = $v['exchange_rate']; // 支付时货币汇率
                }
                if($v['old_total'] == '0.00' || $v['old_total'] == '')
                {
                    $rew['old_total'] = (float)$v['z_price'];
                }
                if($v['old_freight'] == '0.00' || $v['old_freight'] == 0)
                {
                    $rew['old_freight'] = (float)$v['z_freight'];
                }
                $rew['spz_price'] = (float)$v['spz_price']; // 商品价格
                $rew['sNo'] = $v['sNo']; // 订单号
                $rew['real_sno'] = $v['real_sno']; // 支付单号
                $sNo = $v['sNo']; // 订单号
                $rew['status'] = $v['status']; // 订单状态
                $rew['self_lifting'] = $v['self_lifting']; // 订单状态
                $rew['single_store'] = $v['single_store']; // 自提门店id
                $rew['is_distribution'] = $v['is_distribution'];
                $rew['settlement_status'] = $v['settlement_status'];
                $rew['write_off_settings'] = $v['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
                $rew['mch_id'] = $v['mch_id'];
                $rew['sid'] = $v['sid'];
                $rew['invoicePrice'] = $v['z_price'] - $v['z_freight'];//发票金额
                $rew['invoiceTimeout'] = false;
                $rew['isInvoice'] = false;
                $rew['haveExpress'] = false;
                $is_invoice = 0; // 是否支持开票 0.否 1.是

                $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                $r_order_details = Db::query($sql_order_details);
                if($r_order_details)
                {
                    $rew['haveExpress'] = true;
                } 

                if ($rew['otype'] == 'VI')
                { // 虚拟订单
                    $rew['invoiceTimeout'] = false;
                }
                else
                {
                    $arrive_time = $v['arrive_time'];
                    $arrive_time_end = date("Y-m-d H:i:s",strtotime("+20 day",strtotime($arrive_time)));
                    if($arrive_time_end <= $time)
                    { // 允许开发票 <= 当前时间
                        $rew['invoiceTimeout'] = true;
                    }
                }
                
                if($v['status'] == 7 || $v['settlement_status'] == 1)
                {
                    $rew['isOrderDel'] = true;
                }
                else
                {
                    $rew['isOrderDel'] = false;
                }

                if ($v['is_distribution'] == 1)
                {
                    $rew['otype'] = "FX"; //订单类型
                }
                $mch_id = $v['mch_id']; // 店主ID
                //判断是否为多店铺订单hgindex
                $mch_id_arr_ = explode(',', $mch_id);

                if (count($mch_id_arr_) > 3)
                { //是多店铺订单
                    $rew['ismch'] = true;
                }
                else
                { //不是多店铺订单
                    $rew['ismch'] = false;
                }
                $rew['shop_id'] = 0;
                $rew['shop_name'] = '';
                $rew['shop_logo'] = '';
                if (!empty($mch_id))
                {
                    $mch_id_ = substr($mch_id, 1, strlen($mch_id) - 2);
                    $r0 = MchModel::where(['store_id'=>$store_id,'recovery'=>0])->where('id','=',$mch_id_)->field('id,name,logo,poster_img,head_img,is_lock,is_invoice')->select()->toArray();
                    if ($r0)
                    {
                        $rew['shop_id'] = $r0[0]['id'];
                        $rew['shop_name'] = $r0[0]['name'];
                        $rew['shop_logo'] = ServerPath::getimgpath($r0[0]['logo']);
                        $rew['posterImg'] = ServerPath::getimgpath($r0[0]['poster_img'],$store_id);
                        $rew['shopHead_img'] = ServerPath::getimgpath($r0[0]['head_img'],$store_id);
                        $rew['headImg'] = ServerPath::getimgpath($r0[0]['head_img'],$store_id);
                        $rew['shop_is_lock'] = $r0[0]['is_lock'];
                        $is_invoice = $r0[0]['is_invoice'];
                    }
                }

                $get_button_array = array('store_id'=>$store_id,'userid'=>$userid,'sNo'=>$sNo,'otype'=>$v['otype'],'pay'=>$v['pay'],'status'=>$v['status'],'review_status'=>$v['review_status'],'self_lifting'=>$v['self_lifting'],'delivery_status'=>$v['delivery_status'],'arrive_time'=>$v['arrive_time'],'settlement_status'=>$v['settlement_status'],'is_invoice'=>$is_invoice);
                $get_button_list = PC_Tools::get_order_button($get_button_array); // 获取订单按钮（申请售后和评论除外）

                if($v['arrive_time'] != '' && $v['settlement_status'] == 1 && $v['status'] == 5 && $is_invoice == 1)
                { // 收货时间不为空 并且 已结算 并且 订单状态为已完成 并且 店铺支持开票
                    $rew['isInvoice'] = true; // 可以开发票
                }

                //如果为竞拍商品，则查出标题和图片
                if ($rew['otype'] == 'JP')
                {
                    $jp_sNo = $rew['sNo'];
                    $res_jp = AuctionProductModel::where(['store_id'=>$store_id,'trade_no'=>$jp_sNo])
                                            ->field('title,imgurl,current_price')
                                            ->select()
                                            ->toArray();
                    $jp_title = '';
                    $jp_imgurl = '';
                    $jp_price = '';
                    if ($res_jp)
                    {
                        $jp_title = $res_jp[0]['title'];
                        $jp_imgurl = $res_jp[0]['imgurl'];

                        $jp_imgurl = ServerPath::getimgpath($jp_imgurl);
                        $jp_price = $res_jp[0]['current_price'];
                    }
                    $rew['jp']['jp_title'] = $jp_title;
                    $rew['jp']['jp_imgurl'] = $jp_imgurl;
                    $rew['jp']['jp_price'] = $jp_price;
                }
                else if ($rew['otype'] == 'pt')
                {
                    $rew['pt_price'] = sprintf("%.2f", $v['z_price']);
                }

                // 根据订单号,查询订单详情
                $rew['list'] = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->select()->toArray();
                $z_freight = 0;
                foreach ($rew['list'] as $kk => $vv)
                {
                    $freight = $vv['freight'];
                    $z_freight += $freight;
                    if ($vv['re_type'] > 0)
                    {
                        $arr['r_status'] = $vv['r_status'];
                        $arr['p_id'] = $vv['p_id'];
                        $arr['order_id'] = $v['id'];
                        $arr['id'] = $vv['id'];
                        $rew['isreturn'] = 1;
                        $vv['return'] = $arr;
                    }
                }

                if (isset($rew['list'][0]['r_type']))
                {
                    $rew['r_type'] = $rew['list'][0]['r_type'];
                }
                else
                {
                    $rew['r_type'] = '100';
                }
                $rew['z_freight'] = (float)$z_freight;
                $sum23 = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->sum('num');
                $rew['sum'] = $sum23;
                foreach ($rew['list'] as $k_ => $v_)
                {
                    $detailId = $v_['id'];
                    $rew['list'][$k_]['integral'] = floatval($rew['allow']) / floatval($rew['sum']);
                    $rew['list'][$k_]['deliverNum'] = $v_['deliver_num'];
                    $rew['list'][$k_]['storeRecycle'] = $v_['store_recycle'];
                    $rew['list'][$k_]['userRecycle'] = $v_['user_recycle'];
                    $rew['list'][$k_]['mchRecycle'] = $v_['mch_recycle'];

                    $rew['list'][$k_]['isReturn'] = false;
                    $sql__ = "select a.id from lkt_return_order a,lkt_order_details b where a.p_id=b.id and a.store_id = '$store_id' and a.sNo = '$sNo' and a.p_id = '$detailId' and a.r_type in (4,9,13,15) and b.r_status = 7";
                    $r__ = Db::query($sql__);
                    if($r__)
                    {
                        $rew['list'][$k_]['isReturn'] = true;
                    }
                }
                unset($product);
                $product = array();
                $can_buy = true;
                $url = '';
                if ($rew['list'] && !empty($rew['list']))
                {
                    foreach ($rew['list'] as $key => $values)
                    {
                        if (strpos($values['r_sNo'], 'PT') !== false)
                        {
                            $rew['pro_id'] = $values['p_id'];
                        }
                        $sid = $values['sid']; // 产品id
                        $p_id = $values['p_id']; // 产品id
                        if($rew['otype'] == 'IN')
                        {
                            $in_res = IntegralGoodsModel::where('id',$p_id)->field('goods_id')->select()->toArray();
                            $p_id = $in_res[0]['goods_id'];
                        }
                        elseif($rew['otype'] == 'MS')
                        {
                            $ms_res = SecondsActivityModel::where('id',$p_id)->field('goodsId')->select()->toArray();
                            $p_id = $ms_res[0]['goodsId'];
                        }
                        $values['comments_type'] = 0;//不显示
                        // if ($rew['otype'] == 'VI')
                        // { // 虚拟订单
                        //     $values['comments_type'] = 3;//评论完成
                        // }
                        // else
                        // {
                            //判断订单评论状态
                            $res_c = CommentsModel::where(['store_id'=>$store_id,'pid'=>$p_id,'uid'=>$userid,'oid'=>$sNo])->select()->toArray();
                            if(!empty($res_c))
                            {
                                $values['comments_type'] = 2;//待追评
                                if($res_c[0]['review'])
                                {
                                    $values['comments_type'] = 3;//评论完成
                                }
                            }
                            else
                            {
                                if($values['r_status'] == 5)
                                {
                                    $values['comments_type'] = 1;//待评价
                                }
                            }
                        // }
                        
                        $rew['comments_type'] = $values['comments_type'];
                        if ($rew['ismch'])
                        {
                            $sel_mch_sql = "SELECT m.id,m.name,m.logo  from lkt_product_list as l LEFT JOIN lkt_mch as m on l.mch_id = m.id where l.id= $p_id ";
                            $mch_res = Db::query($sel_mch_sql);
                            $values['shop_id'] = $mch_res[0]['id'];
                            $values['shop_name'] = $mch_res[0]['name'];
                            $values['shop_logo'] = ServerPath::getimgpath($mch_res[0]['logo']);
                        }
                        if ($values['r_status'] == 0)
                        {
                            //如果是代付款订单 查询规格价格是否改变
                            $c_res = ConfigureModel::where('id',$sid)->select()->toArray();
                            if (!empty($c_res))
                            {
                                $c_price = $c_res[0]['price'];//当前规格设置价格
                                $order_p_price = $values['p_price'];
                                if ($c_price != $order_p_price)
                                {
                                    $can_buy = false;
                                }
                            }
                        }
                        $values['freight'] = (float)$values['freight'];
                        $values['manual_offer'] = (float)$values['manual_offer'];
                        $values['p_price'] = (float)$values['p_price'];
                        $values['p_price'] = round(($values['p_price'] * $exchange_rate),2); // 支付时货币符号
                        $values['real_money'] = (float)$values['real_money'];
                        $size = $values['size'];
                        $size = rtrim($size, ";");
                        $values['size'] = $size;

                        $values['attribute_id'] = $values['sid']; // 属性id

                        $get_button_array = array('store_id'=>$store_id,'user_id'=>$userid,'id'=>$values['id'],'sNo'=>$sNo,'otype'=>$v['otype'],'pay'=>$v['pay'],'p_id'=>$p_id,'sid'=>$sid,'self_lifting'=>$v['self_lifting'],'r_status'=>$values['r_status'],'settlement_type'=>$values['settlement_type']);
                        $get_order_details_button = PC_Tools::get_product_button($get_button_array); // 获取商品按钮

                        $get_button_array = array('otype'=>$v['otype'],'status'=>$v['status'],'self_lifting'=>$v['self_lifting'],'order_details_num'=>count($rew['list']),'get_button_list'=>$get_button_list,'get_order_details_button'=>$get_order_details_button,'Number_of_cycles'=>$key);
                        $get_order_button = PC_Tools::get_order_button_assistant($get_button_array); // 获取订单按钮（申请售后和评论）
                        $get_button_list = $get_order_button['get_button_list']; // 订单详情按钮数组
                        $values['get_order_details_button'] = $get_order_button['get_order_details_button']; // 获取商品按钮

                        $arr = $values;
                        // 根据产品id,查询产品列表 (产品图片)
                        $sql = "select a.imgurl,b.img from lkt_product_list as a left join lkt_configure as b on a.id = b.pid where a.store_id = '$store_id' and a.id = '$p_id' and b.id = '$sid'";
                        $rrr = Db::query($sql);
                        if ($rrr)
                        {
                            $img_res = $rrr['0']['imgurl'];
                            if($rrr['0']['img'])
                            {
                                $img_res = $rrr['0']['img'];
                            }
                            else
                            {
                                $img_res = $rrr['0']['imgurl'];
                            }
                            $url = ServerPath::getimgpath($img_res); // 拼图片路径
                            if($type == 'PS')
                            {
                                $rew['imgurl'] = $url;
                                
                                $r_pre_sell_goods = PreSellGoodsModel::where('product_id',$p_id)->field('sell_type,deposit,delivery_time,deadline,balance_pay_time')->select()->toArray();
                                if($r_pre_sell_goods)
                                {
                                    if($r_pre_sell_goods[0]['sell_type'] == 1)
                                    {
                                        $balance_pay_time = $r_pre_sell_goods[0]['balance_pay_time'];
                                        $rew['sellType'] = $r_pre_sell_goods[0]['sell_type'];
                                        $rew['deliveryTime'] = $r_pre_sell_goods[0]['delivery_time'];
                                        $rew['deposit'] = round($r_pre_sell_goods[0]['deposit'],2);
                                        $rew['balance'] = round(($values['p_price'] - $r_pre_sell_goods[0]['deposit']),2);
                                        $rew['startTime'] = $r_pre_sell_goods[0]['balance_pay_time'];
                                        $rew['endTime'] = date("Y-m-d 23:59:59",strtotime($r_pre_sell_goods[0]['balance_pay_time']));
                                        $rew['canPay'] = false;
                                        if($rew['startTime'] <= $time && $time <= $rew['endTime'] )
                                        {
                                            $rew['canPay'] = true;
                                        }
                                    }
                                    else
                                    {
                                        $rew['sellType'] = $r_pre_sell_goods[0]['sell_type'];
                                        $rew['deliveryTime'] = $r_pre_sell_goods[0]['delivery_time'];
                                        $rew['endTime'] = $r_pre_sell_goods[0]['deadline'];
                                    }
                                }
                            }
                            $arr['imgurl'] = $url;

                            if ($rew['otype'] == 'IN')
                            {   
                                $rrr = ConfigureModel::where('id',$sid)->field('img')->select()->toArray();
                                if ($rrr)
                                {
                                    $arr['imgurl'] = ServerPath::getimgpath($rrr[0]['img']); // 拼图片路径
                                }
                            }
                            $product[$key] = $arr;
                        }

                        $r_status = $values['r_status']; // 订单详情状态
                        if($r_status != 7)
                        {   
                            $res_o = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo,'r_status'=>$r_status])->where('r_status','<>','-1')->count();
                        }
                        else
                        {
                            $res_o = 0;
                        }

                        $res_d = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo,'r_status'=>7])->count();

                        $res_s = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->count();

                        // 如果订单下面的商品都处在同一状态,那就改订单状态为已完成
                        if (($res_o + $res_d) == $res_s )
                        {
                            $condition = " status = '$r_status' ";
                            if($r_status == 5 && empty($v['arrive_time']))
                            {
                               $condition .= ",arrive_time = '$time' ";
                            }
                            //如果订单数量相等 则修改父订单状态
                            $sql = "update lkt_order set $condition where store_id = '$store_id' and sNo = '$sNo'";
                            $r = Db::execute($sql);
                            if ($r < 1)
                            {
                                $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改订单详情状态失败！sql:" . $sql);
                            }
                            if ($r_status > 0)
                            {
                                $rew['status'] = $r_status;
                            }
                        }
                    }
                    $rew['sale_type'] = 0;
                    //判断有无订单售后未结束
                    $res_s = ReturnOrderModel::where(['store_id'=>$store_id,'user_id'=>$userid,'sNo'=>$sNo])->where('r_type','not in','2,4,5,8,9,10,12')->select()->toArray();
                    if($res_s)
                    {
                        $rew['sale_type'] = 1;
                    }
                    $rew['list'] = $product;
                    $rew['can_buy'] = $can_buy;
                    if ($rew['otype'] == 'pt' && $order_type == 'payment')
                    {
                        //如果是拼团未付款的订单，进入判断
                        //查询所有关闭了，结束了的活动商品id(attr_id)的
                        $group_cfg = GroupProductModel::select()->toArray();
                        $can_open = 1;
                        $close = array();
                        foreach ($group_cfg as $ks => $vs)
                        {
                            $e_time = strtotime(unserialize($vs['group_data'])->endtime);//获取结束时间戳
                            if ($e_time < time())
                            {
                                //活动已经结束
                                $close[$ks] = $vs['attr_id'];
                            }
                            else if ($vs['g_status'] != 2)
                            {
                                //活动已经关闭
                                $close[$ks] = $vs['attr_id'];
                            }
                        }
                        //比较订单是否是该商品
                        $isin_ = in_array($rew['list'][0]['attribute_id'], $close);
                        if ($isin_)
                        {
                            $can_open = 0;
                        }
                        //如果是，can_open = 0
                        $rew['can_open'] = $can_open;
                    }
                    $rew['imgurl'] = $url;
                }
                $rew['get_button_list'] = $get_button_list;

                $rew['isDfkGbi'] = 0; // //0:其他正常关闭   1：待付款关闭订单
                if($v['pay_time'] == '' || $v['pay_time'] == null)
                {
                    $rew['isDfkGbi'] = 1; // //0:其他正常关闭   1：待付款关闭订单
                }
                $rew['old_total'] = round(($rew['old_total'] * $exchange_rate),2);
                // $rew['freight'] = round(($rew['freight'] * $exchange_rate),2);
                $order[] = $rew;
            }
        }

        $data = array('order' => $order, 'order_failure' => $order_failure, 'company' => $company,'order_num'=>$order_num,'res_order'=>$res_order,'isDistribution'=>$isDistribution);
        return $data;
    }

    // 订单详情-优化后(移动端)
    public static function app_order_details0($array)
    {
        $lktlog = new LaiKeLogUtils();
        $time = date("Y-m-d H:i:s");

        $store_id = $array['store_id'];
        $access_id = $array['access_id'];
        $id = $array['id'];

        $integral = array();
        $sNo_list = array();

        $admin_shop_id = PC_Tools::SelfOperatedStore($store_id); // 获取自营店ID

        $payment_config = Tools::getPayment($store_id);// 支付方式配置

        // 根据微信id,查询用户id
        $access = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,login_num,verification_time,password,money,score')->select()->toArray();
        if (!empty($access))
        {
            $user_id = $access[0]['user_id']; // user_id
            $login_num = $access[0]['login_num']; // 支付密码错误次数
            $verification_time = $access[0]['verification_time']; // 支付密码验证时间
            $verification_time = date('Y-m-d H:i:s', strtotime('+1 day', strtotime($verification_time)));
            $user_password = $access[0]['password'];
            $user_money = $access[0]['money'];
            $user_score = $access[0]['score'];
            if ($user_password != '')
            {
                $password_status = 1;
            }
            else
            {
                $password_status = 0;
            }
            if ($login_num == 5)
            {
                if ($time < $verification_time)
                {
                    $enterless = false;
                }
                else
                {
                    $sql = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->find();
                    $sql->login_num = 0;
                    $sql->save();
                    $enterless = true;
                }
            }
            else
            {
                $enterless = true;
            }

            // 根据商品ID、用户user_id、订单ID，查询订单
            $r_o0 = OrderModel::where(['store_id'=>$store_id,'id'=>$id,'user_id'=>$user_id])->select()->toArray();
            if($r_o0)
            {
                $sNo = $r_o0[0]['sNo']; // 订单号
                $otype = $r_o0[0]['otype']; // 订单号

                if($otype == 'GM')
                {
                    $r_o1 = OrderModel::where(['store_id'=>$store_id,'p_sNo'=>$sNo,'user_id'=>$user_id])->select()->toArray();
                    if($r_o1)
                    {
                        foreach($r_o1 as $ko1 => $vo1)
                        {
                            $sNo_list[] = $vo1['sNo'];
                        }
                    }
                    else
                    {
                        $sNo_list[] = $sNo;
                    }
                }
                else
                {
                    $sNo_list[] = $sNo;
                }
            }

            $str = "id,sNo,status,add_time,order_failure_time,old_total,z_price,old_freight,z_freight,spz_price as product_total,pay,otype,coupon_price,preferential_amount,name,mobile,sheng,shi,xian,address,mch_id,remarks,coupon_id,self_lifting,wx_order_status,delivery_status,arrive_time,settlement_status,recycle,voucher,review_status,reason_for_rejection";
            $address_list = array();
            $isInvoice = false; // 不可以开发票
            // 根据商品ID、用户user_id、订单ID，查询订单
            $r = OrderModel::where(['store_id'=>$store_id,'id'=>$id,'user_id'=>$user_id])->field($str)->select()->toArray();
            if ($r)
            {   
                $sNo = $r[0]['sNo'];
                $status = $r[0]['status'];
                $recycle = $r[0]['recycle'];
                $otype = $r[0]['otype'];
                $delivery_status = $r[0]['delivery_status'];
                $arrive_time = $r[0]['arrive_time'];
                $settlement_status = $r[0]['settlement_status'];
                $self_lifting = $r[0]['self_lifting']; // 自提 0.配送 1.自提 2.商家自配 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
                if(count($sNo_list) == 1)
                {
                    if($recycle != 0)
                    {
                        ob_clean();
                        $message = Lang('order.36');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit();
                    }
                }

                if($r[0]['old_total'] == '0.00' || $r[0]['old_total'] == '')
                {
                    $r[0]['old_total'] = $r[0]['z_price']; // 历史订单价
                }

                if($r[0]['old_freight'] == '0.00' || $r[0]['old_freight'] == 0)
                {
                    $r[0]['old_freight'] = $r[0]['z_freight']; // 历史订单运费
                }

                $mobile1 = $r[0]['mobile'];//联系手机号
                $mobile = substr_replace($mobile1, '****', 3, 4);//隐藏操作
                $address_list = array('name'=>$r[0]['name'],'mobile1'=>$mobile1,'sheng'=>$r[0]['sheng'],'shi'=>$r[0]['shi'],'xian'=>$r[0]['xian'],'address'=>$r[0]['address']);
                $r[0]['address_list'] = $address_list; // 联系人信息

                $mch_id = $r[0]['mch_id'];
                $mch_id = explode(',', $mch_id);
                $mch_id = $mch_id[1];
                $mch_name = '';
                $is_invoice = 0; // 是否支持开票 0.否 1.是
                $mch_name_res = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name,is_invoice')->select()->toArray();
                if (!empty($mch_name_res))
                {
                    $mch_name = $mch_name_res[0]['name']; // 店铺名称
                    $is_invoice = $mch_name_res[0]['is_invoice'];
                }
                $r[0]['mch_name'] = $mch_name;

                $remarks = '';
                if($r[0]['remarks'] != '')
                {
                    if(Tools::is_serialized($r[0]['remarks']))
                    {
                        $remarks0 = unserialize($r[0]['remarks']); // 订单备注
                        foreach ($remarks0 as $k => $v)
                        {
                            $remarks .= $v.',';
                        }
                        $remarks = rtrim($remarks,',');
                    }
                    else
                    {
                        $remarks = $r[0]['remarks'];
                    }
                }
                $r[0]['remarks'] = $remarks; // 订单备注

                $coupon_id = $r[0]['coupon_id']; // 优惠券ID
                $r[0]['coupon_price'] = $r[0]['coupon_price'] - $r[0]['preferential_amount']; // 店铺优化金额 = 优惠金额 - 平台优惠金额
                $discount_type = '';
                $zifuchuan = trim(strrchr($coupon_id, ','),',');
                if ($zifuchuan != 0 )
                {
                    $discount_type = '优惠券';
                }
                $r[0]['discount_type'] = $discount_type;

                $array = array('store_id'=>$store_id,'sNo'=>$sNo);
                $logistics['list'] = PC_Tools::View_logistics($array);
                $r[0]['logistics'] = $logistics; // 物流信息

                $get_button_array = array('store_id'=>$store_id,'userid'=>$user_id,'sNo'=>$sNo,'otype'=>$otype,'pay'=>$r[0]['pay'],'status'=>$status,'review_status'=>$r[0]['review_status'],'self_lifting'=>$self_lifting,'delivery_status'=>$delivery_status,'arrive_time'=>$arrive_time,'settlement_status'=>$settlement_status,'is_invoice'=>$is_invoice);
                $get_button_list = PC_Tools::get_order_button($get_button_array); // 获取订单按钮（申请售后和评论除外）

                // 根据订单号,查询订单详情
                foreach($sNo_list as $k_list => $v_list)
                {
                    $str1 = "id,p_id,p_name,p_price,num,sid,size,unit,r_status,after_discount,exchange_num,settlement_type,anchor_id";
                    $list_od = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$v_list])->field($str1)->select()->toArray();
                    if ($list_od)
                    {
                        foreach ($list_od as $key => $values)
                        {
                            $order_details_id = $values['id']; // 订单详情id
                            $p_id = $values['p_id']; // 产品id
                            $sid = $values['sid']; // 属性id
                            $p_price = $values['p_price']; // 商品售价
                            $p_num = $values['num']; // 商品数量
                            $r_status = $values['r_status'];
                            $exchange_num = $values['exchange_num']; // 换货次数
                            $settlement_type = $values['settlement_type']; // 订单结算标识 0未结算 1已结算

                            if($otype == 'IN')
                            { // 积分订单
                                // 根据积分商品ID，查询商品ID和兑换积分
                                $in_res = IntegralGoodsModel::where('id',$p_id)->field('goods_id,integral')->select()->toArray();
                                $p_id = $in_res[0]['goods_id']; // 商品ID
                                $values['allow'] = $in_res[0]['integral']; // 兑换积分
                            }
                            else if($otype == 'MS')
                            { // 秒杀订单
                                // 根据秒杀商品ID，查询商品ID
                                $ms_res = SecondsActivityModel::where('id',$p_id)->field('goodsId')->select()->toArray();
                                $p_id = $ms_res[0]['goodsId']; // 商品ID
                            }
                            else if($otype == 'JP')
                            { // 竞拍订单
                                $values['pluginId'] = $values['p_id'];
                                // 根据秒杀商品ID，查询商品ID
                                $jp_res = AuctionProductModel::where('id',$p_id)->field('goods_id')->select()->toArray();
                                $p_id = $jp_res[0]['goods_id']; // 商品ID
                                $values['p_id'] = $p_id;
                            }

                            //根据产品id 查询店铺id
                            $mch_id_res = ProductListModel::where(['store_id'=>$store_id,'id'=>$p_id])->field('mch_id,write_off_settings,write_off_mch_ids,is_appointment')->select()->toArray();
                            if($mch_id_res)
                            {
                                $shop_id = $mch_id_res[0]['mch_id'];

                                if($otype == 'VI')
                                {
                                    $show_appointment = 0; // 0.不展示预约信息 1.展示预约信息
                                    $show_write_store = 0; // 是否显示适用核销门店
                                    $write_store_num = 0; // 适用核销门店数量
                                    
                                    $write_off_settings = $mch_id_res[0]['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
                                    $write_off_mch_ids = $mch_id_res[0]['write_off_mch_ids']; // 核销门店id  0全部门店,  1,2,3使用逗号分割
                                    $is_appointment = $mch_id_res[0]['is_appointment']; // 预约时间设置 1.无需预约下单 2.需要预约下单
                                    if($write_off_settings == 1)
                                    { // 线下核销
                                        $show_write_store = 1; // 是否显示适用核销门店
                                        if($is_appointment == 2)
                                        { // 需要预约下单
                                            $show_appointment = 1; // 0.不展示预约信息 1.展示预约信息
                                        }
                                        else
                                        { // 无需预约下单
                                           
                                        }
                                        if($write_off_mch_ids == 0)
                                        {
                                            $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->field('id')->select()->toArray();
                                            if($r0_0)
                                            {
                                                $write_store_num = count($r0_0); // 适用核销门店数量
                                            }
                                        }
                                        else
                                        {
                                            $write_store_num = count(explode(',',$write_off_mch_ids));
                                        }
                                    }

                                    $VI_list = array('show_appointment'=>$show_appointment,'show_write_store'=>$show_write_store,'write_store_num'=>$write_store_num);
                                    $values['vi_list'] = $VI_list;
                                }

                                $values['shop_id'] = $shop_id;
                                $values['shop_name'] = '';
                                $values['shop_head'] = '';
                                $mch_res = MchModel::where(['id'=>$shop_id,'recovery'=>0])->field('name,head_img')->select()->toArray();
                                if ($mch_res)
                                {
                                    $values['shop_name'] = $mch_res[0]['name'];
                                    $values['shop_head'] = ServerPath::getimgpath($mch_res[0]['head_img'],$store_id);
                                }
                            }

                            $sql_c = "select img from lkt_configure where id = '$sid' ";
                            $r_c = Db::query($sql_c);
                            if($r_c)
                            {
                                $values['imgurl'] = ServerPath::getimgpath($r_c[0]['img'],$store_id);
                            }

                            $get_button_array = array('store_id'=>$store_id,'user_id'=>$user_id,'id'=>$order_details_id,'sNo'=>$v_list,'otype'=>$otype,'pay'=>$r[0]['pay'],'p_id'=>$p_id,'sid'=>$sid,'self_lifting'=>$self_lifting,'r_status'=>$r_status,'settlement_type'=>$settlement_type);
                            $get_order_details_button = PC_Tools::get_product_button($get_button_array); // 获取商品按钮

                            $get_button_array = array('otype'=>$otype,'status'=>$status,'self_lifting'=>$self_lifting,'order_details_num'=>count($list_od),'get_button_list'=>$get_button_list,'get_order_details_button'=>$get_order_details_button,'Number_of_cycles'=>$key);
                            $get_order_button = PC_Tools::get_order_button_assistant($get_button_array); // 获取订单按钮（申请售后和评论）
                            $get_button_list = $get_order_button['get_button_list']; // 订单详情按钮数组
                            $values['get_order_details_button'] = $get_order_button['get_order_details_button']; // 获取商品按钮

                            $list[] = $values;
                        }
                    }
                }

                $r[0]['list'] = $list; // 商品信息
                $r[0]['get_button_list'] = $get_button_list; // 订单详情底部按钮数组

                ob_clean();
                $message = Lang('Success');
                echo json_encode(array('code' => "200", 'data' => $r[0], 'message' => $message));
                exit();
            }
            else
            {
                ob_clean();
                $message = Lang('Parameter error');
                echo json_encode(array('code' => ERROR_CODE_CSCW, 'message' => $message));
                exit();
            }
        }
        else
        {
            ob_clean();
            $message = Lang('Illegal invasion');
            echo json_encode(array('code' => ERROR_CODE_WLYC, 'message' => $message));
            exit;
        }
        return;
    }

    // 订单详情(移动端)
    public static function app_order_details($action)
    {
        $store_id = $action->store_id;
        $id = $action->id;
        $lktlog = $action->lktlog;
        $user_list = $action->user;
        $user_id = $action->userid;
        $access_id = $action->access_id;

        $integral = array();
        $sNo_list = array();
        $storeSelfInfo = array();

        $admin_shop_id = PC_Tools::SelfOperatedStore($store_id);

        $r_2 = Tools::getOrderConfig($store_id,$id);
        if ($r_2)
        {
            $order_failure = $r_2['order_failure']; // 订单失效
            $order_after = $r_2['order_after']; // 订单售后时限
            $company = $r_2['company'];
        }
        else
        {
            $order_after = 7; // 订单售后时限
            $order_failure = 2;
            $company = "day";
        }
        $time = date('Y-m-d H:i:s');
        $payment_config = Tools::getPayment($store_id);// 支付方式配置
        
        $appointment = array();
        $cancel_method = 0; // 订单取消方式 0 定时任务取消订单  1 手动点击取消订单
        $isDfkGbi = 0; // 0.其他正常关闭 1.待付款关闭订单
        $isOrderDelBtn = 0; // 订单是否可以删除
        $show_appointment = 0; // 0.不展示预约信息 1.展示预约信息
        $show_write_store = 0; // 是否显示适用核销门店
        $write_store_num = 0; // 适用核销门店数量
        $list = array();
        $batch_del = 0; // 有批量退货按钮
        $anchor_name = '';
        // 根据微信id,查询用户id
        $access = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('login_num,verification_time,password')->select()->toArray();
        if (!empty($access))
        {
            $login_num = $access[0]['login_num']; // 支付密码错误次数
            $verification_time = $access[0]['verification_time']; // 支付密码验证时间
            $verification_time = date('Y-m-d H:i:s', strtotime('+1 day', strtotime($verification_time)));
            $user_password = $access[0]['password'];
            if ($user_password != '')
            {
                $password_status = 1;
            }
            else
            {
                $password_status = 0;
            }
            if ($login_num == 5)
            {
                if ($time < $verification_time)
                {
                    $enterless = false;
                }
                else
                {
                    $sql = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->find();
                    $sql->login_num = 0;
                    $sql->save();
                    $enterless = true;
                }
            }
            else
            {
                $enterless = true;
            }

            $r_o0 = OrderModel::where(['store_id'=>$store_id,'id'=>$id])->select()->toArray();
            if($r_o0)
            {
                $sNo = $r_o0[0]['sNo']; // 订单号
                $otype = $r_o0[0]['otype']; // 订单号
                
                if($otype == 'GM')
                {
                    $r_o1 = OrderModel::where(['store_id'=>$store_id,'p_sNo'=>$sNo,'user_id'=>$user_id])->select()->toArray();
                    if($r_o1)
                    {
                        foreach($r_o1 as $ko1 => $vo1)
                        {
                            $sNo_list[] = $vo1['sNo'];
                        }
                    }
                    else
                    {
                        $sNo_list[] = $sNo;
                    }
                }
                else
                {
                    $sNo_list[] = $sNo;
                }
            }

            $isInvoice = false; // 不可以开发票
            $r = OrderModel::where(['store_id'=>$store_id,'id'=>$id])->select()->toArray();
            if ($r)
            {   
                $recycle = $r[0]['recycle'];
                if(count($sNo_list) == 1)
                {
                    if($recycle != 0)
                    {
                        ob_clean();
                        $message = Lang('order.36');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit();
                    }
                }

                $r[0]['offset_balance'] = (float)$r[0]['offset_balance'];
                $r[0]['old_total'] = (float)$r[0]['old_total'];
                $r[0]['preferential_amount'] = (float)$r[0]['preferential_amount'];
                $r[0]['reduce_price'] = (float)$r[0]['reduce_price'];
                $r[0]['spz_price'] = (float)$r[0]['spz_price'];
                $r[0]['z_freight'] = (float)$r[0]['z_freight'];
                $r[0]['z_price'] = (float)$r[0]['z_price'];
                $r[0]['zhekou'] = (float)$r[0]['zhekou'];
                $r[0]['manual_offer'] = (float)$r[0]['manual_offer'];
                $r[0]['comm_discount'] = (float)$r[0]['comm_discount'];
                $r[0]['coupon_price'] = (float)$r[0]['coupon_price'];
                $r[0]['mchRecycle'] = $r[0]['mch_recycle'];
                $r[0]['storeRecycle'] = $r[0]['store_recycle'];
                $r[0]['userRecycle'] = $r[0]['user_recycle'];

                $exchange_rate = $r[0]['exchange_rate'];
                $currency_symbol = $r[0]['currency_symbol'];
                $currency_code = $r[0]['currency_code'];
                $cancel_method = $r[0]['cancel_method'];
                $mch_id = $r[0]['mch_id'];
                $mch_id = explode(',', $mch_id);
                $mch_id = $mch_id[1];
                $mch_name = '';
                $is_invoice = 0; // 是否支持开票 0.否 1.是
                $mch_name_res = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name,is_invoice')->select()->toArray();
                if (!empty($mch_name_res))
                {
                    $mch_name = $mch_name_res[0]['name'];
                    $is_invoice = $mch_name_res[0]['is_invoice'];
                }

                if($r[0]['arrive_time'] != '' && $r[0]['settlement_status'] == 1 && $r[0]['status'] == 5 && $is_invoice == 1)
                { // 收货时间不为空 并且 已结算 并且 订单状态为已完成 并且 店铺支持开票
                    $isInvoice = true; // 可以开发票
                }
                $sNo = $r[0]['sNo']; // 订单号
                $real_sno = $r[0]['real_sno']; // 订单号
                $z_price = $r[0]['z_price']; // 总价
                $z_freight = $r[0]['z_freight']; // 总运费
                $status = $r[0]['status'];//订单状态
                if ($r[0]['otype'] == 'pt' && $r[0]['z_price'] != $r[0]['offset_balance'] && $status != 0)
                {
                    $z_price = $r[0]['z_price'] + $r[0]['offset_balance'];
                }
                $old_total = $r[0]['old_total']; // 总价
                if($r[0]['old_total'] == '0.00' || $r[0]['old_total'] == '')
                {
                    $old_total = $z_price;
                }
                $old_freight = $r[0]['old_freight']; // 总价
                
                $hand_del = 0;
                if ($status == 6)
                {
                    //如果订单是关闭状态，查询是手动关闭还是自动guanbi
                    $del_type = RecordModel::where('type',23)
                                        ->where('event','like',$sNo)
                                        ->select()
                                        ->toArray();
                    if (!empty($del_type))
                    {
                        $hand_del = 1;
                    }
                }
                $add_time = $r[0]['add_time']; // 订单时间
                $remarks = '';
                if($r[0]['remarks'] != '')
                {
                    if(Tools::is_serialized($r[0]['remarks']))
                    {
                        $remarks0 = unserialize($r[0]['remarks']); // 订单备注
                        foreach ($remarks0 as $k => $v)
                        {
                            $remarks .= $v.',';
                        }
                        $remarks = rtrim($remarks,',');
                    }
                    else
                    {
                        $remarks = $r[0]['remarks'];
                    }
                }
                $name = $r[0]['name'];//联系人
                $cpc = $r[0]['cpc'];//联系人
                $mobile1 = $r[0]['mobile'];//联系手机号
                $mobile = substr_replace($mobile1, '****', 3, 4);//隐藏操作
                $address = $r[0]['address'];//联系地址
                $otype = $r[0]['otype'];//订单类型
                $ptcode = $r[0]['ptcode'];//拼团编号
                $pid = $r[0]['pid'];//拼团订单类型
                $coupon_activity_name = $r[0]['coupon_activity_name'];//自动满减名称
                if ($r[0]['reduce_price'] != 0)
                {
                    $coupon_activity_name = "￥" . $r[0]['reduce_price'];
                }
                $coupon_id = $r[0]['coupon_id'];// 优惠券ID
                $subtraction_id = $r[0]['subtraction_id'];// 满减活动ID
                $coupon_price = $r[0]['coupon_price'];// 优惠券金额
                $reduce_price = $r[0]['reduce_price'];// 满减金额
                $offset_balance = $r[0]['offset_balance'];// 余额抵扣
                $comm_discount = $r[0]['comm_discount'];// 分销折扣
                $comm_amount = 0;
                if($comm_discount > 0)
                {
                    $comm_amount = round($r[0]['spz_price'] * (100 - $comm_discount),2);// 分销优惠
                }
                $mch_id = trim($r[0]['mch_id'], ','); // 店铺ID
                $grade_rate = (float)$r[0]['grade_rate'];//会员等级折扣
                $delivery_status = $r[0]['delivery_status'];//提醒状态
                $self_lifting = $r[0]['self_lifting']; // 是否自提
                $single_store = $r[0]['single_store']; // 自提门店
                $preferential_amount = $r[0]['preferential_amount']; // 优惠金额
                $wx_order_status = $r[0]['wx_order_status']; // 微信小程序订单状态枚举：(1) 待发货；(2) 已发货；(3) 确认收货；(4) 交易完成；(5) 已退款。
                $settlement_status = $r[0]['settlement_status']; // 结算状态
                $coupon_name = '';

                $sql0 = "select a.activity_type,a.discount from lkt_coupon_activity as a left join lkt_coupon as b on a.id = b.hid where a.store_id = '$store_id' and b.id = '$coupon_id'";
                $r0 = Db::query($sql0);
                if ($r0)
                {
                    if ($r0[0]['activity_type'] == 2)
                    {
                        $coupon_name = '(' . $r0[0]['discount'] * 10 . '折)';
                    }
                }

                if ($pid == 'cantuan')
                {            //参团订单，查询团的结束时间
                    $ptcode = $r[0]['ptcode'];
                    $gstatus = GroupOpenModel::where('ptcode',$ptcode)->select()->toArray();
                    $gstatus = $gstatus[0];
                }
                else
                {
                    $gstatus = array();
                }
                $user_score = 0;
                if ($status)
                { // 付款
                    $user_money = false;
                }
                else
                { // 未付款
                    $o_user_money_res = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('money,score')->select()->toArray();
                    if ($o_user_money_res)
                    {
                        $user_money = $o_user_money_res[0]['money'];
                        $user_score = $o_user_money_res[0]['score'];
                    }
                    else
                    {
                        $user_money = false;
                    }
                }

                $product_total = 0;
                $z_freight = 0;
                $discount_type = '';
                $zifuchuan = trim(strrchr($coupon_id, ','),',');
                if ($zifuchuan != 0 )
                {
                    $discount_type = '优惠券';
                }

                $coupon_price = $coupon_price - $preferential_amount;
                $grade_rate_amount = 0; // 会员优惠金额

                $array = array('store_id'=>$store_id,'sNo'=>$sNo);
                $logistics['list'] = PC_Tools::View_logistics($array);
                $allRefundNum = 0;
                $p_id = 0;
                $user_can_after = true; // 售后截至状态 真：可以售后  假：过了售后时效

                $get_button_array = array('store_id'=>$store_id,'userid'=>$user_id,'sNo'=>$sNo,'pay'=>$r[0]['pay'],'otype'=>$otype,'status'=>$status,'review_status'=>$r[0]['review_status'],'self_lifting'=>$self_lifting,'delivery_status'=>$delivery_status,'arrive_time'=>$r[0]['arrive_time'],'settlement_status'=>$settlement_status,'is_invoice'=>$is_invoice);
                $get_button_list = PC_Tools::get_order_button($get_button_array); // 获取订单按钮（申请售后和评论除外）

                // 根据订单号,查询订单详情
                foreach($sNo_list as $k_list => $v_list)
                {
                    $list_od = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$v_list])->select()->toArray();
                    if ($list_od)
                    {
                        $batch = array();
                        $courier_num_arr = array();
                        foreach ($list_od as $key => $values)
                        {
                            $p_price = $values['p_price']; // 商品售价
                            $p_num = $values['num']; // 商品数量
                            $after_discount = $values['after_discount']; // 优惠后的金额
                            $settlement_type = $values['settlement_type'];//订单结算标识 0未结算 1已结算
                            $store_self_delivery = $values['store_self_delivery']; // 商家自配信息id
                            
                            if($otype != 'IN')
                            {
                                $grade_rate_amount += round(($p_price * (1 - ($grade_rate* 0.1))) * $p_num,2);
                            }
                            
                            $p_id = $values['p_id']; // 产品id
                            if($otype == 'IN')
                            {
                                $in_res = IntegralGoodsModel::where('id',$p_id)->field('goods_id,integral')->select()->toArray();
                                $p_id = $in_res[0]['goods_id'];
                                if($values['p_integral'] == 0)
                                {
                                    $values['allow'] = $in_res[0]['integral'];
                                    $values['scoreDeduction'] = $in_res[0]['integral'];
                                }
                                else
                                {
                                    $values['allow'] = $values['p_integral'];
                                    $values['scoreDeduction'] = $values['p_integral'];
                                }
                            }
                            elseif($otype == 'MS')
                            {
                                $ms_res = SecondsActivityModel::where('id',$p_id)->field('goodsId')->select()->toArray();
                                $p_id = $ms_res[0]['goodsId'];
                            }
                            elseif($otype == 'JP')
                            {
                                $values['pluginId'] = $values['p_id'];
                                $jp_res = AuctionProductModel::where('id',$p_id)->field('goods_id')->select()->toArray();
                                $p_id = $jp_res[0]['goods_id'];
                                $values['p_id'] = $p_id;
                            }

                            if($otype == 'ZB')
                            {
                                $anchor_id = $values['anchor_id'];
                                $sql_anchor_id = "select user_name from lkt_user where user_id = '$anchor_id' ";
                                $r_anchor_id = Db::query($sql_anchor_id);
                                $anchor_name = $r_anchor_id[0]['user_name'];
                            }
                            $sid = $values['sid'];//属性id

                            $values['comments_type'] = 0;//不显示
                            //判断订单评论状态
                            $res_c = CommentsModel::where(['store_id'=>$store_id,'pid'=>$p_id,'uid'=>$user_id,'oid'=>$sNo])->select()->toArray();
                            if(!empty($res_c))
                            {
                                $values['comments_type'] = 2;//待追评
                                if($res_c[0]['review'])
                                {
                                    $values['comments_type'] = 3;//评论完成
                                }
                            }
                            else
                            {
                                if($values['r_status'] == 5)
                                {
                                    $values['comments_type'] = 1;//待评价
                                }
                            }

                            //根据产品id 查询店铺id
                            $mch_id_res = ProductListModel::where(['store_id'=>$store_id,'id'=>$p_id])->field('mch_id,write_off_settings,write_off_mch_ids,is_appointment')->select()->toArray();
                            if (!empty($mch_id_res))
                            {
                                $mch_id = $mch_id_res[0]['mch_id'];
                                $write_off_settings = $mch_id_res[0]['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
                                $write_off_mch_ids = $mch_id_res[0]['write_off_mch_ids']; // 核销门店id  0全部门店,  1,2,3使用逗号分割
                                $is_appointment = $mch_id_res[0]['is_appointment']; // 预约时间设置 1.无需预约下单 2.需要预约下单
                                if($write_off_settings == 1)
                                { // 线下核销
                                    $show_write_store = 1; // 是否显示适用核销门店
                                    if($is_appointment == 2)
                                    { // 需要预约下单
                                        $show_appointment = 1; // 0.不展示预约信息 1.展示预约信息
                                    }
                                    else
                                    { // 无需预约下单
                                       
                                    }
                                    if($write_off_mch_ids == 0)
                                    {
                                        $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('id')->select()->toArray();
                                        if($r0_0)
                                        {
                                            $write_store_num = count($r0_0); // 适用核销门店数量
                                        }
                                    }
                                    else
                                    {
                                        $write_store_num = count(explode(',',$write_off_mch_ids));
                                    }
                                }
                                
                                $mch_res = MchModel::where(['id'=>$mch_id,'recovery'=>0])->field('id,name,logo,poster_img,head_img')->select()->toArray();
                                if ($mch_res)
                                {
                                    $values['shop_id'] = $mch_id;
                                    $values['shop_name'] = $mch_res[0]['name'];
                                    $values['shop_logo'] = ServerPath::getimgpath($mch_res[0]['logo']);
                                    $values['posterImg'] = ServerPath::getimgpath($mch_res[0]['poster_img'],$store_id);
                                    $values['shop_head'] = ServerPath::getimgpath($mch_res[0]['head_img'],$store_id);
                                }
                                else
                                {
                                    $values['shop_id'] = $mch_id;
                                    $values['shop_name'] = '';
                                    $values['shop_logo'] = '';
                                    $values['posterImg'] = '';
                                    $values['shop_head'] = '';
                                }
                            }
                            $values['is_appointment'] = $is_appointment;
                            $values['mchRecycle'] = $values['mch_recycle'];
                            $values['storeRecycle'] = $values['store_recycle'];
                            $values['userRecycle'] = $values['user_recycle'];
                            
                            $p_price = $values['p_price'];//商品单价
                            $num = $values['num'];// 数量
                            $freight = $values['freight']; // 运费
                            $product_total += $p_price * $num; // 商品总价
                            //如果为竞拍商品，重新计算总价
                            $z_freight += $freight; // 运费总价
                            $arrive_time = $values['arrive_time']; // 到货时间
                            $arrive_times = strtotime($arrive_time);
                            $order_after_times = $order_after * 24 * 60 * 60;
                            if($arrive_times != '')
                            { // 已经确认收货
                                if ((time() - $order_after_times) > $arrive_times || $settlement_type == 1)
                                { // 当前时间戳 - 售后时间戳 > 到货时间戳 或者 已结算
                                    $user_can_after = false;
                                }
                            }
                            $details_id = $values['id'];//订单详情ID

                            /*售后按钮判断开始*/
                            $returnInfo = Tools::getReturnOrderStatus($store_id,$values['id']);
                            
                            $values['refund'] = $returnInfo['refund'];
                            $values['refundAmt'] = $returnInfo['refundAmt'];
                            $values['refundAmtBtn'] = $returnInfo['refundAmtBtn'];
                            $values['refundGoods'] = $returnInfo['refundGoods'];
                            $values['refundGoodsAmt'] = $returnInfo['refundGoodsAmt'];
                            $values['refundShowBtn'] = $returnInfo['refundShowBtn'];
                            if($status == 1 && $otype == 'GM' && $self_lifting != 2)
                            {
                                $values['refund'] = false;
                            }
                            if($returnInfo['refund'])
                            {
                                $allRefundNum++;
                            }
                            
                            if ($user_can_after == false) 
                            {
                                $values['refund'] = false;
                            }
                            /*售后按钮判断结束*/

                            $date = date('Y-m-d 00:00:00', strtotime('-7 days'));

                            if ($arrive_time != '')
                            {
                                if ($arrive_time < $date)
                                {
                                    $values['info'] = 1; // 到货时间少于7天
                                }
                                else
                                {
                                    $values['info'] = 0; // 已经到货
                                }
                            }
                            else
                            {
                                $values['info'] = 0; // 还没到货
                            }

                            $values['add_time'] = strtotime($values['add_time']);
                            $values['after_discount'] = (float)$values['after_discount'];
                            $values['deliver_time'] = strtotime($values['deliver_time']);
                            $values['freight'] = (float)$values['freight'];
                            $values['manual_offer'] = (float)$values['manual_offer'];
                            $values['p_price'] = (float)$values['p_price'];
                            $values['real_money'] = (float)$values['real_money'];
                            $values['deliverNum'] = $values['deliver_num'];
                            
                            $get_button_array = array('store_id'=>$store_id,'user_id'=>$user_id,'id'=>$values['id'],'sNo'=>$v_list,'otype'=>$otype,'pay'=>$r[0]['pay'],'p_id'=>$p_id,'sid'=>$sid,'self_lifting'=>$self_lifting,'r_status'=>$values['r_status'],'settlement_type'=>$settlement_type);
                            $get_order_details_button = PC_Tools::get_product_button($get_button_array); // 获取商品按钮

                            $get_button_array = array('otype'=>$otype,'status'=>$status,'self_lifting'=>$self_lifting,'order_details_num'=>count($list_od),'get_button_list'=>$get_button_list,'get_order_details_button'=>$get_order_details_button,'Number_of_cycles'=>$key);
                            $get_order_button = PC_Tools::get_order_button_assistant($get_button_array); // 获取订单按钮（申请售后和评论）
                            $get_button_list = $get_order_button['get_button_list']; // 订单详情按钮数组
                            $values['get_order_details_button'] = $get_order_button['get_order_details_button']; // 获取商品按钮

                            $arr = $values;
                            // 根据产品id,查询产品列表 (产品图片)
                            $sql = "select a.imgurl,a.product_title,a.is_distribution,c.img,a.recycle from lkt_product_list AS a 
                            LEFT JOIN lkt_configure AS c ON a.id = c.pid 
                            where a.store_id = '$store_id' AND c.id= '$sid' ";
                            $rrr = Db::query($sql);
                            if($rrr[0]['img'])
                            {
                                $url = ServerPath::getimgpath($rrr[0]['img']);
                            }
                            else
                            {
                                $url = ServerPath::getimgpath($rrr[0]['imgurl']); // 拼图片路径
                            }
                            if ($otype == 'IN')
                            {
                                $arr['after_discount'] = $values['allow'];
                                $url = ServerPath::getimgpath($rrr[0]['img']); // 拼图片路径
                            }
                            $arr['imgurl'] = $url;
                            $arr['sid'] = $sid;
                            $arr['is_distribution'] = $rrr[0]['is_distribution'];//是否为分销商品
                            $arr['recycle'] = $rrr[0]['recycle'];
                            $product[] = $arr;

                            $r_status = $values['r_status']; // 订单详情状态
                            if ($r_status)
                            {
                                $res_o = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo,'r_type'=>0,'r_status'=>4])->count();

                                $res_d = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->count();
                                // 如果订单下面的商品都处在同一状态,那就改订单状态为已完成
                                if ($res_o == $res_d)
                                {
                                    //如果订单数量相等 则修改父订单状态
                                    $sql = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->find();
                                    $sql->status = $r_status;
                                    $r = $sql->save();
                                    if (!$r)
                                    {
                                        $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改订单状态失败！sNo:" . $sNo);
                                    }
                                }
                            }
                            else
                            {
                                $status = $r_status;
                            }
                        }

                        $batch_del = 0; // 有批量退货按钮
                        foreach ($batch as $key => $val)
                        {
                            if ($val == 1)
                            {
                                $batch_del = 1; // 没有批量退货按钮
                                break;
                            }
                        }
                        $payment = $z_price;
                        $list = $product;
                    }
                    else
                    {
                        // $logistics = array();
                        $batch_del = 0;
                        $p_id = '';
                    }
                }
                
                $order_no = '';
                if ($otype == 'KJ')
                {
                    $order_no_res = OrderModel::where('id',$id)->field('p_sNo')->select()->toArray();
                    $order_no = $order_no_res[0]['p_sNo'];
                }
                $allow = '';
                $p_sNo = '';
                if ($otype == 'IN')
                {
                    $order_no_res = OrderModel::where('id',$id)->field('allow,p_sNo')->select()->toArray();
                    $allow = $order_no_res[0]['allow'];
                    $p_sNo = $order_no_res[0]['p_sNo'];
                }
                $is_end = false;

                $sel_end_sql = "select tt.* from (SELECT *,row_number () over (PARTITION BY product_id) AS top FROM lkt_group_product WHERE product_id = '$p_id' and store_id = '$store_id' and is_delete = 0) as tt where tt.top<2 ";
                $res_end = Db::query($sel_end_sql);
                if ($res_end)
                {
                    $group_cfg = $res_end[0]['group_data'];
                    $group_data = unserialize($group_cfg);
                    $end_time = $group_data['endtime'];
                    $end_time = strtotime($end_time);

                    if ($end_time < time())
                    {
                        $is_end = true;
                    }
                }

                $user_can_open = true;
                $user_can_can = true;
                $user_can_ms = true;
                $user_can_buy_ms = true;
                $isagain_can = false;
                $isagain_open = false;
                $isinpt = false;

                $sellInfo = (object)array();
                $live_img = '';
                $anchor_name = '';

                if($otype == 'PS')
                {
                    $r_pre_sell_record = PreSellRecordModel::where('sNo',$sNo)->field('price')->select()->toArray();
                    $r_price = $r_pre_sell_record[0]['price']; // 当前付款金额
                    
                    $r_pre_sell_goods = PreSellGoodsModel::where('product_id',$p_id)->field('sell_type,deposit,delivery_time,deadline,balance_pay_time')->select()->toArray();
                    if($r_pre_sell_goods)
                    {
                        if($r_pre_sell_goods[0]['sell_type'] == 1)
                        { // 定金
                            $balance_pay_time = $r_pre_sell_goods[0]['balance_pay_time'];
                            $sellInfo->sellType = $r_pre_sell_goods[0]['sell_type'];
                            $sellInfo->deliveryTime = $r_pre_sell_goods[0]['delivery_time'];
                            $sellInfo->deposit = round($r_pre_sell_goods[0]['deposit'],2);
                            $sellInfo->balance = round($r_pre_sell_goods[0]['deposit'],2);
                            $sellInfo->startTime = $r_pre_sell_goods[0]['balance_pay_time'];
                            $sellInfo->endTime = date("Y-m-d 23:59:59",strtotime($r_pre_sell_goods[0]['balance_pay_time']));
                            $sellInfo->canPay = false;
                            if($sellInfo->startTime <= $time && $time <= $sellInfo->endTime )
                            {
                                $sellInfo->canPay = true;
                            }
                            
                            $sellInfo->total = round(($z_price - $r_price),2);
                        }
                        else
                        {
                            $sellInfo->sellType = $r_pre_sell_goods[0]['sell_type'];
                            $sellInfo->deliveryTime = $r_pre_sell_goods[0]['delivery_time'];
                            $sellInfo->endTime = $r_pre_sell_goods[0]['deadline'];
                            $sellInfo->total = round(($z_price),2);
                        }
                    }
                }
                if($otype == 'MS')
                {
                    //获取店铺的秒杀订单结束时间
                    $res_ms = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$admin_shop_id])->field('order_failure')->select()->toArray();
                    if($res_ms)
                    {
                        $order_failure = $res_ms[0]['order_failure']/3600;
                    }
                }
                elseif($otype == 'IN')
                {

                    //获取店铺的积分订单结束时间
                    $res_ms = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$admin_shop_id])->field('order_failure')->select()->toArray();
                    if($res_ms)
                    {
                        $order_failure = $res_ms[0]['order_failure']/3600;
                    }
                }
                elseif($otype == 'ZB')
                {
                    $sql_order_details = "select living_room_id,anchor_id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' ";
                    $r_order_details = Db::query($sql_order_details);
                    if($r_order_details)
                    {
                        $living_room_id = $r_order_details[0]['living_room_id'];
                        $anchor_id = $r_order_details[0]['anchor_id'];

                        $sql_living = "select a.living_img,b.user_name from lkt_living_room as a left join lkt_user as b on a.user_id = b.user_id where a.store_id = '$store_id' and a.id = '$living_room_id' and a.user_id = '$anchor_id' ";
                        $r_living = Db::query($sql_living);
                        if($r_living)
                        {
                            $live_img = $r_living[0]['living_img'];
                            $anchor_name = $r_living[0]['user_name'];
                        }
                    }
                }

                $haveExpress = false;
                $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                $r_order_details = Db::query($sql_order_details);
                if($r_order_details)
                {
                    $haveExpress = true;
                } 

                if ($self_lifting == '3')
                {
                    if($single_store != 0)
                    {
                        $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$single_store])->select()->toArray();
                        if($r0_0)
                        {
                            $sql_order_details = "select write_time from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' ";
                            $r_order_details = Db::query($sql_order_details);
                            $write_time = $r_order_details[0]['write_time'];

                            $array_address = array('cpc'=>$r0_0[0]['cpc'],'sheng'=>$r0_0[0]['sheng'],'shi'=>$r0_0[0]['shi'],'xian'=>$r0_0[0]['xian'],'address'=>$r0_0[0]['address'],'code'=>$r0_0[0]['code']);
                            $address_xq = PC_Tools::address_translation($array_address);
                            $appointment = array('name'=>$r0_0[0]['name'],'address'=>$address_xq,'time'=>$write_time);
                        }
                    }
                }
                else if($self_lifting == 2)
                {
                    $sql_order_details = "select sd.* from lkt_order_details as d left join lkt_self_delivery_info as sd on d.store_self_delivery = sd.id where d.store_id = '$store_id' and d.r_sNo = '$sNo' ";
                    $r_order_details = Db::query($sql_order_details);
                    if($r_order_details)
                    {
                        $storeSelfInfo = $r_order_details[0];
                    }
                }
                                            
                if($status == 7)
                {
                    $isDfkGbi = 1;
                }
                if($status == 7 || $self_lifting == 1 && $settlement_status == 1)
                { // 订单关闭 或者 自提订单 并且 已经结算
                    $isOrderDelBtn = 1;
                }

                $sale_type = 0;
                //判断有无订单售后未结束
                $res_s = ReturnOrderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'sNo'=>$sNo])
                                        ->where('r_type','not in','2,4,5,8,9,10,12')
                                        ->count();
                if($res_s > 0 )
                {
                    $sale_type = 1;
                }

                $sql_s1 = "select * from lkt_return_order where store_id = '$store_id' and sNo = '$sNo' and r_type in (4,9,12,13,15) ";
                $r_s1 = Db::query($sql_s1);
                if($r_s1)
                {
                    $cancel_method = 2;
                }

                //判断订单是否全在售后且未结束
                $res_d = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])
                                        ->where('r_status','<>',7)
                                        ->count();
                if($res_s == $res_d)
                {
                    $sale_type = 2;
                }
                //批量售后
                $allRefund = false;
                if($allRefundNum > 1 and $sale_type < 2)
                {
                    $allRefund = true;
                }
                if($old_freight == '0.00' || $old_freight == '')
                {
                    $old_freight = $z_freight;
                }

                ob_clean();
                $message = Lang('Success');
                if ($otype == 'JP')
                {
                    $r_auction_product = AuctionProductModel::where(['sNo'=>$sNo,'user_id'=>$user_id])->field('price')->select()->toArray();
                    $invoicePrice = round($r_auction_product[0]['price'],2);
                    
                    $r_auction_Config = AuctionConfigModel::where(['store_id'=>$store_id])->field('order_failure')->select()->toArray();
                    $order_failure = $r_auction_Config[0]['order_failure'] / 3600;

                    echo json_encode(array('code' => "200", 'data' => array('status' => $status, 'payment' => $payment_config, 'hand_del' => $hand_del, 'isInvoice' => $isInvoice,'is_end' => $is_end, 'user_can_Open' => $user_can_open, 'user_can_after' => $user_can_after, 'user_can_ms' => $user_can_ms, 'user_can_buy_ms' => $user_can_buy_ms, 'user_can_can' => $user_can_can, 'isagain_can' => $isagain_can, 'isagain_open' => $isagain_open, 'isinpt' => $isinpt, 'remarks' => $remarks, 'gstatus' => $gstatus, 'otype' => $otype, 'pttype' => $pid, 'id' => (int)$id, 'sNo' => $sNo, 'z_price' => (float)$z_price, 'product_total' => $product_total, 'mch_name' => $mch_name, 'z_freight' => $z_freight, 'name' => $name, 'cpc' => $cpc, 'mobile' => $mobile, 'address' => $address, 'add_time' => $add_time, 'r_status' => $status, 'list' => $list, 'user_money' => (float)$user_money, 'logistics' => $logistics, 'order_failure' => $order_failure, 'company' => $company, 'batch_del' => $batch_del, 'coupon_activity_name' => $coupon_activity_name, 'coupon_price' => $coupon_price, 'coupon_name' => $coupon_name, 'enterless' => $enterless, 'offset_balance' => (float)$offset_balance, 'omsg' => $r[0], 'pro_id' => $p_id, 'order_no' => $order_no, 'password_status' => $password_status, 'comm_discount' => (float)$comm_discount, 'delivery_status' => $delivery_status, 'self_lifting' => $self_lifting, 'allow' => $allow,'p_sNo' => $p_sNo,'preferential_amount'=>(float)$preferential_amount,'discount_type'=>$discount_type,'sale_type'=>$sale_type,'sellInfo'=>$sellInfo,'message'=>$sNo,'invoicePrice'=>$invoicePrice,'invoiceTimeout'=>false,'isOrderDelBtn'=>false, 'old_total' => (float)$old_total, 'old_freight' => (float)$old_freight,'appointment'=>$appointment,'haveExpress'=>$haveExpress,'real_sno'=>$real_sno,'cancel_method'=>$cancel_method,'isDfkGbi'=>$isDfkGbi,'isOrderDelBtn'=>$isOrderDelBtn,'show_appointment'=>$show_appointment,'show_write_store'=>$show_write_store,'write_store_num'=>$write_store_num,'wx_order_status'=>$wx_order_status,'storeSelfInfo'=>$storeSelfInfo,'live_img'=>$live_img,'anchor_name'=>$anchor_name,'get_button_list'=>$get_button_list,'currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code), 'message' => $message));
                }
                else
                {
                    echo json_encode(array('code' => "200", 'data' => array('status' => $status, 'payment' => $payment_config, 'hand_del' => $hand_del, 'isInvoice' => $isInvoice,'is_end' => $is_end, 'user_can_open' => $user_can_open, 'user_can_after' => $user_can_after, 'user_can_ms' => $user_can_ms, 'user_can_buy_ms' => $user_can_buy_ms, 'user_can_can' => $user_can_can, 'isagain_can' => $isagain_can, 'isagain_open' => $isagain_open, 'isinpt' => $isinpt, 'remarks' => $remarks, 'gstatus' => $gstatus, 'otype' => $otype, 'pttype' => $pid, 'id' => (int)$id, 'sNo' => $sNo, 'z_price' => (float)$z_price, 'product_total' => $product_total, 'mch_name' => $mch_name, 'z_freight' => $z_freight, 'name' => $name, 'cpc' => $cpc, 'mobile' => $mobile, 'address' => $address, 'add_time' => $add_time, 'r_status' => $status, 'list' => $list, 'user_money' => (float)$user_money,'user_score' => (float)$user_score, 'logistics' => $logistics, 'order_failure' => $order_failure, 'company' => $company, 'batch_del' => $batch_del, 'coupon_activity_name' => $coupon_activity_name, 'coupon_price' => $coupon_price, 'coupon_name' => $coupon_name, 'enterless' => $enterless, 'offset_balance' => (float)$offset_balance, 'omsg' => $r[0], 'pro_id' => $p_id, 'order_no' => $order_no, 'password_status' => $password_status, 'comm_discount' => (float)$comm_discount,  'comm_amount' => (float)$comm_amount, 'delivery_status' => $delivery_status, 'self_lifting' => $self_lifting, 'allow' => $allow,'p_sNo' => $p_sNo,'preferential_amount'=>(float)$preferential_amount,'discount_type'=>$discount_type,'sale_type'=>$sale_type,'sellInfo'=>$sellInfo,'message'=>$sNo,'invoicePrice'=>0,'invoiceTimeout'=>false,'grade_rate_amount'=>(float)$grade_rate_amount,'allRefund'=>$allRefund, 'old_total' => (float)$old_total, 'old_freight' => (float)$old_freight,'appointment'=>$appointment,'haveExpress'=>$haveExpress,'real_sno'=>$real_sno,'cancel_method'=>$cancel_method,'isDfkGbi'=>$isDfkGbi,'isOrderDelBtn'=>$isOrderDelBtn,'show_appointment'=>$show_appointment,'show_write_store'=>$show_write_store,'write_store_num'=>$write_store_num,'wx_order_status'=>$wx_order_status,'storeSelfInfo'=>$storeSelfInfo,'live_img'=>$live_img,'anchor_name'=>$anchor_name,'get_button_list'=>$get_button_list,'currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code), 'message' => $message));
                }
                exit();
            }
            else
            {
                ob_clean();
                $message = Lang('Parameter error');
                echo json_encode(array('code' => ERROR_CODE_CSCW, 'message' => $message));
                exit();
            }
        }
        else
        {
            ob_clean();
            $message = Lang('Illegal invasion');
            echo json_encode(array('code' => ERROR_CODE_WLYC, 'message' => $message));
            exit;
        }
        return;
    }

    //订单列表（平台）
    public static function order_index($action)
    {
        $store_id = $action->store_id;
        $store_type = $action->store_type;
        $ordtype1 = 'ti/普通订单,';
        $Jurisdiction = new Jurisdiction();
        // $Plugin = new Plugin();
        // $Plugin_arr = $Plugin->is_Plugin1($store_id, 'order');

        // $Plugin_arr['res1'] = rtrim($Plugin_arr['res1'], ',');
        // $ordtype1 = $ordtype1.$Plugin_arr['res1'];

        // $ordtype1 = rtrim($ordtype1, ',');
        // $ordtype2 = explode(',', $ordtype1);
        // foreach ($ordtype2 as $k => $v)
        // {
        //     $ordtype3 = explode('/', $v);
        //     $ordtype[$ordtype3[0]] = $ordtype3[1];
        // }

        $self_operated_store_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID

        $selfLifting = 1;
        if(isset($action->selfLifting))
        {
            $selfLifting = $action->selfLifting;
        }
        if(isset($action->order_label))
        {
            $order_label = $action->order_label;//1普通订单2自提订单3虚拟订单4活动订单
        }
        else
        {
            $order_label = 0;
        }
        if(isset($action->otype))
        {
            $otype = $action->otype;
        }
        if(isset($action->mch_name))
        {
            $mch_name = $action->mch_name; // 请输入店铺名称
        }
        if(isset($action->status))
        {
            $status = $action->status; // 订单状态
        }
        if(isset($action->news_status))
        {
            $news_status = $action->news_status; // 订单状态
        }
        if(isset($action->delivery_status))
        {
            $delivery_status = $action->delivery_status; // 提醒发货
        }
        if(isset($action->readd))
        {
            $readd = $action->readd; // 未查看信息
        }
        if(isset($action->mch_id))
        {
            $mch_id = $action->mch_id; // 店铺ID
        }
        if(isset($action->x_order))
        {
            $x_order = $action->x_order; 
        }
        if(isset($action->operation_type))
        {
            $operation_type = $action->operation_type; 
        }
        if(isset($action->sNo))
        {
            $sNo = $action->sNo;
        }
        if(isset($action->brand))
        {
            $brand = $action->brand;
        }
        if(isset($action->source))
        {
            $source = $action->source;
        }
        if(isset($action->proId))   //商品ID
        {
            $proId = $action->proId;
        }
        // $ostatus = $action->ostatus;
        $prostr = '';
        $URL = '';
        $con = '';
        foreach ($_GET as $key => $value001)
        {
            $con .= "&$key=$value001";
        }
        // 导出
        $exportType = isset($action->exportType)?$action->exportType:0;
        $pagesize = $action->pagesize;
        $pagesize = $pagesize ? $pagesize : '10';
        // 每页显示多少条数据
        $page = $action->page;


        $condition = " and o.supplier_id = 0";
        if($store_type == 7)
        {
            $condition .= " and o.recycle not in (1,3) ";
        }
        else if($store_type == 8)
        {
            $condition .= " and o.recycle != 1 ";
        }
        switch ($order_label) {
            case '2':
                $condition .= " and o.self_lifting = 1 "; 
                break;
            case '3':
                $condition .= " and o.self_lifting in (3,4) and o.otype = 'VI' "; 
                break;
            case '4':
                $condition .= " and o.self_lifting = 0 and o.otype != 'VI' and  o.otype != 'GM' "; 
                break;
            case '1' :
                $condition .= " and o.self_lifting = 0 and o.otype = 'GM'"; 
                break;
        }
        if (isset($brand) && $brand)
        {
            $prostr .= " and lpl.brand_id = '$brand'";
        }
        if (isset($proId) && $proId)
        {
            $goods_id = Db::name('integral_goods')->where('id', $proId)->value('goods_id');
            $condition .= " and p.id = '$goods_id' ";
        }
        $brand_str = '';
        if (isset($mch_name) && $mch_name)
        {   
            $mch_name = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and m.name like $mch_name ";
        }
        if (isset($operation_type) && $operation_type)
        {
            $condition .= " and o.operation_type = '$operation_type' ";
        }
        
        if (isset($source) && $source == 1)
        {
            $condition .= " and o.source = '1' ";
        }
        else if (isset($source) && $source == 2)
        {
            $condition .= " and o.source = '2' ";
        }

        $startdate = $action->startdate;
        $enddate = $action->enddate;
        if (isset($x_order) && $x_order)
        {
            $startdate = date('Y-m-d 00:00:00');
            $enddate = date('Y-m-d 23:59:59');
            $condition .= " and (o.status = 0 or o.status = 1) ";
        }
        if ($startdate != '')
        {
            $condition .= " and o.add_time >= '$startdate' ";
        }
        if ($enddate != '')
        {
            $condition .= " and o.add_time <= '$enddate' ";
        }
        if ($otype == 't2')
        {
            $condition .= " and o.otype='pt'";
            if ($status == 'g0')
            {
                $condition .= " and o.status= 0 ";
            }
            else if ($status == 'g1')
            {
                $condition .= " and o.status= 9 ";
            }
            else if ($status == 'g2')
            {
                $condition .= " and o.status > 0 and o.status < 9 ";
            }
            else if ($status == 'g3')
            {
                $condition .= " and o.status > 9 ";
            }
        }
        else if ($otype == 't3')
        {
            $condition .= " and o.otype = 'KJ' ";
            if ($status != '')
            {
                $condition .= " and o.status=$status";
            }
        }
        else if ($otype == 't4')
        {
            $condition .= " and o.otype = 'JP' ";
            if ($status != '')
            {
                $condition .= " and o.status=$status";
            }
        }
        else if ($otype == 't5')
        {
            $condition .= " and o.otype = 'FX' ";
            if ($status != '')
            {
                $condition .= " and o.status=$status";
            }
        }
        else if ($otype == 't7')
        {
            $condition .= " and o.otype = 'IN' ";
            if ($status != '')
            {
                $condition .= " and o.status=$status";
            }
        }
        else if ($otype == 't8')
        {
            $condition .= " and o.otype = 'MS' ";
            if ($status != '')
            {
                $condition .= " and o.status=$status";
            }
        }
        else if ($otype == 't9')
        {
            $condition .= " and o.otype = 'FS' ";
            if ($status != '')
            {
                $condition .= " and o.status=$status";
            }
        }
        else if ($otype == 'ZB')
        {
            $condition .= " and o.otype = 'ZB' ";
        }
        else
        {   
            if($selfLifting == 3)
            { // 直播
                $condition .= " and o.otype = 'VI' ";
            }
            else if($selfLifting == 2)
            { // 自提
                $condition .= " and o.otype = 'GM' and self_lifting = 1 ";
            }
            else if($selfLifting == 5)
            { // 配送
                $condition .= " and o.otype = 'GM' and self_lifting = 2 ";
            }
            else
            {
                $condition .= " and o.otype = 'GM' and self_lifting = 0 ";
            }
            if ($status != '')
            {
                $condition .= " and o.status=$status ";
            }
        }
        if (isset($readd) && $readd)
        { // 未查看信息
            $condition .= " and o.readd = '$readd' ";
        }
        if (isset($news_status) && $news_status === 0)
        { // 消息传过来的参数
            $condition .= " and o.status in (0,1)";
        }
        else if (isset($news_status) && $news_status == 1)
        {
            $condition .= " and o.status = 1";
            $status = 1;
        }
        if (isset($delivery_status) && $delivery_status)
        { // 提现发货
            $condition .= " and o.delivery_status = '$delivery_status' ";
        }
        if (isset($mch_id) && $mch_id)
        {
            $condition .= " and o.mch_id = ',$mch_id,' ";
        }
        if ($sNo != '')
        {
            $sNo_0 = Tools::FuzzyQueryConcatenation($sNo);
            $condition .= " and (o.sNo like $sNo_0 or o.name like $sNo_0 or o.mobile like $sNo_0 or o.user_id like $sNo_0 or o.p_sNo like $sNo_0) ";
        }

        $sql112 = "select ifnull(count(a.sNo),0) as num from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                right join lkt_mch as m on p.mch_id = m.id
                where o.store_id = '$store_id' and lu.store_id = '$store_id' $condition ) a ";
        $res112 = Db::query($sql112);
        $total = $res112[0]['num'];

        // 页码
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        // $sort_order = " tt.status asc,tt.createDate desc ";
        $sort_order = " tt.createDate desc,tt.status asc ";
        if($selfLifting == 3)
        { // 虚拟
            $sort_order = " tt.createDate desc,FIELD(status,'0','8','5','7') ";
        }
        else if($selfLifting == 2)
        { // 自提
            $sort_order = " tt.createDate desc,FIELD(status,'0','2','5','7') ";
        }
        else if($selfLifting == 5)
        { // 配送
            $sort_order = " tt.createDate desc,FIELD(status,'0','2','5','7') ";
        }
        $sql1 = "select tt.* from (select o.real_sno as mchOrderNo,o.id,o.num,o.sNo,o.name as userName,o.sheng,o.shi,o.xian,o.address,o.shop_cpc,o.code,o.add_time as createDate,o.mobile,o.old_total,o.z_price as orderPrice,o.status,o.allow,o.drawid,o.otype,o.ptstatus,o.pay,lu.user_name,o.user_id as userId,o.mch_id,o.currency_symbol,o.exchange_rate,m.id as shop_id,o.self_lifting,o.arrive_time,o.operation_type,o.p_sNo,d.after_discount,p.is_appointment,p.write_off_settings,d.store_self_delivery,o.voucher,o.review_status,o.reason_for_rejection,row_number () over (PARTITION BY o.sNo) AS top  
            from lkt_order as o 
            left join lkt_user as lu on o.user_id = lu.user_id 
            right join lkt_order_details as d on o.sNo = d.r_sNo
            RIGHT JOIN lkt_configure attr ON attr.id=d.sid
            RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
            right join lkt_mch as m on p.mch_id = m.id
            where o.store_id = '$store_id' and lu.store_id = '$store_id' $condition ) AS tt WHERE tt.top < 2 order by $sort_order limit $start,$pagesize";
        $res1 = Db::query($sql1);
        $list = array();
        $oids = array();
        $info = array();
        //支付方式
        $payments = PaymentModel::order('sort','desc')->select()->toArray();
        $payments_type = array();
        foreach ($payments as $keyp => $valuep)
        {
            $payments_type[$valuep['class_name']] = $valuep['name'];
        }
        $mch_id = '';
        if (!empty($res1))
        {
            foreach ($res1 as $k => $v)
            {
                $oids[$v['id']] = $v['id'];
                $shop_id = $v['shop_id'];
                $r_sNo = $v['sNo'];
                $store_self_delivery = $v['store_self_delivery'];
                if($v['exchange_rate'] == '')
                {
                    $exchange_rate = 1;
                }
                else
                {
                    $exchange_rate = $v['exchange_rate']; // 支付时货币汇率
                }
                if($v['self_lifting'] == 2)
                {
                    $sql_self_delivery_info = "select sd.delivery_time,sd.delivery_period from lkt_order_details as d left join lkt_self_delivery_info as sd on d.store_self_delivery = sd.id where store_id = '$store_id' and d.r_sNo = '$r_sNo' ";
                    $r_self_delivery_info = Db::query($sql_self_delivery_info);
                    if($r_self_delivery_info)
                    {
                        $res1[$k]['delivery_time'] = $r_self_delivery_info[0]['delivery_time'];
                        $res1[$k]['delivery_period'] = $r_self_delivery_info[0]['delivery_period'];
                    }
                }
                if($v['old_total'] == '0.00' || $v['old_total'] == '')
                {
                    $res1[$k]['old_total'] = $v['orderPrice'];
                }

                $res1[$k]['orderStatus'] = $v['status'];
                if($v['operation_type'] == 1)
                { // 用户下单
                    $res1[$k]['operationTypeName'] = '用户下单';
                }
                else if($v['operation_type'] == 2)
                { // 店铺下单
                    $res1[$k]['operationTypeName'] = '店铺下单';
                }
                else if($v['operation_type'] == 3)
                { // 平台下单
                    $res1[$k]['operationTypeName'] = '平台下单';
                }
                
                $res1[$k]['userName'] = $v['user_name'];
                $res1[$k]['logistics_type'] = false;
                $sql_express_delivery = "select * from lkt_express_delivery where store_id = '$store_id' and sNo = '$r_sNo' and subtable_id != 0";
                $r_express_delivery = Db::query($sql_express_delivery);
                if($r_express_delivery)
                {
                    $res1[$k]['logistics_type'] = true;
                }

                if ($sNo !== false && $exportType != 1)
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

                $freight = 0;
                $mch_id = $v['mch_id'];
                $mch_arr = explode(',', $mch_id);

                $array_address = array('cpc'=>$v['shop_cpc'],'sheng'=>$v['sheng'],'shi'=>$v['shi'],'xian'=>$v['xian'],'address'=>$v['address'],'code'=>$v['code']);
                $address_xq = PC_Tools::address_translation($array_address);
                $res1[$k]['addressInfo'] = $address_xq;
                $zqprice = 0;
                $order_id = $v['sNo'];
                $pay = $v['pay'];

                if (array_key_exists($pay, $payments_type))
                {
                    $res1[$k]['payName'] = $payments_type[$pay];
                }
                else if ($pay == 'offline_support')
                {
                    $res1[$k]['payName'] = '线下支付';
                }
                else
                {
                    $res1[$k]['payName'] = '钱包支付';
                }

                $user_id = $v['userId'];
                $sqldt = "select lpl.imgurl,lpl.product_title,lpl.product_number,lod.p_price,lod.unit,lod.num,lod.size,lod.p_id,lod.courier_num,lod.express_id,lod.freight,lpl.brand_id ,lm.name as mchname,lod.r_status,lod.id,attr.img,lod.write_off_num,lod.score_deduction,lod.p_integral
                        from lkt_order_details as lod 
                        left JOIN lkt_configure attr ON attr.id=lod.sid
                        left join lkt_product_list as lpl on lpl.id=attr.pid 
                        LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id 
                        where r_sNo='".$v['sNo']."' $prostr";
                $products = Db::query($sqldt);
                $res1[$k]['freight'] = $freight;

                if ($products)
                {
                    $courier_num = array();       // 快递单号
                    $res1[$k]['courier_num'] = '';
                    foreach ($products as $kd => $vd)
                    {
                        $freight += $vd['freight'];
                        $vd['imgurl'] = ServerPath::getimgpath($vd['imgurl'], $store_id);
                        if($vd['img'])
                        {
                            $vd['imgurl'] = ServerPath::getimgpath($vd['img'], $store_id);
                        }
                        if ($vd['express_id'])
                        {
                            $exper_id = $vd['express_id'];
                            $r03 = ExpressModel::where('id',$exper_id)->select()->toArray();
                            $courier_num[] = $vd['courier_num'] . "  (" . $r03[0]['kuaidi_name'] . ")"; // 快递单号
                            if($res1[$k]['courier_num'] == '')
                            {
                                $res1[$k]['courier_num'] = $vd['courier_num'];
                            }
                            else
                            {
                                $res1[$k]['courier_num'] .= ',' . $vd['courier_num'];
                            }
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
                        if (($res_o + $res_d) == $res_s && $exportType != 1)
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
                    $res1[$k]['detailFreight'] = $products[0]['freight'];
                    $res1[$k]['detailId'] = $products[0]['id'];
                    $res1[$k]['goodsImgUrl'] = ServerPath::getimgpath($products[0]['imgurl'], $store_id);
                    if($products[0]['img'])
                    {
                        $res1[$k]['goodsImgUrl'] = ServerPath::getimgpath($products[0]['img'], $store_id);
                    }
                    $res1[$k]['goodsName'] = $products[0]['product_title'];
                    $res1[$k]['goodsPrice'] = $products[0]['p_price'];
                    $res1[$k]['goodsNum'] = count($products);
                    $res1[$k]['expressList'] = $courier_num_arr;
                    $res1[$k]['expressStr'] = $expressStr;
                    
                    $res1[$k]['mchName'] = $products[0]['mchname'];
                    $res1[$k]['shopName'] = $products[0]['mchname'];
                    $res1[$k]['needNum'] = $products[0]['num'];
                    $res1[$k]['orderno'] = $v['sNo'];
                    $res1[$k]['attrStr'] = $products[0]['size'];
                    $res1[$k]['status'] = $v['status'];
                    $res1[$k]['p_integral'] = $products[0]['p_integral'];
                
                    $res1[$k]['showHX'] = 0; // 核销按钮 0.不显示 1.显示
                    if($self_operated_store_id == $shop_id)
                    {
                        if($v['status'] == 8 && $products[0]['write_off_num'] != 0)
                        {
                            $res1[$k]['showHX'] = 1;
                        }
                    }

                    $pt_status = '';
                    if ($v['otype'] == 'pt')
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res1[$k]['status'] = '待付款';
                                $res1[$k]['pt_status'] = '待付款';
                                break;
                            case 1 :
                                $res1[$k]['pt_status'] = '拼团成功';
                                $res1[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                $res1[$k]['pt_status'] = '拼团成功';
                                $res1[$k]['status'] = '待收货';
                                break;
                            case 5 :
                                $res1[$k]['status'] = '已完成';
                                $res1[$k]['pt_status'] = '拼团成功';
                                break;
                            case 7 :
                                $res1[$k]['status'] = '已关闭';
                                $res1[$k]['pt_status'] = '拼团成功';
                                break;
                        }
                    }
                    else
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res1[$k]['status'] = '待付款';
                                break;
                            case 1 :
                                $res1[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                $res1[$k]['status'] = '待收货';
                                break;
                            case 5 :
                                $res1[$k]['status'] = '已完成';
                                break;
                            case 7 :
                                $res1[$k]['status'] = '已关闭';
                                break;
                            case 8 :
                                $res1[$k]['status'] = '待核销';
                                break;
                        }
                    }
                    if ($v['otype'] == 'IN')
                    {
                        $res1[$k]['orderAllow'] = $res1[$k]['allow'];
                        $integralid = $vd['p_id'];
                        $sql = "select g.integral,g.money,c.img from lkt_integral_goods as g left join lkt_configure as c on g.attr_id = c.id where g.id='$integralid'";
                        $inr = Db::query($sql);
                        if ($inr)
                        {
                            if($products[0]['p_integral'] == 0)
                            {
                                $res1[$k]['p_integral'] = $inr[0]['integral'];
                                $res1[$k]['allow'] = $inr[0]['integral'];
                            }
                            $res1[$k]['after_discount'] = $inr[0]['money'];
                            $res1[$k]['p_money'] = $inr[0]['money'];
                            $vd['imgurl'] = ServerPath::getimgpath($inr[0]['img']);
                        }
                    }
                    switch ($v['self_lifting']) {
                        case '1':
                            $res1[$k]['selfLiftingName'] = '自提';
                            break;
                        case '2':
                            $res1[$k]['selfLiftingName'] = '同城配送';
                            break;
                        
                        default:
                            $res1[$k]['selfLiftingName'] = '快递';
                            break;
                    }
                    //订单类型
                    switch ($v['otype']) 
                    {
                        case 'FX':
                            $res1[$k]['otype'] = '分销';
                            break;

                        case 'pt':
                            $res1[$k]['otype'] = '拼团';
                            break;

                        case 'KJ':
                            $res1[$k]['otype'] = '砍价';
                            break;

                        case 'JP':
                            $res1[$k]['otype'] = '竞拍';
                            break;

                        case 'MS':
                            $res1[$k]['otype'] = '秒杀';
                            break;

                        case 'FS':
                            $res1[$k]['otype'] = '折扣';
                            break;

                        case 'IN':
                            $res1[$k]['otype'] = '积分';
                            break;

                        case 'PP':
                            $res1[$k]['otype'] = '拼团';
                            break;

                        case 'PM':
                            $res1[$k]['otype'] = '秒杀';
                            break;
                        case 'VI':
                            $res1[$k]['otype'] = '虚拟';
                            break;
                        default:
                            $res1[$k]['otype'] = '普通';
                            break;
                    }

                    $res1[$k]['freight'] = $freight;
                    $res1[$k]['old_total'] = round(($res1[$k]['old_total'] * $exchange_rate),2);
                    $res1[$k]['freight'] = round(($res1[$k]['freight'] * $exchange_rate),2);
                    $res1[$k]['goodsPrice'] = round(($res1[$k]['goodsPrice'] * $exchange_rate),2);
                    $list[] = $res1[$k];
                }
            }
        }
        else
        {
            $list = array();
        }
        if($oids)
        {
            $oids = implode(',',$oids);
            $sql11 = "select tt.* from (select o.real_sno as mchOrderNo,o.id,d.num,o.sNo,o.name as userName,o.sheng,o.shi,o.xian,o.address,o.shop_cpc,o.code,o.add_time as createDate,o.mobile,o.z_price as orderPrice,o.status,o.allow,o.drawid,o.otype,o.ptstatus,o.pay,lu.user_name,o.user_id as userId,o.mch_id,m.id as shop_id,o.self_lifting,o.arrive_time,o.operation_type,o.p_sNo,d.after_discount,row_number () over (PARTITION BY o.sNo) AS top,p.imgurl,p.product_title,p.product_number,d.p_price,d.unit,d.size,d.p_id,d.courier_num,d.express_id,d.freight,p.brand_id ,m.name as mchname,d.r_status,d.id as did,attr.img
            from lkt_order_details as d 
            left join lkt_user as lu on d.user_id = lu.user_id 
            right join lkt_order as o on o.sNo = d.r_sNo
            RIGHT JOIN lkt_configure attr ON attr.id=d.sid
            RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
            right join lkt_mch as m on p.mch_id = m.id
            where o.store_id = '$store_id' and lu.store_id = '$store_id' and o.id in ($oids) $condition ) AS tt WHERE 1 order by tt.createDate desc ";
            $res11 = Db::query($sql11);
            if (!empty($res11))
            {
                foreach ($res11 as $k => $v)
                {
                    $shop_id = $v['shop_id'];
                    if ($sNo !== false && $exportType != 1)
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

                    $freight = 0;
                    $mch_id = $v['mch_id'];
                    $mch_arr = explode(',', $mch_id);
    
                    $array_address = array('cpc'=>$v['shop_cpc'],'sheng'=>$v['sheng'],'shi'=>$v['shi'],'xian'=>$v['xian'],'address'=>$v['address'],'code'=>$v['code']);
                    $address_xq = PC_Tools::address_translation($array_address);
                    $res11[$k]['addressInfo'] = $address_xq;
                    $zqprice = 0;
                    $order_id = $v['sNo'];
                    $pay = $v['pay'];

                    if (array_key_exists($pay, $payments_type))
                    {
                        $res11[$k]['payName'] = $payments_type[$pay];
                    }
                    else
                    {
                        $res11[$k]['payName'] = '钱包支付';
                    }

                    $user_id = $v['userId'];
                    $courier_num = array();       // 快递单号
                        
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
                    $res11[$k]['detailFreight'] = $v['freight'];
                    $res11[$k]['detailId'] = $v['did'];
                    $res11[$k]['goodsImgUrl'] = ServerPath::getimgpath($v['img'], $store_id);
                    $res11[$k]['goodsName'] = $v['product_title'];
                    $res11[$k]['goodsPrice'] = $v['p_price'];
                    $res11[$k]['goodsNum'] = $v['num'];
                    $res11[$k]['expressList'] = $courier_num_arr;
                    $res11[$k]['expressStr'] = $expressStr;
                    
                    $res11[$k]['mchName'] = $v['mchname'];
                    $res11[$k]['shopName'] = $v['mchname'];
                    $res11[$k]['needNum'] = $v['num'];
                    $res11[$k]['orderno'] = $v['sNo'];
                    $res11[$k]['attrStr'] = $v['size'];
                

                    $pt_status = '';
                    if ($v['otype'] == 'pt')
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res11[$k]['status'] = '待付款';
                                $res11[$k]['pt_status'] = '待付款';
                                break;
                            case 1 :
                                $res11[$k]['pt_status'] = '拼团成功';
                                $res11[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                $res11[$k]['pt_status'] = '拼团成功';
                                $res11[$k]['status'] = '待收货';
                                break;
                            case 5 :
                                $res11[$k]['status'] = '已完成';
                                $res11[$k]['pt_status'] = '拼团成功';
                                break;
                            case 7 :
                                $res11[$k]['status'] = '已关闭';
                                $res11[$k]['pt_status'] = '拼团成功';
                                break;
                        }
                    }
                    else
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res11[$k]['status'] = '待付款';
                                break;
                            case 1 :
                                $res11[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                $res11[$k]['status'] = '待收货';
                                break;
                            case 5 :
                                $res11[$k]['status'] = '已完成';
                                break;
                            case 7 :
                                $res11[$k]['status'] = '已关闭';
                                break;
                            case 8 :
                                $res11[$k]['status'] = '待核销';
                                break;
                        }
                    }

                    if ($v['otype'] == 'IN')
                    {
                        $res1[$k]['orderAllow'] = $res1[$k]['allow'];
                        $integralid = $v['p_id'];
                        $sql = "select g.integral,g.money,c.img from lkt_integral_goods as g left join lkt_configure as c on g.attr_id = c.id where g.id='$integralid'";
                        $inr = Db::query($sql);
                        if ($inr)
                        {
                            $v['p_integral'] = $inr[0]['integral'];
                            $res11[$k]['allow'] = $inr[0]['integral'];
                            $res11[$k]['after_discount'] = $inr[0]['money'];
                            $v['p_money'] = $inr[0]['money'];
                            $vd['imgurl'] = ServerPath::getimgpath($inr[0]['img']);
                        }
                    }
                    switch ($v['self_lifting']) {
                        case '1':
                            $res11[$k]['selfLiftingName'] = '自提';
                            break;
                        
                        case '2':
                            $res11[$k]['selfLiftingName'] = '同城配送';
                            break;
                            
                        default:
                            $res11[$k]['selfLiftingName'] = '快递';
                            break;
                    }
                    //订单类型
                    switch ($v['otype']) 
                    {
                        case 'FX':
                            $res11[$k]['otype'] = '分销';
                            break;

                        case 'pt':
                            $res11[$k]['otype'] = '拼团';
                            break;

                        case 'KJ':
                            $res11[$k]['otype'] = '砍价';
                            break;

                        case 'JP':
                            $res11[$k]['otype'] = '竞拍';
                            break;

                        case 'MS':
                            $res11[$k]['otype'] = '秒杀';
                            break;

                        case 'FS':
                            $res11[$k]['otype'] = '折扣';
                            break;

                        case 'IN':
                            $res11[$k]['otype'] = '积分';
                            break;

                        case 'PP':
                            $res11[$k]['otype'] = '拼团';
                            break;

                        case 'PM':
                            $res11[$k]['otype'] = '秒杀';
                            break;
                        
                        default:
                            $res11[$k]['otype'] = '普通';
                            break;
                    }

                    $res11[$k]['freight'] = $v['freight'];
                    $info[] = $res11[$k];

                }
            }
        }
        
        //全部待发货订单数
        $sql002 = "select ifnull(count(a.sNo),0) as c from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                right join lkt_product_list as p on p.id = d.p_id
                right join lkt_mch as m on p.mch_id = m.id
                where o.store_id = '$store_id' and lu.store_id = '$store_id' and o.recycle != 1 and o.status=1 group by o.sNo) a";
        $re002 = Db::query($sql002);
        if($re002)
        {
            $zd_total = $re002[0]['c'];
        }
        else
        {
            $zd_total = 0;
        }

        //实物订单待发货数量
        $sql003 = "select ifnull(count(a.sNo),0) as c from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                right join lkt_product_list as p on p.id = d.p_id
                right join lkt_mch as m on p.mch_id = m.id
                where o.store_id = '$store_id' and lu.store_id = '$store_id' and o.recycle != 1 and o.status=1 and o.otype = 'GM' group by o.sNo) a";
        $re003 = Db::query($sql003);
        if($re003)
        {
            $zs_total = $re003[0]['c'];
        }
        else
        {
            $zs_total = 0;
        }

        //活动订单待发货数量
        $sql004 = "select ifnull(count(a.sNo),0) as c from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                right join lkt_product_list as p on p.id = d.p_id
                right join lkt_mch as m on p.mch_id = m.id
                where o.store_id = '$store_id' and lu.store_id = '$store_id' and o.recycle != 1 and o.status=1 and o.otype != 'VI' and  o.otype != 'GM' group by o.sNo) a";
        $re004 = Db::query($sql004);
        if($re004)
        {
            $zh_total = $re004[0]['c'];
        }
        else
        {
            $zh_total = 0;
        }

        $class = PC_Tools::data_dictionary('订单状态', $status);
        $info = array('brand_str'=>$brand_str,'ordtype'=>array(),'list'=>$list,'info'=>$info,'total'=>$total,'class'=>$class,'zd_total'=>$zd_total,'zs_total'=>$zs_total,'zh_total'=>$zh_total,'mch_id'=>$mch_id);
        return $info;
    }
    
    // 订单详情(后台、PC店铺)
    public static function order_details($action)
    {
        $lktlog = new LaiKeLogUtils();
        $store_id = $action->store_id;
        $id = $action->id;
        // $type = $action->type;
        $order_type = $action->order_type;

        $update_s = true;

        $r0 = OrderConfigModel::where('store_id',$store_id)->field('remind')->select()->toArray();
        if ($r0)
        {
            $remind = $r0[0]['remind'];
            if ($remind == 0)
            {
                $remind_time = date("Y-m-d H:i:s", strtotime("+7 day"));
            }
            else
            {
                $remind_day = floor($remind / 24);
                $remind_hour = $remind % 24;
                $remind_time = date("Y-m-d H:i:s", strtotime("+$remind_day day +$remind_hour hour"));
            }
        }
        else
        {
            $remind_time = date("Y-m-d H:i:s", strtotime("+7 day"));
        }

        /*-----------进入订单详情把未读状态改成已读状态，已读状态的状态不变-------*/
        $r01 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$id])
                        ->field('delivery_status,readd,z_price,mch_id,status,old_total,old_freight,z_freight,currency_symbol,exchange_rate,currency_code,cpc')
                        ->select()
                        ->toArray();
        $old_total = 0; // 订单总价
        $old_freight = 0; // 订单总价
        $pay_price = 0; // 订单总价
        $exchange_rate = 1; // 汇率
        $currency_symbol = ''; // 货币符号
        $currency_code = ''; // 支付时货币编码
        if ($r01)
        {
            $pay_price = $r01[0]['z_price'];
            $old_total = $r01[0]['old_total'];
            $old_freight = $r01[0]['old_freight'];
            $exchange_rate = $r01[0]['exchange_rate'];
            $currency_symbol = $r01[0]['currency_symbol'];
            $currency_code = $r01[0]['currency_code'];
            if($r01[0]['old_total'] == '0.00' || $r01[0]['old_total'] == '')
            {
                $old_total = $r01[0]['z_price'];
            }
            if($r01[0]['old_freight'] == '0.00' || $r01[0]['old_freight'] == 0)
            {
                $old_freight = $r01[0]['z_freight'];// 订单总价
            }

            $shop_id = trim($r01[0]['mch_id'],',');
            if ($r01[0]['readd'] == 0)
            {   

                $sql02 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$id])->find();
                $sql02->readd = 1;
                if ($r01[0]['delivery_status'] == 1)
                {   
                    $sql02->remind = $remind_time;
                }
                $up = $sql02->save();
                if (!$up)
                {
                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改已读状态失败！sNo:" . $id);
                }

                $message_logging_list_1 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$id,'type'=>1);
                PC_Tools::message_pop_up($message_logging_list_1);
                PC_Tools::message_read($message_logging_list_1);
                $message_logging_list_3 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$id,'type'=>3);
                PC_Tools::message_pop_up($message_logging_list_3);
                PC_Tools::message_read($message_logging_list_3);
                $message_logging_list_5 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$id,'type'=>5);
                PC_Tools::message_pop_up($message_logging_list_5);
                PC_Tools::message_read($message_logging_list_5);
            }
            if($r01[0]['status'] == 5)
            {
                $message_logging_list_6 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$id,'type'=>6);
                PC_Tools::message_pop_up($message_logging_list_6);
                PC_Tools::message_read($message_logging_list_6);
            }
            if($r01[0]['status'] == 7)
            {
                $message_logging_list_4 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$id,'type'=>4);
                PC_Tools::message_pop_up($message_logging_list_4);
                PC_Tools::message_read($message_logging_list_4);
            }
        }
        /*--------------------------------------------------------------------------*/

        //支付方式
        $payments = PaymentModel::order('sort','desc')->select()->toArray();
        $payments_type = array();
        foreach ($payments as $keyp => $valuep) 
        {
            $payments_type[$valuep['class_name']] = $valuep['name'];
        }

        $sql = "select l.p_sNo,d.id,l.source,l.remarks,l.pay,l.pay_time,l.id as oid,l.spz_price,l.comm_discount,l.z_freight,l.old_freight,u.user_name,l.sNo,l.name,l.mobile,l.cpc,l.sheng,l.shi,l.old_total,l.z_price,l.xian,l.status,l.address,l.shop_cpc,l.code,l.pay,l.trade_no,l.coupon_id,l.reduce_price,l.coupon_price,l.allow,l.drawid,l.otype,l.grade_rate,l.preferential_amount,l.mch_id,d.user_id,d.p_id,d.p_name,d.p_price,d.num,d.unit,d.add_time,d.deliver_time,d.arrive_time,d.r_status,d.content,d.express_id,d.courier_num,d.sid,d.size,d.freight,e.kuaidi_name,c.total_num,c.price,c.yprice as supplier_settlement ,l.subtraction_id,d.after_discount,d.r_type,d.re_type,d.r_sNo,l.supplier_id,l.self_lifting,l.single_store,d.after_write_off_num,d.platform_coupon_price,d.store_coupon_price,d.write_off_num,d.anchor_id,d.commission,d.supplier_settlement,d.score_deduction,d.p_integral
                from lkt_order_details as d 
                left join lkt_order as l on l.sNo=d.r_sNo 
                left join lkt_user as u on u.user_id=l.user_id and u.store_id='$store_id' 
                left join lkt_express as e on d.express_id=e.id 
                left join lkt_configure as c on c.id = d.sid
                where l.store_id = '$store_id' and l.sNo='$id'";
        $res = Db::query($sql);
        // var_dump($res[0]['cpc']);die;
        $num = count($res);
        $data = array();
        $reduce_price = 0; // 满减金额
        $coupon_price = 0; // 优惠券金额
        $preferential_amount = 0; // 平台优惠券金额
        $comm_amount = 0; // 分销优惠金额
        $allow = 0; // 积分
        $yunfei = 0;
        $courier_num_arr = array();
        $yh_money = 0; // 优化金额

        $discount_type = '';
        $coupon_id = $res[0]['coupon_id'];// 优惠券ID
        $cpc = $res[0]['cpc'];//电话区号
        $subtraction_id = $res[0]['subtraction_id'];// 满减活动ID
        $grade_rate = $res[0]['grade_rate'];//会员等级折扣
        $spz_price = $res[0]['spz_price'];// 商品总价
        $comm_discount = $res[0]['comm_discount'];// 商品总价
        $self_lifting = $res[0]['self_lifting'];// 自提 0.配送 1.自提
        $single_store = $res[0]['single_store']; // 下单门店
        $z_freight = $old_freight;// 总运费
        $totalSupplierFright = $z_freight;
        $storeSelfInfo = array();
        $show_write_time = 0; // 是否显示预约信息 0.不显示 1.显示
        $write_time_info = array();
        if($self_lifting == 2)
        {
            $sql_self_delivery_info = "select sd.* from lkt_order_details as d left join lkt_self_delivery_info as sd on d.store_self_delivery = sd.id where store_id = '$store_id' and d.r_sNo = '$id' ";
            $r_self_delivery_info = Db::query($sql_self_delivery_info);
            if($r_self_delivery_info)
            {
                $storeSelfInfo = $r_self_delivery_info[0];
            }
        }
        else if ($self_lifting == '3')
        {
            if($single_store != 0)
            {
                $show_write_time = 1;
                $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'id'=>$single_store])->select()->toArray();
                if($r0_0)
                {
                    $sql_order_details = "select write_time from lkt_order_details where store_id = '$store_id' and r_sNo = '$id' ";
                    $r_order_details = Db::query($sql_order_details);
                    $write_time = $r_order_details[0]['write_time'];

                    $array_address = array('cpc'=>$r0_0[0]['cpc'],'sheng'=>$r0_0[0]['sheng'],'shi'=>$r0_0[0]['shi'],'xian'=>$r0_0[0]['xian'],'address'=>$r0_0[0]['address'],'code'=>$r0_0[0]['code']);
                    $address_xq = PC_Tools::address_translation($array_address);
                    $write_time_info = array('mch_store'=>$r0_0[0]['name'],'time'=>$write_time,'address'=>$address_xq);
                }
            }
        }
        $data['show_write_time'] = $show_write_time;
        $data['write_time_info'] = $write_time_info;
        
        $otype = $res[0]['otype'];// 订单类型
        $mch_id = $res[0]['mch_id'];//店铺ID
        $supplier_id = $res[0]['supplier_id'];//店铺ID
        $mch = substr($mch_id, 1, -1);
        $isManyMch = false;
        if(count(explode(',', $mch)) > 1)
        {
            $data['update_flag'] = 0;
            $isManyMch = true;
        }
        else
        {
            $data['update_flag'] = 1;
        }
        $zifuchuan = trim(strrchr($coupon_id, ','),',');
        if ($zifuchuan != 0 )
        {
            $discount_type = '优惠券';
        }
        $grade_rate_amount = 0; // 会员优惠金额
        $express_ids = array();
        $data['fh'] = 0;
        $returnStatus = '';
        $expressStr = '';
        $data['express_name'] = '';
        $returnStatus0 = '';

        foreach ($res as $k => $v)
        {   
            $r_type = $v['r_type'];
            $re_type = $v['re_type'];
            $res[$k]['p_price'] = round($v['p_price'],2);
            $res[$k]['after_discount'] = round($v['after_discount'],2);
            $res[$k]['coupon_price'] = round($v['coupon_price'],2);
            $res[$k]['reduce_price'] = round($v['reduce_price'],2);
            $res[$k]['preferential_amount'] = round($v['preferential_amount'],2);
            $res[$k]['freight'] = round($v['freight'],2);
            $res[$k]['spz_price'] = round($v['spz_price'],2);
            $res[$k]['z_freight'] = round($v['z_freight'],2);
            $res[$k]['z_price'] = number_format($v['z_price'],2);
            $res[$k]['supplier_settlement'] = round($v['supplier_settlement'],2);
            $res[$k]['p_price'] = round(($res[$k]['p_price'] * $exchange_rate),2);
            $p_price = $v['p_price']; // 商品售价
            $p_num = $v['num']; // 商品数量
            $after_discount = $v['after_discount']; // 优惠后的金额

            $rtype = Db::name('return_order')->where('p_id', $v['id'])->value('r_type'); //查询是否有售后状态
            if($rtype)
            {
                $r_type = $rtype;
            }

            $r_id = Db::name('return_order')->where('p_id', $v['id'])->value('id'); // 售后ID
            $returnInfo = array();
            
            $returnStatus = Tools::ObtainAfterSalesStatus($re_type,$r_type);
            if($returnStatus != '')
            {
                $returnStatus0 = $returnStatus;
            }
            
            $returnInfo = array('id'=>$r_id,'statusName'=>$returnStatus,'name'=>$returnStatus);
            if($otype != 'IN')
            {
                $grade_rate_amount += round(($p_price - $p_price * $grade_rate * 0.1) * $p_num,2);
            }
            $sid = $v['sid'];
            $data['user_name'] = $v['user_name']; // 联系人
            $data['name'] = $v['name']; // 联系人
            $data['remarks'] = ''; //备注
            $remarks = '';
            if($v['remarks'] != '')
            {
                if(Tools::is_serialized($v['remarks']))
                {
                    $remarks0 = unserialize($v['remarks']); // 订单备注
                    foreach ($remarks0 as $k1 => $v1)
                    {
                        $data['remarks'] = $v1;
                        $remarks = $v1; 
                    }
                }
                else
                {
                    $data['remarks'] = $v['remarks'];
                    $remarks = $v['remarks'];
                }
            }

            // $returnInfo = Tools::getReturnOrderStatus($store_id,$v['id']);
            $res[$k]['returnInfo'] = $returnInfo;

            $data['pay'] = $v['pay'];//支付时间
            $data['pay_time'] = $v['pay_time'];//支付时间
            $data['source'] = $v['source']; //来源
            $data['sNo'] = $v['sNo']; // 订单号
            $data['oid'] = $v['oid']; // oid
            $data['mobile'] = $v['mobile']; // 联系电话
            if ($v['grade_rate'] == "10.00" || $v['grade_rate'] == "0.00")
            {
                $res[$k]['grade_rate2'] = 1;
                $res[$k]['grade_rate'] = 0;
                $v['grade_rate2'] = 1;
            }
            else
            {
                $v['grade_rate2'] = $v['grade_rate'];
                $res[$k]['grade_rate'] = $v['grade_rate'] . "折";
            }
            $res[$k]['subtotal'] = round($p_price * $p_num,2);
            $res[$k]['subtotal'] = round(($res[$k]['subtotal'] * $exchange_rate),2);
            $yh_money = $yh_money + ($v['num'] * $v['p_price']) - ($v['num'] * $v['p_price'] * $v['grade_rate2']);

            $data['grade_rate'] = 0;
            $data['grade_rate2'] = $v['grade_rate2'];

            $array_address = array('cpc'=>$v['shop_cpc'],'sheng'=>$v['sheng'],'shi'=>$v['shi'],'xian'=>$v['xian'],'address'=>$v['address'],'code'=>$v['code']);
            $address_xq = PC_Tools::address_translation($array_address);
            
            $data['address'] = $address_xq; // 详细地址
            $data['sheng'] = $v['sheng'];
            $data['shi'] = $v['shi'];
            $data['xian'] = $v['xian'];
            $data['r_address'] = $v['address'];
            $data['add_time'] = $v['add_time']; // 添加时间
            $data['z_price'] = (float)$v['z_price']; // 添加时间
            $data['user_id'] = $v['user_id']; // 用户id
            $data['deliver_time'] = '';
            $data['arrive_time'] = '';
            if(!empty($v['deliver_time']))
            {
                $data['deliver_time'] = $v['deliver_time']; // 发货时间
            }
            else
            {
                $data['deliver_time'] = $v['arrive_time']; // 到货时间
            }
            if(!empty($v['arrive_time']))
            {
                $data['arrive_time'] = $v['arrive_time']; // 到货时间
            }
            $data['r_status'] = $v['r_status']; // 订单详情状态
            $data['status01'] = $v['r_status']; // 订单详情状态
            $data['gstatus'] = $v['status']; // 订单详情状态
            $data['otype'] = $v['otype'];  // 订单类型
            switch ($data['otype']) 
            {
                case 'MS':
                    $data['orderTypeName'] = '秒杀';
                    break;
                case 'FX':
                    $data['orderTypeName'] = '分销';
                    break;
                case 'IN':
                    $data['orderTypeName'] = '积分';
                    break;
                case 'JP':
                    $data['orderTypeName'] = '竞拍';
                    break;
                case 'PT':
                    $data['orderTypeName'] = '拼团';
                    break;
                case 'FS':
                    $data['orderTypeName'] = '限时折扣';
                    break;
                case 'VI':
                    $data['orderTypeName'] = '虚拟';
                    break;
                case 'ZB':
                    $data['orderTypeName'] = '直播';
                    break;
                default:
                    $data['orderTypeName'] = '普通';
                    break;
            }

            if($v['supplier_id'] != 0)
            {
                $data['orderTypeName'] = '供应商';
            }

            $data['content'] = $v['content']; // 退货原因
            $data['express_id'] = $v['express_id']; // 快递公司id
            if( $v['express_id'])
            {
                $express_id_list = explode(',',$v['express_id']); // 快递公司id
                $courier_num_list = explode(',',$v['courier_num']); // 快递公司id
                foreach ($courier_num_list as $k_e => $v_e)
                {
                    if(!in_array($v_e,$express_ids))
                    {
                        $express_ids[] =  $v_e;
                        // 根据快递公司id,查询快递公司表信息
                        $r03 = ExpressModel::where('id',$express_id_list[$k_e])->select()->toArray();
                        if (isset($r03[0]['kuaidi_name']))
                        {
                            $data['express_name'] .= $r03[0]['kuaidi_name'].","; // 快递公司名称
                            $expressStr .= $r03[0]['kuaidi_name']."(".$v_e."),";
                        }
                    }
                }
            }

            if (!in_array($v['courier_num'], $courier_num_arr))
            {
                if (!empty($v['courier_num']))
                {
                    $data['courier_num'][$k]['num'] = $v['courier_num']; // 快递单号
                    $data['courier_num'][$k]['kuaidi_name'] = $v['kuaidi_name'];
                    $courier_num_arr[] = $v['courier_num'];
                }
            }
            $data['courierList'] = $courier_num_arr;

            if (isset($data['courier_num'][$k]['kuaidi_name']))
            {
                $data['fh'] = 1;
            }
            $data['drawid'] = $v['drawid']; // 抽奖ID
            $reduce_price = (float)$v['reduce_price']; // 满减金额
            $coupon_price = (float)$v['coupon_price']; // 优惠券金额
            $preferential_amount = (float)$v['preferential_amount']; // 平台优惠券金额
            $coupon_price = $coupon_price - $preferential_amount;

            $allow = $v['allow']; // 积分

            if (array_key_exists($v['pay'], $payments_type))
            {
                $paytype = $payments_type[$v['pay']];
            }
            else if ($v['pay'] == 'offline_payment')
            {
                $paytype = '线下支付';
            }
            else
            {
                $paytype = '钱包支付';
            }

            $data['paytype'] = $paytype; // 支付方式
            $data['trade_no'] = $v['trade_no']; // 微信支付交易号
            $yunfei = $yunfei + $v['freight'];
            $data['id'] = $id;

            // 根据产品id,查询产品主图
            $img = ConfigureModel::where(['id'=>$v['sid']])->field('pid,img,num')->select()->toArray();
            if (!empty($img))
            {
                $pid = $img[0]['pid'];
                $res[$k]['img'] = $img[0]['img'];
                $res[$k]['stockNum'] = $img[0]['num'];
                $res[$k]['pic'] = ServerPath::getimgpath($img[0]['img'], $store_id);

                $sql_p = "SELECT c.supplier_name,c.contact_phone,c.province,c.city,c.area,c.address FROM lkt_product_list as p LEFT JOIN lkt_supplier as c on p.gongyingshang=c.id where p.store_id = '$store_id' and p.id = '$pid' ";
                $r_p = Db::query($sql_p);
                if($r_p)
                {
                    $data['supplierName'] = $r_p[0]['supplier_name'];
                    $data['supplierPhone'] = $r_p[0]['contact_phone'];
                    $data['supplierAddress'] = $r_p[0]['province'].$r_p[0]['city'].$r_p[0]['area'].$r_p[0]['address'];
                }
            }

            if ($v['otype'] == 'JP')
            {
                if($v['status'] == 0)
                {
                    $res[$k]['status'] = '待付款';
                    $res[$k]['statusName'] = '待付款';
                }
                else if($v['status'] == 1)
                {
                    $res[$k]['status'] = '待发货';
                    $res[$k]['statusName'] = '待发货';
                }
                else if($v['status'] == 2)
                {
                    $res[$k]['status'] = '待收货';
                    $res[$k]['statusName'] = '待收货';
                }
                else if($v['status'] == 5)
                {
                    $res[$k]['status'] = '已完成';
                    $res[$k]['statusName'] = '已完成';
                }
                else if($v['status'] == 7)
                {
                    $res[$k]['status'] = '已关闭';
                    $res[$k]['statusName'] = '已关闭';
                }
            }
            else
            {
                if($v['status'] == 0)
                {
                    $res[$k]['statusName'] = '待付款';
                }
                else if($v['status'] == 1)
                {
                    $res[$k]['statusName'] = '待发货';
                }
                else if($v['status'] == 2)
                {
                    $res[$k]['statusName'] = '待收货';
                }
                else if($v['status'] == 5)
                {
                    $res[$k]['statusName'] = '已完成';
                }
                else if($v['status'] == 7)
                {
                    $res[$k]['statusName'] = '已关闭';
                }
                else if($v['status'] == 8)
                {
                    $res[$k]['statusName'] = '待核销';
                }
            }

            if($v['otype'] == 'JP')
            {
                $mch_id_JP = trim($v['mch_id'],',');//店铺ID

                $mch = MchModel::where(['id'=>$mch_id_JP])->field('name')->select()->toArray();
                $res[$k]['mchName'] = $mch[0]['name'];
            }
            else if($v['otype'] == 'PT')
            {
                $sNo = $v['sNo'];
                $drawid = $v['drawid'];
                $res[$k]['goodsPrice'] = $v['price'];

                $sql_g = "select a.price,b.team_num from lkt_group_open_record as a left join lkt_group_open as b on a.open_id = b.id where b.activity_id = '$drawid' and a.sno = '$sNo' ";
                $r_g = Db::query($sql_g);
                if($r_g)
                {
                    $res[$k]['teamPrice'] = $r_g[0]['price'];
                    $res[$k]['teamNum'] = $r_g[0]['team_num'];
                }
            }
            $data['lottery_status'] = 7;
        }
        $yh_money = $yh_money + $coupon_price + $reduce_price;
        $zp_res = array();

        $data['freight'] = $yunfei; // 运费
        $data['yunfei'] = $yunfei; // 运费
        // if (isset($express_ids))
        // {
        //     $exper_id = array_unique($express_ids);
        //     foreach ($exper_id as $key => $value) {
        //         // 根据快递公司id,查询快递公司表信息
        //         $r03 = ExpressModel::where('id',$value)->select()->toArray();
        //         if (isset($r03[0]['kuaidi_name']))
        //         {
        //             $data['express_name'] .= $r03[0]['kuaidi_name'].","; // 快递公司名称
        //             $expressStr .= $r03[0]['kuaidi_name']."(".$v['courier_num']."),";

        //         }
        //     }
        //     $expressStr = rtrim($expressStr,',');
        // }
        $expressStr = rtrim($expressStr,',');

        if ($data['otype'] == 'pt')
        {
            switch ($data['gstatus'])
            {
                case 0 :
                    $data['r_status'] = '待付款';
                    $data['pt_status'] = '待付款';
                    $data['bgcolor'] = '#f5b1aa';
                    break;
                case 1 :
                    $data['pt_status'] = '拼团成功';
                    $data['r_status'] = '待发货';
                    $data['bgcolor'] = '#f0908d';
                    break;
                case 2 :
                    $data['pt_status'] = '拼团成功';
                    $data['r_status'] = '待收货';
                    $data['bgcolor'] = '#f0908d';
                    break;
                case 3 :
                    $data['r_status'] = '待评价';
                    $data['bgcolor'] = '#f0908d';
                    $data['pt_status'] = '拼团成功';
                    break;
                case 4 :
                    $data['r_status'] = '退货';
                    $data['bgcolor'] = '#e198b4';
                    $data['pt_status'] = '拼团成功';
                    break;
                case 5 :
                    $data['r_status'] = '订单完成';
                    $data['bgcolor'] = '#f7b977';
                    $data['pt_status'] = '拼团成功';
                    break;
                case 6 :
                    $data['r_status'] = '订单关闭';
                    $data['bgcolor'] = '#f7b977';
                    $data['pt_status'] = '拼团成功';
                    break;
                case 9 :
                    $data['r_status'] = '待成团';
                    $data['bgcolor'] = '#f5b199';
                    $data['pt_status'] = '拼团中';
                    break;
                case 10 :
                    $data['r_status'] = '未退款';
                    $data['pt_status'] = '拼团失败';
                    $data['bgcolor'] = '#ee827c';
                    break;
                case 11 :
                    $data['r_status'] = '已退款';
                    $data['pt_status'] = '拼团失败';
                    $data['bgcolor'] = '#ee827c';
                    break;
            }
        }
        else if ($data['otype'] == 'JP')
        {
            switch ($data['gstatus'])
            {
                case 0 :
                    $data['status'] = '待付款';
                    $data['r_status'] = 0;
                    $data['bgcolor'] = '#f5b1aa';
                    break;
                case 1 :
                    $data['status'] = '待发货';
                    $data['r_status'] = 1;
                    $data['bgcolor'] = '#f09199';
                    break;
                case 2 :
                    $data['status'] = '待收货';
                    $data['r_status'] = 2;
                    $data['bgcolor'] = '#f19072';
                    break;
                case 5 :
                    $data['status'] = '订单完成';
                    $data['r_status'] = 5;
                    $data['bgcolor'] = '#f7b977';
                    break;
                case 7 :
                    $data['status'] = '订单关闭';
                    $data['r_status'] = 7;
                    $data['bgcolor'] = '#ffbd8b';
                    break;
            }
        }
        else
        {
            switch ($data['gstatus'])
            {
                case 0 :
                    $data['status'] = '待付款';
                    $data['r_status'] = 0;
                    $data['bgcolor'] = '#f5b1aa';
                    break;
                case 1 :
                    $data['status'] = '待发货';
                    $data['r_status'] = 1;
                    $data['bgcolor'] = '#f09199';
                    break;
                case 2 :
                    if($self_lifting == 1)
                    {
                        $data['status'] = '待核销';
                    }
                    else
                    {
                        $data['status'] = '待收货';
                    }
                    $data['r_status'] = 2;
                    $data['bgcolor'] = '#f19072';
                    break;
                case 5 :
                    $data['status'] = '订单完成';
                    $data['r_status'] = 5;
                    $data['bgcolor'] = '#f7b977';
                    break;
                case 7 :
                    $data['status'] = '订单关闭';
                    $data['r_status'] = 7;
                    $data['bgcolor'] = '#ffbd8b';
                    break;
                case 8 :
                    $data['status'] = '待核销';
                    $data['r_status'] = 8;
                    $data['bgcolor'] = '#ffbd8b';
                    break;
            }
        }

        if ($v['otype'] == 'IN')
        {
            $integralid = $v['p_id'];
            $sql = "select g.integral,g.money,c.img from lkt_integral_goods as g left join lkt_configure as c on g.attr_id = c.id where g.id='$integralid'";
            $inr = Db::query($sql);
            if ($inr)
            {
                if($res[$k]['p_integral'] == 0)
                {
                    $res[$k]['p_integral'] = $inr[0]['integral'];
                    $res[$k]['score_deduction'] = $inr[0]['integral'];
                }
                else
                {
                    $res[$k]['score_deduction'] = $res[$k]['p_integral'];
                }
                // $res[$k]['after_discount'] = $inr[0]['money'];
                $res[$k]['p_money'] = $inr[0]['money'];
                $res[$k]['goodsPrice'] = $inr[0]['money'];
                $res[$k]['pic'] = ServerPath::getimgpath($inr[0]['img']);
            }
        }
        if ($data['otype'] == 'FX')
        {
            $comm_amount = round($spz_price * (100 - $comm_discount),2); //分销折扣优惠
        }

        $sdata = array('待付款', '待发货', '待收货', '订单完成', '订单关闭','待核销');

        // $old_total = round(($old_total * $exchange_rate),2);
        // $old_freight = round(($old_freight * $exchange_rate),2);
        // $spz_price = round(($spz_price * $exchange_rate),2);
        // $z_freight = round(($z_freight * $exchange_rate),2);
        // $preferential_amount = round(($preferential_amount * $exchange_rate),2);
        // $coupon_price = round(($coupon_price * $exchange_rate),2);
        
        $old_total = round(($old_total ),2);
        $old_freight = round(($old_freight ),2);
        $spz_price = round(($spz_price ),2);
        $z_freight = round(($z_freight ),2);
        $preferential_amount = round(($preferential_amount),2);
        $coupon_price = round(($coupon_price ),2);
        
        if($data['otype'] == 'ZB')
        {
            $anchor_id = $res[0]['anchor_id'];// 主播ID
            $commission = $res[0]['commission']; // 佣金
            $supplier_settlement = $res[0]['supplier_settlement']; // 总供货价
            
            $sql_u = "select user_name,headimgurl from lkt_user where user_id = '$anchor_id' ";
            $r_u = Db::query($sql_u);
            if($r_u)
            {
                $anchor_name = $r_u[0]['user_name'];
                $anchor_url = $r_u[0]['headimgurl'];
            }

            $list = array('sdata'=>$sdata,'yh_money'=>$yh_money,'zp_res'=>$zp_res,'update_s'=>$update_s,'data'=>$data,'detail'=>$res,'reduce_price'=>$reduce_price,'coupon_price'=>$coupon_price,'preferential_amount'=>$preferential_amount,'grade_rate_amount'=>$grade_rate_amount,'allow'=>$allow,'num'=>$num,'discount_type'=>$discount_type,'pay_price'=>$pay_price,'spz_price'=>$spz_price,'z_freight'=>$z_freight,'grade_rate'=>$grade_rate,'expressStr'=>$expressStr,'remarks'=>$remarks,'isManyMch'=>$isManyMch,'id'=>$id,'otype'=>$otype,'comm_amount'=>$comm_amount,'returnStatus'=>$returnStatus,'supplier_id'=>$supplier_id,'old_total'=>$old_total,'old_freight'=>$old_freight,'returnStatus'=>$returnStatus0,'selfLifting'=>$self_lifting,'anchor_name'=>$anchor_name,'anchor_url'=>$anchor_url,'comm_amount'=>$commission,'totalSupplyPrice'=>$supplier_settlement,'totalSupplierFright'=>$z_freight,'currency_code'=>$currency_code,'currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'cpc'=>$cpc);
        }
        else
        {
            $list = array('sdata'=>$sdata,'yh_money'=>$yh_money,'zp_res'=>$zp_res,'update_s'=>$update_s,'data'=>$data,'detail'=>$res,'reduce_price'=>$reduce_price,'coupon_price'=>$coupon_price,'preferential_amount'=>$preferential_amount,'grade_rate_amount'=>$grade_rate_amount,'allow'=>$allow,'num'=>$num,'discount_type'=>$discount_type,'pay_price'=>$pay_price,'spz_price'=>$spz_price,'z_freight'=>$z_freight,'grade_rate'=>$grade_rate,'expressStr'=>$expressStr,'remarks'=>$remarks,'isManyMch'=>$isManyMch,'id'=>$id,'otype'=>$otype,'comm_amount'=>$comm_amount,'returnStatus'=>$returnStatus,'supplier_id'=>$supplier_id,'old_total'=>$old_total,'old_freight'=>$old_freight,'returnStatus'=>$returnStatus0,'selfLifting'=>$self_lifting,'storeSelfInfo'=>$storeSelfInfo,'totalSupplierFright'=>$totalSupplierFright,'currency_code'=>$currency_code,'currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'cpc'=>$cpc);
        } 
        // var_dump($list);die;
        return $list;
    }

    //订单列表（PC店铺端）
    public static function b_mch_order_index($action)
    {
        $store_id = $action->store_id;
        $ordtype1 = 'ti/普通订单,';
        $Jurisdiction = new Jurisdiction();
        $Plugin = new Plugin();
        $Plugin_arr = $Plugin->is_Plugin1($store_id, 'order');

        $Plugin_arr['res1'] = rtrim($Plugin_arr['res1'], ',');
        $ordtype1 = $ordtype1.$Plugin_arr['res1'];

        $ordtype1 = rtrim($ordtype1, ',');
        $ordtype2 = explode(',', $ordtype1);
        foreach ($ordtype2 as $k => $v)
        {
            $ordtype3 = explode('/', $v);
            $ordtype[$ordtype3[0]] = $ordtype3[1];
        }
        if(isset($action->selfLifting))
        {
            $self_lifting = $action->selfLifting;//是否自提1快递2自提
        }
        if(isset($action->otype))
        {
            $otype = $action->otype;
        }
        if(isset($action->mch_name))
        {
            $mch_name = $action->mch_name; // 请输入店铺名称
        }
        if(isset($action->status))
        {
            $status = $action->status; // 订单状态
        }
        if(isset($action->news_status))
        {
            $news_status = $action->news_status; // 订单状态
        }
        if(isset($action->delivery_status))
        {
            $delivery_status = $action->delivery_status; // 提醒发货
        }
        if(isset($action->readd))
        {
            $readd = $action->readd; // 未查看信息
        }
        if(isset($action->mch_id))
        {
            $mch_id = $action->mch_id; // 店铺ID
        }
        if(isset($action->x_order))
        {
            $x_order = $action->x_order; 
        }
        if(isset($action->operation_type))
        {
            $operation_type = $action->operation_type; 
        }
        if(isset($action->sNo))
        {
            $sNo = $action->sNo;
        }
        if(isset($action->brand))
        {
            $brand = $action->brand;
        }
        if(isset($action->source))
        {
            $source = $action->source;
        }
        if(isset($action->mch_store_id))
        {
            $mch_store_id = $action->mch_store_id;
        }
        if(isset($action->sNo_id))
        {
            $sNo_id = $action->sNo_id;
        }
        // $ostatus = $action->ostatus;
        $prostr = '';
        $URL = '';
        $con = '';
        foreach ($_GET as $key => $value001)
        {
            $con .= "&$key=$value001";
        }

        // 导出
        $pagesize = $action->pagesize;
        $pagesize = $pagesize ? $pagesize : '10';
        // 每页显示多少条数据
        $page = $action->page;


        $condition = " and o.recycle !=3 and o.supplier_id = 0";
        if (isset($brand) && $brand)
        {
            $prostr .= " and lpl.brand_id = '$brand'";
        }
        $brand_str = '';
        if (isset($mch_name) && $mch_name)
        {   
            $mch_name_0 = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and m.name like $mch_name_0 ";
        }
        if (isset($operation_type) && $operation_type)
        {
            $condition .= " and o.operation_type = '$operation_type' ";
        }
        if(isset($self_lifting) && $self_lifting)
        {   
            if($self_lifting == 1)
            {
                $condition .= " and o.self_lifting = 0";
            }
            else if($self_lifting == 5)
            {
                $condition .= " and o.self_lifting = 2";
            }
            else
            {
                $condition .= " and o.self_lifting = 1";
            }
            
        }
        if (isset($source) && $source == 1)
        {
            $condition .= " and o.source = '1' ";
        }
        else if (isset($source) && $source == 2)
        {
            $condition .= " and o.source = '2' ";
        }
        else if (isset($source) && $source == 9)
        {
            $condition .= " and o.status != 0 ";
        }

        if (isset($mch_store_id) )
        {
            $condition .= " and o.single_store = '$mch_store_id' ";
        }

        $startdate = $action->startdate;
        $enddate = $action->enddate;
        if (isset($x_order) && $x_order)
        {
            $startdate = date('Y-m-d 00:00:00');
            $enddate = date('Y-m-d 23:59:59');
            $condition .= " and (o.status = 0 or o.status = 1) ";
        }
        if ($startdate != '')
        {
            $condition .= " and o.add_time >= '$startdate' ";
        }
        if ($enddate != '')
        {
            $condition .= " and o.add_time <= '$enddate' ";
        }
        if ($otype == 't2')
        {
            $condition .= " and o.otype='pt'";
            if ($status == 'g0')
            {
                $condition .= " and o.status= 0 ";
            }
            else if ($status == 'g1')
            {
                $condition .= " and o.status= 9 ";
            }
            else if ($status == 'g2')
            {
                $condition .= " and o.status > 0 and o.status < 9 ";
            }
            else if ($status == 'g3')
            {
                $condition .= " and o.status > 9 ";
            }
        }
        else if ($otype == 't3')
        {
            $condition .= " and o.otype = 'KJ' ";
            if ($status != '')
            {
                $condition .= " and o.status=$status";
            }
        }
        else if ($otype == 't4')
        {
            $condition .= " and o.otype = 'JP' ";
            if ($status != '')
            {
                $condition .= " and o.status=$status";
            }
        }
        else if ($otype == 't5')
        {
            $condition .= " and o.otype = 'FX' ";
            if ($status != '')
            {
                $condition .= " and o.status=$status";
            }
        }
        else if ($otype == 't7')
        {
            $condition .= " and o.otype = 'integral' ";
            if ($status != '')
            {
                $condition .= " and o.status=$status";
            }
        }
        else if ($otype == 't8')
        {
            $condition .= " and o.otype = 'MS' ";
            if ($status != '')
            {
                $condition .= " and o.status=$status";
            }
        }
        else
        {   
            $condition .= " and (o.otype = 'GM' or o.otype = 'VI') ";
            if ($status != '')
            {
                $condition .= " and o.status=$status ";
            }
        }
        if (isset($readd) && $readd)
        { // 未查看信息
            $condition .= " and o.readd = '$readd' ";
        }
        if (isset($news_status) && $news_status === 0)
        { // 消息传过来的参数
            $condition .= " and o.status in (0,1)";
        }
        else if (isset($news_status) && $news_status == 1)
        {
            $condition .= " and o.status = 1";
            $status = 1;
        }
        if (isset($delivery_status) && $delivery_status)
        { // 提现发货
            $condition .= " and o.delivery_status = '$delivery_status' ";
        }
        if (isset($mch_id) && $mch_id)
        {
            $condition .= " and o.mch_id = ',$mch_id,' ";
        }
        if ($sNo != '')
        {
            $sNo_0 = Tools::FuzzyQueryConcatenation($sNo);
            $condition .= " and (o.sNo like $sNo_0 or o.name like $sNo_0 or o.mobile like $sNo_0 or o.user_id like $sNo_0 or o.p_sNo like $sNo_0 ) ";
        }
        if(isset($sNo_id) && $sNo_id != '')
        {
            $sNo_id_0 = Tools::FuzzyQueryConcatenation($sNo_id);
            $condition .= " and (o.sNo like $sNo_id_0 or o.p_sNo like $sNo_id_0) ";
        }

        $sql112 = "select ifnull(count(a.sNo),0) as num from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                right join lkt_mch as m on p.mch_id = m.id
                where o.store_id = '$store_id' and lu.store_id = '$store_id' $condition group by o.sNo) a ";
        $res112 = Db::query($sql112);
        $total = $res112[0]['num'];

        // 页码
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        // $str = " o.real_sno as mchOrderNo,o.id,o.num,o.sNo,o.name,o.sheng,o.shi,o.xian,o.address,o.add_time as createDate,o.mobile,o.z_price as orderPrice,o.status,o.allow,o.drawid,o.otype,o.ptstatus,o.pay,lu.user_name as userName,o.user_id as userId,o.mch_id,m.id as shop_id,o.self_lifting,o.arrive_time,o.operation_type,o.p_sNo,row_number () over (PARTITION BY o.sNo) AS top ";
        $str = " o.real_sno as mchOrderNo,o.id,o.num,o.sNo,o.name as userName,o.sheng,o.shi,o.xian,o.address,o.shop_cpc,o.code,o.add_time as createDate,o.mobile,o.old_total,o.z_price as orderPrice,o.status,o.allow,o.drawid,o.otype,o.ptstatus,o.pay,o.user_id as userId,o.mch_id,m.id as shop_id,o.self_lifting,o.arrive_time,o.operation_type,o.p_sNo,d.store_self_delivery,row_number () over (PARTITION BY o.sNo) AS top,o.currency_symbol,o.exchange_rate,o.currency_code,o.voucher,o.review_status,o.reason_for_rejection ";
        $sql1 = "select tt.* from (select $str  
            from lkt_order as o 
            left join lkt_user as lu on o.user_id = lu.user_id 
            right join lkt_order_details as d on o.sNo = d.r_sNo
            RIGHT JOIN lkt_configure attr ON attr.id=d.sid
            RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
            right join lkt_mch as m on p.mch_id = m.id
            where o.store_id = '$store_id' and lu.store_id = '$store_id' $condition ) AS tt WHERE tt.top < 2 order by tt.createDate desc limit $start,$pagesize";
        $res1 = Db::query($sql1);
        $list = array();
        $oids = array();
        $info = array();
        //支付方式
        $payments = PaymentModel::order('sort','desc')->select()->toArray();
        $payments_type = array();
        foreach ($payments as $keyp => $valuep)
        {
            $payments_type[$valuep['class_name']] = $valuep['name'];
        }
        $mch_id = '';
        if (!empty($res1))
        {
            foreach ($res1 as $k => $v)
            {
                $oids[$v['id']] = $v['id'];
                $shop_id = $v['shop_id'];
                $r_sNo = $v['sNo'];
                $store_self_delivery = $v['store_self_delivery'];
                if($v['self_lifting'] == 2)
                {
                    $sql_self_delivery_info = "select sd.delivery_time,sd.delivery_period from lkt_order_details as d left join lkt_self_delivery_info as sd on d.store_self_delivery = sd.id where store_id = '$store_id' and d.r_sNo = '$r_sNo' ";
                    $r_self_delivery_info = Db::query($sql_self_delivery_info);
                    if($r_self_delivery_info)
                    {
                        $res1[$k]['delivery_time'] = $r_self_delivery_info[0]['delivery_time'];
                        $res1[$k]['delivery_period'] = $r_self_delivery_info[0]['delivery_period'];
                    }
                }
                if($v['old_total'] == '0.00' || $v['old_total'] == '')
                {
                    $res1[$k]['old_total'] = $v['orderPrice'];
                }

                $res1[$k]['logistics_type'] = false;
                $sql_express_delivery = "select * from lkt_express_delivery where store_id = '$store_id' and sNo = '$r_sNo' and subtable_id != 0";
                $r_express_delivery = Db::query($sql_express_delivery);
                if($r_express_delivery)
                {
                    $res1[$k]['logistics_type'] = true;
                }

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

                $freight = 0;
                $mch_id = $v['mch_id'];
                $mch_arr = explode(',', $mch_id);

                $array_address = array('cpc'=>$v['shop_cpc'],'sheng'=>$v['sheng'],'shi'=>$v['shi'],'xian'=>$v['xian'],'address'=>$v['address'],'code'=>$v['code']);
                $address_xq = PC_Tools::address_translation($array_address);

                $res1[$k]['addressInfo'] = $address_xq;
                $zqprice = 0;
                $order_id = $v['sNo'];
                $pay = $v['pay'];

                if (array_key_exists($pay, $payments_type))
                {
                    $res1[$k]['payName'] = $payments_type[$pay];
                }
                else
                {
                    $res1[$k]['payName'] = '钱包';
                }

                $user_id = $v['userId'];
                $sqldt = "select lpl.imgurl,lpl.product_title,lpl.product_number,lod.p_price,lod.unit,lod.num,lod.size,lod.p_id,lod.courier_num,lod.express_id,lod.freight,lpl.brand_id ,lm.name as mchname,lod.r_status,lod.id,attr.img
                        from lkt_order_details as lod 
                        left JOIN lkt_configure attr ON attr.id=lod.sid
                        left join lkt_product_list as lpl on lpl.id=attr.pid 
                        LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id 
                        where r_sNo='".$v['sNo']."' $prostr";
                $products = Db::query($sqldt);
                $res1[$k]['freight'] = $freight;

                if ($products)
                {
                    $courier_num = array();       // 快递单号
                    $res1[$k]['courier_num'] = '';
                    foreach ($products as $kd => $vd)
                    {
                        $freight += $vd['freight'];

                        $vd['imgurl'] = ServerPath::getimgpath($vd['imgurl'], $store_id);
                        if($vd['img'])
                        {
                            $vd['imgurl'] = ServerPath::getimgpath($vd['img'], $store_id);
                        }
                        if ($vd['express_id'])
                        {
                            $exper_id = $vd['express_id'];
                            $r03 = ExpressModel::where('id',$exper_id)->select()->toArray();
                            $courier_num[] = $vd['courier_num'] . "  (" . $r03[0]['kuaidi_name'] . ")"; // 快递单号
                            if($res1[$k]['courier_num'] == '')
                            {
                                $res1[$k]['courier_num'] = $vd['courier_num'];
                            }
                            else
                            {
                                $res1[$k]['courier_num'] .= ',' . $vd['courier_num'];
                            }
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
                    $res1[$k]['detailFreight'] = $products[0]['freight'];
                    $res1[$k]['detailId'] = $products[0]['id'];
                    $res1[$k]['goodsImgUrl'] = ServerPath::getimgpath($products[0]['imgurl'], $store_id);
                    
                    $res1[$k]['goodsName'] = $products[0]['product_title'];
                    $res1[$k]['goodsPrice'] = $products[0]['p_price'];
                    $res1[$k]['goodsNum'] = count($products);
                    $res1[$k]['expressList'] = $courier_num_arr;
                    $res1[$k]['expressStr'] = $expressStr;
                    
                    $res1[$k]['mchName'] = $products[0]['mchname'];
                    $res1[$k]['shopName'] = $products[0]['mchname'];
                    $res1[$k]['needNum'] = $products[0]['num'];
                    $res1[$k]['orderno'] = $v['sNo'];
                    $res1[$k]['attrStr'] = $products[0]['size'];
                    
                    $pt_status = '';
                    if ($v['otype'] == 'pt')
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res1[$k]['status'] = '待付款';
                                $res1[$k]['pt_status'] = '待付款';
                                break;
                            case 1 :
                                $res1[$k]['pt_status'] = '拼团成功';
                                $res1[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                $res1[$k]['pt_status'] = '拼团成功';
                                $res1[$k]['status'] = '待收货';
                                break;
                            case 5 :
                                $res1[$k]['status'] = '订单完成';
                                $res1[$k]['pt_status'] = '拼团成功';
                                break;
                            case 7 :
                                $res1[$k]['status'] = '订单关闭';
                                $res1[$k]['pt_status'] = '拼团成功';
                                break;
                        }
                    }
                    else
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res1[$k]['status'] = '待付款';
                                break;
                            case 1 :
                                $res1[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                if (isset($source) && $source == 9)
                                {
                                    $res1[$k]['status'] = '待核销';
                                }
                                else
                                {
                                    $res1[$k]['status'] = '待收货';
                                }
                                // if($v['self_lifting'] == 1)
                                // {
                                //     $res1[$k]['status'] = '待核销';
                                // }
                                // else
                                // {
                                //     $res1[$k]['status'] = '待收货';
                                // }
                                break;
                            case 5 :
                                if($v['self_lifting'] == 1)
                                {
                                    $res1[$k]['status'] = '已完成';
                                }
                                else
                                {
                                    $res1[$k]['status'] = '已完成';
                                }
                                break;
                            case 7 :
                                $res1[$k]['status'] = '已关闭';
                                break;
                        }
                    }

                    if ($v['otype'] == 'IN')
                    {
                        $integralid = $v['p_sNo'];
                        $sql = "select g.integral,g.money,c.img from lkt_integral_goods as g left join lkt_configure as c on g.attr_id = c.id where g.id='$integralid'";
                        $inr = Db::query($sql);
                        if ($inr)
                        {
                            $v['p_integral'] = $inr[0]['integral'];
                            $v['p_money'] = $inr[0]['money'];
                            $vd['imgurl'] = ServerPath::getimgpath($inr[0]['img']);
                        }
                    }
                    switch ($v['self_lifting']) {
                        case '1':
                            $res1[$k]['selfLiftingName'] = '自提';
                            break;
                        case '2':
                            $res1[$k]['selfLiftingName'] = '同城配送';
                            break;
                        default:
                            $res1[$k]['selfLiftingName'] = '快递';
                            break;
                    }
                    //订单类型
                    switch ($v['otype']) 
                    {
                        case 'FX':
                            $res1[$k]['otype'] = '分销';
                            break;

                        case 'pt':
                            $res1[$k]['otype'] = '拼团';
                            break;

                        case 'KJ':
                            $res1[$k]['otype'] = '砍价';
                            break;

                        case 'JP':
                            $res1[$k]['otype'] = '竞拍';
                            break;

                        case 'MS':
                            $res1[$k]['otype'] = '秒杀';
                            break;

                        case 'integral':
                            $res1[$k]['otype'] = '积分';
                            break;

                        case 'PP':
                            $res1[$k]['otype'] = '拼团';
                            break;

                        case 'PM':
                            $res1[$k]['otype'] = '秒杀';
                            break;
                        
                        default:
                            $res1[$k]['otype'] = '普通';
                            break;
                    }

                    $res1[$k]['freight'] = $freight;
                    $list[] = $res1[$k];
                }
            }
        }
        else
        {
            $list = array();
        }
        if ($oids) 
        {
            $oids = implode(',',$oids);
            $str1 = " o.real_sno as mchOrderNo,o.id,d.num,o.sNo,o.name as userName,o.sheng,o.shi,o.xian,o.address,o.shop_cpc,o.code,o.add_time as createDate,o.mobile,o.z_price as orderPrice,o.status,o.allow,o.drawid,o.otype,o.ptstatus,o.pay,o.user_id as userId,o.mch_id,m.id as shop_id,o.self_lifting,o.arrive_time,o.operation_type,o.p_sNo,row_number () over (PARTITION BY o.sNo) AS top,p.imgurl,p.product_title,p.product_number,d.p_price,d.unit,d.size,d.p_id,d.courier_num,d.express_id,d.freight,p.brand_id ,m.name as mchname,d.r_status,d.id as did,attr.img ";
            $sql11 = "select tt.* from (select $str1  
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                right join lkt_mch as m on p.mch_id = m.id
                where o.store_id = '$store_id' and lu.store_id = '$store_id' and o.id in ($oids) $condition ) AS tt WHERE 1 order by tt.createDate desc ";
            $res11 = Db::query($sql11);
            if (!empty($res11))
            {
                foreach ($res11 as $k => $v)
                {
                    $shop_id = $v['shop_id'];
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

                    $freight = 0;
                    $mch_id = $v['mch_id'];
                    $mch_arr = explode(',', $mch_id);

                    $array_address = array('cpc'=>$v['shop_cpc'],'sheng'=>$v['sheng'],'shi'=>$v['shi'],'xian'=>$v['xian'],'address'=>$v['address'],'code'=>$v['code']);
                    $address_xq = PC_Tools::address_translation($array_address);

                    $res11[$k]['addressInfo'] = $address_xq;
                    $zqprice = 0;
                    $order_id = $v['sNo'];
                    $pay = $v['pay'];

                    if (array_key_exists($pay, $payments_type))
                    {
                        $res11[$k]['payName'] = $payments_type[$pay];
                    }
                    else
                    {
                        $res11[$k]['payName'] = '钱包';
                    }

                    $user_id = $v['userId'];
                    $courier_num = array();       // 快递单号
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
                    $res11[$k]['freight'] = $v['freight'];
                    $res11[$k]['detailFreight'] = $v['freight'];
                    $res11[$k]['detailId'] = $v['did'];
                    $res11[$k]['goodsImgUrl'] = ServerPath::getimgpath($v['img'], $store_id);
                    
                    $res11[$k]['goodsName'] = $v['product_title'];
                    $res11[$k]['goodsPrice'] = $v['p_price'];
                    $res11[$k]['goodsNum'] = $v['num'];
                    $res11[$k]['expressList'] = $courier_num_arr;
                    $res11[$k]['expressStr'] = $expressStr;
                    
                    $res11[$k]['mchName'] = $v['mchname'];
                    $res11[$k]['shopName'] = $v['mchname'];
                    $res11[$k]['needNum'] = $v['num'];
                    $res11[$k]['orderno'] = $v['sNo'];
                    $res11[$k]['attrStr'] = $v['size'];
                    
                    $pt_status = '';
                    if ($v['otype'] == 'pt')
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res1[$k]['status'] = '待付款';
                                $res1[$k]['pt_status'] = '待付款';
                                break;
                            case 1 :
                                $res1[$k]['pt_status'] = '拼团成功';
                                $res1[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                $res1[$k]['pt_status'] = '拼团成功';
                                $res1[$k]['status'] = '待收货';
                                break;
                            case 5 :
                                $res1[$k]['status'] = '订单完成';
                                $res1[$k]['pt_status'] = '拼团成功';
                                break;
                            case 7 :
                                $res1[$k]['status'] = '订单关闭';
                                $res1[$k]['pt_status'] = '拼团成功';
                                break;
                        }
                    }
                    else
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res11[$k]['status'] = '待付款';
                                break;
                            case 1 :
                                $res11[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                if($v['self_lifting'] == 1)
                                {
                                    $res11[$k]['status'] = '待核销';
                                }
                                else
                                {
                                    $res11[$k]['status'] = '待收货';
                                }
                                break;
                            case 5 :
                                if($v['self_lifting'] == 1)
                                {
                                    $res11[$k]['status'] = '已完成';
                                }
                                else
                                {
                                    $res11[$k]['status'] = '订单完成';
                                }
                                break;
                            case 7 :
                                $res11[$k]['status'] = '已关闭';
                                break;
                        }
                    }

                    if ($v['otype'] == 'IN')
                    {
                        $integralid = $v['p_sNo'];
                        $sql = "select g.integral,g.money,c.img from lkt_integral_goods as g left join lkt_configure as c on g.attr_id = c.id where g.id='$integralid'";
                        $inr = Db::query($sql);
                        if ($inr)
                        {
                            $v['p_integral'] = $inr[0]['integral'];
                            $v['p_money'] = $inr[0]['money'];
                            $vd['imgurl'] = ServerPath::getimgpath($inr[0]['img']);
                        }
                    }
                    switch ($v['self_lifting']) {
                        case '1':
                            $res11[$k]['selfLiftingName'] = '自提';
                            break;
                        
                        case '2':
                            $res11[$k]['selfLiftingName'] = '同城配送';
                            break;
                        
                        default:
                            $res11[$k]['selfLiftingName'] = '快递';
                            break;
                    }
                    //订单类型
                    switch ($v['otype']) 
                    {
                        case 'FX':
                            $res11[$k]['otype'] = '分销';
                            break;

                        case 'pt':
                            $res11[$k]['otype'] = '拼团';
                            break;

                        case 'KJ':
                            $res11[$k]['otype'] = '砍价';
                            break;

                        case 'JP':
                            $res11[$k]['otype'] = '竞拍';
                            break;

                        case 'MS':
                            $res11[$k]['otype'] = '秒杀';
                            break;

                        case 'integral':
                            $res11[$k]['otype'] = '积分';
                            break;

                        case 'PP':
                            $res11[$k]['otype'] = '拼团';
                            break;

                        case 'PM':
                            $res11[$k]['otype'] = '秒杀';
                            break;
                        
                        default:
                            $res11[$k]['otype'] = '普通';
                            break;
                    }

                    $info[] = $res11[$k];

                }
            }
        }
        $class = PC_Tools::data_dictionary('订单状态', $status);
        $info = array('brand_str'=>$brand_str,'ordtype'=>$ordtype,'list'=>$list,'info'=>$info,'total'=>$total,'class'=>$class,'mch_id'=>$mch_id);
        return $info;
    }

    // 订单列表（PC门店核销端）
    public static function MchSon_order_index($action)
    {
        $store_id = $action->store_id;
        $ordtype1 = 'ti/普通订单,';
        $Jurisdiction = new Jurisdiction();
        $Plugin = new Plugin();
        $Plugin_arr = $Plugin->is_Plugin1($store_id, 'order');

        $Plugin_arr['res1'] = rtrim($Plugin_arr['res1'], ',');
        $ordtype1 = $ordtype1.$Plugin_arr['res1'];

        $ordtype1 = rtrim($ordtype1, ',');
        $ordtype2 = explode(',', $ordtype1);
        foreach ($ordtype2 as $k => $v)
        {
            $ordtype3 = explode('/', $v);
            $ordtype[$ordtype3[0]] = $ordtype3[1];
        }
        if(isset($action->selfLifting))
        {
            $self_lifting = $action->selfLifting;//是否自提1快递2自提
        }
        if(isset($action->mch_name))
        {
            $mch_name = $action->mch_name; // 请输入店铺名称
        }
        if(isset($action->status))
        {
            $status = $action->status; // 订单状态
        }
        if(isset($action->news_status))
        {
            $news_status = $action->news_status; // 订单状态
        }
        if(isset($action->delivery_status))
        {
            $delivery_status = $action->delivery_status; // 提醒发货
        }
        if(isset($action->readd))
        {
            $readd = $action->readd; // 未查看信息
        }
        if(isset($action->mch_id))
        {
            $mch_id = $action->mch_id; // 店铺ID
        }
        if(isset($action->x_order))
        {
            $x_order = $action->x_order; 
        }
        if(isset($action->operation_type))
        {
            $operation_type = $action->operation_type; 
        }
        if(isset($action->sNo))
        {
            $sNo = $action->sNo;
        }
        if(isset($action->brand))
        {
            $brand = $action->brand;
        }
        if(isset($action->source))
        {
            $source = $action->source;
        }
        if(isset($action->mch_store_id))
        {
            $mch_store_id = $action->mch_store_id;
        }
        if(isset($action->administrators_id))
        {
            $administrators_id = $action->administrators_id;
        }
        $prostr = '';
        $URL = '';
        $con = '';
        foreach ($_GET as $key => $value001)
        {
            $con .= "&$key=$value001";
        }

        // 导出
        $pagesize = $action->pagesize;
        $pagesize = $pagesize ? $pagesize : '10';
        // 每页显示多少条数据
        $page = $action->page;

        $condition = " and o.recycle !=3 and o.supplier_id = 0";
        if (isset($brand) && $brand)
        {
            $prostr .= " and lpl.brand_id = '$brand'";
        }
        $brand_str = '';
        if (isset($mch_name) && $mch_name)
        {   
            $mch_name = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and m.name like $mch_name ";
        }
        if (isset($operation_type) && $operation_type)
        {
            $condition .= " and o.operation_type = '$operation_type' ";
        }
        if(isset($self_lifting) && $self_lifting)
        {   
            if($self_lifting == 1)
            {
                $condition .= " and o.self_lifting = 0";
            }
            else
            {
                $condition .= " and (o.self_lifting = 1 or o.self_lifting = 3)";
            }
            
        }
        if (isset($source) && $source == 1)
        {
            $condition .= " and o.source = '1' ";
        }
        else if (isset($source) && $source == 2)
        {
            $condition .= " and o.source = '2' ";
        }
        else if (isset($source) && $source == 9)
        {
            $condition .= " and o.status != 0 ";
        }

        if (isset($mch_store_id) )
        {
            $condition .= " and o.single_store = '$mch_store_id' ";
        }
        // if (isset($administrators_id) )
        // {
        //     $condition .= " and (o.VerifiedBy = '$administrators_id' or o.VerifiedBy = '0') ";
        // }
        if($status == 2)
        {
            $condition .= " and (o.status = 2 or o.status = 8) ";
        }
        else if($status == 5)
        {
            $condition .= " and o.status = 5 and o.VerifiedBy_type = 2 and o.VerifiedBy = '$administrators_id' ";
        }
        else
        {
            $condition .= " and (o.status = 2 or o.status = 8 or (o.status = 5 and o.VerifiedBy_type = 2 and o.VerifiedBy = '$administrators_id')) ";
        }

        $startdate = $action->startdate;
        $enddate = $action->enddate;
        if (isset($x_order) && $x_order)
        {
            $startdate = date('Y-m-d 00:00:00');
            $enddate = date('Y-m-d 23:59:59');
            $condition .= " and (o.status = 0 or o.status = 1) ";

        }
        if ($startdate != '')
        {
            $condition .= " and o.add_time >= '$startdate' ";
        }
        if ($enddate != '')
        {
            $condition .= " and o.add_time <= '$enddate' ";
        }
        
        if (isset($readd) && $readd)
        { // 未查看信息
            $condition .= " and o.readd = '$readd' ";
        }
        if (isset($news_status) && $news_status === 0)
        { // 消息传过来的参数
            $condition .= " and o.status in (0,1)";
        }
        else if (isset($news_status) && $news_status == 1)
        {
            $condition .= " and o.status = 1";
            $status = 1;
        }
        if (isset($delivery_status) && $delivery_status)
        { // 提现发货
            $condition .= " and o.delivery_status = '$delivery_status' ";
        }
        if (isset($mch_id) && $mch_id)
        {
            $condition .= " and o.mch_id = ',$mch_id,' ";
        }
        if ($sNo != '')
        {
            $sNo_0 = Tools::FuzzyQueryConcatenation($sNo);
            $condition .= " and (o.sNo like $sNo_0 or o.name like $sNo_0 or o.mobile like $sNo_0 or o.user_id like $sNo_0 or o.p_sNo like $sNo_0 ) ";
        }

        $sql112 = "select ifnull(count(a.sNo),0) as num from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                right join lkt_mch as m on p.mch_id = m.id
                where o.store_id = '$store_id' and lu.store_id = '$store_id' $condition group by o.sNo) a ";
        $res112 = Db::query($sql112);
        $total = $res112[0]['num'];

        // 页码
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        $str = " o.real_sno as mchOrderNo,o.id,o.num,o.sNo,o.name as userName,o.sheng,o.shi,o.xian,o.address,o.shop_cpc,o.code,o.add_time as createDate,o.mobile,o.z_price as orderPrice,o.status,o.allow,o.drawid,o.otype,o.ptstatus,o.pay,o.user_id as userId,o.mch_id,m.id as shop_id,o.self_lifting,o.arrive_time,o.operation_type,o.p_sNo,row_number () over (PARTITION BY o.sNo) AS top ";
        $sql1 = "select tt.* from (select $str  
            from lkt_order as o 
            left join lkt_user as lu on o.user_id = lu.user_id 
            right join lkt_order_details as d on o.sNo = d.r_sNo
            RIGHT JOIN lkt_configure attr ON attr.id=d.sid
            RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
            right join lkt_mch as m on p.mch_id = m.id
            where o.store_id = '$store_id' and lu.store_id = '$store_id' $condition ) AS tt WHERE tt.top < 2 order by tt.createDate desc limit $start,$pagesize";
        $res1 = Db::query($sql1);
        $list = array();
        $oids = array();
        $info = array();
        //支付方式
        $payments = PaymentModel::order('sort','desc')->select()->toArray();
        $payments_type = array();
        foreach ($payments as $keyp => $valuep)
        {
            $payments_type[$valuep['class_name']] = $valuep['name'];
        }
        $mch_id = '';
        if (!empty($res1))
        {
            foreach ($res1 as $k => $v)
            {
                $oids[$v['id']] = $v['id'];
                $shop_id = $v['shop_id'];
                $r_sNo = $v['sNo'];

                $res1[$k]['logistics_type'] = false;
                $sql_express_delivery = "select * from lkt_express_delivery where store_id = '$store_id' and sNo = '$r_sNo' and subtable_id != 0";
                $r_express_delivery = Db::query($sql_express_delivery);
                if($r_express_delivery)
                {
                    $res1[$k]['logistics_type'] = true;
                }

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

                $freight = 0;
                $mch_id = $v['mch_id'];
                $mch_arr = explode(',', $mch_id);

                $array_address = array('cpc'=>$v['shop_cpc'],'sheng'=>$v['sheng'],'shi'=>$v['shi'],'xian'=>$v['xian'],'address'=>$v['address'],'code'=>$v['code']);
                $address_xq = PC_Tools::address_translation($array_address);

                $res1[$k]['addressInfo'] = $address_xq;
                $zqprice = 0;
                $order_id = $v['sNo'];
                $pay = $v['pay'];

                if (array_key_exists($pay, $payments_type))
                {
                    $res1[$k]['payName'] = $payments_type[$pay];
                }
                else
                {
                    $res1[$k]['payName'] = '钱包';
                }

                $user_id = $v['userId'];
                $sqldt = "select lpl.imgurl,lpl.product_title,lpl.product_number,lod.p_price,lod.unit,lod.num,lod.size,lod.p_id,lod.courier_num,lod.express_id,lod.freight,lpl.brand_id ,lm.name as mchname,lod.r_status,lod.id,attr.img
                        from lkt_order_details as lod 
                        left JOIN lkt_configure attr ON attr.id=lod.sid
                        left join lkt_product_list as lpl on lpl.id=attr.pid 
                        LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id 
                        where r_sNo='".$v['sNo']."' $prostr";
                $products = Db::query($sqldt);
                $res1[$k]['freight'] = $freight;

                if ($products)
                {
                    $courier_num = array();       // 快递单号
                    foreach ($products as $kd => $vd)
                    {
                        $freight += $vd['freight'];

                        $vd['imgurl'] = ServerPath::getimgpath($vd['imgurl'], $store_id);
                        if($vd['img'])
                        {
                            $vd['imgurl'] = ServerPath::getimgpath($vd['img'], $store_id);
                        }
                        $res1[$k]['courier_num'] = null;
                        if ($vd['express_id'])
                        {
                            $exper_id = $vd['express_id'];
                            $r03 = ExpressModel::where('id',$exper_id)->select()->toArray();
                            $courier_num[] = $vd['courier_num'] . "  (" . $r03[0]['kuaidi_name'] . ")"; // 快递单号
                            $res1[$k]['courier_num'] = $vd['courier_num'];
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
                    $res1[$k]['detailFreight'] = $products[0]['freight'];
                    $res1[$k]['detailId'] = $products[0]['id'];
                    $res1[$k]['goodsImgUrl'] = ServerPath::getimgpath($products[0]['imgurl'], $store_id);
                    
                    $res1[$k]['goodsName'] = $products[0]['product_title'];
                    $res1[$k]['goodsPrice'] = $products[0]['p_price'];
                    $res1[$k]['goodsNum'] = count($products);
                    $res1[$k]['expressList'] = $courier_num_arr;
                    $res1[$k]['expressStr'] = $expressStr;
                    
                    $res1[$k]['mchName'] = $products[0]['mchname'];
                    $res1[$k]['shopName'] = $products[0]['mchname'];
                    $res1[$k]['needNum'] = $products[0]['num'];
                    $res1[$k]['orderno'] = $v['sNo'];
                    $res1[$k]['attrStr'] = $products[0]['size'];
                    
                    $pt_status = '';
                    if ($v['otype'] == 'pt')
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res1[$k]['status'] = '待付款';
                                $res1[$k]['pt_status'] = '待付款';
                                break;
                            case 1 :
                                $res1[$k]['pt_status'] = '拼团成功';
                                $res1[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                $res1[$k]['pt_status'] = '拼团成功';
                                $res1[$k]['status'] = '待收货';
                                break;
                            case 5 :
                                $res1[$k]['status'] = '订单完成';
                                $res1[$k]['pt_status'] = '拼团成功';
                                break;
                            case 7 :
                                $res1[$k]['status'] = '订单关闭';
                                $res1[$k]['pt_status'] = '拼团成功';
                                break;
                        }
                    }
                    else
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res1[$k]['status'] = '待付款';
                                break;
                            case 1 :
                                $res1[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                if (isset($source) && $source == 9)
                                {
                                    $res1[$k]['status'] = '待核销';
                                }
                                else
                                {
                                    $res1[$k]['status'] = '待收货';
                                }
                                break;
                            case 5 :
                                if($v['self_lifting'] == 1)
                                {
                                    $res1[$k]['status'] = '已完成';
                                }
                                else
                                {
                                    $res1[$k]['status'] = '已完成';
                                }
                                break;
                            case 7 :
                                $res1[$k]['status'] = '已关闭';
                                break;
                            case 8 :
                                if (isset($source) && $source == 9)
                                {
                                    $res1[$k]['status'] = '待核销';
                                }
                                else
                                {
                                    $res1[$k]['status'] = '待收货';
                                }
                                break;
                        }
                    }

                    if ($v['otype'] == 'IN')
                    {
                        $integralid = $v['p_sNo'];
                        $sql = "select g.integral,g.money,c.img from lkt_integral_goods as g left join lkt_configure as c on g.attr_id = c.id where g.id='$integralid'";
                        $inr = Db::query($sql);
                        if ($inr)
                        {
                            $v['p_integral'] = $inr[0]['integral'];
                            $v['p_money'] = $inr[0]['money'];
                            $vd['imgurl'] = ServerPath::getimgpath($inr[0]['img']);
                        }
                    }
                    switch ($v['self_lifting']) {
                        case '1':
                            $res1[$k]['selfLiftingName'] = '自提';
                            break;
                        
                        case '2':
                            $res1[$k]['selfLiftingName'] = '同城配送';
                            break;
                        
                        default:
                            $res1[$k]['selfLiftingName'] = '快递';
                            break;
                    }
                    //订单类型
                    switch ($v['otype']) 
                    {
                        case 'FX':
                            $res1[$k]['otype'] = '分销';
                            break;

                        case 'pt':
                            $res1[$k]['otype'] = '拼团';
                            break;

                        case 'KJ':
                            $res1[$k]['otype'] = '砍价';
                            break;

                        case 'JP':
                            $res1[$k]['otype'] = '竞拍';
                            break;

                        case 'MS':
                            $res1[$k]['otype'] = '秒杀';
                            break;

                        case 'integral':
                            $res1[$k]['otype'] = '积分';
                            break;

                        case 'PP':
                            $res1[$k]['otype'] = '拼团';
                            break;

                        case 'PM':
                            $res1[$k]['otype'] = '秒杀';
                            break;
                        
                        default:
                            $res1[$k]['otype'] = '普通';
                            break;
                    }

                    $res1[$k]['freight'] = $freight;
                    $list[] = $res1[$k];
                }
            }
        }
        else
        {
            $list = array();
        }
        if ($oids) 
        {
            $oids = implode(',',$oids);
            $str1 = " o.real_sno as mchOrderNo,o.id,d.num,o.sNo,o.name as userName,o.sheng,o.shi,o.xian,o.address,o.shop_cpc,o.code,o.add_time as createDate,o.mobile,o.z_price as orderPrice,o.status,o.allow,o.drawid,o.otype,o.ptstatus,o.pay,o.user_id as userId,o.mch_id,m.id as shop_id,o.self_lifting,o.arrive_time,o.operation_type,o.p_sNo,row_number () over (PARTITION BY o.sNo) AS top,p.imgurl,p.product_title,p.product_number,d.p_price,d.unit,d.size,d.p_id,d.courier_num,d.express_id,d.freight,p.brand_id ,m.name as mchname,d.r_status,d.id as did,attr.img ";
            $sql11 = "select tt.* from (select $str1  
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                right join lkt_mch as m on p.mch_id = m.id
                where o.store_id = '$store_id' and lu.store_id = '$store_id' and o.id in ($oids) $condition ) AS tt WHERE 1 order by tt.createDate desc ";
            $res11 = Db::query($sql11);
            if (!empty($res11))
            {
                foreach ($res11 as $k => $v)
                {
                    $shop_id = $v['shop_id'];
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

                    $freight = 0;
                    $mch_id = $v['mch_id'];
                    $mch_arr = explode(',', $mch_id);

                    $array_address = array('cpc'=>$v['shop_cpc'],'sheng'=>$v['sheng'],'shi'=>$v['shi'],'xian'=>$v['xian'],'address'=>$v['address'],'code'=>$v['code']);
                    $address_xq = PC_Tools::address_translation($array_address);

                    $res11[$k]['addressInfo'] = $address_xq;
                    $zqprice = 0;
                    $order_id = $v['sNo'];
                    $pay = $v['pay'];

                    if (array_key_exists($pay, $payments_type))
                    {
                        $res11[$k]['payName'] = $payments_type[$pay];
                    }
                    else
                    {
                        $res11[$k]['payName'] = '钱包';
                    }

                    $user_id = $v['userId'];
                    $courier_num = array();       // 快递单号
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
                    $res11[$k]['freight'] = $v['freight'];
                    $res11[$k]['detailFreight'] = $v['freight'];
                    $res11[$k]['detailId'] = $v['did'];
                    $res11[$k]['goodsImgUrl'] = ServerPath::getimgpath($v['img'], $store_id);
                    
                    $res11[$k]['goodsName'] = $v['product_title'];
                    $res11[$k]['goodsPrice'] = $v['p_price'];
                    $res11[$k]['goodsNum'] = $v['num'];
                    $res11[$k]['expressList'] = $courier_num_arr;
                    $res11[$k]['expressStr'] = $expressStr;
                    
                    $res11[$k]['mchName'] = $v['mchname'];
                    $res11[$k]['shopName'] = $v['mchname'];
                    $res11[$k]['needNum'] = $v['num'];
                    $res11[$k]['orderno'] = $v['sNo'];
                    $res11[$k]['attrStr'] = $v['size'];
                    
                    $pt_status = '';
                    if ($v['otype'] == 'pt')
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res1[$k]['status'] = '待付款';
                                $res1[$k]['pt_status'] = '待付款';
                                break;
                            case 1 :
                                $res1[$k]['pt_status'] = '拼团成功';
                                $res1[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                $res1[$k]['pt_status'] = '拼团成功';
                                $res1[$k]['status'] = '待收货';
                                break;
                            case 5 :
                                $res1[$k]['status'] = '订单完成';
                                $res1[$k]['pt_status'] = '拼团成功';
                                break;
                            case 7 :
                                $res1[$k]['status'] = '订单关闭';
                                $res1[$k]['pt_status'] = '拼团成功';
                                break;
                        }
                    }
                    else
                    {
                        switch ($v['status'])
                        {
                            case 0 :
                                $res11[$k]['status'] = '待付款';
                                break;
                            case 1 :
                                $res11[$k]['status'] = '待发货';
                                break;
                            case 2 :
                                if($v['self_lifting'] == 1)
                                {
                                    $res11[$k]['status'] = '待核销';
                                }
                                else
                                {
                                    $res11[$k]['status'] = '待收货';
                                }
                                break;
                            case 5 :
                                if($v['self_lifting'] == 1)
                                {
                                    $res11[$k]['status'] = '已完成';
                                }
                                else
                                {
                                    $res11[$k]['status'] = '订单完成';
                                }
                                break;
                            case 7 :
                                $res11[$k]['status'] = '已关闭';
                                break;
                        }
                    }

                    if ($v['otype'] == 'IN')
                    {
                        $integralid = $v['p_sNo'];
                        $sql = "select g.integral,g.money,c.img from lkt_integral_goods as g left join lkt_configure as c on g.attr_id = c.id where g.id='$integralid'";
                        $inr = Db::query($sql);
                        if ($inr)
                        {
                            $v['p_integral'] = $inr[0]['integral'];
                            $v['p_money'] = $inr[0]['money'];
                            $vd['imgurl'] = ServerPath::getimgpath($inr[0]['img']);
                        }
                    }
                    switch ($v['self_lifting']) {
                        case '1':
                            $res11[$k]['selfLiftingName'] = '自提';
                            break;
                        
                        case '2':
                            $res11[$k]['selfLiftingName'] = '同城配送';
                            break;
                        
                        default:
                            $res11[$k]['selfLiftingName'] = '快递';
                            break;
                    }
                    //订单类型
                    switch ($v['otype']) 
                    {
                        case 'FX':
                            $res11[$k]['otype'] = '分销';
                            break;

                        case 'pt':
                            $res11[$k]['otype'] = '拼团';
                            break;

                        case 'KJ':
                            $res11[$k]['otype'] = '砍价';
                            break;

                        case 'JP':
                            $res11[$k]['otype'] = '竞拍';
                            break;

                        case 'MS':
                            $res11[$k]['otype'] = '秒杀';
                            break;

                        case 'integral':
                            $res11[$k]['otype'] = '积分';
                            break;

                        case 'PP':
                            $res11[$k]['otype'] = '拼团';
                            break;

                        case 'PM':
                            $res11[$k]['otype'] = '秒杀';
                            break;
                        
                        default:
                            $res11[$k]['otype'] = '普通';
                            break;
                    }
                    $info[] = $res11[$k];
                }
            }
        }
        $class = PC_Tools::data_dictionary('订单状态', $status);
        $info = array('brand_str'=>$brand_str,'ordtype'=>$ordtype,'list'=>$list,'info'=>$info,'total'=>$total,'class'=>$class,'mch_id'=>$mch_id);
        return $info;
    }

    // 去除重复
    public static function a_array_unique($array)
    { //写的比较好（写方法）
        $out = array(); //定义变量out为一个数组
        foreach ($array as $key => $value)
        { //将$array数组按照$key=>$value的样式进行遍历
            if (!in_array($value, $out))
            { //如果$value不存在于out数组中，则执行
                $out[$key] = $value; //将该value值存入out数组中
            }
        }
        return $out; //最后返回数组out
    }

    // 电子面单
    public static function ShippingRecords($array)
    {
        $store_id = $array['store_id'];
        $express_name = $array['express_name'];
        $sNo = $array['sNo'];
        $mch_name = $array['mch_name'];
        $status = $array['status'];
        $startdate = $array['startdate'];
        $enddate = $array['enddate'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $pagesize = $pagesize ? $pagesize : 10;
        $shop_id = 0;
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $list = array();
        $total = 0;

        $condition = " a.store_id = '$store_id' and a.subtable_id != 0 ";
        if($shop_id != 0)
        {
            $condition .= " and e.id = '$shop_id' ";
        }
        if($express_name != '')
        {
            $express_name_0 = Tools::FuzzyQueryConcatenation($express_name);
            $condition .= " and (a.courier_num like $express_name_0 or a.kdComOrderNum like $express_name_0) ";
        }
        if($sNo != '')
        {
            $sNo_0 = Tools::FuzzyQueryConcatenation($sNo);
            $condition .= " and (a.sNo like $sNo_0 or a.courier_num like $sNo_0) ";
        }
        if($mch_name != '')
        {
            $condition .= " and e.name = '$mch_name' ";
        }
        if($status != '')
        {
            $condition .= " and a.is_status = '$status' ";
        }
        if($startdate != '')
        {
            $condition .= " and a.deliver_time >= '$startdate' ";
        }
        if($enddate != '')
        {
            $condition .= " and a.deliver_time <= '$enddate' ";
        }

        $sql0 = "select tt.* from (select a.id,row_number () over (PARTITION BY a.courier_num) AS top from lkt_express_delivery as a right join lkt_order as o on a.sNo = o.sNo left join lkt_express as b on a.express_id = b.id left join lkt_order_details as c on a.order_details_id = c.id left join lkt_product_list as d on c.p_id = d.id left join lkt_mch as e on d.mch_id = e.id where $condition ) as tt where tt.top<2 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = count($r0);
        }

        $sql1 = "select tt.* from (select a.*,b.kuaidi_name,e.name as shop_name,o.name,o.mobile,o.sheng,o.shi,o.xian,o.address,e.realname as send_name,e.tel as send_tel,e.sheng as send_sheng,e.shi as send_shi,e.xian as send_xian,e.address as send_address,row_number () over (PARTITION BY a.courier_num) AS top from lkt_express_delivery as a right join lkt_order as o on a.sNo = o.sNo left join lkt_express as b on a.express_id = b.id left join lkt_order_details as c on a.order_details_id = c.id left join lkt_product_list as d on c.p_id = d.id left join lkt_mch as e on d.mch_id = e.id where $condition ) as tt where tt.top<2 order by tt.deliver_time desc  limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $list = $r1;
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 获取商品
    public static function getPro($array)
    {
        $store_id = $array['store_id'];
        $id = $array['id'];
        $name = $array['name'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $pagesize = $pagesize ? $pagesize : 10;

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $list = array();
        $total = 0;

        $sql = "select express_id,courier_num from lkt_express_delivery where store_id = '$store_id' and subtable_id = 0 and id = '$id' ";
        $r = Db::query($sql);
        if($r)
        {
            $express_id = $r[0]['express_id'];
            $courier_num = $r[0]['courier_num'];
        }
        else
        {
            $message = Lang('Parameter error');
            echo json_encode(array("code" => 109,'message' => $message));
            exit;
        }

        $condition = " a.store_id = '$store_id' and a.subtable_id = 0 and a.express_id = '$express_id' and a.courier_num = '$courier_num' ";
        if($name != '')
        {
            $name = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and c.p_name like $name ";
        }

        $sql0 = "select a.id from lkt_express_delivery as a right join lkt_order_details as c on a.order_details_id = c.id left join lkt_product_list as d on c.p_id = d.id left join lkt_configure as e on c.sid = e.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = count($r0);
        }

        $sql1 = "select c.p_id,c.p_name,c.p_price,c.size as attribute,a.num,e.img from lkt_express_delivery as a right join lkt_order_details as c on a.order_details_id = c.id left join lkt_product_list as d on c.p_id = d.id left join lkt_configure as e on c.sid = e.id where $condition order by a.deliver_time desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k1 => $v1)
            {
                $v1['img'] = ServerPath::getimgpath($v1['img'], $store_id);
                $list[] = $v1;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 管理端发货-面单发货
    public static function FaceSheetSend($array)
    {
        $store_id = $array['store_id'];
        $store_type = $array['store_type'];
        $admin_name = $array['admin_name'];
        $id = $array['id'];
        $express_id = $array['express_id'];
        $source = $array['source'];
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($data['operator']))
        {
            $operator = $data['operator'];
        }

        $Jurisdiction = new Jurisdiction();
        $lktlog = new LaiKeLogUtils();
        $time = date('Y-m-d H:i:s', time());

        Db::startTrans();

        $date_list = json_decode($id,true);
        $status_num = 0; // 未发货订单详情数量
        if($date_list != array())
        {
            $cargo = '';
            $d_id0 = $date_list[0]['detailId']; // 订单详情ID
            $sql0 = "select r_sNo,p_name from lkt_order_details where id = '$d_id0' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $sNo = $r0[0]['r_sNo'];
                $cargo = $r0[0]['p_name'];
                // 根据订单号，查询还未发货的订单详情数量
                $sql1 = "select id from lkt_order_details where r_sNo = '$sNo' and r_status = 1 ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    $status_num = count($r1); // 未发货订单详情数量
                }
            }

            if(count($date_list) > 1)
            {
                $cargo .= '等';
            }

            $array = array('store_id'=>$store_id,'sNo'=>$sNo,'express_id'=>$express_id,'cargo'=>$cargo,'source'=>$source);
            $data = self::Delivery_by_waybill($array);
            if(isset($data['code']))
            { // 面单发货失败
                Db::rollback();
                echo json_encode(array('code'=>109,'message'=>$data['message']));
                exit;
            }
            $courier_num = $data['courier_num']; // 快递单号
            $childNum = $data['childNum']; // 子单号
            $returnNum = $data['returnNum']; // 回单号
            $label = $data['label']; // 面单短链
            $kdComOrderNum = $data['kdComOrderNum']; // 快递公司订单号
            $subtable_id = $data['subtable_id']; // 快递公司订单号
            $mch_id = $data['mch_id']; // 店铺ID
            
            if($source == 1)
            {
                $shop_id = 0;
            }
            else
            {
                $shop_id = $mch_id;
            }

            foreach($date_list as $k_list => $v_list)
            {
                $d_id = $v_list['detailId']; // 订单详情ID
                $d_num = $v_list['num']; // 发货数量
                $sql_p = "select o.id,o.user_id,o.sNo,d.p_name,d.p_id,d.sid,d.num,o.name,o.address,d.express_id,d.courier_num,d.deliver_num from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.id = '$d_id' ";
                $res_p = Db::query($sql_p);
                if($res_p)
                {
                    $num = $res_p[0]['num'];
                    $user_id = $res_p[0]['user_id'];
                    $deliver_num = $res_p[0]['deliver_num']; // 已发数量

                    $express_id_0 = '';
                    if($res_p[0]['express_id'] == '')
                    {
                        $express_id_0 = $express_id;
                    }
                    else
                    {
                        $express_id_0 = $res_p[0]['express_id'] . ',' . $express_id;
                    }
                    $courier_num_0 = '';
                    if($res_p[0]['courier_num'] == '')
                    {
                        $courier_num_0 = $courier_num;
                    }
                    else
                    {
                        $courier_num_0 = $res_p[0]['courier_num'] . ',' . $courier_num;
                    }

                    if($d_num == ($num - $deliver_num))
                    { // 发货数量 = 购买数量
                        $con = " r_status = 2,express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' ";
                        $status_num--;
                    }
                    else
                    {
                        $con = " express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' ";
                    }

                    $sqld = "update lkt_order_details set $con where id = '$d_id' and r_status = '1'";
                    $rd = Db::execute($sqld);
                    if ($rd < 1)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, ' 面单发货,修改订单详情失败，ID为 ' . $d_id,2,$source,$shop_id,$operator_id);
                        $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单详情状态失败！sql:" . $sqld);
                        Db::rollback();
                        $message = Lang('admin_order.0');
                        echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                        exit;
                    }

                    $sql3 = "insert into lkt_express_delivery(store_id,sNo,order_details_id,express_id,courier_num,num,deliver_time,childNum,returnNum,label,kdComOrderNum,is_status,subtable_id) value ('$store_id','$sNo','$d_id','$express_id','$courier_num','$d_num','$time','$childNum','$returnNum','$label','$kdComOrderNum',0,'$subtable_id')";
                    $r3 = Db::execute($sql3);
                    if ($rd < 1)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, ' 面单发货,添加快递记录表失败',2,$source,$shop_id,$operator_id);
                        $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加快递记录表失败！sql:" . $sql3);
                        Db::rollback();
                        $message = Lang('admin_order.0');
                        echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                        exit;
                    }
                }
            }

            if($status_num == 0)
            { // 未发货订单详情数量 为 0 （该订单全部发货）
                $sql = OrderModel::where('sNo',$sNo)->find();
                $sql->status = 2;
                $rl = $sql->save();
                if (!$rl)
                {
                    $Jurisdiction->admin_record($store_id, $operator, ' 面单发货,修改订单状态失败，订单号为' . $sNo,2,$source,$shop_id,$operator_id);
                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "发货失败！sNo:" . $sNo);
                    Db::rollback();
                    $message = Lang('admin_order.1');
                    echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                    exit;
                }
                else
                {
                    $msg_title = "【" . $sNo . "】订单发货啦！";
                    $msg_content = Lang("admin_order.2");
                    $Jurisdiction->admin_record($store_id, $operator, ' 将订单号为 ' . $sNo . ' 的订单发货 ',3,$source,$shop_id,$operator_id);
                    LaiKeLogUtils::lktLog($msg_title);
                    /**发货成功通知*/
                    $pusher = new LaikePushTools();
                    $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, $admin_name);

                    $msgsql = "select o.id,o.user_id,o.sNo,d.p_name,o.name,o.address from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.r_sNo ='$sNo'";
                    $msgres = Db::query($msgsql);
                    $msgres = (object)$msgres[0];
                    $openid = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('wx_id')->select()->toArray();
                    if(!empty($openid))
                    {
                        $msgres->uid = $openid[0]['wx_id'];
                        $compres = ExpressModel::where('id',$express_id)->field('kuaidi_name')->select()->toArray();
                        if (!empty($compres))
                        {
                            $msgres->company = $compres[0]['kuaidi_name'];
                        }

                        if($msgres->uid)
                        {
                            self::sendWXTopicMsg($msgres, $sNo, $courier_num, $store_id);
                        }
                    }
                }
            }
        }

        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message));
        exit;
    }

    // 面单发货
    public static function Delivery_by_waybill($array)
    {
        $lktlog = new LaiKeLogUtils();
        $store_id = $array['store_id'];
        $sNo = $array['sNo'];
        $express_id = $array['express_id'];
        $cargo = $array['cargo'];
        $source = $array['source'];
       
        $express_key = ''; // 客户授权key
        $express_secret = ''; // 授权secret
        $express_tempId = ''; // 主单模板
        $sql_config = "select express_key,express_secret,express_tempId from lkt_config where store_id = '$store_id' ";
        $r_config = Db::query($sql_config);
        if($r_config)
        {
            $express_key = $r_config[0]['express_key']; // 客户授权key
            $express_secret = $r_config[0]['express_secret']; // 授权secret
            $express_tempId = $r_config[0]['express_tempId']; // 授权secret
        }
        list($msec, $sec) = explode(' ', microtime());
        $t = (float)sprintf('%.0f', (floatval($msec) + floatval($sec)) * 1000);    // 当前时间戳

        $sql_1 = "select mch_id,name,mobile,sheng,shi,xian,address from lkt_order where store_id = '$store_id' and sNo = '$sNo' ";
        $r_1 = Db::query($sql_1);
        if($r_1)
        {
            $mch_id = trim($r_1[0]['mch_id'],',');
            $name = $r_1[0]['name'];
            $mobile = $r_1[0]['mobile'];
            $sheng = $r_1[0]['sheng'];
            $shi = $r_1[0]['shi'];
            $xian = $r_1[0]['xian'];
            $address = $r_1[0]['address'];
        }

        // 获取店铺物流信息
        $sql_0 = "select a.*,b.kuaidi_name,b.type from lkt_express_subtable as a left join lkt_express as b on a.express_id = b.id where a.express_id = '$express_id' and a.mch_id = '$mch_id' and a.recovery = 0 ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $mch_id = $r_0[0]['mch_id'];
            $subtable_id = $r_0[0]['id'];
            $partnerId = $r_0[0]['partnerId'];
            $partnerKey = $r_0[0]['partnerKey'];
            $partnerSecret = $r_0[0]['partnerSecret'];
            $partnerName = $r_0[0]['partnerName'];
            $net = $r_0[0]['net'];
            $code = $r_0[0]['code'];
            $checkMan = $r_0[0]['checkMan'];
            $kuaidicom = $r_0[0]['type'];
        }

        $sql_2 = "select realname,tel,sheng,shi,xian,address from lkt_mch where store_id = '$store_id' and id = '$mch_id' ";
        $r_2 = Db::query($sql_2);
        if($r_2)
        {
            $sender_name = $r_2[0]['realname'];
            $sender_mobile = $r_2[0]['tel'];
            $sender_sheng = $r_2[0]['sheng'];
            $sender_shi = $r_2[0]['shi'];
            $sender_xian = $r_2[0]['xian'];
            $sender_address = $r_2[0]['address'];
        }

        $param = array (
            'printType' => 'NON',              // 打印类型，NON:只下单不打印（默认）；IMAGE:生成图片短链；HTML:生成html短链；CLOUD:使用快递100云打印机打印
            'partnerId' => $partnerId,                 // 电子面单客户账户或月结账号
            'partnerKey' => $partnerKey,                // 电子面单密码
            'partnerSecret' => $partnerSecret,             // 电子面单密钥
            'partnerName' => $partnerName,               // 电子面单客户账户名称
            'net' => $net,                       // 收件网点名称,由快递公司当地网点分配
            'code' => $code,                      // 电子面单承载编号
            'checkMan' => $checkMan,                  // 电子面单承载快递员名
            'tbNet' => '',                     // 在使用菜鸟/淘宝/拼多多授权电子面单时，若月结账号下存在多个网点，则tbNet="网点名称,网点编号" ，注意此处为英文逗号
            'kuaidicom' => $kuaidicom,                 // 快递公司的编码：https://api.kuaidi100.com/document/5f0ff6e82977d50a94e10237.html
            'recMan' => array (
                'name' => $name,                  // 收件人姓名
                'mobile' => $mobile,                // 收件人的手机号，手机号和电话号二者其一必填
                'tel' => '',                   // 收件人的电话号，手机号和电话号二者其一必填
                'printAddr' => $sheng . $shi . $xian . $address,             // 收件人地址
                'company' => ''                // 收件人公司名
            ),
            'sendMan' => array (
                'name' => $sender_name,                  // 寄件人姓名
                'mobile' => $sender_mobile,                // 寄件人的手机号，手机号和电话号二者其一必填
                'tel' => '',                   // 寄件人的电话号，手机号和电话号二者其一必填
                'printAddr' => $sender_sheng . $sender_shi . $sender_xian . $sender_address,             // 寄件人地址
                'company' => ''                // 寄件人公司名
            ),
            'cargo' => $cargo,                 // 物品名称
            'count' => '1',                    // 物品总数量
            'weight' => '',                 // 物品总重量KG
            'payType' => 'SHIPPER',            // 支付方式
            'expType' => '标准快递',           // 快递类型: 标准快递（默认）、顺丰特惠、EMS经济
            'remark' => '',                // 备注
            'siid' => '',                      // 设备编码
            'direction' => '0',                // 打印方向，0：正方向（默认）； 1：反方向；只有printType为CLOUD时该参数生效
            'tempId' => $express_tempId,                    // 主单模板：快递公司模板V2链接：https://api.kuaidi100.com/manager/v2/shipping-label/template-shipping-label
            'childTempId' => '',               // 子单模板：快递公司模板V2链接：https://api.kuaidi100.com/manager/v2/shipping-label/template-shipping-label
            'backTempId' => '',                // 回单模板：快递公司模板V2链接：https://api.kuaidi100.com/manager/v2/shipping-label/template-shipping-label
            'valinsPay' => '',                 // 保价额度
            'collection' => '',                // 代收货款额度
            'needChild' => '1',                // 是否需要子单
            'needBack' => '1',                 // 是否需要回单
            'orderId' => null,                 // 贵司内部自定义的订单编号,需要保证唯一性
            'callBackUrl' => null,             // 打印状态回调地址，默认仅支持http
            'salt' => '',                      // 签名用随机字符串
            'needSubscribe' => false,          // 是否开启订阅功能 false：不开启(默认)；true：开启
            'pollCallBackUrl' => null,         // 如果needSubscribe 设置为true时，pollCallBackUrl必须填入，用于跟踪回调
            'resultv2' => '0',                 // 添加此字段表示开通行政区域解析或地图轨迹功能
            'needDesensitization' => false,    // 是否脱敏 false：关闭（默认）；true：开启
            'needLogo' => false,               // 面单是否需要logo false：关闭（默认）；true：开启
            'thirdOrderId' => null,            // 平台导入返回的订单id：如平台类加密订单，使用此下单为必填
            'oaid' => null,                    // 淘宝订单收件人ID (Open Addressee ID)，长度不超过128个字符，淘宝订单加密情况用于解密
            'thirdTemplateURL' => null,        // 第三方平台面单基础模板链接，如为第三方平台导入订单选填，如不填写，默认返回两联面单模板
            'thirdCustomTemplateUrl' => null,  // 第三方平台自定义区域模板地址
            'customParam' => null,             // 面单自定义参数
            'needOcr' => false,                // 第三方平台订单是否需要开启ocr，开启后将会通过推送方式推送 false：关闭（默认）；true：开启
            'ocrInclude' => null,              // orc需要检测识别的面单元素
            'height' => null,                  // 打印纸的高度，以mm为单位
            'width' => null                    // 打印纸的宽度，以mm为单位
        );

        // 请求参数
        $post_data = array();
        $post_data['param'] = json_encode($param, JSON_UNESCAPED_UNICODE);
        $post_data['key'] = $express_key;
        $post_data['t'] = $t;
        $sign = md5($post_data['param'].$t.$express_key.$express_secret);
        $post_data['sign'] = strtoupper($sign);
        
        $url = 'https://api.kuaidi100.com/label/order?method=order';    // 电子面单下单接口请求地址
        $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "传参:" . json_encode($post_data));

        //发送post请求
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_HEADER, 0);
        curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($post_data));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        $result = curl_exec($ch);
        $data = json_decode($result, true);

        $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "返回数据:" . json_encode($data));
        if($data['code'] == 200)
        {
            $courier_num = $data['kuaidinum']; // 快递单号
            $childNum = $data['childNum']; // 子单号
            $returnNum = $data['returnNum']; // 回单号
            $label = $data['label']; // 面单短链
            $kdComOrderNum = $data['kdComOrderNum']; // 快递公司订单号
            $res = array('courier_num'=>$courier_num,'childNum'=>$childNum,'returnNum'=>$returnNum,'label'=>$label,'kdComOrderNum'=>$kdComOrderNum,'subtable_id'=>$subtable_id,'mch_id'=>$mch_id);
            return $res;
        }
        else
        {
            $res = array('code'=>$data['code'],'message'=>$data['message']);
            return $res;
        }
    }

    // 取消电子面单
    public static function CancelElectronicWaybill($array)
    {
        $store_id = $array['store_id'];
        $store_type = $array['store_type'];
        $admin_name = $array['admin_name'];
        $id = $array['id'];
        $source = $array['source'];
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($data['operator']))
        {
            $operator = $data['operator'];
        }
        
        $Jurisdiction = new Jurisdiction();
        $lktlog = new LaiKeLogUtils();
        $time = date('Y-m-d H:i:s', time());

        Db::startTrans();

        $sql0 = "select * from lkt_express_delivery where store_id = '$store_id' and id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $express_id = $r0[0]['express_id']; // 快递公司ID
            $courier_num = $r0[0]['courier_num']; // 快递单号
            $kdComOrderNum = $r0[0]['kdComOrderNum']; // 快递公司订单号
            $subtable_id = $r0[0]['subtable_id']; // 
            $sNo = $r0[0]['sNo']; // 订单号
        }

        $express_key = ''; // 客户授权key
        $express_secret = ''; // 授权secret
        $express_tempId = ''; // 主单模板
        $sql_config = "select express_key,express_secret,express_tempId from lkt_config where store_id = '$store_id' ";
        $r_config = Db::query($sql_config);
        if($r_config)
        {
            $express_key = $r_config[0]['express_key']; // 客户授权key
            $express_secret = $r_config[0]['express_secret']; // 授权secret
            $express_tempId = $r_config[0]['express_tempId']; // 授权secret
        }

        // 获取店铺物流信息
        $sql_0 = "select a.*,b.kuaidi_name,b.type from lkt_express_subtable as a left join lkt_express as b on a.express_id = b.id where a.express_id = '$express_id' and a.id = '$subtable_id'";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $mch_id = $r_0[0]['mch_id'];
            $partnerId = $r_0[0]['partnerId'];
            $partnerKey = $r_0[0]['partnerKey'];
            $partnerSecret = $r_0[0]['partnerSecret'];
            $partnerName = $r_0[0]['partnerName'];
            $net = $r_0[0]['net'];
            $code = $r_0[0]['code'];
            $checkMan = $r_0[0]['checkMan'];
            $kuaidicom = $r_0[0]['type'];
        }
        else
        {
            $message = Lang("order.35");
            echo json_encode(array('code'=>109,'message'=>$message));
            exit;
        }

        if($source == 1)
        {
            $shop_id = 0;
        }
        else
        {
            $shop_id = $mch_id;
        }

        $sql1 = "select * from lkt_express_delivery where store_id = '$store_id' and sNo = '$sNo' and express_id = '$express_id' and courier_num = '$courier_num' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k1 => $v1)
            {
                $order_details_id = $v1['order_details_id']; // 订单详情ID
                $num = $v1['num']; // 发货数量

                $sql2 = "select r_status,num,express_id,courier_num,deliver_num from lkt_order_details where id = '$order_details_id' ";
                $r2 = Db::query($sql2);
                $status1 = $r2[0]['r_status'];
                $num1 = $r2[0]['num'];
                $express_id1 = explode(',',$r2[0]['express_id']);
                $courier_num1 = explode(',',$r2[0]['courier_num']);
                $deliver_num1 = $r2[0]['deliver_num'];

                foreach($express_id1 as $k_1 => $v_1)
                {
                    if($v_1 == $express_id)
                    {
                        unset($express_id1[$k_1]);
                    }
                }
                $express_id2 = implode(',',$express_id1);

                foreach($courier_num1 as $k_2 => $v_2)
                {
                    if($v_2 == $courier_num)
                    {
                        unset($courier_num1[$k_2]);
                    }
                }
                $courier_num2 = implode(',',$courier_num1);

                $deliver_num2 = $deliver_num1 - $num;

                $sql3 = "update lkt_order_details set r_status = 1,express_id = '$express_id2',courier_num = '$courier_num2',deliver_num = '$deliver_num2' where id = '$order_details_id' ";
                $r3 = Db::execute($sql3);
                if($r3 < 1)
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, ' 取消面单,修改订单详情失败，ID为 ' . $order_details_id,3,$source,$shop_id,$operator_id);
                    $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "取消面单,修改订单详情失败，sql：" . $sql3);
                    $message = Lang('operation failed');
                    echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                    exit;
                }
            }
        }

        // 参数设置
        list($msec, $sec) = explode(' ', microtime());
        $t = (float)sprintf('%.0f', (floatval($msec) + floatval($sec)) * 1000);    // 当前时间戳
        $param = array (
            'partnerId' => $partnerId,                 // 电子面单客户账户或月结账号
            'partnerKey' => $partnerKey,                // 电子面单密码
            'partnerSecret' => $partnerSecret,             // 电子面单密钥
            'partnerName' => $partnerName,               // 电子面单客户账户名称
            'net' => $net,                       // 收件网点名称,由快递公司当地网点分配
            'code' => $code,                      // 电子面单承载编号
            'kuaidicom' => $kuaidicom,                 // 快递公司的编码：https://api.kuaidi100.com/document/5f0ff6e82977d50a94e10237.html
            'kuaidinum' => $courier_num,                 // 快递单号
            'orderId' => $kdComOrderNum,                   // 快递公司订单号，对应下单时返回的kdComOrderNum，如果下单时有返回该字段，则取消时必填，否则可以不填
            'reason' => ''                     // 取消原因
        );
        
        // 请求参数
        $post_data = array();
        $post_data['param'] = json_encode($param, JSON_UNESCAPED_UNICODE);
        $post_data['key'] = $express_key;
        $post_data['t'] = $t;
        $sign = md5($post_data['param'].$t.$express_key.$express_secret);
        $post_data['sign'] = strtoupper($sign);
        
        $url = 'https://poll.kuaidi100.com/eorderapi.do?method=cancel';    // 电子面单取消请求地址
        $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . ' 取消面单传参： ' . json_encode($post_data));
        
        // 发送post请求
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_HEADER, 0);
        curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($post_data));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        $result = curl_exec($ch);
        $data = json_decode($result, true);
        $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . ' 取消面单返回数据： ' . json_encode($data));

        if($data['returnCode'] == 200)
        {
            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator, ' 取消面单成功，ID为 ' . $id,2,$source,$shop_id,$operator_id);
            $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . ' 取消面单成功，ID为 ' . $id);
            $message = Lang("Success");
            echo json_encode(array('code'=>200,'message'=>$message));
            exit;
        }
        else
        {
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, ' 取消面单失败，ID为 ' . $id,2,$source,$shop_id,$operator_id);
            $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . ' 取消面单失败，ID为 ' . $id);
            echo json_encode(array('code'=>$data['returnCode'],'message'=>$data['message']));
            exit;
        }
    }

    /**
     * 发送微信订阅消息
     * @param $msgres
     * @param $oid
     * @param $courier_num
     * @param $store_id
     * @throws Exception
     */
    public static function sendWXTopicMsg($msgres, $oid, $courier_num, $store_id)
    {
        try
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '推送微信小程序订阅消息开始 ';

            $deliveryOrderData = array();
            //用户
            $deliveryOrderData['touser'] = $msgres->uid;
            //跳转地址
            $deliveryOrderData['page'] = 'pages/order/myOrder';
            //温馨提示
            $deliveryOrderData['thing6'] = '订单发货啦！';
            //订单号
            $deliveryOrderData['character_string1'] = $oid;
            //商品名称
            $deliveryOrderData['thing2'] = $msgres->p_name;
            //快递类型
            $deliveryOrderData['phrase3'] = $msgres->company;
            //快递单号
            $deliveryOrderData['character_string4'] = $courier_num;
            //商城ID
            $deliveryOrderData['store_id'] = $store_id;
            WXTopicMsgUtils::deliveryOrder($deliveryOrderData);
            $Log_content = __METHOD__ . '->' . __LINE__ . '推送微信小程序订阅消息结束 ';
        }
        catch (Exception $e)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '推送微信小程序订阅消息失败 ';
        }
    }

    // 发货（新）
    // 秒杀：后台和PC店铺
    // 积分：后台和PC店铺
    // 竞拍：后台和PC店铺
    // 限时折扣：后台和PC店铺
    // 预售：后台和PC店铺
    public static function send_out_goods($action)
    {
        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();
        $store_id = $action['store_id'] ;
        $user_id = $action['user_id'] ;
        $order_details_id = $action['order_details_id'] ;
        $con = $action['con'] ;
        $sNo = $action['sNo'] ;
        $len = $action['len'] ;
        $express_id = $action['express_id'] ;
        $courier_num = $action['courier_num'] ;
        $time = date('Y-m-d H:i:s', time());

        $shop_id = '';
        if(isset($action['shop_id']))
        {
            $shop_id = $action['shop_id'];
        }
        $operator_id = 0;
        if(isset($action['operator_id']))
        {
            $operator_id = $action['operator_id'];
        }
        $operator = '';
        if(isset($action['operator']))
        {
            $operator = $action['operator'];
        }
        $source = 1;
        if(isset($action['source']))
        {
            $source = $action['source'];
        }

        Db::startTrans();
        
        $con['r_status'] = 2;
        $rd = Db::name('order_details')->where(['r_status'=>'1','r_sNo'=>$sNo])->whereIn('id',$order_details_id)->update($con);
        if ($rd < 0)
        {
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货失败,订单号为' . $sNo . '！';
            $lktlog->log("app/mch.log",$Log_content);
            $message = Lang('Busy network');
            echo json_encode(array('code' => 103, 'message' => $message));
            exit();
        }
        $batchSend = false;
        $count = OrderDetailsModel::where(['r_sNo'=>$sNo])->whereIn('r_status','1,2')->field('id')->select()->toArray();
        // 所有为已发货状态的订单详情跟所选订单一样多的情况下为批量发货
        if (count($count) == $len)
        { //批量发货
            $batchSend = true;
        }

        //查询订单信息
        $sql_p = "select o.id,o.user_id,o.sNo,d.p_name,d.p_id,d.sid,d.num,o.name,o.address,d.id as d_id from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo where o.store_id = '$store_id' and d.r_sNo = '$sNo' ";
        $res_p = Db::query($sql_p);

        $curSendPos = 0; //统计当前发货次数
        foreach ($res_p as $key => $value)
        {
            $d_id = $value['d_id'];
            $p_name = $value['p_name'];
            $user_id = $value['user_id'];
            $address = $value['address'];
            $name = $value['name'];
            $order_id = $value['id'];
            $oid = $value['sNo'];
            $p_id = $value['p_id'];
            $sid = $value['sid'];
            $num = $value['num'];

            $sql3 = "insert into lkt_express_delivery(store_id,sNo,order_details_id,express_id,courier_num,num,deliver_time) value ('$store_id','$sNo','$d_id','$express_id','$courier_num','$num','$time')";
            $r3 = Db::execute($sql3);
            if ($rd < 1)
            {
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加快递记录表失败！sql:" . $sql3);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '将订单号为' . $sNo . '，进行了发货失败',2,$source,$shop_id,$operator_id);
                $message = Lang('admin_order.0');
                echo json_encode(array('code'=>ERROR_CODE_FHSB,'message'=>$message));
                exit;
            }

            $upod0 = OrderDetailsModel::where(['r_sNo'=>$oid,'r_status'=>1])->field('id')->select()->toArray();
            $upod = count($upod0);
            $curSendPos = $curSendPos + 1;
            $sendFinish = (!$upod && !$batchSend) || ($batchSend && $curSendPos == $len);
            if ($sendFinish)
            {
                $rl = Db::name('order')->where(['sNo'=>$oid])->update(['status'=>2]);
                if ($rl < 0)
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '将订单号为' . $sNo . '，进行了发货失败',2,$source,$shop_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货失败,订单号为' . $oid . '！';
                    $lktlog->log("app/mch.log",$Log_content);
                    $message = Lang('Busy network');
                    echo json_encode(array('code' => 103, 'message' => $message));
                    exit();
                }
                else
                {
                    $msg_title = "【" . $oid . "】订单发货啦！";
                    $msg_content = Lang("admin_order.2");

                    /**发货成功通知*/
                    $pusher = new LaikePushTools();
                    $pusher->pushMessage($user_id,$msg_title, $msg_content, $store_id, $user_id);

                    $msgsql = "select o.id,o.user_id,o.sNo,d.p_name,o.name,o.address from lkt_order as o left join lkt_order_details as d on o.sNo=d.r_sNo where o.store_id = '$store_id' and d.r_sNo ='$oid'";
                    $msgres = Db::query($msgsql);
                    $msgres = (object)$msgres[0];

                    $openid = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('wx_id')->select()->toArray();
                    if(!empty($openid))
                    {
                        $msgres->uid = $openid[0]['wx_id'];
                        $compres = ExpressModel::where(['id'=>$express_id])->field('kuaidi_name')->select()->toArray();
                        if (!empty($compres))
                        {
                            $msgres->company = $compres[0]['kuaidi_name'];
                        }
                    }
                }
            }
        }
        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货成功,订单号为' . $sNo . '！';
        $lktlog->log("app/mch.log",$Log_content);

        Db::commit();
        $Jurisdiction->admin_record($store_id, $operator, '将订单号为' . $sNo . '，进行了发货',2,$source,$shop_id,$operator_id);

        $message = Lang("Success");
        echo json_encode(array('code' => 200, 'message' => $message));
        exit();
    }

    // 验证提货码-获取商品信息
    public static function Self_pickup_order_to_obtain_goods($array)
    {
        $lktlog = new LaiKeLogUtils();

        $store_id = $array['store_id']; // 商城ID
        $store_type = $array['store_type']; // 来源 1.微信小程序 2.H5 3.支付宝小程序 4.字节跳动小程序 5.百度小程序 6.pc商城 7.pc店铺 8.pc管理后台 9.PC门店核销 10.H5门店核销 11.app 12.供应商
        $id = $array['id']; // 订单ID
        $extraction_code_1 = explode(',',$array['extraction_code']); // 二维码
        $extraction_code = $extraction_code_1[0];
        $shop_id = $array['shop_id']; // 店铺ID
        $mch_store_id = $array['mch_store_id']; // 门店ID

        $time = date("Y-m-d H:i:s");
        
        $show_write_time = 0; // 是否显示预约信息 0.不显示 1.显示
        $appointment = array();
        $data = array();
        $orderInfo = array();
        $por_list = array();
        $str = " a.user_id,a.sNo,a.z_price,a.old_total,a.status,a.mch_id,a.extraction_code,a.extraction_code_img,a.num,a.name,a.mobile,a.otype,b.id,b.p_id,b.sid,b.size,b.p_price,b.num as bnum,b.write_off_num,a.self_lifting,a.single_store ";
        if($store_type == 1 || $store_type == 2 || $store_type == 3 || $store_type == 4 || $store_type == 5 || $store_type == 6 || $store_type == 11 )
        { // 微信小程序 或 H5 或 支付宝小程序 或 字节跳动小程序 或 百度小程序 或 pc商城 或 APP
            if($shop_id == 0 || $shop_id == '')
            { // 用户
                $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.id = '$id' ";
            }
            else
            { // 店铺
                if ($id)
                {
                    $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.id = '$id' and a.mch_id like '%,$shop_id,%' and a.extraction_code like '%$extraction_code%'";
                }
                else
                {
                    $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and a.extraction_code like '%$extraction_code%'";
                }
            }
        }
        else if($store_type == 7)
        { // PC店铺
            $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.id = '$id' ";
        }
        else if($store_type == 8)
        { // pc管理后台
            $extraction_code_str = $extraction_code . ',';
            $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and a.id = '$id' and binary a.extraction_code like '$extraction_code_str%' ";

            // if($mch_store_id != '')
            // {
            //     $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and a.single_store = '$mch_store_id' and a.id = '$id' and binary a.extraction_code like '$extraction_code_str%' ";
            // }
            // else
            // {
            //     $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and a.id = '$id' and binary a.extraction_code like '$extraction_code_str%' ";
            // }
        }
        else if($store_type == 9)
        { // PC门店核销
            $extraction_code_str = $extraction_code . ',';
            $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and a.single_store = '$mch_store_id' and a.id = '$id' and binary a.extraction_code like '$extraction_code_str%' ";
        }
        else if($store_type == 10)
        { // H5门店核销
            $extraction_code_str = $extraction_code . ',';
            if ($id)
            {
                $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and (a.single_store = '$mch_store_id' or a.single_store = '0') and a.id = '$id' and binary a.extraction_code like '$extraction_code_str%'";
            }
            else
            {
                $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and (a.single_store = '$mch_store_id' or a.single_store = '0') and binary a.extraction_code like '$extraction_code_str%'";
            }
        }
        $r1 = Db::query($sql1);
        if ($r1)
        {
            $user_id = $r1[0]['user_id'];
            $sNo = $r1[0]['sNo'];
            $z_price = $r1[0]['z_price'];
            $status = $r1[0]['status'];
            $num = $r1[0]['num'];
            $name = $r1[0]['name'];
            $mobile = $r1[0]['mobile'];
            $rew = explode(',', $r1[0]['extraction_code']);
            $self_lifting = $r1[0]['self_lifting']; // 自提 0.配送 1.自提 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
            $single_store = $r1[0]['single_store']; // 下单门店
            if ($self_lifting == '3')
            {
                if($single_store != 0)
                {
                    $show_write_time = 1;
                    $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'id'=>$single_store])->select()->toArray();
                    if($r0_0)
                    {
                        $sql_order_details = "select write_time from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' ";
                        $r_order_details = Db::query($sql_order_details);
                        $write_time = $r_order_details[0]['write_time'];
                        $appointment = array('mch_store'=>$r0_0[0]['name'],'time'=>$write_time);
                    }
                }
                else
                {
                    $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'id'=>$mch_store_id])->select()->toArray();
                    if($r0_0)
                    {
                        $sql_order_details = "select write_time from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' ";
                        $r_order_details = Db::query($sql_order_details);
                        $write_time = $r_order_details[0]['write_time'];
                        $appointment = array('mch_store'=>$r0_0[0]['name'],'time'=>$write_time);
                    }
                }
            }

            $sql_u = "select user_name from lkt_user where user_id = '$user_id' ";
            $r_u = Db::query($sql_u);
            if($r_u)
            {
                $name = $r_u[0]['user_name'];
            }

            $otype = $r1[0]['otype']; // 订单状态
            if($r1[0]['old_total'] == '0.00' || $r1[0]['old_total'] == '')
            {
                $r1[0]['old_total'] = $r1[0]['z_price'];
            }

            foreach ($r1 as $k => $v)
            {
                $p_id = $v['p_id']; // 商品ID
                $sid = $v['sid']; // 属性ID

                if($otype == 'MS')
                {
                    $sql_s = "select goodsId from lkt_seconds_activity where id = '$p_id' ";
                    $r_s = Db::query($sql_s);
                    $p_id = $r_s[0]['goodsId']; // 商品ID
                }
            
                // 根据产品id,查询产品列表 (产品图片)
                $sql2 = "select a.product_title,c.img from lkt_product_list AS a LEFT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.id = '$p_id' AND c.id= '$sid' ";
                $r2 = Db::query($sql2);
                $product_title = $r2[0]['product_title']; // 商品名称
                $img = ServerPath::getimgpath($r2[0]['img']); // 拼图片路径
                $por_list[$k]['dId'] = $v['id'];
                $por_list[$k]['p_id'] = $v['p_id'];
                $por_list[$k]['p_price'] = $v['p_price'];
                $por_list[$k]['num'] = $v['bnum'];
                $por_list[$k]['sid'] = $v['sid'];
                $por_list[$k]['size'] = $v['size'];
                $por_list[$k]['write_off_num'] = $v['write_off_num'];
                $por_list[$k]['product_title'] = $product_title;
                $por_list[$k]['img'] = $img;
                
                $por_list[$k]['status'] = 1; // 1.待使用 2.已用完
                if($v['write_off_num'] == 0)
                {
                    $por_list[$k]['status'] = 2; // 1.待使用 2.已用完
                }
                
                $is_write = 1; // 是否核销过 1.没核销过的 2.核销过
                $sql2_0 = "select * from lkt_write_record where s_no = '$sNo' ";
                $r2_0 = Db::query($sql2_0);
                if($r2_0)
                {
                    $is_write = 2;
                }
                $por_list[$k]['is_write'] = $is_write;
            }

            // if($shop_id != 0 && $shop_id != '')
            // {
            //     if ($status == 2)
            //     {
            //         if ($rew[2] <= time())
            //         { // 提货码有效时间 小于等于 当前时间
            //             $message = Lang('mch.21');
            //             echo json_encode(array('code' => 103, 'message' => $message));
            //             exit();
            //         }
            //         else
            //         {
            //             if ($rew[0] != $extraction_code)
            //             {
            //                 $message = Lang('mch.94');
            //                 echo json_encode(array('code' => 103, 'message' => $message));
            //                 exit();
            //             }
            //         }
            //     }
            //     else
            //     {
            //         $message = Lang('mch.21');
            //         echo json_encode(array('code' => 103, 'message' => $message));
            //         exit();
            //     }
            // }

            if($store_type == 1 || $store_type == 2 || $store_type == 3 || $store_type == 4 || $store_type == 5 || $store_type == 6 || $store_type == 11 )
            { // 微信小程序 或 H5 或 支付宝小程序 或 字节跳动小程序 或 百度小程序 或 pc商城 或 APP
                if($shop_id == 0 || $shop_id == '')
                { // 用户
                    $rew = explode(',', $r1[0]['extraction_code']); // 提现码
                    $data = array('status' => $status, 'extraction_code' => $rew[0], 'extraction_code_img' => $r1[0]['extraction_code_img'], 'por_list' => $por_list, 'z_price' => $z_price, 'sNo' => $sNo, 'num' => $num);
                }
                else
                { // 店铺
                    if($otype == 'VI')
                    {
                        $sql_o = "select a.*,b.id as dId,b.p_id,b.p_price,b.platform_coupon_price,b.store_coupon_price,b.after_write_off_num,b.write_off_num,p.write_off_settings,p.write_off_mch_ids,p.is_appointment from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo left join lkt_product_list as p on b.p_id = p.id where a.sNo = '$sNo' ";
                        $r01 = Db::query($sql_o);
                    }
                    else 
                    {
                        $r01 = Db::name('order')->where('sNo', $sNo)->select()->toArray();
                    }
                    if($r01)
                    {
                        $r01[0]['mch_store_id'] = $mch_store_id;
                    }
                    $data = array('orderInfo' =>$r01[0],'por_list'=>$por_list);
                }
            }
            else if($store_type == 7 || $store_type == 9)
            { // PC店铺 或 PC门店核销
                $orderInfo = array('sNo'=>$r1[0]['sNo'],'old_total'=>$r1[0]['old_total'],'name'=>$r1[0]['name'],'mobile'=>$r1[0]['mobile'],'num'=>$r1[0]['num']);
                $data = array('por_list'=>$por_list,'orderInfo'=>$orderInfo);
            }
            else if($store_type == 8)
            { // 管理后台
                $data = array('por_list'=>$por_list,'sNo'=>$r1[0]['sNo'],'p_price'=>$r1[0]['old_total'],'name'=>$r1[0]['name'],'mobile'=>$r1[0]['mobile'],'num'=>$r1[0]['num'],'mch_store_id'=>$mch_store_id);
            }
            else if($store_type == 10)
            { // H5门店核销
                if($appointment == array())
                {
                    $data = array('sNo'=>$sNo,'status'=>$status,'num'=>$num,'name'=>$name,'mobile'=>$mobile,'z_price'=>$z_price,'por_list'=>$por_list,'otype'=>$otype,'show_write_time'=>$show_write_time);
                }
                else
                {
                    $data = array('sNo'=>$sNo,'status'=>$status,'num'=>$num,'name'=>$name,'mobile'=>$mobile,'z_price'=>$z_price,'por_list'=>$por_list,'otype'=>$otype,'show_write_time'=>$show_write_time,'write_time_info'=>$appointment);
                }
            }

            $message = Lang('Success');
            return $data;
        }
        else
        {
            $message = Lang('tools.10');
            echo json_encode(array('code' => 103, 'message' => $message));
            exit();
        }
    }

    // 验证提货码
    public static function VerificationExtractionCode($array)
    {
        $Jurisdiction = new Jurisdiction();
        $store_id = $array['store_id'];
        $store_type = $array['store_type'];

        $id = $array['id'] ;
        $extraction_code_1 = explode(',',$array['extraction_code']); // 二维码
        $extraction_code = $extraction_code_1[0];
        $time = date('Y-m-d H:i:s', time());

        $shop_id = '';
        if(isset($array['shop_id']))
        {
            $shop_id = $array['shop_id'];
        }
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }

        $p_id = 0; // 商品ID
        if(isset($array['p_id']))
        {
            $p_id = $array['p_id'];
        }
        $c_id = 0;
        if(isset($array['c_id']))
        {
            $c_id = $array['c_id'];
        }
        $write_shop_id = 0; // 门店ID
        if(isset($array['write_shop_id']))
        {
            $write_shop_id = $array['write_shop_id'];
        }

        Db::startTrans();
        $time = date("Y-m-d H:i:s");
        $mch = new MchPublicMethod();
        $str = " a.user_id,a.sNo,a.z_price,a.allow,a.status,a.extraction_code,b.id as d_id,b.p_id,b.num,b.p_price,b.sid,a.otype,b.write_off_num,b.after_write_off_num ";
        if ($id)
        {
            if($source == 1)
            {
                $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.id = '$id' and a.extraction_code like '%$extraction_code%'";
            }
            else
            {
                $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.id = '$id' and a.mch_id like '%,$shop_id,%' and a.extraction_code like '%$extraction_code%'";
            }
        }
        else
        {
            if($source == 1)
            {
                $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.extraction_code like '%$extraction_code%'";
            }
            else
            {
                $sql1 = "select $str from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and a.extraction_code like '%$extraction_code%'";
            }
        }
        $r1 = Db::query($sql1);
        if ($r1)
        {
            $d_id = $r1[0]['d_id']; // 订单详情ID
            $user_id = $r1[0]['user_id'];
            $sNo = $r1[0]['sNo'];
            $allow = $r1[0]['allow'];
            $z_price = $r1[0]['z_price'];
            $status = $r1[0]['status'];
            $p_id = $r1[0]['p_id'];
            $num = $r1[0]['num'];
            $sid = $r1[0]['sid'];
            $otype = $r1[0]['otype'];
            $rew = explode(',', $r1[0]['extraction_code']);

            if($operator_id == 0)
            { // 操作人ID不存在
                $VerifiedBy_type = 1; // 核销人类型 1.店主核销 2.门店核销
                $VerifiedBy = 0; // 核销人ID
            }
            else
            {
                $VerifiedBy_type = 2; // 核销人类型 1.店主核销 2.门店核销
                $VerifiedBy = $operator_id; // 核销人ID
            }
            if($otype == 'VI')
            { // 虚拟订单
                $status = 3; // 核销状态 1.为已核销完 2.为退款状态中 3.为还有核销次数

                $write_off_num = $r1[0]['write_off_num']; // 虚拟商品待核销次数
                $after_write_off_num = $r1[0]['after_write_off_num']; // 虚拟商品已核销次数
                if($write_off_num < 1)
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '验证提货码时，验证码已失效！';
                    $mch->mchLog($Log_content);
                    $message = Lang('mch.21');
                    echo json_encode(array('code' => 103, 'message' => $message));
                    exit();
                }

                $z_write_off_num = $write_off_num + $after_write_off_num;
                $z_price1 = $z_price / $z_write_off_num;

                $update_details = " write_off_num = '$write_off_num' - 1,after_write_off_num = '$after_write_off_num' + 1";

                if($write_off_num == 1)
                {
                    $status = 1;
                    $update_details .= ",r_status = 5,arrive_time = '$time'";

                    $r2 = Db::name('order')->where('sNo', $sNo)->update(['status' => '5','arrive_time'=>$time,'VerifiedBy'=>$VerifiedBy,'VerifiedBy_type'=>$VerifiedBy_type]);
                    if ($r2 < 0)
                    {
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '核销了订单号：' . $sNo . '失败',2,$source,$shop_id,$operator_id);
                        $Log_content = __METHOD__ . '->' . __LINE__ . '自提订单号' . $sNo . '时,改变订单状态失败！';
                        $mch->mchLog($Log_content);
                        $message = Lang('Busy network');
                        echo json_encode(array('code' => 103, 'message' => $message));
                        exit();
                    }
                }

                if($operator_id != 0)
                {
                    $sql_0 = "insert into lkt_mch_admin_record(store_id,administrators_id,sNo,add_date) value ('$store_id','$operator_id','$sNo','$time')";
                    $r_0 = Db::execute($sql_0);
                }
                
                $sql3 = "update lkt_order_details set $update_details where store_id = '$store_id' and id = '$d_id'  ";
                $r3 = Db::execute($sql3);
                if ($r3 < 0)
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '核销了订单号：' . $sNo . '失败',2,$source,$shop_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '自提订单号' . $sNo . '时,改变订单详情状态失败！';
                    $mch->mchLog($Log_content);
                    $message = Lang('Busy network');
                    echo json_encode(array('code' => 103, 'message' => $message));
                    exit();
                }

                $sql4 = "insert into lkt_write_record(write_store_id,write_time,write_code,s_no,p_id,status) value ('$write_shop_id','$time','$extraction_code','$sNo','$d_id','$status')";
                $r4 = Db::execute($sql4);
                if ($r4 > 0)
                {
                    $mch->parameter($store_id, $sNo, $z_price1, $allow);

                    Db::commit();
                    $Jurisdiction->admin_record($store_id, $operator, '核销了订单号：' . $sNo,2,$source,$shop_id,$operator_id);
                    $data = array('sNo' => $sNo, 'p_price' => round($z_price,2));
                    $message = Lang('Success');
                    echo json_encode(array('code' => 200, 'message' => $message, 'data' => $data));
                    exit();
                }
                else
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '核销了订单号：' . $sNo . '失败',2,$source,$shop_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '自提订单号' . $sNo . '时,添加核销记录失败！';
                    $mch->mchLog($Log_content);
                    $message = Lang('Busy network');
                    echo json_encode(array('code' => 103, 'message' => $message));
                    exit();
                }
            }
            else
            {
                if($otype == 'MS')
                {
                    $sql_s = "select * from lkt_seconds_activity where store_id = '$store_id' and id = '$p_id' ";
                    $r_s = Db::query($sql_s);
                    $p_id = $r_s[0]['goodsId'];
                }
    
                $p_price = $r1[0]['p_price'];
                if ($status == 2)
                {
                    // if ($rew[2] <= time())
                    // { // 提货码有效时间 小于等于 当前时间
                    //     $Log_content = __METHOD__ . '->' . __LINE__ . '验证提货码时，验证码已失效！';
                    //     $mch->mchLog($Log_content);
                    //     $message = Lang('mch.21');
                    //     echo json_encode(array('code' => 103, 'message' => $message));
                    //     exit();
                    // }
                    // else
                    // {
                        if ($rew[0] != $extraction_code)
                        {
                            Db::rollback();
                            $Log_content = __METHOD__ . '->' . __LINE__ . '验证提货码时，验证码已失效！';
                            $mch->mchLog($Log_content);
                            $message = Lang('mch.21');
                            echo json_encode(array('code' => 103, 'message' => $message));
                            exit();
                        }
                    // }
                    $r2 = Db::name('order')->where('sNo', $sNo)->update(['status' => '5','arrive_time'=>$time,'VerifiedBy'=>$VerifiedBy,'VerifiedBy_type'=>$VerifiedBy_type]);
                    if ($r2 < 0)
                    {
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '核销了订单号：' . $sNo . '失败',2,$source,$shop_id,$operator_id);
                        $Log_content = __METHOD__ . '->' . __LINE__ . '自提订单号' . $sNo . '时,改变订单状态失败！';
                        $mch->mchLog($Log_content);
                        $message = Lang('Busy network');
                        echo json_encode(array('code' => 103, 'message' => $message));
                        exit();
                    }
    
                    $r3 = Db::name('order_details')->where('r_sNo', $sNo)->update(['r_status' => '5','deliver_time'=>$time,'arrive_time'=>$time]);
                    if ($r3 < 0)
                    {
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '核销了订单号：' . $sNo . '失败',2,$source,$shop_id,$operator_id);
                        $Log_content = __METHOD__ . '->' . __LINE__ . '自提订单号' . $sNo . '时,改变订单详情状态失败！';
                        $mch->mchLog($Log_content);
                        $message = Lang('Busy network');
                        echo json_encode(array('code' => 103, 'message' => $message));
                        exit();
                    }
    
                    $r_x = Db::name('product_list')->where(['store_id'=>$store_id,'id'=>$p_id])->update(['volume'=>Db::raw('volume+'.$num)]);
                    if ($r_x < 1)
                    {
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '核销了订单号：' . $sNo . '失败',2,$source,$shop_id,$operator_id);
                        $Log_content = __METHOD__ . '->' . __LINE__ . '自提订单号' . $sNo . '时,改变商品销量失败！';
                        $mch->mchLog($Log_content);
                        $message = Lang('Busy network');
                        echo json_encode(array('code' => 103, 'message' => $message));
                        exit();
                    }
    
                    $r_s = ConfigureModel::where(['pid'=>$p_id,'id'=>$sid])->field('total_num')->select()->toArray();
                    $total_num = $r_s[0]['total_num'];
                    $content = $user_id . '订单自提' . $num;
    
                    $data_insert = array('store_id'=>$store_id,'product_id'=>$p_id,'attribute_id'=>$sid,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>1,'user_id'=>$user_id,'add_date'=>$time,'content'=>$content);
                    Db::name('stock')->insert($data_insert);

                    if($operator_id != 0)
                    {
                        $sql_0 = "insert into lkt_mch_admin_record(store_id,administrators_id,sNo,add_date) value ('$store_id','$operator_id','$sNo','$time')";
                        $r_0 = Db::execute($sql_0);
                    }

                    $mch->parameter($store_id, $sNo, $z_price, $allow);

                    $Log_content = __METHOD__ . '->' . __LINE__ . '自提订单号' . $sNo . '成功！';
                    $mch->mchLog($Log_content);

                    Db::commit();
                    $Jurisdiction->admin_record($store_id, $operator, '核销了订单号：' . $sNo,2,$source,$shop_id,$operator_id);
                    $data = array('sNo' => $sNo, 'p_price' => round($z_price,2));
                    $message = Lang('Success');
                    echo json_encode(array('code' => 200, 'message' => $message, 'data' => $data));
                    exit();
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '自提订单号' . $sNo . '时，验证码已失效！';
                    $mch->mchLog($Log_content);
                    $message = Lang('mch.21');
                    echo json_encode(array('code' => 103, 'message' => $message));
                    exit();
                }
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '自提时，提货码' . $extraction_code . '错误！';
            $mch->mchLog($Log_content);
            $message = Lang('tools.10');
            echo json_encode(array('code' => 103, 'message' => $message));
            exit();
        }
    }

    // 批量发货修改订单状态（新）
    public static function ShipmentModificationOrderStatus($array)
    {

        $store_id = $array['store_id']; // 商城ID
        $store_type = $array['store_type']; // 
        $sNo = $array['sNo']; // 订单号
        $detailId = $array['detailId']; // 订单详情
        $express_id = $array['express_id']; // 快递公司ID
        $courier_num = $array['courier_num']; // 物流单号
        $d_num = $array['d_num']; // 发货数量
        $childNum = $array['childNum']; // 子单号
        $returnNum = $array['returnNum']; // 回单号
        $label = $array['label']; // 面单短链
        $kdComOrderNum = $array['kdComOrderNum']; // 快递公司订单号
        $subtable_id = $array['subtable_id']; // 快递公司子表ID
        $operator_shop_id = $array['shop_id'];
        $operator = $array['operator'];
        $operator_source = $array['source'];
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }

        $time = date('Y-m-d H:i:s', time());
        $code = 1;
        $lktlog = new LaiKeLogUtils();

        $sql0 = "select * from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and id = '$detailId' and r_status = 1  ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $express_id_0 = '';
            if($r0[0]['express_id'] == '')
            {
                $express_id_0 = $express_id;
            }
            else
            {
                $express_id_0 = $r0[0]['express_id'] . ',' . $express_id;
            }

            $courier_num_0 = '';
            if($r0[0]['courier_num'] == '')
            {
                $courier_num_0 = $courier_num;
            }
            else
            {
                $courier_num_0 = $r0[0]['courier_num'] . ',' . $courier_num;
            }

            $sql1 = "update lkt_order_details set r_status = 2,express_id = '$express_id_0',courier_num = '$courier_num_0',deliver_time = '$time',deliver_num = deliver_num + '$d_num' where store_id = '$store_id' and r_sNo = '$sNo' and id = '$detailId' ";
            $r1 = Db::execute($sql1);
            if ($r1 < 1)
            {
                $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "修改订单详情状态失败！sql:" . $sql1);
                $code = 0;
            }

            $sql3 = "insert into lkt_express_delivery(store_id,sNo,order_details_id,express_id,courier_num,num,deliver_time,childNum,returnNum,label,kdComOrderNum,is_status,subtable_id) value ('$store_id','$sNo','$detailId','$express_id','$courier_num','$d_num','$time','$childNum','$returnNum','$label','$kdComOrderNum',0,'$subtable_id')";
            $r3 = Db::execute($sql3);
            if ($r3 < 1)
            {
                $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "添加快递记录表失败！sql:" . $sql3);
                $code = 0;
            }
        }
        else
        {
            $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "该商品已发货失败！sql:" . $sql0);
            $code = 0;
        }

        $status_num = 0; // 未发货订单详情数量
        $sql_0 = "select id from lkt_order_details where r_sNo = '$sNo' and r_status = 1 ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $status_num = count($r_0); // 未发货订单详情数量
        }

        if($status_num == 0)
        {
            $sql_1 = "update lkt_order set status = 2 where store_id = '$store_id' and sNo = '$sNo' ";
            $r_1 = Db::execute($sql_1);
            if($r_1 < 0)
            {
                $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "修改订单状态失败！sql:" . $sql_1);
                $code = 0;
            }
        }

        return $code;
    }
    
    // 发货信息录入接口
    public static function Delivery_information_input_interface($array)
    {
        $store_id = $array['store_id'];
        $real_sno = $array['real_sno'];
        $user_id = $array['user_id'];
        $shipping_list = $array['shipping_list'];
        $lktlog = $array['lktlog'];
        $Tools = new Tools($store_id, 1);
        $access_token = '';
        $sql0 = "select config_data from lkt_payment as p left join lkt_payment_config as c on c.pid = p.id where c.status = 1 and p.class_name = 'mini_wechat' and c.store_id = '$store_id' ";
        $r0 = Db::query($sql0);
        
        if($r0)
        {
            $config_data = json_decode($r0[0]['config_data'],true);
            $appid = $config_data['appid']; // 公众号唯一标识
            $appsecret = $config_data['appsecret']; // 公众号的 app secret
            $mch_id = $config_data['mch_id']; // 公众号唯一标识

            $url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=".$appid."&secret=".$appsecret;
            $res = $Tools->https_get($url);
            $data = json_decode($res,true);

            if(isset($data['access_token']))
            {
                $access_token = $data['access_token'];
            }
        }

        $url0 = "https://api.weixin.qq.com/wxa/sec/order/upload_shipping_info?access_token=" . $access_token;

        $order_key = array(
            'order_number_type'=>1, // 订单单号类型，用于确认需要上传详情的订单。枚举值1，使用下单商户号和商户侧单号；枚举值2，使用微信支付单号。
            'mchid'=>$mch_id,
            'out_trade_no'=>$real_sno // 商户系统内部订单号，只能是数字、大小写字母`_-*`且在同一个商户号下唯一
        );
        $logistics_type = 1; // 物流模式，发货方式枚举值：1、实体物流配送采用快递公司进行实体物流配送形式 2、同城配送 3、虚拟商品，虚拟商品，例如话费充值，点卡等，无实体配送形式 4、用户自提
        $delivery_mode = 2; // 发货模式，发货模式枚举值：1、UNIFIED_DELIVERY（统一发货）2、SPLIT_DELIVERY（分拆发货） 示例值: UNIFIED_DELIVERY
        $is_all_delivered = false; // 分拆发货模式时必填，用于标识分拆发货模式下是否已全部发货完成，只有全部发货完成的情况下才会向用户推送发货完成通知。示例值: true/false
        $upload_time = date(DATE_ATOM); // 上传时间，用于标识请求的先后顺序 示例值: `2022-12-15T13:29:35.120+08:00`
        
        $payer = array(); // 支付者，支付者信息

        // 根据商城ID、用户ID，查询openid
        $sql1 = "select * from lkt_user where store_id = '$store_id' and user_id = '$user_id' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $wx_id = $r1[0]['wx_id'];
            $payer['openid'] = $wx_id;
        }

        // 根据商城ID、支付单号，查询订单数
        $sql2 = "select id from lkt_order where store_id = '$store_id' and real_sno = '$real_sno' ";
        $r2 = Db::query($sql2);
        
        // 根据商城ID、支付单号、已发货，查询订单数
        $sql3 = "select id from lkt_order where store_id = '$store_id' and real_sno = '$real_sno' and status > 1 ";
        $r3 = Db::query($sql3);
        if(count($r2) == count($r3))
        {
            $is_all_delivered = true;
        }

        $data0 = array();
        if($payer['openid'] != '')
        {
            $data0 = array(
                'order_key'=>$order_key,
                'logistics_type'=>$logistics_type,
                'delivery_mode'=>$delivery_mode,
                'is_all_delivered'=>$is_all_delivered,
                'shipping_list'=>$shipping_list,
                'upload_time'=>$upload_time,
                'payer'=>$payer
            );
        }

        if($data0 != array())
        {
            $res0 = $Tools->https_post($url0,json_encode($data0));
            $list = json_decode($res0,true);
            $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "发货信息录入接口返回:" . $res0);
        }
        
        return;
    }

    // 线下订单审核
    public static function offlineReview($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $shop_id = $array['shop_id']; // 操作人店铺
        $sNo = $array['sNo']; // 订单ID
        $review_status = $array['review_status']; // 凭证审核状态 0.未上传凭证 1.待审核 2.通过 3.拒绝
        $reason_for_rejection = $array['reason_for_rejection']; // 拒绝原因
        $source = $array['source'];
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($data['operator']))
        {
            $operator = $data['operator'];
        }

        $Jurisdiction = new Jurisdiction();
        $lktlog = new LaiKeLogUtils();
        $time = date('Y-m-d H:i:s');

        $sql0 = "select self_lifting from lkt_order where store_id = '$store_id' and sNo = '$sNo' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $self_lifting = $r0[0]['self_lifting']; // 自提 0.配送 1.自提 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销

            if($review_status == 3)
            {
                if($reason_for_rejection == '')
                {
                    $message = Lang('mch.41');
                    echo json_encode(array('code' => 103, 'message' => $message));
                    exit();
                }
                $res = "拒绝订单：" . $sNo . "线下支付";
            }
            else
            {
                $res = "通过订单：" . $sNo . "线下支付";

            }

            $sql_oo_where = array('store_id'=>$store_id,'sNo'=>$sNo);
            $sql_oo_where1 = array('store_id'=>$store_id,'r_sNo'=>$sNo);
            $sql_oo_update = array('pay'=>'offline_payment','pay_time'=>$time,'review_status'=>$review_status,'review_time'=>$time,'reason_for_rejection'=>$reason_for_rejection);
            
            if($review_status == 2)
            {
                if ($self_lifting == '1' )
                { // 自提 
                    $sql_oo_update['status'] = 2;
                    $sql_oo_update1['r_status'] = 2;
                }
                else if ($self_lifting == '3')
                { // 虚拟订单需要线下核销
                    $sql_oo_update['status'] = 8;
                    $sql_oo_update1['r_status'] = 8;
                }
                else if ($self_lifting == '4')
                { // 虚拟订单无需线下核销
                    $sql_oo_update['status'] = 5;
                    $sql_oo_update1['r_status'] = 5;
                }
                else
                { // 配送
                    $sql_oo_update['status'] = 1;
                    $sql_oo_update1['r_status'] = 1;
                }
                $r1 = Db::name('order_details')->where($sql_oo_where1)->update($sql_oo_update1);
            }

            $r = Db::name('order')->where($sql_oo_where)->update($sql_oo_update);

            $Jurisdiction->admin_record($store_id, $operator, $res,6,$source,$shop_id,$operator_id);
            $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . $res);
            if($r > 0)
            {
                $message = Lang('Success');
                echo json_encode(array('code' => 200, 'message' => $message));
                exit();
            }
            else
            {
                $message = Lang('operation failed');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit();
            }
        }
        else
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 103, 'message' => $message));
            exit();
        }
    }

    // 拆分订单
    public static function leave_Settlement($array)
    {
        $store_id = $array['store_id'];
        $user_id = $array['user_id'];
        $sNo = $array['sNo'];

        $lktlog = new LaiKeLogUtils();

        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1,'recycle'=>0])->field('shop_id')->select()->toArray();
        $admin_mch_id = $r_admin[0]['shop_id'];

        //事务开启
        Db::startTrans();
        try
        {
            // 查询刚刚生成的订单
            $r0 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id])->select()->toArray();

            //获取订单数据
            $coupon_id = $r0[0]['coupon_id']; // 优惠券ID
            $subtraction_id = $r0[0]['subtraction_id']; // 满减ID
            $reduce_price = $r0[0]['reduce_price']; // 查询出的满减金额
            $grade_rate = $r0[0]['grade_rate']; // 会员折扣
            $z_spz_price = $r0[0]['spz_price'];
            $status = $r0[0]['status']; // 订单状态
            $operation_type = $r0[0]['operation_type'];//下单类型
            $z_price = $r0[0]['z_price'];//订单金额
            $p_sNo = $r0[0]['p_sNo'];//订单金额
            $lj_price = 0;
            $remarks = array();
            if($r0[0]['remarks'] != '')
            {
                $remarks = unserialize($r0[0]['remarks']); // 订单状态
            }

            $mch_id = $r0[0]['mch_id']; // 店铺ID字符串
            $data = (array)$r0[0];
            unset($data['id']);

            $suppliers = array();
            // 根据订单号，查询订单商品ID
            $sql1 = "select a.id,d.id as p_id,b.num,a.offset_balance,b.supplier_id from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo left join lkt_configure as c on b.sid = c.id left join lkt_product_list as d on c.pid = d.id where a.store_id = $store_id and b.r_sNo = '$sNo'";
            $r1 = Db::query($sql1);
            if ($r1)
            {   
                $payment_money = $r1[0]['offset_balance'];
                foreach ($r1 as $k => $v)
                {   
                    array_push($suppliers, $v['supplier_id']);
                }
            }
            $type = substr($sNo, 0, 2);//获取订单号前两位字母（类型）
            $mch_id = substr($mch_id, 1, -1);
            $shop_id = explode(',', $mch_id); // 店铺id字符串

            $Toosl = new Tools($store_id,1);
            $suppliers = array_unique($suppliers);
            if (count($shop_id) != 1 || count($suppliers) != 1)
            {  // 当为多家店铺时
                $coupon_list = array();

                if($coupon_id != '' && $coupon_id != '0')
                {
                    if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
                    {
                        $coupon = new CouponPublicMethod();
                        $coupon_list = $coupon->leave_Settlement($store_id, $user_id, $sNo);
                    }
                }

                $m_num = count($shop_id) - 1;
                $s_num = count($suppliers) - 1;
                foreach ($shop_id as $k => $v)
                {   
                    foreach ($suppliers as $key => $value) 
                    {
                        $sNo1 = $Toosl->Generate_order_number($type, 'sNo'); // 生成订单号
                        $data['mch_id'] = $v;
                        $data['sNo'] = $sNo1;
                        $data['p_sNo'] = $sNo;
                        $data['supplier_id'] = $value;
                        //查询单个商品的价格，运费，数量
                        $sql1 = "select a.id,a.p_id,a.p_price,a.num,a.freight,a.r_sNo,a.manual_offer,a.actual_total,a.score_deduction,a.after_discount
                                from lkt_order_details as a
                                left join lkt_product_list as b on a.p_id = b.id
                                where a.store_id = '$store_id' and a.r_sNo = '$sNo' and b.mch_id = '$v' and a.supplier_id = '$value'";
                        $r1 = Db::query($sql1);
                        //如果查询到数据
                        $order_num_ = 0;
                        if ($r1)
                        {
                            $spz_price = 0;
                            $z_freight = 0;
                            $z_manual_offer = 0;
                            $actual_total = 0; // 累计金额
                            $score_deduction = 0; // 累计抵扣积分
                            $after_discount = 0; // 累计优惠后金额
                            foreach ($r1 as $ke => $va)
                            {
                                $order_details_id = $va['id'];
                                $order_num_ = $order_num_ + $va['num'];
                                $spz_price += $va['p_price'] * $va['num'];
                                $z_freight += $va['freight'];
                                $z_manual_offer += $va['manual_offer'];
                                $actual_total += $va['actual_total'];
                                $score_deduction += $va['score_deduction'];
                                $after_discount += $va['after_discount'];
                                //更新详单订单号
                                $r2 = Db::name('order_details')->where(['store_id'=>$store_id,'id'=>$order_details_id])->update(['r_sNo' => $sNo1]);
                                if (!$r2)
                                {
                                    $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "修改订单号失败！order_details_id:" . $order_details_id);
                                }
                                //更新供应商运费表
                                $r3 = Db::name('supplier_order_fright')->where(['store_id'=>$store_id,'detail_id'=>$order_details_id])->update(['sNo' => $sNo1]);
                                if (!$r3)
                                {
                                    $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "修改供应商运费订单失败！detail_id:" . $order_details_id);
                                }
                            }
                            $total_fright = Db::name('supplier_order_fright')->where(['store_id'=>$store_id,'sNo'=>$sNo1])->sum('freight');
                            //更新供应商运费表
                            $r3 = Db::name('supplier_order_fright')->where(['store_id'=>$store_id,'sNo'=>$sNo1])->update(['total_fright' => $total_fright]);
                            if (!$r3)
                            {
                                $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "修改供应商运费订单失败！sNo:" . $sNo1);
                            }
                            $data['spz_price'] = $spz_price;
                            $data['pay_time'] = date("Y-m-d H:i:s");
                            $data['manual_offer'] = $z_manual_offer;
                            $data['subtraction_id'] = 0;
                            $data['reduce_price'] = 0;
                            $data['coupon_id'] = 0;
                            $data['coupon_price'] = 0;
                            $data['grade_rate'] = $grade_rate;
                            $data['preferential_amount'] = 0;
                            $data['operation_type'] = $operation_type;
                            $data['score_deduction'] = $score_deduction;
                            $z_price_0 = round($spz_price * $grade_rate * 0.1, 2); // 会员价
                            if($coupon_id != '' && $coupon_id != '0')
                            { // 参与优惠券
                                if(count($coupon_list) > 0)
                                {
                                    foreach ($coupon_list as $k1 => $v1)
                                    {
                                        if($v == $v1['mch_id'])
                                        {
                                            $data['coupon_id'] = $v1['coupon_id'];
                                            $data['coupon_price'] = $v1['preferential_amount'];
                                            $z_price_0 = ($z_price_0 - $data['coupon_price']);
                                            $data['preferential_amount'] = $v1['preferential_amount'] - $v1['mah_coupon_amount'];

                                            $r_coupon1 = $coupon->coupon_sno($store_id, $user_id, $v1['coupon_id'],$sNo1,'add');
                                            if ($r_coupon1 == 2)
                                            {
                                                //回滚删除已经创建的订单
                                                Db::rollback();
                                                $message = Lang('order.1');
                                                return output(ERROR_CODE_TJYHQGLDDSJSB,$message);
                                            }
                                        }
                                    }
                                }
                            }

                            if($score_deduction != 0)
                            {
                                if($m_num == $k && $s_num == $ke)
                                {
                                    $data['z_price'] = $z_price - $lj_price;
                                }
                                else
                                {
                                    $data['z_price'] = $after_discount - $actual_total + $z_freight;
                                    $lj_price += $data['z_price'];
                                }
                            }
                            else
                            {
                                $data['z_price'] = $z_price_0 + $z_freight - $z_manual_offer;
                            }
                            if($data['z_price'] == '0.00' || $data['z_price'] == 0)
                            {
                                $data['z_price'] = '0.01';
                            }
                            $data['z_freight'] = $z_freight;
                            $data['status'] = 1;
                            $data['mch_id'] = ',' . $data['mch_id'] . ',';
                            $data['num'] = $order_num_;
                            $data['remarks'] = '';
                            if(count($remarks) > 0)
                            {
                                if($remarks[$v] != '')
                                {
                                    $remark = array('0'=>$remarks[$v]);
                                    $data['remarks'] = serialize($remark);
                                }
                            }

                            $r_attribute = OrderModel::create($data); // 框架模型-新增-静态方法
                            if ($r_attribute->id < 1)
                            {
                                $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "添加订单失败！");
                            }

                            $message_5 = "您来新订单了，订单为".$sNo1."，请及时处理！";
                            $message_logging_list5 = array('store_id'=>$store_id,'mch_id'=>$v,'gongyingshang'=>0,'type'=>5,'parameter'=>$sNo1,'content'=>$message_5);
                            PC_Tools::add_message_logging($message_logging_list5);
                        }
                        // else
                        // {
                        //     //回滚删除已经创建的订单
                        //     Db::rollback();
                        //     ob_clean();
                        //     $message = Lang('Parameter error');
                        //     return output(ERROR_CODE_CSCW,$message,array("line" => __LINE__));
                        // }
                    }
                    
                }

                $sql4 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id])->find();
                $sql4->status = 7;
                $sql4->recycle = 1;
                $r4 = $sql4->save();
                if (!$r4)
                {
                    $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "修改订单状态失败！sNo:" . $sNo);
                }
            }
            else
            {   
                if($suppliers && $suppliers[0] > 0)
                {
                    $supplier_id = $suppliers[0];

                    Db::name('order')->where(['store_id'=>$store_id,'sNo'=>$sNo])->update(['supplier_id'=>$supplier_id]);

                    $total_fright = Db::name('supplier_order_fright')->where(['store_id'=>$store_id,'sNo'=>$sNo])->sum('freight');
                    //更新供应商运费表
                    $r3 = Db::name('supplier_order_fright')->where(['store_id'=>$store_id,'sNo'=>$sNo])->update(['total_fright' => $total_fright]);
                    if (!$r3)
                    {
                        $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . "修改供应商运费订单失败！sNo:" . $sNo);
                    }
                }

                $message_5 = "您来新订单了，订单为".$sNo."，请及时处理！";
                $message_logging_list5 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>5,'parameter'=>$sNo,'content'=>$message_5);
                PC_Tools::add_message_logging($message_logging_list5);
            }
            
            Db::commit();
            return ;
        }
        catch (\Exception $e) 
        {
            // 回滚事务
            Db::rollback();
            $Log_content = $e->getMessage();
            $lktlog->log("common/DeliveryHelper.log",__METHOD__ . ":" . __LINE__ . $Log_content);
            $message = Lang('operation failed');
            return output(ERROR_CODE_SQSB,$message);
        }
    }
}