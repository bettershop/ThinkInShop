<?php
namespace app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\ServerPath;
use app\common\Product;
use app\common\Tools;

class ProductDrafts
{   
    // 草稿箱列表
    public function get_list($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $page = $array['page']; // 页码
        $pagesize = $array['pagesize']; // 每页多少条数据
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $total = 0;
        $list = array();

        $ObtainCategoriesAndBrands_list = Product::ObtainCategoriesAndBrands($store_id);
        $product_class_arr = $ObtainCategoriesAndBrands_list['product_class_arr'];
        $brand_class_arr = $ObtainCategoriesAndBrands_list['brand_class_arr'];

        $con = " store_id = '$store_id' ";
        if($source == 12)
        {
            $con .= " and supplier_id = '$supplier_id' ";
        }
        else
        {
            $con .= " and mch_id = '$mch_id' ";
        }

        $sql0 = "select count(id) as total from lkt_drafts where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select * from lkt_drafts where $con order by add_time desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $text = $v['text'];
                $v['product_title'] = "";
                $v['imgurls'] = "";
                $v['pname'] = "";
                $v['brand_name'] = "";
                $v['price'] = "";
                $text_array = json_decode($text,true);
                if(isset($text_array['productTitle']))
                {
                    $v['product_title'] = $text_array['productTitle'];
                }
                if(isset($text_array['showImg']))
                {
                    if($text_array['showImg'] != '')
                    {
                        $imgurls = explode(',',$text_array['showImg']);
                        $v['imgurls'] = $imgurls[0];
                    }
                }
                if(isset($text_array['productClassId']))
                {
                    $Tools = new Tools($store_id, 1);
                    $product_class = $Tools->str_option($text_array['productClassId']);
                    // 分类名称
                    $v['pname'] = array_key_exists($product_class, $product_class_arr) ? $product_class_arr[$product_class]:'暂无';
                }
                if(isset($text_array['brandId']))
                {
                    $brand_id = $text_array['brandId'];
                    // 品牌名称
                    $v['brand_name'] = array_key_exists($brand_id, $brand_class_arr) ? $brand_class_arr[$brand_id]:'暂无';
                }
                
                if(isset($text_array['initial']))
                {
                    if($text_array['initial'] != '')
                    {
                        $initial = explode(',',$text_array['initial']);
                        foreach ($initial as $k1 => $v1)
                        {
                            $initial1 = explode('=', $v1); // 转数组
                            if($initial1[0] == 'sj')
                            {
                                $v['price'] = $initial1[1];
                            }
                        }
                    }
                }
                $list[] = $v;
            }
        }
        
        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 添加草稿箱
    public function add($array)
    {
        
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $id = $array['id']; // 草稿箱ID
        $text = $array['text']; // 草稿数据
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商

        $time = date("Y-m-d H:i:s");
        if($id != 0 && $id != '')
        {
            $sql = "update lkt_drafts set text = '$text' where store_id = '$store_id' and mch_id = '$mch_id' and supplier_id = '$supplier_id' and id = '$id' ";
            $r = Db::execute($sql);
            if($r == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 编辑商品草稿失败！sql:'.$sql;
                $this->Log($Log_content);
                $message = Lang('Busy network');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 编辑商品草稿成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
        }
        else
        {
            $sql = "insert into lkt_drafts(store_id,mch_id,supplier_id,text,add_time) value ('$store_id','$mch_id','$supplier_id','$text','$time')";
            $r = Db::execute($sql);
            if($r > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 添加商品草稿成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                echo json_encode(array('code' => 200,'message' => $message));
                exit;
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 添加商品草稿失败！sql:'.$sql;
                $this->Log($Log_content);
                $message = Lang('Busy network');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
        }
    }

    // 编辑页面
    public function edit_page($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $id = $array['id']; // 草稿箱ID
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商

        $time = date("Y-m-d H:i:s");

        $sql = "select text from lkt_drafts where mch_id = '$mch_id' and supplier_id = '$supplier_id' and id = '$id' ";
        $r = Db::query($sql);
        if($r > 0)
        {
            $message = Lang('Success');
            echo json_encode(array('code' => 200,'message' => $message,'data' => $r[0]));
            exit;
        }
        else
        {
            $message = Lang('Busy network');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }
    }

    // 删除草稿箱
    public function del($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id']; // 店铺ID
        $supplier_id = $array['supplier_id']; // 供应商ID
        $id = $array['id']; // 草稿箱ID
        $source = $array['source']; // 1.管理后台 2.PC店铺端 3.移动店铺端 12.PC供应商

        $time = date("Y-m-d H:i:s");

        $r1 = Db::table('lkt_drafts')->whereIn('id',$id)->delete();
        if($r1 > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 添加商品草稿成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array('code' => 200,'message' => $message));
            exit;
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 添加商品草稿失败！sql:'.$sql;
            $this->Log($Log_content);
            $message = Lang('Busy network');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("common/ProductClass.log",$Log_content);
        return;
    }
}   
