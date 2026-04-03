<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\Jurisdiction;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\ExcelUtils;

use app\admin\model\BlockHomeModel;
use app\admin\model\BlockProductModel;
use app\admin\model\ProductClassModel;
use app\admin\model\BrandClassModel;
use app\admin\model\ConfigureModel;

/**
 * 功能：楼层类
 * 修改人：PJY
 */

class Block extends BaseController
{   
    // 列表
    public function list()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        // 每页显示多少条数据
        $name = $this->request->param('name');//楼层名称
        $pagesize = (int)$this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize : '10';
        // 页码
        $page = (int)$this->request->param('pageNo');
        if ($page)
        {
            $page = $page;
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $page = 1;
            $start = 0;
        }

        $condition = " store_id = '$store_id' and recycle = 0 ";
        if($name != '')
        {
            $name = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and block_name like $name ";
        }
        $total = 0;
        $sql_num = "select ifnull(count(id),0) as num from lkt_block_home where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        $sql = "select * from lkt_block_home where $condition order by sort desc limit $start,$pagesize";
        $res = Db::query($sql);
        if($res)
        {
            $list = $res;
        }
        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加，编辑
    public function add()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = (int)$this->request->param('id');
        $sort = (int)$this->request->param('sort');
        $name = $this->request->param('name');
        $img = $this->request->param('img');

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if(empty($id))
        {
            if(empty($name))
            {
                $message = Lang('block.0');
                return output(109,$message);
            }
            else
            {
                $res_r = BlockHomeModel::where(['store_id'=>$store_id,'recycle'=>0])->where('block_name',$name)->select()->toArray();
                if($res_r)
                {
                    $message = Lang('block.1');
                    return output(109,$message);
                }
            }
            
            $sql = new BlockHomeModel();
            $sql->store_id = $store_id;
            $sql->sort = $sort;
            $sql->block_name = $name;
            $sql->img = $img;
            $sql->add_date = date('Y-m-d H:i:s');
            $sql->save();
            $res = $sql->id;
            if($res < 1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了楼层：'.$name.' 失败',1,1,0,$operator_id);
                $this->log(__LINE__ . ':插入楼层失败，name为：' . $name . "\r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            $Jurisdiction->admin_record($store_id, $operator, '添加了楼层ID：'.$res,1,1,0,$operator_id);
        }
        else
        {
            if($name != '')
            {
                // 判断名称是否重复
                $res_r = BlockHomeModel::where(['store_id'=>$store_id,'recycle'=>0])->where('id','<>',$id)->where('block_name',$name)->select()->toArray();
                if($res_r)
                {
                    $message = Lang('block.1');
                    return output(109,$message);
                }
            }
            
            $sql = BlockHomeModel::find($id);
            $sql->sort = $sort;
            if($name != '')
            {
                $sql->block_name = $name;
            }
            $sql->img = $img;
            $res = $sql->save();
            if($res < 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了楼层ID：'.$id.' 失败',2,1,0,$operator_id);
                $this->log(__LINE__ . ':修改楼层失败，id为：' . $id . "\r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            $Jurisdiction->admin_record($store_id, $operator, '修改了楼层ID：'.$id,2,1,0,$operator_id);
        }
        $message = Lang('Success');
        return output(200,$message);
    }

    // 是否开启
    public function EnableOrNot()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = (int)$this->request->param('id');

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $sql0 = "select enable_or_not from lkt_block_home where id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            if($r0[0]['enable_or_not'] == 1)
            {
                $enable_or_not = 2;
            }
            else
            {
                $enable_or_not = 1;
            }

            $sql1 = "update lkt_block_home set enable_or_not = '$enable_or_not' where id = '$id' ";
            $r1 = Db::execute($sql1);
            if($r1 < 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '将楼层ID：'.$id.' 进行了是否开启操作失败',2,1,0,$operator_id);
                $this->log(__LINE__ . ':修改楼层失败，id为：' . $id );
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }

        $Jurisdiction->admin_record($store_id, $operator, '将楼层ID：'.$id.' 进行了是否开启操作',2,1,0,$operator_id);
        $message = Lang('Success');
        return output(200,$message);
    }

    // 删除
    public function deleteBlock()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = (int)$this->request->param('id');
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $sql = BlockHomeModel::find($id);
        if($sql)
        {
            $sql->recycle = 1;
            $res = $sql->save();
            if($res < 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '删除了楼层ID：'.$id.' 失败',3,1,0,$operator_id);
                $this->log(__LINE__ . ':删除楼层失败，id为：' . $id . "\r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }
        $Jurisdiction->admin_record($store_id, $operator, '删除了楼层ID：'.$id,3,1,0,$operator_id);
        $message = Lang('Success');
        return output(200,$message);
    }

    // 获取店铺和供应商
    public function getALlMchOrSupplier()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $allMch = array(); // 店铺数据
        $allSupplier = array(); // 供应商数据

        $sql_m = "select id,name from lkt_mch where store_id = '$store_id' and review_status = 1 and recovery = 0 ";
        $r_m = Db::query($sql_m);
        if($r_m)
        {
            foreach($r_m as $k_m => $v_m)
            {
                $v_m['type'] = "isMch";
                $allMch[] = $v_m;
            }
        }

        $gongyingshang_id_list = array();
        $sql0 = "select gongyingshang from lkt_product_list where store_id = '$store_id' and recycle = 0 and gongyingshang != 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $gongyingshang = $v0['gongyingshang'];
                if(!in_array($gongyingshang,$gongyingshang_id_list))
                {
                    $gongyingshang_id_list[] = $gongyingshang;

                    $sql1 = "select id,supplier_name as name from lkt_supplier where store_id = '$store_id' and status = 0 and recovery = 0 and id = '$gongyingshang' ";
                    $r1 = Db::query($sql1);
                    if($r1)
                    {
                        $allSupplier[] = array('id'=>$r1[0]['id'],'name'=>$r1[0]['name'],'type'=>"isSupplier");
                    }
                }
            }
        }

