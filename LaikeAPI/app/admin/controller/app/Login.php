<?php
namespace app\admin\controller\app;

require_once MO_LIB_DIR."/RedisClusters.php";
use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\LKTConfigInfo;
use app\common\Commission;
use app\common\lktpay\ttpay\TTUtils;
use app\common\lktpay\bdpay\BDUtils;
use app\common\alipay\AlipayTools;
use app\common\alipay0\aop\test\TestImage;
use app\common\wxinfo\WXBizDataCrypt;
use app\common\EmailController;

use app\admin\model\ConfigModel;
use app\admin\model\AgreementModel;
use app\admin\model\UserModel;
use app\admin\model\RecordModel;
use app\admin\model\OrderModel;
use app\admin\model\UserDistributionModel;
use app\admin\model\PluginsModel;
use app\admin\model\DistributionConfigModel;
use app\admin\model\CartModel;
use app\admin\model\SessionIdModel;
use app\admin\model\ConfigureModel;


/**
 * 功能：移动端登录类
 * 修改人：PJY
 */
class Login 
{
    // 判断是否要注册
    public function is_register()
    {
        $store_id = Request::param('store_id'); // 商城ID
        $store_type = Request::param('store_type'); // 来源
        // 根据商城id，查询配置表
        $r0 = ConfigModel::where(['store_id'=>$store_id])->field('is_register')->select()->toArray();
        if ($r0)
        {
            $is_register = $r0[0]['is_register'];
            if ($is_register == 2 && $store_type == 1)
            { // 当注册为免注册，并且来源为小程序
                $is_register = 2; // 免注册
            }
            else
            {
                $is_register = 1; // 注册
            }
            $data = array('is_register'=>$is_register);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            $message = Lang('login.0');
            return output(ERROR_CODE_PZBCZ,$message);
        }
    }

    // 授权未过期
    public function login_access()
    {
        $store_id = Request::param('store_id'); // 商城ID
        $store_type = Request::param('store_type'); // 来源

        $unionid = Request::param('unionid');//用户unionid
        $openid = Request::param('openid');//用户openid
        if (!Request::has('unionid') and !Request::has('openid'))
        {
            LaiKeLogUtils::lktLog("unionid异常:" . $unionid);
            $message = Lang('login.4');
            return output(ERROR_CODE_CSCW,$message);
        }
        if ($openid) 
        {
            $unionid = $openid;
        }

        // 根据商城id，openid,查询用户信息
        $r = UserModel::where(['store_id'=>$store_id])
                      ->where('unionid|wx_id',$unionid)
                      ->field('id,user_name,headimgurl,access_id,mima,user_id')
                      ->select()
                      ->toArray();
        if ($r)
        {
            // 存在
            $access_id = $r[0]['access_id'];
            if (!$access_id || !Tools::verifyToken($access_id) || $access_id == 'undefined')
            {   
                cache($access_id, NULL);//清理原token数据

                $now_time = date('Y-m-d H:i:s');
                $UserModel = UserModel::where(['store_id'=>$store_id,'unionid'=>$unionid])->find();
                $UserModel->access_id     = $access_id;
                $UserModel->last_time     = $now_time;
                $UserModel->save();
                // 用户信息存入redis
                //获取用户信息
                $access_id = Tools::getToken($store_id,$store_type);
                $res_a = UserModel::where(['store_id'=>$store_id,'unionid'=>$unionid])
                                  ->select()
                                  ->toArray();
                $res = ConfigModel::where('store_id', $store_id)
                                  ->field('exp_time')
                                  ->select()
                                  ->toArray();
                if($res)
                {
                    $exp_time = intval($res[0]['exp_time']* 3600);
                }
                else
                {
                    $exp_time = 7200;
                }
                cache($access_id, $res_a[0], $exp_time);//添加新token数据
            }
            $user_status = 1; // 老用户
            $user = array();
            $user['user_name'] = $r[0]['user_name'];
            $user['headimgurl'] = $r[0]['headimgurl'];
            $user['user_id'] = $r[0]['user_id'];
            if (empty(($r[0]['mima'])))
            {
                $user['nopswd'] = 1;
            }
            $message = Lang('Success');
            $data = array('unionid' => $unionid, 'user_status' => $user_status, 'user' => $user, 'access_id' => $access_id);
            return output(200,$message,$data);
        }
        else
        {
            LaiKeLogUtils::lktLog("免注册登录流程,不存在此用户:" . $unionid);
            $message = Lang('login.5');
            return output(ERROR_CODE_YHBCZ,$message);
        }
    }

    // H5授权
    public function appletsWx()
    {
        $code = addslashes(trim(Request::param('code'))); // code
        if($code)
        {       
            //公众号
            $this->userWx();
        }
        else
        {
            $this->user();
        }
    }

    // 获取授权参数
    public function appletsParam()
    {
        $store_id = Request::param('store_id'); // 商城ID
        $store_type = Request::param('store_type'); // 来源

        $code = addslashes(trim(Request::param('code'))); // code

        $data = array();
        // if ($store_type == 1)
        // { // 来源为小程序
            //获取用户openid
            if (!$code)
            {
                $message = Lang('Parameter error');
                echo json_encode(array('code'=>ERROR_CODE_CSCW,'message'=>$message));
                exit;
            }
            
            // 查询小程序配置
            $r0 = ConfigModel::where('store_id',$store_id)->field('appid,appsecret')->select()->toArray();
            if ($r0)
            {
                $appid = $r0[0]['appid']; // 小程序唯一标识
                $appsecret = $r0[0]['appsecret']; // 小程序的 app secret
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

            $url = 'https://api.weixin.qq.com/sns/jscode2session?appid=' . $appid . '&secret=' . $appsecret . '&js_code=' . $code . '&grant_type=authorization_code';
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
            $user = (array)json_decode($res);
            LaiKeLogUtils::lktLog("小程序授权返回信息:" . json_encode($user));
            if (isset($user['errcode']) || !isset($user['unionid']))
            {
                LaiKeLogUtils::lktLog("小程序授权错误信息:");
                LaiKeLogUtils::lktLog("appid:" . $appid . "->appsecret:" . $appsecret);
                LaiKeLogUtils::lktLog("授权url:" . $url);
                LaiKeLogUtils::lktLog(json_encode($user));
                $message = Lang('login.6');
                echo json_encode(array('code'=>ERROR_CODE_CSCW,'message'=>$message));
                exit;
            }
            else if (isset($user['unionid']))
            {
                $openid = $user['openid'];
                $unionid = $user['unionid']; 
				//后台自己管理session_key 不通过前端传递 授权后给前端 openid 然后前端给后台传入 openid 给后台 从而后台根据 openid来获取redis中的 ession_key

                cache('sessionKey_'.$openid, $user['session_key']);//添加新token数据
				
                $data = array('openid' => $openid,'unionid'=>$unionid);
            }
        // }

        $message = Lang('Success');
        echo json_encode(array('code'=>200,'message'=>$message,'data'=>$data));
        exit;
    }

    // 用户授权时，存储用户信息
    public function user()
    {
        $store_id = Request::param('store_id'); // 商城ID
        $store_type = Request::param('store_type'); // 来源
        $access_id = trim(Request::param('access_id')); 

        $unionid = addslashes(trim(Request::param('unionid'))); // 用户统一标识。针对一个微信开放平台帐号下的应用，同一用户的 unionid 是唯一的
        $openid = addslashes(trim(Request::param('openid'))); // openid
        $user_name = addslashes(trim(Request::param('nickName'))); // 用户昵称
        $headimgurl = trim(Request::param('headimgurl')); // 用户头像
        $sex = trim(Request::param('sex')); // 用户性别
        $pid = trim(Request::param('pid')); // 推荐人id
        $wx_id = '';
        if (!Request::has('unionid') and !Request::has('openid'))
        {
            LaiKeLogUtils::lktLog("unionid异常:" . $unionid);
            $message = Lang('login.4');
            return output(ERROR_CODE_CSCW,$message);
        }
        if ($unionid) 
        {
            $wx_id = $unionid;
        }
        if ($openid) 
        {
            $wx_id = $openid;
        }
        $r0 = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r0)
        {
            $user_id1 = $r0[0]['user_id']; //默认用户名ID前缀
            $is_register = $r0[0]['is_register'];
            $wx_headimgurl = $r0[0]['wx_headimgurl'];//默认微信用户头像
            if (empty($user_name))
            {
                $user_name = $r0[0]['wx_name'];  //默认微信用户名
            }
            if (empty($headimgurl))
            {
                $headimgurl = ServerPath::getimgpath($r0[0]['wx_headimgurl']);//默认微信用户头像
            }
            // if ($is_register == 2)
            // { // 当注册为免注册，并且来源为小程序

                // if (empty($access_id))
                // { // 授权ID为空,代表没有进入商品详情
                    // 生成密钥
                    // $token = Tools::getToken($store_id,$store_type);
                // }
                // else
                // { // 授权ID存在,代表有进入商品详情
                //     // $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组
                //     // if ($getPayload_test == false)
                //     // { // 过期
                //         // 生成密钥
                //         $token = Tools::getToken($store_id,$store_type);
                //     // }
                //     // else
                //     // {
                //     //     $token = $access_id;
                //     // }
                // }
                
                // 判断是否有该用户信息
                $res_1 = UserModel::where(['store_id'=>$store_id,'unionid|wx_id'=>$wx_id])->field('user_id,access_id')->select()->toArray();               
                $strs = "QWERTYUIOPASDFGHJKLZXCVBNM1234567890";
                $user_id = 'LKT-'.substr(str_shuffle($strs),mt_rand(0,strlen($strs)-9),8);
                // 用户是否第一次授权登录 0 不是 1 是
                $user_first_auth = 0;
                //事务开启
                Db::startTrans();
                try
                { 
                    if ($res_1)
                    {    
                        //存在则更新
                        $user_id = $res_1[0]['user_id'];
                        $old_token = $res_1[0]['access_id'];//原令牌
                        $token = Tools::getToken($store_id,$user_id);
                        if($old_token)
                        {
                            $res = Tools::verifyToken($old_token);
                            if($res)
                            {   
                                //未过期不更新token
                                $token = $old_token;
                            }
                            else
                            {
                                cache($old_token, NULL);//清理原token数据
                            }
                        }
                        
                        $UserModel = UserModel::where(['store_id'=>$store_id,'unionid|wx_id'=>$wx_id])->find();
                        $UserModel->access_id = $token;
                        $UserModel->last_time = date('Y-m-d H:i:s');
                        if (!empty($user_name))
                        {
                            $UserModel->user_name = $user_name;
                        }

                        if (!empty($headimgurl))
                        {
                            $UserModel->headimgurl = $headimgurl;
                        }

                        if (!empty($sex))
                        {
                            $UserModel->sex = $sex;
                        }
                        $UserModel->save();  
                    }
                    else
                    {
                        $user_first_auth = 1;
                        //不存在，则添加
                        $ppid = '';
                        if (!empty($pid))
                        {
                            $r_0 = UserDistributionModel::where(['user_id'=>$pid,'store_id'=>$store_id])->field('id')->select()->toArray();
                            if ($r_0)
                            {
                                $ppid = $pid;
                            }
                        }
                        else
                        {
                            $res_de = UserModel::where('store_id',$store_id)->field('user_id')->limit(1)->order('id','asc')->select()->toArray();
                            if($res_de)
                            {
                                $ppid = $res_de[0]['user_id'];
                            }   
                        }
                        $mima = Tools::lock_url('000000');
                        //添加用户数据
                        $UserModel = new UserModel();
                        $UserModel->store_id = $store_id;
                        // $UserModel->access_id = $token;
                        $UserModel->user_name = $user_name;
                        $UserModel->wx_id = $openid;
                        $UserModel->unionid = $unionid;
                        $UserModel->headimgurl = $headimgurl;
                        $UserModel->sex = $sex;
                        $UserModel->zhanghao = $user_id;
                        $UserModel->mima = $mima;
                        $UserModel->source = $store_type;
                        $UserModel->birthday = date("Y-m-d H:i:s");
                        if($ppid)
                        {
                            $UserModel->Referee = $ppid;
                        }
                        $UserModel->save();

                        //更新user_id
                        $r = UserModel::where('store_id',$store_id)->field('id')->limit(1)->order('id','desc')->select()->toArray();
                        $rr = $r[0]['id'];
                        $user_id = $user_id1 . ($rr + 1);//新注册的用户user_id
                        $token = Tools::getToken($store_id,$user_id);

                        $res_1 = UserModel::find($rr);
                        $res_1->user_id = $user_id;
                        $res_1->access_id = $token;
                        $res_1->save();

                        // 分销插件
                        $res_plu = PluginsModel::where(['store_id'=>$store_id,'plugin_code'=>'distribution','status'=>1])->select()->toArray();
                        if($res_plu)
                        {   
                            // 获取分销配置
                            $res_con = DistributionConfigModel::where(['store_id'=>$store_id,'status'=>1,'relationship'=>0])->select()->toArray();
                            if($res_con)
                            {
                                $relationship = $res_con[0]['relationship'];
                                if($relationship != 1)
                                { // 注册确定绑定关系
                                    if($ppid != '')
                                    { // 注册绑定永久         
                                        $comm = new Commission();          
                                        $comm->create_level($user_id, 0, $ppid, $store_id);               
                                    }  
                                }
                            }
                        }

                        $event = '会员' . $user_id . '授权注册成功';
                        // 在操作列表里添加一条会员登录信息
                        $RecordModel = new RecordModel();
                        $RecordModel->store_id = $store_id;
                        $RecordModel->user_id = $user_id;
                        $RecordModel->event = $event;
                        $RecordModel->type = 0;
                        $RecordModel->save();
                    }
                    // 用户信息存入redis
                    //获取用户信息
                    $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                                      ->select()
                                      ->toArray();
                    $res = ConfigModel::where('store_id', $store_id)
                                      ->field('exp_time')
                                      ->select()
                                      ->toArray();
                    if($res)
                    {
                        $exp_time = intval($res[0]['exp_time']* 3600);
                    }
                    else
                    {
                        $exp_time = 7200;
                    }
                    cache($token, $res_a[0], $exp_time);//添加新token数据

                    $this->update_user($store_id, $user_id, $access_id,$token);
                    Db::commit();
                }
                catch (\Exception $e) 
                {
                    // 回滚事务
                    Db::rollback();
                    $Log_content = $e->getMessage();
                    $this->Log($Log_content);
                    $message = Lang('login.8');
                    echo json_encode(array('code'=>ERROR_CODE_SQSB,'message'=>$message));
                    exit;
                }
            // }
            // else
            // {
            //     $message = Lang('login.7');
            //     echo json_encode(array('code'=>ERROR_CODE_QWSXTSZ,'message'=>$message));
            //     exit;
            // }

            $Log_content = __METHOD__ . '->' . __LINE__ . '授权成功!';
            $this->Log($Log_content);

            $r_user = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();

            $array = array('store_id'=>$store_id,'store_type'=>$store_type,'read_id'=>$user_id);
            $r_system = PC_Tools::GetAnnouncement($array);
            $tell_id = $r_system['tell_id'];
            $systemMsgTitle = $r_system['systemMsgTitle'];
            $systemMsg = $r_system['systemMsg'];
            $systemMsgStartDate = $r_system['systemMsgStartDate'];
            $systemMsgEndDate = $r_system['systemMsgEndDate'];
            $systemMsgType = $r_system['systemMsgType'];

            $data = array('access_id' => $token,'headimgurl' => $headimgurl,'user_name' => $user_name,'y_password'=>1,'user' => $r_user[0],'tell_id'=>$tell_id,'systemMsg'=>$systemMsg,'systemMsgEndDate'=>$systemMsgEndDate,'systemMsgStartDate'=>$systemMsgStartDate,'systemMsgTitle'=>$systemMsgTitle,'systemMsgType'=>$systemMsgType);

            $message = Lang('Success');
            echo json_encode(array('code'=>200,'message'=>$message,'data'=>$data));
            exit;
        }
        else
        {
            $message = Lang('login.0');
            echo json_encode(array('code'=>ERROR_CODE_PZBCZ,'message'=>$message));
            exit;
        }
    }

