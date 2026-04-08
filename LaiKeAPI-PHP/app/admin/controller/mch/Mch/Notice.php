<?php
namespace app\admin\controller\mch\Mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;

class Notice extends BaseController
{   
    // 消息
    public function NoticeList()
    {   
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $accessId = trim($this->request->param('accessId'));

        $mch_id = cache($accessId.'_7');

        // 订单(待发货)
        $message_logging_list_1 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>1,'supplier_id'=>0);
        $total1 = PC_Tools::news_num($message_logging_list_1);
        $list1 = PC_Tools::news_list($message_logging_list_1);
		
        // 订单(售后)
        $message_logging_list_2 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>2,'supplier_id'=>0);
        $total2 = PC_Tools::news_num($message_logging_list_2);
        $list2 = PC_Tools::news_list($message_logging_list_2);

        // 订单(提醒发货)
        $message_logging_list_3 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>3,'supplier_id'=>0);
        $total3 = PC_Tools::news_num($message_logging_list_3);
        $list3 = PC_Tools::news_list($message_logging_list_3);

        // 订单(订单关闭)
        $message_logging_list_4 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>4,'supplier_id'=>0);
        $total4 = PC_Tools::news_num($message_logging_list_4);
        $list4 = PC_Tools::news_list($message_logging_list_4);

        // 订单(新订单)
        $message_logging_list_5 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>5,'supplier_id'=>0);
        $total5 = PC_Tools::news_num($message_logging_list_5);
        $list5 = PC_Tools::news_list($message_logging_list_5);

        // 订单(收货)
        $message_logging_list_6 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>6,'supplier_id'=>0);
        $total6 = PC_Tools::news_num($message_logging_list_6);
        $list6 = PC_Tools::news_list($message_logging_list_6);

        // 商品(补货)
        $message_logging_list_9 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>9,'supplier_id'=>0);
        $total9 = PC_Tools::news_num($message_logging_list_9);
        $list9 = PC_Tools::news_list($message_logging_list_9);

        // 违规下架
        $message_logging_list_15 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>15,'supplier_id'=>0);
        $total15 = PC_Tools::news_num($message_logging_list_15);
        $list15 = PC_Tools::news_list($message_logging_list_15);

        // 店铺商品审核消息通知(PC店铺)
        $message_logging_list_18 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>18,'supplier_id'=>0);
        $total18 = PC_Tools::news_num($message_logging_list_18);
        $list18 = PC_Tools::news_list($message_logging_list_18);

        // pc店铺提现审核消息通知(您的店铺提现申请提交成功，正在等待管理员审核！ + 审核通过 + 审核失败)
        $message_logging_list_24 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>24,'supplier_id'=>0);
        $total24 = PC_Tools::news_num($message_logging_list_24);
        $list24 = PC_Tools::news_list($message_logging_list_24);

        // pc店铺保证金审核消息通知  pc店铺用户提交竞拍保证金提醒
        $message_logging_list_25 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>25,'supplier_id'=>0);
        $total25 = PC_Tools::news_num($message_logging_list_25);
        $list25 = PC_Tools::news_list($message_logging_list_25);

        $total_0 = (int)$total1 + (int)$total2 + (int)$total3 + (int)$total4 + (int)$total5 + (int)$total6;
        $list_0 = array(
            array(
                'type'=>'1',
                'total'=>$total1,
                'list'=>$list1
            ),
            array(
                'type'=>'2',
                'total'=>$total2,
                'list'=>$list2
            ),
            array(
                'type'=>'3',
                'total'=>$total3,
                'list'=>$list3
            ),
            array(
                'type'=>'4',
                'total'=>$total4,
                'list'=>$list4
            ),
            array(
                'type'=>'5',
                'total'=>$total5,
                'list'=>$list5
            ),
            array(
                'type'=>'6',
                'total'=>$total6,
                'list'=>$list6
            )
        );

        $total_1 = (int)$total9 + (int)$total15 + (int)$total18;
        $list_1 = array(
            array(
                'type'=>9,
                'total'=>$total9,
                'list'=>$list9
            ),
            array(
                'type'=>15,
                'total'=>$total15,
                'list'=>$list15
            ),
            array(
                'type'=>18,
                'total'=>$total18,
                'list'=>$list18
            ),
        );
        $total_2 = (int)$total24 + (int)$total25;
        $list_2 = array(
            array(
                'type'=>24,
                'total'=>$total24,
                'list'=>$list24
            ),
            array(
                'type'=>25,
                'total'=>$total25,
                'list'=>$list25
            ),
        );
        $data0 = array(
            array(
                'type'=>"[1, 2, 3, 4, 5, 6]",
                'total'=>$total_0,
                'list'=>$list_0
            ),
            array(
                'type'=>"[9, 15, 18]",
                'total'=>$total_1,
                'list'=>$list_1
            ),
            array(
                'type'=>"[24, 25]",
                'total'=>$total_2,
                'list'=>$list_2
            )
        );

        $mchOnlineMessageNotRead = 0; // 店铺未读客服消息
        $sql3 = "select count(id) as total from lkt_online_message where store_id = '$store_id' and receive_id = '$mch_id' and is_read = 0 ";
        $r3 = Db::query($sql3);
        if($r3)
        {
            $mchOnlineMessageNotRead = $r3[0]['total'];
        }
        
        $data = array('list'=>$data0,'mchOnlineMessageNotRead'=>$mchOnlineMessageNotRead);
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'data' => $data, 'message' => $message));
        exit;
    }

    // 一键已读
    public function NoticeRead()
    {   
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $accessId = trim($this->request->param('accessId'));

        $type = trim($this->request->param('types'));
        $id = trim($this->request->param('id'));

        $mch_id = cache($accessId.'_7');

        $message_logging_list = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>$type,'id'=>$id,'supplier_id'=>0);
        PC_Tools::one_click_read($message_logging_list);
        
        $message = Lang('Success');
        echo json_encode(array('code' => 200,  'message' => $message));
        exit;
    }
}