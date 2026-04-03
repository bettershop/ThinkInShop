<?php
namespace app\admin\controller\mch\App;

use app\BaseController;
use think\facade\Db;

use app\common\Plugin\CouponPublicMethod;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\PC_Tools;
use app\common\Tools;
use app\common\Plugin\Plugin;
use app\common\Plugin\MchPublicMethod;
use app\common\FreightPublicMethod;
use app\common\Product;
use app\common\Plugin\PluginUtils;
use app\common\DeliveryHelper;
use app\common\EditOrderStatus;
use app\common\Plugin\RefundUtils;
use app\common\LKTConfigInfo;
use app\common\OSSCommon;
use app\common\MinIOServer;
use app\common\ExpressPublicMethod;

use app\admin\model\AdminModel;
use app\admin\model\MchModel;
use app\admin\model\MchConfigModel;
use app\admin\model\MchBrowseModel;
use app\admin\model\MchStoreModel;
use app\admin\model\MchPromiseModel;
use app\admin\model\PluginsModel;
use app\admin\model\AgreementModel;

use app\admin\model\ConfigModel;
use app\admin\model\UserModel;
use app\admin\model\ProductClassModel;
use app\admin\model\BrandClassModel;
use app\admin\model\FreightModel;
use app\admin\model\ProductNumberModel;
use app\admin\model\ProLabelModel;
use app\admin\model\ProductListModel;
use app\admin\model\ProductImgModel;
use app\admin\model\ConfigureModel;
use app\admin\model\UserCollectionModel;
use app\admin\model\BannerModel;
use app\admin\model\MchAccountLogModel;
use app\admin\model\CashierRecordModel;
use app\admin\model\OrderModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\ExpressModel;
use app\admin\model\ReturnGoodsModel;
use app\admin\model\SystemMessageModel;
use app\admin\model\RecordModel;
use app\admin\model\DiyModel;

class Mch extends BaseController
{
    // 我的店铺
    public function Index()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id

        $mch_data = array();

        $user_id = $this->user_list['user_id'];

