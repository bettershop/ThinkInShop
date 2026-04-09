<?php
namespace app\common;
use think\facade\Db;
use app\common\PC_Tools;
use app\common\Plugin\Plugin;
use app\common\Jurisdiction;
use app\common\LaiKeLogUtils;
use app\common\Plugin\MchPublicMethod;

use app\admin\model\AdminModel;
use app\admin\model\ProductListModel;
use app\admin\model\FreightModel;
use app\admin\model\ProductImgModel;
use app\admin\model\ProductClassModel;
use app\admin\model\BrandClassModel;
use app\admin\model\ProLabelModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProductNumberModel;
use app\admin\model\MchModel;
use app\admin\model\CouponActivityModel;
use app\admin\model\UserRuleModel;
use app\admin\model\UserGradeModel;
use app\admin\model\SkuModel;
use app\admin\model\PreSellGoodsModel;
use app\admin\model\SupplierModel;

class Product
{   
    // 商品列表
    public function get_product_list($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $classnotset = 0;
        if(isset($array['classnotset']))
        {
            $classnotset = $array['classnotset'];
        }
        $product_class = $array['product_class'];
        $brandnotset = 0;
        if(isset($array['brandnotset']))
        {
            $brandnotset = $array['brandnotset'];
        }
        $brand_id = $array['brand_id'];
        $active = $array['active'];
        $status = $array['status'];
        $lang_code = "";
        if(isset($array['lang_code']) && $array['lang_code'] != '')
        {
            $lang_code = Tools::get_lang($array['lang_code']);
        }
        $show_adr = $array['show_adr'];
        $label_id = $array['label_id'];
        $examineStatus = $array['examineStatus']; // 审核状态  5.待审核 4.待提交 3 审核失败
        $product_title = $array['product_title'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $mch_status = $array['mch_status'];
        $commodity_type = $array['commodity_type'];
        $parameter = $array['parameter'];
        $source = $array['source'];
        $IsItDescendingOrder = "";
        if(isset($array['IsItDescendingOrder']))
        {
            $IsItDescendingOrder = $array['IsItDescendingOrder']; // 是否降序
        }
        
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $page = 1;
        }

        $ObtainCategoriesAndBrands_list = self::ObtainCategoriesAndBrands($store_id);
        $product_class_arr = $ObtainCategoriesAndBrands_list['product_class_arr'];
        $brand_class_arr = $ObtainCategoriesAndBrands_list['brand_class_arr'];

        $condition = "a.store_id = '$store_id' and a.recycle = 0 and a.mch_id != 0 and a.is_presell = 0 and a.commodity_type = '$commodity_type' and a.gongyingshang = 0 ";
        if($source == 'mch')
        {
            $condition .= " and a.mch_id = '$mch_id' and a.gongyingshang = 0 ";
        }
        $class_list = array();
        if($parameter != '')
        {
            $condition .= " and a.id = '$parameter' ";
            $message_logging_list_8 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$parameter,'type'=>8);
            PC_Tools::message_read($message_logging_list_8);

            $message_logging_list_10 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$parameter,'type'=>10);
            PC_Tools::message_read($message_logging_list_10);
        }

        if ($product_class != 0 && $product_class != '')
        {
            if($classnotset == 1)
            {
                $condition .= " and a.product_class = '' ";
            }
            else
            {
                $Tools = new Tools($store_id, 1);
                $product_class1 = $Tools->str_option( $product_class);

                $condition .= " and a.product_class like '%$product_class1%' ";
                $res = explode('-',trim($product_class1,'-'));
                $class_id0 = $res[0]; //  商品所属分类的顶级
                $shuliang = count($res)-1;
                $class_id1 = $res[$shuliang]; // 商品所属分类
                foreach ($res as $k => $v)
                {
                    $r = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$v,'examine'=>1])->field('cid,pname,level')->select()->toArray();
                    if ($r)
                    {
                        $class_list[] = $r[0];
                    }
                }
            }
        }    

        if ($brand_id != 0 && $brand_id != '')
        {
            if($brandnotset == 1)
            {
                $condition .= " and a.brand_id = 0 ";
            }
            else
            {
                $condition .= " and a.brand_id like '$brand_id' ";
            }
        }

        if($status != '')
        {
            $condition .= " and a.status = $status ";
        }

        if($lang_code != '')
        {
            $condition .= " and a.lang_code = '$lang_code' ";
        }

        if($active != 0 && $active != '')
        {
            $condition .= " and active like '%$active%' ";
        }

        if($label_id != 0 && $label_id != '')
        {
            $condition .= " and s_type like '%,$label_id,%' ";
        }

        if($mch_status == 0)
        { // 待审核列表进入
            if($examineStatus == 5)
            {
                $condition .= " and a.mch_status = 1 ";
            }
            else if($examineStatus == 4)
            {
                $condition .= " and a.mch_status = 4 ";
            }
            else if($examineStatus == 3)
            {
                $condition .= " and a.mch_status = 3 ";
            }
            else
            {
                $condition .= " and a.mch_status != 2 ";
            }
        }
        else
        {
            $condition .= " and a.mch_status = 2 ";
        }

        if ($product_title != '')
        {
            $product_title = Tools::FuzzyQueryConcatenation($product_title);
            $condition .= " and (a.product_title like $product_title or b.name like $product_title ) ";
        }

        if($show_adr != '' )
        {
            if($show_adr == '0')
            {
                $condition .= " and a.show_adr like '%,0,%' ";
            }
            else
            {
                $condition .= " and a.show_adr like '%,".$show_adr.",%' ";
            }
        }

        $list = array();

        if($IsItDescendingOrder == '')
        {
            if($source == 'mch')
            {
                $sort = " a.mch_sort desc,a.upper_shelf_time desc,a.add_date desc ";
            }
            else
            {
                $sort = " a.sort desc,a.add_date desc ";
            }
        }
        else
        {
            $sort = " (a.volume + a.real_volume) desc ";
            if($IsItDescendingOrder == 'asc')
            {
                $sort = " (a.volume + a.real_volume) asc ";
            }
        }

        $sql0 = "select count(a.id) as total from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition ";
        $r_pager = Db::query($sql0);
        $total = $r_pager[0]['total'];

        $sql1 = "select a.id,a.store_id,a.product_number,a.commodity_type,a.product_title,a.subtitle,a.label,a.scan,a.product_class,a.imgurl,a.pro_video,a.video,a.content,a.richList,a.sort,a.add_date,a.upper_shelf_time,a.volume,a.real_volume,a.initial,a.s_type,a.num,a.min_inventory,a.status,a.brand_id,a.is_distribution,a.is_default_ratio,a.keyword,a.weight,a.weight_unit,a.freight,a.is_zhekou,a.separate_distribution,a.recycle,a.gongyingshang,a.is_hexiao,a.active,a.mch_id,a.mch_status,a.search_num,a.publisher,a.is_zixuan,a.source,a.comment_num,a.cover_map,a.class_sort,a.display_position_sort,a.is_presell,a.show_adr,b.name,a.refuse_reasons,a.receiving_form,a.zixuan_id,a.mch_sort,a.lang_code,a.country_num from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by $sort limit $start,$pagesize ";
        $r = Db::query($sql1);
        if($r)
        {
            $total1 = count($r);
            foreach ($r as $key => $value)
            {
                $product = new Product();
                $rew = $product->chaxun($value['id'],$store_id);
                if ($rew == 2)
                { // 有参与插件活动
                    $value['rew'] = 1;
                }
                else if ($rew == 3)
                { // 有未完成的订单
                    $value['rew'] = 2;
                }
                else
                {
                    $value['rew'] = 0;
                }

                $pid = $value['id'];
                $class = $value['product_class'];
                $bid = $value['brand_id'];
                $shop_id = $value['mch_id'];
                $present_price = 0;
                $unit = '';

                // 获取上一条数据的ID
                if($key == 0)
                { // 为当前页面第一条时
                    if ($page)
                    { // 有页码
                        $start1 = $start-1; // 上一页最后一条数据
                        if($start1 < 0)
                        {
                            $value['upper_status'] = false; // 下移
                            $upper_id = '';
                        }
                        else
                        {
                            // 查询上一页最后一条数据
                            $sql2 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $start1,1";
                            $r2 = Db::query($sql2);
                            $upper_id = $r2[0]['id'];
                            $value['upper_status'] = true;
                        }
                    }
                    else
                    {
                        $value['upper_status'] = false; // 下移
                        $upper_id = '';
                    }
                }
                else
                {
                    $key1 = $key-1;
                    $upper_id = $r[$key1]['id']; // 上条数据ID
                    $value['upper_status'] = true;
                }
                // 获取下一条数据的ID
                if($key == $total1-1)
                {  // 为当页面最后一条时
                    if ($page)
                    {  // 有页码
                        if($page == 1)
                        {
                            $sql3 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $pagesize,1";
                        }
                        else
                        {
                            $start2 = $start + $pagesize;
                            $sql3 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $start2,1";
                        }
                    }
                    else
                    {
                        $sql3 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $pagesize,1";
                    }
                    $r3 = Db::query($sql3);
                    if($r3)
                    {
                        $underneath_id = $r3[0]['id']; // 下条数据ID
                    }
                    else
                    {
                        $underneath_id = '';
                    }
                }
                else
                {
                    $key2 = $key+1;
                    $underneath_id = $r[$key2]['id']; // 下条数据ID
                }
                $value['upper_id'] = $upper_id;
                $value['underneath_id'] = $underneath_id;

                $min_inventory = $value['min_inventory'];
                $s_type = explode(',', $value['s_type']);
                $labelList = PC_Tools::getProductLabel(array('store_id'=>$store_id,'s_type'=>$s_type));
                $value['labelList'] = $labelList;

                $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                $value['s_type_list'] = $s_type_list;
                
                $showAdrList = explode(',', trim($value['show_adr'],',')); // 展示位置:1.首页 2.购物车 3.分类 4.我的-推荐

                $value['showAdrList'] = $showAdrList;
                $value['showAdrNameList'] = $showAdrList;

                $value['showName'] = '全部商品';
                if($showAdrList[0] == 1)
                { 
                    $value['showName'] = '首页';
                }
                else if($showAdrList[0] == 2)
                {
                    $value['showName'] = '购物车';
                }
                else if($showAdrList[0] == 3)
                {
                    $value['showName'] = '分类';
                }
                else if($showAdrList[0] == 4)
                {
                    $value['showName'] = '我的-推荐';
                }

                // 分类名称
                $pname = array_key_exists($class, $product_class_arr) ? $product_class_arr[$class]:'顶级';
                // 品牌名称
                $brand_name = array_key_exists($bid, $brand_class_arr) ? $brand_class_arr[$bid]:'暂无';
                $value['pname'] = $pname;
                $value['brand_name'] = $brand_name;
                // 查询商品库存
                $res_n = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('SUM(num) as num')->select()->toArray();
                $value['num'] = $res_n[0]['num'];

                $r_s = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('min(price) as price')->select()->toArray();
                if ($r_s)
                {
                    $present_price = $r_s[0]['price'];
                }
                
                $r_unit = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('unit')->select()->toArray();
                if ($r_unit)
                {
                    $unit = $r_unit[0]['unit'];
                }
                $value['cover_map'] = ServerPath::getimgpath($value['cover_map'],$store_id);
                $value['imgurl'] = ServerPath::getimgpath($value['imgurl'],$store_id);
                $value['unit'] = $unit;
                $value['price'] = round($present_price,2);
                $value['sj'] = round($present_price,2);

                $value['volume'] = $value['volume'] + $value['real_volume'];
                if($value['volume'] < 0)
                {
                    $value['volume'] = 0;
                }
                if($value['status'] == 1)
                {
                    $value['status_name'] = '待上架';
                }
                else if($value['status'] == 2)
                {
                    $value['status_name'] = '上架';
                }
                else if($value['status'] == 3)
                {
                    $value['status_name'] = '下架';
                }
                
                $value['product_title'] = stripslashes($value['product_title']);
                // $value['product_title'] = str_replace("\\'", "'", $value['product_title']) ;//stripslashes
                $value['lang_name'] = Tools::get_lang_name($value['lang_code']);
                $value['country_name'] = Tools::get_country($value['country_num']);
                $list[$key] = (object)$value;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 获取分类及品牌
    public function get_classified_brands($store_id,$class_str,$brand_id,$lang_code = '')
    {
        $list = array();
        $id = 0;

        if ($class_str == '0' || $class_str == '')
        { // 没有选择分类
            // 获取产品类别
            if($lang_code != '')
            {
                $r0 = ProductClassModel::where(['store_id'=>$store_id,'lang_code'=>$lang_code,'recycle'=>0,'sid'=>0,'examine'=>1,'notset'=>0])->order('sort', 'desc')->field('cid,pname,level,notset')->select()->toArray();
            }
            else
            {
                $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0,'examine'=>1,'notset'=>0])->order('sort', 'desc')->field('cid,pname,level,notset')->select()->toArray();
            }
            if ($r0)
            {
                foreach ($r0 as $k => $v)
                {
                    $r0[$k]['status'] = false;
                }
                $list[] = $r0;
                $id = $r0[0]['cid'];
            }
        }
        else
        { // 选择了分类
            $list1 = array();
            // 获取产品类别
            $r0_0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$class_str,'examine'=>1,'notset'=>0])->order('sort', 'desc')->field('cid,sid,pname,level,notset')->select()->toArray();
            if ($r0_0)
            {
                if($r0_0[0]['sid'] == 0)
                {
                    $sid = $class_str;
                }
                else
                {
                    $sid = $r0_0[0]['sid']; // 上级ID
                }
            }

            // 根据分类ID，查询下级
            $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$class_str,'examine'=>1,'notset'=>0])->order('sort', 'desc')->field('cid,pname,level,notset')->select()->toArray();
            if ($r0)
            { // 有下级
                foreach ($r0 as $k => $v)
                {
                    $r0[$k]['status'] = false;
                }
                $list1[] = $r0;
            }
            else
            { // 没有下级
                $list1[] = array();
            }

            $res = $this->superior($store_id,$sid, $list1);
            if ($res != array())
            {
                $num = count($res['list']) - 1;
                $list[] = $res['list'][$num];
                $id = $res['id'];
            }
            else
            {
                $list = $list1;
                $id = $class_str;
            }
        }

        // $brand_list1 = array('brand_id' => '0', 'brand_name' => Lang('product.56'));
        $brand_list = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0,'status'=>0,'examine'=>1])->whereLike('categories','%,' . $id . ',%')->order('sort', 'desc')->field('brand_id,brand_name,sort,notset')->select()->toArray();
        // array_unshift($brand_list, $brand_list1);
        if ($brand_id != 0 || $brand_id != '')
        {
            foreach ($brand_list as $k => $v)
            {
                if ($brand_id == $v['brand_id'])
                {
                    $brand_list[$k]['status'] = true;
                }
                else
                {
                    $brand_list[$k]['status'] = false;
                }
            }
        }

        $list = array('class_list' => $list, 'brand_list' => $brand_list);
        $data = array('list'=> $list);
        return json_encode($data);
    }

    // 添加商品
    public function add_product($store_id,$admin_name,$mch_id,$array,$type0)
    {
        $time = date("Y-m-d H:i:s");

        $Tools = new Tools($store_id, 1);
        $Jurisdiction = new Jurisdiction();
        // 启动事务
        Db::startTrans();

        $supplier_id = 0;
        if(empty($mch_id))
        {
            if($array['supplier_id'] != '')
            {
                $supplier_id = $array['supplier_id'];//供应商id
            }
        }
        $commodity_type = 0;
        if(isset($array['commodity_type']))
        {
            $commodity_type = $array['commodity_type']; // 商品类型 0.实物商品 1.虚拟商品
        }
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = Tools::get_lang($array['lang_code']);
        }
        $country_num = "";
        if(isset($array['country_num']))
        {
            $country_num = $array['country_num'];
        }
        $product_title = $array['product_title']; // 商品标题
        $subtitle = $array['subtitle']; // 副标题
        $keyword = $array['keyword']; // 关键词
        $product_class = $array['product_class']; // 商品分类ID
        $brand_id = $array['brand_id']; // 品牌ID
        $weight = $array['weight']; // 重量
        $weight_unit = $array['weight_unit']; // 重量单位
        $cover_map = $array['cover_map']; // 商品封面图
        $imgurls = $array['imgurls']; // 商品展示图
        $video = ''; // 展示视频
        if(isset($array['video']))
        {
            $video = $array['video']; // 展示视频
        }
        
        $initial = $array['initial']; // 初始值
        $attr = $array['attr']; // 属性
        
        $min_inventory = $array['min_inventory']; // 库存预警
        $freight = $array['freight']; // 运费ID
        $s_type = $array['s_type']; // 显示标签
        $active = $array['active']; // 支持活动
        $show_adr = $array['show_adr']; // 展示位置
        $volume = $array['volume']; // 销量
        $sort = $array['sort']; // 排序
        $mch_sort = 0; // 店铺排序值
        if(isset($array['mch_sort']))
        {
            $mch_sort = $array['mch_sort']; // 店铺排序值
        }

        $pro_video = ''; // 展示视频
        if(isset($array['pro_video']))
        {
            $pro_video = $array['pro_video']; // 商品视频
        }

        $content = $array['content']; // 产品内容
        $richList = $array['richList'];
        $mch_status = $array['mch_status']; // 审核状态：1.待审核，2.审核通过，3.审核不通过，4.暂不审核
        $unit = $array['unit']; // 单位

        $write_off_settings = 2;
        if(isset($array['write_off_settings']))
        {
            $write_off_settings = $array['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
        }
        $write_off_mch_ids = '';
        if(isset($array['write_off_mch_ids']))
        {
            $write_off_mch_ids = $array['write_off_mch_ids']; // 核销门店id  0.全部门店,  1,2,3使用逗号分割
        }
        $is_appointment = 1;
        if(isset($array['is_appointment']))
        {
            $is_appointment = $array['is_appointment']; // 预约时间设置 1.无需预约下单 2.需要预约下单
        }

        $type = $array['type'];
        //商品参数
        if(isset($array['pro_param']))
        {
            $pro_param = $array['pro_param'];
        }
        else
        {
            $pro_param = '';
        }
        //商品介绍
        if(isset($array['pro_introduce']))
        {
            $pro_introduce = $array['pro_introduce'];
        }
        else
        {
            $pro_introduce = '';
        }

        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }

        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }

        $draftsId = 0;
        if(isset($array['draftsId']))
        {
            $draftsId = $array['draftsId']; // 草稿箱ID
        }

        if($source != 2 && $source != 8)
        {
            Tools::National_Language($lang_code,$country_num);
        }

        if (empty($product_title))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品标题不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.8');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        if ($keyword == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 关键词不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.10');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }
        else
        {
            $keyword = trim($keyword, ',');
        }

        if (empty($product_class))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品类别不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.11');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }
        else
        {
            $product_class = $Tools->str_option($product_class);
        }

        if (empty($brand_id))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 品牌不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.12');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        if($commodity_type != 1)
        { // 实物商品
            if (is_numeric($weight))
            {
                if ($weight < 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 重量不能为负数';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.13');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 重量请填写数字';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.14');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
        }

        if(empty($cover_map))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品封面图不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.17');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }
        else
        {
            $cover_map = preg_replace('/.*\//', '', $cover_map);
            if ($cover_map == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品封面图不能为空';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.17');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }

        if (!empty($imgurls))
        {
            if($type0 == '店铺')
            {
                if($imgurls == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品展示图不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.16');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
                $imgurls = explode(',',$imgurls);
                
                if($s_type)
                {
                    $s_type = explode(',',$s_type);
                }
            }
            
            $image = preg_replace('/.*\//', '', $imgurls['0']);
            if ($image == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品展示图不能为空';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.16');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
            unset($imgurls[0]);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品展示图不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.16');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }

        if ($initial)
        {
            $initial1 = explode(',', $initial); // 转数组
            foreach ($initial1 as $k => $v)
            {
                $initial2 = explode('=', $v); // 转数组
                if($initial2[0] == 'stockWarn')
                {
                    $initial2[0] == 'kucun';
                }
                $key[] = $initial2[0];
                $val[] = $initial2[1];
                if ($initial2[0] == 'kucun')
                {
                    $kucun = $initial2[1];
                }
            }
            $initial = array_combine($key, $val); // 创建一个数组，用一个数组的值作为其键名，另一个数组的值作为其值

            foreach ($initial as $k => $v)
            {
                if ($k == 'cbj' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 成本价初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.18');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'yj' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 原价初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.19');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'sj')
                {
                    if($v == '')
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 售价初始值不能为空';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.20');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
                else if ($k == 'unit' && $v == '0')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 单位初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.22');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'kucun' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 库存初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.23');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }

                if ($k == 'cbj' || $k == 'yj' || $k == 'sj' || $k == 'kucun' )
                {
                    if (is_numeric($v))
                    {
                        if ($v < 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值不能为负数';
                            $this->proLog($type0,$Log_content);
                            $message = Lang('product.24');
                            echo json_encode(array('code' => 109,  'message' => $message));
                            exit();
                        }
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值请填写数字';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.25');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
            }

            $initial = serialize($initial);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.26');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }

        //--处理属性
        if($commodity_type != 1)
        { // 实物商品
            if (!is_numeric($min_inventory) || strpos($min_inventory, ".") !== false)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 库存预警请输入整数';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.27');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
            else
            {
                if ($min_inventory <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 库存预警请输入大于0的整数';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.28');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
            }
        }
        else
        {
            $min_inventory = 0;
        }

        $z_num = 0;
        $attributes = array();
        //处理属性
        if (count($attr) == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写属性';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.29');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }
        else
        {
            $foreach_num = 0;
            foreach ($attr as $k => $v)
            {
                $foreach_num++;
                foreach ($v['attr_list'] as $ke => $va)
                {
                    $attr_group_name_id = Tools::attribute_id($store_id, 1, $va['attr_group_name'], 0);
                    if ($attr_group_name_id != 0)
                    { // 当属性名称ID不为0，SKU表里有数据
                        $attr_name_id = Tools::attribute_id($store_id, 2, $va['attr_name'], $attr_group_name_id);
                        if ($attr_name_id == 0)
                        { // 当属性值ID为0，SKU表里没有数据
                            $attr_name_id = $Tools->add_attribute($store_id, $attr_group_name_id, $va['attr_name'], 2, $admin_name, $foreach_num,$type0); // 添加属性值
                        }
                    }
                    else
                    { // 当属性名称ID为0，SKU表里没有数据
                        $attr_group_name_id = $Tools->add_attribute($store_id, 0, $va['attr_group_name'], 1, $admin_name, 0,$type0); // 添加属性名
                        $attr_name_id = $Tools->add_attribute($store_id, $attr_group_name_id, $va['attr_name'], 2, $admin_name, $foreach_num,$type0); // 添加属性值
                    }
                    $attr[$k]['attr_list'][$ke]['attr_group_name'] = $va['attr_group_name'] . '_LKT_' . $attr_group_name_id; // 拼接属性名
                    $attr[$k]['attr_list'][$ke]['attr_name'] = $va['attr_name'] . '_LKT_' . $attr_name_id; // 拼接属性值
                }
            }

            foreach ($attr as $key => $value)
            {
                $attr_list = $value['attr_list'];
                $attr_list_arr = array();
                $attr_list_srt = '';
                foreach ($attr_list as $k => $v)
                {
                    $attr_list_arr[$v['attr_group_name']] = $v['attr_name'];
                    $attr_list_srt .= substr($v['attr_group_name'], 0, strpos($v['attr_group_name'], '_')) . '-' . substr($v['attr_name'], 0, strpos($v['attr_name'], '_'));
                }

                if($commodity_type != 1)
                { // 实物商品
                    if((int)$min_inventory > (int)$value['kucun'])
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 规格库存低于预警值，请先修改库存！';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.30');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                    $attributes[$key]['write_off_num'] = 0;
                }
                else
                { // 虚拟商品
                    if(isset($value['kucun']))
                    {
                        if(!is_numeric($value['kucun']))
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的库存不为数字！';
                            $this->proLog($type0,$Log_content);
                            $message = Lang('product.100');
                            echo json_encode(array('code' => 51054,  'message' => $message));
                            exit();
                        }
                        if($write_off_settings == 2)
                        {
                            $attributes[$key]['write_off_num'] = 1;
                        }
                        else
                        {
                            $attributes[$key]['write_off_num'] = $value['kucun'];
                        }
                    }
                    else
                    {
                        $attributes[$key]['write_off_num'] = 1;
                    }
                    $value['kucun'] = 1000000;
                }

                $z_num += $value['kucun'];
                $value['total_num'] = $value['kucun'];
                
                if(isset($value['img']))
                {
                    if ($value['img'] == '')
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的属性图片未上传';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.31');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的属性图片未上传';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.31');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                $attributes[$key]['img'] = preg_replace('/.*\//', '', $value['img']);
                $attributes[$key]['unit'] = $unit;
                
                //价格判断
                foreach ($value as $cvkey => $cvvalue)
                {
                    if (!is_array($cvvalue))
                    {
                        if($cvkey != 'bar_code' && $cvkey != 'cid')
                        {
                            if (empty($cvvalue) && $cvvalue != 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 请完善属性';
                                $this->proLog($type0,$Log_content);
                                $message = Lang('product.32');
                                echo json_encode(array('code' => 109,  'message' => $message));
                                exit();
                            }
                        }
                    }
                }

                if(!is_numeric($value['cbj']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的成本价不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.33');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                if(!is_numeric($value['yj']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的原价不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.34');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                if(!is_numeric($value['sj']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的售价不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.35');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                if(!is_numeric($value['kucun']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的库存不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.36');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                $costprice = $value['cbj'];
                $price = $value['sj'];
                $yprice = $value['yj'];
                if ($costprice >= $price)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '上传商品时，成本价不能大于售价！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.37');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
                if(!$mch_id)
                {
                    // if($yprice >= $price)
                    // {
                    //     $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '上传商品时，供货价不能大于售价！';
                    //     $this->proLog($type0,$Log_content);
                    //     $message = Lang('product.95');
                    //     echo json_encode(array('code' => 109, 'message' => $message));
                    //     exit;
                    // }
                    if($yprice <= $costprice)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '上传商品时，成本价不能大于供货价！';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.96');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                }
                $value['attribute'] = serialize($attr_list_arr);
                $value = Tools::array_key_remove($value, 'attr_list');
                $attributes[$key]['costprice'] = $value['cbj'];
                $attributes[$key]['yprice'] = $value['yj'];
                $attributes[$key]['price'] = $value['sj'];
                $attributes[$key]['msrp'] = $value['sj'];
                $attributes[$key]['num'] = $value['kucun'];
                $attributes[$key]['total_num'] = $value['total_num'];
                $attributes[$key]['min_inventory'] = $min_inventory;
                $attributes[$key]['attribute'] = $value['attribute'];
            }
        }

        if($commodity_type != 1)
        { // 实物商品
            if ($freight == 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择运费模板名称';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.38');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
        }

        $s_type = $s_type ? $s_type : array();
        if (count($s_type) == 0)
        {
            $s_type = 0;
        }
        else
        {
            $s_type = ',' . implode(",", $s_type) . ',';
        }
    
        $active = 1;
        if($show_adr == '')
        {
            $show_adr = 0;
        }
        $show_adr = ',' . $show_adr . ',';

        //--处理属性
        if($volume != '' )
        {
            if (!is_numeric($volume) || strpos($volume, ".") !== false)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 虚拟销量输入整数';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.40');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
            else
            {
                if ($volume < 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 虚拟销量请输入大于等于0的整数';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.41');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
            }
        }
        else
        {
            $volume = 0;
        }
        
        $shop_name = '';
        if($mch_id)
        {
            $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name')->select()->toArray();
            if($r_mch)
            {
                $shop_name = $r_mch[0]['name'];
            }
        }
        else
        {
            $r_mch = SupplierModel::where(['store_id'=>$store_id,'id'=>$supplier_id])->field('supplier_name')->select()->toArray();
            if($r_mch)
            {
                $shop_name = $r_mch[0]['supplier_name'];
            }
        }
        
        if($sort == '' )
        {
            $r_pro = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0])->max('sort');
            $sort = $r_pro + 1;
        }

        if($mch_sort == '' || $mch_sort == 0)
        {
            $r_pro1 = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0])->max('mch_sort');
            $mch_sort = $r_pro1 + 1;
        }

        $data_sql = array('store_id'=>$store_id,'commodity_type'=>$commodity_type,'product_title'=>$product_title,'subtitle'=>$subtitle,'scan'=>'','product_number'=>'','keyword'=>$keyword,'product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'cover_map'=>$cover_map,'imgurl'=>$image,'video'=>$video,'initial'=>$initial,'min_inventory'=>$min_inventory,'freight'=>$freight,'s_type'=>$s_type,'active'=>$active,'show_adr'=>$show_adr,'volume'=>$volume,'sort'=>$sort,'mch_sort'=>$mch_sort,'pro_video'=>$pro_video,'content'=>$content,'richList'=>$richList,'num'=>$z_num,'add_date'=>$time,'is_distribution'=>0,'distributor_id'=>0,'mch_id'=>$mch_id,'pro_introduce'=>$pro_introduce,'pro_param'=>$pro_param,'mch_status'=>$mch_status,'publisher'=>$admin_name,'write_off_settings'=>$write_off_settings,'write_off_mch_ids'=>$write_off_mch_ids,'is_appointment'=>$is_appointment,'gongyingshang'=>$supplier_id,'lang_code'=>$lang_code,'country_num'=>$country_num);
        if(empty($mch_id))
        {
            if($mch_status == 2)
            {
                $data_sql['status'] = 2;
                $data_sql['examine_time'] = $time;
            }
        }
        $id1 = Db::name('product_list')->insertGetId($data_sql);
        if ($id1 >= 1)
        {
            if ($imgurls)
            {
                $arrimg = array();
                $rf = ProductImgModel::where(['product_id'=>$id1])->field('product_url')->select()->toArray();
                if ($rf)
                {
                    foreach ($rf as $key => $fs)
                    {
                        $key_rf = $fs['product_url'];
                        $arrimg[$key_rf] = $fs['product_url'];
                    }
                }

                if (count($imgurls) > 4)
                {
                    $Jurisdiction->admin_record($store_id, $operator, '产品展示图数量超出限制',1,$source,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 产品展示图数量超出限制';
                    $this->proLog($type0,$Log_content);
                    // 回滚事务
                    Db::rollback();
                    $message = Lang('product.42');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }

                foreach ($imgurls as $key => $file)
                {
                    $imgsURL_name = preg_replace('/.*\//', '', $file);
                    if (array_key_exists($imgsURL_name, $arrimg))
                    {
                        unset($arrimg[$imgsURL_name]);
                    }

                    $data_img = array('product_url'=>$imgsURL_name,'product_id'=>$id1,'add_date'=>$time,'seller_id'=>$admin_name);
                    $r = Db::name('product_img')->save($data_img);
                    if ($r < 1)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '产品展示图上传失败',1,$source,$mch_id,$operator_id);
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 产品展示图上传失败！参数:'. json_encode($data_img);
                        $this->proLog($type0,$Log_content);
                        // 回滚事务
                        Db::rollback();
                        $message = Lang('product.43');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }
                }
                if (!empty($arrimg))
                {
                    foreach ($arrimg as $keys => $fss)
                    {
                        $r = Db::table('lkt_product_img')->where('product_url',$fss)->delete();
                    }
                }
            }

            foreach ($attributes as $ke => $va)
            {
                $data_attribute = array('pid'=>$id1,'costprice'=>$va['costprice'],'yprice'=>$va['yprice'],'price'=>$va['price'],'msrp'=>$va['msrp'],'num'=>$va['num'],'total_num'=>$va['total_num'],'min_inventory'=>$min_inventory,'attribute'=>$va['attribute'],'img'=>$va['img'],'unit'=>$va['unit'],'ctime'=>$time,'write_off_num'=>$va['write_off_num']);
                $r_attribute = Db::name('configure')->insertGetId($data_attribute);
                if ($r_attribute < 1)
                {
                    $attributes1 = json_encode($va);
                    $Jurisdiction->admin_record($store_id, $operator, '属性数据添加失败',1,$source,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 属性数据添加失败！属性数据:'.$attributes1;
                    $this->proLog($type0,$Log_content);
                    // 回滚事务
                    Db::rollback();
                    $message = Lang('product.44');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
                $num = $va['num'];
                // 在库存记录表里，添加一条入库信息
                $content = $admin_name . '增加商品总库存' . $num;
                $data_stock0 = array('store_id'=>$store_id,'product_id'=>$id1,'attribute_id'=>$r_attribute,'total_num'=>$va['total_num'],'flowing_num'=>$va['num'],'type'=>0,'add_date'=>$time,'content'=>$content);
                $r_stock0 = Db::name('stock')->insertGetId($data_stock0);
                if($r_stock0 < 1)
                {
                    $Jurisdiction->admin_record($store_id, $operator, '库存记录添加失败',1,$source,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 库存记录添加失败！参数:'.json_encode($data_stock0);
                    $this->proLog($type0,$Log_content);
                    // 回滚事务
                    Db::rollback();
                    $message = Lang('product.45');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
                if ($min_inventory >= $num)
                { // 当属性库存低于等于预警值
                    $content1 = '预警';
                    // 在库存记录表里，添加一条预警信息
                    $data_stock1 = array('store_id'=>$store_id,'product_id'=>$id1,'attribute_id'=>$r_attribute,'total_num'=>$va['total_num'],'flowing_num'=>$va['num'],'type'=>2,'add_date'=>$time,'content'=>$content1);
                    $r_stock1 = Db::name('stock')->insertGetId($data_stock1);
                    if($r_stock1 < 1)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '库存记录添加失败',1,$source,$mch_id);
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 库存记录添加失败！参数:'.json_encode($data_stock1);
                        $this->proLog($type0,$Log_content);
                        // 回滚事务
                        Db::rollback();
                        $message = Lang('product.45');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }

                    $message_9 = "商品ID为" . $id1 . "的商品库存不足，请尽快补充库存";
                    $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>$supplier_id,'type'=>9,'parameter'=>$r_attribute,'content'=>$message_9);
                    PC_Tools::add_message_logging($message_logging_list9);
                }
            }

            $array = array('store_id'=>$store_id,'type0'=>2,'id'=>$id1,'name'=>$product_title,'lang_code'=>$lang_code,'country_num'=>$country_num);
            PC_Tools::jump_path($array);

            $Jurisdiction->admin_record($store_id, $operator, '添加了商品ID：' . $id1,1,$source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 添加商品' . $product_title . '成功';
            $this->proLog($type0,$Log_content);

            if($mch_id)
            {
                if($mch_status == 1)
                {
                    $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1])->field('shop_id')->select()->toArray();
                    $shop_id = $r_admin[0]['shop_id'];

                    $message_7 = "商品id为:". $id1 . "商品名称为:" . $product_title . "的商品需要管理员审核";
                    $message_logging_list7 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'gongyingshang'=>0,'type'=>7,'parameter'=>$id1,'content'=>$message_7,'source'=>'mch');
                    PC_Tools::add_message_logging($message_logging_list7);
                }
            }
            else
            {
                if($mch_status == 1)
                {
                    $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1])->field('shop_id')->select()->toArray();
                    $shop_id = $r_admin[0]['shop_id'];

                    $message_7 = "供应商商品，商品id为:" . $id1 . "商品名称为:" . $product_title . "的商品需要管理员审核";
                    $message_logging_list7 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'gongyingshang'=>0,'type'=>7,'parameter'=>$id1,'content'=>$message_7,'source'=>'supplier');
                    PC_Tools::add_message_logging($message_logging_list7);
                }
            }

            if($draftsId != 0 && $draftsId != '')
            {
                $sql_d = Db::table('lkt_drafts')->where('id',$draftsId)->delete();
            }

            // 提交事务
            Db::commit();
            $message = Lang('Success');
            echo json_encode(array('code' => 200,  'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '添加商品失败',1,$source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 添加商品失败！参数:'.json_encode($data_sql);
            $this->proLog($type0,$Log_content);
            // 回滚事务
            Db::rollback();
            $message = Lang('product.47');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }
    }

    // 编辑页面
    public function edit_page($store_id,$admin_name,$mch_id,$id,$type,$roomId = 0)
    {
        if($type != '供应商')
        {
            $message_logging_list_8 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$id,'type'=>8);
            PC_Tools::message_pop_up($message_logging_list_8);
            PC_Tools::message_read($message_logging_list_8);
            $message_logging_list_10 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$id,'type'=>10);
            PC_Tools::message_pop_up($message_logging_list_10);
            PC_Tools::message_read($message_logging_list_10);
        }
        else
        {
            $message_logging_list_8 = array('store_id'=>$store_id,'supplier_id'=>$mch_id,'parameter'=>$id,'type'=>8);
            PC_Tools::message_pop_up_sup($message_logging_list_8);
            PC_Tools::message_read_sup($message_logging_list_8);
            $message_logging_list_10 = array('store_id'=>$store_id,'supplier_id'=>$mch_id,'parameter'=>$id,'type'=>10);
            PC_Tools::message_pop_up_sup($message_logging_list_10);
            PC_Tools::message_read_sup($message_logging_list_10);
        }
        
        // 根据产品id，查询产品产品信息
        $r = ProductListModel::where(['store_id'=>$store_id,'id'=>$id])->select()->toArray();
        if ($r)
        {
            $commodity_type = $r[0]['commodity_type']; // 商品类型 0.实物商品 1.虚拟商品
            $product_title = stripslashes($r[0]['product_title']); // 产品标题
            $r[0]['product_title'] = $product_title;
            $subtitle = $r[0]['subtitle']; // 副标题
            $product_class = $r[0]['product_class']; // 产品类别
            $brand_id = $r[0]['brand_id']; // 产品品牌
            $keyword = $r[0]['keyword']; // 关键词
            $weight = $r[0]['weight']; // 关键词
            $weight_unit = $r[0]['weight_unit']; // 关键词
            $status = $r[0]['status']; // 上下架状态
            $min_inventory = $r[0]['min_inventory']; // 库存预警
            $volume = $r[0]['volume']; // 虚拟销量
            $content = $r[0]['content']; // 产品内容
            $imgurl = ServerPath::getimgpath($r[0]['imgurl'], $store_id); //图片
            $cover_map = ServerPath::getimgpath($r[0]['cover_map'], $store_id); //图片
            $richList = $r[0]['richList'];

            $initial = $r[0]['initial'];
            $s_type = trim($r[0]['s_type'],',');
            $show_adr = $r[0]['show_adr'];
            $freight_id = $r[0]['freight'];
            $shop_id = $r[0]['mch_id'];

            $active = str_replace(",",'',$r[0]['active']);

            $video = $r[0]['video']; // 展示视频
            $pro_video = $r[0]['pro_video']; // 商品视频

            $write_off_settings = $r[0]['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
            $write_off_mch_ids = $r[0]['write_off_mch_ids']; // 核销门店id  0全部门店,  1,2,3使用逗号分割
            $is_appointment = $r[0]['is_appointment']; // 预约时间设置 1.无需预约下单 2.需要预约下单
            $write_off_mch_names = '';
            if($write_off_mch_ids == 0 || $write_off_mch_ids == '')
            {
                $write_off_mch_names = '全部';
            }
            else
            {
                $sql_sm = "select name from lkt_mch_store where id in ($write_off_mch_ids)";
                $r_sm = Db::query($sql_sm);
                if($r_sm)
                {
                    foreach($r_sm as $k_sm => $v_sm)
                    {
                        $write_off_mch_names .= $v_sm['name'] . ',';
                    }

                    $write_off_mch_names = trim($write_off_mch_names,',');
                }
            }
            $lang_code = $r[0]['lang_code']; // 语种
            $country_num = $r[0]['country_num']; // 所属国家
            $country_name = Tools::get_country_name($country_num);
            $lang_name = Tools::get_lang_name($lang_code);
        }
        else
        {
            $message = Lang('Illegal invasion');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }

        $freight_list = array();
        $freight_list1 = array();
        if($type == '供应商')
        {
            $r_freight = FreightModel::where(['store_id'=>$store_id,'supplier_id'=>$mch_id,'lang_code'=>$lang_code])->field('id,name')->order('id', 'asc')->select()->toArray();
        }
        else
        {
            $r_freight = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code])->field('id,name')->order('id', 'asc')->select()->toArray();
        }

        if ($r_freight)
        {
            foreach ($r_freight as $k => $v)
            {
                if ($freight_id == $v['id'])
                {
                    $v['selected'] = true;
                }
                else
                {
                    $v['selected'] = false;
                }
                $freight_list[] = $v;
            }
        }

        $r_freight1 = FreightModel::where(['store_id'=>$store_id,'id'=>$freight_id])->field('id,name')->order('id', 'asc')->select()->toArray();
        if($r_freight1)
        {
            $freight_list1 = array('id'=>$r_freight1[0]['id'],'name'=>$r_freight1[0]['name']);
        }

        $imgurls = ProductImgModel::where(['product_id'=>$id])->select()->toArray();
        if ($imgurls)
        {
            foreach ($imgurls as $k => $v)
            {
                $imgurls[$k] = ServerPath::getimgpath($v['product_url'], $store_id);
            }
        }

        $brand_name = '';
        $brand_class_list1 = array();
        $class_id1 = '';
        $class = array();

        // 商品分类
        $res = explode('-', trim($product_class, '-'));
        $class_id0 = $res[0]; //  商品所属分类的顶级
        $shuliang = count($res) - 1;
        $class_id1 = $res[$shuliang]; // 商品所属分类
        foreach ($res as $k => $v)
        {
            $r_class = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$v,'recycle'=>0,'examine'=>1,'lang_code'=>$lang_code,'notset'=>0])->select()->toArray();
            if (count($r_class) > 0)
            {
                $class[] = $r_class[0];
            }
        }

        // 产品品牌
        $brand_list = BrandClassModel::where(['store_id'=>$store_id,'status'=>0,'recycle'=>0,'examine'=>1,'lang_code'=>$lang_code])->whereLike('categories','%,' . $class_id0 . ',%')->order('sort','desc')->field('brand_id,brand_name')->select()->toArray();
        if($brand_list)
        {
            $brand_name = $brand_list[0]['brand_name'];
        }
        
        // 产品品牌
        $brand_list1 = BrandClassModel::where(['brand_id'=>$brand_id])->select()->toArray();
        if($brand_list1)
        {
            $brand_class_list1 = $brand_list1[0];
        }
        
        $imgurls0 = array();
        $imgurls0[] = $imgurl;
        foreach ($imgurls as $k => $v)
        {
            $imgurls0[] = $v;
        }
        $imgurls = $imgurls0;
       

        $attr_group_list = array();
        $checked_attr_list = array();
        $strArr = array();
        //-----查询规格数据
        $res_size = ConfigureModel::where(['pid'=>$id,'recycle'=>0])->select()->toArray();
        if ($res_size)
        {
            if ($res_size[0]['attribute'] != '')
            {
                $arrar_t = unserialize($res_size[0]['attribute']);
                foreach ($arrar_t as $key => $value)
                {
                    if (strpos($key, '_LKT_') !== false)
                    {
                        $key = substr($key, 0, strrpos($key, "_LKT"));
                    }
                    // if($key == '默认')
                    // {
                    //     break;
                    // }
                    $attr_group_list[] = array('attr_group_name' => $key, 'attr_list' => [], 'attr_all' => []);
                }
                if($attr_group_list != array())
                {
                    foreach ($res_size as $k => $v)
                    {
                        $attribute = unserialize($v['attribute']); // 属性
                        $attr_lists = array();
                        //列出属性名
                        foreach ($attribute as $key => $value)
                        {
                            if (strpos($key, '_LKT_') !== false)
                            {
                                $key0 = trim(strrchr($key, '_'),'_');
                                $value0 = trim(strrchr($value, '_'),'_');
                                // $strArr[] = array('id0'=>$key0,'id1'=>$value0);
                                $key = substr($key, 0, strrpos($key, "_LKT"));
                                $value = substr($value, 0, strrpos($value, "_LKT"));

                                $strArr[] = array('id'=>$key0,'name'=>$key,'sid'=>$value0,'sname'=>$value,'status'=>false,'label'=>$key . ':' . $value);
                            }
                            foreach ($attr_group_list as $keya => $valuea)
                            {
                                if ($key == $valuea['attr_group_name'])
                                {
                                    if (!in_array($value, $attr_group_list[$keya]['attr_all']))
                                    {
                                        if ($status == 1)
                                        {
                                            $attr_list = array('attr_name' => $value, 'status' => true);
                                        }
                                        else
                                        {
                                            $attr_list = array('attr_name' => $value, 'status' => false);
                                        }
                                        array_push($attr_group_list[$keya]['attr_list'], $attr_list);
                                        array_push($attr_group_list[$keya]['attr_all'], $value);
                                    }
                                }
                            }
                            $attr_lists[] = array('attr_id' => '', 'attr_group_name' => $key, 'attr_name' => $value);
                        }
                        if($commodity_type == 1)
                        { // 实物商品
                            $v['num'] = $v['write_off_num'];
                        }

                        if($commodity_type == 2 )
                        {
                            if($type == '平台' || $type == '店铺移动端')
                            {
                                $xl_num_ = 0;
                                $sql_x = "select sum(xl_num) as xl_num from lkt_living_product where store_id = '$store_id' and pro_id = '$id' and config_id = " . $v['id'];
                                $r_x = Db::query($sql_x);
                                if($r_x)
                                {
                                    $xl_num_ = $r_x[0]['xl_num'];
                                }
                                
                                $checked_attr_list[] = array('attr_list' => $attr_lists, 'cbj' => (float)$v['costprice'], 'yj' => (float)$v['yprice'], 'sj' => (float)$v['price'], 'kucun' => $v['num'], 'unit' => $v['unit'], 'bar_code' => $v['bar_code'], 'img' => ServerPath::getimgpath($v['img'], $store_id), 'cid' => $v['id'], 'msrp' => (float)$v['msrp'], 'commission' => $v['commission'], 'live_price' =>(float)$v['live_price'],'xl_num'=>$xl_num_,'num'=>0,'sy_num'=>0);
                            }
                            else
                            {
                                $xl_num_ = 0;
                                $num_ = 0;
                                $sy_num_ = 0;
                                $live_price_ = '0.00';
                               
                                $sql_x = "select live_price,total_num,num,xl_num from lkt_living_product where store_id = '$store_id' and living_id = '$roomId' and pro_id = '$id' and config_id = " . $v['id'];
                                $r_x = Db::query($sql_x);
                                if($r_x)
                                {
                                    $xl_num_ = $r_x[0]['xl_num'];
                                    $num_ = $r_x[0]['total_num'];
                                    $sy_num_ = $r_x[0]['num'];
                                    $live_price_ = $r_x[0]['live_price'];
                                }
    
                                $checked_attr_list[] = array('attr_list' => $attr_lists, 'cbj' => (float)$v['costprice'], 'yj' => (float)$v['yprice'], 'sj' => (float)$v['live_price'], 'kucun' => $v['num'], 'unit' => $v['unit'], 'bar_code' => $v['bar_code'], 'img' => ServerPath::getimgpath($v['img'], $store_id), 'cid' => $v['id'], 'msrp' => (float)$v['msrp'], 'commission' => $v['commission'], 'live_price' =>(float)$live_price_,'xl_num'=>$xl_num_,'num'=>$num_,'sy_num'=>$sy_num_);
                            }
                        }
                        else
                        {
                            $checked_attr_list[] = array('attr_list' => $attr_lists, 'cbj' => (float)$v['costprice'], 'yj' => (float)$v['yprice'], 'sj' => (float)$v['price'], 'kucun' => $v['num'], 'unit' => $v['unit'], 'bar_code' => $v['bar_code'], 'img' => ServerPath::getimgpath($v['img'], $store_id), 'cid' => $v['id'], 'msrp' => (float)$v['msrp']);
                        }
                    }

                    foreach ($attr_group_list as $key => $value)
                    {
                        $attr_group_list[$key] = Tools::array_key_remove($attr_group_list[$key], 'attr_all');
                    }
                }
            }
        }

        $strArr = Tools::assoc_unique($strArr,'sid');
        // $strArr = Tools::assoc_unique($strArr,'id1');
        $arr = explode(',', $s_type);
        $sp_type = array();
        if ($show_adr)
        {
            $show_adr0 = explode(',', $show_adr);
        }
        else
        {
            $show_adr0 = array(0);
        }
        $show_adr = array();
        //-------查询规格数据
        $Plugin_arr = array();
        $Plugin = new Plugin();
        $Plugin_arr1 = $Plugin->pro_Plugin($store_id,$active);
        foreach ($Plugin_arr1 as $k_p => $v_p)
        {
            $Plugin_arr['active'][] = $v_p;
        }

        $r_sp_type = ProLabelModel::where(['store_id'=>$store_id,'lang_code'=>$lang_code])->order('add_time','desc')->field('id,name')->select()->toArray();
        if ($r_sp_type)
        {
            foreach ($r_sp_type as $k => $v)
            {
                if (in_array($v['id'], $arr))
                {
                    $sp_type[] = array('name' => $v['name'], 'value' =>$v['id'], 'status' => true);
                }
                else
                {
                    $sp_type[] = array('name' => $v['name'], 'value' =>$v['id'], 'status' => false);
                }
            }
        }

        $show_adr = Tools::get_data_dictionary(array('name'=>'商品展示位置','lang_code'=>$lang_code,'show_adr'=>$show_adr0));

        if ($initial != '')
        {
            $initial = unserialize($initial);
            $initial['cbj']= (float)$initial['cbj'];
            $initial['sj']= (float)$initial['sj'];
            $initial['yj']= (float)$initial['yj'];
            if(!isset($initial['stockWarn']))
            {
                $initial['stockWarn'] = $min_inventory;
            }
        }
        else
        {
            $initial = array();
        }

        $unit = Tools::get_data_dictionary(array('name'=>'单位','lang_code'=>$lang_code));

        $distributors1 = array('id'=>0,'name'=>'会员专区商品绑定等级');

        $data = array('mch_id'=>$shop_id,'list' => $r[0],'commodity_type'=>$commodity_type,'product_title'=>$product_title,'subtitle'=>$subtitle,'keyword'=>$keyword,'weight'=>$weight,'weight_unit'=>$weight_unit,'product_number'=>'','class_id'=>$class_id1,'ctypes'=>$class,'brand_class'=>$brand_list,'brand_id'=>$brand_id,'imgurls'=>$imgurls,'initial'=>$initial,'status'=>$status,'unit'=>$unit,'attr_group_list'=>$attr_group_list,'checked_attr_list'=>$checked_attr_list,'min_inventory'=>$min_inventory,'freight_list'=>$freight_list,'sp_type'=>$sp_type,'active'=>$active,'Plugin_arr'=>$Plugin_arr,'show_adr'=>$show_adr,'content'=>$content,'brand_name'=>$brand_name,'brand_class_list1'=>$brand_class_list1,'freight_list1'=>$freight_list1,'distributors'=>'','distributors1'=>$distributors1, 'status' => $status,'richList' => $richList,'strArr'=>$strArr,'cover_map'=>$cover_map,'volume'=>$volume,'video'=>$video,'pro_video'=>$pro_video,'write_off_settings'=>$write_off_settings,'write_off_mch_ids'=>$write_off_mch_ids,'is_appointment'=>$is_appointment,'write_off_mch_names'=>$write_off_mch_names,'lang_code'=>$lang_code,'country_num'=>$country_num,'country_name'=>$country_name,'lang_name'=>$lang_name);
        return json_encode($data);
    }
    
    // 编辑商品
    public function edit_product($store_id,$admin_name,$mch_id,$array,$type0)
    {
        // 1.开启事务
        Db::startTrans();
        $Tools = new Tools($store_id, 1);
        $Jurisdiction = new Jurisdiction();
        $time = date("Y-m-d H:i:s");
        
        $id = $array['id'];
        $supplier_id = 0;
        if(empty($mch_id))
        {
            if($array['supplier_id'] != '')
            {
                $supplier_id = $array['supplier_id'];//供应商id
            }
        }
        $commodity_type = 0;
        if(isset($array['commodity_type']))
        {
            $commodity_type = $array['commodity_type']; // 商品类型 0.实物商品 1.虚拟商品
        }
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = Tools::get_lang($array['lang_code']);
        }
        $country_num = "";
        if(isset($array['country_num']))
        {
            $country_num = $array['country_num'];
        }
        $product_title = $array['product_title']; // 商品标题
        $subtitle = $array['subtitle']; // 副标题
        $keyword = $array['keyword']; // 关键词
        $product_class = $array['product_class']; // 商品分类ID
        $brand_id = $array['brand_id']; // 品牌ID
        $weight = $array['weight']; // 重量
        $weight_unit = $array['weight_unit']; // 重量单位
        $cover_map = $array['cover_map']; // 商品封面图
        $imgurls = $array['imgurls']; // 商品展示图
        $video = ''; // 展示视频
        if(isset($array['video']))
        {
            $video = $array['video']; // 展示视频
        }

        $initial = $array['initial']; // 初始值
        $attr = $array['attr']; // 属性
        
        $min_inventory = $array['min_inventory']; // 库存预警
        $freight = $array['freight']; // 运费ID
        $s_type = $array['s_type']; // 显示标签
        $active = $array['active']; // 支持活动
        $show_adr = $array['show_adr']; // 展示位置
        $volume = $array['volume']; // 销量
        $sort = $array['sort']; // 排序
        $mch_sort = 0; // 店铺排序值
        if(isset($array['mch_sort']))
        {
            $mch_sort = $array['mch_sort']; // 店铺排序值
        }
        $pro_video = ''; // 展示视频
        if(isset($array['pro_video']))
        {
            $pro_video = $array['pro_video']; // 商品视频
        }

        $content = $array['content']; // 产品内容
        $richList = $array['richList'];
        $mch_status = $array['mch_status']; // 审核状态：1.待审核，2.审核通过，3.审核不通过，4.暂不审核
        $unit = $array['unit']; // 单位

        $write_off_settings = 2;
        if(isset($array['write_off_settings']))
        {
            $write_off_settings = $array['write_off_settings']; // 核销设置 1.线下核销 2.无需核销
        }
        $write_off_mch_ids = '';
        if(isset($array['write_off_mch_ids']))
        {
            $write_off_mch_ids = $array['write_off_mch_ids']; // 核销门店id  0.全部门店,  1,2,3使用逗号分割
        }
        $is_appointment = 1;
        if(isset($array['is_appointment']))
        {
            $is_appointment = $array['is_appointment']; // 预约时间设置 1.无需预约下单 2.需要预约下单
        }

        $type = $array['type'];
        //商品参数
        if(isset($array['pro_param']))
        {
            $pro_param = $array['pro_param'];
        }
        else
        {
            $pro_param = '';
        }
        //商品介绍
        if(isset($array['pro_introduce']))
        {
            $pro_introduce = $array['pro_introduce'];
        }
        else
        {
            $pro_introduce = '';
        }

        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }

        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }

        if (empty($product_title))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品标题不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.8');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        if($commodity_type != 1)
        { // 实物商品
            if ($keyword == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 关键词不能为空';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.10');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
            else
            {
                $keyword = trim($keyword, ',');
            }
        }

        if (empty($product_class))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品类别不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.11');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }
        else
        {
            $product_class = $Tools->str_option($product_class);
        }

        if (empty($brand_id))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 品牌不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.12');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        if($commodity_type != 1)
        { // 实物商品
            if (is_numeric($weight))
            {
                if ($weight < 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 重量不能为负数';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.13');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 重量请填写数字';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.14');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
        }

        if(empty($cover_map))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品封面图不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.17');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }
        else
        {
            $cover_map = preg_replace('/.*\//', '', $cover_map);
            if ($cover_map == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品封面图不能为空';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.17');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }

        if (!empty($imgurls))
        {
            if($type0 == '店铺')
            {
                if($imgurls == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品展示图不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.16');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
                $imgurls = explode(',',$imgurls);
                
                if($s_type)
                {
                    $s_type = explode(',',$s_type);
                }
            }
            
            $image = preg_replace('/.*\//', '', $imgurls['0']);
            if ($image == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品展示图不能为空';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.16');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
            unset($imgurls[0]);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品展示图不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.16');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        //五张轮播图
        if ($imgurls)
        {
            $arrimg = array();
            $rf = ProductImgModel::where(['product_id'=>$id])->field('id,product_url')->select()->toArray();
            if ($rf)
            {
                foreach ($rf as $key => $fs)
                {
                    $key1 = $fs['id'];
                    $arrimg[$key1] = $fs['product_url'];
                }
            }

            if (count($imgurls) > 4)
            {
                $Jurisdiction->admin_record($store_id, $operator, '产品展示图数量超出限制',1,$source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 产品展示图数量超出限制';
                $this->proLog($type0,$Log_content);
                Db::rollback();
                $message = Lang('product.42');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }

            foreach ($imgurls as $key => $file)
            {
                $imgsURL_name = preg_replace('/.*\//', '', $file);

                $key1 = array_search($imgsURL_name, $arrimg); // 在数组中搜索某个键值，并返回对应的键名。
                if ($key1)
                {
                    unset($arrimg[$key1]);
                }
                else
                {
                    $sql_img = ['product_url' => $imgsURL_name, 'product_id' => $id,'add_date'=>$time,'seller_id'=>$admin_name];
                    $r_img = Db::name('product_img')->insert($sql_img);
                    if ($r_img < 1)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '产品展示图上传失败',1,$source,$mch_id,$operator_id);
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 产品展示图上传失败！参数:' . json_encode($sql_img);
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $message = Lang('product.43');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }
                }
            }

            if (!empty($arrimg))
            {
                foreach ($arrimg as $keys => $fss)
                {
                    Db::table('lkt_product_img')->where('id',$keys)->delete();
                }
            }
        }
        else
        {
            Db::table('lkt_product_img')->where('product_id',$id)->delete();
        }

        if ($initial)
        {
            $key = array();
            $val = array();
            $initial1 = explode(',', $initial); // 转数组
            foreach ($initial1 as $k => $v)
            {
                $initial2 = explode('=', $v); // 转数组
                if($initial2[0] == 'stockWarn')
                {
                    $initial2[0] == 'kucun';
                }
                $key[] = $initial2[0];
                $val[] = $initial2[1];
                if ($initial2[0] == 'kucun')
                {
                    $kucun = $initial2[1];
                }
            }
            $initial = array_combine($key, $val); // 创建一个数组，用一个数组的值作为其键名，另一个数组的值作为其值

            foreach ($initial as $k => $v)
            {
                if ($k == 'cbj' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 成本价初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.18');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'yj' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 原价初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.19');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'sj')
                {
                    if($v == '')
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 售价初始值不能为空';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.20');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
                else if ($k == 'unit' && $v == '0')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 单位初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.22');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'kucun' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 库存初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.23');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                if ($k == 'cbj' || $k == 'yj' || $k == 'sj' || $k == 'kucun' )
                {
                    if (is_numeric($v))
                    {
                        if ($v < 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值不能为负数';
                            $this->proLog($type0,$Log_content);
                            $message = Lang('product.24');
                            echo json_encode(array('code' => 109,  'message' => $message));
                            exit();
                        }
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值请填写数字';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.25');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
            }
            $initial = serialize($initial);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.26');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }

        if($commodity_type != 1)
        { // 实物商品
            if (!is_numeric($min_inventory) || strpos($min_inventory, ".") !== false)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 库存预警请输入整数';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.27');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
            else
            {
                if ($min_inventory <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 库存预警请输入大于0的整数';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.28');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
            }
        }
        else
        {
            $min_inventory = 0;
        }

        $z_num = 0;
        $attributes = array();
        $is_moreng = 0; // 是否默认 0.不是默认 1.是默认
        //处理属性
        if (count($attr ? $attr : array()) == 0 || empty($attr))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写属性';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.29');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }
        else
        {
            $foreach_num = 0;
            foreach ($attr as $k => $v)
            {
                $foreach_num++;
                foreach ($v['attr_list'] as $ke => $va)
                {
                    // if($va['attr_group_name'] == '默认')
                    // {
                    //     $is_moreng = 1;
                    // }
                    $attr_group_name_id = Tools::attribute_id($store_id, 1, $va['attr_group_name'], 0);
                    if ($attr_group_name_id != 0)
                    { // 当属性名称ID不为0，SKU表里有数据
                        $attr_name_id = Tools::attribute_id($store_id, 2, $va['attr_name'], $attr_group_name_id);
                        if ($attr_name_id == 0)
                        { // 当属性值ID为0，SKU表里没有数据
                            $attr_name_id = $Tools->add_attribute($store_id, $attr_group_name_id, $va['attr_name'], 2, $admin_name, $foreach_num,$type0); // 添加属性值
                        }
                    }
                    else
                    { // 当属性名称ID为0，SKU表里没有数据
                        $attr_group_name_id = $Tools->add_attribute($store_id, 0, $va['attr_group_name'], 1, $admin_name, 0,$type0); // 添加属性名
                        $attr_name_id = $Tools->add_attribute($store_id, $attr_group_name_id, $va['attr_name'], 2, $admin_name, $foreach_num,$type0); // 添加属性值
                    }
                    $attr[$k]['attr_list'][$ke]['attr_group_name'] = $va['attr_group_name'] . '_LKT_' . $attr_group_name_id; // 拼接属性名
                    $attr[$k]['attr_list'][$ke]['attr_name'] = $va['attr_name'] . '_LKT_' . $attr_name_id; // 拼接属性值
                }
            }
            // if($is_moreng != 1)
            // {
                foreach ($attr as $key => $value)
                {
                    $cid = isset($value['cid'])?$value['cid']:'';
                    $attr_list = $value['attr_list'];
                    $attr_list_arr = array();
                    $attr_list_srt = '';
                    $str0 = '';
                    foreach ($attr_list as $k => $v)
                    {
                        if (strpos($v['attr_group_name'], '_LKT_') !== false)
                        {
                            $str0 = substr($v['attr_group_name'], 0, strrpos($v['attr_group_name'], "_LKT")) .':'. substr($v['attr_name'], 0, strrpos($v['attr_name'], "_LKT"));
                        }
                        else
                        {
                            $str0 = $v['attr_group_name'] .':'. $v['attr_name'];
                        }
                        $attr_list_arr[$v['attr_group_name']] = $v['attr_name'];
                        $attr_list_srt .= substr($v['attr_group_name'], 0, strpos($v['attr_group_name'], '_')) . '-' . substr($v['attr_name'], 0, strpos($v['attr_name'], '_'));
                    }
    
                    if($commodity_type != 1)
                    { // 实物商品
                        if((int)$min_inventory > (int)$value['kucun'])
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 规格库存低于预警值，请先修改库存！';
                            $this->proLog($type0,$Log_content);
                            $message = Lang('product.30');
                            echo json_encode(array('code' => 109,  'message' => $message));
                            exit();
                        }
                        $attributes[$key]['write_off_num'] = 0;
                    }
                    else
                    { // 虚拟商品
                        if(isset($value['kucun']))
                        {
                            if(!is_numeric($value['kucun']))
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的属性核销次数不能为空！';
                                $this->proLog($type0,$Log_content);
                                $message = Lang('product.100');
                                echo json_encode(array('code' => 51054,  'message' => $message));
                                exit();
                            }
                            if($write_off_settings == 2)
                            {
                                $attributes[$key]['write_off_num'] = 1;
                            }
                            else
                            {
                                $attributes[$key]['write_off_num'] = $value['kucun'];
                            }
                        }
                        else
                        {
                            $attributes[$key]['write_off_num'] = 1;
                        }
                        $value['kucun'] = 1000000;
                    }
    
                    $z_num += $value['kucun'];
                    $value['total_num'] = $value['kucun'];
                    //价格判断
                    foreach ($value as $cvkey => $cvvalue)
                    {
                        if (!is_array($cvvalue))
                        {
                            if($cvkey != 'bar_code' && $cvkey != 'cid')
                            {
                                if (empty($cvvalue) && $cvvalue != 0)
                                {
                                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 请完善属性';
                                    $this->proLog($type0,$Log_content);
                                    $message = Lang('product.32');
                                    echo json_encode(array('code' => 109,  'message' => $message));
                                    exit();
                                }
                            }
                        }
                    }
    
                    if(!is_numeric($value['cbj']))
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的成本价不为数字！';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.33');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                    if(!is_numeric($value['yj']))
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的原价不为数字！';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.34');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                    if(!is_numeric($value['sj']))
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的售价不为数字！';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.35');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                    if(!is_numeric($value['kucun']))
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的库存不为数字！';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.36');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                    $costprice = $value['cbj'];
                    $price = $value['sj'];
                    $yprice = $value['yj'];
                    if ($costprice >= $price)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '上传商品时，成本价不能大于售价！';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.37');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                    if(!$mch_id)
                    {
                        // if($yprice >= $price)
                        // {
                        //     $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '上传商品时，供货价不能大于售价！';
                        //     $this->proLog($type0,$Log_content);
                        //     $message = Lang('product.95');
                        //     echo json_encode(array('code' => 109, 'message' => $message));
                        //     exit;
                        // }
                        if($yprice <= $costprice)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '上传商品时，成本价不能大于供货价！';
                            $this->proLog($type0,$Log_content);
                            $message = Lang('product.96');
                            echo json_encode(array('code' => 109, 'message' => $message));
                            exit;
                        }
                    }
                    if(isset($value['img']))
                    {
                        if ($value['img'] == '')
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的属性图片未上传';
                            $this->proLog($type0,$Log_content);
                            $message = Lang('product.31');
                            echo json_encode(array('code' => 109,  'message' => $message));
                            exit();
                        }
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的属性图片未上传';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.31');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                    $attributes[$key]['img'] = preg_replace('/.*\//', '', $value['img']);
                    $attributes[$key]['unit'] = $unit;
    
                    if ($cid != 0 && $cid != '')
                    {
                        $r0_0 = ConfigureModel::where(['id'=>$cid,'recycle'=>0])->field('total_num,num')->select()->toArray();
                        if($r0_0)
                        {
                            $total_num = $r0_0[0]['total_num']; // 总库存数
                            $num = $r0_0[0]['num']; // 剩余数量
                        }
                        else
                        {
                            $value['cid'] = 0;
                            $total_num = $value['kucun']; // 总库存数
                            $num = $value['kucun']; // 剩余数量
                        }
                        
                        if ($value['kucun'] == $num)
                        { // 当传过来的数量 等于 剩余数量，表示没有改变了数量
                            $attributes[$key]['num'] = $num;
                            $attributes[$key]['total_num'] = $total_num;
                        }
                        else
                        {
                            $total_num1 = $num - $value['kucun']; // 剩余数量 - 传过来的数量
                            if ($total_num1 > 0)
                            { // 大于0，表示减少总库存
                                $attributes[$key]['total_num'] = $total_num - $total_num1;
                                $attributes[$key]['num'] = $value['kucun'];
                            }
                            else
                            { // 小于0，表示增加总库存
                                $attributes[$key]['total_num'] = $total_num - $total_num1;
                                $attributes[$key]['num'] = $value['kucun'];
                            }
                        }
                    }
                    else
                    {
                        $attributes[$key]['num'] = $value['kucun'];
                        $attributes[$key]['total_num'] = $value['kucun'];
                    }
    
                    $value['attribute'] = serialize($attr_list_arr);
                    $value = Tools::array_key_remove($value, 'attr_list');
    
                    $attributes[$key]['cid'] = $cid;
                    $attributes[$key]['costprice'] = $value['cbj'];
                    $attributes[$key]['yprice'] = $value['yj'];
                    $attributes[$key]['price'] = $value['sj'];
                    $attributes[$key]['msrp'] = $value['sj'];
                    $attributes[$key]['min_inventory'] = $min_inventory;
                    $attributes[$key]['attribute'] = $value['attribute'];
                }
            // }
        }

        //--处理属性
        if($commodity_type != 1)
        { // 实物商品
            if ($freight == 0)
            {   
                if($mch_id)
                {
                    $rr = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0,'is_default'=>1])->field('id')->select()->toArray();
                }
                else
                {
                    $rr = FreightModel::where(['store_id'=>$store_id,'supplier_id'=>$supplier_id,'recycle'=>0,'is_default'=>1])->field('id')->select()->toArray();
                }
                
                if ($rr)
                {
                    $freight = $rr[0]['id'];
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择运费模板名称';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.38');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
            }
        }

        $s_type = $s_type ? $s_type : array();
        if (count($s_type) == 0)
        {
            $s_type = 0;
        }
        else
        {
            $s_type = ',' . implode(",", $s_type) . ',';
        }
        $is_distribution = 0;
        $active = 1;

        if($show_adr == '')
        {
            $show_adr = 0;
        }
        $show_adr = ',' . $show_adr . ',';

        //--处理属性
        if($volume != '')
        {
            if (!is_numeric($volume) || strpos($volume, ".") !== false)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 虚拟销量输入整数';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.40');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
            else
            {
                if ($volume < 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 虚拟销量请输入大于等于0的整数';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.41');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
            }
        }
        else
        {
            $volume = 0;
        }
        if($mch_id)
        {
            $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name')->select()->toArray();
            if($r_mch)
            {
                $shop_name = $r_mch[0]['name'];
            }
        }
        else
        {
            $r_mch = SupplierModel::where(['store_id'=>$store_id,'id'=>$supplier_id])->field('supplier_name')->select()->toArray();
            if($r_mch)
            {
                $shop_name = $r_mch[0]['supplier_name'];
            }
        }
        
        $r_p = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0,'id'=>$id])->field('is_zixuan,commodity_str,gongyingshang')->select()->toArray();
        if($r_p)
        {
            $is_zixuan = $r_p[0]['is_zixuan']; // 是否自选 0.自选 1.不是自选
            $commodity_str = $r_p[0]['commodity_str']; // 商品ID字符串
            $gongyingshang = $r_p[0]['gongyingshang'];
        }

        // 根据产品id,修改产品信息
        if($mch_id != 0 && $gongyingshang != 0)
        { // 店铺选择的供应商商品
            $data_sql_1_update = array('product_title'=>$product_title,'subtitle'=>$subtitle,'scan'=>'','keyword'=>$keyword,'product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'cover_map'=>$cover_map,'imgurl'=>$image,'video'=>$video,'initial'=>$initial,'min_inventory'=>$min_inventory,'freight'=>$freight,'s_type'=>$s_type,'active'=>$active,'show_adr'=>$show_adr,'volume'=>$volume,'sort'=>$sort,'pro_video'=>$pro_video,'content'=>$content,'richList'=>$richList,'num'=>$z_num,'mch_status'=>$mch_status,'add_date'=>$time,'write_off_settings'=>$write_off_settings,'write_off_mch_ids'=>$write_off_mch_ids,'is_appointment'=>$is_appointment);
            // if($mch_status == 2)
            // {
            //     $data_sql_1_update['status'] = 2;
            // }
        }
        else if($mch_id != 0 && $gongyingshang == 0)
        { // 店铺自己商品
            $data_sql_1_update = array('product_title'=>$product_title,'subtitle'=>$subtitle,'scan'=>'','keyword'=>$keyword,'product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'cover_map'=>$cover_map,'imgurl'=>$image,'video'=>$video,'initial'=>$initial,'min_inventory'=>$min_inventory,'freight'=>$freight,'s_type'=>$s_type,'active'=>$active,'show_adr'=>$show_adr,'volume'=>$volume,'sort'=>$sort,'pro_video'=>$pro_video,'content'=>$content,'richList'=>$richList,'num'=>$z_num,'mch_status'=>$mch_status,'add_date'=>$time,'write_off_settings'=>$write_off_settings,'write_off_mch_ids'=>$write_off_mch_ids,'is_appointment'=>$is_appointment);
        }
        else
        { // 供应商商品
            $data_sql_1_update = array('product_title'=>$product_title,'subtitle'=>$subtitle,'scan'=>'','keyword'=>$keyword,'product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'cover_map'=>$cover_map,'imgurl'=>$image,'video'=>$video,'initial'=>$initial,'min_inventory'=>$min_inventory,'freight'=>$freight,'s_type'=>$s_type,'active'=>$active,'show_adr'=>$show_adr,'volume'=>$volume,'sort'=>$sort,'pro_video'=>$pro_video,'content'=>$content,'pro_introduce'=>$pro_introduce,'pro_param'=>$pro_param,'richList'=>$richList,'num'=>$z_num,'mch_status'=>$mch_status,'add_date'=>$time,'write_off_settings'=>$write_off_settings,'write_off_mch_ids'=>$write_off_mch_ids,'is_appointment'=>$is_appointment);
            if($mch_status == 2)
            {
                $data_sql_1_update['status'] = 2;
            }
        }
        
        if($mch_id)
        {
            $data_sql_1_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
        }
        else
        {
            $data_sql_1_where = array('store_id'=>$store_id,'gongyingshang'=>$supplier_id,'id'=>$id);
            if($mch_status == 2)
            {
                $data_sql['status'] = 2;
                $data_sql_1_update['examine_time'] = $time;
            }
        }
        $r_update = Db::name('product_list')->where($data_sql_1_where)->update($data_sql_1_update);
        if ($r_update == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了商品ID： '. $id . ' 的信息失败',2,$source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改商品失败！条件参数：'.json_encode($data_sql_1_where).';修改参数：'.json_encode($data_sql_1_update);
            $this->proLog($type0,$Log_content);
            Db::rollback();
            $message = Lang('product.48');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }
        else
        {   
            if($mch_status == 2 && $mch_id == 0)
            {
                $this->goodsSynchronization($store_id,$data_sql_1_update,$id);
            }

            $cids = array();
            if ($attributes)
            {
                $rcs = ConfigureModel::where(['pid'=>$id,'recycle'=>0])->field('id')->select()->toArray();
                if ($rcs)
                {
                    foreach ($rcs as $keyc => $valuec)
                    {
                        $keyc1 = $valuec['id'];
                        $cids[$keyc1] = $valuec['id'];
                    }
                }
            }

            $is_Live_product_synchronization = false; // 不需要同步
            if($is_zixuan == 1 && $commodity_str != '' && $cids != array())
            { // 不是自选 并且 有衍射数据 并且 有属性要删除
                $sql_living_product = "select * from lkt_living_room as a left join lkt_living_product as b on a.id = b.pro_id where b.store_id = '$store_id' and b.pro_id = '$id' and b.recycle = 0 and a.living_status in (0,1) and a.recycle = 0 ";
                $r_living_product = Db::query($sql_living_product);
                if($r_living_product)
                {
                    Db::rollback();
                    $message = Lang('living.12');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
                else
                {
                    $is_Live_product_synchronization = true; // 需要同步
                }
            }

            if($attributes != array())
            {
                foreach ($attributes as $ke => $va)
                {
                    $num = $va['num'];
                    $cid = $va['cid'];
                    $total_num = $va['total_num'];
                    $va['min_inventory'] = $min_inventory;
                    $va['ctime'] = 'CURRENT_TIMESTAMP';
                    $va = Tools::array_key_remove($va, 'cid');
                    if ($cid)
                    {
                        if (array_key_exists($cid, $cids))
                        {
                            unset($cids[$cid]);
                        }
                        // 查询剩余数量
                        $ccc = ConfigureModel::where(['id'=>$cid,'recycle'=>0])->field('num')->select()->toArray();
                        $cnums = $ccc ? $ccc[0]['num'] : 0;
                        $z_num1 = $num - $cnums; // 传过来的剩余数量 - 数据库里的剩余数量
    
                        $sql_1_0_where = array('id'=>$cid);
                        $sql_1_0_update = array('costprice'=>$va['costprice'],'yprice'=>$va['yprice'],'price'=>$va['price'],'msrp'=>$va['msrp'],'num'=>$va['num'],'total_num'=>$va['total_num'],'min_inventory'=>$min_inventory,'attribute'=>$va['attribute'],'img'=>$va['img'],'unit'=>$va['unit'],'ctime'=>$time,'write_off_num'=>$va['write_off_num']);
                        $r_attribute = Db::name('configure')->where($sql_1_0_where)->update($sql_1_0_update);
                        $attribute_id = $cid;
                        if ($r_attribute < 1)
                        {
                            $Jurisdiction->admin_record($store_id, $operator, '商品ID： '. $id . ' 的属性数据修改失败',2,$source,$mch_id,$operator_id);
                            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 属性数据修改失败！属性数据:'.json_encode($va);
                            $this->proLog($type0,$Log_content);
                            Db::rollback();
                            $message = Lang('product.49');
                            echo json_encode(array('code' => 109,  'message' => $message));
                            exit;
                        }
                        if($mch_status == 2 && $mch_id == 0)
                        {
                            $this->goodsInfoSynchronization($store_id,$sql_1_0_update,$cid);
                        }
                    }
                    else
                    {
                        $va['pid'] = $id;
    
                        $data_attribute = array('pid'=>$id,'costprice'=>$va['costprice'],'yprice'=>$va['yprice'],'price'=>$va['price'],'msrp'=>$va['msrp'],'num'=>$va['num'],'total_num'=>$va['total_num'],'min_inventory'=>$min_inventory,'attribute'=>$va['attribute'],'img'=>$va['img'],'unit'=>$va['unit'],'ctime'=>$time,'write_off_num'=>$va['write_off_num']);
                        $r_attribute = Db::name('configure')->insertGetId($data_attribute);
                        $z_num1 = $num;
                        $attribute_id = $r_attribute;
                        if ($r_attribute < 1)
                        {
                            $Jurisdiction->admin_record($store_id, $operator, '商品ID： '. $id . ' 的属性数据添加失败',2,$source,$mch_id,$operator_id);
                            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 属性数据添加失败！属性数据:'.json_encode($va);
                            $this->proLog($type0,$Log_content);
                            Db::rollback();
                            $message = Lang('product.44');
                            echo json_encode(array('code' => 109,  'message' => $message));
                            exit;
                        }
                        if($mch_status == 2 && $mch_id == 0)
                        {   
                            $this->goodsInfoInsert($store_id,$data_attribute,$r_attribute,$id);
                        }
                    }
                    if ($z_num1 > 0)
                    { // 增加
                        // 在库存记录表里，添加一条入库信息
                        $content = $admin_name . '增加商品总库存' . $z_num1;
                        $data_stock0 = array('store_id'=>$store_id,'product_id'=>$id,'attribute_id'=>$attribute_id,'total_num'=>$total_num,'flowing_num'=>$z_num1,'type'=>0,'add_date'=>$time,'content'=>$content);
                        $r_stock0 = Db::name('stock')->insertGetId($data_stock0);
                        if($r_stock0 < 1)
                        {
                            $Jurisdiction->admin_record($store_id, $operator, '库存记录添加失败',1,$source,$mch_id,$operator_id);
                            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 库存记录添加失败！参数:'.json_encode($data_stock0);
                            $this->proLog($type0,$Log_content);
                            Db::rollback();
                            $message = Lang('product.45');
                            echo json_encode(array('code' => 109,  'message' => $message));
                            exit;
                        }
                        if ($min_inventory >= $num)
                        { // 当属性库存低于等于预警值
                            $content1 = '预警';
                            // 在库存记录表里，添加一条预警信息
                            $data_stock1 = array('store_id'=>$store_id,'product_id'=>$id,'attribute_id'=>$attribute_id,'total_num'=>$total_num,'flowing_num'=>$z_num1,'type'=>2,'add_date'=>$time,'content'=>$content1);
                            $r_stock1 = Db::name('stock')->insertGetId($data_stock1);
                            if($r_stock1 < 1)
                            {
                                $Jurisdiction->admin_record($store_id, $operator, '库存记录添加失败',1,$source,$mch_id,$operator_id);
                                $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 库存记录添加失败！参数:'.json_encode($data_stock1);
                                $this->proLog($type0,$Log_content);
                                Db::rollback();
                                $message = Lang('product.45');
                                echo json_encode(array('code' => 109,  'message' => $message));
                                exit;
                            }
                            $message_9 = "商品ID为" . $id . "的商品库存不足，请尽快补充库存";
                            $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>$supplier_id,'type'=>9,'parameter'=>$attribute_id,'content'=>$message_9);
                            PC_Tools::add_message_logging($message_logging_list9);
                        }
                    }
                    else if ($z_num1 == 0)
                    {
    
                    }
                    else
                    { // 减少
                        $j_num = $cnums - $num; // 数据库里的剩余数量 - 传过来的剩余数量
    
                        $content2 = $admin_name . '减少商品总库存' . $j_num;
                        // 在库存记录表里，添加一条入库信息
                        $data_stock2 = array('store_id'=>$store_id,'product_id'=>$id,'attribute_id'=>$attribute_id,'total_num'=>$total_num,'flowing_num'=>$j_num,'type'=>1,'add_date'=>$time,'content'=>$content2);
                        $r_stock2 = Db::name('stock')->insertGetId($data_stock2);
                        if($r_stock2 < 1)
                        {
                            $Jurisdiction->admin_record($store_id, $operator, '库存记录添加失败',1,$source,$mch_id,$operator_id);
                            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 库存记录添加失败！参数:'.json_encode($data_stock2);
                            $this->proLog($type0,$Log_content);
                            Db::rollback();
                            $message = Lang('product.45');
                            echo json_encode(array('code' => 109,  'message' => $message));
                            exit;
                        }
                    }
                }
            }

            //删除属性
            if (!empty($cids))
            {
                foreach ($cids as $keyds => $valueds)
                {   
                    Db::name('configure')->where('id',$valueds)->update(['recycle'=>1]);
                    if($mch_status == 2 && $mch_id == 0)
                    {
                        Db::name('configure')->where('supplier_superior',$valueds)->update(['recycle'=>1]);
                    }
                }
            }

            $Jurisdiction->admin_record($store_id, $operator, '修改了商品ID：' . $id . ' 的信息',2,$source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改商品ID为' . $id . '成功';
            $this->proLog($type0,$Log_content);

            if($mch_id)
            {
                if($mch_status == 1)
                {
                    $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1])->field('shop_id')->select()->toArray();
                    $shop_id = $r_admin[0]['shop_id'];

                    $message_7 = "商品id为:". $id . "商品名称为:" . $product_title . "的商品需要管理员审核";
                    $message_logging_list7 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'gongyingshang'=>0,'type'=>7,'parameter'=>$id,'content'=>$message_7,'source'=>'mch');
                    PC_Tools::add_message_logging($message_logging_list7);
                }
            }
            else
            {
                if($mch_status == 1)
                {
                    $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1])->field('shop_id')->select()->toArray();
                    $shop_id = $r_admin[0]['shop_id'];

                    $message_7 = "供应商商品，商品id为:" . $id . "商品名称为:" . $product_title . "的商品需要管理员审核";
                    $message_logging_list7 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'gongyingshang'=>0,'type'=>7,'parameter'=>$id,'content'=>$message_7,'source'=>'supplier');
                    PC_Tools::add_message_logging($message_logging_list7);
                }
            }

            $array = array('store_id'=>$store_id,'type0'=>2,'id'=>$id,'name'=>$product_title,'lang_code'=>$lang_code,'country_num'=>$country_num);
            PC_Tools::modify_jump_path($array);

            Db::commit();
            if($is_Live_product_synchronization)
            {
                $array = array('store_id'=>$store_id,'id'=>$id);
                $this->Live_product_synchronization($array);
            }
            $message = Lang('Success');
            echo json_encode(array('code' => 200,  'message' => $message));
            exit;
        }
    }

    // 修改普通商品，同步直播商品
    public function Live_product_synchronization($array)
    {
        $store_id = $array['store_id'];
        $pid = $array['id'];

        $commodity_str1 = '';
        $sql0 = "select * from lkt_product_list where id = '$pid' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            unset($r0[0]['id']);
            unset($r0[0]['commodity_type']);
            $p_list = $r0[0];
            if($r0[0]['commodity_str'] != '')
            {
                $commodity_str1 = unserialize($r0[0]['commodity_str']);
                unset($r0[0]['commodity_str']);
                foreach($commodity_str1 as $k => $v)
                {
                    $data_where = array('id'=>$v,'commodity_type'=>2,'recycle'=>0);
                    $r1 = Db::name('product_list')->where($data_where)->update($p_list);
                }
            }
        }

        $sql_0 = "select * from lkt_configure where pid = '$pid' ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            foreach($r_0 as $k_0 => $v_0)
            {
                $id_0 = $v_0['id'];
                unset($v_0['id']);
                if($v_0['attribute_str'] != '')
                {
                    unset($v_0['pid']);
                    unset($v_0['commission']);
                    unset($v_0['update_time']);
                    unset($v_0['live_price']);
                    $data_update_1 = $v_0;
                    if($v_0['attribute_str'] != '')
                    {
                        $attribute_str1 = unserialize($v_0['attribute_str']);
                        unset($v_0['attribute_str']);
                        foreach($attribute_str1 as $k_1 => $v_1)
                        {
                            $data_where_1 = array('id'=>$v_1,'recycle'=>0);
                            $r1 = Db::name('configure')->where($data_where_1)->update($data_update_1);
                        }
                    }
                }
                else
                { // 新数据
                    $attribute_str1 = array();
                    if($commodity_str1 != '')
                    {
                        foreach($commodity_str1 as $k => $v)
                        {
                            $v_0['pid'] = $v;
                            $c_list = $v_0;
                            $r_1 = Db::name('configure')->insertGetId($c_list);
    
                            $attribute_str1[] = $r_1;
                        }
                        $attribute_str = serialize($attribute_str1);
    
                        $sql_2 = "update lkt_configure set attribute_str = '$attribute_str' where id = '$id_0' and pid = '$pid' ";
                        $r_2 = Db::execute($sql_2);
                    }
                }
            }
        }
        return;
    }
    
    // 查询分类上级
    public function superior($store_id,$cid, $list)
    {
        $arr = array();
        // 根据id，查询分类
        $r = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$cid,'examine'=>1])->field('cid,sid,pname,level')->select()->toArray();
        if ($r)
        {
            $id = $r[0]['cid'];
            $sid = $r[0]['sid'];
            $pname = $r[0]['pname'];
            $level = $r[0]['level'];
            $arr['id'] = $id;
            if ($level != 0)
            { // 当不是1级分类
                // 获取产品类别
                $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$sid,'examine'=>1])->order('sort','desc')->field('cid,pname,level')->select()->toArray();
                if ($r0)
                {
                    foreach ($r0 as $k => $v)
                    {
                        if ($v['cid'] == $cid)
                        {
                            $r0[$k]['status'] = true;
                        }
                        else
                        {
                            $r0[$k]['status'] = false;
                        }
                    }
                    array_unshift($list, $r0);
                }
                $res = $this->superior($store_id,$sid, $list);
                $arr['id'] = $res['id'];
                $arr['list'] = $res['list'];
            }
            else
            { // 当是1级分类
                // 获取产品类别
                $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0,'examine'=>1])->order('sort','desc')->field('cid,pname,level')->select()->toArray();
                if ($r0)
                {
                    foreach ($r0 as $k => $v)
                    {
                        if ($v['cid'] == $cid)
                        {
                            $r0[$k]['status'] = true;
                        }
                        else
                        {
                            $r0[$k]['status'] = false;
                        }
                    }
                    array_unshift($list, $r0);
                }
                $arr['list'] = $list;
            }
        }
        return $arr;
    }
    
    // 提交审核/撤销审核
    public function examine($array)
    {
        $store_id = $array['store_id'];
        $admin_name = $array['admin_name'];
        $user_id = $array['user_id'];
        $p_id = $array['p_id'];
        $type0 = $array['type0'];
        $p_list = explode(',', $p_id);

        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }

        $Jurisdiction = new Jurisdiction();
        Db::startTrans();

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id']; // 店铺id
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $admin_name . '没有店铺！';
            $this->proLog($type0,$Log_content);
            $message = Lang('Illegal invasion');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }
        
        if(count($p_list) > 0)
        {
            foreach ($p_list as $k => $v)
            {
                $r1 = ProductListModel::where(['store_id'=> $store_id,'recycle'=>0,'id'=>$v])->field('id,product_title,mch_status,gongyingshang')->select()->toArray();
                if ($r1)
                {
                    $mch_status = $r1[0]['mch_status'];
                    $product_title = $r1[0]['product_title'];
                    $gongyingshang = $r1[0]['gongyingshang'];
                    $sql2 = array('store_id'=>$store_id,'id'=>$v);
    
                    if ($mch_status == 4)
                    {
                        $r2 = Db::name('product_list')->where($sql2)->update(['mch_status'=>1]);
                    }
                    else
                    {
                        $r2 = Db::name('product_list')->where($sql2)->update(['mch_status'=>4]);
                    }
                    if ($r2 != -1)
                    {
                        if ($mch_status == 4)
                        {
                            $r_admin = AdminModel::where(['store_id'=> $store_id,'type'=>1])->field('shop_id')->select()->toArray();
                            $shop_id = $r_admin[0]['shop_id'];
    
                            $message_7 = "商品id为:". $v . "商品名称为:" . $product_title . "的商品需要管理员审核";
                            $message_logging_list7 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'gongyingshang'=>0,'type'=>7,'parameter'=>$v,'content'=>$message_7);
                            PC_Tools::add_message_logging($message_logging_list7);
    
                            $Jurisdiction->admin_record($store_id, $operator, ' 提交了商品ID： ' . $v . '，的商品审核成功',2,$source,$mch_id,$operator_id);
                            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $operator . '提交商品ID为' . $v . '审核,操作成功！';

                        }
                        else
                        {
                            $Jurisdiction->admin_record($store_id, $operator, ' 撤销了商品ID： ' . $v . '，的商品审核成功',2,$source,$mch_id,$operator_id);
                            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $operator . '撤销商品ID为' . $v . '审核,操作成功！';
                        }
                        $this->proLog($type0,$Log_content);
                    }
                    else
                    {
                        if ($mch_status == 4)
                        {
                            $Jurisdiction->admin_record($store_id, $operator, ' 提交了商品ID： ' . $v . '，的商品审核失败',2,$source,$mch_id,$operator_id);
                            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $operator . '提交商品ID为' . $v . '审核失败！';
                        }
                        else
                        {
                            $Jurisdiction->admin_record($store_id, $operator, ' 撤销了商品ID： ' . $v . '，的商品审核失败',2,$source,$mch_id,$operator_id);
                            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $operator . '撤销商品ID为' . $v . '审核失败！';
                        }
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $message = Lang('Busy network');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $admin_name . '提交/撤销商品审核时，商品ID' . $v . '错误！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('Parameter error');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
            }
        }
       
        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200,  'message' => $message));
        exit;
    }
    
    // 上下架
    public function upper_and_lower_shelves($array,$type0)
    {
        $store_id = $array['store_id'];
        $admin_name = $array['admin_name'];
        $id = $array['p_id'];
        $type = $array['type'];
        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }
        $Jurisdiction = new Jurisdiction();

        $time = date("Y-m-d H:i:s");
        Db::startTrans();

        $Tools = new Tools($store_id, 1);

        $id = explode(',',$id);

        $r_admin = AdminModel::where(['store_id'=>$store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $shop_id = $r_admin[0]['shop_id'];

        foreach ($id as $k => $v)
        {
            $r = ProductListModel::where(['id'=>$v])->field('product_title,status,num,mch_id,min_inventory,gongyingshang,supplier_superior,commodity_str')->select()->toArray();
            if($r)
            {
                $mch_id = $r[0]['mch_id'];
                $product_title = $r[0]['product_title'];
                $min_inventory = $r[0]['min_inventory'];
                $gongyingshang = $r[0]['gongyingshang']; // 供应商ID
                $supplier_superior = $r[0]['supplier_superior']; // 供应商商品ID
                
                if(strlen($product_title) > 10)
                {
                    $product_title = substr($product_title,0,9) . '...';
                }

                $r_mch = MchModel::where(['id'=>$mch_id])->field('user_id,name')->select()->toArray();
                if ($r[0]['status'] == 2)
                {
                    $res_0 = Lang('product.57');
                    $res = Tools::del_banner($store_id,$v,'productId');
                    
                    $rr = Db::name('product_list')->where(['id' => $v])->update(['status' => 3]);
                    if ($rr >= 0)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, ' 下架商品ID为 ' . $v . ' 下架成功',2,$source,$mch_id,$operator_id);
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 下架商品ID为 ' . $v . ' 下架成功';
                        $this->proLog($type0,$Log_content);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 下架商品ID为 ' . $v . ' 下架失败';
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, ' 下架商品ID为 ' . $v . ' 下架失败',2,$source,$mch_id,$operator_id);
                        $message = Lang('product.50');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                    $data_update = array('status'=>3);
                }
                else
                {
                    $sql_p = "select status from lkt_product_list where id = '$supplier_superior' ";
                    $r_p = Db::query($sql_p);
                    if($r_p)
                    {
                        if($r_p[0]['status'] != 2)
                        {
                            $message = Lang('product.97');
                            echo json_encode(array('code' => 109, 'message' => $message));
                            exit;
                        }
                    }

                    $info = $Tools->commodity_status($store_id, $v,'status');
                    $res_0 = Lang('product.51');
                    if($info == '请先去完善商品信息！')
                    {
                        $message = Lang('product.52');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                    if ($r[0]['num'] < 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 商品ID为 ' . $v . ' 操作失败';
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $message = Lang('product.53');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }

                    $r_p = ConfigureModel::where(['pid'=>$v,'recycle'=>0])->field('num')->select()->toArray();
                    if($r_p)
                    {
                        foreach($r_p as $k_p => $v_p)
                        {
                            if ($v_p['num'] < $min_inventory)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 商品库存低于预警值，请先添加商品库存!';
                                $this->proLog($type0,$Log_content);
                                Db::rollback();
                                $message = Lang('product.54');
                                echo json_encode(array('code' => 109, 'message' => $message));
                                exit;
                            }
                        }
                    }
                   
                    $rr = Db::name('product_list')->where(['id' => $v])->update(['status'=>2,'upper_shelf_time'=>$time]);
                    if ($rr >= 0)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, ' 上架商品ID为 ' . $v . ' 上架成功',2,$source,$mch_id,$operator_id);
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 上架商品ID为 ' . $v . ' 上架成功';
                        $this->proLog($type0,$Log_content);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 上架商品ID为 ' . $v . ' 上架失败';
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, ' 上架商品ID为 ' . $v . ' 上架失败',2,$source,$mch_id,$operator_id);
                        $message = Lang('product.55');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }

                    $data_update = array('status'=>2,'upper_shelf_time'=>$time);
                }
                if($r[0]['commodity_str'] != '')
                {
                    $commodity_str1 = unserialize($r[0]['commodity_str']);
                    foreach($commodity_str1 as $k => $v)
                    {
                        $data_where = array('id'=>$v,'commodity_type'=>2,'recycle'=>0);
                        $r1 = Db::name('product_list')->where($data_where)->update($data_update);
                    }
                }
            }
            else
            {
                Db::rollback();
                $message = Lang('Parameter error');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }

        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }
    
    // 删除
    public function del($store_id,$admin_name,$id,$type0,$operator = '',$source = 1,$operator_id = 0)
    {
        $Jurisdiction = new Jurisdiction();
        $Tools = new Tools($store_id, 1);
        $id = explode(',',$id);
       
        foreach ($id as $k => $v)
        {
            $sql0 = "select mch_id from lkt_product_list where id = '$v' ";
            $r0 = Db::query($sql0);
            $mch_id = $r0[0]['mch_id'];

            $sql1 = "select a.id from lkt_group_goods a left join lkt_group_activity b on a.activity_id=b.id and a.recycle = 0 where b.store_id = '$store_id' and b.mch_id = '$mch_id' and (b.status = 1 or b.status = 0) and a.goods_id = '$v' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $message = Lang('product.99');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }

            $sql_o1 = "select a.* from lkt_product_list as a left join lkt_order_details as b on a.id = b.p_id where a.store_id = '$store_id' and a.recycle = 0 and a.mch_status = 2 and r_status in (0,1,2) and a.id = '$v'";
            $r_o1 = Db::query($sql_o1);
            if($r_o1)
            {
                $message = Lang('product.102');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }

            Tools::del_banner($store_id, $v, 'productId');
            
            $Tools->commodity_status($store_id, $v,'recycle');

            // // 删除购物车里的数据
            // Db::table('lkt_cart')->where('Goods_id',$v)->delete();
            // 删除我的足迹数据
            Db::table('lkt_user_footprint')->where('p_id',$v)->delete();
            // // 删除我的收藏里的数据
            // Db::table('lkt_user_collection')->where('p_id',$v)->delete();
            // 根据产品id，删除产品信息
            Db::name('product_list')->where('id', $v)->update(['recycle' => '1','status' => '3']);
            // 根据产品id，删除产品属性信息
            Db::name('configure')->where('pid', $v)->update(['recycle' => '1']);

            // Db::name('group_goods')->where('goods_id', $v)->update(['recycle' => '1']);

            // Db::table('lkt_product_img')->where('product_id', $v)->delete();

            Db::table('lkt_jump_path')->where('type0',2)->whereIn('parameter','%='.$v)->delete();

            $array = array('store_id'=>$store_id,'id'=>$v);
            $this->Live_product_synchronization($array);

            $Jurisdiction->admin_record($store_id, $operator, ' 删除商品ID为 ' . $v . ' 的信息',3,$source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 删除商品ID为 ' . $v . ' 的信息';
            $this->proLog($type0,$Log_content);

            //获取店铺商品数据
            $res2 = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0,'supplier_superior'=>$v])->select()->toArray();
            if($res2)
            {
                foreach ($res2 as $key => $value) 
                {
                    $mid = $value['id'];

                    $sql_o2 = "select a.* from lkt_product_list as a left join lkt_order_details as b on a.id = b.p_id where a.store_id = '$store_id' and a.recycle = 0 and a.mch_status = 2 and r_status in (0,1,2) and a.id = '$mid'";
                    $r_o2 = Db::query($sql_o2);
                    if($r_o2)
                    {
                        $message = Lang('product.102');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                    
                    Tools::del_banner($store_id, $mid, 'productId');
            
                    $Tools->commodity_status($store_id, $mid,'recycle');

                    // 删除购物车里的数据
                    Db::table('lkt_cart')->where('Goods_id',$mid)->delete();
                    // 删除我的足迹数据
                    Db::table('lkt_user_footprint')->where('p_id',$mid)->delete();
                    // 删除我的收藏里的数据
                    Db::table('lkt_user_collection')->where('p_id',$mid)->delete();
                    // 根据产品id，删除产品信息
                    Db::name('product_list')->where('id', $mid)->update(['recycle' => '1','status' => '3']);
                    // 根据产品id，删除产品属性信息
                    Db::name('configure')->where('pid', $mid)->update(['recycle' => '1']);

                    Db::name('product_img')->where('product_id', $mid)->delete();

                    Db::name('jump_path')->where('type0',2)->whereIn('parameter','%='.$mid)->delete();
                }
            }
        }
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 删除(店铺注销)
    public function mch_del($store_id,$admin_name,$id,$type0,$operator = '',$source = 1,$operator_id = 0)
    {
        $Jurisdiction = new Jurisdiction();
        $Tools = new Tools($store_id, 1);
        $id = explode(',',$id);
       
        foreach ($id as $k => $v)
        {
            $sql0 = "select mch_id from lkt_product_list where id = '$v' ";
            $r0 = Db::query($sql0);
            $mch_id = $r0[0]['mch_id'];

            $sql1 = "select a.id from lkt_group_goods a left join lkt_group_activity b on a.activity_id=b.id and a.recycle = 0 where b.store_id = '$store_id' and b.mch_id = '$mch_id' and (b.status = 1 or b.status = 0) and a.goods_id = '$v' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $message = Lang('product.99');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }

            Tools::del_banner($store_id, $v, 'productId');
            
            $Tools->commodity_status($store_id, $v,'recycle');

            // // 删除购物车里的数据
            // Db::table('lkt_cart')->where('Goods_id',$v)->delete();
            // 删除我的足迹数据
            Db::table('lkt_user_footprint')->where('p_id',$v)->delete();
            // // 删除我的收藏里的数据
            // Db::table('lkt_user_collection')->where('p_id',$v)->delete();
            // 根据产品id，删除产品信息
            Db::name('product_list')->where('id', $v)->update(['recycle' => '1','status' => '3']);
            // 根据产品id，删除产品属性信息
            Db::name('configure')->where('pid', $v)->update(['recycle' => '1']);

            // Db::name('group_goods')->where('goods_id', $v)->update(['recycle' => '1']);

            // Db::table('lkt_product_img')->where('product_id', $v)->delete();

            Db::table('lkt_jump_path')->where('type0',2)->whereIn('parameter','%='.$v)->delete();

            $Jurisdiction->admin_record($store_id, $operator, ' 删除商品ID为 ' . $v . ' 的信息',3,$source,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 删除商品ID为 ' . $v . ' 的信息';
            $this->proLog($type0,$Log_content);

            //获取店铺商品数据
            $res2 = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0,'supplier_superior'=>$v])->select()->toArray();
            if($res2)
            {
                foreach ($res2 as $key => $value) 
                {
                    $mid = $value['id'];
                    Tools::del_banner($store_id, $mid, 'productId');
            
                    $Tools->commodity_status($store_id, $mid,'recycle');

                    // 删除购物车里的数据
                    Db::table('lkt_cart')->where('Goods_id',$mid)->delete();
                    // 删除我的足迹数据
                    Db::table('lkt_user_footprint')->where('p_id',$mid)->delete();
                    // 删除我的收藏里的数据
                    Db::table('lkt_user_collection')->where('p_id',$mid)->delete();
                    // 根据产品id，删除产品信息
                    Db::name('product_list')->where('id', $mid)->update(['recycle' => '1','status' => '3']);
                    // 根据产品id，删除产品属性信息
                    Db::name('configure')->where('pid', $mid)->update(['recycle' => '1']);

                    Db::name('product_img')->where('product_id', $mid)->delete();

                    Db::name('jump_path')->where('type0',2)->whereIn('parameter','%='.$mid)->delete();
                }
            }
        }
        return;
    }

    // 批量上下架
    public function BatchLoadingAndUnloading($array)
    {
        $store_id = $array['store_id'];
        $id = $array['p_id'];
        $status = $array['status'];
        $type0 = $array['type'];
        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }
        $Jurisdiction = new Jurisdiction();

        $time = date("Y-m-d H:i:s");
        Db::startTrans();

        $Tools = new Tools($store_id, 1);

        $id = explode(',',$id);

        $r_admin = AdminModel::where(['store_id'=>$store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $shop_id = $r_admin[0]['shop_id'];

        foreach ($id as $k => $v)
        {
            $r = ProductListModel::where(['id'=>$v])->field('product_title,status,num,mch_id,min_inventory,gongyingshang,supplier_superior,commodity_str')->select()->toArray();
            if($r)
            {
                $mch_id = $r[0]['mch_id'];
                $product_title = $r[0]['product_title'];
                $min_inventory = $r[0]['min_inventory'];
                $gongyingshang = $r[0]['gongyingshang']; // 供应商ID
                $supplier_superior = $r[0]['supplier_superior']; // 供应商商品ID
                
                if(strlen($product_title) > 10)
                {
                    $product_title = substr($product_title,0,9) . '...';
                }

                if($status == 3)
                {
                    $res_0 = Lang('product.57');
                    $res = Tools::del_banner($store_id,$v,'productId');

                    $rr = Db::name('product_list')->where(['id' => $v])->update(['status' => 3]);
                    if ($rr >= 0)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, ' 下架商品ID为 ' . $v . ' 下架成功',2,$source,$mch_id,$operator_id);
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 下架商品ID为 ' . $v . ' 下架成功';
                        $this->proLog($type0,$Log_content);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 下架商品ID为 ' . $v . ' 下架失败';
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, ' 下架商品ID为 ' . $v . ' 下架失败',2,$source,$mch_id,$operator_id);
                        $message = Lang('product.50');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                    $data_update = array('status'=>3);
                }
                else
                {
                    $sql_p = "select status from lkt_product_list where id = '$supplier_superior' ";
                    $r_p = Db::query($sql_p);
                    if($r_p)
                    {
                        if($r_p[0]['status'] != 2)
                        {
                            $message = Lang('product.97');
                            echo json_encode(array('code' => 109, 'message' => $message));
                            exit;
                        }
                    }

                    $info = $Tools->commodity_status($store_id, $v,'status');
                    $res_0 = Lang('product.51');
                    if($info == '请先去完善商品信息！')
                    {
                        $message = Lang('product.52');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                    if ($r[0]['num'] < 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 商品ID为 ' . $v . ' 操作失败';
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $message = Lang('product.53');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }

                    $r_p = ConfigureModel::where(['pid'=>$v,'recycle'=>0])->field('num')->select()->toArray();
                    if($r_p)
                    {
                        foreach($r_p as $k_p => $v_p)
                        {
                            if ($v_p['num'] < $min_inventory)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 商品库存低于预警值，请先添加商品库存!';
                                $this->proLog($type0,$Log_content);
                                Db::rollback();
                                $message = Lang('product.54');
                                echo json_encode(array('code' => 109, 'message' => $message));
                                exit;
                            }
                        }
                    }

                    $rr = Db::name('product_list')->where(['id' => $v])->update(['status'=>2,'upper_shelf_time'=>$time]);
                    if ($rr >= 0)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, ' 上架商品ID为 ' . $v . ' 上架成功',2,$source,$mch_id,$operator_id);
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 上架商品ID为 ' . $v . ' 上架成功';
                        $this->proLog($type0,$Log_content);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 上架商品ID为 ' . $v . ' 上架失败';
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, ' 上架商品ID为 ' . $v . ' 上架失败',2,$source,$mch_id,$operator_id);
                        $message = Lang('product.55');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }

                    $data_update = array('status'=>2,'upper_shelf_time'=>$time);
                }

                if($r[0]['commodity_str'] != '')
                {
                    $commodity_str1 = unserialize($r[0]['commodity_str']);
                    foreach($commodity_str1 as $k => $v)
                    {
                        $data_where = array('id'=>$v,'commodity_type'=>2,'recycle'=>0);
                        $r1 = Db::name('product_list')->where($data_where)->update($data_update);
                    }
                }
            }
            else
            {
                Db::rollback();
                $message = Lang('Parameter error');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }

        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 批量选择位置
    public function BatchSelectionOfLocations($array)
    {
        $store_id = $array['store_id'];
        $id = $array['p_id'];
        $status = $array['status'];
        $type0 = $array['type'];
        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }
        $Jurisdiction = new Jurisdiction();

        Db::startTrans();

        $id = explode(',',$id);

        if($status == '')
        {
            $status = 0;
        }
        $show_adr = ',' . $status . ',';

        $r_admin = AdminModel::where(['store_id'=>$store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $shop_id = $r_admin[0]['shop_id'];

        foreach ($id as $k => $v)
        {
            $r = ProductListModel::where(['id'=>$v])->field('mch_id')->select()->toArray();
            if($r)
            {
                $mch_id = $r[0]['mch_id'];
                $rr = Db::name('product_list')->where(['id' => $v])->update(['show_adr' => $show_adr]);
                if ($rr >= 0)
                {
                    $Jurisdiction->admin_record($store_id, $operator, ' 修改商品ID为 ' . $v . ' 的显示位置成功',2,$source,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改商品ID为 ' . $v . ' 的显示位置成功';
                    $this->proLog($type0,$Log_content);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改商品ID为 ' . $v . ' 的显示位置失败';
                    $this->proLog($type0,$Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, ' 修改商品ID为 ' . $v . ' 的显示位置失败',2,$source,$mch_id,$operator_id);
                    $message = Lang('product.50');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
            }
            else
            {
                Db::rollback();
                $message = Lang('Parameter error');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }

        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 批量获取运费
    public function BatchObtainShippingFees($array)
    {
        $store_id = $array['store_id'];
        $id = $array['p_id'];
        $type0 = $array['type'];
        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }
        $id = explode(',',$id);
        $list = array();
        $mch_id_list = array();
        foreach ($id as $k => $v)
        {
            $r = ProductListModel::where(['id'=>$v])->field('mch_id')->select()->toArray();
            if($r)
            {
                if(!in_array($r[0]['mch_id'],$mch_id_list))
                {
                    $mch_id_list[] = $r[0]['mch_id'];
                }
            }
        }

        if($mch_id_list != array() && count($mch_id_list) == 1)
        {
            $mch_id = $mch_id_list[0];
            $sql1 = "select * from lkt_freight where store_id = '$store_id' and mch_id = '$mch_id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $list = $r1;
            }
        }
        $data = array('list'=>$list);
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message, 'data' => $data));
        exit;
    }

    // 批量设置运费
    public function BatchSetShippingFees($array)
    {
        $store_id = $array['store_id'];
        $id = $array['p_id'];
        $fid = $array['fid'];
        $type0 = $array['type'];
        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }
        $Jurisdiction = new Jurisdiction();

        Db::startTrans();

        $id = explode(',',$id);

        $r_admin = AdminModel::where(['store_id'=>$store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $shop_id = $r_admin[0]['shop_id'];

        foreach ($id as $k => $v)
        {
            $r = ProductListModel::where(['id'=>$v])->field('mch_id')->select()->toArray();
            if($r)
            {
                $mch_id = $r[0]['mch_id'];
                $rr = Db::name('product_list')->where(['id' => $v])->update(['freight' => $fid]);
                if ($rr >= 0)
                {
                    $Jurisdiction->admin_record($store_id, $operator, ' 修改商品ID为 ' . $v . ' 的运费成功',2,$source,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改商品ID为 ' . $v . ' 的运费成功';
                    $this->proLog($type0,$Log_content);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改商品ID为 ' . $v . ' 的运费失败';
                    $this->proLog($type0,$Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, ' 修改商品ID为 ' . $v . ' 的运费失败',2,$source,$mch_id,$operator_id);
                    $message = Lang('product.50');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
            }
            else
            {
                Db::rollback();
                $message = Lang('Parameter error');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }

        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 批量预警
    public function BatchWarning($array)
    {
        $store_id = $array['store_id'];
        $id = $array['p_id'];
        $min_inventory = $array['min_inventory'];
        $type0 = $array['type'];
        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }
        $Jurisdiction = new Jurisdiction();

        Db::startTrans();

        if (!is_numeric($min_inventory) || strpos($min_inventory, ".") !== false)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 库存预警请输入整数';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.27');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }
        else
        {
            if ($min_inventory <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 库存预警请输入大于0的整数';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.28');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
        }
        
        $id = explode(',',$id);

        $r_admin = AdminModel::where(['store_id'=>$store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $shop_id = $r_admin[0]['shop_id'];

        foreach ($id as $k => $v)
        {
            $r = ProductListModel::where(['id'=>$v])->field('mch_id,initial')->select()->toArray();
            if($r)
            {
                $mch_id = $r[0]['mch_id'];
                $initial = unserialize($r[0]['initial']);
                $initial['stockWarn'] = $min_inventory;
                $initial = serialize($initial);

                $rrr = Db::name('configure')->where(['pid' => $v,'recycle'=>0])->update(['min_inventory' => $min_inventory]);

                $rr = Db::name('product_list')->where(['id' => $v])->update(['min_inventory' => $min_inventory,'initial'=>$initial]);
                if ($rr >= 0)
                {
                    $Jurisdiction->admin_record($store_id, $operator, ' 修改商品ID为 ' . $v . ' 的库存预警成功',2,$source,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改商品ID为 ' . $v . ' 的库存预警成功';
                    $this->proLog($type0,$Log_content);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改商品ID为 ' . $v . ' 的库存预警失败';
                    $this->proLog($type0,$Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, ' 修改商品ID为 ' . $v . ' 的库存预警失败',2,$source,$mch_id,$operator_id);
                    $message = Lang('product.50');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
            }
            else
            {
                Db::rollback();
                $message = Lang('Parameter error');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }

        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    public function get_attribute($store_id, $page, $keyword, $strArr = array(), $lang_code = 'zh_CN')
    {
        $pagesize = 10;
        $page     = max(1, (int)$page);
        $start    = ($page - 1) * $pagesize;
    
        $condition = "a.status = 1 AND a.recycle = 0 AND a.is_examine = 1 
                      AND a.type = 2 AND b.recycle = 0 AND a.lang_code = '$lang_code'";
    
        if ($keyword !== '') {
            $keyword = Tools::FuzzyQueryConcatenation($keyword);
            $condition .= " AND (a.name LIKE $keyword OR b.name LIKE $keyword)";
        }
    
        // 计算总数（只算 type=2 的记录）
        $sql_count = "SELECT COUNT(a.id) AS total 
                      FROM lkt_sku AS a 
                      LEFT JOIN lkt_sku AS b ON a.sid = b.id 
                      WHERE $condition";
        $r0 = Db::query($sql_count);
        $total = $r0 ? (int)$r0[0]['total'] : 0;
    
        $list = [];
    
        // 如果是第一页，且有预置的属性名（type=1），先加入
        // 但注意：这里加的条数会影响分页偏移
        if ($page === 1 && !empty($strArr)) {
            foreach ($strArr as $item) {
                $list[] = [
                    'id'     => (int)$item['id'],
                    'name'   => $item['name'],
                    'sid'    => (int)$item['sid'],
                    'sname'  => $item['sname'] ?? '',
                    'status' => true
                ];
            }
        }
    
        // 主查询 - 属性值（type=2）
        $sql = "SELECT a.id, a.sid, a.name, a.type, 
                       b.id AS s_id, b.sid AS s_sid, b.name AS s_name, b.type AS s_type 
                FROM lkt_sku AS a 
                LEFT JOIN lkt_sku AS b ON a.sid = b.id 
                WHERE $condition 
                ORDER BY a.sid ASC, a.id ASC   -- 关键：加 a.id 保证排序稳定
                LIMIT $start, $pagesize";
    
        $r1 = Db::query($sql);
    
        foreach ($r1 as $row) {
            $list[] = [
                'id'     => (int)$row['s_id'],
                'name'   => $row['s_name'],
                'sid'    => (int)$row['id'],
                'sname'  => $row['name'],
                'status' => false
            ];
        }
    
        // 如果第一页加了 $strArr 的数据，但总数没包含它们
        // 这里可以选择：
        // 1. 不调整总数（前端显示时可能看到 total < 实际显示条数）
        // 2. 或者在总数里把 $strArr 的数量加进去（推荐）
        if ($page === 1 && !empty($strArr)) {
            $total += count($strArr);
        }
    
        return json_encode([
            'list'  => $list,
            'total' => $total
        ]);
    }

    // 获取属性名
    public function get_attribute0($store_id,$page,$keyword,$strArr = array())
    {
        $list = array();
        $id_list = array();
        $pagesize = 10;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $page = 1;
            $start = 0;
        }
        $end = $page * $pagesize;

        $condition = " status = 1 and recycle = 0 and is_examine = 1 ";

        if($keyword != '')
        {
            $keyword = Tools::FuzzyQueryConcatenation($keyword);
            $condition .= " and name like $keyword ";
        }
        $num = 0;
        if(count($strArr) > 0)
        {
            foreach ($strArr as $k => $v)
            {
                $id0 = $v['id0'];
                $id1 = $v['id1'];
                if(!in_array($id0,$id_list))
                {
                    $id_list[] = $id0;
                }
                if(!in_array($id1,$id_list))
                {
                    $id_list[] = $id1;
                }
                if($page == 1)
                {
                    if(!isset($v['name0']))
                    {
                        $v['name0'] = '';
                        $r_0 = SkuModel::where('id',$id0)->field('name')->select()->toArray();
                        if($r_0)
                        {
                            $v['name0'] = $r_0[0]['name'];
                        }
                    }
                    if(!isset($v['name1']))
                    {
                        $v['name1'] = '';
                        $r_1 = SkuModel::where('id',$id1)->field('name')->select()->toArray();
                        if($r_1)
                        {
                            $v['name1'] = $r_1[0]['name'];
                        }
                    }
                    $list[] = array('id' => $v['id0'], 'name' => $v['name0'],'sid' => $v['id1'], 'sname' => $v['name1'], 'status' => true);
                }
            }
        }
        // 查询SKU数据
        $r0 = SkuModel::where($condition)->field('id,sid,name,type')->select()->toArray();
        if($r0)
        {
            foreach ($r0 as $k0 => $v0)
            {
                if($v0['type'] == 1)
                { // 当为属性名时
                    $id = $v0['id']; // 属性名ID
                    if(!in_array($v0['id'],$id_list))
                    { // 属性ID不存在数组里
                        $id_list[] = $id;
                    }
                    // 根据属性名ID查询属性值信息
                    $r1 = SkuModel::where(['recycle'=>0,'type'=>2,'status'=>1,'sid'=>$id,'is_examine'=>1])->field('id,name')->select()->toArray();
                    if ($r1)
                    {
                        foreach ($r1 as $k1 => $v1)
                        {
                            if(!in_array($v1['id'],$id_list))
                            {
                                $id_list[] = $v1['id'];
                                if($start <= $num && $num < $end)
                                {
                                    $list[] = array('id' => $v0['id'], 'name' => $v0['name'],'sid' => $v1['id'], 'sname' => $v1['name'], 'status' => false);
                                }
                                else if($num >= $end)
                                {
                                    break 2;
                                }
                                $num++;
                            }
                        }
                    }
                }
                else
                {
                    $sid = $v0['sid']; // 属性名ID
                    if(!in_array($v0['id'],$id_list))
                    {
                        $id_list[] = $v0['id'];
                        // 根据属性名ID查询属性名信息
                        $r1 = SkuModel::where(['recycle'=>0,'type'=>1,'status'=>1,'id'=>$sid,'is_examine'=>1])->field('id,name')->select()->toArray();
                        if($r1)
                        {
                            if($start <= $num && $num < $end)
                            {
                                $list[] = array('id' => $r1[0]['id'], 'name' => $r1[0]['name'],'sid' => $v0['id'], 'sname' => $v0['name'], 'status' => false);
                            }
                            else if($num >= $end)
                            {
                                break;
                            }
                            $num++;
                        }
                    }
                }
            }
        }

        $data = array('list' => $list,'total' => count($r0));
        return json_encode($data);
    }
    
    public function add($obj = '')
    {
        $res1 = "L18";
        $res2 = "K";
        $res3 = "T";
        if ($obj == '')
        {
            $product_number = $res1 . '-' . $res2 . '001-' . $res3 . '001';
        }
        else
        {
            $arr = explode("-", $obj);
            $arr[1] = substr($arr[1], 1);
            $arr[2] = substr($arr[2], 1);
            $bit = 3;//产生7位数的数字编号

            if ((int)$arr[2] == 999)
            {
                $rew2 = (int)$arr[1] + 1;
                $rew3 = "001";
                $num_len = strlen($rew2);
                $zero = '';
                for ($i = $num_len; $i < $bit; $i++)
                {
                    $zero .= "0";
                }
                $real_num = $zero . $rew2;
                $product_number = $res1 . '-' . $res2 . $real_num . '-' . $res3 . $rew3;
            }
            else
            {
                $rew3 = (int)$arr[2] + 1;
                $num_len = strlen($rew3);
                $zero = '';
                for ($i = $num_len; $i < $bit; $i++)
                {
                    $zero .= "0";
                }
                $real_num = $zero . $rew3;
                $product_number = $res1 . '-' . $res2 . $arr[1] . '-' . $res3 . $real_num;
            }
        }
        return $product_number;
    }

    // 添加商品页面
    public function add_page($store_id,$admin_name,$mch_id,$type,$lang_code = 'zh_CN')
    {
        $time = date('Y-m-d H:i:s');
        $product_number = '';
        // // 查询最新一条已使用的商品编码
        // $r0 = ProductNumberModel::where(['store_id'=>$store_id,'status'=>1])->limit(1)->order('addtime', 'desc')->field('product_number')->select()->toArray();
        // if ($r0)
        // {
        //     $product_number1 = $r0[0]['product_number'];
        //     $product_number = $this->add($product_number1); // 获取商品编码
        // }
        // else
        // {
        //     $product_number = $this->add(); // 获取商品编码
        // }
        // // 添加一条商品编码记录
        // $data_product_number = array('store_id'=>$store_id,'mch_id'=>$mch_id,'operation'=>$admin_name,'product_number'=>$product_number,'addtime'=>$time);
        // $ProductNumberModel = new ProductNumberModel;
        // $r1 = $ProductNumberModel->save($data_product_number);

        $freight = array();
        $freight_num = 0;
        // 运费
        $r2 = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code])->order('add_time', 'desc')->field('id,name,is_default')->select()->toArray();
        if ($r2)
        {
            foreach ($r2 as $k2 => $v2)
            {
                $freight[$freight_num++] = (object)array('id' => $v2['id'], 'name' => $v2['name'], 'is_default' => $v2['is_default']);
            }
        }

        $sp_type = array();
        $r_sp_type = ProLabelModel::where(['store_id'=>$store_id,'lang_code'=>$lang_code])->order('add_time', 'desc')->field('id,name')->select()->toArray();
        if ($r_sp_type)
        {
            foreach ($r_sp_type as $k => $v)
            {
                $sp_type[] = array('name' => $v['name'], 'value' =>$v['id'], 'status' => false);
            }
        }

        $show_adr = Tools::get_data_dictionary(array('name'=>'商品展示位置','lang_code'=>$lang_code,'show_adr'=>array(0)));

        $unit = Tools::get_data_dictionary(array('name'=>'单位','lang_code'=>$lang_code));

        $Plugin = new Plugin();
        $Plugin_arr = $Plugin->pro_Plugin($store_id);
        
        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id);
        $mchStoreModels = MchPublicMethod::getMchStore($array);
        $haveStore = 0; // 0.没有门店 1.是有门店
        if($mchStoreModels != array())
        {
            $haveStore = 1; // 0.没有门店 1.是有门店
        }

        $data = array('product_number'=>$product_number,'Plugin_arr'=>$Plugin_arr,'freight'=>$freight,'sp_type'=>$sp_type,'show_adr'=>$show_adr,'unit'=>$unit,'mchStoreModels'=>$mchStoreModels['list'],'haveStore'=>$haveStore);
        return json_encode($data);
    }

    // 选择分类
    public function select_category($store_id,$cid,$brand_id)
    {
        $list = array();
        // 查询下级
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$cid,'examine'=>1])->order('sort', 'desc')->field('cid,pname,level')->select()->toArray();
        if ($r0)
        { // 有下级
            foreach ($r0 as $k => $v)
            {
                $r0[$k]['status'] = false;
            }
            $list[] = $r0;
        }

        $r2 = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$cid,'examine'=>1])->field('cid,sid')->select()->toArray();
        if ($r2)
        {
            $sid = $r2[0]['sid']; // 上级ID
            if ($sid == 0)
            {
                $cid = $r2[0]['cid'];
            }
            else
            {
                $Tools = new Tools($store_id, 1);
                $res = $Tools->str_option($sid);
                $res = explode('-', trim($res, '-'));
                $cid = $res[0];
            }
        }

        // $brand_list1 = array('brand_id' => '0', 'brand_name' => Lang('product.56'));
        $brand_list = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0,'status'=>0,'examine'=>1])->whereLike('categories','%,' . $cid . ',%')->order('sort', 'desc')->field('brand_id,brand_name,sort,notset')->select()->toArray();
        // array_unshift($brand_list, $brand_list1);
        if ($brand_id != 0 || $brand_id != '')
        {
            foreach ($brand_list as $k => $v)
            {
                if ($brand_id == $v['brand_id'])
                {
                    $brand_list[$k]['status'] = true;
                }
                else
                {
                    $brand_list[$k]['status'] = false;
                }
            }
        }

        $data = array('class_list' => $list, 'brand_list' => $brand_list);
        return json_encode($data);
    }

    // 移动端店铺-商品列表
    public function mobile_store_product_list($array)
    {
        $list = array();
        $store_id = $array['store_id'];
        $lang_code = Tools::get_lang($array['lang_code']);
        $admin_name = $array['zhanghao'];
        $mch_id = $array['mch_id'];
        $commodity_type = $array['commodity_type'];
        $mch_status = $array['mch_status'];
        $status = $array['status'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $ObtainCategoriesAndBrands_list = self::ObtainCategoriesAndBrands($store_id);
        $product_class_arr = $ObtainCategoriesAndBrands_list['product_class_arr'];
        $brand_class_arr = $ObtainCategoriesAndBrands_list['brand_class_arr'];

        $condition = "a.store_id = '$store_id' and a.recycle = 0 and a.is_presell = 0 and a.gongyingshang = 0 and a.mch_id = '$mch_id' ";
        if($commodity_type == 1)
        {
            $condition .= " and a.commodity_type = '1' ";
        }
        else
        {
            $condition .= " and a.commodity_type = '0' ";
        }

        if($lang_code != '')
        {
            $condition .= " and a.lang_code = '$lang_code' ";
        }

        if($mch_status == 2)
        {
            if($status == 2)
            {
                $condition .= " and a.mch_status = '2' and a.status = '2' ";
            }
            else
            {
                $condition .= " and a.mch_status = '2' and a.status != '2' ";
            }
        }
        else
        {
            $condition .= " and a.mch_status != '2' ";
        }

        $total = 0;
        $sql0 = "select count(a.id) as total from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition ";
        $r_pager = Db::query($sql0);
        if ($r_pager)
        {
            $total = $r_pager[0]['total'];
        }

        $sql1 = "select a.id,a.store_id,a.product_number,a.commodity_type,a.product_title,a.subtitle,a.label,a.scan,a.product_class,a.imgurl,a.content,a.richList,a.sort,a.add_date,a.upper_shelf_time,a.real_volume as volume,a.initial,a.s_type,a.num,a.min_inventory,a.status,a.brand_id,a.is_distribution,a.is_default_ratio,a.keyword,a.weight,a.weight_unit,a.freight,a.is_zhekou,a.separate_distribution,a.recycle,a.gongyingshang,a.is_hexiao,a.active,a.mch_id,a.mch_status,a.search_num,a.publisher,a.is_zixuan,a.source,a.comment_num,a.cover_map,a.class_sort,a.display_position_sort,a.is_presell,a.show_adr,b.name,a.refuse_reasons from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $start,$pagesize ";
        $r = Db::query($sql1);
        if($r)
        {
            foreach ($r as $key => $value)
            {
                $rew = $this->chaxun($value['id'],$store_id);
                if ($rew == 2)
                { //有参与插件活动
                    $value['rew'] = 1;
                }
                else if ($rew == 3)
                { //有未完成的订单
                    $value['rew'] = 2;
                }
                else
                {
                    $value['rew'] = 0;
                }

                $pid = $value['id'];
                $class = $value['product_class'];
                $bid = $value['brand_id'];
                $present_price = 0;
                $unit = '';

                $s_type = explode(',', trim($value['s_type'],','));
                $value['s_type'] = $s_type;
                $s_type_list = PC_Tools::getProductLabel(array('store_id'=>$store_id,'s_type'=>$s_type));
                $value['labelList'] = $s_type_list;
                
                $showAdrList = explode(',', trim($value['show_adr'],','));

                $value['showAdrList'] = $showAdrList;
                $value['showAdrNameList'] = $showAdrList;
                
                // 分类名称
                $pname = array_key_exists($class, $product_class_arr) ? $product_class_arr[$class]:'顶级';
                // 品牌名称
                $brand_name = array_key_exists($bid, $brand_class_arr) ? $brand_class_arr[$bid]:'暂无';
                $value['pname'] = $pname;
                $value['brand_name'] = $brand_name;
                
                // 查询商品库存
                $res_n = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('SUM(num) as num')->select()->toArray();
                $value['num'] = $res_n[0]['num'];
                
                $r_s = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('min(price) as price')->select()->toArray();
                if ($r_s)
                {
                    $present_price = round($r_s[0]['price'],2);
                }
                
                $r_unit = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('unit')->select()->toArray();
                if ($r_unit)
                {
                    $unit = $r_unit[0]['unit'];
                }
                
                $value['imgurl'] = ServerPath::getimgpath($value['imgurl'],$store_id);
                $value['unit'] = $unit;
                $value['price'] = $present_price;
                if($value['volume'] < 0)
                {
                    $value['volume'] = 0;
                }

                if($value['mch_status'] == 1)
                {
                    $value['status_name'] = "待审核";
                }
                else if($value['mch_status'] == 3)
                {
                    $value['status_name'] = "审核不通过";
                }
                else if($value['mch_status'] == 4)
                {
                    $value['status_name'] = "暂不审核";
                }
                else
                {
                    if($value['status'] == 1)
                    {
                        $value['status_name'] = '待上架';
                    }
                    else if($value['status'] == 2)
                    {
                        $value['status_name'] = '上架';
                    }
                    else if($value['status'] == 3)
                    {
                        $value['status_name'] = '下架';
                    }
                }

                $list[$key] = (object)$value;
            }
        }
        $data = array('total'=>$total,'list'=>$list);
        return json_encode($data);
    }

    // 查询商品参与状态
    public function chaxun($id,$store_id)
    {
        $r0 = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0,'mch_status'=>2,'id'=>$id])->field('product_class,status,brand_id,mch_id')->select()->toArray();
        if($r0)
        {
            if ($r0[0]['status'] == 2)
            { // 当为上架状态
                // 拼团
                $sql01 = "SELECT g_status from lkt_group_product where store_id = '$store_id' and product_id = '$id' and is_delete = 0 and (g_status = 2 or g_status = 1)";
                $r01 = Db::query($sql01);
                if ($r01)
                {
                    return 2;
                }
                $arr = array();//正在进行活动中的商品id数组
                // 竞拍
                $sql02 = "(select goods_id from lkt_auction_product where status in (0,1) ) union (select goods_id from lkt_auction_product where status = 2 )";
                $r02 = Db::query($sql02);
                if ($r02)
                {
                    foreach ($r02 as $k => $v)
                    {
                        $arr[$k] = $v['goods_id'];
                    }
                    if (in_array($id, $arr))
                    {
                        return 2;
                    }
                }

                // 优惠券
                $r03 = CouponActivityModel::where(['store_id'=>$store_id,'recycle'=>0])->whereIn('status','0,1,2')->field('type,product_class_id,product_id')->select()->toArray();
                if ($r03)
                {
                    foreach ($r03 as $key => $val)
                    {
                        if ($val['type'] == 2)
                        {
                            if($val['product_id'] != '')
                            {
                                $product_list = unserialize($val['product_id']);
                                $product_list = explode(',', $product_list);
                                if (in_array($id, $product_list))
                                {
                                    return 2;
                                }
                            }
                        }
                    }
                }

                $sql1 = "select a.* from lkt_product_list as a left join lkt_order_details as b on a.id = b.p_id where a.store_id = '$store_id' and a.recycle = 0 and a.mch_status = 2 and r_status in (0,1,2) and a.id = '$id'";
                $r1 = Db::query($sql1);
                if ($r1)
                {
                    return 3;
                }

                //会员赠送商品
                $res_06 = UserRuleModel::where(['store_id'=>$store_id])
                                        ->field('is_product')
                                        ->select()
                                        ->toArray();
                if ($res_06)
                {
                    $is_product = $res_06[0]['is_product'];//0-未开通 1-开通
                    if ($is_product == 1)
                    {
                        $res06 = UserGradeModel::where(['store_id'=>$store_id,'pro_id'=>$id])
                                                ->field('id')
                                                ->select()
                                                ->toArray();
                        if ($res06)
                        {
                            return 2;
                        }
                    }
                }
                return 1;
            }
            else
            {
                return 1;
            }
        }
        return;
    }
    
    // 添加自选商品页面（获取平台商品）
    public function get_optional_pro_list($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $classnotset = 0;
        if(isset($array['classnotset']))
        {
            $classnotset = $array['classnotset'];
        }
        $product_class = $array['product_class'];
        $brandnotset = 0;
        if(isset($array['brandnotset']))
        {
            $brandnotset = $array['brandnotset'];
        }
        $brand_id = $array['brand_id'];
        $examineStatus = $array['examineStatus']; // 审核状态  5.待审核 4.待提交 3 审核失败
        $product_title = $array['product_title'];
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = Tools::get_lang($array['lang_code']);
        }
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $page = 1;
        }

        $ObtainCategoriesAndBrands_list = self::ObtainCategoriesAndBrands($store_id);
        $product_class_arr = $ObtainCategoriesAndBrands_list['product_class_arr'];
        $brand_class_arr = $ObtainCategoriesAndBrands_list['brand_class_arr'];

        $sql_admin = "select shop_id from lkt_admin where store_id = '$store_id' and type = 1 ";
        $r_admin = Db::query($sql_admin);
        if($r_admin)
        {
            $shop_id = $r_admin[0]['shop_id'];
        }
        $list = array();
        $total = 0;
        if($mch_id != $shop_id)
        {
            $condition = "a.store_id = '$store_id' and a.recycle = 0 and a.active = 1 and a.status = 2 and a.mch_status = 2 and a.is_presell = 0 and a.commodity_type = 0 and a.is_zixuan = 1 and a.mch_id = '$shop_id' ";

            $Tools = new Tools($store_id, 1);
            $res = $Tools->query_product($store_id,$mch_id);
            if($res != '')
            {
                $condition .= $res;
            }
            
            $class_list = array();
            if ($product_class != 0 && $product_class != '')
            {
                if($classnotset == 1)
                {
                    $condition .= " and a.product_class = '' ";
                }
                else
                {
                    $Tools = new Tools($store_id, 1);
                    $product_class1 = $Tools->str_option( $product_class);

                    $condition .= " and a.product_class like '%$product_class1%' ";
                    $res = explode('-',trim($product_class1,'-'));
                    $class_id0 = $res[0]; //  商品所属分类的顶级
                    $shuliang = count($res)-1;
                    $class_id1 = $res[$shuliang]; // 商品所属分类
                    foreach ($res as $k => $v)
                    {
                        $r = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$v,'examine'=>1])->field('cid,pname,level')->select()->toArray();
                        if ($r)
                        {
                            $class_list[] = $r[0];
                        }
                    }
                }
            } 

            if ($brand_id != 0 && $brand_id != '')
            {
                if($brandnotset == 1)
                {
                    $condition .= " and a.brand_id = 0 ";
                }
                else
                {
                    $condition .=  " and a.brand_id like '$brand_id' ";
                }
            }

            if ($product_title != '')
            {
                if(strpos($product_title," "))
                {
                    $product_title1 = explode(' ',$product_title);

                    $condition1 = ' and (';
                    foreach ($product_title1 as $k => $v)
                    {
                        if($v)
                        {
                            $v = Tools::FuzzyQueryConcatenation($v);
                            $condition1 .= " a.product_title like $v or b.name like $v or ";
                        }
                    }

                    $condition1 = substr($condition1,0,strlen($condition1)-3);
                    $condition1 .= ' )';
                    $condition .= $condition1;
                }
                else
                {
                    $product_title = Tools::FuzzyQueryConcatenation($product_title);
                    $condition .= " and (a.product_title like $product_title or b.name like $product_title ) ";
                }
            }

            if($lang_code != '')
            {
                $condition .= " and a.lang_code = '$lang_code' ";
            }

            $sort = " a.sort desc,a.upper_shelf_time desc ";

            $sql0 = "select count(a.id) as total from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition ";
            $r_pager = Db::query($sql0);
            $total = $r_pager[0]['total'];

            $sql1 = "select a.id,a.store_id,a.product_number,a.commodity_type,a.product_title,a.subtitle,a.label,a.scan,a.product_class,a.imgurl,a.content,a.richList,a.sort,a.add_date,a.upper_shelf_time,a.volume,a.initial,a.s_type,a.num,a.min_inventory,a.status,a.brand_id,a.is_distribution,a.is_default_ratio,a.keyword,a.weight,a.weight_unit,a.freight,a.is_zhekou,a.separate_distribution,a.recycle,a.gongyingshang,a.is_hexiao,a.active,a.mch_id,a.mch_status,a.search_num,a.publisher,a.is_zixuan,a.source,a.comment_num,a.cover_map,a.class_sort,a.display_position_sort,a.is_presell,a.show_adr,b.name,a.refuse_reasons from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by $sort limit $start,$pagesize ";
            $r = Db::query($sql1);
            if($r)
            {
                $total1 = count($r);
                foreach ($r as $key => $value)
                {
                    $product = new Product();
                    $rew = $product->chaxun($value['id'],$store_id);
                    if ($rew == 2)
                    { // 有参与插件活动
                        $value['rew'] = 1;
                    }
                    else if ($rew == 3)
                    { // 有未完成的订单
                        $value['rew'] = 2;
                    }
                    else
                    {
                        $value['rew'] = 0;
                    }

                    $pid = $value['id'];
                    $class = $value['product_class'];
                    $bid = $value['brand_id'];
                    $shop_id = $value['mch_id'];
                    $present_price = 0;
                    $unit = '';

                    // 获取上一条数据的ID
                    if($key == 0)
                    { // 为当前页面第一条时
                        if ($page)
                        { // 有页码
                            $start1 = $start-1; // 上一页最后一条数据
                            if($start1 < 0)
                            {
                                $value['upper_status'] = false; // 下移
                                $upper_id = '';
                            }
                            else
                            {
                                // 查询上一页最后一条数据
                                $sql2 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $start1,1";
                                $r2 = Db::query($sql2);
                                $upper_id = $r2[0]['id'];
                                $value['upper_status'] = true;
                            }
                        }
                        else
                        {
                            $value['upper_status'] = false; // 下移
                            $upper_id = '';
                        }
                    }
                    else
                    {
                        $key1 = $key-1;
                        $upper_id = $r[$key1]['id']; // 上条数据ID
                        $value['upper_status'] = true;
                    }
                    // 获取下一条数据的ID
                    if($key == $total1-1)
                    {  // 为当页面最后一条时
                        if ($page)
                        {  // 有页码
                            if($page == 1)
                            {
                                $sql3 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $pagesize,1";
                            }
                            else
                            {
                                $start2 = $start + $pagesize;
                                $sql3 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $start2,1";
                            }
                        }
                        else
                        {
                            $sql3 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $pagesize,1";
                        }
                        $r3 = Db::query($sql3);
                        if($r3)
                        {
                            $underneath_id = $r3[0]['id']; // 下条数据ID
                        }
                        else
                        {
                            $underneath_id = '';
                        }
                    }
                    else
                    {
                        $key2 = $key+1;
                        $underneath_id = $r[$key2]['id']; // 下条数据ID
                    }
                    $value['upper_id'] = $upper_id;
                    $value['underneath_id'] = $underneath_id;

                    $min_inventory = $value['min_inventory'];
                    $s_type = explode(',', $value['s_type']);
                    $s_type_list = PC_Tools::getProductLabel(array('store_id'=>$store_id,'s_type'=>$s_type));
                    $value['labelList'] = $s_type_list;

                    $showAdrList = explode(',', trim($value['show_adr'],','));

                    $value['showAdrList'] = $showAdrList;
                    $value['showAdrNameList'] = $showAdrList;

                    // 分类名称
                    $pname = array_key_exists($class, $product_class_arr) ? $product_class_arr[$class]:'顶级';
                    // 品牌名称
                    $brand_name = array_key_exists($bid, $brand_class_arr) ? $brand_class_arr[$bid]:'暂无';
                    $value['pname'] = $pname;
                    $value['brand_name'] = $brand_name;
                    // 查询商品库存
                    $res_n = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('SUM(num) as num')->select()->toArray();
                    $value['num'] = $res_n[0]['num'];

                    $r_s = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('min(price) as price,costprice')->select()->toArray();
                    if ($r_s)
                    {
                        $present_price = round($r_s[0]['price'],2);
                        $costprice = round($r_s[0]['costprice'],2);
                    }
                    
                    $r_unit = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('unit')->select()->toArray();
                    if ($r_unit)
                    {
                        $unit = $r_unit[0]['unit'];
                    }

                    $value['imgurl'] = ServerPath::getimgpath($value['imgurl'],$store_id);
                    $value['unit'] = $unit;
                    $value['price'] = $present_price;
                    $value['sj'] = $present_price;
                    $value['cbj'] = $costprice;
                    if($value['volume'] < 0)
                    {
                        $value['volume'] = 0;
                    }
                    if($value['status'] == 1)
                    {
                        $value['status_name'] = '待上架';
                    }
                    else if($value['status'] == 2)
                    {
                        $value['status_name'] = '上架';
                    }
                    else if($value['status'] == 3)
                    {
                        $value['status_name'] = '下架';
                    }
                    
                    $value['product_title'] = htmlspecialchars($value['product_title']);

                    $list[$key] = (object)$value;
                }
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }
    
    // 自选商品
    public function self_selected_products($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $classnotset = 0;
        if(isset($array['classnotset']))
        {
            $classnotset = $array['classnotset'];
        }
        $product_class = $array['product_class'];
        $brandnotset = 0;
        if(isset($array['brandnotset']))
        {
            $brandnotset = $array['brandnotset'];
        }
        $brand_id = $array['brand_id'];
        $status = $array['status'];
        $product_title = $array['product_title'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $mch_status = $array['mch_status'];
        $commodity_type = $array['commodity_type'];
        $parameter = $array['parameter'];
        $source = $array['source'];
        $lang_code = "";
        if(isset($array['lang_code']) && $array['lang_code'] != '')
        {
            $lang_code = Tools::get_lang($array['lang_code']);
        }

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $page = 1;
        }

        $ObtainCategoriesAndBrands_list = self::ObtainCategoriesAndBrands($store_id);
        $product_class_arr = $ObtainCategoriesAndBrands_list['product_class_arr'];
        $brand_class_arr = $ObtainCategoriesAndBrands_list['brand_class_arr'];

        $condition = "a.store_id = '$store_id' and a.recycle = 0 and a.active = 1 and a.mch_status = 2 and a.is_presell = 0 and a.commodity_type = 0 and a.is_zixuan = 0 and a.mch_id = '$mch_id' and a.gongyingshang = 0 ";

        $class_list = array();
        if($parameter != '')
        {
            $condition .= " and a.id = '$parameter' ";
            $message_logging_list_8 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$parameter,'type'=>8);
            PC_Tools::message_read($message_logging_list_8);

            $message_logging_list_10 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$parameter,'type'=>10);
            PC_Tools::message_read($message_logging_list_10);
        }

        $class_list = array();
        if ($product_class != 0 && $product_class != '')
        {
            if($classnotset == 1)
            {
                $condition .= " and a.product_class = '' ";
            }
            else
            {
                $Tools = new Tools($store_id, 1);
                $product_class1 = $Tools->str_option( $product_class);

                $condition .= " and a.product_class like '%$product_class1%' ";
                $res = explode('-',trim($product_class1,'-'));
                $class_id0 = $res[0]; //  商品所属分类的顶级
                $shuliang = count($res)-1;
                $class_id1 = $res[$shuliang]; // 商品所属分类
                foreach ($res as $k => $v)
                {
                    $r = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'cid'=>$v,'examine'=>1])->field('cid,pname,level')->select()->toArray();
                    if ($r)
                    {
                        $class_list[] = $r[0];
                    }
                }
            }
        }

        if ($brand_id != 0 && $brand_id != '')
        {
            if($brandnotset == 1)
            {
                $condition .= " and a.brand_id = 0 ";
            }
            else
            {
                $condition .=  " and a.brand_id like '$brand_id' ";
            }
        }

        if($status != '')
        {
            $condition .= " and a.status = $status ";
        }

        if ($product_title != '')
        {
            if(strpos($product_title," "))
            {
                $product_title1 = explode(' ',$product_title);

                $condition1 = ' and (';
                foreach ($product_title1 as $k => $v)
                {
                    if($v)
                    {
                        $v = Tools::FuzzyQueryConcatenation($v);
                        $condition1 .= " a.product_title like $v or b.name like $v or ";
                    }
                }

                $condition1 = substr($condition1,0,strlen($condition1)-3);
                $condition1 .= ' )';
                $condition .= $condition1;
            }
            else
            {
                $product_title = Tools::FuzzyQueryConcatenation($product_title);
                $condition .= " and (a.product_title like $product_title or b.name like $product_title ) ";
            }
        }

        if($lang_code != '')
        {
            $condition .= " and a.lang_code = '$lang_code' ";
        }

        $list = array();
        $sort = " a.mch_sort desc,a.upper_shelf_time desc ";

        $sql0 = "select count(a.id) as total from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition ";
        $r_pager = Db::query($sql0);
        $total = $r_pager[0]['total'];

        $sql1 = "select a.id,a.store_id,a.product_number,a.commodity_type,a.product_title,a.subtitle,a.label,a.scan,a.product_class,a.imgurl,a.content,a.richList,a.sort,a.add_date,a.upper_shelf_time,a.volume,a.initial,a.s_type,a.num,a.min_inventory,a.status,a.brand_id,a.is_distribution,a.is_default_ratio,a.keyword,a.weight,a.weight_unit,a.freight,a.is_zhekou,a.separate_distribution,a.recycle,a.gongyingshang,a.is_hexiao,a.active,a.mch_id,a.mch_status,a.search_num,a.publisher,a.is_zixuan,a.source,a.comment_num,a.cover_map,a.class_sort,a.display_position_sort,a.is_presell,a.show_adr,b.name,a.refuse_reasons,a.receiving_form,a.mch_sort,a.lang_code from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by $sort limit $start,$pagesize ";
        $r = Db::query($sql1);
        if($r)
        {
            $total1 = count($r);
            foreach ($r as $key => $value)
            {
                $product = new Product();
                $rew = $product->chaxun($value['id'],$store_id);
                if ($rew == 2)
                { // 有参与插件活动
                    $value['rew'] = 1;
                }
                else if ($rew == 3)
                { // 有未完成的订单
                    $value['rew'] = 2;
                }
                else
                {
                    $value['rew'] = 0;
                }

                $pid = $value['id'];
                $class = $value['product_class'];
                $bid = $value['brand_id'];
                $shop_id = $value['mch_id'];
                $present_price = 0;
                $unit = '';

                // 获取上一条数据的ID
                if($key == 0)
                { // 为当前页面第一条时
                    if ($page)
                    { // 有页码
                        $start1 = $start-1; // 上一页最后一条数据
                        if($start1 < 0)
                        {
                            $value['upper_status'] = false; // 下移
                            $upper_id = '';
                        }
                        else
                        {
                            // 查询上一页最后一条数据
                            $sql2 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $start1,1";
                            $r2 = Db::query($sql2);
                            $upper_id = $r2[0]['id'];
                            $value['upper_status'] = true;
                        }
                    }
                    else
                    {
                        $value['upper_status'] = false; // 下移
                        $upper_id = '';
                    }
                }
                else
                {
                    $key1 = $key-1;
                    $upper_id = $r[$key1]['id']; // 上条数据ID
                    $value['upper_status'] = true;
                }
                // 获取下一条数据的ID
                if($key == $total1-1)
                {  // 为当页面最后一条时
                    if ($page)
                    {  // 有页码
                        if($page == 1)
                        {
                            $sql3 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $pagesize,1";
                        }
                        else
                        {
                            $start2 = $start + $pagesize;
                            $sql3 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $start2,1";
                        }
                    }
                    else
                    {
                        $sql3 = "select a.id from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort desc limit $pagesize,1";
                    }
                    $r3 = Db::query($sql3);
                    if($r3)
                    {
                        $underneath_id = $r3[0]['id']; // 下条数据ID
                    }
                    else
                    {
                        $underneath_id = '';
                    }
                }
                else
                {
                    $key2 = $key+1;
                    $underneath_id = $r[$key2]['id']; // 下条数据ID
                }
                $value['upper_id'] = $upper_id;
                $value['underneath_id'] = $underneath_id;

                $min_inventory = $value['min_inventory'];
                $s_type = explode(',', $value['s_type']);
                $s_type_list = PC_Tools::getProductLabel(array('store_id'=>$store_id,'s_type'=>$s_type));
                $value['labelList'] = $s_type_list;

                $showAdrList = explode(',', trim($value['show_adr'],','));

                $value['showAdrList'] = $showAdrList;
                $value['showAdrNameList'] = $showAdrList;

                // 分类名称
                $pname = array_key_exists($class, $product_class_arr) ? $product_class_arr[$class]:'顶级';
                // 品牌名称
                $brand_name = array_key_exists($bid, $brand_class_arr) ? $brand_class_arr[$bid]:'暂无';
                $value['pname'] = $pname;
                $value['brand_name'] = $brand_name;
                // 查询商品库存
                $res_n = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('SUM(num) as num')->select()->toArray();
                $value['num'] = $res_n[0]['num'];

                $r_s = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('min(price) as price')->select()->toArray();
                if ($r_s)
                {
                    $present_price = round($r_s[0]['price'],2);
                }
                
                $r_unit = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('unit')->select()->toArray();
                if ($r_unit)
                {
                    $unit = $r_unit[0]['unit'];
                }

                $value['imgurl'] = ServerPath::getimgpath($value['imgurl'],$store_id);
                $value['unit'] = $unit;
                $value['price'] = $present_price;
                $value['sj'] = $present_price;
                if($value['volume'] < 0)
                {
                    $value['volume'] = 0;
                }
                if($value['status'] == 1)
                {
                    $value['status_name'] = '待上架';
                }
                else if($value['status'] == 2)
                {
                    $value['status_name'] = '上架';
                }
                else if($value['status'] == 3)
                {
                    $value['status_name'] = '下架';
                }
                
                $value['product_title'] = htmlspecialchars($value['product_title']);
                $value['lang_name'] = langCode2Name($value['lang_code']);

                $list[$key] = (object)$value;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 自选商品-添加商品
    public function Add_self_selection($array)
    {
        Db::startTrans();
        
        $Jurisdiction = new Jurisdiction();
        $time = date("Y-m-d H:i:s");
        $store_id = $array['store_id'];
        $pro_list = $array['pro_list'];
        $freight_id = $array['freight_id'];
        $admin_name = $array['admin_name'];
        $language = $array['language'];
        $mch_id = $array['mch_id'];
        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $source = 1;
        if(isset($array['source']))
        {
            $source = $array['source'];
        }

        if($freight_id == '' || $freight_id == 0)
        {
            echo json_encode(array('code' => 109, 'message' => '请选择运费模板名称！'));
            exit;
        }

        $r_freight = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$freight_id])->field('id')->select()->toArray();
        if(!$r_freight)
        {
            echo json_encode(array('code' => 109, 'message' => '参数错误！'));
            exit;
        }

        $r_mch = MchModel::where(['id'=>$mch_id])->field('name')->select()->toArray();
        if ($r_mch)
        {
            $name = $r_mch[0]['name'];
        }

        foreach ($pro_list as $k => $v)
        {
            // 查询商品信息
            $r1 = ProductListModel::where(['store_id'=>$store_id,'status'=>2,'mch_status'=>2,'recycle'=>0,'active'=>1,'id'=>$v])->field('commodity_type,product_title,product_class,brand_id,subtitle,scan,imgurl,content,richList,sort,initial,s_type,num,min_inventory,status,is_distribution,is_default_ratio,keyword,weight,weight_unit,distributor_id,freight,separate_distribution,is_hexiao,hxaddress,active,mch_status,show_adr,commodity_str,cover_map,video,pro_video,lang_code')->select()->toArray();
            if ($r1)
            {
                $commodity_type = $r1[0]['commodity_type'];
                $product_title = $r1[0]['product_title'];
                $product_class = $r1[0]['product_class'];
                $brand_id = $r1[0]['brand_id'];
                $subtitle = $r1[0]['subtitle'];
                $scan = $r1[0]['scan'];
                $keyword = $r1[0]['keyword'];
                $weight = $r1[0]['weight'];
                $weight_unit = $r1[0]['weight_unit'];
                $imgurl = $r1[0]['imgurl'];
                $sort = $r1[0]['sort'];
                $content = $r1[0]['content'];
                $richList = $r1[0]['richList'];
                $z_num = $r1[0]['num'];
                $min_inventory = $r1[0]['min_inventory'];
                $s_type = $r1[0]['s_type'];
                $is_distribution = $r1[0]['is_distribution'];
                $distributor_id = $r1[0]['distributor_id'];
                $is_hexiao = $r1[0]['is_hexiao'];
                $hxaddress = $r1[0]['hxaddress'];
                $active = $r1[0]['active'];
                $show_adr = $r1[0]['show_adr'];
                $initial = $r1[0]['initial'];
                $cover_map = $r1[0]['cover_map'];
                $video = $r1[0]['video'];
                $pro_video = $r1[0]['pro_video'];
                $lang_code = $r1[0]['lang_code'];

                // 复制商品信息
                $sql2 = array('store_id'=>$store_id,'product_number'=>'','commodity_type'=>$commodity_type,'product_title'=>$product_title,'subtitle'=>$subtitle,'scan'=>$scan,'product_class'=>$product_class,'brand_id'=>$brand_id,'keyword'=>$keyword,'weight'=>$weight,'weight_unit'=>$weight_unit,'imgurl'=>$imgurl,'sort'=>$sort,'content'=>$content,'richList'=>$richList,'num'=>$z_num,'min_inventory'=>$min_inventory,'status'=>2,'upper_shelf_time'=>$time,'s_type'=>$s_type,'add_date'=>$time,'is_distribution'=>$is_distribution,'distributor_id'=>$distributor_id,'freight'=>$freight_id,'active'=>$active,'mch_id'=>$mch_id,'mch_status'=>2,'show_adr'=>$show_adr,'initial'=>$initial,'publisher'=>$name,'is_zixuan'=>0,'cover_map'=>$cover_map,'video'=>$video,'pro_video'=>$pro_video,'is_presell'=>0,'lang_code'=>$lang_code);
                $r2 = Db::name('product_list')->insertGetId($sql2);
                if ($r2)
                {
                    $commodity_str1 = unserialize($r1[0]['commodity_str']);
                    $commodity_str1[] = $r2;

                    $commodity_str = serialize($commodity_str1);
                    // 修改商品ID字符串
                    $r3 = Db::name('product_list')->where('id', $v)->update(['commodity_str' => $commodity_str]);
                    if ($r3 == -1)
                    {
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, ' 添加了商品ID： ' . $v . '，为代售商品失败',2,$source,$mch_id,$operator_id);
                        echo json_encode(array('code' => 110, 'message' => '业务异常！'));
                        exit;
                    }
                    // 查询商品轮播图

                    $r4 = ProductImgModel::where(['product_id'=>$v])->field('product_url')->select()->toArray();
                    if ($r4)
                    {
                        foreach ($r4 as $ke => $va)
                        {
                            $product_url = $va['product_url'];
                            // 添加新的商品轮播图
                            $sql_img = array('product_url'=>$product_url,'product_id'=>$r2,'add_date'=>$time);
                            $r_img = Db::name('product_img')->insert($sql_img);
                            if ($r_img < 1)
                            {
                                Db::rollback();
                                $Jurisdiction->admin_record($store_id, $operator, ' 添加了商品ID： ' . $v . '，为代售商品失败',2,$source,$mch_id,$operator_id);
                                echo json_encode(array('code' => 110, 'message' => '业务异常！'));
                                exit;
                            }
                        }
                    }
                    // 查询商品属性
                    $r5 = ConfigureModel::where(['pid'=>$v,'recycle'=>0])->field('id,costprice,price,yprice,img,total_num,num,unit,status,attribute,min_inventory,attribute_str')->select()->toArray();
                    if ($r5)
                    {
                        foreach ($r5 as $key => $val)
                        {
                            $attribute_id = $val['id'];
                            $attribute_str1 = unserialize($val['attribute_str']);

                            $sql_attribute = array('costprice'=>$val['costprice'],'price'=>$val['price'],'yprice'=>$val['yprice'],'img'=>$val['img'],'unit'=>$val['unit'],'status'=>$val['status'],'attribute'=>$val['attribute'],'min_inventory'=>$val['min_inventory'],'pid'=>$r2,'total_num'=>$val['total_num'],'num'=>$val['num'],'ctime'=>$time,'attribute_str'=>'');
                            $r_attribute = Db::name('configure')->insertGetId($sql_attribute);
                            if ($r_attribute < 1)
                            {
                                Db::rollback();
                                $Jurisdiction->admin_record($store_id, $operator, ' 添加了商品ID： ' . $v . '，为代售商品失败',2,$source,$mch_id,$operator_id);
                                echo json_encode(array('code' => 110, 'message' => '业务异常！'));
                                exit;
                            }
                            $total_num = $val['total_num'];
                            $num = $val['num'];
                            // 在库存记录表里，添加一条入库信息
                            $content0 = $admin_name . '增加商品总库存' . $num;
                            $sql_stock0 = array('store_id'=>$store_id,'product_id'=>$r2,'attribute_id'=>$r_attribute,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>0,'add_date'=>$time,'content'=>$content0);
                            $r_stock0 = Db::name('stock')->insert($sql_stock0);
                            if($r_stock0 < 1)
                            {
                                Db::rollback();
                                $Jurisdiction->admin_record($store_id, $operator, ' 添加了商品ID： ' . $v . '，为代售商品失败',2,$source,$mch_id,$operator_id);
                                echo json_encode(array('code' => 110, 'message' => '库存记录添加失败，请稍后再试！'));
                                exit;
                            }

                            if ($min_inventory >= $num)
                            { // 当属性库存低于等于预警值
                                $content1 = '预警';
                                // 在库存记录表里，添加一条预警信息
                                $sql_stock1 = array('store_id'=>$store_id,'product_id'=>$r2,'attribute_id'=>$r_attribute,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>2,'add_date'=>$time,'content'=>$content1);
                                $r_stock1 = Db::name('stock')->insert($sql_stock1);
                                if($r_stock1 < 1)
                                {
                                    Db::rollback();
                                    $Jurisdiction->admin_record($store_id, $operator, ' 添加了商品ID： ' . $v . '，为代售商品失败',2,$source,$mch_id,$operator_id);
                                    echo json_encode(array('code' => 110, 'message' => '库存记录添加失败，请稍后再试！'));
                                    exit;
                                }
                            }

                            $attribute_str1[] = $r_attribute;
                            $attribute_str = serialize($attribute_str1);

                            $r6 = Db::name('configure')->where(['pid'=>$v,'id'=>$attribute_id])->update(['attribute_str' => $attribute_str]);
                            if ($r6 == -1)
                            {
                                Db::rollback();
                                $Jurisdiction->admin_record($store_id, $operator, ' 添加了商品ID： ' . $v . '，为代售商品失败',2,$source,$mch_id,$operator_id);
                                echo json_encode(array('code' => 110, 'message' => '业务异常！'));
                                exit;
                            }
                        }
                    }

                    $Jurisdiction->admin_record($store_id, $operator, ' 添加了商品ID： ' . $v . '，为代售商品成功',2,$source,$mch_id,$operator_id);
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $operator, ' 添加了商品ID： ' . $v . '，为代售商品失败',2,$source,$mch_id,$operator_id);
                    Db::rollback();
                    echo json_encode(array('code' => 110, 'message' => '业务异常！'));
                    exit;
                }
            }
        }
        Db::commit();
        echo json_encode(array('code' => 200, 'message' => '成功！'));
        exit;
    }

    // 添加商品（预售）
    public function add_presale_product($store_id,$admin_name,$mch_id,$array,$type0)
    {
        $time = date("Y-m-d H:i:s");

        $Tools = new Tools($store_id, 1);
        $Jurisdiction = new Jurisdiction();
        // 启动事务
        Db::startTrans();

        $isVirtual = 0;
        $product_title = $array['product_title'];
        $subtitle = $array['subtitle'];
        $keyword = isset($array['keyword'])?$array['keyword']:'';
        $product_class = $array['product_class'];
        $brand_id = $array['brand_id'];
        $weight = $array['weight'];
        $weight_unit = 'kg';
        $imgurls = $array['imgurls'];
        $initial = $array['initial'];
        $unit = $array['unit'];
        $attr = $array['attr'];
        $min_inventory = $array['min_inventory'];
        $freight = $array['freight'];
        $s_type = $array['s_type'];
        $show_adr = $array['show_adr'];
        $active = $array['active'];
        $content = $array['content'];
        $richList = $array['richList'];
        $mch_status = $array['mch_status'];
        $cover_map = $array['cover_map'];
        $video = ''; // 展示视频
        if(isset($array['video']))
        {
            $video = $array['video']; // 展示视频
        }
        $pro_video = ''; // 商品视频
        if(isset($array['pro_video']))
        {
            $pro_video = $array['pro_video']; // 商品视频
        }
        $is_presale = 1; // 预售商品
        $sell_type = $array['sell_type'];
        $deposit = $array['deposit'];
        $pay_type = $array['pay_type'];
        $deposit_start_time = $array['deposit_start_time'];
        $deposit_end_time = $array['deposit_end_time'];
        $time = date("Y-m-d H:i:s");
        $balance_pay_time = $array['balance_pay_time'];
        $sellNum = $array['sellNum'];
        $endDay = $array['endDay'];
        $delivery_time = $array['delivery_time'];

        $type = $array['type'];

        $shop_id_record = $array['shop_id_record'];
        $operator_record = $array['operator_record'];
        $source_record = $array['source_record'];
        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }

        if (empty($product_title))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品标题不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.8');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }
        // else
        // {
        //     if(strlen($product_title) > 20)
        //     {
        //         $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品标题不能大于20个字符';
        //         $this->proLog($type0,$Log_content);
        //         $message = Lang('product.101');
        //         echo json_encode(array('code' => 109,  'message' => $message));
        //         exit();
        //     }
        // }

        if (empty($product_class))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品类别不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.11');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }
        else
        {
            $product_class = $Tools->str_option($product_class);
        }

        if (empty($brand_id))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 品牌不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.12');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        if (is_numeric($weight))
        {
            if ($weight < 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 重量不能为负数';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.13');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 重量请填写数字';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.14');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        if(empty($cover_map))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品封面图不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.17');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }
        else
        {
            $cover_map = preg_replace('/.*\//', '', $cover_map);
            if ($cover_map == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品封面图不能为空';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.17');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }

        if (!empty($imgurls))
        {
            // 检查键名 "center" 是否存在于数组中：
            if (array_key_exists('center', $imgurls))
            {
                $image = preg_replace('/.*\//', '', $imgurls['center']);
                unset($imgurls['center']);
            }
            else
            {
                $image = preg_replace('/.*\//', '', $imgurls['0']);
                if ($image == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品展示图不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.16');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
                unset($imgurls[0]);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品展示图不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.16');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }

        if ($initial)
        {
            $initial1 = explode(',', $initial); // 转数组

            foreach ($initial1 as $k => $v)
            {
                if($v != '')
                {
                    $initial2 = explode('=', $v); // 转数组
                    if($initial2[0] == 'stockWarn')
                    {
                        $initial2[0] == 'kucun';
                    }
                    $key[] = $initial2[0];
                    $val[] = $initial2[1];
                    if ($initial2[0] == 'kucun')
                    {
                        $kucun = $initial2[1];
                    }
                }
            }

            $initial = array_combine($key, $val); // 创建一个数组，用一个数组的值作为其键名，另一个数组的值作为其值
            foreach ($initial as $k => $v)
            {
                if ($k == 'cbj' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 成本价初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.18');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'yj' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 原价初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.19');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'sj')
                {
                    if($v == '')
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 售价初始值不能为空';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.20');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                    elseif($type0 != '店铺' && $type0 != 'PC店铺' && $is_presale && $v <= $deposit)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 定金不能大于售价';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.21');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
                else if ($k == 'unit' && $v == '0')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 单位初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.22');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'kucun' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 库存初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.23');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }

                if ($k == 'cbj' || $k == 'yj' || $k == 'sj' || $k == 'kucun' )
                {
                    if (is_numeric($v))
                    {
                        if ($v < 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值不能为负数';
                            $this->proLog($type0,$Log_content);
                            $message = Lang('product.24');
                            echo json_encode(array('code' => 109,  'message' => $message));
                            exit();
                        }
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值请填写数字';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.25');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
            }
            $initial = serialize($initial);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.26');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }

        $z_num = 0;
        $attributes = array();
        //处理属性
        if (count($attr) == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写属性';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.29');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }
        else
        {
            $foreach_num = 0;
            foreach ($attr as $k => $v)
            {
                $foreach_num++;
                foreach ($v['attr_list'] as $ke => $va)
                {
                    $attr_group_name_id = Tools::attribute_id($store_id, 1, $va['attr_group_name'], 0);
                    if ($attr_group_name_id != 0)
                    { // 当属性名称ID不为0，SKU表里有数据
                        $attr_name_id = Tools::attribute_id($store_id, 2, $va['attr_name'], $attr_group_name_id);
                        if ($attr_name_id == 0)
                        { // 当属性值ID为0，SKU表里没有数据
                            $attr_name_id = $Tools->add_attribute($store_id, $attr_group_name_id, $va['attr_name'], 2, $admin_name, $foreach_num,$type0); // 添加属性值
                        }
                    }
                    else
                    { // 当属性名称ID为0，SKU表里没有数据
                        $attr_group_name_id = $Tools->add_attribute($store_id, 0, $va['attr_group_name'], 1, $admin_name, 0,$type0); // 添加属性名
                        $attr_name_id = $Tools->add_attribute($store_id, $attr_group_name_id, $va['attr_name'], 2, $admin_name, $foreach_num,$type0); // 添加属性值
                    }
                    $attr[$k]['attr_list'][$ke]['attr_group_name'] = $va['attr_group_name'] . '_LKT_' . $attr_group_name_id; // 拼接属性名
                    $attr[$k]['attr_list'][$ke]['attr_name'] = $va['attr_name'] . '_LKT_' . $attr_name_id; // 拼接属性值
                }
            }
            foreach ($attr as $key => $value)
            {
                $attr_list = $value['attr_list'];
                $attr_list_arr = array();
                $attr_list_srt = '';
                foreach ($attr_list as $k => $v)
                {
                    $attr_list_arr[$v['attr_group_name']] = $v['attr_name'];
                    $attr_list_srt .= substr($v['attr_group_name'], 0, strpos($v['attr_group_name'], '_')) . '-' . substr($v['attr_name'], 0, strpos($v['attr_name'], '_'));
                }
                if(!$value['kucun'])
                {
                    $value['kucun'] = '9999999';
                }
                // if((int)$min_inventory > (int)$value['kucun'])
                // {
                //     $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 规格库存低于预警值，请先修改库存！';
                //     $this->proLog($type0,$Log_content);
                //     $message = Lang('product.30');
                //     echo json_encode(array('code' => 109,  'message' => $message));
                //     exit();
                // }
                $z_num += $value['kucun'];
                $value['total_num'] = $value['kucun'];
                
                if(isset($value['img']))
                {
                    if ($value['img'] == '')
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的属性图片未上传';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.31');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的属性图片未上传';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.31');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                $attributes[$key]['img'] = preg_replace('/.*\//', '', $value['img']);
                $attributes[$key]['unit'] = $unit;
                
                //价格判断
                foreach ($value as $cvkey => $cvvalue)
                {
                    if (!is_array($cvvalue))
                    {
                        if($cvkey != 'bar_code' && $cvkey != 'cid')
                        {
                            if (empty($cvvalue) && $cvvalue != 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 请完善属性';
                                $this->proLog($type0,$Log_content);
                                $message = Lang('product.32');
                                echo json_encode(array('code' => 109,  'message' => $message));
                                exit();
                            }
                        }
                    }
                }

                if(!is_numeric($value['cbj']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的成本价不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.33');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                if(!is_numeric($value['yj']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的原价不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.34');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                if(!is_numeric($value['sj']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的售价不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.35');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                if(!is_numeric($value['kucun']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的库存不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.36');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                $costprice = $value['cbj'];
                $price = $value['sj'];
                if ($costprice > $price)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '上传商品时，成本价不能大于售价！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.37');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }

                $value['attribute'] = serialize($attr_list_arr);
                $value = Tools::array_key_remove($value, 'attr_list');
                $attributes[$key]['costprice'] = $value['cbj'];
                $attributes[$key]['yprice'] = $value['yj'];
                $attributes[$key]['price'] = $value['sj'];
                $attributes[$key]['num'] = $value['kucun'];
                $attributes[$key]['total_num'] = $value['total_num'];
                $attributes[$key]['min_inventory'] = $min_inventory;
                $attributes[$key]['attribute'] = $value['attribute'];
                $attributes[$key]['bar_code'] = isset($value['bar_code'])?$value['bar_code']:'';
            }
        }
        
        if ($freight == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择运费模板名称';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.38');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }

        $sort_r = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $sort_r[0]['sort'] + 1;

        $s_type = ',' . $s_type . ',';
        $show_adr = ',0,';
        $is_distribution = 0;
