<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;

use app\admin\model\UserModel;
use app\admin\model\ProductListModel;
use app\admin\model\ProductClassModel;

/**
 * 功能：数据管理
 * 修改人：PJY
 */
class District extends BaseController
{   
    // 地区列表
    public function districtList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id')));
        $keyword = addslashes(trim($this->request->param('district_name')));
        $page = addslashes($this->request->param('pageNo'));
        $pagesize = addslashes($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $total = 0;
        $list = array();

        if($id != '')
        {
            $total = 1;
            $sql1 = "select * from lkt_map where id = '$id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $list = $r1[0];
            }
            $data = array('model'=>$list);
        }
        else
        {
            $sql0 = "select count(id) as total from lkt_map";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $total = $r0[0]['total'];
            }
    
            if($keyword != '')
            {
                $sql1 = "select * from lkt_map where district_name like '%$keyword%' limit $start,$pagesize ";
            }
            else
            {
                $sql1 = "select * from lkt_map limit $start,$pagesize ";
            }
            $r1 = Db::query($sql1);
            if($r1)
            {
                $list = $r1;
            }
            $data = array('total'=>$total,'list'=>$list);
        }

        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 获取省、市
    public function allDistrict()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id')));
        $keyword = addslashes(trim($this->request->param('keyword')));
        $page = addslashes($this->request->param('pageNo'));
        $pagesize = addslashes($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $list = array();

        $sql0 = "select id,district_name,district_pid,district_country_num from lkt_map where district_level in (2,3) ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $list = $r0;
        }
        
        $data = array('list'=>$list);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 添加地区
    public function saveOrEditCountry()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id')));
        $district_pid = addslashes(trim($this->request->param('district_pid'))); // 上级地区ID
        $district_country_num = addslashes($this->request->param('district_country_num')); // 国家代码
        $district_name = addslashes($this->request->param('district_name')); // 地区名称

        $district_level = 2;
        if($district_pid != '')
        {
            $sql0 = "select district_level from lkt_map where id = '$district_pid' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $district_level = $r0[0]['district_level'];
            }
        }

        if($district_country_num == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '请选择国家代码！';
            $this->Log($Log_content);
            $message = Lang('district.0');
            return output(109,$message);
        }
        if($district_name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '请输入地区名称！';
            $this->Log($Log_content);
            $message = Lang('district.1');
            return output(109,$message);
        }
        else
        {
            if($id != '')
            {
                $sql1 = "select id from lkt_map where district_name = '$district_name' and district_country_num = '$district_country_num' and id != '$id' ";
            }
            else
            {
                $sql1 = "select id from lkt_map where district_name = '$district_name' and district_country_num = '$district_country_num' ";
            }
            $r1 = Db::query($sql1);
            if($r1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '地区名称重复！';
                $this->Log($Log_content);
                $message = Lang('district.2');
                return output(109,$message);
            }
        }
        if($id != '')
        {
            $sql = "update lkt_map set district_name = '$district_name',district_pid = '$district_pid',district_country_num = '$district_country_num' where id = '$id' ";
            $r = Db::execute($sql);
            if($r > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改地区成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改地区失败！sql:'. $sql;
                $this->Log($Log_content);
                $message = Lang('district.4');
                return output(109,$message);
            }
        }
        else
        {
            $sql = "insert into lkt_map(district_name,district_pid,district_country_num,district_show_order,district_childcount,district_level) value ('$district_name','$district_pid','$district_country_num',0,0,'$district_level') ";
            $r = Db::execute($sql);
            if($r > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加地区成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加地区失败！sql:'. $sql;
                $this->Log($Log_content);
                $message = Lang('district.3');
                return output(109,$message);
            }
        }
    }

    // 删除添加地区
    public function deleteDistrict()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id')));

        $sql = "delete from lkt_map where id = '$id' ";
        $r = Db::execute($sql);
        if($r > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除地区成功';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除地区失败！sql:'. $sql;
            $this->Log($Log_content);
            $message = Lang('district.5');
            return output(109,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/District.log",$Log_content);
        return;
    }
}
