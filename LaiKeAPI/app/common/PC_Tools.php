<?php

namespace app\common;
use think\facade\Db;
use think\facade\Request;
use app\admin\model\ConfigModel;
use app\admin\model\UserModel;
use app\admin\model\UserGradeModel;
use app\admin\model\ProductConfigModel;
use app\admin\model\ProLabelModel;
use app\admin\model\MessageLoggingModel;
use app\admin\model\DataDictionaryNameModel;
use app\admin\model\DataDictionaryListModel;
use app\admin\model\UserAddressModel;
use app\admin\model\ProductListModel;
use app\admin\model\ConfigureModel;
use app\admin\model\OrderModel;
use app\admin\model\UserCollectionModel;
use app\admin\model\MemberConfigModel;
use app\admin\model\ExpressModel;
use app\admin\model\ProductClassModel;
use app\admin\model\BrandClassModel;
use app\admin\model\ReturnOrderModel;

use app\common\LaiKeLogUtils;
use app\common\JWT;
use app\common\third\logistics\LogisticsTool;
use app\common\ServerPath;
use app\common\LKTConfigInfo;
use app\common\OSSCommon;
use app\common\MinIOServer;

class PC_Tools
{
    public function __construct($store_id, $store_type)
    {
        $this->store_id = $store_id;
        $this->store_type = $store_type;
        $this->get_config($store_id);
    }

    public function get_config($store_id)
    {
        $r = ConfigModel::where('store_id', $store_id)
                        ->select()
                        ->toArray();
        if ($r)
        {
            $this->config = $r[0];
        }
        else
        {
            $this->config = array();
        }
    }

    // 获取会员折扣
    public static function Member_discount($array)
    {
        $store_id = $array['store_id'];
        $access_id = $array['access_id'];
        $grade = 0;
        $grade_rate = 1;
        if($access_id != '')
        {
            $r0 = UserModel::where('access_id', $access_id)
                            ->field('user_id,parameter,grade')
                            ->select()
                            ->toArray();
            if ($r0)
            {
                $grade = $r0[0]['grade'];
                $r0_0 = MemberConfigModel::where(['store_id'=>$store_id,'is_open'=>1])
                                    ->field('member_discount')
                                    ->select()
                                    ->toArray();
                if ($r0_0)
                {
                    $grade_rate = $r0_0[0]['member_discount'] / 10;
                }
            }
        }

        $data = array('grade'=>$grade,'grade_rate'=>$grade_rate);
        return $data;
    }

    // 获取商品设置
    public static function getProductSettings($array)
    {
        $store_id = $array['store_id'];
        $type = $array['type'];
        $is_open = 0;
        $is_display_sell_put = 0;
        $condition = '';
        // 查询
        $r_con = ProductConfigModel::where('store_id', $store_id)->field('is_open,is_display_sell_put')->select()->toArray();
        if ($r_con)
        {
            $is_open = $r_con[0]['is_open']; // 是否显示下架商品 0.不显示  1.显示
            $is_display_sell_put = $r_con[0]['is_display_sell_put']; // 是否显示已售罄商品 0.不显示  1.显示
        }

        if($type == 1)
        {
            if($is_open == 1)
            { // 显示下架商品
                if($is_display_sell_put == 1)
                { // 显示售罄商品
                    $condition = " and a.status in (2,3) ";
                }
                else
                { // 不显示售罄商品
                    $condition = " and ((a.status = 2 and a.num > 0) or (a.status = 3)) ";
                }
            }
            else
            { // 不显示下架商品
                if($is_display_sell_put == 1)
                { // 显示售罄商品
                    $condition = " and a.status = 2 ";
                }
                else
                { // 不显示售罄商品
                    $condition = " and a.status = 2 and a.num > 0 ";
                }
            }
        }
        else if($type == 2)
        {
            if ($is_open == 1)
            {
                $condition = '2,3';
            }
            else
            {
                $condition = '2';
            }
        }
        
        return $condition;
    }

    // // 获取商品设置
    // public static function getProductSettings($array)
    // {
    //     $store_id = $array['store_id'];
    //     $is_open = 0;
    //     $status = '2';
    //     // 查询
    //     $r_con = ProductConfigModel::where('store_id', $store_id)
    //                     ->field('is_open')
    //                     ->select()
    //                     ->toArray();
    //     if ($r_con)
    //     {
    //         $is_open = $r_con[0]['is_open'];
    //     }

    //     if ($is_open == 1)
    //     {
    //         $status = '2,3';
    //     }
    //     return $status;
    // }

    // 获取商品已售罄设置
    public static function getProductSoldOutSettings($array)
    {
        $store_id = $array['store_id'];
        $is_display_sell_put_str = " and a.num >= 0 ";
        $is_display_sell_put = 0;
        // 查询
        $r_con = ProductConfigModel::where('store_id', $store_id)
                        ->field('is_display_sell_put')
                        ->select()
                        ->toArray();
        if ($r_con)
        {
            $is_display_sell_put = $r_con[0]['is_display_sell_put'];
        }

        if ($is_display_sell_put != 1)
        {
            $is_display_sell_put_str = " and a.num > 0 ";
        }

        return $is_display_sell_put_str;
    }

    // 获取商品标签
    public static function getProductLabel($array)
    {
        $store_id = $array['store_id'];
        $s_type = $array['s_type'];

        $s_type_list = array();
        $r_sp_type = ProLabelModel::where('store_id', $store_id)->field('id,name')->order('add_time','desc')->select()->toArray();
        if ($r_sp_type)
        {
            foreach ($r_sp_type as $k => $v)
            {
                if(in_array($v['id'],$s_type))
                {
                    $s_type_list[] = $v['name'];
                }
            }
        }

        return $s_type_list;
    }

    // 获取商品标签
    public static function getProductLabel0($array)
    {
        $store_id = $array['store_id'];
        $s_type = $array['s_type'];

        $s_type_list = array();
        $r_sp_type = ProLabelModel::where('store_id', $store_id)->field('id,name,color')->order('add_time','desc')->select()->toArray();
        if ($r_sp_type)
        {
            foreach ($r_sp_type as $k => $v)
            {
                if(in_array($v['id'],$s_type))
                {
                    $s_type_list[] = array('name' => $v['name'], 'id' =>$v['id'], 'color' =>$v['color']);
                }
            }
        }

        return $s_type_list;
    }

    // 重组属性
    public static function getArrSet($arrs,$_current_index=-1)
    {
        //总数组
        static $_total_arr;
        //总数组下标计数
        static $_total_arr_index;
        //输入的数组长度
        static $_total_count;
        //临时拼凑数组
        static $_temp_arr;

        //进入输入数组的第一层，清空静态数组，并初始化输入数组长度
        if($_current_index<0)
        {
            $_total_arr=array();
            $_total_arr_index=0;
            $_temp_arr=array();
            $_total_count=count($arrs)-1;
            PC_Tools::getArrSet($arrs,0);
        }
        else
        {
            //循环第$_current_index层数组
            foreach($arrs[$_current_index] as $v)
            {
                //如果当前的循环的数组少于输入数组长度
                if($_current_index<$_total_count)
                {
                    //将当前数组循环出的值放入临时数组
                    $_temp_arr[$_current_index]=$v;
                    //继续循环下一个数组
                    PC_Tools::getArrSet($arrs,$_current_index+1);
                }
                //如果当前的循环的数组等于输入数组长度(这个数组就是最后的数组)
                else if($_current_index==$_total_count)
                {
                    //将当前数组循环出的值放入临时数组
                    $_temp_arr[$_current_index]=$v;
                    //将临时数组加入总数组
                    $_total_arr[$_total_arr_index]=$_temp_arr;
                    //总数组下标计数+1
                    $_total_arr_index++;
                }
            }
        }
        return $_total_arr;
    }

    // 消息-改为已经弹窗（店铺）
    public static function message_pop_up($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $parameter = $array['parameter'];
        $type = $array['type'];

        $sql_message_logging = MessageLoggingModel::update(['is_popup' => 1], ['store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$parameter,'type'=>$type]);

        return;
    }

    // 消息-改为已读（店铺）
    public static function message_read($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $parameter = $array['parameter'];
        $type = $array['type'];

        $sql_message_logging = MessageLoggingModel::update(['read_or_not' => 1], ['store_id'=>$store_id,'mch_id'=>$mch_id,'parameter'=>$parameter,'type'=>$type]);

        $sql_0 = "select * from lkt_message_logging where store_id = '$store_id' and mch_id = '$mch_id' and parameter = '$parameter' and type = '$type' ";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            $content = $r_0[0]['content'];
            $add_date = $r_0[0]['add_date'];

            $sql_2 = "update lkt_message_logging set read_or_not = 1 where store_id = '$store_id' and mch_id = '$mch_id' and content = '$content' and add_date = '$add_date' ";
            $r_2 = Db::execute($sql_2);
        }

        return;
    }

    // 消息-改为已经弹窗（供应商）
    public static function message_pop_up_sup($array)
    {
        $store_id = $array['store_id'];
        $supplier_id = $array['supplier_id'];
        $parameter = $array['parameter'];
        $type = $array['type'];

        $sql_message_logging = MessageLoggingModel::update(['is_popup' => 1], ['store_id'=>$store_id,'supplier_id'=>$supplier_id,'parameter'=>$parameter,'type'=>$type]);

        return;
    }

    // 消息-改为已读（供应商）
    public static function message_read_sup($array)
    {
        $store_id = $array['store_id'];
        $supplier_id = $array['supplier_id'];
        $parameter = $array['parameter'];
        $type = $array['type'];

        $sql_message_logging = MessageLoggingModel::update(['read_or_not' => 1], ['store_id'=>$store_id,'supplier_id'=>$supplier_id,'parameter'=>$parameter,'type'=>$type]);

        return;
    }

    // 添加跳转路径
    public static function jump_path($array)
    {
        $time = date("Y-m-d H:i:s");
        $store_id = $array['store_id'];
        $type0 = $array['type0'];
        $id = $array['id'];
        $name = $array['name'];
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = $array['lang_code']; // 语种
        }
        $country_num = "";
        if(isset($array['country_num']))
        {
            $country_num = $array['country_num']; // 国家代码
        }
        $url = '';
        $url1 = '';
        $parameter = '';
        $parameter1 = '';
        if($type0 == 1)
        {
            $url = "/pagesC/goods/goods";
            $url1 = "index.html?module=homeList&action=homeList";
            $parameter = "cid=" . $id;
            $parameter1 = "cid=" . $id;
        }
        else if($type0 == 2)
        {
            $url = "/pagesC/goods/goodsDetailed";
            $url1 = "index.html?module=homedetail&action=homedetail";
            $parameter = "productId=" . $id;
            $parameter1 = "id=" . $id;
        }
        else if($type0 == 3)
        {
            $url = "/pagesB/store/store";
            $parameter = "shop_id=" . $id;
        }

        if($url != '')
        {
            $sql0 = array('store_id'=>$store_id,'type0'=>$type0,'type'=>1,'name'=>$name,'url'=>$url,'status'=>1,'parameter_status'=>true,'parameter'=>$parameter,'add_date'=>$time,'sid'=>$id,'lang_code'=>$lang_code,'country_num'=>$country_num);
            $r0 = Db::name('jump_path')->save($sql0);
        }

        if($url1 != '')
        {
            $sql1 = array('store_id'=>$store_id,'type0'=>$type0,'type'=>2,'name'=>$name,'url'=>$url,'status'=>1,'parameter_status'=>true,'parameter'=>$parameter,'add_date'=>$time,'sid'=>$id,'lang_code'=>$lang_code,'country_num'=>$country_num);
            $r1 = Db::name('jump_path')->save($sql1);
        }
    }

