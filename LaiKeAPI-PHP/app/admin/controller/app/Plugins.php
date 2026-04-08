<?php
namespace app\admin\controller\app;

use think\facade\Db;
use think\facade\Request;
use app\common\Plugin\Plugin;
use app\common\LaiKeLogUtils;

class Plugins
{
    // 获取移动端店铺里面插件数据
    public function getPluginAll()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id

        $Plugin_arr = array('auction' => false,'coupon' => false,'distribution' => false,'diy' => false,'integral' => false,'mch' => false,'member' => false,'presell' => false,'seconds' => false,'sigin' => false,'wallet' => false,'go_group' => false,'living'=>false,'mch_is_open'=>0,'flashsale'=>false);

        $Plugin = new Plugin();
        $Plugin_arr1 = $Plugin->front_Plugin($store_id);
        foreach ($Plugin_arr1 as $k => $v)
        {
            if ($k == 'MchPublicMethod' && $v == 1)
            {
                $Plugin_arr['mch'] = true;
            }
            else if ($k == 'CouponPublicMethod' && $v == 1)
            {
                $Plugin_arr['coupon'] = true;
            }
            else if ($k == 'Auction' && $v == 1)
            {
                $Plugin_arr['auction'] = true;
            }
            else if ($k == 'Go_groupPublicMethod' && $v == 1)
            {
                $Plugin_arr['go_group'] = true;
            }
            else if ($k == 'Presell_order' && $v == 1)
            {
                $Plugin_arr['presell'] = true;
            }
            else if ($k == 'IntegralPublicMethod' && $v == 1)
            {
                $Plugin_arr['integral'] = true;
            }
            else if ($k == 'SecondsPublic' && $v == 1)
            {
                $Plugin_arr['seconds'] = true;
            }
            else if ($k == 'FlashSalePublic' && $v == 1)
            {
                $Plugin_arr['flashsale'] = true;
            }
            else if ($k == 'LivingPublic' && $v == 1)
            {
                $Plugin_arr['living'] = true;
                $sql_living = "select mch_is_open from lkt_living_config where store_id = '$store_id' ";
                $r_living = Db::query($sql_living);
                if($r_living)
                {
                    $Plugin_arr['mch_is_open'] = $r_living[0]['mch_is_open'];
                }
            }
        }
        $data = $Plugin_arr;
        $message = Lang('Success');
        return output(200,$message, $data);
    }


    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/Plugins.log",$Log_content);
        return;
    }
}

?>