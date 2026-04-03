<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cache;
use think\facade\Request;
use app\common\Tools;
use app\common\ServerPath;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;

use app\admin\model\ConfigModel;
use app\admin\model\UserModel;
use app\admin\model\CustomerModel;
use app\admin\model\MchModel;
use app\admin\model\SessionIdModel;
use app\admin\model\UserAuthorityModel;
use app\admin\model\UserRoleModel;

/**
 * 功能：PC店鋪登录类
 * 修改人：PJY
 */
class User 
{
    // 帐密登录
    public function LoginUser()
    {
        $store_id = addslashes(Request::param('storeId'));
        $store_type = addslashes(Request::param('storeType'));
        $name = addslashes(Request::param('login'));
        $password = addslashes(Request::param('pwd'));

        $imgCodeToken = addslashes(Request::param('imgCodeToken'));
        $imgCode = addslashes(Request::param('imgCode'));

        $Jurisdiction = new Jurisdiction();
        $time = date("Y-m-d H:i:s");

        if (!$imgCodeToken || !$imgCode) 
        {
            $message = Lang("mch.21");
            return output(ERROR_CODE_YZMBZQ,$message,null);
        }
        // 获取验证码
        $cacheCode = cache($imgCodeToken);
        if (strtolower($cacheCode) != strtolower($imgCode))
        {   
            $message = Lang("user.26");
            return output(ERROR_CODE_TXYZMBZQ,$message,null);
        }
        $lktlog = new LaiKeLogUtils();
        if ($name == '' || $password == '')
        {   
            $message = Lang("user.22");
            return output(ERROR_CODE_ZHHMMBPP,$message,null);
        }
        $token = "";
        $res = UserModel::where('store_id',$store_id)
                      ->where('zhanghao|mobile|e_mail',$name)
                      ->select()
                      ->toArray();
        if (count($res) <= 0)
        {   
            $message = Lang("user.23");
            return output(ERROR_CODE_ZHBCZ,$message,null);
        }
        else
        {
            // var_dump($res);die;
            $shopMasterInfo = $res[0];
            $id = $shopMasterInfo['id'];
            $userid = $shopMasterInfo['user_id'];
            $preferred_currency = $shopMasterInfo['preferred_currency']; // 用户偏好货币id
            $lang = $shopMasterInfo['lang']; // 语言

            $res = $this->store_status($store_id);

            $role_id = 0;

            // 根据商城ID、用户ID、店铺审核状态通过，查询是否有店铺
            $res_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$userid,'review_status'=>1,'recovery'=>0])->select()->toArray();
            if (count($res_mch) <= 0)
            {  //  子账号登录 
                //判断是否有角色
                $res_role = UserAuthorityModel::where(['main_id'=>$userid,'type'=>1])->select()->toArray();
                if(empty($res_role))
                {
                    $message = Lang("user.25");
                    return output(ERROR_CODE_WKTDPHZDPYZX,$message,null);
                }
                else
                {
                    $role_id = $res_role[0]['role_id'];
                }
                //获取子账号绑定的店铺
                $sql_mch = "select m.* from lkt_mch as m left join lkt_user_role as b on m.id = b.main_id where b.type = 2 and b.id = '$role_id' ";
                $res_mch = Db::query($sql_mch);
            }
            $mch_id = $res_mch[0]['id']; // 店铺ID

            // 根据店铺ID、注销状态，查询数据
            $sql_mch1 = "select * from lkt_mch where id = '$mch_id' and recovery = 1 ";
            $res_mch1 = Db::query($sql_mch1);
            if($res_mch1)
            { // 已经被注销
                $message = Lang("mch.86");
                return output("50080",$message);
            }

            $password01 = Tools::unlock_url($shopMasterInfo['mima']);
            if ($password == $password01 && strlen($password) == strlen($password01))
            {
                $sql_m = "update lkt_mch set last_login_time = '$time' where store_id = '$store_id' and user_id = '$userid' and review_status = 1 and recovery = 0 ";
                $r_m = Db::execute($sql_m);
            }
            else
            {   
                $message = Lang("Password error");
                return output(ERROR_CODE_MMCWQZXSR,$message,null);
            }

            $mchInfo = $res_mch[0];
            //生成session_id
            $access_token = Tools::getToken($store_id,7);

            UserModel::update(['mch_token' => $access_token],['id'=>$id]);

