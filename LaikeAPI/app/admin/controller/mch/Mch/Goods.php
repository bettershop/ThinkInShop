<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cache;
use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\Product;
use app\common\Plugin\Plugin;
use app\common\ExcelUtils;
use app\common\Jurisdiction;
use app\common\Plugin\MchPublicMethod;
use app\common\LKTConfigInfo;
use app\common\OSSCommon;
use app\common\MinIOServer;
use app\common\ProductClass;
use app\common\ProductBrand;
use app\common\PC_Tools;
use app\common\ProductInventory;

use app\admin\model\MchConfigModel;
use app\admin\model\MchModel;
use app\admin\model\ProLabelModel;
use app\admin\model\AdminModel;
use app\admin\model\ProductListModel;
use app\admin\model\ConfigureModel;
use app\admin\model\DsCountryModel;

/**
 * 功能：PC店鋪商品
 * 修改人：DHB
 */
class Goods extends BaseController
{
    // 获取店铺设置的商品上传方式
    public function GetCommoditySetup()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $r = MchConfigModel::where(['store_id'=>$store_id])->order('id','asc')->limit(1)->field('commodity_setup')->select()->toArray();
        if($r)
        {
            $commodity_setup = explode(',',$r[0]['commodity_setup']);
        }

        $data = array('commodity_setup'=>$commodity_setup);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 分类列表
    public function getClassInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        $language = addslashes(trim($this->request->param('language')));

        $type = addslashes(trim($this->request->param('type'))); // 类型 1=查询下级,2=查询上级,3=根据id查询 默认查询一级
        $classId = addslashes(trim($this->request->param('classId'))); // 分类ID
        $className = addslashes(trim($this->request->param('className'))); // 分类名称
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据

        if($lang_code == '')
        {
            $lang_code = $language;
        }
        $data = array('store_id'=>$store_id,'mch_id'=>0,'supplier_id'=>0,'type'=>$type,'classId'=>$classId,'className'=>$className,'lang_code'=>$lang_code,'page'=>$page,'pagesize'=>$pagesize,'source'=>2);
        $product_class = new ProductClass();
        $product_class_list = $product_class->get_product_class_list($data);

