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
class currency extends BaseController
{
    // 币种列表
    public function currencyList()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id')));
        $keyword = addslashes(trim($this->request->param('keyword')));
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
        if($id != '')
        {
            $sql0 = "select count(id) as total from lkt_currency where id = '$id' ";
            $sql1 = "select * from lkt_currency where id = '$id' limit $start,$pagesize ";
        }
        else
        {
            if($keyword != '')
            {
                $sql0 = "select count(id) as total from lkt_currency where recycle = 0 and currency_name like '%$keyword%' ";
                $sql1 = "select * from lkt_currency where recycle = 0 and currency_name like '%$keyword%' order by id desc limit $start,$pagesize ";
            }
            else
            {
                $sql0 = "select count(id) as total from lkt_currency where recycle = 0 ";
                $sql1 = "select * from lkt_currency where recycle = 0 order by id desc limit $start,$pagesize ";
            }
        }
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $r1 = Db::query($sql1);
        if($r1)
        {
            $list = $r1;
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang("Success");
        return output(200,$message,$data);
    }
    
    // 添加/编辑币种
    public function saveOrEditCurrency()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id')));
        $currency_name = addslashes($this->request->param('currency_name')); // 货币名称
        $currency_code = addslashes($this->request->param('currency_code')); // ISO货币代码(如USD)
        $currency_symbol = addslashes($this->request->param('currency_symbol')); // 货币符号($)
        $is_show = addslashes($this->request->param('is_show')); // 是否展示 0 不展示 1展示

        $time = date("Y-m-d H:i:s");

        if($currency_name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '货币名称不能为空！';
            $this->Log($Log_content);
            $message = Lang('currency.0');
            return output(109,$message);
        }
        else
        {
            if($id != '')
            {
                $sql0 = "select id from lkt_currency where currency_name = '$currency_name' and recycle = 0 and id != '$id' ";
            }
            else
            {
                $sql0 = "select id from lkt_currency where currency_name = '$currency_name' and recycle = 0 ";
            }
            $r0 = Db::query($sql0);
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '货币名称重复！';
                $this->Log($Log_content);
                $message = Lang('currency.1');
                return output(109,$message);
            }
        }
        if($currency_code == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . 'ISO货币代码不能为空！';
            $this->Log($Log_content);
            $message = Lang('currency.2');
            return output(109,$message);
        }
        else
        {
            if($id != '')
            {
                $sql1 = "select id from lkt_currency where currency_code = '$currency_code' and recycle = 0 and id != '$id' ";
            }
            else
            {
                $sql1 = "select id from lkt_currency where currency_code = '$currency_code' and recycle = 0 ";
            }
            $r1 = Db::query($sql1);
            if($r1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . 'ISO货币代码重复！';
                $this->Log($Log_content);
                $message = Lang('currency.3');
                return output(109,$message);
            }
        }
        if($currency_symbol == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '货币符号不能为空！';
            $this->Log($Log_content);
            $message = Lang('currency.4');
            return output(109,$message);
        }

        if($id != '')
        {
            $sql = "update lkt_currency set currency_code = '$currency_code',currency_name = '$currency_name',currency_symbol = '$currency_symbol',update_time = '$time' where id = '$id' ";
            $r = Db::execute($sql);
            if($r > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改货币成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改货币失败！sql:'. $sql;
                $this->Log($Log_content);
                $message = Lang('currency.6');
                return output(109,$message);
            }
        }
        else
        {
            $sql = "insert into lkt_currency(currency_code,currency_name,currency_symbol,is_show,update_time) value ('$currency_code','$currency_name','$currency_symbol','$is_show','$time') ";
            $r = Db::execute($sql);
            if($r > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加货币成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加货币失败！sql:'. $sql;
                $this->Log($Log_content);
                $message = Lang('currency.5');
                return output(109,$message);
            }
        }
    }

    // 删除币种
    public function delCurrency()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $id = addslashes(trim($this->request->param('id')));

        $sql = "update lkt_currency set recycle = 1 where id = '$id' ";
        $r = Db::execute($sql);
        if($r > 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除国家成功';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除国家失败！sql:'. $sql;
            $this->Log($Log_content);
            $message = Lang('currency.7');
            return output(109,$message);
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
