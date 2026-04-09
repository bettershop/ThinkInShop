<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cache;
use think\facade\Request;
use app\common\Jurisdiction;
use app\common\ProductClass;

use app\admin\model\MchModel;

/**
 * 功能：PC店鋪商品
 * 修改人：DHB
 */
class ProClass extends BaseController
{
    // 分类审核列表
    public function auditList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $name = addslashes(trim($this->request->param('condition'))); // 分类名称/供应商名称
        $examine = addslashes(trim($this->request->param('status'))); // 审核状态 0.待审核 1.审核通过 2.不通过
        $startTime = addslashes(trim($this->request->param('startTime'))); // 查询开始时间
        $endTime = addslashes(trim($this->request->param('endTime'))); // 查询结束时间

        $page = trim($this->request->param('pageNo'));//页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据
        $pagesize = $pagesize ? $pagesize : '10';

        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'supplier_id'=>0,'lang_code'=>$lang_code,'name'=>$name,'examine'=>$examine,'startTime'=>$startTime,'endTime'=>$endTime,'page'=>$page,'pagesize'=>$pagesize,'source'=>2,'source_'=>2);
        $product_class = new ProductClass();
        $product_class_list = $product_class->auditList($data);

        return;
    }
}
