<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Log;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;

use app\admin\model\UserModel;
use app\admin\model\InvoiceHeaderModel;
use app\admin\model\InvoiceInfoModel;
use app\admin\model\OrderModel;
use app\admin\model\MchModel;

class InvoiceInfo extends BaseController
{
    //待开票
    public function getToInvoiced()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));

        $user_id = $this->user_list['user_id'];

        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'))?trim($this->request->param('pageSize')):10;
        $start = ($page-1) * $pagesize;

        $time = date('Y-m-d H:i:s'); // 当前时间
        $time_0 = date('Y-m-d H:i:s',strtotime("-20 day",strtotime($time))); // 20天前的时间

        $total = 0;
        $sql_num = "select ifnull(count(id),0) as num from lkt_order where store_id = '$store_id' and recycle = 0 and status = 5 and settlement_status = 1 and user_id = '$user_id' and z_price > 0 and arrive_time > '$time_0' and sNo not in (select s_no from lkt_invoice_info where user_id = '$user_id' and recovery = 0)";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        $list = array();
        $sql = "select add_time,mch_id,old_total,id,sNo,settlement_status,store_id,user_id,z_freight,z_price from lkt_order where store_id = '$store_id' and recycle = 0 and status = 5 and settlement_status = 1 and user_id = '$user_id' and z_price > 0 and arrive_time > '$time_0' and sNo not in (select s_no from lkt_invoice_info where user_id = '$user_id' and recovery = 0 ) order by add_time desc limit $start,$pagesize";
        $res = Db::query($sql);
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $list[$key]['add_time'] = $value['add_time'];
                $list[$key]['old_total'] = $value['old_total'];
                $list[$key]['order_id'] = $value['id'];
                $list[$key]['mch_id'] = $value['mch_id'];
                $list[$key]['sNo'] = $value['sNo'];
                $list[$key]['settlement_status'] = $value['settlement_status'];
                $list[$key]['store_id'] = $value['store_id'];
                $list[$key]['user_id'] = $value['user_id'];
                $list[$key]['z_freight'] = $value['z_freight'];
                $list[$key]['z_price'] = number_format($value['z_price'] - $value['z_freight'],2);
                //获取店铺信息
                $mch_id = substr($value['mch_id'], 1, -1);
                $list[$key]['shop_id'] = $mch_id;
                $mch_res = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name,logo,is_invoice')->select()->toArray();
                if($mch_res)
                {
                    $list[$key]['shop_name'] = $mch_res[0]['name'];
                    $list[$key]['shop_logo'] = ServerPath::getimgpath($mch_res[0]['logo'],$store_id);
                    if($mch_res[0]['is_invoice'] == 0)
                    {
                        unset($list[$key]);
                    }
                }
            }
            sort($list);
        }
        $message =  Lang('Success');
        return output(200,$message,array('list' => $list,'total'=>$total));
    }
    
    //申请开票
    public function applyInvoicing()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));

        $user_id = $this->user_list['user_id'];
        $id = trim($this->request->param('id'));//开票id
        $headId = trim($this->request->param('headId'));//抬头id
        $type = trim($this->request->param('type'));//抬头类型 1.企业 2.个人
        $company_name = trim($this->request->param('companyName'));//公司名称(抬头名称)
        $company_tax_number = trim($this->request->param('companyTaxNumber'));//公司税号
        $amount = trim($this->request->param('amount'));//发票金额
        $sNo = trim($this->request->param('sNo'));//订单号
        $email = trim($this->request->param('email'));//邮箱

        //获取订单信息
        $res_o = OrderModel::where(['store_id'=>$store_id,'status'=>5,'settlement_status'=>1,'user_id'=>$user_id,'sNo'=>$sNo])->where('recycle','<>',2)->field('mch_id')->select()->toArray();
        if(empty($res_o))
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }
        $mch_id = substr($res_o[0]['mch_id'], 1, -1);
        if(empty($id))
        {
            $sql0 = "select * from lkt_invoice_info where store_id = '$store_id' and s_no = '$sNo' and recovery = 0 ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $message = Lang('invoice.4');
                return output(109,$message);
            }

            $sql = new InvoiceInfoModel();
            $sql->store_id = $store_id;
            $sql->mch_id = $mch_id;
            $sql->user_id = $user_id;
            $sql->type = $type;
            $sql->company_name = $company_name;
            if($type == 1)
            {
                $sql->company_tax_number = $company_tax_number;
            }
            //获取抬头快照
            $res_h = InvoiceHeaderModel::where(['store_id'=>$store_id,'id'=>$headId])->select()->toArray();
            if(empty($res_h))
            {
                $message = Lang('invoice.3');
                return output(ERROR_CODE_CSCW, $message);
            }
            $sql->invoice_header = json_encode($res_h[0]);
            $sql->s_no = $sNo;
            $sql->invoice_amount = $amount;
            $sql->email = $email;
            $sql->add_time = date('Y-m-d H:i:s');
            $sql->save();
            $id = $sql->id;
            if($id < 1)
            {
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB,$message);
            }
        }
        else
        {
            $sql = InvoiceInfoModel::find($id);
            $sql->type = $type;
            $sql->company_name = $company_name;
            if($type == 1)
            {
                $sql->company_tax_number = $company_tax_number;
            }
            //获取抬头快照
            $res_h = InvoiceHeaderModel::where(['store_id'=>$store_id,'id'=>$headId])->select()->toArray();
            if(empty($res_h))
            {
                $message = Lang('invoice.3');
                return output(ERROR_CODE_CSCW, $message);
            }
            $sql->invoice_header = json_encode($res_h[0]);
            $sql->s_no = $sNo;
            $sql->invoice_amount = $amount;
            $sql->email = $email;
            $sql->invoice_status = 1;
            $res = $sql->save();
            if(!$res)
            {
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB,$message);
            }
        }
        
        $message = Lang('Success');
        return output(200,$message);
    }

    //撤销开票
    public function revoke()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));

        $user_id = $this->user_list['user_id'];
        $id = trim($this->request->param('id'));//开票id

        $sql = InvoiceInfoModel::find($id);
        if($sql)
        {
            $sql->invoice_status = 3;
            $res = $sql->save();
            if(!$res)
            {
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB,$message);
            }
        }
        $message = Lang('Success');
        return output(200,$message);
    }

    //申请列表
    public function getInvoiceInfo()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));

        $user_id = $this->user_list['user_id'];
        $status = trim($this->request->param('status'));//发票状态 1.申请中 2.已完成 3.已撤销
        $page = trim($this->request->param('pageNo'))?trim($this->request->param('pageNo')):1;//页码
        $pagesize = trim($this->request->param('pageSize'))?trim($this->request->param('pageSize')):10;
        $start = ($page - 1) * $pagesize;

        $total = 0;
        $total = InvoiceInfoModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0,'invoice_status'=>$status])->count();
        
        $list = array();
        $res = InvoiceInfoModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0,'invoice_status'=>$status])->select()->toArray();
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $sNo = $value['s_no'];
                //获取订单信息
                $res_o = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->field('add_time,arrive_time,id')->select()->toArray();
                $res[$key]['order_id'] = $res_o[0]['id'];
                $arrive_time = $res_o[0]['arrive_time'];
                $add_time = $res_o[0]['add_time'];
                $time = date('Y-m-d H:i:s',strtotime("+20 day",strtotime($arrive_time)));
                if($time < date('Y-m-d H:i:s'))
                {
                    $res[$key]['invoiceTimeout'] = true;
                }
                else
                {
                    $res[$key]['invoiceTimeout'] = false;
                }
                $res[$key]['add_time'] = $add_time;
                $mch_id = $value['mch_id'];
                //获取店铺信息
                $res_m = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('logo,name')->select()->toArray();
                $res[$key]['shop_id'] = $mch_id;
                $res[$key]['shop_logo'] = ServerPath::getimgpath($res_m[0]['logo'],$store_id);
                $res[$key]['shop_name'] = $res_m[0]['name'];
                $res[$key]['user_name'] = $this->user_list['user_name'];
                $res[$key]['file'] = ServerPath::getimgpath($value['file'],$store_id);
            }
            $list = $res;
        }
        $message =  Lang('Success');
        return output(200,$message,array('list' => $list,'total'=>$total));
    }

    //发票详情
    public function getDetails()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));

        $user_id = $this->user_list['user_id'];
        $id = trim($this->request->param('id'));//开票id
        $res = InvoiceInfoModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'id'=>$id])->select()->toArray();
        if($res)
        {
            $message = Lang('Success');
            return output(200,$message,$res[0]);
        }
        else
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/invoice.log",$Log_content);
        return;
    }
}
