<?php
namespace app\admin\controller\saas;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cookie;
use think\facade\Request;
use app\common\ServerPath;
use app\common\Role_Tools;
use app\common\pinyin;
use app\common\Tools;
use app\common\Jurisdiction;
use app\common\LaiKeLogUtils;
use app\common\Lang;

use app\admin\model\AdminModel;
use app\admin\model\CoreMenuModel;
use app\admin\model\RoleModel;
use app\admin\model\RoleMenuModel;

/**
 * 功能：控制台权限类
 * 修改人：PJY
 */
class Role extends BaseController
{   
    const ASYNC_ROUTES_CACHE_PREFIX = 'saas_async_routes:';
    const ASYNC_ROUTES_CACHE_INDEX_KEY = 'saas_async_routes:index';

    // 获取用户权限菜单
    public function getAsyncRoutesByRoutes()
    {   
        $accessId = $this->request->param('accessId');
        $store_id = $this->request->param('storeId');//商城ID
        $language = $this->request->param('language'); // 语言

        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $role_id = cache($accessId.'role');//管理员类型
        if($role_id === '' || $role_id === null)
        {
            $role_id = isset($admin_list['role']) ? $admin_list['role'] : 0;
        }

        $lang_code = Tools::get_lang($language);

        $cache_key = $this->getAsyncRoutesCacheKey($admin_type1,$role_id,$store_id,$lang_code);
        $cache_data = cache($cache_key);
        if(is_array($cache_data) && isset($cache_data['menu']))
        {
            $message = Lang('Success');
            return output(200,$message,$cache_data);
        }

        $all_menu = CoreMenuModel::where(['recycle'=>0,'lang_code'=>$lang_code])->order('sort','desc')->select()->toArray();
        $menu_map = array();
        foreach ($all_menu as $v_menu)
        {
            $sid = intval($v_menu['s_id']);
            if(!isset($menu_map[$sid]))
            {
                $menu_map[$sid] = array();
            }
            $menu_map[$sid][] = $v_menu;
        }

        //获取第一层菜单
        $menu = array();
        $role_menu_ids = array();//权限菜单
        $role_menu_map = array();
        if($admin_type1 == 0)
        {
            $r0 = CoreMenuModel::where('lang_code',$lang_code)->order('id','asc')->find();
            if($r0)
            {
                $first_menu = $r0->toArray();
                $first_menu['children'] = $this->buildChildren0ByMap($first_menu['id'],$store_id,$menu_map);
                $menu[0] = $first_menu;
            }
        }

        if($role_id !== '' && $role_id !== null)
        {
            $role_menu_ids = RoleMenuModel::where('role_id',$role_id)->column('menu_id');
            if($role_menu_ids)
            {
                foreach ($role_menu_ids as $role_menu_id)
                {
                    $role_menu_map[intval($role_menu_id)] = 1;
                }
            }
        }

        $res = array();
        if($role_menu_ids)
        {
            $res = CoreMenuModel::where(['recycle'=>0,'s_id'=>0,'level'=>1,'lang_code'=>$lang_code])->whereIn('id',$role_menu_ids)->order('sort', 'desc')->select()->toArray();
        }

        foreach ($res as $key => $value)
        {
            $res[$key]['children'] = array();
            $res[$key]['isChildren'] = false;
            $menu_id = $value['id'];
            if(isset($menu_map[intval($menu_id)]) && $menu_map[intval($menu_id)])
            {
                $res[$key]['isChildren'] = true;
            }
            $res[$key]['image'] = ServerPath::getimgpath($res[$key]['image'], $store_id);
            $res[$key]['image1'] = ServerPath::getimgpath($res[$key]['image1'], $store_id);
            $res[$key]['children'] = $this->buildChildrenByMap($menu_id,$store_id,$role_menu_map,$menu_map,true);
            
            $menu[] = $res[$key];
        }

        $data = array('menu'=>$menu);
        cache($cache_key,$data,3600);
        $this->appendAsyncRoutesCacheKey($cache_key);

        $message = Lang('Success');
        return output(200,$message,$data); 
    }

