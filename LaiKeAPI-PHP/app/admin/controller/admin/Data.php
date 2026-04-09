<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;

use app\admin\model\UserModel;
use app\admin\model\ProductListModel;
use app\admin\model\ProductClassModel;

/**
 * 功能：数据管理
 * 修改人：PJY
 */
class Data extends BaseController
{   
    //新增用户折线图
    public function getAddUserInfo()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $startdate = addslashes(trim($this->request->param('startDate')));
        $enddate = addslashes(trim($this->request->param('endDate')));

        //没有时间参数，默认返回七天的数据
        if (empty($startdate) || empty($enddate))
        {
            $enddate = date("Y-m-d"). " 23:59:59";
            $startdate_1 = strtotime('-6 day');
            $startdate = date("Y-m-d", $startdate_1);

        }
        else
        {
            $days = $this->diffBetweenTwoDays($startdate, $enddate);
            if ($days > 31)
            {
                $enddate = $enddate;
                $startdate = date("Y-m-d", strtotime('-31 day'));
            }

        }
        //时间段内，每日日期获取函数
        $day_arr = $this->getDateFromRange($startdate, $enddate);

        //查询时间段内每天的新增----------微信会员数(所有小程序)
        $sql = "select ifnull(COUNT(*),0) as sum,DATE_FORMAT(Register_data,'%Y-%m-%d') as rdate from lkt_user where store_id = '$store_id' and source in (1,3,4,5) group by rdate having rdate between '{$startdate}' and '{$enddate}' order by rdate desc ";
        $res_wx = Db::query($sql);
        //给日期数组中没有新增人数的日期，设置新增人数为0
        $sum_arr_wx = [];
        foreach ($day_arr as $k => $v)
        {   
            $sum_arr_wx[$k]['rdate'] = $v;
            $sum_arr_wx[$k]['sum'] = 0;
        }
        foreach ($day_arr as $k => $v)
        {
            foreach ($res_wx as $k1 => $v1)
            {
                if ($v == $v1['rdate'])
                {
                    $sum_arr_wx[$k]['sum'] = intval($v1['sum']);
                }
            }
        }
        // $sum_arr_wx1 = json_encode($sum_arr_wx);

        //查询时间段内每天的新增----------app会员数
        $sql = "select ifnull(COUNT(*),0) as sum,DATE_FORMAT(Register_data,'%Y-%m-%d') as rdate from lkt_user where store_id = '$store_id' and source = 2 group by rdate having rdate between '{$startdate}' and '{$enddate}' order by rdate desc ";
        $res_app = Db::query($sql);


        //给日期数组中没有新增人数的日期，设置新增人数为0
        $sum_arr_app = [];
        foreach ($day_arr as $k => $v)
        {   
            $sum_arr_app[$k]['rdate'] = $v;
            $sum_arr_app[$k]['sum'] = 0;
        }
        foreach ($day_arr as $k => $v)
        {
            foreach ($res_app as $k1 => $v1)
            {
                if ($v == $v1['rdate'])
                {
                    $sum_arr_app[$k]['sum'] = intval($v1['sum']);
                }
            }
        }
        // $sum_arr_app1 = json_encode($sum_arr_app);

        //查询时间段内每天的新增----------pc会员数
        $sql = "select ifnull(COUNT(*),0) as sum,DATE_FORMAT(Register_data,'%Y-%m-%d') as rdate from lkt_user where store_id = '$store_id' and source = 6 group by rdate having rdate between '{$startdate}' and '{$enddate}' order by rdate desc ";
        $res_pc = Db::query($sql);


        //给日期数组中没有新增人数的日期，设置新增人数为0
        $sum_arr_pc = [];
        foreach ($day_arr as $k => $v)
        {   
            $sum_arr_pc[$k]['rdate'] = $v;
            $sum_arr_pc[$k]['sum'] = 0;
        }
        foreach ($day_arr as $k => $v)
        {
            foreach ($res_pc as $k1 => $v1)
            {
                if ($v == $v1['rdate'])
                {
                    $sum_arr_pc[$k]['sum'] = intval($v1['sum']);
                }
            }
        }
        // $sum_arr_pc1 = json_encode($sum_arr_pc);

        //查询时间段内每天的新增----------H5会员数
        $sql = "select ifnull(COUNT(*),0) as sum,DATE_FORMAT(Register_data,'%Y-%m-%d') as rdate from lkt_user where store_id = '$store_id' and source = 7 group by rdate having rdate between '{$startdate}' and '{$enddate}' order by rdate desc ";
        $res_h5 = Db::query($sql);


