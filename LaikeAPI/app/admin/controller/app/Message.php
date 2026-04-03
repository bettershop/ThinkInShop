<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\Tools;
use app\common\LaiKeLogUtils;

use app\admin\model\NoticeModel;
use app\admin\model\SystemMessageModel;
/**
 * 功能：移动端用户站内信
 * 修改人：PJY
 */
class Message extends BaseController
{
	
    /**
     * 获取微信模板
     */
    public function getWXTemplates()
    {
        $store_id = trim($this->request->param('store_id'));
        $res = NoticeModel::where('store_id',$store_id)->select()->toArray();
        if ($res)
        {
            $res = $res[0];
            $wxtmpids = array();
            if (isset($res['pay_success']))
            {
                $wxtmpids[] = $res['pay_success'];
            }
            if (isset($res['order_delivery']))
            {
                $wxtmpids[] = $res['order_delivery'];
            }
            if (isset($res['refund_res']))
            {
                $wxtmpids[] = $res['refund_res'];
            }
            $message = Lang('Success');
            return output(200, $message,$wxtmpids);
        }
        else
        {
            $message = Lang('message.0');
            return output(ERROR_CODE_HQWXMBSBWLYC, $message);
        }
        exit();
    }

    // 消息列表
    public function index()
    {
        $store_id = trim($this->request->param('store_id'));
        $user_id = $this->user_list['user_id'];//会员ID

	    // 根据用户id,查询消息
	    $r = SystemMessageModel::where(['store_id'=>$store_id,'recipientid'=>$user_id])->limit(0,10)->order('time','desc')->select()->toArray();
	    //查询未读数量
	    $r01 = SystemMessageModel::where(['store_id'=>$store_id,'recipientid'=>$user_id,'type'=>1])->count();
	    //查询消息总数
	    $r02 = SystemMessageModel::where(['store_id'=>$store_id,'recipientid'=>$user_id])->count();

	    $data = array('message' => $r, 'noread' => $r01, 'total' => $r02);
	    $message = Lang('Success');
	    return output(200, $message, $data);
    }

    // 后续加载
    public function more()
    {
        $store_id = trim($this->request->param('store_id'));
        $page = intval(trim($this->request->post('page'))); // 页数
        $user_id = $this->user_list['user_id'];//会员ID
        $pagesize = 10;
        $start = $page * $pagesize;

        // 根据用户id,查询消息
        $r = SystemMessageModel::where(['store_id'=>$store_id,'recipientid'=>$user_id])->limit($start,$pagesize)->order('time','desc')->select()->toArray();

        $data = array('message' => $r);
        $message = Lang('Success');
        return output(200, $message, $data);
    }

    // 消息详情
    public function oneindex()
    {
        $store_id = trim($this->request->param('store_id'));
        $user_id = $this->user_list['user_id'];//会员ID
        $id = $this->request->post('id'); //消息ID

        // 根据用户id,查询消息表
        $r = SystemMessageModel::where(['store_id'=>$store_id,'id'=>$id])->select()->toArray();
        // 修改消息为已读状态
        $sql001 = SystemMessageModel::where(['store_id'=>$store_id,'id'=>$id])->find();
        $sql001->type = 2;
        $sql001->save();
        $data = array('message' => $r);
        $message = Lang('Success');
        return output(200, $message, $data);
    }

    // 全部已读
    public function all()
    {
        $store_id = trim($this->request->param('store_id'));
        $user_id = $this->user_list['user_id'];//会员ID

        // 根据用户id,查询地址表
        $r = SystemMessageModel::where(['store_id'=>$store_id,'recipientid'=>$user_id])->select()->toArray();
        // 修改消息为已读状态
        SystemMessageModel::update(['type'=>2],['store_id'=>$store_id,'recipientid'=>$user_id]);
        $data = array('message' => $r);
        $message = Lang('Success');
        return output(200, $message, $data);
    }

    // 删除
    public function del()
    {
        $store_id = trim($this->request->param('store_id'));
        $id = trim($this->request->post('id')); // 消息ID

        $user_id = $this->user_list['user_id'];//会员ID

        if($id == '')
        {
            $sql = "delete from lkt_system_message where store_id = '$store_id' and recipientid = '$user_id' ";
            $r = Db::execute($sql);
        }
        else
        {
            // ID字符串转数组
            $ids = explode(',', $id);
            foreach ($ids as $k => $v)
            {// 循环ID
                if (!empty($v))
                {	
                    $v = intval($v);
                    // 删除消息
                    SystemMessageModel::destroy($v);
                }
            }
        }
        
        $message = Lang('Success');
        return output(200, $message);
    }

    // 获取客服未读消息
    public function messageNotReade()
    {
        $store_id = trim($this->request->param('store_id'));
        $access_id = trim($this->request->param('access_id'));

        $mchOnlineMessageNotRead = 0; // 店铺未读客服消息
        $userOnlineMessageNotRead = 0; // 用户未读客服消息

        $sql0 = "select * from lkt_user where store_id = '$store_id' and access_id = '$access_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $user_id = $r0[0]['user_id'];

            $sql2 = "select count(id) as total from lkt_online_message where store_id = '$store_id' and receive_id = '$user_id' and is_read = 0 ";
            $r2 = Db::query($sql2);
            if($r2)
            {
                $userOnlineMessageNotRead = $r2[0]['total'];
            }

            $sql1 = "select * from lkt_mch where store_id = '$store_id' and user_id = '$user_id' ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $mch_id = $r1[0]['id'];

                $sql3 = "select count(id) as total from lkt_online_message where store_id = '$store_id' and receive_id = '$mch_id' and is_read = 0 ";
                $r3 = Db::query($sql3);
                if($r3)
                {
                    $mchOnlineMessageNotRead = $r3[0]['total'];
                }
            }
        }

        $data = array('mchOnlineMessageNotRead'=>$mchOnlineMessageNotRead,'userOnlineMessageNotRead'=>$userOnlineMessageNotRead);
        $message = Lang('Success');
        return output(200, $message);
    }
}