        return;
    }

    // 商品分类添加/编辑
    public function addClass()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $cid = addslashes(trim($this->request->param('id'))); // 分类ID
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语言
        $country_num = addslashes(trim($this->request->param('country_num'))); // 国家代码
        $pname = trim($this->request->param('className')); // 分类名称
        $english_name = trim($this->request->param('ename')); // 分类英文名称
        $level = addslashes(trim($this->request->param('level'))); // 级别
        $sid = addslashes(trim($this->request->param('fatherId'))); // 上级ID
        $img = addslashes(trim($this->request->param('img'))); // 图片

        $time = date("Y-m-d H:i:s");

        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        $source = 2;

        $user_id = $this->user_list['user_id'];

        $shop_id = PC_Tools::SelfOperatedStore($store_id);

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id
        if($shop_id == $mch_id)
        {
            $examine = 1; // 审核状态 0.待审核 1.审核通过 2.不通过
        }
        else
        {
            $examine = 0; // 审核状态 0.待审核 1.审核通过 2.不通过
        }

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'supplier_id'=>0,'cid'=>$cid,'lang_code'=>$lang_code,'country_num'=>$country_num,'pname'=>$pname,'english_name'=>$english_name,'level'=>$level,'sid'=>$sid,'img'=>$img,'examine'=>$examine,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>$source);
        $product_class = new ProductClass();
        $product_class_list = $product_class->addClass($data);

        return;
    }

    // 编辑商品分类页面（一级分类除外）
    public function getClassLevelTopAllInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $cid = addslashes(trim($this->request->param('classId'))); // 分类ID

        $data = array('store_id'=>$store_id,'cid'=>$cid,'source'=>2);
        $product_class = new ProductClass();
        $product_class_list = $product_class->getClassLevelTopAllInfo($data);

        return;
    }

    // 分类审核列表
    public function Class_auditList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $name = addslashes(trim($this->request->param('condition'))); // 分类名称/供应商名称
        $examine = addslashes(trim($this->request->param('status'))); // 审核状态 0.待审核 1.审核通过 2.不通过
        $startTime = addslashes(trim($this->request->param('startTime'))); // 查询开始时间
        $endTime = addslashes(trim($this->request->param('endTime'))); // 查询结束时间
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种

        $page = trim($this->request->param('pageNo'));//页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据
        $pagesize = $pagesize ? $pagesize : '10';

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'supplier_id'=>0,'name'=>$name,'level'=>'','examine'=>$examine,'startTime'=>$startTime,'endTime'=>$endTime,'lang_code'=>$lang_code,'page'=>$page,'pagesize'=>$pagesize,'source'=>2);
        $product_class = new ProductClass();
        $product_class_list = $product_class->auditList($data);

        return;
    }

    // 删除分类审核记录
    public function DelClass()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $cid = addslashes(trim($this->request->param('classId')));

        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        $source = 2;

        $user_id = $this->user_list['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'cid'=>$cid,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>$source);
        $product_class = new ProductClass();
        $product_class_list = $product_class->delClass($data);

        return;
    }

    // 品牌列表
    public function getBrandInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $brand_name = addslashes(trim($this->request->param('brandName'))); // 品牌名称
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据

        $data = array('store_id'=>$store_id,'mch_id'=>0,'supplier_id'=>0,'lang_code'=>$lang_code,'brand_name'=>$brand_name,'page'=>$page,'pagesize'=>$pagesize,'source'=>2);
        $product_brand = new ProductBrand();
        $product_brand_list = $product_brand->get_product_brand_list($data);

        return;
    }

    // 获取国家
    public function getCountry()
    {
        // 查询国家
        $r0 = DsCountryModel::where('is_show',1)->select()->toArray();

        $message = Lang('Success');
        return output(200,$message,$r0);
    }

    // 品牌添加/编辑
    public function addBrand()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $brand_id = addslashes(trim($this->request->param('brandId'))); // 品牌名称
        $brand_name = addslashes(trim($this->request->param('brandName'))); // 品牌名称
        $image = addslashes(trim($this->request->param('brandLogo'))); // 品牌图片
        $categories = addslashes(trim($this->request->param('brandClass'))); // 所属分类
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $country_num = addslashes(trim($this->request->param('country_num'))); // 产地
        $remarks = addslashes(trim($this->request->param('remarks'))); // 备注
        
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        $source = 2;
        
        $user_id = $this->user_list['user_id'];

        $shop_id = PC_Tools::SelfOperatedStore($store_id);

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id
        if($shop_id == $mch_id)
        {
            $examine = 1; // 审核状态 0.待审核 1.审核通过 2.不通过
        }
        else
        {
            $examine = 0; // 审核状态 0.待审核 1.审核通过 2.不通过
        }
        
        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'supplier_id'=>0,'brand_id'=>$brand_id,'brand_name'=>$brand_name,'image'=>$image,'categories'=>$categories,'lang_code'=>$lang_code,'country_num'=>$country_num,'remarks'=>$remarks,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>$source,'examine'=>$examine);
        $product_brand = new ProductBrand();
        $product_brand_list = $product_brand->addBrand($data);

        return;
    }

    // 品牌审核列表
    public function Brand_auditList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $name = addslashes(trim($this->request->param('condition'))); // 品牌名称/供应商名称
        $examine = addslashes(trim($this->request->param('status'))); // 审核状态 0.待审核 1.审核通过 2.不通过
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种

        $page = trim($this->request->param('pageNo'));//页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据
        $pagesize = $pagesize ? $pagesize : '10';

        $user_id = $this->user_list['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'supplier_id'=>0,'name'=>$name,'examine'=>$examine,'lang_code'=>$lang_code,'page'=>$page,'pagesize'=>$pagesize,'source'=>2);
        $product_brand = new ProductBrand();
        $product_brand_list = $product_brand->auditList($data);

        return;
    }

    // 品牌删除
    public function DelBrand()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $brand_id = addslashes(trim($this->request->param('brandId'))); // 品牌id

        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        $source = 2;

        $user_id = $this->user_list['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'supplier_id'=>0,'brand_id'=>$brand_id,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>$source);
        $product_brand = new ProductBrand();
        $product_brand_list = $product_brand->delBrand($data);

        return;
    }
    
    // 商品列表
    public function Index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $commodity_type = addslashes(trim($this->request->param('commodityType'))); // 商品类型 0.实物商品 1.虚拟商品
        $classnotset = addslashes($this->request->param('classnotset')); // 未设置 默认值为0  为1的时候表示是未设置
        $product_class = addslashes(trim($this->request->param('cid'))); // 分类ID
        $brandnotset = addslashes($this->request->param('brandnotset')); // 未设置 默认值为0  为1的时候表示是未设置
        $brand_id = addslashes(trim($this->request->param('brandId'))); // 品牌ID
        $status = addslashes(trim($this->request->param('status'))); // 上下架
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $product_title = addslashes(trim($this->request->param('productTitle'))); // 标题
        $IsItDescendingOrder = addslashes($this->request->param('IsItDescendingOrder')); // 是否降序
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize:'10';
 
        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'classnotset'=>$classnotset,'product_class'=>$product_class,'brandnotset'=>$brandnotset,'brand_id'=>$brand_id,'active'=>'','status'=>$status,'lang_code'=>$lang_code,'show_adr'=>'','label_id'=>'','examineStatus'=>'','product_title'=>$product_title,'IsItDescendingOrder'=>$IsItDescendingOrder,'page'=>$page,'pagesize'=>$pagesize,'mch_status'=>2,'commodity_type'=>0,'parameter'=>'','source'=>'mch');

        $product = new Product();
        if($commodity_type == 2)
        {
            $product_list = $product->get_optional_pro_list($data); // 添加自选商品页面（获取平台商品）
        }
        else if($commodity_type == -2)
        {
            $product_list = $product->self_selected_products($data); // 自选商品
        }
        else
        {
            $product_list = $product->get_product_list($data);
        }

        $array = array('store_id'=>$store_id,'shop_id'=>$mch_id);
        $mch = new MchPublicMethod();
        $mch_array = $mch->is_margin($array);
        $is_Payment = $mch_array['is_Payment'];
        $isPromiseExamine = $mch_array['isPromiseExamine'];

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '商品编号',
                1 => '商品标题',
                2 => '商品分类',
                3 => '库存',
                4 => '商品状态',
                5 => '销量',
                6 => '发布时间',
                7 => '商品品牌',
                8 => '价格',
                9 => '所属店铺',
                10 => '上架时间'
            );
            $exportExcel_list = array();

            if ($product_list['list'])
            {
                foreach ($product_list['list'] as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v->id,
                        $v->product_title,
                        $v->pname,
                        $v->num,
                        $v->status_name,
                        $v->volume,
                        $v->add_date,
                        $v->brand_name,
                        $v->price,
                        $v->name,
                        $v->upper_shelf_time,
                    );
                }
            }
            ExcelUtils::exportExcel($exportExcel_list, $titles, '商品列表');
            exit;
        }

        $data = array('list' => $product_list['list'], 'total' => $product_list['total'], 'is_Payment' => $is_Payment, 'isPromiseExamine' => $isPromiseExamine);
        $message = Lang('Success');
        return output('200', $message,$data);
    }
    
    // 待审核商品列表
    public function GetGoodsExamineInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));

        $commodity_type = addslashes(trim($this->request->param('commodityType'))); // 商品类型 -2.自选商品列表 0.实物商品 1.虚拟商品 2.平台商品
    	$product_class = addslashes(trim($this->request->param('classId'))); // 分类ID
    	$brand_id = addslashes(trim($this->request->param('brandId'))); // 品牌ID
    	$examineStatus = addslashes(trim($this->request->param('examineStatus'))); // 审核状态  5.待审核 4.待提交 3 审核失败
    	$product_title = addslashes(trim($this->request->param('productTitle'))); // 标题
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
    	$page = addslashes(trim($this->request->param('pageNo'))); // 页码
    	$pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize:'10';

        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'product_class'=>$product_class,'brand_id'=>$brand_id,'active'=>'','status'=>'','show_adr'=>'','label_id'=>'','examineStatus'=>$examineStatus,'product_title'=>$product_title,'lang_code'=>$lang_code,'page'=>$page,'pagesize'=>$pagesize,'mch_status'=>0,'commodity_type'=>$commodity_type,'parameter'=>'','source'=>'mch');

        $product = new Product();
        $product_list = $product->get_product_list($data);
        
        $data = array('list' => $product_list['list'], 'total' => $product_list['total']);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 获取分类和品牌
    public function GetClass()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $class_str = addslashes(trim($this->request->param('classId'))); // 分类ID
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种

        $product = new product();
        $list = $product->get_classified_brands($store_id,$class_str,'',$lang_code);

        $message = Lang('Success');
        return output(200,$message,json_decode($list,true));
    }

    // 获取商品类型
    public function GetGoodsActive()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $Plugin = new Plugin();
        $Plugin_arr = $Plugin->pro_Plugin($store_id);
        $message = Lang('Success');
        return output(200,$message,$Plugin_arr);
    }

    // 获取商品标签
    public function GetGoodsLabel()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $pagesize = addslashes(trim($this->request->param('pageSize')));

        $list = array();
        $r_sp_type = ProLabelModel::where(['store_id'=>$store_id,'lang_code'=>$lang_code])->page(0,(int)$pagesize)->order('add_time', 'desc')->select()->toArray();
        if ($r_sp_type)
        {
            $list = $r_sp_type;
        }
        $data = array('list'=>$list,'total'=>count($list));
        $message = Lang('Success');
        return output(200,$message,$data);
    }
   
    // 添加/编辑商品
    public function AddGoods()
    {
        $store_id = addslashes(trim($this->request->post('storeId')));
        $access_id = addslashes(trim($this->request->post('accessId')));
        
        // if(!$store_id)
        // {
        //     $store_id = Db::name('user')
        //         ->where('access_id', $access_id)   // 假设数据库字段名为 access_id
        //         ->value('store_id');               // 获取 store_id 字段值
        
        //     if (empty($store_id)) {
        //         throw new \Exception('无效的 accessId，未找到关联的商城');
        //     }
        // }
        
        $store_type = addslashes(trim($this->request->post('storeType')));
        
        
    	$id = addslashes(trim($this->request->post('pId'))); // 商品ID
    	$product_title = addslashes(trim($this->request->post('productTitle'))); // 商品标题
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
    	$country_num = addslashes(trim($this->request->param('country_num'))); // 所属国家
    	$subtitle = addslashes(trim($this->request->post('subtitle'))); // 副标题
    	$keyword = addslashes(trim($this->request->post('keyword'))); // 关键词
    	$product_class = addslashes(trim($this->request->post('productClassId'))); // 商品分类ID
    	$brand_id = addslashes(trim($this->request->post('brandId'))); // 品牌ID
    	$weight = addslashes($this->request->post('weight')); // 重量
        $weight_unit = 'kg';
    	$cover_map = $this->request->post('coverMap'); // 商品封面图
    	$imgurls = $this->request->post('showImg'); // 商品展示图
    	$video = $this->request->post('video'); // 展示视频

    	$initial = $this->request->post('initial'); // 初始值
    	$attr = $this->request->post('attrArr'); // 属性

    	$min_inventory = addslashes(trim($this->request->post('stockWarn'))); // 库存预警
    	$freight = $this->request->post('freightId'); // 运费ID
    	$s_type = trim($this->request->post('sType')); // 显示标签
    	$active = $this->request->post('active'); // 支持活动
    	$show_adr = $this->request->post('displayPosition'); // 展示位置
    	$volume = $this->request->post('volume'); // 销量
    	$sort = $this->request->post('sort'); // 排序

    	$pro_video = $this->request->post('proVideo'); // 商品视频
    	$content = $this->request->post('content'); // 产品内容
    	$unit = addslashes(trim($this->request->post('unit'))); // 单位
    	$mch_sort = addslashes(trim($this->request->post('mchSort'))); // 店铺排序值

    	$draftsId = addslashes(trim($this->request->param('draftsId'))); // 草稿箱ID
        if($draftsId != 0 && $draftsId != '')
        {
            $id = 0;
        }

        $imgurls = explode(',',$imgurls);
        $attr = json_decode($attr, true); // 属性
        $s_type = explode(',',$s_type);
        // $show_adr = explode(',',$show_adr);
        
        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];

        $admin_mch_id = 0;
        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        if($r_admin)
        {
            $admin_mch_id = $r_admin[0]['shop_id']; // 店铺id
        }
        
        // var_dump($store_id,$user_id);die;

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        if($admin_mch_id == $mch_id)
        {
            $mch_status = 2;
        }
        else
        {
            $mch_status = 1;
        }

        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        if($id != 0 && $id != '')
        { // 编辑
            $r_p = ProductListModel::where(['id'=> $id])->field('mch_id,status')->select()->toArray();
            $status = $r_p[0]['status'];

            $data = array('id'=>$id,'product_title'=>$product_title,'lang_code'=>$lang_code,'country_num'=>$country_num,'subtitle'=>$subtitle,'keyword'=>$keyword,'product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'cover_map'=>$cover_map,'imgurls'=>$imgurls,'video'=>$video,
            'initial'=>$initial,'attr'=>$attr,
            'min_inventory'=>$min_inventory,'freight'=>$freight,'s_type'=>$s_type,'active'=>$active,'show_adr'=>$show_adr,'volume'=>$volume,'sort'=>$sort,
            'pro_video'=>$pro_video,'content'=>$content,'richList'=>'','mch_status'=>$mch_status,'unit'=>$unit,'mch_sort'=>$mch_sort,'type'=>'PC','operator_id'=>$operator_id,'operator'=>$operator,'source'=>2);

            $product = new Product();
            $product->edit_product($store_id,$admin_name,$mch_id,$data,'PC店铺');
        }
        else
        { // 添加
            $data = array('product_title'=>$product_title,'lang_code'=>$lang_code,'country_num'=>$country_num,'subtitle'=>$subtitle,'keyword'=>$keyword,'product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'cover_map'=>$cover_map,'imgurls'=>$imgurls,'video'=>$video,
            'initial'=>$initial,'attr'=>$attr,
            'min_inventory'=>$min_inventory,'freight'=>$freight,'s_type'=>$s_type,'active'=>$active,'show_adr'=>$show_adr,'volume'=>$volume,'sort'=>$sort,
            'pro_video'=>$pro_video,'content'=>$content,'richList'=>'','mch_status'=>$mch_status,'unit'=>$unit,'mch_sort'=>$mch_sort,'type'=>'PC','operator_id'=>$operator_id,'operator'=>$operator,'source'=>2,'draftsId'=>$draftsId);

            $product = new Product();
            $product->add_product($store_id,$admin_name,$mch_id,$data,'PC店铺');
        }
        return;
    }

    // 编辑商品页面
    public function GetGoodsInfoById()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = intval($this->request->param('goodsId')); // 商品ID

        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $product = new Product();
        $list = $product->edit_page($store_id,$admin_name,$mch_id,$id,'平台');
        $list = json_decode($list,true);

        $data = array('Plugin_arr'=>$list['Plugin_arr'],'active'=>$list['active'],'attr_group_list'=>$list['attr_group_list'],'brand_class'=>$list['brand_class'],'brand_class_list1'=>$list['brand_class_list1'],'brand_id'=>$list['brand_id'],'brand_name'=>$list['brand_name'],'checked_attr_list'=>$list['checked_attr_list'],'class_id'=>$list['class_id'],'content'=>$list['content'],'cover_map'=>$list['cover_map'],'ctypes'=>$list['ctypes'],'freight_list'=>$list['freight_list'],'freight_list1'=>$list['freight_list1'],'imgurls'=>$list['imgurls'],'initial'=>$list['initial'],'keyword'=>$list['keyword'],'list'=>$list['list'],'mch_id'=>$list['mch_id'],'min_inventory'=>$list['min_inventory'],'product_title'=>$list['product_title'],'richList'=>$list['richList'],'show_adr'=>$list['show_adr'],'sp_type'=>$list['sp_type'],'status'=>$list['status'],'strArr'=>$list['strArr'],'subtitle'=>$list['subtitle'],'unit'=>$list['unit'],'weight'=>$list['weight'],'product_number'=>$list['product_number'],'distributors'=>$list['distributors'],'distributors1'=>$list['distributors1'],'video'=>$list['video'],'proVideo'=>$list['pro_video'],'lang_code'=>$list['lang_code'],'country_num'=>$list['country_num']);
        $message = Lang('Success');
        return output(200,$message,$data);
    }
    
    // 提交审核/撤销审核
    public function SubmitAudit()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));

    	$p_id = addslashes(trim($this->request->param('pIds'))); // 商品ID

        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        
        $data = array('store_id'=>$store_id,'admin_name'=>$admin_name,'user_id'=>$user_id,'p_id'=>$p_id,'type0'=>'PC店铺','operator_id'=>$operator_id,'operator'=>$operator,'source'=>2);

        $product = new Product();
        $product_list = $product->examine($data);
        return;
    }

    // 修改排序
    public function editSort()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = intval($this->request->param('id')); // 商品ID
        $sort = intval($this->request->param('sort')); // 排序
        
        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $sql0 = array('store_id'=>$store_id,'recycle'=>0,'id'=>$id);
        $r0 = Db::name('product_list')->where($sql0)->update(['mch_sort'=>$sort]);
        if ($r0 > -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, ' 修改商品ID为' . $id . '的排序成功', 2,2,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改商品ID为' . $id . '的排序成功';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200, $message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, ' 修改商品ID为' . $id . '的排序失败', 2,2,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改商品排序失败！参数:'.json_encode($sql0);
            $this->Log($Log_content);
            $message = Lang('Modification failed');
            return output(109, $message);
        }
    }

    // 上下架
    public function UpperAndLowerShelves()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = addslashes(trim($this->request->param('goodsIds'))); // 商品ID
    	$status = addslashes(trim($this->request->param('status'))); // 商品状态

        $admin_name = $this->user_list['name'];
        
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        
        $data = array('store_id'=>$store_id,'admin_name'=>$admin_name,'p_id'=>$id,'type'=>'PC','operator_id'=>$operator_id,'operator'=>$operator,'source'=>2);
        $product = new Product();
        $product->upper_and_lower_shelves($data,'PC店铺');
        return;
    }

    // 添加库存页面
    public function GetAttrByGoodsId()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $p_id = addslashes(trim($this->request->param('goodsId'))); // 商品ID
      
        $attr_group_list = array();
        $checked_attr_list = array();
        $list = array();

        $r1 = ConfigureModel::where(['pid'=>$p_id,'recycle'=>0])->field('id,num,min_inventory,attribute')->select()->toArray();
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $attr = '';
                $attribute = unserialize($v['attribute']); // 属性
                foreach ($attribute as $key => $value)
                {
                    if (strpos($key, '_LKT_') !== false)
                    {
                        $key = substr($key, 0, strrpos($key, "_LKT"));
                        $value = substr($value, 0, strrpos($value, "_LKT"));
                    }
                    $attr .= $key . ':' . $value . ';';
                }
                $v['attr'] = $attr;
                $list[] = $v;
            }
            
            $data = array('list'=>$list);
            $message = Lang('Success');
            return output(200, $message,$data);
        }
        else
        {
            $message = Lang('Illegal invasion');
            return output(115, $message);
        }
    }

    // 添加库存
    public function AddStock()
    {
        $store_id = addslashes(trim($this->request->post('storeId')));
        $store_type = addslashes(trim($this->request->post('storeType')));
        $access_id = addslashes(trim($this->request->post('accessId')));
        
    	$stock = $this->request->post('stock'); // 数据
        $stock = json_decode($stock, true); // 属性
        $Jurisdiction = new Jurisdiction();
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];
        $time = date("Y-m-d H:i:s");
        Db::startTrans();
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $z_num = 0;
        $z_num_ = 0;
        foreach($stock as $k => $v)
        {
            $id = $v['id'];
            $addNum = $v['addNum'];
            $z_num_ = $z_num_ + $v['addNum'];
            $r0 = ConfigureModel::where(['id'=>$id])->field('pid,num,total_num,min_inventory')->select()->toArray();
            if($r0)
            {
                $pid = $r0[0]['pid'];
                $min_inventory = $r0[0]['min_inventory'];
                $total_num = $r0[0]['total_num'] + $addNum;
                $num = $r0[0]['num'] + $addNum;
                $z_num = $z_num + $num;

                if ($num < 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '修改商品ID为' . $p_id . '属性ID为' . $sid . '的库存时，库存错误！';
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('Parameter error');
                    return output(109, $message);
                }
                else
                {
                    $sql1_where = array('pid'=>$pid,'id'=>$id);
                    $sql1_update = array('total_num'=>$total_num,'num'=>$num);
                    $r1 = Db::name('configure')->where($sql1_where)->update($sql1_update);
                    if ($r1 == -1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '修改商品ID为' . $p_id . '属性ID为' . $sid . '的库存时，修改失败！';
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('Busy network');
                        return output(109, $message);
                    }
                    else
                    {
                        if((int)$addNum < 0)
                        {
                            $flowing_num = abs($addNum);
                            $content = $admin_name . '减少商品总库存' . $flowing_num;

                            $sql_stock = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$id,'total_num'=>$total_num,'flowing_num'=>$flowing_num,'type'=>1,'add_date'=>$time,'content'=>$content);
                            $r_stock = Db::name('stock')->insert($sql_stock);
                            if($r_stock <= 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '修改商品库存时，添加库存记录失败！sql:'.$sql_stock;
                                $this->Log($Log_content);
                                Db::rollback();
                                $message = Lang('Busy network');
                                return output(109, $message);
                            }
                        }
                        else if((int)$addNum > 0)
                        {
                            $content = $admin_name . '增加商品总库存' . $addNum;
                            $sql_stock = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$id,'total_num'=>$total_num,'flowing_num'=>$addNum,'type'=>0,'add_date'=>$time,'content'=>$content);
                            $r_stock = Db::name('stock')->insert($sql_stock);
                            if($r_stock <= 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '修改商品库存时，添加库存记录失败！sql:'.$sql_stock;
                                $this->Log($Log_content);
                                Db::rollback();
                                $message = Lang('Busy network');
                                return output(109, $message);
                            }
                        }
                        
                        if ($min_inventory >= $num)
                        { // 当属性库存低于等于预警值
                            // 在库存记录表里，添加一条预警信息
                            $content1 = '预警';
                            $sql_stock1 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$id,'total_num'=>$total_num,'flowing_num'=>$addNum,'type'=>2,'add_date'=>$time,'content'=>$content1);
                            $r_stock1 = Db::name('stock')->insert($sql_stock1);
                            if($r_stock1 <= 0)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '修改商品库存时，添加库存记录失败！sql:'.$sql_stock1;
                                $this->Log($Log_content);
                                Db::rollback();
                                $message = Lang('Busy network');
                                return output(109, $message);
                            }
                        }
                        $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $admin_name . '修改商品ID为' . $pid . '属性ID为' . $id . '的库存时，修改成功！';
                        $this->Log($Log_content);
                    }
                }
            }
            else
            {
                Db::rollback();
                $message = Lang('Parameter error');
                return output(109, $message);
            }
        }

        $sql3_where = array('id'=>$pid);
        $sql3_update = array('num'=>$z_num);
        $r3 = Db::name('product_list')->where($sql3_where)->update($sql3_update);
        if ($r3 == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $operator . '修改商品ID为' . $pid . '的库存时，修改失败！';
            $this->Log($Log_content);
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, ' 添加了商品ID：' . $pid . '，' . $z_num_ . ' 个库存失败',2,2,$mch_id,$operator_id);
            $message = Lang('Busy network');
            return output(109, $message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $operator . '修改商品ID为' . $pid . '的库存时，修改成功！';
            $this->Log($Log_content);
            $Jurisdiction->admin_record($store_id, $operator, ' 添加了商品ID：' . $pid . '，' . $z_num_ . ' 个库存成功',2,2,$mch_id,$operator_id);
        }
        
        Db::commit();
        $message = Lang('Success');
        return output(200, $message);
    }

    // 删除商品
    public function DelGoodsById()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = $this->request->param('goodsIds'); // 商品ID

        $admin_name = $this->user_list['name'];
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $product = new Product();
        $product->del($store_id,$admin_name,$id,'PC店铺',$operator,2,$operator_id);
    }

    // 自选商品-添加商品
    public function AddZxGoods()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));
        $language = addslashes($this->request->param('language'));
    	$pro_id = addslashes($this->request->param('goodsIds')); // 商品ID 
    	$freight_id = addslashes($this->request->param('yunFeiId')); // 运费ID
        $pro_list = explode(',', $pro_id);
        
        $admin_name = $this->user_list['name'];
        $user_id = $this->user_list['user_id'];
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id']; // 店铺id
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $admin_name . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
        $array = array('store_id'=>$store_id,'pro_list'=>$pro_list,'freight_id'=>$freight_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>2,'language'=>$language);
        $product = new product();
        $product->Add_self_selection($array);

        return;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("mch/goods.log",$Log_content);
        return;
    }

    // 上传图片
    public function UploadImgs()
    {
        if(!empty($_SERVER) && ($_SERVER['REQUEST_METHOD'] == 'OPTIONS')) 
        {
            header('Access-Control-Allow-Headers:*');
            header('Access-Control-Allow-Methods: GET, POST, PUT,DELETE,OPTIONS,PATCH');
            exit;
        }
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id
        $shop_id = trim($this->request->param('shop_id')); // 店铺ID
        $src_img = trim($this->request->param('src_img')); // 

        if ($store_type = 1 || $store_type = 7)
        {
            $store_type = '0';
        }
        elseif ($store_type = 2)
        {
            $store_type = 'app';
        }

        $imgUrls = array();
        $dir = $store_id . '/' . $store_type . '/' . date("Ymd") . '/';

        if (!empty($_FILES))
        { // 如果图片不为空

            $name = '';
            foreach ($_FILES as $key => $value)
            {
                $name = $key;
            }

            $error = $_FILES[$name]['error'];
            switch ($_FILES[$name]['error'])
            {
                case 0:
                    $msg = '';
                    break;
                case 1:
                    $msg = '超出了php.ini中文件大小';
                    break;
                case 2:
                    $msg = '超出了MAX_FILE_SIZE的文件大小';
                    break;
                case 3:
                    $msg = '文件被部分上传';
                    break;
                case 4:
                    $msg = '没有文件上传';
                    break;
                case 5:
                    $msg = '文件大小为0';
                    break;
                default:
                    $msg = '上传失败';
                    break;
            }

            $imgURL = $_FILES[$name]['tmp_name'];
            $files = $_FILES[$name]['name'];
            $pathinfo = pathinfo($files);
            
            $contentType = $_FILES[$name]['type'];
            $fType = explode('/', $contentType);
            
            $type = isset($pathinfo['extension'])?$pathinfo['extension']:$fType[1];

            $type_0 = PC_Tools::upload();

            $imgURL_name = time() . mt_rand(1, 1000) . '.' .$type;
            $path = $dir . $imgURL_name;
            
            $common = LKTConfigInfo::getOSSConfig();
            try
            {
                //查询文件上传配置方式
                $upserver = Db::name('config')->where('store_id', 1)->value('upserver');
                //阿里云
                if($upserver == '2')
                {
                    $ossClient = OSSCommon::getOSSClient();
                    $ossClient->uploadFile($common['bucket'], $path, $imgURL);
                }
                //MinIO
                if($upserver == '5')
                {
                    $ossClient = new MinIOServer();
                    $ossClient->upLoadObject($imgURL,$path,$contentType);
                }
            }
            catch (OssException $e)
            {
                printf(__FUNCTION__ . ": FAILED\n");
                printf($e->getMessage() . "\n");
                return;
            }

            if($upserver == '2')
            {
                $isopenzdy = $common['isopenzdy'];
                $url = 'https://' . $common['bucket'] . '.' . $common['endpoint'] . '/' . $path;
                if($isopenzdy == 1)
                {
                    $url = 'https://'. $common['MyEndpoint'] . '/' . $path;
                }
            }
            if($upserver == '5')
            {
                $url = 'http://' . $common['endpoint'] . '/' . $common['bucket'] . '/' . $path;
                if (strpos($common['endpoint'], "http") !== false) 
                {
                    $url = $common['endpoint'] . '/' . $common['bucket'] . '/' . $path;
                }
            }

            $fsql = " INSERT INTO `lkt_files_record` ( `store_id`, `store_type`, `group`, `upload_mode`, `image_name`,`mch_id`,`supplier_id`,`type`,`name`) VALUES ('$store_id', '$store_type', '-1', $upserver, '$imgURL_name','$shop_id','0','$type_0','$files') ";
            $res = Db::execute($fsql);
            
            if($src_img != '')
            {
                $collection_code = preg_replace('/.*\//', '', $url);
                $sql_where = array('store_id'=>$store_id,'id'=>$shop_id);
                $sql_update = array('collection_code' => $collection_code);
                $r = Db::name('mch')->where($sql_where)->update($sql_update);
            }
            $data = array('url'=>array($url));
            ob_clean();
            $message = Lang('Success');
            return output('200',$message,$data);
        }
        else
        {
            ob_clean();
            $message = Lang('mch.4');
            return output(109,$message);
        }
    }

    // 批量设置运费
    public function BatchSetShippingFees()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$access_id = addslashes($this->request->param('accessId'));
        
    	$id = $this->request->param('goodsIds'); // 商品ID
    	$fid = intval($this->request->param('fid')); // 运费ID

        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $data = array('store_id'=>$store_id,'p_id'=>$id,'fid'=>$fid,'type'=>'PC店铺','operator_id'=>$operator_id,'operator'=>$operator,'source'=>2);
        $product = new Product();
        $product->BatchSetShippingFees($data);
    }

    // 入库详情/出货详情/库存预警/预警记录
    public function getStockDetailInfo()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $type = addslashes($this->request->param('type')); // 0.入库详情 1.出货详情 2.库存预警
        $pid = addslashes($this->request->param('pid')); // 商品ID
        $attrId = addslashes($this->request->param('attrId')); // 属性ID
        $mch_name = addslashes($this->request->param('mchName'));
        $product_title = addslashes($this->request->param('productTitle'));
        $startdate = addslashes($this->request->param('startDate'));
        $enddate = addslashes($this->request->param('endDate'));
        $page = addslashes($this->request->param('pageNo'));
        $pagesize = addslashes($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';

        $data = array();
        // if($type == 1)
        // { // 出货详情
        //     $data = $this->Shipment_list();
        // }
        // else 
        if($type == 2)
        {
            if($pid != '' && $attrId != '')
            { // 预警记录
                $data = $this->Seewarning_list();
            }
            else
            { // 库存预警
                $data = $this->Warning_list();
            }
        }
        // else if($type == 3)
        // { // 库存详情
        //     $data = $this->Details_list();
        // }
        // else
        // { // 入库详情
        //     $data = $this->Enter_list();
        // }

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 库存预警
    public function Warning_list()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $parameter = addslashes(trim($this->request->param('parameter')));
        $mch_name = addslashes(trim($this->request->param('mchName')));
        $product_title = addslashes($this->request->param('productTitle'));
        $startdate = addslashes($this->request->param('startDate'));
        $enddate = addslashes($this->request->param('endDate'));
        $sortCriteria = addslashes($this->request->param('sortCriteria')); // 排序条件
        $sort = addslashes($this->request->param('sort')); // 排序
        $page = addslashes($this->request->param('pageNo'));
        $pagesize = addslashes($this->request->param('pageSize'));

        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$parameter,'lang_code'=>$lang_code,'mch_name'=>$mch_name,'product_title'=>$product_title,'startdate'=>$startdate,'enddate'=>$enddate,'sortCriteria'=>$sortCriteria,'sort'=>$sort,'page'=>$page,'pagesize'=>$pagesize,'operator_source'=>2);
        $ProductInventory = new ProductInventory();
        $data = $ProductInventory->Warning_list($array);
        $list = $data['list'];

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '序号',
                1 => '商品ID',
                2 => '商品名称',
                3 => '商品售价',
                4 => '商品规格',
                5 => '商品总库存',
                6 => '剩余库存',
                7 => '上架时间'
            );
            $exportExcel_list = array();
            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['attrId'],
                        $v['goodsId'],
                        $v['product_title'],
                        $v['price'],
                        $v['specifications'],
                        $v['total_num'],
                        $v['num'],
                        $v['upper_shelf_time']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '库存预警');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }

        return $data;
    }

    // 预警记录
    public function Seewarning_list()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $pid = addslashes(trim($this->request->param('pid'))); // 商品ID
        $id = addslashes($this->request->param('attrId')); // 属性ID
        $page = addslashes($this->request->param('pageNo'));
        $pagesize = addslashes($this->request->param('pageSize'));

        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'pid'=>$pid,'id'=>$id,'page'=>$page,'pagesize'=>$pagesize,'operator_source'=>2);
        $ProductInventory = new ProductInventory();
        $data = $ProductInventory->Seewarning_list($array);

        return $data;
    }

    // 追加库存
    public function AddInventory()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $pid = addslashes($this->request->param('pid')); // 商品ID
        $id = addslashes($this->request->param('id')); // 属性ID
        $add_num = addslashes($this->request->param('addNum')); // 增加库存

        $user_id = $this->user_list['user_id'];
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id']; // 店铺id
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主' . $admin_name . '没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'pid'=>$pid,'id'=>$id,'add_num'=>$add_num,'operator_id'=>$operator_id,'operator'=>$operator,'operator_source'=>2);
        $ProductInventory = new ProductInventory();
        $data = $ProductInventory->addStock($array);

        return;
    }
}
