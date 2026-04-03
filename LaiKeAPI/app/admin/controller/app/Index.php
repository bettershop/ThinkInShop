<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use think\facade\Session;
use think\facade\Log;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\ServerPath;
use app\common\Plugin\Plugin;
use app\common\pinyin;
use app\common\sign;
use app\common\Plugin\CouponPublicMethod;
use app\common\Plugin\Auction;
use app\common\Plugin\IntegralPublicMethod;
use app\common\Plugin\Distribution;

use app\admin\model\UserModel;
use app\admin\model\RecordModel;
use app\admin\model\BannerModel;
use app\admin\model\ConfigModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProductClassModel;
use app\admin\model\UiNavigationBarModel;
use app\admin\model\GuideModel;
use app\admin\model\ProLabelModel;
use app\admin\model\AdminCgGroupModel;
use app\admin\model\DiyModel;
use app\admin\model\PluginsModel;
use app\admin\model\ActivityModel;
use app\admin\model\ProductTitleModel;
use app\admin\model\SecondsTimeModel;
use app\admin\model\DistributionConfigModel;
use app\admin\model\DistributionGradeModel;
use app\admin\model\DistributionGoodsModel;
use app\admin\model\MchClassModel;
use app\admin\model\SystemMessageModel;
use app\admin\model\SecondsConfigModel;
use app\admin\model\MemberConfigModel;

class Index 
{
    const INDEX_CACHE_PREFIX = 'app_index_cache:';
    const INDEX_TOP_PLUGIN_CACHE_PREFIX = 'app_index_top_plugin:';

    // 获取用户是否是会员
    public function get_membership_status()
    {
        $store_id = addslashes(Request::param('store_id')); // 商城ID
        $access_id = addslashes(Request::param('access_id')); // 授权ID

        $membership_status = false;
        $now = date("Y-m-d H:i:s");
        if($access_id != '')
        {
            $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('grade,grade_end')->select()->toArray();
            if($r0)
            {
                if($r0[0]['grade'] != 0)
                {
                    if($r0[0]['grade_end'] > $now) 
                    {
                        $membership_status = true;
                    }
                }
            }
        }

        $data = array('membership_status' => $membership_status);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 首页是否diy接口
    public function hasDiy()
    {
        $store_id = addslashes(Request::param('store_id')); // 商城ID
        $access_id = addslashes(Request::param('access_id')); // 授权ID

        $has = false;
        $sql = "select status,plugin_code from lkt_plugins where store_id = '$store_id' and plugin_code = 'diy' and flag = 0 ORDER BY id ";
        $r = Db::query($sql);
        if($r)
        {
            $r0 = DiyModel::where(['store_id'=>$store_id,'status'=>1,'mch_id'=>0])->select()->toArray();
            if($r0)
            {
                $has = true;
            }
        }

        $message = Lang('Success');
        return output(200,$message, $has);
    }

    // 获取前端基础信息配置
    public function GetBasicConfiguration()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        if($store_id == '')
        {
            $store_id = addslashes(trim(Request::param('storeId')));
        }

        if($store_id == '')
        {
            $message = Lang("Parameter error");
            return output(ERROR_CODE_CSCW,$message);
        }

        $cache_key = 'app_basic_configuration:' . intval($store_id);
        $cache_data = cache($cache_key);
        if(is_array($cache_data))
        {
            $message = Lang('Success');
            return output(200,$message,$cache_data);
        }

        $list = array();
        $r = ConfigModel::where('store_id',$store_id)->field('H5_domain as h5_domain,message_day as messageSaveDay,exp_time as appLoginValid,watermark_name,watermark_url,logon_logo,copyright_information,record_information,link_to_landing_page,admin_default_portrait,store_name,app_logo,html_icon')->find();
        if($r)
        {
            $list = $r->toArray();
        }

        $store_logo = Db::name('customer')->where('id',$store_id)->value('merchant_logo');
        if($store_logo !== null)
        {
            $list['store_logo'] = $store_logo;
        }

        $data = array('list'=>$list);
        cache($cache_key,$data,300);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 首页数据
    public function index()
    {
        $store_id = addslashes(Request::param('store_id')); // 商城ID
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权ID
        $language = addslashes(Request::param('language')); // 语言
        $longitude = addslashes(Request::param('longitude')); // 经度
        $latitude = addslashes(Request::param('latitude')); // 纬度

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $lang_code = Tools::get_lang($language);

        $cache_longitude = (is_numeric($longitude) ? sprintf('%.4f',(float)$longitude) : '0.0000');
        $cache_latitude = (is_numeric($latitude) ? sprintf('%.4f',(float)$latitude) : '0.0000');
        $index_cache_key = self::INDEX_CACHE_PREFIX . intval($store_id) . ':' . $lang_code . ':' . md5($access_id . ':' . $cache_longitude . ':' . $cache_latitude);
        $index_cache_data = cache($index_cache_key);
        if(is_array($index_cache_data))
        {
            $message = Lang('Success');
            return output(200,$message,$index_cache_data);
        }

        $region = Tools::get_access_area($store_id, $longitude, $latitude); // 获取地区

        // 获取首页logo
        $logo = '';
        $appTitle = '';
        $r_config = ConfigModel::where('store_id', $store_id)->field('logo1,app_title')->find();
        if ($r_config)
        {
            $r_config = $r_config->toArray();
            $logo = ServerPath::getimgpath($r_config['logo1'], $store_id);
            $appTitle = $r_config['app_title'];
        }

        $newsNum = 0; // 消息数量
        $user_id = '';
        $user_info = array();
        if (!empty($access_id))
        { // 存在
            // 查询用户ID
            $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,grade_end,is_box,is_out')->find();
            if ($r0)
            {
                $user_info = $r0->toArray();
                $user_id = $user_info['user_id'];
            }

            if (!empty($user_id))
            { // 过期
                $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组
                // 查询消息数量
                if($getPayload_test)
                {
                    $r_xx = SystemMessageModel::where('store_id', $store_id)->where('recipientid',$user_id)->where('type',1)->count();
                    $newsNum = $r_xx;
                }
            }
        }

        $login_status = '0'; // 未登录
        if ($user_id)
        {
            $login_status = '1'; // 已登录
        }

        // 获取会员折扣
        $Member_discount = array('store_id' => $store_id, 'access_id' => $access_id);
        $grade_list = PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade']; // 会员等级ID
        $grade_rate = $grade_list['grade_rate']; // 折扣值

        $banner = array();
        // 查询轮播图,根据排序、轮播图id顺序排列
        $r_banner = BannerModel::where(['store_id'=>$store_id,'mch_id'=>0])->where('type','<>', 4)->order('sort', 'desc')->select()->toArray();
        if ($r_banner)
        {
            foreach ($r_banner as $k => $v)
            {
                $v['image'] = ServerPath::getimgpath($v['image'], $store_id); // 图片
                $domain = strstr($v['url'], 'tabBar');
                if ($domain)
                {
                    $v['type'] = 'switchTab';
                }
                else
                {
                    $v['type'] = 'navigate';
                }
                $banner[] = $v;
            }
        }

        $grade_remind = false;
        $memberPlugin = false;

        // 插件
        $Plugin = new Plugin();
        $UI_Plugin_arr = $Plugin->Get_plugin_entry($store_id);
        $top_marketing_plugin = $this->getTopMarketingPlugin($store_id);

        $livingStatus = false; // 没有推荐主播
        if(isset($UI_Plugin_arr['living']) && $UI_Plugin_arr['living'] == 1)
        {
            $sql_l = "select id from lkt_living_room where store_id = '$store_id' and living_status = 1 and recycle = 0 limit 1";
            $r_l = Db::query($sql_l);
            if($r_l)
            {
                $livingStatus = true; // 有推荐主播
            }
        }
        // 插件
        $Plugin_arr = array('sign' => 0, 'coupon' => 0, 'go_group' => 0, 'seconds' => 0, 'auction' => 0, 'integral' => 0, 'daily_update' => 0, 'live_broadcast' => 1, 'preferred_stores' => 0, 'member_center' => 1, 'mch' => 0, 'distribution' => 0);

        $mobile_terminal_image = '';
        $subtraction_list = array();
        $member_equity = array();
        foreach ($UI_Plugin_arr as $k => $v)
        {
            if ($k == 'mch' && $v == 1)
            {
                $Plugin_arr['mch'] = 1;
            }
            else if ($k == 'coupon' && $v == 1)
            {
                $Plugin_arr['coupon'] = 1;
            }
            else if ($k == 'seconds' && $v == 1)
            {
                $Plugin_arr['seconds'] = 1;
            }
            else if ($k == 'go_group' && $v == 1)
            {
                $Plugin_arr['go_group'] = 1;
            }
            else if ($k == 'auction' && $v == 1)
            {
                $Plugin_arr['auction'] = 1;
            }
            else if ($k == 'integral' && $v == 1)
            {        
                $Plugin_arr['integral'] = 1;
            }
            else if($k == 'member' && $v == 1)
            {
                $memberPlugin = true;
                if($login_status == 1 && $grade == 1)
                {
                    $res_1 = MemberConfigModel::where(['store_id'=>$store_id,'renew_open'=>1,'is_open'=>1])->field('renew_day,member_equity')->select()->toArray();
                    if ($res_1)
                    {
                        $auto_time = $res_1[0]['renew_day'];//自动续费提醒时间
                        //计算离会员到期还有多久
                        $now = strtotime("now");
                        $end = isset($user_info['grade_end']) ? strtotime($user_info['grade_end']) : 0;
                        if ($now < $end)
                        {
                            $cha = floor(($end - $now) / 86400); //相差天数
                            if ($cha <= $auto_time && isset($user_info['is_box']) && isset($user_info['is_out']) && $user_info['is_box'] == 1 && $user_info['is_out'] == 0)
                            {//到了自动提醒时间,且用户未关闭提示
                                $grade_remind = true;
                            }
                        }
                        $member_equity = json_decode($res_1[0]['member_equity'],true);
                    }
                }
            }
        }

        // 获取商品设置
        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));

