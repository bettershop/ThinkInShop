<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\FreightPublicMethod;
use app\common\PC_Tools;

use app\admin\model\MchModel;

/**
 * 功能：PC店鋪运费
 * 修改人：DHB
 */
class Freight extends BaseController
{
    // 运费列表
    public function getFreightInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $fid = addslashes(trim($this->request->param('fid'))); // 店铺ID
        $status = addslashes(trim($this->request->param('status'))); // 状态  0.未使用  1.已使用
        $type = addslashes(trim($this->request->param('type'))); // 类型 0.件 1.重量 2.默认
        $name = addslashes(trim($this->request->param('name'))); // 名称
        $pageNo = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pageSize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据

        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];
        
        $r_mch = MchModel::where(['store_id'=> $store_id,'recovery'=>0,'user_id'=>$user_id])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id'];
        
        if($fid != 0 && $fid != '')
        {
            $data = array('store_id'=>$store_id,'fid'=>$fid,'mch_id'=>$mch_id);

            $freight = new FreightPublicMethod();
            $freight_list = $freight->edit_page($data);
        }
        else 
        {
            $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'status'=>$status,'type'=>$type,'name'=>$name,'pageNo'=>$pageNo,'pageSize'=>$pageSize);

            $freight = new FreightPublicMethod();
            $freight_list = $freight->freight_list($data);
        }

        $message = Lang('Success');
        return output(200, $message,$freight_list);
    }

    // 添加/编辑运费
    public function addFreight()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $fid = addslashes(trim($this->request->post('fid'))); // 运费ID
        $name = addslashes(trim($this->request->post('name'))); // 模板名称
        $type = addslashes(trim($this->request->post('type'))); // 类型 0:件 1:重量
        $is_package_settings = addslashes(trim($this->request->post('isPackageSettings'))); // 是否是包邮设置 0.未开启 1.开启
        $package_settings = addslashes(trim($this->request->post('packageSettings'))); // 包邮设置
        $is_no_delivery = addslashes(trim($this->request->post('isNoDelivery'))); // 是否不配送 0.未开启 1.开启
        $default_freight = $this->request->post('defaultFreight'); // 默认运费
        $hidden_freight = $this->request->post('hiddenFreight'); // 指定运费
        $no_delivery = $this->request->post('noDelivery'); // 不配送地区

        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r_mch = MchModel::where(['store_id'=> $store_id,'recovery'=>0,'user_id'=>$user_id])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id'];

        $data = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'fid'=>$fid,'name'=>$name,'type'=>$type,'default_freight'=>$default_freight,'hidden_freight'=>$hidden_freight,'no_delivery'=>$no_delivery,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);

        $freight = new FreightPublicMethod();
        $freight_list = $freight->preserve($data);
        return;
    }

    // 设为默认运费
    public function freightSetDefault()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = intval($this->request->param('id'));

        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r_mch = MchModel::where(['store_id'=> $store_id,'recovery'=>0,'user_id'=>$user_id])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id'];

        $data = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'id'=>$id,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $freight = new FreightPublicMethod();
        $freight_list = $freight->set_default($data);
        return;
    }

    // 删除运费
    public function delFreight()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        // 接收信息
        $id = addslashes(trim($this->request->param('idList'))); // 单运费id

        if($id == '' || $id == 0)
        {
            $id = addslashes(trim($this->request->param('freightIds'))); // 多运费id
        }

        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r_mch = MchModel::where(['store_id'=> $store_id,'recovery'=>0,'user_id'=>$user_id])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id'];

        $data = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'id'=>$id,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $freight = new FreightPublicMethod();
        $freight_list = $freight->freight_del($data);

        return;
    }

    // 关联商品
    public function RelatedProducts()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        // 接收信息
        $id = addslashes(trim($this->request->param('id'))); // 运费id
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $mch_id = PC_Tools::SelfOperatedStore($store_id);

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id,'page'=>$page,'pagesize'=>$pagesize,'operator_id'=>$operator_id,'operator'=>$operator,'operator_source'=>1);
        $freight = new FreightPublicMethod();
        $freight_list = $freight->RelatedProducts($data);

        return;
    }
}
