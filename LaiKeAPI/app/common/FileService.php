<?php

namespace app\common;
use think\facade\Db;

class FileService
{
    /**
     * 文件夹打包zip并下载
     * @param string $path  源文件路径
     * @param string $filename 压缩文件名
     */
    public static function zipDown($path="",$filename="")
    {
        $path = iconv("UTF-8", "GBK", $path); //加这行中文文件夹也ok了

        $zip = new \ZipArchive();
        if($zip->open($filename.'.zip', \ZipArchive::CREATE | \ZipArchive::OVERWRITE)) 
        {
            FileService::addFileToZip($path, $zip);//调用方法，对要打包的根目录进行操作，并将ZipArchive的对象传递给方法
            $zip->close(); //关闭处理的zip文件
        }
        if(!file_exists('./' . $filename . '.zip'))
        {
            echo 1;die;
        }

        header("Cache-Control: public");
        header("Content-Description: File Transfer");
        header('Content-disposition: attachment; filename=' . basename($filename . '.zip')); //文件名
        header("Content-Type: application/zip"); //zip格式的
        header("Content-Transfer-Encoding: binary"); //告诉浏览器，这是二进制文件
        header('Content-Length: ' . filesize('./' . $filename. '.zip')); //告诉浏览器，文件大小
        @readfile('./' . $filename . '.zip');//下载到本地
        @unlink('./' . $filename . '.zip');//删除服务器上生成的这个压缩文件
    }

    public static function addFileToZip($path,$zip)
    {
        $handler=opendir($path); //打开当前文件夹由$path指定。
        while(($filename=readdir($handler))!==false)
        {
            if($filename != "." && $filename != "..")
            {//文件夹文件名字为'.'和‘..’，不要对他们进行操作
                if(is_dir($path."/".$filename))
                {
                    FileService::addFileToZip($path."/".$filename, $zip);
                }
                else
                {
                    $zip->addFile($path."/".$filename);
                    $zip->renameName($path."/".$filename,$filename);    //避免压缩文件多层目录
                }
            }
        }
        @closedir($handler);
    }
}
