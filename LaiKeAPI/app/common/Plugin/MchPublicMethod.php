<?php

namespace app\common\Plugin;

use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\PC_Tools;
use app\common\Jurisdiction;
use app\common\LKTConfigInfo;
use app\common\OSSCommon;
use app\common\MinIOServer;
use app\common\Product;

use app\admin\model\MchConfigModel;
use app\admin\model\ProductConfigModel;
use app\admin\model\ProductListModel;
use app\admin\model\ConfigureModel;
use app\admin\model\UserCollectionModel;
use app\admin\model\MchStoreModel;
use app\admin\model\OrderModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\MchModel;
use app\admin\model\ThirdModel;

require_once '../app/common/phpqrcode.php';
class MchPublicMethod
{

    // 获取插件状态
    public function is_Plugin($store_id)
    {
        $r0 = MchConfigModel::where('store_id', $store_id)->field('is_display')->select()->toArray();
        if ($r0)
        {
            $is_display = $r0[0]['is_display'];
        }
        else
        {
            $is_display = 2;
        }
        return $is_display;
    }

    // 添加插件设置
    public function add($store_id)
    {
        Db::name('lkt_mch_config')->save(array('store_id'=>$store_id));
        return;
    }

    // 删除插件设置
    public function del($store_id)
    {
        Db::table('lkt_mch_config')->where('store_id',$store_id)->delete();
        return;
    }

