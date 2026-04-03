<?php
declare(strict_types=1);
 
namespace app\common;
 
use think\facade\Db;
 
class RecoveryController
{
    protected $backupConfig;
 
    public function __construct()
    {
        $this->backupConfig = config('database');
    }
 
    public function recovery($array)
    {
        $store_id = $array['store_id'];
        $id = $array['id'];

        $time = date("Y-m-d H:i:s");

        $sql = "select * from lkt_backup_record where store_id = '$store_id' and id = '$id' ";
        $r = Db::query($sql);
        if($r)
        {
            $file_name = $r[0]['file_name'];
            $file_url = $r[0]['file_url'];
        }

        // 防止还原数据过程超时
        set_time_limit(0);
        ini_set('memory_limit', '1024M');
 
        $filename = input('get.filename');
 
        $content = file_get_contents($file_url);
 
        // 使用";"分割内容
        $statements = explode("-- ----------------------------", $content);
 
        // 开始事务
        Db::startTrans();
 
        foreach ($statements as $index => $stmt) 
        {
            if (trim($stmt) === '') 
            {
                continue;
            }

            if (strpos($stmt, "-- ") !== false) 
            {
                
            } 
            else 
            {
                if (strpos($stmt, "DROP TABLE IF EXISTS ") !== false) 
                {
                    $res = explode(";", $stmt);
                    
                    $results = Db::query($res[0]);
                    // if ($results === false) 
                    // {
                    //     Db::rollback();
                    //     return false;
                    // }

                    $results = Db::query($res[1]);
                    // if ($results === false) 
                    // {
                    //     Db::rollback();
                    //     return false;
                    // }
                } 
                else
                {
                    $res = explode("INSERT INTO ", $stmt);
                    foreach($res as $k => $v)
                    {
                        if($k != 0)
                        {
                            $v = str_replace("''", 'null', $v);
                            $rew = "INSERT INTO " . $v;
                            $results = Db::query($rew);
                            // if ($results === false) 
                            // {
                            //     Db::rollback();
                            //     return false;
                            // }
                        }
                    }
                }
            }
        }
 
        // 提交事务
        Db::commit();
 
        // 删除非压缩的文件
        // unlink($this->backupConfig['path'] . $filename);
 
        return true;
    }
}