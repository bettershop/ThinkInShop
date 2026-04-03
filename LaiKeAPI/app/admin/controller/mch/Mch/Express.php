<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cache;
use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\ExpressPublicMethod;
use app\common\PC_Tools;

use app\admin\model\MchModel;

class Express extends BaseController
{
    // 物流列表
    public function logistics_list()
    {   
        $store_id = trim($this->request->param('storeId'));
        $name = addslashes(trim($this->request->param('name'))); // 物流公司名称、编码
        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页显示多少条数据

        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'name'=>$name,'page'=>$page,'pagesize'=>$pagesize);

        $Express = new ExpressPublicMethod();
        $Express_list = $Express->logistics_list($array);

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }

    // 获取物流主表数据
    public function get_logistics()
    {   
        $store_id = trim($this->request->param('storeId'));
        $access_id = trim($this->request->param('accessId'));
        
        $id = trim($this->request->param('id')); // id

        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

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
        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $Express = new ExpressPublicMethod();

        if($id != 0 && $id != '')
        {
            $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'express_id'=>$express_id,'partnerId'=>$partnerId,'partnerKey'=>$partnerKey,'partnerSecret'=>$partnerSecret,'partnerName'=>$partnerName,'net'=>$net,'code'=>$code,'checkMan'=>$checkMan,'source'=>2,'admin_name'=>$admin_name,'id'=>$id);

            $Express_list = $Express->edit_logistics($array);
        }
        else 
        {
            $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'express_id'=>$express_id,'partnerId'=>$partnerId,'partnerKey'=>$partnerKey,'partnerSecret'=>$partnerSecret,'partnerName'=>$partnerName,'net'=>$net,'code'=>$code,'checkMan'=>$checkMan,'source'=>2,'admin_name'=>$admin_name);

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

        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);

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
        $user_id = $this->user_list['user_id'];
        $r_mch = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'recovery'=>0])->field('id')->select()->toArray();
        $mch_id = $r_mch[0]['id']; // 店铺id

        $array = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id,'source'=>2,'admin_name'=>$admin_name);

        $Express = new ExpressPublicMethod();
        $Express_list = $Express->del_logistics($array);

        $message = Lang("Success");
        return output(200,$message,$Express_list);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("mch/Express.log",$Log_content);
        return;
    }
}
