<?php
namespace app\common;

use think\facade\Db;
use app\common\LKTConfigInfo;
use app\common\MinIOServer;
use app\admin\model\UploadSetModel;
use app\admin\model\FilesRecordModel;

class ServerPath
{
    public static $serverURL = null;
    public static $imgpath = null;

    // 后台获取图片路径
    public static function getimgpath($img, $store_id = 1)
    {
        $store_id = isset($_GET['store_id']) ? $_GET['store_id'] : $store_id;

        if ($img == '')
        {
            return $img;
        }
        $res = FilesRecordModel::where(['store_id'=>$store_id,'image_name'=>$img,'recycle'=>0])->select()->toArray();
        if ($res)
        {
            $store_type = $res[0]['store_type'];
            $upload_mode = $res[0]['upload_mode'];
            $add_time = date("Ymd",strtotime($res[0]['add_time']));
            if ($upload_mode == 1)
            {
                $r1 = UploadSetModel::where('upserver','=', '1')->field('attr,attrvalue')->select()->toArray();
                foreach ($r1 as $k => $v)
                {
                    if ($v['attr'] == 'uploadImg_domain')
                    {
                        $uploadImg_domain = $v['attrvalue']; // 图片上传域名
                    }
                    else if ($v['attr'] == 'uploadImg')
                    {
                        $uploadImg = $v['attrvalue']; // 图片上传位置
                    }
                }
                if ($store_id)
                {
                    $uploadImg = $uploadImg . 'image_' . $store_id . '/';
                }
                else
                {
                    $uploadImg = $uploadImg . 'image_0/';
                }
                if (strpos($uploadImg, './') === false)
                { // 判断字符串是否存在 ../
                    $imgpath = $uploadImg_domain . $uploadImg; // 图片路径
                }
                else
                { // 不存在
                    $imgpath = $uploadImg_domain . substr($uploadImg, 1); // 图片路径
                }
                if (self::$imgpath == null)
                {
                    self::$imgpath = $imgpath;
                }
                $image = self::$imgpath . $img;
            }
            elseif ($upload_mode == 5) 
            {
                $common = LKTConfigInfo::getOSSConfig('5');
                $image = 'http://' . $common['endpoint'] .'/'. $common['bucket']. '/' . $store_id . '/' . $store_type . '/' . $add_time  . '/' . $img;               
                if (strpos($common['endpoint'], "http") !== false) 
                {
                    $image = $common['endpoint'] .'/'. $common['bucket']. '/' . $store_id . '/' . $store_type . '/' . $add_time  . '/' . $img;
                }
            }
            else
            {    
                $serverURL = array(
                    'OSS' => 'https://',
                    'qiniu' => 'https://',
                    'tenxun' => 'https://'
                );
                if (self::$serverURL == null)
                {
                    $serverres = UploadSetModel::where('upserver','=', '2')->whereIn('attr','Bucket,Endpoint,isopenzdy,MyEndpoint')->select()->toArray();

                    if (!empty($serverres))
                    {   
                        foreach ($serverres as $k => $v)
                        {   
                            if ($v['type'] == '阿里云OSS' || $v['type'] == '阿里云oos')
                            {   
                                if ($v['attr'] == 'Bucket')
                                {   
                                    $OSS['Bucket'] = $v['attrvalue'];
                                }
                                if ($v['attr'] == 'Endpoint')
                                {
                                    $OSS['Endpoint'] = $v['attrvalue'];
                                }
                                if ($v['attr'] == 'isopenzdy')
                                {
                                    $OSS['isopenzdy'] = $v['attrvalue'];
                                }
                                if ($v['attr'] == 'MyEndpoint')
                                {
                                    $OSS['MyEndpoint'] = $v['attrvalue'];
                                }
                            }
                        }
                        if($OSS['isopenzdy'] == 0 )
                        {
                            $serverURL['OSS'] .= $OSS['Bucket'] . '.' . $OSS['Endpoint'];
                        }
                        else
                        {
                            //自定义域名
                            $serverURL['OSS'] .= $OSS['MyEndpoint'];
                        }
                    }
                    self::$serverURL = $serverURL;
                }

                if ($upload_mode == 2)
                {
                    $image = self::$serverURL['OSS'] . '/' . $store_id . '/' . $store_type . '/' . $add_time  . '/' . $img;
                }
            }
        }
        else
        {
            $image = self::$imgpath . $img;
        }

        return $image;
    }

