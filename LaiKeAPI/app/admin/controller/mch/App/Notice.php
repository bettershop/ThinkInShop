<?php
namespace app\admin\controller\mch\App;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\GETUI\LaikePushTools;
use app\common\Tools;

class Notice extends BaseController
{   
    // 消息列表
    public function NoticeList()
    {
        $store_id = addslashes(trim($this->request->param('store_id')));
        $storeType = addslashes(trim($this->request->param('store_type')));
        $accessId = addslashes(trim($this->request->param('access_id')));

        $page = addslashes(trim($this->request->post('page'))); // 页码
        $pagesize = addslashes(trim($this->request->post('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : '10';

        $user_id = $this->user_list['user_id'];

        $sql_mch = "select id from lkt_mch where user_id = '$user_id' and recovery = 0 ";
        $r_mch = Db::query($sql_mch);
        $mch_id = $r_mch[0]['id'];
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $total = 0;
        $sql0 = "select count(id) as num from lkt_message_logging where store_id = '$store_id' and mch_id = '$mch_id' and supplier_id = 0 and type in (1,2,3,4,5,6,9,15,16,17,22,23,30) ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        $noread = 0;
        $sql1 = "select count(id) as num from lkt_message_logging where store_id = '$store_id' and mch_id = '$mch_id' and read_or_not = 0 and supplier_id = 0 and type in (1,2,3,4,5,6,9,15,16,17,22,23,30) ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $noread = $r1[0]['num'];
        }
       
        $list = array();
        $sql2 = "select id,type,content,add_date as time,read_or_not from lkt_message_logging where store_id = '$store_id' and mch_id = '$mch_id' and supplier_id = 0 and type in (1,2,3,4,5,6,9,15,16,17,22,23,30) order by read_or_not asc,time desc limit $start,$pagesize";
        $r2 = Db::query($sql2);
        if ($r2)
        {
            foreach ($r2 as $k => $v)
            {
                if($v['type'] == 1)
                {
                    $v['title'] = '订单待发货通知';
                }
                else if($v['type'] == 2)
                {
                    $v['title'] = '售后待处理通知通知';
                }
                else if($v['type'] == 3)
                {
                    $v['title'] = '系统消息';
                }
                else if($v['type'] == 4)
                {
                    $v['title'] = '订单关闭通知';
                }
                else if($v['type'] == 5)
                {
                    $v['title'] = '新订单通知';
                }
                else if($v['type'] == 6)
                {
                    $v['title'] = '订单收货通知';
                }
                else if($v['type'] == 9)
                {
                    $v['title'] = '商品补货通知';
                }
                else if($v['type'] == 15)
                {
                    $v['title'] = '平台违规下架';
                }
                else if($v['type'] == 16)
                {
                    $v['title'] = '保证金审核消息通知';
                }
                else if($v['type'] == 17)
                {
                    $v['title'] = '商品审核消息通知';
                }
                else if($v['type'] == 22)
                {
                    $v['title'] = '用户提交竞拍保证金提醒';
                }
                else if($v['type'] == 23)
                {
                    $v['title'] = '提现申请提交成功';
                }
                else if($v['type'] == 30)
                {
                    $v['title'] = '申请退还保证金';
                }
                $v['type'] = $v['read_or_not'] + 1;
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'message'=>$list,'noread'=>$noread);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 消息详情
    public function NoticeDetails()
    {
        $store_id = addslashes(trim($this->request->param('store_id')));
        $storeType = addslashes(trim($this->request->param('store_type')));
        $accessId = addslashes(trim($this->request->param('access_id')));

        $id = addslashes(trim($this->request->post('id'))); // 消息ID

        $user_id = $this->user_list['user_id'];

        $sql_mch = "select id from lkt_mch where user_id = '$user_id' and recovery = 0 ";
        $r_mch = Db::query($sql_mch);
        $mch_id = $r_mch[0]['id'];

        $list = array();
        $sql0 = "select id,type,content,add_date as time from lkt_message_logging where store_id = '$store_id' and mch_id = '$mch_id' and supplier_id = 0 and id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            if($r0[0]['type'] == 1)
            {
                $r0[0]['title'] = '订单代发货通知';
            }
            else if($r0[0]['type'] == 2)
            {
                $r0[0]['title'] = '售后待处理通知通知';
            }
            else if($r0[0]['type'] == 3)
            {
                $r0[0]['title'] = '系统消息';
            }
            else if($r0[0]['type'] == 4)
            {
                $r0[0][0]['title'] = '订单关闭通知';
            }
            else if($r0[0]['type'] == 5)
            {
                $r0[0]['title'] = '新订单通知';
            }
            else if($r0[0]['type'] == 6)
            {
                $r0[0]['title'] = '订单收货通知';
            }
            else if($r0[0]['type'] == 9)
            {
                $r0[0]['title'] = '商品补货通知';
            }
            else if($r0[0]['type'] == 15)
            {
                $r0[0]['title'] = '平台违规下架';
            }
            else if($r0[0]['type'] == 16)
            {
                $r0[0]['title'] = '保证金审核消息通知';
            }
            else if($r0[0]['type'] == 17)
            {
                $r0[0]['title'] = '商品审核消息通知';
            }
            else if($r0[0]['type'] == 22)
            {
                $r0[0]['title'] = '用户提交竞拍保证金提醒';
            }
            else if($r0[0]['type'] == 23)
            {
                $r0[0]['title'] = '提现申请提交成功';
            }
            else if($r0[0]['type'] == 30)
            {
                $r0[0]['title'] = '申请退还保证金';
            }

            $list = $r0[0];

            $sql1 = "update lkt_message_logging set read_or_not = 1 where store_id = '$store_id' and mch_id = '$mch_id' and read_or_not = 0 and supplier_id = 0 and id = '$id' ";
            $r1 = Db::execute($sql1);

            $content = $r0[0]['content'];
            $add_date = $r0[0]['time'];
            $sql2 = "update lkt_message_logging set read_or_not = 1 where store_id = '$store_id' and mch_id = '$mch_id' and supplier_id = 0 and content = '$content' and add_date = '$add_date' ";
            $r2 = Db::execute($sql2);
        }

        $data = array('message'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }
    
    // 一键已读
    public function AllMessage()
    {
        $store_id = addslashes(trim($this->request->param('store_id')));
        $storeType = addslashes(trim($this->request->param('store_type')));
        $accessId = addslashes(trim($this->request->param('access_id')));

        $user_id = $this->user_list['user_id'];

        $sql_mch = "select id from lkt_mch where user_id = '$user_id' and recovery = 0 ";
        $r_mch = Db::query($sql_mch);
        $mch_id = $r_mch[0]['id'];

        $sql_0 = "select * from lkt_message_logging where store_id = '$store_id' and mch_id = '$mch_id' and read_or_not = 0 and supplier_id = 0 and type in (1,2,3,4,5,6,9,15,16,17,22,23,30) ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            foreach($r_0 as $k => $v)
            {
                $content = $v['content'];
                $add_date = $v['add_date'];

                $sql_2 = "update lkt_message_logging set read_or_not = 1 where store_id = '$store_id' and mch_id = '$mch_id' and supplier_id = 0 and content = '$content' and add_date = '$add_date' ";
                $r_2 = Db::execute($sql_2);
            }
        }

        $sql0 = "update lkt_message_logging set read_or_not = 1 where store_id = '$store_id' and mch_id = '$mch_id' and read_or_not = 0 and supplier_id = 0 and type in (1,2,3,4,5,6,9,15,16,17,22,23,30) ";
        $r0 = Db::execute($sql0);

        $message = Lang('Success');
        return output(200,$message);
    }

    // 删除消息
    public function DelMessage()
    {
        $store_id = addslashes(trim($this->request->param('store_id')));
        $storeType = addslashes(trim($this->request->param('store_type')));
        $accessId = addslashes(trim($this->request->param('access_id')));

        $ids = addslashes(trim($this->request->param('ids')));

        $user_id = $this->user_list['user_id'];

        $sql_mch = "select id from lkt_mch where user_id = '$user_id' and recovery = 0 ";
        $r_mch = Db::query($sql_mch);
        $mch_id = $r_mch[0]['id'];

        if($ids == '')
        {
            $sql0 = "delete from lkt_message_logging where store_id = '$store_id' and mch_id = '$mch_id' and supplier_id = 0 ";
            $r0 = Db::execute($sql0);
        }
        else
        {
            $id = trim($ids,',');
            $sql0 = "delete from lkt_message_logging where store_id = '$store_id' and mch_id = '$mch_id' and supplier_id = 0 and id in ($id) ";
            $r0 = Db::execute($sql0);
        }

        $message = Lang('Success');
        return output(200,$message);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("mch/App/Notice.log",$Log_content);
        return;
    }
}
