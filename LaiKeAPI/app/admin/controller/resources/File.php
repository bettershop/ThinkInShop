<?php
namespace app\admin\controller\resources;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\LKTConfigInfo;
use app\common\OSSCommon;
use app\common\MinIOServer;
use app\common\Tools;
use app\common\PC_Tools;

use app\admin\model\ImgGroupModel;
use app\admin\model\AdminModel;

/**
 * 功能：资源
 * 修改人：DHB
 */
class File extends BaseController
{   
    // 上传图片
    public function uploadFiles()
    {
        $store_id = addslashes($this->request->param('storeId'))?addslashes($this->request->param('storeId')):addslashes($this->request->param('store_id'));
        $store_type = addslashes($this->request->param('storeType'))?addslashes($this->request->param('storeType')):addslashes($this->request->param('store_type'));
        $access_id = addslashes($this->request->param('accessId'))?addslashes($this->request->param('accessId')):addslashes($this->request->param('access_id'));

        $id = addslashes($this->request->param('id'));
        $supplier_id = (int)addslashes($this->request->param('supplierId')); // 供应商ID
        $mchId_get = addslashes($this->request->param('mchId'))?addslashes($this->request->param('mchId')):trim($this->request->param('shop_id')); // 店铺ID
        $group_id = addslashes($this->request->param('groupId'))?addslashes($this->request->param('groupId')):'-1'; // 分组ID
    	$title = addslashes($this->request->param('title'))?addslashes($this->request->param('title')):addslashes($this->request->param('name'));
        $explain = addslashes($this->request->param('explain'));
        $alternativeText = addslashes($this->request->param('alternativeText'));
        $describe = addslashes($this->request->param('describe'));

        $admin_name = '';
        if($store_type == 1 || $store_type == 2 || $store_type == 3 || $store_type == 4 || $store_type == 5 || $store_type == 6 || $store_type == 11)
        { // 微信小程序 或者 h5 或者 支付宝小程序 或者 字节跳动小程序 或者 百度小程序 或者 pc商城 或者 app
            $admin_name = $this->user_list['user_name'];
        }
        else if($store_type == 12)
        { // 供应商
            $admin_name = $this->user_list['supplier_name'];
        }
        else if($store_type == 7 || $store_type == 8)
        {
            $admin_name = $this->user_list['name'];
        }

        $mchId = 0;
        if($supplier_id == '' || $supplier_id == 0)
        {
            if($mchId_get != '' && $mchId_get != 0)
            {
                $mchId = $mchId_get;
            }
            if($mchId == 0 && $store_id != 0 && $store_id != '')
            {
                $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1])->select()->toArray();
                $mchId = $r_admin[0]['shop_id'];
            }
        }

        if($store_id == '' || $store_id == 0)
        {
            $store_id = '0';
        }
        if($store_type == '' || $store_type == 0)
        {
            $store_type = '0';
        }
        else if ($store_type == 11)
        {
            $store_type = 'app';
        }
        $imgUrls = array();
        if($id == '')
        {
            $dir = $store_id . '/' . $store_type . '/' . date("Ymd") . '/';

            if (!empty($_FILES))
            {
                $name = '';
                foreach ($_FILES as $key => $value)
                {
                    $name = $key;
                }

                $error = $_FILES[$name]['error'];
                switch ($_FILES[$name]['error'])
                {
                    case 0:
                        $msg = '';
                        break;
                    case 1:
                        $msg = '超出了php.ini中文件大小';
                        break;
                    case 2:
                        $msg = '超出了MAX_FILE_SIZE的文件大小';
                        break;
                    case 3:
                        $msg = '文件被部分上传';
                        break;
                    case 4:
                        $msg = '没有文件上传';
                        break;
                    case 5:
                        $msg = '文件大小为0';
                        break;
                    default:
                        $msg = '上传失败';
                        break;
                }

                $imgURL = $_FILES[$name]['tmp_name'];
                $files = $_FILES[$name]['name'];
                $pathinfo = pathinfo($files);

                $contentType = $_FILES[$name]['type'];
                $fType = explode('/', $contentType);

                $type = isset($pathinfo['extension'])?$pathinfo['extension']:$fType[1];

                $type_0 = PC_Tools::upload();
                // $type_0 = 1; // 图片
                // if($type != 'jpeg' && $type != 'jpg' && $type != 'swf' && $type != 'png' && $type != 'psd' && $type != 'svg' && $type != 'gif')
                // {
                //     $type_0 = 2; // 视频
                // }
                
                $imgURL_name = time() . mt_rand(1, 1000) . '.' .$type;
                $path = $dir . $imgURL_name;

                $upserver = 2;
                $common = LKTConfigInfo::getOSSConfig();
                try
                {
                    //查询文件上传配置方式
                    $sql = "select upserver from lkt_config where store_id = 1 ";
                    $r = Db::query($sql);
                    if($r)
                    {
                        $upserver = $r[0]['upserver'];
                    }

                    //阿里云
                    if($upserver == '2')
                    {
                        $ossClient = OSSCommon::getOSSClient();
                        $ossClient->uploadFile($common['bucket'], $path, $imgURL);
                    }
                    //MinIO
                    if($upserver == '5')
                    {
                        $ossClient = new MinIOServer();
                        $ossClient->upLoadObject($imgURL,$path,$contentType);
                    }
                }
                catch (OssException $e)
                {
                    printf(__FUNCTION__ . ": FAILED\n");
                    printf($e->getMessage() . "\n");
                    return;
                }

                $url = '';
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
                
                $fsql = " INSERT INTO `lkt_files_record` ( `store_id`, `store_type`, `group`, `upload_mode`, `image_name`,`mch_id`,`supplier_id`,`type`,`name`,`add_user`) VALUES ('$store_id', '$store_type', '$group_id', '$upserver', '$imgURL_name','$mchId','$supplier_id','$type_0','$files','$admin_name') ";
                $res = Db::execute($fsql);

                $imgUrls[] = $url;
                $data = array('extension' => $fType[1], 'size' => $_FILES[$name]['size'], 'type' => $fType[0], 'url' => $url, 'imgUrls'=>$imgUrls);
                $message = Lang('Success');
                return output(200,$message,$data);
            }
        }
        else
        {
            $sql_update = array('group'=>$group_id,'mch_id'=>$mchId);
            if($title != '')
            {
                $sql_update['title'] = $title;
            }
            if($explain != '')
            {
                $sql_update['explain'] = $explain;
            }
            if($alternativeText != '')
            {
                $sql_update['alternativeText'] = $alternativeText;
            }
            if($describe != '')
            {
                $sql_update['describe'] = $describe;
            }
            $sql_where = array('id'=>$id);
            $r = Db::name('files_record')->where($sql_where)->update($sql_update);
            $data = array('imgUrls'=>$imgUrls);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
    }
    
    // url上传
    public function uploadUrlFiles()
    {
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $accessId = addslashes($this->request->param('accessId'));

        $url = addslashes($this->request->param('imgUrl'));
        $alternative_text = addslashes($this->request->param('text'));
        $mchId_get = addslashes($this->request->param('mchId'));
        $mchId_post = addslashes($this->request->post('mchId'));
        $supplier_id = (int)addslashes($this->request->param('supplierId'));
        $mchId = 0;
    	if($mchId_get != '' && $mchId_get != 0)
    	{
    	    $mchId = $mchId_get;
    	}
    	else
    	{
    	    if($mchId_post != '' && $mchId_post != 0)
    	    {
    	        $mchId = $mchId_post;
    	    }
    	}
        $group_id = '-1';

        if($store_id == '' || $store_id == 0)
        {
            $store_id = '0';
        }
        if($store_type == '' || $store_type == 0)
        {
            $store_type = '0';
        }
        $uploadImg = "../public/image/url/";
        if (filter_var($url, FILTER_VALIDATE_URL) !== false)
        { // 地址正确
            $rename = time() . mt_rand(1, 1000);

            if ($store_id)
            {
                $file_path = $uploadImg . 'image_' . $store_id ;
            }
            else
            {
                $store_id = 0;
                $file_path = $uploadImg . 'image_0';
            }
            $res = $this->getFile($url,$file_path,$rename.'.png',1);
            $file_name = $res['file_name'];
            //查询文件上传配置方式
            $upserver = Db::name('config')->where('store_id', 1)->value('upserver');
            if (is_file($res['save_path']))
            {
                $common = LKTConfigInfo::getOSSConfig();
                $dir = $store_id . '/' . $store_type . '/' . date("Ymd") . '/' . $file_name;
                //阿里云
                if($upserver == '2')
                {
                    $ossClient = OSSCommon::getOssClient();
                    $ossClient->uploadFile($common['bucket'], $dir, $res['save_path']);
                }
                //MinIO
                if($upserver == '5')
                {
                    $infFile = getimagesize($url);
                    $mime = isset($infFile['mime'])?$infFile['mime']:'application/octet-stream';
                    $ossClient = new MinIOServer();                 
                    $ossClient->upLoadObject($res['save_path'],$dir,$mime);
                }              
                // unlink($res['save_path']);
            }
            $sql1 = " INSERT INTO `lkt_files_record` ( `store_id`, `store_type`, `group`, `upload_mode`, `image_name`,`add_time`,`mch_id`,`alternative_text`,`supplier_id`) VALUES ('$store_id', '$store_type', '$group_id', $upserver, '$file_name',CURRENT_TIMESTAMP,'$mchId','$alternative_text','$supplier_id') ";
            $r1 = Db::execute($sql1);
            if($r1 > 0)
            {
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $message = Lang('image.5');
                return output(109,$message);
            }
        }
        else
        { // 地址不正确
            $message = Lang('image.5');
            return output(109,$message);
        }
    }

    // 获取图片分类
    public function groupList()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));
        $supplier_id = (int)addslashes($this->request->param('supplierId'));//供应商id
        $mchId_get = addslashes($this->request->param('mchId')); // 店铺ID

        $shop_id = 0;
        if($mchId_get == '')
        {
            $sql0 = "select shop_id from lkt_admin where store_id = '$store_id' and type = '1' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $shop_id = $r0[0]['shop_id'];
            }
        }
        else
        {
            $shop_id = $mchId_get;
        }

        $list = array();
        if($supplier_id)
        {
            $r0 = ImgGroupModel::where(['store_id'=>$store_id,'supplier_id'=>$supplier_id])->select()->toArray();
        }
        else
        {
            $r0 = ImgGroupModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->select()->toArray();
        }
        
        if($r0)
        {
            $list = $r0;
        }

        $data = array('list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 获取图片
    public function index()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));
        $supplier_id = (int)addslashes($this->request->param('supplierId'));//供应商id

        $mchId_get = addslashes($this->request->param('mchId')); // 店铺ID
    	$group_id = addslashes($this->request->param('groupId'));
    	$title = addslashes($this->request->param('title'));
    	$startDate = addslashes($this->request->param('startDate'));
    	$endDate = addslashes($this->request->param('endDate'));
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据

        $shop_id = 0;

        if($supplier_id)
        {
            $admin_name = $this->user_list['supplier_name'];
        }
        else
        {
            $admin_name = $this->user_list['name'];
            if($mchId_get == '')
            {
                $sql0 = "select shop_id from lkt_admin where store_id = '$store_id' and type = '1' ";
                $r0 = Db::query($sql0);
                if($r0)
                {
                    $shop_id = $r0[0]['shop_id'];
                }
            }
            else
            {
                $shop_id = $mchId_get;
            }
        }

        if($pagesize == '')
        {
            $pagesize = 10;
        }
        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        // $con = " recycle = 0  ";
        $con = " recycle = 0 and image_name not like '%.webm' and image_name not like '%.mp4' ";
        if($store_id && $store_id != '0')
        {
            $con .= " and `store_id` = '$store_id' ";
        }
        if($supplier_id)
        {
            $con .= " and `supplier_id` = '$supplier_id' ";
        }
        if ($group_id)
        {
            $con .= " and `group` = '$group_id' ";
        }

        if($mchId_get == '')
        { // 后台进入
            if($group_id != -1)
            { // 不是全部
                $con .= " and mch_id = '$shop_id' ";
            }
        }
        else
        {
            $con .= " and mch_id = '$shop_id' ";
        }

        if($startDate != '')
        {
            $start_time = date("Y-m-1 00:00:00",strtotime($startDate));
            $con .= " and add_time >= '$start_time' ";
        }

        if($endDate != '')
        {
            $mdays = date( 't', strtotime($endDate) );
            $end_time = date("Y-m-".$mdays." 23:59:59",strtotime($endDate));
            $con .= " and add_time <= '$end_time' ";
        }

        if ($title != '')
        {
            $title_0 = Tools::FuzzyQueryConcatenation($title);
            $con .= " and `title` like $title_0 ";
        }

        $list = array();
        $total = 0;
        $sql0 = "select count(*) as file_count from lkt_files_record where $con  ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['file_count'];
        }

        $sql1 = "select * from lkt_files_record where $con order by add_time desc LIMIT $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $add_time = date("Ymd",strtotime($v['add_time']));
                $dir = $v['store_id'] . '/' . $v['store_type'] . '/' . $add_time  . '/';
                $url = '';
                //阿里云OSS
                if($v['upload_mode'] == '2')
                {
                    $common = LKTConfigInfo::getOSSConfig('2');
                    $url = 'https://' . $common["bucket"] . '.' . $common["endpoint"] . '/' . $dir . $v['image_name'];
                    if (strpos($common['endpoint'], "http") !== false) 
                    {
                        $url = $common["bucket"] . '.' . $common["endpoint"] . '/' . $dir . $v['image_name'];
                    }
                }              
                //MinIO
                if($v['upload_mode'] == '5')
                {
                    $common = LKTConfigInfo::getOSSConfig('5');
                    $url = 'http://' . $common['endpoint'] . '/' . $common['bucket'] . '/' . $dir . $v['image_name'];
                    if (strpos($common['endpoint'], "http") !== false) 
                    {
                        $url = $common['endpoint'] . '/' . $common['bucket'] . '/' . $dir . $v['image_name'];
                    }
                }

                $list[] = array('id'=>$v['id'],'store_id'=>$v['store_id'],'store_type'=>$v['store_type'],'group'=>$v['group'],'upload_mode'=>$v['upload_mode'],'image_name'=>$v['image_name'],'add_time'=>$v['add_time'],'recycle'=>$v['recycle'],'url'=>$url,'att_dir'=>$url);
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output('200',$message,$data);
    }

    // 创建图片分类
    public function createCatalogue()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));

        $supplierId_get = addslashes($this->request->param('supplierId')); // 供应商ID
        $mchId_get = addslashes($this->request->param('mchId')); // 店铺ID
    	$id = addslashes($this->request->param('id')); // 分类名称
    	$name = addslashes($this->request->param('catalogueName')); // 分类名称

        $time = date("Y-m-d H:i:s");
        $shop_id = 0;
        $supplier_id = 0;
        if($supplierId_get == '')
        {
            if($mchId_get == '')
            {
                $sql0 = "select shop_id from lkt_admin where store_id = '$store_id' and type = '1' ";
                $r0 = Db::query($sql0);
                if($r0)
                {
                    $shop_id = $r0[0]['shop_id'];
                }
            }
            else
            {
                $shop_id = $mchId_get;
            }
        }
        else
        {
            $supplier_id = $supplierId_get;
        }

        if($id == '')
        {
            $sql1 = "select id from lkt_img_group where store_id = '$store_id' and name = '$name' and type = '1' and supplier_id = '$supplier_id' and mch_id = '$shop_id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $message = Lang('resources.0');
                return output(50755,$message);
            }

            $sql = array('store_id'=>$store_id,'name'=>$name,'add_date'=>$time,'supplier_id'=>$supplier_id,'mch_id'=>$shop_id,'type'=>1);
            $r = Db::name('img_group')->insert($sql);
        }
        else
        {
            $sql1 = "select id from lkt_img_group where store_id = '$store_id' and name = '$name' and type = '1' and supplier_id = '$supplier_id' and mch_id = '$shop_id' and id != '$id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $message = Lang('resources.0');
                return output(50755,$message);
            }

            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('name'=>$name);
            $r = Db::name('img_group')->where($sql_where)->update($sql_update);
        }

        $message = Lang('Success');
        return output(200,$message);
    }

    // 删除图片分类
    public function delCatalogue()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));

    	$id = addslashes($this->request->param('id')); // 分类名称
        
        $time = date("Y-m-d H:i:s");
        
        $r = Db::table('lkt_img_group')->where(['store_id'=>$store_id,'id'=>$id])->delete();

        $r = Db::table('lkt_files_record')->where(['store_id'=>$store_id,'group'=>$id])->delete();

        $message = Lang('Success');
        return output(200,$message);
    }
    
    // 删除图片
    public function delFile()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));

    	$id = addslashes($this->request->param('id')); // 图片ID
        
        $time = date("Y-m-d H:i:s");
        
        $r = Db::table('lkt_files_record')->where(['id'=>$id])->delete();

        $message = Lang('Success');
        return output(200,$message);
    }
    
    // 远程下载文件并保存到指定路径
    function getFile($url, $save_dir = '', $filename = '', $type = 0) 
    {
        if (trim($url) == '') 
        {
            return false;
        }
        if (trim($save_dir) == '') 
        {
            $save_dir = './';
        }
        if (0 !== strrpos($save_dir, '/')) 
        {
            $save_dir.= '/';
        }
        //创建保存目录
        if (!file_exists($save_dir) && !mkdir($save_dir, 0777, true)) 
        {
            return false;
        }
        //获取远程文件所采用的方法
        if ($type) 
        {
            $ch = curl_init();
            $timeout = 5;
            curl_setopt($ch, CURLOPT_URL, $url);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
            curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, $timeout);
            $content = curl_exec($ch);
            curl_close($ch);
        } 
        else 
        {
            ob_start();
            readfile($url);
            $content = ob_get_contents();
            ob_end_clean();
        }
        $size = strlen($content);
        //文件大小
        $fp2 = @fopen($save_dir . $filename, 'a');
        fwrite($fp2, $content);
        fclose($fp2);
        unset($content, $url);
        return array(
            'file_name' => $filename,
            'save_path' => $save_dir . $filename
        );
    }
    
    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("resources/file.log",$Log_content);
        return;
    }
}
