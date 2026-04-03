<?php
namespace app\admin\controller\app;

use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\JSSDKS;

class Jssdk
{
    // h5授权信息接口
    public function getData()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id
        $language = trim(Request::param('language')); // 语言
        $url = trim(Request::param('url'));

        $jssdk = new JSSDKS($store_id,$url);
        $a = $jssdk->getSignPackage();

        $message = Lang('Success');
        return output(200,$message, $a);
    }
}

?>