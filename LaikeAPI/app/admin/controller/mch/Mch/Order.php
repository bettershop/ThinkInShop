<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\Plugin\PluginUtils;
use app\common\ServerPath;
use app\common\Jurisdiction;
use app\common\DeliveryHelper;
use app\common\EditOrderStatus;
use app\common\third\logistics\LogisticsTool;
use app\common\ExcelUtils;
use app\common\Plugin\MchPublicMethod;
use PhpOffice\PhpSpreadsheet\IOFactory;
use app\common\ExpressPublicMethod;
use app\common\Comments;
use app\common\Plugin\RefundUtils;

use app\admin\model\OrderConfigModel;
use app\admin\model\MchConfigModel;
use app\admin\model\IntegralConfigModel;
use app\admin\model\UserModel;
use app\admin\model\CustomerModel;
use app\admin\model\MchModel;
use app\admin\model\ExpressModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\GroupOpenModel;
use app\admin\model\BrandClassModel;
use app\admin\model\ProductClassModel;
use app\admin\model\ProductListModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\ReturnRecordModel;
use app\admin\model\FileDeliveryModel;
use app\admin\model\ReplyCommentsModel;
use app\admin\model\CommentsModel;
use app\admin\model\CommentsImgModel;

/**
 * 功能：PC店鋪订单类
 * 修改人：PJY
 */
class Order extends BaseController
{
    // 店铺订单设置
    public function MchIndex()
    {
        $store_id = trim($this->request->param('storeId'))?trim($this->request->param('storeId')):trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('storeType'))?trim($this->request->param('storeType')):trim($this->request->param('store_type'));

        $mch_id = addslashes($this->request->param('mchId'))?addslashes($this->request->param('mchId')):addslashes($this->request->post('mchId'));
        $is_type = addslashes($this->request->param('isType'))?addslashes($this->request->param('isType')):addslashes($this->request->post('isType'));

