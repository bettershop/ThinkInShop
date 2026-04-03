<?php
namespace app\admin\controller\saas;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;

use app\common\LaiKeLogUtils;

use app\admin\model\ThirdModel;

/**
 * 功能：参数设置
 * 修改人：DHB
 */
class Authorize extends BaseController
{
    // 获取参数设置
    public function getThridParmate()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$storeType = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));
    	
    	$id = addslashes($this->request->param('id')); // 
    	$workDomain = addslashes($this->request->param('workDomain')); // H5
    	$redirectUrl = addslashes($this->request->param('redirectUrl')); // 授权回调地址
    	$miniUrl = addslashes($this->request->param('miniUrl')); // 小程序请求地址
    	$qrCode = addslashes($this->request->param('qrCode')); // 体验二维码地址
    	$endurl = addslashes($this->request->param('endurl')); // 根目录路径

        $admin_name = $this->user_list['name'];
        
        $list = array();
        $r0 = ThirdModel::select()->toArray();
        if($r0)
        {
            $r0[0]['work_domain'] = $r0[0]['H5'];
            $list = $r0[0];
        }
        $data = array('list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加/修改参数设置
    public function addThridParmate()
    {
        $store_id = addslashes($this->request->param('storeId'));
    	$storeType = addslashes($this->request->param('storeType'));
    	$accessId = addslashes($this->request->param('accessId'));
    	
    	$id = addslashes($this->request->param('id')); // 
    	$workDomain = addslashes($this->request->param('workDomain')); // H5
    	$redirectUrl = addslashes($this->request->param('redirectUrl')); // 授权回调地址
    	$miniUrl = addslashes($this->request->param('miniUrl')); // 小程序请求地址
    	$qrCode = addslashes($this->request->param('qrCode')); // 体验二维码地址
    	$endurl = addslashes($this->request->param('endurl')); // 根目录路径

        $admin_name = $this->user_list['name'];

        if($id == '')
        {
            $sql = array('work_domain'=>$workDomain,'redirect_url'=>$redirectUrl,'mini_url'=>$miniUrl,'qr_code'=>$qrCode,'endurl'=>$endurl);
            $r = Db::name('third')->insert($sql);
            if ($r > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加参数设置成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加参数设置失败！参数:'. json_encode($sql);
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(109,$message);
            }
        }
        else
        {
            $sql_where = array('id'=>$id);
            $sql_update = array('work_domain'=>$workDomain,'redirect_url'=>$redirectUrl,'mini_url'=>$miniUrl,'qr_code'=>$qrCode,'endurl'=>$endurl);
            $r = Db::name('third')->where($sql_where)->update($sql_update);
            if ($r >= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改参数设置成功';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改参数设置失败！参数:'. json_encode($sql_where);
                $this->Log($Log_content);
                $message = Lang('operation failed');
                return output(109,$message);
            }
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("saas/authorize.log",$Log_content);
        return;
    }
}
