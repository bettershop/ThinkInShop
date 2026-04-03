<?php
namespace app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\ServerPath;

use app\admin\model\BrandClassModel;
use app\admin\model\ProductClassModel;
use app\admin\model\ProductListModel;
use app\admin\model\SupplierBrandModel;

class ProductBrand
{
    // 商品分类列表
    public function get_product_brand_list($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $brand_name = $array['brand_name']; // 品牌名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页多少条数据
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = $array['lang_code'];
        }

        $list = array();
        $total = 0;
        $field = "brand_id,store_id,brand_pic,brand_name,country_num,remarks,status,brand_time,sort,recycle,categories,is_default,examine,lang_code";

        $condition = " store_id = '$store_id' and recycle = 0 and examine = 1 and notset = 0 ";
        if ($brand_name != '')
        {
            $brand_name = Tools::FuzzyQueryConcatenation($brand_name);
            $condition .= " and brand_name like $brand_name ";
        }

        if($lang_code != '')
        {
            $condition .= " and lang_code = '$lang_code' ";
        }

        $r0 = BrandClassModel::where($condition)->field('count(brand_id) as total')->select()->toArray();
        if ($r0)
        {
            $total = $r0[0]['total'];
        }

        // 查询新闻分类表，根据sort顺序排列
        $r1 = BrandClassModel::where($condition)->page((int)$page,(int)$pagesize)->order('sort','desc')->field($field)->select()->toArray();
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                if ($v['brand_pic'] != '')
                {
                    $v['brand_pic'] = ServerPath::getimgpath($v['brand_pic'], $store_id);
                }
                $categories = $v['categories']; // 所属分类

