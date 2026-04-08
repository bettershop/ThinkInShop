<?php
namespace app\admin\controller\saas;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Role_Tools;
use app\common\Tools;
use app\common\Common_Tools;
use app\common\PC_Tools;
use app\common\ExcelUtils;

use app\admin\model\CustomerModel;
use app\admin\model\AdminModel;
use app\admin\model\UploadSetModel;
use app\admin\model\RoleModel;
use app\admin\model\ConfigModel;
use app\admin\model\PluginsModel;

class Shop extends BaseController
{

    /**
     * 获取落地页默认币种和语言
     *
     * @param Request $request
     * @return array
     * @throws HttpException
     */
    public function getLandingCurrency(Request $request)
    {
        try
        {
            $storeId = addslashes($this->request->param('storeId'));

            $defaultCurrencyRes = Common_Tools::get_store_currency(array("store_id" => $storeId,"type" => 1,"id" => 0));
            $defaultCurrencyId = '';
            if($defaultCurrencyRes)
            {
                $defaultCurrencyId = $defaultCurrencyRes[0]['currency_id'];
            }

            $defaultLangRes = Common_Tools::get_store_lang(array("store_id" => $storeId));
            $defaultLangId = '';

            if($defaultLangRes)
            {
                $defaultLangId = $defaultLangRes[0]['id'];
            }

            $data = array('default_currency'=>$defaultCurrencyId,'default_lang_id'=>$defaultLangId);
            $message = Lang('Success');
            return output(200, $message, $data);
        }
        catch (\Exception $e)
        {
            // 记录日志（可选）
            trace($e->getMessage(), 'error');
            // 抛出 HTTP 异常，状态码 500，错误信息
            return output(109,  $e->getMessage());
        }
    }