        $data = array('allMch'=>$allMch,'allSupplier'=>$allSupplier);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 楼层商品列表
    public function getGoodsByBlock()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = (int)$this->request->param('id');
        $bid = $this->request->param('bid');//品牌id
        $cid = $this->request->param('cid');//分类id
        $sourceType = $this->request->param('sourceType');// 来源类型
        $sourceId = $this->request->param('sourceId');// 来源数据ID
        $search = $this->request->param('key');//商品名称，商品id
        $startdate = $this->request->param("startTime");
        $enddate = $this->request->param("endTime"); 
        $pagesize = (int)$this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize : '10';
        // 页码
        $page = (int)$this->request->param('pageNo');
        if ($page)
        {
            $page = $page;
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $page = 1;
            $start = 0;
        }

        $product_class_arr = array();
        //分类下拉选择
        $r_class = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0])->order('sort','desc')->field('cid,pname')->select()->toArray();
        if($r_class)
        {
            foreach ($r_class as $key => $value)
            {
                $c = '-' . $value['cid'] . '-';
                $product_class_arr[$c] = $value['pname'];
                //循环第一层
                $r_e = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$value['cid']])->order('sort','desc')->field('cid,pname')->select()->toArray();
                if ($r_e)
                {
                    foreach ($r_e as $ke => $ve)
                    {
                        $cone = $c . $ve['cid'] . '-';
                        $product_class_arr[$cone] = $ve['pname'];

                        //循环第二层
                        $r_t = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$ve['cid']])->order('sort','desc')->field('cid,pname')->select()->toArray();
                        if ($r_t)
                        {
                            foreach ($r_t as $k => $v)
                            {
                                $ctow = $cone . $v['cid'] . '-';
                                $product_class_arr[$ctow] = $v['pname'];
                            }
                        }
                    }
                }
            }
        }

        $total = 0;

        $condition = " a.store_id = '$store_id' and a.id = '$id' and a.recycle  = 0 and c.recycle = 0 ";
        if($cid != '')
        {
            $condition .= " and c.product_class like '%-$cid-%' ";
        }
        if($bid != '')
        {
            $condition .= " and c.brand_id = '$bid' ";
        }

        if($sourceType == "isSupplier")
        { // 供应商
            if($sourceId != '')
            {
                $condition .= " and c.gongyingshang = '$sourceId' ";
            }
            else
            {
                $condition .= " and c.gongyingshang != 0 ";
            }
        }
        else if($sourceType == "isMch")
        { // 店铺
            if($sourceId != '')
            {
                $condition .= " and c.mch_id = '$sourceId' ";
            }
        }

        if($search != '')
        {
            $search1 = Tools::FuzzyQueryConcatenation($search);
            $condition .= " and (b.product_id = '$search' or c.product_title like $search1)";
        }
        if($startdate != '')
        {
            $condition .= " and b.add_date >= '$startdate' ";
        }
        if($enddate != '')
        {
            $condition .= " and b.add_date <= '$enddate' ";
        }
        $sql_num = "select ifnull(count(b.id),0) as num from lkt_block_home a
                                INNER JOIN lkt_block_product b ON a.id = b.main_id
                                INNER JOIN lkt_product_list c ON c.id = b.product_id
                                LEFT JOIN lkt_brand_class brand ON brand.brand_id = c.brand_id where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        $list = array();
        $sql = "select c.id,c.scan,c.gongyingshang,c.mch_id,c.imgurl,c.product_title,c.num,c.recycle,c.status,brand.brand_name brandName,b.sort,a.add_date,c.initial,b.id mappingId,c.product_class classId,c.store_id,c.s_type,b.add_date as proAddDate,m.name as mch_name 
                from lkt_block_home a
                INNER JOIN lkt_block_product b ON a.id = b.main_id
                INNER JOIN lkt_product_list c ON c.id = b.product_id
                left join lkt_mch as m on c.mch_id = m.id
                LEFT JOIN lkt_brand_class brand ON brand.brand_id = c.brand_id where $condition order by b.sort desc limit $start,$pagesize ";
        $res = Db::query($sql);
        if($res)
        {
            foreach ($res as $key => $value) 
            {   
                $pid = $value['id'];
                $class = $value['classId'];
                $gongyingshang = $value['gongyingshang'];
                $res[$key]['className'] = array_key_exists($class, $product_class_arr) ? $product_class_arr[$class]:'顶级'; // 分类名称
                $res[$key]['imgUrl'] = ServerPath::getimgpath($value['imgurl'], $store_id);

                $sql_c = "select min(price) as price from lkt_configure where pid = '$pid' and recycle = 0 ";
                $r_c = Db::query($sql_c);
                $res[$key]['price'] = $r_c[0]['price'];

                $res[$key]['pro_source'] = '';
                if($gongyingshang != 0)
                { // 供应商商品
                    $sql_s = "select supplier_name from lkt_supplier where id = '$gongyingshang' ";
                    $r_s = Db::query($sql_s);
                    if($r_s)
                    {
                        $res[$key]['pro_source'] = $r_s[0]['supplier_name'];
                    }
                }
                else
                {
                    $res[$key]['pro_source'] = $value['mch_name'];
                }
            }
            $list = $res;
        }
        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加楼层商品
    public function addOrDeleteGoodsWithBlock()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = (int)$this->request->param('id');
        $goodsId = $this->request->param('goodsId');
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if(empty($goodsId))
        {
            $message = Lang('block.2');
            return output(109,$message);
        }
        $goods_arr = explode(",",$goodsId);
        Db::startTrans();
        foreach ($goods_arr as $key => $value) 
        {
            $sql = new BlockProductModel();
            $sql->store_id = $store_id;
            $sql->main_id = $id;
            $sql->product_id = $value;
            $sql->add_date = date('Y-m-d H:i:s');
            $sql->save();
            $res = $sql->id;
            if($res < 1)
            {
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '将楼层ID：'.$id.' 添加了商品，商品ID：'.$value.'失败',1,1,0,$operator_id);
                $this->log(__LINE__ . ':添加商品失败，product_id为：' . $value . "\r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            $Jurisdiction->admin_record($store_id, $operator, '将楼层ID：'.$id.' 添加了商品，商品ID：'.$value,1,1,0,$operator_id);
        }
        Db::commit();
        $message = Lang('Success');
        return output(200,$message);
    }

    // 修改商品排序
    public function editSort()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $mapping_id = $this->request->param('mappingId');
        $sort = $this->request->param('sort');

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $sql = BlockProductModel::find($mapping_id);
        if($sql)
        {
            $sql->sort = $sort;
            $res = $sql->save();
            if($res < 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改商品排序失败，id为：'.$mapping_id.' 失败',2,1,0,$operator_id);
                $this->log(__LINE__ . ':修改商品排序失败，id为：' . $mapping_id . "\r\n");
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }
        $Jurisdiction->admin_record($store_id, $operator, '修改商品排序失败，id为：'.$mapping_id,2,1,0,$operator_id);
        $message = Lang('Success');
        return output(200,$message);
    }

    // 移除商品
    public function delGoods()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = (int)$this->request->param('id');
        $goodsId = $this->request->param('goodsIds');
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if(empty($goodsId))
        {
            $message = Lang('block.2');
            return output(109,$message);
        }

        $res = BlockProductModel::where(['store_id'=>$store_id,'main_id'=>$id])->where('product_id','in',$goodsId)->delete();
        if(!$res)
        {
            $Jurisdiction->admin_record($store_id, $operator, '将楼层ID：'.$id.' 的商品ID：'.$goodsId.' 进行了移除失败',3,1,0,$operator_id);
            $this->log(__LINE__ . ':删除楼层失败，product_id为：' . $goodsId . "\r\n");
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
        $Jurisdiction->admin_record($store_id, $operator, '将楼层ID：'.$id.' 的商品ID：'.$goodsId.' 进行了移除',3,1,0,$operator_id);
        $message = Lang('Success');
        return output(200,$message);
    }

    // 编辑回显
    public function selectOne()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));
        
        $id = (int)$this->request->param('id');

        $res = BlockHomeModel::where(['store_id'=>$store_id,'id'=>$id])->field('id,block_name as name,sort,img')->select()->toArray();
        $data = $res[0];
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 获取自营店商品
    public function GetSpecifiedGoodsInfo()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));
        
        $block_id = $this->request->param('blockId');//楼层id
        $cid = $this->request->param('cid');//分类id
        $brand_id = $this->request->param('brandId');//品牌id
        $sourceType = $this->request->param('sourceType');// 来源类型
        $sourceId = $this->request->param('sourceId');// 来源数据ID
        $productTitle = addslashes($this->request->param('productTitle')); // 商品名称
        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : 10;

        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        // $mch_id = PC_Tools::SelfOperatedStore($store_id);

        $product_class_arr = array();
        //分类下拉选择
        $r_class = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0])->order('sort','desc')->field('cid,pname')->select()->toArray();
        if($r_class)
        {
            foreach ($r_class as $key => $value)
            {
                $c = '-' . $value['cid'] . '-';
                $product_class_arr[$c] = $value['pname'];
                //循环第一层
                $r_e = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$value['cid']])->order('sort','desc')->field('cid,pname')->select()->toArray();
                if ($r_e)
                {
                    foreach ($r_e as $ke => $ve)
                    {
                        $cone = $c . $ve['cid'] . '-';
                        $product_class_arr[$cone] = $ve['pname'];

                        //循环第二层
                        $r_t = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$ve['cid']])->order('sort','desc')->field('cid,pname')->select()->toArray();
                        if ($r_t)
                        {
                            foreach ($r_t as $k => $v)
                            {
                                $ctow = $cone . $v['cid'] . '-';
                                $product_class_arr[$ctow] = $v['pname'];
                            }
                        }
                    }
                }
            }
        }
        
        $brand_class_arr = array();
        //品牌下拉选择
        $r_brand_class = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0])->select()->toArray();
        if($r_brand_class)
        {
            foreach ($r_brand_class as $key => $value)
            {
                $key0 = $value['brand_id'];
                $brand_class_arr[$key0] = $value['brand_name'];
            }
        }
        
        $condition = "a.store_id = '$store_id' and a.recycle = 0 and a.is_presell = 0 and a.commodity_type = '0'  ";
        if($block_id)
        {
            //获取楼层商品id
            $sql_bp = "select b.product_id from lkt_block_home a INNER JOIN lkt_block_product b ON a.id = b.main_id where a.store_id = '$store_id' and a.id = '$block_id' and a.recycle  = 0";
            $res_bp = Db::query($sql_bp);
            if($res_bp)
            {   
                $res_a = array();
                foreach ($res_bp as $key => $value) 
                {
                    $res_a[] = $value['product_id'];
                }
                $bp_arr = implode(",",$res_a);
                $condition .= " and a.id not in ($bp_arr) ";
            }
        }
        if($cid)
        {
            $condition .= " and a.product_class like '%-$cid-%' ";
        }
        if($brand_id)
        {
            $condition .= " and a.brand_id = '$brand_id' ";
        }
        if($sourceType == "isSupplier")
        { // 供应商
            if($sourceId != '')
            {
                $condition .= " and a.gongyingshang = '$sourceId' ";
            }
            else
            {
                $condition .= " and a.gongyingshang != 0 ";
            }
        }
        else if($sourceType == "isMch")
        { // 店铺
            if($sourceId != '')
            {
                $condition .= " and a.mch_id = '$sourceId' ";
            }
        }

        if($productTitle)
        {
            $productTitle = Tools::FuzzyQueryConcatenation($productTitle);
            $condition .= " and a.product_title like $productTitle ";
        }
        $list = array();
        $total = 0;
        $sql0 = "select count(a.id) as total from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $str = "a.id,a.store_id,a.product_number,a.commodity_type,a.product_title,a.subtitle,a.label,a.scan,a.product_class,a.imgurl,a.content,a.richList,a.sort,a.add_date,a.upper_shelf_time,a.volume,a.initial,a.s_type,a.num,a.min_inventory,a.status,a.brand_id,a.is_distribution,a.is_default_ratio,a.keyword,a.weight,a.freight,a.is_zhekou,a.separate_distribution,a.recycle,a.gongyingshang,a.is_hexiao,a.active,a.mch_id,a.mch_status,a.search_num,a.publisher,a.is_zixuan,a.source,a.comment_num,a.cover_map,a.class_sort,a.display_position_sort,a.is_presell,a.show_adr,b.name as mch_name";

        $sql1 = "select $str from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort asc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $class = $v['product_class'];
                $bid = $v['brand_id'];
                $gongyingshang = $v['gongyingshang'];
                $v['p_name'] = array_key_exists($class, $product_class_arr) ? $product_class_arr[$class]:'顶级'; // 分类名称
                $v['brand_name'] = array_key_exists($bid, $brand_class_arr) ? $brand_class_arr[$bid]:'暂无'; // 品牌名称
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id);
                $v['receiving_form'] = '1';

                $pid = $v['id'];
                $r_s = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('min(price) as price')->select()->toArray();
                if ($r_s)
                {
                    $v['price'] = round($r_s[0]['price'],2);
                }

                $v['pro_source'] = '';
                if($gongyingshang != 0)
                { // 供应商商品
                    $sql_s = "select supplier_name from lkt_supplier where id = '$gongyingshang' ";
                    $r_s = Db::query($sql_s);
                    if($r_s)
                    {
                        $v['pro_source'] = $r_s[0]['supplier_name'];
                    }
                }
                else
                {
                    $v['pro_source'] = $v['mch_name'];
                }
               
                $list[] = $v;
            }
        }

        $data = array('list' => $list,'total' => $total);
        $message = Lang('Success');
        return output(200,$message, $data);
    }
    
    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/block.log",$Log_content);
        return;
    }
}
