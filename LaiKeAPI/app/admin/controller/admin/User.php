<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\GETUI\LaikePushTools;
use app\common\ExcelUtils;
use PhpOffice\PhpSpreadsheet\IOFactory;

use app\admin\model\AdminModel;
use app\admin\model\ConfigModel;
use app\admin\model\UserModel;
use app\admin\model\UserGradeModel;
use app\admin\model\UserDistributionModel;
use app\admin\model\UserRuleModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\ReturnRecordModel;
use app\admin\model\MchModel;
use app\admin\model\RecordModel;
use app\admin\model\PrizeConfigModel;
use app\admin\model\PrizePayrecordModel;
use app\admin\model\SignRecordModel;
use app\admin\model\FinanceConfigModel;
use app\admin\model\WithdrawModel;
use app\admin\model\UserAddressModel;
use app\admin\model\UserCollectionModel;
/**
 * 功能：后台会员类
 * 修改人：PJY
 */
class User extends BaseController
{   
    // 会员管理
    public function getUserInfo()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $page = trim($this->request->param('pageNo'));//页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据

        $vague = addslashes(trim($this->request->param('vague')));// 是否模糊查询 1=是 0=否
        $key = addslashes(trim($this->request->param('key')));// 用户ID/用户昵称/用户账号
        $uid = addslashes(trim($this->request->param('uid')));// 用户ID
        $source = addslashes(trim($this->request->param('source'))); // 来源
        $cpc = addslashes(trim($this->request->param('cpc'))); // 区号
        $country_num = addslashes(trim($this->request->param('country_num'))); // 国家代码
        $tel = addslashes(trim($this->request->param('tel'))); // 联系电话
        $exportType = addslashes($this->request->param('exportType')); // 导出
        
        $pageto = $this->request->param('exportType'); // 每页显示多少条数据
        $page = (int)$this->request->param('pageNo'); // 页码
        $pagesize = (int)$this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize : '10';

        if ($page)
        {
            $page = $page;
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $page = 1;
            $start = 0;
        }
        $Self_operated_store = PC_Tools::SelfOperatedStore($store_id);
        
        $condition = " store_id = $store_id and is_lock = 0 ";
        //过滤PC店铺添加的管理员
        $condition .= " and user_id not in (select main_id from lkt_user_authority where type=1) ";
        if($key != '')
        {
            if($vague == 1)
            {
                $key = Tools::FuzzyQueryConcatenation($key);
                $condition .= " and ( `user_id` like $key OR `user_name` like $key OR `e_mail` like $key ) ";
            }
            else
            {
                $condition .= " and user_id = '$key' ";
            }
        }
        if($uid != '')
        {
            $condition .= " and user_id = '$uid' ";
        }
        if ($cpc != '')
        {
            $condition .= " and cpc = '$cpc' ";
        }
        if ($tel != '')
        {
            $tel = Tools::FuzzyQueryConcatenation($tel);
            $condition .= " and mobile like $tel ";
        }
        if ($source != '')
        {
            $condition .= " and source = $source ";
        }

        $total = UserModel::where($condition)->count();
        $list = array();
        $r = UserModel::where($condition)->order('Register_data','desc')->page($page,$pagesize)->select()->toArray();
        if($r)
        {
            //判断分销表关联表
            $sql_name = "select DATABASE() as name";
            $res_name = Db::query($sql_name);
            $db_name = $res_name[0]['name'];
            //TABLE_NAME TABLE_SCHEMA
            $sql_dis = "select * from information_schema.TABLES where TABLE_NAME = 'lkt_user_distribution' and TABLE_SCHEMA = '$db_name'";
            $res_dis = Db::query($sql_dis);
            //查询订单数以及会员等级
            foreach ($r as $key => $value)
            {
                $preferred_currency = $value['preferred_currency'];
                $userCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>$preferred_currency));
                $r[$key]['currency_symbol'] = $userCurrency[0]['currency_symbol'];
                $r[$key]['exchange_rate'] = $userCurrency[0]['exchange_rate'];
                $r[$key]['currency_code'] = $userCurrency[0]['currency_code'];

                $r1 = OrderModel::where(['store_id'=>$store_id,'user_id'=>$value['user_id']])
                                ->where('status','>',0)
                                ->where('status','not in','7')
                                ->where('pay_time','not null')
                                ->sum('z_price');
                $r[$key]['z_price'] = round($r1,2);
                $return = '';
                // 查询退款的订单
                $r_r = ReturnRecordModel::where(['store_id'=>$store_id,'user_id'=>$value['user_id']])
                                        ->where('r_type','not in','1,4,9,11')
                                        ->field('sNo')
                                        ->select()
                                        ->toArray();
                if ($r_r)
                {
                    foreach ($r_r as $k => $v)
                    {
                        $return .=  $v['sNo']  . ',';
                    }
                    $return = trim($return, ',');
                }
                $whe[] = ['status','>',0];
                if ($return != '')
                {
                    $whe[] = ['sNo','not in',$return];
                }
                $r[$key]['z_num'] = OrderModel::where(['user_id'=>$value['user_id'],'store_id'=>$store_id])
                                               ->where($whe)
                                               ->where('status','not in','4,7,11')
                                               ->where('pay_time','not null')
                                               ->count();

                $res_0 = UserGradeModel::where(['store_id'=>$store_id,'id'=>$value['grade']])->field('name')->select()->toArray();
                if ($res_0)
                {
                    $r[$key]['grade'] = $res_0[0]['name'];
                }
                else
                {
                    $r[$key]['grade'] = '普通会员';
                }
                //判断是否可以删除
                $r[$key]['isDelBtn'] = 1; //1是可删除吧

                if($res_dis)
                {
                    $res_d = UserDistributionModel::where('user_id',$value['user_id'])->field('level')->select()->toArray();
                    if($res_d)
                    {
                        $r[$key]['level'] = $res_d[0]['level'];
                        // $r[$key]['isDelBtn'] = 0;
                    }
                    else
                    {
                        $r[$key]['level'] = '';
                    }
                }
                else
                {
                    $r[$key]['level'] = '';
                }

                //判断店铺
                $res_m = MchModel::where(['user_id'=>$value['user_id'],'store_id'=>$store_id,'recovery'=>0])
                                ->where('review_status','in','0,1')
                                ->select()
                                ->toArray();
                if($res_m)
                {
                    $r[$key]['is_mch'] = 1;
                    if($Self_operated_store == $res_m[0]['id'])
                    {
                        $r[$key]['isDelBtn'] = 0;
                    }
                }
                else
                {
                    $r[$key]['is_mch'] = 0;
                }
                //判断支付密码设置
                if($value['password'])
                {
                    $r[$key]['isPaymentPwd'] = true;
                }
                else
                {
                    $r[$key]['isPaymentPwd'] = false;
                }
                
