<?php

/**
 * [Laike System] Copyright (c) 2018 laiketui.com
 * Laike is not a free software, it under the license terms, visited http://www.laiketui.com/ for more details.
 */

namespace app\admin\controller\mch\mall;

use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;
use app\common\Plugin\Plugin;
use app\common\Plugin\MchPublicMethod;

use app\admin\model\UserModel;
use app\admin\model\UserCollectionModel;
use app\admin\model\ProductListModel;
use app\admin\model\ProductClassModel;
use app\admin\model\BannerModel;
use app\admin\model\MchModel;
use app\admin\model\MchStoreModel;
use app\admin\model\AgreementModel;
use app\admin\model\AdminCgGroupModel;
use app\admin\model\MchConfigModel;
use app\admin\model\ProLabelModel;
use app\admin\model\UserSearchModel;
use app\admin\model\MchClassModel;
use app\admin\model\UserAuthorityModel;

/**
 * <p>Copyright (c) 2019</p>
 * <p>Company: www.laiketui.com</p>
 * @Author  段宏波
 * @version 2.0
 * @date    2019-01-29 15:51:29+0800
 * @return  [type]   多店铺接口   [description]
 */
class Mch
{
    // 店铺主页
    public function storeHomepage()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言

        $shop_id = addslashes(trim(Request::param('shop_id'))); // 店铺ID
        $cid = addslashes(trim(Request::param('cid'))); // 分类ID
        $keyword = addslashes(trim(Request::param('keyword'))); // 关键字
        $class_id = addslashes(trim(Request::param('class_id'))); // 选择的分类ID(71,74)
        $amount = addslashes(trim(Request::param('amount'))); // 金额
        $sort_type = addslashes(trim(Request::param('sortType'))); // 排序类型(price 或 volume)
        $sort = addslashes(trim(Request::param('sort'))); // 排序(asc 或 desc)  0.asc  1.desc  
        $page = addslashes(trim(Request::param('page'))); // 加载次数
        $end = addslashes(trim(Request::param('pageSize'))); // 加载次数

        $time = date("Y-m-d H:i:s");
        $lang_code = Tools::get_lang($language);

        if (!$page)
        {
            $page = 1;
        }
        $start = ($page - 1) * $end;

        $user_id = '';
        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
        if ($r_user)
        {
            $user_id = $r_user[0]['user_id'];
        }

