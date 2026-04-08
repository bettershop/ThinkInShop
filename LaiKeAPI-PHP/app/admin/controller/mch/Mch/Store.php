<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;

use app\admin\model\MchModel;
use app\admin\model\MchStoreModel;

/**
 * 功能：门店
 * 修改人：DHB
 */
class Store extends BaseController
{
    // 门店管理
    public function Index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('mchStoreId')); // 门店ID
        $name = addslashes($this->request->param('mchName')); // 门店名称
        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页多少条数据
        $page = $page ? $page : '1';
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if($page)
        {
            $start = ($page - 1)*$pagesize;
        }

        $user_id = cache($access_id.'_uid'); // 用户user_id
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
            
            $condition = " store_id = '$store_id' and mch_id = '$mch_id' ";

            if($id != '' && $id != 0)
            {
                $condition .= " and id = '$id' ";
            }

            $total = 0;
            $list = array();
            if($name != '')
            {
                $name_0 = Tools::FuzzyQueryConcatenation($name);
                $condition .= " and name like $name_0 ";
            }
            
            $sql0_1 = "select id from lkt_mch_store where $condition ";
            $r0_1 = Db::query($sql0_1);

            $sql1 = "select * from lkt_mch_store where $condition order by add_date desc limit $start,$pagesize ";
            $r1 = Db::query($sql1);

            $total = count($r0_1);

            if($r1)
            {
                $list = $r1;
            }

            $data = array('list' => $list, 'total' => $total);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 添加/编辑门店
    public function AddStore()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('id')); // 门店ID
        $name = addslashes($this->request->param('name')); // 门店名称
        $cpc = addslashes(trim($this->request->param('cpc'))); // 区号
        $mobile = addslashes($this->request->param('mobile')); // 联系电话
        $business_hours = addslashes($this->request->param('businessHours')); // 营业时间
        $city_all = addslashes($this->request->param('cityAll')); // 省市区
        $address = addslashes($this->request->param('address')); // 详细地址
        $code = trim($this->request->param('code')); // 邮政编码

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();
        $user_id = cache($access_id.'_uid'); // 用户user_id
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];

            if ($name == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '店主店铺名称不能为空！';
                $this->Log($Log_content);
                $message = Lang('store.0');
                return output(109, $message);
            }

            if($id != '' && $id != 0)
            {
                $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            else
            {
                $r0_0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name])->field('id')->select()->toArray();
            }
            if($r0_0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '店主店铺名称重复！';
                $this->Log($Log_content);
                $message = Lang('store.1');
                return output(109, $message);
            }

            if ($mobile == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '店主联系电话不能为空！';
                $this->Log($Log_content);
                $message = Lang('store.2');
                return output(109, $message);
            }
            
            $longitude = 1;
            $latitude = 1;
            
            if ($city_all != '')
            {
                $city_all = explode('-', $city_all);
                $sheng = $city_all[0];
                $shi = $city_all[1];
                $xian = $city_all[2];  
                // $array_address = array('cpc'=>$cpc,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'code'=>$code);
                // $address_xx = PC_Tools::address_translation($array_address); 
                // $Longitude_and_latitude = Tools::get_Longitude_and_latitude($store_id,$address_xx);
                
            }else{
                $sheng = "";
                $shi ="";
                $xian = "";
            }
            
            if ($address == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '详细地址不能为空！';
                $this->Log($Log_content);
                $message = Lang('store.6');
                return output(109, $message);
            }

            $is_default = 1;
            $r0_1 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'is_default'=>1])->field('id')->select()->toArray();
            if($r0_1)
            {
                $is_default = 0;
            }
            
            if($id != '' && $id != 0)
            {
                $sql1_update = array('name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'business_hours'=>$business_hours,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'longitude'=>$longitude,'latitude'=>$latitude,'code'=>$code);
                $sql1_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
                $r1 = Db::name('mch_store')->where($sql1_where)->update($sql1_update);
                if ($r1 == -1)
                {
                    $Jurisdiction->admin_record($store_id, $operator, '修改了门店ID：' . $id . '失败', 2,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主编辑门店失败！';
                    $this->Log($Log_content);
                    $message = Lang('Busy network');
                    return output(109, $message);
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $operator, '修改了门店ID：' . $id . '成功', 2,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主编辑门店成功！';
                    $this->Log($Log_content);
                    $message = Lang('Success');
                    return output(200, $message);
                }
            }
            else
            {
                $sql1 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'cpc'=>$cpc,'mobile'=>$mobile,'business_hours'=>$business_hours,'sheng'=>$sheng,'shi'=>$shi,'xian'=>$xian,'address'=>$address,'add_date'=>$time,'longitude'=>$longitude,'latitude'=>$latitude,'is_default'=>$is_default,'code'=>$code);
                $r1 = Db::name('mch_store')->insertGetId($sql1);
                if ($r1 > 0)
                {   
                    $Jurisdiction->admin_record($store_id, $operator, '添加了门店ID：' . $r1, 1,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主添加门店成功！';
                    $this->Log($Log_content);
                    $message = Lang('Success');
                    return output(200, $message);
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $operator, '添加门店失败', 1,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主添加门店失败！';
                    $this->Log($Log_content);
                    $message = Lang('Busy network');
                    return output(109, $message);
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

    // 设置默认
    public function SetDefaultStore()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('mchStoreId')); // 门店ID

        $Jurisdiction = new Jurisdiction();
        $user_id = cache($access_id.'_uid'); // 用户user_id
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $time = date("Y-m-d H:i:s");
        Db::startTrans();

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];

            // 查询默认门店
            $r0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'is_default'=>1])->field('id')->select()->toArray();
            if($r0[0]['id'] == $id)
            { // 原先的默认门店ID 是 传过来的默认门店ID
                $r1 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->where('id','<>',$id)->limit(1)->order('add_date','asc')->field('id')->select()->toArray();
                if($r1)
                {
                    $id1 = $r1[0]['id'];

                    $sql2_update = array('is_default'=>0);
                    $sql2_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
                    $r2 = Db::name('mch_store')->where($sql2_where)->update($sql2_update);
                    if ($r2 < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改门店是否默认失败！参数:'.json_encode($sql2_where);
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('store.7');
                        return output(109,$message);
                    }

                    $sql3_update = array('is_default'=>1);
                    $sql3_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id1);
                    $r3 = Db::name('mch_store')->where($sql3_where)->update($sql3_update);
                    if ($r3 < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改门店是否默认失败！参数:'.json_encode($sql3_where);
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('store.7');
                        return output(109,$message);
                    }
                }
                else
                {
                    $message = Lang('store.8');
                    return output(109, $message);
                }
            }
            else
            {
                $sql2_update = array('is_default'=>0);
                $sql2_where = array('store_id'=>$store_id,'mch_id'=>$mch_id);
                $r2 = Db::name('mch_store')->where($sql2_where)->update($sql2_update);
                if ($r2 < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改门店是否默认失败！参数:'.json_encode($sql2_where);
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('store.7');
                    return output(109,$message);
                }

                $sql3_update = array('is_default'=>1);
                $sql3_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
                $r3 = Db::name('mch_store')->where($sql3_where)->update($sql3_update);
                if ($r3 < 1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改门店是否默认失败！参数:'.json_encode($sql3_where);
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('store.7');
                    return output(109,$message);
                }
            }

            $Jurisdiction->admin_record($store_id, $operator, '将门店ID：' . $id . '，设为默认', 2,2,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改门店是否默认成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 删除门店
    public function DelStore()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('id')); // 门店ID
        $id_list = explode(',',$id);

        $Jurisdiction = new Jurisdiction();
        $user_id = cache($access_id.'_uid'); // 用户user_id
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $time = date("Y-m-d H:i:s");
        Db::startTrans();

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];

            if(count($id_list) > 0)
            {
                foreach ($id_list as $k => $v)
                {
                    $r = Db::table('lkt_mch_store')->where(['store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$v])->delete();
                    if ($r < 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . '店主删除门店失败！';
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('Busy network');
                        return output(109, $message);
                    }
                }

                $r0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'is_default'=>1])->field('id')->select()->toArray();
                if(!$r0)
                {
                    $r1 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->limit(1)->order('add_date','asc')->field('id')->select()->toArray();
                    if($r1)
                    {
                        $id1 = $r1[0]['id'];

                        $sql2_update = array('is_default'=>1);
                        $sql2_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id1);
                        $r2 = Db::name('mch_store')->where($sql2_where)->update($sql2_update);
                        if ($r2 < 1)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改门店是否默认失败！参数:'.json_encode($sql2_where);
                            $this->Log($Log_content);
                            Db::rollback();
                            $message = Lang('store.7');
                            return output(109,$message);
                        }
                    }
                }
            }

            $Jurisdiction->admin_record($store_id, $operator, '删除门店,ID为' . $id, 3,2,$mch_id,$operator_id);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 门店管理员列表
    public function AdminList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $mch_store_id = addslashes($this->request->param('mch_store_id')); // 门店ID
        $account_number = addslashes($this->request->param('account_number')); // 管理员账号
        if ($account_number == '')
        {
            $account_number = addslashes($this->request->param('phone')); // 兼容 phone
        }
        if ($account_number == '')
        {
            $account_number = addslashes($this->request->param('mobile')); // 兼容 mobile
        }
        $page = addslashes($this->request->param('pageNo')); // 页码
        $pagesize = addslashes($this->request->param('pageSize')); // 每页多少条数据
        $page = $page ? $page : '1';
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $user_id = cache($access_id.'_uid'); // 用户user_id
        $time = date("Y-m-d H:i:s");

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];

            $con = " store_id = '$store_id' and mch_id = '$mch_id' and mch_store_id = '$mch_store_id' and recycle = 0 ";
            if($account_number != '')
            {
                $con .= " and account_number = '$account_number' ";
            }

            $total = 0;
            $list = array();

            $sql0 = "select count(id) as total from lkt_mch_admin where $con ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $total = $r0[0]['total'];
            }

            $sql1 = "select * from lkt_mch_admin where $con order by add_date desc limit $start,$pagesize ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                foreach($r1 as $k => $v)
                {
                    $v['password'] = Tools::unlock_url($v['password']);
                    $list[] = $v;
                }
            }

            $data = array('list' => $list, 'total' => $total);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 添加/编辑门店管理员
    public function AddAdmin()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes($this->request->param('id')); // 管理员ID
        $mch_store_id = addslashes($this->request->param('mch_store_id')); // 门店ID
        $account_number = addslashes(trim($this->request->param('account_number'))); // 账号
        if ($account_number == '')
        {
            $account_number = addslashes(trim($this->request->param('phone'))); // 兼容 phone
        }
        if ($account_number == '')
        {
            $account_number = addslashes(trim($this->request->param('mobile'))); // 兼容 mobile
        }
        $password = addslashes(trim($this->request->param('password'))); // 密码

        $Jurisdiction = new Jurisdiction();
        $user_id = cache($access_id.'_uid'); // 用户user_id
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $time = date("Y-m-d H:i:s");

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];

            if($account_number == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '管理员账号不能为空';
                $this->Log($Log_content);
                $message = Lang('store.9');
                return output(109, $message);
            }
            else
            {
                if($id != '' && $id != 0)
                {
                    // 手机号账号在同商城下必须唯一（不区分店铺/门店）
                    $sql0 = "select id from lkt_mch_admin where store_id = '$store_id' and account_number = '$account_number' and recycle = 0 and id != '$id' ";
                }
                else
                {
                    // 手机号账号在同商城下必须唯一（不区分店铺/门店）
                    $sql0 = "select id from lkt_mch_admin where store_id = '$store_id' and account_number = '$account_number' and recycle = 0 ";
                }
                $r0 = Db::query($sql0);
                if($r0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '管理员账号重复';
                    $this->Log($Log_content);
                    $message = Lang('store.10');
                    return output(109, $message);
                }
            }

            if (strlen($password) < 6)
            {
                $message = Lang("user.0");
                return output(ERROR_CODE_MMBFHGF,$message);
            }
            else
            {
                $password = Tools::lock_url($password);
            }

            if($id != '' && $id != 0)
            {
                $sql1_update = array('account_number'=>$account_number,'password'=>$password);
                $sql1_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
                $r1 = Db::name('mch_admin')->where($sql1_where)->update($sql1_update);
                if ($r1 == -1)
                {
                    $Jurisdiction->admin_record($store_id, $operator, '修改了门店ID：' . $r1 . '，的管理员账号：' . $account_number . '失败', 2,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主编辑门店管理员失败！';
                    $this->Log($Log_content);
                    $message = Lang('Busy network');
                    return output(109, $message);
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $operator, '修改了门店ID：' . $r1 . '，的管理员账号：' . $account_number, 2,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主编辑门店管理员成功！';
                    $this->Log($Log_content);
                    $message = Lang('Success');
                    return output(200, $message);
                }
            }
            else
            {
                $sql1 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'mch_store_id'=>$mch_store_id,'account_number'=>$account_number,'password'=>$password,'add_date'=>$time);
                $r1 = Db::name('mch_admin')->insertGetId($sql1);
                if ($r1 > 0)
                {   
                    $Jurisdiction->admin_record($store_id, $operator, '添加了门店ID：' . $r1 . '，的管理员账号：' . $account_number, 1,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主添加门店管理员成功！';
                    $this->Log($Log_content);
                    $message = Lang('Success');
                    return output(200, $message);
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $operator, '添加门店管理员失败！' , 1,2,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主添加门店管理员失败！';
                    $this->Log($Log_content);
                    $message = Lang('Busy network');
                    return output(109, $message);
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

    // 删除门店管理员
    public function DelAdmin()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $ids = addslashes($this->request->param('id')); // 管理员ID
        $mch_store_id = addslashes($this->request->param('mch_store_id')); // 门店ID

        $Jurisdiction = new Jurisdiction();
        $user_id = cache($access_id.'_uid'); // 用户user_id
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        $time = date("Y-m-d H:i:s");

        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        if ($r_mch)
        {
            $mch_id = $r_mch[0]['id'];
            $id_array =  explode(',',$ids);
            foreach ($id_array as $k => $val) 
            {
                $sql0 = "update lkt_mch_admin set recycle = 1 where store_id = '$store_id' and mch_id = '$mch_id' and mch_store_id = '$mch_store_id' and id = '$val' and recycle = 0 ";
                $r0 = Db::execute($sql0);
                if($r0 > 0)
                {
                    
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '店主删除门店管理员失败！';
                    $this->Log($Log_content);
                    $message = Lang('Busy network');
                    return output(109, $message);
                }
            }

            $Jurisdiction->admin_record($store_id, $operator, '删除了门店ID：' . $mch_store_id . '，的管理员'  , 3,2,$mch_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主删除门店管理员成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200, $message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '店主没有店铺！';
            $this->Log($Log_content);
            $message = Lang('Illegal invasion');
            return output(109, $message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("mch/set.log",$Log_content);
        return;
    }
}
