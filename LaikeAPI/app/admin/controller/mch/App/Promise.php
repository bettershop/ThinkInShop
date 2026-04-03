<?php
namespace app\admin\controller\mch\App;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\Jurisdiction;
use app\common\LaiKeLogUtils;
use app\common\GETUI\LaikePushTools;
use app\common\Plugin\RefundUtils;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\alipay0\aop\test\AlipayReturn;

use app\admin\model\MchModel;
use app\admin\model\MchPromiseModel;
use app\admin\model\PromiseShModel;
use app\admin\model\MchConfigModel;
use app\admin\model\RecordModel;
use app\admin\model\OrderModel;
use app\admin\model\UserModel;

class Promise extends BaseController
{
    // 缴纳保证金页面
    public function Index()
    {
        $store_id = addslashes(trim($this->request->param('store_id')));
        $access_id = addslashes(trim($this->request->param('access_id')));

        $id = addslashes(trim($this->request->param('id'))); // 保证金审核ID

        $user_id = $this->user_list['user_id'];

        $r = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();
        $userMoney = round($r[0]['money'],2); // 用户余额
        $password = $r[0]['password']; // 支付密码
        
        if ($password != '')
        {
            $isSetPayment = true;
        }
        else
        {
            $isSetPayment = false;
        }

        $time = date("Y-m-d H:i:s");

        $payment = Tools::getPayment($store_id);

        $r0 = MchConfigModel::where(['store_id'=>$store_id])->order('id','asc')->field('promise_switch,promise_amt,promise_text')->select()->toArray();
        if($r0)
        {
            $promiseSwitch = $r0[0]['promise_switch']; // 保证金开关
            $promisePrice = round($r0[0]['promise_amt'],2); // 保证金
            $promiseText = $r0[0]['promise_text']; // 保证金说明

            $data = array('payment'=>$payment,'isSetPayment'=>$isSetPayment,'promisePrice'=>$promisePrice,'promiseSwitch'=>$promiseSwitch,'promiseText'=>$promiseText,'userMoney'=>$userMoney);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else 
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 缴纳保证金
    public function Payment()
    {
        $store_id = addslashes(trim($this->request->param('store_id')));
        $access_id = addslashes(trim($this->request->post('access_id')));

        $mch_id = addslashes(trim($this->request->post('mch_id'))); // 店铺ID
        $money = addslashes(trim($this->request->post('money'))); // 金额
        $pwd = MD5(addslashes(trim($this->request->post('pwd')))); // 密码
        $payType = addslashes(trim($this->request->post('payType'))); // 支付方式

        $user_id = $this->user_list['user_id'];
        $user_money = $this->user_list['money'];

        $time = date("Y-m-d H:i:s");

        $promise_switch = 0;
        $r0_0 = MchConfigModel::where(['store_id'=>$store_id])->field('promise_switch')->select()->toArray();
        if($r0_0)
        {
            $promise_switch = $r0_0[0]['promise_switch']; // 保证金开关
        }

        if($promise_switch == 0)
        {
            $message = Lang('mch.82');
            return output(50663,$message);
        }

        Db::startTrans();

        $Toosl = new Tools(1,1);
        $sNo = $Toosl->Generate_order_number('PR', 'orderNo'); // 生成订单号

        $data_ = array('paymentAmt'=>$money,'mchId'=>$mch_id,'user_id'=>$user_id,'pay'=>$payType,'storeId'=>$store_id);
        $sql_inser = array('trade_no'=>$sNo,'order_type'=>$payType,'data'=>json_encode($data_),'addtime'=>$time,'status'=>0,'pay_type'=>'wallet_pay');
        $r0 = Db::name('order_data')->insertGetId($sql_inser);
        if($r0 > 0)
        {
            if($payType == 'wallet_pay')
            {
                $sql_where = array('store_id'=>$store_id,'user_id'=>$user_id);
                $sql_update = array('money'=>Db::raw('money-'.$money));
                $r = Db::name('user')->where($sql_where)->where('money','>',0)->update($sql_update);
                if($r <= 0)
                { // 回滚删除已经创建的订单
                    $this->Log(__METHOD__ . ":" . __LINE__ . "修改用户余额失败！where参数:" . json_encode($sql_where) . ";update参数：" . json_encode($sql_update));
                    Db::rollback();
                    ob_clean();
                    $message = Lang('operation ailed');
                    return output(400, $message);
                }
                
                $array = array('store_id'=>$store_id,'money'=>$money,'user_money'=>$user_money,'type'=>10,'money_type'=>2,'money_type_name'=>6,'record_notes'=>'','type_name'=>'','s_no'=>$sNo,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
                $details_id = PC_Tools::add_Balance_details($array);

                $event_1 = $user_id . '缴纳了' . $money . '元店铺押金！';
                $sql_1 = array('store_id'=>$store_id,'user_id'=>$user_id,'money'=>$money,'oldmoney'=>$user_money,'event'=>$event_1,'type'=>38,'details_id'=>$details_id);
                $r_1 = Db::name('record')->insert($sql_1);

                $sql_4 = "select id from lkt_mch_promise where mch_id = '$mch_id' order by add_date desc limit 1 ";
                $r_4 = Db::query($sql_4);
                if($r_4)
                {
                    $id = $r_4[0]['id'];

                    $sql_3_where = array('id'=>$id,'mch_id'=>$mch_id);
                    $sql_3_update = array('orderNo'=>$sNo,'promise_amt'=>$money,'status'=>1,'pay_type'=>$payType,'is_return_pay'=>0,'add_date'=>$time);
                    $r_3 = Db::name('mch_promise')->where($sql_3_where)->update($sql_3_update);
                }
                else
                {
                    $id = Tools::getUid();
                    $sql_3 = array('id'=>$id,'orderNo'=>$sNo,'mch_id'=>$mch_id,'promise_amt'=>$money,'status'=>1,'pay_type'=>$payType,'is_return_pay'=>0,'add_date'=>$time);
                    $r_3 = Db::name('mch_promise')->insertGetId($sql_3);
                }
                
                $event_2 = '缴纳店铺保证金';
                $sql_2 = array('store_id'=>$store_id,'user_id'=>$user_id,'money'=>$money,'oldmoney'=>$user_money,'event'=>$event_2,'type'=>40,'main_id'=>$mch_id);
                $r_2 = Db::name('record')->insert($sql_2);

                $sql_3 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'money'=>$money,'type'=>1,'status'=>1,'add_date'=>$time);
                $r_3 = Db::name('promise_record')->insert($sql_3);
            }

            Db::commit();
            $data = array('orderId'=>$r0,'orderNo'=>$sNo,'total'=>$money);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            $this->Log(__METHOD__ . ":" . __LINE__ . "添加保证金记录失败！参数：" . json_encode($sql_inser));
            Db::rollback();
            $message = Lang('Busy network');
            return output(109,$message);
        }
    }
    
    // 保证金管理
    public function PromiseManage()
    {
        $store_id = addslashes(trim($this->request->param('store_id')));
        $access_id = addslashes(trim($this->request->post('access_id')));

        $mch_id = addslashes(trim($this->request->post('mch_id'))); // 店铺ID

        $user_id = $this->user_list['user_id'];
        $user_money = $this->user_list['money'];

        $refusedWhy = array();
        $status = '';
        $add_date = '';

        $r0 = MchConfigModel::where(['store_id'=>$store_id])->order('id','asc')->field('promise_amt,promise_text')->select()->toArray();
        if($r0)
        {
            $promisePrice = round($r0[0]['promise_amt'],2); // 保证金
            $promiseText = $r0[0]['promise_text']; // 保证金说明

            $r1 = MchPromiseModel::where(['mch_id'=>$mch_id])->field('add_date')->select()->toArray();
            if($r1)
            {
                $add_date = $r1[0]['add_date']; // 保证金
            }

            $r2 = PromiseShModel::where(['mch_id'=>$mch_id])->field('status,refused_why')->select()->toArray();
            if($r2)
            {
                $refused_why = $r2[0]['refused_why']; // 拒绝原因
                $status = $r2[0]['status']; // 保证金审核状态 1=通过 2=拒绝 3=审核中
            }

            $data = array('payDate'=>$add_date,'promisePrice'=>$promisePrice,'promiseText'=>$promiseText,'refusedWhy'=>$refusedWhy,'status'=>$status);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else 
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 缴纳记录
    public function PromiseList()
    {
        $store_id = addslashes(trim($this->request->param('store_id')));
        $access_id = addslashes(trim($this->request->post('access_id')));

        $user_id = $this->user_list['user_id'];

        $list = array();
        $total = 0;

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
        }
        else 
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }

        $r0 = RecordModel::where(['store_id'=>$store_id,'main_id'=>$mch_id])->whereIn('type','40,41')->order('add_date','desc')->select()->toArray();
        if($r0)
        {
            $total = count($r0);
            foreach($r0 as $k => $v)
            {
                $v['money'] = number_format($v['money'],2);
                $v['oldmoney'] = number_format($v['oldmoney'],2);

                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }
    
    // 申请退还保证金
    public function InsertPromisePrice()
    {
        $store_id = addslashes(trim($this->request->param('store_id')));
        $access_id = addslashes(trim($this->request->post('access_id')));
        $mch_id = addslashes(trim($this->request->post('mch_id')));
        
        $user_id = $this->user_list['user_id'];

        $time = date("Y-m-d H:i:s");

        $sql1 = "select id from lkt_mch_promise where mch_id = '$mch_id' and status = 1 and is_return_pay = 0 ";
        $r1 = Db::query($sql1);
        if(!$r1)
        {
            $message = Lang('mch.93');
            return output(50760,$message);
        }

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
        $lktlog->log("mch/promise.log",$Log_content);
        return;
    }
}
