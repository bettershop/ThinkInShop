<?php

namespace app\common;
use think\facade\Db;
use app\admin\model\ConfigModel;
use app\admin\model\AdminModel;
use app\admin\model\UserModel;
use app\admin\model\AdminCgGroupModel;
use app\admin\model\AdminRecordModel;
use app\admin\model\SessionIdModel;
use app\admin\model\MessageConfigModel;
use app\admin\model\BuyAgainModel;
use app\admin\model\CartModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProductListModel;
use app\admin\model\MchModel;
use app\admin\model\FreightModel;
use app\admin\model\UserAddressModel;
use app\admin\model\OrderConfigModel;
use app\admin\model\SkuModel;
use app\admin\model\ProductClassModel;
use app\admin\model\CouponActivityModel;
use app\admin\model\UserRuleModel;
use app\admin\model\MchStoreModel;
use app\admin\model\DataDictionaryListModel;
use app\admin\model\DataDictionaryNameModel;
use app\admin\model\AuctionProductModel;
use app\admin\model\BannerModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\ReturnOrderModel;
use app\admin\model\PreSellRecordModel;
use app\admin\model\MchPromiseModel;
use app\admin\model\AuctionPromiseModel;
use app\admin\model\MemberConfigModel;
use app\admin\model\IntegralConfigModel;
use app\admin\model\SecondsConfigModel;
use app\admin\model\PreSellConfigModel;
use app\admin\model\GroupOrderConfigModel;
use app\admin\model\MchConfigModel;
use app\admin\model\WithdrawModel;

use think\facade\lang;
use think\facade\Cache;
use app\common\LaiKeLogUtils;
use app\common\third\authorize\Third;
use app\common\JWT;
use app\common\ServerPath;
use app\common\Plugin_order;
use app\common\GETUI\LaikePushTools;
use app\common\Formatter;
use app\common\PC_Tools;

require_once 'aliyun-dysms-php-sdk-lite/demo/sendSms.php';
/**
 * 功能：公共应用类
 */
class Tools
{
    public function __construct( $store_id, $store_type)
    {
        $this->store_id = $store_id;
        $this->store_type = $store_type;
        $this->get_config($store_id);
    }

    public function get_config($store_id)
    {
        $r = ConfigModel::where('store_id', $store_id)
                        ->select()
                        ->toArray();
        if ($r)
        {
            $this->config = $r[0];
        }
        else
        {
            $this->config = array();
        }
    }

    // 获取支付方式状态
    public static function getPayment($store_id,$type = 0)
    {
        $res = array();
        if($type == 1)
        {
            require_once(MO_LIB_DIR.'/PayMappingUtils.php');

            $sql = "select p.class_name from lkt_payment_config c left join lkt_payment p on c.pid=p.id where c.store_id='$store_id' and c.isdefaultpay = 1";
            $r = Db::query($sql);
            if($r)
            {
                $class_name = $r[0]['class_name'];

                $res['defaultpayName'] = $PayMappingUtils[$class_name];
            }
        }
        else
        {
            $sql = "select c.status,p.class_name from lkt_payment_config c left join lkt_payment p on c.pid=p.id where c.store_id='$store_id'";
            $r = Db::query($sql);
            foreach ($r as $k => $v)
            {
                $res[$v['class_name']] = $v['status'];
            }
        }

        // $res['offline_payment'] = 0;
        // if($type == 2)
        // {
        //     $res['offline_payment'] = 1;
        // }

        return $res;
    }


    // 获取地区
    public static function get_access_area( $store_id, $longitude, $latitude)
    {
        // $log = new LaiKeLogUtils();

        // $from = $latitude . ',' . $longitude;

        // $r0_0 = ConfigModel::where('store_id', $store_id)
        //                     ->field('tencent_key')
        //                     ->select()
        //                     ->toArray();
        // if ($r0_0)
        // {
        //     $key = $r0_0[0]['tencent_key'];
        // }
        // else
        // {
        //     $message = Lang('index.0');
        //     return output(109,$message);
        // }

        // $url = "https://apis.map.qq.com/ws/geocoder/v1/?location=$from&key=$key&get_poi=1";
        // // 初始url会话
        // $ch = new Third();
        // $data = $ch->https_get($url, 1);
        // $log->log("app/laiketui_address.log",__LINE__ . "->返回数据：" . $data );

        // $map = json_decode($data);
        $province = '';
        $city = '';
        $district = '';
        // if(isset($map->result->ad_info))
        // {
        //     $province = $map->result->ad_info->province;
        //     $city = $map->result->ad_info->city;
        //     $district = $map->result->ad_info->district;
        // }

        $province_GroupID = '';
        $city_GroupID = '';
        $district_GroupID = '';
        // $r0 = AdminCgGroupModel::where('district_name', $province)
        //                 ->field('id')
        //                 ->select()
        //                 ->toArray();
        // if ($r0)
        // {
        //     $province_GroupID = $r0[0]['id'];
        // }
        // $r1 = AdminCgGroupModel::where('district_name', $city)
        //                 ->field('id')
        //                 ->select()
        //                 ->toArray();
        // if ($r1)
        // {
        //     $city_GroupID = $r1[0]['id'];
        // }

        // $r2 = AdminCgGroupModel::where('district_name', $district)
        //                 ->field('id')
        //                 ->select()
        //                 ->toArray();
        // if ($r2)
        // {
        //     $district_GroupID = $r2[0]['id'];
        // }
        // TODO 等接口好了删除这里
      
        $data = array('province' => $province, 'city' => $city, 'district' => $district, 'province_GroupID' => $province_GroupID, 'city_GroupID' => $city_GroupID, 'district_GroupID' => $district_GroupID);

        return $data;
    }

    // 用户生成密钥
    public static function getToken($store_id,$store_type)
    {   
        //获取有效期
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
        
        $payload = array('iat' => time(), 'exp' => time() + $exp_time,'store_type'=>$store_type, 'jti' => md5(uniqid('JWT') . time()));
        
        $token = Jwt::getToken($payload);
        
        cache($token, $token, $exp_time);
        return $token;
    }

    // 用户生成密钥
    public static function getTokenSupplier($store_id,$isAuto = 0)
    {   
        //获取有效期
        $res = ConfigModel::where('store_id', $store_id)
                        ->field('exp_time')
                        ->select()
                        ->toArray();
        if($isAuto)
        {
            $exp_time = 3600*24*7;
        }
        else
        {
            $exp_time = 7200;
        }
        $payload = array('iat' => time(), 'exp' => time() + $exp_time,'store_type'=>8, 'jti' => md5(uniqid('JWT') . time()));
        $token = Jwt::getToken($payload);
        return $token;
    }

    // 对用户token进行验证签名,如果过期返回false,成功返回数组
    public static function verifyToken($access_id)
    {   
        $res = cache($access_id);
        if($res)
        {
            $getPayload_test = Jwt::verifyToken($access_id);
        }
        else
        {
            $getPayload_test = false;
        }
        return $getPayload_test;
    }

    //后台生成秘钥
    public static function getAdminToken($id)
    {   
        //获取有效期
        $exp_time = 3600;
        $payload = array('iat' => time(), 'exp' => time() + $exp_time,'store_type'=>8,'id'=>$id, 'jti' => md5(uniqid('JWT') . time()));
        $token = Jwt::getToken($payload);
        return $token;
    }

    // 获取商城门店信息
    public static function get_store($store_id,$longitude, $latitude, $page, $pagesize,$cid = '')
    {
        $start = ($page - 1) * $pagesize;
        $end = $page * $pagesize;

        $list = array();
        $list0 = array();
        $con = " m.store_id = '$store_id' and m.review_status = 1 and m.recovery = 0 ";
        if($cid != '')
        {
            $con .= " and m.cid = '$cid' ";
        }
        $sql = "select m.id as shop_id,m.name,m.shop_information,m.latitude,m.longitude,m.logo,m.head_img as headImg,m.poster_img as posterImg,m.collection_num,m.review_time,u.user_id,u.user_name,u.headimgurl,u.source from lkt_mch as m left join lkt_user as u on m.user_id = u.user_id where $con order by m.collection_num desc,m.review_time asc limit $start,$pagesize ";
        $res = Db::query($sql);
        if($res)
        {   
            foreach($res as $k => $v)
            {
                $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id);// 商品图  
                $v['headImg'] = ServerPath::getimgpath($v['headImg'], $store_id);// 商品图 
                $v['posterImg'] = ServerPath::getimgpath($v['posterImg'], $store_id);// 商品图  
                $v['backImgUrl'] = $v['posterImg'];// 商品图  
                $v['headimgurl'] = $v['headImg'];// 商品图  
                $latitude1 = $v['latitude'];
                $longitude1 = $v['longitude'];
                $v['LongitudeAndLatitude'] = $latitude1 . ',' . $longitude1;
                // if($latitude1 != '' && $longitude1 != '')
                // {
                //     $v['distance'] = Tools::getDistance($latitude, $longitude, $latitude1, $longitude1);
                    $list0[] = $v;
                // }
            }
        }