    // 店铺商品信息
    public function commodity_information($store_id,$mch_id)
    {
        $shop_list = array();

        $status2 = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>2));

        $r0 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id, 'mch_status'=>2, 'recycle'=>0, 'active'=>1,'status'=>2])
                        ->whereNotIn('commodity_type','2')
                        ->field('id')
                        ->select()
                        ->toArray();
        $shop_list['quantity_on_sale'] = count($r0);

        $quantity_sold = 0;

        $sql1 = "select tt.* from (select (a.real_volume) as volume0,row_number () over (PARTITION BY c.pid) as top from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.mch_id = '$mch_id' and a.mch_status = 2 and a.recycle = 0 and c.recycle = 0 and a.active = 1 and a.commodity_type != 2 ) as tt where tt.top < 2 ";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $quantity_sold += $v['volume0'];  // 已售数量
            }
        }
        if($quantity_sold < 0)
        {
            $quantity_sold = 0;
        }
        $shop_list['quantity_sold'] = $quantity_sold;

        $r0_0 = UserCollectionModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])
                                    ->field('id')
                                    ->select()
                                    ->toArray();
        $shop_list['collection_num'] = count($r0_0);

        return $shop_list;
    }

    // 自提结算
    public function Settlement($store_id, $products, $res = '',$shop_address_id = '')
    {
        $shop_status = 0;
        $name = '';
        $cpc = '';
        $tel = '';
        $sheng = '';
        $shi = '';
        $xian = '';
        $address = '';
        $code = '';
        $extraction_code = '';
        $extraction_code_img = '';
        $is_self_delivery = 0;
        $time_end = strtotime("+30 minutes");
        $time = date('Y-m-d H:i:s');
        if (count($products) > 1)
        {
            $shop_status = 0;
        }
        else
        {
            $mch_id = $products[0]['shop_id'];

            $sql = "select * from lkt_mch where id = '$mch_id' ";
            $r = Db::query($sql);
            if($r)
            {
                $name = $r[0]['name'];
                $cpc = $r[0]['cpc'];
                $tel = $r[0]['tel'];
                $is_self_delivery = $r[0]['is_self_delivery'];
            }

            if($shop_address_id != '' && $shop_address_id != 0)
            {
                $r0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'id'=>$shop_address_id])->select()->toArray();
            }
            else
            {
                $rm = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'is_default'=>1])->select()->toArray();
                if($rm)
                {
                    $r0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'is_default'=>1])->select()->toArray();
                }
                else
                {
                    $r0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
                }
            }
            if ($r0)
            {
                $sheng = $r0[0]['sheng'];
                $shi = $r0[0]['shi'];
                $xian = $r0[0]['xian'];
                $address = $r0[0]['address'];
                $code = $r0[0]['code'];
                $business_hours = explode('~', $r0[0]['business_hours']);

                $shop_status = 1;
                if ($res == 'payment')
                {
                    $extraction_code = $this->extraction_code() . ',' . time() . ',' . $time_end;
                    $extraction_code_img = $this->extraction_code_img($extraction_code, 'image/extraction_code_img', 5);
                }
            }
            else
            {
                $shop_status = 0;
            }
        }
        $arr = array('name' => $name,'cpc' => $cpc,'tel' => $tel,'shop_status' => $shop_status, 'sheng' => $sheng,'shi' => $shi,'xian' => $xian,'address' => $address,'code' => $code,'extraction_code' => $extraction_code, 'extraction_code_img' => $extraction_code_img,'is_self_delivery'=>$is_self_delivery);

        return $arr;
    }

    // 虚拟订单生成提取码和二维码
    public function Settlement_0()
    {
        $extraction_code = '';
        $extraction_code_img = '';
        $time_end = strtotime("+30 minutes");
        
        $extraction_code = $this->extraction_code() . ',' . time() . ',' . $time_end;
        $extraction_code_img = $this->extraction_code_img($extraction_code, 'image/extraction_code_img', 5);
        $arr = array('extraction_code' => $extraction_code, 'extraction_code_img' => $extraction_code_img);

        return $arr;
    }

    // 根据门店ID，查询门店信息
    public function Settlement1($store_id,$products, $shop_address_id)
    {
        $list = array();
        $mch_id = $products[0]['shop_id'];
        if($shop_address_id)
        {
            $r0 = MchStoreModel::where(['store_id'=>$store_id,'id'=>$shop_address_id])->select()->toArray();
            if ($r0)
            {
                $list['id'] = $r0[0]['id'];
                $list['name'] = $r0[0]['name'];
                $list['mobile'] = $r0[0]['mobile'];
                $list['sheng'] = $r0[0]['sheng'];
                $list['shi'] = $r0[0]['shi'];
                $list['xian'] = $r0[0]['xian'];
                $list['address'] = $r0[0]['address'];
            }
        }
        else
        {
            $rm = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'is_default'=>1])->select()->toArray();
            if($rm)
            {
                $r0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'is_default'=>1])->select()->toArray();
            }
            else
            {
                $r0 = MchStoreModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id])->select()->toArray();
            }
            if ($r0)
            {
                $list['id'] = $r0[0]['id'];
                $list['name'] = $r0[0]['name'];
                $list['mobile'] = $r0[0]['mobile'];
                $list['sheng'] = $r0[0]['sheng'];
                $list['shi'] = $r0[0]['shi'];
                $list['xian'] = $r0[0]['xian'];
                $list['address'] = $r0[0]['address'];
            }
        }

        return $list;
    }

    // 生成提取码
    private function extraction_code()
    {
        $arr1 = range('a', 'z');
        $arr2 = range('A', 'Z');
        $arr3 = range(0, 9);
        $arr = array_merge($arr1, $arr2, $arr3); // 把多个数组合并为一个数组
        shuffle($arr); // 把数组中的元素按随机顺序重新排序
        $code = $arr[0] . $arr[1] . $arr[2] . $arr[3] . $arr[4] . $arr[5];
        $res = OrderModel::where('extraction_code',$code)->where('status','in','0,2')->select()->toArray();
        if ($res)
        {
            $this->extraction_code();
        }
        else
        {
            return $code;
        }
    }

    // 生成提取码-二维码
    private function extraction_code_img($extraction_code, $uploadImg, $size = 5)
    {
        $qrcode_name = md5(time() . rand(1000, 9999));

        $value = $extraction_code; //二维码内容
        $errorCorrectionLevel = 'L'; //容错级别
        $matrixPointSize = $size; //生成图片大小

        $contentType = '.png';
        $filename = $uploadImg . '/' . $qrcode_name . $contentType;

        $imgURL = '../public/' . $filename;
        app('QRcode')::png($value,'../public/' . $filename, $errorCorrectionLevel, $matrixPointSize, 2);

        $path = $filename;
        $upserver = 2;
        $common = LKTConfigInfo::getOSSConfig();
        try
        {
            if (!file_exists($imgURL))
            {
                mkdir($imgURL, 0777, true);
            }
            //查询文件上传配置方式
            $sql0 = "select upserver from lkt_config where store_id = 1 ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $upserver = $r0[0]['upserver'];
            }
            //阿里云
            if($upserver == '2')
            {
                $ossClient = OSSCommon::getOSSClient();
                $ossClient->uploadFile($common['bucket'], $path, $imgURL);
            }
            //MinIO
            if($upserver == '5')
            {
                $ossClient = new MinIOServer();
                $ossClient->upLoadObject($imgURL,$path,$contentType);
            }
        }
        catch (OssException $e)
        {
            printf(__FUNCTION__ . ": FAILED\n");
            printf($e->getMessage() . "\n");
            return;
        }

        $url = '';
        if($upserver == '2')
        {
            $isopenzdy = $common['isopenzdy'];
            $url = 'https://' . $common['bucket'] . '.' . $common['endpoint'] . '/' . $path;
            if($isopenzdy == 1)
            {
                $url = 'https://'. $common['MyEndpoint'] . '/' . $path;
            }
        }
        if($upserver == '5')
        {
            $url = 'http://' . $common['endpoint'] . '/' . $common['bucket'] . '/' . $path;
            if (strpos($common['endpoint'], "http") !== false) 
            {
                $url = $common['endpoint'] . '/' . $common['bucket'] . '/' . $path;
            }
        }

        if (file_exists($filename)) 
        {
            unlink($filename);
        } 

        return $url;
    }

    // 验证店铺是否存在
    public function verification_mch($store_id, $user_id,$shop_id)
    {
        $r0 = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->field('id')->select()->toArray();
        if ($r0)
        {
            $mch_id = $r0[0]['id'];
            if ($mch_id != $shop_id)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '店铺ID为' . $mch_id . '与传过来的店铺ID' . $shop_id . '不一致！';
                $this->mchLog($Log_content);
                $message = Lang('Illegal invasion');
                echo json_encode(array('code' => 115, 'message' => $message));
                exit;
            }
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '会员' . $user_id . '没有店铺！';
            $this->mchLog($Log_content);
            $message = Lang('Illegal invasion');
            echo json_encode(array('code' => 115, 'message' => $message));
            exit;
        }
        return;
    }

    // 买家确认收货,店主收入
    public function parameter($store_id, $sNo, $payment_money, $allow = 0)
    {
        Db::startTrans();
        $r0 = OrderModel::where(['store_id'=>$store_id,'sNo'=>$sNo])->field('mch_id')->select()->toArray();
        if ($r0)
        {
            $mch_id = $r0[0]['mch_id'];
            $mch_id = substr($mch_id, 1, strlen($mch_id) - 2);
            //获取供应商价总计
            // $supplier_settlement = OrderDetailsModel::where(['store_id'=>$store_id,'r_sNo'=>$sNo,'r_status' => 5])->sum('supplier_settlement');
            // if($supplier_settlement > 0)
            // {
            //     $payment_money = $payment_money - $supplier_settlement;
            // }
            $sql_where1 = array('store_id'=>$store_id,'id'=>$mch_id);
            $sql_update1 = array('account_money'=>Db::raw('account_money+'.$payment_money),'integral_money'=>Db::raw('integral_money+'.$allow));
            $r1 = Db::name('mch')->where($sql_where1)->update($sql_update1);
            if ($r1 < 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '修改店铺收入失败！条件参数：'.json_encode($sql_where1).';修改参数：'.json_encode($sql_update1);
                $this->mchLog($Log_content);
                Db::rollback();
                $message = Lang('operation failed');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }

            $r2 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('account_money,integral_money')->select()->toArray();
            if(!$r2)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . "查询店铺不存在 mchid $mch_id !";
                $this->mchLog($Log_content);
                Db::rollback();
                $message = Lang('mch.14');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }

            $account_money = $r2[0]['account_money'];
            $integral_money = $r2[0]['integral_money'];

            $data3 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'price'=>$payment_money,'integral'=>$allow,'integral_money'=>$integral_money,'account_money'=>$account_money,'type'=>1,'addtime'=>date("Y-m-d H:i:s"),'remake'=>$sNo);
            $r3 = Db::name('mch_account_log')->insert($data3);
            if ($r3 <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '添加入驻商户账户收支记录失败！sql:'.json_encode($data3);
                $this->mchLog($Log_content);
                Db::rollback();
                $message = Lang('operation failed');
                echo json_encode(array('code' => 109, 'message' => $message));
                exit;
            }
            Db::commit();
        }
        return;
    }

    // 定时修改提取码问题
    public function up_extraction_code()
    {
        $r0 = OrderModel::where('self_lifting', '1')->field('id,store_id,extraction_code')->select()->toArray();
        if ($r0)
        {
            $id = $r0[0]['id'];
            $store_id = $r0[0]['store_id'];
            $rew = explode(',', $r0[0]['extraction_code']);
            if ($rew[2] <= time())
            { // 提货码有效时间 小于等于 当前时间
                $shop = $this->Settlement2($store_id, $id);
            }
        }

        return;
    }

    // 生成新的提取码
    public function Settlement2( $store_id, $id)
    {
        $time_end = strtotime("+30 minutes");

        $extraction_code = $this->extraction_code() . ',' . time() . ',' . $time_end;
        $extraction_code_img = $this->extraction_code_img($extraction_code, 'image/extraction_code_img', 5);

        $r2 = Db::name('order')->where(['store_id'=>$store_id,'id'=>$id])->update(['extraction_code'=>$extraction_code,'extraction_code_img'=>$extraction_code_img]);

        $arr = array('extraction_code' => $extraction_code, 'extraction_code_img' => $extraction_code_img);
        return $arr;
    }

    // 添加店铺浏览记录
    public function addMchBrowse($mch_id, $store_id, $mch_id1, $user_id)
    {
        if ($mch_id != 0)
        {   
            $sql = array('store_id' => $store_id, 'mch_id' => $mch_id1, 'user_id' => $user_id, 'event' => "购买了商品", 'add_time' => date("Y-m-d H:i:s"));
            $res = Db::name('mch_browse')->insert($sql);
            if ($res <= 0)
            {
                $this->mchLog(__METHOD__ . ":" . __LINE__ . "添加购买记录失败！mch_id:" . $mch_id1);
            }
        }
        return $res;
    }

    // 营业状态
    public function Business_status()
    {
        $time = date("H:i");
        $sql0 = "select id,business_hours from lkt_mch where recovery = 0 and business_hours != '' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $id = $v0['id'];
                $business_hours = explode('~',$v0['business_hours']);
                if($business_hours[0] <= $time && $time < $business_hours[1])
                {
                    $sql1 = "update lkt_mch set is_open = 1 where id = '$id' ";
                }
                else
                {
                    $sql1 = "update lkt_mch set is_open = 2 where id = '$id' ";
                }
                $r1 = Db::execute($sql1);
            }
        }
       
        return;
    }

    // 满减日志
    public function mchLog($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/mch.log",$Log_content);
        return;
    }

    // 定时任务
    public function timing()
    {
        $time = date("Y-m-d H:i:s");
        
        $sql_config = "select store_id,auto_examine,auto_log_off from lkt_mch_config where mch_id = 0 ";
        $r_config = Db::query($sql_config);
        if($r_config)
        {
            foreach($r_config as $k_config => $v_config)
            {
                $store_id = $v_config['store_id']; // 商城ID
                $auto_examine = $v_config['auto_examine']; // 店铺自动审核天数
                $auto_log_off = $v_config['auto_log_off']; // 自动注销时间(月)

                // 自动审核
                if($auto_examine > 0)
                {
                    $end = date("Y-m-d H:i:s", strtotime("-$auto_examine day",strtotime($time))); // 店铺申请的最后时间
                    // 根据商城ID、未审核状态，查询店铺信息
                    $sql0 = "select id,add_time from lkt_mch where store_id = '$store_id' and review_status = 0 ";
                    $r0 = Db::query($sql0);
                    if($r0)
                    {
                        foreach($r0 as $k0 => $v0)
                        {
                            $id = $v0['id']; // 店铺ID
                            $add_time = $v0['add_time']; // 申请时间

                            if($add_time <= $end)
                            { // 申请时间 <= 店铺申请的最后时间
                                $sql1 = "update lkt_mch set review_time = '$time',review_status = 1,is_open = 2 where id = '$id' ";
                                $r1 = Db::execute($sql1);
                            }
                        }
                    }
                }
                else
                {
                    // 根据商城ID、未审核状态，查询店铺信息
                    $sql0 = "select id,add_time from lkt_mch where store_id = '$store_id' and review_status = 0 ";
                    $r0 = Db::query($sql0);
                    if($r0)
                    {
                        foreach($r0 as $k0 => $v0)
                        {
                            $id = $v0['id']; // 店铺ID

                            $sql1 = "update lkt_mch set review_time = '$time',review_status = 1,is_open = 2 where id = '$id' ";
                            $r1 = Db::execute($sql1);
                        }
                    }
                }

                if($auto_log_off > 0)
                {
                    $last_login_time = date("Y-m-d H:i:s", strtotime("-$auto_log_off month",strtotime($time))); // 注销店铺，店铺最后登录时间
                    
                    $sql_0 = "select id,account_money,last_login_time from lkt_mch where store_id = '$store_id' and last_login_time IS NOT NULL ";
                    $r_0 = Db::query($sql_0);
                    if($r_0)
                    {
                        foreach($r_0 as $k_0 => $v_0)
                        {
                            $id = $v_0['id']; // 店铺ID
                            $last_login_time_0 = $v_0['last_login_time']; // 最后登录时间
                            if($v_0['account_money'] > 0)
                            {
                                continue;
                            }

                            $sql_2 = "select id from lkt_order where store_id = '$store_id' and mch_id like '%," . $id . ",%' and status in (0,1,2) ";
                            $r_2 = Db::query($sql_2);
                            if($r_2)
                            {
                                continue;
                            }

                            if($last_login_time_0 <= $last_login_time)
                            {
                                $this->Automatically_cancel_store($store_id,$id);
                            }
                        }
                    }
                }
            }
        }
       
        return;
    }

    // 自动注销店铺
    public function Automatically_cancel_store($store_id,$id)
    {
        Db::startTrans();

        $sql_1 = "update lkt_mch set is_lock = '1' where id = '$id' ";
        $r_1 = Db::execute($sql_1);
        if($r_1 < 1)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除店铺失败！sql:'.$sql_1;
            $this->mchLog($Log_content);
            Db::rollback();
            return 0;
        }

        $r1 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$id])->field('id')->select()->toArray();
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $p_id = $v['id'];
                // 根据产品id，删除产品信息
                $r2 = Db::name('product_list')->where(['store_id'=>$store_id,'id'=>$p_id])->update(['recycle'=>1,'status'=>1]);
                if ($r2 == -1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '删除店铺商品失败！商品ID:'.$p_id;
                    $this->mchLog($Log_content);
                    Db::rollback();
                    return 0;
                }
                $r3 = Db::name('configure')->where(['pid'=>$p_id])->update(['recycle'=>1]);
                if ($r3 == -1)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除店铺商品属性失败！商品ID:'.$p_id;
                    $this->mchLog($Log_content);
                    Db::rollback();
                    return 0;
                }

                $r4 = Db::table('lkt_product_img')->where('product_id',$p_id)->delete();
                if ($r4 < 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除店铺商品图片失败！商品ID:'.$p_id;
                    $this->mchLog($Log_content);
                    Db::rollback();
                    return 0;
                }

                $r5 = Db::table('lkt_cart')->where(['store_id'=>$store_id,'Goods_id'=>$p_id])->delete();
                if ($r5 < 0)
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除店铺商品在购物车里的数据失败！商品ID:'.$p_id;
                    $this->mchLog($Log_content);
                    Db::rollback();
                    return 0;
                }

                $r6 = Db::table('lkt_user_footprint')->where(['store_id'=>$store_id,'p_id'=>$p_id])->delete();

                $sql7 = "delete from lkt_user_collection where store_id = '$store_id' and p_id = '$p_id' or mch_id = '$id'";
                $r7 = Db::execute($sql7);
            }
        }
        Db::commit();
        return 1;
    }

    // 注销店铺
    public function Cancel_store($array)
    {
        $Jurisdiction = new Jurisdiction();
        $store_id = $array['store_id'];
        $id = $array['id'];
        $operator_id = $array['operator_id'];
        $operator = $array['operator'];
        $source = $array['source'];
        Db::startTrans();

        $r0_0 = MchModel::where(['store_id'=>$store_id,'id'=>$id])->field('account_money,is_open')->select()->toArray();
        if($r0_0[0]['account_money'] > 0)
        {
            $message = Lang('mch.34');
            echo json_encode(array('code' => 115,'message'=>$message));
            exit;
        }
        if($r0_0[0]['is_open'] == 1)
        {
            $message = Lang('mch.35');
            echo json_encode(array('code' => 115,'message'=>$message));
            exit;
        }

        $r0_1 = OrderModel::where(['store_id'=>$store_id])->whereLike('mch_id','%,'.$id.',%')->whereIn('status','0,1,2')->field('id')->select()->toArray();
        if($r0_1)
        {
            $message = Lang('mch.13');
            echo json_encode(array('code' => 115,'message'=>$message));
            exit;
        }

        $r0_2 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$id,'status'=>2,'recycle'=>0])->field('id')->select()->toArray();
        if($r0_2)
        {
            $message = Lang('mch.91');
            echo json_encode(array('code' => 115,'message'=>$message));
            exit;
        }

        $sql0_3 = "select id from lkt_mch_promise where mch_id = '$id' and status = 1 ";
        $r0_3 = Db::query($sql0_3);
        if($r0_3)
        {
            $message = Lang('mch.92');
            echo json_encode(array('code' => 115,'message'=>$message));
            exit;
        }

        $r0 = Db::name('mch')->where(['store_id' => $store_id,'id'=>$id])->update(['recovery'=>1]);
        if ($r0 > 0)
        {
            $r1 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$id])->field('id')->select()->toArray();
            if ($r1)
            {
                $str = "";
                foreach ($r1 as $k => $v)
                {
                    $str = $v['id'] . ',';
                }
                $str = trim($str,',');

                $product = new Product();
                $product->mch_del($store_id,$operator,$str,'店铺');
            }

            Db::table('lkt_jump_path')->where('type0',3)->whereLike('parameter','%='.$id)->delete();

            $Log_content = __METHOD__ . '->' . __LINE__ . $operator . ' 删除店铺ID为' . $id . '成功';
            $this->mchLog($Log_content);
            Db::commit();
            if($source == 1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '删除了店铺ID：'.$id,3,1,0,$operator_id);
            }
            $message = Lang('Success');
            echo json_encode(array('code' => 200,'message'=>$message));
            exit;
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . $operator  . ' 删除店铺失败！';
            $this->mchLog($Log_content);
            Db::rollback();
            if($source == 1)
            {
                $Jurisdiction->admin_record($store_id, $operator, '删除了店铺ID：'.$id.'失败',3,1,0,$operator_id);
            }
            $message = Lang('Busy network');
            echo json_encode(array('code' => 115,'message'=>$message));
            exit;
        }
    }

    // 获取店铺是否缴纳保证金
    public function is_margin($array)
    {
        $store_id = $array['store_id'];
        $shop_id = $array['shop_id'];
        
        $is_Payment = false; // 未缴纳保证金弹窗 false:弹窗 true:不弹窗
        $isPromisePay = false; // 未缴纳保证金弹窗 false:弹窗 true:不弹窗
        $isPromiseSwitch = false;
        $commodity_setup = array(1);
        // 查询店铺设置
        $sql0 = "select commodity_setup,promise_switch from lkt_mch_config where store_id = '$store_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $commodity_setup = explode(',',$r0[0]['commodity_setup']); // 商品设置 1.上传商品 2.自选商品
            if($r0[0]['promise_switch'] == 1)
            { // 保证金开关（开启）
                $isPromiseSwitch = true;
                // 根据店铺ID、已缴纳、未退还，查询店铺保证金记录
                $sql1 = "select id from lkt_mch_promise where mch_id = '$shop_id' and status = 1 and is_return_pay = 0 ";
                $r1 = Db::query($sql1);
                if($r1)
                { // 存在
                    $isPromisePay = true; // 未缴纳保证金弹窗 false:弹窗 true:不弹窗
                    $is_Payment = true; // 未缴纳保证金弹窗 false:弹窗 true:不弹窗
                }
            }
            else
            { // 保证金开关（关闭）
                $is_Payment = true; // 未缴纳保证金弹窗 false:弹窗 true:不弹窗
                $isPromisePay = true; // 未缴纳保证金弹窗 false:弹窗 true:不弹窗
            }
        }

        $isPromiseExamine = true; // 是否申请退还保证金 true:没有 false:有
        // 根据店铺ID、审核中，查询保证金审核表
        $sql2 = "select id from lkt_promise_sh where mch_id = '$shop_id' and status = 3 ";
        $r2 = Db::query($sql2);
        if($r2)
        {
            $isPromiseExamine = false; // 是否申请退还保证金 true:没有 false:有
        }
        
        $data = array('is_Payment'=>$is_Payment,'isPromiseSwitch'=>$isPromiseSwitch,'commodity_setup'=>$commodity_setup,'isPromiseExamine'=>$isPromiseExamine,'isPromisePay'=>$isPromisePay);
        return $data;
    }

    // 获取店铺门店
    public static function getMchStore($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];

        $list = array();
        $sql0 = "select id,name from lkt_mch_store where store_id = '$store_id' and mch_id = '$mch_id' order by is_default desc ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $list = $r0;
        }
       
        $data = array('list'=>$list);
        return $data;
    }

    // 店铺统计数据
    public function statisticsMch($store_id)
    {
        $time = date("Y-m-d 00:00:00");
        $time0 = date("Y-m-d H:i:s");
        // 根据商城ID、审核通过、没有被删除，查询店铺ID
        $sql0 = "select * from lkt_mch where store_id = '$store_id' and review_status = 1 and recovery = 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach($r0 as $k0 => $v0)
            {
                $mch_id = $v0['id']; // 店铺ID

                /* 店铺数据汇总表开始 */
                //获取店铺下所有的商品信息
                $sjNum = 0; // 上架数
                $xjNum = 0; // 下架数
                $dshProNum = 0; // 待审核商品数
                $classNum = 0; // 商品分类数
                $brandNum = 0; // 商品品牌数
                $saleSkuNum = 0; // 销售商品sku数量
                $skuNum = 0; // sku数量
                $kcbzNum = 0; // 库存不足的商品

                // 上架数
                $sql0_0 = "select ifnull(count(1),0) as num from lkt_product_list where store_id = '$store_id' and mch_id = '$mch_id' and status = 2 and recycle = 0 and mch_status in (2,4)";
                $r0_0 = Db::query($sql0_0);
                if($r0_0)
                {
                    $sjNum = $r0_0[0]['num'];
                }

                // 下架数
                $sql0_1 = "select ifnull(count(1),0) as num from lkt_product_list where store_id = '$store_id' and mch_id = '$mch_id' and status = 3 and recycle = 0 and mch_status in (2,4)";
                $r0_1 = Db::query($sql0_1);
                if($r0_1)
                {
                    $xjNum = $r0_1[0]['num'];
                }

                // 待审核商品数
                $sql0_2 = "select ifnull(count(1),0) as num from lkt_product_list where store_id = '$store_id' and mch_id = '$mch_id' and mch_status = 1 and recycle = 0";
                $r0_2 = Db::query($sql0_2);
                if($r0_2)
                {
                    $dshProNum = $r0_2[0]['num'];
                }

                // 商品分类数
                $sql0_3 = "select ifnull(count(1),0) as num from (select ifnull(count(1),0) as num from lkt_product_list where store_id = '$store_id' and mch_id = '$mch_id' and recycle = 0 group by product_class) a";
                $r0_3 = Db::query($sql0_3);
                if($r0_3)
                {
                    $classNum = $r0_3[0]['num'];
                }

                // 商品品牌数
                $sql0_4 = "select ifnull(count(1),0) as num from (select ifnull(count(1),0) as num from lkt_product_list a left join lkt_brand_class b on a.brand_id = b.brand_id where a.store_id = '$store_id' and a.mch_id = '$mch_id' and a.recycle = 0 and b.recycle = 0 group by a.brand_id) a";
                $r0_4 = Db::query($sql0_4);
                if($r0_4)
                {
                    $brandNum = $r0_4[0]['num'];
                }

                // 销售商品sku数量
                $sql0_5 = "select ifnull(count(1),0) as num from lkt_configure c left join lkt_product_list a on a.id = c.pid where a.store_id = '$store_id' and a.mch_id = '$mch_id' and a.mch_status = 2 and a.recycle = 0 and a.status = 2";
                $r0_5 = Db::query($sql0_5);
                if($r0_5)
                {
                    $saleSkuNum = $r0_5[0]['num'];
                }

                // sku数量
                $sql0_6 = "select ifnull(count(1),0) as num from lkt_configure c left join lkt_product_list a on a.id = c.pid where a.store_id = '$store_id' and a.mch_id = '$mch_id' and a.mch_status = 2 and a.recycle = 0";
                $r0_6 = Db::query($sql0_6);
                if($r0_6)
                {
                    $skuNum = $r0_6[0]['num'];
                }

                // 库存不足的商品
                $sql0_7 = "select tt.* from (select a.product_number,a.product_title,a.imgurl,a.status,a.mch_id,a.upper_shelf_time,a.min_inventory,c.id as attrId,c.pid as goodsId,c.price,c.attribute,c.total_num,c.num,max(b.add_date) over (partition by b.attribute_id) as add_date,b.type,b.flowing_num,b.user_id,row_number () over (partition by b.attribute_id) as top from lkt_configure as c left join lkt_product_list as a on c.pid = a.id left join lkt_mch as m on a.mch_id = m.id left join lkt_stock as b on c.id = b.attribute_id where a.store_id = '$store_id' and a.recycle = 0 and c.recycle = 0 and a.mch_status = 2 and c.num <= a.min_inventory and a.gongyingshang = 0 and a.mch_id = '$mch_id' ) as tt where tt.top<2 ";
                // $sql0_7 = "select ifnull(count(1),0) as num from lkt_product_list where store_id = '$store_id' and mch_id = '$mch_id' and recycle = 0 and num <= min_inventory";
                $r0_7 = Db::query($sql0_7);
                if($r0_7)
                {
                    $kcbzNum = count($r0_7);
                }

                //获取店铺下所有的订单信息
                $dfhOrderNum = 0; // 待发货订单
                $dfkOrderNum = 0; // 待付款订单
                $shOrderNum = 0; // 待收货订单
                $tkOrderNum = 0; // 退款数量
                $dshOrderNum = 0; // 退款-待审核订单
                $djsOrderNum = 0; // 待结算订单

                // 待发货订单
                $sql0_8 = "select ifnull(count(1),0) as num from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 and status = 1";
                $r0_8 = Db::query($sql0_8);
                if($r0_8)
                {
                    $dfhOrderNum = $r0_8[0]['num'];
                }

                // 待付款订单
                $sql0_9 = "select ifnull(count(1),0) as num from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 and status = 0";
                $r0_9 = Db::query($sql0_9);
                if($r0_9)
                {
                    $dfkOrderNum = $r0_9[0]['num'];
                }
                
                // 待收货订单
                $sql0_10 = "select ifnull(count(1),0) as num from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 and status = 2";
                $r0_10 = Db::query($sql0_10);
                if($r0_10)
                {
                    $shOrderNum = $r0_10[0]['num'];
                }
                
                // 退款数量
                $sql0_11 = "select ifnull(count(1),0) as num from lkt_invoice_info d where d.store_id = '$store_id' and d.mch_id = '$mch_id' and d.recovery = 0 and d.invoice_status = 1";
                $r0_11 = Db::query($sql0_11);
                if($r0_11)
                {
                    $tkOrderNum = $r0_11[0]['num'];
                }

                // 退款-待审核订单
                $sql0_12 = "select ifnull(count(1),0) as num from lkt_return_order r left join  lkt_order d on d.sNo = r.sNo where d.store_id = '$store_id' and d.mch_id = CONCAT(',','$mch_id',',') and d.recycle = 0 and r.r_type in (0,3,16) and r.re_type in (1,2)";
                $r0_12 = Db::query($sql0_12);
                if($r0_12)
                {
                    $dshOrderNum = $r0_12[0]['num'];
                }

                // 待结算订单
                $sql0_13 = "select ifnull(count(1),0) as num from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 and settlement_status = 0 and status = 5";
                $r0_13 = Db::query($sql0_13);
                if($r0_13)
                {
                    $djsOrderNum = $r0_13[0]['num'];
                }
            
                //账户余额
                $zhye = $v0['cashable_money']; // 可提现金额
                $djsje = $v0['account_money']; // 商户余额

                $ytxje = 0; // 已提现金额
                $tkje = 0; // 退款金额
                // 已提现金额
                $sql0_14 = "select ifnull(sum(w.money),0) as money from lkt_withdraw as w left join lkt_mch as m on m.user_id = w.user_id where m.store_id = '$store_id' and m.id = '$mch_id' and is_mch = 1 and w.status = 1";
                $r0_14 = Db::query($sql0_14);
                if($r0_14)
                {
                    $ytxje = $r0_14[0]['money'];
                }

                // 退款金额
                $sql0_15 = "select ifnull(sum(r.real_money),0) as money from lkt_return_order r left join lkt_order d on d.sNo = r.sNo where d.store_id = '$store_id' and d.mch_id = CONCAT(',','$mch_id',',') and d.recycle = 0 and r.r_type in (4,9,13,15)";
                $r0_15 = Db::query($sql0_15);
                if($r0_15)
                {
                    $tkje = $r0_15[0]['money'];
                }

                //获取客户客单数据
                $zkd = 0; // 总客单
                $gzKh = 0; // 关注客户
                $fwKh = 0; // 访问客户
                $newOrderNum = 0; // 新增下单客户数

                // 总客单
                $sql0_16 = "select ifnull(count(1),0) as num from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 ";
                $r0_16 = Db::query($sql0_16);
                if($r0_16)
                {
                    $zkd = $r0_16[0]['num'];
                }

                // 关注客户
                $sql0_17 = "select ifnull(count(1),0) as num from (select ifnull(count(1),0) from lkt_user_collection where store_id = '$store_id' and mch_id = '$mch_id' group by user_id) a";
                $r0_17 = Db::query($sql0_17);
                if($r0_17)
                {
                    $gzKh = $r0_17[0]['num'];
                }

                // 访问客户
                $sql0_18 = "select ifnull(count(1),0) as num from (select ifnull(count(1),0) from lkt_mch_browse where store_id = '$store_id' and mch_id = '$mch_id' group by user_id) a";
                $r0_18 = Db::query($sql0_18);
                if($r0_18)
                {
                    $fwKh = $r0_18[0]['num'];
                }
    
                //新增下单客户数
                $newOrderNum = $v0['new_user_order_num'];

                // 根据商城ID、店铺ID、今天，查询店铺数据汇总表
                $sql_0 = "select * from lkt_mch_statistics where store_id = '$store_id' and mch_id = '$mch_id' and add_date = '$time' ";
                $r_0 = Db::query($sql_0);
                if($r_0)
                { // 存在，修改
                    $data_update = array('pending_shipment'=>$dfhOrderNum,'obligation'=>$dfkOrderNum,'refund_order'=>$tkOrderNum,'audit_order'=>$dshOrderNum,'audit_pro'=>$dshProNum,'ckbz_pro'=>$kcbzNum,'djs_order'=>$djsOrderNum,'dsh_order'=>$shOrderNum,'sj_pro'=>$sjNum,'xj_pro'=>$xjNum,'pro_class'=>$classNum,'pro_brand'=>$brandNum,'sale_pro_sku'=>$saleSkuNum,'pro_sku'=>$skuNum,'customer_num'=>$zkd,'attention_user_num'=>$gzKh,'access_user_num'=>$fwKh,'new_pay_user'=>$newOrderNum,'djs_money'=>$djsje,'ytx_money'=>$ytxje,'return_money'=>$tkje);
                    $data_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'add_date'=>$time,'recycle'=>0);
                    $r_1 = Db::name('mch_statistics')->where($data_where)->update($data_update);
                }
                else
                { // 不存在，添加
                    $data_insert = array('store_id'=>$store_id,'mch_id'=>$mch_id,'pending_shipment'=>$dfhOrderNum,'obligation'=>$dfkOrderNum,'refund_order'=>$tkOrderNum,'audit_order'=>$dshOrderNum,'audit_pro'=>$dshProNum,'ckbz_pro'=>$kcbzNum,'djs_order'=>$djsOrderNum,'dsh_order'=>$shOrderNum,'sj_pro'=>$sjNum,'xj_pro'=>$xjNum,'pro_class'=>$classNum,'pro_brand'=>$brandNum,'sale_pro_sku'=>$saleSkuNum,'pro_sku'=>$skuNum,'customer_num'=>$zkd,'attention_user_num'=>$gzKh,'access_user_num'=>$fwKh,'new_pay_user'=>$newOrderNum,'djs_money'=>$djsje,'ytx_money'=>$ytxje,'return_money'=>$tkje,'add_date'=>$time,'recycle'=>0);
                    $r_1 = Db::name('mch_statistics')->insert($data_insert);
                }
                /* 店铺数据汇总表结束 */

                /* 店铺购买力表开始 */
                $sql1 = "select user_id from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 group by user_id";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    foreach ($r1 as $k1 => $v1) 
                    {
                        $user_id1 = $v1['user_id'];

                        $allMoney = 0; // 获取客户下单的所有金额
                        $sql1_0 = "select ifnull(sum(z_price),0) as sum from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 and user_id = '$user_id1' and status in (1,2,5)";
                        $r1_0 = Db::query($sql1_0);
                        if($r1_0)
                        {
                            $allMoney = $r1_0[0]['sum'];
                        }

                        $sql1_1 = "select * from lkt_mch_buy_power where store_id = '$store_id' and mch_id = '$mch_id' and user_id = '$user_id1' and recycle = 0 ";
                        $r1_1 = Db::query($sql1_1);
                        if($r1_1)
                        { // 存在，修改
                            $data_update = array('money'=>$allMoney,'add_date'=>$time0);
                            $data_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'user_id'=>$user_id1,'recycle'=>0);
                            $r1_2 = Db::name('mch_buy_power')->where($data_where)->update($data_update);
                        }
                        else
                        { // 不存在，添加
                            $data_insert = array('store_id'=>$store_id,'mch_id'=>$mch_id,'user_id'=>$user_id1,'money'=>$allMoney,'add_date'=>$time0,'recycle'=>0);
                            $r1_2 = Db::name('mch_buy_power')->insert($data_insert);
                        }
                    }
                }
                /* 店铺购买力表结束 */

                /* 店铺交易数据表开始 */
                // $sql2 = "select date_format(add_time,'%Y-%m-%d') as add_time from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 group by date_format(add_time,'%Y-%m-%d')";
                // $r2 = Db::query($sql2);
                // if($r2)
                // {
                //     foreach ($r2 as $k2 => $v2) 
                //     {
                //         $add_time = $v2['add_time'];
                        $orderMoney = 0; // 订单总金额
                        $orderNum = 0; // 订单数

                        // $sql2_0 = "select ifnull(sum(z_price),0) as money from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 and date_format(add_time,'%Y-%m-%d') = '$add_time'";
                        $sql2_0 = "select ifnull(sum(z_price),0) as money from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 and add_time >= '$time'";
                        $r2_0 = Db::query($sql2_0);
                        if($r2_0)
                        {
                            $orderMoney = $r2_0[0]['money']; // 订单总金额
                        }

                        // $sql2_1 = "select ifnull(count(1),0) as num from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 and date_format(add_time,'%Y-%m-%d') = '$add_time'";
                        $sql2_1 = "select ifnull(count(1),0) as num from lkt_order where store_id = '$store_id' and mch_id = CONCAT(',','$mch_id',',') and recycle = 0 and add_time >= '$time'";
                        $r2_1 = Db::query($sql2_1);
                        if($r2_1)
                        {
                            $orderNum = $r2_1[0]['num']; // 订单数
                        }

                        // $sql2_2 = "select * from lkt_mch_order_record where store_id = '$store_id' and mch_id = '$mch_id' and date_format(count_day,'%Y-%m-%d') = '$add_time' and recycle = 0 ";
                        $sql2_2 = "select * from lkt_mch_order_record where store_id = '$store_id' and mch_id = '$mch_id' and count_day = '$time' and recycle = 0 ";
                        $r2_2 = Db::query($sql2_2);
                        if($r2_2)
                        { // 存在，修改
                            $data_update = array('order_number'=>$orderNum,'money'=>$orderMoney,'add_date'=>$time0);
                            // $data_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'count_day'=>$add_time,'recycle'=>0);
                            $data_where = array('store_id'=>$store_id,'mch_id'=>$mch_id,'count_day'=>$time,'recycle'=>0);
                            $r2_3 = Db::name('mch_order_record')->where($data_where)->update($data_update);
                        }
                        else
                        { // 不存在，添加
                            // $data_insert = array('store_id'=>$store_id,'mch_id'=>$mch_id,'count_day'=>$add_time,'order_number'=>$orderNum,'money'=>$orderMoney,'add_date'=>$time0,'recycle'=>0);
                            $data_insert = array('store_id'=>$store_id,'mch_id'=>$mch_id,'count_day'=>$time,'order_number'=>$orderNum,'money'=>$orderMoney,'add_date'=>$time0,'recycle'=>0);
                            $r2_3 = Db::name('mch_order_record')->insert($data_insert);
                        }
                //     }
                // }
                /* 店铺交易数据表结束 */
            }
        }
    }

    // 获取店铺ID
    public static function GetMchID($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $user_id = $array['user_id']; // 用户user_id

        $mch_id = Db::name('mch')->where(['store_id'=>$store_id,'user_id'=>$user_id,'review_status'=>1,'recovery'=>0])->value('id');

        return $mch_id;
    }
}
