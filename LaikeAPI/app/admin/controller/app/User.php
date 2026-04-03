<?php
namespace app\admin\controller\app;

require_once MO_LIB_DIR."/RedisClusters.php";

use app\BaseController;
use app\common\ServerPath;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\Plugin\Plugin;
use think\facade\Db;
use think\facade\Request;
use think\cache;
use app\common\GETUI\LaikePushTools;
use app\common\EmailController;

use app\admin\model\AdminModel;
use app\admin\model\ConfigModel;
use app\admin\model\UserModel;
use app\admin\model\SystemMessageModel;
use app\admin\model\MemberConfigModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\OrderModel;
use app\admin\model\UserCollectionModel;
use app\admin\model\UserFootprintModel;
use app\admin\model\NoticeModel;
use app\admin\model\WithdrawModel;
use app\admin\model\UserRuleModel;
use app\admin\model\BondModel;
use app\admin\model\RecordModel;
use app\admin\model\BankCardModel;
use app\admin\model\MchModel;
use app\admin\model\MchConfigModel;
use app\admin\model\SessionIdModel;
use app\admin\model\FinanceConfigModel;
use app\admin\model\ConfigureModel;
/**
 * 功能：移动端个人中心类
 * 修改人：PJY
 */

class User extends BaseController
{
    // 请求我的数据
    public function index()
    {
        $store_id = trim(Request::param('store_id'));
        $Language = trim(Request::param('Language')); // 语言
        $store_type = trim(Request::param('store_type'));
        // 获取信息
        $access_id = trim(Request::param('access_id')); // 授权id
        $mobile_n = trim(Request::param('mobile'));//新用户授权进入的传参
        // 查询系统参数
        $r_1 = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r_1)
        {
            $logo = $r_1['0']['logo'];
            $company = $r_1['0']['company'];
            $Hide_your_wallet = $r_1['0']['Hide_your_wallet'];
            $wx_headimgurl = $r_1[0]['wx_headimgurl'];//默认微信用户头像
            $defaultHeaderImgUrl = ServerPath::getimgpath($wx_headimgurl);//默认微信用户头像
            $defaultUserName = $r_1[0]['wx_name'];  //默认微信用户名
        }
        else
        {
            $logo = '';
            $company = '';
            $Hide_your_wallet = 0;
        }
        
        $logo = ServerPath::getimgpath($logo);
        // 支付状态
        $payment = Tools::getPayment($store_id);

        // 查询插件是否开启
        $plugin = array();
        if($Hide_your_wallet == 0 && $payment['wallet_pay'] == 1)
        {
            //钱包 0 隐藏 1 显示
            $plugin['wallet'] = 1;
        }
        else
        {
            $plugin['wallet'] = 0;
        }

        $Plugin_arr = new Plugin();
        $Plugin_arr1 = $Plugin_arr->Get_plugin_entry($store_id);
        foreach ($Plugin_arr1 as $k => $v)
        {
            $plugin[$k] = $v;
        }

        $storeMchId = PC_Tools::SelfOperatedStore($store_id);

        $synchronize_type = 0;//是否存在其他账号需要同步0否1是
        if (!empty($access_id))
        { // 存在
            $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组
            if ($getPayload_test == false)
            { // 过期
                ob_clean();
                $message =  Lang('Success');
                return output(200,$message,array('defaultHeaderImgUrl' => $defaultHeaderImgUrl,'defaultUserName' => $defaultUserName,'plugin'=>$plugin));
            }
            // 查询会员信息
            $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
            if ($r)
            {
                $user['headimgurl'] = $r[0]['headimgurl'];
                $user['wx_name'] = $r[0]['wx_name'];
                $user['user_name'] = htmlspecialchars_decode($r[0]['user_name']);
                // $user['money'] = number_format($r[0]['money'],2);
                $user['money'] = $r[0]['money'];
                $user['score'] = $r[0]['score'];
                $user['e_mail'] = $r[0]['e_mail']; // 邮箱
                $user['cpc'] = $r[0]['cpc']; // 区号
                $user['country_num'] = $r[0]['country_num']; // 国家代码
                $user['mobile'] = $mobile = $r[0]['mobile'];//电话
                $user['haveAllUserInfo'] = false;
                $user['havaMobile'] = false;
                if($r[0]['mobile'] != '')
                {
                    $user['havaMobile'] = true;
                }
                if($r[0]['is_default_value'] == 1)
                {
                    $user['haveAllUserInfo'] = true;
                }
                $user['checkInfo'] = false;
                if($r[0]['mobile'] != '' && $r[0]['birthday'] != '' && $r[0]['sex'] != '')
                {
                    $user['checkInfo'] = true;
                }
                $user['is_default_birthday'] = $r[0]['is_default_birthday']; // 是否使用的默认日期配置   1是 2否  默认否
                $user['sex'] = $r[0]['sex'];
                $user['zhanghao'] = $r[0]['zhanghao'];
                if($r[0]['mima'])
                {
                    $user['isLoginPwd'] = true;
                }
                else
                {
                    $user['isLoginPwd'] = false;
                }

                $user['wx_withdraw'] = false; 
                if($r[0]['wx_id'] != '' && $r[0]['wx_id'] != null)
                {
                    $user['wx_withdraw'] = true; 
                }

                if($mobile_n != '')
                {
                    //查询手机号是否绑定其他账号
                    $sql_q = "select id from lkt_user where store_id = '$store_id' and (access_id != '$access_id' or access_id is null) and mobile = '$mobile_n' and is_lock = 0";
                    $res_q = Db::query($sql_q);
                    if($res_q)
                    {
                        $synchronize_type = 1;
                    }
                    else
                    {
                        //同步用户信息
                        $sql_u = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                        $sql_u->mobile = $mobile_n;
                        $sql_u->save();
                    }
                }
                
                if ($r[0]['birthday'])
                {//不为null
                    $user['birthday'] = date("Y-m-d", strtotime($r[0]['birthday']));
                }
                else
                {//为null
                    $user['birthday'] = '0000-00-00';
                }
                $user_id = $r[0]['user_id'];
                //查询未读消息数量
                $xxnum = SystemMessageModel::where(['store_id'=>$store_id,'recipientid'=>$user_id,'type'=>1])->count();
                $memberInfo = array();
                if(isset($plugin['member']) && $plugin['member'] == 1 )
                {   
                    $memberInfo['remind'] = 0;
                    $memberInfo['grade'] = $r[0]['grade'];
                    $memberInfo['gradeAddTime'] = $r[0]['grade_add'];
                    $memberInfo['gradeEndTime'] = $r[0]['grade_end'];
                    $memberInfo['gradeM'] = $r[0]['grade_m'];
                    $memberInfo['isBox'] = $r[0]['is_box'];
                    $memberInfo['isOut'] = $r[0]['is_out'];
                    $memberInfo['tuiId'] = $r[0]['tui_id'];

                    //查询是否需要续费提醒
                    if($r[0]['grade'] == 1)
                    {
                        $res_1 = MemberConfigModel::where(['store_id'=>$store_id,'renew_open'=>1,'is_open'=>1])->field('renew_day')->select()->toArray();
                        if ($res_1)
                        {
                            $auto_time = $res_1[0]['renew_day'];//自动续费提醒时间
                            //计算离会员到期还有多久
                            $now = strtotime("now");
                            $end = strtotime($r[0]['grade_end']);
                            if ($now > $end || $r[0]['grade'] == 0)
                            {
                                $memberInfo['remind'] = 0;
                            }
                            else
                            {
                                $cha = floor(($end - $now) / 86400); //相差天数
                                if ($cha <= $auto_time && $r[0]['is_box'] == 1 && $r[0]['is_out'] == 0)
                                {//到了自动提醒时间,且用户未关闭提示
                                    $memberInfo['remind'] = 1;

                                    $msg_title = Lang("member.15");
                                    $msg_content = Lang("member.16");
                                    $pusher = new LaikePushTools();
                                    $pusher->pushMessage($user_id,$msg_title, $msg_content, $store_id, '');
                                }
                            }
                            //权益
                            $memberInfo['member_equity'] = array();
                            $r_config = MemberConfigModel::where('store_id',$store_id)->field('member_equity')->select()->toArray();
                            if($r_config)
                            {
                                $memberInfo['member_equity'] = json_decode($r_config[0]['member_equity'],true);
                            }
                        }
                    }
                }
                $level = 0;
                $imgurl_my = '';
                $rate = '';
                $font_color = '';
                $imgurl_s = '';
                $date_color = '';

                //是否分销商
                $user['isDistribution'] = 0;
                $fx_status = Db::name('distribution_config')->where('store_id', $store_id)->value('status');//插件状态 0关闭 1开启
                if($fx_status == 1)
                {
                    $fxOrderNum = OrderModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'otype'=>'FX','recycle'=>0])->count();
                    $fx_level = Db::name('user_distribution')->where(['store_id'=>$store_id,'user_id'=>$user_id])->value('level');
                    if($fx_level > 0)
                    {
                        $user['isDistribution'] = 1;
                    }
                    // else
                    // {
                    //     if($fxOrderNum > 0)
                    //     {
                    //         $user['isDistribution'] = 2;
                    //     }
                    // }
                }

                //无会员等级
                $have_grade = 0;
                // //个人中心小红点
                $num_arr = [0, 1, 2, 3, 4];
                $res_order = [];

