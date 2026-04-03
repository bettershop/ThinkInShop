<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\LKTConfigInfo;
use app\common\OSSCommon;
use app\common\MinIOServer;
use app\common\PC_Tools;

use app\admin\model\BannerModel;
use app\admin\model\PaymentConfigModel;
use app\admin\model\ImgGroupModel;
use app\admin\model\AdminModel;

/**
 * 功能：支付管理
 * 修改人：DHB
 */
class Payment extends BaseController
{   
    // 支付管理
    public function index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));

        $page = addslashes($this->request->param('pageNo')); // 页码
    	$pagesize = addslashes($this->request->param('pageSize')); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';// 每页显示多少条数据
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        
        $total = 0;
        $list = array();

        $sql0 = "select count(p.id) as num from lkt_payment p left join lkt_payment_config c on p.id = c.pid where store_id = '$store_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        // 查询轮播图表，根据sort顺序排列
        $sql1 = "select p.logo,p.name,p.id,p.class_name,p.description,p.status as switch,c.status,c.isdefaultpay from lkt_payment p left join lkt_payment_config c on p.id = c.pid where store_id = '$store_id' order by p.sort desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $list[] = $v;
            }
        }
       
        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 参数修改页面
    public function paymentParmaInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('id')); // 支付方式ID

        $config = array();
        $mrnotify_url = '';
        $r = PaymentConfigModel::where(['store_id'=> $store_id,'pid'=>$id])->field('config_data')->select()->toArray();
        if($r)
        {
            $config = json_decode($r[0]['config_data'],true);
        }

        if($config)
        {
            foreach ($config as $key => $value)
            {
                if (!is_string($value))
                {
                    continue;
                }
                $lower_key = strtolower($key);
                if (strpos($lower_key, 'key') !== false || strpos($lower_key, 'secret') !== false || strpos($lower_key, 'cert') !== false || strpos($lower_key, 'pem') !== false)
                {
                    $config[$key] = $this->maskValue($value, 4, 4);
                }
            }
            if(isset($config['cert_pem']) && $config['cert_pem'] !== '')
            {
                $config['cert_pem'] = $this->maskValue($config['cert_pem'], 6, 6);
            }
            if(isset($config['key_pem']) && $config['key_pem'] !== '')
            {
                $config['key_pem'] = $this->maskValue($config['key_pem'], 6, 6);
            }
        }

        $data = array('config'=>$config,'mrnotify_url'=>$mrnotify_url);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 保存
    public function setPaymentParma()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 支付方式ID
        $json = trim($this->request->param('json')); // 参数
        $status = addslashes(trim($this->request->param('status'))); // 是否显示 0否 1是

        $data = json_decode($json,true);
        if(is_array($data))
        {
            $masked_keys = array();
            foreach ($data as $key => $value)
            {
                if ($this->isMaskedValue($value))
                {
                    $masked_keys[] = $key;
                }
            }
            if ($masked_keys)
            {
                $existing_config = PaymentConfigModel::where(['store_id'=> $store_id,'pid'=>$id])->value('config_data');
                $existing_data = json_decode($existing_config, true);
                if (is_array($existing_data))
                {
                    foreach ($masked_keys as $masked_key)
                    {
                        if (array_key_exists($masked_key, $existing_data))
                        {
                            $data[$masked_key] = $existing_data[$masked_key];
                        }
                    }
                    $json = json_encode($data);
                }
            }
        }

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        if($id==4 || $id==5 || $id==6 || $id==10 || $id==12)
        {
            if(!is_array($data))
            {
                $data = array();
            }
            $cert_pem = $data['cert_pem'] ?? '';
            $key_pem = $data['key_pem'] ?? '';

            $masked_cert = $this->isMaskedValue($cert_pem);
            $masked_key = $this->isMaskedValue($key_pem);
            if ($masked_cert || $masked_key)
            {
                $existing = PaymentConfigModel::where(['store_id'=> $store_id,'pid'=>$id])->field('config_data')->select()->toArray();
                $existing_data = array();
                if ($existing && $existing[0]['config_data'])
                {
                    $existing_data = json_decode($existing[0]['config_data'], true);
                }
                if ($masked_cert)
                {
                    $data['cert_pem'] = $existing_data['cert_pem'] ?? '';
                }
                if ($masked_key)
                {
                    $data['key_pem'] = $existing_data['key_pem'] ?? '';
                }
                $json = json_encode($data);
                $cert_pem = $data['cert_pem'] ?? '';
                $key_pem = $data['key_pem'] ?? '';
            }

            $wx_config_path = MO_LIB_DIR . '/cert/';
            $cert_path = $wx_config_path . 'wx/'.md5($id.$store_id);
            $data['sslkey_path'] = $sslkey_path = $cert_path.'/apiclient_key.pem';
            $data['sslcert_path'] = $sslcert_path = $cert_path.'/apiclient_cert.pem';
            $json = json_encode($data);
            if(!is_dir($cert_path))
            {
                mkdir($cert_path);
            }
            if (is_file($sslcert_path))
            {   
                fopen($sslcert_path, "w");
            }
            if (!empty($cert_pem))
            {   
                @file_put_contents($sslcert_path, urldecode($cert_pem));
            }

            if (is_file($sslkey_path))
            {
                fopen($sslkey_path, "w");
            }
            if (!empty($key_pem))
            {
                @file_put_contents($sslkey_path, urldecode($key_pem));
            }
        }

        $sql_0 = "select config_data from lkt_payment_config where store_id = '$store_id' and pid = '$id' ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            if($r_0[0]['config_data'] == '')
            {
                $status = 1;
            }
        }

        $name = '';
        $sql0 = "select name from lkt_payment where id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $name = $r0[0]['name'];
        }

        $sql_update = array('status'=>$status,'config_data'=>$json);
        $sql_where = array('store_id'=>$store_id,'pid'=>$id);
        $r = Db::name('payment_config')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了'.$name.' 的参数失败',2,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":修改支付参数失败：条件参数：".json_encode($sql_where)."；修改参数：".json_encode($sql_where));
            $message = Lang('payment.0');
            return output(109,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了'.$name.' 的参数',2,1,0,$operator_id);
            $this->Log(__LINE__ . $operator . ":修改支付参数成功！");
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 钱包支付保存
    public function setPayment()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 支付方式ID
        $name = trim($this->request->param('name')); // 参数
        $remark  = addslashes(trim($this->request->param('remark'))); // 是否显示 0否 1是
        $Logo  = addslashes(trim($this->request->param('Logo'))); // 是否显示 0否 1是

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        
        $sql_update = array('description'=>$remark,'logo'=>$Logo);
        $sql_where = array('id'=>$id);
        $r = Db::name('payment')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了'.$name.' 的支付信息失败',2,1,0,$operator_id);
            $this->Log(__LINE__ ."修改支付参数失败：条件参数：".json_encode($sql_where)."；修改参数：".json_encode($sql_where));
            $message = Lang('payment.0');
            return output(109,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了'.$name.' 的支付信息',2,1,0,$operator_id);
            $this->Log(__LINE__ . "修改支付参数成功！");
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 是否开启
    public function setPaymentSwitch()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 支付方式ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $name = '';
        $sql0 = "select name from lkt_payment where id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $name = $r0[0]['name'];
        }

        $r0 = PaymentConfigModel::where(['store_id'=> $store_id,'pid'=>$id])->field('status,config_data')->select()->toArray();
        if($r0)
        {
            $status = $r0[0]['status'];
            $config_data = json_decode($r0[0]['config_data'],true);
            if($status == 1)
            {
                $config_data['status'] = 0;
                $json = json_encode($config_data);
                $sql_update = array('status'=>0,'config_data'=>$json);
            }
            else
            {
                $config_data['status'] = 1;
                $json = json_encode($config_data);
                $sql_update = array('status'=>1,'config_data'=>$json);
            }

            $sql_where = array('store_id'=>$store_id,'pid'=>$id);
            $r = Db::name('payment_config')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了'.$name.' 的支付参数失败',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":修改支付参数失败：条件参数：".json_encode($sql_where)."；修改参数：".json_encode($sql_where));
                $message = Lang('payment.0');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了'.$name.' 的支付参数成功',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":修改支付参数成功！");
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(200,$message);
        }
    }

    // 是否默认
    public function settingDefaultPaytype()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 支付方式ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $name = '';
        $sql0 = "select name from lkt_payment where id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $name = $r0[0]['name'];
        }

        $r0 = PaymentConfigModel::where(['store_id'=> $store_id,'pid'=>$id])->field('status,config_data')->select()->toArray();
        if($r0)
        {
            $sql_update1 = array('isdefaultpay'=>2);
            $sql_where1 = array('store_id'=>$store_id);
            $r1 = Db::name('payment_config')->where($sql_where1)->update($sql_update1);

            $sql_update = array('isdefaultpay'=>1);
            $sql_where = array('store_id'=>$store_id,'pid'=>$id);
            $r = Db::name('payment_config')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了'.$name.' 的默认支付方式失败',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":修改默认支付方式失败：条件参数：".json_encode($sql_where)."；修改参数：".json_encode($sql_where));
                $message = Lang('payment.0');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了'.$name.' 的默认支付方式成功',2,1,0,$operator_id);
                $this->Log(__LINE__ . $operator . ":修改默认支付方式成功！");
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(200,$message);
        }
    }

    // 上传图片
    public function setPaymentLoge()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $imgUrls = array();
        $dir = $store_id . '/' . $store_type . '/' . date("Ymd") . '/';
        $uploadImg = "../public/images/";

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

            $common = LKTConfigInfo::getOSSConfig();
            try
            {
                //查询文件上传配置方式
                $upserver = Db::name('config')->where('store_id', 1)->value('upserver');
                // 阿里云
                if($upserver == '2')
                {
                    $ossClient = OSSCommon::getOSSClient();
                    $ossClient->uploadFile($common['bucket'], $path, $imgURL);
                }
                // MinIO
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

            $fsql = " INSERT INTO `lkt_files_record` ( `store_id`, `store_type`, `group`, `upload_mode`, `image_name`,`mch_id`,`supplier_id`,`type`,`name`) VALUES ('$store_id', '$store_type', '-1', $upserver, '$imgURL_name','0','0','$type_0','$files') ";
            $res = Db::execute($fsql);

            $data = $url;
            $message = Lang('Success');
            return output("200",$message,$data);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/payment.log",$Log_content);
        return;
    }
}
