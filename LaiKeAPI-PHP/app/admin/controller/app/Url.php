<?php
namespace app\admin\controller\app;

use think\facade\Request;
use app\common\LaiKeLogUtils;

use app\admin\model\ThirdModel;
use app\admin\model\ConfigModel;

class Url
{
    // 请求地址获取接口
    public function geturl()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $language = addslashes(trim(Request::param('language'))); // 语言

        $get = addslashes(trim($_POST['get'])); // 纬度
        // $mch_id = addslashes(trim($_POST['mch_id'])); // 店铺ID

        $lktlog = new LaiKeLogUtils();

        $APP_INDEX_KEY = LAIKE_REDIS_PRE_KEY.__CLASS__.'_'.__METHOD__.$get;
        $redis_data = cache($APP_INDEX_KEY);
        $lktlog->log("common/a_redis.log", __LINE__ . $redis_data);
        if($redis_data)
        {
            $message = Lang('Success');
            $data = json_decode($redis_data);
            return output(200,$message,$data);
        }

        $r = ThirdModel::where('id',1)->field($get)->select()->toArray();
        if ($r)
        {
            if (!empty($r[0]['kefu_url']))
            {   
                // if($mch_id != '')
                // {
                //     $r[0]['kefu_url'] = htmlspecialchars_decode($r[0]['kefu_url']) . '&store_id=' . $store_id . '&access_id=' . $access_id . '&language=' . $language. '&mch_id=' . $mch_id;
                // }
                // else
                // {
                    $r[0]['kefu_url'] = htmlspecialchars_decode($r[0]['kefu_url']) . '&store_id=' . $store_id . '&access_id=' . $access_id . '&language=' . $language;
                // }
            }
            else if (!empty($r[0]['mini_url']))
            {
                $r[0]['mini_url'] = htmlspecialchars_decode($r[0]['mini_url']) . '?store_id=' . $store_id;
            }

            if (strpos($get, 'H5'))
            {
                $rr = ConfigModel::where('store_id',$store_id)->field('H5_domain')->select()->toArray();
                if (!empty($rr[0]['H5_domain']))
                {
                    $r[0]['H5'] = $rr[0]['H5_domain'];
                }
            }
            
            cache($APP_INDEX_KEY, json_encode($r[0]), 3600);
            $lktlog->log("common/a_redis.log", __LINE__ . cache($APP_INDEX_KEY));

            $message = Lang('Success');
            return output(200,$message,$r[0]);
        }
        else
        {
            $message = Lang('Not_configured');
            return output(404,$message);
        }
    }
}

?>