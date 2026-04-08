<?php
namespace app\admin\controller\mch\Admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\Jurisdiction;
use app\common\Tools;
use app\common\LaiKeLogUtils;
use app\common\PC_Tools;
use app\common\Product;
use app\common\ExcelUtils;
use app\common\GETUI\LaikePushTools;
use app\common\Plugin\RefundUtils;
use app\common\wxpayv2\wxpay;
use app\common\alipay0\aop\test\AlipayReturn;
use app\common\Plugin\MchPublicMethod;

use app\admin\model\MchModel;
use app\admin\model\AdminModel;
use app\admin\model\ProductClassModel;
use app\admin\model\ProductListModel;
use app\admin\model\BrandClassModel;
use app\admin\model\FreightModel;
use app\admin\model\ProductImgModel;
use app\admin\model\ConfigureModel;
use app\admin\model\OrderModel;
use app\admin\model\WithdrawModel;
use app\admin\model\MchConfigModel;
use app\admin\model\MchClassModel;
use app\admin\model\MchPromiseModel;
use app\admin\model\UserModel;
use app\admin\model\ConfigModel;
use app\admin\model\PrintSetupModel;
use app\admin\model\PromiseShModel;

class Mch extends BaseController
{   
    // 获取店铺设置
    public function GetStoreConfigInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1,'recycle'=>0])->field('shop_id')->select()->toArray();
        $shop_id = $r_admin[0]['shop_id'];

        $list = array();
        $r = MchConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->select()->toArray();
        if ($r)
        {
            $r[0]['logo'] = ServerPath::getimgpath($r[0]['logo'], $store_id);
            $r[0]['poster_img'] = ServerPath::getimgpath($r[0]['poster_img'], $store_id);
            $r[0]['head_img'] = ServerPath::getimgpath($r[0]['head_img'], $store_id);
            $r[0]['max_charge'] = round($r[0]['max_charge'],2);
            $r[0]['min_charge'] = round($r[0]['min_charge'],2);
            $r[0]['promise_amt'] = round($r[0]['promise_amt'],2);
            $r[0]['service_charge'] = $r[0]['service_charge'] * 0.01;

            $list = $r[0];
        }
        $data = array('data'=>$list);
        $message = Lang('Success');
        return output("200",$message,$data);
    }
 
    // 设置店铺设置
    public function SetStoreConfigInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

    	$head_img = $this->request->post('headImg'); // 店铺头像
    	$logo = $this->request->post('logiUrl'); // logo
    	$poster_img = $this->request->post('posterImg'); // 店铺宣传图

    	$auto_log_off = $this->request->post('autoLogOff'); // 注销设置
    	$auto_examine = $this->request->post('autoExamine'); // 店铺自动审核天数
    	$commodity_setup = addslashes(trim($this->request->post('uploadType'))); // 商品设置  1.上传商品 2.自选商品
    	$promise_switch = addslashes(trim($this->request->post('promiseSwitch'))); // 是否开启店铺保证金 0关闭 1开启
    	$promise_amt = addslashes(trim($this->request->post('promiseAmt'))); // 保证金
    	$promise_text = $this->request->post('promiseText'); // 保证金说明

        $min_charge = addslashes(trim($this->request->post('minWithdrawalMoney'))); // 最低提现金额
    	$max_charge = addslashes(trim($this->request->post('maxWithdrawalMoney'))); // 最大提现金额
    	$service_charge = addslashes(trim($this->request->post('serviceCharge'))); // 手续费
    	$withdrawal_time_open = addslashes(trim($this->request->post('withdrawalTimeOpen'))); // 提现时间开关 0.不限制 1.指定日期 2.指定时间段
    	$withdrawal_time = addslashes(trim($this->request->post('withdrawalTime'))); // 指定时间(时间段:15-20)
    	$illustrate = $this->request->post('illustrate'); // 提现说明
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1,'recycle'=>0])->field('shop_id')->select()->toArray();
        $shop_id = $r_admin[0]['shop_id'];

        Db::startTrans();

        if (empty($head_img))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '请上传默认店铺Logo！';
            $this->Log($Log_content);
            $message = Lang('mch.78');
            return output(109,$message);
        }
        else
        {
            $head_img = preg_replace('/.*\//', '', $head_img); // 获取图片名称
        }

        if (empty($logo))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '请上传默认店铺Logo！';
            $this->Log($Log_content);
            $message = Lang('mch.44');
            return output(109,$message);
        }
        else
        {
            $logo = preg_replace('/.*\//', '', $logo); // 获取图片名称
        }

        if (empty($poster_img))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '请上传默认店铺宣传图！';
            $this->Log($Log_content);
            $message = Lang('mch.79');
            return output(109,$message);
        }
        else
        {
            $poster_img = preg_replace('/.*\//', '', $poster_img); // 获取图片名称
        }

        if($auto_log_off == '' )
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '注销设置不能为空！';
            $this->Log($Log_content);
            $message = Lang('等待反馈提示语！');
            return output(109,$message);
        }
        else
        {
            if (is_numeric($auto_log_off))
            {
                if($auto_log_off <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '注销设置请填写正整数！';
                    $this->Log($Log_content);
                    $message = Lang('等待反馈提示语！');
                    return output(109,$message);
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '注销设置请填写数字！';
                $this->Log($Log_content);
                $message = Lang('等待反馈提示语！');
                return output(109,$message);
            }
        }

        if($commodity_setup == '' )
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '请选择商品设置！';
            $this->Log($Log_content);
            $message = Lang('mch.56');
            return output(109,$message);
        }

        if($promise_switch == 1)
        {
            if (is_numeric($promise_amt))
            {
                if (preg_match('/^[0-9]+(.[0-9]{1,2})?$/', $promise_amt))
                {

                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '保证金金额格式错误！';
                    $this->Log($Log_content);
                    $message = Lang('mch.57');
                    return output(109,$message);
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '保证金金额请填写数字！';
                $this->Log($Log_content);
                $message = Lang('mch.58');
                return output(109,$message);
            }
        }

        if (is_numeric($min_charge))
        {
            if (preg_match('/^[0-9]+(.[0-9]{1,2})?$/', $min_charge))
            {

            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '最低提现金额格式错误！';
                $this->Log($Log_content);
                $message = Lang('mch.48');
                return output(109,$message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '最低提现金额请填写数字！';
            $this->Log($Log_content);
            $message = Lang('mch.49');
            return output(109,$message);
        }

        if (is_numeric($max_charge))
        {
            if (preg_match('/^[0-9]+(.[0-9]{1,2})?$/', $max_charge))
            {

            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '最大提现金额格式错误！';
                $this->Log($Log_content);
                $message = Lang('mch.50');
                return output(109,$message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '最大提现金额请填写数字！';
            $this->Log($Log_content);
            $message = Lang('mch.51');
            return output(109,$message);
        }

        if ($max_charge <= $min_charge)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '最低提现金额不能大于最大提现金额！';
            $this->Log($Log_content);
            $message = Lang('mch.52');
            return output(109,$message);
        }

        if (is_numeric($service_charge))
        {
            if($service_charge < 0 || $service_charge >= 100)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '手续费为大于0小于1的小数！';
                $this->Log($Log_content);
                $message = Lang('mch.53');
                return output(109,$message);
            }
            $service_charge = $service_charge * 100;

            // if (preg_match('/^[0-9]+(.[0-9]{1,2})?$/', $service_charge))
            // {

            // }
            // else
            // {
            //     $Log_content = __METHOD__ . '->' . __LINE__ . '手续费格式错误！';
            //     $this->Log($Log_content);
            //     $message = Lang('mch.54');
            //     return output(109,$message);
            // }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '手续费请填写数字！';
            $this->Log($Log_content);
            $message = Lang('mch.55');
            return output(109,$message);
        }

        $r0 = MchConfigModel::where(['store_id'=>$store_id,'mch_id'=>0])->select()->toArray();
        if ($r0)
        {
            $sql_where = array('store_id'=>$store_id,'mch_id'=>0);
            $sql_update = array('head_img'=>$head_img,'logo'=>$logo,'poster_img'=>$poster_img,'auto_log_off'=>$auto_log_off,'auto_examine'=>$auto_examine,'commodity_setup'=>$commodity_setup,'promise_switch'=>$promise_switch,'promise_amt'=>$promise_amt,'promise_text'=>$promise_text,'min_charge'=>$min_charge,'max_charge'=>$max_charge,'service_charge'=>$service_charge,'withdrawal_time_open'=>$withdrawal_time_open,'withdrawal_time'=>$withdrawal_time,'illustrate'=>$illustrate);
            $r = Db::name('mch_config')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改店铺设置失败！参数:'.json_encode($sql_where);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '修改了店铺插件的配置信息失败',2,1,0,$operator_id);
                $message = Lang('label.3');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了店铺插件的配置信息',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改店铺设置成功';
                $this->Log($Log_content);
                Db::commit();
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $sql = array('store_id'=>$store_id,'head_img'=>$head_img,'logo'=>$logo,'poster_img'=>$poster_img,'auto_log_off'=>$auto_log_off,'auto_examine'=>$auto_examine,'commodity_setup'=>$commodity_setup,'promise_switch'=>$promise_switch,'promise_amt'=>$promise_amt,'promise_text'=>$promise_text,'min_charge'=>$min_charge,'max_charge'=>$max_charge,'service_charge'=>$service_charge,'withdrawal_time_open'=>$withdrawal_time_open,'withdrawal_time'=>$withdrawal_time,'illustrate'=>$illustrate,'mch_id'=>0);
            $r = Db::name('mch_config')->insert($sql);
            if ($r > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了店铺插件的配置信息',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加店铺设置成功';
                $this->Log($Log_content);
                Db::commit();
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加店铺设置失败！参数:'.json_encode($sql);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '添加了店铺插件的配置信息失败',2,1,0,$operator_id);
                $message = Lang('label.5');
                return output(109,$message);
            }
        }
    }

    // 店铺分类
    public function MchClassList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id')));
        $name = addslashes(trim($this->request->param('name')));
        $page = addslashes(trim($this->request->param('pageNo')));
        $pagesize = addslashes(trim($this->request->param('pageSize')));
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? $pagesize : 10;

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $list = array();
        $total = 0;
        $maxSort = 0;

        $con = " store_id = '$store_id' and recycle = 0 ";
        if($id != '' && $id != 0)
        {
            $con .= " and id = '$id' ";
        }
        if($name != '')
        {
            $name_0 = Tools::FuzzyQueryConcatenation($name);
            $con .= " and name like $name_0 ";
        }

        $sql0 = "select count(id) as total from lkt_mch_class where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql = "select * from lkt_mch_class where $con order by sort desc limit $start,$pagesize";
        $r = Db::query($sql);
        if ($r)
        {
            $maxSort = $r[0]['sort'];
            foreach($r as $k => $v)
            {
                $v['img'] = ServerPath::getimgpath($v['img'], $store_id);
                $list[] = $v;
            }
        }
        $data = array('total'=>$total,'list'=>$list,'maxSort'=>$maxSort);
        $message = Lang('Success');
        return output("200",$message,$data);
    }
    
    // 添加店铺分类
    public function AddMchClass()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id')));
        $name = addslashes(trim($this->request->param('name')));
        $img = addslashes(trim($this->request->param('img')));
        $sort = addslashes(trim($this->request->param('sort')));
        $isDisplay = addslashes(trim($this->request->param('isDisplay')));

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if($name == '')
        {
            $message = Lang('mch.59');
            return output(109,$message);
        }
        else
        {
            if($id != '' && $id != 0)
            {
                $r0 = MchClassModel::where(['store_id'=>$store_id,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            else 
            {
                $r0 = MchClassModel::where(['store_id'=>$store_id,'name'=>$name])->field('id')->select()->toArray();
            }
            if($r0)
            {
                $message = Lang('mch.60');
                return output(109,$message);
            }
        }
        if($img != '')
        {
            $img = preg_replace('/.*\//', '', $img); // 获取图片名称
        }
        if($sort == '')
        {
            $message = Lang('mch.61');
            return output(109,$message);
        }

        $time = date("Y-m-d H:i:s");

        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('name'=>$name,'img'=>$img,'sort'=>$sort,'is_display'=>$isDisplay);
            $r = Db::name('mch_class')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了店铺分类ID：'.$id.' 的信息失败',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改店铺分类失败！参数：' . json_encode($sql_where);
		        $this->Log($Log_content);
                $message = Lang('mch.63');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了店铺分类ID：'.$id.' 的信息',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改店铺分类成功！';
		        $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $sql = array('store_id'=>$store_id,'name'=>$name,'img'=>$img,'sort'=>$sort,'is_display'=>$isDisplay,'add_date'=>$time);
            $r = Db::name('mch_class')->insertGetId($sql);
            if ($r > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了店铺分类：'.$r,1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加店铺分类成功！';
		        $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了店铺分类失败',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加店铺分类失败！参数：' . json_encode($sql);
		        $this->Log($Log_content);
                $message = Lang('mch.62');
                return output(109,$message);
            }
        }
    }
    
    // 店铺分类-是否显示
    public function IsDisplay()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id')));
        $is_display = addslashes(trim($this->request->param('isDisplay')));

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r0 = MchClassModel::where(['store_id'=>$store_id,'recycle'=>0,'id'=>$id])->field('id')->select()->toArray();
        if($r0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('is_display'=>$is_display);
            $r = Db::name('mch_class')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '将店铺分类ID：'.$id.' 进行了是否显示操作失败',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改店铺分类是否显示失败！参数：' . json_encode($sql_where);
		        $this->Log($Log_content);
                $message = Lang('mch.63');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '将店铺分类ID：'.$id.' 进行了是否显示操作',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改店铺分类是否显示成功！';
		        $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }
    
    // 删除店铺分类
    public function DelMchClass()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id')));

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r0 = MchClassModel::where(['store_id'=>$store_id,'recycle'=>0,'id'=>$id])->field('id')->select()->toArray();
        if($r0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('recycle'=>1);
            $r = Db::name('mch_class')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '删除了店铺分类ID：'.$id.' 失败',3,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除店铺分类失败！参数：' . json_encode($sql_where);
		        $this->Log($Log_content);
                $message = Lang('mch.63');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '删除了店铺分类ID：'.$id,3,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除店铺分类成功！';
		        $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }
    
    // 店铺列表
    public function GetMchInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

    	$id = addslashes(trim($this->request->param('id'))); // 店铺ID
    	$is_open = addslashes(trim($this->request->param('isOpen'))); // 是否营业：0.未营业 1.营业中 2.打烊
    	$is_margin = addslashes(trim($this->request->param('promiseStatus'))); // 是否缴纳保证金1未缴纳2已缴纳
    	$name = addslashes(trim($this->request->param('name'))); // 店铺名称
    	$cid = addslashes(trim($this->request->param('cid'))); // 店铺分类
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
    	$pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $time = date("Y-m-d H:i:s");
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $isAccounts = 0;
        $r_config = ConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if($r_config)
        {
            $isAccounts = $r_config[0]['is_accounts'];
        }

        $condition = " m.store_id = '$store_id' and u.store_id = '$store_id' and m.recovery = 0 ";
        if($id != 0 && $id != '')
        {
            $condition .= " and m.id = '$id' ";
        }
        else 
        {
            $condition .= " and m.review_status = 1 ";
        }
        if ($is_open != '')
        {
            $condition .= " and m.is_open = '$is_open'";
        }

        if (!empty($name))
        {
            $name_0 = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and (m.user_id like $name_0 OR m.name like $name_0)";
        }

        if ($cid != '')
        {
            $condition .= " and m.cid = '$cid'";
        }
        
        $list = array();
        $total = 0;
        if($is_margin == '1')
        {
            $condition .= " and (select max(p.add_date) as promise_id from lkt_mch_promise as p where m.id = p.mch_id and p.status = 1 and p.is_return_pay = 0) > 0 ";
        }
        else if($is_margin == '0')
        {
            $condition .= " and ((select max(p.add_date) as promise_id from lkt_mch_promise as p where m.id = p.mch_id and p.is_return_pay = 1) > 0 or (select max(p.add_date) as promise_id from lkt_mch_promise as p where m.id = p.mch_id) is null) ";
        }

        $sql0 = "select count(1) as num from lkt_mch as m left join lkt_user as u on m.user_id = u.user_id left join lkt_mch_class as c on m.cid = c.id where $condition order by m.add_time desc";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        $str = "m.id,m.store_id,m.user_id,m.name,m.shop_information,m.shop_range,m.realname,m.ID_number,m.cpc,m.tel,m.sheng,m.shi,m.xian,m.address,m.logo,m.shop_nature,m.business_license,m.add_time,m.review_status,m.review_result,m.integral_money,m.account_money,m.collection_num,m.is_open,m.is_lock,m.roomid,m.old_roomid,m.cashable_money,m.recovery,m.is_invoice,m.cid,m.poster_img,m.head_img,m.is_open_coupon,m.last_login_time,u.user_name,u.headimgurl,u.source,c.name as className";

        $sql1 = "select $str from lkt_mch as m left join lkt_user as u on m.user_id = u.user_id left join lkt_mch_class as c on m.cid = c.id where $condition order by m.add_time desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id);
                $v['poster_img'] = ServerPath::getimgpath($v['poster_img'], $store_id);
                $v['head_img'] = ServerPath::getimgpath($v['head_img'], $store_id);
                $v['headimgurl'] = $v['head_img'];
                $v['account_money'] = round($v['account_money'],2);
                $v['cashable_money'] = round($v['cashable_money'],2);
                $v['integral_money'] = round($v['integral_money'],2);
                $v['is_open'] = strval($v['is_open']);
                $v['isAccounts'] = $isAccounts;
                
                $business_license = array();
                $business_license_list = explode(',',$v['business_license']);
                foreach ($business_license_list as $k1 => $v1)
                {
                    $business_license[] = ServerPath::getimgpath($v1, $store_id); //图片
                }
    
                $v['business_license'] = $business_license; //图片

                $v['promiseStatus'] = '未缴';
                $id = $v['id'];
                $r2 = MchPromiseModel::where(['mch_id'=>$id])->order('add_date','desc')->limit(1)->field('status,is_return_pay')->select()->toArray();
                if($r2)
                {
                    if($r2[0]['status'] == 1 && $r2[0]['is_return_pay'] == 0)
                    {
                        $v['promiseStatus'] = '已交纳';
                    }
                }

                $goodsNum = 0;
                $ordersNum = 0;

                $r3 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$id,'recycle'=>0])->whereIn('commodity_type','0,1')->field('count(id) as total')->select()->toArray();
                if($r3)
                {
                    $goodsNum = $r3[0]['total'];
                }
                $v['goodsNum'] = $goodsNum; // 商品数量

                $mch_id = ',' . $id . ',';
                $r4 = orderModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0])->field('count(id) as total')->select()->toArray();
                if($r4)
                {
                    $ordersNum = $r4[0]['total'];
                }
                $v['ordersNum'] = $ordersNum; // 订单数量

                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }
   
    // 添加店铺
    public function AddMchInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

    	$name = addslashes(trim($this->request->param('name'))); // 店铺名称
    	$cid = addslashes(trim($this->request->param('cid'))); // 店铺分类
        $shop_range = trim($this->request->param('shop_range')); // 经营范围
        $shop_information = trim($this->request->param('shop_information')); // 店铺信息
        $realname = trim($this->request->param('realname')); // 真实姓名
        $ID_number = trim($this->request->param('ID_number')); // 身份证号码
        $tel = trim($this->request->param('tel')); // 联系电话
        $city_all = trim($this->request->param('city_all')); // 联系地址
        $address = trim($this->request->param('address')); // 联系地址
        $shop_nature = trim($this->request->param('shop_nature')); // 店铺性质
        $imgUrls = trim($this->request->param('imgUrls')); // 身份证证件照

        $zhanghao = trim($this->request->param('account')); // 账号
        $password = trim($this->request->param('password')); // 密码

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        require('../app/common/shop_name.php');

        foreach ($shop_name as $key => $val)
        {
            if (strstr($name, $val) !== false)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加店铺时，店铺名称不合法！';
                $this->Log($Log_content);
                $message = Lang('mch.0');
                return output(225, $message);
            }
        }

        $r = MchModel::where(['store_id'=>$store_id,'name'=>$name,'recovery'=>0])->field('id')->select()->toArray();
        if ($r)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加店铺时，店铺名称已存在！';
            $this->Log($Log_content);
            $message = Lang('mch.1');
            return output(223, $message);
        }

        if($cid == '' || $cid == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加店铺时，请选择店铺分类！';
            $this->Log($Log_content);
            $message = Lang('mch.75');
            return output(225, $message);
        }

        $res = $this->is_idcard($ID_number);
        if (!$res)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加店铺时，身份证号码错误！';
            $this->Log($Log_content);
            $message = Lang('mch.2');
            return output(225, $message);
        }

        if (preg_match("/^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$/", $tel))
        {
            //账号唯一性判断
            $res_1 = UserModel::where('store_id',$store_id)->where('mobile',$tel)->field('id')->select()->toArray();
            if ($res_1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 账号重复！';
                $this->Log($Log_content);
                $message = Lang("user.3");
                return output(ERROR_CODE_ZHYCZ,$message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加店铺时，手机号码有误！';
            $this->Log($Log_content);
            $message = Lang('mch.3');
            return output(117, $message);
        }

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

        $city_list = explode('-',$city_all);
        $sheng = $city_list[0];
        $shi = $city_list[1];
        $xian = $city_list[2];
        if($address == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加店铺时，详细地址不能为空！';
            $this->Log($Log_content);
            $message = Lang('mch.5');
            return output(109, $message);
        }
        $address_xx = $sheng.$shi.$xian.$address;

        $Longitude_and_latitude = Tools::get_Longitude_and_latitude( $store_id,$address_xx);
        $longitude = $Longitude_and_latitude['longitude'];
        $latitude = $Longitude_and_latitude['latitude'];
        
        $logo = $poster_img = $head_img = '';
        $r = MchConfigModel::where('store_id',$store_id)->field('logo,poster_img,head_img')->select()->toArray();
        if($r)
        {
            $logo = $r[0]['logo'];
            $poster_img = $r[0]['poster_img'];
            $head_img = $r[0]['head_img'];
        }

        $r0 = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r0)
        {
            $wx_headimgurl = $r0[0]['wx_headimgurl'];//默认微信用户头像
            $headimgurl = ServerPath::getimgpath($wx_headimgurl);//默认微信用户头像
            $wx_name = $r0[0]['wx_name'];  //默认微信用户名
            $user_id1 = $r0[0]['user_id']; //默认用户名ID前缀
        }

        //账号唯一性判断
        $res_1 = UserModel::where('store_id',$store_id)->where('zhanghao',$zhanghao)->field('id')->select()->toArray();
        if ($res_1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 账号重复！';
            $this->Log($Log_content);
            $message = Lang("user.1");
            return output(ERROR_CODE_ZHYCZ,$message);
        }

        if (strlen($password) < 6)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 密码低于6位数！';
            $this->Log($Log_content);
            $message = Lang("user.0");
            return output(ERROR_CODE_MMBFHGF,$message);
        }
        else
        {
            $password = Tools::lock_url($password);
        }

        $sql_insert = array('store_id'=>$store_id,'user_name'=>$wx_name,'headimgurl'=>$headimgurl,'mobile'=>$tel,'zhanghao'=>$zhanghao,'mima'=>$password,'source'=>6,'Register_data'=>$time,'birthday'=>$time); //固定来源PC端
        $r_data = Db::name('user')->insertGetId($sql_insert);
        //同注册逻辑一致，先写再更新user_id
        $rr = UserModel::where('store_id',$store_id)->max('id');
        $user_id = $user_id1 . ($rr + 1); //新增加的用户user_id
        //更新user_id
        $res_2 = UserModel::find($rr);
        $res_2->user_id = $user_id;
        $res_2->save();

        $data = array('store_id'=>$store_id,'user_id'=>$user_id,'name'=>$name,'shop_information'=>$shop_information,'shop_range'=>$shop_range,'realname'=>$realname,'ID_number'=>$ID_number,'tel'=>$tel,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'shop_nature'=>$shop_nature,'business_license'=>$business_license,'add_time'=>$time,'longitude'=>$longitude,'latitude'=>$latitude,'cid'=>$cid,'logo'=>$logo,'poster_img'=>$poster_img,'head_img'=>$head_img,'review_status'=>1,'is_open'=>2);
        $res_data = Db::name('mch')->insertGetId($data);
        if ($res_data > 0)
        {
            $array = array('store_id'=>$store_id,'type0'=>3,'id'=>$res_data,'name'=>$name);
            PC_Tools::jump_path($array);
            
            $Jurisdiction->admin_record($store_id, $operator, '添加了店铺ID：'.$res_data,1,1,0,$operator_id);
            $message = Lang('Success');
            return output(200, $message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '添加了店铺ID：'.$res_data.'失败',1,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加店铺失败！参数:'. json_encode($data);
            $this->Log($Log_content);
            $message = Lang('mch.6');
            return output(224, $message);
        }
    }

    // 审核列表
    public function GetMchExamineInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $exportType = addslashes($this->request->param('exportType')); // 导出
    	$review_status = addslashes(trim($this->request->param('reviewStatus'))); // 审核状态：0.待审核 1.审核通过 2.审核不通过
    	$name = addslashes(trim($this->request->param('name'))); // 店铺名称
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
    	$pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " m.store_id = '$store_id' and u.store_id = '$store_id' and (m.review_status = 0 or m.review_status = 2) and m.recovery = 0 ";
        $condition1 = " m.store_id = '$store_id' and u.store_id = '$store_id' and (m.review_status = 0 or m.review_status = 2) and m.recovery = 0 ";
        if ($review_status != '')
        {
            $condition .= " and m.review_status = '$review_status'";
        }
        if (!empty($name))
        {
            $name_0 = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and (m.user_id like $name_0 OR m.name like $name_0)";
        }

        $list = array();
        $total = 0;
        $sql0 = "select count(1) as num from lkt_mch as m left join lkt_user as u on m.user_id = u.user_id where $condition order by m.add_time desc";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }
        
        $sql1 = "select m.*,m.poster_img as posterImg,m.head_img as headimg,u.user_name from lkt_mch as m left join lkt_user as u on m.user_id = u.user_id where $condition order by m.add_time desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        foreach ($r1 as $k => $v)
        {
            $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id);
            $v['posterImg'] = ServerPath::getimgpath($v['posterImg'], $store_id);
            $v['headimg'] = ServerPath::getimgpath($v['headimg'], $store_id);
            $v['headimgurl'] = $v['headimg'];
            $v['examineName'] = '';
            if($v['review_status'] == 0)
            {
                $v['examineName'] = '待审核';
                $v['review_time'] = $v['add_time'];
            }
            else if($v['review_status'] == 2)
            {
                $v['examineName'] = '审核不通过';
                $v['review_time'] = $v['review_time'];
            }
            $list[] = $v;
        }

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '店铺ID',
                1 => '店铺名称',
                2 => '联系人',
                3 => '联系电话',
                4 => '申请/审核时间',
                5 => '审核状态'
            );
            $exportExcel_list = array();
            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['id'],
                        $v['name'],
                        $v['realname'],
                        $v['tel'],
                        $v['review_time'],
                        $v['examineName']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '店铺审核列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 编辑店铺
    public function ModifyMchInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

    	$id = addslashes(trim($this->request->param('id'))); // 店铺ID
    	$roomid = addslashes(trim($this->request->param('roomid'))); // 直播ID
    	$shop_information = addslashes(trim($this->request->param('mchInfo'))); // 店铺信息
        $shop_range = addslashes(trim($this->request->param('confines'))); // 经营范围
    	$tel = addslashes(trim($this->request->param('tel'))); // 联系电话
    	$sheng = addslashes(trim($this->request->param('shen'))); // 省
    	$shi = addslashes(trim($this->request->param('shi'))); // 市
    	$xian = addslashes(trim($this->request->param('xian'))); // 县
    	$address = addslashes(trim($this->request->param('address'))); // 详细地址
    	$shop_nature = addslashes(trim($this->request->param('nature'))); // 店铺性质：0.个人 1.企业
    	$cid = addslashes(trim($this->request->param('cid'))); // 店铺分类ID
    	$realname = addslashes(trim($this->request->param('realName'))); // 真实姓名
    	$ID_number = addslashes(trim($this->request->param('idNumber'))); // 身份证号码
    	$name = addslashes(trim($this->request->param('mchName'))); // 店铺名称
    	$is_open = addslashes(trim($this->request->param('isOpen'))); // 是否营业：0.未营业 1.营业中 2.打烊
    	$logo = addslashes(trim($this->request->param('logo'))); // 店铺Logo
    	$head_img = addslashes(trim($this->request->param('headImg'))); // 店铺头像
    	$poster_img = addslashes(trim($this->request->param('posterImg'))); // 店铺新增宣传图
    	$imgUrls = addslashes(trim($this->request->param('license'))); // 身份证证件照

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        // if($logo)
        // {
        //     $logo = preg_replace('/.*\//', '', $logo);
        // }
        // else
        // {
        //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 店铺Logo不能为空';
        //     $this->Log($Log_content);
        //     $message = Lang('mch.78');
        //     return output(109,$message);
        // }

        // if($head_img)
        // {
        //     $head_img = preg_replace('/.*\//', '', $head_img);
        // }
        // else
        // {
        //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 店铺头像不能为空';
        //     $this->Log($Log_content);
        //     $message = Lang('mch.77');
        //     return output(109,$message);
        // }

        if($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 店铺名称不能为空！';
            $this->Log($Log_content);
            $message = Lang('mch.17');
            return output(109,$message);
        }
        else
        {
            $r = MchModel::where(['store_id'=>$store_id,'name'=>$name,'recovery'=>0])->where('id', '<>',$id)->field('id')->select()->toArray();
            if ($r)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '店铺名称已存在！';
                $this->Log($Log_content);
                $message = Lang('mch.80');
                return output(109, $message);
            }
        }

        $old_roomid = 0;
        $user_id = '';
        //获取老的roomid
        $res_r = MchModel::where(['id'=>$id])->field('user_id,roomid')->select()->toArray();
        if ($res_r)
        {
            $user_id = $res_r[0]['user_id'];
            if ($roomid != $res_r[0]['roomid'])
            {
                $old_roomid = $res_r[0]['roomid'];
            }
        }

        // if (empty($shop_information))
        // {
        //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 店铺信息不能为空';
        //     $this->Log($Log_content);
        //     $message = Lang('mch.28');
        //     return output(109,$message);
        // }
        // if (strlen($shop_information) > 150)
        // {
        //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 店铺信息不能超过50个中文字长度';
        //     $this->Log($Log_content);
        //     $message = Lang('mch.29');
        //     return output(109,$message);
        // }

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

        if ($realname == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 真实姓名不能为空！';
            $this->Log($Log_content);
            $message = Lang('mch.81');
            return output(109,$message);
        }

        $r_ID_number = $this->is_idcard($ID_number);
        if (!$r_ID_number)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 身份证号码错误！';
            $this->Log($Log_content);
            $message = Lang('mch.2');
            return output(225, $message);
        }

        if ($tel == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 联系电话不能为空！';
            $this->Log($Log_content);
            $message = Lang('mch.17');
            return output(109,$message);
        }

		if (strlen($tel) != 11)
		{
		    $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写正确的手机号！';
		    $this->Log($Log_content);
            $message = Lang('mch.18');
            return output(109,$message);
		}

        if (empty($address))
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 联系地址不能为空！';
            $this->Log($Log_content);
            $message = Lang('mch.32');
            return output(109,$message);
        }
        if (strlen($address) > 150)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 联系地址不能超过50个中文字长度';
            $this->Log($Log_content);
            $message = Lang('mch.33');
            return output(109,$message);
        }
        $address_xx = $sheng.$shi.$xian.$address;

        $Longitude_and_latitude = Tools::get_Longitude_and_latitude($store_id ,$address_xx);
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

        $sql_update = array('roomid'=>$roomid,'shop_information'=>$shop_information,'shop_range'=>$shop_range,'tel'=>$tel,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'longitude'=>$longitude,'latitude'=>$latitude,'shop_nature'=>$shop_nature,'old_roomid'=>$old_roomid,'cid'=>$cid,'realname'=>$realname,'ID_number'=>$ID_number,'name'=>$name,'is_open'=>$is_open,'logo'=>$logo,'head_img'=>$head_img,'poster_img'=>$poster_img,'business_license'=>$business_license);
        $r = Db::name('mch')->where('id',$id)->update($sql_update);
        if ($r == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了店铺ID：'.$id.' 的信息失败',2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改 ' . $user_id . ' 的开店信息失败！ID为:'.$id;
            $this->Log($Log_content);
            $message = Lang('label.3');
            return output(109,$message);
        }
        else
        {
            $parameter = "shop_id=".$id;
            $sql_j = "select * from lkt_jump_path where store_id = '$store_id' and type0 = 3 and parameter = '$parameter'  ";
            $r_j = Db::query($sql_j);
            if($r_j)
            {
                $array = array('store_id'=>$store_id,'type0'=>3,'id'=>$id,'name'=>$name);
                PC_Tools::modify_jump_path($array);
            }
            else
            {
                $array = array('store_id'=>$store_id,'type0'=>3,'id'=>$id,'name'=>$name);
                PC_Tools::jump_path($array);
            }
            
            $Jurisdiction->admin_record($store_id, $operator, '修改了店铺ID：'.$id.' 的信息',2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改 ' . $user_id . ' 的开店信息成功';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }
    
    // 店铺审核/拒绝
    public function ExamineMch()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

    	$id = addslashes(trim($this->request->param('mchId'))); // 店铺ID
    	$review_status = addslashes(trim($this->request->param('reviewStatus'))); // 审核状态：0.待审核 1.审核通过 2.审核不通过
    	$review_result = addslashes(trim($this->request->param('text'))); // 拒绝理由

        $admin_name = $this->user_list['name'];
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $time = date("Y-m-d H:i:s");

        if($review_status == 1)
        {
            $sql_update = array('review_status'=>$review_status,'review_result'=>$review_result,'review_time'=>$time,'is_open'=>1,'is_lock'=>0);
        }
        else
        {
            $sql_update = array('review_status'=>$review_status,'review_result'=>$review_result,'review_time'=>$time,'is_open'=>2);
        }
        
        
        $r = Db::name('mch')->where('id',$id)->update($sql_update);
        if ($r == -1)
        {
            if($review_status == 1)
            {
                $event = "通过了店铺ID：".$id." 的申请审核失败";
            }
            else
            {
                $event = "拒绝了店铺ID：".$id." 的申请审核失败";
            }
            $Jurisdiction->admin_record($store_id, $operator, $event,2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 审核开店信息失败！参数：'.json_encode($sql_update);
            $this->Log($Log_content);
            $message = Lang('label.3');
            return output(109,$message);
        }
        else
        {
            $res_r = MchModel::where(['id'=>$id])->field('user_id,name')->select()->toArray();
            $user_id = $res_r[0]['user_id'];
            $name = $res_r[0]['name'];

            if ($review_status == 1)
            {
                $msg_title = "店铺申请通过审核！";
                $msg_content = "您店铺【 " . $name . "】 已经通过审核，快去看看吧！";

                $array = array('store_id'=>$store_id,'type0'=>3,'id'=>$id,'name'=>$name);
                PC_Tools::jump_path($array);
            }
            else
            {
                $msg_title = "店铺申请未通过审核！";
                $msg_content = "您店铺【 " . $name . "】 未通过审核！驳回原因：" . $review_result;
            }

            /**店铺申请通知*/
            $pusher = new LaikePushTools();
            $pusher->pushMessage($user_id, $msg_title, $msg_content, $store_id, $admin_name);

            if($review_status == 1)
            {
                $event = "通过了店铺ID：".$id." 的申请审核";
            }
            else
            {
                $event = "拒绝了店铺ID：".$id." 的申请审核";
            }
            $Jurisdiction->admin_record($store_id, $operator, $event,2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 审核 ' . $user_id . ' 的开店信息成功';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 经营数据
    public function StoreLookMch()
    {
        $store_id = addslashes(Request::param('storeId'));
        $store_type = addslashes(Request::param('storeType'));
        $mch_id = addslashes(Request::param('mchId'));

        $role_id = 0;

        $lktlog = new LaiKeLogUtils();
        $res_mch = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->select()->toArray();
        if($res_mch)
        {
            $userid = $res_mch[0]['user_id'];
            $mchInfo = $res_mch[0];
        }
        else
        {
            $message = Lang("Password error");
            return output(ERROR_CODE_MMCWQZXSR,$message);
        }

        //生成session_id
        $access_token = Tools::getToken($store_id,7);
        $exp_time = 7200;
        cache($access_token, $mchInfo, $exp_time);//添加新token数据
        cache($access_token.'_7',$mchInfo['id'],$exp_time);
        cache($access_token.'_roleId', $role_id, $exp_time);
        cache($access_token.'_uid', $userid, $exp_time);

        $pcMchPath = "";
        $r_config = ConfigModel::where(['store_id'=>$store_id])->field('pc_mch_path')->select()->toArray();
        if($r_config)
        {
            $pcMchPath = $r_config[0]['pc_mch_path'];
        }

        $ip = $this->getClientIp();
        $this->Log(__METHOD__ . '->' . __LINE__ . ":" . $ip . "登录成功");
        
        $this->Log(__METHOD__ . '->' . __LINE__ . ":TOKEN " . $access_token);
        $message = Lang("Success");
        return output(200,$message,array('token'=>$access_token,'pcMchPath'=>$pcMchPath,'store_id'=>$store_id));
    }
    
    // 删除店铺
    public function DelMchInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('mchId'))); // 店铺ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('store_id'=>$store_id,'id'=>$id,'operator_id'=>$operator_id,'operator'=>$operator,'source'=>1);
        $mch = new MchPublicMethod();
        $mch->Cancel_store($array);

        return;
    }

    // 保证金列表
    public function Index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $search = addslashes(trim($this->request->param('keyName'))); // 店铺名或用户ID
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $time = date("Y-m-d H:i:s");
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " b.store_id = '$store_id' ";
        if($search)
        {
            $search_0 = Tools::FuzzyQueryConcatenation($search);
            $condition .= " and (b.name like $search_0 or b.user_id = '$search')"; 
        }
        $total = 0;
        $sql_num = "select ifnull(count(a.id),0) as num from lkt_mch_promise as a left join lkt_mch as b on a.mch_id = b.id left join lkt_user as c on b.user_id = c.user_id where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        if($total > 0)
        {
            $str = "a.*,b.user_id,b.logo,b.name as mchName,c.user_name as userName,b.realname,b.tel,b.head_img";

            $sql = "select $str from lkt_mch_promise as a left join lkt_mch as b on a.mch_id = b.id left join lkt_user as c on b.user_id = c.user_id where $condition order by a.add_date desc limit $start,$pagesize";
            $r1 = Db::query($sql);
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id);
                    $v['headimgurl'] = ServerPath::getimgpath($v['head_img'], $store_id);
                    $list[] = $v;
                }
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 保证金审核列表
    public function SelectPromisePrice()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $search = addslashes(trim($this->request->param('title'))); // 店铺名或用户ID
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $time = date("Y-m-d H:i:s");
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " b.store_id = '$store_id' ";
        if($search)
        {
            $search_0 = Tools::FuzzyQueryConcatenation($search);
            $condition .= " and (b.name like $search_0 or b.user_id = '$search')"; 
        }
        $total = 0;
        $sql_num = "select ifnull(count(a.id),0) as num from lkt_promise_sh as a left join lkt_mch as b on a.mch_id = b.id left join lkt_user as c on b.user_id = c.user_id where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        if($total > 0)
        {
            $sql = "select a.*,a.id as aid,b.user_id,b.logo,b.name,c.user_name as userName,b.realname,b.tel,b.head_img as headimgurl from lkt_promise_sh as a left join lkt_mch as b on a.mch_id = b.id left join lkt_user as c on b.user_id = c.user_id where $condition order by a.add_date desc limit $start,$pagesize";
            $r1 = Db::query($sql);
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id);
                    $v['headimgurl'] = ServerPath::getimgpath($v['headimgurl'], $store_id);
                    $v['id'] = $v['mch_id'];
                    $list[] = $v;
                }
            }
        }

        $data = array('total'=>$total,'list'=>$list,'pageStart'=>$page,'pageEnd'=>$pagesize);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 保证金审核
    public function PassOrRefused()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 店铺名或用户ID
        $is_pass = addslashes(trim($this->request->param('isPass'))); // 是否通过 1=通过 2=不通过 3=审核中
        $refused_why = addslashes(trim($this->request->param('refusedWhy'))); // 拒绝原因

        $admin_name = $this->user_list['name'];
        $Tools = new Tools( $store_id, 1);
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");
        
        $r0 = PromiseShModel::where(['id'=>$id])->select()->toArray();
        if($r0)
        {
            $pay = $r0[0]['pay_type']; // 支付方式
            $price = $r0[0]['promise_amt']; // 保证金
            $mch_id = $r0[0]['mch_id']; // 店铺ID
            
            if($is_pass == 1)
            { // 通过
                // 根据店铺ID，查询店铺信息
                $r1 = MchModel::where(['id'=>$mch_id])->field('user_id,name,cpc,tel')->select()->toArray();
                $user_id = $r1[0]['user_id'];
                $mch_name = $r1[0]['name'];
                $cpc = $r1[0]['cpc']; // 区号
                $mobile = $r1[0]['tel']; // 店铺联系电话

                $sql_u = "select money from lkt_user where user_id = '$user_id' ";
                $r_u = Db::query($sql_u);
                if($r_u)
                {
                    $user_money = $r_u[0]['money'];
                }

                // 根据user_id，查询保证金表
                $r2 = MchPromiseModel::where(['mch_id'=>$mch_id])->limit(1)->order('add_date','desc')->field('id,orderNo,status,is_return_pay')->select()->toArray();
                $id2 = $r2[0]['id']; // 保证金记录ID
                $p_sNo = $r2[0]['orderNo']; // 订单号
                $status = $r2[0]['status']; // 保证金状态 1=已交 2=已退还
                $is_return_pay = $r2[0]['is_return_pay']; // 是否退还

                if($status == 2 && $is_return_pay == 1)
                {
                    $message = Lang('Busy network');
                    return output(109,$message);
                }
                //不同支付方式判断
                switch ($pay)
                {
                    case 'wallet_pay' :
                        //钱包
                        $res = RefundUtils::return_user_money($store_id,$user_id, $price,$p_sNo,'',$mch_name);
                        break;
                    case 'aliPay' :
                    case 'alipay' :
                    case 'pc_alipay' :
                    case 'alipay_mobile' :
                    case 'alipay_minipay' :
                        // 支付宝小程序退款 //支付宝手机支付//支付宝扫码支付
                        $zfb_res = AlipayReturn::refund($p_sNo, $price, $store_id, $id, $pay);
                        if ($zfb_res != 'success')
                        {   
                            if($zfb_res == '商家余额不足！' && !empty($mobile))
                            {
                                $array_code = array('cpc'=>$cpc,'mobile'=>$mobile,'type'=>1,'type1'=>10,'bizparams'=>array("sNo" => $p_sNo));
                                $Tools->generate_code($array_code);
                            }  
                            Db::rollback();
                            echo json_encode(array('code' => 109, 'message' => $zfb_res));
                            exit;
                        }
                        break;
                    case 'app_wechat' :
                    case 'mini_wechat' :
                    case 'pc_wechat' :
                    case 'H5_wechat' :
                    case 'jsapi_wechat' :
                        //微信公众号 微信小程序支付 微信APP支付.
                        $wxtk_res = wxpay::wxrefundapi($p_sNo, $p_sNo . $id2, $price, $price, $store_id, $pay);

                        if ($wxtk_res['result_code'] != 'SUCCESS')
                        {
                            Db::rollback();
                            if ($wxtk_res['err_code_des'] == '基本账户余额不足，请充值后重新发起')
                            {
                                $msg_title = Lang('Account_balance_reminder');

                                $msg_content = "账户余额不足，订单【".$p_sNo."】自动退款失败。请尽快登陆平台完成处理！";

                                $sql_admin = "select b.user_id from lkt_admin as a left join lkt_mch as b on a.shop_id = b.id where a.store_id = '$store_id' and a.type = 1 and a.recycle = 0 ";
                                $r_admin = Db::query($sql_admin);
                                if($r_admin)
                                {
                                    $user_id_admin = $r_admin[0]['user_id'];
                                }
                                $pusher = new LaikePushTools();
                                $pusher->pushMessage($user_id_admin, $msg_title, $msg_content, $store_id, $admin_name);

                                $message = Lang('return.12');
                                echo json_encode(array('code' => 109, 'message' => $message));
                                exit;
                            }

                            $message = Lang('return.0');
                            echo json_encode(array('code' => 109, 'message' => $message));
                            exit;
                        }
                        break;
                    case 'baidu_pay' :
                    default:
                        echo $pay . '支付方式不存在！';
                        exit;
                }
                // 根据保证金ID，修改保证金
                $sql_where3 = array('id'=>$id2);
                $sql_update3 = array('status'=>2,'is_return_pay'=>1,'update_date'=>$time);
                $r3 = Db::name('mch_promise')->where($sql_where3)->update($sql_update3);
                if($r3 > 0)
                {
                    $this->Log(__METHOD__ . ":" . __LINE__ . "修改保证金记录成功！");
                }
                else
                {
                    $this->Log(__METHOD__ . ":" . __LINE__ . "修改保证金记录失败！条件参数：".json_encode($sql_where3)."；修改参数：".json_encode($sql_update3));
                }

                $array = array('store_id'=>$store_id,'money'=>$price,'user_money'=>$user_money,'type'=>5,'money_type'=>1,'money_type_name'=>6,'record_notes'=>'','type_name'=>'','s_no'=>$p_sNo,'title_name'=>'','activity_code'=>'','mch_name'=>$mch_name,'withdrawal_fees'=>'','withdrawal_method'=>'');
                $details_id = PC_Tools::add_Balance_details($array);

                $event_2 = '退还店铺保证金';
                $sql_2 = array('store_id'=>$store_id,'user_id'=>$user_id,'money'=>$price,'oldmoney'=>$user_money,'event'=>$event_2,'type'=>41,'main_id'=>$mch_id,'details_id'=>$details_id);
                $r_2 = Db::name('record')->insert($sql_2);
            }
            
            $sql_where = array('id'=>$id);
            $sql_update = array('status'=>$is_pass,'is_pass'=>$is_pass,'refused_why'=>$refused_why,'refused_date'=>$time);
            $r = Db::name('promise_sh')->where($sql_where)->update($sql_update);
            if($r > 0)
            {
                if($is_pass == 1)
                { // 通过
                    $sql_3 = "update lkt_promise_record set status = 1 where store_id = '$store_id' and promise_sh_id = '$id' ";
                    $r_3 = Db::execute($sql_3);

                    $message_16 = "通过：店铺退还保证金审核通过";
                    $message_logging_list16 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>16,'parameter'=>$id,'content'=>$message_16);
                    PC_Tools::add_message_logging($message_logging_list16);

                    $message_25 = "通过：店铺退还保证金审核通过";
                    $message_logging_list25 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>25,'parameter'=>$id,'content'=>$message_25);
                    PC_Tools::add_message_logging($message_logging_list25);
                    
                    $Jurisdiction->admin_record($store_id, $operator, '通过了店铺ID：'.$id.' 保证金的审核',2,1,0,$operator_id);
                }
                else
                { // 不通过
                    $sql_3 = "update lkt_promise_record set status = 2,remarks = '$refused_why' where store_id = '$store_id' and promise_sh_id = '$id' ";
                    $r_3 = Db::execute($sql_3);

                    $message_16 = "失败：店铺退还保证金审核被拒绝，拒绝理由为" . $refused_why;
                    $message_logging_list16 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>16,'parameter'=>$id,'content'=>$message_16);
                    PC_Tools::add_message_logging($message_logging_list16);

                    $message_25 = "失败：店铺退还保证金审核被拒绝，拒绝理由为" . $refused_why;
                    $message_logging_list25 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>25,'parameter'=>$id,'content'=>$message_25);
                    PC_Tools::add_message_logging($message_logging_list25);

                    $Jurisdiction->admin_record($store_id, $operator, '拒绝了店铺ID：'.$id.' 保证金的审核',2,1,0,$operator_id);
                }
                $this->Log(__METHOD__ . ":" . __LINE__ . "修改保证金审核表成功！");
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '通过/拒绝了店铺ID：'.$id.' 保证金的审核失败',2,1,0,$operator_id);
                $this->Log(__METHOD__ . ":" . __LINE__ . "修改保证金审核表失败！条件参数：".json_encode($sql_where)."；修改参数：".json_encode($sql_update));
            }
            $message = Lang('Success');
            return output(200,$message);
        }
        else 
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 删除保证金审核
    public function DeletePromisePrice()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 保证金审核ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        $r0 = PromiseShModel::where(['id'=>$id])->select()->toArray();
        if($r0)
        {
            $pay = $r0[0]['pay_type']; // 支付方式
            $price = $r0[0]['promise_amt']; // 保证金
            $mch_id = $r0[0]['mch_id']; // 店铺ID

            $sql_where = array('id'=>$id);
            $r = Db::table('lkt_promise_sh')->where($sql_where)->delete();
            if ($r > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '拒绝了店铺ID：'.$id.' 保证金的审核记录',3,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除删除保证金审核成功！ID为 ' . $id;
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '拒绝了店铺ID：'.$id.' 保证金的审核记录失败',3,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除删除保证金审核失败！ID为 ' . $id;
                $this->Log($Log_content);
                $message = Lang('Abnormal business');
                return output(109,$message);
            }
        }
        else 
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 商品审核
    public function GetGoodsExamineInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$goodsId = addslashes(trim($this->request->param('goodsId'))); // 商品ID
    	$mch_name = addslashes(trim($this->request->param('mchName'))); // 店铺名称
    	$product_title = addslashes(trim($this->request->param('goodsName'))); // 商品名称
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
    	$pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
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

        $condition = "a.store_id = '$store_id' and a.recycle = 0 and a.mch_status = 1 and a.mch_id > 0 and a.commodity_type != 2 ";
        if($goodsId != '')
        {
            $condition .= " and a.id = '$goodsId' ";
        }
        if ($product_title != '')
        {
            $product_title_0 = Tools::FuzzyQueryConcatenation($product_title);
            $condition .= " and a.product_title like $product_title_0 ";
        }
        if (!empty($mch_name))
        {
            $mch_name_0 = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and (a.mch_id = '$mch_name' or b.name like $mch_name_0) ";
        }

        $list = array();
        $total = 0;
        $sql = "select count(a.id) as num from lkt_product_list as a LEFT JOIN lkt_mch as b on a.mch_id = b.id where $condition ";
        $r_pager = Db::query($sql);
        if ($r_pager)
        {
            $total = $r_pager[0]['num'];
        }
        $sql1 = "select a.id,a.store_id,a.product_number,a.commodity_type,a.product_title,a.subtitle,a.label,a.scan,a.product_class,a.imgurl,a.content,a.richList,a.sort,a.add_date,a.upper_shelf_time,a.volume,a.initial,a.s_type,a.num,a.min_inventory,a.status,a.brand_id,a.is_distribution,a.is_default_ratio,a.keyword,a.weight,a.weight_unit,a.freight,a.is_zhekou,a.separate_distribution,a.recycle,a.gongyingshang,a.is_hexiao,a.active,a.mch_id,a.mch_status,a.search_num,a.publisher,a.is_zixuan,a.source,a.comment_num,a.cover_map,a.class_sort,a.display_position_sort,a.is_presell,a.show_adr,b.name from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.add_date desc limit $start,$pagesize ";
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
                $present_price = '';
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

                $s_type = explode(',', trim($value['s_type'],','));
                $value['s_type'] = $s_type;
                $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                $value['s_type_list'] = $s_type_list;

                $showAdrList = explode(',', trim($value['show_adr'],','));
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

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 商品审核编辑页面
    public function GetGoodsDetailInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = addslashes(trim($this->request->param('goodsId'))); // 商品ID

        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1])->field('shop_id')->select()->toArray();
        $shop_id = $r_admin[0]['shop_id'];

        $message_logging_list = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$id,'type'=>7);
        PC_Tools::message_pop_up($message_logging_list);
        PC_Tools::message_read($message_logging_list);

        $list = array();
        // 根据产品id，查询产品产品信息
        $r = ProductListModel::where(['store_id'=>$store_id,'id'=>$id])->select()->toArray();
        if ($r)
        {
            // $r[0]['imgurl'] = ServerPath::getimgpath($r[0]['imgurl'], $store_id); //图片
            $imgurl = ServerPath::getimgpath($r[0]['imgurl'], $store_id); //图片
            $r[0]['cover_map'] = ServerPath::getimgpath($r[0]['cover_map'], $store_id); //图片
            $r[0]['proVideo'] = $r[0]['pro_video']; //图片

            $product_class = explode('-',trim($r[0]['product_class'],'-')); // 产品类别
            $product_class_num = count($product_class) - 1;
            $brand_id = $r[0]['brand_id']; // 产品品牌
            $freight_id = $r[0]['freight'];
            $status = $r[0]['status'];

            $r_product_class = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$product_class[$product_class_num]])->field('pname')->select()->toArray();
            if ($r_product_class)
            {
                $r[0]['className'] = $r_product_class[0]['pname'];
            }

            $r_brand_class = BrandClassModel::where(['store_id'=>$store_id,'brand_id'=>$brand_id])->field('brand_name')->select()->toArray();
            if ($r_brand_class)
            {
                $r[0]['brandName'] = $r_brand_class[0]['brand_name'];
            }

            $r_freight = FreightModel::where(['store_id'=>$store_id,'id'=>$freight_id])->field('name')->select()->toArray();
            if ($r_freight)
            {
                $r[0]['freightName'] = $r_freight[0]['name'];
            }
            $list = $r;
        }

        $goodsImageUrls = array();
        $goodsImageUrls[] = $imgurl;
        $imgurls = ProductImgModel::where(['product_id'=>$id])->select()->toArray();
        if ($imgurls)
        {
            foreach ($imgurls as $k => $v)
            {
                $v['product_url'] = ServerPath::getimgpath($v['product_url'], $store_id);
                $goodsImageUrls[] = $v['product_url'];
            }
        }
        $attr_group_list = array();
        $checked_attr_list = array();
        //-----查询规格数据
        $res_size = ConfigureModel::where(['pid'=>$id,'recycle'=>0])->select()->toArray();
        if ($res_size)
        {
            $arrar_t = unserialize($res_size[0]['attribute']);
            foreach ($arrar_t as $key => $value)
            {
                if (strpos($key, '_LKT_') !== false)
                {
                    $key = substr($key, 0, strrpos($key, "_LKT"));
                }
                if($key == '默认')
                {
                    break;
                }
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
                            $key = substr($key, 0, strrpos($key, "_LKT"));
                            $value = substr($value, 0, strrpos($value, "_LKT"));
                        }
                        foreach ($attr_group_list as $keya => $valuea)
                        {
                            if ($key == $valuea['attr_group_name'])
                            {
                                if (!in_array($value, $attr_group_list[$keya]['attr_all']))
                                {
                                    $attr_list = array('attr_name' => $value);
                                    array_push($attr_group_list[$keya]['attr_list'], $attr_list);
                                    array_push($attr_group_list[$keya]['attr_all'], $value);
                                }
                            }
                        }
                        $attr_lists[] = array('attr_id' => '', 'attr_group_name' => $key, 'attr_name' => $value);
                    }
                    $checked_attr_list[] = array('attr_list' => $attr_lists, 'cbj' => round($v['costprice'],2), 'yj' => round($v['yprice'],2), 'sj' => round($v['price'],2), 'kucun' => $v['total_num'], 'unit' => $v['unit'], 'img' => ServerPath::getimgpath($v['img'], $store_id), 'cid' => $v['id'],'write_off_num'=>$v['write_off_num']);
                }

                foreach ($attr_group_list as $key => $value)
                {
                    $attr_group_list[$key] = Tools::array_key_remove($attr_group_list[$key], 'attr_all');
                }
            }
        }
        
        $data = array('attrList'=>array(),'attr_group_list'=>$attr_group_list,'checked_attr_list'=>$checked_attr_list,'goodsImageUrls'=>$goodsImageUrls,'goodsInfo'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 商品审核通过/拒绝
    public function GoodsExamine()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = addslashes(trim($this->request->param('goodsId'))); // 商品ID
    	$mch_status = addslashes(trim($this->request->param('status'))); // 审核状态
    	$refuse_reasons = addslashes(trim($this->request->param('text'))); // 拒绝原因

        $admin_name = $this->user_list['name'];
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");
        Db::startTrans();
        
        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1])->field('shop_id')->select()->toArray();
        $shop_id = $r_admin[0]['shop_id'];

        $message_logging_list = array('store_id'=>$store_id,'mch_id'=>$shop_id,'parameter'=>$id,'type'=>7);
        PC_Tools::message_pop_up($message_logging_list);
        PC_Tools::message_read($message_logging_list);

        $r = ProductListModel::where(['store_id'=>$store_id,'id'=>$id])->field('product_title,status,num,mch_id,commodity_str')->select()->toArray();

        $msg_title = "商品通过审核！";
        $msg_content = "您ID为 " . $id . " 的商品已经通过审核！";
        if ($r)
        {
            $mch_id = $r[0]['mch_id'];
            $status = $r[0]['status'];
            $product_title = $r[0]['product_title'];
            if(strlen($product_title) > 10)
            {
                $product_title = substr(addslashes(trim($product_title)),0,9) . '...';
            }

            $sku_id_list = array();
            $r0 = ConfigureModel::where(['pid'=>$id,'recycle'=>0])->field('attribute')->select()->toArray();
            if($r0)
            {
                foreach ($r0 as $k => $v)
                {
                    if($v['attribute'])
                    {
                        $arrar_0 = unserialize($v['attribute']);
                        foreach ($arrar_0 as $key => $value)
                        {
                            $arrar_key = explode('_',$key);
                            $arrar_value = explode('_',$value);
                            $key_num = count($arrar_key) - 1;
                            $value_num = count($arrar_value) - 1;
                            if(!in_array($arrar_key[$key_num],$sku_id_list))
                            {
                                $sku_id_list[] = $arrar_key[$key_num];
                            }
                            if(!in_array($arrar_value[$value_num],$sku_id_list))
                            {
                                $sku_id_list[] = $arrar_value[$value_num];
                            }
                        }
                    }
                }
            }

            $res = MchModel::where(['id'=>$mch_id])->field('user_id,name')->select()->toArray();
            if ($mch_status == 1)
            {
                foreach ($sku_id_list as $k => $v)
                {
                    $r1 = Db::name('sku')->where('id',$v)->update(['status'=>1,'is_examine'=>1]);
                    if($r1 == -1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '通过店铺商品审核失败！商品ID为 ' . $id;
                        $this->Log($Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '通过了商品ID：'.$id.' 审核失败',2,1,0,$operator_id);
                        $message = Lang('mch.40');
                        return output(200,$message);
                    }
                }

                $sql = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
                $rr = Db::name('product_list')->where($sql)->update(['status'=>2,'upper_shelf_time'=>$time,'mch_status'=>2]);
                if ($rr != -1)
                {
                    if($r[0]['commodity_str'] != '')
                    {
                        $commodity_str1 = unserialize($r[0]['commodity_str']);
                        foreach($commodity_str1 as $k => $v)
                        {
                            $data_where = array('id'=>$v,'commodity_type'=>2,'recycle'=>0);
                            $data_update = array('status'=>2,'upper_shelf_time'=>$time,'mch_status'=>2);
                            $r1 = Db::name('product_list')->where($data_where)->update($data_update);
                        }
                    }
                    // /**商品审核通过*/
                    // $pusher = new LaikePushTools();
                    // $pusher->pushMessage($res[0]['user_id'], $msg_title, $msg_content, $store_id, $admin_name);

                    $message_17 = "通过：商品" . $product_title . "审核通过";
                    $message_logging_list17 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>17,'parameter'=>$id,'content'=>$message_17);
                    PC_Tools::add_message_logging($message_logging_list17);

                    $message_18 = "通过：商品" . $product_title . "审核通过";
                    $message_logging_list18 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>18,'parameter'=>$id,'content'=>$message_18);
                    PC_Tools::add_message_logging($message_logging_list18);

                    $Jurisdiction->admin_record($store_id, $operator, '通过了商品ID：'.$id.' 审核',2,1,0,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '通过店铺ID: ' . $mch_id . ' 商品ID为 ' . $id . ' 审核成功';
                    Db::commit();
                    $message = Lang('Success');
                    return output(200,$message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '通过店铺商品审核失败！参数:'.json_encode($sql);
                    $this->Log($Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '通过了商品ID：'.$id.' 审核失败',2,1,0,$operator_id);
                    
                    $msg_title = "您ID为 " . $id . " 的商品未通过审核！";
                    $msg_content = "拒绝原因：" . $refuse_reasons;
                    /**商品审核通过*/
                    $pusher = new LaikePushTools();
                    $pusher->pushMessage($res[0]['user_id'],  $msg_title, $msg_content, $store_id, '');
                    $message = Lang('mch.40');
                    return output(109,$message);
                }
            }
            else
            {
                if (empty($refuse_reasons))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '请填写拒绝原因！';
                    $this->Log($Log_content);
                    $message = Lang('mch.41');
                    return output(109,$message);
                }

                $sql = array('id'=>$id);
                $rr = Db::name('product_list')->where($sql)->update(['mch_status'=>3,'refuse_reasons'=>$refuse_reasons]);
                if ($rr != -1)
                {
                    foreach ($sku_id_list as $k => $v)
                    {
                        $r1 = Db::table('lkt_sku')->where('id',$v)->delete();
                    }
                    
                    // $msg_title = "您ID为 " . $id . " 的商品未通过审核！";
                    // $msg_content = "拒绝原因：" . $refuse_reasons;

                    // /**商品审核未通过*/
                    // $pusher = new LaikePushTools();
                    // $pusher->pushMessage($res[0]['user_id'], $msg_title, $msg_content, $store_id, $admin_name);

                    $message_17 = "失败：商品ID为:" . $id . "的审核申请被拒绝。拒接理由为:" . $refuse_reasons;
                    $message_logging_list17 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>17,'parameter'=>$id,'content'=>$message_17);
                    PC_Tools::add_message_logging($message_logging_list17);

                    $message_18 = "失败：商品ID为:" . $id . "的审核申请被拒绝。拒接理由为:" . $refuse_reasons;
                    $message_logging_list18 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>18,'parameter'=>$id,'content'=>$message_18);
                    PC_Tools::add_message_logging($message_logging_list18);

                    $Jurisdiction->admin_record($store_id, $operator, '拒绝了商品ID：'.$id.' 审核',2,1,0,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '拒绝店铺ID: ' . $mch_id . ' 商品ID为 ' . $id . ' 审核成功';
                    $this->Log($Log_content);
                    Db::commit();
                    $message = Lang('mch.42');
                    return output(200,$message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '拒绝店铺商品审核失败！参数:'.json_encode($sql);
                    $this->Log($Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '拒绝了商品ID：'.$id.' 审核失败',2,1,0,$operator_id);
                    $message = Lang('mch.43');
                    return output(109,$message);
                }
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 提现审核列表
    public function GetWithdrawalExamineInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

    	$name = addslashes(trim($this->request->param('mchName'))); // 店铺名称
    	$mobile = addslashes(trim($this->request->param('phone'))); // 联系电话
    	$startdate = addslashes(trim($this->request->param('startDate'))); // 开始时间
    	$enddate = addslashes(trim($this->request->param('endDate'))); // 截止时间
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
    	$pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition1 = " w.store_id = '$store_id' and m.store_id = '$store_id' and m.review_status = 1 and w.status = 0 and w.is_mch = 1 and m.recovery = 0 ";
        $condition = " w.store_id = '$store_id' and m.store_id = '$store_id' and m.review_status = 1 and w.status = 0 and w.is_mch = 1 and m.recovery = 0 ";
        if ($name)
        {
            $name_0 = Tools::FuzzyQueryConcatenation($name);
            $name = htmlspecialchars($name);
            $condition .= " and (w.id = '$name' OR m.name like $name_0)";
        }
        if ($mobile)
        {
            $mobile_0 = Tools::FuzzyQueryConcatenation($mobile);
            $condition .= " and w.mobile like $mobile_0 ";
        }
        if ($startdate)
        {
            $condition .= " and w.add_date >= '$startdate' ";
        }
        if ($enddate)
        {
            $condition .= " and w.add_date <= '$enddate' ";
        }
        $total = 0;
        $list = array();
        $sql = "select count(1) as num from lkt_withdraw as w left join lkt_bank_card as b on w.Bank_id = b.id left join lkt_mch as m on m.user_id = w.user_id where $condition";
        $r = Db::query($sql);
        if($r)
        {
            $total = $r[0]['num'];
        }

        $str = "w.id,w.user_id,w.name,w.add_date,w.money,w.s_charge,w.mobile,w.status,w.withdraw_status,b.Cardholder,b.Bank_name,b.Bank_card_number,b.branch,m.id as mch_id,m.name as mch_name,m.logo,m.head_img";

        $sql = "select $str from lkt_withdraw as w left join lkt_bank_card as b on w.Bank_id = b.id left join lkt_mch as m on m.user_id = w.user_id where $condition order by w.add_date desc limit $start,$pagesize ";
        $r1 = Db::query($sql);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id);
                $v['headimgurl'] = ServerPath::getimgpath($v['head_img'], $store_id);
                $v['money'] = round($v['money'],2);
                $v['s_charge'] = round($v['s_charge'],2);
                $user_id = $v['user_id'];
                
                $v['examineName'] = '审核中';
                $v['userName'] = '';
                $source = '';
                $v['sourceName'] = '';
                $sql_user = "select user_name,source from lkt_user where store_id = '$store_id' and user_id = '$user_id' ";
                $r_user = Db::query($sql_user);
                if($r_user)
                {
                    $v['userName'] = $r_user[0]['user_name'];
                    $v['source'] = $r_user[0]['source'];
                    $source = $r_user[0]['source'];
                }

                if($source == 1)
                {
                    $v['sourceName'] = '微信小程序';
                }
                else if($source == 2)
                {
                    $v['sourceName'] = 'H5';
                }
                else if($source == 3)
                {
                    $v['sourceName'] = '支付宝小程序';
                }
                else if($source == 4)
                {
                    $v['sourceName'] = '字节跳动小程序';
                }
                else if($source == 5)
                {
                    $v['sourceName'] = '百度小程序';
                }
                else if($source == 6)
                {
                    $v['sourceName'] = 'pc商城';
                }
                else if($source == 7)
                {
                    $v['sourceName'] = 'pc店铺';
                }
                else if($source == 8)
                {
                    $v['sourceName'] = 'pc管理后台';
                }
                else if($source == 9)
                {
                    $v['sourceName'] = 'PC门店核销';
                }
                else if($source == 10)
                {
                    $v['sourceName'] = 'H5门店核销';
                }
                else if($source == 11)
                {
                    $v['sourceName'] = 'app';
                }
                
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 提现审核通过/拒绝
    public function WithdrawalExamine()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

    	$id = addslashes(trim($this->request->param('id'))); // 提现id
    	$m = addslashes(trim($this->request->param('stauts'))); // 状态 1：审核通过 2：拒绝
    	$refuse = addslashes(trim($this->request->param('text'))); // 拒绝原因

        $admin_name = $this->user_list['name'];
        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        Db::startTrans();
        $res = WithdrawModel::where(['store_id'=>$store_id,'id'=>$id])->field('user_id,money,s_charge,add_date')->select()->toArray();
        $user_id = $res[0]['user_id'];
        $money = $res[0]['money']; // 提款金额
        $s_charge = $res[0]['s_charge']; // 手续费
        $add_date = $res[0]['add_date']; // 申请时间

        $r = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0,'review_status'=>1])->field('id,account_money,cashable_money')->select()->toArray();
        $shop_id = $r[0]['id']; // 店铺id
        $yaccount_money = $r[0]['account_money']; // 原有店铺金额
		$ycashable_money = $r[0]['cashable_money'] + $money ; // 原有店铺可提现金额
		
        // 根据提现id，修改状态信息
        if ($m == 1)
        {
            $event = '店主' . $user_id . "提现了" . $money;
            // 在操作列表里添加一条数据
            $sql1 = array('store_id'=>$store_id,'user_id'=>$user_id,'money'=>$money,'oldmoney'=>$ycashable_money,'event'=>$event,'type'=>21,'is_mch'=>1);
            $r1 = Db::name('record')->insert($sql1);
            if($r1 <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加提现记录信息失败！参数:'.json_encode($sql1);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '通过了店铺ID：'.$shop_id.' 的提现审核失败',6,1,0,$operator_id);
                $message = Lang('Busy network');
                return output(109,$message);
            }

            $sql2 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'price'=>$money,'account_money'=>$ycashable_money,'status'=>2,'type'=>3,'addtime'=>$time);
            $r2 = Db::name('mch_account_log')->insert($sql2);
            if($r2 <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加入驻商户账户收支记录失败！参数:'.json_encode($sql2);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '通过了店铺ID：'.$shop_id.' 的提现审核失败',6,1,0,$operator_id);
                $message = Lang('Busy network');
                return output(109,$message);
            }
            // 根据id,修改提现列表中数据的状态

            $r3 = Db::name('withdraw')->where(['store_id'=>$store_id,'id'=>$id])->update(['status'=>1,'examine_date'=>$time]);
            if($r3 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改提现记录状态失败！ID为:'.$id;
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '通过了店铺ID：'.$shop_id.' 的提现审核失败',6,1,0,$operator_id);
                $message = Lang('Busy network');
                return output(109,$message);
            }

            $message_23 = "您提现的" . $money . "元已到账,快去看看吧!";
            $message_logging_list23 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'gongyingshang'=>0,'type'=>23,'parameter'=>$id,'content'=>$message_23);
            PC_Tools::add_message_logging($message_logging_list23);

            $message_24 = "您提现的" . $money . "元已到账,快去看看吧!";
            $message_logging_list24 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'gongyingshang'=>0,'type'=>24,'parameter'=>$id,'content'=>$message_24);
            PC_Tools::add_message_logging($message_logging_list24);
            
            Db::commit();
            
            // $msg_title = "店铺提现成功！";
            // $msg_content = "您提现的" . $money . "元已到账，快去看看吧！";

            $Jurisdiction->admin_record($store_id, $operator, '通过了店铺ID：'.$shop_id.' 的提现审核',6,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 通过id为 ' . $id . ' 的提现信息';
            $this->Log($Log_content);

            // /**店铺提现通知*/
            // $pusher = new LaikePushTools();
            // $pusher->pushMessage($user_id,  $msg_title, $msg_content, $store_id, $admin_name);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $sql1_update = array('cashable_money'=>Db::raw('cashable_money+'.$money)); //'account_money'=>Db::raw('account_money+'.$money)
            $sql1_where = array('store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1);
            $r1 = Db::name('mch')->where($sql1_where)->update($sql1_update);
            if($r1 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改店铺金额失败！参数：'.json_encode($sql1_where);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '拒绝了店铺ID：'.$shop_id.' 的提现审核失败',6,1,0,$operator_id);
                $message = Lang('Busy network');
                return output(109,$message);
            }
            $event = '店主' . $user_id . "提现" . $money . "被拒绝";
            // 在操作列表里添加一条数据
            $sql2 = array('store_id'=>$store_id,'user_id'=>$user_id,'money'=>$money,'oldmoney'=>$ycashable_money,'event'=>$event,'type'=>22,'is_mch'=>1);
            $r2 = Db::name('record')->insert($sql2);
            if($r2 <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加入驻商户账户收支记录失败！参数:'.json_encode($sql2);
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '拒绝了店铺ID：'.$shop_id.' 的提现审核失败',6,1,0,$operator_id);
                $message = Lang('Busy network');
                return output(109,$message);
            }

            $r3 = Db::name('withdraw')->where(['store_id'=>$store_id,'id'=>$id])->update(['status'=>2,'refuse'=>$refuse,'examine_date'=>$time]);
            if($r3 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改提现记录状态失败！ID为:'.$id;
                $this->Log($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '拒绝了店铺ID：'.$shop_id.' 的提现审核失败',6,1,0,$operator_id);
                $message = Lang('Busy network');
                return output(109,$message);
            }

            $message_23 = "您" . $add_date . "申请的提现被驳回!驳回原因:" . $refuse;
            $message_logging_list23 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'gongyingshang'=>0,'type'=>23,'parameter'=>$id,'content'=>$message_23);
            PC_Tools::add_message_logging($message_logging_list23);

            $message_24 = "您" . $add_date . "申请的提现被驳回!驳回原因:" . $refuse;
            $message_logging_list24 = array('store_id'=>$store_id,'mch_id'=>$shop_id,'gongyingshang'=>0,'type'=>24,'parameter'=>$id,'content'=>$message_24);
            PC_Tools::add_message_logging($message_logging_list24);

            Db::commit();
            // $msg_title = "店铺提现失败！";
            // $msg_content = "您" . $add_date . "申请的提现被驳回！驳回原因：" . $refuse;

            $Jurisdiction->admin_record($store_id, $operator, '拒绝了店铺ID：'.$shop_id.' 的提现审核',6,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 拒绝id为 ' . $id . ' 的提现信息';
            $this->Log($Log_content);
            // /**店铺提现通知*/
            // $pusher = new LaikePushTools();
            // $pusher->pushMessage($user_id,  $msg_title, $msg_content, $store_id, $admin_name);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 提现记录
    public function GetWithdrawalInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $exportType = addslashes($this->request->param('exportType')); // 导出
    	$name = addslashes(trim($this->request->param('mchName'))); // 店铺名称
    	$mobile = addslashes(trim($this->request->param('phone'))); // 联系电话
    	$status = addslashes(trim($this->request->param('status'))); // 状态 0：审核中 1：审核通过 2：拒绝
    	$startdate = addslashes(trim($this->request->param('startDate'))); // 开始时间
    	$enddate = addslashes(trim($this->request->param('endDate'))); // 截止时间
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
    	$pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition1 = " w.store_id = '$store_id' and m.store_id = '$store_id' and m.review_status = 1 and w.status != 0 and w.is_mch = 1 and m.recovery = 0 ";
        $condition = " w.store_id = '$store_id' and m.store_id = '$store_id' and m.review_status = 1 and w.status != 0 and w.is_mch = 1 and m.recovery = 0 ";
        if ($name)
        {
            $name_0 = Tools::FuzzyQueryConcatenation($name);
            $name = htmlspecialchars($name);
            $condition .= " and (m.id = '$name' OR m.name like $name_0)";
        }
        if ($mobile)
        {
            $mobile_0 = Tools::FuzzyQueryConcatenation($mobile);
            $condition .= " and w.mobile like $mobile_0 ";
        }
        if($status != '')
        {
            $condition .= " and w.status = '$status' ";
        }
        if ($startdate)
        {
            $condition .= " and w.add_date >= '$startdate' ";
        }
        if ($enddate)
        {
            $condition .= " and w.add_date <= '$enddate' ";
        }
        $total = 0; 
        $list = array();
        $sql = "select count(1) as num from lkt_withdraw as w left join lkt_bank_card as b on w.Bank_id = b.id left join lkt_mch as m on m.user_id = w.user_id where $condition";
        $r = Db::query($sql);
        if($r)
        {
            $total = $r[0]['num'];
        }
       
        $str = "w.id,w.user_id,w.name,w.add_date,w.money,w.s_charge,w.mobile,w.status,w.refuse,w.withdraw_status,b.Cardholder,b.Bank_name,b.Bank_card_number,b.branch,m.id as mch_id,m.name as mch_name,m.logo,m.head_img";

        $sql = "select $str from lkt_withdraw as w left join lkt_bank_card as b on w.Bank_id = b.id left join lkt_mch as m on m.user_id = w.user_id where $condition order by w.add_date desc limit $start,$pagesize ";
        $r1 = Db::query($sql);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $user_id = $v['user_id'];
                $v['logo'] = ServerPath::getimgpath($v['logo'], $store_id);
                $v['headimgurl'] = ServerPath::getimgpath($v['head_img'], $store_id);
                $v['money'] = round($v['money'],2);
                $v['s_charge'] = round($v['s_charge'],2);

                if($v['status'] == 2)
                {
                    $v['examineName'] = '拒绝';
                }
                else if($v['status'] == 1)
                {
                    $v['examineName'] = '审核通过';
                }
                else
                {
                    $v['examineName'] = '审核中';
                }

                $v['userName'] = '';
                $source = '';
                $v['sourceName'] = '';
                $sql_user = "select user_name,source from lkt_user where store_id = '$store_id' and user_id = '$user_id' ";
                $r_user = Db::query($sql_user);
                if($r_user)
                {
                    $v['userName'] = $r_user[0]['user_name'];
                    $v['source'] = $r_user[0]['source'];
                    $source = $r_user[0]['source'];
                }

                if($source == 1)
                {
                    $v['sourceName'] = '微信小程序';
                }
                else if($source == 2)
                {
                    $v['sourceName'] = 'H5';
                }
                else if($source == 3)
                {
                    $v['sourceName'] = '支付宝小程序';
                }
                else if($source == 4)
                {
                    $v['sourceName'] = '字节跳动小程序';
                }
                else if($source == 5)
                {
                    $v['sourceName'] = '百度小程序';
                }
                else if($source == 6)
                {
                    $v['sourceName'] = 'pc商城';
                }
                else if($source == 7)
                {
                    $v['sourceName'] = 'pc店铺';
                }
                else if($source == 8)
                {
                    $v['sourceName'] = 'pc管理后台';
                }
                else if($source == 9)
                {
                    $v['sourceName'] = 'PC门店核销';
                }
                else if($source == 10)
                {
                    $v['sourceName'] = 'H5门店核销';
                }
                else if($source == 11)
                {
                    $v['sourceName'] = 'app';
                }

                $v['id'] = $v['mch_id'];
                $list[] = $v;
            }
        }

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '序号',
                1 => '店铺',
                2 => '联系电话',
                3 => '状态',
                4 => '申请时间',
                5 => '提现金额',
                6 => '提现手续费',
                7 => '持卡人姓名',
                8 => '银行名称',
                9 => '支行名称',
                10 => '卡号',
                11 => '备注'
            );
            $exportExcel_list = array();
            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['id'],
                        $v['mch_name'],
                        $v['mobile'],
                        $v['examineName'],
                        $v['add_date'],
                        $v['money'],
                        $v['s_charge'],
                        $v['Cardholder'],
                        $v['Bank_name'],
                        $v['branch'],
                        $v['Bank_card_number'],
                        $v['refuse']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '店铺提现记录');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
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

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/mch.log",$Log_content);
        return;
    }

    public function getClientIp($type = 0, $client = true)
    {
        $type = $type ? 1 : 0;
        static $ip = NULL;
        if ($ip !== NULL) return $ip[$type];
        if ($client)
        {
            if (isset($_SERVER['HTTP_X_FORWARDED_FOR']))
            {
                $arr = explode(',', $_SERVER['HTTP_X_FORWARDED_FOR']);
                $pos = array_search('unknown', $arr);
                if (false !== $pos) unset($arr[$pos]);
                $ip = trim($arr[0]);
            }
            elseif (isset($_SERVER['HTTP_CLIENT_IP']))
            {
                $ip = $_SERVER['HTTP_CLIENT_IP'];
            }
            elseif (isset($_SERVER['REMOTE_ADDR']))
            {
                $ip = $_SERVER['REMOTE_ADDR'];
            }
        }
        elseif (isset($_SERVER['REMOTE_ADDR']))
        {
            $ip = $_SERVER['REMOTE_ADDR'];
        }
        // 防止IP伪造
        $long = sprintf("%u", ip2long($ip));
        $ip = $long ? array($ip, $long) : array('0.0.0.0', 0);
        return $ip[$type];
    }

    // 公告已读
    public function markToRead()
    {
        $store_id = trim(Request::param('storeId'));
        $store_type = trim(Request::param('storeType')); // 来源
        $access_id = trim(Request::param('accessId')); // 授权id
        $tell_id = trim(Request::param('tell_id')); // 公告ID
        $read_id = cache($access_id.'_uid');
        
        $array = array('store_id'=>$store_id,'store_type'=>$store_type,'read_id'=>$read_id,'tell_id'=>$tell_id);
        PC_Tools::markToRead($array);

        $message = Lang("Success");
        return output(200,$message);
    }

    // 获取维护公告
    public function getUserTell()
    {
        $store_id = trim(Request::param('storeId'));
        $store_type = trim(Request::param('storeType')); // 来源
        $access_id = trim(Request::param('accessId')); // 授权id
        $language = trim(Request::param('language')); // 授权id
        
        $read_id = cache($access_id.'_uid');

        $array = array('store_id'=>$store_id,'store_type'=>7,'read_id'=>$read_id);
        $data = PC_Tools::GetAnnouncement($array);
        // $data = PC_Tools::Obtain_maintenance_announcements($store_type);
        
        $message = Lang("Success");
        return output('200',$message,$data);
    }
}
