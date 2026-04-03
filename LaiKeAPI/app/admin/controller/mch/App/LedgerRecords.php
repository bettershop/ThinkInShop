<?php
namespace app\admin\controller\mch\App;

use app\BaseController;
use think\facade\Db;
use app\common\Tools;

class LedgerRecords extends BaseController
{
    // 分账记录
    public function queryLedgerRecord()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $shop_id = trim($this->request->param('mchId')); // 店铺ID
        $condition = trim($this->request->param('condition')); // 搜索内容
        $startDate = trim($this->request->param('startDate')); // 查询开始时间
        $endDate = trim($this->request->param('endDate')); // 查询结束时间
        $page = trim($this->request->param('pageNo')); // 加载次数
        $pagesize = trim($this->request->param('pageSize')); // 每页多少条数据
        $user_id = $this->user_list['user_id'];

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $con = " a.mch_id = '$shop_id' and b.mch_id = '$shop_id' ";
        if($condition != '')
        {
            $condition_0 = Tools::FuzzyQueryConcatenation($condition);
            $con .= " and (a.order_no like $condition_0 or b.account like $condition_0) ";
        }

        if($startDate != '')
        {
            $con .= " and a.add_date >= '$startDate' ";
        }

        if($endDate != '')
        {
            $con .= " and a.add_date <= '$endDate' ";
        }

        $total = 0;
        $list = array();

        $sql0 = "select count(a.id) as total from lkt_mch_distribution_record as a left join lkt_mch_distribution as b on a.sub_mch_id = b.id where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql0 = "select a.*,b.account from lkt_mch_distribution_record as a left join lkt_mch_distribution as b on a.sub_mch_id = b.id where $con order by a.add_date desc limit $start,$pagesize ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $list = $r0;
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang("Success");
        return output(200,$message,$data);
    }
}
