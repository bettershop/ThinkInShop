<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\PC_Tools;
use app\common\Tools;
use app\common\FreightPublicMethod;
use app\common\Plugin\Plugin;
use app\common\Product;
use app\common\ExcelUtils;
use PhpOffice\PhpSpreadsheet\IOFactory;
use app\common\ProductClass;
use app\common\ProductBrand;
use app\common\ProductInventory;

use app\admin\model\ProductClassModel;
use app\admin\model\ProductListModel;
use app\admin\model\BrandClassModel;
use app\admin\model\DsCountryModel;
use app\admin\model\AdminCgGroupModel;
use app\admin\model\AdminModel;
use app\admin\model\FreightModel;
use app\admin\model\ProductConfigModel;
use app\admin\model\ProLabelModel;
use app\admin\model\ConfigureModel;
use app\admin\model\MchModel;

/**
 * 功能：商品类
 * 修改人：DHB
 */
class Goods extends BaseController
{
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
        $data = array('store_id'=>$store_id,'mch_id'=>0,'supplier_id'=>0,'type'=>$type,'classId'=>$classId,'className'=>$className,'lang_code'=>$lang_code,'page'=>$page,'pagesize'=>$pagesize,'source'=>1);
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

        $cid = addslashes(trim($this->request->param('classId'))); // 分类ID
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语言
        $country_num = addslashes(trim($this->request->param('country_num'))); // 国家代码
        $pname = trim($this->request->param('className')); // 分类名称
        $english_name = trim($this->request->param('ename')); // 分类英文名称
        $level = addslashes(trim($this->request->param('level'))); // 级别
        $sid = addslashes(trim($this->request->param('fatherId'))); // 上级ID
        $img = addslashes(trim($this->request->param('img'))); // 图片

        $time = date("Y-m-d H:i:s");

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $source = 1;
        $examine = 1; // 审核状态 0.待审核 1.审核通过 2.不通过

        $data = array('store_id'=>$store_id,'mch_id'=>0,'supplier_id'=>0,'cid'=>$cid,'lang_code'=>$lang_code,'country_num'=>$country_num,'pname'=>$pname,'english_name'=>$english_name,'level'=>$level,'sid'=>$sid,'img'=>$img,'examine'=>$examine,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>$source);
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

        $data = array('store_id'=>$store_id,'cid'=>$cid,'source'=>1);
        $product_class = new ProductClass();
        $product_class_list = $product_class->getClassLevelTopAllInfo($data);

