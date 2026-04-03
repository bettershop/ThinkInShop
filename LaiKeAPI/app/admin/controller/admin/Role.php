<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\ServerPath;
use app\common\Role_Tools;
use app\common\Jurisdiction;
use app\common\LaiKeLogUtils;
use app\common\ExcelUtils;
use app\common\Tools;

use app\admin\model\CoreMenuModel;
use app\admin\model\RoleModel;
use app\admin\model\RoleMenuModel;
use app\admin\model\AdminModel;
use app\admin\model\AdminRecordModel;
use app\admin\model\CustomerModel;

/**
 * 功能：权限管理类
 * 修改人：PJY
 */
class Role extends BaseController
{   
    // 获取角色菜单
    public function getUserRoleInfo()
    {
        $store_id = addslashes($this->request->param('storeId'));
        
        $id = addslashes($this->request->param('id'));

        $list = array();
        if($id)
        {
            $r0 = RoleMenuModel::where('role_id',$id)->field('menu_id')->select()->toArray();
            if($r0)
            {
                $list0 = Role_Tools::see_menu_0($store_id,array());
                $list = Role_Tools::see_menu_1($store_id,$r0,$list0);
            }
        }
        else
        {
            $list = Role_Tools::see_menu_0($store_id,array());
        }

        $data = array('menuList'=>$list);
        $message = Lang('Success');
        return output(200, $message, $data);
    }