            $exp_time = 7200;
            $res = ConfigModel::where('store_id', $store_id)
                ->field('exp_time')
                ->select()
                ->toArray();
            if($res)
            {
                $exp_time = intval($res[0]['exp_time']* 3600);
            }

            cache($access_token, $mchInfo, $exp_time);//添加新token数据
            cache($access_token.'_7',$mchInfo['id'],$exp_time);
            cache($access_token.'_roleId', $role_id, $exp_time);
            cache($access_token.'_uid', $userid, $exp_time);
            cache($access_token.'_7_operator_id', $id, $exp_time);

            $ip = $this->getClientIp();
            $lktlog->log("common/pc_api.log",__METHOD__ . '->' . __LINE__ . ":" . $ip . "登录成功");
        }

        $Jurisdiction->admin_record($store_id, $userid, ' 管理员:' . $userid . '登陆店铺后台 ',0,2,$mch_id,$id);

        $array = array('store_id'=>$store_id,'store_type'=>7,'read_id'=>$userid);
        $r_system = PC_Tools::GetAnnouncement($array);
        $tell_id = $r_system['tell_id'];
        $systemMsgTitle = $r_system['systemMsgTitle'];
        $systemMsg = $r_system['systemMsg'];
        $systemMsgStartDate = $r_system['systemMsgStartDate'];
        $systemMsgEndDate = $r_system['systemMsgEndDate'];
        $systemMsgType = $r_system['systemMsgType'];

        $storeCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>1,'id'=>0));
        $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>$preferred_currency));

        cache($access_token . '_currency',$preferred_currency); // 添加用户默认币种缓存
        
        //
        $shopMasterInfo['id']= $mch_id;
        $shopMasterInfo['logo']= ServerPath::getimgpath($res_mch[0]['logo']);
        
        $data = array('token'=>$access_token,'info'=>'登录成功','systemMsg'=>$systemMsg,'systemMsgEndDate'=>$systemMsgEndDate,'systemMsgStartDate'=>$systemMsgStartDate,'systemMsgTitle'=>$systemMsgTitle,'systemMsgType'=>$systemMsgType,'tell_id'=>$tell_id,'storeCurrency' => $storeCurrency[0],'userCurrency' => $userCurrency[0],'user_id' => $userid,'preferred_currency'=>$preferred_currency,'lang' => $lang,'res' => $shopMasterInfo);
        $lktlog->log("common/pc_api.log",__METHOD__ . '->' . __LINE__ . ":TOKEN " . $access_token);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 验证码登录
    public function LoginBySms()
    {
        $store_id = addslashes(Request::param('storeId'));
        $tel = addslashes(Request::param('phone'));
        $code = addslashes(Request::param('pcode'));
        $lktlog = new LaiKeLogUtils();
        $time = date("Y-m-d H:i:s");

        $Jurisdiction = new Jurisdiction();
        
        $res = UserModel::where('store_id',$store_id)
                      ->where('mobile',$tel)
                      ->select()
                      ->toArray();
        if($res)
        {
            $id = $res[0]['id'];
            $userid = $res[0]['user_id'];
            $preferred_currency = $res[0]['preferred_currency']; // 用户偏好货币id
            $lang = $res[0]['lang']; // 语言
            $role_id = 0;
            //看是否有开店 且店铺通过申请
            $res_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$userid,'review_status'=>1,'recovery'=>0])->select()->toArray();
            if (count($res_mch) <= 0)
            {   
                //判断是否有角色
                $res_role = UserAuthorityModel::where(['main_id'=>$userid,'type'=>1])->select()->toArray();
                if(empty($res_role))
                {
                    $message = Lang("user.25");
                    return output(ERROR_CODE_WKTDPHZDPYZX,$message);
                }
                else
                {
                    $role_id = $res_role[0]['role_id'];
                }
                //获取子账号绑定的店铺
                $sql_mch = "select m.* from lkt_mch as m left join lkt_user_role as b on m.id = b.main_id where b.type = 2 and b.id = '$role_id' ";
                $res_mch = Dn::query($sql_mch);
            }

            $arr = array($tel, array('store_id'=>$store_id,'code' => $code));
            $Tools = new Tools($store_id, 1);
            $rew = $Tools->verification_code($arr);

            $sql_m = "update lkt_mch set last_login_time = '$time' where store_id = '$store_id' and user_id = '$userid' and review_status = 1 and recovery = 0 ";
            $r_m = Db::execute($sql_m);

            $mch_id = $res_mch[0]['id'];
            $mchInfo = $res_mch[0];
            //生成session_id
            $access_token = Tools::getToken($store_id,8);
            
            UserModel::update(['mch_token' => $access_token],['id'=>$id]);
            
            $exp_time = 7200;
            cache($access_token, $mchInfo, $exp_time);//添加新token数据
            cache($access_token.'_7',$mchInfo['id'],$exp_time);
            cache($access_token.'_roleId', $role_id, $exp_time);
            cache($access_token.'_uid', $userid, $exp_time);
            cache($access_token.'_7_operator_id', $id, $exp_time);
            $ip = $this->getClientIp();
            $lktlog->log("common/pc_api.log",__METHOD__ . '->' . __LINE__ . ":" . $ip . "登录成功");

            if ($rew)
            {
                $sql2 = SessionIdModel::find($rew);
                $r2 = $sql2->delete();
                if(!$r2){
                    $Log_content = __METHOD__ . '->' . __LINE__ . '删除验证码失败!id:' . $rew;
                    $lktlog->log("common/pc_api.log",$Log_content);

                    $message = Lang("Busy network");
                    return output(ERROR_CODE_WLFMQSHZS,$message);
                }
            }

            $Jurisdiction->admin_record($store_id, $userid, ' 管理员:' . $userid . '登陆店铺后台 ',0,2,$mch_id,$id);

            $array = array('store_id'=>$store_id,'store_type'=>7,'read_id'=>$userid);
            $r_system = PC_Tools::GetAnnouncement($array);
            $tell_id = $r_system['tell_id'];
            $systemMsgTitle = $r_system['systemMsgTitle'];
            $systemMsg = $r_system['systemMsg'];
            $systemMsgStartDate = $r_system['systemMsgStartDate'];
            $systemMsgEndDate = $r_system['systemMsgEndDate'];
            $systemMsgType = $r_system['systemMsgType'];

            $storeCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>1,'id'=>0));
            $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>$preferred_currency));

            cache($access_token . '_currency',$preferred_currency); // 添加用户默认币种缓存

            $data = array('token'=>$access_token,'info'=>'登录成功','systemMsg'=>$systemMsg,'systemMsgEndDate'=>$systemMsgEndDate,'systemMsgStartDate'=>$systemMsgStartDate,'systemMsgTitle'=>$systemMsgTitle,'systemMsgType'=>$systemMsgType,'tell_id'=>$tell_id,'storeCurrency' => $storeCurrency[0],'userCurrency' => $userCurrency[0],'user_id' => $userid,'preferred_currency'=>$preferred_currency,'lang' => $lang,'res' => $res[0]);
            $message = Lang("Success");
            return output(200,$message,array('token'=>$access_token));
        }
        else
        {   
            $message = Lang("login.17");
            return output(ERROR_CODE_YHBCZ,$message);  
        }
    }

    //商户状态
    public function store_status($store_id)
    {
        $r0 = CustomerModel::where('id',$store_id)->field('recycle')->select()->toArray();
        $recycle = $r0[0]['recycle'];
        if($recycle == 1)
        {
            $message = Lang('user.24');
            return output(ERROR_CODE_ZHBCZ,$message);
        }

        return;
    }

    public function getClientIp($type = 0, $client = true)
    {
        $type = $type ? 1 : 0;
        static $ip = NULL;
        if ($ip !== NULL) return $ip[$type];
        if ($client)
        {
            if (isset($_SERVER['HTTP_X_FORWARDED_FOR']))
            {
                $arr = explode(',', $_SERVER['HTTP_X_FORWARDED_FOR']);
                $pos = array_search('unknown', $arr);
                if (false !== $pos) unset($arr[$pos]);
                $ip = trim($arr[0]);
            }
            elseif (isset($_SERVER['HTTP_CLIENT_IP']))
            {
                $ip = $_SERVER['HTTP_CLIENT_IP'];
            }
            elseif (isset($_SERVER['REMOTE_ADDR']))
            {
                $ip = $_SERVER['REMOTE_ADDR'];
            }
        }
        elseif (isset($_SERVER['REMOTE_ADDR']))
        {
            $ip = $_SERVER['REMOTE_ADDR'];
        }
        // 防止IP伪造
        $long = sprintf("%u", ip2long($ip));
        $ip = $long ? array($ip, $long) : array('0.0.0.0', 0);
        return $ip[$type];
    }

    //手机号登录短信验证码
    public function SendSms()
    {
        $store_id = addslashes(Request::param('storeId'));
        // 接收信息
        $cpc = trim(Request::param('cpc'));//区号
        $mobile = trim(Request::param('phone')); // 手机号码

        $Tools = new Tools($store_id, 1);
        $array_code = array('cpc'=>$cpc,'mobile'=>$mobile,'type'=>0,'type1'=>0,'bizparams'=>array());
        $res = $Tools->generate_code($array_code);
    }

    //忘记密码短信验证码
    public function SendSmsForgetPwd()
    {
        $store_id = addslashes(Request::param('storeId'));
        // 接收信息
        $cpc = trim(Request::param('cpc'));//区号
        $mobile = trim(Request::param('phone')); // 手机号码

        $Tools = new Tools($store_id, 1);
        $array_code = array('cpc'=>$cpc,'mobile'=>$mobile,'type'=>0,'type1'=>1,'bizparams'=>array());
        $res = $Tools->generate_code($array_code);
    }

    //忘记密码重置密码
    public function ForgetPwd()
    {   
        $store_id = trim(Request::param('storeId'));
        $tel = Request::param('phone'); // 手机号码
        $keyCode = trim(Request::param('pcode')); // 短信验证码
        $password = trim(Request::param('pwd')); // 重置密码
        $r_password = trim(Request::param('rpwd'));//确认密码
        // $imgCode = trim(Request::param('imgCode'));//图形验证码
        // $imgtoken = trim(Request::param('accessId'));//图形验证令牌

        // if($imgCode != cache($imgtoken))
        // {
        //     $message = Lang("user.26");
        //     return output(ERROR_CODE_TXYZMBZQ,$message);
        // }
        // else
        // {
        //     cache($imgtoken,NULL);
        // }
        $re = UserModel::where(['store_id'=>$store_id,'mobile'=>$tel])->select()->toArray();
        if (!empty($re))
        {
            $mima = $re[0]['mima'];
            $arr = array($tel, array('code' => $keyCode));
            $Tools = new Tools($store_id, 1);
            $rew = $Tools->verification_code($arr);
            if ($rew)
            {
                $sql2 = SessionIdModel::find($rew);
                $r2 = $sql2->delete();
                if(!$r2)
                {   
                    $message = Lang("Busy network");
                    return output(ERROR_CODE_WLFM,$message);
                }
            }
            if($password != $r_password)
            {
                $message = Lang("user.27");
                return output(ERROR_CODE_MMBZQ,$message);
            }
            if(strlen($password) < 6 || strlen($password) > 16)
            {   
                $message = Lang("user.0");
                return output(ERROR_CODE_MMBFHGF,$message);
            }
            else
            {   
                $password = Tools::lock_url($password);
            }

            $sql = UserModel::where(['store_id'=>$store_id,'mobile'=>$tel])->find();
            $sql->mima = $password;
            $r = $sql->save();
            if (!$r)
            {
                $message = Lang("Busy network");
                return output(ERROR_CODE_WLFM,$message);
            }
            else
            {
                $message = Lang("Success");
                return output("200",$message);
            }
        }
        else
        {   
            $message = Lang("user.28");
            return output(ERROR_CODE_GSJHMWZC,$message);
        }
    }

    // 公告已读
    public function markToRead()
    {
        $store_id = trim(Request::param('storeId'));
        $store_type = trim(Request::param('storeType')); // 来源
        $access_id = trim(Request::param('accessId')); // 授权id
        $tell_id = trim(Request::param('tell_id')); // 公告ID
        $read_id = cache($access_id.'_uid');
        
        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'read_id'=>$read_id,'tell_id'=>$tell_id);
        PC_Tools::markToRead($array);

        $message = Lang("Success");
        return output(200,$message);
    }

    // 退出
    public function quit()
    {
        $store_id = trim(Request::param('storeId'));
        $store_type = trim(Request::param('storeType')); // 来源
        $access_id = trim(Request::param('accessId')); // 授权id
        
        $user_id = cache($access_id.'_uid');

        $sql0 = "update lkt_user set mch_token = '' where store_id = '$store_id' and user_id = '$user_id' ";
        $r0 = Db::execute($sql0);

        cache($access_id,NULL);

        $message = Lang("Success");
        return output(200,$message);
    }

    // 获取图形验证码
    public function getCode()
    {   
        $data = PC_Tools::getCode();
        $message = Lang("Success");
        return output(200,$message,$data);
    }
}