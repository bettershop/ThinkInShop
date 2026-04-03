<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\BackupController;
use app\common\RecoveryController;

/**
 * 功能：数据备份
 * 修改人：DHB
 */

class BackUp extends BaseController
{   
    // 获取备份设置
    public function queryConfig()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $list = array();
        $sql = "select * from lkt_backup_config where store_id = 0 ";
        $r = Db::query($sql);
        if($r)
        {
            $list = $r[0];
        }
        $data = array('backUpConfigModel'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 获取备份文件列表
    public function backUpRecord()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $page = (int)$this->request->param('page'); // 页码
        $pagesize = (int)$this->request->param('pageSize'); // 每页显示多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $total = 0;
        $list = array();
        $sql0 = "select count(id) as total from lkt_backup_record where store_id = 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select * from lkt_backup_record where store_id = 0 and recycle = 0 limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k1 => $v1)
            {
                $file_url = $v1['file_url'];
                $v1['xz_url'] = str_replace("../public/", '/', $file_url);
                $list[] = $v1;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 保存备份设置
    public function addConfig()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $is_open = addslashes(trim($this->request->param('is_open'))); // 是否开启自动备份,0关闭，1开启
        $query_data = addslashes(trim($this->request->param('query_data'))); // 条件 1.每天 2.N天 3.每小时 4.N小时 5.N分钟 6.每周 7.每月
        $execute_cycle = addslashes(trim($this->request->param('execute_cycle'))); // 执行周期
        $url = addslashes(trim($this->request->param('url'))); // 保存路径

        $time = date("Y-m-d H:i:s");

        if($is_open == 1)
        {
            if($url == '')
            {
                $this->log(__LINE__ . ':保存路径不能为空！');
                $message = Lang("BackUp.0");
                return output(50326,$message);
            }
        }

        $sql = "select * from lkt_backup_config where store_id = 0 ";
        $r = Db::query($sql);
        if($r)
        {
            $sql0 = "update lkt_backup_config set is_open = '$is_open',query_data = '$query_data',execute_cycle = '$execute_cycle',url = '$url' where store_id = 0 and recycle = 0 ";
            $r0 = Db::execute($sql0);
            if($r0 < 1)
            {
                $this->log(__LINE__ . ':修改数据备份配置失败，sql0为：' . $sql0 );
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }
        else
        {
            $sql0 = "insert into lkt_backup_config(store_id,is_open,execute_cycle,url,add_time,query_data,recycle) value (0,'$is_open','$execute_cycle','$url','$time','$query_data',0)";
            $r0 = Db::execute($sql0);
            if($r0 < 1)
            {
                $this->log(__LINE__ . ':添加数据备份配置失败，sql0为：' . $sql0 );
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
        }

        $message = Lang('Success');
        return output(200,$message);
    }

    // 立即备份
    public function immediately()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $array = array('store_id'=>0,'file_type'=>'人工');
        $res = new BackupController();
        $res->backup($array);


        $message = Lang('Success');
        return output(200,$message);
    }

    // 还原
    public function reduction()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes(trim($this->request->param('id')));

        $array = array('store_id'=>0,'id'=>$id);
        $res = new RecoveryController();
        $res->recovery($array);

        $message = Lang('Success');
        return output(200,$message);
    }
    
    // 删除
    public function delBackUpRecord()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes(trim($this->request->param('id')));

        $sql = "update lkt_backup_record set recycle = 1 where store_id = 0 and id = '$id' ";
        $r = Db::execute($sql);

        $message = Lang('Success');
        return output(200,$message);
    }

    // 取消备份
    public function cancelTask()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes(trim($this->request->param('id')));

        $sql = "update lkt_backup_record set recycle = 1 where store_id = 0 and id = '$id' ";
        $r = Db::execute($sql);

        $message = Lang('Success');
        return output(200,$message);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/Backup.log",$Log_content);
        return;
    }
}
