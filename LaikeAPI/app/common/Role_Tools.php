<?php
namespace app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\Plugin\Plugin;
use app\common\Jurisdiction;

use app\admin\model\CoreMenuModel;
use app\admin\model\AdminModel;
use app\admin\model\RoleModel;
use app\admin\model\RoleMenuModel;
use app\admin\model\GuideMenuModel;
//角色权限类
class Role_Tools
{
    // 获取商城菜单
    public static function see_menu_0($store_id)
    {
        $data = array();
        $list = array();
        $id_str = '';
        $sql_admin = "select role from lkt_admin where store_id = '$store_id' ";
        $r_admin = Db::query($sql_admin);
        if($r_admin)
        {
            $role = $r_admin[0]['role'];
            $r0 = RoleMenuModel::where('role_id',$role)->field('menu_id')->select()->toArray();
            if($r0)
            {
                foreach ($r0 as $k0 => $v0)
                {
                    $id_str .= $v0['menu_id'] . ",";
                }
                $id_str = trim($id_str,',');
            }
        }

        // 查询菜单表(模块名称、模块标识、模块描述)
        $r = CoreMenuModel::where(['s_id'=>0,'type'=>1,'recycle'=>0])->whereIn('id',$id_str)->order('sort','desc')->order('id','asc')->field('id,title,s_id')->select()->toArray();
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $v['checked'] = false; // 定义没选中
                $id_0 = $v['id'];

                $v['children'] = Role_Tools::see_menu_subordinate($id_0,$list);
                $data[] = $v;
            }
        }

        return $data;
    }
    
    // 根据角色ID，标记选中
    public static function see_menu_1($store_id,$r0,$list0)
    {
        $id_list = array();
        foreach ($r0 as $k => $v)
        {
            $id_list[] = $v['menu_id'];
        }

        $list = array();
        foreach ($list0 as $k0 => $v0)
        {
            $is_checked = false;
            $list1 = array();
            foreach ($v0['children'] as $k1 => $v1)
            {
                $list2 = array();
                foreach ($v1['children'] as $k2 => $v2)
                {
                    if(in_array($v2['id'],$id_list))
                    {
                        $v2['checked'] = true;
                        $is_checked = true;
                    }
                    $list2[] = $v2;
                }
                if(in_array($v1['id'],$id_list))
                {
                    $v1['checked'] = true;
                }
                $v1['children'] = $list2;
                $list1[$k1] = $v1;
            }
            
            if(in_array($v0['id'],$id_list))
            {
                $v0['checked'] = true;
            }
            if($is_checked == true)
            {
                $v0['checked'] = true;
            }
            $v0['children'] = $list1;
            $list[] = $v0;
        }
        return $list;
    }

    // 菜单树结构-查看(商城角色管理)
    public static function see_menu($list)
    {
        $data = array();
        $list1 = array();
        $list3 = array();
        // 查询菜单表(模块名称、模块标识、模块描述)
        $r = CoreMenuModel::where(['s_id'=>0,'type'=>1,'recycle'=>0])->order('sort','desc')->order('id','asc')->field('id,title,s_id')->select()->toArray();
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $v['checked'] = false; // 定义没选中
                $id_0 = $v['id'];
                if ($list != '')
                {
                    foreach ($list as $k_0 => $v_0)
                    {
                        if ($id_0 == $v_0['menu_id'])
                        {
                            $v['checked'] = true; // 选中
                        }
                    }
                }
                $v['children'] = Role_Tools::see_menu_subordinate($id_0,$list);
                $data[] = $v;
            }
        }

        return $data;
    }

    // 查看角色
    public static function see_menu_subordinate($id,$list)
    {
        $array = array();
        $r = CoreMenuModel::where(['s_id'=>$id,'type'=>1,'recycle'=>0])->order('sort','desc')->order('id','asc')->field('id,title,s_id')->select()->toArray();
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $v['checked'] = false; // 定义没选中
                $id_1 = $v['id'];
                if ($list != '')
                {
                    foreach ($list as $k1_1 => $v1_1)
                    {
                        if ($id_1 == $v1_1['menu_id'])
                        {
                            $v['checked'] = true; // 选中
                        }
                    }
                }
                $v['children'] = Role_Tools::see_menu_subordinate($id_1,$list);
                $array[] = $v;
            }
        }
        return $array;
    }

    // 绑定信息（控制台商城管理）
    public static function bangding0($array)
    {
        $admin_name = $array['admin_name'];
        $id = $array['id'];
        $list = $array['list'];
        $list1 = $array['list1'];
        $Plugin = new Plugin();
        $lktlog = new LaiKeLogUtils();

        Db::startTrans();

        $store_list = array();
        if($list1 != array() && $list1)
        { // 已绑定得用户
            // 根据角色ID，查询角色所绑定的管理员信息
            $r0_0 = AdminModel::where(['role'=>$id,'recycle'=>0])->field('id,store_id')->select()->toArray();
            if($r0_0)
            {
                foreach ($r0_0 as $k => $v)
                {
                    if(!in_array($v['id'],$list1))
                    { // 当已绑定的商城管理员ID，不存在改变后的管理员ID数组里
                        $id1 = $v['id'];
                        $store_id1 = $v['store_id']; // 商户ID
                        $store_list[] = $v['store_id'];
                        // 根据商户管理员ID，修改管理员角色数据

                        $sql0_1 = array('id'=>$id1,'recycle'=>0);
                        $r0_1 = Db::name('admin')->where($sql0_1)->update(['role'=>'']);
                        if($r0_1 <= 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 取消商户绑定角色失败！参数:'.json_encode($sql0_1);
                            $lktlog->Log("app/Role_Tools.log",$Log_content);
                            Db::rollback();
                            echo json_encode(array('code'=>109,'message'=>'未知原因，绑定失败！'));
                            exit;
                        }
                        else
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 取消商户绑定角色，商户ID为'.$id1;
                            $lktlog->Log("app/Role_Tools.log",$Log_content);
                        }

                        // 根据商户ID和角色ID，删除导览菜单
                        $sql_guide_menu0 = array('role_id'=>$id,'store_id'=>$store_id1);
                        $r_guide_menu0 = Db::table('lkt_guide_menu')->where($sql_guide_menu0)->delete();
                        // if($r_guide_menu0)
                        // {
                        //     $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 取消商户绑定角色，删除导览菜单成功！参数:'.json_encode($sql_guide_menu0);
                        //     $lktlog->Log("app/Role_Tools.log",$Log_content);
                        // }
                        // else
                        // {
                        //     $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 取消商户绑定角色,删除导览菜单失败！参数:'.json_encode($sql_guide_menu0);
                        //     $lktlog->Log("app/Role_Tools.log",$Log_content);
                        //     Db::rollback();
                        //     echo json_encode(array('code'=>109,'message'=>'失败!'));
                        //     exit;
                        // }
                    }
                }
            }
        }
        else
        { // 已绑定得用户,全部取消
            // 根据角色ID，查询角色所绑定的商户
            $r0_0 = AdminModel::where(['role'=>$id,'recycle'=>0])->field('id,store_id')->select()->toArray();
            if($r0_0)
            {
                foreach ($r0_0 as $k => $v)
                {
                    $id1 = $v['id']; // 商户管理员ID
                    $store_id1 = $v['store_id']; // 商户ID
                    // 根据商户管理员ID，修改管理员角色数据
                    $sql0_1 = array('id'=>$id1,'recycle'=>0);
                    $r0_1 = Db::name('admin')->where($sql0_1)->update(['role'=>'']);
                    if($r0_1 <= 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 取消商户绑定角色失败！参数:'.json_encode($sql0_1);
                        $lktlog->Log("app/Role_Tools.log",$Log_content);
                        Db::rollback();
                        echo json_encode(array('code'=>109,'message'=>'未知原因，绑定失败！'));
                        exit;
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 取消商户绑定角色，商户ID为'.$id1;
                        $lktlog->Log("app/Role_Tools.log",$Log_content);
                    }
                    // 根据商户ID和角色ID，删除导览菜单
                    $sql_guide_menu0 = array('role_id'=>$id,'store_id'=>$store_id1);
                    $r_guide_menu0 = Db::table('lkt_guide_menu')->where($sql_guide_menu0)->delete();
                    // if($r_guide_menu0 > 0)
                    // {
                    //     $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 取消商户绑定角色，删除导览菜单成功！参数:'.json_encode($sql_guide_menu0);
                    //     $lktlog->Log("app/Role_Tools.log",$Log_content);
                    // }
                    // else
                    // {
                    //     $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 取消商户绑定角色,删除导览菜单失败！参数:'.json_encode($sql_guide_menu0);
                    //     $lktlog->Log("app/Role_Tools.log",$Log_content);
                    //     Db::rollback();
                    //     echo json_encode(array('code'=>109,'message'=>'失败!'));
                    //     exit;
                    // }
                }
            }
        }

        // 需要绑定得商户
        if($list != array() && $list)
        {
            $menu_list = array();
            $add_menu_list = array();
            // 根据角色ID，查询菜单信息
            $sql0_0 = "select a.module,b.menu_id,b.is_display,b.guide_sort from lkt_core_menu as a left join lkt_role_menu as b on a.id = b.menu_id where role_id = '$id'";
            $r0_0 = Db::query($sql0_0);
            if($r0_0)
            {
                foreach ($r0_0 as $k => $v)
                {
                    $menu_list1[] = $v['module'];
                    $add_menu_list[] = array('menu_id'=>$v['menu_id'],'is_display'=>$v['is_display'],'guide_sort'=>$v['guide_sort']);
                }
                $menu_list = array_unique($menu_list1); // 菜单模块标识
            }

            foreach ($list as $k => $v)
            {
                // 根据管理员ID，查询管理员信息
                $r0_1 = AdminModel::where(['id'=>$v,'recycle'=>0])->field('role,store_id')->select()->toArray();
                if($r0_1)
                {
                    foreach ($r0_1 as $k1 => $v1)
                    {
                        $store_id = $v1['store_id'];

                        $Plugin->Modifying_plugins($store_id,$menu_list); // 修改插件

                        foreach ($add_menu_list as $k_guide => $v_guide)
                        {
                            $menu_id = $v_guide['menu_id'];
                            $is_display = $v_guide['is_display'];
                            $guide_sort = $v_guide['guide_sort'];

                            $sql_guide_menu0 = array('store_id'=>$store_id,'role_id'=>$id,'menu_id'=>$menu_id,'is_display'=>$is_display,'guide_sort'=>$guide_sort,'add_date'=>date('Y-m-d H:i:s'));
                            $r_guide_menu0 = Db::name('guide_menu')->insert($sql_guide_menu0);
                            if($r_guide_menu0 < 1)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 绑定商户角色,添加导览菜单失败！参数:'.json_encode($sql_guide_menu0);
                                $lktlog->Log("app/Role_Tools.log",$Log_content);
                                Db::rollback();
                                echo json_encode(array('code'=>109,'message'=>'未知原因，绑定失败！'));
                                exit;
                            }
                        }
                    }
                }
                // 根据管理员ID,修改管理员角色
                $sql0 = array('recycle' => 0,'id' => $v);
                $r0 = Db::name('admin')->where($sql0)->update(['role' => $id]);
                if($r0 > 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 绑定商户角色，商户ID为'.$v;
                    $lktlog->Log("app/Role_Tools.log",$Log_content);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 绑定商户角色失败！参数:'.json_encode($sql0);
                    $lktlog->Log("app/Role_Tools.log",$Log_content);
                    Db::rollback();
                    echo json_encode(array('code'=>109,'message'=>'未知原因，绑定失败！'));
                    exit;
                }
            }
        }

        if($store_list != array())
        {
            foreach ($store_list as $k => $v)
            {
                $Plugin->Modifying_plugins($v,array()); // 修改插件
            }
        }
        Db::commit();
        return;
    }

    // 角色列表（控制台权限管理、商城角色管理）
    public static function role_list($array)
    {   
        $id = $array['id'];
        $store_id = $array['store_id'];
        $status = $array['status'];
        $start = $array['start'];
        $pagesize = $array['pagesize'];
        $list = array();

        $total = 0;
        if($status == 1)
        {
            $condition['status'] = 1;
        }
        else
        { 
            $condition['store_id'] = $store_id;  
        }
        if($id)
        {
            $condition['id'] = $id;
        }

        // 查询商城角色信息
        $total = RoleModel::where($condition)->count();
        // 查询商城角色信息
        $r1 = RoleModel::where($condition)->order('add_date','desc')->limit($start,$pagesize)->select()->toArray();
        if($r1)
        {
            foreach ($r1 as $k1 => $v1)
            {
                if($status == 1)
                {
                    $id = $v1['id'];

                    $v1['bindAdminList'] = array();
                    $sql2 = "select b.name,a.id,b.mobile from lkt_admin as a left join lkt_customer as b on a.store_id = b.id where a.type = 1 and a.role = '$id' and a.recycle = 0 and b.recycle = 0 ";
                    $r2 = Db::query($sql2);
                    if($r2)
                    {
                        foreach ($r2 as $k2 => $v2)
                        {
                            $v1['bindAdminList'][$k2]['name'] = $v2['name'];
                            $v1['bindAdminList'][$k2]['id'] = $v2['id'];
                            $v1['bindAdminList'][$k2]['tel'] = $v2['mobile'];
                        }
                    }
                }
                $list[] = $v1;
            }
        }
        $data = array('total'=>$total,'list'=>$list);
        return $data;
    }

    // 添加角色（控制台权限管理、商城角色管理）
    public static function add_role($array)
    {
        $admin_id = $array['admin_id'];
        $admin_name = $array['admin_name'];
        $store_id = $array['store_id'];
        $status = $array['status'];
        $name = $array['name'];
        $permissions = $array['permissions'];
        $role_describe = $array['role_describe'];
        $operator_id = 0;
        $operator = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();
        
        Db::startTrans();
        if ($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色名称不能为空！';
            $lktlog->log("common/role.log",$Log_content);
            $message = Lang("role.11");
            echo json_encode(array('code'=>ERROR_CODE_JSMCBNWK,'message'=>$message));
            exit;
        }
        else
        {
            // if(preg_match("/[\'.,:;*?~`!@bai#$%^&+=)(<>{}]|du\]|\[|\/|\\\|\"|\|/",$name)){ //不允zhi许特殊dao字符
            if(preg_match("[\'.,`+=;@_!#$%^&*()<>?/|}{~:]",$name))
            { //不允zhi许特殊dao字符
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 存在特殊字符！';
                $lktlog->log("common/role.log",$Log_content);
                $message = Lang("role.12");
                echo json_encode(array('code'=>ERROR_CODE_JSMCBNWK,'message'=>$message));
                exit;
            }
            if(strlen($name) > 60)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色名称不能超过20个中文字长度！';
                $lktlog->log("common/role.log",$Log_content);
                $message = Lang("role.13");
                echo json_encode(array('code'=>ERROR_CODE_JSMCBNCGGZWZZD,'message'=>$message));
                exit;
            }
            else
            {
                $r = RoleModel::where(['store_id'=>$store_id,'name'=>$name])->field('id')->select()->toArray();
                if ($r)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色名称已经存在！';
                    $lktlog->log("common/role.log",$Log_content);
                    $message = Lang("role.14");
                    echo json_encode(array('code'=>ERROR_CODE_JSMCYCZ,'message'=>$message));
                    exit;
                }
            }
        }
        if(!$permissions)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择绑定权限！';
            $lktlog->log("common/role.log",$Log_content);
            $message = Lang("role.15");
            echo json_encode(array('code'=>ERROR_CODE_QXZBDQX,'message'=>$message));
            exit;
        }
        else
        {
            $permissions = Role_Tools::childrenMenuInRoleNotChoose($permissions);
        }
        $title_list = array('商品','商品分类','品牌','运费设置','商品列表','库存管理','订单','订单列表','退货列表','评价管理','订单设置','会员','会员等级','会员添加','会员列表','会员设置','设置','支付','短信','协议','地址','公告');

        // if ($role_describe == '')
        // {
        //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色描述不能为空';
        //     $lktlog->log("common/role.log",$Log_content);
        //     $message = Lang("role.16");
        //     echo json_encode(array('code'=>ERROR_CODE_JSMSBNWK,'message'=>$message));
        //     exit;
        // }
        // 添加一条数据
        $sql0 = new RoleModel();
        $sql0->name = $name;
        $sql0->status = $status;
        $sql0->admin_id = $admin_id;
        $sql0->store_id = $store_id;
        $sql0->add_date = date("Y-m-d H:i:s");
        $sql0->role_describe = $role_describe;
        $sql0->save();
        $r0 = $sql0->id; // 得到添加数据的id
        if ($r0 > 0)
        {
            foreach ($permissions as $k => $v)
            {
                $is_display = 0;
                $r1_0 = CoreMenuModel::where('id',$v)->field('title')->select()->toArray();
                if($r1_0)
                {
                    if(in_array($r1_0[0]['title'],$title_list))
                    {
                        $is_display = 1;
                    }
                }
                $sql1 = new RoleMenuModel();
                $sql1->role_id = $r0;
                $sql1->menu_id = $v;
                $sql1->is_display = $is_display;
                $sql1->add_date = date("Y-m-d H:i:s");
                $sql1->save();
                $r1 = $sql1->id;
                if($r1 <= 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 添加角色权限菜单失败！r0:'.$r0;
                    $lktlog->log("common/role.log",$Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '添加了角色名称：'.$name.'失败',1,1,0,$operator_id);
                    $message = Lang("operation failed");
                    echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                    exit;
                }
            }
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 添加角色成功';
            $lktlog->log("common/role.log",$Log_content);
            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator, '添加了角色名称：'.$name,1,1,0,$operator_id);
            $message = Lang("Success");
            echo json_encode(array('code'=>200,'message'=>$message));
            exit;
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 添加角色失败！name:'.$name;
            $lktlog->log("common/role.log",$Log_content);
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '添加了角色名称：'.$name.'失败',1,1,0,$operator_id);
            $message = Lang("operation failed");
            echo json_encode(array('code'=>109,'message'=>$message));
            exit;
        }
    }

    /**
     * 子菜单在角色权限表但是父菜单没有选中的情况 需要吧父菜单的id换进去
     * @param $db
     * @param $roleMenusid
     * @return array
     */
    public static function childrenMenuInRoleNotChoose(&$roleMenusid)
    {
        //临时数组 存放没有被选择的菜单id
        $menuTmp = array();
        $levels = array(1, 2, 3, 4);
        $roleMenusid = explode(',', $roleMenusid);
        foreach ($levels as $lv)
        {
            $res = CoreMenuModel::where(['level'=>$lv,'is_core'=>0,'recycle'=>0])->select()->toArray();
            foreach ($res as $menu)
            {
                $menuid = $menu['id'];
                if (!in_array($menuid, $roleMenusid))
                {
                    $sonmenusql = "SELECT t3.childId FROM
                                ( SELECT *, IF (find_in_set(t1.s_id, @p) > 0 , @p:= concat(@p,',',id),0) AS childId FROM
                                (SELECT id,s_id FROM   lkt_core_menu as  t     WHERE   t.recycle = 0 and is_core = 0 and recycle = 0  ORDER BY id) t1,
                                (SELECT @p:= $menuid ) t2 ) t3 WHERE  childId != 0 ";
                    $sonmenuids = Db::query($sonmenusql);
                    if ($sonmenuids)
                    {
                        $num = count($sonmenuids) - 1;
                        $childId = ',' . $sonmenuids[$num]['childId'] . ',';
                        foreach ($roleMenusid as $k => $v)
                        {
                            if (strpos($childId, (',' . $v . ',')) !== false)
                            {
                                //存放没有被主动选择的菜单id
                                array_push($menuTmp, $menuid);
                            }
                        }
                        //合并未主动被选择的菜单id
                        $roleMenusid = array_unique(array_merge($roleMenusid, $menuTmp));
                        unset($childId);
                    }
                }
            }
        }
        return $roleMenusid;
    }

    // 编辑角色（控制台权限管理、商城角色管理）
    public static function edit_role($array)
    {
        $id = $array['id'];
        $admin_name = $array['admin_name'];
        $store_id = $array['store_id'];
        $name = $array['name'];
        $permissions = $array['permissions'];
        $role_describe = $array['role_describe'];
        $operator_id = 0;
        $operator = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $Plugin = new Plugin();
        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();
        Db::startTrans();

        if ($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色名称不能为空';
            $lktlog->log("common/role.log",$Log_content);
            $message = Lang("role.11");
            echo json_encode(array('code'=>ERROR_CODE_JSMCBNWK,'message'=>$message));
            exit;
        }
        else
        {
            // if(preg_match("/[\'.,:;*?~`!@bai#$%^&+=)(<>{}]|du\]|\[|\/|\\\|\"|\|/",$name))
            if(preg_match("[\'.,`+=;@_!#$%^&*()<>?/|}{~:]",$name)) 
            { //不允zhi许特殊dao字符
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 存在特殊字符';
                $lktlog->log("common/role.log",$Log_content);
                $message = Lang("role.12");
                echo json_encode(array('code'=>ERROR_CODE_JSMCBNWK,'message'=>$message));
                exit;
            }
            if(strlen($name) > 60)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色名称不能超过20个中文字长度';
                $lktlog->log("common/role.log",$Log_content);
                $message = Lang("role.13");
                echo json_encode(array('code'=>ERROR_CODE_JSMCBNCGGZWZZD,'message'=>$message));
                exit;
            }
            else
            {
                $r = RoleModel::where(['status'=>1,'name'=>$name,'store_id'=>$store_id])
                             ->where('id','<>',$id)
                             ->field('id')
                             ->select()
                             ->toArray();
                if ($r)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色名称已经存在';
                    $lktlog->log("common/role.log",$Log_content);
                    $message = Lang("role.14");
                    echo json_encode(array('code'=>ERROR_CODE_JSMCYCZ,'message'=>$message));
                    exit;
                }
            }
        }

        if(!$permissions)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择绑定权限';
            $lktlog->log("common/role.log",$Log_content);
            $message = Lang("role.15");
            echo json_encode(array('code'=>ERROR_CODE_QXZBDQX,'message'=>$message));
            exit;
        }
        else
        {
            $permissions = Role_Tools::childrenMenuInRoleNotChoose($permissions);
        }
        $permissions0 = array();
        $menu_list0 = array();
        // 根据角色ID，查询角色菜单
        $r0_0 = RoleMenuModel::where('role_id',$id)->field('menu_id,is_display,guide_sort')->select()->toArray();
        if($r0_0)
        {
            foreach ($r0_0 as $k0_0 => $v0_0)
            { // 循环修改前菜单信息
                $menu_list0[] = $v0_0['menu_id'];
            }

            foreach ($permissions as $k => $v)
            { // 循环修改后的菜单id
                if(in_array($v,$menu_list0))
                { // 修改后的菜单ID，存在修改前的菜单ID
                    foreach ($menu_list0 as $k1 => $v1)
                    { // 循环修改后的菜单id
                        if($v == $v1)
                        {
                            $permissions0[] = array('menu_id'=>$v,'is_display'=>$r0_0[$k1]['is_display'],'guide_sort'=>$r0_0[$k1]['guide_sort']);
                            break;
                        }
                    }
                }
                else
                {
                    $permissions0[] = array('menu_id'=>$v,'is_display'=>0,'guide_sort'=>0);
                }
            }
        }
        // if ($role_describe == '')
        // {
        //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 角色描述不能为空';
        //     $lktlog->log("common/role.log",$Log_content);
        //     $message = Lang("role.16");
        //     echo json_encode(array('code'=>ERROR_CODE_JSMSBNWK,'message'=>$message));
        //     exit;
        // }

        //更新数据表
        // $sql = RoleModel::where(['store_id'=>$store_id,'id'=>$id])->find();
        $sql = RoleModel::where(['id'=>$id])->find();
        $sql->name = $name;
        $sql->role_describe = $role_describe;
        $r = $sql->save();
        if (!$r)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改角色信息失败！id:'.$id;
            $lktlog->log("common/role.log",$Log_content);
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '修改了角色ID：'.$id.'失败',2,1,0,$operator_id);
            $message = Lang("operation failed");
            echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
            exit;
        }
        else
        {
            $r_del = RoleMenuModel::where('role_id',$id)->delete();
            if($r_del)
            {
                $menu_list = array();
                foreach ($permissions0 as $k => $v)
                {
                    $menu_id = $v['menu_id'];
                    $is_display = $v['is_display'];
                    $guide_sort = $v['guide_sort'];

                    $r0 = CoreMenuModel::where('id',$menu_id)->field('module')->select()->toArray();
                    if($r0)
                    {
                        $menu_list1[] = $r0[0]['module'];
                    }
                    else
                    {
                        $menu_list1 = array();
                    }
                    $menu_list = array_unique($menu_list1);

                    $sql1 = new RoleMenuModel();
                    $sql1->role_id = $id;
                    $sql1->menu_id = $menu_id;
                    $sql1->is_display = $is_display;
                    $sql1->guide_sort = $guide_sort;
                    $sql1->add_date = date("Y-m-d H:i:s");
                    $sql1->save();
                    $r1 = $sql1->id;
                    if($r1 <= 0)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 添加角色权限菜单失败！role_id:'.$id;
                        $lktlog->log("common/role.log",$Log_content);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '修改了角色ID：'.$id.'失败',2,1,0,$operator_id);
                        $message = Lang("operation failed");
                        echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                        exit;
                    }
                }
                $r0 = AdminModel::where('role',$id)->field('store_id')->select()->toArray();
                if($r0)
                {
                    foreach ($r0 as $k => $v)
                    {
                        $store_id = $v['store_id'];
                        $Plugin->Modifying_plugins($store_id,$menu_list); // 修改插件

                        // 根据商户ID和角色ID，删除导览菜单
                        $sql_guide_menu0 = "delete from lkt_guide_menu where role_id = '$id' and store_id = '$store_id' ";
                        $r_guide_menu0 = Db::execute($sql_guide_menu0);
                        if($r_guide_menu0 >= 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改角色，删除导览菜单成功！sql:' . $sql_guide_menu0;
                            $lktlog->log("common/role.log",$Log_content);
                        }
                        else
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改角色,删除导览菜单失败！sql:'.$sql_guide_menu0;
                            $lktlog->log("common/role.log",$Log_content);
                            Db::rollback();
                            $Jurisdiction->admin_record($store_id, $operator, '修改了角色ID：'.$id.'失败',2,1,0,$operator_id);
                            $message = Lang("operation failed");
                            echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                            exit;
                        }

                        foreach ($permissions0 as $k0 => $v0)
                        {
                            $menu_id = $v0['menu_id'];
                            $is_display = $v0['is_display'];
                            $guide_sort = $v0['guide_sort'];

                            $sql_guide_menu0 = new GuideMenuModel();
                            $sql_guide_menu0->store_id = $store_id;
                            $sql_guide_menu0->role_id = $id;
                            $sql_guide_menu0->menu_id = $menu_id;
                            $sql_guide_menu0->is_display = $is_display;
                            $sql_guide_menu0->guide_sort = $guide_sort;
                            $sql_guide_menu0->add_date = date("Y-m-d H:i:s");
                            $sql_guide_menu0->save();
                            $r_guide_menu0 = $sql_guide_menu0->id;
                            if($r_guide_menu0 < 1)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改角色,添加导览菜单失败！role_id:'.$id;
                                $lktlog->log("common/role.log",$Log_content);
                                Db::rollback();
                                $Jurisdiction->admin_record($store_id, $operator, '修改了角色ID：'.$id.'失败',2,1,0,$operator_id);
                                $message = Lang("operation failed");
                                echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                                exit;
                            }
                        }
                    }
                }

                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改角色ID为 '.$id.'成功';
                $lktlog->log("common/role.log",$Log_content);
                Db::commit();
                $Jurisdiction->admin_record($store_id, $operator, '修改了角色ID：'.$id,2,1,0,$operator_id);
                $message = Lang("Success");
                echo json_encode(array('code'=>200,'message'=>$message));
                exit;
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 删除角色菜单失败！role_id:'.$id;
                $lktlog->log("common/role.log",$Log_content);
                Db::rollback();
                $message = Lang("operation failed");
                echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                exit;
            }
        }
    }

    // 删除角色（控制台权限管理、商城角色管理）
    public static function del_role($array)
    {
        $store_id = $array['store_id'];
        $admin_name = $array['admin_name'];
        $status = $array['status'];
        $id = $array['id'];
        $operator_id = 0;
        $operator = '';
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        if(isset($array['operator']))
        {
            $operator = $array['operator'];
        }
        $Plugin = new Plugin();
        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();

        // 1.开启事务
        Db::startTrans();
        $condition['recycle'] = 0;
        $condition['role'] = $id;
        if($status == 1)
        { // 查询所有没有回收的客户账号
            $condition['type'] = 1;
        }
        else
        { // 查询所有没有回收的客户账号
            $condition['store_id'] = $store_id;
        }
        $r_admin = AdminModel::where($condition)->field('role')->select()->toArray();
        if($r_admin)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 该角色正在使用';
            $lktlog->log("common/role.log",$Log_content);
            $message = Lang("role.17");
            echo json_encode(array('code'=>ERROR_CODE_CSCW,'message'=>$message));
            exit;
        }

        $sql = RoleModel::find($id);
        $res = $sql->delete();
        if($res)
        {
            $sql_del = "delete from lkt_role_menu where role_id = '$id'";
            $r_del = Db::execute($sql_del);
            if($r_del <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 删除角色菜单失败！sql:'.$sql_del;
                $lktlog->log("common/role.log",$Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '删除了角色ID：'.$id.'失败',3,1,0,$operator_id);
                $message = Lang("operation failed");
                echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                exit;
            }
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 删除角色ID为'.$id.'成功';
            $lktlog->log("common/role.log",$Log_content);
            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator, '删除了角色ID：'.$id,3,1,0,$operator_id);
            $message = Lang("Success");
            echo json_encode(array('code'=>200,'message'=>$message));
            exit;
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 删除角色失败！id:'.$id;
            $lktlog->log("common/role.log",$Log_content);
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '删除了角色ID：'.$id.'失败',3,1,0,$operator_id);
            $message = Lang("operation failed");
            echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
            exit;
        }
    }

    // 菜单树结构
    public static function menu($type,$lang_code, $list, $menu_id)
    {
        $data = array();
        $disabled = false;
        $str = "id,s_id,title,image,image1,module,sort,level,is_core,is_plug_in,type,add_time,recycle,is_display,guide_sort,guide_name,is_button,is_tab,lang_code,country_num";
        // 查询菜单表(模块名称、模块标识、模块描述)
        $r = CoreMenuModel::where(['s_id'=>0,'type'=>1,'recycle'=>0,'lang_code'=>$lang_code])->order(['type','id','sort'])->field($str)->select()->toArray();
        if ($r)
        {   
            foreach ($r as $k => $v)
            {
                $v['checked'] = false; // 定义没选中
                $v['field'] = '';
                $id = $v['id'];
                
                if($type == 0)
                {
                    if ($list != array())
                    {
                        foreach ($list as $k1 => $v1)
                        {
                            if ($id == $v1['menu_id'])
                            {
                                $v['checked'] = true; // 选中
                            }
                        }
                    }
                    $v['children'] = Role_Tools::menu_subordinate0($id,$lang_code,$list);
                    $data[] = $v;
                }
                else
                {
                    $v['children'] = Role_Tools::menu_subordinate1($id,$lang_code,$list,$menu_id);
                    $data[] = $v;
                }
            }
        }

        $list4 = json_encode($data);

        return $list4;
    }

    //编辑角色
    public static function menu_subordinate0($id,$lang_code,$list)
    {
        $str = "id,s_id,title,image,image1,module,sort,level,is_core,is_plug_in,type,add_time,recycle,is_display,guide_sort,guide_name,is_button,is_tab,lang_code,country_num,url,briefintroduction,action";
        $r = CoreMenuModel::where(['s_id'=>$id,'type'=>1,'recycle'=>0,'lang_code'=>$lang_code])->order(['sort','id'])->field($str)->select()->toArray();
        if ($r)
        {   
            foreach ($r as $k => $v)
            {
                $r[$k]['checked'] = false; // 定义没选中
                $r[$k]['field'] = '';
                $id_0 = $v['id'];

                if ($list != array())
                {
                    foreach ($list as $k1 => $v1)
                    {
                        if ($id_0 == $v1['menu_id'])
                        {
                            $r[$k]['checked'] = true; // 选中
                        }
                    }
                }
                $r[$k]['children'] = Role_Tools::menu_subordinate0($id_0,$lang_code,$list);
            }
        }
        return $r;
    }

    //添加角色
    public static function menu_subordinate1($id,$lang_code,$list,$menu_id)
    {
        $array = array();
        $str = "id,s_id,title,image,image1,module,sort,level,is_core,is_plug_in,type,add_time,recycle,is_display,guide_sort,guide_name,is_button,is_tab,lang_code,country_num";
        $r = CoreMenuModel::where(['s_id'=>$id,'type'=>1,'recycle'=>0,'lang_code'=>$lang_code])->order(['sort','id'])->field($str)->select()->toArray();
        if ($r)
        {
            foreach ($r as $k => $v)
            {
                $v['checked'] = false; // 定义没选中
                $v['field'] = '';
                $id_0 = $v['id'];
                $v['children'] = Role_Tools::menu_subordinate1($id_0,$lang_code,$list,$menu_id);
                $array[] = $v;
            }
        }
        return $array;
    }

    // 菜单树结构-
    public static function transFitterData($data)
    {
        $items = [];
        foreach ($data as $key => $value)
        {
            $item = [
                'id' => $value['id'],
                'label' => $value['title'],
                'checked' => $value['checked'],
                'disabled' => $value['disabled'],
                'children' => isset($value['children']) ?  Role_Tools::transFitterData($value['children']) : []
            ];
            array_push($items, $item);
        }
        return $items;
    }


    // 绑定信息（控制台权限管理）
    public static function bangding($array)
    {
        $admin_name = $array['admin_name'];
        $id = $array['id'];
        $list = $array['list'];
        $lktlog = new LaiKeLogUtils();
        $Plugin = new Plugin();

        $menu_list = array();
        $add_menu_list = array();
        // 根据角色ID，查询菜单信息
        $sql0_0 = "select a.module,b.menu_id,b.is_display,b.guide_sort from lkt_core_menu as a left join lkt_role_menu as b on a.id = b.menu_id where role_id = '$id'";
        $r0_0 = Db::query($sql0_0);
        if($r0_0)
        {
            foreach ($r0_0 as $k => $v)
            {
                $menu_list1[] = $v['module'];
                $add_menu_list[] = array('menu_id'=>$v['menu_id'],'is_display'=>$v['is_display'],'guide_sort'=>$v['guide_sort']);
            }
            $menu_list = array_unique($menu_list1); // 菜单模块标识
        }
        Db::startTrans();
        foreach ($list as $k => $v) 
        {
            //判断管理员是否绑定当前角色
            $res0 = AdminModel::where(['recycle'=>0,'id'=>$v])->field('role,store_id')->select()->toArray();

            if($id != $res0[0]['role'])
            {   
                $store_id = $res0[0]['store_id'];
                //变更角色
                $sql0 = AdminModel::find($v);
                $sql0->role = $id;
                $res = $sql0->save();
                if(!$res)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 变更商户角色失败！admin_id:'.$v;
                    $lktlog->log("common/client.log",$Log_content);
                    Db::rollback();
                    $message = Lang("operation failed");
                    echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                    exit;
                }
                
                $Plugin->Modifying_plugins($store_id,$menu_list); // 修改插件
                
                //添加导览
                foreach ($add_menu_list as $k_guide => $v_guide)
                {
                    $menu_id = $v_guide['menu_id'];
                    $is_display = $v_guide['is_display'];
                    $guide_sort = $v_guide['guide_sort'];
                    $sql_guide_menu0 = new GuideMenuModel();
                    $sql_guide_menu0->store_id = $store_id;
                    $sql_guide_menu0->role_id = $id;  
                    $sql_guide_menu0->menu_id = $menu_id; 
                    $sql_guide_menu0->is_display = $is_display; 
                    $sql_guide_menu0->guide_sort = $guide_sort;
                    $sql_guide_menu0->add_date = date('Y-m-d H:i:s');
                    $sql_guide_menu0->save();
                    $r_guide_menu0 = $sql_guide_menu0->id;
                    if($r_guide_menu0 < 1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 绑定商户角色,添加导览菜单失败！role_id:'.$id;
                        $lktlog->log("common/client.log",$Log_content);
                        Db::rollback();
                        $message = Lang("operation failed");
                        echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                        exit;
                    }
                }
            }
            else
            {   
                $store_id = $res0[0]['store_id'];
                //变更角色
                $sql0 = AdminModel::find($v);
                $sql0->role = '';
                $res = $sql0->save();
                if(!$res)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 变更商户角色失败！admin_id:'.$v;
                    $lktlog->log("common/client.log",$Log_content);
                    Db::rollback();
                    $message = Lang("operation failed");
                    echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                    exit;
                }
                
                $Plugin->Modifying_plugins($store_id,array()); // 修改插件
                
                //删除导览
                $r_guide_menu0 = GuideMenuModel::where(['role_id'=>$id,'store_id'=>$store_id])->delete();
                if(!$r_guide_menu0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 取消商户绑定角色,删除导览菜单失败！role_id:'.$id;
                    $lktlog->log("common/client.log",$Log_content);
                    Db::rollback();
                    $message = Lang("operation failed");
                    echo json_encode(array('code'=>ERROR_CODE_CZSB,'message'=>$message));
                    exit;
                }
            }
        }

        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code'=>200,'message'=>$message));
        exit;
    }
}