                // //余额
                // if($value['money'] > 0)
                // {
                //     $r[$key]['isDelBtn'] = 0;
                // }
                // //订单
                // if($r1 > 0)
                // {
                //     $r[$key]['isDelBtn'] = 0;
                // }
                // //店铺
                // if($res_m)
                // {
                //     $r[$key]['isDelBtn'] = 0;
                // }
                //获取用户默认地址
                $res_a = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$value['user_id'],'is_default'=>1])->select()->toArray();
                if($res_a)
                {
                    $r[$key]['userAddress'] = $res_a[0];
                }
                //金额返数字类型
                $r[$key]['money'] = floatval($value['money']);
            }
            $list = $r;
        }

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '用户ID',
                1 => '用户头像',
                2 => '用户名称',
                3 => '用户账号',
                4 => '手机号码',
                5 => '邮箱地址',
                6 => '账户余额',
                7 => '积分余额',
                8 => '有效订单数',
                9 => '交易金额',
                10 => '注册时间'
            );
            $exportExcel_list = array();

            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $zhanghao = "";
                    if($v['zhanghao'] != '')
                    {
                        $zhanghao = "'" .$v['zhanghao'];
                    }
                    $tel = "";
                    if($v['mobile'] != '')
                    {
                        $tel = "'" .$v['cpc'].$v['mobile'];
                    }
                    $e_mail = "";
                    if($v['e_mail'] != '')
                    {
                        $e_mail = "'" .$v['e_mail'];
                    }
                    
                    $exportExcel_list[] = array(
                        $v['user_id'],
                        $v['headimgurl'],
                        $v['user_name'],
                        $zhanghao,
                        $tel,
                        $e_mail,
                        $v['money'],
                        $v['score'],
                        $v['z_num'],
                        $v['z_price'],
                        $v['Register_data']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '用户列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }

        if (!$exportType && $list)
        {
            foreach ($list as $k => $v)
            {
                if (isset($v['mobile']) && $v['mobile'] !== '')
                {
                    $list[$k]['mobile'] = $this->maskMobile($v['mobile']);
                }
                if (isset($v['ID_number']) && $v['ID_number'] !== '')
                {
                    $list[$k]['ID_number'] = $this->maskIdNumber($v['ID_number']);
                }
                if (isset($v['id_number']) && $v['id_number'] !== '')
                {
                    $list[$k]['id_number'] = $this->maskIdNumber($v['id_number']);
                }
            }
        }

        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total));
    }

    // 获取自定义账号
    public function generateAccount()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
         
        $randomString = Tools::generateRandomString(8);

        $message = Lang("Success");
        return output(200,$message,$randomString);
    }

    //添加会员页面
    public function getUserConfigInfo()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        //默认头像 和 昵称
        $res = ConfigModel::where('store_id',$store_id)->field('wx_headimgurl,wx_name')->select()->toArray();
        if ($res)
        {
            $wx_name = $res[0]['wx_name'];
            $wx_headimgurl = ServerPath::getimgpath($res[0]['wx_headimgurl'], $store_id);
        }
        else
        {
            $wx_name = '';
            $wx_headimgurl = '';
        }
        $message = Lang("Success");
        return output(200,$message,array('wx_name'=>$wx_name,'wx_headimgurl'=>$wx_headimgurl,'grade'=>[],'gradeRule'=>[]));
    }

    // 添加会员
    public function saveUser()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim(Request::param('accessId')));

        $wx_headimgurl = addslashes(trim($this->request->param('headerUrl'))); //用户头像
        $wx_name = addslashes(trim($this->request->param('userName'))); //用户名
        $zhanghao = trim($this->request->param('zhanghao')); //账号
        $mima = $this->request->param('mima'); //密码
        $cpc = addslashes(trim($this->request->param('cpc'))); // 区号
        $country_num = addslashes(trim($this->request->param('country_num'))); // 国家代码
        $mobile = addslashes(trim($this->request->param('phone'))); //手机号
        $e_mail = addslashes(trim($this->request->param('e_mail'))); // 邮箱
        $source = addslashes(trim($this->request->param('source'))); //来源
        $sex = $this->request->param('sex');//1:男 2:女

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $log = new LaiKeLogUtils();
        $user_zhanghao = $zhanghao; //用户账号
        // 密码判断
        if (strlen($mima) < 6)
        {
            $message = Lang("user.0");
            return output(ERROR_CODE_MMBFHGF,$message);
        }
        else
        {
            $mima = Tools::lock_url($mima);
        }
        // 账号唯一性判断
        $res_1 = UserModel::where('store_id',$store_id)->where('zhanghao|mobile|e_mail',$user_zhanghao)->field('id')->select()->toArray();
        if ($res_1)
        {
            $message = Lang("user.1");
            return output(ERROR_CODE_ZHYCZ,$message);
        }

        if($mobile == '')
        {
            $message = Lang("user.59");
            return output(ERROR_CODE_SJHGSBZQ,$message);
        }

        // 根据商城ID、区号、国家代码、手机号，查询用户信息
        $sql_m = "select id from lkt_user where store_id = '$store_id' and cpc = '$cpc' and country_num = '$country_num' and mobile = '$mobile' ";
        $r_m = Db::query($sql_m);
        if($r_m)
        { // 存在
            $message = Lang("user.3");
            return output(ERROR_CODE_SJHYBZC,$message);
        }

        if($e_mail == '')
        {
            $message = Lang("user.60");
            return output(ERROR_CODE_SJHGSBZQ,$message);
        }
        else
        {
            // 根据商城ID、邮箱，查询用户信息
            $sql_m = "select id from lkt_user where store_id = '$store_id' and e_mail = '$e_mail' ";
            $r_m = Db::query($sql_m);
            if($r_m)
            { // 存在
                $message = Lang("login.22");
                return output(ERROR_CODE_SJHYBZC,$message);
            }
        }

        // 默认推荐人
        $ppid = '';
        $res_de = UserModel::where('store_id',$store_id)->field('user_id')->limit(0,1)->select()->toArray();
        if($res_de)
        {
            $ppid = $res_de[0]['user_id'];
        }   
        // 事务开启
        Db::startTrans();
        $time = date("Y-m-d H:i:s");

        $code = true;
        $sql = new UserModel();
        $sql->headimgurl = $wx_headimgurl;
        $sql->user_name = $wx_name;
        $sql->zhanghao = $zhanghao;
        $sql->cpc = $cpc;
        $sql->country_num = $country_num;
        $sql->mobile = $mobile;
        $sql->e_mail = $e_mail;
        $sql->mima = $mima;
        $sql->sex = $sex;//性别 0:未知 1:男 2:女
        $sql->source = $source;
        $sql->store_id = $store_id;
        $sql->Referee = $ppid;
        $sql->is_default_value = 1;
        $sql->is_default_birthday = 1;
        $sql->Register_data = $time;
        $sql->birthday = $time;
        $sql->save();
        $res = $sql->id;
        if ($res < 0)
        {   
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '添加了用户账号：'.$zhanghao.'失败',1,1,0,$operator_id);
            $log->log('common/userlist.log',__LINE__ . ':插入用户记录失败，mobile为：' . $mobile . "\r\n");
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
        $r0 = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r0)
        {
            $user_id1 = $r0[0]['user_id']; //默认用户名ID前缀
        }
        else
        {   
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '添加了用户账号：'.$zhanghao.'失败',1,1,0,$operator_id);
            $log->log('common/userlist.log',__LINE__ . '没有配置后台用户的系统设置' . "\r\n");
            $message = Lang("user.4");
            return output(ERROR_CODE_QWSXTSZ,$message);
        }

        //更新user_id
        $rr = UserModel::where('store_id',$store_id)->max('id');
        $user_id = $user_id1 . ($rr + 1); //新注册的用户user_id

        $sql_1 = UserModel::where(['store_id'=>$store_id,'id'=>$res])->find();
        $sql_1->user_id = $user_id;
        $res_1 = $sql_1->save();
        if (!$res_1)
        {   
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '添加了用户账号：'.$zhanghao.'失败',1,1,0,$operator_id);
            $log->log('common/userlist.log',__LINE__ . ':更新用户user_id失败，id为：' . $res . "\r\n");
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }

        $event = '会员' . $user_id . '注册成功';
        // 在操作列表里添加一条会员登录信息
        $sql = new RecordModel();
        $sql->store_id = $store_id;
        $sql->user_id = $user_id;
        $sql->event = $event;
        $sql->type = 0;
        $sql->save();
        $r01 = $sql->id;
        if ($r01 < 0)
        {   
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '添加了用户账号：'.$zhanghao.'失败',1,1,0,$operator_id);
            $log->log('common/userlist.log',__LINE__ . ':添加会员登录消息失败，user_id为：' . $event . "\r\n");
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
        Db::commit();
        $Jurisdiction->admin_record($store_id, $operator, '添加了用户ID：'.$user_id,1,1,0,$operator_id);
        $message = Lang("Success");
        return output(200,$message);   
    }

    // 保存会员设置
    public function addUserRule()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim(Request::param('accessId')));
        $wx_headimgurl = addslashes(trim($this->request->param('wxImgUrl'))); //用户头像
        $wx_name = addslashes(trim($this->request->param('wxName'))); //用户名

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $log = new LaiKeLogUtils();
        //默认头像，用户名设置
        $res = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($res)
        {
            if ($wx_headimgurl)
            {
                $wx_headimgurl = preg_replace('/.*\//', '', $wx_headimgurl); //获取头像名称
            }
            $sql_0 = ConfigModel::where('store_id',$store_id)->find();
            $sql_0->wx_headimgurl = $wx_headimgurl;
            $sql_0->wx_name = $wx_name;
            $res_0 = $sql_0->save();
            if (!$res_0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了用户设置信息失败',2,1,0,$operator_id);
                $log->log('common/userlist.log',__LINE__ . ':更新系统配置失败，store_id为：' . $store_id . "\r\n");
                $message = Lang("user.5");
                return output(ERROR_CODE_CZSB,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了用户设置信息',2,1,0,$operator_id);
                $message = Lang("Success");
                return output(200,$message); 
            }
        }
        else
        {
            $message = Lang("user.4");
            return output(ERROR_CODE_QWSXTSZ,$message);
        }
    }

    // 修改会员
    public function updateUserById()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim(Request::param('accessId')));
        //1.接受用户传来的修改数据
        $user_id = $this->request->param("userId");//会员ID
        $user_name = $this->request->param("uname");//会员名称
        $cpc = addslashes(trim($this->request->param('cpc'))); // 区号
        $country_num = addslashes(trim($this->request->param('country_num'))); // 国家代码
        $mobile = addslashes(trim($this->request->param('phone'))); //手机号
        $e_mail = addslashes(trim($this->request->param('e_mail'))); // 邮箱
        $mima = $this->request->param("pwd"); //登录密码
        $password = $this->request->param('paypwd'); //支付密码
        $headimgurl = $this->request->param('headerUrl');//头像

        $money = $this->request->param('money')?$this->request->param('money'):0; //余额
        $score = $this->request->param('jifen')?$this->request->param('jifen'):0; //积分
        $birthday = addslashes($this->request->param('birthday')); //会员生日日期
        $sex = $this->request->param('sex');//1:男 2:女
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $log = new LaiKeLogUtils();
        $now = date("Y-m-d H:i:s"); //单前时间
        $r = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('cpc,source,zhanghao,password,mobile,user_id')->select()->toArray();
        $user_cpc = $r[0]['cpc']; //用户账号
        $user_zhanghao = $r[0]['zhanghao']; //用户账号
        $user_mobile = $r[0]['mobile']; //用户手机号
        $password_db = $r[0]['password']; //数据库中的密码

        if ($password != '')
        {
            if (strstr( $password, ' ')) 
            {
                $message = Lang("user.6");
                return output(ERROR_CODE_MMBFHGF,$message);
            }
            if ($password != $password_db)
            {
                if (strlen($password) != 6)
                {
                    $message = Lang("user.7");
                    return output(ERROR_CODE_MMBFHGF,$message);
                }
                else
                {
                    $password = md5($password);
                }
            }
        }

        if (empty($user_name))
        {   
            $res = ConfigModel::where('store_id',$store_id)->field('wx_name')->select()->toArray();
            if($res)
            {
                $user_name = $res[0]['wx_name'];
            }
            else
            {
                $user_name = '来客电商粉丝';
            }
        }

        if($mobile == '')
        {
            $message = Lang("user.59");
            return output(ERROR_CODE_SJHGSBZQ,$message);
        }

        // 根据商城ID、区号、国家代码、手机号，查询用户信息
        $sql_m = "select id from lkt_user where store_id = '$store_id' and cpc = '$cpc' and country_num = '$country_num' and mobile = '$mobile' and user_id != '$user_id'  ";
        $r_m = Db::query($sql_m);
        if($r_m)
        { // 存在
            $message = Lang("user.3");
            return output(ERROR_CODE_SJHYBZC,$message);
        }

        if($e_mail == '')
        {
            $message = Lang("user.60");
            return output(ERROR_CODE_SJHGSBZQ,$message);
        }
        else
        {
            // 根据商城ID、邮箱，查询用户信息
            $sql_m = "select id from lkt_user where store_id = '$store_id' and e_mail = '$e_mail' and user_id != '$user_id'  ";
            $r_m = Db::query($sql_m);
            if($r_m)
            { // 存在
                $message = Lang("login.22");
                return output(ERROR_CODE_SJHYBZC,$message);
            }
        }

        if($mima != '')
        {
            if (strstr( $mima, ' ')) 
            {
                $message = Lang("user.8");
                return output(ERROR_CODE_MMBFHGF,$message);
            }
            if (strlen($mima) < 6)
            {
                $message = Lang("user.0");
                return output(ERROR_CODE_MMBFHGF,$message);
            }
            if($mima)
            {
                $mima = Tools::lock_url($mima);
            }
        }
        
        $sql = UserModel::where(['user_id'=>$user_id,'store_id'=>$store_id])->find();
        $sql->user_name = $user_name;
        $sql->cpc = $cpc;
        $sql->country_num = $country_num;
        $sql->mobile = $mobile;
        $sql->e_mail = $e_mail;
        $sql->money = $money;
        $sql->score = $score;
        $sql->sex = $sex;
        if($mima != '')
        {
            $sql->mima = $mima;
        }
        if($password != '')
        {
            $sql->password = $password;
        }
        // if($user_zhanghao == $user_mobile || $user_zhanghao == $user_cpc.$user_mobile)
        // {
        //     $sql->zhanghao = $cpc.$mobile;
        // }
        if($birthday != '')
        {   
            $sql->birthday = $birthday;
        }
        if($sex != '')
        {
            $sql->sex = $sex;
        }
        if($headimgurl != '')
        {
            $sql->headimgurl = $headimgurl;
        }
        $res = $sql->save();

        //3.根据操作结果，提示修改成功或失败
        if (!$res)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了用户ID：'.$user_id.' 的信息失败',2,1,0,$operator_id);
            $log->log('common/userlist.log',__LINE__ . ':更新用户信息失败，user_id为：' . $user_id . "\r\n");
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了用户ID：'.$user_id.' 的信息',2,1,0,$operator_id);
            $message = Lang("Success");
            return output(200,$message); 
        }
    }

    // 删除会员
    public function delUserById()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim(Request::param('accessId')));
        $id = $this->request->param("id");//会员主键ID
        $log = new LaiKeLogUtils();
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        //查询会员信息
        $res = UserModel::where(['store_id'=>$store_id,'id'=>$id])->select()->toArray();
        if($res)
        {
            $user_id = $res[0]['user_id'];
            // 余额
            if($res[0]['money'] > 0)
            {
                $message = Lang("删除失败该用户有余额未使用!");
                return output(ERROR_CODE_CSCW,$message,null);
            }
            // 订单
            $r1 = OrderModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->where('status','>',0)->where('status','not in','7')->where('pay_time','not null')->sum('z_price');
            if($r1 > 0)
            {
                $message = Lang("删除失败该用户有未关闭的订单!");
                return output(ERROR_CODE_CSCW,$message);
            }
            // 店铺
            $res_m = MchModel::where(['user_id'=>$user_id,'store_id'=>$store_id,'recovery'=>0])->where('review_status','in','0,1')->select()->toArray();
            if($res_m)
            {
                $message = Lang("删除失败该用户有店铺未注销");
                return output(ERROR_CODE_CSCW,$message,null);
            }
            $res_d = UserDistributionModel::where('user_id',$user_id)->where('level','<>','0')->field('level')->select()->toArray();
            if($res_d)
            {
                $message = Lang("该用户是分销商不能删除");
                return output(ERROR_CODE_CSCW,$message,null);
            }
        }
        else
        {
            $message = Lang("Parameter error");
            return output(ERROR_CODE_CSCW,$message,null);
        }
        $code = true;
        Db::startTrans();
        $res = UserModel::where(['store_id'=>$store_id,'id'=>$id])->delete();
        if ($res < 0)
        {
            Db::rollback();
            $log->log('common/userlist.log',__LINE__ . ':删除会员订单主单失败，user_id为：' . $user_id . "\r\n");
            $code = false;
        }
        //删除订单
        $res1 = OrderModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->delete();
        if($res1 < 0)
        {   
            Db::rollback();
            $log->log('common/userlist.log',__LINE__ . ':删除会员订单主单失败，user_id为：' . $user_id . "\r\n");
            $code = false;
        }
        //删除详单
        $res2 = OrderDetailsModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->delete();
        if($res2 < 0)
        {   
            Db::rollback();
            $log->log('common/userlist.log',__LINE__ . ':删除会员订单详单失败，user_id为：' . $user_id . "\r\n");
            $code = false;
        }
        //删除用户地址
        $res3 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->delete();
        if ($res3 < 0)
        {
            Db::rollback();
            $log->log('common/userlist.log',__LINE__ . ':删除会员收货地址失败，user_id为：' . $user_id . "\r\n");
            $code = false;
        }
        //删除用户收藏信息
        $res4 = UserCollectionModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->delete();
        if ($res4 < 0)
        {
            Db::rollback();
            $log->log('common/userlist.log',__LINE__ . ':删除会员收藏信息失败，user_id为：' . $user_id . "\r\n");
            $code = false;
        }
        //删除用户店铺信息
        $res5 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->delete();
        if ($res5 < 0)
        {
            Db::rollback();
            $log->log('common/userlist.log',__LINE__ . ':删除会员店铺失败，user_id为：' . $user_id . "\r\n");
            $code = false;
        }
        if($code == true)
        {
            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator, '删除了用户ID：'.$user_id,3,1,0,$operator_id);
            $message = Lang("Success");
            return output(200,$message); 
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了用户ID：'.$user_id.'失败',3,1,0,$operator_id);
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
    }

    //会员充值
    public function userRechargeMoney()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim(Request::param('accessId')));
        $log = new LaiKeLogUtils();
        $id = trim($this->request->param('id'));
        $price = trim($this->request->param('money'));//充值额
        $type = trim($this->request->param('type'));//1充值余额3充值积分
        $remake = trim($this->request->param('remake'));// 备注
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if($price == '')
        {
            $message = Lang("Parameter error");
            return output(ERROR_CODE_CSCW,$message);
        }
        $sql_u = UserModel::where('id',$id)->select()->toArray();
        $user_id = $sql_u[0]['user_id'];

        Db::startTrans();

        $old_money = $sql_u[0]['money'];
        $old_score = $sql_u[0]['score'];
        $sql = UserModel::find($id);
        switch ($type) 
        {
            case '3':
                if($old_score + $price < 0)
                {
                    $message = Lang("user.9");
                    return output(ERROR_CODE_CZSB,$message);
                }
                if($price < 0)
                {
                    $sql->score = Db::raw('score + '.$price);
                    $event = "将用户ID：'.$user_id.' 进行了积分扣减";
                }
                else
                {
                    $sql->score = Db::raw('score + '.$price);
                    $event = "将用户ID：'.$user_id.' 进行了积分充值";
                }
                break;
            
            default:
                if($old_money + $price < 0)
                {
                    $message = Lang("user.10");
                    return output(ERROR_CODE_CZSB,$message);
                }
                if($price < 0)
                {
                    $sql->money = Db::raw('money + '.$price);
                    $event = "将用户ID：'.$user_id.' 进行了余额扣减";
                }
                else
                {
                    $sql->money = Db::raw('money + '.$price);
                    $event = "将用户ID：'.$user_id.' 进行了余额扣减";
                }
                break;
        }
        $res = $sql->save();
        if(!$res)
        {
            Db::rollback();
            $event .= '失败';
            $Jurisdiction->admin_record($store_id, $operator, $event,2,1,0,$operator_id);
            $log->log('common/userlist.log',__LINE__ . ':更新会员余额失败，user_id为：' . $user_id . "\r\n");
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
        if($type == 1)
        {   
            if($price < 0)
            {
                $array = array('store_id'=>$store_id,'money'=>abs($price),'user_money'=>$old_money,'type'=>6,'money_type'=>2,'money_type_name'=>7,'record_notes'=>$remake,'type_name'=>'','s_no'=>'','title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
            }
            else
            {
                $array = array('store_id'=>$store_id,'money'=>abs($price),'user_money'=>$old_money,'type'=>2,'money_type'=>1,'money_type_name'=>2,'record_notes'=>$remake,'type_name'=>'','s_no'=>'','title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>'','withdrawal_method'=>'');
            }
            $details_id = PC_Tools::add_Balance_details($array);

            $sqll = new RecordModel();
            $sqll->store_id = $store_id;
            $sqll->user_id = $user_id;
            $sqll->money = abs($price);
            $sqll->oldmoney = $old_money;
            $sqll->add_date = date('Y-m-d H:i:s');
            $sqll->details_id = $details_id;
            if($price < 0)
            {   
                $event = '将用户ID：' . $user_id . '，系统扣除了：' . abs($price) . '余额';
                $sqll->type = 11;
                $sqll->event = $event;
            }
            else
            {
                $event = '将用户ID：' . $user_id . '，充值了：' . abs($price) . '余额';
                $sqll->type = 14;
                $sqll->event = $event;
            }
            $sqll->save();
            $rr = $sqll->id;
            if ($rr < 0)
            {   
                Db::rollback();
                $event .= '失败';
                $Jurisdiction->admin_record($store_id, $operator, $event,2,1,0,$operator_id);
                $log->log('common/userlist.log',__LINE__ . ':插入余额记录失败，user_id为：' . $user_id .'-' . $price ."\r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }
        else
        {   
            //添加日志
            $sqll = new RecordModel();
            $sqll->store_id = $store_id;
            $sqll->user_id = $user_id;
            $sqll->money = abs($price);
            $sqll->oldmoney = $old_score;
            $sqll->add_date = date('Y-m-d H:i:s');

            //积分日志
            $sql = new SignRecordModel();
            $sql->store_id = $store_id;
            $sql->user_id = $user_id;
            $sql->sign_score = abs($price);
            $sql->sign_time = date('Y-m-d H:i:s');

            if($price < 0)
            {
                $event = '将用户ID：' . $user_id . '，系统扣除了：' . abs($price) . "积分";
                $sqll->event = $event;
                $sqll->type = 17;
                
                $sql->record = $event;
                $sql->type = 5;
            }
            else
            {
                $event = '将用户ID：' . $user_id . '，充值了：' . abs($price) . "积分";
                $sqll->event = $event;
                $sqll->type = 15;

                $sql->record = $event;
                $sql->type = 6;
            }
            $sqll->save();
            $rr = $sqll->id;
            if ($rr < 0)
            {   
                Db::rollback();
                $event .= '失败';
                $Jurisdiction->admin_record($store_id, $operator, $event,2,1,0,$operator_id);
                $log->log('common/userlist.log',__LINE__ . ':插入积分记录失败，user_id为：' . $user_id .'-' . $price ."\r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            $sql->save();
            $r = $sql->id;
            if ($r < 0)
            {   
                Db::rollback();
                $event .= '失败';
                $Jurisdiction->admin_record($store_id, $operator, $event,2,1,0,$operator_id);
                $log->log('common/userlist.log',__LINE__ . ':插入积分记录失败，user_id为：' . $user_id .'-' . $price ."\r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        } 

        Db::commit();
        $Jurisdiction->admin_record($store_id, $operator, $event,2,1,0,$operator_id);
        $message = Lang("Success");
        return output(200,$message);
    }

    // 充值列表
    public function getupInfo()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim(Request::param('accessId')));
        $page = trim($this->request->param('pageNo'));//页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据
        $pagesize = $pagesize ? $pagesize : '10'; // 每页显示多少条数据
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        // 接收参数
        $exportType = addslashes($this->request->param('exportType'));//是否导出
        $name = addslashes(trim($this->request->param('key'))); // 用户名称、user_id、手机号码
        $type = addslashes(trim($this->request->param('oType'))); // 类型
        $startdate = $this->request->param('startDate'); //开始时间
        $enddate = $this->request->param('endDate'); //结束时间
        $pageto = $this->request->param('pageto'); // 导出
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }
        $condition = " c.store_id = '$store_id' and (a.type = 1 or a.type = 14 or (a.type = 11 and b.type = 6)) ";
        $condition1 = " c.store_id = '$store_id' ";
        if ($name)
        {
            $name = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and (c.user_id like $name or c.user_name like $name or c.mobile like $name) ";
            $condition1 .= " and (c.user_id like $name or c.user_name like $name or c.mobile like $name) ";
        }
        if ($type)
        {
            $condition .= " and a.type = '$type' ";
            $condition1 .= " and a.type = '$type' ";
        }
        if ($startdate)
        {
            $condition .= " and a.add_date >= '$startdate' ";
            $condition1 .= " and a.add_date >= '$startdate' ";
        }
        if ($enddate)
        {
            $condition .= " and a.add_date <= '$enddate' ";
            $condition1 .= " and a.add_date <= '$enddate' ";
        }
        $total = 0;
        $sql_num = "select count(1) as num from lkt_record as a left join lkt_record_details as b on a.details_id = b.id left join lkt_user as c on c.user_id = a.user_id  where $condition order by a.add_date desc";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        $allMoney = 0;
        $sql_money = "select sum(a.money) as money from lkt_record as a left join lkt_record_details as b on a.details_id = b.id left join lkt_user as c on c.user_id = a.user_id  where $condition1 and (a.type = 1 or a.type = 14) ";
        $res_money = Db::query($sql_money);
        if($res_money)
        {
            $allMoney = $res_money[0]['money'];
        }

        $sql_money1 = "select sum(a.money) as money from lkt_record as a left join lkt_record_details as b on a.details_id = b.id left join lkt_user as c on c.user_id = a.user_id  where $condition and a.type = 11 ";
        $res_money1 = Db::query($sql_money1);
        if($res_money1)
        {
            $allMoney = $allMoney - $res_money1[0]['money'];
        }

        $list = array();
        if ($total > 0)
        {
            // 页码调整
            if ($start > $total)
            {
                $page = 1;
                $start = 0;
            }
            $sql = "select a.user_id,a.type,a.add_date,c.user_name,c.mobile,c.source,a.money,c.Register_data,c.zhanghao,b.record_notes as remake from lkt_record as a left join lkt_record_details as b on a.details_id = b.id left join lkt_user as c on a.user_id = c.user_id where $condition order by a.add_date desc limit $start,$pagesize";
            $res = Db::query($sql);
            foreach ($res as $key => $value) 
            {
                $res[$key]['money'] = (float)$value['money'];

                $res[$key]['sourceName'] = PC_Tools::get_source($value['source']);

                switch ($value['type']) 
                {
                    case '14':
                        $res[$key]['typeName'] = '系统充值';
                        break;
                    case '11':
                        $res[$key]['money'] = 0 - (float)$value['money'];
                        $res[$key]['typeName'] = '系统扣款';
                        break;
                    default:
                        $res[$key]['typeName'] = '用户充值';
                        break;
                }
                
                $res[$key]['recordType'] = $res[$key]['type'];
            }
            if($pageto)
            {
                $Jurisdiction->admin_record($store_id, $operator, '导出充值管理列表',4,1,0,$operator_id);
            }
            $list = $res;
        }
        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '序号',
                1 => '用户ID',
                2 => '用户名称',
                3 => '充值总金额',
                4 => '来源',
                5 => '手机号码',
                6 => '类型',
                7 => '备注',
                8 => '充值时间'
            );
            $exportExcel_list = array();

            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $k+1,
                        $v['user_id'],
                        $v['user_name'],
                        $v['money'],
                        $v['sourceName'],
                        $v['mobile'],
                        $v['typeName'],
                        $v['remake'],
                        $v['add_date']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '充值列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total,'allMoney'=>$allMoney));
    }

    // 提现审核
    public function getWithdrawalInfo()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim(Request::param('accessId')));

        $name = addslashes(trim($this->request->param('userNameAndPhone'))); // 用户名/联系电话
        $withdrawStatus = addslashes(trim($this->request->param('withdrawStatus'))); // 1.微信零钱 2.银行卡
        $startdate = $this->request->param('startDate'); //开始时间
        $enddate = $this->request->param('endDate'); //结束时间
        $pageto = $this->request->param('pageto'); // 导出
        $page = $this->request->param('pageNo'); // 页码
        $pagesize = $this->request->param('pageSize'); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10';
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        $condition = " a.store_id = '$store_id' and c.store_id = '$store_id' and a.status = 0 and a.is_mch = 0 ";

        if ($name)
        {
            $name = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and (a.name like $name or a.mobile like $name) ";
        }
        if ($withdrawStatus)
        {
            $condition .= " and a.withdraw_status = '$withdrawStatus' ";
        }
        if ($startdate)
        {
            $condition .= " and a.add_date >= '$startdate' ";
        }
        if ($enddate)
        {
            $condition .= " and a.add_date <= '$enddate' ";
        }
        $total = 0;
        $sql_num = "select count(a.id) as num from lkt_withdraw as a left join lkt_bank_card as b on a.Bank_id = b.id right join lkt_user as c on a.user_id = c.user_id where $condition";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        if ($total > 0)
        {
            // 页码调整
            if ($start > $total)
            {
                $page = 1;
                $start = 0;
            }
            $sql = "select a.id,a.user_id,a.name as userName,a.add_date,a.money,a.s_charge,a.mobile,a.status,a.withdraw_status,a.txsno as sNo,b.Cardholder,b.Bank_name,b.Bank_card_number,c.source,b.branch from lkt_withdraw as a left join lkt_bank_card as b on a.Bank_id = b.id right join lkt_user as c on a.user_id = c.user_id where $condition order by a.add_date desc limit $start,$pagesize ";
            if($pageto)
            {
                $Jurisdiction->admin_record($store_id, $operator, '导出待审核列表',4,1,0,$operator_id);
            }
            $r1 = Db::query($sql);
            if($r1)
            {
                foreach ($r1 as $key => $value) 
                {
                    $r1[$key]['money'] = (float)$value['money'];
                    $r1[$key]['s_charge'] = (float)$value['s_charge'];

                    $r1[$key]['sourceName'] = PC_Tools::get_source($value['source']);

                    switch ($value['status']) 
                    {
                        case '1':
                            $r1[$key]['type_name'] = '审核通过';
                            break;
                        case '2':
                            $r1[$key]['type_name'] = '审核拒绝';
                            break;
                        default:
                            $r1[$key]['type_name'] = '待审核';
                            break;
                    }

                    if($value['withdraw_status'] == 2)
                    {
                        $r1[$key]['Cardholder'] = '';
                        $r1[$key]['Bank_name'] = '';
                        $r1[$key]['Bank_card_number'] = '';
                        $r1[$key]['branch'] = '';
                    }
                }
            }
            $list = $r1;
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total));
    }

    // 审核提现
    public function withdrawalExamine()
    {   
        $admin_list = $this->user_list;
        $admin_name = $this->user_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim(Request::param('accessId')));

        $id = intval($this->request->param('id')); // 提现id
        $m = intval($this->request->param('status')); // 参数 1.通过 2.拒绝
        $refuse = trim($this->request->param('refuse')); // 拒绝原因
        $log = new LaiKeLogUtils(); // 日志
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $time = date("Y-m-d H:i:s");
        // 查询记录
        $res = WithdrawModel::where(['store_id'=>$store_id,'id'=>$id])->field('money,add_date,user_id,txsno,s_charge,withdraw_status,bank_id')->select()->toArray();
        $user_id = $res[0]['user_id'];
        $money = $res[0]['money'];
        $txsno = $res[0]['txsno'];
        $s_charge = $res[0]['s_charge'];
        $withdraw_status = $res[0]['withdraw_status']; // 提现类型 1银行卡  2微信余额
        $bank_id = $res[0]['bank_id']; // 银行卡id
        if($withdraw_status == 1)
        {
            $sql_b = "select Bank_name,Bank_card_number from lkt_bank_card where store_id = '$store_id' and id = '$bank_id' ";
            $r_b = Db::query($sql_b);
            if($r_b)
            {
                $withdrawal_method = $r_b[0]['Bank_name'] .' 尾号 ('. substr($r_b[0]['Bank_card_number'], -4).')';
            }
        }
        else
        {
            $withdrawal_method = "微信零钱";
        }

        $total_amount = $money - $s_charge; // 提现金额 - 手续费
        // 查询提现人信息
        $r = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();
        $ymoney = $r[0]['money']; // 原有金额
        $wx_id = $r[0]['wx_id']; // wx_id

        $sql_ = "select id,withdrawal_fees from lkt_record_details where store_id = '$store_id' and s_no = '$txsno' ";
        $r_ = Db::query($sql_);
        if($r_)
        {
            $details_id = $r_[0]['id'];
            $withdrawal_fees = $r_[0]['withdrawal_fees'];
        }
        // 开始事物
        Db::startTrans();
        if ($m == 1)
        {
            // 通过
            $event = $user_id . "提现了" . $money;
            // 在操作列表里添加一条数据
            $sql = new RecordModel();
            $sql->store_id = $store_id;
            $sql->user_id = $user_id;
            $sql->money = $money;
            $sql->oldmoney = $ymoney;
            $sql->event = $event;
            $sql->details_id = $details_id;
            $sql->type = 21;//提现成功
            $sql->save();
            $r = $sql->id;
            if ($r < 1)
            {
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '通过了用户ID：'.$user_id.' 的提现失败',2,1,0,$operator_id);
                $log->log('common/finance.log',__LINE__ . ":【" . $id . "】提现审核通过失败 \r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_SHSB_001,$message);
            }

            // 根据id,修改提现列表中数据的状态
            $sql = WithdrawModel::where(['store_id'=>$store_id,'id'=>$id,'status'=>0])->find();
            $sql->status = 1;
            $sql->examine_date = $time;
            $r = $sql->save();
            if (!$r)
            {
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '通过了用户ID：'.$user_id.' 的提现失败',2,1,0,$operator_id);
                $log->log('common/finance.log',__LINE__ . ":【" . $id . "】提现审核通过失败 \r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_SHSB_001,$message);
            }

            if($withdraw_status == 2)
            {
                if($wx_id == '')
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '通过了用户ID：'.$user_id.' 的提现失败',2,1,0,$operator_id);
                    $log->log('common/finance.log',__LINE__ . ":该用户未微信授权，审核失败");
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_SHSB_001,$message);
                }
                
                $Tools = new Tools( $store_id, 1);
                $res = $Tools->sendMoney($store_id,$total_amount,$wx_id,'提现');
                
                if(!isset($res['batch_id']) || !isset($res['out_batch_no']))
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '通过了用户ID：'.$user_id.' 的提现失败',2,1,0,$operator_id);
                    $log->log('common/finance.log',__LINE__ . "提现审核通过失败：" . json_encode($res));
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_SHSB_001,$message);
                }
            }

            $msg_title = "余额提现申请审批通过！";
            $msg_content = "您的提现申请已通过审核，提现金额将会在1-3个工作日到账。";

            $log->log('common/finance.log',__LINE__ . ":【" . $id . "】提现审核通过成功！ \r\n");
            /**余额提现成功通知*/
            $pusher = new LaikePushTools();
            $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, $admin_name);

            $Jurisdiction->admin_record($store_id, $operator, '通过了用户ID：'.$user_id.' 的提现',2,1,0,$operator_id);
        }
        else
        { // 拒绝
            // 根据微信昵称,修改会员列表里的金额
            $sql = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->find();
            $sql->money = Db::raw('money+ '.$money);
            $r = $sql->save();
            if (!$r)
            {
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '拒绝了用户ID：'.$user_id.' 的提现失败',2,1,0,$operator_id);
                $log->log('common/finance.log',__LINE__ . ":【" . $id . "】提现审核拒绝失败 \r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_SHSB_001,$message);
            }

            // $withdrawal_fees = number_format($s_charge,2) . '(' . number_format($s_charge / $money * 100,2) . '%)';
            $array = array('store_id'=>$store_id,'money'=>$money,'user_money'=>$ymoney,'type'=>11,'money_type'=>1,'money_type_name'=>1,'record_notes'=>'','type_name'=>'','s_no'=>$txsno,'title_name'=>'','activity_code'=>'','mch_name'=>'','withdrawal_fees'=>$withdrawal_fees,'withdrawal_method'=>$withdrawal_method);
            $details_id = PC_Tools::add_Balance_details($array);

            $event = $user_id . "提现" . $money . "被拒绝";
            // 在操作列表里添加一条数据
            $sql = new RecordModel();
            $sql->store_id = $store_id;
            $sql->user_id = $user_id;
            $sql->money = $money;
            $sql->oldmoney = $ymoney;
            $sql->event = $event;
            $sql->details_id = $details_id;
            $sql->type = 22;
            $sql->save();
            $r = $sql->id;
            if ($r < 1)
            {
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '拒绝了用户ID：'.$user_id.' 的提现失败',2,1,0,$operator_id);
                $log->log('common/finance.log',__LINE__ . ":【" . $id . "】提现审核拒绝失败 \r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_SHSB_001,$message);
            }

            $sql = WithdrawModel::where(['store_id'=>$store_id,'id'=>$id,'status'=>0])->find();
            $sql->status = 2;
            $sql->refuse = $refuse;
            $sql->examine_date = $time;
            $r = $sql->save();
            if (!$r)
            {
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '拒绝了用户ID：'.$user_id.' 的提现失败',2,1,0,$operator_id);
                $log->log('common/finance.log',__LINE__ . ":【" . $id . "】提现审核拒绝失败 \r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_SHSB_001,$message);
            }

            $msg_title = "余额提现失败！";
            $msg_content = "您申请的提现被驳回！驳回原因：" . $refuse;

            $log->log('common/finance.log',__LINE__ . ":【" . $id . "】提现审核拒绝成功！ \r\n");

            /**余额提现失敗通知*/
            $pusher = new LaikePushTools();
            $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, $admin_name);

            $Jurisdiction->admin_record($store_id, $operator, '拒绝了用户ID：'.$user_id.' 的提现',2,1,0,$operator_id);
        }
        Db::commit();
        $message = Lang("Success");
        return output(200,$message);
    }

    //提现记录
    public function getWithdrawalRecord()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim(Request::param('accessId')));

        $name = addslashes(trim($this->request->param('userNameAndPhone'))); // 用户名/联系电话
        $withdrawStatus = addslashes(trim($this->request->param('withdrawStatus'))); // 1.微信零钱 2.银行卡
        $status = addslashes(trim($this->request->param('status'))); // 1.通过 2.拒绝
        $wxStatus = addslashes(trim($this->request->param('wxStatus'))); // 1.进行中 2.已完成 3.提现失败
        
        $startdate = $this->request->param('startDate');
        $enddate = $this->request->param('endDate');
        $exportType = $this->request->param('exportType'); // 导出
        $page = $this->request->param('pageNo'); // 页码
        $pagesize = $this->request->param('pageSize'); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10';
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        $condition = " a.store_id = '$store_id' and c.store_id = '$store_id' and a.is_mch = 0 and a.status != 0 ";
        $condition1 = " a.store_id = '$store_id' and c.store_id = '$store_id' and a.is_mch = 0 and a.status != 0 ";

        if ($name)
        {
            $name = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and (a.name like $name or a.mobile like $name) ";
        }
        if ($withdrawStatus)
        {
            $condition .= " and a.withdraw_status = '$withdrawStatus' ";
        }
        if ($status)
        {
            $condition .= " and a.status = '$status' ";
        }
        if ($wxStatus)
        {
            $condition .= " and a.wx_status = '$wxStatus' ";
        }
        if ($startdate)
        {
            $condition .= " and a.add_date >= '$startdate' ";
        }
        if ($enddate)
        {
            $condition .= " and a.add_date <= '$enddate' ";
        }
        $total = 0;
        $sql_num = "select count(a.id) as num from lkt_withdraw as a left join lkt_bank_card as b on a.Bank_id = b.id left join lkt_user as c on a.user_id = c.user_id where $condition";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        if ($total > 0)
        {
            // 页码调整
            if ($start > $total)
            {
                $page = 1;
                $start = 0;
            }
            $sql = "select a.id,a.user_id,a.name as userName,a.add_date,a.money,a.s_charge,a.mobile,a.status,a.refuse,a.txsno as sNo,a.withdraw_status,b.Cardholder,b.Bank_name,b.branch,b.Bank_card_number,c.source from lkt_withdraw as a left join lkt_bank_card as b on a.Bank_id = b.id left join lkt_user as c on a.user_id = c.user_id where $condition order by a.add_date desc limit $start,$pagesize ";
            $r1 = Db::query($sql);
            foreach ($r1 as $key => $value) 
            {
                $r1[$key]['money'] = (float)$value['money'];
                $r1[$key]['s_charge'] = (float)$value['s_charge'];
                switch ($value['source']) 
                {
                    case '1':
                        $r1[$key]['sourceName'] = '微信小程序';
                        break;
                    case '2':
                        $r1[$key]['sourceName'] = 'H5';
                        break;
                    case '3':
                        $r1[$key]['sourceName'] = '支付宝小程序';
                        break;
                    case '4':
                        $r1[$key]['sourceName'] = '字节跳动小程序';
                        break;
                    case '5':
                        $r1[$key]['sourceName'] = '百度小程序';
                        break;
                    case '6':
                        $r1[$key]['sourceName'] = 'pc商城';
                        break;
                    case '7':
                        $r1[$key]['sourceName'] = 'pc店铺';
                        break;
                    case '8':
                        $r1[$key]['sourceName'] = 'pc管理后台';
                        break;
                    default:
                        $r1[$key]['sourceName'] = '其它';
                        break;
                }
                switch ($value['status']) 
                {
                    case '1':
                        $r1[$key]['type_name'] = '审核通过';
                        $r1[$key]['examineName'] = '审核通过';
                        break;
                    case '2':
                        $r1[$key]['type_name'] = '审核拒绝';
                        $r1[$key]['examineName'] = '审核拒绝';
                        break;
                    default:
                        $r1[$key]['type_name'] = '审核中';
                        $r1[$key]['examineName'] = '审核中';
                        break;
                }

                if($value['withdraw_status'] == 2)
                {
                    $r1[$key]['Cardholder'] = '';
                    $r1[$key]['Bank_name'] = '';
                    $r1[$key]['Bank_card_number'] = '';
                    $r1[$key]['branch'] = '';
                }
            }
            
            $list = $r1;
        }

        if($exportType)
        {
            $Jurisdiction->admin_record($store_id, $operator, '导出提现记录列表',4,1,0,$operator_id);
            $titles = array(
                0 => '序号',
                1 => '用户昵称',
                2 => '提交时间',
                3 => '提现金额',
                4 => '提现手续费',
                5 => '银行名称',
                6 => '支行名称',
                7 => '持卡人姓名',
                8 => '卡号',
                9 => '联系电话',
                10 => '状态'
            );
            $exportExcel_list = array();
            if($list != array())
            {
                foreach ($list as $k => $v)
                {
                    $k_ = $k + 1;
                    $exportExcel_list[] = array(
                        $k_,
                        $v['userName'],
                        $v['add_date'],
                        $v['money'],
                        $v['s_charge'],
                        $v['Bank_name'],
                        $v['branch'],
                        $v['Cardholder'],
                        $v['Bank_card_number'],
                        $v['mobile'],
                        $v['examineName']
                    );
                }
            }
            
            ExcelUtils::exportExcel($exportExcel_list, $titles, '商品列表');
            exit;
        }

        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total));
    }

    //钱包参数界面
    public function getWalletInfo()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        // 查询钱包配置
        $r = FinanceConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r)
        {
            $min_cz = (float)$r[0]['min_cz']; // 最小充值金额
            $min_amount = (float)$r[0]['min_amount']; // 最少提现金额
            $max_amount = (float)$r[0]['max_amount']; // 最大提现金额
            $service_charge = $r[0]['service_charge'] * 0.01; // 手续费
            $unit = $r[0]['unit']; // 小程序钱包单位
            $multiple = $r[0]['multiple']; // 提现倍数
            $transfer_multiple = $r[0]['transfer_multiple']; // 转账倍数
            $cz_multiple = $r[0]['cz_multiple']; // 充值倍数
        }
        else
        {
            $min_cz = 50;
            $min_amount = 50;
            $max_amount = 100;
            $service_charge = 0.5;
            $unit = '元';
            $multiple = 0;
            $transfer_multiple = 0;
            $cz_multiple = 100;
        }
        $data = array('min_cz'=>$min_cz,'min_amount'=>$min_amount,'max_amount'=>$max_amount,'service_charge'=>$service_charge,'unit'=>$unit,'multiple'=>$multiple,'transfer_multiple'=>$transfer_multiple,'cz_multiple'=>$cz_multiple);
        $message = Lang("Success");
        return output(200,$message,array('data'=>$data));
    }

    //保存钱包配置
    public function setWalletInfo()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim(Request::param('accessId')));

        $min_cz = addslashes(trim($this->request->param('minMoney'))); // 最小充值金额
        $min_amount = addslashes(trim($this->request->param('minOutMoney'))); // 最小提现金额
        $max_amount = addslashes($this->request->param('maxOutMoney')); // 最大提现金额
        $service_charge = addslashes($this->request->param('serviceMoney')); // 手续费
        $unit = addslashes($this->request->param('unit')); // 单位
        $multiple = trim($this->request->param('multiple'))?trim($this->request->param('multiple')):0; //提现倍数
        $transfer_multiple = trim($this->request->param('transfer_multiple'))?trim($this->request->param('transfer_multiple')):0; //转账倍数
        $cz_multiple = trim($this->request->param('cz_multiple'))?trim($this->request->param('cz_multiple')):0; //充值倍数

        $log = new LaiKeLogUtils();//日志

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        
        if (is_numeric($min_cz) == false)
        {
            $log->log('common/finance.log',__LINE__ . ":钱包配置修改失败：最小充值金额请输入数字！ \r\n");

            $message = Lang("user.11");
            return output(ERROR_CODE_ZXCZJEBNXYDY,$message);
        }
        else
        {
            if ($min_cz <= 0)
            {
                $log->log('common/finance.log',__LINE__ . ":钱包配置修改失败：最小充值金额不能小于等于0！ \r\n");
                $message = Lang("user.12");
                return output(ERROR_CODE_ZXCZJEBNXYDY,$message);
            }
        }

        if (is_numeric($min_amount) == false)
        {
            $log->log('common/finance.log',__LINE__ . ":钱包配置修改失败：最小提现金额请输入数字！ \r\n");
            $message = Lang("user.13");
            return output(ERROR_CODE_ZXTXJEBNXYDY,$message);
        }
        else
        {
            if ($min_amount <= 0)
            {
                $log->log('common/finance.log',__LINE__ . ":钱包配置修改失败：最小提现金额不能小于等于0！ \r\n");
                $message = Lang("user.14");
                return output(ERROR_CODE_ZXTXJEBNXYDY,$message);
            }
        }
        if (is_numeric($max_amount) == false)
        {
            $log->log('common/finance.log',__LINE__ . ":钱包配置修改失败：最大提现金额请输入数字！ \r\n");
            $message = Lang("user.15");
            return output(ERROR_CODE_ZDTXJEBNXYDY,$message);
        }
        else
        {
            if ($max_amount <= 0)
            {
                $log->log('common/finance.log',__LINE__ . ":钱包配置修改失败：最大提现金额不能小于等于0！ \r\n");
                $message = Lang("user.16");
                return output(ERROR_CODE_ZDTXJEBNXYDY,$message);
            }
        }
        if (is_numeric($service_charge))
        {
            if($service_charge < 0 || $service_charge >= 100)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '手续费为小于1的小数！';
                $log->log('common/finance.log',$Log_content);
                $message = Lang("user.17");
                return output(ERROR_CODE_ZXTXJEBNXYDYSXF,$message);
            }
            $service_charge = $service_charge * 100;

            // if (preg_match('/^[0-9]+(.[0-9]{1,2})?$/', $service_charge))
            // {

            // }
            // else
            // {
            //     $Log_content = __METHOD__ . '->' . __LINE__ . '手续费格式错误！';
            //     $log->log('common/finance.log',$Log_content);
            //     $message = Lang("user.18");
            //     return output(ERROR_CODE_ZXTXJEBNXYDYSXF,$message);
            // }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '手续费请填写数字！';
            $log->log('common/finance.log',$Log_content);
            $message = Lang("user.19");
            return output(ERROR_CODE_ZXTXJEBNXYDYSXF,$message);
        }

        if ($min_amount >= $max_amount)
        {
            $log->log('common/finance.log',__LINE__ . ":钱包配置修改失败：最小提现金额不能大于等于最大提现金额！ \r\n");
            $message = Lang("user.21");
            return output(ERROR_CODE_ZXTXJEBNXYDYSXF,$message);
        }
        else
        {
            if ($unit == '')
            {
                $unit = '元';
            }
            $r = FinanceConfigModel::where('store_id',$store_id)->select()->toArray();
            if ($r)
            {
                $sql = FinanceConfigModel::where('store_id',$store_id)->find();
                $sql->min_cz = $min_cz;
                $sql->min_amount = $min_amount;
                $sql->max_amount = $max_amount;
                $sql->service_charge = $service_charge;
                $sql->unit = $unit;
                $sql->modify_date = date('Y-m-d H:i:s');
                $r_1 = $sql->save();
                if (!$r_1)
                {
                    $log->log('common/finance.log',__LINE__ . ":钱包配置修改失败：$sql \r\n");
                    $Jurisdiction->admin_record($store_id, $operator, '修改了用户的提现设置信息失败',2,1,0,$operator_id);
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_CZSB,$message);
                }
                else
                {
                    $log->log('common/finance.log',__LINE__ . ":钱包配置修改成功！ \r\n");
                    $Jurisdiction->admin_record($store_id, $operator, '修改了用户的提现设置信息',2,1,0,$operator_id);
                    $message = Lang("Success");
                    return output(200,$message);
                }
            }
            else
            {
                $sql = new FinanceConfigModel();
                $sql->store_id = $store_id;
                $sql->min_cz = $min_cz;
                $sql->min_amount = $min_amount;
                $sql->max_amount = $max_amount;
                $sql->service_charge = $service_charge;
                $sql->unit = $unit;
                $sql->modify_date = date('Y-m-d H:i:s');
                $sql->save();
                $r_1 =$sql->id;
                if ($r_1 < 1)
                {
                    $log->log('common/finance.log',__LINE__ . ":钱包配置修改失败：$sql \r\n");
                    $Jurisdiction->admin_record($store_id, $operator, '添加了用户的提现设置信息失败',1,1,0,$operator_id);
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_CZSB,$message);
                }
                else
                {
                    $log->log('common/finance.log',__LINE__ . ":钱包配置修改成功！ \r\n");
                    $Jurisdiction->admin_record($store_id, $operator, '添加了用户的提现设置信息',1,1,0,$operator_id);
                    $message = Lang("Success");
                    return output(200,$message);
                }
            }
        }
    }

    // 资金管理
    public function getUserMoneyInfo()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $page = trim($this->request->param('pageNo'));//页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据
        $pagesize = $pagesize ? $pagesize : '10'; // 每页显示多少条数据

        // 接收数据
        $name = addslashes(trim($this->request->param('userName'))); // 用户名称
        $source = addslashes(trim($this->request->param('source'))); // 用户来源
        $starttime = $this->request->param('startDate'); //开始时间
        $group_end_time = $this->request->param('endDate'); //结束时间
        $exportType = $this->request->param('exportType'); // 是否导出
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }
        $condition = " store_id = '$store_id' ";
        if ($name)
        {
            $name = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and (user_id like $name or user_name like $name) ";
        }
        if ($source)
        {
            $condition .= " and source = '$source' ";
        }
        if ($starttime)
        {   
            $condition .= " and Register_data >= '$starttime' ";
        }
        if($group_end_time)
        {
            $condition .= " and Register_data <= '$group_end_time' ";
        }
        $list = array();
        $total = 0;
        $sql_num = "select ifnull(count(id),0) as num from lkt_user where $condition";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        if ($total > 0)
        {
            // 页码调整
            if ($start > $total)
            {
                $page = 1;
                $start = 0;
            }
            $sql = "select * from lkt_user where $condition order by Register_data desc limit $start,$pagesize ";
            $res = Db::query($sql);
            foreach ($res as $key => $value) 
            {
                $res[$key]['money'] = (float)$value['money'];

                $res[$key]['sourceName'] = PC_Tools::get_source($value['source']);
            }
            $list = $res;
            //请求为导出
            if ($exportType)
            {
                $titles = array(
                    0 => '序号',
                    1 => '用户ID',
                    2 => '用户名称',
                    3 => '来源',
                    4 => '余额',
                    5 => '注册时间'
                );
                $exportExcel_list = array();

                if ($list)
                {
                    foreach ($list as $k => $v)
                    {
                        $exportExcel_list[] = array(
                            $k+1,
                            $v['user_id'],
                            $v['user_name'],
                            $v['sourceName'],
                            $v['money'],
                            $v['Register_data']
                        );
                    }
                    ExcelUtils::exportExcel($exportExcel_list, $titles, '资金列表');
                    exit;
                }
                else
                {
                    $message = Lang('No data available');
                    return output(109, $message);
                }
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total));
    }

    // 查看资金详情
    public function getUserMoneyInfo_see()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        // 接收参数
        $user_id = $this->request->param('userid'); // 用户id
        $type = $this->request->param('ostatus'); // 1.入账 2.出账
        $oType = $this->request->param('oType'); // 类型
        $starttime = $this->request->param('startDate'); // 开始时间
        $group_end_time = $this->request->param('endDate'); // 结束时间
        $exportType = $this->request->param('exportType'); // 导出
        
        $page = trim($this->request->param('pageNo'));//页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据
        $pagesize = $pagesize ? $pagesize : '10'; // 每页显示多少条数据
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }
        //申请提现不显示#39954
        $condition = " c.store_id = '$store_id' and c.user_id = '$user_id' and a.type !=0 and a.type !=2 and a.type !=6 and a.type !=7 and a.type !=8 and a.type !=9 and a.type !=10 and a.type !=15 and a.type !=16 and a.type !=17 and a.type !=18 and a.money !=0";
        if ($type && $type != 0)
        {
            $condition .= " and b.money_type = '$type' ";
        }
        if ($oType && $oType != 0)
        {
            $condition .= " and a.type = '$oType' ";
        }
        if ($starttime)
        {
            $condition .= " and a.add_date >= '$starttime' ";
        }
        if ($group_end_time)
        {
            $condition .= " and a.add_date <= '$group_end_time' ";
        }
        $total = 0;
        $sql = "select count(a.id) as num from lkt_record as a left join lkt_record_details as b on a.details_id = b.id left join lkt_user as c on a.user_id = c.user_id where $condition order by a.add_date desc ";
        $r_total = Db::query($sql);
        if($r_total)
        {
            $total = $r_total[0]['num'];
        }
        $list = array();
        $Account_entry = 0;
        $Payment_out = 0;
        // 页码调整
        if ($start > $total)
        {
            $page = 1;
            $start = 0;
        }
        if($total > 0)
        {
            $sql = "select  a.id, c.user_id, a.type, c.user_name, a.money,c.Register_data,a.add_date as registerData,
                        CASE a.type WHEN 1 THEN '用户充值' WHEN 2 THEN '申请提现' WHEN 4 THEN '余额消费' WHEN 5 THEN '退款' WHEN 11 THEN '系统扣除' WHEN 14  THEN '系统充值' WHEN 21 THEN '提现成功' WHEN 22 THEN '提现失败' WHEN 36 THEN '线上消费' WHEN 37 THEN '第三方退款' WHEN 38 THEN '缴纳店铺押金' WHEN 39 THEN '退店铺押金' WHEN 28 THEN '售后退款' WHEN 31 THEN '充值会员' WHEN 32 THEN '购物消费' WHEN 33 THEN '押金退回' WHEN 34 THEN '缴纳押金' WHEN 40 THEN '缴纳店铺保证金' ELSE '-' END as typeName from lkt_record as a left join lkt_record_details as b on a.details_id = b.id left join lkt_user as c on a.user_id = c.user_id where $condition order by a.add_date desc limit $start,$pagesize";
            $res = Db::query($sql);
            foreach ($res as $key => $value) 
            {
                $res[$key]['money'] = (float)$value['money'];
                $res[$key]['addtime'] = $value['registerData'];
                switch ($value['type']) 
                {
                    case '1':
                    case '5':
                    case '14':
                    case '22':
                    case '27':
                    case '37':
                    case '39':
                    case '28':
                    case '33':
                        $res[$key]['status'] = 2;
                        $res[$key]['statusName'] = '入账';
                        break;
                    
                    case '2':
                    case '4':
                    case '11':
                    case '21':
                    case '36':
                    case '38':
                    case '31':
                    case '32':
                    case '34':
                    case '40':
                        $res[$key]['status'] = 1;
                        $res[$key]['statusName'] = '支出';
                        break;
                }
            }
            $list = $res;
            //请求为导出
            if ($exportType)
            {
                $titles = array(
                    0 => '序号',
                    1 => '用户ID',
                    2 => '用户名称',
                    3 => '入账/支出',
                    4 => '金额',
                    5 => '类型'
                );
                $exportExcel_list = array();

                if ($list)
                {
                    foreach ($list as $k => $v)
                    {
                        $exportExcel_list[] = array(
                            $k+1,
                            $v['user_id'],
                            $v['user_name'],
                            $v['statusName'],
                            $v['money'],
                            $v['typeName']
                        );
                    }
                    ExcelUtils::exportExcel($exportExcel_list, $titles, '资金详情');
                    exit;
                }
                else
                {
                    $message = Lang('No data available');
                    return output(109, $message);
                }
            }

            $sql_r = "select ifnull(sum(a.money),0) as money from lkt_record as a left join lkt_record_details as b on a.details_id = b.id left join lkt_user as c on a.user_id = c.user_id where $condition and a.type in (1,5,14,22,27,37,39,28,33) ";
            $r_r = Db::query($sql_r);
            if($r_r)
            {
                $Account_entry = $r_r[0]['money'];
            }

            $sql_c = "select ifnull(sum(a.money),0) as money from lkt_record as a left join lkt_record_details as b on a.details_id = b.id left join lkt_user as c on a.user_id = c.user_id where $condition and a.type in (2,4,11,21,36,38,31,32,34,40) ";
            $r_c = Db::query($sql_c);
            if($r_c)
            {
                $Payment_out = $r_c[0]['money'];
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total,'income'=>$Account_entry,'outcome'=>$Payment_out));
    }

    // 积分列表
    public function getUserIntegralInfo()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $page = trim($this->request->param('pageNo'));//页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据
        $pagesize = $pagesize ? $pagesize : '10'; // 每页显示多少条数据

        $user_name = addslashes(trim($this->request->param('userName'))); // 用户名/联系电话
        $startdate = $this->request->param('startDate');//开始时间
        $enddate = $this->request->param('endDate');//结束时间
        $exportType = $this->request->param('exportType');// 导出
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        $condition = " store_id = '$store_id' ";
        if ($user_name)
        {
            $user_name = Tools::FuzzyQueryConcatenation($user_name);
            $condition .= " and (user_name like $user_name or mobile like $user_name) ";
        }
        if ($startdate)
        {
            $condition .= " and Register_data >= '$startdate' ";
        }
        if ($enddate)
        {
            $condition .= " and Register_data <= '$enddate' ";
        }
        $total = 0;
        $sql_num = "select count(id) as num from lkt_user where $condition";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        if ($total > 0)
        {
            // 页码调整
            if ($start > $total)
            {
                $page = 1;
                $start = 0;
            }
            $sql = "select * from lkt_user where $condition order by Register_data desc limit $start,$pagesize ";
            $res = Db::query($sql);
            if($res)
            {
                foreach ($res as $key => $value) 
                {
                    $res[$key]['sourceName'] = PC_Tools::get_source($value['source']);
                }
            }
            $list = $res;
            //请求为导出
            if ($exportType)
            {
                $titles = array(
                    0 => '序号',
                    1 => '用户ID',
                    2 => '用户名称',
                    3 => '手机号码',
                    4 => '积分',
                    5 => '来源',
                    6 => '注册时间'
                );
                $exportExcel_list = array();

                if ($list)
                {
                    foreach ($list as $k => $v)
                    {
                        $exportExcel_list[] = array(
                            $k+1,
                            $v['user_id'],
                            $v['user_name'],
                            $v['mobile'],
                            $v['score'],
                            $v['sourceName'],
                            $v['Register_data']
                        );
                    }
                    ExcelUtils::exportExcel($exportExcel_list, $titles, '积分列表');
                    exit;
                }
                else
                {
                    $message = Lang('No data available');
                    return output(109, $message);
                }
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total));
    }

    //查看积分详情
    public function getUserIntegralInfo_see()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $page = trim($this->request->param('pageNo'));//页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据
        $pagesize = $pagesize ? $pagesize : '10'; // 每页显示多少条数据

        // 接收参数
        $type = $this->request->param('oType'); // 查询类型
        $user_id = $this->request->param('userid'); // 用户ID
        $startdate = $this->request->param('startDate'); //开始时间
        $enddate = $this->request->param('endDate'); //结束时间
        $exportType = $this->request->param('exportType'); // 导出
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        $condition = " b.store_id = '$store_id' and a.store_id = '$store_id' and b.user_id = '$user_id' ";

        if (strlen($type) > 0)
        {
            $condition .= " and a.type = '$type' ";
        }
        if ($startdate)
        {
            $condition .= " and a.sign_time >= '$startdate' ";
        }
        if ($enddate)
        {
            $condition .= " and a.sign_time <= '$enddate' ";
        }

        // 查询记录总数
        $sql1 = "select count(a.id) as num from lkt_sign_record as a left join lkt_user as b on a.user_id = b.user_id where $condition ";
        $r_total = Db::query($sql1);
        $total = 0;
        if ($r_total)
        {
            $total = $r_total[0]['num'];
        }
        $list = array();
        if($total > 0)
        {
            // 页码调整
            if ($start > $total)
            {
                $page = 1;
                $start = 0;
            }
            $sql = "select a.*,b.user_name,b.mobile,b.source from lkt_sign_record as a left join lkt_user as b on a.user_id = b.user_id where $condition order by a.sign_time desc limit $start,$pagesize";
            $res = Db::query($sql);
            if($res)
            {
                foreach ($res as $key => $value) 
                {
                    switch ($value['type']) 
                    {
                        case '1':
                            $res[$key]['typeName'] = '购物消费';
                            break;
                        case '10':
                            $res[$key]['typeName'] = '积分过期';
                            break;
                        case '5':
                            $res[$key]['typeName'] = '系统扣除';
                            break;
                        case '6':
                            $res[$key]['typeName'] = '系统充值';
                            break;
                        case '8':
                            $res[$key]['typeName'] = '用户购物';
                            break;
                        case '11':
                            $res[$key]['typeName'] = '购买会员';
                            break;
                        case '12':
                            $res[$key]['typeName'] = '会员生日';
                            break;
                        case '13':
                            $res[$key]['typeName'] = '冻结积分';
                            break;
                        default:
                            $res[$key]['typeName'] = '签到积分';
                            break;
                    }
                    
                }
            }
            $list = $res;
            //请求为导出
            if ($exportType)
            {
                $titles = array(
                    0 => '序号',
                    1 => '用户ID',
                    2 => '用户名称',
                    3 => '积分数',
                    4 => '时间',
                    5 => '类型'
                );
                $exportExcel_list = array();

                if ($list)
                {
                    foreach ($list as $k => $v)
                    {
                        $exportExcel_list[] = array(
                            $k+1,
                            $v['user_id'],
                            $v['user_name'],
                            $v['sign_score'],
                            $v['sign_time'],
                            $v['typeName']
                        );
                    }
                    ExcelUtils::exportExcel($exportExcel_list, $titles, '积分详情');
                    exit;
                }
                else
                {
                    $message = Lang('No data available');
                    return output(109, $message);
                }
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total));
    }

    // 获取会员等级（功能弃用，但优惠券这边还是调用了这个接口）
    public function goodsStatus()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $message = Lang("Success");
        return output(200,$message,array());
    }

    //添加默认收货地址
    public function saveAddress()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $user_id = $this->request->param('userId');
        $user_name = $this->request->param('userName');
        $mobile = $this->request->param('mobile');
        $address = $this->request->param('address');
        $place = $this->request->param('place');
        $is_default = (int)$this->request->param('isDefault');

        $place_array = explode('-', $place);

        $province = $place_array['0']; // 省
        $city = $place_array['1']; // 市
        $county = $place_array['2']; //县

        $address_xq = $province . $city . $county . $address; // 带省市县的详细地址
        if (Tools::check_phone($mobile))
        {   
            //事务开启
            Db::startTrans();
            try
            {   
                // 根据微信id,查询会员id
                $r0 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->select()->toArray();
                if ($r0)
                {
                    if ($is_default == 1)
                    {
                        //清空默认地址数据
                        UserAddressModel::update(['is_default'=>0],['store_id'=>$store_id,'uid'=>$user_id]);
                        //添加数据
                        $UserAddressModel           = new UserAddressModel;
                        $UserAddressModel->store_id     = $store_id;
                        $UserAddressModel->name     = $user_name;
                        $UserAddressModel->tel     = $mobile;
                        $UserAddressModel->sheng     = $province;
                        $UserAddressModel->city     = $city;
                        $UserAddressModel->quyu     = $county;
                        $UserAddressModel->address     = $address;
                        $UserAddressModel->address_xq     = $address_xq;
                        $UserAddressModel->uid     = $user_id;
                        $UserAddressModel->is_default     = 1;
                        $UserAddressModel->save();
                    }
                    else
                    {
                        //添加数据
                        $UserAddressModel           = new UserAddressModel;
                        $UserAddressModel->store_id     = $store_id;
                        $UserAddressModel->name     = $user_name;
                        $UserAddressModel->tel     = $mobile;
                        $UserAddressModel->sheng     = $province;
                        $UserAddressModel->city     = $city;
                        $UserAddressModel->quyu     = $county;
                        $UserAddressModel->address     = $address;
                        $UserAddressModel->address_xq     = $address_xq;
                        $UserAddressModel->uid     = $user_id;
                        $UserAddressModel->is_default     = 0;
                        $UserAddressModel->save();
                    }
                }
                else
                {
                    //添加数据
                    $UserAddressModel           = new UserAddressModel;
                    $UserAddressModel->store_id     = $store_id;
                    $UserAddressModel->name     = $user_name;
                    $UserAddressModel->tel     = $mobile;
                    $UserAddressModel->sheng     = $province;
                    $UserAddressModel->city     = $city;
                    $UserAddressModel->quyu     = $county;
                    $UserAddressModel->address     = $address;
                    $UserAddressModel->address_xq     = $address_xq;
                    $UserAddressModel->uid     = $user_id;
                    $UserAddressModel->is_default     = 1;
                    $UserAddressModel->save();

                    $r0 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->select()->toArray();
                }
                Db::commit();
                $message = Lang('Success');
                return output(200, $message, $r0[0]);
            }
            catch (\Exception $e) 
            {
                // 回滚事务
                Db::rollback();
                $Log_content = $e->getMessage();
                $this->Log($Log_content);
                $message = Lang('Busy network');
                return output(ERROR_CODE_TJSB,$message);
            }     
        }
        else
        {
            $message = Lang('Wrong mobile number');
            return output(ERROR_CODE_SJHGSBZQ, $message);
        }
        return;
    }

    // 批量上传
    public function uploadAddUser()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $storeType = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $time = date("Y-m-d H:i:s");
        $Tools = new Tools($store_id, 1);

        $filename = $_FILES['image']['tmp_name'];
        $name = $_FILES['image']['name'];
        $pathinfo = pathinfo($name);
                
        $contentType = $_FILES['image']['type'];
        $fType = explode('/', $contentType);
        
        $type = isset($pathinfo['extension'])?$pathinfo['extension']:$fType[1];
        if (empty ($filename)) 
        {
            $message = Lang('product.91');
            return output(109, $message,null);
        }
        if (!in_array($type,['xls','xlsx'])) 
        {
            $message = Lang('tools.16');
            return output(109, $message,null);
        }

        $handle = fopen($filename,'r');
        $result = $this->input_excel($filename);

        $len_result = count($result);

        if($len_result == 0)
        {
            $message = Lang('product.92');
            return output(109, $message,null);
        }
        unset($result[0]);
        sort($result);

        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1,'recycle'=>0])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id'];

        //默认推荐人
        $res_de = UserModel::where('store_id',$store_id)->field('user_id')->limit(0,1)->select()->toArray();
        if($res_de)
        {
            $ppid = $res_de[0]['user_id'];
        } 

        Db::startTrans();
        $p_data = array();
        $sourceArray = array(
            '1' => '小程序',
            '2' => 'H5移动端',
            '3' => '支付宝小程序',
            '4' => '头条小程序',
            '5' => '百度小程序',
            '6' => 'PC端',
            '11' => 'APP端',
        );
        foreach ($result as $key => $value) 
        {   
            $key1 = $key + 2;
            $user_zhanghao = Tools::generateRandomString(8);
            $mima = $value['A'];
            $mobile= $value['B'];
            $source = $value['C'];
            $cpc = "86";
            if(isset($value['D']))
            {
                $cpc = $value['D'];
            }
            $country_num = "156";
            if(isset($value['E']))
            {
                $country_num = $value['E'];
            }
            // $user_zhanghao = $value['A'];//;mb_convert_encoding($value['A'], 'utf-8','gb2312');
            // $mima = $value['B'];
            // $mobile= $value['C'];
            // $source = $value['D'];

            // $cpc = "86";
            // if(isset($value['E']))
            // {
            //     $cpc = $value['E'];
            // }
            // $country_num = "156";
            // if(isset($value['F']))
            // {
            //     $country_num = $value['F'];
            // }
            
            $sour = array_search($source, $sourceArray);
            if($sour)
            {
                $source = $sour;
            }
            else
            {
                Db::rollback();
                $reason = "第" . $key1 . "行请完善正确的账号来源pc端,小程序,APP端,H5移动端";
                $data = array('file_name'=>$name,'type'=>3,'status'=>0,'reason'=>$reason,'add_time'=>$time);
                $res_log = Db::name('import_log')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                    $this->Log($Log_content);
                }
                $message = Lang("product.93");
                return output(109,$message);
            }
            //密码判断
            if (strlen($mima) < 6)
            {
                Db::rollback();
                $reason = "用户账号" . $user_zhanghao . "密码不能低于六位数！";
                $data = array('file_name'=>$name,'type'=>3,'status'=>0,'reason'=>$reason,'add_time'=>$time);
                $res_log = Db::name('import_log')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                    $this->Log($Log_content);
                }

                $message = Lang("user.0");
                return output(ERROR_CODE_MMBFHGF,$message);
            }
            else
            {
                $mima = Tools::lock_url($mima);
            }
            // //账号唯一性判断
            // $res_1 = UserModel::where('store_id',$store_id)->where('zhanghao|mobile',$user_zhanghao)->field('id')->select()->toArray();
            // if ($res_1)
            // {
            //     Db::rollback();
            //     $reason = "用户账号" . $user_zhanghao . "该账号已存在！";
            //     $data = array('file_name'=>$name,'type'=>3,'status'=>0,'reason'=>$reason,'add_time'=>$time);
            //     $res_log = Db::name('import_log')->insert($data);
            //     if($res_log < 1)
            //     {
            //         $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
            //         $this->Log($Log_content);
            //     }
            //     $message = Lang("user.1");
            //     return output(ERROR_CODE_ZHYCZ,$message);
            // }

            if($mobile == '')
            {
                Db::rollback();
                $reason = "用户账号" . $user_zhanghao . "手机号不能为空";
                $data = array('file_name'=>$name,'type'=>3,'status'=>0,'reason'=>$reason,'add_time'=>$time);
                $res_log = Db::name('import_log')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                    $this->Log($Log_content);
                }
                $message = Lang("user.59");
                return output(ERROR_CODE_SJHGSBZQ,$message);
            }

            // 根据商城ID、区号、国家代码、手机号，查询用户信息
            $sql_m = "select id from lkt_user where store_id = '$store_id' and cpc = '$cpc' and country_num = '$country_num' and mobile = '$mobile' ";
            $r_m = Db::query($sql_m);
            if($r_m)
            { // 存在
                Db::rollback();
                $reason = "用户账号" . $user_zhanghao . "手机号有误。区号：".$cpc.";国家代码：".$country_num.";手机号：".$mobile;
                $data = array('file_name'=>$name,'type'=>3,'status'=>0,'reason'=>$reason,'add_time'=>$time);
                $res_log = Db::name('import_log')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                    $this->Log($Log_content);
                }
                $message = Lang("user.3");
                return output(ERROR_CODE_SJHYBZC,$message);
            }

            //默认头像 和 昵称
            $wx_name = '';
            $wx_headimgurl = '';
            $res_c = ConfigModel::where('store_id',$store_id)->field('wx_headimgurl,wx_name')->select()->toArray();
            if ($res_c)
            {
                $wx_name = $res_c[0]['wx_name'];
                $wx_headimgurl = ServerPath::getimgpath($res_c[0]['wx_headimgurl'], $store_id);
            }
            $p_data[$key]['headimgurl'] = $wx_headimgurl;
            $p_data[$key]['user_name'] = $wx_name;
            $p_data[$key]['zhanghao'] = $user_zhanghao;
            $p_data[$key]['mima'] = $mima;
            $p_data[$key]['cpc'] = $cpc;
            $p_data[$key]['country_num'] = $country_num;
            $p_data[$key]['mobile'] = $mobile;
            $p_data[$key]['source'] = $source;
            $p_data[$key]['store_id'] = $store_id;
            $p_data[$key]['Referee'] = $ppid;
        }
        
        foreach ($p_data as $key => $value)
        {
            $sql = new UserModel();
            $sql->headimgurl = $value['headimgurl'];
            $sql->user_name = $value['user_name'];
            $sql->zhanghao = $value['zhanghao'];
            $sql->cpc = $value['cpc'];
            $sql->country_num = $value['country_num'];
            $sql->mobile = $value['mobile'];
            $sql->mima = $value['mima'];
            $sql->source = $value['source'];
            $sql->store_id = $value['store_id'];
            $sql->Referee = $value['Referee'];
            $sql->is_default_value = 1;
            $sql->is_default_birthday = 1;
            $sql->Register_data = $time;
            $sql->birthday = $time;
            $sql->save();
            $res = $sql->id;
            if ($res < 0)
            {   
                Db::rollback();
                $reason = "用户账号" . $value['zhanghao'] . ":插入用户记录失败，mobile为：" . $value['mobile'];
                $data = array('file_name'=>$name,'type'=>3,'status'=>0,'reason'=>$reason,'add_time'=>$time);
                $res_log = Db::name('import_log')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                    $this->Log($Log_content);
                }
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            $r0 = ConfigModel::where('store_id',$store_id)->select()->toArray();
            if ($r0)
            {
                $user_id1 = $r0[0]['user_id']; //默认用户名ID前缀
            }
            else
            {   
                Db::rollback();
                $reason = "没有配置后台用户的系统设置";
                $data = array('file_name'=>$name,'type'=>3,'status'=>0,'reason'=>$reason,'add_time'=>$time);
                $res_log = Db::name('import_log')->insert($data);
                $message = Lang("user.4");
                return output(ERROR_CODE_QWSXTSZ,$message);
            }

            //更新user_id
            $rr = UserModel::where('store_id',$store_id)->max('id');
            $user_id = $user_id1 . ($rr + 1); //新注册的用户user_id

            $sql_1 = UserModel::where(['store_id'=>$store_id,'id'=>$res])->find();
            $sql_1->user_id = $user_id;
            $res_1 = $sql_1->save();
            if (!$res_1)
            {   
                Db::rollback();
                $reason = "更新用户user_id失败，id为：". $res;
                $data = array('file_name'=>$name,'type'=>3,'status'=>0,'reason'=>$reason,'add_time'=>$time);
                $res_log = Db::name('import_log')->insert($data);
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }

            $event = '会员' . $user_id . '注册成功';
            // 在操作列表里添加一条会员登录信息
            $sql = new RecordModel();
            $sql->store_id = $store_id;
            $sql->user_id = $user_id;
            $sql->event = $event;
            $sql->type = 0;
            $sql->save();
            $r01 = $sql->id;
            if ($r01 < 0)
            {   
                Db::rollback();
                $reason = "添加会员登录消息失败，user_id为：". $event;
                $data = array('file_name'=>$name,'type'=>3,'status'=>0,'reason'=>$reason,'add_time'=>$time);
                $res_log = Db::name('import_log')->insert($data);
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }

        fclose($handle);//关闭指针
        //保存记录
        $sql_log = array('file_name'=>$name,'type'=>3,'status'=>1,'add_time'=>$time);
        $res_log = Db::name('import_log')->insert($sql_log);
        if($res_log < 1)
        {
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加记录失败！';
            $this->Log($Log_content);
            $message = Lang('product.93');
            return output(412, $message);
        }
        Db::commit();
        $message = Lang('product.94');
        return output(200, $message);
    }

    public function input_csv($handle)
    {
        $out = array ();
        $n = 0;
        while ($data = fgetcsv($handle, 10000)) 
        {
            $num = count($data);
            for ($i = 0; $i < $num; $i++) 
            {
                $out[$n][$i] = $data[$i];
            }
            $n++;
        }
        return $out;
    }

    public function input_excel($filename)
    {
        $out = array ();
        $n = 0;
        $reader = IOFactory::createReader('Xlsx'); // 先创建一个Reader对象
        $spreadsheet = $reader->load($filename); // 载入文件到Spreadsheet对象中

        $worksheet = $spreadsheet->getActiveSheet(); // 获取活动工作表

        $highestRow = $worksheet->getHighestRow(); // 获取最大行数
        $highestColumn = $worksheet->getHighestColumn(); // 获取最大列数

        // 从第2行开始遍历每一行
        for ($row = 2; $row <= $highestRow; ++$row) {
            // 从A列开始遍历每一列
            for ($col = 'A'; $col <= $highestColumn; ++$col) {
                $cell = $worksheet->getCell($col . $row); // 获取单元格对象
                $value = $cell->getValue(); // 获取单元格值
                if ($value) 
                {
                    $out[$row][$col] = $value;
                }
            }
        }
        return $out;
    }

    public function assoc_unique($arr,$key = 0) 
    {
        $tmp_arr = array();
        foreach ($arr as $k => $v) 
        {
            if (in_array($v[$key], $tmp_arr)) 
            {//搜索$v[$key]是否在$tmp_arr数组中存在，若存在返回true
                unset($arr[$k]);
            } 
            else 
            {
                $tmp_arr[] = $v[$key];
            }
        }
        sort($arr); //sort函数对数组进行排序
        return $arr;
    }

    // 批量上传记录
    public function uploadRecordList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $storeType = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $key = addslashes($this->request->param('key')); // 文件名称
        $status = addslashes($this->request->param('status')); // 文件状态 导入状态 1成功 2失败
        $startdate = addslashes($this->request->param('startDate')); // 开始时间
        $enddate = addslashes($this->request->param('endDate')); // 结束时间
        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页多少条数据
        
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? $pagesize:'10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " type = 3 ";
        if($key != '')
        {
            $key = Tools::FuzzyQueryConcatenation($key);
            $condition .= " and file_name like $key ";
        }
        if($status != '')
        {
            $condition .= " and status = '$status'";
        }
        if ($startdate != '')
        {
            $condition .= "and add_time >= '$startdate' ";
        }
        if ($enddate != '')
        {
            $condition .= "and add_time <= '$enddate' ";
        }

        $total = 0;
        $list = array();

        $sql_num = "select ifnull(count(id),0) as num from lkt_import_log where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        if($total > 0)
        {
            $sql = "select id,file_name as name,status,reason as text,add_time as add_date,mch_id from lkt_import_log where $condition order by add_time desc limit $start,$pagesize";
            $res = Db::query($sql);
            if($res)
            {
                $list = $res;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 删除批量上传记录
    public function delUploadRecord()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $storeType = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('id')); // 上传记录ID
        
        $res = Db::table('lkt_import_log')->where('id',$id)->delete();
        if($res < 1)
        {
            $message = Lang('操作失败，请重试！');
            return output(400, $message);
        }

        $message = Lang('Success');
        return output(200, $message);
    }

    // 获取会员开通方式
    public function getUserGradeType()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $storeType = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $gradeType = array(
            array('value'=>'1','label'=>'包月'),
            array('value'=>'2','label'=>'包季'),
            array('value'=>'3','label'=>'包年')
        );
        
        $message = Lang('Success');
        return output(200, $message, array('gradeType'=>$gradeType));
    }

    // 获取区号
    public function getItuList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $storeType = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        // 接收信息
        $keyword = trim($this->request->param('keyword'));//区号

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
}