        return $list0;
    }

    // 根据两点间的经纬度计算距离 
    public static function getDistance($lat1, $lng1, $lat2, $lng2) 
    { 
        $earthRadius = 6367000; //approximate radius of earth in meters 
        
        // Convert these degrees to radians to work with the formula 
        
        $lat1 = ($lat1 * pi() ) / 180; 
        $lng1 = ($lng1 * pi() ) / 180; 

        $lat2 = ($lat2 * pi() ) / 180; 
        $lng2 = ($lng2 * pi() ) / 180; 
        
        // Using the Haversine formula http://en.wikipedia.org/wiki/Haversine_formula calculate the distance 
        $calcLongitude = $lng2 - $lng1; 
        $calcLatitude = $lat2 - $lat1; 
        $stepOne = pow(sin($calcLatitude / 2), 2) + cos($lat1) * cos($lat2) * pow(sin($calcLongitude / 2), 2); 
        $stepTwo = 2 * asin(min(1, sqrt($stepOne))); 
        $calculatedDistance = $earthRadius * $stepTwo; 
        
        return round($calculatedDistance); 
    }

    // 二维数组排序
    public static function arraySort($array, $keys, $sort = 'asc')
    {
        $newArr = $valArr = array();
        foreach ($array as $key => $value)
        {
            $valArr[$key] = $value[$keys];
        }
        ($sort == 'asc') ? asort($valArr) : arsort($valArr);
        reset($valArr);
        foreach ($valArr as $key => $value)
        {
            $newArr[$key] = $array[$key];
        }
        return $newArr;
    }

    //加密函数
    public static function lock_url($txt, $key = 'www.jb51.net')
    {
        $chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-=+";
        $nh = rand(0, 64);
        $ch = $chars[$nh];
        $mdKey = md5($key . $ch);
        $mdKey = substr($mdKey, $nh % 8, $nh % 8 + 7);
        $txt = base64_encode($txt);
        $tmp = '';
        $i = 0;
        $j = 0;
        $k = 0;
        for ($i = 0; $i < strlen($txt); $i++)
        {
            $k = $k == strlen($mdKey) ? 0 : $k;
            $j = ($nh + strpos($chars, $txt[$i]) + ord($mdKey[$k++])) % 64;
            $tmp .= $chars[$j];
        }
        return urlencode($ch . $tmp);
    }

    //解密函数
    public static function unlock_url($txt, $key = 'www.jb51.net')
    {
        $txt = urldecode($txt);
        $chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-=+";
        $ch = $txt[0];
        $nh = strpos($chars, $ch);
        $mdKey = md5($key . $ch);
        $mdKey = substr($mdKey, $nh % 8, $nh % 8 + 7);
        $txt = substr($txt, 1);
        $tmp = '';
        $i = 0;
        $j = 0;
        $k = 0;
        for ($i = 0; $i < strlen($txt); $i++)
        {
            $k = $k == strlen($mdKey) ? 0 : $k;
            $j = strpos($chars, $txt[$i]) - $nh - ord($mdKey[$k++]);
            while ($j < 0)
            {
                $j += 64;
            }

            $tmp .= $chars[$j];
        }
        return base64_decode($tmp);
    }

    /**
     * [check_phone description]
     * <p>Copyright (c) 2018-2019</p>
     * <p>Company: www.laiketui.com</p>
     * @Author  苏涛
     * @param   [type]                   $mobile [description]
     * @return  [type]                验证手机号 [description]
     * @version 2.0
     * @date    2019-01-10T19:16:19+0800
     */
    public static function check_phone($mobile)
    {
        if (preg_match("/^(13[0-9]|14[0-9]|15[0-9]|16[0-9]|17[0-9]|18[0-9]|19[0-9])\\d{8}$/", $mobile))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // 发送验证码
    public function generate_code($array)
    {
        $time = date('Y-m-d H:i:s'); // 当前时间
        $code = rand(100000, 999999);
        $store_id = $this->store_id;
        $cpc = $array['cpc']; // 区号
        $mobile = $array['mobile']; // 电话号码
        $type = $array['type']; // 类型 0:验证码 1:自定义
        $type1 = $array['type1']; // 类别 0:通用 1:申请通过 2:申请拒绝 3:订单提现 4:发货提现 5:收货提现
        $bizparams = array();
        if(isset($array['bizparams']))
        {
            $bizparams = $array['bizparams'];
        }

        $sql = "delete from lkt_session_id where date_add(add_date, interval 5 minute) < now() ";
        Db::execute($sql);

        if($cpc == '86' || $cpc == '')
        {
            $cpc = '86';
            $international = 0;
        }
        else
        {
            $international = 1;
        }

        $mobile = $cpc . $mobile;

        $r0 = MessageConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r0)
        {
            $accessKeyId = $r0[0]['accessKeyId'];
            $accessKeySecret = $r0[0]['accessKeySecret'];
            if ($type == 0)
            {
                $TemplateParam = array('code' => $code); // 验证码
                $sql1 = "select SignName,TemplateCode from lkt_message where store_id = '$store_id' and international = '$international' and type = 0 and type1 = '$type1'";
                $r1 = Db::query($sql1);
                if ($r1)
                {
                    $SignName = $r1[0]['SignName'];
                    $TemplateCode = $r1[0]['TemplateCode'];
                }
                else
                {
                    $sql2 = "select SignName,TemplateCode from lkt_message where store_id = '$store_id' and international = '$international' and type = 0 and type1 = '1'";
                    $r2 = Db::query($sql2);
                    if ($r2)
                    {
                        $SignName = $r2[0]['SignName'];
                        $TemplateCode = $r2[0]['TemplateCode'];
                    }
                    else
                    {
                        $message = Lang('tools.1');
                        echo json_encode(array('code' => ERROR_CODE_DXMBBCZ, 'message' => $message));
                        exit();
                    }
                }
            }
            else
            {
                $sql2 = "select b.SignName,TemplateCode,content from lkt_message where store_id = '$store_id' and international = '$international' and type = '$type' and type1 = '$type1' ";
                $r2 = Db::query($sql2);
                if ($r2)
                {
                    $SignName = $r2[0]['SignName'];
                    $TemplateCode = $r2[0]['TemplateCode'];

                    preg_match_all("/(?<={)[^}]+/", $r2[0]['content'], $result);
                    $content1 = array_combine($result[0], $content);
                    $content = $content1;
                    foreach ($content as $k => $v)
                    {
                        if ($k == 'code')
                        {
                            $content['code'] = $code;
                        }
                        else if ($k == 'orderno')
                        {
                            if (isset($bizparams['sNo']))
                            {
                                $content['orderno'] = $bizparams['sNo'];
                            }
                            else if (isset($bizparams['orderno']))
                            {
                                $content['orderno'] = $bizparams['orderno'];
                            }
                        }
                        else if ($k == 'store')
                        {
                            if (isset($bizparams['mch_name']))
                            {
                                $content['store'] = $bizparams['mch_name'];
                            }
                        }
                        else if ($k == 'amount')
                        {
                            if (isset($bizparams['money']))
                            {
                                $content['amount'] = $bizparams['money'];
                            }
                        }
                        else
                        {
                            $content[$k] = $bizparams[$k];
                        }
                    }
                    $TemplateParam = $content;
                }
            }

            $arr = array($mobile, $TemplateParam);
            $content1 = json_encode($arr); // 数组转json字符串

            $res = sendSms($accessKeyId, $accessKeySecret, $SignName, $mobile, $TemplateCode, $TemplateParam);
            if ($type == 0)
            {
                if ($res->Code == 'OK')
                {
                    $rew = 0; // 用来判断，是否有短信数据。0代表没有，1代表有
                    $r1 = SessionIdModel::select()->toArray();
                    if ($r1)
                    {
                        foreach ($r1 as $k => $v)
                        {
                            $content2 = json_decode($v['content']);
                            if (($mobile == $content2[0]))
                            {
                                $update = array('content' => $content1);
                                Db::name('session_id')->where('id', $v['id'])->update($update);
                                $rew = 1;
                            }
                        }
                    }
                    if ($rew == 0)
                    {
                        $insert = array('content' => $content1, 'add_date' => $time);
                        Db::name('session_id')->insert($insert);
                    }
                    $message = Lang('Success');
                    echo json_encode(array('code' => "200",'data'=>true, 'message' => $message));
                    exit();
                }
                else
                {
                    if ($res->Code == 'isv.OUT_OF_SERVICE')
                    {
                        $message = Lang('tools.2');
                        echo json_encode(array('code' => ERROR_CODE_DXFSSB, 'message' => $message));
                        exit();
                    }
                    else if ($res->Code == 'isv.SMS_TEMPLATE_ILLEGAL')
                    {
                        $message = Lang('tools.3');
                        echo json_encode(array('code' => ERROR_CODE_DXFSSB, 'message' => $message));
                        exit();
                    }
                    else if ($res->Code == 'isv.SMS_SIGNATURE_ILLEGAL')
                    {
                        $message = Lang('tools.4');
                        echo json_encode(array('code' => ERROR_CODE_DXFSSB, 'message' => $message));
                        exit();
                    }
                    else if ($res->Code == 'isv.MOBILE_NUMBER_ILLEGAL')
                    {
                        $message = Lang('tools.6');
                        echo json_encode(array('code' => ERROR_CODE_DXFSSB, 'message' => $message));
                        exit();
                    }
                    else if ($res->Code == 'isv.MOBILE_COUNT_OVER_LIMIT')
                    {
                        $message = Lang('tools.5');
                        echo json_encode(array('code' => ERROR_CODE_DXFSSB, 'message' => $message));
                        exit();
                    }
                    else if ($res->Code == 'isv.BUSINESS_LIMIT_CONTROL')
                    {
                        $message = Lang('tools.7');
                        echo json_encode(array('code' => ERROR_CODE_DXFSSB, 'message' => $message));
                        exit();
                    }
                    else if ($res->Code == 'isv.AMOUNT_NOT_ENOUGH')
                    {
                        $message = Lang('tools.8');
                        echo json_encode(array('code' => ERROR_CODE_DXFSSB, 'message' => $message));
                        exit();
                    }
                    else
                    {
                        $message = Lang('tools.9');
                        echo json_encode(array('code' => ERROR_CODE_DXFSSB, 'message' => $message));
                        exit();
                    }
                }
            }
            else
            {
                return json_encode($res);
            }
        }
        else
        {
            $message = Lang('tools.1');
            echo json_encode(array('code' => ERROR_CODE_DXFSSB, 'message' => $message));
            exit();
        }
    }

    // 验证验证码
    public function verification_code($arr)
    {
        $time = date('Y-m-d H:i:s'); // 当前时间
        $status = 0;
        $store_id = $this->store_id;
        $sql = "delete from lkt_session_id where date_add(add_date, interval 5 minute) < now() ";
        Db::execute($sql);

        $r1 = SessionIdModel::select();
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $id = $v['id'];
                $content1 = json_decode($v['content']);
                if ($arr[0] == $content1[0])
                {
                    if (isset($content1[1]->code))
                    {
                        if ($arr[1]['code'] != $content1[1]->code)
                        {
                            $message = Lang('tools.17');
                            echo json_encode(array('code' => ERROR_CODE_YZMBZQ, 'message' => $message));
                            exit;
                        }
                        else
                        {
                            $status = 1;
                        }
                    }
                }
            }
        }

        if ($status == 0)
        {
            $message = Lang('tools.11');
            echo json_encode(array('code' => ERROR_CODE_QZXHQYZM, 'message' => $message));
            exit;
        }
        return $id;
    }

    //区分购物车结算和立即购买---列出选购商品
    public static function products_list($store_id,$cart_id, $product, $product_type = 'PT',$buy_type = 0)
    {
        $products = [];
        if (!empty($cart_id))
        {
            if (is_string($cart_id))
            { //是字符串
                $typestr = trim($cart_id, ','); // 移除两侧的逗号
                $cartArr = explode(',', $typestr); // 字符串打散为数组
            }
            else
            {
                $cartArr = $cart_id;
            }
            
            foreach ($cartArr as $key => $value)
            {   
                if($buy_type != 0)
                {
                    $y_cart = BuyAgainModel::where(['store_id'=>$store_id,'id'=>$value])->where('Goods_num','>',0)->field('Goods_id,Size_id,Goods_num')->select()->toArray();
                }
                else
                { // 根据购物车id，查询购物车信息
                    $y_cart = CartModel::where(['store_id'=>$store_id,'id'=>$value])->where('Goods_num','>',0)->field('Goods_id,Size_id,Goods_num')->select()->toArray();
                }
                if ($y_cart)
                {
                    $products[$key] = array('pid' => $y_cart['0']['Goods_id'], 'cid' => $y_cart['0']['Size_id'], 'num' => $y_cart['0']['Goods_num']);
                }
            }
        }
        else
        {
            $arr = array();
            if (empty($product))
            {
                $message = Lang('tools.12');
                echo json_encode(array('code' => ERROR_CODE_LBYSPKCBZ, 'message' => $message));
                exit;
            }
            else
            {
                $typestr = trim($product, ','); // 移除两侧的逗号
                $cartArr = explode(',', $typestr); // 字符串打散为数组

                $arr['pid'] = $cartArr[0];
                $arr['cid'] = $cartArr[1];
                $arr['num'] = $cartArr[2];
                if($product_type == 'IN')
                {
                    $sql0 = "select num from lkt_integral_goods where store_id = '$store_id' and goods_id = '$cartArr[0]' and attr_id = '$cartArr[1]' and is_delete = 0 ";
                    $r0 = Db::query($sql0);
                    if($r0)
                    {
                        $num = $r0[0]['num'];
                        if ($num < $cartArr[2])
                        {
                            $message = Lang('tools.14');
                            echo json_encode(array('code' => ERROR_CODE_KCBZ, 'message' => $message));
                            exit;
                        }
                    }
                    else
                    {
                        $message = Lang('Parameter error');
                        echo json_encode(array('code' => ERROR_CODE_SPCSCW, 'message' => $message));
                        exit;
                    }
                }
                else
                {
                    //秒杀商品不检查库存
                    if($product_type != 'MS')
                    {
                        $r0 = ProductListModel::where('id',$cartArr[0])->field('status')->select()->toArray();
                        if ($r0)
                        {
                            $status = $r0[0]['status'];
                            if ($status == 1)
                            {
                                $message = Lang('tools.13');
                                echo json_encode(array('code' => ERROR_CODE_SPWSJ, 'message' => $message));
                                exit;
                            }
                            if($product_type == 'ZB')
                            {
                                $sql1 = "select num from lkt_living_product where living_id = " . $cartArr[3] . " and pro_id = " . $cartArr[0] . " and config_id = " . $cartArr[1];
                                $r1 = Db::query($sql1);
                            }
                            else
                            {
                                $r1 = ConfigureModel::where(['pid'=>$cartArr[0],'id'=>$cartArr[1]])->field('num')->select()->toArray();
                            }
                            if ($r1)
                            {
                                $num = $r1[0]['num'];
                                if ($num < $cartArr[2])
                                {
                                    if ($product_type != 'KJ')
                                    {
                                        $message = Lang('tools.14');
                                        echo json_encode(array('code' => ERROR_CODE_KCBZ, 'message' => $message));
                                        exit;
                                    }
                                }
                            }
                            else
                            {
                                $message = Lang('Parameter error');
                                echo json_encode(array('code' => ERROR_CODE_SPCSCW, 'message' => $message));
                                exit;
                            }
                        }
                        else
                        {
                            $message = Lang('Parameter error');
                            echo json_encode(array('code' => ERROR_CODE_SPCSCW, 'message' => $message));
                            exit;
                        }
                    }
                }
            }
            if($product_type == 'ZB')
            {
                $arr['roomId'] = $cartArr[3];
                $products[0] = array('pid' => intval($arr['pid']), 'cid' => intval($arr['cid']), 'num' => $arr['num'] ? intval($arr['num']) : 1, 'roomId' => $arr['roomId'] ? intval($arr['roomId']) : 0);
            }
            else
            {
                $products[0] = array('pid' => intval($arr['pid']), 'cid' => intval($arr['cid']), 'num' => $arr['num'] ? intval($arr['num']) : 1);
            }
        }

        if (empty($products))
        {
            $message = Lang('tools.12');
            echo json_encode(array('code' => ERROR_CODE_LBYSPKCBZ, 'message' => $message));
            exit;
        }

        $list = array();
        $list0 = array();
        $list1 = array();
        $list2 = array();

        foreach($products as $k => $v)
        {
            if(in_array($v['pid'],$list0))
            {
                if($v['num'] > $list1[$v['pid']])
                {
                    $list1[$v['pid']] = $v['num'];
                }
                $list[$v['pid']] += $v['num'];
            }
            else
            {
                $list0[] = $v['pid'];
                $list[$v['pid']] = $v['num'];
                $list1[$v['pid']] = $v['num'];
            }
        }
        //tongbu？同件商品不同规格
        foreach($products as $k => $v)
        {
            if($v['num'] == $list1[$v['pid']])
            {
                if(!in_array($v['pid'],$list2))
                {
                    $list2[] = $v['pid'];
                    $products[$k]['tongbu'] = 1;
                }
                else
                {
                    $products[$k]['tongbu'] = 2;
                }
            }
            else
            {
                $products[$k]['tongbu'] = 2;
            }
            //最终合计数量
            $products[$k]['merge_num'] = $list[$v['pid']]; 
        }

        return $products;
    }

    // 列出选购商品，--竞拍商品
    public static function products_JP_list($store_id, $auction_id)
    {
        $product = array();

        $r0 = AuctionProductModel::where(['id'=>$auction_id])->field('goods_id,attr_id')->select()->toArray();
        if($r0)
        {
            $goods_id = $r0[0]['goods_id']; // 商品id
            $attr_id = $r0[0]['attr_id']; // 商品规格id

            $product[] = array('pid'=>$goods_id,'cid'=>$attr_id,'num'=>1,'merge_num'=>1,'tongbu'=>1);
        }
        else
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 110, 'message' => $message));
            exit;
        }

        return $product;
    }

    //订单商品数据分组处理
    public static function get_products_data($store_id,$products, $products_total = 0, $product_type = 'GM',$shop_address_id = 0)
    {
        if (empty($products) && $product_type != 'KJ')
        {
            $message = Lang('Parameter error');
            echo json_encode(array('code' => ERROR_CODE_SPCSCW, 'message' => $message));
            exit;
        }
        $is_supplier_pro = true; // 不是供应商商品
        $str = "m.product_title,m.volume,c.price,c.unit,c.attribute,c.img,c.yprice,m.freight,m.product_class,m.brand_id,m.weight,m.weight_unit,m.mch_id,m.is_distribution,m.supplier_superior,m.gongyingshang,c.supplier_superior as supplier_cid,m.commodity_type,m.is_appointment,m.write_off_settings,m.write_off_mch_ids,m.receiving_form,c.write_off_num";
        foreach ($products as $key => $value)
        {
            $pid = $value['pid'];
            $cid = $value['cid'];
            $num = $value['num'];
            $merge_num = $value['merge_num'];
            $tongbu = $value['tongbu'];
            if($product_type == 'ZB')
            {
                $roomId = $value['roomId'];
                $str .= ",l.live_price,c.commission";
                $sql_list = "select $str from lkt_product_list AS m LEFT JOIN lkt_configure AS c ON m.id = c.pid left join lkt_living_product as l on c.id = l.config_id where m.store_id = '$store_id' and l.num > 0 and l.num >= $num and m.status = 2 and m.id = '$pid' and c.id = '$cid' and l.living_id = '$roomId' ";
            }
            else
            {
                $sql_list = "select $str from lkt_product_list AS m LEFT JOIN lkt_configure AS c ON m.id = c.pid  where m.store_id = '$store_id' and c.num > 0 and c.num >= $num and m.status = 2 and m.id = '$pid' and c.id = '$cid'";
            }
            $r_list = Db::query($sql_list);
            if ($r_list)
            {
                $value = array_merge($value, $r_list[0]);
            }
            else
            {
                if ($product_type != 'KJ' && $product_type != 'MS' && $product_type != 'IN')
                {
                    $message = Lang('tools.12');
                    echo json_encode(array('message' => $message,'code' => ERROR_CODE_LBYSPKCBZ));
                    exit;
                }
                else
                {
                    $sql_list = "select $str from lkt_product_list AS m LEFT JOIN lkt_configure AS c ON m.id = c.pid  where m.store_id = '$store_id' and m.status = 2 and m.id = '$pid' and c.id = '$cid'";
                    $r_list = Db::query($sql_list);
                    $value = array_merge($value, $r_list[0]);
                }
            }
            $attribute = unserialize($value['attribute']);
            $product_id[] = $pid;
            $product_class[] = $value['product_class'];
            $size = '';
            foreach ($attribute as $ka => $va)
            {
                if (strpos($ka, '_LKT_') !== false)
                {
                    $ke = substr($ka, 0, strrpos($ka, "_LKT"));
                    $va = substr($va, 0, strrpos($va, "_LKT"));
                    $size .= $ke . ":" . $va . ";";
                }
                else
                {
                    $size .= $ka . ":" . $va . ";";
                }
            }
            $value = self::array_key_remove($value, 'attribute');
            //储存商品数据
            $products_freight[$value['mch_id']][] = array('pid' => $pid, 'num' => $num,'merge_num' => $merge_num,'tongbu'=>$tongbu, 'freight_id' => $value['freight'], 'weight' => $value['weight'], 'weight_unit' => $value['weight_unit'],'supplier_id'=>$value['gongyingshang']);

            $value['img'] = ServerPath::getimgpath($value['img']); /* 拼接图片链接 */
            if($product_type == 'ZB')
            {
                $value['price'] = round($value['live_price'],2); // 产品价格
                $value['commission'] = round($value['live_price'] * $value['commission'] / 100,2); // 产品价格
            }
            else
            {
                $value['price'] = round($value['price'],2); // 产品价格
            }
            $value['yprice'] = round($value['yprice'],2); // 原价/供货价
            $price = $value['price']; // 产品价格
            $value['size'] = $size; // 产品属性
            $products_total += $num * $price; // 产品总价

            if($value['commodity_type'] == 1)
            { // 虚拟商品
                if($value['write_off_settings'] != 1)
                { // 无需核销
                    $value['write_off_num'] = 0;
                }
            }
            $products[$key] = $value;
            $commodity_type = $value['commodity_type']; // 商品类型 0.实物商品 1.虚拟商品
            $mch_id = $value['mch_id']; // 店铺ID
            $write_off_settings = $value['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
            $write_off_mch_ids = $value['write_off_mch_ids']; // 核销门店id  0全部门店,  1,2,3使用逗号分割
            $is_appointment = $value['is_appointment']; // 预约时间设置 1.无需预约下单 2.需要预约下单

            if($value['supplier_superior'] != 0)
            {
                $is_supplier_pro = false; // 供应商商品
            }
        }

        $time = date("Y-m-d H:i:s");
        $time_0 = date("Y-m-d");
        $address_status = 1;
        $mchStoreList = array();
        if($commodity_type == 1)
        { // 虚拟商品
            if($write_off_settings == 1)
            { // 线下核销
                if($is_appointment == 2)
                {
                    $address_status = 2;

                    if($shop_address_id != 0)
                    {
                        $sql_mch_store = "select id,store_id,mch_id,name,mobile,business_hours,sheng,shi,xian,address,add_date,is_default from lkt_mch_store where store_id = '$store_id' and mch_id = '$mch_id' and id = '$shop_address_id' ";
                    }
                    else
                    {
                        if($write_off_mch_ids == 0)
                        {
                            $sql_mch_store = "select id,store_id,mch_id,name,mobile,business_hours,sheng,shi,xian,address,add_date,is_default from lkt_mch_store where store_id = '$store_id' and mch_id = '$mch_id' order by is_default desc,id asc limit 1";
                        }
                        else
                        {
                            $sql_mch_store = "select id,store_id,mch_id,name,mobile,business_hours,sheng,shi,xian,address,add_date,is_default from lkt_mch_store where store_id = '$store_id' and mch_id = '$mch_id' and id in ($write_off_mch_ids) order by is_default desc,id asc limit 1";
                        }
                    }
                    
                    $r_mch_store = Db::query($sql_mch_store);
                    if($r_mch_store)
                    {
                        $mch_store_id = $r_mch_store[0]['id'];

                        $sql_mch_store1 = "select * from lkt_mch_store_write where store_id = '$store_id' and mch_id = '$mch_id' and mch_store_id = '$mch_store_id' and recycle = 0 order by id asc limit 1";
                        $r_mch_store1 = Db::query($sql_mch_store1);
                        if($r_mch_store1)
                        {
                            $r_mch_store[0]['w_id'] = $r_mch_store1[0]['id'];
                            $r_mch_store[0]['start_time'] = $r_mch_store1[0]['start_time'];
                            $r_mch_store[0]['end_time'] = $r_mch_store1[0]['end_time'];
                            $r_mch_store[0]['write_off_num'] = $r_mch_store1[0]['write_off_num'];
                            $r_mch_store[0]['off_num'] = $r_mch_store1[0]['off_num'];
                        }

                        $date_arr = array();
                        $sql_mch_store2 = "select * from lkt_mch_store_write where store_id = '$store_id' and mch_id = '$mch_id' and mch_store_id = '$mch_store_id' and recycle = 0 and end_time > '$time' ";
                        $r_mch_store2 = Db::query($sql_mch_store2);
                        if($r_mch_store2)
                        {
                            foreach($r_mch_store2 as $k_mch_store2 => $v_mch_store2)
                            {
                                $w_id2 = $v_mch_store2['id']; // 时间段ID
                                $start_time2 = $v_mch_store2['start_time']; // 开始时间
                                $end_time2 = $v_mch_store2['end_time']; // 结束时间
                                $write_off_num2 = $v_mch_store2['write_off_num']; // 可预约核销次数 0无限制
                                $off_num2 = explode(',',$v_mch_store2['off_num']); // 已预约核销次数
                                $diff_days = count($off_num2); // 相差天数

                                $sort = date('H:i',strtotime($start_time2));
                                $endTime = date('H:i',strtotime($end_time2));
                                $endTime_0 = date('H:i:s',strtotime($end_time2));
                                $time_range = $sort . '~' . $endTime;
                                $write_status = 0; // 不能预约

                                for($i=0;$i<$diff_days;$i++)
                                {
                                    $key_time = date("Y-m-d H:i:s",strtotime("+ $i day",strtotime($start_time2))); // 每天预约的开始时间
                                    $key_time_e = date("Y-m-d ".$endTime_0,strtotime("+ $i day",strtotime($start_time2))); // 每天预约的结束时间
                                    $key_time0 = date("Y-m-d",strtotime("+ $i day",strtotime($start_time2))); // 可预约日期
                                   
                                    if($time_0 <= $key_time0)
                                    { // 当前日期 <= 可预约日期  （今天或者还没到的预约日期）
                                        if($time <= $key_time_e)
                                        { // 当前时间 <= 每天预约的结束时间 （当前预约时间段还未结束，可以预约）
                                            if($write_off_num2 != 0)
                                            { // 有限制
                                                if($off_num2[$i] < $write_off_num2)
                                                { // 当天已预约数 < 每天预约总数
                                                    $write_status = 1; // 可以预约
                                                }
                                            }
                                            else
                                            { // 无限制
                                                $write_status = 1; // 可以预约
                                            }
                                            $date_arr[$key_time0][] = array('w_id'=>$w_id2,'sort'=>$sort,'endTime'=>$endTime,'time_range'=>$time_range,'write_status'=>$write_status);
                                        }
                                    }
                                }
                            }
                        }
                        
                        $r_mch_store[0]['date'] = $date_arr;

                        $mchStoreList = $r_mch_store;
                    }
                }
            }
        }

        if ($product_type == 'JP')
        {
            return array('product_id' => $product_id, 'product_class' => $product_class, 'products_freight' => $products_freight, 'products' => $products, 'products_total' => $products_total,'is_supplier_pro'=>$is_supplier_pro);
        }
        else
        {
            $products1 = [];
            $products2 = [];
            foreach ($products as $key => $value)
            {
                $products1[$value['mch_id']][] = $value;
            }

            $num_1 = 0;
            foreach ($products1 as $k => $v)
            {
                $product_total = 0;
                foreach ($v as $ke => $va)
                {
                    $product_total += $va['price'] * $va['num'];
                }
                if ($k != 0)
                {
                    // 根据商城id、用户id、审核通过，查询店铺ID
                    $r0 = MchModel::where(['store_id'=>$store_id,'id'=>$k,'review_status'=>1])->field('id,name,logo,collection_num,head_img')->select()->toArray();
                    if ($r0)
                    {
                        $products2[$num_1]['shop_id'] = $r0[0]['id']; // 店铺id
                        $products2[$num_1]['shop_name'] = $r0[0]['name']; // 店铺名称
                        $products2[$num_1]['shop_logo'] = ServerPath::getimgpath($r0[0]['logo'],$store_id); // 店铺logo
                        $products2[$num_1]['head_img']  = ServerPath::getimgpath($r0[0]['head_img'],$store_id); // 店铺头像
                        $products2[$num_1]['list'] = $v;
                    }
                    else
                    {
                        $products2[$num_1]['shop_id'] = 0; // 店铺id
                        $products2[$num_1]['shop_name'] = ''; // 店铺名称
                        $products2[$num_1]['shop_logo'] = ''; // 店铺logo
                        $products2[$num_1]['head_img']  = ''; // 店铺头像
                        $products2[$num_1]['list'] = $v;
                    }
                }
                else
                {
                    $products2[$num_1]['shop_id'] = 0; // 店铺id
                    $products2[$num_1]['shop_name'] = ''; // 店铺名称
                    $products2[$num_1]['shop_logo'] = ''; // 店铺logo
                    $products2[$num_1]['head_img']  = ''; // 店铺头像
                    $products2[$num_1]['list'] = $v;
                }
                $products2[$num_1]['product_total'] = $product_total; // 该店铺商品总价

                $num_1 = $num_1 + 1;
            }
            return array('product_id' => $product_id, 'product_class' => $product_class, 'products_freight' => $products_freight, 'products' => $products2, 'products_total' => $products_total,'is_supplier_pro'=>$is_supplier_pro,'address_status'=>$address_status,'mchStoreList'=>$mchStoreList);
        }
    }


    /**
     * [array_key_remove description]
     * <p>Copyright (c) 2018-2019</p>
     * <p>Company: www.laiketui.com</p>
     * @Author  苏涛
     * @param   [type]                   $arr [description]
     * @param   [type]                   $key [description]
     * @return  [type]          删除指定数组元素[description]
     * @version 2.0
     * @date    2019-01-08T17:44:00+0800
     */
    public static function array_key_remove($arr, $key)
    {
        if (!array_key_exists($key, $arr))
        {
            return $arr;
        }
        $keys = array_keys($arr);
        $index = array_search($key, $keys);
        if ($index !== false)
        {
            array_splice($arr, $index, 1);
        }
        return $arr;
    }

    // 获取不配送省的名称
    public static function No_distribution_Province($store_id,$products_freight)
    {
        $no_delivery_list = array();
        foreach($products_freight as $k => $v)
        {
            $mch_id = $k;
            foreach($v as $k0 => $v0)
            {
                $freight_id = $v0['freight_id'];
                // 根据商品绑定的运费ID，查询运费不配送信息
                $r0 = FreightModel::where(['store_id'=>$store_id,'id'=>$freight_id,'mch_id'=>$mch_id])->field('no_delivery')->select()->toArray();
                if($r0)
                {
                    $no_delivery = $r0[0]['no_delivery']; // 不配送

                    if($no_delivery != '')
                    {
                        $no_delivery = json_decode($no_delivery,true); // 不配送
                        foreach($no_delivery as $k1 => $v1)
                        {
                            if(!in_array($v1,$no_delivery_list))
                            {
                                $no_delivery_list[] = $v1;
                            }
                        }
                    }
                }
            }
        }

        return json_encode($no_delivery_list);
    }

    // 查询默认地址
    public static function find_address($store_id, $user_id,$no_delivery_str, $address_id = '', $product_type = 'GM')
    {
        $no_delivery_list = array();
        if($no_delivery_str != '')
        {
            $no_delivery_list = json_decode($no_delivery_str,true);
        }

        $address = array();
        $no_delivery_address_id_str = '';
        if($address_id == '')
        {
            $r2 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->field('id,sheng,city,quyu')->select()->toArray();
            if($r2)
            {
                foreach($r2 as $k2 => $v2)
                {
                    $str = $v2['sheng'] . '-' . $v2['city'] . '-' . $v2['quyu'];
                    if(in_array($str,$no_delivery_list))
                    {
                        $no_delivery_address_id_str .= $v2['id'] . ',';
                    }
                }
                $no_delivery_address_id_str = trim($no_delivery_address_id_str,','); // 不配送省的ID
            }

            // 查询用户地址
            if($product_type == 'MS')
            {
                $r3 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id,'is_default'=>1])->select()->toArray();
            }
            else 
            {
                if($no_delivery_address_id_str == '')
                {
                    $r3 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id,'is_default'=>1])->select()->toArray();
                }
                else
                {
                    $r3 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->where('id','not in',$no_delivery_address_id_str)->order('is_default', 'desc')->order('id', 'asc')->limit(1)->select()->toArray();
                }
            }
            
            if ($r3)
            {
                $address = $r3[0]; // 收货地址
            }
        }
        else
        {
            $r0 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id,'id'=>$address_id])->select()->toArray();
            if($r0)
            {
                $address = $r0[0]; // 收货地址
            }
            else
            {
                $r2 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->field('id,sheng,city,quyu')->select()->toArray();
                if($r2)
                {
                    foreach($r2 as $k2 => $v2)
                    {
                        $str = $v2['sheng'] . '-' . $v2['city'] . '-' . $v2['quyu'];
                        if(in_array($str,$no_delivery_list))
                        {
                            $no_delivery_address_id_str .= $v2['id'] . ',';
                        }
                    }
                    $no_delivery_address_id_str = trim($no_delivery_address_id_str,','); // 不配送省的ID
                }
    
                // 查询用户地址
                if($no_delivery_address_id_str == '')
                {
                    $r3 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id,'is_default'=>1])->select()->toArray();
                }
                else
                {
                    $r3 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->where('id','not in',$no_delivery_address_id_str)->order('is_default', 'desc')->order('id', 'asc')->limit(1)->select()->toArray();
                }
                if ($r3)
                {
                    $address = $r3[0]; // 收货地址
                }
            }
        }
        return $address;
    }

    //获取用户收货地址列表
    public static function find_address_list($store_id, $user_id,$no_delivery_str)
    {
        $no_delivery_list = array();
        if($no_delivery_str != '')
        {
            $no_delivery_list = json_decode($no_delivery_str,true);
        }
        $address = array();
        $no_delivery_address_id_str = '';
        $r2 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->field('id,sheng,city,quyu')->select()->toArray();
        if($r2)
        {
            foreach($r2 as $k2 => $v2)
            {
                $str = $v2['sheng'] . '-' . $v2['city'] . '-' . $v2['quyu'];
                if(in_array($str,$no_delivery_list))
                {
                    $no_delivery_address_id_str .= $v2['id'] . ',';
                }
            }
            $no_delivery_address_id_str = trim($no_delivery_address_id_str,','); // 不配送省的ID
        }

        // 查询用户地址
        if($no_delivery_address_id_str == '')
        {
            $r3 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->order('is_default', 'desc')->order('id', 'asc')->select()->toArray();
        }
        else
        {
            $r3 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->where('id','not in',$no_delivery_address_id_str)->order('is_default', 'desc')->order('id', 'asc')->limit(1)->select()->toArray();
        }
        if ($r3)
        {
            $address = $r3; // 收货地址
        }
        return $address;
    }

    public static function get_products_data0($store_id, $products, $products_total, $user_id,$vipSource = 0)
    {
        $grade_rate = 10; // 会员折扣
        $products_total = 0; // 商品总价

        if($vipSource == 1)
        {
            //判断用户身份
            $r_u = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'is_out'=>0])->field('grade')->select()->toArray();
            if($r_u && $r_u[0]['grade'] == 1)
            {
                //查询折扣
                $r_g = MemberConfigModel::where(['store_id'=>$store_id,'is_open'=>1])->field('member_discount')->select()->toArray();
                if($r_g)
                {
                    $grade_rate = $r_g[0]['member_discount'];
                }
            }
        }
        foreach ($products as $k => $v)
        {
            $products_total0 = 0; // 商品售价总价
            $products_total1 = 0; // 商品会员价总价

            foreach ($v['list'] as $k1 => $v1)
            {
                $num = $v1['num'];
                if($v1['supplier_superior'])
                {
                   $products[$k]['list'][$k1]['supplier_settlement'] =  round($v1['yprice'] * $num, 2);//供货价
                }
                else
                {
                    $products[$k]['list'][$k1]['supplier_settlement'] = 0;
                }
                $products[$k]['list'][$k1]['membership_price'] = round($v1['price'] * $grade_rate * 0.1, 2); // 会员价
                $products[$k]['list'][$k1]['amount_after_discount'] = round($v1['price'] * $grade_rate * 0.1 * $num, 2); // 会员价
                $membership_price = round($v1['price'] * $grade_rate * 0.1, 2); // 会员价

                $products_total0 += $num * $v1['price']; // 单个产品售价总价
                $products_total1 += $num * $membership_price; // 单个产品会员价总价
                if($vipSource == 1)
                {
                    $products_total += $num * $v1['price']; // 产品总价
                }
                else
                {
                    $products_total += $num * $membership_price; // 产品总价
                }
            }
            $products[$k]['grade_rate_amount'] = $products_total0 - $products_total1;
            if($vipSource == 1)
            {
                $products[$k]['product_total'] = $products_total0;
            }
            else
            {
                $products[$k]['product_total'] = $products_total1;
            }
        }
        return array('grade_rate' => $grade_rate, 'products' => $products, 'products_total' => $products_total);
    }

    // 获取运费
    public static function get_freight($freight_array, $products, $address,$store_id, $product_type,$is_self_delivery = 1)
    {
        $yunfei = 0;
        $z_num = 0; // 商品总数量
        $city = '';
        // 防止空地址报错
        if (!empty($address))
        {
            $sheng = $address['sheng'];
            $city = $address['city'];
            $quyu = $address['quyu'];
            $city = $sheng . '-' . $city . '-' . $quyu; 
        }

        $farray = [];
        $package_settings = 0;
        $order_yunfei = false; // 不包邮
        
        foreach($freight_array as $k_0 => $v_0)
        {
            $mch_id = $k_0;
            break;
        }

        $shop_id = PC_Tools::SelfOperatedStore($store_id);
        if($product_type == 'PS')
        {
            $r0 = PreSellConfigModel::where(['store_id'=>$store_id])->field('package_settings,same_piece,same_order')->select()->toArray();
        }
        else if($product_type == 'MS')
        {
            $r0 = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->field('package_settings,same_piece,same_order')->select()->toArray();
        }
        else if($product_type == 'IN')
        {
            $r0 = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->field('package_settings,same_piece')->select()->toArray();
        }
        else
        {
            $r0 = MchConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'package_settings'=>1])->field('package_settings,same_piece,same_order')->select()->toArray();
            if($r0)
            {
                $r01 = OrderConfigModel::where('store_id',$store_id)->field('package_settings,same_piece,same_order')->select()->toArray();
            }
            else
            {
                $r0 = OrderConfigModel::where('store_id',$store_id)->field('package_settings,same_piece,same_order')->select()->toArray();
            }
        }
        if ($r0)
        {
            //# 50506 ....
            if(isset($r01[0]['package_settings']) && $r01[0]['package_settings'] == 1)
            {
                if($r0[0]['same_piece'] > $r01[0]['same_piece'])
                {
                    $r0[0]['same_piece'] = $r01[0]['same_piece'];
                }
                if($r0[0]['same_order'] > $r01[0]['same_order'])
                {
                    $r0[0]['same_order'] = $r01[0]['same_order'];
                }
            }
            $package_settings = $r0[0]['package_settings']; // 包邮设置 0.未开启 1.开启
            $same_piece = $r0[0]['same_piece']; // 同件
            if(isset($r0[0]['same_order']))
            {
                $same_order = $r0[0]['same_order']; // 同单
            }
        }

        if ($package_settings == 1)
        { // 开启包邮设置
            foreach ($freight_array as $key_f => $value_f)
            {
                foreach ($value_f as $key_f1 => $value_f1)
                {
                    $z_num += $value_f1['num'];
                    if ($same_piece <= $value_f1['merge_num'])
                    { // 该商品购买的数量 >= 同件数量
                        $freight_array[$key_f][$key_f1]['order_yunfei'] = true; // 该商品免邮
                    }
                    else
                    {
                        $freight_array[$key_f][$key_f1]['order_yunfei'] = false; // 该商品不免邮
                    }
                }
                if($product_type == 'GM' || $product_type == '')
                {
                    if ($z_num >= $same_order)
                    { // 购买商品的数量 >= 同单数量
                        $order_yunfei = true; // 免运费
                    }
                }
            }
        }
        else
        {   // 未开启包邮设置
            foreach ($freight_array as $key_f => $value_f)
            {
                foreach ($value_f as $key_f1 => $value_f1)
                {
                    $freight_array[$key_f][$key_f1]['order_yunfei'] = false; // 该商品不免邮
                }
            }
        }

        if($is_self_delivery == 1)
        {
            if ($order_yunfei)
            { // 满足同单免邮
                foreach ($products as $kf => $vf)
                {
                    $products[$kf]['freight'] = 0;
                }
            }
            else
            {   
                foreach ($freight_array as $key_f => $value_f)
                {
                    $num_num = 0;
                    foreach ($value_f as $key_f1 => $value_f1)
                    {
                        $DefaultState = true; // 需要获取默认运费
                        $freight_id = $value_f1['freight_id'];
                        // 根据运费ID，查询运费信息
                        $r_1 = FreightModel::where(['store_id'=>$store_id,'id'=>$freight_id])->select()->toArray();
                        if ($r_1)
                        {
                            $type = $r_1[0]['type']; // 类型 0:件 1:重量
                            $rule_1 = $r_1[0]['freight']; // 运费规则
                            $default_freight = json_decode($r_1[0]['default_freight'],true); // 默认运费规则

                            $rule_2 = array();
                            if($rule_1 != '')
                            {
                                $rule_2 = json_decode($rule_1,true); // 运费规则
                            }

                            if ($value_f1['order_yunfei'])
                            { // 该商品免邮
                                $farray['freight_id'][$key_f][$key_f1] = 0;
                                $yunfei = $yunfei;
                            }
                            else
                            {
                                foreach ($rule_2 as $key => $value)
                                { // 循环指定运费规则
                                    $name = explode(',',$value['name']); // 指定运费地区
                                    if(in_array($city,$name))
                                    { // 收货地址，存在指定运费地区
                                        $DefaultState = false; // 不用获取默认运费
                                        $yfmb = self::SpecifiedFreight($value_f1,$type,$value);
                                        
                                        $yunfei += $yfmb;
                                        $farray['freight_id'][$key_f][$key_f1] = $yfmb;
                                        break;
                                    }
                                }

                                if($DefaultState)
                                {
                                    $yfmb = self::DefaultFreight($value_f1,$type,$default_freight);
                                    $yunfei += $yfmb;
                                    $farray['freight_id'][$key_f][$key_f1] = $yfmb;
                                }

                                $num_num++;
                            }
                        }
                    }
                }
            }
        }
        else
        {
            foreach ($freight_array as $key_f => $value_f)
            {
                foreach ($value_f as $key_f1 => $value_f1)
                {
                    $farray['freight_id'][$key_f][$key_f1] = 0;
                }
            }
        }

        $farray['yunfei'] = $yunfei;
        $freight_ids = isset($farray['freight_id']) ? $farray['freight_id'] : [];
        $farray['freight_ids'] = $freight_ids;
        if ($product_type == 'JP')
        {
            foreach ($products as $kf => $vf)
            {
                $products[$kf]['freight_price'] = floatval($yunfei);
                $products[$kf]['supplier_freight_price'] = floatval($yunfei);
            }
        }
        else
        {
            foreach ($products as $kf => $vf)
            {
                $freight_price = 0;
                $products_num = 0;
                $shop_id = $vf['shop_id'];
                foreach ($vf['list'] as $kf1 => $vf1)
                {
                    $products_num = $products_num + $vf1['num'];
                    if($freight_ids && isset($freight_ids[$shop_id][$kf1]))
                    {
                        $freight_price += $freight_ids[$shop_id][$kf1];
                        $products[$kf]['list'][$kf1]['freight_price'] = floatval($freight_ids[$shop_id][$kf1]);
                        $products[$kf]['list'][$kf1]['supplier_freight_price'] = floatval($freight_ids[$shop_id][$kf1]);
                    }
                    else
                    {
                        $products[$kf]['list'][$kf1]['freight_price'] = 0;
                        $products[$kf]['list'][$kf1]['supplier_freight_price'] = 0;
                    }
                }
                $products[$kf]['freight_price'] = floatval($freight_price);
                $products[$kf]['products_num'] = $products_num;
                $products[$kf]['product_total'] += floatval($freight_price);
            }
        }

        $farray['products'] = $products;
        return $farray;
    }

    // 获取指定运费规则运费
    public static function SpecifiedFreight($p_list,$type,$f_list)
    {
        $merge_num = $p_list['merge_num']; // 购买商品数量（同件商品合并后的数量，同一商品不同规格）
        $weight = $p_list['weight']; //  购买商品重量
        $weight_unit = $p_list['weight_unit']; //  重量单位
        if($weight_unit == 'g')
        {
            $weight = $weight / 1000;
        }
        $First_article = $f_list['one']; // 首件\首重
        $First_article_freight = $f_list['freight']; // 首件\首重运费
        $Continuation = $f_list['two']; // 续件\续重
        $Continuation_freight = $f_list['Tfreight']; // 续件\续重运费
        if($First_article_freight == '')
        {
            $First_article_freight = 0;
        }
        if($Continuation_freight == '')
        {
            $Continuation_freight = 0;
        }

        $yfmb = 0;
        if($type == 1)
        { // 重量
            $z_weight = round(round($weight,2) * (int)$merge_num,2);
            if($p_list["tongbu"] == 1)
            {
                if($z_weight <= $First_article)
                { // 商品重量 <= 首重重量
                    $yfmb = $First_article_freight;
                }
                else
                {
                    $yfmb = $First_article_freight;
                    $weight_0 = $z_weight - $First_article; // 商品重量 - 首件重量 = 剩余商品重量
                    if($weight_0 <= $Continuation)
                    { // 剩余商品重量 <= 续重运费
                        $yfmb += $Continuation_freight;
                    }
                    else
                    {
                        $num_1 = ceil($weight_0 / $Continuation); // 多余重量 / 续件重量（向上取整）
                        $yfmb += $Continuation_freight * $num_1;
                    }
                }
            }
        }
        else
        {
            if($p_list["tongbu"] == 1)
            {
                if($merge_num <= $First_article)
                { // 商品购买数量 <= 首件数量
                    $yfmb = $First_article_freight;
                }
                else
                {
                    $yfmb = $First_article_freight;
                    $num_0 = (int)$merge_num - (int)$First_article; // 商品购买数量 - 首件数量 = 剩余商品数量

                    if($num_0 <= $Continuation)
                    { // 剩余商品数量 <= 续件数量
                        $yfmb += $Continuation_freight;
                    }
                    else
                    {
                        $num_1 = ceil((int)$num_0 / (int)$Continuation); // 多余数量 / 续件数量（向上取整）
                        $yfmb += $Continuation_freight * $num_1;
                    }
                }
            }
        }

        return $yfmb;
    }

    // 获取默认运费规则运费
    public static function DefaultFreight($p_list,$type,$default_freight)
    {
        $merge_num = $p_list['merge_num']; // 购买商品数量（同件商品合并后的数量，同一商品不同规格）
        $weight = $p_list['weight']; //  购买商品重量
        $weight_unit = $p_list['weight_unit']; //  重量单位
        if($weight_unit == 'g')
        {
            $weight = $weight / 1000;
        }
        $First_article = $default_freight['num1']; // 首件\首重
        $First_article_freight = $default_freight['num2']; // 首件\首重运费
        $Continuation = $default_freight['num3']; // 续件\续重
        $Continuation_freight = $default_freight['num4']; // 续件\续重运费

        $yfmb = 0;
        if($type == 1)
        { // 重量
            $z_weight = round(round($weight,2) * (int)$merge_num,2);
            if($p_list["tongbu"] == 1)
            {
                if($z_weight <= $First_article)
                { // 商品重量 <= 首重重量
                    $yfmb = $First_article_freight;
                }
                else
                {
                    $yfmb = $First_article_freight;
                    $weight_0 = $z_weight - $First_article; // 商品重量 - 首件重量 = 剩余商品重量
                    if($weight_0 <= $Continuation)
                    { // 剩余商品重量 <= 续重运费
                        $yfmb += $Continuation_freight;
                    }
                    else
                    {
                        $num_1 = ceil($weight_0 / $Continuation); // 多余重量 / 续件重量（向上取整）
                        $yfmb += $Continuation_freight * $num_1;
                    }
                }
            }
        }
        else
        {
            if($p_list["tongbu"] == 1)
            {
                if($merge_num <= $First_article)
                { // 商品购买数量 <= 首件数量
                    $yfmb = $First_article_freight;
                }
                else
                {
                    $yfmb = $First_article_freight;
                    $num_0 = (int)$merge_num - (int)$First_article; // 商品购买数量 - 首件数量 = 剩余商品数量
                    if($num_0 <= $Continuation)
                    { // 剩余商品数量 <= 续件数量
                        $yfmb += $Continuation_freight;
                    }
                    else
                    {
                        $num_1 = ceil((int)$num_0 / (int)$Continuation); // 多余数量 / 续件数量（向上取整）
                        $yfmb += $Continuation_freight * $num_1;
                    }
                }
            }
        }
        return $yfmb;
    }

    // 生成session
    public function generate_session($cache, $type)
    {
        $time = date('Y-m-d H:i:s');
        $sql = "delete from lkt_session_id where date_add(add_date, interval 5 minute) < now() ";
        Db::execute($sql);
        if ($type == 1)
        {
            $variable = 'p_id';
        }
        else if ($type == 2)
        {
            $variable = 'order_details_id';
        }
        else if ($type == 3)
        {
            $variable = 'comment_id';
        }
        $image_arr = array();
        $image_arr1 = array();
        $rew = 0; // 用来判断，是否有短信数据。0代表没有，1代表有

        $r1 = SessionIdModel::where('type' , $type)
                            ->select()
                            ->toArray();
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $content1 = json_decode($v['content'],true);
                if (!empty($content1['user_id']))
                {
                    if ($cache['user_id'] == $content1['user_id'] && $cache[$variable] == $content1[$variable])
                    { // 存在
                        $id = $v['id'];
                        $image_arr1 = $content1['image_arr'];
                        $rew = 1;
                        break;
                    }
                }
            }
        }

        if ($rew == 0)
        {
            // 不存在
            $content1 = json_encode($cache);

            $data_session_id = ['content' => $content1, 'add_date' => $time, 'type' => $type];
            $r = Db::name('session_id')->save($data_session_id);
            if ($r > 0)
            {
                $res = 1;
            }
            else
            {
                $res = 0;
            }
        }
        else
        {
            if(isset($image_arr1[0]))
            {
                foreach ($image_arr1 as $k => $v)
                {
                    $image_arr[] = $v;
                }
            }
            else
            {
                 $image_arr[] = $image_arr1;
            }
        
            $image_arr[] = $cache['image_arr'];
           
            $content2 = array('user_id' => $cache['user_id'], $variable => $cache[$variable], 'image_arr' => $image_arr);
            $content = json_encode($content2); // 数组转json字符串

            $sql = SessionIdModel::where(['id'=>$id,'type'=>$type])->find();
            $sql->content = $content;
            $r = $sql->save();
            if ($r != -1)
            {
                $res = 1;
            }
            else
            {
                $res = 0;
            }
        }
        return $res;
    }

    // 获得session
    public function obtain_session($user_id, $type, $p_id)
    {
        $time = date('Y-m-d H:i:s');

        $sql = "delete from lkt_session_id where date_add(add_date, interval 5 minute) < now() ";
        Db::execute($sql);
        if ($type == 1)
        {
            $variable = 'p_id';
        }
        else if ($type == 2)
        {
            $variable = 'order_details_id';
        }
        else if ($type == 3)
        {
            $variable = 'comment_id';
        }

        $cache = '';
        $r1 = SessionIdModel::where('type' , $type)
                            ->select()
                            ->toArray();
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $content1 = json_decode($v['content']);
                if (!empty($content1->user_id))
                {
                    if ($user_id == $content1->user_id && $p_id == $content1->$variable)
                    {
                        // 存在
                        $cache = json_encode($content1->image_arr); // 数组转json字符串
                        break;
                    }
                }
            }
        }
        return $cache;
    }

    // 删除session
    public function del_session($user_id, $type, $p_id)
    {
        $time = date('Y-m-d H:i:s');

        if ($type == 1)
        {
            $variable = 'p_id';
        }
        else if ($type == 2)
        {
            $variable = 'order_details_id';
        }
        else if ($type == 3)
        {
            $variable = 'comment_id';
        }

        $sql = "delete from lkt_session_id where date_add(add_date, interval 5 minute) < now() ";
        Db::execute($sql);

        $r1 = SessionIdModel::where('type' , $type)
                            ->select()
                            ->toArray();
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $content1 = json_decode($v['content']);
                if (!empty($content1->user_id))
                {
                    if ($user_id == $content1->user_id && $p_id == $content1->$variable)
                    { // 存在
                        Db::table('lkt_session_id')->where(['id' => $v['id'],'type' => $type])->delete();
                    }
                }
            }
        }
        return;
    }

    // 二维数组去重
    public static function assoc_unique($arr, $key)
    {
        $tmp_arr = array();
        foreach ($arr as $k => $v)
        {
            if (in_array($v[$key], $tmp_arr))
            {
                //搜索$v[$key]是否在$tmp_arr数组中存在，若存在返回true
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

    // 获取经纬度
    public static function get_Longitude_and_latitude($store_id, $address_xx)
    {
        // $lktlog = new LaiKeLogUtils();
        // // 腾讯地图
        // $r0_0 = ConfigModel::where('store_id',$store_id)
        //                     ->field('tencent_key')
        //                     ->select()
        //                     ->toArray();
        // if ($r0_0)
        // {
        //     $key = $r0_0[0]['tencent_key'];
        // }
        // else
        // {
        //     $message = Lang('index.0');
        //     echo json_encode(array('code' => 109, 'message' => $message));
        //     exit;
        // }
        // $url = "https://apis.map.qq.com/ws/geocoder/v1/?address=$address_xx&key=$key";
        // // 初始url会话
        // $ch = new Third();
        // $data = $ch->https_get($url, 1);
        // $lktlog->log("app/laiketui_address.log",__METHOD__ . '->' . __LINE__ . '返回数据:' . $data);
        $longitude = '';
        $latitude = '';
        // $map = json_decode($data);
        // if($map)
        // {
        //     if ($map->status == 0)
        //     {
        //         $location = $map->result->location;
        //         $longitude = $location->lng; // 经度
        //         $latitude = $location->lat; // 纬度
        //     }
        // }
        
        $data = array('longitude' => $longitude, 'latitude' => $latitude);
        return $data;
    }

    /**
     * [array_key_remove description]
     * <p>Copyright (c) 2019-2020</p>
     * <p>Company: www.laiketui.com</p>
     * @title   http网络请求
     * @Author  凌烨棣
     * @param string $url : 网络地址
     * @param json $data ： 发送的json格式数据
     * @param type： 1-普通post请求，以文件流返回而不是直接输出  2-处理图片post请求,且为直接输出
     */
    public function https_post($url, $data, $type = 1)
    {
        $curl = curl_init();
        curl_setopt($curl, CURLOPT_URL, $url);
        if (!empty($data))
        {
            curl_setopt($curl, CURLOPT_POST, 1);
            curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
        }
        if ($type == 1)
        {
            curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
        }
        else
        {
            curl_setopt($curl, CURLOPT_RETURNTRANSFER, 0);
        }
        $output = curl_exec($curl);
        curl_close($curl);
        return $output;
    }

    /**
     * [array_key_remove description]
     * <p>Copyright (c) 2019-2020</p>
     * <p>Company: www.laiketui.com</p>
     * @title   http网络请求
     * @Author  凌烨棣
     * @param string $url : 网络地址
     * @param type： 1-普通get请求，以文件流返回而不是直接输出  2-处理图片get请求,且为直接输出
     */
    public function https_get($url, $type = 1)
    {
        $curl = curl_init();
        curl_setopt($curl, CURLOPT_URL, $url);
        if ($type == 1)
        {
            curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
        }
        else
        {
            curl_setopt($curl, CURLOPT_RETURNTRANSFER, false);
        }
        curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, false);
        curl_setopt($curl, CURLOPT_HEADER, false);
        curl_setopt($curl, CURLOPT_TIMEOUT, 60);

        if (curl_errno($curl))
        {
            return 'Errno' . curl_error($curl);
        }
        else
        {
            $result = curl_exec($curl);
        }
        curl_close($curl);
        return $result;
    }

    // 商品查询
    public function query_product($store_id, $shop_id)
    {
        $r_mch = AdminModel::where(['store_id'=>$store_id,'recycle'=>0,'type'=>1])
                            ->field('shop_id')
                            ->select()
                            ->toArray();
        $mch_id = $r_mch[0]['shop_id'];
        $arr = array();
        // 查询自选的商品
        $r0 = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0,'is_zixuan'=>0,'mch_id'=>$shop_id])
                            ->field('id,product_title')
                            ->select()
                            ->toArray();
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $id = $v['id']; // 自选商品id
                $product_title = $v['product_title']; // 自选商品名称
                // 查询平台商品
                $r1 = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0,'is_zixuan'=>1,'active'=>1,'mch_id'=>$mch_id])
                                        ->field('id,product_title,commodity_str')
                                        ->select()
                                        ->toArray();
                if ($r1)
                {
                    foreach ($r1 as $ke => $va)
                    {
                        if ($va['commodity_str'] != '')
                        {
                            $commodity_str = unserialize($va['commodity_str']); // 平台商品所绑定的自选ID数组
                            
                            if (in_array($id, $commodity_str))
                            { // 自选商品ID，存在数组中
                                if(!in_array($va['id'],$arr))
                                {
                                    $arr[] = $va['id'];
                                }
                            }
                        }
                        
                        if($product_title == $va['product_title'] )
                        { // 自选商品名称 与 平台商品名称相同
                            if(!in_array($va['id'],$arr))
                            {
                                $arr[] = $va['id'];
                            }
                        }
                    }
                }
            }
        }

        $rew = implode(',', $arr);
        if ($rew == '')
        {
            $res = " ";
        }
        else
        {
            $res = " and a.id not in ($rew) ";
        }

        return $res;
    }

    // 分类递归找上级
    public function str_option($cid, $res = '')
    {
        $store_id = $this->store_id;

        $r2 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$cid,'examine'=>1])->field('cid,sid')->select()->toArray();
        if ($r2)
        {
            $sid = $r2[0]['sid']; // 上级ID
            $res = $r2[0]['cid'] . '-' . $res;
            if ($sid == 0)
            {
                $res = '-' . $res;
            }
            else
            {
                $res = $this->str_option($r2[0]['sid'], $res);
            }
        }
        return $res;
    }

    // 获取属性名
    public static function attribute($store_id, $values = array(),$lang_code = 'zh_CN')
    {
        $arr = array();
        $arr0 = array();
        if(count($values) > 0)
        {
            foreach ($values as $k => $v)
            {
                $r0_0 = SkuModel::where(['recycle'=>0,'name'=>$v,'type'=>1,'is_examine'=>1,'lang_code'=>$lang_code])->field('id,name')->select()->toArray();
                if($r0_0)
                {
                    $arr0[] = $r0_0[0]['id'];
                    $arr[] = array('id' => $r0_0[0]['id'], 'text' => $r0_0[0]['name'], 'status' => true);
                }
            }
        }

        $r0 = SkuModel::where(['recycle'=>0,'status'=>1,'type'=>1,'is_examine'=>1,'lang_code'=>$lang_code])->field('id,name')->select()->toArray();
        if ($r0)
        {
            foreach ($r0 as $k => $v)
            {
                if (!in_array($v['id'], $arr0))
                {
                    $arr[] = array('id' => $v['id'], 'text' => $v['name'], 'status' => false);
                }
            }
        }

        return $arr;
    }

    // 获取属性值
    public static function attribute_name( $store_id, $attribute_name, $values = array())
    {
        $arr = array();
        $arr0 = array();

        $r0 = SkuModel::where(['recycle'=>0,'name'=>$attribute_name,'type'=>1,'is_examine'=>1])->field('id,name')->select()->toArray();
        if ($r0)
        {
            $id = $r0[0]['id'];

            if (count($values) > 0)
            {
                foreach ($values as $k => $v)
                {
                    $r0_0 = SkuModel::where(['recycle'=>0,'name'=>$v,'type'=>2,'sid'=>$id,'is_examine'=>1])->field('id,name')->select()->toArray();
                    if($r0_0)
                    {
                        $arr0[] = $r0_0[0]['id'];
                        $arr[] = array('id' => $r0_0[0]['id'], 'value' => $r0_0[0]['name'], 'status' => true);
                    }
                }
            }

            $r1 = SkuModel::where(['recycle'=>0,'type'=>2,'sid'=>$id,'is_examine'=>1])->field('id,name')->select()->toArray();
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    if (!in_array($v['id'], $arr0))
                    {
                        $arr[] = array('id' => $v['id'], 'value' => $v['name'], 'status' => false);
                    }
                }
            }
        }
        return $arr;
    }

    // 获取属性id
    public static function attribute_id( $store_id, $type, $name, $sid)
    {
        $id = 0;
        $r0 = SkuModel::where(['recycle'=>0,'type'=>$type,'name'=>$name,'sid'=>$sid])->field('id')->select()->toArray();
        if ($r0)
        {
            $id = $r0[0]['id'];
        }
        return $id;
    }

    // 添加属性
    public function add_attribute( $store_id, $sid, $name, $type, $admin_name, $z_num,$type0='平台')
    {
        $time = date("Y-m-d H:i:s");
        if ($type == 1)
        {
            $code = substr(strtoupper(Tools::encode($name)), 0);
            $rew = 'LKT_' . $code . '_' . 0;
            $rew = Tools::attribute_code($rew);
        }
        else
        {
            $r2 = SkuModel::where(['recycle'=>0,'id'=>$sid])->field('code')->select()->toArray();
            $code = $r2[0]['code'];
            if ($z_num == 1)
            {
                $r1 = SkuModel::where(['recycle'=>0,'id'=>$sid,'status'=>1])->limit(1)->order('add_date','desc')->field('code')->select()->toArray();
                if ($r1)
                {
                    $code1 = $r1[0]['code'];
                    $num = trim(strrchr($code1, '_'), '_') + 1;
                }
                else
                {
                    $num = 1;
                }
            }
            else
            {
                $r1 = SkuModel::where(['recycle'=>0,'sid'=>$sid,'status'=>1])->limit(1)->order('add_date','desc')->field('code')->select()->toArray();
                if ($r1)
                {
                    $code1 = $r1[0]['code'];
                    $num = trim(strrchr($code1, '_'), '_') + 1;
                }
                else
                {
                    $num = $z_num;
                }
            }

            $numlength = strlen((string)$num);
            if ($numlength > 3)
            {
                $res = $num;
            }
            else
            {
                $res = sprintf("%03d", $num);
            }
            $rew = $code . '_' . $res;
        }
        if($type0 == '平台')
        {
            $data0 = array('store_id'=>0,'sid'=>$sid,'code'=>$rew,'name'=>$name,'status'=>1,'type'=>$type,'admin_name'=>$admin_name,'add_date'=>$time,'is_examine'=>1);
        }
        else
        {
            $data0 = array('store_id'=>0,'sid'=>$sid,'code'=>$rew,'name'=>$name,'status'=>0,'type'=>$type,'admin_name'=>$admin_name,'add_date'=>$time,'is_examine'=>0);
        }
        $r0 = Db::name('sku')->insertGetId($data0);

        return $r0;
    }

    // 验证数据编码是否重复
    public function attribute_code( $code)
    {
        $r0 = SkuModel::where(['code'=>$code])
                    ->field('id')
                    ->select()
                    ->toArray();
        if ($r0)
        {
            $code0 = explode('_',$code);
            $code1 = (int)$code0[2] + 1;
            $code2 = $code0[0] . '_' . $code0[1] . '_' . $code1;
            $code3 = $this->attribute_code($code2);
            return $code3;
        }
        else
        {
            return $code;
        }
    }

    public static function del_banner($store_id, $id, $type)
    {
        $r0 = BannerModel::where(['store_id'=>$store_id])->field('id,url')->select()->toArray();
        if ($r0)
        {
            foreach ($r0 as $key => $val)
            {
                $banner_id = $val['id'];
                if (strpos($val['url'], $type) !== false)
                {
                    $parameter = trim(strrchr($val['url'], '='), '=');
                    if ($id == $parameter)
                    {
                        $r2 = Db::name('banner')->where(['store_id' => $store_id,'id' => $banner_id])->update(['open_type' => '','url' => '']);
                    }
                }
            }
        }

        $r1 = CouponActivityModel::where(['store_id'=>$store_id,'skip_type'=>2,'recycle'=>0])->field('url')->select()->toArray();
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $res = trim(strrchr($v['url'], '?'), '?');
                $res1 = explode('&', $res);
                foreach ($res1 as $ke => $va)
                {
                    $rew = explode('=', $va);
                    if ($rew[0] == $type)
                    {
                        if ($id == $rew[1])
                        {
                            $r3 = Db::name('coupon_activity')->where(['store_id'=>$store_id,'id'=>$banner_id])->update(['skip_type'=>'1','url'=>'/pages/tabBar/home']);
                        }
                    }
                }
            }
        }
        return;
    }

    // 商品状态
    public function commodity_status($store_id, $id, $status)
    {
        $info = '';
        $sql0 = array('store_id'=>$store_id,'recycle'=>0,'mch_status'=>2,'id'=>$id);
        $sql_field0 = "commodity_type,product_title,product_class,status,brand_id,mch_id,initial,freight,active";
        $r0 = ProductListModel::where($sql0)->field($sql_field0)->select()->toArray();
        if ($r0)
        {
            if ($r0[0]['status'] == 2)
            { // 当为上架状态
                $sql1 = "select a.* from lkt_product_list as a left join lkt_order_details as b on a.id = b.p_id where a.store_id = '$store_id' and a.recycle = 0 and a.mch_status = 2 and r_status in (0,1,2) and a.id = '$id'";
                $r1 = Db::query($sql1);
                if ($r1)
                {
                    $info = '';
                    return $info;
                }
            }
            else
            {
                // 当为下架或待上架状态
                if (strlen($r0[0]['product_class']) == '')
                {
                    $info = '请先去完善商品信息！';
                    return $info;
                }
                if (strlen($r0[0]['brand_id']) == 0)
                {
                    $info = '请先去完善商品信息！';
                    return $info;
                }
                if ($r0[0]['initial'])
                {
                    $initial = unserialize($r0[0]['initial']);
                    foreach ($initial as $k => $v)
                    {
                        if ($k == 'cbj' && $v == '')
                        {
                            $info = '请先去完善商品信息！';
                            return $info;
                        }
                        else if ($k == 'yj' && $v == '')
                        {
                            $info = '请先去完善商品信息！';
                            return $info;
                        }
                        else if ($k == 'sj' && $v == '')
                        {
                            $info = '请先去完善商品信息！';
                            return $info;
                        }
                        else if ($k == 'unit' && $v == '0')
                        {
                            $info = '请先去完善商品信息！';
                            return $info;
                        }
                        else if ($k == 'kucun' && $v == '')
                        {
                            $info = '请先去完善商品信息！';
                            return $info;
                        }
                    }

                    $r_attribute = ConfigureModel::where(['pid'=>$id,'recycle'=>0])->field('attribute')->select()->toArray();
                    if ($r_attribute && $r0[0]['commodity_type'] != 1)
                    {
                        if ($r_attribute[0]['attribute'] != '')
                        {
                            $arrar_t = unserialize($r_attribute[0]['attribute']);
                            if (count($arrar_t) == 0)
                            {
                                $info = '请先去完善商品信息！';
                                return $info;
                            }
                        }
                        else
                        {
                            $info = '请先去完善商品信息！';
                            return $info;
                        }
                    }
                }
                if ($r0[0]['freight'] == 0 && $r0[0]['commodity_type'] != 1)
                {
                    $info = '请先去完善商品信息！';
                    return $info;
                }
                if ($r0[0]['active'] == '' && $r0[0]['commodity_type'] != 1)
                {
                    $info = '请先去完善商品信息！';
                    return $info;
                }
            }
        }
        return $info;
    }

    // 获取某个门店距离
    public static function get_store_distance($store_id, $longitude, $latitude, $shop_id)
    {
        // $list = array();
        // $list0 = array();
        // $Longitude_and_latitude = array();

        // $from = $latitude . ',' . $longitude;

        // $r0_0 = ConfigModel::where(['store_id'=>$store_id])
        //                     ->field('tencent_key')
        //                     ->select()
        //                     ->toArray();
        // if ($r0_0)
        // {
        //     $key = $r0_0[0]['tencent_key'];
        // }
        // else
        // {
        //     $message = Lang('mch.12');
        //     echo json_encode(array('code' => 109, 'message' => $message));
        //     exit;
        // }

        // $r0 = MchModel::where(['store_id'=>$store_id,'review_status'=>1,'recovery'=>0,'id'=>$shop_id])
        //                 ->field('latitude,longitude')
        //                 ->select()
        //                 ->toArray();
        // if ($r0)
        // {
        //     $r1 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])
        //                 ->limit(1)
        //                 ->order('id','asc')
        //                 ->select()
        //                 ->toArray();
        //     if ($r1)
        //     {
        //         foreach ($r1 as $k1 => $v1)
        //         {
        //             if ($v1['latitude'] != '' && $v1['longitude'] != '')
        //             {
        //                 $Longitude_and_latitude[] = $v1['latitude'] . ',' . $v1['longitude'];
        //             }
        //         }
        //     }
        // }
        // $to = implode(';', $Longitude_and_latitude);
        // $url = "https://apis.map.qq.com/ws/distance/v1/matrix/?mode=driving&from=$from&to=$to&key=$key";

        // // 初始url会话
        // $ch = new Third();
        // $data = $ch->https_get($url, 1);
        // $map = json_decode($data);
        $distance = 0;
        // if ($map->status == 0)
        // {
        //     foreach ($map->result->rows[0]->elements as $k => $v)
        //     {
        //         $distance = $v->distance;
        //     }
        // }
        return $distance;
    }

    public static function get_order_price( $store_id, $id,$type = '')
    {   
        if($type != '')
        {
            $res = ReturnOrderModel::where(['store_id'=>$store_id,'id'=>$id])
                                    ->field('p_id')
                                    ->select()
                                    ->toArray();
            $id = $res[0]['p_id'];
        }
        //判断单个商品退款是否有使用优惠
        $sql_id = "select a.id,m.freight,a.trade_no,m.num,a.sNo,a.pay,a.z_price,a.user_id,a.spz_price,m.p_price,a.consumer_money,m.express_id,a.z_freight,m.after_discount from lkt_order as a LEFT JOIN lkt_order_details AS m ON a.sNo = m.r_sNo where a.store_id = '$store_id' and m.id = '$id' ";
        $order_res = Db::query($sql_id);

        $pay = $order_res[0]['pay'];
        $num = $order_res[0]['num'];
        $p_price = $order_res[0]['p_price'] * $num;
        $express_id = $order_res[0]['express_id'];
        $consumer_money = $order_res[0]['consumer_money'];
        $spz_price = $order_res[0]['spz_price'];

        //运费
        $freight = $order_res[0]['freight'];
        $z_price = $order_res[0]['z_price'];
        $after_discount = $order_res[0]['after_discount'];//商品优惠后金额即实际支付金额
        //总运费
        $z_freight = $order_res[0]['z_freight'];
        //判断是否发货
        if ($freight && $express_id)
        {
            //计算实际支付金额
            $price = number_format($after_discount, 2, ".", "");
        }
        else
        {
            //计算实际支付金额
            $price = number_format(($after_discount), 2, ".", "");// + $freight 49714
        }

        if ($price <= 0 && $pay == 'consumer_pay' && $consumer_money > 0)
        {
            $price = $consumer_money;
        }

        return $price;
    }

    // 获取该订单详情商品支付金额
    public static function get_order_pro_price( $store_id, $id,$type = '')
    {   
        if($type != '')
        {
            $res = ReturnOrderModel::where(['store_id'=>$store_id,'id'=>$id])->field('p_id')->select()->toArray();
            $id = $res[0]['p_id'];
        }
        //判断单个商品退款是否有使用优惠
        $sql_id = "select a.pay,a.consumer_money,m.after_discount,m.actual_total,m.score_deduction from lkt_order as a LEFT JOIN lkt_order_details AS m ON a.sNo = m.r_sNo where a.store_id = '$store_id' and m.id = '$id' ";
        $order_res = Db::query($sql_id);

        $pay = $order_res[0]['pay'];
        $consumer_money = $order_res[0]['consumer_money'];
        $after_discount = $order_res[0]['after_discount'];//商品优惠后金额即实际支付金额
        $price = number_format($after_discount, 2, ".", "");

        if ($price <= 0 && $pay == 'consumer_pay' && $consumer_money > 0)
        {
            $price = $consumer_money;
        }

        if($order_res[0]['actual_total'] != '0.00' && $order_res[0]['score_deduction'] != 0)
        { // 使用了积分抵扣
            $price = number_format(($after_discount - $order_res[0]['actual_total']),2 ,".", "");
        }

        return $price;
    }

    public static function get_pay_config( $type)
    {
        $sql2 = "select config_data from lkt_payment as p left join lkt_payment_config as c on c.pid = p.id where c.status = 1  and p.status = 0 and p.class_name = '$type' ";
        $res2 = Db::query($sql2);
        if ($res2)
        {
            return json_decode($res2[0]['config_data']);
        }
        else
        {
            return false;
        }
    }

    public static function get_openid($store_id, $user_id)
    {
        //查询openid
        $res_openid = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])
                                ->field('wx_id')
                                ->select()
                                ->toArray();
        return $res_openid ? $res_openid[0]['wx_id'] : false;
    }

    public static function is_serialized($data)
    {
        $data = trim($data);
        if ('N;' == $data)
        {
            return true;
        }

        if (!preg_match('/^([adObis]):/', $data, $badions))
        {
            return false;
        }
        switch ($badions[1])
        {
            case 'a' :
            case 'O' :
            case 's' :
                if (preg_match("/^{$badions[1]}:[0-9]+:.*[;}]\$/s", $data))
                {
                    return true;
                }
                break;
            case 'b' :
            case 'i' :
            case 'd' :
                if (preg_match("/^{$badions[1]}:[0-9.E-]+;\$/", $data))
                {
                    return true;
                }
                break;
        }
        return false;
    }

    // 生成订单号
    public static function order_number($type)
    {
        if ($type == '')
        {
            $type = 'GM';
        }
        else if($type == 'JP')
        {
            $type = 'JP';
        }
        else if ($type == 'PS')
        {
            $type = 'PS';
        }
        return $type . date("ymdhis") . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9);
    }

    // 获取用户会话密钥
    public static function code_open($appid, $appsecret, $code)
    {
        $url = 'https://api.weixin.qq.com/sns/jscode2session?appid=' . $appid . '&secret=' . $appsecret . '&js_code=' . $code . '&grant_type=authorization_code';
        $res = self::httpsRequest($url);
        $user = (array)json_decode($res);
        return $user;
    }

    public static function httpsRequest($url, $data = null)
    {
        // 1.初始化会话
        $ch = curl_init();
        // 2.设置参数: url + header + 选项
        // 设置请求的url
        curl_setopt($ch, CURLOPT_URL, $url);
        // 保证返回成功的结果是服务器的结果
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        // curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
        //这个是重点。
        if (!empty($data))
        {
            // 发送post请求
            curl_setopt($ch, CURLOPT_POST, 1);
            // 设置发送post请求参数数据
            curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
        }
        // 3.执行会话; $result是微信服务器返回的JSON字符串
        $result = curl_exec($ch);
        // 4.关闭会话
        curl_close($ch);
        return $result;
    }

    // 头部导航数据字典-
    public static function header_data_dictionary($name, $values)
    {
        $select = '';

        // 根据参数，查询数据字典名称
        $r0 = DataDictionaryNameModel::where(['name'=>$name,'status'=>1,'recycle'=>0])->field('id')->select()->toArray();
        if ($r0)
        {
            $id = $r0[0]['id'];
            $r1 = DataDictionaryListModel::where(['status'=>1,'recycle'=>0,'sid'=>$id])->field('value,text')->select()->toArray();
            if ($r1)
            {

                foreach ($r1 as $k => $v)
                {
                    if ($values == $v['value'])
                    {
                        $select = $v['text'];
                    }
                }
            }
        }
        return $select;
    }

    // 数据字典
    public static function data_dictionary($name, $values)
    {
        $select = '';

        // 根据参数，查询数据字典名称
        $r0 = DataDictionaryNameModel::where(['name'=>$name,'status'=>1,'recycle'=>0])->field('id')->select()->toArray();
        if ($r0)
        {
            $id = $r0[0]['id'];
            $r1 = DataDictionaryListModel::where(['status'=>1,'recycle'=>0,'sid'=>$id])->field('value,text')->select()->toArray();
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    if ($values == $v['value'])
                    {
                        $select .= "<option selected value='".$v['value']."'>$v->text</option>";
                    }
                    else
                    {
                        $select .= "<option value='".$v['value']."'>".$v['text']."</option>";
                    }
                }
            }
        }
        return $select;
    }

    // 数据字典
    public static function data_dictionary1($name, $values)
    {
        $arr = array();
        $arr1 = array();
        $arr2 = array();

        // 根据参数，查询数据字典名称
        $r0 = DataDictionaryNameModel::where(['name'=>$name,'status'=>1,'recycle'=>0])->select()->toArray();
        if ($r0)
        {
            $arr1 = $r0[0];
            $id = $r0[0]['id'];
            $r1 = DataDictionaryListModel::where(['status'=>1,'recycle'=>0,'sid'=>$id])->select()->toArray();
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    if ($values == $v['value'])
                    {
                        $v['selected'] = true;
                    }
                    else
                    {
                        $v['selected'] = false;
                    }
                    $arr2[] = $v;
                }
            }
        }
        $arr[] = array('name'=>$arr1,'value'=>$arr2);
        return $arr;
    }

    // 验证数据字典名称
    public static function test_data_dictionary_name($id)
    {
        $res = true;
        $r0 = DataDictionaryListModel::where(['sid'=>$id,'recycle'=>0])->field('id')->select()->toArray();
        if ($r0)
        {
            $res = false;
        }
        return $res;
    }

    // 验证数据字典
    public static function test_data_dictionary($id, $status)
    {
        $res = array('status' => true, 'msg' => '');
        $sql0 = "select a.s_name,a.text,b.name from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.id = '$id'";
        $r0 = Db::query($sql0);
        if ($r0)
        {
            if ($r0[0]['name'] == '商品分类')
            {
                $newstr = substr($r0[0]['text'], 0, strlen($r0[0]['text']) - 3); //这是去掉字符串中的最后一个汉字
                $level = Tools::Chinese_to_Number_Conversion($newstr); // 中文转数字
                if ($status || $status == '0')
                {
                    if ($status == 0)
                    {
                        // 目前不生效，改为生效
                        if ($level >= 2)
                        {
                            $level1 = $level - 2;
                            // 查询目前级别上一级是否正在使用
                            $r1 = ProductClassModel::where(['level'=>$level,'recycle'=>0,'examine'=>1])->field('cid')->select()->toArray();
                            if (empty($r1))
                            { // 没有
                                $message = Lang('该级别上级未生效，不能生效！');
                                $res = array('status' => false, 'message' => $message);
                            }
                        }
                    }
                    else
                    {
                        //  目前生效，改为不生效
                        $level1 = $level - 1;
                        // 查询目前级别是否正在使用
                        $r1 = ProductClassModel::where(['level'=>$level1,'recycle'=>0,'examine'=>1])->field('cid')->select()->toArray();
                        if ($r1)
                        {
                            $message = Lang('该级别正在使用，不能失效！');
                            $res = array('status' => false, 'message' => $message);
                        }
                    }
                }
                else
                {
                    $level1 = $level - 1;
                    // 查询目前级别是否正在使用
                    $r1 = ProductClassModel::where(['level'=>$level1,'recycle'=>0,'examine'=>1])->field('cid')->select()->toArray();
                    if ($r1)
                    {
                        $message = Lang('该级别正在使用，不能删除！');
                        $res = array('status' => false, 'message' => $message);
                    }
                }
            }
        }
        return $res;
    }

    // 中文转数字
    public static function Chinese_to_Number_Conversion($string)
    {
        if (is_numeric($string))
        {
            return $string;
        }
        $string = str_replace('仟', '千', $string);
        $string = str_replace('佰', '百', $string);
        $string = str_replace('拾', '十', $string);
        $num = 0;
        $wan = explode('万', $string);
        if (count($wan) > 1)
        {
            $num += cn2num($wan[0]) * 10000;
            $string = $wan[1];
        }
        $qian = explode('千', $string);
        if (count($qian) > 1)
        {
            $num += cn2num($qian[0]) * 1000;
            $string = $qian[1];
        }
        $bai = explode('百', $string);
        if (count($bai) > 1)
        {
            $num += cn2num($bai[0]) * 100;
            $string = $bai[1];
        }
        $shi = explode('十', $string);
        if (count($shi) > 1)
        {
            $num += cn2num($shi[0] ? $shi[0] : '一') * 10;
            $string = $shi[1] ? $shi[1] : '零';
        }
        $ling = explode('零', $string);
        if (count($ling) > 1)
        {
            $string = $ling[1];
        }
        $d = array(
            '一' => '1', '二' => '2', '三' => '3', '四' => '4', '五' => '5', '六' => '6', '七' => '7', '八' => '8', '九' => '9',
            '壹' => '1', '贰' => '2', '叁' => '3', '肆' => '4', '伍' => '5', '陆' => '6', '柒' => '7', '捌' => '8', '玖' => '9',
            '零' => 0, '0' => 0, 'O' => 0, 'o' => 0,
            '两' => 2,
        );
        return $num + @$d[$string];
    }

    /**
     * 将中文编码成拼音
     * @param string $utf8Data utf8字符集数据
     * @param string $sRetFormat 返回格式 [head:首字母|all:全拼音]
     * @return string
     */
    public static function encode($utf8Data, $sRetFormat = 'head')
    {
        $sGBK = iconv('UTF-8', 'GBK', $utf8Data);
        $aBuf = array();
        for ($i = 0, $iLoop = strlen($sGBK); $i < $iLoop; $i++)
        {
            $iChr = ord($sGBK[$i
            ]);
            if ($iChr > 160)
            {
                $iChr = ($iChr << 8) + ord($sGBK[
                    ++$i]) - 65536;
            }

            if ('head' === $sRetFormat)
            {
                $aBuf[] = substr(self::zh2py($iChr), 0, 1);
            }
            else
            {
                $aBuf[] = self::zh2py($iChr);
            }
        }
        if ('head' === $sRetFormat)
        {
            return implode('', $aBuf);
        }
        else
        {
            return implode(' ', $aBuf);
        }
    }

    /**
     * 中文转换到拼音(每次处理一个字符)
     * @param number $iWORD 待处理字符双字节
     * @return string 拼音
     */
    private static function zh2py($iWORD)
    {
        if ($iWORD > 0 && $iWORD < 160)
        {
            return chr($iWORD);
        }
        elseif ($iWORD < -20319 || $iWORD > -10247)
        {
            return '';
        }
        else
        {
            foreach (self::$_aMaps as $py => $code)
            {
                if ($code > $iWORD)
                {
                    break;
                }

                $result = $py;
            }
            return $result;
        }
    }

    /**
     * 拼音字符转换图
     * @var array
     */
    private static $_aMaps = array(
        'a' => -20319, 'ai' => -20317, 'an' => -20304, 'ang' => -20295, 'ao' => -20292,
        'ba' => -20283, 'bai' => -20265, 'ban' => -20257, 'bang' => -20242, 'bao' => -20230, 'bei' => -20051, 'ben' => -20036, 'beng' => -20032, 'bi' => -20026, 'bian' => -20002, 'biao' => -19990, 'bie' => -19986, 'bin' => -19982, 'bing' => -19976, 'bo' => -19805, 'bu' => -19784,
        'ca' => -19775, 'cai' => -19774, 'can' => -19763, 'cang' => -19756, 'cao' => -19751, 'ce' => -19746, 'ceng' => -19741, 'cha' => -19739, 'chai' => -19728, 'chan' => -19725, 'chang' => -19715, 'chao' => -19540, 'che' => -19531, 'chen' => -19525, 'cheng' => -19515, 'chi' => -19500, 'chong' => -19484, 'chou' => -19479, 'chu' => -19467, 'chuai' => -19289, 'chuan' => -19288, 'chuang' => -19281, 'chui' => -19275, 'chun' => -19270, 'chuo' => -19263, 'ci' => -19261, 'cong' => -19249, 'cou' => -19243, 'cu' => -19242, 'cuan' => -19238, 'cui' => -19235, 'cun' => -19227, 'cuo' => -19224,
        'da' => -19218, 'dai' => -19212, 'dan' => -19038, 'dang' => -19023, 'dao' => -19018, 'de' => -19006, 'deng' => -19003, 'di' => -18996, 'dian' => -18977, 'diao' => -18961, 'die' => -18952, 'ding' => -18783, 'diu' => -18774, 'dong' => -18773, 'dou' => -18763, 'du' => -18756, 'duan' => -18741, 'dui' => -18735, 'dun' => -18731, 'duo' => -18722,
        'e' => -18710, 'en' => -18697, 'er' => -18696,
        'fa' => -18526, 'fan' => -18518, 'fang' => -18501, 'fei' => -18490, 'fen' => -18478, 'feng' => -18463, 'fo' => -18448, 'fou' => -18447, 'fu' => -18446,
        'ga' => -18239, 'gai' => -18237, 'gan' => -18231, 'gang' => -18220, 'gao' => -18211, 'ge' => -18201, 'gei' => -18184, 'gen' => -18183, 'geng' => -18181, 'gong' => -18012, 'gou' => -17997, 'gu' => -17988, 'gua' => -17970, 'guai' => -17964, 'guan' => -17961, 'guang' => -17950, 'gui' => -17947, 'gun' => -17931, 'guo' => -17928,
        'ha' => -17922, 'hai' => -17759, 'han' => -17752, 'hang' => -17733, 'hao' => -17730, 'he' => -17721, 'hei' => -17703, 'hen' => -17701, 'heng' => -17697, 'hong' => -17692, 'hou' => -17683, 'hu' => -17676, 'hua' => -17496, 'huai' => -17487, 'huan' => -17482, 'huang' => -17468, 'hui' => -17454, 'hun' => -17433, 'huo' => -17427,
        'ji' => -17417, 'jia' => -17202, 'jian' => -17185, 'jiang' => -16983, 'jiao' => -16970, 'jie' => -16942, 'jin' => -16915, 'jing' => -16733, 'jiong' => -16708, 'jiu' => -16706, 'ju' => -16689, 'juan' => -16664, 'jue' => -16657, 'jun' => -16647,
        'ka' => -16474, 'kai' => -16470, 'kan' => -16465, 'kang' => -16459, 'kao' => -16452, 'ke' => -16448, 'ken' => -16433, 'keng' => -16429, 'kong' => -16427, 'kou' => -16423, 'ku' => -16419, 'kua' => -16412, 'kuai' => -16407, 'kuan' => -16403, 'kuang' => -16401, 'kui' => -16393, 'kun' => -16220, 'kuo' => -16216,
        'la' => -16212, 'lai' => -16205, 'lan' => -16202, 'lang' => -16187, 'lao' => -16180, 'le' => -16171, 'lei' => -16169, 'leng' => -16158, 'li' => -16155, 'lia' => -15959, 'lian' => -15958, 'liang' => -15944, 'liao' => -15933, 'lie' => -15920, 'lin' => -15915, 'ling' => -15903, 'liu' => -15889, 'long' => -15878, 'lou' => -15707, 'lu' => -15701, 'lv' => -15681, 'luan' => -15667, 'lue' => -15661, 'lun' => -15659, 'luo' => -15652,
        'ma' => -15640, 'mai' => -15631, 'man' => -15625, 'mang' => -15454, 'mao' => -15448, 'me' => -15436, 'mei' => -15435, 'men' => -15419, 'meng' => -15416, 'mi' => -15408, 'mian' => -15394, 'miao' => -15385, 'mie' => -15377, 'min' => -15375, 'ming' => -15369, 'miu' => -15363, 'mo' => -15362, 'mou' => -15183, 'mu' => -15180,
        'na' => -15165, 'nai' => -15158, 'nan' => -15153, 'nang' => -15150, 'nao' => -15149, 'ne' => -15144, 'nei' => -15143, 'nen' => -15141, 'neng' => -15140, 'ni' => -15139, 'nian' => -15128, 'niang' => -15121, 'niao' => -15119, 'nie' => -15117, 'nin' => -15110, 'ning' => -15109, 'niu' => -14941, 'nong' => -14937, 'nu' => -14933, 'nv' => -14930, 'nuan' => -14929, 'nue' => -14928, 'nuo' => -14926,
        'o' => -14922, 'ou' => -14921,
        'pa' => -14914, 'pai' => -14908, 'pan' => -14902, 'pang' => -14894, 'pao' => -14889, 'pei' => -14882, 'pen' => -14873, 'peng' => -14871, 'pi' => -14857, 'pian' => -14678, 'piao' => -14674, 'pie' => -14670, 'pin' => -14668, 'ping' => -14663, 'po' => -14654, 'pu' => -14645,
        'qi' => -14630, 'qia' => -14594, 'qian' => -14429, 'qiang' => -14407, 'qiao' => -14399, 'qie' => -14384, 'qin' => -14379, 'qing' => -14368, 'qiong' => -14355, 'qiu' => -14353, 'qu' => -14345, 'quan' => -14170, 'que' => -14159, 'qun' => -14151,
        'ran' => -14149, 'rang' => -14145, 'rao' => -14140, 're' => -14137, 'ren' => -14135, 'reng' => -14125, 'ri' => -14123, 'rong' => -14122, 'rou' => -14112, 'ru' => -14109, 'ruan' => -14099, 'rui' => -14097, 'run' => -14094, 'ruo' => -14092,
        'sa' => -14090, 'sai' => -14087, 'san' => -14083, 'sang' => -13917, 'sao' => -13914, 'se' => -13910, 'sen' => -13907, 'seng' => -13906, 'sha' => -13905, 'shai' => -13896, 'shan' => -13894, 'shang' => -13878, 'shao' => -13870, 'she' => -13859, 'shen' => -13847, 'sheng' => -13831, 'shi' => -13658, 'shou' => -13611, 'shu' => -13601, 'shua' => -13406, 'shuai' => -13404, 'shuan' => -13400, 'shuang' => -13398, 'shui' => -13395, 'shun' => -13391, 'shuo' => -13387, 'si' => -13383, 'song' => -13367, 'sou' => -13359, 'su' => -13356, 'suan' => -13343, 'sui' => -13340, 'sun' => -13329, 'suo' => -13326,
        'ta' => -13318, 'tai' => -13147, 'tan' => -13138, 'tang' => -13120, 'tao' => -13107, 'te' => -13096, 'teng' => -13095, 'ti' => -13091, 'tian' => -13076, 'tiao' => -13068, 'tie' => -13063, 'ting' => -13060, 'tong' => -12888, 'tou' => -12875, 'tu' => -12871, 'tuan' => -12860, 'tui' => -12858, 'tun' => -12852, 'tuo' => -12849,
        'wa' => -12838, 'wai' => -12831, 'wan' => -12829, 'wang' => -12812, 'wei' => -12802, 'wen' => -12607, 'weng' => -12597, 'wo' => -12594, 'wu' => -12585,
        'xi' => -12556, 'xia' => -12359, 'xian' => -12346, 'xiang' => -12320, 'xiao' => -12300, 'xie' => -12120, 'xin' => -12099, 'xing' => -12089, 'xiong' => -12074, 'xiu' => -12067, 'xu' => -12058, 'xuan' => -12039, 'xue' => -11867, 'xun' => -11861,
        'ya' => -11847, 'yan' => -11831, 'yang' => -11798, 'yao' => -11781, 'ye' => -11604, 'yi' => -11589, 'yin' => -11536, 'ying' => -11358, 'yo' => -11340, 'yong' => -11339, 'you' => -11324, 'yu' => -11303, 'yuan' => -11097, 'yue' => -11077, 'yun' => -11067,
        'za' => -11055, 'zai' => -11052, 'zan' => -11045, 'zang' => -11041, 'zao' => -11038, 'ze' => -11024, 'zei' => -11020, 'zen' => -11019, 'zeng' => -11018, 'zha' => -11014, 'zhai' => -10838, 'zhan' => -10832, 'zhang' => -10815, 'zhao' => -10800, 'zhe' => -10790, 'zhen' => -10780, 'zheng' => -10764, 'zhi' => -10587, 'zhong' => -10544, 'zhou' => -10533, 'zhu' => -10519, 'zhua' => -10331, 'zhuai' => -10329, 'zhuan' => -10328, 'zhuang' => -10322, 'zhui' => -10315, 'zhun' => -10309, 'zhuo' => -10307, 'zi' => -10296, 'zong' => -10281, 'zou' => -10274, 'zu' => -10270, 'zuan' => -10262, 'zui' => -10260, 'zun' => -10256, 'zuo' => -10254,
    );

    // 获取短信类别
    public static function get_message_data_dictionary($name, $type, $values)
    {
        $list = array();

        // 根据参数，查询数据字典名称
        $r0 = DataDictionaryNameModel::where(['status'=>1,'recycle'=>0,'name'=>$name])->field('id')->select()->toArray();

        if ($r0)
        {
            $id = $r0[0]['id'];
            $r1 = DataDictionaryListModel::where(['status'=>1,'recycle'=>0,'sid'=>$id,'s_name'=>$type])->field('value,text')->select()->toArray();
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    $v['s_name'] = $type;
                    if ($values == $v['value'])
                    {
                        $v['status'] = true;
                    }
                    else
                    {
                        $v['status'] = false;
                    }
                    $list[] = $v;
                }
            }
        }
        return $list;
    }

    // 后台验证短信模板是否正确
    public function message($store_id,$SignName, $PhoneNumbers, $TemplateCode, $TemplateParam)
    {
        $r = MessageConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if ($r)
        {
            $accessKeyId = $r[0]['accessKeyId'];
            $accessKeySecret = $r[0]['accessKeySecret'];
            if(isset($TemplateParam['amount']))
            {
                $TemplateParam['amount'] = 1.0;
            }

            $res = sendSms($accessKeyId, $accessKeySecret, $SignName, $PhoneNumbers, $TemplateCode, $TemplateParam);
            if ($res->Code == 'OK')
            {
                return $res;
            }
            else
            {
                echo json_encode(array('code'=>109,'message' => '配置信息错误'));
                exit;
            }
        }
        else
        {
            echo json_encode(array('code'=>109,'message' => '失败！'));
            exit;
        }
    }

    //生成UUID
    public static function getUid()
    {
        $chars = md5(uniqid(mt_rand(), true));  
        $uuid = substr ( $chars, 0, 8 ) . '-'
                . substr ( $chars, 8, 4 ) . '-' 
                . substr ( $chars, 12, 4 ) . '-'
                . substr ( $chars, 16, 4 ) . '-'
                . substr ( $chars, 20, 12 );
        return $uuid;
    }
    
    // 处理商品
    public static function productHandle($product1, $cart_id)
    {
        $product = '';
        $shop_list = array();
        if ($product1 != '')
        {
            $product1 = htmlspecialchars_decode($product1);
            $product2 = json_decode(stripslashes(html_entity_decode($product1)));; // 字符串打散为数组
            $product3 = array();
            foreach ($product2 as $k => $v)
            {
                foreach ($v as $ke => $va)
                {
                    $product3[$ke] = $va;
                }
            }
            $product = implode(',', $product3);
            $cart_id = '';
        }
        return array($product, $shop_list, $cart_id);
    }
    
    // 获取用户基本信息
    public static function userInfo($user, $store_id, $user_id,LaiKeLogUtils $lktlog)
    {
        $r = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();
        $user_money = round($r[0]['money'],2); // 用户余额
        $user_score = $r[0]['score']; // 用户积分
        $user_password = $r[0]['password']; // 支付密码
        $login_num = $r[0]['login_num']; // 支付密码错误次数
        $verification_time = $r[0]['verification_time']; // 支付密码验证时间
        $verification_time = date('Y-m-d H:i:s', strtotime('+1 day', strtotime($verification_time)));
        $time = date('Y-m-d H:i:s', time());

        if ($login_num == 5)
        {
            if ($time < $verification_time)
            {
                $enterless = false;  //前端判断是否还可以使用余额支付
            }
            else
            {
                $sql = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->find();
                $sql->login_num = 0;
                $r = $sql->save();
                if (!$r)
                {
                    $lktlog->log("app/order.log",__METHOD__ . ":" . __LINE__ . "修改用户信息失败！user_id:" . $user_id);
                }
                $enterless = true;
            }
        }
        else
        {
            $enterless = true;
        }

        if ($user_password != '')
        {
            $password_status = 1;
        }
        else
        {
            $password_status = 0;
        }
        return array($user_money, $enterless, $password_status);
    }

    // 生成订单号
    public function Generate_order_number($pay = 'GM', $text = 'sNo')
    {
        $sNo = $pay . date("ymdhis") . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9) . rand(0, 9);
        if($pay == 'PR')
        {
            $res = MchPromiseModel::where($text,$sNo)->select()->toArray();
        }
        else if($pay == 'JB')
        {
            $res = AuctionPromiseModel::where('trade_no',$sNo)->field('id')->select()->toArray();
        }
        else if($pay == 'USE')
        {
            $res = WithdrawModel::where($text,$sNo)->field('id')->select()->toArray();
        }
        else
        {
            $res = OrderModel::where($text,$sNo)->select()->toArray();
        }
        if ($res)
        {
            $this->order_number($pay, $text);
        }
        else
        {
            return $sNo;
        }
    }

    // 获取订单售后状态
    public static function getReturnOrderStatus($store_id,$oid)
    {
        $refund = true; // 申请售后 （真：有申请售后按钮  假：没有申请售后按钮）
        $refundAmt = false; // 仅退款 只有代发货才有仅退款 （真：有仅退款选项 假：没有仅退款选项）
        $refundGoods = false; // 换货 未发货没有换货 （真：有换货选项 假：没有换货选项）
        $refundGoodsAmt = false; // 退货退款 未发货没有退货退款 （真：有退货退款选项 假：没有退货退款选项）
        $refundShowBtn = false; // 查看售后按钮（真：有查看售后按钮 假：没有查看售后按钮）
        $refundAmtBtn = false; // 申请退款按钮 如果订单完成了则显示 售后按钮 否则显示退款按钮 俩按钮只有文字上的区别;插件只显示申请售后按钮

        // 获取订单数据
        $r0 = OrderDetailsModel::where(['store_id'=>$store_id,'id'=>$oid])->field('p_id,r_sNo,r_status,exchange_num,settlement_type')->select()->toArray();
        $otype = substr($r0[0]['r_sNo'], 0, 2);
        if($otype == 'JP' || $otype == 'IN')
        { // 竞拍订单没有售后 积分兑换订单没有售后
            $refund = false;
        }
        elseif($otype == 'MS' || $otype == 'FX')
        {
            if($r0[0]['r_status'] == 0 || $r0[0]['r_status'] == 1)
            { // 待付款 或者 待发货
                $refund = false;
            }
            elseif($r0[0]['r_status'] == 5)
            {
                if($r0[0]['exchange_num'] < 2)
                {
                    $refundGoods = true;
                }
            }
            $res_k = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->where('r_type','not in','2,4,8,9,10,12')->select()->toArray();
            if($res_k)
            {
                $refund = false;
                $refundShowBtn = true;
            }
        }
        else if ($otype == 'VI')
        {
            $p_id = $r0[0]['p_id'];
            $sql_p = "select write_off_settings from lkt_product_list where id = '$p_id' ";
            $r_p = Db::query($sql_p);
            if($r_p)
            {
                $write_off_settings = $r_p[0]['write_off_settings'];
            }
            if($write_off_settings == 1)
            { // 线下核销
                if($r0[0]['r_status'] == 5)
                {
                    $refund = false;
                }
                else
                {
                    $refundAmt = true;
                    $refundAmtBtn = true;
                }
            }
            else
            { // 无需核销
                $refundAmt = true;
                $refundAmtBtn = true;
            }
        }
        else if ($otype == 'FS')
        { // 限时折扣
            if($r0[0]['r_status'] != 0)
            {
                $refundAmtBtn = true;
            }
            if($r0[0]['r_status'] == 1)
            { // 已付款
                $refundAmt = true; // 可以申请仅退款
            }
            else if($r0[0]['r_status'] == 2)
            { // 待收货
                $refundAmt = true; // 可以申请仅退款
                $refundGoodsAmt = true; // 可以申请退货退款
                if($r0[0]['exchange_num'] < 2)
                { // 换货此时小于2次
                    $refundGoods = true; // 可以申请换货
                }
            }
            if($r0[0]['r_status'] == 5)
            {
                $refund = false;
            }
            else if($r0[0]['r_status'] == 7 || $r0[0]['settlement_type'] == 1 || $r0[0]['r_status'] == 0)
            { // 订单关闭 或者 已结算 或者 未付款
                $refund = false; // 没有售后按钮
            }

            // r_type 类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5:拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后) 11:同意并且寄回商品 12:售后结束 13:人工售後完成 15:极速退款
            $res_k = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->where('r_type','not in','2,4,8,9,10,12,13,15')->select()->toArray();
            if($res_k)
            { // 售后未完成
                $refund = false;
                $refundAmt = false;
                $refundAmtBtn = false;
                $refundGoods = false;
                $refundGoodsAmt = false;
                $refundShowBtn = true;
            }

            $res_k = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->where('r_type','in','4,9,13,15')->select()->toArray();
            if($res_k)
            { // 售后已完成完成
                $refundShowBtn = true;
            }
        }
        else if ($otype == 'PT' || $otype == 'KT')
        { // 拼团
            if($r0[0]['r_status'] == 1)
            { // 已付款
                $refundAmt = true; // 可以申请仅退款
            }
            else if($r0[0]['r_status'] == 2)
            { // 待收货
                $refundAmt = true; // 可以申请仅退款
                $refundGoodsAmt = true; // 可以申请退货退款
                if($r0[0]['exchange_num'] < 2)
                { // 换货此时小于2次
                    $refundGoods = true; // 可以申请换货
                }
            }
            else if($r0[0]['r_status'] == 5)
            { // 已完成
                if($r0[0]['settlement_type'] == 1)
                {
                    $refund = false;
                    $refundAmt = false;
                    $refundAmtBtn = false;
                    $refundGoods = false;
                    $refundGoodsAmt = false;
                    $refundShowBtn = false;
                }
                else
                {
                    $refundGoodsAmt = true; // 可以申请退货退款
                    $refundAmtBtn = true; // 售后按钮
                    if($r0[0]['settlement_type'] != 1)
                    {
                        $refundAmtBtn = true; // 售后按钮
                    }
                    if($r0[0]['exchange_num'] < 2)
                    { // 换货此时小于2次
                        $refundGoods = true; // 可以申请换货
                    }
                }
            }
            else if($r0[0]['r_status'] == 7 || $r0[0]['settlement_type'] == 1 || $r0[0]['r_status'] == 0)
            { // 订单关闭 或者 已结算 或者 未付款
                $refund = false; // 没有售后按钮
            }
    
            // r_type 类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5:拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后) 11:同意并且寄回商品 12:售后结束 13:人工售後完成 15:极速退款
            $res_k = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->where('r_type','not in','2,4,8,9,10,12,13,15')->select()->toArray();
            if($res_k)
            { // 售后未完成
                $refund = false;
                $refundAmt = false;
                $refundAmtBtn = false;
                $refundGoods = false;
                $refundGoodsAmt = false;
                $refundShowBtn = true;
            }

            $res_k = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->where('r_type','in','4,9,13,15')->select()->toArray();
            if($res_k)
            { // 售后已完成完成
                $refundShowBtn = true;
            }
            
        }
        else
        {
            $r1 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$r0[0]['r_sNo']])->field('status,self_lifting')->select()->toArray();
            if($r1)
            {
                if($r1[0]['self_lifting'] == 1)
                { // 自提无售后
                    $refund = false; // 没有申请售后按钮
                }
                else
                { // 物流订单
                    if($r1[0]['status'] == 1)
                    {
                        $refund = false; // 没有申请售后按钮
                    }
                    else
                    {
                        if($r0[0]['r_status'] == 1)
                        { // 已付款
                            $refundAmt = true; // 可以申请仅退款
                        }
                        else if($r0[0]['r_status'] == 2)
                        { // 待收货
                            $refundAmt = true; // 可以申请仅退款
                            $refundGoodsAmt = true; // 可以申请退货退款
                            if($r0[0]['exchange_num'] < 2)
                            { // 换货此时小于2次
                                $refundGoods = true; // 可以申请换货
                            }
                        }
                        else if($r0[0]['r_status'] == 5)
                        { // 已完成
                            if($r0[0]['settlement_type'] == 1)
                            {
                                $refund = false;
                                $refundAmt = false;
                                $refundAmtBtn = false;
                                $refundGoods = false;
                                $refundGoodsAmt = false;
                                $refundShowBtn = false;
                            }
                            else
                            {
                                if($r1[0]['self_lifting'] == 2)
                                {
                                    $refund = false; // 没有申请售后按钮
                                }
                                else
                                {
                                    $refundGoodsAmt = true; // 可以申请退货退款
                                    $refundAmtBtn = true; // 售后按钮
                                    if($r0[0]['settlement_type'] != 1)
                                    {
                                        $refundAmtBtn = true; // 售后按钮
                                    }
                                    if($r0[0]['exchange_num'] < 2)
                                    { // 换货此时小于2次
                                        $refundGoods = true; // 可以申请换货
                                    }
                                }
                            }
                        }
                        else if($r0[0]['r_status'] == 7 || $r0[0]['settlement_type'] == 1 || $r0[0]['r_status'] == 0)
                        { // 订单关闭 或者 已结算 或者 未付款
                            $refund = false; // 没有售后按钮
                        }
                
                        // r_type 类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5:拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后) 11:同意并且寄回商品 12:售后结束 13:人工售後完成 15:极速退款
                        $res_k = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->where('r_type','not in','2,4,8,9,10,12,13,15')->select()->toArray();
                        if($res_k)
                        { // 售后未完成
                            $refund = false;
                            $refundAmt = false;
                            $refundAmtBtn = false;
                            $refundGoods = false;
                            $refundGoodsAmt = false;
                            $refundShowBtn = true;
                        }

                        $res_k = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->where('r_type','in','4,9,13,15')->select()->toArray();
                        if($res_k)
                        { // 售后已完成完成
                            $refundShowBtn = true;
                        }
                    }

                }
            }
        }

        //获取售后退款状态
        $prompt = '';
        $rId = '';//售后ID
        $res2 = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->field('id,r_type,re_type')->select()->toArray();
        if($res2)
        {   
            $rId = $res2[0]['id'];

            $prompt = self::ObtainAfterSalesStatus($res2[0]['re_type'],$res2[0]['r_type']);
        }

        $data = array('refund'=>$refund,'refundAmt'=>$refundAmt,'refundAmtBtn'=>$refundAmtBtn,'refundGoods'=>$refundGoods,'refundGoodsAmt'=>$refundGoodsAmt,'refundShowBtn'=>$refundShowBtn,'name'=>$prompt,'id'=>$rId);
        return $data;
    }

    // 获取订单售后状态-优化
    public static function getReturnOrderStatus0($store_id,$oid,$order_after = 7)
    {
        $refund = 1; // 申请售后 0.没有申请按钮 1.有申请按钮
        $refundAmt = 0; // 仅退款 只有代发货才有仅退款 0.没有仅退款选项 1.有仅退款选项
        $refundGoods = 0; // 换货 未发货没有换货 0.没有换货选项 1.有换货选项
        $refundGoodsAmt = 0; // 退货退款 未发货没有退货退款 0.没有退货退款选项 1.有退货退款选项
        $refundShowBtn = 0; // 查看售后按钮 0.没有查看售后按钮 1.有查看售后按钮
        
        $r0 = OrderDetailsModel::where(['store_id'=>$store_id,'id'=>$oid])->field('r_sNo,r_status,arrive_time,exchange_num,settlement_type')->select()->toArray();
        $otype = substr($r0[0]['r_sNo'], 0, 2);
        $arrive_time = $r0[0]['arrive_time']; // 到货时间
        $arrive_times = strtotime($arrive_time);
        $order_after_times = $order_after * 24 * 60 * 60;
        if($otype == 'JP')
        { // 竞拍订单没有售后
            $refund = 0; // 没有申请按钮
        }
        elseif($otype == 'MS' || $otype == 'FX')
        {
            if($r0[0]['r_status'] == 0 || $r0[0]['r_status'] == 1)
            { // 待付款 或者 待发货
                $refund = 0; // 没有申请按钮
            }
            elseif($r0[0]['r_status'] == 5)
            {
                if($r0[0]['exchange_num'] < 2)
                {
                    $refundGoods = 1; // 有换货选项
                }
            }
            $res_k = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->where('r_type','not in','2,4,8,9,10,12,13,15')->select()->toArray();
            if($res_k)
            {            
                $refund = 0; // 没有申请按钮
                $refundShowBtn = 1; // 有查看售后按钮
            }
        }
        else
        {
            $r1 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$r0[0]['r_sNo']])->field('status,self_lifting')->select()->toArray();
            if($r1)
            {
                if($r1[0]['self_lifting'] == 1)
                { // 自提无售后
                    $refund = 0; // 没有申请售后按钮
                }
                else
                { // 物流订单
                    if($r1[0]['status'] == 1)
                    {
                        $refund = 0; // 没有售后按钮
                    }
                    else
                    {
                        if($r0[0]['r_status'] == 1)
                        { // 已付款
                            $refund = 0; // 没有售后按钮
                            $refundAmt = 1; // 可以申请仅退款
                        }
                        else if($r0[0]['r_status'] == 2)
                        { // 待收货
                            $refundAmt = 1; // 可以申请仅退款
                            $refundGoodsAmt = 1; // 可以申请退货退款
                            if($r0[0]['exchange_num'] < 2)
                            { // 换货此时小于2次
                                $refundGoods = 1; // 可以申请换货
                            }
                        }
                        else if($r0[0]['r_status'] == 5)
                        { // 已完成
                            $refundGoodsAmt = 1; // 可以申请退货退款
                            $refundAmtBtn = 1; // 售后按钮
                            if($r0[0]['settlement_type'] != 1)
                            {
                                $refundAmtBtn = 1; // 售后按钮
                            }
                            if($r0[0]['exchange_num'] < 2)
                            { // 换货此时小于2次
                                $refundGoods = 1; // 可以申请换货
                            }

                            if($arrive_times != '')
                            { // 已经确认收货
                                if ((time() - $order_after_times) > $arrive_times || $r0[0]['settlement_type'] == 1)
                                { // 当前时间戳 - 售后时间戳 > 到货时间戳 或者 已结算
                                    $refund = 0; // 没有售后按钮
                                }
                            }
                        }
                        else if($r0[0]['r_status'] == 7 || $r0[0]['settlement_type'] == 1 || $r0[0]['r_status'] == 0)
                        { // 订单关闭 或者 已结算 或者 未付款
                            $refund = 0; // 没有售后按钮
                        }
                
                        // r_type 类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5:拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后) 11:同意并且寄回商品 12:售后结束 13:人工售後完成 15:极速退款
                        $res_k = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->where('r_type','not in','2,4,8,9,10,12,13,15')->select()->toArray();
                        if($res_k)
                        { // 售后未完成
                            $refund = 0;
                            $refundAmt = 0;
                            $refundAmtBtn = 0;
                            $refundGoods = 0;
                            $refundGoodsAmt = 0;
                            $refundShowBtn = 1;
                        }

                        $res_k = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->where('r_type','in','4,9,13,15')->select()->toArray();
                        if($res_k)
                        { // 售后已完成完成
                            $refundShowBtn = 1;
                        }
                    }
                }
            }
        }

        //获取售后退款状态
        $prompt = '';
        $rId = '';//售后ID
        $res2 = ReturnOrderModel::where(['store_id'=>$store_id,'p_id'=>$oid])->field('id,r_type,re_type')->select()->toArray();
        if($res2)
        {   
            $rId = $res2[0]['id'];

            $prompt = self::ObtainAfterSalesStatus($res2[0]['re_type'],$res2[0]['r_type']);
        }

        $data = array('refund'=>$refund,'refundAmt'=>$refundAmt,'refundAmtBtn'=>$refundAmtBtn,'refundGoods'=>$refundGoods,'refundGoodsAmt'=>$refundGoodsAmt,'refundShowBtn'=>$refundShowBtn,'name'=>$prompt,'id'=>$rId);
        return $data;
    }

    //获取订单配置
    public static function getOrderConfig($store_id,$oid)
    {   
        $otype = '';
        $order_after = 7; // 订单售后时限
        $order_failure = 2;// 订单失效
        $company = "day";

        $admin_shop_id = PC_Tools::SelfOperatedStore($store_id); // 获取自营店ID

        $r = OrderModel::where(['store_id'=>$store_id,'id'=>$oid])->select()->toArray();
        if($r)
        {
            $otype = $r[0]['otype'];//订单类型
            $mch_id = trim($r[0]['mch_id'], ','); // 店铺ID
            if ($otype == 'MS') //秒
            {
                $r_2 = SecondsConfigModel::where(['store_id'=>$store_id,'mch_id'=>$admin_shop_id])->select()->toArray();
                if ($r_2) 
                {
                    $order_failure = floatval($r_2[0]['order_failure']/3600); // 订单失效
                    $order_after = floatval($r_2[0]['order_after']/3600/24); // 订单售后时限
                }
            }
            else if ($otype == 'IN') //秒
            {
                $r_2 = IntegralConfigModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
                if ($r_2) 
                {
                    $order_failure = floatval($r_2[0]['order_failure']/3600); // 订单失效
                    $order_after = floatval($r_2[0]['order_after']/3600/24); // 订单售后时限
                }
            }
            else if ($otype == 'PS') //秒
            {
                $r_2 = PreSellConfigModel::where('store_id',$store_id)->select()->toArray();
                if ($r_2) 
                {
                    $order_failure = floatval($r_2[0]['order_failure']/3600); // 订单失效
                    $order_after = floatval($r_2[0]['order_after']/3600/24); // 订单售后时限
                }
            }
            else if ($otype == 'PT') //秒
            {
                $r_2 = GroupOrderConfigModel::where('store_id',$store_id)->select()->toArray();
                if ($r_2) 
                {
                    // $order_failure = floatval($r_2[0]['order_failure']/3600/24); // 订单失效
                    $order_after = floatval($r_2[0]['order_after']/3600/24); // 订单售后时限
                }
            }
            else if ($otype == 'FS') //秒
            {
                $sql_2 = "select * from lkt_flashsale_config where store_id = '$store_id' and mch_id = 0 ";
                $r_2 = Db::query($sql_2);
                if ($r_2) 
                {
                    // $order_failure = floatval($r_2[0]['order_failure']/3600/24); // 订单失效
                    $order_after = floatval($r_2[0]['order_after']/3600/24); // 订单售后时限
                }
            }
            else 
            {
                $r_2 = OrderConfigModel::where('store_id',$store_id)->select()->toArray();
                if ($r_2)
                {
                    $order_failure = $r_2[0]['order_failure']; // 订单失效
                    $order_after = $r_2[0]['order_after']; // 订单售后时限
                    if ($r_2[0]['company'] == '天')
                    {
                        $company = "day";
                    }
                    else
                    {
                        $company = "hour";
                    }
                }
            }
        }

        $data = array('order_after'=>$order_after,'order_failure'=>$order_failure,'company'=>$company);
        return $data;
    }

    // 消息推送和消息记录
    public function orderMessage($sNo, $store_id, $user_id,$shop_id)
    {
        if(count($shop_id) == 1)
        {
            $r1 = OrderModel::where(['sNo'=>$sNo])->field('z_price,sNo,mch_id')->select()->toArray();
        }
        else
        {
            $r1 = OrderModel::where(['p_sNo'=>$sNo])->field('z_price,sNo,mch_id,status')->select()->toArray();
        }
        if ($r1)
        {

            foreach ($r1 as $ke => $va)
            {
                $r_sNo = $va['sNo'];
                $z_price = $va['z_price'];

                $msg_title = "【" . $r_sNo . "】订单支付成功！";
                $msg_content = Lang("order.13");

                /**买家付款成功通知(站内信)*/
                $pusher = new LaikePushTools();
                $pusher->pushMessage($user_id,$msg_title, $msg_content, $store_id, '');

                // 微信订阅消息发送
                $msgres = new \stdClass();
                $msgres->uid = Tools::get_openid($store_id, $user_id);
                $msgres->sNo = $r_sNo;
                $ordersinfo = OrderDetailsModel::where(['r_sNo'=>$r_sNo])->select()->toArray();
                if (count($ordersinfo) > 0)
                {
                    $msgres->p_name = $ordersinfo[0]['p_name'];
                }
                else
                {
                    $msgres->p_name = '默认商品名';
                }
                $msgres->amount4 = $z_price;
                $this->sendWXTopicMsg($msgres, $store_id);
                
                /**买家付款成功通知卖家(站内信)*/
                // $mch_id = $va['mch_id'];
                // $mch_id = substr($mch_id, 1, -1);
                // $r0 = MchModel::where('id',$mch_id)->field('user_id')->select()->toArray();
                // $msg_title = Lang("pay.11");

                // $pusher->pushMessage($r0[0]['user_id'], $msg_title, "订单号【" . $r_sNo . "】,请发货", $store_id, '');
            }
        }
    }

    /**
     * 发送微信订阅消息
     * @param $msgres
     * @param $store_id
     * @throws Exception
     */
    public function sendWXTopicMsg($msgres, $store_id)
    {
        try
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '推送微信小程序订阅消息开始 ';
            LaiKeLogUtils::lktLog($Log_content);
            $orderData = array();
            //用户
            $orderData['touser'] = $msgres->uid;
            //跳转地址
            $orderData['page'] = 'pages/index/index';
            //商城ID
            $orderData['store_id'] = $store_id;
            //订单号
            $orderData['character_string1'] = $msgres->sNo;
            //商品名称
            $orderData['thing3'] = $msgres->p_name;
            //支付金额
            $orderData['amount4'] = $msgres->amount4;
            //支付时间
            $time = date("Y-m-d H:i", time());
            $orderData['date2'] = $time;
            WXTopicMsgUtils::orderSuccess($orderData);
            $Log_content = __METHOD__ . '->' . __LINE__ . '推送微信小程序订阅消息结束 ';
            LaiKeLogUtils::lktLog($Log_content);
        } catch (Exception $e)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '推送微信小程序订阅消息失败 ';
            LaiKeLogUtils::lktLog($Log_content);
            LaiKeLogUtils::lktLog(__METHOD__ . '->' . __LINE__ . $e->getMessage());
        }
    }

    // 获取售后状态
    public static function ObtainAfterSalesStatus($re_type,$r_type)
    {   
        // re_type 退款类型 1:退货退款  2:退款 3:换货
        // r_type 类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5:拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后) 11:同意并且寄回商品 12:售后结束 13:人工售後完成 15:极速退款
        $prompt = "";
        
        if($re_type == 0 && $r_type == 100)
        {

        }
        else
        {
            if(($re_type == 1 && $r_type == 1) || ($re_type == 1 && $r_type == 3) || ($re_type == 1 && $r_type == 16))
            { // (退货退款 并且 同意并让用户寄回) 或者 (退货退款 并且 用户已快递)
                $prompt = '退款中';
            }
            elseif($r_type == 4 || $r_type == 9 || $r_type == 13 || $r_type == 15)
            { // 收到寄回商品,同意并退款 或者 同意并退款 或者 人工售後完成 或者 极速退款
                $prompt = '退款成功';
            }
            elseif(($re_type != 3 && $r_type == 2)|| ($re_type != 3 && $r_type == 5) || ($re_type != 3 && $r_type == 8))
            { // (不是换货 并且 拒绝(退货退款)) 或者 (不是换货 并且 拒绝并退回商品) 或者 (不是换货 并且 拒绝(退款))
                $prompt = '退款失败';
            }
            elseif(($re_type == 3 && $r_type == 1)|| ($re_type == 3 && $r_type == 3) || ($re_type == 3 && $r_type == 11) || ($re_type == 3 && $r_type == 16))
            { // (换货 并且 同意并让用户寄回) 或者 (换货 并且 用户已快递) 或者 (换货 并且 同意并且寄回商品)
                $prompt = '换货中';
            }
            elseif($r_type == 12)
            { // 售后结束
                $prompt = '换货成功';
            }
            elseif(($re_type == 3 && $r_type == 5)|| ($re_type == 3 && $r_type == 10))
            { // (换货 并且 拒绝并退回商品) 或者 (换货 并且 拒绝(售后))
                $prompt = '换货失败';
            }
            else
            {
                $prompt = '审核中';
            }
        }

        return $prompt;
    }

    /**
     * [sendMoney 企业付款到零钱]
     * @param [type] $amount  [发送的金额（分）目前发送金额不能少于1元]
     * @param [type] $re_openid [发送人的 openid]
     * @param string $desc  [企业付款描述信息 (必填)]
     * @param string $check_name [收款用户姓名 (选填)]
     * @return [type]    [description]
     */
    function sendMoney($store_id,$amount,$openid,$desc)
    {
        $lktlog = new LaiKeLogUtils();
        $config = LKTConfigInfo::getPayConfig($store_id,'wechat_v3_withdraw');
        $appid = $config['appid']; // 如果是公众号 就是公众号的appid
        $mch_id = $config['mch_id']; // 商户id
        
        $serial = $config['serial_no']; // 商户id
        $priKey = rawurldecode($config['key_pem']); // 商户id
        
        $total_amount = round(100 * $amount);

        $queryUrl = 'https://api.mch.weixin.qq.com/v3/transfer/batches';//账单接口
        $param = array(
            'appid'=>$appid, // 【商户appid】 申请商户号的appid或商户号绑定的appid（企业号corpid即为此appid）
            'out_batch_no'=>date('YmdHis').rand(1000, 9999), // 【商家批次单号】 商户系统内部的商家批次单号，要求此参数只能由数字、大小写字母组成，在商户系统内部唯一
            'batch_name'=>$desc, // 【批次名称】 该笔批量转账的名称
            'batch_remark'=>$desc, // 【批次备注】 转账说明，UTF8编码，最多允许32个字符
            'total_amount'=>$total_amount, // 【转账总金额】 转账金额单位为“分”。转账总金额必须与批次内所有明细转账金额之和保持一致，否则无法发起转账操作
            'total_num'=>1, // 【转账总笔数】 一个转账批次单最多发起一千笔转账。转账总笔数必须与批次内所有明细之和保持一致，否则无法发起转账操作
            'transfer_detail_list'=>array(
                array(
                    'out_detail_no'=>$this->createNoncestr(), // 【商家明细单号】 商户系统内部区分转账批次单下不同转账明细单的唯一标识，要求此参数只能由数字、大小写字母组成
                    'transfer_amount'=>$total_amount, // 【转账金额】 转账金额单位为“分”
                    'transfer_remark'=>$desc, // 【转账备注】 单条转账备注（微信用户会收到该备注），UTF8编码，最多允许32个字符
                    'openid'=>$openid // 【收款用户openid】 商户appid下，某用户的openid    
                )
            )
        );

        $authorization = Formatter::getAuthorization($queryUrl, $param, $mch_id, $priKey, $serial);//生成签名

        $result = Formatter::curlPostWithWx($param, $authorization,$queryUrl,$serial); // 接口请求
        
        $lktlog->log("app/Tools.log",__METHOD__ . '->' . __LINE__ . '返回数据:' . json_encode($result));
        return $result;
    }

    /**
     * [createNoncestr 生成随机字符串]
     * @param integer $length [长度]
     * @return [type]   [字母大小写加数字]
     */
    function createNoncestr($length = 32)
    {
        $chars = "ABCDEFGHIJKLMNOPQRSTUVWXYabcdefghijklmnopqrstuvwxyz0123456789"; 
        $str ="";
        
        for($i = 0;$i < $length;$i++)
        { 
            $str .= substr($chars, mt_rand(0, strlen($chars)-1), 1); 
        } 
        return $str;
    }

    // 获取access_token
    public function get_access_token($store_id)
    {
        $access_token = '';
        $sql0 = "select config_data from lkt_payment as p left join lkt_payment_config as c on c.pid = p.id where c.status = 1 and p.class_name = 'mini_wechat' and c.store_id = '$store_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $config_data = json_decode($r0[0]['config_data'],true);
            $appid = ''; // 公众号唯一标识
            $appsecret = ''; // 公众号的 app secret
            if(isset($config_data['appid']))
            {
                $appid = $config_data['appid']; // 公众号唯一标识
                $appsecret = $config_data['appsecret']; // 公众号的 app secret
            }

            $url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=".$appid."&secret=".$appsecret;
            $res = $this->https_get($url);
            $data = json_decode($res,true);

            if(isset($data['access_token']))
            {
                $access_token = $data['access_token'];
            }
        }

        return $access_token;
    }

    // 模糊查询拼接
    public static function FuzzyQueryConcatenation($key)
    {
        $res = '/' . $key;
        $str = "  '%$res%'ESCAPE'/' ";
        return $str;
    }

    // 判断是否是敏感词
    public static function is_sensitive_words($array)
    {
        $store_id = $array['store_id'];
        $keyword = $array['keyword'];

        $sql = "select word from lkt_sensitive_words where store_id = '$store_id' and recycle = 0 ";
        $r = Db::query($sql);
        if($r)
        {
            foreach($r as $k => $v)
            {
                if(strpos($v['word'], $keyword) !== false)
                {
                    $message = Lang('Sensitive words exist');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                } 
            }
        }

        return;
    }

    // 获取系统设置
    public static function Get_system_settings()
    {
        $list = array();

        $sql0 = "select id from lkt_customer where recycle = 0 and is_default = 1 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $store_id = $r0[0]['id'];

            $sql1 = "select id,store_id,logon_logo as logo,copyright_information,record_information,link_to_landing_page,admin_default_portrait from lkt_config where store_id = '$store_id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $link_to_landing_page = json_decode($r1[0]['link_to_landing_page'],true);
                if($link_to_landing_page)
                {
                    foreach ($link_to_landing_page as $key => $value) 
                    {
                        $link_to_landing_page[$key]['url'] = $value['url']; //"https://".
                    }
                    $link_to_landing_page = json_encode($link_to_landing_page);
                }
                $r1[0]['link_to_landing_page'] = $link_to_landing_page;
                $r1[0]['adminDefaultPortrait'] = $r1[0]['admin_default_portrait'];

                $list = $r1[0];
            }
        }

        return $list;
    }

    // 获取失效时间
    public static function Obtain_expiration_time($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $otype = $array['otype']; // 订单类型

        $order_failure_time = 3600;
        if($otype == 'PS')
        { // 预售
            $sql = "select order_failure from lkt_pre_sell_config where store_id = '$store_id' ";
            $r = Db::query($sql);
            if ($r)
            {
                $order_failure = $r[0]['order_failure']; // 订单失效（秒）
            }
        }
        else if($otype == 'JP')
        { // 竞拍
            $sql = "select order_failure from lkt_auction_config where store_id = '$store_id' ";
            $r = Db::query($sql);
            if($r)
            {
                $order_failure_time = $r[0]['order_failure']; // 订单失效（秒）
            }
        }
        else if($otype == 'IN')
        { // 积分
            $sql = "select order_failure from lkt_integral_config where store_id = '$store_id' ";
            $r = Db::query($sql);
            if($r)
            {
                $order_failure_time = $r[0]['order_failure']; // 订单失效（秒）
            }
        }
        else if($otype == 'MS')
        { // 秒杀
            $sql = "select order_failure from lkt_seconds_config where store_id = '$store_id' ";
            $r = Db::query($sql);
            if($r)
            {
                $order_failure_time = $r[0]['order_failure']; // 订单失效（秒）
            }
        }
        else
        {
            $sql = "select order_failure from lkt_order_config where store_id = '$store_id' ";
            $r = Db::query($sql);
            if($r)
            {
                $order_failure_time = $r[0]['order_failure'] * 3600; // 订单失效（秒）
            }
        }

        return $order_failure_time;
    }

    // 获取8位随机生成字母数字
    public static function generateRandomString($length)
    {
        // 生成足够长的随机字符串
        $bytes = random_bytes(ceil($length / 2));
        // 将二进制数据转换为十六进制表示
        $zhanghao = 'lkt' . substr(bin2hex($bytes), 0, $length);

        $sql0 = "select id from lkt_user where zhanghao = '$zhanghao' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $zhanghao = self::generateRandomString($length);
        }
        else
        {
            return $zhanghao;
        }
    }

    // 获取语言
    public static function get_lang($language)
    {
        $lang_code = 'zh_CN';
        if($language == 'en' || $language == 'en_US')
        {
            $lang_code = 'en_US';
        }
        else if($language == 'zh_TW')
        {
            $lang_code = 'zh_TW';
        }
        else if($language == 'ja_JP')
        {
            $lang_code = 'ja_JP';
        }
        else if($language == 'ru_RU')
        {
            $lang_code = 'ru_RU';
        }
        else if($language == 'ms_MY')
        {
            $lang_code = 'ms_MY';
        }
        else if($language == 'id_ID')
        {
            $lang_code = 'id_ID';
        }
        else if($language == 'fil_PH')
        {
            $lang_code = 'fil_PH';
        }

        return $lang_code;
    }

    // 获取语言名称
    public static function get_lang_name($lang_code)
    {
        $lang_name = "";
        $sql = "select lang_name from lkt_lang where lang_code = '$lang_code' ";
        $r = Db::query($sql);
        if($r)
        {
            $lang_name = $r[0]['lang_name'];
        }

        return $lang_name;
    }

    // 获取商城币种
    public static function get_store_currency($array)
    {
        $store_id = $array['store_id'];
        $type = $array['type'];
        $id = $array['id'];

        $list = array();
        $con = " a.store_id = '$store_id' and a.recycle = 0 ";
        if($type == 1)
        {
            $con .= " and a.default_currency = 1 ";
        }
        if($id != 0)
        {
            $con .= " and a.currency_id = '$id' ";
        }
        $sql0 = "select a.currency_id,a.default_currency,a.exchange_rate,a.is_show,a.recycle,a.store_id,a.update_time,b.id,b.currency_code,b.currency_name,b.currency_symbol from lkt_currency_store as a left join lkt_currency as b on a.currency_id = b.id where $con order by a.default_currency desc ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $r0[0]['exchange_rate'] = round($r0[0]['exchange_rate'],2);
            $list = $r0;
        }

        return $list;
    }

    // 获取国家名称
    public static function get_country_name($country_num)
    {
        $country_name = "";
        $sql = "select name from lkt_ds_country where num3 = '$country_num' ";
        $r = Db::query($sql);
        if($r)
        {
            $country_name = $r[0]['name'];
        }
        return $country_name;
    }

    // 判断语言和国家代码是否必选
    public static function National_Language($lang_code,$country_num)
    {
        if($lang_code == '')
        {
            $message = Lang("please select language");
            echo json_encode(array('code' => ERROR_CODE_qxzyy, 'message' => $message));
            exit();
        }

        if($country_num == '')
        {
            $message = Lang("Please select the country code");
            echo json_encode(array('code' => ERROR_CODE_qxzgjdm, 'message' => $message));
            exit();
        }
        return;
    }

    // 获取数据字典
    public static function get_data_dictionary($array)
    {
        $name = $array['name']; // 数据名称
        $lang_code = '';
        if(isset($array['lang_code']) )
        {
            $lang_code = $array['lang_code']; // 语种
        }
        $arr = array();

        if($name == '商品分类')
        {
            // 查询数据字典里，存在的分类级别
            $sql0_0 = "select a.value,a.text from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.recycle = 0 and b.name = '$name' and a.lang_code = '$lang_code' ";
            $r0_0 = Db::query($sql0_0);
            if($r0_0)
            {
                foreach ($r0_0 as $k => $v)
                {
                    $k0_0 = $v['value'];
                    $arr[$k0_0] = $v['text'];
                }
            }
        }
        else if($name == '商品展示位置')
        {
            $show_adr = $array['show_adr'];

            $sql0_0 = "select a.value,a.text from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.recycle = 0 and b.name = '$name' and a.lang_code = '$lang_code' and a.status = 1";
            $r0_0 = Db::query($sql0_0);
            if ($r0_0)
            {
                foreach ($r0_0 as $k => $v)
                {
                    if (in_array($v['value'], $show_adr))
                    {
                        $arr[] = array('name' => $v['text'], 'value' =>$v['value'], 'status' => true);
                    }
                    else
                    {
                        $arr[] = array('name' => $v['text'], 'value' =>$v['value'], 'status' => false);
                    }
                }
            }
        }
        else if($name == '单位')
        {
            $sql0_0 = "select a.value,a.text from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.recycle = 0 and b.name = '$name' and a.lang_code = '$lang_code' and a.status = 1";
            $r0_0 = Db::query($sql0_0);
            if ($r0_0)
            {
                foreach ($r0_0 as $k => $v)
                {
                    $arr[] = $v['text'];
                }
            }
        }
        else if($name == 'DIY主题')
        {
            $sql0_0 = "select a.code,a.value,a.text from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.recycle = 0 and b.name = '$name' and a.status = 1";
            $r0_0 = Db::query($sql0_0);
            if ($r0_0)
            {
                foreach ($r0_0 as $k => $v)
                {
                    $arr[] = $v;
                }
            }
        }
        else if($name == 'VOD视频存储地' || $name == 'VOD视频转码模板')
        {
            $sql0_0 = "select a.value,a.text from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.recycle = 0 and b.name = '$name' and a.status = 1";
            $r0_0 = Db::query($sql0_0);
            if ($r0_0)
            {
                foreach ($r0_0 as $k => $v)
                {
                    $arr[] = $v;
                }
            }
        }

        return $arr;
    }

    // 获取数据字典
    public static function get_country($country_num)
    {
        $name = '';

        // 查询数据字典里，存在的分类级别
        $sql0_0 = "select name from lkt_ds_country where num3 = '$country_num' ";
        $r0_0 = Db::query($sql0_0);
        if($r0_0)
        {
            $name = $r0_0[0]['name'];
        }

        return $name;
    }

    // 币种换算
    public static function Currency_Conversion($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $currency_id = $array['currency_id']; // 币种ID
        $money = $array['money']; // 金额

        $amount = $money;
        $sql0 = "select currency_id,default_currency,exchange_rate from lkt_currency_store where store_id = '$store_id' and recycle = 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                if($v0['currency_id'] == $currency_id)
                {
                    $amount = round($money * $v0['exchange_rate'],2);
                }
            }
        }
        return $amount;
    }

    // 是否登录
    public static function is_logged_in($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $access_id = $array['access_id']; // 
        $login_status = $array['login_status']; // 0.不需要验证 1.需要验证

        $list = array();
        if ($access_id != '')
        {
            // 查询用户id
            $user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
            if ($user)
            {
                $list = $user;
                return $list;
            }
            else
            {
                if($login_status == 1)
                {
                    $message = Lang('Please log in');
                    echo json_encode(array('code' => ERROR_CODE_QDL, 'message' => $message));
                    exit;
                }
                else
                {
                    return $list;
                }
            }
        }
        else
        {
            if($login_status == 1)
            {
                $message = Lang('Please log in');
                echo json_encode(array('code' => ERROR_CODE_QDL, 'message' => $message));
                exit;
            }
            else
            {
                return $list;
            }
        }
    }
}
