<?php
namespace app\admin\controller\app;

use app\BaseController;
use think\facade\Db;
use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\Common_Tools;
use app\common\Tools;

class Common extends BaseController
{
    // 获取商城语种
    public function getLangs()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $data = Common_Tools::get_lang($store_id);

        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取商城币种
    public function getCurrencys()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $array = array('store_id'=>$store_id,'type'=>0,'id'=>0);
        $data = Common_Tools::get_store_currency($array);

        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取商城默认币种
    public function getStoreDefaultCurrency()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id 
        $array = array('store_id'=>$store_id,'type'=>1,'id'=>0);
        
        $data = Common_Tools::get_store_currency($array);
// var_dump($data);die;
        $message = Lang('Success');
        return output(200,$message, $data[0]);
    }

    // 获取国家列表
    public function getCountry()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $data = Common_Tools::get_country($store_id);

        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 更改用户默认币种
    public function userChangeCurrency()
    {
        $store_id = trim(Request::param('store_id'));
        $store_type = trim(Request::param('store_type')); // 来源
        $access_id = trim(Request::param('access_id')); // 授权id

        $currency_id = trim(Request::param('currency_id')); // 币种ID

        $sql0 = "select id from lkt_user where store_id = '$store_id' and access_id = '$access_id'";
        $r0 = Db::query($sql0);
        if ($r0)
        {
            $sql1 = "update lkt_user set preferred_currency = '$currency_id' where store_id = '$store_id' and access_id = '$access_id' ";
            $r1 = Db::execute($sql1);

            cache($access_id . '_currency',$currency_id); // 修改缓存
        }

        $message = Lang("Success");
        return output(200,$message);
    }

    // 获取商城默认币种和语种
    public function getStoreDefaultI18n()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $array = array('store_id'=>$store_id,'type'=>1,'id'=>0);
        $storeDefaultCurrency = Common_Tools::get_store_currency($array);

        $array = array('store_id'=>$store_id);
        $storeDefaultLanguage = Common_Tools::get_store_lang($array);

        $data = array('storeId'=>$store_id,'storeDefaultCurrency'=>$storeDefaultCurrency[0],'storeDefaultLanguage'=>$storeDefaultLanguage[0]);
        $message = Lang('Success');
        return output(200,$message, $data);
    }

    // 获取用户货币信息
    public function fetchUserCurrencyInfo()
    {
        $store_id = addslashes(Request::param('store_id'));
        $store_type = addslashes(Request::param('store_type')); // 来源
        $access_id = addslashes(Request::param('access_id')); // 授权id

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种

        $array = array('store_id'=>$store_id,'type'=>0,'id'=>$currency_id);
        $data = Common_Tools::get_store_currency($array);

        $message = Lang('Success');
        return output(200,$message, $data[0]);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/common.log",$Log_content);

        return;
    }
}

?>