        return;
    }

    // 分类置顶
    public function classSortTop()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $cid = addslashes(trim($this->request->param('classId')));
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $source = 1;
        
        $data = array('store_id'=>$store_id,'mch_id'=>0,'cid'=>$cid,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>$source);
        $product_class = new ProductClass();
        $product_class_list = $product_class->classSortTop($data);

        return;
    }

    // 分类删除
    public function delClass()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $cid = addslashes(trim($this->request->param('classId')));

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $source = 1;
        
        $data = array('store_id'=>$store_id,'mch_id'=>0,'cid'=>$cid,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>$source);
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

        $data = array('store_id'=>$store_id,'mch_id'=>0,'supplier_id'=>0,'lang_code'=>$lang_code,'brand_name'=>$brand_name,'page'=>$page,'pagesize'=>$pagesize,'source'=>1);
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

        $brand_id = addslashes(trim($this->request->param('brandId'))); // 品牌ID
        $brand_name = addslashes(trim($this->request->param('brandName'))); // 品牌名称
        $image = addslashes(trim($this->request->param('brandLogo'))); // 品牌图片
        $categories = addslashes(trim($this->request->param('brandClass'))); // 所属分类
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $country_num = addslashes(trim($this->request->param('country_num'))); // 产地
        $remarks = addslashes(trim($this->request->param('remarks'))); // 备注

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $source = 1;
        $examine = 1;
        
        $data = array('store_id'=>$store_id,'mch_id'=>0,'supplier_id'=>0,'brand_id'=>$brand_id,'brand_name'=>$brand_name,'image'=>$image,'categories'=>$categories,'lang_code'=>$lang_code,'country_num'=>$country_num,'remarks'=>$remarks,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>$source,'examine'=>$examine);
        $product_brand = new ProductBrand();
        $product_brand_list = $product_brand->addBrand($data);

        return;
    }

    // 品牌置顶
    public function brandByTop()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $brand_id = addslashes(trim($this->request->param('brandId'))); // 品牌id

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $source = 1;

        $data = array('store_id'=>$store_id,'mch_id'=>0,'supplier_id'=>0,'brand_id'=>$brand_id,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>$source);
        $product_brand = new ProductBrand();
        $product_brand_list = $product_brand->brandByTop($data);

        return;
    }

    // 品牌删除
    public function delBrand()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $brand_id = addslashes(trim($this->request->param('brandId'))); // 品牌id

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $source = 1;

        $data = array('store_id'=>$store_id,'mch_id'=>0,'supplier_id'=>0,'brand_id'=>$brand_id,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>$source);
        $product_brand = new ProductBrand();
        $product_brand_list = $product_brand->delBrand($data);

        return;
    }

    // 设为默认运费
    public function freightSetDefault()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = intval($this->request->param('id'));

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $admin_name = $this->user_list['name'];
        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id']; // 店铺id

        $data = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'id'=>$id,'operator'=>$operator,'source'=>1);
        $freight = new FreightPublicMethod();
        $freight_list = $freight->set_default($data);
        return;
    }
    
    // 删除运费
    public function delFreight()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        // 接收信息
        $id = addslashes(trim($this->request->param('idList'))); // 单运费id
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if($id == '' || $id == 0)
        {
            $id = addslashes(trim($this->request->param('freightIds'))); // 多运费id
        }

        $admin_name = $this->user_list['name'];
        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id']; // 店铺id

        $data = array('store_id'=>$store_id,'admin_name'=>$admin_name,'mch_id'=>$mch_id,'id'=>$id,'operator'=>$operator,'source'=>1);
        $freight = new FreightPublicMethod();
        $freight_list = $freight->freight_del($data);

        return;
    }

    // 获取活动类型
    public function getGoodsActive()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $Plugin = new Plugin();
        $data = $Plugin->pro_Plugin($store_id);

        $message = Lang('Success');
        return output('200',$message,$data);
    }

    // 获取分类和品牌
    public function choiceClass()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $class_str = addslashes(trim($this->request->param('classId')));
        $brand_id = addslashes(trim($this->request->param('brandId')));
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        if(!$lang_code)
        {
           $lang_code =  addslashes(trim($this->request->param('language'))); // 语种
        }
        $product = new Product();
        $data = $product->get_classified_brands($store_id,$class_str,$brand_id,$lang_code);

        $message = Lang('Success');
        return output(200,$message,json_decode($data));
    }

    // 获取地区
    public function getRegion()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $sid = addslashes(trim($this->request->param('sid')));
        $level = addslashes(trim($this->request->param('level')));

        $list = array();
        $str = "id,district_name as districtName,district_pid as districtPid,district_show_order as districtShowOrder,district_level as districtLevel,district_childcount as district_ChildCount,district_delete as districtDelete,district_num as districtNum,district_country_num as districtCountryNum";
        if($level != 0 && $sid != 0)
        {
            $r0_0 = AdminCgGroupModel::where(['district_level'=>$level,'district_pid'=>$sid])->field($str)->select()->toArray();
        }
        else
        {
            $r0_0 = AdminCgGroupModel::where(['district_pid'=>0])->field($str)->select()->toArray();
        }
        if($r0_0)
        {
            foreach ($r0_0 as $k => $v)
            {
                $list[] = $v;
            }
        }

        $message = Lang('Success');
        return output(200,$message,$list);
    }

    // 添加店铺
    public function addMch()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$logo = addslashes($this->request->param('logo')); // logo
    	$head_img = addslashes($this->request->param('headImg')); // logo
    	$poster_img = addslashes($this->request->param('posterImg')); // logo
    	$name = addslashes($this->request->param('name')); // 店铺名称
    	$shop_information = addslashes($this->request->param('shop_information')); // 店铺信息
    	$cid = addslashes($this->request->param('cid')); // 店铺分类ID
    	$shop_range = addslashes($this->request->param('shop_range')); // 经营范围
    	$realname = addslashes($this->request->param('realname')); // 真实姓名
    	$ID_number = addslashes($this->request->param('ID_number')); // 身份证号码
    	$tel = addslashes($this->request->param('tel')); // 联系电话
    	$city_all = addslashes($this->request->param('city_all')); // 联系地址
    	$address = addslashes($this->request->param('address')); // 联系地址
    	$shop_nature = addslashes($this->request->param('shop_nature')); // 店铺性质
    	$imgUrls = addslashes($this->request->param('imgUrls')); // 营业执照 

        $admin_name = $this->user_list['name'];

        $JurisdictionAction = new Jurisdiction();

        if (!empty($logo))
        {
            $logo = preg_replace('/.*\//', '', $logo);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 店铺logo不能为空';
            $this->Log($Log_content);
            $message = Lang('店铺logo不能为空！');
            return output(109, $message);
        }
        
        if($name == '')
        {
            $message = Lang('set.1');
            return output(109,$message);
        }
        
        if (empty($shop_information))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 店铺信息不能为空！';
            $this->Log($Log_content);
            $message = Lang('mch.28');
            return output(109,$message);
        }
        
        if (empty($cid))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 店铺分类不能为空！';
            $this->Log($Log_content);
            $message = Lang('mch.64');
            return output(109,$message);
        }
        
        if (empty($shop_range))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 经营范围不能为空';
            $this->Log($Log_content);
            $message = Lang('mch.30');
            return output(109,$message);
        }
        if (strlen($shop_range) > 150)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 经营范围不能超过50个中文字长度';
            $this->Log($Log_content);
            $message = Lang('mch.31');
            return output(109,$message);
        }
        
        if (empty($realname))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 用户姓名不能为空！';
            $this->Log($Log_content);
            $message = Lang('mch.65');
            return output(109,$message);
        }
        
        if (empty($ID_number))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 身份证号码不能为空！';
            $this->Log($Log_content);
            $message = Lang('mch.2');
            return output(109,$message);
        }
        else 
        {
            $res = $this->is_idcard($ID_number);
            if (!$res)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '身份证号码错误！';
                $this->Log($Log_content);
                $message = Lang('mch.2');
                return output(225, $message);
            }
        }
        
        if ($tel == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 联系电话不能为空！';
            $this->Log($Log_content);
            $message = Lang('mch.17');
            return output(109,$message);
        }

        $city_list = explode('-',$city_all);
        $sheng = $city_list[0];
        $shi = $city_list[1];
        $xian = $city_list[2];
        if($address == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '详细地址不能为空！';
            $this->Log($Log_content);
            $message = Lang('mch.5');
            return output(109, $message);
        }
        
        $address_xx = $sheng.$shi.$xian.$address;
        
        $Longitude_and_latitude = Tools::get_Longitude_and_latitude($store_id,$address_xx);
        $longitude = $Longitude_and_latitude['longitude'];
        $latitude = $Longitude_and_latitude['latitude'];

        $business_license = '';
        if($imgUrls != '')
        {
            $imgUrls_list = explode(',',$imgUrls);
            foreach($imgUrls_list as $k => $v)
            {
                $business_license .= preg_replace('/.*\//', '', $v) . ',';
            }
            $business_license = trim($business_license,',');
        }
        
        $r0 = AdminModel::where(['store_id'=>$store_id,'type'=>1])->field('name')->select()->toArray();
        $user_id = $r0[0]['name']; // 客户账号

        $time = date("Y-m-d H:i:s");

        $zhanghao = Tools::generateRandomString(8);
        $mima = Tools::lock_url('000000');

        // 添加用户信息
        $sql_user = array('store_id'=>$store_id,'user_id'=>$user_id,'user_name'=>$name,'real_name'=>$realname,'mobile'=>$tel,'zhanghao'=>$zhanghao,'mima'=>$mima,'Register_data'=>$time,'source'=>6,'birthday'=>$time);
        $user = Db::name('user')->insert($sql_user);
        if($user < 1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加会员信息失败';
            $this->Log($Log_content);
            $message = Lang('shop.18');
            return output(109, $message);
        }

        $sql1 = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'shop_information'=>$shop_information,'cid'=>$cid,'shop_range'=>$shop_range,'realname'=>$realname,'ID_number'=>$ID_number,'tel'=>$tel,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'logo'=>$logo,'shop_nature'=>$shop_nature,'business_license'=>$business_license,'review_status'=>1,'add_time'=>$time,'is_open'=>1,'longitude'=>$longitude,'latitude'=>$latitude,'poster_img'=>$poster_img,'head_img'=>$head_img);
        $res_data = Db::name('mch')->insertGetId($sql1);
        if ($res_data > 0)
        {
            Db::name('admin')->where('store_id', $store_id)->update(['shop_id' => $res_data]);

            $array = array('store_id'=>$store_id,'type0'=>3,'id'=>$res_data,'name'=>$name);
            PC_Tools::jump_path($array);

            $address_xq = $sheng . $shi . $xian . $address;
            $sqll = array('store_id'=>$store_id,'name'=>$realname,'tel'=>$tel,'code'=>'000000','sheng'=>$sheng,'shi'=>$shi,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'address_xq'=>$address_xq,'uid'=>'admin','type'=>2,'is_default'=>1);
            $r = Db::name('service_address')->insert($sqll);

            $JurisdictionAction->admin_record($store_id, $admin_name, '设置自营店铺成功', 1);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 设置自营店铺成功';
            $this->Log($Log_content);
            $data = array('mchId'=>$res_data);
            $message = Lang('Success');
            return output(200, $message,$data);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '设置自营店铺失败', 1);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 设置自营店铺失败！参数:'.json_encode($sql1);
            $this->Log($Log_content);
            $message = Lang('Busy network');
            return output(109, $message);
        }
    }
    
    // 验证身份证格式是否正确
    public function is_idcard($id)
    {
        $id = strtoupper($id);
        $regx = "/(^\d{15}$)|(^\d{17}([0-9]|X)$)/";
        $arr_split = array();
        if (!preg_match($regx, $id))
        {
            return false;
        }
        if (15 == strlen($id)) //检查15位
        {
            $regx = "/^(\d{6})+(\d{2})+(\d{2})+(\d{2})+(\d{3})$/";

            @preg_match($regx, $id, $arr_split);
            //检查生日日期是否正确
            $dtm_birth = "19" . $arr_split[2] . '/' . $arr_split[3] . '/' . $arr_split[4];
            if (!strtotime($dtm_birth))
            {
                return FALSE;
            }
            else
            {
                return TRUE;
            }
        }
        else      //检查18位
        {
            $regx = "/^(\d{6})+(\d{4})+(\d{2})+(\d{2})+(\d{3})([0-9]|X)$/";
            @preg_match($regx, $id, $arr_split);
            $dtm_birth = $arr_split[2] . '/' . $arr_split[3] . '/' . $arr_split[4];
            if (!strtotime($dtm_birth)) //检查生日日期是否正确
            {
                return FALSE;
            }
            else
            {
                //检验18位身份证的校验码是否正确。
                //校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。
                $arr_int = array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
                $arr_ch = array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
                $sign = 0;
                for ($i = 0; $i < 17; $i++)
                {
                    $b = (int)$id[$i];
                    $w = $arr_int[$i];
                    $sign += $b * $w;
                }
                $n = $sign % 11;
                $val_num = $arr_ch[$n];
                if ($val_num != substr($id, 17, 1))
                {
                    return FALSE;
                } //phpfensi.com
                else
                {
                    return TRUE;
                }
            }
        }
    }

    // 获取运费
    public function getFreightInfo()
    {
        
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $mch_id = addslashes(trim($this->request->param('mchId')));
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $pageSize = addslashes(trim($this->request->param('pageSize')));
        
        if(!$lang_code)
        {
            $lang_code = trim($this->request->param('language'));  
        }
        
        // $lang_code = 'zh_CN';
        $admin_name = $this->user_list['name'];

        $freight = array();
        $r0 = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code])->order('is_default','desc')->field('id,name,is_default')->select()->toArray();
        // 运费
        $r0_0 = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code])->page(0,(int)$pageSize)->order('is_default','desc')->field('id,name,is_default')->select()->toArray();
        // var_dump($lang_code);die;
        if ($r0_0)
        {
            foreach ($r0_0 as $k => $v)
            {
                $freight[] = array('id' => $v['id'], 'name' => $v['name'], 'is_default' => $v['is_default']);
            }
        }
        
        // 
        $data = array('list'=>$freight,'total'=>count($r0),'start'=>1,'package_settings'=>0);
        $message = Lang('Success');
        
        return output(200,$message,$data);
        
    }

    // 添加/编辑商品
    public function addGoods()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        $mch_id = addslashes(trim($this->request->param('mch_id'))); // 店铺ID
        
    	$id = addslashes(trim($this->request->param('pId'))); // 商品ID
    	$commodity_type = addslashes(trim($this->request->param('commodityType'))); // 商品类型 0.实物商品 1.虚拟商品
    	$product_title = addslashes(trim($this->request->param('productTitle'))); // 商品标题        
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
    	$country_num = addslashes(trim($this->request->param('country_num'))); // 所属国家
    	$subtitle = addslashes(trim($this->request->param('subtitle'))); // 副标题
    	$keyword = addslashes(trim($this->request->param('keyword'))); // 关键词
    	$product_class = addslashes(trim($this->request->param('productClassId'))); // 商品分类ID
    	$brand_id = addslashes(trim($this->request->param('brandId'))); // 品牌ID
    	$weight = addslashes($this->request->param('weight')); // 重量
        $weight_unit = 'kg';
    	$cover_map = $this->request->param('coverMap'); // 商品封面图
    	$imgurls = $this->request->param('showImg'); // 商品展示图
    	$video = $this->request->param('video'); // 展示视频

    	$initial = $this->request->param('initial'); // 初始值
    	$attr = $this->request->param('attrArr'); // 属性

    	$min_inventory = addslashes(trim($this->request->param('stockWarn'))); // 库存预警
    	$freight = $this->request->param('freightId'); // 运费ID
    	$s_type = trim($this->request->param('sType')); // 显示标签
    	$active = $this->request->param('active'); // 支持活动
    	$show_adr = $this->request->param('displayPosition'); // 展示位置
    	$volume = $this->request->param('volume'); // 销量
    	$sort = $this->request->param('sort'); // 排序
    	
    	$pro_video = $this->request->param('proVideo'); // 商品视频
    	$content = $this->request->param('content'); // 产品内容
    	$unit = addslashes(trim($this->request->param('unit'))); // 单位

    	$write_off_settings = addslashes(trim($this->request->param('writeOffSettings'))); // 核销设置 1.线下核销 2.无需核销
    	$write_off_mch_ids = addslashes(trim($this->request->param('writeOffMchIds'))); // 核销门店id  0.全部门店,  1,2,3使用逗号分割
    	$is_appointment = addslashes(trim($this->request->param('isAppointment'))); // 预约时间设置 1.无需预约下单 2.需要预约下单

    	$draftsId = addslashes(trim($this->request->param('draftsId'))); // 草稿箱ID

        $imgurls = explode(',',$imgurls);
        $attr = json_decode($attr, true); // 属性
        $s_type = explode(',',$s_type);

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $admin_name = $this->user_list['name'];
        if($id != '' && $id != 0 && $id != "null")
        { // 编辑
            $r_p = ProductListModel::where(['id'=> $id])->field('mch_id,status')->select()->toArray();
            $status = $r_p[0]['status'];
            $mch_id = $r_p[0]['mch_id'];

            $data = array('id'=>$id,'commodity_type'=>$commodity_type,'lang_code'=>$lang_code,'country_num'=>$country_num,'product_title'=>$product_title,'subtitle'=>$subtitle,'keyword'=>$keyword,'product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'cover_map'=>$cover_map,'imgurls'=>$imgurls,'video'=>$video,
            'initial'=>$initial,'attr'=>$attr,
            'min_inventory'=>$min_inventory,'freight'=>$freight,'s_type'=>$s_type,'active'=>$active,'show_adr'=>$show_adr,'volume'=>$volume,'sort'=>$sort,
            'pro_video'=>$pro_video,'content'=>$content,'richList'=>'','mch_status'=>2,'unit'=>$unit,'write_off_settings'=>$write_off_settings,'write_off_mch_ids'=>$write_off_mch_ids,'is_appointment'=>$is_appointment,'type'=>'Admin','operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);

            $product = new Product();
            $product->edit_product($store_id,$admin_name,$mch_id,$data,'平台');
        }
        else
        { // 添加
            if($mch_id == '' || $mch_id == 0)
            {
                $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1,'recycle'=>0])->field('shop_id')->select()->toArray();
                $mch_id = $r_admin[0]['shop_id'];
            }
            
            $data = array('commodity_type'=>$commodity_type,'lang_code'=>$lang_code,'country_num'=>$country_num,'product_title'=>$product_title,'subtitle'=>$subtitle,'keyword'=>$keyword,'product_class'=>$product_class,'brand_id'=>$brand_id,'weight'=>$weight,'weight_unit'=>$weight_unit,'cover_map'=>$cover_map,'imgurls'=>$imgurls,'video'=>$video,
            'initial'=>$initial,'attr'=>$attr,
            'min_inventory'=>$min_inventory,'freight'=>$freight,'s_type'=>$s_type,'active'=>$active,'show_adr'=>$show_adr,'volume'=>$volume,'sort'=>$sort,
            'pro_video'=>$pro_video,'content'=>$content,'richList'=>'','mch_status'=>2,'unit'=>$unit,'write_off_settings'=>$write_off_settings,'write_off_mch_ids'=>$write_off_mch_ids,'is_appointment'=>$is_appointment,'type'=>'Admin','operator_id'=>$operator_id,'operator'=>$operator,'source'=>1,'draftsId'=>$draftsId);

            $product = new Product();
            $product->add_product($store_id,$admin_name,$mch_id,$data,'平台');
        }
        return;
    }
    
    // 编辑商品页面
    public function getGoodsInfoById()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

    	$id = intval($this->request->param('goodsId')); // 商品ID

        $admin_name = $this->user_list['name'];

        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id']; // 店铺id

        $product = new Product();
        $list = $product->edit_page($store_id,$admin_name,$mch_id,$id,'平台');
        $list = json_decode($list,true);

        $data = array('Plugin_arr'=>$list['Plugin_arr'],'active'=>$list['active'],'attr_group_list'=>$list['attr_group_list'],'brand_class'=>$list['brand_class'],'brand_class_list1'=>$list['brand_class_list1'],'brand_id'=>$list['brand_id'],'brand_name'=>$list['brand_name'],'checked_attr_list'=>$list['checked_attr_list'],'class_id'=>$list['class_id'],'content'=>$list['content'],'cover_map'=>$list['cover_map'],'ctypes'=>$list['ctypes'],'freight_list'=>$list['freight_list'],'freight_list1'=>$list['freight_list1'],'imgurls'=>$list['imgurls'],'initial'=>$list['initial'],'keyword'=>$list['keyword'],'list'=>$list['list'],'mch_id'=>$list['mch_id'],'min_inventory'=>$list['min_inventory'],'product_title'=>stripslashes($list['product_title']),'richList'=>$list['richList'],'show_adr'=>$list['show_adr'],'sp_type'=>$list['sp_type'],'status'=>$list['status'],'strArr'=>$list['strArr'],'subtitle'=>$list['subtitle'],'unit'=>$list['unit'],'weight'=>$list['weight'],'product_number'=>$list['product_number'],'distributors'=>$list['distributors'],'distributors1'=>$list['distributors1'],'video'=>$list['video'],'proVideo'=>$list['pro_video'],'commodityType'=>$list['commodity_type'],'writeOffSettings'=>$list['write_off_settings'],'writeOffMchIds'=>$list['write_off_mch_ids'],'isAppointment'=>$list['is_appointment'],'lang_code'=>$list['lang_code'],'country_num'=>$list['country_num']);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 上架/下架
    public function upperAndLowerShelves()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = $this->request->param('goodsIds'); // 商品ID
    	$status = intval($this->request->param('status')); // 商品ID

        $admin_name = $this->user_list['name'];
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data = array('store_id'=>$store_id,'admin_name'=>$admin_name,'p_id'=>$id,'type'=>'Admin','operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $product = new Product();
        $product->upper_and_lower_shelves($data,'平台');
    }

    // 批量上下架
    public function BatchLoadingAndUnloading()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = $this->request->param('goodsIds'); // 商品ID
    	$status = intval($this->request->param('status')); // 2.上架 3.下架

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data = array('store_id'=>$store_id,'p_id'=>$id,'status'=>$status,'type'=>'平台','operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $product = new Product();
        $product->BatchLoadingAndUnloading($data);
    }

    // 批量选择位置
    public function BatchSelectionOfLocations()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = $this->request->param('goodsIds'); // 商品ID
    	$status = intval($this->request->param('status')); // 

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data = array('store_id'=>$store_id,'p_id'=>$id,'status'=>$status,'type'=>'平台','operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $product = new Product();
        $product->BatchSelectionOfLocations($data);
    }

    // 批量获取运费
    public function BatchObtainShippingFees()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = $this->request->param('goodsIds'); // 商品ID

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data = array('store_id'=>$store_id,'p_id'=>$id,'type'=>'平台','operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $product = new Product();
        $product->BatchObtainShippingFees($data);
    }

    // 批量设置运费
    public function BatchSetShippingFees()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = $this->request->param('goodsIds'); // 商品ID
    	$fid = intval($this->request->param('fid')); // 运费ID

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data = array('store_id'=>$store_id,'p_id'=>$id,'fid'=>$fid,'type'=>'平台','operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $product = new Product();
        $product->BatchSetShippingFees($data);
    }

    // 批量预警
    public function BatchWarning()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = $this->request->param('goodsIds'); // 商品ID
    	$min_inventory = intval($this->request->param('min_inventory')); // 库存预警

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data = array('store_id'=>$store_id,'p_id'=>$id,'min_inventory'=>$min_inventory,'type'=>'平台','operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $product = new Product();
        $product->BatchWarning($data);
    }

    // 是否显示已下架商品
    public function isOpen()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $time = date("Y-m-d H:i:s");
        $is_open = 0;
        $r_con = ProductConfigModel::where(['store_id'=>$store_id])->field('is_open')->select()->toArray();
        if($r_con)
        {
            $is_open = $r_con[0]['is_open'];
            if($is_open == 0)
            {
                $r = Db::name('product_config')->where('store_id',$store_id)->update(['is_open' => '1']);
            }
            else
            {
                $r = Db::name('product_config')->where('store_id',$store_id)->update(['is_open' => '0']);
            }
        }
        else
        {
            $sql_con = array('store_id'=>$store_id,'config'=>'','add_date'=>$time,'is_open'=>1);
            $r_con = Db::name('product_config')->insert($sql_con);
        }

        $Jurisdiction->admin_record($store_id, $operator, ' 进行了是否显示已下架商品操作',2,1,0,$operator_id);
        $message = Lang('Success');
        return output(200, $message);
    }

    // 是否显示已售罄商品
    public function displaySellOut()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $time = date("Y-m-d H:i:s");
        $is_open = 0;
        $r_con = ProductConfigModel::where(['store_id'=>$store_id])->field('is_display_sell_put')->select()->toArray();
        if($r_con)
        {
            $is_display_sell_put = $r_con[0]['is_display_sell_put'];
            if($is_display_sell_put == 0)
            {
                $r = Db::name('product_config')->where('store_id',$store_id)->update(['is_display_sell_put' => '1']);
            }
            else
            {
                $r = Db::name('product_config')->where('store_id',$store_id)->update(['is_display_sell_put' => '0']);
            }
        }
        else
        {
            $sql_con = array('store_id'=>$store_id,'config'=>'','add_date'=>$time,'is_display_sell_put'=>1);
            $r_con = Db::name('product_config')->insert($sql_con);
        }

        $Jurisdiction->admin_record($store_id, $operator, ' 进行了显示已售罄商品操作',2,1,0,$operator_id);
        $message = Lang('Success');
        return output(200, $message);
    }

    // 商品列表
    public function index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $parameter = addslashes($this->request->param('parameter')); // 商品ID
        $commodity_type = addslashes($this->request->param('commodityType')); // 商品类型 0.实物商品 1.虚拟商品
        $classnotset = addslashes($this->request->param('classnotset')); // 未设置 默认值为0  为1的时候表示是未设置
        $product_class = addslashes($this->request->param('cid')); // 分类ID
        $brandnotset = addslashes($this->request->param('brandnotset')); // 未设置 默认值为0  为1的时候表示是未设置
        $brand_id = addslashes($this->request->param('brandId')); // 品牌ID
        $active = addslashes($this->request->param('active')); // 商品类型
        $status = addslashes($this->request->param('status')); // 上下架
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $show_adr = addslashes($this->request->param('showAdr')); // 显示位置
        $label_id = addslashes($this->request->param('goodsTga')); // 分类标签ID
        $product_title = addslashes($this->request->param('productTitle')); // 标题
        $IsItDescendingOrder = addslashes($this->request->param('IsItDescendingOrder')); // 是否降序

        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize:'10';

        $is_open = 0;
        $is_display_sell_put = 0;
        $r_con = ProductConfigModel::where(['store_id'=>$store_id])->field('is_open,is_display_sell_put')->select()->toArray();
        if($r_con)
        {
            $is_open = $r_con[0]['is_open'];
            $is_display_sell_put = $r_con[0]['is_display_sell_put'];
        }

        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id'];

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'classnotset'=>$classnotset,'product_class'=>$product_class,'brandnotset'=>$brandnotset,'brand_id'=>$brand_id,'active'=>$active,'status'=>$status,'lang_code'=>$lang_code,'show_adr'=>$show_adr,'label_id'=>$label_id,'examineStatus'=>'','product_title'=>$product_title,'IsItDescendingOrder'=>$IsItDescendingOrder,'page'=>$page,'pagesize'=>$pagesize,'mch_status'=>2,'commodity_type'=>$commodity_type,'parameter'=>$parameter,'source'=>'admin');
        $product = new Product();
        $product_list = $product->get_product_list($data);

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '商品编号',
                1 => '商品标题',
                2 => '商品分类',
                3 => '品牌',
                4 => '价格',
                5 => '库存',
                6 => '商品状态',
                7 => '所属店铺',
                8 => '销量',
                9 => '上架时间',
                10 => '发布时间'
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
                        $v->brand_name,
                        $v->price,
                        $v->num,
                        $v->status_name,
                        $v->name,
                        $v->volume,
                        $v->upper_shelf_time,
                        $v->add_date
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '商品列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }

        $data = array('store_id' => $store_id,'mch_id' => $mch_id,'class_id' => $product_class, 'ctypes' => $mch_id, 'brand_id' => $brand_id, 'is_open' => $is_open,'isDisplaySellOut' => $is_display_sell_put,'list' => $product_list['list'],'total' => $product_list['total']);

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 修改排序
    public function editSort()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = intval($this->request->param('id')); // 商品ID
    	$sort = intval($this->request->param('sort')); // 排序

        $admin_name = $this->user_list['name'];
        $JurisdictionAction = new Jurisdiction();

        $sql0 = array('store_id'=>$store_id,'recycle'=>0,'id'=>$id);
        $r0 = Db::name('product_list')->where($sql0)->update(['sort'=>$sort]);
        if ($r0 > -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改商品ID为' . $id . '的排序成功', 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改商品ID为' . $id . '的排序成功';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200, $message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改商品ID为' . $id . '的排序失败', 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改商品排序失败！参数:'.json_encode($sql0);
            $this->Log($Log_content);
            $message = Lang('Modification failed');
            return output(109, $message);
        }
    }

    // 删除商品
    public function delGoodsById()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = $this->request->param('goodsId'); // 商品ID

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $id = rtrim($id, ','); // 去掉最后一个逗号
        if(isset($this->user_list['name']))
        {
            $admin_name = $this->user_list['name'];
        }
        else
        {
            $admin_name = $this->user_list['supplier_name'];
        }
        
        $product = new Product();
        $product->del($store_id,$admin_name,$id,'平台',$operator,1,$operator_id);
    }

    // 库存列表
    public function getStockInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $mch_name = addslashes(trim($this->request->param('mchName')));
        $product_title = addslashes(trim($this->request->param('productTitle')));
        $page = addslashes(trim($this->request->param('pageNo')));
        $pagesize = addslashes(trim($this->request->param('pageSize')));

        $array = array('store_id'=>$store_id,'mch_id'=>0,'lang_code'=>$lang_code,'mch_name'=>$mch_name,'product_title'=>$product_title,'page'=>$page,'pagesize'=>$pagesize,'operator_source'=>1);
        $ProductInventory = new ProductInventory();
        $data = $ProductInventory->Inventory_List($array);
        $list = $data['list'];

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '序号',
                1 => '商品名称',
                2 => '商品售价',
                3 => '商品规格',
                4 => '商品总库存',
                5 => '剩余库存',
                6 => '供货商',
                7 => '上架时间'
            );
            $exportExcel_list = array();
            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['id'],
                        $v['product_title'],
                        $v['price'],
                        $v['specifications'],
                        $v['total_num'],
                        $v['num'],
                        $v['shop_name'],
                        $v['upper_shelf_time']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '库存列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }

        $message = Lang('Success');
        return output("200", $message,$data);
    }

    // 入库详情/出货详情/库存预警/预警记录
    public function getStockDetailInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

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

        if($type == 1)
        { // 出货详情
            $data = $this->Shipment_list();
        }
        else if($type == 2)
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
        else if($type == 3)
        { // 库存详情
            $data = $this->Details_list();
        }
        else
        { // 入库详情
            $data = $this->Enter_list();
        }

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 入库详情
    public function Enter_list()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        
        if(!$lang_code)
        {
            $lang_code = addslashes(trim($this->request->param('language')));
        }
        
        $mch_name = addslashes(trim($this->request->param('mchName')));
        $product_title = addslashes($this->request->param('productTitle'));
        $startdate = addslashes($this->request->param('startDate'));
        $enddate = addslashes($this->request->param('endDate'));
        $page = addslashes($this->request->param('pageNo'));
        $pagesize = addslashes($this->request->param('pageSize'));

        $array = array('store_id'=>$store_id,'mch_id'=>0,'lang_code'=>$lang_code,'mch_name'=>$mch_name,'product_title'=>$product_title,'startdate'=>$startdate,'enddate'=>$enddate,'page'=>$page,'pagesize'=>$pagesize,'operator_source'=>1);
        $ProductInventory = new ProductInventory();
        $data = $ProductInventory->Enter_list($array);
        $list = $data['list'];
        
        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '序号',
                1 => '商品名称',
                2 => '商品售价',
                3 => '商品规格',
                4 => '商品状态',
                5 => '商品总库存',
                6 => '入库数量',
                7 => '入库时间',
                8 => '店铺'
            );
            $exportExcel_list = array();
            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['attrId'],
                        $v['product_title'],
                        $v['price'],
                        $v['specifications'],
                        $v['statusName'],
                        $v['total_num'],
                        $v['flowing_num'],
                        $v['add_date'],
                        $v['name']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '入库详情');
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

    // 出货详情
    public function Shipment_list()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $exportType = addslashes($this->request->param('exportType')); // 导出
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $mch_name = addslashes(trim($this->request->param('mchName')));
        $product_title = addslashes($this->request->param('productTitle'));
        $startdate = addslashes($this->request->param('startDate'));
        $enddate = addslashes($this->request->param('endDate'));
        $page = addslashes($this->request->param('pageNo'));
        $pagesize = addslashes($this->request->param('pageSize'));

        $array = array('store_id'=>$store_id,'mch_id'=>0,'lang_code'=>$lang_code,'mch_name'=>$mch_name,'product_title'=>$product_title,'startdate'=>$startdate,'enddate'=>$enddate,'page'=>$page,'pagesize'=>$pagesize,'operator_source'=>1);
        $ProductInventory = new ProductInventory();
        $data = $ProductInventory->Shipment_list($array);
        $list = $data['list'];

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '序号',
                1 => '商品名称',
                2 => '商品售价',
                3 => '商品规格',
                4 => '商品状态',
                5 => '商品总库存',
                6 => '剩余库存',
                7 => '店铺',
                8 => '入库时间'
            );
            $exportExcel_list = array();
            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['attrId'],
                        $v['product_title'],
                        $v['price'],
                        $v['specifications'],
                        $v['statusName'],
                        $v['total_num'],
                        $v['num'],
                        $v['name'],
                        $v['add_date']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '出货详情');
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

    // 库存预警
    public function Warning_list()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

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

        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$parameter,'lang_code'=>$lang_code,'mch_name'=>$mch_name,'product_title'=>$product_title,'startdate'=>$startdate,'enddate'=>$enddate,'sortCriteria'=>$sortCriteria,'sort'=>$sort,'page'=>$page,'pagesize'=>$pagesize,'operator_source'=>1);
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
    
    // 库存详情
    public function Details_list()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $pid = addslashes(trim($this->request->param('pid'))); // 商品ID
        $id = addslashes($this->request->param('attrId')); // 属性ID
        $page = addslashes($this->request->param('pageNo'));
        $pagesize = addslashes($this->request->param('pageSize'));

        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'pid'=>$pid,'id'=>$id,'page'=>$page,'pagesize'=>$pagesize,'operator_source'=>1);
        $ProductInventory = new ProductInventory();
        $data = $ProductInventory->Details_list($array);

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

        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'pid'=>$pid,'id'=>$id,'page'=>$page,'pagesize'=>$pagesize,'operator_source'=>1);
        $ProductInventory = new ProductInventory();
        $data = $ProductInventory->Seewarning_list($array);

        return $data;
    }

    // 增加库存
    public function addStock()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $pid = addslashes($this->request->param('pid')); // 商品ID
        $id = addslashes($this->request->param('id')); // 属性ID
        $add_num = addslashes($this->request->param('addNum')); // 增加库存

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'mch_id'=>0,'pid'=>$pid,'id'=>$id,'add_num'=>$add_num,'operator_id'=>$operator_id,'operator'=>$operator,'operator_source'=>1);
        $ProductInventory = new ProductInventory();
        $data = $ProductInventory->addStock($array);

        return;
    }

    // 批量增加库存
    public function BatchAddStock()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $ids = addslashes($this->request->param('ids')); // 属性ID
        $add_num = addslashes($this->request->param('addNum')); // 增加库存

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'mch_id'=>0,'ids'=>$ids,'add_num'=>$add_num,'operator_id'=>$operator_id,'operator'=>$operator,'operator_source'=>1);
        $ProductInventory = new ProductInventory();
        $data = $ProductInventory->BatchAddStock($array);

        return;
    }

    //获取代客下单商品列表
    public function getGoodsConfigureList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $token = $this->request->param('accessId');
        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id']; // 店铺id

        $lang_code = addslashes(trim($this->request->param('language'))); // 语种
        $page = addslashes($this->request->param('pageNo'));
        $pagesize = addslashes($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $cid = $this->request->param('cid');//分类id
        $brand_id = $this->request->param('brandId');//品牌id
        $product_title = $this->request->param('productTitle');//名称
        $total = 0;
        $condition = " b.store_id = '$store_id' and b.mch_id = '$mch_id' and a.recycle = 0 and b.status = 2 and b.recycle = 0 and b.is_presell = 0 and b.commodity_type = 0 ";
        if($lang_code != '')
        {
            $condition .= " and b.lang_code = '$lang_code' ";
        }
        if($cid != '')
        {
            $condition .= " and b.product_class like '%-".$cid."-%' ";
        }
        if($brand_id != '')
        {
            $condition .= " and b.brand_id = '$brand_id' ";

        }
        if($product_title != '')
        {
            $condition .= " and b.product_title like '%$product_title%' ";
        }
        $sql_num = "select ifnull(count(a.id),0) as num from lkt_configure as a left join lkt_product_list as b on a.pid = b.id left join lkt_mch as c on b.mch_id = c.id where $condition ";

        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $goodsList = array();
        if($total > 0)
        {
            $sql = "select a.id as attr_id,a.attribute,a.pid as goodsId,a.img,b.mch_id,a.min_inventory,c.name,a.num,a.price,b.product_number,b.product_title,a.total_num from lkt_configure as a left join lkt_product_list as b on a.pid = b.id left join lkt_mch as c on b.mch_id = c.id where $condition order by b.add_date desc limit $start,$pagesize ";
            $res = Db::query($sql);
            foreach ($res as $key => $value) 
            {
                $res[$key]['imgurl'] = ServerPath::getimgpath($value['img'], $store_id);
                $attribute = unserialize($value['attribute']);
                $specifications = '';
                foreach ($attribute as $ke => $va)
                {
                    if (strpos($ke, '_LKT_') !== false)
                    {
                        $ke = substr($ke, 0, strrpos($ke, "_LKT"));
                        $va = substr($va, 0, strrpos($va, "_LKT"));
                    }
                    $specifications .= $ke . ':' . $va . ',';
                }
                $res[$key]['attribute'] = $specifications;
            }
            $goodsList = $res;
        }
        $message = Lang('Success');
        return output(200, $message,array('total'=>$total,'goodsList'=>$goodsList));
    }

    // 批量上传
    public function uploadAddGoods()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $product_class = addslashes($this->request->param('productClassId')); // 分类ID
        $brand_id = addslashes($this->request->param('brandId')); // 品牌ID
        $freight_id = addslashes($this->request->param('freightId')); // 运费ID
      
        $admin_name = $this->user_list['name'];

        $time = date("Y-m-d H:i:s");
        $Tools = new Tools($store_id, 1);
        if (empty($product_class))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择商品分类！';
            $this->Log($Log_content);
            $message = Lang('product.89');
            return output(109, $message);
        }
        else
        {
            $product_class = $Tools->str_option($product_class);
        }

        if (empty($brand_id))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择商品品牌！';
            $this->Log($Log_content);
            $message = Lang('product.56');
            return output(109, $message);
        }

        if (empty($freight_id))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择运费模板！';
            $this->Log($Log_content);
            $message = Lang('product.90');
            return output(109, $message);
        }

        $filename = $_FILES['image']['tmp_name'];
        $name = $_FILES['image']['name'];
        if (empty ($filename)) 
        {
            $message = Lang('product.91');
            return output(109, $message);
        }

        $handle = fopen($filename,'r');
        $result = $this->input_excel($filename);

        $len_result = count($result);

        if($len_result == 0)
        {
            $message = Lang('product.92');
            return output("50973", $message,null);
        }

        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1,'recycle'=>0])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id'];

        Db::startTrans();
        $p_data = array();
        foreach ($result as $k => $v) 
        {
            $d_date = array();
            $p_data[$k]['product_title'] = '';
            if(isset($v['A']))
            {
                $p_data[$k]['product_title'] = addslashes(trim($v['A']));
            }
            $p_data[$k]['subtitle'] = '';
            if(isset($v['B']))
            {
                $p_data[$k]['subtitle'] = addslashes(trim($v['B']));
            }
            $p_data[$k]['keyword'] = '';
            if(isset($v['C']))
            {
                $p_data[$k]['keyword'] = addslashes(trim($v['C']));
            }
            $attr = "";
            if(isset($v['D']))
            {
                $attr = trim($v['D'],';');
            }
            $cbj = "0.00";
            if(isset($v['E']))
            {
                $cbj = trim($v['E']);
            }
            $yj = "0.00";
            if(isset($v['F']))
            {
                $yj = trim($v['F']);
            }
            $sj = "0.00";
            if(isset($v['G']))
            {
                $sj = trim($v['G']);
            }
            $kucun = "0";
            if(isset($v['H']))
            {
                $kucun = trim($v['H']);
            }
            $unit = "";
            if(isset($v['I']))
            {
                $unit = trim($v['I']);
            }
            if($unit == '')
            {
                Db::rollback();
                $text = "第" . $k . "行 单位不能为空！";
                $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                $res_log = Db::name('file_delivery')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                    $this->Log($Log_content);
                }
                $message = Lang('Success');
                return output(200, $message);
            }
            $p_data[$k]['volume'] = "0";
            if(isset($v['J']))
            {
                $p_data[$k]['volume'] = $v['J'];
            }
            $p_data[$k]['scan'] = '';
            $scan = '';
            if(isset($v['K']))
            {
                $p_data[$k]['scan'] = trim($v['K']);
                $scan = trim($v['K']);
            }
            $p_data[$k]['weight'] = "0";
            if(isset($v['L']))
            {
                $p_data[$k]['weight'] = $v['L'];
            }
            $p_data[$k]['min_inventory'] = 0;
            $min_inventory =0;
            if(isset($v['M']))
            {
                $p_data[$k]['min_inventory'] = $v['M'];
                $min_inventory = $v['M'];
            }
            $p_data[$k]['content'] = "";
            if(isset($v['N']))
            {
                $p_data[$k]['content'] = $v['N'];
            }
            $p_data[$k]['cover_map'] = "";
            if(isset($v['O']))
            {
                $p_data[$k]['cover_map'] = $v['O'];
            }
            if($p_data[$k]['cover_map'] == '')
            {
                Db::rollback();
                $text = "第" . $k . "行 属性图不能为空！";
                $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                $res_log = Db::name('file_delivery')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                    $this->Log($Log_content);
                }
                $message = Lang('Success');
                return output(200, $message);
            }
            $img = $v['P'];
            $p_data[$k]['imgurl'] = '';
            if($v['Q'] == '')
            {
                Db::rollback();
                $text = "第" . $k . "行 缺少商品主图！";
                $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                $res_log = Db::name('file_delivery')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                    $this->Log($Log_content);
                }
                $message = Lang('Success');
                return output(200, $message);
            }
            else
            {
                $imgurls = explode(',',$v['Q']);
                $p_data[$k]['imgurl'] = preg_replace('/.*\//', '', $imgurls['0']);
                unset($imgurls[0]);
            }

            if (count($imgurls) > 4)
            {
                Db::rollback();
                $text = "第" . $k . "行 产品展示图数量超出限制！";
                $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                $res_log = Db::name('file_delivery')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                    $this->Log($Log_content);
                }
                $message = Lang('Success');
                return output(200, $message);
            }

            $p_data[$k]['imgurl_list'] = $imgurls;

            if(!is_numeric($cbj))
            {
                Db::rollback();
                $text = "第" . $k . "行 成本价不为数字！";
                $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                $res_log = Db::name('file_delivery')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 成本价不为数字！';
                    $this->Log($Log_content);
                }
                $message = Lang('Success');
                return output(200, $message);
            }
            if(!is_numeric($yj))
            {
                Db::rollback();
                $text = "第" . $k . "行 原价不为数字！";
                $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                $res_log = Db::name('file_delivery')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 原价不为数字！';
                    $this->Log($Log_content);
                }
                $message = Lang('Success');
                return output(200, $message);
            }
            if(!is_numeric($sj))
            {
                Db::rollback();
                $text = "第" . $k . "行 售价不为数字！";
                $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                $res_log = Db::name('file_delivery')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 售价不为数字！';
                    $this->Log($Log_content);
                }
                $message = Lang('Success');
                return output(200, $message);
            }
            if(!is_numeric($kucun))
            {
                Db::rollback();
                $text = "第" . $k . "行 库存不为数字！";
                $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                $res_log = Db::name('file_delivery')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 库存不为数字！';
                    $this->Log($Log_content);
                }
                $message = Lang('Success');
                return output(200, $message);
            }

            if ($cbj >= $sj)
            {
                Db::rollback();
                $text = "第" . $k . "行 成本价不能大于售价！";
                $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                $res_log = Db::name('file_delivery')->insert($data);
                if($res_log < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 成本价不能大于售价！';
                    $this->Log($Log_content);
                }
                $message = Lang('Success');
                return output(200, $message);
            }

            $p_data[$k]['initial'] = serialize(array('cbj'=>$cbj,'yj'=>$yj,'sj'=>$sj,'unit'=>$unit,'kucun'=>$kucun)) ;
            $p_data[$k]['product_class'] = $product_class;
            $p_data[$k]['brand_id'] = $brand_id;
            $p_data[$k]['freight'] = $freight_id;
            $p_data[$k]['store_id'] = $store_id;

            $attr_list1 = explode(';',$attr);

            $Attribute_Name_list = array(); // 属性名数组
            $Attribute_list = array(); // 属性数组
            foreach($attr_list1 as $k1 => $v1)
            {
                $attr_list2 = explode(':',$v1);
                if(!in_array($attr_list2[0],$Attribute_Name_list))
                { // 属性名 不存在 属性名数组里
                    $Attribute_Name_list[] = $attr_list2[0];
                    $Attribute_list[$attr_list2[0]][] = $attr_list2[1];
                }
                else
                { // 属性名 存在 属性名数组里
                    if(!in_array($attr_list2[1],$Attribute_list[$attr_list2[0]]))
                    {
                        $Attribute_list[$attr_list2[0]][] = $attr_list2[1];
                    }
                }
            }

            $Attribute_list1 = array();
            // 把属性名拼入属性值里
            foreach($Attribute_list as $k_0 => $v_0)
            {
                foreach($v_0 as $k_1 => $v_1)
                {
                    $Attribute_list1[$k_0][$k_1] = $k_0 . ':' . $v_1;
                }
            }

            $Attribute_list2 = array();
            // 去掉键名
            foreach($Attribute_list1 as $k_2 => $v_2)
            {
                $Attribute_list2[] = $v_2;
            }
            
            // 把规格组合
            $Attribute_list3 = array();
            for($i = 0;$i < count($Attribute_list2) - 1;$i++)
            {
                if($i === 0)
                {
                    $Attribute_list3 = $Attribute_list2[0];
                }
                
                $ret = array();
                foreach($Attribute_list3 as $k_3 => $v_3)
                {
                    foreach($Attribute_list2[$i+1] as $k_4 => $v_4)
                    {
                        $ret[] = $v_3 . ';' . $v_4;
                    }
                    $Attribute_list3 = $ret;
                }
            }
            $d_date = array();
            foreach($Attribute_list3 as $k_5 => $v_5)
            {
                $attr_list = array();
                $Attribute_list4 = array_filter(explode(';',$v_5));
                foreach ($Attribute_list4 as $k_6 => $v_6) 
                {
                    $attr_list[$k_6]['attr_id'] = '';
                    $attr = explode(':',$v_6);
                    $attr_list[$k_6]['attr_name'] = $attr[1];
                    $attr_list[$k_6]['attr_group_name'] = $attr[0];
                }
                $d_date[$k_5]['attr_list'] = $attr_list;
                $d_date[$k_5]['costprice'] = $cbj;
                $d_date[$k_5]['price'] = $sj;
                $d_date[$k_5]['yprice'] = $yj;
                $d_date[$k_5]['total_num'] = $kucun;
                $d_date[$k_5]['num'] = $kucun;
                $d_date[$k_5]['unit'] = $unit;
                $d_date[$k_5]['bar_code'] = $scan;
                $d_date[$k_5]['min_inventory'] = $min_inventory;
                $d_date[$k_5]['img'] = $img;
            }
            
            $p_data[$k]['c_list'] = $d_date;
        }

        foreach ($p_data as $key => $value)
        {
            $product_title = $value['product_title'];

            //处理属性
            $attr = $value['c_list'];
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
                            $attr_name_id = $Tools->add_attribute($store_id, $attr_group_name_id, $va['attr_name'], 2, $admin_name, $foreach_num,'平台'); // 添加属性值
                        }
                    }
                    else
                    { // 当属性名称ID为0，SKU表里没有数据
                        $attr_group_name_id = $Tools->add_attribute($store_id, 0, $va['attr_group_name'], 1, $admin_name, 0,'平台'); // 添加属性名
                        $attr_name_id = $Tools->add_attribute($store_id, $attr_group_name_id, $va['attr_name'], 2, $admin_name, $foreach_num,'平台'); // 添加属性值
                    }
                    $attr[$k]['attr_list'][$ke]['attr_group_name'] = $va['attr_group_name'] . '_LKT_' . $attr_group_name_id; // 拼接属性名
                    $attr[$k]['attr_list'][$ke]['attr_name'] = $va['attr_name'] . '_LKT_' . $attr_name_id; // 拼接属性值
                }
            }
            $z_num = 0;
            $attributes = array();
            foreach ($attr as $k => $v) 
            {
                $attr_list = $v['attr_list'];
                $attr_list_arr = array();
                $attr_list_srt = '';
                foreach ($attr_list as $kk => $va)
                {
                    $attr_list_arr[$va['attr_group_name']] = $va['attr_name'];
                    $attr_list_srt .= substr($va['attr_group_name'], 0, strpos($va['attr_group_name'], '_')) . '-' . substr($va['attr_name'], 0, strpos($va['attr_name'], '_'));
                }

                $z_num += $v['num'];
                $v['total_num'] = $v['num'];

                $v['attribute'] = serialize($attr_list_arr);
                $v = Tools::array_key_remove($v, 'attr_list');
                $attributes[$k]['costprice'] = $v['costprice'];
                $attributes[$k]['yprice'] = $v['yprice'];
                $attributes[$k]['price'] = $v['price'];
                $attributes[$k]['num'] = $v['num'];
                $attributes[$k]['total_num'] = $v['num'];
                $attributes[$k]['unit'] = $v['unit'];
                $attributes[$k]['bar_code'] = $v['bar_code'];
                $attributes[$k]['min_inventory'] = $v['min_inventory'];
                $attributes[$k]['img'] = $v['img'];
                $attributes[$k]['attribute'] = $v['attribute'];
            }

            $sort_r = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0])->field('MAX(sort) as sort')->select()->toArray();
            $sort = $sort_r[0]['sort'] + 1;

            $r_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('name')->select()->toArray();
            $shop_name = $r_mch[0]['name'];

            $subtitle = $value['subtitle'];
            $keyword = $value['keyword'];
            $volume = $value['volume'];
            $scan = $value['scan'];
            $weight = $value['weight'];
            $min_inventory = $value['min_inventory'];
            $imgurl = $value['imgurl'];
            $cover_map = $value['cover_map'];
            $product_class = $value['product_class'];
            $content = $value['content'];
            $num = $z_num;
            $brand_id = $value['brand_id'];
            $freight = $value['freight'];
            $initial = $value['initial'];
            $imgurl_list = $value['imgurl_list'];

            $data = array('store_id'=>$store_id,'commodity_type'=>0,'product_title'=>$product_title,'subtitle'=>$subtitle,'scan'=>$scan,'product_number'=>'','product_class'=>$product_class,'brand_id'=>$brand_id,'keyword'=>$keyword,'weight'=>$weight,'weight_unit'=>'kg','imgurl'=>$imgurl,'sort'=>$sort,'content'=>$content,'num'=>$num,'min_inventory'=>$min_inventory,'add_date'=>$time,'freight'=>$freight,'active'=>1,'mch_id'=>$mch_id,'mch_status'=>2,'initial'=>$initial,'publisher'=>$admin_name,'cover_map'=>$cover_map,'volume'=>$volume,'richList'=>'','show_adr'=>',3,');
            $res = Db::name('product_list')->insertGetId($data);
            if($res > 1)
            {
                if($imgurl_list != array())
                {
                    foreach ($imgurl_list as $k_list => $v_list)
                    {
                        $imgsURL_name = preg_replace('/.*\//', '', $v_list);

                        $data_img = array('product_url'=>$imgsURL_name,'product_id'=>$res,'add_date'=>$time,'seller_id'=>$admin_name);
                        $r = Db::name('product_img')->save($data_img);
                        if ($r < 1)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 产品展示图上传失败！参数:'. json_encode($data_img);
                            $this->Log($Log_content);

                            Db::rollback();
                            $text = "第" . $key . "行 产品展示图上传失败！";
                            $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                            $res_log = Db::name('file_delivery')->insert($data);
                            if($res_log < 1)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                                $this->Log($Log_content);
                            }
                            $message = Lang('Success');
                            return output(200, $message);
                        }
                    }
                }

                foreach ($attributes as $ke => $va)
                {
                    $va['pid'] = $res;
                    $va['total_num'] = $va['num'];
                    $total_num = $va['num'];
                    $va['min_inventory'] = $min_inventory;
                    $va['ctime'] = $time;

                    $r_attribute = Db::name('configure')->insertGetId($va);
                    if ($r_attribute < 1)
                    {
                        $attributes1 = json_encode($va);
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 属性数据添加失败！参数：' . $attributes1;
                        $this->Log($Log_content);
                        
                        Db::rollback();
                        $text = "第" . $key . "行 规格数据有误！";
                        $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                        $res_log = Db::name('file_delivery')->insert($data);
                        if($res_log < 1)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                            $this->Log($Log_content);
                        }
                        $message = Lang('Success');
                        return output(200, $message);
                    }

                    $num = $va['num'];
                    // 在库存记录表里，添加一条入库信息
                    $content = $admin_name . '增加商品总库存' . $num;
                    $sql_stock0 = array('store_id'=>$store_id,'product_id'=>$res,'attribute_id'=>$r_attribute,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>0,'add_date'=>$time,'content'=>$content);
                    $r_stock0 = Db::name('stock')->insert($sql_stock0);
                    if($r_stock0 < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 库存记录添加失败！参数：'.json_encode($sql_stock0);
                        $this->Log($Log_content);
                        Db::rollback();
                        $text = "第" . $key . "行 写入库存失败！";
                        $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                        $res_log = Db::name('file_delivery')->insert($data);
                        if($res_log < 1)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                            $this->Log($Log_content);
                        }
                        $message = Lang('Success');
                        return output(200, $message);
                    }
                    if ($min_inventory >= $num)
                    { // 当属性库存低于等于预警值
                        $content1 = '预警';
                        // 在库存记录表里，添加一条预警信息
                        $sql_stock1 = array('store_id'=>$store_id,'product_id'=>$res,'attribute_id'=>$r_attribute,'total_num'=>$total_num,'flowing_num'=>$num,'type'=>2,'add_date'=>$time,'content'=>$content1);
                        $r_stock1 = Db::name('stock')->insert($sql_stock1);
                        if($r_stock1 < 1)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 库存记录添加失败！参数：'.json_encode($sql_stock1);
                            $this->Log($Log_content);
                            Db::rollback();
                            $text = "第" . $key . "行 写入库存失败！";
                            $data = array('store_id'=>$store_id,'name'=>$name,'status'=>0,'text'=>$text,'mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
                            $res_log = Db::name('file_delivery')->insert($data);
                            if($res_log < 1)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 记录写入失败！';
                                $this->Log($Log_content);
                            }
                            $message = Lang('Success');
                            return output(200, $message);
                        }
                    }
                }
            }
        }

        $data = array('store_id'=>$store_id,'name'=>$name,'status'=>1,'text'=>'','mch_id'=>$mch_id,'add_date'=>$time,'type'=>0,'order_num'=>$len_result);
        $res_log = Db::name('file_delivery')->insert($data);
        Db::commit();
        $message = Lang('product.94');
        return output(200, $message);
    }

    public function input_excel($filename)
    {
        $out = array ();
        $n = 0;
        $reader = IOFactory::createReader('Xlsx'); // 先创建一个Reader对象
        $spreadsheet = $reader->load($filename); // 载入文件到Spreadsheet对象中

        $worksheet = $spreadsheet->getActiveSheet(); // 获取活动工作表

        $highestRow = $worksheet->getHighestRow(); // 获取最大行数
        $highestColumn = $worksheet->getHighestColumn(); // 获取最大列数

        // 从第2行开始遍历每一行
        for ($row = 2; $row <= $highestRow; ++$row) {
            // 从A列开始遍历每一列
            for ($col = 'A'; $col <= $highestColumn; ++$col) {
                $cell = $worksheet->getCell($col . $row); // 获取单元格对象
                $value = $cell->getValue(); // 获取单元格值
                if ($value) 
                {
                    $out[$row][$col] = $value;
                }
            }
        }
        return $out;
    }

    // 批量上传记录
    public function uploadRecordList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $key = addslashes($this->request->param('key')); // 文件名称
        $status = addslashes($this->request->param('status')); // 文件状态 1=成功 0=失败
        $startdate = addslashes($this->request->param('startDate')); // 开始时间
        $enddate = addslashes($this->request->param('endDate')); // 结束时间
        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页多少条数据
        
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? $pagesize:'10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $admin_name = $this->user_list['name'];

        $condition = " store_id = '$store_id' and type = 0 ";
        if($key != '')
        {
            $key = Tools::FuzzyQueryConcatenation($key);
            $condition .= " and name like $tel ";
        }
        if($status != '')
        {
            $condition .= " and status = '$status'";
        }
        if ($startdate != '')
        {
            $condition .= "and add_date >= '$startdate' ";
        }
        if ($enddate != '')
        {
            $condition .= "and add_date <= '$enddate' ";
        }

        $total = 0;
        $list = array();

        $sql_num = "select ifnull(count(id),0) as num from lkt_file_delivery where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        if($total > 0)
        {
            $sql = "select id,name,status,text,add_date,mch_id,type from lkt_file_delivery where $condition order by add_date desc limit $start,$pagesize";
            $res = Db::query($sql);
            if($res)
            {
                $list = $res;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 删除批量上传记录
    public function delUploadRecord()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('id')); // 上传记录ID
        
        $res = Db::table('lkt_file_delivery')->where('id',$id)->delete();
        if($res < 1)
        {
            $message = Lang('操作失败，请重试！');
            return output(400, $message);
        }

        $message = Lang('Success');
        return output(200, $message);
    }

    // 获取语言
    public function getLangs()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        $language = addslashes(trim($this->request->param('language'))); // 语言

        $id = addslashes($this->request->param('id')); // 上传记录ID

        $store_langs = "";
        $sql0 = "select store_langs from lkt_customer where id = '$store_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $store_langs = $r0[0]['store_langs'];
        }

        if($store_langs != '')
        {
            $sql = "select lang_code,lang_name from lkt_lang where is_show = 1 and recycle = 1 and id in ($store_langs)";
        }
        else
        {
            $sql = "select lang_code,lang_name from lkt_lang where is_show = 1 and recycle = 1 ";
        }
        $r = Db::query($sql);
        if($r)
        {
            $data = $r;
        }

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/goods.log",$Log_content);
        return;
    }
}