        $nav_list = array();
        $ui_navigation = UiNavigationBarModel::where(['store_id'=>$store_id])->field('name,image,url,is_login,isshow')->order('sort', 'desc')->select()->toArray();
        if ($ui_navigation)
        {
            foreach ($ui_navigation as $k => $v)
            {
                $v['appimg'] = ServerPath::getimgpath($v['image'], $store_id);  // 图片
                $v['isPlugin'] = true;
                $url = $v['url'];
                
                if (strstr($url, '/pagesA/shop/coupon'))
                { // 优惠券
                    if (!isset($UI_Plugin_arr['coupon']) || $UI_Plugin_arr['coupon'] != 1)
                    {
                        unset($ui_navigation[$k]);
                        continue;
                    }
                }
                else if (strstr($url, '/pagesA/OrderBidding/OrderBidding'))
                { // 竞拍专场
                    if (!isset($UI_Plugin_arr['auction']) || $UI_Plugin_arr['auction'] != 1)
                    {
                        unset($ui_navigation[$k]);
                        continue;
                    }
                }
                else if (strstr($url, '/pagesA/OrderBidding/StoreBidding'))
                { // 店铺专场
                    if (!isset($UI_Plugin_arr['auction']) || $UI_Plugin_arr['auction'] != 1)
                    {
                        unset($ui_navigation[$k]);
                        continue;
                    }
                }
                else if(strstr($url, '/pagesB/seckill/seckill?needLogin=1'))
                { // 秒杀
                    if (!isset($UI_Plugin_arr['seconds']) || $UI_Plugin_arr['seconds'] != 1)
                    {
                        unset($ui_navigation[$k]);
                        continue;
                    }
                }
                else if (strstr($url, '/pagesA/group/group'))
                { // 拼团
                    if (!isset($UI_Plugin_arr['go_group']) || $UI_Plugin_arr['go_group'] != 1)
                    {
                        unset($ui_navigation[$k]);
                        continue;
                    }
                }
                else if (strstr($url, '/pagesB/integral/integral?toBack=true&needLogin=1'))
                { // 积分商城
                    if (!isset($UI_Plugin_arr['integral']) || $UI_Plugin_arr['integral'] != 1)
                    {
                        unset($ui_navigation[$k]);
                        continue;
                    }
                }
                else if (strstr($url, '/pagesB/userVip/memberCenter'))
                { // 会员
                    if (!isset($UI_Plugin_arr['member']) || $UI_Plugin_arr['member'] != 1)
                    {
                        unset($ui_navigation[$k]);
                        continue;
                    }
                }
                else if (strstr($url, '/pagesC/preSale/goods/goods'))
                { // 预售
                    if (!isset($UI_Plugin_arr['presell']) || $UI_Plugin_arr['presell'] != 1)
                    {
                        unset($ui_navigation[$k]);
                        continue;
                    }
                }
                else if (strstr($url, '/pagesA/distribution/fxProduct'))
                { // 分销中心
                    if (!isset($UI_Plugin_arr['distribution']) || $UI_Plugin_arr['distribution'] != 1)
                    {
                        unset($ui_navigation[$k]);
                        continue;
                    }
                }
                else 
                {
                    if ($v['isshow'] == 0)
                    {
                        unset($ui_navigation[$k]);
                        continue;
                    }
                }
                $nav_list[] = $v;
            }
        }

