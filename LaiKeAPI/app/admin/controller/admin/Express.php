<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;

use app\common\LaiKeLogUtils;
use app\common\ExpressPublicMethod;
use app\common\PC_Tools;

use app\admin\model\ExpressModel;

class Express extends BaseController
{   
    // 物流管理
    public function index()
    {   
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $keyWord = trim($this->request->param('keyWord')); // 物流公司名称、编码
        $sortType = trim($this->request->param('sortType')); // 排序 1.降序 2.升序
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据
        $id = addslashes(trim($this->request->param('id'))); // 物流ID
     
        if($pagesize == '')
        {
            $pagesize = 10;
        }
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        
        $condition = " recycle = 0 ";
        if($keyWord != '')
        {
            $keyWord = Tools::FuzzyQueryConcatenation($keyWord);
            $condition .= " and (kuaidi_name like $keyWord OR type like $keyWord)";
        }
        if($id != '')
        {
            $condition .= " and id = '$id' ";
        }
        if($sortType == 1)
        {
            $order = " order by sort desc ";
        }
        else
        {
            $order = " order by sort asc ";
        }

        $list = array();
        $total = 0;

        $sql0 = "select count(id) as num from lkt_express where $condition";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }
        
        if($total > 0)
        {
            $sql1 = "select * from lkt_express where $condition $order limit $start,$pagesize";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $list = $r1;
            }
        }
        
        $data = array('total' => $total,  'list' => $list);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 添加物流
    public function expressSave()
    {   
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $id = trim($this->request->param('id')); // 物流ID
        $kuaidi_name = trim($this->request->param('name')); // 物流公司名称
        $type = trim($this->request->param('code')); // 编码
        $sort = addslashes(trim($this->request->param('sort'))); // 排序
        $is_open = addslashes(trim($this->request->param('switchse'))); // 开关

        $admin_name = $admin_list['name'];
        if($kuaidi_name != '')
        {
			$id = $id == 'null' ? null :$id ;
            // 判断唯一性
            if($id == '' || $id == null)
            {
                $r_0 = ExpressModel::where(['kuaidi_name'=>$kuaidi_name,'recycle'=>0])->field('id')->select()->toArray();
            }
            else
            {
                $r_0 = ExpressModel::where(['kuaidi_name'=>$kuaidi_name,'recycle'=>0])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            if($r_0)
            {
                $message = Lang("express.0");
                return output(400,$message);
            }
        }
        else
        {
            $message = Lang("express.1");
            return output(400,$message);
        }

        if($type != '')
        {
            //判断唯一性
            if($id == '')
            {
                $r_1 = ExpressModel::where(['type'=>$type,'recycle'=>0])->field('id')->select()->toArray();
            }
            else
            {
                $r_1 = ExpressModel::where(['type'=>$type,'recycle'=>0])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            if($r_1)
            {
                $message = Lang("express.2");
                return output(400,$message);
            }
        }
        else
        {
            $message = Lang("express.3");
            return output(400,$message);
        }

        $time = date("Y-m-d H:i:s");
        if($id == '' || $id == null)
        {
            $sql0 = array('kuaidi_name'=>$kuaidi_name,'type'=>$type,'is_open'=>$is_open,'sort'=>$sort,'add_date'=>$time);
            $r0 = Db::name('express')->insert($sql0);
            if($r0 < 1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加物流失败！';
                $this->Log($Log_content);
                $message = Lang("express.4");
                return output(400,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 添加物流成功！';
                $this->Log($Log_content);
                $message = Lang("Success");
                return output(200,$message);
            }
        }
        else
        {
            $sql_where = array('id'=>$id);
            if($is_open == '')
            {
                $sql_update = array('kuaidi_name'=>$kuaidi_name,'type'=>$type,'sort'=>$sort);
            }
            else
            {
                $sql_update = array('kuaidi_name'=>$kuaidi_name,'type'=>$type,'is_open'=>$is_open,'sort'=>$sort);
            }
            $r0 = Db::name('express')->where($sql_where)->update($sql_update);
            if($r0 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 编辑物流失败功！';
                $this->Log($Log_content);
                $message = Lang("express.5");
                return output(400,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 编辑物流成功！';
                $this->Log($Log_content);
                $message = Lang("Success");
                return output(200,$message);
            }
        }
    }
    
    // 修改开关
    public function expressSwitch()
    {   
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $id = trim($this->request->param('id')); // 物流ID

        $admin_name = $admin_list['name'];

        $r0 = ExpressModel::where('id',$id)->field('is_open')->select()->toArray();
        if($r0)
        {
            $is_open = $r0[0]['is_open'];
            if($r0[0]['is_open'] == 1)
            {
                $sql_update = array('is_open'=>0);
            }
            else
            {
                $sql_update = array('is_open'=>1);
            }

            $sql_where = array('id'=>$id);
            $r1 = Db::name('express')->where($sql_where)->update($sql_update);
            if($r1 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 编辑物流失败功！';
                $this->Log($Log_content);
                $message = Lang("express.5");
                return output(400,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 编辑物流成功！';
                $this->Log($Log_content);
                $message = Lang("Success");
                return output(200,$message);
            }
        }
        else
        {
            $message = Lang("Parameter error");
            return output(400,$message);
        }
    }
    
    // 删除物流
    public function expressDel()
    {   
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $ids = trim($this->request->param('ids')); // 物流ID
        $id_list = explode(',',$ids);

        $admin_name = $admin_list['name'];
        Db::startTrans();
        if($id_list != array())
        {
            foreach($id_list as $k => $v)
            {
                $sql_where = array('id'=>$v);
                $sql_update = array('recycle'=>1);
                $r = Db::name('express')->where($sql_where)->update($sql_update);
                if($r < 1)
                {
                    Db::rollback();
                    $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . '删除物流失败！参数：'.json_encode($sql_where);
                    $this->Log($Log_content);
                    $message = Lang("express.6");
                    return output(400,$message);
                }
            }
        }
        Db::commit();
        $Log_content = __METHOD__ . '->' . __LINE__ . $admin_name . ' 删除物流成功！参数：'.$ids;
        $this->Log($Log_content);
        $message = Lang("Success");
        return output(200,$message);
    }
    
    // 物流列表
    public function logistics_list()
    {   
        $store_id = trim($this->request->param('storeId'));
        $name = addslashes(trim($this->request->param('name'))); // 物流公司名称、编码
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据

        $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID

        $array = array('store_id'=>$store_id,'mch_id'=>$SelfOperatedStore_id,'name'=>$name,'page'=>$page,'pagesize'=>$pagesize);

        $Express = new ExpressPublicMethod();
        $Express_list = $Express->logistics_list($array);

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }

    // 获取物流主表数据
    public function get_logistics()
    {   
        $store_id = trim($this->request->param('storeId'));

        $id = trim($this->request->param('id')); // id

        $sql0 = "select shop_id from lkt_admin where store_id = '$store_id' and type = 1 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $mch_id = $r0[0]['shop_id'];
        }
        
        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
        $Express = new ExpressPublicMethod();
        $Express_list = $Express->get_logistics($array);

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }

    // 添加/编辑快递公司子表
    public function add_logistics()
    {   
        $store_id = trim($this->request->param('storeId'));
        $id = addslashes(trim($this->request->param('id'))); // ID
        $express_id = addslashes(trim($this->request->param('express_id'))); // 主表ID
        $partnerId = addslashes(trim($this->request->param('partnerId'))); // 电子面单客户账户或月结账号
        $partnerKey = addslashes(trim($this->request->param('partnerKey'))); // 电子面单密码
        $partnerSecret = addslashes(trim($this->request->param('partnerSecret'))); // 电子面单密钥
        $partnerName = addslashes(trim($this->request->param('partnerName'))); // 电子面单客户账户名称
        $net = addslashes(trim($this->request->param('net'))); // 收件网点名称
        $code = addslashes(trim($this->request->param('code'))); // 电子面单承载编号
        $checkMan = addslashes(trim($this->request->param('checkMan'))); // 电子面单承载快递员名

        $admin_name = $this->user_list['name'];

        $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID

        $Express = new ExpressPublicMethod();

        if($id != 0 && $id != '')
        {
            $array = array('store_id'=>$store_id,'mch_id'=>$SelfOperatedStore_id,'express_id'=>$express_id,'partnerId'=>$partnerId,'partnerKey'=>$partnerKey,'partnerSecret'=>$partnerSecret,'partnerName'=>$partnerName,'net'=>$net,'code'=>$code,'checkMan'=>$checkMan,'source'=>1,'admin_name'=>$admin_name,'id'=>$id);

            $Express_list = $Express->edit_logistics($array);
        }
        else 
        {
            $array = array('store_id'=>$store_id,'mch_id'=>$SelfOperatedStore_id,'express_id'=>$express_id,'partnerId'=>$partnerId,'partnerKey'=>$partnerKey,'partnerSecret'=>$partnerSecret,'partnerName'=>$partnerName,'net'=>$net,'code'=>$code,'checkMan'=>$checkMan,'source'=>1,'admin_name'=>$admin_name);

            $Express_list = $Express->add_logistics($array);
        }

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }
    
    // 编辑快递公司子表页面
    public function edit_logistics_page()
    {   
        $store_id = trim($this->request->param('storeId'));
        $id = addslashes(trim($this->request->param('id'))); // ID

        $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID

        $array = array('store_id'=>$store_id,'mch_id'=>$SelfOperatedStore_id,'id'=>$id);

        $Express = new ExpressPublicMethod();
        $Express_list = $Express->edit_logistics_page($array);

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }

    // 删除快递公司子表
    public function del_logistics()
    {   
        $store_id = trim($this->request->param('storeId'));
        $id = addslashes(trim($this->request->param('id'))); // ID
        $admin_name = $this->user_list['name'];

        $SelfOperatedStore_id = PC_Tools::SelfOperatedStore($store_id); // 自营店ID

        $array = array('store_id'=>$store_id,'mch_id'=>$SelfOperatedStore_id,'id'=>$id,'source'=>1,'admin_name'=>$admin_name);

        $Express = new ExpressPublicMethod();
        $Express_list = $Express->del_logistics($array);

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/express.log",$Log_content);
        return;
    }
}