<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cache;
use think\facade\Request;
use app\common\ExcelUtils;
use app\common\ServerPath;
use app\common\Jurisdiction;
use app\common\Tools;

use app\admin\model\MchModel;
use app\admin\model\InvoiceInfoModel;
use app\admin\model\UserModel;

/**
 * 功能：PC店鋪发票管理
 * 修改人：PJY
 */
class Invoice extends BaseController
{
    //列表
    public function GetList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $mch_id = cache($access_id.'_'.$store_type);
        $exportType = addslashes($this->request->param('exportType')); // 导出
        $invoice_status = addslashes(trim($this->request->param('invoiceStatus')));//发票状态 1.申请中 2.已完成 3.已撤销
        $type = addslashes(trim($this->request->param('type')));//抬头类型 1.企业 2.个人
        $search = trim($this->request->param('condition'));//发票抬头，税号，订单号
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize:'10';
        if($page)
        {
            $start = ($page - 1)*$pagesize;
        }
        else
        {
            $start = 0;
        }

        $total = 0;
        $condition = " a.store_id = '$store_id' and a.mch_id = '$mch_id' and a.recovery = 0 ";
        if($type)
        {
            $condition .= " and a.type = '$type' ";
        }
        if($invoice_status)
        {
            $condition .= " and a.invoice_status = '$invoice_status' ";
        }
        if($search)
        {
            $search_0 = Tools::FuzzyQueryConcatenation($search);
            $condition .= " and (a.company_name like $search_0 or a.company_tax_number = '$search' or a.s_no like $search_0 ) ";
        }
        $sql_num = "select ifnull(count(a.id),0) as num from lkt_invoice_info as a left join lkt_user as b on a.user_id = b.user_id where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        $sql = "select a.*,b.user_name from lkt_invoice_info as a left join lkt_user as b on a.user_id = b.user_id where $condition order by a.add_time desc limit $start,$pagesize";
        $res = Db::query($sql);
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $invoice_status = $value['invoice_status'];
                if($invoice_status == 1)
                {
                    $res[$key]['invoiceStatusDesc'] = '申请中';
                }
                elseif($invoice_status == 2)
                {
                    $res[$key]['invoiceStatusDesc'] = '已完成';
                }
                else
                {
                    $res[$key]['invoiceStatusDesc'] = '已撤销';
                }
                $i_type = $value['type'];
                if($i_type == 1)
                {
                    $res[$key]['typeDesc'] = '企业';
                }
                else
                {
                    $res[$key]['typeDesc'] = '个人';
                }
            }
            $list = $res;
        }
        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '发票id',
                1 => '昵称',
                2 => '抬头类型',
                3 => '发票抬头',
                4 => '税号',
                5 => '电子邮箱',
                6 => '开票状态',
                7 => '订单号',
                8 => '发票金额',
                9 => '提交时间',
                10 => '上传时间'
            );
            $exportExcel_list = array();

            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['id'],
                        $v['user_name'],
                        $v['typeDesc'],
                        $v['company_name'],
                        $v['company_tax_number'],
                        $v['email'],
                        $v['invoiceStatusDesc'],
                        $v['s_no'],
                        $v['invoice_amount'],
                        $v['add_time'],
                        $v['file_time'],
                    );
                }
            }
            ExcelUtils::exportExcel($exportExcel_list, $titles, '发票列表');
            exit;
        }
        $data = array('list' => $list, 'total' => $total);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    //发票详情
    public function GetInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $mch_id = cache($access_id.'_'.$store_type);
        $id = $this->request->param('id');//开票id

        $list = array();
        $res = InvoiceInfoModel::where(['store_id'=>$store_id,'id'=>$id,'recovery'=>0,'mch_id'=>$mch_id])->select()->toArray();
        if($res)
        {
            //用户信息
            $user_id = $res[0]['user_id'];
            $res_u = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('user_name')->select()->toArray();
            $res[0]['user_name'] = $res_u[0]['user_name'];
            if($res[0]['type'] == 1)
            {
                $res[0]['typeDesc'] = '企业';
            }
            else
            {
                $res[0]['typeDesc'] = '个人';
            }
            $res[0]['file'] = ServerPath::getimgpath($res[0]['file'],$store_id);
            if($res[0]['invoice_status'] == 1)
            {
                $res[0]['invoiceStatusDesc'] = '申请中';
            }
            elseif($res[0]['invoice_status'] == 2)
            {
                $res[0]['invoiceStatusDesc'] = '已完成';
            }
            else
            {
                $res[0]['invoiceStatusDesc'] = '已撤销';
            }
            $res[0]['invoice_header'] = json_decode($res[0]['invoice_header']);
            $list = $res[0];
        }
        $message = Lang('Success');
        return output(200, $message,$list);
    }

    //上传发票
    public function UploadInvoice()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = $this->request->param('id');//开票id
        $file = $this->request->param('filePath');//发票地址
        
        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        if(empty($file))
        {
            $message = Lang('mch.76');
            return output(400, $message);
        }
        else
        {
            $file = preg_replace('/.*\//', '', $file);
        }

        $sql = InvoiceInfoModel::find($id);
        if($sql)
        {
            $sql->file = $file;
            $sql->file_time = date('Y-m-d H:i:s');
            $sql->invoice_status = 2;
            $res = $sql->save();
            if(!$res)
            {
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }

            $Jurisdiction->admin_record($store_id, $operator, '上传发票,ID为' . $id, 2,2,$shop_id,$operator_id);
            $message = Lang('Success');
            return output(200, $message);
        }
        else
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }
    }

    //删除
    public function DelInvoice()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = $this->request->param('ids');//开票id

        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $sql = InvoiceInfoModel::find($id);
        if($sql)
        {
            $sql->recovery = 1;
            $res = $sql->save();
            if(!$res)
            {
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }

            $Jurisdiction->admin_record($store_id, $operator, '删除发票,ID为' . $id, 3,2,$shop_id,$operator_id);
            $message = Lang('Success');
            return output(200, $message);
        }
        else
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }
    }
}
