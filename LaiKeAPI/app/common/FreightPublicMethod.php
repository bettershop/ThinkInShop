<?php

namespace app\common;

use think\facade\Db;
use app\common\Jurisdiction;
use app\common\LaiKeLogUtils;

use app\admin\model\ProductListModel;
use app\admin\model\FreightModel;
use app\admin\model\AdminCgGroupModel;

class FreightPublicMethod
{
    // 运费列表
    public function freight_list($data)
    {
        $store_id = $data['store_id'];
        $mch_id = $data['mch_id'];
        $supplier_id = 0;
        if(isset($data['supplier_id']))
        {
            $supplier_id = $data['supplier_id'];
        }
        $status = $data['status'];
        $type = $data['type'];
        $name = $data['name'];
        $page = $data['pageNo'];
        $pagesize = $data['pageSize'];
        $lang_code = "";
        if(isset($data['lang_code']) && $data['lang_code'] != '')
        {
            $lang_code = Tools::get_lang($data['lang_code']);
        }

        $list = array();
        $pagesize = $pagesize ? $pagesize : 10;
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $freight_str = '';
        $freight_list = array();
        if($supplier_id == 0)
        {
            $r0_0 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'recycle'=>0])->field('freight')->select()->toArray();
        }
        else
        {
            $r0_0 = ProductListModel::where(['store_id'=>$store_id,'gongyingshang'=>$supplier_id,'recycle'=>0])->field('freight')->select()->toArray();
        }
        
        if($r0_0)
        {
            foreach ($r0_0 as $k => $v)
            {
                if($v['freight'] != '')
                {
                    if(!in_array($v['freight'],$freight_list))
                    {
                        $freight_list[] = $v['freight'];
                    }
                }
            }
        }
        $freight_str = implode(',',$freight_list);
        if($supplier_id == 0)
        {
            $condition = " store_id = '$store_id' and mch_id = '$mch_id' ";
        }
        else
        {
            $condition = " store_id = '$store_id' and supplier_id = '$supplier_id' ";
        }

        if($lang_code != '')
        {
            $condition .= " and lang_code = '$lang_code' ";
        }

        if ($status == '')
        { // 查询所有
        
        }
        elseif ($status == '1')
        { // 查询使用中
            $condition .= " and id in ($freight_str) ";
        }
        else if ($status == '0')
        { // 查询未使用
            $condition .= " and id not in ($freight_str) ";
        }
        if ($type != '' && $type != 'undefined')
        {
            $condition .= " and type = '$type' ";
        }
        if ($name != '')
        {
            $name = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and name like $name ";
        }
       
        $total = 0;
        $r0 = FreightModel::where($condition)->field('count(id) as total')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $r1 = FreightModel::where($condition)->limit($start,$pagesize)->order('is_default','desc')->order('add_time','desc')->select()->toArray();
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $v['country_name'] = Tools::get_country_name($v['country_num']);
                $v['lang_name'] = Tools::get_lang_name($v['lang_code']);
                $id = $v['id'];
                $r2 = ProductListModel::where(['store_id'=>$store_id,'freight'=>$id,'recycle'=>0])->field('freight')->select()->toArray();
                if ($r2)
                {
                    $v['is_use'] = 1;
                }
                else
                {
                    $v['is_use'] = 2;
                }
                
                $default_freight = json_decode($v['default_freight'],true);
                $v['default_freight'] = json_decode($v['default_freight'],true);
                $v['freight'] = json_decode($v['freight'],true);
                $v['no_delivery'] = json_decode($v['no_delivery'],true);

                $rule = "";
                $rule1 = "";
                if($v['type'] == 1)
                { // 重量
                    $rule = $default_freight['num1'] . "千克内" . $default_freight['num2'] . "元" . PHP_EOL;
                    if($default_freight['num3'] != 0)
                    {
                        $rule1 = "每加" . $default_freight['num3'] . "千克，加" . $default_freight['num4'] . "元";
                    }
                }
                else
                { // 件
                    $rule = $default_freight['num1'] . "件内" . $default_freight['num2'] . "元" . PHP_EOL;
                    if($default_freight['num3'] != 0)
                    {
                        $rule1 = "每加" . $default_freight['num3'] . "件，加" . $default_freight['num4'] . "元";
                    }
                }
                $v['rule'] = $rule;
                $v['rule1'] = $rule1;
                $list[] = $v;
            }
        }
        $data = array('total'=>$total,'list'=>$list,'start'=>$start,'package_settings'=>$pagesize);
        $message = Lang('Success');
        echo json_encode(array("code" => 200,'message' => $message,'data' => $data));
        exit;
    }
    
    // 运费列表
    public function freight_list_0($data)
    {
        $store_id = $data['store_id'];
        $mch_id = $data['mch_id'];
        if(empty($mch_id))
        {
            $supplier_id = $data['supplier_id'];
        }
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = Tools::get_lang($array['lang_code']);
        }
        $status = $data['status'];
        $type = $data['type'];
        $name = $data['name'];
        $page = $data['pageNo'];
        $pagesize = $data['pageSize'];
        $list = array();
        $pagesize = $pagesize ? $pagesize : 10;
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $freight_str = '';
        $freight_list = array();
        if($mch_id)
        {
            $r0_0 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code,'recycle'=>0])->field('freight')->select()->toArray();
        }
        else
        {
            $r0_0 = ProductListModel::where(['store_id'=>$store_id,'supplier_id'=>$supplier_id,'lang_code'=>$lang_code,'recycle'=>0])->field('freight')->select()->toArray();
        }
        
        if($r0_0)
        {
            foreach ($r0_0 as $k => $v)
            {
                if($v['freight'] != '')
                {
                    if(!in_array($v['freight'],$freight_list))
                    {
                        $freight_list[] = $v['freight'];
                    }
                }
            }
        }
        $freight_str = implode(',',$freight_list);
        if($mch_id)
        {
            $condition = " store_id = '$store_id' and mch_id = '$mch_id' ";
        }
        else
        {
            $condition = " store_id = '$store_id' and supplier_id = '$supplier_id' ";
        }

        if($lang_code != '')
        {
            $condition .= " and lang_code = '$lang_code' ";
        }
        if ($status == '')
        { // 查询所有
        
        }
        elseif ($status == '1')
        { // 查询使用中
            $condition .= " and id in ($freight_str) ";
        }
        else if ($status == '0')
        { // 查询未使用
            $condition .= " and id not in ($freight_str) ";
        }
        if ($type != '' && $type != 'undefined')
        {
            $condition .= " and type = '$type' ";
        }
        if ($name != '')
        {
            $name = Tools::FuzzyQueryConcatenation($name);
            $condition .= " and name like $name ";
        }
       
        $total = 0;
        $r0 = FreightModel::where($condition)->field('count(id) as total')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $r1 = FreightModel::where($condition)->limit($start,$pagesize)->order('is_default','desc')->order('add_time','desc')->select()->toArray();
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $id = $v['id'];
                $r2 = ProductListModel::where(['store_id'=>$store_id,'freight'=>$id,'recycle'=>0])->field('freight')->select()->toArray();
                if ($r2)
                {
                    $v['is_use'] = 1;
                }
                else
                {
                    $v['is_use'] = 2;
                }
                
                $list[] = $v;
            }
        }

        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        echo json_encode(array("code" => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 添加运费
    public function preserve($data)
    {
        $store_id = $data['store_id']; // 商城ID
        $mch_id = $data['mch_id']; // 店铺ID
        $fid = $data['fid']; // 运费ID
        $lang_code = "";
        if(isset($data['lang_code']))
        {
            $lang_code = Tools::get_lang($data['lang_code']);
        }
        $country_num = "";
        if(isset($data['country_num']))
        {
            $country_num = $data['country_num'];
        }
        $name = $data['name']; // 模板名称
        $type = $data['type']; // 类型 0:件 1:重量
        if(empty($mch_id))
        {
            $supplier_id = $data['supplier_id'];
        }
        $default_freight = $data['default_freight']; // 默认运费
        $hidden_freight = $data['hidden_freight']; // 指定运费
        $no_delivery = $data['no_delivery']; // 不配送地区
        $threeIdsList = '';
        if(isset($data['threeIdsList']))
        {
            $threeIdsList = $data['threeIdsList']; // 不配送地区ID
        }
        $source = '';
        if(isset($data['source']))
        {
            $source = $data['source'];
        }
        $operator_id = '';
        if(isset($data['operator_id']))
        {
            $operator_id = $data['operator_id'];
        }
        $operator = '';
        if(isset($data['operator']))
        {
            $operator = $data['operator'];
        }

        $is_default = 0;
       
        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();

        if ($name == '')
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 模板名称不能为空，请输入模板名称';
            $this->freightLog($Log_content);
            $message = Lang('freight.0');
            echo json_encode(array("code" => 109,'message' => $message));
            exit;
        }
        else
        {   
            if($mch_id)
            {
                if($fid != '' && $fid != 0)
                {
                    $r = FreightModel::where(['store_id'=>$store_id,'name'=>$name,'mch_id'=>$mch_id,'lang_code'=>$lang_code,'country_num'=>$country_num])->where('id','<>',$fid)->select()->toArray();
                }
                else
                {
                    $r = FreightModel::where(['store_id'=>$store_id,'name'=>$name,'mch_id'=>$mch_id,'lang_code'=>$lang_code,'country_num'=>$country_num])->select()->toArray();
                }
            }
            else
            {
                if($fid != '' && $fid != 0)
                {
                    $r = FreightModel::where(['store_id'=>$store_id,'name'=>$name,'supplier_id'=>$supplier_id,'lang_code'=>$lang_code,'country_num'=>$country_num])->where('id','<>',$fid)->select()->toArray();
                }
                else
                {
                    $r = FreightModel::where(['store_id'=>$store_id,'name'=>$name,'mch_id'=>$mch_id,'supplier_id'=>$supplier_id,'lang_code'=>$lang_code,'country_num'=>$country_num])->select()->toArray();
                }
            }

            if ($r)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . " 模板名称{$name}已经存在，请选用其他名称！";
                $this->freightLog($Log_content);
                $message = Lang('freight.1');
                echo json_encode(array("code" => 4003, 'message' => $message));
                exit;
            }
        }

        if($default_freight == '')
        {
            $message = Lang('freight.29');
            echo json_encode(array("code" => 109,'message' => $message));
            exit;
        }

        $hidden_freight_list = array(); // 指定运费数组
        if($hidden_freight != '')
        {
            $hidden_freight_list = json_decode($hidden_freight,true);
        }

        $address_list = array(); // 指定运费城市数组
        if(count($hidden_freight_list) > 0)
        { // 有指定运费
            foreach ($hidden_freight_list as $k0 => $v0)
            {
                $name_list = explode(',',$v0['name']); // 城市数组
                foreach ($name_list as $k1 => $v1)
                {
                    if(in_array($v1,$address_list))
                    { // 存在
                        $message = Lang('freight.30');
                        echo json_encode(array("code" => 109,'message' => $message));
                        exit;
                    }
                    else
                    { // 不存在
                        $address_list[] = $v1;
                    }
                }
            }
        }

        $is_no_delivery = 0;
        $no_delivery_list = array(); // 不配送城市数组
        if($no_delivery != '')
        {
            $no_delivery_list = json_decode($no_delivery,true);
            $is_no_delivery = 1;
        }

        if(count($no_delivery_list) > 0)
        {
            foreach ($no_delivery_list as $k2 => $v2)
            {
                if(in_array($v2,$address_list))
                { // 存在
                    $message = Lang('freight.31');
                    echo json_encode(array("code" => 109,'message' => $message));
                    exit;
                }
            }
        }

        $is_default = 0;
        if(isset($data['is_default']))
        {
            $is_default = $data['is_default']; // 是否默认
        }
        
        if($is_default == 1)
        {   
            if($mch_id)
            {
                $data_where_1 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code);
            }
            else
            {
                $data_where_1 = array('store_id'=>$store_id,'supplier_id'=>$supplier_id,'lang_code'=>$lang_code);
            }
            $data_update_1 = array('is_default'=>0);
            $r_1 = Db::name('freight')->where($data_where_1)->update($data_update_1);
        }
        else
        {
            // 根据商城ID、店铺ID，查询运费信息
            if($mch_id)
            {
                $r_0 = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code])->field('id')->select()->toArray();
            }
            else
            {
                $r_0 = FreightModel::where(['store_id'=>$store_id,'supplier_id'=>$supplier_id,'lang_code'=>$lang_code])->field('id')->select()->toArray();
            }
            if($r_0)
            { // 存在
                $is_default = 0;
            }
            else
            { // 不存在
                $is_default = 1;
            }
        }

        if($fid != '' && $fid != 0)
        {   
            if($mch_id)
            {
                $data_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$fid);
            }
            else
            {
                $data_where = array('store_id'=>$store_id,'supplier_id'=>$supplier_id,'id'=>$fid);
            }
            if(isset($data['is_default']))
            {
                $data_update = array('lang_code'=>$lang_code,'country_num'=>$country_num,'name'=>$name,'type'=>$type,'freight'=>$hidden_freight,'is_default'=>$is_default,'is_no_delivery'=>$is_no_delivery,'no_delivery'=>$no_delivery,'default_freight'=>$default_freight,'threeIdsList'=>$threeIdsList);
            }
            else
            {
                $data_update = array('lang_code'=>$lang_code,'country_num'=>$country_num,'name'=>$name,'type'=>$type,'freight'=>$hidden_freight,'is_no_delivery'=>$is_no_delivery,'no_delivery'=>$no_delivery,'default_freight'=>$default_freight,'threeIdsList'=>$threeIdsList);
            }
            $r = Db::name('freight')->where($data_where)->update($data_update);
            if ($r == -1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了运费模板ID:'.$fid.' 的信息失败',2,$source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改运费规则失败！参数：' . json_encode($data_where);
                $this->freightLog($Log_content);
                $message = Lang('freight.20');
                echo json_encode(array("code" => 109, 'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '修改了运费模板ID:'.$fid.' 的信息',2,$source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改运费规则成功！';
                $this->freightLog($Log_content);
                $message = Lang('freight.19');
                echo json_encode(array("code" => 200, 'message' => $message));
                exit;
            }
        }
        else
        {
            // 添加规则
            if($mch_id)
            {
                $data_insert = array('store_id'=>$store_id,'lang_code'=>$lang_code,'country_num'=>$country_num,'name'=>$name,'type'=>$type,'freight'=>$hidden_freight,'is_default'=>$is_default,'add_time'=>$time,'mch_id'=>$mch_id,'is_no_delivery'=>$is_no_delivery,'no_delivery'=>$no_delivery,'default_freight'=>$default_freight,'threeIdsList'=>$threeIdsList);
            }
            else
            {
                $data_insert = array('store_id'=>$store_id,'lang_code'=>$lang_code,'country_num'=>$country_num,'name'=>$name,'type'=>$type,'freight'=>$hidden_freight,'is_default'=>$is_default,'add_time'=>$time,'supplier_id'=>$supplier_id,'is_no_delivery'=>$is_no_delivery,'no_delivery'=>$no_delivery,'default_freight'=>$default_freight,'threeIdsList'=>$threeIdsList);
            }
            $r_1 = Db::name('freight')->insertGetId($data_insert);
            if ($r_1)
            {
                $Jurisdiction->admin_record($store_id, $operator, ' 添加了运费模板ID：'.$r_1, 1,$source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加运费规则成功！';
                $this->freightLog($Log_content);
                $message = Lang('freight.16');
                echo json_encode(array("code" => 200, 'message' => $message));
                exit;
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '添加了运费模板失败！',1,$source,$mch_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 添加运费规则失败！参数：' . json_encode($data_insert);
                $this->freightLog($Log_content);
                $message = Lang('freight.17');
                echo json_encode(array("code" => 109, 'message' => $message));
                exit;
            }
        }
    }

    // 编辑运费-页面
    public function edit_page($data)
    {
        $store_id = $data['store_id'];
        $id = $data['fid'];
        $supplier_id = 0;
        if(isset($data['supplier_id']))
        {
            $supplier_id = $data['supplier_id'];
        }
        // 根据商城ID、运费ID，查询运费信息
        $r0 = FreightModel::where(['store_id'=>$store_id,'id'=>$id])->select()->toArray();
        if($r0)
        {
            if($r0[0]['freight'] != '')
            {
                $r0[0]['freight'] = json_decode($r0[0]['freight'],true); // 运费规则
            }

            if($r0[0]['no_delivery'] != '')
            {
                $r0[0]['no_delivery'] = json_decode($r0[0]['no_delivery'],true); // 不配送
            }
            
            if($r0[0]['default_freight'] != '')
            {
                $r0[0]['default_freight'] = json_decode($r0[0]['default_freight'],true); // 默认运费规则
            }

            if($supplier_id != 0)
            {
                $data = array('list'=>$r0,'package_settings'=>'','total'=>1);
            }
            else
            {
                $r0[0]['list'] = $r0[0]['freight'];
                $data = $r0[0];
            }
            $message = Lang('Success');
            echo json_encode(array('code' => 200, 'message' => $message,'data' => $data));
            exit;
        }
        else
        {
            $message = Lang('freight.18');
            echo json_encode(array("code" => 400, "message" => $message));
            exit;
        }
    }

    // 设置默认运费
    public function set_default($data)
    {
        $store_id = $data['store_id'];
        $admin_name = $data['admin_name'];
        $mch_id = $data['mch_id'];
        if(empty($mch_id))
        {
            $supplier_id = $data['supplier_id'];
        }
        
        $id = $data['id'];
        $source = '';
        if(isset($data['source']))
        {
            $source = $data['source'];
        }
        $operator_id = '';
        if(isset($data['operator_id']))
        {
            $operator_id = $data['operator_id'];
        }
        $operator = '';
        if(isset($data['operator']))
        {
            $operator = $data['operator'];
        }

        $Jurisdiction = new Jurisdiction();
        if($mch_id)
        {
            $r = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id])->field('id,is_default,lang_code')->select()->toArray();
        }
        else
        {
            $r = FreightModel::where(['store_id'=>$store_id,'supplier_id'=>$supplier_id,'id'=>$id])->field('id,is_default,lang_code')->select()->toArray();
        }
        if ($r)
        {
            $lang_code = $r[0]['lang_code']; // 语种
        }

        // 根据商城ID、店铺ID，查询运费信息
        if($mch_id)
        {
            $r0 = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code])->field('id,is_default')->select()->toArray();
        }
        else
        {
            $r0 = FreightModel::where(['store_id'=>$store_id,'supplier_id'=>$supplier_id,'lang_code'=>$lang_code])->field('id,is_default')->select()->toArray();
        }
        if ($r0)
        {
            if(count($r0) == 1)
            {
                $message = Lang('freight.21');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
            $y_id = 0; // 原来的默认运费ID
            foreach ($r0 as $k => $v)
            {
                if ($v['is_default'] == 1)
                {
                    $y_id = $v['id'];
                }
            }

            if ($y_id == $id)
            { // 原来的默认运费ID == 修改的运费ID（取消原来的默认运费，重新设置一个默认运费）
                if($mch_id)
                {
                    $r1 = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code])->where('id','<>',$id)->limit(1)->order('id','asc')->field('id')->select()->toArray();
                    if($r1)
                    {
                        $x_id = $r1[0]['id']; // 新的默认运费ID
                        // 根据商城ID、店铺ID、新默认的运费ID，修改运费信息
                        $sql_where2 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$x_id);
                        $sql_update2 = array('is_default'=>1);
                        $r2 = Db::name('freight')->where($sql_where2)->update($sql_update2);
                    }
                    $sql_where3 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
                    $sql_update3 = array('is_default'=>0);
                    $r3 = Db::name('freight')->where($sql_where3)->update($sql_update3);
                }
                else
                {
                    $r1 = FreightModel::where(['store_id'=>$store_id,'supplier_id'=>$supplier_id,'lang_code'=>$lang_code])->where('id','<>',$id)->limit(1)->order('id','asc')->field('id')->select()->toArray();
                    if($r1)
                    {
                        $x_id = $r1[0]['id']; // 新的默认运费ID
                        // 根据商城ID、店铺ID、新默认的运费ID，修改运费信息
                        $sql_where2 = array('store_id'=>$store_id,'supplier_id'=>$supplier_id,'id'=>$x_id);
                        $sql_update2 = array('is_default'=>1);
                        $r2 = Db::name('freight')->where($sql_where2)->update($sql_update2);
                    }
                    $sql_where3 = array('store_id'=>$store_id,'supplier_id'=>$supplier_id,'id'=>$id);
                    $sql_update3 = array('is_default'=>0);
                    $r3 = Db::name('freight')->where($sql_where3)->update($sql_update3);
                }
                if ($r3)
                {
                    $Jurisdiction->admin_record($store_id, $operator, '将运费模板ID：' . $id . '，设为默认', 2,$source,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改默认运费成功，ID为 ' . $id;
                    $this->freightLog($Log_content);
                    $message = Lang('freight.33');
                    echo json_encode(array('code' => 200, 'message' => $message));
                    exit;
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $operator, '将运费模板ID：' . $id . '，设为默认失败', 2,$source,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改默认运费失败，ID为 ' . $id;
                    $this->freightLog($Log_content);
                    $message = Lang('freight.23');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
            }
            else
            {   
                if($mch_id)
                {
                    $sql_where2 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code);
                    $sql_update2 = array('is_default'=>0);
                    $r2 = Db::name('freight')->where($sql_where2)->update($sql_update2);

                    $sql_where3 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$id);
                    $sql_update3 = array('is_default'=>1);
                    $r3 = Db::name('freight')->where($sql_where3)->update($sql_update3);
                }
                else
                {   
                    $sql_where2 = array('store_id'=>$store_id,'supplier_id'=>$supplier_id,'lang_code'=>$lang_code);
                    $sql_update2 = array('is_default'=>0);
                    $r2 = Db::name('freight')->where($sql_where2)->update($sql_update2);

                    $sql_where3 = array('store_id'=>$store_id,'supplier_id'=>$supplier_id,'id'=>$id);
                    $sql_update3 = array('is_default'=>1);
                    $r3 = Db::name('freight')->where($sql_where3)->update($sql_update3);
                }
                
                if ($r3 > 0 )
                {
                    $Jurisdiction->admin_record($store_id, $operator, '将运费模板ID：' . $id . '，设为默认成功', 2,$source,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改默认运费成功，ID为 ' . $id;
                    $this->freightLog($Log_content);
                    $message = Lang('freight.32');
                    echo json_encode(array('code' => 200, 'message' => $message));
                    exit;
                }
                else
                {
                    $Jurisdiction->admin_record($store_id, $operator, '将运费模板ID：' . $id . '，设为默认失败', 2,$source,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改默认运费失败，ID为 ' . $id;
                    $this->freightLog($Log_content);
                    $message = Lang('freight.23');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
            }
        }
        return;
    }

    // 删除运费
    public function freight_del($data)
    {
        $store_id = $data['store_id'];
        $admin_name = $data['admin_name'];
        $mch_id = $data['mch_id'];
        if(empty($mch_id))
        {
            $supplier_id = $data['supplier_id'];
        }
        $id = $data['id'];
        $source = '';
        if(isset($data['source']))
        {
            $source = $data['source'];
        }
        $operator_id = '';
        if(isset($data['operator_id']))
        {
            $operator_id = $data['operator_id'];
        }
        $operator = '';
        if(isset($data['operator']))
        {
            $operator = $data['operator'];
        }

        $Jurisdiction = new Jurisdiction();
        Db::startTrans();

        // 根据商城ID、店铺ID，查询剩余运费总数
        if($mch_id)
        {
            $r_total = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code])->field('count(id) as total')->select()->toArray();
        }
        else
        {
            $r_total = FreightModel::where(['store_id'=>$store_id,'supplier_id'=>$supplier_id,'lang_code'=>$lang_code])->field('count(id) as total')->select()->toArray();
        }
        $total = $r_total[0]['total'];

        $id_list = explode(',', $id); // 变成数组
        if($total == count($id_list))
        {
            $message = Lang('freight.21');
            echo json_encode(array('code' => 109, 'message' => $message));
            exit;
        }

        if (count($id_list) > 0)
        {
            $status = false; // 不是默认运费
            foreach ($id_list as $k => $v)
            {
                $sql_where1 = array('store_id'=>$store_id,'freight'=>$v);
                $sql_update1 = array('freight'=>0);
                $r1 = Db::name('product_list')->where($sql_where1)->update($sql_update1);
                
                if($mch_id)
                {
                    $r2 = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$v,'is_default'=>1])->field('id,lang_code')->select()->toArray();
                }
                else
                {
                    $r2 = FreightModel::where(['store_id'=>$store_id,'supplier_id'=>$supplier_id,'id'=>$v,'is_default'=>1])->field('id,lang_code')->select()->toArray();
                }
                
                if($r2)
                {
                    $lang_code = $r2[0]['lang_code']; // 语种
                    $status = true; // 是默认运费
                }
                // 根据商城ID、店铺ID、运费ID，删除运费信息
                if($mch_id)
                {
                    $sql_where3 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$v);
                }
                else
                {
                    $sql_where3 = array('store_id'=>$store_id,'supplier_id'=>$supplier_id,'id'=>$v);
                }
                
                $r3 = Db::table('lkt_freight')->where($sql_where3)->delete();
                if ($r3 > 0)
                {
                    $Jurisdiction->admin_record($store_id, $operator, '删除了运费模板ID：' . $v . '的信息', 3,$source,$mch_id,$operator_id);
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除规则ID为 ' . $v . ' 的信息';
                    $this->freightLog($Log_content);
                }
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除规则ID为' . $v . '失败';
                    $this->freightLog($Log_content);
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '删除了运费模板ID：' . $v . '的信息失败', 3,$source,$mch_id,$operator_id);
                    $message = Lang('freight.25');
                    echo json_encode(array('code' => 109, 'message' => $message));
                    exit;
                }
            }
            if($status)
            {   
                if($mch_id)
                {
                    $sql_where4 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'lang_code'=>$lang_code);
                }
                else
                {
                   $sql_where4 = array('store_id'=>$store_id,'supplier_id'=>$supplier_id,'lang_code'=>$lang_code); 
                }
                $sql_update4 = array('is_default'=>1);
                $r4 = Db::name('freight')->where($sql_where4)->limit(1)->order('id','asc')->update($sql_update4);
            }

            if($mch_id)
            {
                $r2 = FreightModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'is_default'=>1])->field('id')->select()->toArray();
            }
            else
            {
                $r2 = FreightModel::where(['store_id'=>$store_id,'supplier_id'=>$supplier_id,'is_default'=>1])->field('id')->select()->toArray();
            }
            if($r2)
            {
                $freight = $r2[0]['id']; // 默认运费ID

                $sql_where1 = array('store_id'=>$store_id,'freight'=>0);
                $sql_update1 = array('freight'=>$freight);
                $r1 = Db::name('product_list')->where($sql_where1)->update($sql_update1);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除规则失败,没有默认运费';
                $this->freightLog($Log_content);
                Db::rollback();
                $Jurisdiction->admin_record($store_id, $operator, '删除规则失败,没有默认运费', 3,$source,$mch_id,$operator_id);
                $message = Lang('freight.25');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
        }
        Db::commit();
        $message = Lang('freight.27');
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 获取城市信息
    public function cityInfo()
    {
        if(cache('AdminCgGroup'))
        {
            $list = cache('AdminCgGroup');
        }
        else
        {
            $list = array();
            $r0 = AdminCgGroupModel::where(['district_pid'=>0])->select()->toArray();
            if($r0)
            {
                foreach ($r0 as $k0 => $v0)
                {
                    $GroupID = $v0['id'];
                    $children = $this->GetCities($GroupID);
                    if($children != array())
                    {
                        $v0['children'] = $this->GetCities($GroupID);
                    }
                    $list[] = $v0;
                }
            }
            cache('AdminCgGroup', $list, 0);//添加新token数据
        }

        $data = array('list'=>$list);
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message, 'data' => $data));
        exit;
    }
    
    // 获取城市信息
    public function GetCities($GroupID)
    {
        $list = array();
        $r0 = AdminCgGroupModel::where(['district_pid'=>$GroupID])->select()->toArray();
        if($r0)
        {
            foreach ($r0 as $k0 => $v0)
            {
                $GroupID = $v0['id'];
                $children = $this->GetCities($GroupID);
                if($children != array())
                {
                    $v0['children'] = $this->GetCities($GroupID);
                }
                $list[] = $v0;
            }
        }
        return $list;
    }
    
    // 获取省
    public function get_sheng()
    {
        $list = array();
        $r0 = AdminCgGroupModel::where(['district_pid'=>0])->field('id,district_name')->select()->toArray();
        if($r0)
        {
            foreach ($r0 as $k0 => $v0)
            {
                $list[] = $v0;
            }
        }
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message, 'data' => $list));
        exit;
    }

    // 关联商品
    public function RelatedProducts($data)
    {
        $store_id = $data['store_id'];
        $mch_id = $data['mch_id'];
        $id = $data['id'];
        $operator_id = $data['operator_id'];
        $operator = $data['operator'];
        $operator_source = $data['operator_source'];
        $page = $data['page'];
        $pagesize = $data['pagesize'];

        $list = array();
        $total = 0;
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $sql0 = "select ifnull(count(a.id),0) as total from lkt_product_list as a left join lkt_mch as m on a.mch_id = m.id where a.store_id = '$store_id' and a.recycle = 0 and a.freight = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql1 = "select a.id,a.imgurl,a.product_title,a.initial,m.name as mch_name from lkt_product_list as a left join lkt_mch as m on a.mch_id = m.id where a.store_id = '$store_id' and a.recycle = 0 and a.freight = '$id' order by (a.volume + a.real_volume) desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl'], $store_id);
                $initial = $v['initial'];
                $initial = unserialize($initial);
                $v['price']= $initial['sj'];
                $list[] = $v;
            }
        }
        
        $data = array('total'=>$total,'list'=>$list);
        $message = Lang('Success');
        echo json_encode(array("code" => 200,'message' => $message,'data' => $data));
        exit;
    }

    // 日志
    public function freightLog($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("common/freight.log",$Log_content);
        return;
    }
}

?>