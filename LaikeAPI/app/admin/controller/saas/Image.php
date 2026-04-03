<?php
namespace app\admin\controller\saas;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;

use app\common\LaiKeLogUtils;
use app\admin\model\UploadSetModel;

/**
 * 功能：数据字典
 * 修改人：DHB
 */
class Image extends BaseController
{
    // 获取图片设置
    public function getImageConfigInfo()   
    {
        $upserver = addslashes($this->request->param('type'));//默认阿里云
        $list = array();
        
        if (empty($upserver)) 
        {
            $upserver = Db::name('config')->where('store_id', 1)->value('upserver');
        }
        
        $res = UploadSetModel::where(['upserver'=>$upserver])->select()->toArray();
        if($res)
        {
            foreach ($res as $key => $value)
            {
                foreach ($value as $k => $val) 
                {
                    if ($k == 'upserver') 
                    {
                        //upserver 转字符串
                        $res[$key][$k] = (string)$val;
                    }
                }
                if (isset($value['attr']) && in_array($value['attr'], ['AccessKeyID', 'AccessKeySecret'], true))
                {
                    $res[$key]['attrvalue'] = $this->maskValue($value['attrvalue'], 3, 4);
                }
            }
            $list = $res;
        }

        $data = array('data'=>$list);
        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 设置图片设置
    public function addImageConfigInfo()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$storeType = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));
    	
        $upserver = addslashes($this->request->param('type'))?addslashes($this->request->param('type')):'2'; // upserver  默认阿里云
    	$ossbucket = addslashes($this->request->param('ossbucket')); // Bucket
    	$ossendpoint = addslashes($this->request->param('ossendpoint')); // Endpoint
    	$myEndpoint = addslashes($this->request->param('myEndpoint')); // MyEndpoint
    	$isOpenDiyDomain = addslashes($this->request->param('isOpenDiyDomain')); // 是否开启自定义域名
    	$ossaccesskey = addslashes($this->request->param('ossaccesskey')); // Access Key ID
    	$ossaccesssecret = addslashes($this->request->param('ossaccesssecret')); // Access Key Secret
    	$ossimgstyleapi = addslashes($this->request->param('ossimgstyleapi')); // 图片样式接口
    	$serveruri = addslashes($this->request->param('serveruri')); // Server URI

        $maskedAccessKey = $this->isMaskedValue($ossaccesskey);
        $maskedAccessSecret = $this->isMaskedValue($ossaccesssecret);
        if ($maskedAccessKey || $maskedAccessSecret)
        {
            $existing = UploadSetModel::where(['upserver'=>$upserver])
                ->where('attr','in',['AccessKeyID','AccessKeySecret'])
                ->select()
                ->toArray();
            $existingAccessKey = '';
            $existingAccessSecret = '';
            if ($existing)
            {
                foreach ($existing as $row)
                {
                    if ($row['attr'] === 'AccessKeyID')
                    {
                        $existingAccessKey = $row['attrvalue'];
                    }
                    elseif ($row['attr'] === 'AccessKeySecret')
                    {
                        $existingAccessSecret = $row['attrvalue'];
                    }
                }
            }
            if ($maskedAccessKey)
            {
                $ossaccesskey = $existingAccessKey !== '' ? $existingAccessKey : '';
            }
            if ($maskedAccessSecret)
            {
                $ossaccesssecret = $existingAccessSecret !== '' ? $existingAccessSecret : '';
            }
        }
        
        $admin_name = $this->user_list['name'];

        Db::startTrans();

        if($ossbucket == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写存储空间名称';
            $this->Log($Log_content);
            $message = Lang('image.0');
            return output(109,$message);
        }
        if($ossendpoint == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写Endpoint';
            $this->Log($Log_content);
            $message = Lang('image.1');
            return output(109,$message);
        }

        if($isOpenDiyDomain == '1' && $myEndpoint == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写MyEndpoint';
            $this->Log($Log_content);
            $message = Lang('image.2');
            return output(109,$message);
        }
        if($ossaccesskey == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写Access Key ID';
            $this->Log($Log_content);
            $message = Lang('image.3');
            return output(109,$message);
        }
        if($ossaccesssecret == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请填写Access Key Secret';
            $this->Log($Log_content);
            $message = Lang('image.4');
            return output(109,$message);
        }

        $r0 = Db::name('config')->where(['store_id'=>$store_id])->save(['upserver'=>$upserver]);
        if($r0 == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改系统图片状态失败！sql:'.$sql0;
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('operation failed');
            return output(109,$message);
        }

        $r1 = Db::table('lkt_upload_set')->where(['upserver'=>$upserver])->delete();
        if($r1 < 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除图片设置失败！sql:'.$sql1;
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('operation failed');
            return output(109,$message);
        }
        //阿里云
        if ($upserver == '2') 
        {
            $data = array(
                array('upserver'=>$upserver,'type'=>'阿里云OSS','attr'=>'Bucket','attrvalue'=>$ossbucket),
                array('upserver'=>$upserver,'type'=>'阿里云OSS','attr'=>'Endpoint','attrvalue'=>$ossendpoint),
                array('upserver'=>$upserver,'type'=>'阿里云OSS','attr'=>'isopenzdy','attrvalue'=>$isOpenDiyDomain),
                array('upserver'=>$upserver,'type'=>'阿里云OSS','attr'=>'AccessKeyID','attrvalue'=>$ossaccesskey),
                array('upserver'=>$upserver,'type'=>'阿里云OSS','attr'=>'AccessKeySecret','attrvalue'=>$ossaccesssecret),
                array('upserver'=>$upserver,'type'=>'阿里云OSS','attr'=>'MyEndpoint','attrvalue'=>$myEndpoint),
                array('upserver'=>$upserver,'type'=>'阿里云OSS','attr'=>'imagestyle','attrvalue'=>$ossimgstyleapi)
            );
        }
        //MinIO
        if ($upserver == '5') 
        {
            $data = array(
                array('upserver'=>$upserver,'type'=>'MinIO','attr'=>'Bucket','attrvalue'=>$ossbucket),
                array('upserver'=>$upserver,'type'=>'MinIO','attr'=>'Endpoint','attrvalue'=>$ossendpoint),
                // array('upserver'=>$upserver,'type'=>'MinIO','attr'=>'isopenzdy','attrvalue'=>$isOpenDiyDomain),
                array('upserver'=>$upserver,'type'=>'MinIO','attr'=>'AccessKeyID','attrvalue'=>$ossaccesskey),
                array('upserver'=>$upserver,'type'=>'MinIO','attr'=>'AccessKeySecret','attrvalue'=>$ossaccesssecret),
                // array('upserver'=>$upserver,'type'=>'MinIO','attr'=>'MyEndpoint','attrvalue'=>$myEndpoint),
                // array('upserver'=>$upserver,'type'=>'MinIO','attr'=>'imagestyle','attrvalue'=>$ossimgstyleapi),
                array('upserver'=>$upserver,'type'=>'MinIO','attr'=>'serveruri','attrvalue'=>$serveruri)
            );
        }
      
        $res = Db::name('upload_set')->insertAll($data);
        if ($res > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改图片管理成功';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改图片管理失败！参数:'. json_encode($data);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('operation failed');
            return output(109,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("saas/image.log",$Log_content);
        return;
    }
}
