<?php
namespace app\admin\controller\plugin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Tools;

use app\admin\model\UserModel;
use app\admin\model\MemberConfigModel;
use app\admin\model\MemberProModel;
use app\admin\model\ProductListModel;
use app\admin\model\ConfigureModel;
use app\admin\model\AgreementModel;
use app\admin\model\OrderDataModel;
use app\admin\model\PaymentModel;
// 准备弃用
class Member 
{
    // 会员中心
    public function memberCenter()
    {
        $store_id = Request::param('store_id'); // 商城ID
        $store_type = Request::param('store_type');
        $access_id = Request::param('access_id'); // 授权ID
        

        $userInfo = array();
        if($access_id != '')
        {
            $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('id,user_id,user_name,headimgurl,grade,grade_end,grade_m')->select()->toArray();
            if($r0)
            {
                $userInfo = array('id'=>$r0[0]['id'],'user_id'=>$r0[0]['user_id'],'user_name'=>$r0[0]['user_name'],'headimgurl'=>$r0[0]['headimgurl'],'grade'=>$r0[0]['grade'],'grade_end'=>$r0[0]['grade_end'],'grade_m'=>$r0[0]['grade_m']);
            }
            
        }

        $member_discount = 1;
        $member_equity = array();
        $r_config = MemberConfigModel::where('store_id',$store_id)->field('member_discount,member_equity')->select()->toArray();
        if($r_config)
        {
            $member_discount = $r_config[0]['member_discount'];
            $member_equity = json_decode($r_config[0]['member_equity'],true);
        }
        else
        {
            $message = Lang("member.8");
            return output(109,$message);
        }

        $memberPro = array();
        $sql0 = "select p.* from lkt_member_pro as a left join lkt_product_list as p on a.pro_id = p.id where a.store_id = '$store_id' and a.recovery = 0 and p.recycle = 0 order by a.id desc limit 0,10";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k => $v)
            {
                $pro_id = $v['id'];
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id);
                $r2 = ConfigureModel::where(['recycle'=>0,'pid'=>$pro_id])->field('min(price) as price')->select()->toArray();
                if($r2)
                {
                    $v['price'] = floatval($r2[0]['price']);
                    $v['vipPrice'] = round(($r2[0]['price'] * $member_discount * 0.1),2);
                }
                $memberPro[] = $v;
            }
        }

        $data = array('memberEquity'=>$member_equity,'memberPro'=>$memberPro,'userInfo'=>$userInfo);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 会员商品-更多
    public function memberProList()
    {
        $store_id = Request::param('store_id');
        $access_id = Request::param('access_id'); // 授权ID
        $page = Request::param('pageNo');
        $pagesize = Request::param('pageSize');
        if($access_id != '')
        {
            $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('id,user_id,user_name,headimgurl,grade,grade_end,grade_m')->select()->toArray();
            if($r0)
            {
                
            }
            else
            {
                ob_clean();
                $message =  Lang('Illegal invasion');
                return output(400,$message);
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }
        $start = ($page - 1)*$pagesize;

        $member_discount = 1;
        $r_config = MemberConfigModel::where('store_id',$store_id)->field('member_discount,member_equity')->select()->toArray();
        if($r_config)
        {
            $member_discount = $r_config[0]['member_discount'];
        }
        else
        {
            $message = Lang("member.8");
            return output(109,$message);
        }

        $total = 0;
        $sql_num = "select ifnull(count(a.id),0) as num from lkt_member_pro as a left join lkt_product_list as p on a.pro_id = p.id where a.store_id = '$store_id' and a.recovery = 0 and p.recycle = 0";
        $r_0 = Db::query($sql_num);
        if($r_0)
        {
            $total = $r_0[0]['num'];
        }
        $list = array();
        $sql0 = "select p.* from lkt_member_pro as a left join lkt_product_list as p on a.pro_id = p.id where a.store_id = '$store_id' and a.recovery = 0 and p.recycle = 0 order by a.id desc limit $start,$pagesize";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k => $v)
            {
                $pro_id = $v['id'];
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id);
                $r2 = ConfigureModel::where(['recycle'=>0,'pid'=>$pro_id])->field('min(price) as price')->select()->toArray();
                if($r2)
                {
                    $v['price'] = $r2[0]['price'];
                    $v['vipPrice'] = round(($r2[0]['price'] * $member_discount * 0.1),2);
                }
                $list[] = $v;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 会员协议
    public function memberAgreement()
    {
        $store_id = Request::param('store_id');
        $list = array();
        $r = AgreementModel::where(['store_id'=>$store_id,'type'=>3])->limit(1)->select()->toArray();
        if($r)
        {
            $list = $r[0];
        }

        $data = array('memberAgreement'=>$list);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 开通会员页面
    public function settlement()
    {   
        $access_id = Request::param('access_id'); // 授权ID
        $store_id = Request::param('store_id');
        $coupon_id = Request::param('couponId'); // 优惠券ID
        $amount = Request::param('amount'); // 金额
        if($access_id != '')
        {
            $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('id,user_id,user_name,headimgurl,grade,grade_end,grade_m,money,password')->select()->toArray();
            if($r0)
            {
                $user_id = $r0[0]['user_id'];
                $user_money = $r0[0]['money'];
            }
            else
            {
                ob_clean();
                $message =  Lang('Illegal invasion');
                return output(400,$message);
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }
        
        // 支付方式
        $payment = Tools::getPayment($store_id);
        // 用户是否设置了支付密码
        $password_status = 0;
        if($r0[0]['password'])
        {
            $password_status = 1;
        } 

        $agreement = '';
        $r_agreement = AgreementModel::where(['store_id'=>$store_id,'type'=>3])->limit(1)->select()->toArray();
        if($r_agreement)
        {
            $agreement = $r_agreement[0]['content'];
        }

        $total = 0; // 实际支付金额

        $memberConfig = array();
        $r_config = MemberConfigModel::where('store_id',$store_id)->select()->toArray();
        if($r_config)
        {
            $r_config[0]['open_config'] = json_decode($r_config[0]['open_config'],true);
            foreach ($r_config[0]['open_config'] as $key => $value) 
            {
                if($value['openMethod'] == '月卡')
                {
                    $r_config[0]['open_config'][$key]['day'] = 30;
                    $r_config[0]['open_config'][$key]['priceForDay'] = round($value['price']/30,2);
                }
                elseif($value['openMethod'] == '季卡')
                {
                    $r_config[0]['open_config'][$key]['day'] = 90;
                    $r_config[0]['open_config'][$key]['priceForDay'] = round($value['price']/90,2);
                }
                elseif($value['openMethod'] == '年卡')
                {
                    $r_config[0]['open_config'][$key]['day'] = 365;
                    $r_config[0]['open_config'][$key]['priceForDay'] = round($value['price']/365,2);
                }

            }
            $r_config[0]['member_equity'] = json_decode($r_config[0]['member_equity'],true);
            $r_config[0]['bonus_points_config'] = json_decode($r_config[0]['bonus_points_config'],true);
            $memberConfig = $r_config[0];
            if(empty($amount))
            {
                $amount = $r_config[0]['open_config'][0]['price'];
            }
            
        }
        else
        {
            $message = Lang("member.8");
            return output(109,$message);
        }

        $total = $amount; // 实际支付金额

        $couponList = array();
        $now = date("Y-m-d H:i:s");
        //优惠券
        $sql0 = "select a.id,b.name,b.activity_type,b.z_money,b.discount,b.money,b.grade_id,a.hid,b.mch_id,a.store_id,a.type,a.user_id,b.type as couponType from lkt_coupon a LEFT JOIN lkt_coupon_activity b on a.hid = b.id where a.type = 0 and a.status = 0 and a.recycle = 0 and b.type = 4 and b.recycle = 0 and b.status = 1 and b.activity_type > 1 and b.z_money <= '$amount' and a.user_id = '$user_id'";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $activity_type = $v0['activity_type'];
                $z_money = $v0['z_money'];
                $j_money =  $v0['money'];
                $discount = $v0['discount'];

                if($activity_type == 2)
                {
                    $v0['total'] = round(($total - $j_money),2);
                }
                if($activity_type == 3)
                {
                    $v0['total'] = round(($total * $discount * 0.1),2);
                }

                if($total >= $z_money && $coupon_id == $v0['id'])
                {
                    $total = $v0['total'];
                }
                $couponList[] = $v0;
            }
        }

        $data = array('user_money'=>$user_money,'payment'=>$payment,'passwordStatus'=>$password_status,'memberAgreement'=>$agreement,'memberConfig'=>$memberConfig,'amount'=>$total,'couponList'=>$couponList);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 开通会员
    public function payment()
    {   
        $access_id = Request::param('access_id'); // 授权ID
        $store_id = Request::param('store_id');
        $store_type = Request::param('store_type');
        $coupon_id = Request::param('couponId'); // 优惠券ID
        $amount = Request::param('amount'); // 支付金额
        $memberType = Request::param('memberType'); // 1-月卡 2-季卡 3-年卡
        $payType = Request::param('payType'); // 支付方式
        if($access_id != '')
        {
            $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('id,user_id,user_name,headimgurl,grade,grade_end,grade_m,money')->select()->toArray();
            if($r0)
            {
                $user_id = $r0[0]['user_id'];
                $user_money = $r0[0]['money'];
            }
            else
            {
                ob_clean();
                $message =  Lang('Illegal invasion');
                return output(400,$message);
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }

        $now = date("Y-m-d H:i:s");
        Db::startTrans();

        $r_0 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('grade_end,grade,grade_add,money,password')->select()->toArray();
        if ($r_0)
        {
            $grade_end = $r_0[0]['grade_end'] ?:'';
            $is_grade = $r_0[0]['grade'];
            $s_time = $r_0[0]['grade_add'];
        }

        $total = 0;
        $openMethod = '';
        $points = 0;
        $r_config = MemberConfigModel::where('store_id',$store_id)->field('open_config,bonus_points_open,bonus_points_config')->select()->toArray();
        if($r_config)
        {
            $open_config = json_decode($r_config[0]['open_config'],true);
            $bonus_points_open = $r_config[0]['bonus_points_open'];
            $bonus_points_config = json_decode($r_config[0]['bonus_points_config'],true);
           
            foreach($open_config as $k_0 => $v_0)
            {
                if($memberType == ($k_0+1))
                {
                    $total = $v_0['price'];
                    $openMethod = $v_0['openMethod'];
                }
            }
            foreach($bonus_points_config as $k_1 => $v_1)
            {
                if($openMethod == $v_1['openMethod'])
                {
                    $points = $v_1['points'];
                }
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请设置会员制设置！';
            $this->Log($Log_content);
            $message = Lang("member.8");
            return output(109,$message);
        }

        // 查询优惠券信息
        if($coupon_id)
        {
            $sql_1 = "select a.id,b.name,b.activity_type,b.z_money,b.discount,b.money from lkt_coupon a LEFT JOIN lkt_coupon_activity b on a.hid = b.id where a.type = 0 and a.status = 0 and a.recycle = 0 and b.type = 4 and b.recycle = 0 and b.status = 1 and b.activity_type > 1 and a.id = $coupon_id";
            $r_1 = Db::query($sql_1);
            if($r_1)
            {
                $activity_type = $r_1[0]['activity_type'];
                $z_money = $r_1[0]['z_money'];
                $j_money =  $r_1[0]['money'];
                $discount = $r_1[0]['discount'];
                if($total < $z_money)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 该优惠券不满足使用条件！参数：优惠券' . $coupon_id . ';订单金额' . $total;
                    $this->Log($Log_content);
                    $message = Lang("member.9");
                    return output(109,$message);
                }
                if($activity_type == 2)
                {
                    //满减金额大于支付金额时付款金额为0
                    if($j_money >= $total)
                    {
                        $total = 0;
                    }
                    else
                    {
                        $total = round(($total - $j_money),2);
                    }
                }
                if($activity_type == 3)
                {
                    $total = round(($total * $discount * 0.1),2);
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改优惠券状态失败！参数：' . $sql_1;
                $this->Log($Log_content);
                $message = Lang("Busy network");
                return output(109,$message);
            }
        }

        //金额有误提示
        if($total < 0 || $amount < 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 订单金额低于0！';
            $this->Log($Log_content);
            $message = Lang("member.10");
            return output(109,$message,array('total'=>$total,'amount'=>$amount));
        }

        //生成订单号
        $sNo = 'DJ' . date("ymdhis") . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9);

        if($is_grade == 0)
        { // 不是会员
            switch ($memberType)
            {
                case 1:
                    $datetime = date("Y-m-d H:i:s", strtotime("+1 months"));
                    break;
                case 2:
                    $datetime = date("Y-m-d H:i:s", strtotime("+3 months"));
                    break;
                case 3:
                    $datetime = date("Y-m-d H:i:s", strtotime("+1 years"));
                    break;
            }
            $s_time = $now;
        }
        else
        {
            if($grade_end < $now)
            {
                $grade_end = $now;
                $s_time = $now;
            }
            else
            {
                $s_time = $grade_end;
            }
            
            switch ($memberType)
            {
                case 1:
                    $datetime = date("Y-m-d H:i:s", strtotime("$grade_end +1 months"));
                    break;
                case 2:
                    $datetime = date("Y-m-d H:i:s", strtotime("$grade_end +3 months"));
                    break;
                case 3:
                    $datetime = date("Y-m-d H:i:s", strtotime("$grade_end +1 years"));
                    break;
            }
        }
        if($payType == 'wallet_pay')
        {
            if ($user_money < $total)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 余额不足！';
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang("Busy network");
                return output(109,$message);
            }
        }

        $info = array('amount'=>$total,'couponId'=>$coupon_id,'endTime'=>$datetime,'memberType'=>$memberType,'payType'=>$payType,'startTime'=>$s_time,'storeId'=>$store_id,'userId'=>$user_id,'source'=>$store_type);
        $sql = array('trade_no'=>$sNo,'order_type'=>'DJ','addtime'=>$now,'pay_type'=>$payType,'pay_type'=>$payType,'data'=>json_encode($info));
        $r = Db::name('order_data')->insertGetId($sql);
        if($r > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加订单成功！';
            $this->Log($Log_content);
            Db::commit();
            $data = array('amount'=>$total,'endTime'=>$datetime,'memberType'=>$memberType,'memberTypeDesc'=>$openMethod,'sNo'=>$sNo,'orderTime'=>date('Y-m-d H:i:s'));
            $message = Lang("Success");
            return output(200,$message,$data);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加订单失败！参数：' . json_encode($sql);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang("Busy network");
            return output(109,$message);
        }
    }

    // 购买记录
    public function getBuyRecord()
    {
        $store_id = Request::param('store_id');
        $store_type = Request::param('store_type');
        $access_id = Request::param('access_id'); // 授权ID
        $page = Request::param('page');
        $pagesize = Request::param('pagesize');
        if($access_id != '')
        {
            $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('id,user_id,user_name,headimgurl,grade,grade_end,grade_m,money')->select()->toArray();
            if($r0)
            {
                $user_id = $r0[0]['user_id'];
                $user_money = $r0[0]['money'];
            }
            else
            {
                ob_clean();
                $message =  Lang('Illegal invasion');
                return output(400,$message);
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }

        $headImgUrl = '';
        $zhanghao = '';
        $phone = '';
        //支付方式
        $payments = PaymentModel::order('sort','desc')->select()->toArray();
        $payments_type = array();
        foreach ($payments as $keyp => $valuep)
        {
            $payments_type[$valuep['class_name']] = $valuep['name'];
        }

        $r_0 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('headImgUrl,zhanghao,mobile as phone')->select()->toArray();
        if ($r_0)
        {
            $headImgUrl = $r_0[0]['headImgUrl'];
            $zhanghao = $r_0[0]['zhanghao'];
            $phone = $r_0[0]['phone'];
        }
        
        $r_config = MemberConfigModel::where('store_id',$store_id)->field('open_config,bonus_points_open,bonus_points_config')->select()->toArray();
        if($r_config)
        {
            $open_config = json_decode($r_config[0]['open_config'],true);
        }

        $total = 0;
        $list = array();
        $total = OrderDataModel::where(['order_type'=>'DJ','status'=>1])
                            ->where('data','like','%'.$user_id.'%')
                            ->where('data','like','%'.$store_id.'%')
                            ->count();

        $r1 = OrderDataModel::where(['order_type'=>'DJ','status'=>1])
                            ->where('data','like','%'.$user_id.'%')
                            ->where('data','like','%'.$store_id.'%')
                            ->select()
                            ->toArray();
        if($r1)
        {
            foreach($r1 as $k => $v)
            {   
                $info = json_decode($v['data'],true);
                $v['amount'] = $info['amount'];
                $v['couponId'] = $info['couponId'];
                $v['endTime'] = $info['endTime'];
                $v['userId'] = $info['userId'];
                $v['memberType'] = $info['memberType'];
                $v['payType'] = $info['payType'];
                $v['startTime'] = $info['startTime'];
                $v['storeId'] = $info['storeId'];
                if($v['memberType'] == 1)
                {
                    $v['memberTypeDesc'] = '月卡';
                } 
                else if($v['memberType'] == 2)
                {
                    $v['memberTypeDesc'] = '季卡';
                }
                else if($v['memberType'] == 3)
                {
                    $v['memberTypeDesc'] = '年卡';
                }
                $pay = $v['payType'];
                if (array_key_exists($pay, $payments_type))
                {
                    $v['payTypeDesc'] = $payments_type[$pay];
                }
                else
                {
                    $v['payTypeDesc'] = '钱包支付';
                }
                $v['headImgUrl'] = $headImgUrl;
                $v['zhanghao'] = $zhanghao;
                $v['phone'] = $phone;
                $list[] = $v;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 关闭自动续费弹框
    public function closeFrame()
    {
        $store_id = Request::param('store_id');
        $store_type = Request::param('store_type');
        $access_id = Request::param('access_id'); // 授权ID
        $page = Request::param('page');
        $pagesize = Request::param('pagesize');
        if($access_id != '')
        {
            $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('id,user_id,user_name,headimgurl,grade,grade_end,grade_m,money')->select()->toArray();
            if($r0)
            {
                $user_id = $r0[0]['user_id'];
                $user_money = $r0[0]['money'];
            }
            else
            {
                ob_clean();
                $message =  Lang('Illegal invasion');
                return output(400,$message);
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }

        $sql_where = array('store_id'=>$store_id,'user_id'=>$user_id);
        $sql_update = array('is_box'=>0);
        $r = Db::name('user')->where($sql_where)->update($sql_update);
        if($r > 0)
        {
            $message = Lang("Success");
            return output(200,$message);
        }
        else
        {
            $message = Lang("member.1");
            return output(200,$message);
        } 
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("plugin/member.log",$Log_content);
        return;
    }
}

