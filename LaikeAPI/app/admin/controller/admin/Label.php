<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\Tools;

use app\admin\model\ProLabelModel;
use app\admin\model\ProductListModel;

/**
 * 功能：商品类
 * 修改人：DHB
 */
class Label extends BaseController
{
    // 分类标签列表
    public function index()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 分类标签ID
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语种
        $name = addslashes(trim($this->request->param('name'))); // 标题
        $pageNo = addslashes(trim($this->request->param('pageNo'))); // 页码
        $pageSize = addslashes(trim($this->request->param('pageSize'))); // 每页多少条数据

        $lang_code = Tools::get_lang($lang_code);

        $condition = " store_id = '$store_id' ";

        if($id != '' && $id != 0)
        {
            $condition .= " and id = '$id' ";
        }
        if ($name != '')
        {
            $name = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and name like $name ";
        }
        if($lang_code != '')
        {
            $condition .= " and lang_code = '$lang_code' ";
        }
        $total = 0;
        $list = array();
        $r0 = ProLabelModel::where($condition)->field('count(id) as total')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['total'];
        }
        
        $r1 = ProLabelModel::where($condition)->page((int)$pageNo,(int)$pageSize)->order('add_time','desc')->select()->toArray();
        if($r1)
        {
            foreach($r1 as $k1 => $v1)
            {
                $v1['country_name'] = Tools::get_country_name($v1['country_num']);
                $v1['lang_name'] = Tools::get_lang_name($v1['lang_code']);
                $list[] = $v1;
            }
        }
        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加/编辑分类标签
    public function addGoodsLabel()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 分类标签ID
        $name = addslashes(trim($this->request->param('name'))); // 标题
        $lang_code = addslashes(trim($this->request->param('lang_code'))); // 语言
        $country_num = addslashes(trim($this->request->param('country_num'))); // 国家代码
        $color = addslashes(trim($this->request->param('color'))); // 颜色 1.红色 2.橘色 3.黄色 4.绿色 5.蓝色

        Tools::National_Language($lang_code,$country_num);

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');
        
        $time = date("Y-m-d H:i:s");
        if($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品标签不能为空！';
            $this->Log($Log_content);
            $message = Lang('label.0');
            return output(109,$message);
        }
        if($id != '' && $id != 0)
        {
            $r0 = ProLabelModel::where(['store_id'=>$store_id,'name'=>$name,'lang_code'=>$lang_code,'country_num'=>$country_num])->where('id','<>',$id)->field('id')->select()->toArray();
        }
        else
        {
            $r0 = ProLabelModel::where(['store_id'=>$store_id,'name'=>$name,'lang_code'=>$lang_code,'country_num'=>$country_num])->field('id')->select()->toArray();
        }
        
        if($r0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 商品标签已经存在！';
            $this->Log($Log_content);
            $message = Lang('label.1');
            return output(109,$message);
        }
        
        if($id != '' && $id != 0)
        {
            $sql1 = array('store_id'=>$store_id,'id'=>$id);
            $r1 = Db::name('pro_label')->where($sql1)->update(['name'=>$name,'lang_code'=>$lang_code,'country_num'=>$country_num,'color'=>$color,'add_time'=>$time]);
            if($r1 == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了标签ID：'.$id.' 的信息失败',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改失败！参数:'.json_encode($sql1);
                $this->Log($Log_content);
                $message = Lang('label.2');
                return output(109,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了标签ID：'.$id.' 的信息',2,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改成功！';
                $this->Log($Log_content);
                $message = Lang('label.3');
                return output(200,$message);
            }
        }
        else
        {
            $sql1 = array('store_id'=>$store_id,'name'=>$name,'lang_code'=>$lang_code,'country_num'=>$country_num,'color'=>$color,'add_time'=>$time);
            $r1 = Db::name('pro_label')->insert($sql1);
            if($r1 > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了标签：'.$name,1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加成功！';
                $this->Log($Log_content);
                $message = Lang('label.4');
                return output(200,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了标签：'.$name.' 失败',1,1,0,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加失败！参数:'.json_encode($sql1);
                $this->Log($Log_content);
                $message = Lang('label.5');
                return output(109,$message);
            }
        }
    }

    // 删除分类标签
    public function delGoodsLabel()
    {
        $store_id = addslashes(trim($this->request->param('storeId')));
        $store_type = addslashes(trim($this->request->param('storeType')));
        $access_id = addslashes(trim($this->request->param('accessId')));

        $id = addslashes(trim($this->request->param('id'))); // 分类标签ID

        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $r0 = ProductListModel::where(['store_id'=>$store_id,'recycle'=>0])->whereIn('s_type',$id)->field('id')->select()->toArray();
        if($r0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 改标签正在使用，不能删除！参数:'.$id;
            $this->Log($Log_content);
            $message = Lang('label.6');
            return output(109,$message);
        }

        $r1 = Db::table('lkt_pro_label')->where(['store_id'=>$store_id,'id'=>$id])->delete();
        if($r1 == -1)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了标签ID：'.$id.' 失败',3,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除失败！参数:'.$id;
            $this->Log($Log_content);
            $message = Lang('label.7');
            return output(109,$message);
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了标签ID：'.$id,3,1,0,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除成功！';
            $this->Log($Log_content);
            $message = Lang('label.8');
            return output(200,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("admin/label.log",$Log_content);
        return;
    }
}
