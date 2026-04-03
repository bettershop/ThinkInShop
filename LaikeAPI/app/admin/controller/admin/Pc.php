<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;

use app\admin\model\BannerModel;

/**
 * 功能：PC端轮播图
 * 修改人：PJY
 */
class Pc extends BaseController
{   
    // 获取PC端轮播图
    public function getBannerInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 轮播图ID
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
    	$pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? $pagesize : 10;

        $sql_where = array('store_id'=>$store_id,'type'=>4,'mch_id'=>0);
        if($id != 0 && $id != '')
        {
            $sql_where['id'] = $id;
        }
        $total = 0;
        $res_num = BannerModel::where($sql_where)->field('count(id) as num')->select()->toArray();
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }

        $list = array();
        // 查询轮播图表，根据sort顺序排列
        $r = BannerModel::where($sql_where)->page((int)$page,(int)$pagesize)->order('sort','desc')->select()->toArray();
        if($r)
        {
            foreach ($r as $k => $v)
            {
                $v['image'] = ServerPath::getimgpath($v['image'], $store_id);
                $list[] = $v;
            }
        }
       
        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加/编辑轮播图
    public function addBannerInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));
        
    	$id = addslashes(trim($this->request->param('id'))); // 轮播图ID
    	$image = addslashes(trim($this->request->param('imageUrl'))); // 图片
    	$url = addslashes(trim($this->request->param('path'))); // 路径

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");
        
        if ($image)
        {
            $image = preg_replace('/.*\//', '', $image);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 轮播图不能为空！';
            $this->Log($Log_content);
            $message = Lang('pc.0');
            return output(109,$message);
        }

        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id,'type'=>4);
            $sql_update = array('image'=>$image,'url'=>$url,'add_date'=>$time);
            $r = Db::name('banner')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改轮播图ID为'.$id.'失败',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改轮播图失败！参数: ' . json_encode($sql_where);
                $this->Log($Log_content);
                $message = Lang('pc.1');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改轮播图ID为'.$id,2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改轮播图成功！';
                $this->Log($Log_content);
                $message = Lang('pc.2');
                return output(200,$message);
            }
        }
        else
        {
            $rr = BannerModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
            $sort = $rr[0]['sort'];
            $sort = $sort + 1;
    
            // 添加
            $sql = array('store_id'=>$store_id,'open_type'=>'','image'=>$image,'url'=>$url,'sort'=>$sort,'type'=>4,'add_date'=>$time);
            $r = Db::name('banner')->insert($sql);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加轮播图失败',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加轮播图失败！参数: ' . json_encode($sql);
                $this->Log($Log_content);
                $message = Lang('pc.3');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加轮播图',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加轮播图成功！';
                $this->Log($Log_content);
                $message = Lang('pc.4');
                return output(200,$message);
            }
        }
    }

    // 置顶轮播图
    public function topBannerById()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes(trim($this->request->param('id'))); // 轮播图ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        // 查询最大的排序号
        $rr = BannerModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $rr[0]['sort'];
        $sort = $sort + 1;

        // 根据轮播图ID修改排序
        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $sql_update = array('sort'=>$sort);
        $r = Db::name('banner')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $Jurisdiction->admin_record($store_id, $operator, '置顶轮播图ID为' . $id,7,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 置顶轮播图成功！';
            $this->Log($Log_content);
            $message = Lang('pc.5');
            return output(200,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '置顶轮播图ID为' . $id . '失败',7,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 置顶轮播图失败！ID: ' . $id;
            $this->Log($Log_content);
            $message = Lang('pc.6');
            return output(109,$message);
        }
    }

    // 删除轮播图
    public function delBannerById()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes(trim($this->request->param('id'))); // 轮播图ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        // 根据轮播图id，删除轮播图信息
        $sql = array('store_id'=>$store_id,'id'=>$id);
        $res = Db::table('lkt_banner')->where($sql)->delete();
        if ($res > 0)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除轮播图ID为' . $id,3,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 删除轮播图成功！';
            $this->Log($Log_content);
            $message = Lang('pc.7');
            return output(200,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除轮播图ID为' . $id . '失败',3,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 删除轮播图失败！参数: ' . json_encode($sql);
            $this->Log($Log_content);
            $message = Lang('pc.8');
            return output(109,$message);
        }
    }

    // 获取PC商城配置
    public function getConfig()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 轮播图ID
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? $pagesize : 10;

        $sql_where = array('store_id'=>$store_id);
        if($id != 0 && $id != '')
        {
            $sql_where['id'] = $id;
        }
        $total = 0;
        $list = array();
        // 查询轮播图表，根据sort顺序排列
        $r = Db::name('pc_mall_config')->where($sql_where)->select()->toArray();
        if($r)
        {
            foreach ($r as $k => $v)
            {
                $list[$v['type']] = $v['value'];
            }
        }
       
        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$list);
    }

    // 添加/编辑PC商城配置（原）
    public function addConfig()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes(trim($this->request->param('id'))); // 轮播图ID
        $mallIcon = addslashes(trim($this->request->param('mallIcon'))); // 浏览器标签-图标
        $mallName = addslashes(trim($this->request->param('mallName'))); // 浏览器标签-名称
        
        $mallLogo = addslashes(trim($this->request->param('mallLogo'))); // 商城logo
        $shortcutMenu2 = addslashes(trim($this->request->param('shortcutMenu2'))); // 快捷菜单-登录页
        $archival = addslashes(trim($this->request->param('archival'))); // 版权信息
        $copyright = addslashes(trim($this->request->param('copyright'))); // 备案信息
        $authority = addslashes(trim($this->request->param('authority'))); // 官网网站
        $list = trim($this->request->param('list')); // 底部链接

        $welcomeTerm = addslashes(trim($this->request->param('welcomeTerm'))); // 欢迎术语
        $shortcutMenu3 = addslashes(trim($this->request->param('shortcutMenu3'))); // 快捷菜单-首页
        $APPUrl = addslashes(trim($this->request->param('APPUrl'))); // 下载APP二维码
        $APPExplain = addslashes(trim($this->request->param('APPExplain'))); // 下载APP说明
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        if ($mallIcon == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' PC商城浏览器标签图标不能为空!';
            $this->Log($Log_content);
            $message = Lang('bottom.5');
            return output(109,$message);
        }
        if ($mallName == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' PC商城浏览器标签名称不能为空!';
            $this->Log($Log_content);
            $message = Lang('bottom.6');
            return output(109,$message);
        }
        if ($mallLogo == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' PC商城登录页商城logo不能为空!';
            $this->Log($Log_content);
            $message = Lang('bottom.7');
            return output(109,$message);
        }
        if ($welcomeTerm == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' PC商城首页欢迎术语不能为空!';
            $this->Log($Log_content);
            $message = Lang('bottom.8');
            return output(109,$message);
        }

        if ($APPUrl == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' PC商城下载APP二维码不能为空!';
            $this->Log($Log_content);
            $message = Lang('bottom.9');
            return output(109,$message);
        }
        if ($APPExplain == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' PC商城下载APP说明不能为空!';
            $this->Log($Log_content);
            $message = Lang('bottom.10');
            return output(109,$message);
        }

        // 添加
        $r1 = array('mallIcon'=>$mallIcon,'mallName'=>$mallName);
        $r2 = array('mallLogo'=>$mallLogo,'copyright'=>$copyright,'authority'=>$authority,'list'=>$list,'archival'=>$archival,'shortcutMenu2'=>$shortcutMenu2);
        $r3 = array('APPExplain'=>$APPExplain,'APPUrl'=>$APPUrl,'welcomeTerm'=>$welcomeTerm,'shortcutMenu3'=>$shortcutMenu3);

        $res1 = Db::name('pc_mall_config')->where('store_id',$store_id)->delete();

        $data = array(
            array('store_id'=>$store_id,'type'=>1,'value'=>json_encode($r1,320),'add_date'=>$time),
            array('store_id'=>$store_id,'type'=>2,'value'=>json_encode($r2,320),'add_date'=>$time),
            array('store_id'=>$store_id,'type'=>3,'value'=>json_encode($r3,320),'add_date'=>$time)
        );

        $res = Db::name('pc_mall_config')->insertAll($data);
        if ($res == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了PC商城的配置信息失败',2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加PC商城配置失败！参数: ' . json_encode($sql);
            $this->Log($Log_content);
            $message = Lang('pc.3');
            return output(109,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '修改了PC商城的配置信息',2,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加PC商城配置成功！';
            $this->Log($Log_content);
            $message = Lang('pc.4');
            return output(200,$message);
        }
    }

    // 获取PC端底部栏配置
    public function getBottomInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 底部栏ID
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据
        $page = $page ? $page : 1;
        $pagesize = $pagesize ? $pagesize : 10;

        $sql_where = array('store_id'=>$store_id);
        if($id != 0 && $id != '')
        {
            $sql_where['id'] = $id;
        }
        $total = 0;
        $list = array();
        // 查询配置表，根据sort顺序排列
        $r = Db::name('pc_mall_bottom')->where($sql_where)->order('sort','desc')->select()->toArray();
        if($r)
        {   
            $total = count($r);
            foreach ($r as $k => $v)
            {
                $list[] = $v;
            }
        }
       
        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加/编辑PC商城底部栏配置
    public function addBottomInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes($this->request->post('id')); // id
        $images = addslashes($this->request->post('images')); // 图标
        $title = addslashes($this->request->post('title')); // 标题
        $subheading = addslashes($this->request->post('subheading')); // 副标题
        $sort = addslashes($this->request->post('sort')); // 排序号
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        if ($images == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' PC商城底部栏图标不能为空!';
            $this->Log($Log_content);
            $message = Lang('bottom.1');
            return output(109,$message);
        }
        if ($title == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator.' PC商城底部栏标题不能为空!';
            $this->Log($Log_content);
            $message = Lang('bottom.2');
            return output(109,$message);
        }

        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('image'=>$images,'title'=>$title,'subheading'=>$subheading,'sort'=>$sort,'update_date'=>$time);
            $r = Db::name('pc_mall_bottom')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改PC商城的底部栏配置失败',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改底部栏配置失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('bottom.1');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改PC商城的底部栏配置',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 修改底部栏配置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            // 查询最大的排序号
            $r_sort = Db::name('pc_mall_bottom')->where('store_id', $store_id)->value('MAX(sort) as sort');
            $sort = $r_sort + 1;

            $sql_inset = array('store_id'=>$store_id,'image'=>$images,'title'=>$title,'subheading'=>$subheading,'sort'=>$sort,'add_date'=>$time);
            $r = Db::name('pc_mall_bottom')->insert($sql_inset);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加PC商城的底部栏配置失败',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加底部栏配置失败！参数: ' . json_encode($sql_inset);
                $this->Log($Log_content);
                $message = Lang('bottom.3');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加PC商城的底部栏配置',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 添加底部栏配置成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // 删除底部栏配置
    public function delBottomById()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes(trim($this->request->param('id'))); // 配置ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        // 根据轮播图id，删除轮播图信息
        $sql = array('store_id'=>$store_id,'id'=>$id);
        $res = Db::name('pc_mall_bottom')->where($sql)->delete();
        if ($res > 0)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除PC商城的底部栏配置ID:' . $id,3,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 删除底部栏配置成功！';
            $this->Log($Log_content);
            $message = Lang('pc.7');
            return output(200,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除PC商城的底部栏配置ID:' . $id . '失败',3,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 删除底部栏配置失败！参数: ' . json_encode($sql);
            $this->Log($Log_content);
            $message = Lang('pc.8');
            return output(109,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/pc.log",$Log_content);
        return;
    }
}
