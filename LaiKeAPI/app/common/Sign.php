<?php
namespace app\common;

use app\admin\model\SignConfigModel;
use app\admin\model\SignModel;
use app\admin\model\SignRecordModel;

class Sign 
{
    // 获取插件状态
    public function is_Plugin($store_id)
    {
        $r0 = SignConfigModel::where('store_id', $store_id)
                            ->field('is_status')
                            ->select()
                            ->toArray();
        if ($r0)
        {
            $is_display = $r0[0]['is_status'];
        }
        else
        {
            $is_display = 2;
        }
        return $is_display;
    }

    // 添加插件设置
    public function add($store_id)
    {
        $data = ['store_id' => $store_id];
        Db::name('lkt_sign_config')->save($data);
        return;
    }

    // 删除插件设置
    public function del($store_id)
    {
        Db::table('lkt_sign_config')->where('store_id',$store_id)->delete();
        return;
    }

    // 前端首页
    public function test($store_id, $user_id)
    {
        $start_1 = date("Y-m-d 00:00:00"); // 今天开始时间
        $time = date("Y-m-d H:i:s"); // 当前时间
        // 查询签到活动
        $r0 = SignConfigModel::where('store_id', $store_id)
                            ->select()
                            ->toArray();
        if ($r0)
        {
            $is_status = $r0[0]['is_status']; // 签到插件是否启用
            $is_remind = $r0[0]['is_remind']; // 是否提醒
            $starttime = $r0[0]['starttime']; // 签到有效开始时间
            $endtime = $r0[0]['endtime']; // 签到结束时间
            $is_many_time = $r0[0]['is_many_time']; // 是否允许多次
            $reset = $r0[0]['reset']; // 重置时间
            $score_num = $r0[0]['score_num']; // 签到次数
            if ($is_status == 0)
            { // 签到插件没开启
                $sign_status = 0; // 不用弹出签名框
            }
            else
            {
                if ($endtime <= $time || $starttime >= $time)
                {
                    // 今天开始时间大于签到结束时间 或者 今天开始时间小于签到开始时间 签到还没进行
                    $r1 = SignConfigModel::where('store_id',$store_id)->find();
                    $r1->is_status = 0;
                    $r1->save();
                    $sign_status = 0; // 不用弹出签名框
                    $is_status = 0;
                }
                else
                {
                    // 今天开始时间大于签到结束时间 或者 今天开始时间小于签到开始时间 签到还没进行
                    $r1 = SignConfigModel::where('store_id',$store_id)->find();
                    $r1->is_status = 1;
                    $r1->save();
                    // 签到进行中
                    if ($is_remind == 0)
                    { // 不用提醒签到
                        $sign_status = 0; // 不用弹出签名框
                    }
                    else
                    {
                        if ($user_id == '')
                        {
                            $sign_status = 0; // 没签
                        }
                        else
                        {
                            
                            $r2 = SignRecordModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'type'=>0])
                                                ->where('sign_time','>',$start_1)
                                                ->order('sign_time','desc')
                                                ->select()
                                                ->toArray();
                            if ($r2)
                            {
                                // 有数据
                                if ($is_many_time == 0)
                                { // 不允许多次
                                    $sign_status = 0; // 签过了
                                }
                                else
                                {
                                    if (count($r2) < $score_num)
                                    {
                                        // 没签够
                                        $sign_time = $r2[0]['sign_time'];
                                        if (strtotime($time) - strtotime($sign_time) >= $reset)
                                        {
                                            $sign_status = 1; // 没签
                                        }
                                        else
                                        {
                                            $sign_status = 0; // 签过了
                                        }
                                    }
                                    else
                                    {
                                        $sign_status = 0; // 签过了
                                    }
                                }
                            }
                            else
                            {
                                $sign_status = 1; // 没签
                            }
                        }
                    }
                }
            }
            $is_sign_status = $is_status;
        }
        else
        {
            $sign_status = 0;
            $is_sign_status = 0;
        }
        $data = array('is_sign_status' => $is_sign_status, 'sign_status' => $sign_status);
        return $data;
    }

    /**
     * 定时任务
     * @param $params
     * @return mixed|void
     */
    public function dotask($params)
    {
        $store_id = $params->store_id;
        $time = date("Y-m-d H:i:s");
        if(date('Y-m-d 00:00:00') <= $time && $time < date('Y-m-d 00:00:05'))
        {
            $r_user = Db::name('_user')->where(['store_id'=>$store_id])->update(['popup'=>0]);
        }
        // 查询签到参数
        $r = SignConfigModel::where(['store_id'=>$store_id])
                            ->select()
                            ->toArray();
        if ($r)
        {
            $endtime = $r[0]['endtime']; // 结束时间
            if ($time >= $endtime)
            {
                Db::name('_sign_config')->where(['store_id'=>$store_id])->update(['is_status'=>0]);
            }
        }
    }
}
