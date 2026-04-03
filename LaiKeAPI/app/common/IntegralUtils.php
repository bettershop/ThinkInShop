<?php

namespace app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;

use app\admin\model\OrderModel;
use app\admin\model\UserRuleModel;
use app\admin\model\UserModel;

class IntegralUtils
{
    public $store_id = null; //公共属性,商户id
    public $db = null; //公共属性,db实例
    public $user_id = null; //公共属性,用户id

    /**
     * 积分方法封装
     * @param   $store_id string 商户号
     * @param   $sNo string 订单号
     * @param   $user_id string 用户id
     */
    public function issue($store_id, $sNo, $user_id)
    {
        $this->store_id = $store_id;
        $this->user_id = $user_id;
        $time = date("Y-m-d H:i:s");
        Db::startTrans();
        //获取订单总价
        $res_o = OrderModel::where(['sNo'=>$sNo])
                            ->field('z_price')
                            ->select()
                            ->toArray();
        if($res_o)
        {
            $z_price = $res_o[0]['z_price'];
        }
        else
        {
            $z_price = 0;
        }
        //积分获取配置
        $r_con = UserRuleModel::where(['store_id'=>$store_id])
                                ->select()
                                ->toArray();
        if ($r_con)
        {
            $is_birthday = $r_con[0]['is_birthday'];
            $bir_multiple = $r_con[0]['bir_multiple'];
            $bonus_points_shopping = $r_con[0]['bonus_points_shopping'];
            $proportion_of_gifts = $r_con[0]['proportion_of_gifts'];
            $release_time = $r_con[0]['release_time'];
            $jifen_m = $r_con[0]['jifen_m'];
            $is_jifen = $r_con[0]['is_jifen'];//会员等比例积分 0-不开启 1-开启
        }
        else
        {
            $is_birthday = 0;
            $bir_multiple = 0;
            $bonus_points_shopping = 0;
            $proportion_of_gifts = '';
            $release_time = 2;
            $jifen_m = 0;
            $is_jifen = 0;
        }
        //查询会员身份
        $res_l = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                            ->field('grade')
                            ->select()
                            ->toArray();
        $grade = $res_l[0]['grade'];
        if ($grade > 0)
        {
            //购物赠送积分切且 积分发放在收货时
            if ($bonus_points_shopping == 1 )
            {
                if ($is_birthday == 1)
                {
                    $time = date("m-d");
                    $r_user = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                                        ->field('birthday')
                                        ->select()
                                        ->toArray();
                    $birthday = $r_user[0]['birthday'];
                    $riqi = date("m-d", strtotime($birthday));
                    if ($riqi == $time)
                    {
                        $scor = floor($z_price * $bir_multiple);
                    }
                    else
                    {
                        if($is_jifen == 1 && $jifen_m == 1)
                        {
                            $scor = floor($z_price);
                        }
                        else
                        {
                            $scor = floor($z_price * $proportion_of_gifts / 100);
                        }
                    }
                }
                else
                {
                    if($is_jifen == 1 && $jifen_m == 1)
                    {
                        $scor = floor($z_price);
                    }
                    else
                    {
                        $scor = floor($z_price * $proportion_of_gifts / 100);
                    }
                }

                $sql_score_where = array('store_id'=>$store_id,'user_id'=>$user_id);
                $sql_score_update = array('score'=>Db::raw('score+'.$scor));
                $r_score = Db::name('_user')->where($sql_score_where)->update($sql_score_update);
                if ($r_score == -1)
                {
                    //回滚删除已经创建的订单
                    Db::rollback();
                    $this->Log(__METHOD__ . ":" . __LINE__ . "修改用户积分失败！参数:" .json_encode($sql_score_where).'=>'.json_encode($sql_score_update));
                    return 0;
                }

                $event = $user_id . '购物获得' . $scor . '积分';

                $sqll = array('store_id'=>$store_id,'user_id'=>$user_id,'sign_score'=>$scor,'record'=>$event,'sign_time'=>$time,'type'=>8,'recovery'=>0,'sNo'=>$sNo);
                $rr = Db::name('_sign_record')->insert($sql2);
                if ($rr < 1)
                {
                    Db::rollback();
                    $this->Log(__METHOD__ . ":" . __LINE__ . "添加用户积分记录失败！参数:" . json_encode($sqll));
                    return 0;
                }
            }
        }
        else
        {
            if ($bonus_points_shopping == 1 && $release_time == 1)
            {

                $scor = floor($z_price * $proportion_of_gifts / 100);
                $sql_score = "update lkt_user set score = score+'$scor' where store_id = '$store_id' and user_id = '$user_id' ";
                $r_score = $db->update($sql_score);
                if ($r_score == -1)
                {
                    //回滚删除已经创建的订单
                    $db->rollback();
                    $lktlog->customerLog(__METHOD__ . ":" . __LINE__ . "修改用户积分失败！sql:" . $sql);
                    return 0;
                }

                $event = $user_id . '购物获得' . $scor . '积分';
                $sqll = "insert into lkt_sign_record (store_id,user_id,sign_score,record,sign_time,type,recovery,sNo) values ('$store_id','$user_id','$scor','$event',CURRENT_TIMESTAMP,8,0,'$sNo')";
                $rr = $db->insert($sqll);
                if ($rr < 1)
                {
                    $db->rollback();
                    $lktlog->customerLog(__METHOD__ . ":" . __LINE__ . "添加用户积分记录失败！sql:" . $sql);
                    return 0;
                }
            }
        }
        Db::commit();
        return 1;
    }

    // 日志
    public function Log($Log_content)
    {
        $time = date("Y-m-d");
        $lktlog = new LaiKeLogUtils();

        $lktlog->log("app/integral/".$time.".log",$Log_content);
        return;
    }
}