    //管理员列表
    public function getAdminInfo()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $pagesize = $this->request->param('pageSize');// 每页显示多少条数据
        $page = $this->request->param('pageNo');
        $pagesize = $pagesize ? ($pagesize == 'undefined' ? 10 : $pagesize) : 10;
        // 页码
        $page = $page ? $page : 1;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }
        // 根据管理员id,查询管理员信息(是否是客户或商城管理员)
        $r0 = AdminModel::where('id',$admin_id)->select()->toArray();
        $store_type = 0; // 允许查看该商城所有管理员
        if ($r0[0]['type'] == 0 || $r0[0]['type'] == 1)
        { // 允许查看该商城所有管理员
            $store_type = 0; // 允许查看该商城所有管理员
        }
        else
        {
            $store_type = 1; // 不允许查看该商城所有管理员
        }
        $r0 = CustomerModel::where('id',$store_id)->field('customer_number')->select()->toArray();
        $customer_number = $r0[0]['customer_number'];
        $total = 0;
        if ($store_type == 0)
        {

            $r = AdminModel::where(['recycle'=>0,'type'=>2,'store_id'=>$store_id])->count();
        }
        else
        {
            $r = AdminModel::where(['recycle'=>0,'type'=>2,'id'=>$admin_id])->count();
        }
        // 查询管理员信息
        if ($r)
        {
            $total = $r;
        }
        $list = array();
        if($total > 0)
        {
            if ($store_type == 0)
            {
                $rr = AdminModel::where(['recycle'=>0,'type'=>2,'store_id'=>$store_id])->order('add_date','desc')->limit($start,$pagesize)->select()->toArray();
            }
            else
            {
                $rr = AdminModel::where(['recycle'=>0,'type'=>2,'id'=>$admin_id])->order('add_date','desc')->limit($start,$pagesize)->select()->toArray();
            }
            // 查询管理员信息
            if ($rr)
            {
                foreach ($rr as $k1 => $v1)
                {
                    $list_3[$k1] = '';
                    $sid = $v1['sid'];
                    $role = $v1['role'];
                    $r_role = RoleModel::where('id',$role)->field('name,permission')->select()->toArray();
                    if ($r_role)
                    {
                        $rr[$k1]['roleName'] = $r_role[0]['name'];
                        $r_admin_name = AdminModel::where('id',$sid)->field('name')->select()->toArray();
                        if ($r_admin_name)
                        {
                            $rr[$k1]['superName'] = $r_admin_name[0]['name'];
                        }
                        else
                        {
                            $rr[$k1]['superName'] = '';
                        }
                    }
                }
            }
            $list = $rr;
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'customer_number'=>$customer_number,'total'=>$total));
    }

    //添加管理员界面
    public function getUserRoles()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $customer_number = '';
        $r0 = CustomerModel::where('id',$store_id)->field('customer_number')->select()->toArray();
        if ($r0)
        {
            $customer_number = $r0[0]['customer_number'];
        }
        $list = array();
        // 查询角色
        $r_1 = RoleModel::where(['status'=>0,'store_id'=>$store_id])->select()->toArray();
        if($r_1)
        {
            $list = $r_1;
        }
        $message = Lang('Success');
        return output(200, $message, array('roleList'=>$list,'adminName'=>$admin_name,'roleId'=>$admin_list['role']));
    }

    // 添加管理员
    public function addAdminInfo()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $id = addslashes(trim($this->request->param('id'))); // 管理员id
        $name = addslashes(trim($this->request->param('adminName'))); // 管理员账号
        $password = MD5(addslashes(trim($this->request->param('adminPWD')))); // 密码
        $role = addslashes(trim($this->request->param('roleId'))); // 角色
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $mch_id = 0;
        $sql_a = "select shop_id from lkt_admin where store_id = '$store_id' and type = 1 ";
        $r_a = Db::query($sql_a);
        if($r_a)
        {
            $mch_id = $r_a[0]['shop_id'];
        }

        if ($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员账号不能为空';
            $this->Log($Log_content);
            $message = Lang("role.0");
            return output(ERROR_CODE_GLYZHBNWK,$message);
        }

        $condition = " name = '$name' and recycle = 0 and (store_id = '$store_id' or store_id = 0) ";
        if($id)
        {
            $condition .= " and id != '$id'";
        }
        //检查是否重复
        $sq = "select id from lkt_admin where $condition ";
        $sr = Db::query($sq);
        if ($sr && is_array($sr) && count($sr) > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员账号已经存在';
            $this->Log($Log_content);
            $message = Lang("role.26");
            return output(ERROR_CODE_GLYZHYCZ,$message);
        }
        else
        {
            if (strlen($name) > 20)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员账号不能超过20个字符内的英文数字长度';
                $this->Log($Log_content);
                $message = Lang("role.2");
                return output(ERROR_CODE_JSMCBNCGGZWZZD,$message);
            }
            
            if ($password && strlen(addslashes(trim($this->request->param('adminPWD')))) < 6)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员密码不能低于6位';
                $this->Log($Log_content);
                $message = Lang("role.4");
                return output(ERROR_CODE_MMBFHGF,$message);
            }
            if ($password && strlen(addslashes(trim($this->request->param('adminPWD')))) > 16)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员密码不能高于20位';
                $this->Log($Log_content);
                $message = Lang("role.5");
                return output(ERROR_CODE_MMBFHGF,$message);
            }

            if ($role == 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择角色';
                $this->Log($Log_content);
                $message = Lang("role.6");
                return output(ERROR_CODE_QXZJS,$message);
            }
            else
            {   
                if($id)
                {
                    $sql = AdminModel::find($id);
                    $sql->name = $name;
                    $sql->role = $role;
                    if($password)
                    {
                        $sql->password = $password;
                    }
                    $r = $sql->save();
                    if ($r)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '修改了管理员ID：' . $id . ' 的信息',2,1,0,$operator_id);
                        $Log_content = __METHOD__ . '->' .__LINE__ . ':修改管理员成功，sid为：' . $admin_id;
                        $this->Log($Log_content);
                        $message = Lang("Success");
                        return output(200,$message);
                    }
                    else
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '修改了管理员ID：' . $id . ' 的信息失败',2,1,0,$operator_id);
                        $Log_content = __METHOD__ . '->' .__LINE__ . ':修改管理员失败，sid为：' . $admin_id;
                        $this->Log($Log_content);
                        $message = Lang("operation failed");
                        return output(ERROR_CODE_CZSB,$message);
                    }
                }
                else
                {
                    $sql = new AdminModel();
                    $sql->sid = $admin_id;
                    $sql->name = $name;
                    $sql->password = $password;
                    $sql->role = $role;
                    $sql->store_id = $store_id;
                    $sql->shop_id = $mch_id;
                    $sql->status = 2;
                    $sql->add_date = date("Y-m-d H:i:s");
                    $sql->recycle = 0;
                    if($store_id == '')
                    {
                        $sql->type = 0;
                    }
                    else
                    {
                        $sql->type = 2;
                    }
                    $sql->save();
                    $r = $sql->id;
                    if ($r > 0)
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '添加了管理员名称：' . $name,1,1,0,$operator_id);
                        $Log_content = __METHOD__ . '->' .__LINE__ . ':添加管理员成功，sid为：' . $admin_id;
                        $this->Log($Log_content);
                        $message = Lang("Success");
                        return output(200,$message);
                    }
                    else
                    {
                        $Jurisdiction->admin_record($store_id, $operator, '添加了管理员名称：' . $name . '失败',1,1,0,$operator_id);
                        $Log_content = __METHOD__ . '->' .__LINE__ . ':添加管理员失败，sid为：' . $admin_id;
                        $this->Log($Log_content);
                        $message = Lang("operation failed");
                        return output(ERROR_CODE_CZSB,$message);
                    }
                }
            }
        }
    }

    //禁用管理员
    public function stopAdmin()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $id = addslashes(trim($this->request->param('id'))); // 管理员账号
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r = AdminModel::where('id',$admin_id)->select()->toArray();
        if ($r)
        {
            $admin_type = $r[0]['admin_type'];
            $type = $r[0]['type'];
            if ($type == 1 || $type == 0)
            {
                if ($admin_id == $id)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 不能禁用自己';
                    $this->Log($Log_content);
                    $message = Lang("role.8");
                    return output(ERROR_CODE_CZSB,$message);
                }
                else
                {
                    $r = AdminModel::where('id',$id)->field('name,status')->select()->toArray();
                    if ($r)
                    {
                        $name = $r[0]['name'];
                        $status = $r[0]['status'];
                        if ($status == 1)
                        {   
                            $sql = AdminModel::find($id);
                            $sql->status = 2;
                            $sql->login_num = 0;
                            $res_1 = $sql->save();
                            if (!$res_1)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 更新管理员状态失败，id:' . $id;
                                $this->Log($Log_content);
                                $message = Lang("operation failed");
                                return output(ERROR_CODE_CZSB,$message);
                            }
                            $Jurisdiction->admin_record($store_id, $operator, '将管理员名称：' . $name . '  进行了启用操作',5,1,0,$operator_id);
                            $message = Lang("Success");
                            return output(200,$message);
                        }
                        else if ($status == 2)
                        {
                            $sql = AdminModel::find($id);
                            $sql->status = 1;
                            $res_2 = $sql->save();
                            if (!$res_2)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . ' 更新管理员状态失败，id:' . $id;
                                $this->Log($Log_content);
                                $message = Lang("operation failed");
                                return output(ERROR_CODE_CZSB,$message);
                            }

                            $Jurisdiction->admin_record($store_id, $operator, '将管理员名称：' . $name . '  进行了禁用操作',5,1,0,$operator_id);
                            $message = Lang("Success");
                            return output(200,$message);
                        }
                    }
                }
            }
            else
            {   
                $message = Lang("role.9");
                return output(ERROR_CODE_QXBZ,$message);
            }
        }
    }

    //删除管理员
    public function delAdminInfo()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $id = addslashes(trim($this->request->param('id'))); // 管理员账号
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r = AdminModel::where('id',$admin_id)->select()->toArray();
        if ($r)
        {
            $admin_type = $r[0]['admin_type'];
            $type = $r[0]['type'];
            $admin_id1 = $r[0]['id'];
            if ($type == 1 || $type == 0)
            {
                if ($admin_id1 == $id)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 不能删除自己';
                    $this->Log($Log_content);
                    $message = Lang("role.10");
                    return output(ERROR_CODE_CZSB,$message);
                }
                else
                {
                    $r = AdminModel::where('id',$id)->field('name')->select()->toArray();
                    $name = $r[0]['name'];
                    $sql_0 = AdminModel::find($id);
                    $sql_0->recycle = 1;
                    $sql_0->status = 1;
                    $res_0 = $sql_0->save();
                    if (!$res_0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除管理员失败，id为：' . $id;
                        $this->Log($Log_content);
                        $message = Lang("operation failed");
                        return output(ERROR_CODE_CZSB,$message);
                    }
                    $Jurisdiction->admin_record($store_id, $operator, '删除了管理员名称：' . $name,3,1,0,$operator_id);
                    $message = Lang("Success");
                    return output(200,$message);
                }
            }
            else
            {
                $message = Lang("role.9");
                return output(ERROR_CODE_QXBZ,$message);
            }
        }
    }

    // 管理员日志
    public function getAdminLoggerInfo()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $name = trim($this->request->param('adminName')); // 管理员账号
        $logAccountType = trim($this->request->param("logAccountType")); // 账号类型 0:系统管理员 1：客户 2:商城管理员 3:店主
        $logOperationType = trim($this->request->param("logOperationType")); // 操作类型 0:登录/退出 1:添加 2:修改 3:删除 4:导出 5:启用/禁用 6:通过/拒绝 10删除订单
        $startdate = trim($this->request->param("startDate"));
        $enddate = trim($this->request->param("endDate"));

        $exportType = $this->request->param('exportType');//导出
        $page = $this->request->param('pageNo'); // 页码
        $pagesize = $this->request->param('pageSize'); // 每页显示多少条数据
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? $pagesize : '10';
        
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }
        // 根据管理员id,查询管理员信息(是否是客户或商城管理员)
        $r0 = AdminModel::where('id',$admin_id)->select()->toArray();
        $store_type = 0; // 允许查看该商城所有管理员
        if ($r0[0]['type'] == 0 || $r0[0]['type'] == 1)
        { // 允许查看该商城所有管理员
            $store_type = 0; // 允许查看该商城所有管理员
        }
        else
        {
            $store_type = 1; // 不允许查看该商城所有管理员
        }

        $store_id_str = '0,'.$store_id;
        $condition = " a.store_id = '$store_id' and a.source = 1 and a.store_id in ($store_id_str) and b.recycle = 0 ";
        if ($name != '')
        {
            $name = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and a.admin_name like $name";
        }
        
        if ($logAccountType != '')
        {
            $condition .= " and b.type = '$logAccountType'";
        }

        if ($logOperationType != '')
        {
            $condition .= " and a.type = '$logOperationType'";
        }

        if ($startdate != '')
        {
            $condition .= " and a.add_date >= '$startdate' ";
        }
        
        if ($enddate != '')
        {
            $enddate = date("Y-m-d 23:59:59", strtotime($enddate));
            $condition .= " and a.add_date <= '$enddate' ";
        }
        $total = 0;
        $sql_num = "select ifnull(count(a.id),0) as num from lkt_admin_record as a left join lkt_admin as b on a.admin_name = b.name where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        if($total > 0)
        {   
            $sql = "select a.*,b.id as admin_id,b.type as admin_type from lkt_admin_record as a left join lkt_admin as b on a.admin_name = b.name where $condition order by a.add_date desc limit $start,$pagesize";
            $res = Db::query($sql);
            if($res)
            {
                foreach($res as $k => $v)
                {
                    if($v['admin_type'] == 1)
                    {
                        $v['account_type'] = '客户';
                    }
                    else if($v['admin_type'] == 2)
                    {
                        $v['account_type'] = '商城管理员';
                    }
                    else if($v['admin_type'] == 3)
                    {
                        $v['account_type'] = '店主';
                    }
                    else
                    {
                        $v['account_type'] = '超级管理员';
                    }

                    if($v['type'] == 1)
                    {
                        $v['operation_type'] = '添加';
                    }
                    else if($v['type'] == 2)
                    {
                        $v['operation_type'] = '修改';
                    }
                    else if($v['type'] == 3)
                    {
                        $v['operation_type'] = '删除';
                    }
                    else if($v['type'] == 4)
                    {
                        $v['operation_type'] = '导出';
                    }
                    else if($v['type'] == 5)
                    {
                        $v['operation_type'] = '启用/禁用';
                    }
                    else if($v['type'] == 6)
                    {
                        $v['operation_type'] = '通过/拒绝';
                    }
                    else if($v['type'] == 10)
                    {
                        $v['operation_type'] = '删除订单';
                    }
                    else
                    {
                        $v['operation_type'] = '登录/退出';
                    }

                    $list[] = $v;
                }
            }
        }
        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '账号ID',
                1 => '账号名称',
                2 => '账号类型',
                3 => '操作类型',
                4 => '操作说明',
                5 => '时间'
            );
            $exportExcel_list = array();

            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['admin_id'],
                        $v['admin_name'],
                        $v['account_type'],
                        $v['operation_type'],
                        $v['event'],
                        $v['add_date']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, '管理员日志');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total,'store_type'=>$store_type));
    }

    //删除日志
    public function delAdminLogger()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        // 接收信息
        $id = $this->request->param('ids'); // id数组
        $id = explode(',', $id); // 变成数组
        foreach ($id as $k => $v)
        {
            $sql_0 = AdminRecordModel::find($v);
            $res_0 = $sql_0->delete();
            if (!$res_0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除管理员日志记录失败，id为：' . $v;
                $this->Log($Log_content);
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }
        $Jurisdiction->admin_record($store_id, $operator, '将管理员日志进行了批量删除操作',3,1,0,$operator_id);
        $message = Lang("Success");
        return output(200,$message);
    }

    //角色列表
    public function getRoleListInfo()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $id = $this->request->param('id');
        $page = $this->request->param('pageNo');
        $pagesize = $this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize : '10'; // 每页显示多少条数据

        $start = 0;
        // 页码
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $array = array('status'=>0,'store_id'=>$store_id,'start'=>$start,'pagesize'=>$pagesize,'id'=>$id);
        $data = Role_Tools::role_list($array);
        $total = $data['total'];
        $list = $data['list'];

        // 根据管理员id,查询管理员信息(是否是客户或商城管理员)
        $r0 = AdminModel::where('id',$admin_id)->select()->toArray();
        $store_type = 0; // 允许查看该商城所有管理员
        if ($r0[0]['type'] == 0 || $r0[0]['type'] == 1)
        { // 允许查看该商城所有管理员
            $store_type = 0; // 允许查看该商城所有管理员
        }
        else
        {
            $store_type = 1; // 不允许查看该商城所有管理员
        }
        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total,'store_type'=>$store_type));
    }

    // 添加角色
    public function addUserRoleMenu()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        // 接收数据
        $id = $this->request->post('id');//角色id
        $name = addslashes(trim($this->request->post('roleName'))); // 角色
        $permissions = $this->request->post('permissions'); // 权限
        $role_describe = $this->request->post('describe'); // 描述

        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if($id)
        {
            $array = array('id'=>$id,'admin_id'=>$admin_id,'admin_name'=>$admin_name,'store_id'=>$store_id,'name'=>$name,'permissions'=>$permissions,'role_describe'=>$role_describe,'operator_id'=>$operator_id,'operator'=>$operator);
            Role_Tools::edit_role($array);
        }
        else
        {
            $array = array('admin_id'=>$admin_id,'admin_name'=>$admin_name,'store_id'=>$store_id,'status'=>0,'name'=>$name,'permissions'=>$permissions,'role_describe'=>$role_describe,'operator_id'=>$operator_id,'operator'=>$operator);
            Role_Tools::add_role($array);
        }
        
    }

    //删除角色
    public function delUserRoleMenu()
    {
        $admin_list = $this->user_list;
        $admin_name = $admin_list['name'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $id = intval($this->request->param('id')); //id
        
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $array = array('status'=>0,'store_id'=>$store_id,'admin_name'=>$admin_name,'id'=>$id,'operator_id'=>$operator_id,'operator'=>$operator);
        Role_Tools::del_role($array);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/member.log",$Log_content);
        return;
    }
}