// var_dump(['store_id'=>$store_id,'id'=>$mch_id]);die;
        $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name')->select()->toArray();
        
        $shop_name = $r_mch[0]['name'];

        if($sell_type == 1)
        {
            // 预售商品验证
            if(!is_numeric($deposit) || $deposit <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 定金设置不正确！'.$deposit;
                $this->proLog($type0,$Log_content);
                $message = Lang('product.3');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
            if($attr)
            {
                foreach ($attr as $val)
                {
                    if($val['sj'] <= $deposit)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 定金不能大于售价！'.$deposit;
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.4');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
            }
            
            if($pay_type == 1)
            {
                $deposit_start_time = $time;
                $deposit_end_time = $balance_pay_time;
            }
            else
            {
                if(strtotime($deposit_start_time) >= strtotime($deposit_end_time) || $deposit_start_time == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 定金支付时间设置不正确！'.$deposit_start_time.'-'.$deposit_end_time;
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.6');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
            }
            
            if( strtotime($balance_pay_time) < strtotime(date('Y-m-d H:i:s')) || $balance_pay_time == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 尾款支付时间设置不正确！'.$balance_pay_time;
                $this->proLog($type0,$Log_content);
                $message = Lang('product.5');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
        }
        else
        {
            if(!is_numeric($sellNum) || $sellNum <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 预售数量不正确！'.$sellNum;
                $this->proLog($type0,$Log_content);
                $message = Lang('product.3');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
            
            if(!is_numeric($endDay) || $endDay <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 预售截止时间不正确！'.$endDay;
                $this->proLog($type0,$Log_content);
                $message = Lang('product.3');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
        }

        if(!is_numeric($delivery_time) || $delivery_time < 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 发货时间不正确！'.$delivery_time;
            $this->proLog($type0,$Log_content);
            $message = Lang('product.7');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        $data_sql = array('store_id'=>$store_id,'product_number'=>'','commodity_type'=>$isVirtual,'product_title'=>$product_title,'subtitle'=>$subtitle,'keyword'=>$keyword,'scan'=>'','product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'imgurl'=>$image,'sort'=>$sort,'content'=>$content,'richList'=>$richList,'num'=>$z_num,'min_inventory'=>$min_inventory,'s_type'=>$s_type,'add_date'=>$time,'is_distribution'=>$is_distribution,'distributor_id'=>0,'freight'=>$freight,'active'=>$active,'mch_id'=>$mch_id,'mch_status'=>$mch_status,'show_adr'=>$show_adr,'initial'=>$initial,'publisher'=>$admin_name,'cover_map'=>$cover_map,'is_presell'=>$is_presale,'video'=>$video,'pro_video'=>$pro_video);
        $id1 = Db::name('product_list')->insertGetId($data_sql);
        if ($id1 >= 1)
        {
            if ($imgurls)
            {
                $arrimg = array();
                $rf = ProductImgModel::where(['product_id'=>$id1])->field('product_url')->select()->toArray();
                if ($rf)
                {
                    foreach ($rf as $key => $fs)
                    {
                        $key_rf = $fs['product_url'];
                        $arrimg[$key_rf] = $fs['product_url'];
                    }
                }
                if (count($imgurls) > 4)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 产品展示图数量超出限制';
                    $this->proLog($type0,$Log_content);
                    // 回滚事务
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator_record, '添加了预售商品失败。产品展示图数量超出限制',1,$source_record,$shop_id_record,$operator_id);
                    $message = Lang('product.42');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }

                foreach ($imgurls as $key => $file)
                {
                    $imgsURL_name = preg_replace('/.*\//', '', $file);
                    if (array_key_exists($imgsURL_name, $arrimg))
                    {
                        unset($arrimg[$imgsURL_name]);
                    }

                    $data_img = array('product_url'=>$imgsURL_name,'product_id'=>$id1,'add_date'=>$time);
                    $r = Db::name('product_img')->save($data_img);
                    if ($r < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 产品展示图上传失败！参数:'. json_encode($data_img);
                        $this->proLog($type0,$Log_content);
                        // 回滚事务
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator_record, '添加了预售商品失败。产品展示图上传失败',1,$source_record,$shop_id_record,$operator_id);
                        $message = Lang('product.43');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }
                }
                if (!empty($arrimg))
                {
                    foreach ($arrimg as $keys => $fss)
                    {
                        $r = Db::table('lkt_product_img')->where('product_url',$fss)->delete();
                    }
                }
            }

            foreach ($attributes as $ke => $va)
            {
                if($sell_type == 2)
                {
                    $va['total_num'] = $sellNum;
                    $va['num'] = $sellNum;
                }
                
                $data_attribute = array('pid'=>$id1,'costprice'=>$va['costprice'],'yprice'=>$va['yprice'],'price'=>$va['price'],'num'=>$va['num'],'total_num'=>$va['total_num'],'min_inventory'=>$min_inventory,'attribute'=>$va['attribute'],'img'=>$va['img'],'unit'=>$va['unit'],'ctime'=>$time,'bar_code'=>$va['bar_code']);
                $r_attribute = Db::name('configure')->insertGetId($data_attribute);
                if ($r_attribute < 1)
                {
                    $attributes1 = json_encode($va);
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 属性数据添加失败！属性数据:'.$attributes1;
                    $this->proLog($type0,$Log_content);
                    // 回滚事务
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator_record, '添加了预售商品失败。属性数据添加失败',1,$source_record,$shop_id_record,$operator_id);
                    $message = Lang('product.44');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
                $num = $va['num'];
                // 在库存记录表里，添加一条入库信息
                $content = $admin_name . '增加商品总库存' . $num;
                $data_stock0 = array('store_id'=>$store_id,'product_id'=>$id1,'attribute_id'=>$r_attribute,'total_num'=>$va['total_num'],'flowing_num'=>$va['num'],'type'=>0,'add_date'=>$time,'content'=>$content);
                $r_stock0 = Db::name('stock')->insertGetId($data_stock0);
                if($r_stock0 < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 库存记录添加失败！sql:'.$sql_stock0;
                    $this->proLog($type0,$Log_content);
                    // 回滚事务
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator_record, '添加了预售商品失败。库存记录添加失败',1,$source_record,$shop_id_record,$operator_id);
                    $message = Lang('product.45');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit;
                }
            }

            if($sell_type == 1)
            {
                $sql_presale_sql = array('product_id'=>$id1,'product_title'=>$product_title,'sell_type'=>$sell_type,'deposit'=>$deposit,'pay_type'=>$pay_type,'deposit_start_time'=>$deposit_start_time,'deposit_end_time'=>$deposit_end_time,'balance_pay_time'=>$balance_pay_time,'delivery_time'=>$delivery_time);
            }
            else
            {
                $sql_presale_sql = array('product_id'=>$id1,'product_title'=>$product_title,'sell_type'=>$sell_type,'deposit'=>'','pay_type'=>$pay_type,'sell_num'=>$sellNum,'surplus_num'=>$sellNum,'end_day'=>$endDay,'delivery_time'=>$delivery_time);
            }
            $r_presale = Db::name('pre_sell_goods')->insert($sql_presale_sql);
            if($r_presale < 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 添加预售商品失败！sql:'.$sql;
                $this->proLog($type0,$Log_content);
                // 回滚事务
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator_record, '添加了预售商品失败。',1,$source_record,$shop_id_record,$operator_id);
                $message = Lang('product.47');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }

            $array = array('store_id'=>$store_id,'type0'=>2,'id'=>$id1,'name'=>$product_title);
            PC_Tools::jump_path($array);

            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 添加商品' . $product_title . '成功';
            $this->proLog($type0,$Log_content);

            // 提交事务
            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator_record, '添加了预售商品ID：'.$id1,1,$source_record,$shop_id_record,$operator_id);
            $message = Lang('Success');
            echo json_encode(array('code' => 200,  'message' => $message));
            exit;
        }
        else
        {   
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 添加商品失败！sql:'.$sql;
            $this->proLog($type0,$Log_content);
            // 回滚事务
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator_record, '添加了预售商品失败',1,$source_record,$shop_id_record,$operator_id);
            $message = Lang('product.47');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }
    }

    // 编辑页面（预售）
    public function edit_presale_page($store_id,$admin_name,$mch_id,$presale_id,$type0)
    {
        $sellGoodInfo = array();
        $r_presale = PreSellGoodsModel::where(['product_id'=> $presale_id])->select()->toArray();
        if($r_presale)
        {
            $id = $r_presale[0]['product_id'];
            if($r_presale[0]['sell_type'] == 1)
            {
                if($r_presale[0]['pay_type'] == 1)
                {
                    $sellGoodInfo = array('sell_type'=>$r_presale[0]['sell_type'],'deposit'=>round($r_presale[0]['deposit'],2),'payType'=>$r_presale[0]['pay_type'],'balancePayTime'=>$r_presale[0]['balance_pay_time'],'deliveryTime'=>$r_presale[0]['delivery_time'],'endDay'=>$r_presale[0]['end_day']);
                }
                else
                {
                    $sellGoodInfo = array('sell_type'=>$r_presale[0]['sell_type'],'deposit'=>round($r_presale[0]['deposit'],2),'payType'=>$r_presale[0]['pay_type'],'depositStartTime'=>$r_presale[0]['deposit_start_time'],'depositEndTime'=>$r_presale[0]['deposit_end_time'],'balancePayTime'=>$r_presale[0]['balance_pay_time'],'deliveryTime'=>$r_presale[0]['delivery_time'],'endDay'=>$r_presale[0]['end_day']);
                }
            }
            else
            {
                $sellGoodInfo = array('sell_type'=>$r_presale[0]['sell_type'],'payType'=>$r_presale[0]['pay_type'],'sellNum'=>$r_presale[0]['sell_num'],'surplusNum'=>$r_presale[0]['surplus_num'],'deliveryTime'=>$r_presale[0]['delivery_time'],'endDay'=>$r_presale[0]['end_day']);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品预售ID错误!' . $presale_id;
            $this->proLog($type0,$Log_content);
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1])->field('shop_id')->select()->toArray();
        $message_logging_list_8 = array('store_id'=>$store_id,'mch_id'=>$r_admin[0]['shop_id'],'parameter'=>$id,'type'=>8);
        PC_Tools::message_pop_up($message_logging_list_8);
        PC_Tools::message_read($message_logging_list_8);

        $message_logging_list_10 = array('store_id'=>$store_id,'mch_id'=>$r_admin[0]['shop_id'],'parameter'=>$id,'type'=>10);
        PC_Tools::message_pop_up($message_logging_list_10);
        PC_Tools::message_read($message_logging_list_10);
        // 根据产品id，查询产品产品信息
        $r = ProductListModel::where(['store_id'=>$store_id,'id'=>$id])->select()->toArray();
        if ($r)
        {
            $product_title = $r[0]['product_title']; // 产品标题
            $subtitle = $r[0]['subtitle']; // 副标题
            $product_class = $r[0]['product_class']; // 产品类别
            $brand_id = $r[0]['brand_id']; // 产品品牌
            $keyword = $r[0]['keyword']; // 关键词
            $weight = $r[0]['weight']; // 关键词
            $status = $r[0]['status']; // 上下架状态
            $min_inventory = $r[0]['min_inventory']; // 库存预警
            $content = $r[0]['content']; // 产品内容
            $imgurl = ServerPath::getimgpath($r[0]['imgurl'], $store_id); //图片
            $cover_map = ServerPath::getimgpath($r[0]['cover_map'], $store_id); //图片
            $richList = $r[0]['richList'];

            $initial = $r[0]['initial'];
            $s_type = trim($r[0]['s_type'],',');
            $show_adr = $r[0]['show_adr'];
            $freight_id = $r[0]['freight'];
            $shop_id = $r[0]['mch_id'];

            $active = str_replace(",",'',$r[0]['active']);

            $video = $r[0]['video']; // 展示视频
            $pro_video = $r[0]['pro_video']; // 商品视频
        }
        else
        {
            $message = Lang('Illegal invasion');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }

        $freight_list = array();
        $r_freight = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field('id,name')->order('id', 'asc')->select()->toArray();
        if ($r_freight)
        {
            foreach ($r_freight as $k => $v)
            {
                if ($freight_id == $v['id'])
                {
                    $v['selected'] = true;
                }
                else
                {
                    $v['selected'] = false;
                }
                $freight_list[] = $v;
            }
        }

        $imgurls = array();
        $imgurls[] = $imgurl;
        $r_imgurls = ProductImgModel::where(['product_id'=>$id])->select()->toArray();
        if ($r_imgurls)
        {
            foreach ($r_imgurls as $k => $v)
            {
                $imgurls[] = ServerPath::getimgpath($v['product_url'], $store_id);
            }
        }

        $brand_name = '';
        $brand_class_list1 = array();
        $freight_list1 = array();
        $class_id1 = '';
        $class = array();
        // 商品分类
        $res = explode('-', trim($product_class, '-'));
        $class_id0 = $res[0]; //  商品所属分类的顶级
        $shuliang = count($res) - 1;
        $class_id1 = $res[$shuliang]; // 商品所属分类
        foreach ($res as $k => $v)
        {
            $r_class = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$v,'recycle'=>0,'examine'=>1])->select()->toArray();
            if (count($r_class) > 0)
            {
                $class[] = $r_class[0];
            }
        }

        $r_brand = BrandClassModel::where(['store_id'=>$store_id,'brand_id'=>$brand_id])->select()->toArray();
        if($r_brand)
        {
            $brand_class_list1 = $r_brand[0];
            $brand_name = $r_brand[0]['brand_name'];
        }
        // 产品品牌
        $brand_list = BrandClassModel::where(['store_id'=>$store_id,'status'=>0,'recycle'=>0,'examine'=>1])->whereLike('categories','%,' . $class_id0 . ',%')->order('sort','desc')->field('brand_id,brand_name')->select()->toArray();

        $attr_group_list = array();
        $checked_attr_list = array();
        $strArr = array();
        //-----查询规格数据
        $res_size = ConfigureModel::where(['pid'=>$id,'recycle'=>0])->select()->toArray();
        if ($res_size)
        {
            if ($res_size[0]['attribute'] != '')
            {
                $arrar_t = unserialize($res_size[0]['attribute']);
                foreach ($arrar_t as $key => $value)
                {
                    if (strpos($key, '_LKT_') !== false)
                    {
                        $key = substr($key, 0, strrpos($key, "_LKT"));
                    }
                    $attr_group_list[] = array('attr_group_name' => $key, 'attr_list' => [], 'attr_all' => []);
                }

                foreach ($res_size as $k => $v)
                {
                    $attribute = unserialize($v['attribute']); // 属性
                    $attr_lists = array();
                    //列出属性名
                    foreach ($attribute as $key => $value)
                    {
                        if (strpos($key, '_LKT_') !== false)
                        {
                            $key0 = trim(strrchr($key, '_'),'_');
                            $value0 = trim(strrchr($value, '_'),'_');
                            $strArr[] = array('id0'=>$key0,'id1'=>$value0);
                            $key = substr($key, 0, strrpos($key, "_LKT"));
                            $value = substr($value, 0, strrpos($value, "_LKT"));
                        }
                        foreach ($attr_group_list as $keya => $valuea)
                        {
                            if ($key == $valuea['attr_group_name'])
                            {
                                if (!in_array($value, $attr_group_list[$keya]['attr_all']))
                                {
                                    if ($status == 1)
                                    {
                                        $attr_list = array('attr_name' => $value, 'status' => true);
                                    }
                                    else
                                    {
                                        $attr_list = array('attr_name' => $value, 'status' => false);
                                    }
                                    array_push($attr_group_list[$keya]['attr_list'], $attr_list);
                                    array_push($attr_group_list[$keya]['attr_all'], $value);
                                }
                            }
                        }
                        $attr_lists[] = array('attr_id' => '', 'attr_group_name' => $key, 'attr_name' => $value);
                    }
                    $checked_attr_list[] = array('attr_list' => $attr_lists, 'cbj' => $v['costprice'], 'yj' => $v['yprice'], 'sj' => $v['price'], 'kucun' => $v['num'], 'unit' => $v['unit'], 'bar_code' => $v['bar_code'], 'img' => ServerPath::getimgpath($v['img'], $store_id), 'cid' => $v['id']);
                }

                foreach ($attr_group_list as $key => $value)
                {
                    $attr_group_list[$key] = Tools::array_key_remove($attr_group_list[$key], 'attr_all');
                }
            }
        }
        $strArr = Tools::assoc_unique($strArr,'id1');
        $arr = explode(',', $s_type);
        $sp_type = array();
        if ($show_adr)
        {
            $show_adr0 = explode(',', $show_adr);
        }
        else
        {
            $show_adr0 = array(0);
        }
        $show_adr = array();

        $r_sp_type = ProLabelModel::where(['store_id'=>$store_id])->order('add_time','desc')->field('id,name')->select()->toArray();
        if ($r_sp_type)
        {
            foreach ($r_sp_type as $k => $v)
            {
                if (in_array($v['id'], $arr))
                {
                    $sp_type[] = array('name' => $v['name'], 'value' =>$v['id'], 'status' => true);
                }
                else
                {
                    $sp_type[] = array('name' => $v['name'], 'value' =>$v['id'], 'status' => false);
                }
            }
        }

        $sql_show_adr = "select a.value,a.text from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.recycle = 0 and b.name = '商品展示位置' and a.status = 1";
        $r_show_adr = Db::query($sql_show_adr);
        if ($r_show_adr)
        {
            foreach ($r_show_adr as $k => $v)
            {
                if (in_array($v['value'], $show_adr0))
                {
                    $show_adr[] = array('name' => $v['text'], 'value' =>$v['value'], 'status' => true);
                }
                else
                {
                    $show_adr[] = array('name' => $v['text'], 'value' =>$v['value'], 'status' => false);
                }
            }
        }

        if ($initial != '')
        {
            $initial = unserialize($initial);
            if(!isset($initial['stockWarn']))
            {
                $initial['stockWarn'] = $min_inventory;
            }
        }
        else
        {
            $initial = array();
        }

        $unit = array();
        $sql_unit = "select a.text from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.recycle = 0 and b.name = '单位' and a.status = 1";
        $r_unit = Db::query($sql_unit);
        if ($r_unit)
        {
            foreach ($r_unit as $k => $v)
            {
                $unit[] = $v['text'];
            }
        }

        $data = array('Plugin_arr'=>array(),'active'=>$active,'attr_group_list'=>$attr_group_list,'brand_class'=>$brand_list,'brand_class_list1'=>$brand_class_list1,'brand_id'=>$brand_id,'brand_name'=>$brand_name,'checked_attr_list'=>$checked_attr_list,'class_id'=>$class_id1,'content'=>$content,'cover_map'=>$cover_map,'ctypes'=>$class,'distributors'=>array(),'distributors1'=>array(),'freight_list'=>$freight_list,'freight_list1'=>$freight_list1,'imgurls'=>$imgurls,'initial'=>$initial,'keyword'=>$keyword,'list'=>$r[0],'mch_id'=>$shop_id,'min_inventory'=>$min_inventory,'product_title'=>$product_title,'richList' => $richList,'sellGoodInfo'=>$sellGoodInfo,'show_adr'=>$show_adr,'sp_type'=>$sp_type,'status'=>$status,'strArr'=>$strArr,'subtitle'=>$subtitle,'unit'=>$unit,'weight'=>$weight,'video'=>$video,'pro_video'=>$pro_video);
        return json_encode($data);
    }

    // 编辑商品（预售）
    public function edit_presale_product($store_id,$admin_name,$mch_id,$array,$type0)
    {
        // 1.开启事务
        Db::startTrans();
        $Tools = new Tools($store_id, 1);
        $Jurisdiction = new Jurisdiction();
        $time = date("Y-m-d H:i:s");
        $product_id = $array['id'];

        $isVirtual = 0;
        $product_title = $array['product_title'];
        $subtitle = $array['subtitle'];
        $keyword = isset($array['keyword'])?$array['keyword']:'';
        $product_class = $array['product_class'];
        $brand_id = $array['brand_id'];
        $weight = $array['weight'];
        $weight_unit = 'kg';
        $imgurls = $array['imgurls'];
        $initial = $array['initial'];
        $unit = $array['unit'];
        $attr = $array['attr'];
        $min_inventory = $array['min_inventory'];
        $freight = $array['freight'];
        $s_type = $array['s_type'];
        $show_adr = $array['show_adr'];
        $active = $array['active'];
        $content = $array['content'];
        $richList = $array['richList'];
        $mch_status = $array['mch_status'];
        $cover_map = $array['cover_map'];
        $volume = '';
        $video = ''; // 展示视频
        if(isset($array['video']))
        {
            $video = $array['video']; // 展示视频
        }
        $pro_video = ''; // 商品视频
        if(isset($array['pro_video']))
        {
            $pro_video = $array['pro_video']; // 商品视频
        }
        $is_presale = 1; // 预售商品
        $sell_type = $array['sell_type'];
        $deposit = $array['deposit'];
        $pay_type = $array['pay_type'];
        $deposit_start_time = $array['deposit_start_time'];
        $deposit_end_time = $array['deposit_end_time'];
        $time = date("Y-m-d H:i:s");
        $balance_pay_time = $array['balance_pay_time'];
        $sellNum = $array['sellNum'];
        $endDay = $array['endDay'];
        $delivery_time = $array['delivery_time'];
        $status = $array['status'];
       
        if(isset($array['volume']))
        {
            $volume = $array['volume'];
        }
        $type = $array['type'];
        
        $shop_id_record = $array['shop_id_record'];
        $operator_record = $array['operator_record'];
        $source_record = $array['source_record'];
        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }

        $r_presale = PreSellGoodsModel::where(['product_id'=> $product_id])->field('product_id')->select()->toArray();
        if($r_presale)
        {
            $id = $r_presale[0]['product_id'];
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品预售ID错误!' . $product_id;
            $this->proLog($type0,$Log_content);
            $message = Lang('Parameter error');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        if (empty($product_title))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品标题不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.8');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }
        // else
        // {
        //     if(strlen($product_title) > 20)
        //     {
        //         $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品标题不能大于20个字符';
        //         $this->proLog($type0,$Log_content);
        //         $message = Lang('product.101');
        //         echo json_encode(array('code' => 109,  'message' => $message));
        //         exit();
        //     }
        // }

        if (empty($product_class))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品类别不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.11');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }
        else
        {
            $product_class = $Tools->str_option($product_class);
        }

        if (empty($brand_id))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 品牌不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.12');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        if (is_numeric($weight))
        {
            if ($weight < 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 重量不能为负数';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.13');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 重量请填写数字';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.14');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        // 检查键名 "center" 是否存在于数组中：
        if (array_key_exists('center', $imgurls))
        {
            $image = preg_replace('/.*\//', '', $imgurls['center']);
            unset($imgurls['center']);
        }
        else
        {
            if (!empty($imgurls))
            {
                $image = preg_replace('/.*\//', '', $imgurls['0']);
                if ($image == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品展示图不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.16');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                unset($imgurls[0]);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品展示图不能为空';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.16');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
        }
        
        //五张轮播图
        if ($imgurls)
        {
            $arrimg = array();
            $rf = ProductImgModel::where(['product_id'=>$id])->field('id,product_url')->select()->toArray();
            if ($rf)
            {
                foreach ($rf as $key => $fs)
                {
                    $key1 = $fs['id'];
                    $arrimg[$key1] = $fs['product_url'];
                }
            }

            if (count($imgurls) > 4)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品展示图数量超出限制';
                $this->proLog($type0,$Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator_record, '修改了预售商品ID：'.$id.'失败。产品展示图数量超出限制',2,$source_record,$shop_id_record,$operator_id);
                $message = Lang('product.42');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }

            foreach ($imgurls as $key => $file)
            {
                $imgsURL_name = preg_replace('/.*\//', '', $file);
                $key1 = array_search($imgsURL_name, $arrimg);
                if ($key1)
                {
                    unset($arrimg[$key1]);
                }
                else
                {
                    $sql_img = ['product_url' => $imgsURL_name, 'product_id' => $id,'add_date'=>$time];
                    $r_img = Db::name('product_img')->insert($sql_img);
                    if ($r_img < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ .' 产品展示图上传失败！参数:' . json_encode($sql_img);
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator_record, '修改了预售商品ID：'.$id.'失败。产品展示图上传失败',2,$source_record,$shop_id_record,$operator_id);
                        $message = Lang('product.43');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }
                }
            }

            if (!empty($arrimg))
            {
                foreach ($arrimg as $keys => $fss)
                {
                    Db::table('lkt_product_img')->where('id',$keys)->delete();
                }
            }
        }
        else
        {
            Db::table('lkt_product_img')->where('product_id',$id)->delete();
        }

        if(empty($cover_map))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品封面图不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.17');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }
        else
        {
            $cover_map = preg_replace('/.*\//', '', $cover_map);
            if ($cover_map == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 产品封面图不能为空';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.17');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }

        if ($initial)
        {
            $key = array();
            $val = array();
            $initial1 = explode(',', $initial); // 转数组

            foreach ($initial1 as $k => $v)
            {
                if($v != '')
                {
                    $initial2 = explode('=', $v); // 转数组
                    if($initial2[0] == 'stockWarn')
                    {
                        $initial2[0] == 'kucun';
                    }
                    $key[] = $initial2[0];
                    $val[] = $initial2[1];
                    if ($initial2[0] == 'kucun')
                    {
                        $kucun = $initial2[1];
                    }
                }
            }
            $initial = array_combine($key, $val); // 创建一个数组，用一个数组的值作为其键名，另一个数组的值作为其值

            foreach ($initial as $k => $v)
            {
                if ($k == 'cbj' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 成本价初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.18');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'yj' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 原价初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.19');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'sj')
                {
                    if($v == '')
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 售价初始值不能为空';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.20');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                    elseif($is_presale && $v <= $deposit)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 定金不能大于售价';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.21');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
                else if ($k == 'unit' && $v == '0')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 单位初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.22');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                else if ($k == 'kucun' && $v == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 库存初始值不能为空';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.23');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                if ($k == 'cbj' || $k == 'yj' || $k == 'sj' || $k == 'kucun' )
                {
                    if (is_numeric($v))
                    {
                        if ($v < 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值不能为负数';
                            $this->proLog($type0,$Log_content);
                            $message = Lang('product.24');
                            echo json_encode(array('code' => 109,  'message' => $message));
                            exit();
                        }
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值请填写数字';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.25');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
            }
            $initial = serialize($initial);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 初始值不能为空';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.26');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }

        $z_num = 0;
        $attributes = array();
        //处理属性
        if (count($attr ? $attr : array()) == 0 || empty($attr))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写属性';
            $this->proLog($type0,$Log_content);
            $message = Lang('product.29');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }
        else
        {
            $foreach_num = 0;
            foreach ($attr as $k => $v)
            {
                $foreach_num++;
                foreach ($v['attr_list'] as $ke => $va)
                {
                    $attr_group_name_id = Tools::attribute_id($store_id, 1, $va['attr_group_name'], 0);
                    if ($attr_group_name_id != 0)
                    { // 当属性名称ID不为0，SKU表里有数据
                        $attr_name_id = Tools::attribute_id($store_id, 2, $va['attr_name'], $attr_group_name_id);
                        if ($attr_name_id == 0)
                        { // 当属性值ID为0，SKU表里没有数据
                            $attr_name_id = $Tools->add_attribute($store_id, $attr_group_name_id, $va['attr_name'], 2, $admin_name, $foreach_num,$type0); // 添加属性值
                        }
                    }
                    else
                    { // 当属性名称ID为0，SKU表里没有数据
                        $attr_group_name_id = $Tools->add_attribute($store_id, 0, $va['attr_group_name'], 1, $admin_name, 0,$type0); // 添加属性名
                        $attr_name_id = $Tools->add_attribute($store_id, $attr_group_name_id, $va['attr_name'], 2, $admin_name, $foreach_num,$type0); // 添加属性值
                    }
                    $attr[$k]['attr_list'][$ke]['attr_group_name'] = $va['attr_group_name'] . '_LKT_' . $attr_group_name_id; // 拼接属性名
                    $attr[$k]['attr_list'][$ke]['attr_name'] = $va['attr_name'] . '_LKT_' . $attr_name_id; // 拼接属性值
                }
            }

            foreach ($attr as $key => $value)
            {
                $cid = $value['cid'];
                $attr_list = $value['attr_list'];
                $attr_list_arr = array();
                $attr_list_srt = '';
                $str0 = '';
                foreach ($attr_list as $k => $v)
                {
                    if (strpos($v['attr_group_name'], '_LKT_') !== false)
                    {
                        $str0 = substr($v['attr_group_name'], 0, strrpos($v['attr_group_name'], "_LKT")) .':'. substr($v['attr_name'], 0, strrpos($v['attr_name'], "_LKT"));
                    }
                    else
                    {
                        $str0 = $v['attr_group_name'] .':'. $v['attr_name'];
                    }
                    $attr_list_arr[$v['attr_group_name']] = $v['attr_name'];
                    $attr_list_srt .= substr($v['attr_group_name'], 0, strpos($v['attr_group_name'], '_')) . '-' . substr($v['attr_name'], 0, strpos($v['attr_name'], '_'));
                }
                // if((int)$min_inventory > (int)$value['kucun'])
                // {
                //     $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 规格库存低于预警值，请先修改库存！';
                //     $this->proLog($type0,$Log_content);
                //     $message = Lang('product.30');
                //     echo json_encode(array('code' => 109,  'message' => $message));
                //     exit();
                // }
                $z_num += $value['kucun'];
                $value['total_num'] = $value['kucun'];

                //价格判断
                foreach ($value as $cvkey => $cvvalue)
                {
                    if (!is_array($cvvalue))
                    {
                        if($cvkey != 'bar_code' && $cvkey != 'cid')
                        {
                            if (empty($cvvalue) && $cvvalue != 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $admin_name . '编辑商品时，请完善属性！';
                                $this->proLog($type0,$Log_content);
                                $message = Lang('product.32');
                                echo json_encode(array('code' => 109, 'message' => $message));
                                exit;
                            }
                        }
                    }
                }

                if(!is_numeric($value['cbj']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的成本价不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.33');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                if(!is_numeric($value['yj']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的原价不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.34');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                if(!is_numeric($value['sj']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的售价不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.35');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                if(!is_numeric($value['kucun']))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的库存不为数字！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.36');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                $costprice = $value['cbj'];
                $price = $value['sj'];
                if ($costprice > $price)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '上传商品时，成本价不能大于售价！';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.37');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }

                if(isset($value['img']))
                {
                    if ($value['img'] == '')
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的属性图片未上传';
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.31');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $attr_list_srt . ' 的属性图片未上传';
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.31');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
                $attributes[$key]['img'] = preg_replace('/.*\//', '', $value['img']);
                $attributes[$key]['unit'] = $unit;

                if ($cid != 0 && $cid != '')
                {
                    $r0_0 = ConfigureModel::where(['id'=>$cid,'recycle'=>0])->field('total_num,num')->select()->toArray();
                    if($r0_0)
                    {
                        $total_num = $r0_0[0]['total_num']; // 总库存数
                        $num = $r0_0[0]['num']; // 剩余数量
                    }
                    else
                    {
                        $value['cid'] = 0;
                        $total_num = $value['kucun']; // 总库存数
                        $num = $value['kucun']; // 剩余数量
                    }
                    
                    if ($value['kucun'] == $num)
                    { // 当传过来的数量 等于 剩余数量，表示没有改变了数量
                        $attributes[$key]['num'] = $num;
                        $attributes[$key]['total_num'] = $total_num;
                    }
                    else
                    {
                        $total_num1 = $num - $value['kucun']; // 剩余数量 - 传过来的数量
                        if ($total_num1 > 0)
                        { // 大于0，表示减少总库存
                            $attributes[$key]['total_num'] = $total_num - $total_num1;
                            $attributes[$key]['num'] = $value['kucun'];
                        }
                        else
                        { // 小于0，表示增加总库存
                            $attributes[$key]['total_num'] = $total_num - $total_num1;
                            $attributes[$key]['num'] = $value['kucun'];
                        }
                    }
                }
                else
                {
                    $attributes[$key]['num'] = $value['kucun'];
                    $attributes[$key]['total_num'] = $value['kucun'];
                }

                $value['attribute'] = serialize($attr_list_arr);
                $value = Tools::array_key_remove($value, 'attr_list');

                $attributes[$key]['cid'] = $value['cid'];
                $attributes[$key]['costprice'] = $value['cbj'];
                $attributes[$key]['yprice'] = $value['yj'];;
                $attributes[$key]['price'] = $value['sj'];;
                $attributes[$key]['min_inventory'] = $min_inventory;
                $attributes[$key]['attribute'] = $value['attribute'];
                $attributes[$key]['bar_code'] = $value['bar_code'];
            }
        }
        
        //--处理属性
        if ($freight == 0)
        {
            $rr = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'is_default'=>1])->field('id')->select()->toArray();
            if ($rr)
            {
                $freight = $rr[0]['id'];
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择运费模板名称';
                $this->proLog($type0,$Log_content);
                $message = Lang('product.38');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
        }

        $s_type = ',' . $s_type . ',';
        $show_adr = ',0,';
        $is_distribution = 0;

        if($sell_type == 1)
        {
            // 预售商品验证
            if(!is_numeric($deposit) || $deposit <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 定金设置不正确！'.$deposit;
                $this->proLog($type0,$Log_content);
                $message = Lang('product.3');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
            if($attr)
            {
                foreach ($attr as $val)
                {
                    if($val['sj'] <= $deposit)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 定金不能大于售价！'.$deposit;
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.21');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
            }

            if($status <> 2)
            {
                if($pay_type == 1)
                {
                    $deposit_start_time = $time;
                    $deposit_end_time = $balance_pay_time;
                }
                else
                {
                    if(strtotime($deposit_start_time) >= strtotime($deposit_end_time) || $deposit_start_time == '')
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 定金支付时间设置不正确！'.$deposit_start_time.'-'.$deposit_end_time;
                        $this->proLog($type0,$Log_content);
                        $message = Lang('product.6');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit();
                    }
                }
    
                if( strtotime($balance_pay_time) < strtotime(date('Y-m-d H:i:s')) || $balance_pay_time == '')
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 尾款支付时间设置不正确！'.$balance_pay_time;
                    $this->proLog($type0,$Log_content);
                    $message = Lang('product.5');
                    echo json_encode(array('code' => 109,  'message' => $message));
                    exit();
                }
            }
            else
            {
                $deposit_start_time = $time;
                $deposit_end_time = $balance_pay_time;
            }
        }
        else
        {
            if(!is_numeric($sellNum) || $sellNum <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 预售数量不正确！'.$sellNum;
                $this->proLog($type0,$Log_content);
                $message = Lang('product.3');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
            
            if(!is_numeric($endDay) || $endDay <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 预售截止时间不正确！'.$endDay;
                $this->proLog($type0,$Log_content);
                $message = Lang('product.3');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit();
            }
        }

        if(!is_numeric($delivery_time) || $delivery_time < 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 发货时间不正确！'.$delivery_time;
            $this->proLog($type0,$Log_content);
            $message = Lang('product.7');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit();
        }

        $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name')->select()->toArray();
        $shop_name = $r_mch[0]['name'];
        // 根据产品id,修改产品信息
        $data_sql_1_update = array('product_title'=>$product_title,'subtitle'=>$subtitle,'keyword'=>$keyword,'scan'=>'','product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'s_type'=>$s_type,'num'=>$z_num,'min_inventory'=>$min_inventory,'content'=>$content,'richList'=>$richList,'imgurl'=>$image,'distributor_id'=>0,'is_distribution'=>$is_distribution,'freight'=>$freight,'show_adr'=>$show_adr,'active'=>$active,'initial'=>$initial,'mch_status'=>$mch_status,'cover_map'=>$cover_map,'is_presell'=>$is_presale,'video'=>$video,'pro_video'=>$pro_video);
        $data_sql_1_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
        $r_update = Db::name('product_list')->where($data_sql_1_where)->update($data_sql_1_update);
        if ($r_update == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改商品失败！条件参数：'.json_encode($data_sql_1_where) .'；修改参数：'.json_encode($data_sql_1_update);
            $this->proLog($type0,$Log_content);
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator_record, '修改了预售商品ID：'.$id.'失败',2,$source_record,$shop_id_record,$operator_id);
            $message = Lang('product.48');
            echo json_encode(array('code' => 109,  'message' => $message));
            exit;
        }
        else
        {
            $cids = array();
            if ($attributes)
            {
                $rcs = ConfigureModel::where(['pid'=>$id,'recycle'=>0])->field('id')->select()->toArray();
                if ($rcs)
                {
                    foreach ($rcs as $keyc => $valuec)
                    {
                        $keyc1 = $valuec['id'];
                        $cids[$keyc1] = $valuec['id'];
                    }
                }
            }

            foreach ($attributes as $ke => $va)
            {
                if($sell_type == 2)
                {
                    $va['total_num'] = $sellNum;
                    $va['num'] = $sellNum;
                }
                
                $num = $va['num'];
                $cid = $va['cid'];
                $total_num = $va['total_num'];
                $va['min_inventory'] = $min_inventory;
                $va['ctime'] = 'CURRENT_TIMESTAMP';
                $va = Tools::array_key_remove($va, 'cid');
                if ($cid)
                {
                    if (array_key_exists($cid, $cids))
                    {
                        unset($cids[$cid]);
                    }
                    // 查询剩余数量
                    $ccc = ConfigureModel::where(['id'=>$cid,'recycle'=>0])->field('num')->select()->toArray();
                    $cnums = $ccc ? $ccc[0]['num'] : 0;
                    $z_num1 = $num - $cnums; // 传过来的剩余数量 - 数据库里的剩余数量

                    $sql_1_0_where = array('id'=>$cid);
                    $sql_1_0_update = array('costprice'=>$va['costprice'],'yprice'=>$va['yprice'],'price'=>$va['price'],'num'=>$va['num'],'total_num'=>$va['total_num'],'min_inventory'=>$min_inventory,'attribute'=>$va['attribute'],'img'=>$va['img'],'unit'=>$va['unit'],'ctime'=>$time,'bar_code'=>$va['bar_code']);
                    $r_attribute = Db::name('configure')->where($sql_1_0_where)->update($sql_1_0_update);
                    $attribute_id = $cid;
                    if ($r_attribute < 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 属性数据修改失败！属性数据:'.json_encode($va);
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator_record, '修改了预售商品ID：'.$id.'失败，属性数据修改失败',2,$source_record,$shop_id_record,$operator_id);
                        $message = Lang('product.49');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }
                }
                else
                {
                    $va['pid'] = $id;

                    $data_attribute = array('pid'=>$id,'costprice'=>$va['costprice'],'yprice'=>$va['yprice'],'price'=>$va['price'],'num'=>$va['num'],'total_num'=>$va['total_num'],'min_inventory'=>$min_inventory,'attribute'=>$va['attribute'],'img'=>$va['img'],'unit'=>$va['unit'],'ctime'=>$time,'bar_code'=>$va['bar_code']);
                    $r_attribute = Db::name('configure')->insertGetId($data_attribute);
                    $z_num1 = $num;
                    $attribute_id = $r_attribute;
                    if ($r_attribute < 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 属性数据添加失败！属性数据:'.json_encode($va);
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator_record, '修改了预售商品ID：'.$id.'失败，属性数据添加失败',1,$source_record,$shop_id_record,$operator_id);
                        $message = Lang('product.44');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }
                }

                if ($z_num1 > 0)
                { // 增加
                    // 在库存记录表里，添加一条入库信息
                    $content = $admin_name . '增加商品总库存' . $num;
                    $data_stock0 = array('store_id'=>$store_id,'product_id'=>$id,'attribute_id'=>$attribute_id,'total_num'=>$total_num,'flowing_num'=>$z_num1,'type'=>0,'add_date'=>$time,'content'=>$content);
                    $r_stock0 = Db::name('stock')->insertGetId($data_stock0);
                    if($r_stock0 < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 库存记录添加失败！参数:'.json_encode($data_stock0);
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator_record, '修改了预售商品ID：'.$id.'失败，库存记录添加失败',1,$source_record,$shop_id_record,$operator_id);
                        $message = Lang('product.45');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }
                }
                else if ($z_num1 == 0)
                {

                }
                else
                { // 减少
                    $j_num = $cnums - $num; // 数据库里的剩余数量 - 传过来的剩余数量

                    $content2 = $admin_name . '减少商品总库存' . $j_num;
                    // 在库存记录表里，添加一条入库信息
                    $data_stock2 = array('store_id'=>$store_id,'product_id'=>$id,'attribute_id'=>$attribute_id,'total_num'=>$total_num,'flowing_num'=>$j_num,'type'=>1,'add_date'=>$time,'content'=>$content2);
                    $r_stock2 = Db::name('stock')->insertGetId($data_stock2);
                    if($r_stock2 < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 库存记录添加失败！参数:'.json_encode($data_stock2);
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator_record, '修改了预售商品ID：'.$id.'失败，库存记录添加失败',1,$source_record,$shop_id_record,$operator_id);
                        $message = Lang('product.45');
                        echo json_encode(array('code' => 109,  'message' => $message));
                        exit;
                    }
                }
            }

            //删除属性
            if (!empty($cids))
            {
                foreach ($cids as $keyds => $valueds)
                {
                    Db::name('configure')->where('id',$valueds)->update(['recycle'=>1]);
                }
            }

            if($sell_type == 1)
            {
                $sql_presale_sql_update = array('product_title'=>$product_title,'sell_type'=>$sell_type,'deposit'=>$deposit,'pay_type'=>$pay_type,'deposit_start_time'=>$deposit_start_time,'deposit_end_time'=>$deposit_end_time,'balance_pay_time'=>$balance_pay_time,'delivery_time'=>$delivery_time);
            }
            else
            {
                $sql_presale_sql_update = array('product_title'=>$product_title,'sell_type'=>$sell_type,'deposit'=>'','pay_type'=>$pay_type,'sell_num'=>$sellNum,'surplus_num'=>$sellNum,'end_day'=>$endDay,'delivery_time'=>$delivery_time);
            }
            
            $sql_presale_sql_where = array('product_id'=>$product_id);

            $r_presale = Db::name('pre_sell_goods')->where($sql_presale_sql_where)->update($sql_presale_sql_update);
            if($r_presale < 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改预售商品失败！条件参数：'.json_encode($sql_presale_sql_where) .'；修改参数：'.json_encode($sql_presale_sql_update);
                $this->proLog($type0,$Log_content);
                // 回滚事务
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator_record, '修改了预售商品ID：'.$id.'失败',2,$source_record,$shop_id_record,$operator_id);
                $message = Lang('product.47');
                echo json_encode(array('code' => 109,  'message' => $message));
                exit;
            }
            
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改商品ID为' . $id . '成功';
            $this->proLog($type0,$Log_content);

            $array = array('store_id'=>$store_id,'type0'=>2,'id'=>$id,'name'=>$product_title);
            PC_Tools::modify_jump_path($array);

            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator_record, '修改了预售商品ID：'.$id,2,$source_record,$shop_id_record,$operator_id);
            $message = Lang('Success');
            echo json_encode(array('code' => 200,  'message' => $message));
            exit;
        }
    }

    // 上下架（预售）
    public function upper_and_lower_shelves_presale($array,$type0)
    {
        $Jurisdiction = new Jurisdiction();

        $store_id = $array['store_id'];
        $admin_name = $array['admin_name'];
        $product_id = $array['p_id']; // 商品ID
        $status = $array['status']; // 0.下架 1.上架
        $type = $array['type'];
        
        $shop_id_record = $array['shop_id_record'];
        $operator_record = $array['operator_record'];
        $source_record = $array['source_record'];
        $operator_id = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }

        $time = date("Y-m-d H:i:s");
        $id_list = array();
        
        $Tools = new Tools($store_id, 1);

        $id = explode(',',$product_id);
        foreach ($id as $k => $v)
        {
            $r = ProductListModel::where(['id'=>$v])->field('product_title,status,num,mch_id,min_inventory')->select()->toArray();
            if($r)
            {
                $mch_id = $r[0]['mch_id'];
                $product_title = $r[0]['product_title'];
                $min_inventory = $r[0]['min_inventory'];
                if(strlen($product_title) > 10)
                {
                    $product_title = substr($product_title,0,9) . '...';
                }

                $r_mch = MchModel::where(['id'=>$mch_id])->field('user_id,name')->select()->toArray();
                
                if($r[0]['status'] == 1 || $r[0]['status'] == 3)
                { 
                    // 商品状态为待上架或已下架
                    $info = $Tools->commodity_status($store_id, $v,'status');
                    $res_0 = Lang('product.51');
                    if($info == '请先去完善商品信息！')
                    {
                        $message = Lang('product.52');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                    if ($r[0]['num'] < 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 商品ID为 ' . $v . ' 操作失败';
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $message = Lang('product.53');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                    $rp0 = PreSellGoodsModel::where(['product_id'=>$v])->field('*')->select()->toArray();
                    $sell_type = 1;
                    $endDay = 0;
                    if ($rp0) 
                    {
                        $sell_type = $rp0[0]['sell_type'];
                        //定金
                        if($sell_type == 1)
                        {
                            if($time > $rp0[0]['balance_pay_time']) 
                            {
                                $message = Lang('product.98');
                                echo json_encode(array('code' => 109, 'message' => $message));
                                exit;
                            }
                        }
                        //订货
                        if($sell_type == 2)
                        {
                            $endDay = $rp0[0]['end_day'];
                            if(isset($r[0]['upper_shelf_time']))
                            {
                                $deadline = date('Y-m-d H:i:s', strtotime($r[0]['upper_shelf_time'] . ' +'.$endDay.' day'));
                                if($time > $deadline) 
                                {
                                    $message = Lang('product.98');
                                    echo json_encode(array('code' => 109, 'message' => $message));
                                    exit;
                                }
                            }
                        }
                    }

                    // 根据商品ID，修改商品数据
                    $rr = Db::name('product_list')->where(['id' => $v])->update(['status'=>2,'upper_shelf_time'=>$time]);
                    if ($rr >= 0)
                    {
                        if($sell_type == 2)
                        {
                            $deadline = date('Y-m-d H:i:s', strtotime($time . ' +'.$endDay.' day'));
                            $rr = Db::name('pre_sell_goods')->where(['product_id' => $v])->update(['deadline'=>$deadline]);
                        }
                        
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 上架商品ID为 ' . $v . ' 上架成功';
                        $this->proLog($type0,$Log_content);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 上架商品ID为 ' . $v . ' 上架失败';
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator_record, '上架了预售商品ID：'.$v.'失败',2,$source_record,$shop_id_record,$operator_id);
                        $message = Lang('product.55');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                    // 根据商品ID，修改预售商品数据
                    $rrr = Db::name('pre_sell_goods')->where(['product_id' => $v])->update(['is_on_shelf'=>1]);
                    
                    $Jurisdiction->admin_record($store_id, $operator_record, '上架了预售商品ID：'.$v,2,$source_record,$shop_id_record,$operator_id); 
                }
                else
                {
                    $res_0 = Lang('product.57');
                    $res = Tools::del_banner($store_id,$v,'productId');

                    $rr = Db::name('product_list')->where(['id' => $v])->update(['status' => 3]);
                    if ($rr >= 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 下架商品ID为 ' . $v . ' 下架成功';
                        $this->proLog($type0,$Log_content);
                        $Jurisdiction->admin_record($store_id, $operator_record, '下架了预售商品ID：'.$v,2,$source_record,$shop_id_record,$operator_id);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 下架商品ID为 ' . $v . ' 下架失败';
                        $this->proLog($type0,$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator_record, '下架了预售商品ID：'.$v.'失败',2,$source_record,$shop_id_record,$operator_id);
                        $message = Lang('product.50');
                        echo json_encode(array('code' => 109, 'message' => $message));
                        exit;
                    }
                }
            }
            else
            {
                Db::rollback();
                $message = Lang('Parameter error');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }

        Db::commit();
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 删除
    public function del_presale($store_id,$admin_name,$product_id,$type0,$array = array())
    {
        $Jurisdiction = new Jurisdiction();
        $Tools = new Tools($store_id, 1);

        $shop_id_record = 0;
        $operator_id = 0;
        $operator_record = '';
        $source_record = 0;
        if($array != array())
        {
            $shop_id_record = $array['shop_id_record'];
            $operator_record = $array['operator_record'];
            $source_record = $array['source_record'];
            $operator_id = '';
            if(isset($array['operator_id']))
            {
                $operator_id = $array['operator_id'];
            }
        }
        $id_list = explode(',',$product_id);

        if($id_list != array())
        {
            foreach ($id_list as $k => $v)
            {
                Tools::del_banner($store_id, $v, 'productId');

                $Tools->commodity_status($store_id, $v,'recycle');

                // 删除购物车里的数据
                Db::table('lkt_cart')->where('Goods_id',$v)->delete();
                // 删除我的足迹数据
                Db::table('lkt_user_footprint')->where('p_id',$v)->delete();
                // 删除我的收藏里的数据
                Db::table('lkt_user_collection')->where('p_id',$v)->delete();
                // 根据产品id，删除产品信息
                Db::name('product_list')->where('id', $v)->update(['recycle' => '1','status' => '3']);
                // 根据产品id，删除产品属性信息
                Db::name('configure')->where('pid', $v)->update(['recycle' => '1']);
        
                Db::table('lkt_product_img')->where('product_id', $v)->delete();
        
                Db::table('lkt_jump_path')->where('type0',2)->whereIn('parameter','%='.$v)->delete();
                
                // 根据产品id，删除预售商品
                Db::name('pre_sell_goods')->where('product_id', $v)->update(['is_delete' => '1']);

                $Jurisdiction->admin_record($store_id, $operator_record, '删除了预售商品ID：'.$v,3,$source_record,$shop_id_record,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 删除商品ID为 ' . $v . ' 的信息';
                $this->proLog($type0,$Log_content);
            }
        }

        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 店铺日志
    public function proLog($type0,$Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        if($type0 == '平台')
        {
            $lktlog->log("admin/goods.log",$Log_content);
        }
        else if($type0 == 'PC店铺')
        {
            $lktlog->log("PC_mch/goods.log",$Log_content);
        }
        else if($type0 == '店铺')
        {
            $lktlog->log("app/mch.log",$Log_content);
        }
        return;
    }

    //商品信息同步
    public function goodsSynchronization($store_id,$data,$id)
    {
        $r_update = Db::name('product_list')->where(array('store_id'=>$store_id,'supplier_superior'=>$id))->update($data);
    }

    //规格信息编辑同步
    public function goodsInfoSynchronization($store_id,$data,$cid)
    {
        $r_attribute = Db::name('configure')->where(array('supplier_superior'=>$cid))->update($data);
    }

    //规格信息新增同步
    public function goodsInfoInsert($store_id,$data,$cid,$pid)
    {
        //获取需要同步的商品id 
        $res1 = ProductListModel::where(['store_id'=>$store_id,'supplier_superior'=>$pid,'recycle'=>0])->field('id')->select()->toArray();
        if($res1)
        {
            foreach ($res1 as $key => $value) 
            {
                $pid = $value['id'];
                $data['pid']=$pid;
                $r_attribute = Db::name('configure')->insertGetId($data);
            }
        }
    }

    // 属性处理
    public static function AttributeProcessing($array)
    {
        $store_id = $array['store_id'];
        $attribute = $array['attribute']; // 属性

        $name = "";
        $specifications = '';
        if($attribute != '')
        {
            $attribute = unserialize($attribute);
            if (count($attribute) > 0)
            {
                foreach($attribute as $ke => $va)
                {
                    if(strpos($ke, '_LKT_') !== false)
                    {
                        $ke = substr($ke, 0, strrpos($ke, "_LKT"));
                        $va = substr($va, 0, strrpos($va, "_LKT"));
                    }
                    $specifications .= $ke . ':' . $va . ';';
                }
            }
            $name = rtrim($specifications, ";");
        }

        return $name;
    }

    // 获取商品状态
    public static function GetProductStatus($array)
    {
        $status = $array['status']; // 状态

        $name = "";
        if($status == 2)
        {
            $name = '上架';
        }
        else if($status == 3)
        {
            $name = '下架';
        }
        else 
        {
            $name = '待上架';
        }

        return $name;
    }

    // 获取分类和品牌
    public static function ObtainCategoriesAndBrands($store_id)
    {
        $product_class_arr = array();
        //分类下拉选择
        $r_class = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0,'examine'=>1])->order('sort','desc')->field('cid,pname')->select()->toArray();
        if($r_class)
        {
            foreach ($r_class as $key => $value)
            {
                $c = '-' . $value['cid'] . '-';
                $product_class_arr[$c] = $value['pname'];
                //循环第一层
                $r_e = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$value['cid'],'examine'=>1])->order('sort','desc')->field('cid,pname')->select()->toArray();
                if ($r_e)
                {
                    foreach ($r_e as $ke => $ve)
                    {
                        $cone = $c . $ve['cid'] . '-';
                        $product_class_arr[$cone] = $ve['pname'];

                        //循环第二层
                        $r_t = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$ve['cid'],'examine'=>1])->order('sort','desc')->field('cid,pname')->select()->toArray();
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
        $r_brand_class = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0,'examine'=>1])->select()->toArray();
        if($r_brand_class)
        {
            foreach ($r_brand_class as $key => $value)
            {
                $key0 = $value['brand_id'];
                $brand_class_arr[$key0] = $value['brand_name'];
            }
        }

        $date = array('product_class_arr'=>$product_class_arr,'brand_class_arr'=>$brand_class_arr);
        return $date;
    }
}   