    /**
     * @param $imgBase64 获取图片base64字符串
     * return String '返回阿里云上的图片路径
     */
    public static function base64_OSSupload($imgBase64, $store_id, $store_type)
    {
        $store_type = $store_type != '' ? $store_type : '0';
        $dir = $store_id . '/' . $store_type . '/' . date("Ymd") . '/';

        // 图片上传
        if (preg_match('/^(data:\s*image\/(\w+);base64,)/', $imgBase64, $res))
        {
            //获取图片类型
            $type = $res[2];
            //图片名字
            $fileName = time() . mt_rand(1, 1000) . $type;
            // 临时文件
            $tmpfname = tempnam("/tmp/", "FOO");
            $handle = fopen($tmpfname, "w");
            if (fwrite($handle, base64_decode(str_replace($res[1], '', $imgBase64))))
            {
                $path = $dir . $fileName;
                try
                {
                    $common = LKTConfigInfo::getOSSConfig();
                    $ossClient = OSSCommon::getOSSClient();
                    $ossClient->uploadFile($common['bucket'], $path, "/tmp/" . $tmpfname);
                } catch (OssException $e)
                {
                    printf(__FUNCTION__ . ": FAILED\n");
                    printf($e->getMessage() . "\n");
                    return false;
                }
                fclose($handle);
                unlink($tmpfname);

                $FilesRecordModel = new FilesRecordModel();
                $res = $FilesRecordModel->save([
                    'store_id'   =>  $store_id,
                    'store_type' =>  $store_type,
                    'group'      =>  '1',
                    'upload_mode' => '2',
                    'image_name'   =>  $fileName   
                ]);

                return $fileName;
            }
        }
    }