        $shop_id = 0; // 店铺ID
        $account_money = 0;
        $collection_num = 0;
        $name = '';
        $shop_information = '';
        $logo = '';
        $posterImg = '';
        $headImg = '';
        $review_result = '';
        $roomid = 0;
        $is_lock = 0;

        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1])->field('shop_id')->select()->toArray();
        $admin_shop_id = $r_admin[0]['shop_id'];

        $auto_examine = 0;
        $sql_config = "select auto_examine from lkt_mch_config where mch_id = 0 ";
        $r_config = Db::query($sql_config);
        if($r_config)
        {
            $auto_examine = $r_config[0]['auto_examine'];
        }

        $collection_code = 0;
        // 根据商城ID、用户ID、店铺审核状态通过，查询是否有店铺
        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0,'is_lock'=>0])->select()->toArray();
        if ($r0)
        {
            $shop_id = $r0[0]['id'];
            $account_money = $r0[0]['account_money'];
            $collection_num = $r0[0]['collection_num'];
            if($r0[0]['collection_code'] != '')
            {
                $collection_code = ServerPath::getimgpath($r0[0]['collection_code'], $store_id);
            }

            $name = $r0[0]['name'];
            $shop_information = $r0[0]['shop_information'];
            $logo = ServerPath::getimgpath($r0[0]['logo'], $store_id);
            $headImg = ServerPath::getimgpath($r0[0]['head_img'], $store_id);
            $review_result = $r0[0]['review_result'];
            $roomid = $r0[0]['roomid'];
            $is_lock = $r0[0]['is_lock'];
            $status = 2; // 审核通过
        }
        else
        {
            $r1 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->limit(1)->order('add_time','desc')->select()->toArray();
            if ($r1)
            {
                $shop_id = $r1[0]['id'];
                $account_money = $r1[0]['account_money'];
                $collection_num = $r1[0]['collection_num'];
                $collection_code = ServerPath::getimgpath($r1[0]['collection_code'], $store_id);;
                $name = $r1[0]['name'];
                $shop_information = $r1[0]['shop_information'];
                $logo = ServerPath::getimgpath($r1[0]['logo'], $store_id);
                $review_result = $r1[0]['review_result'];
                $roomid = $r1[0]['roomid'];
                $is_lock = $r1[0]['is_lock'];
                
                if ($r1[0]['review_status'] == 0)
                {
                    $status = 1; // 审核中
                }
                else if ($r1[0]['review_status'] == 2)
                {
                    $status = 3; // 审核不通过
                }
                else
                {
                    if($is_lock == 1)
                    {
                        $message = Lang('mch.86');
                        return output('50080', $message);
                    }
                }
            }
            else
            {
                $status = 0; // 没有申请
            }
        }
        $time = date("Y-m-d H:i:s");
        $time1 = date("Y-m-d 00:00:00");
        $time2 = date("Y-m-d 23:59:59");
        $mch_data['mch_status'] = 0;

        // $is_Payment = false;
        if($status == 0)
        {
            $data = array('mch_data'=>$mch_data,'status'=>$status);
        }
        else
        {
            $mch_data['shop_id'] = $shop_id;
            $mch_data['account_money'] = $account_money;
            $mch_data['collection_code'] = $collection_code;
            $mch_data['name'] = $name;
            $mch_data['shop_information'] = $shop_information;
            $mch_data['logo'] = $logo;
            $mch_data['headImg'] = $headImg;
            $mch_data['review_result'] = $review_result;
            $mch_data['roomid'] = $roomid;
            
            $array = array('store_id'=>$store_id,'shop_id'=>$shop_id);
            $mch = new MchPublicMethod();
            $mch_array = $mch->is_margin($array);
            $mch_data['commodity_setup'] = $mch_array['commodity_setup'];
            $mch_data['isPromiseSwitch'] = $mch_array['isPromiseSwitch'];
            $mch_data['isPromiseExamine'] = $mch_array['isPromiseExamine'];
            $mch_data['isPromisePay'] = $mch_array['isPromisePay'];
            $is_Payment = $mch_array['is_Payment'];
            
            if ($shop_id != 0)
            {
                $sql_0 = "select id from lkt_order where store_id = '$store_id' and mch_id like '%,$shop_id,%' and add_time >= '$time1' and add_time <= '$time2' and status != 7 and supplier_id = 0 ";
                $r_0 = Db::query($sql_0);
                $mch_data['order_num'] = is_array($r_0)?count($r_0):0; // 今日订单

                $sql_1 = "select id from lkt_order as a where a.store_id = '$store_id' and a.mch_id like '%,$shop_id,%' and ((a.status = 1 and a.self_lifting = '0') or (a.status = 2 and a.self_lifting = '1')) and (a.otype = 'GM' or a.otype = 'VI') and a.supplier_id = 0";
                $r_1 = Db::query($sql_1);
                $mch_data['order_num1'] = count($r_1); // 待发货订单

                $sql_2 = "select tt.* from (select d.id,d.sNo,d.pid,a.p_name,a.p_price,a.num,a.size,a.sid,d.re_type,d.r_type,d.re_time,b.imgurl,b.is_distribution,c.img,row_number () over (partition by d.sNo) as top from lkt_return_order as d left join lkt_order_details as a on d.p_id = a.id left join lkt_product_list as b on a.p_id = b.id left join lkt_configure as c on a.sid = c.id where a.store_id = '$store_id' and b.mch_id = '$shop_id' and d.r_type in (0,1)) as tt where tt.top<2 ";
                $r_2 = Db::query($sql_2);
                $mch_data['order_num2'] = count($r_2); // 售后订单

                $res1 = array();
                $res1_1 = array();
                $r_3 = MchBrowseModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->field('user_id,token')->select()->toArray();
                if ($r_3)
                {
                    foreach ($r_3 as $k => $v)
                    {
                        $token = $v['token'];
                        $res1[$token][] = $v;
                    }
                    foreach ($res1 as $k => $v)
                    {
                        foreach ($v as $ke => $va)
                        {
                            $val = $va['user_id'];
                            $res1_1[$val][] = $va;
                        }
                    }
                }
                $mch_data['visitor_num'] = count($res1_1); // 访客数

                $sr = 0; // 今日收入
                $js = 0;
                $sql_4 = "select * from lkt_mch_account_log where store_id = '$store_id' and addtime >= '$time1' and addtime <= '$time2' and mch_id = '$shop_id' and type in (1,2)";
                $r_4 = Db::query($sql_4);
                if ($r_4)
                {
                    foreach ($r_4 as $k => $v)
                    {
                        if ($v['status'] == 1)
                        {
                            $sr += $v['price'];
                        }
                        else
                        {
                            $js += $v['price'];
                        }
                    }
                }
                $income = number_format($sr - $js,2);
                if ($income < 0)
                {
                    $mch_data['income'] = 0;
                }
                else
                {
                    $mch_data['income'] = $income;
                }

                $r_5 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->select()->toArray();
                if($r_5)
                {
                    $mch_data['mch_status'] = 1;
                }

                $mch_data['goodsUpNum'] = 0;
                $r_7 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'status'=>2,'recycle'=>0,'gongyingshang'=>0])->whereIn('commodity_type','0,1')->field('count(id) as total')->select()->toArray();
                if($r_7)
                {
                    $mch_data['goodsUpNum'] = $r_7[0]['total'];
                }

                $sql_m = "update lkt_mch set last_login_time = '$time' where store_id = '$store_id' and id = '$shop_id'";
                $r_m = Db::execute($sql_m);
            }

            $coupon_status = false;
            if (file_exists('../app/common/Plugin/CouponPublicMethod.php'))
            {
                $coupon = new CouponPublicMethod();
                $coupon_list = $coupon->test($store_id,$shop_id);
                if ($coupon_list == 1)
                {
                    $coupon_status = true;
                }
            }

            $pa_flag = false;
            // 获取平台活动管理插件安装状态
            $res_pg = PluginsModel::where(['plugin_code'=>'platform_activities','status'=>1,'flag'=>0])->select()->toArray();
            if($res_pg)
            {
                $pa_flag = true;
            }

            $zx = true;
            if($admin_shop_id == $shop_id)
            {
                $zx = false;
            }

            $noread = 0;
            $sql1 = "select count(id) as num from lkt_message_logging where store_id = '$store_id' and mch_id = '$shop_id' and read_or_not = 0 and supplier_id = 0 and type in (1,2,3,4,5,6,9,15,16,17,22,23,30) ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $noread = $r1[0]['num'];
            }

            $data = array('mch_data'=>$mch_data,'status'=>$status,'coupon_status'=>$coupon_status,'pa_flag'=>$pa_flag,'zx'=>$zx,'noread'=>$noread,'is_lock'=>$is_lock,'auto_examine'=>$auto_examine,'is_Payment'=>$is_Payment);
        }
        $message = Lang('Success');
        return output(200, $message, $data);
    }

    // 入驻协议
    public function Agreement()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));

        $name = Lang('mch.11');
        $agreement = '';

        $r = AgreementModel::where(['store_id'=>$store_id,'type'=>1])->field('name,content')->select()->toArray();
        if ($r)
        {
            $name = $r[0]['name'];
            $agreement = $r[0]['content'];
        }
        $data = array('agreement' => $agreement,'content' => $agreement, 'name' => $name);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 验证店铺名称
    public function Verify_store_name()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $name = trim($this->request->param('name')); // 店铺名称

        $user_id = $this->user_list['user_id'];

        require('../app/common/shop_name.php');

        foreach ($shop_name as $key => $val)
        {
            if (strstr($name, $val) !== false)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '店铺名称不合法！';
                $this->mchLog($Log_content);
                $message = Lang('mch.0');
                return output(225, $message);
            }
        }

        $r = MchModel::where(['store_id'=>$store_id,'name'=>$name,'recovery'=>0])->where('user_id','<>',$user_id)->field('id')->select()->toArray();
        if ($r)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺名称已存在！';
            $this->mchLog($Log_content);
            $message = Lang('mch.1');
            return output(223, $message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '验证店铺名称成功！';
            $this->mchLog($Log_content);
            $message = Lang('Success');
            return output(200, $message);
        }
    }

    // 申请开通店铺
    public function Apply()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id

        // $head_img = trim($this->request->param('storeImg')); // 店铺头像
        // $logo = trim($this->request->param('logo')); // LOGO
        // $poster_img = trim($this->request->param('posterImg')); // 店铺新增宣传图
        $name = trim($this->request->param('name')); // 店铺名称
        $shop_information = trim($this->request->param('shop_information')); // 店铺信息
        $cid = trim($this->request->param('cid')); // 店铺分类
        $shop_range = trim($this->request->param('shop_range')); // 经营范围
        $realname = trim($this->request->param('realname')); // 真实姓名
        $ID_number = trim($this->request->param('ID_number')); // 身份证号码
        $cpc = trim($this->request->param('cpc')); // 区号
        $tel = trim($this->request->param('tel')); // 联系电话
        $city_all = trim($this->request->param('city_all')); // 联系地址
        $address = trim($this->request->param('address')); // 联系地址
        $shop_nature = trim($this->request->param('shop_nature')); // 店铺性质
        $imgUrls = trim($this->request->param('imgUrls')); // 身份证证件照

        $time = date("Y-m-d H:i:s");
        $user_id = $this->user_list['user_id'];
        if ($store_type = 1)
        {
            $store_type = '0';
        }
        elseif ($store_type = 2)
        {
            $store_type = 'app';
        }
        
        require('../app/common/shop_name.php');

        foreach ($shop_name as $key => $val)
        {
            if (strstr($name, $val) !== false)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '申请开通店铺时，店铺名称不合法！';
                $this->mchLog($Log_content);
                $message = Lang('mch.0');
                return output(225, $message);
            }
        }

        $r = MchModel::where(['store_id'=>$store_id,'name'=>$name,'recovery'=>0])->where('user_id', '<>',$user_id)->field('id')->select()->toArray();
        if ($r)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '申请开通店铺时，店铺名称已存在！';
            $this->mchLog($Log_content);
            $message = Lang('mch.1');
            return output(223, $message);
        }

        if($cid == '' || $cid == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '申请开通店铺时，请选择店铺分类！';
            $this->mchLog($Log_content);
            $message = Lang('mch.75');
            return output(225, $message);
        }

        $res = $this->is_idcard($ID_number);
        if (!$res)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '申请开通店铺时，身份证号码错误！';
            $this->mchLog($Log_content);
            $message = Lang('mch.2');
            return output(225, $message);
        }

        $business_license = '';
        if($imgUrls != '')
        {
            $imgUrls_list = explode(',',$imgUrls);
            foreach($imgUrls_list as $k => $v)
            {
                $business_license .= preg_replace('/.*\//', '', $v) . ',';
            }
            $business_license = trim($business_license,',');
        }
        $id = 0;
        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id,business_license')->select()->toArray();
        if ($r0)
        {
            $id = $r0[0]['id'];
        }

        $city_list = explode('-',$city_all);
        $sheng = $city_list[0];
        $shi = $city_list[1];
        $xian = $city_list[2];
        if($address == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员'.$user_id.'申请店铺时，详细地址不能为空！';
            $this->mchLog($Log_content);
            $message = Lang('mch.5');
            return output(109, $message);
        }

        $array_address = array('cpc'=>$cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'code'=>'');
        $address_xx = PC_Tools::address_translation($array_address);

        $Longitude_and_latitude = Tools::get_Longitude_and_latitude( $store_id,$address_xx);
        $longitude = $Longitude_and_latitude['longitude'];
        $latitude = $Longitude_and_latitude['latitude'];
        if ($id == 0)
        {
            $r = MchConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->field('logo,head_img,poster_img')->select()->toArray();
            $head_img = ServerPath::getimgpath($r[0]['head_img'], $store_id);
            $logo = ServerPath::getimgpath($r[0]['logo'], $store_id);
            $poster_img = ServerPath::getimgpath($r[0]['poster_img'], $store_id);

            $data = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'shop_information'=>$shop_information,'shop_range'=>$shop_range,'realname'=>$realname,'ID_number'=>$ID_number,'tel'=>$tel,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'shop_nature'=>$shop_nature,'business_license'=>$business_license,'add_time'=>$time,'longitude'=>$longitude,'latitude'=>$latitude,'cid'=>$cid,'head_img'=>$head_img,'logo'=>$logo,'poster_img'=>$poster_img);
            $res_data = Db::name('mch')->insertGetId($data);
            if ($res_data > 0)
            {
                $message = Lang('Success');
                return output(200, $message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '申请开通店铺失败！参数:'. json_encode($data);
                $this->mchLog($Log_content);
                $message = Lang('mch.6');
                return output(224, $message);
            }
        }
        else
        {
            $data_update0 = array('store_id'=>$store_id,'id'=>$id);
            $data_update = array('review_status'=>0,'name'=>$name,'shop_information'=>$shop_information,'shop_range'=>$shop_range,'realname'=>$realname,'ID_number'=>$ID_number,'tel'=>$tel,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'shop_nature'=>$shop_nature,'business_license'=>$business_license,'add_time'=>$time,'longitude'=>$longitude,'latitude'=>$latitude,'cid'=>$cid);
            $res_data = Db::name('mch')->where($data_update0)->update($data_update);
            if($res_data)
            {
                $message = Lang('Success');
                return output(200, $message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '继续申请开通店铺失败！参数:' . json_encode($data_update0);
                $this->mchLog($Log_content);
                $message = Lang('mch.6');
                return output(224, $message);
            }
        }
    }

    // 继续开通店铺
    public function Continue_apply()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $user_id = $this->user_list['user_id'];

        $business_license = "";
        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->limit(1)->order('add_time','desc')->select()->toArray();
        if ($r0)
        {
            $r0[0]['logo'] = ServerPath::getimgpath($r0[0]['logo'], $store_id); //图片
            $r0[0]['storeImg'] = ServerPath::getimgpath($r0[0]['head_img'], $store_id); //图片
            $r0[0]['posterImg'] = ServerPath::getimgpath($r0[0]['poster_img'], $store_id); //图片
            $business_license_list = explode(',',$r0[0]['business_license']);
            foreach ($business_license_list as $k => $v)
            {
                $business_license .= ServerPath::getimgpath($v, $store_id) . ','; //图片
            }

            $business_license = trim($business_license,',');
            $r0[0]['business_license'] = $business_license; //图片
            $r0[0]['id_number'] = $r0[0]['ID_number'];
            $r0[0]['account_money'] = round($r0[0]['account_money'],2);
            $r0[0]['cashable_money'] = round($r0[0]['cashable_money'],2);

            $data = array('list' => $r0[0]);
            $message = Lang('Success');
            return output(200, $message,$data);
        }
        else
        {
            $message = Lang('Illegal invasion');
            return output(115, $message);
        }
    }

    // 运费列表
    public function Freight_list()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        
        $fid = addslashes(trim($this->request->param('fid'))); // 运费ID
        $page = trim($this->request->param('page')); // 页码
        $pagesize = trim($this->request->param('pagesize')); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $user_id = $this->user_list['user_id'];

        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $data = array('store_id'=>$store_id,'fid'=>0,'mch_id'=>$shop_id,'status'=>'','type'=>'','name'=>'','lang_code'=>$lang_code,'pageNo'=>$page,'pageSize'=>$pagesize);

        $freight = new FreightPublicMethod();
        $freight_list = $freight->freight_list($data);

        $data = array('total'=>$freight_list['total'],'list'=>$freight_list['list']);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 获取省
    public function Get_sheng()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $user_id = $this->user_list['user_id'];

        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $freight = new FreightPublicMethod();
        $freight_list = $freight->get_sheng();
        $message = Lang('Success');
        return output(200, $message,$freight_list);
    }

    // 添加运费
    public function Freight_add()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $country_num = addslashes(trim($this->request->param('country_num'))); // 所属国家
        $name = trim($this->request->param('name')); // 名称
        $type = trim($this->request->param('type')); // 类型 0:件 1:重量
        $default_freight = $this->request->param('defaultFreight'); // 默认运费
        $hidden_freight = trim($this->request->param('hidden_freight')); // 指定运费
        $is_package_settings = trim($this->request->param('is_package_settings')); // 是否是包邮设置 0.未开启 1.开启
        $package_settings = trim($this->request->param('package_settings')); // 包邮设置
        $is_no_delivery = trim($this->request->param('is_no_delivery')); // 是否不配送 0.未开启 1.开启
        $no_delivery = trim($this->request->param('no_delivery')); // 不配送地区
        $is_default = trim($this->request->param('is_default')); // 类型 0:不默认 1:默认

        $hidden_freight = htmlspecialchars_decode($hidden_freight);
        $no_delivery = htmlspecialchars_decode($no_delivery);

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $data = array('store_id'=>$store_id,'admin_name'=>$zhanghao,'mch_id'=>$shop_id,'fid'=>0,'lang_code'=>$lang_code,'country_num'=>$country_num,'name'=>$name,'type'=>$type,'default_freight'=>$default_freight,'hidden_freight'=>$hidden_freight,'no_delivery'=>$no_delivery,'is_default'=>$is_default);
        $freight = new FreightPublicMethod();
        $freight_list = $freight->preserve($data);
        return;
    }

    // 编辑运费-页面
    public function Freight_modify_show()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $id = trim($this->request->param('id')); // 运费ID

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $data = array('store_id'=>$store_id,'fid'=>$id,'mch_id'=>$shop_id);

        $freight = new FreightPublicMethod();
        $freight_list = $freight->edit_page($data);
        
        $message = Lang('Success');
        return output(200, $message,$freight_list);
    }

    // 编辑运费
    public function Freight_modify()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = trim($this->request->param('id')); // 运费ID
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $country_num = addslashes(trim($this->request->param('country_num'))); // 所属国家
        $name = trim($this->request->param('name')); // 名称
        $type = trim($this->request->param('type')); // 类型 0:件 1:重量
        $default_freight = $this->request->param('defaultFreight'); // 默认运费
        $hidden_freight = trim($this->request->param('hidden_freight')); // 指定运费
        $is_package_settings = trim($this->request->param('is_package_settings')); // 是否是包邮设置 0.未开启 1.开启
        $package_settings = trim($this->request->param('package_settings')); // 包邮设置
        $is_no_delivery = trim($this->request->param('is_no_delivery')); // 是否不配送 0.未开启 1.开启
        $no_delivery = trim($this->request->param('no_delivery')); // 不配送地区
        $is_default = trim($this->request->param('is_default')); // 类型 0:不默认 1:默认

        $hidden_freight = htmlspecialchars_decode($hidden_freight);
        $no_delivery = htmlspecialchars_decode($no_delivery);
        
        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $data = array('store_id'=>$store_id,'admin_name'=>$zhanghao,'mch_id'=>$shop_id,'fid'=>$id,'lang_code'=>$lang_code,'country_num'=>$country_num,'name'=>$name,'type'=>$type,'default_freight'=>$default_freight,'hidden_freight'=>$hidden_freight,'no_delivery'=>$no_delivery,'is_default'=>$is_default);
        $freight = new FreightPublicMethod();
        $freight_list = $freight->preserve($data);
        return;
    }

    // 设置默认运费
    public function Set_default()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $id = trim($this->request->param('id')); // 运费ID

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $data = array('store_id'=>$store_id,'admin_name'=>$zhanghao,'mch_id'=>$shop_id,'id'=>$id);
        $freight = new FreightPublicMethod();
        $freight_list = $freight->set_default($data);
        return;
    }

    // 删除运费
    public function Freight_del()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $id = trim($this->request->param('id')); // 运费ID
        
        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        $id = rtrim($id, ','); // 去掉最后一个逗号

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $data = array('store_id'=>$store_id,'admin_name'=>$zhanghao,'mch_id'=>$shop_id,'id'=>$id);
        $freight = new FreightPublicMethod();
        $freight_list = $freight->freight_del($data);
        return;
    }

    // 自选商品-添加商品页面
    public function add_goods_page()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $lang_code = Tools::get_lang($lang_code);

        $sql0 = "select b.shop_id from lkt_customer as a left join lkt_admin as b on a.admin_id = b.id where b.store_id = '$store_id'";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $mch_id = $r0[0]['shop_id'];
        }

        //获取产品类别
        $r1 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0,'examine'=>1,'lang_code'=>$lang_code,'notset'=>0])->field('cid,pname')->select()->toArray();
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $cid1 = $v['cid'];
                //循环第一层
                $r2 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$cid1,'examine'=>1,'lang_code'=>$lang_code,'notset'=>0])->field('cid,pname')->select()->toArray();
                if($r2)
                {
                    foreach ($r2 as $ke => $va)
                    {
                        $r1[$k]['res'][] = $va;
                    }
                }
            }
        }

        $product_class_list = $r1;

        $list = array();
        //产品品牌
        $brand_class_list = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0,'status'=>0])->field('brand_id,brand_name')->select()->toArray();
        if($mch_id == $shop_id)
        {
            $list = array();
        }
        else
        {
            $Tools = new Tools($store_id, $store_type);
            $res = $Tools->query_product($store_id,$shop_id);

            $from_res = "a.id,a.product_title,a.subtitle,a.product_class,a.imgurl,a.volume,a.s_type,a.num as stockNum,a.status,a.brand_id,a.keyword,a.freight,a.mch_id,a.gongyingshang,c.id as cid,c.pid,min(c.price) over (partition by c.pid) as price,c.yprice,c.img,c.num,c.attribute,row_number () over (partition by c.pid) as top";

            if($res == '')
            {
                $sql4 = "select tt.* from (select $from_res from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.commodity_type = 0 and a.status = 2 and a.mch_status = 2 and a.recycle = 0 and a.is_presell = 0 and a.active = 1 and a.is_zixuan = 1 and a.mch_id = '$mch_id' order by a.sort desc,a.add_date DESC) as tt where tt.top<2 LIMIT 0,10";
            }
            else
            {
                $sql4 = "select tt.* from (select $from_res from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.commodity_type = 0 and a.status = 2 and a.mch_status = 2 and a.recycle = 0 and a.is_presell = 0 and a.active = 1 and a.is_zixuan = 1 and a.mch_id = '$mch_id' $res order by a.sort desc,a.add_date DESC) as tt where tt.top<2 LIMIT 0,10";
            }
            $r4 = Db::query($sql4);
            if($r4)
            {
                foreach($r4 as $k => $v)
                {
                    if($v['volume'] < 0)
                    {
                        $r4[$k]['volume'] = 0;
                    }
                    $v['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id);

                    $v['price'] = round($v['price'],2);
                    $v['yprice'] = round($v['yprice'],2);
                    $v['sizeid'] = $v['cid'];
                    $v['mchId'] = $v['mch_id'];
                    $v['logo'] = '';
                    $v['mch_name'] = '';
                    $v['is_open'] = 0;

                    $sql5 = "select logo,name,is_open from lkt_mch where id = '$mch_id' ";
                    $r5 = Db::query($sql5);
                    if($r5)
                    {
                        $v['logo'] = $r5[0]['logo'];
                        $v['mch_name'] = $r5[0]['name'];
                        $v['is_open'] = $r5[0]['is_open'];
                    }
                    $list[] = $v;
                }
            }
        }

        $freight_status = false;
        $r_freight = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'is_default'=>1])->field('id')->select()->toArray();
        if($r_freight)
        {
            $freight_status = true;
        }
        $data = array('product_class_list'=>$product_class_list,'brand_class_list'=>$brand_class_list,'list'=>$list,'freight_status'=>$freight_status);
        $message = Lang('Success');
        return output('200', $message,$data);
    }

    // 自选商品-添加商品页面-加载更多
    public function add_goods_page_load()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        
        $product_class_id = trim($this->request->param('product_class_id')); // 产品一级分类ID
        $product_class_id1 = trim($this->request->param('product_class_id1')); // 产品二级分类ID
        $brand_id = trim($this->request->param('brand_id')); // 产品品牌ID
        $proName = trim($this->request->param('proName')); // 产品名称
        $page = trim($this->request->param('page')); // 页码

        $start = ($page - 1)*10;
        $end = 10;
        $list = array();

        $sql0 = "select b.shop_id from lkt_customer as a left join lkt_admin as b on a.admin_id = b.id where store_id = '$store_id'";
        $r0 = Db::query($sql0);
        $mch_id = $r0[0]['shop_id'];
        if($mch_id == $shop_id)
        {
            $list = array();
        }
        else
        {
            $Tools = new Tools($store_id, $store_type);
            $res = $Tools->query_product($store_id,$shop_id);

            $from_res = "a.id,a.product_title,a.subtitle,a.product_class,a.imgurl,a.volume,a.s_type,a.num as stockNum,a.status,a.brand_id,a.keyword,a.freight,a.mch_id,a.gongyingshang,c.id as cid,c.pid,min(c.price) over (partition by c.pid) as price,c.yprice,c.img,c.num,c.attribute,row_number () over (partition by c.pid) as top";

            if($res == '')
            {
                $condition = "a.store_id = '$store_id' and a.commodity_type = 0 and a.status = 2 and a.recycle = 0 and a.mch_status = 2 and a.mch_id = '$mch_id' and a.active = 1 and a.is_zixuan = 1 ";
            }
            else
            {
                $condition = "a.store_id = '$store_id' and a.commodity_type = 0 and a.status = 2 and a.recycle = 0 and a.mch_status = 2 and a.mch_id = '$mch_id' and a.active = 1 and a.is_zixuan = 1 $res ";
            }
            if ($product_class_id != 0 && $product_class_id != '') 
            {
                if ($product_class_id1 != 0 && $product_class_id1 != '') 
                {
                    $condition .= " and a.product_class like '%-$product_class_id1-%' ";
                }
                else
                {
                    $condition .= " and a.product_class like '%-$product_class_id-%' ";
                }
            }
            if ($brand_id != 0 && $brand_id != '') 
            {
                $condition .= " and a.brand_id like '$brand_id' ";
            }
            if ($proName != '') 
            {
                $proName_0 = Tools::FuzzyQueryConcatenation($proName);
                $condition .= " and a.product_title like $proName_0 ";
            }
            $sql1 = "select tt.* from (select $from_res from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where $condition order by a.sort desc,a.add_date DESC) as tt where tt.top<2 LIMIT $start,$end";
            $r1 = Db::query($sql1);
            if($r1)
            {
                foreach($r1 as $k => $v)
                {
                    if($v['volume'] < 0)
                    {
                        $v['volume'] = 0;
                    }
                    $v['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id);

                    $v['price'] = round($v['price'],2);
                    $v['yprice'] = round($v['yprice'],2);
                    $v['sizeid'] = $v['cid'];
                    $v['mchId'] = $v['mch_id'];
                    $v['logo'] = '';
                    $v['mch_name'] = '';
                    $v['is_open'] = 0;

                    $sql5 = "select logo,name,is_open from lkt_mch where id = '$mch_id' ";
                    $r5 = Db::query($sql5);
                    if($r5)
                    {
                        $v['logo'] = $r5[0]['logo'];
                        $v['mch_name'] = $r5[0]['name'];
                        $v['is_open'] = $r5[0]['is_open'];
                    }
                    $list[] = $v;
                }
            }
        }
        $data = array('list'=>$list);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 自选商品-添加商品页面-查询
    public function goods_query()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $product_class_id = trim($this->request->param('product_class_id')); // 产品一级分类ID
        $product_class_id1 = trim($this->request->param('product_class_id1')); // 产品二级分类ID
        $brand_id = trim($this->request->param('brand_id')); // 产品品牌ID
        $proName = trim($this->request->param('proName')); // 产品名称

        $list = array();
        $brand_class_list = array();

        $sql0 = "select b.shop_id from lkt_customer as a left join lkt_admin as b on a.admin_id = b.id where store_id = '$store_id'";
        $r0 = Db::query($sql0);
        $mch_id = $r0[0]['shop_id'];
        if($mch_id == $shop_id)
        {
            $list = array();
        }
        else
        {
            $Tools = new Tools($store_id, $store_type);
            $res = $Tools->query_product($store_id,$shop_id);

            if($res == '')
            {
                $condition = "a.store_id = '$store_id' and a.commodity_type = 0 and a.status = 2 and a.recycle = 0 and a.mch_status = 2 and a.mch_id = '$mch_id' and a.active = 1 and a.is_zixuan = 1 and a.is_presell = 0 ";
            }
            else
            {
                $condition = "a.store_id = '$store_id' and a.commodity_type = 0 and a.status = 2 and a.recycle = 0 and a.mch_status = 2 and a.mch_id = '$mch_id' and a.active = 1 and a.is_zixuan = 1 and a.is_presell = 0 $res ";
            }

            if ($product_class_id != 0 && $product_class_id != '') 
            {
                if ($product_class_id1 != 0 && $product_class_id1 != '') 
                {
                    $condition .= " and a.product_class like '%-$product_class_id1-%' ";
                }
                else
                {
                    $condition .= " and a.product_class like '%-$product_class_id-%' ";
                }
            }

            if ($brand_id != 0 && $brand_id != '') 
            {
                $condition .= " and a.brand_id like '$brand_id' ";
            }
            if ($proName != '') 
            {
                $proName_0 = Tools::FuzzyQueryConcatenation($proName);
                $condition .= " and a.product_title like '%$proName%' ";
            }

            $from_res = "a.id,a.product_title,a.subtitle,a.product_class,a.imgurl,a.volume,a.s_type,a.num as stockNum,a.status,a.brand_id,a.keyword,a.freight,a.mch_id,a.gongyingshang,c.id as cid,c.pid,min(c.price) over (partition by c.pid) as price,c.yprice,c.img,c.num,c.attribute,row_number () over (partition by c.pid) as top";

            $sql1 = "select tt.* from (select $from_res from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where $condition  order by a.sort desc,a.add_date DESC) as tt where tt.top<2 LIMIT 0,10";
            $r1 = Db::query($sql1);
            if($r1)
            {
                foreach($r1 as $k => $v)
                {
                    if($v['volume'] < 0)
                    {
                        $v['volume'] = 0;
                    }
                    $v['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id);

                    $v['price'] = round($v['price'],2);
                    $v['yprice'] = round($v['yprice'],2);
                    $v['sizeid'] = $v['cid'];
                    $v['mchId'] = $v['mch_id'];
                    $v['logo'] = '';
                    $v['mch_name'] = '';
                    $v['is_open'] = 0;

                    $sql5 = "select logo,name,is_open from lkt_mch where id = '$mch_id' ";
                    $r5 = Db::query($sql5);
                    if($r5)
                    {
                        $v['logo'] = $r5[0]['logo'];
                        $v['mch_name'] = $r5[0]['name'];
                        $v['is_open'] = $r5[0]['is_open'];
                    }
                    $list[] = $v;
                }
            }
        }

        if($product_class_id != '')
        {
            $product_class_id_ = ',' . $product_class_id . ',';
            $sql_b = "select brand_id,brand_name from lkt_brand_class where store_id = '$store_id' and recycle = 0 and status = 0 and categories like '%$product_class_id_%' ";
        }
        else
        {
            $sql_b = "select brand_id,brand_name from lkt_brand_class where store_id = '$store_id' and recycle = 0 and status = 0 ";
        }
        $r_b = Db::query($sql_b);
        if($r_b)
        {
            foreach($r_b as $k_b => $v_b)
            {
                $brand_class_list[] = $v_b;
            }
        }

        $data = array('list'=>$list,'brand_class_list'=>$brand_class_list);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 自选商品-添加商品页面-商品详情
    public function see()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $p_id = trim($this->request->param('p_id')); // 产品id

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $product = new product();
        $list = $product->edit_page($store_id,$zhanghao,$shop_id,$p_id,'店铺');
        $list = json_decode($list,true);

        $data = array('plugin_list'=>$list['Plugin_arr'],'attr'=>$list['attr_group_list'],'brand_list'=>$list['brand_class'],'brand_class_list1'=>$list['brand_class_list1'],'attrList'=>$list['checked_attr_list'],'content'=>$list['content'],'cover_map'=>$list['cover_map'],'product_class_list1'=>$list['ctypes'],'freight_list'=>$list['freight_list'],'freight_list1'=>$list['freight_list1'],'imgurls'=>$list['imgurls'],'initial'=>$list['initial'],'list'=>$list['list'],'richList'=>$list['richList'],'show_adr'=>$list['show_adr'],'s_type'=>$list['sp_type'],'status'=>$list['status'],'unit'=>$list['unit'],'distributors'=>$list['distributors'],'distributors1'=>$list['distributors1']);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 自选商品-添加商品
    public function add_goods()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $pro_id = trim($this->request->param('pro_id')); // 产品ID字符串
        $freight_id = trim($this->request->param('freight_id')); // 运费ID

        $user_id = $this->user_list['user_id'];

        $time = date("Y-m-d H:i:s");

        $arr = explode(',',$pro_id);

        //进入正式添加---开启事物
        Db::startTrans();

        if($freight_id == '' || $freight_id == 0)
        {
            $message = Lang('mch.7');
            return output(109, $message);
        }
        $r3 = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'id'=>$freight_id])->field('id')->select()->toArray();
        if(!$r3)
        {
            $message = Lang('Parameter error');
            return output(109, $message);
        }

        $r01 = MchModel::where(['id'=>$shop_id,'recovery'=>0])->field('name')->select()->toArray();
        if($r01)
        {
            $name = $r01[0]['name'];
        }

        foreach ($arr as $k => $v)
        {
            // 查询商品信息
            $data1 = array('store_id'=>$store_id,'status'=>2,'mch_status'=>2,'recycle'=>0,'active'=>1,'id'=>$v);
            $r1 = ProductListModel::where($data1)->select()->toArray();
            if($r1)
            {
                $min_inventory = $r1[0]['min_inventory'];
                // 复制商品信息
                $data2 = array('store_id'=>$store_id,'product_number'=>'','commodity_type'=>$r1[0]['commodity_type'],'product_title'=>$r1[0]['product_title'],'subtitle'=>$r1[0]['subtitle'],'scan'=>$r1[0]['scan'],'product_class'=>$r1[0]['product_class'],'brand_id'=>$r1[0]['brand_id'],'keyword'=>$r1[0]['keyword'],'weight'=>$r1[0]['weight'],'weight_unit'=>$r1[0]['weight_unit'],'imgurl'=>$r1[0]['imgurl'],'sort'=>$r1[0]['sort'],'content'=>$r1[0]['content'],'richList'=>$r1[0]['richList'],'num'=>$r1[0]['num'],'min_inventory'=>$r1[0]['min_inventory'],'status'=>2,'upper_shelf_time'=>$time,'s_type'=>$r1[0]['s_type'],'add_date'=>$time,'is_distribution'=>$r1[0]['is_distribution'],'distributor_id'=>$r1[0]['distributor_id'],'freight'=>$freight_id,'active'=>$r1[0]['active'],'mch_id'=>$shop_id,'mch_status'=>2,'show_adr'=>$r1[0]['show_adr'],'initial'=>$r1[0]['initial'],'publisher'=>$name,'is_zixuan'=>0,'cover_map'=>$r1[0]['cover_map'],'video'=>$r1[0]['video'],'pro_video'=>$r1[0]['pro_video'],'is_presell'=>0);
                $r2 = Db::name('product_list')->insertGetId($data2);
                if($r2)
                {
                    $commodity_str1 = unserialize($r1[0]['commodity_str']);
                    $commodity_str1[] = $r2;

                    $commodity_str = serialize($commodity_str1);
                    // 修改商品ID字符串
                    $r3 = Db::name('product_list')->where('id', $v)->update(['commodity_str' => $commodity_str]);
                    if($r3 == -1)
                    {
                        Db::rollback();
                        $message = Lang('Abnormal business');
                        return output(110, $message);
                    }
                    // 查询商品轮播图
                    $r4 = ProductImgModel::where(['product_id'=>$v])->field('product_url')->select()->toArray();
                    if($r4)
                    {
                        foreach ($r4 as $ke => $va)
                        {
                            $product_url = $va['product_url'];
                            // 添加新的商品轮播图
                            $data_img = array('product_url'=>$product_url,'product_id'=>$r2,'add_date'=>$time);
                            $r = Db::name('product_img')->insert($data_img);
                            if ($r < 1) 
                            {
                                Db::rollback();
                                $message = Lang('Abnormal business');
                                return output(110, $message);
                            }
                        }
                    }
                    // 查询商品属性
                    $r5 = ConfigureModel::where(['pid'=>$v,'recycle'=>0])->field('id,costprice,price,yprice,img,num,unit,status,attribute,min_inventory,attribute_str')->select()->toArray();
                    if($r5)
                    {
                        foreach ($r5 as $key => $val)
                        {
                            $attribute_id = $val['id'];
                            $attribute_str1 = unserialize($val['attribute_str']);

                            $data_attribute = array('costprice'=>$val['costprice'],'price'=>$val['price'],'yprice'=>$val['yprice'],'img'=>$val['img'],'unit'=>$val['unit'],'status'=>$val['status'],'attribute'=>$val['attribute'],'min_inventory'=>$val['min_inventory'],'pid'=>$r2,'total_num'=>$val['num'],'num'=>$val['num'],'ctime'=>$time,'attribute_str'=>'');
                            $r_attribute = Db::name('configure')->insertGetId($data_attribute);
                            if ($r_attribute < 1) 
                            {
                                Db::rollback();
                                $message = Lang('Abnormal business');
                                return output(110, $message);
                            }

                            $total_num = $val['num'];
                            $num = $val['num'];
                            // 在库存记录表里，添加一条入库信息
                            $content = $user_id . '增加商品总库存' . $num;

                            $data_stock0 = array('store_id'=>$store_id,'product_id'=>$r2,'attribute_id'=>$r_attribute,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>0,'add_date'=>$time,'content'=>$content);
                            $r_stock0 = Db::name('stock')->insertGetId($data_stock0);
                            if($r_stock0 < 1)
                            {
                                Db::rollback();
                                $message = Lang('mch.8');
                                return output(109, $message);
                            }

                            if ($min_inventory >= $num)
                            { // 当属性库存低于等于预警值
                                $content1 = '预警';
                                // 在库存记录表里，添加一条预警信息
                                $data_stock1 = array('store_id'=>$store_id,'product_id'=>$r2,'attribute_id'=>$r_attribute,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>2,'add_date'=>$time,'content'=>$content1);
                                $r_stock1 = Db::name('stock')->insertGetId($data_stock1);
                                if($r_stock1 < 1)
                                {
                                    Db::rollback();
                                    $message = Lang('mch.8');
                                    return output(109, $message);
                                }
                            }

                            $attribute_str1[] = $r_attribute;
                            $attribute_str = serialize($attribute_str1);

                            $r6 = Db::name('configure')->where(['pid'=>$v,'id'=>$attribute_id])->update(['attribute_str' => $attribute_str]);
                            if ($r6 == -1) 
                            {
                                Db::rollback();
                                $message = Lang('Abnormal business');
                                return output(110, $message);
                            }
                        }
                    }
                }
                else
                {
                    Db::rollback();
                    $message = Lang('Abnormal business');
                    return output(109, $message);
                }
            }
        }
        Db::commit();
        $message = Lang('Success');
        return output(200, $message);
    }

    // 上传商品页面
    public function Upload_merchandise_page()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $zhanghao = $this->user_list['zhanghao'];

        $product = new Product();
        $list = $product->add_page($store_id,$zhanghao,$shop_id,'店铺');
        $list = json_decode($list,true);
        $product_number = $list['product_number'];
        $Plugin_arr = array('active'=>$list['Plugin_arr']);
        $freight = $list['freight'];
        $sp_type = $list['sp_type'];
        $show_adr = $list['show_adr'];
        $unit = $list['unit'];
        $mchStoreModels = $list['mchStoreModels'];
        $haveStore = $list['haveStore'];

        $data = array('product_number' => $product_number, 'freight_list' => $freight, 'unit' => $unit, 'show_adr' => $show_adr, 's_type' => $sp_type, 'plugin_list' => $Plugin_arr,'mchStoreModels'=>$mchStoreModels,'haveStore'=>$haveStore);

        $message = Lang('Success');
        return output(200, $message,$data);
    }
    
    // 获取分类
    public function Get_class()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $class_str = trim($this->request->param('class_str')); // 分类ID
        $brand_id = trim($this->request->param('brand_str')); // 品牌ID
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $lang_code = Tools::get_lang($lang_code);

        $product = new Product();
        $list = $product->get_classified_brands($store_id,$class_str,$brand_id,$lang_code);
        $list = json_decode($list,true);

        $message = Lang('Success');
        return output(200, $message,$list);
    }

    // 选择分类
    public function Choice_class()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $cid = trim($this->request->param('cid')); // 分类ID
        $brand_id = trim($this->request->param('brand_str')); // 品牌ID

        $product = new Product();
        $list = $product->select_category($store_id,$cid,$brand_id);
        $list = json_decode($list,true);

        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 获取属性名
    public function Get_attribute_name()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $attribute_str = trim($this->request->param('attribute_str')); // 属性名字符串
        $attribute_str = htmlspecialchars_decode($attribute_str); // 将特殊的 HTML 实体转换回普通字符
        $attribute_str1 = explode(',', $attribute_str); // 转数组

        $attribute = Tools::attribute($store_id, $attribute_str1,$lang_code);

        $list = array('attribute' => $attribute);
        $message = Lang('Success');
        return output(200, $message,$list);
    }

    // 获取属性值
    public function Get_attribute_value()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $attribute_str = trim($this->request->param('attribute_str')); // 属性名字符串
        $attr_arr = trim($this->request->param('attr_arr')); // 属性字符串

        $attribute_str = htmlspecialchars_decode($attribute_str); // 将特殊的 HTML 实体转换回普通字符
        $attribute_str1 = explode(',', $attribute_str); // 转数组
        $attribute_str2 = $attribute_str1;
        $attr_arr = htmlspecialchars_decode($attr_arr); // 将特殊的 HTML 实体转换回普通字符

        $rew = array();
        $arr = array();
        $arr1 = array();
        $arr2 = array();
        if($attr_arr != '')
        {
            $attr_arr1 = json_decode($attr_arr); // json字符串转数组
            if (count($attr_arr1) > 0)
            {
                foreach ($attr_arr1 as $k => $v)
                {
                    foreach ($attribute_str2 as $ke => $va)
                    {
                        if ($v->attr_group_name == $va)
                        {
                            unset($attribute_str2[$ke]);
                        }
                    }
                    foreach ($v->attr_list as $k1 => $v1)
                    {
                        $arr1[] = array($v->attr_group_name, $v1->attr_name);
                    }
                }
                foreach ($attribute_str2 as $k => $v)
                {
                    $arr1[] = array($v, '');
                }

                // 将二维数组某一个字段相同的数组合并起来的方法
                foreach ($arr1 as $k => $v)
                {
                    $arr2[$v[0]][] = $v[1];
                }
                // 去重
                foreach ($arr2 as $k => $v)
                {
                    $arr3[$k] = array_unique($v);
                }
                foreach ($arr3 as $k => $v)
                {
                    if (in_array($k, $attribute_str1))
                    {
                        $arr[$k] = $v;
                    }
                }

                foreach ($arr as $k => $v)
                {
                    $rew[$k] = Tools::attribute_name($store_id, $k, $v);
                }
            }
            else
            {
                if (count($attribute_str1) != 0)
                {
                    foreach ($attribute_str1 as $k => $v)
                    {
                        $rew[$v] = Tools::attribute_name($store_id, $v);
                    }
                }
            }
        }
        else
        {
            if (count($attribute_str1) != 0)
            {
                foreach ($attribute_str1 as $k => $v)
                {
                    $rew[$v] = Tools::attribute_name( $store_id, $v);
                }
            }
        }

        $data = array('list' => $rew);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 上传商品页面返回
    public function Del()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $mch_id = trim($this->request->param('shop_id')); // 店铺ID

        $zhanghao = $this->user_list['zhanghao'];

        $r0 = ProductNumberModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'operation'=>$zhanghao])->limit(1)->order('id', 'desc')->field('id')->select()->toArray();
        if($r0)
        {
            $id = $r0[0]['id'];
            $sql = "update lkt_product_number set status = 2 where store_id = '$store_id' and mch_id = '$mch_id' and operation = '$zhanghao' and id = '$id' ";
            $r = Db::execute($sql);
            if ($r)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $zhanghao . '离开上传商品页面，删除商品编号成功！sql:'.$sql;
                $this->mchLog($Log_content);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $zhanghao . '离开上传商品页面，删除商品编号失败！sql:'.$sql;
                $this->mchLog($Log_content);
            }
        }
        
        $message = Lang('Success');
        return output(200, $message);
    }

    // 获取门店
    public function getWriteStore()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id

        $shop_id = trim($this->request->param('mchId')); // 店铺ID

        $array = array('store_id'=>$store_id,'mch_id'=>$shop_id);
        $data = MchPublicMethod::getMchStore($array);

        $message = Lang('Success');
        return output(200, $message,$data['list']);
    }

    // 上传商品
    public function Upload_merchandise()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        
        $p_id = addslashes(trim($this->request->param('p_id'))); // 商品ID
        $commodity_type = urldecode(addslashes(trim($this->request->param('commodity_type')))); // 商品类型 0.实物商品 1.虚拟商品
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
    	$country_num = addslashes(trim($this->request->param('country_num'))); // 所属国家
        $product_title = urldecode(addslashes(trim($this->request->param('product_title')))); // 产品标题
        $subtitle = urldecode(addslashes(trim($this->request->param('subtitle')))); // 小标题
        $keyword = urldecode(addslashes(trim($this->request->param('keyword')))); // 关键词
        $weight = addslashes(trim($this->request->param('weight'))); // 重量
        $weight_unit = 'kg'; // 重量单位
        $product_class = addslashes(trim($this->request->param('product_class_id'))); // 商品分类
        $brand_id = addslashes(trim($this->request->param('brand_id'))); // 品牌
        $cover_map = trim($this->request->param('cover_map')); // 产品封面图
        $showImg = trim($this->request->param('showImg')); // 商品图片
    	$video = $this->request->param('video'); // 展示视频

        $initial = urldecode(addslashes(trim($this->request->param('initial')))); // 初始值
        $min_inventory = urldecode(addslashes(trim($this->request->param('stockWarn')))); // 库存预警
        $unit = urldecode(addslashes(trim($this->request->param('unit')))); // 单位
        $attrList = trim($this->request->param('attr_arr')); // 属性
        $freight_id = trim($this->request->param('freight_id')); // 运费
        $s_type = trim($this->request->param('s_type')); // 显示类型
        $active = trim($this->request->param('active')); // 支持活动
        $show_adr = trim($this->request->param('display_position')); // 显示位置

    	$pro_video = $this->request->param('proVideo'); // 商品视频
        $content = $this->request->param('content'); // 产品内容
        $richList = $this->request->param('richList'); // 产品数组内容
        $volume = trim($this->request->param('volume')); // 虚拟销量
        $mch_status = addslashes(trim($this->request->param('mch_status'))); // 审核状态：1.待审核，2.审核通过，3.审核不通过，4.暂不审核

    	$write_off_settings = addslashes(trim($this->request->param('write_off_settings'))); // 核销设置 1.线下核销 2.无需核销
    	$write_off_mch_ids = addslashes(trim($this->request->param('write_off_mch_ids'))); // 核销门店id  0.全部门店,  1,2,3使用逗号分割
    	$is_appointment = addslashes(trim($this->request->param('is_appointment'))); // 预约时间设置 1.无需预约下单 2.需要预约下单

        $initial = htmlspecialchars_decode($initial); // 将特殊的 HTML 实体转换回普通字符
        $attrList = htmlspecialchars_decode($attrList);
        $attrList = json_decode($attrList, true);
        $content = htmlspecialchars_decode($content);
        $richList = htmlspecialchars_decode($richList);

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $admin_mch_id = $r_admin[0]['shop_id']; // 店铺id

        if($admin_mch_id == $shop_id)
        {
            $mch_status = 2;
        }

        $data = array('commodity_type'=>$commodity_type,'lang_code'=>$lang_code,'country_num'=>$country_num,'product_title'=>$product_title,'subtitle'=>$subtitle,'keyword'=>$keyword,'product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'cover_map'=>$cover_map,'imgurls'=>$showImg,'video'=>$video,
        'initial'=>$initial,'attr'=>$attrList,
        'min_inventory'=>$min_inventory,'freight'=>$freight_id,'s_type'=>$s_type,'active'=>$active,'show_adr'=>trim($show_adr,','),'volume'=>$volume,'sort'=>'',
        'pro_video'=>$pro_video,'content'=>$content,'richList'=>$richList,'mch_status'=>$mch_status,'unit'=>$unit,'write_off_settings'=>$write_off_settings,'write_off_mch_ids'=>$write_off_mch_ids,'is_appointment'=>$is_appointment,'type'=>'');

        $product = new Product();
        $product->add_product($store_id,$zhanghao,$shop_id,$data,'店铺');
    }

    // 我的商品
    public function My_merchandise()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $commodity_type = trim($this->request->param('commodity_type')); // 商品类型 0.实物商品 1.虚拟商品
        $mch_status = trim($this->request->param('type')); // 审核状态：1.待审核，2.审核通过，3.审核不通过，4.暂不审核
        $status = trim($this->request->param('status')); // 状态 1:待上架 2:上架 3:下架 
        $page = trim($this->request->param('page')); // 页码

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $array = array('store_id'=>$store_id,'zhanghao'=>$zhanghao,'mch_id'=>$shop_id,'page'=>$page,'pagesize'=>10,'mch_status'=>$mch_status,'status'=>$status,'commodity_type'=>$commodity_type,'lang_code'=>$lang_code);

        $product = new Product();
        $product_list = $product->mobile_store_product_list($array);
        $product_list = json_decode($product_list);
        $list = $product_list->list;

        $array = array('store_id'=>$store_id,'shop_id'=>$shop_id);
        $mch_array = $mch->is_margin($array);
        $is_Payment = $mch_array['is_Payment'];
        $isPromiseExamine = $mch_array['isPromiseExamine'];

        $data = array('list' => $list,'is_Payment'=>$is_Payment,'isPromiseExamine'=>$isPromiseExamine);
        $message = Lang('Success');
        return output(200, $message,$data);
    }
    
    // 我的商品-加载更多
    public function My_merchandise_load()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $commodity_type = trim($this->request->param('commodity_type')); // 商品类型 0.实物商品 1.虚拟商品
        $mch_status = trim($this->request->param('type')); // 审核状态：1.待审核，2.审核通过，3.审核不通过，4.暂不审核
        $status = trim($this->request->param('status')); // 状态 1:待上架 2:上架 3:下架 
        $page = trim($this->request->param('page')); // 页码

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $array = array('store_id'=>$store_id,'zhanghao'=>$zhanghao,'mch_id'=>$shop_id,'page'=>$page,'pagesize'=>10,'mch_status'=>$mch_status,'status'=>$status,'commodity_type'=>$commodity_type,'lang_code'=>$lang_code);

        $product = new Product();
        $product_list = $product->mobile_store_product_list($array);
        $product_list = json_decode($product_list);
        $list = $product_list->list;

        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 编辑商品页面
    public function Modify()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $p_id = trim($this->request->param('p_id')); // 商品ID

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $product = new Product();
        $list = $product->edit_page($store_id,$zhanghao,$shop_id,$p_id,'店铺');
        $list = json_decode($list,true);

        $data = array('plugin_list'=>$list['Plugin_arr'],'attr'=>$list['attr_group_list'],'brand_list'=>$list['brand_class'],'brand_class_list1'=>$list['brand_class_list1'],'attrList'=>$list['checked_attr_list'],'content'=>$list['content'],'cover_map'=>$list['cover_map'],'product_class_list1'=>$list['ctypes'],'freight_list'=>$list['freight_list'],'freight_list1'=>$list['freight_list1'],'imgurls'=>$list['imgurls'],'initial'=>$list['initial'],'list'=>$list['list'],'richList'=>$list['richList'],'show_adr'=>$list['show_adr'],'s_type'=>$list['sp_type'],'status'=>$list['status'],'unit'=>$list['unit'],'distributors'=>$list['distributors'],'distributors1'=>$list['distributors1'],'video'=>$list['video'],'proVideo'=>$list['pro_video'],'commodity_type'=>$list['commodity_type'],'write_off_settings'=>$list['write_off_settings'],'write_off_mch_ids'=>$list['write_off_mch_ids'],'is_appointment'=>$list['is_appointment'],'write_off_mch_names'=>$list['write_off_mch_names'],'lang_code'=>$list['lang_code'],'country_num'=>$list['country_num'],'country_name'=>$list['country_name'],'lang_name'=>$list['lang_name']);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 编辑商品
    public function Re_edit()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $p_id = addslashes(trim($this->request->param('p_id'))); // 商品ID
        $commodity_type = urldecode(addslashes(trim($this->request->param('commodity_type')))); // 商品类型 0.实物商品 1.虚拟商品
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
    	$country_num = addslashes(trim($this->request->param('country_num'))); // 所属国家
        $product_title = urldecode(addslashes(trim($this->request->param('product_title')))); // 产品标题
        $subtitle = urldecode(addslashes(trim($this->request->param('subtitle')))); // 小标题
        $keyword = urldecode(addslashes(trim($this->request->param('keyword')))); // 关键词
        $weight = addslashes(trim($this->request->param('weight'))); // 重量
        $weight_unit = 'kg'; // 重量单位
        $product_class = addslashes(trim($this->request->param('product_class_id'))); // 商品分类
        $brand_id = addslashes(trim($this->request->param('brand_id'))); // 品牌
        $cover_map = trim($this->request->param('cover_map')); // 产品封面图
        $showImg = trim($this->request->param('showImg')); // 商品图片
    	$video = $this->request->post('video'); // 展示视频

        $initial = urldecode(addslashes(trim($this->request->param('initial')))); // 初始值
        $min_inventory = urldecode(addslashes(trim($this->request->param('stockWarn')))); // 库存预警
        $unit = urldecode(addslashes(trim($this->request->param('unit')))); // 单位
        $attrList = trim($this->request->param('attr_arr')); // 属性
        $freight_id = trim($this->request->param('freight_id')); // 运费
        $s_type = trim($this->request->param('s_type')); // 显示类型
        $active = trim($this->request->param('active')); // 支持活动
        $show_adr = trim($this->request->param('display_position')); // 显示位置

    	$pro_video = $this->request->post('proVideo'); // 商品视频
        $content = $this->request->param('content'); // 产品内容
        $richList = $this->request->param('richList'); // 产品数组内容
        $volume = trim($this->request->param('volume')); // 虚拟销量
        $mch_status = addslashes(trim($this->request->param('mch_status'))); // 审核状态：1.待审核，2.审核通过，3.审核不通过，4.暂不审核

    	$write_off_settings = addslashes(trim($this->request->param('write_off_settings'))); // 核销设置 1.线下核销 2.无需核销
    	$write_off_mch_ids = addslashes(trim($this->request->param('write_off_mch_ids'))); // 核销门店id  0.全部门店,  1,2,3使用逗号分割
    	$is_appointment = addslashes(trim($this->request->param('is_appointment'))); // 预约时间设置 1.无需预约下单 2.需要预约下单

        $initial = htmlspecialchars_decode($initial); // 将特殊的 HTML 实体转换回普通字符
        $attrList = htmlspecialchars_decode($attrList);
        $attrList = json_decode($attrList, true);
        $content = htmlspecialchars_decode($content);
        $richList = htmlspecialchars_decode($richList);

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $admin_mch_id = $r_admin[0]['shop_id']; // 店铺id

        if($admin_mch_id == $shop_id)
        {
            $mch_status = 2;
        }

        $data = array('id'=>$p_id,'commodity_type'=>$commodity_type,'lang_code'=>$lang_code,'country_num'=>$country_num,'product_title'=>$product_title,'subtitle'=>$subtitle,'keyword'=>$keyword,'product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'cover_map'=>$cover_map,'imgurls'=>$showImg,'video'=>$video,
        'initial'=>$initial,'attr'=>$attrList,
        'min_inventory'=>$min_inventory,'freight'=>$freight_id,'s_type'=>$s_type,'active'=>$active,'show_adr'=>trim($show_adr,','),'volume'=>$volume,'sort'=>'',
        'pro_video'=>$pro_video,'content'=>$content,'richList'=>$richList,'mch_status'=>$mch_status,'unit'=>$unit,'write_off_settings'=>$write_off_settings,'write_off_mch_ids'=>$write_off_mch_ids,'is_appointment'=>$is_appointment,'type'=>'');

        $product = new Product();
        $product->edit_product($store_id,$zhanghao,$shop_id,$data,'店铺');
    }

    // 修改库存页面
    public function Up_stock_page()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $p_id = trim($this->request->param('p_id')); // 商品ID

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $sql0 = "select b.id,b.num,b.min_inventory,b.attribute from lkt_product_list as a left join lkt_configure as b on a.id = b.pid where a.store_id = '$store_id' and a.mch_id = '$shop_id' and a.id = '$p_id' and a.recycle = 0 and b.recycle = 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $attr_group_list = array();
            $checked_attr_list = array();
            $arr1 = array();
            $arr2 = array();
            $arr3 = array();
            foreach ($r0 as $k0 => $v0)
            {
                $arrar_t = unserialize($v0['attribute']);
                foreach ($arrar_t as $k_t => $v_t)
                {
                    if (strpos($k_t, '_LKT_') !== false)
                    {
                        $k_t = substr($k_t, 0, strrpos($k_t, "_LKT"));
                        $v_t = substr($v_t, 0, strrpos($v_t, "_LKT"));
                    }
                    $checked_attr_list[$k0][] = array('cid' => $v0['id'],'name' => $k_t,'value' => $v_t, 'stock' => (int)$v0['num'], 'stockWarn' => (int)$v0['min_inventory'], 'addStockNum' => 0);

                    if(!in_array($k_t,$arr1))
                    {
                        $arr1[] = $k_t;
                        if(!in_array($v_t,$arr2))
                        {
                            $arr2[] = $v_t;
                            $arr3[$k_t][] = $v_t;
                        }
                    }
                    else
                    {
                        if(!in_array($v_t,$arr2))
                        {
                            $arr2[] = $v_t;
                            $arr3[$k_t][] = $v_t;
                        }
                    }
                }
            }

            foreach ($arr3 as $k3 => $v3)
            {
                $attr_group_list[] = array('attrName' => $k3, 'attrValue' => $v3);
            }

            $data = array('attr' => $attr_group_list, 'attrList' => $checked_attr_list);
            $message = Lang('Success');
            return output(200, $message,$data);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '进入修改库存页面失败，商品ID错误！sql:'.$sql1;
            $this->mchLog($Log_content);
            $message = Lang('Illegal invasion');
            return output(115, $message);
        }
    }

    // 修改库存
    public function Up_stock()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $number = trim($this->request->param('number')); // 数量数组
        $attributeInfo = trim($this->request->param('attributeInfo')); // 数量数组
        $stock = json_decode($attributeInfo, true); // 属性

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];
        
        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);
        
        Db::startTrans();
        $time = date("Y-m-d H:i:s");
        $z_num = 0;
        foreach($stock as $k => $v)
        {
            $id = $v['id'];
            $p_id = $v['pid'];
            $addNum = $v['num'];
            $r0 = ConfigureModel::where(['id'=>$id])->field('pid,num,total_num,min_inventory')->select()->toArray();
            if($r0)
            {
                $pid = $r0[0]['pid'];
                $min_inventory = $r0[0]['min_inventory'];
                $total_num = $r0[0]['total_num'] + $addNum;
                $num = $r0[0]['num'] + $addNum;
                $z_num = $z_num + $num;

                if ($num < 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $zhanghao . '修改商品ID为' . $p_id . '属性ID为' . $sid . '的库存时，库存错误！';
                    $this->mchLog($Log_content);
                    Db::rollback();
                    $message = Lang('Parameter error');
                    return output(109, $message);
                }
                else
                {
                    $sql1_where = array('pid'=>$pid,'id'=>$id);
                    $sql1_update = array('total_num'=>$total_num,'num'=>$num);
                    $r1 = Db::name('configure')->where($sql1_where)->update($sql1_update);
                    if ($r1 == -1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $zhanghao . '修改商品ID为' . $p_id . '属性ID为' . $sid . '的库存时，修改失败！';
                        $this->mchLog($Log_content);
                        Db::rollback();
                        $message = Lang('Busy network');
                        return output(109, $message);
                    }
                    else
                    {
                        if((int)$addNum < 0)
                        {
                            $flowing_num = abs($addNum);
                            $content = $zhanghao . '减少商品总库存' . $flowing_num;

                            $sql_stock = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$id,'total_num'=>$total_num,'flowing_num'=>$flowing_num,'type'=>1,'add_date'=>$time,'content'=>$content);
                            $r_stock = Db::name('stock')->insert($sql_stock);
                            if($r_stock <= 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $zhanghao . '修改商品库存时，添加库存记录失败！sql:'.$sql_stock;
                                $this->mchLog($Log_content);
                                Db::rollback();
                                $message = Lang('Busy network');
                                return output(109, $message);
                            }
                        }
                        else if((int)$addNum > 0)
                        {
                            $content = $zhanghao . '增加商品总库存' . $addNum;
                            $sql_stock = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$id,'total_num'=>$total_num,'flowing_num'=>$addNum,'type'=>0,'add_date'=>$time,'content'=>$content);
                            $r_stock = Db::name('stock')->insert($sql_stock);
                            if($r_stock <= 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $zhanghao . '修改商品库存时，添加库存记录失败！sql:'.$sql_stock;
                                $this->mchLog($Log_content);
                                Db::rollback();
                                $message = Lang('Busy network');
                                return output(109, $message);
                            }
                        }
                        
                        if ($min_inventory >= $num)
                        { // 当属性库存低于等于预警值
                            // 在库存记录表里，添加一条预警信息
                            $content1 = '预警';
                            $sql_stock1 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$id,'total_num'=>$total_num,'flowing_num'=>$addNum,'type'=>2,'add_date'=>$time,'content'=>$content1);
                            $r_stock1 = Db::name('stock')->insert($sql_stock1);
                            if($r_stock1 <= 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $zhanghao . '修改商品库存时，添加库存记录失败！sql:'.$sql_stock1;
                                $this->mchLog($Log_content);
                                Db::rollback();
                                $message = Lang('Busy network');
                                return output(109, $message);
                            }
                        }
                        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $zhanghao . '修改商品ID为' . $pid . '属性ID为' . $id . '的库存时，修改成功！';
                        $this->mchLog($Log_content);
                    }
                }
            }
            else
            {
                Db::rollback();
                $message = Lang('Parameter error');
                return output(109, $message);
            }
        }
        
        $sql3_where = array('id'=>$pid);
        $sql3_update = array('num'=>$z_num);
        $r3 = Db::name('product_list')->where($sql3_where)->update($sql3_update);
        if ($r3 == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $zhanghao . '修改商品ID为' . $pid . '的库存时，修改失败！';
            $this->mchLog($Log_content);
            Db::rollback();
            $message = Lang('Busy network');
            return output(109, $message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $zhanghao . '修改商品ID为' . $pid . '的库存时，修改成功！';
            $this->mchLog($Log_content);
        }
        
        Db::commit();
        $message = Lang('Success');
        return output(200, $message);
    }

    // 提交审核/撤销审核
    public function Submit_audit()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $p_id = trim($this->request->param('p_id')); // 商品ID

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);
        
        $data = array('store_id'=>$store_id,'admin_name'=>$zhanghao,'user_id'=>$user_id,'p_id'=>$p_id,'type0'=>'店铺');

        $product = new Product();
        $product_list = $product->examine($data);
        return;
    }

    // 我的商品上下架
    public function My_merchandise_status()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $p_id = trim($this->request->param('p_id')); // 商品ID

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $data = array('store_id'=>$store_id,'admin_name'=>$zhanghao,'p_id'=>$p_id,'type'=>'');
        $product = new Product();
        $product->upper_and_lower_shelves($data,'店铺');
    }

    // 删除我的商品
    public function Del_my_merchandise()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $p_id = trim($this->request->param('p_id')); // 商品ID

        $user_id = $this->user_list['user_id'];
        $zhanghao = $this->user_list['zhanghao'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $product = new Product();
        $product->del($store_id,$zhanghao,$p_id,'店铺');
    }

    // 浏览记录
    public function browse_record()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $current_time = date("Y-m-d :H:i:s");
        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if ($r_user)
        {
            $user_id = $r_user[0]['user_id'];

            $data_mch_browse = array('store_id' => $store_id, 'token' => $access_id, 'mch_id' => $shop_id, 'user_id' => $user_id, 'event' => '访问了店铺', 'add_time' => $current_time);
            $r_mch_browse = Db::name('mch_browse')->replace()->insert($data_mch_browse);
        }

        $message = Lang('Success');
        return output(200,$message);
    }

    // 店铺主页-DIY
    public function diy_home_page()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $lang_code = addslashes($this->request->param('lang_code')); // 语言

        $lang_code = Tools::get_lang($lang_code);

        $pageJson = '';
        // 根据商城id、用户id、审核通过，查询店铺ID
        $r0 = MchModel::where(['store_id'=>$store_id,'id'=>$shop_id,'review_status'=>1,'recovery'=>0])->select()->toArray();
        if ($r0)
        {
            $r1 = DiyModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'status'=>1,'lang_code'=>$lang_code])->field('value')->select()->toArray();
            if($r1)
            {
                $pageJson = $r1[0]['value'];
            }

            $data = array('pageJson' => $pageJson);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺ID错误！sql:'.$sql0;
            $this->mchLog($Log_content);
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
    }

    // 店铺主页
    public function Store_homepage()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $lang_code = addslashes($this->request->param('lang_code')); // 语言

        $shop_list_id = trim($this->request->param('shop_list_id')); // 门店ID
        $longitude = trim($this->request->param('longitude')); // 经度
        $latitude = trim($this->request->param('latitude')); // 纬度
        $type = trim($this->request->param('type')); // 1.推荐 2.全部 3。分类
        $lang_code = Tools::get_lang($lang_code);

        $Member_discount = array('store_id' => $store_id, 'access_id' => $access_id);
        $grade_list = PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade']; // 会员等级ID
        $grade_rate = $grade_list['grade_rate']; // 折扣值

        $has = false;
        $sql = "select status,plugin_code from lkt_plugins where store_id = '$store_id' and plugin_code = 'diy' and flag = 0 ORDER BY id ";
        $r = Db::query($sql);
        if($r)
        {
            $r0 = DiyModel::where(['store_id'=>$store_id,'status'=>1,'mch_id'=>$shop_id,'lang_code'=>$lang_code])->select()->toArray();
            if($r0)
            {
                $has = true;
            }
        }

        $current_time = date("Y-m-d :H:i:s");
        $collection_status = 0; // 未收藏
        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if ($r_user)
        {
            $user_id = $r_user[0]['user_id'];

            $r = UserCollectionModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'mch_id'=>$shop_id])->field('id')->select()->toArray();
            if ($r)
            {
                $collection_status = 1; // 已收藏
            }
            else
            {
                $collection_status = 0; // 未收藏
            }
        }

        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));
        $status2 = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>2));

        // 根据商城id、用户id、审核通过，查询店铺ID
        $r0 = MchModel::where(['store_id'=>$store_id,'id'=>$shop_id,'review_status'=>1,'recovery'=>0])->select()->toArray();
        if ($r0)
        {
            $shop_name = $r0[0]['name']; // 店铺名称
            $shop_logo = ServerPath::getimgpath($r0[0]['logo'], $store_id); // 店铺logo
            $posterImg = ServerPath::getimgpath($r0[0]['poster_img'],$store_id);
            $headimg = ServerPath::getimgpath($r0[0]['head_img'],$store_id);
            $shop_logo = $headimg;
            $roomid = $r0[0]['roomid']; // 直播房间ID
            $is_open = $r0[0]['is_open']; // 是否营业：0.未营业 1.营业中 2.打烊
            $old_roomid = $r0[0]['old_roomid'];//上一次直播房间号
            $mobile = $r0[0]['tel'];//上一次直播房间号
            $business_hours = $r0[0]['business_hours'];//上一次直播房间号
            $shop_information = $r0[0]['shop_information'];//店铺信息

            $quantity_on_sale = 0; // 在售数量
            $quantity_sold = 0; // 已售数量
            $product_class = array();

            $distance = Tools::get_store_distance($store_id,$longitude, $latitude,$shop_id); //根据经纬度获取店铺

			$mchPublicMethod = new MchPublicMethod();
            $mch_list = PC_Tools::StoreData($store_id,$shop_id,$status2);
            // $mch_list = $mchPublicMethod->commodity_information($store_id,$shop_id);
            $collection_num = $mch_list['collection_num'];
            $quantity_on_sale = $mch_list['quantity_on_sale'];
            $quantity_sold = $mch_list['quantity_sold'];

            $r1 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'mch_status'=>2,'recycle'=>0,'active'=>1,'lang_code'=>$lang_code])->whereIn('status',$status2)->order('add_date','desc')->field('id,product_class')->select()->toArray();
            $res = array();
            if ($type == 1)
            {
                $r_sp_type = ProLabelModel::where(['store_id'=>$store_id,'name'=>'推荐'])->field('id')->select()->toArray();
                $label_id = $r_sp_type[0]['id'];
                
                $sql_3 = "select tt.* from (select a.id,a.product_title,a.status,a.subtitle,a.imgurl,a.num,a.volume,a.is_appointment,a.write_off_settings,a.s_type,min(c.price) over (partition by c.pid) as price,c.yprice,row_number () over (partition by c.pid) as top from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.mch_id = '$shop_id' and a.mch_status = 2 and a.recycle = 0 and c.recycle = 0 and a.active = 1 and a.s_type like '%,$label_id,%' and a.is_presell = 0 and a.lang_code = '$lang_code' $status order by a.supplier_superior,a.mch_sort desc,a.upper_shelf_time desc) as tt where tt.top<2 LIMIT 10";
                $r_3 = Db::query($sql_3);
                if ($r_3)
                {
                    foreach ($r_3 as $k => $v)
                    {
                        $s_type = explode(',', trim($v['s_type'],','));
                        $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                        $r_3[$k]['s_type_list'] = $s_type_list;
                        if($v['volume'] < 0)
                        {
                            $r_3[$k]['volume'] = 0;
                        }
                        $r_3[$k]['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                        
                        $r_3[$k]['price'] = round($r_3[$k]['price'],2);
                        $r_3[$k]['yprice'] = round($r_3[$k]['yprice'],2);
                        $r_3[$k]['vip_yprice'] = $r_3[$k]['price'];
                        $r_3[$k]['vip_price'] = $r_3[$k]['price'] * $grade_rate;

						$r_3[$k]['grade'] = $grade;
						$r_3[$k]['stockNum'] = $v['num'];

                        if($v['write_off_settings'] == 1)
                        { // 核销设置 1.线下核销 2.无需核销
                            $r_3[$k]['is_appointment'] = 2; // 2.不能加入购物车
                        }
                        else
                        {
                            $r_3[$k]['is_appointment'] = 1; // 1.能加入购物车
                        }
                    }
                    $res = $r_3;
                }
            }
            else if ($type == 2)
            {
                $sql_3 = "select tt.* from (select a.id,a.product_title,a.status,a.subtitle,a.imgurl,a.num,a.volume,a.is_appointment,a.write_off_settings,a.s_type,min(c.price) over (partition by c.pid) as price,c.yprice,row_number () over (partition by c.pid) as top from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.mch_id = '$shop_id' and a.mch_status = 2 and a.recycle = 0 and c.recycle = 0 and a.active = 1 and a.is_presell = 0 and a.lang_code = '$lang_code' $status order by a.supplier_superior,a.mch_sort desc,a.upper_shelf_time desc) as tt where tt.top<2 LIMIT 10";
                $r_3 = Db::query($sql_3);
                if ($r_3)
                {
                    foreach ($r_3 as $k => $v)
                    {
                        $s_type = explode(',', trim($v['s_type'],','));
                        $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                        $r_3[$k]['s_type_list'] = $s_type_list;
                        if($v['volume'] < 0)
                        {
                            $r_3[$k]['volume'] = 0;
                        }
                        $r_3[$k]['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);

                        $r_3[$k]['price'] = round($r_3[$k]['price'],2);
                        $r_3[$k]['yprice'] = round($r_3[$k]['yprice'],2);
                        $r_3[$k]['vip_yprice'] = $r_3[$k]['price'];
                        $r_3[$k]['vip_price'] = $r_3[$k]['price'] * $grade_rate;

						$r_3[$k]['grade'] = $grade;
						$r_3[$k]['stockNum'] = $v['num'];

                        if($v['write_off_settings'] == 1)
                        { // 核销设置 1.线下核销 2.无需核销
                            $r_3[$k]['is_appointment'] = 2; // 2.不能加入购物车
                        }
                        else
                        {
                            $r_3[$k]['is_appointment'] = 1; // 1.能加入购物车
                        }
                    }
                    $res = $r_3;
                }
            }
            else if ($type == 3)
            {
                if ($r1)
                {
                    foreach ($r1 as $k => $v)
                    {
                        $v['product_class'] = ltrim($v['product_class'], "-"); // 去掉字符串前面的'-'
                        $v['product_class'] = substr($v['product_class'], 0, strpos($v['product_class'], '-')); // 截取字符串第一个'-'前面的内容
                        $product_class[] = $v['product_class'];
                    }
                    $product_class = array_unique($product_class);
                    foreach ($product_class as $key => $value)
                    {
                        $cid = $value;
                        $r_c = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$cid,'recycle'=>0,'lang_code'=>$lang_code])
                                                ->order('sort','asc')
                                                ->field('cid,pname')
                                                ->select()
                                                ->toArray();
                        if ($r_c)
                        {
                            $res[] = $r_c[0];
                        }
                    }
                }
            }
            
            $sheng = $r0[0]['sheng'];
            $shi = $r0[0]['shi'];
            $xian = $r0[0]['xian'];
            $address = $r0[0]['address'];
            $shop_list = array();
            if($shop_list_id)
            {
                $data4 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'id'=>$shop_list_id);
            }
            else
            {
                $data4 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'is_default'=>1);
            }
            $r4 = MchStoreModel::where($data4)->select()->toArray();
            if($r4)
            {
                $sheng = $r4[0]['sheng'];// 省
                $shi = $r4[0]['shi'] ;// 市
                $xian = $r4[0]['xian'];//县
                $address = $r4[0]['address'];// 详细地址
                $shop_list[] = $r4[0];
            }

            $banner = array();
            $r5 = BannerModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->limit(4)->order('sort','desc')->select()->toArray();
            if($r5)
            {
                foreach($r5 as $k=>$v)
                {
                    $result = array();
                    $result['id'] = $v['id']; // 轮播图id
                    $result['image'] = ServerPath::getimgpath($v['image'],$store_id); // 图片
                    $result['url'] = $v['url'];
                    $domain = strstr($v['url'], 'tabBar');
                    if($domain)
                    {
                        $result['type'] = 'switchTab';
                    }
                    else
                    {
                        $result['type'] = 'navigate';
                    }
                    $result['parameter'] = trim(strrchr($v['url'], '='),'=');
                    $banner[] = $result;
                    unset($result); // 销毁指定变量
                }
            }
            $isOpenCoupon = 0; // 优惠券状态
            $isPreSell = false; // 预售状态
            $isFlashSale = false; // 限时折扣状态
            $isShowTime = 0;

            $fs_open = Db::name('flashsale_config')->where(['store_id'=>$store_id,'mch_id'=>$shop_id])->value('is_open');
            if($fs_open == 1)
            {
                //查自营店是否开启
                $shop_id_main = Db::name('admin')->where(['store_id'=>$store_id,'recycle'=>0,'type'=>1])->value('shop_id');
                $fs_status = Db::name('flashsale_config')->where(['store_id'=>$store_id,'mch_id'=>$shop_id_main])->value('is_open');
                if($fs_status == 1)
                {
                    $isFlashSale = true;
                }
            }
            $Plugin_arr = new Plugin();
            $Plugin_arr1 = $Plugin_arr->front_Plugin($store_id);
            foreach ($Plugin_arr1 as $k => $v)
            {
                if ($k == 'CouponPublicMethod' && $v == 1)
                {
                    bind('coupon','app\common\Plugin\CouponPublicMethod');
                    $isOpenCoupon = app('coupon')->Get_store_coupon_status($store_id,$shop_id);
                }
                else if($k == 'Presell_order' && $v == 1)
                {
                    $isPreSell = true;
                }
            }

            $data = array('shop_name' => $shop_name, 'shop_logo' => $shop_logo, 'posterImg' => $posterImg,'poster_img' => $posterImg, 'headImg' => $headimg, 'roomid' => $roomid,'is_open' => $is_open, 'collection_num' => $collection_num, 'collection_status' => $collection_status, 'quantity_on_sale' => $quantity_on_sale, 'quantity_sold' => $quantity_sold, 'list' => $res, 'shop_list' => $r4,'banner'=>$banner,'distance'=>$distance,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'mobile'=>$mobile,'business_hours'=>$business_hours,'isOpenCoupon'=>$isOpenCoupon,'isPreSell'=>$isPreSell,'isFlashSale'=>$isFlashSale,'isShowTime'=>$isShowTime,'shop_information'=>$shop_information,'hasDiy'=>$has);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺ID错误！sql:'.$sql0;
            $this->mchLog($Log_content);
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
    }

    // 店铺主页-加载更多
    public function Store_homepage_load()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $lang_code = addslashes($this->request->param('lang_code')); // 语言

        $type = trim($this->request->param('type')); // 1.推荐 2.全部 3。分类
        $page = trim($this->request->param('page')); // 页码
        $pagesize = trim($this->request->param('pagesize')); // 每页多少条数据

        $start = ($page - 1) * $pagesize;
        $end = $pagesize;
        $lang_code = Tools::get_lang($lang_code);

        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));
    
		$Member_discount = array('store_id' => $store_id, 'access_id' => $access_id);
        $grade_list = PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade']; // 会员等级ID
        $grade_rate = $grade_list['grade_rate']; // 折扣值

        $str = " a.id,a.product_title,a.status,a.subtitle,a.imgurl,a.num,a.volume,a.is_appointment,a.write_off_settings,a.s_type,a.brand_id,a.freight,a.gongyingshang,a.keyword,a.product_class,a.recycle,a.mch_id,c.id as cid,c.img,c.pid,c.costprice,min(c.price) over (partition by c.pid) as price,c.yprice,c.num as stockNum,c.attribute,row_number () over (partition by c.pid) as top ";

        if ($type == 1)
        {
            $r_sp_type = ProLabelModel::where(['store_id'=>$store_id,'name'=>'推荐'])->field('id')->select()->toArray();
            $label_id = $r_sp_type[0]['id'];

            $sql0 = "select tt.* from (select $str from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.mch_id = '$shop_id' and a.mch_status = 2 and a.recycle = 0 and c.recycle = 0 and a.active = 1 and a.s_type like '%,$label_id,%' and a.is_presell = 0 and a.lang_code = '$lang_code' $status order by a.supplier_superior,a.mch_sort desc,a.upper_shelf_time desc) as tt where tt.top<2 LIMIT $start,$end";
        }
        else if ($type == 2)
        {
            $sql0 = "select tt.* from (select $str from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.mch_id = '$shop_id' and a.mch_status = 2 and a.recycle = 0 and c.recycle = 0 and a.active = 1 and a.is_presell = 0 and a.lang_code = '$lang_code' $status order by a.supplier_superior,a.mch_sort desc,a.upper_shelf_time desc) as tt where tt.top<2 LIMIT $start,$end";
        }
        $r0 = Db::query($sql0);
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $s_type = explode(',', trim($v['s_type'],','));
                $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                $r0[$k]['s_type_list'] = $s_type_list;

                if($v['volume'] < 0)
                {
                    $r0[$k]['volume'] = 0;
                }
                $r0[$k]['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id);
                $r0[$k]['img'] = ServerPath::getimgpath($v['img'],$store_id);

                $r0[$k]['price'] = round($r0[$k]['price'],2);
                $r0[$k]['yprice'] = round($r0[$k]['yprice'],2);
                $r0[$k]['vip_yprice'] = $r0[$k]['price'];
                $r0[$k]['vip_price'] = $r0[$k]['price'] * $grade_rate;
                $r0[$k]['sizeid'] = $r0[$k]['cid'];
                $r0[$k]['mchId'] = $shop_id;
                $r0[$k]['mch_name'] = '';
                $r0[$k]['logo'] = '';
                $r0[$k]['is_open'] = 0;
                
                $sql_m = "select id,name,logo,is_open from lkt_mch where id = '$shop_id' ";
                $r_m = Db::query($sql_m);
                if($r_m)
                {
                    $r0[$k]['mch_name'] = $r_m[0]['name'];
                    $r0[$k]['logo'] = ServerPath::getimgpath($r_m[0]['logo'],$store_id);
                    $r0[$k]['is_open'] = $r_m[0]['is_open'];
                }

                if($v['write_off_settings'] == 1)
                { // 核销设置 1.线下核销 2.无需核销
                    $r0[$k]['is_appointment'] = 2; // 2.不能加入购物车
                }
                else
                {
                    $r0[$k]['is_appointment'] = 1; // 1.能加入购物车
                }
				$r0[$k]['grade'] = $grade;
            }
        }
        $data = array('list' => $r0);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 店铺点击收藏按钮
    public function Collection_shop()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $user_id = $this->user_list['user_id'];

        $time = date("Y-m-d H:i:s");
        $r0 = UserCollectionModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'mch_id'=>$shop_id])->select()->toArray();
        if ($r0)
        {
            $r1 = Db::name('user_collection')->where(['store_id'=>$store_id,'user_id'=>$user_id,'mch_id'=>$shop_id])->delete();
            if ($r1 > 0)
            {
                $r2 = Db::name('mch')->where(['store_id'=>$store_id,'id'=>$shop_id])->update(['collection_num' => Db::raw('collection_num-1')]);
                if($r2 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '取消收藏，修改店铺信息失败！';
                    $this->mchLog($Log_content);
                    $message = Lang('Busy network');
                    return output(105,$message );
                }

                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '取消收藏，店铺ID为' . $shop_id . '失败！';
                $this->mchLog($Log_content);
                $message = Lang('Busy network');
                return output(105,$message );
            }
        }
        else
        {
            // 在收藏表里添加一条数据
            $data = ['store_id' => $store_id, 'user_id' => $user_id, 'mch_id' => $shop_id, 'add_time' => $time];
            $r1 = Db::name('user_collection')->replace()->insertGetId($data);
            if ($r1 > 0)
            {
                $r2 = Db::name('mch')->where(['store_id'=>$store_id,'id'=>$shop_id])->update(['collection_num' => Db::raw('collection_num+1')]);
                if($r2 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加收藏，修改店铺信息失败！';
                    $this->mchLog($Log_content);
                    $message = Lang('Busy network');
                    return output(105,$message );
                }

                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加收藏，店铺ID为' . $shop_id . '失败！';
                $this->mchLog($Log_content);
                $message = Lang('Busy network');
                return output(105,$message );
            }
        }
    }

    // 进入设置店铺
    public function Into_set_shop()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $field = "id,name,logo,shop_information,shop_range,realname,ID_number,tel,sheng,shi,xian,address,is_open,cashable_money,is_invoice as isInvoice,poster_img as posterImg,head_img as headImg,cid,business_hours,account_money,is_self_delivery";
        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->field($field)->select()->toArray();
        $r0[0]['logo'] = ServerPath::getimgpath($r0[0]['logo'], $store_id);
        $r0[0]['posterImg'] = ServerPath::getimgpath($r0[0]['posterImg'], $store_id);
        $r0[0]['headImg'] = ServerPath::getimgpath($r0[0]['headImg'], $store_id);
        $r0[0]['cashable_money'] = round($r0[0]['cashable_money'],2);
        $r0[0]['account_money'] = round($r0[0]['account_money'],2);

        $cid = $r0[0]['cid'];
        $sql1 = "select name from lkt_mch_class where store_id = '$store_id' and id = '$cid' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $r0[0]['cname'] = $r1[0]['name'];
        }
        
        $str = "";
        $sr = strlen($r0[0]['ID_number']) - 12;
        for($i=0;$i<$sr;$i++)
        {
            $str .= '*';
        }
        $ID_number1 = substr($r0[0]['ID_number'], 0,8);
        $ID_number2 = substr($r0[0]['ID_number'], -4);
        
        $r0[0]['ID_number'] = $ID_number1 . $str . $ID_number2;

        $data = array('list' => $r0);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 设置店铺
    public function Set_shop()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $name = trim($this->request->param('name')); // 店铺名称
        $shop_information = trim($this->request->param('shop_information')); // 店铺信息
        $cid = trim($this->request->param('cid')); // 店铺信息
        $shop_range = trim($this->request->param('shop_range')); // 经营范围
        $is_open = trim($this->request->param('isOpen')); // 是否营业：0.未营业 1.营业中 2.打样
        $business_hours = trim($this->request->param('businessHours')); // 是否营业：0.未营业 1.营业中 2.打样
        $cpc = trim($this->request->param('cpc')); // 区号
        $tel = trim($this->request->param('tel')); // 联系电话
        $city_all = trim($this->request->param('city_all')); // 省市县
        $address = trim($this->request->param('address')); // 联系地址
        $is_invoice = addslashes(trim($this->request->param('isInvoice'))); // 是否支持开票 0.否 1.是
        $logo = trim($this->request->param('logoImg')); // LOGO
        $posterImg = trim($this->request->param('posterImg')); // 店铺新增宣传图
        $headImg = trim($this->request->param('headImg')); // 店铺头像
        $is_self_delivery = trim($this->request->param('is_self_delivery')); // 是否支持商家自配 0.否 1.是
        
        if ($store_type = 1)
        {
            $store_type = '0';
        }
        elseif ($store_type = 2)
        {
            $store_type = 'app';
        }
        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $data_r = array();

        if (!empty($_FILES))
        {
            $image = ServerPath::file_OSSupload($store_id, $store_type,false,$shop_id);
            if ($image == false)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '设置店铺ID为' . $shop_id . '时，上传失败或图片格式错误！';
                $this->mchLog($Log_content);
                $message = Lang('product.2');
                return output(109,$message);
            }
            else
            {
                $image = preg_replace('/.*\//', '', $image);
            }
            
            if($logo != '')
            {
                $data_r['logo'] = $image;
            }
            else if($posterImg != '')
            {
                $data_r['poster_img'] = $image;
            }
            else
            {
                $data_r['head_img'] = $image;
            }
        }
        else if ($name != '')
        {
            $r = MchModel::where(['store_id'=>$store_id,'name'=>$name,'recovery'=>0])
                        ->where('review_status','<>',2)
                        ->where('id','<>',$shop_id)
                        ->field('id')
                        ->select()
                        ->toArray();
            if ($r)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '设置店铺ID为' . $shop_id . '时，店铺名称已存在！';
                $this->mchLog($Log_content);
                $message = Lang('mch.1');
                return output(223,$message);
            }
            $data_r['name'] = $name;
        }
        else if ($shop_information != '')
        {
            $data_r['shop_information'] = $shop_information;
        }
        else if ($cid != '')
        {
            $data_r['cid'] = $cid;
        }
        else if ($shop_range != '')
        {
            $data_r['shop_range'] = $shop_range;
        }
        else if ($is_open != '')
        {
            $data_r['is_open'] = $is_open;
            if($is_open == 1)
            {
                $data_r['business_hours'] = $business_hours;
            }
        }
        else if ($tel != '')
        {
            if (Tools::check_phone($tel))
            {

            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '设置店铺ID为' . $shop_id . '时，手机号码有误！';
                $this->mchLog($Log_content);
                $message = Lang('Wrong mobile number');
                return output(117,$message);
            }
            $data_r['tel'] = $tel;
        }
        else if ($city_all != '' && $address != '')
        {
            $city_list = explode('-',$city_all);
            $sheng = $city_list[0];
            $shi = $city_list[1];
            $xian = $city_list[2];

            if($address == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员'.$user_id.'申请店铺时，详细地址不能为空！';
                $this->mchLog($Log_content);
                $message = Lang('mch.5');
                return output(109,$message);
            }

            $array_address = array('cpc'=>$cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'code'=>'');
            $address_xx = PC_Tools::address_translation($array_address);

            $Longitude_and_latitude = Tools::get_Longitude_and_latitude( $store_id,$address_xx);
            $longitude = $Longitude_and_latitude['longitude'];
            $latitude = $Longitude_and_latitude['latitude'];

            $data_r['sheng'] = $sheng;
            $data_r['shi'] = $shi;
            $data_r['xian'] = $xian;
            $data_r['address'] = $address;
            $data_r['longitude'] = $longitude;
            $data_r['latitude'] = $latitude;
        }
        // else if ($mch_service != '')
        // {
        //     $data_r['mch_service'] = $mch_service;
        // }
        else if ($is_invoice != '')
        {
            $data_r['is_invoice'] = $is_invoice;
        }
        
        $data_r['is_self_delivery'] = $is_self_delivery;
        
        $r0_0 = Db::name('mch')->where(['store_id'=>$store_id,'user_id'=>$user_id,'id' =>$shop_id])->update($data_r);
        if ($r0_0 < 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '设置店铺失败！参数:'.json_encode($data_r);
            $this->mchLog($Log_content);
            $message = Lang('Busy network');
            return output(103,$message);
        }
        else
        {
            $array = array('store_id'=>$store_id,'type0'=>3,'id'=>$shop_id,'name'=>$name);
            PC_Tools::modify_jump_path($array);

            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 注销店铺
    public function Cancellation_shop()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $user_list = cache($access_id);
        $user_id = $user_list['user_id'];
        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $array = array('store_id'=>$store_id,'id'=>$shop_id,'operator_id'=>0,'operator'=>$user_id,'source'=>3);
        $mch->Cancel_store($array);

        return;
    }

    // 我的顾客
    public function Shop_customer()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $page = trim($this->request->param('page')); // 加载次数

        $user_id = $this->user_list['user_id'];

        $list = array();
        $start_time_1 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d'), date('Y'))); // 今天开始时间
        $end_time_1 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d') + 1, date('Y')) - 1); // 今天结束时间

        $start_time_2 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d') - 1, date('Y'))); // 昨天开始时间
        $end_time_2 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d'), date('Y')) - 1); // 昨天结束时间

        $start_time_3 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d') - 2, date('Y'))); // 前天开始时间
        $end_time_3 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d') - 1, date('Y')) - 2); // 前天结束时间

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $several = 10;
        $start = ($page - 1) * $several;

        // 查询今天的数据
        $list['list1'] = $this->customer_list( $store_id, $shop_id, $start_time_1, $end_time_1, $start, $several);

        if(isset($list['list1']['res']))
        {
            // 当今天的数据少于10条时，查询昨天的数据
            if (count($list['list1']['res']) < 10)
            {
                $several = $several - count($list['list1']['res']);
            }
        }
        
        if($several > 0)
        {
            $list['list2'] = $this->customer_list($store_id, $shop_id, $start_time_2, $end_time_2, $start, $several);
            if(isset($list['list2']['res']))
            {
                // 当今天的数据加上昨天的数据少于10条时，查询前天的数据
                if (count($list['list2']['res']) < $several)
                {
                    $several = $several - count($list['list2']['res']);
                }
            }
        }
        
        if($several > 0)
        {
            $list['list3'] = $this->customer_list($store_id, $shop_id, $start_time_3, $end_time_3, $start, $several);
            if(isset($list['list3']['res']))
            {
                // 当今天昨天前天的数据之和少于10条时，查询更早的数据
                if (count($list['list3']['res']) < $several)
                {
                    $several = $several - count($list['list3']['res']);
                }
            }
        }
        
        if($several > 0)
        {
            $list['list4'] = $this->customer_list($store_id, $shop_id, $start_time_3, '', $start, $several);
        }

        $num = 0;
        foreach ($list as $k => $v)
        {
            if(isset($v['res']))
            {
                $num = $num + count($v['res']);
            }
        }
        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '进入我的顾客';
        $this->mchLog($Log_content);

        $data = array('list' => $list, 'num' => $num);

        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 查询顾客数据
    public function customer_list($store_id, $mch_id, $start_time, $end_time, $start, $several)
    {
        $list = array();
        $arr = array();
        if ($end_time != '')
        {
            $sql1 = "select tt.* from (select a.*,row_number () over (partition by a.user_id) as top from (select user_id,token,event,add_time from lkt_mch_browse where store_id = '$store_id' and mch_id = '$mch_id' and add_time > '$start_time' and add_time < '$end_time' order by add_time desc) as a) as tt where tt.top<2 order by tt.add_time desc limit $start,$several";
            $r1 = Db::query($sql1);
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    $user_id = $v['user_id'];
                    $v['add_time'] = date('H:i:s', strtotime($v['add_time']));
                    $r1_1 = MchBrowseModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'user_id'=>$user_id])
                                            ->where('add_time','>',$start_time)
                                            ->where('add_time','<=',$end_time)
                                            ->field('id,store_id,user_id,token,event,add_time')
                                            ->select()
                                            ->toArray();
                    $arr[$user_id] = $r1_1;
                }
                if (count($arr) != 0)
                {
                    $list = $this->customer($store_id, $arr);
                }
                else
                {
                    $list['res'] = array();
                }
            }
            else
            {
                $list['res'] = array();
            }
            $list['time'] = date('Y-m-d', strtotime($end_time));
        }
        else
        {
            $sql1 = "select tt.* from (select a.*,row_number () over (partition by a.user_id) as top from (select user_id,token,event,add_time from lkt_mch_browse where store_id = '$store_id' and mch_id = '$mch_id' and add_time <= '$start_time' order by add_time desc) as a) as tt where tt.top<2 order by tt.add_time desc limit $start,$several";
            $r1 = Db::query($sql1);
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    $user_id = $v['user_id'];
                    $v['add_time'] = date('H:i:s', strtotime($v['add_time']));
                    $r1_1 = MchBrowseModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'user_id'=>$user_id])
                                            ->where('add_time','<=',$start_time)
                                            ->field('id,store_id,user_id,token,event,add_time')
                                            ->select()
                                            ->toArray();
                    $arr[$user_id] = $r1_1;
                }
                if (count($arr) != 0)
                {
                    $list = $this->customer($store_id, $arr);
                }
                else
                {
                    $list['res'] = array();
                }
            }
            else
            {
                $list['res'] = array();
            }
            $list['time'] = '更早';
        }
        return $list;
    }

    // 顾客数据
    public function customer($store_id, $res)
    {
        $list = array();
        foreach ($res as $K => $v)
        {
            if (mb_substr($v[0]['user_id'], 0, 2, 'utf-8') == '游客')
            {
                $r_1 = ConfigModel::where(['store_id'=>$store_id])->field('wx_headimgurl')->select()->toArray();
                if ($r_1)
                {
                    foreach ($v as $ke => $va)
                    {
                        $v[$ke]['headimgurl'] = ServerPath::getimgpath($r_1[0]['wx_headimgurl'], $store_id);
                        $v[$ke]['zhanghao'] = $va['user_id'];
                    }
                }
            }
            else
            {
                $user_id1 = $v[0]['user_id'];
                $r_1 = UserModel::where(['user_id'=>$user_id1])->field('headimgurl,user_name')->select()->toArray();
                if ($r_1)
                {
                    foreach ($v as $ke => $va)
                    {
                        $v[$ke]['headimgurl'] = $r_1[0]['headimgurl'];
                        $v[$ke]['zhanghao'] = $r_1[0]['user_name'];
                    }
                }
            }
            $list['res'][] = $v;
        }
        return $list;
    }
    
    // 我的粉丝
    public function ShopFans()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $page = trim($this->request->param('page')); // 加载次数

        $user_id = $this->user_list['user_id'];

        $list = array();
        $start_time_1 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d'), date('Y'))); // 今天开始时间
        $end_time_1 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d') + 1, date('Y')) - 1); // 今天结束时间

        $start_time_2 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d') - 1, date('Y'))); // 昨天开始时间
        $end_time_2 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d'), date('Y')) - 1); // 昨天结束时间

        $start_time_3 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d') - 2, date('Y'))); // 前天开始时间
        $end_time_3 = date("Y-m-d H:i:s", mktime(0, 0, 0, date('m'), date('d') - 1, date('Y')) - 2); // 前天结束时间

        // 验证店铺信息
        $mch = new MchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $several = 10;
        $start = ($page - 1) * $several;
        // 查询今天的数据
        $list1 = array();
        $list2 = array();
        $list3 = array();
        $list4 = array();

        $r0 = UserCollectionModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->where('add_time','>=',$start_time_1)->where('add_time','<',$end_time_1)->field('id as cid,user_id as userId')->select()->toArray();
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $user_id = $v0['userId'];
                $r_0 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('headimgurl,user_name')->select()->toArray();
                if($r_0)
                {
                    $v0['headImg'] = $r_0[0]['headimgurl'];
                    $v0['userName'] = $r_0[0]['user_name'];
                }
                $list1['res'][] = $v0;
                $list1['time'] = date('Y-m-d', strtotime($start_time_1));
            }
        }

        $r1 = UserCollectionModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->where('add_time','>=',$start_time_2)->where('add_time','<',$end_time_2)->field('id as cid,user_id as userId')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k1 => $v1)
            {
                $user_id = $v1['userId'];
                $r_1 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('headimgurl,user_name')->select()->toArray();
                if($r_1)
                {
                    $v1['headImg'] = $r_1[0]['headimgurl'];
                    $v1['userName'] = $r_1[0]['user_name'];
                }
                $list2['res'][] = $v1;
                $list2['time'] = date('Y-m-d', strtotime($start_time_2));
            }
        }

        $r2 = UserCollectionModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->where('add_time','>=',$start_time_3)->where('add_time','<',$end_time_3)->field('id as cid,user_id as userId')->select()->toArray();
        if($r2)
        {
            foreach($r2 as $k2 => $v2)
            {
                $user_id = $v2['userId'];
                $r_2 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('headimgurl,user_name')->select()->toArray();
                if($r_2)
                {
                    $v2['headImg'] = $r_2[0]['headimgurl'];
                    $v2['userName'] = $r_2[0]['user_name'];
                }
                $list3['res'][] = $v2;
                $list3['time'] = date('Y-m-d', strtotime($start_time_3));
            }
        }

        $r3 = UserCollectionModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->where('add_time','<=',$start_time_3)->field('id as cid,user_id as userId')->select()->toArray();
        if($r3)
        {
            foreach($r3 as $k3 => $v3)
            {
                $user_id = $v3['userId'];
                $r_3 = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('headimgurl,user_name')->select()->toArray();
                if($r_3)
                {
                    $v3['headImg'] = $r_3[0]['headimgurl'];
                    $v3['userName'] = $r_3[0]['user_name'];
                }
                $list4['res'][] = $v3;
                $list4['time'] = '更早';
            }
        }
        $list = array('list1'=>$list1,'list2'=>$list2,'list3'=>$list3,'list4'=>$list4);
        $num = 0;
        foreach ($list as $k => $v)
        {
            if(isset($v['res']))
            {
                $num = $num + count($v['res']);
            }
        }
        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '进入我的顾客';
        $this->mchLog($Log_content);

        $data = array('list' => $list, 'num' => $num);

        $message = Lang('Success');
        return output(200,$message,$data);
    }
    
    // 移除粉丝
    public function RemoveFans()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $cid = trim($this->request->param('cid')); // 收藏ID

        $user_id = $this->user_list['user_id'];
        $user_name = $this->user_list['user_name'];

        $r0 = UserCollectionModel::where(['store_id'=>$store_id,'id'=>$cid])->field('mch_id')->select()->toArray();
        if($r0)
        {
            $shop_id = $r0[0]['mch_id'];

            $r1 = Db::name('user_collection')->where(['store_id'=>$store_id,'id'=>$cid])->delete();
            if ($r1 > 0)
            {
                $r2 = Db::name('mch')->where(['store_id'=>$store_id,'id'=>$shop_id])->update(['collection_num' => Db::raw('collection_num-1')]);
                if($r2 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_name . '移除粉丝，修改店铺信息失败！';
                    $this->mchLog($Log_content);
                    $message = Lang('Busy network');
                    return output(105,$message );
                }

                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $user_name . '移除粉丝，店铺ID为' . $shop_id . '失败！';
                $this->mchLog($Log_content);
                $message = Lang('Busy network');
                return output(105,$message );
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '取消收藏，店铺ID为' . $shop_id . '失败！';
            $this->mchLog($Log_content);
            $message = Lang('Parameter error');
            return output(105,$message );
        }
    }

    // 我的提现
    public function My_wallet()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        // 根据商城id、用户id、审核通过，查询店铺ID
        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->field('id,account_money,integral_money,cashable_money')->select()->toArray();
        if ($r0)
        {
            $account_money = $r0[0]['account_money']; // 商户余额
            $integral_money = $r0[0]['integral_money']; // 商户积分
            $cashable_money = $r0[0]['cashable_money']; // 可提现金额

            $day = date("j"); // 获取今天是几号，没有前置0
            $PopUpContent = ''; // 弹窗内容
            $illustrate = '';
            $withdrawal_time_open = ''; // 提现时间开关 0.不限制 1.指定日期 2.指定时间段
            $withdrawal_time = ''; // 指定时间(时间段:15-20)
            $r1 = MchConfigModel::where(['store_id'=>$store_id])->field('illustrate,withdrawal_time_open,withdrawal_time')->select()->toArray();
            if ($r1)
            {
                $illustrate = $r1[0]['illustrate']; // 提现说明
                $withdrawal_time_open = $r1[0]['withdrawal_time_open']; // 提现时间开关 0.不限制 1.指定日期 2.指定时间段
                $withdrawal_time = $r1[0]['withdrawal_time']; // 指定时间(时间段:15-20)
                if($withdrawal_time_open == 1)
                {
                    if($withdrawal_time != $day)
                    {
                        $PopUpContent = Lang('mch.87') . $withdrawal_time . Lang('mch.90'); // 弹窗内容
                    }
                }
                else if($withdrawal_time_open == 2)
                {
                    $withdrawal_time_list = explode('-',$withdrawal_time);
                    if($day < $withdrawal_time_list[0] || $day > $withdrawal_time_list[1])
                    {
                        $PopUpContent = Lang('mch.88') . $withdrawal_time_list[0] . Lang('mch.89') . $withdrawal_time_list[1] . Lang('mch.90'); // 弹窗内容
                    }
                }
            }

            // 收入总额
            $rrr = MchAccountLogModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'type'=>1])->field('sum(price) as sum')->select()->toArray();
            if ($rrr)
            {
                if ($rrr[0]['sum'])
                {
                    $all_money = $rrr[0]['sum'];
                }
                else
                {
                    $all_money = '0.00';
                }
            }
            else
            {
                $all_money = '0.00';
            }

            $array = array('store_id'=>$store_id,'shop_id'=>$shop_id);
            $mch_array = $mch->is_margin($array);
            $is_Payment = $mch_array['is_Payment'];
            $isPromiseExamine = $mch_array['isPromiseExamine'];

            $data = array('account_money' => round($account_money,2), 'integral_money' => round($integral_money,2), 'all_money' => round($all_money,2), 'illustrate' => $illustrate, 'cashAmt'=>floatval($cashable_money),'PopUpContent' => $PopUpContent,'is_Payment' => $is_Payment,'isPromiseExamine' => $isPromiseExamine);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
    }

    // // 账户明细
    // public function account_details()
    // {
    //     $store_id = trim($this->request->param('store_id'));
    //     $store_type = trim($this->request->param('store_type'));
    //     $language = trim($this->request->param('language')); // 语言
    //     $access_id = trim($this->request->param('access_id')); // 授权id
    //     $shop_id = trim($this->request->param('shop_id')); // 店铺ID

    //     $type = trim($this->request->param('type')); // 类型
    //     $keyWord = trim($this->request->param('keyWord')); // 搜索条件

    //     $user_id = $this->user_list['user_id'];

    //     // 验证店铺信息
    //     $mch = new mchPublicMethod();
    //     $mch->verification_mch($store_id, $user_id,$shop_id);

        
    //     $list = array();
    //     $list1 = array();
    //     $num = 0;
    //     if ($type == 1)
    //     { // 入账
    //         if($keyWord != '')
    //         {
    //             $r1 = MchAccountLogModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'status'=>1])
    //                                 ->whereLike('remake','%'.$keyWord.'%')
    //                                 ->limit(10)
    //                                 ->order('addtime','desc')
    //                                 ->select()
    //                                 ->toArray();
    //         }
    //         else
    //         {
    //             $r1 = MchAccountLogModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'status'=>1])
    //                                 ->limit(10)
    //                                 ->order('addtime','desc')
    //                                 ->select()
    //                                 ->toArray();
    //         }
    //     }
    //     else if ($type == 2)
    //     { // 出账
    //         if($keyWord != '')
    //         {
    //             $r1 = MchAccountLogModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'status'=>2])
    //                                 ->whereLike('remake','%'.$keyWord.'%')
    //                                 ->limit(10)
    //                                 ->order('addtime','desc')
    //                                 ->select()
    //                                 ->toArray();
    //         }
    //         else
    //         {
    //             $r1 = MchAccountLogModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'status'=>2])
    //                                 ->limit(10)
    //                                 ->order('addtime','desc')
    //                                 ->select()
    //                                 ->toArray();
    //         }
    //     }
    //     else
    //     { // 积分
    //         if($keyWord != '')
    //         {
    //             $r1 = MchAccountLogModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])
    //                                 ->where('integral','>','0')
    //                                 ->whereLike('remake','%'.$keyWord.'%')
    //                                 ->limit(10)
    //                                 ->order('addtime','desc')
    //                                 ->select()
    //                                 ->toArray();
    //         }
    //         else
    //         {
    //             $r1 = MchAccountLogModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])
    //                                 ->where('integral','>','0')
    //                                 ->limit(10)
    //                                 ->order('addtime','desc')
    //                                 ->select()
    //                                 ->toArray();
    //         }
    //     }
    //     if ($r1)
    //     {
    //         foreach ($r1 as $k => $v)
    //         {
    //             $v['time'] = date("Y-n-j", strtotime($v['addtime']));
    //             if ($v['type'] == 1)
    //             {
    //                 $v['type_name'] = '订单';
    //             }
    //             else if ($v['type'] == 2)
    //             {
    //                 $v['type_name'] = '退款';
    //             }
    //             else
    //             {
    //                 $v['type_name'] = '提现';
    //             }
    //             $time = $v['time'];
    //             $list1[$time][] = $v;
    //         }

    //         foreach ($list1 as $ke => $va)
    //         {
    //             $list[$num]['time'] = $ke;
    //             $list[$num]['res'] = $va;
    //             $num = $num + 1;
    //         }
    //     }
    //     $data = array('list' => $list);
    //     $message = Lang('Success');
    //     return output(200,$message,$data);
    // }

    // // 账户明细-加载更多
    // public function account_details_load()
    // {
    //     $store_id = trim($this->request->param('store_id'));
    //     $store_type = trim($this->request->param('store_type'));
    //     $language = trim($this->request->param('language')); // 语言
    //     $access_id = trim($this->request->param('access_id')); // 授权id
    //     $shop_id = trim($this->request->param('shop_id')); // 店铺ID

    //     $type = trim($this->request->param('type')); // 类型
    //     $keyWord = trim($this->request->param('keyWord')); // 搜索条件
    //     $pege = trim($this->request->param('pege')); // 加载次数

    //     $user_id = $this->user_list['user_id'];
    //     $end = 10;
    //     $start = $pege * $end;

    //     // 验证店铺信息
    //     $mch = new mchPublicMethod();
    //     $mch->verification_mch($store_id, $user_id,$shop_id);

    //     $list = array();
    //     if ($type == 1)
    //     { // 入账
    //         if($keyWord != '')
    //         {
    //             $r1 = MchAccountLogModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'status'=>1])
    //                                 ->whereLike('remake','%'.$keyWord.'%')
    //                                 ->limit($start,$end)
    //                                 ->order('addtime','desc')
    //                                 ->select()
    //                                 ->toArray();
    //         }
    //         else
    //         {
    //             $r1 = MchAccountLogModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'status'=>1])
    //                                 ->limit($start,$end)
    //                                 ->order('addtime','desc')
    //                                 ->select()
    //                                 ->toArray();
    //         }
    //     }
    //     else if ($type == 2)
    //     { // 出账
    //         if($keyWord != '')
    //         {
    //             $r1 = MchAccountLogModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'status'=>2])
    //                                 ->whereLike('remake','%'.$keyWord.'%')
    //                                 ->limit($start,$end)
    //                                 ->order('addtime','desc')
    //                                 ->select()
    //                                 ->toArray();
    //         }
    //         else
    //         {
    //             $r1 = MchAccountLogModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'status'=>2])
    //                                 ->limit($start,$end)
    //                                 ->order('addtime','desc')
    //                                 ->select()
    //                                 ->toArray();
    //         }
    //     }
    //     if ($r1)
    //     {
    //         foreach ($r1 as $k => $v)
    //         {
    //             $v['time'] = date("Y-n-j", strtotime($v['addtime']));
    //             if ($v['type'] == 1)
    //             {
    //                 $v['type_name'] = '订单';
    //             }
    //             else if ($v['type'] == 2)
    //             {
    //                 $v['type_name'] = '退款';
    //             }
    //             else
    //             {
    //                 $v['type_name'] = '提现';
    //             }
    //             $list[$k]['time'] = $v['time'];
    //             $list[$k]['res'] = $v;
    //         }
    //     }
    //     $data = array('list' => $list);
    //     $message = Lang('Success');
    //     return output(200,$message,$data);
    // }

    // 明细
    public function Detail()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $type = trim($this->request->param('type')); // 类型 1.售后 2.提现 3.收入明细 4.支出明细
        $tabIndex = trim($this->request->param('tabIndex')); // 类型 0.全部 1.待审核 2.审核通过 3.审核失败
        $orderNo = trim($this->request->param('orderNo')); // 订单号
        $startDay = trim($this->request->param('startDay')); // 查询开始时间
        $endDay = trim($this->request->param('endDay')); // 查询结束时间
        $pege = trim($this->request->param('pageNo')); // 加载次数
        $pageSize = trim($this->request->param('pageSize')); // 每页多少条数据

        $user_id = $this->user_list['user_id'];
        $start = ($pege - 1) * $pageSize;

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $list = array();
        $z_price = 0;
        
        $rew = " store_id = '$store_id' and mch_id = '$shop_id' ";
        if($orderNo != '')
        {
            $rew .= " and remake like '%$orderNo%' ";
        }
        if($startDay != '')
        {
            $rew .= " and addtime >= '$startDay' ";
        }
        if($endDay != '')
        {
            $rew .= " and addtime <= '$endDay' ";
        }
        if ($type == 1)
        { // 售后
            $rew .= " and status = 2 and type = 2 ";
            $sql1 = "select * from lkt_mch_account_log where $rew order by addtime desc limit $start,$pageSize";
            $sql_z = "select ifnull(sum(price),0) as price from lkt_mch_account_log where $rew ";
        }
        else if ($type == 2)
        { // 提现
            $con = '';
            if($tabIndex != 0)
            {
                $status = $tabIndex - 1;
                $con = " and w.status = '$status' ";
            }
            $sql1 = "select w.id,w.user_id,w.name as NAME,w.add_date as addtime,w.money,w.s_charge,w.mobile,w.status,w.examine_date,b.Bank_name,b.Bank_card_number from lkt_withdraw as w left join lkt_user_bank_card as b on w.Bank_id = b.id where w.store_id = '$store_id' and w.is_mch = 1 and w.user_id = '$user_id' and w.recovery = 0 $con order by w.add_date desc limit $start,$pageSize ";

            $sql_z = "select ifnull(sum(w.money),0) as price from lkt_withdraw as w left join lkt_user_bank_card as b on w.Bank_id = b.id where w.store_id = '$store_id' and w.is_mch = 1 and w.user_id = '$user_id' and w.recovery = 0 $con ";
        }
        else if ($type == 3)
        {
            $rew .= " and status = 1 ";
            $sql1 = "select * from lkt_mch_account_log where $rew order by addtime desc limit $start,$pageSize";

            $sql_z = "select ifnull(sum(price),0) as price from lkt_mch_account_log where $rew ";
        }
        else
        {
            $rew .= " and status = 2 ";
            $sql1 = "select * from lkt_mch_account_log where $rew order by addtime desc limit $start,$pageSize";

            $sql_z = "select ifnull(sum(price),0) as price from lkt_mch_account_log where $rew ";
        }
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['time'] = date("Y-n-j", strtotime($v['addtime']));
                if($type != 2)
                {
                    if($v['type'] == 1)
                    {
                        $v['type_name'] = '订单';
                    }
                    else if($v['type'] == 2)
                    {
                        $v['type_name'] = '退款';
                    }
                    else if($v['type'] == 3)
                    {
                        $v['type_name'] = '提现';
                    }
                    else if($v['type'] == 4)
                    {
                        $v['type_name'] = '保证金';
                    }
                    else
                    {
                        $v['type_name'] = '供应商订单';
                    }
                }
                else
                {
                    $v['type_name'] = '提现';
                    $v['price'] = $v['money'];
                }
                $list[] = $v;
            }
        }

        $r_z = Db::query($sql_z);
        if($r_z)
        {
            $z_price = $r_z[0]['price'];
        }
        $income = $z_price;

        $data = array('list' => $list,'income' => $income);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 提现详情
    public function Withdrawal_details()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = trim($this->request->param('id')); // 

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $list = array();

        $sql0 = "select id,user_id,name as NAME,add_date,money as price,s_charge,mobile,status,examine_date,withdraw_status,wx_status as wxStatus,txsno as remake,Bank_id,refuse from lkt_withdraw where store_id = '$store_id' and is_mch = 1 and user_id = '$user_id' and recovery = 0 and id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $Bank_id = $v0['Bank_id'];
                $v0['addtime'] = $v0['add_date'];
                $v0['withdrawStatus'] = (string)$v0['withdraw_status'];
                $v0['withdrawalMethod'] = '';
                $v0['Bank_card_number'] = '';
                if($v0['withdraw_status'] == 2)
                {
                    $v0['withdrawalMethod'] = '微信零钱';
                }
                else
                {
                    if($Bank_id != 0)
                    {
                        $sql1 = "select Bank_name,Bank_card_number from lkt_bank_card where id = '$Bank_id' ";
                        $r1 = Db::query($sql1);
                        if($r1)
                        {
                            $v0['Bank_card_number'] = $r1[0]['Bank_card_number'];
                            $v0['withdrawalMethod'] = $r1[0]['Bank_name'] . ' 尾号 ('. substr($r1[0]['Bank_card_number'], -4) . ') ';;
                        }
                    }
                }
                
                $list = $v0;
            }
        }

        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 提现明细删除
    public function del_Withdrawal_details()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = trim($this->request->param('id')); // 提现ID

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $r1 = Db::name('withdraw')->where(['store_id' => $store_id,'id' => $id])->update(['recovery' => 1]);
        if($r1 > 0)
        {
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '删除提现详情失败！';
            $this->mchLog($Log_content);
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
    }
    
    // 获取门店信息
    public function get_write_shop()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $s_no = trim($this->request->param('s_no')); // 订单号

        $data = array();
        $sql0 = "select a.mch_id,b.write_off_settings,b.write_off_mch_ids from lkt_order_details as a left join lkt_product_list as b on a.p_id = b.id where a.store_id = '$store_id' and a.r_sNo = '$s_no' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $mch_id = $r0[0]['mch_id']; // 店铺ID
            $write_off_settings = $r0[0]['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
            $write_off_mch_ids = $r0[0]['write_off_mch_ids']; // 核销门店id  0全部门店,  1,2,3使用逗号分割

            if($write_off_mch_ids == 0 || $write_off_mch_ids == '')
            {
                $sql3 = "select id,name from lkt_mch_store where store_id = '$store_id' and mch_id = '$mch_id' order by is_default desc ";
            }
            else
            {
                $sql3 = "select id,name from lkt_mch_store where store_id = '$store_id' and mch_id = '$mch_id' and id in ($write_off_mch_ids) ";
            }
            $r3 = Db::query($sql3);
            if($r3)
            {
                $data = $r3;
            }
        }
        
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 门店管理
    public function See_my_store()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $pro_id = trim($this->request->param('pro_id')); // 商品ID
        
        $list = array();
        $mchStoreList = array();
        if($pro_id)
        {
            $sql2 = "select write_off_mch_ids from lkt_product_list where id = '$pro_id' ";
            $r2 = Db::query($sql2);
            if($r2)
            {
                $write_off_mch_ids = $r2[0]['write_off_mch_ids'];

                if($write_off_mch_ids != 0)
                {
                    $sql3 = "select * from lkt_mch_store where store_id = '$store_id' and mch_id = '$shop_id' and id in ($write_off_mch_ids) ";
                }
                else
                { // 全部门店
                    $sql3 = "select * from lkt_mch_store where store_id = '$store_id' and mch_id = '$shop_id' ";
                }
                $r3 = Db::query($sql3);
                if($r3)
                {
                    foreach($r3 as $k3 => $v3)
                    {
                        $address = $v3['sheng'] . ' ' . $v3['shi'] . ' ' . $v3['xian'] . ' ' . $v3['address'];
                        $mchStoreList[] = array('name'=>$v3['name'],'phone'=>$v3['mobile'],'address'=>$address,'business_hours'=>$v3['business_hours']);
                        $list[] = $v3;
                    }
                }
            }
        }
        else
        {
            $sql3 = "select * from lkt_mch_store where store_id = '$store_id' and mch_id = '$shop_id' ";
            $r3 = Db::query($sql3);
            if($r3)
            {
                foreach($r3 as $k3 => $v3)
                {
                    $address = $v3['sheng'] . ' ' . $v3['shi'] . ' ' . $v3['xian'] . ' ' . $v3['address'];
                    $mchStoreList[] = array('name'=>$v3['name'],'phone'=>$v3['mobile'],'address'=>$address,'business_hours'=>$v3['business_hours']);
                    $list[] = $v3;
                }
            }
        }
        $data = array('list' => $list,'mchStoreList'=>$mchStoreList);

        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加我的门店
    public function Add_store()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $name = trim($this->request->param('name')); // 店铺名称
        $cpc = addslashes(trim($this->request->param('cpc'))); // 区号
        $mobile = trim($this->request->param('mobile')); // 联系电话
        $business_hours = trim($this->request->param('business_hours')); // 营业时间
        $city_all = trim($this->request->param('city_all')); // 省市区
        $address = trim($this->request->param('address')); // 详细地址
        $code = trim($this->request->param('code')); // 邮政编码
        $is_default = trim($this->request->param('is_default')); // 是否默认 0.不默认 1.默认

        $time = date("Y-m-d H:i:s");
        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        if ($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加门店时，店铺名称不能为空！';
            $this->mchLog($Log_content);
            $message = Lang('mch.15');
            return output(109,$message);
        }

        $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'name'=>$name])->field('id')->select()->toArray();
        if($r0_0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加门店时，店铺名称重复！';
            $this->mchLog($Log_content);
            $message = Lang('mch.16');
            return output(109,$message);
        }

        if ($mobile == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加门店时，联系电话不能为空！';
            $this->mchLog($Log_content);
            $message = Lang('mch.17');
            return output(109,$message);
        }

        if ($city_all == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加门店时，所在城市不能为空！';
            $this->mchLog($Log_content);
            $message = Lang('mch.20');
            return output(109,$message);
        }
        else
        {
            $city_all = explode('-', $city_all);
            $sheng = $city_all[0];
            $shi = $city_all[1];
            $xian = $city_all[2];

            if ($address == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加门店时，详细地址不能为空！';
                $this->mchLog($Log_content);
                $message = Lang('mch.5');
                return output(109,$message);
            }

            $array_address = array('cpc'=>$cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'code'=>$code);
            $address_xx = PC_Tools::address_translation($array_address);

            $Longitude_and_latitude = Tools::get_Longitude_and_latitude( $store_id ,$address_xx);
            $longitude = $Longitude_and_latitude['longitude'];
            $latitude = $Longitude_and_latitude['latitude'];
        }

        if($is_default == 1)
        {
            Db::name('mch_store')->where(['store_id'=>$store_id,'mch_id'=>$shop_id,'is_default'=>1])->update(['is_default' => 0]);
        }
        else
        {
            $r0_1 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->field('id')->select()->toArray();
            if(!$r0_1)
            {
                $is_default = 1;
            }
        }

        $data1 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'business_hours'=>$business_hours,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'add_date'=>$time,'longitude'=>$longitude,'latitude'=>$latitude,'is_default'=>$is_default,'code'=>$code);
        $r1 = Db::name('mch_store')->insertGetId($data1);
        if ($r1 > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加门店成功！';
            $this->mchLog($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加门店失败！';
            $this->mchLog($Log_content);
            $message = Lang('Busy network');
            return output(103,$message);
        }
    }

    // 编辑我的门店-页面
    public function Edit_store_page()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = trim($this->request->param('id')); // 门店ID

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $list = array();

        $r1 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'id'=>$id])->select()->toArray();
        if ($r1)
        {
            $r1[0]['business_hours'] = explode(',', $r1[0]['business_hours']);
            $list = $r1[0];
        }
        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 编辑我的门店
    public function Edit_store()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = trim($this->request->param('id')); // 门店ID
        $name = trim($this->request->param('name')); // 店铺名称
        $cpc = trim($this->request->param('cpc')); // 联系电话
        $mobile = trim($this->request->param('mobile')); // 联系电话
        $business_hours = trim($this->request->param('business_hours')); // 营业时间
        $city_all = trim($this->request->param('city_all')); // 省市区
        $address = trim($this->request->param('address')); // 详细地址
        $code = trim($this->request->param('code')); // 邮政编码
        $is_default = trim($this->request->param('is_default')); // 是否默认 0.不默认 1.默认

        $time = date("Y-m-d H:i:s");
        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        if ($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '编辑门店时，店铺名称不能为空！';
            $this->mchLog($Log_content);
            $message = Lang('mch.15');
            return output(109,$message);
        }

        $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
        if($r0_0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加门店时，店铺名称重复！';
            $this->mchLog($Log_content);
            $message = Lang('mch.16');
            return output(109,$message);
        }
        if ($mobile == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '编辑门店时，联系电话不能为空！';
            $this->mchLog($Log_content);
            $message = Lang('mch.17');
            return output(109,$message);
        }

        if ($city_all == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '编辑门店时，所在城市不能为空！';
            $this->mchLog($Log_content);
            $message = Lang('mch.20');
            return output(109,$message);
        }
        else
        {
            $city_all = explode('-', $city_all);
            $sheng = $city_all[0];
            $shi = $city_all[1];
            $xian = $city_all[2];

            if ($address == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '编辑门店时，详细地址不能为空！';
                $this->mchLog($Log_content);
                $message = Lang('mch.5');
                return output(109,$message);
            }

            $array_address = array('cpc'=>$cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'code'=>$code);
            $address_xx = PC_Tools::address_translation($array_address);

            $Longitude_and_latitude = Tools::get_Longitude_and_latitude($store_id ,$address_xx);
            $longitude = $Longitude_and_latitude['longitude'];
            $latitude = $Longitude_and_latitude['latitude'];
        }

        if($is_default == 1)
        {
            Db::name('mch_store')->where(['store_id'=>$store_id,'mch_id'=>$shop_id,'is_default'=>1])->update(['is_default' => 0]);
        }
        else
        {
            $r0_1 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'id'=>$id])->field('is_default')->select()->toArray();
            if($r0_1[0]['is_default'] == 1)
            {
                Db::name('mch_store')->where(['store_id'=>$store_id,'mch_id'=>$shop_id])->limit(1)->order('id','asc')->update(['is_default' => '1']);
            }
        }

        $data1 = array('name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'business_hours'=>$business_hours,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'longitude'=>$longitude,'latitude'=>$latitude,'is_default'=>$is_default,'code'=>$code);
        $r1 = Db::name('mch_store')->where(['store_id'=>$store_id,'mch_id'=>$shop_id,'id'=>$id])->update($data1);
        if ($r1 < 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '编辑门店失败！';
            $this->mchLog($Log_content);
            $message = Lang('Busy network');
            return output(103,$message);
        }
        else
        {   
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '编辑门店成功！';
            $this->mchLog($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 删除我的门店
    public function Del_store()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = trim($this->request->param('id')); // 门店ID
        $id_list = explode(',',$id);
        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        Db::startTrans();

        foreach ($id_list as $k => $v)
        {
            $r1 = Db::table('lkt_mch_store')->where(['store_id'=>$store_id,'mch_id'=>$shop_id,'id'=>$v])->delete();
            if ($r1 < 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '删除门店失败！';
                $this->mchLog($Log_content);
                Db::rollback();
                $message = Lang('Busy network');
                return output(103,$message);
            }
        }

        $r3 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'is_default'=>1])->field('id')->select()->toArray();
        if(empty($r3))
        {
            Db::name('mch_store')->where(['store_id'=>$store_id,'mch_id'=>$shop_id])->limit(1)->order('id','asc')->update(['is_default' => '1']);
        }

        Db::commit();
        $message = Lang('Success');
        return output(200,$message);
    }

    // 核销时间管理
    public function getAppointmenTime()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $mch_id = trim($this->request->param('mch_id')); // 店铺ID
        $mch_store_id = trim($this->request->param('mchStoreId')); // 店铺门店ID

        $pege = trim($this->request->param('pageNo')); // 加载次数
        $pageSize = 10; // 每页多少条数据

        $start = ($pege - 1) * $pageSize;

        $total = 0;
        $sql_total = "select count(id) as total from lkt_mch_store_write where mch_id = '$mch_id' and mch_store_id = '$mch_store_id' and recycle = 0 ";
        $r_total = Db::query($sql_total);
        if($r_total)
        {
            $total = $r_total[0]['total'];
        }

        $list = array();
        $sql0 = "select id,start_time,end_time,write_off_num from lkt_mch_store_write where mch_id = '$mch_id' and mch_store_id = '$mch_store_id' and recycle = 0 order by id asc limit $start,$pageSize ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $start_time = $v0['start_time'];
                $end_time = $v0['end_time'];

                $startDate = date("Y-m-d",strtotime($start_time));
                $startTime = date("H:i",strtotime($start_time));
                $endDate = date("Y-m-d",strtotime($end_time));
                $endTime = date("H:i",strtotime($end_time));

                $notDelete = 0; // 该时间分段下有订单不能删除时间段 0.可以删除 1.不能删除
                $sql1 = "select id from lkt_order_details where mch_store_write_id = '$mch_store_id' and write_time >= '$start_time' and write_time <= '$end_time' ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    $notDelete = 1;
                }

                $list[] = array('id'=>$v0['id'],'startDate'=>$startDate,'startTime'=>$startTime,'endDate'=>$endDate,'endTime'=>$endTime,'num'=>$v0['write_off_num'],'notDelete'=>$notDelete);
            }
        }
        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加核销时间
    public function addAppointmenTime()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id

        $mch_id = trim($this->request->param('mch_id')); // 店铺ID
        $mch_store_id = trim($this->request->param('mch_store_id')); // 店铺门店ID
        $write_date = trim($this->request->param('write_date')); // 核销日期
        $write_time = trim($this->request->param('write_time')); // 核销时间
        $write_off_num = trim($this->request->param('write_off_num')); // 核销次数
        
        $time = date('Y-m-d H:i:s');
        $write_date_list = explode(',',$write_date);
        $write_time_list = explode(',',$write_time);

        $start_time = $write_date_list[0] . ' ' . $write_time_list[0];
        $end_time = $write_date_list[1] . ' ' . $write_time_list[1];

        $seconds = strtotime($end_time) - strtotime($start_time); // 两个时间戳之差
        $days = ceil($seconds / 3600 / 24);    // 计算天数，向上取整

        $off_num = '';
        for($i=1;$i<=$days;$i++)
        {
            if($i == 1)
            {
                $off_num .= '0';
            }
            else
            {
                $off_num .= ',0';
            }
        }

        if($write_time_list[0] >= $write_time_list[1])
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加的核销时间重叠！';
            $this->mchLog($Log_content);
            $message = Lang('mch.95');
            return output(51059,$message);
        }

        // $sql_0 = "select * from lkt_mch_store_write where store_id = '$store_id' and mch_id = '$mch_id' and mch_store_id = '$mch_store_id'";
        // $r_0 = Db::query($sql_0);
        // if($r_0)
        // {
        //     foreach($r_0 as $k_0 => $v_0)
        //     {
        //         $start_time_0 = $v_0['start_time'];
        //         $end_time_0 = $v_0['end_time'];

        //         if($start_time_0 < $start_time && $start_time < $end_time_0)
        //         { // 开始日期 < 新数据开始日期 && 新数据开始日期 < 结束日期 （新数据开始日期 是否存在其他日期中）
        //             $Log_content = __METHOD__ . '->' . __LINE__ . '添加的核销时间重叠！';
        //             $this->mchLog($Log_content);
        //             $message = Lang('mch.95');
        //             return output(51059,$message);
        //         }
        //         else if($start_time_0 < $end_time && $end_time < $end_time_0)
        //         { // 开始日期 < 新数据结束日期 && 新数据结束日期 < 结束日期（新数据结束日期 是否存在其他日期中）
        //             $Log_content = __METHOD__ . '->' . __LINE__ . '添加的核销时间重叠！';
        //             $this->mchLog($Log_content);
        //             $message = Lang('mch.95');
        //             return output(51059,$message);
        //         }
        //         else if($start_time_0 > $start_time && $end_time > $end_time_0)
        //         { // 开始日期 > 新数据开始日期 && 新数据结束日期 > 结束日期（新数据日期 包含其他日期）
        //             $Log_content = __METHOD__ . '->' . __LINE__ . '添加的核销时间重叠！';
        //             $this->mchLog($Log_content);
        //             $message = Lang('mch.95');
        //             return output(51059,$message);
        //         }
        //     }
        // }

        $sql0 = "insert into lkt_mch_store_write(store_id,mch_id,mch_store_id,start_time,end_time,period,write_off_num,off_num,add_time,recycle) value ('$store_id','$mch_id','$mch_store_id','$start_time','$end_time','','$write_off_num','$off_num','$time',0)";
        $r0 = Db::execute($sql0);
        if ($r0 > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加核销时间成功！';
            $this->mchLog($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加核销时间失败！';
            $this->mchLog($Log_content);
            $message = Lang('Busy network');
            return output(103,$message);
        }
    }

    // 删除核销时间
    public function deleteAppointmenTime()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id

        $id = trim($this->request->param('id')); // ID
        
        $sql0 = "select user_id from lkt_user where store_id = '$store_id' and access_id = '$access_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $user_id = $r0[0]['user_id'];

            $sql1 = "select id from lkt_mch where store_id = '$store_id' and user_id = '$user_id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $mch_id = $r1[0]['id'];

                $sql2 = "update lkt_mch_store_write set recycle = 1 where store_id = '$store_id' and mch_id = '$mch_id' and id = '$id' ";
                $r2 = Db::execute($sql2);
                if ($r2 > 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加核销时间成功！';
                    $this->mchLog($Log_content);
                    $message = Lang('Success');
                    return output(200,$message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加核销时间失败！';
                    $this->mchLog($Log_content);
                    $message = Lang('Busy network');
                    return output(103,$message);
                }
            }
            else
            {
                $message = Lang('Parameter error');
                return output(103,$message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(103,$message);
        }
    }

    // 门店管理员列表
    public function StoreAdminList()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $mch_store_id = addslashes($this->request->param('mch_store_id')); // 门店ID
        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页多少条数据
        $page = $page ? $page : '1';
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $con = " store_id = '$store_id' and mch_id = '$shop_id' and mch_store_id = '$mch_store_id' and recycle = 0 ";

        $total = 0;
        $list = array();

        $sql0 = "select count(id) as total from lkt_mch_admin where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select * from lkt_mch_admin where $con order by add_date desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $key => $value) 
            {
                $r1[$key]['password'] = Tools::unlock_url($value['password']);
                if($value['last_time'] == '')
                {
                    $value['last_time'] = '';
                }
                $list[] = $value;
            }
        }
        
        $data = array('list' => $list, 'total' => $total);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加/编辑门店管理员
    public function AddStoreAdmin()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        
        $id = addslashes($this->request->param('id')); // 管理员ID
        $mch_store_id = addslashes($this->request->param('mch_store_id')); // 门店ID
        $account_number = addslashes(trim($this->request->param('account_number'))); // 账号
        $password = addslashes(trim($this->request->param('password'))); // 密码
        $time = date("Y-m-d H:i:s");

        if($account_number == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '管理员账号不能为空';
            $this->mchLog($Log_content);
            $message = Lang('store.9');
            return output(109, $message);
        }
        else
        {
            if($id != '' && $id != 0)
            {
                $sql0 = "select id from lkt_mch_admin where store_id = '$store_id' and mch_id = '$shop_id' and mch_store_id = '$mch_store_id' and account_number = '$account_number' and id != '$id' ";
            }
            else
            {
                $sql0 = "select id from lkt_mch_admin where store_id = '$store_id' and mch_id = '$shop_id' and mch_store_id = '$mch_store_id' and account_number = '$account_number' ";
            }
            $r0 = Db::query($sql0);
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '管理员账号重复';
                $this->mchLog($Log_content);
                $message = Lang('store.10');
                return output(109, $message);
            }
        }

        if (strlen($password) < 6)
        {
            $message = Lang("user.0");
            return output(ERROR_CODE_MMBFHGF,$message);
        }
        else
        {
            $password = Tools::lock_url($password);
        }

        if($id != '' && $id != 0)
        {
            $sql1_update = array('account_number'=>$account_number,'password'=>$password);
            $sql1_where = array('store_id'=>$store_id,'mch_id'=>$shop_id,'id'=>$id);
            $r1 = Db::name('mch_admin')->where($sql1_where)->update($sql1_update);
            if ($r1 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '编辑门店管理员失败！';
                $this->mchLog($Log_content);
                $message = Lang('Busy network');
                return output(109, $message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '编辑门店管理员成功！';
                $this->mchLog($Log_content);
                $message = Lang('Success');
                return output(200, $message);
            }
        }
        else
        {
            $sql1 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'mch_store_id'=>$mch_store_id,'account_number'=>$account_number,'password'=>$password,'add_date'=>$time);
            $r1 = Db::name('mch_admin')->insertGetId($sql1);
            if ($r1 > 0)
            {   
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加门店管理员成功！';
                $this->mchLog($Log_content);
                $message = Lang('Success');
                return output(200, $message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加门店管理员失败！';
                $this->mchLog($Log_content);
                $message = Lang('Busy network');
                return output(109, $message);
            }
        }
    }

    // 删除门店管理员
    public function DelStoreAdmin()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        
        $id = addslashes($this->request->param('id')); // 管理员ID
        $mch_store_id = addslashes($this->request->param('mch_store_id')); // 门店ID

        $sql0 = "update lkt_mch_admin set recycle = 1 where store_id = '$store_id' and mch_id = '$shop_id' and mch_store_id = '$mch_store_id' and id = '$id' and recycle = 0 ";
        $r0 = Db::execute($sql0);
        if($r0 > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '删除门店管理员成功！';
            $this->mchLog($Log_content);
            $message = Lang('Success');
            return output(200, $message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '删除门店管理员失败！';
            $this->mchLog($Log_content);
            $message = Lang('Busy network');
            return output(109, $message);
        }
    }

    // 验证提货码
    public function OrderInfoForCode()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = trim($this->request->param('order_id')); // 订单id
        $extraction_code = trim($this->request->param('extraction_code')); // 提货码
        $mch_store_id = trim($this->request->param('write_shop_id')); // 门店ID
        
        $time = date("Y-m-d H:i:s");
        $mch = new MchPublicMethod();

        $por_lista = array();
        // 根据微信id,查询用户id
        $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if ($r0)
        {
            $user_id = $r0[0]['user_id'];

            $array = array('store_id'=>$store_id,'store_type'=>$store_type,'id'=>$id,'extraction_code'=>$extraction_code,'shop_id'=>$shop_id,'mch_store_id'=>$mch_store_id);
            $data = DeliveryHelper::Self_pickup_order_to_obtain_goods($array);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $access_id . '错误！';
            $mch->mchLog($Log_content);
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
    }

    // 确认核销
    public function verification_extraction_code()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = trim($this->request->param('order_id')); // 订单id
        $extraction_code = trim($this->request->param('extraction_code')); // 提货码
        
        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'id'=>$id,'extraction_code'=>$extraction_code,'shop_id'=>$shop_id,'operator'=>'','source'=>3);
        $data = DeliveryHelper::VerificationExtractionCode($array);
    }

    // 扫码提货码
    public function Sweep_extraction_code()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = trim($this->request->param('order_id')); // 订单id
        $extraction_code = trim($this->request->param('extraction_code')); // 提货码

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'id'=>$id,'extraction_code'=>$extraction_code,'shop_id'=>$shop_id,'operator'=>'','source'=>3);
        $data = DeliveryHelper::VerificationExtractionCode($array);
    }

    // 店铺订单设置
    public function MchIndex()
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
        else
        {
            $list = array('package_settings'=>0,'same_piece'=>0,'same_order'=>0);
        }
        $message = Lang("Success");
        return output(200,$message,$list);
    }

    // 保存店铺订单设置
    public function MchSaveConfig()
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
            if($res < 0)
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

    // 订单结算
    public function GetSettlementOrderList()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $status = trim($this->request->param('status')); // 1.已结算 0.未结算
        $startDate = trim($this->request->param('startDate')); // 开始时间
        $endDate = trim($this->request->param('endDate'));  // 结束时间
        $pageto = trim($this->request->param('pageto')); 
        $pagesize = trim($this->request->param('pagesize')); // 每页多少条数据
        $page = trim($this->request->param('page')); // 页码
        
        $user_id = $this->user_list['user_id'];

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $mch_id = 0;
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->select()->toArray();
        if($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
            $mch_id0 = ',' . $mch_id . ',';
        }

        $settlementPrice = 0;

        $con = "o.store_id = '$store_id' AND o.recycle = 0 AND o.settlement_status = '$status' AND o.status in (5,7) AND o.mch_id = '$mch_id0' AND p.gongyingshang = '0' and o.otype = 'GM' ";
        if($startDate != '')
        {
            $con .= " and o.add_time >= '$startDate' ";
        }
        if($endDate != '')
        {
            $con .= " and o.add_time <= '$endDate' ";
        }

        $sql_0 = "SELECT ifnull(SUM(o.z_price), 0) z_price,ifnull(SUM(o.z_freight), 0) z_freight FROM lkt_order AS o
                    LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id 
                    RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo
                    RIGHT JOIN lkt_configure attr ON attr.id = d.sid
                    RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid
                    RIGHT JOIN lkt_mch AS m ON p.mch_id = m.id  WHERE $con";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $settlementPrice = $r_0[0]['z_price'];
        }
       
        $total = 0;
        $sql0 = "select count(o.id) as total
        from lkt_order as o
        left join lkt_user as lu on o.user_id = lu.user_id
        right join lkt_order_details as d on o.sNo = d.r_sNo
        RIGHT JOIN lkt_configure attr on attr.id=d.sid
        right join lkt_product_list as p on p.id = attr.pid
        right join lkt_mch as m on p.mch_id = m.id
        left join lkt_payment pay on pay.class_name = o.pay where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $list = array();
        $sql1 = "select d.id as detailId,d.p_id,d.sid,o.real_sno,o.id,o.consumer_money,o.num,o.sNo,o.name,o.sheng,o.shi,o.xian,o.source,o.address,o.add_time,o.mobile,o.z_price,o.z_freight,o.status,o.reduce_price,o.coupon_price,o.preferential_amount,o.allow,o.drawid,o.otype,o.ptstatus,o.spz_price,o.pay,o.drawid,lu.user_name,o.user_id,o.mch_id,o.p_sNo,m.id as shop_id,m.name as shopName,o.arrive_time,o.settlement_status,o.operation_type,d.p_name product_title,attr.attribute,attr.img,d.num as needNum,o.self_lifting,o.is_lssued,d.express_id,d.courier_num,d.freight,attr.price goodsAmt,d.after_discount,pay.name as pay,d.commission,o.old_total
        from lkt_order as o
        left join lkt_user as lu on o.user_id = lu.user_id
        right join lkt_order_details as d on o.sNo = d.r_sNo
        RIGHT JOIN lkt_configure attr on attr.id=d.sid
        right join lkt_product_list as p on p.id = attr.pid
        right join lkt_mch as m on p.mch_id = m.id
        left join lkt_payment pay on pay.class_name = o.pay where $con order by o.add_time desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $sNo = $v['sNo'];
                $v['after_discount'] = round($v['after_discount'],2);
                $v['coupon_price'] = round($v['coupon_price'],2);
                $v['freight'] = round($v['freight'],2);
                $v['goodsAmt'] = round($v['goodsAmt'],2);
                $v['preferential_amount'] = round($v['preferential_amount'],2);
                $v['reduce_price'] = round($v['reduce_price'],2);
                $v['spz_price'] = round($v['spz_price'],2);
                $v['z_freight'] = round($v['z_freight'],2);
                $v['z_price'] = round($v['z_price'],2);
                $v['settlementPrice'] = round($v['z_price'],2);
                $v['mch_discount'] = $v['coupon_price'] - $v['preferential_amount'];
                
                $v['goodsNum'] = 0;
                $sql2 = "select count(id) as goodsNum from lkt_order_details where r_sNo = '$sNo' ";
                $r2 = Db::query($sql2);
                if($r2)
                {
                    $v['goodsNum'] = $r2[0]['goodsNum'];
                }
                
                $v['status_name'] = '未结算';
                if($v['settlement_status'] == 1)
                {
                    $v['status_name'] = '已结算';
                }

                $v['r_commission'] = '0.00'; // 退还佣金
                if($v['z_price'] == '0.00' || $v['z_price'] == 0)
                {
                    $v['r_commission'] = $v['commission'];
                    $v['commission'] = '0.00';
                }
                if($v['old_total'] == '0.00' || $v['old_total'] == 0)
                {
                    $v['old_total'] = $v['z_price'];
                }
                $v['return_money'] = $v['old_total'] - $v['z_price']; // 退单金额
                $list[] = $v;
            }
        }

        $data = array('list' => $list, 'total' => $total, 'settlementPrice' => $settlementPrice);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 我的订单-普通
    public function My_order()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $order_type = trim($this->request->param('order_type')); 
        $keyword = trim($this->request->param('keyword')); // 订单号
        $platform_activities_id = trim($this->request->param('platform_activities_id')); // 平台活动ID
        $order_headr_type = trim($this->request->param('order_headr_type')); // 订单类型
        $page = trim($this->request->param('page')); // 页码

        $start = 0;
        $end = 10;
        if (!empty($page) || $page != '')
        {
            $start = ($page - 1) * 10;
        }

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $action = array('store_id'=>$store_id,'shop_id'=>$shop_id,'order_type'=>$order_type,'keyword'=>$keyword,'platform_activities_id'=>$platform_activities_id,'page'=>$page,'user_id'=>$user_id,'start'=>$start,'end'=>$end,'Servertype'=>'a_mch','order_headr_type'=>$order_headr_type);
        $data = DeliveryHelper::a_mch_order_index($action);
        $list = $data['list'];
        $payment_num = $data['payment_num'];
        $send_num = $data['send_num'];
        $return_num = $data['return_num'];

        $data = array('list' => $list, 'payment_num' => $payment_num, 'send_num' => $send_num, 'return_num' => $return_num);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 订单详情
    public function Order_details()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $sNo = trim($this->request->param('sNo')); 

        $user_id = $this->user_list['user_id'];
        $arr = array();

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $action = array('store_id'=>$store_id,'shop_id'=>$shop_id,'user_id'=>$user_id,'sNo'=>$sNo);
        $data = DeliveryHelper::mch_order_details($action);
    }

    // 发货列表显示
    public function Deliver_show()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = trim($this->request->param('id'));  // 订单号
        $logistics_type = false; // false：获取lkt_express  true：获取lkt_express_subtable

        $ret = array();
        $sql = "select o.p_sNo,o.coupon_price,o.coupon_id,o.otype,d.id,d.r_sNo,d.user_id,d.p_id,d.p_name,d.p_price,d.num,d.r_status,d.sid,d.size,p.imgurl,o.otype ,o.ptcode ,p.product_class,b.brand_name,c.costprice,c.price,c.img,d.deliver_num,o.mch_id
                from lkt_order_details as d 
                left join lkt_configure as c on d.sid = c.id
                left join lkt_product_list as p on p.id = d.p_id 
                left join lkt_order as o on d.r_sNo = o.sNo 
                left join lkt_brand_class as b on p.brand_id = b.brand_id
                where d.store_id = '$store_id' and d.r_sNo = '$id' and d.r_status = 1";
        $res = Db::query($sql);
        if (!empty($res))
        {
            $mch_id = trim($res[0]['mch_id'],',');
            $logistics_type = PC_Tools::Determine_logistics_type($store_id,$mch_id);
            foreach ($res as $k => $v)
            {
                $v['costprice'] = round($v['costprice'],2);
                $v['coupon_price'] = round($v['coupon_price'],2);
                $v['p_price'] = round($v['p_price'],2);
                $v['price'] = round($v['price'],2);
                $v['deliverNum'] = $v['num'] - $v['deliver_num'];
                if ($v['otype'] == 'pt')
                {
                    $cost_res = GroupOpenModel::where(['store_id'=>$store_id,'ptcode'=>$v['ptcode']])->field('group_data')->select()->toArray();
                    if ($cost_res)
                    {
                        $v['p_price'] = 0;
                        $group_data = unserialize($cost_res[0]['group_data']);
                        if (isset($group_data->cost_price))
                        {
                            $cost_price = $group_data->cost_price;
                            $v['p_price'] = $cost_price * $v['num'];
                        }
                    }
                }

                $v['imgurl'] = ServerPath::getimgpath($v['img'], $store_id);
                $class_arr = explode('-', $v['product_class']);
                $class = $class_arr[count($class_arr) - 2];

                $class_res = ProductClassModel::where(['cid'=>$class])->select()->toArray();
                if (!empty($class_res))
                {
                    $v['class_name'] = $class_res[0]['pname'];
                }
                if ($v['otype'] == 'integral')
                {
                    $integralid = $v['p_sNo'];
                    $sql = "select g.integral,g.money,c.img from lkt_integral_goods as g left join lkt_configure as c on g.attr_id = c.id where g.id='$integralid'";
                    $inr = Db::query($sql);
                    if ($inr)
                    {
                        $v['p_integral'] = $inr[0]['integral'];
                        $v['p_price'] = $inr[0]['money'];
                        $v['imgurl'] = ServerPath::getimgpath($inr[0]['img']);
                    }
                }
                // 原来显示成本价 现在显示售价
                $v['costprice'] = $v['price'];
                $res[$k] = $v;
                
                $oid = $v['id'];//详情ID
                //判断售后情况
                $res_s = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->whereIn('r_type','0,1,3,11')->select()->toArray();
                if($res_s)
                {
                    unset($res[$k]);
                }
            }

            $ret['goods'] = array_values($res);
        }

        $ret['logistics_type'] = $logistics_type;
        $message = Lang('Success');
        return output(200,$message,$ret);
    }

    // 点击发货按钮-弹出填写发货信息
    public function Into_send()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $r1 = ExpressModel::where(['is_open'=>1,'recycle'=>0])->order('sort','desc')->select()->toArray();

        $data = array('list' => $r1,'total' => count($r1));
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 获取物流信息
    public function GetLogistics()
    {
        $store_id = addslashes($this->request->param('store_id'));

        $sNo = $this->request->param('sNo'); // 订单号

        $Express = new ExpressPublicMethod();
        $Express_list = $Express->GetLogistics(array('store_id'=>$store_id,'sNo'=>$sNo));
        return;
    }

    // 发货
    public function Send()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $sNo = trim($this->request->param('sNo')); // 订单号
        $express_id = trim($this->request->param('express_id')); // 快递公司ID
        $courier_num = trim($this->request->param('courier_num')); // 快递单号
        $orderList_id = trim($this->request->param('orderList_id')); // 发货数组

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $time = date('Y-m-d H:i:s', time());
        // $count = 0; //统计详细订单记录数
        // $batchSend = false; //是否批量发货
        // $len = 0; //选择的订单数
        $data_array = array();
        // $update_data = array();
        if (!empty($express_id))
        {
            // $update_data['express_id'] = $express_id;
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,未选择快递公司！';
            $this->mchLog($Log_content);
            $message = Lang('mch.23');
            return output(109,$message);
        }
        if (!empty($courier_num))
        {
            // if (strlen($courier_num) > 10 && strlen($courier_num) < 20)
            // {
            //     $r1 = OrderDetailsModel::where(['express_id'=>$express_id,'courier_num'=>$courier_num])->field('id')->select()->toArray();
            //     if ($r1)
            //     {
            //         $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,快递单号已存在！';
            //         $this->mchLog($Log_content);
            //         $message = Lang('mch.24');
            //         return output(109,$message);
            //     }
            //     else
            //     {
            //         $update_data['courier_num'] = $courier_num;
            //     }
            // }
            // else
            // {
            //     $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,快递单号输入错误！';
            //     $this->mchLog($Log_content);
            //     $message = Lang('mch.25');
            //     return output(109,$message);
            // }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,未填写快递单号！';
            $this->mchLog($Log_content);
            $message = Lang('mch.26');
            return output(109,$message);
        }
        // $update_data['deliver_time'] = $time;

        // if ($orderList_id == '')
        // {
        //     // 订单列表发货
        //     // 根据商城ID、订单号，查询订单详情ID
        //     $r0_0 = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->field('id')->select()->toArray();
        //     if ($r0_0)
        //     {
        //         foreach ($r0_0 as $k => $v)
        //         {
        //             $order_details_list[] = $v['id'];
        //         }
        //     }
        //     $order_details_id = implode(',', $order_details_list);
        //     $len = count($order_details_list);
        // }
        // else
        // {
        //     $order_details_id = $orderList_id;
        // }
        // 根据订单号，查询是否存在退货的商品
        $r2 = OrderModel::where(['sNo'=>$sNo])->field('otype')->select()->toArray();
        if ($r2)
        {
            $otype = $r2[0]['otype'];

            // $data_array = array('store_id'=>$store_id,'access_id'=>$access_id,'user_id'=>$user_id,'courier_num'=>$courier_num,'express_id'=>$express_id,'len'=>$len,'order_details_id'=>$order_details_id,'sNo'=>$sNo,'con'=>$update_data,'delivery_type'=>'front');
            $data_array = array('store_id'=>$store_id,'user_id'=>$user_id,'courier_num'=>$courier_num,'express_id'=>$express_id,'orderList_id'=>$orderList_id,'sNo'=>$sNo,'delivery_type'=>'front');
            $data = DeliveryHelper::frontDelivery_x($data_array);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,订单号错误,订单号为' . $sNo . '！';
            $this->mchLog($Log_content);
            $message = Lang('mch.27');
            return output(109,$message);
        }
    }

    // 商家配送订单-发货
    public function selfSend()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $sNo = trim($this->request->param('sNo')); // 订单号
        $courier_name = trim($this->request->param('courier_name')); // 配送人姓名
        $phone = trim($this->request->param('phone')); // 配送人电话

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $time = date('Y-m-d H:i:s', time());
        
        $data_array = array();
        if (!empty($courier_name))
        {
            
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,未选择快递公司！';
            $this->mchLog($Log_content);
            $message = Lang('order.37');
            return output(109,$message);
        }
        if (!empty($phone))
        {
            
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,未填写快递单号！';
            $this->mchLog($Log_content);
            $message = Lang('order.37');
            return output(109,$message);
        }
        
        // 根据订单号，查询是否存在退货的商品
        $r2 = OrderModel::where(['sNo'=>$sNo])->field('otype')->select()->toArray();
        if ($r2)
        {
            $otype = $r2[0]['otype'];

            $data_array = array('store_id'=>$store_id,'user_id'=>$user_id,'courier_num'=>'','express_id'=>'','orderList_id'=>'','sNo'=>$sNo,'courier_name'=>$courier_name,'phone'=>$phone,'delivery_type'=>'front');
            $data = DeliveryHelper::frontDelivery_x($data_array);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '发货时,订单号错误,订单号为' . $sNo . '！';
            $this->mchLog($Log_content);
            $message = Lang('mch.27');
            return output(109,$message);
        }
    }

    // 关闭订单
    public function Closing_order()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $sNo = trim($this->request->param('sNo')); // 订单号

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        Db::startTrans();

        $r1 = Db::name('order_details')->where(['store_id'=>$store_id,'r_sNo'=>$sNo])->update(['r_status'=>'7']);
        if (!$r1)
        {
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '关闭订单时，修改订单详情状态失败，订单号为' . $sNo . '！';
            $this->mchLog($Log_content);
            $message = Lang('Busy network');
            return output(103,$message);
        }

        $r2 = Db::name('order')->where(['store_id'=>$store_id,'sNo'=>$sNo])->update(['status'=>'7']);
        if (!$r2)
        {
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '关闭订单时，修改订单状态失败，订单号为' . $sNo . '！';
            $this->mchLog($Log_content);
            $message = Lang('Busy network');
            return output(103,$message);
        }

        $r3 = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->field('p_id,num,sid')->select()->toArray();
        if ($r3)
        {
            $p_id = $r3[0]['p_id'];
            $num = $r3[0]['num'];
            $sid = $r3[0]['sid'];

            Db::name('product_list')->where(['store_id'=>$store_id,'id'=>$p_id])->update(['num'=>Db::raw('num+'.$num)]);

            Db::name('configure')->where(['id'=>$sid])->update(['num'=>Db::raw('num+'.$num)]);
        }
        Db::commit();
        $message = Lang('Success');
        return output(200,$message);
    }

    // 修改订单
    public function Up_order()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $sNo = trim($this->request->param('sNo')); // 订单号
        $data = trim($this->request->param('orderDetail')); // 数据

        $data = htmlspecialchars_decode($data);
        $list = json_decode($data, true);
        
        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $data = array();
        $r1 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->whereLike('mch_id', '%,'.$shop_id.',%')->field('id')->select()->toArray();
        if ($r1)
        {
            $area = !empty($list['area']) ? explode('-', $list['area']) : [];
            $data['sheng'] = $area[0] ?? '';
            $data['shi']   = $area[1] ?? '';
            $data['xian']  = $area[2] ?? '';
            $data['name'] = $list['name'];
            $data['mobile'] = $list['mobile'];
            $data['address'] = $list['address'];
            $data['cpc'] = $list['cpc'] ?? '86';
            $data['z_price'] = $list['z_price'];//订单总价
            $data['remarks'] = $list['remarks'];//订单总价
            $data['delivery_time'] = $list['delivery_time']; // 配送时间
            $data['delivery_period'] = $list['delivery_period']; // 配送时间段 1.上午 2.下午
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '修改商品属性时，订单号错误，订单号为' . $sNo . '！';
            $this->mchLog($Log_content);
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
        if(empty($data))
        {
            $message = Lang('Busy network');
            return output(115,$message);
        }
        else
        {
            $res = EditOrderStatus::update_order($store_id,$sNo,'merchant',$data);
        }
    }

    // 取消订单
    public function CancellationOfOrder()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $sNo = $this->request->param('sNo'); // 订单号

        $array = array('store_id'=>$store_id,'mch_id'=>$shop_id,'sNo'=>$sNo,'operator_source'=>3);
        RefundUtils::CancellationOfOrder($array);
        return;
    }
    
    // 售后详情数据
    public function Returndetail()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $sNo = trim($this->request->param('sNo')); // 订单号
        $id = trim($this->request->param('id')); // 售后ID
        
        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $goods_list = array();
        //查询详情订单信息
        $sql0 = "select a.*,b.p_name,d.img,b.p_price,b.num,b.size from lkt_return_order as a left join lkt_order_details as b on a.p_id = b.id left join lkt_product_list as c on b.p_id = c.id left join lkt_configure as d on b.sid = d.id where a.store_id = '$store_id' and a.id = '$id' and a.sNo = '$sNo' ";
        $r0 = Db::query($sql0);
        if(empty($r0))
        {
            $message = Tools::language($db, $store_id,$access_id,$language, 'Parameter_error');
            $this->output(115, $message);
            $message = Lang('Parameter error');
            return output(115,$message);
        }
        $r_type = $r0[0]['r_type'];//售后状态
        $re_type = $r0[0]['re_type'];//售后类型
        $audit_time = $r0[0]['audit_time'];//审核时间
        $details_id = $r0[0]['p_id'];//订单详情ID
        $goods_list['img'] = ServerPath::getimgpath($r0[0]['img'], $store_id);//产品图片
        $goods_list['p_name'] = $r0[0]['p_name'];//商品名称
        $goods_list['p_price'] = round($r0[0]['p_price'],2);//商品单价
        $goods_list['num'] = $r0[0]['num'];//数量
        $goods_list['size'] = $r0[0]['size'];//规格
        $send_info = array();
        $return_info = array();

        //查询买家回寄信息
        $r1 = ReturnGoodsModel::where(['store_id'=>$store_id,'oid'=>$details_id])->select()->toArray();
        if (!empty($r1))
        {
            $send_info = $r1[0];

            if (count($r1) > 1)
            {
                //查询卖家退换信息
                $return_info = $r1[1];
            }
        }

        $info = array();

        //退款信息
        $info['re_time'] = $r0[0]['re_time'];//申请时间
        $price = $r0[0]['real_money'];//退款金额
        $info['p_price'] = $price;//退款金额
        $info['r_content'] = $r0[0]['r_content'];//拒绝原因
        $info['re_apply_money'] = $r0[0]['re_apply_money'];//申请金额
        $info['re_type'] = $r0[0]['re_type'];//售后类型1
        $info['real_money'] = round($r0[0]['real_money'],2);//售后类型1
        $info['p_name'] = $r0[0]['p_name'];//售后商品名称
        $info['r_sNo'] = $r0[0]['sNo'];//售后订单
        $info['type'] = $r0[0]['r_type'];//售后类型
        $info['content'] = $r0[0]['content'];//退货原因
        $re_photo = unserialize($r0[0]['re_photo']);//凭证
        if($r_type == 15)
        {
            $info['content'] = '极速退款'; // 退货原因
        }

        $info['re_photo'] = array();
        if (!empty($re_photo))
        {
            foreach ($re_photo as $k => $v)
            {
                $info['re_photo'][$k] = ServerPath::getimgpath($v, $store_id); // 获取图片路径
            }
        }

        $sql_o = "select currency_symbol,exchange_rate,currency_code from lkt_order where store_id = '$store_id' and sNo = '$sNo' ";
        $r_o = Db::query($sql_o);
        if($r_o)
        {
            $currency_symbol = $r_o[0]['currency_symbol'];
            $exchange_rate = $r_o[0]['exchange_rate'];
            $currency_code = $r_o[0]['currency_code'];
        }

        $data = array('info' => $info, 'send_info' => $send_info, 'return_info' => $return_info,'goods_list'=>$goods_list,'r_type'=>$r_type,'re_type'=>$re_type,'audit_time'=>$audit_time,'price'=>$price,'currency_symbol'=>$currency_symbol,'exchange_rate'=>$exchange_rate,'currency_code'=>$currency_code);
        $message = Lang('Success');
        return output('200',$message,$data);
    }

    // 通过/拒绝
    public function Examine()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $language = trim($this->request->param('language')); // 语言
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID

        $id = trim($this->request->param('id')); // 售后订单ID
        $sNo = trim($this->request->param('sNo')); // 订单号
        $r_type = trim($this->request->param('r_type')); // 退货类型
        $price = trim($this->request->param('price')); // 退款金额
        $text = trim($this->request->param('text')); // 拒绝理由
        $express_id = trim($this->request->param('express_id')); // 快递公司编号
        $courier_num = trim($this->request->param('courier_num')); // 快递单号

        $user_id = $this->user_list['user_id'];

        // 验证店铺信息
        $mch = new mchPublicMethod();
        $mch->verification_mch($store_id, $user_id,$shop_id);

        $action = array('store_id'=>$store_id,'express_id'=>$express_id,'courier_num'=>$courier_num,'admin_name'=>$user_id,'id'=>$id,'m'=>$r_type,'text'=>$text,'price'=>$price,'mch_id'=>$shop_id);
        $data = RefundUtils::refund($action);
    }

    // 去除重复
    public function a_array_unique($array)
    { //写的比较好（写方法）
        $out = array(); //定义变量out为一个数组
        foreach ($array as $key => $value)
        { //将$array数组按照$key=>$value的样式进行遍历
            if (!in_array($value, $out))
            { //如果$value不存在于out数组中，则执行
                $out[$key] = $value; //将该value值存入out数组中
            }
        }
        return $out; //最后返回数组out
    }

    // 上传图片
    public function UploadImgs()
    {
        if(!empty($_SERVER) && ($_SERVER['REQUEST_METHOD'] == 'OPTIONS')) 
        {
            header('Access-Control-Allow-Headers:*');
            header('Access-Control-Allow-Methods: GET, POST, PUT,DELETE,OPTIONS,PATCH');
            exit;
        }
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $src_img = trim($this->request->param('src_img')); // 

        if ($store_type = 1 || $store_type = 7)
        {
            $store_type = '0';
        }
        elseif ($store_type = 2)
        {
            $store_type = 'app';
        }

        $imgUrls = array();
        $dir = $store_id . '/' . $store_type . '/' . date("Ymd") . '/';

        if (!empty($_FILES))
        { // 如果图片不为空

            $name = '';
            foreach ($_FILES as $key => $value)
            {
                $name = $key;
            }

            $error = $_FILES[$name]['error'];
            switch ($_FILES[$name]['error'])
            {
                case 0:
                    $msg = '';
                    break;
                case 1:
                    $msg = '超出了php.ini中文件大小';
                    break;
                case 2:
                    $msg = '超出了MAX_FILE_SIZE的文件大小';
                    break;
                case 3:
                    $msg = '文件被部分上传';
                    break;
                case 4:
                    $msg = '没有文件上传';
                    break;
                case 5:
                    $msg = '文件大小为0';
                    break;
                default:
                    $msg = '上传失败';
                    break;
            }

            $imgURL = $_FILES[$name]['tmp_name'];
            $files = $_FILES[$name]['name'];
            $pathinfo = pathinfo($files);
            
            $contentType = $_FILES[$name]['type'];
            $fType = explode('/', $contentType);
            
            $type = isset($pathinfo['extension'])?$pathinfo['extension']:$fType[1];

            $type_0 = PC_Tools::upload();

            $imgURL_name = time() . mt_rand(1, 1000) . '.' .$type;
            $path = $dir . $imgURL_name;
            
            $common = LKTConfigInfo::getOSSConfig();
            try
            {
                //查询文件上传配置方式
                $upserver = Db::name('config')->where('store_id', 1)->value('upserver');
                //阿里云
                if($upserver == '2')
                {
                    $ossClient = OSSCommon::getOSSClient();
                    $ossClient->uploadFile($common['bucket'], $path, $imgURL);
                }
                //MinIO
                if($upserver == '5')
                {
                    $ossClient = new MinIOServer();
                    $ossClient->upLoadObject($imgURL,$path,$contentType);
                }
            }
            catch (OssException $e)
            {
                printf(__FUNCTION__ . ": FAILED\n");
                printf($e->getMessage() . "\n");
                return;
            }

            if($upserver == '2')
            {
                $isopenzdy = $common['isopenzdy'];
                $url = 'https://' . $common['bucket'] . '.' . $common['endpoint'] . '/' . $path;
                if($isopenzdy == 1)
                {
                    $url = 'https://'. $common['MyEndpoint'] . '/' . $path;
                }
            }
            if($upserver == '5')
            {
                $url = 'http://' . $common['endpoint'] . '/' . $common['bucket'] . '/' . $path;
                if (strpos($common['endpoint'], "http") !== false) 
                {
                    $url = $common['endpoint'] . '/' . $common['bucket'] . '/' . $path;
                }
            }

            $fsql = " INSERT INTO `lkt_files_record` ( `store_id`, `store_type`, `group`, `upload_mode`, `image_name`,`mch_id`,`supplier_id`,`type`,`name`) VALUES ('$store_id', '$store_type', '-1', $upserver, '$imgURL_name','$shop_id','0','$type_0','$files') ";
            $res = Db::execute($fsql);
            
            if($src_img != '')
            {
                $collection_code = preg_replace('/.*\//', '', $url);
                $sql_where = array('store_id'=>$store_id,'id'=>$shop_id);
                $sql_update = array('collection_code' => $collection_code);
                $r = Db::name('mch')->where($sql_where)->update($sql_update);
            }
            ob_clean();
            $message = Lang('Success');
            return output(200,$message,$url);
        }
        else
        {
            ob_clean();
            $message = Lang('mch.4');
            return output(109,$message);
        }
    }

    // 店铺日志
    public function mchLog($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/mch.log",$Log_content);
        return;
    }

    // 验证身份证格式是否正确
    public function is_idcard($id)
    {
        $id = strtoupper($id);
        $regx = "/(^\d{15}$)|(^\d{17}([0-9]|X)$)/";
        $arr_split = array();
        if (!preg_match($regx, $id))
        {
            return false;
        }
        if (15 == strlen($id)) //检查15位
        {
            $regx = "/^(\d{6})+(\d{2})+(\d{2})+(\d{2})+(\d{3})$/";

            @preg_match($regx, $id, $arr_split);
            //检查生日日期是否正确
            $dtm_birth = "19" . $arr_split[2] . '/' . $arr_split[3] . '/' . $arr_split[4];
            if (!strtotime($dtm_birth))
            {
                return FALSE;
            }
            else
            {
                return TRUE;
            }
        }
        else      //检查18位
        {
            $regx = "/^(\d{6})+(\d{4})+(\d{2})+(\d{2})+(\d{3})([0-9]|X)$/";
            @preg_match($regx, $id, $arr_split);
            $dtm_birth = $arr_split[2] . '/' . $arr_split[3] . '/' . $arr_split[4];
            if (!strtotime($dtm_birth)) //检查生日日期是否正确
            {
                return FALSE;
            }
            else
            {
                //检验18位身份证的校验码是否正确。
                //校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。
                $arr_int = array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
                $arr_ch = array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
                $sign = 0;
                for ($i = 0; $i < 17; $i++)
                {
                    $b = (int)$id[$i];
                    $w = $arr_int[$i];
                    $sign += $b * $w;
                }
                $n = $sign % 11;
                $val_num = $arr_ch[$n];
                if ($val_num != substr($id, 17, 1))
                {
                    return FALSE;
                } //phpfensi.com
                else
                {
                    return TRUE;
                }
            }
        }
    }

    public function order_number()
    {
        $sNo = 'FK' . date("ymdhis") . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9);
        $res = CashierRecordModel::where(['out_trade_no'=>$sNo])->select()->toArray();
        if ($res)
        {
            $sNo = $this->order_number();
            return $sNo;
        }
        else
        {
            return $sNo;
        }
    }

    // 面单设置
    public function logistics_list()
    {   
        $store_id = trim($this->request->param('store_id'));
        $name = addslashes(trim($this->request->param('name'))); // 物流公司名称、编码
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据

        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'page'=>$page,'pagesize'=>$pagesize);

        $Express = new ExpressPublicMethod();
        $Express_list = $Express->logistics_list($array);

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }

    // 获取物流主表数据
    public function get_logistics()
    {   
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id

        $id = trim($this->request->param('id')); // id

        $sql0 = "select a.id from lkt_mch as a left join lkt_user as u on a.user_id = u.user_id where a.store_id = '$store_id' and a.review_status = 1 and a.recovery = 0 and a.is_lock = 0 and u.access_id = '$access_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $mch_id = $r0[0]['id'];
        }

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
        $Express = new ExpressPublicMethod();
        $Express_list = $Express->get_logistics($array);

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }

    // 添加/编辑快递公司子表
    public function add_logistics()
    {   
        $store_id = trim($this->request->param('store_id'));
        $id = addslashes(trim($this->request->param('id'))); // ID
        $express_id = addslashes(trim($this->request->param('express_id'))); // 主表ID
        $partnerId = addslashes(trim($this->request->param('partnerId'))); // 电子面单客户账户或月结账号
        $partnerKey = addslashes(trim($this->request->param('partnerKey'))); // 电子面单密码
        $partnerSecret = addslashes(trim($this->request->param('partnerSecret'))); // 电子面单密钥
        $partnerName = addslashes(trim($this->request->param('partnerName'))); // 电子面单客户账户名称
        $net = addslashes(trim($this->request->param('net'))); // 收件网点名称
        $code = addslashes(trim($this->request->param('code'))); // 电子面单承载编号
        $checkMan = addslashes(trim($this->request->param('checkMan'))); // 电子面单承载快递员名

        $admin_name = $this->user_list['user_name'];
        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $Express = new ExpressPublicMethod();

        if($id != 0 && $id != '')
        {
            $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'express_id'=>$express_id,'partnerId'=>$partnerId,'partnerKey'=>$partnerKey,'partnerSecret'=>$partnerSecret,'partnerName'=>$partnerName,'net'=>$net,'code'=>$code,'checkMan'=>$checkMan,'source'=>2,'admin_name'=>$admin_name,'id'=>$id);

            $Express_list = $Express->edit_logistics($array);
        }
        else 
        {
            $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'express_id'=>$express_id,'partnerId'=>$partnerId,'partnerKey'=>$partnerKey,'partnerSecret'=>$partnerSecret,'partnerName'=>$partnerName,'net'=>$net,'code'=>$code,'checkMan'=>$checkMan,'source'=>2,'admin_name'=>$admin_name);

            $Express_list = $Express->add_logistics($array);
        }

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }
    
    // 编辑快递公司子表页面
    public function edit_logistics_page()
    {   
        $store_id = trim($this->request->param('store_id'));
        $id = addslashes(trim($this->request->param('id'))); // ID

        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);

        $Express = new ExpressPublicMethod();
        $Express_list = $Express->edit_logistics_page($array);

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }

    // 删除快递公司子表
    public function del_logistics()
    {   
        $store_id = trim($this->request->param('store_id'));
        $id = addslashes(trim($this->request->param('id'))); // ID
        $admin_name = $this->user_list['user_name'];
        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id,'source'=>2,'admin_name'=>$admin_name);

        $Express = new ExpressPublicMethod();
        $Express_list = $Express->del_logistics($array);

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }

    // 电子面单
    public function ShippingRecords()
    {   
        $store_id = trim($this->request->param('store_id'));

        $express_name = $this->request->param('express_name'); // 快递单号、快递订单ID
        $sNo = $this->request->param('search'); // 订单号
        $status = $this->request->param('status'); // 是否打印 0.未打印 1.已打印
        $startdate = $this->request->param("startDate"); // 查询开始时间
        $enddate = $this->request->param("endDate"); // 查询结束时间
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据

        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $shop_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'express_name'=>$express_name,'sNo'=>$sNo,'mch_name'=>'','status'=>$status,'startdate'=>$startdate,'enddate'=>$enddate,'page'=>$page,'pagesize'=>$pagesize,'shop_id'=>$shop_id);

        $data = DeliveryHelper::ShippingRecords($array);
       
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 获取商品
    public function getPro()
    {   
        $store_id = trim($this->request->param('store_id'));

        $id = $this->request->param('id'); // 发货记录ID
        $name = $this->request->param('name'); // 商品名称
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据

        $array = array('store_id'=>$store_id,'id'=>$id,'name'=>$name,'page'=>$page,'pagesize'=>$pagesize);

        $data = DeliveryHelper::getPro($array);
       
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 面单发货
    public function FaceSheetSend()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $id = trim($this->request->param('orderList_id')); // 订单详情表id
        $express_id = trim($this->request->param('express_id'));//快递id
        $courier_num = trim($this->request->param('exNo'));//快递单号
        $user_id = $this->user_list['user_id'];

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'admin_name'=>$user_id,'id'=>$id,'express_id'=>$express_id,'courier_num'=>$courier_num,'source'=>'2');
        $data = DeliveryHelper::FaceSheetSend($array);

        exit;
    }

    // 取消电子面单
    public function CancelElectronicWaybill()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $id = trim($this->request->param('id')); // 发货记录id
        $user_id = $this->user_list['user_id'];

        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'admin_name'=>$user_id,'id'=>$id,'source'=>'2');
        $data = DeliveryHelper::CancelElectronicWaybill($array);

        exit;
    }

    // 统一发货
    public function UnifiedShipment()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
    	$access_id = addslashes($this->request->param('access_id'));

    	$list = trim($this->request->param('list'));
        $list = htmlspecialchars_decode($list);

        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $data_array = array('store_id'=>$store_id,'shop_id'=>$mch_id,'list'=>$list,'operator_id'=>0,'operator'=>'','source'=>3);
        $data = DeliveryHelper::UnifiedShipment($data_array);
        exit;
    }
}
