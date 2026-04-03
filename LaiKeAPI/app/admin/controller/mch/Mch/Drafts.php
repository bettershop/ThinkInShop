<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cache;
use think\facade\Request;
use app\common\ProductDrafts;
use app\common\Plugin\MchPublicMethod;

/**
 * 功能：PC店鋪商品草稿箱类
 * 修改人：DHB
 */
class Drafts extends BaseController
{
    // 草稿箱列表
    public function get_list()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $page = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pagesize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据

        $user_id = $this->user_list['user_id'];
        $mch_id = MchPublicMethod::GetMchID(array('store_id'=>$store_id,'user_id'=>$user_id));

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'supplier_id'=>0,'page'=>$page,'pagesize'=>$pagesize,'source'=>2);
        $Drafts = new ProductDrafts();
        $Drafts->get_list($data);

        return;
    }

    // 添加草稿箱
    public function add()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 草稿箱ID
        $text = addslashes(trim($this->request->param('text'))); // 草稿数据

        $user_id = $this->user_list['user_id'];
        $mch_id = MchPublicMethod::GetMchID(array('store_id'=>$store_id,'user_id'=>$user_id));

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'supplier_id'=>0,'id'=>$id,'text'=>$text,'source'=>2);
        $Drafts = new ProductDrafts();
        $Drafts->add($data);

        return;
    }

    // 编辑页面
    public function edit_page()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 草稿箱ID

        $user_id = $this->user_list['user_id'];
        $mch_id = MchPublicMethod::GetMchID(array('store_id'=>$store_id,'user_id'=>$user_id));

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'supplier_id'=>0,'id'=>$id,'source'=>2);
        $Drafts = new ProductDrafts();
        $Drafts->edit_page($data);

        return;
    }

    // 删除草稿箱
    public function del()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 草稿箱ID

        $user_id = $this->user_list['user_id'];
        $mch_id = MchPublicMethod::GetMchID(array('store_id'=>$store_id,'user_id'=>$user_id));

        $data = array('store_id'=>$store_id,'mch_id'=>$mch_id,'supplier_id'=>0,'id'=>$id,'source'=>2);
        $Drafts = new ProductDrafts();
        $Drafts->del($data);

        return;
    }
}
