<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\ExcelUtils;
use app\common\SettlementOrder;

/**
 * 功能：后台订单类
 * 修改人：PJY
 */
class OrderSettlement extends BaseController
{   
    // 结算订单列表
    public function index()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $exportType = $this->request->param('exportType');

        $search = addslashes(trim($this->request->param('search'))); // 订单号/订单id
        $startdate = $this->request->param("startDate");
        $enddate = $this->request->param("endDate"); 
        $status = $this->request->param('status');
        $mch_name = $this->request->param('mchName');
        $page = $this->request->param('pageNo'); // 页码
        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? $pagesize : 10;

        $array = array('store_id'=>$store_id,'mch_id'=>0,'shop_id'=>0,'otype'=>'GM','search'=>$search,'startdate'=>$startdate,'enddate'=>$enddate,'status'=>$status,'mch_name'=>$mch_name,'page'=>$page,'pagesize'=>$pagesize);
        $SettlementOrder = new SettlementOrder();
        $data = $SettlementOrder->index($array);
        $list = $data['list'];
        
        //请求为导出
        if ($exportType)
        {   
            $titles = array(
                0 => '结算单号',
                1 => '退还佣金',
                2 => '订单编号',
                3 => '订单金额',
                4 => '店铺名称',
                5 => '退单金额',
                6 => '结算状态',
                7 => '结算时间',
                8 => '运费',
                10 => '店铺优惠',
                11 => '平台优惠',
                12 => '订单生成时间'
            );
            
            $exportExcel_list = array();

            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['id'],
                        $v['r_commission'],
                        $v['sNo'],
                        $v['z_price'],
                        $v['shopName'],
                        $v['return_money'],
                        $v['status_name'],
                        $v['arrive_time'],
                        $v['z_freight'],
                        $v['mch_discount'],
                        $v['preferential_amount'],
                        $v['add_time']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '结算订单列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 结算订单详情
    public function detail()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $sNo = $this->request->param('orderNo'); // 订单号

        $array = array('store_id'=>$store_id,'mch_id'=>0,'sNo'=>$sNo,'operator'=>$admin_name,'operator_source'=>1);
        $SettlementOrder = new SettlementOrder();
        $date = $SettlementOrder->detail($array);

        return;
    }

    // 结算订单-刪除
    public function del()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $id = $this->request->param('id'); // 订单id

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'mch_id'=>0,'id'=>$id,'operator_id'=>$operator_id,'operator'=>$operator,'operator_source'=>1);
        $SettlementOrder = new SettlementOrder();
        $date = $SettlementOrder->del($array);

        return;
    }
}