    // 修改跳转路径
    public static function modify_jump_path($array)
    {
        $time = date("Y-m-d H:i:s");
        $store_id = $array['store_id'];
        $type0 = $array['type0'];
        $id = $array['id'];
        $name = $array['name'];
        $lang_code = "";
        if(isset($array['lang_code']))
        {
            $lang_code = $array['lang_code']; // 语种
        }
        $country_num = "";
        if(isset($array['country_num']))
        {
            $country_num = $array['country_num']; // 国家代码
        }
        $url = '';
        $url1 = '';
        $parameter = '';
        $parameter1 = '';
        if($type0 == 1)
        {
            $url = "/pagesC/goods/goods";
            $url1 = "index.html?module=homeList&action=homeList";
            $parameter = "cid=" . $id;
            $parameter1 = "cid=" . $id;
        }
        else if($type0 == 2)
        {
            $url = "/pagesC/goods/goodsDetailed";
            $url1 = "index.html?module=homedetail&action=homedetail";
            $parameter = "productId=" . $id;
            $parameter1 = "id=" . $id;
        }
        else if($type0 == 3)
        {
            $url = "/pagesB/store/store";
            $parameter = "shop_id=" . $id;
        }

        if($url != '')
        {
            $sql0 = array('store_id'=>$store_id,'type0'=>$type0,'type'=>1,'parameter'=>$parameter,'lang_code'=>$lang_code,'country_num'=>$country_num);
            $r0 = Db::name('jump_path')->where($sql0)->update(['name' => $name]);
        }

        if($url1 != '')
        {
            $sql1 = array('store_id'=>$store_id,'type0'=>$type0,'type'=>2,'parameter'=>$parameter,'lang_code'=>$lang_code,'country_num'=>$country_num);
            $r1 = Db::name('jump_path')->where($sql1)->update(['name' => $name]);
        }
        return;
    }

    // 添加消息记录
    public static function add_message_logging($array)
    {
        $time = date("Y-m-d H:i:s");
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $gongyingshang = 0;
        if($mch_id == 0)
        {
            if(isset($array['gongyingshang']))
            {
                $gongyingshang = $array['gongyingshang'];
            }
        }

        $source = '';
        if(isset($array['source']))
        {
            $source = $array['source'];
        }
        $type = $array['type'];
        $parameter = $array['parameter'];
        $content = $array['content'];
        $otype = substr($parameter, 0, 2);//获取订单号前两位字母（类型）

        $audit_status = 1;
        if(isset($array['audit_status']))
        {
            $audit_status = $array['audit_status'];
        }

        $to_url = "";
        // type :消息类型 1.订单(待发货) 2.订单(售后) 3.订单(提醒发货) 4.订单(订单关闭) 5.订单(新订单) 6.订单(收货) 7.商品(审核) 8.商品(下架) 9.商品(补货) 10.商品(新商品上架) 11.商品(分类) 12.商品(品牌) 13.提现 14.违规下架 15.商品删除 16.保证金审核消息通知 17.店铺商品审核消息通知(移动端店铺) 18.店铺商品审核消息通知(PC店铺) 19.用户提现 20.店铺提现 21.供应商提现 22.h5店铺用户提交竞拍保证金提醒 23.h5店铺消息通知(您的店铺提现申请提交成功，正在等待管理员审核！+ 审核通过 + 审核失败) 24.pc店铺提现审核消息通知(您的店铺提现申请提交成功，正在等待管理员审核！ + 审核通过 + 审核失败) 25.pc店铺保证金审核消息通知  pc店铺用户提交竞拍保证金提醒 30.店铺申请退还保证金
        if($type == 1 || $type == 3 || $type == 4 || $type == 5 || $type == 6)
        {
            if($otype == 'GM')
            {
                $sql_o = "select supplier_id,self_lifting,otype from lkt_order where sNo = '$parameter' ";
                $r_o = Db::query($sql_o);
                if($r_o[0]['supplier_id'] == 0)
                {
                    $to_url = "/order/orderList/orderLists";
                    if($r_o[0]['otype'] == 'VI')
                    {
                        $to_url = "/order/orderList/orderLists?selfLifting=3";
                    }
                    else
                    {
                        if($r_o[0]['self_lifting'] == 1)
                        { // 自提
                            $to_url = "/order/orderList/orderLists?selfLifting=2";
                        }
                        else if($r_o[0]['self_lifting'] == 2)
                        { // 商家自配
                            $to_url = "/order/orderList/orderLists?selfLifting=5";
                        }
                    }
                }
                else
                {
                    $to_url = "/plug_ins/supplier/supplierOrder";
                }
            }
            else if($otype == 'KJ' || $otype == 'PT')
            {
                $to_url = "/plug_ins/group/groupOrderList";
            }
            else if($otype == 'FX')
            {
                $to_url = "/plug_ins/distribution/distributorsOrderList";
            }
            else if($otype == 'JP')
            {
                $to_url = "/plug_ins/auction/auctionOrder";
            }
            else if($otype == 'MS')
            {
                $to_url = "/plug_ins/seckill/seckillOrder";
            }
            else if($otype == 'IN')
            {
                $to_url = "/plug_ins/integralMall/integralOrder";
            }
            else if($otype == 'PS')
            {
                $to_url = "/plug_ins/preSale/preSaleOrder";
            }
        }
        else if($type == 2)
        {
            if($otype == 'PS')
            {
                $to_url = "/plug_ins/preSale/afterSaleList";
            }
            else 
            {
                $to_url = "/order/salesReturn/salesReturnList";
            }
        }
        else if($type == 7)
        { // 商品(审核) --- 供应商ID为0，平台消息。供应商ID不为0，供应商端消息
            if($source == 'mch')
            { // 管理后台供应商商品审核通知前端路由路径
                $to_url = "/plug_ins/stores/goodsAudit";
            }
            else if($source == 'supplier')
            {
                $to_url = "/plug_ins/supplier/poolReviewed";
            }
        }
        else if($type == 18)
        { // 店铺商品审核消息通知(PC店铺)
            if (strpos($content,"通过：" ) !== false) 
            {
                $to_url = "/goods/goodslist/physicalgoods";
            } 
            else 
            {
                $to_url = "/goods/auditGoods";
            }
        }
        else if($type == 19)
        { // 用户提现
            // $to_url = "/finance/withdrawalManage/withdrawalExamineList";
            $to_url = "/members/withdrawalManage/withdrawalRecordList";
            
        }
        else if($type == 20)
        { // 店铺提现
            $to_url = "/plug_ins/stores/withdrawalAudit";
        }
        else if($type == 21)
        { // 供应商提现
            if($gongyingshang == 0)
            {
                $to_url = "/plug_ins/supplier/withdrawalAudit";
            }
        }
        else if($type == 24)
        {
            $to_url = "/moneyManagement/withdrawalDetails/detailsList";
        }
        else if($type == 30)
        { // 店铺申请退还保证金
            $to_url = "/plug_ins/stores/bondExamine";
        }

        if($mch_id != 0)
        {
            $sql_message_logging = array('store_id'=>$store_id,'mch_id'=>$mch_id,'type'=>$type,'content'=>$content,'parameter'=>$parameter,'add_date'=>$time,'to_url'=>$to_url);
        }
        else
        {
            $sql_message_logging = array('store_id'=>$store_id,'supplier_id'=>$gongyingshang,'type'=>$type,'content'=>$content,'parameter'=>$parameter,'add_date'=>$time,'to_url'=>$to_url);
        }
        
        $r = Db::name('message_logging')->insert($sql_message_logging);

        return;
    }

