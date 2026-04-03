<?php
namespace app\admin\controller\app;

use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\ServerPath;
use app\common\mchPublicMethod;
use app\common\CouponPublicMethod;
use app\common\Plugin\Plugin;
// use app\common\badword;
use app\common\LaiKeLogUtils;

use app\admin\model\UploadSetModel;
use app\admin\model\UserModel;
use app\admin\model\UserCollectionModel;
use app\admin\model\ProductConfigModel;
use app\admin\model\ProductImgModel;
use app\admin\model\ProductListModel;
use app\admin\model\MchModel;
use app\admin\model\MchBrowseModel;
use app\admin\model\ProductClassModel;
use app\admin\model\BrandClassModel;
use app\admin\model\CommentsImgModel;
use app\admin\model\ReplyCommentsModel;
use app\admin\model\ConfigureModel;
use app\admin\model\CartModel;
use app\admin\model\CommentsModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\ConfigModel;
use app\admin\model\PreSellConfigModel;
use app\admin\model\PreSellGoodsModel;
use app\admin\model\IntegralGoodsModel;
use app\admin\model\DistributionGradeModel;
use app\admin\model\SecondsActivityModel;
use app\admin\model\SecondsLabelModel;
use app\admin\model\SecondsConfigModel;
use app\admin\model\SecondsRecordModel;
use app\admin\model\FlashsaleConfigModel;
use app\admin\model\FlashsaleLabelModel;
use app\admin\model\FlashsaleProModel;
use app\admin\model\FlashsaleActivityModel;

