<?php

namespace app\admin\controller\app;

use think\facade\Db;
use app\common\LaiKeLogUtils;

use app\admin\model\ConfigModel;
use app\admin\model\CustomerModel;
class TestReport
{
    public function index()
    {
        $tres = ConfigModel::order('store_id', 'asc')->field('store_id')->select()->toArray();
        
        $this->testLog(__METHOD__ . ":" . __LINE__ . "定时任务开启");
        foreach ($tres as $tkey => $tvalue)
        {
            $this->store_id = $tvalue['store_id'];
            if (!$this->getStoreStatus($this->store_id))
            {
                $this->testLog(__METHOD__ . "-->" . __LINE__ . "店铺 [ $this->store_id ] 状态已经锁定或删除、回收");
                continue;
            }

            // 新增用户
            $this->userReport();

             // 订单总数
            $this->orderCount();

            // 退付款订单数
            $this->returnOrder();
        }
        echo '执行完成。';
        exit;
    }
   
    public function getStoreStatus($store_id)
    {
        $res = CustomerModel::where(['id'=>$store_id,'recycle'=>0,'status'=>0])->select()->toArray();
        return count($res) > 0 ? true : false;
    }

    //新增用户
    public function userReport()
    {
        $store_id = $this->store_id;
        Db::startTrans();
        //清理数据
        $sql_d = "delete from lkt_order_report where store_id = '$store_id' and type = 5";
        $res_d = Db::execute($sql_d);
        if($res_d < 0)
        {
            Db::rollback();
            $this->testLog(__METHOD__ . '->' . __LINE__ . '记录删除失败！sql：' . $sql_d);
        }
        $additiondata = array();
        $today = date("Y-m-d");//今日
        //近7天
        $date_week = array();
        for ($i = 6; $i >=0; $i--)
        {
            $date_week[] = date('Y-m-d', strtotime("$today -$i days"));
        }
        foreach ($date_week as $key => $value)
        {   
            $additiondata['week'][0][$key] = $value;
            $additiondata['week'][1][$key] = 0;
            $additiondata['week'][2][$key] = 0;
            $additiondata['week'][3][$key] = 0;
            $additiondata['week'][4][$key] = 0;
            //app
            $sql0 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 2 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res0 = Db::query($sql0);
            if($res0)
            {
                $additiondata['week'][1][$key] = $res0[0]['num'];
            }
            //小程序
            $sql1 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 1 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res1 = Db::query($sql1);
            if($res1)
            {
                $additiondata['week'][2][$key] = $res1[0]['num'];
            }
            //H5
            $sql2 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 7 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res2 = Db::query($sql2);
            if($res2)
            {
                $additiondata['week'][3][$key] = $res2[0]['num'];
            }
            //PC
            $sql3 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 6 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res3 = Db::query($sql3);
            if($res3)
            {
                $additiondata['week'][4][$key] = $res3[0]['num'];
            }
        }

        //近一月
        $date_month = array();
        for ($i = 30; $i >= 0; $i--)
        {
            $date_month[] = date('Y-m-d', strtotime("$today -$i days")); //每隔一天赋值给数组
        }
        foreach ($date_month as $key => $value) 
        {
            $additiondata['month'][0][$key] = $value;
            $additiondata['month'][1][$key] = 0;
            $additiondata['month'][2][$key] = 0;
            $additiondata['month'][3][$key] = 0;
            $additiondata['month'][4][$key] = 0;
            //app
            $sql4 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 2 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res4 = Db::query($sql4);
            if($res4)
            {
                $additiondata['month'][1][$key] = $res4[0]['num'];
            }
            //小程序
            $sql5 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 1 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res5 = Db::query($sql5);
            if($res5)
            {
                $additiondata['month'][2][$key] = $res5[0]['num'];
            }
            //H5
            $sql6 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 7 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res6 = Db::query($sql6);
            if($res6)
            {
                $additiondata['month'][3][$key] = $res6[0]['num'];
            }
            //PC
            $sql7 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 6 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res7 = Db::query($sql7);
            if($res7)
            {
                $additiondata['month'][4][$key] = $res7[0]['num'];
            }
        }

        //近一年
        $date_year = array();
        for ($i = 365; $i >= 0; $i--)
        {
            $date_year[] = date('Y-m-d', strtotime("$today -$i days")); //每隔一天赋值给数组
        }
        foreach ($date_year as $key => $value) 
        {
            $additiondata['year'][0][$key] = $value;
            $additiondata['year'][1][$key] = 0;
            $additiondata['year'][2][$key] = 0;
            $additiondata['year'][3][$key] = 0;
            $additiondata['year'][4][$key] = 0;
            //app
            $sql8 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 2 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res8 = Db::query($sql8);
            if($res8)
            {
                $additiondata['year'][1][$key] = $res8[0]['num'];
            }
            //小程序
            $sql9 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 1 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res9 = Db::query($sql9);
            if($res9)
            {
                $additiondata['year'][2][$key] = $res9[0]['num'];
            }
            //H5
            $sql10 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 7 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res10 = Db::query($sql10);
            if($res10)
            {
                $additiondata['year'][3][$key] = $res10[0]['num'];
            }
            //PC
            $sql11 = "SELECT COUNT( 1 ) num,DATE_FORMAT( Register_data, '%Y-%m-%d' ) AS DAY FROM lkt_user WHERE store_id = 1 AND DATE_FORMAT( Register_data, '%Y-%m-%d' )= '$value' AND source = 6 GROUP BY DATE_FORMAT( Register_data, '%Y-%m-%d' )";
            $res11 = Db::query($sql11);
            if($res11)
            {
                $additiondata['year'][4][$key] = $res11[0]['num'];
            }
        }
        $data = json_encode($additiondata);
        $sql = "insert into lkt_order_report (type,num,data,store_id) values (5,0,'$data','$store_id')";
        $res = Db::execute($sql);
        if($res < 0)
        {   
            Db::rollback();
            $this->testLog(__METHOD__ . '->' . __LINE__ . '记录插入失败！sql：' . $sql);
        }
        Db::commit();
    }