    // 数据字典
    public static function data_dictionary($name, $values)
    {
        $list = array();
        $list1 = array();
        $list2 = array();

        // 根据参数，查询数据字典名称
        $r0 = DataDictionaryNameModel::where(['name'=>$name,'status'=>1,'recycle'=>0])->select()->toArray();
        if ($r0)
        {
            $id = $r0[0]['id'];
            $list1 = $r0[0];

            $r1 = DataDictionaryListModel::where(['sid'=>$id,'status'=>1,'recycle'=>0])->select()->toArray();
            if ($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    $status = false;
                    if ($values == $v['value'])
                    {
                        $status = true;
                    }
                    $list2[] = $v;
                }
            }
        }
        $list = array('name'=>$list1,'value'=>$list2);
        return $list;
    }

    //查询默认地址
    public static function find_address($store_id, $user_id,$no_delivery_list, $address_id = '')
    {
        $address = array();
        $no_delivery_address_id_str = '';
        if($address_id == '')
        {
            if($no_delivery_list != array())
            { // 循环不配送地址
                foreach($no_delivery_list as $k => $v)
                {
                    // 根据省的名称，查询该用户的地址ID
                    $r2 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id,'sheng'=>$v])->field('id')->select()->toArray();
                    if($r2)
                    {
                        foreach($r2 as $k2 => $v2)
                        {
                            $no_delivery_address_id_str .= $v2['id'] . ',';
                        }
                    }
                }
                $no_delivery_address_id_str = trim($no_delivery_address_id_str,','); // 不配送省的ID
            }
            // 查询用户地址
            if($no_delivery_address_id_str == '')
            {
                $r3 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->select()->toArray();
            }
            else
            {
                $r3 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->whereNotIn('id',$no_delivery_address_id_str)->limit(1)->order('id','desc')->select()->toArray();
            }
            if ($r3)
            {
                foreach($r3 as $k => $v)
                {
                    $v['no_delivery_status'] = false; // 可以配送
                    $v['status'] = false; // 默认不选中
                    if($v['is_default'] == 1)
                    {
                        $v['status'] = true; // 选中
                    }
                    $address[] = (array)$v; // 收货地址
                }
            }
            else
            {
                $r4 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->select()->toArray();
                if($r4)
                {
                    foreach($r4 as $k => $v)
                    {
                        $v['no_delivery_status'] = true; // 不可以配送
                        $v['status'] = false; // 不选中
                        $address[] = (array)$v; // 收货地址
                    }
                }
            }
        }
        else
        {
            $r0 = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id])->select()->toArray();
            if($r0)
            {
                foreach($r0 as $k => $v)
                {
                    $v['no_delivery_status'] = false; // 可以配送
                    $v['status'] = false; // 默认不选中
                    if($v['id'] == $address_id)
                    {
                        if(in_array($v['sheng'],$no_delivery_list))
                        {
                            $v['status'] = false;
                        }
                        else
                        {
                            $v['status'] = true; // 选中
                        }
                    }
                    $address[] = (array)$v; // 收货地址
                }
            }
        }

        return $address;
    }

    // 区分购物车结算和立即购买---列出选购商品
    public static function products_list($store_id, $cart_id, $product, $product_type = 'PT',$buy_type = 0)
    {
        $products = [];
        if (!empty($cart_id))
        {
            if (is_string($cart_id))
            {
                //是字符串
                $typestr = trim($cart_id, ','); // 移除两侧的逗号
                $cartArr = explode(',', $typestr); // 字符串打散为数组
            }
            else
            {
                $cartArr = $cart_id;
            }
            foreach ($cartArr as $key => $value)
            {   
                if($buy_type != 0)
                {
                    $y_cart = BuyAgainModel::where(['store_id'=>$store_id,'id'=>$value])->where('Goods_num','>',0)->field('Goods_id,Size_id,Goods_num')->select()->toArray();
                }
                else
                { // 根据购物车id，查询购物车信息
                    $y_cart = CartModel::where(['store_id'=>$store_id,'id'=>$value])->where('Goods_num','>',0)->field('Goods_id,Size_id,Goods_num')->select()->toArray();
                }
                
                if ($y_cart)
                {
                    $products[$key] = array('pid' => $y_cart['0']['Goods_id'], 'cid' => $y_cart['0']['Size_id'], 'num' => $y_cart['0']['Goods_num']);
                }
            }
        }
        else
        {
            $arr = array();
            if (empty($product))
            {
                $message = Lang('tools.12');
                echo json_encode(array('code' => ERROR_CODE_LBYSPKCBZ, 'message' => $message));
                exit;
            }
            else
            {
                $typestr = trim($product, ','); // 移除两侧的逗号
                $cartArr = explode(',', $typestr); // 字符串打散为数组

                $arr['pid'] = $cartArr[0];
                $arr['cid'] = $cartArr[1];
                $arr['num'] = $cartArr[2];

                $r0 = ProductListModel::where('id',$cartArr[0])->field('status')->select()->toArray();
                if ($r0)
                {
                    $status = $r0[0]['status'];
                    if ($status == 1)
                    {
                        $message = Lang('tools.13');
                        echo json_encode(array('code' => ERROR_CODE_SPWSJ, 'message' => $message));
                        exit;
                    }
                    $r1 = ConfigureModel::where(['pid'=>$cartArr[0],'id'=>$cartArr[1]])->field('num')->select()->toArray();
                    if ($r1)
                    {
                        $num = $r1[0]['num'];
                        if ($num < $cartArr[2])
                        {
                            if ($product_type != 'KJ')
                            {
                                $message = Lang('tools.14');
                                echo json_encode(array('code' => ERROR_CODE_KCBZ, 'message' => $message));
                                exit;
                            }
                        }
                    }
                    else
                    {
                        $message = Lang('Parameter error');
                        echo json_encode(array('code' => ERROR_CODE_SPCSCW, 'message' => $message));
                        exit;
                    }
                }
                else
                {
                    $message = Lang('Parameter error');
                    echo json_encode(array('code' => ERROR_CODE_SPCSCW, 'message' => $message));
                    exit;
                }
            }
            $products[0] = array('pid' => $arr['pid'], 'cid' => $arr['cid'], 'num' => $arr['num'] ? $arr['num'] : 1);
        }
        if (empty($products))
        {
            $message = Lang('tools.12');
            echo json_encode(array('code' => ERROR_CODE_LBYSPKCBZ, 'message' => $message));
            exit;
        }

        $list = array();
        $list0 = array();
        $list1 = array();
        $list2 = array();

        foreach($products as $k => $v)
        {
            if(in_array($v['pid'],$list0))
            {
                if($v['num'] > $list1[$v['pid']])
                {
                    $list1[$v['pid']] = $v['num'];
                }
                $list[$v['pid']] += $v['num'];
            }
            else
            {
                $list0[] = $v['pid'];
                $list[$v['pid']] = $v['num'];
                $list1[$v['pid']] = $v['num'];
            }
        }
        
        foreach($products as $k => $v)
        {
            if($v['num'] == $list1[$v['pid']])
            {
                if(!in_array($v['pid'],$list2))
                {
                    $list2[] = $v['pid'];
                    $products[$k]['tongbu'] = 1;
                }
                else
                {
                    $products[$k]['tongbu'] = 2;
                }
            }
            else
            {
                $products[$k]['tongbu'] = 2;
            }
            $products[$k]['merge_num'] = $list[$v['pid']];
        }

        return $products;
    }
    
    // 一键已读
    public static function one_click_read($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $supplier_id = $array['supplier_id'];
        
        $type = $array['type'];
        $ids = '';
        if(isset($array['id']))
        {
            $ids = $array['id'];
        }

        if($ids == '')
        {
            $r0 = MessageLoggingModel::where(['store_id'=>$store_id,'read_or_not'=>0,'supplier_id'=>$supplier_id])->whereIn('mch_id',$mch_id)->whereIn('type',$type)->field('id,content,add_date')->select()->toArray();
            if($r0)
            {
                foreach ($r0 as $k => $v)
                {
                    $id = $v['id'];
                    $content = $v['content'];
                    $add_date = $v['add_date'];
    
                    $sql_where = array('store_id'=>$store_id,'id'=>$id);
                    $sql_update = array('read_or_not'=>1);
                    $r = Db::name('message_logging')->where($sql_where)->update($sql_update);

                    $sql_2 = "update lkt_message_logging set read_or_not = 1 where store_id = '$store_id' and mch_id = '$mch_id' and supplier_id = '$supplier_id' and content = '$content' and add_date = '$add_date' ";
                    $r_2 = Db::execute($sql_2);
                }
            }
        }
        else
        {
            $sql_where = array('store_id'=>$store_id,'id'=>$ids);
            $sql_update = array('read_or_not'=>1);
            $r = Db::name('message_logging')->where($sql_where)->update($sql_update);

            $sql_0 = "select * from lkt_message_logging where store_id = '$store_id' and mch_id = '$mch_id' and supplier_id = '$supplier_id' and id = '$ids' ";
            $r_0 = Db::query($sql_0);
            if($r_0)
            {
                $content = $r_0[0]['content'];
                $add_date = $r_0[0]['add_date'];

                $sql_2 = "update lkt_message_logging set read_or_not = 1 where store_id = '$store_id' and mch_id = '$mch_id' and content = '$content' and add_date = '$add_date' ";
                $r_2 = Db::execute($sql_2);
            }
        }
       
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message));
        exit;
    }

    // 消息数量
    public static function news_num($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $type = $array['type'];
        $supplier_id = $array['supplier_id'];

        $total = 0;
        $r0 = MessageLoggingModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'read_or_not'=>0,'type'=>$type,'supplier_id'=>$supplier_id])->field('count(id) as total')->select()->toArray();
        if($r0)
        {
            $total = $r0[0]['total'];
        }
        return $total;
    }

    // 消息数据
    public static function news_list($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $type = $array['type'];
        $supplier_id = $array['supplier_id'];

        $list = array();
        $r0 = MessageLoggingModel::where(['store_id'=>$store_id,'mch_id'=>$mch_id,'read_or_not'=>0,'type'=>$type,'supplier_id'=>$supplier_id])->field('id,parameter,content,add_date,is_popup,to_url')->select()->toArray();
        if($r0)
        {
            foreach ($r0 as $key => $value) 
            {
                $r0[$key]['toUrl'] = $value['to_url'];
            }
            $list = $r0;
        }
        return $list;
    }
    
    // 获取店铺数据
    public static function StoreData($store_id,$shop_id,$status)
    {
        $quantity_on_sale = 0; // 在售数量
        $quantity_sold = 0; // 已售数量
        $collection_num = 0; // 收藏数量
        $r0 = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'mch_status'=>2,'recycle'=>0,'active'=>1,'status'=>2])->whereNotIn('commodity_type','2')->count('id');
        if($r0)
        {
            $quantity_on_sale = $r0;// 在售数量
        }

        $sql1 = "select tt.* from (select a.real_volume as volume,row_number () over (partition by c.pid) as top from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.mch_id = '$shop_id' and a.mch_status = 2 and a.recycle = 0 and a.active = 1 and a.commodity_type != 2) as tt where tt.top<2 ";
        $r1 = Db::query($sql1);
        if ($r1)
        {
            foreach ($r1 as $k => $v)
            {
                $quantity_sold += $v['volume'];  // 已售数量
            }
        }
        if ($quantity_sold < 0)
        {
            $quantity_sold = 0;
        }

        $r2 = UserCollectionModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->field('id')->select()->toArray();
        if($r2)
        {
            $collection_num = count($r2);
        }

        $list = array('collection_num'=>$collection_num,'quantity_on_sale'=>$quantity_on_sale,'quantity_sold'=>$quantity_sold);
        return $list;
    }

    // 获取自营店ID
    public static function SelfOperatedStore($store_id)
    {
        $SelfOperatedStore_id = 0;
        $sql = "select shop_id from lkt_admin where type = 1 and recycle = 0 and store_id = '$store_id' ";
        $r = Db::query($sql);
        if ($r)
        {
            $SelfOperatedStore_id = $r[0]['shop_id'];
        }
        
        return $SelfOperatedStore_id;
    }

    // 添加余额详情
    public static function add_Balance_details($data)
    {
        $time = date('Y-m-d H:i:s');
        
        if($data['money_type'] == 1)
        {
            $data['user_money'] = $data['user_money'] + $data['money'];
        }
        else
        {
            $data['user_money'] = $data['user_money'] - $data['money'];
        }
        $data['record_time'] = $time;
        $data['add_time'] = $time;

        $r0 = Db::name('record_details')->insertGetId($data);
        
        return $r0;
    }

    // 查看物流
    public static function View_logistics($data)
    {
        $store_id = $data['store_id'];
        $sNo = $data['sNo'];
        $list = array();
        $o_source = 1;
        if(isset($data['o_source']))
        {
            $o_source = $data['o_source'];
        }

        $express_id_list = array(); // 快递ID数组
        $express_list = array(); // 快递数组
        if($o_source == 1)
        { // 订单
            $sql1 = "select mobile from lkt_order where sNo = '$sNo' ";
            $r1 = Db::query($sql1);
            $mobile = $r1[0]['mobile'];
            
            $sql0 = "select a.order_details_id,a.express_id,a.courier_num,a.num,b.sid,a.logistics from lkt_express_delivery as a left join lkt_order_details as b on a.order_details_id = b.id where a.store_id = '$store_id' and a.sNo = '$sNo' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                foreach($r0 as $k0 => $v0)
                {
                    if (!empty($v0['express_id']) && !empty($v0['courier_num']))
                    {
                        $order_details_id = $v0['order_details_id']; // 订单详情ID
                        $express_id = $v0['express_id']; // 快递公司ID
                        $courier_num = $v0['courier_num']; // 快递单号
                        $sid = $v0['sid'];
                        $logistics = $v0['logistics'];

                        $r01 = ExpressModel::where('id',$express_id)->select()->toArray();
                        $type = $r01[0]['type'];//快递公司代码
                        $kuaidi_name = $r01[0]['kuaidi_name']; // 快递公司名称

                        $size_res = ConfigureModel::where('id',$sid)->select()->toArray();
                        $img = ServerPath::getimgpath($size_res[0]['img'], $store_id);// 商品图片

                        if($logistics != '')
                        {
                            $express_list[$courier_num]['courier_num'] = $courier_num;
                            $express_list[$courier_num]['kuaidi_name'] = $kuaidi_name;
                            $express_list[$courier_num]['type'] = $type;
                            $express_list[$courier_num]['shop_type'] = 1; // 普通物流
                            $express_list[$courier_num]['list'] = json_decode($logistics,true);
                            $express_list[$courier_num]['logistics_method'] = 1;
                            $express_list[$courier_num]['pro_list'][] = array('num'=>$v0['num'],'img'=>$img);
                        }
                        else
                        {
                            $res_1 = LogisticsTool::getLogistics($type, $courier_num,$store_id,$mobile);

                            
                            if(!in_array($courier_num,$express_id_list))
                            {
                                $express_id_list[] = $courier_num;
                                $express_list[$courier_num]['courier_num'] = $courier_num;
                                $express_list[$courier_num]['kuaidi_name'] = $kuaidi_name;
                                $express_list[$courier_num]['type'] = $type;
                                $express_list[$courier_num]['shop_type'] = 1; // 普通物流
                                $express_list[$courier_num]['list'] = $res_1;
                                $express_list[$courier_num]['logistics_method'] = 0;
                                $express_list[$courier_num]['pro_list'][] = array('num'=>$v0['num'],'img'=>$img);
                            }
                            else
                            {
                                $express_list[$courier_num]['logistics_method'] = 0;
                                $express_list[$courier_num]['pro_list'][] = array('num'=>$v0['num'],'img'=>$img);
                            }
                        }
                    }
                }
            }
        }
        else
        {
            // 根据商城ID、订单号，查询售后订单
            $sql0 = "select sid from lkt_return_order where store_id = '$store_id' and id = '$sNo' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $sid = $r0[0]['sid'];

                $sql1 = "select tel,express,express_num,logistics from lkt_return_goods where store_id = '$store_id' and re_id = '$sNo' order by id desc ";
                $r1 = Db::query($sql1);

                $num_1 = count($r1);
                if($r1)
                {
                    foreach ($r1 as $k1 => $v1) 
                    {
                        $express = $v1['express'];//快递公司ID
                        $courier_num = $v1['express_num'];//快递单号
                        $mobile = $v1['tel'];//快递单号
                        $logistics = $v1['logistics'];

                        $r01 = ExpressModel::where(['kuaidi_name'=>$express])->select()->toArray();
                        $express_id = $r01[0]['id'];//快递公司代码
                        $type = $r01[0]['type'];//快递公司代码
                        $kuaidi_name = $r01[0]['kuaidi_name']; // 快递公司名称

                        $size_res = ConfigureModel::where('id',$sid)->select()->toArray();
                        $img = ServerPath::getimgpath($size_res[0]['img'], $store_id);// 商品图片

                        if($logistics != '')
                        {
                            $express_list[$courier_num]['courier_num'] = $courier_num;
                            $express_list[$courier_num]['kuaidi_name'] = $kuaidi_name;
                            $express_list[$courier_num]['type'] = $type;
                            $express_list[$courier_num]['shop_type'] = 1; // 普通物流
                            $express_list[$courier_num]['list'] = json_decode($logistics,true);
                            $express_list[$courier_num]['logistics_method'] = 1;
                            $express_list[$courier_num]['pro_list'][] = array('num'=>$v0['num'],'img'=>$img);
                        }
                        else
                        {
                            $res_1 = LogisticsTool::getLogistics($type, $courier_num,$store_id,$mobile);

                            $num = 0;

                            if(!in_array($courier_num,$express_id_list))
                            {
                                $express_id_list[] = $courier_num;
                                $express_list[$courier_num]['courier_num'] = $courier_num;
                                $express_list[$courier_num]['kuaidi_name'] = $kuaidi_name;
                                $express_list[$courier_num]['type'] = $type;
                                if($num_1 == 1)
                                {
                                    $express_list[$courier_num]['shop_type'] = 3; // 买家回寄
                                }
                                else
                                {
                                    if($k1 == 0)
                                    {
                                        $express_list[$courier_num]['shop_type'] = 2; // 商家回寄
                                    }
                                    else
                                    {
                                        $express_list[$courier_num]['shop_type'] = 3; // 买家回寄
                                    }
                                }
                                $express_list[$courier_num]['list'] = $res_1;
                                $express_list[$courier_num]['logistics_method'] = 0;
                                $express_list[$courier_num]['pro_list'][] = array('num'=>$num,'img'=>$img);
                            }
                            else
                            {
                                $express_list[$courier_num]['logistics_method'] = 0;
                                $express_list[$courier_num]['pro_list'][] = array('num'=>$num,'img'=>$img);
                            }
                        }
                    }
                }
            }
        }
        
        if($express_list != array())
        {
            foreach($express_list as $k => $v)
            {
                $list[] = $v;
            }
        }

        return $list;
    }

    // 判断物流类型
    public static function Determine_logistics_type($store_id,$mch_id)
    {
        $logistics_type = false; // false：获取lkt_express  true：获取lkt_express_subtable

        $sql = "select * from lkt_express_subtable where store_id = '$store_id' and mch_id = '$mch_id' and recovery = 0 ";
        $r = Db::query($sql);
        if($r)
        {
            $logistics_type = true;
        }
        
        return $logistics_type;
    }

    // 获取公告
    public static function GetAnnouncement($array)
    {
        $store_id = $array['store_id'];
        $store_type = $array['store_type'];
        $read_id = $array['read_id'];
        $admin_type = 1;
        if(isset($array['admin_type']))
        {
            $admin_type = $array['admin_type'];
        }

        $info = '';
        $tell_id = '';
        $systemMsgTitle = '';
        $systemMsg = '';
        $systemMsgStartDate = '';
        $systemMsgEndDate = '';
        $systemMsgType = '';
        $time_System = date('Y-m-d H:i:s'); // 当前时间

        if($admin_type == 0)
        {
            $data = array('info' => $info,'tell_id'=>$tell_id,'systemMsgTitle'=>$systemMsgTitle,'systemMsg'=>$systemMsg,'systemMsgStartDate'=>$systemMsgStartDate,'systemMsgEndDate'=>$systemMsgEndDate,'systemMsgType'=>$systemMsgType);
            return $data;
        }

        $store_tell = 1; // 管理后台公告  1未选择 2选择
        $user_tell = 1; // 用户公告  1未选择 2选择
        $mch_tell = 1; // 商户公告  1未选择 2选择
        $supplier_tell = 1; // 供应商公告  1未选择 2选择
        $mch_son_tell = 1; // 门店端公告  1未选择 2选择

        $con = "";
        if($store_type == 1 || $store_type == 2 || $store_type == 3 || $store_type == 4 || $store_type == 5 || $store_type == 6 || $store_type == 11 )
        {
            $con = " and user_tell = 2 ";
        }
        else if($store_type == 7)
        {
            $con = " and mch_tell = 2 ";
        }
        else if($store_type == 8)
        {
            $con = " and store_tell = 2 ";
        }
        else if($store_type == 9 || $store_type == 10)
        {
            $con = " and mch_son_tell = 2 ";
        }
        else if($store_type == 12)
        {
            $con = " and supplier_tell = 2 ";
        }

        $sql_system = "select * from lkt_system_tell where store_id = 0 and type = 1 and startdate <= '$time_System' and enddate > '$time_System' $con ";
        $r_system = Db::query($sql_system);
        if($r_system)
        {
            $info = '系统维护中';
            $tell_id = $r_system[0]['id'];
            $systemMsgTitle = $r_system[0]['title'];
            $systemMsg = $r_system[0]['content'];
            $systemMsgStartDate = $r_system[0]['startdate'];
            $systemMsgEndDate = $r_system[0]['enddate'];
            $systemMsgType = $r_system[0]['type'];
        }
        else
        {
            $sql_system = "select * from lkt_system_tell where store_id = 0 and startdate <= '$time_System' and enddate > '$time_System' $con and id not in (select tell_id from lkt_system_tell_user where store_id = '$store_id' and store_type = '$store_type' and read_id = '$read_id') order by type asc";
            $r_system = Db::query($sql_system);
            if($r_system)
            {
                $tell_id = $r_system[0]['id'];
                $systemMsgTitle = $r_system[0]['title'];
                $systemMsg = $r_system[0]['content'];
                $systemMsgStartDate = $r_system[0]['startdate'];
                $systemMsgEndDate = $r_system[0]['enddate'];
                $systemMsgType = $r_system[0]['type'];
            }
        }
        
        $data = array('info' => $info,'tell_id'=>$tell_id,'systemMsgTitle'=>$systemMsgTitle,'systemMsg'=>$systemMsg,'systemMsgStartDate'=>$systemMsgStartDate,'systemMsgEndDate'=>$systemMsgEndDate,'systemMsgType'=>$systemMsgType);
        return $data;
    }

    // 获取维护公告
    public static function Obtain_maintenance_announcements($store_type)
    {
        $con = '';
        if($store_type == 1 || $store_type == 2 || $store_type == 3 || $store_type == 4 || $store_type == 5 || $store_type == 6 || $store_type == 11 )
        {
            $con = " and user_tell = 2 ";
        }
        else if($store_type == 7)
        {
            $con = " and mch_tell = 2 ";
        }
        else if($store_type == 8)
        {
            $con = " and store_tell = 2 ";
        }
        else if($store_type == 9 || $store_type == 10)
        {
            $con = " and mch_son_tell = 2 ";
        }
        else if($store_type == 12)
        {
            $con = " and supplier_tell = 2 ";
        }
        
        $info = '';
        $systemMsg = '';
        $systemMsgStartDate = '';
        $systemMsgEndDate = '';
        $systemMsgType = 0;
        $systemMsgTitle = '';
        $time_System = date('Y-m-d H:i:s'); // 当前时间

        $sql_system = "select * from lkt_system_tell where store_id = 0 and type = 1 and startdate <= '$time_System' and enddate > '$time_System' $con ";
        $r_system = Db::query($sql_system);
        if($r_system)
        {
            $info = '系统维护中';
            $tell_id = $r_system[0]['id'];
            $systemMsgTitle = $r_system[0]['title'];
            $systemMsg = $r_system[0]['content'];
            $systemMsgStartDate = $r_system[0]['startdate'];
            $systemMsgEndDate = $r_system[0]['enddate'];
            $systemMsgType = $r_system[0]['type'];
        }

        $data = array('info' => $info,'tell_id'=>$tell_id,'systemMsgTitle'=>$systemMsgTitle,'systemMsg'=>$systemMsg,'systemMsgStartDate'=>$systemMsgStartDate,'systemMsgEndDate'=>$systemMsgEndDate,'systemMsgType'=>$systemMsgType);
        return $data;
    }

    // 公告已读
    public static function markToRead($array)
    {
        $store_id = $array['store_id'];
        $store_type = $array['store_type'];
        $read_id = $array['read_id'];
        $tell_id = $array['tell_id'];
        $time = date("Y-m-d H:i:s");

        $is_supplier = 0;
        if(isset($array['is_supplier']))
        {
            $is_supplier = $array['is_supplier'];
        }

        $sql0 = "select id from lkt_system_tell where store_id = 0 and id = '$tell_id'";
        $r0 = Db::query($sql0);
        if ($r0)
        {
            $sql1 = "insert lkt_system_tell_user(store_id,tell_id,read_id,store_type,is_supplier,add_time) value ('$store_id','$tell_id','$read_id','$store_type','$is_supplier','$time') ";
            $r1 = Db::execute($sql1);
        }
        
        return;
    }

    // 修改库存
    public static function Modify_inventory($array)
    {
        $lktlog = new LaiKeLogUtils();
        $store_id = $array['store_id'];
        $supplier_id = $array['supplier_id']; // 供应商ID
        $pid = $array['pid']; // 商品ID
        $sid = $array['sid']; // 属性ID
        $num = $array['num']; // 数量
        $type = $array['type']; // 类型 1.下单 2.售后 3.取消订单
        
        $time = date("Y-m-d H:i:s");
        $code = 0;
        // 根据商品ID ，查询供应商商品ID
        $sql0 = "select supplier_superior,mch_id,commodity_str,is_zixuan from lkt_product_list where id = '$pid' ";
        $r0 = Db::query($sql0);
        $supplier_superior_p = $r0[0]['supplier_superior']; // 供应商商品ID
        $mch_id = $r0[0]['mch_id']; // 店铺ID

        // 根据属性ID，查询属性
        $sql1 = "select num,min_inventory,supplier_superior,attribute_str from lkt_configure where id = '$sid' ";
        $r1 = Db::query($sql1);
        $total_num = $r1[0]['num'];
        $min_inventory = $r1[0]['min_inventory']; // 预警值
        $supplier_superior_c = $r1[0]['supplier_superior']; // 供应商商品属性ID

        if($type == 1)
        { // 下单，库存减少
            $user_id = $array['user_id'];

            $total_num1 = $total_num - $num; // 剩余库存
            $sql_c = array('num'=>Db::raw('num-'.$num));
            $sql_p = array('num'=>Db::raw('num-'.$num));

            $content = $user_id . '生成订单所需' . $num;
            $sql_stock0 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$sid,'total_num'=>$total_num1,'flowing_num'=>$num,'type'=>1,'user_id'=>$user_id,'add_date'=>$time,'content'=>$content);
            $sql_stock_s = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$sid,'total_num'=>$total_num1,'flowing_num'=>$num,'type'=>1,'user_id'=>$user_id,'add_date'=>$time,'content'=>$content);

            if($total_num1 <= $min_inventory)
            {   
                $content1 = '预警';
                $sql_stock_insert1 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$sid,'total_num'=>$total_num1,'flowing_num'=>$num,'type'=>2,'add_date'=>$time,'content'=>$content1);
                Db::name('stock')->insert($sql_stock_insert1);

                $message_9 = "商品ID为".$pid."的商品库存不足，请尽快补充库存";
                $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>$mch_id,'gongyingshang'=>0,'type'=>9,'parameter'=>$sid,'content'=>$message_9);
                PC_Tools::add_message_logging($message_logging_list9);
            }

            if($r0[0]['commodity_str'] != '' && $r0[0]['is_zixuan'] == 1)
            { // 不是自选 并且 有衍射数据 
                $commodity_str1 = unserialize($r0[0]['commodity_str']);
                foreach($commodity_str1 as $k => $v)
                {
                    $data_where_1 = array('id'=>$v,'commodity_type'=>2,'recycle'=>0);
                    $data_update_1 = array('num'=>Db::raw('num-'.$num));
                    $r_1 = Db::name('product_list')->where($data_where_1)->update($data_update_1);
                }

                if($r1[0]['attribute_str'] != '')
                {
                    $attribute_str1 = unserialize($r1[0]['attribute_str']);
                    foreach($attribute_str1 as $k_1 => $v_1)
                    {
                        $data_where_2 = array('id'=>$v_1,'recycle'=>0);
                        $data_update_2 = array('num'=>Db::raw('num-'.$num));
                        $r_2 = Db::name('configure')->where($data_where_2)->update($data_update_2);
                    }
                }
            }
        }
        else if($type == 2)
        { // 售后，库存增加，销量减少
            $total_num1 = $total_num + $num; // 剩余库存

            $sql_c = array('num'=>Db::raw('num+'.$num));
            $sql_p = array('num'=>Db::raw('num+'.$num),'real_volume'=>Db::raw('real_volume-'.$num));
            $content =  '退款成功，增加商品总库存' . $num;
            $sql_stock0 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$sid,'total_num'=>$total_num1,'flowing_num'=>$num,'type'=>0,'add_date'=>$time,'content'=>$content);
            $sql_stock_s = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$sid,'total_num'=>$total_num1,'flowing_num'=>$num,'type'=>0,'add_date'=>$time,'content'=>$content);

            if($r0[0]['commodity_str'] != '' && $r0[0]['is_zixuan'] == 1)
            { // 不是自选 并且 有衍射数据 
                $commodity_str1 = unserialize($r0[0]['commodity_str']);
                foreach($commodity_str1 as $k => $v)
                {
                    $data_where_1 = array('id'=>$v,'commodity_type'=>2,'recycle'=>0);
                    $data_update_1 = array('num'=>Db::raw('num+'.$num));
                    $r_1 = Db::name('product_list')->where($data_where_1)->update($data_update_1);
                }

                if($r1[0]['attribute_str'] != '')
                {
                    $attribute_str1 = unserialize($r1[0]['attribute_str']);
                    foreach($attribute_str1 as $k_1 => $v_1)
                    {
                        $data_where_2 = array('id'=>$v_1,'recycle'=>0);
                        $data_update_2 = array('num'=>Db::raw('num+'.$num));
                        $r_2 = Db::name('configure')->where($data_where_2)->update($data_update_2);
                    }
                }
            }
        }
        else
        { // 取消订单
            $total_num1 = $total_num + $num; // 剩余库存
            $sql_c = array('num'=>Db::raw('num+'.$num));
            $sql_p = array('num'=>Db::raw('num+'.$num));
            $content =  '取消订单，返还' . $num;
            $sql_stock0 = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$sid,'total_num'=>$total_num1,'flowing_num'=>$num,'type'=>0,'add_date'=>$time,'content'=>$content);
            $sql_stock_s = array('store_id'=>$store_id,'product_id'=>$pid,'attribute_id'=>$sid,'total_num'=>$total_num1,'flowing_num'=>$num,'type'=>0,'add_date'=>$time,'content'=>$content);

            if($r0[0]['commodity_str'] != '' && $r0[0]['is_zixuan'] == 1)
            { // 不是自选 并且 有衍射数据 
                $commodity_str1 = unserialize($r0[0]['commodity_str']);
                foreach($commodity_str1 as $k => $v)
                {
                    $data_where_1 = array('id'=>$v,'commodity_type'=>2,'recycle'=>0);
                    $data_update_1 = array('num'=>Db::raw('num+'.$num));
                    $r_1 = Db::name('product_list')->where($data_where_1)->update($data_update_1);
                }

                if($r1[0]['attribute_str'] != '')
                {
                    $attribute_str1 = unserialize($r1[0]['attribute_str']);
                    foreach($attribute_str1 as $k_1 => $v_1)
                    {
                        $data_where_2 = array('id'=>$v_1,'recycle'=>0);
                        $data_update_2 = array('num'=>Db::raw('num+'.$num));
                        $r_2 = Db::name('configure')->where($data_where_2)->update($data_update_2);
                    }
                }
            }
        }

        if($supplier_id != 0)
        { // 供应商商品
            $sql_c_where = array('supplier_superior'=>$supplier_superior_c);
            $sql_p_where = array('store_id'=>$store_id,'supplier_superior'=>$supplier_superior_p);

            $sql_c_where0 = array('id'=>$supplier_superior_c);
            $r_p0 = Db::name('configure')->where($sql_c_where0)->update($sql_c);
            if ($r_p0 < 1)
            {
                $code = 1;
                $lktlog->log("common/stock.log",__METHOD__ . ":" . __LINE__ . "修改商品数量失败！参数：" . json_encode($sql_c_where0));
            }

            // 根据商品id,修改卖出去的销量
            $sql_p_where0 = array('store_id'=>$store_id,'id'=>$supplier_superior_p);
            $r_x0 = Db::name('product_list')->where($sql_p_where0)->update($sql_p);
            if ($r_x0 < 1)
            {
                $code = 1;
                $lktlog->log("common/stock.log",__METHOD__ . ":" . __LINE__ . "修改商品销量失败！参数：" . json_encode($sql_p_where0));
            }

            $r_stock_s = Db::name('stock')->insert($sql_stock_s);

            if($total_num1 < $min_inventory)
            {
                $message_9 = "商品ID为".$supplier_superior_p."的商品库存不足，请尽快补充库存";
                $message_logging_list9 = array('store_id'=>$store_id,'mch_id'=>0,'gongyingshang'=>$supplier_id,'type'=>9,'parameter'=>$supplier_superior_c,'content'=>$message_9);
                PC_Tools::add_message_logging($message_logging_list9);
            }
        }
        else
        {
            $sql_c_where = array('id'=>$sid);
            $sql_p_where = array('store_id'=>$store_id,'id'=>$pid);
        }

        $r2 = Db::name('configure')->where($sql_c_where)->update($sql_c);
        if ($r2 < 1)
        {
            $code = 1;
            $lktlog->log("common/stock.log",__METHOD__ . ":" . __LINE__ . "修改商品数量失败！参数：" . json_encode($sql_c_where));
        }

        // 根据商品id,修改卖出去的销量
        $r3 = Db::name('product_list')->where($sql_p_where)->update($sql_p);
        if ($r3 < 1)
        {
            $code = 1;
            $lktlog->log("common/stock.log",__METHOD__ . ":" . __LINE__ . "修改商品销量失败！参数：" . json_encode($sql_p_where));
        }

        $r_stock0 = Db::name('stock')->insert($sql_stock0);
        if($r_stock0 < 1)
        {
            $code = 1;
        }
        
        return $code;
    }

    // 获取分账状态
    public static function Obtain_the_splitting_status($array)
    {
        $store_id = $array['store_id'];
        
        $profit_sharing = 'N';
        $sql = "select id,is_accounts from lkt_config where store_id = '$store_id' ";
        $r = Db::query($sql);
        if($r)
        {
            if($r[0]['is_accounts'] == 1)
            {
                $profit_sharing = 'Y';
            }
        }
        
        return $profit_sharing;
    }

    // 获取分账数据
    public static function Obtain_ledger_data($array)
    {
        $store_id = $array['store_id'];
        $mch_id = $array['mch_id'];
        $money = $array['money'] * 100;

        $is_accounts = 0; // 是否分账
        $accounts_set = ''; // 分账账号
        $receivers = array(); // 分账接收方列表
        $With_proportion = array(); // 店铺分账记录信息
        $Remaining_amount = $money; // 订单金额

        $list = array();

        $Remaining_proportion = 100; // 平台比例
        $sql0 = "select is_accounts,accounts_set from lkt_config where store_id = '$store_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $is_accounts = $r0[0]['is_accounts']; // 是否分账
            $accounts_set = $r0[0]['accounts_set']; // 分账账号
        }

        // 根据店铺ID，查询店铺分账账户信息
        $sql1 = "select id,mch_id,d_type,account,relationship,proportion,add_date from lkt_mch_distribution where mch_id = '$mch_id' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $type = strtoupper($v['d_type']); // 分账接收方类型
                $account = $v['account']; // 分账接收方账号
                $proportion = $v['proportion']; // 分账比例
                $Remaining_proportion = $Remaining_proportion - $proportion; // 平台比例 - 分账比例

                $amount = $money * $proportion * 0.01; // 该用户分账金额
                $Remaining_amount1 = $Remaining_amount - $amount; // 订单金额 - 该用户分账金额 = 该笔剩余金额

                $description = '订单结算，获得' . $amount;

                $receivers[] = array('type'=>$type,'account'=>$account,'amount'=>$amount,'description'=>$description); // 分账接收方列表
                $With_proportion[] = array('account'=>$account,'amount'=>$amount,'description'=>$description,'proportion'=>$proportion); // 店铺分账记录信息
            }

            $amount = $money * $Remaining_proportion * 0.01; // 该用户分账金额
            $description = '订单结算，获得' . $amount;
            $With_proportion[] = array('account'=>$accounts_set,'amount'=>$amount,'description'=>$description,'proportion'=>$Remaining_proportion); // 店铺分账记录信息
        }

        $data = array('is_accounts'=>$is_accounts,'accounts_set'=>$accounts_set,'receivers'=>$receivers,'With_proportion'=>$With_proportion);
        return $data;
    }

    // 获取图形验证码
    public static function getCode()
    {   
        $image = imagecreatetruecolor(100, 30);
        //背景颜色为白色
        $color=imagecolorallocate($image, 255, 255, 255);
        imagefill($image, 20, 20, $color);

        $code = '';
        $font = MO_PUBLIC_DIR. '/static/arialbd.ttf';
        for($i=0;$i<4;$i++)
        {
            $fontSize=20;
            $x=30+$i*60/4;//rand(5,10)+$i*100/4;;
            $y=24;//rand(5, 15);
            $data='qwertyuiopasdfghjklzxcvbnm123456789';
            $string=substr($data,rand(0, strlen($data)-1),1);
            $code .= $string;
        }
        $color=imagecolorallocate($image,0, 64, 240);
        imagettftext($image, $fontSize, 1, 30, 26, $color, $font, $code);

        $codeToken = MD5(time());
        cache($codeToken,$code,3600);
        
        $uploadImg = dirname(MO_IMAGE_DIR) . '/code_img/' ; // 图片上传位置
        if (is_dir($uploadImg) == '')
        { // 如果文件不存在
            mkdir($uploadImg); // 创建文件
        }

        $uploadImg = dirname(MO_IMAGE_DIR) . '/code_img/'.date('Y-m-d') . '/' ; // 图片上传位置
        if (is_dir($uploadImg) == '')
        { // 如果文件不存在
            mkdir($uploadImg); // 创建文件
        }
        $imgURL_name = time() . mt_rand(1, 1000) . '.jpg';
        $imgURL_name1 = time() . mt_rand(1, 1000) . '.jpg';
        $uploadImg1 = 'code_img/'.date('Y-m-d') . '/' . $imgURL_name1;
        imagepng($image, $uploadImg1);
        imagedestroy($image);

        $path = 'code_img/' . date('Y-m-d') . '/' . $imgURL_name;
        $contentType = '.jpg';
        $imgURL = "./" . $uploadImg1;

        $upserver = 2;
        $common = LKTConfigInfo::getOSSConfig();
        try
        {
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
        
        cache($codeToken.'img',$url,3600);
        $data = array('code_img' => $url,'code'=>$codeToken);
        return $data;
    }

    // 获取图形验证码
    public static function get_source($source)
    {   
        $sourceName = '';
        switch ($source) 
        {
            case '1':
                $sourceName = '小程序';
                break;
            case '2':
                $sourceName = 'H5移动端';
                break;
            case '3':
                $sourceName = '支付宝小程序';
                break;
            case '4':
                $sourceName = '字节跳动小程序';
                break;
            case '5':
                $sourceName = '百度小程序';
                break;
            case '6':
                // $sourceName = 'pc商城';
                $sourceName = 'pc端';
                break;
            case '7':
                // $sourceName = 'pc店铺';
                $sourceName = 'pc端';
                break;
            case '8':
                // $sourceName = 'pc管理后台';
                $sourceName = 'pc端';
                break;
            case '9':
                // $sourceName = 'PC门店核销';
                $sourceName = 'pc端';
                break;
            case '10':
                $sourceName = 'H5门店核销';
                break;
            case '11':
                $sourceName = 'APP端';
                break;
            case '12':
                $sourceName = '供应商';
                break;
            default:
                $sourceName = '其它';
                break;
        }			

        return $sourceName;
    }

    // 获取商品分类和品牌
    public static function get_pro($store_id)
    {
        $product_class_arr = array();
        //分类下拉选择
        $r_class = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>0])->order('sort','desc')->field('cid,pname')->select()->toArray();
        if($r_class)
        {
            foreach ($r_class as $key => $value)
            {
                $c = '-' . $value['cid'] . '-';
                $product_class_arr[$c] = $value['pname'];
                //循环第一层
                $r_e = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$value['cid']])->order('sort','desc')->field('cid,pname')->select()->toArray();
                if ($r_e)
                {
                    foreach ($r_e as $ke => $ve)
                    {
                        $cone = $c . $ve['cid'] . '-';
                        $product_class_arr[$cone] = $ve['pname'];

                        //循环第二层
                        $r_t = ProductClassModel::where(['store_id'=>$store_id,'recycle'=>0,'sid'=>$ve['cid']])->order('sort','desc')->field('cid,pname')->select()->toArray();
                        if ($r_t)
                        {
                            foreach ($r_t as $k => $v)
                            {
                                $ctow = $cone . $v['cid'] . '-';
                                $product_class_arr[$ctow] = $v['pname'];
                            }
                        }
                    }
                }
            }
        }
        
        $brand_class_arr = array();
        //品牌下拉选择
        $r_brand_class = BrandClassModel::where(['store_id'=>$store_id,'recycle'=>0])->select()->toArray();
        if($r_brand_class)
        {
            foreach ($r_brand_class as $key => $value)
            {
                $key0 = $value['brand_id'];
                $brand_class_arr[$key0] = $value['brand_name'];
            }
        }

        $date = array('product_class_arr'=>$product_class_arr,'brand_class_arr'=>$brand_class_arr);
        return $date;
    }

    // 获取订单按钮（申请售后和评论除外）
    public static function get_order_button($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $userid = $array['userid']; // user_id
        $sNo = $array['sNo']; // 订单号
        $pay = $array['pay']; // 支付方式
        $otype = $array['otype']; // 订单类型
        $status = $array['status']; // 状态 0:未付款 1:未发货 2:待收货 5:已完成 7:订单关闭 8:待核销
        $self_lifting = $array['self_lifting']; // 自提 0.配送 1.自提 2.商家自配 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
        $delivery_status = $array['delivery_status']; // 提醒发货
        $arrive_time = $array['arrive_time']; // 到货时间
        $settlement_status = $array['settlement_status']; // 结算状态
        $is_invoice = $array['is_invoice']; // 店铺是否支持开票 0.否 1.是
        $review_status = $array['review_status']; // 凭证审核状态 0.未上传凭证 1.待审核 2.通过 3.拒绝

        $time = date("Y-m-d H:i:s");

        $button_list = array(
            'qxdd'=>0, // 取消订单按钮 0:没有 1:有
            'ljfk'=>0, // 立即付款按钮 0:没有 1:有
            'txfh'=>0, // 提醒发货按钮 0:没有 1:有
            'jstk'=>0, // 极速退款按钮 0:没有 1:有
            'ckwl'=>0, // 查看物流按钮 0:没有 1:有
            'qrsh'=>0, // 确认收货按钮 0:没有 1:有
            'sqsh'=>0, // 申请退款按钮 0:没有 1:有
            'ljpj'=>0, // 立即评价按钮 0:没有 1:有
            'zjpj'=>0, // 追加评价按钮 0:没有 1:有
            'scdd'=>0, // 删除订单按钮 0:没有 1:有
            'zcgm'=>0, // 再次购买按钮 0:没有 1:有
            'sqkp'=>0, // 申请开票按钮 0:没有 1:有
            'tqm'=>0, // 提取码按钮 0:没有 1:有
            'cktqm'=>0, // 查看提取码按钮 0:没有 1:有
            'hxm'=>0, // 核销码按钮 0:没有 1:有
            'cksh'=>0, // 核销码按钮 0:没有 1:有
            'scpz'=>0, // 上传凭证按钮 0:没有 1:有
            'pzshz'=>0 // 凭证审核中按钮 0:没有 1:有
        );

        if($otype == 'VI')
        { // 虚拟
            if($self_lifting == 3)
            { // 虚拟订单需要线下核销
                if($status == 0)
                { // 待付款
                    $button_list['qxdd'] = 1; // 取消订单按钮
                    if($pay == 'offline_payment')
                    {
                        if($review_status == 1)
                        {
                            $button_list['pzshz'] = 1; // 凭证审核中按钮
                        }
                        else
                        {
                            $button_list['scpz'] = 1; // 上传凭证按钮
                        }
                    }
                    else
                    {
                        $button_list['ljfk'] = 1; // 立即付款按钮
                    }
                }
                else if($status == 8)
                { // 待核销
                    $button_list['hxm'] = 1; // 核销码按钮
                }
                else if($status == 5)
                { // 已完成
                    $button_list['hxm'] = 1; // 核销码按钮
                }
                else if($status == 7)
                {
                    $button_list['scdd'] = 1; // 删除订单按钮
                }
            }
            else
            { // 虚拟订单无需线下核销
                if($status == 0)
                { // 待付款
                    $button_list['qxdd'] = 1; // 取消订单按钮
                    if($pay == 'offline_payment')
                    {
                        if($review_status == 1)
                        {
                            $button_list['pzshz'] = 1; // 凭证审核中按钮
                        }
                        else
                        {
                            $button_list['scpz'] = 1; // 上传凭证按钮
                        }
                    }
                    else
                    {
                        $button_list['ljfk'] = 1; // 立即付款按钮
                    }
                }
                else if($status == 7)
                {
                    $button_list['scdd'] = 1; // 删除订单按钮
                }
            }
        }
        else if($otype == 'PS')
        { // 预售
            $sql0 = "select product_id,is_deposit,is_balance from lkt_pre_sell_record where store_id = '$store_id' and user_id = '$userid' and sNo = '$sNo' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $product_id = $r0[0]['product_id']; // 商品ID
                $is_deposit = $r0[0]['is_deposit']; // 是否支付订金  0.否  1.是
                $is_balance = $r0[0]['is_balance']; // 是否支付尾款  0.否  1.是

                $sql1 = "select sell_type,balance_pay_time from lkt_pre_sell_goods where product_id = '$product_id' ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    if($r1[0]['sell_type'] == 1)
                    { // 订金模式
                        if($status == 0)
                        { // 待付款
                            if($is_deposit == 1)
                            { // 已支付定金
                                $startTime = $r1[0]['balance_pay_time'];
                                $endTime = date("Y-m-d 23:59:59",strtotime($r1[0]['balance_pay_time']));
                                if($startTime <= $time && $time <= $endTime )
                                { // 到支付尾款时间
                                    $button_list['ljfk'] = 1; // 立即付款按钮
                                }
                            }
                            else
                            { // 还未支付定金
                                $button_list['qxdd'] = 1; // 取消订单按钮
                                $button_list['ljfk'] = 1; // 立即付款按钮
                            }
                        }
                        else if($status == 1)
                        { // 未发货
                            if($delivery_status == 0)
                            {
                                $button_list['txfh'] = 1; // 提醒发货按钮
                            }
                            $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                            $r_order_details = Db::query($sql_order_details);
                            if($r_order_details)
                            {
                                $button_list['ckwl'] = 1; // 查看物流按钮
                            } 
                        }
                        else if($status == 2)
                        { // 待收货
                            $button_list['qrsh'] = 1; // 确认收货按钮
            
                            $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                            $r_order_details = Db::query($sql_order_details);
                            if($r_order_details)
                            {
                                $button_list['ckwl'] = 1; // 查看物流按钮
                            }
                        }
                        else if($status == 5)
                        { // 已完成
                            $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                            $r_order_details = Db::query($sql_order_details);
                            if($r_order_details)
                            {
                                $button_list['ckwl'] = 1; // 查看物流按钮
                            }
                        }
                    }
                    else
                    { // 订货模式
                        if($status == 0)
                        { // 待付款
                            $button_list['qxdd'] = 1; // 取消订单按钮
                            $button_list['ljfk'] = 1; // 立即付款按钮
                        }
                        else if($status == 1)
                        { // 未发货
                            if($delivery_status == 0)
                            {
                                $button_list['txfh'] = 1; // 提醒发货按钮
                            }
                            $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                            $r_order_details = Db::query($sql_order_details);
                            if($r_order_details)
                            {
                                $button_list['ckwl'] = 1; // 查看物流按钮
                            } 
                        }
                        else if($status == 2)
                        { // 待收货
                            $button_list['qrsh'] = 1; // 确认收货按钮
            
                            $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                            $r_order_details = Db::query($sql_order_details);
                            if($r_order_details)
                            {
                                $button_list['ckwl'] = 1; // 查看物流按钮
                            }
                        }
                        else if($status == 5)
                        { // 已完成
                            $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                            $r_order_details = Db::query($sql_order_details);
                            if($r_order_details)
                            {
                                $button_list['ckwl'] = 1; // 查看物流按钮
                            }
                        }
                        else if($status == 7)
                        {
                            $button_list['scdd'] = 1; // 删除订单按钮
                        }
                    }
                }
            }
        }
        else if($otype == 'GM' && $self_lifting == 1)
        { // 自提
            if($status == 0)
            { // 待付款
                $button_list['qxdd'] = 1; // 取消订单按钮
                if($pay == 'offline_payment')
                {
                    if($review_status == 1)
                    {
                        $button_list['pzshz'] = 1; // 凭证审核中按钮
                    }
                    else
                    {
                        $button_list['scpz'] = 1; // 上传凭证按钮
                    }
                }
                else
                {
                    $button_list['ljfk'] = 1; // 立即付款按钮
                }
            }
            else if($status == 2)
            { // 待收货
                $button_list['tqm'] = 1; // 提取码按钮
            }
            else if($status == 5)
            { // 已完成
                $button_list['cktqm'] = 1; // 查看提取码按钮
            }
            else if($status == 7)
            { // 订单关闭
                if($otype == 'GM')
                {
                    $button_list['zcgm'] = 1; // 再次购买按钮
                }
                if ($otype != 'PT' && $otype != 'KT')
                {
                    $button_list['scdd'] = 1; // 删除订单按钮
                }
            }
        }
        else
        {
            if($status == 0)
            { // 待付款
                $button_list['qxdd'] = 1; // 取消订单按钮
                if($pay == 'offline_payment')
                {
                    if($review_status == 1)
                    {
                        $button_list['pzshz'] = 1; // 凭证审核中按钮
                    }
                    else
                    {
                        $button_list['scpz'] = 1; // 上传凭证按钮
                    }
                }
                else
                {
                    $button_list['ljfk'] = 1; // 立即付款按钮
                }
            }
            else if($status == 1)
            { // 未发货
                if($delivery_status == 0)
                {
                    $button_list['txfh'] = 1; // 提醒发货按钮
                }

                $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                $r_order_details = Db::query($sql_order_details);
                if($r_order_details)
                {
                    $button_list['ckwl'] = 1; // 查看物流按钮
                } 
            }
            else if($status == 2)
            { // 待收货
                $button_list['qrsh'] = 1; // 确认收货按钮

                $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                $r_order_details = Db::query($sql_order_details);
                if($r_order_details)
                {
                    $button_list['ckwl'] = 1; // 查看物流按钮
                } 
            }
            else if($status == 5)
            { // 已完成
                if($otype == 'GM')
                {
                    $button_list['zcgm'] = 1; // 再次购买按钮
                }
                $sql_order_details = "select id from lkt_order_details where store_id = '$store_id' and r_sNo = '$sNo' and deliver_num != 0 ";
                $r_order_details = Db::query($sql_order_details);
                if($r_order_details)
                {
                    $button_list['ckwl'] = 1; // 查看物流按钮
                } 

                if($arrive_time != '' && $settlement_status == 1 && $status == 5 && $is_invoice == 1)
                { // 收货时间不为空 并且 已结算 并且 订单状态为已完成 并且 店铺支持开票
                    $button_list['sqkp'] = 1; // 申请开票按钮
                }
            }
            else if($status == 7)
            { // 订单关闭
                $button_list['ckwl'] = 0; // 查看物流按钮
                if($otype == 'GM')
                {
                    $button_list['zcgm'] = 1; // 再次购买按钮
                }
                if ($otype != 'PT' && $otype != 'KT')
                {
                    $button_list['scdd'] = 1; // 删除订单按钮
                }
            }
        }

        return $button_list;
    }

    // 获取商品按钮
    public static function get_product_button($array)
    {
        $store_id = $array['store_id']; // 商城ID
        $user_id = $array['user_id']; // user_id
        $id = $array['id']; // 订单详情ID
        $sNo = $array['sNo']; // 订单号
        $otype = $array['otype']; // 订单类型
        $pay = $array['pay']; // 订单类型
        $p_id = $array['p_id']; // 商品ID
        $sid = $array['sid']; // 属性ID
        $self_lifting = $array['self_lifting']; // 自提 0.配送 1.自提 2.商家自配 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
        $r_status = $array['r_status']; // 状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭 8:待核销
        $settlement_type = $array['settlement_type']; // 订单结算标识 0未结算 1已结算

        $time = date("Y-m-d H:i:s");

        $button_list = array(
            'jstk'=>0, // 极速退款按钮 0:没有 1:有
            'sqsh'=>0, // 申请退款按钮 0:没有 1:有
            'cksh'=>0, // 查看售后按钮 0:没有 1:有
            'ljpj'=>0, // 立即评价按钮 0:没有 1:有
            'zjpj'=>0, // 追加评价按钮 0:没有 1:有
        );

        if($otype == 'VI')
        { // 虚拟商品订单
            $sql_p = "select write_off_settings from lkt_product_list where id = '$p_id' ";
            $r_p = Db::query($sql_p);
            if($r_p)
            {
                $write_off_settings = $r_p[0]['write_off_settings'];
            }

            if($r_status != 0)
            {
                if($write_off_settings == 1)
                { // 线下核销
                    if($r_status == 2 || $r_status == 8)
                    { // 待收货
                        $button_list['sqsh'] = 1; // 申请退款按钮显示
                    }
                }
                else
                { // 无需核销
                    $button_list['sqsh'] = 1; // 申请退款按钮显示
                }
            }
        }
        else if($otype == 'JP' )
        { // 竞拍 或 积分兑换 没有售后
            $button_list['sqsh'] = 0; // 申请退款按钮隐藏
        }
        else if($otype == 'MS' || $otype == 'FX')
        { // 秒杀 或 分销
            if($r_status == 2)
            { // 待收货
                $button_list['sqsh'] = 1; // 申请退款按钮显示
            }
            else if($r_status == 5)
            { // 已完成
                if($settlement_type == 0)
                { // 未结算
                    $button_list['sqsh'] = 1; // 申请退款按钮显示
                }
            }
        }
        else if ($otype == 'PT' || $otype == 'KT')
        { // 拼团 或 开团
            if($r_status == 1)
            { // 已付款
                $button_list['sqsh'] = 1; // 申请退款按钮显示
            }
            else if($r_status == 2)
            { // 待收货
                $button_list['sqsh'] = 1; // 申请退款按钮显示
            }
            else if($r_status == 5)
            { // 已完成
                if($settlement_type == 0)
                { // 未结算
                    $button_list['sqsh'] = 1; // 申请退款按钮
                }
            }
        }
        else if($otype == 'FS' )
        { // 限时折扣
            if($r_status == 1 || $r_status == 2 )
            {
                $button_list['sqsh'] = 1; // 申请退款按钮
            }
            else if($r_status == 5)
            { // 已完成
                if($settlement_type == 0)
                { // 未结算
                    $button_list['sqsh'] = 1; // 申请退款按钮
                }
            }
        }
        else if($otype == 'PS' )
        { // 预售
            if($r_status == 1 || $r_status == 2 )
            {
                $button_list['sqsh'] = 1; // 申请退款按钮
            }
            else if($r_status == 5)
            { // 已完成
                if($settlement_type == 0)
                { // 未结算
                    $button_list['sqsh'] = 1; // 申请退款按钮
                }
            }
        }
        else if($otype == 'IN' )
        {
            if($r_status == 1)
            {
                $button_list['jstk'] = 1; // 极速退款按钮显示
            }
            else if($r_status == 2)
            { // 待收货
                $button_list['sqsh'] = 1; // 申请退款按钮
            }
            else if($r_status == 5)
            { // 已完成
                if($settlement_type == 0)
                { // 未结算
                    $button_list['sqsh'] = 1; // 申请退款按钮
                }
            }
        }
        else
        {
            if($self_lifting == 0)
            { // 物流订单
                if($r_status == 1)
                { // 待发货
                    $button_list['jstk'] = 1; // 极速退款按钮
                }
                else if($r_status == 2)
                { // 待收货
                    $button_list['sqsh'] = 1; // 申请退款按钮
                }
                else if($r_status == 5)
                { // 已完成
                    if($settlement_type == 0)
                    { // 未结算
                        $button_list['sqsh'] = 1; // 申请退款按钮
                    }
                }
            }
            else if($self_lifting == 1)
            { // 自提订单
                if($r_status == 2)
                { // 待收货
                    $button_list['jstk'] = 1; // 极速退款按钮
                }
            }
            else if($self_lifting == 2)
            { // 商家配送
                if($r_status == 1)
                { // 待发货
                    $button_list['jstk'] = 1; // 极速退款按钮
                }
                else if($r_status == 2)
                { // 待收货
                    $button_list['sqsh'] = 1; // 申请退款按钮
                }
                else if($r_status == 5)
                { // 已完成
                    if($settlement_type == 0)
                    { // 未结算
                        $button_list['sqsh'] = 1; // 申请退款按钮
                    }
                }
            }
        }

        if($r_status == 5)
        {
            // 判断订单评论状态
            $sql_c = "select * from lkt_comments where store_id = '$store_id' and uid = '$user_id' and oid = '$sNo' and pid = '$p_id' and attribute_id = '$sid' ";
            $res_c = Db::query($sql_c);
            if(!empty($res_c))
            { // 有评论数据
                $button_list['zjpj'] = 1; // 追加评价按钮
                if($res_c[0]['review'] != '')
                { // 追评内容不为空
                    $button_list['zjpj'] = 0; // 追加评价按钮
                }
            }
            else
            { // 没有评论数据
                $button_list['ljpj'] = 1; // 立即评价按钮
            }
        }
        // r_type 类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5:拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后) 11:同意并且寄回商品 12:售后结束 13:人工售後完成 15:极速退款
        // 根据商城ID、订单详情ID、售后未完成，查看售后信息
        $sql1 = "select * from lkt_return_order where store_id = '$store_id' and p_id = '$id' order by re_time desc limit 1 ";
        $r1 = Db::query($sql1);
        if($r1)
        { // 有售后
            if($r1[0]['r_type'] == 4 || $r1[0]['r_type'] == 9 || $r1[0]['r_type'] == 13 || $r1[0]['r_type'] == 15)
            { // 售后已完成
                $button_list['sqsh'] = 0; // 申请退款按钮
                $button_list['cksh'] = 1; // 查看售后按钮
            }
            else if($r1[0]['r_type'] == 2 || $r1[0]['r_type'] == 8 || $r1[0]['r_type'] == 10 || $r1[0]['r_type'] == 12)
            { // 已拒绝
                if($settlement_type == 0)
                { // 未结算
                    $button_list['sqsh'] = 1; // 申请退款按钮
                }
                else
                { // 已结算
                    $button_list['sqsh'] = 0; // 申请退款按钮
                    $button_list['cksh'] = 1; // 查看售后按钮
                }
            }
            else
            { // 售后未完成
                $button_list['sqsh'] = 0; // 申请退款按钮
                $button_list['cksh'] = 1; // 查看售后按钮
            }
        }

        if($pay == 'offline_payment')
        {
            $button_list['jstk'] = 0; // 极速退款按钮 0:没有 1:有
            $button_list['sqsh'] = 0; // 申请退款按钮 0:没有 1:有
            $button_list['cksh'] = 0; // 查看售后按钮 0:没有 1:有
        }

        return $button_list;
    }

    // 获取订单按钮（申请售后和评论）
    public static function get_order_button_assistant($array)
    {
        $otype = $array['otype']; // 订单类型
        $status = $array['status']; // 状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭 8:待核销
        $self_lifting = $array['self_lifting']; // 自提 0.配送 1.自提 2.商家自配 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销
        $order_details_num = $array['order_details_num']; // 订单详情数量
        $get_button_list = $array['get_button_list']; // 按钮数据
        $get_order_details_button = $array['get_order_details_button']; // 底部按钮数据
        $Number_of_cycles = $array['Number_of_cycles']; // 循环次数

        $time = date("Y-m-d H:i:s");

        if($status == 1)
        { // 待发货
            if($get_order_details_button['jstk'] == 1)
            {
                $get_button_list['jstk'] = 1; // 极速退款按钮 0:没有 1:有
                $get_order_details_button['jstk'] = 0; // 该商品极速退款按钮隐藏
            }

            if($otype == 'FS' || $otype == 'PT' || $otype == 'PS')
            {
                if($get_order_details_button['sqsh'] == 1)
                { // 该订单详情有申请退款按钮
                    $get_button_list['sqsh'] = 1; // 底部申请退款按钮 0:没有 1:有
                    $get_order_details_button['sqsh'] = 0; // 该商品申请退款按钮隐藏
                }
                // if($get_order_details_button['cksh'] == 1)
                // { // 该订单详情有查看售后按钮
                //     $get_button_list['cksh'] = 1; // 底部查看售后按钮 0:没有 1:有
                //     $get_order_details_button['cksh'] = 0; // 该商品查看售后按钮隐藏
                // }
            }
        }
        else if($status == 2)
        { // 待收货
            if($self_lifting == 1)
            {
                if($get_order_details_button['jstk'] == 1)
                {
                    $get_button_list['jstk'] = 1; // 极速退款按钮 0:没有 1:有
                    $get_order_details_button['jstk'] = 0; // 该商品极速退款按钮隐藏
                }
            }
            if($order_details_num == 1)
            { // 单商品订单
                if($get_order_details_button['sqsh'] == 1)
                { // 该订单详情有申请退款按钮
                    $get_button_list['sqsh'] = 1; // 底部申请退款按钮 0:没有 1:有
                    $get_order_details_button['sqsh'] = 0; // 该商品申请退款按钮隐藏
                }
                if($get_order_details_button['cksh'] == 1)
                { // 该订单详情有查看售后按钮
                    $get_button_list['cksh'] = 1; // 底部查看售后按钮 0:没有 1:有
                    $get_order_details_button['cksh'] = 0; // 该商品查看售后按钮隐藏
                }
            }
            else
            { // 多商品订单
                if($Number_of_cycles == 0)
                { // 第一次
                    $get_button_list['sqsh'] = $get_order_details_button['sqsh']; // 底部申请退款按钮 与 该商品申请退款按钮一致
                    // $get_button_list['cksh'] = $get_order_details_button['cksh']; // 底部查看售后按钮 与 该商品查看售后按钮一致
                }
                else
                {
                    if($get_button_list['sqsh'] != $get_order_details_button['sqsh'])
                    { // 底部申请退款按钮 与 该商品申请退款按钮 不一致（该订单下，存在有的商品有申请售后按钮，有的没有申请售后按钮）
                        $get_button_list['sqsh'] = 1; // 底部申请退款按钮显示
                    }
                    // if($get_button_list['cksh'] != $get_order_details_button['cksh'])
                    // { // 底部查看售后按钮 与 该商品查看售后按钮 不一致
                    //     $get_button_list['cksh'] = 0; // 底部查看售后钮不显示（该订单下，存在有的商品有查看售后按钮，有的没有查看售后按钮）
                    // }
                }
            }
        }
        else if($status == 5)
        { // 已完成
            if($order_details_num == 1)
            { // 单商品订单
                if($get_order_details_button['sqsh'] == 1)
                { // 该订单详情有申请退款按钮
                    $get_button_list['sqsh'] = 1; // 底部申请退款按钮 0:没有 1:有
                    $get_order_details_button['sqsh'] = 0; // 订单详情申请退款按钮隐藏
                }
                if($get_order_details_button['cksh'] == 1)
                { // 该订单详情有查看售后按钮
                    $get_button_list['cksh'] = 1; // 底部查看售后按钮 0:没有 1:有
                    $get_order_details_button['cksh'] = 0; // 订单详情查看售后按钮隐藏
                }
                if($get_order_details_button['ljpj'] == 1)
                { // 该订单详情有立即评价按钮
                    $get_button_list['ljpj'] = 1; // 底部立即评价按钮 0:没有 1:有
                    $get_order_details_button['ljpj'] = 0; // 订单详情立即评价按钮隐藏
                }
                // if($get_order_details_button['zjpj'] == 1)
                // { // 该订单详情有追加评价按钮
                //     $get_button_list['zjpj'] = 1; // 底部追加评价按钮 0:没有 1:有
                //     $get_order_details_button['zjpj'] = 0; // 订单详情追加评价按钮隐藏
                // }
            }
            else
            { // 多商品订单
                if($Number_of_cycles == 0)
                { // 第一次
                    $get_button_list['sqsh'] = $get_order_details_button['sqsh']; // 底部申请退款按钮 与 该商品申请退款按钮一致
                    // $get_button_list['cksh'] = $get_order_details_button['cksh']; // 底部查看售后按钮 与 该商品查看售后按钮一致
                    $get_button_list['ljpj'] = $get_order_details_button['ljpj']; // 底部立即评价按钮 与 该商品立即评价按钮一致
                    // $get_button_list['zjpj'] = $get_order_details_button['zjpj']; // 底部追加评价按钮 与 该商品追加评价按钮一致
                }
                else
                {
                    if($get_button_list['sqsh'] != $get_order_details_button['sqsh'])
                    { // 底部申请退款按钮 与 该商品申请退款按钮 不一致（该订单下，存在有的商品有申请售后按钮，有的没有申请售后按钮）
                        $get_button_list['sqsh'] = 1; // 底部申请退款按钮显示
                    }
                    // if($get_button_list['cksh'] != $get_order_details_button['cksh'])
                    // { // 底部查看售后按钮 与 该商品查看售后按钮 不一致
                    //     $get_button_list['cksh'] = 0; // 底部查看售后钮不显示（该订单下，存在有的商品有查看售后按钮，有的没有查看售后按钮）
                    // }
                    if($get_button_list['ljpj'] != $get_order_details_button['ljpj'])
                    { // 底部立即评价按钮 与 该商品立即评价按钮 不一致（该订单下的商品，没有全部立即评论完）
                        $get_button_list['ljpj'] = 1; // 底部立即评价钮显示（该订单下，存在有的商品有立即评价按钮，有的没有立即评价按钮）
                        // $get_button_list['zjpj'] = 0; // 底部追加评价钮不显示
                    }
                    // else
                    // { // 底部立即评价按钮 与 该商品立即评价按钮 一致
                    //     if($get_button_list['ljpj'] == 0)
                    //     { // 底部没有立即评价钮（该订单下，所以商品都以立即评论完成）
                    //         if($get_button_list['zjpj'] != $get_order_details_button['zjpj'])
                    //         { // 底部追加评价按钮 与 该商品追加评价按钮 不一致（该订单下的商品，没有全部追加评论完）
                    //             $get_button_list['zjpj'] = 1; // 底部追加评价钮显示
                    //         }
                    //     }
                    //     else
                    //     { // 底部有立即评价钮
                    //         $get_button_list['zjpj'] = 0; // 底部追加评价钮不显示
                    //     }
                    // }
                }
                $get_order_details_button['ljpj'] = 0;
            }
        }
        else if($status == 7)
        { // 已关闭
            if($order_details_num == 1)
            { // 单商品订单
                if($get_order_details_button['cksh'] == 1)
                { // 该订单详情有查看售后按钮
                    $get_button_list['cksh'] = 1; // 底部查看售后按钮 0:没有 1:有
                    $get_order_details_button['cksh'] = 0; // 订单详情查看售后按钮隐藏
                }
            }
        }
        else if($status == 8)
        { // 待核销
            if($order_details_num == 1)
            { // 单商品订单
                if($get_order_details_button['sqsh'] == 1)
                { // 该订单详情有申请退款按钮
                    $get_button_list['sqsh'] = 1; // 底部申请退款按钮 0:没有 1:有
                    $get_order_details_button['sqsh'] = 0; // 该商品申请退款按钮隐藏
                }
                if($get_order_details_button['cksh'] == 1)
                { // 该订单详情有查看售后按钮
                    $get_button_list['cksh'] = 1; // 底部查看售后按钮 0:没有 1:有
                    $get_order_details_button['cksh'] = 0; // 该商品查看售后按钮隐藏
                }
            }
            else
            { // 多商品订单
                if($Number_of_cycles == 0)
                { // 第一次
                    $get_button_list['sqsh'] = $get_order_details_button['sqsh']; // 底部申请退款按钮 与 该商品申请退款按钮一致
                    // $get_button_list['cksh'] = $get_order_details_button['cksh']; // 底部查看售后按钮 与 该商品查看售后按钮一致
                }
                else
                {
                    if($get_button_list['sqsh'] != $get_order_details_button['sqsh'])
                    { // 底部申请退款按钮 与 该商品申请退款按钮 不一致（该订单下，存在有的商品有申请售后按钮，有的没有申请售后按钮）
                        $get_button_list['sqsh'] = 1; // 底部申请退款按钮显示
                    }
                    // if($get_button_list['cksh'] != $get_order_details_button['cksh'])
                    // { // 底部查看售后按钮 与 该商品查看售后按钮 不一致
                    //     $get_button_list['cksh'] = 0; // 底部查看售后钮不显示（该订单下，存在有的商品有查看售后按钮，有的没有查看售后按钮）
                    // }
                }
            }
        }

        $button_array = array('get_button_list'=>$get_button_list,'get_order_details_button'=>$get_order_details_button);
        return $button_array;
    }

    // 判断是否有积分抵扣
    public static function determine_whether_there_is_a_deduction_of_points($store_id,$user_id,$scoreDeduction,$scoreRatio = '')
    {
        $mch_id = self::SelfOperatedStore($store_id); // 获取自营店ID

        $r = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();
        $user_score = $r[0]['score']; // 用户积分

        $deductible_amount = 0; // 用户本次可抵扣金额
        $deductible_amount_all = 0; // 用户本次可抵扣金额
        $scoreDeductionValue = false; // 没有积分抵扣
        $sql_config = "select * from lkt_integral_config where store_id = '$store_id' and mch_id = '$mch_id' ";
        $r_config = Db::query($sql_config);
        if($r_config)
        {
            if($r_config[0]['status'] == 1)
            { // 积分商城插件开启
                if($r_config[0]['score_ratio'] != '')
                { // 积分抵扣比例不为空
                    $score_ratio = explode(':',$r_config[0]['score_ratio']);
                    $scoreDeductionValue = true; // 有积分抵扣

                    $deductible_amount = (float)$scoreDeduction * (float)$score_ratio[1] / (float)$score_ratio[0]; // 用户使用积分 * 金额比例 / 积分比例 = 用户本次可抵扣金额
                    $deductible_amount_all = (float)$user_score * (float)$score_ratio[1] / (float)$score_ratio[0]; // 用户积分 * 金额比例 / 积分比例 = 用户本次可抵扣金额
                }
            }
        }

        if($scoreRatio != $r_config[0]['score_ratio'] && $scoreRatio != '')
        {
            $message = Lang('tools.18');
            echo json_encode(array('message' => '比例设置发生改变','code' => ERROR_CODE_LBYSPKCBZ));
            exit;
        }

        $data = array('scoreDeductionValue'=>$scoreDeductionValue,'user_score'=>$user_score,'deductible_amount'=>$deductible_amount,'deductible_amount_all'=>$deductible_amount_all,'score_ratio'=>$r_config[0]['score_ratio']);
        return $data;
    }

    // 计算详单抵扣数据
    public static function Calculate_the_deduction_data_for_the_detailed_invoice($array)
    {
        $products = $array['products']; // 商品数据
        $products_total = $array['products_total']; // 商品总价
        $scoreDeductionPrice = $array['scoreDeductionPrice']; // 需要扣除的抵扣金额
        $scoreDeductionpoints = $array['scoreDeductionpoints']; // 需要扣除的抵扣积分

        $scoreDeductionPrice_z = 0; // 累计抵扣金额
        $scoreDeductionpoints_z = 0; // 累计抵扣积分
        $pro_num = count($products) - 1;
        foreach($products as $k => $v)
        {
            $pro_num1 = count($v['list']) - 1;
            foreach ($v['list'] as $ke => $va) 
            {
                if($pro_num == $k && $pro_num1 == $ke)
                { // 最后一个店铺 并且 最后一个商品
                    $scoreDeductionPrice_0 = round($scoreDeductionPrice - $scoreDeductionPrice_z,2);
                    $scoreDeductionpoints_0 = $scoreDeductionpoints - $scoreDeductionpoints_z;
                }
                else
                {
                    $scoreDeductionPrice_0 = round($va['amount_after_discount'] / $products_total * $scoreDeductionPrice,2); // 该详单优惠后金额 / 商品总价 * 需要扣除的抵扣金额 = 该详单抵扣金额
                    $scoreDeductionpoints_0 = round($va['amount_after_discount'] / $products_total * $scoreDeductionpoints); // 该详单优惠后金额 / 商品总价 * 需要扣除的抵扣积分 = 该详单抵扣积分

                    $scoreDeductionPrice_z += $scoreDeductionPrice_0; // 该详单抵扣金额
                    $scoreDeductionpoints_z += $scoreDeductionpoints_0; // 该详单抵扣金额
                }
                $products[$k]['list'][$ke]['scoreDeductionPrice'] = $scoreDeductionPrice_0; // 该详单抵扣金额
                $products[$k]['list'][$ke]['scoreDeductionpoints'] = $scoreDeductionpoints_0; // 该详单抵扣积分
            }
        }

        return $products;
    }

    // 地址转换
    public static function address_translation($array)
    {
        $cpc = $array['cpc']; // 区号
        $sheng = $array['sheng']; // 省
        $shi = $array['shi']; // 市
        $xian = $array['xian']; // 县
        $address = $array['address']; // 街道
        $code = $array['code']; // 邮政编码

        if($cpc == '')
        {
            $cpc = '86';
        }

        if($cpc == '86' || $cpc == '852')
        {
            $address_xq = $sheng . $shi . $xian . $address;
        }
        else
        {
            $address_xq = $address . $xian . $shi . $sheng . $code;
        }

        return $address_xq;
    }

    public static function upload()
    {
        $file = Request::file('file')? Request::file('file') :  Request::file('image');

        if (!$file || !$file->isValid()) 
        {
            $message = lang("没有上传文件或文件无效！");
            echo json_encode(array('code' => 109, 'message' => $message));
            exit();
        }

        $ext = strtolower($file->getOriginalExtension());
        $tempFilePath = $file->getRealPath(); // 获取临时文件路径

        $detectedMimeType = self::getMimeTypeByHeader($tempFilePath);

        // 定义允许的类型
        $allowedImageExts = ['jpg', 'jpeg', 'gif', 'png', 'bmp', 'webp'];
        $allowedVideoExts = ['mp4', 'mov', 'avi', 'wmv', 'mkv', 'flv', 'webm'];

        $imageMimeTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/bmp', 'image/webp'];
        $videoMimeTypes = ['video/mp4', 'video/mpeg', 'video/quicktime', 'video/x-msvideo', 'video/x-ms-wmv', 'video/x-matroska', 'video/x-flv', 'video/webm'];

        $isAllowedExt = in_array($ext, $allowedImageExts) || in_array($ext, $allowedVideoExts);

        if (!$isAllowedExt) 
        {
            $message = Lang("文件扩展名不允许！");
            echo json_encode(array('code' => 109, 'message' => $message));
            exit();
        }

        // 检查检测到的 MIME 类型是否匹配
        if (in_array($ext, $allowedImageExts) && in_array($detectedMimeType, $imageMimeTypes)) 
        {
            return 1;
        } 
        elseif (in_array($ext, $allowedVideoExts) && in_array($detectedMimeType, $videoMimeTypes)) 
        {
            return 2;
        } 
        else 
        {
            $message = Lang("文件类型验证失败！文件内容可能被篡改。");
            echo json_encode(array('code' => 109, 'message' => $message));
            exit();
        }
    }

    public static function getMimeTypeByHeader($filePath) 
    {
        $handle = fopen($filePath, 'rb');
        if (!$handle) 
        {
            return false;
        }
        $bytes = fread($handle, 8); // 读取前几个字节通常足够
        fclose($handle);

        if ($bytes === false) 
        {
            return false;
        }

        // 检查常见的图片和视频魔数
        $hex = bin2hex($bytes);
        $firstBytes = substr($hex, 0, 4); // JPEG, PNG, GIF 的魔数通常在前4字节

        if ($firstBytes === 'ffd8') 
        { // JPEG
            return 'image/jpeg';
        } 
        elseif (substr($hex, 0, 8) === '89504e47') 
        { // PNG
            return 'image/png';
        } 
        elseif (substr($hex, 0, 6) === '474946') 
        { // GIF
            return 'image/gif';
        } 
        elseif (substr($hex, 0, 8) === '424d') 
        { // BMP
            return 'image/bmp';
        } 
        elseif (substr($hex, 0, 8) === '52494646' && substr($hex, 8, 8) === '57454250') 
        { // WEBP
            return 'image/webp';
        } 
        elseif (substr($hex, 0, 8) === '00000018' && substr($hex, 8, 8) === '66747970' || // MP4 (ftyp)
                  substr($hex, 0, 8) === '00000020' && substr($hex, 8, 8) === '66747970' || // MP4 (ftyp)
                  substr($hex, 0, 4) === '6674' || // MP4 (ftyp short)
                  substr($hex, 0, 8) === '4d546864') 
        { // AVI (RIFF -> MThd header for AVI)
            // MP4 检查稍微复杂，这里简化处理，实际可能需要更详细的检查
            // AVI 检查：开头是 RIFF
            if (substr($hex, 0, 8) === '52494646') 
            { // RIFF
                return 'video/x-msvideo'; // AVI
            }
            return 'video/mp4'; // 假设是 MP4
        }
        // ... 可以继续添加更多魔数检查 ...

        return 'application/octet-stream'; // 未知类型
    }
}
