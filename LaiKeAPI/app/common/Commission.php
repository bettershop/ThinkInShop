<?php

namespace app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\GETUI\LaikePushTools;
use app\common\Jurisdiction;

use app\admin\model\DistributionConfigModel;
use app\admin\model\UserDistributionModel;
use app\admin\model\OrderModel;
use app\admin\model\DistributionGradeModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\UserModel;
use app\admin\model\DistributionRecordModel;

class Commission
{
    //用户升级 $up_0 -> 商品绑定等级  $up_1 -> 会员id
    public function uplevel($store_id, $up_1, $up_0 = 0)
    {
        $this->store_id = $store_id;
        Db::startTrans();

        // 查询配置参数
        $r_1 = DistributionConfigModel::where(['store_id'=>$store_id])
                                    ->field('sets')
                                    ->select()
                                    ->toArray();
        $c_pay = 2;
        $c_cengji = 0;
        if ($r_1) 
        {
            $sets = json_decode($r_1[0]['sets'],true);
            if (!is_array($sets)) 
            {   
                Db::commit();
                return;
            }
            if (array_key_exists('c_pay', $sets)) 
            {
                $c_pay = $sets['c_pay']; //规则统计方式  1.付款后 2.收货后
            }
            $c_cengji = array_key_exists('c_cengji', $sets) ? $sets['c_cengji'] : 0;
        }

        //统一升级
        $up_3 = $this->getlevels(); //获取所有分销等级
        if (!empty($up_3) && count($up_3) > 0) 
        {
            //有分销等级时
            $up_4 = $up_1; //会员id
            //循环升级
            $up_res_2 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$up_4])
                                            ->limit(0,1)
                                            ->select()
                                            ->toArray();
            if (!$up_res_2) {return;}