    // 商户列表
    public function getShopInfo()
    {   
        $storeId = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));
        
        $exportType = addslashes($this->request->param('exportType'));//是否导出
        $name = addslashes($this->request->param('storeName')); // 姓名
        $startdate = addslashes($this->request->param('startDate')); // 开始时间
        $enddate = addslashes($this->request->param('endDate')); // 结束日期
        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize:'10';

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * 10;
        }
        $time = date("Y-m-d H:i:s"); // 当前时间

        // 查询所有
        $r = CustomerModel::select()->toArray();
        foreach ($r as $k => $v)
        {
            $asd['id'] = $v['id'];
            $asd['end_date'] = $v['end_date']; // 到期时间
            $id = $v['id'];
            $end_date = $v['end_date']; // 到期时间
            $time1 = date("Y-m-d",strtotime("$end_date +1 week")); // 过期延期一周
            if($asd['end_date'] < $time)
            { // 到期时间小于当前时间
                Db::name('customer')->where('id', $asd['id'])->update(['status' => 1]);
            }
            if($time >= $time1)
            {
                Db::name('customer')->where('id', $id)->update(['recycle' => 1]);
            }
        }

        $condition = " recycle = 0 ";

        $query_name = addslashes($name);
        if($query_name != '' && $query_name != 'undefined')
        {
            $query_name = Tools::FuzzyQueryConcatenation($query_name);
            $condition .= " and name like $query_name ";
        }
        if($startdate != '' && $startdate != 'undefined')
        { // 查询开始日期不为空
            $condition .= " and add_date >= '$startdate' ";
        }
        if($enddate != '' && $enddate != 'undefined')
        { // 查询结束日期不为空
            if($enddate == date("Y-m-d 00:00:00",strtotime($enddate)))
            {
                $enddate = date("Y-m-d 23:59:59",strtotime($enddate));
            }
            else
            {
                $enddate = $enddate;
            }
            $condition .= " and add_date <= '$enddate' ";
        }
        if($storeId)
        {   
            $condition .= " and id = '$storeId' ";
        }
        $total = 0;
        $sql0 = "select count(id) as total from lkt_customer where $condition";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }
  
        $storeIdPrefix = true;

        $list = array();

        $sql01 = "select * from lkt_customer where $condition order by is_default desc,id desc limit $start,$pagesize ";
        $r01 = Db::query($sql01);
        if($r01)
        {
            foreach ($r01 as $key => $value)
            {
                $store_id = $value['id'];
                $value['isDefault'] = false;
                if($value['is_default'] == 1)
                {
                    $value['isDefault'] = true;
                }
                switch ($value['status']) 
                {
                    case '1':
                        $value['statusName'] = '到期';
                        break;
                    case '2':
                        $value['statusName'] = '锁定';
                        break;
                    default:
                        $value['statusName'] = '启用';
                        break;
                }
                $a = ConfigModel::where('store_id',$value['id'])->field('domain,default_currency,default_lang_code')->select()->toArray();
                if($a)
                {
                    $value['storeDomain'] = $a[0]['domain'];
                    $value['default_lang_code'] = $a[0]['default_lang_code'];
                    $value['default_currency'] = $a[0]['default_currency'];
                }
                //在每个商户的字段中添加该管理员的login_num
                $id = $value['admin_id'];
                $r = AdminModel::where('id',$id)->field('shop_id,login_num,role,name,portrait')->select()->toArray();
                if($r)
                {
                    $value['shop_id'] = $r[0]['shop_id']; // 店铺ID
                    $login_num = $r[0]['login_num'];
                    $role = $r[0]['role'];
                    $value['login_num'] = $login_num;
                    $value['roleId'] = $role;
                    $value['adminName'] = $r[0]['name'];
                    $value['portrait'] = $r[0]['portrait'];
                    $r1 = RoleModel::where('id',$role)->field('name')->select()->toArray();
                    if($r1)
                    {
                        $value['roleName'] = $r1[0]['name']; // 角色名称
                    }
                    else
                    {
                        $value['roleName'] = ''; // 角色名称
                    }
                }

                $store_currencys = array();
                $sql_c_s = "select b.id from lkt_currency_store as a left join lkt_currency as b on a.currency_id = b.id where b.recycle = 0 and a.store_id = '$store_id' ";
                $r_c_s = Db::query($sql_c_s);
                if($r_c_s)
                {
                    foreach($r_c_s as $k_c_s => $v_c_s)
                    {
                        $store_currencys[] = $v_c_s['id'];
                    }
                }

                $storeDefaultCurrencyInfo = array();
                $sql_c_s1 = "select b.id,b.currency_code,b.currency_name,b.currency_symbol,a.* from lkt_currency_store as a left join lkt_currency as b on a.currency_id = b.id where b.recycle = 0 and a.default_currency = 1 and a.store_id = '$store_id' ";
                $r_c_s1 = Db::query($sql_c_s1);
                if($r_c_s1)
                {
                    $value['default_currency'] = $r_c_s1[0]['default_currency'];
                    $storeDefaultCurrencyInfo = $r_c_s1[0];
                }

                $value['store_currencys'] = implode(',',$store_currencys);
                $value['storeDefaultCurrencyInfo'] = $storeDefaultCurrencyInfo;

                $list[] = $value;
            }
        }
        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '商城id',
                1 => '商城名称',
                2 => '手机',
                3 => '价格',
                4 => '公司名称',
                5 => '购买时间',
                6 => '到期时间',
                7 => '角色',
                8 => '状态'
            );
            $exportExcel_list = array();

            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['id'],
                        $v['name'],
                        $v['mobile'],
                        $v['price'],
                        $v['company'],
                        $v['add_date'],
                        $v['end_date'],
                        $v['roleName'],
                        $v['statusName']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '商城列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }
        $data = array('total'=>$total,'dataList'=>$list,'storeIdPrefix'=>$storeIdPrefix);
        $message = Lang('Success');
        return output(200, $message, $data);
    }

    // 添加商户
    public function addStore()
    {
        $store_id = addslashes($this->request->param('id')); // 商城ID
        $name = addslashes($this->request->param('storeName')); // 姓名
        $customer_number = addslashes($this->request->param('storeNo')); // 客户编号
        $company = addslashes($this->request->param('company')); // 公司名称
        $phone = addslashes($this->request->param('mobile')); // 手机
        $price = addslashes($this->request->param('price')); // 价格
        $email = addslashes($this->request->param('email')); // 邮箱
        $set_admin_name = addslashes($this->request->param('adminAccount')); // 管理员账号
        $password = $this->request->has('adminPwd')?$this->request->param('adminPwd'):''; // 密码
        $endtime = addslashes($this->request->param('endDate')); // 结束时间
        $adminDefaultPortrait = addslashes($this->request->param('adminDefaultPortrait')); // 管理员头像
        $status = addslashes($this->request->param('isOpen')); // 类型 0:启用 1:到期 2:锁定

        $store_langs = addslashes($this->request->param('store_langs')); // 语种
        $default_lang_code = addslashes($this->request->param('default_lang')); // 默认语种
        $store_currencys = addslashes($this->request->param('store_currencys')); // 币种
        $default_currency = addslashes($this->request->param('default_currency')); // 默认币种
        $domain = addslashes($this->request->param('storeDomain')); // 商城根目录域名
        $contact_address = addslashes($this->request->param('contactAddress')); // 联系地址
        $contact_number = addslashes($this->request->param('contactNumber')); // 联系电话
        $cpc = addslashes($this->request->param('cpc')); // 区号
        $copyright_information = addslashes($this->request->param('copyrightInformation')); // 版权信息
        $record_information = addslashes($this->request->param('recordInformation')); // 备案信息
        $official_website = addslashes($this->request->param('website')); // 官方网址
        $merchant_logo = addslashes($this->request->param('logUrl')); // 商户logo
        $role_id = addslashes($this->request->param('roleId')); // 角色ID

        $admin_id = $this->user_list['id'];
        $admin_name = $this->user_list['name'];
        require_once(MO_LIB_DIR.'/Lang_code.php');

        // 1.开启事务
        Db::startTrans();

        $time = date("Y-m-d H:i:s");
        if(empty($name))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写客户名称';
            $this->Log($Log_content);
            $message = Lang('shop.0');
            return output(109, $message);
        }
        if(empty($company))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写公司名称';
            $this->Log($Log_content);
            $message = Lang('shop.1');
            return output(109, $message);
        }

        if(empty($phone))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写手机号';
            $this->Log($Log_content);
            $message = Lang('shop.2');
            return output(109, $message);
        }
        if(empty($price))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写价格';
            $this->Log($Log_content);
            $message = Lang('shop.3');
            return output(109, $message);
        }
        // if(empty($email))
        // {
        //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写邮箱';
        //     $this->Log($Log_content);
        //     $message = Lang('shop.4');
        //     return output(109, $message);
        // }
        if(empty($endtime))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写到期时间';
            $this->Log($Log_content);
            $message = Lang('shop.5');
            return output(109, $message);
        }

        // if(empty($domain))
        // {
        //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写商城根目录域名';
        //     $this->Log($Log_content);
        //     $message = Lang('shop.6');
        //     return output(109, $message);
        // }
        // if(empty($copyright_information))
        // {
        //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写版权信息';
        //     $this->Log($Log_content);
        //     $message = Lang('shop.7');
        //     return output(109, $message);
        // }
        // if(empty($merchant_logo))
        // {
        //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写商户logo';
        //     $this->Log($Log_content);
        //     $message = Lang('shop.8');
        //     return output(109, $message);
        // }
        if(strlen($status) == 0)
        {
            $status = 2;
        }

        if($store_id == 0 || $store_id == '')
        {
            if(empty($role_id))
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择角色';
                $this->Log($Log_content);
                $message = Lang('shop.9');
                return output(109, $message);
            }
            else
            {
                $role_list = array();
                // 查询已绑定商户
                $sql0_role = "select a.id from lkt_admin as a left join lkt_customer as b on a.store_id = b.id where a.type = 1 and a.recycle = 0 and b.recycle = 0 and a.role = '$role_id'";
                $r0_role = Db::query($sql0_role);
                if($r0_role)
                {
                    foreach ($r0_role as $k_role => $v_role)
                    {
                        $role_list[] = $v_role['id'];
                    }
                }
            }

            $r_admin = AdminModel::where(['recycle'=>0,'name'=>$set_admin_name])->field('id')->select()->toArray();
            if($r_admin)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员账号已存在';
                $this->Log($Log_content);
                $message = Lang('shop.37');
                return output(109, $message);
            }
            if (strlen($set_admin_name) > 20)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员账号不能超过20个字符内的英文数字长度';
                $this->Log($Log_content);
                $message = Lang('shop.11');
                return output(109, $message);
            }
            if (strlen($set_admin_name) < 6)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员账号不能低于6个字符内的英文数字长度';
                $this->Log($Log_content);
                $message = Lang('shop.36');
                return output(109, $message);
            }

            if(preg_match("/^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$/", $phone))
            {
                if($endtime < $time)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 到期时间不正确';
                    $this->Log($Log_content);
                    $message = Lang('shop.12');
                    return output(109, $message);
                }
                
                if(empty($password))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员密码不能为空';
                    $this->Log($Log_content);
                    $message = Lang('shop.13');
                    return output(109, $message);
                }
                else
                {
                    if (strlen($password) < 6)
                    {
                        $message = Lang("user.0");
                        return output(ERROR_CODE_MMBFHGF,$message);
                    }
                    $password1 = Tools::lock_url($password);
                    $password = MD5($password);
                }
                // 根据邮箱、客户名称、客户编号查询商户信息

                $sql_customer = "select * from lkt_customer where recycle = 0 and (name ='$name' OR customer_number = '$customer_number')";
                $r_customer = Db::query($sql_customer);
                if(!empty($r_customer))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 商户名称、商户编号';
                    $this->Log($Log_content);
                    $message = Lang('shop.14');
                    return output(109, $message);
                }
                // 生成session_id
                $access_token = session_id();
                //修改token
                $ip = $this->get_client_ip();

                $is_default = 0;
                $r0_0 = CustomerModel::where('recycle',0)->field('id')->select()->toArray();
                if(!$r0_0)
                {
                    $is_default = 1;
                }
                // 活动开始时间大于当前时间,活动还没开始
                $sql = array('customer_number'=>$customer_number,'admin_id'=>0,'name'=>$name,'mobile'=>$phone,'price'=>$price,'company'=>$company,'function'=>2,'add_date'=>$time,'end_date'=>$endtime,'status'=>$status,'email'=>$email,'contact_address'=>$contact_address,'cpc'=>$cpc,'contact_number'=>$contact_number,'copyright_information'=>$copyright_information,'record_information'=>$record_information,'official_website'=>$official_website,'merchant_logo'=>$merchant_logo,'is_default'=>$is_default,'store_langs'=>$store_langs);
                $rr = Db::name('customer')->insertGetId($sql);
                if($rr > 0)
                {
                    // 查询图片上传设置
                    $r0 = UploadSetModel::limit(1)->field('upserver')->select()->toArray();
                    if($r0)
                    {
                        $upserver = $r0[0]['upserver'];
                    }
                    else
                    {
                        $upserver = 2;
                    }
                    // 添加系统设置
                    $sql1 = array('store_id'=>$rr,'company'=>$company,'domain'=>$domain,'upserver'=>$upserver,'modify_date'=>$time,'uploadImg'=>'./images/','user_id'=>'user','wx_name'=>'user','default_lang_code'=>$default_lang_code,'default_currency'=>$default_currency);
                    $r1 = Db::name('config')->insert($sql1);
                    if($r1 < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加系统设置失败';
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('shop.15');
                        return output(109, $message);
                    }
                    // 添加管理员信息
                    $sql = array('sid'=>$admin_id,'name'=>$set_admin_name,'password'=>$password,'portrait'=>$adminDefaultPortrait,'permission'=>'','role'=>'','type'=>1,'store_id'=>$rr,'status'=>2,'tel'=>$phone,'token'=>$access_token,'ip'=>$ip,'add_date'=>$time,'recycle'=>0);
                    $admin_id1 = Db::name('admin')->insertGetId($sql);
                    if($admin_id1 < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加管理员信息失败';
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('shop.16');
                        return output(109, $message);
                    }
                    // 根据商户ID，修改商户管理员ID
                    $r2 = Db::name('customer')->where('id', $rr)->update(['admin_id' => $admin_id1]);
                    if($r2 < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改商户信息失败';
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('shop.17');
                        return output(109, $message);
                    }

                    $r_plugins = PluginsModel::where('store_id',0)->select()->toArray();
                    if($r_plugins)
                    {
                        foreach($r_plugins as $k_plugins => $v_plugins)
                        {
                            $r_plugins[$k_plugins]['store_id'] = $rr;
                            $r_plugins[$k_plugins]['optime'] = $time;
                            unset($r_plugins[$k_plugins]['id']);
                        }
                        $r_plugins_insert = Db::name('plugins')->insertAll($r_plugins);
                    }

                    if($role_id != 0 && $role_id != '')
                    {
                        $array = array('admin_name'=>$admin_name,'id'=>$role_id,'list'=>array($admin_id1),'list1'=>$role_list);
                        Role_Tools::bangding0($array);
                    }

                    // // 添加用户信息
                    // $sql_user = array('store_id'=>$rr,'user_id'=>$set_admin_name,'user_name'=>$set_admin_name,'real_name'=>$name,'mobile'=>$phone,'zhanghao'=>$set_admin_name,'mima'=>$password1,'Register_data'=>$time,'source'=>6,'birthday'=>$time);
                    // $user = Db::name('user')->insert($sql_user);
                    // if($user < 1)
                    // {
                    //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加会员信息失败';
                    //     $this->Log($Log_content);
                    //     Db::rollback();
                    //     $message = Lang('shop.18');
                    //     return output(109, $message);
                    // }

                    $payConfigSQLs = array(
                        array('store_id'=>$rr,'pid'=>5,'status'=>1,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>4,'status'=>1,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>1,'status'=>1,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>2,'status'=>0,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>6,'status'=>1,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>7,'status'=>0,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>8,'status'=>0,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>9,'status'=>0,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>3,'status'=>1,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>10,'status'=>0,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>11,'status'=>0,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>12,'status'=>0,'config_data'=>''),
                        array('store_id'=>$rr,'pid'=>13,'status'=>0,'config_data'=>''),
                    );
                    $r3 = Db::name('payment_config')->insertAll($payConfigSQLs);
                    if($r3 < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加支付信息失败';
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('shop.19');
                        return output(109, $message);
                    }

                    if($store_currencys != '')
                    {
                        $store_currencys_list = explode(',',$store_currencys);
                        foreach($store_currencys_list as $k_currencys => $v_currencys)
                        {
                            $default_currency = 0;
                            
                            //不用默认设置
                            // if($k_currencys == 0)
                            // {
                            //     $default_currency = 1;
                            // }
                            
                            $sql_currency_store = "insert into lkt_currency_store(store_id,currency_id,is_show,default_currency,exchange_rate,recycle,update_time) value ('$rr','$v_currencys',1,'$default_currency','1.0000',0,'$time')";
                            $r_currency_store = Db::execute($sql_currency_store);
                        }
                    }

                    $store_langs_list = explode(',',$store_langs);
                    if($store_langs_list != array())
                    {
                        foreach($store_langs_list as $k_l => $v_l)
                        {
                            $sql_l = "select lang_code from lkt_lang where id = '$v_l' ";
                            $r_l = Db::query($sql_l);
                            if($r_l)
                            {
                                $c_name = $Lang_code[$r_l[0]['lang_code']];
                                $categories = "";

                                //添加商品默认分类
                                $sql_class = array('store_id'=>$rr,'sid'=>0,'pname'=>$c_name,'level'=>0,'is_default'=>1,'lang_code'=>$r_l[0]['lang_code'],'examine'=>1,'notset'=>1);
                                $r_class = Db::name('product_class')->insertGetId($sql_class);
                                if($r_class < 1)
                                {
                                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加商品默认分类失败';
                                    $this->Log($Log_content);
                                    Db::rollback();
                                    $message = Lang('shop.21');
                                    return output(109, $message);
                                }
                                $categories = ',' . $r_class . ',';

                                //添加商品默认分类
                                $sql_brand_class = array('store_id'=>$rr,'brand_name'=>$c_name,'brand_pic'=>'','remarks'=>'','brand_time'=>$time,'sort'=>0,'categories'=>$categories,'lang_code'=>$r_l[0]['lang_code'],'examine'=>1,'notset'=>1);
                                $r_brand_class = Db::name('brand_class')->insert($sql_brand_class);
                                if($r_brand_class < 1)
                                {
                                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加商品默认品牌失败';
                                    $this->Log($Log_content);
                                    Db::rollback();
                                    $message = Lang('shop.21');
                                    return output(109, $message);
                                }
                            }
                        }
                    }

                    //店铺默认分类
                    $sql = array('store_id'=>$rr,'name'=>'自营','sort'=>1,'is_display'=>1,'add_date'=>$time);
                    $r = Db::name('mch_class')->insertGetId($sql);

                    Db::commit();
                    $message = Lang('Success');
                    return output(200, $message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 未知原因，添加商户失败！';
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('shop.22');
                    return output(109, $message);
                }
            }
            else
            {
                $message = Lang('shop.23');
                return output(109, $message);
            }
        }
        else
        {
            // 根据商城id和类型为客户，查询管理员id
            $r = AdminModel::where(['type'=>1,'store_id'=>$store_id])->select()->toArray();
            if ($r)
            {
                $adminid = $r[0]['id'];
                // 根据商城id、类型为客户、管理员账号、管理员id不同，查询管理员id
                $r_1 = AdminModel::where(['type'=>1,'recycle'=>0,'name'=>$set_admin_name])->where('id','<>',$adminid)->field('id')->select()->toArray();
                if($r_1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员账号已存在';
                    $this->Log($Log_content);
                    $message = Lang('shop.37');
                    return output(109, $message);
                }
                if (strlen($set_admin_name) > 20)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员账号不能超过20个字符内的英文数字长度';
                    $this->Log($Log_content);
                    $message = Lang('shop.11');
                    return output(109, $message);
                }
                if (strlen($set_admin_name) < 6)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员账号不能低于6个字符内的英文数字长度';
                    $this->Log($Log_content);
                    $message = Lang('shop.36');
                    return output(109, $message);
                }
                if($password != 'null')
                {
                    if($password != '')
                    {
                        $password1 = $r[0]['password'];
                        if (strlen($password) < 6)
                        {
                            $message = Lang("user.0");
                            return output(ERROR_CODE_MMBFHGF,$message);
                        }
                        if ($password1 == $password)
                        {
                            $password = $password1;
                        }
                        else
                        {
                            $password = MD5($password);
                        }
                    }
                }
            }

            if (preg_match("/^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$/", $phone))
            {
                if ($endtime <= $time)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 到期时间不正确';
                    $this->Log($Log_content);
                    $message = Lang('shop.12');
                    return output(109, $message);
                }
                else
                {
                    $status = 0;
                }
                // 根据邮箱、客户名称、客户编号查询商户信息
                $sql_customer = "select 1 from lkt_customer where (name ='$name' OR customer_number = '$customer_number' ) and id <> '$store_id' and recycle = 0";
                $r_customer = Db::query($sql_customer);
                if (!empty($r_customer))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 商户名称、商户编号重复';
                    $this->Log($Log_content);
                    $message = Lang('shop.14');
                    return output(109, $message);
                }
                // 修改商户信息
                $sql_update = array('name'=>$name,'company'=>$company,'mobile'=>$phone,'price'=>$price,'email'=>$email,'end_date'=>$endtime,'status'=>$status,'contact_address'=>$contact_address,'cpc'=>$cpc,'contact_number'=>$contact_number,'copyright_information'=>$copyright_information,'record_information'=>$record_information,'official_website'=>$official_website,'merchant_logo'=>$merchant_logo,'customer_number'=>$customer_number,'store_langs'=>$store_langs);
                $sql_where = array('id'=>$store_id,'recycle'=>0);
                $r = Db::name('customer')->where($sql_where)->update($sql_update);
                if ($r == -1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 未知原因，修改商户失败！';
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('shop.24');
                    return output(109, $message);
                }
                else
                {
                    // 生成session_id
                    $access_token = session_id();
                    //修改token
                    $ip = $this->get_client_ip();

                    if($status == 2)
                    {
                        $sql_update = array('name'=>$set_admin_name,'tel'=>$phone,'token'=>$access_token,'ip'=>$ip,'portrait'=>$adminDefaultPortrait,'login_num'=>0,'status'=>1);
                    }
                    else
                    {
                        $sql_update = array('name'=>$set_admin_name,'tel'=>$phone,'token'=>$access_token,'ip'=>$ip,'portrait'=>$adminDefaultPortrait);
                    }
                    if($password != 'null')
                    {
                        if($password)
                        {
                            $sql_update = array_merge($sql_update,array('password'=>$password));
                        }
                    }
                    $sql_where = array('store_id'=>$store_id,'id'=>$adminid);

                    $r = Db::name('admin')->where($sql_where)->update($sql_update);

                    $r2 = Db::name('config')->where('store_id',$store_id)->update(['domain'=>$domain]);

                    if($store_currencys != '')
                    {
                        $store_currencys_list = explode(',',$store_currencys);

                        $sql_currency_store0 = "delete from lkt_currency_store where store_id = '$store_id' ";
                        $r_currency_store0 = Db::execute($sql_currency_store0);

                        foreach($store_currencys_list as $k_currencys => $v_currencys)
                        {
                            $default_currency = 0;
                            if($k_currencys == 0)
                            {
                                $default_currency = 1;
                            }
                            $sql_currency_store = "insert into lkt_currency_store(store_id,currency_id,is_show,default_currency,exchange_rate,recycle,update_time) value ('$store_id','$v_currencys',1,'$default_currency','1.0000',0,'$time')";
                            $r_currency_store = Db::execute($sql_currency_store);
                        }
                    }

                    $store_langs_list = explode(',',$store_langs);
                    if($store_langs_list != array())
                    {
                        foreach($store_langs_list as $k_l => $v_l)
                        {
                            $sql_l = "select lang_code from lkt_lang where id = '$v_l' ";
                            $r_l = Db::query($sql_l);
                            if($r_l)
                            {
                                $lang_code_name = $r_l[0]['lang_code'];
                                $c_name = $Lang_code[$lang_code_name];
                                $categories = "";
                                $sql_class0 = "select cid from lkt_product_class where store_id = '$store_id' and pname = '$c_name' and lang_code = '$lang_code_name' ";
                                $r_class0 = Db::query($sql_class0);
                                if(empty($r_class0))
                                {

                                    //添加商品默认分类
                                    $sql_class = array('store_id'=>$store_id,'sid'=>0,'pname'=>$c_name,'level'=>0,'is_default'=>1,'lang_code'=>$lang_code_name,'examine'=>1,'notset'=>1);
                                    $r_class = Db::name('product_class')->insertGetId($sql_class);
                                    if($r_class < 1)
                                    {
                                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加商品默认分类失败';
                                        $this->Log($Log_content);
                                        Db::rollback();
                                        $message = Lang('shop.21');
                                        return output(109, $message);
                                    }
                                    $categories = ',' . $r_class . ',';
                                }

                                $sql_brand_class0 = "select brand_id from lkt_brand_class where store_id = '$store_id' and brand_name = '$c_name' and lang_code = '$lang_code_name' ";
                                $r_brand_class0 = Db::query($sql_brand_class0);
                                if(empty($r_brand_class0))
                                {
                                    //添加商品默认分类
                                    $sql_brand_class = array('store_id'=>$store_id,'brand_name'=>$c_name,'brand_pic'=>'','remarks'=>'','brand_time'=>$time,'sort'=>0,'categories'=>$categories,'lang_code'=>$lang_code_name,'examine'=>1,'notset'=>1);
                                    $r_brand_class = Db::name('brand_class')->insert($sql_brand_class);
                                    if($r_brand_class < 1)
                                    {
                                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加商品默认品牌失败';
                                        $this->Log($Log_content);
                                        Db::rollback();
                                        $message = Lang('shop.21');
                                        return output(109, $message);
                                    }
                                }
                            }
                        }
                    }

                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 用户修改成功';
                    $this->Log($Log_content);
                    Db::commit();
                    $message = Lang('Success');
                    return output(200, $message);
                }
            }
            else
            {
                $message = Lang('shop.23');
                return output(109, $message);
            }
        }
    }

    // 重置密码
    public function resetAdminPwd()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $id = addslashes($this->request->param('adminId'));
        $password0 = addslashes($this->request->param('pwd'));
        $password = md5($password0);

        if(strlen($password0) < 6)
        {
            $message = Lang('shop.25');
            return output(109, $message);
        }
        
        if(preg_match('[@_!#$%^&*()<>?/|}{~:]', $password0))
        {
            $message = Lang('shop.26');
            return output(109, $message);
        }

        //根据id将管理员的密码重置
        $res = Db::name('admin')->where('id',$id)->update(['password'=>$password,'token'=>'']);
        if($res == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 重置用户ID为'.$id.'密码失败';
            $this->Log($Log_content);
            $message = Lang('shop.27');
            return output(109, $message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 重置用户ID为'.$id.'密码成功';
            $this->Log($Log_content);
            $message = Lang('shop.28');
            return output(200, $message);
        }
        return ;
    }

    // 锁定/开锁
    public function setStoreOpenSwitch()
    {
        $id = addslashes($this->request->param('id'));

        // 1.开启事务
        Db::startTrans();

        $time = date("Y-m-d H:i:s");

        $r0 = CustomerModel::where(['recycle'=>0,'id'=>$id])->field('status,end_date')->select()->toArray();
        if($r0)
        {
            $status = $r0[0]['status'];
            $end_date = $r0[0]['end_date'];
            if($status == 0)
            { // 当前商户为启用中
                // 根据商户ID，修改商户状态(锁定)
                $r1 = Db::name('customer')->where(['id'=>$id,'recycle'=>0])->update(['status'=>2]);
                if ($r1 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改商户ID为'.$id.'失败';
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('shop.29');
                    return output(109, $message);
                }

                $r2 = Db::name('admin')->where(['store_id'=>$id])->update(['login_num'=>3,'status'=>1]);
                if ($r2 == -1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 锁定用户ID为'.$id.'失败';
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('shop.29');
                    return output(109, $message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 锁定用户ID为'.$id.'成功';
                    $this->Log($Log_content);
                    Db::commit();
                    $message = Lang('shop.30');
                    return output(200, $message);
                }
            }
            else if($status == 1)
            { // 当前商户为到期中
                // 根据商户ID，修改商户状态(启用)
                if($end_date <= $time)
                { // 过期
                    $message = Lang('shop.31');
                    return output(109, $message);
                }
                else
                { // 未过期
                    $r1 = Db::name('customer')->where(['id'=>$id,'recycle'=>0])->update(['status'=>0]);
                    if ($r1 <= 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改商户ID为'.$id.'失败';
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('shop.29');
                        return output(109, $message);
                    }
                }

                $r2 = Db::name('admin')->where(['store_id'=>$id])->update(['login_num'=>0,'status'=>2]);
                if ($r2 == -1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 解锁用户ID为'.$id.'失败';
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('shop.32');
                    return output(109, $message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 解锁用户ID为'.$id.'成功';
                    $this->Log($Log_content);
                    Db::commit();
                    $message = Lang('shop.33');
                    return output(200, $message);
                }
            }
            else if($status == 2)
            { // 当前商户为锁定中
                // 根据商户ID，修改商户状态(启用)
                $r1 = Db::name('customer')->where(['id'=>$id,'recycle'=>0])->update(['status'=>0]);
                if ($r1 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改商户ID为'.$id.'失败';
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('shop.29');
                    return output(109, $message);
                }

                $r2 = Db::name('admin')->where(['store_id'=>$id])->update(['login_num'=>0,'status'=>2]);
                if ($r2 == -1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 锁定用户ID为'.$id.'失败';
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('shop.32');
                    return output(109, $message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 锁定用户ID为'.$id.'成功';
                    $this->Log($Log_content);
                    Db::commit();
                    $message = Lang('shop.33');
                    return output(200, $message);
                }
            }
        }
        return;
    }

    // 删除商户
    public function delStore()
    {
        $id = addslashes($this->request->param('id'));

        $r0_0 = CustomerModel::where(['id'=>$id])->field('is_default')->select()->toArray();

        $r0 = Db::name('customer')->where(['id'=>$id])->update(['recycle'=>1]);
        if($r0 > 0)
        {
            if($r0_0[0]['is_default'] == 1)
            {
                $r2 = Db::name('customer')->where(['recycle'=>0])->limit(1)->order('add_date','asc')->update(['is_default'=>1]);
            }

            $r1 = Db::name('admin')->where(['store_id'=>$id])->update(['recycle'=>1]);

            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除商户ID为'.$id.'成功';
            $this->Log($Log_content);

            $message = Lang('shop.34');
            return output(200, $message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除商户ID为'.$id.'失败';
            $this->Log($Log_content);

            $message = Lang('shop.35');
            return output(109, $message);
        }
        return ;
    }

    function get_client_ip($type = 0,$client=true)
    {
        $type = $type ? 1 : 0;
        static $ip = NULL;
        if ($ip !== NULL) return $ip[$type];
        if($client)
        {
            if (isset($_SERVER['HTTP_X_FORWARDED_FOR']))
            {
                $arr = explode(',', $_SERVER['HTTP_X_FORWARDED_FOR']);
                $pos = array_search('unknown',$arr);
                if(false !== $pos) unset($arr[$pos]);
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
        $long = sprintf("%u",ip2long($ip));
        $ip = $long ? array($ip, $long) : array('0.0.0.0', 0);
        return $ip[$type];
    }

    //设为默认
    public function setStoreDefaultSwitch()
    {   
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_type = addslashes($this->request->param('storeType'));
        $id = addslashes($this->request->param('storeId'));
        Db::startTrans();
        $r0 = CustomerModel::where(['recycle'=>0,'id'=>$id])->field('is_default')->select()->toArray();
        if($r0)
        {
            $is_default = $r0[0]['is_default']; // 是否默认 0.不是默认 1.默认
            if($is_default == 1)
            { // 默认
                $sql1 = CustomerModel::find($id);
                $sql1->is_default = 0;
                $r1 = $sql1->save();
                if($r1)
                {
                    $sql2 = "update lkt_customer set is_default = 1 where recycle = 0 and id != '$id' order by id limit 1 ";
                    $r2 = Db::execute($sql2);
                    if($r2)
                    {
                        Db::commit();
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改默认值成功!sql:'.$sql2;
                        $this->Log($Log_content);
                        $message = Lang('Success');
                        return output(200,$message);
                    }
                    else
                    {
                        Db::rollback();
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改默认值失败!id:'.$id;
                        $this->Log($Log_content);
                        $message = Lang('operation failed');
                        return output(ERROR_CODE_CZSB,$message);
                    }
                }
                else
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改默认值失败!id:'.$id;
                    $this->Log($Log_content);
                    $message = Lang('operation failed');
                    return output(ERROR_CODE_CZSB,$message);
                }
            }
            else
            {
                $sql1 = CustomerModel::where('is_default',1)->where('id','<>',$id)->find();
                $sql1->is_default = 0;
                $r1 = $sql1->save();
                if($r1)
                {
                    $sql2 = CustomerModel::find($id);
                    $sql2->is_default = 1;
                    $r2 = $sql2->save();
                    if($r2)
                    {
                        Db::commit();
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改默认值成功!id:'.$id;
                        $this->Log($Log_content);
                        $message = Lang('Success');
                        return output(200,$message);
                    }
                    else
                    {
                        Db::rollback();
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改默认值失败!id:'.$id;
                        $this->Log($Log_content);
                        $message = Lang('operation failed');
                        return output(ERROR_CODE_CZSB,$message);
                    }
                }
                else
                {
                    $db->rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改默认值失败!id:'.$id;
                    $this->Log($Log_content);
                    $message = Lang('operation failed');
                    return output(ERROR_CODE_CZSB,$message);
                }
            }
        }

    }

    // 判断商城是否有自营店
    public function checkShopHavaSelfOwnedShop()
    {   
        $storeId = addslashes($this->request->param('storeId'));
        $storeType = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $res = PC_Tools::SelfOperatedStore($storeId);
        $message = Lang('Success');
        if($res == 0)
        {
            return output(200,$message,true);
        }
        else
        {
            return output(200,$message,false);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/shop.log",$Log_content);
        return;
    }
}
