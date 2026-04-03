<?php
namespace app\admin\controller\app;

use app\BaseController;
use think\facade\Db;
use app\common\Tools;

use app\admin\model\FinanceConfigModel;

class Recharge extends BaseController
{
    // 请求我的详细数据
    public function index()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id

        //查询最低充值金额
        $rs = FinanceConfigModel::where(['store_id'=>$store_id])
                                ->select()
                                ->toArray();
        $min_cz = isset($rs[0])?$rs[0]['min_cz']:1;

        $payment = Tools::getPayment($store_id);

        $resArr = array('min_cz' => $min_cz, 'payment' => $payment);
        $message = Lang('Success');
        return output(200,$message, $resArr);
    }

}

?>