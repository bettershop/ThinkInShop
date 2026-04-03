<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Plugin\Plugin;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;

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
class Diy extends BaseController
{   
    public function index()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $name  = '默认模板'; // 当前应用名称
        $H5_domain = '';
        $list = array();

        $r_config = ConfigModel::where(['store_id'=>$store_id])->field('H5_domain')->select()->toArray();
        if($r_config)
        {
            $H5_domain = $r_config[0]['H5_domain'];
        }

        $data = array('H5_domain' => $H5_domain,'list' => $list,'name' => $name,'status' => 0,'use_id' => '');
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 轮播图列表
    public function bannerIndex()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';

        $list = array();
        $total = 0;
        $r0 = BannerModel::where(['store_id'=>$store_id,'mch_id'=>0])->where('type','<>','4')->select()->toArray();
        if($r0)
        {
            $total = count($r0);
        }

        $r1 = BannerModel::where(['store_id'=>$store_id,'mch_id'=>0])->where('type','<>','4')->page($page,$pagesize)->order('sort','desc')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $v['image'] = ServerPath::getimgpath($v['image'], $store_id);
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取路径
    public function bannerPathList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $type0 = trim($this->request->param('type'));

        $where_list = array('store_id'=>$store_id,'type0'=>$type0,'status'=>1,'type'=>1);
        $list = array();
        $total = 0;
        // if($type0 != 2)
        // {
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
                        $r1 = ProductListModel::where(['id'=>$parameter,'recycle'=>0,'mch_status'=>2,'status'=>2])->field('id')->select()->toArray();
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
        // }

        $data = array('list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 添加/编辑轮播图
    public function bannerSave()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 轮播图ID
        $type0 = trim($this->request->param('type0')); // 类型 1.分类 2.商品 3.店铺 4.pc店铺 0=自定义
        $url = trim($this->request->param('url'));// 路径
        $image = trim($this->request->param('picUrl')); // 图片

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

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

        $time = date("Y-m-d H:i:s");
        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('open_type'=>$type0,'image'=>$image,'url'=>$url,'add_date'=>$time);
            $r = Db::name('banner')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改轮播图失败！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改轮播图失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('pc.1');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改轮播图成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改轮播图成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            // 查询最大的排序号
            $r_sort = BannerModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
            $sort = $r_sort[0]['sort'];
            $sort = $sort + 1;

            $sql_inset = array('store_id'=>$store_id,'open_type'=>$type0,'image'=>$image,'url'=>$url,'sort'=>$sort,'type'=>0,'add_date'=>$time);
            $r = Db::name('banner')->insert($sql_inset);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '添加轮播图失败！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加轮播图失败！参数: ' . json_encode($sql_inset);
                $this->Log($Log_content);
                $message = Lang('pc.3');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '添加轮播图成功！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加轮播图成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // 上移/下移轮播图
    public function bannerRemove()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 互换排序的轮播图ID
        $id1 = trim($this->request->param('id1')); // 操作的轮播图ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();
        // 查询互换排序的轮播图ID的排序
        $r0 = BannerModel::where(['store_id'=>$store_id,'id'=>$id])->field('sort')->select()->toArray();
        $sort0 = $r0[0]['sort']; // 排序号

        // 操作的轮播图ID的排序
        $r1 = BannerModel::where(['store_id'=>$store_id,'id'=>$id1])->field('sort')->select()->toArray();
        $sort1 = $r1[0]['sort']; // 排序号

        if($sort0 == $sort1)
        {
            $sort0 = (int)$sort0 + 1;
        }

        // 修改当前轮播图排序
        $sql_where2 = array('store_id'=>$store_id,'id'=>$id);
        $sql_update2 = array('sort'=>$sort1);
        $r2 = Db::name('banner')->where($sql_where2)->update($sql_update2);
        
        // 修改上条数据轮播图排序
        $sql_where3 = array('store_id'=>$store_id,'id'=>$id1);
        $sql_update3 = array('sort'=>$sort0);
        $r3 = Db::name('banner')->where($sql_where3)->update($sql_update3);
        if ($r2 > -1 && $r3 > -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改轮播图排序成功！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改轮播图排序成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改轮播图排序失败！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改轮播图排序失败！参数：'.json_encode($sql_where2).'；参数：'.json_encode($sql_where3);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('pc.1');
            return output(200,$message);
        }
    }

