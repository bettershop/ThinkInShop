<?php
namespace app\admin\controller\app;

use think\facade\Db;
use think\facade\Request;
use app\common;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Tools;

use app\admin\model\DistributionGradeModel;
use app\admin\model\UserModel;
use app\admin\model\ProductClassModel;
/**
 * 功能：移动端分销商品类
 * 修改人：PJY
 */
class Commproduct
{
    //礼包列表
    public function getstart()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::post('access_id'));
        $product_title = trim(Request::post('product_title'));// 商品名
        $distributor_id = trim(Request::post('distributor_id'));// 查询分销等级ID
        $pagesize = Request::post('pagesize'); // 每页显示多少条数据
        $page = Request::post('page'); // 页码
        $pagesize = $pagesize ? $pagesize : 10;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }
        // 查询出所有分销等级
        $level = DistributionGradeModel::where('store_id',$store_id)->order('sort','asc')->select()->toArray();
        foreach ($level as $k => $v)
        {
            // 循环分销等级
            $sets = json_decode($v['sets'],true);
            $level[$k]['levelname'] = $sets['s_dengjiname']; // 等级名称
        }
        
        // 获取会员
        $direct_m_type = 1;
        $direct_m = 0;
        if (!empty($access_id))
        {
            $sql_user1 = UserModel::where('access_id',$access_id)->field('user_id')->select()->toArray();
            if ($sql_user1)
            {
                $user_id = $sql_user1[0]['user_id'];
                
                $sql0_0 = "select a.level,b.sets from lkt_user_distribution as a left join lkt_distribution_grade as b on a.level = b.id where a.store_id = '$store_id' and a.user_id = '$user_id' and a.level > 0";
                $r0_0 = Db::query($sql0_0);

                if($r0_0)
                {
                    $sets = json_decode($r0_0[0]['sets'],true);
                    $direct_m_type = $sets['direct_m_type'];
                    $direct_m = $sets['direct_m'];
                }
                else
                {
                    $r0_0 = DistributionGradeModel::where('store_id',$store_id)->field('sets')->order('id','desc')->limit(0,1)->select()->toArray();
                    if($r0_0)
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
                if($r0_0)
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
            if($r0_0)
            {
                $sets = json_decode($r0_0[0]['sets'],true);
                $direct_m_type = $sets['direct_m_type'];
                $direct_m = $sets['direct_m'];
            }
        }

        $con = " d.store_id = '$store_id' and b.status = 2 and c.recycle = 0 and a.uplevel >0 and a.recycle = 0";
        if($distributor_id != '')
        {
            $con .= " and a.uplevel = '$distributor_id'";
        }
        if($product_title != '')
        {
            $product_title_0 = Tools::FuzzyQueryConcatenation($product_title);
            $con .= " and b.product_title like $product_title_0";
        }
        $sqll = "select tt.* from (select a.*,b.product_title,b.imgurl,c.price,row_number () over (PARTITION BY a.p_id) AS top from lkt_distribution_goods as a left join lkt_product_list as b on a.p_id = b.id left join lkt_configure as c on a.s_id = c.id left join lkt_distribution_grade as d on a.uplevel = d.id where $con) as tt where tt.top<2 order by tt.add_time limit $start,$pagesize";
        $rr = Db::query($sqll);
        if($rr)
        {
            foreach ($rr as $k => $v)
            {
                $rr[$k]['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id); // 商品图
                $price = $v['price'];
                if($direct_m_type == 0)
                {
                    $rr[$k]['fx_price'] = round($price * $direct_m * 0.01,2);
                }else{
                    $rr[$k]['fx_price'] = $direct_m;
                }
            }
        }
        else
        {
            $rr = array();
        }
        $message = Lang('Success');
        return output(200,$message,array('pro' => $rr,'level' => $level));     
    }

    //分销商品
    public function listdetail()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::post('access_id'));

        $product_title = trim(Request::post('product_title'));// 商品名
        $sort_key =  trim(Request::post('sort_key'));//排序字段
        $queue = trim(Request::post('queue'));//1降序2升序
        $c_id = trim(Request::post('cid'));//分类ID
        $pagesize = Request::post('pagesize'); // 每页显示多少条数据
        $page = Request::post('start'); // 页码
        $pagesize = $pagesize ? $pagesize : 10;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        $c = '';
        
        // 获取会员
        $direct_m_type = 1;
        $direct_m = 0;
        $discount = 1;//分销折扣
        if (!empty($access_id))
        {
            $sql_user1 = UserModel::where('access_id',$access_id)->field('user_id')->select()->toArray();
            if ($sql_user1)
            {
                $user_id = $sql_user1[0]['user_id'];
                
                $sql0_0 = "select a.level,b.sets,b.discount from lkt_user_distribution as a left join lkt_distribution_grade as b on a.level = b.id where a.store_id = '$store_id' and a.user_id = '$user_id' and a.level > 0";
                $r0_0 = Db::query($sql0_0);
                if($r0_0)
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
                    if($r0_0)
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
                if($r0_0)
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
            if($r0_0)
            {
                $sets = json_decode($r0_0[0]['sets'],true);
                $direct_m_type = $sets['direct_m_type'];
                $direct_m = $sets['direct_m'];
            }
        }
        $order = " a.add_time desc";
        if(!empty($sort_key))
        {
            if($queue == 1)
            {   
                if($sort_key == 'price')
                {
                    $order = " c.$sort_key desc";
                }
                else
                {
                    $order = " b.$sort_key desc";
                }
            }
            else
            {   
                if($sort_key == 'price')
                {
                    $order = " c.$sort_key asc";
                }
                else
                {
                    $order = " b.$sort_key asc";
                }
            }
        }
        $list = array();
        $con = " a.store_id = '$store_id' and b.status = 2 and c.recycle = 0 and a.uplevel = 0 and a.recycle = 0";
        if(!empty($product_title))
        {
            $product_title_0 = Tools::FuzzyQueryConcatenation($product_title);
            $con .= " and b.product_title like $product_title_0";
        }
        if(!empty($c_id))
        {
            $con .= " and b.product_class like '%-$c_id-%'";
        }
        //商品列表
        $sql = "select tt.* from (select a.*,b.product_title,b.imgurl,c.price,c.costprice,c.min_inventory,c.attribute,c.num,row_number () over (PARTITION BY a.p_id) AS top from lkt_distribution_goods as a left join lkt_product_list as b on a.p_id = b.id left join lkt_configure as c on a.s_id = c.id where $con order by $order) as tt where tt.top<2 limit $start,$pagesize";
        $res = Db::query($sql);
        if ($res)
        {
            foreach ($res as $k => $v)
            {
                $res[$k]['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id); // 商品图
                $price = $v['price'];
                if($direct_m_type == 0)
                {
                    $res[$k]['fx_price'] = round($price * $direct_m * 0.01,2);
                }else{
                    $res[$k]['fx_price'] = $direct_m;
                }
                $res[$k]['goodsPrice'] = (float)$v['price'];
                $res[$k]['price'] = round($v['price'] * $discount,2);
                $res[$k]['costprice'] = (float)$v['costprice'];
                $res[$k]['pv'] = (float)$v['pv'];
            }
            $message = Lang('Success');
            return output(200,$message,array('pro' => $res));
        }
        else
        {   
            $message = Lang('distribution.14');
            return output(ERROR_CODE_MYGDL,$message);
        }
    }

    //获取分类
    public function getclass()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::post('access_id'));
        $language = trim(Request::post('language'));


        $APP_INDEX_KEY = LAIKE_REDIS_PRE_KEY.__CLASS__.'_'.__METHOD__;
        $redis_data = cache($APP_INDEX_KEY);
        LaiKeLogUtils::log("common/a_redis.log",$redis_data);
        if($redis_data)
        {
            $message = Lang('Successs');
            return output(200,$message,json_decode($redis_data));
        }
        else
        {
            $product_class =array();
            //获取分类
            $sql1 = "select a.product_class from lkt_product_list as a right join lkt_distribution_goods as b on a.store_id = b.store_id and a.id = b.p_id left join lkt_configure as c on b.s_id = c.id where a.store_id = '$store_id' and a.status = 2 and c.recycle = 0 and b.recycle = 0 and b.uplevel = 0 order by b.add_time desc ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                foreach ($r1 as $k => $v) {
                    $v['product_class'] = ltrim($v['product_class'], "-"); // 去掉字符串前面的'-'
                    $v['product_class'] = substr($v['product_class'], 0, strpos($v['product_class'], '-')); // 截取字符串第一个'-'前面的内容
                    $product_classr[] = $v['product_class'];
                }
                $product_classr = array_unique($product_classr);
                sort($product_classr);
                foreach ($product_classr as $key => $value) 
                {
                       $product_class[$key]['id'] =  $value;
                       $res0 = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$value])->field('pname')->select()->toArray();
                       $product_class[$key]['name'] = $res0[0]['pname'];
                }
                cache($APP_INDEX_KEY,json_encode(array('product_class'=>$product_class)),120);
            }
            $message = Lang('Successs');
            return output(200,$message,array('product_class'=>$product_class));
        } 
    }
}

?>

