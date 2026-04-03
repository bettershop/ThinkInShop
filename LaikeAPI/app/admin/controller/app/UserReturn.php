<?php
namespace app\admin\controller\app;

// require_once('../app/common/alipay/test.php');

use app\BaseController;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\wxpayv2\wxpay;

use app\admin\model\UserModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\BondModel;
use app\admin\model\ReturnRecordModel;

class UserReturn extends BaseController
{
    public $appid;
    public $mch_id;
    public $appsecret;
    public $ip = '120.76.189.152';
    public $mch_key;

    // 关闭订单 并退款
    public function return_bond()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->post('access_id')); // 授权id

        $id = trim($this->request->post('id')); // 

        $user_id = $this->user_list['user_id'];

        $appid = '';
        //查询保证金记录
        $res = BondModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'id'=>$id,'is_pay'=>1,'is_back'=>0])->select()->toArray();
        if ($res)
        {
            $money = $res[0]['money'];
            $trade_no = $res[0]['trade_no'];
            $pay = $res[0]['type'];
        }
        else
        {
            $message = Lang('Parameter error');
            return output(400,$message);
        }

        //不同支付方式判断
        switch ($pay)
        {
            case 'aliPay' :
                //支付宝手机支付
                $zfb_res = app('Alipay')::refund($trade_no, $money, $appid, $store_id, $pay);
                // $zfb_res = Alipay::refund($trade_no, $money, $appid, $store_id, $pay);
                if ($zfb_res != 'success')
                {
                    $message = Lang('return.0');
                    return output(400,$message);
                }
                else
                {
                    $this->update_bond($id, $store_id, $user_id, $money);
                    $message = Lang('return.1');
                    return output(200,$message);
                }
                break;
            case 'alipay' :
                //支付宝手机支付
                $zfb_res = app('Alipay')::refund($trade_no, $money, $appid, $store_id, $pay);
                // $zfb_res = Alipay::refund($trade_no, $money, $appid, $store_id, $pay);
                if ($zfb_res != 'success')
                {
                    $message = Lang('return.0');
                    return output(400,$message);
                }
                else
                {
                    $this->update_bond($id, $store_id, $user_id, $money);
                    $message = Lang('return.1');
                    return output(200,$message);
                }
                break;
            case 'alipay_minipay' :
                // 支付宝小程序退款
                $zfb_res = app('Alipay')::refund($trade_no, $money, $appid, $store_id, "alipay_minipay");
                // $zfb_res = Alipay::refund($trade_no, $money, $appid, $store_id, "alipay_minipay");
                if ($zfb_res != 'success')
                {
                    $message = Lang('return.0');
                    return output(400,$message);
                }
                else
                {
                    $this->update_bond($id, $store_id, $user_id, $money);
                    $message = Lang('return.1');
                    return output(200,$message);
                }
                break;
            case 'wap_unionpay' :
                // 中国银联手机支付

                break;
            case 'app_wechat' :
                //微信APP支付.
                $wxtk_res = wxpay::wxrefundapi($trade_no, $trade_no . mt_rand(0, 9), $money, $money, $store_id, $pay);
                if ($wxtk_res['result_code'] != 'SUCCESS')
                {
                    if ($wxtk_res['err_code_des'] == '基本账户余额不足，请充值后重新发起')
                    {
                        $message = Lang('return.2');
                        return output(400,$message);
                    }
                }
                else
                {
                    $this->update_bond($id, $store_id, $user_id, $money);
                    $message = Lang('return.1');
                    return output(200,$message);
                }
                break;
            case 'mini_wechat' :
                //微信小程序支付
                $wxtk_res = wxpay::wxrefundapi($trade_no, $trade_no . mt_rand(0, 9), $money, $money, $store_id, $pay);
                if ($wxtk_res['result_code'] != 'SUCCESS')
                {
                    if ($wxtk_res['err_code_des'] == '基本账户余额不足，请充值后重新发起')
                    {
                        $message = Lang('return.2');
                        return output(400,$message);
                    }
                }
                else
                {
                    $this->update_bond($id, $store_id, $user_id, $money);
                    $message = Lang('return.1');
                    return output(200,$message);
                }
                break;
            case 'jsapi_wechat' :
                //微信小程序支付
                $wxtk_res = wxpay::wxrefundapi($trade_no, $trade_no . mt_rand(0, 9), $money, $money, $store_id, $pay);
                if ($wxtk_res['result_code'] != 'SUCCESS')
                {
                    if ($wxtk_res['err_code_des'] == '基本账户余额不足，请充值后重新发起')
                    {
                        $message = Lang('return.2');
                        return output(400,$message);
                    }
                }
                else
                {
                    $this->update_bond($id, $store_id, $user_id, $money);
                    $message = Lang('return.1');
                    return output(200,$message);
                }
                break;
            case 'baidu_pay' :

            default:
                echo $pay . '支付方式不存在！';
                exit;
        }
    }

    public function update_bond($id, $store_id, $user_id, $money)
    {
        $time = date("Y-m-d H:i:s");
        $res = Db::name('_bond')->where(['id'=>$id])->update(['is_back' => '1']);

        $event = "退回保证金";

        $sql2 = array('store_id'=>$store_id,'user_id'=>$user_id,'money'=>$money,'oldmoney'=>$money,'add_date'=>$time,'event'=>$event,'type'=>45,'is_mch'=>0);
        $res2 = Db::name('_record')->insert($sql2);
    }

    //售后确认收货
    public function confirm_receipt()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->post('access_id')); // 授权id

        $id = trim($this->request->post('id')); // 售后订单ID

        Db::startTrans();

        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if (!empty($r))
        {
            $user_id = $r[0]['user_id'];

            $res_s = ReturnOrderModel::where(['store_id'=>$store_id,'id'=>$id])->field('p_id,sNo,pid,sid,re_type')->select()->toArray();
            if($res_s)
            {   
                $oid = $res_s[0]['p_id'];//订单详情ID
                $sNo = $res_s[0]['sNo'];//订单号
                $pid = $res_s[0]['pid'];//商品id
                $sid = $res_s[0]['sid'];//规格id
                $re_type = $res_s[0]['re_type'];//售后类型

                $r1 = OrderDetailsModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'id'=>$oid])->field('exchange_num')->select()->toArray();
                if($r1)
                {
                    $exchange_num = (int)$r1[0]['exchange_num'] + 1;
                }
            }
            else
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "售后订单ID错误！参数:" . $id);
                $message = Lang('Parameter error');
                return output(400,$message);
            }

            $res = Db::name('return_order')->where(['store_id'=>$store_id,'id'=>$id])->update(['r_type' => '12']);
            if ($res < 0)
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "更新售后订单失败！参数:" . $id);
                Db::rollback();
                ob_clean();
                $message = Lang('operation failed');
                return output(400,$message);
            }
            else
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "更新售后订单成功！参数:" . $id);
            }

            $sql0 = array('store_id'=>$store_id,'user_id'=>$user_id,'id'=>$oid);
            $r0 = Db::name('order_details')->where($sql0)->update(['exchange_num'=>Db::raw('exchange_num+1')]);
            if($r0 < 0)
            {
                $this->Log(__METHOD__ . ":" . __LINE__ . "更新订单失败！参数:" . json_encode($sql0));
                Db::rollback();
                $message = Lang('return.3');
                return output(400,$message);
            }
            else
            {   
                //添加售后记录
                $sql_r = new ReturnRecordModel();
                $sql_r->user_id = $user_id;
                $sql_r->store_id = $store_id;
                $sql_r->re_type = $re_type;
                $sql_r->r_type = 12;
                $sql_r->sNo = $sNo;
                $sql_r->money = 0;
                $sql_r->product_id = $pid;
                $sql_r->attr_id = $sid;
                $sql_r->re_time = date('Y-m-d H:i:s');
                $sql_r->p_id = $oid;
                $sql_r->save();

                Db::commit();
                $message = Lang('return.4');
                return output(200,$message);
            }
        }
        else
        {
            ob_clean();
            $message = Lang('Illegal invasion');
            return output(400,$message);
        }
    }

    // 店铺日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/return.log",$Log_content);
        return;
    }
}

?>