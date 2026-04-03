<?php
namespace app\admin\controller\mch\App;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cache;
use think\facade\Request;
use app\common\ExcelUtils;
use app\common\ServerPath;
use app\common\Plugin\MchPublicMethod;
use app\common\Tools;

use app\admin\model\MchModel;
use app\admin\model\InvoiceInfoModel;
use app\admin\model\UserModel;

/**
 * 功能：移动端店鋪发票管理
 * 修改人：PJY
 */
class Invoice extends BaseController
{
   
    // 列表
    public function GetList()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        
        $invoice_status = addslashes(trim($this->request->param('invoiceStatus'))); // 发票状态 1.申请中 2.已开票 3.已撤销
        $search = trim($this->request->param('condition'));//发票抬头，税号，订单号

        $page = trim($this->request->param('page')); // 页码
        $pagesize = trim($this->request->param('pagesize')); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $user_id = $this->user_list['user_id'];

        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " a.store_id = '$store_id' and a.mch_id = '$shop_id' and a.recovery = 0 ";
        
        if($invoice_status)
        {
            $condition .= " and a.invoice_status = '$invoice_status' ";
        }
        if($search)
        {
            $search_0 = Tools::FuzzyQueryConcatenation($search);
            $condition .= " and (a.company_name like $search_0 or a.company_tax_number like $search_0 or a.s_no like $search_0 ) ";
        }

        $total = 0;
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
                    $value['invoiceStatusDesc'] = '申请中';
                }
                else if($invoice_status == 2)
                {
                    $value['invoiceStatusDesc'] = '已开票';
                }
                else
                {
                    $value['invoiceStatusDesc'] = '已撤销';
                }
                $list[] = $value;
            }
        }

        $data = array('list' => $list, 'total' => $total);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 发票详情
    public function GetInfo()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = $this->request->param('id');//开票id

        $user_id = $this->user_list['user_id'];

        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $list = array();
        $res = InvoiceInfoModel::where(['store_id'=>$store_id,'id'=>$id,'recovery'=>0,'mch_id'=>$shop_id])->select()->toArray();
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

    // 上传发票
    public function UploadInvoice()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = $this->request->param('id');//开票id
        $file = $this->request->param('filePath');//发票地址

        $user_id = $this->user_list['user_id'];

        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

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
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = $this->request->param('ids');//开票id

        $user_id = $this->user_list['user_id'];

        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);
        
        $sql0 = "select * from lkt_invoice_info where id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $sql = "update lkt_invoice_info set recovery = 1 where id = '$id' ";
            $res = Db::execute($sql);
            if(!$res)
            {
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }
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
