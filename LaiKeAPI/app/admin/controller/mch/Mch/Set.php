<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;
use app\common\Product;
use app\common\Jurisdiction;
use app\common\Plugin\MchPublicMethod;

use app\admin\model\MchModel;
use app\admin\model\MchClassModel;
use app\admin\model\AdminCgGroupModel;
use app\admin\model\UserModel;
use app\admin\model\BannerModel;
use app\admin\model\ProductListModel;
use app\admin\model\ProductClassModel;
use app\admin\model\PrintSetupModel;
use app\admin\model\JumpPathModel;

/**
 * 功能：店铺设置
 * 修改人：DHB
 */
class Set extends BaseController
{
    // 店铺信息
    public function Index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $res = cache($access_id);
        $user_id = $res['user_id'];

        $field = "id,store_id,user_id,name,shop_information,shop_range,realname,ID_number as iD_number,cpc,tel,sheng,shi,xian,address,logo,shop_nature,business_license,add_time,review_status,integral_money,account_money,collection_num,is_open,is_lock,roomid,old_roomid,longitude,latitude,cashable_money,recovery,is_invoice,cid,poster_img,head_img,business_hours,is_self_delivery";
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field($field)->select()->toArray();
        if ($r_mch)
        {
            $r_mch[0]['logo'] = ServerPath::getimgpath($r_mch[0]['logo'], $store_id); //图片
            $r_mch[0]['poster_img'] = ServerPath::getimgpath($r_mch[0]['poster_img'], $store_id); // 店铺新增宣传图
            $r_mch[0]['head_img'] = ServerPath::getimgpath($r_mch[0]['head_img'], $store_id); // 店铺头像
            $cid = $r_mch[0]['cid'];
            $sheng = $r_mch[0]['sheng'];
            $shi = $r_mch[0]['shi'];
            $xian = $r_mch[0]['xian'];

            $r_mch[0]['className'] = '';
            if($cid != 0)
            {
                $r_class = MchClassModel::where(['id'=>$cid])->field('name')->select()->toArray();
                $r_mch[0]['className'] = $r_class[0]['name'];
            }

            $r_mch[0]['sheng_id'] = 0;
            $r_mch[0]['shi_id'] = 0;
            $r_mch[0]['xian_id'] = 0;
            if($sheng != 0)
            {
                $r_sheng = AdminCgGroupModel::where(['district_name'=>$sheng])->field('id')->select()->toArray();
                $r_mch[0]['sheng_id'] = $r_sheng[0]['id'];
            }

            if($sheng != 0)
            {
                $r_shi = AdminCgGroupModel::where(['district_name'=>$shi])->field('id')->select()->toArray();
                $r_mch[0]['shi_id'] = $r_shi[0]['id'];
            }

            if($sheng != 0)
            {
                $r_xian = AdminCgGroupModel::where(['district_name'=>$xian])->field('id')->select()->toArray();
                $r_mch[0]['xian_id'] = $r_xian[0]['id'];
            }

            $r_user = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();
            $r_mch[0]['user'] = $r_user[0];

            $data = array('res'=>$r_mch[0]);
            $message = Lang('Success');
            return output('200', $message,$data);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Parameter error');
            return output(109, $message);
        }
    }
    
    // 获取店铺分类
    public function MchClassList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $res = cache($access_id);
        $user_id = $res['user_id'];

        $total = 0;
        $list = array();
        
        $sql = array('store_id'=>$store_id,'recycle'=>0);
        $r0 = MchClassModel::where($sql)->field('count(id) as total')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $r = MchClassModel::where($sql)->order('sort','desc')->select()->toArray();
        if ($r)
        {
            $maxSort = $r[0]['sort'];
            foreach($r as $k => $v)
            {
                $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
                $list[] = $v;
            }
        }

        $data = array('list'=>$list);
        $message = Lang('Success');
        return output('200', $message,$data);
    }

    // 编辑店铺信息
    public function Edit()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('id')); // 店铺ID
        $logo = addslashes($this->request->param('logo')); // 店铺Logo
        $poster_img = addslashes($this->request->param('posterImg')); // 店铺Logo
        $head_img = addslashes($this->request->param('headImg')); // 店铺Logo
        $name = addslashes($this->request->param('mchName')); // 店铺名称
        $shop_information = addslashes($this->request->param('mchInfo')); // 店铺信息
        $shop_range = addslashes($this->request->param('confines')); // 经营范围
        $cpc = addslashes($this->request->param('cpc')); // 区号
        $tel = addslashes($this->request->param('tel')); // 联系电话
        $sheng = addslashes($this->request->param('shen')); // 省
        $shi = addslashes($this->request->param('shi')); // 市
        $xian = addslashes($this->request->param('xian')); // 县
        $address = addslashes($this->request->param('address')); // 地址
        $is_open = addslashes($this->request->param('isOpen')); // 营业或者是打样
        $business_hours = addslashes($this->request->param('businessHours')); // 营业或者是打样
        $isInvoice = addslashes($this->request->param('isInvoice')); // 是否开发票
        $cid = addslashes($this->request->param('cid')); // 店铺分类ID
        $is_self_delivery = addslashes($this->request->param('is_self_delivery')); // 是否支持商家自配 0.否 1.是
        
        $Jurisdiction = new Jurisdiction();
        $res = cache($access_id);
        $user_id = $res['user_id'];
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            if ($r_mch[0]['id'] != $id)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '店主的店铺ID为' . $r_mch[0]['id'] . '与传过来的店铺ID' . $id . '不一致！';
                $this->Log($Log_content);
                $message = Lang('Illegal invasion');
                return output(109, $message);
            }

            $change_name = '';
            if ($name)
            {
                $r2 = MchModel::where(['store_id'=>$store_id,'id'=>$id])->field('name')->select()->toArray();
                if ($r2[0]['name'] != $name)
                {
                    $r3 = MchModel::where(['store_id'=>$store_id,'name'=>$name,'recovery'=>0])->where('id','<>',$id)->field('id')->select()->toArray();
                    if ($r3)
                    {
                        $message = Lang('set.0');
                        return output(109, $message);
                    }
                    $change_name = $name;
                }
            }
            else
            {
                $message = Lang('set.1');
                return output(109, $message);
            }

            if ($logo)
            {
                $logo = preg_replace('/.*\//', '', $logo); // 获取图片名称
            }
            else
            {
                $message = Lang('set.2');
                return output(109, $message);
            }
            if ($head_img)
            {
                $head_img = preg_replace('/.*\//', '', $head_img); // 获取图片名称
            }
            else
            {
                $message = Lang('set.2');
                return output(109, $message);
            }
            if ($poster_img)
            {
                $poster_img = preg_replace('/.*\//', '', $poster_img); // 获取图片名称
            }
            else
            {
                $message = Lang('set.2');
                return output(109, $message);
            }

            $address_xx = $sheng.$shi.$xian.$address;

            $Longitude_and_latitude = Tools::get_Longitude_and_latitude($store_id ,$address_xx);
            $longitude = $Longitude_and_latitude['longitude'];
            $latitude = $Longitude_and_latitude['latitude'];

            $sql1_where = array('store_id'=>$store_id,'id'=>$id);
            if ($change_name == '')
            {
                $sql1_update = array('is_open'=>$is_open,'shop_information'=>$shop_information,'shop_range'=>$shop_range,'cpc'=>$cpc,'tel'=>$tel,'logo'=>$logo,'head_img'=>$head_img,'poster_img'=>$poster_img,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'longitude'=>$longitude,'latitude'=>$latitude,'cid'=>$cid,'is_invoice'=>$isInvoice,'business_hours'=>$business_hours,'is_self_delivery'=>$is_self_delivery);
            }
            else
            {
                $sql1_update = array('is_open'=>$is_open,'shop_information'=>$shop_information,'shop_range'=>$shop_range,'cpc'=>$cpc,'tel'=>$tel,'logo'=>$logo,'head_img'=>$head_img,'poster_img'=>$poster_img,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'longitude'=>$longitude,'latitude'=>$latitude,'cid'=>$cid,'name'=>$change_name,'is_invoice'=>$isInvoice,'business_hours'=>$business_hours,'is_self_delivery'=>$is_self_delivery);
            }
            $r1 = Db::name('mch')->where($sql1_where)->update($sql1_update);
            if ($r1 == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了店铺设置信息失败', 2,2,$id,$operator_id);

                $message = Lang('Abnormal business');
                return output(109, $message);
            }
            else
            {
                $array = array('store_id'=>$store_id,'type0'=>3,'id'=>$id,'name'=>$change_name);
                PC_Tools::modify_jump_path($array);

                $Jurisdiction->admin_record($store_id, $operator, '修改了店铺设置信息', 2,2,$id,$operator_id);
                $message = Lang('Success');
                return output(200, $message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 修改密码
    public function SetPassword()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

    	$j_password = addslashes(trim($this->request->param('pwdOld'))); // 旧密码
    	$x_password = addslashes(trim($this->request->param('pwd'))); // 新密码

        $Jurisdiction = new Jurisdiction();
        $res = cache($access_id);
        $user_id = $res['user_id'];
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
            if($j_password == '')
            {
                $message = Lang('set.3');
                return output(109, $message);
            }
            if(strlen($x_password) < 6 || strlen($x_password) > 16)
            {
                $message = Lang('set.4');
                return output(109, $message);
            }

            $re = UserModel::where(['store_id'=>$store_id,'user_id'=>$operator])->field('mima')->select()->toArray();
            if (!empty($re))
            {
                $mima = $re[0]['mima'];
                $password01 = Tools::unlock_url($mima);
            }
            if($j_password != $password01)
            {
                $message = Lang('set.5');
                return output(109, $message);
            }

            if($x_password == $password01)
            {
                $message = Lang('set.6');
                return output(109, $message);
            }
            else
            {
                $x_password = Tools::lock_url($x_password);
            }

            $sql_where = array('store_id'=>$store_id,'user_id'=>$operator);
            $sql_update = array('mima'=>$x_password);
            $r = Db::name('user')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改密码失败', 2,2,$mch_id,$operator_id);
                $message = Lang('Busy network');
                return output(109, $message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了密码', 2,2,$mch_id,$operator_id);
                $message = Lang('Success');
                return output(200, $message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 店铺轮播图
    public function BannerList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $type = addslashes($this->request->param('type')); // 类型 1:移动端 4.PC商城
    	$page = addslashes($this->request->param('pageNo')); // 页码
    	$pagesize = addslashes($this->request->param('pageSize')); // 每页显示多少条数据
        $page = $page ? $page : '1';
        $pagesize = $pagesize ? $pagesize : '10';

        $res = cache($access_id);
        $user_id = $res['user_id'];

        $total = 0;
        $list = array();

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();

        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
            if($type == 4)
            {
                $r1 = BannerModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>4])->field('id')->select()->toArray();

                $r2 = BannerModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>4])->page((int)$page,(int)$pagesize)->order('sort','desc')->select()->toArray();
            }
            else
            {
                $r1 = BannerModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->where('type','<>',4)->field('id')->select()->toArray();

                $r2 = BannerModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->where('type','<>',4)->page((int)$page,(int)$pagesize)->order('sort','desc')->select()->toArray();
            }
            $total = count($r1);

            if ($r2)
            {
                foreach ($r2 as $k => $v)
                {
                    $v['image'] = ServerPath::getimgpath($v['image'], $store_id);
                    $list[] = $v;
                }
            }
            
            $data = array('total' => $total, 'list' => $list);
            $message = Lang('Success');
            return output(200, $message,$data);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 获取轮播图数据
    public function BannerPathList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $open_type = addslashes($this->request->param('type')); // 跳转类型 1.分类 2.商品 3.店铺

        $res = cache($access_id);
        $user_id = $res['user_id'];

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
            $pro_list = array();
            $product_class_list = array();

            $r1 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0,'mch_status'=>2,'active'=>1])->whereIn('status','2,3')->field('id,product_class')->select()->toArray();
            if($r1)
            {
                foreach ($r1 as $k1 => $v1)
                {
                    if(!in_array($v1['id'],$pro_list))
                    {
                        $pro_list[] = $v1['id'];
                    }
                    $product_class = explode('-',trim($v1['product_class'],'-'));
                    if(!in_array($product_class[0],$product_class_list))
                    {
                        $product_class_list[] = $product_class[0];
                    }
                }
            }

            $url_list = array();
            $list = array();
            if($open_type == 1)
            {
                $list = $product_class_list;
            }
            else if($open_type == 2)
            {
                $list = $pro_list;
            }

            if(count($list) > 0)
            {
                foreach ($list as $k => $v)
                {
                    $r0 = JumpPathModel::where(['store_id'=>$store_id,'type0'=>$open_type,'status'=>1])->whereLike('parameter','%='.$v)->order('add_date','desc')->select()->toArray();
                    if($r0)
                    {
                        if($r0[0]['parameter'] != '')
                        {
                            $parameter = explode('=',$r0[0]['parameter'])[1];

                            if($open_type == 1)
                            {
                                $r0[0]['url'] .= '&name=' . $r0[0]['name'] . '&shop_id=' . $mch_id;
                            }
                        }
                        
                        if($r0[0]['type0'] == 1)
                        {
                            $r1 = ProductClassModel::where(['recycle'=>0,'cid'=>$parameter])->field('cid')->select()->toArray();
                        }
                        else if($r0[0]['type0'] == 2)
                        {
                            $r1 = ProductListModel::where(['recycle'=>0,'mch_status'=>2,'id'=>$parameter])->field('id')->select()->toArray();
                        }
                        else if($r0[0]['type0'] == 3)
                        {
                            $r1 = MchModel::where(['recovery'=>0,'review_status'=>1,'id'=>$parameter])->field('id')->select()->toArray();
                        }
                        if($r1)
                        {
                            $url_list[] = $r0[0];
                        }
                    }
                }
            }
            $data = array('list'=>$url_list);
            $message = Lang('Success');
            return output(200, $message, $data);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 添加/编辑店铺轮播图
    public function AddBannerInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('id')); // 轮播图ID
        $type = addslashes($this->request->param('type')); // 跳转类型 1.分类 2.商品 3.店铺
        $image = addslashes($this->request->param('imageUrl')); // 图片
        $url = addslashes($this->request->param('path')); // 路径

        $Jurisdiction = new Jurisdiction();
        $res = cache($access_id);
        $user_id = $res['user_id'];
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $time = date("Y-m-d H:i:s");
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
            if ($image)
            {
                $image = preg_replace('/.*\//', '', $image);
            }
            else
            {
                $message = Lang('set.7');
                return output(109, $message);
            }

            // 查询最大的排序号
            $rr = BannerModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
            $sort = $rr[0]['sort'];
            $sort = $sort + 1;
            
            if($id != '' && $id != 0)
            { // 修改轮播图
                $sql_update = array('image'=>$image,'url'=>$url,'type'=>$type,'add_date'=>$time);
                $sql_where = array('store_id'=>$store_id,'id'=>$id,'mch_id'=>$mch_id);
                $r = Db::name('banner')->where($sql_where)->update($sql_update);
                if ($r == -1)
                {
                    $Jurisdiction->admin_record($store_id, $user_id, '修改了轮播图ID：' . $id . '失败！', 2,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主修改轮播图ID为' . $id . '失败！';
                    $this->Log($Log_content);
                    $message = Lang('set.8');
                    return output(109, $message);
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $user_id, '修改了轮播图ID：' . $id . '成功！', 2,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主修改轮播图ID为' . $id . '成功！';
                    $this->Log($Log_content);
                    $message = Lang('Success');
                    return output(200, $message);
                }
            }
            else
            { // 添加
                $sql = array('store_id'=>$store_id,'image'=>$image,'url'=>$url,'sort'=>$sort,'type'=>$type,'add_date'=>$time,'mch_id'=>$mch_id);
                $r = Db::name('banner')->insertGetId($sql);
                if ($r == -1)
                {
                    $Jurisdiction->admin_record($store_id, $user_id, '添加轮播图失败！', 1,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主添加轮播图失败！';
                    $this->Log($Log_content);
                    $message = Lang('set.9');
                    return output(109, $message);
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $user_id, '添加了轮播图ID:' . $r, 1,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主添加轮播图成功！';
                    $this->Log($Log_content);
                    $message = Lang('Success');
                    return output(200, $message);
                }
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 店铺轮播图排序
    public function SetBannerSort()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('id')); // 轮播图ID
        $sort = addslashes($this->request->param('sort')); // 排序

        $Jurisdiction = new Jurisdiction();
        $res = cache($access_id);
        $user_id = $res['user_id'];
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];

            // 根据轮播图ID修改排序
            $sql_update = array('sort'=>$sort);
            $sql_where = array('store_id'=>$store_id,'id'=>$id,'mch_id'=>$mch_id);
            $r = Db::name('banner')->where($sql_where)->update($sql_update);
            if ($r > 0)
            {
                $Jurisdiction->admin_record($store_id, $user_id, '置顶轮播图ID为' . $id . '成功！', 2,2,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . '店主置顶轮播图ID为' . $id . '成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200, $message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $user_id, '置顶轮播图ID为' . $id . '失败！', 2,2,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . '店主置顶轮播图ID为' . $id . '失败！';
                $this->Log($Log_content);
                $message = Lang('set.8');
                return output(109, $message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 删除店铺轮播图
    public function DelBannerById()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('id')); // 轮播图ID

        $Jurisdiction = new Jurisdiction();
        $res = cache($access_id);
        $user_id = $res['user_id'];
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];

            // 根据轮播图id，删除轮播图信息
            $res = Db::table('lkt_banner')->where(['store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id])->delete();
            if ($res > 0)
            {
                $Jurisdiction->admin_record($store_id, $user_id, '删除了轮播图ID：' . $id . '成功！', 3,2,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . '店主删除轮播图ID为' . $id . '成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200, $message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $user_id, '删除了轮播图ID：' . $id . '失败！', 3,2,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . '店主删除轮播图ID为' . $id . '失败！';
                $this->Log($Log_content);
                $message = Lang('set.10');
                return output(109, $message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 注销店铺
    public function DelMchInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = addslashes(trim($this->request->param('id'))); // 店铺ID

        $res = cache($access_id);
        $user_id = $res['user_id'];
        
        $array = array('store_id'=>$store_id,'id'=>$id,'operator_id'=>0,'operator'=>$user_id,'source'=>2);
        $mch = new MchPublicMethod();
        $mch->Cancel_store($array);

        return;
    }

    // 打印配置页面
    public function GetMchPrintSetup()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $mch_id = $this->user_list['id'];
        
        $printSetupConfig = array();
        $str = "id,store_id as storeId,mch_id as mchId,print_name as printName,print_url as printUrl,sheng,shi,xian,address,phone,add_time as addTime";
        $r2 = PrintSetupModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->field($str)->select()->toArray();
        if ($r2)
        {
            $printSetupConfig = $r2[0];
        }
       
        $data = array('printSetupConfig'=>$printSetupConfig);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 打印配置
    public function SetMchPrintSetup()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        // $mch_id = addslashes(trim($this->request->param('mchId')));
        $id = addslashes(trim($this->request->param('id')));
        $print_name = addslashes(trim($this->request->param('printName'))); // 打印名称
        $print_url = addslashes(trim($this->request->param('printUrl'))); // 打印网址
        $sheng = addslashes(trim($this->request->param('sheng'))); // 省
        $shi = addslashes(trim($this->request->param('shi'))); // 市
        $xian = addslashes(trim($this->request->param('xian'))); // 县
        $address = addslashes(trim($this->request->param('address'))); // 地址
        $phone = addslashes(trim($this->request->param('phone'))); // 联系电话

        $Jurisdiction = new Jurisdiction();
        $res = cache($access_id);
        $user_id = $res['user_id'];
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        
        if($print_name == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！打印名称不能为空！");
            $message = Lang('system.42');
            return output(109,$message);
        }
        if($print_url == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！打印网址不能为空！");
            $message = Lang('system.43');
            return output(109,$message);
        }
        if($sheng == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！省不能为空！");
            $message = Lang('system.34');
            return output(109,$message);
        }
        if($shi == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！市不能为空！");
            $message = Lang('system.35');
            return output(109,$message);
        }
        if($xian == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！县不能为空！");
            $message = Lang('system.36');
            return output(109,$message);
        }
        if($address == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！详细地址不能为空！");
            $message = Lang('system.37');
            return output(109,$message);
        }
        if($phone == '')
        {
            $this->Log(__LINE__ . ":修改基础配置失败！联系电话不能为空！");
            $message = Lang('system.31');
            return output(109,$message);
        }

        $time = date("Y-m-d H:i:s");

        if($id != '')
        {
            $sql_where_0 = array('store_id'=>$store_id,'mch_id'=>$mch_id);
            $sql_update_0 = array('print_name'=>$print_name,'print_url'=>$print_url,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'phone'=>$phone);
            $r_1 = Db::name('print_setup')->where($sql_where_0)->update($sql_update_0);
            if ($r_1 == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了打印配置信息失败', 2,2,$mch_id,$operator_id);
                $this->Log(__LINE__ . ":修改打印配置失败: 条件参数：" . json_encode($sql_where_0) . "；修改参数：" . json_encode($sql_update_0));
                $message = Lang('system.8');
                return output(109,$message);
            }
            $Jurisdiction->admin_record($store_id, $operator, '修改了打印配置信息', 2,2,$mch_id,$operator_id);
        }
        else
        {
            $sql_insert_0 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'print_name'=>$print_name,'print_url'=>$print_url,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'phone'=>$phone,'add_time'=>$time);
            $r_1 = Db::name('print_setup')->insert($sql_insert_0);
            if ($r_1 == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了打印配置信息失败', 1,2,$mch_id,$operator_id);
                $this->Log(__LINE__ . ":添加打印配置失败: 参数:" . json_encode($sql_insert_0));
                $message = Lang('system.8');
                return output(109,$message);
            }
            $Jurisdiction->admin_record($store_id, $operator, '添加了打印配置信息', 2,2,$mch_id,$operator_id);
        }
        $message = Lang('Success');
        return output(200,$message);
    }

    // 获取商城币种
    public function getCurrencys()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $storeCurrency = Tools::get_store_currency(array('store_id'=>$store_id,'type'=>0,'id'=>0));

        $message = Lang('Success');
        return output(200,$message,$storeCurrency);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("mch/set.log",$Log_content);
        return;
    }
}

