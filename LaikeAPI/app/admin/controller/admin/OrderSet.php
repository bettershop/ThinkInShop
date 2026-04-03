<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;

use app\admin\model\OrderConfigModel;
use app\admin\model\MchConfigModel;
use app\admin\model\IntegralConfigModel;
/**
 * 功能：后台订单设置类
 * 修改人：PJY
 */
class OrderSet extends BaseController
{   
    //订单设置页面
    public function index()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = $this->request->param('accessId');
        $mch_id = cache($access_id.'_'.$store_type);

        $r = OrderConfigModel::where('store_id',$store_id)->select()->toArray();
        $r_config = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
        $day = 0;//订单售后时间
        $hour = 0;//订单失效时间
        $remind_day = 0;
        $remind_hour = 0;//提醒限制
        $auto_good_comment_day = 0;
        $amsTime = 0;//积分发放时间 收货后多少天返回积分
        $proportion = 0; //购物赠积分比例
        $giveStatus = 0;//发放状态(0=收货后 1=付款后)
        $autoCommentContent = '';
        if ($r)
        {
            $order_failure = $r[0]['order_failure'];//订单失效时间
            $order_after = $r[0]['order_after'];//订单售后时间
            $auto_the_goods = $r[0]['auto_the_goods'];//确认收货设置
            $order_ship = $r[0]['order_ship'];//发货时限
            $remind = $r[0]['remind'];//提醒限制
            $auto_good_comment_day = $r[0]['auto_good_comment_day'];//自动好评设置
            $autoCommentContent = $r[0]['auto_comment_content']; //好评内容
            $hour = $order_ship;
            //积分设置
            if ($r_config) 
            {
                $amsTime = $r_config[0]['ams_time'];//积分发放时间 收货后多少天返回积分
                $proportion = $r_config[0]['proportion'];//购物赠积分比例
                $giveStatus = $r_config[0]['give_status'];//发放状态(0=收货后 1=付款后)
            }
            if ($remind == 0)
            {
                $remind_day = 0;
                $remind_hour = 0;
            }
            else
            {
                $remind_day = floor($remind / 24);
                $remind_hour = $remind % 24;
            }
            if ($order_ship > 24)
            {
                $day = floor($order_ship / 24);
                $hour = $order_ship % 24;
            }

            if ($hour == 24)
            {
                $day = $day + 1;
                $hour = 0;
            }
        }
        else
        {
            $order_failure = 1;
            $order_after = 7;
            $auto_the_goods = 7;
        }
        $package_settings = $r ? $r[0]['package_settings'] : ''; // 包邮设置 0.未开启 1.开启
        $same_piece = $r ? $r[0]['same_piece'] : ''; // 同件
        $same_order = $r ? $r[0]['same_order'] : ''; // 同单

