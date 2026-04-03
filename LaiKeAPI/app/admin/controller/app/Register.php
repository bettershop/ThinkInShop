<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\LKTConfigInfo;
use app\common\lktpay\ttpay\TTUtils;
use app\common\lktpay\bdpay\BDUtils;
use app\common\alipay\AlipayTools;

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
 * 功能：移动端验证码登录类
 * 修改人：PJY
 */
class Register 
{
    // 验证码登录
    public function login()
    {
        $store_id = trim(Request::param('store_id'));
        $language = trim(Request::post('language')); // 语言
        $store_type = trim(Request::param('store_type'));
        $access_id = trim(Request::post('access_id')); // 授权ID
        $clientid = trim(Request::post('clientid')); // 推送客户ID

        $type = Request::param('type'); // 类型 1.手机号 2.邮箱
        $cpc = Request::param('cpc'); // 区号
        $country_num = Request::param('country_num'); // 国家代码
        $tel = Request::post('phone'); // 手机号码
        $keyCode = trim(Request::post('keyCode')); // 验证码

        $pid = trim(Request::post('pid')); // 推荐人id

        if($type == 1)
        {
            $arr = array($cpc.$tel, array('code' => $keyCode));
        }
        else
        {
            $arr = array($tel, array('code' => $keyCode));
        }

        $Tools = new Tools($store_id, 1);
        $rew = $Tools->verification_code($arr);
        $wx_status = 0;

        if($type == 1)
        {
            // 根据商城id、手机号，查询用户信息
            $r = UserModel::where(['store_id'=>$store_id,'cpc'=>$cpc,'country_num'=>$country_num,'mobile'=>$tel])->select()->toArray();
        }
        else
        {
            // 根据商城id、手机号，查询用户信息
            $r = UserModel::where(['store_id'=>$store_id,'e_mail'=>$tel])->select()->toArray();
        }
        if ($r)
        {
            //开启事务
            Db::startTrans();
            try
            { // 存在
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
                Db::commit();
                // 用户信息存入redis
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
                cache($token, $r[0], $exp_time);//添加新token数据

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

                cache($token . '_currency',$preferred_currency, $exp_time); // 添加用户默认币种缓存

                $data = array('access_id' => $token,'country_num' => $country_num,'cpc' => $cpc,'headimgurl' => $headimgurl,'info' => '登录成功','lang' => $lang,'preferred_currency' => $preferred_currency,'sellOrderNum' => $sellOrderNum,'storeCurrency' => $storeCurrency[0],'systemMsg' => $systemMsg,'systemMsgEndDate' => $systemMsgEndDate,'systemMsgStartDate' => $systemMsgStartDate,'systemMsgTitle' => $systemMsgTitle,'systemMsgType' => $systemMsgType,'tell_id' => $tell_id,'token' => '','userCurrency' => $userCurrency[0],'user_id' => $user_id,'user_name' => $user_name,'wx_status' => $wx_status,'y_password' => $y_password);

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
        {   
            $storeCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>1,'id'=>0));
            $preferred_currency = $storeCurrency[0]['preferred_currency'];
            //开启事务
            Db::startTrans();
            try
            {
                $zhanghao = Tools::generateRandomString(8);

                $exp_time = 7200;
                $r1 = ConfigModel::where('store_id',$store_id)->select()->toArray();
                if ($r1)
                {
                    $wx_headimgurl = $r1[0]['wx_headimgurl'];//默认微信用户头像
                    $headimgurl = ServerPath::getimgpath($wx_headimgurl);//默认微信用户头像
                    $wx_name = $r1[0]['wx_name'];  //默认微信用户名
                    $user_id1 = $r1[0]['user_id']; //默认用户名ID前缀
                    $exp_time = intval($r1[0]['exp_time'] * 3600);
                }

                // 生成密钥
                $token = Tools::getToken($store_id,$store_type);

                $ppid = $pid;

                //添加会员
                $sql = new UserModel();
                $sql->store_id = $store_id;
                $sql->access_id = $token;
                $sql->user_name = $wx_name;
                $sql->headimgurl = $headimgurl;
                $sql->zhanghao = $zhanghao;
                $sql->mima = '';
                $sql->source = $store_type;
                $sql->preferred_currency = $preferred_currency;
                if($type == 1)
                {
                    $sql->cpc = $cpc;
                    $sql->country_num = $country_num;
                    $sql->mobile = $tel;
                }
                else
                {
                    $sql->e_mail = $tel;
                }
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
                Db::commit();

                $array = array('store_id'=>$store_id,'store_type'=>$store_type,'read_id'=>$user_id);
                $r_system = PC_Tools::GetAnnouncement($array);
                $tell_id = $r_system['tell_id'];
                $systemMsgTitle = $r_system['systemMsgTitle'];
                $systemMsg = $r_system['systemMsg'];
                $systemMsgStartDate = $r_system['systemMsgStartDate'];
                $systemMsgEndDate = $r_system['systemMsgEndDate'];
                $systemMsgType = $r_system['systemMsgType'];

                cache($token . '_currency',$preferred_currency, $exp_time); // 添加用户默认币种缓存

                $data = array('access_id' => $token,'country_num' => $country_num,'cpc' => $cpc,'headimgurl' => $headimgurl,'info' => '登录成功','lang' => $lang,'preferred_currency' => $preferred_currency,'sellOrderNum' => 0,'storeCurrency' => $storeCurrency[0],'systemMsg' => $systemMsg,'systemMsgEndDate' => $systemMsgEndDate,'systemMsgStartDate' => $systemMsgStartDate,'systemMsgTitle' => $systemMsgTitle,'systemMsgType' => $systemMsgType,'tell_id' => $tell_id,'token' => '','userCurrency' => $storeCurrency[0],'user_id' => $user_id,'user_name' => $wx_name,'wx_status' => $wx_status,'y_password' => $y_password);

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
            if (!$res_update_cart)
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

    public function update_user($store_id, $user_id, $token,$n_token)
    {
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

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/login.log",$Log_content);
        return;
    }

}