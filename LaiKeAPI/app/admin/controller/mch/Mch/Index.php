<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\ServerPath;

use app\admin\model\SystemConfigurationModel;
use app\admin\model\CustomerModel;
use app\admin\model\MchModel;
/**
 * 功能：PC店鋪首页
 * 修改人：DHB
 */
class Index extends BaseController
{
    // 经营数据
    public function index()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$store_type = addslashes(Request::param('storeType'));
    	$access_id = addslashes(Request::param('accessId'));

        $user_id = cache($access_id.'_uid'); // 用户user_id
        
    	$mchData = array(); // 代办事项
    	$mchMap = array(); // 店铺信息
    	$mchBuyPowerList = array(); // 客户购买力排行
    	$proList = array(); // 商品销量排行
    	$mchRecordList = array(); // 交易数据

        $r_mch = MchModel::where(['store_id'=> $store_id,'recovery'=>0,'user_id'=>$user_id])->select()->toArray();
        $mch_id = $r_mch[0]['id'];
        $mch_id_str = ',' . $mch_id . ',';
        
        $sql0 = "select * from lkt_mch_statistics where store_id = '$store_id' and mch_id = '$mch_id' order by id desc limit 1 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $mchData = $r0[0];
        }
        
        $mchMap['mchName'] = $r_mch[0]['name']; // 店铺名称
        $mchMap['tel'] = $r_mch[0]['tel']; // 店铺联系电话
        $mchMap['address'] = $r_mch[0]['address']; // 联系地址
        $mchMap['img'] = ServerPath::getimgpath($r_mch[0]['logo'], $store_id); //图片
        $mchMap['is_open'] = $r_mch[0]['is_open']; // 是否营业：0.未营业 1.营业中 2.打烊
        $mchMap['business_hours'] = $r_mch[0]['business_hours']; // 营业时间
        $mchMap['sy_money'] = $r_mch[0]['cashable_money']; // 账户余额
        $mchMap['djs_money'] = $r_mch[0]['account_money']; // 待结算金额
        $mchMap['return_money'] = 0; // 退款金额
        $mchMap['ytx_money'] = 0; // 已提现金额

        $sql1_0 = "select sum(r.real_money) as return_money from lkt_order as o left join lkt_order_details AS d ON o.sNo = d.r_sNo left join lkt_product_list AS p ON p.id = d.p_id RIGHT JOIN lkt_return_order as r on d.id = r.p_id where o.store_id = '$store_id' AND o.status IN (1,2,5,7) and o.recycle not in (1,3) and p.mch_id = '$mch_id' and r.re_type in (1,2) and r.r_type in (4,9) ";
        $r1_0 = Db::query($sql1_0);
        if($r1_0)
        {
            $mchMap['return_money'] = round($r1_0[0]['return_money'],2); // 退款金额
        }

        $sql1_1 = "select sum(money) as money from lkt_withdraw where store_id = '$store_id' and user_id = '$user_id' and is_mch = 1 and status = 1 ";
        $r1_1 = Db::query($sql1_1);
        if($r1_1)
        {
            if($r1_1[0]['money'] != null)
            {
                $mchMap['ytx_money'] = $r1_1[0]['money']; // 已提现金额
            }
        }

        $sql2_0 = "select a.money,u.user_name,u.headimgurl as img from lkt_mch_buy_power as a left join lkt_user as u on a.user_id = u.user_id where a.store_id = '$store_id' and a.mch_id = '$mch_id' order by a.money desc ";
        $r2_0 = Db::query($sql2_0);
        if($r2_0)
        {
            $mchBuyPowerList = $r2_0;
        }
        
        $sql3_0 = "select id as pid,product_title,imgurl,real_volume as volume from lkt_product_list where store_id = '$store_id' and mch_id = '$mch_id' and recycle = 0 order by real_volume desc limit 10 ";
        $r3_0 = Db::query($sql3_0);
        if($r3_0)
        {
           $proList = $r3_0;
        }

        $mon = date("Y-m"); // 当前月份
        $today = date("Y-m-d"); // 今天
        // 近7天
        $date_week = array();
        for ($i = 6; $i >=0; $i--)
        {
            $date_week[] = date('Y-m-d', strtotime("$today -$i days"));
        }

        // 近一个月
        $date_month = array();
        for ($i = 30; $i >= 0; $i--)
        {
            $date_month[] = date('Y-m-d', strtotime("$today -$i days")); //每隔一天赋值给数组
        }
        
        // 近一年
        $date_year = array();
        for ($i = 12; $i >= 0; $i--)
        {
            $date_year[] = date('Y-m', strtotime("$mon -$i month")); //每隔一天赋值给数组
        }

        foreach($date_week as $k => $v)
        {
            $mchRecordList[0][0][$k] = $v;
            $mchRecordList[0][1][$k] = 0;
            $mchRecordList[0][2][$k] = 0;
            $sql4_0 = "select ifnull(sum(order_number),0) as total,ifnull(sum(money),0) as money from lkt_mch_order_record where store_id = '$store_id' and mch_id = '$mch_id' and recycle = 0 and date_format(count_day,'%Y-%m-%d') = '$v' ";
            $r4_0 = Db::query($sql4_0);
            if($r4_0)
            {
                $mchRecordList[0][1][$k] = $r4_0[0]['money'];
                $mchRecordList[0][2][$k] = $r4_0[0]['total'];
            }
        }

        foreach($date_month as $k => $v)
        {
            $mchRecordList[1][0][$k] = $v;
            $mchRecordList[1][1][$k] = 0;
            $mchRecordList[1][2][$k] = 0;
            $sql4_0 = "select ifnull(sum(order_number),0) as total,ifnull(sum(money),0) as money from lkt_mch_order_record where store_id = '$store_id' and mch_id = '$mch_id' and recycle = 0 and date_format(count_day,'%Y-%m-%d') = '$v' ";
            $r4_0 = Db::query($sql4_0);
            if($r4_0)
            {
                $mchRecordList[1][1][$k] = $r4_0[0]['money'];
                $mchRecordList[1][2][$k] = $r4_0[0]['total'];
            }
        }

        foreach($date_year as $k => $v)
        {
            $mchRecordList[2][0][$k] = $v;
            $mchRecordList[2][1][$k] = 0;
            $mchRecordList[2][2][$k] = 0;
            $sql4_0 = "select ifnull(sum(order_number),0) as total,ifnull(sum(money),0) as money from lkt_mch_order_record where store_id = '$store_id' and mch_id = '$mch_id' and recycle = 0 and date_format(count_day,'%Y-%m') = '$v' ";
            $r4_0 = Db::query($sql4_0);
            if($r4_0)
            {
                $mchRecordList[2][1][$k] = $r4_0[0]['money'];
                $mchRecordList[2][2][$k] = $r4_0[0]['total'];
            }
        }

        $data = array('mchBuyPowerList'=>$mchBuyPowerList,'mchData'=>$mchData,'mchMap'=>$mchMap,'mchRecordList'=>$mchRecordList,'proList'=>$proList);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

}