        $message = Lang("Success");
        return output(200,$message,array('auto_good_comment_day'=>$auto_good_comment_day,'order_failure'=>$order_failure,'order_after'=>$order_after,'auto_the_goods'=>$auto_the_goods,'day'=>$day,'hour'=>$hour,'package_settings'=>$package_settings,'same_piece'=>$same_piece,'same_order'=>$same_order,'remind_day'=>$remind_day,'remind_hour'=>$remind_hour,'amsTime'=>$amsTime,'proportion'=>$proportion,'giveStatus'=>$giveStatus,'autoCommentContent'=>$autoCommentContent));
    }

    // 保存订单设置
    public function saveConfig()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = $this->request->param('accessId');

        $auto_the_goods = trim($this->request->param('autoTheGoods'))?trim($this->request->param('autoTheGoods')):7; // 自动收货时间
        $order_failure = addslashes(trim($this->request->param('orderFailure')))?addslashes(trim($this->request->param('orderFailure'))):2; //订单过期删除时间
        $order_after = addslashes(trim($this->request->param('orderAfter')))?addslashes(trim($this->request->param('orderAfter'))):0; //订单售后时间
        $remind_day = trim($this->request->param('remindDay'))?trim($this->request->param('remindDay')):1; // 提醒限制
        $remind_hour = trim($this->request->param('remindHour'))?trim($this->request->param('remindHour')):0; // 提醒限制
        $auto_good_comment_day = trim($this->request->param('autoGoodCommentDay'))?trim($this->request->param('autoGoodCommentDay')):0; // 多少天默认好评
        $auto_comment_content = addslashes(trim($this->request->param('autoCommentContent')))?addslashes(trim($this->request->param('autoCommentContent'))):''; //好评内容
        $package_settings = trim($this->request->param('packageSettings')); // 包邮设置 0.未开启 1.开启
        $same_piece = (int)trim($this->request->param('samePiece')); // 同件
        $same_order = (int)trim($this->request->param('sameOrder')); // 同单
        $proportion = trim($this->request->param('proportion'))?trim($this->request->param('proportion')):0;//购物赠积分比例
        $giveStatus = trim($this->request->param('giveStatus'))?trim($this->request->param('giveStatus')):0;//发放状态(0=收货后 1=付款后)
        $amsTime = trim($this->request->param('amsTime'))?trim($this->request->param('amsTime')):0;//积分发放时间 收货后多少天返回积分
        
        $mch_id = cache($access_id.'_'.$store_type);
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $lktlog = new LaiKeLogUtils();
        if ($package_settings == 1)
        {
            if ($same_piece != '')
            {
                if (is_numeric($same_piece))
                {
                    if ($same_piece < 0)
                    {   
                        $message = Lang("admin_order.3");
                        return output(ERROR_CODE_TJSLBNWFSHL,$message);
                    }
                }
                else
                {   
                    $message = Lang("admin_order.4");
                    return output(ERROR_CODE_TJSLBNWFSHL,$message);
                }
            }
            if ($same_order != '')
            {
                if (is_numeric($same_order))
                {
                    if ($same_order < 0)
                    {   

                        $message = Lang("admin_order.5");
                        return output(ERROR_CODE_TDSLBNWFSHL,$message);
                    }
                }
                else
                {
                    $message = Lang("admin_order.6");
                    return output(ERROR_CODE_TDSLBNWFSHL,$message);
                }
            }
        }
        else
        {
            $same_piece = 0;
            $same_order = 0;
        }
        if ($auto_the_goods != '')
        {
            if (is_numeric($auto_the_goods))
            {
                if ($auto_the_goods <= 0)
                {
                    $message = Lang("admin_order.7");
                    return output(ERROR_CODE_ZDSHSJBNWFSHL,$message);
                }
            }
            else
            {   
                $message = Lang("admin_order.8");
                return output(ERROR_CODE_ZDSHSJBNWFSHL,$message);
            }
        }
        if ($order_failure != '')
        {
            if (is_numeric($order_failure))
            {
                if ($order_failure <= 0)
                {
                    $message = Lang("admin_order.9");
                    return output(ERROR_CODE_DDGQSCSJBNWFSHL,$message);
                }
            }
            else
            {
                $message = Lang("admin_order.10");
                return output(ERROR_CODE_DDGQSCSJBNWFSHL,$message);
            }
        }
        if ($order_after != '')
        {
            if (floor($order_after)==$order_after)
            {
                if ($order_after < 0)
                {
                    $message = Lang("admin_order.11");
                    return output(ERROR_CODE_DDSHSJBNWFSHL,$message);
                }
            }
            else
            {
                $message = Lang("admin_order.12");
                return output(ERROR_CODE_DDSHSJBNWFSHL,$message);
            }
        }
        if ($remind_day == '' || $remind_day == '0')
        {
            $remind_day = 0;
        }
        else
        {
            if ($remind_day < 0)
            {
                $message = Lang("admin_order.13");
                return output(ERROR_CODE_QSRZQDTXXZSJ,$message);
            }
        }
        if ($remind_hour == '' || $remind_hour == '0')
        {
            $remind_hour = 0;
        }
        else
        {
            if ($remind_hour < 0)
            {   

                $message = Lang("admin_order.13");
                return output(ERROR_CODE_QSRZQDTXXZSJ,$message);
            }
        }
        if ($remind_day == 0 && $remind_hour == 0)
        {
            $remind = 0;
        }
        else
        {
            $remind = $remind_hour + ($remind_day * 24);
        }

        $jfStatus = 0; //积分插件状态
        $overdueime = 0; //取消积分失效
        if ($proportion > 0) 
        {
            $jfStatus = 1; //开启
        }
        Db::startTrans();
        $r = OrderConfigModel::where('store_id',$store_id)->select()->toArray();
        $r_config = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
        if ($r)
        {
            $sql = OrderConfigModel::where('store_id',$store_id)->find();
            $sql->auto_good_comment_day = $auto_good_comment_day;
            $sql->auto_comment_content = $auto_comment_content;
            $sql->order_failure = $order_failure;
            $sql->order_after = $order_after;
            $sql->auto_the_goods = $auto_the_goods;
            $sql->remind = $remind;
            $sql->modify_date = date("Y-m-d H:i:s");
            $sql->package_settings = $package_settings;
            $sql->same_piece = $same_piece;
            $sql->same_order = $same_order;
            
            $r_1 = $sql->save();
            if (!$r_1)
            {
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单设置失败！store_id:" . $store_id);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '修改了订单设置信息失败',2,1,0,$operator_id);
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            else
            {
                
                if ($r_config) 
                {
                    $sql_1 = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->find();
                    $sql_1->status = $jfStatus;
                    $sql_1->same_piece = $same_piece;
                    $sql_1->package_settings = $package_settings;
                    $sql_1->order_failure = $order_failure*60*60;
                    $sql_1->order_after = $order_after*60*60*24;
                    $sql_1->auto_the_goods = $auto_the_goods*60*60*24;
                    $sql_1->deliver_remind = $remind_hour*60*60;
                    $sql_1->auto_good_comment_day = $auto_good_comment_day*60*60*24;
                    $sql_1->proportion = $proportion;
                    $sql_1->give_status = $giveStatus;
                    $sql_1->overdue_time = $overdueime;
                    $sql_1->ams_time = $amsTime;
                    $r_2 = $sql_1->save();
                    if (!$r_2) 
                    {
                        $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单设置失败！store_id:" . $store_id);
                        $Jurisdiction->admin_record($store_id, $operator, '修改了订单设置信息失败',2,1,0,$operator_id);
                        Db::rollback();
                        $message = Lang("operation failed");
                        return output(ERROR_CODE_CZSB,$message);
                    }
                }
                $Jurisdiction->admin_record($store_id, $operator, '修改了订单设置信息',2,1,0,$operator_id);
                Db::commit();
                $message = Lang("Success");
                return output(200,$message);
            }
        }
        else
        {
            $sql = new OrderConfigModel();
            $sql->store_id = $store_id;
            $sql->auto_good_comment_day = $auto_good_comment_day;
            $sql->auto_comment_content = $auto_comment_content;
            $sql->order_failure = $order_failure;
            $sql->order_after = $order_after;
            $sql->auto_the_goods = $auto_the_goods;
            $sql->remind = $remind;
            $sql->modify_date = date("Y-m-d H:i:s");
            $sql->package_settings = $package_settings;
            $sql->same_piece = $same_piece;
            $sql->same_order = $same_order;
            $sql->save();
            $r_1 = $sql->id;
            if ($r_1 < 1)
            {
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加订单设置失败！store_id:" . $store_id);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '添加了订单设置信息失败',1,1,0,$operator_id);
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            else
            {
                $sql_1 = new IntegralConfigModel();
                $sql_1->store_id = $store_id;
                $sql_1->mch_id = $mch_id;
                $sql_1->status = $jfStatus;
                $sql_1->same_piece = $same_piece;
                $sql_1->package_settings = $package_settings;
                $sql_1->order_failure = $order_failure*60*60;
                $sql_1->order_after = $order_after*60*60*24;
                $sql_1->auto_the_goods = $auto_the_goods*60*60*24;
                $sql_1->deliver_remind = $remind_hour*60*60;
                $sql_1->auto_good_comment_day = $auto_good_comment_day*60*60*24;
                $sql_1->proportion = $proportion;
                $sql_1->give_status = $giveStatus;
                $sql_1->overdue_time = $overdueime;
                $sql_1->ams_time = $amsTime;
                $sql_1->save();
                $r_2 = $sql_1->id;
                if ($r_2 < 1)
                {
                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加订单设置失败！store_id:" . $store_id);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '添加了订单设置信息失败',1,1,0,$operator_id);
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_CZSB,$message);
                }
                $Jurisdiction->admin_record($store_id, $operator, '添加了订单设置信息',1,1,0,$operator_id);
                Db::commit();
                $message = Lang("Success");
                return output(200,$message);
            }
        }
        return;
    }

    //店铺设置
    public function mchIndex()
    {
        $store_id = trim($this->request->param('storeId'))?trim($this->request->param('storeId')):trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('storeType'))?trim($this->request->param('storeType')):trim($this->request->param('store_type'));

        $mch_id = addslashes($this->request->param('mchId'))?addslashes($this->request->param('mchId')):addslashes($this->request->post('mchId'));
        $is_type = addslashes($this->request->param('isType'))?addslashes($this->request->param('isType')):addslashes($this->request->post('isType'));

        if(empty($mch_id))
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
        $list = array();
        $res = MchConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('package_settings,same_piece,same_order')->select()->toArray();
        if($res)
        {
            $list = $res[0];
        }
        $message = Lang("Success");
        return output(200,$message,$list);
    }


    //保存店铺设置
    public function mchSaveConfig()
    {
        $store_id = trim($this->request->param('storeId'))?trim($this->request->param('storeId')):trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('storeType'))?trim($this->request->param('storeType')):trim($this->request->param('store_type'));

        $mch_id = addslashes($this->request->param('mchId'));
        $is_type = addslashes($this->request->param('isType'));

        $package_settings = addslashes($this->request->param('packageSettings')); // 包邮设置 0.未开启 1.开启
        $same_piece = addslashes($this->request->param('samePiece')); // 同件
        $same_order = addslashes($this->request->param('sameOrder')); // 同单

        $lktlog = new LaiKeLogUtils();
        if(empty($mch_id))
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
        if ($package_settings == 1)
        {
            if ($same_piece != '')
            {
                if (is_numeric($same_piece))
                {
                    if ($same_piece < 0)
                    {   
                        $message = Lang("admin_order.3");
                        return output(ERROR_CODE_TJSLBNWFSHL,$message);
                    }
                }
                else
                {   
                    $message = Lang("admin_order.4");
                    return output(ERROR_CODE_TJSLBNWFSHL,$message);
                }
            }
            if ($same_order != '')
            {
                if (is_numeric($same_order))
                {
                    if ($same_order < 0)
                    {   

                        $message = Lang("admin_order.5");
                        return output(ERROR_CODE_TDSLBNWFSHL,$message);
                    }
                }
                else
                {
                    $message = Lang("admin_order.6");
                    return output(ERROR_CODE_TDSLBNWFSHL,$message);
                }
            }
        }
        else
        {
            $same_piece = 0;
            $same_order = 0;
        }
        
        $r0 = MchConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('id')->select()->toArray();
        if($r0)
        {
            $sql_where = array('store_id'=>$store_id,'mch_id'=>$mch_id);
            $sql_update = array('package_settings'=>$package_settings,'same_piece'=>$same_piece,'same_order'=>$same_order);
            $res = Db::name('mch_config')->where($sql_where)->update($sql_update);
            if(!$res)
            {
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单设置失败！条件参数:" . json_encode($sql_where) . "；修改参数:" . json_encode($sql_update));
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }
        else 
        {
            $sql_insert = array('store_id'=>$store_id,'mch_id'=>$mch_id,'package_settings'=>$package_settings,'same_piece'=>$same_piece,'same_order'=>$same_order);
            $res = Db::name('mch_config')->insert($sql_insert);
            if(!$res)
            {
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "添加订单设置失败！参数:" . json_encode($sql_insert));
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }

        $message = Lang("Success");
        return output(200,$message);
    }
}
