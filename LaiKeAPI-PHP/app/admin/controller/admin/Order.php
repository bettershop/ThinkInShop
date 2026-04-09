<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\GETUI\LaikePushTools;
use app\common\WXTopicMsgUtils;
use app\common\Plugin\PluginUtils;
use app\common\third\logistics\LogisticsTool;
use app\common\EditOrderStatus;
use app\common\ExcelUtils;
use app\common\DeliveryHelper;
use app\common\ExpressPublicMethod;
use app\common\Comments;
use app\common\Plugin\RefundUtils;
use PhpOffice\PhpSpreadsheet\IOFactory;

use app\admin\model\ReturnOrderModel;
use app\admin\model\ReturnRecordModel;
use app\admin\model\ReplyCommentsModel;
use app\admin\model\CommentsModel;
use app\admin\model\CommentsImgModel;
use app\admin\model\ExpressModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\OrderModel;
use app\admin\model\RecordModel;
use app\admin\model\ProductListModel;
use app\admin\model\ConfigureModel;
use app\admin\model\UserModel;
use app\admin\model\MchBrowseModel;
use app\admin\model\StockModel;
use app\admin\model\UserAddressModel;
use app\admin\model\FileDeliveryModel;
/**
 * 功能：后台订单类
 * 修改人：PJY
 */
class Order extends BaseController
{   
    //发货页面
    public function deliveryView()
    {   
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = $this->request->param('sNo'); // 订单号

        $list = array();
        $put = 1;
        $logistics_type = false; // false：获取lkt_express  true：获取lkt_express_subtable
        if($id != '')
        {
            $sql = "select o.p_sNo,o.otype,d.id,d.r_sNo,d.user_id,d.p_id,d.p_name,d.p_price,d.num,d.r_status,d.sid,d.size,p.imgurl,o.otype,d.deliver_num,b.brand_name,o.mch_id,o.exchange_rate from lkt_order_details as d left join lkt_configure as c on d.sid = c.id left join lkt_product_list as p on p.id=c.pid left join lkt_brand_class as b on p.brand_id = b.brand_id left join lkt_order as o on d.r_sNo=o.sNo where d.store_id = '$store_id' and d.r_sNo='$id' and d.r_status=1";
            $res = Db::query($sql);
            $mch_id = trim($res[0]['mch_id'],',');
            $logistics_type = PC_Tools::Determine_logistics_type($store_id,$mch_id);
            foreach ($res as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                $v['deliverNum'] = $v['num'] - $v['deliver_num'];
                $v['p_price'] = round($v['p_price'],2);

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
                $res_s = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->where('r_type','in','0,1,3,11')->select()->toArray();
                if($res_s)
                {
                    unset($res[$k]);
                }
            }
            $list = array_values($res);
        }
        $express = ExpressModel::where(['is_open'=>1,'recycle'=>0])->order('sort','desc')->select()->toArray();
        $express_num = ExpressModel::where(['is_open'=>1,'recycle'=>0])->order('sort','desc')->count();
        $message = Lang("Success");
        return output(200,$message,array('goods'=>$list,'express'=>array('list'=>$express,'total'=>$express_num),'put'=>$put,'id'=>$id,'count'=>count($list),'logistics_type'=>$logistics_type));
    }

    // 获取物流信息
    public function GetLogistics()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $sNo = $this->request->param('sNo'); // 订单号

        $Express = new ExpressPublicMethod();
        $Express_list = $Express->GetLogistics(array('store_id'=>$store_id,'sNo'=>$sNo));
        