    //订单总数
    public function orderCount()
    {
        $store_id = $this->store_id;
        Db::startTrans();
        //清理数据
        $sql_d = "delete from lkt_order_report where store_id = '$store_id' and type = 7";
        $res_d = Db::execute($sql_d);
        if($res_d < 0)
        {
            Db::rollback();
            $this->testLog(__METHOD__ . '->' . __LINE__ . '记录删除失败！sql：' . $sql_d);
        }
        $totalAmount = array();
        $today = date("Y-m-d");//今日
        $mon = date("Y-m");//当前月份
        //近7天
        $date_week = array();
        for ($i = 6; $i >=0; $i--)
        {
            $date_week[] = date('Y-m-d', strtotime("$today -$i days"));
        }
        foreach ($date_week as $key => $value) 
        {   
            $totalAmount['week'][0][$key] = $value;
            $totalAmount['week'][1][$key] = 0;
            $sql0 = "SELECT count(1) num FROM lkt_order WHERE store_id = '$store_id' AND DATE_FORMAT(pay_time,'%Y-%m-%d')= '$value'";
            $res0 = Db::query($sql0);
            if($res0)
            {
                $totalAmount['week'][1][$key] = $res0[0]['num'];
            }
        }
        //近一月
        $date_month = array();
        for ($i = 30; $i >= 0; $i--)
        {
            $date_month[] = date('Y-m-d', strtotime("$today -$i days")); //每隔一天赋值给数组
        }
        foreach ($date_month as $key => $value) 
        {
            $totalAmount['month'][0][$key] = $value;
            $totalAmount['month'][1][$key] = 0;
            $sql1 = "SELECT count(1) num FROM lkt_order WHERE store_id = '$store_id' AND DATE_FORMAT(pay_time,'%Y-%m-%d')= '$value'";
            $res1 = Db::query($sql1);
            if($res1)
            {
                $totalAmount['month'][1][$key] = $res1[0]['num'];
            }
        }
        //近一年
        $date_year = array();
        for ($i = 12; $i >= 0; $i--)
        {
            $date_year[] = date('Y-m', strtotime("$mon -$i month")); //每隔一天赋值给数组
        }
        foreach ($date_year as $key => $value) 
        {
            $totalAmount['year'][0][$key] = $value;
            $totalAmount['year'][1][$key] = 0;
            $sql1 = "SELECT count(1) num FROM lkt_order WHERE store_id = '$store_id' AND DATE_FORMAT(pay_time,'%Y-%m')= '$value'";
            $res1 = Db::query($sql1);
            if($res1)
            {
                $totalAmount['year'][1][$key] = $res1[0]['num'];
            }
        }
        $data = json_encode($totalAmount);
        $sql = "insert into lkt_order_report (type,num,data,store_id) values (7,0,'$data','$store_id')";
        $res = Db::execute($sql);
        if($res < 0)
        {   
            Db::rollback();
            $this->testLog(__METHOD__ . '->' . __LINE__ . '记录插入失败！sql：' . $sql);
        }
        Db::commit();
    }

