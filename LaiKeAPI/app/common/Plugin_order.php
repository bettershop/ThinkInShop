<?php
namespace app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;

use app\admin\model\UserRuleModel;
use app\admin\model\UserModel;
use app\admin\model\UserGradeModel;

/**
 * LKT 插件订单处理类
 * 方法一：JP_setment 竞拍订单确认
 * 方法二：JP_payment 竞拍订单生成
 *
 */
class Plugin_order
{
    public function __construct($store_id)
    {
        $this->store_id = $store_id;
    }

    /**
     * 会员特惠折扣计算
     * $type    订单类型
     * $product_total 原商品价
     * $store_id 商城id
     */
    public function user_grade($type, $product_total, $user_id, $store_id)
    {

        //会员特惠支持商品标识
        $flag = '';
        $log = new LaiKeLogUtils();
        //商品
        if ($type == 'GM')
        {
            //普通
            $flag = 1;
        }
        else if ($type == 'PT')
        {
            //拼团
            $flag = 2;
        }
        else if ($type == 'KJ')
        {
            //砍价
            $flag = 3;
        }
        else if ($type == 'JP')
        {
            //竞拍
            $flag = 4;
        }
        else if ($type == 'FX')
        {
            //分销
            $flag = 5;
        }
        else if ($type == 'MS')
        {
            //秒杀
            $flag = 6;
        }
        else if ($type == 'TH')
        {
            //会员特惠
            $flag = 7;
        }

        $active = array();
        //插叙可支持的特惠商品或活动
        $res = UserRuleModel::where('store_id',$store_id)->field('active')->select()->toArray();
        if ($res)
        {
            $active = explode(',', $res[0]['active']); //可支持活动数组
        }

        $can = false; //能否支持会员特惠
        if ($flag)
        {
            if (in_array($flag, $active))
            {
                $can = true;
            }
            else
            {
                $can = false;
            }

        }
        else
        {
            $can = false;
        }

        $now = date("Y-m-d H:i:s");
        if ($can == true)
        {
            //可以使用会员特惠
            $res_0 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                              ->where('grade_end','>',$now)
                              ->field('grade')
                              ->select()
                              ->toArray();
            if ($res_0)
            {
                $grade = $res_0[0]['grade'];
                $res_1 = UserGradeModel::where(['store_id'=>$store_id,'id'=>$grade])->field('rate')->select()->toArray();
                if ($res_1)
                {
                    $rate = floatval($res_1[0]['rate']);
                }
                else
                {
                    $log->log('app/recharge.log','查询会员级别失败,sql为：' . $sql_1 . "\r\n");
                    $rate = 10;
                }
            }
            else
            {
                $log->log('app/recharge.log','查询用户的会员等级失败,sql为：' . $sql_0 . "\r\n");
                $rate = 10; //折扣
            }
            $total = $product_total * $rate;

        }
        else
        {
            //不可以使用会员特惠
            $rate = 10;
            $total = $product_total;
        }

        $arr = array();
        $arr['total'] = $total;
        $arr['rate'] = $rate / 10;
        //放回打折后商品总价
        return $arr;

    }

   

}
