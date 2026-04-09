<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\ServerPath;
use app\common\LKTConfigInfo;
use app\common\OSSCommon;
use app\common\MinIOServer;
use app\common\FileService;
use app\common\Tools;
use app\common\PC_Tools;

use app\admin\model\ProLabelModel;
use app\admin\model\ProductListModel;
use app\admin\model\ImgGroupModel;
use app\admin\model\AdminModel;

/**
 * 功能:资源类
 * 修改人:DHB
 */
class Resources extends BaseController
{
    // 图片管理
    public function index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $type = addslashes(trim($this->request->param('type'))); // 资源类型，1图片，2视频
        $group_id = addslashes($this->request->param('groupId'))?addslashes($this->request->param('groupId')):'-1'; // 分组ID
        $search = addslashes(trim($this->request->param('imageName'))); // 标题
        $startTime = addslashes(trim($this->request->param('startTime'))); // 标题
        $endTime = addslashes(trim($this->request->param('endTime'))); // 标题
        $pageNo = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';
        $start = 0;
        if ($pageNo)
        {
            $start = ($pageNo - 1) * $pagesize;
        }

        $condition = " a.store_id = '$store_id' AND a.mch_id >= 0 and a.recycle = 0 and a.type = '$type' ";
        if($group_id != -1)
        {
            $condition .= " and a.group = '$group_id' ";
        }
        if ($search)
        {
            $search1 = Tools::FuzzyQueryConcatenation($search);
            $condition .= " and (b.name like $search1 or b.id = '$search') ";
        }
        if ($startTime)
        {
            $condition .= " and a.add_time >= '$startTime' ";
        }
        if ($endTime)
        {
            $condition .= " and a.add_time <= '$endTime' ";
        }
        $total = 0;
        $list = array();
        $sql0 = "select count(a.id) as total from lkt_files_record as a left join lkt_mch as b on a.mch_id = b.id where $condition";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }
        
        $sql1 = "select a.*,b.name as mchName from lkt_files_record as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.add_time desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $key => $value) 
            {
                $imgurl = ServerPath::getimgpath($value['image_name'], $store_id);
                $value['imgUrl'] = $imgurl;
                $size = @get_headers($imgurl,true);
                if($size)
                {
                    $value['size'] = $this->trans_byte($size['Content-Length']);
                }
                else
                {
                    $value['size'] = 0;
                }
                $list[] = $value;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        return output(200,$message,$data);
    }
    
    // 获取图片分组
    public function groupList()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $type = addslashes(trim($this->request->param('type'))); // 资源类型，1图片，2视频
        
        $list = array();

        $shop_id = 0;
        $sql0 = "select shop_id from lkt_admin where store_id = '$store_id' and type = '1' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $shop_id = $r0[0]['shop_id'];
        }

        $sql1 = "select id,store_id,name,sort,add_date,is_default,mch_id,type,is_show from lkt_img_group where store_id = '$store_id' and type = '$type' and mch_id = '$shop_id' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $key => $value) 
            {
                $list[] = $value;
            }
        }

        $data = array('list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加图片分组
    public function addGroup()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $type = addslashes(trim($this->request->param('type'))); // 资源类型，1图片，2视频
        $id = addslashes($this->request->param('id')); // 分类名称
    	$name = addslashes($this->request->param('catalogueName')); // 分类名称
        
        $time = date("Y-m-d H:i:s");

        $shop_id = 0;
        $sql0 = "select shop_id from lkt_admin where store_id = '$store_id' and type = '1' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $shop_id = $r0[0]['shop_id'];
        }

        if($id == '')
        {
            $sql1 = "select id from lkt_img_group where store_id = '$store_id' and name = '$name' and type = '$type' and mch_id = '$shop_id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $message = Lang('resources.0');
                return output(50755,$message);
            }

            $sql = array('store_id'=>$store_id,'name'=>$name,'add_date'=>$time,'mch_id'=>$shop_id,'type'=>$type);
            $r = Db::name('img_group')->insert($sql);
        }
        else
        {
            $sql1 = "select id from lkt_img_group where store_id = '$store_id' and name = '$name' and type = '1' and mch_id = '$shop_id' and id != '$id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $message = Lang('resources.0');
                return output(50755,$message);
            }
            
            $sql_where = array('store_id'=>$store_id,'id'=>$id,'mch_id'=>$shop_id);
            $sql_update = array('name'=>$name,'type'=>$type);
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

    // 修改是否常驻
    public function updateCatalogueShow()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));

        $type = addslashes(trim($this->request->param('type'))); // 资源类型，1图片，2视频
    	$id = addslashes($this->request->param('id')); // 分类名称
        
        $shop_id = 0;
        $sql0 = "select shop_id from lkt_admin where store_id = '$store_id' and type = '1' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $shop_id = $r0[0]['shop_id'];
        }

        $sql0 = "update lkt_img_group set is_show = 1 where store_id = '$store_id' and mch_id = '$shop_id' and type = '$type' and id not in ($id)";
        Db::execute($sql0);

        $sql1 = "update lkt_img_group set is_show = 2 where store_id = '$store_id' and mch_id = '$shop_id' and type = '$type' and id in ($id)";
        Db::execute($sql1);

        $message = Lang('Success');
        return output(200,$message);
    }

    // 上传图片
    public function uploadFiles()
    {
        $store_id = addslashes($this->request->param('storeId'))?addslashes($this->request->param('storeId')):addslashes($this->request->param('store_id'));
        $store_type = addslashes($this->request->param('storeType'))?addslashes($this->request->param('storeType')):addslashes($this->request->param('store_type'));
        $accessId = addslashes($this->request->param('accessId'))?addslashes($this->request->param('accessId')):addslashes($this->request->param('access_id'));

        $id = addslashes($this->request->param('id'));
        $group_id = addslashes($this->request->param('groupId'))?addslashes($this->request->param('groupId')):'-1'; // 分组ID
    	$title = addslashes($this->request->param('name'));
        $explain = addslashes($this->request->param('explain'));
        $alternativeText = addslashes($this->request->param('alternativeText'));
        $describe = addslashes($this->request->param('describe'));

        $admin_name = cache($accessId.'admin_name');
        
        $supplier_id = 0;
        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1])->select()->toArray();
        $mchId = $r_admin[0]['shop_id'];

        $type_0 = 1;
        if($group_id != -1)
        {
            $sql_0 = "select type from lkt_img_group where store_id = '$store_id' and mch_id = '$mchId' and id = '$group_id' ";
            $r_0 = Db::query($sql_0);
            if($r_0)
            {
                $type_0 = $r_0[0]['type'];
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
                $sql_update['name'] = $title;
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

    // 转移分类
    public function updateCatalogueByImageIds()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));

        $catalogueId = addslashes(trim($this->request->param('catalogueId'))); // 图片分组ID
    	$imageIds = addslashes($this->request->param('imageIds')); // 图片ID
        
        $sql1 = "update lkt_files_record set `group` = '$catalogueId' where store_id = '$store_id' and id = '$imageIds' ";
        $r1 = Db::execute($sql1);

        $message = Lang('Success');
        return output(200,$message);
    }

    // 下载
    public function downForZip()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$store_type = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));

        $exportType = addslashes(trim($this->request->param('exportType'))); // 
    	$imageIds = addslashes($this->request->param('imgIds')); // 图片ID
        
        // 根据商城ID、图片ID，查询图片数据
        $sql0 = "select * from lkt_files_record where store_id = '$store_id' and id = '$imageIds' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $imgurl = ServerPath::getimgpath($r0[0]['image_name'], $store_id);
            $zip_name = time() . mt_rand(1, 1000);

            $base_file_path = '../public/image/download/'.$zip_name.'/';
            if(!is_dir($base_file_path)){
                mkdir ($base_file_path,0777,true);
                chmod ($base_file_path,0777);
            }
            copy($imgurl,$base_file_path.basename($imgurl));   //将要下载的图片copy到下载目录

            // $list = "这里获取要下载的图片列表";
            // foreach($list as $val){
            //     copy($val,$base_file_path.basename($val));   //将要下载的图片copy到下载目录
            // }
            FileService::zipDown($base_file_path,$zip_name);  //调用完成下载
        }

        $message = Lang('Success');
        return output(200,$message);
    }

    public function trans_byte($byte)
    {
        $KB = 1024;
        $MB = 1024 * $KB;
        $GB = 1024 * $MB;
        $TB = 1024 * $GB;
        if ($byte < $KB) 
        {
            return $byte . "B";
        } 
        elseif ($byte < $MB) 
        {
            return round($byte / $KB, 2) . "KB";
        } 
        elseif ($byte < $GB) 
        {
            return round($byte / $MB, 2) . "MB";

        } 
        elseif ($byte < $TB) 
        {
            return round($byte / $GB, 2) . "GB";

        } 
        else 
        {
            return round($byte / $TB, 2) . "TB";
        }
    }

    // 批量删除
    public function del()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $ids = addslashes(trim($this->request->param('imgIds'))); // ID字符串

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if($ids == '')
        {
            $message = Lang('resources.0');
            return output(400,$message);
        }
        Db::startTrans();
        $id_list = explode(',',$ids);
        
        foreach ($id_list as $key => $value) 
        {
            $sql_where = array('id'=>$value);
            $sql_update = array('recycle'=>1);
            $r = Db::name('files_record')->where($sql_where)->update($sql_update);
            if($r < 0)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 删除图片记录失败！参数:'.json_encode($sql_where);
                $this->Log($Log_content);
                $Jurisdiction->admin_record($store_id, $operator, '将管图片资源进行了批量删除操作失败',3,1,0,$operator_id);
                $message = Lang('Abnormal business');
                return output(400,$message);
            }
        }
        Db::commit();
        $Jurisdiction->admin_record($store_id, $operator, '将管图片资源进行了批量删除操作',3,1,0,$operator_id);
        $message = Lang('Success');
        return output(200,$message);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/resources.log",$Log_content);
        return;
    }
}
