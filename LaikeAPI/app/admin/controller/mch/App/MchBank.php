<?php
namespace app\admin\controller\mch\App;

use app\BaseController;
use think\facade\Db;

use app\common\LaiKeLogUtils;
use app\common\MchPublicMethod;

use app\admin\model\MchModel;
use app\admin\model\BankCardModel;

class MchBank extends BaseController
{
    // 银行卡管理
    public function BankList()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->post('access_id')); // 授权id

        $user_id = $this->user_list['user_id'];

        $list = array();

        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->field('id')->select()->toArray();
        if($r0)
        {
            $mch_id = $r0[0]['id'];
        }
        
        $r1 = BankCardModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0])->order('is_default','desc')->field('id,Bank_name,Bank_card_number,is_default,branch as branchName')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $Bank_card_number = $v['Bank_card_number'];

                $v['Bank_card_number'] = substr($Bank_card_number,-4);

                $list[] = $v;
            }
        }

        $data = array('list'=>$list);
        $message = Lang('Success');
        return output(200, $message, $data);
    }

    // 添加银行卡
    public function AddBank()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->post('access_id')); // 授权id

        $Cardholder = trim($this->request->post('Cardholder')); // 持卡人
        $Bank_name = trim($this->request->post('Bank_name')); // 银行名称
        $branch = trim($this->request->post('branch')); // 支行名称
        $Bank_card_number = trim($this->request->post('Bank_card_number')); // 银行卡号
        $is_default = trim($this->request->post('is_default')); // 是否默认
        
        $user_id = $this->user_list['user_id'];
        Db::startTrans();

        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->field('id')->select()->toArray();
        if($r0)
        {
            $mch_id = $r0[0]['id'];
        }

        if (is_numeric($Bank_card_number) == false)
        {   
            $message = Lang('finance.2');
            return output(400, $message);
        }
        else
        {
            if (strlen($Bank_card_number) != 19 && strlen($Bank_card_number) != 16)
            {   
                $message = Lang('finance.3');
                return output(400, $message);
            }
            //查询是否重复添加
            $r1 = BankCardModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'Bank_card_number'=>$Bank_card_number,'recycle'=>0])->field('id')->select()->toArray();
            if ($r1) 
            {   
                $message = Lang('finance.4');
                return output(400, $message);
            }
        }

        // 根据卡号,查询银行名称
        require_once('../app/admin/controller/app/bankList.php');
        $r = $this->bankInfo($Bank_card_number, $bankList);
        if ($r == '')
        {
            $message = Lang('finance.3');
            return output(400, $message);
        }
        else
        {
            $name = strstr($r, '银行', true) . "银行";
            if ($name != $Bank_name)
            {   
                $message = Lang('finance.5');
                return output(400, $message);
            }
        }

        if($Cardholder == '')
        {   
            $message = Lang('finance.1');
            return output(400, $message);
        }
        if($branch == '')
        {   
            $message = Lang('finance.6');
            return output(400, $message);
        }

        if($is_default == '1')
        {
            // 清空默认选择
            $r2 = Db::name('bank_card')->where(['store_id'=>$store_id,'mch_id'=>$mch_id])->update(['is_default'=>0]);
            if($r2 < 0)
            {
                Db::rollback();
                $message = Lang('finance.9');
                return output(400, $message);
            }
        }
        else
        {
            //查询是否存在默认数据
            $r2 = BankCardModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'is_default'=>1,'recycle'=>0])->field('id')->select()->toArray();
            if(empty($r2))
            {
                $is_default = 1;
            }
        }

        $sql_inser = array('store_id'=>$store_id,'mch_id'=>$mch_id,'Cardholder'=>$Cardholder,'Bank_name'=>$Bank_name,'branch'=>$branch,'Bank_card_number'=>$Bank_card_number,'is_default'=>$is_default);
        $r_0 = Db::name('bank_card')->insert($sql_inser);
        if($r_0 < 1)
        {   
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . '添加银行卡失败！参数：' . json_encode($sql_inser);
            $this->Log($Log_content);
            $message = Lang('finance.9');
            return output(400, $message);
        }
        Db::commit();
        $message = Lang('Success');
        return output(200, $message);
    }

    // 编辑银行卡页面
    public function BankPage()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->post('access_id')); // 授权id
        $id = trim($this->request->post('id')); // 银行卡ID

        $user_id = $this->user_list['user_id'];

        $list = array();

        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->field('id')->select()->toArray();
        if($r0)
        {
            $mch_id = $r0[0]['id'];
        }
        
        $str = "id,store_id,user_id,mch_id,Cardholder as cardholder,Bank_name as bank_name,branch,Bank_card_number as bank_card_number,add_date,is_default,recycle";
        $r1 = BankCardModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id])->field($str)->select()->toArray();
        if($r1)
        {
            $list = $r1;
        }

        $data = array('bank'=>$list);
        $message = Lang('Success');
        return output(200, $message, $data);
    }

    // 编辑银行卡
    public function EditBank()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->post('access_id')); // 授权id

        $id = trim($this->request->post('id')); // 银行卡ID
        $Cardholder = trim($this->request->post('Cardholder')); // 持卡人
        $Bank_name = trim($this->request->post('Bank_name')); // 银行名称
        $branch = trim($this->request->post('branch')); // 支行名称
        $Bank_card_number = trim($this->request->post('Bank_card_number')); // 银行卡号
        $is_default = trim($this->request->post('is_default')); // 是否默认
        
        $user_id = $this->user_list['user_id'];
        Db::startTrans();

        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->field('id')->select()->toArray();
        if($r0)
        {
            $mch_id = $r0[0]['id'];
        }

        if (is_numeric($Bank_card_number) == false)
        {   
            $message = Lang('finance.2');
            return output(400, $message);
        }
        else
        {
            if (strlen($Bank_card_number) != 19 && strlen($Bank_card_number) != 16)
            {   
                $message = Lang('finance.3');
                return output(400, $message);
            }
            // 查询是否重复添加
            $r1 = BankCardModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'Bank_card_number'=>$Bank_card_number,'recycle'=>0])->where('id','<>',$id)->field('id')->select()->toArray();
            if ($r1) 
            {   
                $message = Lang('finance.4');
                return output(400, $message);
            }
        }

        // 根据卡号,查询银行名称
        require_once('../app/admin/controller/app/bankList.php');
        $r = $this->bankInfo($Bank_card_number, $bankList);
        if ($r == '')
        {
            $message = Lang('finance.3');
            return output(400, $message);
        }
        else
        {
            $name = strstr($r, '银行', true) . "银行";
            if ($name != $Bank_name)
            {   
                $message = Lang('finance.5');
                return output(400, $message);
            }
        }

        if($Cardholder == '')
        {   
            $message = Lang('finance.1');
            return output(400, $message);
        }
        if($branch == '')
        {   
            $message = Lang('finance.6');
            return output(400, $message);
        }

        if($is_default == '1')
        {
            // 清空默认选择
            $r2 = Db::name('bank_card')->where(['store_id'=>$store_id,'mch_id'=>$mch_id])->update(['is_default'=>0]);
            if($r2 < 0)
            {
                Db::rollback();
                $message = Lang('finance.9');
                return output(400, $message);
            }
        }
        else
        {
            //查询是否存在默认数据
            $r2 = BankCardModel::where(['id'=>$id])->field('is_default')->select()->toArray();
            if($r2[0]['is_default'] == 1)
            {
                $message = Lang('finance.12');
                return output(400, $message);
            }
        }

        $sql_where = array('id'=>$id);
        $sql_update = array('Cardholder'=>$Cardholder,'Bank_name'=>$Bank_name,'branch'=>$branch,'Bank_card_number'=>$Bank_card_number,'is_default'=>$is_default);
        $r_0 = Db::name('bank_card')->where($sql_where)->update($sql_update);
        if($r_0 < 0)
        {   
            Db::rollback();
            $Log_content = __METHOD__ . '->' . __LINE__ . '编辑银行卡失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
            $this->Log($Log_content);
            $message = Lang('finance.9');
            return output(400, $message);
        }
        Db::commit();
        $message = Lang('Success');
        return output(200, $message);
    }

    // 解绑银行卡
    public function DelBank()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $access_id = trim($this->request->param('access_id')); // 授权id

        $id = trim($this->request->param('id')); // 银行卡ID
        
        $user_id = $this->user_list['user_id'];

        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->field('id')->select()->toArray();
        if($r0)
        {
            $mch_id = $r0[0]['id'];
        }

        $sql_where = array('id'=>$id);
        $sql_update = array('recycle'=>1);
        $r_0 = Db::name('bank_card')->where($sql_where)->update($sql_update);
        if($r_0 < 0)
        {   
            $Log_content = __METHOD__ . '->' . __LINE__ . '解绑银行卡失败！条件参数：' . json_encode($sql_where) . '；修改参数：' . json_encode($sql_update);
            $this->Log($Log_content);
            $message = Lang('finance.9');
            return output(400, $message);
        }
        $message = Lang('Success');
        return output(200, $message);
    }

    // 验证卡号是否跟银行匹配
    function bankInfo($card, $bankList)
    {
        $card_8 = substr($card, 0, 8);
        if (isset($bankList[$card_8]))
        {
            return $bankList[$card_8];
        }
        $card_6 = substr($card, 0, 6);
        if (isset($bankList[$card_6]))
        {
            return $bankList[$card_6];
        }
        $card_5 = substr($card, 0, 5);
        if (isset($bankList[$card_5]))
        {
            return $bankList[$card_5];
        }
        $card_4 = substr($card, 0, 4);
        if (isset($bankList[$card_4]))
        {
            return $bankList[$card_4];
        }
        return '';
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/MchBank.log",$Log_content);
        return;
    }

}
