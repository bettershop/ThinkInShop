<?php
namespace app\admin\controller\plugin\template;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Plugin\Plugin;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\PC_Tools;

use app\admin\model\DiyModel;
use app\admin\model\ConfigModel;
use app\admin\model\BannerModel;
use app\admin\model\JumpPathModel;
use app\admin\model\ProductClassModel;
use app\admin\model\ProductListModel;
use app\admin\model\MchModel;
use app\admin\model\UiNavigationBarModel;
use app\admin\model\ActivityModel;
use app\admin\model\AdminModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ActivityProModel;

/**
 * 功能：模板管理
 * 修改人：DHB
 */
class MchAdminDiy extends BaseController
{   
    // 获取H5域名
    public function Index()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));
        $lang_code = trim($this->request->param('language'));

        $mch_id = cache($access_id.'_'.$store_type);
        $H5_domain = '';

        $r_config = ConfigModel::where(['store_id'=>$store_id])->field('H5_domain')->select()->toArray();
        if($r_config)
        {
            $H5_domain = $r_config[0]['H5_domain'] . "pagesB/store/store?shop_id=" . $mch_id;
        }

        $data = array('H5_domain' => $H5_domain);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取diy数据
    public function GetDiyList()
    {
        $store_id = $this->request->param('storeId');
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));
        $lang_code = trim($this->request->param('language'));
        $id = $this->request->param('id');
        $mch_id = cache($access_id.'_'.$store_type);

        $list = array();
        $total = 0;
        $where_list = array('store_id'=>$store_id,'is_del'=>0,'mch_id'=>$mch_id,'lang_code'=>$lang_code);
        if($id != '' && $id != 0)
        {
            $where_list['id'] = $id;
        }


        $r0 = DiyModel::where($where_list)->field('count(id) as total')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $r1 = DiyModel::where($where_list)->select()->toArray();
        if($r1)
        {
            $list = $r1;
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang("Success");
        return output("200",$message,$data);
    }

    // 获取路径
    public function BannerPathList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));
        $lang_code = trim($this->request->param('language'));
        
        $type0 = trim($this->request->param('type'));
        $mch_id = cache($access_id.'_'.$store_type);

        // $where_list = array('store_id'=>$store_id,'type0'=>$type0,'status'=>1,'type'=>1,'lang_code'=>$lang_code,'mch_id'=>$mch_id);
        $where_list = array('store_id'=>$store_id,'type0'=>$type0,'status'=>1,'type'=>1,'lang_code'=>$lang_code);
        $list = array();
        $total = 0;

        $r0 = JumpPathModel::where($where_list)->select()->toArray();
        if($r0)
        {
            foreach($r0 as $k => $v)
            {
                $parameter = explode('=',$v['parameter'])[1];
                if($v['type0'] == 1)
                {
                    $r1 = ProductClassModel::where(['cid'=>$parameter,'recycle'=>0])->field('cid')->select()->toArray();
                }
                else if($v['type0'] == 2)
                {
                    $r1 = ProductListModel::where(['id'=>$parameter,'recycle'=>0,'mch_status'=>2,'status'=>2,'mch_id'=>$mch_id])->field('id')->select()->toArray();
                }
                else if($v['type0'] == 3)
                {
                    $r1 = MchModel::where(['id'=>$parameter,'recovery'=>0,'review_status'=>1])->field('id')->select()->toArray();
                }
                if($r1)
                {
                    $list[] = $v;
                }
            }
        }

        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取页面数据
    public function getBindPageList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id'));

        $list = array();
        $sql = "select a.bind_id,a.id,a.link_key,a.page_name,a.link,a.page_key,a.image,a.page_context,a.status,a.recycle,a.create_time,a.create_by,a.update_time from (select b.id AS bind_id,b.link_key,p.*,ROW_NUMBER() over ( PARTITION BY b.diy_page_id ) AS top from lkt_diy_page_bind b left join lkt_diy_page p on p.id = b.diy_page_id where b.diy_id = '$id') AS a WHERE a.top = 1";
        $r = Db::query($sql);
        if($r)
        {
            $list = $r;
        }

        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 添加/编辑diy数据
    public function AddOrUpdateDiy()
    {
        $store_id = $this->request->param('storeId');
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));
        $lang_code = trim($this->request->param('language'));

        $id = $this->request->param('id');
        $name = $this->request->param('name'); // 页面名称
        $cover = $this->request->param('cover'); // 封面图
        $value = $this->request->param('value'); // 页面数据

        $mch_id = cache($access_id.'_'.$store_type);

        if($id != '' && $id != 0)
        {
            $r0 = DiyModel::where(['store_id'=>$store_id,'name'=>$name,'mch_id'=>$mch_id])->where('id','<>',$id)->field('id')->select()->toArray();
        }
        else
        {
            $r0 = DiyModel::where(['store_id'=>$store_id,'name'=>$name,'mch_id'=>$mch_id])->field('id')->select()->toArray();
        }
        if($r0)
        {
            $message = Lang('template.14');
            return output(109,$message);
        }

        $current_time = time();
        $update_time = time();
        $time = date("Y-m-d H:i:s");

        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('name'=>$name,'cover'=>$cover,'value'=>$value,'update_time'=>$update_time);
            $r = Db::name('diy')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改diy配置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $lktlog->customerLog($Log_content);
                $message = Lang('template.15');
                return output(109,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改diy配置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $sql_inset = array('version'=>'1.0','name'=>$name,'value'=>$value,'add_time'=>$current_time,'update_time'=>$update_time,'status'=>0,'type'=>1,'is_del'=>0,'store_id'=>$store_id,'mch_id'=>$mch_id,'cover'=>$cover,'lang_code'=>$lang_code);
            $res = Db::name('diy')->insertGetId($sql_inset);
            if ($res < 1) 
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 新增diy配置失败！参数：'.json_encode($sql_inset);
                $this->Log($Log_content);
                $message = Lang('template.16');
                return output(109,$message);
            } 
            else 
            {
                $data = array('id'=>$res);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 新增diy配置成功！';
                $this->Log($Log_content);
                $message = Lang("Success");
                return output("200",$message,$data);
            }
        }
    }

    // 应用diy
    public function DiyStatus()
    {
        $store_id = $this->request->param('storeId');
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));
        $lang_code = trim($this->request->param('language'));
        $id = $this->request->param('id');

        $mch_id = cache($access_id.'_'.$store_type);

        $sql_where0 = array('store_id'=>$store_id,'mch_id'=>$mch_id);
        $sql_update0 = array('status'=>0);
        $r0 = Db::name('diy')->where($sql_where0)->update($sql_update0);

        $sql_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
        $sql_update = array('status'=>1);
        $r = Db::name('diy')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改diy配置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
            $lktlog->customerLog($Log_content);
            $message = Lang('template.15');
            return output(109,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改diy配置成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 删除diy数据
    public function DelDiy()
    {
        $store_id = $this->request->param('storeId');
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));
        $lang_code = trim($this->request->param('language'));
        $id = $this->request->param('id');

        $mch_id = cache($access_id.'_'.$store_type);

        $sql_where = array('store_id'=>$store_id,'id'=>$id,'mch_id'=>$mch_id);
        $sql_update = array('is_del'=>1);
        $r = Db::name('diy')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改diy配置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
            $lktlog->customerLog($Log_content);
            $message = Lang('template.15');
            return output(109,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改diy配置成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("template/AdminDiy.log",$Log_content);
        return;
    }
}