        $message = Lang("Success");
        return output(200,$message,array('list'=>$Express_list));
    }

    // 统一发货
    public function UnifiedShipment()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));

    	$list = trim($this->request->param('list'));
        $list = htmlspecialchars_decode($list);

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data_array = array('store_id'=>$store_id,'shop_id'=>0,'list'=>$list,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $data = DeliveryHelper::UnifiedShipment($data_array);
        exit;
    }

    // 面单发货
    public function FaceSheetSend()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));
        $id = trim($this->request->param('orderDetailIds')); // 订单详情表id
        $express_id = trim($this->request->param('exId'));//快递id
        $courier_num = trim($this->request->param('exNo'));//快递单号
        $admin_name = $admin_list['name'];

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'admin_name'=>$admin_name,'id'=>$id,'express_id'=>$express_id,'courier_num'=>$courier_num,'source'=>'1','operator_id'=>$operator_id,'operator'=>$operator);
        $data = DeliveryHelper::FaceSheetSend($array);

        exit;
    }

    // 发货
    public function deliverySave()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));
        $id = trim($this->request->param('orderDetailIds')); // 订单详情表id
        $express_id = trim($this->request->param('exId'));//快递id
        $courier_num = trim($this->request->param('exNo'));//快递单号
        
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data_array = array('store_id'=>$store_id,'id'=>$id,'courier_num'=>$courier_num,'express_id'=>$express_id,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1,'admin_list'=>$admin_list);
        $data = DeliveryHelper::adminDelivery_x($data_array);

        exit;
    }

    // 商家配送订单-发货
    public function deliverySaveForStoreSelf()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));

        $sNo = trim($this->request->param('sNo')); // 订单号
        $courier_name = trim($this->request->param('courier_name')); // 快递人名称
        $phone = trim($this->request->param('phone')); // 快递人电话
        
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data_array = array('store_id'=>$store_id,'sNo'=>$sNo,'courier_name'=>$courier_name,'phone'=>$phone,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1,'admin_list'=>$admin_list);
        $data = DeliveryHelper::deliverySaveForStoreSelf($data_array);

        exit;
    }

    // 批量发货
    public function batchDelivery()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim($this->request->param('accessId')));
        $mch_id = cache($access_id.'_'.$store_type);

        $admin_name = $this->user_list['name'];
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

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
            $courier_num = '';
            if(isset($v['D']))
            {
                $courier_num = $v['D']; // 物流单号
            }

            if($sNo)
            {
                // 根据订单号，查询订单信息
                $sql0 = "select mch_id from lkt_order where store_id = '$store_id' and sNo = '$sNo' ";
                $r0 = Db::query($sql0);
                if($r0)
                {
                    $shop_id = trim($r0[0]['mch_id'],','); // 店铺ID
                }
                else
                {   
                    Db::rollback();
                    $text = "第".$k."行订单号错误！";
                    $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                    $r_f = Db::execute($sql_f);
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0,$operator_id);
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
                        $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0,$operator_id);
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
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0,$operator_id);
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
                        $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0,$operator_id);
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
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0,$operator_id);
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
                $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0,$operator_id);
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
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0,$operator_id);
                    return output(109,$data['message'],null);
                }
                $courier_num = $data['courier_num']; // 快递单号
                $childNum = $data['childNum']; // 子单号
                $returnNum = $data['returnNum']; // 回单号
                $label = $data['label']; // 面单短链
                $kdComOrderNum = $data['kdComOrderNum']; // 快递公司订单号
                $subtable_id = $data['subtable_id']; // 快递公司订单号

                $data_array = array('store_id'=>$store_id,'store_type'=>$store_type,'sNo'=>$sNo,'detailId'=>$did,'express_id'=>$express_id,'courier_num'=>$courier_num,'d_num'=>$d_num,'childNum'=>$childNum,'returnNum'=>$returnNum,'label'=>$label,'kdComOrderNum'=>$kdComOrderNum,'subtable_id'=>$subtable_id,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
                $res = DeliveryHelper::ShipmentModificationOrderStatus($data_array);
                if($res != 1)
                {
                    Db::rollback();
                    $text = "第".$k."行快递单号错误！";
                    $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                    $r_f = Db::execute($sql_f);
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0,$operator_id);
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
                        $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0,$operator_id);
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
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0,$operator_id);
                    $message = Lang("快递单号错误！");
                    return output(109,$message,null);
                }

                $data_array = array('store_id'=>$store_id,'store_type'=>$store_type,'sNo'=>$sNo,'detailId'=>$did,'express_id'=>$express_id,'courier_num'=>$courier_num,'d_num'=>$d_num,'childNum'=>'','returnNum'=>'','label'=>'','kdComOrderNum'=>'','subtable_id'=>0,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
                $res = DeliveryHelper::ShipmentModificationOrderStatus($data_array);
                if($res != 1)
                {
                    Db::rollback();
                    $text = "第".$k."行快递单号错误！";
                    $sql_f = "insert into lkt_file_delivery (store_id,name,status,text,mch_id,add_date,order_num) value ('$store_id','$name',0,'$text','$mch_id','$time','$len_result')";
                    $r_f = Db::execute($sql_f);
                    $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作失败',2,1,0,$operator_id);
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

        $Jurisdiction->admin_record($store_id, $operator, '进行了订单批量发货操作',2,1,0,$operator_id);
        $message = Lang('导入成功！');
        return output(200, $message,null);
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

    //批量发货日志
    public function deliveryList()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim($this->request->param('accessId')));
        $mch_id = cache($access_id.'_'.$store_type);

        $fileName  = trim($this->request->param('fileName')); // 编号
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
            $condition .= " and id = '$fileName' ";
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
        $message = Lang('Success');
        return output(200, $message,array('total'=>$total,'list'=>$list));
    }

    //删除日志
    public function delDelivery()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim($this->request->param('accessId')));
        $mch_id = cache($access_id.'_'.$store_type);

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

    // 关闭订单
    public function close()
    {
        $store_id = trim($this->request->post('storeId'));
        $store_type = trim($this->request->post('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));

        $sNo = $this->request->post('oid'); // 订单号
        
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'sNo'=>$sNo,'operator_id'=>$operator_id,'operator'=>$operator,'shop_id'=>0,'source'=>1);
        EditOrderStatus::closeOrder($array);
     
        $message = Lang("Success");
        return output(200,$message);
    }

    // 刪除订单
    public function del()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));

        $sNo = $this->request->param('orders'); // 订单号逗号隔开

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'sNo'=>$sNo,'operator_id'=>$operator_id,'operator'=>$operator,'shop_id'=>0,'source'=>1);
        EditOrderStatus::admin_del_order($array);
     
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

        $array = array('store_id'=>$store_id,'mch_id'=>0,'sNo'=>$sNo,'operator_source'=>1);
        RefundUtils::CancellationOfOrder($array);
        return;
    }

    //订单修改页
    public function editOrderView()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $sNo = $this->request->param('sNo'); // 订单号

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
        $old_freight = $list['old_freight'];
        $cpc = $list['cpc'];
        ob_clean();
        $r02 = ExpressModel::where('is_open',1)->order('sort','desc')->select()->toArray();
        $data = array('sdata'=>$sdata,'yh_money'=>$yh_money,'zp_res'=>$zp_res,'update_s'=>true,'data'=>$data,
            'detail'=>$detail,'reduce_price'=>(float)$reduce_price,'coupon_price'=>(float)$coupon_price,'preferential_amount'=>(float)$preferential_amount,
            'allow'=>$allow,'num'=>$num,'spz_price'=>(float)$spz_price,'discount_type'=>$discount_type,'id'=>$sNo,'expressStr'=>$expressStr,'isManyMch'=>$isManyMch,'z_freight'=>(float)$z_freight,'grade_rate'=>(float)$grade_rate,'grade_rate_amount'=>(float)$grade_rate_amount,'pay_price'=>(float)$pay_price,'express'=>$r02,'id'=>$sNo,'remarks'=>$remarks,'old_total'=>(float)$old_total,'old_freight'=>(float)$old_freight,'cpc'=>$cpc);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    //订单详情
    public function orderDetailsInfo()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $sNo = $this->request->param('sNo'); // 订单号

        $this->store_id = $store_id;
        $this->id = $sNo;
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
        $old_total = $list['old_total'];
        $old_freight = $list['old_freight'];
        $returnStatus = $list['returnStatus'];
        $selfLifting = $list['selfLifting'];
        $storeSelfInfo = $list['storeSelfInfo'];
        $currency_code = $list['currency_code'];
        $currency_symbol = $list['currency_symbol'];
        $exchange_rate = $list['exchange_rate'];
        ob_clean();
        $data = array('sdata'=>$sdata,'yh_money'=>$yh_money,'zp_res'=>$zp_res,'update_s'=>false,'data'=>$data,
            'detail'=>$detail,'reduce_price'=>(float)$reduce_price,'coupon_price'=>(float)$coupon_price,'preferential_amount'=>(float)$preferential_amount,
            'allow'=>$allow,'num'=>$num,'spz_price'=>(float)$spz_price,'discount_type'=>$discount_type,'id'=>$sNo,'expressStr'=>$expressStr,'isManyMch'=>$isManyMch,'z_freight'=>(float)$z_freight,'grade_rate'=>(float)$grade_rate,'grade_rate_amount'=>(float)$grade_rate_amount,'pay_price'=>(float)$pay_price,'id'=>$sNo,'remarks'=>$remarks,'old_total'=>(float)$old_total,'old_freight'=>(float)$old_freight,'returnStatus'=>$returnStatus,'selfLifting'=>$selfLifting,'storeSelfInfo'=>$storeSelfInfo,'currency_code'=>$currency_code,'currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    //订单列表
    public function index()
    {
        $admin_list = $this->user_list;
        $this->admin = $admin_list;
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $this->store_id = $store_id;
        $store_type = trim($this->request->param('storeType'));
        $this->store_type = $store_type;
        $Jurisdiction = new Jurisdiction();
        $access_id = trim($this->request->param('accessId'));
        $shop_id = cache($access_id.'_'.$store_type); //店铺ID
        
        $selfLifting = trim($this->request->param('selfLifting')); // 
        $this->selfLifting = $selfLifting;

        $mch_name = trim($this->request->param('mchName')); // 请输入店铺名称
        $this->mch_name = $mch_name;
        $operation_type  = trim($this->request->param('operationType')); // 下单类型1用户下单2店铺下单3平台下单
        $this->operation_type  = $operation_type ;
        $status = trim($this->request->param('status')); // 订单状态
        $this->status = $status;
        $news_status = trim($this->request->param('news_status')); // 订单状态
        $this->news_status = $news_status;
        $delivery_status = trim($this->request->param('delivery_status')); // 提醒发货
        $this->delivery_status = $delivery_status;
        $readd = trim($this->request->param('readd')); // 未查看信息
        $this->readd = $readd;
        $mch_id = trim($this->request->param('mch_id')); // 店铺ID
        $this->mch_id = $mch_id;
        $x_order = trim($this->request->param('x_order')); // 店铺ID
        $this->x_order = $x_order;
        $sNo = trim($this->request->param('keyWord'));//查询
        $id = trim($this->request->param('id'));//订单号
        
        if($id)
        {
            $sNo = $id;
        }
        $this->sNo = $sNo;
        $brand = trim($this->request->param('brand'));
        $this->brand = $brand;
        $otype = 'GM';//订单类型
        $this->otype = $otype;
        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $this->pagesize = $pagesize;
        $page = $this->request->param('pageNo');
        $this->page = $page;
        $source = trim($this->request->param('source'));
        $this->source = $source;
        $startdate = $this->request->param("startDate");
        $this->startdate = $startdate;
        $enddate = $this->request->param("endDate");
        $this->enddate = $enddate;
        $this->Servertype = 'pc';
        $order_label = $this->request->param("selfLifting");//1普通订单2自提订单3虚拟订单4活动订单
        $this->order_label = $order_label;
        $exportType = $this->request->param('exportType');//是否导出
        $this->exportType = $exportType;
        $list = PluginUtils::invokeMethod('','order_index',$this);
        $info = $list['list'];
        $total = $list['total'];
        ob_clean();
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

            if ($list['info'])
            {
                foreach ($list['info'] as $k => $v)
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
                        $v['detailFreight']
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
        return output(200,$message,array('list'=>$info,'info'=>$list['info'],'total'=>$total));
    }

    //快递查看
    public function kuaidishow()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $r_sNo = $this->request->param('orderno'); // 订单号

        $array = array('store_id'=>$store_id,'sNo'=>$r_sNo);
        $list = PC_Tools::View_logistics($array);
        $data = array('list'=>$list);

        $message = Lang("Success");
        return output(200,$message,$data);
    }

    //保存编辑
    public function saveEditOrder()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));

        $name = $this->request->param('userName');
        $sheng = $this->request->param('shen');
        $shi = $this->request->param('shi');
        $xian = $this->request->param('xian');
        $address = $this->request->param('address');
        $cpc = $this->request->param('cpc');
        $mobile = $this->request->param('tel');
        $status = $this->request->param('orderStatus');
        $z_price = $this->request->param('orderAmt');
        $remarks = $this->request->param('remarks');
        $sNo =  $this->request->param('orderNo');

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data = array();
        if(empty($sNo) || empty($name) || empty($sheng) || empty($shi) || empty($xian) || empty($address) || (empty($cpc) && empty($mobile)))
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
            if($z_price)
            {
                $data['z_price'] = $z_price;
            }
            $remark = array('0'=>$remarks);
            $data['remarks'] = serialize($remark);//订单备注
            $data['shop_id'] = 0; // 店铺ID
            $data['operator_id'] = $operator_id;
            $data['operator'] = $operator; // user_id
            $data['source'] = 1; // user_id
        }
        if(empty($data))
        {
            $message = Lang("Parameter error");
            return output(ERROR_CODE_CSCW,$message);
        }
        else
        {
           EditOrderStatus::update_order($store_id,$sNo,'backstage',$data);
        }
    }

    // 售后订单列表
    public function getRefundList()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));
        $mch_id = cache($access_id.'_'.$store_type);
       
        $id = $this->request->param("id"); // 售后id
        $sNo = addslashes(trim($this->request->param('orderno'))); // 订单号
        $mch_name = $this->request->param('mchName'); // 店铺名称
        $re_type = $this->request->param('reType'); // 店铺ID
        $r_type = trim($this->request->param('status')); 
        $startdate = $this->request->param("startDate");
        $enddate = $this->request->param("endDate");   
        $page = $this->request->param('pageNo'); // 页码
        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? ($pagesize == 'undefined' ? 10 : $pagesize) : 10;

        if($id)
        { // 查看售后详情
            $array = array('store_id'=>$store_id,'id'=>$id);
            $data = RefundUtils::After_sales_details($array);
            $message = Lang("Success");
            return output(200,$message,$data);
        }
        else
        { // 售后订单列表
            $array = array('store_id'=>$store_id,'store_type'=>$store_type,'otype'=>'GM','sNo'=>$sNo,'mch_name'=>$mch_name,'re_type'=>$re_type,'r_type'=>$r_type,'startdate'=>$startdate,'enddate'=>$enddate,'page'=>$page,'pagesize'=>$pagesize,'mch_id'=>$mch_id);
            $data = RefundUtils::After_sales_list($array);
            $message = Lang("Success");
            return output(200,$message,$data);
        }
    }

    // 审核售后
    public function examine()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));

        $id = $this->request->param('id');
        $m = intval($this->request->param('type'));
        $text = trim($this->request->param('text'));
        $price = trim($this->request->param('price'))?trim($this->request->param('price')) : 0;
        $express = trim($this->request->param('expressId'));
        $courier_num = trim($this->request->param('courierNum'));

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $action['store_id'] = $store_id;
        $action['admin_name'] = $admin_name;
        $action['id'] = $id; // 订单详情id
        $action['m'] = $m; // 退货类型
        $action['text'] = $text; // 拒绝理由
        $action['price'] = $price; // 退款金额
        $action['mch_id'] = 0; // 店铺ID
        $action['express_id'] = $express; // 快递公司编号
        $action['courier_num'] = $courier_num; // 快递单号
        $action['shop_id'] = 0; 
        $action['operator_id'] = $operator_id; 
        $action['operator'] = $operator; 
        $action['source'] = 1; 

        //都走普通的订单插件逻辑后面可以吧订单类型传入而走各自的退款逻辑，主要是这块代码几乎都是一样的逻辑
        PluginUtils::invokeMethod('','refund',$action);
    }

    // 评论列表
    public function getCommentsInfo()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));
        $shop_id = cache($access_id.'_'.$store_type);

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
        
        $shop_id = cache($access_id.'_'.$store_type);
        $array = array('store_id'=>$store_id,'shop_id'=>$shop_id,'cid'=>$cid,'type'=>$otype,'orderType'=>'GM','search'=>$search,'startDate'=>$startdate,'endDate'=>$enddate,'page'=>$page,'pagesize'=>$pagesize,'mchName'=>$mchName,'source'=>1);
        $data = Comments::CommentsList($array);

        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 删除评论
    public function delComments()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));

        // 接收信息
        $id = intval($this->request->param('commentId'));
        
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'id'=>$id,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
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
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));

        $id = $this->request->param('id');//回复id

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'id'=>$id,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $data = Comments::delCommentReply($array);
        
        $message = Lang('Success');
        return output(200,$message);
    }

    // 修改评论     
    public function updateCommentsDetailInfoById()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));

        $id = addslashes(trim($this->request->param('cid')));
        $comment_input = addslashes(trim($this->request->param('commentText')));
        $comment_type = addslashes(trim($this->request->param('commentType')));
        $review = addslashes(trim($this->request->param('review')));
        $imgurls = $this->request->param('commentImgUrls');//逗号分隔
        $review_time_images = $this->request->param('reviewImgList');//逗号分隔

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'id'=>$id,'comment_input'=>$comment_input,'comment_type'=>$comment_type,'review'=>$review,'imgurls'=>$imgurls,'review_time_images'=>$review_time_images,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
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
    	$access_id = addslashes($this->request->param('accessId'));

        $id = addslashes(trim($this->request->param('commentId')));
        $comment_input = addslashes(trim($this->request->param('commentText')));

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        
        $array = array('admin_list'=>$admin_list,'store_id'=>$store_id,'id'=>$id,'comment_input'=>$comment_input,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $data = Comments::replyComments($array);
        $message = Lang("Success");
        return output(200,$message);
    }

    //订单统计
    public function orderCount()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $access_id = $this->request->post('accessId');
        $mch_id = cache($access_id.'_'.$store_type);

        $condition = " o.store_id = '$store_id' and lu.store_id = '$store_id' and o.recycle != 1 and o.supplier_id = 0 and o.store_recycle = 1 ";
        if($store_type != 8)
        {
            $condition .= " and p.mch_id = '$mch_id' ";
        }
        //全部
        $orderNum = 0;
        $sql0 = "select ifnull(count(a.sNo),0) as num from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                right join lkt_mch as m on p.mch_id = m.id
                where $condition and o.otype in('GM','VI') and o.status in (1,8) group by o.sNo) a";
        $res0 = Db::query($sql0);
        if($res0)
        {
            $orderNum = $res0[0]['num'];
        }
        //实物
        $num = 0;
        $sql = "select ifnull(count(a.sNo),0) as num from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                right join lkt_mch as m on p.mch_id = m.id
                where $condition and o.otype = 'GM' and o.self_lifting = 0 and o.status = 1 group by o.sNo) a";
        $res = Db::query($sql);
        if($res)
        {
            $num = $res[0]['num'];
        }

        //活动
        $activityNum = 0;
        $sql1 = "select ifnull(count(a.sNo),0) as num from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                right join lkt_mch as m on p.mch_id = m.id
                where $condition and o.otype not in('GM','VI') and o.self_lifting = 0 group by o.sNo) a";
        $res1 = Db::query($sql1);
        if($res1)
        {
            $activityNum = $res1[0]['num'];
        }

        $VINum = 0; // 虚拟订单数量
        $sql2 = "select ifnull(count(a.sNo),0) as num from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                right join lkt_mch as m on p.mch_id = m.id
                where $condition and o.otype = 'VI' and o.status = 8 group by o.sNo) a";
        $res2 = Db::query($sql2);
        if($res2)
        {
            $VINum = $res2[0]['num'];
        }

        $ziTiNum = 0; // 自提订单数量
        $sql3 = "select ifnull(count(a.sNo),0) as num from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                right join lkt_mch as m on p.mch_id = m.id
                where $condition and o.otype = 'GM' and o.self_lifting = 1 and o.status = 2 group by o.sNo) a";
        $res3 = Db::query($sql3);
        if($res3)
        {
            $ziTiNum = $res3[0]['num'];
        }

        $ziPeiNum = 0; // 配送订单数量
        $sql4 = "select ifnull(count(a.sNo),0) as num from (select o.sNo
                from lkt_order as o 
                left join lkt_user as lu on o.user_id = lu.user_id 
                right join lkt_order_details as d on o.sNo = d.r_sNo
                RIGHT JOIN lkt_configure attr ON attr.id=d.sid
                RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                right join lkt_mch as m on p.mch_id = m.id
                where $condition and o.otype = 'GM' and o.self_lifting = 2 and o.status = 1 group by o.sNo) a";
        $res4 = Db::query($sql4);
        if($res4)
        {
            $ziPeiNum = $res4[0]['num'];
        }

        //售后订单
        $returnNum = 0;
        $sql5 = "select ifnull(count(1),0) as num from lkt_return_order as b
                left join lkt_order_details as c on b.p_id = c.id
                left join lkt_order as a on c.r_sNo = a.sNo 
                left join lkt_configure as fig on b.sid = fig.id
                LEFT JOIN lkt_product_list as lpl on lpl.id = fig.pid
                LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id
                where b.store_id = '$store_id' and a.otype = 'GM' and b.r_type in(0,1,3,11) and b.r_type = 0";
        $res5 = Db::query($sql5);
        if($res5)
        {
            $returnNum = $res5[0]['num'];
        }

        


        $message = Lang("Success");
        return output(200,$message,array('shiWuNum'=>$num,'orderNum'=>$orderNum,'activityNum'=>$activityNum,'returnNum'=>$returnNum,'VINum'=>$VINum,'ziTiNum'=>$ziTiNum,'ziPeiNum'=>$ziPeiNum));
    }

    //订单打印
    public function orderPrint()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $sNo = $this->request->param('sNo'); // 订单号
        $orders = explode(',', $sNo);
        $datalist = array();

        //打印配置
        $str = "id,store_id as storeId,mch_id as mchId,print_name as printName,print_url as printUrl,sheng as printSheng,shi as printShi,xian as printxian,address as printAddress,phone as printPhone,add_time as addTime";
        $r2 = Db::name('print_setup')->where(['store_id'=>$store_id])->field($str)->select()->toArray();
        $printName = isset($r2[0]['printName'])?$r2[0]['printName']:'';
        $printUrl = isset($r2[0]['printUrl'])?$r2[0]['printUrl']:'';
        $printSheng = isset($r2[0]['printSheng'])?$r2[0]['printSheng']:'';
        $printShi = isset($r2[0]['printShi'])?$r2[0]['printShi']:'';
        $printxian = isset($r2[0]['printxian'])?$r2[0]['printxian']:'';
        $printAddress = isset($r2[0]['printAddress'])?$r2[0]['printAddress']:'';
        $printPhone = isset($r2[0]['printPhone'])?$r2[0]['printPhone']:'';

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
            $old_total = $list['old_total'];
            $old_freight = $list['old_freight'];
            $otype = $list['otype'];
            ob_clean();
            $data = array('sdata'=>$sdata,'yh_money'=>$yh_money,'zp_res'=>$zp_res,'update_s'=>false,'data'=>$data,
                'detail'=>$detail,'reduce_price'=>(float)$reduce_price,'coupon_price'=>(float)$coupon_price,'preferential_amount'=>(float)$preferential_amount,
                'allow'=>$allow,'num'=>$num,'spz_price'=>(float)$spz_price,'discount_type'=>$discount_type,'id'=>$sNo,'expressStr'=>$expressStr,'isManyMch'=>$isManyMch,'z_freight'=>(float)$z_freight,'grade_rate'=>(float)$grade_rate,'grade_rate_amount'=>(float)$grade_rate_amount,'pay_price'=>(float)$pay_price,'id'=>$sNo,'remarks'=>$remarks,'otype'=>$otype,'printName'=>$printName,'printUrl'=>$printUrl,'printSheng'=>$printSheng,'printShi'=>$printShi,'printxian'=>$printxian,'printAddress'=>$printAddress,'printPhone'=>$printPhone,'operator'=>$admin_name,'old_total'=>(float)$old_total,'old_freight'=>(float)$old_freight);
            $datalist[$key]['list'] = $data;
        }
        $message = Lang("Success");
        return output(200,$message,$datalist);
    }

    // 获取核销门店
    public function getMch_store()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $mchId = trim($this->request->param('mchId')); // 店铺ID
        $sNo = trim($this->request->param('sNo')); // 订单号
        $pid = trim($this->request->param('pid')); // 商品ID

        $total = 0;
        $list = array();
        $con = " b.mch_id = '$mchId' ";
        if($sNo != '')
        {
            $con .= " and a.r_sNo = '$sNo' ";
        }
        if($pid != '')
        {
            $con .= " and a.p_id = '$pid' ";
        }
        $sql0 = "select b.write_off_mch_ids from lkt_order_details as a left join lkt_product_list as b on a.p_id = b.id where ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $write_off_mch_ids = $r0[0]['write_off_mch_ids'];
            if($write_off_mch_ids == 0)
            {
                $sql1 = "select id,name,sheng,shi,xian,address from lkt_mch_store where store_id = '$store_id' and mch_id = '$mchId' order by is_default desc ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    $total = count($r1);
                    foreach($r1 as $k1 => $v1)
                    {
                        $list[] = array('id'=>$v1['id'],'name'=>$v1['name'],'address'=>$v1['sheng'] . $v1['shi'] . $v1['xian'] . $v1['address']);
                    }
                }
            }
            else
            {
                $write_off_mch_ids1 = explode(',',$write_off_mch_ids);
                $total = count($write_off_mch_ids1);
                $sql1 = "select id,name,sheng,shi,xian,address from lkt_mch_store where store_id = '$store_id' and mch_id = '$mchId' and id in ($write_off_mch_ids) order by is_default desc ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    foreach($r1 as $k1 => $v1)
                    {
                        $list[] = array('id'=>$v1['id'],'name'=>$v1['name'],'address'=>$v1['sheng'] . $v1['shi'] . $v1['xian'] . $v1['address']);
                    }
                }
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 虚拟订单-获取商品信息
    public function testExtractionCode()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $id = trim($this->request->param('orderId')); // 订单id
        $mch_store_id = trim($this->request->param('mch_store_id')); // 店铺门店id
        $extraction_code = trim($this->request->param('extractionCode')); // 提货码
        
        $shop_id = cache($access_id.'_'.$store_type);

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'id'=>$id,'extraction_code'=>$extraction_code,'shop_id'=>$shop_id,'mch_store_id'=>$mch_store_id);
        $data = DeliveryHelper::Self_pickup_order_to_obtain_goods($array);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 验证提货码
    public function VerificationExtractionCode()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $id = trim($this->request->param('orderId')); // 订单id
        $extraction_code = trim($this->request->param('extractionCode')); // 提货码

        $shop_id = cache($access_id.'_'.$store_type);
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'id'=>$id,'extraction_code'=>$extraction_code,'shop_id'=>0,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $data = DeliveryHelper::VerificationExtractionCode($array);
    }

    // 代客下单
    public function helpOrder()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim($this->request->param('accessId')));
        $admin_name = $this->user_list['name'];
        $admin_type1 = $this->user_list['type'];

        $user_id = $this->request->param('userId');
        $products = $this->request->param('products');
        $wipe_off = $this->request->param('wipeOff'); // 立减
        $address_id = $this->request->param('addressId'); // 地址ID
        $isOfflinePayment = $this->request->param('isOfflinePayment'); // 0.线下 1余额

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $products = json_decode(urldecode($products),true);
        $lktlog = new LaiKeLogUtils();
        //获取会员信息
        $res_u = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('money,preferred_currency')->select()->toArray();
        $user_money = $res_u[0]['money'];
        $preferred_currency = $res_u[0]['preferred_currency'];

        $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>$preferred_currency));
        $currency_symbol = $userCurrency[0]['currency_symbol'];
        $exchange_rate = $userCurrency[0]['exchange_rate'];
        $currency_code = $userCurrency[0]['currency_code'];

        $Toosl = new Tools(1,1);
        $sNo = $Toosl->Generate_order_number('GM', 'sNo'); // 生成订单号
        $real_sno = $Toosl->Generate_order_number('GM', 'real_sno'); // 生成支付订单号
        $remarks_0 = array();
        $remarks = serialize($remarks_0);
        // $address_id = '';
        $z_price = 0;//订单金额
        $spz_price = 0;//商品总价
        $z_num = 0;//总数量
        if($products != '')
        {   
            $products0 = $this->products_list($store_id,$products);
            $products_total = 0;
            $products_data = Tools::get_products_data($store_id,$products0, $products_total, 'GM');
            $products0 = $products_data['products'];
            $products_freight = $products_data['products_freight'];
            $products_total = $products_data['products_total'];
            $no_delivery_str = '';
            if($address_id == '')
            { // 获取不配送省的名称
                $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);
            }
            //查询默认地址order_details
            $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);
            if(empty($address))
            {
                $message = Lang("nomal_order.1");
                return output(400,$message);
            }
            $products_data0 = Tools::get_products_data0($store_id, $products0,$products_total, $user_id);
            $grade_rate = $products_data0['grade_rate'];
            $products0 = $products_data0['products'];
            
            $yunfei = $zfreight = 0;
            //4.计算运费
            $freight = Tools::get_freight($products_freight, $products0, $address, $store_id, 'GM');
            $yunfei = $freight['yunfei'];
            //获取默认地址
            $res_add = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id,'is_default'=>1])->select()->toArray();
            if($res_add)
            {
                $name = $res_add[0]['name'];
                $mobile = $res_add[0]['tel'];
                $sheng = $res_add[0]['sheng'];
                $shi = $res_add[0]['city'];
                $xian = $res_add[0]['quyu'];
                $address = $res_add[0]['address'];
                $address_id = $res_add[0]['id'];
            }
            else
            {
                $message = Lang("nomal_order.1");
                return output(400,$message);
            }
            $mch_id = '';
            foreach ($products as $key => $value) 
            {   
                $pro_id = $value['pid'];
                $size_id = $value['id'];
                $num = $value['num'];
                $z_num += $num;
                $sql_p = "select m.product_title,c.price,c.attribute,m.mch_id,c.unit from lkt_product_list AS m LEFT JOIN lkt_configure AS c ON m.id = c.pid  where m.store_id = '$store_id' and m.id = '$pro_id' and c.id = '$size_id'";
                $res_p = Db::query($sql_p);
                $attribute = unserialize($res_p[0]['attribute']);
                $value['size']= '';
                foreach ($attribute as $ka => $va)
                {
                    if (strpos($ka, '_LKT_') !== false)
                    {
                        $ke = substr($ka, 0, strrpos($ka, "_LKT"));
                        $va = substr($va, 0, strrpos($va, "_LKT"));
                        $value['size'] .= $ke . ":" . $va . ";";
                    }
                    else
                    {
                        $value['size'] .= $ka . ":" . $va . ";";
                    }
                }
                $value['product_title'] = $res_p[0]['product_title'];
                $value['unit'] = $res_p[0]['unit'];
                $value['p_price'] = $res_p[0]['price'];
                $value['mch_id'] = $res_p[0]['mch_id'];
                $value['sp_price'] = round($res_p[0]['price'] * $num,2);
                $products[$key] = $value;
                $spz_price += round($res_p[0]['price'] * $num,2);
                $mch_id .= ','.$res_p[0]['mch_id'];
            }
            $mch_id1 = explode(',', $mch_id);  
            $mch_id1 = array_unique($mch_id1);//内置数组去重算法  
            $mch_id1 =implode(',', array_filter($mch_id1));
            $mch_id = ','.$mch_id1.',';
            Db::startTrans();
            if($wipe_off > $spz_price)
            {
                $message = Lang("order.31");
                return output(400,$message);
            }
            foreach ($products as $key => $value) 
            {
                $manual_offer = 0;//手动优惠金额
                $after_discount = 0;//优惠后金额
                $sp_price = $value['sp_price'];
                if($wipe_off > 0)
                {
                    $manual_offer = round($sp_price/$spz_price*$wipe_off,2);
                }
                //如果是多店铺，添加一条购买记录
                $sql = $this->addMchBrowse(1, $store_id, $value['mch_id'], $user_id, $lktlog);
                $after_discount = $sp_price - $manual_offer;
                $sql_d = new OrderDetailsModel();
                $sql_d->store_id = $store_id;
                $sql_d->user_id = $user_id; 
                $sql_d->p_id = $value['pid'];
                $sql_d->p_name = $value['product_title'];
                $sql_d->p_price = $value['p_price'];
                $sql_d->freight = $value['freight'];
                $sql_d->num = $value['num'];
                $sql_d->unit = $value['unit'];
                $sql_d->r_sNo = $sNo;
                $sql_d->add_time = date('Y-m-d H:i:s');
                $sql_d->r_status = 1;
                $sql_d->size = $value['size'];
                $sql_d->sid = $value['id'];
                $sql_d->manual_offer = $manual_offer;
                $sql_d->after_discount = $after_discount;
                $sql_d->save();
                $beres = $sql_d->id;
                // 如果添加失败
                if ($beres < 1)
                {
                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加订单详情失败！json:" . json_encode($value));
                    // 回滚事件，给提示
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$user_id.' 进行了代客下单失败',1,1,0,$operator_id);
                    ob_clean();
                    $message = Lang("nomal_order.6");
                    return output(400,$message);
                }
                $zfreight += $value['freight'];//计算代客下单总运费
                $r0 = ConfigureModel::where(['id'=>$value['id'],'pid'=>$value['pid']])->field('num,min_inventory')->select()->toArray();
                $total_num = $r0[0]['num'];
                $min_inventory = $r0[0]['min_inventory'];
                //非会员特惠商品才减库存
                // 销量+1 库存-1
                $sql_p = ProductListModel::find($value['pid']);
                if($sql_p)
                {
                    $sql_p->num = Db::raw('num - '.$value['num']);
                    $sql_p->real_volume = Db::raw('real_volume + '.$value['num']);
                    $res_del1 = $sql_p->save();
                    if (!$res_del1)
                    {
                        $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改商品库存失败！id:" . $value['pid']);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$user_id.' 进行了代客下单失败',1,1,0,$operator_id);
                        ob_clean();
                        $message = Lang("nomal_order.6");
                        return output(400,$message);
                    }
                }
                $sql_c = ConfigureModel::find($value['id']);
                if($sql_c)
                {
                    $sql_c->num = Db::raw('num - '.$value['num']);
                    $res_del2 = $sql_c->save();
                    if(!$res_del2)
                    {
                        $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改商品属性库存失败！id:" . $value['id']);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$user_id.' 进行了代客下单失败',1,1,0,$operator_id);
                        ob_clean();
                        $message = Lang("nomal_order.6");
                        return output(400,$message);
                    }
                }

                $content = $user_id . '生成订单所需' . $value['num'];
                $sql_s = new StockModel();
                $sql_s->store_id = $store_id;
                $sql_s->product_id = $value['pid'];
                $sql_s->attribute_id = $value['id'];
                $sql_s->total_num = $total_num;
                $sql_s->flowing_num = $value['num'];
                $sql_s->type = 1;
                $sql_s->user_id = $user_id;
                $sql_s->add_date = date('Y-m-d H:i:s');
                $sql_s->content = $content;
                $sql_s->save();

                if($total_num - $num <= $min_inventory)
                {
                    $content1 = '预警';
                    // 在库存记录表里，添加一条预警信息
                    $sql_stock1 = new StockModel();
                    $sql_stock1->store_id = $store_id;
                    $sql_stock1->product_id = $value['pid'];
                    $sql_stock1->attribute_id = $value['id'];
                    $sql_stock1->total_num = $total_num;
                    $sql_stock1->flowing_num = $min_inventory;
                    $sql_stock1->type = 2;
                    $sql_stock1->add_date = date('Y-m-d H:i:s');
                    $sql_stock1->content = $content;
                    $sql_stock1->save();

                    $message_9 = "商品ID为".$value['pid']."的商品库存不足，请尽快补充库存";
                    $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>$value['mch_id'],'type'=>9,'parameter'=>$value['id'],'content'=>$message_9);
                    PC_Tools::add_message_logging($message_logging_list9);
                }

            }
            $z_price = round($spz_price,2) - $wipe_off + $zfreight;

            $pay = 'offline_support';
            if($isOfflinePayment != 0)
            {
                $pay = 'wallet_pay';
                if($user_money < $z_price)
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$user_id.' 进行了代客下单失败',1,1,0,$operator_id);
                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "余额不足！");
                    ob_clean();
                    $message = Lang("pay.4");
                    return output(400,$message);
                }
            }
            $sql_o = new OrderModel();
            $sql_o->store_id = $store_id;
            $sql_o->user_id = $user_id;
            $sql_o->name = $name;
            $sql_o->mobile = $mobile;
            $sql_o->num = $z_num;
            $sql_o->z_price = $z_price;
            $sql_o->sNo = $sNo;
            $sql_o->sheng = $sheng;
            $sql_o->shi = $shi;
            $sql_o->xian = $xian;
            $sql_o->address = $address;
            $sql_o->pay = $pay;
            $sql_o->add_time = date('Y-m-d H:i:s');
            $sql_o->status = 1;
            $sql_o->spz_price = $spz_price;
            $sql_o->source = 6;
            $sql_o->otype = 'GM';
            $sql_o->mch_id = $mch_id;
            $sql_o->p_sNo = '';
            $sql_o->bargain_id = 0;
            $sql_o->comm_discount = 0;
            $sql_o->remarks = $remarks;
            $sql_o->real_sno = $real_sno;
            $sql_o->self_lifting = 0;
            $sql_o->extraction_code = '';
            $sql_o->extraction_code_img = '';
            $sql_o->grade_rate = 10;
            $sql_o->z_freight = $zfreight;
            $sql_o->preferential_amount = $wipe_off;
            $sql_o->single_store = 0;
            $sql_o->coupon_price = $wipe_off;
            $sql_o->manual_offer = $wipe_off;
            $sql_o->operation_type = 3;
            $sql_o->currency_symbol = $currency_symbol;
            $sql_o->exchange_rate = $exchange_rate;
            $sql_o->currency_code = $currency_code;
            $sql_o->save();
            $r_o = $sql_o->id;
            if($r_o < 1)
            {   
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "生成主订单失败！json:" . json_encode($products));
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$user_id.' 进行了代客下单失败',1,1,0,$operator_id);
                ob_clean();
                $message = Lang("nomal_order.6");
                return output(400,$message);
            }

            if($isOfflinePayment != 0)
            {
                $sql_up = UserModel::where(['user_id'=>$user_id,'store_id'=>$store_id])->find();
                if($sql_up)
                {
                    $sql_up->money = Db::raw('money - '.$z_price);
                    $res_up = $sql_up->save();
                    if(!$res_up)
                    {
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$user_id.' 进行了代客下单失败',1,1,0,$operator_id);
                        $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "更新会员余额失败！user_id:" . $user_id);
                        ob_clean();
                        $message = Lang("nomal_order.6");
                        return output(400,$message);
                    }
                }

                $array = array('store_id'=>$store_id,'money'=>$z_price,'user_money'=>$user_money,'type'=>7,'money_type'=>2,'money_type_name'=>10,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'','currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code);
                $details_id = PC_Tools::add_Balance_details($array);

                //消费记录
                $event = $user_id . '使用了' . $z_price . '余额';
                // 添加一条记录
                $sql1 = new RecordModel();
                $sql1->store_id = $store_id;
                $sql1->user_id = $user_id;
                $sql1->money = $z_price;
                $sql1->oldmoney = $user_money;
                $sql1->event = $event;
                $sql1->type = 11;
                $sql1->details_id = $details_id;
                $sql1->save();
                $rr = $sql1->id;
                if($rr < 1)
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$user_id.' 进行了代客下单失败',1,1,0,$operator_id);
                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加消费记录失败！user_id:" . $user_id);
                    ob_clean();
                    $message = Lang("nomal_order.6");
                    return output(400,$message);
                }
            }
            
            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$user_id.' 进行了代客下单',1,1,0,$operator_id);
            $arr = array('sNo' => $sNo, 'total' => $z_price, 'order_id' => $r_o);
            ob_clean();
            $message = Lang("Success");
            return output(200,$message,$arr);
        }
        else
        {
            $message = Lang("Parameter error");
            return output(400,$message);
        }
    }

    //区分购物车结算和立即购买---列出选购商品
    public function products_list($store_id,$product)
    {
        $list = array();
        $list0 = array();
        $list1 = array();
        $list2 = array();

        foreach($product as $k => $v)
        {
            if(in_array($v['pid'],$list0))
            {
                if($v['num'] > $list1[$v['pid']])
                {
                    $list1[$v['pid']] = $v['num'];
                }
                $list[$v['pid']] += $v['num'];
            }
            else
            {
                $list0[] = $v['pid'];
                $list[$v['pid']] = $v['num'];
                $list1[$v['pid']] = $v['num'];
            }
        }

        foreach($product as $k => $v)
        {
            if($v['num'] == $list1[$v['pid']])
            {
                if(!in_array($v['pid'],$list2))
                {
                    $list2[] = $v['pid'];
                    $product[$k]['tongbu'] = 1;
                }
                else
                {
                    $product[$k]['tongbu'] = 2;
                }
            }
            else
            {
                $product[$k]['tongbu'] = 2;
            }
            $product[$k]['merge_num'] = $list[$v['pid']];
            $product[$k]['cid'] = $v['id'];
        }

        return $product;
    }

    /**
     * @param $mch_id
     * @param $store_id
     * @param $mch_id1
     * @param $user_id
     * @param $db
     * @param LaiKeLogUtils $lktlog
     * @return string
     */
    public function addMchBrowse($mch_id, $store_id, $mch_id1, $user_id, LaiKeLogUtils $lktlog)
    {
        if ($mch_id != 0)
        {
            $sql = new MchBrowseModel();
            $sql->store_id = $store_id;
            $sql->mch_id = $mch_id1;
            $sql->user_id = $user_id;
            $sql->event = '购买了商品';
            $sql->add_time = date('Y-m-d H:i:s');
            $sql->save();
            $res = $sql->id;
            if ($res < 1)
            {
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加购买记录失败！user_id:" . $user_id);
            }
        }
        return $sql;
    }
    
    // 电子面单
    public function ShippingRecords()
    {   
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $express_name = $this->request->param('express_name'); // 快递单号、快递订单ID
        $sNo = $this->request->param('sNo'); // 订单号
        $mch_name = $this->request->param('mch_name'); // 店铺名称
        $status = $this->request->param('status'); // 是否打印 0.未打印 1.已打印
        $startdate = $this->request->param("startDate"); // 查询开始时间
        $enddate = $this->request->param("endDate"); // 查询结束时间
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据

        $array = array('store_id'=>$store_id,'express_name'=>$express_name,'sNo'=>$sNo,'mch_name'=>$mch_name,'status'=>$status,'startdate'=>$startdate,'enddate'=>$enddate,'page'=>$page,'pagesize'=>$pagesize);

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

    // 取消电子面单
    public function CancelElectronicWaybill()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $id = trim($this->request->param('id')); // 发货记录id
        $admin_name = $admin_list['name'];
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'admin_name'=>$admin_name,'id'=>$id,'source'=>'1','operator_id'=>$operator_id,'operator'=>$operator);
        $data = DeliveryHelper::CancelElectronicWaybill($array);

        exit;
    }

    // 线下审核
    public function offlineReview()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $sNo = trim($this->request->param('sNo')); // 订单号
        $review_status = trim($this->request->param('review_status')); // 凭证审核状态 0.未上传凭证 1.待审核 2.通过 3.拒绝
        $reason_for_rejection = trim($this->request->param('reason_for_rejection')); // 拒绝原因

        $array = array('store_id'=>$store_id,'shop_id'=>0,'admin_name'=>$admin_name,'sNo'=>$sNo,'review_status'=>$review_status,'reason_for_rejection'=>$reason_for_rejection,'source'=>'1','operator_id'=>$operator_id,'operator'=>$operator);
        $data = DeliveryHelper::offlineReview($array);

        exit;
    }
}