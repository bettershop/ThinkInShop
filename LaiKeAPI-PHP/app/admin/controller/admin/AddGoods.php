<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use app\common\PC_Tools;
use app\common\Plugin\MchPublicMethod;


/**
 * 功能：商品获取门店类
 * 修改人：DHB
 */
class AddGoods extends BaseController
{
    // 获取店铺门店
    public function getMchStore()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID

        $array = array('store_id'=>$store_id,'mch_id'=>$SelfOperatedStore_id);
        $data = MchPublicMethod::getMchStore($array);

        $message = Lang('Success');
        return output(200,$message,$data);
    }
}
