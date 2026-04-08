<?php

namespace app\common;

use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;

use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\RecordModel;
use app\admin\model\ProductListModel;
use app\admin\model\ConfigureModel;

class EditOrderStatus 
{
	public function __construct( $store_id, $store_type)
    {
        $this->store_id = $store_id;
        $this->store_type = $store_type;
        $this->get_config($store_id);
    }

    public static function update_order($store_id, $sNo,$type,$data)
    {   
        // var_dump($data);die;
        Db::startTrans();
        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();
        //获取原订单信息
        $res_o = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->field('status,z_price,otype,self_lifting')->select()->toArray();
        if($res_o)
        {
            $status = $res_o[0]['status'];
            $oldz_price = $res_o[0]['z_price'];//原订单总额
            $otype = $res_o[0]['otype']; // 订单类型
            $self_lifting = $res_o[0]['self_lifting']; // 自提 0.配送 1.自提 2.商家自配 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
        }
        else
        {
            $message = Lang('order.21');
            echo json_encode(array('code' => ERROR_CODE_DDSJCW, 'message' => $message));
            exit;
        }

        foreach ($data as $key => $value)
        {
            // if ($value == '' && $value != 0 && $key != 'remarks' && $key != 'sheng'&& $key != 'shi'&& $key != 'xian')
            // {
            //     $message = Lang('order.22');
            //     echo json_encode(array('code' => ERROR_CODE_CSCW, 'message' => $message));
            //     exit;
            // }
            if ($key == 'z_price')
            {
                if($otype != 'JP')
                {
                    if ($value < 0)
                    {
                        $message = Lang('order.23');
                        echo json_encode(array('code' => ERROR_CODE_DDJEYW, 'message' => $message));
                        exit;
                    }
                }
            }

            if ($key == 'mobile')
            {
                if (!preg_match("/^1[3456789]\d{9}$/", $value) || strlen($value) != 11)
                {
                    $message = Lang('order.24');
                    echo json_encode(array('code' => ERROR_CODE_SJHGSBZQ, 'message' => $message));
                    exit;
                }
            }
        }
        $shop_id = '';
        if(isset($data['shop_id']))
        {
            $shop_id = $data['shop_id'];
        }
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }
        $operator = '';
        if(isset($data['operator']))
        {
            $operator = $data['operator'];
        }
        $source = 1;
        if(isset($data['source']))
        {
            $source = $data['source'];
        }
    	if($type == 'backstage')
    	{	
            if($otype == 'JP')
            {
                $data_u = array('name'=>$data['name'],'mobile'=>$data['mobile'],'address'=>$data['address'],'sheng'=>$data['sheng'],'shi'=>$data['shi'],'xian'=>$data['xian'],'remarks'=>$data['remarks'],'cpc'=>$data['cpc']);
                $res_u = Db::name('order')->where(['store_id'=>$store_id,'sNo'=>$sNo])->update($data_u);
                if($res_u < 0)
                {
                    Db::rollback();
                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单失败！");
                    $message = Lang('Modification failed');
                    echo json_encode(array('code' => ERROR_CODE_XGSB, 'message' => $message));
                    exit;
                }
                else
                {
                    $message = Lang('Success');
                    echo json_encode(array('code' => 200, 'message' => $message));
                }
            }
            else
            {   
                if($status == 0 && isset($data['u_status']) && $status != $data['u_status'])
                {
                    if($data['u_status'] == 1 || $data['u_status'] == 7)
                    {	
                        $u_status = $data['u_status'];
                        $res_d = Db::name('order_details')->where(['r_sNo'=>$sNo])->update(['r_status'=>$u_status]);

                        $res = Db::name('order')->where(['sNo'=>$sNo])->update(['status'=>$u_status]);

                        if($res < 0 || $res_d < 0)
                        {
                            Db::rollback();
                            $Jurisdiction->admin_record($store_id, $operator, '修改订单号：' . $sNo . '，失败',2,$source,$shop_id,$operator_id);
                            $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单状态失败！");
                            $message = Lang('order.25');
                            echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                            exit;
                        }
                    }
                    else
                    {
                        Db::rollback();
                        $message = Lang('order.25');
                        echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                        exit;
                    }
                }

                if($status > 0)
                {
                    $data_u = array('name'=>$data['name'],'mobile'=>$data['mobile'],'address'=>$data['address'],'sheng'=>$data['sheng'],'shi'=>$data['shi'],'xian'=>$data['xian'],'remarks'=>$data['remarks'],'cpc'=>$data['cpc']);
                }
                else
                {     
                    //若修改订单金额
                    if(isset($data['z_price']) && (float)$oldz_price != (float)$data['z_price'])
                    {   
                        $z_freight = 0;
                        $sz_price = $oldz_price - $data['z_price'];//订单差额
                        //获取详单信息
                        $res_de = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->field('id,after_discount,freight')->select()->toArray();
                        foreach ($res_de as $key => $value) 
                        {
                            $up_price = round((((float)$value['after_discount'] + (float)$value['freight']) / $oldz_price) * $sz_price,2);//该单均摊差额
                            $after_discount = (float)$value['after_discount'] - round(((float)$value['after_discount'] / ((float)$value['after_discount'] + (float)$value['freight'])) * $up_price,2);
                            $freight = (float)$value['freight'] - round(((float)$value['freight'] / ((float)$value['after_discount'] + (float)$value['freight'])) * $up_price,2);
                            $z_freight += $freight; 
                            //更新详单数据
                            $res_ud = Db::name('order_details')->where(['id'=>$value['id']])->update(['after_discount'=>$after_discount,'freight'=>$freight]);
                            if($res_ud < 0)
                            {
                                Db::rollback();
                                $Jurisdiction->admin_record($store_id, $operator, '修改详单：' . $value['id'] . '，失败',2,$source,$shop_id,$operator_id);
                                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改详单数据失败！");
                                $message = Lang('Modification failed');
                                echo json_encode(array('code' => ERROR_CODE_XGSB, 'message' => $message));
                                exit;
                            }
                        }
                        $data_u = array('name'=>$data['name'],'mobile'=>$data['mobile'],'address'=>$data['address'],'z_price'=>$data['z_price'],'sheng'=>$data['sheng'],'shi'=>$data['shi'],'xian'=>$data['xian'],'z_freight'=>$z_freight,'cpc'=>$data['cpc']);
                    }
                    else
                    {
                        $data_u = array('name'=>$data['name'],'mobile'=>$data['mobile'],'address'=>$data['address'],'sheng'=>$data['sheng'],'shi'=>$data['shi'],'xian'=>$data['xian'],'cpc'=>$data['cpc']);
                    }     
                }
                $res_u = Db::name('order')->where(['store_id'=>$store_id,'sNo'=>$sNo])->update($data_u);
                if($res_u < 0)
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '修改订单号：' . $sNo . '，失败',2,$source,$shop_id,$operator_id);
                    $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单失败！");
                    $message = Lang('Modification failed');
                    echo json_encode(array('code' => ERROR_CODE_XGSB, 'message' => $message));
                    exit;
                }
                else
                {
                    $message = Lang('Success');
                    echo json_encode(array('code' => 200, 'message' => $message));
                }
            }
	        
            $Jurisdiction->admin_record($store_id, $operator, '编辑了订单号：' . $sNo . '，的信息',2,$source,$shop_id,$operator_id);
	        Db::commit();
	        exit;
    	}
    	elseif ($type == 'merchant') 
    	{	
    		if($status > 0)
        	{
        	    if(isset($data['z_price']))
                {
                    $data_u = array('name'=>$data['name'],'mobile'=>$data['mobile'],'address'=>$data['address'],'z_price'=>$data['z_price'],'remarks'=>$data['remarks'],'sheng'=>$data['sheng'],'shi'=>$data['shi'],'xian'=>$data['xian'],'cpc'=>$data['cpc']);
                }
                else
                {
                    $data_u = array('name'=>$data['name'],'mobile'=>$data['mobile'],'address'=>$data['address'],'sheng'=>$data['sheng'],'shi'=>$data['shi'],'xian'=>$data['xian'],'cpc'=>$data['cpc']);
                }
        	}
        	else
        	{      
                if(isset($data['z_price']))
                {
                    //若修改订单金额
                    if((float)$oldz_price != (float)$data['z_price'])
                    {
                        $sz_price = $oldz_price - $data['z_price'];//订单差额
                        //获取详单信息
                        $res_de = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->field('id,after_discount,freight')->select()->toArray();
                        foreach ($res_de as $key => $value) 
                        {
                            $up_price = round((((float)$value['after_discount'] + (float)$value['freight']) / $oldz_price) * $sz_price,2);//该单均摊差额
                            //更新详单数据
                            $res_ud = Db::name('order_details')->where(['id'=>$value['id']])->update(['after_discount'=>Db::raw('after_discount-'.$up_price)]);
                            if($res_ud < 0)
                            {
                                Db::rollback();
                                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改详单数据失败！");
                                $message = Lang('Modification failed');
                                echo json_encode(array('code' => ERROR_CODE_XGSB, 'message' => $message));
                                exit;
                            }
                        }
                    }
                    $data_u = array('name'=>$data['name'],'mobile'=>$data['mobile'],'address'=>$data['address'],'z_price'=>$data['z_price'],'remarks'=>$data['remarks'],'sheng'=>$data['sheng'],'shi'=>$data['shi'],'xian'=>$data['xian'],'cpc'=>$data['cpc']);
                }
                else
                {
                    $data_u = array('name'=>$data['name'],'mobile'=>$data['mobile'],'address'=>$data['address'],'sheng'=>$data['sheng'],'shi'=>$data['shi'],'xian'=>$data['xian'],'cpc'=>$data['cpc']);
                }
        	}
            $res_u = Db::name('order')->where(['store_id'=>$store_id,'sNo'=>$sNo])->update($data_u);
        	if($res_u < 0)
        	{
                Db::rollback();
                $lktlog->log("common/orderslist.log",__METHOD__ . ":" . __LINE__ . "修改订单失败！");
                $message = Lang('Modification failed');
                echo json_encode(array('code' => ERROR_CODE_XGSB, 'message' => $message));
        		exit;
        	}
        	else
        	{
                if($self_lifting == 2)
                {
                    if(isset($data['delivery_period']) && isset($data['delivery_time']))
                    {
                        $sql_d = "select store_self_delivery from lkt_order_details where r_sNo = '$sNo' ";
                        $r_d = Db::query($sql_d);
                        if($r_d)
                        {
                            $store_self_delivery = $r_d[0]['store_self_delivery'];
                            
                            $sql_self_delivery_info = "update lkt_self_delivery_info set delivery_period = '$delivery_period',delivery_time = '$delivery_time' where id = '$store_self_delivery' ";
                            $r_self_delivery_info = Db::execute($sql_self_delivery_info);
                        }
                    }
                }
                $message = Lang('Success');
                echo json_encode(array('code' => 200, 'message' => $message));
        	}
        	Db::commit();
        	exit;
    	}
    	else
    	{
            $message = Lang('operation failed');
            echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
            exit;
    	}
    }

    // 管理后台-删除订单
    public static function admin_del_order($array)
    {
        $store_id = $array['store_id'];
        $sNo = $array['sNo'];
        $operator = $array['operator'];
        $shop_id = $array['shop_id'];
        $source = $array['source'];
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }

        $Jurisdiction = new Jurisdiction();

        $orders = explode(',', $sNo);

        Db::startTrans();
        if($orders != array())
        {
            foreach ($orders as $key => $value)
            {
                $sql0 = "select sNo from lkt_order where store_id = '$store_id' and (id = '$value' or sNo = '$value') ";
                $r0 = Db::query($sql0);
                if($r0)
                {
                    $sNo = $r0[0]['sNo'];

                    $sql1 = "update lkt_order_details set recycle = 1 where store_id = '$store_id' and r_sNo = '$sNo'";
                    $res1 = Db::execute($sql1);
                    if ($res1 < 0)
                    {
                        self::Log(__METHOD__ . ":" . __LINE__ . "修改订单详情状态！sql:" . $sql1);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '删除的订单号：' . $sNo . '，的信息失败',10,$source,$shop_id,$operator_id);
                        $message = Lang("operation failed");
                        echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                        exit;
                    }

                    $sql2 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->find();
                    $sql2->recycle = 1;
                    $res2 =$sql2->save();
                    if (!$res2)
                    {
                        self::Log(__METHOD__ . ":" . __LINE__ . "修改订单状态！sNo:" . $sNo);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '删除的订单号：' . $sNo . '，的信息失败',10,$source,$shop_id,$operator_id);
                        $message = Lang("operation failed");
                        echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                        exit;
                    }
                    $Jurisdiction->admin_record($store_id, $operator, '删除的订单号：' . $sNo . '，的信息',10,$source,$shop_id,$operator_id);
                }
            }
        }
        
        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 店铺端-删除订单
    public static function mch_del_order($array)
    {
        $store_id = $array['store_id'];
        $sNo = $array['sNo'];
        $operator = $array['operator'];
        $shop_id = $array['shop_id'];
        $source = $array['source'];
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }

        $Jurisdiction = new Jurisdiction();

        $orders = explode(',', $sNo);

        Db::startTrans();
        if($orders != array())
        {
            foreach ($orders as $key => $value)
            {
                $sql0 = "select sNo from lkt_order where store_id = '$store_id' and (id = '$value' or sNo = '$value') ";
                $r0 = Db::query($sql0);
                if($r0)
                {
                    $sNo = $r0[0]['sNo'];

                    $sql1 = "update lkt_order_details set recycle = 3 where store_id = '$store_id' and r_sNo = '$sNo'";
                    $res1 = Db::execute($sql1);
                    if ($res1 < 0)
                    {
                        self::Log(__METHOD__ . ":" . __LINE__ . "修改订单详情状态！sql:" . $sql1);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '删除的订单号：' . $sNo . '，的信息失败',10,$source,$shop_id,$operator_id);
                        $message = Lang("operation failed");
                        echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                        exit;
                    }

                    $sql2 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->find();
                    $sql2->recycle = 3;
                    $res2 =$sql2->save();

                    if (!$res2)
                    {
                        self::Log(__METHOD__ . ":" . __LINE__ . "修改订单状态！sNo:" . $sNo);
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '删除的订单号：' . $sNo . '，的信息失败',10,$source,$shop_id,$operator_id);
                        $message = Lang("operation failed");
                        echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                        exit;
                    }
                    
                    $Jurisdiction->admin_record($store_id, $operator, '删除的订单号：' . $sNo . '，的信息',10,$source,$shop_id,$operator_id);
                }
            }
        }
        
        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 关闭订单
    public static function closeOrder($array)
    {
        $store_id = $array['store_id'];
        $id = $array['sNo'];
        $operator = $array['operator'];
        $shop_id = $array['shop_id'];
        $source = $array['source'];
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }

        $Jurisdiction = new Jurisdiction();

        $sql0 = "select user_id,sNo,otype from lkt_order where store_id = '$store_id' and (id = '$id' or sNo = '$id') ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $sNo = $r0[0]['sNo'];
            $otype = $r0[0]['otype'];
            $user_id = $r0[0]['user_id'];
        }
        else
        {
            $message = Lang("Parameter error");
            return output(ERROR_CODE_CSCW,$message);
        }

        Db::startTrans();
        $sql1 = "update lkt_order_details set r_status = 7 where store_id = '$store_id' and r_sNo = '$sNo'";
        $res1 = Db::execute($sql1);
        if (!$res1)
        {
            self::Log(__METHOD__ . ":" . __LINE__ . "关闭订单详情失败！sql:" . $sql1);
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '关闭了订单ID：'.$sNo.'，的信息失败',2,$source,$shop_id,$operator_id);
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }

        $sql2 = "update lkt_order set status = 7 where store_id = '$store_id' and sNo = '$sNo'";
        $res2 = Db::execute($sql2);
        if (!$res2)
        {
            self::Log(__METHOD__ . ":" . __LINE__ . "关闭订单失败！sql:" . $sql2);
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '关闭了订单ID：'.$sNo.'，的信息失败',2,$source,$shop_id,$operator_id);
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }

        $event = 'Admin取消订单号为' . $sNo . '的订单';
        $sql = new RecordModel();
        $sql->store_id = $store_id;
        $sql->user_id = 'Admin';
        $sql->money = 0;
        $sql->oldmoney = 0;
        $sql->add_date = date("Y-m-d H:i:s");
        $sql->event = $event;
        $sql->type = 23;
        $sql->save();

        $r3 = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo])->field('p_id,num,sid')->select()->toArray();
        if ($r3)
        {
            foreach ($r3 as $k => $v)
            {
                $p_id = $v['p_id'];
                $num = $v['num'];
                $sid = $v['sid'];

                $sql3_0 = "select goods_id from lkt_auction_product where id = '$p_id' ";
                $r3_0 = Db::query($sql3_0);
                if($r3_0)
                {
                    $p_id = $r3_0[0]['goods_id'];
                }

                $sql4 = ProductListModel::where(['store_id'=>$store_id,'id'=>$p_id])->find();
                $sql4->num = Db::raw('num + '.$num);
                $res_4 = $sql4->save();
                if (!$res_4)
                {   
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '关闭了订单ID：'.$sNo.'，的信息失败',2,$source,$shop_id,$operator_id);
                    self::Log(__METHOD__ . ":" . __LINE__ . "修改商品库存失败！p_id:" . $p_id);
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_CZSB,$message);
                }

                $sql5 = ConfigureModel::find($sid);
                $sql5->num = Db::raw('num + '.$num);
                $res_5 = $sql5->save();
                if ($res_5 < 1)
                {   
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '关闭了订单ID：'.$sNo.'，的信息失败',2,$source,$shop_id,$operator_id);
                    self::Log(__METHOD__ . ":" . __LINE__ . "修改商品规格库存失败！sid:" . $sid);
                    $message = Lang("operation failed");
                    return output(ERROR_CODE_CZSB,$message);
                }
            }
        }

        if($otype == 'JP')
        {
            if(file_exists(MO_LIB_DIR.'/Plugin/Auction.php'))
            {
                $array = array('store_id'=>$store_id,'sNo'=>$sNo,'user_id'=>$user_id);
                app('app\common\Plugin\Auction')->closeOrder($array);
            }
        }
        
        Db::commit();
        $Jurisdiction->admin_record($store_id, $operator, '关闭了订单ID：'.$sNo.'，的信息',2,$source,$shop_id,$operator_id);
        $message = Lang("Success");
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 管理后台-结算订单删除
    public static function admin_OrderSettlement_del($array)
    {
        $store_id = $array['store_id'];
        $id = $array['id'];
        $operator = $array['operator'];
        $shop_id = $array['shop_id'];
        $source = $array['source'];
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }

        $Jurisdiction = new Jurisdiction();

        $orders = explode(',', $id);

        Db::startTrans();
        if($orders != array())
        {
            foreach ($orders as $key => $value)
            {
                $r0 = OrderModel::where(['store_id'=> $store_id,'id'=>$value])->field('sNo')->select()->toArray();
                if($r0)
                {
                    $sNo = $r0[0]['sNo'];

                    $sql_where1 = array('store_id'=>$store_id,'r_sNo'=>$sNo);
                    $sql_update1 = array('recycle'=>1);
                    $r1 = Db::name('order_details')->where($sql_where1)->update($sql_update1);
                    if ($r1 < 0)
                    {
                        self::Log(__METHOD__ . ":" . __LINE__ . "修改订单详情状态！参数:" . json_encode($sql_where1));
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '删除了订单ID：'.$sNo.' 的结算信息失败',10,$source,$shop_id,$operator_id);
                        $message = Lang("operation failed");
                        echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                        exit;
                    }

                    $sql_where2 = array('store_id'=>$store_id,'id'=>$value);
                    $sql_update2 = array('recycle'=>1);
                    $r2 = Db::name('order')->where($sql_where2)->update($sql_update2);
                    if ($r2 < 0)
                    {
                        self::Log(__METHOD__ . ":" . __LINE__ . "修改订单状态！参数:" . json_encode($sql_where2));
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '删除了订单ID：'.$sNo.' 的结算信息失败',10,$source,$shop_id,$operator_id);
                        $message = Lang("operation failed");
                        echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                        exit;
                    }
                    
                    $Jurisdiction->admin_record($store_id, $operator, '删除了订单ID：'.$sNo.' 的结算信息',10,$source,$shop_id,$operator_id);
                }
            }
        }
        
        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 店铺端-结算订单删除
    public static function mch_OrderSettlement_del($array)
    {
        $store_id = $array['store_id'];
        $id = $array['id'];
        $operator = $array['operator'];
        $shop_id = $array['shop_id'];
        $source = $array['source'];
        $operator_id = 0;
        if(isset($array['operator_id']))
        {
            $operator_id = $array['operator_id'];
        }

        $Jurisdiction = new Jurisdiction();
        $orders = explode(',', $id);

        Db::startTrans();
        if($orders != array())
        {
            foreach ($orders as $key => $value)
            {
                $r0 = OrderModel::where(['store_id'=> $store_id,'id'=>$value])->field('sNo')->select()->toArray();
                if($r0)
                {
                    $sNo = $r0[0]['sNo'];

                    $sql_where1 = array('store_id'=>$store_id,'r_sNo'=>$sNo);
                    $sql_update1 = array('recycle'=>3);
                    $r1 = Db::name('order_details')->where($sql_where1)->update($sql_update1);
                    if ($r1 < 0)
                    {
                        self::Log(__METHOD__ . ":" . __LINE__ . "修改订单详情状态！参数:" . json_encode($sql_where1));
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '删除了订单ID：'.$sNo.' 的结算信息失败',3,$source,$shop_id,$operator_id);
                        $message = Lang("operation failed");
                        echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                        exit;
                    }

                    $sql_where2 = array('store_id'=>$store_id,'id'=>$value);
                    $sql_update2 = array('recycle'=>3);
                    $r2 = Db::name('order')->where($sql_where2)->update($sql_update2);
                    if ($r2 < 0)
                    {
                        self::Log(__METHOD__ . ":" . __LINE__ . "修改订单状态！参数:" . json_encode($sql_where2));
                        Db::rollback();
                        $Jurisdiction->admin_record($store_id, $operator, '删除了订单ID：'.$sNo.' 的结算信息失败',3,$source,$shop_id,$operator_id);
                        $message = Lang("operation failed");
                        echo json_encode(array('code' => ERROR_CODE_CZSB, 'message' => $message));
                        exit;
                    }
                    
                    $Jurisdiction->admin_record($store_id, $operator, '删除了订单ID：'.$sNo.' 的结算信息',3,$source,$shop_id,$operator_id);
                }
            }
        }

        Db::commit();
        $message = Lang("Success");
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 日志
    public static function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("common/EditOrderStatus.log",$Log_content);
        return;
    }
}
?>