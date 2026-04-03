<?php

namespace app\common;

use think\facade\Db;
use app\common\Jurisdiction;
use app\common\LaiKeLogUtils;
use app\common\Tools;

class ExpressPublicMethod
{
    // 物流列表
    public function logistics_list($array)
    {   
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $name = $array['name'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $pagesize = $pagesize ? $pagesize : 10;

        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $condition = " a.store_id = '$store_id' and a.recovery = 0 and a.mch_id = '$mch_id' and b.recycle = 0 ";
        if($name != '')
        {
            $name_0 = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and (b.kuaidi_name like $name_0 or b.type like $name_0) ";
        }
        $list = array();
        $total = 0;

        $sql0 = "select count(a.id) as num from lkt_express_subtable as a left join lkt_express as b on a.express_id = b.id where $condition";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['num'];
        }
        
        $sql1 = "select a.*,b.kuaidi_name,b.type from lkt_express_subtable as a left join lkt_express as b on a.express_id = b.id where $condition order by add_time desc limit $start,$pagesize";
        $r1 = Db::query($sql1);
        if($r1)
        {
            $list = $r1;
        }
        
        $data = array('total' => $total,'list' => $list);
        $message = Lang('Success');
        echo json_encode(array("code" => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 获取物流主表数据
    public function get_logistics($array)
    {   
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id'];
        $list = array();

        $express_id_list = array();
        $sql_ = "select express_id from lkt_express_subtable where store_id = '$store_id' and mch_id = '$mch_id' and recovery = 0 ";
        $r_ = Db::query($sql_);
        if($r_)
        {
            foreach($r_ as $k => $v)
            {
                if($id != '')
                {
                    if($id != $v['express_id'])
                    {
                        $express_id_list[] = $v['express_id'];
                    }
                }
                else
                {
                    $express_id_list[] = $v['express_id'];
                }
            }
        }

        $express_id_str = implode(',',$express_id_list);
        if($express_id_str != '')
        {
            $sql0 = "select id,kuaidi_name from lkt_express where recycle = 0 and is_open = 1 and id not in ($express_id_str) order by sort desc ";
        }
        else
        {
            $sql0 = "select id,kuaidi_name from lkt_express where recycle = 0 and is_open = 1 order by sort desc ";
        }

        $r0 = Db::query($sql0);
        if($r0)
        {
            $list = $r0;
        }
        
        $data = array('list' => $list);
        $message = Lang('Success');
        echo json_encode(array("code" => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 添加快递公司子表
    public function add_logistics($array)
    {   
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $express_id = $array['express_id']; // 主表ID
        $partnerId = $array['partnerId']; // 电子面单客户账户或月结账号
        $partnerKey = $array['partnerKey']; // 电子面单密码
        $partnerSecret = $array['partnerSecret']; // 电子面单密钥
        $partnerName = $array['partnerName']; // 电子面单客户账户名称
        $net = $array['net']; // 收件网点名称
        $code = $array['code']; // 电子面单承载编号
        $checkMan = $array['checkMan']; // 电子面单承载快递员名
        $source = $array['source'];
        $admin_name = $array['admin_name']; // 操作人名称

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        if($express_id == '' || $express_id == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择快递公司！';
            $this->Log($Log_content);
            $message = Lang('express.7');
            echo json_encode(array("code" => 109,'message' => $message));
            exit;
        }
        else
        {
            $sql0 = "select * from lkt_express_subtable where store_id = '$store_id' and mch_id = '$mch_id' and express_id = '$express_id' and recovery = 0 ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '请勿重复添加！';
                $this->Log($Log_content);
                $message = Lang('express.8');
                echo json_encode(array("code" => 109,'message' => $message));
                exit;
            }
        }

        if($partnerId == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 电子面单客户账户或月结账号不能为空！';
            $this->Log($Log_content);
            $message = Lang('express.9');
            echo json_encode(array("code" => 109,'message' => $message));
            exit;
        }
        $data_insert = array('store_id'=>$store_id,'mch_id'=>$mch_id,'express_id'=>$express_id,'partnerId'=>$partnerId,'partnerKey'=>$partnerKey,'partnerSecret'=>$partnerSecret,'partnerName'=>$partnerName,'net'=>$net,'code'=>$code,'checkMan'=>$checkMan,'add_time'=>$time,'recovery'=>0);
        $r1 = Db::name('express_subtable')->insertGetId($data_insert);
        if($r1 > 0)
        {
            $Jurisdiction->admin_record($store_id, $admin_name, ' 添加成功！ID为'.$r1, 1,$source,$mch_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array("code" => 200,'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $admin_name, ' 添加失败！', 1,$source,$mch_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加失败，请重试！添加参数：' . json_encode($data_insert);
            $this->Log($Log_content);
            $message = Lang('express.4');
            echo json_encode(array("code" => 109,'message' => $message));
            exit;
        }
    }

    // 编辑快递公司子表页面
    public function edit_logistics_page($array)
    {   
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // id

        $list = array();

        $sql0 = "select * from lkt_express_subtable where store_id = '$store_id' and mch_id = '$mch_id' and id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $list = $r0[0];
        }

        $data = array('list' => $list);
        $message = Lang('Success');
        echo json_encode(array("code" => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 编辑快递公司子表
    public function edit_logistics($array)
    {   
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $express_id = $array['express_id']; // 主表ID
        $partnerId = $array['partnerId']; // 电子面单客户账户或月结账号
        $partnerKey = $array['partnerKey']; // 电子面单密码
        $partnerSecret = $array['partnerSecret']; // 电子面单密钥
        $partnerName = $array['partnerName']; // 电子面单客户账户名称
        $net = $array['net']; // 收件网点名称
        $code = $array['code']; // 电子面单承载编号
        $checkMan = $array['checkMan']; // 电子面单承载快递员名
        $source = $array['source'];
        $admin_name = $array['admin_name']; // 操作人名称
        $id = $array['id']; // id

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        if($express_id == '' || $express_id == 0)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 请选择快递公司！';
            $this->Log($Log_content);
            $message = Lang('express.7');
            echo json_encode(array("code" => 109,'message' => $message));
            exit;
        }
        else
        {
            $sql0 = "select * from lkt_express_subtable where store_id = '$store_id' and mch_id = '$mch_id' and express_id = '$express_id' and recovery = 0 and id != '$id' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '请勿重复添加！';
                $this->Log($Log_content);
                $message = Lang('express.8');
                echo json_encode(array("code" => 109,'message' => $message));
                exit;
            }
        }

        if($partnerId == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 电子面单客户账户或月结账号不能为空！';
            $this->Log($Log_content);
            $message = Lang('express.9');
            echo json_encode(array("code" => 109,'message' => $message));
            exit;
        }

        $data_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
        $data_update = array('express_id'=>$express_id,'partnerId'=>$partnerId,'partnerKey'=>$partnerKey,'partnerKey'=>$partnerKey,'partnerSecret'=>$partnerSecret,'partnerName'=>$partnerName,'net'=>$net,'code'=>$code,'checkMan'=>$checkMan);
        $r1 = Db::name('express_subtable')->where($data_where)->update($data_update);
        if($r1 > 0)
        {
            $Jurisdiction->admin_record($store_id, $admin_name, ' 编辑成功！ID为'.$id, 2,$source,$mch_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 编辑成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array("code" => 200,'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $admin_name, ' 编辑失败！', 2,$source,$mch_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 编辑失败，请重试！条件参数：' . json_encode($data_where) . '，编辑参数：' . json_encode($data_update);
            $this->Log($Log_content);
            $message = Lang('express.5');
            echo json_encode(array("code" => 109,'message' => $message));
            exit;
        }
    }

    // 删除快递公司子表
    public function del_logistics($array)
    {   
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $id = $array['id']; // id
        $source = $array['source'];
        $admin_name = $array['admin_name']; // 操作人名称

        $list = array();
        $Jurisdiction = new Jurisdiction();

        $sql0 = "select * from lkt_express_subtable where store_id = '$store_id' and mch_id = '$mch_id' and id = '$id' ";
        $r0 = Db::query($sql0);
        if(!$r0)
        {
            $message = Lang('Parameter error');
            echo json_encode(array("code" => 109,'message' => $message));
            exit;
        }

        $sql1 = "update lkt_express_subtable set recovery = 1 where store_id = '$store_id' and mch_id = '$mch_id' and id = '$id' ";
        $r1 = Db::execute($sql1);
        if($r1 > 0)
        {
            $Jurisdiction->admin_record($store_id, $admin_name, ' 删除快递公司子表数据成功！ID为'.$id, 3,$source,$mch_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除成功！';
            $this->Log($Log_content);
            $message = Lang('Success');
            echo json_encode(array("code" => 200,'message' => $message));
            exit;
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $admin_name, ' 删除快递公司子表数据失败！ID为'.$id, 3,$source,$mch_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除失败，请重试！sql：' . $sql1;
            $this->Log($Log_content);
            $message = Lang('express.6');
            echo json_encode(array("code" => 109,'message' => $message));
            exit;
        }
    }

    // 获取物流信息
    public function GetLogistics($array)
    {   
        $store_id = $array['store_id'];
        $sNo = $array['sNo']; 

        $list = array();

        $mch_id = 0;
        $sql0 = "select mch_id from lkt_order where sNo = '$sNo' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $mch_id = trim($r0[0]['mch_id'],',');
        }

        $sql0 = "select * from lkt_express where is_open = 1 and recycle = 0 order by sort desc ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k => $v)
            {
                $logistics_type = false; // false：获取lkt_express  true：获取lkt_express_subtable

                $express_id = $v['id'];

                if($mch_id != 0)
                {
                    $sql1 = "select * from lkt_express_subtable where store_id = '$store_id' and mch_id = '$mch_id' and recovery = 0 and express_id = '$express_id' ";
                    $r1 = Db::query($sql1);
                    if($r1)
                    {
                        $logistics_type = true; // false：获取lkt_express  true：获取lkt_express_subtable
                    }
                }

                // $v['logistics_type'] = $logistics_type;
                $v['logistics_type'] = false; // 17track用
                $list[] = $v;
            }
        }

        // $list = array();
        // if($logistics_type)
        // {
        //     $sql1 = "select b.* from lkt_express_subtable as a left join lkt_express as b on a.express_id = b.id where a.store_id = '$store_id' and a.recovery = 0 and a.mch_id = '$mch_id' and b.is_open = 1 and b.recycle = 0 order by b.sort desc ";
        // }
        // else
        // {
        //     $sql1 = "select * from lkt_express where is_open = 1 and recycle = 0 order by sort desc ";
        // }
        // $r1 = Db::query($sql1);
        // if($r1)
        // {
        //     $list = $r1;
        // }

        $total = count($list);

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        echo json_encode(array("code" => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("common/Express.log",$Log_content);
        return;
    }
}

?>