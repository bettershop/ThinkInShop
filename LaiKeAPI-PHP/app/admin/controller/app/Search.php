<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Log;
use think\facade\Request;
use app\common\ServerPath;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Plugin\Plugin;

use app\admin\model\MchModel;
use app\admin\model\ProductClassModel;
use app\admin\model\ProductListModel;
use app\admin\model\ProLabelModel;
use app\admin\model\BrandClassModel;
use app\admin\model\HotkeywordsModel;

class Search 
{
    // 全部分类
    public function index()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(Request::param('language')); // 语言

        $id = trim(Request::param('cid')); // 分类ID
        $keyword = trim(Request::param('keyword')); // 关键词

        $lang_code = Tools::get_lang($language);

        $sid = '';
        $r_0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$id,'examine'=>1,'lang_code'=>$lang_code,'notset'=>0])->field('sid')->select()->toArray();
        if($r_0)
        {
            $sid = (string)$r_0[0]['sid'];
        }

        $cate_name = Lang('Classify All');
        $allList = array('cate_id'=>'','cate_name'=>$cate_name,'cimgurl'=>'','ishaveChild'=>true,'children'=>array());
        $children = array();

        // 查询一级分类
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$id,'examine'=>1,'lang_code'=>$lang_code,'notset'=>0])->field('cid,pname,img,bg')->order('sort','desc')->select()->toArray();
        if($r0)
        {
            foreach ($r0 as $k0 => $v0)
            {
                $children0 = array();
                $cimgurl = '';
                $cid = $v0['cid'];
                if ($v0['bg'])
                {
                    $cimgurl = ServerPath::getimgpath($v0['bg'], $store_id);
                }
                // 根据上级分类ID，查询下级分类
                $r1 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$cid,'examine'=>1,'lang_code'=>$lang_code,'notset'=>0])->field('cid,pname,img')->order('sort','desc')->select()->toArray();
                if ($r1)
                {
                    foreach ($r1 as $k1 => $v1)
                    {
                        $imgurl = '';
                        if ($v1['img'])
                        {
                            $imgurl = ServerPath::getimgpath($v1['img'], $store_id);
                        }
                        $children0[] = array('child_id' => $v1['cid'], 'name' => $v1['pname'], 'picture' => $imgurl);
                    }
                }

                $children[] = array('cate_id'=>$v0['cid'],'cate_name'=>$v0['pname'],'cimgurl'=>$cimgurl,'ishaveChild'=>true,'children'=>$children0);
            }
        }
        $allList['children'] = $children;

        $data = array('allList'=>$allList,'List'=>$children,'sid'=>$sid);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 全部分类进入商品列表
    public function listdetail()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $language = addslashes(Request::param('language')); // 语言
        $shop_id = trim(Request::param('shop_id')); // 店铺id
        $id = trim(Request::param('cid')); // 分类ID
        $paegr = trim(Request::param('page')); // 页面
        $pro = trim(Request::param('pro')); // 关键词
        $keyword = trim(Request::param('keyword')); // 关键词
        $query_criteria = trim(Request::param('query_criteria')); // 查询条件
        $sort_criteria = trim(Request::param('sort_criteria')); // 排序条件
        $sort = addslashes(trim(Request::param('sort'))) ? addslashes(trim(Request::param('sort'))) : 'asc'; // 排序条件

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $lang_code = Tools::get_lang($language);

        // 获取会员
        $Member_discount = array('store_id'=>$store_id,'access_id'=>$access_id);
        $grade_list= PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade'];
        $grade_rate = $grade_list['grade_rate'];

        $brand_id = '';
        if (!$paegr)
        {
            $paegr = 1;
        }
        $start = ($paegr - 1) * 10;
        $end = 10;

        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));

        $pname = '';
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$id,'examine'=>1,'lang_code'=>$lang_code,'notset'=>0])->field('pname')->select()->toArray();
        if ($r0)
        {
            $pname = $r0[0]['pname'];
        }
        
        $con = " a.store_id = '$store_id' and a.commodity_type in (0,1) and a.recycle = 0 and a.active = 1 and a.mch_status = 2 and a.product_class like '%-$id-%' and c.recycle = 0 and a.is_presell = 0 and a.mch_id != 0 and a.lang_code = '$lang_code' $status ";
        if($shop_id != '')
        {
            $con .= " and a.mch_id = '$shop_id' ";
        }
        if($keyword != '')
        {
            $keyword_0 = Tools::FuzzyQueryConcatenation($keyword);
            $con .= " and (a.product_title like $keyword_0 or a.keyword like $keyword_0) ";
        }
        if($query_criteria)
        {
            $query_criteria = json_decode($query_criteria, true);
            if($query_criteria['brand_id'] != '')
            {
                $brand_id = $query_criteria['brand_id'];
                $con .= " and a.brand_id = '$brand_id' ";
            }
            if($query_criteria['min_price'] != '')
            {
                $min_price = $query_criteria['min_price'];
                $con .= " and c.price >= '$min_price' ";
            }
            if($query_criteria['max_price'] != '')
            {
                $max_price = $query_criteria['max_price'];
                $con .= " and c.price <= '$max_price' ";
            }
        }
        $condition = " a.class_sort desc,a.upper_shelf_time desc ";
        if($sort_criteria == 'volume')
        {
            $condition = " (a.volume + a.real_volume) " . $sort;
        }
        else if($sort_criteria == 'price')
        {
            $condition = " price " . $sort;
        }
        else if($sort_criteria == 'comment_num')
        {
            $condition = " a.comment_num desc" ;
        }
        else
        {
            $condition = " a.class_sort desc,a.upper_shelf_time desc ";
        }

        $product = array();
        $str = "a.id,a.commodity_type,a.product_title,a.subtitle,a.status,a.imgurl,(a.volume+a.real_volume) as volume,a.product_class,a.brand_id,a.freight,a.mch_id,a.s_type,a.keyword,a.num,c.id as cid,c.img,c.pid,min(c.price) over (partition by c.pid) as price,c.yprice,c.attribute,m.id as mchId,m.logo,m.name as mch_name,m.is_open,row_number () over (partition by c.pid) as top,a.is_appointment,a.write_off_settings,a.write_off_mch_ids,a.recycle";
        $sql = "select tt.* from (select $str from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where $con order by $condition) as tt where tt.top<2 LIMIT $start,$end ";
        $r = Db::query($sql);
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id);/* end 保存*/
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);/* end 保存*/
                
                if($v['volume'] < 0)
                {
                    $v['volume'] = 0;
                }
                
                // // 货币转换
                // $v['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['price']));
                // $v['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['yprice']));
                $v['price'] = round($v['price'],2);
                $v['yprice'] = round($v['yprice'],2);
                $v['price_yh'] = $v['price'];
                $v['vip_yprice'] = $v['price'];
                $v['vip_price'] = round($v['price'] * $grade_rate,2);
                $v['sizeid'] = $v['cid'];
                $v['stockNum'] = $v['num'];

                $s_type = explode(',', trim($v['s_type'],','));
                $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                $v['s_type_list'] = $s_type_list;

                $pid = $v['id'];
                $v['payPeople'] = 0;

                $sql_3 = "select count(DISTINCT r_sNo) as total from lkt_order_details where r_status != 0 and p_id = '$pid' ";
                $r_3 = Db::query($sql_3);
                if($r_3)
                {
                    $v['payPeople'] = $r_3[0]['total'];
                }

                $mch_id = isset($v['mchId']) ? (int)$v['mchId'] : (isset($v['mch_id']) ? (int)$v['mch_id'] : 0);
                $marketingParams = $this->getTopMarketingParams((int)$store_id, (int)$pid, $language ? $language : 'zh_CN', $mch_id);
                if($marketingParams)
                {
                    $v['marketingParams'] = $marketingParams;
                }

                if($v['write_off_settings'] == 1)
                { // 核销设置 1.线下核销 2.无需核销
                    $v['is_appointment'] = 2; // 2.不能加入购物车
                    $v['isAddCar'] = 2; // 2.不能加入购物车
                }
                else
                {
                    $v['is_appointment'] = 1; // 1.能加入购物车
                    $v['isAddCar'] = 1; // 1.能加入购物车
                }

                $product[] = $v;
            }
        }

        $brand_id_list = array();
        $sqlb0 = "select tt.* from (select a.brand_id,c.pid,min(c.price) over (partition by c.pid) as price,row_number () over (partition by c.pid) as top from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where $con) as tt where tt.top<2 ";
        $rb0 = Db::query($sqlb0);
        if ($rb0)
        {
            foreach ($rb0 as $k => $v)
            {
                if(!in_array($v['brand_id'],$brand_id_list))
                {
                    $brand_id_list[] = $v['brand_id'];
                }
            }
        }

        $brand_class_list = array();
        $brand_id = implode(',',$brand_id_list);

        $r_brand_class = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0,'examine'=>1,'lang_code'=>$lang_code])->whereIn('brand_id',$brand_id)->field('brand_id,brand_name')->select()->toArray();
        if($r_brand_class)
        {
            $brand_class_list = $r_brand_class;
        }

        $data = array('pro' => $product, 'pname' => $pname, 'grade' => $grade,'brand_class_list'=>$brand_class_list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }
    
    // 热门搜索
    public function hot_search()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $language = addslashes(Request::param('language')); // 语言
        $type = trim(Request::param('type')); // 类型 0.商品 1.店铺

        $lang_code = Tools::get_lang($language);

        $list = array();
        $is_open = 0;

        $r = HotkeywordsModel::where('store_id',$store_id)->select()->toArray();
        if ($r)
        {
            $is_open = $r[0]['is_open']; // 是否开启
            $num = $r[0]['num']; // 关键词上限
            $keyword = $r[0]['keyword']; // 关键词
            $mch_keyword = $r[0]['mch_keyword']; // 关键词
        }

        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>2));

        if ($type == '0')
        {
            if ($is_open == 1)
            {
                $keyword = explode(',', $keyword);
                if (count($keyword) < $num)
                {
                    $num = count($keyword);
                }
                for ($i = 0; $i < $num; $i++)
                {
                    $list[] = $keyword[$i];
                }
            }
            else
            {
                $r0 = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0,'active'=>1,'lang_code'=>$lang_code])->where('keyword','<>','')->where('num','>',0)->whereIn('status',$status)->limit(6)->order('search_num','desc')->order('add_date','desc')->field('keyword')->select()->toArray();
                if ($r0)
                {
                    foreach ($r0 as $k => $v)
                    {
                        if ($v['keyword'])
                        {
                            $list[] = $v['keyword'];
                        }
                    }
                }
                $list = array_unique($list);
            }
        }
        else
        {
            if ($is_open == 1)
            {
                if ($mch_keyword != '')
                {
                    $mch_keyword = explode(',', $mch_keyword);
                    for ($i = 0; $i < $num; $i++)
                    {
                        $list[] = $mch_keyword[$i];
                    }
                }
            }
            else
            {
                $r0 = MchModel::where(['store_id'=>$store_id,'recovery'=>0,'review_status'=>1])->limit(6)->order('collection_num','desc')->field('name')->select()->toArray();
                if ($r0)
                {
                    foreach ($r0 as $k => $v)
                    {
                        $list[] = $v['name'];
                    }
                }
            }
        }

        $data = array('list' => $list, 'is_open' => $is_open);

        $message = Lang('Success');
        return output(200,$message, $data);
    }
    
    // 输入一部分，返回字符串全部
    public function input_search()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $language = addslashes(Request::param('language')); // 语言
        $type = trim(Request::param('type')); // 类型 0.商品 1.店铺
        $keyword = trim(Request::param('keyword')); // 关键词

        $lang_code = Tools::get_lang($language);

        $keyword_0 = Tools::FuzzyQueryConcatenation($keyword);
        $list = array();

        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));

        if ($type == '0')
        {
            $sql1 = "select a.product_title from lkt_product_list as a left join lkt_mch as m on a.mch_id = m.id where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.recycle = 0 and a.active = 1 and a.product_title LIKE $keyword_0 and a.mch_status = 2  and a.is_presell = 0 and a.mch_id != 0 and a.lang_code = '$lang_code' $status order by a.search_num desc,a.add_date desc";
            $r1 = Db::query($sql1);
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    $list[] = $v['product_title'];
                }
            }

            $sql2 = "select a.keyword from lkt_product_list as a left join lkt_mch as m on a.mch_id = m.id where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.recycle = 0 and a.active = 1 and a.keyword LIKE $keyword_0 and a.mch_status = 2  and a.is_presell = 0 and a.mch_id != 0 and a.lang_code = '$lang_code' $status order by a.search_num desc,a.add_date desc";
            $r2 = Db::query($sql2);
            if ($r2)
            {
                foreach ($r2 as $k => $v)
                {
                    $list[] = $v['keyword'];
                }
            }

            $sql3 = "select pname from lkt_product_class where store_id = '$store_id' and recycle = 0 and lang_code = '$lang_code' and pname like $keyword_0 order by sort desc ";
            $r3 = Db::query($sql3);
            if ($r3)
            {
                foreach ($r3 as $k => $v)
                {
                    $list[] = $v['pname'];
                }
            }

            $list = array_unique($list);
        }
        else
        {
            $sql0 = "select name from lkt_mch where store_id = '$store_id' and recovery = 0 and review_status = 1 and name like $keyword_0 order by collection_num desc ";
            $r0 = Db::query($sql0);
            if ($r0)
            {
                foreach ($r0 as $k => $v)
                {
                    $list[] = $v['name'];
                }
            }
        }

        $message = Lang('Success');
        return output(200,$message, $list);
    }

    // 搜索
    public function search()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $language = addslashes(Request::param('language')); // 语言
        $type = trim(Request::param('type')); // 类型 0.全部 1.热销 2.店铺
        $keyword = trim(Request::param('keyword')); // 关键词
        $query_criteria = Request::param('query_criteria'); // 查询条件
        $sort_criteria = trim(Request::param('sort_criteria')); // 排序条件
        $num = trim(Request::param('num')); // 次数
        $sort = addslashes(trim(Request::param('sort'))) ? addslashes(trim(Request::param('sort'))) : 'asc'; // 排序条件

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $lang_code = Tools::get_lang($language);

        $keyword_0 = Tools::FuzzyQueryConcatenation($keyword);

        $list = array();
        $id_list = array();
        $start = 10 * ($num - 1);
        $end = 10;

        $brand_id = '';
        if ($type == '0' || $type == '1')
        {
            $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));

            $Member_discount = array('store_id'=>$store_id,'access_id'=>$access_id);
            $grade_list = PC_Tools::Member_discount($Member_discount);
            $grade = $grade_list['grade'];
            $grade_rate = $grade_list['grade_rate'];
            
            $str = "a.id,a.commodity_type,a.product_title,a.subtitle,a.status,a.imgurl,a.volume,a.product_class,a.brand_id,a.mch_id,a.s_type,a.keyword,a.num,a.gongyingshang,c.id as cid,c.img,c.pid,min(c.price) over (partition by c.pid) as price,c.yprice,c.costprice,c.attribute,m.id as mchId,m.logo,m.name as mch_name,m.is_open,row_number () over (partition by c.pid) as top,a.is_appointment,a.write_off_settings,a.write_off_mch_ids,a.recycle";

            //查出所有产品分类
            $res = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'examine'=>1,'lang_code'=>$lang_code,'notset'=>0])->field('pname,cid')->select()->toArray();
            foreach ($res as $key => $value)
            {
                $pname = $value['pname'];
                $types[$pname] = $value['cid'];
            }
            if (array_key_exists($keyword, $types))
            {
                $cid = $types[$keyword];

                $sqlb = "select tt.* from (select $str from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.recycle = 0 and c.recycle = 0 and a.active = 1 and a.product_class like '%-$cid-%' and a.mch_status = 2  and a.is_presell = 0 and a.mch_id != 0 and a.lang_code = '$lang_code' $status order by a.sort desc,a.search_num desc,a.add_date desc) as tt where tt.top<2  LIMIT $start,$end";
                $data = Db::query($sqlb);
                if ($data)
                {
                    foreach ($data as $k => $v)
                    {
                        $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id);
                        $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                        if($v['volume'] < 0)
                        {
                            $v['volume'] = 0;
                        }

                        // // 货币转换
                        // $v['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['price']));
                        // $v['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['yprice']));
                        // $v['costprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['costprice']));
                        $v['price'] = round($v['price'],2);
                        $v['price_yh'] = $v['price'];
                        if($v['gongyingshang'] != '' && $v['gongyingshang'] != 0)
                        { // 供应商商品
                            $v['vip_yprice'] = $v['costprice'];
                            $v['vip_price'] = round($v['price'] * $grade_rate,2);
                        }
                        else
                        {
                            $v['vip_yprice'] = $v['price'];
                            $v['vip_price'] = round($v['price'] * $grade_rate,2);
                        }
                        
                        $v['sizeid'] = $v['cid'];
                        $v['stockNum'] = $v['num'];
        
                        $s_type = explode(',', trim($v['s_type'],','));
                        $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                        $v['s_type_list'] = $s_type_list;

                        $mch_id = isset($v['mchId']) ? (int)$v['mchId'] : (isset($v['mch_id']) ? (int)$v['mch_id'] : 0);
                        $marketingParams = $this->getTopMarketingParams((int)$store_id, (int)$v['id'], $language ? $language : 'zh_CN', $mch_id);
                        if($marketingParams)
                        {
                            $v['marketingParams'] = $marketingParams;
                        }
                        
                        $id_list[] = $v['id'];
                        $list[] = $v;
                    }
                }
            }
            $keyword = addslashes($keyword);
            
            $con = " a.store_id = '$store_id' and a.commodity_type in (0,1) and a.recycle = 0 and c.recycle = 0 and a.active = 1 and (a.product_title like $keyword_0 or a.keyword like $keyword_0) and a.mch_status = 2  and a.is_presell = 0 and a.mch_id != 0 and a.lang_code = '$lang_code' $status ";
            if($id_list != array())
            {
                $id_str = implode(',',$id_list);
                $con .= " and a.id not in ($id_str) ";
            }

            if ($type == '1')
            {
                $r_sp_type = ProLabelModel::where(['store_id'=>$store_id,'name'=>'热销'])->field('id')->select()->toArray();
                $label_id = $r_sp_type[0]['id'];

                $con .= " and a.s_type like '%,$label_id,%' ";
            }
            if($query_criteria)
            {
                $query_criteria = json_decode($query_criteria, true);
                if($query_criteria['brand_id'] != '')
                {
                    $brand_id = $query_criteria['brand_id'];
                    $con .= " and a.brand_id = '$brand_id' ";
                }
                if($query_criteria['min_price'] != '')
                {
                    $min_price = $query_criteria['min_price'];
                    $con .= " and c.price >= '$min_price' ";
                }
                if($query_criteria['max_price'] != '')
                {
                    $max_price = $query_criteria['max_price'];
                    $con .= " and c.price <= '$max_price' ";
                }
            }
            if($sort_criteria == 'volume')
            {
                $condition = " a.volume " . $sort;
            }
            else if($sort_criteria == 'price')
            {
                $condition = " c.price " . $sort;
            }
            else if($sort_criteria == 'comment_num')
            {
                $condition = " a.comment_num desc" ;
            }
            else
            {
                $condition = " a.sort desc,a.search_num desc,a.add_date desc ";
            }

            $sqlb = "select tt.* from (select $str from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where $con order by $condition) as tt where tt.top<2 LIMIT $start,$end";
            $data = Db::query($sqlb);
            if ($data)
            {
                foreach ($data as $k => $v)
                {
                    $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id);
                    $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);

                    // // 货币转换
                    // $v['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['price']));
                    // $v['yprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['yprice']));
                    // $v['costprice'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['costprice']));
                    $v['price'] = round($v['price'],2);
                    $v['vip_yprice'] = $v['yprice'];
                    $v['vip_price'] = sprintf("%.2f",round($v['price'] * $grade_rate,2));

                    $v['grade'] = $grade;
                    if($v['volume'] < 0)
                    {
                        $v['volume'] = 0;
                    }

                    $v['price_yh'] = $v['price'];
                    if($v['gongyingshang'] != '' && $v['gongyingshang'] != 0)
                    { // 供应商商品
                        $v['vip_yprice'] = $v['costprice'];
                        $v['vip_price'] = round($v['price'] * $grade_rate,2);
                    }
                    else
                    {
                        $v['vip_yprice'] = $v['yprice'];
                        $v['vip_price'] = round($v['price'] * $grade_rate,2);
                    }
                    $v['sizeid'] = $v['cid'];
                    $v['stockNum'] = $v['num'];
    
                    $s_type = explode(',', trim($v['s_type'],','));
                    $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                    $v['s_type_list'] = $s_type_list;

                    $pid = $v['id'];
                    $sql = "update lkt_product_list set search_num = search_num+1 where id = '$pid'";
                    Db::execute($sql);

                    if($v['write_off_settings'] == 1)
                    { // 核销设置 1.线下核销 2.无需核销
                        $v['is_appointment'] = 2; // 2.不能加入购物车
                    }
                    else
                    {
                        $v['is_appointment'] = 1; // 1.能加入购物车
                    }

                    $mch_id = isset($v['mchId']) ? (int)$v['mchId'] : (isset($v['mch_id']) ? (int)$v['mch_id'] : 0);
                    $marketingParams = $this->getTopMarketingParams((int)$store_id, (int)$pid, $language ? $language : 'zh_CN', $mch_id);
                    if($marketingParams)
                    {
                        $v['marketingParams'] = $marketingParams;
                    }

                    $list[] = $v;
                }
            }

            $sqlb0 = "select tt.* from (select a.brand_id,c.pid,min(c.price) over (partition by c.pid) as price,row_number () over (partition by c.pid) as top from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid left join lkt_mch as m on a.mch_id = m.id where $con) as tt where tt.top<2 ";
            $rb0 = Db::query($sqlb0);
            if ($rb0)
            {
                foreach ($rb0 as $k => $v)
                {
                    $brand_id .= $v['brand_id'] . ',' ;
                }
            }
        }
        else if ($type == '2')
        {
            $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>2));
            
            $str = "id,store_id,user_id,name,shop_information,shop_range,realname,ID_number as iD_number,tel,sheng,shi,xian,address,logo,shop_nature,business_license,add_time,review_status,integral_money,account_money,collection_num,is_open,is_lock,business_hours,roomid,old_roomid,longitude,latitude,cashable_money,recovery,is_invoice,cid,poster_img,head_img,pc_mch_path,last_login_time";
            
            $sql0 = "select $str from lkt_mch where store_id = '$store_id' and recovery = 0 and review_status = 1 and name like $keyword_0 order by collection_num desc ";
            $r0 = Db::query($sql0);
            if ($r0)
            {
                foreach ($r0 as $k => $v)
                {
                    $shop_id = $v['id']; // 店主ID
                    // $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id); // 店铺logo
                    $v['logo'] = ServerPath::getimgpath($v['head_img'], $store_id); // 店铺logo

                    $mch_list = PC_Tools::StoreData($store_id,$shop_id,$status);
                    $v['quantity_on_sale'] = $mch_list['quantity_on_sale'];
                    $v['quantity_sold'] = $mch_list['quantity_sold'];
                    $v['follow'] = $mch_list['collection_num'];
                    // // 货币转换
                    // $v['account_money'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['account_money']));
                    $list[] = $v;
                }
            }
        }
        $brand_class_list = array();
        $brand_id = trim($brand_id,',');

        $r_brand_class = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0,'examine'=>1,'lang_code'=>$lang_code])->whereIn('brand_id',$brand_id)->field('brand_id,brand_name')->select()->toArray();
        if($r_brand_class)
        {
            $brand_class_list = $r_brand_class;
        }

        $mch_status = false;
        $Plugin_arr = new Plugin();
        $Plugin_arr1 = $Plugin_arr->front_Plugin($store_id); 
        foreach ($Plugin_arr1 as $k => $v)
        {
            if ($k == 'MchPublicMethod' && $v == 1)
            {
                $mch_status = true;
            }
        }
        $data = array('list'=>$list,'brand_class_list'=>$brand_class_list,'mch_status'=>$mch_status);

        $message = Lang('Success');
        return output(200,$message, $data);
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
}
