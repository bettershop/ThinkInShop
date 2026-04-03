<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;

use app\admin\model\GuideModel;

/**
 * 功能：引导图
 * 修改人：DHB
 */
class WeixinApp extends BaseController
{   
    // 引导图列表
    public function getWeiXinGuideImageInfo()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes(trim($this->request->param('id'))); // 引导图ID
        $list = array();
        if($id != '' && $id != 0)
        {
            $r = GuideModel::where(['store_id'=>$store_id,'id'=>$id])->select()->toArray();
            if ($r)
            {
                $r[0]['image'] = ServerPath::getimgpath($r[0]['image'], $store_id); // 轮播图
                $list = $r;
            }
            else
            {
                $message = Lang('参数错误！');
                return output(109,$message);
            }
        }
        else
        {
            $r = GuideModel::where(['store_id'=>$store_id])->order('sort','desc')->select()->toArray();
            if ($r)
            {
                foreach($r as $k => $v)
                {
                    $v['image'] = ServerPath::getimgpath($v['image'], $store_id); // 轮播图
                    $list[] = $v;
                }
            }
        }

        $data = array('total'=>count($list),'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加/编辑引导图
    public function addWeiXinGuideImage()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes(trim($this->request->post('id'))); // 引导图ID
        $top_type = addslashes(trim($this->request->post('source'))); // 1.小程序 2.APP
        $image = addslashes(trim($this->request->post('imgUrl'))); // 图片
    	$type = addslashes(trim($this->request->post('type'))); // 类型
    	$sort = addslashes(trim($this->request->post('sort'))); // 排序

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
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 引导图不能为空';
            $this->Log($Log_content);
            $message = Lang('引导图不能为空！');
            return output(109,$message);
        }
        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('image'=>$image,'sort'=>$sort,'type'=>$type);
            $r = Db::name('guide')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改引导图失败',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改引导图失败！条件参数: ' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('未知原因，修改失败！');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改引导图',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改引导图成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            // 添加
            $sql = array('store_id'=>$store_id,'image'=>$image,'source'=>$top_type,'sort'=>$sort,'type'=>$type,'add_date'=>$time);
            $r = Db::name('guide')->insert($sql);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加引导图失败',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加引导图失败！参数: ' . json_encode($sql);
                $this->Log($Log_content);
                $message = Lang('未知原因，添加失败！');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加引导图',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加引导图成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // 删除引导图
    public function delWeiXinGuideImage()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
    	$store_type = addslashes(trim($this->request->param('storeType')));
    	$access_id = addslashes(trim($this->request->param('accessId')));
        
        $id = addslashes(trim($this->request->param('id'))); // 引导图ID
        
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        $time = date("Y-m-d H:i:s");

        // 根据轮播图id，删除轮播图信息
        $sql = array('store_id'=>$store_id,'id'=>$id);
        $r1 = Db::table('lkt_guide')->where($sql)->delete();
        if ($r1 > 0)
        {
            $Jurisdiction->admin_record($store_id, $operator,'删除引导图ID为' . $id,3,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除引导图成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator,'删除引导图ID为' . $id . '失败',3,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除引导图失败！ID: ' . $id;
            $this->Log($Log_content);
            $message = Lang('失败！');
            return output(109,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/weixinApp.log",$Log_content);
        return;
    }
}
