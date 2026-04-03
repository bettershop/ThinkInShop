<?php
namespace app\common\third\logistics;

use app\admin\model\ConfigModel;

// require_once MO_LIB_DIR . '/third/html/HtmlTool.class.php';
require_once '../app/common/third/httpClient/HttpTools.class.php';
require_once "logistics_config.php";

/**
 * 物流信息工具类
 */
class LogisticsTool
{
    //开启
    const KD_OPEN = 1;
    //关闭
    const KD_CLOSE = 0;

    /**
     * 获取物流信息
     */
    public static function getLogistics($type, $courier_num,$store_id,$tel = '')
    {
        // $is_express = 0;
        $express_address = '';
        $express_number = '';
        $express_key = '';
        // $express_tel = '';
        $express_array = array();

        $r_config = ConfigModel::where(['store_id'=>$store_id])
                                ->field('is_express,express_address,express_number,express_key,express_tel')
                                ->select()
                                ->toArray();
        if($r_config)
        {
            // $is_express = $r_config[0]['is_express'];
            $express_address = $r_config[0]['express_address'];
            $express_number = $r_config[0]['express_number'];
            $express_key = $r_config[0]['express_key'];
            // $express_tel = $r_config[0]['express_tel'];
        }

        $res_1 = array();
        if (trim($type) == 'linexsolutions')
        {
            $awbTrackingUrl = "http://eexpress.linexsolutions.com/awb/awbTracking?sno=" . $courier_num;
            $html = new HtmlTool($awbTrackingUrl);
            $resultArr = $html->getAwbTracingResult();
            if (!empty($resultArr))
            {
                $res_1 = json_decode(json_encode($resultArr));
            }
        }
        else
        {
            /*
             * http://poll.kuaidi100.com/poll/query.do?customer=96EB664A39B315C5C81294793F7EEC19
             * &sign=4621FBADF161FC4B26DC775B76EAEABA&
             * param={"com":"yuantong","num":"1111111111111","from":"","phone":"","to":"","resultv2":0}
             */
            if($type == 'shunfeng')
            {
                // $phone = $express_tel;
                $phone = $tel;
            }
            else
            {
                $phone = '';
            }

            $res = null;
            // if ($is_express == self::KD_OPEN)
            // {
                $url = $express_address; //实时查询
                $key = $express_key; //客户授权key
                $customer = $express_number; //查询公司编号
                $param = array(
                    'com' => $type, //快递公司编码
                    'num' => $courier_num, //快递单号
                    'phone' => $phone, //手机号
                    'from' => '', //出发地城市
                    'to' => '', //目的地城市
                    'resultv2' => '1', //开启行政区域解析
                );

                //请求参数
                $post_data = array();
                $post_data["customer"] = $customer;
                $post_data["param"] = json_encode($param);
                $sign = md5($post_data["param"] . $key . $post_data["customer"]);
                $post_data["sign"] = strtoupper($sign);
                $params = "";
                foreach ($post_data as $k => $v)
                {
                    $params .= "$k=" . urlencode($v) . "&"; //默认UTF-8编码格式
                }
                $post_data = substr($params, 0, -1);

                $res = app('HttpTools')::httpsRequest($url, $post_data);

                // $res = HttpTools::httpsRequest($url, $post_data);
            // }
            // else if ($is_express == self::KD_CLOSE)
            // {
            //     $url = "http://www.kuaidi100.com/query?type=$type&postid=$courier_num";
            //     // $res = HttpTools::httpsRequest($url);
            //     $res = app('HttpTools')::httpsRequest($url);

            // }
            if (!empty($res))
            {
                $retJson = json_decode($res);
                if ($retJson->message == 'ok')
                {
                    $res_1 = json_decode($res)->data;
                }
            }

        }

        return $res_1;
    }

}
