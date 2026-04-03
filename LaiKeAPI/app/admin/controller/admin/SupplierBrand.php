<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ProductBrand;

/**
 * 功能：供应商品牌
 * 修改人：DHB
 */
class SupplierBrand extends BaseController
{   
    // 品牌审核列表
    public function auditList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $name = addslashes(trim($this->request->param('condition'))); // 品牌名称/供应商名称
        $examine = addslashes(trim($this->request->param('status'))); // 审核状态 0.待审核 1.审核通过 2.不通过
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种

        $page = trim($this->request->param('pageNo'));//页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据
        $pagesize = $pagesize ? $pagesize : '10';

        $data = array('store_id'=>$store_id,'mch_id'=>0,'supplier_id'=>0,'name'=>$name,'examine'=>$examine,'lang_code'=>$lang_code,'page'=>$page,'pagesize'=>$pagesize,'source'=>1);
        $product_brand = new ProductBrand();
        $product_brand_list = $product_brand->auditList($data);

        return;
    }
    
    // 品牌审核
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
        $product_brand = new ProductBrand();
        $product_brand_list = $product_brand->examine($data);

        return;
    }
}
