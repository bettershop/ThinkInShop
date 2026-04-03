<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;

use app\common\LaiKeLogUtils;
use app\common\Tools;

class Turnover extends BaseController
{   

    //商户营业报表
    public function mchTurnoverReport()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $type = trim($this->request->param('type'));//时间段
        $startdate = trim($this->request->param('startDate'));
        $enddate = trim($this->request->param('endDate'));
        $sorttype = trim($this->request->param('sortType'));//排序0升序1降序
        $mch_name = trim($this->request->param('mchName'));//商户名称
        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $page = $this->request->param('pageNo');
        $pagesize = $pagesize ? ($pagesize == 'undefined' ? 10 : $pagesize) : 10;
        $page = $page ? $page : 1;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        $order = " order by tt.total ASC ";
        if(strlen($sorttype))
        {
            if($sorttype == 1)
            {
                $order = " order by tt.total DESC ";
            }
        }

        $condition = " b.recycle = 0 and b.status = 0 and a.status in (1,2,5,7) and a.z_price > 0 ";
        if($type)
        {
            switch ($type) 
            {
                case 'yesterday':
                    $start_time = date("Y-m-d 00:00:00",strtotime("-1 day"));
                    $end_time = date("Y-m-d 00:00:00");
                    $condition .= " and a.add_time >= '$start_time' and a.add_time < $end_time ";
                    break;
                
                case 'day':
                    $start_time = date("Y-m-d 00:00:00");
                    $condition .= " and a.add_time >= '$start_time' ";
                    break;

                case 'week':
                    $start_time = date("Y-m-d 00:00:00",strtotime("-6 day"));
                    $condition .= " and a.add_time >= '$start_time' ";
                    break;

                case 'month':
                    $start_time = date("Y-m-01 00:00:00");
                    $condition .= " and a.add_time >= '$start_time' ";
                    break;
            }
        }
        if($startdate != '')
        {
            $condition .= " and a.add_time >= '$startdate' ";
        }
        if($enddate != '')
        {
            $condition .= " and a.add_time < '$enddate' ";
        }
        if($mch_name != '')
        {
            $mch_name = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and b.name like $mch_name ";
        }

