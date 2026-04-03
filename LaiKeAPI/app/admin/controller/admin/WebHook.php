<?php
namespace app\admin\controller\admin;
session_name("money_mojavi");
session_start();
date_default_timezone_set('Asia/Chongqing');
set_time_limit(0);
use app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;

header("Content-type:text/html;charset=utf-8");

class WebHook
{
    public function apply()
    {
        $lktlog = new LaiKeLogUtils();
        $log_name = 'common/WebHook.log';
        if(isset($_SESSION))
        {
            $lktlog->log($log_name, __LINE__ . "1:" . json_encode($_SESSION));
        }
        if(isset($_POST))
        {
            $lktlog->log($log_name, __LINE__ . "2:" . json_encode($_POST));
        }
        if(isset($_GET))
        {
            $lktlog->log($log_name, __LINE__ . "3:" . json_encode($_GET));
        }
        if(file_get_contents('php://input') != '')
        {
            $lktlog->log($log_name, __LINE__ . "4:" . json_encode(file_get_contents('php://input')));
        }
        if(isset($GLOBALS['HTTP_RAW_POST_DATA']))
        {
            $lktlog->log($log_name, __LINE__ . "5:" . json_encode($GLOBALS['HTTP_RAW_POST_DATA']));
        }

        $str = file_get_contents('php://input');

        if(preg_match('/\\"/',$str))
        {
            $str = str_ireplace('\\"', '"', $str);
        }

        $list = json_decode($str, true);

        $number = $list['data']['number'];
        $events = $list['data']['track_info']['tracking']['providers'][0]['events'];

        $logistics = json_encode($events);
        $logistics = addslashes($logistics);

        $sql0 = "select id,logistics from lkt_order_details where courier_num like '%$number%' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach ($r0 as $row)
            {
                $order_details_id = $row['id'];
                $logistics0 = $row['logistics'];
                if($logistics0 == '')
                {
                    $logistics1 = $logistics;
                }
                else
                {
                    $logistics1 = $logistics0 . '/-/' . $logistics;
                }

                $sql = "update lkt_order_details set logistics = '$logistics1' where id = '$order_details_id' ";
                Db::execute($sql);

                $sql = "update lkt_express_delivery set logistics = '$logistics' where order_details_id = '$order_details_id' and courier_num like '%$number%' ";
                Db::execute($sql);
            }
        }
        else
        {
            $sql = "update lkt_return_goods set logistics = '$logistics' where express_num like '%$number%' ";
            Db::execute($sql);
            
            $sql = "update lkt_express_delivery set logistics = '$logistics' where courier_num like '%$number%' ";
            Db::execute($sql);
        }

        return;
    }
}
?> /Users/wangxian/php代码/tp-api-20260306/LaikeAPI/app/admin/controller/admin/WebHook.php