        $collection_status = 0; // 未收藏
        if ($user_id != '')
        {
            $r_collection = UserCollectionModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'mch_id'=>$shop_id])->field('id')->select()->toArray();
            if ($r_collection)
            {
                $collection_status = 1; // 已收藏
            }
        }

        $label_id_t = 0;
        $r_sp_type_t = ProLabelModel::where(['store_id'=>$store_id,'name'=>'推荐'])->field('id')->select()->toArray();
        if($r_sp_type_t)
        {
            $label_id_t = $r_sp_type_t[0]['id'];
        }
        
        // 获取商品设置
        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));

        // 根据商城id、用户id、审核通过，查询店铺ID
        $data0 = array('store_id'=>$store_id,'review_status'=>1,'id'=>$shop_id);
        $r0 = MchModel::where($data0)->field('id,name,logo,roomid,is_open,old_roomid,sheng,shi,xian,address,poster_img,head_img,shop_information')->select()->toArray();
        if ($r0)
        {
            $mchAddress = $r0[0]['sheng'] . $r0[0]['shi'] . $r0[0]['xian'] . $r0[0]['address'];
            $shop_name = $r0[0]['name']; // 店铺名称
            $shop_information = $r0[0]['shop_information']; // 店铺信息
            $shop_logo = ServerPath::getimgpath($r0[0]['logo'], $store_id); // 店铺logo
            $poster_img = ServerPath::getimgpath($r0[0]['poster_img'], $store_id); // 店铺宣传图
            $head_img = ServerPath::getimgpath($r0[0]['head_img'], $store_id); // 店铺头像
            $roomid = $r0[0]['roomid']; // 直播房间ID
            $is_open = $r0[0]['is_open']; // 是否营业：0.未营业 1.营业中 2.打烊
            $old_roomid = $r0[0]['old_roomid'];//上一次直播房间号
            
            $product_class = array();
            
            $collection_num = 0; // 收藏数量
            $quantity_on_sale = 0; // 在售数量
            $quantity_sold = 0; // 已售数量

            $mchPublicMethod = new MchPublicMethod();
            $mch_list = $mchPublicMethod->commodity_information($store_id,$shop_id);
            $collection_num = $mch_list['collection_num'];
            $quantity_on_sale = $mch_list['quantity_on_sale'];
            $quantity_sold = $mch_list['quantity_sold'];

            $recommend_list = array();

            $str1 = " a.id,a.product_title,a.subtitle,a.keyword,a.imgurl,a.status,a.product_class,a.brand_id,a.freight,a.gongyingshang,a.mch_id,a.recycle,a.s_type,a.volume,a.num,c.id as cid,min(c.price) over (partition by c.pid) as price,c.costprice,c.yprice,c.img,c.num as stockNum,c.pid,c.attribute,row_number () over (partition by c.pid) as top ";

            // 查询店家推荐
            $sql1 = "select tt.* from (select $str1 from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.mch_status = 2 and a.recycle = 0 and a.active = 1 and a.is_presell = 0 and a.mch_id = '$shop_id' and a.s_type like '%,$label_id_t,%' $status order by a.supplier_superior,a.mch_sort desc,a.upper_shelf_time desc) as tt where tt.top<2 LIMIT 0,4";
            $r1 = Db::query($sql1);
            
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                    $v['mchId'] = $shop_id;
                    $v['logo'] = $shop_logo;
                    $v['mch_name'] = $shop_name;
                    $v['is_open'] = $is_open;
                    $v['sizeid'] = $v['cid'];
                    $recommend_list[] = $v;
                }
            }

            $con = " a.store_id = '$store_id' and a.commodity_type in (0,1) and a.mch_status = 2 and a.recycle = 0 and a.active = 1 and a.mch_id = '$shop_id' and a.is_presell = 0 and a.lang_code = '$lang_code' $status ";
            $con0 = " a.store_id = '$store_id' and a.commodity_type in (0,1) and a.mch_status = 2 and a.recycle = 0 and a.active = 1 and a.mch_id = '$shop_id' and a.is_presell = 0 and a.lang_code = '$lang_code' $status ";
            if($keyword != '')
            { // 搜索
                $keyword_0 = Tools::FuzzyQueryConcatenation($keyword);
                $con .= " and (a.product_title like $keyword_0 or a.keyword like $keyword_0) ";
                $con0 .= " and (a.product_title like $keyword_0 or a.keyword like $keyword_0) ";
                if($user_id == '')
                {
                    $r_search = UserSearchModel::where(['store_id'=>$store_id,'token'=>$access_id,'keyword'=>$keyword])->field('keyword')->select()->toArray();
                }
                else
                {
                    $r_search = UserSearchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'keyword'=>$keyword])->field('keyword')->select()->toArray();
                }
                if($r_search)
                {
                    Db::name('_user_search')->where(['store_id'=>$store_id,'user_id'=>$user_id,'keyword'=>$keyword])->update(['add_date' => $time]);
                }
                else
                {
                    $sql_search1 = array('store_id'=>$store_id,'token'=>$access_id,'user_id'=>$user_id,'keyword'=>$keyword,'add_date'=>$time);
                    Db::name('_user_search')->insert($sql_search1);
                }
            }
            if($cid != '')
            { // 点击分类
                $con .= " and a.product_class like '%-$cid-%' ";
                $con0 .= " and a.product_class like '%-$cid-%' ";
            }
            if($class_id != '')
            {
                $class_id = explode(',',$class_id);
                $co = ' and (';
                foreach ($class_id as $k =>$v)
                {
                    if($k == 0)
                    {
                        $co .= " a.product_class like '%-$v-%' ";
                    }
                    else
                    {
                        $co .= " or a.product_class like '%-$v-%' ";
                    }
                }
                $co .= ')';
                $con .= $co;
            }
            if($amount != '')
            {
                $amount = explode('-',$amount);
                $min_amount = $amount[0];
                $max_amount = $amount[1];
                if($max_amount != '')
                {
                    $con .= " and c.price >= '$min_amount' and c.price <= '$max_amount' ";
                }
                else
                {
                    $con .= " and c.price >= '$min_amount' ";
                }
            }

            if($sort == 1)
            {
                $sort = 'desc';
            }
            else
            {
                $sort = 'asc';
            }

            if($sort_type == 'price')
            {
                $sort_type = " price " . $sort;
            }
            else if($sort_type == 'volume')
            {
                $sort_type = " volume desc ";
            }
            else
            {
                $sort_type = " a.supplier_superior,a.mch_sort desc,a.upper_shelf_time desc ";
            }

            $class_list = array();
            $product_class_list = array();
            // 查询店铺里的商品
            $sql2 = "select a.product_class,c.pid from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where $con0 group by a.product_class,c.pid order by a.sort desc ";
            $r2 = Db::query($sql2);
            if($r2)
            {
                foreach ($r2 as $k => $v)
                {
                    $product_class_0 = explode('-',trim($v['product_class'],'-'));
                    if(isset($product_class_0[1]))
                    {
                        if(!in_array($product_class_0[1],$product_class_list))
                        { // 分类ID不存在数组中
                            $product_class_list[] = $product_class_0[1];
                        }
                    }
                    
                    // $product_class = explode('-',$v['product_class']);
                    // if(!in_array($product_class[1],$product_class_list))
                    // { // 分类ID不存在数组中
                    //     $product_class_list[] = $product_class[1];
                    // }
                }
            }

            if(count($product_class_list) > 0)
            {
                foreach ($product_class_list as $k => $v)
                {
                    $r2_0 = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$v,'recycle'=>0])->order('sort','desc')->field('cid,pname')->select()->toArray();
                    if($r2_0)
                    {
                        $class_list[] = $r2_0[0];
                    }
                }
            }

            $sql2_1 = "select a.id from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where $con group by c.pid ";
            $r2_1 = Db::query($sql2_1);
            $total = count($r2_1);

            $sql2_2 = "select tt.* from (select a.id,a.product_title,a.imgurl,a.status,a.volume,a.product_class,a.brand_id,min(c.price) over (partition by c.pid) as price,row_number () over (partition by c.pid) as top from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where $con order by $sort_type ) as tt where tt.top<2 limit $start,$end";
            $r2_2 = Db::query($sql2_2);
            if ($r2_2)
            {
                foreach ($r2_2 as $k => $v)
                {
                    $r2_2[$k]['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                }
            }

            $banner = array();
            $r5 = BannerModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'type'=>4])->order('sort','desc')->select()->toArray();
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
            
            $is_coupons = false;
            $isOpenCoupon = false;
            $Plugin = new Plugin();
            $Plugin_arr1 = $Plugin->front_Plugin($store_id);
            foreach ($Plugin_arr1 as $k => $v)
            {
                if ($k == 'CouponPublicMethod' && $v == 1)
                {
                    $isOpenCoupon = true;
                    bind('coupon','app\common\Plugin\CouponPublicMethod');
                    $is_open_coupon = app('coupon')->Get_store_coupon_status($store_id,$shop_id);

                    if($is_open_coupon == 1)
                    {
                        $is_coupons = true;
                    }
                }
            }

            $data = array('mch_address'=>$mchAddress,'shop_name' => $shop_name, 'shop_logo' => $shop_logo, 'poster_img' => $poster_img, 'Head_img' => $head_img,'shop_information'=>$shop_information, 'roomid' => $roomid,'is_open' => $is_open, 'collection_num' => $collection_num, 'collection_status' => $collection_status, 'quantity_on_sale' => $quantity_on_sale, 'quantity_sold' => $quantity_sold,'recommend_list'=>$recommend_list,'total'=>$total,'class_list'=>$class_list,'list'=>$r2_2,'banner'=>$banner,'is_coupons'=>$is_coupons,'isOpenCoupon'=>$isOpenCoupon);
            $message = Lang('Success');
            return output(200,$message, $data);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺ID错误！参数:'.json_encode($data0);
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(115,$message);
        }
    }

    // 店铺点击收藏按钮
    public function collectionShop()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言

        $shop_id = addslashes(trim(Request::param('shopId'))); // 店铺ID
        Db::startTrans();
        $thme = date("Y-m-d H:i:s");
        $user_id = '';
        if (!empty($access_id))
        {
            $r_user = UserModel::where('access_id', $access_id)->field('user_id')->select()->toArray();
            if ($r_user)
            {
                $user_id = $r_user[0]['user_id'];
            }
        }

        $r0 = UserCollectionModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'mch_id'=>$shop_id])->select()->toArray();
        if ($r0)
        {
            $sql1 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'user_id'=>$user_id);
            $r1 = Db::table('lkt_user_collection')->where($sql1)->delete();
            if ($r1 > 0)
            {
                $sql2_where = array('store_id'=>$store_id,'id'=>$shop_id);
                $sql2_update = array('collection_num'=>Db::raw('collection_num-1'));
                $r2 = Db::name('mch')->where($sql2_where)->update($sql2_update);
                if($r2 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '取消收藏失败！参数:'.json_encode($sql2_where);
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('Busy network');
                    return output(105,$message);
                }

                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '取消收藏成功！参数:'.json_encode($sql1);
                $this->Log($Log_content);
                Db::commit();
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '取消收藏失败！参数:'.json_encode($sql1);
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('Busy network');
                return output(105,$message);
            }
        }
        else
        {
            // 在收藏表里添加一条数据
            $sql1 = array('store_id'=>$store_id,'user_id'=>$user_id,'mch_id'=>$shop_id,'add_time'=>$thme);
            $r1 = Db::name('user_collection')->insert($sql1);
            if ($r1 > 0)
            {
                $sql2_where = array('store_id'=>$store_id,'id'=>$shop_id);
                $sql2_update = array('collection_num'=>Db::raw('collection_num+1'));
                $r2 = Db::name('mch')->where($sql2_where)->update($sql2_update);
                if($r2 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加收藏，修改店铺信息失败！参数:'.json_encode($sql2_where);
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('Busy network');
                    return output(105,$message);
                }

                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加收藏成功！参数:'.json_encode($sql1);
                $this->Log($Log_content);
                Db::commit();
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '添加收藏失败！参数:'.json_encode($sql1);
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('Busy network');
                return output(105,$message);
            }
        }
    }

    // 获取店铺分类
    public function mchClass()
    {
        $store_id = addslashes(trim(Request::param('store_id')));

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

    // 我的店铺
    public function index()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言

        $user_id = '';
        if (!empty($access_id))
        {
            $r_user = UserModel::where('access_id', $access_id)->field('user_id,zhanghao')->select()->toArray();
            if ($r_user)
            {
                $user_id = $r_user[0]['user_id'];
            }
        }
        $token = '';
        $review_result = '';
        // 根据商城ID、用户ID、店铺审核状态通过，查询是否有店铺
        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->select()->toArray();
        if ($r0)
        {
            $status = 2; // 审核通过
            $review_result = $r0[0]['review_result'];
            
            $role_id = 0;
            
            $token = Tools::getToken($store_id,8);
            $mchInfo = $r0[0];
            $exp_time = 7200;
            cache($token, $mchInfo, $exp_time);//添加新token数据
            cache($token.'_7',$mchInfo['id'],$exp_time);
            cache($token.'_roleId', $role_id, $exp_time);
            cache($token.'_uid', $user_id, $exp_time);
        }
        else
        {
            $r1 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->limit(1)->order('add_time','desc')->select()->toArray();
            if ($r1)
            {
                if ($r1[0]['review_status'] == 0)
                {
                    $status = 1; // 审核中
                }
                else
                {
                    $review_result = $r1[0]['review_result'];
                    $status = 3; // 审核不通过
                }
            }
            else
            {
                $status = 0; // 没有申请
            }
        }
        $data = array('status'=>$status,'review_result' => $review_result,'token' => $token);
        $message = Lang('Success');
        return output(200,$message, $data);
    }
    
    // 验证店铺名称
    public function verifyStoreName()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言

        $name = addslashes(trim(Request::param('name'))); // 店铺名称

        $user_id = '';
        if (!empty($access_id))
        {
            $r_user = UserModel::where('access_id', $access_id)->field('user_id')->select()->toArray();
            if ($r_user)
            {
                $user_id = $r_user[0]['user_id'];
            }
        }

        require('../app/common/shop_name.php');
        foreach ($shop_name as $key => $val)
        {
            if (strstr($name, $val) !== false)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '店铺名称不合法！';
                $this->Log($Log_content);
                $message = Lang('mch.0');
                return output(225, $message);
            }
        }

        $r = MchModel::where(['store_id'=>$store_id,'name'=>$name,'recovery'=>0])->where('user_id','<>',$user_id)->field('id')->select()->toArray();
        if ($r)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店铺名称已存在！';
            $this->Log($Log_content);
            $message = Lang('mch.1');
            return output(223, $message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '验证店铺名称成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200, $message);
        }
    }

    // 入驻协议
    public function agreement()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言

        $name = '申请入驻协议';
        $agreement = '';
        $r = AgreementModel::where(['store_id'=>$store_id,'type'=>1])->field('name,content')->select()->toArray();
        if ($r)
        {
            $name = $r[0]['name'];
            $agreement = $r[0]['content'];
        }
        $data = array('agreement' => $agreement, 'name' => $name);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 申请开通店铺
    public function apply()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言

        $name = addslashes(trim(Request::param('name'))); // 店铺名称
        $shop_information = addslashes(trim(Request::param('shop_information'))); // 店铺信息
        $cid = trim(Request::param('cid')); // 店铺分类
        $shop_range = addslashes(trim(Request::param('shop_range'))); // 经营范围
        $realname = addslashes(trim(Request::param('realname'))); // 真实姓名
        $ID_number = addslashes(trim(Request::param('ID_number'))); // 身份证号码
        $tel = addslashes(trim(Request::param('tel'))); // 联系电话
        $city_all = addslashes(trim(Request::param('city_all'))); // 联系地址
        $address = addslashes(trim(Request::param('address'))); // 联系地址
        $shop_nature = addslashes(trim(Request::param('shop_nature'))); // 店铺性质
        $imgUrls = trim(Request::param('imgUrls')); // 身份证证件照

        $time = date("Y-m-d H:i:s");

        $user_id = '';
        if (!empty($access_id))
        {
            $r_user = UserModel::where('access_id', $access_id)->field('user_id')->select()->toArray();
            if ($r_user)
            {
                $user_id = $r_user[0]['user_id'];
            }
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

        $r = MchModel::where(['store_id'=>$store_id,'name'=>$name])->where('user_id','<>',$user_id)->field('id')->select()->toArray();
        if ($r)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '申请开通店铺时，店铺名称已存在！';
            $this->Log($Log_content);
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
            $this->Log($Log_content);
            $message = Lang('mch.2');
            return output(223,$message);
        }

        if (preg_match("/^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$/", $tel))
        {
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '申请开通店铺时，手机号码有误！';
            $this->Log($Log_content);
            $message = Lang('mch.3');
            return output(117, $message);
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
        $address_xx = $sheng.$shi.$xian.$address;

        $Longitude_and_latitude = Tools::get_Longitude_and_latitude( $store_id,$address_xx);
        $longitude = $Longitude_and_latitude['longitude'];
        $latitude = $Longitude_and_latitude['latitude'];

        $id = 0;
        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id,business_license')->select()->toArray();
        if ($r0)
        {
            $id = $r0[0]['id'];
        }

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
    public function continueApply()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言
        $shop_id = addslashes(trim(Request::param('shop_id'))); // 店铺ID
        $user_id = '';
        if (!empty($access_id))
        {
            $r_user = UserModel::where('access_id', $access_id)->field('user_id')->select()->toArray();
            if ($r_user)
            {
                $user_id = $r_user[0]['user_id'];
            }
        }
        
        $business_license = "";
        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->limit('1')->order('add_time','desc')->select()->toArray();
        if ($r0)
        {
            $r0[0]['logo'] = ServerPath::getimgpath($r0[0]['logo'], $store_id); //图片
            $business_license_list = explode(',',$r0[0]['business_license']);
            foreach ($business_license_list as $k => $v)
            {
                $business_license .= ServerPath::getimgpath($v, $store_id) . ','; //图片
            }

            $business_license = trim($business_license,',');
            $imgUrls = $business_license; //图片
            $r0[0]['business_license'] = $business_license; //图片
            $r0[0]['id_number'] = $r0[0]['ID_number'];
            $r0[0]['account_money'] = round($r0[0]['account_money'],2);
            $r0[0]['cashable_money'] = round($r0[0]['cashable_money'],2);

            $data = array('list'=>$r0[0],'imgUrls'=>$imgUrls);
            $message = Lang('Success');
            return output(200, $message,$data);
        }
        else
        {
            $message = Lang('Illegal invasion');
            return output(115, $message);
        }
    }

    // // 获取地址（前端没有调用）
    // public function mchAddress()
    // {
    //     $GroupID = addslashes(trim(Request::param('GroupID')));

    //     $r = AdminCgGroupModel::where('district_pid',$GroupID)->select()->toArray();

    //     $message = Lang('Success');
    //     return output(200, $message,$r);
    // }

    public function get_address($sheng)
    {
        $r = AdminCgGroupModel::where('district_name',$sheng)->field('id')->select()->toArray();
        $GroupID = $r[0]['id'];

        return $GroupID;
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

    //获取门店列表
    public function seeMyStore()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $shop_id = trim(Request::param('shop_id'));//店铺id

        $list = array();
        $res = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->order('is_default','desc')->order('add_date','desc')->select()->toArray();
        if($res)
        {
            $list = $res;
        }
        $message = Lang('Success');
        return output(200, $message,array('list'=>$list));
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app_pc/mch.log",$Log_content);
        return;
    }
}
