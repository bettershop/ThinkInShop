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
use app\common\Jurisdiction;

use app\admin\model\MchModel;
use app\admin\model\MenuModel;
use app\admin\model\AdminModel;
use app\admin\model\AuthorityMappingModel;

/**
 * 功能：店铺菜单
 * 修改人：PJY
 */
class Menu extends BaseController
{
    //菜单列表
    public function GetMenuList()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));
        $shop_id = cache($access_id.'_'.$store_type);

        $id = $this->request->param('id');
        $lang_code = $this->request->param('lang_code');
        $name = $this->request->param('name');
        $sid = $this->request->param('sid');
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
        $list = array();
        $condition = " d.TYPE = 2 AND d.main_id in (0,$shop_id) and d.is_display = 0 and d.name not in (select plugin_name from lkt_plugins where store_id='$store_id' and status = 0) ";
        if($lang_code != '')
        {
            $condition .= " and d.lang_code = '$lang_code' ";
        }

        if($id != '')
        {
            // $condition = " d.TYPE = 2 AND d.main_id in (0,$shop_id) and d.is_display = 0 and d.id = '$id' and d.name not in (select plugin_name from lkt_plugins where store_id='$store_id' and status = 0) ";
            $condition .= " and d.id = '$id' ";

            $sql = "select e.* from (select d.*,row_number () over (PARTITION BY d.id) AS top from lkt_user_role a 
                                RIGHT JOIN lkt_user_authority b ON b.role_id=a.id
                                RIGHT JOIN lkt_authority_mapping c ON c.role_id = a.id 
                                RIGHT JOIN lkt_menu d ON d.id = c.menu_id WHERE $condition) as e where e.top < 2 order by e.sort desc limit $start,$pagesize ";
            $res = Db::query($sql);
            if($res)
            {
                $list = $res;
            }
            $total = count($res);
        }
        else
        {
            // $condition = " d.TYPE = 2 AND d.main_id in (0,$shop_id) and d.is_display = 0 and d.name not in (select plugin_name from lkt_plugins where store_id='$store_id' and status = 0) ";
            if($name != '')
            {
                $name_0 = Tools::FuzzyQueryConcatenation($name);
                $condition .= " and d.name like $name_0";
            }
            if($sid != '')
            {
                $condition .= " and d.sid = '$sid' ";
            }
            else
            {
                $condition .= " and d.sid = 0 ";
            }
            $sql_num = " select ifnull(count(e.id),0) as num from (select d.id from lkt_user_role a 
                                RIGHT JOIN lkt_user_authority b ON b.role_id=a.id
                                RIGHT JOIN lkt_authority_mapping c ON c.role_id = a.id 
                                RIGHT JOIN lkt_menu d ON d.id = c.menu_id WHERE $condition group by d.id ) as e ";
            $res_num = Db::query($sql_num);
            if($res_num)
            {
                $total = $res_num[0]['num'];
            }
            if($total > 0)
            {
                $sql = "select e.* from (select d.*,row_number () over (PARTITION BY d.id) AS top from lkt_user_role a 
                                RIGHT JOIN lkt_user_authority b ON b.role_id=a.id
                                RIGHT JOIN lkt_authority_mapping c ON c.role_id = a.id 
                                RIGHT JOIN lkt_menu d ON d.id = c.menu_id WHERE $condition) as e where e.top < 2 order by e.sort desc limit $start,$pagesize ";
                $res = Db::query($sql);
                if($res)
                {
                    $list = $res;
                }
            }
        }
        
        $data['list'] = $list;
        $data['total'] = $total;
        if($sid != '')
        {
            $r = MenuModel::where('id',$sid)->field('name')->select()->toArray();
            $data['fatherName'] = $r[0]['name'];
        }
        $message = Lang('Success');
        return output(200, $message,$data);

    }

    //删除菜单
    public function DelMenu()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $id = $this->request->param('id');
        
        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        //自营店ID
        $main_id = 1;
        $r0 = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id,role')->select()->toArray();
        if($r0)
        {
            $main_id = $r0[0]['shop_id'];
        }
        if($shop_id != $main_id)
        {
            $message = Lang('role.9');
            return output(ERROR_CODE_LJBNWK, $message);
        }

        //判断菜单是否为系统菜单
        $res = MenuModel::where('id',$id)->select()->toArray();
        if($res)
        {
            // if($res[0]['main_id'] == 0)
            // {
            //     $message = Lang('mch.64');
            //     return output(ERROR_CODE_CDSCSB, $message);
            // }
            //自营店ID
            $main_id = 1;
            $r0 = AdminModel::where(['store_id'=> $store_id,'recycle'=>0,'type'=>1])->field('shop_id,role')->select()->toArray();
            if($r0)
            {
                $main_id = $r0[0]['shop_id'];
            }
            if($shop_id != $main_id)
            {
                $message = Lang('role.9');
                return output(ERROR_CODE_LJBNWK, $message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }

        //判断是否有下级菜单
        $res_q = MenuModel::where('sid',$id)->select()->toArray();
        if($res_q)
        {
            $message = Lang('mch.65');
            return output(ERROR_CODE_SCSBQXSCZCD, $message);
        }

        Db::startTrans();
        //删除菜单
        $sql1 = MenuModel::find($id);
        $res1 = $sql1->delete();
        if(!$res1)
        {
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单ID为 ' . $id . ' 删除失败';
            $this->Log($Log_content);
            $message = Lang('operation failed');
            return output(ERROR_CODE_CDSCSB, $message);
        }
        //删除角色绑定菜单
        $res2 = AuthorityMappingModel::where('menu_id',$id)->delete();
        if($res2 < 0)
        {
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单ID为 ' . $id . ' 删除失败';
            $this->Log($Log_content);
            $message = Lang('operation failed');
            return output(ERROR_CODE_CDSCSB, $message);
        }

        $Jurisdiction->admin_record($store_id, $operator, '删除菜单,ID为' . $id, 3,2,$shop_id,$operator_id);

        Db::commit();
        $message = Lang('Success');
        return output(200, $message);
    }

    //添加菜单
    public function AddMenu()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $id = $this->request->param('id');//菜单id
        $level = $this->request->param('level');//菜单等级
        $name = $this->request->param('menuName');//菜单名称
        $path = $this->request->param('path');//页面所在路径文件夹
        $url = $this->request->param('menuUrl');
        $is_display = (int)$this->request->param('isDisplay');//是否禁用 1=是 0=否
        $sid = $this->request->param('fatherMenuId')?$this->request->param('fatherMenuId'):0;
        $text = $this->request->param('text');//导览简介
        $logo = $this->request->param('defaultLogo');//菜单logo
        $checked_logo = $this->request->param('checkedLogo');//选中后菜单logo

        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID

        //自营店ID
        $main_id = PC_Tools::SelfOperatedStore($store_id);
        if($shop_id != $main_id)
        {
            $message = Lang('role.27');
            return output(ERROR_CODE_LJBNWK, $message);
        }

        if($path == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单路径为空 ';
            $this->Log($Log_content);
            $message = Lang('mch.72');
            return output(ERROR_CODE_LJBNWK, $message);
        }
        if($level == 1)
        {
            if($logo == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 默认图标不能为空 ';
                $this->Log($Log_content);
                $message = Lang('mch.66');
                return output(ERROR_CODE_MRTBBNWK, $message);
            }
            if($checked_logo == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 选中图标不能为空';
                $this->Log($Log_content);
                $message = Lang('mch.67');
                return output(ERROR_CODE_XZTBBNWK, $message);
            }
        }
        else
        {
            if($sid == 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 上级菜单为空';
                $this->Log($Log_content);
                $message = Lang('mch.69');
                return output(ERROR_CODE_SJCDBNWK, $message);
            }
            if($url == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单地址为空';
                $this->Log($Log_content);
                $message = Lang('mch.71');
                return output(ERROR_CODE_LJBNWK, $message);
            }
        }
        if(empty($id))
        {
            if($name == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单名称不能为空 ';
                $this->Log($Log_content);
                $message = Lang('mch.70');
                return output(ERROR_CODE_CDMCBNWK, $message);
            }
            else
            {
                $sql_q = array('name'=>$name,'sid'=>$sid);
                //判断名称是否重复
                $res_q = MenuModel::where($sql_q)->select()->toArray();
                if($res_q)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单名称已存在 ';
                    $this->Log($Log_content);
                    $message = Lang('mch.68');
                    return output(ERROR_CODE_CDYCZ, $message);
                }
            }
            //获取排序
            $id = substr(uniqid(mt_rand(), true),0,19);
            $sort = MenuModel::where(['sid'=>$sid,'main_id'=>0,'type'=>2])->max('sort');
            $sql = new MenuModel();
            $sql->id = $id;
            $sql->main_id = 0;
            $sql->type = 2;
            $sql->sid = $sid;
            $sql->name = $name;
            $sql->level = $level;
            $sql->logo = $logo;
            $sql->checked_logo = $checked_logo;
            $sql->path = $path;
            $sql->url = $url;
            $sql->text = $text;
            $sql->is_display = $is_display;
            $sql->sort = $sort + 1;
            $sql->add_date = date('Y-m-d H:i:s');
            $sql->update_date = date('Y-m-d H:i:s');
            $sql->save();
            $res = $sql->id;
            if($res < 1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单添加失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }

            $Jurisdiction->admin_record($store_id, $operator, '增加' . $name . '菜单,ID为' . $res, 1,2,$shop_id,$operator_id);
        }
        else
        {
            if($name == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单名称不能为空 ';
                $this->Log($Log_content);
                $message = Lang('mch.70');
                return output(ERROR_CODE_CDMCBNWK, $message);
            }
            else
            {
                $sql_q = array('name'=>$name,'sid'=>$sid);
                //判断名称是否重复
                $res_q = MenuModel::where($sql_q)->where('id','<>',$id)->select()->toArray();
                if($res_q)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单名称已存在 ';
                    $this->Log($Log_content);
                    $message = Lang('mch.68');
                    return output(ERROR_CODE_CDYCZ, $message);
                }
            }
            if($sid == $id)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单上级不能是当前菜单 ';
                $this->Log($Log_content);
                $message = Lang('mch.73');
                return output(ERROR_CODE_CDSJBNSDQCD, $message);
            }
            //获取原上级
            $res_o = MenuModel::where('id',$id)->field('sid,sort')->select()->toArray();
            $old_sid = $res_o[0]['sid'];
            if($old_sid != $sid)
            {
                $sort = MenuModel::where(['sid'=>$sid,'main_id'=>$shop_id,'type'=>2])->max('sort') + 1;
            }
            else
            {
                $sort = $res_o[0]['sort'];
            }
            $sql = MenuModel::find($id);
            $sql->sid = $sid;
            $sql->name = $name;
            $sql->level = $level;
            $sql->logo = $logo;
            $sql->checked_logo = $checked_logo;
            $sql->path = $path;
            $sql->url = $url;
            $sql->text = $text;
            $sql->is_display = $is_display;
            $sql->sort = $sort;
            $sql->update_date = date('Y-m-d H:i:s');
            $res = $sql->save();
            if(!$res)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单id'.$id.'修改失败';
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(ERROR_CODE_CZSB, $message);
            }

            $Jurisdiction->admin_record($store_id, $operator, '修改' . $name . '菜单,ID为' . $id, 2,2,$shop_id,$operator_id);
        }

        $message = Lang('Success');
        return output(200, $message);
    }

    // 置顶菜单
    public function Top()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $id = $this->request->param('id');//菜单id

        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        $mch_id = PC_Tools::SelfOperatedStore($store_id);

        if($shop_id != $mch_id)
        {
            $message = Lang('role.9');
            return output(ERROR_CODE_LJBNWK, $message);
        }

        //获取菜单信息
        $res = MenuModel::where('id',$id)->select()->toArray();
        if($res)
        {
            // if($res[0]['main_id'] == 0)
            // {
            //     $message = Lang('mch.74');
            //     return output(ERROR_CODE_CZSB, $message);
            // }
            $sid = $res[0]['sid'];
        }
        else
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }
        //获取排序
        if($mch_id == $shop_id)
        {
            $sort = MenuModel::where(['sid'=>$sid])->max('sort');
        }
        else
        {
            $sort = MenuModel::where(['sid'=>$sid,'main_id'=>$shop_id])->max('sort');
        }
        $sql = MenuModel::find($id);
        $sql->sort = $sort + 1;
        $res = $sql->save();
        if(!$res)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单id'.$id.'置顶失败';
            $this->Log($Log_content);
            $message = Lang('operation failed');
            return output(ERROR_CODE_CZSB, $message);
        }

        $Jurisdiction->admin_record($store_id, $operator, '置顶菜单,ID为' . $id, 2,2,$shop_id,$operator_id);

        $message = Lang('Success');
        return output(200, $message);
    }

    //上移、下移
    public function SortMove()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $up = $this->request->param('moveId');//上移id
        $down = $this->request->param('moveId1');//下移id
        
        $Jurisdiction = new Jurisdiction();
        $shop_id = cache($access_id.'_'.$store_type);
        $operator = cache($access_id.'_uid'); // 用户user_id
        $operator_id = cache($access_id.'_7_operator_id'); // 用户ID
        $mch_id = PC_Tools::SelfOperatedStore($store_id);
        if($shop_id != $mch_id)
        {
            $message = Lang('role.9');
            return output(ERROR_CODE_LJBNWK, $message);
        }

        //获取上移菜单本身排序值
        $res1 = MenuModel::where('id',$up)->field('sort,main_id')->select()->toArray();
        if($res1)
        {
            // if($res1[0]['main_id'] == 0)
            // {
            //     $message = Lang('mch.74');
            //     return output(ERROR_CODE_CZSB, $message);
            // }
            $down_s = $res1[0]['sort'];
        }
        else
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }
        //获取下移菜单本身排序值
        $res2 = MenuModel::where('id',$down)->field('sort,main_id')->select()->toArray();
        if($res2)
        {
            // if($res2[0]['main_id'] == 0)
            // {
            //     $message = Lang('mch.74');
            //     return output(ERROR_CODE_CZSB, $message);
            // }
            $up_s = $res2[0]['sort'];
        }
        else
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }
        Db::startTrans();
        //上移
        $sql_u = MenuModel::find($up);
        $sql_u->sort = $up_s;
        $res_u = $sql_u->save();
        if(!$res_u)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单id'.$up.'上移失败';
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('operation failed');
            return output(ERROR_CODE_CZSB, $message);
        }
        //下移
        $sql_d = MenuModel::find($down);
        $sql_d->sort = $down_s;
        $res_d = $sql_d->save();
        if(!$res_d)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 菜单id'.$down.'下移失败';
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('operation failed');
            return output(ERROR_CODE_CZSB, $message);
        }
        Db::commit();

        $Jurisdiction->admin_record($store_id, $operator, '上移菜单,ID为' . $up . '，下移菜单.ID为' . $down, 2,2,$shop_id,$operator_id);

        $message = Lang('Success');
        return output(200, $message);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("mch/Menu.log",$Log_content);
        return;
    }
}

