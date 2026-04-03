<?php
namespace app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\ServerPath;

use app\admin\model\ProductClassModel;
use app\admin\model\ProductListModel;
use app\admin\model\SupplierProClassModel;

class ProductClass
{
    // 商品分类列表
    public function get_product_class_list($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $type = $array['type']; // 类型 1=查询下级,2=查询上级,3=根据id查询 默认查询一级
        $classId = $array['classId']; // 分类ID
        $className = $array['className']; // 分类名称
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页多少条数据
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = $array['lang_code'];
        }

        $arr = Tools::get_data_dictionary(array('name'=>'商品分类','lang_code'=>$lang_code));

        $total = 0;
        $list = array();
        $field = "cid,sid,pname,english_name,img,level,sort,store_id,add_date,recycle,is_default,is_display,examine,lang_code,country_num";
        if($type == 1)
        { // 查询下级
            $condition = " store_id = '$store_id' and recycle = 0 and examine = 1 and notset = 0 ";
            if($classId != '')
            {
                $condition .= " and sid = '$classId' ";
            }

            if($className != '')
            {
                $className = Tools::FuzzyQueryConcatenation($className);
                $condition .= " and pname like $className ";
            }

            if($lang_code != '')
            {
                $condition .= " and lang_code = '$lang_code' ";
            }

            $r0 = ProductClassModel::where($condition)->field('count(cid) as total')->select()->toArray();
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $r1 = ProductClassModel::where($condition)->page((int)$page,(int)$pagesize)->order('sort','desc')->field($field)->select()->toArray();
            if ($r1) 
            {
                foreach ($r1 as $k => $v)
                {
                    $product_class = '-' . $v['cid'] . '-';
                    if ($v['img'] != '')
                    {
                        $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
                    }
                    foreach ($arr as $ke => $va)
                    {
                        if ((int)$v['level'] + 1 == $ke)
                        {
                            $v['levelFormat'] = $va;
                        }
                    }

                    $v['country_name'] = Tools::get_country_name($v['country_num']);
                    $v['lang_name'] = Tools::get_lang_name($v['lang_code']);
                    $list[] = $v;
                }
            }
        }
        else if($type == 2)
        { // 查询上级
            
        }
        else if($type == 3)
        { // 根据id查询
            $condition = " store_id = '$store_id' and recycle = 0 and examine = 1 and notset = 0 ";
            if($classId != '')
            {
                $condition .= " and cid = '$classId' ";
            }

            if($lang_code != '')
            {
                $condition .= " and lang_code = '$lang_code' ";
            }

            $r0 = ProductClassModel::where($condition)->field('count(cid) as total')->select()->toArray();
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $r1 = ProductClassModel::where($condition)->page((int)$page,(int)$pagesize)->order('sort','desc')->field($field)->select()->toArray();
            if ($r1) 
            {
                foreach ($r1 as $k => $v)
                {
                    $product_class = '-' . $v['cid'] . '-';
                    if ($v['img'] != '')
                    {
                        $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
                    }
                    foreach ($arr as $ke => $va)
                    {
                        if ((int)$v['level'] + 1 == $ke)
                        {
                            $v['levelFormat'] = $va;
                        }
                    }

                    $v['country_name'] = Tools::get_country_name($v['country_num']);
                    $v['lang_name'] = Tools::get_lang_name($v['lang_code']);
                    $list[] = $v;
                }
            }
        }
        else
        { // 查询一级
            $condition = " store_id = '$store_id' and recycle = 0 and sid = 0 and examine = 1 and notset = 0 ";
            if($className != '')
            {
                $className = Tools::FuzzyQueryConcatenation($className);
                $condition .= " and pname like $className ";
            }

            if($lang_code != '')
            {
                $condition .= " and lang_code = '$lang_code' ";
            }

            $r0 = ProductClassModel::where($condition)->field('count(cid) as total')->select()->toArray();
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $r1 = ProductClassModel::where($condition)->page((int)$page,(int)$pagesize)->order('sort','desc')->field($field)->select()->toArray();
            if ($r1) 
            {
                foreach ($r1 as $k => $v)
                {
                    $product_class = '-' . $v['cid'] . '-';
    
                    if ($v['img'] != '')
                    {
                        $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
                    }
                    foreach ($arr as $ke => $va)
                    {
                        if ((int)$v['level'] + 1 == $ke)
                        {
                            $v['levelFormat'] = $va;
                        }
                    }

                    $v['country_name'] = Tools::get_country_name($v['country_num']);
                    $v['lang_name'] = Tools::get_lang_name($v['lang_code']);
                    $list[] = $v;
                }
            }
        }

