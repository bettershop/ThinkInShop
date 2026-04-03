<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\ExcelUtils;

use app\admin\model\OrderModel;
use app\admin\model\MchModel;
use app\admin\model\MchConfigModel;
use app\admin\model\MchPromiseModel;
use app\admin\model\PromiseShModel;
/**
 * 功能：PC店鋪保证金
 * 修改人：DHB
 */
class Promise extends BaseController
{
    // 保证金记录
    public function PromiseList()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));

    	$type = addslashes($this->request->param('type')); // 保证金类型：1缴纳；2退还
    	$status = addslashes($this->request->param('status')); // 审核状态：0审核中；1通过；2拒绝
    	$startDate = addslashes($this->request->param('startDate')); // 查询开始时间
    	$endDate = addslashes($this->request->param('endDate')); // 查询结束时间
    	$page = addslashes($this->request->param('pageNo')); // 页码
    	$pagesize = addslashes($this->request->param('pageSize')); // 每页多少条数据
        $exportType = addslashes($this->request->param('exportType')); // 导出

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $user_id = cache($access_id.'_uid'); // 用户user_id
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
        }

        $condition = " store_id = '$store_id' and mch_id = '$mch_id' ";
        if($type != '')
        {
            $condition .= " and type = '$type' ";
        }
        if($status != '')
        {
            $condition .= " and status = '$status' ";
        }
        if($startDate != '')
        {
            $condition .= " and add_date >= '$startDate' ";
        }
        if($endDate != '')
        {
            $condition .= " and add_date <= '$endDate' ";
        }
        
        $total = 0;
        $list = array();
        $sql0 = "select count(id) as total from lkt_promise_record where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select * from lkt_promise_record where $condition order by add_date desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $v['statusName'] = "";
                $v['typeName'] = "缴纳";
                if($v['type'] == 2)
                {
                    $v['typeName'] = "退还";

                    if($v['status'] == 0)
                    {
                        $v['statusName'] = "审核中";
                    }
                    else if($v['status'] == 1)
                    {
                        $v['statusName'] = "审核通过";
                    }
                    else if($v['status'] == 2)
                    {
                        $v['statusName'] = "审核失败";
                    }
                }
                else
                {
                    unset($v['status']);
                }
                
                $v['remark'] = $v['remarks'];

                $list[] = $v;
            }
        }

        $haveOrder = true; // 是否有未完成的订单 true:没有 false:有
        $r0_1 = OrderModel::where(['store_id'=>$store_id])->whereLike('mch_id','%,'.$mch_id.',%')->whereIn('status','0,1,2')->field('id')->select()->toArray();
        if($r0_1)
        {
            $haveOrder = false; // 是否有未完成的订单
        }

        $haveProduct = true; // 有上架商品不能退还保证金  true:没有 false:有
        $sql0_2 = "select id from lkt_product_list where store_id = '$store_id' and mch_id = '$mch_id' and status = 2 and recycle = 0 ";
        $r0_2 = Db::query($sql0_2);
        if($r0_2)
        {
            $haveProduct = false; // 是否有商品不能退款
        }

        $isPromisePay = false; // 是否缴纳保证金
        $r0_0 = MchConfigModel::where(['store_id'=>$store_id])->field('commodity_setup,promise_switch')->select()->toArray();
        if($r0_0)
        {   
            if($r0_0[0]['promise_switch'] == 1)
            {
                $r_6 = MchPromiseModel::where(['mch_id'=>$mch_id,'status'=>1,'is_return_pay'=>0])->select()->toArray();
                if($r_6)
                {
                    $isPromisePay = true;
                }
            }
            else
            {
                $r_6 = MchPromiseModel::where(['mch_id'=>$mch_id,'status'=>1,'is_return_pay'=>0])->select()->toArray();
                if($r_6)
                {
                    $isPromisePay = true;
                }
            }
        }

        $promisePrice = 0;
        $isPromiseExamine = true; // 判断保证金是否在退还中 true:没 false:退还中
        $sql0_3 = "select type,status,money from lkt_promise_record where store_id = '$store_id' and mch_id = '$mch_id' order by add_date desc limit 1 ";
        $r0_3 = Db::query($sql0_3);
        if($r0_3)
        {
            $promisePrice = $r0_3[0]['money'];
            if($r0_3[0]['type'] == 2 && $r0_3[0]['status'] == 0)
            {
                $isPromiseExamine = false; // 判断保证金是否在退还中
            }
        }

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '序号',
                1 => '保证金金额',
                2 => '缴纳/退还',
                3 => '审核状态',
                4 => '操作时间',
                5 => '备注'
            );
            $exportExcel_list = array();
            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['id'],
                        $v['money'],
                        $v['typeName'],
                        $v['statusName'],
                        $v['add_date'],
                        $v['remark']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '保证金列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }

        $data = array('list'=>$list,'total'=>$total,'haveOrder'=>$haveOrder,'haveProduct'=>$haveProduct,'isPromisePay'=>$isPromisePay,'isPromiseExamine'=>$isPromiseExamine,'promisePrice'=>$promisePrice);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 申请退还保证金
    public function InsertPromisePrice()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));
        
        $user_id = cache($access_id.'_uid'); // 用户user_id
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
        }

        $time = date("Y-m-d H:i:s");

        $r0_0 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id,'recovery'=>0])->field('account_money,is_open')->select()->toArray();
        if($r0_0[0]['account_money'] > 0)
        {
            $message = Lang('mch.84');
            return output(109,$message);
        }

        $r0_1 = OrderModel::where(['store_id'=>$store_id])->whereLike('mch_id','%,'.$mch_id.',%')->whereIn('status','0,1,2')->field('id')->select()->toArray();
        if($r0_1)
        {
            $message = Lang('mch.83');
            return output('50761',$message);
        }

        $sql0_2 = "select id from lkt_product_list where store_id = '$store_id' and mch_id = '$mch_id' and status = 2 and recycle = 0 ";
        $r0_2 = Db::query($sql0_2);
        if($r0_2)
        {
            $message = Lang('mch.85');
            return output('50761',$message);
        }

        $r0 = MchPromiseModel::where(['mch_id'=>$mch_id])->order('add_date','desc')->field('id,promise_amt,pay_type')->select()->toArray();
        if($r0)
        {
            $promise_amt = $r0[0]['promise_amt'];
            $pay_type = $r0[0]['pay_type'];
            
            $r_0 = PromiseShModel::where(['mch_id'=>$mch_id,'status'=>3,'is_pass'=>3])->field('id')->select()->toArray();
            if($r_0)
            {
                $message = Lang('promise.0');
                return output(109,$message);
            }
    
            $sql_insert = array('mch_id'=>$mch_id,'promise_amt'=>$promise_amt,'status'=>3,'pay_type'=>$pay_type,'is_pass'=>3,'add_date'=>$time);
            $r1 = Db::name('promise_sh')->insertGetId($sql_insert);
            if($r1 > 0)
            {
                $sql_3 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'money'=>$promise_amt,'type'=>2,'status'=>0,'add_date'=>$time,'promise_sh_id'=>$r1);
                $r_3 = Db::name('promise_record')->insert($sql_3);
                
                $message_30 = "ID为" . $mch_id . "的店铺申请提取保证金，请及时处理！";
                $message_logging_list30 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>0,'type'=>30,'parameter'=>$mch_id,'content'=>$message_30);
                PC_Tools::add_message_logging($message_logging_list30);
                
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加保证金审核成功！参数：" . json_encode($sql_insert));
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "添加保证金审核失败！参数：" . json_encode($sql_insert));
                $message = Lang('Busy network');
                return output(109,$message);
            }
        }
        else 
        {
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("mch/Mch/promise.log",$Log_content);
        return;
    }
}
