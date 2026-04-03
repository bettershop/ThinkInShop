<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\GETUI\LaikePushTools;
use app\common\ExcelUtils;
use PhpOffice\PhpSpreadsheet\IOFactory;

use app\admin\model\AdminModel;
use app\admin\model\ConfigModel;
use app\admin\model\UserModel;
use app\admin\model\UserGradeModel;
use app\admin\model\UserDistributionModel;
use app\admin\model\UserRuleModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\ReturnRecordModel;
use app\admin\model\MchModel;
use app\admin\model\RecordModel;
use app\admin\model\PrizeConfigModel;
use app\admin\model\PrizePayrecordModel;
use app\admin\model\SignRecordModel;
use app\admin\model\FinanceConfigModel;
use app\admin\model\WithdrawModel;
use app\admin\model\UserAddressModel;
use app\admin\model\UserCollectionModel;
/**
 * 功能：语种管理
 * 修改人：DHB
 */
class Lang extends BaseController
{
    // 语种列表
    public function index()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id'))); // 语种ID
        $lang_name = addslashes(trim($this->request->param('lang_name'))); // 语种名称
        $type = addslashes(trim($this->request->param('type')));// 
        $page = (int)$this->request->param('pageNo'); // 页码
        $pagesize = (int)$this->request->param('pageSize');
        $pagesize = $pagesize ? $pagesize : '10';

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        
        $condition = " recycle = 1 ";
        if($id != '')
        {
            $condition .= " and id = '$id' ";
        }
        //过滤PC店铺添加的管理员
        if($lang_name != '')
        {
            $condition .= " and lang_name like '%$lang_name%' ";
        }
        
        $total = 0;
        $list = array();

        $sql0 = "select count(id) as total from lkt_lang where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select * from lkt_lang where $condition order by show_num desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $list = $r1;
        }

        $message = Lang("Success");
        return output(200,$message,array('list'=>$list,'total'=>$total));
    }

    // 添加/编辑语种
    public function addOrEditLang()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id'))); // 语种ID
        $lang_name = addslashes(trim($this->request->param('lang_name'))); // 语种名称
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语言编码

        $time = date('Y-m-d H:i:s');
        
        if($id != '')
        {
            $sql0 = "select id from lkt_lang where recycle = 0 and lang_name = '$lang_name' and id != '$id' ";
            $sql1 = "select id from lkt_lang where recycle = 0 and lang_code = '$lang_code' and id != '$id' ";
        }
        else
        {
            $sql0 = "select id from lkt_lang where recycle = 0 and lang_name = '$lang_name' ";
            $sql1 = "select id from lkt_lang where recycle = 0 and lang_code = '$lang_code' ";
        }
        $r0 = Db::query($sql0);
        if($r0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 语种名称重复';
            $lktlog->log("admin/Lang.log",$Log_content);
            $message = Lang("lang.0");
            return output(109,$message);
        }

        $r1 = Db::query($sql1);
        if($r1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 语言编码重复';
            $lktlog->log("admin/Lang.log",$Log_content);
            $message = Lang("lang.1");
            return output(109,$message);
        }

        if($id != '')
        {
            $sql2 = "update lkt_lang set lang_name = '$lang_name',lang_code = '$lang_code',op_time = '$time' where id = '$id' ";
            $r2 = Db::execute($sql2);
            if($r2 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改失败';
                $lktlog->log("admin/Lang.log",$Log_content);
                $message = Lang("lang.2");
                return output(109,$message);
            }
        }
        else
        {
            $sql2 = "insert into lkt_lang (lang_name,lang_code,op_time) value ('$lang_name','$lang_code','$time') ";
            $r2 = Db::execute($sql2);
            if($r2 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加失败';
                $lktlog->log("admin/Lang.log",$Log_content);
                $message = Lang("lang.3");
                return output(109,$message);
            }
        }

        $message = Lang("Success");
        return output(200,$message);
    }

    // 删除语种
    public function delLang()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id'))); // 语种ID

        $time = date('Y-m-d H:i:s');
        
        $sql2 = "update lkt_lang set recycle = 2,op_time = '$time' where id = '$id' ";
        $r2 = Db::execute($sql2);
        if($r2 == -1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除失败';
            $lktlog->log("admin/Lang.log",$Log_content);
            $message = Lang("lang.4");
            return output(109,$message);
        }
        
        $message = Lang("Success");
        return output(200,$message);
    }
}