        $data = array('total'=>$total,'classInfo'=>$list);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 商品分类添加/编辑
    public function addClass($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $cid = $array['cid']; // 分类ID
        $pname = $array['pname']; // 分类名称
        $english_name = $array['english_name']; // 分类英文名称
        $level = $array['level']; // 级别
        $sid = $array['sid']; // 上级ID
        $img = $array['img']; // 图片
        $examine = $array['examine']; // 审核状态 0.待审核 1.审核通过 2.不通过
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = $array['lang_code'];
        }
        $country_num = "";
        if(isset($array['country_num']))
        {
            $country_num = $array['country_num'];
        }
        if($source != 2)
        {
            Tools::National_Language($lang_code,$country_num);
        }

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        Db::startTrans();

        if ($pname == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 分类名称不能为空！';
            $this->Log($Log_content);
            $message = Lang('product.58');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            $pname = addslashes($pname);
        }

        if(($level != 0 && $sid == 0) || ($level != 0 && $sid == ''))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 请选择上级分类！';
            $this->Log($Log_content);
            $message = Lang('product.64');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }

        if ($img)
        {
            $img = preg_replace('/.*\//', '', $img);
        }
        //检查分类名是否重复
        if($cid == 0 || $cid == '')
        { // 商品分类进入添加分类页面
            // 根据商城ID、分类名称、上级ID，查询分类
            $r = ProductClassModel::where(['store_id'=>$store_id,'lang_code'=>$lang_code,'country_num'=>$country_num,'pname'=>$pname,'recycle'=>0,'sid'=>$sid])->order('sort', 'desc')->field('cid')->select()->toArray();
            if ($r)
            { // 有重复的数据
                $Log_content = __METHOD__ . '->' . __LINE__ .' 同级分类中已存在此分类(' . $pname . ')，请选用其他名称修改！';
                $this->Log($Log_content);
                $message = Lang('product.61');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }

            $r_sort = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$sid])->max('sort');
            $sort = $r_sort + 1;

            $sql_res = array('sid'=>$sid,'lang_code'=>$lang_code,'country_num'=>$country_num,'pname'=>$pname,'english_name'=>$english_name,'img'=>$img,'level'=>$level,'store_id'=>$store_id,'sort'=>$sort,'add_date'=>$time,'mch_id'=>$mch_id,'supplier_id'=>$supplier_id,'examine'=>$examine);
            $class_res = Db::name('product_class')->insertGetId($sql_res);
            if ($class_res > 0)
            {
                // if($examine == 1)
                // {
                //     $sql_b = "select brand_id,categories from lkt_brand_class where store_id = '$store_id' order by brand_id asc limit 1 ";
                //     $r_b = Db::query($sql_b);
                //     if($r_b)
                //     {
                //         $brand_id = $r_b[0]['brand_id'];
                //         $categories = $r_b[0]['categories'];
                //         if($categories == '')
                //         {
                //             $categories = ',' . $class_res . ',';
                //         }
                //         else
                //         {
                //             $categories .= $class_res . ',';
                //         }

                //         $sql_b1 = "update lkt_brand_class set categories = '$categories' where store_id = '$store_id' and brand_id = '$brand_id' ";
                //         $r_b1 = Db::execute($sql_b1);
                //         if ($r_b1 == -1)
                //         {
                //             $Log_content = __METHOD__ . '->' . __LINE__ . $operator . '通过供应商分类，修改品牌失败！参数:'.json_encode($sql_where1);
                //             $this->Log($Log_content);
                //             Db::rollback();
                //             $Jurisdiction->admin_record($store_id, $operator, '添加了分类：'.$pname.'失败',1,$source,$mch_id,$operator_id);
                //             $message = Lang('supplier.21');
                //             echo json_encode(array('code' => 109,'message' => $message));
                //             exit;
                //         }
                //     }

                    $array = array('store_id'=>$store_id,'type0'=>1,'id'=>$class_res,'name'=>$pname,'lang_code'=>$lang_code,'country_num'=>$country_num);
                    PC_Tools::jump_path($array);
                // }
                
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加商品分类成功';
                $this->Log($Log_content);
                Db::commit();
                $Jurisdiction->admin_record($store_id, $operator, '添加了分类ID：'.$class_res,1,$source,$mch_id,$operator_id);
                $message = Lang('product.63');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加商品分类失败！参数:'.json_encode($sql_res);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '添加了分类：'.$pname.'失败',1,$source,$mch_id,$operator_id);
                $message = Lang('product.62');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
        }
        else
        {
            if ($cid == $sid)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品分类不能选择自己';
                $this->Log($Log_content);
                $message = Lang('product.65');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }

            $xiaji_list = array();
            $level_0 = 0; // 分类本身级别
            $level_1 = 0; // 最低一级的分类级别

            $xiaji = $this->xiaji($store_id,$cid,$xiaji_list); // 本身及下级的分类级别数组
            $level_0 = $xiaji[0];
            foreach ($xiaji as $k => $v)
            {
                if($v > $level_1)
                {
                    $level_1 = $v; // 最低一级的分类级别
                }
            }

            $grade_difference = $level_1 - $level_0; // 分类本身级别与最低一级的级别差
            $level1 = $level + $grade_difference; // 分类本身级别 + 级别差

            $arr = Tools::get_data_dictionary(array('name'=>'商品分类','lang_code'=>$lang_code));

            $arr_num = count($arr);
            if($level1 >= $arr_num)
            { // 当级别之和 >= 设置的级别
                $Log_content = __METHOD__ . '->' . __LINE__ . '分类'.$pname.'下级的级别超过数据字典里设置的级别！';
                $this->Log($Log_content);
                $message = Lang('product.66');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }

            if ($level == 0)
            {
                $sid = '0';
            }

            // 根据分类ID，查询商品
            $r0 = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0])->whereLike('product_class', '%-'.$cid.'-%')->field('id')->select()->toArray();
            if($r0)
            { // 该分类正在使用
                // 查询该分类的上级
                $r1 = ProductClassModel::where(['cid'=>$cid])->field('sid')->select()->toArray();
                if($r1)
                {
                    if($r1[0]['sid'] != $sid )
                    { // 当上级被修改
                        $Log_content = __METHOD__ . '->' . __LINE__ . "该分类正在使用，不能修改分类级别或上级分类！";
                        $this->Log($Log_content);
                        $message = Lang('product.67');
                        echo json_encode(array('code' => 109,'message' => $message));
                        exit;
                    }
                }
            }

            $r = ProductClassModel::where(['store_id'=>$store_id,'pname'=>$pname,'recycle'=>0,'sid'=>$sid])->where('cid', '<>',$cid)->order('sort', 'desc')->field('cid')->select()->toArray();
            if ($r)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 同级分类中已存在此分类(' . $pname . ')，请选用其他名称修改！';
                $this->Log($Log_content);
                $message = Lang('product.61');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }

            $sql_where = array('store_id'=>$store_id,'cid'=>$cid);
            $sql_update = array('lang_code'=>$lang_code,'country_num'=>$country_num,'pname'=>$pname,'english_name'=>$english_name,'level'=>$level,'sid'=>$sid,'img'=>$img,'examine'=>$examine);
            $theme_data = Db::name('product_class')->where($sql_where)->update($sql_update);
            if ($theme_data == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改商品分类失败！sql:'.$sql;
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '修改了分类ID：'.$cid.' 的信息失败',2,$source,$mch_id,$operator_id);
                $message = Lang('product.68');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            else
            {
                $this->found($store_id,$sid,$level);

                $array = array('store_id'=>$store_id,'type0'=>1,'id'=>$cid,'name'=>$pname,'lang_code'=>$lang_code,'country_num'=>$country_num);
                PC_Tools::modify_jump_path($array);

                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改商品分类ID为' . $cid . '成功';
                $this->Log($Log_content);
                Db::commit();

                $Jurisdiction->admin_record($store_id, $operator, '修改了分类ID：'.$cid.' 的信息',2,$source,$mch_id,$operator_id);
                $message = Lang('product.69');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
    }

    // 编辑商品分类页面（一级分类除外）
    public function getClassLevelTopAllInfo($array)
    {
        $store_id = $array['store_id'];
        $cid = $array['cid']; // 分类ID
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商

        $classInfo = array();
        $levelInfoList = array();
        // 根据分类id,查询产品分类表
        $r = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$cid])->select()->toArray();
        if($r)
        {
            $sid = $r[0]['sid']; // 上级id
            $r[0]['img'] = $r[0]['img'] ? ServerPath::getimgpath($r[0]['img'], $store_id) : '';

            $classInfo = $r[0];

            $r1 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$sid])->select()->toArray();
            if($r1)
            {
                $levelInfoList[] = $r1;
            }
            $levelInfoList[] = $r;
        }

        $data = array('classInfo' => $classInfo, 'levelInfoList' => $levelInfoList);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }
    
    public function xiaji($store_id,$cid,$xiaji_list)
    {
        // 根据分类ID，查询商品分类
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$cid])->field('cid,level')->select()->toArray();
        if($r0)
        {
            $xiaji_list[] = $r0[0]['level']; // 分类级别
        }
        // 根据上级ID，查询下级商品分类
        $r1 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$cid])->order('sort', 'desc')->field('cid,level')->select()->toArray();
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $id = $v['cid'];
                $xiaji_list[] = $v['level']; // 分类级别
                $xiaji_list = $this->xiaji($store_id,$id,$xiaji_list);
            }
        }
        return $xiaji_list;
    }
    
    public function found($store_id,$sid = 0, $level = 0)
    {
        $rr = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$sid])->order('sort', 'desc')->select()->toArray();
        foreach ($rr as $k => $v)
        {
            $cid = $v['cid'];

            $sql_where = array('store_id'=>$store_id,'cid'=>$cid,'recycle'=>0);
            $sql_update = array('level'=>$level);
            Db::name('product_class')->where($sql_where)->update($sql_update);

            $uplevel = $level + 1;
            $this->found($store_id,$cid, $uplevel);
        }
    }

    // 分类置顶
    public function classSortTop($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $cid = $array['cid']; // 分类ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商

        $Jurisdiction = new Jurisdiction();

        // 根据商城ID、分类ID，查询分类信息
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$cid])->field('sid')->select()->toArray();
        $sid = $r0[0]['sid']; // 上级分类ID
        // 根据商城ID、上级分类ID，查询该分类下级排序最高的排序
        $r1 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$sid])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $r1[0]['sort'];
        $sort = (int)$sort + 1;

        $sql_where = array('store_id'=>$store_id,'cid'=>$cid,'recycle'=>0);
        $sql_update = array('sort'=>$sort);
        $r = Db::name('product_class')->where($sql_where)->update($sql_update);
        if($r == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '置顶了分类ID：'.$cid.' 失败',2,$source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 置顶商品分类失败！参数:'.json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('product.70');
            echo json_encode(array('code' => 109,'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '置顶了分类ID：'.$cid,2,$source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 置顶商品分类成功！参数:'.json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('product.71');
            echo json_encode(array('code' => 200,'message' => $message));
            exit;
        }
    }

    // 分类删除
    public function delClass($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $cid = $array['cid']; // 分类ID
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商

        $Jurisdiction = new Jurisdiction();

        // 1.开启事务
        Db::startTrans();

        $str_option = array();
        // 根据分类ID,查询商品分类表
        $r = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$cid])->select()->toArray();
        $level = $r[0]['level']; // 分类级别
        $cid_r = $r[0]['cid']; // 分类ID
        $lang_code = $r[0]['lang_code']; // 语种
        $country_num = $r[0]['country_num']; // 国家代码

        $str_option[] = $cid;
        if ($level >= 0)
        {
            $str_option = $this->str_option($store_id, $cid, $str_option);
        }

        // $cid0 = '';
        // $sql_c = "select cid from lkt_product_class where store_id = '$store_id' and lang_code = '$lang_code' order by cid asc limit 1 ";
        // $r_c = Db::query($sql_c);
        // if($r_c)
        // {
        //     $cid0 = '-' . $r_c[0]['cid'] . '-';
        // }

        // $brand_id = 0;
        // $sql_b = "select brand_id,categories from lkt_brand_class where store_id = '$store_id' and lang_code = '$lang_code' order by brand_id asc limit 1 ";
        // $r_b = Db::query($sql_b);
        // if($r_b)
        // {
        //     $brand_id = $r_b[0]['brand_id'];
        // }

        // $r_0 = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0])->whereLike('product_class', '%-'.$cid.'-%')->order('sort','desc')->field('id')->select()->toArray();
        // if($r_0)
        // {
        //     $sql_c1 = "update lkt_product_list set product_class = '$cid0',brand_id = '$brand_id' where store_id = '$store_id' and product_class like '%$cid%' ";
        //     $r_c1 = Db::execute($sql_c1);
        // }

        $cid_r = '-' . $cid . '-';
        $sql_c1 = "update lkt_product_list set product_class = '',brand_id = '0',status = 3 where store_id = '$store_id' and product_class like '%$cid_r%' ";
        $r_c1 = Db::execute($sql_c1);

        $brand_id0 = 0;
        $sql_b = "select brand_id,categories from lkt_brand_class where store_id = '$store_id' and categories like '%,$cid,%' ";
        $r_b = Db::query($sql_b);
        if($r_b)
        {
            foreach($r_b as $k_b => $v_b)
            {
                $brand_id0 = $v_b['brand_id'];
                $categories0 = explode(',',trim($v_b['categories'],','));
                
                foreach($categories0 as $k0 => $v0)
                {
                    if($cid == $v0)
                    {
                        unset($categories0[$k0]);
                    }
                }
                $categories1 = '';
                if($categories0 != array())
                {
                    $categories1 = ',' . implode(',',$categories0) . ',';
                }
                // else
                // {
                //     $categories1 = ',' . $brand_id . ',';
                // }

                $sql_b1 = "update lkt_brand_class set categories = '$categories1' where store_id = '$store_id' and brand_id = '$brand_id0' ";
                $r_b1 = Db::execute($sql_b1);
            }
        }

        foreach ($str_option as $k => $v)
        {
            $res = Tools::del_banner($store_id, $v, 'cid');

            $sql_where = array('store_id'=>$store_id,'cid'=>$v);
            $sql_update = array('recycle'=>1);
            $res = Db::name('product_class')->where($sql_where)->update($sql_update);
            if ($res == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 删除商品分类失败！参数:' . json_encode($sql_where);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '删除了分类ID：'.$v.' 失败',3,$source,$mch_id,$operator_id);
                $message = Lang('product.73');
                echo json_encode(array('code' => 109,'message' => $message));
                exit;
            }
            else
            {
                Db::table('lkt_jump_path')->where('type0',1)->whereLike('parameter','%='.$v)->delete();

                $Jurisdiction->admin_record($store_id, $operator, '删除了分类ID：'.$v,3,$source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 删除商品分类ID为 ' . $v . '成功';
                $this->Log($Log_content);
            }
        }

        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 递归找下级
    public function str_option($store_id, $cid, $str_option)
    {
        $res = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$cid])->field('cid')->select()->toArray();
        if ($res)
        {
            foreach ($res as $k => $v)
            {
                $str_option[] = $v['cid'];
                $str_option = $this->str_option($store_id, $v['cid'], $str_option);
            }
            return $str_option;
        }
        else
        {
            return $str_option;
        }
    }

    // 分类审核列表
    public function auditList($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $name = $array['name']; // 分类名称/供应商名称
        $examine = $array['examine']; // 审核状态 0.待审核 1.审核通过 2.不通过
        $startTime = $array['startTime']; // 查询开始时间
        $endTime = $array['endTime']; // 查询结束时间
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页数据
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商
        $level = "";
        if(isset($array['level']))
        {
            $level = $array['level']; 
        }
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

        $arr = Tools::get_data_dictionary(array('name'=>'商品分类','lang_code'=>$lang_code));

        $list = array();
        $total = 0;

        if($source == 1)
        { // 管理后台
            $condition = " a.store_id = '$store_id' and a.recycle = 0 ";
            if ($name != '')
            {
                $name = Tools::FuzzyQueryConcatenation($name);
                $condition .= " and (a.pname like $name or b.name like $name or s.supplier_name like $name) ";
            }

            if ($examine != '')
            {
                $condition .= " and a.examine = '$examine' ";
            }

            if ($startTime != '')
            {
                $condition .= " and a.add_date >= '$startTime' ";
            }

            if ($endTime != '')
            {
                $condition .= " and a.add_date <= '$endTime' ";
            }

            if($lang_code != '')
            {
                $condition .= " and lang_code = '$lang_code' ";
            }

            $sql0 = "select count(a.cid) as total from lkt_product_class as a left join lkt_mch as b on a.mch_id = b.id left join lkt_supplier as s on a.supplier_id = s.id where $condition ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $sql1 = "select a.*,b.id as supplierId,b.name,s.supplier_name from lkt_product_class as a left join lkt_mch as b on a.mch_id = b.id left join lkt_supplier as s on a.supplier_id = s.id where $condition order by a.add_date desc limit $start,$pagesize ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                foreach($r1 as $k1 => $v1)
                {
                    $v1['lang_name'] = langCode2Name($v1['lang_code']);
                    $sid = $v1['sid'];
                    $v1['img'] = ServerPath::getimgpath($v1['img'], $store_id);

                    $v1['spname'] = '';
                    $r_class = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$sid,'recycle'=>0])->field('pname')->select()->toArray();
                    if($r_class)
                    {
                        $v1['spname'] = $r_class[0]['pname'];
                    }

                    foreach ($arr as $ke => $va)
                    {
                        if ((int)$v1['level'] + 1 == $ke)
                        {
                            $v1['levelFormat'] = $va;
                        }
                    }

                    if($v1['examine'] == 1)
                    {
                        $v1['examineDesc'] = '审核通过';
                    }
                    else if($v1['examine'] == 2)
                    {
                        $v1['examineDesc'] = '审核不通过';
                    }
                    else
                    {
                        $v1['examineDesc'] = '待审核';
                    }

                    if($v1['mch_id'] != 0)
                    {
                        $v1['supplier_name'] = $v1['name'];
                    }
                    else if($v1['supplier_id'] != 0)
                    {
                        $v1['supplier_name'] = $v1['supplier_name'];
                    }
                    else
                    {
                        $v1['supplier_name'] = "平台";
                    }

                    $list[] = $v1;
                }
            }

            $data = array('list'=>$list,'total'=>$total);
        }
        else if($source == 2)
        { // PC店铺端
            $condition = " a.store_id = '$store_id' and a.recycle = 0 and a.mch_id = '$mch_id' ";
            if ($name != '')
            {
                $name = Tools::FuzzyQueryConcatenation($name);
                $condition .= " and (a.pname like $name or b.name like $name) ";
            }

            if ($examine != '')
            {
                $condition .= " and a.examine = '$examine' ";
            }

            if ($startTime != '')
            {
                $condition .= " and a.add_date >= '$startTime' ";
            }

            if ($endTime != '')
            {
                $condition .= " and a.add_date <= '$endTime' ";
            }

            if($lang_code != '')
            {
                $condition .= " and lang_code = '$lang_code' ";
            }

            $sql0 = "select count(a.cid) as total from lkt_product_class as a left join lkt_mch as b on a.mch_id = b.id where $condition ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $sql1 = "select a.*,b.id as supplierId,b.name as supplier_name from lkt_product_class as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.add_date desc limit $start,$pagesize ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                foreach($r1 as $k1 => $v1)
                {
                    $v1['lang_name'] = langCode2Name($v1['lang_code']);
                    $sid = $v1['sid'];
                    $v1['img'] = ServerPath::getimgpath($v1['img'], $store_id);

                    $v1['spname'] = '';
                    $r_class = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$sid,'recycle'=>0])->field('pname')->select()->toArray();
                    if($r_class)
                    {
                        $v1['spname'] = $r_class[0]['pname'];
                    }

                    foreach ($arr as $ke => $va)
                    {
                        if ((int)$v1['level'] + 1 == $ke)
                        {
                            $v1['levelFormat'] = $va;
                        }
                    }

                    if($v1['examine'] == 1)
                    {
                        $v1['examineDesc'] = '审核通过';
                    }
                    else if($v1['examine'] == 2)
                    {
                        $v1['examineDesc'] = '审核不通过';
                    }
                    else
                    {
                        $v1['examineDesc'] = '待审核';
                    }
                    
                    $list[] = $v1;
                }
            }

            $data = array('classInfo'=>$list,'total'=>$total);
        }
        else if($source == 12)
        { // PC供应商
            $condition = " a.store_id = '$store_id' and a.recycle = 0 and a.supplier_id = '$supplier_id' ";
            
            if ($name != '')
            {
                $name = Tools::FuzzyQueryConcatenation($name);
                $condition .= " and (a.pname like $name or b.supplier_name like $name) ";
            }

            if ($level != '')
            {
                $condition .= " and a.level = '$level' ";
            }

            if ($examine != '')
            {
                $condition .= " and a.examine = '$examine' ";
            }

            if($lang_code != '')
            {
                $condition .= " and lang_code = '$lang_code' ";
            }

            $sql0 = "select count(a.cid) as total from lkt_product_class as a left join lkt_supplier as b on a.supplier_id = b.id where $condition ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $sql1 = "select a.*,b.id as supplierId,b.supplier_name from lkt_product_class as a left join lkt_supplier as b on a.supplier_id = b.id where $condition order by a.add_date desc limit $start,$pagesize ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                foreach($r1 as $k1 => $v1)
                {
                    $v1['lang_name'] = langCode2Name($v1['lang_code']);
                    $sid = $v1['sid'];
                    $v1['img'] = ServerPath::getimgpath($v1['img'], $store_id);

                    $v1['spname'] = '';
                    $r_class = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$sid,'recycle'=>0])->field('pname')->select()->toArray();
                    if($r_class)
                    {
                        $v1['spname'] = $r_class[0]['pname'];
                    }

                    foreach ($arr as $ke => $va)
                    {
                        if ((int)$v1['level'] + 1 == $ke)
                        {
                            $v1['levelFormat'] = $va;
                        }
                    }

                    if($v1['examine'] == 1)
                    {
                        $v1['examineDesc'] = '审核通过';
                    }
                    else if($v1['examine'] == 2)
                    {
                        $v1['examineDesc'] = '审核不通过';
                    }
                    else
                    {
                        $v1['examineDesc'] = '待审核';
                    }
                    
                    $list[] = $v1;
                }
            }

            $data = array('classInfo'=>$list,'total'=>$total);
        }

        $message = Lang("Success");
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 分类审核
    public function examine($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $id = $array['id']; // 分类ID
        $status = $array['status']; // 审核状态 0.待审核 1.审核通过 2.不通过
        $refuse_reasons = $array['refuse_reasons']; // 拒绝原因
        $operator_id = $array['operator_id']; // 操作人ID
        $operator = $array['operator']; // 操作人名称
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        // $arr = Tools::get_data_dictionary(array('name'=>'商品分类','lang_code'=>$lang_code));

        $list = array();
        $total = 0;

        $sql0 = "select * from lkt_product_class where store_id = '$store_id' and cid = '$id' and recycle = 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $sid = $r0[0]['sid'];
            $pname = $r0[0]['pname'];
            $level = $r0[0]['level'];
            $lang_code = $r0[0]['lang_code'];
            $country_num = $r0[0]['country_num'];
            if($status == 2)
            { // 拒绝
                $examine = 2;
                if ($refuse_reasons == "")
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '请填写拒绝原因！';
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('supplier.20');
                    echo json_encode(array('code' => 109,'message' => $message));
                    exit;
                }

                $sql1 = "update lkt_product_class set examine = '$examine',remark = '$refuse_reasons' where store_id = '$store_id' and cid = '$id' and recycle = 0 ";
                $r1 = Db::execute($sql1);
                if ($r1 != -1)
                {
                    // $message_11 = "失败：商品分类" . $pname . "审核拒绝";
                    // $message_logging_list11 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>$gongyingshang,'type'=>11,'parameter'=>$id,'content'=>$message_11);
                    // PC_Tools::add_message_logging($message_logging_list11);

                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator . '拒绝店铺分类，ID为: ' . $id . ' 审核成功！';
                    $this->Log($Log_content);
                    Db::commit();
                    $Jurisdiction->admin_record($store_id, $operator, '拒绝了分类ID：'.$id.'，并且名称为：'.$pname.' 的审核',6,$source,$mch_id,$operator_id);
                    $message = Lang('Success');
                    echo json_encode(array('code' => 200,'message' => $message));
                    exit;
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator . '拒绝店铺分类失败！sql:'.$sql1;
                    $this->Log($Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '拒绝了分类ID：'.$id.'，并且名称为：'.$pname.' 的审核失败',6,$source,$mch_id,$operator_id);
                    $message = Lang('supplier.21');
                    echo json_encode(array('code' => 109,'message' => $message));
                    exit;
                }
            }
            else
            { // 通过
                $sql0 = "select * from lkt_product_class where store_id = '$store_id' and cid != '$id' and recycle = 0 and sid = '$sid' and pname = '$pname' and examine = 1 ";
                $r0 = Db::query($sql0);
                if($r0)
                {
                    $examine = 2;
                    $refuse_reasons = "";
                    $sql1 = "update lkt_product_class set examine = '$examine',remark = '$refuse_reasons' where store_id = '$store_id' and cid = '$id' and recycle = 0 ";
                    $r1 = Db::execute($sql1);
                    if ($r1 != -1)
                    {
                        // $message_11 = "失败：商品分类" . $pname . "审核拒绝";
                        // $message_logging_list11 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>$gongyingshang,'type'=>11,'parameter'=>$id,'content'=>$message_11);
                        // PC_Tools::add_message_logging($message_logging_list11);

                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . '拒绝店铺分类，ID为: ' . $id . ' 审核成功！';
                        $this->Log($Log_content);
                        Db::commit();
                        $Jurisdiction->admin_record($store_id, $operator, '拒绝了分类ID：'.$id.'，并且名称为：'.$pname.' 的审核',6,$source,$mch_id,$operator_id);
                        $message = Lang('Success');
                        echo json_encode(array('code' => ERROR_CODE_XTJCGFLMCYCZ,'message' => $message));
                        exit;
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . '拒绝店铺分类失败！sql:'.$sql1;
                        $this->Log($Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '拒绝了分类ID：'.$id.'，并且名称为：'.$pname.' 的审核失败',6,$source,$mch_id,$operator_id);
                        $message = Lang('supplier.21');
                        echo json_encode(array('code' => 109,'message' => $message));
                        exit;
                    }
                }
                else
                {
                    $examine = 1;
                    $sql1 = "update lkt_product_class set examine = '$examine' where store_id = '$store_id' and cid = '$id' and recycle = 0 ";
                    $r1 = Db::execute($sql1);
                    if ($r1 != -1)
                    {
                        $sql_b = "select brand_id,categories from lkt_brand_class where store_id = '$store_id' order by brand_id asc limit 1 ";
                        $r_b = Db::query($sql_b);
                        if($r_b)
                        {
                            $brand_id = $r_b[0]['brand_id'];
                            $categories = $r_b[0]['categories'];
                            if($categories == '')
                            {
                                $categories = ',' . $id . ',';
                            }
                            else
                            {
                                $categories .= $id . ',';
                            }
    
                            $sql_b1 = "update lkt_brand_class set categories = '$categories' where store_id = '$store_id' and brand_id = '$brand_id' ";
                            $r_b1 = Db::execute($sql_b1);
                            if ($r_b1 == -1)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . '通过店铺分类，修改品牌失败！参数:'.json_encode($sql_where1);
                                $this->Log($Log_content);
                                Db::rollback();
                                $Jurisdiction->admin_record($store_id, $operator, '通过了分类ID：'.$id.'，并且名称为：'.$pname.' 的审核失败',6,$source,$mch_id,$operator_id);
                                $message = Lang('supplier.21');
                                echo json_encode(array('code' => 109,'message' => $message));
                                exit;
                            }
                        }
                        
                        $array = array('store_id'=>$store_id,'type0'=>1,'id'=>$id,'name'=>$pname,'lang_code'=>$lang_code,'country_num'=>$country_num);
                        PC_Tools::jump_path($array);
    
                        // $message_11 = "通过：商品分类" . $pname . "审核通过";
                        // $message_logging_list11 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>$gongyingshang,'type'=>11,'parameter'=>$id,'content'=>$message_11);
                        // PC_Tools::add_message_logging($message_logging_list11);
    
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 审核分类成功，添加商品分类成功！';
                        $this->Log($Log_content);
    
                        Db::commit();
                        $Jurisdiction->admin_record($store_id, $operator, '通过了分类ID：'.$id.'，并且名称为：'.$pname.' 的审核',6,$source,$mch_id,$operator_id);
                        $message = Lang('Success');
                        echo json_encode(array('code' => 200,'message' => $message));
                        exit;
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . '通过店铺分类失败！sql:'.$sql1;
                        $this->Log($Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '通过了分类ID：'.$id.'，并且名称为：'.$pname.' 的审核失败',6,$source,$mch_id,$operator_id);
                        $message = Lang('supplier.21');
                        echo json_encode(array('code' => 109,'message' => $message));
                        exit;
                    }
                }
            }
        }

        $message = Lang("Success");
        echo json_encode(array('code' => 200,'message' => $message));
        exit;
    }

    // 获取分类名称
    public static function GetClassName($array)
    {
        $store_id = $array['store_id'];
        $cid = $array['cid']; // 分类ID

        $name = "";
        $r_class = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$cid])->field('pname')->select()->toArray();
        if($r_class)
        {
            $name = $r_class[0]['pname'];
        }

        return $name;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("common/ProductClass.log",$Log_content);
        return;
    }
}   
