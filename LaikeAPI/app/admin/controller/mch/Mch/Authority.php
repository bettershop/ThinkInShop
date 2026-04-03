<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cache;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;
use app\common\ExcelUtils;
use app\common\Plugin\CouponPublicMethod;
use app\common\Jurisdiction;

use app\admin\model\MchModel;
use app\admin\model\MenuModel;
use app\admin\model\UserRoleModel;
use app\admin\model\UserModel;
use app\admin\model\AuthorityMappingModel;
use app\admin\model\UserAuthorityModel;
use app\admin\model\ConfigModel;
use app\admin\model\AdminRecordModel;

/**
 * 功能：店铺权限
 * 修改人：PJY
 */
class Authority extends BaseController
{
    //获取用户权限
    public function GetUserAuthorityTree()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));
        $language = addslashes($this->request->param('language'));
        $lang_code = addslashes($this->request->param('lang_code'));
        
        $shop_id = cache($access_id.'_'.$store_type);
        $role_id = cache($access_id.'_roleId');//登录用户权限
        $sid = $this->request->param('sid');
        $roleId = $this->request->param('roleId');//前端传参权限id

        $condition = " d.TYPE = 2 and d.is_display = 0 and d.lang_code = '$language' "; //AND d.main_id in (0,$shop_id)
        if(empty($sid))
        {
            $condition .= " and d.sid = 0 ";
        }
        else
        {
            $condition .= " and d.sid = '$sid' ";
        }
        $list = array();
        if(strlen($roleId) > 0)
        {   
            $r_arr = array();
            if($roleId == 0)
            {
                $r_arr = AuthorityMappingModel::where(['role_id'=>$role_id])->field('menu_id')->select()->toArray();
            }
            else
            {
                $r_arr = AuthorityMappingModel::where(['role_id'=>$roleId])->field('menu_id')->select()->toArray();

            }
            if($r_arr)
            {
                $sql = "select distinct d.* from lkt_user_role a 
                            RIGHT JOIN lkt_user_authority b ON b.role_id=a.id
                            RIGHT JOIN lkt_authority_mapping c ON c.role_id = a.id 
                            RIGHT JOIN lkt_menu d ON d.id = c.menu_id WHERE $condition order by sort desc ";
                $res = Db::query($sql);
                if($res)
                {
                    foreach ($res as $key => $value) 
                    {   
                        $sid = $value['id'];
                        $res[$key]['checked'] = false;
                        foreach($r_arr as $k => $v) 
                        {
                            if ($sid == $v['menu_id'])
                            {
                                $res[$key]['checked'] = true; // 选中
                            }
                        }
                        $r = MenuModel::where(['is_display'=>0,'sid'=>$sid])->field('id')->select()->toArray();
                        if($r)
                        {
                            $res[$key]['children'] = $this->getChildren($store_id,$sid,$shop_id,$r_arr);
                        }
                        else
                        {
                            $res[$key]['children'] = array();
                        }
                    }
                }
            }
            else
            {
                $sql = "select distinct d.* from lkt_user_role a 
                            RIGHT JOIN lkt_user_authority b ON b.role_id=a.id
                            RIGHT JOIN lkt_authority_mapping c ON c.role_id = a.id 
                            RIGHT JOIN lkt_menu d ON d.id = c.menu_id WHERE $condition order by sort desc ";
                $res = Db::query($sql);
                if($res)
                {
                    foreach ($res as $key => $value) 
                    {   
                        $sid = $value['id'];
                        $res[$key]['checked'] = true;
                        $r = MenuModel::where(['is_display'=>0,'sid'=>$sid])->field('id')->select()->toArray();
                        if($r)
                        {
                            $res[$key]['children'] = $this->getChildren($store_id,$sid,$shop_id);
                        }
                        else
                        {
                            $res[$key]['children'] = array();
                        }
                    }
                }
            }
        }
        else
        {
            $sql = "select distinct d.* from lkt_user_role a 
                            RIGHT JOIN lkt_user_authority b ON b.role_id=a.id
                            RIGHT JOIN lkt_authority_mapping c ON c.role_id = a.id 
                            RIGHT JOIN lkt_menu d ON d.id = c.menu_id WHERE $condition order by sort desc ";
            $res = Db::query($sql);
            foreach ($res as $key => $value) 
            {
                $res[$key]['checked'] = true;
                $res[$key]['children'] = array();
            }

        }
        if($res)
        {
            $list = $res;
        }
        $message = Lang('Success');
        return output(200, $message,array('list'=>$list));
    }

    //获取下级菜单
    public function getChildren($store_id,$sid,$shop_id,$list = array())
    {
        $coupon_status = true;
        if($sid == '1479711351306715136')
        {
            if (file_exists('../app/common/Plugin/CouponPublicMethod.php'))
            {
                $coupon = new CouponPublicMethod();
                $coupon_list = $coupon->Get_plugin_status_mch($store_id); // 店铺是否看见该插件
                if ($coupon_list != 1)
                {
                    $coupon_status = false;
                }
            }
        }
        if($coupon_status)
        {
            $sql = "select distinct d.* from lkt_user_role a 
                            RIGHT JOIN lkt_user_authority b ON b.role_id=a.id
                            RIGHT JOIN lkt_authority_mapping c ON c.role_id = a.id 
                            RIGHT JOIN lkt_menu d ON d.id = c.menu_id WHERE d.TYPE = 2 AND d.main_id in (0,$shop_id) and d.is_display = 0 and d.sid = '$sid' and d.name not in (select plugin_name from lkt_plugins where store_id='$store_id' and status = 0) order by sort desc";
        }
        else
        {
            $sql = "select distinct d.* from lkt_user_role a 
                            RIGHT JOIN lkt_user_authority b ON b.role_id=a.id
                            RIGHT JOIN lkt_authority_mapping c ON c.role_id = a.id 
                            RIGHT JOIN lkt_menu d ON d.id = c.menu_id WHERE d.TYPE = 2 AND d.main_id in (0,$shop_id) and d.is_display = 0 and d.sid = '$sid' and d.id != '1598230490370801664' and d.name not in (select plugin_name from lkt_plugins where store_id='$store_id' and status = 0) order by sort desc";
        }
        $res = Db::query($sql);
        if($res)
        {   
            if($list)
            {
                foreach ($res as $key => $value)
                {
                    $sid = $value['id'];
                    $res[$key]['checked'] = false;
                    if($list)
                    {
                        foreach ($list as $k_0 => $v_0)
                        {
                            if ($sid == $v_0['menu_id'])
                            {
                                $res[$key]['checked'] = true; // 选中
                            }
                        }
                    }
                    $r = MenuModel::where(['is_display'=>0,'sid'=>$sid])->field('id')->select()->toArray();
                    if($r)
                    {
                        $res[$key]['children'] = $this->getChildren($store_id,$sid,$shop_id,$list);
                    }
                    else
                    {
                        $res[$key]['children'] = array();
                    }
                }
            }
            else
            {
                foreach ($res as $key => $value) 
                {
                    $sid = $value['id'];
                    $res[$key]['checked'] = true;
                    $r = MenuModel::where(['is_display'=>0,'sid'=>$sid])->field('id')->select()->toArray();
                    if($r)
                    {
                        $res[$key]['children'] = $this->getChildren($store_id,$sid,$shop_id);
                    }
                    else
                    {
                        $res[$key]['children'] = array();
                    }
                }
            }
        }

        return $res;
    }

    //管理员列表
    public function GetAdminList()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $shop_id = cache($access_id.'_'.$store_type);
        $id = $this->request->param('id');
        $page = $this->request->param('pageNo');
        $pagesize = $this->request->param('pageSize');
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

        $condition = " b.main_id = '$shop_id' and b.type = 2 and a.type = 1 and u.store_id = '$store_id' ";
        if($id != '')
        {
            $condition .= " and a.id = '$id' ";
        }

        $total = 0;
        $sql_num = "select ifnull(count(a.id),0) as num from lkt_user_authority as a left join lkt_user_role as b on a.role_id = b.id left join lkt_user as u on a.main_id = u.user_id where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        $sql = "select a.id as authorityId,a.add_date,u.id,u.user_name,u.zhanghao,a.role_id as roleId,b.name as roleName,a.create_id from lkt_user_authority as a left join lkt_user_role as b on a.role_id = b.id left join lkt_user as u on a.main_id = u.user_id where $condition order by a.add_date desc limit $start,$pagesize";
        $res = Db::query($sql);
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $operator = $value['create_id'];
                $res_q = UserModel::where(['store_id'=>$store_id,'user_id'=>$operator])->field('user_name')->select()->toArray();
                $res[$key]['addName'] = $res_q[0]['user_name'];
            }
            $list = $res;
        }
        $message = Lang('Success');
        return output(200, $message,array('total'=>$total,'list'=>$list));
    }

    //添加编辑管理员
    public function InsertUser()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $id = $this->request->param('id');
        $isUpdate = $this->request->param('isUpdate');//1添加2修改
        $zhanghao = $this->request->param('zhanghao');
        $roleId = $this->request->param('roleId');
        $mima = $this->request->param('mima');

        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type); // 店铺ID
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        if(strlen($mima) < 6)
        {
            $message = Lang('user.0');
            return output(ERROR_CODE_ZHBNWK,$message);
        }
        if($zhanghao == '')
        {
            $message = Lang('login.13');
            return output(ERROR_CODE_QSRMM,$message);
        }
        if($roleId == '')
        {
            $message = Lang('role.15');
            return output(ERROR_CODE_QXZQX,$message);
        }
        if($isUpdate == 2 && $id != '')
        {   
            Db::startTrans();
            //修改绑定角色
            $sql = UserAuthorityModel::find($id);
            $sql->role_id = $roleId;
            $sql->update_date = date('Y-m-d H:i:s');
            $res1 = $sql->save();
            if(!$res1)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员id'.$id.'编辑失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }

            //修改用户密码
            $mima = Tools::lock_url($mima);
            $sql_u = UserModel::where(['zhanghao'=>$zhanghao,'store_id'=>$store_id])->find();
            $sql_u->mima = $mima;
            $res_u = $sql_u->save();
            if(!$res_u)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 用户账号zhanhao'.$zhanghao.'编辑失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }
            Db::commit();

            $Jurisdiction->admin_record($store_id, $operator, '修改了管理员名称：' . $zhanghao . ' 的信息', 2,2,$shop_id,$operator_id);
        }
        else
        {
            //判断账号是否被使用
            $res_q = UserModel::where(['zhanghao'=>$zhanghao,'store_id'=>$store_id])->select()->toArray();
            if($res_q)
            {
                $message = Lang('login.14');
                return output(ERROR_CODE_ZHYCZ,$message);
            }
            $r0 = ConfigModel::where('store_id',$store_id)->select()->toArray();
            if ($r0)
            {
                $wx_headimgurl = $r0[0]['wx_headimgurl'];//默认微信用户头像
                $headimgurl = ServerPath::getimgpath($wx_headimgurl);//默认微信用户头像
                $wx_name = $r0[0]['wx_name'];  //默认微信用户名
                $user_id1 = $r0[0]['user_id']; //默认用户名ID前缀
            }
            Db::startTrans();
            $token = Tools::getToken($store_id,$store_type);
            $mima = Tools::lock_url($mima);
            //添加用户
            $sql_u = new UserModel();
            $sql_u->store_id = $store_id;
            $sql_u->access_id = $token;
            $sql_u->user_name = $wx_name;
            $sql_u->headimgurl = $headimgurl;
            $sql_u->zhanghao = $zhanghao;
            $sql_u->mima = $mima;
            $sql_u->source = $store_type;
            $sql_u->Register_data = date('Y-m-d H:i:s');
            $sql_u->save();
            $res_u = $sql_u->id;
            if($res_u < 1)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 用户zhanghao'.$zhanghao.'添加失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }
            //更新user_id
            $user_id = $user_id1 . ($res_u + 1);//新注册的用户user_id
            $res_1 = UserModel::find($res_u);
            $res_1->user_id = $user_id;
            $res_1->save();

            //绑定角色
            $id = substr(uniqid(mt_rand(), true),0,19);
            $sql = new UserAuthorityModel();
            $sql->id = $id;
            $sql->main_id = $user_id;
            $sql->type = 1;
            $sql->role_id = $roleId;
            $sql->create_id = $operator;
            $sql->add_date = date('Y-m-d H:i:s');
            $sql->update_date = date('Y-m-d H:i:s');
            $sql->save();
            $res = $sql->id;
            if($res < 1)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 用户user_id'.$user_id.'角色绑定失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }
            Db::commit();

            $Jurisdiction->admin_record($store_id, $operator, '添加了管理员名称：' . $zhanghao, 1,2,$shop_id,$operator_id);
        }
        $message = Lang('Success');
        return output(200, $message);
    }

    //删除管理员
    public function DelBindUserAuthorityTree()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $id = $this->request->param('id');
        
        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type); // 店铺ID
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        //获取管理员信息
        $res_q = UserAuthorityModel::where('id',$id)->select()->toArray();
        if($res_q)
        {
            $user_id = $res_q[0]['main_id'];

            $res_q = UserModel::where(['user_id'=>$user_id,'store_id'=>$store_id])->select()->toArray();
            $zhanghao = $res_q[0]['zhanghao'];

            Db::startTrans();
            //删除管理员
            $sql1 = UserAuthorityModel::find($id);
            $res1 = $sql1->delete();
            if(!$res1)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 管理员id'.$id.'删除失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }
            //删除用户
            $res2 = UserModel::where('user_id',$user_id)->delete();
            if($res2 < 0)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 用户user_id'.$user_id.'删除失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }
            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator, '删除了管理员名称：' . $zhanghao, 3,2,$shop_id,$operator_id);

            $message = Lang('Success');
            return output(200, $message);
        }
        else
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }
    }

    //管理员日志
    public function GetRecord()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $shop_id = cache($access_id.'_'.$store_type);

        $exportType = $this->request->param('exportType');//是否导出
        $zhanhao = $this->request->param('zhangHao');
        $logOperationType = $this->request->param('logOperationType');
        $startdate = $this->request->param('startDate');
        $enddate = $this->request->param('endDate');
        $page = $this->request->param('pageNo');
        $pagesize = $this->request->param('pageSize');
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
        $total = 0;
        $condition = " a.source = 2 and a.mch_id = '$shop_id' and b.store_id = '$store_id' ";
        if($zhanhao != '')
        {
            $zhanhao_0 = Tools::FuzzyQueryConcatenation($zhanhao);
            $condition .= " and b.zhanghao like $zhanhao_0 ";
        }
        if($logOperationType != '')
        {
            $condition .= " and a.type = '$logOperationType' ";
        }
        if($startdate != '')
        {
            $condition .= " and a.add_date >= '$startdate' ";
        }
        if($enddate != '')
        {
            $condition .= " and a.add_date < '$enddate' ";
        }
        $sql_num = "select ifnull(count(a.id),0) as num from lkt_admin_record a left join lkt_user b on a.admin_name=b.user_id where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        $sql = "select a.*,b.zhanghao,b.user_name from lkt_admin_record a left join lkt_user b on a.admin_name=b.user_id where $condition order by a.add_date desc limit $start,$pagesize";
        $res = Db::query($sql);
        if($res)
        {
            foreach($res as $k => $v)
            {
                if($v['type'] == 1)
                { // 类型 0:登录/退出 1:添加 2:修改 3:删除 4:导出 5:启用/禁用 6:通过/拒绝 10删除订单
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
                    $v['operation_type'] = '登陆/退出';
                }
                $list[] = $v;
            }
        }
        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '序号',
                1 => '管理员账号',
                2 => '事件',
                3 => '操作时间'
            );
            $exportExcel_list = array();

            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $k+1,
                        $v['admin_name'],
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
        $message = Lang('Success');
        return output(200, $message,array('total'=>$total,'list'=>$list));
    }

    //批量删除日志
    public function DelRecord()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $shop_id = cache($access_id.'_'.$store_type);
        $ids = $this->request->param('ids');//日志id
        if($ids == '')
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }
        $id_arr = array_unique(explode(',', $ids));
        Db::startTrans();
        foreach ($id_arr as $key => $value) 
        {
            $sql = AdminRecordModel::find($value);
            $res = $sql->delete();
            if(!$res)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 日志id'.$id.'删除失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }
        }
        Db::commit();
        $message = Lang('Success');
        return output(200, $message);
    }

    //角色管理
    public function RoleList()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $shop_id = cache($access_id.'_'.$store_type);

        $role_id = $this->request->param('roleId');//角色id
        $page = $this->request->param('pageNo');
        $pagesize = $this->request->param('pageSize');

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

        $condition['main_id'] = $shop_id;
        $condition['type'] = 2;
        if($role_id != '')
        {
            $condition['id'] = $role_id;
        }

        $total = UserRoleModel::where(['main_id'=>$shop_id,'type'=>2])->count();

        $list = array();
        $res = UserRoleModel::where(['main_id'=>$shop_id,'type'=>2])->order('add_date','desc')->limit($start,$pagesize)->select()->toArray();
        if($res)
        {
            $list = $res;
        }
        $data = array('list'=>$list,'total'=>$total);
        if($role_id != '')
        {
            //获取角色绑定的一级菜单
            $sql = "select distinct m.* from lkt_authority_mapping as a left join lkt_menu as m on a.menu_id = m.id where m.is_display = 0 and m.sid = 0";
            $currentRoleMenuList = Db::query($sql);
            $data = array('list'=>$list,'total'=>$total,'currentRoleMenuList'=>$currentRoleMenuList);
        }
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    //添加、编辑角色
    public function AddRole()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $role_arr = $this->request->param('roleIdTree');//菜单id
        $name = $this->request->param('name');//角色名称
        $describe = $this->request->param('describe');//角色描述
        $id = $this->request->param('id');//角色id
        
        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type); // 店铺ID
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        if($role_arr == '')
        {
            $message = Lang('role.15');
            return output(ERROR_CODE_QXZQX, $message);
        }
        if($name == '')
        {
            $message = Lang('role.11');
            return output(ERROR_CODE_JSMCBNWK, $message);
        }
        // if($describe == '')
        // {
        //     $message = Lang('role.16');
        //     return output(ERROR_CODE_JSMSBNWK, $message);
        // }

        if($id)
        {
            //判断名称是否存在
            $res_n = UserRoleModel::where(['main_id'=>$shop_id,'type'=>2,'name'=>$name])->where('id','<>',$id)->select()->toArray();
            if($res_n)
            {
                $message = Lang('role.14');
                return output(ERROR_CODE_JSYCZ, $message);
            }
            Db::startTrans();
            //修改角色
            $sql1 = UserRoleModel::find($id);
            $sql1->main_id = $shop_id;
            $sql1->type = 2;
            $sql1->name = $name;
            $sql1->text = $describe;
            $sql1->update_date = date('Y-m-d H:i:s');
            $res = $sql1->save();
            if(!$res)
            {   
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色id'.$id.'编辑失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }
            //先清除角色绑定菜单
            $sql_d = AuthorityMappingModel::where('role_id',$id)->delete();
            if($sql_d < 0)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色id'.$id.'清除绑定菜单失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }
            $roleMenusid = array_unique(explode(',', $role_arr));
            foreach ($roleMenusid as $key => $value) 
            {
                $rid = substr(uniqid(mt_rand(), true),0,19);
                $sql2 = new AuthorityMappingModel();
                $sql2->id = $rid;
                $sql2->role_id = $id;
                $sql2->menu_id = $value;
                $sql2->add_date = date('Y-m-d H:i:s');
                $res2 = $sql2->save();
                if(!$res2)
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单id'.$value.'绑定角色失败';
                    $this->Log($Log_content);
                    $message = Lang('operation failed');
                    return output(ERROR_CODE_CZSB, $message);
                }
            }
            Db::commit();

            $Jurisdiction->admin_record($store_id, $operator, '修改了角色名称：' . $name, 2,2,$shop_id,$operator_id);

            $message = Lang('Success');
            return output(200, $message);
        }
        else
        {
            //判断名称是否存在
            $res_n = UserRoleModel::where(['main_id'=>$shop_id,'type'=>2,'name'=>$name])->select()->toArray();
            if($res_n)
            {
                $message = Lang('role.14');
                return output(ERROR_CODE_JSYCZ, $message);
            }
            $roleMenusid = array_unique(explode(',', $role_arr));
            Db::startTrans();
            //添加角色
            $id = substr(uniqid(mt_rand(), true),0,19);
            $sql1 = new UserRoleModel();
            $sql1->id = $id;
            $sql1->main_id = $shop_id;
            $sql1->type = 2;
            $sql1->name = $name;
            $sql1->text = $describe;
            $sql1->add_date = date('Y-m-d H:i:s');
            $sql1->update_date = date('Y-m-d H:i:s');
            $res = $sql1->save();
            if(!$res)
            {   
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色id'.$id.'添加失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }
            foreach ($roleMenusid as $key => $value) 
            {
                $rid = substr(uniqid(mt_rand(), true),0,19);
                $sql2 = new AuthorityMappingModel();
                $sql2->id = $rid;
                $sql2->role_id = $id;
                $sql2->menu_id = $value;
                $sql2->add_date = date('Y-m-d H:i:s');
                $res2 = $sql2->save();
                if(!$res2)
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单id'.$value.'绑定角色失败';
                    $this->Log($Log_content);
                    $message = Lang('operation failed');
                    return output(ERROR_CODE_CZSB, $message);
                }
            }
            Db::commit();

            $Jurisdiction->admin_record($store_id, $operator, '添加了角色名称：' . $name, 1,2,$shop_id,$operator_id);

            $message = Lang('Success');
            return output(200, $message);
        }
    }

    //删除角色
    public function DelRole()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type); // 店铺ID
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        $role_id = $this->request->param('id');//角色id
        //判断角色是否有人使用
        $res_q = UserAuthorityModel::where('role_id',$role_id)->select()->toArray();
        if($res_q)
        {
            $message = Lang('role.17');
            return output(ERROR_CODE_QXJCBDGXZJXSCCZ, $message);
        }

        Db::startTrans();
        $res_u = UserRoleModel::where('id',$role_id)->select()->toArray();
        $name = $res_u[0]['name'];

        $sql = UserRoleModel::find($role_id);
        $res = $sql->delete();
        if(!$res)
        {
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色id'.$id.'删除失败';
            $this->Log($Log_content);
            $message = Lang('operation failed');
            return output(ERROR_CODE_CZSB, $message);
        }
        //删除角色绑定菜单
        $res_m = AuthorityMappingModel::where('role_id',$role_id)->delete();
        if($res_m < 0)
        {
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色id'.$id.'绑定菜单删除失败';
            $this->Log($Log_content);
            $message = Lang('operation failed');
            return output(ERROR_CODE_CZSB, $message);
        }
        Db::commit();

        $Jurisdiction->admin_record($store_id, $operator, '删除了角色名称：' . $name, 3,2,$shop_id,$operator_id);

        $message = Lang('Success');
        return output(200, $message);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("mch/role.log",$Log_content);
        return;
    }
}

