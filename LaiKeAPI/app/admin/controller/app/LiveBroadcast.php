<?php
namespace app\admin\controller\app;
use think\facade\Db;
use think\facade\Request;

use app\admin\model\ConfigModel;

class LiveBroadcast
{
    // 接口文档地址
    // https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/livebroadcast/studio-management/getLiveInfo.html

    // 直播列表
    public function getLiveList()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id

        $start = trim(Request::param('start')); // 起始拉取视频，0表示从第一个视频片段开始拉取
        $limit = trim(Request::param('limit')); // 每次拉取的数量，建议100以内

        // 1.当前时间戳
        $currentTime = time();
        // 2.修改文件时间
        $fileName = "../app/common/accessToken"; // 文件名
        if (is_file($fileName))
        {
            $modifyTime = filemtime($fileName);
            if (($currentTime - $modifyTime) < 7200)
            {
                // 可用, 直接读取文件的内容
                $accessToken = file_get_contents($fileName);
            }
            else
            {
                $r = ConfigModel::where(['store_id'=>$store_id])->select()->toArray();
                if ($r)
                {
                    $appid = $r[0]['appid']; // 小程序唯一标识
                    $appsecret = $r[0]['appsecret']; // 小程序的 app secret
                    $url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" . $appid . "&secret=" . $appsecret;
                    // 重新发送请求
                    $result = $this->httpsRequest($url);
                    $jsonArray = json_decode($result, true);
                    // 写入文件
                    $accessToken = $jsonArray['access_token'];
                }
            }
        }
        $url = 'https://api.weixin.qq.com/wxa/business/getliveinfo?access_token=' . $accessToken;
        $data = array();
        $data['start'] = $start;
        $data['limit'] = $limit;
        $data = json_encode($data);
        $res = $this->httpsRequest($url, $data);
        $list = (array)json_decode($res);
        if ($list['errcode'] != 0)
        {
            $message = Lang('Failed_to_get_data');
            return output(400,$message);
        }
        else
        {
            $resArr = array('list' => $list);
            $message = Lang('Success');
            return output(200,$message, $resArr);
        }
    }

    // 直播回放
    public function getLiveHistory()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id

        $start = trim(Request::param('start')); // 起始拉取视频，0表示从第一个视频片段开始拉取
        $limit = trim(Request::param('limit')); // 每次拉取的数量，建议100以内
        $roomid = trim(Request::param('roomid')); // 直播间ID

        // 1.当前时间戳
        $currentTime = time();
        // 2.修改文件时间
        $fileName = "../app/common/accessToken"; // 文件名
        if (is_file($fileName))
        {
            $modifyTime = filemtime($fileName);
            if (($currentTime - $modifyTime) < 7200)
            {
                // 可用, 直接读取文件的内容
                $accessToken = file_get_contents($fileName);
            }
            else
            {
                $r = ConfigModel::where(['store_id'=>$store_id])->select()->toArray();
                if ($r)
                {
                    $appid = $r[0]['appid']; // 小程序唯一标识
                    $appsecret = $r[0]['appsecret']; // 小程序的 app secret
                    $url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" . $appid . "&secret=" . $appsecret;
                    // 重新发送请求
                    $result = $this->httpsRequest($url);
                    $jsonArray = json_decode($result, true);
                    // 写入文件
                    $accessToken = $jsonArray['access_token'];
                }
            }
        }
        $url = 'https://api.weixin.qq.com/wxa/business/getliveinfo?access_token=' . $accessToken;
        $data = array();
        $data['start'] = $start;
        $data['limit'] = $limit;
        $data['action'] = "get_replay";
        $data['room_id'] = $roomid;
        $data = json_encode($data);
        $res = $this->httpsRequest($url, $data);
        $list = (array)json_decode($res);
        if ($list['errcode'] != 0)
        {
            $message = Lang('Failed_to_get_data');
            return output(400,$message);
        }
        else
        {
            $resArr = array('list' => $list);
            $message = Lang('Success');
            return output(200,$message, $resArr);
        }
    }

    public function httpsRequest($url, $data = null)
    {
        // 1.初始化会话
        $ch = curl_init();
        // 2.设置参数: url + header + 选项
        // 设置请求的url
        curl_setopt($ch, CURLOPT_URL, $url);
        // 保证返回成功的结果是服务器的结果
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        if (!empty($data))
        {
            // 发送post请求
            curl_setopt($ch, CURLOPT_POST, 1);
            // 设置发送post请求参数数据
            curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
        }
        // 3.执行会话; $result是微信服务器返回的JSON字符串
        $result = curl_exec($ch);
        // 4.关闭会话
        curl_close($ch);
        return $result;
    }

}

?>