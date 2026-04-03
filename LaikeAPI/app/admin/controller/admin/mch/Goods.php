<?php
namespace app\admin\controller\admin\mch;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;

/**
 * 功能：店铺设置
 * 修改人：DHB
 */
class Goods extends BaseController
{
    // 获取语种
    public function getLangs()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $language = addslashes(trim($this->request->param('language')));
        $language = Tools::get_lang($language);

        $store_langs = "";
        $sql0 = "select store_langs from lkt_customer where id = '$store_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $store_langs = $r0[0]['store_langs'];
        }

        if($store_langs != '')
        {
            $sql = "select lang_code,lang_name from lkt_lang where is_show = 1 and recycle = 1 and id in ($store_langs)";
        }
        else
        {
            $sql = "select lang_code,lang_name from lkt_lang where is_show = 1 and recycle = 1 ";
        }
        $r = Db::query($sql);
        if($r)
        {
            $data = $r;
        }

        $message = Lang('Success');
        return output(200, $message,$data);
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/mch/goods.Home",$Log_content);
        return;
    }
}

