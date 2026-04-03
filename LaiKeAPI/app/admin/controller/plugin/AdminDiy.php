<?php
namespace app\admin\controller\plugin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;

use app\admin\model\DiyModel;

/**
 * 功能：DIY
 * 修改人：DHB
 */
class AdminDiy extends BaseController
{   
    // 获取diy数据
    public function getDiyList()
    {
        $store_id = $this->request->param('storeId');
        $id = $this->request->param('id');

        $list = array();
        $total = 0;
        $where_list = array('store_id'=>$store_id,'is_del'=>0);
        if($id != '' && $id != 0)
        {
            $where_list['id'] = $id;
        }
        $r0 = DiyModel::where($where_list)->field('count(id) as total')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $r1 = DiyModel::where($where_list)->select()->toArray();
        if($r1)
        {
            $list = $r1;
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang("Success");
        return output("200",$message,$data);
    }

    // 添加/编辑diy数据
    public function addOrUpdateDiy()
    {
        $store_id = $this->request->param('storeId');
        $id = $this->request->param('id');
        $name = $this->request->param('name');
        $cover = $this->request->param('cover');
        $values = $this->request->param('value');

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        if($id != '' && $id != 0)
        {
            $r0 = DiyModel::where(['store_id'=>$store_id,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
        }
        else
        {
            $r0 = DiyModel::where(['store_id'=>$store_id,'name'=>$name])->field('id')->select()->toArray();
        }
        if($r0)
        {
            $message = Lang('模板名称不能重复！');
            return output(109,$message);
        }

        $current_time = time();
        $update_time = time();

        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('name'=>$name,'cover'=>$cover,'value'=>$values);
            $r = Db::name('diy')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置失败！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $lktlog->customerLog($Log_content);
                $message = Lang('修改失败！');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $sql_inset = array('version'=>'1.0','name'=>$name,'value'=>$values,'add_time'=>$current_time,'update_time'=>$update_time,'status'=>0,'type'=>1,'is_del'=>0,'store_id'=>$store_id,'cover'=>$cover);
            $res = Db::name('diy')->insert($sql_inset);
            if ($res < 1) 
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '新增diy配置失败！', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 新增diy配置失败！参数：'.json_encode($sql_inset);
                $this->Log($Log_content);
                $message = Lang('未知原因，新增失败！');
                return output(109,$message);
            } 
            else 
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '新增diy配置成功！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 新增diy配置成功！';
                $this->Log($Log_content);
                $message = Lang("Success");
                return output("200",$message);
            }
        }
    }

    // 删除diy数据
    public function delDiy()
    {
        $store_id = $this->request->param('storeId');
        $id = $this->request->param('id');

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $sql_update = array('is_del'=>1);
        $r = Db::name('diy')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置失败！ID：' . $id, 2);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
            $lktlog->customerLog($Log_content);
            $message = Lang('修改失败！');
            return output(109,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置成功！ID：' . $id, 2);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 应用diy
    public function diyStatus()
    {
        $store_id = $this->request->param('storeId');
        $id = $this->request->param('id');

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $sql_where0 = array('store_id'=>$store_id);
        $sql_update0 = array('status'=>0);
        $r0 = Db::name('diy')->where($sql_where0)->update($sql_update0);

        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $sql_update = array('status'=>1);
        $r = Db::name('diy')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置失败！ID：' . $id, 2);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
            $lktlog->customerLog($Log_content);
            $message = Lang('修改失败！');
            return output(109,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改diy配置成功！ID：' . $id, 2);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改diy配置成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/adminDiy.log",$Log_content);
        return;
    }
}