    //退付款订单数
    public function returnOrder()
    {
        $store_id = $this->store_id;
        Db::startTrans();
        //清理数据
        $sql_d = "delete from lkt_order_report where store_id = '$store_id' and type = 6";
        $res_d = Db::execute($sql_d);
        if($res_d < 0)
        {
            Db::rollback();
            $this->testLog(__METHOD__ . '->' . __LINE__ . '记录删除失败！sql：' . $sql_d);
        }
        $refundOrderData = array();
        $today = date("Y-m-d");//今日
        $mon = date("Y-m");//当前月份
        //近七天
        $date_week = array();
        for ($i = 6; $i >=0; $i--)
        {
            $date_week[] = date('Y-m-d', strtotime("$today -$i days"));
        }
        foreach ($date_week as $key => $value) 
        {
            $refundOrderData['week'][0][$key] = $value;
            $refundOrderData['week'][1][$key] = 0;
            $refundOrderData['week'][2][$key] = 0;
            //付款
            $sql0 = "SELECT count(1) num FROM lkt_order WHERE store_id = '$store_id' AND DATE_FORMAT(pay_time,'%Y-%m-%d')= '$value'";
            $res0 = Db::query($sql0);
            if($res0)
            {
                $refundOrderData['week'][1][$key] = $res0[0]['num'];
            }
            //退款
            $sql1 = "SELECT count(1) num FROM lkt_return_order WHERE store_id = '$store_id' AND DATE_FORMAT(re_time,'%Y-%m-%d')= '$value' ";
            $res1 = Db::query($sql1);
            if($res1)
            {
                $refundOrderData['week'][2][$key] = $res1[0]['num'];
            }
        }
        //近一月
        $date_month = array();
        for ($i = 30; $i >= 0; $i--)
        {
            $date_month[] = date('Y-m-d', strtotime("$today -$i days")); //每隔一天赋值给数组
        }
        foreach ($date_month as $key => $value) 
        {
            $refundOrderData['month'][0][$key] = $value;
            $refundOrderData['month'][1][$key] = 0;
            $refundOrderData['month'][2][$key] = 0;
            //付款
            $sql2 = "SELECT count(1) num FROM lkt_order WHERE store_id = '$store_id' AND DATE_FORMAT(pay_time,'%Y-%m-%d')= '$value'";
            $res2 = Db::query($sql2);
            if($res2)
            {
                $refundOrderData['month'][1][$key] = $res2[0]['num'];
            }
            //退款
            $sql3 = "SELECT count(1) num FROM lkt_return_order WHERE store_id = '$store_id' AND DATE_FORMAT(re_time,'%Y-%m-%d')= '$value'";
            $res3 = Db::query($sql3);
            if($res3)
            {
                $refundOrderData['month'][2][$key] = $res3[0]['num'];
            }
        }
        //近一年
        $date_year = array();
        for ($i = 12; $i >= 0; $i--)
        {
            $date_year[] = date('Y-m', strtotime("$mon -$i month")); //每隔一天赋值给数组
        }
        foreach ($date_year as $key => $value) 
        {
            $refundOrderData['year'][0][$key] = $value;
            $refundOrderData['year'][1][$key] = 0;
            $refundOrderData['year'][2][$key] = 0;
            //付款
            $sql4 = "SELECT count(1) num FROM lkt_order WHERE store_id = '$store_id' AND DATE_FORMAT(pay_time,'%Y-%m')= '$value'";
            $res4 = Db::query($sql4);
            if($res4)
            {
                $refundOrderData['year'][1][$key] = $res4[0]['num'];
            }
            //退款
            $sql5 = "SELECT count(1) num FROM lkt_return_order WHERE store_id = '$store_id' AND DATE_FORMAT(re_time,'%Y-%m')= '$value'";
            $res5 = Db::query($sql5);
            if($res5)
            {
                $refundOrderData['year'][2][$key] = $res5[0]['num'];
            }
        }
        $data = json_encode($refundOrderData);
        $sql = "insert into lkt_order_report (type,num,data,store_id) values (6,0,'$data','$store_id')";
        $res = Db::execute($sql);
        if($res < 0)
        {   
            Db::rollback();
            $this->testLog(__METHOD__ . '->' . __LINE__ . '记录插入失败！sql：' . $sql);
        }
        Db::commit();
    }

    // 日志
    public function testLog($Log_content)
    {
        $time = date("Y-m-d");
        $lktlog = new LaiKeLogUtils();

        $lktlog->log("app/TestReport/".$time.".log",$Log_content);
        return;
    }
}

