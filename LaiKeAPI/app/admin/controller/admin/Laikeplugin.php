<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use app\common\Tools;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Plugin\PluginUtils;

use app\admin\model\PluginsModel;
use app\admin\model\CoreMenuModel;

/**
 * 功能：后台插件管理类
 * 修改人：PJY
 */
class Laikeplugin extends BaseController
{   
    //插件列表
    public function index()
    {
        $admin_name = $this->user_list['name'];
        $pagesize = $this->request->post('pageSize');
        $pagesize = $pagesize ? $pagesize : '10';
        // 每页显示多少条数据
        $page = $this->request->post('pageNo');

        // 页码
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        $total = 0;
        // 查询插件表
        $sql_num = "  select count(id) as num,plugin_code  from lkt_plugins where status = 1 and flag =0  GROUP BY plugin_code ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        if($total > 0)
        {
            $sql = "select tt.* from (select MAX(id) over (PARTITION BY plugin_code) AS id,MAX(optime) over (PARTITION BY plugin_code) AS optime,plugin_img,plugin_name,opuser,row_number () over (partition by plugin_code) as top,plugin_code from lkt_plugins where status = 1 and flag =0) as tt where tt.top<2 ORDER BY tt.id desc limit $start,$pagesize  ";
            $res = Db::query($sql);
            if($res)
            {
                $list = $res;
            }
        }
        $message = Lang("Success");
        return output(200,$message,array('total'=>$total,'list'=>$list));
    }