        $Marketing_list = array();
        $Activity_list = ActivityModel::where(['store_id'=>$store_id,'is_display'=>1])->order('sort', 'desc')->select()->toArray();
        if ($Activity_list)
        {
            foreach ($Activity_list as $key => $value)
            {
                $activity_id = $value['id'];
                $value['list'] = array();

                if ($value['is_image'] == 1)
                {
                    $value['image'] = ServerPath::getimgpath($value['image'], $store_id);
                }
                else
                {
                    $value['image'] = '';
                }
                if ($value['activity_type'] == 0)
                { // 营销
                    $Activity_list_sql0 = "select tt.* from (select a.id,a.cover_map,a.product_title,a.imgurl,a.status,a.volume,a.s_type,min(c.price) over (partition by c.pid) as price,c.yprice,row_number () over (partition by c.pid) as top from lkt_activity_pro as b left join lkt_product_list as a on b.p_id = a.id left join lkt_configure as c ON a.id = c.pid where b.store_id = '$store_id' and b.activity_id = '$activity_id'and b.is_display = 1 and a.store_id = '$store_id' and a.commodity_type in (0,1) and a.mch_status = 2 and a.recycle = 0 and c.recycle = 0 and a.active = 1 and a.is_presell = 0 and a.mch_id != 0 $status order by b.sort desc) as tt where tt.top < 2 ";
                    $Activity_list0 = Db::query($Activity_list_sql0);
                    if ($Activity_list0)
                    {
                        $activity_pid_list = array_values(array_unique(array_column($Activity_list0,'id')));
                        $activity_num_map = array();
                        if($activity_pid_list)
                        {
                            $r_configure = ConfigureModel::where('recycle',0)->whereIn('pid',$activity_pid_list)->field('pid,sum(num) as num')->group('pid')->select()->toArray();
                            if($r_configure)
                            {
                                foreach ($r_configure as $v_configure)
                                {
                                    $activity_num_map[intval($v_configure['pid'])] = intval($v_configure['num']);
                                }
                            }
                        }
                        foreach ($Activity_list0 as $k => $v)
                        {
                            $s_type = explode(',', trim($v['s_type'],','));
                            $s_type_list = PC_Tools::getProductLabel(array('store_id'=>$store_id,'s_type'=>$s_type));
                            
                            $Activity_list0[$k]['xp'] = false;
                            $Activity_list0[$k]['rx'] = false;
                            $Activity_list0[$k]['tj'] = false;
                            if(in_array('新品',$s_type_list))
                            {
                                $Activity_list0[$k]['xp'] = true;
                            }
                            if(in_array('热销',$s_type_list))
                            {
                                $Activity_list0[$k]['rx'] = true;
                            }
                            if(in_array('推荐',$s_type_list))
                            {
                                $Activity_list0[$k]['tj'] = true;
                            }

                            if ($v['volume'] < 0)
                            {
                                $Activity_list0[$k]['volume'] = 0;
                            }

                            // $v['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['price']));
                            // $v['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['yprice']));
                            $Activity_list0[$k]['price'] = floatval($v['price']);
                            $Activity_list0[$k]['yprice'] = floatval($v['yprice']);
                            $Activity_list0[$k]['vip_price'] = round($v['price'] * $grade_rate, 2);

                            $Activity_list0[$k]['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                            $Activity_list0[$k]['cover_map'] = ServerPath::getimgpath($v['cover_map'], $store_id);

                            $pid = intval($v['id']);
                            $Activity_list0[$k]['num'] = isset($activity_num_map[$pid]) ? $activity_num_map[$pid] : 0;
                        }
                        $value['list'] = $Activity_list0;
                    }
                }
                else
                { // 插件
                    if ($value['plug_type'] == 7)
                    {
                        //首页积分活动不检查插件配置
                        if ($UI_Plugin_arr['integral'] == 1)
                        {
                            $value['list'] = $this->integral($store_id);
                        }
                    }
                    // if ($value['plug_type'] == 2)
                    // {
                    //     if ($UI_Plugin_arr['go_group'] == 1)
                    //     {
                    //         $value['list'] = $this->go_group($store_id);
                    //     }
                    // }
                    // else if ($value['plug_type'] == 4)
                    // {
                    //     if ($UI_Plugin_arr['auction'] == 1)
                    //     {
                    //         $value['list'] = $this->auction($store_id, $access_id);
                    //     }
                    // }
                    // else if ($value['plug_type'] == 7)
                    // {
                    //     if ($UI_Plugin_arr['integral'] == 1)
                    //     {
                    //         $value['list'] = $this->integral($store_id);
                    //     }
                    // }
                    // else if ($value['plug_type'] == 8)
                    // {
                    //     if ($UI_Plugin_arr['seconds'] == 1)
                    //     {
                    //         $value['list'] = $this->seconds($store_id);
                    //     }
                    // }
                }
                $Marketing_list[] = $value;
            }
        }

        $productArray = array(); // 商品数组
        //查询商品并分类显示返回JSON至小程序
        $r_c = ProductClassModel::where(['store_id'=>$store_id,'is_display'=>1,'recycle'=>0,'sid'=>0,'examine'=>1,'lang_code'=>$lang_code,'notset'=>0])->field('cid,pname,english_name')->order('sort','desc')->select()->toArray();
        if ($r_c)
        {
            foreach ($r_c as $key => $value)
            {
                $cid = $value['cid'];

                $query_content = "a.id,a.commodity_type,a.product_title,a.cover_map,a.imgurl,a.volume,a.s_type,a.num,a.status,a.brand_id,c.id as cid,min(c.price) over (partition by c.pid) as price,c.yprice,row_number () over (partition by c.pid) as top,m.name as mch_name,m.logo,m.head_img,a.is_appointment,a.write_off_settings,a.write_off_mch_ids,a.real_volume,a.recycle";
                $query_criteria = "a.store_id = '$store_id' and a.commodity_type in (0,1) and a.mch_status = 2 and a.recycle = 0 and a.active = 1 and c.recycle = 0 and a.is_presell = 0 and a.product_class like '%-$cid-%' and a.mch_id != 0 and a.show_adr like '%,1,%' and a.lang_code = '$lang_code' $status ";

                $sql_2 = "select tt.* from (select $query_content from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where $query_criteria order by a.sort desc,a.upper_shelf_time desc) as tt where tt.top<2 LIMIT 10";
                $r_2 = Db::query($sql_2);
                if ($r_2)
                {
                    $pay_people_map = array();
                    $pid_list = array_values(array_unique(array_column($r_2,'id')));
                    if($pid_list)
                    {
                        $pid_list_str = implode(',',array_map('intval',$pid_list));
                        $sql_pay_people = "select p_id,count(DISTINCT r_sNo) as total from lkt_order_details where r_status != 0 and p_id in ($pid_list_str) group by p_id";
                        $res_pay_people = Db::query($sql_pay_people);
                        if($res_pay_people)
                        {
                            foreach ($res_pay_people as $v_pay_people)
                            {
                                $pay_people_map[intval($v_pay_people['p_id'])] = intval($v_pay_people['total']);
                            }
                        }
                    }
                    foreach ($r_2 as $k => $v)
                    {
                        $s_type = explode(',', trim($v['s_type'],','));
                        $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                        $r_2[$k]['s_type_list'] = $s_type_list;
                        if ($v['volume'] < 0)
                        {
                            $r_2[$k]['volume'] = 0;
                        }

                        $r_2[$k]['logo'] = ServerPath::getimgpath($v['logo'], $store_id);
                        $r_2[$k]['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                        $r_2[$k]['cover_map'] = ServerPath::getimgpath($v['cover_map'], $store_id);

                        // $v['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['price']));
                        // $v['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['yprice']));
                        $r_2[$k]['price'] = round($v['price'],2);
                        $r_2[$k]['yprice'] = round($v['yprice'],2);
                        $r_2[$k]['vip_yprice'] = round($v['price'],2);
                        $r_2[$k]['vip_price'] = round($v['price'] * $grade_rate, 2);
                        $r_2[$k]['contNum'] = $v['num'];
                        $r_2[$k]['goodsNum'] = $v['num'];
                        $r_2[$k]['classId'] = $cid;

                        if($v['write_off_settings'] == 1)
                        { // 核销设置 1.线下核销 2.无需核销
                            $isAddCar = 2; // 2.不能加入购物车
                        }
                        else
                        {
                            $isAddCar = 1; // 1.能加入购物车
                        }

                        $r_2[$k]['isAddCar'] = $isAddCar;

                        $pid = intval($v['id']);
                        $r_2[$k]['payPeople'] = isset($pay_people_map[$pid]) ? $pay_people_map[$pid] : 0;

                        $mch_id = isset($v['mchId']) ? (int)$v['mchId'] : (isset($v['mch_id']) ? (int)$v['mch_id'] : 0);
                        $marketingParams = $this->getTopMarketingParamsByPlugin($top_marketing_plugin,(int)$store_id,(int)$pid,$language ? $language : 'zh_CN',$mch_id);
                        if($marketingParams)
                        {
                            $r_2[$k]['marketingParams'] = $marketingParams;
                        }
                    }
                }
                $r_c[$key]['list'] = $r_2;
            }
            $productArray = $r_c;
        }

        $data = array('logo' => $logo,'appTitle' => $appTitle, 'xxnum' => $newsNum, 'region' => $region, 'banner' => $banner, 'grade' => $grade_rate,'memberPlugin'=>$memberPlugin,'grade_remind'=>$grade_remind, 'login_status' => $login_status,  'list2' => $productArray, 'Marketing_list' => $Marketing_list, 'subtraction_list' => $subtraction_list,'nav_list' => $nav_list,'member_equity'=>$member_equity,'livingStatus'=>$livingStatus);
        cache($index_cache_key,$data,120);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取首页营销优先插件（缓存）
    private function getTopMarketingPlugin($store_id)
    {
        $store_id = intval($store_id);
        $cache_key = self::INDEX_TOP_PLUGIN_CACHE_PREFIX . $store_id;
        $cache_data = cache($cache_key);
        if(is_array($cache_data) && isset($cache_data['code']))
        {
            return $cache_data;
        }
        if($cache_data === 'none')
        {
            return null;
        }

        $sql_plugins = "select plugin_code,plugin_name from lkt_plugins where store_id = '$store_id' and flag = 0 order by plugin_sort desc,id asc";
        $plugins = Db::query($sql_plugins);
        if(!$plugins)
        {
            cache($cache_key,'none',300);
            return null;
        }

        $excludeTypes = array('coupon','living','mch','wallet','sign','sigin','diy');
        $supportTypes = array('distribution','seconds','go_group','presell','zc','auction','flashsale','integral','member');
        foreach ($plugins as $plugin)
        {
            $plugin_code = isset($plugin['plugin_code']) ? $plugin['plugin_code'] : '';
            if($plugin_code == '' || in_array($plugin_code,$excludeTypes))
            {
                continue;
            }

            $activityCode = $plugin_code == 'advertising' ? 'zc' : $plugin_code;
            if(!in_array($activityCode,$supportTypes))
            {
                continue;
            }

            $top_plugin = array(
                'code' => $activityCode,
                'name' => isset($plugin['plugin_name']) ? $plugin['plugin_name'] : ''
            );
            cache($cache_key,$top_plugin,300);
            return $top_plugin;
        }

        cache($cache_key,'none',300);
        return null;
    }

    // 根据首页营销优先插件，获取单商品营销参数
    private function getTopMarketingParamsByPlugin($top_plugin,$store_id,$goods_id,$language,$mch_id = 0)
    {
        if(!$top_plugin || !isset($top_plugin['code']))
        {
            return null;
        }

        $store_id = intval($store_id);
        $goods_id = intval($goods_id);
        $mch_id = intval($mch_id);
        $language = $language ? addslashes($language) : 'zh_CN';
        $type = $top_plugin['code'];
        $name = isset($top_plugin['name']) ? $top_plugin['name'] : '';
        $param = '';

        if($type == 'distribution')
        {
            $sql = "select id from lkt_distribution_goods where store_id = '$store_id' and p_id = '$goods_id' and recycle = 0 order by id desc limit 1";
            $res = Db::query($sql);
            if($res)
            {
                $fx_id = intval($res[0]['id']);
                $param = "?isDistribution=true&toback=true&pro_id=" . $goods_id . "&fx_id=" . $fx_id . "&language=" . $language;
            }
        }
        else if($type == 'seconds')
        {
            $sql = "select id from lkt_seconds_activity where store_id = '$store_id' and goodsId = '$goods_id' and is_delete = 0 and isshow = 1 and status != 3 order by id asc limit 1";
            $res = Db::query($sql);
            if($res)
            {
                $sec_id = intval($res[0]['id']);
                $param = "pro_id=" . $goods_id . "&navType=1&id=" . $sec_id . "&type=MS&language=" . $language;
            }
        }
        else if($type == 'go_group')
        {
            $sql = "select activity_id from lkt_group_goods where goods_id = '$goods_id' and recycle = 0 order by id desc limit 1";
            $res = Db::query($sql);
            if($res)
            {
                $activity_id = $res[0]['activity_id'];
                $param = "pro_id=" . $goods_id . "&goodsId=" . $goods_id . "&acId=" . $activity_id . "&type=MS&language=" . $language;
            }
        }
        else if($type == 'presell')
        {
            $sql = "select id from lkt_pre_sell_goods where product_id = '$goods_id' and is_delete = 0 and is_display = 1 limit 1";
            $res = Db::query($sql);
            if($res)
            {
                $param = "toback=true&pro_id=" . $goods_id . "&language=" . $language;
            }
        }
        else if($type == 'zc')
        {
            $sql = "select id from lkt_bbs_post where store_id = '$store_id' and recycle = 0 and is_hide = 0 and status = 2 and FIND_IN_SET('$goods_id', pro_ids) limit 1";
            $res = Db::query($sql);
            if($res)
            {
                $zc_id = intval($res[0]['id']);
                $param = "id=" . $zc_id . "&language=" . $language;
            }
        }
        else if($type == 'auction')
        {
            $sql = "select id,session_id from lkt_auction_product where goods_id = '$goods_id' and recovery = 0 and status = 1 and is_show = 1 order by id desc limit 1";
            $res = Db::query($sql);
            if($res)
            {
                $auction_id = intval($res[0]['id']);
                $session_id = $res[0]['session_id'];
                $sql_detail = "select a.id from lkt_auction_product a,lkt_auction_session b,lkt_auction_special c,lkt_product_list d,lkt_freight e where a.session_id = b.id and b.special_id = c.id and a.recovery = b.recovery and a.recovery = c.recovery and a.recovery = d.recycle and d.id = a.goods_id and d.freight = e.id and a.id = '$auction_id' ";
                $res_detail = Db::query($sql_detail);
                if($res_detail)
                {
                    $param = "specialId=" . $session_id . "&acId=" . $auction_id . "&language=" . $language;
                }
            }
        }
        else if($type == 'flashsale')
        {
            $sql = "select id from lkt_flashsale_activity where store_id = '$store_id' and goods_id = '$goods_id' and is_delete = 0 and isshow = 1 order by id desc limit 1";
            $res = Db::query($sql);
            if($res)
            {
                $flashsale_id = intval($res[0]['id']);
                $param = "toback=true&type=FS&pro_id=" . $goods_id . "&fsid=" . $flashsale_id . "&mch_id=" . $mch_id . "&language=" . $language;
            }
        }
        else if($type == 'integral')
        {
            $sql = "select id,goods_id,integral,num from lkt_integral_goods where store_id = '$store_id' and goods_id = '$goods_id' and is_delete = 0 and status = 2 order by id desc limit 1";
            $res = Db::query($sql);
            if($res)
            {
                $integral_id = intval($res[0]['id']);
                $integral_goods_id = intval($res[0]['goods_id']);
                $integral = $res[0]['integral'];
                $integral_num = $res[0]['num'];
                $param = "goodsId=" . $integral_goods_id . "&pro_id=" . $integral_id . "&integral=" . $integral . "&num=" . $integral_num . "&language=" . $language;
            }
        }
        else if($type == 'member')
        {
            $sql = "select id from lkt_member_pro where store_id = '$store_id' and pro_id = '$goods_id' and recovery = 0 order by id desc limit 1";
            $res = Db::query($sql);
            if($res)
            {
                $param = "is_hy=true&pro_id=" . $goods_id . "&language=" . $language;
            }
        }

        if($param == '')
        {
            return null;
        }

        return array(
            'type' => $type,
            'name' => $name,
            'param' => $param
        );
    }

    // 获取商品营销活动映射
    private function buildMarketingActivityMap($store_id, $goods_id, $language, $mch_id = 0)
    {
        $store_id = (int)$store_id;
        $goods_id = (int)$goods_id;
        $mch_id = (int)$mch_id;
        $language = $language ? addslashes($language) : 'zh_CN';

        $activity_map = array();

        $sql_distribution = "select id from lkt_distribution_goods where store_id = '$store_id' and p_id = '$goods_id' and recycle = 0 order by id desc limit 1";
        $res_distribution = Db::query($sql_distribution);
        if ($res_distribution)
        {
            $fx_id = (int)$res_distribution[0]['id'];
            $activity_map['distribution'] = "?isDistribution=true&toback=true&pro_id=" . $goods_id . "&fx_id=" . $fx_id . "&language=" . $language;
        }

        $sql_seconds = "select id from lkt_seconds_activity where store_id = '$store_id' and goodsId = '$goods_id' and is_delete = 0 and isshow = 1 and status != 3 order by id asc limit 1";
        $res_seconds = Db::query($sql_seconds);
        if ($res_seconds)
        {
            $sec_id = (int)$res_seconds[0]['id'];
            $activity_map['seconds'] = "pro_id=" . $goods_id . "&navType=1&id=" . $sec_id . "&type=MS&language=" . $language;
        }

        $sql_group = "select activity_id from lkt_group_goods where goods_id = '$goods_id' and recycle = 0 order by id desc limit 1";
        $res_group = Db::query($sql_group);
        if ($res_group)
        {
            $activity_id = $res_group[0]['activity_id'];
            $activity_map['go_group'] = "pro_id=" . $goods_id . "&goodsId=" . $goods_id . "&acId=" . $activity_id . "&type=MS&language=" . $language;
        }

        $sql_presell = "select id from lkt_pre_sell_goods where product_id = '$goods_id' and is_delete = 0 and is_display = 1 limit 1";
        $res_presell = Db::query($sql_presell);
        if ($res_presell)
        {
            $activity_map['presell'] = "toback=true&pro_id=" . $goods_id . "&language=" . $language;
        }

        $sql_grass = "select id from lkt_bbs_post where store_id = '$store_id' and recycle = 0 and is_hide = 0 and status = 2 and FIND_IN_SET('$goods_id', pro_ids) limit 1";
        $res_grass = Db::query($sql_grass);
        if ($res_grass)
        {
            $zc_id = (int)$res_grass[0]['id'];
            $activity_map['zc'] = "id=" . $zc_id . "&language=" . $language;
        }

        $sql_auction = "select id,session_id from lkt_auction_product where goods_id = '$goods_id' and recovery = 0 and status = 1 and is_show = 1 order by id desc limit 1";
        $res_auction = Db::query($sql_auction);
        if ($res_auction)
        {
            $auction_id = (int)$res_auction[0]['id'];
            $session_id = $res_auction[0]['session_id'];
            $sql_auction_detail = "select a.id from lkt_auction_product a,lkt_auction_session b,lkt_auction_special c,lkt_product_list d,lkt_freight e where a.session_id = b.id and b.special_id = c.id and a.recovery = b.recovery and a.recovery = c.recovery and a.recovery = d.recycle and d.id = a.goods_id and d.freight = e.id and a.id = '$auction_id' ";
            $res_auction_detail = Db::query($sql_auction_detail);
            if ($res_auction_detail)
            {
                $activity_map['auction'] = "specialId=" . $session_id . "&acId=" . $auction_id . "&language=" . $language;
            }
        }

        $sql_flashsale = "select id from lkt_flashsale_activity where store_id = '$store_id' and goods_id = '$goods_id' and is_delete = 0 and isshow = 1 order by id desc limit 1";
        $res_flashsale = Db::query($sql_flashsale);
        if ($res_flashsale)
        {
            $flashsale_id = (int)$res_flashsale[0]['id'];
            $activity_map['flashsale'] = "toback=true&type=FS&pro_id=" . $goods_id . "&fsid=" . $flashsale_id . "&mch_id=" . $mch_id . "&language=" . $language;
        }

        $sql_integral = "select id,goods_id,integral,num from lkt_integral_goods where store_id = '$store_id' and goods_id = '$goods_id' and is_delete = 0 and status = 2 order by id desc limit 1";
        $res_integral = Db::query($sql_integral);
        if ($res_integral)
        {
            $integral_id = (int)$res_integral[0]['id'];
            $integral_goods_id = (int)$res_integral[0]['goods_id'];
            $integral = $res_integral[0]['integral'];
            $integral_num = $res_integral[0]['num'];
            $activity_map['integral'] = "goodsId=" . $integral_goods_id . "&pro_id=" . $integral_id . "&integral=" . $integral . "&num=" . $integral_num . "&language=" . $language;
        }

        $sql_member = "select id from lkt_member_pro where store_id = '$store_id' and pro_id = '$goods_id' and recovery = 0 order by id desc limit 1";
        $res_member = Db::query($sql_member);
        if ($res_member)
        {
            $activity_map['member'] = "is_hy=true&pro_id=" . $goods_id . "&language=" . $language;
        }

        return $activity_map;
    }

    // 获取最高优先级营销活动参数（与java逻辑保持一致）
    private function getTopMarketingParams($store_id, $goods_id, $language, $mch_id = 0)
    {
        $activity_map = $this->buildMarketingActivityMap($store_id, $goods_id, $language, $mch_id);
        if (empty($activity_map))
        {
            return null;
        }

        $store_id = (int)$store_id;
        $sql_plugins = "select plugin_code,plugin_name from lkt_plugins where store_id = '$store_id' and flag = 0 order by plugin_sort desc,id asc";
        $plugins = Db::query($sql_plugins);
        if (!$plugins)
        {
            return null;
        }

        $excludeTypes = array('coupon','living','mch','wallet','sign','sigin','diy');
        $top_plugin = null;
        foreach ($plugins as $plugin)
        {
            $plugin_code = isset($plugin['plugin_code']) ? $plugin['plugin_code'] : '';
            if($plugin_code == '')
            {
                continue;
            }
            if(in_array($plugin_code, $excludeTypes))
            {
                continue;
            }
            $top_plugin = $plugin;
            break;
        }

        if (!$top_plugin)
        {
            return null;
        }

        $activityCode = $top_plugin['plugin_code'] == 'advertising' ? 'zc' : $top_plugin['plugin_code'];
        if (!isset($activity_map[$activityCode]) || $activity_map[$activityCode] == '')
        {
            return null;
        }

        return array(
            'type' => $activityCode,
            'name' => $top_plugin['plugin_name'],
            'param' => $activity_map[$activityCode]
        );
    }

    // 加载更多商品
    public function get_more()
    {
        $store_id = addslashes(Request::param('store_id')); // 商城ID
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权ID
        $language = addslashes(Request::param('language')); // 语言
        $cid = addslashes(Request::param('cid')); // 分类ID
        $page = addslashes(Request::param('page')); // 加载次数

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $lang_code = Tools::get_lang($language);

        $Member_discount = array('store_id' => $store_id, 'access_id' => $access_id);
        $grade_list = PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade']; // 会员等级ID
        $grade_rate = $grade_list['grade_rate']; // 折扣值

        if (!$page)
        {
            $page = 1;
        }
        $start = ($page - 1) * 10;
        $end = 10;

        // 获取商品设置
        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));

        $query_content = "a.id,a.commodity_type,a.product_title,a.cover_map,a.imgurl,a.volume,a.s_type,a.num,a.status,a.brand_id,c.id as cid,min(c.price) over (partition by c.pid) as price,c.yprice,row_number () over (partition by c.pid) as top,m.name as mch_name,m.logo,a.write_off_settings";
        $queryCriteria = " a.store_id = '$store_id' and a.commodity_type in (0,1) and a.mch_status = 2 and a.recycle = 0 and a.active = 1 and c.recycle = 0 and a.is_presell = 0 and a.product_class like '%-$cid-%' and a.mch_id != 0 and a.show_adr like '%,1,%' and a.lang_code = '$lang_code' $status ";

        $z_num = 0;
        $sql_p = "select count(a.id) as num,c.pid from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where $queryCriteria group by c.pid  ";
        $r_p = Db::query($sql_p);
        if($r_p)
        {
            $z_num = count($r_p);
        }

        $sql_t = "select tt.* from (select $query_content from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where $queryCriteria order by a.sort desc,a.upper_shelf_time desc) as tt where tt.top<2 LIMIT $start,$end";
        $r_t = Db::query($sql_t);
        $productArray = array();
        if ($r_t != '')
        {
            if ($z_num >= $end)
            {
                foreach ($r_t as $k => $v)
                {
                    $s_type = explode(',', trim($v['s_type'],','));
                    $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                    $v['s_type_list'] = $s_type_list;
                    if ($v['volume'] < 0)
                    {
                        $v['volume'] = 0;
                    }

                    $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id);
                    $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                    $v['cover_map'] = ServerPath::getimgpath($v['cover_map'], $store_id);

                    // $v['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['price']));
                    // $v['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['yprice']));
                    $v['price'] = round($v['price'],2);
                    $v['yprice'] = round($v['yprice'],2);
                    $v['vip_yprice'] = $v['price'];
                    $v['vip_price'] = round($v['price'] * $grade_rate, 2);
                    $v['contNum'] = $v['num'];
                    $v['classId'] = $cid;

                    $pid = $v['id'];
                    $v['payPeople'] = 0;
                    $sql_3 = "select count(DISTINCT r_sNo) as total from lkt_order_details where r_status != 0 and p_id = '$pid' ";
                    $r_3 = Db::query($sql_3);
                    if($r_3)
                    {
                        $v['payPeople'] = $r_3[0]['total'];
                    }
                    if($v['write_off_settings'] == 1)
                    { // 核销设置 1.线下核销 2.无需核销
                        $v['isAddCar'] = 2; // 2.不能加入购物车
                    }
                    else
                    {
                        $v['isAddCar'] = 1; // 1.能加入购物车
                    }

                    $pid = $v['id'];
                    $sql_3 = "select count(DISTINCT r_sNo) as total from lkt_order_details where r_status != 0 and p_id = '$pid' ";
                    $r_3 = Db::query($sql_3);
                    if($r_3)
                    {
                        $v['volume'] = $r_3[0]['total'];
                    }
                    $productArray[] = $v;
                }

                $message = Lang('Success');
                return output(200,$message, $productArray);
            }
            else
            {
                $message = Lang('Success');
                return output(200,$message, $productArray);
            }
        }
        else
        {
            $message = Lang('Unknown_error');
            return output(101,$message);
        }
    }

    // 分销
    public function distribution_list()
    {
        $store_id = addslashes(Request::param('store_id')); // 商城ID
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权ID
        $language = addslashes(Request::param('language')); // 语言
        $limit_num = addslashes(Request::param('limit_num')); // 语言
        $country_num = addslashes(Request::param('country_num')); // 语言

        $user_id = '';
        if (!empty($access_id))
        { // 存在
            $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组

            // 查询用户ID
            $r0 = UserModel::where('access_id', $access_id)->select()->toArray();
            if ($r0)
            {
                $user_id = $r0[0]['user_id'];
            }
        }

        $distribution_list = [
            'ad_image' => '',
            'list' => [],
            'title'=>'fx'
        ];

        $res_dc = DistributionConfigModel::where(['store_id'=>$store_id,'advertising'=>1])->field('ad_image')->select()->toArray();
        if($res_dc)
        {
            $distribution_list['ad_image'] = $res_dc[0]['ad_image'];
        }


        $array_get_distribution_level = array('store_id'=>$store_id,'user_id'=>$user_id);
        $get_distribution_level_list = Distribution::get_distribution_level($array_get_distribution_level);
        $level = $get_distribution_level_list['level']; // 分销等级
        $discount = $get_distribution_level_list['discount']; // 折扣
        $direct_m_type = $get_distribution_level_list['direct_m_type']; // 直推分销比例发放模式 0.百分比 1.固定金额
        $direct_m = $get_distribution_level_list['direct_m']; // 直推分销奖

        $sql_dis = "select tt.* from (select a.*,b.product_title,b.imgurl,c.price,c.costprice,c.min_inventory,c.attribute,c.num,row_number () over (PARTITION BY a.p_id) AS top from lkt_distribution_goods as a left join lkt_product_list as b on a.p_id = b.id left join lkt_configure as c on a.s_id = c.id where a.store_id = '$store_id' and b.commodity_type in (0,1) and b.status = 2 and c.recycle = 0 and a.uplevel = 0 and a.recycle = 0 and b.mch_id != 0 order by a.add_time desc) as tt where tt.top<2 limit $limit_num";
        $res_dis = Db::query($sql_dis);
        if($res_dis)
        {
            foreach ($res_dis as $k => $v)
            {
                $res_dis[$k]['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id); // 商品图
                $price = $v['price'];

                $array_get_distribution = array('store_id'=>$store_id,'user_id'=>$user_id,'price'=>$price,'distribution_rule'=>$v['distribution_rule'],'rules_set'=>$v['rules_set'],'level'=>$level,'discount'=>$discount,'direct_m_type'=>$direct_m_type,'direct_m'=>$direct_m);
                $get_distribution_list = Distribution::get_distribution($array_get_distribution);
                $fx_price = $get_distribution_list['fx_price']; // 分享赚
                $discount = $get_distribution_list['discount']; // 折扣

                $res_dis[$k]['fx_price'] = $fx_price;
                $res_dis[$k]['goodsPrice'] = (float)$v['price'];
                $res_dis[$k]['price'] = round($v['price'] * $discount * 0.01,2);
                $res_dis[$k]['costprice'] = (float)$v['costprice'];
                $res_dis[$k]['pv'] = (float)$v['pv'];
            }
            $distribution_list['list'] = $res_dis;
        }

        $data = array('distribution_list' => $distribution_list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 优选店铺
    public function getMchList()
    {
        $store_id = addslashes(Request::param('store_id')); // 商城ID
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权ID
        $language = addslashes(Request::param('language')); // 语言
        $limit_num = addslashes(Request::param('limit_num')); // 语言
        $country_num = addslashes(Request::param('country_num')); // 语言

        $user_id = '';
        if (!empty($access_id))
        { // 存在
            $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组

            // 查询用户ID
            $r0 = UserModel::where('access_id', $access_id)->select()->toArray();
            if ($r0)
            {
                $user_id = $r0[0]['user_id'];
            }
        }

        $list_mch = Tools::get_store($store_id, '', '', 1, $limit_num); //根据经纬度获取店铺
        if($list_mch != array())
        {
            foreach($list_mch as $k_mch => $v_mch)
            {
                $shop_id = $v_mch['shop_id'];

                $list_mch[$k_mch]['collection_status'] = false;
                $sql_collection = "select id from lkt_user_collection where store_id = '$store_id' and mch_id = '$shop_id' and user_id = '$user_id' ";
                $r_collection = Db::query($sql_collection);
                if($r_collection)
                {
                    $list_mch[$k_mch]['collection_status'] = true;
                }
            }
        }

        $data = array('r_mch' => $list_mch);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 引导图
    public function guided_graph()
    {
        $store_id = addslashes(trim($_GET['store_id']));
        if(isset($_GET['store_type']))
        {
            $store_type = addslashes(trim($_GET['store_type']));
        }
        else
        {
            if(isset($_POST['store_type']))
            {
                $store_type = addslashes(trim($_POST['store_type']));
            }
        }

        $guidedGraphArray = array();
        // 查询引导图
        $r = GuideModel::where(['store_id'=>$store_id,'type'=>1])->limit(3)->order('sort', 'asc')->order('add_date', 'desc')->select()->toArray();
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $v['image'] = ServerPath::getimgpath($v['image'], $store_id);
                $guidedGraphArray[] = $v;
            }
        }
        $message = Lang('Success');
        return output(200,$message, $guidedGraphArray);
    }

    // 新品上市
    public function new_arrival()
    {
        $store_id = addslashes(Request::param('store_id')); // 商城ID
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权ID
        $language = addslashes(Request::param('language')); // 语言
        $page = addslashes(Request::param('page')); // 加载次数

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $lang_code = Tools::get_lang($language);

        if (!$page)
        {
            $page = 1;
        }
        // 获取会员
        $Member_discount = array('store_id' => $store_id, 'access_id' => $access_id);
        $grade_list = PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade']; // 会员等级ID
        $grade_rate = $grade_list['grade_rate']; // 折扣值
        $isGrade = false;
        if($grade_rate != 1)
        {
            $isGrade = true;
        }
        $start = ($page - 1) * 10;
        $end = 10;

        // 获取商品设置
        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));

        $r_sp_type = ProLabelModel::where(['store_id'=>$store_id,'name'=>'新品'])->field('id')->select()->toArray();
        $label_id = $r_sp_type[0]['id'];

        $productArray = array();

        $query_content = "a.id,a.commodity_type,a.product_title,a.subtitle,a.product_class,a.imgurl,a.volume,a.s_type,a.num,a.status,a.brand_id,a.keyword,a.freight,a.mch_id,c.id as cid,min(c.price) over (partition by c.pid) as price,c.yprice,c.img,c.pid,c.attribute,row_number () over (partition by c.pid) as top,m.id as mchId,m.name as mch_name,m.logo,m.is_open,a.write_off_settings";
        $query_criteria = "a.store_id = '$store_id' and a.commodity_type in (0,1) and a.recycle = 0 and a.active = 1 and a.s_type like '%,$label_id,%' and a.mch_status = 2 and a.is_presell = 0 and a.mch_id != 0 and a.lang_code = '$lang_code' $status ";
        
        $sql = "select tt.* from (select $query_content from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where $query_criteria order by a.sort desc,a.search_num desc,a.add_date desc) as tt where tt.top<2 LIMIT $start,$end ";
        $r = Db::query($sql);
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);/* end 保存*/

                if ($v['volume'] < 0)
                {
                    $v['volume'] = 0;
                }
                
                $s_type = explode(',', trim($v['s_type'],','));
                $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                $v['s_type_list'] = $s_type_list;

                // $v['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['price']));
                // $v['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['yprice']));
                $v['price'] = round($v['price'],2);
                $v['yprice'] = round($v['yprice'],2);
                $v['vip_yprice'] = $v['price'];
                $v['vip_price'] = round($v['price'] * $grade_rate, 2);
                $v['price_yh'] = $v['price'];
                $v['name'] = $v['product_title'];
                $v['size'] = $v['cid'];
                $v['sizeid'] = $v['cid'];
                $v['stockNum'] = $v['num'];
                if($v['write_off_settings'] == 1)
                { // 核销设置 1.线下核销 2.无需核销
                    $v['isAddCar'] = 2; // 2.不能加入购物车
                }
                else
                {
                    $v['isAddCar'] = 1; // 1.能加入购物车
                }

                $pid = $v['id'];
                $sql_3 = "select count(DISTINCT r_sNo) as total from lkt_order_details where r_status != 0 and p_id = '$pid' ";
                $r_3 = Db::query($sql_3);
                if($r_3)
                {
                    $v['volume'] = $r_3[0]['total'];
                }

                $productArray[$k] = $v;
            }
        }
        $data = array('list' => $productArray, 'grade' => $grade,'isGrade'=>$isGrade);

        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取地址位置
    public function get_location()
    {
        $store_id = addslashes(trim($_GET['store_id']));
        $GroupID = addslashes(trim($_POST['GroupID'])); // 

        $xianArray = array();
        $list = array();
        $list0 = array();
        $r0 = AdminCgGroupModel::where('district_level',3)->select()->toArray();
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                if ($v['district_name'] == '市辖区')
                {
                    $G_ParentID = $v['district_pid'];
                    $r1 = AdminCgGroupModel::where('id',$G_ParentID)->field('district_name')->select()->toArray();
                    if ($r1)
                    {
                        $v['district_name'] = $r1[0]['district_name'] . $v['district_name'];
                    }
                }
                $list0[] = $v;
            }

            foreach ($list0 as $k1 => $v1)
            {
                $class = new pinyin();
                $first0 = $class->str2py($v1['district_name']);
                $list[ucfirst(substr($first0, 0, 1))][] = $v1;
            }

            ksort($list);
        }

        if ($GroupID)
        {
            $xianArray = AdminCgGroupModel::where('district_pid',$GroupID)->select()->toArray();
        }

        $data = array('list' => $list, 'xian' => $xianArray);

        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 好物优选
    public function recommend()
    {
        $store_id = addslashes(Request::param('store_id')); // 商城ID
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权ID
        $language = addslashes(Request::param('language')); // 语言
        $page = addslashes(Request::param('page')); // 加载次数

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $lang_code = Tools::get_lang($language);

        if (!$page)
        {
            $page = 1;
        }
        $start = ($page - 1) * 10;
        $end = 10;

        $r_sp_type = ProLabelModel::where(['store_id'=>$store_id,'name'=>'推荐'])->field('id')->select()->toArray();
        $label_id = $r_sp_type[0]['id'];

        // 获取会员
        $Member_discount = array('store_id' => $store_id, 'access_id' => $access_id);
        $grade_list = PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade']; // 会员等级ID
        $grade_rate = $grade_list['grade_rate']; // 折扣值
        $isGrade = false;
        if($grade_rate != 1)
        {
            $isGrade = true;
        }
        // 获取商品设置
        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));

        $productArray = array();

        $query_content = "a.id,a.commodity_type,a.product_title,a.subtitle,a.product_class,a.imgurl,a.volume,a.s_type,a.num,a.status,a.brand_id,a.keyword,a.freight,a.mch_id,c.id as cid,min(c.price) over (partition by c.pid) as price,c.yprice,c.img,c.pid,c.attribute,row_number () over (partition by c.pid) as top,m.id as mchId,m.name as mch_name,m.logo,m.is_open,a.write_off_settings";
        $query_criteria = "a.store_id = '$store_id' and a.commodity_type in (0,1) and a.recycle = 0 and a.active = 1 and a.s_type like '%,$label_id,%' and a.mch_status = 2 and a.is_presell = 0 and a.mch_id != 0 and a.lang_code = '$lang_code' $status ";

        $sql = "select tt.* from (select $query_content from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where $query_criteria order by a.sort desc,a.search_num desc,a.add_date desc) as tt where tt.top<2 LIMIT $start,$end ";
        $r = Db::query($sql);
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                if ($v['volume'] < 0)
                {
                    $v['volume'] = 0;
                }
                
                $s_type = explode(',', trim($v['s_type'],','));
                $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                $v['s_type_list'] = $s_type_list;

                // $v['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['price']));
                // $v['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['yprice']));
                $v['price'] = round($v['price'],2);
                $v['yprice'] = round($v['yprice'],2);
                $v['vip_yprice'] = $v['price'];
                $v['vip_price'] = round($v['price'] * $grade_rate, 2);
                $v['price_yh'] = $v['price'];
                $v['name'] = $v['product_title'];
                $v['size'] = $v['cid'];
                $v['sizeid'] = $v['cid'];
                $v['stockNum'] = $v['num'];
                if($v['write_off_settings'] == 1)
                { // 核销设置 1.线下核销 2.无需核销
                    $v['isAddCar'] = 2; // 2.不能加入购物车
                }
                else
                {
                    $v['isAddCar'] = 1; // 1.能加入购物车
                }
                $productArray[$k] = $v;
            }
        }
        $data = array('list' => $productArray, 'grade' => $grade,'isGrade'=>$isGrade);

        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取店铺分类
    public function mchClass()
    {
        $store_id = addslashes(trim($_GET['store_id']));

        $list = array();
        $total = 0;

        $r = MchClassModel::where(['store_id'=>$store_id,'recycle'=>0,'is_display'=>1])->order('sort','desc')->select()->toArray();
        if($r)
        {
            foreach($r as $k => $v)
            {
                $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
                $list[] = $v;
            }
        }

        $total = count($list);

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        return output(200,$message, $data);
    }
    
    // 推荐门店
    public function recommend_stores()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $longitude = trim(Request::param('longitude')); // 经度
        $latitude = trim(Request::param('latitude')); // 纬度

        $page = trim(Request::param('page')) ? trim(Request::param('page')) : trim(Request::param('pageNo')); // 页面
        $pagesize = trim(Request::param('pageSize')) ? trim(Request::param('pageSize')) : 10; // 页面
        $cid = '';
        if(isset($_POST['cid']))
        {
            $cid = addslashes(trim($_POST['cid'])); // 店铺分类ID
        }

        // 获取商品设置
        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>2));

        $list_mch = Tools::get_store($store_id, $longitude, $latitude, $page, $pagesize,$cid); //根据经纬度获取店铺

        if (count($list_mch) > 0)
        {
            foreach ($list_mch as $k => $v)
            {
                $quantity_sold = 0; // 已售数量
                $shop_id = $v['shop_id'];

                $res = PC_Tools::StoreData($store_id,$shop_id,$status);

                $list_mch[$k]['collection_num'] = $res['collection_num'];
                $list_mch[$k]['quantity_on_sale'] = $res['quantity_on_sale'];
                $list_mch[$k]['quantity_sold'] = $res['quantity_sold'];
            }
        }
        
        $data = array('list'=>$list_mch);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 积分商城
    public function integral($store_id)
    {
        $list = array();
        // 查询商品信息
        $sql = "select b.id,b.store_id,b.goods_id,b.integral,b.money,b.num,b.max_num,b.sort,b.is_delete,b.add_time,b.update_time,a.id as goodsId,a.imgurl,a.product_title,a.subtitle,a.s_type,a.volume,a.status as STATUS,a.initial from lkt_integral_goods as b left join lkt_product_list as a on b.goods_id=a.id where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.recycle=0 and b.is_delete = 0 and a.mch_id != 0 order by b.sort desc limit 0,3";
        //店铺未授权时只查自营店数据
        $plugin = Db::name('plugins')->where(['store_id'=>$store_id,'plugin_code'=>'integral'])->value('status');
        if ($plugin == 0)
        {
            $shop_id = Db::name('admin')->where(['store_id'=>$store_id,'recycle'=>0,'type'=>1])->value('shop_id');
            $sql = "select b.id,b.store_id,b.goods_id,b.integral,b.money,b.num,b.max_num,b.sort,b.is_delete,b.add_time,b.update_time,a.id as goodsId,a.imgurl,a.product_title,a.subtitle,a.s_type,a.volume,a.status as STATUS,a.initial from lkt_integral_goods as b left join lkt_product_list as a on b.goods_id=a.id where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.recycle=0 and b.is_delete = 0 and a.mch_id = '$shop_id' and exists(select 1 from lkt_integral_config where status=1 and mch_id = a.mch_id) order by b.sort desc limit 0,3";
        }
        $goods = Db::query($sql);
        if($goods)
        {
            foreach ($goods as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);// 商品图
                $v['sales'] = $v['volume'] > 0 ? $v['volume'] : 0;// 销量
                $list[] = $v;
            }
        }

        return $list;
    }

    // 获取首页商品数据
    public function classList()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $language = addslashes(Request::param('language')); // 语言

        $shop_id = addslashes(Request::param('shop_id')); // 语言

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $lang_code = Tools::get_lang($language);

        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));
        // 获取会员折扣
        $Member_discount = array('store_id' => $store_id, 'access_id' => $access_id);
        $grade_list = PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade']; // 会员等级ID
        $grade_rate = $grade_list['grade_rate']; // 折扣值

        $productArray = array(); // 商品数组
        //查询商品并分类显示返回JSON至小程序
        $r_c = ProductClassModel::where(['store_id'=>$store_id,'is_display'=>1,'recycle'=>0,'sid'=>0,'examine'=>1,'lang_code'=>$lang_code,'notset'=>0])->field('cid,pname,english_name')->order('sort', 'desc')->select()->toArray();
        if ($r_c)
        {
            foreach ($r_c as $key => $value)
            {
                $cid = $value['cid'];

                $query_content = "a.id,a.product_title,a.cover_map,a.imgurl,a.volume,a.s_type,a.num,a.status,a.brand_id,c.id as cid,min(c.price) over (partition by c.pid) as price,c.yprice,row_number () over (partition by c.pid) as top,m.name as mch_name,m.logo";
                $query_criteria = "a.store_id = '$store_id' and a.commodity_type in (0,1) and a.mch_status = 2 and a.recycle = 0 and a.active = 1 and c.recycle = 0 and a.is_presell = 0 and a.product_class like '%-$cid-%' and a.mch_id != 0 and a.lang_code = '$lang_code' $status ";
                if($shop_id != '')
                {
                    $query_criteria .= " and a.mch_id = '$shop_id' ";
                }

                $sql_2 = "select tt.* from (select $query_content from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where $query_criteria order by a.class_sort desc,a.upper_shelf_time desc) as tt where tt.top<2 LIMIT 10";
                $r_2 = Db::query($sql_2);
                if ($r_2)
                {
                    foreach ($r_2 as $k => $v)
                    {
                        $s_type = explode(',', trim($v['s_type'],','));
                        $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                        $r_2[$k]['s_type_list'] = $s_type_list;
                        if ($v['volume'] < 0)
                        {
                            $r_2[$k]['volume'] = 0;
                        }

                        $r_2[$k]['logo'] = ServerPath::getimgpath($v['logo'], $store_id);
                        $r_2[$k]['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                        $r_2[$k]['cover_map'] = ServerPath::getimgpath($v['cover_map'], $store_id);

                        // $v['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['price']));
                        // $v['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['yprice']));
                        $r_2[$k]['price'] = round($v['price'],2);
                        $r_2[$k]['yprice'] = round($v['yprice'],2);
                        $r_2[$k]['vip_yprice'] = round($v['price'],2);
                        $r_2[$k]['vip_price'] = round($v['price'] * $grade_rate, 2);
                        $r_2[$k]['contNum'] = $v['num'];
                        $r_2[$k]['classId'] = $cid;
                    }
                }
                $r_c[$key]['list'] = $r_2;
            }
            $productArray = $r_c;
        }
        
        $data = array('list'=>$productArray);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 更改语言
    public function select_language()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $language = trim(Request::param('language')); // 授权id

        $lang = Tools::get_lang($language);

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

    // 获取用户能否看见插件入口
    public function pluginStatus()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $language = trim(Request::param('language')); // 授权id
        $mch_id = trim(Request::param('mchId')); // 店铺ID
        // 插件
        $Plugin = new Plugin();
        $Plugin_arr = $Plugin->Get_plugin_entry($store_id);
        
        if($Plugin_arr['coupon'] == 1 && $mch_id != '')
        {
            $sql_mch = "select is_open_coupon from lkt_mch where store_id = '$store_id' and id = '$mch_id' and recovery = 0 ";
            $r_mch = Db::query($sql_mch);
            if($r_mch)
            {
                $Plugin_arr['coupon'] = $r_mch[0]['is_open_coupon']; // 是否开启店铺主页领卷人口 1.开启 0.关闭
            }
        }

        $data = array('plugin' => $Plugin_arr);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 获取维护公告
    public function getUserTell()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id

        $user_id = '';
        // 查询用户ID
        $r0 = UserModel::where('access_id', $access_id)->select()->toArray();
        if ($r0)
        {
            $user_id = $r0[0]['user_id'];
        }

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'read_id'=>$user_id);
        $data = PC_Tools::GetAnnouncement($array);
        // $data = PC_Tools::Obtain_maintenance_announcements($store_type);
        // if($data['info'] == '')
        // {
            $code = '200';
        // }
        // else
        // {
        //     $code = '4003';
        // }
        $message = Lang("Success");
        return output($code,$message,$data);
    }

    // 公告已读
    public function markToRead()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $tell_id = trim(Request::param('tell_id')); // 公告ID

        $sql0 = "select user_id from lkt_user where store_id = '$store_id' and access_id = '$access_id'";
        $r0 = Db::query($sql0);
        if ($r0)
        {
            $user_id = $r0[0]['user_id'];
        }
        
        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'read_id'=>$user_id,'tell_id'=>$tell_id);
        PC_Tools::markToRead($array);

        $message = Lang("Success");
        return output(200,$message);
    }

    // 更改用户默认币种
    public function changeCurrency()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id

        $currency_id = trim(Request::param('currency_id')); // 币种ID

        $sql0 = "select id from lkt_user where store_id = '$store_id' and access_id = '$access_id'";
        $r0 = Db::query($sql0);
        if ($r0)
        {
            $sql1 = "update lkt_user set preferred_currency = '$currency_id' where store_id = '$store_id' and access_id = '$access_id' ";
            $r1 = Db::execute($sql1);

            cache($access_id . '_currency',$currency_id); // 修改缓存
        }

        $message = Lang("Success");
        return output(200,$message);
    }
}
