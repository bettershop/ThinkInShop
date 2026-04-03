<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Session;
use think\facade\Request;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\WXTopicMsgUtils;
use app\common\Plugin\PluginUtils;


use app\admin\model\UserModel;
use app\admin\model\UserAddressModel;
use app\admin\model\MchModel;
/**
 * 功能：后台代课下单类
 * 修改人：PJY
 */
class ValetOrder extends BaseController
{   
    //订单结算页
    public function Settlement()
    {
        $admin_list = $this->user_list;
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $admin_name = $this->user_list['name'];
        $admin_type1 = $this->user_list['type'];

        $user_id = $this->request->param('userId');
        $products = $this->request->param('products');
        $wipe_off = $this->request->param('wipeOff');

        $products = json_decode(urldecode($products),true);

        //获取默认地址
        $address_id = '';
        $res_add = UserAddressModel::where(['store_id'=>$store_id,'uid'=>$user_id,'is_default'=>1])->select()->toArray();
        if($res_add)
        {
            $address_id = $res_add[0]['id'];
        }
        
        $pro_arr = array();
        $gz_price = 0;//商品总价
        $z_price = 0;//支付金额
        $yunfei = 0;//运费
        $grade_discount = 0;//会员折扣金额
        $zfreight = 0;
        if($products)
        {   
            $products0 = $this->products_list($store_id,$products);
            $products_total = 0;
            $products_data = Tools::get_products_data($store_id,$products0, $products_total, 'GM');
            $products0 = $products_data['products'];
            $products_freight = $products_data['products_freight'];
            $products_total = $products_data['products_total'];
            $no_delivery_str = '';
            if($address_id == '')
            { // 获取不配送省的名称
                $no_delivery_str = Tools::No_distribution_Province($store_id, $products_freight);
            }
            //查询默认地址order_details
            $address = Tools::find_address($store_id, $user_id,$no_delivery_str, $address_id);
            if(empty($address))
            {
                $message = Lang("nomal_order.1");
                return output(400,$message);
            }
            $products_data0 = Tools::get_products_data0($store_id, $products0,$products_total, $user_id);
            $grade_rate = $products_data0['grade_rate'];
            $products0 = $products_data0['products'];

            //4.计算运费
            $freight = Tools::get_freight($products_freight, $products0, $address, $store_id, 'GM');
            $yunfei = $freight['yunfei'];
            $products1 = $freight['products'];

            //获取商品信息
            foreach ($products as $key => $value) 
            {
                $pro_id = $value['pid'];
                $size_id = $value['id'];
                $num = $value['num'];
                //查询商品信息
                $sql_p = "select m.product_title,c.price,c.attribute,c.img,m.freight,m.mch_id from lkt_product_list AS m LEFT JOIN lkt_configure AS c ON m.id = c.pid  where m.store_id = '$store_id' and m.id = '$pro_id' and c.id = '$size_id'";
                $res_p = Db::query($sql_p);
                $attribute = unserialize($res_p[0]['attribute']);
                $pro_arr[$key]['size'] = '';
                foreach ($attribute as $ka => $va)
                {
                    if (strpos($ka, '_LKT_') !== false)
                    {
                        $ke = substr($ka, 0, strrpos($ka, "_LKT"));
                        $va = substr($va, 0, strrpos($va, "_LKT"));
                        $pro_arr[$key]['size'] .= $ke . ":" . $va . ";";
                    }
                    else
                    {
                        $pro_arr[$key]['size'] .= $ka . ":" . $va . ";";
                    }
                }
                $pro_arr[$key]['goodsId'] = $pro_id;
                $pro_arr[$key]['attr_id'] = $size_id;
                $pro_arr[$key]['price'] = $res_p[0]['price'];
                $pro_arr[$key]['nums'] = $num;
                $pro_arr[$key]['imgurl'] = ServerPath::getimgpath($res_p[0]['img'],$store_id);
                $pro_arr[$key]['mch_id'] = $res_p[0]['mch_id'];
                //获取店铺信息
                $res_m = MchModel::where('id',$res_p[0]['mch_id'])->field('name')->select()->toArray();
                if($res_m)
                {
                    $pro_arr[$key]['name'] = $res_m[0]['name'];
                }
                else
                {
                    $pro_arr[$key]['name'] = '';
                }
                $gz_price += $res_p[0]['price'] * $num;
                foreach ($products1 as $k1 => $val1) 
                {
                    foreach ($val1['list'] as $k2 => $val2) 
                    {
                        if ($val2['pid'] == $value['pid']) {
                            $pro_arr[$key]['attribute'] = $val2['size'];
                            $pro_arr[$key]['product_title'] = $val2['product_title'];
                            $pro_arr[$key]['freight'] = $val2['freight_price'];
                        }
                    }
                }
                //代客下单直接取值 第一次-1时跳过
                $pfreight = $value['freight'];
                if ($pfreight >= 0) 
                {
                    $pro_arr[$key]['freight'] = $pfreight;
                    $zfreight += $pfreight;
                }
            }
        }
        //代客下单设置了运费时取传参值
        if($zfreight)
        {
            $yunfei = $zfreight;
        }
        $z_price = round($gz_price + $yunfei - floatval($wipe_off) ,2);
        $data = array('goodsPriceTotal'=>$gz_price,'payPrice'=>$z_price,'vipDiscountPrice'=>0,'zfreight'=>$yunfei,'goosdList'=>$pro_arr);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    //区分购物车结算和立即购买---列出选购商品
    public function products_list($store_id,$product)
    {
        $list = array();
        $list0 = array();
        $list1 = array();
        $list2 = array();

        foreach($product as $k => $v)
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

        foreach($product as $k => $v)
        {
            if($v['num'] == $list1[$v['pid']])
            {
                if(!in_array($v['pid'],$list2))
                {
                    $list2[] = $v['pid'];
                    $product[$k]['tongbu'] = 1;
                }
                else
                {
                    $product[$k]['tongbu'] = 2;
                }
            }
            else
            {
                $product[$k]['tongbu'] = 2;
            }
            $product[$k]['merge_num'] = $list[$v['pid']];
            $product[$k]['cid'] = $v['id'];
        }

        return $product;
    }
}