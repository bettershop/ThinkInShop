<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\ServerPath;
use app\common\pinyin;
use app\common\Tools;

use app\admin\model\AdminModel;
use app\admin\model\GuideMenuModel;
use app\admin\model\CoreMenuModel;

/**
 * 功能：功能导览类
 * 修改人：DHB
 */
class Overview extends BaseController
{
    // 功能导览
    public function index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        $language = addslashes(trim($this->request->param('language')));

        $isAdmin = $this->user_list['type']; // 类型 0:系统管理员 1：客户 2:商城管理员 3:店主

        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('role')->select()->toArray();
        $role = $r_admin[0]['role'];

        $lang_code = Tools::get_lang($language);
        $con = " a.recycle = 0 and a.s_id = 0 and a.lang_code = '$lang_code' ";
        
        $total = 0;
        $list = array();

        if($isAdmin == 0)
        {
            $sql0 = "select a.id,a.guide_name,a.image,a.briefintroduction from lkt_core_menu as a where $con and a.is_display = 1 and a.is_core = 1 order by a.guide_sort desc ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                foreach($r0 as $k => $v)
                {
                    $id = $v['id'];
                    $title = $v['guide_name'];

                    $list_total = $this->findSubordinates($store_id,0,$id,array(),$role);
                    $list[] = array('title'=>$title,'list'=>$list_total);
                }
            }
        }

        $sql0 = "select tt.* from (select a.id,a.guide_name,a.image1 as image,a.briefintroduction,b.guide_sort,row_number () over (partition by b.menu_id) as top from lkt_core_menu as a left join lkt_guide_menu as b on a.id = b.menu_id left join lkt_role as c on b.role_id = c.id where $con and b.store_id = '$store_id' and b.is_display = 0 and a.type = 1 and c.id = '$role') as tt where tt.top<2 order by tt.guide_sort desc ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k => $v)
            {
                $id = $v['id'];
                $title = $v['guide_name'];

                $list_total = $this->findSubordinates($store_id,$id,array(),$role);
                if($list_total != array())
                {
                    $list[] = array('title'=>$title,'list'=>$list_total);
                }
            }
        }
        

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 查询所有下级
    public function findSubordinates($store_id,$id,$list_total,$role,$url = '')
    {
        $sql0 = "select tt.* from (select a.id,a.guide_name,a.image1 as image,a.action as menuPath,a.url,a.level,a.briefintroduction,b.guide_sort,row_number () over (partition by b.menu_id) as top from lkt_core_menu as a left join lkt_guide_menu as b on a.id = b.menu_id left join lkt_role as c on b.role_id = c.id where a.recycle = 0 and b.store_id = '$store_id' and b.is_display = 0 and a.s_id = '$id' and c.id = '$role' ) as tt where tt.top<2 order by tt.guide_sort desc";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach ($r0 as $k0 => $v0)
            {
                $id1 = $v0['id']; // 菜单ID
                $level = $v0['level']; // 级别
                if($level == 2)
                {
                    $url = $v0['url']; // 跳转路径
                }
                $list_total[] = array('title'=>$v0['guide_name'],'image'=>$v0['image'],'introduction'=>$v0['briefintroduction'],'url'=>$url,'menuPath'=>$v0['menuPath']);
            }
            return $list_total;
        }
        else
        {
            return $list_total;
        }
    }
    
    // 编辑导览
    public function functionList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        $language = addslashes(trim($this->request->param('language')));

        $sid = addslashes(trim($this->request->param('sid'))); // 上级菜单ID
        $title = addslashes(trim($this->request->param('name'))); // 菜单ID/菜单名称
        $pageNo = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';
        $start = 0;
        if ($pageNo)
        {
            $start = ($pageNo - 1) * $pagesize;
        }
        
        $isAdmin = $this->user_list['type']; // 类型 0:系统管理员 1：客户 2:商城管理员 3:店主

        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('role')->select()->toArray();
        $role = $r_admin[0]['role'];

        $lang_code = Tools::get_lang($language);
        $condition = " a.recycle = 0 and b.store_id = '$store_id' and c.id = '$role' and a.lang_code = '$lang_code' ";
        if($sid != '' && $sid != 0)
        {
            $condition .= " and a.s_id = '$sid' ";
        }
        else
        {
            $condition .= " and a.s_id = 0 and a.is_core = 0 ";
        }
        if($title != '')
        {
            $title1 = Tools::FuzzyQueryConcatenation($title);
            $condition .= " and (a.id = '$title' or a.title like $title1 or a.guide_name like $title1 ) ";
        }
        
        $total = 0;
        $list = array();
        $pinyin = new pinyin();
        // 查询所有商城的一级菜单
        $sql0 = "select tt.* from (select a.id,row_number () over (partition by b.menu_id) as top from lkt_core_menu as a left join lkt_guide_menu as b on a.id = b.menu_id left join lkt_role as c on b.role_id = c.id where $condition) as tt where tt.top<2 ";
        $r0 = Db::query($sql0);
        $total = count($r0);
        
        // 查询商城一级菜单
        $sql1 = "select tt.* from (select a.id,a.s_id,title,a.image,a.image1,a.level,a.action,a.url,b.id as guidId,b.is_display,b.guide_sort,a.guide_name,a.briefintroduction,b.role_id,row_number () over (partition by b.menu_id) as top from lkt_core_menu as a left join lkt_guide_menu as b on a.id = b.menu_id left join lkt_role as c on b.role_id = c.id where $condition order by b.guide_sort desc,b.id) as tt where tt.top<2 limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k1 => $v1)
            {
                $r_title = $v1['title'];
                $res = $pinyin->str2py($r_title) . '_';
                $v1['id_id'] = $res . $v1['id'];
                $v1['sid'] = $res . $v1['s_id'];
                // $v1['image1'] = $v1['image'];
                if($v1['image'] == null)
                {
                    $v1['image'] = '';
                }
                if($v1['image1'] == null)
                {
                    $v1['image1'] = '';
                }
                $list[] = $v1;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        return output(200,$message,$data);
    }
    
    // 是否显示
    public function isDisplaySwitch()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 菜单ID
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('role')->select()->toArray();
        $role = $r_admin[0]['role'];

        $title = '';
        $sql_0 = "select title from lkt_core_menu where id = '$id' ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $title = $r_0[0]['title'];
        }

        $r0 = GuideMenuModel::where(['store_id'=>$store_id,'menu_id'=>$id,'role_id'=>$role])->field('is_display')->select()->toArray();
        if($r0)
        {
            $is_display = $r0[0]['is_display'];

            if($is_display == 1)
            {
                $sql_update = array('is_display'=>0);
            }
            else
            {
                $sql_update = array('is_display'=>1);
            }

            $sql_where = array('store_id'=>$store_id,'menu_id'=>$id,'role_id'=>$role);
            $r1 = Db::name('guide_menu')->where($sql_where)->update($sql_update);
            if($r1 > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改菜单是否显示成功！';
                $this->Log($Log_content);
                $Jurisdiction->admin_record($store_id, $operator, '将导览名称：'.$title.'进行了是否显示操作',2,1,0,$operator_id);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改菜单是否显示失败！参数：'.json_encode($sql_where);
                $this->Log($Log_content);
                $Jurisdiction->admin_record($store_id, $operator, '将导览名称：'.$title.'进行了是否显示操作失败',2,1,0,$operator_id);
                $message = Lang('失败！');
                return output(109,$message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 上移、下移
    public function move()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 菜单ID
        $id2 = addslashes(trim($this->request->param('id2'))); // 更换排序的菜单ID
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        // 根据商城ID，查询商城角色
        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('role')->select()->toArray();
        $role = $r_admin[0]['role']; // 商城角色ID

        $title = '';
        $sql_0 = "select title from lkt_core_menu where id = '$id' ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $title = $r_0[0]['title'];
        }

        $r0 = GuideMenuModel::where(['store_id'=>$store_id,'menu_id'=>$id,'role_id'=>$role])->field('guide_sort')->select()->toArray();
        if($r0)
        {
            $guide_sort0 = $r0[0]['guide_sort']; // 当前排序
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }

        $r_0 = GuideMenuModel::where(['store_id'=>$store_id,'menu_id'=>$id2,'role_id'=>$role])->field('guide_sort')->select()->toArray();
        if($r_0)
        {
            $guide_sort_0 = $r_0[0]['guide_sort']; // 更换排序的菜单ID的当前排序
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }

        $sql_update1 = array('guide_sort'=>$guide_sort_0);
        $sql_where1 = array('store_id'=>$store_id,'menu_id'=>$id,'role_id'=>$role);
        $r1 = Db::name('guide_menu')->where($sql_where1)->update($sql_update1);
        if($r1 <= 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改菜单是否显示失败！参数：'.json_encode($sql_where1);
            $this->Log($Log_content);
            $Jurisdiction->admin_record($store_id, $operator, '将导览名称：'.$title.'进行了上移、下移操作失败',2,1,0,$operator_id);
            $message = Lang('Modification failed');
            return output(109,$message);
        }

        $sql_update_1 = array('guide_sort'=>$guide_sort0);
        $sql_where_1 = array('store_id'=>$store_id,'menu_id'=>$id2,'role_id'=>$role);
        $r_1 = Db::name('guide_menu')->where($sql_where_1)->update($sql_update_1);
        if($r_1 <= 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改菜单是否显示失败！参数：'.json_encode($sql_where_1);
            $this->Log($Log_content);
            $Jurisdiction->admin_record($store_id, $operator, '将导览名称：'.$title.'进行了上移、下移操作失败',2,1,0,$operator_id);
            $message = Lang('Modification failed');
            return output(109,$message);
        }

        $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 修改导览排序成功！';
        $this->Log($Log_content);
        $Jurisdiction->admin_record($store_id, $operator, '将导览名称：'.$title.'进行了上移、下移操作',2,1,0,$operator_id);
        $message = Lang('Success');
        return output(200,$message);
    }
    
    // 置顶
    public function sortTop()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 菜单ID
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $title = '';
        $sql_0 = "select title from lkt_core_menu where id = '$id' ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $title = $r_0[0]['title'];
        }
        
        // 根据商城ID，查询商城角色
        $r_admin = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('role')->select()->toArray();
        $role = $r_admin[0]['role']; // 商城角色ID
        
        $id_str = "";
        // 根据菜单ID，查询上级ID
        $r0 = CoreMenuModel::where(['id'=>$id])->field('s_id')->select()->toArray();
        if($r0)
        {
            $s_id = $r0[0]['s_id']; // 查询上级ID
            // 根据上级ID，查询所有下级ID
            $r1 = CoreMenuModel::where(['s_id'=>$s_id,'recycle'=>0,'type'=>1])->field('id')->select()->toArray();
            if($r1)
            {
                foreach ($r1 as $k1 => $v1)
                {
                    $id_str .= $v1['id'] . ',';
                }
                
                $id_str = trim($id_str,',');
            }
        }

        $r2 = GuideMenuModel::where(['store_id'=>$store_id,'role_id'=>$role])->whereIn('menu_id',$id_str)->max('guide_sort');
        $guide_sort = $r2 + 1;

        $sql_update = array('guide_sort'=>$guide_sort);
        $sql_where = array('store_id'=>$store_id,'menu_id'=>$id,'role_id'=>$role);
        $r3 = Db::name('guide_menu')->where($sql_where)->update($sql_update);
        if($r3 <= 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 置顶失败！参数：'.json_encode($sql_where);
            $this->Log($Log_content);
            $Jurisdiction->admin_record($store_id, $operator, '置顶了导览名称：'.$title. ' 失败',2,1,0,$operator_id);
            $message = Lang('Modification failed');
            return output(109,$message);
        }

        $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' 置顶导览成功！';
        $this->Log($Log_content);
        $Jurisdiction->admin_record($store_id, $operator, '置顶了导览名称：'.$title,2,1,0,$operator_id);
        $message = Lang('Success');
        return output(200,$message);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/overview.log",$Log_content);
        return;
    }
}
