<?php
namespace app\common;
use think\facade\Db;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Plugin\Plugin;
use app\common\Jurisdiction;
use app\common\Product;
use app\common\LaiKeLogUtils;

use app\admin\model\ConfigureModel;

class ProductInventory
{   
    // 库存列表
    public function Inventory_List($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = $array['lang_code'];
        }
        $mch_name = $array['mch_name'];
        $product_title = $array['product_title'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $operator_source = $array['operator_source'];
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = "a.store_id = '$store_id' and a.recycle = 0 and c.recycle = 0 and a.mch_status = 2 and a.gongyingshang = 0 ";
        if($operator_source != 1)
        {
            $condition .= " and a.mch_id = '$mch_id' ";
        }
        if ($lang_code != '')
        {
            $condition .= " and a.lang_code = '$lang_code' ";
        }
        if ($mch_name != '')
        {
            $mch_name = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and m.name like $mch_name ";
        }
        if ($product_title != '')
        {
            $product_title = Tools::FuzzyQueryConcatenation($product_title);
            $condition .= " and a.product_title like $product_title ";
        }

        $list = array();
        $total = 0;

        $sql0 = "select ifnull(count(1),0) as total from lkt_configure as c left join lkt_product_list as a on c.pid = a.id left join lkt_mch as m on a.mch_id = m.id left join (select max(add_date) as add_date,attribute_id from lkt_stock group by attribute_id)  as b on c.id = b.attribute_id where $condition order by a.upper_shelf_time desc ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select a.product_number,a.product_title,a.imgurl,a.status,a.mch_id,a.upper_shelf_time,c.id,c.pid,c.price,c.yprice,c.attribute,c.total_num,c.num,b.add_date,m.name as shop_name from lkt_configure as c left join lkt_product_list as a on c.pid = a.id left join lkt_mch as m on a.mch_id = m.id left join (select max(add_date) as add_date,attribute_id from lkt_stock group by attribute_id)  as b on c.id = b.attribute_id where $condition order by a.upper_shelf_time desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);

                $attribute_array = array('store_id'=>$store_id,'attribute'=>$v['attribute']);
                $v['specifications'] = Product::AttributeProcessing($attribute_array); // 属性处理
                
                $GetProductStatus_array = array('store_id'=>$store_id,'status'=>$v['status']);
                $v['statusName'] = Product::GetProductStatus($GetProductStatus_array); // 获取商品状态

                $v['price'] = round($v['price'],2);
                $v['yprice'] = round($v['yprice'],2);
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 入库详情
    public function Enter_list($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = $array['lang_code'];
        }
        $mch_name = $array['mch_name'];
        $product_title = $array['product_title'];
        $startdate = $array['startdate'];
        $enddate = $array['enddate'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $operator_source = $array['operator_source'];
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = "a.store_id = '$store_id' and a.recycle = 0 and c.recycle = 0 and a.mch_status = 2 and b.type = 0 and a.gongyingshang = 0 ";
        if($operator_source != 1)
        {
            $condition .= " and a.mch_id = '$mch_id' ";
        }
        if ($lang_code != '')
        {
            $condition .= " and a.lang_code = '$lang_code' ";
        }
        if ($mch_name != '')
        {
            $mch_name = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and d.name like $mch_name ";
        }
        if ($product_title != '')
        {
            $product_title = Tools::FuzzyQueryConcatenation($product_title);
            $condition .= " and a.product_title like $product_title ";
        }
        if ($startdate != '')
        {
            $condition .= " and b.add_date >= '$startdate' ";
        }
        if ($enddate != '')
        {
            $condition .= " and b.add_date <= '$enddate' ";
        }

        $list = array();
        $total = 0;

        $sql0 = "select ifnull(count(a.id),0) as total from lkt_stock as b left join lkt_product_list as a on b.product_id = a.id left join lkt_configure as c on b.attribute_id = c.id left join lkt_mch as d on a.mch_id = d.id where $condition order by b.add_date desc";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select a.product_number,a.product_title,a.imgurl,a.status,a.upper_shelf_time,a.mch_id,c.id as attrId,c.pid as goodsId,c.price,c.yprice,c.attribute,b.total_num,b.flowing_num,c.min_inventory,c.num,b.type,b.add_date,b.content,b.user_id,d.name from lkt_stock as b left join lkt_product_list as a on b.product_id = a.id left join lkt_configure as c on b.attribute_id = c.id left join lkt_mch as d on a.mch_id = d.id where $condition order by b.add_date desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        // var_dump($sql1);die;
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);

                $attribute_array = array('store_id'=>$store_id,'attribute'=>$v['attribute']);
                $v['specifications'] = Product::AttributeProcessing($attribute_array); // 属性处理
                
                $GetProductStatus_array = array('store_id'=>$store_id,'status'=>$v['status']);
                $v['statusName'] = Product::GetProductStatus($GetProductStatus_array); // 获取商品状态

                $v['price'] = round($v['price'],2);
                $v['yprice'] = round($v['yprice'],2);
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 出货详情
    public function Shipment_list($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = $array['lang_code'];
        }
        $mch_name = $array['mch_name'];
        $product_title = $array['product_title'];
        $startdate = $array['startdate'];
        $enddate = $array['enddate'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $operator_source = $array['operator_source'];
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = "a.store_id = '$store_id' and a.recycle = 0 and c.recycle = 0 and a.mch_status = 2 and b.type = 1 and a.gongyingshang = 0 ";
        if($operator_source != 1)
        {
            $condition .= " and a.mch_id = '$mch_id' ";
        }
        if ($lang_code != '')
        {
            $condition .= " and a.lang_code = '$lang_code' ";
        }
        if ($mch_name != '')
        {
            $mch_name = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and m.name like $mch_name ";
        }
        if ($product_title != '')
        {
            $product_title = Tools::FuzzyQueryConcatenation($product_title);
            $condition .= " and a.product_title like $product_title ";
        }
        if ($startdate != '')
        {
            $condition .= " and b.add_date >= '$startdate' ";
        }
        if ($enddate != '')
        {
            $condition .= " and b.add_date <= '$enddate' ";
        }

        $list = array();
        $total = 0;

        $sql0 = "select ifnull(count(a.id),0) as total from lkt_stock as b left join lkt_product_list as a on b.product_id = a.id left join lkt_configure as c on b.attribute_id = c.id left join lkt_mch as m on a.mch_id = m.id where $condition order by b.add_date desc";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select a.product_number,a.product_title,a.imgurl,a.status,a.upper_shelf_time,a.mch_id,c.id as attrId,c.pid as goodsId,c.price,c.yprice,c.attribute,b.total_num,b.flowing_num,c.min_inventory,c.num,b.type,b.add_date,b.content,b.user_id,m.name from lkt_stock as b left join lkt_product_list as a on b.product_id = a.id left join lkt_configure as c on b.attribute_id = c.id left join lkt_mch as m on a.mch_id = m.id where $condition order by b.add_date desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);

                $attribute_array = array('store_id'=>$store_id,'attribute'=>$v['attribute']);
                $v['specifications'] = Product::AttributeProcessing($attribute_array); // 属性处理
                
                $GetProductStatus_array = array('store_id'=>$store_id,'status'=>$v['status']);
                $v['statusName'] = Product::GetProductStatus($GetProductStatus_array); // 获取商品状态

                $v['price'] = round($v['price'],2);
                $v['yprice'] = round($v['yprice'],2);
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 库存预警
    public function Warning_list($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $lang_code = "";
        if(isset($array['lang_code']) && $array['lang_code'] != '')
        {
            $lang_code = $array['lang_code'];
        }
        $parameter = $array['parameter'];
        $mch_name = $array['mch_name'];
        $product_title = $array['product_title'];
        $startdate = $array['startdate'];
        $enddate = $array['enddate'];
        $sortCriteria = $array['sortCriteria'];
        $sort = $array['sort'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $operator_source = $array['operator_source'];
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = "a.store_id = '$store_id' and a.recycle = 0 and c.recycle = 0 and a.mch_status = 2 and a.gongyingshang = 0 and b.type = 2 ";
        if ($lang_code != '')
        {
            $condition .= " and a.lang_code = '$lang_code' ";
        }
        if($parameter != '')
        {
            $condition .= " and c.id = '$parameter' ";
            $message_logging_list_9 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$parameter,'type'=>9);
            PC_Tools::message_read($message_logging_list_9);
        }
        if($operator_source != 1)
        {
            $condition .= " and a.mch_id = '$mch_id' ";
        }
        if ($mch_name != '')
        {
            $mch_name = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and m.name like $mch_name ";
        }
        if ($product_title != '')
        {
            $product_title_1 = Tools::FuzzyQueryConcatenation($product_title);
            $condition .= " and (a.id = '$product_title' or a.product_title like $product_title_1 or m.name like $product_title_1) ";
        }
        if ($startdate != '')
        {
            $condition .= " and b.add_date >= '$startdate' ";
        }
        if ($enddate != '')
        {
            $condition .= " and b.add_date <= '$enddate' ";
        }

        $order_by = " a.upper_shelf_time desc ";
        if($sortCriteria == 'num')
        {
            $order_by = " c.num " . $sort;
        }
        else if($sortCriteria == 'upper_shelf_time')
        {
            $order_by = " a.upper_shelf_time " . $sort;
        }
        $list = array();
        $total = 0;

        $sql0 = "select tt.* from (select a.product_number,a.product_title,a.imgurl,a.status,a.mch_id,a.upper_shelf_time,a.min_inventory,c.id as attrId,c.pid as goodsId,c.price,c.attribute,c.total_num,c.num,max(b.add_date) over (partition by b.attribute_id) as add_date,b.type,b.flowing_num,b.user_id,row_number () over (partition by b.attribute_id) as top from lkt_configure as c left join lkt_product_list as a on c.pid = a.id left join lkt_mch as m on a.mch_id = m.id left join lkt_stock as b on c.id = b.attribute_id where $condition and c.num <= a.min_inventory order by $order_by) as tt where tt.top<2 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = count($r0);
        }

        $sql1 = "select tt.* from (select a.product_number,a.product_title,a.imgurl,a.status,a.mch_id,a.upper_shelf_time,a.min_inventory,c.id as attrId,c.pid as goodsId,c.price,c.yprice,c.attribute,c.total_num,c.num,max(b.add_date) over (partition by b.attribute_id) as add_date,b.type,b.flowing_num,b.user_id,m.name,row_number () over (partition by b.attribute_id) as top from lkt_configure as c left join lkt_product_list as a on c.pid = a.id left join lkt_mch as m on a.mch_id = m.id left join lkt_stock as b on c.id = b.attribute_id where $condition and c.num <= a.min_inventory order by $order_by) as tt where tt.top<2 limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);

                $attribute_array = array('store_id'=>$store_id,'attribute'=>$v['attribute']);
                $v['specifications'] = Product::AttributeProcessing($attribute_array); // 属性处理
                
                $GetProductStatus_array = array('store_id'=>$store_id,'status'=>$v['status']);
                $v['statusName'] = Product::GetProductStatus($GetProductStatus_array); // 获取商品状态

                if($v['type'] == 1)
                {
                    $v['content'] = '出库';
                }
                else if($v['status'] == 2)
                {
                    $v['content'] = '预警';
                }
                else 
                {
                    $v['content'] = '入库';
                }

                $v['price'] = round($v['price'],2);
                $v['yprice'] = round($v['yprice'],2);
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 库存详情
    public function Details_list($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $pid = $array['pid'];
        $id = $array['id'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $operator_source = $array['operator_source'];
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $message_logging_list_9 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$pid,'type'=>9);
        PC_Tools::message_pop_up($message_logging_list_9);
        PC_Tools::message_read($message_logging_list_9);

        $list = array();
        $total = 0;

        $sql0 = "select ifnull(count(a.id),0) as total from lkt_stock as b left join lkt_product_list as a on b.product_id = a.id left join lkt_configure as c on b.attribute_id = c.id left join lkt_mch as m on a.mch_id = m.id where a.store_id = '$store_id' and a.recycle = 0 and b.product_id = '$pid' and b.attribute_id = '$id' and a.gongyingshang = 0 order by b.add_date desc";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select a.product_number,a.product_title,a.imgurl,a.status,a.upper_shelf_time,a.mch_id,c.id as attrId,c.pid as goodsId,c.price,c.yprice,c.attribute,b.total_num,b.flowing_num,c.min_inventory,c.num,b.type,b.add_date,b.content,b.user_id,m.name from lkt_stock as b left join lkt_product_list as a on b.product_id = a.id left join lkt_configure as c on b.attribute_id = c.id left join lkt_mch as m on a.mch_id = m.id where a.store_id = '$store_id' and a.recycle = 0 and b.product_id = '$pid' and b.attribute_id = '$id' and a.gongyingshang = 0 order by b.add_date desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);

                $attribute_array = array('store_id'=>$store_id,'attribute'=>$v['attribute']);
                $v['specifications'] = Product::AttributeProcessing($attribute_array); // 属性处理

                $GetProductStatus_array = array('store_id'=>$store_id,'status'=>$v['status']);
                $v['statusName'] = Product::GetProductStatus($GetProductStatus_array); // 获取商品状态

                $v['price'] = round($v['price'],2);
                $v['yprice'] = round($v['yprice'],2);
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 预警记录
    public function Seewarning_list($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $pid = $array['pid'];
        $id = $array['id'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $operator_source = $array['operator_source'];
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $message_logging_list_9 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$pid,'type'=>9);
        PC_Tools::message_pop_up($message_logging_list_9);
        PC_Tools::message_read($message_logging_list_9);

        $list = array();
        $total = 0;

        $sql0 = "select ifnull(count(a.id),0) as total from lkt_stock as b left join lkt_product_list as a on b.product_id = a.id left join lkt_configure as c on b.attribute_id = c.id left join lkt_mch as m on a.mch_id = m.id where a.store_id = '$store_id' and a.recycle = 0 and b.type = 2 and b.product_id = '$pid' and b.attribute_id = '$id' and a.gongyingshang = 0 order by b.add_date desc";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select a.product_number,a.product_title,a.imgurl,a.status,a.upper_shelf_time,a.mch_id,c.id as attrId,c.pid as goodsId,c.price,c.yprice,c.attribute,b.total_num as num,b.flowing_num,c.min_inventory,c.total_num,b.type,b.add_date,b.content,b.user_id,m.name from lkt_stock as b left join lkt_product_list as a on b.product_id = a.id left join lkt_configure as c on b.attribute_id = c.id left join lkt_mch as m on a.mch_id = m.id where a.store_id = '$store_id' and a.recycle = 0 and b.type = 2 and b.product_id = '$pid' and b.attribute_id = '$id' and a.gongyingshang = 0 order by b.add_date desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);

                $attribute_array = array('store_id'=>$store_id,'attribute'=>$v['attribute']);
                $v['specifications'] = Product::AttributeProcessing($attribute_array); // 属性处理

                $GetProductStatus_array = array('store_id'=>$store_id,'status'=>$v['status']);
                $v['statusName'] = Product::GetProductStatus($GetProductStatus_array); // 获取商品状态

                $v['price'] = round($v['price'],2);
                $v['yprice'] = round($v['yprice'],2);
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 增加库存
    public function addStock($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $pid = $array['pid'];
        $id = $array['id'];
        $add_num = $array['add_num'];
        $operator_id = $array['operator_id'];
        $operator = $array['operator'];
        $operator_source = $array['operator_source'];

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        $sql_where0 = array('id'=>$id,'pid'=>$pid);
        $sql_update0 = array('num'=>Db::raw('num+'.$add_num),'total_num'=>Db::raw('total_num+'.$add_num));
        $r0 = Db::name('configure')->where($sql_where0)->update($sql_update0);

        $sql_update1 = array('num'=>Db::raw('num+'.$add_num));
        $res1 = Db::name('product_list')->where(['id'=>$pid])->update($sql_update1);       

        $r2 = ConfigureModel::where(['id'=>$id,'pid'=>$pid])->field('total_num')->select()->toArray();
        $total_num = $r2[0]['total_num'];

        if($add_num > 0)
        {
            $event = '添加了商品ID：' . $pid . '，' . $add_num . '个库存';

            $content = $operator . '增加商品总库存' . $add_num;
            $sql3 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$id,'total_num'=>$total_num,'flowing_num'=>$add_num,'type'=>0,'add_date'=>$time,'content'=>$content);
        }
        else
        {
            $add_num = abs($add_num);
            $event = '扣除了商品ID：' . $pid . '，' . $add_num . '个库存';

            $content = $operator . '减少商品总库存' . $add_num;
            $sql3 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$id,'total_num'=>$total_num,'flowing_num'=>$add_num,'type'=>1,'add_date'=>$time,'content'=>$content);
        }
        $r3 = Db::name('stock')->insert($sql3);
        if ($r3) 
        {
            $Jurisdiction->admin_record($store_id, $operator, $event,1,$operator_source,$mch_id,$operator_id);
            $message = Lang('Success');
            echo json_encode(array('code' => 200, 'message' => $message));
            exit;
        } 
        else 
        {
            $event .= '失败';
            $Jurisdiction->admin_record($store_id, $operator, $event,1,$operator_source,$mch_id,$operator_id);
            $message = Lang('Unknown error');
            echo json_encode(array('code' => 201, 'message' => $message));
            exit;
        }
    }

    // 批量增加库存
    public function BatchAddStock($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $ids = $array['ids'];
        $add_num = $array['add_num'];
        $operator_id = $array['operator_id'];
        $operator = $array['operator'];
        $operator_source = $array['operator_source'];

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        $id_list = array();
        if($ids != '')
        {
            $id_list = json_decode($ids,true);
        }

        Db::startTrans();

        if($id_list != array())
        {
            foreach($id_list as $k => $v)
            {
                $sql0 = "select * from lkt_configure where id = '$v' ";
                $r0 = Db::query($sql0);
                if($r0)
                {
                    $pid = $r0[0]['pid'];
                    $total_num = (int)$r0[0]['total_num'] + (int)$add_num;

                    $sql_where0 = array('id'=>$v,'pid'=>$pid);
                    $sql_update0 = array('num'=>Db::raw('num+'.$add_num),'total_num'=>Db::raw('total_num+'.$add_num));
                    $r1 = Db::name('configure')->where($sql_where0)->update($sql_update0);

                    $sql_where1 = array('id'=>$pid);
                    $sql_update1 = array('num'=>Db::raw('num+'.$add_num));
                    $r2 = Db::name('product_list')->where($sql_where1)->update($sql_update1);

                    if($add_num > 0)
                    {
                        $event = '添加了商品ID：' . $pid . '，' . $add_num . '个库存';

                        $content = $operator . '增加商品总库存' . $add_num;
                        $sql3 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$v,'total_num'=>$total_num,'flowing_num'=>$add_num,'type'=>0,'add_date'=>$time,'content'=>$content);
                    }
                    else
                    {
                        $add_num1 = abs($add_num);
                        $event = '扣除了商品ID：' . $pid . '，' . $add_num1 . '个库存';

                        $content = $operator . '减少商品总库存' . $add_num1;
                        $sql3 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$v,'total_num'=>$total_num,'flowing_num'=>$add_num1,'type'=>1,'add_date'=>$time,'content'=>$content);
                    }
                    $r3 = Db::name('stock')->insert($sql3);
                    if($r3 < 1)
                    {
                        Db::rollback();
                        $event .= '失败';
                        $Jurisdiction->admin_record($store_id, $operator, $event,1,$operator_source,$mch_id,$operator_id);
                        $message = Lang('Unknown error');
                        echo json_encode(array('code' => 201, 'message' => $message));
                        exit;
                    }
                    else
                    {
                        $Jurisdiction->admin_record($store_id, $operator, $event,1,$operator_source,$mch_id,$operator_id);
                    }
                }
            }
        }
        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 店铺日志
    public function Log($type0,$Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("common/ProductInventory.log",$Log_content);

        return;
    }

}