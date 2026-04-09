<?php
namespace app\admin\controller\admin\role;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Role_Tools;
use app\common\Tools;
use app\common\Jurisdiction;
use app\common\LaiKeLogUtils;

use app\admin\model\CoreMenuModel;
use app\admin\model\RoleModel;
use app\admin\model\RoleMenuModel;

/**
 * 功能：角色权限类
 * 修改人：PJY
 */
class Platform extends BaseController
{
    //获取角色权限数据
    public function getUserRoleInfo()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $lang_code = trim($this->request->param('lang_code'));

        $id = $this->request->param('id');
        if($id)
        {

            $r1 = RoleMenuModel::where('role_id',$id)->field('menu_id')->select()->toArray();
            $list = Role_Tools::menu(0,$lang_code,$r1,array());
            $list = json_decode($list);

            $data = array('menuList'=>$list);
        }
        else
        {
            $list = Role_Tools::menu(1,$lang_code,array(),array());
            $list = json_decode($list);

            $data = array('menuList'=>$list);
        }
        $message = Lang("Success");
        return output(200, $message, $data);
    }
}
