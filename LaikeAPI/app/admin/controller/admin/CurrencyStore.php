<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\LaiKeLogUtils;

/**
 * 功能：币种管理
 * 修改人：DHB
 */
class CurrencyStore extends BaseController
{
    // 币种列表
    public function currencyStoreList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $page = addslashes($this->request->param('pageNo'));
        $pagesize = addslashes($this->request->param('pageSize'));
        $pagesize = $pagesize ? $pagesize : '10';
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $total = 0;
        $list = array();
        $haveDefaultCurrency = array();

        $sql0 = "select count(b.id) as total from lkt_currency_store as a left join lkt_currency as b on a.currency_id = b.id where b.recycle = 0 and a.store_id = '$store_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select a.store_id,a.currency_id,a.is_show,a.default_currency,a.exchange_rate,a.recycle,a.update_time,b.currency_code,b.currency_name,b.currency_symbol from lkt_currency_store as a left join lkt_currency as b on a.currency_id = b.id where b.recycle = 0 and a.store_id = '$store_id' order by b.id limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $list = $r1;
        }

        $sql2 = "select a.store_id,a.currency_id,a.is_show,a.default_currency,a.exchange_rate,a.recycle,a.update_time,b.currency_code,b.currency_name,b.currency_symbol,b.id from lkt_currency_store as a left join lkt_currency as b on a.currency_id = b.id where b.recycle = 0 and a.store_id = '$store_id' and a.default_currency";
        $r2 = Db::query($sql2);
        if($r2)
        {
            $haveDefaultCurrency = $r2[0];
        }

        $data = array('total'=>$total,'list'=>$list,'haveDefaultCurrency'=>$haveDefaultCurrency);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 修改币种设置
    public function saveOrEditCurrencyStore()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $currency_id = addslashes($this->request->param('currency_id'));
        $exchange_rate = addslashes($this->request->param('exchange_rate'));
        $time = date("Y-m-d H:i:s"); // 当前时间

        $sql0 = "select * from lkt_currency_store where store_id = '$store_id' and currency_id = '$currency_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $sql1 = "update lkt_currency_store set exchange_rate = '$exchange_rate',update_time = '$time' where store_id = '$store_id' and currency_id = '$currency_id' ";
            $r1 = Db::execute($sql1);
            if($r1 > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改失败！sql:'. $sql1;
                $this->Log($Log_content);
                $message = Lang('currency.6');
                return output(109,$message);
            }
        }
        else
        {
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 设置默认币种
    public function setDefaultCurrency()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $currency_id = addslashes($this->request->param('currency_id'));
        $time = date("Y-m-d H:i:s"); // 当前时间

        $sql0 = "update lkt_currency_store set default_currency = 0,update_time = '$time' where store_id = '$store_id' ";
        $r0 = Db::execute($sql0);

        $sql1 = "update lkt_currency_store set default_currency = 1,update_time = '$time' where store_id = '$store_id' and currency_id = '$currency_id' ";
        $r1 = Db::execute($sql1);
        if($r1 > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改成功';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改失败！sql:'. $sql1;
            $this->Log($Log_content);
            $message = Lang('currency.6');
            return output(109,$message);
        }
    }

    // 删除
    public function delCurrencyStore()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $currency_id = addslashes($this->request->param('currency_id'));

        if($currency_id != '')
        {
            $sql1 = "update lkt_currency_store set recycle = 1 where store_id = '$store_id' and currency_id = '$currency_id' ";
            $r1 = Db::execute($sql1);
            if($r1 > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改失败！sql:'. $sql1;
                $this->Log($Log_content);
                $message = Lang('currency.7');
                return output(109,$message);
            }
        }
        else
        {
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/currency.log",$Log_content);
        return;
    }
}
