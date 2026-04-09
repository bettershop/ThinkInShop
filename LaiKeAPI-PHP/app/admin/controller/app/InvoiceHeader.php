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

class InvoiceHeader extends BaseController
{
    //发票抬头管理
    public function getList()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $id = trim($this->request->param('id'));

        $user_id = $this->user_list['user_id'];
        $condition = array('store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0);
        if($id != '')
        {
            $condition['id'] = $id;
        }
        $total = InvoiceHeaderModel::where($condition)->count();

        $list = array();
        $res = InvoiceHeaderModel::where($condition)
                                ->order('is_default','desc')
                                ->order('add_time','desc')
                                ->select()
                                ->toArray();
        if($res)
        {
            $list = $res;
        }

        $message = Lang('Success');
        return output(200, $message,array('list'=>$list,'total'=>$total));
    }

    //添加、编辑发票抬头
    public function addOrUpdate()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $user_id = $this->user_list['user_id'];

        $id = trim($this->request->param('id'));//抬头id
        $type = trim($this->request->param('type'));//抬头类型 1.企业 2.个人
        $company_name = trim($this->request->param('companyName'));//公司名称(抬头名称)
        $company_tax_number = trim($this->request->param('companyTaxNumber'));//公司税号
        $register_address = trim($this->request->param('registerAddress'));//注册地址
        $deposit_bank = trim($this->request->param('depositBank'));//开户银行
        $bank_number = trim($this->request->param('bankNumber'));//银行卡账号
        $register_phone = trim($this->request->param('registerPhone'));//注册电话
        $is_default = (int)trim($this->request->param('isDefault'));//是否默认 0.否 1.是

        if ($company_name == '')
        {
            $message = Lang('invoice.0');
            return output(ERROR_CODE_GSMCBNWK, $message);
        }
        if($type == 1)
        {
            if($company_tax_number == '')
            {
                $message = Lang('invoice.1');
                return output(109, $message);
            }
        }
        if(empty($id))
        {   
            $num = InvoiceHeaderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->count();
            if($num > 0)
            {
                if($is_default == 1)
                {   
                    //去除默认
                    $sql_d = InvoiceHeaderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0,'is_default'=>1])->find();
                    $sql_d->is_default = 0;
                    $sql_d->save();
                }
            }
            else
            {
                $is_default = 1;
            }
            $sql = new InvoiceHeaderModel();
            $sql->store_id = $store_id;
            $sql->user_id = $user_id;
            $sql->type = $type;
            $sql->company_name = $company_name;
            $sql->is_default = $is_default;
            if($type == 1)
            {
                $sql->company_tax_number = $company_tax_number;
                $sql->register_address = $register_address;
                $sql->deposit_bank = $deposit_bank;
                $sql->bank_number = $bank_number;
                $sql->register_phone = $register_phone;
            }
            $sql->add_time = date('Y-m-d H:i:s');
            $sql->save();
            $res = $sql->id;
            if($res < 1)
            {   
                $this->Log(__LINE__ . ":添加抬头失败！");
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }
        }
        else
        {   
            $res_i = InvoiceHeaderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0,'id'=>$id])->select()->toArray();
            if(empty($res_i))
            {
                $message = Lang('Parameter error');
                return output(ERROR_CODE_CSCW, $message);
            }
            else
            {
                $old_is_default = $res_i[0]['is_default'];
            }

            //总抬头数
            $num = InvoiceHeaderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->count();
            if($num == 1)
            {
                if($is_default == 0)
                {
                    $message = Lang('invoice.2');
                    return output(109, $message);
                }
            }
            else
            {
                if($old_is_default == 1)
                {
                    if($is_default == 0)
                    {   
                        //默认一个
                        $sql_d = InvoiceHeaderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0,'is_default'=>0])->order('id','desc')->limit(0,1)->find();
                        $sql_d->is_default = 1;
                        $sql_d->save();
                    }
                }
                else
                {
                    if($is_default == 1)
                    {   
                        //去除默认
                        $sql_d = InvoiceHeaderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0,'is_default'=>1])->find();
                        $sql_d->is_default = 0;
                        $sql_d->save();
                    }
                }
                $sql = InvoiceHeaderModel::find($id);
                $sql->type = $type;
                $sql->company_name = $company_name;
                $sql->is_default = $is_default;
                if($type == 1)
                {
                    $sql->company_tax_number = $company_tax_number;
                    $sql->register_address = $register_address;
                    $sql->deposit_bank = $deposit_bank;
                    $sql->bank_number = $bank_number;
                    $sql->register_phone = $register_phone;
                }
                $res = $sql->save();
                if(!$res)
                {
                    $this->Log(__LINE__ . ":编辑抬头失败！");
                    $message = Lang('Modification failed');
                    return output(ERROR_CODE_CZSB, $message);
                }
            }  
        }
        $message = Lang('Success');
        return output(200, $message);
    }

    //删除
    public function del()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $user_id = $this->user_list['user_id'];

        $id = trim($this->request->param('ids'));//抬头id
        $sql = InvoiceHeaderModel::find($id);
        if($sql)
        {
            $res = $sql->delete();
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

    //获取默认抬头
    public function getDefault()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));

        $user_id = $this->user_list['user_id'];
        $list = array();
        $res = InvoiceHeaderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0,'is_default'=>1])->select()->toArray();
        if($res)
        {
            $list = $res[0];
        }
        $message = Lang('Success');
        return output(200, $message,$list);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/invoice.log",$Log_content);
        return;
    }
}
