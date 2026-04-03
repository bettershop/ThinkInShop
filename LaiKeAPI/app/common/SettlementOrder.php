<?php

namespace app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Jurisdiction;

use app\admin\model\OrderModel;
use app\admin\model\MchModel;
use app\admin\model\DistributionRecordModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\PaymentModel;
use app\admin\model\ConfigureModel;

class SettlementOrder
{   
    // 结算订单列表
    public function index($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $shop_id = $array['shop_id']; // 店铺ID
        $otype = $array['otype']; // 订单类型
        $search = $array['search']; // 订单号/订单id
        $startdate = $array['startdate']; // 查询开始时间
        $enddate = $array['enddate']; // 查询结束时间
        $status = $array['status']; // 结算状态
        $mch_name = $array['mch_name']; // 店铺名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页显示多少条数据

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $total = 0;
        $list = array();

        $condition = " o.store_id = 1 AND o.status IN (1,2,5,7) and o.recycle != 1 ";
        if($otype != '')
        {
            $condition .= " and o.otype = '$otype' ";
        }

        if($shop_id != 0)
        {
            $condition .= " and m.id = '$shop_id' ";
        }

        if($search != '')
        {
            $search1 = Tools::FuzzyQueryConcatenation($search);
            $condition .= " and (o.sNo like $search1 or o.id = '$search')";
        }
        if(strlen($status) > 0)
        {
            $condition .= " and o.settlement_status = '$status' ";
        }
        if($startdate != '')
        {
            $condition .= " and o.add_time >= '$startdate' ";
        }
        if($enddate != '')
        {
            $condition .= " and o.add_time <= '$enddate' ";
        }
        if($mch_name != '')
        {
            $mch_name = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and m.name like $mch_name ";
        }

        $sql_num = "select ifnull(count(e.id),0) as num from (SELECT tt.* FROM (SELECT d.id detailId,d.p_id,d.sid,o.real_sno,o.id,o.consumer_money,o.num,o.sNo,o.name,o.sheng,o.shi,o.xian,o.source,o.address,o.add_time,o.mobile,o.z_price,o.z_freight,COUNT(d.id) over (PARTITION BY o.sNo) AS goodsNum,o.status,o.reduce_price,o.coupon_price,o.preferential_amount,o.allow,o.otype,o.ptstatus,o.spz_price,o.drawid,lu.user_name,o.user_id,o.mch_id,o.p_sNo,m.id AS shop_id,m.name shopName,o.arrive_time,o.settlement_status,o.operation_type,d.p_name product_title,attr.attribute,attr.img,d.num needNum,o.self_lifting,d.express_id,d.courier_num,d.freight,attr.price goodsAmt,d.after_discount,pay.name pay,row_number () over (PARTITION BY o.sNo) AS top
            FROM lkt_order AS o
            LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id
            RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo
            RIGHT JOIN lkt_configure attr ON attr.id=d.sid
            RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
            RIGHT JOIN lkt_mch AS m ON p.mch_id = m.id
            LEFT JOIN lkt_payment pay ON pay.class_name = o.pay
            WHERE $condition ) AS tt WHERE tt.top<2) as e ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        if($total > 0)
        {
            $sql = "select tt.* from (select d.id detailId,d.p_id,d.sid,o.real_sno,o.id,o.consumer_money,o.num,o.sNo,o.name,o.sheng,o.shi,o.xian,o.source,o.address,o.add_time,o.mobile,o.z_price,o.z_freight,COUNT(d.id) over (PARTITION BY o.sNo) AS goodsNum,o.status,o.reduce_price,o.coupon_price,o.preferential_amount,o.allow,o.otype,o.ptstatus,o.spz_price,o.drawid,lu.user_name,o.user_id,o.mch_id,o.p_sNo,m.id AS shop_id,m.name shopName,o.arrive_time,o.settlement_status,o.operation_type,d.p_name product_title,attr.attribute,attr.img,d.num needNum,o.self_lifting,d.express_id,d.courier_num,d.freight,attr.price goodsAmt,d.after_discount,pay.name pay,d.p_price as goodsPrice,row_number () over (PARTITION BY o.sNo) AS top
                from lkt_order AS o
                LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id
                RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                RIGHT JOIN lkt_mch AS m ON p.mch_id = m.id
                LEFT JOIN lkt_payment pay ON pay.class_name = o.pay
                WHERE $condition ) AS tt WHERE tt.top<2 order by tt.add_time desc limit $start,$pagesize";
            $res = Db::query($sql);
            foreach ($res as $key => $value) 
            {
                //店铺优惠金额
                $value['mch_discount'] = $value['coupon_price'];
                $otype = $value['otype'];//订单类型
                $sNo = $value['sNo'];//订单号
                //佣金计算
                if($otype == 'FX')
                {
                    $value['commission'] = DistributionRecordModel::where(['type'=>1,'sNo'=>$sNo])->sum('money');
                    $value['r_commission'] = DistributionRecordModel::where(['type'=>9,'sNo'=>$sNo])->sum('money');
                }
                else
                {
                   $value['commission'] = 0;//佣金
                   $value['r_commission'] = 0;//回退佣金
                }

                //退款金额
                $value['return_money'] = ReturnOrderModel::where('re_type','in','1,2')->where('r_type','in','4,9')->where('sNo',$sNo)->sum('real_money');
                if($value['settlement_status']==1)
                {
                    $value['status_name'] = '已结算';
                    $value['settlementPrice'] = $value['z_price'];
                }
                else
                {
                    $value['status_name'] = '待结算';
                    $value['settlementPrice'] = 0;
                }

                $list[$key] = $value;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 结算订单详情
    public function detail($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $mch_id = $array['mch_id']; // 店铺ID
        $sNo = $array['sNo']; // 订单号
        $operator = $array['operator']; // 操作人
        $operator_source = $array['operator_source']; // 操作人来源

        //支付方式
        $payments = PaymentModel::order('sort','desc')->select()->toArray();
        $payments_type = array();
        foreach ($payments as $keyp => $valuep) 
        {
            $payments_type[$valuep['class_name']] = $valuep['name'];
        }

        $sql = "select l.p_sNo,d.id,l.source,l.remarks,l.pay_time,l.id as oid,l.spz_price,l.z_freight,u.user_name,l.sNo,l.name,l.mobile,l.sheng,l.shi,l.z_price,l.xian,l.status,l.address,l.pay,l.trade_no,l.coupon_id,l.reduce_price,l.coupon_price,l.allow,l.drawid,l.otype,l.grade_rate,l.preferential_amount,l.mch_id,d.user_id,d.p_id,d.p_name,d.p_price,d.num,d.unit,d.add_time,d.arrive_time,d.deliver_time,d.r_status,d.express_id,d.courier_num,d.sid,d.size,d.freight,e.kuaidi_name,c.total_num ,l.subtraction_id,d.after_discount,m.name as mchName
                from lkt_order_details as d 
                left join lkt_order as l on l.sNo=d.r_sNo 
                left join lkt_user as u on u.user_id=l.user_id and u.store_id='$store_id' 
                left join lkt_express as e on d.express_id=e.id 
                left join lkt_configure as c on c.id = d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = c.pid
                RIGHT JOIN lkt_mch AS m ON p.mch_id = m.id
                where l.store_id = '$store_id' and l.sNo='$sNo'";
        $res = Db::query($sql);
        $num = count($res);
        $data = array();
        $reduce_price = 0; // 满减金额
        $coupon_price = 0; // 优惠券金额
        $preferential_amount = 0; // 平台优惠券金额
        $allow = 0; // 积分
        $yunfei = 0;
        $courier_num_arr = array();
        $yh_money = 0; // 优化金额

        $discount_type = '';
        $coupon_id = $res[0]['coupon_id'];// 优惠券ID
        $subtraction_id = $res[0]['subtraction_id'];// 满减活动ID
        $grade_rate = $res[0]['grade_rate'];//会员等级折扣
        $spz_price = $res[0]['spz_price'];// 商品总价
        $z_freight = $res[0]['z_freight'];// 总运费
        $otype = $res[0]['otype'];// 订单类型
        $mch_id = $res[0]['mch_id'];//店铺ID
        $pay_price = $res[0]['z_price'];
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
        $data['express_name'] = '';
        foreach ($res as $k => $v)
        {   
            $res[$k]['p_price'] = (float)$v['p_price'];
            $res[$k]['after_discount'] = (float)$v['after_discount'];
            $res[$k]['coupon_price'] = (float)$v['coupon_price'];
            $res[$k]['reduce_price'] = (float)$v['reduce_price'];
            $res[$k]['preferential_amount'] = (float)$v['preferential_amount'];
            $res[$k]['freight'] = (float)$v['freight'];
            $res[$k]['spz_price'] = (float)$v['spz_price'];
            $res[$k]['z_freight'] = (float)$v['z_freight'];
            $res[$k]['z_price'] = (float)$v['z_price'];
            $p_price = $v['p_price']; // 商品售价
            $p_num = $v['num']; // 商品数量
            $after_discount = $v['after_discount']; // 优惠后的金额
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
            $data['remark'] = $remarks;
            $data['arrive_time'] = $v['arrive_time'];
            $data['pay'] = $v['pay'];//支付时间
            $data['pay_time'] = $v['pay_time'];//支付时间
            $data['source'] = $v['source']; //来源
            $data['sNo'] = $v['sNo']; // 订单号
            $data['oid'] = $v['oid']; // oid
            $data['mobile'] = $v['mobile']; // 联系电话
            if ($v['grade_rate'] == "1.00" || $v['grade_rate'] == "0.00")
            {
                $v['grade_rate2'] = 1;
            }
            else
            {
                $v['grade_rate2'] = $v['grade_rate'];
            }
            $yh_money = $yh_money + ($v['num'] * $v['p_price']) - ($v['num'] * $v['p_price'] * $v['grade_rate2']);

            $data['grade_rate'] = 0;
            $data['grade_rate2'] = $v['grade_rate2'];
            $data['address'] = $v['sheng'] . $v['shi'] . $v['xian'] . $v['address']; // 详细地址
            $data['sheng'] = $v['sheng'];
            $data['shi'] = $v['shi'];
            $data['xian'] = $v['xian'];
            $data['r_address'] = $v['address'];
            $data['add_time'] = $v['add_time']; // 添加时间
            $data['z_price'] = (float)$v['z_price']; // 添加时间
            $data['user_id'] = $v['user_id']; // 用户id
            $data['deliver_time'] = '';
            if(!empty($v['deliver_time']))
            {
                $data['deliver_time'] = $v['deliver_time']; // 发货时间
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
                default:
                    $data['orderTypeName'] = '普通';
                    break;
            }

            $data['express_id'] = $v['express_id']; // 快递公司id
            if( $v['express_id'])
            {
                $express_ids[] =  $v['express_id'];
            }
            $expressStr = '';
            $data['express_name'] .= $v['kuaidi_name'].",";

            if (!in_array($v['courier_num'], $courier_num_arr))
            {
                if (!empty($v['courier_num']))
                {
                    $data['courier_num'][$k]['num'] = $v['courier_num']; // 快递单号
                    $data['courier_num'][$k]['kuaidi_name'] = $v['kuaidi_name'];
                    $courier_num_arr[] = $v['courier_num'];
                    $expressStr .= $v['kuaidi_name']."(".$v['courier_num']."),";
                }
            }
            $expressStr = rtrim($expressStr,',');
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
            else
            {
                $paytype = '组合支付';
            }

            $data['paytype'] = $paytype; // 支付方式
            $data['trade_no'] = $v['trade_no']; // 微信支付交易号
            $yunfei = $yunfei + $v['freight'];
            $data['id'] = $sNo;

            // 根据产品id,查询产品主图
            $img = ConfigureModel::where(['id'=>$v['sid']])->field('img,num')->select()->toArray();
            if (!empty($img))
            {
                $res[$k]['img'] = $img[0]['img'];
                $res[$k]['stockNum'] = $img[0]['num'];
                $res[$k]['pic'] = ServerPath::getimgpath($img[0]['img'], $store_id);
            }

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
                $res[$k]['statusName'] = '订单完成';
            }
            else if($v['status'] == 7)
            {
                $res[$k]['statusName'] = '订单关闭';
            }

            if($v['otype'] == 'JP')
            {
                $mch_id_JP = trim($v['mch_id'],',');//店铺ID

                $mch = MchModel::where(['id'=>$mch_id_JP])->field('name')->select()->toArray();
                $res[$k]['mchName'] = $mch[0]['name'];
            }
            $data['lottery_status'] = 7;
            $res[$k]['returnInfo'] = array();
            //获取订单售后信息
            $oid = $v['id'];
            $returnInfo = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->field('id,r_type')->select()->toArray();
            if($returnInfo)
            {
                foreach ($returnInfo as $key => $value) 
                {
                    $res[$k]['returnInfo'][$key]['id'] = $value['id'];
                    $res[$k]['returnInfo'][$key]['statusName'] = '';
                }
            }
        }
        $yh_money = $yh_money + $coupon_price + $reduce_price;
        $zp_res = array();

        $data['freight'] = $yunfei; // 运费
        $data['yunfei'] = $yunfei; // 运费

        if ($data['otype'] == 'PT')
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

        if ($v['otype'] == 'IN')
        {
            $integralid = $v['p_sNo'];
            $sql = "select g.integral,g.money,c.img from lkt_integral_goods as g left join lkt_configure as c on g.attr_id = c.id where g.id='$integralid'";
            $inr = Db::query($sql);
            if ($inr)
            {
                $res[$k]['p_integral'] = $inr[0]['integral'];
                $res[$k]['p_money'] = $inr[0]['money'];
                $res[$k]['pic'] = ServerPath::getimgpath($inr[0]['img']);
            }
        }
        $sdata = array('待付款', '待发货', '待收货', '订单完成', '订单关闭');

        $data = array('sdata'=>$sdata,'zp_res'=>$zp_res,'update_s'=>true,'data'=>$data,'detail'=>$res,'reduce_price'=>(float)$reduce_price,'coupon_price'=>(float)$coupon_price,'preferential_amount'=>(float)$preferential_amount,'allow'=>$allow,'num'=>$num,'spz_price'=>(float)$spz_price,'discount_type'=>$discount_type,'expressStr'=>$expressStr,'isManyMch'=>$isManyMch,'z_freight'=>(float)$z_freight,'grade_rate'=>(float)$grade_rate,'grade_rate_amount'=>(float)$grade_rate_amount,'pay_price'=>(float)$pay_price,'id'=>$sNo,'remark'=>$remarks,'returnStatus'=>'','operator'=>$operator);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 管理后台-结算订单删除
    public function del($array)
    {
        $store_id = $array['store_id'];
        $id = $array['id'];
        $mch_id = $array['mch_id'];
        $operator_id = $array['operator_id'];
        $operator = $array['operator'];
        $operator_source = $array['operator_source'];

        $Jurisdiction = new Jurisdiction();

        $orders = explode(',', $id);

        Db::startTrans();
        if($orders != array())
        {
            foreach ($orders as $key => $value)
            {
                $r0 = OrderModel::where(['store_id'=> $store_id,'id'=>$value])->field('sNo')->select()->toArray();
                if($r0)
                {
                    $sNo = $r0[0]['sNo'];

                    $sql_where1 = array('store_id'=>$store_id,'r_sNo'=>$sNo);
                    $sql_update1 = array('recycle'=>1);
                    $r1 = Db::name('order_details')->where($sql_where1)->update($sql_update1);
                    if ($r1 < 0)
                    {
                        $this->Log(__METHOD__ . ":" . __LINE__ . "修改订单详情状态！参数:" . json_encode($sql_where1));
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '删除了订单ID：'.$sNo.' 的结算信息失败',10,$operator_source,$mch_id,$operator_id);
                        $message = Lang("operation failed");
                        echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                        exit;
                    }

                    $sql_where2 = array('store_id'=>$store_id,'id'=>$value);
                    $sql_update2 = array('recycle'=>1);
                    $r2 = Db::name('order')->where($sql_where2)->update($sql_update2);
                    if ($r2 < 0)
                    {
                        $this->Log(__METHOD__ . ":" . __LINE__ . "修改订单状态！参数:" . json_encode($sql_where2));
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '删除了订单ID：'.$sNo.' 的结算信息失败',10,$operator_source,$mch_id,$operator_id);
                        $message = Lang("operation failed");
                        echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                        exit;
                    }
                    
                    $Jurisdiction->admin_record($store_id, $operator, '删除了订单ID：'.$sNo.' 的结算信息',10,$operator_source,$mch_id,$operator_id);
                }
            }
        }
        
        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 日志
    public function Log($Log_content)
    {
        $time = date("Y-m-d");
        $lktlog = new LaiKeLogUtils();

        $lktlog->log("common/SettlementOrder.log",$Log_content);
        return;
    }
}
