<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\FreightPublicMethod;

use app\admin\model\AdminModel;

/**
 * 功能：运费类
 * 修改人：DHB
 */
class AdminFreight extends BaseController
{
    // 运费列表
    public function AdminGetFreightInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $fid = addslashes(trim($this->request->param('fid'))); // 店铺ID
        $mch_id = addslashes(trim($this->request->param('mchId'))); // 店铺ID
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $status = addslashes(trim($this->request->param('status'))); // 是否使用 1.使用 0.未使用
        $type = addslashes(trim($this->request->param('type'))); // 类型 0:件 1:重量
        $name = addslashes(trim($this->request->param('name'))); // 模板名称
        $pageNo = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pageSize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据

        if($fid != 0 && $fid != '')
        {
            $data = array('store_id'=>$store_id,'fid'=>$fid,'mch_id'=>$mch_id);

            $freight = new FreightPublicMethod();
            $freight_list = $freight->edit_page($data);
        }
        else 
        {
            $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code,'status'=>$status,'type'=>$type,'name'=>$name,'pageNo'=>$pageNo,'pageSize'=>$pageSize);

            $freight = new FreightPublicMethod();
            $freight_list = $freight->freight_list($data);
        }
        return;
    }

    // 添加/编辑运费
    public function addAdminFreight()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $fid = addslashes(trim($this->request->post('fid'))); // 运费ID
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $country_num = addslashes(trim($this->request->param('country_num'))); // 所属国家
        $name = addslashes(trim($this->request->post('name'))); // 模板名称
        $type = addslashes(trim($this->request->post('type'))); // 类型 0:件 1:重量
        $default_freight = $this->request->post('defaultFreight'); // 默认运费
        $hidden_freight = $this->request->post('hiddenFreight'); // 指定运费
        $no_delivery = $this->request->post('noDelivery'); // 不配送地区
        $threeIdsList = $this->request->post('threeIdsList'); // 不配送地区ID

        $admin_name = $this->user_list['name'];
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id']; // 店铺id

        $data = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'fid'=>$fid,'lang_code'=>$lang_code,'country_num'=>$country_num,'name'=>$name,'type'=>$type,'default_freight'=>$default_freight,'hidden_freight'=>$hidden_freight,'no_delivery'=>$no_delivery,'threeIdsList'=>$threeIdsList,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);

        $freight = new FreightPublicMethod();
        $freight_list = $freight->preserve($data);
        return;
    }

    // 获取城市信息
    public function cityInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
 
        $freight = new FreightPublicMethod();
        $freight_list = $freight->cityInfo();
        return;
    }
    
    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/adminFreight.log",$Log_content);
        return;
    }
}