    //本地上传
    public static function base64_image_contents($base64_image_content, $path)
    {
        $db = DBAction::getInstance();
        if (preg_match('/^(data:\s*image\/(\w+);base64,)/', $base64_image_content, $result))
        {
            $type = $result[2];
            $new_file = $path . "/";
            if (!file_exists($new_file))
            {
                mkdir($new_file, 0700);
            }
            $imgname = time() . mt_rand(1, 1000);
            $new_file = $path . '/' . $imgname . ".{$type}";
            $storage_path = $imgname . ".{$type}";
            if (file_put_contents($new_file, base64_decode(str_replace($result[1], '', $base64_image_content))))
            {
                $FilesRecordModel = new FilesRecordModel();
                $res = $FilesRecordModel->save([
                    'store_id'   =>  $store_id,
                    'store_type' =>  $store_type,
                    'group'      =>  '1',
                    'upload_mode' => '1',
                    'image_name'   =>  $storage_path   
                ]);
               
                return $storage_path;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * 阿里云上传普通文件
     * @param $store_id String  商户id
     * @param $store_type String  平台类型
     * @param $returnpath bool  是否返回全路径
     * return String '返回阿里云上的图片路径
     */
    public static function file_OSSupload($store_id, $store_type = 'app', $returnpath = false,$mch_id = '')
    {
        $store_type = $store_type != '' ? $store_type : 'app';

        if($store_id == '' || $store_id == 0)
        {
            $store_id = '0';
        }
        if($store_type == '' || $store_type == 0)
        {
            $store_type = '0';
        }
        
        $name = array_keys($_FILES);
        $type = "";
        $contentType = '';
        if (!empty($name) && count($name) == 1)
        {
            list($name) = $name;
            //获取图片类型
            $imageName = $_FILES[$name]['name'];
            $arr = explode('.', $imageName);
            $type = "." . end($arr);
            $contentType = $_FILES[$name]['type'];
            $tmpname = $_FILES[$name]['tmp_name'];
        }
        
        $imgtype = array('.png', '.jpg', '.jpeg', '.gif');//
        $type = strtolower($type);
        if (!in_array($type, $imgtype))
        {
            return false;
        }
        //图片名字
        $fileName = time() . mt_rand(1, 1000) . $type;
        $dir = $store_id . '/' . $store_type . '/' . date("Ymd") . '/';
        $path = $dir . $fileName;
        $common = LKTConfigInfo::getOSSConfig();
        //查询文件上传配置方式
        $upserver = Db::name('config')->where('store_id', $store_id)->value('upserver');
        try
        {
            //阿里云
            if($upserver == '2')
            {
                $ossClient = OSSCommon::getOSSClient();
                $ossClient->uploadFile($common['bucket'], $path, $tmpname);
            }
            //MinIO
            if($upserver == '5')
            {
                $ossClient = new MinIOServer();
                $ossClient->upLoadObject($tmpname,$path,$contentType);
            } 
        }
         catch (OssException $e)
        {
            printf(__FUNCTION__ . ": FAILED\n");
            printf($e->getMessage() . "\n");
            return false;
        }

        if($upserver == '2')
        {
            $isopenzdy = $common['isopenzdy'];
            $url = 'https://' . $common['bucket'] . '.' . $common['endpoint'] . '/' . $path;
            if($isopenzdy == 1)
            {
                $url = 'https://'. $common['MyEndpoint'] . '/' . $path;
            }
        }
        if($upserver == '5')
        {
            $url = 'http://' . $common['endpoint'] . '/' . $common['bucket'] . '/' . $path;
            if (strpos($common['endpoint'], "http") !== false) 
            {
                $url = $common['endpoint'] . '/' . $common['bucket'] . '/' . $path;
            }
        }

        $FilesRecordModel = new FilesRecordModel();
        $res = $FilesRecordModel->save([
            'store_id'   =>  $store_id,
            'store_type' =>  $store_type,
            'group'      =>  '1',
            'upload_mode' => $upserver,
            'image_name'   =>  $fileName, 
            'mch_id'   =>  $mch_id   
        ]);
        if ($returnpath == false)
        {
            return $fileName;
        }
        else
        {
            return $url;
        }
    }

    /**
     * @param $store_id String  商户id
     * @param $uploadImg String  图片上传位置
     * @param $uploadImg_domain String  服务器域名
     * @param $returnpath bool  是否返回全路径
     * return String '返回阿里云上的图片路径
     */
    public static function file_upload($store_id, $uploadImg, $uploadImg_domain, $store_type = 'app', $returnpath = false,$mch_id = '')
    {
        $name = array_keys($_FILES);
        if (!empty($name) && count($name) == 1)
        {
            list($name) = $name;
            //获取图片类型
            $type = str_replace('image/', '.', $_FILES[$name]['type']);
            $tmpname = $_FILES[$name]['tmp_name'];
        }

        $imgtype = array('.png', '.jpg', '.jpeg', '.gif');
        if (!in_array($type, $imgtype))
        {
            return false;
        }

        $r1 = UploadSetModel::where('type', '本地')->field('attr,attrvalue')->select()->toArray();
        foreach ($r1 as $k => $v)
        {
            if ($v['attr'] == 'uploadImg_domain')
            {
                $uploadImg_domain = $v['attrvalue']; // 图片上传域名
            }
            else if ($v['attr'] == 'uploadImg')
            {
                $uploadImg = $v['attrvalue']; // 图片上传位置
            }
        }
        if ($store_id)
        {
            $uploadImg = $uploadImg . 'image_' . $store_id . '/';
        }
        else
        {
            $uploadImg = $uploadImg . 'image_0/';
        }

        if (is_dir($uploadImg) == '')
        { // 如果文件不存在
            mkdir($uploadImg); // 创建文件
        }
        if (strpos($uploadImg, './') === false)
        { // 判断字符串是否存在 ../
            $img = $uploadImg_domain . $uploadImg; // 图片路径
        }
        else
        {
            $img = $uploadImg_domain . substr($uploadImg, 1); // 图片路径
        }

        //图片名字
        $fileName = time() . mt_rand(1, 1000) . $type;
        move_uploaded_file($tmpname, $uploadImg . $fileName);
        $url = $img . $fileName;

        $FilesRecordModel = new FilesRecordModel();
        $res = $FilesRecordModel->save([
            'store_id'   =>  $store_id,
            'store_type' =>  $store_type,
            'group'      =>  '1',
            'upload_mode' => '1',
            'image_name'   =>  $fileName, 
            'mch_id'   =>  $mch_id   
        ]);

        if ($returnpath == false)
        {
            return $fileName;
        }
        else
        {
            return $url;
        }
    }

    public function removeimg($img, $store_id = 1)
    {
        $store_id = isset($_GET['store_id']) ? $_GET['store_id'] : $store_id;

        $res = FilesRecordModel::where('store_id', $store_id)->where('image_name',$img)->select()->toArray();
        if (!empty($res))
        {
            $store_type = $res[0]->store_type;
            $upload_mode = $res[0]->upload_mode;
        }
    }
}


?>
