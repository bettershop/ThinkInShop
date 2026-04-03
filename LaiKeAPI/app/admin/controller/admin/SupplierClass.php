<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ProductClass;


/**
 * 功能：供应商分类
 * 修改人：DHB
 */
class SupplierClass extends BaseController
{   
    // 分类审核列表
    public function auditList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $name = addslashes(trim($this->request->param('condition'))); // 分类名称/供应商名称
        $examine = addslashes(trim($this->request->param('status'))); // 审核状态 0.待审核 1.审核通过 2.不通过
        $startTime = addslashes(trim($this->request->param('startTime'))); // 查询开始时间
        $endTime = addslashes(trim($this->request->param('endTime'))); // 查询结束时间
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种

        $page = trim($this->request->param('pageNo'));//页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据
        $pagesize = $pagesize ? $pagesize : '10';

        $data = array('store_id'=>$store_id,'mch_id'=>0,'supplier_id'=>0,'name'=>$name,'level'=>'','examine'=>$examine,'startTime'=>$startTime,'endTime'=>$endTime,'lang_code'=>$lang_code,'page'=>$page,'pagesize'=>$pagesize,'source'=>1);
        $product_class = new ProductClass();
        $product_class_list = $product_class->auditList($data);

        return;
    }
    
    // 分类审核
    public function examine()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 分类名称/供应商名称
        $status = addslashes(trim($this->request->param('status'))); // 审核状态 0.待审核 1.审核通过 2.不通过
        $refuse_reasons = trim($this->request->param('remark')); // 拒绝原因

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $source = 1;

        $data = array('store_id'=>$store_id,'mch_id'=>0,'supplier_id'=>0,'id'=>$id,'status'=>$status,'refuse_reasons'=>$refuse_reasons,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>$source);
        $product_class = new ProductClass();
        $product_class_list = $product_class->examine($data);

        return;
    }
}