                $fs_num = 0;
                $sql = "";
                foreach ($num_arr as $key => $value)
                {
                    if ($value == '4')
                    {
                        $orders_res = ReturnOrderModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                                                    ->where('re_type','>',0)
                                                    ->where('r_type','in','0,1,3')
                                                    ->where('sNo','like','GM%')
                                                    ->field('sNo')
                                                    ->count();
                        $res_order[$key] = $orders_res;
                    }
                    else
                    {
                        if ($value == 1)
                        {
                            $re = OrderModel::where(['store_id'=>$store_id,'status'=>$value,'user_id'=>$user_id,'recycle'=>0,'otype'=>'GM'])->field('id')->count();

                            $res_order[$key] = $re;
                        }
                        elseif($value == 3)
                        {
                            $sql_order = "SELECT a.sNo FROM lkt_order a RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE a.store_id = '$store_id' and a.user_id = '$user_id' and a.status = 5 and a.recycle = 0 and a.otype = 'GM' and a.sNo not in (select oid from lkt_comments where store_id = '$store_id' and uid = '$user_id') GROUP BY a.sNo";
                            $order_num = Db::query($sql_order);
                            $res_order[$key] = count($order_num);

                            $sql_fs_order = "SELECT a.sNo FROM lkt_order a RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo LEFT JOIN lkt_product_list as p on p.id = d.p_id
                    WHERE a.store_id = '$store_id' and a.user_id = '$user_id' and a.status = 5 and a.recycle = 0 and a.otype = 'FS' and a.sNo not in (select oid from lkt_comments where store_id = '$store_id' and uid = '$user_id') GROUP BY a.sNo";
                            $fs_order_num = Db::query($sql_fs_order);
                            $fs_num = $fs_num + count($fs_order_num);
                        }
                        else
                        {
                            $order_num = OrderModel::where(['store_id'=>$store_id,'status'=>$value,'user_id'=>$user_id,'recycle'=>0,'otype'=>'GM'])->field('id')->count();
                            $res_order[$key] = $order_num;

                            $fs_order_num = OrderModel::where(['store_id'=>$store_id,'status'=>$value,'user_id'=>$user_id,'recycle'=>0,'otype'=>'FS'])->field('id')->count();
                            $fs_num = $fs_num + $fs_order_num;
                        }
                    }
                }
                $collection = UserCollectionModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'type'=>1])->count();
                if ($collection)
                {
                    $collection_num = $collection;
                }
                else
                {
                    $collection_num = 0;
                }

                $footprint = UserFootprintModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->count();
                if ($footprint)
                {
                    $footprint_num = $footprint;
                }
                else
                {
                    $footprint_num = 0;
                }
                $user['coupon_num'] = 0;
                if(file_exists('../app/common/Plugin/CouponPublicMethod.php'))
                {   
                    bind('coupon','app\common\Plugin\CouponPublicMethod');
                    $coupon_list = app('coupon')->mycoupon($store_id, $user_id, 0);
                    $user['coupon_num'] = count($coupon_list['list']);
                }

                $data = array('user' => $user, 'th' => $res_order['4'], 'dfk_num' => $res_order['0'], 'dfh_num' => $res_order['1'], 'dsh_num' => $res_order['2'], 'dpj_num' => $res_order['3'], 'logo' => $logo, 'company' => $company, 'collection_num' => $collection_num, 'footprint_num' => $footprint_num, 'level' => $level, 'font_color' => $font_color, 'date_color' => $date_color, 'rate' => $rate, 'imgurl_my' => $imgurl_my, 'imgurl_s' => $imgurl_s, 'xxnum' => $xxnum,'synchronize_type'=>$synchronize_type,'memberInfo'=>$memberInfo,'storeMchId'=>$storeMchId,'fs_num'=>$fs_num);
                ob_clean();
                $message =  Lang('Success');
                return output(200,$message,array('data' => $data,'plugin'=>$plugin));
            }
            else
            {
                ob_clean();
                $message =  Lang('Success');
                return output(200,$message,array('defaultHeaderImgUrl' => $defaultHeaderImgUrl,'defaultUserName' => $defaultUserName,'plugin'=>$plugin));
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Success');
            return output(200,$message,array('defaultHeaderImgUrl' => $defaultHeaderImgUrl,'defaultUserName' => $defaultUserName,'plugin'=>$plugin));
        }
    }

    public function luck_draw_msg($openid)
    {

        $store_id = trim(Request::param('store_id'));
        $log = new LaiKeLogUtils();

        $page = 'pages/index/index';
        $r = NoticeModel::where('store_id',$store_id)->select()->toArray();
        if (!$r)
        {
            return;
        }
        $template_id = $r[0]['buy'];
        $time = date('Y-m-d h:i:s', time());
        $send_id = $template_id; // 订单号、消费金额、订购时间
        $keyword1 = array('value' => "GM20191106121410");
        $keyword2 = array('value' => $time);
        //拼成规定的格式
        $r = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r)
        {
            $appid = $r[0]['appid'];
            // 小程序唯一标识
            $appsecret = $r[0]['appsecret'];
            // 小程序的 app secret
        }
        $o_data = array('character_string1' => $keyword1, 'date3' => $keyword2);
        $pusher = new LaikePushTools();
        $res = $pusher->Send_Prompt($appid, $appsecret, $openid, $page, $send_id, $o_data);

        $log->log("app/user.log",__METHOD__ . ":" . __LINE__ . "小程序消息推送:" . json_encode($res));

        ob_clean();
       return output(200,'',array('list' => $res));
    }

    // 我的钱包
    public function details()
    {
        $store_id = trim(Request::param('store_id'));
        // 接收信息
        $access_id = trim(Request::param('access_id')); // 授权id
        // 根据微信id,查询用户id
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,money')->select()->toArray();
        if (!empty($r))
        {
            $user_id = $r[0]['user_id']; // 用户id
            $user_money = $r[0]['money']; // 用户余额
            if ($user_money == '')
            {
                $user_money = 0;
            }
            $APP_INDEX_KEY = LAIKE_REDIS_PRE_KEY.__CLASS__.'_'.__METHOD__.'_record'.$user_id;
            $record_data = cache($APP_INDEX_KEY);
            LaiKeLogUtils::log("common/a_redis.log",$record_data);
            if($record_data)
            {
                $message = Lang('Success');
                $list = json_decode($record_data);
            }
            else
            {
                // 根据用户id、类型为充值,查询操作列表-----消费记录
                $r_1 = RecordModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'is_mch'=>0])
                                ->where('type','in','1,2,3,4,5,11,12,13,14,19,20,22,23,24,26,27,38,41,42')
                                ->order('add_date','desc')
                                ->limit(0,10)
                                ->select()
                                ->toArray();
                $list = [];
                if ($r_1)
                {
                    foreach ($r_1 as $k => $v)
                    {
                        $details_id = $v['details_id'];
                        $v['time'] = substr($v['add_date'], 0, strrpos($v['add_date'], ':'));

                        if($v['type'] == 2 || $v['type'] == 4 || $v['type'] == 11 || $v['type'] == 12 || $v['type'] == 21 || $v['type'] == 26)
                        { // 支出
                            $v['moneyType'] = 2;
                        }
                        else
                        { // 收入
                            $v['moneyType'] = 1;
                        }
                        
                        $sql_2 = "select type from lkt_record_details where store_id = '$store_id' and id = '$details_id' ";
                        $r_2 = Db::query($sql_2);
                        if($r_2)
                        {
                            $v['type'] = $r_2[0]['type'];
                        }

                        $list[$k] = $v;
                    }
                    cache($APP_INDEX_KEY,json_encode($list),10);
                }
            }
            $count = WithdrawModel::where(['store_id'=>$store_id,'status'=>0,'user_id'=>$user_id,'is_mch'=>0])->count(); // 条数
            if ($count > 0)
            {
                $status = 1;
            }
            else
            {
                $status = 0;
            }
        }
        else
        {
            ob_clean();

            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }
        //查询钱包单位
        $res_1 = FinanceConfigModel::where('store_id',$store_id)->field('unit')->select()->toArray();
        $unit = '元';
        if ($res_1)
        {
            $unit = $res_1[0]['unit'];
        }
        $APP_INDEX_KEY = LAIKE_REDIS_PRE_KEY.__CLASS__.'_'.__METHOD__.'_bond'.$store_id;
        $redis_data = cache($APP_INDEX_KEY);
        LaiKeLogUtils::log("common/a_redis.log",$redis_data);
        if($redis_data)
        {
            $message = Lang('Success');
            $bond = json_decode($redis_data);
        }
        else
        {   
            $bond = array();
            //查询保证金金额
            $bond_res1 = UserRuleModel::where('store_id',$store_id)->field('bond')->select()->toArray();
            if($bond_res1)
            {
                $bond['bond'] = $bond_res1[0]['bond'];
                $bond['is_bond'] = 0;
            }
            
            //查询是否缴纳保证金
            $bond_res2 = BondModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'is_back'=>0])->select()->toArray();
            if ($bond_res2)
            {

                $bond['is_bond'] = 1;
            }
            cache($APP_INDEX_KEY,json_encode($bond),120);
        }

        ob_clean();
        $message =  Lang('Success');
        return output(200,$message,array('unit' => $unit, 'user_money' => $user_money, 'list' => $list, 'bond' => $bond, 'status' => $status));
    }

    // 我的钱包明细加载更多
    public function wallet_detailed()
    {
        $store_id = trim(Request::param('store_id'));
        // 接收信息
        $access_id = trim(Request::param('access_id')); // 授权id
        $type = trim(Request::param('type'));
        $num = trim(Request::param('page')); // 加载次数
        $start = ($num-1) * 10;
        $end = 10;

        // 根据微信id,查询用户id
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,money')->select()->toArray();
        if (!empty($r))
        {
            $user_id = $r[0]['user_id']; // 用户id
            $user_money = $r[0]['money']; // 用户余额
            if ($user_money == '')
            {
                $user_money = 0;
            }
            if($type == 1)
            { // 收入
                $type = "1,3,5,13,14,19,20,22,23,24,27,41,42";
            }
            elseif($type == 2)
            { // 支出
                $type = "2,4,11,12,26";
            }
            else
            {
                $type = "1,2,3,4,5,11,12,13,14,19,20,22,23,24,26,27,38,41,42";
            }
            $z_num = RecordModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->where('type','in',$type)->count();
            $list = [];
            if ($z_num > 0)
            {
                // 根据用户id、类型为充值,查询操作列表-----消费记录
                $r_1 = RecordModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'is_mch'=>0])
                                ->where('type','in',$type)
                                ->field('money,add_date,type,details_id')
                                ->order('add_date','desc')
                                ->limit($start,$end)
                                ->select()
                                ->toArray();
                
                foreach ($r_1 as $k => $v)
                {
                    $details_id = $v['details_id'];
                    $v['time'] = substr($v['add_date'], 0, strrpos($v['add_date'], ':'));
                    
                    // if($v['type'] == 2 || $v['type'] == 4 || $v['type'] == 11 || $v['type'] == 12 || $v['type'] == 22 || $v['type'] == 21 || $v['type'] == 26)
                    // { // 支出
                    //     $v['moneyType'] = 2;
                    // }
                    // else
                    // { // 收入
                    //     $v['moneyType'] = 1;
                    // }

                    $sql_2 = "select type,money_type from lkt_record_details where store_id = '$store_id' and id = '$details_id' ";
                    $r_2 = Db::query($sql_2);
                    if($r_2)
                    {
                        $v['type'] = $r_2[0]['type'];
                        if($r_2[0]['money_type'] == 1)
                        { // 收入
                            $v['moneyType'] = 1;
                        }
                        else
                        { // 支出
                            $v['moneyType'] = 2;
                        }
                    }
                    
                    $list[$k] = $v;
                }
                ob_clean();
                $message =  Lang('Success');
                return output(200,$message,array('list' => $list));
            }
            else
            {
                ob_clean();
                $message =  Lang('No data available');
                return output(200,$message,array('list' => $list));
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }
    }

    // 钱包详情
    public function getRecordDetails()
    {
        $store_id = trim(Request::param('store_id'));
        // 接收信息
        $access_id = trim(Request::param('access_id')); // 授权id
        $id = trim(Request::param('id'));

        $list = array();
        // 根据微信id,查询用户id
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,money')->select()->toArray();
        if (!empty($r))
        {
            $user_id = $r[0]['user_id']; // 用户id

            $str = " store_id as storeId,money,user_money as userMoney,type,money_type as moneyType,money_type_name as moneyTypeName,record_time as recordTime,record_notes as recordNotes,type_name as typeName,s_no as sNo,title_name as titleName,activity_code as activityCode,mch_name as mchName,withdrawal_fees as withdrawalFees,withdrawal_method as withdrawalMethod ";

            $sql0 = "select $str from lkt_record_details where store_id = '$store_id' and id = '$id' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                if($r0[0]['type'] == 11)
                {
                    $s_no = $r0[0]['sNo'];
                    if($r0[0]['withdrawalMethod'] == '' || $r0[0]['withdrawalMethod'] == '微信余额')
                    {
                        $r0[0]['withdrawalMethod'] = '微信零钱';
                    }

                    $sql1 = "select status,money,add_date,examine_date,refuse,s_charge from lkt_withdraw where store_id = '$store_id' and user_id = '$user_id' and txsno = '$s_no' ";
                    $r1 = Db::query($sql1);
                    if($r1)
                    {
                        $r0[0]['status'] = $r1[0]['status'];
                        $r0[0]['account'] = number_format(round($r1[0]['money'],2) - round($r1[0]['s_charge'],2),2);
                        $r0[0]['startTime'] = $r1[0]['add_date'];
                        $r0[0]['examineTime'] = $r1[0]['examine_date'];
                        $r0[0]['refuse'] = $r1[0]['refuse'];
                    }
                }
                $list = $r0[0];
            }

            ob_clean();
            $message =  Lang('Success');
            return output(200,$message,$list);
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }
    }

    // 进入提现页面
    public function into_withdrawals()
    {    
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $shop_id = (int)trim(Request::param('shop_id')); // 店铺ID

        $wx_name = ''; // 微信名称
        $wx_open = false; // wx_open：是否开启微信余额提现
        $wx_withdraw = false; // 判断用户是否绑定过微信可以进行微信零钱提现

        $sql_payment_config = "select p.id from lkt_payment as p left join lkt_payment_config as c on p.id = c.pid where c.store_id = '$store_id' and p.class_name = 'wechat_v3_withdraw' and c.status = 1 ";
        $r_payment_config = Db::query($sql_payment_config);
        if($r_payment_config)
        {
            $wx_open = true; // wx_open：是否开启微信余额提现
        }

        if ($shop_id != 0)
        {
            $data = $this->into_wallet1();
            $message =  Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            // 查询单位
            $r_1 = FinanceConfigModel::where('store_id',$store_id)->select()->toArray();
            if($r_1)
            {
                $min_amount = floatval($r_1[0]['min_amount']); // 最小提现金额
                $max_amount = floatval($r_1[0]['max_amount']); // 最大提现金额
                $serviceCharge = floatval($r_1[0]['service_charge']) / 100;
                $service_charge = floatval($r_1[0]['service_charge']) . '%';
                $unit = $r_1[0]['unit']; // 单位
            }
            else
            {
                ob_clean();
                $message =  Lang('user.58');
                return output(400,$message); 
            }
            // 根据微信id,查询会员金额
            $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
            if ($r)
            {
                $user_id = $r[0]['user_id']; // 会员id
                $money = $r[0]['money']; // 会员余额
                $mobile = $r[0]['mobile']; // 手机号
                $wx_name = $r[0]['wx_name']; // 微信名称
                if($r[0]['wx_id'] != '' && $r[0]['wx_id'] != null)
                {
                    $wx_withdraw = true; 
                }
                
                // 查询用户银行卡信息
                $rr = BankCardModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recycle'=>0])
                                    ->field('id,Cardholder,Bank_name,Bank_card_number,branch,is_default')
                                    ->select()
                                    ->toArray();
                $rew = '';
                if ($rr)
                {
                    foreach ($rr as $key => $value) {
                        $rr[$key]['Bank_card_number'] = $value['Bank_name'] .' 尾号 ('. substr($value['Bank_card_number'], -4).'）';
                    }
                    $bank_information = $rr;
                }
                else
                {
                    $bank_information = array();
                }

                $pshd = '请输入提现金额(大于' . $min_amount . '小于' . $max_amount . ')';

                $data = array('min_amount' => $min_amount, 'max_amount' => $max_amount, 'money' => floatval($money), 'pshd' => $pshd, 'unit' => $unit, 'bank_information' => $bank_information, 'mobile' => $mobile, 'service_charge' => $service_charge,'serviceCharge'=>$serviceCharge,'wx_name'=>$wx_name,'wx_open'=>$wx_open,'wx_withdraw'=>$wx_withdraw);
                ob_clean();
                $message =  Lang('Success');
                return output(200,$message,$data);
            }
            else
            {
                ob_clean();

                $message =  Lang('Illegal invasion');
                return output(400,$message);
            }
        }
    }

    // 进入保证金页面
    public function bond()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id

        // 根据微信id,查询会员金额
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if ($r)
        {
            $user_id = $r[0]['user_id']; // 会员id
            $rr = BondModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'is_back'=>0,'is_pay'=>1])->select()->toArray();
            $bond_id = 0;
            $bond = 0;
            $is_bond = 0;
            if ($rr)
            {
                //有交押金
                $bond = $rr[0]['money'];
                $is_bond = 1;
                $bond_id = $rr[0]['id'];

            }
            else
            {
                //未交押金
                $rr = UserRuleModel::where('store_id',$store_id)->select()->toArray();
                $bond = $rr[0]['bond'];
            }

            $data = array();
            $data['id'] = $bond_id;
            $data['bond'] = $bond;
            $data['is_bond'] = $is_bond;

            ob_clean();
            $message =  Lang('Success');
           return output(200,$message,array('data' => $data));
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
           return output(400,$message);
        }

    }

    // 进入保证金列表
    public function bond_list()
    {
        
        
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $pages = trim(Request::param('pages'));

        $start = ($pages - 1) * 10;
        $limit = "$start,10";

        // 根据微信id,查询会员金额
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if ($r)
        {
            $user_id = $r[0]['user_id']; // 会员id
            $rr = RecordModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                            ->where('type','in','44,45')
                            ->field('id,store_id,type,user_id,money,add_date,event')
                            ->order('add_date','desc')
                            ->limit($limit)
                            ->select()
                            ->toArray();
            $data = array();
            if ($rr)
            {
                $data = $rr;
            }

            ob_clean();
            $message =  Lang('Success');
            return output(200,$message,array('data' => $data));
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }

    }

    // 店主进入提现页面
    public function into_wallet1()
    {
        $store_id = trim(Request::param('store_id'));
        $shop_id = trim(Request::param('shop_id')); // 店铺ID
        $access_id = trim(Request::param('access_id')); // 授权id
        $id = trim(Request::param('id')); // id

        $wx_name = ''; // 微信名称
        $wx_open = false; // wx_open：是否开启微信余额提现
        $wx_withdraw = false; // 判断用户是否绑定过微信可以进行微信零钱提现
        
        $sql_payment_config = "select p.id from lkt_payment as p left join lkt_payment_config as c on p.id = c.pid where c.store_id = '$store_id' and p.class_name = 'wechat_v3_withdraw' and c.status = 1 ";
        $r_payment_config = Db::query($sql_payment_config);
        if($r_payment_config)
        {
            $wx_open = true; // wx_open：是否开启微信余额提现
        }

        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if ($r)
        {
            $user_id = $r[0]['user_id']; // 会员id
            $wx_name = $r[0]['wx_name']; // 微信名称
            if($r[0]['wx_id'] != '' && $r[0]['wx_id'] != null)
            {
                $wx_withdraw = true; 
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }

        // 根据商城id、用户id、审核通过，查询店铺ID
        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])
                    ->field('id,account_money,tel,cashable_money')
                    ->select()
                    ->toArray();
        if ($r0)
        {
            $mch_id = $r0[0]['id'];
            $account_money = $r0[0]['account_money']; // 商户余额
            $tel = $r0[0]['tel']; // 联系电话
            $cashable_money = $r0[0]['cashable_money']; // 商户可提现金额
            if ($mch_id == $shop_id)
            {
                $r1 = MchConfigModel::where('store_id',$store_id)->field('min_charge,max_charge,service_charge')->select()->toArray();
                if ($r1)
                {
                    $min_charge = floatval($r1[0]['min_charge']); // 最小提现金额
                    $max_charge = floatval($r1[0]['max_charge']); // 最大提现金额
                    $serviceCharge = floatval($r1[0]['service_charge']) / 100;
                    $service_charge = floatval($r1[0]['service_charge']) . '%';
                }
                else
                {
                    ob_clean();
                    $message =  Lang('Abnormal business');
                    return output(400,$message);
                }
                // 查询用户银行卡信息
                $rr = BankCardModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'recycle'=>0])
                                   ->field('id,Cardholder,Bank_name,Bank_card_number,branch,is_default')
                                   ->select()
                                   ->toArray();
                $rew = '';
                if ($rr)
                {
                    foreach ($rr as $key => $value) 
                    {
                        $rr[$key]['Bank_card_number'] = $value['Bank_name'] .' 尾号 ('. substr($value['Bank_card_number'], -4).')';
                    }
                    $bank_information = $rr;
                }
                else
                {
                    $bank_information = array();
                }
                $pshd = '请输入提现金额(大于' . $min_charge . '小于' . $max_charge . ')';
                $data = array('min_amount' => $min_charge, 'max_amount' => $max_charge, 'money' => floatval($account_money), 'pshd' => $pshd, 'unit' => '元', 'bank_information' => $bank_information, 'mobile' => $tel, 'service_charge' => $service_charge,'total'=>$cashable_money,'serviceCharge'=>$serviceCharge,'wx_name'=>$wx_name,'wx_open'=>$wx_open,'wx_withdraw'=>$wx_withdraw);
                return $data;
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
    }

    // 验证卡号与银行名是否匹配
    public function Verification()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
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
        require_once('bankList.php');
        $r = $this->bankInfo($Bank_card_number, $bankList);
        if ($r == '')
        {
            ob_clean();
            $message =  Lang('user.45');
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
                    $message =  Lang('user.46');
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

    // 获取区号
    public function getItuList()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id'));

        // 接收信息
        $keyword = trim(Request::param('keyword'));//区号

        $data = array();

        $con = " is_show = 1 ";
        if($keyword != '')
        {
            $con .= " and (name like '%$keyword%' or code2 like '%$keyword%') ";
        }
        $sql0 = "select * from lkt_ds_country where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $data = $r0;
        }
        $message =  Lang('Success');
        return output(200,$message,$data);
    }

    // 发送邮箱验证码
    public function send_email_verification_code()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id'));

        // 接收信息
        $email = trim(Request::param('email')); // 邮箱

        $array = array('store_id'=>$store_id,'email'=>$email);
        $EmailController = new EmailController();
        $res = $EmailController->sendEmail($array);
    }

    // 短信验证码
    public function secret_key()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id'));

        // 接收信息
        $cpc = trim(Request::param('cpc'));//区号
        $mobile = trim(Request::param('phone')); // 手机号码
        $message_type = trim(Request::param('message_type'))?trim(Request::param('message_type')):0; // 短信类型
        $message_type1 = trim(Request::param('message_type1'))?trim(Request::param('message_type1')):1; // 短信类别

        $Tools = new Tools($store_id, 1);
        $array_code = array('cpc'=>$cpc,'mobile'=>$mobile,'type'=>$message_type,'type1'=>$message_type1,'bizparams'=>array());
        $res = $Tools->generate_code($array_code);
    }

    // 提现申请
    public function withdrawals()
    {
        $store_id = trim(Request::param('store_id'));
        // 接收信息
        $access_id = trim(Request::param('access_id')); // 授权id
        $shop_id = (int)trim(Request::param('shop_id')); // 店铺ID
        $amoney = trim(Request::param('amoney')); // 提现金额
        $withdraw_status = trim(Request::param('withdrawStatus')); // 提现类型 1银行卡 2微信余额
        $bank_id = trim(Request::param('bank_id'));//银行卡ID
        $mobile = trim(Request::param('mobile')); // 手机号码
        $keyCode = trim(Request::param('keyCode')); // 验证码
        
        $Tools = new Tools( $store_id, 1);
        if ($shop_id != 0)
        {
            $res = $this->withdrawals1();
            if($res['code'] != 200)
            {
                return output($res['code'],$res['message']);
            }
        }
        else
        {
            if($withdraw_status == 1)
            {
                $arr = array($mobile, array('store_id'=>$store_id,'access_id'=>$access_id,'code' => $keyCode));
                $rew = $Tools->verification_code( $arr);
            }

            // 查询单位
            $r_1 = FinanceConfigModel::where('store_id',$store_id)->select()->toArray();
            $min_amount = $r_1[0]['min_amount']; // 最小提现金额
            $max_amount = $r_1[0]['max_amount']; // 最大提现金额
            $tax = $r_1[0]['service_charge']; // 设置的手续费参数

            // 提现金额不为数字
            if (is_numeric($amoney) == false)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '提现金额不为数字!';
                $this->Log($Log_content);
                ob_clean();
                $message =  Lang('user.47');
                return output(400,$message);
            }

            // 根据微信id,查询会员金额
            $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
            if ($r)
            {
                $user_id = $r[0]['user_id']; // 会员id
                $user_name = $r[0]['user_name']; // 会员昵称
                $money = $r[0]['money']; // 会员金额

                // 提现金额是否小于等于0,或者大于现有金额
                if ($amoney > $money || $amoney <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '提现金额是否小于等于0,或者大于现有金额!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('user.48');
                    return output(400,$message);
                }
                // 提现金额小于最小提现金额
                if ($amoney < $min_amount)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '提现金额小于最小提现金额!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('user.43');
                    return output(400,$message);
                }
                // 提现金额大于最大提现金额
                if ($amoney > $max_amount)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '提现金额大于最大提现金额!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('user.44');
                    return output(400,$message);
                }
                $cost = round(($amoney * $tax / 100),2);  // 实际的手续费
                $t_money = $amoney; // 提现金额
                // 根据用户id和未核审,查询数据
                $count = WithdrawModel::where(['store_id'=>$store_id,'status'=>0,'user_id'=>$user_id,'is_mch'=>0])->count();
                if ($count > 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '申请提现时，已存在未审核的记录!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('user.49');
                    return output(400,$message);
                }
                else
                {   
                    $now = date("Y-m-d 00:00:00");
                    $res_p = WithdrawModel::where(['store_id'=>$store_id,'status'=>1,'user_id'=>$user_id,'is_mch'=>0])->where('add_date','>=',$now)->count();
                    if($res_p >0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '今日已提现一次，禁止再提!';
                        $this->Log($Log_content);
                        ob_clean();
                        $message =  Lang('user.55');
                        return output(400,$message);
                    }
                    Db::startTrans();
                    $sql = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->find();
                    $sql->money = Db::raw('money - '.$t_money);
                    $res = $sql->save();
                    if (!$res)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '修改用户金额失败！user_id:' . $user_id;
                        $this->Log($Log_content);
                        Db::rollback();
                        ob_clean();
                        $message =  Lang('Abnormal business');
                        return output(400,$message);
                    }

                    $withdrawal_method = '';
                    if($withdraw_status == 2)
                    {
                        $withdrawal_method = "微信零钱";
                    }
                    else
                    {
                        $sql_b = "select Bank_name,Bank_card_number from lkt_bank_card where store_id = '$store_id' and id = '$bank_id' ";
                        $r_b = Db::query($sql_b);
                        if($r_b)
                        {
                            $withdrawal_method = $r_b[0]['Bank_name'] .' 尾号 ('. substr($r_b[0]['Bank_card_number'], -4).')';
                        }
                    }

                    $txsno = $Tools->Generate_order_number('USE', 'txsno'); // 生成订单号

                    //添加申请
                    $sql = new WithdrawModel();
                    $sql->store_id = $store_id;
                    $sql->user_id = $user_id;
                    $sql->name = $user_name;
                    $sql->mobile = $mobile;
                    $sql->Bank_id = $bank_id;
                    $sql->money = $t_money;
                    $sql->z_money = $money;
                    $sql->s_charge = $cost;
                    $sql->txsno = $txsno;
                    $sql->status = 0;
                    $sql->add_date = date("Y-m-d H:i:s");
                    $sql->withdraw_status = $withdraw_status;
                    $sql->save();
                    $r = $sql->id;
                    if ($r > 0)
                    {
                        $withdrawal_fees = number_format($cost,2) . '(' . number_format($tax,2) . '%)';
                        $array = array('store_id'=>$store_id,'money'=>$t_money,'user_money'=>$money,'type'=>11,'money_type'=>2,'money_type_name'=>7,'record_notes'=>'','type_name'=>'','s_no'=>$txsno,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>$withdrawal_fees,'withdrawal_method'=>$withdrawal_method);
                        $details_id = PC_Tools::add_Balance_details($array);

                        $event = $user_id . '申请提现' . $t_money . '元余额';
                        $sql1 = new RecordModel();
                        $sql1->store_id = $store_id;
                        $sql1->user_id = $user_id;
                        $sql1->money = $t_money;
                        $sql1->oldmoney = $money;
                        $sql1->event = $event;
                        $sql1->details_id = $details_id;
                        $sql1->type = 2;
                        $sql1->save();
                        if($withdraw_status == 1)
                        {
                            if ($rew)
                            {
                                SessionIdModel::where('id',$rew)->delete();
                            }
                        }

                        $message_19 = "ID为" . $user_id . "的用户申请提取余额，请及时处理！";
                        $message_logging_list19 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>0,'type'=>19,'parameter'=>$r,'content'=>$message_19);
                        PC_Tools::add_message_logging($message_logging_list19);

                        $Log_content = __METHOD__ . '->' . __LINE__ . '提现成功！';
                        $this->Log($Log_content);
                        Db::commit();
                        ob_clean();
                        $message =  Lang('Success');
                        return output(200,$message);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '添加提现记录失败！user_id:' . $user_id;
                        $this->Log($Log_content);
                        Db::rollback();
                        ob_clean();
                        $message =  Lang('Abnormal business');
                        return output(400,$message);
                    }
                }
            }
            else
            {
                ob_clean();
                $message =  Lang('Parameter error');
                return output(400,$message);
            }
        }
        $message =  Lang('Success');
        return output(200,$message);
    }

    // 申请提现
    public function withdrawals1()
    {
        $store_id = trim(Request::param('store_id'));
        $shop_id = trim(Request::param('shop_id')); // 店铺ID
        $access_id = trim(Request::param('access_id')); // 授权id
        $amoney = trim(Request::param('amoney')); // 提现金额
        $withdraw_status = trim(Request::param('withdrawStatus')); // 提现类型 1银行卡 2微信余额
        $bank_id = trim(Request::param('bank_id'));//银行卡ID
        $mobile = trim(Request::param('mobile')); // 手机号码
        $keyCode = trim(Request::param('keyCode')); // 验证码
        $id = trim(Request::param('id')); // 提现ID

        $Tools = new Tools( $store_id, 1);
        if($withdraw_status == 1)
        {
            $arr = array($mobile, array('store_id'=>$store_id,'code' => $keyCode));
            $rew = $Tools->verification_code( $arr);
        }
        else
        {
            $bank_id = 0;
        }
        
        // 根据微信id,查询会员金额
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,user_name')->select()->toArray();
        if ($r)
        {
            $user_id = $r[0]['user_id'];
            $user_name = $r[0]['user_name'];
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            $data = array('code'=>400,'message'=>$message);
            return $data;
        }

        // 根据商城id、用户id、审核通过，查询店铺ID
        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->field('id,account_money,tel,cashable_money')->select()->toArray();
        if ($r0)
        {
            $mch_id = $r0[0]['id'];
            $account_money = $r0[0]['account_money']; // 商户余额
            $mobile = $r0[0]['tel']; // 联系电话
            $cashable_money = $r0[0]['cashable_money']; // 商户可提现金额
            if ($mch_id == $shop_id)
            {
                $r1 = MchConfigModel::where('store_id',$store_id)->field('settlement,min_charge,max_charge,service_charge')->select()->toArray();
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
                    $message =  Lang('Unknown error');
                    $data = array('code'=>400,'message'=>$message);
                    return $data;
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
                    $message =  Lang('user.47');
                    $data = array('code'=>400,'message'=>$message);
                    return $data;
                }
                // 提现金额是否小于等于0
                if ($amoney <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '提现金额不能小于等于0！';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('user.42');
                    $data = array('code'=>400,'message'=>$message);
                    return $data;
                }
                // 提现金额是否小于最低提现金额
                if ($amoney < $min_charge)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '提现金额不能小于最低提现金额！';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('user.43');
                    $data = array('code'=>400,'message'=>$message);
                    return $data;
                }
                // 提现金额是否大于最大提现金额
                if ($amoney > $max_charge)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '提现金额不能大于最大提现金额！';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('user.44');
                    $data = array('code'=>400,'message'=>$message);
                    return $data;
                }
                // 提现金额 大于 店主金额
                if ($amoney > $cashable_money)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '提现金额不能大于最大可提现金额！';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('user.50');
                    $data = array('code'=>400,'message'=>$message);
                    return $data;
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
                $count0 = WithdrawModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'is_mch'=>1,'status'=>0])->count();
                
                if ($count0 > 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '您的上笔申请还未审核，请稍后再试！';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('distribution.23');
                    $data = array('code'=>400,'message'=>$message);
                    return $data;
                }

                if (!$id)
                {
                    // 根据用户id和未核审,查询数据
                    $count = WithdrawModel::where(['store_id'=>$store_id,'status'=>1,'user_id'=>$user_id,'is_mch'=>1])
                                        ->where('add_date','>=',$time1)
                                        ->where('add_date','<=',$time2)
                                        ->count();
                    if ($count > 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '店铺' . $mch_id . '提现申请次数已达上限！';
                        $this->Log($Log_content);
                        ob_clean();
                        $message =  Lang('user.51');
                        $data = array('code'=>400,'message'=>$message);
                        return $data;
                    }
                }
                Db::startTrans();

                $sql = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->find();
                // $sql->account_money = Db::raw('account_money - '.$t_money);
                $sql->cashable_money = Db::raw('cashable_money - '.$t_money);
                $sql->save();
                if (!$id)
                { // 不存在
                    // 在提现列表里添加一条数据
                    $sql = new WithdrawModel();
                    $sql->store_id = $store_id;
                    $sql->user_id = $user_id;
                    $sql->name = $user_name;
                    $sql->mobile = $mobile;
                    $sql->Bank_id = $bank_id;
                    $sql->money = $t_money;
                    $sql->z_money = $account_money;
                    $sql->s_charge = $cost;
                    $sql->status = 0;
                    $sql->add_date = date("Y-m-d H:i:s");
                    $sql->is_mch = 1;
                    $sql->withdraw_status = $withdraw_status;
                    $sql->save();
                    $r = $sql->id;
                    if ($r>0)
                    {
                        $event = '店主' . $user_id . '申请提现' . $t_money . '元余额';
                        $sql1 = new RecordModel();
                        $sql1->store_id = $store_id;
                        $sql1->user_id = $user_id;
                        $sql1->money = $t_money;
                        $sql1->oldmoney = $account_money;
                        $sql1->event = $event;
                        $sql1->type = 2;
                        $sql1->is_mch = 1;
                        $sql1->save();
                        if($withdraw_status == 1)
                        {
                            if ($rew)
                            {
                                SessionIdModel::where('id',$rew)->delete();
                            }
                        }

                        $message_20 = "ID为" . $mch_id . "的店铺申请提取余额，请及时处理！";
                        $message_logging_list20 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>0,'type'=>20,'parameter'=>$r,'content'=>$message_20);
                        PC_Tools::add_message_logging($message_logging_list20);

                        $message_23 = "您的店铺提现申请已提交成功，正在等待管理员审核！";
                        $message_logging_list23 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>23,'parameter'=>$r,'content'=>$message_23);
                        PC_Tools::add_message_logging($message_logging_list23);

                        $Log_content = __METHOD__ . '->' . __LINE__ . '提现成功！';
                        $this->Log($Log_content);
                        Db::commit();
                        ob_clean();
                        $message =  Lang('Success');
                        $data = array('code'=>200,'message'=>$message);
                        return $data;
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '添加提现记录失败！user_id:' . $user_id;
                        $this->Log($Log_content);
                        Db::rollback();
                        ob_clean();
                        $message =  Lang('Abnormal business');
                        $data = array('code'=>400,'message'=>$message);
                        return $data;
                    }
                }
                else
                { // 存在
                    $sql = "update lkt_withdraw set mobile = '$mobile',Bank_id = '$bank_id',money = '$t_money',z_money = '$account_money',s_charge = '$cost',status = 0,add_date = CURRENT_TIMESTAMP,is_mch = 1 where store_id = '$store_id' and id = '$id' ";
                    $r = Db::execute($sql);
                    $sql = WithdrawModel::where(['store_id'=>$store_id,'id'=>$id])->find();
                    $sql->mobile = $mobile;
                    $sql->Bank_id = $bank_id;
                    $sql->money = $t_money;
                    $sql->z_money = $account_money;
                    $sql->s_charge = $cost;
                    $sql->status = 0;
                    $sql->add_date = date("Y-m-d H:i:s");
                    $sql->is_mch = 1;
                    $sql->withdraw_status = $withdraw_status;
                    $r = $sql->save();
                    if ($r)
                    {
                        $event = '店主' . $user_id . '申请提现' . $t_money . '元余额';
                        $sql1 = new RecordModel();
                        $sql1->store_id = $store_id;
                        $sql1->user_id = $user_id;
                        $sql1->money = $t_money;
                        $sql1->oldmoney = $account_money;
                        $sql1->event = $event;
                        $sql1->type = 2;
                        $sql1->is_mch = 1;
                        $sql1->save();
                        if($withdraw_status == 1)
                        {
                            if ($rew)
                            {
                                SessionIdModel::where('id',$rew)->delete();
                            }
                        }

                        $message_20 = "ID为" . $mch_id . "的店铺申请提取余额，请及时处理！";
                        $message_logging_list20 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>0,'type'=>20,'parameter'=>$id,'content'=>$message_20);
                        PC_Tools::add_message_logging($message_logging_list20);

                        $message_23 = "您的店铺提现申请已提交成功，正在等待管理员审核！";
                        $message_logging_list23 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>23,'parameter'=>$r_0,'content'=>$message_23);
                        PC_Tools::add_message_logging($message_logging_list23);

                        $Log_content = __METHOD__ . '->' . __LINE__ . '提现成功！';
                        $this->Log($Log_content);
                        Db::commit();
                        ob_clean();
                        $message =  Lang('Success');
                        $data = array('code'=>200,'message'=>$message);
                        return $data;
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '重新申请提现记录失败！user_id:' . $user_id;
                        $this->Log($Log_content);
                        Db::rollback();
                        ob_clean();
                        $message =  Lang('Abnormal business');
                        $data = array('code'=>400,'message'=>$message);
                        return $data;
                    }
                }
            }
            else
            {
                ob_clean();
                $message =  Lang('Illegal invasion');
                $data = array('code'=>400,'message'=>$message);
                return $data;
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            $data = array('code'=>400,'message'=>$message);
            return $data;
        }
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

    public function set()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id

        // 根据授权id,查询用户id
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('password,mima')->select()->toArray();
        if ($r)
        {
            $password = $r[0]['password'];
            $mima = $r[0]['mima'];
            if ($password != '')
            {
                $password_status = 1;
            }
            else
            {
                $password_status = 0;
            }
            if ($mima != '')
            {
                $mima_status = 1;
            }
            else
            {
                $mima_status = 0;
            }
            ob_clean();
            $message =  Lang('Success');
            return output(200,$message,array('password_status' => $password_status, 'mima_status' => $mima_status));
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }
    }

    // 修改密码
    public function updatepassword()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $o_password = Request::param('password'); // 密码
        $n_password = Request::param('newPwd');//新密码
        $c_password = Request::param('confirm');//确认密码

        // 根据微信id,查询用户id
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,mima')->select()->toArray();
        if (!empty($r))
        {
            $user_id = $r[0]['user_id'];
            $password = $r[0]['mima'];
            if(Tools::unlock_url($password) != $o_password)
            {
                ob_clean();
                $message =  Lang('set.5');
                return output(400,$message);
            }
            if($n_password != $c_password)
            {
                ob_clean();
                $message =  Lang('user.37');
                return output(400,$message);
            }
            if(Tools::unlock_url($password) == $n_password)
            {
                ob_clean();
                $message =  Lang('user.38');
                return output(400,$message);
            }
            $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
            $sql->mima = Tools::lock_url($c_password);
            $r = $sql->save();
            if ($r)
            {

                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改密码成功!';
                $this->Log($Log_content);
                ob_clean();
                $message =  Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改密码失败!';
                $this->Log($Log_content);
                ob_clean();
                $message =  Lang('Busy network');
                return output(400,$message);
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }
        return;
    }

    // 设置密码
    public function set_password()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $password = Tools::lock_url(Request::param('password')); // 密码
        $cpc = trim(Request::param('cpc'));//区号
        $country_num = Request::param('country_num'); // 国家代码
        $tel = Request::param('phone'); // 手机号码
        $keyCode = trim(Request::param('keyCode')); // 验证码

        $arr = array($cpc.$tel, array('store_id'=>$store_id,'code' => $keyCode));
        $Tools = new Tools( $store_id, 1);
        $rew = $Tools->verification_code( $arr);
        Db::startTrans();
        // 根据授权id,查询用户id
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if ($r)
        {
            $user_id = $r[0]['user_id'];
            $sql = "update lkt_user set mima = '$password' where store_id = '$store_id' and access_id = '$access_id'";
            $rr = Db::execute($sql);
            $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
            $sql->mima = $password;
            $r = $sql->save();
            if ($r)
            {
                if ($rew)
                {
                    SessionIdModel::where('id',$rew)->delete();
                }
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改密码成功!';
                $this->Log($Log_content);
                Db::commit();
                ob_clean();
                $message =  Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '设置密码失败!';
                $this->Log($Log_content);
                Db::rollback();
                ob_clean();
                $message =  Lang('Busy network');
                return output(400,$message);
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }
    }

    // 修改手机号
    public function update_phone()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $y_tel = Request::param('y_phone'); // 旧手机号码
        $cpc = trim(Request::param('cpc'));//区号
        $country_num = Request::param('country_num'); // 国家代码
        $x_tel = Request::param('x_phone'); // 新手机号码
        $keyCode = trim(Request::param('keyCode')); // 验证码
        $time = date("Y-m-s H:i:s");

        $arr = array($cpc.$x_tel, array('store_id'=>$store_id,'code' => $keyCode));
        $Tools = new Tools( $store_id, 1);
        $rew = $Tools->verification_code( $arr);
        Db::startTrans();

        // 根据授权id,查询用户id
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,zhanghao,mobile')->select()->toArray();
        if ($r)
        {
            $user_id = $r[0]['user_id']; // 账号
            $zhanghao = $r[0]['zhanghao']; // 账号
            $mobile = $r[0]['mobile']; // 手机号
            if (empty($mobile) || $mobile == $y_tel)
            { // 没有设置手机号 或者 修改手机号
                $x_zhanghao = $cpc . $x_tel;
                $sql_1 = "select * from lkt_user where store_id = '$store_id' and (zhanghao = '$x_zhanghao' or (cpc = '$cpc' and country_num = '$country_num' and mobile = '$x_tel' )) ";
                $r_1 = Db::query($sql_1);
                if ($r_1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改手机号时，新手机号已存在！';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('user.39');
                    return output(400,$message);
                }
                else
                {   
                    $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                    $sql->cpc = $cpc;
                    $sql->country_num = $country_num;
                    $sql->mobile = $x_tel;
                    if(!empty($mobile) && $zhanghao == $x_zhanghao)
                    { // 原来有手机号 并且 账号和手机号一致
                        $sql->zhanghao = $x_zhanghao;
                    }
                    $rr = $sql->save();
                    if ($rr)
                    {
                        if ($rew)
                        {
                            SessionIdModel::where('id',$rew)->delete();
                        }
                        $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改手机号成功!';
                        $this->Log($Log_content);
                        Db::commit();
                        ob_clean();
                        $message =  Lang('Success');
                        return output(200,$message);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改手机号失败!';
                        $this->Log($Log_content);
                        Db::rollback();
                        ob_clean();
                        $message = Lang('Busy network');
                        return output(400,$message);
                    }
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改手机号时，旧号码错误！';
                $this->Log($Log_content);
                ob_clean();
                $message = Lang('user.52');
                return output(400,$message);
            }
        }
        else
        {
            ob_clean();
            $message = Lang('Illegal invasion');
            return output(400,$message);
        }
    }

    //授权修改手机号
    public function bind_phone()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $y_tel = Request::param('y_phone'); // 旧手机号码
        $x_tel = Request::param('x_phone'); // 新手机号码
        $time = date("Y-m-s H:i:s");
        // if (preg_match("/^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$/", $x_tel))
        // {
            // 根据授权id,查询用户id
            $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,zhanghao,mobile')->select()->toArray();
            if ($r)
            {
                $user_id = $r[0]['user_id']; // 账号
                $zhanghao = $r[0]['zhanghao']; // 账号
                $mobile = $r[0]['mobile']; // 手机号
                $r_1 = UserModel::where('store_id',$store_id)
                                    ->where('zhanghao|mobile','=',$x_tel)
                                    ->select()
                                    ->toArray();
                if ($r_1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改手机号时，新手机号已存在！';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('user.39');
                    return output(400,$message);
                }
                else
                {
                    $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                    $sql->mobile = $x_tel;
                    if($zhanghao == $mobile)
                    {   // 当 手机号码跟账号相同时
                        // 修改账号和手机号
                        $sql->zhanghao = $x_tel;
                    }
                    $rr = $sql->save();
                    if ($rr)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改手机号成功!';
                        $this->Log($Log_content);
                        ob_clean();
                        $message =  Lang('Success');
                        return output(200,$message);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改手机号失败!';
                        $this->Log($Log_content);
                        ob_clean();
                        $message =  Lang('Busy network');
                        return output(400,$message);
                    }
                }
            }
            else
            {
                ob_clean();
                $message =  Lang('Illegal invasion');
                return output(400,$message);
            }
        // }
        // else
        // {
        //     ob_clean();
        //     $message =  Lang('Wrong_cell_phone_number');
        //     return output(400,$message);
        // }
    }

    // 修改用户信息
    public function set_user()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $Nickname = addslashes(trim(Request::param('Nickname'))); // 昵称
        $src_img = Request::param('src_img'); // 图片base64
        $store_type = Request::param('store_type');
        $birthday = Request::param('birthday'); // 出生日期
        $sex = Request::param('sex');//性别
        $e_mail = Request::param('e_mail');//邮箱
        // 根据授权id,查询用户id
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('zhanghao,user_id')->select()->toArray();
        if ($r)
        {
            $user_id = $r[0]['user_id'];
            if (!empty($src_img))
            {
                // 查询配置表信息
                $r = ConfigModel::where('store_id',$store_id)->select()->toArray();
                $upserver = !empty($r) ? $r[0]['upserver'] : '2';   //如果没有设置配置则默认用阿里云
                $headimgurl = ServerPath::file_OSSupload($store_id, $store_type, true);
                if ($headimgurl == false)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改用户信息时，上传失败或图片格式错误！';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('product.1');
                    return output(400,$message);
                }
                $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                $sql->headimgurl = $headimgurl;
                $r = $sql->save();
                if ($r)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改用户头像信息成功!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Success');
                    return output(200,$message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改用户头像信息失败!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Busy network');
                    return output(400,$message);
                }
            }
            else if (!empty($Nickname))
            {
                $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                $sql->user_name = $Nickname;
                $r = $sql->save();
                if ($r > 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改用户昵称信息成功!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Success');
                    return output(200,$message,array('Nickname' => $Nickname));
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改用户昵称信息失败!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Busy network');
                    return output(400,$message);
                }
            }
            else if (!empty($birthday))
            {
                $res1 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('birthday,is_default_birthday')->select()->toArray();
                if ($res1)
                {
                    if($res1[0]['is_default_birthday'] == 2)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '你已设置过生日，不可更改!';
                        $this->Log($Log_content);
                        ob_clean();
                        $message =  Lang('user.53');
                        return output(400,$message);
                    }
                    // if ($res1[0]['birthday'] != "0000-00-00 00:00:00" && $res1[0]['birthday'] != NULL)
                    // {
                    //     $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '你已设置过生日，不可更改!';
                    //     $this->Log($Log_content);
                    //     ob_clean();
                    //     $message =  Lang('user.53');
                    //     return output(400,$message);
                    // }
                }
                $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                $sql->birthday = $birthday;
                $sql->is_default_birthday = 2;
                $r = $sql->save();
                if ($r)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改用户生日信息成功!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Success');
                    return output(200,$message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改用户生日信息失败!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Modification failed');
                    return output(400,$message);
                }
            }
            elseif (!empty($sex))
            {
                $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                $sql->sex = $sex;
                $r = $sql->save();
                if ($r > 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改用户性别信息成功!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Success');
                    return output(200,$message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改用户性别信息失败!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Busy network');
                    return output(400,$message);
                }
            }
            elseif (!empty($e_mail))
            {
                $res1 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('e_mail')->select()->toArray();
                if($res1)
                {
                    if($res1[0]['e_mail'] == $e_mail)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '该邮箱已被使用!';
                        $this->Log($Log_content);
                        ob_clean();
                        $message =  Lang('login.24');
                        return output(400,$message);
                    }
                }
                $sql_e = "select id from lkt_user where store_id = '$store_id' and (zhanghao = '$e_mail' or e_mail = '$e_mail') ";
                $r_e = Db::query($sql_e);
                if($r_e)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '该邮箱已被使用!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('login.23');
                    return output(400,$message);
                }

                $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                $sql->e_mail = $e_mail;
                $r = $sql->save();
                if ($r > 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改用户邮箱信息成功!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Success');
                    return output(200,$message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改用户邮箱信息失败!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Busy network');
                    return output(400,$message);
                }
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }
    }

    // 设置支付密码
    public function set_payment_password()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $password = MD5(Request::param('password')); // 密码

        // 根据授权id,查询用户id
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if ($r)
        {
            $user_id = $r[0]['user_id'];
            $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
            $sql->password = $password;
            $rr = $sql->save();
            if ($rr)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '设置支付密码成功!';
                $this->Log($Log_content);
                ob_clean();
                $message =  Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '设置支付密码失败!';
                $this->Log($Log_content);
                ob_clean();
                $message =  Lang('Busy network');
                return output(400,$message);
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }
    }

    // 修改支付密码
    public function modify_payment_password()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $x_password = MD5(Request::param('x_password')); // 新密码
        $tel = Request::param('phoneNum'); // 手机号码
        $keyCode = trim(Request::param('keyCode')); // 验证码
        $time = date("Y-m-s H:i:s");

        $arr = array($tel, array('store_id'=>$store_id,'code' => $keyCode));
        $Tools = new Tools( $store_id, 1);
        $rew = $Tools->verification_code( $arr);
        // 根据微信id,查询用户id
        $re = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,mobile,password')->select()->toArray();
        if (!empty($re))
        {
            $user_id = $re[0]['user_id'];
            $mobile = $re[0]['mobile'];
            $password = $re[0]['password'];
            if ($mobile == $tel)
            {
                if ($x_password == $password)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改支付密码时，新密码与旧密码相同!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('user.38');
                    return output(400,$message);
                }
                else
                {
                    $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                    $sql->password = $x_password;
                    $rr = $sql->save();
                    if ($rr)
                    {
                        if ($rew)
                        {
                            SessionIdModel::where('id',$rew)->delete();
                        }
                        $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改支付密码成功!';
                        $this->Log($Log_content);
                        ob_clean();
                        $message =  Lang('Success');
                        return output(200,$message);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改支付密码失败!';
                        $this->Log($Log_content);
                        ob_clean();
                        $message =  Lang('Busy network');
                        return output(400,$message);
                    }
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改支付密码时，输入的手机号与绑定的手机号不相同!';
                $this->Log($Log_content);
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
    }

    // 验证支付密码
    public function payment_password()
    {
        $store_id = trim(Request::param('store_id'));
        $Language = trim(Request::param('Language')); // 语言
        $access_id = trim(Request::param('access_id')); // 授权id
        $password1 = MD5(Request::param('password')); // 密码
        $time = date('Y-m-d H:i:s', time());

        // 根据授权id,查询用户id
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,password,login_num')->select()->toArray();
        if ($r)
        {
            $user_id = $r[0]['user_id'];
            $password = $r[0]['password'];
            $login_num = $r[0]['login_num'];
            if ($password1 == $password)
            {
                $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                $sql->login_num = 0;
                $sql->save();
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '输入支付密码时，验证成功!';
                $this->Log($Log_content);
                ob_clean();
                $message =  Lang('Success');
                return output(200,$message,true);
            }
            else
            {
                if ($login_num < 4)
                {
                    $login_num = $login_num + 1;
                    $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                    $sql->login_num = $login_num;
                    $sql->save();
                    $enterless = true;
                }
                else
                {
                    $sql = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                    $sql->login_num = 5;
                    $sql->verification_time = $time;
                    $sql->save();
                    $enterless = false;
                }
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '输入支付密码时，密码错误!';
                $this->Log($Log_content);
                ob_clean();
                $message =  Lang('Password error');
                return output(400,$message,array('enterless' => $enterless));
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Illegal invasion');
            return output(400,$message);
        }
    }

    public function base64_image_contents($base64_image_content, $path, $imgname)
    {
        if (preg_match('/^(data:\s*image\/(\w+);base64,)/', $base64_image_content, $result))
        {
            $type = $result[2];
            $new_file = $path . "/";
            if (!file_exists($new_file))
            {
                mkdir($new_file, 0700);
            }
            $new_file = $path . '/' . $imgname . ".{$type}";
            $storage_path = $imgname . ".{$type}";
            if (file_put_contents($new_file, base64_decode(str_replace($result[1], '', $base64_image_content))))
            {
                return $storage_path;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public function about_us()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $aboutus = '';
        $r = ConfigModel::where('store_id',$store_id)->field('aboutus')->select()->toArray();
        if($r)
        {
            $aboutus = $r[0]['aboutus'];
        }
        ob_clean();
        $message =  Lang('Success');
        return output(200,$message,array('aboutus' => $aboutus));
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/user.log",$Log_content);
        return;
    }

    //银行卡添加
    public function add_bank()
    {
        $store_id = trim(Request::param('store_id'));
        $Cardholder = trim(Request::param('Cardholder')); // 开户人
        $Bank_name = trim(Request::param('Bank_name')); // 银行名称
        $branch = trim(Request::param('branch')); // 支行名称
        $Bank_card_number = trim(Request::param('Bank_card_number')); // 卡号
        $access_id = trim(Request::param('access_id')); // 授权id
        $is_default = (int)trim(Request::param('is_default'));//是否默认
        $id = trim(Request::param('id'));//银行卡id
        // 根据微信id,查询会员信息
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r)
        {
            $user_id = $r[0]['user_id'];
            $mobile = $r[0]['mobile'];
            // if (is_numeric($Bank_card_number) == false)
            // {
            //     $message =  Lang('finance.2');
            //     return output(400,$message);
            // }
            // else
            // {
            //     if (strlen($Bank_card_number) != 19 && strlen($Bank_card_number) != 16)
            //     {
            //         $message =  Lang('finance.3');
            //         return output(400,$message);
            //     }
                
            // }
            // // 根据卡号,查询银行名称
            // require_once('bankList.php');
            // $r = $this->bankInfo($Bank_card_number, $bankList);
            // if ($r == '')
            // {
            //     $message =  Lang('finance.3');
            //     return output(400,$message);
            // }
            // else
            // {
            //     $name = strstr($r, '银行', true) . "银行";
            //     if ($name != $Bank_name)
            //     {
            //         $message =  Lang('finance.5');
            //         return output(400,$message);
            //     }
            // }
            if($Cardholder == '')
            {
                $message =  Lang('finance.1');
                return output(400,$message);
            }
            if($branch == '')
            {
                $message =  Lang('finance.6');
                return output(400,$message);
            }
            if($id)
            {   
                $res_n = BankCardModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recycle'=>0])->count();
                if($res_n > 1)
                {   
                    if($is_default == 1)
                    {
                        //清空默认数据
                        $sql_u = "update lkt_bank_card set is_default = 0 where store_id = '$store_id' and user_id = '$user_id' and recycle = 0 ";
                        $res_u = Db::execute($sql_u);
                    }
                    else
                    {
                        //判断原数据是不是默认数据
                        $sql_b = BankCardModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recycle'=>0,'id'=>$id,'is_default'=>1])->select()->toArray();
                        if($sql_b)
                        {
                            //默认一条数据
                            $sql_u = "update lkt_bank_card set is_default = 1 where store_id = '$store_id' and user_id = '$user_id' and recycle = 0 and id != '$id' order by id asc limit 1";
                            $res_u = Db::execute($sql_u);
                        }
                    }
                }   
                else
                {
                    $is_default = 1;
                }
                $sql = BankCardModel::find($id);
                $sql->Cardholder = $Cardholder;
                $sql->Bank_name = $Bank_name;
                $sql->branch = $branch;
                $sql->Bank_card_number = $Bank_card_number;
                $sql->is_default = $is_default;
                $res = $sql->save();
                if(!$res)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '修改银行卡失败！user_id:' . $user_id;
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Modification failed');
                    return output(400,$message);
                }
            }
            else
            {   
                //查询是否重复添加
                $res_b = BankCardModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'Bank_card_number'=>$Bank_card_number,'recycle'=>0])->field('id')->select()->toArray();
                if ($res_b)
                {
                    $message =  Lang('finance.4');
                    return output(400,$message);
                }
                
                $res_n = BankCardModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recycle'=>0])->count();
                if($res_n < 1)
                {   
                    $is_default = 1;
                }   
                $sql = new BankCardModel();
                $sql->store_id = $store_id;
                $sql->user_id = $user_id;
                $sql->Cardholder = $Cardholder;
                $sql->Bank_name = $Bank_name;
                $sql->branch = $branch;
                $sql->Bank_card_number = $Bank_card_number;
                $sql->is_default = $is_default;
                $sql->save();
                $res = $sql->id;
                if($res < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '添加银行卡失败！user_id:' . $user_id;
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('Modification failed');
                    return output(400,$message);
                }
            }
            
            $message =  Lang('Success');
            return output(200,$message);
        }
        else
        {
            ob_clean();
            $message =  Lang('Parameter error');
            return output(400,$message);
        }
    }

    //银行卡列表
    public function bank_list()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        // 根据微信id,查询会员信息
        $r =  UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r)
        {
            $user_id = $r[0]['user_id'];

            // $APP_INDEX_KEY = LAIKE_REDIS_PRE_KEY.__CLASS__.'_'.__METHOD__.'_'.$user_id;
            // $redis_data = cache($APP_INDEX_KEY);
            // LaiKeLogUtils::log("common/a_redis.log",$redis_data);
            // if($redis_data)
            // {
            //     $message = Lang( 'Success');
            //     return output(200,$message,json_decode($redis_data));
            // }
            // else
            // {
                $list = array();
                $res_b = BankCardModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recycle'=>0])->select()->toArray();
                if($res_b)
                {
                    foreach ($res_b as $key => $value) 
                    {
                        $list[$key]['id'] = $value['id'];
                        $list[$key]['Bank_name'] = $value['Bank_name'];
                        $list[$key]['Bank_card_number'] = substr($value['Bank_card_number'], -4);
                        $list[$key]['branchName'] = $value['branch'];
                    }
                    // cache($APP_INDEX_KEY,json_encode($list),10);
                }
                ob_clean();
                return output(200,'',$list);
            // }
        }
        else
        {
            ob_clean();
            $message =  Lang('Parameter error');
            return output(400,$message);
        }
    }

    //银行卡详情
    public function getBankDetail()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $id = trim(Request::param('bankId'));
        // 根据微信id,查询会员信息
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r)
        {
            $user_id = $r[0]['user_id'];
            $res_b = BankCardModel::where(['store_id'=>$store_id,'recycle'=>0,'id'=>$id])->select()->toArray();
            if($res_b)
            {
                if ($user_id != $res_b[0]['user_id'])
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '持卡人信息错误!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('bank.1');
                    return output(400,$message);
                }
                $message =  Lang('Success');
                return output(200,$message,array('bankName'=>$res_b[0]['Bank_name'],'branchName'=>$res_b[0]['branch'],'cardNo'=>$res_b[0]['Bank_card_number'],'cardholder'=>$res_b[0]['Cardholder'],'isDefault'=>$res_b[0]['is_default']));
            }
            else
            {
                ob_clean();
                $message =  Lang('Parameter error');
                return output(400,$message);
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Parameter error');
            return output(400,$message);
        }
    }

    //解除绑定
    public function del_bank()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $id = trim(Request::param('id'));
        // 根据微信id,查询会员信息
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r)
        {
            $user_id = $r[0]['user_id'];
            $res_b = BankCardModel::where(['store_id'=>$store_id,'recycle'=>0,'id'=>$id])->select()->toArray();
            if($res_b)
            {
                if ($user_id != $res_b[0]['user_id'])
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '持卡人信息错误!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('bank.1');
                    return output(400,$message);
                }
                $sql = "update lkt_bank_card set recycle = 1 where store_id = '$store_id' and id = '$id'";
                $res = Db::execute($sql);
                $sql = BankCardModel::where(['store_id'=>$store_id,'id'=>$id])->find();
                $sql->recycle = 1;
                $res = $sql->save();
                if(!$res)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '解绑银行卡失败';
                    $this->Log($Log_content);
                    ob_clean();
                    $message =  Lang('bank.2');
                    return output(400,$message);
                }
                else
                {
                    $message =  Lang('Success');
                    return output(200,$message);
                }
            }
            else
            {
                ob_clean();
                $message =  Lang('Parameter error');
                return output(400,$message);
            }
        }
        else
        {
            ob_clean();
            $message =  Lang('Parameter error');
            return output(400,$message);
        }
    }

    //同步账号
    public function synchronizeAccount()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type'));
        // 获取信息
        $access_id = trim(Request::param('access_id')); // 授权id
        $mobile = trim(Request::param('mobile'));//电话

        // 查询会员信息
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r)
        {
            $user_name = $r[0]['user_name'];//用户昵称
            $wx_id = $r[0]['wx_id'];//授权id
            $wx_name = $r[0]['wx_name'];//微信昵称
            $headimgurl = $r[0]['headimgurl'];//微信头像
            $id = $r[0]['id'];//ID
            $sql_q = "select id,wx_id,wx_name,user_name,headimgurl from lkt_user where store_id = '$store_id' and (access_id != '$access_id' or access_id is null) and mobile = '$mobile' and is_lock = 0";
            $res_q = Db::query($sql_q);
            if($res_q[0]['wx_id'] == '')
            {
                Db::startTrans();

                $u_id = $res_q[0]['id'];
                //更新数据
                $sql_u = UserModel::where(['store_id'=>$store_id,'id'=>$u_id])->find();
                $sql_u->user_name = $user_name;
                $sql_u->wx_id = $wx_id;
                $sql_u->wx_name = $wx_name;
                $sql_u->headimgurl = $headimgurl;
                $sql_u->access_id = $access_id;
                $res_u = $sql_u->save();
                if(!$res_u)
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '更新用户信息失败！id:' . $u_id;
                    $this->Log($Log_content);
                    $message =  Lang('operation_failed');
                    return output(400,$message);
                }
                //更新数据
                $sql0 = UserModel::find($id);
                $res0 = $sql0->delete();
                if(!$res0)
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '删除用户信息失败！id:' . $id;
                    $this->Log($Log_content);
                    $message =  Lang('operation failed');
                    return output(400,$message);
                }
                Db::commit();
                $message =  Lang('Success');
                return output(200,$message);
            }
            elseif($wx_id == '')
            {
                Db::startTrans();

                $u_id = $res_q[0]['id'];
                $user_name = $res_q[0]['user_name'];//用户昵称
                $wx_id = $res_q[0]['wx_id'];//授权id
                $wx_name = $res_q[0]['wx_name'];//微信昵称
                $headimgurl = $res_q[0]['headimgurl'];//微信头像
                //更新数据
                $sql_u = UserModel::where(['store_id'=>$store_id,'id'=>$id])->find();
                $sql_u->user_name = $user_name;
                $sql_u->wx_id = $wx_id;
                $sql_u->wx_name = $wx_name;
                $sql_u->headimgurl = $headimgurl;
                $sql_u->access_id = $access_id;
                $res_u = $sql_u->save();
                if(!$res_u)
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '更新用户信息失败！id:' . $id;
                    $this->Log($Log_content);
                    $message =  Lang('operation failed');
                    return output(400,$message);
                }
                //删除数据
                $sql0 = UserModel::find($u_id);
                $res0 = $sql0->delete();
                if(!$res0)
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . '删除用户信息失败！id:' . $u_id;
                    $this->Log($Log_content);
                    $message =  Lang('operation failed');
                    return output(400,$message);
                }
                Db::commit();
                $message =  Lang('Success');
                return output(200,$message);
            }
        }
    }
    
    // 获取用户信息
    // module:app
    // action:user
    // app:get_user_information
    // store_id:1
    // Language:''  // 语言
    // access_id:'' // 授权id
    // 获取用户信息
    public function get_user_information()
    {
        $store_id = trim(Request::param('store_id'));
        $Language = trim(Request::param('Language')); // 语言
        $access_id = trim(Request::param('access_id')); // 授权id
        $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('zhanghao,mima')->select()->toArray();
        if($r0)
        {
            $zhanghao = $r0[0]['zhanghao'];
            $password = md5($r0[0]['mima']);
            $data = array('zhanghao' => $zhanghao,'password'=>$password);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 个人中心-推荐商品
    public function myRecommendation()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $language = addslashes(Request::param('language')); // 语言
        $page = trim(Request::param('page')); // 页码

        $lang_code = Tools::get_lang($language);

        $start = 0;
        $end = 10;
        if($page)
        {
            $start = ($page - 1) * $end;
        }

        $list = array();

        // 获取商品设置
        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));
        
        $Member_discount = array('store_id'=>$store_id,'access_id'=>$access_id);
        $grade_list= PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade'];
        $grade_rate = 1;
        
        $str_t = " a.id,a.commodity_type,a.product_title,a.product_class,a.brand_id,a.freight,a.gongyingshang,a.imgurl,a.keyword,a.mch_id,a.num,a.recycle,a.s_type,a.status,a.subtitle,a.volume,c.id as cid,c.img,c.costprice,c.pid,min(c.price) over (partition by c.pid) as price,c.yprice,c.attribute,m.id as mchId,m.logo,m.name as mch_name,m.is_open,a.display_position_sort,a.upper_shelf_time,row_number () over (partition by c.pid) as top,a.is_appointment,a.write_off_settings,a.write_off_mch_ids ";

        $sql_t = "select tt.* from (select $str_t 
                    from lkt_product_list AS a 
                    RIGHT JOIN lkt_configure AS c ON a.id = c.pid 
                    left join lkt_mch as m on a.mch_id = m.id 
                    where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.recycle = 0 and c.recycle = 0 and a.mch_status = 2 and a.show_adr like '%,4,%' and a.mch_id != 0 and a.lang_code = '$lang_code' $status) as tt where tt.top<2   
                    order by tt.display_position_sort desc,tt.upper_shelf_time desc 
                    LIMIT $start,$end";
        $r_t = Db::query($sql_t);
        if ($r_t)
        {
            foreach ($r_t as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl']);
                $v['logo'] = ServerPath::getimgpath($v['logo']);
                $s_type = explode(',', trim($v['s_type'],','));
                $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                $v['s_type_list'] = $s_type_list;

                $v['sizeid'] = $v['cid'];
                $v['contNum'] = $v['num'];
                $v['stockNum'] = $v['num'];
                $v['vip_yprice'] = $v['price'];
                $v['vip_price'] = sprintf("%.2f",round($v['price'] * $grade_rate,2));

                if($v['volume'] < 0)
                {
                    $v['volume'] = 0;
                }

                $pid = $v['id'];
                $r_configure = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->sum('num');
                $v['num'] = $r_configure;

                $v['payPeople'] = 0;
                $sql_3 = "select count(DISTINCT r_sNo) as total from lkt_order_details where r_status != 0 and p_id = '$pid' ";
                $r_3 = Db::query($sql_3);
                if($r_3)
                {
                    $v['payPeople'] = $r_3[0]['total'];
                }

                if($v['write_off_settings'] == 1)
                { // 核销设置 1.线下核销 2.无需核销
                    $v['is_appointment'] = 2; // 2.不能加入购物车
                    $v['isAddCar'] = 2; // 2.不能加入购物车
                }
                else
                {
                    $v['is_appointment'] = 1; // 1.能加入购物车
                    $v['isAddCar'] = 1; // 1.能加入购物车
                }
                $list[] = $v;
            }
        }

        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 修改个人中心弹窗
    public function isDefaultValue()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type'));
        $access_id = trim(Request::param('access_id')); // 授权id

        // 查询会员信息
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r)
        {
            $user_id = $r[0]['user_id'];

            $sql1 = "update lkt_user set is_default_value = 2 where store_id = '$store_id' and user_id = '$user_id' ";
            $r1 = Db::execute($sql1);
        }

        $message = Lang('Success');
        return output(200,$message);
    }

    // 【微信公众号,APP】 用户绑定微信
    public function bindWechat()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $code = addslashes(trim(Request::param('code'))); // code

        // 查询会员信息
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r)
        {
            $user_id = $r[0]['user_id'];
        }

        $appid = '';
        $appsecret = '';
        //获取微信公众号支付的appid,appsecret
        $sql1 = "select config_data from lkt_payment as p left join lkt_payment_config as c on c.pid = p.id where c.status = 1 and p.class_name = 'jsapi_wechat' and  c.store_id = '$store_id' ";           
        $res1 = Db::query($sql1);
        if($res1)
        {
            $r0 = json_decode($res1[0]['config_data'],true);
            $appid = $r0['appid']; // 公众号唯一标识
            $appsecret = $r0['appsecret']; // 公众号的 app secret
        } 
        else
        {
            $message = Lang('login.6');
            echo json_encode(array('code'=>ERROR_CODE_QXSZJCPZ,'message'=>$message));
            exit;
        }

        if (!$appid || !$appsecret)
        {
            $message = Lang('login.6');
            echo json_encode(array('code'=>ERROR_CODE_CSCW,'message'=>$message));
            exit;
        }

        $openid = '';
        $unionid = '';
        $access_token = '';
        $url = 'https://api.weixin.qq.com/sns/oauth2/access_token?appid=' . $appid . '&secret=' . $appsecret . '&code='.$code.'&grant_type=authorization_code';
        $token_res = self::http_post($url);
        $token_res = (array)json_decode($token_res, true);
        LaiKeLogUtils::lktLog("公众号授权返回信息1:" . json_encode($token_res));
        if (isset($token_res['errcode']) || !isset($token_res['unionid']))
        {
            LaiKeLogUtils::lktLog("公众号授权错误信息:");
            LaiKeLogUtils::lktLog("appid:" . $appid . "->appsecret:" . $appsecret);
            LaiKeLogUtils::lktLog("授权url:" . $url);
            LaiKeLogUtils::lktLog(json_encode($token_res));
            $message = Lang('login.6');
            echo json_encode(array('code'=>ERROR_CODE_CSCW,'message'=>$message));
            exit;
        }
        else if (isset($token_res['unionid']))
        {
            $openid = $token_res['openid'];
            $unionid = $token_res['unionid'];
            $access_token = $token_res['access_token'];
        }

        $user_name = '';
        $sex = '';
        $headimgurl = '';
        if ($openid) 
        {
            $url = "https://api.weixin.qq.com/sns/userinfo?access_token=".$access_token."&openid=".$openid."&lang=zh_CN";
            $user_res = self::http_post($url);
            $user = (array)json_decode($user_res, true);
            LaiKeLogUtils::lktLog("公众号授权返回信息2:" . json_encode($user));
            if (isset($user['errcode']) || !isset($user['unionid']))
            {
                LaiKeLogUtils::lktLog("授权错误信息:");
                LaiKeLogUtils::lktLog("openid:" . $openid . "->access_token:" . $access_token);
                LaiKeLogUtils::lktLog("授权url:" . $url);
                LaiKeLogUtils::lktLog(json_encode($user));
                $message = Lang('login.6');
                echo json_encode(array('code'=>ERROR_CODE_CSCW,'message'=>$message));
                exit;
            }
            else if (isset($user['unionid']))
            {
                $unionid = $user['unionid'];
                $user_name = $user['nickname'];
                $sex = $user['sex'];
                $headimgurl = $user['headimgurl'];
            }
        }

        $sql_u = "update lkt_user set wx_name = '$user_name',headimgurl = '$headimgurl',wx_id = '$openid',unionid = '$unionid',sex = '$sex' where store_id = '$store_id' and user_id = '$user_id' ";
        $r_u = Db::execute($sql_u);

        $message = Lang('Success');
        return output(200,$message);
    }

    // 【小程序】用户绑定微信
    public function wxBind()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type'));
        $access_id = trim(Request::param('access_id')); // 授权id

        // $unionid = addslashes(trim(Request::param('unionid'))); // 用户统一标识。针对一个微信开放平台帐号下的应用，同一用户的 unionid 是唯一的
        $openid = addslashes(trim(Request::param('openId'))); // openid
        $user_name = addslashes(trim(Request::param('wxName'))); // 用户昵称
        // $headimgurl = trim(Request::param('headimgurl')); // 用户头像
        // $sex = trim(Request::param('sex')); // 用户性别

        // 查询会员信息
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r)
        {
            $user_id = $r[0]['user_id'];

            // $sql_u = "update lkt_user set wx_name = '$user_name',headimgurl = '$headimgurl',wx_id = '$openid',unionid = '$unionid',sex = '$sex' where store_id = '$store_id' and user_id = '$user_id' ";
            $sql_u = "update lkt_user set wx_name = '$user_name',wx_id = '$openid' where store_id = '$store_id' and user_id = '$user_id' ";
            $r_u = Db::execute($sql_u);
        }

        $message = Lang('Success');
        return output(200,$message);
    }

    // 解除微信绑定
    public function wxUnbind()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $code = addslashes(trim(Request::param('code'))); // code

        // 查询会员信息
        $r = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if($r)
        {
            $user_id = $r[0]['user_id'];

            $sql_u = "update lkt_user set wx_id = '' where store_id = '$store_id' and user_id = '$user_id' ";
            $r_u = Db::execute($sql_u);
        }

        $message = Lang('Success');
        return output(200,$message);
    }
    
    public static function http_post($url)
    {
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_HEADER, 0);
        // 保证返回成功的结果是服务器的结果
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, FALSE);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 10);
        $res = curl_exec($ch);
        curl_close($ch);

        return $res;
    }

    public function getSystemIconAndName()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id

        $r = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if($r)
        {
            $data = array('store_name' => $r[0]['store_name'],'html_icon' => $r[0]['html_icon']);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            ob_clean();
            $message =  Lang('Parameter error');
            return output(400,$message);
        }
    }
}

?>
