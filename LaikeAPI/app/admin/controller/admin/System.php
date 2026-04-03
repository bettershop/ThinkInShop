<?php
namespace app\admin\controller\admin;

use app\BaseController;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\PC_Tools;
use PhpOffice\PhpSpreadsheet\IOFactory;

use app\admin\model\AdminModel;
use app\admin\model\ConfigModel;
use app\admin\model\AgreementModel;
use app\admin\model\HotkeywordsModel;
use app\admin\model\ServiceAddressModel;
use app\admin\model\MessageConfigModel;
use app\admin\model\MessageModel;
use app\admin\model\MessageListModel;
use app\admin\model\PrintSetupModel;

/**
 * 功能：系统配置
 * 修改人：DHB
 */
class System  extends BaseController
{

    /**
     * @return \think\response\Json 快速配置
     */
    public function  quickProfile ()
    {
        try
        {
            $storeId = trim(Request::param('storeId'));
            $mailConfig = trim(Request::param('mail_config'));
            $logo = trim(Request::param('logo'));
            //店铺名称
            $name = trim(Request::param('name'));
            $cpc = trim(Request::param('cpc'));
            $tel = trim(Request::param('tel'));
            $sheng = trim(Request::param('sheng'));
            $shi = trim(Request::param('shi'));
            $xian = trim(Request::param('xian'));
            $address = trim(Request::param('address'));
            $code = trim(Request::param('code'));
            $wx_name = trim(Request::param('wx_name'));
            $wx_headimgurl = trim(Request::param('wx_headimgurl'));
            $default_currency = trim(Request::param('default_currency'));
            $default_lang = trim(Request::param('default_lang'));
            $h_Address = trim(Request::param('h_Address'));

            if ($mailConfig !== '')
            {
                $mailConfig = $this->mergeMaskedMailConfig($storeId, $mailConfig);
            }

            //添加自营店
            $JurisdictionAction = new Jurisdiction();

            $admin_name = $this->user_list['name'];

            if (!empty($logo))
            {
                $logo = preg_replace('/.*\//', '', $logo);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 店铺logo不能为空';
                $this->Log($Log_content);
                $message = Lang('店铺logo不能为空！');
                return output(109, $message);
            }

            if($name == '')
            {
                $message = Lang('set.1');
                return output(109,$message);
            }

            if ($tel == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 联系电话不能为空！';
                $this->Log($Log_content);
                $message = Lang('mch.17');
                return output(109,$message);
            }

            if($address == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '详细地址不能为空！';
                $this->Log($Log_content);
                $message = Lang('mch.5');
                return output(109, $message);
            }

            $address_xx = $sheng.$shi.$xian.$address;

            $r0 = AdminModel::where(['store_id'=>$storeId,'type'=>1])->field('name')->select()->toArray();
            $user_id = $r0[0]['name']; // 客户账号
            $time = date("Y-m-d H:i:s");

            //店主手机号为账户+密码6个0 密码可以自己修改 不然每次建好自营店还得去查用户列表
            $zhanghao = $tel;
            $mima = Tools::lock_url('000000');

            // 添加用户信息
            $sql_user = array('store_id'=>$storeId,'user_id'=>$user_id,'user_name'=>$name,'mobile'=>$tel,'zhanghao'=>$zhanghao,'mima'=>$mima,'Register_data'=>$time,'source'=>6,'birthday'=>$time,'lang'=>$default_lang,'preferred_currency'=> $default_currency);
            $user = Db::name('user')->insert($sql_user);
            if($user < 1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加会员信息失败';
                $this->Log($Log_content);
                $message = Lang('shop.18');
                return output(109, $message);
            }

            //店铺分类id
            $cid =Db::name('mch_class')
                ->where('store_id', $storeId)
                ->value('id');

            $sql1 = array('store_id'=>$storeId,'user_id'=>$user_id,'name'=>$name, 'tel'=>$tel,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'logo'=>$logo,
                'review_status'=>1,'add_time'=>$time,'is_open'=>1,'cid' => $cid);

            $res_data = Db::name('mch')->insertGetId($sql1);
            if ($res_data > 0)
            {
                Db::name('admin')->where('store_id', $storeId)->update(['shop_id' => $res_data,'lang'=> $default_lang]);

                $array = array('store_id'=>$storeId,'type0'=>3,'id'=>$res_data,'name'=>$name);
                PC_Tools::jump_path($array);

                $array_address = array('cpc'=>$cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'code'=>$code);
                $address_xq = PC_Tools::address_translation($array_address);
                $sqll = array('store_id'=>$storeId,'name'=>$name,'cpc'=>$cpc,'tel'=>$tel,'code'=>'000000','sheng'=>$sheng,'shi'=>$shi,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'address_xq'=>$address_xq,'uid'=>'admin','type'=>2,'is_default'=>1);
                $r = Db::name('service_address')->insert($sqll);

                $JurisdictionAction->admin_record($storeId, $admin_name, '设置自营店铺成功', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 设置自营店铺成功';
                $this->Log($Log_content);
                //商城配置
                $sql = "UPDATE lkt_config SET mail_config = '$mailConfig',h5_domain='$h_Address',wx_headimgurl='$wx_headimgurl',wx_name='$wx_name',default_lang_code='$default_lang', default_currency = '$default_currency' WHERE store_id = $storeId ";
                // var_dump($sql);
                Db::execute($sql);

                //设置默认币种
                $sql_currency_store = "UPDATE lkt_currency_store set  default_currency = 1,exchange_rate = 1.0000,recycle = 0  where store_id = $storeId and currency_id = $default_currency ";
                //var_dump($sql_currency_store);
                $r_currency_store = Db::execute($sql_currency_store);


                $message = Lang('Success');
                return output(200, $message, "Success");
            }
            else
            {
                $JurisdictionAction->admin_record($storeId, $admin_name, '设置自营店铺失败', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 设置自营店铺失败！参数:'.json_encode($sql1);
                $this->Log($Log_content);
                $message = Lang('Busy network');
                return output(109, $message);
            }


        }
        catch (\Exception $e)
        {
            trace($e->getMessage(), 'error');
            return output(109,  $e->getMessage());
        }
    }

    /**
     * @return \think\response\Json 添加邮箱配置
     */
    public function  addOrUpdateEmailConfig ()
    {
        try
        {
            $storeId = trim(Request::param('storeId'));
            $mailConfig = trim(Request::param('mail_config'));

            if ($mailConfig !== '')
            {
                $mailConfig = $this->mergeMaskedMailConfig($storeId, $mailConfig);
            }

            $sql = "UPDATE lkt_config SET mail_config = ? WHERE store_id = ?";
            Db::execute($sql, [$mailConfig, $storeId]);

            $message = Lang('Success');
            return output(200, $message, "Success");
        }
        catch (\Exception $e)
        {
            trace($e->getMessage(), 'error');
            return output(109,  $e->getMessage());
        }
    }

    /**
     * @return \think\response\Json 获取邮箱配置
     */
    public function getEmailConfig()
    {
        try
        {
            $storeId = addslashes(trim(Request::param('storeId')));

            $sql = "select mail_config,id from lkt_config where store_id = ? ";
            $result = Db::query($sql, [$storeId]);

            $mail_config = '';
            $id = 0;
            if($result)
            {
                $mail_config = $result[0]['mail_config'];
                $id = $result[0]['id'];
            }
            if($mail_config !== '')
            {
                $mail_config = $this->maskMailConfigString($mail_config);
            }
            $data = array('id'=>$id,'mail_config'=>$mail_config);
            $message = Lang('Success');
            return output(200, $message, $data);
            // var_dump($result);die;
        }
        catch (\Exception $e)
        {
            trace($e->getMessage(), 'error');
            // 抛出 HTTP 异常，状态码 500，错误信息
            return output(109,  $e->getMessage());
        }
    }


    /**
     * @return \think\response\Json 检查是否有自营店
     */
    public function checkHaveStoreMchId()
    {
        try
        {
            $storeId = addslashes(trim(Request::param('storeId')));

            $sql = "SELECT b.shop_id 
            FROM lkt_customer AS a 
            LEFT JOIN lkt_admin AS b 
              ON a.admin_id = b.id AND b.store_id = a.id  
            WHERE a.id = ? ";
            $result = Db::query($sql, [$storeId]);
            $flag = $result[0]['shop_id'] != 0 ;
            $message = Lang('Success');
            return output(200, $message, $flag);
            // var_dump($result);die;
        }
        catch (\Exception $e)
        {
            trace($e->getMessage(), 'error');
            // 抛出 HTTP 异常，状态码 500，错误信息
            $message = Lang('No data available');
            return output(109,  $e->getMessage());
        }
    }

    // 获取基础配置（准备弃用）
    public function getSystemIndex()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $list = array();
        // 查询配置信息
        $r = ConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if ($r)
        {
            $r[0]['logo'] = ServerPath::getimgpath($r[0]['logo'], $store_id); // 公司logo
            $r[0]['logo1'] = ServerPath::getimgpath($r[0]['logo1'], $store_id); // 首页logo
            $r[0]['wx_headimgurl'] = ServerPath::getimgpath($r[0]['wx_headimgurl'], $store_id); //默认微信用户头像
            if(empty($r[0]['wx_name'] ))
            {
                $r[0]['wx_name'] = 'user';
            }
            if(empty($r[0]['user_id']))
            {
                $r[0]['user_id'] = 'user';
            }
            $r[0]['h5_domain'] = $r[0]['H5_domain'];
            $r[0]['hide_your_wallet'] = $r[0]['Hide_your_wallet'];
            $r[0]['isAccounts'] = $r[0]['is_accounts'];
            $r[0]['accountsSet'] = $r[0]['accounts_set'];
            $list = $r[0];
            if(isset($list['mail_config']) && $list['mail_config'] !== '')
            {
                $list['mail_config'] = $this->maskMailConfigString($list['mail_config']);
            }
            if(isset($list['express_key']) && $list['express_key'] !== '')
            {
                $list['express_key'] = $this->maskValue($list['express_key'], 4, 4);
            }
            if(isset($list['express_secret']) && $list['express_secret'] !== '')
            {
                $list['express_secret'] = $this->maskValue($list['express_secret'], 4, 4);
            }
            if(isset($list['track_secret']) && $list['track_secret'] !== '')
            {
                $list['track_secret'] = $this->maskValue($list['track_secret'], 4, 4);
            }
            if(isset($list['appsecret']) && $list['appsecret'] !== '')
            {
                $list['appsecret'] = $this->maskValue($list['appsecret'], 4, 4);
            }
            if(isset($list['official_account_appsecret']) && $list['official_account_appsecret'] !== '')
            {
                $list['official_account_appsecret'] = $this->maskValue($list['official_account_appsecret'], 4, 4);
            }
        }
        
        $id = '';
        $is_open = '';
        $num = '';
        $keyword = '';
        $mch_keyword = '';

        $r1 = HotkeywordsModel::where(['store_id'=>$store_id])->select()->toArray();
        if ($r1)
        {
            $id = $r1[0]['id']; // 是否开启
            $is_open = $r1[0]['is_open']; // 是否开启
            $num = $r1[0]['num']; // 关键词上限
            $keyword = $r1[0]['keyword']; // 关键词
            $mch_keyword = $r1[0]['mch_keyword']; // 店铺关键词
        }
        $hotKeywordsConfig = array('id'=>$id,'store_id'=>$store_id,'is_open'=>$is_open,'num'=>$num,'keyword'=>$keyword,'mch_keyword'=>$mch_keyword);
        
        $printSetupConfig = array();
        $str = "id,store_id as storeId,mch_id as mchId,print_name as printName,print_url as printUrl,sheng,shi,xian,address,phone,add_time as addTime";
        $r2 = PrintSetupModel::where(['store_id'=>$store_id,'mch_id'=>0])->field($str)->select()->toArray();
        if ($r2)
        {
            $printSetupConfig = $r2[0];
        }

        $data = array('data'=>$list,'hotKeywordsConfig'=>$hotKeywordsConfig,'printSetupConfig'=>$printSetupConfig);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 设置基础配置（准备弃用）
    public function addSystemConfig()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

    	$is_register = addslashes(Request::param('isRegister')); // 是否需要注册 1.注册 2.免注册
    	$image = addslashes(Request::param('logoUrl')); // 公司logo
    	$wx_headimg = addslashes(Request::param('wxHeader')); // 微信头像
    	$pc_mch_path = addslashes(Request::param('pcMchPath')); // PC店铺地址
    	$H5_domain = addslashes(Request::param('pageDomain')); // H5域名
    	$message_day = addslashes(Request::param('messageSaveDay')); // 消息保留天数
    	$exp_time = addslashes(Request::param('appLoginValid')); // 移动端登录有效期
    	$customer_service = addslashes(Request::param('serverClient')); // 客服
    	// $tencent_key = addslashes(Request::param('tencentKey')); // 腾讯位置服务开发密钥
        $tencent_key = '';
    	$push_Appkey = addslashes(Request::param('pushAppkey')); // 推送Appkey
    	$push_Appid = addslashes(Request::param('pushAppid')); // 推送Appid
    	$push_MasterECRET = addslashes(Request::param('pushMasterEcret')); // 推送秘钥
    	$express_address = addslashes(Request::param('expressAddress')); // 查询接口地址
    	$express_number = addslashes(Request::param('expressNumber')); // 用户编号
    	$express_key = addslashes(Request::param('expressKey')); // 接口调用key
        $express_tel = addslashes(Request::param('expressTel'));//顺丰用查询电话
        $express_secret = addslashes(Request::param('express_secret'));// secret
        $express_tempId = addslashes(Request::param('express_tempId'));// tempId
    	$is_Kicking = addslashes(Request::param('isKicking')); // 是否登录踢人  0.不开启 1.开启
    	$watermark_name = addslashes(Request::param('watermarkName')); // 水印名称
    	$watermark_url = addslashes(Request::param('watermarkUrl')); // 水印网址
        
    	$print_name = addslashes(Request::param('printName')); // 打印名称
    	$print_url = addslashes(Request::param('printUrl')); // 打印网址
    	$sheng = addslashes(Request::param('sheng')); // 省
    	$shi = addslashes(Request::param('shi')); // 市
    	$xian = addslashes(Request::param('xian')); // 县
    	$address = addslashes(Request::param('address')); // 地址
    	$phone = addslashes(Request::param('phone')); // 联系电话
        
        $is_open = addslashes(trim(Request::param('isOpen'))); // 是否开启
        $num = addslashes(trim(Request::param('limitNum'))); // 关键词上限
        $keyword = addslashes(trim(Request::param('keyword'))); // 关键词
        $is_accounts = addslashes(trim(Request::param('isAccounts'))); // 是否分账
        $accounts_set = addslashes(trim(Request::param('accountsSet'))); // 分账账号

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $masked_express_key = $this->isMaskedValue($express_key);
        $masked_express_secret = $this->isMaskedValue($express_secret);
        if ($masked_express_key || $masked_express_secret)
        {
            $existing_express = ConfigModel::where(['store_id'=>$store_id])->field('express_key,express_secret')->select()->toArray();
            if ($existing_express)
            {
                if ($masked_express_key)
                {
                    $express_key = $existing_express[0]['express_key'];
                }
                if ($masked_express_secret)
                {
                    $express_secret = $existing_express[0]['express_secret'];
                }
            }
        }

        $wx_name = 'user';
        $time = date("Y-m-d H:i:s");
        // if ($image == '')
        // {
        //     $this->Log(__LINE__ . ":修改基础配置失败！公司名称不能为空！");
        //     $message = Lang('system.0');
        //     return output(109,$message);
        // }

        if ($wx_headimg == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！微信用户默认名不能为空！");
            $message = Lang('system.1');
            return output(109,$message);
        }

        // if ($image)
        // {
        //     $image = preg_replace('/.*\//', '', $image); // 获取图片名称
        // }

        if ($wx_headimg)
        {
            $wx_headimg = preg_replace('/.*\//', '', $wx_headimg); //获取头像名称
        }
        
        if ($message_day)
        {
        }
        else
        {
            $message_day = 0;
        }

        if($print_name == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！打印名称不能为空！");
            $message = Lang('system.42');
            return output(109,$message);
        }
        if($print_url == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！打印网址不能为空！");
            $message = Lang('system.43');
            return output(109,$message);
        }
        if($sheng == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！省不能为空！");
            $message = Lang('system.34');
            return output(109,$message);
        }
        if($shi == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！市不能为空！");
            $message = Lang('system.35');
            return output(109,$message);
        }
        if($xian == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！县不能为空！");
            $message = Lang('system.36');
            return output(109,$message);
        }
        if($address == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！详细地址不能为空！");
            $message = Lang('system.37');
            return output(109,$message);
        }
        if($phone == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！联系电话不能为空！");
            $message = Lang('system.31');
            return output(109,$message);
        }

        $mch_keyword = '';
        if ($is_open == '')
        { // 如果开启插件为空
            $is_open = 0;
        }

        if($is_open == 1)
        {
            // 正则，防止使用人员用中文逗号
            $keyword = preg_replace("/(\n)|(\s)|(\t)|(\')|(')|(，)/", ',', $keyword);
            $keyword_num = count(explode(',',$keyword));
            if($num < $keyword_num)
            {
                $Log_content = __LINE__ . $operator . ":关键词不能大于上限数量！";
                $this->Log($Log_content);
                $message = Lang('system.41');
                return output(109,$message);
            }
        }
        
        if($is_accounts == 1)
        {
            if($accounts_set == '')
            {
                $this->Log(__LINE__ . ":修改基础配置失败！分账账号不能为空！");
                $message = Lang('system.44');
                return output(109,$message);
            }
        }

        $r_0 = PrintSetupModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('id')->select()->toArray();
        if($r_0)
        {
            $sql_where_0 = array('store_id'=>$store_id);
            $sql_update_0 = array('print_name'=>$print_name,'print_url'=>$print_url,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'phone'=>$phone);
            $r_1 = Db::name('print_setup')->where($sql_where_0)->update($sql_update_0);
            if ($r_1 == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了基础配置信息失败',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":修改基础配置失败: 条件参数：" . json_encode($sql_where_0) . "；修改参数：" . json_encode($sql_update_0));
                $message = Lang('system.8');
                return output(109,$message);
            }
        }
        else
        {
            $sql_insert_0 = array('store_id'=>$store_id,'mch_id'=>0,'print_name'=>$print_name,'print_url'=>$print_url,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'phone'=>$phone,'add_time'=>$time);
            $r_1 = Db::name('print_setup')->insert($sql_insert_0);
            if ($r_1 == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了基础配置信息失败',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":修改基础配置失败: 参数:" . json_encode($sql_insert_0));
                $message = Lang('system.8');
                return output(109,$message);
            }
        }

        $r0 = ConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if ($r0)
        { // 更新
            //是否需要注册   第一次保存后不能更改,请谨慎选择
            if ($r0[0]['is_register'] == 0)
            {
                $sql_update = array('is_register'=>$is_register,'wx_headimgurl'=>$wx_headimg,'H5_domain'=>$H5_domain,'wx_name'=>$wx_name,'message_day'=>$message_day,'customer_service'=>$customer_service,'tencent_key'=>$tencent_key,'exp_time'=>$exp_time,'modify_date'=>$time,'push_Appkey'=>$push_Appkey,'push_Appid'=>$push_Appid,'push_MasterECRET'=>$push_MasterECRET,'express_address'=>$express_address,'express_number'=>$express_number,'express_key'=>$express_key,'express_tel'=>$express_tel,'express_secret'=>$express_secret,'express_tempId'=>$express_tempId,'is_Kicking'=>$is_Kicking,'pc_mch_path'=>$pc_mch_path,'watermark_name'=>$watermark_name,'watermark_url'=>$watermark_url,'is_accounts'=>$is_accounts,'accounts_set'=>$accounts_set);
            }
            else
            {
                $sql_update = array('wx_headimgurl'=>$wx_headimg,'H5_domain'=>$H5_domain,'wx_name'=>$wx_name,'message_day'=>$message_day,'customer_service'=>$customer_service,'tencent_key'=>$tencent_key,'exp_time'=>$exp_time,'modify_date'=>$time,'push_Appkey'=>$push_Appkey,'push_Appid'=>$push_Appid,'push_MasterECRET'=>$push_MasterECRET,'express_address'=>$express_address,'express_number'=>$express_number,'express_key'=>$express_key,'express_tel'=>$express_tel,'express_secret'=>$express_secret,'express_tempId'=>$express_tempId,'is_Kicking'=>$is_Kicking,'pc_mch_path'=>$pc_mch_path,'watermark_name'=>$watermark_name,'watermark_url'=>$watermark_url,'is_accounts'=>$is_accounts,'accounts_set'=>$accounts_set);
            }
            $sql_where = array('store_id'=>$store_id);
            $r = Db::name('config')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了基础配置信息失败',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":修改基础配置失败: 参数:" . json_encode($sql_where));
                $message = Lang('system.8');
                return output(109,$message);
            }
            else
            {
                $res = $this->addSearchConfig($store_id,$is_open,$num,$keyword,$mch_keyword);
                if($res > 0)
                {
                    $Jurisdiction->admin_record($store_id, $operator, '修改了基础配置信息',2,1,0,$operator_id);
                    $this->Log(__LINE__ . $operator . ":修改基础配置信息成功！");
                    $message = Lang('Success');
                    return output(200,$message);
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $operator, '修改了基础配置信息失败',2,1,0,$operator_id);
                    $this->Log(__LINE__ . $operator . ":修改基础配置失败: 参数:" . json_encode($sql_where));
                    $message = Lang('system.8');
                    return output(109,$message);
                }
            }
        }
        else
        {
            // $sql1 = array('store_id'=>$store_id,'is_register'=>$is_register,'logo'=>$image,'wx_headimgurl'=>$wx_headimg,'H5_domain'=>$H5_domain,'wx_name'=>$wx_name,'message_day'=>$message_day,'customer_service'=>$customer_service,'tencent_key'=>$tencent_key,'exp_time'=>$exp_time,'modify_date'=>$time,'push_Appkey'=>$push_Appkey,'push_Appid'=>$push_Appid,'push_MasterECRET'=>$push_MasterECRET,'express_address'=>$express_address,'express_number'=>$express_number,'express_key'=>$express_key,'express_tel'=>$express_tel,'express_secret'=>$express_secret,'express_tempId'=>$express_tempId,'is_Kicking'=>$is_Kicking,'pc_mch_path'=>$pc_mch_path,'watermark_name'=>$watermark_name,'watermark_url'=>$watermark_url,'is_accounts'=>$is_accounts,'accounts_set'=>$accounts_set);
            $sql1 = array('store_id'=>$store_id,'is_register'=>$is_register,'wx_headimgurl'=>$wx_headimg,'H5_domain'=>$H5_domain,'wx_name'=>$wx_name,'message_day'=>$message_day,'customer_service'=>$customer_service,'tencent_key'=>$tencent_key,'exp_time'=>$exp_time,'modify_date'=>$time,'push_Appkey'=>$push_Appkey,'push_Appid'=>$push_Appid,'push_MasterECRET'=>$push_MasterECRET,'express_address'=>$express_address,'express_number'=>$express_number,'express_key'=>$express_key,'express_tel'=>$express_tel,'express_secret'=>$express_secret,'express_tempId'=>$express_tempId,'is_Kicking'=>$is_Kicking,'pc_mch_path'=>$pc_mch_path,'watermark_name'=>$watermark_name,'watermark_url'=>$watermark_url,'is_accounts'=>$is_accounts,'accounts_set'=>$accounts_set);
            $r1 = Db::name('config')->insert($sql1);
            if ($r1 > 0)
            {
                $res = $this->addSearchConfig($store_id,$is_open,$num,$keyword,$mch_keyword);
                if($res > 0)
                {
                    $Jurisdiction->admin_record($store_id, $operator, '添加基础配置信息',1,1,0,$operator_id);
                    $this->Log(__LINE__ . $operator . ":添加基础配置成功！");
                    $message = Lang('Success');
                    return output(200,$message);
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $operator, '添加基础配置信息失败',1,1,0,$operator_id);
                    $this->Log(__LINE__ . $operator . ":添加基础配置失败！参数:" . json_encode($sql1));
                    $message = Lang('system.9');
                    return output(109,$message);
                }
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加基础配置信息失败',1,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":添加基础配置失败！参数:" . json_encode($sql1));
                $message = Lang('system.9');
                return output(109,$message);
            }
        }
    }

    // 编辑关于我们（准备弃用）
    public function updateAboutMe()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

    	$aboutus = Request::param('auboutMe'); // 关于我们

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        // 更新
        $sql_update = array('aboutus'=>$aboutus,'modify_date'=>$time);
        $sql_where = array('store_id'=>$store_id);
        $r = Db::name('config')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了关于我们的内容信息失败',2,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":修改关于我们失败！参数:" . json_encode($sql_where));
            $message = Lang('system.8');
            return output(109,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了关于我们的内容信息',2,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":修改关于我们成功！");
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 获取协议配置
    public function getAgreementIndex()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

    	$id = addslashes(trim(Request::param('id'))); // 协议ID

        $list = array('store_id'=>$store_id);
        if($id)
        {
            $list['id'] = $id;
        }
        $r = AgreementModel::where($list)->select()->toArray();

        $data = array('list'=>$r);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加/编辑协议
    public function addAgreement()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

    	$id = addslashes(trim(Request::param('id'))); // 协议ID
        $name = addslashes(trim(Request::param('title'))); // 标题
        $type = addslashes(trim(Request::param('type'))); // 类型
    	$content = Request::param('content'); // 内容

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        if($id != 0 && $id != '')
        {
            $r0 = AgreementModel::where(['store_id'=>$store_id,'type'=>$type])->where('id','<>',$id)->select()->toArray();
            if ($r0)
            {
                $this->Log(__LINE__ . $operator . ":添加协议失败！该类协议已存在！" );
                $message = Lang('system.10');
                return output(109,$message);
            }
            if ($content == '')
            {
                $this->Log(__LINE__ . $operator . ":添加协议失败！协议内容不能为空！" );
                $message = Lang('system.11');
                return output(109,$message);
            }
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('name'=>$name,'type'=>$type,'content'=>$content,'modify_date'=>$time);
            $r = Db::name('agreement')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了ID为 '.$id.' 的协议信息失败',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":修改协议失败！参数：".json_encode($sql_where) );
                $message = Lang('system.8');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了ID为 '.$id.' 的协议信息',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":修改协议成功！");
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $r0 = AgreementModel::where(['store_id'=>$store_id,'type'=>$type])->select()->toArray();
            if ($r0)
            {
                $this->Log(__LINE__ . $operator . ":添加协议失败！该类协议已存在！" );
                $message = Lang('system.10');
                return output(109,$message);
            }
            if ($content == '')
            {
                $this->Log(__LINE__ . $operator . ":添加协议失败！协议内容不能为空！" );
                $message = Lang('system.11');
                return output(109,$message);
            }
    
            $sql = array('store_id'=>$store_id,'name'=>$name,'type'=>$type,'content'=>$content,'modify_date'=>$time);
            $r = Db::name('agreement')->insert($sql);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了 '.$name.' 失败',1,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":添加协议失败！参数：".json_encode($sql) );
                $message = Lang('system.9');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了 '.$name,1,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":添加协议成功！");
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // 删除协议
    public function delAgreement()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $id = addslashes(trim(Request::param('id'))); // 协议ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r = Db::table('lkt_agreement')->where(['store_id'=>$store_id,'id'=>$id])->delete();
        if ($r > 0)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了ID为 '.$id.' 的协议',3,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":删除协议成功！");
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了ID为 '.$id.' 的协议失败',3,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":删除协议失败！参数：".$id );
            $message = Lang('system.12');
            return output(109,$message);
        }
    }

    // 设置搜索配置（准备弃用）
    public function addSearchConfig($store_id,$is_open,$num,$keyword,$mch_keyword)
    {
        $r0 = HotkeywordsModel::where(['store_id'=>$store_id])->field('id')->select()->toArray();
        if ($r0)
        {
            $sql_where = array('store_id'=>$store_id);
            $sql_update = array('is_open'=>$is_open,'num'=>$num,'keyword'=>$keyword,'mch_keyword'=>$mch_keyword);
            $r = Db::name('hotkeywords')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            $sql = array('store_id'=>$store_id,'is_open'=>$is_open,'num'=>$num,'keyword'=>$keyword,'mch_keyword'=>$mch_keyword);
            $r = Db::name('hotkeywords')->insert($sql);
            if ($r > 0)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }

    // 获取售后地址
    public function getAddressInfo()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));
        
        $id = addslashes(trim(Request::param('id'))); // 地址ID
        $type = addslashes(trim(Request::param('type'))); // 类型（1发货地址 2售后地址）
        $name = addslashes(trim(Request::param('name'))); // 联系人
        $page = addslashes(trim(Request::param('pageNo')));
        $pagesize = addslashes(trim(Request::param('pageSize')));
        $pagesize = $pagesize ? $pagesize : 10;
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " store_id = '$store_id' and uid = 'admin' and type = '$type' ";
        if($id != '' && $id != 0)
        {
            $condition .= "  and id = '$id' ";
        }
        if(!empty($name))
        {
            $name = Tools::FuzzyQueryConcatenation($name);
            $condition .= "  and (name like $name or tel like $name)";
        }
        // 如果没有发货地址没有设置默认  默认设置第一个为默认
        $r = ServiceAddressModel::where(['store_id'=>$store_id,'uid'=>'admin','is_default'=>1,'type'=>$type])->field('id')->select()->toArray();
        if(empty($r))
        {
            $sql_default_update_where = array('store_id'=>$store_id,'uid'=>'admin','type'=>$type);
            $sql_default_update_update = array('is_default'=>1);
            $r_default_update = Db::name('service_address')->where($sql_default_update_where)->limit(1)->order('id','asc')->update($sql_default_update_update);
        }

        $total = 0;
        $sql0 = "select id from lkt_service_address where $condition";
        $r0 = Db::query($sql0);
        if($r0)
        {
           $total = count($r0); 
        }

        $list = array();
        $sql1 = "select id,store_id,name,tel,sheng,shi,sheng as province,shi as city,xian,address,address_xq,code,uid,type,is_default,cpc,country_num from lkt_service_address where $condition order by is_default desc,id limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $list = $r1;
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加/编辑售后地址
    public function addAddressInfo()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $id = addslashes(trim(Request::param('id'))); // 售后地址ID
        $country_num = addslashes(trim(Request::param('country_num'))); // 国家代码
        $type = addslashes(trim(Request::param('type'))); // 类型（1发货地址 2售后地址）
        $name = addslashes(trim(Request::param('name'))); // 联系人
        $cpc = addslashes(trim(Request::param('cpc'))); // 区号
        $tel = addslashes(trim(Request::param('tel'))); // 联系电话
        $sheng = addslashes(trim(Request::param('shen'))); // 省
        $shi = addslashes(trim(Request::param('shi'))); // 市
        
        if(!is_china_calling_code($cpc)){
            $sheng = addslashes(trim(Request::param('province'))); // 省
            $shi = addslashes(trim(Request::param('city'))); // 市
        }
        
        // var_dump($sheng,$shi);
        $xian = addslashes(trim(Request::param('xian'))); // 县
        $address = addslashes(trim(Request::param('address'))); // 详细地址
        $code = addslashes(trim(Request::param('code'))); // 邮政编码
        $is_default = addslashes(trim(Request::param('isDefault'))); // 是否为默认收货地址 1.是  0.不是

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        $array_address = array('cpc'=>$cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'code'=>$code);
        $address_xq = PC_Tools::address_translation($array_address);

        if ($name == '')
        {
            $message = Lang('system.30');
            return output(109,$message);
        }
        if ($tel == '')
        {
            $message = Lang('system.31');
            return output(109,$message);
        }
        
        if (strlen($tel) > 11 || !is_numeric($tel))
        {
            $message = Lang('system.32');
            return output(109,$message);
        }
        else
        {   
            if($id != '' && $id != 0)
            {
                $r0 = ServiceAddressModel::where(['store_id'=>$store_id,'cpc'=>$cpc,'tel'=>$tel,'uid'=>'admin','type'=>$type])->where('id','<>',$id)->select()->toArray();
            }
            else
            {
                $r0 = ServiceAddressModel::where(['store_id'=>$store_id,'cpc'=>$cpc,'tel'=>$tel,'uid'=>'admin','type'=>$type])->select()->toArray();
            }
            if ($r0)
            {
                $message = Lang('system.33');
                return output(109,$message);
            }
        }

        if ($sheng == '')
        {
            $message = Lang('system.34');
            return output(109,$message);
        }
        
        if ($shi == '')
        {
            $message = Lang('system.35');
            return output(109,$message);
        }
        if ($xian == '')
        {
            $message = Lang('system.36');
            return output(109,$message);
        }
        if ($address == '')
        {
            $message = Lang('system.37');
            return output(109,$message);
        }
        if ($code == '')
        {
            $message = Lang('system.38');
            return output(109,$message);
        }
        else
        {
            if (strlen($code) != 6)
            {
                $message = Lang('system.39');
                return output(109,$message);
            }
        }

        if (intval($is_default) == 1)
        {
            $sql_service_address_where = array('store_id'=>$store_id,'type'=>$type);
            $sql_service_address_update = array('is_default'=>0);
            Db::name('service_address')->where($sql_service_address_where)->update($sql_service_address_update);
        }

        if ($id == 'null' || $id == 'undefined')
        {
            $id = '';
        }

        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('name'=>$name,'cpc'=>$cpc,'tel'=>$tel,'code'=>$code,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'address_xq'=>$address_xq,'is_default'=>$is_default,'country_num'=>$country_num);
            $r = Db::name('service_address')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了ID为 '.$id.' 的售后地址的信息失败',2,1,0,$operator_id);
                $message = Lang('system.8');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了ID为 '.$id.' 的售后地址的信息',2,1,0,$operator_id);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $sqll = array('store_id'=>$store_id,'name'=>$name,'cpc'=>$cpc,'tel'=>$tel,'code'=>$code,'sheng'=>$sheng,'shi'=>$shi,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'address_xq'=>$address_xq,'uid'=>'admin','type'=>$type,'is_default'=>$is_default,'country_num'=>$country_num);
            $r = Db::name('service_address')->insert($sqll);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了 '.$address.' 的售后地址失败',1,1,0,$operator_id);
                $message = Lang('system.9');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了 '.$address.' 的售后地址',1,1,0,$operator_id);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // 设置默认售后地址
    public function setDefaultAddress()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $id = addslashes(trim(Request::param('id'))); // 售后地址ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");
        
        $r0 = ServiceAddressModel::where(['id'=>$id])->field('type')->select()->toArray();
        if($r0)
        {
            $type = $r0[0]['type'];
            
            // 接收信息
            $sql_where = array('store_id'=>$store_id,'uid'=>'admin','type'=>$type);
            $sql_update = array('is_default'=>0);
            Db::name('service_address')->where($sql_where)->update($sql_update);
    
            $sql_where = array('store_id'=>$store_id,'uid'=>'admin','id'=>$id);
            $sql_update = array('is_default'=>1);
            $r = Db::name('service_address')->where($sql_where)->update($sql_update);
    
            $Jurisdiction->admin_record($store_id, $operator, '将售后地址ID：'.$id.'，设为了默认',2,1,0,$operator_id);
    
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $message = Lang('Parameter error');
            return output(200,$message);
        }
    }

    // 删除售后地址
    public function delAddress()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $id = addslashes(trim(Request::param('id'))); // 售后地址ID
        $type = addslashes(trim(Request::param('type'))); // 类型（1发货地址 2售后地址）

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        // 根据id，删除地址信息
        $res = Db::table('lkt_service_address')->where(['store_id'=>$store_id,'id'=>$id])->delete();
        if ($res > 0)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除售后地址ID为 '.$id,3,1,0,$operator_id);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除售后地址ID为 '.$id.' 失败',3,1,0,$operator_id);
            $message = Lang('Abnormal business');
            return output(109,$message);
        }
    }

    // 获取短信核心配置
    public function getTemplateConfigInfo()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $list = array();
        $r0 = MessageConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if($r0)
        {
            $list = $r0[0];
            if (isset($list['accessKeyId']) && $list['accessKeyId'] !== '')
            {
                $list['accessKeyId'] = $this->maskValue($list['accessKeyId'], 4, 4);
            }
            if (isset($list['accessKeySecret']) && $list['accessKeySecret'] !== '')
            {
                $list['accessKeySecret'] = $this->maskValue($list['accessKeySecret'], 4, 4);
            }
        }
        $data = array('data' => $list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 设置短信核心配置
    public function addTemplateConfig()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $accessKeyId = addslashes(Request::param('key')); // accessKeyId
        $accessKeySecret = addslashes(Request::param('secret')); // accessKeyId

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        $masked_access_key = $this->isMaskedValue($accessKeyId);
        $masked_access_secret = $this->isMaskedValue($accessKeySecret);
        if ($masked_access_key || $masked_access_secret)
        {
            $existing_sms = MessageConfigModel::where(['store_id'=>$store_id])->field('accessKeyId,accessKeySecret')->select()->toArray();
            if ($existing_sms)
            {
                if ($masked_access_key)
                {
                    $accessKeyId = $existing_sms[0]['accessKeyId'];
                }
                if ($masked_access_secret)
                {
                    $accessKeySecret = $existing_sms[0]['accessKeySecret'];
                }
            }
        }

        if ($accessKeyId == '')
        {
            $this->Log(__LINE__ . ":修改短信配置失败：accessKeyId不能为空！");
            $message = Lang('system.13');
            return output(109,$message);
        }

        if ($accessKeySecret == '')
        {
            $this->Log(__LINE__ . ":修改短信配置失败：accessKeySecret不能为空！");
            $message = Lang('system.14');
            return output(109,$message);
        }

        // 根据商城id, 查询短信配置
        $r = MessageConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if ($r)
        { // 存在 修改
            $sql_where = array('store_id'=>$store_id);
            $sql_update = array('accessKeyId'=>$accessKeyId,'accessKeySecret'=>$accessKeySecret,'add_time'=>$time);
            $rr = Db::name('message_config')->where($sql_where)->update($sql_update);
            if ($rr > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了短信配置的核心设置信息 ',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":修改短信配置成功！");
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了短信配置的核心设置信息失败 ',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":修改短信配置失败！参数:".json_encode($sql_where));
                $message = Lang('system.8');
                return output(109,$message);
            }
        }
        else
        { // 不存在 插入
            $sql = array('store_id'=>$store_id,'accessKeyId'=>$accessKeyId,'accessKeySecret'=>$accessKeySecret,'add_time'=>$time);
            $rr = Db::name('message_config')->insert($sql);
            if ($rr == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了短信配置的核心设置信息失败 ',1,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":添加短信配置失败！参数:".json_encode($sql));
                $message = Lang('system.9');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了短信配置的核心设置信息 ',1,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":添加短信配置成功！");
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // 短信模板列表
    public function getSmsTemplateInfo()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $id = addslashes(Request::param('id')); // 短信模板ID
        $international = addslashes(Request::param('international')); // 是否是国际化 0.国内 1.国际
        $page = addslashes(Request::param('pageNo')); // 页码
        $pagesize = addslashes(Request::param('pageSize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10';// 每页显示多少条数据

        $total = 0;
        if($id != 0 && $id != '')
        {
            $r = MessageModel::where(['store_id'=>$store_id,'international'=>$international,'id'=>$id])->field('count(id) as num')->select()->toArray();

            $rr = MessageModel::where(['store_id'=>$store_id,'international'=>$international,'id'=>$id])->page((int)$page,(int)$pagesize)->order('add_time','desc')->select()->toArray();
        }
        else
        {
            $r = MessageModel::where(['store_id'=>$store_id,'international'=>$international])->field('count(id) as num')->select()->toArray();

            $rr = MessageModel::where(['store_id'=>$store_id,'international'=>$international])->page((int)$page,(int)$pagesize)->order('add_time','desc')->select()->toArray();
        }
        if($r)
        {
            $total = $r[0]['num'];
        }

        $data = array('list' => $rr, 'total' => $total);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 获取短信类型、获取短信类别
    public function getSmsTypeList()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $superName = addslashes(trim(Request::param('superName')));

        if ($superName != '')
        {
            $list = Tools::get_message_data_dictionary('短信模板类别', $superName, '');
        }
        else
        {
            $list = Tools::data_dictionary1( '短信模板类型', '');
        }

        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加、编辑短信模板
    public function addMessage()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $id = addslashes(Request::param('id')); // 短信模板ID
        $SignName = addslashes(Request::param('SignName')); // 短信签名
        $name = addslashes(Request::param('name')); // 短信模板名称
        $type = addslashes(Request::param('type')); // 类型
        $type1 = addslashes(Request::param('type1')); // 类别
        $TemplateCode = addslashes(Request::param('code')); // 短信模板Code
        $PhoneNumbers = addslashes(Request::param('phone')); // 短信接收号码
        $content = addslashes(Request::param('content')); // 发送内容
        $international = addslashes(Request::param('international')); // 是否是国际化 0.国内 1.国际

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        if ($SignName == '')
        {
            $this->Log(__LINE__ . ":修改短信配置失败：短信签名不能为空！");
            $message = Lang('system.15');
            return output(109,$message);
        }
        if ($name == '')
        {
            $this->Log(__LINE__ . ":添加短信模版失败：短信模板名称不能为空！");
            $message = Lang('system.16');
            return output(109,$message);
        }
        else
        {
            if($id != 0 && $id != '')
            {
                $r = MessageModel::where(['store_id'=>$store_id,'name'=>$name,'international'=>$international])->where('id','<>',$id)->select()->toArray();
            }
            else
            {
                $r = MessageModel::where(['store_id'=>$store_id,'name'=>$name,'international'=>$international])->select()->toArray();
            }
            if ($r)
            {
                $this->Log(__LINE__ . ":添加短信模版失败：短信模板名称" . $name . "已存在！");
                $message = Lang('system.17');
                return output(109,$message);
            }
        }
        if ($TemplateCode == '')
        {
            $this->Log(__LINE__ . ":添加短信模版失败：短信模板Code不能为空！");
            $message = Lang('system.18');
            return output(109,$message);
        }
        else
        {
            if($id != 0 && $id != '')
            {
                $r = MessageModel::where(['store_id'=>$store_id,'TemplateCode'=>$TemplateCode])->where('id','<>',$id)->select()->toArray();
            }
            else
            {
                $r = MessageModel::where(['store_id'=>$store_id,'TemplateCode'=>$TemplateCode])->select()->toArray();
            }
            if ($r)
            {
                $this->Log(__LINE__ . ":添加短信模版失败：短信模板Code" . $TemplateCode . "已存在！");
                $message = Lang('system.19');
                return output(109,$message);
            }
        }
        // if ($PhoneNumbers == '')
        // {
        //     $this->Log(__LINE__ . ":添加短信模版失败：短信接收号码不能为空！");
        //     $message = Lang('system.20');
        //     return output(109,$message);
        // }
        if ($content == '')
        {
            $this->Log(__LINE__ . ":添加短信模版失败：发送内容不能为空！");
            $message = Lang('system.21');
            return output(109,$message);
        }
        if ($type == 1)
        {
            preg_match_all("/(?<={)[^}]+/", $content, $result);
            if($result[0] == array())
            {
                $TemplateParam = '';
            }
            else
            {
                foreach ($result[0] as $k => $v)
                {
                    $TemplateParam[$v] = $v;
                }
            }
        }
        else
        {
            $code = rand(100000, 999999);
            $TemplateParam = array('code' => $code);
        }
        if ($type1 == '')
        {
            $this->Log(__LINE__ . ":添加短信模版失败：请选择类型！");
            $message = Lang('system.22');
            return output(109,$message);
        }
        // $Tools = new Tools($store_id, 1);
        // $Tools_arr = $Tools->message($store_id,$SignName, $PhoneNumbers, $TemplateCode, $TemplateParam);

        // if ($Tools_arr != '')
        // {
        //     if ($Tools_arr->Code == 'OK')
        //     {
                if($id != 0 && $id != '')
                {
                    $sql_update = array('SignName'=>$SignName,'name'=>$name,'type'=>$type,'type1'=>$type1,'TemplateCode'=>$TemplateCode,'content'=>$content,'add_time'=>$time,'international'=>$international);
                    $sql_where = array('store_id'=>$store_id,'id'=>$id);
                    $rr = Db::name('message')->where($sql_where)->update($sql_update);
                    if ($rr == -1)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '修改了短信模板ID：'.$id.' 的信息失败',2,1,0,$operator_id);
                        $this->Log(__LINE__ . $operator . ":修改短信模版失败：参数:".json_encode($sql_where));
                        $message = Lang('system.8');
                        return output(109,$message);
                    }
                    else
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '修改了短信模板ID：'.$id.' 的信息',2,1,0,$operator_id);
                        $this->Log(__LINE__ . $operator . ":修改短信模版成功！");
                        $message = Lang('Success');
                        return output(200,$message);
                    }
                }
                else
                {
                    $sql = array('store_id'=>$store_id,'SignName'=>$SignName,'name'=>$name,'type'=>$type,'type1'=>$type1,'TemplateCode'=>$TemplateCode,'content'=>$content,'add_time'=>$time,'international'=>$international);
                    $rr = Db::name('message')->insert($sql);
                    if ($rr == -1)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '添加了短信模板'.$name.'失败',1,1,0,$operator_id);
                        $this->Log(__LINE__ . $operator . ":添加短信模版失败：参数:".json_encode($sql));
                        $message = Lang('system.9');
                        return output(109,$message);
                    }
                    else
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '添加了短信模板'.$name,1,1,0,$operator_id);
                        $this->Log(__LINE__ . $operator . ":添加短信模版成功！");
                        $message = Lang('Success');
                        return output(200,$message);
                    }
                }
        //     }
        //     else
        //     {
        //         $Jurisdiction->admin_record($store_id, $operator, '短信模板有误：'.$Tools_arr->Code,1,1,0,$operator_id);
        //         $this->Log(__LINE__ . $operator . ":短信模版失败：" . $Tools_arr->Code);
        //         $message = Lang('Parameter error');
        //         return output(109,$message,array('verification'=>$Tools_arr->Code));
        //     }
        // }
        // else
        // {
        //     $this->Log(__LINE__ . ":添加短信模版失败：请先配置核心设置");
        //     $message = Lang('system.23');
        //     return output(109,$message);
        // }
    }

    // 删除短信模板
    public function delMessage()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $id = addslashes(trim(Request::param('id'))); // 短信模板ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r = MessageListModel::where(['store_id'=>$store_id,'Template_id'=>$id])->field('id')->select()->toArray();
        if ($r)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了短信模板ID：'.$id.'失败',3,1,0,$operator_id);
            $this->Log(__LINE__ . ":删除短信模版失败：该模板正在使用！");
            $message = Lang('system.24');
            return output(109,$message);
        }
        else
        {
            Db::table('lkt_message')->where('id',$id)->delete();

            $Jurisdiction->admin_record($store_id, $operator, '删除了短信模板ID：'.$id,3,1,0,$operator_id);
            $this->Log(__LINE__ . ":删除短信模板ID为 ' . $id . '成功");
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 获取短信模板
    public function getSmsTemplateList()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $type = addslashes(Request::param('type')); // 类型
        $type1 = addslashes(Request::param('id')); // 类别

        $list = array();
        if ($type != '')
        {
            if ($type1 != '')
            {
                $r = MessageModel::where(['store_id'=>$store_id,'type'=>$type,'type1'=>$type1])->select()->toArray();
                if ($r)
                {
                    foreach ($r as $k => $v)
                    {
                        $list[] = $v;
                    }
                }
                $data = array('templateList' => $list);
                $message = Lang('Success');
                return output(200,$message,$data);
            }
            else
            {
                $r = MessageModel::where(['store_id'=>$store_id,'type'=>$type])->select()->toArray();
                if ($r)
                {
                    foreach ($r as $k => $v)
                    {
                        $list[] = $v;
                    }
                }

                $data = array('templateList' => $list);
                $message = Lang('Success');
                return output(200,$message,$data);
            }
        }
    }

    // 短信列表
    public function getSmsInfo()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $page = addslashes(Request::param('pageNo')); // 页码
        $pagesize = addslashes(Request::param('pageSize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10';// 每页显示多少条数据
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $total =  0;
        $sql = "select count(*) as num from lkt_message_list where store_id = '$store_id'";
        $r = Db::query($sql);
        if($r)
        {
            $total = $r[0]['num'];
        }
        
        $list = array();
        $sql = "select a.*,b.name,b.content as content1 from lkt_message_list as a left join lkt_message as b on a.Template_id = b.id where a.store_id = '$store_id' order by a.add_time desc limit $start,$pagesize";
        $rr = Db::query($sql);
        if ($rr)
        {
            foreach ($rr as $k => $v)
            {
                $content1 = unserialize($v['content']);
                $content2 = $v['content1'];
                if ($v['type'] == 0)
                {
                    $v['content'] = $content2;
                }
                else if ($v['type'] == 1)
                {
                    foreach ($content1 as $ke => $va)
                    {
                        $content2 = str_replace('${' . $ke . '}', $va, $content2);
                    }
                    $v['content'] = $content2;
                }
                $v['NAME'] = $v['name'];
                $list[] = $v;
            }
        }
        $data = array('list' => $list, 'total' => $total);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加、编辑短信
    public function addMessageList()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $id = addslashes(Request::param('id')); // 短信ID
        $type = addslashes(Request::param('type')); // 类型
        $type1 = addslashes(Request::param('category')); // 类别
        $tid = addslashes(Request::param('smsTemplateId')); // 模板id
        $content = addslashes(Request::param('templateStr')); // 发送内容
        $content1 = addslashes(Request::param('templateStr1')); // 发送内容

        
        $Jurisdiction = new Jurisdiction();
        $admin_id = cache($access_id.'admin_id');
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        if ($tid == 0)
        {
            $this->Log(__LINE__ . ":请选择短信模板！");
            $message = Lang('system.25');
            return output(109,$message);
        }

        if ($type == '0')
        {
            $code = rand(100000, 999999);
            $TemplateParam = array('code' => $code);
        }
        else if ($type == '1')
        {
            if (in_array('', $content))
            {
                $this->Log(__LINE__ . ":请填写短信内容！");
                $message = Lang('system.26');
                return output(109,$message);
            }
            preg_match_all("/(?<={)[^}]+/", $content1, $result);
            $TemplateParam = array_combine($result[0], $content);
        }
        $TemplateParam = serialize($TemplateParam);
        // 根据商城id、管理员id、短信模板，查询该管理员是否重复添加该短信模板
        if($id != 0 && $id != '')
        {
            $r0 = MessageListModel::where(['store_id'=>$store_id,'type'=>$type,'type1'=>$type1])->where('id',"<>",$id)->field('id')->select()->toArray();
        }
        else
        {
            $r0 = MessageListModel::where(['store_id'=>$store_id,'type'=>$type,'type1'=>$type1])->field('id')->select()->toArray();
        }
        if ($r0)
        {
            $this->Log(__LINE__ . ":该类短信模板已添加，请勿重复添加！");
            $message = Lang('system.40');
            return output(109,$message);
        }
        else
        {
            if($id != 0 && $id != '')
            {
                $sql_update = array('type'=>$type,'type1'=>$type1,'Template_id'=>$tid,'content'=>$TemplateParam,'admin_id'=>$admin_id,'add_time'=>$time);
                $sql_where = array('store_id'=>$store_id,'id'=>$id);
                $r1 = Db::name('message_list')->where($sql_where)->update($sql_update);
                if ($r1 == -1)
                {
                    $Jurisdiction->admin_record($store_id, $operator, '修改了短信ID：'.$id.'失败',2,1,0,$operator_id);
                    $this->Log(__LINE__ . ":修改短信失败！参数:" . json_encode($sql_where));
                    $message = Lang('system.8');
                    return output(109,$message);
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $operator, '修改了短信ID：'.$id,2,1,0,$operator_id);
                    $this->Log(__LINE__ . ":修改短信成功！");
                    $message = Lang('Success');
                    return output('200',$message);
                }
            }
            else
            {
                $sql = array('store_id'=>$store_id,'type'=>$type,'type1'=>$type1,'Template_id'=>$tid,'content'=>$TemplateParam,'admin_id'=>$admin_id,'add_time'=>$time);
                $r1 = Db::name('message_list')->insert($sql);
                if ($r1 < 1)
                {
                    $Jurisdiction->admin_record($store_id, $operator, '添加短信失败',1,1,0,$operator_id);
                    $this->Log(__LINE__ . ":添加短信失败！参数:" . json_encode($sql));
                    $message = Lang('system.9');
                    return output(109,$message);
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $operator, '添加短信成功',1,1,0,$operator_id);
                    $this->Log(__LINE__ . ":添加短信成功！");
                    $message = Lang('Success');
                    return output('200',$message);
                }
            }
        }
    }

    // 删除短信
    public function delMessageList()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
        $store_type = addslashes(trim(Request::param('storeType')));
        $access_id = addslashes(trim(Request::param('accessId')));

        $id = addslashes(Request::param('id')); // 短信ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        $r = Db::table('lkt_message_list')->where('id',$id)->delete();
        if ($r)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除短信ID为' . $id,3,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":删除短信成功！");
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除短信ID为' . $id . '失败',3,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":删除短信失败！参数:".$id);
            $message = Lang('system.12');
            return output(109,$message);
        }
    }
    
    // 获取系统设置
    public function getSetSystem()
    {   
        $is_edit = addslashes(Request::param('is_edit')); //

        $list = Tools::Get_system_settings();

        $data = array('config'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 修改系统设置
    public function setSystem()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$store_type = addslashes(Request::param('storeType'));
    	$access_id = addslashes(Request::param('accessId'));
    	
    	$id = addslashes(Request::param('id')); // 
    	$logo = addslashes(Request::param('logoUrl')); // 登录页logo
        $H5_domain = addslashes(Request::param('h5Domain'));//H5地址
        $endurl = addslashes(Request::param('domainPath'));//根目录地址
    	$copyright_information = addslashes(Request::param('copyrightInformation')); // 版权信息
    	$record_information = addslashes(Request::param('recordInformation')); // 备案信息
    	$linkPageJson = addslashes(Request::param('linkPageJson')); // 登录页友情链接 
        $linkPageJson = urldecode($linkPageJson);
        $storeIdPrefix = addslashes(Request::param('storeIdPrefix')); // 商城id前缀
        $adminDefaultPortrait = addslashes(Request::param('adminDefaultPortrait'));//默认头像设置
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if ($logo == '')
        {
            $log->customerLog(__LINE__ . "登录页logo不能为空！");
            $message = Lang('system.27');
            return output(109,$message);
        }
        if ($copyright_information == '')
        {
            $log->customerLog(__LINE__ . "版权信息不能为空！");
            $message = Lang('system.28');
            return output(109,$message);
        }
        if ($record_information == '')
        {
            $log->customerLog(__LINE__ . "版权信息不能为空！");
            $message = Lang('system.29');
            return output(109,$message);
        }
        
        $time = date('Y-m-d H:i:s');
        if($id != '')
        {
            $sql_where = array('store_id'=>0,'id'=>$id);
            $sql_update = array('logo'=>$logo,'copyright_information'=>$copyright_information,'record_information'=>$record_information,'link_to_landing_page'=>$linkPageJson,'add_time'=>$time,'store_id_prefix'=>$storeIdPrefix,'admin_default_portrait'=>$adminDefaultPortrait);
            $r = Db::name('system_configuration')->where($sql_where)->update($sql_update);
            if ($r < 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改系统设置失败！',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改系统设置失败！参数:'. json_encode($sql_where);
                $this->Log($Log_content);
                $message = Lang('system.8');
                return output(109,$message);
            }
        }
        else
        {
            $sql = array('store_id'=>0,'logo'=>$logo,'copyright_information'=>$copyright_information,'record_information'=>$record_information,'link_to_landing_page'=>$linkPageJson,'add_time'=>$time,'store_id_prefix'=>$storeIdPrefix,'admin_default_portrait'=>$adminDefaultPortrait);
            $r = Db::name('system_configuration')->insert($sql);
            if ($r <= 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加系统设置失败！',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加系统设置失败！参数:'. json_encode($sql);
                $this->Log($Log_content);
                $message = Lang('system.9');
                return output(109,$message);
            }
        }

        //判断是否存在
        $sql_third = "select * from lkt_third where 1 = 1 ";
        $r_third = Db::query($sql_third);
        if($r_third)
        {
            $id = $r_third[0]['id'];
            $sql_update = array('H5'=>$H5_domain,'endurl'=>$endurl);
            $r = Db::name('third')->where(array('id'=>$id))->update($sql_update);
            if ($r >= 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改系统设置！',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改参数设置成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改系统设置失败！',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改参数设置失败！参数:'. json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(109,$message);
            }
        }
        else
        {
            $sql = array('H5'=>$H5_domain,'endurl'=>$endurl);
            $r = Db::name('third')->insert($sql);
            if ($r > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加系统设置！',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加参数设置成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加系统设置失败！',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加参数设置失败！参数:'. json_encode($sql);
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(109,$message);
            }
        }

        $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加系统设置成功';
        $this->Log($Log_content);
        $message = Lang('Success');
        return output(200,$message);

    }

    // 获取基础配置（新）
    public function GetBasicConfiguration()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

        $list = array();
        $r = ConfigModel::where(['store_id'=>$store_id])->field('H5_domain as h5_domain,message_day as messageSaveDay,exp_time as appLoginValid,watermark_name,watermark_url,logon_logo,copyright_information,record_information,link_to_landing_page,admin_default_portrait,store_name,app_logo,html_icon')->select()->toArray();
        if($r)
        {
            $list = $r[0];
        }

        $sql1 = "select merchant_logo from lkt_customer where id = '$store_id' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $list['store_logo'] = $r1[0]['merchant_logo'];
        }

    	$data = array('list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 设置基础配置（新）
    public function SetBasicConfiguration()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

        $store_name = addslashes(Request::param('store_name')); // 商城名称
        $H5_domain = addslashes(Request::param('h_Address')); // H5域名
    	$message_day = addslashes(Request::param('messageSaveDay')); // 消息保留天数
    	$exp_time = addslashes(Request::param('login_validity')); // 移动端登录有效期
        $copyright_information = addslashes(trim(Request::param('copyright_information'))); // 版权信息
        $record_information = addslashes(trim(Request::param('record_information'))); // 备案信息
        $link_to_landing_page = Request::param('link_to_landing_page'); // 登录页友情链接

        $app_logo = addslashes(trim(Request::param('app_logo'))); // 小程序授权登录logo
        $html_icon = addslashes(trim(Request::param('html_icon'))); // 浏览器icon图标
        $logon_logo = addslashes(trim(Request::param('logon_logo'))); // 登录logo
        $store_logo = addslashes(trim(Request::param('store_logo'))); // 商城后台logo

    	$watermark_name = addslashes(Request::param('watermarkName')); // 水印名称
    	$watermark_url = addslashes(Request::param('watermarkUrl')); // 水印网址

        $admin_default_portrait = addslashes(trim(Request::param('admin_default_portrait'))); // 默认头像设置

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $sql_update = array();
        $sql_update1 = array();
        if($store_name != '')
        {
            $sql_update['store_name'] = $store_name;
        }
        if($H5_domain != '')
        {
            $sql_update['H5_domain'] = $H5_domain;
        }
        if($message_day != '')
        {
            $sql_update['message_day'] = $message_day;
        }
        if($exp_time != '')
        {
            $sql_update['exp_time'] = $exp_time;
        }
        if($copyright_information != '')
        {
            $sql_update['copyright_information'] = $copyright_information;
        }
        if($record_information != '')
        {
            $sql_update['record_information'] = $record_information;
        }
        if($link_to_landing_page != '')
        {
            $sql_update['link_to_landing_page'] = $link_to_landing_page;
        }
        if($app_logo != '')
        {
            $sql_update['app_logo'] = $app_logo;
        }
        if($html_icon != '')
        {
            $sql_update['html_icon'] = $html_icon;
        }
        if($logon_logo != '')
        {
            $sql_update['logon_logo'] = $logon_logo;
        }
        if($store_logo != '')
        {
            $sql_update1['merchant_logo'] = $store_logo;
        }
        if($watermark_name != '')
        {
            $sql_update['watermark_name'] = $watermark_name;
        }
        if($watermark_url != '')
        {
            $sql_update['watermark_url'] = $watermark_url;
        }

        // $sql_update = array('message_day'=>$message_day,'H5_domain'=>$H5_domain,'exp_time'=>$exp_time,'watermark_name'=>$watermark_name,'watermark_url'=>$watermark_url,'modify_date'=>$time,'logon_logo'=>$logon_logo,'copyright_information'=>$copyright_information,'record_information'=>$record_information,'link_to_landing_page'=>$link_to_landing_page,'admin_default_portrait'=>$admin_default_portrait,'store_name'=>$store_name,'app_logo'=>$app_logo,'html_icon'=>$html_icon);
        $sql_where = array('store_id'=>$store_id);
        $r = Db::name('config')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了基础配置信息失败',2,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":修改基础配置失败: 参数:" . json_encode($sql_where));
            $message = Lang('system.8');
            return output(109,$message);
        }
        else
        {
            $sql_update1 = array('merchant_logo'=>$store_logo);
            if($sql_update1 != array())
            {
                $sql_where1 = array('id'=>$store_id);
                $r1 = Db::name('customer')->where($sql_where1)->update($sql_update1);
            }
            // 清理前端基础配置缓存（app.index.GetBasicConfiguration）
            cache('app_basic_configuration:' . intval($store_id), NULL);
            
            $Jurisdiction->admin_record($store_id, $operator, '修改了基础配置信息',2,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":修改基础配置信息成功！");
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 获取搜索及敏感词（新）
    public function GetSearchAndSensitiveWords()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

        $list = array();

        $r = HotkeywordsModel::where(['store_id'=>$store_id])->select()->toArray();
        if ($r)
        {
            $list = $r[0];
        }

    	$data = array('list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 设置搜索及敏感词（新）
    public function SearchAndSensitiveWords()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

        // $is_open = addslashes(trim(Request::param('isOpen'))); // 是否开启
        $num = addslashes(trim(Request::param('limitNum'))); // 关键词上限
        $keyword = addslashes(trim(Request::param('keyword'))); // 关键词

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $is_open = 1;
        $mch_keyword = '';
        if($is_open == 1)
        {
            // 正则，防止使用人员用中文逗号
            $keyword = preg_replace("/(\n)|(\s)|(\t)|(\')|(')|(，)/", ',', $keyword);
            $keyword_num = count(explode(',',$keyword));
            if($num < $keyword_num)
            {
                $Log_content = __LINE__ . $operator . ":关键词不能大于上限数量！";
                $this->Log($Log_content);
                $message = Lang('system.41');
                return output(109,$message);
            }
        }

        $r0 = HotkeywordsModel::where(['store_id'=>$store_id])->field('id')->select()->toArray();
        if ($r0)
        {
            $sql_where = array('store_id'=>$store_id);
            $sql_update = array('is_open'=>$is_open,'num'=>$num,'keyword'=>$keyword,'mch_keyword'=>$mch_keyword);
            $r = Db::name('hotkeywords')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '搜索配置修改失败！',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":搜索配置修改失败！: 参数:" . json_encode($sql_where));
                $message = Lang('system.8');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '搜索配置修改成功！',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":搜索配置修改成功！");
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $sql = array('store_id'=>$store_id,'is_open'=>$is_open,'num'=>$num,'keyword'=>$keyword,'mch_keyword'=>$mch_keyword);
            $r = Db::name('hotkeywords')->insert($sql);
            if ($r > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '搜索配置添加成功！',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":搜索配置添加成功！");
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '搜索配置添加失败！',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":搜索配置添加失败！: 参数:" . json_encode($sql_where));
                $message = Lang('system.48');
                return output(109,$message);
            }
        }
    }

    // 敏感字设置（新）
    public function selectSensitive()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

    	$word = addslashes(trim(Request::param('word'))); // 敏感词
    	$page = addslashes(trim(Request::param('pageNo'))); // 页码
    	$pagesize = addslashes(trim(Request::param('pageSize'))); // 每页数据
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $con = " store_id = '$store_id' and recycle = 0 ";
        if($word != '')
        {
            $con .= " and word like '%$word%' ";
        }
        $total = 0;
        $list = array();

        $sql0 = "select count(id) as total from lkt_sensitive_words where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql0 = "select id,word,add_time from lkt_sensitive_words where $con order by add_time desc limit $start,$pagesize ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $list = $r0;
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 添加敏感字（新）
    public function addSensitive()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

    	$id = addslashes(trim(Request::param('id'))); // 敏感词ID
    	$word = addslashes(trim(Request::param('word'))); // 敏感词

        $time = date("Y-m-d H:i:s");
        if($word == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '敏感词不能为空！';
            $this->Log($Log_content);
            $message = Lang('system.45');
            return output(109,$message);
        }

        if($id == '')
        {
            $sql0 = "select id from lkt_sensitive_words where store_id = '$store_id' and recycle = 0 and word = '$word' ";
        }
        else
        {
            $sql0 = "select id from lkt_sensitive_words where store_id = '$store_id' and recycle = 0 and word = '$word' and id != '$id' ";
        }
        $r0 = Db::query($sql0);
        if($r0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '敏感词重复';
            $this->Log($Log_content);
            $message = Lang('system.46');
            return output(109,$message);
        }

        if($id == '')
        {
            $sql1 = "insert into lkt_sensitive_words(word,add_time,store_id,recycle) value ('$word','$time','$store_id',0)";
            $r1 = Db::execute($sql1);
            if($r1 < 1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加失败';
                $this->Log($Log_content);
                $message = Lang('system.9');
                return output(109,$message);
            }
        }
        else
        {
            $sql1 = "update lkt_sensitive_words set word = '$word' where store_id = '$store_id' and id = '$id' ";
            $r1 = Db::execute($sql1);
            if($r1 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '修改失败';
                $this->Log($Log_content);
                $message = Lang('system.8');
                return output(109,$message);
            }
        }

        $message = Lang("Success");
        return output(200,$message);
    }

    // 删除敏感字（新）
    public function deleteSensitive()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

    	$ids = addslashes(trim(Request::param('ids'))); // 敏感词ID

        $sql1 = "update lkt_sensitive_words set recycle = 1 where store_id = '$store_id' and id in ($ids) ";
        $r1 = Db::execute($sql1);
        if($r1 == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '敏感词重复';
            $this->Log($Log_content);
            $message = Lang('system.12');
            return output(109,$message);
        }

        $message = Lang("Success");
        return output(200,$message);
    }

    // 批量上传
    public function importSensitives()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

        $time = date("Y-m-d H:i:s");
        $filename = $_FILES['file']['tmp_name'];
        $name = $_FILES['file']['name'];
        if (empty ($filename)) 
        {
            $message = Lang('product.91');
            return output(109, $message);
        }

        $handle = fopen($filename,'r');
        $result = $this->input_excel($filename);

        $len_result = count($result);

        if($len_result == 0)
        {
            $message = Lang('product.92');
            return output("50973", $message,null);
        }
        
        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1,'recycle'=>0])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id'];
        
        Db::startTrans();
        foreach ($result as $k => $v) 
        {
            $word = addslashes(trim($v['A']));

            if($word == '')
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . '敏感词不能为空！';
                $this->Log($Log_content);
                $message = Lang('system.45');
                return output(109,$message);
            }
            
            $sql0 = "select id from lkt_sensitive_words where store_id = '$store_id' and recycle = 0 and word = '$word' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . '敏感词重复';
                $this->Log($Log_content);
                $message = Lang('system.46');
                return output(109,$message);
            }
            
            $sql1 = "insert into lkt_sensitive_words(word,add_time,store_id,recycle) value ('$word','$time','$store_id',0)";
            $r1 = Db::execute($sql1);
            if($r1 < 1)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加失败';
                $this->Log($Log_content);
                $message = Lang('system.9');
                return output(109,$message);
            }
        }

        Db::commit();
        $message = Lang('Success');
        return output(200, $message);
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

    // 获取物流及打印（新）
    public function GetLogisticsAndPrinting()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

        $express_list = array(); // 物流
        $print_list = array(); // 打印
        $r = ConfigModel::where(['store_id'=>$store_id])->field('express_address,express_number,express_key,express_secret,express_tempId')->select()->toArray();
        if($r)
        {
            $express_list = $r[0];
            if(isset($express_list['express_key']) && $express_list['express_key'] !== '')
            {
                $express_list['express_key'] = $this->maskValue($express_list['express_key'], 4, 4);
            }
            if(isset($express_list['express_secret']) && $express_list['express_secret'] !== '')
            {
                $express_list['express_secret'] = $this->maskValue($express_list['express_secret'], 4, 4);
            }
        }

        $r_0 = PrintSetupModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('print_name,print_url,sheng,shi,xian,address,phone')->select()->toArray();
        if($r_0)
        {
            $print_list = $r_0[0];
        }

    	$data = array('express_list'=>$express_list,'print_list'=>$print_list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 物流及打印（新）
    public function LogisticsAndPrinting()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

    	$express_address = addslashes(Request::param('expressAddress')); // 查询接口地址
    	$express_number = addslashes(Request::param('expressNumber')); // 用户编号
    	$express_key = addslashes(Request::param('authorization')); // 授权key
    	// $express_key = addslashes(Request::param('expressKey')); // 接口调用key
        $express_secret = addslashes(Request::param('express_secret'));// secret
        $express_tempId = addslashes(Request::param('express_tempId'));// tempId

    	$print_name = addslashes(Request::param('printName')); // 打印名称
    	$print_url = addslashes(Request::param('printUrl')); // 打印网址
    	$sheng = addslashes(Request::param('sheng')); // 省
    	$shi = addslashes(Request::param('shi')); // 市
    	$xian = addslashes(Request::param('xian')); // 县
    	$address = addslashes(Request::param('address')); // 地址
    	$phone = addslashes(Request::param('phone')); // 联系电话

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $masked_express_key = $this->isMaskedValue($express_key);
        $masked_express_secret = $this->isMaskedValue($express_secret);
        if ($masked_express_key || $masked_express_secret)
        {
            $existing_express = ConfigModel::where(['store_id'=>$store_id])->field('express_key,express_secret')->select()->toArray();
            if ($existing_express)
            {
                if ($masked_express_key)
                {
                    $express_key = $existing_express[0]['express_key'];
                }
                if ($masked_express_secret)
                {
                    $express_secret = $existing_express[0]['express_secret'];
                }
            }
        }

        Db::startTrans();
        $sql_update = array('express_address'=>$express_address,'express_number'=>$express_number,'express_key'=>$express_key,'express_secret'=>$express_secret,'express_tempId'=>$express_tempId);
        $sql_where = array('store_id'=>$store_id);
        $r = Db::name('config')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '修改了物流信息失败',2,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":修改了物流信息失败: 参数:" . json_encode($sql_where));
            $message = Lang('system.8');
            return output(109,$message);
        }
        else
        {
            $r_0 = PrintSetupModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('id')->select()->toArray();
            if($r_0)
            {
                $sql_where_0 = array('store_id'=>$store_id);
                $sql_update_0 = array('print_name'=>$print_name,'print_url'=>$print_url,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'phone'=>$phone);
                $r_1 = Db::name('print_setup')->where($sql_where_0)->update($sql_update_0);
                if ($r_1 == -1)
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '修改了打印配置信息失败',2,1,0,$operator_id);
                    $this->Log(__LINE__ . $operator . ":修改打印配置失败: 条件参数：" . json_encode($sql_where_0) . "；修改参数：" . json_encode($sql_update_0));
                    $message = Lang('system.8');
                    return output(109,$message);
                }
            }
            else
            {
                $sql_insert_0 = array('store_id'=>$store_id,'mch_id'=>0,'print_name'=>$print_name,'print_url'=>$print_url,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'phone'=>$phone,'add_time'=>$time);
                $r_1 = Db::name('print_setup')->insert($sql_insert_0);
                if ($r_1 == -1)
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '修改了打印配置信息失败',2,1,0,$operator_id);
                    $this->Log(__LINE__ . $operator . ":修改打印配置失败: 参数:" . json_encode($sql_insert_0));
                    $message = Lang('system.8');
                    return output(109,$message);
                }
            }

            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator, '修改了物流及打印配置信息',2,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":修改物流及打印配置信息成功！");
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 获取国际话设置
    public function storeIntenationSetting()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

        $list = array();
        $sql = "select default_lang_code from lkt_config where store_id = '$store_id' ";
        $r = Db::query($sql);
        if($r)
        {
            $default_lang_code = $r[0]['default_lang_code'];
            $list = array('default_lang_code'=>$default_lang_code);
        }
        $data = array('list'=>$list);

        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 设置国际话设置
    public function addOrUpdateStoreIntenationSetting()
    {
        $store_id = addslashes(trim(Request::param('storeId')));
    	$store_type = addslashes(trim(Request::param('storeType')));
    	$access_id = addslashes(trim(Request::param('accessId')));

    	$default_lang_code = addslashes(trim(Request::param('default_lang_code')));

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $sql = "update lkt_config set default_lang_code = '$default_lang_code' where store_id = '$store_id' ";
        $r = Db::execute($sql);
        if($r == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改国际化设置失败！',2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改国际化设置失败！参数:'. json_encode($sql_update);
            $this->Log($Log_content);
            $message = Lang('operation failed');
            return output(109,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改国际化设置！',2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改国际化设置成功';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    private function maskMailConfigString($mailConfig)
    {
        if ($mailConfig === null || $mailConfig === '')
        {
            return $mailConfig;
        }
        $decoded = json_decode($mailConfig, true);
        if (!is_array($decoded))
        {
            return $mailConfig;
        }
        $keys = array('password','auth_code','authCode','authorization','smtp_password');
        foreach ($keys as $key)
        {
            if (isset($decoded[$key]) && $decoded[$key] !== '')
            {
                $decoded[$key] = $this->maskValue($decoded[$key], 2, 2);
            }
        }
        return json_encode($decoded, JSON_UNESCAPED_UNICODE);
    }

    private function mergeMaskedMailConfig($storeId, $mailConfig)
    {
        $decoded = json_decode($mailConfig, true);
        if (!is_array($decoded))
        {
            return $mailConfig;
        }
        $keys = array('password','auth_code','authCode','authorization','smtp_password');
        $has_masked = false;
        foreach ($keys as $key)
        {
            if (isset($decoded[$key]) && $this->isMaskedValue($decoded[$key]))
            {
                $has_masked = true;
                break;
            }
        }
        if (!$has_masked)
        {
            return $mailConfig;
        }
        $existing = ConfigModel::where(['store_id'=>$storeId])->value('mail_config');
        $existing_decoded = json_decode($existing, true);
        if (!is_array($existing_decoded))
        {
            return $mailConfig;
        }
        foreach ($keys as $key)
        {
            if (isset($decoded[$key]) && $this->isMaskedValue($decoded[$key]) && array_key_exists($key, $existing_decoded))
            {
                $decoded[$key] = $existing_decoded[$key];
            }
        }
        return json_encode($decoded, JSON_UNESCAPED_UNICODE);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/system.log",$Log_content);
        return;
    }
}