                if ($categories == '' || $categories == 'NULL')
                {
                    $v['categoriesName'] = '';
                    $v['categories'] = array();
                }
                else
                {
                    $categories = trim($categories, ',');
                    $res = explode(',', $categories);
                    $res1 = array();
                    $class_list = array();
                    foreach ($res as $k1 => $v1)
                    {
                        $r_p_c = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0,'cid'=>$v1])->field('pname')->select()->toArray();
                        if ($r_p_c)
                        {
                            $class_list[] = $r_p_c[0]['pname'];
                            $res1[] = $v1;
                        }
                    }
                   
                    $class_name = implode(',', $class_list);
                    $v['categoriesName'] = $class_name;
                    $v['categories'] = $class_list;
                }

                $v['country_name'] = Tools::get_country_name($v['country_num']);
                $v['lang_name'] = Tools::get_lang_name($v['lang_code']);
                $list[] = $v;
            }
        }

        $data = array('brandInfoList' => $list, 'total' => $total);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 品牌添加/编辑
    public function addBrand($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $brand_id = $array['brand_id']; // 品牌ID
        $brand_name = $array['brand_name']; // 品牌名称
        $image = $array['image']; // 品牌图片
        $categories = $array['categories']; // 所属分类
        $country_num = $array['country_num']; // 产地
        $remarks = $array['remarks']; // 备注
        $examine = $array['examine']; // 审核状态 0.待审核 1.审核通过 2.不通过
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = $array['lang_code'];
        }
        $time = date("Y-m-d H:i:s");

        $Jurisdiction = new Jurisdiction();
        // 1.开启事务
        Db::startTrans();
        if($source != 2)
        {
            Tools::National_Language($lang_code,$country_num);
        }

        $lang_code = Tools::get_lang($lang_code);

        if ($brand_name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 品牌名称不能为空';
            $this->Log($Log_content);
            $message = Lang('product.74');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        
        // 检查分类名称是否重复
        if($brand_id != 0 && $brand_id != '')
        {
            $r = BrandClassModel::where(['store_id'=>$store_id,'brand_name'=>$brand_name,'lang_code'=>$lang_code,'country_num'=>$country_num,'recycle'=>0])->where('brand_id','<>',$brand_id)->select()->toArray();
        }
        else
        {
            $r = BrandClassModel::where(['store_id'=>$store_id,'brand_name'=>$brand_name,'lang_code'=>$lang_code,'country_num'=>$country_num,'recycle'=>0])->select()->toArray();
        }
        // 如果有数据 并且 数据条数大于0
        if ($r && count($r) > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品品牌名称(' . $brand_name . ') 已经存在，请选用其他名称！';
            $this->Log($Log_content);
            $message = Lang('product.75');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if (!empty($image))
        {
            $image = preg_replace('/.*\//', '', $image);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 品牌logo不能为空';
            $this->Log($Log_content);
            $message = Lang('product.76');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if ($categories == '' || $categories == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 所属分类不能为空';
            $this->Log($Log_content);
            $message = Lang('product.77');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            $categories = trim($categories, ',');
            $categories = ',' . $categories . ',';
        }

        if($brand_id != 0 && $brand_id != '')
        {
            $sql_update = array('brand_name'=>$brand_name,'brand_pic'=>$image,'country_num'=>$country_num,'remarks'=>$remarks,'categories'=>$categories,'lang_code'=>$lang_code,'examine'=>$examine);
            $sql_where = array('store_id'=>$store_id,'brand_id'=>$brand_id);
            $r = Db::name('brand_class')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改商品品牌失败！参数:'.json_encode($sql_where);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '修改了品牌ID：'.$brand_id.' 的信息失败',2,$source,$mch_id,$operator_id);
                $message = Lang('product.79');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改商品品牌ID为 ' . $brand_id . ' 成功';
                $this->Log($Log_content);
                Db::commit();
                $Jurisdiction->admin_record($store_id, $operator, '修改了品牌ID：'.$brand_id.' 的信息',2,$source,$mch_id,$operator_id);
                $message = Lang('product.80');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
        else
        {
            $r_sort = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0])->max('sort');
            $sort = $r_sort + 1;

            $sql = array('store_id'=>$store_id,'brand_name'=>$brand_name,'brand_pic'=>$image,'country_num'=>$country_num,'remarks'=>$remarks,'brand_time'=>$time,'sort'=>$sort,'categories'=>$categories,'lang_code'=>$lang_code,'mch_id'=>$mch_id,'supplier_id'=>$supplier_id,'examine'=>$examine);
            $r = Db::name('brand_class')->insertGetId($sql);
            if ($r == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加商品品牌失败！参数:'.json_encode($sql);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '添加了品牌：'.$brand_name.' 失败',1,$source,$mch_id,$operator_id);
                $message = Lang('product.81');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加商品品牌成功！参数:'.json_encode($sql);
                $this->Log($Log_content);
                Db::commit();
                $Jurisdiction->admin_record($store_id, $operator, '添加了品牌ID：'.$r,1,$source,$mch_id,$operator_id);
                $message = Lang('product.82');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
    }

    // 品牌置顶
    public function brandByTop($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $brand_id = $array['brand_id']; // 品牌ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商

        $Jurisdiction = new Jurisdiction();

        $rr = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $rr[0]['sort'];
        $sort = $sort + 1;

        $code = 200;
        $message = '';

        $sql_update = array('sort'=>$sort);
        $sql_where = array('store_id'=>$store_id,'brand_id'=>$brand_id,'recycle'=>0);
        $r = Db::name('brand_class')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $code = 200;
            $message = Lang('product.83');

            $Jurisdiction->admin_record($store_id, $operator, '置顶了品牌ID：'.$brand_id,2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 置顶品牌ID为' . $brand_id . '成功';
            $this->Log($Log_content);
        }
        else
        {
            $code = 201;
            $message = Lang('product.84');

            $Jurisdiction->admin_record($store_id, $operator, '置顶了品牌ID：'.$brand_id.' 失败',2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 置顶品牌失败！参数:'.json_encode($sql_update);
            $this->Log($Log_content);
        }
        
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 品牌删除
    public function delBrand($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $brand_id = $array['brand_id']; // 品牌ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商

        $Jurisdiction = new Jurisdiction();

        $r = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0,'brand_id'=>$brand_id])->select()->toArray();
        $lang_code = $r[0]['lang_code']; // 语种

        $sql_b1 = "update lkt_product_list set brand_id = '0',status = 3 where store_id = '$store_id' and brand_id = '$brand_id' ";
        $r_b1 = Db::execute($sql_b1);
        // $brand_id0 = 0;
        // $sql_b = "select brand_id,categories from lkt_brand_class where store_id = '$store_id' and lang_code = '$lang_code' order by brand_id asc limit 1 ";
        // $r_b = Db::query($sql_b);
        // if($r_b)
        // {
        //     $brand_id0 = $r_b[0]['brand_id'];
        // }

        // $r = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0,'brand_id'=>$brand_id])->field('id')->select()->toArray();
        // if ($r)
        // {
        //     $sql_b1 = "update lkt_product_list set brand_id = '$brand_id0' where store_id = '$store_id' and brand_id = '$brand_id' ";
        //     $r_b1 = Db::execute($sql_b1);
        // }

        // 根据分类id,删除这条数据
        $sql_update = array('recycle'=>1);
        $sql_where = array('store_id'=>$store_id,'brand_id'=>$brand_id,'recycle'=>0);
        $res = Db::name('brand_class')->where($sql_where)->update($sql_update);
        if ($res > 0)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了品牌ID：'.$brand_id,3,$source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 删除品牌ID为' . $brand_id . '成功';
            $this->Log($Log_content);
            $message = Lang('product.86');
            echo json_encode(array('code' => 200,'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了品牌ID：'.$brand_id.' 失败',3,$source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 删除品牌失败！参数:'.json_encode($sql_update);
            $this->Log($Log_content);
            $message = Lang('product.87');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
    }

    // 品牌审核列表
    public function auditList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $name = $array['name']; // 品牌名称/供应商名称
        $examine = $array['examine']; // 审核状态 0.待审核 1.审核通过 2.不通过
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页多少条数据
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商
        
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = $array['lang_code'];
        }

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $list = array();
        $total = 0;

        $condition = " a.store_id = '$store_id' and a.recycle = 0 ";

        if ($examine != '')
        {
            $condition .= " and a.examine = '$examine' ";
        }

        if($lang_code != '')
        {
            $condition .= " and a.lang_code = '$lang_code' ";
        }

        if($source == 1)
        { // 管理后台
            if ($name != '')
            {
                $name = Tools::FuzzyQueryConcatenation($name);
                $condition .= " and (a.brand_name like $name or b.name like $name or s.supplier_name like $name) ";
            }

            $sql0 = "select count(a.brand_id) as total from lkt_brand_class as a left join lkt_mch as b on a.mch_id = b.id left join lkt_supplier as s on a.supplier_id = s.id where $condition ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $str = " a.brand_id,a.store_id,a.brand_pic,a.brand_image,a.brand_name,a.country_num,a.remarks,a.status,a.brand_time,a.sort,a.recycle,a.categories,a.supplier_id,a.examine,a.remark,b.id as supplierId,b.name,s.supplier_name,a.mch_id,a.supplier_id,a.lang_code";
            
            $sql1 = "select $str from lkt_brand_class as a left join lkt_mch as b on a.mch_id = b.id left join lkt_supplier as s on a.supplier_id = s.id where $condition order by a.brand_time desc limit $start,$pagesize ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                foreach($r1 as $k => $v)
                {
                    $v['lang_name'] = Tools::get_lang_name($v['lang_code']);
                    if ($v['brand_pic'] != '')
                    {
                        $v['brand_pic'] = ServerPath::getimgpath($v['brand_pic'], $store_id);
                    }
                    $categories = $v['categories']; // 所属分类

                    if ($categories == '' || $categories == 'NULL')
                    {
                        $v['categoriesName'] = '';
                    }
                    else
                    {
                        $categories = trim($categories, ',');
                        $res = explode(',', $categories);
                        $res1 = array();
                        $class_list = array();
                        foreach ($res as $k1 => $v1)
                        {
                            $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0,'cid'=>$v1])->field('pname')->select()->toArray();
                            if ($r0)
                            {
                                $class_list[] = $r0[0]['pname'];
                                $res1[] = $v1;
                            }
                        }
                    
                        $class_name = implode(',', $class_list);
                        $v['categoriesName'] = $class_name;
                        $v['categories'] = $class_list;
                    }

                    if($v['examine'] == 1)
                    {
                        $v['examineDesc'] = '审核通过';
                    }
                    else if($v['examine'] == 2)
                    {
                        $v['examineDesc'] = '审核不通过';
                    }
                    else
                    {
                        $v['examineDesc'] = '待审核';
                    }
                    
                    if($v['mch_id'] != 0)
                    {
                        $v['supplier_name'] = $v['name'];
                    }
                    else if($v['supplier_id'] != 0)
                    {
                        $v['supplier_name'] = $v['supplier_name'];
                    }
                    else
                    {
                        $v['supplier_name'] = "平台";
                    }

                    $list[] = $v;
                }
            }
        }
        else if($source == 2)
        { // PC店铺端
            $condition .= " and a.mch_id = '$mch_id' ";
            if ($name != '')
            {
                $name = Tools::FuzzyQueryConcatenation($name);
                $condition .= " and (a.brand_name like $name or b.name like $name) ";
            }

            $sql0 = "select count(a.brand_id) as total from lkt_brand_class as a left join lkt_mch as b on a.mch_id = b.id where $condition ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $str = " a.brand_id,a.store_id,a.brand_pic,a.brand_image,a.brand_name,a.country_num,a.remarks,a.status,a.brand_time,a.sort,a.recycle,a.categories,a.supplier_id,a.examine,a.remark,b.id as supplierId,b.name as supplier_name,a.lang_code ";
            
            $sql1 = "select $str from lkt_brand_class as a left join lkt_mch as b on a.mch_id = b.id where $condition order by brand_time desc limit $start,$pagesize ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                foreach($r1 as $k => $v)
                {
                    $v['lang_name'] = Tools::get_lang_name($v['lang_code']);
                    if ($v['brand_pic'] != '')
                    {
                        $v['brand_pic'] = ServerPath::getimgpath($v['brand_pic'], $store_id);
                    }
                    $categories = $v['categories']; // 所属分类

                    if ($categories == '' || $categories == 'NULL')
                    {
                        $v['categoriesName'] = '';
                    }
                    else
                    {
                        $categories = trim($categories, ',');
                        $res = explode(',', $categories);
                        $res1 = array();
                        $class_list = array();
                        foreach ($res as $k1 => $v1)
                        {
                            $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0,'cid'=>$v1])->field('pname')->select()->toArray();
                            if ($r0)
                            {
                                $class_list[] = $r0[0]['pname'];
                                $res1[] = $v1;
                            }
                        }
                    
                        $class_name = implode(',', $class_list);
                        $v['categoriesName'] = $class_name;
                        $v['categories'] = $class_list;
                    }

                    if($v['examine'] == 1)
                    {
                        $v['examineDesc'] = '审核通过';
                    }
                    else if($v['examine'] == 2)
                    {
                        $v['examineDesc'] = '审核不通过';
                    }
                    else
                    {
                        $v['examineDesc'] = '待审核';
                    }
                    $list[] = $v;
                }
            }
        }
        else if($source == 2)
        { // PC供应商
            $condition .= " and a.supplier_id = '$supplier_id' ";
            if ($name != '')
            {
                $name = Tools::FuzzyQueryConcatenation($name);
                $condition .= " and (a.brand_name like $name or b.supplier_name like $name) ";
            }

            $sql0 = "select count(a.brand_id) as total from lkt_brand_class as a left join lkt_supplier as b on a.supplier_id = b.id where $condition ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $str = " a.brand_id,a.store_id,a.brand_pic,a.brand_image,a.brand_name,a.country_num,a.remarks,a.status,a.brand_time,a.sort,a.recycle,a.categories,a.supplier_id,a.examine,a.remark,b.id as supplierId,b.supplier_name,a.lang_code ";
            
            $sql1 = "select $str from lkt_brand_class as a left join lkt_supplier as b on a.supplier_id = b.id where $condition order by a.brand_time desc limit $start,$pagesize ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                foreach($r1 as $k => $v)
                {
                    $v['lang_name'] = Tools::get_lang_name($v['lang_code']);
                    if ($v['brand_pic'] != '')
                    {
                        $v['brand_pic'] = ServerPath::getimgpath($v['brand_pic'], $store_id);
                    }
                    $categories = $v['categories']; // 所属分类

                    if ($categories == '' || $categories == 'NULL')
                    {
                        $v['categoriesName'] = '';
                    }
                    else
                    {
                        $categories = trim($categories, ',');
                        $res = explode(',', $categories);
                        $res1 = array();
                        $class_list = array();
                        foreach ($res as $k1 => $v1)
                        {
                            $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0,'cid'=>$v1])->field('pname')->select()->toArray();
                            if ($r0)
                            {
                                $class_list[] = $r0[0]['pname'];
                                $res1[] = $v1;
                            }
                        }
                    
                        $class_name = implode(',', $class_list);
                        $v['categoriesName'] = $class_name;
                        $v['categories'] = $class_list;
                    }

                    if($v['examine'] == 1)
                    {
                        $v['examineDesc'] = '审核通过';
                    }
                    else if($v['examine'] == 2)
                    {
                        $v['examineDesc'] = '审核不通过';
                    }
                    else
                    {
                        $v['examineDesc'] = '待审核';
                    }
                    $list[] = $v;
                }
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang("Success");
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 品牌审核
    public function examine($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $id = $array['id']; // 品牌ID
        $status = $array['status']; // 审核状态 0.待审核 1.审核通过 2.不通过
        $refuse_reasons = $array['refuse_reasons']; // 拒绝理由
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        $list = array();
        $total = 0;
        Db::startTrans();

        $sql0 = "select * from lkt_brand_class where store_id = '$store_id' and brand_id = '$id' and recycle = 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $brand_name = $r0[0]['brand_name'];
            $lang_code = $r0[0]['lang_code'];
            $country_num = $r0[0]['country_num'];

            if($status == 2)
            { // 拒绝
                if ($refuse_reasons == "")
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '请填写拒绝原因！';
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('supplier.20');
                    echo json_encode(array('code' => 109,'message' => $message));
                    exit;
                }

                $sql1 = "update lkt_brand_class set examine = 2,remark = '$refuse_reasons' where store_id = '$store_id' and brand_id = '$id' and recycle = 0 ";
                $r1 = Db::execute($sql1);
                if ($r1 != -1)
                {
                    // $message_12 = "失败：商品品牌" . $brand_name . "审核拒绝";
                    // $message_logging_list12 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>$gongyingshang,'type'=>12,'parameter'=>$id,'content'=>$message_12);
                    // PC_Tools::add_message_logging($message_logging_list12);

                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator . '拒绝供应商品牌，ID为: ' . $id . ' 审核成功！';
                    $this->Log($Log_content);
                    Db::commit();
                    $Jurisdiction->admin_record($store_id, $operator, '拒绝了品牌ID：'.$id.'，并且名称为：'.$brand_name.' 的审核',6,$source,$mch_id,$operator_id);
                    $message = Lang('Success');
                    echo json_encode(array('code' => 200,'message' => $message));
                    exit;
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator . '拒绝供应商品牌失败！参数:'.json_encode($sql_where1);
                    $this->Log($Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '拒绝了品牌ID：'.$id.'，并且名称为：'.$brand_name.' 的审核失败',6,$source,$mch_id,$operator_id);
                    $message = Lang('supplier.21');
                    echo json_encode(array('code' => 109,'message' => $message));
                    exit;
                }
            }
            else
            { // 通过
                $sql0 = "select * from lkt_brand_class where store_id = '$store_id' and lang_code = '$lang_code' and country_num = '$country_num' and brand_id != '$id' and recycle = 0 and brand_name = '$brand_name' ";
                $r0 = Db::query($sql0);
                if($r0)
                {
                    $refuse_reasons = "";
                    $sql1 = "update lkt_brand_class set examine = 2,remark = '$refuse_reasons' where store_id = '$store_id' and brand_id = '$id' and recycle = 0 ";
                    $r1 = Db::execute($sql1);
                    if ($r1 != -1)
                    {
                        // $message_12 = "失败：商品品牌" . $brand_name . "审核拒绝";
                        // $message_logging_list12 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>$gongyingshang,'type'=>12,'parameter'=>$id,'content'=>$message_12);
                        // PC_Tools::add_message_logging($message_logging_list12);

                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . '拒绝供应商品牌，ID为: ' . $id . ' 审核成功！';
                        $this->Log($Log_content);
                        Db::commit();
                        $Jurisdiction->admin_record($store_id, $operator, '拒绝了品牌ID：'.$id.'，并且名称为：'.$brand_name.' 的审核',6,$source,$mch_id,$operator_id);
                        $message = Lang('Success');
                        echo json_encode(array('code' => ERROR_CODE_XTJCGPPMCYCZ,'message' => $message));
                        exit;
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . '拒绝供应商品牌失败！参数:'.json_encode($sql_where1);
                        $this->Log($Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '拒绝了品牌ID：'.$id.'，并且名称为：'.$brand_name.' 的审核失败',6,$source,$mch_id,$operator_id);
                        $message = Lang('supplier.21');
                        echo json_encode(array('code' => 109,'message' => $message));
                        exit;
                    }
                }
                else
                {
                    $sql1 = "update lkt_brand_class set examine = 1 where store_id = '$store_id' and brand_id = '$id' and recycle = 0 ";
                    $r1 = Db::execute($sql1);
                    if ($r1 != -1)
                    {
                        // $message_12 = "通过：商品品牌" . $brand_name . "审核通过";
                        // $message_logging_list12 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>$gongyingshang,'type'=>12,'parameter'=>$id,'content'=>$message_12);
                        // PC_Tools::add_message_logging($message_logging_list12);

                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 审核品牌成功，添加商品品牌成功！';
                        $this->Log($Log_content);

                        Db::commit();
                        $Jurisdiction->admin_record($store_id, $operator, '审核了品牌ID：'.$id.'，并且名称为：'.$brand_name.' 的审核',6,1,0,$operator_id);
                        $message = Lang('Success');
                        echo json_encode(array('code' => 200,'message' => $message));
                        exit;
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 审核品牌成功，添加商品品牌失败！参数:'.json_encode($sql2);
                        $this->Log($Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '审核了品牌ID：'.$id.'，并且名称为：'.$brand_name.' 的审核失败',6,$source,$mch_id,$operator_id);
                        $message = Lang('product.62');
                        echo json_encode(array('code' => 109,'message' => $message));
                        exit;
                    }
                }
            }
        }
    }

    // 获取品牌名称
    public static function GetBrandName($array)
    {
        $store_id = $array['store_id'];
        $bid = $array['bid']; // 品牌ID

        $name = "";
        $r_brand = BrandClassModel::where(['store_id'=>$store_id,'brand_id'=>$bid])->field('brand_name')->select()->toArray();
        if($r_brand)
        {
            $name = $r_brand[0]['brand_name'];
        }

        return $name;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("common/ProductBrand.log",$Log_content);
        return;
    }
}   