    // 置顶轮播图
    public function bannerMoveTop()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 轮播图ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        // 查询最大的排序号
        $r_sort = BannerModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $r_sort[0]['sort'];
        $sort = $sort + 1;

        // 修改当前轮播图排序
        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $sql_update = array('sort'=>$sort);
        $r = Db::name('banner')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶轮播图ID为' . $id . '成功', 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶轮播图成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶轮播图ID为' . $id . '失败', 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶轮播图失败！参数：' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('pc.6');
            return output(200,$message);
        }
    }

    // 删除轮播图
    public function bannerDel()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 轮播图ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $r = Db::table('lkt_banner')->where($sql_where)->delete();
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '删除轮播图id为' . $id, 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除轮播图成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output('200',$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '删除轮播图id为' . $id . '失败', 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除轮播图失败！参数: ' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('pc.7');
            return output(109,$message);
        }
    }

    // UI导航栏
    public function uiIndex()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';

        $list = array();
        $total = 0;
        
        $r0 = UiNavigationBarModel::where(['store_id'=>$store_id])->select()->toArray();
        if($r0)
        {
            $total = count($r0);
        }

        $r1 = UiNavigationBarModel::where(['store_id'=>$store_id])->page($page,$pagesize)->order('sort','desc')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $v['image'] = ServerPath::getimgpath($v['image'], $store_id);
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 添加/编辑UI导航栏
    public function uiSave()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID
        $name = trim($this->request->param('name')); // 名称
        $image = trim($this->request->param('picUrl')); // 图片
        $type = trim($this->request->param('type0')); // 类型
        $url = trim($this->request->param('url')); // 路径
        $isshow = trim($this->request->param('isShow')); // 是否显示

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];
        $time = date("Y-m-d H:i:s");

        if ($name)
        {
            if($id != '' && $id != 0)
            {
                $r0 = UiNavigationBarModel::where(['store_id'=>$store_id,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            else
            {
                $r0 = UiNavigationBarModel::where(['store_id'=>$store_id,'name'=>$name])->field('id')->select()->toArray();
            }
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' UI导航栏名称已存在！';
                $this->Log($Log_content);
                $message = Lang('UI导航栏名称已存在！');
                return output(109,$message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' UI导航栏名称不能为空！';
            $this->Log($Log_content);
            $message = Lang('UI导航栏名称不能为空！');
            return output(109,$message);
        }

        if ($image)
        {
            $image = preg_replace('/.*\//', '', $image);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' UI导航栏图片不能为空！';
            $this->Log($Log_content);
            $message = Lang('UI导航栏图片不能为空！');
            return output(109,$message);
        }

        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('name'=>$name,'image'=>$image,'type'=>$type,'url'=>$url,'isshow'=>$isshow);
            $r = Db::name('ui_navigation_bar')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改UI导航栏失败！ID：' . $id . '失败', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('pc.1');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改UI导航栏成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            // 查询最大的排序号
            $r_sort = UiNavigationBarModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
            $sort = $r_sort[0]['sort'];
            $sort = $sort + 1;

            $sql_inset = array('store_id'=>$store_id,'name'=>$name,'image'=>$image,'type'=>$type,'url'=>$url,'sort'=>$sort,'add_date'=>$time,'isshow'=>$isshow);
            $r = Db::name('ui_navigation_bar')->insert($sql_inset);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 添加UI导航栏失败！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加UI导航栏失败！参数: ' . json_encode($sql_inset);
                $this->Log($Log_content);
                $message = Lang('添加失败！');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '添加UI导航栏成功！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加UI导航栏成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // UI导航栏是否显示
    public function uiIsShowSwitch()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];
        $time = date("Y-m-d H:i:s");

        $r0 = UiNavigationBarModel::where(['store_id'=>$store_id,'id'=>$id])->field('isshow')->select()->toArray();
        if($r0)
        {
            $isshow = $r0[0]['isshow'];

            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            if($isshow == 1)
            {
                $sql_update = array('isshow'=>0);
            }
            else
            {
                $sql_update = array('isshow'=>1);
            }
            $r = Db::name('ui_navigation_bar')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改UI导航栏失败！ID：' . $id . '失败', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('修改UI导航栏失败！');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改UI导航栏成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // UI导航栏是否显示
    public function uiIsLoginSwitch()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];
        $time = date("Y-m-d H:i:s");

        $r0 = UiNavigationBarModel::where(['store_id'=>$store_id,'id'=>$id])->field('is_login')->select()->toArray();
        if($r0)
        {
            $is_login = $r0[0]['is_login'];

            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            if($is_login == 1)
            {
                $sql_update = array('is_login'=>0);
            }
            else
            {
                $sql_update = array('is_login'=>1);
            }
            $r = Db::name('ui_navigation_bar')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改UI导航栏失败！ID：' . $id . '失败', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('修改UI导航栏失败！');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改UI导航栏成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
    }

    // 上移/下移UI导航栏
    public function uiMove()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 互换排序的UI导航栏ID
        $id1 = trim($this->request->param('id1')); // 操作的UI导航栏ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();
        // 互换排序的UI导航栏ID的排序
        $r0 = UiNavigationBarModel::where(['store_id'=>$store_id,'id'=>$id])->field('sort')->select()->toArray();
        $sort0 = $r0[0]['sort']; // 排序号

        // 操作的UI导航栏ID的排序
        $r1 = UiNavigationBarModel::where(['store_id'=>$store_id,'id'=>$id1])->field('sort')->select()->toArray();
        $sort1 = $r1[0]['sort']; // 排序号

        if($sort0 == $sort1)
        {
            $sort0 = (int)$sort0 + 1;
        }

        // 修改当前轮播图排序
        $sql_where2 = array('store_id'=>$store_id,'id'=>$id);
        $sql_update2 = array('sort'=>$sort1);
        $r2 = Db::name('ui_navigation_bar')->where($sql_where2)->update($sql_update2);
        
        // 修改上条数据轮播图排序
        $sql_where3 = array('store_id'=>$store_id,'id'=>$id1);
        $sql_update3 = array('sort'=>$sort0);
        $r3 = Db::name('ui_navigation_bar')->where($sql_where3)->update($sql_update3);
        if ($r2 > -1 && $r3 > -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改UI导航栏排序成功！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏排序成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改UI导航栏排序失败！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改UI导航栏排序失败！参数：'.json_encode($sql_where2).'；参数：'.json_encode($sql_where3);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('修改UI导航栏失败！');
            return output(200,$message);
        }
    }

    // 置顶UI导航栏
    public function uiTop()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        // 查询最大的排序号
        $r_sort = UiNavigationBarModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $r_sort[0]['sort'];
        $sort = $sort + 1;

        // 修改当前UI导航栏排序
        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $sql_update = array('sort'=>$sort);
        $r = Db::name('ui_navigation_bar')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶UI导航栏成功！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶UI导航栏成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶UI导航栏失败！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶UI导航栏失败！参数：' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('修改UI导航栏失败！');
            return output(200,$message);
        }
    }

    // 删除UI导航栏
    public function uiDel()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $sql_where = array('store_id'=>$store_id,'id'=>$id);
        $r = Db::table('lkt_ui_navigation_bar')->where($sql_where)->delete();
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 删除UI导航栏成功！ID：' . $id, 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除UI导航栏成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output('200',$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 删除UI导航栏失败！ID：' . $id, 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除UI导航栏失败！参数: ' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('删除失败！');
            return output(200,$message);
        }
    }

    // 分类管理
    public function classIndex()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';

        $list = array();
        $total = 0;
        
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'sid'=>0,'recycle'=>0])->field('cid')->select()->toArray();
        if($r0)
        {
            $total = count($r0);
        }

        $r1 = ProductClassModel::where(['store_id'=>$store_id,'sid'=>0,'recycle'=>0])->page($page,$pagesize)->order('sort','desc')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 上移/下移分类
    public function classMove()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 互换排序的分类ID
        $id1 = trim($this->request->param('id1')); // 操作的分类ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();
        // 互换排序的分类ID的排序
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$id])->field('sort')->select()->toArray();
        $sort0 = $r0[0]['sort']; // 排序号

        // 操作的分类ID的排序
        $r1 = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$id1])->field('sort')->select()->toArray();
        $sort1 = $r1[0]['sort']; // 排序号

        if($sort0 == $sort1)
        {
            $sort0 = (int)$sort0 + 1;
        }

        // 修改当前轮播图排序
        $sql_where2 = array('store_id'=>$store_id,'cid'=>$id);
        $sql_update2 = array('sort'=>$sort1);
        $r2 = Db::name('product_class')->where($sql_where2)->update($sql_update2);
        
        // 修改上条数据轮播图排序
        $sql_where3 = array('store_id'=>$store_id,'cid'=>$id1);
        $sql_update3 = array('sort'=>$sort0);
        $r3 = Db::name('product_class')->where($sql_where3)->update($sql_update3);
        if ($r2 > -1 && $r3 > -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改分类排序成功！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改分类排序成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改分类排序失败！参数：' . $id1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改分类排序失败！参数：'.json_encode($sql_where2).'；参数：'.json_encode($sql_where3);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('修改分类失败！');
            return output(200,$message);
        }
    }

    // 置顶分类
    public function classTop()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 分类ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        // 查询最大的排序号
        $r_sort = ProductClassModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $r_sort[0]['sort'];
        $sort = $sort + 1;

        // 修改当前UI导航栏排序
        $sql_where = array('store_id'=>$store_id,'cid'=>$id);
        $sql_update = array('sort'=>$sort);
        $r = Db::name('product_class')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶分类成功！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶分类成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶分类失败！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶分类失败！参数：' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('修改分类失败！');
            return output(200,$message);
        }
    }

    // 是否显示-分类
    public function classSwitch()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 分类ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $is_display = 1;
        $r0 = ProductClassModel::where(['store_id'=>$store_id,'cid'=>$id])->field('is_display')->select()->toArray();
        if($r0)
        {
            if($r0[0]['is_display'] == 1)
            {
                $is_display = 0;
            }
        }

        // 修改当前UI导航栏排序
        $sql_where = array('store_id'=>$store_id,'cid'=>$id);
        $sql_update = array('is_display'=>$is_display);
        $r = Db::name('product_class')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改分类成功！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改分类成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改分类失败！ID：' . $id, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改分类失败！参数：' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('修改分类失败！');
            return output(200,$message);
        }
    }

    // 活动管理
    public function activityList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id'));
        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $page = $page ? $page : '1';
        $pagesize = $pagesize ? $pagesize : '10';

        $list = array();
        $total = 0;
        
        $where_list = array('store_id'=>$store_id);
        if($id != '' && $id != 0)
        {
            $where_list['id'] = $id;
        }
        $r0 = ActivityModel::where($where_list)->field('id')->select()->toArray();
        if($r0)
        {
            $total = count($r0);
        }

        $r1 = ActivityModel::where($where_list)->page($page,$pagesize)->order('sort','desc')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取商品-添加商品
    public function getGoodsList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';

        $list = array();
        $total = 0;
        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $r_admin = AdminModel::where(['store_id'=>$store_id,'type'=>1,'recycle'=>0])->field('shop_id')->select()->toArray();
        if($r_admin)
        {
            $mch_id = $r_admin[0]['shop_id'];
        }
        
        $condition = "a.store_id = '$store_id' and a.recycle = 0 and a.is_presell = 0 and a.commodity_type = '0' and a.mch_id = '$mch_id' ";

        $sql0 = "select count(a.id) as total from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $str = "a.id,a.product_title,a.imgurl,a.volume,a.s_type,a.num,a.status,a.cover_map,a.product_class as classId,a.brand_id,b.name as mch_name,b.logo";

        $sql1 = "select $str from lkt_product_list as a left join lkt_mch as b on a.mch_id = b.id where $condition order by a.sort asc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'],$store_id);

                $pid = $v['id'];
                $r_s = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('min(price) as price')->select()->toArray();
                if ($r_s)
                {
                    $v['price'] = round($r_s[0]['price'],2);
                }
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取插件数据
    public function getPluginTypeList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $Plugin_list = array();
        $Plugin = new Plugin();
        $Plugin_arr1 = $Plugin->front_Plugin($store_id);

        foreach ($Plugin_arr1 as $k => $v)
        {
            if ($k == 'SecondsPublic' && $v == 1)
            {
                $Plugin_list[] = array('value'=>8,'name'=>'秒杀','status'=>false);
            }
            else if ($k == 'Auction' && $v == 1)
            {
                $Plugin_list[] = array('value'=>4,'name'=>'竞拍','status'=>false);
            }
            else if ($k == 'IntegralPublicMethod' && $v == 1)
            {
                $Plugin_list[] = array('value'=>7,'name'=>'积分','status'=>false);
            }
        }

        $data = array('list' => $Plugin_list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 添加/编辑活动
    public function activitySave()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // UI导航栏ID
        $activity_type = trim($this->request->param('activityType')); // 类型 0.活动专题 1.营销插件
        $plug_type = trim($this->request->param('plugType')); // 插件类型
        $name = trim($this->request->param('name')); // 活动名称
        $p_id = trim($this->request->param('pid')); // 商品ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];
        $time = date("Y-m-d H:i:s");
        Db::startTrans();
        if($activity_type == 1)
        {
            if($plug_type == 0 || $plug_type == '')
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择活动类型！';
                $this->Log($Log_content);
                $message = Lang('请选择活动类型！');
                return output(109,$message);
            }
        }
        else
        {
            $plug_type = 0;
        }

        if ($name)
        {
            if($id != '' && $id != 0)
            {
                $r0 = ActivityModel::where(['store_id'=>$store_id,'name'=>$name])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            else
            {
                $r0 = ActivityModel::where(['store_id'=>$store_id,'name'=>$name])->field('id')->select()->toArray();
            }
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 活动标题已存在！';
                $this->Log($Log_content);
                $message = Lang('活动标题已存在！');
                return output(109,$message);
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 活动标题不能为空！';
            $this->Log($Log_content);
            $message = Lang('活动标题不能为空！');
            return output(109,$message);
        }

        if($id != '' && $id != 0)
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $sql_update = array('activity_type'=>$activity_type,'plug_type'=>$plug_type,'name'=>$name,'p_id'=>$p_id);
            $r = Db::name('activity')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动失败！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $lktlog->customerLog($Log_content);
                Db::rollback();
                $message = Lang('修改失败！');
                return output(109,$message);
            }
            else
            {
                $r_1 = Db::table('lkt_activity_pro')->where(['store_id'=>$store_id,'activity_id'=>$id])->delete();

                if($p_id != '')
                {
                    $p_list = explode(',',$p_id);
                    $z_sort = count($p_list);
                    foreach ($p_list as $k => $v)
                    {
                        $sql_inset_pro = array('store_id'=>$store_id,'activity_id'=>$id,'p_id'=>$v,'is_display'=>0,'sort'=>$z_sort,'add_date'=>$time,'is_display'=>1);
                        $r_activity_pro = Db::name('activity_pro')->insertGetId($sql_inset_pro);
                        if($r_activity_pro <= 0)
                        {
                            $JurisdictionAction->admin_record($store_id, $admin_name, ' 添加活动商品失败！', 1);
                            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加活动商品失败！参数: ' . json_encode($sql_inset_pro);
                            $this->Log($Log_content);
                            Db::rollback();
                            $message = Lang('添加失败！');
                            return output(109,$message);
                        }
                        $z_sort = $z_sort - 1;
                    }
                }

                $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动成功！';
                $this->Log($Log_content);
                Db::commit();
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            // 查询最大的排序号
            $r_sort = ActivityModel::where(['store_id'=>$store_id])->field('MAX(sort) as sort')->select()->toArray();
            $sort = $r_sort[0]['sort'];
            $sort = $sort + 1;

            $sql_inset = array('store_id'=>$store_id,'activity_type'=>$activity_type,'plug_type'=>$plug_type,'name'=>$name,'p_id'=>$p_id,'sort'=>$sort,'add_date'=>$time,'is_display'=>1);
            $r = Db::name('activity')->insert($sql_inset);
            if ($r > 0)
            {
                if($p_id != '')
                {
                    $p_list = explode(',',$p_id);
                    $z_sort = count($p_list);
                    foreach ($p_list as $k => $v)
                    {
                        $sql_inset_pro = array('store_id'=>$store_id,'activity_id'=>$r,'p_id'=>$v,'is_display'=>0,'sort'=>$z_sort,'add_date'=>$time,'is_display'=>1);
                        $r_activity_pro = Db::name('activity_pro')->insertGetId($sql_inset_pro);
                        if($r_activity_pro <= 0)
                        {
                            $JurisdictionAction->admin_record($store_id, $admin_name, ' 添加活动商品失败！', 1);
                            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加活动商品失败！参数: ' . json_encode($sql_inset_pro);
                            $this->Log($Log_content);
                            Db::rollback();
                            $message = Lang('添加失败！');
                            return output(109,$message);
                        }
                        $z_sort = $z_sort - 1;
                    }
                }
                $JurisdictionAction->admin_record($store_id, $admin_name, '添加活动成功！', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加活动成功！';
                $this->Log($Log_content);
                Db::commit();
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '添加活动失败', 1);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加活动失败！参数: ' . json_encode($sql_inset);
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('添加失败！');
                return output(109,$message);
            }
        }
    }

    // 获取商品-编辑页码
    public function getActGoodsList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id'));

        $list = array();
        $total = 0;

        $r0 = ActivityProModel::where(['store_id'=>$store_id,'activity_id'=>$id])->field('count(id) as total')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $r1 = ActivityProModel::where(['store_id'=>$store_id,'activity_id'=>$id])->order('sort','desc')->select()->toArray();
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $pid = $v['p_id'];
                $v['imgurl'] = '';
                $v['product_title'] = '';
                $v['num'] = 0;
                $v['price'] = 0;
                $r2 = ProductListModel::where(['id'=>$pid])->field('product_title,imgurl,num')->select()->toArray();
                if($r2)
                {
                    $v['imgurl'] = ServerPath::getimgpath($r2[0]['imgurl'],$store_id);
                    $v['product_title'] = $r2[0]['product_title'];
                    $v['num'] = $r2[0]['num'];
                }

                $r3 = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->field('min(price) as price')->select()->toArray();
                if ($r3)
                {
                    $v['price'] = round($r3[0]['price'],2);
                }
                $list[] = $v;
            }
        }

        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 活动是否显示
    public function activitySwitch()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 活动商品ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $r0 = ActivityModel::where(['store_id'=>$store_id,'id'=>$id])->field('is_display')->select()->toArray();
        if($r0)
        {
            $is_display = $r0[0]['is_display'];

            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            if($is_display == 1)
            {
                $sql_update = array('is_display'=>0);
            }
            else
            {
                $sql_update = array('is_display'=>1);
            }
            $r = Db::name('activity')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动商品失败！ID：' . $id . '失败', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('修改活动商品失败！');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改活动商品成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 商品是否显示
    public function actGoodsSwitch()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 活动商品ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        $r0 = ActivityProModel::where(['store_id'=>$store_id,'activity_id'=>$id])->field('is_display')->select()->toArray();
        if($r0)
        {
            $is_display = $r0[0]['is_display'];

            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            if($is_display == 1)
            {
                $sql_update = array('is_display'=>0);
            }
            else
            {
                $sql_update = array('is_display'=>1);
            }
            $r = Db::name('activity_pro')->where($sql_where)->update($sql_update);
            if ($r == -1)
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动商品失败！ID：' . $id . '失败', 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
                $this->Log($Log_content);
                $message = Lang('修改活动商品失败！');
                return output(109,$message);
            }
            else
            {
                $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改活动商品成功！ID：' . $id, 2);
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品成功！';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 上移/下移活动商品
    public function actGoodsMove()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 活动ID
        $goodsId = trim($this->request->param('goodsId')); // 互换排序的活动商品ID
        $goodsId1 = trim($this->request->param('goodsId1')); // 操作的活动商品ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();
        // 互换排序的分类ID的排序
        $r0 = ActivityProModel::where(['store_id'=>$store_id,'activity_id'=>$id,'id'=>$goodsId])->field('sort')->select()->toArray();
        $sort0 = $r0[0]['sort']; // 排序号

        // 操作的分类ID的排序
        $r1 = ActivityProModel::where(['store_id'=>$store_id,'activity_id'=>$id,'id'=>$goodsId1])->field('sort')->select()->toArray();
        $sort1 = $r1[0]['sort']; // 排序号

        if($sort0 == $sort1)
        {
            $sort0 = (int)$sort0 + 1;
        }

        // 修改当前轮播图排序
        $sql_where2 = array('store_id'=>$store_id,'activity_id'=>$id,'id'=>$goodsId);
        $sql_update2 = array('sort'=>$sort1);
        $r2 = Db::name('activity_pro')->where($sql_where2)->update($sql_update2);
        
        // 修改上条数据轮播图排序
        $sql_where3 = array('store_id'=>$store_id,'activity_id'=>$id,'id'=>$goodsId1);
        $sql_update3 = array('sort'=>$sort0);
        $r3 = Db::name('activity_pro')->where($sql_where3)->update($sql_update3);
        if ($r2 > -1 && $r3 > -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改活动商品排序成功！参数：' . $goodsId1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品排序成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动商品排序失败！参数：' . $goodsId1, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动商品排序失败！参数：'.json_encode($sql_where2).'；参数：'.json_encode($sql_where3);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('修改活动商品失败！');
            return output(200,$message);
        }
    }
    
    // 置顶活动商品
    public function actGoodsTop()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 活动ID
        $goodsId = trim($this->request->param('goodsId')); // 活动商品ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        // 查询最大的排序号
        $r_sort = ActivityProModel::where(['store_id'=>$store_id,'activity_id'=>$id])->field('MAX(sort) as sort')->select()->toArray();
        $sort = $r_sort[0]['sort'];
        $sort = $sort + 1;

        // 修改当前UI导航栏排序
        $sql_where = array('store_id'=>$store_id,'activity_id'=>$id,'id'=>$goodsId);
        $sql_update = array('sort'=>$sort);
        $r = Db::name('activity_pro')->where($sql_where)->update($sql_update);
        if ($r > 0)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶活动商品成功！ID：' . $goodsId, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶活动商品成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '置顶活动商品失败！ID：' . $goodsId, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 置顶活动商品失败！参数：' . json_encode($sql_where);
            $this->Log($Log_content);
            $message = Lang('修改活动商品失败！');
            return output(200,$message);
        }
    }

    // 上移/下移活动
    public function activityMove()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $moveId = trim($this->request->param('moveId')); // 互换排序的活动ID
        $moveId2 = trim($this->request->param('moveId2')); // 操作的活动ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();
        // 互换排序的分类ID的排序
        $r0 = ActivityModel::where(['store_id'=>$store_id,'id'=>$moveId])->field('sort')->select()->toArray();
        $sort0 = $r0[0]['sort']; // 排序号

        // 操作的分类ID的排序
        $r1 = ActivityModel::where(['store_id'=>$store_id,'id'=>$moveId2])->field('sort')->select()->toArray();
        $sort1 = $r1[0]['sort']; // 排序号

        if($sort0 == $sort1)
        {
            $sort0 = (int)$sort0 + 1;
        }

        // 修改当前轮播图排序
        $sql_where2 = array('store_id'=>$store_id,'id'=>$moveId);
        $sql_update2 = array('sort'=>$sort1);
        $r2 = Db::name('activity')->where($sql_where2)->update($sql_update2);
        
        // 修改上条数据轮播图排序
        $sql_where3 = array('store_id'=>$store_id,'id'=>$moveId2);
        $sql_update3 = array('sort'=>$sort0);
        $r3 = Db::name('activity')->where($sql_where3)->update($sql_update3);
        if ($r2 > -1 && $r3 > -1)
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 修改活动排序成功！参数：' . $moveId2, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动排序成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, '修改活动排序失败！参数：' . $moveId2, 7);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 修改活动排序失败！参数：'.json_encode($sql_where2).'；参数：'.json_encode($sql_where3);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('修改活动失败！');
            return output(200,$message);
        }
    }

    // 删除活动
    public function activityDel()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = trim($this->request->param('id')); // 活动ID

        $JurisdictionAction = new Jurisdiction();
        $admin_name = $this->user_list['name'];

        Db::startTrans();

        $where_list0 = array('store_id'=>$store_id,'id'=>$id);
        $r0 = Db::table('lkt_activity')->where($where_list0)->delete();
        if ($r0 > 0)
        {
            $r1 = Db::table('lkt_activity_pro')->where(['store_id'=>$store_id,'activity_id'=>$id])->delete();

            $JurisdictionAction->admin_record($store_id, $admin_name, '删除活动成功！ID：' . $id, 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除活动成功！';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $JurisdictionAction->admin_record($store_id, $admin_name, ' 删除活动失败！ID：' . $id, 3);
            $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除活动失败！参数：' . json_encode($where_list0);
            $this->Log($Log_content);
            Db::rollback();
            $message = Lang('删除活动失败！');
            return output(109,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/diy.log",$Log_content);
        return;
    }
}