        $total = 0;
        $sql_num = "select ifnull(count(e.mchId),0) as num from 
                    (select tt.* from 
                        (select b.id mchId,row_number () over (partition by a.store_id) as top 
                        FROM lkt_order as a
                        LEFT JOIN lkt_customer as b ON a.store_id = b.id
                        WHERE $condition) as tt 
                    where tt.top < 2) as e";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        $sql = "select tt.* from 
                (select sum(a.z_price) over (partition by a.store_id) as total,b.name,b.id mchId,row_number () over (partition by a.store_id) as top 
                FROM lkt_order as a
                LEFT JOIN lkt_customer as b ON a.store_id = b.id
                WHERE $condition) as tt where tt.top < 2 $order
                LIMIT $start,$pagesize";
        $res = Db::query($sql);
        if($res)
        {
            $list = $res;
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total));

    }

    //商户新增用户
    public function mchTurnoverNewUserReport()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $type = trim($this->request->param('type'));//时间段
        $startdate = trim($this->request->param('startDate'));
        $enddate = trim($this->request->param('endDate'));
        $sorttype = trim($this->request->param('sortType'));//排序0升序1降序
        $mch_name = trim($this->request->param('mchName'));//商户名称
        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $page = $this->request->param('pageNo');
        $pagesize = $pagesize ? ($pagesize == 'undefined' ? 10 : $pagesize) : 10;
        $page = $page ? $page : 1;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }
        $order = " order by tt.total ASC ";
        if(strlen($sorttype))
        {
            if($sorttype == 1)
            {
                $order = " order by tt.total DESC ";
            }
        }
        $condition = " b.recycle = 0 ";
        if($type)
        {
            switch ($type) 
            {
                case 'yesterday':
                    $start_time = date("Y-m-d 00:00:00",strtotime("-1 day"));
                    $end_time = date("Y-m-d 00:00:00");
                    $condition .= " and a.Register_data >= '$start_time' and a.Register_data < $end_time ";
                    break;
                
                case 'day':
                    $start_time = date("Y-m-d 00:00:00");
                    $condition .= " and a.Register_data >= '$start_time' ";
                    break;

                case 'week':
                    $start_time = date("Y-m-d 00:00:00",strtotime("-6 day"));
                    $condition .= " and a.Register_data >= '$start_time' ";
                    break;

                case 'month':
                    $start_time = date("Y-m-01 00:00:00");
                    $condition .= " and a.Register_data >= '$start_time' ";
                    break;
            }
        }
        if($startdate != '')
        {
            $condition .= " and a.Register_data >= '$startdate' ";
        }
        if($enddate != '')
        {
            $condition .= " and a.Register_data < '$enddate' ";
        }
        if($mch_name != '')
        {
            $mch_name = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and b.name like $mch_name ";
        }
        $total = 0;
        $sql_num = "select ifnull(count(e.mchId),0) as num from 
                    (select tt.* from 
                        (select b.id mchId,row_number () over (partition by a.store_id) as top
                         FROM lkt_user as a
                         LEFT JOIN lkt_customer as b
                         ON a.store_id = b.id
                         WHERE $condition) as tt 
                    where tt.top < 2) as e";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        $sql = "select tt.* from 
                (select count(a.id) over (partition by a.store_id) as total,b.name,b.id mchId,row_number () over (partition by a.store_id) as top 
                FROM lkt_user as a
                LEFT JOIN lkt_customer as b ON a.store_id = b.id
                WHERE $condition) as tt where tt.top < 2 $order
                LIMIT $start,$pagesize";             
        $res = Db::query($sql);
        if($res)
        {
            $list = $res;
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total));
    }

    //商户订单报表
    public function mchTurnoverOrderReport()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $type = trim($this->request->param('type'));//时间段
        $startdate = trim($this->request->param('startDate'));
        $enddate = trim($this->request->param('endDate'));
        $sorttype = trim($this->request->param('sortType'));//排序0升序1降序
        $mch_name = trim($this->request->param('mchName'));//商户名称
        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $page = $this->request->param('pageNo');
        $pagesize = $pagesize ? ($pagesize == 'undefined' ? 10 : $pagesize) : 10;
        $page = $page ? $page : 1;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }
        $order = " order by tt.total ASC ";
        if(strlen($sorttype))
        {
            if($sorttype == 1)
            {
                $order = " order by tt.total DESC ";
            }
        }
        $condition = " b.recycle = 0 and b.status = 0 and a.status in (1,2,5,7) and a.z_price > 0 ";
        if($type)
        {
            switch ($type) 
            {
                case 'yesterday':
                    $start_time = date("Y-m-d 00:00:00",strtotime("-1 day"));
                    $end_time = date("Y-m-d 00:00:00");
                    $condition .= " and a.add_time >= '$start_time' and a.add_time < '$end_time' ";
                    break;
                
                case 'day':
                    $start_time = date("Y-m-d 00:00:00");
                    $condition .= " and a.add_time >= '$start_time' ";
                    break;

                case 'week':
                    $start_time = date("Y-m-d 00:00:00",strtotime("-6 day"));
                    $condition .= " and a.add_time >= '$start_time' ";
                    break;

                case 'month':
                    $start_time = date("Y-m-01 00:00:00");
                    $condition .= " and a.add_time >= '$start_time' ";
                    break;
            }
        }
        if($startdate != '')
        {
            $condition .= " and a.add_time >= '$startdate' ";
        }
        if($enddate != '')
        {
            $condition .= " and a.add_time < '$enddate' ";
        }
        if($mch_name != '')
        {
            $mch_name = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and b.name like $mch_name ";
        }
        $total = 0;
        $sql_num = "select ifnull(count(e.store_id),0) as num from 
                    (select tt.* from 
                        (select a.store_id,row_number () over (partition by a.store_id) as top
                         FROM lkt_order as a
                         LEFT JOIN lkt_customer as b
                         ON a.store_id = b.id
                         WHERE $condition) as tt 
                    where tt.top < 2) as e";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        $sql = "select tt.* from 
                (select count(a.id) over (partition by a.store_id) as total,b.name,a.store_id,row_number () over (partition by a.store_id) as top 
                FROM lkt_order as a
                LEFT JOIN lkt_customer as b ON a.store_id = b.id
                WHERE $condition) as tt where tt.top < 2 $order
                LIMIT $start,$pagesize";             
        $res = Db::query($sql);
        if($res)
        {
            $list = $res;
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total));
    }


    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/express.log",$Log_content);
        return;
    }
}