<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;

/**
 * 功能：分账记录
 * 修改人：DHB
 */
class DivideAccount extends BaseController
{
    // 分账记录
    public function divideRecord()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));
        
        $condition = trim($this->request->param('condition'));
        $startDate = trim($this->request->param('startDate'));
        $endDate = trim($this->request->param('endDate'));
        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $page = $page ? $page : '1';
        $pagesize = $pagesize ? $pagesize : '10';

        $shop_id = cache($access_id.'_'.$store_type);

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