    /**
     * 卸载插件
     */
    public function delInstallLog()
    {
        $lktlog = new LaiKeLogUtils();
        // 1.开启事务
        Db::startTrans();

        // 接收信息
        $plugin_code = $this->request->post('plugin_code'); // 插件code
        if(empty($plugin_code))
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW,$message);
        }

        $r = PluginsModel::where('plugin_code',$plugin_code)->order('id','desc')->select()->toArray();
        if (empty($r))
        {   
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW,$message);
        }

        $image = $r[0]['plugin_img'];
        $pluginname = $r[0]['plugin_name'];
        $admin_name = $this->user_list['name']; // 管理员账号
        $sql =  " update lkt_plugins set flag = 1 where plugin_code = '$plugin_code' ";
        $r = Db::execute($sql);

        $sql = new PluginsModel();
        $sql->plugin_name = $pluginname;
        $sql->plugin_code = $plugin_code;
        $sql->plugin_img = $image;
        $sql->optime = date('Y-m-d H:i:s');
        $sql->status = 3;
        $sql->opuser = $admin_name;
        $sql->flag = 1;
        $sql->save();
        $res = $sql->id;
        if ($r && $res >0)
        {
            $data = new stdClass();
            $data->plugin_name = $plugin_code ;//插件名同包名
            $data->plugin_class_name = $plugin_code;
            PluginUtils::uninstall($data);

            $sql_menu = "update lkt_core_menu set recycle = 1 where module = '$plugin_code' ";
            $r_menu = Db::execute($sql_menu);
            
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            Db::rollback();
            $message = Lang('operation failed');
            return output(ERROR_CODE_CZSB,$message);
        }
    }

    /**
     * 检查是否已经安装了此插件
     */
    public function checkPlugin()
    {
        $plugin_code = $this->request->post('plugin_code'); // 插件名
        $ret = PluginsModel::where(['plugin_code'=>$plugin_code,'flag'=>0])->select()->toArray();
        if($ret)
        {   
            $message = Lang('laikeplugin.0');
            return output(501,$message);
        }
        else
        {
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    /**
     * 安装插件
     */
    public function pluginInstall()
    {
        $log  = "common/webinstall.log";
        $log = new LaiKeLogUtils();
        $log->log($log,'界面安装插件开始！');
        //获取表单提交的压缩文件
        $file = $_FILES['pluginfile'];
        $name = $file['name'];
        $plugin_code = explode(".", $name)[0];
        $plugin_name = $plugin_code;
        //定义文件保存路径 路径不能错
        $filepath= MO_PUBLIC_DIR.'/upload/'.$name;
        move_uploaded_file($file['tmp_name'],$filepath);
        $log->log($log,'插件保存位置:'.$filepath);
        $data = new stdClass();
        $data->plugin_name = $plugin_name;
        $ret = PluginUtils::install($data);
        $admin_name = $this->user_list['name']; // 管理员账号
        // 插件名
        $pluginname = $this->request->post('pluginname');
        if(empty($pluginname))
        {
            $res = PluginsModel::where('plugin_code',$plugin_code)
                               ->where('plugin_name','<>','')
                               ->field('plugin_name')
                               ->order('id','desc')
                               ->limit(0,1)
                               ->select()
                               ->toArray();
            if($res)
            {
                $pluginname = $res[0]['plugin_name'];
            }
        }
        else
        {
            $res = PluginsModel::where(['plugin_code'=>$plugin_code,'status'=>1,'flag'=>0])->field('id')->select()->toArray();
            if($res)
            {
                $log->log($log,'插件已经安装。');
                $message = Lang('laikeplugin.1');
                return output(ERROR_CODE_CZSB,$message);
            }
        }

        // 图片
        $image = $this->request->post('image');
        if(empty($image))
        {
            $res = PluginsModel::where('plugin_code',$plugin_code)
                               ->where('plugin_name','<>','')
                               ->field('plugin_img')
                               ->order('id','desc')
                               ->limit(0,1)
                               ->select()
                               ->toArray();
            if($res)
            {
                $image = $res[0]['plugin_img'];
            }
        }

        if(isset($ret['code']) && $ret['code'] == '502')
        {   
            $message = Lang('laikeplugin.2');
            return output(ERROR_CODE_CZSB,$message);
        }

        $log->log($log,'插件安装信息：'.json_encode($ret));

        if(isset($ret['code']) && $ret['code'] == 200)
        {
            $sql = new PluginsModel();
            $sql->plugin_name = $pluginname;
            $sql->plugin_code = $plugin_code;
            $sql->plugin_img = $image;
            $sql->optime = date('Y-m-d H:i:s');
            $sql->status = 1;
            $sql->opuser = $admin_name;
            $sql->save();
            $res = $sql->id;
            if($res>0)
            {
                $sql_menu = "update lkt_core_menu set recycle = 0 where module = '$plugin_code' ";
                $r_menu = Db::execute($sql_menu);
                $log->log($log,'插件安装成功。');
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $sql = new PluginsModel();
            $sql->plugin_name = $pluginname;
            $sql->plugin_code = $plugin_code;
            $sql->plugin_img = $image;
            $sql->optime = date('Y-m-d H:i:s');
            $sql->status = 2;
            $sql->opuser = $admin_name;
            $sql->save();
            $res = $sql->id;
            if($res>0)
            {
                
                $log->log($log,'插件安装失败。');
                $message = Lang('laikeplugin.3');
                return output(ERROR_CODE_CZSB,$message);
            }
            else
            {
                $log->log($log,'插件安装失败信息保存失败。');
                $message = Lang('laikeplugin.2');
                return output(ERROR_CODE_CZSB,$message);
            }
        }
    }

    /**
     * 安装记录
     */
    public function record()
    {
        $admin_name = $this->user_list['name']; // 管理员账号

        $plugin_name = addslashes(trim($this->request->post('plugin_name'))); //插件
        $status = addslashes(trim($this->request->post('status'))); //

        $pagesize = $this->request->post('pageSize');
        $pagesize = $pagesize ? $pagesize : '10';


        // 每页显示多少条数据
        $page = $this->request->post('pageNo');

        // 页码
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        else
        {
            $start = 0;
        }

        $condition = " 1=1  ";
        if ($plugin_name)
        {
            $condition .= " and plugin_name like '%$plugin_name%' ";
        }
        if ($status)
        {

            $condition .= " and status = '$status' ";
        }
        // 查询插件表
        $sql = "  select count(id) as num from lkt_plugins where $condition ";
        $r_pager = Db::query($sql);
        $total = empty($r_pager) ? 0 : $r_pager[0]['num'] ;

        $sql =  " select * from lkt_plugins where $condition order by optime desc limit $start,$pagesize  " ;
        $list = Db::query($sql);

        $message = Lang('Success');
        return output(200,$message,array('total'=>$total,'list'=>$list));
    }

    
}