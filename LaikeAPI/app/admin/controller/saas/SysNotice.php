<?php
namespace app\admin\controller\saas;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;

use app\admin\model\SystemTellModel;

/**
 * 功能：控制台公告管理类
 * 修改人：PJY
 */
class SysNotice extends BaseController
{   
    // 公告列表
    public function getSysNoticeInfo()
    {   
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_token = trim($this->request->param('accessId'));

        $id = $this->request->param('id');
        $page = $this->request->param('pageNo');
        $pagesize = $this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize : '10';
        
        $admin_id = cache($access_token.'admin_id');

        // 页码
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }
        if($id != '' && $id != 0)
        {
            $r = SystemTellModel::where(['store_id'=>0,'id'=>$id])->select()->toArray();
        }
        else
        {
            // 查询插件表
            $r = SystemTellModel::where('store_id',0)->order('add_time','desc')->limit($start,$pagesize)->select()->toArray();
        }
        if (!empty($r)) 
        {
            foreach ($r as $k => $v)
            {
                $tell_id = $v['id'];
                $status = '生效中';
                $now_timestamp = time();
                $start_timestamp = strtotime($v['startdate']);
                $end_timestamp = strtotime($v['enddate']);
                if ($now_timestamp < $start_timestamp) 
                {
                    $status = '未生效';
                } 
                else if ($now_timestamp > $start_timestamp && $now_timestamp < $end_timestamp)
                {
                    $status = '生效中';
                } 
                else if ($now_timestamp > $end_timestamp) 
                {
                    $status = '已失效';
                }
                $storeTypes = array();
                $storeTypesName = '';
                if($v['store_tell'] == 2)
                { // 管理后台
                    $storeTypes[] = 0;
                    $storeTypesName .= '管理后台,';
                }
                if($v['user_tell'] == 2)
                { // 用户
                    $storeTypes[] = 1;
                    $storeTypesName .= '用户端,';
                }
                if($v['mch_tell'] == 2)
                { // 商户
                    $storeTypes[] = 2;
                    $storeTypesName .= '商家端,';
                }
                if($v['supplier_tell'] == 2)
                { // 供应商
                    $storeTypes[] = 3;
                    $storeTypesName .= '供应商端,';
                }
                if($v['mch_son_tell'] == 2)
                { // 门店端
                    $storeTypes[] = 4;
                    $storeTypesName .= '门店端,';
                }
                $storeTypesName = trim($storeTypesName,',');
                $r[$k]['status'] = $status;
                $r[$k]['storeTypes'] = $storeTypes;
                $r[$k]['storeTypesName'] = $storeTypesName;
                
                $r[$k]['is_read'] = 0;
                $sql_ = "select id from lkt_system_tell_user where store_id = '$store_id' and tell_id = '$tell_id' and read_id = '$admin_id' and store_type = '$store_type' ";
                $r_ = Db::query($sql_);
                if($r_)
                {
                    $r[$k]['is_read'] = 1;
                }
            }
        }

