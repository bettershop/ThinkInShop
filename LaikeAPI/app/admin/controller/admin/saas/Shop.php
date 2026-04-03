<?php
namespace app\admin\controller\admin\saas;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;
use app\common\LaiKeLogUtils;

use app\admin\model\CustomerModel;
use app\admin\model\AdminModel;
use app\admin\model\RoleModel;


class Shop extends BaseController
{
    // 获取商城信息
    public function getShopInfo()
    {	
        $page = addslashes(trim($this->request->post('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->post('pageSize'))); // 每页显示多少条数据

        $list = array();
        $total = 0;
        // 查询商城总数
        $r0 = CustomerModel::where(['recycle'=>0])->field('count(id) as total')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        // 查询商城
        $r1 = CustomerModel::where(['recycle'=>0])->page($page,$pagesize)->order('is_default','desc')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $store_id = $v['id'];
                $admin_id = $v['admin_id'];
                $v['adminName'] = ''; // 商城管理员名称
                $v['roleId'] = ''; // 商城角色ID
                $v['roleName'] = ''; // 角色名称

                $r2 = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'id'=>$admin_id])->field('name,role')->select()->toArray();
                if($r2)
                {
                    $v['adminName'] = $r2[0]['name']; // 商城管理员名称
                    $v['roleId'] = $r2[0]['role']; // 商城角色ID

                    $r3 = RoleModel::where(['id'=>$r2[0]['role']])->field('name')->select()->toArray();
                    if($r3)
                    {
                        $v['roleName'] = $r3[0]['name']; // 角色名称
                    }
                }
      
                if($v['status'] == 1)
                {
                    $v['statusName'] = '到期';
                }
                else if($v['status'] == 2)
                {
                    $v['statusName'] = '锁定';
                }
                else
                {
                    $v['statusName'] = '启用';
                }
                $list[] = $v;
            }
        }

        $data = array('dataList'=>$list,'total'=>$total);
        $message = Lang('Success');
        return output(200,$message,$data);
    }
    

}