    private function buildChildren0ByMap($menu_id,$store_id,$menu_map)
    {
        $children = array();
        $menu_id = intval($menu_id);
        if(isset($menu_map[$menu_id]) && $menu_map[$menu_id])
        {
            $res = $menu_map[$menu_id];
            foreach ($res as $key => $value)
            {
                $res[$key]['isChildren'] = false;
                $child_id = intval($value['id']);
                $isChildren = isset($menu_map[$child_id]) ? $menu_map[$child_id] : array();
                if($isChildren)
                {
                    if(intval($isChildren[0]['level']) <= 3)
                    {
                        $res[$key]['isChildren'] = true;
                    }
                }
                $res[$key]['image'] = ServerPath::getimgpath($res[$key]['image'], $store_id);
                $res[$key]['image1'] = ServerPath::getimgpath($res[$key]['image1'], $store_id);
                $res[$key]['children'] = $this->buildChildren0ByMap($child_id,$store_id,$menu_map);
            }
            $children = $res;
        }

        return $children;
    }

    private function buildChildrenByMap($menu_id,$store_id,$role_menu_map,$menu_map,$need_filter)
    {
        $children = array();
        $menu_id = intval($menu_id);
        $res = isset($menu_map[$menu_id]) ? $menu_map[$menu_id] : array();
        if($need_filter && $role_menu_map)
        {
            $res0 = array();
            foreach ($res as $v)
            {
                if(isset($role_menu_map[intval($v['id'])]))
                {
                    $res0[] = $v;
                }
            }
            $res = $res0;
        }
        if($res)
        {
            foreach ($res as $key => $value)
            {
                $res[$key]['isChildren'] = false;
                $child_id = intval($value['id']);
                $isChildren = isset($menu_map[$child_id]) ? $menu_map[$child_id] : array();
                if($isChildren)
                {
                    if(intval($isChildren[0]['level']) <= 3)
                    {
                        $res[$key]['isChildren'] = true;
                    }
                }
                $res[$key]['children'] = $this->buildChildrenByMap($child_id,$store_id,$role_menu_map,$menu_map,false);
            }
            $children = $res;
        }
        return $children;
    }

    private function getAsyncRoutesCacheKey($admin_type,$role_id,$store_id,$lang_code)
    {
        $role_id = intval($role_id);
        $store_id = intval($store_id);
        $lang_code = str_replace(':','_',$lang_code);
        return self::ASYNC_ROUTES_CACHE_PREFIX . $admin_type . ':' . $role_id . ':' . $store_id . ':' . $lang_code;
    }

    private function appendAsyncRoutesCacheKey($cache_key)
    {
        if($cache_key == '')
        {
            return;
        }
        $cache_keys = cache(self::ASYNC_ROUTES_CACHE_INDEX_KEY);
        if(!is_array($cache_keys))
        {
            $cache_keys = array();
        }
        if(!in_array($cache_key,$cache_keys))
        {
            $cache_keys[] = $cache_key;
            cache(self::ASYNC_ROUTES_CACHE_INDEX_KEY,$cache_keys,30 * 24 * 60 * 60);
        }
    }

    private function clearAsyncRoutesCache()
    {
        $cache_keys = cache(self::ASYNC_ROUTES_CACHE_INDEX_KEY);
        if(is_array($cache_keys))
        {
            foreach ($cache_keys as $cache_key)
            {
                cache($cache_key,NULL);
            }
        }
        cache(self::ASYNC_ROUTES_CACHE_INDEX_KEY,NULL);
    }

    private function clearAdminPermissionCache($role_id = null,$admin_ids = array())
    {
        $query = AdminModel::where('recycle',0)->where('token','<>','');
        if($role_id !== null && $role_id !== '')
        {
            $query = $query->where('role',$role_id);
        }
        if($admin_ids)
        {
            $admin_ids = array_values(array_filter(array_map('intval',$admin_ids)));
            if(!$admin_ids)
            {
                return;
            }
            $query = $query->whereIn('id',$admin_ids);
        }
        $admin_list = $query->field('id,token')->select()->toArray();
        foreach ($admin_list as $v)
        {
            $token = $v['token'];
            if($token == '')
            {
                continue;
            }
            cache($token.'admin_permission',NULL);
        }
    }