    // H5微信公众号授权时，存储用户信息
    public function userWx()
    {
        //微信公众号授权 
        $store_id = Request::param('store_id'); // 商城ID
        $store_type = Request::param('store_type'); // 来源
        $code = addslashes(trim(Request::param('code'))); // code

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
    
        $r0 = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r0)
        {
            $user_id1 = $r0[0]['user_id']; //默认用户名ID前缀
            $is_register = $r0[0]['is_register'];
            if (empty($user_name))
            {
                $user_name = $r0[0]['wx_name'];  //默认微信用户名
            }
            if (empty($headimgurl))
            {
                $headimgurl = ServerPath::getimgpath($r0[0]['wx_headimgurl']);//默认微信用户头像
            }
            // if ($is_register == 2)
            // { // 当注册为免注册，这里来源为公众号

                //判断是否有该用户信息
                $res_1 = UserModel::where(['store_id'=>$store_id,'unionid'=>$unionid])->field('user_id,access_id')->select()->toArray();
                $strs="QWERTYUIOPASDFGHJKLZXCVBNM1234567890";
                $user_id='LKT-'.substr(str_shuffle($strs),mt_rand(0,strlen($strs)-9),8);
                // 用户是否第一次授权登录 0 不是 1 是
                $user_first_auth = 0;
                //事务开启
                Db::startTrans();
                try
                { 
                    if ($res_1)
                    {    
                        //存在则更新
                        $user_id = $res_1[0]['user_id'];
                        $old_token = $res_1[0]['access_id'];//原令牌
                        $token = Tools::getToken($store_id,$user_id);
                        if($old_token)
                        {
                            $res = Tools::verifyToken($old_token);
                            if($res)
                            {   
                                //未过期不更新token
                                $token = $old_token;
                            }
                            else
                            {
                                cache($old_token, NULL);//清理原token数据
                            }
                        }
                        $UserModel = UserModel::where(['store_id'=>$store_id,'unionid'=>$unionid])->find();
                        $UserModel->access_id = $token;
                        $UserModel->last_time = date('Y-m-d H:i:s');
                        if (!empty($user_name))
                        {
                            $UserModel->user_name = $user_name;
                        }

                        if (!empty($headimgurl))
                        {
                            $UserModel->headimgurl = $headimgurl;
                        }

                        if (!empty($sex))
                        {
                            $UserModel->sex = $sex;
                        }
                        $UserModel->save();  
                    }
                    else
                    {
                        $user_first_auth = 1;
                        //不存在，则添加
                        $ppid = '';
                        $res_de = UserModel::where('store_id',$store_id)->field('user_id')->limit(1)->order('id','asc')->select()->toArray();
                        if($res_de)
                        {
                            $ppid = $res_de[0]['user_id'];
                        } 
                        $mima = Tools::lock_url('000000');
                        //添加用户数据
                        $UserModel = new UserModel();
                        $UserModel->store_id = $store_id;
                        // $UserModel->access_id = $token;
                        $UserModel->user_name = $user_name;
                        $UserModel->wx_id = $openid;
                        $UserModel->unionid = $unionid;
                        $UserModel->headimgurl = $headimgurl;
                        $UserModel->sex = $sex;
                        // $UserModel->mobile = $mobile;
                        $UserModel->zhanghao = $user_id;
                        $UserModel->mima = $mima;
                        $UserModel->source = $store_type;
                        $UserModel->birthday = date("Y-m-d H:i:s");
                        if($ppid)
                        {
                            $UserModel->Referee = $ppid;
                        }
                        $UserModel->save();

                        //更新user_id
                        $r = UserModel::where('store_id',$store_id)->field('id')->limit(1)->order('id','desc')->select()->toArray();
                        $rr = $r[0]['id'];
                        $user_id = $user_id1 . ($rr + 1);//新注册的用户user_id
                        $token = Tools::getToken($store_id,$user_id);

                        $res_1 = UserModel::find($rr);
                        $res_1->user_id = $user_id;
                        $res_1->access_id = $token;
                        $res_1->save();

                        // 分销插件
                        $res_plu = PluginsModel::where(['store_id'=>$store_id,'plugin_code'=>'distribution','status'=>1])->select()->toArray();
                        if($res_plu)
                        {   
                            // 获取分销配置
                            $res_con = DistributionConfigModel::where(['store_id'=>$store_id,'status'=>1,'relationship'=>0])->select()->toArray();
                            if($res_con)
                            {
                                $relationship = $res_con[0]['relationship'];
                                if($relationship != 1)
                                { // 注册确定绑定关系
                                    if($ppid != '')
                                    { // 注册绑定永久         
                                        $comm = new Commission();          
                                        $comm->create_level($user_id, 0, $ppid, $store_id);               
                                    }  
                                }
                            }
                        }

                        $event = '会员' . $user_id . '授权注册成功';
                        // 在操作列表里添加一条会员登录信息
                        $RecordModel = new RecordModel();
                        $RecordModel->store_id = $store_id;
                        $RecordModel->user_id = $user_id;
                        $RecordModel->event = $event;
                        $RecordModel->type = 0;
                        $RecordModel->save();
                    }
                    // 用户信息存入redis
                    //获取用户信息
                    $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                                      ->select()
                                      ->toArray();
                    $res = ConfigModel::where('store_id', $store_id)
                                      ->field('exp_time')
                                      ->select()
                                      ->toArray();
                    if($res)
                    {
                        $exp_time = intval($res[0]['exp_time']* 3600);
                    }
                    else
                    {
                        $exp_time = 7200;
                    }
                    cache($token, $res_a[0], $exp_time);//添加新token数据

                    Db::commit();
                }
                catch (\Exception $e) 
                {
                    // 回滚事务
                    Db::rollback();
                    $Log_content = $e->getMessage();
                    $this->Log($Log_content);
                    $message = Lang('login.8');
                    echo json_encode(array('code'=>ERROR_CODE_SQSB,'message'=>$message));
                    exit;
                }
            // }
            // else
            // {
            //     $message = Lang('login.7');
            //     echo json_encode(array('code'=>ERROR_CODE_QWSXTSZ,'message'=>$message));
            //     exit;
            // }

            $user = array();
            $user['user_name'] = $user_name;
            $user['headimgurl'] = $headimgurl;
            $user['user_id'] = $user_id;

            $Log_content = __METHOD__ . '->' . __LINE__ . '授权成功!';
            $this->Log($Log_content);

            $r_user = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();

            $data = array('access_id' => $token,'headimgurl' => $headimgurl,'user_name' => $user_name,'y_password'=>1,'user' => $r_user[0]);

            $message = Lang('Success');
            echo json_encode(array('code'=>200,'message'=>$message,'data'=>$data));
            exit;
        }
        else
        {
            $message = Lang('login.0');
            echo json_encode(array('code'=>ERROR_CODE_PZBCZ,'message'=>$message));
            exit;
        }
    }

    //切换用户推荐人
    public function chang_pid()
    {   
        $lktlog = new LaiKeLogUtils();
        $store_id = addslashes(trim(Request::param('store_id'))); // 商城id
        $language = trim(Request::param('language')); // 语言
        $pid = trim(Request::param('pid')); // 推荐人id
        $access_id = trim(Request::param('access_id')); 
        $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if($r0)
        {
            $user_id = $r0[0]['user_id'];

            $res_plu = PluginsModel::where(['plugin_code'=>'distribution','status'=>1,'flag'=>0])->select()->toArray();
            if($res_plu)
            {   
                //获取分销配置
                $res_con = DistributionConfigModel::where('store_id',$store_id)->select()->toArray();
                if($res_con && $res_con[0]['status'] == 1)
                {
                    if($res_con[0]['relationship'] == 1 && $pid != '')
                    {
                        //判断分销员身份
                        $res_d = UserDistributionModel::where(['store_id' => $store_id,'user_id' => $user_id])->field('id')->select()->toArray();
                        if(!$res_d && $user_id != $pid)
                        {
                            $UserModel = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->find();
                            $UserModel->Referee = $pid;
                            $res = $UserModel->save();
                            if(!$res)
                            {
                                $lktlog->log("app/user.log",__LINE__ . ":修改失败user_id-pid：$user_id-$pid\r\n");
                            }
                        }
                    }  
                }
            }
        }
        $message = Lang('Success');
        return output(200,$message);
    }

    /**
     * 阿里用户登陆
     */
    public function mpaliUserLogin()
    {
        try
        {
            $store_id = addslashes(trim(Request::param('store_id'))); // 商城id
            $store_type = trim(Request::param('store_type'));

            $alimp_auth_code = addslashes(trim(Request::param('alimp_auth_code'))); // 授权ID
            $access_id = trim(Request::param('access_id')); // 来客电商访问token
            if (empty($alimp_auth_code))
            {
                $message = Lang('Parameter error');
                return output(ERROR_CODE_CSCW,$message);
            }
            $userid = AlipayTools::getAliUserId($store_id, $alimp_auth_code);
            $data = array();
            if ($userid)
            {
                //直接查询数据库用户表
                $userRs = UserModel::where('zfb_id',$userid)->select()->toArray();
                //存在用户
                if ($userRs)
                {
                    //查询到用户则直接返回给用户
                    // if (!Request::has('access_id') || "undefined" == $access_id)
                    // { // 授权ID为空,代表没有进入商品详情
                        // 生成密钥
                        $token = Tools::getToken($store_id,$store_type);
                    // }
                    // else
                    // {
                    //     // 授权ID存在,代表有进入商品详情
                    //     $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组
                    //     if ($getPayload_test == false)
                    //     { // 过期
                    //         // 生成密钥
                    //         $token = Tools::getToken($store_id,$store_type);;
                    //     }
                    //     else
                    //     {
                    //         $token = $access_id;
                    //     }
                    // }
                    $old_token = $userRs[0]['access_id'];//原令牌
                    cache($old_token, NULL);//清理原token数据

                    $lkt_user_id = $userRs[0]['user_id'];
                    $userRs[0]['access_id'] = $token;
                    $code = 200;
                    $message = Lang('login.9');
                    $data["userInfo"] = $userRs[0];
                    $UserModel = UserModel::where(['user_id'=>$lkt_user_id])->find();
                    $UserModel->access_id = $token;
                    $UserModel->login_num = Db::raw('login_num+1');
                    $UserModel->last_time = date('Y-m-d H:i:s');
                    $updateRowNum = $UserModel->save();
                    // 用户信息存入redis
                    //获取用户信息
                    $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$lkt_user_id])
                                      ->select()
                                      ->toArray();
                    $res = ConfigModel::where('store_id', $store_id)
                                      ->field('exp_time')
                                      ->select()
                                      ->toArray();
                    if($res)
                    {
                        $exp_time = intval($res[0]['exp_time']* 3600);
                    }
                    else
                    {
                        $exp_time = 7200;
                    }
                    cache($token, $res_a[0], $exp_time);//添加新token数据

                    if (!$updateRowNum)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '更新用户信息失败!user_id:' . $lkt_user_id;
                        $this->Log($Log_content);
                        $message = Lang('login.10');
                        return output(ERROR_CODE_WLYC,$message);
                    }
                    else
                    {
                        //更新购物车里面的商品用户ID
                        $this->updateCartInfo($access_id, $store_id, $lkt_user_id,$token);
                    }
                }
                else
                {
                    $message = Lang('login.11');
                    //没有查询到用户则前端授权
                    $data["zfb_id"] = $userid;
                    return output(ERROR_CODE_SQSB,$message);
                }
            }
            else
            {
                //没有取到阿里的用户ID
                $message = Lang('login.12');
                return output(ERROR_CODE_SQSB,$message);
            }
            return output(200,$message,$data);
        }
        catch (Exception $exception)
        {
            $message = Lang('login.12');
            return output($exception->getCode(),$message);
        }
    }

    /**
     * 新增、修改阿里用户
     */
    public function updateAliUser()
    {
        try
        {
            $store_id = addslashes(trim(Request::param('store_id'))); // 商城id
            $language = trim(Request::param('language')); // 语言

            $store_type = trim(Request::param('store_type')); // 来源
            $user_name = trim(Request::param('nickName')); // 用户昵称
            $headimgurl = trim(Request::param('headimgurl')); // 用户头像
            $sex = trim(Request::param('sex')); // 用户性别
            $pid = trim(Request::param('pid')); // 推荐人id

            //支付宝ID
            $zfb_id = trim(Request::param('zfb_id')); // 支付宝ID

            $access_id = trim(Request::param('access_id')); 
            $token = $access_id;

            //返回前端的数组
            $data = array();

            $token = Tools::getToken($store_id,$store_type);

            $user_id = $this->getPreUserId($store_id);

            $res_1 = UserModel::where(['store_id'=>$store_id,'zfb_id'=>$zfb_id])->field('user_id,access_id')->select()->toArray();
            //事务开启
            Db::startTrans();
            try
            {
                if ($res_1)
                {//存在则更新
                    $user_id = $res_1[0]['user_id'];
                    $old_token = $res_1[0]['access_id'];//原令牌
                    cache($old_token, NULL);//清理原token数据
                    $sql2 = UserModel::where(['store_id'=>$store_id,'zfb_id'=>$zfb_id])->find();
                    $sql2->access_id = $token;
                    $sql2->user_name = $user_name;
                    $sql2->headimgurl = $headimgurl;
                    $sql2->sex = $sex;
                    $res_2 = $sql2->save();
                    if (!$res_2)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '更新用户信息失败!zfb_id:' . $zfb_id;
                        $this->Log($Log_content);
                    }
                }
                else
                {//不存在，则添加
                    $ppid = '';
                    if (!empty($pid))
                    {
                        $r_0 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$pid])->field('id')->select()->toArray();
                        if ($r_0)
                        {
                            $ppid = $pid;
                        }
                    }
                    $sql = new UserModel();
                    $sql->store_id = $store_id;
                    $sql->access_id = $token;
                    $sql->user_name = $user_name;
                    $sql->zfb_id = $zfb_id;
                    $sql->headimgurl = $headimgurl;
                    $sql->sex = $sex;
                    $sql->mobile = '';
                    $sql->zhanghao = $user_id;
                    $sql->mima = '';
                    $sql->source = $store_type;
                    $sql->birthday = date("Y-m-d H:i:s");
                    if($ppid)
                    {
                        $sql->Referee = $ppid;
                    }
                    else
                    {
                        $sql->Referee = '';
                    }
                    $sql->save();
                    $rr = $sql->id;
                    $user_id = $user_id . ($rr + 1);//新注册的用户user_id

                    //更新会员ID
                    $sql_1 = "update lkt_user set user_id = '$user_id' where id = '$rr' and store_id = '$store_id'";
                    $res_1 = $db->update($sql_1);
                    $sql_1 = UserModel::find($rr);
                    $sql_1->user_id = $user_id;
                    $sql_1->save();

                    $event = '会员' . $user_id . '授权注册成功';
                    // 在操作列表里添加一条会员登录信息
                    $sql01 = new RecordModel();
                    $sql01->store_id = $store_id;
                    $sql01->user_id = $user_id;
                    $sql01->event = $event;
                    $sql01->type = 0;
                    $sql01->save();

                    // 用户信息存入redis
                    //获取用户信息
                    $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                                      ->select()
                                      ->toArray();
                    $res = ConfigModel::where('store_id', $store_id)
                                      ->field('exp_time')
                                      ->select()
                                      ->toArray();
                    if($res)
                    {
                        $exp_time = intval($res[0]['exp_time']* 3600);
                    }
                    else
                    {
                        $exp_time = 7200;
                    }
                    cache($token, $res_a[0], $exp_time);//添加新token数据
                }
            }
            catch (\Exception $e) 
            {
                // 回滚事务
                Db::rollback();
                $Log_content = $e->getMessage();
                $this->Log($Log_content);
                $message = Lang('login.8');
                return output(ERROR_CODE_SQSB,$message);
            }
            
            $this->updateCartInfo($access_id,$store_id,$user_id,$token);

            $userRs = UserModel::where('zfb_id',$zfb_id)->select()->toArray();
            //存在用户
            if ($userRs)
            {
                $data = array("userInfo" => $userRs[0]);
                $message = Lang('Success');
                return output(200,$message,$data);
            }
            else
            {
                throw  new Exception("未查获取到用户信息", ERROR_CODE_YHBCZ);
            }
        }
        catch (Exception $exception)
        {
            $message = Lang('login.10');
            return output(ERROR_CODE_WLYC,$message);
        }
    }

    /**
     * 新增或修改头条用户
     */
    public function updateTTUser()
    {
        try
        {
            $store_id = addslashes(trim(Request::param('store_id'))); // 商城id
            $language = trim(Request::param('language')); // 语言

            $store_type = trim(Request::param('store_type')); // 来源
            $user_name = trim(Request::param('nickName')); // 用户昵称
            $headimgurl = trim(Request::param('headimgurl')); // 用户头像
            $sex = trim(Request::param('sex')); // 用户性别
            $pid = trim(Request::param('pid')); // 推荐人id

            //头条ID
            $tt_id = trim(Request::param('tt_id')); // 头条ID
            $access_id = trim(Request::param('access_id')); //
            $token = $access_id;
            //返回前端的数组
            $data = array();
            $token = Tools::getToken($store_id,$store_type);

            $user_id = $this->getPreUserId($store_id);

            $res_1 = UserModel::where(['store_id'=>$store_id,'tt_id'=>$tt_id])->field('user_id,access_id')->select()->toArray();
            //事务开启
            Db::startTrans();
            try
            {
                if ($res_1)
                {//存在则更新
                    $user_id = $res_1[0]['user_id'];
                    $old_token = $res_1[0]['access_id'];//原令牌
                    cache($old_token, NULL);//清理原token数据
                    $sql2 = UserModel::where(['store_id'=>$store_id,'tt_id'=>$tt_id])->find();
                    $sql2->access_id = $token;
                    $sql2->user_name = $user_name;
                    $sql2->headimgurl = $headimgurl;
                    $sql2->sex = $sex;
                    $res_2 = $sql2->save();
                    if (!$res_2)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '更新用户信息失败!zfb_id:' . $zfb_id;
                        $this->Log($Log_content);
                    }
                }
                else
                {//不存在，则添加
                    $ppid = '';
                    if (!empty($pid))
                    {
                        $r_0 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$pid])->field('id')->select()->toArray();
                        if ($r_0)
                        {
                            $ppid = $pid;
                        }
                    }
                    $sql = new UserModel();
                    $sql->store_id = $store_id;
                    $sql->access_id = $token;
                    $sql->user_name = $user_name;
                    $sql->tt_id = $tt_id;
                    $sql->headimgurl = $headimgurl;
                    $sql->sex = $sex;
                    $sql->mobile = '';
                    $sql->zhanghao = $user_id;
                    $sql->mima = '';
                    $sql->source = $store_type;
                    $sql->birthday = date("Y-m-d H:i:s");
                    if($ppid)
                    {
                        $sql->Referee = $ppid;
                    }
                    else
                    {
                        $sql->Referee = '';
                    }
                    $sql->save();
                    $rr = $sql->id;
                    $user_id = $user_id . ($rr + 1);//新注册的用户user_id

                    //更新会员ID
                    $sql_1 = "update lkt_user set user_id = '$user_id' where id = '$rr' and store_id = '$store_id'";
                    $res_1 = $db->update($sql_1);
                    $sql_1 = UserModel::find($rr);
                    $sql_1->user_id = $user_id;
                    $sql_1->save();

                    $event = '会员' . $user_id . '授权注册成功';
                    // 在操作列表里添加一条会员登录信息
                    $sql01 = new RecordModel();
                    $sql01->store_id = $store_id;
                    $sql01->user_id = $user_id;
                    $sql01->event = $event;
                    $sql01->type = 0;
                    $sql01->save();
                    // 用户信息存入redis
                    //获取用户信息
                    $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                                      ->select()
                                      ->toArray();
                    $res = ConfigModel::where('store_id', $store_id)
                                      ->field('exp_time')
                                      ->select()
                                      ->toArray();
                    if($res)
                    {
                        $exp_time = intval($res[0]['exp_time']* 3600);
                    }
                    else
                    {
                        $exp_time = 7200;
                    }
                    cache($token, $res_a[0], $exp_time);//添加新token数据
                }
            }
            catch (\Exception $e) 
            {
                // 回滚事务
                Db::rollback();
                $Log_content = $e->getMessage();
                $this->Log($Log_content);
                $message = Lang('login.8');
                return output(ERROR_CODE_SQSB,$message);
            }

            $this->updateCartInfo($access_id,$store_id,$user_id,$token);
            $userRs = UserModel::where('tt_id',$tt_id)->select()->toArray();
            //存在用户
            if ($userRs)
            {
                $data = array("userInfo" => $userRs[0]);
                $message = Lang('Success');
                return output(200,$message,$data);
            }
            else
            {
                throw  new Exception("未查获取到用户信息", ERROR_CODE_YHBCZ);
            }
        }
        catch (Exception $exception)
        {
            $message = Lang('login.10');
            return output(ERROR_CODE_WLYC,$message);
        }
    }

    /**
     * 头条用户登陆
     */
    function ttUserLogin()
    {
        try
        {
            $store_id = addslashes(trim(Request::param('store_id'))); // 商城id
            $store_type = addslashes(trim(Request::param('store_type'))); // 商城id
            $language = trim(Request::param('language')); // 语言

            $tt_auth_code = addslashes(trim(Request::param('tt_auth_code'))); // 授权ID
            $access_id = trim(Request::param('access_id')); // 来客电商访问token
            if (empty($tt_auth_code))
            {
                $message = Lang('Parameter error');
                return output(ERROR_CODE_CSCW,$message);
            }
            $config = LKTConfigInfo::getPayConfig($store_id, "tt_alipay");
            if (empty($config))
            {
                $message = Lang('login.0');
                return output(ERROR_CODE_PZBCZ,$message);
            }
            $ttAppid = $config['ttAppid'];
            $ttAppSecret = $config['ttAppSecret'];

            // $userid = TTUtils::getTTOpenId($ttAppid, $ttAppSecret, $tt_auth_code);
            $userid = 125;
            $data = array();
            if ($userid)
            {
                //直接查询数据库用户表
                $userRs = UserModel::where('tt_id',$userid)->select()->toArray();
                //存在用户
                if ($userRs)
                {
                    //查询到用户则直接返回给用户
                    // if (Request::has('access_id') || "undefined" == $access_id)
                    // { // 授权ID为空,代表没有进入商品详情
                        // 生成密钥
                        $token = Tools::getToken($store_id,$store_type);
                    // }
                    // else
                    // {
                    //     // 授权ID存在,代表有进入商品详情
                    //     $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组
                    //     if ($getPayload_test == false)
                    //     { // 过期
                    //         // 生成密钥
                    //         $token = Tools::getToken($store_id,$store_type);
                    //     }
                    //     else
                    //     {
                    //         $token = $access_id;
                    //     }
                    // }
                    $old_token = $userRs[0]['access_id'];//原令牌
                    cache($old_token, NULL);//清理原token数据

                    $lkt_user_id = $userRs[0]['user_id'];
                    $userRs[0]['access_id'] = $token;
                    $data["code"] = 200;
                    $data["msg"] = Lang('login.9');
                    $data["userInfo"] = $userRs[0];
                    $UserModel = UserModel::where(['user_id'=>$lkt_user_id])->find();
                    $UserModel->access_id = $token;
                    $UserModel->login_num = Db::raw('login_num+1');
                    $UserModel->last_time = date('Y-m-d H:i:s');
                    $updateRowNum = $UserModel->save();

                    // 用户信息存入redis
                    //获取用户信息
                    $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$lkt_user_id])
                                      ->select()
                                      ->toArray();
                    $res = ConfigModel::where('store_id', $store_id)
                                      ->field('exp_time')
                                      ->select()
                                      ->toArray();
                    if($res)
                    {
                        $exp_time = intval($res[0]['exp_time']* 3600);
                    }
                    else
                    {
                        $exp_time = 7200;
                    }
                    cache($token, $res_a[0], $exp_time);//添加新token数据
                    if (!$updateRowNum)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '更新用户信息失败!sql:' . $sql;
                        $this->Log($Log_content);
                        $message = Lang('login.10');
                        return output(ERROR_CODE_WLYC,$message);
                    }
                    else
                    {
                        //更新购物车里面的商品用户ID
                        $this->updateCartInfo($access_id,$store_id, $lkt_user_id,$token);
                    }
                }
                else
                {
                    $message = Lang('login.11');
                    //没有查询到用户则前端授权
                    $data["tt_id"] = $userid;
                    return output(ERROR_CODE_SQSB,$message,$data);
                }
            }
            else
            {

                //没有取到用户ID
                $message = Lang('login.12');
                return output(ERROR_CODE_SQSB,$message);
            }
            $Log_content = __METHOD__ . '->' . __LINE__ . '登录成功!';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        catch (Exception $exception)
        {
            $message = Lang('login.12');
            return output($exception->getCode(),$message);
        }
    }

    /**
     * 百度用户登录
     */
    function bdUserLogin()
    {
        try
        {
            $store_id = addslashes(trim(Request::param('store_id'))); // 商城id
            $language = trim(Request::param('language')); // 语言
            $store_type = addslashes(trim(Request::param('store_type')));
            $bd_auth_code = addslashes(trim(Request::param('bd_auth_code'))); // 授权ID
            $access_id = trim(Request::param('access_id')); // 来客电商访问token
            if (empty($bd_auth_code))
            {
                $message = Lang('Parameter error');
                return output(ERROR_CODE_CSCW,$message);
            }

            $bdconfig = LKTConfigInfo::getPayConfig($store_id, "baidu_pay");

            if (empty($bdconfig))
            {
                $message = Lang('login.0');
                return output(ERROR_CODE_PZBCZ,$message);
            }

            $ttAppid = $bdconfig['bdmpappid'];
            $ttAppSecret = $bdconfig['bdmpappsk'];

            LaiKeLogUtils::lktLog(__METHOD__ . '->' . __LINE__ . json_encode($bdconfig));
            $userid = BDUtils::getBDOpenId($ttAppid, $ttAppSecret, $bd_auth_code);
            LaiKeLogUtils::lktLog(__METHOD__ . '->' . __LINE__ . "=" . $userid);

            $data = array();
            if ($userid)
            {
                //直接查询数据库用户表
                $userRs = UserModel::where('bd_id',$userid)->select()->toArray();
                //存在用户
                if ($userRs)
                {
                    //查询到用户则直接返回给用户
                    // if (Request::has($access_id) || 'undefined' == $access_id)
                    // { // 授权ID为空,代表没有进入商品详情
                        // 生成密钥
                        $token = Tools::getToken($store_id,$store_type);
                    // }
                    // else
                    // {
                    //     // 授权ID存在,代表有进入商品详情
                    //     $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组
                    //     if ($getPayload_test == false)
                    //     { // 过期
                    //         // 生成密钥
                    //         $token = Tools::getToken($store_id,$store_type);
                    //     }
                    //     else
                    //     {
                    //         $token = $access_id;
                    //     }
                    // }
                    $old_token = $userRs[0]['access_id'];//原令牌
                    cache($old_token, NULL);//清理原token数据

                    $lkt_user_id = $userRs[0]['user_id'];
                    $sql = "update lkt_user set access_id = '$token' ,login_num=login_num+1, last_time = now() where user_id = '$lkt_user_id'";
                    $userRs[0]['access_id'] = $token;
                    $data["code"] = 200;
                    $data["msg"] = Lang('login.9');
                    $data["userInfo"] = $userRs[0];
                    $UserModel = UserModel::where(['user_id'=>$lkt_user_id])->find();
                    $UserModel->access_id = $token;
                    $UserModel->login_num = Db::raw('login_num+1');
                    $UserModel->last_time = date('Y-m-d H:i:s');
                    $updateRowNum = $UserModel->save();
                    // 用户信息存入redis
                    //获取用户信息
                    $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$lkt_user_id])
                                      ->select()
                                      ->toArray();
                    $res = ConfigModel::where('store_id', $store_id)
                                      ->field('exp_time')
                                      ->select()
                                      ->toArray();
                    if($res)
                    {
                        $exp_time = intval($res[0]['exp_time']* 3600);
                    }
                    else
                    {
                        $exp_time = 7200;
                    }
                    cache($token, $res_a[0], $exp_time);//添加新token数据

                    if (!$updateRowNum)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '更新用户信息失败!sql:' . $sql;
                        $this->Log($Log_content);
                        $message = Lang('login.10');
                        return output(ERROR_CODE_WLYC,$message);
                    }
                    else
                    {
                        //更新购物车里面的商品用户ID
                        $this->updateCartInfo($access_id,$store_id, $lkt_user_id,$token);
                    }
                }
                else
                {
                    $message = Lang('login.11');
                    //没有查询到用户则前端授权
                    $data["bd_id"] = $userid;
                    return output(ERROR_CODE_SQSB,$message,$data);
                }
            }
            else
            {
                //没有取到用户ID
                $message = Lang('login.12');
                return output(ERROR_CODE_SQSB,$message);
            }
            $Log_content = __METHOD__ . '->' . __LINE__ . '登录成功!';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        catch (Exception $exception)
        {
            $message = Lang('login.12');
            return output($exception->getCode(),$message);
        }
    }

    /**
     * 新增或修改百度用户
     */
    public function updateBDUser()
    {
        try
        {
            $store_id = addslashes(trim(Request::param('store_id'))); // 商城id
            $language = trim(Request::param('language')); // 语言

            $store_type = trim(Request::param('store_type')); // 来源
            $user_name = trim(Request::param('nickName')); // 用户昵称
            $headimgurl = trim(Request::param('headimgurl')); // 用户头像
            $sex = trim(Request::param('sex')); // 用户性别
            $pid = trim(Request::param('pid')); // 推荐人id

            //百度openID
            $bd_id = trim(Request::param('bd_id')); // 百度openID
            $access_id = trim(Request::param('access_id')); //
            //返回前端的数组
            $data = array();

            // 获取 token
            $token = Tools::getToken($store_id,$store_type);

            //前缀
            $user_id = $this->getPreUserId($store_id);

            $res_1 = UserModel::where(['store_id'=>$store_id,'bd_id'=>$bd_id])->field('user_id,access_id')->select()->toArray();
            //事务开启
            Db::startTrans();
            try
            {
                if ($res_1)
                {//存在则更新
                    $user_id = $res_1[0]['user_id'];
                    $old_token = $res_1[0]['access_id'];//原令牌
                    cache($old_token, NULL);//清理原token数据
                    $sql2 = UserModel::where(['store_id'=>$store_id,'bd_id'=>$bd_id])->find();
                    $sql2->access_id = $token;
                    $sql2->user_name = $user_name;
                    $sql2->headimgurl = $headimgurl;
                    $sql2->sex = $sex;
                    $res_2 = $sql2->save();
                    if (!$res_2)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '更新用户信息失败!zfb_id:' . $zfb_id;
                        $this->Log($Log_content);
                    }
                }
                else
                {//不存在，则添加
                    $ppid = '';
                    if (!empty($pid))
                    {
                        $r_0 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$pid])->field('id')->select()->toArray();
                        if ($r_0)
                        {
                            $ppid = $pid;
                        }
                    }
                    $sql = new UserModel();
                    $sql->store_id = $store_id;
                    $sql->access_id = $token;
                    $sql->user_name = $user_name;
                    $sql->bd_id = $bd_id;
                    $sql->headimgurl = $headimgurl;
                    $sql->sex = $sex;
                    $sql->mobile = '';
                    $sql->zhanghao = $user_id;
                    $sql->mima = '';
                    $sql->source = $store_type;
                    $sql->birthday = date("Y-m-d H:i:s");
                    if($ppid)
                    {
                        $sql->Referee = $ppid;
                    }
                    else
                    {
                        $sql->Referee = '';
                    }
                    $sql->save();
                    $rr = $sql->id;
                    $user_id = $user_id . ($rr + 1);//新注册的用户user_id

                    //更新会员ID
                    $sql_1 = "update lkt_user set user_id = '$user_id' where id = '$rr' and store_id = '$store_id'";
                    $res_1 = $db->update($sql_1);
                    $sql_1 = UserModel::find($rr);
                    $sql_1->user_id = $user_id;
                    $sql_1->save();

                    $event = '会员' . $user_id . '授权注册成功';
                    // 在操作列表里添加一条会员登录信息
                    $sql01 = new RecordModel();
                    $sql01->store_id = $store_id;
                    $sql01->user_id = $user_id;
                    $sql01->event = $event;
                    $sql01->type = 0;
                    $sql01->save();

                    // 用户信息存入redis
                    //获取用户信息
                    $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                                      ->select()
                                      ->toArray();
                    $res = ConfigModel::where('store_id', $store_id)
                                      ->field('exp_time')
                                      ->select()
                                      ->toArray();
                    if($res)
                    {
                        $exp_time = intval($res[0]['exp_time']* 3600);
                    }
                    else
                    {
                        $exp_time = 7200;
                    }
                    cache($token, $res_a[0], $exp_time);//添加新token数据
                }
            }
            catch (\Exception $e) 
            {
                // 回滚事务
                Db::rollback();
                $Log_content = $e->getMessage();
                $this->Log($Log_content);
                $message = Lang('login.8');
                return output(ERROR_CODE_SQSB,$message);
            }
            $this->update_user($store_id, $user_id, $access_id,$token);
            $userRs = UserModel::where('bd_id',$bd_id)->select()->toArray();
            //存在用户
            if ($userRs)
            {
                $data = array("userInfo" => $userRs[0]);
                $message = Lang('Success');
                return output(200,$message,$data);
            }
            else
            {
                throw  new Exception("未查获取到用户信息", ERROR_CODE_YHBCZ);
            }
        }
        catch (Exception $exception)
        {
            $message = Lang('login.10');
            return output(ERROR_CODE_WLYC,$message);
        }
    }

    // 进入登录页面
    public function index()
    {
        $store_id = Request::param('store_id'); // 商城ID
        $access_id = Request::param('access_id'); // 授权id

        $r0 = ConfigModel::where(['store_id'=>$store_id])->field('logo,company,app_title,app_logo')->select()->toArray();
        if ($r0)
        {
            $logo = ServerPath::getimgpath($r0[0]['logo']);
            $appLogo = ServerPath::getimgpath($r0[0]['app_logo']);
            $company = $r0[0]['company'];
        }
        else
        {
            $logo = '../../static/img/landing_logo@2x.png';
            $company = '来客电商';
        }

        //查询协议名称
        $res_0 = AgreementModel::where(['store_id'=>$store_id,'type'=>0])->field('name')->limit(1)->select()->toArray();
        $Agreement = $res_0 ? $res_0[0]['name'] : '';

        //查询协议名称
        $res_1 = AgreementModel::where(['store_id'=>$store_id,'type'=>2])->field('name')->limit(1)->select()->toArray();
        $Agreement_1 = $res_1 ? $res_1[0]['name'] : '';

        $data = array('logo' => $logo, 'appLogo' => $appLogo, 'company' => $company, 'Agreement' => $Agreement,'Agreement_1' => $Agreement_1);

        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 注册
    public function user_register()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权ID
        $clientid = trim(Request::param('clientid')); // 推送客户id

        $type = Request::param('type'); // 类型 0.手机号 1.邮箱
        $cpc = Request::param('cpc'); // 区号
        $country_num = Request::param('country_num'); // 国家代码
        $tel = Request::param('phone'); // 手机号码
        $e_mail = Request::param('e_mail'); // 邮箱
        $password = Tools::lock_url((Request::param('password')));//密码
        $keyCode = trim(Request::param('keyCode')); // 验证码
        $pid = trim(Request::param('pid')); // 推荐人id

        $zhanghao = Tools::generateRandomString(8);

        if($type == 1)
        {
            $arr = array($e_mail, array('code' => $keyCode));
        }
        else
        {
            $arr = array($cpc.$tel, array('code' => $keyCode));
        }
        $Tools = new Tools($store_id, 1);
        $rew = $Tools->verification_code($arr);

        $r0 = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r0)
        {
            $wx_headimgurl = $r0[0]['wx_headimgurl'];//默认微信用户头像
            $headimgurl = ServerPath::getimgpath($wx_headimgurl);//默认微信用户头像
            $wx_name = $r0[0]['wx_name'];  //默认微信用户名
            $user_id1 = $r0[0]['user_id']; //默认用户名ID前缀
        }

        $ppid = '';
        if (!empty($pid))
        {
            $r_0 = UserDistributionModel::where(['user_id'=>$pid,'store_id'=>$store_id])->field('id')->select()->toArray();
            if ($r_0)
            {
                $ppid = $pid;
            }
        }
        else
        {
            $res_de = UserModel::where('store_id',$store_id)->field('user_id')->limit(1)->order('id','asc')->select()->toArray();
            if($res_de)
            {
                $ppid = $res_de[0]['user_id'];
            }   
        }

        // 生成密钥
        $token = Tools::getToken($store_id,$store_type);

        Db::startTrans();
        try
        {
            //添加会员数据
            $sql = new UserModel();
            $sql->store_id = $store_id;
            $sql->access_id = $token;
            $sql->user_name = $wx_name;
            $sql->headimgurl = $headimgurl;
            $sql->cpc = $cpc;
            $sql->country_num = $country_num;
            $sql->mobile = $tel;
            $sql->e_mail = $e_mail;
            $sql->zhanghao = $zhanghao;
            $sql->mima = $password;
            $sql->sex = 1;//性别 0:未知 1:男 2:女
            $sql->source = $store_type;
            $sql->birthday = date("Y-m-d H:i:s");
            if($clientid)
            {
                $sql->clientid = $clientid;
            }
            if($ppid)
            {
                $sql->Referee = $ppid;
            }
            $sql->save();

            //更新user_id
            $r = UserModel::where('store_id',$store_id)->field('id')->limit(1)->order('id','desc')->select()->toArray();
            $rr = $r[0]['id'];
            $user_id = $user_id1 . ($rr + 1);//新注册的用户user_id

            $res_1 = UserModel::find($rr);
            $res_1->user_id = $user_id;
            $res_1->save();

            //分销插件
            $res_plu = PluginsModel::where(['store_id'=>$store_id,'plugin_code'=>'distribution','status'=>1])->select()->toArray();
            if($res_plu)
            {   
                //获取分销配置
                $res_con = DistributionConfigModel::where(['store_id'=>$store_id,'status'=>1,'relationship'=>0])->select()->toArray();
                if($res_con)
                {
                    $relationship = $res_con[0]['relationship'];
                    if($relationship != 1)
                    { // 注册确定绑定关系
                        if($ppid != '')
                        {   
                            //注册绑定永久         
                            $comm = new Commission();          
                            $comm->create_level($user_id, 0, $ppid, $store_id);               
                        }  
                    }
                }
            }

            $event = '会员' . $user_id . '注册成功';
            // 在操作列表里添加一条会员登录信息
            $RecordModel = new RecordModel();
            $RecordModel->store_id = $store_id;
            $RecordModel->user_id = $user_id;
            $RecordModel->event = $event;
            $RecordModel->type = 0;
            $RecordModel->save();

            // 用户信息存入redis
            //获取用户信息
            $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();

            $res = ConfigModel::where('store_id', $store_id)->field('exp_time')->select()->toArray();
            if($res)
            {
                $exp_time = intval($res[0]['exp_time']* 3600);
            }
            else
            {
                $exp_time = 7200;
            }
            cache($token, $res_a[0], $exp_time);//添加新token数据

            $y_password = 1;
            if ($rew)
            {
                SessionIdModel::where('id',$rew)->delete();
            }
            $this->update_user($store_id, $user_id, $access_id,$token);
            Db::commit();
            $data = array('access_id' => $token, 'user_name' => $tel, 'headimgurl' => $headimgurl, 'y_password' => $y_password);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        catch (\Exception $e) 
        {
            // 回滚事务
            Db::rollback();
            $Log_content = $e->getMessage();
            $this->Log($Log_content);
            $message = Lang('login.16');
            return output(ERROR_CODE_ZCSBWLFM,$message);
        }        
    }

    // 注册协议
    public function register_agreement()
    {
        $store_id = trim(Request::param('store_id'));
        $r1 = AgreementModel::where(['store_id'=>$store_id,'type'=>0])->select()->toArray();
        if ($r1)
        {
            $name = $r1[0]['name'];
            $content = $r1[0]['content'];
        }
        else
        {
            $name = '';
            $content = '';
        }
        $data = array('name' => $name,'content' => $content);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 隐私协议
    public function privacy_agreement()
    {
        $store_id = trim(Request::param('store_id'));
        $r1 = AgreementModel::where(['store_id'=>$store_id,'type'=>2])->select()->toArray();
        if ($r1)
        {
            $name = $r1[0]['name'];
            $content = $r1[0]['content'];
        }
        else
        {
            $name = '';
            $content = '';
        }
        $data = array('name' => $name,'content' => $content);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 验证码登录
    public function register()
    {
        $store_id = trim(Request::param('store_id'));
        $language = trim(Request::param('language')); // 语言
        $store_type = trim(Request::param('store_type'));
        $access_id = trim(Request::param('access_id')); // 授权ID
        $clientid = trim(Request::param('clientid')); // 推送客户ID

        $type = Request::param('type'); // 类型 0.手机号 1.邮箱
        $cpc = Request::param('cpc'); // 区号
        $country_num = Request::param('country_num'); // 国家代码
        $tel = Request::param('phone'); // 手机号码
        $e_mail = Request::param('e_mail'); // 邮箱
        $keyCode = trim(Request::param('keyCode')); // 验证码

        $pid = trim(Request::param('pid')); // 推荐人id

        if($type == 1)
        {
            $arr = array($e_mail, array('code' => $keyCode));
        }
        else
        {
            $arr = array($cpc.$tel, array('code' => $keyCode));
        }
        $Tools = new Tools($store_id, 1);
        $rew = $Tools->verification_code($arr);

        $wx_status = 0;

        if($type == 1)
        {
            $r = UserModel::where(['store_id'=>$store_id,'e_mail'=>$e_mail])->select()->toArray();
        }
        else
        {
            $r = UserModel::where(['store_id'=>$store_id,'cpc'=>$cpc,'country_num'=>$country_num,'mobile'=>$tel])->select()->toArray();
        }
        if ($r)
        { // 已加注册
            //开启事务
            Db::startTrans();
            try
            {
                // 存在
                $user_id = $r[0]['user_id'];
                $user_name = $r[0]['user_name'];
                $headimgurl = $r[0]['headimgurl'];
                $password = $r[0]['mima'];
                $cpc = $r[0]['cpc']; // ITU电话区号
                $country_num = $r[0]['country_num']; // 国家代码
                $lang = $r[0]['lang']; // 语言
                $preferred_currency = $r[0]['preferred_currency']; // 用户偏好货币id
                $event = '会员' . $user_id . '登录';

                if ($password)
                {
                    $y_password = 1; // 有密码
                }
                else
                {
                    $y_password = 0; // 没密码
                }
                if ($store_type == 1)
                {
                    $wx_id = $r[0]['wx_id'];
                    if ($wx_id != '')
                    {
                        $wx_status = 1;
                    }
                }
                if (empty($access_id))
                { // 授权ID为空,代表没有进入商品详情
                    // 生成密钥
                    $token = Tools::getToken($store_id,$store_type);
                }
                else
                { // 授权ID存在,代表有进入商品详情
                    $token = Tools::getToken($store_id,$store_type);
                    $this->update_user($store_id, $user_id, $access_id,$token);
                }

                $res = ConfigModel::where('store_id', $store_id)->field('exp_time')->select()->toArray();
                if($res)
                {
                    $exp_time = intval($res[0]['exp_time']* 3600);
                }
                else
                {
                    $exp_time = 7200;
                }
                cache($token, $r[0], $exp_time);//添加新token数据

                //最后登录时间
                $now_time = date('Y-m-d H:i:s');
                //登录后更新，最后登录时间
                $sql1 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->find();
                $sql1->access_id = $token;
                $sql1->last_time = $now_time;
                if($clientid)
                {
                    $sql1->clientid = $clientid;
                }
                $sql1->save();

                // 在操作列表里添加一条会员登录信息
                $sql = new RecordModel();
                $sql->store_id = $store_id;
                $sql->user_id = $user_id;
                $sql->add_date = $now_time;
                $sql->event = $event;
                $sql->type = 0;
                $sql->save();

                if ($sql->id > 0)
                {
                    if ($rew)
                    {
                        SessionIdModel::destroy($rew);
                    }
                }

                $array = array('store_id'=>$store_id,'store_type'=>$store_type,'read_id'=>$user_id);
                $r_system = PC_Tools::GetAnnouncement($array);
                $tell_id = $r_system['tell_id'];
                $systemMsgTitle = $r_system['systemMsgTitle'];
                $systemMsg = $r_system['systemMsg'];
                $systemMsgStartDate = $r_system['systemMsgStartDate'];
                $systemMsgEndDate = $r_system['systemMsgEndDate'];
                $systemMsgType = $r_system['systemMsgType'];

                $storeCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>1,'id'=>0));
                $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>$preferred_currency));

                cache($token . '_currency',$preferred_currency); // 添加用户默认币种缓存
                
                Db::commit();

                $data = array('access_id' => $token,'userId' => $user_id, 'user_name' => $user_name, 'headimgurl' => $headimgurl, 'y_password' => $y_password, 'wx_status' => $wx_status,'tell_id'=>$tell_id,'systemMsg'=>$systemMsg,'systemMsgEndDate'=>$systemMsgEndDate,'systemMsgStartDate'=>$systemMsgStartDate,'systemMsgTitle'=>$systemMsgTitle,'systemMsgType'=>$systemMsgType,'info' => '登录成功','preferred_currency' => $preferred_currency,'storeCurrency' => $storeCurrency[0],'userCurrency' => $userCurrency[0],'user_lang_code' => $lang);
                $message = Lang('Success');
                return output(200,$message,$data);
            }
            catch (\Exception $e) 
            {
                // 回滚事务
                Db::rollback();
                $Log_content = $e->getMessage();
                $this->Log($Log_content);
                $message = Lang('Busy network');
                return output(ERROR_CODE_DLSJCW,$message);
            }
        }
        else
        { // 该手机号还未注册，去注册
            $zhanghao = Tools::generateRandomString(8);
            //开启事务
            Db::startTrans();
            try
            {
                // 根据商城id、账号，查询用户信息
                $r0 = UserModel::where(['store_id'=>$store_id,'zhanghao'=>$zhanghao])->select()->toArray();
                if ($r0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '手机号不正确!zhanghao:' . $zhanghao;
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('Wrong mobile number');
                    return output(ERROR_CODE_DLSJCW,$message);
                }
                else
                {
                    $exp_time = 7200;
                    $r1 = ConfigModel::where('store_id',$store_id);
                    if ($r1)
                    {
                        $wx_headimgurl = $r1[0]['wx_headimgurl'];//默认微信用户头像
                        $headimgurl = ServerPath::getimgpath($wx_headimgurl);//默认微信用户头像
                        $wx_name = $r1[0]['wx_name'];  //默认微信用户名
                        $user_id1 = $r1[0]['user_id']; //默认用户名ID前缀
                        $exp_time = intval($r1[0]['exp_time'] * 3600);
                    }

                    // 生成密钥
                    $token = Tools::getToken($db,$store_id);;

                    $ppid = '';
                    if (!empty($pid))
                    {
                        $r_0 = UserDistributionModel::where(['store_id'=>$store_id,'user_id'=>$pid])->select()->toArray();
                        if ($r_0)
                        {
                            $ppid = $pid;
                        }
                    }
                    //添加会员
                    $sql = new UserModel();
                    $sql->store_id = $store_id;
                    $sql->access_id = $token;
                    $sql->user_name = $wx_name;
                    $sql->headimgurl = $headimgurl;
                    $sql->cpc = $cpc;
                    $sql->country_num = $country_num;
                    $sql->mobile = $tel;
                    $sql->e_mail = $e_mail;
                    $sql->zhanghao = $zhanghao;
                    $sql->mima = '';
                    $sql->source = $store_type;
                    $sql->birthday = date("Y-m-d H:i:s");
                    if($ppid)
                    {
                        $sql->Referee = $ppid;
                    }
                    if($clientid)
                    {
                        $sql->clientid = $clientid;
                    }
                    $sql->save();

                    //更新user_id
                    $r = UserModel::where('store_id',$store_id)->field('id')->limit(1)->order('id','desc')->select()->toArray();
                    $rr = $r[0]['id'];
                    $user_id = $user_id1 . ($rr + 1);//新注册的用户user_id

                    $res_1 = UserModel::find($sql->id);
                    $res_1->user_id = $user_id;
                    $res_1->save();

                    // 分销插件
                    $res_plu = PluginsModel::where(['store_id'=>$store_id,'plugin_code'=>'distribution','status'=>1])->select()->toArray();
                    if($res_plu)
                    {   
                        // 获取分销配置
                        $res_con = DistributionConfigModel::where(['store_id'=>$store_id,'status'=>1,'relationship'=>0])->select()->toArray();
                        if($res_con)
                        {
                            $relationship = $res_con[0]['relationship'];
                            if($relationship != 1)
                            { // 注册确定绑定关系
                                if($ppid != '')
                                { // 注册绑定永久         
                                    $comm = new Commission();          
                                    $comm->create_level($user_id, 0, $ppid, $store_id);               
                                }  
                            }
                        }
                    }

                    $event = '会员' . $user_id . '注册成功';
                    // 在操作列表里添加一条会员登录信息
                    $RecordModel = new RecordModel();
                    $RecordModel->store_id = $store_id;
                    $RecordModel->user_id = $user_id;
                    $RecordModel->event = $event;
                    $RecordModel->type = 0;
                    $RecordModel->save();

                    //获取用户信息
                    $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();

                    cache($token, $res_a[0], $exp_time);//添加新token数据

                    $y_password = 0; // 没密码
                    if ($rew)
                    {
                        SessionIdModel::destroy($rew);
                    }
                    $this->update_user($store_id, $user_id, $access_id,$token);

                    $array = array('store_id'=>$store_id,'store_type'=>$store_type,'read_id'=>$user_id);
                    $r_system = PC_Tools::GetAnnouncement($array);
                    $tell_id = $r_system['tell_id'];
                    $systemMsgTitle = $r_system['systemMsgTitle'];
                    $systemMsg = $r_system['systemMsg'];
                    $systemMsgStartDate = $r_system['systemMsgStartDate'];
                    $systemMsgEndDate = $r_system['systemMsgEndDate'];
                    $systemMsgType = $r_system['systemMsgType'];

                    Db::commit();

                    $data = array('access_id' => $token, 'user_name' => $wx_name, 'headimgurl' => $headimgurl, 'y_password' => $y_password,'tell_id'=>$tell_id,'systemMsg'=>$systemMsg,'systemMsgEndDate'=>$systemMsgEndDate,'systemMsgStartDate'=>$systemMsgStartDate,'systemMsgTitle'=>$systemMsgTitle,'systemMsgType'=>$systemMsgType);
                    $message = Lang('Success');
                    return output(200,$message,$data);
                }
            }
            catch (\Exception $e) 
            {
                // 回滚事务
                Db::rollback();
                $Log_content = $e->getMessage();
                $this->Log($Log_content);
                $message = Lang('Busy network');
                return output(ERROR_CODE_DLSJCW,$message);
            } 
        }
    }

    // 用户登录
    public function login()
    {
        $store_id = Request::param('store_id'); // 商城ID
        $language = Request::param('language'); // 语言
        $store_type = Request::param('store_type'); // 来源
        $access_id = Request::param('access_id'); // 授权ID
        $clientid = Request::param('clientid'); //推送客户端ID
        $type = Request::param('type'); // 1.电话号码登录 2.邮箱 3.账号登录
        $cpc = Request::param('cpc'); // 电话区号
        $country_num = Request::param('country_num'); // 国家代码
        $tel = Request::param('phone'); // 电话号码
        $password = Request::param('password');//密码
        $pid = Request::param('pid');//推荐人id

        $lang = Tools::get_lang($language);

        if($type == 1)
        { // 手机号登录
            $sql_ = "select * from lkt_user where store_id = '$store_id' and cpc = '$cpc' and country_num = '$country_num' and mobile = '$tel' and user_id not in (select main_id from lkt_user_authority where type = 1) ";
        }
        else if($type == 2)
        { // 邮箱
            $sql_ = "select * from lkt_user where store_id = '$store_id' and e_mail = '$tel' and user_id not in (select main_id from lkt_user_authority where type = 1) ";
        }
        else
        { // 账号登录
            $sql_ = "select * from lkt_user where store_id = '$store_id' and zhanghao = '$tel' and user_id not in (select main_id from lkt_user_authority where type = 1) ";
        }
        $wx_status = 0;
        $re = Db::query($sql_);
        if (!empty($re))
        {
            if ($re[0]['mima'] == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户登录时，没有设置登录密码!res:' . json_encode($re);
                $this->Log($Log_content);
                $message = Lang('login.1');
                return output(ERROR_CODE_WSZMM,$message);
            }
            $password01 = Tools::unlock_url($re[0]['mima']);
            $user_id = $re[0]['user_id'];
            $user_name = $re[0]['user_name'];
            $headimgurl = $re[0]['headimgurl'];
            $old_token = $re[0]['access_id'];
            $cpc = $re[0]['cpc']; // ITU电话区号
            $country_num = $re[0]['country_num']; // 国家代码
            $lang = $re[0]['lang']; // 语言
            $preferred_currency = $re[0]['preferred_currency']; // 用户偏好货币id

            $event = '会员' . $user_id . '登录';
            if ($store_type == 1)
            {
                $wx_id = $re[0]['wx_id'];
                if ($wx_id != '')
                {
                    $wx_status = 1;
                }
            }

            if ($password01)
            {
                if ($password == $password01 && strlen($password) == strlen($password01))
                {
                    $token = Tools::getToken($store_id,$user_id);
                    if($old_token)
                    {
                        $res = Tools::verifyToken($old_token);
                        if($res)
                        {   
                            $token = $old_token;
                        }
                    }

                    //事务开启
                    Db::startTrans();
                    try
                    {   
                        //最后登录时间
                        $now_time = date('Y-m-d H:i:s');
                        if (empty($clientid))
                        {
                            $sql1 = "update lkt_user set access_id = '$token',last_time = '$now_time',lang = '$lang' where store_id = '$store_id' and user_id = '$user_id' ";
                        }
                        else
                        {
                            $sql1 = "update lkt_user set clientid = '$clientid',access_id = '$token',last_time = '$now_time',lang = '$lang' where store_id = '$store_id' and user_id = '$user_id' ";
                        }
                        $r1 = Db::execute($sql1);
                        if($r1 < 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '修改用户登录信息失败!sql:' . $sql1;
                            $this->Log($Log_content);
                        }
                        //添加用户登录记录失败
                        $RecordModel = new RecordModel;
                        $RecordModel->store_id = $store_id;
                        $RecordModel->user_id = $user_id;
                        $RecordModel->event = $event;
                        $RecordModel->add_date = $now_time;
                        $RecordModel->type = 0;
                        $RecordModel->save();
                        Db::commit();

                        // 用户信息存入redis
                        //获取用户信息
                        $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();

                        $res = ConfigModel::where('store_id', $store_id)->field('exp_time')->select()->toArray();
                        if($res)
                        {
                            $exp_time = intval($res[0]['exp_time']* 3600);
                        }
                        else
                        {
                            $exp_time = 7200;
                        }
                        cache($token, $res_a[0], $exp_time);//添加新token数据

                        $this->update_user($store_id, $user_id, $access_id,$token);
                    }
                    catch (\Exception $e) 
                    {
                        // 回滚事务
                        Db::rollback();
                        $Log_content = $e->getMessage();
                        $this->Log($Log_content);
                        $message = Lang('Busy network');
                        return output(ERROR_CODE_DLSJCW,$message);
                    }

                    $y_password = 1; // 有密码
                    //查询未支付订单
                    $sellOrderNum = OrderModel::where(['status'=>0,'user_id'=>$user_id])->count();

                    $array = array('store_id'=>$store_id,'store_type'=>$store_type,'read_id'=>$user_id);
                    $r_system = PC_Tools::GetAnnouncement($array);
                    $tell_id = $r_system['tell_id'];
                    $systemMsgTitle = $r_system['systemMsgTitle'];
                    $systemMsg = $r_system['systemMsg'];
                    $systemMsgStartDate = $r_system['systemMsgStartDate'];
                    $systemMsgEndDate = $r_system['systemMsgEndDate'];
                    $systemMsgType = $r_system['systemMsgType'];

                    $storeCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>1,'id'=>0));
                    $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>$preferred_currency));

                    cache($token . '_currency',$preferred_currency); // 添加用户默认币种缓存

                    $data = array('access_id' => $token,'country_num' => $country_num,'cpc' => $cpc,'headimgurl' => $headimgurl,'info' => '登录成功','lang' => $lang,'preferred_currency' => $preferred_currency,'sellOrderNum' => $sellOrderNum,'storeCurrency' => $storeCurrency[0],'systemMsg' => $systemMsg,'systemMsgEndDate' => $systemMsgEndDate,'systemMsgStartDate' => $systemMsgStartDate,'systemMsgTitle' => $systemMsgTitle,'systemMsgType' => $systemMsgType,'tell_id' => $tell_id,'token' => '','userCurrency' => $userCurrency[0],'user_id' => $user_id,'user_name' => $user_name,'wx_status' => $wx_status,'y_password' => $y_password);

                    $message = Lang('Success');
                    return output(200,$message,$data);
                }
                else
                {
                    $message = Lang('Password error');
                    return output(ERROR_CODE_ZHHMMCWQZXSR,$message);
                }
            }
            else
            {
                $message = Lang('login.2');
                return output(ERROR_CODE_WSZMM,$message);
            }
        }
        else
        {
            $message = Lang('login.3');
            return output(ERROR_CODE_GSJHMWZC,$message);
        }  
    }

    // 忘记密码,验证账号是否存在
    public function forget_zhanghao()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id

        $type = Request::param('type'); // 类型 0.手机号 1.邮箱
        $cpc = Request::param('cpc'); // 区号
        $country_num = Request::param('country_num'); // 国家代码
        $zhanghao = Request::param('zhanghao'); // 账号

        if($type == 1)
        {
            $where_array = array('store_id'=>$store_id,'e_mail'=>$zhanghao);
            $re = UserModel::where($where_array)->field('e_mail')->select()->toArray(); 
        }
        else
        {
            $where_array = array('store_id'=>$store_id,'cpc'=>$cpc,'country_num'=>$country_num,'mobile'=>$zhanghao);
            $re = UserModel::where($where_array)->field('mobile')->select()->toArray(); 
        }
        if (!empty($re))
        {
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $message = Lang('login.17');
            return output(ERROR_CODE_ZHBCZ_001,$message);
        }
    }

    // 忘记密码,验证验证码是否正确，并修改密码
    public function forget_code()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id

        $type = Request::param('type'); // 类型 0.手机号 1.邮箱
        $cpc = Request::param('cpc'); // 区号
        $country_num = Request::param('country_num'); // 国家代码
        $tel = Request::param('tel'); // 手机号码
        $e_mail = Request::param('e_mail'); // 邮箱
        $keyCode = trim(Request::param('keyCode')); // 验证码
        $password = Tools::lock_url(Request::param('password')); // 重置密码
        Db::startTrans();
        if($type == 1)
        {
            $arr = array($e_mail, array('code' => $keyCode));
            $re = UserModel::where(['store_id'=>$store_id,'e_mail'=>$e_mail])->select()->toArray();
        }
        else
        {
            $arr = array($cpc.$tel, array('code' => $keyCode));
            $re = UserModel::where(['store_id'=>$store_id,'cpc'=>$cpc,'country_num'=>$country_num,'mobile'=>$tel])->select()->toArray();
        }
        $Tools = new Tools($store_id, 1);
        $rew = $Tools->verification_code($arr);

        if (!empty($re))
        {
            if($type == 1)
            {
                $where_array = array('store_id'=>$store_id,'e_mail'=>$e_mail);
            }
            else
            {
                $where_array = array('store_id'=>$store_id,'cpc'=>$cpc,'country_num'=>$country_num,'mobile'=>$tel);
            }
            $sql = UserModel::where($where_array)->find();
            $sql->mima = $password;
            $r = $sql->save();
            if ($r)
            {
                if ($rew)
                {   
                    $SessionIdModel = SessionIdModel::find($rew);
                    $r2 = $SessionIdModel->delete();
                    if(!$r2)
                    {
                        Db::rollback();
                        $Log_content = __METHOD__ . '->' . __LINE__ . '删除验证码失败!id:' . $rew;
                        $this->Log($Log_content);
                        $message = Lang('Busy network');
                        return output(ERROR_CODE_WLYC,$message);
                    }
                }
                Db::commit();
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . '重置密码失败!参数:' . json_encode($where_array);
                $this->Log($Log_content);
                $message = Lang('Busy network');
                return output(ERROR_CODE_MMXGSB,$message);
            }
        }
        else
        {
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . '忘记密码,手机号或邮箱不正确!';
            $this->Log($Log_content);
            $message = Lang('login.17');
            return output(ERROR_CODE_GSJHMWZC,$message);
        }
    }

    // 忘记密码重置密码（新逻辑，这一步已挪移到上一步）
    public function forgotpassword()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $tel = Request::param('phone'); // 手机号码
        $password = Tools::lock_url(Request::param('password')); // 重置密码

        $re = UserModel::where(['store_id'=>$store_id,'mobile'=>$tel])->select()->toArray();
        if (!empty($re))
        {
            $sql = UserModel::where(['store_id'=>$store_id,'mobile'=>$tel])->find();
            $sql->mima = $password;
            $r = $sql->save();
            if ($r)
            {
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '重置密码成功!tel:' . $tel;
                $this->Log($Log_content);
                $message = Lang('Busy network');
                return output(ERROR_CODE_MMXGSB,$message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '忘记密码,手机号不正确!';
            $this->Log($Log_content);
            $message = Lang('login.17');
            return output(ERROR_CODE_GSJHMWZC,$message);
        }
    }

    public function update_user($store_id, $user_id, $token,$n_token)
    {
        if($token == '')
        {
            return;
        }
        // 根据商城ID、token，查询购物车信息
        $r0 = CartModel::where(['store_id'=>$store_id,'token'=>$token])
                       ->field('id,Goods_id,Size_id,Goods_num')
                       ->select()
                       ->toArray();
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $cart_id = $v['id']; //购物车id
                $Goods_id = $v['Goods_id']; //商品id
                $Size_id = $v['Size_id']; // 属性id
                $Goods_num = $v['Goods_num']; // 数量
                // 根据商城id、用户id、商品id、属性id，查询用户购物车信息
                $r1 = CartModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'Goods_id'=>$Goods_id,'Size_id'=>$Size_id])
                               ->field('id,Goods_num')
                               ->select()
                               ->toArray();
                if ($r1)
                { // 存在 表示用户购物车有该商品
                    $id = $r1[0]['id']; // 购物车id
                    $Goods_num1 = $r1[0]['Goods_num'];// 数量
                    // 根据商品id、属性id,查询属性库存
                    $r2 = ConfigureModel::where(['id'=>$Size_id,'pid'=>$Goods_id])->field('num')->select()->toArray();
                    if ($r2)
                    {
                        $num = $r2[0]['num']; // 库存
                        if ($Goods_num + $Goods_num1 >= $num)
                        { // 没登录时购物车数量 + 登入后已存在的购物车数量 >= 库存剩余数量
                            $cart_num = $num;
                        }
                        else
                        {
                            $cart_num = $Goods_num + $Goods_num1;
                        }
                        // 根据商城ID、用户ID、购物车ID，修改购物车数量
                        $sql4 = CartModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'id'=>$id])->find();
                        $sql4->Goods_num = $cart_num;
                        $r4 = $sql4->save();
                        if(!$r4){
                            $Log_content = __METHOD__ . '->' . __LINE__ . '用户登录成功，修改购物车信息失败!id:' . $id;
                            $this->Log($Log_content);
                        }
                        // 根据商城ID、购物车ID，删除购物车信息
                        $sql5 = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id])->find();
                        $r5 = $sql5->delete();
                        if(!$r5){
                            $Log_content = __METHOD__ . '->' . __LINE__ . '用户登录成功，删除购物车信息失败!cart_id:' . $cart_id;
                            $this->Log($Log_content);
                        }
                    }
                }
                else
                { // 不存在 表示用户购物车没有该商品
                    // 根据商城ID、token、购物车ID，修改购物车用户ID
                    $r6 = Db::name('cart')->where(['store_id'=>$store_id,'token'=>$token,'Goods_id'=>$Goods_id])
                                          ->data(['user_id'=>$user_id,'token'=>$n_token])
                                          ->update();
                    if($r6 < 0){
                        $Log_content = __METHOD__ . '->' . __LINE__ . '用户登录成功，修改购物车信息失败!sql:' . $sql6;
                        $this->Log($Log_content);
                    }
                }
            }
        }

        // 根据商城ID、token，修改店铺浏览记录表
        $r3 = Db::name('mch_browse')->where(['store_id'=>$store_id,'token'=>$token])
                                    ->data(['user_id'=>$user_id,'token'=>$n_token])
                                    ->update();
        if($r3 < 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '用户登录成功，修改店铺浏览记录失败!sql:' . $sql3;
            $this->Log($Log_content);
        }

        return;
    }

    // 判断token是否存在或是否失效
    public function token()
    {
        $store_id = addslashes(trim(Request::param('store_id'))); // 商城id
        $language = trim(Request::param('language')); // 语言

        $store_type = trim(Request::param('store_type'));
        $access_id = trim(Request::param('access_id')); // 授权ID
        if (!empty($access_id))
        { // 存在
            $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组
            if ($getPayload_test == false)
            { // 过期
                if ($store_type == 1)
                { // 当为小程序时
                    // 根据商城ID、授权ID，查询用户信息
                    $r1 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
                    if ($r1)
                    { // 存在
                        // 生成密钥
                        $token = Tools::getToken($store_id,$store_type);
                        $sql2 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                        $sql2->access_id = $token;
                        $r2 = $sql2->save();
                        if(!$r2)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '修改用户access_id失败!access_id:' . $access_id;
                            $this->Log($Log_content);
                            $message = Lang('Please log in');
                            return output(ERROR_CODE_QDL,$message);
                        }
                        else
                        {
                            $data = array('login_status' => 1, 'access_id' => $token);
                            $message = Lang('Success');
                            return output(200,$message,$data);
                        }
                    }
                    else
                    { // 不存在
                        $message = Lang('Please log in');
                        return output(ERROR_CODE_QDL,$message);
                    }
                }

                $sql3 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
                if($sql3)
                {
                    $sql3->access_id = '';
                    $r3 = $sql3->save();
                    if(!$r3)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '修改用户access_id失败!access_id:' . $access_id;
                        $this->Log($Log_content);
                    }
                }
                $data = array('login_status' => 0);
                $message = Lang('Success');
                return output(200,$message,$data);
            }
            else
            {
                // 根据商城ID、授权ID，查询用户信息
                $r1 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('id')->select()->toArray();
                if ($r1)
                { // 存在
                    $data = array('login_status' => 1);
                    $message = Lang('Success');
                    return output(200,$message,$data);
                }
                else
                { // 不存在
                    $data = array('login_status' => 0);
                    $message = Lang('Success');
                    return output(200,$message,$data);
                }
            }
        }
        else
        {
            $message = Lang('Please log in');
            return output(ERROR_CODE_QDL,$message);
        }
    }

    //退出登录
    public function quit()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权ID

        $sql_0 = "select user_id from lkt_user where store_id = '$store_id' and access_id = '$access_id' ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $user_id = $r_0[0]['user_id'];
        }
        else
        {
            $message = Lang('Busy network');
            return output(ERROR_CODE_WLYC,$message);
        }

        // 根据商城ID、token，修改会员列表
        $sql3 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->find();
        if(!$sql3)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '退出失败!access_id:' . $access_id;
            $this->Log($Log_content); 
            $message = Lang('Busy network');
            return output(ERROR_CODE_WLYC,$message);
        } 
        $sql3->access_id = '';
        $r3 = $sql3->save();
        if(!$r3)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '修改用户access_id失败!access_id:' . $access_id;
            $this->Log($Log_content);

            $message = Lang('Busy network');
            return output(ERROR_CODE_WLYC,$message);
        }
        else
        {
            cache($access_id,NULL);
            Db::name('cart')->where(['store_id'=>$store_id,'token'=>$access_id])->update(['token'=>'']);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    /**
     * 更新购物车信息
     * @param $token
     * @param $db
     * @param $lkt_user_id
     */
    public function updateCartInfo($token, $store_id, $lkt_user_id,$n_token)
    {
        $r = CartModel::where('token',$token)->count();
        if ($r > 0)
        {
            $sql = CartModel::update(['user_id'=>$lkt_user_id,'token'=>$n_token],['token'=>$token]);
            if (!$sql)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '更新购物车信息失败!user_id:' . $user_id;
                $this->Log($Log_content);
                $message = Lang('login.8');
                return output(ERROR_CODE_GXGWCSB,$message);
            }
        }
    }

    /**
     * 获取用户userid 前缀
     * @param $store_id
     * @param $db
     * @return string
     */
    public function getPreUserId($store_id)
    {
        $user_id = "user";
        $r0 = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r0)
        {
            $user_id = $r0[0]['user_id']; //默认用户名ID前缀
        }
        return $user_id;
    }

    /**
     * 获取token
     * @param $access_id
     * @param $db
     * @param $store_id
     * @param $store_type
     * @return bool|string
     */
    public function getToken($access_id, $store_id, $store_type)
    {
        if (empty($access_id) || "undefined" == $access_id)
        { // 授权ID为空,代表没有进入商品详情
            $token = Tools::getToken($store_id,$store_type);
        }
        else
        {
            // 授权ID存在,代表有进入商品详情
            $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组
            if ($getPayload_test == false)
            { // 过期
                // 生成密钥
                $token = Tools::getToken($store_id,$store_type);
            }
        }
        return $token;
    }
    
    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/login.log",$Log_content);
        return;
    }

    //获取微信用户手机号
    public function getWxInfo()
    {
        $store_id = Request::param('store_id'); // 商城ID

        $openid = trim(Request::param('openid'));
        $encryptedData = trim(Request::param('encryptedData'));
        $iv = trim(Request::param('iv'));

        //获取appid
        $res = ConfigModel::where('store_id', $store_id)->field('appid')->select()->toArray();
        if($res)
        {
            $appid = $res[0]['appid'];
        }
        else
        {
            $message = '小程序后台配置有误';
            return output(ERROR_CODE_PZBCZ,$message);
        }

        require_once('../app/common/wxinfo/wxBizDataCrypt.php');
        header("Content-Type:text/html;charset=utf8"); 
        header("Access-Control-Allow-Origin: *"); //解决跨域
        header('Access-Control-Allow-Methods:POST');// 响应类型  
        header('Access-Control-Allow-Headers:*'); // 响应头设置 
        $data = '';

        $sk = cache('sessionKey_'.$openid);//添加新token数据

        $pc = new WXBizDataCrypt($appid, $sk);
        $errCode = $pc->decryptData($encryptedData, $iv, $data );

        if ($errCode == 0) 
        {
            $res = json_decode($data,true);
            $message = Lang('Success');
            return output('200',$message,array('phoneNumber'=>$res['phoneNumber']));
            print($data . "\n");
        } 
        else 
        {
            print($errCode . "\n");
        }
    }

    //获取微信公众号支付，支付宝支付配置
    public function getAuthConfig()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id

        $is_wx = 0;//微信公众号配置是否已配置
        $is_alipay = 0;//支付宝配置是否已配置
        $wx_arr = array();
        $alipay_arr = array();


        //获取微信公众号支付的appid,appsecret
        $sql1 = "select config_data from lkt_payment as p left join lkt_payment_config as c on c.pid = p.id where c.status = 1 and p.class_name = 'jsapi_wechat' and  c.store_id = '$store_id' ";
        $res1 = Db::query($sql1);
        if($res1)
        {
            $r0 = json_decode($res1[0]['config_data'],true);
            $wx_arr['appid'] = $r0['appid'];
            $wx_arr['appsecret'] = $r0['appsecret'];
            if($wx_arr['appid'])
            {
                $is_wx = 1;
            }
        }

        //获取支付宝配置
        $sql2 = "select config_data from lkt_payment as p left join lkt_payment_config as c on c.pid = p.id where c.status = 1 and p.class_name = 'alipay' and  c.store_id = '$store_id' ";
        $res2 = Db::query($sql2);
        if($res2)
        {
            $r1 = json_decode($res2[0]['config_data'],true);
            $alipay_arr['appid'] = $r1['appid'];
            if($alipay_arr['appid'])
            {
                $is_alipay = 1;
            }
        }

        $data = array('is_wx' => $is_wx, 'is_alipay' => $is_alipay, 'wx_arr' => $wx_arr, 'alipay_arr' => $alipay_arr);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    //获取微信公众号支付，支付宝支付配置
    public function getWxAppId()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id

        $is_wx = 0;//微信公众号配置是否已配置
        $is_alipay = 0;//支付宝配置是否已配置
        $wx_arr = array();
        $alipay_arr = array();

        $appid = '';
        //获取微信公众号支付的appid,appsecret
        $sql1 = "select config_data from lkt_payment as p left join lkt_payment_config as c on c.pid = p.id where c.status = 1 and p.class_name = 'jsapi_wechat' and  c.store_id = '$store_id' ";
        $res1 = Db::query($sql1);
        if($res1)
        {
            $r0 = json_decode($res1[0]['config_data'],true);
            $wx_arr['appid'] = $r0['appid'];
            $wx_arr['appsecret'] = $r0['appsecret'];
            $appid = $r0['appid'];
            if($wx_arr['appid'])
            {
                $is_wx = 1;
            }
        }

        $data = array('is_wx' => $is_wx, 'is_alipay' => $is_alipay, 'wx_arr' => $wx_arr, 'alipay_arr' => $alipay_arr,'appId'=>$appid);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    //获取H5支付宝授权令牌
    public function aliUserLoginByWeb()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源

        $rep = [
            '%21' =>'!',
            '%2A' =>'*', 
            '%28' =>'(', 
            '%29' =>')', 
            '+'  =>'%20'
        ];
        
        $config = LKTConfigInfo::getPayConfig($store_id,'alipay');
        $appId = $config["appid"];
        $state = base64_encode('laiketui');
        $data['url'] = '';
        if($store_type == 7)
        {
            $sql = "select H5_domain from lkt_config where store_id = '$store_id'";
            $rr = Db::query($sql);
            $h5Url = "https://seo.houjiemeishi.com/H5/#/";
            if (!empty($rr[0]['H5_domain']))
            {
                $h5Url = $rr[0]['H5_domain'];
            }
            $redirect_uri = urlencode($h5Url."pages/tabBar/my");
            $url = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=".$appId."&scope=auth_user&redirect_uri=".$redirect_uri."&state=".$state."";
            $result_url = "alipays://platformapi/startapp?appId=20000067&url=".str_replace(array_keys($rep), array_values($rep), urlencode($url));
            $data['url'] = $result_url;
        }
        elseif($store_type == 2)
        {   
            $sql = "select endurl from lkt_third ";
            $rr = Db::query($sql);
            // $return_url = "laiketui://pages/tabBar/my";
            $endurl = "https://seo.houjiemeishi.com/za.html";
            // if (!empty($rr[0]['endurl']))
            // {
            //     $endurl = $rr[0]['endurl']."za.html";
            // }
            $redirect_uri = urlencode($endurl);
            $url = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=".$appId."&scope=auth_user&redirect_uri=".$redirect_uri."&state=".$state."";
            // $result_url = TestImage::getAuth($store_id,$return_url);
            $data['url'] = $url;
            $message = Lang('Success');
            return output(200,$message,$url);
        }
        $message = Lang('Success');
        return output(200,$message,$data);
    }


    // 支付宝用户授权时，存储用户信息
    public function aliUserLoginApp()
    {

        $store_id = addslashes(trim(Request::param('store_id'))); // 商城id
        $store_type = trim(Request::param('store_type')); // 来源
        $code = addslashes(trim(Request::param('authCode'))); // code
        $pid = trim(Request::param('pid')); // 推荐人id
        $access_id = trim(Request::param('access_id')); // 推荐人id

        //获取用户openid
        if (!$code)
        {
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
        
        //获取微信公众号支付的appid,appsecret
        $sql2 = "select config_data from lkt_payment as p left join lkt_payment_config as c on c.pid = p.id where c.status = 1  and p.class_name = 'alipay' and  c.store_id = '$store_id' ";
        $res2 = Db::query($sql2);
        if ($res2)
        {   
            $r0 = json_decode($res2[0]['config_data'],true);
            $appid = $r0['appid'];
        }
        else
        {   

            $message = Lang('login.0');
            return output(228,$message);
        }
        if (!$appid)
        {
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
        $token = TestImage::getToken($store_id,$code);
        if(!$token)
        {
            $message = Lang('login.8');
            return output(115,$message);
        }
        $ali_user = TestImage::getUser($store_id,$token);
        file_put_contents('alipayuser.php', print_r($ali_user,true));
        $alipay_userid = $ali_user->user_id;//支付宝会员id
        $user_name = $ali_user->nick_name;//支付宝昵称
        $headimgurl = $ali_user->avatar;//支付宝头像
        $sex = 0;
        $r0 = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r0)
        {
            $user_id1 = $r0[0]['user_id']; //默认用户名ID前缀
            $is_register = $r0[0]['is_register'];
            if (empty($user_name))
            {
                $user_name = $r0[0]['wx_name'];  //默认微信用户名
            }
            if (empty($headimgurl))
            {
                $headimgurl = ServerPath::getimgpath($r0[0]['wx_headimgurl']);//默认微信用户头像
            }
            // if ($is_register == 2)
            // { // 当注册为免注册，并且来源为小程序

                $token = Tools::getToken($store_id,$store_type);
                //判断是否有该用户信息
                $res_1 = UserModel::where(['store_id'=>$store_id,'zfb_id'=>$alipay_userid])->field('user_id,access_id')->select()->toArray();
                $strs="QWERTYUIOPASDFGHJKLZXCVBNM1234567890";
                $user_id='LKT-'.substr(str_shuffle($strs),mt_rand(0,strlen($strs)-9),8);
                // 用户是否第一次授权登录 0 不是 1 是
                $user_first_auth = 0;
                //事务开启
                Db::startTrans();
                try
                { 
                    if ($res_1)
                    {    
                        //存在则更新
                        $user_id = $res_1[0]['user_id'];
                        $old_token = $res_1[0]['access_id'];//原令牌
                        cache($old_token, NULL);//清理原token数据
                        $UserModel = UserModel::where(['store_id'=>$store_id,'zfb_id'=>$alipay_userid])->find();
                        $UserModel->access_id = $token;
                        $UserModel->last_time = date('Y-m-d H:i:s');
                        if (!empty($user_name))
                        {
                            $UserModel->user_name = $user_name;
                        }

                        if (!empty($headimgurl))
                        {
                            $UserModel->headimgurl = $headimgurl;
                        }

                        if (!empty($sex))
                        {
                            $UserModel->sex = $sex;
                        }
                        $UserModel->save();  
                    }
                    else
                    {
                        $user_first_auth = 1;
                        //不存在，则添加
                        $ppid = '';
                        if (!empty($pid))
                        {
                            $r_0 = UserDistributionModel::where(['user_id'=>$pid,'store_id'=>$store_id])->field('id')->select()->toArray();
                            if ($r_0)
                            {
                                $ppid = $pid;
                            }
                        }
                        else
                        {
                            $res_de = UserModel::where('store_id',$store_id)->field('user_id')->limit(1)->order('id','asc')->select()->toArray();
                            if($res_de)
                            {
                                $ppid = $res_de[0]['user_id'];
                            }   
                        }
                        $mima = Tools::lock_url('000000');
                        //添加用户数据
                        $UserModel = new UserModel();
                        $UserModel->store_id = $store_id;
                        $UserModel->access_id = $token;
                        $UserModel->user_name = $user_name;
                        $UserModel->zfb_id = $alipay_userid;
                        $UserModel->headimgurl = $headimgurl;
                        $UserModel->sex = $sex;
                        $UserModel->zhanghao = $user_id;
                        $UserModel->mima = $mima;
                        $UserModel->source = $store_type;
                        $UserModel->birthday = date("Y-m-d H:i:s");
                        if($ppid)
                        {
                            $UserModel->Referee = $ppid;
                        }
                        $UserModel->save();

                        //更新user_id
                        $r = UserModel::where('store_id',$store_id)->field('id')->limit(1)->order('id','desc')->select()->toArray();
                        $rr = $r[0]['id'];
                        $user_id = $user_id1 . ($rr + 1);//新注册的用户user_id

                        $res_1 = UserModel::find($rr);
                        $res_1->user_id = $user_id;
                        $res_1->save();

                        // 分销插件
                        $res_plu = PluginsModel::where(['store_id'=>$store_id,'plugin_code'=>'distribution','status'=>1])->select()->toArray();
                        if($res_plu)
                        {   
                            // 获取分销配置
                            $res_con = DistributionConfigModel::where(['store_id'=>$store_id,'status'=>1,'relationship'=>0])->select()->toArray();
                            if($res_con)
                            {
                                $relationship = $res_con[0]['relationship'];
                                if($relationship != 1)
                                { // 注册确定绑定关系
                                    if($ppid != '')
                                    { // 注册绑定永久         
                                        $comm = new Commission();          
                                        $comm->create_level($user_id, 0, $ppid, $store_id);               
                                    }  
                                }
                            }
                        }

                        $event = '会员' . $user_id . '授权注册成功';
                        // 在操作列表里添加一条会员登录信息
                        $RecordModel = new RecordModel();
                        $RecordModel->store_id = $store_id;
                        $RecordModel->user_id = $user_id;
                        $RecordModel->event = $event;
                        $RecordModel->type = 0;
                        $RecordModel->save();
                    }
                    // 用户信息存入redis
                    //获取用户信息
                    $res_a = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                                      ->select()
                                      ->toArray();
                    $res = ConfigModel::where('store_id', $store_id)
                                      ->field('exp_time')
                                      ->select()
                                      ->toArray();
                    if($res)
                    {
                        $exp_time = intval($res[0]['exp_time']* 3600);
                    }
                    else
                    {
                        $exp_time = 7200;
                    }
                    cache($token, $res_a[0], $exp_time);//添加新token数据

                    $this->update_user($store_id, $user_id, $access_id,$token);
                    Db::commit();
                }
                catch (\Exception $e) 
                {
                    // 回滚事务
                    Db::rollback();
                    $Log_content = $e->getMessage();
                    $this->Log($Log_content);
                    $message = Lang('login.8');
                    echo json_encode(array('code'=>ERROR_CODE_SQSB,'message'=>$message));
                    exit;
                }
            // }
            // else
            // {
            //     $message = Lang('login.7');
            //     echo json_encode(array('code'=>ERROR_CODE_QWSXTSZ,'message'=>$message));
            //     exit;
            // }
            
            $user = array();
            $user['user_name'] = $user_name;
            $user['headimgurl'] = $headimgurl;
            $user['user_id'] = $user_id;
            $user['access_id'] = $token;

            $Log_content = __METHOD__ . '->' . __LINE__ . '授权成功!';
            $this->Log($Log_content);
            $data = array('access_id' => $token,'user_first_auth'=>$user_first_auth,'user' => $user, 'zfb_id' => $alipay_userid);
            $message = Lang('Success');
            echo json_encode(array('code'=>200,'message'=>$message,'data'=>array('userInfo'=>$user)));
            exit;
        }
        else
        {
            $message = Lang('login.0');
            echo json_encode(array('code'=>ERROR_CODE_PZBCZ,'message'=>$message));
            exit;
        }
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
}