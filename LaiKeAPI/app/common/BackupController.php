<?php
declare(strict_types=1);
namespace app\common;
ini_set('memory_limit', '1024M');
// namespace appcontroller;

use think\facade\Db;
 
// use thinkacadeDb;
 
class BackupController
{
    protected $backupConfig;
 
    public function __construct()
    {
        $this->backupConfig = config('database');
    }
 
    public function backup($array)
    {
        $store_id = $array['store_id'];
        $file_type = $array['file_type'];

        $time = date("Y-m-d H:i:s");

        $sql = "select query_data,url from lkt_backup_config where store_id = '$store_id' ";
        $r = Db::query($sql);
        if($r)
        {
            $query_data = $r[0]['query_data'];
            $url = $r[0]['url'];
        }
        else
        {
            $message = Lang('operation failed');
            echo json_encode(array('code'=>109,'message'=>$message));
            exit;
        }
        if($file_type != '人工')
        {
            $url = str_replace("../", "./", $url);
        }
        // 防止备份数据过程超时
        set_time_limit(0);
 
        $database = $this->backupConfig['connections']['laiketui']['database'];
        $filename = date('Ymd-His', time()) . ".sql";
        // $path = $this->backupConfig['connections']['laiketui']['path'].$filename;
        $path = $url.$filename;
        
        // 检查目录是否存在或者是否有权限写入
        // if(!is_dir($this->backupConfig['connections']['laiketui']['path']))
        if(!is_dir($url))
        { // 不是目录，创建目录
            // mkdir($this->backupConfig['connections']['laiketui']['path'], 0777, true);
            mkdir($url, 0777, true);
        }
        else
        { // 是目录
            // if(!is_writeable($this->backupConfig['connections']['laiketui']['path']))
            if(!is_writeable($url))
            { // 检查指定的文件是否可写，不可写，改变文件模式
                // chmod($this->backupConfig['connections']['laiketui']['path'], 0777);
                chmod($url, 0777);
            }
        }
        // 备份所有数据表
        $result = Db::query("SHOW TABLES");

        $tables = array();
        foreach($result as $index => $row)
        {
            $tables[] = $row['Tables_in_'.$database];
        }

        // file_put_contents("./1.log","====0" . PHP_EOL,FILE_APPEND);
        // 备份所有表结构和表数据
        $content = '';
        foreach($tables as $table)
        {
            if($table != 'lkt_backup_record' && $table != 'lkt_backup_time' )
            // if($table = 'lkt_mch_browse' )
            {
                $content = $content . "-- ----------------------------" . PHP_EOL;
                $content = $content . "-- Table structure for " . $table . PHP_EOL;
                $content = $content . "-- ----------------------------" . PHP_EOL;
                $content = $content . "DROP TABLE IF EXISTS `" . $table  . "`;" . PHP_EOL;
                $content = $content . $this->backupTableSchema($table);
                $content = $content . "-- ----------------------------" . PHP_EOL;
                $content = $content . "-- Records of " . $table . PHP_EOL;
                $content = $content . "-- ----------------------------" . PHP_EOL;
                // $content = $content . $this->buildInsertSql($table);
                $res = file_put_contents($path,$content,FILE_APPEND);
                file_put_contents("./1.log",$table . PHP_EOL,FILE_APPEND);
                if($res !== false)
                {
                    $content = '';
                }
                $result = Db::query("SELECT * FROM `" . $table . "`");
                // file_put_contents("./1.log","SELECT * FROM `" . $table . "`" . PHP_EOL,FILE_APPEND);
                foreach ($result as $key => $value) 
                {
                    $keys = array_keys($value);
        
                    $values = join("','", $value);
                    // $insert = "INSERT INTO `" . $table . "` (`" . join("`,`", $keys) . "`) VALUES ('" . $values . "');" . PHP_EOL;
                    $insert = "INSERT INTO `" . $table . "` VALUES ('" . $values . "');" . PHP_EOL;
                    $res = file_put_contents($path,$insert,FILE_APPEND);
                }
                
                // file_put_contents("./1.log",$table . '写入数据' . PHP_EOL,FILE_APPEND);
            }
        }
        // file_put_contents("./1.log","====1" . PHP_EOL,FILE_APPEND);
        // 写入文件
        // $res = file_put_contents($path,$content);
        if($res !== false)
        {
            $fileSize = filesize($path);
            $status = 1;
            $recycle = 0;

            // // 是否需要压缩
            // if ($this->backupConfig['connections']['laiketui']['zip']) {
            //     $zip = new \ZipArchive();
            //     $zipfilename = $this->backupConfig['connections']['laiketui']['path'] . date('Ymd-His', time()) . ".zip";
                
            //     if ($zip->open($zipfilename, \ZipArchive::CREATE | \ZipArchive::OVERWRITE) === TRUE) 
            //     {
            //         $zip->addFile($path,$filename);
            //         $zip->close();
            //         // 删除非压缩的文件
            //         // unlink($path);
            //     } 
            //     else 
            //     {
            //         // 备份失败
            //     }
            // }
        }
        else
        {
            $status = 0;
            $recycle = 1;
        }

        if($file_type != '人工')
        {
            $path = str_replace("./", "../", $path);
        }

        $sql = "insert into lkt_backup_record(store_id,file_name,file_size,file_type,file_url,status,add_time,recycle) value ('$store_id','$filename','$fileSize','$file_type','$path','$status','$time','$recycle')";
        $r = Db::execute($sql);
        if($r > 0)
        {
            if($file_type == '人工')
            {
                $message = Lang('Success');
                echo json_encode(array('code'=>200,'message'=>$message));
                exit;
            }
            else
            {
                return;
            }
        }
        else
        {
            $message = Lang('operation failed');
            echo json_encode(array('code'=>109,'message'=>$message));
            exit;
        }
        return;
    }
 
    // 备份表结构
    protected function backupTableSchema($table)
    {
        $database = $this->backupConfig['connections']['laiketui']['database'];
        // $result = Db::query("SHOW CREATE TABLE `" . $table . "`");
        $result = Db::query("SHOW CREATE TABLE `" . $table . "`");

        // $result[0]['Create Table'] = str_replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS", $result[0]['Create Table']);

        $create = $result[0]['Create Table'] . ";" . PHP_EOL.PHP_EOL;
        return $create;
    }
 
    // 备份表数据
    protected function buildInsertSql($table)
    {
        $database = $this->backupConfig['connections']['laiketui']['database'];
        $result = Db::query("SELECT * FROM `" . $table . "`");
        
        $insert = '';
        foreach ($result as $key => $value) {
            $keys = array_keys($value);

            // $values = array_map(array(Db::class, 'quote'), array_values($value));
            // $values = join(",", $values);
            $values = join("','", $value);
            // $insert .= "INSERT INTO `" . $table . "` (`" . join("`,`", $keys) . "`) VALUES (" . $values . ");" . PHP_EOL;
            $insert .= "INSERT INTO `" . $table . "` (`" . join("`,`", $keys) . "`) VALUES ('" . $values . "');" . PHP_EOL;
        }
        $insert .= PHP_EOL;
        return $insert;
    }
}