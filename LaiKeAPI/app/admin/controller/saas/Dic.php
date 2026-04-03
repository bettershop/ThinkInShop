<?php
namespace app\admin\controller\saas;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Product;
use app\common\Tools;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\ExcelUtils;

use app\admin\model\DataDictionaryNameModel;
use app\admin\model\DataDictionaryListModel;
use app\admin\model\SkuModel;
use app\admin\model\ConfigureModel;
use app\admin\model\AdminModel;

/**
 * 功能：数据字典
 * 修改人：DHB
 */
class Dic 
{
    // 数据名称列表
    public function getDictionaryCatalogInfo()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $name = addslashes(trim(Request::param('name'))); // 数据名称
        $page = addslashes(Request::param('pageNo'));
        $pagesize = addslashes(Request::param('pageSize'));

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " recycle = 0 ";

        if($name != '')
        {
            $name_0 = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and name like $name_0 ";
        }

        $total = 0;
        $sql0 = "select count(id) as num from lkt_data_dictionary_name where $condition";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }

        $list = array();
        $sql1 = "select * from lkt_data_dictionary_name where $condition order by add_date desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $rew = Tools::test_data_dictionary_name($v['id']);
                $v['status_status'] = $rew;
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加/编辑数据名称
    public function addDictionaryInfo()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $id = addslashes(trim(Request::param('id'))); // 数据名称ID
        $name = addslashes(trim(Request::param('name'))); // 数据名称
        $status = addslashes(trim(Request::param('isOpen'))); // 是否生效 0:不是 1:是

        $admin_name = '';
        $r_admin = AdminModel::where(['recycle'=>0,'token'=>$accessId])->field('name')->select()->toArray();
        if($r_admin)
        {
            $admin_name = $r_admin[0]['name'];
        }
        
        if($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 数据名称不能为空 ';
            $this->Log($Log_content);
            $message = Lang('dic.0');
            return output(109,$message);
        }
        else
        {
            if($id != 0 && $id != '')
            {
                $r0 = DataDictionaryNameModel::where(['name'=>$name,'recycle'=>0])->where('id','<>',$id)->select()->toArray();
            }
            else
            {
                $r0 = DataDictionaryNameModel::where(['name'=>$name,'recycle'=>0])->select()->toArray();
            }
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 数据名称'.$name.'已存在 ';
                $this->Log($Log_content);
                $message = Lang('dic.1');
                return output(109,$message);
            }
        }
        $time = date("Y-m-d H:i:s");

        if($id != 0 && $id != '')
        {
            $sql2 = array('name'=>$name,'status'=>$status,'admin_name'=>$admin_name,'add_date'=>$time);
            $r2 = Db::name('data_dictionary_name')->where('id',$id)->update($sql2);
            if($r2 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 修改数据字典名称失败！sql:'.$sql2;
                $this->Log($Log_content);
                $message = Lang('dic.2');
                return output(109,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改数据字典名称ID为'.$id.'成功 ';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $code1 = substr(strtoupper(Tools::encode($name)),0);
            $code = 'LKT_' . $code1;
            $r_0 = DataDictionaryNameModel::where(['recycle'=>0,'dic_code'=>$code])->field('id')->select()->toArray();
            if($r_0)
            {
                $r_1 = DataDictionaryNameModel::where(['recycle'=>0])->whereLike('dic_code','%'.$code.'%')->field('count(id) as num')->select()->toArray();
                $num = $r_1[0]['num'];
                $code = $code . '_' . $num ;
            }

            $sql1 = array('name'=>$name,'status'=>$status,'admin_name'=>$admin_name,'add_date'=>$time,'dic_code'=>$code);
            $r1 = Db::name('data_dictionary_name')->insert($sql1);
            if($r1 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 添加数据字典名称失败！参数:'.json_encode($sql1);
                $this->Log($Log_content);
                $message = Lang('dic.3');
                return output(109,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 添加数据字典名称成功 ';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
	}

    // 是否生效数据名
    public function switchDictionary()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $id = addslashes(trim(Request::param('id'))); // 数据名称ID

        $r0 = DataDictionaryNameModel::where(['id'=>$id])->select()->toArray();
        if($r0)
        {
            $status = $r0[0]['status'];

            if($status == 0)
            {
                $r1 = Db::name('data_dictionary_name')->where('id',$id)->update(['status'=>1]);
                if($r1 == -1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ .' 开启数据字典名称失败！ID为:'.$id;
                    $this->Log($Log_content);
                    $message = Lang('dic.4');
                    return output(109,$message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ .' 开启数据字典名称ID为'.$id.'成功 ';
                    $this->Log($Log_content);
                    $message = Lang('dic.5');
                    return output(200,$message);
                }
            }
            else
            {
                $r1 = Db::name('data_dictionary_name')->where('id',$id)->update(['status'=>0]);
                if($r1 == -1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ .' 关闭数据字典名称失败！ID为:'.$id;
                    $this->Log($Log_content);
                    $message = Lang('dic.6');
                    return output(109,$message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ .' 关闭数据字典名称ID为'.$id.'成功 ';
                    $this->Log($Log_content);
                    $message = Lang('dic.7');
                    return output(200,$message);
                }
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 开启/关闭数据字典名称ID为'.$id.'失败 ';
            $this->Log($Log_content);
            $message = Lang('dic.8');
            return output(109,$message);
        }
    }

    // 删除数据名称
    public function delDictionary()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $id = addslashes(Request::param('idList')); // 数据名称ID

        $id = trim($id,','); // 移除两侧的逗号
        $id = explode(',',$id);
        Db::startTrans();

        foreach ($id as $k => $v)
        {
            $r = Db::name('data_dictionary_name')->where('id',$v)->update(['recycle'=>1]);
            if($r > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 删除数据字典名称ID为'.$v.'成功 ';
                $this->Log($Log_content);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 删除数据字典名称失败！ID为:'.$v;
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('dic.9');
                return output(109,$message);
            }
        }
        Db::commit();
        $message = Lang('Success');
        return output(200,$message);
    }

    // 获取字典信息
    public function getDictionaryInfo()
    {	
        $store_id = addslashes(Request::param('storeId'));
        $storeType = addslashes(Request::param('storeType'));
        $accessId = addslashes(Request::param('accessId'));
        $language = addslashes(Request::param('language')); // 语言
        $language = Tools::get_lang($language);

        $dicNo = addslashes(trim(Request::param('dicNo'))); // 数据编码
        $key = addslashes(trim(Request::param('key'))); // 数据名称
        $text = addslashes(trim(Request::param('text'))); // 数据值
        $lang_code = addslashes(trim(Request::param('lang_code'))); // 语言
        $page = addslashes(trim(Request::param('pageNo'))); // 页码
        $pagesize = addslashes(trim(Request::param('pageSize'))); // 每页显示多少条数据
        if($pagesize == '')
        {
            $pagesize = 10;
        }
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $total = 0;
        $list = array();
        // $con = " a.recycle = 0 and b.recycle = 0 and a.status = 1 ";
        $con = " a.recycle = 0 and b.recycle = 0 ";
        if($lang_code != '')
        {
            $con .= " and a.lang_code = '$lang_code' ";
        }
        else
        {
            $con .= " and a.lang_code = '$language' ";
        }
        if($dicNo != '')
        {
            $dicNo_0 = Tools::FuzzyQueryConcatenation($dicNo);
            $con .= " and a.code like $dicNo_0 ";
        }
        if($key != '')
        {
            $key_0 = Tools::FuzzyQueryConcatenation($key);
            $con .= " and b.name like $key_0 ";
        }
        if($text != '')
        {
            $text_0 = Tools::FuzzyQueryConcatenation($text);
            $con .= " and a.text like $text_0 ";
        }

        $sql0 = "select count(a.id) as total from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select a.*,b.name from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where $con order by a.add_date desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['lang_name'] = langCode2Name($v['lang_code']);
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 获取数据名称
    public function getDictionaryCatalogList()
    {
        $list = array();
        $r0 = DataDictionaryNameModel::where(['status'=>1,'recycle'=>0])->where('name', '<>','属性名')->where('name', '<>','属性值')->field('id,name')->select()->toArray();
        if($r0)
        {
            $list = $r0;
        }

        $data = array('data'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
	}
	
	// 获取code
    public function getDictionaryCode()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $id = addslashes(trim(Request::param('id'))); // 数据名称ID

        $r0 = DataDictionaryNameModel::where(['id'=> $id])->field('dic_code')->select()->toArray();
        if($r0)
        {
            $code = $r0[0]['dic_code'];
            $rew1 = '';
            $r1 = DataDictionaryListModel::where(['sid'=> $id])->whereLike('code', '%'.$code.'%')->field('count(id) as num')->select()->toArray();
            if($r1[0]['num'] == 0)
            {
                $rew = $code . '_001';
            }
            else
            {
                $num = $r1[0]['num'] + 1;
                if(strlen($num) == 1)
                {
                    $rew = $code . '_00' . $num;
                }
                else if(strlen($num) == 2)
                {
                    $rew = $code . '_0' . $num;
                }
                else if(strlen($num) == 3)
                {
                    $rew = $code . '_' . $num;
                }
                else
                {
                    $rew = $code . '_001';
                }
            }
        }

        $list = array();
        $sql_0 = "select id from lkt_data_dictionary_name where status = 1 and recycle = 0 and name = '短信模板类型' ";
        $r_0 = Db::query($sql_0);
        if($r0)
        {
            $sid = $r_0[0]['id'];
            $sql_1 = "select id,text as ctext from lkt_data_dictionary_list where status = 1 and recycle = 0 and sid = '$sid' ";
            $r_1 = Db::query($sql_1);
            if($r_1)
            {
                $list = $r_1;
            }
        }

        $data = array('code'=>$rew,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
	}

    // 添加/编辑数值
    public function addDictionaryDetailInfo()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $id = addslashes(trim(Request::param('id'))); // 数据ID
        $code = addslashes(trim(Request::param('dataCode'))); // 数据编码
        $data_dictionary_id = addslashes(trim(Request::param('sid'))); // 数据名称ID
        $lang_code = addslashes(trim(Request::param('lang_code'))); // 语言
        $country_num = addslashes(trim(Request::param('country_num'))); // 国家代码
        $value = addslashes(trim(Request::param('valueName'))); // value
        $text = addslashes(trim(Request::param('valueCode'))); // 值
        $status = addslashes(trim(Request::param('isOpen'))); // 是否生效

        Tools::National_Language($lang_code,$country_num);

        $admin_name = '';
        $r_admin = AdminModel::where(['recycle'=>0,'token'=>$accessId])->field('name')->select()->toArray();
        if($r_admin)
        {
            $admin_name = $r_admin[0]['name'];
        }

        if($code == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 数据编码不能为空 ';
            $this->Log($Log_content);
            $message = Lang('dic.10');
            return output(109,$message);
        }
        else
        {
            if($id != '')
            {
                $r0 = DataDictionaryListModel::where(['code'=>$code,'recycle'=>0,'sid'=>$data_dictionary_id,'lang_code'=> $lang_code])->where('id','<>',$id)->field('id')->select()->toArray();
            }
            else
            {  
                $r0 = DataDictionaryListModel::where(['code'=>$code,'recycle'=>0,'sid'=>$data_dictionary_id,'lang_code'=> $lang_code])->field('id')->select()->toArray();
            }
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 数据编码'.$code.'已存在 ';
                $this->Log($Log_content);
                $message = Lang('dic.11');
                return output(109,$message);
            }
        }

        if($data_dictionary_id == '' || $data_dictionary_id == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择数据名称 ';
            $this->Log($Log_content);
            $message = Lang('dic.12');
            return output(109,$message);
        }

        $r1 = DataDictionaryNameModel::where(['id'=>$data_dictionary_id,'recycle'=>0])->field('name')->select()->toArray();
        if($r1)
        {
            $name = $r1[0]['name'];
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 参数错误！id:'.$data_dictionary_id;
            $this->Log($Log_content);
            $message = Lang('Parameter error');
            return output(109,$message);
        }

        if ($value == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' code不能为空 ';
            $this->Log($Log_content);
            $message = Lang('dic.13');
            return output(109,$message);
        }

        if($id != '')
        {
            $sql1_1 = array('sid'=>$data_dictionary_id,'value'=>$value,'lang_code'=>$lang_code,'recycle'=>0);
            $r1_1 = DataDictionaryListModel::where($sql1_1)->where('id','<>',$id)->select()->toArray();
        }
        else
        {  
            $sql1_1 = array('sid'=>$data_dictionary_id,'value'=>$value,'lang_code'=>$lang_code,'recycle'=>0);
            $r1_1 = DataDictionaryListModel::where($sql1_1)->select()->toArray();
        }
        if($r1_1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' code重复！sql:'.json_encode($sql1_1);
            $this->Log($Log_content);
            $message = Lang('dic.14');
            return output(109,$message);
        }
        $time = date("Y-m-d H:i:s");

        if($text == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 值不能为空 ';
            $this->Log($Log_content);
            $message = Lang('dic.15');
            return output(109,$message);
        }
        else
        {
            if($name == '短信模板类别')
            {
                $r2_0 = DataDictionaryListModel::where(['id'=>$subordinate_name])->field('text')->select()->toArray();
                $s_name = $r2_0[0]['text'];

                if($id != 0 && $id != '')
                {
                    $r2 = DataDictionaryListModel::where(['sid'=>$data_dictionary_id,'s_name'=>$s_name,'text'=>$text,'lang_code'=>$lang_code,'recycle'=>0])->where('id','<>',$id)->select()->toArray();
                }
                else
                {
                    $r2 = DataDictionaryListModel::where(['id'=>$subordinate_name,'s_name'=>$s_name,'text'=>$text,'lang_code'=>$lang_code,'recycle'=>0])->select()->toArray();

                    $sql3 = array('code'=>$code,'sid'=>$data_dictionary_id,'s_name'=>$s_name,'value'=>$value,'text'=>$text,'lang_code'=>$lang_code,'country_num'=>$country_num,'status'=>$status,'admin_name'=>$admin_name,'add_date'=>$time);
                }
                if($r2)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 值'.$text.'已存在！';
                    $this->Log($Log_content);
                    $message = Lang('dic.16');
                    return output(109,$message);
                }
            }
            else
            {
                if($id != 0 && $id != '')
                {
                    $r2 = DataDictionaryListModel::where(['sid'=>$data_dictionary_id,'text'=>$text,'lang_code'=>$lang_code,'recycle'=>0])->where('id','<>',$id)->select()->toArray();
                }
                else
                {
                    $r2 = DataDictionaryListModel::where(['sid'=>$data_dictionary_id,'text'=>$text,'lang_code'=>$lang_code,'recycle'=>0])->select()->toArray();

                    $sql3 = array('code'=>$code,'sid'=>$data_dictionary_id,'value'=>$value,'text'=>$text,'lang_code'=>$lang_code,'country_num'=>$country_num,'status'=>$status,'admin_name'=>$admin_name,'add_date'=>$time);
                }
                if($r2)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 值'.$text.'已存在 ';
                    $this->Log($Log_content);
                    $message = Lang('dic.16');
                    return output(109,$message);
                }
            }
        }
        if($id != 0 && $id != '')
        {
            $data3_update = array('text'=>$text,'status'=>$status,'admin_name'=>$admin_name,'add_date'=>$time);
            $data3_where = array('id'=>$id);
            $r3 = Db::name('data_dictionary_list')->where($data3_where)->update($data3_update);
            if($r3 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 修改数据字典失败！参数:'.json_encode($data3_where);
                $this->Log($Log_content);
                $message = Lang('dic.2');
                return output(109,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 修改数据字典ID为'.$id.'成功 ';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
        else
        {
            $r3 = Db::name('data_dictionary_list')->insert($sql3);
            if($r3 == -1)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 添加数据字典失败！参数:'.json_encode($sql3);
                $this->Log($Log_content);
                $message = Lang('dic.3');
                return output(109,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 添加数据字典成功 ';
                $this->Log($Log_content);
                $message = Lang('Success');
                return output(200,$message);
            }
        }
	}

    // 是否生效数据值
    public function switchDictionaryDetail()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $id = addslashes(trim(Request::param('id'))); // 数据ID

        $sql0 = "select a.*,b.name from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.id = '$id'";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $status = $r0[0]['status'];
            $rew = Tools::test_data_dictionary($id,$status);
            
            if($rew['status'])
            {
                if($status == 0)
                {
                    $r1 = Db::name('data_dictionary_list')->where('id',$id)->update(['status'=>1]);
                    if($r1 == -1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ .' 开启数据字典失败！ID为:'.$id;
                        $this->Log($Log_content);
                        $message = Lang('dic.4');
                        return output(109,$message);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ .' 开启数据字典ID为'.$id.'成功 ';
                        $this->Log($Log_content);
                        $message = Lang('dic.5');
                        return output(200,$message);
                    }
                }
                else
                {
                    $r1 = Db::name('data_dictionary_list')->where('id',$id)->update(['status'=>0]);
                    if($r1 == -1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ .' 关闭数据字典失败！ID为:'.$id;
                        $this->Log($Log_content);
                        $message = Lang('dic.6');
                        return output(109,$message);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ .' 关闭数据字典ID为'.$id.'成功 ';
                        $this->Log($Log_content);
                        $message = Lang('dic.7');
                        return output(200,$message);
                    }
                }
            }
            else
            {
                return output(200,$rew['message']);
            }
        }
        else
        {
            $message = Lang('operation failed');
            return output(109,$message);
        }
    }

    // 删除数据值
    public function delDictionaryDetailInfo()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $id = addslashes(Request::param('idList')); // 数据名称ID

        $id = trim($id,','); // 移除两侧的逗号
        $id = explode(',',$id);
        Db::startTrans();

        foreach ($id as $k => $v)
        {
            $rew = Tools::test_data_dictionary($v,'');
            $sql0 = "select a.*,b.name from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.id = '$v'";
            $r0 = Db::query($sql0);
            if($rew['status'])
            {
                $r = Db::name('data_dictionary_list')->where('id',$v)->update(['recycle'=>1]);
                if($r > 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ .' 删除数据字典ID为'.$v.'成功 ';
                    $this->Log($Log_content);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ .' 删除数据字典失败！ID:'.$v;
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('dic.17');
                    return output(109,$message);
                }
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 删除数据字典ID为'.$v.'失败。'.$rew['message'];
                $this->Log($Log_content);
                Db::rollback();
                return output(109,$rew['message']);
            }
        }

        Db::commit();
        $message = Lang('Success');
        return output(200,$message);
    }

    // SKU列表
    public function getSkuInfo()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $exportType = addslashes(Request::param('exportType')); // 导出
        $key = addslashes(Request::param('key')); // 属性编码
        $id = addslashes(Request::param('id')); // 属性编码
        $sid = addslashes(Request::param('sid')); // 属性编码
        $code = addslashes(Request::param('dataCode')); // 属性编码
        $name = addslashes(Request::param('dataName')); // 属性名称
        $lang_code = addslashes(trim(Request::param('lang_code'))); // 语言
        $page = addslashes(Request::param('pageNo')); // 页码
        $pagesize = addslashes(Request::param('pageSize')); // 每页显示多少条数据
        
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $total = 0;
        $list = array();

        if($key != '')
        {
            $data = array('total'=>$total,'list'=>$list);
            $message = Lang('Success');
            return output(200,$message,$data);
        }

        $condition = " recycle = 0 and is_examine = 1 ";
        if($lang_code != '')
        {
            $condition .= " and lang_code = '$lang_code' ";
        }
        if($code != '')
        {
            $condition .= " and code like '%$code%' ";
        }
        if($name != '')
        {
            $name_0 = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and name like $name_0 ";
        }

        if($id != '')
        {
            $total = 1;
            $r_0 = SkuModel::where(['id'=> $id,'recycle'=>0,'type'=>1])->select()->toArray();
            if($r_0)
            {
                foreach ($r_0 as $k_0 => $v_0)
                {
                    if($v_0['status'] == 1)
                    {
                        $v_0['statusName'] = '生效'; 
                    }
                    else
                    {
                        $v_0['statusName'] = '失效';
                    }

                    $condition .= " and sid = '$id' ";

                    $v_0['sunSkusTotal'] = 0;
                    
                    $sql_1 = "select count(id) as num from lkt_sku where $condition ";
                    $r_1 = Db::query($sql_1);
                    if($r_1)
                    {
                        $v_0['sunSkusTotal'] = $r_1[0]['num'];
                    }
                    $v_0['sunSkus'] = array();
                    $sql_2 = "select * from lkt_sku where $condition order by id desc limit $start,$pagesize ";
                    
                    // if($start > 0 )
                    // {
                    //     var_dump($sql_2);
                    // }
                    
                    $r_2 = Db::query($sql_2);
                    if($r_2)
                    {
                        $v_0['sunSkus'] = $r_2;
                    }
                    $list[] = $v_0;
                }
            }
        }
        else 
        {
            if($sid != '')
            { // 编辑页面
                $condition .= " and sid = '$sid' ";
            }
            else
            { // suk列表
                $condition .= " and type = 1 ";
            }

            $sql0 = "select count(id) as num from lkt_sku where $condition ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $total = $r0[0]['num'];
            }

            $sql1 = "select * from lkt_sku where $condition order by add_date desc limit $start,$pagesize ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                foreach($r1 as $k => $v)
                {
                    if($v['status'] == 1)
                    {
                        $v['statusName'] = '生效';
                    }
                    else
                    {
                        $v['statusName'] = '失效';
                    }
                    $v['lang_name'] = langCode2Name($v['lang_code']);
                    $list[] = $v;
                }
            }
        }

        //请求为导出
        if ($exportType)
        {
            $titles = array(
                0 => '属性编码',
                1 => '属性名称',
                2 => '添加人',
                3 => '添加时间',
                4 => '是否生效'
            );
            $exportExcel_list = array();
            if ($list)
            {
                foreach ($list as $k => $v)
                {
                    $exportExcel_list[] = array(
                        $v['code'],
                        $v['name'],
                        $v['admin_name'],
                        $v['add_date'],
                        $v['statusName']
                    );
                }
                ExcelUtils::exportExcel($exportExcel_list, $titles, 'SKU列表');
                exit;
            }
            else
            {
                $message = Lang('No data available');
                return output(109, $message);
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 获取属性名
    public function getSkuAttributeList()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $pagesize = addslashes(trim(Request::param('pageSize'))); // 每页显示多少条数据
        $lang_code = addslashes(trim(Request::param('lang_code'))); // 语种
        if(!$lang_code)
        {
            $lang_code = addslashes(trim(Request::param('language'))); // 语种
        }
        $page = 1;

        $SkuModel = new SkuModel();
        
        $condition = array('recycle'=>0,'type'=>1,'is_examine'=>1,'lang_code'=>$lang_code);

        $r_condition0 = $SkuModel->where($condition);
        $r_condition1 = $SkuModel->where($condition);

        $total = 0;
        $r0 = $r_condition0->field('count(id) as num')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['num'];
        }
        $list = array();
        $r1 = $r_condition1->page((int)$page,(int)$pagesize)->order('add_date','desc')->select()->toArray();
        if($r1)
        {
            $list = $r1;
        }
        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 添加属性名
    public function addSkuName()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $lang_code = addslashes(trim(Request::param('lang_code'))); // 语言
        $country_num = addslashes(trim(Request::param('country_num'))); // 国家代码
        $name = addslashes(trim(Request::param('skuName'))); // 属性名
        $status = addslashes(trim(Request::param('isOpen'))); // 是否生效

        Tools::National_Language($lang_code,$country_num);

        $admin_name = '';
        $r_admin = AdminModel::where(['recycle'=>0,'token'=>$accessId])->field('name')->select()->toArray();
        if($r_admin)
        {
            $admin_name = $r_admin[0]['name'];
        }
        $time = date("Y-m-d H:i:s");

        if($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 数据名称不能为空！';
            $this->Log($Log_content);
            $message = Lang('dic.18');
            return output(109,$message);
        }
        else
        {
            $r0 = SkuModel::where(['name'=>$name,'lang_code'=>$lang_code,'type'=>1,'recycle'=>0])->field('id,name')->select()->toArray();
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 数据名称'.$name.'已存在！';
                $this->Log($Log_content);
                $message = Lang('dic.19');
                return output(109,$message);
            }
        }
        $code = substr(strtoupper(Tools::encode($name)),0);
        $rew = 'LKT_' . $code . '_' . 0;

        $Tools = new Tools( $store_id, 0);
        $rew = $Tools->attribute_code($rew);

        $sql1 = array('store_id'=>'0','sid'=>0,'code'=>$rew,'name'=>$name,'status'=>$status,'type'=>1,'admin_name'=>$admin_name,'add_date'=>$time,'is_examine'=>1,'lang_code'=>$lang_code,'country_num'=>$country_num);
        $r1 = Db::name('sku')->insertGetId($sql1);
        if($r1 <= 0 )
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 添加属性名称失败！参数:'.json_encode($sql1);
            $this->Log($Log_content);
            $message = Lang('dic.3');
            return output(109,$message);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ .' 添加属性名称成功 ';
            $this->Log($Log_content);
            $message = Lang('Success');
            return output(200,$message);
        }
    }

    // 添加SKU
    public function addSku()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $type = addslashes(trim(Request::param('type'))); // 0.添加 1.编辑
        $attribute_id = addslashes(trim(Request::param('sid'))); // 属性名ID
        $lang_code = addslashes(trim(Request::param('lang_code'))); // 语言
        $country_num = addslashes(trim(Request::param('country_num'))); // 国家代码
        $attribute_value = addslashes(trim(Request::param('attributeList'))); // 属性值

        Tools::National_Language($lang_code,$country_num);

        $admin_name = '';
        $r_admin = AdminModel::where(['recycle'=>0,'token'=>$accessId])->field('name')->select()->toArray();
        if($r_admin)
        {
            $admin_name = $r_admin[0]['name'];
        }
        $time = date("Y-m-d H:i:s");

        // 1.开启事务
        Db::startTrans();

        $attribute_value_list = explode(',',$attribute_value);

        if ($store_id == '')
        {
            $store_id = 0;
        }
        if($attribute_id == '' || $attribute_id == 0 )
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择属性名称！';
            $this->Log($Log_content);
            $message = Lang('dic.20');
            return output(109,$message);
        }
        else
        {
            $r0 = SkuModel::where(['id'=>$attribute_id,'type'=>1])->field('code,status')->select()->toArray();
            $code1 = $r0[0]['code'];
            $status1 = $r0[0]['status'];
        }
        $ylist = array(); // 原SKUID
        if(count($attribute_value_list) == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择填写属性值！';
            $this->Log($Log_content);
            $message = Lang('dic.21');
            return output(109,$message);
        }
        else
        {
            $r1 = SkuModel::where(['sid'=>$attribute_id,'recycle'=>0])->limit(1)->order('add_date','desc')->order('id','desc')->field('code')->select()->toArray();
            if($r1)
            {
                $code = $r1[0]['code'];
                $num = trim(strrchr($code, '_'),'_');
                $num = $num + 1;
                if($type == 1)
                {
                    // 查询所有属性值ID
                    $r2 = SkuModel::where(['sid'=>$attribute_id,'recycle'=>0])->order('add_date','desc')->field('id')->select()->toArray();
                    if($r2)
                    {
                        foreach ($r2 as $k => $v)
                        {
                            $ylist[] = $v['id']; // 原SKUID
                        }
                    }
                }
            }
            else
            {
                $num = 1;
            }

            $list = array();
            $add = array();
            foreach ($attribute_value_list as $k => $v)
            {
                // 根据属性名ID和属性值，查询SKUID
                $r2 = SkuModel::where(['sid'=>$attribute_id,'recycle'=>0,'name'=>$v])->field('id')->select()->toArray();
                if($r2)
                { // 存在
                    if($type == 1)
                    { // 编辑
                        foreach($ylist as $yk => $yv)
                        {
                            if($r2[0]['id'] == $yv)
                            {
                                unset($ylist[$yk]);
                            }
                        }
                    }
                    else
                    { // 添加
                        $Log_content = __METHOD__ . '->' . __LINE__ . ' 属性值:'.$v.'重复！';
                        $this->Log($Log_content);
                        $message = Lang('dic.22');
                        return output(109,$message);
                    }
                }
                else
                { // 不存在，添加
                    $add[] = $v;
                }

                if(in_array($v,$list))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 属性值:'.$v.'重复！';
                    $this->Log($Log_content);
                    $message = Lang('dic.22');
                    return output(109,$message);
                }
                else
                {
                    if($v != '')
                    {
                        $list[] = $v;
                    }
                }
            }

            if($add != array())
            {
                foreach ($add as $k => $v)
                {
                    if($v != '')
                    {
                        $num1 = $num + $k;
                        $numlength = strlen((string)$num1);
                        if($numlength > 3)
                        {
                            $res = $num1;
                        }
                        else
                        {
                            $res = sprintf("%03d", $num1);
                        }
                        $rew = $code1 . '_' . $res ;
    
                        $sql3 = array('store_id'=>'0','sid'=>$attribute_id,'code'=>$rew,'name'=>$v,'status'=>$status1,'type'=>2,'admin_name'=>$admin_name,'add_date'=>$time,'is_examine'=>1,'lang_code'=>$lang_code,'country_num'=>$country_num);
                        $r3 = Db::name('sku')->insertGetId($sql3);
                        if($r3 <= 0)
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ .' 添加属性值失败！参数:'.json_encode($sql3);
                            $this->Log($Log_content);
                            Db::rollback();
                            $message = Lang('dic.3');
                            return output(109,$message);
                        }
                    }
                }
            }

            if($ylist != array())
            {
                foreach ($ylist as $ke => $va)
                {
                    $sql1 = array('recycle'=>'0','sid'=>$attribute_id,'id'=>$va);
                    $r1 = Db::name('sku')->where($sql1)->update(['recycle' => '1']);
                    if($r1 == -1)
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ .' 修改数据失败！参数:'.json_encode($sql1);
                        $this->Log($Log_content);
                        Db::rollback();
                        $message = Lang('dic.2');
                        return output(109,$message);
                    }
                    else
                    {
                        $Log_content = __METHOD__ . '->' . __LINE__ .' 修改数据成功 ';
                        $this->Log($Log_content);
                    }
                }
            }

            $Log_content = __METHOD__ . '->' . __LINE__ .' 添加属性值成功 ';
            $this->Log($Log_content);
            Db::commit();
            $message = Lang('添加成功！');
            return output(200,$message);
        }
	    return;
	}

    // 是否生效
    public function setSkuSwitch()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $id = addslashes(trim(Request::param('id'))); // 属性ID

        $time = date("Y-m-d H:i:s");
        // 1.开启事务
        Db::startTrans();

        $list = array();
        $list[] = $id;
        $sid = 0;

        $r0 = SkuModel::where(['id'=>$id,'recycle'=>0])->field('sid,name,status')->select()->toArray();
        if($r0)
        {
            $sid = $r0[0]['sid'];
            $name = $r0[0]['name'];
            $status = $r0[0]['status'];
        }

        if($status == 1)
        { // 当为 开启 改为 关闭
            $attribute_status = true;
            // $r_0 = ConfigureModel::where(['recycle'=>0])->field('id,attribute')->select()->toArray();
            // if($r_0)
            // {
            //     foreach ($r_0 as $k => $v)
            //     {
            //         if ($v['attribute'] != '')
            //         {
            //             $arrar_t = unserialize($v['attribute']);
            //             if(is_array($arrar_t))
            //             {
            //                 foreach ($arrar_t as $key => $value)
            //                 {
            //                     $key_t = explode('_',$key);
            //                     $key_t0 = $key_t[2];
            //                     if($id == $key_t0)
            //                     {
            //                         $attribute_status = false;
            //                         break 2;
            //                     }
            //                 }
            //             }
            //         }
            //     }
            // }

            // if(!$attribute_status)
            // {
            //     $Log_content = __METHOD__ . '->' . __LINE__ .' 该sku正在使用，不能关闭！';
            //     $this->Log($Log_content);
            //     $message = Lang('dic.23');
            //     return output(109,$message);
            // }

            if($sid == 0)
            {
                $r1 = SkuModel::where(['sid'=>$id,'recycle'=>0])->field('id')->select()->toArray();
                if($r1)
                {
                    foreach ($r1 as $key => $val)
                    {
                        $list[] = $val['id'];
                    }
                }
            }
            foreach ($list as $k => $v)
            {
                $sql2 = array('recycle'=>0,'id'=>$v);
                $r2 = Db::name('sku')->where($sql2)->update(['status' => '0']);
                if($r2 < 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ .' 关闭SKU数据失败！参数:'.json_encode($sql2);
                    $this->Log($Log_content);
                    Db::rollback();
                    $message = Lang('dic.6');
                    return output(109,$message);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ .' 关闭SKU数据ID为'.$id.'成功 ';
                    $this->Log($Log_content);
                }
            }
            Db::commit();
            $message = Lang('dic.7');
            return output(200,$message);
        }
        else
        { // 当为 关闭 改为 开启
            $sql2 = array('recycle'=>0,'id'=>$id);
            $r2 = Db::name('sku')->where($sql2)->update(['status' => '1']);
            if($r2 <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 开启SKU数据失败！参数:'.json_encode($sql2);
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('dic.4');
                return output(109,$message);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 开启SKU数据ID为'.$id.'成功 ';
                $this->Log($Log_content);
                Db::commit();
                $message = Lang('dic.5');
                return output(200,$message);
            }
        }
    }

    // 删除SKU
    public function delSku()
    {
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $id = addslashes(trim(Request::param('idList'))); // 属性ID

        $time = date("Y-m-d H:i:s");
        // 1.开启事务
        Db::startTrans();

        $id = trim($id,',');
        $id_list = explode(',',$id);
        $list = array();

        foreach ($id_list as $k => $v)
        {
            $list[] = $v;
            $sid = 0;
            $r0 = SkuModel::where(['id'=>$v,'recycle'=>0])->field('sid,status')->select()->toArray();
            if($r0)
            {
                $sid = $r0[0]['sid'];
                $status = $r0[0]['status']; // 是否生效 0:不是 1:是
                if($status == 1)
                {
                    Db::rollback();
                    $message = Lang('dic.24');
                    return output(109,$message);
                }
            }
            if($sid == 0)
            {
                $r1 = SkuModel::where(['sid'=>$v,'recycle'=>0])->field('id,status')->select()->toArray();
                if($r1)
                {
                    foreach ($r1 as $key => $val)
                    {
                        $status = $val['status']; // 是否生效 0:不是 1:是
                        if($status == 1)
                        {
                            Db::rollback();
                            $message = Lang('dic.24');
                            return output(109,$message);
                        }
                        $list[] = $val['id'];
                    }
                }
            }
        }

        foreach ($list as $k => $v)
        {
            $sql2 = array('recycle'=>0,'id'=>$v);
            $r2 = Db::name('sku')->where($sql2)->update(['recycle' => '1']);
            if($r2 <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ .' 删除SKU数据失败！sql:'.$sql2;
                $this->Log($Log_content);
                Db::rollback();
                $message = Lang('dic.9');
                return output(109,$message);
            }
        }
        Db::commit();
        $message = Lang('Success');
        return output(200,$message);
    }

    // 获取SKU
    public function getSkuList()
    {	
        $store_id = addslashes(Request::param('storeId'));
    	$storeType = addslashes(Request::param('storeType'));
    	$accessId = addslashes(Request::param('accessId'));

        $page = addslashes(trim(Request::param('pageNo')));
        $keyword = addslashes(trim(Request::param('keyword')));
        $strArr = Request::param('strArr');
        $lang_code = addslashes(trim(Request::param('lang_code'))); // 语种
        $lang_code = Tools::get_lang($lang_code);

        if($strArr == '')
        {
            $strArr = array();
        }
        else
        {
            $strArr = json_decode($strArr,true);
        }
        $total = 0;
        $list = array();

        $product = new Product();
        $list = $product->get_attribute($store_id,$page,$keyword,$strArr,$lang_code);
        $message = Lang('Success');
        return output(200,$message,json_decode($list));
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("saas/dic.log",$Log_content);
        return;
    }
}
