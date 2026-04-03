<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\LaiKeLogUtils;

/**
 * 功能：国家管理
 * 修改人：DHB
 */
class Country extends BaseController
{
    // 国家列表
    public function countryList()
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

        $total = 0;
        $list = array();
        if($id != '')
        {
            $sql0 = "select count(id) as total from lkt_ds_country where id = '$id' ";
            $sql1 = "select * from lkt_ds_country where id = '$id' limit $start,$pagesize ";
        }
        else
        {
            if($keyword != '')
            {
                $sql0 = "select count(id) as total from lkt_ds_country where name like '%$keyword%' ";
                $sql1 = "select * from lkt_ds_country where name like '%$keyword%' limit $start,$pagesize ";
            }
            else
            {
                $sql0 = "select count(id) as total from lkt_ds_country ";
                $sql1 = "select * from lkt_ds_country limit $start,$pagesize ";
            }
        }
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $r1 = Db::query($sql1);
        if($r1)
        {
            $list = $r1;
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 添加/编辑国家
    public function saveOrEditCountry()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id')));
        $name = addslashes($this->request->param('name')); // 国家名称
        $zh_name = addslashes($this->request->param('zh_name')); // 中文名称
        $code = addslashes($this->request->param('code')); //英文编码
        $code2 = addslashes($this->request->param('code2')); // 电话区号
        $num3 = addslashes($this->request->param('num3')); // 数字编码
        $national_flag = addslashes($this->request->param('national_flag')); // 国旗

        if($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '国家名称不能为空！';
            $this->Log($Log_content);
            $message = Lang('country.0');
            return output(109,$message);
        }
        else
        {
            if($id != '')
            {
                $sql0 = "select id from lkt_ds_country where name = '$name' and id != '$id' ";
            }
            else
            {
                $sql0 = "select id from lkt_ds_country where name = '$name' ";
            }
            $r0 = Db::query($sql0);
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '国家名称重复！';
                $this->Log($Log_content);
                $message = Lang('country.1');
                return output(109,$message);
            }
        }
        if($zh_name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '中文名称不能为空！';
            $this->Log($Log_content);
            $message = Lang('country.2');
            return output(109,$message);
        }
        else
        {
            if($id != '')
            {
                $sql1 = "select id from lkt_ds_country where zh_name = '$zh_name' and id != '$id' ";
            }
            else
            {
                $sql1 = "select id from lkt_ds_country where zh_name = '$zh_name' ";
            }
            $r1 = Db::query($sql1);
            if($r1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '中文名称重复！';
                $this->Log($Log_content);
                $message = Lang('country.3');
                return output(109,$message);
            }
        }
        if($code2 == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '电话区号不能为空！';
            $this->Log($Log_content);
            $message = Lang('country.4');
            return output(109,$message);
        }
        // else
        // {
        //     if($id != '')
        //     {
        //         $sql2 = "select id from lkt_ds_country where code2 = '$code2' and id != '$id' ";
        //     }
        //     else
        //     {
        //         $sql2 = "select id from lkt_ds_country where code2 = '$code2' ";
        //     }
        //     $r2 = Db::query($sql2);
        //     if($r2)
        //     {
        //         $Log_content = __METHOD__ . '->' . __LINE__ . '电话区号重复！';
        //         $this->Log($Log_content);
        //         $message = Lang('country.5');
        //         return output(109,$message);
        //     }
        // }
        if($code == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '英文编码不能为空！';
            $this->Log($Log_content);
            $message = Lang('country.6');
            return output(109,$message);
        }
        else
        {
            if($id != '')
            {
                $sql3 = "select id from lkt_ds_country where code = '$code' and id != '$id' ";
            }
            else
            {
                $sql3 = "select id from lkt_ds_country where code = '$code' ";
            }
            $r3 = Db::query($sql3);
            if($r3)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '英文编码重复！';
                $this->Log($Log_content);
                $message = Lang('country.7');
                return output(109,$message);
            }
        }
        if($num3 == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '数字编码不能为空！';
            $this->Log($Log_content);
            $message = Lang('country.8');
            return output(109,$message);
        }
        else
        {
            if($id != '')
            {
                $sql4 = "select id from lkt_ds_country where num3 = '$num3' and id != '$id' ";
            }
            else
            {
                $sql4 = "select id from lkt_ds_country where num3 = '$num3' ";
            }
            $r4 = Db::query($sql4);
            if($r4)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '数字编码重复！';
                $this->Log($Log_content);
                $message = Lang('country.9');
                return output(109,$message);
            }
        }

        if($id != '')
        {
            $sql = "update lkt_ds_country set name = '$name',zh_name = '$zh_name',code = '$code',code2 = '$code2',num3 = '$num3',national_flag = '$national_flag' where id = '$id' ";
            $r = Db::execute($sql);
            if($r > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改国家成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改国家失败！sql:'. $sql;
                $this->Log($Log_content);
                $message = Lang('country.11');
                return output(109,$message);
            }
        }
        else
        {
            $sql = "insert into lkt_ds_country(name,zh_name,code,code2,num3,national_flag) value ('$name','$zh_name','$code','$code2','$num3','$national_flag') ";
            $r = Db::execute($sql);
            if($r > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加国家成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加国家失败！sql:'. $sql;
                $this->Log($Log_content);
                $message = Lang('country.10');
                return output(109,$message);
            }
        }
    }

    // 删除国家
    public function deleteCountry()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id')));

        $sql = "delete from lkt_ds_country where id = '$id' ";
        $r = Db::execute($sql);
        if($r > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除国家成功';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除国家失败！sql:'. $sql;
            $this->Log($Log_content);
            $message = Lang('country.12');
            return output(109,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/Country.log",$Log_content);
        return;
    }
}