        $total = SystemTellModel::where('store_id',0)->count();
        $message = Lang("Success");
        return output(200,$message,array('list'=>$r,'total'=>$total));
    }

    //添加公告
    public function addSysNoticeInfo()
    {   
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->post('id'));//公告id
        $title = trim($this->request->post('title')); // notice
        $telltype = $this->request->post('type');//公告类型
        $startdate = trim($this->request->post('startDate')); // 活动图片
        $enddate = trim($this->request->post('endDate')); // 活动介绍
        $detail = trim($this->request->post('content')); // 活动介绍
        $storeTypes = trim($this->request->post('storeTypes')); // 接收方
        
        if ($title == '')
        {   
            $message = Lang("sysNotice.0");
            return output(ERROR_CODE_BTBNWK,$message);
        }
        $now = date('Y-m-d H:i:s');
        // 不限制时间
        if (empty($startdate))
        {   
            $message = Lang("sysNotice.1");
            return output(ERROR_CODE_KSSJBNWK,$message);
        }
        if ($startdate < $now)
        {   
            $message = Lang("sysNotice.4");
            return output(ERROR_CODE_KSSJBNWK,$message);
        }

        if (empty($enddate))
        {   
            $message = Lang("sysNotice.2");
            return output(ERROR_CODE_JSSJBNWK,$message);
        }

        if($storeTypes == '')
        {
            $message = Lang("sysNotice.5");
            return output(ERROR_CODE_JSSJBNWK,$message);
        }
        
        $store_tell = 1;
        $user_tell = 1;
        $mch_tell = 1;
        $supplier_tell = 1;
        $mch_son_tell = 1;

        $storeTypes_list = explode(',',$storeTypes);
        if(in_array(0,$storeTypes_list))
        {
            $store_tell = 2;
        }
        if(in_array(1,$storeTypes_list))
        {
            $user_tell = 2;
        }
        if(in_array(2,$storeTypes_list))
        {
            $mch_tell = 2;
        }
        if(in_array(3,$storeTypes_list))
        {
            $supplier_tell = 2;
        }
        if(in_array(4,$storeTypes_list))
        {
            $mch_son_tell = 2;
        }
        
        if (empty($detail))
        {   
            $message = Lang("sysNotice.6");
            return output(ERROR_CODE_JSSJBNWK,$message);
        }
        
        if($id != '')
        {
            //查询同类型有效公共
            $i = true;
            $res_all = SystemTellModel::where('enddate','>',$now)
                                    ->where('type',$telltype)
                                    ->where('id','<>',$id)
                                    ->select()
                                    ->toArray();
            if($res_all)
            {   
                foreach ($res_all as $key => $value) 
                {
                    if($startdate > $value['enddate'] ||  $enddate < $value['startdate'])
                    {
                        
                    }
                    else
                    {   
                        $i = false;
                        $message = Lang("sysNotice.3");
                        return output(ERROR_CODE_CSCW,$message);
                    }
                }
            }
            if($i)
            {
                //更新数据表
                $sql = SystemTellModel::where(['store_id'=>0,'id'=>$id])->find();
                $sql->title = $title;
                $sql->startdate = $startdate;
                $sql->enddate = $enddate;
                $sql->content = $detail;
                $sql->store_tell = $store_tell;
                $sql->user_tell = $user_tell;
                $sql->mch_tell = $mch_tell;
                $sql->supplier_tell = $supplier_tell;
                $sql->mch_son_tell = $mch_son_tell;
                $r = $sql->save();
                if (!$r)
                {
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_CZSB,$message);
                }
                else
                {
                    $message = Lang("Success");
                    return output(200,$message);
                }
            }
        }
        else
        {
            //查询同类型有效公共
            $i = true;
            $res_all = SystemTellModel::where('enddate','>',$now)
                                    ->where('type',$telltype)
                                    ->select()
                                    ->toArray();
            if($res_all)
            {   
                foreach ($res_all as $key => $value) 
                {
                    if($startdate > $value['enddate'] ||  $enddate < $value['startdate'])
                    {
                        
                    }
                    else
                    {   
                        $i = false;
                        $message = Lang("sysNotice.3");
                        return output(ERROR_CODE_CSCW,$message);
                    }
                }
            }
            if($i)
            {
                $sql = new SystemTellModel();
                $sql->store_id = 0;
                $sql->title = $title;
                $sql->type = $telltype;
                $sql->startdate = $startdate;
                $sql->enddate = $enddate;
                $sql->content = $detail;
                $sql->add_time = date("Y-m-d H:i:s");
                $sql->store_tell = $store_tell;
                $sql->user_tell = $user_tell;
                $sql->mch_tell = $mch_tell;
                $sql->supplier_tell = $supplier_tell;
                $sql->mch_son_tell = $mch_son_tell;
                $sql->save();
                $rr = $sql->id;
                if ($rr < 1)
                {
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_CZSB,$message);
                }
                else
                {
                    $message = Lang("Success");
                    return output(200,$message);
                }
            }
        }   
    }

    //删除公告
    public function delSysNoticeInfo()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id'));//公告id
        $sql = SystemTellModel::find($id);
        $res = $sql->delete();
        if (!$res)
        {
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
        else
        {
            $message = Lang("Success");
            return output(200,$message);
        }
    }

    // 阅读公告
    public function readSysNotice()
    {
        $admin_list = $this->user_list;
        $admin_id = $admin_list['id'];
        $admin_name = $admin_list['name'];
        $admin_type1 = $admin_list['type'];
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_token = trim($this->request->param('accessId'));

        $id = trim($this->request->param('ids'));//公告id

        $admin_id = cache($access_token.'admin_id');
        $time = date("Y-m-d H:i:s");

        $sql0 = "select id from lkt_system_tell where store_id = 0 and id = '$id'";
        $r0 = Db::query($sql0);
        if ($r0)
        {
            $sql1 = "insert lkt_system_tell_user(store_id,tell_id,read_id,store_type,is_supplier,add_time) value ('$store_id','$id','$admin_id','$store_type','0','$time') ";
            $r1 = Db::execute($sql1);
            if ($r1 == -1)
            {
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            else
            {
                $message = Lang("Success");
                return output(200,$message);
            }
        }
    }
}
