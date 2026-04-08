<?php
namespace app\admin\controller\saas;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;

/**
 * 功能：公共工具
 * 修改人：DHB
 */
class PublicTools
{
    // 数据名称列表
    public function getDictionaryCatalogInfo()
    {
        $store_id = addslashes(Request::param('storeId'));

        $keyword = addslashes(trim(Request::param('keyword'))); // 数据名称

        $array = array('store_id'=>$store_id,'keyword'=>$keyword);
        Tools::is_sensitive_words($array);

        $message = Lang('Success');
        echo json_encode(array('code' => 200,  'message' => $message));
        exit();
    }
}
