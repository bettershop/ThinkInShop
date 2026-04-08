<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Plugin\Plugin;
use app\common\LaiKeLogUtils;

use app\admin\model\AdminCgGroupModel;
use app\admin\model\UserAddressModel;
/**
 * 功能：用户收货地址类
 * 修改人：PJY
 */
class Address extends BaseController
{
	// 地址管理
    public function index()
    {
        $store_id = $this->request->param('store_id');
        $product = $this->request->post('product');// 参数
        $user_id = $this->user_list['user_id'];

        $list = array();
        $list0 = array();
        $no_delivery_list = array(); // 不配送地区
        $no_delivery_address_id_list = array(); // 用户存在不配送地区的地址ID
        if($product && $product != '' && $product != 'undefined')
        {
            $product_list = explode(',',$product);
            foreach($product_list as $k => $v)
            {
                $sql0 = "select a.no_delivery from lkt_freight as a left join lkt_product_list as b on a.id = b.freight where a.store_id = '$store_id' and b.id = '$v' ";
                $r0 = Db::query($sql0);
                if($r0)
                {
                    $no_delivery = $r0[0]['no_delivery']; // 不配送
                    if($no_delivery != '')
                    {
                        $no_delivery_list = json_decode($no_delivery,true);
                    }
                }
            }
        }

        if($no_delivery_list != array())
        { // 循环不配送地址
            foreach($no_delivery_list as $k => $v)
            {
                // 根据省的名称，查询该用户的地址ID
                $r2 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->field('id,sheng,city,quyu')->select()->toArray();
                if($r2)
                {
                    foreach($r2 as $k2 => $v2)
                    {
                        $str = $v2['sheng'] . '-' . $v2['city'] . '-' . $v2['quyu'];
                        if(in_array($str,$no_delivery_list))
                        {
                            $no_delivery_address_id_list[] = $v2['id'];
                        }
                    }
                }
            }
        }

        // 根据用户id,查询地址表
        $r = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->order('is_default','desc')->select()->toArray();
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $v['province'] = $v['sheng'];
                $v['city'] = $v['city'];
                $v['county'] = $v['quyu'];
                if(in_array($v['id'], $no_delivery_address_id_list))
                {
                    $list0[] = $v;
                }
                else
                {
                    $list[] = $v;
                }
            }
            $message = Lang('Success');
            $data = array('adds' => $list,'adds0' => $list0);
            return output('200', $message,$data);
        }
        else
        {
            $message = Lang('Success');
            $data = array('adds' => $list,'adds0' => $list0);
            return output('200', $message,$data);
        }

        return;
    }

    // 设置默认
    public function set_default()
    {
        $store_id = $this->request->param('store_id');
        $addr_id = $this->request->post('addr_id');// 参数
        $user_id = $this->user_list['user_id'];
        //事务开启
        Db::startTrans();
        try
        {	
        	//清空默认地址数据
            UserAddressModel::update(['is_default'=>0],['store_id'=>$store_id,'uid'=>$user_id]);
            //修改默认地址
            $UserAddressModel = UserAddressModel::find($addr_id);
			$UserAddressModel->is_default     = 1;
			$UserAddressModel->save();
	        Db::commit();
	        $message = Lang('Success');
	        return output(200, $message);
        }
        catch (\Exception $e) 
        {
            // 回滚事务
            Db::rollback();
            $Log_content = $e->getMessage();
            $this->Log($Log_content);
            $message = Lang('Busy network');
            return output(ERROR_CODE_CZSB,$message);
        }
        return;
    }

    // 删除地址
    public function del_adds()
    {
        $store_id = $this->request->param('store_id');
        $addr_id = $this->request->post('addr_id');// 参数
        $user_id = $this->user_list['user_id'];
        //事务开启
        Db::startTrans();
        try
        {	
	        $r01 = UserAddressModel::where(['store_id'=>$store_id,'id'=>$addr_id])->field('is_default')->select()->toArray();
	        if (!empty($r01))
	        {	
	            $is_default = $r01[0]['is_default'];
	            //删除指定地址
	            $UserAddressModel = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id,'id'=>$addr_id])->find();
				$r = $UserAddressModel->delete();
	            // 根据用户id,查询地址表
	            if ($r)
	            {
	                if ($is_default == 1)
	                {	//默认
	                    $UserAddressModel = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->order('id', 'desc')->find();
                        if($UserAddressModel)
                        {
                            $UserAddressModel->is_default = 1;
                            $UserAddressModel->save();
                        }
	                }
	            }
	        }
	        Db::commit();
	        $message = Lang('Success');
	        return output(200, $message);
        }
        catch (\Exception $e) 
        {
            // 回滚事务
            Db::rollback();
            $Log_content = $e->getMessage();
            $this->Log($Log_content);
            $message = Lang('Busy network');
            return output(ERROR_CODE_SCSB,$message);
        }
        return;
    }

    // 修改地址
    public function up_adds()
    {
        $store_id = $this->request->param('store_id');
        $user_id = $this->user_list['user_id'];
        
        // 获取信息
        $addr_id = $this->request->param('addr_id'); // 地址id
        $user_name = $this->request->param('user_name'); //  联系人
        $cpc = $this->request->param('cpc'); // 区号
        $mobile = $this->request->param('mobile'); // 联系电话
        $place = $this->request->param('place'); // 地址
        $province = $this->request->param('province'); // 省
        $city = $this->request->param('city'); // 市
        $county = $this->request->param('county'); // 县
        $address = $this->request->param('address'); // 详细地址
        $code = $this->request->param('code'); // 邮政编码
        $is_default1 = $this->request->param('is_default'); // 地址状态

        $r = UserAddressModel::where(['store_id'=>$store_id,'id'=>$addr_id])->select()->toArray();
        $uid = $r[0]['uid'];//用户ID
        $is_default = $r[0]['is_default'];//是否默认地址

        if($code == '')
        {
            $code = 0;
        }
        if($cpc == '86' || $cpc == '852')
        {
            $place_array = explode('-', $place);

            $province = $place_array['0']; // 省
            $city = $place_array['1']; // 市
            $county = $place_array['2']; //县
        }

        $array_address = array('cpc'=>$cpc,'sheng'=>$province,'shi'=>$city,'xian'=>$county,'address'=>$address,'code'=>$code);
        $address_xq = PC_Tools::address_translation($array_address);

        //事务开启
        Db::startTrans();
        try
        {	
            if ($is_default1 == 1)
            {  
                // 当修改地址状态为默认时
                //清空默认地址数据
                UserAddressModel::update(['is_default'=>0],['store_id'=>$store_id,'uid'=>$user_id]);
                //修改地址
                $UserAddressModel = UserAddressModel::find($addr_id);
                $UserAddressModel->name     = $user_name;
                $UserAddressModel->cpc     = $cpc;
                $UserAddressModel->tel     = $mobile;
                $UserAddressModel->sheng     = $province;
                $UserAddressModel->city     = $city;
                $UserAddressModel->quyu     = $county;
                $UserAddressModel->address     = $address;
                $UserAddressModel->address_xq     = $address_xq;
                $UserAddressModel->code     = $code;
                $UserAddressModel->uid     = $uid;
                $UserAddressModel->is_default     = $is_default1;
                $UserAddressModel->save();
            }
            else
            { // 当修改地址状态不为默认时
                if ($is_default == 1)
                { 	// 该地址原来默认，而现在不为默认
                    //清空默认地址数据
                    UserAddressModel::update(['is_default'=>0],['store_id'=>$store_id,'uid'=>$user_id]);
                    //修改地址
                    $UserAddressModel = UserAddressModel::find($addr_id);
                    $UserAddressModel->name     = $user_name;
                    $UserAddressModel->cpc     = $cpc;
                    $UserAddressModel->tel     = $mobile;
                    $UserAddressModel->sheng     = $province;
                    $UserAddressModel->city     = $city;
                    $UserAddressModel->quyu     = $county;
                    $UserAddressModel->address     = $address;
                    $UserAddressModel->address_xq     = $address_xq;
                    $UserAddressModel->code     = $code;
                    $UserAddressModel->uid     = $uid;
                    $UserAddressModel->is_default     = 1;
                    $UserAddressModel->save();
                }
                else
                { // 该地址原来不为默认，而现在也不为默认
                    //修改地址
                    $UserAddressModel = UserAddressModel::find($addr_id);
                    $UserAddressModel->name     = $user_name;
                    $UserAddressModel->cpc     = $cpc;
                    $UserAddressModel->tel     = $mobile;
                    $UserAddressModel->sheng     = $province;
                    $UserAddressModel->city     = $city;
                    $UserAddressModel->quyu     = $county;
                    $UserAddressModel->address     = $address;
                    $UserAddressModel->address_xq     = $address_xq;
                    $UserAddressModel->code     = $code;
                    $UserAddressModel->uid     = $uid;
                    $UserAddressModel->is_default     = $is_default1;
                    $UserAddressModel->save();
                }
            }
            Db::commit();
            $message = Lang('Success');
            return output(200, $message);
        }
        catch (\Exception $e) 
        {
            // 回滚事务
            Db::rollback();
            $Log_content = $e->getMessage();
            $this->Log($Log_content);
            $message = Lang('Busy network');
            return output(ERROR_CODE_XGSB,$message);
        } 

        return;
    }

    // 修改地址页面跳转显示
    public function up_addsindex()
    {
        $store_id = $this->request->param('store_id');
        $addr_id = $this->request->post('addr_id');// 参数
        $user_id = $this->user_list['user_id'];
        $r = UserAddressModel::where(['store_id'=>$store_id,'id'=>$addr_id])->select()->toArray();//查询修改前的详细地址
        $province = $r[0]['sheng'];//省
        $city = $r[0]['city'];//市
        $county = $r[0]['quyu'];//县
        $r[0]['province'] = $r[0]['sheng'];
        $r[0]['city'] = $r[0]['city'];
        $r[0]['county'] = $r[0]['quyu'];

        $data = array('address' => $r[0], 'province' => $province, 'city_id' => $city, 'county' => $county);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    //显示省份
    public function AddressManagement()
    {
        // 查询省
        $rr = AdminCgGroupModel::where('district_pid',0)->select()->toArray();
        foreach ($rr as $k => $v)
        {
            $result = array();
            $result['id'] = $v['id']; // 编号
            $result['district_name'] = $v['district_name']; // 省名
            $result['district_pid'] = $v['district_pid']; // 类型
            $sheng[] = $result;
            unset($result); // 销毁指定变量
        }
        $message = Lang('Success');
        return output(200, $message,$sheng);
    }

    // 根据省查询市
    public function getCityArr()
    {
        $GroupID = $this->request->post('GroupID'); // 省ID

        if (empty($GroupID))
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }

        if ($GroupID == '')
        {
            $GroupID = 2;
        }
        else
        {
            $GroupID = $GroupID;
        }

        // 根据省查询市
        $r = AdminCgGroupModel::where('district_pid',$GroupID)->select()->toArray();
        foreach ($r as $k => $v)
        {
            $result = array();
            $result['id'] = $v['id']; // 编号
            $result['district_name'] = $v['district_name']; // 市名
            $result['district_pid'] = $v['district_pid']; // 类型
            $shi[] = $result;
            unset($result); // 销毁指定变量
        }
        $message = Lang('Success');
        $resArr = array('shi' => $shi);
        return output(200, $message,$resArr);

    }

    // 根据省市获取县
    public function getCountyInfo()
    {
        $GroupID = $this->request->post('GroupID'); // 市ID

        if (empty($GroupID))
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }

        // 根据市查询县
        $r = AdminCgGroupModel::where('district_pid',$GroupID)->select()->toArray();
        foreach ($r as $k => $v)
        {
            $result = array();
            $result['id'] = $v['id']; // 编号
            $result['district_name'] = $v['district_name']; // 市名
            $result['district_pid'] = $v['district_pid']; // 类型
            $xian[] = $result;
            unset($result); // 销毁指定变量
        }
        $message = Lang('Success');
        $resArr = array( 'xian' => $xian);
        return output(109, $message,$resArr);
    }

    // 添加地址点击保存
    public function SaveAddress()
    {
        $store_id = $this->request->param('store_id');
        $user_id = $this->user_list['user_id'];
        $user_name = $this->request->param('user_name'); //  联系人
        $cpc = $this->request->param('cpc'); // 区号
        $mobile = $this->request->param('mobile'); // 联系电话
        $place = $this->request->param('place'); // 地址
        $province = $this->request->param('province'); // 省
        $city = $this->request->param('city'); // 市
        $county = $this->request->param('county'); // 县
        $address = $this->request->param('address'); // 详细地址
        $code = $this->request->param('code'); // 邮政编码
        $is_default = $this->request->param('is_default'); // 地址状态

        if($code == '')
        {
            $code = 0;
        }
        if($cpc == '86' || $cpc == '852')
        {
            $place_array = explode('-', $place);

            $province = $place_array['0']; // 省
            $city = $place_array['1']; // 市
            $county = $place_array['2']; //县
        }

        $array_address = array('cpc'=>$cpc,'sheng'=>$province,'shi'=>$city,'xian'=>$county,'address'=>$address,'code'=>$code);
        $address_xq = PC_Tools::address_translation($array_address);

        //事务开启
        Db::startTrans();
        try
        {	
            // 根据微信id,查询会员id
            $r0 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->select()->toArray();
            if ($r0)
            {
                if ($is_default == 1)
                {
                    //清空默认地址数据
                    UserAddressModel::update(['is_default'=>0],['store_id'=>$store_id,'uid'=>$user_id]);
                    //添加数据
                    $UserAddressModel           = new UserAddressModel;
                    $UserAddressModel->store_id     = $store_id;
                    $UserAddressModel->name     = $user_name;
                    $UserAddressModel->cpc     = $cpc;
                    $UserAddressModel->tel     = $mobile;
                    $UserAddressModel->sheng     = $province;
                    $UserAddressModel->city     = $city;
                    $UserAddressModel->quyu     = $county;
                    $UserAddressModel->address     = $address;
                    $UserAddressModel->address_xq     = $address_xq;
                    $UserAddressModel->code     = $code;
                    $UserAddressModel->uid     = $user_id;
                    $UserAddressModel->is_default     = 1;
                    $UserAddressModel->save();
                }
                else
                {
                    //添加数据
                    $UserAddressModel           = new UserAddressModel;
                    $UserAddressModel->store_id     = $store_id;
                    $UserAddressModel->name     = $user_name;
                    $UserAddressModel->cpc     = $cpc;
                    $UserAddressModel->tel     = $mobile;
                    $UserAddressModel->sheng     = $province;
                    $UserAddressModel->city     = $city;
                    $UserAddressModel->quyu     = $county;
                    $UserAddressModel->address     = $address;
                    $UserAddressModel->address_xq     = $address_xq;
                    $UserAddressModel->code     = $code;
                    $UserAddressModel->uid     = $user_id;
                    $UserAddressModel->is_default     = 0;
                    $UserAddressModel->save();
                }
            }
            else
            {
                //添加数据
                $UserAddressModel           = new UserAddressModel;
                $UserAddressModel->store_id     = $store_id;
                $UserAddressModel->name     = $user_name;
                $UserAddressModel->cpc     = $cpc;
                $UserAddressModel->tel     = $mobile;
                $UserAddressModel->sheng     = $province;
                $UserAddressModel->city     = $city;
                $UserAddressModel->quyu     = $county;
                $UserAddressModel->address     = $address;
                $UserAddressModel->address_xq     = $address_xq;
                $UserAddressModel->code     = $code;
                $UserAddressModel->uid     = $user_id;
                $UserAddressModel->is_default     = 1;
                $UserAddressModel->save();
            }
            Db::commit();
            $message = Lang('Success');
            return output(200, $message);
        }
        catch (\Exception $e) 
        {
            // 回滚事务
            Db::rollback();
            $Log_content = $e->getMessage();
            $this->Log($Log_content);
            $message = Lang('Busy network');
            return output(ERROR_CODE_TJSB,$message);
        }     
    
        return;
    }
    
    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/address.log",$Log_content);
        return;
    }
}