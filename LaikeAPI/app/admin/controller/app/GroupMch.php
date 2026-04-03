<?php
namespace app\admin\controller\app;

use app\BaseController;
use think\facade\Db;
use think\facade\Request;

use app\common\LaiKeLogUtils;
use app\common\Plugin\PluginUtils;
// 准备弃用

class GroupMch extends BaseController
{
    // 售后审核
    public function examine()
    {
        $store_id = addslashes($this->request->param('store_id'));
        $store_type = addslashes($this->request->param('store_type')); // 来源
        $access_id = addslashes($this->request->param('access_id')); // 授权id

        $mch_id = addslashes($this->request->param('shop_id')); // 授权id
        $id = addslashes($this->request->param('id')); // 售后id
        $sNo = addslashes($this->request->param('sNo')); // 订单号
        $text = addslashes($this->request->param('text'));
        $m = addslashes($this->request->param('r_type'));
        $price = addslashes($this->request->param('price')) ? addslashes($this->request->param('price')) : 0;
        $express = addslashes($this->request->param('expressId')) ? addslashes($this->request->param('expressId')) : '';
        $courier_num = addslashes($this->request->param('courierNum')) ? addslashes($this->request->param('courierNum')) : '';

        $admin_list = $this->user_list;
        $admin_name = $admin_list['user_name'];

        $action['store_id'] = $store_id;
        $action['admin_name'] = $admin_name;
        $action['id'] = $id; // 订单详情id
        $action['m'] = $m; // 退货类型
        $action['text'] = $text; // 拒绝理由
        $action['price'] = $price; // 退款金额
        $action['mch_id'] = $mch_id; // 店铺ID
        $action['express_id'] = $express; // 快递公司编号
        $action['courier_num'] = $courier_num; // 快递单号
        //都走普通的订单插件逻辑后面可以吧订单类型传入而走各自的退款逻辑，主要是这块代码几乎都是一样的逻辑
        PluginUtils::invokeMethod('','refund',$action);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/GroupMch.log",$Log_content);
        return;
    }
}

?>