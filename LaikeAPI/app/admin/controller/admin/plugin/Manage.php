<?php
namespace app\admin\controller\admin\plugin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use PhpOffice\PhpSpreadsheet\IOFactory;

use app\admin\model\PluginsModel;
use app\admin\model\GroupOrderConfigModel;

/**
 * 功能：插件管理
 * 修改人：LF
 */
class Manage extends BaseController
{   
    // 插件配置
    public function index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$storeType = addslashes(trim($this->request->param('storeType')));
    	$accessId = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 插件ID

        $page = addslashes($this->request->param('pageNo')); // 页码
    	$pagesize = addslashes($this->request->param('pageSize')); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';// 每页显示多少条数据
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $condition = " store_id = '$store_id' and flag = 0 ";
        if ($id)
        {
            $condition .= " and id = '$id' ";
        }
        
        $total = 0;
        $list = array();

        $sql0 = "select count(id) as num from lkt_plugins where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        // 查询插件配置表
        $sql1 = "select * from lkt_plugins where $condition order by plugin_sort desc,id asc ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $r1[$k]['isMchPlugin'] = $v['is_mch_plugin'];
                $r1[$k]['plugin_sort'] = (int)$v['plugin_sort'];
                $r1[$k]['pluginSort'] = (int)$v['plugin_sort'];
            }
        }
       
        $data = array('total'=>$total,'list'=>$r1);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 保存插件排序
    public function saveSort()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $storeType = addslashes(trim($this->request->param('storeType')));
        $accessId = addslashes(trim($this->request->param('accessId')));

        $pluginId = addslashes(trim($this->request->param('pluginId'))); // 插件ID
        $pluginSort = $this->request->param('pluginSort'); // 排序值

        if($pluginId === '' || $pluginSort === '' || $pluginSort === null)
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }

        $pluginSort = (int)$pluginSort;
        $Jurisdiction = new Jurisdiction();
        $operator = cache($accessId.'admin_name');

        $plugin_name = '';
        $sql0 = "select id,plugin_name from lkt_plugins where store_id = '$store_id' and id = '$pluginId' and flag = 0 ";
        $r0 = Db::query($sql0);
        if(!$r0)
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
        $plugin_name = $r0[0]['plugin_name'];

        $sql_where = array('store_id'=>$store_id,'id'=>$pluginId,'flag'=>0);
        $sql_update = array('plugin_sort'=>$pluginSort);
        $r = Db::name('plugins')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了'.$plugin_name.' 插件排序失败',2,1,0);
            $this->Log(__LINE__ . $operator . ":保存插件排序失败：条件参数：".json_encode($sql_where)."；修改参数：".json_encode($sql_update));
            $message = Lang('payment.0');
            return output(109,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了'.$plugin_name.' 插件排序为 '.$pluginSort,2,1,0);
            $this->Log(__LINE__ . $operator . ":保存插件排序成功！");
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 参数修改页面
    public function pluginParmaInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$storeType = addslashes(trim($this->request->param('storeType')));
    	$accessId = addslashes(trim($this->request->param('accessId')));

        $id = addslashes($this->request->param('id')); // 插件ID

        $data = array();
        $r = PluginsModel::where(['store_id'=> $store_id,'id'=>$id])->field('id,plugin_name,plugin_code,status,optime,content')->select()->toArray();
        if($r)
        {
            $data = $r[0];
        }

        $data = array('list'=>$data);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 编辑保存
    public function addPlugin()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$storeType = addslashes(trim($this->request->param('storeType')));
    	$accessId = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 插件ID
        $content = trim($this->request->param('content')); // 插件介绍
        $status = addslashes(trim($this->request->param('pluginSwitch'))); // 是否显示 0否 1是

        $Jurisdiction = new Jurisdiction();
        $operator = cache($accessId.'admin_name');

        $time = date("Y-m-d H:i:s");
        $sql_update = array('content'=>$content,'optime'=>$time);
        if (isset($status)) 
        {
            $sql_update['status'] = $status;
        }

        $plugin_name = '';
        $sql0 = "select plugin_name,status from lkt_plugins where store_id = '$store_id' and id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $plugin_name = $r0[0]['plugin_name'];
			$sql_update['status'] = $r0[0]['status'];
        }

        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $r = Db::name('plugins')->where($sql_where)->update($sql_update);
        if ($r == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了'.$plugin_name.' 插件的信息失败',2,1,0);
            $this->Log(__LINE__ . $operator . ":修改插件参数失败：条件参数：".json_encode($sql_where)."；修改参数：".json_encode($sql_update));
            $message = Lang('payment.0');
            return output(109,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了'.$plugin_name.' 插件的信息',2,1,0);
            $this->Log(__LINE__ . $operator . ":修改插件参数成功！");
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 是否开启
    public function pluginSwitch()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $storeType = addslashes(trim($this->request->param('storeType')));
        $accessId = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 插件ID

        $Jurisdiction = new Jurisdiction();
        $operator = cache($accessId.'admin_name');

        $time = date("Y-m-d H:i:s");
        $r0 = PluginsModel::where(['store_id'=> $store_id,'id'=>$id])->field('status,optime,plugin_code,plugin_name')->select()->toArray();
        if($r0)
        {
            $status = $r0[0]['status'];
            $plugin_code = $r0[0]['plugin_code'];
            $plugin_name = $r0[0]['plugin_name'];

            if($status == 1)
            {
                $status = 0;
            }
            else
            {
                $status = 1;
            }

            if($plugin_code == 'coupon')
            {
                $sql_coupon_config_where = array('store_id'=>$store_id);
                $sql_coupon_config_update = array('is_show'=>$status);
                Db::name('coupon_config')->where($sql_coupon_config_where)->update($sql_coupon_config_update);
            }

            if($plugin_code == 'distribution')
            {
                $sql_distribution_config_where = array('store_id'=>$store_id);
                $sql_distribution_config_update = array('status'=>$status);
                Db::name('distribution_config')->where($sql_distribution_config_where)->update($sql_distribution_config_update);
            }

            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('status'=>$status,'optime'=>$time);
            $r = Db::name('plugins')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '将插件名称：'.$plugin_name.' 进行了授权店铺操作失败',2,1,0);
                $this->Log(__LINE__ . $operator . ":修改插件参数失败：条件参数：".json_encode($sql_where)."；修改参数：".json_encode($sql_update));
                $message = Lang('payment.0');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '将插件名称：'.$plugin_name.' 进行了授权店铺操作',2,1,0);
                $this->Log(__LINE__ . $operator . ":修改插件参数成功！");
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

    // 获取拼团设置信息
    public function getOrderConfig()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $token = addslashes(trim($this->request->param('accessId')));

        $pluginCode = addslashes(trim($this->request->param('pluginCode')));

        $isOpen = 0;
        $autoCommentContent = '';
        $autoTeGood = 7;
        $commentDay = 7;
        $content = '';
        $orderAfter = 0;

        $r_config = GroupOrderConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if ($r_config)
        {
            $autoCommentContent = $r_config[0]['auto_good_comment_content']; // 自动好评内容
            $autoTeGood = (int)$r_config[0]['auto_the_goods']/3600/24; // 自动收货时间
            $commentDay = (int)$r_config[0]['auto_good_comment_day']/3600/24; // 自动评价设置几后自动好评
            $content = $r_config[0]['content']; // 拼团规则
            $orderAfter = (int)$r_config[0]['order_after']; // 订单售后时间
            $isOpen = (int)$r_config[0]['is_open']; // 订单售后时间
        }

        $data = array('autoCommentContent'=>$autoCommentContent,'autoTeGood'=>$autoTeGood,'commentDay'=>$commentDay,'content'=>$content,'orderAfter'=>$orderAfter,'isOpen'=>$isOpen);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 拼团设置
    public function addPluginConfig()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $accessId = addslashes(trim($this->request->param('accessId')));

        $is_open = $this->request->param('isOpen'); // 自动收货时间
        $auto_the_goods = $this->request->param('autoTheGoods') * 60 * 60 * 24; // 自动收货时间
        $order_after = $this->request->param('orderAfter') * 60 * 60 * 24; // 订单售后时间 (单位秒)
        $auto_good_comment_day = $this->request->param('autoGoodCommentDay') * 60 * 60 * 24; // 自动评价设置几后自动好评
        $content = $this->request->param('content'); // 拼团规则
        $autoCommentContent = $this->request->param('autoCommentContent'); // 自动好评内容

        $Jurisdiction = new Jurisdiction();
        $operator = cache($accessId.'admin_name');
        
        $time = date("Y-m-d H:i:s");
        $r_config = GroupOrderConfigModel::where(['store_id'=>$store_id])->select()->toArray();
        if($r_config)
        { // 修改
            $sql_where = array('store_id'=>$store_id);
            $sql_update = array('order_after'=>$order_after,'auto_the_goods'=>$auto_the_goods,'auto_good_comment_day'=>$auto_good_comment_day,'auto_good_comment_content'=>$autoCommentContent,'content'=>$content,'update_date'=>$time,'is_open'=>$is_open);
            $r = Db::name('group_order_config')->where($sql_where)->update($sql_update);
            if($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了拼团插件的配置信息失败',2,1,0);
                $Log_content = __METHOD__ . '->' . __LINE__ . '修改拼团设置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('修改失败！');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了拼团插件的配置信息',2,1,0);
                $Log_content = __METHOD__ . '->' . __LINE__ .  '修改拼团设置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        { // 添加
            $sql_insert = array('store_id'=>$store_id,'order_after'=>$order_after,'auto_the_goods'=>$auto_the_goods,'auto_good_comment_day'=>$auto_good_comment_day,'auto_good_comment_content'=>$autoCommentContent,'content'=>$content,'create_date'=>$time,'is_open'=>$is_open);
            $r = Db::name('group_order_config')->insert($sql_insert);
            if($r > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了拼团插件的配置信息',1,1,0);
                $Log_content = __METHOD__ . '->' . __LINE__ .  '添加拼团设置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了拼团插件的配置信息失败',1,1,0);
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加拼团设置失败！参数：' . json_encode($sql_insert);
                $this->Log($Log_content);
                $message = Lang('添加失败！');
                return output(109,$message);
            }
        }
    }
    
    // 获取直播配置信息
    public function getLivingConfig()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $token = addslashes(trim($this->request->param('accessId')));

        $id = 0;
        $is_open = 0;
        $mch_is_open = 0;
        $push_url = '';
        $play_url = '';
        $agree_title = '';
        $agree_content = '';
        $license_key = '';
        $license_url = '';

        $sql = "select id,is_open,mch_is_open,push_url,play_url,agree_title,agree_content,license_key,license_url from lkt_living_config where store_id = '$store_id' ";
        $r = Db::query($sql);
        if ($r)
        {
            $id = $r[0]['id'];
            $is_open = $r[0]['is_open'];
            $mch_is_open = $r[0]['mch_is_open'];
            $push_url = $r[0]['push_url'];
            $play_url = $r[0]['play_url'];
            $agree_title = $r[0]['agree_title'];
            $agree_content = $r[0]['agree_content'];
            $license_key = $r[0]['license_key'];
            $license_url = $r[0]['license_url'];
        }

        $data = array('id'=>$id,'is_open'=>$is_open,'mch_is_open'=>$mch_is_open,'push_url'=>$push_url,'play_url'=>$play_url,'agree_title'=>$agree_title,'agree_content'=>$agree_content,'license_key'=>$license_key,'license_url'=>$license_url);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 保存直播配置信息
    public function addLivingConfig()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $accessId = addslashes(trim($this->request->param('accessId')));

        $is_open = addslashes(trim($this->request->param('is_open')));
        $mch_is_open = addslashes(trim($this->request->param('mch_is_open')));
        $push_url = addslashes(trim($this->request->param('pushUrl'))); // 推流地址
        $play_url = addslashes(trim($this->request->param('playUrl'))); // 播放地址
        $agree_title = addslashes(trim($this->request->param('agreeTitle'))); // 协议标题
        $agree_content = addslashes(trim($this->request->param('agreeContent'))); // 协议内容
        $license_key = addslashes(trim($this->request->param('license_key'))); // 腾讯云直播播放器的key
        $license_url = addslashes(trim($this->request->param('license_url'))); // 腾讯云直播播放器的url
        
        $Jurisdiction = new Jurisdiction();
        $operator = cache($accessId.'admin_name');
        
        $time = date("Y-m-d H:i:s");
        if($push_url == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '推流地址不能为空！';
            $this->Log($Log_content);
            $message = Lang('推流地址不能为空！');
            return output(109,$message);
        }
        if($play_url == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '播放地址不能为空！';
            $this->Log($Log_content);
            $message = Lang('播放地址不能为空！');
            return output(109,$message);
        }
        if($license_key == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '腾讯云直播播放器的key不能为空！';
            $this->Log($Log_content);
            $message = Lang('腾讯云直播播放器的key不能为空！');
            return output(109,$message);
        }
        if($license_url == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '腾讯云直播播放器的url不能为空！';
            $this->Log($Log_content);
            $message = Lang('腾讯云直播播放器的url不能为空！');
            return output(109,$message);
        }
        if($agree_title == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '协议标题不能为空！';
            $this->Log($Log_content);
            $message = Lang('协议标题不能为空！');
            return output(109,$message);
        }
        if($agree_content == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '协议内容不能为空！';
            $this->Log($Log_content);
            $message = Lang('协议内容不能为空！');
            return output(109,$message);
        }

        $sql_config = "select id from lkt_living_config where store_id = '$store_id' ";
        $r_config = Db::query($sql_config);
        if ($r_config)
        {
            $sql_where = array('store_id'=>$store_id);
            $sql_update = array('is_open'=>$is_open,'mch_is_open'=>$mch_is_open,'push_url'=>$push_url,'play_url'=>$play_url,'agree_title'=>$agree_title,'agree_content'=>$agree_content,'add_time'=>$time,'license_key'=>$license_key,'license_url'=>$license_url);
            $r = Db::name('living_config')->where($sql_where)->update($sql_update);
            if($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改直播配置信息失败',2,1,0);
                $Log_content = __METHOD__ . '->' . __LINE__ . '修改直播配置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('修改失败！');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了直播配置信息',2,1,0);
                $Log_content = __METHOD__ . '->' . __LINE__ .  '修改直播配置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $sql_insert = array('store_id'=>$store_id,'is_open'=>$is_open,'mch_is_open'=>$mch_is_open,'push_url'=>$push_url,'play_url'=>$play_url,'agree_title'=>$agree_title,'agree_content'=>$agree_content,'add_time'=>$time,'recycle'=>0,'license_key'=>$license_key,'license_url'=>$license_url);
            $r = Db::name('living_config')->insert($sql_insert);
            if($r > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加直播配置信息',1,1,0);
                $Log_content = __METHOD__ . '->' . __LINE__ .  '添加直播配置信息成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加直播配置信息失败',1,1,0);
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加直播配置信息失败！参数：' . json_encode($sql_insert);
                $this->Log($Log_content);
                $message = Lang('添加失败！');
                return output(109,$message);
            }
        }

        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 敏感字设置
    public function selectSensitive()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $word = addslashes(trim($this->request->param('word'))); // 敏感词
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = trim($this->request->param('pageSize'));//每页数据
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $con = " store_id = '$store_id' and recycle = 0 ";
        if($word != '')
        {
            $con .= " and word like '%$word%' ";
        }
        $total = 0;
        $list = array();

        $sql0 = "select count(id) as total from lkt_living_sensitive where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql0 = "select id,word,add_time from lkt_living_sensitive where $con order by add_time desc limit $start,$pagesize ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $list = $r0;
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 添加敏感字
    public function addSensitive()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 敏感词ID
        $word = addslashes(trim($this->request->param('word'))); // 敏感词

        $time = date("Y-m-d H:i:s");
        if($word == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '敏感词不能为空！';
            $this->Log($Log_content);
            $message = Lang('system.45');
            return output(109,$message);
        }

        if($id == '')
        {
            $sql0 = "select id from lkt_living_sensitive where store_id = '$store_id' and recycle = 0 and word = '$word' ";
        }
        else
        {
            $sql0 = "select id from lkt_living_sensitive where store_id = '$store_id' and recycle = 0 and word = '$word' and id != '$id' ";
        }
        $r0 = Db::query($sql0);
        if($r0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '敏感词重复';
            $this->Log($Log_content);
            $message = Lang('system.46');
            return output(109,$message);
        }

        if($id == '')
        {
            $sql1 = "insert into lkt_living_sensitive(word,add_time,store_id,recycle) value ('$word','$time','$store_id',0)";
            $r1 = Db::execute($sql1);
            if($r1 < 1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加失败';
                $this->Log($Log_content);
                $message = Lang('system.9');
                return output(109,$message);
            }
        }
        else
        {
            $sql1 = "update lkt_living_sensitive set word = '$word' where store_id = '$store_id' and id = '$id' ";
            $r1 = Db::execute($sql1);
            if($r1 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '修改失败';
                $this->Log($Log_content);
                $message = Lang('system.8');
                return output(109,$message);
            }
        }
        
        $message = Lang("Success");
        return output(200,$message);
    }

    // 删除敏感字
    public function deleteSensitive()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $ids = addslashes(trim($this->request->param('ids'))); // 敏感词ID

        $sql1 = "update lkt_living_sensitive set recycle = 1 where store_id = '$store_id' and id in ($ids) ";
        $r1 = Db::execute($sql1);
        if($r1 == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '敏感词重复';
            $this->Log($Log_content);
            $message = Lang('system.12');
            return output(109,$message);
        }

        $message = Lang("Success");
        return output(200,$message);
    }

    // 批量上传
    public function addSensitives()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $time = date("Y-m-d H:i:s");
        $filename = $_FILES['file']['tmp_name'];
        $name = $_FILES['file']['name'];
        if (empty ($filename)) 
        {
            $message = Lang('product.91');
            return output(109, $message);
        }

        $handle = fopen($filename,'r');
        $result = $this->input_excel($filename);

        $len_result = count($result);

        if($len_result == 0)
        {
            $message = Lang('product.92');
            return output("50973", $message,null);
        }
        
        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1,'recycle'=>0])->field('shop_id')->select()->toArray();
        $mch_id = $r_admin[0]['shop_id'];
        
        Db::startTrans();
        foreach ($result as $k => $v) 
        {
            $word = addslashes(trim($v['A']));

            if($word == '')
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . '敏感词不能为空！';
                $this->Log($Log_content);
                $message = Lang('system.45');
                return output(109,$message);
            }
            
            $sql0 = "select id from lkt_living_sensitive where store_id = '$store_id' and recycle = 0 and word = '$word' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . '敏感词重复';
                $this->Log($Log_content);
                $message = Lang('system.46');
                return output(109,$message);
            }
            
            $sql1 = "insert into lkt_living_sensitive(word,add_time,store_id,recycle) value ('$word','$time','$store_id',0)";
            $r1 = Db::execute($sql1);
            if($r1 < 1)
            {
                Db::rollback();
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加失败';
                $this->Log($Log_content);
                $message = Lang('system.9');
                return output(109,$message);
            }
        }

        Db::commit();
        $message = Lang('Success');
        return output(200, $message);
    }

    public function input_excel($filename)
    {
        $out = array ();
        $n = 0;
        $reader = IOFactory::createReader('Xlsx'); // 先创建一个Reader对象
        $spreadsheet = $reader->load($filename); // 载入文件到Spreadsheet对象中

        $worksheet = $spreadsheet->getActiveSheet(); // 获取活动工作表

        $highestRow = $worksheet->getHighestRow(); // 获取最大行数
        $highestColumn = $worksheet->getHighestColumn(); // 获取最大列数

        // 从第2行开始遍历每一行
        for ($row = 2; $row <= $highestRow; ++$row) {
            // 从A列开始遍历每一列
            for ($col = 'A'; $col <= $highestColumn; ++$col) {
                $cell = $worksheet->getCell($col . $row); // 获取单元格对象
                $value = $cell->getValue(); // 获取单元格值
                if ($value) 
                {
                    $out[$row][$col] = $value;
                }
            }
        }
        return $out;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/plugins.log",$Log_content);
        return;
    }
}
