<?php
namespace app\common;
use app\admin\model\ConfigModel;
use app\common;
use think\facade\Db;

use app\common\LaiKeLogUtils;

class WebHook
{
    public static function SendRequest($number,$store_id,$express_id = 0,$mobile = '')
    {
        $lktlog = new LaiKeLogUtils();

        $url = self::set_url();
        $carrier = '';
        if($express_id != 0)
        {
            $sql0 = "select type from lkt_express where id = '$express_id'";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $carrier = $r0[0]['type']; // 快递公司代码
            }
        }

        $data = array(
            array(
                'number'=>$number,
                'carrier'=>$carrier,
                // 'phone_number_last_4'=>'1130'
            )
        );

        $r_config = ConfigModel::where(['store_id'=>$store_id])
            ->field('track_secret')
            ->select()
            ->toArray();
        $header = self::set_header($r_config[0]['track_secret']);

        $lktlog->log("common/WebHook.log",__METHOD__ . ":" . __LINE__ . "发货传参:" . json_encode($data));
        $WebHook_str = WebHook::https_post($url, json_encode($data), 1, $header);
        $WebHook_list = json_decode($WebHook_str,true);
        $lktlog->log("common/WebHook.log",__METHOD__ . ":" . __LINE__ . "发货:" . $WebHook_str);
        if($WebHook_list['code'] == 0)
        {
            if(isset($WebHook_list['data']['rejected'][0]['error']))
            {
                echo json_encode(array('code' => $WebHook_list['data']['rejected'][0]['error']['code'], 'message' => $WebHook_list['data']['rejected'][0]['error']['message']));
                exit;
            }
            else if(isset($WebHook_list['data']['errors']))
            {
                echo json_encode(array('code' => $WebHook_list['data']['errors'][0]['code'], 'message' => $WebHook_list['data']['errors'][0]['message']));
                exit;
            }
        }

        return;
    }

    public static function set_url()
    {
        $url = 'https://api.17track.net/track/v2.4/register';
        return $url;
    }

    public static function set_header($track_secret)
    {
        $header = array(
            "17token: ".$track_secret,
            "Content-Type:application/json"
        );
        return $header;
    }

    public static function https_post($url, $data, $type = 1, $header = null)
    {
        $curl = curl_init();
        
        curl_setopt_array($curl, [
            CURLOPT_URL => $url,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_FOLLOWLOCATION => true,
            CURLOPT_ENCODING => "",
            CURLOPT_MAXREDIRS => 10,
            CURLOPT_TIMEOUT => 30,
            CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
            CURLOPT_CUSTOMREQUEST => "POST",
            CURLOPT_POSTFIELDS =>$data,
            CURLOPT_HTTPHEADER => $header,
        ]);

        $response = curl_exec($curl);
        $err = curl_error($curl);

        curl_close($curl);

        if ($err) 
        {
            echo json_encode(array('code' => 109, 'message' => "cURL Error #:" . $err));
            exit;
        } 
        else 
        {
            return $response;
        }
    }
}

?>