class Product
{
    // 获取产品详情
    public function index()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::post('access_id'))); // 授权id
        $language = addslashes(trim(Request::post('language'))); // 语言
        $id = addslashes(trim(Request::post('pro_id'))); // 商品id
        $vipSource = addslashes(trim(Request::post('vipSource'))); // 是否是会员商品 0.普通商品  1.会员商品
        $pro_type = trim(Request::post('type'));//商品类型IN，FX
        $active_id = trim(Request::post('id'));;//活动商品id
        $integralNum = trim(Request::post('integralNum'));//兑换积分

        $time = time();
        $current_time = date("Y-m-d :H:i:s");
        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种

        if ($access_id != '')
        {
            $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组
            if ($getPayload_test)
            { // 过期重新生成一个，不需要做登录提示。
                $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
                if ($r_user)
                {
                    $user_id = $r_user[0]['user_id']; // 用户id
                    $login_status = 1;
                    $grade = $r_user[0]['grade'];//会员等级id
                    // 根据用户id、产品id,获取收藏表信息
                    $r_collection = UserCollectionModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$id])->select()->toArray();
                    if ($r_collection)
                    {
                        $type = 1; // 已收藏
                        $collection_id = $r_collection['0']['id']; // 收藏id
                    }
                    else
                    {
                        $type = 0; // 未收藏
                        $collection_id = '';
                    }
                    // 根据用户id,在足迹表里插入一条数据
                    $data_footprint = array('store_id' => $store_id, 'user_id' => $user_id, 'p_id' => $id, 'add_time' => $current_time);
                    $r_footprint = Db::name('user_footprint')->save($data_footprint);
                    if ($r_footprint < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '添加足迹记录失败!参数:' . json_encode($data_footprint);
                        $this->Log($Log_content);
                        ob_clean();
                        $message = Lang('Unknown error');
                        return output(101,$message);
                    }
                }
                else
                {
                    $access_id = Tools::getToken($store_id,$store_type);
                    $user_id = '游客' . $time;
                    $type = 0; // 未收藏
                    $collection_id = '';
                    $login_status = 0;
                    $grade = '';
                }
            }
            else
            {
                $access_id = Tools::getToken($store_id,$store_type);
                $user_id = '游客' . $time;
                $type = 0; // 未收藏
                $collection_id = '';
                $login_status = 0;
                $grade = '';
            }
        }
        else
        {
            $access_id = Tools::getToken($store_id,$store_type);
            $grade = '';
            $user_id = '游客' . $time;;
            $type = 0; // 未收藏
            $collection_id = '';
            $login_status = 0;
        }

        if ($grade)
        {
            $is_grade = 1;//是会员
        }
        else
        {
            $is_grade = 0;//非会员
        }

        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>2));

        $product = array();
        $shop_list = array();
        $img_arr = array();
        $isAddCar = 1; // 1.能加入购物车 2.不能加入购物车
        $mchStoreListNum = 0; // 适用门店数
        $writeNumStatus = 2; // 1.可以预约 2.不可以预约
        // 根据商城ID、商品ID，查询商品信息
        $sql0 = "select tt.* from (SELECT a.*,min(c.price) over (partition by c.pid) as price,c.yprice,c.img,c.unit,c.attribute,row_number () over (partition by c.pid) as top FROM lkt_product_list AS a LEFT JOIN lkt_configure AS c ON a.id = c.pid WHERE a.store_id = '$store_id' AND a.id = '$id' AND c.recycle = 0 and a.mch_id != 0 ) as tt where tt.top < 2 ";
        $r0 = Db::query($sql0);
        if (empty($r0))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '商品ID错误！ID：' . $id;
            $this->Log($Log_content);
            $message = Lang('Unknown error');
            return output(101,$message);
        }
        else
        {
            $is_presell = $r0[0]['is_presell'];
            $upper_shelf_time = $r0[0]['upper_shelf_time'];
            $active = $r0[0]['active'];
            $mch_id = $r0[0]['mch_id'];
            if ($mch_id != 0)
            {
                $mch_list = PC_Tools::StoreData($store_id,$mch_id,$status);
                $shop_list = $mch_list;
                
                $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('id,name,logo,is_open,poster_img as posterImg,head_img as headimg')->select()->toArray();
                if ($r_mch)
                {
                    $shop_list['shop_id'] = $r_mch[0]['id'];
                    $shop_list['shop_name'] = $r_mch[0]['name'];
                    $shop_list['is_open'] = $r_mch[0]['is_open'];
                    $shop_list['shop_logo'] = ServerPath::getimgpath($r_mch[0]['headimg'],$store_id);
                    $shop_list['posterImg'] = ServerPath::getimgpath($r_mch[0]['posterImg'],$store_id);
                    $shop_list['shop_head'] = ServerPath::getimgpath($r_mch[0]['headimg'],$store_id);
                }

                // BUG-ID 1301
                // $data_mch_browse = array('store_id' => $store_id, 'token' => $access_id, 'mch_id' => $mch_id, 'user_id' => $user_id, 'event' => '访问了店铺', 'add_time' => $current_time);
                // $r_mch_browse = Db::name('mch_browse')->replace()->insert($data_mch_browse);
            }

            $commodity_type = $r0[0]['commodity_type'];
            $write_off_settings = $r0[0]['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
            $is_appointment = $r0[0]['is_appointment']; // 预约时间设置 1.无需预约下单 2.需要预约下单
            $write_off_mch_ids = $r0[0]['write_off_mch_ids']; // 核销门店id  0全部门店,  1,2,3使用逗号分割
            if($commodity_type == 1)
            { // 虚拟商品
                if($write_off_settings == 1)
                { // 线下核销
                    $isAddCar = 2; // 2.不能加入购物车

                    if($write_off_mch_ids == 0)
                    { // 全部
                        $write_off_mch_ids_list = array();
                        $sql_mch_store = "select id from lkt_mch_store where store_id = '$store_id' and mch_id = '$mch_id' ";
                        $r_mch_store = Db::query($sql_mch_store);
                        if($r_mch_store)
                        {
                            $mchStoreListNum = count($r_mch_store);
                            foreach($r_mch_store as $k_mch_store => $v_mch_store)
                            {
                                $write_off_mch_ids_list[] = $v_mch_store['id'];
                            }
                        }
                    }
                    else
                    {
                        $write_off_mch_ids_list = explode(',',$write_off_mch_ids);
                        $mchStoreListNum = count($write_off_mch_ids_list);
                    }

                    if($is_appointment == 1)
                    { // 无需预约下单
                        $writeNumStatus = 1; // 1.可以预约
                    }
                    else
                    { // 需要预约下单
                        $write_off_mch_ids_str = implode(',',$write_off_mch_ids_list);
                        // 根据商城ID、店铺ID、门店ID、还未结束的核销时间段，查询核销数据
                        $sql_mch_store_write = "select write_off_num,off_num from lkt_mch_store_write where store_id = '$store_id' and mch_id = '$mch_id' and mch_store_id in ($write_off_mch_ids_str) and end_time >= '$current_time' ";
                        $r_mch_store_write = Db::query($sql_mch_store_write);
                        if($r_mch_store_write)
                        {
                            foreach($r_mch_store_write as $k_mch_store_write => $v_mch_store_write)
                            {
                                $write_off_num_ = $v_mch_store_write['write_off_num']; // 可预约核销次数 0无限制
                                $off_num_ = $v_mch_store_write['off_num']; // 已预约核销次数

                                $off_num_list = explode(',',$off_num_);
                                foreach($off_num_list as $k_num => $v_num)
                                {
                                    if($v_num < $off_num_)
                                    { // 当前预约核销次数 < 已预约核销次数
                                        $writeNumStatus = 1; // 1.可以预约
                                    }
                                }
                            }
                        }
                    }
                }
            }

            $imgurl = ServerPath::getimgpath($r0[0]['imgurl'],$store_id); // 图片
            $product['photo_x'] = $imgurl;
            $product['coverImage'] = $imgurl;
            //有视频文件放第一个
            $video = $r0[0]['video']; // 视频文件
            if($video)
            {
                $img_arr[0] = $video;
                $img_arr[1] = $imgurl;  // 图片路径
            }
            else
            {
                $img_arr[0] = $imgurl;  // 图片路径
            }

            // 根据商品id,查询商品图片表
            $r_img = ProductImgModel::where('product_id',$id)->field('product_url')->select()->toArray();
            if ($r_img)
            {
                foreach ($r_img as $k_img => $v_img)
                {
                    $img_arr[] = ServerPath::getimgpath($v_img['product_url'],$store_id); // 图片路径
                }
            }
            $product['img_arr'] = $img_arr; // 图片路径

            $pname = '';
            $class = $r0[0]['product_class']; // 产品类别
            $typestr = trim($class, '-');
            $typeArr = explode('-', $typestr);
            //  取数组最后一个元素 并查询分类名称
            $cid = end($typeArr);
            // 根据商品类别,查询类别名称
            $r_p = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$cid])->field('pname')->select()->toArray();
            if ($r_p)
            {
                $pname = $r_p['0']['pname']; // 分类名称
            }
            else
            {
                array_pop($typeArr);
                $cid = end($typeArr);
                $r_p1 = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$cid])->field('pname')->select()->toArray();
                if($r_p1)
                {
                    $pname = $r_p1['0']['pname']; // 分类名称
                }
            }

            $product['brand_name'] = '无';
            if (!empty($r0[0]['brand_id']))
            {
                $b_id = $r0[0]['brand_id']; // 品牌id
                // 根据品牌id,查询品牌名称
                $r_brand = BrandClassModel::where(['store_id'=>$store_id,'brand_id'=>$b_id])->field('brand_name')->select()->toArray();
                if($r_brand)
                {
                    $product['brand_name'] = $r_brand[0]['brand_name']; // 品牌名称
                }
            }
            $Member_discount = array('store_id'=>$store_id,'access_id'=>$access_id);
            $grade_list= PC_Tools::Member_discount($Member_discount);
            $grade = $grade_list['grade'];
            $grade_rate = 1;
            //判断会员制折扣
            if($vipSource == 1)
            {
                $grade_rate = $grade_list['grade_rate'];
            }
            if($grade_rate != 1)
            {
                $product['grade_rate'] = $grade_rate * 10;
            }
            else
            {
                $product['grade_rate'] = $grade_rate;
            }
            $product['pro_id'] = $id; // 商品ID
            $product['user_id'] = $user_id; // 用户id
            $product['cat_name'] = $pname; // 分类名称
            $product['name'] = stripslashes($r0[0]['product_title']); // 商品名字
            $product['content'] = $r0[0]['content']; // 分类名称
            $product['num'] = $r0[0]['num']; // 数量
            $product['status'] = $r0[0]['status']; // 状态
            $product['price'] = round($r0[0]['price'],2); // 出售价格
            $product['yprice'] = round($r0[0]['yprice'],2); // 原价格
            $product['vip_yprice'] = round($r0[0]['price'],2);
            $product['vip_price'] = round($r0[0]['price'] * $grade_rate, 2);
            $product['video'] = $r0[0]['video']?$r0[0]['video']:''; // 视频文件
            $product['proVideo'] = $r0[0]['pro_video']?$r0[0]['pro_video']:''; // 商品视频
            // 出售价格
            $cs_price = round($r0[0]['price'],2);
            
            if ($r0[0]['unit'] != '')
            {
                $product['unit'] = $r0[0]['unit']; // 单位
            }
            else
            {
                $product['unit'] = '个';
            }

            $r0[0]['volume'] = $r0[0]['volume'] + $r0[0]['real_volume'];
            if ($r0[0]['volume'] < 0)
            {
                $r0[0]['volume'] = 0;
            }
            $product['volume'] = $r0[0]['volume']; // 销量
            $product['is_distribution'] = 0;
            $product['canbuy'] = 1;

            $product['yunfei'] = 0;
            $product['freight'] = 0;
            $product['freight_name'] = '免运费';
            $sel_freightSql = "SELECT f.name,f.default_freight FROM lkt_product_list as p LEFT JOIN lkt_freight as f on p.freight = f.id WHERE p.id = $id";
            $freightRes = Db::query($sel_freightSql);
            if($freightRes)
            {
                $freightRes0 = json_decode($freightRes[0]['default_freight'],true);
                
                $product['yunfei'] = isset($freightRes0['num2'])?$freightRes0['num2']:'';
                $product['freight'] = isset($freightRes0['num2'])?$freightRes0['num2']:'';
                $product['freight_name'] = $freightRes[0]['name'];
                $product['freight_price'] = isset($freightRes0['num2'])?floatval($freightRes0['num2']):0;
            }
            //获取积分商品信息
            if($pro_type == 'IN')
            {
                $active_res = IntegralGoodsModel::where('id',$active_id)->field('integral,money,status,max_num,num')->select()->toArray();
                if($active_res)
                {
                    $product['price'] = round($active_res[0]['money'],2);
                    $product['integralNum'] = $active_res[0]['integral'];
                    $product['max_num'] = $active_res[0]['max_num'];
                    $product['num'] = $active_res[0]['num'];
                    $product['status'] = $active_res[0]['status'];
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '商品ID错误！ID：' . $id;
                    $this->Log($Log_content);
                    $message = Lang('Unknown error');
                    return output(101,$message);
                }
            }
        }

        /* 属性开始 */
        $attribute_list = array();
        $attrList = array();
        $skuBeanList = array();
        $array_price = array();
        $array_yprice = array();
        $arrayName = array();
        $discount = 1;//分销折扣
        $buyNum = 0;
        // 根据商品ID，查询属性
        if($pro_type == 'FX')
        {   
            $product['is_distribution'] = 1;
            // 获取会员
            if ($login_status == 1)
            {
                $sql0_0 = "select a.level,b.sets,b.discount from lkt_user_distribution as a left join lkt_distribution_grade as b on a.level = b.id where a.store_id = '$store_id' and a.user_id = '$user_id' and a.level > 0";
                $r0_0 = Db::query($sql0_0);
                if ($r0_0)
                {
                    $sets = json_decode($r0_0[0]['sets'],true);
                    $direct_m_type = $sets['direct_m_type'];
                    $direct_m = $sets['direct_m'];
                    if($sets['zhekou'] == 1)
                    {
                        $discount = $r0_0[0]['discount'];
                    }
                }
                else
                {   
                    $r0_0 = DistributionGradeModel::where('store_id',$store_id)->field('sets')->order('id','desc')->limit(0,1)->select()->toArray();
                    if ($r0_0)
                    {
                        $sets = json_decode($r0_0[0]['sets'],true);
                        $direct_m_type = $sets['direct_m_type'];
                        $direct_m = $sets['direct_m'];
                    }
                }
            }
            else
            {
                $r0_0 = DistributionGradeModel::where('store_id',$store_id)->field('sets')->order('id','desc')->limit(0,1)->select()->toArray();
                if ($r0_0)
                {
                    $sets = json_decode($r0_0[0]['sets'],true);
                    $direct_m_type = $sets['direct_m_type'];
                    $direct_m = $sets['direct_m'];
                }
            }
            $product['vip_yprice'] = round($r0[0]['price'] * $discount,2);
            $product['vip_price'] = round($r0[0]['price'] * $discount, 2);

            $sql_size = "SELECT a.* from lkt_configure as a left JOIN lkt_distribution_goods as b on a.id = b.s_id where b.p_id = '$id' and a.recycle = 0 and b.recycle = 0";
            $r_size = Db::query($sql_size);
        }
        elseif($pro_type == 'MS')
        {
            $pro_id = trim(Request::post('id')); // 活动商品id
            $seckill_pro_res = SecondsActivityModel::where(['store_id'=>$store_id,'id'=>$pro_id,'isshow'=>1,'is_delete'=>0])->select()->toArray();
            if(empty($seckill_pro_res))
            {
                $message = Lang('sec.9');
                return output(ERROR_CODE_HDYJS,$message);
            }
            $sql_size = "select c.*,a.num as sec_num from lkt_configure as c left join lkt_seconds_activity as a on c.id = a.attr_id where c.recycle = 0 and a.id = '$pro_id'";
            $r_size = Db::query($sql_size);
        }
        elseif($pro_type == 'FS')
        {
            $pro_id = trim(Request::post('id')); // 活动商品id
            $fs_pro_res = FlashsaleActivityModel::where(['store_id'=>$store_id,'id'=>$pro_id,'is_delete'=>0])->select()->toArray();
            if(empty($fs_pro_res))
            {
                $message = Lang('sec.9');
                return output(ERROR_CODE_HDYJS,$message);
            }
            $buyNum = $fs_pro_res[0]['buylimit'];

            $r_size = ConfigureModel::where(['pid'=>$id,'recycle'=>0])->select()->toArray();
        }
        elseif($pro_type == 'IN')
        {
            $pro_id = trim(Request::post('id')); // 积分商品ID
            $sql_i = "select c.*,a.num as sec_num,a.integral from lkt_integral_goods as a left join lkt_configure as c on a.attr_id = c.id where a.id = '$pro_id' ";
            $r_size = Db::query($sql_i);
        }
        else
        {
            $r_size = ConfigureModel::where(['pid'=>$id,'recycle'=>0])->select()->toArray();
        }
        if ($r_size)
        {   
            if($pro_type == 'MS')
            {   
                $price_type = $seckill_pro_res[0]['price_type'];//秒杀价格单位 0=百分比 1=固定值
                $seconds_price = $seckill_pro_res[0]['seconds_price'];//秒杀价格值
                // 货币转换
                foreach ($r_size as $key => $value)
                {
                    $attnum = 0;
                    $attributes = array();
                    $name = '';

                    // // 货币转换
                    // $value['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$value['price']));
                    // $value['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$value['yprice']));
                    $value['price'] = round($value['price'],2);
                    $value['yprice'] = round($value['yprice'],2);
                    $array_price[$key] = round($value['price'],2);
                    $array_yprice[$key] = round($value['yprice'],2);

                    $attribute_list[$key]['SkuID'] = $value['id'];
                    $attribute_list[$key]['Stock'] = $value['sec_num'];
                    if($price_type == 1)
                    {
                        $seconds_price = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$seconds_price)); // 秒杀价格值
                        $attribute_list[$key]['Price'] = $seconds_price;
                    }
                    else
                    {
                        $attribute_list[$key]['Price'] = round($value['price'] * $seconds_price * 0.01,2);
                    }
                    
                    $attribute_list[$key]['Pic'] = ServerPath::getimgpath($value['img'],0);
                    $attribute = unserialize($value['attribute']);

                    foreach ($attribute as $k => $v)
                    {
                        if (strpos($k, '_LKT_') !== false)
                        {
                            $k = substr($k, 0, strrpos($k, "_LKT"));
                            $v = substr($v, 0, strrpos($v, "_LKT"));
                        }
                        $attribute_list[$key][$k] = $v;

                        if (!in_array($k, $arrayName))
                        {
                            array_push($arrayName, $k);
                            $kkk = $attnum++;
                            if($price_type == 1)
                            {
                                $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$value['sec_num'],'price'=>$seconds_price,'enable'=>false,'select'=>false);
                            }
                            else
                            {
                                $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$value['sec_num'],'price'=>round($value['price'] * $seconds_price * 0.01,2),'enable'=>false,'select'=>false);
                            }

                            $attrList[$kkk] = array('attrName' => $k, 'attrType' => '1', 'id' => md5($k));
                            $attrList[$kkk]['all'][] = $v;
                            $attrList[$kkk]['attr'][] = $attr_array;
                        }
                        else
                        {   
                            if($price_type == 1)
                            {
                                $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$value['sec_num'],'price'=>$seconds_price,'enable'=>false,'select'=>false);
                            }
                            else
                            {
                                $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$value['sec_num'],'price'=>round($value['price'] * $seconds_price * 0.01,2),'enable'=>false,'select'=>false);
                            }

                            $k_ = array_search($k,$arrayName);
                            if(!in_array($v,$attrList[$k_]['all']))
                            {
                                $attrList[$k_]['all'][] = $v;
                                $attrList[$k_]['attr'][] = $attr_array;
                            }
                        }
                        
                        $attributes[] = array('attributeId' => md5($k), 'attributeValId' => md5($v));
                        $name .= $v;
                    }
                    
                    $cimgurl = ServerPath::getimgpath($value['img'],0);
                    $unit = $value['unit'];
                    if($price_type == 1)
                    {
                        $skuBeanList[$key] = array('name' => $name, 'imgurl' => $cimgurl, 'cid' => $value['id'], 'price' => $seconds_price, 'count' => $value['sec_num'], 'unit' => $unit, 'attributes' => $attributes);
                    }
                    else
                    {
                        $skuBeanList[$key] = array('name' => $name, 'imgurl' => $cimgurl, 'cid' => $value['id'], 'price' => round($value['price']* $seconds_price * 0.01,2), 'count' => $value['sec_num'], 'unit' => $unit, 'attributes' => $attributes);
                    } 
                }     
            }
            elseif($pro_type == 'FS')
            {   
                $discount = Db::name('flashsale_activity')->where('id', $pro_id)->value('discount');//折扣
                foreach ($r_size as $key => $value)
                {   
                    $attnum = 0;
                    $attributes = array();
                    $name = '';
                    // // 货币转换
                    // $value['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$value['price']));
                    // $value['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$value['yprice']));
                    $value['price'] = round($value['price'],2);
                    $value['yprice'] = round($value['yprice'],2);
                    $flashsale_price = round($discount * $value['price'] * 0.1,2);//折扣价格值
                    $array_price[$key] = round($value['price'],2);
                    $array_yprice[$key] = round($value['yprice'],2);

                    $attribute_list[$key]['SkuID'] = $value['id'];
                    $attribute_list[$key]['Stock'] = $value['num'];
                    $attribute_list[$key]['Price'] = $flashsale_price;
                    
                    $attribute_list[$key]['Pic'] = ServerPath::getimgpath($value['img'],0);
                    $attribute = unserialize($value['attribute']);
                    
                    foreach ($attribute as $k => $v)
                    {
                        if (strpos($k, '_LKT_') !== false)
                        {
                            $k = substr($k, 0, strrpos($k, "_LKT"));
                            $v = substr($v, 0, strrpos($v, "_LKT"));
                        }
                        $attribute_list[$key][$k] = $v;

                        if (!in_array($k, $arrayName))
                        {
                            array_push($arrayName, $k);
                            $kkk = $attnum++;
                            $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$buyNum,'price'=>$flashsale_price,'enable'=>false,'select'=>false);

                            $attrList[$kkk] = array('attrName' => $k, 'attrType' => '1', 'id' => md5($k));
                            $attrList[$kkk]['all'][] = $v;
                            $attrList[$kkk]['attr'][] = $attr_array;
                        }
                        else
                        {   
                            $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$buyNum,'price'=>$flashsale_price,'enable'=>false,'select'=>false);

                            $k_ = array_search($k,$arrayName);
                            if(!in_array($v,$attrList[$k_]['all']))
                            {
                                $attrList[$k_]['all'][] = $v;
                                $attrList[$k_]['attr'][] = $attr_array;
                            }
                        }
                        
                        $attributes[] = array('attributeId' => md5($k), 'attributeValId' => md5($v));
                        $name .= $v;
                    }
                    
                    $cimgurl = ServerPath::getimgpath($value['img'],0);
                    $unit = $value['unit'];
                    $skuBeanList[$key] = array('name' => $name, 'imgurl' => $cimgurl, 'cid' => $value['id'], 'price' => $flashsale_price, 'count' => $buyNum, 'unit' => $unit, 'attributes' => $attributes); 
                }     
            }
            elseif($pro_type == 'IN')
            {   
                foreach ($r_size as $key => $value)
                {
                    $attnum = 0;
                    $attributes = array();
                    $name = '';

                    // 货币转换
                    // $value['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$value['price']));
                    // $active_res[0]['money'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$active_res[0]['money']));
                    $array_price[$key] = round($active_res[0]['money'],2);
                    $array_yprice[$key] = round($value['price'],2);

                    $attribute_list[$key]['SkuID'] = $value['id'];
                    $attribute_list[$key]['Stock'] = $active_res[0]['num'];
                    $attribute_list[$key]['Price'] = round($active_res[0]['money'],2);
                    $attribute_list[$key]['Pic'] = ServerPath::getimgpath($value['img'],0);
                    $attribute_list[$key]['integralNum'] = $value['integral'];
                    $attribute = unserialize($value['attribute']);

                    foreach ($attribute as $k => $v)
                    {
                        if (strpos($k, '_LKT_') !== false)
                        {
                            $k = substr($k, 0, strrpos($k, "_LKT"));
                            $v = substr($v, 0, strrpos($v, "_LKT"));
                        }
                        $attribute_list[$key][$k] = $v;

                        if (!in_array($k, $arrayName))
                        {
                            array_push($arrayName, $k);
                            $kkk = $attnum++;
                            
                            $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$active_res[0]['num'],'price'=>round($active_res[0]['money'],2),'enable'=>false,'select'=>false);

                            $attrList[$kkk] = array('attrName' => $k, 'attrType' => '1', 'id' => md5($k));
                            $attrList[$kkk]['all'][] = $v;
                            $attrList[$kkk]['attr'][] = $attr_array;
                        }
                        else
                        {
                            $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$active_res[0]['num'],'price'=>round($active_res[0]['money'],2),'enable'=>false,'select'=>false);

                            $k_ = array_search($k,$arrayName);
                            if(!in_array($v,$attrList[$k_]['all']))
                            {
                                $attrList[$k_]['all'][] = $v;
                                $attrList[$k_]['attr'][] = $attr_array;
                            }
                        }
                        
                        $attributes[] = array('attributeId' => md5($k), 'attributeValId' => md5($v));
                        $name .= $v;
                    }
                    
                    $cimgurl = ServerPath::getimgpath($value['img'],0);
                    $unit = $value['unit'];
                    
                    $skuBeanList[$key] = array('name' => $name, 'imgurl' => $cimgurl, 'cid' => $value['id'], 'price' => round($active_res[0]['money'],2), 'count' => $active_res[0]['num'], 'unit' => $unit, 'attributes' => $attributes, 'integralNum' => $value['integral']);
                }
            }
            elseif($pro_type == 'FX')
            {
                foreach ($r_size as $key => $value)
                {
                    $attnum = 0;
                    $attributes = array();
                    $name = '';

                    // // 货币转换
                    // $value['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$value['price']));
                    // $value['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$value['yprice']));
                    $value['price'] = round($value['price'],2);
                    $value['yprice'] = round($value['yprice'],2);
                    $array_price[$key] = round($value['price'],2);
                    $array_yprice[$key] = round($value['yprice'],2);

                    $attribute_list[$key]['SkuID'] = $value['id'];
                    $attribute_list[$key]['Stock'] = $value['num'];
                    $attribute_list[$key]['Price'] = round($value['price']* $discount,2);
                    $attribute_list[$key]['Pic'] = ServerPath::getimgpath($value['img'],0);
                    $attribute = unserialize($value['attribute']);

                    foreach ($attribute as $k => $v)
                    {
                        if (strpos($k, '_LKT_') !== false)
                        {
                            $k = substr($k, 0, strrpos($k, "_LKT"));
                            $v = substr($v, 0, strrpos($v, "_LKT"));
                        }
                        $attribute_list[$key][$k] = $v;

                        if (!in_array($k, $arrayName))
                        {
                            array_push($arrayName, $k);
                            $kkk = $attnum++;
                            
                            $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$value['num'],'price'=>round($value['price'] * $discount,2),'enable'=>false,'select'=>false);

                            $attrList[$kkk] = array('attrName' => $k, 'attrType' => '1', 'id' => md5($k));
                            $attrList[$kkk]['all'][] = $v;
                            $attrList[$kkk]['attr'][] = $attr_array;
                        }
                        else
                        {
                            $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$value['num'],'price'=>round($value['price'] * $discount,2),'enable'=>false,'select'=>false);

                            $k_ = array_search($k,$arrayName);
                            if(!in_array($v,$attrList[$k_]['all']))
                            {
                                $attrList[$k_]['all'][] = $v;
                                $attrList[$k_]['attr'][] = $attr_array;
                            }
                        }
                        
                        $attributes[] = array('attributeId' => md5($k), 'attributeValId' => md5($v));
                        $name .= $v;
                    }
                    
                    $cimgurl = ServerPath::getimgpath($value['img'],0);
                    $unit = $value['unit'];
                    
                    $skuBeanList[$key] = array('name' => $name, 'imgurl' => $cimgurl, 'cid' => $value['id'], 'price' => round($value['price']* $discount,2), 'count' => $value['num'], 'unit' => $unit, 'attributes' => $attributes);
                }
            }
            else
            {
                foreach ($r_size as $key => $value)
                {
                    $attnum = 0;
                    $attributes = array();
                    $name = '';

                    // // 货币转换
                    // $value['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$value['price']));
                    // $value['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$value['yprice']));
                    $value['price'] = round($value['price'],2);
                    $value['yprice'] = round($value['yprice'],2);
                    $array_price[$key] = round($value['price'],2);
                    $array_yprice[$key] = round($value['yprice'],2);

                    $attribute_list[$key]['SkuID'] = $value['id'];
                    $attribute_list[$key]['Stock'] = $value['num'];
                    $attribute_list[$key]['Price'] = round($value['price']* $grade_rate,2);
                    $attribute_list[$key]['Pic'] = ServerPath::getimgpath($value['img'],0);
                    $attribute = unserialize($value['attribute']);

                    foreach ($attribute as $k => $v)
                    {
                        if (strpos($k, '_LKT_') !== false)
                        {
                            $k = substr($k, 0, strrpos($k, "_LKT"));
                            $v = substr($v, 0, strrpos($v, "_LKT"));
                        }
                        $attribute_list[$key][$k] = $v;

                        if (!in_array($k, $arrayName))
                        {
                            array_push($arrayName, $k);
                            $kkk = $attnum++;
                            
                            $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$value['num'],'price'=>round($value['price'] * $grade_rate,2),'enable'=>false,'select'=>false);

                            $attrList[$kkk] = array('attrName' => $k, 'attrType' => '1', 'id' => md5($k));
                            $attrList[$kkk]['all'][] = $v;
                            $attrList[$kkk]['attr'][] = $attr_array;
                        }
                        else
                        {
                            $attr_array = array('attributeId'=>md5($k),'id'=>md5($v),'attributeValue'=>$v,'attrId'=>$value['id'],'count'=>$value['num'],'price'=>round($value['price'] * $grade_rate,2),'enable'=>false,'select'=>false);

                            $k_ = array_search($k,$arrayName);
                            if(!in_array($v,$attrList[$k_]['all']))
                            {
                                $attrList[$k_]['all'][] = $v;
                                $attrList[$k_]['attr'][] = $attr_array;
                            }
                        }
                        
                        $attributes[] = array('attributeId' => md5($k), 'attributeValId' => md5($v));
                        $name .= $v;
                    }

                    $cimgurl = ServerPath::getimgpath($value['img'],0);
                    $unit = $value['unit'];

                    $skuBeanList[$key] = array('name' => $name, 'imgurl' => $cimgurl, 'cid' => $value['id'], 'price' => round($value['price']* $grade_rate,2), 'count' => $value['num'], 'unit' => $unit, 'attributes' => $attributes);
                }
            }
        }
        /* 属性结束 */

        // // 货币转换
        // $product['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$product['price']));
        // $product['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$product['yprice']));
        // $product['vip_yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$product['vip_yprice']));
        // $product['vip_price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$product['vip_price']));
        // $product['yunfei'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$product['yunfei']));
        // $product['freight_price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$product['freight_price']));

        /* 评论开始 */
        $comments = array();
        $comments_total = 0;

        $sql_c_total = "select ifnull(count(1) ,0) as c_total from lkt_comments AS a LEFT JOIN lkt_user AS m ON a.uid = m.user_id and a.store_id = m.store_id where a.store_id = '$store_id' and a.pid = '$id'";
        $r_c_total = Db::query($sql_c_total);
        $comments_total = $r_c_total[0]['c_total'];

        // 根据商品id、评论表(微信id)与会员表(微信id)相等,查询信息
        $sql_c = "select a.id,a.pid,a.attribute_id,a.content,a.CommentType,a.anonymous,a.add_time,a.review_time,a.order_detail_id,m.user_name,m.headimgurl from lkt_comments AS a LEFT JOIN lkt_user AS m ON a.uid = m.user_id and a.store_id = m.store_id where a.store_id = '$store_id' and a.pid = '$id' limit 1 ";
        $r_c = Db::query($sql_c);
        if ($r_c)
        {
            foreach ($r_c as $k_c => $v_c)
            {
                $add_time = $v_c['add_time']; // 评论时间
                $review_time = $v_c['review_time']; // 追评时间
                $order_detail_id = $v_c['order_detail_id']; // 订单详情ID
                $attribute_id = $v_c['attribute_id'];
                $attribute_str = '';
                $v_c['mch_id'] = $mch_id;

                $r_shu = ConfigureModel::where(['pid'=>$id,'id'=>$attribute_id,'recycle'=>0])->field('attribute')->select()->toArray();
                if ($r_shu)
                {
                    $attribute = unserialize($r_shu[0]['attribute']);
                    foreach ($attribute as $k => $v)
                    {
                        if (strpos($k, '_LKT_') !== false)
                        {
                            $k = substr($k, 0, strrpos($k, "_LKT"));
                            $v = substr($v, 0, strrpos($v, "_LKT"));
                        }
                        $attribute_str .= $k . ":" . $v . ',';
                    }
                    $attribute_str = rtrim($attribute_str, ',');
                }
                $v_c['attribute_str'] = $attribute_str;

                $v_c['time'] = substr($v_c['add_time'], 0, 10); // 评论时间

                $comments_id = $v_c['id']; // 评论id

                $v_c['review_day'] = '';
                if($review_time != '0000-00-00 00:00:00')
                {
                    $v_c['review_day'] = strtotime($add_time) - strtotime($review_time);
                }

                $v_c['images'] = array();
                // 根据评论id,查询评论图片表
                $comment_res = CommentsImgModel::where(['comments_id'=>$comments_id,'type'=>0])->field('comments_url')->select()->toArray();
                if ($comment_res)
                {
                    $array_c = array();
                    foreach ($comment_res as $kc => $vc)
                    {
                        $url = $vc['comments_url']; // 评论图片
                        $array_c[$kc] = array('url' => ServerPath::getimgpath($url));
                    }
                    $v_c['images'] = $array_c;
                }
                
                $v_c['review_images'] = array();
                // 根据评论id,查询追评评论图片表
                $comment_res1 = CommentsImgModel::where(['comments_id'=>$comments_id,'type'=>1])->field('comments_url')->select()->toArray();
                if ($comment_res1)
                {
                    $array_c1 = array();
                    foreach ($comment_res1 as $kc1 => $vc1)
                    {
                        $url = $vc1['comments_url']; // 评论图片
                        $array_c1[$kc1] = array('url' => ServerPath::getimgpath($url));
                    }
                    $v_c['review_images'] = $array_c1;
                }
                
                if ($v_c['anonymous'] == 1)
                {
                    $v_c['user_name'] = Lang('anonymous');
                    $v_c['headimgurl'] = '';
                }
                
                $reply_admin = '';
                // 根据评论ID，查询回复评论表
                $ad_res = ReplyCommentsModel::where(['store_id'=>$store_id,'cid'=>$comments_id,'uid'=>'admin'])->field('content')->select()->toArray();
                if ($ad_res)
                {
                    $reply_admin = $ad_res[0]['content'];
                }
                $v_c['replyAdmin'] = $reply_admin;

                $arrive_time = ''; // 到货时间
                $r_d = OrderDetailsModel::where(['id'=>$order_detail_id])->field('arrive_time')->select()->toArray();
                if ($r_d)
                {
                    $arrive_time = $r_d[0]['arrive_time']; // 到货时间
                }
                $v_c['arrive_time'] = $arrive_time; // 到货时间
                
                if($v_c['CommentType'] == 1 || $v_c['CommentType'] == 2)
                {
                    $v_c['plType'] = 1;
                }
                else if($v_c['CommentType'] == 4 || $v_c['CommentType'] == 5)
                {
                    $v_c['plType'] = 5;
                }
                else
                {
                    $v_c['plType'] = 3;
                }
                $comments[] = $v_c;
            }
        }
        /* 评论结束 */

        // $Plugin = new Plugin();
        // $Plugin_arr1 = $Plugin->front_Plugin($store_id);

        $coupon_status = false;
        $coupon_str = array();

        $activity_type_codes = array();
        $activity_map = array();

        // 分销
        $sql_distribution = "select id from lkt_distribution_goods where store_id = '$store_id' and p_id = '$id' and recycle = 0 order by id desc limit 1";
        $res_distribution = Db::query($sql_distribution);
        if ($res_distribution)
        {
            $fx_id = $res_distribution[0]['id'];
            $activity_map['distribution'] = "?isDistribution=true&toback=true&pro_id=" . $id . "&fx_id=" . $fx_id . "&language=" . $language;
        }

        // 秒杀
        $sql_seconds = "select id from lkt_seconds_activity where store_id = '$store_id' and goodsId = '$id' and is_delete = 0 and isshow = 1 and status != 3 order by id asc limit 1";
        $res_seconds = Db::query($sql_seconds);
        if ($res_seconds)
        {
            $sec_id = $res_seconds[0]['id'];
            $activity_map['seconds'] = "pro_id=" . $id . "&navType=1&id=" . $sec_id . "&type=MS&language=" . $language;
        }

        // 拼团
        $sql_group = "select activity_id from lkt_group_goods where goods_id = '$id' and recycle = 0 order by id desc limit 1";
        $res_group = Db::query($sql_group);
        if ($res_group)
        {
            $activity_id = $res_group[0]['activity_id'];
            $activity_map['go_group'] = "pro_id=" . $id . "&goodsId=" . $id . "&acId=" . $activity_id . "&type=MS&language=" . $language;
        }

        // 预售
        $sql_presell = "select id from lkt_pre_sell_goods where product_id = '$id' and is_delete = 0 and is_display = 1 limit 1";
        $res_presell = Db::query($sql_presell);
        if ($res_presell)
        {
            $activity_map['presell'] = "toback=true&pro_id=" . $id . "&language=" . $language;
        }

        // 种草
        $sql_grass = "select id from lkt_bbs_post where store_id = '$store_id' and recycle = 0 and is_hide = 0 and status = 2 and FIND_IN_SET('$id', pro_ids) limit 1";
        $res_grass = Db::query($sql_grass);
        if ($res_grass)
        {
            $zc_id = $res_grass[0]['id'];
            $activity_map['zc'] = "id=" . $zc_id . "&language=" . $language;
        }

        // 竞拍
        $sql_auction = "select id,session_id from lkt_auction_product where goods_id = '$id' and recovery = 0 and status = 1 and is_show = 1 order by id desc limit 1";
        $res_auction = Db::query($sql_auction);
        if ($res_auction)
        {
            $auction_id = $res_auction[0]['id'];
            $session_id = $res_auction[0]['session_id'];
            $sql_auction_detail = "select a.id from lkt_auction_product a,lkt_auction_session b,lkt_auction_special c,lkt_product_list d,lkt_freight e where a.session_id = b.id and b.special_id = c.id and a.recovery = b.recovery and a.recovery = c.recovery and a.recovery = d.recycle and d.id = a.goods_id and d.freight = e.id and a.id = '$auction_id' ";
            $res_auction_detail = Db::query($sql_auction_detail);
            if ($res_auction_detail)
            {
                $activity_map['auction'] = "specialId=" . $session_id . "&acId=" . $auction_id . "&language=" . $language;
            }
        }

        // 限时折扣
        $sql_flashsale = "select id from lkt_flashsale_activity where store_id = '$store_id' and goods_id = '$id' and is_delete = 0 and isshow = 1 order by id desc limit 1";
        $res_flashsale = Db::query($sql_flashsale);
        if ($res_flashsale)
        {
            $flashsale_id = $res_flashsale[0]['id'];
            $activity_map['flashsale'] = "toback=true&type=FS&pro_id=" . $id . "&fsid=" . $flashsale_id . "&mch_id=" . $mch_id . "&language=" . $language;
        }

        // 积分商城
        $sql_integral = "select id,goods_id,integral,num from lkt_integral_goods where store_id = '$store_id' and goods_id = '$id' and is_delete = 0 and status = 2 order by id desc limit 1";
        $res_integral = Db::query($sql_integral);
        if ($res_integral)
        {
            $integral_id = $res_integral[0]['id'];
            $integral_goods_id = $res_integral[0]['goods_id'];
            $integral = $res_integral[0]['integral'];
            $integral_num = $res_integral[0]['num'];
            $activity_map['integral'] = "goodsId=" . $integral_goods_id . "&pro_id=" . $integral_id . "&integral=" . $integral . "&num=" . $integral_num . "&language=" . $language;
        }

        // 会员商品
        $sql_member = "select id from lkt_member_pro where store_id = '$store_id' and pro_id = '$id' and recovery = 0 order by id desc limit 1";
        $res_member = Db::query($sql_member);
        if ($res_member)
        {
            $activity_map['member'] = "is_hy=true&pro_id=" . $id . "&language=" . $language;
        }

        if (!empty($activity_map))
        {
            $activity_type_codes[] = $activity_map;
        }

        $data = array('access_id'=>$access_id,'login_status'=>$login_status,'type'=>$type,'collection_id'=>$collection_id,'login_status'=>$login_status,'is_grade'=>$is_grade,'shop_list'=>$shop_list,'pro'=>$product,'attribute_list'=>$attribute_list,'skuBeanList'=>$skuBeanList,'attrList'=>$attrList,'active'=>$active,'logo'=>'','cs_price'=>$cs_price,'coupon_status'=>$coupon_status,'coupon_str'=>$coupon_str,'commentsTotal'=>$comments_total,'comments'=>$comments,'commodity_type'=>$commodity_type,'isAddCar'=>$isAddCar,'mchStoreListNum'=>$mchStoreListNum,'write_off_settings'=>$write_off_settings,'isAddCar'=>$isAddCar,'mchStoreListNum'=>$mchStoreListNum,'write_off_settings'=>$write_off_settings,'writeNumStatus'=>$writeNumStatus,'activity_type_codes'=>$activity_type_codes);
        
        if($is_presell == 1)
        {
            $sellGoodInfo = array();
            $field = "sell_type as sellType,deposit,pay_type as depositType,deposit_start_time as depositStart,deposit_end_time as depositEnd,balance_pay_time,delivery_time as deliveryTime,sell_num as sellNum,surplus_num as surplusNum,end_day";
            $r_pre_sell = PreSellGoodsModel::where(['product_id'=>$id])->field($field)->select()->toArray();
            if($r_pre_sell)
            {
                if($r_pre_sell[0]['sellType'] == 1)
                {
                    // // 货币转换
                    // $r_pre_sell[0]['deposit'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$r_pre_sell[0]['deposit']));
                    $r_pre_sell[0]['deposit'] = round($r_pre_sell[0]['deposit'],2);
                    $r_pre_sell[0]['balance'] = round($r_pre_sell[0]['deposit'],2);
                    $r_pre_sell[0]['startTime'] = $r_pre_sell[0]['balance_pay_time'];
                    $r_pre_sell[0]['endTime'] = date("Y-m-d 23:59:59",strtotime($r_pre_sell[0]['balance_pay_time']));
                    $sellGoodInfo = array('sellType'=>$r_pre_sell[0]['sellType'],'deposit'=>$r_pre_sell[0]['deposit'],'depositType'=>$r_pre_sell[0]['depositType'],'depositStart'=>$r_pre_sell[0]['depositStart'],'depositEnd'=>$r_pre_sell[0]['depositEnd'],'balance_pay_time'=>$r_pre_sell[0]['balance_pay_time'],'deliveryTime'=>$r_pre_sell[0]['deliveryTime'],'balance'=>$r_pre_sell[0]['balance'],'startTime'=>$r_pre_sell[0]['startTime'],'endTime'=>$r_pre_sell[0]['endTime']);
                }
                else
                {
                    $end_day = $r_pre_sell[0]['end_day'];
                    $endTime = date("Y-m-d H:i:s",strtotime("+$end_day day",strtotime($upper_shelf_time)));
                    $sellGoodInfo = array('sellType'=>$r_pre_sell[0]['sellType'],'sellNum'=>$r_pre_sell[0]['sellNum'],'surplusNum'=>$r_pre_sell[0]['surplusNum'],'deliveryTime'=>$r_pre_sell[0]['deliveryTime'],'endTime'=>$endTime);
                }
                
                $r_pre_sell_config = PreSellConfigModel::where(['store_id'=>$store_id])->field('deposit_desc,balance_desc')->select()->toArray();
                $sellGoodInfo['depositDesc'] = $r_pre_sell_config[0]['deposit_desc'];
                $sellGoodInfo['balanceDesc'] = $r_pre_sell_config[0]['balance_desc'];
            }
            $data['sellGoodInfo'] = $sellGoodInfo;
        }
        if($pro_type == 'MS')
        {   
            $navType = trim(Request::post('navType')); // 0已结束1进行中2未开始
            $seckill_pro_res = SecondsActivityModel::where(['store_id'=>$store_id,'id'=>$pro_id,'isshow'=>1,'is_delete'=>0])->select()->toArray();
            if(empty($seckill_pro_res))
            {
                $message = Lang('sec.9');
                return output(ERROR_CODE_HDYJS,$message);
            }
            $starttime = $seckill_pro_res[0]['starttime'];//开始时间
            $endtime = $seckill_pro_res[0]['endtime'];//结束时间
            $price_type = $seckill_pro_res[0]['price_type'];//秒杀价格单位 0=百分比 1=固定值
            $seconds_price = $seckill_pro_res[0]['seconds_price'];//秒杀价格值
            $status = $seckill_pro_res[0]['status'];//活动状态 1 未开始 2 进行中 3结束
            $seconds_maxnum = $seckill_pro_res[0]['max_num'];//最大库存
            $seconds_num = $seckill_pro_res[0]['num'];//剩余库存
            $label_id = $seckill_pro_res[0]['label_id'];//活动标签
            $activity_id = $seckill_pro_res[0]['id'];//活动id
            $product['yprice'] = $product['price'];//秒杀划线价=原商品售价
            if($price_type == 1)
            {
                // // 货币转换
                // $seconds_price = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$seconds_price));
                $product['price'] = (float)$seconds_price; //秒杀价
            }
            else
            {
                $product['price'] = round($product['price'] * $seconds_price * 0.01,2); //秒杀价
            }
            $product['max_num'] = $seconds_maxnum;
            $product['num'] = $seconds_num;
            //秒杀库存计算规格商品的合计Bug-ID 48560 
            $sql_pro = "select sum(num) as stockNum,sum(max_num) as max_num from lkt_seconds_activity where id='$activity_id' and exists(select 1 from lkt_configure where id=lkt_seconds_activity.attr_id and recycle=0 ) ";
            $res_c = Db::query($sql_pro);
            if($res_c)
            {
                $product['num'] = intval($res_c[0]['stockNum']);//剩余库存
                $product['max_num'] = intval($res_c[0]['max_num']);//最大库存
            }
            $labelres = SecondsLabelModel::where(['id'=>$label_id,'recovery'=>0])->select()->toArray();
            if(empty($labelres))
            {
                $message = Lang('sec.9');
                return output(ERROR_CODE_HDYJS,$message);
            }
            if ($navType == 2)
            {
                $product['remainingTime'] = strtotime($starttime)* 1000;
            }
            else
            {
                $product['remainingTime'] = strtotime($endtime)* 1000;
            }
            $product['secStatus'] = $seckill_pro_res[0]['status'];//秒杀状态
            $mch_id = $labelres[0]['mch_id'];//店铺id
            $product['canbuy'] = 1;

            $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID

            //查询秒杀设置
            $seckill_config = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$SelfOperatedStore_id,'is_open'=>1])->select()->toArray();
            if (!empty($seckill_config))
            {
                //是否开启预估
                if($seckill_config[0]['is_herald'] == 1) 
                {
                    if ($seckill_pro_res[0]['status'] == 1 && strtotime($starttime)-time() <= $seckill_config[0]['heraldTime']) 
                    {
                        $product['secStatus'] = 4; //预告秒杀
                    }
                }
                $product['buyNum'] = $seckill_config[0]['buy_num'];
                $sql_ac1 = "select sum(d.num) as total from lkt_order as o left join lkt_order_details as d on o.sNo = d.r_sNo where o.status in (1,2,5) and d.p_id = '$id' and o.user_id = '$user_id'";
                $ac1_res = Db::query($sql_ac1);
                if(!empty($ac1_res))
                {
                    $total_buy = $ac1_res[0]['total'];
                    if($total_buy >= $seckill_config[0]['buy_num'])
                    {
                        $product['canbuy'] = 0;
                    }
                }
            }
            else
            {   
                $message = Lang('sec.9');
                return output(ERROR_CODE_HDYJS,$message);
            }

            if($seconds_num == 0)
            {
                $product['secStatus'] = 5; // 已抢光
            }
            
            $data['pro'] = $product;
        }
        if($pro_type == 'FS')
        {   
            $navType = trim(Request::post('navType')); // 0已结束1进行中2未开始
            $fs_pro_res = FlashsaleActivityModel::where(['store_id'=>$store_id,'id'=>$pro_id,'is_delete'=>0])->select()->toArray();
            if(empty($fs_pro_res))
            {
                $message = Lang('sec.9');
                return output(ERROR_CODE_HDYJS,$message);
            }
            $fs_label_res = FlashsaleLabelModel::where(['store_id'=>$store_id,'id'=>$fs_pro_res[0]['label_id'],'recovery'=>0])->select()->toArray();
            if(empty($fs_label_res))
            {
                $message = Lang('sec.9');
                return output(ERROR_CODE_HDYJS,$message);
            }           
            $mch_id = $fs_label_res[0]['mch_id'];//店铺id
            $starttime = $fs_label_res[0]['starttime'];//开始时间
            $endtime = $fs_label_res[0]['endtime'];//结束时间
            $status = $fs_label_res[0]['status'];//活动状态 1 未开始 2 进行中 3结束

            $label_id = $fs_pro_res[0]['label_id'];//活动标签
            $discount = $fs_pro_res[0]['discount'];//折扣
            
            $activity_id = $fs_pro_res[0]['id'];//活动id
            $product['yprice'] = sprintf("%.2f",$product['price']);//活动商品原价取商品售价
            $product['price'] = sprintf("%.2f",$product['price'] * $discount * 0.1);//活动售价
            
            if ($navType == 2)
            {
                $product['remainingTime'] = strtotime($starttime)* 1000;
            }
            else
            {
                $product['remainingTime'] = strtotime($endtime)* 1000;
            }
            $product['fsStatus'] = $fs_label_res[0]['status'];//秒杀状态

            $product['canbuy'] = 1;
            //查询秒杀设置
            $product['buyNum'] = $fs_pro_res[0]['buylimit'];//购买上限
            if (!empty($product['buyNum']))
            {
                $sql_ac1 = "select sum(num) as total from lkt_flashsale_record where store_id = '$store_id' and user_id = '$user_id' and activity_id = '$pro_id' and pro_id = '$id' and is_delete = 0 ";
                $ac1_res = Db::query($sql_ac1);
                if(!empty($ac1_res))
                {
                    $total_buy = $ac1_res[0]['total'];
                    $product['buyNum'] = $product['buyNum'] - $total_buy;//购买上限
                    if($product['buyNum'] < 0 )
                    {
                        $product['buyNum'] = 0;
                    }
                }
            }
            $data['pro'] = $product;
        }
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 添加购物车
    public function add_cart()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type'));
        $access_id = trim(Request::param('access_id'));
        $Goods_id = trim(Request::param('pro_id')); // 商品id
        $attribute_id = trim(Request::param('attribute_id')); // 商品属性id
        $Goods_num = trim(Request::param('num')); // 数量
        $type = trim(Request::param('type')); // 类型
        
        $time = date("Y-m-d H:i:s");
        $user_id = '';
        if (empty($Goods_id) || empty($attribute_id))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '缺少参数!Goods_id:' . $Goods_id . ';attribute_id:' . $attribute_id;
            $this->Log($Log_content);
            ob_clean();
            $message = Lang('Parameter error');
            return output(109,$message);
        }
        else
        {
            if (empty($access_id))
            { // 生成密钥
                $access_id = Tools::getToken($store_id, $store_type);
            }
            else
            {
                $getPayload_test = Tools::verifyToken($access_id); //对token进行验证签名,如果过期返回false,成功返回数组
                if ($getPayload_test)
                { // 过期重新生成一个，不需要做登录提示。
                    $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
                    if ($r_user)
                    {
                        $user_id = $r_user[0]['user_id']; // 用户id
                    }
                }
                else
                {
                    $access_id = Tools::getToken($store_id, $store_type);
                }
            }
            //判断商品是否已下架
            $res_s = ProductListModel::where(['store_id'=>$store_id,'id'=>$Goods_id])->field('commodity_type,status')->select()->toArray();
            if ($res_s)
            {
                $pro_type = $res_s[0]['commodity_type'];
                if ($res_s[0]['status'] == 3)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '该商品已下架!商品ID:' . $Goods_id;
                    $this->Log($Log_content);
                    ob_clean();
                    $message = Lang('product.0');
                    return output(120,$message);
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '商品ID错误!商品ID:' . $Goods_id;
                $this->Log($Log_content);
                ob_clean();
                $message = Lang('Parameter error');
                return output(121,$message);
            }

            // 根据商品属性id,查询商品数量
            $res_k = ConfigureModel::where(['recycle'=>0,'id'=>$attribute_id])->field('num')->select()->toArray();
            $num = $res_k[0]['num']; // 商品数量
            if ($num >= $Goods_num)
            {
                if (empty($user_id))
                { // 当用户ID不存在时
                    // 根据商城ID、token、商品ID、属性ID，查询购物车信息
                    $r0 = CartModel::where(['store_id'=>$store_id,'token'=>$access_id,'Goods_id'=>$Goods_id,'Size_id'=>$attribute_id,'pro_type'=>$pro_type])->field('id,Goods_num')->select()->toArray();
                    if ($r0)
                    { // 存在
                        $cart_id = $r0[0]['id']; // 购物车ID
                        $Goods_num1 = $r0[0]['Goods_num']; // 目前购物车商品数量
                        
                        $sql_where = array('store_id'=>$store_id,'token'=>$access_id,'Goods_id'=>$Goods_id,'Size_id'=>$attribute_id,'pro_type'=>$pro_type);
                        if ($Goods_num + $Goods_num1 > $num)
                        { // 添加数量 + 当前数量 > 剩余库存
                            $sql_update = array('Goods_num'=>$num);
                        }
                        else 
                        {
                            $sql_update = array('Goods_num'=>Db::raw('Goods_num + '.$Goods_num));
                        }
                        $r1 = Db::name('cart')->where($sql_where)->update($sql_update);
                        if ($r1 < 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '添加购物车失败!参数:' . json_encode($sql_where);
                            $this->Log($Log_content);
                            ob_clean();
                            $message = Lang('Abnormal business');
                            return output(110,$message);
                        }
                        else
                        {
                            ob_clean();
                            $data = array('cart_id' => $cart_id, 'type' => $type, 'access_id' => $access_id);
                            $message = Lang('Success');
                            return output(200,$message,$data);
                        }
                    }
                    else
                    {
                        $data = array('store_id' => $store_id, 'token' => $access_id, 'user_id' => '', 'Goods_id' => $Goods_id, 'Goods_num' => $Goods_num, 'Create_time' => $time, 'Size_id' => $attribute_id, 'pro_type' => $pro_type);
                        $r = Db::name('cart')->insertGetId($data);
                        if ($r > 0)
                        {
                            ob_clean();
                            $data = array('cart_id' => $r, 'type' => $type, 'access_id' => $access_id);
                            $message = Lang('Success');
                            return output(200,$message,$data);
                        }
                        else
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '添加购物车失败!参数:' . json_encode($data);
                            $this->Log($Log_content);
                            ob_clean();
                            $message = Lang('Abnormal business');
                            return output(110,$message);
                        }
                    }
                }
                else
                { // 已登录
                    // 查询购物车是否有过改商品，有则修改 无则新增
                    $r0 = CartModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'Goods_id'=>$Goods_id,'Size_id'=>$attribute_id,'pro_type'=>$pro_type])->field('id,Goods_num')->select()->toArray();
                    if ($r0)
                    {
                        $cart_id = $r0[0]['id']; // 购物车ID
                        $Goods_num1 = $r0[0]['Goods_num']; // 目前购物车商品数量
                        
                        $sql_where = array('store_id'=>$store_id,'user_id'=>$user_id,'Goods_id'=>$Goods_id,'Size_id'=>$attribute_id,'pro_type'=>$pro_type);
                        if ($type == 'addcart')
                        { // 当类型为添加时
                            if ($Goods_num + $Goods_num1 > $num)
                            { // 添加数量 + 当前数量 > 剩余库存
                                $sql_update = array('Goods_num'=>$num);
                            }
                            else 
                            {
                                $sql_update = array('Goods_num'=>Db::raw('Goods_num + '.$Goods_num));
                            }
                        }
                        else
                        { // 根据用户id、微信id、商品id、属性id，修改购物车数量
                            $sql_update = array('Goods_num'=>$Goods_num);
                        }
                        $r1 = Db::name('cart')->where($sql_where)->update($sql_update);
                        if ($r1 < 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '添加购物车失败!参数:' . json_encode($sql_where);
                            $this->Log($Log_content);
                            ob_clean();
                            $message = Lang('Abnormal business');
                            return output(110,$message);
                        }
                        else
                        {
                            ob_clean();
                            $data = array('cart_id' => $cart_id, 'type' => $type, 'access_id' => $access_id);
                            $message = Lang('Success');
                            return output(200,$message,$data);
                        }
                    }
                    else
                    {
                        $data = array('store_id' => $store_id, 'token' => $access_id, 'user_id' => $user_id, 'Goods_id' => $Goods_id, 'Goods_num' => $Goods_num, 'Create_time' => $time, 'Size_id' => $attribute_id, 'pro_type' => $pro_type);
                        $r = Db::name('cart')->insertGetId($data);
                        if ($r)
                        {
                            ob_clean();
                            $data = array('cart_id' => $r, 'type' => $type, 'access_id' => $access_id);
                            $message = Lang('Success');
                            return output(200,$message,$data);
                        }
                        else
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '添加购物车失败!参数:' . json_encode($data);
                            $this->Log($Log_content);
                            ob_clean();
                            $message = Lang('Abnormal business');
                            return output(110,$message);
                        }
                    }
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '库存不足!Goods_id:' . $Goods_id . ';attribute_id:' . $attribute_id;
                $this->Log($Log_content);
                ob_clean();
                $message = Lang('product.1');
                return output(110,$message);
            }
        }
    }

    // 立即购买
    public function immediately_cart()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $product1 = trim(Request::param('product')); // 商品数组--------['pid'=>66,'cid'=>88]
        $orderType = trim(Request::param('orderType'));
        
        $product = '';
        $cart_id = '';
        $product_type = '';
        if ($product1 != '')
        {
            $product1 = htmlspecialchars_decode($product1);
            $product2 = json_decode(stripslashes(html_entity_decode($product1)));; // 字符串打散为数组
            foreach ($product2 as $k => $v)
            {
                foreach ($v as $ke => $va)
                {
                    if($ke == 'sec_id')
                    {
                        $product_type = 'MS';
                    }
                    $product3[$ke] = $va;
                }
            }
            $product = implode(',', $product3);
        }

        if($orderType == 'IN')
        {
            $product_type = 'IN';
        }
        $products = Tools::products_list($store_id,$cart_id, $product,$product_type);
        ob_clean();
        $message = Lang('Success');
        return output(200,$message);
    }

    // 添加评论
    public function add_comment()
    {
        $store_id = addslashes(trim($_GET['store_id']));
        $store_type = addslashes(trim($_GET['store_type'])); // 来源
        $access_id = addslashes(trim($_POST['access_id'])); // 授权id
        $language = addslashes(trim($_POST['language'])); // 语言
        $anonymous = addslashes(trim($_POST['anonymous'])); // 是否匿名
        $order_details_id = addslashes(trim($_POST['order_details_id'])); // 订单详情id
        $start = addslashes(trim($_POST['start'])); // 星级
        $comment = addslashes(trim($_POST['comment'])); // 内容
        $goodsId = addslashes(trim($_POST['goodsId'])); // 产品id
        $attribute_id = addslashes(trim($_POST['attributeId'])); // 属性id
        
        $add_time = date("Y-m-d H:i:s");
        if (empty($access_id))
        {
            ob_clean();
            $message = Lang('Unauthorized');
            return output(116,$message);
        }

        // 根据access_id,查询用户id
        $r = UserModel::where(['store_id' => $store_id,'access_id' => $access_id])
                        ->field('user_id,user_name')
                        ->select()
                        ->toArray();
        if (!empty($r))
        {
            $user_id = $r[0]['user_id'];
            $rr = OrderDetailsModel::where(['store_id' => $store_id,'id' => $order_details_id,'user_id' => $user_id])
                                    ->field('r_sNo as sNo')
                                    ->select()
                                    ->toArray();
            if ($rr)
            {
                $sNo = $rr[0]['sNo']; // 订单号
                //敏感词表
                $r_c = CommentsModel::where(['store_id' => $store_id,'oid' => $sNo,'pid' => $goodsId,'attribute_id' => $attribute_id])
                                    ->field('id,oid')
                                    ->select()
                                    ->toArray();
                //没有评价
                if ($comment != '' && empty($r_c))
                {
                    //评论表里没有此条记录且用户提交的内容或图片不为空
                    require('../app/common/badword.php');

                    $badword1 = array_combine($badword, array_fill(0, count($badword), '*'));
                    $content = preg_replace("/\s(?=\s)/", "\\1", $this->strtr_array($comment, $badword1));

                    $data_comments = ['store_id' => $store_id,'oid' => $sNo,'uid' => $user_id,'pid' => $goodsId,'attribute_id' => $attribute_id,'content' => $comment,'CommentType' => $start,'anonymous' => $anonymous,'add_time' => $add_time];
                    $cid = Db::name('comments')->insertGetId($data_comments);
                    if ($cid <= 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '添加评论数据失败!参数:' . json_encode($data_comments);
                        $this->Log($Log_content);
                        ob_clean();
                        $message = Lang('Abnormal business');
                        return output(110,$message);
                    }
                }
                if (!empty($r_c))
                {
                    $cid = $r_c[0]['id'];
                }

                //增加商品评论数
                $sql_p = "update lkt_product_list set comment_num = comment_num + 1 where store_id = '$store_id' and id = '$goodsId'";
                $res_p = Db::execute($sql_p);
                // $data_u = array('store_id'=>$store_id,'id'=>$goodsId);
                // $res_p = ProductListModel::where($data_u)->find();
                // $res_p->comment_num = Db::raw('comment_num + 1');
                // $res_p->save();
                if ($res_p < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '商品评论数增加失败!sql:' . $sql_p;
                    // $Log_content = __METHOD__ . '->' . __LINE__ . '商品评论数增加失败!参数:' . json_encode($data_u);
                    $this->Log($Log_content);
                    $message = Lang('Abnormal business');
                    return output(110,$message);
                }

                ob_clean();
                $message = Lang('Success');
                return output(200,$message,array('cid'=>$cid));
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '订单详情ID错误!ID:' . $order_details_id;
                $this->Log($Log_content);
                ob_clean();
                $message = Lang('Parameter error');
                return output(109,$message);
            }
        }
        else
        {
            ob_clean();
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
    }

    // 追加评论
    public function t_comment()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::post('access_id'))); // 授权id
        $anonymous = addslashes(trim(Request::post('anonymous'))); // 是否匿名
        $order_details_id = addslashes(trim(Request::post('order_details_id'))); // 订单详情id
        $start = addslashes(trim(Request::post('start'))); // 星级
        $comment = addslashes(trim(Request::post('comment'))); // 内容
        $goodsId = addslashes(trim(Request::post('goodsId'))); // 产品id
        $attribute_id = addslashes(trim(Request::post('attributeId'))); // 属性id
        $call_num = addslashes(trim(Request::post('upload_num'))); // 接口调用次数
        $for_num = addslashes(trim(Request::post('upload_z_num'))); // 接口调用总次数

        if (empty($access_id))
        {
            ob_clean();
            $message = Lang('Unauthorized');
            return output(116,$message);
        }

        $Tools = new Tools($store_id, $store_type);
        $image_arr = array();
        $image_array = array();
        // 根据微信id,查询用户id
        $r = UserModel::where(['store_id' => $store_id,'access_id' => $access_id])
                        ->field('user_id,user_name')
                        ->select()
                        ->toArray();
        if (!empty($r))
        {
            $user_id = $r[0]['user_id'];
            $data_OrderDetails = array('store_id' => $store_id,'id' => $order_details_id,'user_id' => $user_id);
            $rr = OrderDetailsModel::where($data_OrderDetails)
                                    ->field('r_sNo as sNo')
                                    ->select()
                                    ->toArray();
            if ($rr)
            {
                $sNo = $rr[0]['sNo']; // 订单号
                // 查询配置表信息
                $r = ConfigModel::where('store_id',$store_id)
                                ->select()
                                ->toArray();
                $uploadImg = $r[0]['uploadImg'];
                $uploadImg_domain = $r[0]['uploadImg_domain'];
                $upserver = !empty($r) ? $r[0]['upserver'] : '2';   //如果没有设置配置则默认用阿里云

                //敏感词表
                require('../app/common/badword.php');

                $r_c = CommentsModel::where(['store_id' => $store_id,'oid' => $sNo,'pid' => $goodsId,'attribute_id' => $attribute_id])
                                    ->field('id,oid')
                                    ->select()
                                    ->toArray();
                $r_d = $r_c[0]['id'];
                if ($for_num)
                {
                    if (!empty($_FILES))
                    { // 如果图片不为空
                        if ($upserver == '2')
                        {
                            $image = ServerPath::file_OSSupload($store_id, $store_type);
                        }
                        else
                        {
                            $image = ServerPath::file_upload($store_id, $uploadImg, $uploadImg_domain, $store_type);
                        }
                        if ($image == false)
                        {
                            ob_clean();
                            $message = Lang('product.2');
                            return output(109,$message);
                        }
                        else
                        {
                            $image = preg_replace('/.*\//', '', $image);
                        }
                        $image_arr1 = array('image' => $image, 'call_num' => $call_num); // 图片数组

                        $cache = array('user_id' => $user_id, 'comment_id' => $r_d, 'image_arr' => $image_arr1); // 缓存数组
                    }
                    else
                    {
                        ob_clean();
                        $message = Lang('Parameter error');
                        return output(109,$message);
                    }
                    if ($call_num + 1 != $for_num)
                    { // 当前调用接口次数 不等于 总调用接口次数

                        $res = $Tools->generate_session($cache, 3);

                        ob_clean();
                        $message = Lang('Success');
                        return output(200,$message,$r_d);
                    }
                    else
                    {
                        $rew = $Tools->obtain_session($user_id, 3, $r_d);
                        if ($rew != '')
                        {
                            $image_arr2 = json_decode($rew, true);
                            if (count($image_arr2) == count($image_arr2, 1))
                            {
                                $image_arr[] = $image_arr2;
                                $image_arr[] = $image_arr1;
                            }
                            else
                            {
                                foreach ($image_arr2 as $k => $v)
                                {
                                    $image_arr[] = (array)$v;
                                }
                                array_push($image_arr, $image_arr1);
                            }
                        }
                        else
                        {
                            $image_arr[] = $image_arr1;
                        }
                        foreach ($image_arr as $ke => $va)
                        {
                            $image_array[$ke] = $va['image'];
                        }
                    }
                }

                //追评
                $time = date("Y-m-d H:i:s");

                $data_comments = array('store_id'=>$store_id,'oid'=>$sNo,'pid'=>$goodsId,'attribute_id'=>$attribute_id);
                $sql_1 = CommentsModel::where($data_comments)->find();
                $sql_1->review = $comment;
                $sql_1->review_time = $time;
                $r_1 = $sql_1->save();
                if ($r_1 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '修改评论失败!参数:' . json_encode($data_comments);
                    $this->Log($Log_content);
                    ob_clean();
                    $message = Lang('Abnormal business');
                    return output(110,$message);
                }
                if (count($image_array) != 0)
                {
                    foreach ($image_array as $k => $v)
                    {
                        $data_comments_img = array('comments_url'=>$v,'comments_id'=>$r_d,'type'=>1,'add_time'=>$time);
                        $res = Db::name('comments_img')->save($data_comments_img);
                        if ($res == -1)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '添加评论图片失败!参数:' . json_encode($data_comments_img);
                            $this->Log($Log_content);
                            ob_clean();
                            $message = Lang('Abnormal business');
                            return output(110,$message);
                        }
                    }
                }
                $res = $Tools->del_session($user_id, 3, $r_d);

                ob_clean();
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '订单详情ID错误!参数:' . json_encode($data_OrderDetails);
                $this->Log($Log_content);
                ob_clean();
                $message = Lang('Parameter error');
                return output(109,$message);
            }
        }
        else
        {
            ob_clean();
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
    }

    // 添加图片
    public function img_comment()
    {
        $store_id = addslashes(trim($_GET['store_id']));
        $store_type = addslashes(trim($_GET['store_type'])); // 来源
        $access_id = addslashes(trim($_POST['access_id'])); // 授权id
        $comment_id = addslashes(trim($_POST['cid'])); // 评论ID
        $call_num = addslashes(trim($_POST['upload_num'])); // 接口调用次数
        $for_num = addslashes(trim($_POST['upload_z_num'])); // 接口调用总次数
        $time = date("Y-m-d H:i:s");
        
        if (empty($access_id))
        {
            ob_clean();
            $message = Lang('Unauthorized');
            return output(116,$message);
        }
        $Tools = new Tools( $store_id,$store_type);
        $image_arr = array();
        $image_array = array();
        // 根据access_id,查询用户id
        $r = UserModel::where(['store_id' => $store_id,'access_id' => $access_id])
                        ->field('user_id,user_name')
                        ->select()
                        ->toArray();
        if (!empty($r))
        {
            $user_id = $r[0]['user_id'];
            $r_c = CommentsModel::where('id' ,$comment_id)
                                ->select()
                                ->toArray();
            if ($r_c)
            {
                // 查询配置表信息
                $r = ConfigModel::where('store_id' ,$store_id)
                                ->select()
                                ->toArray();
                $uploadImg = $r[0]['uploadImg'];
                $uploadImg_domain = $r[0]['uploadImg_domain'];
                $upserver = !empty($r) ? $r[0]['upserver'] : '2';   //如果没有设置配置则默认用阿里云

                if (!empty($_FILES))
                { // 如果图片不为空
                    if ($upserver == '2')
                    {
                        $image = ServerPath::file_OSSupload($store_id, $store_type);
                    }
                    else
                    {
                        $image = ServerPath::file_upload($store_id, $uploadImg, $uploadImg_domain, $store_type);
                    }
                    if ($image == false)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '上传失败或图片格式错误!';
                        $this->Log($Log_content);
                        ob_clean();
                        $message = Lang('product.2');
                        return output(109,$message);
                    }
                    else
                    {
                        $image = preg_replace('/.*\//', '', $image);
                    }
                    $image_arr1 = array('image' => $image, 'call_num' => $call_num); // 图片数组

                    $cache = array('user_id' => $user_id, 'comment_id' => $comment_id, 'image_arr' => $image_arr1); // 缓存数组
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '没有上传图片!';
                    $this->Log($Log_content);
                    ob_clean();
                    $message = Lang('Parameter error');
                    return output(109,$message);
                }
                if ($call_num + 1 != $for_num)
                { // 当前调用接口次数 不等于 总调用接口次数
                    $res = $Tools->generate_session($cache, 3);
                    ob_clean();
                    $message = Lang('Success');
                    return output(200,$message,$comment_id);
                }
                else
                {
                    $rew = $Tools->obtain_session($user_id, 3, $comment_id);
                    if ($rew != '')
                    {
                        $image_arr2 = json_decode($rew, true);
                        if (count($image_arr2) == count($image_arr2, 1))
                        {
                            $image_arr[] = $image_arr2;
                            $image_arr[] = $image_arr1;
                        }
                        else
                        {
                            foreach ($image_arr2 as $k => $v)
                            {
                                $image_arr[] = (array)$v;
                            }
                            array_push($image_arr, $image_arr1);
                        }
                    }
                    else
                    {
                        $image_arr[] = $image_arr1;
                    }
                    foreach ($image_arr as $ke => $va)
                    {
                        $image_array[$ke] = $va['image'];
                    }
                }

                //进入正式添加---开启事物
                foreach ($image_array as $k => $v)
                {
                    $data_comments_img = array('comments_url'=>$v,'comments_id'=>$comment_id,'add_time'=>$time);
                    $res = Db::name('comments_img')->save($data_comments_img);
                    if ($res == -1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '添加评论图片失败!参数:' . json_encode($data_comments_img);
                        $this->Log($Log_content);
                        ob_clean();
                        $message = Lang('Abnormal business');
                        return output(110,$message);
                    }
                }

                $res = $Tools->del_session($user_id, 3, $comment_id);

                ob_clean();
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '评论ID错误!comment_id:' . $comment_id;
                $this->Log($Log_content);
                ob_clean();
                $message = Lang('Parameter error');
                return output(109,$message);
            }
        }
        else
        {
            ob_clean();
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 获取未评论数据
    public function commentList()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id

        $order_details_id = addslashes(trim(Request::param('order_details_id'))); // 订单详情ID
        $orderNo = addslashes(trim(Request::param('orderNo'))); // 订单号
        $type = addslashes(trim(Request::param('type')));

        $list = array();
        // 根据微信id,查询用户id
        $r = UserModel::where(['store_id' => $store_id,'access_id' => $access_id])->field('user_id')->select()->toArray();
        if (!empty($r))
        {
            $user_id = $r[0]['user_id'];
            if($type == 1)
            { // 追评
                if($order_details_id != '' && $order_details_id != 'undefined')
                {
                    $sql_o = "select a.id,a.p_id as goodsId,a.p_name,a.p_price,a.num,a.unit,a.r_status,a.re_type,a.r_type,a.express_id,a.courier_num,a.freight,a.size,a.sid,a.mch_id,a.deliver_time,p.commodity_type,p.product_title,p.imgurl as goodsUrl,m.img,p.is_appointment,p.is_distribution
                        from lkt_order_details AS a 
                        LEFT JOIN lkt_configure AS m ON a.sid = m.id 
                        left join lkt_product_list as p on m.pid = p.id 
                        where a.store_id = '$store_id' and a.id = '$order_details_id' and a.r_status in(5,-1) and (a.id not in (select order_detail_id from lkt_comments where store_id = '$store_id' and uid = '$user_id' and order_detail_id = '$order_details_id' and review != '' ) and a.id not in (select p_id from lkt_return_order where p_id = '$order_details_id' and r_type in (0,1,3,4,9,11,13,15) ))";
                }
                else
                {
                    $sql_o = "select a.id,a.p_id as goodsId,a.p_name,a.p_price,a.num,a.unit,a.r_status,a.re_type,a.r_type,a.express_id,a.courier_num,a.freight,a.size,a.sid,a.mch_id,a.deliver_time,p.commodity_type,p.product_title,p.imgurl as goodsUrl,m.img,p.is_appointment,p.is_distribution
                        from lkt_order_details AS a 
                        LEFT JOIN lkt_configure AS m ON a.sid = m.id 
                        left join lkt_product_list as p on m.pid = p.id 
                        where a.store_id = '$store_id' and a.r_sNo = '$orderNo' and a.r_status in(5,-1) and (a.id not in (select order_detail_id from lkt_comments where store_id = '$store_id' and uid = '$user_id' and oid = '$orderNo' and review != '' ) and a.id not in (select p_id from lkt_return_order where sNo = '$orderNo' and r_type in (0,1,3,4,9,11,13,15) ))";
                }
            }
            else
            { // 立即评价
                if($orderNo != '' && $orderNo != 'undefined')
                {
                    $sql_o = "select a.id,a.p_id as goodsId,a.p_name,a.p_price,a.num,a.unit,a.r_status,a.re_type,a.r_type,a.express_id,a.courier_num,a.freight,a.size,a.sid,a.mch_id,a.deliver_time,p.commodity_type,p.product_title,p.imgurl as goodsUrl,m.img,p.is_appointment,p.is_distribution
                        from lkt_order_details AS a 
                        LEFT JOIN lkt_configure AS m ON a.sid = m.id 
                        left join lkt_product_list as p on m.pid = p.id 
                        where a.store_id = '$store_id' and a.r_sNo = '$orderNo' and a.r_status in(5,-1) and (a.id not in (select order_detail_id from lkt_comments where store_id = '$store_id' and uid = '$user_id' and oid = '$orderNo' ) and a.id not in (select p_id from lkt_return_order where sNo = '$orderNo' and r_type in (0,1,3,4,9,11,13,15) ))";
                }
                else
                {
                    $sql_o = "select a.id,a.p_id as goodsId,a.p_name,a.p_price,a.num,a.unit,a.r_status,a.re_type,a.r_type,a.express_id,a.courier_num,a.freight,a.size,a.sid,a.mch_id,a.deliver_time,p.commodity_type,p.product_title,p.imgurl as goodsUrl,m.img,p.is_appointment,p.is_distribution
                        from lkt_order_details AS a 
                        LEFT JOIN lkt_configure AS m ON a.sid = m.id 
                        left join lkt_product_list as p on m.pid = p.id 
                        where a.store_id = '$store_id' and a.id = '$order_details_id' and a.r_status in(5,-1) and (a.id not in (select order_detail_id from lkt_comments where store_id = '$store_id' and uid = '$user_id' and order_detail_id = '$order_details_id' ) and a.id not in (select p_id from lkt_return_order where p_id = '$order_details_id' and r_type in (0,1,3,4,9,11,13,15) ))";
                }
            }
            $r_o = Db::query($sql_o);
            if ($r_o)
            {
                foreach ($r_o as $key => $value)
                {
                    $value['commodityIcon'] = ServerPath::getimgpath($value['img']);
                    $value['commodityId'] = $value['goodsId'];

                    $list[] = $value;
                }

                ob_clean();
                $data = array('commentList' => $list);
                $message = Lang('Success');
                return output(200,$message, $data);
            }
            else
            {
                ob_clean();
                $data = array('commentList' => array());
                $message = Lang('Success');
                return output(200,$message, $data);
            }
        }
        else
        {
            ob_clean();
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
    }

    // 添加评论
    public function addBatch_comment()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id

        $commentList = trim(Request::param('commentList')); // 评论数据

        $comment_list = json_decode($commentList,true);
        $time = date("Y-m-d H:i:s");
        Db::startTrans();

        // 根据微信id,查询用户id
        $r = UserModel::where(['store_id' => $store_id,'access_id' => $access_id])->field('user_id')->select()->toArray();
        if (!empty($r))
        {
            $user_id = $r[0]['user_id'];

            foreach($comment_list as $k => $v)
            {
                $image_array = $v['imgUrls'];
                $order_details_id = $v['order_details_id']; // 订单详情ID
                $comment = isset($v['comment'])?$v['comment']:'好评'; // 评论内容
                $anonymous = $v['anonymous']; // 是否匿名

                $rr = OrderDetailsModel::where(['store_id' => $store_id,'id' => $order_details_id,'user_id' => $user_id])->field('r_sNo as sNo,p_id,sid')->select()->toArray();
                if ($rr)
                {
                    $sNo = $rr[0]['sNo']; // 订单号
                    $goodsId = $rr[0]['p_id']; // 商品ID
                    $attribute_id = $rr[0]['sid']; // 属性ID

                    $ro = OrderModel::where(['store_id' => $store_id,'sNo' => $sNo,'user_id' => $user_id])->field('otype')->select()->toArray();
                    $otype = $ro[0]['otype'];
                    if($otype == 'IN')
                    {
                        $sql_i = "select goods_id from lkt_integral_goods where id = '$goodsId' ";
                        $r_i = Db::query($sql_i);
                        if($r_i)
                        {
                            $goodsId = $r_i[0]['goods_id']; // 商品ID
                        }
                    }

                    $r_c = CommentsModel::where(['store_id' => $store_id,'oid' => $sNo,'pid' => $goodsId,'attribute_id' => $attribute_id])->field('id,oid')->select()->toArray();
                    if($r_c)
                    { // 存在，追评
                        $comments_id = $r_c[0]['id'];
                        if ($comment != '')
                        {
                            require('../app/common/badword.php');

                            $badword1 = array_combine($badword, array_fill(0, count($badword), '*'));
                            $content = preg_replace("/\s(?=\s)/", "\\1", $this->strtr_array($comment, $badword1));

                            $data_comments = array('store_id'=>$store_id,'oid'=>$sNo,'pid'=>$goodsId,'attribute_id'=>$attribute_id);
                            $sql_1 = CommentsModel::where($data_comments)->find();
                            $sql_1->review = $comment;
                            $sql_1->review_time = $time;
                            $r_1 = $sql_1->save();
                            if ($r_1 <= 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '修改评论失败!参数:' . json_encode($data_comments);
                                $this->Log($Log_content);
                                Db::rollback();
                                ob_clean();
                                $message = Lang('Abnormal business');
                                return output(110,$message);
                            }

                            if($image_array != array())
                            { // 图片不为空数组
                                foreach ($image_array as $k => $v)
                                {
                                    $data_comments_img = array('comments_url'=>$v,'comments_id'=>$comments_id,'type'=>1,'add_time'=>$time);
                                    $res = Db::name('comments_img')->save($data_comments_img);
                                    if ($res == -1)
                                    {
                                        $Log_content = __METHOD__ . '->' . __LINE__ . '添加评论图片失败!参数:' . json_encode($data_comments_img);
                                        $this->Log($Log_content);
                                        Db::rollback();
                                        ob_clean();
                                        $message = Lang('Abnormal business');
                                        return output(110,$message);
                                    }
                                }
                            }
                        }
                    }
                    else
                    { // 不存在 立即评价
                        if ($comment != '')
                        {
                            $start = $v['start']; // 评级
                            require('../app/common/badword.php');

                            $badword1 = array_combine($badword, array_fill(0, count($badword), '*'));
                            $content = preg_replace("/\s(?=\s)/", "\\1", $this->strtr_array($comment, $badword1));

                            $data_comments = array('store_id' => $store_id,'oid' => $sNo,'uid' => $user_id,'pid' => $goodsId,'attribute_id' => $attribute_id,'content' => $comment,'CommentType' => $start,'anonymous' => $anonymous,'add_time' => $time,'order_detail_id'=>$order_details_id);
                            $cid = Db::name('comments')->insertGetId($data_comments);
                            if ($cid <= 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '添加评论数据失败!参数:' . json_encode($data_comments);
                                $this->Log($Log_content);
                                Db::rollback();
                                ob_clean();
                                $message = Lang('Abnormal business');
                                return output(110,$message);
                            }

                            // 增加商品评论数
                            $sql_p = "update lkt_product_list set comment_num = comment_num + 1 where store_id = '$store_id' and id = '$goodsId'";
                            $res_p = Db::execute($sql_p);
                            if ($res_p < 1)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '商品评论数增加失败!sql:' . $sql_p;
                                $this->Log($Log_content);
                                Db::rollback();
                                $message = Lang('Abnormal business');
                                return output(110,$message . 4);
                            }

                            if($image_array != array())
                            { // 图片不为空数组
                                foreach ($image_array as $k1 => $v1)
                                {
                                    $data_comments_img = array('comments_url'=>$v1,'comments_id'=>$cid,'add_time'=>$time);
                                    $res = Db::name('comments_img')->save($data_comments_img);
                                    if ($res == -1)
                                    {
                                        $Log_content = __METHOD__ . '->' . __LINE__ . '添加评论图片失败!参数:' . json_encode($data_comments_img);
                                        $this->Log($Log_content);
                                        Db::rollback();
                                        ob_clean();
                                        $message = Lang('Abnormal business');
                                        return output(110,$message);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Db::commit();
            ob_clean();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            ob_clean();
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/product.log",$Log_content);
        return;
    }

    //替换
    function strtr_array($str, $replace_arr)
    {
        $maxlen = 0;
        $minlen = 1024 * 128;
        if (empty($replace_arr)) return $str;
        foreach ($replace_arr as $k => $v)
        {
            $len = strlen($k);
            if ($len < 1) continue;
            if ($len > $maxlen) $maxlen = $len;
            if ($len < $minlen) $minlen = $len;
        }
        $len = strlen($str);
        $pos = 0;
        $result = '';
        while ($pos < $len)
        {
            if ($pos + $maxlen > $len) $maxlen = $len - $pos;
            $found = false;
            $key = '';
            for ($i = 0; $i < $maxlen; ++$i) $key .= $str[$i + $pos];
            for ($i = $maxlen; $i >= $minlen; --$i)
            {
                $key1 = substr($key, 0, $i);
                if (isset($replace_arr[$key1]))
                {
                    $result .= $replace_arr[$key1];
                    $pos += $i;
                    $found = true;
                    break;
                }
            }
            if (!$found) $result .= $str[$pos++];
        }
        return $result;
    }
}

?>