        //给日期数组中没有新增人数的日期，设置新增人数为0
        $sum_arr_h5 = [];
        foreach ($day_arr as $k => $v)
        {   
            $sum_arr_h5[$k]['rdate'] = $v;
            $sum_arr_h5[$k]['sum'] = 0;
        }
        foreach ($day_arr as $k => $v)
        {
            foreach ($res_h5 as $k1 => $v1)
            {
                if ($v == $v1['rdate'])
                {
                    $sum_arr_h5[$k]['sum'] = intval($v1['sum']);
                }
            }
        }
        // $sum_arr_h5 = json_encode($sum_arr_h5);
        $message = Lang("Success");
        return output(200,$message,array('startDate'=>$startdate,'endDate'=>$enddate,'sum_arr_wx'=>$sum_arr_wx,'sum_arr_app'=>$sum_arr_app,
            'sum_arr_pc'=>$sum_arr_pc,'sum_arr_h5'=>$sum_arr_h5));
    }

    //新增用户列表
    public function getAddUserList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $startdate = addslashes(trim($this->request->param('startDate')));
        $enddate = addslashes(trim($this->request->param('endDate')));
        $page = $this->request->param('pageNo');
        $pagesize = $this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize:'10';
        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        //没有时间参数，默认返回七天的数据
        if (empty($startdate) || empty($enddate))
        {
            $enddate = date("Y-m-d");
            $startdate_1 = strtotime('-6 day');
            $startdate = date("Y-m-d", $startdate_1);
        }
        else
        {
            $startdate = $startdate;
            $enddate = $enddate.' 23:59:59';
        }
        $total = 0;
        $total = UserModel::where('store_id',$store_id)->where('Register_data','between',[$startdate,$enddate])->count();
        $list = array();
        if($total > 0)
        {
            //获取元素内容
            $res1 = UserModel::where('store_id',$store_id)
                            ->where('Register_data','between',[$startdate,$enddate])
                            ->field('id,user_id,user_name,Register_data,source')
                            ->order('Register_data','desc')
                            ->limit($start,$pagesize)
                            ->select()
                            ->toArray();
            if($res1)
            {
                $list = $res1;
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total));
    }

    //计算两天时间之差   $day1-------开始时间，$day2----------结束时间
    function diffBetweenTwoDays($day1, $day2)
    {
        $second1 = strtotime($day1);
        $second2 = strtotime($day2);

        if ($second1 < $second2)
        {
            $tmp = $second2;
            $second2 = $second1;
            $second1 = $tmp;
        }
        return ($second1 - $second2) / 86400;
    }

    public function getDateFromRange($startdate, $enddate)
    {

        $stimestamp = strtotime($startdate);
        $etimestamp = strtotime($enddate);

        // 计算日期段内有多少天
        $days = ($etimestamp - $stimestamp) / 86400 + 1;

        // 保存每天日期
        $date = array();

        for ($i = 0; $i < $days; $i++)
        {
            $date[] = date('Y-m-d', $stimestamp + (86400 * $i));
        }

        return $date;
    }

    //用户消费报表
    public function getUserConsumptionInfo()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $startdate = addslashes(trim($this->request->param('startDate')));
        $enddate = addslashes(trim($this->request->param('endDate')));
        $page = $this->request->param('pageNo');
        $pagesize = $this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize:'10';
        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        //初始化分页查询条件
        $condition = " where a.store_id = '$store_id' and b.store_id = '$store_id' and b.status in (1,2,5)";
        if ($startdate != '')
        {
            $condition .= "and a.Register_data >= '$startdate'";
        }
        if ($enddate != '')
        {
            $condition .= "and a.Register_data <= '$enddate'";
        }

        $total = 0;
        //初始化分页类
        $sql = "select ifnull(COUNT(e.id),0) as num from(select tt.* from (select a.id,row_number () over (partition by b.user_id) as top from lkt_user as a INNER JOIN lkt_order as b on a.user_id = b.user_id" .$condition . ") as tt where tt.top<2) as e";
        $res = Db::query($sql);
        if($res)
        {
            $total = $res[0]['num'];
        }
        $sql = "select tt.* from (select a.id ,a.user_id ,a.user_name , a.source,SUM(b.z_price) over (PARTITION BY b.user_id) AS orderAmt,COUNT(b.id) over (PARTITION BY b.user_id) AS orderNum,row_number () over (partition by b.user_id) as top from lkt_user as a INNER JOIN lkt_order as b on a.user_id = b.user_id" .
            $condition . ") as tt where tt.top<2 order by tt.orderAmt desc limit $start,$pagesize";
        $res = Db::query($sql);

        
        //通过foreach计算每个用户的退款数，及退款总金额
        foreach ($res as $k => $v)
        {
            $user_id = $v['user_id'];
            $sql1 = "select COUNT(id) as back_num,ifnull(SUM(re_money),0) as back_z_price from lkt_return_order where store_id = '$store_id' and user_id = '$user_id' and re_type != 3 and r_type in (4,9)";
            $res1 = Db::query($sql1);
            //判断是否存在退货商品
            if (empty($res1))
            {
                $res[$k]['reback_num'] = 0;
                $res[$k]['reback_amt'] = 0;
                $res[$k]['orderNum'] = (float)$v['orderNum'];
                $res[$k]['orderAmt'] = (float)$v['orderAmt'];
                $res[$k]['yx_num'] = (float)$v['orderNum'];
                $res[$k]['yx_price'] = (float)$v['orderAmt'];
            }
            else
            {
                $res[$k]['reback_num'] = $res1[0]['back_num'];
                $res[$k]['reback_amt'] = (float)round($res1[0]['back_z_price'],2);
                $res[$k]['orderNum'] = intval($v['orderNum']);
                $res[$k]['orderAmt'] = (float)round($v['orderAmt'],2);
                $res[$k]['yx_num'] = intval($v['orderNum']);
                $res[$k]['yx_price'] = (float)round($v['orderAmt'],2);
            }
            switch ($v['source']) 
            {
                case '2':
                    $res[$k]['sourceName'] = 'app';
                    break;
                case '1':
                case '3':
                case '4':
                case '5':
                    $res[$k]['sourceName'] = '小程序';
                    break;
                case '6':
                    $res[$k]['sourceName'] = 'PC端';
                    break;
                case '7':
                    $res[$k]['sourceName'] = 'H5';
                    break;
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$res,'total'=>$total));
    }

    //用户比例报表
    public function getUserDistributionInfo()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        //接受参数
        $startdate = addslashes(trim($this->request->param('startDate')));
        $enddate = addslashes(trim($this->request->param('endDate')));

        //没有时间参数则设置为七天
        if (empty($startdate) || empty($enddate))
        {
            $enddate = date("Y-m-d").' 23:59:59';
            $startdate = date("Y-m-d", strtotime('-6 day'));
        }

        //根据时间查询总数---------------小程序
        $num_wx = UserModel::where(['store_id'=>$store_id,'source'=>1])
                        ->where('Register_data','between',[$startdate,$enddate])
                        ->count();

        //根据时间查询总数---------------手机app
        $num_app = UserModel::where(['store_id'=>$store_id,'source'=>2])
                        ->where('Register_data','between',[$startdate,$enddate])
                        ->count();

        //根据时间查询总数---------------pc
        $num_pc = UserModel::where(['store_id'=>$store_id,'source'=>6])
                        ->where('Register_data','between',[$startdate,$enddate])
                        ->count();

        //根据时间查询总数---------------h5
        $num_h5 = UserModel::where(['store_id'=>$store_id,'source'=>7])
                        ->where('Register_data','between',[$startdate,$enddate])
                        ->count();
        //根据时间查询总数---------------支付宝              
        $num_zfb = UserModel::where(['store_id'=>$store_id,'source'=>3])
                        ->where('Register_data','between',[$startdate,$enddate])
                        ->count();

        $data = array('startDate'=>$startdate,'endDate'=>$enddate,'num_wx'=>$num_wx,'num_app'=>$num_app,'num_pc'=>$num_pc,'num_h5'=>$num_h5);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    //订单报表
    public function getOrderReport()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        //接受参数
        $startdate = addslashes($this->request->param('startDate'));
        $enddate = addslashes($this->request->param('endDate'));

        //默认时间段为七天
        if (empty($startdate) || empty($enddate))
        {
            $enddate = date("Y-m-d").' 23:59:59';
            $startdate = date("Y-m-d", strtotime('-6 day'));

        }

        //获取日期数组
        $day_arr_1 = $this->getDateFromRange($startdate, $enddate);
        $day_arr = json_encode($day_arr_1);

        //查询订单总数,总成交额
        $sql = "select ifnull(COUNT(*),0) as num ,ifnull(SUM(z_price),0) as z_price from lkt_order where store_id = '$store_id' and add_time between '{$startdate} 00:00:00' and '{$enddate} 23:59:59'";
        $res_0 = Db::query($sql);

        $num = $res_0[0]['num'];
        $z_price = (float)$res_0[0]['z_price'];


        //查询有效订单总数，有效成交额
        $sql_1 = "select ifnull(COUNT(*),0) as num,ifnull(SUM(z_price),0) as z_price from lkt_order where store_id = '$store_id' and add_time between '{$startdate} 00:00:00' and '{$enddate} 23:59:59' and status > 0 and status in (1,2,5) and pay_time is not null ";
        $res_1 = Db::query($sql_1);
        $yx_num = $res_1[0]['num'];
        $yx_amt = (float)$res_1[0]['z_price'];


        //按每天查询有效订单----有效订单数，有效金额
        $sql = "select ifnull(COUNT(*),0) as num, ifnull(SUM(z_price),0) as z_price,DATE_FORMAT(add_time,'%Y-%m-%d') as r_date from lkt_order where store_id = '$store_id' and status in (1,2,5) and pay_time is not null group by r_date having r_date between '{$startdate}' and '{$enddate}'   order by r_date desc ";
        $res = Db::query($sql);
        //将没有订单的日期中的订单数置为0
        $list = array();//
        foreach ($day_arr_1 as $k => $v)
        {
            //将每个日期的订单数都置为零

            $list[$k]['num'] = 0;
            $list[$k]['r_date'] = $v;
            $list[$k]['z_price'] = 0;

        }
        foreach ($day_arr_1 as $k => $v)
        {
            if(!empty($res))
            {
                foreach ($res as $k1 => $v1)
                {
                    //如果有日期，则将该日期的num，z_price 返回
                    if ($v == ($v1['r_date']))
                    {
                        $list[$k]['num'] = $v1['num'];
                        $list[$k]['z_price'] = (float)$v1['z_price'];
                    }
                }
            }
        }

        $data[] = array('num'=>$num,'z_price'=>(float)$z_price,'yx_num'=>$yx_num,'yx_amt'=>$yx_amt);
        $message = Lang("Success");
        return output(200,$message,array('endDate'=>$enddate,'startDate'=>$startdate,'list'=>$list,'data'=>$data));
    }

    //商品报表图表
    public function getGoodsReport()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        //1.平台所有上架商品数量
        $sql = "select ifnull(count(a.id),0) as num from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where a.store_id = '$store_id' and a.recycle = 0 and a.mch_status = 2 and a.status = 2";
        $res = Db::query($sql);
        $product_num = 0;
        if ($res)
        {
            $product_num = $res[0]['num'];
        }

        //2.平台对接商家数量

        $sql0 = "select ifnull(count(m.id),0) as c_num from lkt_mch as m left join lkt_user as u on m.user_id = u.user_id where m.store_id = '$store_id' and u.store_id = '$store_id' and m.review_status = 1 and m.recovery = 0";
        $res = Db::query($sql0);
        $customer_num = 0;
        if ($res)
        {
            $customer_num = $res[0]['c_num'];
        }
        $res_top = array();
        //3.前十商品销售排行
        $res = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0])->field('id,product_title,volume')->order('volume','desc')->limit(0,10)->select()->toArray();
        if($res)
        {
            $res_top = $res;
        }
        //5.商品库存量统计(按一级商品分类)
        $sql_class = "select pname as className,cid as topClass from lkt_product_class where store_id = '$store_id' and level = 0 and recycle = 0 order by cid desc";
        $res_class = Db::query($sql_class);

        //5.1 入库量
        $sql = "select a.product_id,SUM(flowing_num) as all_num,b.product_class,a.type from lkt_stock as a left join lkt_product_list as b on a.product_id = b.id  where a.store_id = '$store_id' and b.store_id = '$store_id' and b.recycle = 0 and a.type = 0 group by a.product_id,b.product_class,a.type ";
        $res = Db::query($sql); //入库量


        $class_arr = array();

        if ($res)
        {
            foreach ($res as $k => $v)
            {
                if (!empty($v['product_class']))
                {
                    $class_arr = explode('-', $v['product_class']);
                    $res[$k]['class'] = $class_arr[1];  //改商品的顶级分类
                }
                else
                {
                    $res[$k]['class'] = '';
                }
            }
        }

        if ($res_class)
        {

            foreach ($res_class as $k => $v)
            {
                $cid = $v['topClass'];
                $pname = $v['className'];
                $res_class[$k]['in_num'] = 0;

                foreach ($res as $k1 => $v1)
                {
                    if ($cid == $v1['class'])
                    {

                        $res_class[$k]['in_num'] += $v1['all_num'];//顶级分类下入库量

                    }
                }
            }
        }


        //5.2出库量
        $sql_0 = "select a.product_id,SUM(flowing_num) as all_num,b.product_class,a.type from lkt_stock as a left join lkt_product_list as b on a.product_id = b.id  where a.store_id = '$store_id' and b.store_id = '$store_id' and b.recycle = 0 and a.type = 1 group by a.product_id,b.product_class,a.type ";
        $res_0 = Db::query($sql_0);


        if ($res_0)
        {

            foreach ($res_0 as $k => $v)
            {
                $class_arr = explode('-', $v['product_class']);
                if (isset($class_arr[1])) {
                    $res_0[$k]['class'] = $class_arr[1];//顶级分类id
                } else {
                    $res_0[$k]['class'] = '';
                }


            }
        }


        if ($res_class)
        {

            foreach ($res_class as $k => $v)
            {

                $pname = $v['className'];
                $cid = $v['topClass'];
                $res_class[$k]['out_num'] = 0;
                foreach ($res_0 as $k1 => $v1)
                {
                    if ($cid == $v1['class'])
                    {

                        $res_class[$k]['out_num'] += $v1['all_num'];//顶级分类下出库量
                    }
                }
            }
        }

        //5.3剩余库存量
        $res_have = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0])->field('id,product_class,num')->select()->toArray();
        if ($res_have)
        {

            foreach ($res_have as $k => $v)
            {
                if ($v['product_class'])
                {
                    $class_arr_0 = explode('-', $v['product_class']);
                    $res_have[$k]['class'] = $class_arr_0[1];//顶级分类id
                }
                else
                {
                    $res_have[$k]['class'] = '';
                }

            }
        }

        if ($res_class)
        {

            foreach ($res_class as $k => $v)
            {

                $cid = $v['topClass'];
                $res_class[$k]['sy_num'] = 0;//剩余库存
                foreach ($res_have as $k1 => $v1)
                {
                    if ($cid == $v1['class'])
                    {

                        $res_class[$k]['sy_num'] += $v1['num'];//顶级分类下，商品剩余库存
                    }
                }
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('customer_num'=>$customer_num,'product_num'=>$product_num,'top10'=>$res_top,'stock'=>$res_class));
    }

    //商品预警
    public function getGoodsReportGoodsList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $page = $this->request->param('pageNo');
        $pagesize = $this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize:'10';
        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $total = 0;
        $sql_total = "select ifnull(COUNT(*),0) as total  from lkt_configure as c left join lkt_product_list as a on c.pid = a.id left join (select max(add_date) as add_date,type,attribute_id from lkt_stock where type = 2 group by attribute_id,type) as b on c.id = b.attribute_id where a.store_id = '$store_id' and a.recycle = 0 and c.num <= a.min_inventory";
        $res_total = Db::query($sql_total);
        if ($res_total)
        {
            $total = $res_total[0]['total'];//总记录数
        }

        $goodsStock = array();
        if($total > 0)
        {
            $sql_stock = "select a.id,a.product_title,c.img,c.attribute,c.total_num,c.num,b.add_date from lkt_configure as c left join lkt_product_list as a on c.pid = a.id left join (select max(add_date) as add_date ,type ,attribute_id from lkt_stock where type = 2 group by attribute_id,type ) as b on c.id = b.attribute_id where a.store_id = '$store_id' and a.recycle = 0 and c.num <= a.min_inventory order by a.add_date desc limit $start,$pagesize";
            $res_stock = Db::query($sql_stock);
            if ($res_stock)
            {

                foreach ($res_stock as $k => $v)
                {

                    $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
                    $attribute = unserialize($v['attribute']);
                    $specifications = '';
                    if(is_array($attribute))
                    {
                        foreach ($attribute as $ke => $va)
                        {   
                            $ke = substr($ke, 0, strrpos($ke, "_LKT"));
                            $va = substr($va, 0, strrpos($va, "_LKT"));
                            $specifications .= $ke . ':' . $va . ',';
                        }
                        $res_stock[$k]['attribute'] = rtrim($specifications, ",");
                    }
                    else
                    {
                        $res_stock[$k]['attribute'] = '';
                    }
                    
                }
                $goodsStock = $res_stock;
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('total'=>$total,'goodsStock'=>$goodsStock));
    }
}
