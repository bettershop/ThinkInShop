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
class Home extends BaseController
{
    // 店铺信息
    public function select_language()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $language = addslashes(trim($this->request->param('language')));
        $language = Tools::get_lang($language);
        $res = cache($access_id);
        $user_id = $res['user_id'];
        
        $sql0 = "update lkt_user set lang = '$language' where user_id = '$user_id' ";
        $r0 = Db::execute($sql0);
        if($r0 == -1)
        {
            $message = Lang('Modification failed');
            return output(109, $message);
        }
        else
        {
            $message = Lang('Success');
            return output('200', $message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/mch/set.Home",$Log_content);
        return;
    }
}