        if(empty($mch_id))
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
        $list = array('package_settings'=>0,'same_piece'=>0,'same_order'=>0);
        $res = MchConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('package_settings,same_piece,same_order')->select()->toArray();
        if($res)
        {
            $list = $res[0];
        }
        $message = Lang("Success");
        return output(200,$message,$list);
    }

    // 保存店铺订单设置
    public function MchSaveConfig()
    {
        $store_id = trim($this->request->param('storeId'))?trim($this->request->param('storeId')):trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('storeType'))?trim($this->request->param('storeType')):trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('accessId'))?trim($this->request->param('accessId')):trim($this->request->param('access_id'));

        $mch_id = addslashes($this->request->param('mchId'));
        $is_type = addslashes($this->request->param('isType'));

        $package_settings = addslashes($this->request->param('packageSettings')); // 包邮设置 0.未开启 1.开启
        $same_piece = addslashes($this->request->param('samePiece')); // 同件
        $same_order = addslashes($this->request->param('sameOrder')); // 同单

        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid');

        $lktlog = new LaiKeLogUtils();
        if(empty($mch_id))
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
        if ($package_settings == 1)
        {
            if ($same_piece != '')
            {
                if (is_numeric($same_piece))
                {
                    if ($same_piece < 0)
                    {   
                        $message = Lang("admin_order.3");
                        return output(ERROR_CODE_TJSLBNWFSHL,$message);
                    }
                }
                else
                {   
                    $message = Lang("admin_order.4");
                    return output(ERROR_CODE_TJSLBNWFSHL,$message);
                }
            }
            if ($same_order != '')
            {
                if (is_numeric($same_order))
                {
                    if ($same_order < 0)
                    {   

                        $message = Lang("admin_order.5");
                        return output(ERROR_CODE_TDSLBNWFSHL,$message);
                    }
                }
                else
                {
                    $message = Lang("admin_order.6");
                    return output(ERROR_CODE_TDSLBNWFSHL,$message);
                }
            }
        }
        else
        {
            $package_settings = 0;
            $same_piece = 0;
            $same_order = 0;
        }
        
        $r0 = MchConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('id')->select()->toArray();
        if($r0)
        {
            $sql_where = array('store_id'=>$store_id,'mch_id'=>$mch_id);
            $sql_update = array('package_settings'=>$package_settings,'same_piece'=>$same_piece,'same_order'=>$same_order);
            $res = Db::name('mch_config')->where($sql_where)->update($sql_update);
            if($res < 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了订单设置信息失败', 2,2,$shop_id);
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单设置失败！条件参数:" . json_encode($sql_where) . "；修改参数:" . json_encode($sql_update));
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            $Jurisdiction->admin_record($store_id, $operator, '修改了订单设置信息', 2,2,$shop_id);
        }
        else 
        {
            $sql_insert = array('store_id'=>$store_id,'mch_id'=>$mch_id,'package_settings'=>$package_settings,'same_piece'=>$same_piece,'same_order'=>$same_order);
            $res = Db::name('mch_config')->insert($sql_insert);
            if(!$res)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了订单设置信息失败', 1,2,$shop_id);
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加订单设置失败！参数:" . json_encode($sql_insert));
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            $Jurisdiction->admin_record($store_id, $operator, '添加了订单设置信息', 1,2,$shop_id);
        }

        $message = Lang("Success");
        return output(200,$message);
    }

    // 订单列表
    public function Index()
    {   

        $mch_id = $this->user_list['id'];
        $this->mch_id = $mch_id;
        $store_id = addslashes($this->request->param('storeId'));
        $this->store_id = $store_id;
        $store_type = addslashes($this->request->param('storeType'));
        $this->store_type = $store_type;
        $access_id = addslashes($this->request->param('accessId'));
        $this->access_id = $access_id;
        $sNo_id = trim($this->request->param('id')); // 订单号
        $this->sNo_id = $sNo_id;
        $status = trim($this->request->param('status')); // 订单状态
        $this->status = $status;
        $sNo = trim($this->request->param('keyWord'));
        $this->sNo = $sNo;
        $selfLifting = trim($this->request->param('selfLifting'));//是否自提1快递2自提
        $this->selfLifting = $selfLifting;
        $startdate = $this->request->param("startDate");
        $this->startdate = $startdate;
        $enddate = $this->request->param("endDate");
        $this->enddate = $enddate;
        $exportType = $this->request->param('exportType');
        $otype = 'GM';//订单类型
        $this->otype = $otype;
        // 导出
        $pagesize = $this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize : '10';
        // 每页显示多少条数据
        $page = $this->request->param('pageNo');
        
        // 页码
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }
        $this->pagesize = $pagesize;
        $this->page = $page;
        $this->start = $start;
        $this->Servertype = 'b_mch';
        $data = PluginUtils::invokeMethod('','order_index',$this);

        $total = $data['total'];
        $list = $data['list'];
        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '序号',
                1 => '订单编号',
                2 => '详情id',
                3 => '创单时间',
                4 => '产品名称',
                5 => '规格',
                6 => '数量',
                7 => '小计',
                8 => '订单总计',
                10 => '订单状态',
                11 => '订单类型',
                12 => '用户ID',
                13 => '联系人',
                14 => '电话',
                15 => '地址',
                16 => '支付方式',
                17 => '物流单号',
                18 => '运费'
            );
            $exportExcel_list = array();

            if ($data['info'])
            {
                foreach ($data['info'] as $k => $v)
                {
                    switch ($v['r_status'])
                    {
                        case 0 :
                            $r_status_name = '待付款';
                            break;
                        case 1 :
                            $r_status_name = '待发货';
                            break;
                        case 2 :
                            $r_status_name = '待收货';
                            break;
                        case 5 :
                            $r_status_name = '已完成';
                            break;
                        case 7 :
                            $r_status_name = '已关闭';
                            break;
                    }

                    $exportExcel_list[] = array(
                        $v['id'],
                        $v['orderno'],
                        $v['detailId'],
                        $v['createDate'],
                        $v['goodsName'],
                        $v['attrStr'],
                        $v['num'],
                        $v['goodsPrice'],
                        $v['orderPrice'],
                        $r_status_name,
                        $v['otype'],
                        $v['userId'],
                        $v['userName'],
                        $v['mobile'],
                        $v['addressInfo'],
                        $v['payName'],
                        '`'.$v['courier_num'],
                        $v['freight']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '订单列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'info'=>$data['info'],'total'=>$total));
    }

    // 订单编辑、详情展示页
    public function EditeOrderInfo()
    {
        $mch_id = $this->user_list['id'];
        $this->mch_id = $mch_id;
        $store_id = addslashes($this->request->param('storeId'));
        $this->store_id = $store_id;
        $store_type = addslashes($this->request->param('storeType'));
        $this->store_type = $store_type;
        $access_id = addslashes($this->request->param('accessId'));
        $this->access_id = $access_id;
        $sNo = addslashes($this->request->param('sNo'));

        $this->store_id = $store_id;
        $this->id = $sNo;
        $this->order_type = 'modify';
        $list = PluginUtils::invokeMethod('','order_details',$this);
        $sdata = $list['sdata'];
        $yh_money = $list['yh_money'];
        $zp_res = $list['zp_res'];
        $update_s = $list['update_s'];
        $data = $list['data'];
        $detail = $list['detail'];
        $reduce_price = $list['reduce_price'];
        $coupon_price = sprintf("%.2f",$list['coupon_price']);
        $preferential_amount = $list['preferential_amount'];
        $allow = sprintf("%.2f",$list['allow']);
        $num = $list['num'];
        $spz_price = $list['spz_price'];
        $discount_type = $list['discount_type'];
        $expressStr = $list['expressStr'];
        $isManyMch = $list['isManyMch'];
        $z_freight = $list['z_freight'];
        $grade_rate = $list['grade_rate'];
        $grade_rate_amount = $list['grade_rate_amount'];
        $pay_price = $list['pay_price'];
        $remarks = $list['remarks'];
        $old_total = $list['old_total'];
        $storeSelfInfo = $list['storeSelfInfo'];
        $currency_code = $list['currency_code'];
        $currency_symbol = $list['currency_symbol'];
        $cpc = $list['cpc'];
        ob_clean();
        $r02 = ExpressModel::where('is_open',1)->order('sort','desc')->select()->toArray();
        $data = array('sdata'=>$sdata,'yh_money'=>$yh_money,'zp_res'=>$zp_res,'update_s'=>$update_s,'data'=>$data,
            'detail'=>$detail,'reduce_price'=>(float)$reduce_price,'coupon_price'=>(float)$coupon_price,'preferential_amount'=>(float)$preferential_amount,
            'allow'=>$allow,'num'=>$num,'spz_price'=>(float)$spz_price,'discount_type'=>$discount_type,'id'=>$sNo,'expressStr'=>$expressStr,'isManyMch'=>$isManyMch,'z_freight'=>(float)$z_freight,'grade_rate'=>(float)$grade_rate,'grade_rate_amount'=>(float)$grade_rate_amount,'pay_price'=>(float)$pay_price,'express'=>$r02,'id'=>$sNo,'remarks'=>$remarks,'old_total'=>$old_total,'storeSelfInfo'=>$storeSelfInfo,'currency_code'=>$currency_code,'currency_symbol'=>$currency_symbol,'cpc'=>$cpc);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 获取快递信息
    public function SearchExpress()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $total = 0;
        $res_num = ExpressModel::where(['recycle'=>0,'is_open'=>1])->count();
        if($res_num)
        {
            $total = $res_num;
        }
        $list = array();
        $res = ExpressModel::where(['recycle'=>0,'is_open'=>1])->order('sort','desc')->select()->toArray();
        if($res)
        {
            $list = $res;
        }

        $message = Lang("Success");
        return output(200,$message,array('total'=>$total,'list'=>$list));
    }

    // 获取物流信息
    public function GetLogistics()
    {
        $store_id = addslashes($this->request->param('storeId'));

        $sNo = $this->request->param('sNo'); // 订单号

        $Express = new ExpressPublicMethod();
        $Express_list = $Express->GetLogistics(array('store_id'=>$store_id,'sNo'=>$sNo));
        return;
    }

    // 发货列表
    public function DeliverList()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));
        $mch_id = $this->user_list['id'];

        $sNo = $this->request->param('orderno'); // 订单号
        
        $logistics_type = false; // false：获取lkt_express  true：获取lkt_express_subtable

        $sql = "select o.p_sNo,o.otype,d.id,d.r_sNo,d.user_id,d.p_id,d.p_name,d.p_price,d.num,d.r_status,d.sid,d.size,p.imgurl,o.otype,d.deliver_num,o.mch_id from lkt_order_details as d left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on p.id=c.pid left join lkt_order as o on d.r_sNo=o.sNo where d.store_id = '$store_id' and d.r_sNo='$sNo' and d.r_status=1";
        $res = Db::query($sql);
        $put = 1;
        $mch_id = trim($res[0]['mch_id'],',');
        $logistics_type = PC_Tools::Determine_logistics_type($store_id,$mch_id);
        foreach ($res as $k => $v)
        {
            $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
            $v['deliverNum'] = $v['num'] - $v['deliver_num'];

            if ($v['otype'] == 'integral')
            {
                $integralid = $v['p_sNo'];
                $sql = "select g.integral,g.money,c.img from lkt_integral_goods as g left join lkt_configure as c on g.attr_id = c.id where g.id='$integralid'";
                $inr = Db::query($sql);
                if ($inr)
                {
                    $v['p_integral'] = $inr[0]['integral'];
                    $v['p_price'] = $inr[0]['money'];
                    $v['imgurl'] = ServerPath::getimgpath($inr[0]['img']);
                }
            }
            $res[$k] = $v;
            if ($v['r_status'] == 1)
            {
                $put = 0;
            }
            $oid = $v['id'];//详情ID
            //判断售后情况
            $res_s = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])
            ->where('r_type','in','0,1,3,11')
            ->select()
            ->toArray();
            if($res_s)
            {
                unset($res[$k]);
            }
        }
        $res = array_values($res);
        $express = ExpressModel::where(['is_open'=>1,'recycle'=>0])->order('sort','desc')->select()->toArray();
        $express_num = ExpressModel::where(['is_open'=>1,'recycle'=>0])->order('sort','desc')->count();
        $message = Lang("Success");
        return output(200,$message,array('goods'=>$res,'express'=>array('list'=>$express,'total'=>$express_num),'put'=>$put,'id'=>$sNo,'count'=>count($res),'logistics_type'=>$logistics_type));
    }

    // 统一发货
    public function UnifiedShipment()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId')); // 授权id

    	$list = trim($this->request->param('list'));
        $list = htmlspecialchars_decode($list);

        $shop_id = cache($access_id.'_'.$store_type);
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data_array = array('store_id'=>$store_id,'shop_id'=>$shop_id,'list'=>$list,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>2);
        $data = DeliveryHelper::UnifiedShipment($data_array);
        exit;
    }

    // 发货
    public function Deliver()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId')); // 授权id
        $shop_id = $this->user_list['id']; // 店铺ID

        $sNo = trim($this->request->param('sNo')); // 订单号
        $express_id = trim($this->request->param('expressId')); // 快递公司ID
        $courier_num = trim($this->request->param('courierNum')); // 快递单号
        $orderList_id = trim($this->request->param('orderListId')); // 发货数组
        $courier_name = trim($this->request->param('courier_name')); // 配送人姓名
        $phone = trim($this->request->param('phone')); // 配送人电话
        
        $user_id = $this->user_list['user_id'];
        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid');

        $time = date('Y-m-d H:i:s', time());
        $count = 0; //统计详细订单记录数
        $batchSend = false; //是否批量发货
        $len = 0; //选择的订单数
        $data_array = array();
        // $update_data = array();

        // 根据订单号，查询是否存在退货的商品
        $r2 = OrderModel::where(['sNo'=>$sNo])->field('otype,self_lifting')->select()->toArray();
        if ($r2)
        {
            $otype = $r2[0]['otype'];
            $self_lifting = $r2[0]['self_lifting'];
            if($self_lifting == 2)
            {
                if (empty($courier_name) || empty($phone))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,未选择快递公司！';
                    $this->mchLog($Log_content);
                    $message = Lang('order.37');
                    return output(ERROR_CODE_QXZKDGS,$message);
                }
            }
            else
            {
                if (!empty($express_id))
                {
                    // $update_data['express_id'] = $express_id;
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,未选择快递公司！';
                    $this->mchLog($Log_content);
                    $message = Lang('mch.23');
                    return output(ERROR_CODE_QXZKDGS,$message);
                }
                if (!empty($courier_num))
                {
                    // $r1 = OrderDetailsModel::where(['express_id'=>$express_id,'courier_num'=>$courier_num])
                    //                         ->field('id')
                    //                         ->select()
                    //                         ->toArray();
                    // if ($r1)
                    // {
                    //     $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,快递单号已存在！';
                    //     $this->mchLog($Log_content);
                    //     $message = Lang('mch.24');
                    //     return output(ERROR_CODE_KDDHYCZ,$message);
                    // }
                    // else
                    // {
                    //     $update_data['courier_num'] = $courier_num;
                    // }
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,未填写快递单号！';
                    $this->mchLog($Log_content);
                    $message = Lang('mch.26');
                    return output(ERROR_CODE_QSRKDDH,$message);
                }
            }

            $data_array = array('store_id'=>$store_id,'user_id'=>$user_id,'courier_num'=>$courier_num,'express_id'=>$express_id,'orderList_id'=>$orderList_id,'sNo'=>$sNo,'courier_name'=>$courier_name,'phone'=>$phone,'delivery_type'=>'front','shop_id'=>$shop_id,'operator'=>$operator,'source'=>2);
            $data = DeliveryHelper::frontDelivery_x($data_array);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,订单号错误,订单号为' . $sNo . '！';
            $this->mchLog($Log_content);
            $message = Lang('mch.27');
            return output(109,$message);
        }
    }

    // 批量发货
    public function BatchDelivery()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId')); // 授权id
        
        $user_id = $this->user_list['user_id'];
        $mch_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid');
        $Jurisdiction = new Jurisdiction();

        $time = date("Y-m-d H:i:s");

        $filename = $_FILES['image']['tmp_name'];
        $name = $_FILES['image']['name'];
        if (empty ($filename)) 
        {
            $message = Lang('product.91');
            return output(109, $message);
        }

        $handle = fopen($filename,'r');
        $result = $this->input_excel($filename);

        $len_result = count($result);
        if($len_result == 0)
        {
            $message = Lang('没有任何数据！');
            return output(109, $message,null);
        }

        foreach ($result as $k => $v) 
        {
            Db::startTrans();
            $sNo = $v['A']; // 订单号
            $did = $v['B']; // 订单详情ID
            $express_name = $v['C']; // 快递公司
            $courier_num = $v['D']; // 物流单号

            if($sNo)
            {
                // 根据订单号，查询订单信息
                $sql0 = "select mch_id from lkt_order where store_id = '$store_id' and sNo = '$sNo' ";
                $r0 = Db::query($sql0);
                if($r0)
                {
                    $shop_id = trim($r0[0]['mch_id'],','); // 店铺ID
                    if($shop_id != $mch_id)
                    { // 该订单所属店铺 与 操作店铺 不一致
                        Db::rollback();
                        $text = "第".$k."行订单号错误！";
                        $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                        $r_f = Db::execute($sql_f);
                        $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,2,$mch_id);
                        $message = Lang('订单号错误！');
                        return output(109, $message,null);
                    }
                }
                else
                {   
                    Db::rollback();
                    $text = "第".$k."行订单号错误！";
                    $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                    $r_f = Db::execute($sql_f);
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,2,$mch_id);
                    $message = Lang('订单号错误！');
                    return output(109, $message,null);
                }

                // 根据订单号、详情ID，查询订单详情
                $sql1 = "select * from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and id = '$did' ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    $cargo = $r1[0]['p_name'];
                    $z_num = $r1[0]['num']; // 数量
                    $deliver_num = $r1[0]['deliver_num']; // 发货数量
                    $r_status = $r1[0]['r_status']; // 订单状态

                    $d_num = $z_num - $deliver_num; // 待发货数量
                    
                    if($r_status == 2)
                    {
                        Db::rollback();
                        $text = "第".$k."行订单订单已发货！";
                        $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                        $r_f = Db::execute($sql_f);
                        $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0);
                        $message = Lang('订单订单已发货！');
                        return output(109, $message,null);
                    }
                }
                else
                {   
                    Db::rollback();
                    $text = "第".$k."行订单明细ID错误！";
                    $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                    $r_f = Db::execute($sql_f);
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,2,$mch_id);
                    $message = Lang('订单明细ID错误！');
                    return output(109, $message,null);
                }

                if(!empty($express_name))
                {
                    // 获取物流id
                    $sql_e = ExpressModel::where(['kuaidi_name'=>$express_name,'recycle'=>0,'is_open'=>1])->select()->toArray();
                    if(empty($sql_e))
                    {   
                        Db::rollback();
                        $text = "第".$k."行快递公司错误！";
                        $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                        $r_f = Db::execute($sql_f);
                        $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,2,$mch_id);
                        $message = Lang("快递公司错误!");
                        return output(109,$message,null);
                    }
                    else
                    {
                        $express_id = $sql_e[0]['id'];
                    }
                }
                else
                {   
                    Db::rollback();
                    $text = "第".$k."行快递公司为空！";
                    $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                    $r_f = Db::execute($sql_f);
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,2,$mch_id);
                    $message = Lang("快递公司错误!");
                    return output(109,$message,null);
                }
            }
            else
            {   
                Db::rollback();
                $text = "第".$k."行订单号为空！";
                $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                $r_f = Db::execute($sql_f);
                $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,2,$mch_id);
                $message = Lang('订单号为空！');
                return output(109, $message,null);
            }

            // 根据商城ID、店铺ID、快递公司ID，查询快递公司子表
            $sql2 = "select * from lkt_express_subtable where store_id = '$store_id' and mch_id = '$shop_id' and recovery = 0 and express_id = '$express_id' ";
            $r2 = Db::query($sql2);
            if($r2)
            { // 面单发货
                $array = array('store_id'=>$store_id,'sNo'=>$sNo,'express_id'=>$express_id,'cargo'=>$cargo,'source'=>1);
                $data = DeliveryHelper::Delivery_by_waybill($array);
                if(isset($data['code']))
                { // 面单发货失败
                    Db::rollback();
                    $text = "第".$k."行面单发货错误！";
                    $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                    $r_f = Db::execute($sql_f);
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,2,$mch_id);
                    return output(109,$data['message'],null);
                }
                $courier_num = $data['courier_num']; // 快递单号
                $childNum = $data['childNum']; // 子单号
                $returnNum = $data['returnNum']; // 回单号
                $label = $data['label']; // 面单短链
                $kdComOrderNum = $data['kdComOrderNum']; // 快递公司订单号
                $subtable_id = $data['subtable_id']; // 快递公司订单号

                $data_array = array('store_id'=>$store_id,'store_type'=>$store_type,'sNo'=>$sNo,'detailId'=>$did,'express_id'=>$express_id,'courier_num'=>$courier_num,'d_num'=>$d_num,'childNum'=>$childNum,'returnNum'=>$returnNum,'label'=>$label,'kdComOrderNum'=>$kdComOrderNum,'subtable_id'=>$subtable_id,'shop_id'=>0,'operator'=>$operator,'source'=>1);
                $res = DeliveryHelper::ShipmentModificationOrderStatus($data_array);
                if($res != 1)
                {
                    Db::rollback();
                    $text = "第".$k."行快递单号错误！";
                    $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                    $r_f = Db::execute($sql_f);
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,2,$mch_id);
                    $message = Lang("快递单号错误！");
                    return output(109,$message,null);
                }
            }
            else
            { // 普通发货
                if(!empty($courier_num) && strlen($courier_num) >= 10)
                {
                    $rr = OrderDetailsModel::where(['express_id'=>$express_id,'courier_num'=>$courier_num])->field('id')->select()->toArray();
                    if ($rr)
                    {   
                        Db::rollback();
                        $text = "第".$k."行快递单号重复！";
                        $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                        $r_f = Db::execute($sql_f);
                        $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,2,$mch_id);
                        $message = Lang("快递单号重复!");
                        return output(109,$message,null);
                    }
                }
                else
                {   
                    Db::rollback();
                    $text = "第".$k."行快递单号错误！";
                    $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                    $r_f = Db::execute($sql_f);
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,2,$mch_id);
                    $message = Lang("快递单号错误！");
                    return output(109,$message,null);
                }

                $data_array = array('store_id'=>$store_id,'store_type'=>$store_type,'sNo'=>$sNo,'detailId'=>$did,'express_id'=>$express_id,'courier_num'=>$courier_num,'d_num'=>$d_num,'childNum'=>'','returnNum'=>'','label'=>'','kdComOrderNum'=>'','subtable_id'=>0,'shop_id'=>0,'operator'=>$operator,'source'=>1);
                $res = DeliveryHelper::ShipmentModificationOrderStatus($data_array);
                if($res != 1)
                {
                    Db::rollback();
                    $text = "第".$k."行快递单号错误！";
                    $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                    $r_f = Db::execute($sql_f);
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,2,$mch_id);
                    $message = Lang("快递单号错误！");
                    return output(109,$message,null);
                }
            }
            Db::commit();
        }
        
        fclose($handle);//关闭指针
        // 保存记录
        $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',1,'','$mch_id','$time','$len_result')";
        $r_f = Db::execute($sql_f);

        $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作',2,2,$mch_id);
        $message = Lang('导入成功！');
        return output(200, $message,null);
    }

    // 批量发货日志
    public function DeliveryList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId')); // 授权id
        $mch_id = $this->user_list['id']; // 店铺ID

        $fileName  = trim($this->request->param('fileName')); // 名称
        $status = trim($this->request->param('status')); // 是否成功 0.失败 1.成功
        $startdate = $this->request->param("startDate");
        $enddate = $this->request->param("endDate"); 
        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $page = $this->request->param('pageNo');
        $exportType = $this->request->param('exportType');
        $pagesize = $pagesize ? ($pagesize == 'undefined' ? 10 : $pagesize) : 10;
        // 页码
        $page = $page ? $page : 1;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        $total = 0;
        $condition = " mch_id = '$mch_id' and is_del = 0 ";
        if($fileName)
        {
            $fileName_0 = Tools::FuzzyQueryConcatenation($fileName);
            $condition .= " and name like $fileName_0 ";
        }
        if(strlen($status) > 0)
        {
            $condition .= " and status = '$status' ";
        }
        if($startdate != '')
        {
            $condition .= " and add_date >= '$startdate' ";
        }
        if($enddate != '')
        {
            $condition .= " and add_date <= '$enddate' ";
        }
        $sql_num = " select ifnull(count(id),0) as num from lkt_file_delivery where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        $list = array();
        $sql = "select id,name,status,text,mch_id,add_date,order_num from lkt_file_delivery where $condition order by add_date desc limit $start,$pagesize ";
        $res = Db::query($sql);
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $status = $value['status'];
                if($status == 0)
                {
                    $res[$key]['statusName'] = "发货失败";
                }
                else
                {
                    $res[$key]['statusName'] = "发货成功";
                }
            }
            $list = $res;
        }
        //请求为导出
        if ($exportType)
        {   
            $titles = array(
                0 => '序号',
                1 => '文件ID',
                2 => '文件名称',
                3 => '文件状态',
                4 => '批量发货时间',
                5 => '失败原因',
            );
            $exportExcel_list = array();

            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $k+1,
                        $v['id'],
                        $v['name'],
                        $v['statusName'],
                        $v['add_date'],
                        $v['text']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '批量发货导入日志');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }
        $message = Lang('Success');
        return output(200, $message,array('total'=>$total,'list'=>$list));
    }

    // 删除日志
    public function DelDelivery()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId')); // 授权id
        $mch_id = $this->user_list['id']; // 店铺ID

        $id = $this->request->param("id");
        $sql = FileDeliveryModel::find($id);
        if($sql)
        {
            $sql->is_del = 1;
            $res = $sql->save();
            if(!$res)
            {
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }
        $message = Lang("Success");
        return output(200,$message);
    }

    public function input_excel($filename)
    {
        $out = array ();
        $n = 0;
        $reader = IOFactory::createReader('Xlsx'); // 先创建一个Reader对象
        $spreadsheet = $reader->load($filename); // 载入文件到Spreadsheet对象中

        $worksheet = $spreadsheet->getActiveSheet(); // 获取活动工作表

        $highestRow = $worksheet->getHighestRow(); // 获取最大行数
        $highestColumn = $worksheet->getHighestColumn(); // 获取最大列数

        // 从第2行开始遍历每一行
        for ($row = 2; $row <= $highestRow; ++$row) {
            // 从A列开始遍历每一列
            for ($col = 'A'; $col <= $highestColumn; ++$col) {
                $cell = $worksheet->getCell($col . $row); // 获取单元格对象
                $value = $cell->getValue(); // 获取单元格值
                if ($value) 
                {
                    $out[$row][$col] = $value;
                }
            }
        }
        return $out;
    }

    public function assoc_unique($arr,$key = 0) 
    {
        $tmp_arr = array();
        foreach ($arr as $k => $v) 
        {
            if (in_array($v[$key], $tmp_arr)) 
            {//搜索$v[$key]是否在$tmp_arr数组中存在，若存在返回true
                unset($arr[$k]);
            } 
            else 
            {
                $tmp_arr[] = $v[$key];
            }
        }
        sort($arr); //sort函数对数组进行排序
        return $arr;
    }

    // 编辑
    public function SaveEditOrder()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId')); // 授权id

        $admin_name = $this->user_list['name'];
        $sNo = $this->request->param('orderNo');
        $cpc = $this->request->param('cpc');
        $mobile = $this->request->param('tel');
        $name = $this->request->param('userName');
        $sheng = $this->request->param('shen');
        $shi = $this->request->param('shi');
        $xian = $this->request->param('xian');
        $address = $this->request->param('address');
        $remarks = $this->request->param('remarks');
        $orderAmt = $this->request->param('orderAmt');
        $status = $this->request->param('orderStatus');

        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid');

        $data = array();
        if(empty($sNo))
        {
            $message = Lang("Parameter error");
            return output(ERROR_CODE_CSCW,$message);
        }
        else
        {
            $data['sheng'] = $sheng;
            $data['shi'] = $shi;
            $data['xian'] = $xian;
            $data['name'] = $name;
            $data['cpc'] = $cpc;
            $data['mobile'] = $mobile;
            $data['address'] = $address;
            if($status)
            {
                $data['u_status'] = $status;
            }
            if($orderAmt)
            {
                $data['z_price'] = $orderAmt;
            }
            $remark = array('0'=>$remarks); 
            $data['remarks'] = serialize($remark);//订单备注
            $data['shop_id'] = $shop_id; // 店铺ID
            $data['operator'] = $operator; // user_id
            $data['source'] = 2; // user_id
        }
        if(empty($data))
        {   
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
        else
        {
            EditOrderStatus::update_order($store_id,$sNo,'backstage',$data);
        }
        exit;
    }

    // 查看物流
    public function Kuaidishow()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId')); // 授权id
        $sNo = $this->request->param('orderno');

        $array = array('store_id'=>$store_id,'sNo'=>$sNo);
        $list = PC_Tools::View_logistics($array);

        $data = array('list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 售后订单列表
    public function ReturnList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));
        $mch_id = cache($access_id.'_'.$store_type);
       
        $sNo = addslashes(trim($this->request->param('orderno'))); // 订单号
        $r_type = trim($this->request->param('orderStauts')); 
        $startdate = $this->request->param("startDate");
        $enddate = $this->request->param("endDate");   
        $page = $this->request->param('pageNo'); // 页码
        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? ($pagesize == 'undefined' ? 10 : $pagesize) : 10;
        
        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'sNo'=>$sNo,'mch_name'=>'','re_type'=>'','r_type'=>$r_type,'startdate'=>$startdate,'enddate'=>$enddate,'page'=>$page,'pagesize'=>$pagesize,'mch_id'=>$mch_id);
        $data = RefundUtils::After_sales_list($array);
        $message = Lang("Success");
        return output(200,$message,$data);
    }
    
    // 查看售后详情
    public function RefundPageById()
    {
        $mch_id = $this->user_list['id'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $id = $this->request->param('id');

        $array = array('store_id'=>$store_id,'id'=>$id);
        $data = RefundUtils::After_sales_details($array);

        $message = Lang("Success");
        return output(200,$message,$data);
    }

    //打印售后订单
    public function GetRefundList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $token = trim($this->request->param('accessId'));
        $mch_id = cache($token.'_'.$store_type);
        $lktlog = new LaiKeLogUtils();
        $sNo = addslashes(trim($this->request->param('orderno'))); // 订单号
        $startdate = $this->request->param("startDate");
        $enddate = $this->request->param("endDate");   
        $r_type = trim($this->request->param('orderStauts')); 
        $exportType = $this->request->param('exportType');        // 导出
        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $page = $this->request->param('pageNo');
        $pagesize = $pagesize ? ($pagesize == 'undefined' ? 10 : $pagesize) : 10;
        // 页码
        $page = $page ? $page : 1;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        } 

        $condition = " b.store_id = '$store_id' and a.otype = 'GM' and a.mch_id = ',$mch_id,' ";

        if ($sNo != '')    
        {
            $condition .= " and b.sNo like '%$sNo%' ";
        }
        if ($r_type == 7 && is_numeric($r_type))// 待审核    
        {    
         $condition .= " and b.r_type = 0 ";    
        }    
        else if ($r_type == 1)//退款中    
        {
            $condition .= " and b.r_type in (1,3) and b.re_type = 1 ";    
        }    
        else if ($r_type == 2)//退款成功   
        {    
            $condition .= " and b.r_type in (4,9) ";    
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

        $condition .= " and b.r_type != 100 and a.status != 8";
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
            $sql = "select b.id,b.pid,b.r_type,b.re_apply_money,b.re_money,b.re_photo,b.re_time,b.re_type,b.real_money,b.sNo,b.sid,c.size,b.user_id,lm.name as mchName,c.p_name,c.p_price,c.num,lpl.is_distribution,fig.img,c.unit from lkt_return_order as b
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
                    $list[$key]['imgurl'] = ServerPath::getimgpath($value['img'],$store_id);
                    if($value['r_type'] == 0)
                    {
                        $list[$key]['prompt'] = '待审核';
                    }
                    elseif(($value['re_type'] == 3 && $value['r_type'] == 1) || ($value['re_type'] == 3 && $value['r_type'] == 3) || ($value['re_type'] == 3 && $value['r_type'] == 11))
                    {
                        $list[$key]['prompt'] = '换货中';
                    }
                    elseif(($value['re_type'] == 1 && $value['r_type'] == 1) || ($value['re_type'] == 1 && $value['r_type'] == 3))
                    {
                        $list[$key]['prompt'] = '退款中';
                    }
                    elseif(($value['re_type'] == 3 && $value['r_type'] == 5) || $value['r_type'] == 10)
                    {
                        $list[$key]['prompt'] = '换货失败';
                    }
                    elseif(($value['re_type'] != 3 && $value['r_type'] == 5) || $value['r_type'] == 2 || $value['r_type'] == 8) 
                    {
                        $list[$key]['prompt'] = '退款失败';
                    }
                    elseif($value['r_type'] == 2 || $value['r_type'] == 9)
                    {
                        $list[$key]['prompt'] = '退款成功';
                    }
                    elseif($value['r_type'] == 12)
                    {
                        $list[$key]['prompt'] = '换货成功';
                    }

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
                    if($value['re_type'] == 1 && $value['r_type'] == 5)
                    {
                        $list[$key]['isManExamine'] = true;
                    }

                }
            }
        }
        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '序号',
                1 => '用户ID',
                2 => '产品名称',
                3 => '产品价格',
                4 => '数量',
                5 => '订单号',
                6 => '实退金额',
                7 => '发布时间',
                8 => '类型',
                10 => '状态'
            );
            $exportExcel_list = array();

            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $k+1,
                        $v['user_id'],
                        $v['p_name'],
                        $v['p_price'],
                        $v['num'],
                        $v['sNo'],
                        $v['real_money'],
                        $v['re_time'],
                        $v['returnTypeName'],
                        $v['prompt']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '售后列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }
        $data = array('total'=>$total,'list'=>$list);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 批量刪除
    public function DelOrder()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId')); // 授权id

        $sNo = $this->request->param('ordernos');

        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid');
        
        $array = array('store_id'=>$store_id,'sNo'=>$sNo,'operator'=>$operator,'shop_id'=>$shop_id,'source'=>2);
        EditOrderStatus::mch_del_order($array);
     
        $message = Lang("Success");
        return output(200,$message);
    }

    // 取消订单
    public function CancellationOfOrder()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $sNo = $this->request->param('sNo'); // 订单号
        $shop_id = cache($access_id.'_'.$store_type);

        $array = array('store_id'=>$store_id,'mch_id'=>$shop_id,'sNo'=>$sNo,'operator_source'=>2);
        RefundUtils::CancellationOfOrder($array);
        return;
    }

    // 售后审核
    public function Examine()
    {   
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $token = trim($this->request->param('accessId'));
        $mch_id = cache($token.'_'.$store_type);
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $shop_id = cache($token.'_'.$store_type);
        $operator = cache($token.'_uid');

        $id = $this->request->param('id');//售后id
        $m = intval($this->request->param('type'));
        $text = trim($this->request->param('text'));
        $price = trim($this->request->param('price'))?trim($this->request->param('price')) : 0;
        $express = trim($this->request->param('expressId'));
        $courier_num = trim($this->request->param('courierNum'));

        $action['store_id'] = $store_id;
        $action['admin_name'] = $admin_name;
        $action['id'] = $id; // 订单详情id
        $action['m'] = $m; // 退货类型
        $action['text'] = $text; // 拒绝理由
        $action['price'] = $price; // 退款金额
        $action['mch_id'] = $mch_id; // 店铺ID
        $action['express_id'] = $express; // 快递公司编号
        $action['courier_num'] = $courier_num; // 快递单号
        $action['shop_id'] = $shop_id; 
        $action['operator'] = $operator; 
        $action['source'] = 2; 
        //都走普通的订单插件逻辑后面可以吧订单类型传入而走各自的退款逻辑，主要是这块代码几乎都是一样的逻辑
        PluginUtils::invokeMethod('','refund',$action);
    }

    // 订单统计
    public function OrderCount()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $token = $this->request->param('accessId');
        $mch_id = $this->user_list['id'];

        $orderNum = 0; // 普通订单待发货的订单数量
        $shiWuNum = 0; // 普通订单待发货的订单数量
        $activityNum = 0; // 活动订单待发货的订单数量(排除普通订单)
        $returnNum = 0; // 统计 审核中 普通订单
        $secOrderNum = 0; // 秒杀订单总数
        $inOrderNum = 0; // 积分代发货
        $res = OrderModel::where(['store_id'=>$store_id,'otype'=>'GM','supplier_id'=>0])
                        ->where('recycle','<>',3)
                        ->where('status',1)
                        ->where('mch_id',','.$mch_id.',')
                        ->count();
        if($res)
        {
            $orderNum = $res;
            $shiWuNum = $res;
        }

        $res_ms = OrderModel::where(['store_id'=>$store_id,'otype'=>'MS'])
                        ->where('recycle','<>',1)
                        ->where('mch_id',','.$mch_id.',')
                        ->count();
        if($res_ms)
        {
            $secOrderNum = $res_ms;
        }

        $res_in = OrderModel::where(['store_id'=>$store_id,'otype'=>'IN'])
                        ->where('recycle','<>',1)
                        ->where('mch_id',','.$mch_id.',')
                        ->count();
        if($res_in)
        {
            $inOrderNum = $res_in;
        }

        $message = Lang("Success");
        return output(200,$message,array('orderNum'=>$orderNum,'activityNum'=>$activityNum,'inOrderNum'=>$inOrderNum,'returnNum'=>$returnNum,'secOrderNum'=>$secOrderNum,'shiWuNum'=>$shiWuNum));
    }

    // 订单打印
    public function OrderPrint()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $sNo = $this->request->param('sNo'); // 订单号
        $orders = explode(',', $sNo);
        $datalist = array();
        foreach ($orders as $key => $value) 
        {
            $this->store_id = $store_id;
            $this->id = $value;
            $this->order_type = 'see';
            $list = PluginUtils::invokeMethod('','order_details',$this);
            $sdata = $list['sdata'];
            $yh_money = $list['yh_money'];
            $zp_res = $list['zp_res'];
            $update_s = $list['update_s'];
            $data = $list['data'];
            $detail = $list['detail'];
            $reduce_price = $list['reduce_price'];
            $coupon_price = sprintf("%.2f",$list['coupon_price']);
            $preferential_amount = $list['preferential_amount'];
            $allow = sprintf("%.2f",$list['allow']);
            $num = $list['num'];
            $spz_price = $list['spz_price'];
            $discount_type = $list['discount_type'];
            $expressStr = $list['expressStr'];
            $isManyMch = $list['isManyMch'];
            $z_freight = $list['z_freight'];
            $grade_rate = $list['grade_rate'];
            $grade_rate_amount = $list['grade_rate_amount'];
            $pay_price = $list['pay_price'];
            $remarks = $list['remarks'];
            $supplier_id = $list['supplier_id'];
            ob_clean();
            $data = array('sdata'=>$sdata,'yh_money'=>$yh_money,'zp_res'=>$zp_res,'update_s'=>false,'data'=>$data,
                'detail'=>$detail,'reduce_price'=>(float)$reduce_price,'coupon_price'=>(float)$coupon_price,'preferential_amount'=>(float)$preferential_amount,
                'allow'=>$allow,'num'=>$num,'spz_price'=>(float)$spz_price,'discount_type'=>$discount_type,'id'=>$sNo,'expressStr'=>$expressStr,'isManyMch'=>$isManyMch,'z_freight'=>(float)$z_freight,'grade_rate'=>(float)$grade_rate,'grade_rate_amount'=>(float)$grade_rate_amount,'pay_price'=>(float)$pay_price,'id'=>$sNo,'remarks'=>$remarks,'supplier_id'=>$supplier_id);
            $datalist[$key]['list'] = $data;
        }
        $message = Lang("Success");
        return output(200,$message,$datalist);
    }
    
    // 验证提货码-获取商品信息
    public function getGoodsInfoByExtractionCode()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $token = trim($this->request->param('accessId'));

        $id = trim($this->request->param('orderId')); // 订单id
        $extraction_code = trim($this->request->param('extractionCode')); // 提货码

        $shop_id = cache($token.'_'.$store_type);
        $operator = cache($token.'_uid');

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'id'=>$id,'extraction_code'=>$extraction_code,'shop_id'=>$shop_id,'mch_store_id'=>0);
        $data = DeliveryHelper::Self_pickup_order_to_obtain_goods($array);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 验证提货码
    public function VerificationExtractionCode()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $token = trim($this->request->param('accessId'));

        $id = trim($this->request->param('orderId')); // 订单id
        $extraction_code = trim($this->request->param('extractionCode')); // 提货码

        $shop_id = cache($token.'_'.$store_type);
        $operator = cache($token.'_uid');

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'id'=>$id,'extraction_code'=>$extraction_code,'shop_id'=>$shop_id,'operator'=>$operator,'source'=>2);
        $data = DeliveryHelper::VerificationExtractionCode($array);
    }

    // 评论列表
    public function getCommentsInfo()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $token = trim($this->request->param('accessId'));

        $cid = $this->request->param('cid');//评论id
        $otype = $this->request->param('type') ? $this->request->param('type') : false; // 好评，中评，差评
        $search = $this->request->param('orderno')? $this->request->param('orderno') : false;
        $mchName = $this->request->param('mchName')? $this->request->param('mchName') : '';
        $startdate = $this->request->param("startDate");
        $enddate = $this->request->param("endDate");
        $page = $this->request->param('pageNo'); // 页码
        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? ($pagesize == 'undefined' ? 10 : $pagesize) : 10;
        
        $shop_id = cache($token.'_'.$store_type);
        $array = array('store_id'=>$store_id,'shop_id'=>$shop_id,'cid'=>$cid,'type'=>$otype,'orderType'=>'GM','search'=>$search,'startDate'=>$startdate,'endDate'=>$enddate,'page'=>$page,'pagesize'=>$pagesize,'mchName'=>$mchName,'source'=>2);
        $data = Comments::CommentsList($array);

        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 删除评论
    public function delComments()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $id = intval($this->request->param('commentId'));

        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid');
        
        $array = array('store_id'=>$store_id,'id'=>$id,'shop_id'=>$shop_id,'operator'=>$operator,'source'=>2);
        $data = Comments::delComments($array);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 查看明细
    public function getCommentsDetailInfoById()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $cid = $this->request->param('cid');//评论id
        $search = $this->request->param("key");//用户id
        $startdate = $this->request->param("startDate");
        $enddate = $this->request->param("endDate");
        $page = $this->request->param('pageNo'); // 页码
        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? ($pagesize == 'undefined' ? 10 : $pagesize) : 10;
       
        $array = array('store_id'=>$store_id,'cid'=>$cid,'search'=>$search,'startdate'=>$startdate,'enddate'=>$enddate,'page'=>$page,'pagesize'=>$pagesize);
        $data = Comments::getCommentsDetailInfoById($array);
        $message = Lang("Success");
        return output(200,$message,$data);
    }
    
    // 删除明细
    public function delCommentReply()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $id = $this->request->param('id');//回复id

        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid');

        $array = array('store_id'=>$store_id,'id'=>$id,'shop_id'=>$shop_id,'operator'=>$operator,'source'=>2);
        $data = Comments::delCommentReply($array);
        $message = Lang("Success");
        return output(200,$message);
    }

    // 修改评论
    public function updateCommentsDetailInfoById()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $id = addslashes(trim($this->request->param('cid')));
        $comment_input = addslashes(trim($this->request->param('commentText')));
        $comment_type = addslashes(trim($this->request->param('commentType')));
        $review = addslashes(trim($this->request->param('review')));
        $imgurls = $this->request->param('commentImgUrls');//逗号分隔
        $review_time_images = $this->request->param('reviewImgList');//逗号分隔

        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid');
        
        $array = array('store_id'=>$store_id,'id'=>$id,'comment_input'=>$comment_input,'comment_type'=>$comment_type,'review'=>$review,'imgurls'=>$imgurls,'review_time_images'=>$review_time_images,'shop_id'=>$shop_id,'operator'=>$operator,'source'=>2);
        $data = Comments::updateCommentsDetailInfoById($array);
        $message = Lang("Success");
        return output(200,$message);
    }

    // 提交回复
    public function replyComments()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId')); // 授权id

        $id = addslashes(trim($this->request->param('commentId')));
        $comment_input = addslashes(trim($this->request->param('commentText')));

        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid');
        
        $array = array('admin_list'=>$admin_list,'store_id'=>$store_id,'id'=>$id,'comment_input'=>$comment_input,'shop_id'=>$shop_id,'operator'=>$operator,'source'=>2);
        $data = Comments::replyComments($array);
        $message = Lang("Success");
        return output(200,$message);
    }

    // 店铺日志
    public function mchLog($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("pc/mch.log",$Log_content);
        return;
    }

    // 电子面单
    public function ShippingRecords()
    {   
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $express_name = $this->request->param('express_name'); // 快递单号、快递订单ID
        $sNo = $this->request->param('sNo'); // 订单号
        $status = $this->request->param('status'); // 是否打印 0.未打印 1.已打印
        $startdate = $this->request->param("startDate"); // 查询开始时间
        $enddate = $this->request->param("endDate"); // 查询结束时间
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据
        
        $shop_id = cache($access_id.'_'.$store_type);

        $array = array('store_id'=>$store_id,'express_name'=>$express_name,'sNo'=>$sNo,'mch_name'=>'','status'=>$status,'startdate'=>$startdate,'enddate'=>$enddate,'page'=>$page,'pagesize'=>$pagesize,'shop_id'=>$shop_id);

        $data = DeliveryHelper::ShippingRecords($array);
       
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 获取商品
    public function getPro()
    {   
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = $this->request->param('id'); // 发货记录ID
        $name = $this->request->param('name'); // 商品名称
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据

        $array = array('store_id'=>$store_id,'id'=>$id,'name'=>$name,'page'=>$page,'pagesize'=>$pagesize);

        $data = DeliveryHelper::getPro($array);
       
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 面单发货
    public function FaceSheetSend()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $id = trim($this->request->param('orderListId')); // 订单详情表id
        $express_id = trim($this->request->param('expressId'));//快递id
        $courier_num = trim($this->request->param('courierNum'));//快递单号
        $admin_name = $admin_list['name'];

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'admin_name'=>$admin_name,'id'=>$id,'express_id'=>$express_id,'courier_num'=>$courier_num,'source'=>'2');
        $data = DeliveryHelper::FaceSheetSend($array);

        exit;
    }

    // 取消电子面单
    public function CancelElectronicWaybill()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $id = trim($this->request->param('id')); // 发货记录id
        $admin_name = $admin_list['name'];

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'admin_name'=>$admin_name,'id'=>$id,'source'=>'2');
        $data = DeliveryHelper::CancelElectronicWaybill($array);

        exit;
    }

    /**
     * 发送微信订阅消息
     * @param $msgres
     * @param $oid
     * @param $courier_num
     * @param $store_id
     * @throws Exception
     */
    public function sendWXTopicMsg($msgres, $oid, $courier_num, $store_id)
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

    // 线下审核
    public function offlineReview()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId')); // 授权id

        $sNo = trim($this->request->param('sNo')); // 订单号
        $review_status = trim($this->request->param('review_status')); // 凭证审核状态 0.未上传凭证 1.待审核 2.通过 3.拒绝
        $reason_for_rejection = trim($this->request->param('reason_for_rejection')); // 拒绝原因

        $shop_id = cache($access_id.'_'.$store_type);
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'shop_id'=>$shop_id,'sNo'=>$sNo,'review_status'=>$review_status,'reason_for_rejection'=>$reason_for_rejection,'source'=>'2','operator_id'=>$operator_id,'operator'=>$operator);
        $data = DeliveryHelper::offlineReview($array);

        exit;
    }
}