            $up_level = $this->get_nextlevel($up_4); // 获取可升级分销等级

            
            // 如果用户为分销商，并且还能继续升级
            if ($up_res_2 && $up_level) 
            {
                $uplevel = 0;
                foreach ($up_level as $k => $v) {
                    $up_5 = (array)$v;
                    $up_6 = $up_3[$up_5['id']]; //下一等级配置信息
                    $up_7 = count($up_6); //下一等级升级条件数
                    $ok   = 0; //升级条件满足数

                    //一次性消费
                    if (array_key_exists("onebuy", $up_6)) 
                    {
                        $sql_Order = new OrderModel();
                        $r_condition0 = $sql_Order->where(['store_id'=>$store_id,'user_id'=>$up_4,'otype'=>'FX']);
                        if ($c_pay == 1) 
                        {
                            $r_condition0->whereIn('status','1,2,5');
                        }
                        else
                        {
                            $r_condition0->whereIn('status','5');
                        }
                        $r = $r_condition0->limit(0,1)->field('max(z_price) as z_price')->select()->toArray();
                        $sum = $r ? floatval($r[0]['z_price']) : 0;
                        if ($sum >= floatval($up_6['onebuy'])) 
                        {
                            $ok++;
                        }
                    }

                    //推荐指定等级会员
                    if (array_key_exists("recomm", $up_6)) 
                    {
                        $up_14 = explode(',', $up_6['recomm']);
                        if (array_key_exists('1', $up_14)) 
                        {
                            $r = DistributionGradeModel::where(['store_id'=>$store_id])
                                                        ->where('id','>',$up_14[1])
                                                        ->field('id')
                                                        ->select()
                                                        ->toArray();
                            if ($r) {
                                $levels = '';
                                foreach ($r as $k => $v) 
                                {
                                    $levels .= $v['id'] . ',';
                                }
                                $levels = substr($levels, 0, strlen($levels) - 1);
                                if (!empty($levels)) 
                                {
                                    $r = UserDistributionModel::where(['store_id'=>$store_id,'pid'=>$up_4])
                                                                ->whereIn('level',$levels)
                                                                ->field('count(id) as count')
                                                                ->select()
                                                                ->toArray();
                                    if (intval($r[0]['count']) >= intval($up_14[0])) 
                                    {
                                        $ok++;
                                    }
                                }
                            }
                        }
                    }

                    //累计消费升级
                    if (array_key_exists("manybuy", $up_6)) 
                    {
                        $up_8 = floatval($up_6['manybuy']);
                        if (floatval($up_res_2[0]['onlyamount']) >= $up_8) 
                        {
                            $ok++;
                        }
                    }

                    //累计业绩升级
                    if (array_key_exists("manyyeji", $up_6)) {
                        $up_8 = $up_6['manyyeji'];
                        if (floatval($up_res_2[0]['allamount']) >= $up_8) 
                        {
                            $ok++;
                        }
                    }

                    //团队人数升级
                    if (array_key_exists("manypeople", $up_6)) {
                        $up_8 = explode(',', $up_6['manypeople']);
                        //直推人数
                        $up_res_3 = UserDistributionModel::where(['store_id'=>$store_id,'pid'=>$up_4])
                                                        ->where('level','>','0')
                                                        ->field('count(id) as count')
                                                        ->select()
                                                        ->toArray();
                        //团队人数
                        $up_res_4 = UserDistributionModel::where(['store_id'=>$store_id])
                                                        ->where('level','>','0')
                                                        ->where('lt','>=', $up_res_2[0]['lt'])
                                                        ->where('rt','<=',$up_res_2[0]['rt'])
                                                        ->field('count(id) as count')
                                                        ->select()
                                                        ->toArray();
                        if (intval($up_res_3[0]['count']) >= intval($up_8[0]) && intval($up_res_4[0]['count']) >= intval($up_8[1])) 
                        {
                            $ok++;
                        }
                    }

                    if ($up_7 > 0) 
                    {
                        //如果升级条件数大于0
                        if ($sets['c_uplevel'] == 1 && $ok > 0) 
                        { //分销等级晋升设置满足任意一项
                            $uplevel = $up_5['id'];
                        } 
                        else if ($sets['c_uplevel'] == 2 && $ok == $up_7) 
                        { //分销等级晋升设置满足所有选项
                            $uplevel = $up_5['id'];
                        }
                    }
                }

                //升级
                if ($uplevel > 0) {
                    // 修改用户分销等级
                    $dis_sql_9 = array('store_id'=>$store_id,'user_id'=>$up_4);
                    $beres = Db::name('user_distribution')->where($dis_sql_9)->update(['level'=>$uplevel]);
                    if ($beres < 1) 
                    {
                        Db::rollback();
                        $this->Log(__LINE__ . ":分销统一升级失败！参数：".json_encode($dis_sql_9));
                        echo json_encode(array('status' => 0, 'err' => '参数错误 code:c3', 'sql' => $dis_sql_9));
                        exit;
                    } 
                    else 
                    {   
                        $sql_update = array('user_id'=>$up_4,'type'=>1,'old_level'=>$up_res_2[0]['level'],'up_level'=>$uplevel);
                        $res_update = Db::name('level_update')->insert($sql_update);

                        $this->Log(__LINE__ . ":修改用户分销等级成功！参数：".json_encode($sql_update));

                        $msg_title = Lang("distribution.25");
                        $msg_content = Lang("distribution.26");
                        $pusher = new LaikePushTools();
                        $pusher->pushMessage($up_4, $msg_title, $msg_content, $store_id, '');
                    }

                    $this->Log(__LINE__ . ":会员【" . $up_4 . "】分销统一升级到等级[" . $uplevel . "]成功！");

                    $up_12 = $this->getsets($uplevel);
                } 
                else 
                {   
                    Db::commit();
                    return;
                } 
            } 
            else 
            {   
                Db::commit();
                return;
            }
        }
        //统一升级 end
        //事务提交
        Db::commit();
        return;
    }

    //获取所有分销等级
    public function getlevels()
    {
        $store_id = $this->store_id;

        $res_0 = DistributionGradeModel::where(['store_id'=>$store_id])
                                        ->order('sort','asc')
                                        ->select()
                                        ->toArray();
        $res_1 = array();
        if ($res_0) 
        {
            foreach ($res_0 as $k => $v) 
            {
                $levelobj = json_decode($v['uplevel_obj'],true);
                $id = $v['id'];
                $res_1[$id] = $levelobj;
            }
        }
        return $res_1;
    }

    //获取可升级分销等级 $nl_0 -> 会员id
    public function get_nextlevel($nl_0)
    {
        $store_id = $this->store_id;

        $nl_res_1 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$nl_0])
                                        ->field('level')
                                        ->select()
                                        ->toArray();

        $nl_1 = !empty($nl_res_1) ? intval($nl_res_1[0]['level']) : 0;
        if ($nl_1 > 0) 
        {
            $nl_res_3 = DistributionGradeModel::where(['store_id'=>$store_id])
                                                ->where('id','>',$nl_1)
                                                ->select()
                                                ->toArray();
        } 
        else 
        {
            $nl_res_3 = DistributionGradeModel::where(['store_id'=>$store_id])
                                                ->select()
                                                ->toArray();
        }
        
        if ($nl_res_3) 
        {
            $res = $nl_res_3;
        } 
        else 
        {
            $res = '';
        }

        return $res;
    }

    //获取等级配置 $set_0->获取等级id
    public function getsets($set_0)
    {
        $store_id = $this->store_id;
        $set_1 = array();
        if (intval($set_0) > 0) 
        {
            $set_res_0 = DistributionGradeModel::where(['store_id'=>$store_id,'id'=>$set_0])
                                        ->limit(0,1)
                                        ->select()
                                        ->toArray();
            if ($set_res_0) 
            {
                $set_1 = json_decode($set_res_0[0]['sets'],true);
                $set_1['integral'] = $set_res_0[0]['integral'];
            }
        }
        return $set_1;
    }

    //佣金发放 $put_0 -> sNo     $put_3 -> 推广业绩
    public function putcomm($store_id, $put_0, $put_3)
    {   
        Db::startTrans();
        $put_res_0 = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$put_0])
                                        ->limit(0,1)
                                        ->field('user_id')
                                        ->select()
                                        ->toArray();
        if (!$put_res_0) 
        {   
            Db::commit();
            return;
        }

        // 查询配置参数
        $r_1 = DistributionConfigModel::where(['store_id'=>$store_id])
                                    ->field('sets')
                                    ->select()
                                    ->toArray();
        $c_pay = 2;
        if ($r_1) 
        {
            $sets = json_decode($r_1[0]['sets'],true);
            $c_neigou = $sets['c_neigou']; // 是否内购 1.否 2.是
            if (array_key_exists('c_pay', $sets)) 
            {
                $c_pay = $sets['c_pay']; //规则统计方式  1.付款后 2.收货后
            }
        }

        $put_4 = $put_res_0[0]['user_id'];
        $put_res_1 = DistributionRecordModel::where(['store_id'=>$store_id,'sNo'=>$put_0,'status'=>0])
                                            ->select()
                                            ->toArray();
        if (count($put_res_1) > 0) 
        {
            foreach ($put_res_1 as $k => $v) 
            {
                $put_1 = $v['money'];

                //修改佣金表状态
                $put_sql_2 = array('store_id'=>$store_id,'id'=>$v['id']);
                $beres = Db::name('distribution_record')->where($put_sql_2)->update(['status'=>'1']);
                if ($beres < 1) 
                {
                    Db::rollback();
                    $this->Log(__LINE__ . ":佣金[" . $put_1 . "]发放【" . $put_4 . "】失败！参数: ".json_encode($put_sql_2));
                    echo json_encode(array('status' => 0, 'err' => '参数错误 code:c4', 'sql' => $put_sql_2));
                    exit;
                } 
                else 
                {
                    $this->Log(__LINE__ . ":佣金[" . $put_1 . "]发放【" . $put_4 . "】修改佣金表状态成功！");
                }

                //修改用户佣金
                $put_sql_3_where = array('store_id'=>$store_id,'user_id'=>$v['user_id']);
                $put_sql_3_update = array('commission'=>Db::raw('commission+'.$put_1),'accumulative'=>Db::raw('accumulative+'.$put_1));
                //佣金计算方式为付款时直接增加可提现佣金
                if($c_pay == 1)
                {
                    $put_sql_3_update = array('commission'=>Db::raw('commission+'.$put_1),'tx_commission'=>Db::raw('tx_commission+'.$put_1),'accumulative'=>Db::raw('accumulative+'.$put_1));
                }
                $beres = Db::name('user_distribution')->where($put_sql_3_where)->update($put_sql_3_update);
                if ($beres < 1) 
                {
                    Db::rollback();
                    $this->Log(__LINE__ . ":佣金[" . $put_1 . "]发放【" . $put_4 . "】失败！参数:".json_encode($put_sql_3_where).'=>'.json_encode($put_sql_3_update));
                    echo json_encode(array('status' => 0, 'err' => '参数错误 code:c5', 'sql' => $put_sql_3));
                    exit;
                } 
                else 
                {
                    $this->Log(__LINE__ . ":佣金[" . $put_1 . "]发放【" . $put_4 . "】修改用户佣金成功！");
                }

                //金额记录表
                $put_2 = $v['event'] . "[" . $v['sNo'] . "]";

                $put_sql_4 = array('store_id'=>$store_id,'user_id'=>$v['user_id'],'money'=>$put_1,'oldmoney'=>0,'event'=>$put_2,'type'=>7);
                $beres = Db::name('record')->insert($put_sql_4);
                if ($beres < 1) 
                {
                    Db::rollback();
                    $this->Log(__LINE__ . ":佣金[" . $put_1 . "]发放【" . $put_4 . "】失败！参数: ".json_encode($put_sql_4));
                    echo json_encode(array('status' => 0, 'err' => '参数错误 code:c6', 'sql' => $put_sql_4));
                    exit;
                } 
                else 
                {
                    $this->Log(__LINE__ . ":佣金[" . $put_1 . "]发放【" . $put_4 . "】金额记录添加成功！");
                }

                //发送消息给用户
                $msg_title = Lang("distribution.29");
                $msg_content = Lang("distribution.30");
                /**佣金到账通知*/
                $pusher = new LaikePushTools();
                $pusher->pushMessage($v['user_id'], $msg_title, $msg_content, $store_id);
            }
        }

        $r = OrderModel::where(['store_id'=>$store_id,'sNo'=>$put_0])
                        ->field('is_put')
                        ->select()
                        ->toArray();
        if ($r && $r[0]['is_put'] == 0) 
        {// 存在 并且 未发放佣金否 
            // 累计消费增加
            $put_sql_5_update = array('onlyamount'=>Db::raw('onlyamount+'.$put_3));
            if($c_neigou == 2)
            { // 开启内购
                // 业绩增加
                $put_sql_5_update = array('onlyamount'=>Db::raw('onlyamount+'.$put_3),'allamount'=>Db::raw('allamount+'.$put_3));
                
            }
            $put_sql_5_where = array('store_id'=>$store_id,'user_id'=>$put_4);
            $beres = Db::name('user_distribution')->where($put_sql_5_where)->update($put_sql_5_update);
            if ($beres < 0) 
            {
                Db::rollback();
                $this->Log(__LINE__ . ":订单[" . $put_0 . "]发放【" . $put_4 . "】失败！参数: ".json_encode($put_sql_5_where).'=>'.json_encode($put_sql_5_update));
                echo json_encode(array('status' => 0, 'err' => '参数错误 code:c7', 'sql' => $put_sql_5));
                exit;
            } 
            else 
            {
                $this->Log(__LINE__ . ":订单[" . $put_0 . "]发放【" . $put_4 . "】业绩增加成功！");
            }

            // 查询配置参数
            $c_cengji = 0;
            $r_1 = DistributionConfigModel::where(['store_id'=>$store_id])
                                            ->field('sets')
                                            ->select()
                                            ->toArray();
            if ($r_1) 
            {
                $sets = json_decode($r_1[0]['sets'],true);
                $c_cengji = array_key_exists('c_cengji', $sets) ? $sets['c_cengji'] : 0;
            }

            //上级业绩增加
            $put_5 = $put_4;
            $i = 0;
            while ($i < $c_cengji) 
            {
                $put_res_6 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$put_5])
                                                    ->limit(0,1)
                                                    ->field('pid')
                                                    ->select()
                                                    ->toArray();
                if ($put_res_6 && !empty($put_res_6[0]['pid'])) 
                {
                    $put_5 = $put_res_6[0]['pid'];

                    $put_sql_7_where = array('store_id'=>$store_id,'user_id'=>$put_5);
                    $put_sql_7_update = array('allamount'=>Db::raw('allamount+'.$put_3));
                    $beres = Db::name('user_distribution')->where($put_sql_7_where)->update($put_sql_7_update);
                    if ($beres < 0) 
                    {
                        Db::rollback();
                        $this->Log(__LINE__ . ":订单[" . $put_0 . "]发放【" . $put_4 . "】失败！参数: ".json_encode($put_sql_7_where).'=>'.json_encode($put_sql_7_update));
                        echo json_encode(array('status' => 0, 'err' => '参数错误 code:c8', 'sql' => $put_sql_7));
                        exit;
                    } 
                    else 
                    {
                        $this->Log(__LINE__ . ":订单[" . $put_0 . "]发放【" . $put_4 . "】上级业绩增加成功！");
                    }

                    $i++;
                } 
                else 
                {
                    break;
                }
            }

            // 修改发放状态
            $sql_where = array('store_id'=>$store_id,'sNo'=>$put_0);
            $sql_update = array('is_put'=>1);
            //佣金计算方式为付款时直接修改结算状态
            if($c_pay == 1)
            {
                $sql_update = array('is_put'=>1,'commission_type'=>1);
            }
            $r = Db::name('order')->where($sql_where)->update($sql_update);
            if ($r < 0) 
            {
                Db::rollback();
                $this->Log(__LINE__ . ":订单[" . $put_0 . "]发放【" . $put_4 . "】失败！参数: ".json_encode($sql_where).'=>'.json_encode($sql_update));
                echo json_encode(array('status' => 0, 'err' => '参数错误 code:c99', 'sql' => $sql));
                exit;
            }
            else 
            {
                $this->Log(__LINE__ . ":订单[" . $put_0 . "]发放【" . $put_4 . "】修改发放状态成功！");
            }
        }
        Db::commit();
        $this->uplevel($store_id, $put_4);

        return;
    }

    //礼包升级
    public function straight_up($store_id, $user_id, $up_level = 0)
    {   

        //获取用户信息
        $res_u = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                                        ->field('level')
                                        ->select()
                                        ->toArray();
        $old_level = (int)$res_u[0]['level'];
        $up_level = (int)$up_level;
        if($old_level < $up_level)
        {   
            Db::startTrans();

            $sql1_where = array('store_id'=>$store_id,'user_id'=>$user_id);
            $sql1_update = array('level'=>$up_level);
            $res = Db::name('user_distribution')->where($sql1_where)->update($sql1_update);
            if($res < 0)
            {   
                Db::rollback();
                $this->Log(__LINE__ . ":会员【" . $user_id . "】分销直升到等级[" . $up_level . "]失败！参数：".json_encode($sql_where).'=>'.json_encode($sql_update));
            }

            $sql2 = array('user_id'=>$user_id,'type'=>3,'old_level'=>$old_level,'up_level'=>$up_level);
            $res2 = Db::name('level_update')->insert($sql2);
            if($res2 < 1)
            {
                Db::rollback();
                $this->Log(__LINE__ . ":修改分销商等级失败！参数：".json_encode($sql2));
            }
            Db::commit();
        }
    }


    //创建分销等级和会员信息
    //$cl_0 -> 会员id  $cl_1 -> 会员等级  $cl_2 -> 推荐人id
    public function create_level($cl_0,$cl_1,$cl_2,$store_id,$operator = '')
    {
        $log = new LaiKeLogUtils(); // 日志
        $Jurisdiction = new Jurisdiction();
        $cl_res_0 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$cl_0])->field('id')->select()->toArray();
        if ($cl_res_0) 
        {
            $sql = "update lkt_user_distribution set level = '$cl_1' where store_id = '$store_id' and user_id = '$cl_0' ";
            $r = Db::execute($sql);
            return;
        }
        Db::startTrans();
        $cl_res_1 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$cl_2])->field('rt,level,uplevel')->select()->toArray();
        //分销推荐
        if ($cl_res_1) 
        {
            $cl_res_2 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$cl_0])->field('user_id')->select()->toArray();
            if (!$cl_res_2) 
            {
                $rt       = $cl_res_1[0]['rt'];
                $level    = $cl_1;
                $uplevel  = intval($cl_res_1[0]['uplevel']) + 1;
                $cl_sql_3 = "update lkt_user_distribution set lt = lt + 2 where store_id = '$store_id' and lt>='$rt'";
                $cl_sql_4 = "update lkt_user_distribution set rt = rt + 2 where store_id = '$store_id' and rt>='$rt'";
                $lrt      = $rt + 1;

                $cl_res_3 = Db::execute($cl_sql_3);
                $cl_res_4 = Db::execute($cl_sql_4);
                $cl_sql_5 = new UserDistributionModel();
                $cl_sql_5->store_id = $store_id;
                $cl_sql_5->user_id = $cl_0;
                $cl_sql_5->pid = $cl_2;
                $cl_sql_5->level = $level;
                $cl_sql_5->lt = $rt;
                $cl_sql_5->rt = $lrt;
                $cl_sql_5->uplevel = $uplevel;
                $cl_sql_5->add_date = date('Y-m-d H:i:s');
                $cl_sql_5->save();
                $cl_res_5 = $cl_sql_5->id;
                //事务
                if ($cl_res_5 <1 || $cl_res_3 <0  || $cl_res_4<0) 
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$cl_0.' 添加成为分销商失败',1,1,0);
                    $log->log('common/lib_commission.log',__LINE__ . ":创建分销等级[" . $level . "]和会员【" . $cl_0 . "】信息失败: $cl_res_5 \r\n $cl_res_4 \r\n $cl_res_3 \r\n");

                    echo json_encode(array('status' => 0, 'err' => '参数错误 code:30', 'sql' => $cl_sql_3 . $cl_sql_4 . $cl_0));
                    exit;
                } 
                else 
                {
                    $log->log('common/lib_commission.log',__LINE__ . ":创建分销等级[" . $level . "]和会员【" . $cl_0 . "】信息成功: $cl_res_5 \r\n $cl_res_4 \r\n $cl_res_3 \r\n");
                }
            }
        } 
        else 
        {
            //当推荐人不存在时
            $cl_res_6 = UserDistributionModel::where('store_id',$store_id)->order('rt','desc')->select()->toArray();
            if ($cl_res_6) 
            {
                $sql = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$cl_res_6[0]['user_id']])->find();
                $sql->rt = Db::raw('rt+2');
                $r = $sql->save();
                if (!$r) 
                {
                    Db::rollback();
                    $log->log('common/lib_commission.log',__LINE__ . ":创建会员【" . $cl_0 . "】信息失败: ".$cl_res_6[0]['user_id']." \r\n");
                    echo json_encode(array('status' => 0, 'err' => '参数错误 code:31', 'sql' =>$cl_res_6[0]['user_id']));
                    exit;
                }
                else 
                {
                    $log->log('common/lib_commission.log',__LINE__ . ":创建会员【" . $cl_0 . "】信息成功: ".$cl_res_6[0]['user_id']." \r\n");
                }
                $rt      = $cl_res_6[0]['rt'];
                $lrt     = intval($rt) + 1;
                $uplevel = intval($cl_res_6[0]['uplevel']) + 1;
                $pid     = $cl_res_6[0]['user_id'];

                $level    = $cl_1;
                $cl_sql_5 = new UserDistributionModel();
                $cl_sql_5->store_id = $store_id;
                $cl_sql_5->user_id = $cl_0;
                $cl_sql_5->pid = $pid;
                $cl_sql_5->level = $level;
                $cl_sql_5->lt = $rt;
                $cl_sql_5->rt = $lrt;
                $cl_sql_5->uplevel = $uplevel;
                $cl_sql_5->add_date = date('Y-m-d H:i:s');
                $cl_sql_5->save();
                $cl_res_5 = $cl_sql_5->id;
                //事务
                if ($cl_res_5 < 1) 
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$cl_0.' 添加成为分销商失败',1,1,0);
                    $log->log('common/lib_commission.log',__LINE__ . ":创建分销等级[" . $level . "]和会员【" . $cl_0 . "】信息失败 \r\n");
                    echo json_encode(array('status' => 0, 'err' => '参数错误 code:30', 'user_id' => $cl_0));
                    exit;
                } 
                else 
                {
                    $log->log('common/lib_commission.log',__LINE__ . ":创建分销等级[" . $level . "]和会员【" . $cl_0 . "】信息成功 \r\n");
                }
            } 
            else 
            {
                $rt      = 0;
                $lrt     = 1;
                $uplevel = 0;
                $pid     = '';

                //获取商城初始会员ID
                $res_r = UserModel::where('store_id',$store_id)->order('id','asc')->limit(0,1)->select()->toArray();
                if($res_r)
                {   
                    $r_user_id = $res_r[0]['user_id'];
                    $cl_sql_10 = new UserDistributionModel();
                    $cl_sql_10->store_id = $store_id;
                    $cl_sql_10->user_id = $r_user_id;
                    $cl_sql_10->pid = $pid;
                    $cl_sql_10->level = 0;
                    $cl_sql_10->lt = $rt;
                    $cl_sql_10->rt = $lrt;
                    $cl_sql_10->uplevel = $uplevel;
                    $cl_sql_10->add_date = date('Y-m-d H:i:s');
                    $cl_sql_10->save();
                    $cl_res_10 = $cl_sql_10->id;
                    if ($cl_res_10 < 1) 
                    {
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$cl_0.' 添加成为分销商失败',1,1,0);
                        $log->log('common/lib_commission.log',__LINE__ . ":创建初始分销会员信息失败: $r_user_id \r\n");
                        echo json_encode(array('status' => 0, 'err' => '参数错误 code:30', 'user_id' => $r_user_id));
                        exit;
                    }
                }
                $this->create_level($cl_0, $cl_1, $cl_2, $store_id);
            }
        }
        $Jurisdiction->admin_record($store_id, $operator, '将用户ID：'.$cl_0.' 添加成为分销商',1,1,0);
        Db::commit();
        return;
    }

    // 日志
    public function Log($Log_content)
    {
        $time = date("Y-m-d");
        $lktlog = new LaiKeLogUtils();

        $lktlog->log("app/common/commission.log",$Log_content);
        return;
    }
}