    //递归获取下级菜单
    public function getChildren0($menu_id,$store_id,$lang_code)
    {
        $children = array();
        
        $res = CoreMenuModel::where(['s_id'=>$menu_id,'recycle'=>0,'lang_code'=>$lang_code])->order('sort','desc')->select()->toArray();
        if($res)
        {   
            foreach ($res as $key => $value) 
            {
                $res[$key]['isChildren'] = false;
                $menu_id = $value['id'];
                $isChildren = CoreMenuModel::where(['s_id'=>$menu_id,'recycle'=>0,'lang_code'=>$lang_code])->order('sort','desc')->select()->toArray();
                if($isChildren)
                {
                    if($isChildren[0]['level'] <= 3)
                    {
                        $res[$key]['isChildren'] = true;
                    }
                }
                $res[$key]['image'] = ServerPath::getimgpath($res[$key]['image'], $store_id);
                $res[$key]['image1'] = ServerPath::getimgpath($res[$key]['image1'], $store_id);
                $res[$key]['children'] = $this->getChildren0($menu_id,$store_id,$lang_code);
            }
            $children = $res;
        }
        return $children;
    }

    //递归获取下级菜单
    public function getChildren($menu_id,$store_id,$list,$lang_code)
    {
        $children = array();
        if($list)
        {
            $res = CoreMenuModel::where(['s_id'=>$menu_id,'recycle'=>0,'lang_code'=>$lang_code])->where('id','in',$list)->order('sort','desc')->select()->toArray();
			$list = null;
		}
        else
        {
            $res = CoreMenuModel::where(['s_id'=>$menu_id,'recycle'=>0,'lang_code'=>$lang_code])->order('sort','desc')->select()->toArray();
        }
        
        if($res)
        {   
            foreach ($res as $key => $value) 
            {
                $res[$key]['isChildren'] = false;
                $menu_id = $value['id'];
                $isChildren = CoreMenuModel::where(['s_id'=>$menu_id,'recycle'=>0])->order('sort','desc')->select()->toArray();
                if($isChildren)
                {
                    if($isChildren[0]['level'] <= 3)
                    {
                        $res[$key]['isChildren'] = true;
                    }
                }
                $res[$key]['children'] = $this->getChildren($menu_id,$store_id,$list,$lang_code);
            }
            $children = $res;
        }
        return $children;
    }

    // 获取角色
    public function getRoleListInfo()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $id = $this->request->param('$id');
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $page = $this->request->param('pageNo');
        $pagesize = $this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize:'10';

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $array = array('status'=>1,'id'=>$id,'store_id'=>0,'start'=>$start,'pagesize'=>$pagesize);
        $data = Role_Tools::role_list($array);
        // $list = array();

        // $r0 = RoleModel::where('status',1)->order('add_date','desc')->select()->toArray();
        // if($r0)
        // {
        //     foreach ($r0 as $k0 => $v0)
        //     {
        //         $list[] = array('id'=>$v0['id'],'name'=>$v0['name']);
        //     }
        // }
       
