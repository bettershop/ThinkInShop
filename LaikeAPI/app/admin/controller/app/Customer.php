<?php
namespace app\admin\controller\app;

use think\facade\Db;
use think\facade\Request;

use app\common\LaiKeLogUtils;
use app\common\Plugin\Plugin;
use app\common\ServerPath;

use app\admin\model\UserModel;

class Customer
{
    // 客服我的订单
    public function orderIndex()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $mchId = addslashes(Request::param('mchId')); // 店铺ID
        $page = addslashes(Request::param('page')); // 加载次数
        $pagesize = addslashes(Request::param('pageSize')); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : 10;

        $list = array();
        $total = 0;

        $user_id = '';
        if ($access_id != '')
        {
            $user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
            if ($user)
            {
                $user_id = $user[0]['user_id'];

                $mch_id = ',' . $mchId . ',';
                $sql_total = "select count(id) as total from lkt_order where store_id = '$store_id' and otype = 'GM' and status in (1,2) and user_id = '$user_id' and mch_id = '$mch_id' ";
                $r_total = Db::query($sql_total);
                if($r_total)
                {
                    $total = $r_total[0]['total'];
                }

                $sql0 = "select id,sNo,add_time,z_price,old_total,num from lkt_order where store_id = '$store_id' and otype = 'GM' and status in (1,2) and user_id = '$user_id' and mch_id = '$mch_id' ";
                $r0 = Db::query($sql0);
                if($r0)
                {
                    foreach($r0 as $k => $v)
                    {
                        $sNo = $v['sNo'];
                        if($v['old_total'] == '0.00' || $v['old_total'] == '')
                        {
                            $v['old_total'] = (float)$v['z_price'];
                        }
                        
                        $v['list'] = array();
                        $sql1 = "select c.img as imgUrl,a.p_id,a.p_name,a.num,a.sid,a.size,a.p_price,a.freight from lkt_order_details as a left join lkt_configure as c on a.sid = c.id where a.store_id = '$store_id' and a.r_sNo = '$sNo' ";
                        $r1 = Db::query($sql1);
                        if($r1)
                        {
                            foreach($r1 as $k1 => $v1)
                            {
                                $v1['imgUrl'] = ServerPath::getimgpath($v1['imgUrl'], $store_id);
                                $v['list'][] = $v1;
                            }
                        }

                        $list[] = $v;
                    }
                }
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 店铺日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/Customer.log",$Log_content);

        return;
    }
}

?>