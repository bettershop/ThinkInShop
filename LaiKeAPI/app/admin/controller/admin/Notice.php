<?php
namespace app\admin\controller\admin;

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
    public function noticeList()
    {   
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $mch_id = cache($access_id.'_'.$store_type);

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

        // 商品(审核)
        $message_logging_list_7 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>7,'supplier_id'=>0);
        $total7 = PC_Tools::news_num($message_logging_list_7);
        $list7 = PC_Tools::news_list($message_logging_list_7);

        // 商品(补货)
        $message_logging_list_9 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>9,'supplier_id'=>0);
        $total9 = PC_Tools::news_num($message_logging_list_9);
        $list9 = PC_Tools::news_list($message_logging_list_9);

        // 用户提现
        $message_logging_list_19 = array('store_id'=>$store_id,'mch_id'=>0,'type'=>19,'supplier_id'=>0);
        $total19 = PC_Tools::news_num($message_logging_list_19);
        $list19 = PC_Tools::news_list($message_logging_list_19);

        // 店铺提现
        $message_logging_list_20 = array('store_id'=>$store_id,'mch_id'=>0,'type'=>20,'supplier_id'=>0);
        $total20 = PC_Tools::news_num($message_logging_list_20);
        $list20 = PC_Tools::news_list($message_logging_list_20);

        // 供应商提现
        $message_logging_list_21 = array('store_id'=>$store_id,'mch_id'=>0,'type'=>21,'supplier_id'=>0);
        $total21 = PC_Tools::news_num($message_logging_list_21);
        $list21 = PC_Tools::news_list($message_logging_list_21);

        // 店铺申请退货保证金
        $message_logging_list_30 = array('store_id'=>$store_id,'mch_id'=>0,'type'=>30,'supplier_id'=>0);
        $total30 = PC_Tools::news_num($message_logging_list_30);
        $list30 = PC_Tools::news_list($message_logging_list_30);

        $total20 = $total20 + $total30;
        $list20 = array_merge($list20, $list30);

        $total_0 = (int)$total1 + (int)$total2 + (int)$total3 + (int)$total4 + (int)$total5 + (int)$total6;
        $list_0 = array(
            array(
                'type'=>1,
                'total'=>$total1,
                'list'=>$list1
            ),
            array(
                'type'=>2,
                'total'=>$total2,
                'list'=>$list2
            ),
            array(
                'type'=>3,
                'total'=>$total3,
                'list'=>$list3
            ),
            array(
                'type'=>4,
                'total'=>$total4,
                'list'=>$list4
            ),
            array(
                'type'=>5,
                'total'=>$total5,
                'list'=>$list5
            ),
            array(
                'type'=>6,
                'total'=>$total6,
                'list'=>$list6
            )
        );

        $total_1 = (int)$total7 + (int)$total9;
        $list_1 = array(
            array(
                'type'=>7,
                'total'=>$total7,
                'list'=>$list7
            ),
            array(
                'type'=>9,
                'total'=>$total9,
                'list'=>$list9
            ),
        );

        $total_2 = (int)$total19 + (int)$total20 + (int)$total21;
        $list_2 = array(
            array(
                'type'=>19,
                'total'=>$total19,
                'list'=>$list19
            ),
            array(
                'type'=>20,
                'total'=>$total20,
                'list'=>$list20
            ),
            array(
                'type'=>21,
                'total'=>$total21,
                'list'=>$list21
            ),
        );

        $data0 = array(
            array(
                'type'=>'[1,2,3,4,5,6]',
                'total'=>$total_0,
                'list'=>$list_0
            ),
            array(
                'type'=>'[7,9]',
                'total'=>$total_1,
                'list'=>$list_1
            ),
            array(
                'type'=>'[19,20,21]',
                'total'=>$total_2,
                'list'=>$list_2
            )
        );
        
        $data = array('list'=>$data0);
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'data' => $data, 'message' => $message));
        exit;
    }

    // 一键已读
    public function noticeRead()
    {   
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = addslashes(trim($this->request->param('accessId')));
        $type = trim($this->request->param('types'));
        $id = trim($this->request->param('id'));

        $mch_id = '0,' . cache($access_id.'_'.$store_type);

        if($id != '')
        {
            $r = Db::name('message_logging')->where(['id'=>$id])->update(['read_or_not'=>1]);
        }
        else
        {
            $message_logging_list = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>$type,'supplier_id'=>0);
            PC_Tools::one_click_read($message_logging_list);
        }
        
        $message = Lang('Success');
        echo json_encode(array('code' => 200,  'message' => $message));
        exit;
    }
}