        // $data = array('list'=>$list,'totla'=>count($list));
        $message = Lang('Success');
        return output(200, $message, $data);
    }

    //菜单列表
    public function getMenuLeveInfo()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $pinyin = new pinyin();

        $id = $this->request->param('id');//菜单id
        $parentId = $this->request->param('sid'); // 上级ID
        $type = $this->request->param('type'); // 菜单类型0=控制台 1=商城
        $lang_code = trim($this->request->param('lang_code'));
        $title = $this->request->param('name'); // 菜单名称
        $page = $this->request->param('pageNo');
        $pagesize = $this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize:'10';

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition['recycle'] = 0;
        if(strlen($type) >0)
        {   
            $condition['type'] = $type;
        }
        if($lang_code != '')
        {   
            $condition['lang_code'] = $lang_code;
        }
        if($title != '')
        {
            $condition['title|id'] = ['like',$title];
        }
        if($id != '')
        {
            $condition['id'] = $id;
        }
        else
        {
            if($parentId != '')
            {
                $condition['s_id'] = $parentId;
            }
            else
            {
                // $condition['s_id'] = 0;
            }
        }
        // var_dump($condition);die;
        $res = '';
        $total = 0;
        $r0 = CoreMenuModel::where($condition)->count();
        if($r0)
        {
            $total = $r0;
        }
        $list = array();
        if($total > 0)
        {
            $r1 = CoreMenuModel::where($condition)->order('sort','desc')->limit($start,$pagesize)->select()->toArray();
            if($r1)
            {
                foreach($r1 as $k1 => $v1)
                {
                    $r_title = $v1['title'];
                    $res = $pinyin->str2py($r_title) . '_';
                    $v1['id_id'] = $res . $v1['id'];
                    if($v1['s_id'] > 0)
                    {
                        $r2 = CoreMenuModel::where('id',$v1['s_id'])->field('title')->select()->toArray();
                        $v1['fatherName'] = $r2[0]['title'];
                    }
                    $list[] = $v1;
                }
            }
        }
        $data = array('total'=>$total,'list'=>$list,'sid'=>$parentId);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    //添加菜单
    public function addMenuInfo()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $lktlog = new LaiKeLogUtils();
        $id = intval($this->request->param('mid')); // ID
        $type = intval($this->request->param('menuClass'));//类型 0=控制台 1=商城
        $module = $this->request->param('path');//菜单模块标识
        $url = $this->request->param('menuUrl');//路径
        $action = $this->request->param('menuPath')?$this->request->param('menuPath'):'';//导览地址
        $is_button = intval($this->request->param('isButton'));//是否按钮
        $is_core = intval($this->request->param('isCore')); // 是否是核心
        $level = intval($this->request->param('level')); // 级别
        $title = addslashes(trim($this->request->param('menuName'))); // 菜单名称
        $guide_name = addslashes(trim($this->request->param('guideName'))); // 导览名称
        $briefintroduction = addslashes(trim($this->request->param('briefintroduction'))); // 导览简介
        $image1 = addslashes(trim($this->request->param('chekedLogo'))); // 选中图标、导览图标
        $image = addslashes(trim($this->request->param('defaultLogo'))); // 默认图标
        $s_id = intval($this->request->param('fatherMenuId')); // 上级ID
        $isTab = intval($this->request->param('isTab')); // 是否为tab页面
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语言
        $country_num = addslashes(trim($this->request->param('country_num'))); // 国家代码

        if($id == '')
        {
            Tools::National_Language($lang_code,$country_num);
        }

        if($title == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单名称不能为空';
            $lktlog->log("common/menu.log",$Log_content);
            $message = Lang("role.18");
            return output(ERROR_CODE_CDMCBNWK,$message);
        }
        // if (mb_strlen($title,"utf-8") > 6)
        // {
        //     $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单名称不能超过6个字符长度';
        //     $lktlog->log("common/menu.log",$Log_content);
        //     $message = Lang("role.19");
        //     return output(ERROR_CODE_CDMCBNWK,$message);
        // }

        if($guide_name == '')
        {
            $guide_name = $title;
        }

        if($level == 1)
        { // 一级菜单
            $briefintroduction = '';
            $url = '';
            $s_id = 0;
            if($image == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 默认图标不能为空';
                $lktlog->log("common/menu.log",$Log_content);
                $message = Lang("role.20");
                return output(ERROR_CODE_MRTBBNWK,$message);
            }
            if($image1 == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 选中图标不能为空';
                $lktlog->log("common/menu.log",$Log_content);
                $message = Lang("role.21");
                return output(ERROR_CODE_XZTBBNWK,$message);
            }
        }
        else
        {
            if($s_id == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择上级ID';
                $lktlog->log("common/menu.log",$Log_content);
                $message = Lang("role.22");
                return output(ERROR_CODE_SJCDBNWK,$message);
            }
            if($url == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 路径不能为空';
                $lktlog->log("common/menu.log",$Log_content);
                $message = Lang("role.23");
                return output(ERROR_CODE_LJBNWK,$message);
            }
        }

        $condition[] = ['recycle','=',0];
        $condition[] = ['title','=',$title];
        $condition[] = ['s_id','=',$s_id];
        $condition[] = ['is_core','=',$is_core];
        $condition[] = ['lang_code','=',$lang_code];
        if($id)
        {
            $condition[] = ['id','<>',$id];
        }
        $r0 = CoreMenuModel::where($condition)->field('id')->select()->toArray();
        if($r0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单名称'.$title.'已存在！';
            $lktlog->log("common/menu.log",$Log_content);
            $message = Lang("role.24");
            return output(ERROR_CODE_CDMCYCZ,$message);
        }

        if($id)
        {
            $sql1 = CoreMenuModel::find($id);
            $sql1->type = $type;
            $sql1->module = $module;
            $sql1->url = $url;
            $sql1->action = $action;
            $sql1->is_button = $is_button;
            $sql1->is_core = $is_core;
            $sql1->level = $level;
            $sql1->title = $title;
            $sql1->guide_name = $guide_name;
            $sql1->briefintroduction = $briefintroduction;
            $sql1->image = $image;
            $sql1->image1 = $image1;
            $sql1->s_id = $s_id;
            $sql1->is_tab = $isTab;
            // $sql1->lang_code = $lang_code;
            // $sql1->country_num = $country_num;
            //判断上级id是否变更
            $res0 = CoreMenuModel::where('id',$id)->select()->toArray();
            $old_sid = $res0[0]['s_id'];
            if($old_sid != $s_id)
            {
                //获取上级最大排序值
                $sql_s = CoreMenuModel::where(['s_id'=>$s_id,'recycle'=>0])->max('sort');
                $sort = $sql_s + 1;
                $sql1->sort = $sort;
            }
            $r1 = $sql1->save();
            if(!$r1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改菜单失败!id:'.$id;
                $lktlog->log("common/menu.log",$Log_content);
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            else
            {
                $this->clearAsyncRoutesCache();
                $this->clearAdminPermissionCache();
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改菜单成功';
                $lktlog->log("common/menu.log",$Log_content);
                $message = Lang("Success");
                return output(200,$message);
            }
        }
        else
        {   
            //获取上级最大排序值
            $sql_s = CoreMenuModel::where(['s_id'=>$s_id,'recycle'=>0])->max('sort');
            $sort = $sql_s + 1;
            $sql1 = new CoreMenuModel();
            $sql1->sort = $sort;
            $sql1->type = $type;
            $sql1->module = $module;
            $sql1->url = $url;
            $sql1->action = $action;
            $sql1->is_button = $is_button;
            $sql1->is_core = $is_core;
            $sql1->level = $level;
            $sql1->title = $title;
            $sql1->guide_name = $guide_name;
            $sql1->briefintroduction = $briefintroduction;
            $sql1->image = $image;
            $sql1->image1 = $image1;
            $sql1->s_id = $s_id;
            $sql1->is_tab = $isTab;
            $sql1->lang_code = $lang_code;
            $sql1->country_num = $country_num;
            $sql1->add_time = date('Y-m-d H:i:s');
            $sql1->save();
            $r1 = $sql1->id;
            if($r1 > 0)
            {
                $this->clearAsyncRoutesCache();
                $this->clearAdminPermissionCache();
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 添加菜单成功';
                $lktlog->log("common/menu.log",$Log_content);
                $message = Lang("Success");
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 添加菜单失败!title:'.$title;
                $lktlog->log("common/menu.log",$Log_content);
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }
    }

    //删除菜单
    public function delMenu()
    {   
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $lktlog = new LaiKeLogUtils();
        // 接收信息
        $id = $this->request->param('menuId'); //id
        $status = 0;
        // 根据id,查询他的下级
        $r = CoreMenuModel::where(['s_id'=>$id,'recycle'=>0])->field('id')->select()->toArray();
        if($r)
        { // 有下级
            $status = 1;
        }
        if($status == 0)
        {   
            $sql1 = CoreMenuModel::find($id);
            $sql1->recycle = 1;
            $r1 = $sql1->save();
            if(!$r1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 修改菜单状态失败！id:'.$id;
                $lktlog->log("common/menu.log",$Log_content);
                $message = Lang("role.25");
                return output(ERROR_CODE_CZSB,$message);
            }
            else
            {
                $this->clearAsyncRoutesCache();
                $this->clearAdminPermissionCache();
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 删除菜单id为 '.$id.' 成功 ';
                $lktlog->log("common/menu.log",$Log_content);
                $message = Lang("Success");
                return output(200,$message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name.' 删除菜单id为 '.$id.' 失败';
            $lktlog->log("common/menu.log",$Log_content);
            $message = Lang("role.25");
            return output(ERROR_CODE_CZSB,$message);
        }
    }

    //移动菜单
    public function moveMenuSort()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $downId = $this->request->param('downId'); //下移菜单id
        $upId = $this->request->param('upId'); //上移菜单id
        
        //分别获取交换的菜单排序
        $sql0 = CoreMenuModel::where('id',$downId)->field('sort')->select()->toArray();
        $sort_u = $sql0[0]['sort'];//上移序号

        $sql1 = CoreMenuModel::where('id',$upId)->field('sort')->select()->toArray();
        $sort_d = $sql1[0]['sort'];//下移序号
        Db::startTrans();
        try 
        {
            //交换排序
            $sql2 = CoreMenuModel::find($downId);
            $sql2->sort = $sort_d;
            $sql2->save();

            $sql3 = CoreMenuModel::find($upId);
            $sql3->sort = $sort_u;
            $sql3->save();
            // 提交事务
            Db::commit();
            $this->clearAsyncRoutesCache();
            $this->clearAdminPermissionCache();
            $message = Lang("Success");
            return output(200,$message);
        }
        catch (\Exception $e) 
        {
            // 回滚事务
            Db::rollback();
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
    }

    //置顶菜单
    public function moveTopMenuSort()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = $this->request->param('id'); //菜单id
        $sid = $this->request->param('sid'); //上级菜单id

        $sql = CoreMenuModel::find($id);
        //获取当前上级菜单id下最大排序号
        $sql_s = CoreMenuModel::where(['s_id'=>$sid,'recycle'=>0])->max('sort');
        $sort = $sql_s + 1;
        $sql->sort = $sort;
        $res = $sql->save();
        if($res)
        {
            $this->clearAsyncRoutesCache();
            $this->clearAdminPermissionCache();
            $message = Lang("Success");
            return output(200,$message);
        }
        else
        {
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
    }

    //添加、编辑角色
    public function addUserRoleMenu()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = $this->request->param('id');
        // 接收数据
        $name = addslashes(trim($this->request->param('roleName'))); // 角色
        $permissions = $this->request->param('permissions'); // 权限，逗号分隔
        $role_describe = $this->request->param('describe'); // 描述
        $this->clearAsyncRoutesCache();
        if($id)
        {
            $this->clearAdminPermissionCache($id);
            $array = array('id'=>$id,'admin_name'=>$admin_name,'store_id'=>0,'name'=>$name,'permissions'=>$permissions,'role_describe'=>$role_describe,'admin_id'=>$admin_id);
            Role_Tools::edit_role($array);
        }
        else
        {
            $array = array('admin_id'=>$admin_id,'admin_name'=>$admin_name,'store_id'=>0,'status'=>1,'name'=>$name,'permissions'=>$permissions,'role_describe'=>$role_describe);
            Role_Tools::add_role($array);
        }
    }

    //权限绑定列表
    public function getBindListInfo()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = $this->request->param('roleId');//角色id
        $name = $this->request->param('name');//商城名
        $is_bind = $this->request->param('isBind');//0未绑定1已绑定
        $list = array();
        $condition = " a.type = 1 and a.recycle = 0 and b.recycle = 0 ";
        if($name != '')
        {
            $condition .= " and b.name like '%$name%' ";
        }
        if($is_bind)
        {
            // 查询已绑定商户
            $sql0 = "select a.id,a.tel,b.name from lkt_admin as a left join lkt_customer as b on a.store_id = b.id where $condition and a.role = '$id'";

        }
        else
        {
            // 查询未绑定商户
            $sql0 = "select a.id,a.tel,b.name from lkt_admin as a left join lkt_customer as b on a.store_id = b.id where $condition and (a.role != '$id' or a.role is null) ";
        }
        $r1 = Db::query($sql0);
        if($r1)
        {
            $list = $r1;
        }
        $message = Lang("Success");
        return output(200,$message,array('bindAdminList'=>$list));
    }

    //验证商户时候已绑定角色
    public function verificationBind()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $admin_ids = $this->request->param('adminIds');//管理员id
        $id_arr = explode(',',$admin_ids);
        $status = false;
        foreach ($id_arr as $k => $v)
        {
            $r0 = AdminModel::where(['recycle'=>0,'id'=>$v])->field('role')->select()->toArray();
            if($r0)
            {
                $role = $r0[0]['role'];
                if($role != '')
                {
                    $status = true;
                }
            }
            else
            {   
                $message = Lang("Parameter error");
                return output(ERROR_CODE_CSCW,$message);
            }
        }
        $message = Lang("Success");
        return output(200,$message,$status);
    }

    //绑定角色
    public function bindRole()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = $this->request->param('roleId');//角色id
        $admin_ids = $this->request->param('adminIds');//管理员id
        $id_arr = array_values(array_filter(array_map('intval',explode(',',$admin_ids))));
        $this->clearAdminPermissionCache(null,$id_arr);

        $array = array('admin_name'=>$admin_name,'id'=>$id,'list'=>$id_arr);
        Role_Tools::bangding($array);
        exit;
    }

    //删除角色
    public function delUserRoleMenu()
    {   
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = intval($this->request->param('id')); //角色id
        $this->clearAsyncRoutesCache();
        $this->clearAdminPermissionCache($id);

        $array = array('status'=>0,'store_id'=>$store_id,'admin_name'=>$admin_name,'id'=>$id);
        Role_Tools::del_role($array);
        exit;
    }

    // 获取操作按钮
    public function getButton()
    {   
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $accessId = $this->request->param('accessId');
        $role_id = cache($accessId.'role');//管理员类型
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        
        $id = intval($this->request->param('menuId')); //角色id
        
        $list = array();
        if($admin_type1 == 0)
        {
            $r0 = CoreMenuModel::where(['s_id'=> $id,'recycle'=>0])->field('id,image,image1,title')->select()->toArray();
        }
        else
        {   

            $role_sql = "select a.* from lkt_core_menu as a 
                           left join lkt_role_menu as b on a.id = b.menu_id 
                           left join lkt_role as c on b.role_id = c.id 
                           where a.recycle = 0 and c.id = $role_id and a.s_id = $id";
            $r0 = Db::query($role_sql);
        }

        if($r0)
        {
            $list = $r0;
        }
        $message = Lang('Success');
        return output(200,$message,$list);
    }

    //获取角色菜单数据
    public function getRoleMenu()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $role_id = intval($this->request->param('roleId'));

        $menuList = array();
        $sql1 = "select menu_id from lkt_role_menu as a left join lkt_core_menu as b on a.menu_id = b.id where b.recycle = 0 and b.is_button = 0 and a.role_id = '$role_id' ";
        $res1 = Db::query($sql1); 
        if($res1)
        {
            $menuList = array_values($res1);
        }
        $btnIds = array();
        $sql2 = "select menu_id from lkt_role_menu as a left join lkt_core_menu as b on a.menu_id = b.id where b.recycle = 0 and b.is_button = 1 and a.role_id = '$role_id' ";
        $res2 = Db::query($sql2);
        if($res2)
        {
            $btnIds = array_values($res2);
        }
        $message = Lang('Success');
        return output(200,$message,array('menuList'=>$menuList,'btnIds'=>$btnIds));
    }

    public function temporary()
    {
        $res = array(

        );

        foreach($res as $k => $v)
        {
            // print_r($v);echo "<br>"; 594 + 600
            $v[0] = $v[0] + 1200;
            if($v[1] != 0)
            {
                $v[1] = $v[1] + 1200;
            }
            $v[22] = 'zh_TW';
            $v[23] = 344;
            // print_r($v);echo "<br>";
            // Db::name('core_menu')->insert($v);

            $sql = "INSERT INTO `lkt_core_menu`(`id`, `s_id`, `title`, `name`, `image`, `image1`, `module`, `action`, `url`, `sort`, `level`, `is_core`, `is_plug_in`, `type`, `add_time`, `recycle`, `is_display`, `briefintroduction`, `guide_sort`, `guide_name`, `is_button`, `is_tab`, `lang_code`, `country_num`) VALUES ('$v[0]', '$v[1]', '$v[2]', '$v[3]', '$v[4]', '$v[5]', '$v[6]', '$v[7]', '$v[8]', '$v[9]', '$v[10]', '$v[11]', '$v[12]', '$v[13]', '$v[14]', '$v[15]', '$v[16]', '$v[17]', '$v[18]', '$v[19]', '$v[20]', '$v[21]', '$v[22]', '$v[23]') ";
            $r = Db::execute($sql);
            // print_r('======');echo "<br>";
            // print_r($sql);echo "<br>";die;
            // $list[] = $v;
        }

        // $r3 = Db::name('core_menu')->insertAll($list);
    }
}
