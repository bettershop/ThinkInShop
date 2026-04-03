<?php
namespace app\admin\controller\admin\saas;

use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Jurisdiction;
use app\admin\model\AdminModel;
use app\admin\model\CustomerModel;
use app\admin\model\RecordModel;
use app\admin\model\UploadSetModel;
/**
 * 功能：后台登录类
 * 修改人：PJY
 */
class User
{
    public function login()
    {
        $customer_number = addslashes(Request::param('customerNumber'));
        $name = addslashes(Request::param('userName'));

        //是否有自营店
        $haveStoreMchId = true;
        //默认为0
        $defaultStoreId = 0;
        // 获取输入的密码
        $password = md5(Request::param('pwd'));
        if (!Request::has('userName')|| !Request::has('pwd'))
        {
            $message = Lang('user.22');
            return output(400,$message,null);
        }
        $day = '';
        $result = null;
        if ($customer_number && $customer_number != 'null')
        {
            // 客户编号不为空，不是系统管理员
            $imgCodeToken = addslashes(Request::param('imgCodeToken'));
            $imgCode = addslashes(Request::param('imgCode'));

            // 获取验证码
            $cacheCode = cache($imgCodeToken);
            $cacheImg = cache($imgCodeToken.'img');
            if (file_exists($cacheImg))
            {
                unlink($cacheImg);
            }

            if (!$imgCodeToken || !$imgCode)
            {
                $message = Lang('user.26');
                return output(400,$message,null);
            }
            if (strtolower($cacheCode) != strtolower($imgCode))
            {
                $message = Lang('user.26');
                return output(400,$message,null);
            }
            // 查询客户编号是否存在
            $r = CustomerModel::where('customer_number', $customer_number)->where('recycle',0)->select()->toArray();
            if (empty($r))
            {
                $message = Lang('login.18');//请输入正确的商户编号！
                return output(400,$message,null);
            }
            else
            {
                $store_id1 = $r[0]['id']; // 商城id
                // 根据商城id、管理员账号，查询管理员信息
                $result = AdminModel::where('name', $name)
                    ->where('recycle',0)
                    ->where('store_id',$store_id1)
                    ->field('id,name,password,admin_type,permission,type,status,store_id,login_num,shop_id,token,role')
                    ->select()
                    ->toArray();
                if ($result == false)
                {
                    $message = Lang('supplier.34');
                    return output(400,$message,null);
                }
                $shop_id = $result[0]['shop_id']; //店主id
                $defaultStoreId = $store_id1;
            }
        }
        else
        {
            // 客户编号为空,系统管理员
            // 根据管理员账号，查询管理员信息

            $result = AdminModel::where(['name'=> $name,'recycle'=>0,'store_id'=>0])
                ->field('id,name,password,admin_type,permission,type,status,store_id,login_num,shop_id,token,role')
                ->select()
                ->toArray();
            if ($result == false)
            {
                $message = Lang('supplier.34');
                return output(400,$message,null);
            }
            $shop_id = $result[0]['shop_id']; // 店主id
            $sql_m = "select b.shop_id,a.id from lkt_customer a left join lkt_admin as b on a.admin_id = b.id where a.status = 0 and a.recycle = 0 and a.is_default = 1";
            $res_m = Db::query($sql_m);
            if($res_m)
            {
                $shop_id = $res_m[0]['shop_id']; //默认商城自营店id
                $defaultStoreId = $res_m[0]['id']; //默认商城id
            }
        }
        $haveStoreMchId = $shop_id != 0;
        $role_id = $result[0]['role'];//用户权限ID
        $admin_id = $result[0]['id']; // 管理员id
        $admin_name = $result[0]['name']; // 管理员账号
        $login_password = $result[0]['password']; // 管理员密码
        $admin_type = $result[0]['admin_type']; // 管理类型
        $admin_permission = $result[0]['permission']; // 许可
        $admin_type1 = $result[0]['type']; // 类型 0:系统管理员 1:客户 2:商城管理员 3:店主
        // $shop_id = $result[0]['shop_id']; // 店主id
        $status = $result[0]['status']; // 状态 1:禁用 2：启用
        $login_num = $result[0]['login_num']; // 登录次数
        $old_token = $result[0]['token'];//原token

        if ($admin_type == 0)
        {
            $store_id = $defaultStoreId;
            //使用默认商城id
        }
        else
        {
            $store_id = $result[0]['store_id'];
            if ($store_id == 0)
            {
                $store_id = $defaultStoreId;
            }
            else
            {
                $r = CustomerModel::where('id',$defaultStoreId)->select()->toArray();
                $customer_status = $r[0]['status']; // 类型 0:启用 1:到期 2:锁定
                $end_date = $r[0]['end_date']; // 到期时间
                $time = date('Y-m-d H:i:s'); // 当前时间
                $time1 = date("Y-m-d", strtotime("$end_date -1 week")); // 还有7天到期
                if ($end_date <= $time)
                { // 当前时间 大于等于 到期时间 禁止登陆
                    $message = Lang('supplier.33');//您的授权已到期，请联系客服完成续费再使用，谢谢！
                    return output(401,$message,null);
                }
                if ($time >= $time1)
                { // 当前时间 大于等于 7天到期时间  提醒客户
                    $day = bcdiv(strtotime($end_date) - strtotime($time), (60 * 60 * 24));
                }
                if($customer_status != 0)
                {
                    $message = Lang('supplier.32');//您的商城已锁定，请联系管理员再使用，谢谢！
                    return output(401,$message,null);
                }
            }
        }
        if ($login_password == $password)
        {
            if ($login_num >= 3)
            {
                $message = Lang('supplier.32');//账号已锁定，请联系客服！
                return output(400,$message,null);
            }
            if ($status == 1)
            {
                return output(400,'账号已被禁用！若有疑问，请与商城管理员联系！',null);
            }
            AdminModel::update(['login_num' => 0,'status' => 2],['name'=>$name]);
        }
        else
        {
            if ($customer_number != '')
            {
                //商城管理员和平台管理员不锁账号
                if ($admin_type == 0 || $admin_type1 == 1)
                {

                }
                else
                {
                    AdminModel::update(['login_num' => Db::raw('login_num+1')],['name'=>$name]);
                }
                if ($login_num + 1 >= 3)
                {
                    AdminModel::update(['status' => 1],['name'=>$name]);
                    if ($admin_type1 == 1)
                    {
                        CustomerModel::update(['status' => 2],['admin_id'=>$admin_id]);
                    }
                    $message = Lang('supplier.32');//账号已锁定，请联系客服！
                    return output(400,$message,null);
                }
            }
            // 没有查询到匹配值就在lkt_record表里添加一组数据
            $RecordModel           = new RecordModel;
            $RecordModel->store_id     = $defaultStoreId;
            $RecordModel->user_id    = $name;
            $RecordModel->event    = '登录密码错误';
            $RecordModel->save();
            $message = Lang('supplier.34');//密码错误，请重新输入！
            return output(400,$message,null);
        }
        $aid = $result[0]['id'];

        //修改token
        $ip = $this->getClientIp();

        //获取用户信息
        $res_a = AdminModel::where(['id'=>$admin_id])->select()->toArray();
        if(!$res_a[0]['portrait'])
        {
            $rsc= Db::name('system_configuration')->field('admin_default_portrait')->select()->toArray();
            $res_a[0]['portrait'] = $rsc[0]['admin_default_portrait']; // 店铺头像
        }
        $token = $res_a[0]['token'];

        if($admin_type1 == 0)
        { // 系统管理员
            $getPayload_test = Tools::verifyToken($token); //对token进行验证签名,如果过期返回false,成功返回数组
            if ($getPayload_test)
            { // 过期重新生成一个，不需要做登录提示。
                $access_token = $token;
            }
            else
            {
                $access_token = Tools::getAdminToken($aid);
            }
        }
        else
        {
            // 生成session_id
            $access_token = Tools::getAdminToken($aid);
        }
        AdminModel::update(['token' => $access_token,'ip'=>$ip],['id'=>$aid]);

        //管理员操作记录
        $Jurisdiction = new Jurisdiction();
        $Jurisdiction->admin_record($defaultStoreId, $admin_name, ' 登录成功',0,1,0,$aid);

        $serverres = UploadSetModel::where('attr','in',['Bucket','Endpoint'])->select()->toArray();
        $serverURL = array(
            'OSS' => 'https://'
        );

        if (!empty($serverres))
        {
            foreach ($serverres as $k => $v)
            {
                if ($v['type'] == '阿里云OSS')
                {
                    if ($v['attr'] == 'Bucket')
                    {
                        $OSS['Bucket'] = $v['attrvalue'];
                        $serverURL['OSS'] .= $OSS['Bucket'];
                    }
                    if ($v['attr'] == 'Endpoint')
                    {
                        $OSS['Endpoint'] = $v['attrvalue'];
                        $serverURL['OSS'] .= '.' . $OSS['Endpoint'];
                    }
                }
            }
        }
        cache($access_token.'serverURL', $serverURL);
        // 用户信息存入redis

        cache($old_token, NULL);//清理原token数据
        cache($access_token, $res_a[0], 60*60*24);//添加新token数据
        cache($old_token.'_8', NULL);
        cache($access_token.'_8',$shop_id,60*60*24);

        //获取商城权限id写入session
        if($admin_type1 == 0)
        {
            $sql = "select b.role from lkt_customer as a left join lkt_admin as b on a.admin_id = b.id where a.status = 0 and a.recycle = 0 and a.is_default = 1";
            $res = Db::query($sql);
            if($res)
            {
                $role_id = $res[0]['role'];
                if($res[0]['role'] == '')
                {
                    $sql = "select b.* from lkt_customer as a left join lkt_admin as b on a.admin_id = b.id where a.status = 0 and a.recycle = 0 and b.role != '' order by a.add_date asc limit 1";
                    $res = Db::query($sql);
                    if($res)
                    {
                        $role_id = $res[0]['role'];
                    }
                }
            }
        }

        $login_time = time();
        $store_type1 = '';
        cache($access_token.'login_time', $login_time);// 登录时间
        cache($access_token.'store_id', $defaultStoreId);// 商城ID
        cache($access_token.'store_type', $store_type1);
        cache($access_token.'mch_id', $shop_id);// 登录管理员店铺ID
        cache($access_token.'admin_permission', $admin_permission);// 登录管理员权限
        cache($access_token.'admin_id', $admin_id);
        cache($access_token.'admin_name', $admin_name);
        cache($access_token.'role', $role_id);

        $array = array('store_id'=>$defaultStoreId,'store_type'=>8,'read_id'=>$admin_id,'admin_type'=>$admin_type1);
        $r_system = PC_Tools::GetAnnouncement($array);
        $tell_id = $r_system['tell_id'];
        $systemMsgTitle = $r_system['systemMsgTitle'];
        $systemMsg = $r_system['systemMsg'];
        $systemMsgStartDate = $r_system['systemMsgStartDate'];
        $systemMsgEndDate = $r_system['systemMsgEndDate'];
        $systemMsgType = $r_system['systemMsgType'];

        $currency_code = "";
        $currency_name = "";
        $sql_c = "select default_lang_code from lkt_config where store_id = '$defaultStoreId' ";
        $r_c = Db::query($sql_c);
        if($r_c)
        {
            $currency_code = $r_c[0]['default_lang_code'];
            $currency_name = Tools::get_lang_name($currency_code);
        }

        $storeDefaultCurrencyInfo = array();

        $storeDefaultCurrencyInfo = Tools::get_store_currency(array('store_id'=>$defaultStoreId,'type'=>1,'id'=>0)); 
        if($storeDefaultCurrencyInfo != array())
        {
            $storeDefaultCurrencyInfo['currency_symbol'] = $storeDefaultCurrencyInfo[0]['currency_symbol'];
            $storeDefaultCurrencyInfo['currency_code'] = $currency_code;
            $storeDefaultCurrencyInfo['currency_name'] = $currency_name; 
        }

        // 登录成功后跳转地址
        if ($day == '')
        {
            $message = Lang('Success');
            return output(200,$message,array('birthday'=>$res_a[0]['birthday'],'info'=>$message,'mchId'=>$shop_id,'name'=>$res_a[0]['name'],'nickname'=>$res_a[0]['nickname'],'phone'=>$res_a[0]['tel'],'portrait'=>$res_a[0]['portrait'],'role'=>$role_id,'sex'=>$res_a[0]['sex'],'status'=>$res_a[0]['status'],'storeId'=>$defaultStoreId,'type'=>$res_a[0]['type'],'token'=>$access_token,'tell_id'=>$tell_id,'systemMsgTitle'=>$systemMsgTitle,'systemMsg'=>$systemMsg,'systemMsgEndDate'=>$systemMsgEndDate,'systemMsgStartDate'=>$systemMsgStartDate,'systemMsgType'=>$systemMsgType,'lang'=>$currency_code,'storeDefaultCurrencyInfo'=>$storeDefaultCurrencyInfo,"haveStoreMchId"=>$haveStoreMchId));
        }
        else
        {
            $str = '您的账号还有' . $day . '天到期，请及时续费！';
            if ($day == 0)
            {
                $str = '您的账号将在今日到期，请及时续费！';
            }
            $message = Lang('Success');
            return output(200,$str,array('birthday'=>$res_a[0]['birthday'],'info'=>$message,'mchId'=>$shop_id,'name'=>$res_a[0]['name'],'nickname'=>$res_a[0]['nickname'],'phone'=>$res_a[0]['tel'],'portrait'=>$res_a[0]['portrait'],'role'=>$role_id,'sex'=>$res_a[0]['sex'],'status'=>$res_a[0]['status'],'storeId'=>$defaultStoreId,'type'=>$res_a[0]['type'],'token'=>$access_token,'tell_id'=>$tell_id,'systemMsgTitle'=>$systemMsgTitle,'systemMsg'=>$systemMsg,'systemMsgEndDate'=>$systemMsgEndDate,'systemMsgStartDate'=>$systemMsgStartDate,'systemMsgType'=>$systemMsgType,'lang'=>$currency_code,'storeDefaultCurrencyInfo'=>$storeDefaultCurrencyInfo,"haveStoreMchId"=>$haveStoreMchId));
        }
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

    public function logout()
    {
        $token = Request::param('accessId')?Request::param('accessId'):Request::post('accessId');
        $store_id = cache($token.'store_id');//商城ID
        $admin_id = cache($token.'admin_id');
        $admin_name = cache($token.'admin_name');

        //管理员记录
        $RecordModel           = new RecordModel;
        $RecordModel->store_id     = $store_id;
        $RecordModel->user_id    = $admin_name;
        $RecordModel->event    = '安全退出成功';
        $RecordModel->save();

        //管理员操作记录
        $Jurisdiction = new Jurisdiction();
        $Jurisdiction->admin_record($store_id, $admin_name, ' 安全退出成功',0,1,0,$admin_id);

        cache($token,null);//清理原token数据
        return output(200,'登出成功');
    }

    // 获取自营店ID
    public function setUserAdmin()
    {
        $store_id = addslashes(Request::param('storeId'));
        $access_id = addslashes(Request::param('accessId'));

        $shop_id = 0;
        $r0 = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id,role')->select()->toArray();
        if($r0)
        {
            $shop_id = $r0[0]['shop_id'];
            cache($access_id.'role',null);
            cache($access_id.'role',$r0[0]['role']);
        }

        $data = array('mchId'=>$shop_id);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 修改用户信息
    public function updateAdminInfo()
    {
        $store_id = addslashes(Request::param('storeId'));
        $access_id = addslashes(Request::param('accessId'));
        $passwordOld = addslashes(Request::param('passwordOld')); // 原密码
        $password = addslashes(Request::param('password')); // 新密码
        $portrait = addslashes(Request::param('portrait')); // 头像
        $nickname = addslashes(Request::param('nickname')); // 昵称
        $birthday = addslashes(Request::param('birthday')); // 生日
        $sex = addslashes(Request::param('sex')); // 性别
        $tel = addslashes(Request::param('phone')); // 手机号码

        $Jurisdiction = new Jurisdiction();

        $r0 = AdminModel::where(['recycle'=>0,'token'=>$access_id])->field('id,name')->select()->toArray();
        if($r0)
        {
            $admin_id = $r0[0]['id'];
            $admin_name = $r0[0]['name'];
        }

        if (!empty($portrait) || !empty($nickname) || !empty($birthday) || !empty($sex) || !empty($tel))
        {
            $sql_where = array('id'=>$admin_id);
            $sql_update = array('portrait'=>$portrait,'nickname'=>$nickname,'birthday'=>$birthday,'sex'=>$sex,'tel'=>$tel);
            $r = Db::name('admin')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $admin_name, ' 修改基本信息失败！',2,1,0,$admin_id);
                $message = Lang('未知原因，修改失败！');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $admin_name, ' 修改基本信息成功！',2,1,0,$admin_id);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $y_password = md5($passwordOld);
            $r0 = AdminModel::where(['id'=>$admin_id,'password'=>$y_password])->field('id')->select()->toArray();
            if(!$r0)
            {
                $message = Lang('原密码输入不正确，请重新输入！');
                return output(109,$message);
            }

            if(strlen($password) < 6)
            {
                $message = Lang('密码不能小于6位！');
                return output(109,$message);
            }

            if ($passwordOld == $password)
            {
                $message = Lang('新密码不能与原密码相同！');
                return output(109,$message);
            }

            $sql_where = array('id'=>$admin_id);
            $sql_update = array('password'=>md5($password));
            $r = Db::name('admin')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $admin_name, ' 修改密码失败！',2,1,0,$admin_id);
                $message = Lang('未知原因，修改失败！');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $admin_name, ' 修改基本信息成功！',2,1,0,$admin_id);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    //获取图形验证码
    public function getCode()
    {
        $data = PC_Tools::getCode();
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 更改语言
    public function select_language()
    {
        $store_id = trim(Request::param('storeId'));
        $store_type = trim(Request::param('storeType')); // 来源
        $access_id = trim(Request::param('accessId')); // 授权id
        $language = trim(Request::param('language')); // 授权id

        $lang = 'zh-cn';
        if ($language == 'en')
        {
            $lang = 'en-gb';
        }
        $sql0 = "select id from lkt_user where store_id = '$store_id' and access_id = '$access_id'";
        $r0 = Db::query($sql0);
        if ($r0)
        {
            $sql1 = "update lkt_user set lang = '$lang' where store_id = '$store_id' and access_id = '$access_id' ";
            $r1 = Db::execute($sql1);
        }
        $message = Lang("Success");
        return output(200,$message);
    }

    // 公告已读
    public function markToRead()
    {
        $store_id = trim(Request::param('storeId'));
        $store_type = trim(Request::param('storeType')); // 来源
        $access_id = trim(Request::param('accessId')); // 授权id
        $language = trim(Request::param('language')); // 授权id
        $tell_id = trim(Request::param('tell_id')); // 公告ID
        $admin_id = cache($access_id.'admin_id');

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'read_id'=>$admin_id,'tell_id'=>$tell_id);
        PC_Tools::markToRead($array);

        $message = Lang("Success");
        return output(200,$message);
    }

    // 获取维护公告
    public function getUserTell()
    {
        $store_id = trim(Request::param('storeId'));
        $store_type = trim(Request::param('storeType')); // 来源
        $access_id = trim(Request::param('accessId')); // 授权id
        $language = trim(Request::param('language')); // 授权id

        $res = cache($access_id);
        $admin_type1 = $res['type'];
        $read_id = cache($access_id.'admin_id');
        $array = array('store_id'=>$store_id,'store_type'=>8,'read_id'=>$read_id,'admin_type'=>$admin_type1);
        $data = PC_Tools::GetAnnouncement($array);
        // $data = PC_Tools::Obtain_maintenance_announcements($store_type);

        $message = Lang("Success");
        return output('200',$message,$data);
    }

}
