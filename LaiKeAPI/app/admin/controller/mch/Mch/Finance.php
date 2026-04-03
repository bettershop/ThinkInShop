<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cache;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;
use app\common\ExcelUtils;
use app\common\Jurisdiction;
use app\common\Plugin\MchPublicMethod;

use app\admin\model\MchModel;
use app\admin\model\MchAccountLogModel;
use app\admin\model\MchConfigModel;
use app\admin\model\BankCardModel;
use app\admin\model\UserModel;
use app\admin\model\WithdrawModel;

/**
 * 功能：资金
 * 修改人：DHB
 */
class Finance extends BaseController
{
    // 入账/出账记录
    public function RevenueRecords()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $status = addslashes(trim($this->request->param('status'))); // 状态：1.入账 2.出账
        $type = addslashes(trim($this->request->param('type'))); // 类型：1.订单 2.退款 3.提现
        $search = addslashes(trim($this->request->param('oid'))); // 开始时间
        $startdate = addslashes(trim($this->request->param('startDate'))); // 开始时间
        $enddate = addslashes(trim($this->request->param('endDate'))); // 结束时间
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $user_id = cache($access_id.'_uid'); // 用户user_id
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_id . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }

        $condition = " store_id = '$store_id' and mch_id = '$mch_id' and status = '$status' ";

        if($search != '')
        {
            $search_0 = Tools::FuzzyQueryConcatenation($search);
            $condition .= " and remake like $search_0 ";
        }
        if ($startdate)
        {
            $condition .= " and addtime > '$startdate' ";
        }
        if ($enddate)
        {
            $condition .= " and addtime < '$enddate' ";
        }
        $total = 0;
        $sql0 = "select count(id) as total from lkt_mch_account_log where $condition";
        $r0 = Db::query($sql0);
        if ($r0)
        {
            $total = $r0[0]['total'];
        }

        $list = array();
        $z_price = 0;
        $sql1 = "select * from lkt_mch_account_log where $condition order by addtime desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                if ($v['type'] == 1)
                {
                    $v['typeName'] = '订单支付';
                }
                else if ($v['type'] == 2)
                {
                    $v['typeName'] = '订单退款';
                }
                else if ($v['type'] == 3)
                {
                    $v['typeName'] = '提现';
                }
                else if ($v['type'] == 4)
                {
                    $v['typeName'] = '保证金';
                }
                else if ($v['type'] == 5)
                {
                    $v['typeName'] = '供应商订单';
                }
                $v['price'] = round($v['price'],2);
                $list[] = $v;
            }
        }

        //请求为导出
        if ($exportType)
        {
            if($status == 1)
            {
                $res = '入账记录';
                $titles = array(
                    0 => '序号',
                    1 => 'ID',
                    2 => '入账金额',
                    3 => '入账类型',
                    4 => '入账时间'
                );
            }
            else
            {
                $res = '出账记录';
                $titles = array(
                    0 => '序号',
                    1 => 'ID',
                    2 => '出账金额',
                    3 => '出账类型',
                    4 => '出账时间'
                );
            }
            
            $exportExcel_list = array();
            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $k0 = $k + 1;
                    $exportExcel_list[] = array(
                        $k0,
                        $v['id'],
                        $v['price'],
                        $v['typeName'],
                        $v['addtime']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, $res);
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }

        $sql_z = "select ifnull(sum(price),0) as price from lkt_mch_account_log where $condition ";
        $r_z = Db::query($sql_z);
        if($r_z)
        {
            $z_price = $r_z[0]['price'];
        }
        $income = 0;
        $outcome = 0;
        if($status == 1)
        {
            $income = $z_price;
        }
        else
        {
            $outcome = $z_price;
        }
        
        $data = array('list' => $list,'total' => $total,'income' => $income,'outcome'=>$outcome);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 提现记录
    public function WithdrawList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $status = addslashes(trim($this->request->param('status'))); // 提现状态 0.提现未审核 1.提现已通过 2提现已拒绝
        $startdate = addslashes(trim($this->request->param('startDate'))); // 开始时间
        $enddate = addslashes(trim($this->request->param('endDate'))); // 结束时间
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $user_id = cache($access_id.'_uid'); // 用户user_id

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_id . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        //拼接查询条件
        $conditions = " a.store_id = '$store_id' and a.user_id = '$user_id' and is_mch = 1 ";
        if ($status != '' || $status === '0')
        {
            $conditions .= " and a.status = '$status' ";
        }
        if ($startdate)
        {
            $conditions .= " and a.add_date > '$startdate' ";
        }
        if ($enddate)
        {
            $conditions .= " and a.add_date < '$enddate' ";
        }

        $total = 0;
        //总记录数
        $sql0 = "select count(a.id) as num from lkt_withdraw as a left join lkt_bank_card as b on a.Bank_id = b.id where $conditions ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        $list = array();
  
        $sql1 = "select a.id,a.user_id,a.name,a.mobile,a.money,a.s_charge,a.status,a.add_date,b.Cardholder,b.Bank_name,b.branch,b.Bank_card_number,a.refuse,CASE a.status WHEN 0 THEN '待审核' WHEN 1 THEN '审核通过' WHEN 2 THEN '审核拒绝' ELSE '未知' END as type_name from lkt_withdraw as a left join lkt_bank_card as b on a.Bank_id = b.id  where $conditions order by add_date desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['money'] = round($v['money'],2);
                $v['s_charge'] = round($v['s_charge'],2);
                $v['arrivalMoney'] = round($v['money'] - $v['s_charge'],2);
                $user_id = $v['user_id'];

                $v['source'] = '';
                $v['sourceName'] = '';
                $v['userName'] = '';

                $r_user = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('source,user_name')->select()->toArray();
                if($r_user)
                {
                    $v['source'] = $r_user[0]['source'];
                    $v['userName'] = $r_user[0]['user_name'];
                }

                if($v['source'] == 1)
                {
                    $v['sourceName'] = '小程序';
                }
                else if($v['source'] == 2)
                {
                    $v['sourceName'] = 'APP';
                }
                else if($v['source'] == 3)
                {
                    $v['sourceName'] = '支付宝小程序';
                }
                else if($v['source'] == 4)
                {
                    $v['sourceName'] = '头条小程序';
                }
                else if($v['source'] == 5)
                {
                    $v['sourceName'] = '百度小程序';
                }
                else if($v['source'] == 6)
                {
                    $v['sourceName'] = 'PC端';
                }
                else if($v['source'] == 7)
                {
                    $v['sourceName'] = 'H5';
                }
                $list[] = $v;
            }
        }

        $money = 0;
        $cashable_money = 0;
        //查询账户余额
        $r_1 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('account_money,cashable_money')->select()->toArray();
        if($r_1)
        {
            $money = round($r_1[0]['account_money'],2);
            $cashable_money = round($r_1[0]['cashable_money'],2);
        }
        
        $day = date("j"); // 获取今天是几号，没有前置0
        $PopUpContent = ''; // 弹窗内容
        $illustrate = '';
        $withdrawal_time_open = ''; // 提现时间开关 0.不限制 1.指定日期 2.指定时间段
        $withdrawal_time = ''; // 指定时间(时间段:15-20)
        $r_3 = MchConfigModel::where(['store_id'=>$store_id])->field('min_charge,max_charge,service_charge,illustrate,withdrawal_time_open,withdrawal_time')->select()->toArray();
        if ($r_3)
        {
            $min_charge = floatval($r_3[0]['min_charge']); // 最小提现金额
            $max_charge = floatval($r_3[0]['max_charge']); // 最大提现金额
            $service_charge = floatval($r_3[0]['service_charge']) ;
            $illustrate = $r_3[0]['illustrate'];
            $withdrawal_time_open = $r_3[0]['withdrawal_time_open']; // 提现时间开关 0.不限制 1.指定日期 2.指定时间段
            $withdrawal_time = $r_3[0]['withdrawal_time']; // 指定时间(时间段:15-20)
            if($withdrawal_time_open == 1)
            {
                if($withdrawal_time != $day)
                {
                    $PopUpContent = Lang('mch.87') . $withdrawal_time . Lang('mch.90'); // 弹窗内容
                }
            }
            else if($withdrawal_time_open == 2)
            {
                $withdrawal_time_list = explode('-',$withdrawal_time);
                if($day < $withdrawal_time_list[0] || $day > $withdrawal_time_list[1])
                {
                    $PopUpContent = Lang('mch.88') . $withdrawal_time_list[0] . Lang('mch.89') . $withdrawal_time_list[1] . Lang('mch.90'); // 弹窗内容
                }
            }
        }
        else
        {
            $message = Lang('finance.0');
            return output(109,$message);
        }
        
        $array = array('store_id'=>$store_id,'shop_id'=>$mch_id);
        $mch = new MchPublicMethod();
        $mch_array = $mch->is_margin($array);
        $is_Payment = $mch_array['is_Payment'];
        $isPromiseExamine = $mch_array['isPromiseExamine'];

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '序号',
                1 => '提现金额',
                2 => '手续费',
                3 => '到账金额',
                4 => '银行卡号',
                5 => '开户银行',
                6 => '状态',
                7 => '提现时间',
                8 => '备注'
            );
            $exportExcel_list = array();
            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $k0 = $k + 1;
                    $exportExcel_list[] = array(
                        $k0,
                        $v['money'],
                        $v['s_charge'],
                        $v['arrivalMoney'],
                        $v['Bank_card_number'],
                        $v['Bank_name'],
                        $v['type_name'],
                        $v['add_date'],
                        $v['refuse']
                    );
                }
            }
            ExcelUtils::exportExcel($exportExcel_list, $titles, '提现记录');
            exit;
        }

        $data = array('list' => $list,'money' => $money,'total' => $total,'cashable_money' => $cashable_money,'min_charge' => $min_charge,'max_charge' => $max_charge,'service_charge' => $service_charge,'illustrate' => $illustrate,'PopUpContent' => $PopUpContent, 'is_Payment' => $is_Payment, 'isPromiseExamine' => $isPromiseExamine);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 售后明细
    public function ReturnDetail()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $startdate = addslashes(trim($this->request->param('startDate'))); // 开始时间
        $enddate = addslashes(trim($this->request->param('endDate'))); // 结束时间
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $user_id = cache($access_id.'_uid'); // 用户user_id

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_id . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $MchAccountLogModel = new MchAccountLogModel();

        $condition = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>2,'status'=>2);
        $r_condition0 = $MchAccountLogModel->where($condition);
        $r_condition1 = $MchAccountLogModel->where($condition);

        if ($startdate)
        {
            $r_condition0->where('addtime','>',$startdate);
            $r_condition1->where('addtime','>',$startdate);
        }
        if ($enddate)
        {
            $r_condition0->where('addtime','<',$enddate);
            $r_condition1->where('addtime','<',$enddate);
        }

        $total = 0;
        $r0 = $r_condition0->field('count(id) as total')->select()->toArray();
        if ($r0)
        {   
            $total = $r0[0]['total'];
        }

        $list = array();
        $r1 = $r_condition1->page((int)$page,(int)$pagesize)->order('addtime','desc')->select()->toArray();
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {   
                $v['account_money'] = (float)$v['account_money'];
                $v['price'] = (float)$v['price'];
                $list[] = $v;
            }
        }

        $data = array('list' => $list,'total' => $total);
        $message = Lang('Success');
        return output("200",$message,$data);
    }

    // 申请提现页面
    public function WithdrawPage()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $user_id = cache($access_id.'_uid'); // 用户user_id

        $wx_name = ''; // 微信名称
        $wx_open = false; // wx_open：是否开启微信余额提现
        $wx_withdraw = false; // 判断用户是否绑定过微信可以进行微信零钱提现
        
        $sql_payment_config = "select p.id from lkt_payment as p left join lkt_payment_config as c on p.id = c.pid where c.store_id = '$store_id' and p.class_name = 'wechat_v3_withdraw' and c.status = 1 ";
        $r_payment_config = Db::query($sql_payment_config);
        if($r_payment_config)
        {
            $wx_open = true; // wx_open：是否开启微信余额提现
        }
        // 根据微信id,查询会员金额
        $r = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();
        if ($r)
        {
            // $wx_name = $r[0]['wx_name']; // 微信名称
            if($r[0]['wx_id'] != '' && $r[0]['wx_id'] != null)
            {
                $wx_withdraw = true; 
                $wx_name = "已绑定";
            }
        }
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id,tel')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
            $tel = $r_mch[0]['tel'];
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_id . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }

        $r_config = MchConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('min_charge,max_charge,service_charge,illustrate')->select()->toArray();
        if ($r_config)
        {
            $min_charge = floatval($r_config[0]['min_charge']); // 最小提现金额
            $max_charge = floatval($r_config[0]['max_charge']); // 最大提现金额
            $service_charge = floatval($r_config[0]['service_charge']);
            $illustrate = $r_config[0]['illustrate'];
        }
        else
        {
            $message = Lang('finance.0');
            return output(109,$message);
        }

        $list = array();
        $r0 = BankCardModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0])->field('id,Bank_name,Bank_card_number')->select()->toArray();
        if($r0)
        {
            foreach($r0 as $k => $v)
            {
                $Bank_card_number = $v['Bank_card_number'];
                require_once('../app/admin/controller/app/bankList.php');
                $r = $this->bankInfo($Bank_card_number, $bankList);
                $v['bandName'] = $r;
                $list[] = $v;
            }
        }

        $data = array('list' => $list,'tel' => $tel,'min_charge' => $min_charge,'max_charge' => $max_charge,'service_charge' => $service_charge,'illustrate' => $illustrate,'wx_name'=>$wx_name,'wx_open'=>$wx_open,'wx_withdraw'=>$wx_withdraw);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 获取验证码
    public function WithdrawalsSms()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $user_id = cache($access_id.'_uid'); // 用户user_id
       
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('cpc,tel')->select()->toArray();
        if ($r_mch)
        {
            $cpc = $r_mch[0]['cpc'];
            $tel = $r_mch[0]['tel'];
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_id . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }

        $Tools = new Tools($store_id, 1);
        $array_code = array('cpc'=>$cpc,'mobile'=>$tel,'type'=>0,'type1'=>6,'bizparams'=>array());
        $res = $Tools->generate_code($array_code);

        $message = Lang('Success');
        return output('200',$message);
    }

    // 申请提现
    public function Withdrawals()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $withdraw_status = addslashes(trim($this->request->param('withdrawStatus'))); // 提现类型 1银行卡 2微信余额
        $bank_id = addslashes(trim($this->request->param('bankId'))); // 银行卡ID
        $amoney = addslashes(trim($this->request->param('amoney'))); // 提现金额
        $mobile = addslashes(trim($this->request->param('mobile'))); // 联系电话
        $keyCode = addslashes(trim($this->request->param('keyCode'))); // 验证码

        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $user_id = cache($access_id.'_uid'); // 用户user_id
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        $source = 2;
		$cashable_money=0;//可提现金额 提现界面显示的是账户余额
		$account_money =0;//账户余额 提现界面显示的是预计到账金额 
        
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->field('id,account_money,tel,cashable_money')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
            $account_money = $r_mch[0]['account_money']; // 商户余额
            $mobile = $r_mch[0]['tel']; // 联系电话
            $cashable_money = $r_mch[0]['cashable_money']; // 商户可提现金额
			if($cashable_money <= 0)
			{
				$Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $admin_name . '可提现金额不足无法提现！';
				$this->Log($Log_content);
				$message = Lang('Illegal invasion');
				return output(109, $message);
			}
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_id . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }

        $r_user = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('user_name')->select()->toArray();
        $user_name = $r_user[0]['user_name'];

        $Tools = new Tools($store_id, 1);
        if($withdraw_status == 1)
        {
            $arr = array($mobile, array('store_id'=>$store_id,'code' => $keyCode));
            $rew = $Tools->verification_code( $arr);
        }

        $r1 = MchConfigModel::where(['store_id'=>$store_id])->field('settlement,min_charge,max_charge,service_charge')->select()->toArray();
        if ($r1)
        {
            $settlement = $r1[0]['settlement']; // 结算方式
            $min_charge = $r1[0]['min_charge']; // 最低提现金额
            $max_charge = $r1[0]['max_charge']; // 最大提现金额
            $service_charge = $r1[0]['service_charge']; // 提现说明
        }
        else
        {
            ob_clean();
            $message = Lang('Unknown error');
            return output(109, $message);
        }

        if ($settlement == 0)
        {
            $time1 = date("Y-m-d 00:00:00");
            $time2 = date("Y-m-d 23:59:59");
        }
        else if ($settlement == 1)
        {
            $time1 = date('Y-m-01 00:00:00', time());
            $time2 = date('Y-m-d 23:59:59', strtotime("$time1 +1 month -1 day"));
        }
        else if ($settlement == 2)
        {
            $season = ceil(date('n') / 3);
            $time1 = date('Y-m-01 00:00:00', mktime(0, 0, 0, ($season - 1) * 3 + 1, 1, date('Y')));
            $time2 = date('Y-m-d 23:59:59', mktime(0, 0, 0, $season * 3, 1, date('Y')));
        }

        // 提现金额不为数字
        if (is_numeric($amoney) == false)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '提现金额不为数字！';
            $this->Log($Log_content);
            ob_clean();
            $message = Lang('user.29');
            return output(109, $message);
        }
        // 提现金额是否小于等于0
        if ($amoney <= 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '提现金额不能小于等于0！';
            $this->Log($Log_content);
            ob_clean();
            $message = Lang('user.42');
            return output(109, $message);
        }
        // 提现金额是否小于最低提现金额
        if ($amoney < $min_charge)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '提现金额不能小于最低提现金额！';
            $this->Log($Log_content);
            ob_clean();
            $message = Lang('user.43');
            return output(109, $message);
        }
        // 提现金额是否大于最大提现金额
        if ($amoney > $max_charge)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '提现金额不能大于最大提现金额！';
            $this->Log($Log_content);
            ob_clean();
            $message = Lang('user.44');
            return output(109, $message);
        }
        // 提现金额 大于 店主金额
        if ($amoney > $cashable_money)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '提现金额不能大于最大可提现金额！';
            $this->Log($Log_content);
            ob_clean();
            $message = Lang('user.44');
            return output(109, $message);
        }

        if ($service_charge == '0.00')
        {
            $cost = 0;  // 实际的手续费
        }
        else
        {
            $cost = $amoney * $service_charge / 100;  // 实际的手续费
        }
        $t_money = $amoney; // 提现金额

        // 根据用户id和未核审,查询数据
        $r0 = WithdrawModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'is_mch'=>1,'status'=>0])->field('count(id) as a')->select()->toArray();
        $count0 = $r0[0]['a']; // 条数
        if ($count0 > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '您的上笔申请还未审核，请稍后再试！';
            $this->Log($Log_content);
            ob_clean();
            $message = Lang('distribution.23');
            return output(109, $message);
        }
        
        // 根据用户id和未核审,查询数据
        $r1 = WithdrawModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'is_mch'=>1,'status'=>1])->where('add_date','>=',$time1)->where('add_date','<=',$time2)->field('count(id) as a')->select()->toArray();
        $count = $r1[0]['a']; // 条数
        if ($count > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '提现申请次数已达上限！';
            $this->Log($Log_content);
            ob_clean();
            $message = Lang('user.31');
            return output(109, $message);
        }

        Db::startTrans();

        $sql_where = array('store_id' => $store_id,'id' => $mch_id);
        $sql_update = array('cashable_money' => Db::raw('cashable_money-' . $t_money));
        $res = Db::name('mch')->where($sql_where)->update($sql_update);

        $time = date("Y-m-d H:i:s");
        // 在提现列表里添加一条数据
        $insert_0 = array('store_id' => $store_id,'user_id' => $user_id,'name' => $user_name,'mobile' => $mobile,'Bank_id' => $bank_id,'money' => $t_money,'z_money' => $account_money,'s_charge' => $cost,'status' => 0,'add_date' => $time,'is_mch' => 1,'withdraw_status'=>$withdraw_status);
        $r_0 = Db::name('withdraw')->insertGetId($insert_0);
        if ($r_0 > 0)
        {
            $event = '店主' . $user_id . '申请提现' . $t_money . '元余额';
            $insert = array('store_id' => $store_id,'user_id' => $user_id,'money' => $t_money,'oldmoney' => $cashable_money,'event' => $event,'type' => 2,'is_mch' => 1);
            $r = Db::name('record')->insert($insert);
            if($withdraw_status == 1)
            {
                if ($rew)
                {
                    $r2 = Db::table('lkt_session_id')->where('id',$rew)->delete();
                    if ($r2 <= 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '删除短信信息失败！参数:' . $rew;
                        $this->Log($Log_content);
                        Db::rollback();
                        ob_clean();
                        $message = Lang('Abnormal business');
                        return output(109, $message);
                    }
                }
            }

            $message_20 = "ID为" . $mch_id . "的店铺申请提取余额，请及时处理！";
            $message_logging_list20 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>0,'type'=>20,'parameter'=>$r_0,'content'=>$message_20);
            PC_Tools::add_message_logging($message_logging_list20);

            $message_24 = "您的店铺提现申请已提交成功，正在等待管理员审核！";
            $message_logging_list24 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>24,'parameter'=>$r_0,'content'=>$message_24);
            PC_Tools::add_message_logging($message_logging_list24);
            
            $Log_content = __METHOD__ . '->' . __LINE__ . '提现成功！';
            $this->Log($Log_content);
            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator, '申请了提现操作',1,$source,$shop_id,$operator_id);
            ob_clean();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加提现记录失败！参数:' . json_encode($insert_0);
            $this->Log($Log_content);
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '申请了提现操作失败',1,$source,$shop_id,$operator_id);
            ob_clean();
            $message = Lang('Abnormal business');
            return output(109, $message);
        }
    }

    // 银行卡列表
    public function BankList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 开始时间
        $startdate = addslashes(trim($this->request->param('startDate'))); // 开始时间
        $enddate = addslashes(trim($this->request->param('endDate'))); // 结束时间
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $user_id = cache($access_id.'_uid');

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_id . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $BankCardModel = new BankCardModel();
        
        $condition = array('store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0);
        $r_condition0 = $BankCardModel->where($condition);
        $r_condition1 = $BankCardModel->where($condition);
        if($id != '' && $id != 0)
        {
             $r_condition0->where('id',$id);
             $r_condition1->where('id',$id);
        }
        if ($startdate)
        {
            $r_condition0->where('add_date','>',$startdate);
            $r_condition1->where('add_date','>',$startdate);
        }
        if ($enddate)
        {
            $r_condition0->where('add_date','<',$enddate);
            $r_condition1->where('add_date','<',$enddate);
        }

        $total = 0;
        $r0 = $r_condition0->field('count(id) as total')->select()->toArray();
        if ($r0)
        {
            $total = $r0[0]['total'];
        }

        $list = array();
        $r1 = $r_condition1->page((int)$page,(int)$pagesize)->order('add_date','desc')->select()->toArray();
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $list[] = $v;
            }
        }

        $data = array('list' => $list,'total' => $total);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 验证卡号与银行名是否匹配
    public function Verification()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        $Bank_name = trim(Request::param('Bank_name')); // 银行名称
        $Bank_card_number = trim(Request::param('Bank_card_number')); // 银行卡号
        // 银行卡号不为数字
        if (is_numeric($Bank_card_number) == false)
        {
            ob_clean();
            $message =  Lang('Parameter error');
            return output(400,$message);
        }
        // 根据卡号,查询银行名称
        require_once('../app/admin/controller/app/bankList.php');
        $r = $this->bankInfo($Bank_card_number, $bankList);
        if ($r == '')
        {
            ob_clean();
            $message = Lang('finance.3');
            return output(400,$message);
        }
        else
        {
            $name = strstr($r, '银行', true) . "银行";
            if ($Bank_name)
            {
                if ($name != $Bank_name)
                {
                    ob_clean();
                    $message = Lang('finance.5');
                    return output(400,$message);
                }
                else
                {
                    ob_clean();
                    $message =  Lang('Success');
                    return output(200,$message,array('Bank_name' => $Bank_name));
                }
            }
            else
            {
                $Bank_name = $name;
                ob_clean();
                $message =  Lang('Success');
                return output(200,$message,array('Bank_name' => $Bank_name));
            }
        }
    }

    // 添加/编辑银行卡
    public function AddBank()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 银行卡ID
        $Cardholder = addslashes(trim($this->request->param('cardholder'))); // 开户人
        $Bank_name = addslashes(trim($this->request->param('bankName'))); // 银行名称
        $branch = addslashes(trim($this->request->param('branchName'))); // 支行名称
        $Bank_card_number = addslashes(trim($this->request->param('bankCardNumber'))); // 卡号

        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $user_id = cache($access_id.'_uid');
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_id . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }

        if($Cardholder == '')
        {
            $message = Lang('finance.1');
            return output(109, $message);
        }

        // 银行卡号不为数字
        if (is_numeric($Bank_card_number) == false)
        {
            $message = Lang('finance.2');
            return output(109, $message);
        }
        else
        {
            if (strlen($Bank_card_number) != 19 && strlen($Bank_card_number) != 16)
            {
                $message = Lang('finance.3');
                return output(109, $message);
            }
            //查询是否重复添加
            if($id != '' && $id != 0)
            {
                $res_b = BankCardModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'Bank_card_number'=>$Bank_card_number,'recycle'=>0])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            else
            {
                $res_b = BankCardModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'Bank_card_number'=>$Bank_card_number,'recycle'=>0])->field('id')->select()->toArray();
            }
            if ($res_b) 
            {
                $message = Lang('finance.4');
                return output(109, $message);
            }
        }
        
        // 根据卡号,查询银行名称
        require_once('../app/admin/controller/app/bankList.php');
        $r = $this->bankInfo($Bank_card_number, $bankList);
        if ($r == '')
        {
            $message = Lang('finance.3');
            return output(109, $message);
        }
        else
        {
            $name = strstr($r, '银行', true) . "银行";
            if ($name != $Bank_name)
            {
                $message = Lang('finance.5');
                return output(109, $message);
            }
        }
        
        if($branch == '')
        {
            $message = Lang('finance.6');
            return output(109, $message);
        }

        if($id != '' && $id != 0)
        {
            $sql1_update = array('Cardholder'=>$Cardholder,'Bank_name'=>$Bank_name,'branch'=>$branch,'Bank_card_number'=>$Bank_card_number);
            $sql1_where = array('id'=>$id);
            $r1 = Db::name('bank_card')->where($sql1_where)->update($sql1_update);
            if($r1 < 0)
            {
                $message = Lang('finance.8');
                return output(109, $message);
            }

            $Jurisdiction->admin_record($store_id, $operator, '修改银行卡,ID为' . $id, 2,2,$shop_id,$operator_id);
        }
        else
        {
            $is_default = 1;
            $r0 = BankCardModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0])->field('id')->select()->toArray();
            if($r0)
            {
                $is_default = 0;
            }
            
            $sql1 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'Cardholder'=>$Cardholder,'Bank_name'=>$Bank_name,'branch'=>$branch,'Bank_card_number'=>$Bank_card_number,'is_default'=>$is_default);
            $r1 = Db::name('bank_card')->insertGetId($sql1);
            if($r1 < 1)
            {
                $message = Lang('finance.9');
                return output(109, $message);
            }

            $Jurisdiction->admin_record($store_id, $operator, '添加银行卡,ID为' . $r1, 1,2,$shop_id,$operator_id);
        }

        $message = Lang('Success');
        return output(200, $message);
    }

    // 设为默认
    public function SetDefault()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('bankId')); // 银行卡ID
        
        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $user_id = cache($access_id.'_uid');
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_id . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }

        $r0 = BankCardModel::where(['id'=>$id])->field('is_default')->select()->toArray();
        if($r0)
        {
            $is_default = $r0[0]['is_default'];
            if($is_default == 1)
            {
                $message = Lang('finance.7');
                return output(109, $message);
            }
            else
            {
                $sql1_update = array('is_default'=>1);
                $sql1_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0,'id'=>$id);
                $r1 = Db::name('bank_card')->where($sql1_where)->update($sql1_update);
                if($r1 < 0)
                {
                    $message = Lang('finance.8');
                    return output(109, $message);
                }

                $sql2_update = array('is_default'=>0);
                $sql2_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0);
                $r2 = Db::name('bank_card')->where($sql2_where)->where('id','<>',$id)->update($sql2_update);
                if($r2 < 0)
                {
                    $message = Lang('finance.8');
                    return output(109, $message);
                }

                $Jurisdiction->admin_record($store_id, $operator, '更改银行卡默认,ID为' . $id, 2,2,$shop_id,$operator_id);
                $message = Lang('Success');
                return output(200, $message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109, $message);
        }
    }

    // 删除银行卡
    public function DelBank()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('id')); // 银行卡ID

        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $user_id = cache($access_id.'_uid');
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_id . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }

        $r0 = BankCardModel::where(['id'=>$id])->field('is_default')->select()->toArray();
        if($r0)
        {
            $is_default = $r0[0]['is_default'];
            if($is_default == 1)
            {
                $message = Lang('finance.11');
                return output(109, $message);
            }
        }

        $sql1_update = array('recycle'=>1);
        $sql1_where = array('id'=>$id);
        $r1 = Db::name('bank_card')->where($sql1_where)->update($sql1_update);
        if($r1 < 0)
        {
            $message = Lang('finance.10');
            return output(109, $message);
        }

        $Jurisdiction->admin_record($store_id, $operator, '删除银行卡,ID为' . $id, 3,2,$shop_id,$operator_id);
        $message = Lang('Success');
        return output(200, $message);
    }

    // 验证卡号是否跟银行匹配
    function bankInfo($card, $bankList)
    {
        $card_8 = substr($card, 0, 8);
        if (isset($bankList[$card_8]))
        {
            return $bankList[$card_8];
        }
        $card_6 = substr($card, 0, 6);
        if (isset($bankList[$card_6]))
        {
            return $bankList[$card_6];
        }
        $card_5 = substr($card, 0, 5);
        if (isset($bankList[$card_5]))
        {
            return $bankList[$card_5];
        }
        $card_4 = substr($card, 0, 4);
        if (isset($bankList[$card_4]))
        {
            return $bankList[$card_4];
        }
        return '';
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("mch/finance.log",$Log_content);
        return;
    }
}

