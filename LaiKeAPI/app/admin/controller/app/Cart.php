<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\LaiKeLogUtils;

use app\admin\model\ProductConfigModel;
use app\admin\model\UserModel;
use app\admin\model\ConfigureModel;
use app\admin\model\MchModel;
use app\admin\model\CartModel;
use app\admin\model\ProductListModel;
/**
 * 功能：购物车
 * 修改人：PJY
 */
class Cart 
{
	// 进入购物车页面
    public function index()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::param('access_id')); // 授权id
        $language = addslashes(Request::param('language')); // 语言

        $pro_type = trim(Request::param('commodity_type')); // 类型
        $num = trim(Request::param('page')); // 加载次数

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        $lang_code = Tools::get_lang($language);

        $end = 10;
        if (!$num)
        {
            $num = 1;
        }
        $start = ($num-1) * $end;
        $arr = array();
        $arr0 = array();
        $login_status = 0;

        $arr_0 = array(); // 已删除
        $arr_1 = array(); // 已下架
        $arr_2 = array(); // 已售罄

        // 获取商品设置
        $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>1));

        $Member_discount = array('store_id'=>$store_id,'access_id'=>$access_id);
        $grade_list= PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade'];
        $grade_rate = 1;

        if (!empty($access_id))
        { // 存在
            // 查询登录用户
            $r0_0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,grade')->select()->toArray();
            // 如果查询到用户，获取用户user_id ,登录状态设置为1 
            if ($r0_0)
            {
                $login_status = 1;
                $user_id = $r0_0[0]['user_id'];
            }

            $str = "a.id,a.store_id,a.token,a.user_id,a.Goods_id,a.Goods_num,a.Create_time,a.Size_id,a.pro_type,m.product_title,m.mch_id,m.status,c.recycle,c.id AS attribute_id,c.pid,c.price,c.img,c.unit,c.num as stock,c.attribute,mc.business_hours,mc.is_open";

            if (empty($user_id))
            {
                // 如果没有查询到用户，查询游客添加购物车商品
                $sql_c = "select $str from lkt_cart AS a LEFT JOIN lkt_product_list AS m  ON a.Goods_id = m.id LEFT JOIN lkt_configure AS c ON a.Size_id = c.id left join lkt_mch as mc on m.mch_id = mc.id where a.store_id = '$store_id' and m.status in (2,3) and m.mch_status = 2 and pro_type = '$pro_type' and m.lang_code = '$lang_code' and a.token = '$access_id' order by a.Create_time desc";
            }
            else
            {
                // 如果查询到用户，查询用户的购物车商品
                $sql_c = "select $str from lkt_cart AS a LEFT JOIN lkt_product_list AS m  ON a.Goods_id = m.id LEFT JOIN lkt_configure AS c  ON a.Size_id = c.id left join lkt_mch as mc on m.mch_id = mc.id where a.store_id = '$store_id' and m.status in (2,3) and m.mch_status = 2 and pro_type = '$pro_type' and m.lang_code = '$lang_code' and a.user_id = '$user_id' order by a.Create_time desc";
            }
            $r_c = Db::query($sql_c);
            //如果有购物车商品
            if (!empty($r_c))
            {
                $mch_str = '';
                // 循环处理购物车商品数据
                foreach ($r_c as $key => $value)
                {
                    $imgurl = ServerPath::getimgpath($value['img']); // 商品属性图片
                    $pid = $value['pid']; // 商品id
                    $status1 = $value['status']; // 商品状态

                    $attribute_id = $value['attribute_id']; // 商品属性id
                    $attribute_1 = '';
                    $mch_id = $value['mch_id'];
                    $mch_str .= $mch_id . ',';
                    // // 货币转换
                    // $value['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$value['price']));
                    $value['price'] = round($value['price'],2);
                    //如果商品下有其他属性
                    if (empty($value['attribute']))
                    {
                        $attribute1 = $value['attribute'];
                        $attribute2 = unserialize($attribute1);
                        if (!empty($attribute2))
                        {
                            foreach ($attribute2 as $k => $v)
                            {
                                if (strpos($k, '_LKT_') !== false)
                                {
                                    $v = substr($v, 0, strrpos($v, "_LKT"));
                                }
                                $attribute_1 .= $v . ' ';
                            }
                        }
                        $attribute_1 = trim($attribute_1, ' '); // 移除两侧的空格(购物车显示的属性)
                    }

                    if ($value['stock'] > 0)
                    {
                        $stock = $value['stock']; // 库存
                    } 
                    else
                    {
                        $stock = 0; // 库存
                    }

                    $r_size = ConfigureModel::where(['pid'=>$pid,'id'=>$attribute_id])->select()->toArray();
                    $skuBeanList = array();
                    $attrList = array();
                    $attr = array();
                    foreach ($r_size as $ke => $va)
                    {
                        $attribute = unserialize($va['attribute']);
                        $attnum = 0;
                        $arrayName = array();
                        foreach ($attribute as $k => $v)
                        {
                            if (!in_array($k, $arrayName))
                            {
                                array_push($arrayName, $k);
                                $kkk = $attnum++;
                                if (strpos($k, '_LKT_') !== false)
                                {
                                    $k = substr($k, 0, strrpos($k, "_LKT"));
                                }
                                $attrList[$kkk] = array('attrName' => $k, 'attrType' => '1', 'id' => md5($k), 'attr' => [], 'all' => []);
                            }
                        }
                    }
                    foreach ($r_size as $ke => $va)
                    {   
                        $attribute = unserialize($va['attribute']);
                        $attributes = array();
                        $name = '';
                        foreach ($attribute as $k => $v)
                        {
                            if (strpos($k, '_LKT_') !== false)
                            {
                                $k = substr($k, 0, strrpos($k, "_LKT"));
                                $v = substr($v, 0, strrpos($v, "_LKT"));
                            }
                            $attributes[] = array('attributeId' => md5($k), 'attributeValId' => md5($v));
                            $name .= $k.':'.$v.';';
                        }

                        $cimgurl = ServerPath::getimgpath($va['img']);
                        $unit = $va['unit'];
                        // // 货币转换
                        // $va['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$va['price']));
                        $va['price'] = round($va['price'],2);
                        $skuBeanList[$key] = array('name' => $name, 'imgurl' => $cimgurl, 'cid' => $va['id'], 'price' => $va['price'], 'count' => $va['num'], 'unit' => $unit, 'attributes' => $attributes);
                        for ($i = 0; $i < count($attrList); $i++)
                        {
                            $attr = $attrList[$i]['attr'];
                            $all = $attrList[$i]['all'];
                            foreach ($attribute as $k => $v)
                            {
                                if (strpos($k, '_LKT_') !== false)
                                {
                                    $k1 = substr($k, 0, strrpos($k, "_LKT"));
                                    $v1 = substr($v, 0, strrpos($v, "_LKT"));
                                    if ($attrList[$i]['attrName'] == $k1)
                                    {
                                        $attr_array = array('attributeId' => md5($k1), 'id' => md5($v1), 'attributeValue' => $v1, 'enable' => false, 'select' => false);

                                        if (empty($attr))
                                        {
                                            array_push($attr, $attr_array);
                                            array_push($all, $v1);
                                        } 
                                        else
                                        {
                                            if (!in_array($v1, $all))
                                            {
                                                array_push($attr, $attr_array);
                                                array_push($all, $v1);
                                            }
                                        }
                                    }
                                } 
                                else
                                {
                                    if ($attrList[$i]['attrName'] == $k)
                                    {
                                        $attr_array = array('attributeId' => md5($k), 'id' => md5($v), 'attributeValue' => $v, 'enable' => false, 'select' => false);

                                        if (empty($attr))
                                        {
                                            array_push($attr, $attr_array);
                                            array_push($all, $v);
                                        } 
                                        else
                                        {
                                            if (!in_array($v, $all))
                                            {
                                                array_push($attr, $attr_array);
                                                array_push($all, $v);
                                            }
                                        }
                                    }
                                }
                            }
                            $attrList[$i]['all'] = $all;
                            $attrList[$i]['attr'] = $attr;
                        }
                    }

                    if($grade)
                    {
                        $value['price'] = round($value['price'] * $grade_rate,2);
                    }

                    $value['attribute'] = $attribute_1;
                    $value['pro_name'] = $value['product_title'];
                    $value['num'] = $value['Goods_num'];
                    $value['contNum'] = $value['stock'];
                    $value['imgurl'] = $imgurl;
                    $value['attrList'] = $attrList;
                    $value['skuBeanList'] = $skuBeanList;

                    if($value['recycle'] == 1)
                    {
                        $arr_0[] = $value;
                    }
                    else
                    {
                        if($status1 == 2)
                        {
                            if( $stock == 0)
                            { // 已售罄
                                $value['pro_status'] = 2;
                                $value['goodsStatus'] = 0;
                                $arr_2[] = $value;
                            }
                            else
                            {
                                $value['pro_status'] = 0;
                                $arr[] = $value;
                            }
                        }
                        else
                        { // 已下架
                            $value['pro_status'] = 3;
                            $value['goodsStatus'] = 1;
                            $arr_1[] = $value;
                        }
                    }
                }
                $mch_str = rtrim($mch_str, ',');
                // 查询所有店铺信息
                $mch_res = MchModel::where(['review_status'=>1,'store_id'=>$store_id])->where('id','in',$mch_str)->select()->toArray();
            }
            else
            {
                $mch_res = array();
            }
        }
        else
        {
            $mch_res = array();
        }
        
        $arr0 = array_merge($arr_0 , $arr_1 , $arr_2);
        
        $str_t = " a.id,a.commodity_type,a.product_title,a.product_class,a.brand_id,a.freight,a.gongyingshang,a.imgurl,a.keyword,a.mch_id,a.num,a.recycle,a.s_type,a.status,a.subtitle,a.volume,c.id as cid,c.img,c.costprice,c.pid,min(c.price) over (partition by c.pid) as price,c.yprice,c.attribute,m.id as mchId,m.logo,m.name as mch_name,m.is_open,a.display_position_sort,a.upper_shelf_time,row_number () over (partition by c.pid) as top,a.is_appointment,a.write_off_settings,a.write_off_mch_ids ";

        $sql_t = "select tt.* from (select $str_t 
                    from lkt_product_list AS a 
                    RIGHT JOIN lkt_configure AS c ON a.id = c.pid 
                    left join lkt_mch as m on a.mch_id = m.id 
                    where a.store_id = '$store_id' and a.commodity_type in (0,1) and a.recycle = 0 and c.recycle = 0 and a.mch_status = 2 and a.show_adr like '%,2,%' and a.mch_id != 0 and a.lang_code = '$lang_code' $status) as tt where tt.top<2   
                    order by tt.display_position_sort desc,tt.upper_shelf_time desc 
                    LIMIT $start,$end";
        $r_t = Db::query($sql_t);
        if ($r_t)
        {
            foreach ($r_t as $k => $v)
            {
                $v['imgurl'] = ServerPath::getimgpath($v['imgurl']);
                $v['logo'] = ServerPath::getimgpath($v['logo']);
                $s_type = explode(',', trim($v['s_type'],','));
                $s_type_list = PC_Tools::getProductLabel0(array('store_id'=>$store_id,'s_type'=>$s_type));
                $v['s_type_list'] = $s_type_list;

                $v['sizeid'] = $v['cid'];
                $v['stockNum'] = $v['num'];
                // // 货币转换
                // $v['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$v['price']));
                $v['price'] = round($v['price'],2);
                $v['vip_yprice'] = $v['price'];
                $v['vip_price'] = sprintf("%.2f",round($v['price'] * $grade_rate,2));

                if($v['volume'] < 0)
                {
                    $v['volume'] = 0;
                }

                $pid = $v['id'];
                $r_configure = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->sum('num');
                $v['num'] = $r_configure;

                $v['payPeople'] = 0;
                $sql_3 = "select count(DISTINCT r_sNo) as total from lkt_order_details where r_status != 0 and p_id = '$pid' ";
                $r_3 = Db::query($sql_3);
                if($r_3)
                {
                    $v['payPeople'] = $r_3[0]['total'];
                }

                if($v['write_off_settings'] == 1)
                { // 核销设置 1.线下核销 2.无需核销
                    $v['is_appointment'] = 2; // 2.不能加入购物车
                    $v['isAddCar'] = 2; // 2.不能加入购物车
                }
                else
                {
                    $v['is_appointment'] = 1; // 1.能加入购物车
                    $v['isAddCar'] = 1; // 1.能加入购物车
                }
                $list[] = $v;
            }
        }
        else
        {
            $list = array();
        }

        $data = array('data' => $arr,'data0' => $arr0, 'list' => $list, 'login_status' => $login_status, 'mch_list' => $mch_res, 'grade' => $grade);
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    // 用户修改购物车数量操作
    public function up_cart()
    {
        $store_id = trim(Request::param('store_id'));
        $language = trim(Request::post('language')); // 语言
        $access_id = Request::post('access_id'); // 授权id
        $goods = Request::post('goods'); // 购物车id(购物车id+数量)

        $goods1 = htmlspecialchars_decode($goods);
        $goods2 = json_decode($goods1);

        if (!empty($access_id))
        { // 存在
            $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
            if ($r0)
            {
                $user_id = $r0[0]['user_id'];
            }
            if (is_array($goods2))
            {
                foreach ($goods2 as $k => $v)
                {
                    $cart_id = $v->cart_id;
                    $num = $v->num;
                    if($num <= 0)
                    {
                        $message = Lang('Parameter error');
                        return output(ERROR_CODE_CSCW,$message);
                    }
                    if (empty($user_id))
                    {
                        $r_1 = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'token'=>$access_id])->field('Size_id')->select()->toArray();
                    }
                    else
                    {
                        $r_1 = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'user_id'=>$user_id])->field('Size_id')->select()->toArray();
                    }
                    if ($r_1)
                    {
                        $attribute_id = $r_1[0]['Size_id']; // 属性id
                        // 根据属性id,查询库存
                        $r_2 = ConfigureModel::where(['id'=>$attribute_id,'recycle'=>0])->field('num')->select()->toArray();
                        $z_num = $r_2[0]['num'];
                        if ($z_num >= $num)
                        {
                            if (empty($user_id))
                            {
                                $sql_u = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'token'=>$access_id])->find();
                            } else
                            {
                                $sql_u = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'user_id'=>$user_id])->find();
                            }
                            $sql_u->Goods_num = $num;
                            $r_u = $sql_u->save();
                            if ($r_u)
                            {
                                $message = Lang('Success');
                                return output(200,$message);
                            } 
                            else
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '用户修改购物车商品数量失败,cart_id:' . $cart_id;
                                $this->Log($Log_content);

                                $message = Lang('Parameter error');
                                return output(ERROR_CODE_CSCW,$message);
                            }
                        } 
                        else
                        {
                            if (empty($user_id))
                            {	
                            	$sql_u = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'token'=>$access_id])->find();
                            } 
                            else
                            {	
                            	$sql_u = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'user_id'=>$user_id])->find();
                            }
                            $sql_u->Goods_num = $z_num;
                            $r_u = $sql_u->save();
                            if ($r_u)
                            {
                                $message = Lang('Success');
                                return output(200,$message);
                            } 
                            else
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '用户修改购物车商品数量失败,cart_id:' . $cart_id;
                                $this->Log($Log_content);
                                $message = Lang('Parameter error');
                                return output(ERROR_CODE_GXGWCSB,$message);
                            }
                        }
                    } 
                    else
                    {
                        $message = Lang('Parameter error');
                        return output(ERROR_CODE_GXGWCSB,$message);
                    }
                }
            } 
            else
            {
                $cart_id = $goods2->cart_id;
                $num = $goods2->num;
                if($num <= 0)
                {
                    $message = Lang('Parameter error');
                    return output(ERROR_CODE_CSCW,$message);
                }
                if (empty($user_id))
                {
                    $r_1 = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'token'=>$access_id])->field('Size_id')->select()->toArray();
                } 
                else
                {
                    $r_1 = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'user_id'=>$user_id])->field('Size_id')->select()->toArray();
                }
                if ($r_1)
                {
                    $attribute_id = $r_1[0]['Size_id']; // 属性id

                    // 根据属性id,查询库存
                    $r_2 = ConfigureModel::where(['id'=>$attribute_id,'recycle'=>0])->field('num')->select()->toArray();
                    $z_num = $r_2[0]['num'];
                    if ($z_num >= $num)
                    {
                        if (empty($user_id))
                        {	
                        	$sql_u = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'token'=>$access_id])->find();
                        } 
                        else
                        {
                            $sql_u = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'user_id'=>$user_id])->find();
                        }
                        $sql_u->Goods_num = $num;
                        $r_u = $sql_u->save();
                        if ($r_u)
                        {
                            $message = Lang('Success');
                            return output(200,$message);
                        }
                        else
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '用户修改购物车商品数量失败,cart_id:' . $cart_id;
                            $this->Log($Log_content);
                            $message = Lang('Parameter error');
                            return output(ERROR_CODE_GXGWCSB,$message);
                        }
                    } 
                    else
                    {
                        if (empty($user_id))
                        {	
                        	$sql_u = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'token'=>$access_id])->find();
                        } 
                        else
                        {	
                        	$sql_u = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'user_id'=>$user_id])->find();
                        }
                        $sql_u->Goods_num = $z_num;
                        $r_u = $sql_u->save();
                        if ($r_u)
                        {
                            $message = Lang('Success');
                            return output(200,$message);
                        }
                        else
                        {
                            $Log_content = __METHOD__ . '->' . __LINE__ . '用户修改购物车商品数量成功,cart_id:' . $cart_id;
                            $this->Log($Log_content);

                            $message = Lang('Parameter error');
                            return output(ERROR_CODE_GXGWCSB,$message);
                        }
                    }
                }
            }
        }
        else
        {
            $message = Lang('Please log in');
            return output(ERROR_CODE_QDL,$message);
        }
    }

    // 清空购物车
    public function delAll_cart()
    {
        $store_id = trim(Request::param('store_id'));
        $language = trim(Request::post('language')); // 语言
        $access_id = trim(Request::post('access_id')); // 授权id
        if (!empty($access_id))
        { // 存在
            $r0_0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
            if ($r0_0)
            {
                $user_id = $r0_0[0]['user_id'];
            }
            if (empty($user_id))
            {
                $res = CartModel::where(['store_id'=>$store_id,'token'=>$access_id])->delete();
            } 
            else
            {
                $res = CartModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->delete();
            }
            if ($res <= 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '清空购物车失败,access_id:' . $access_id;
                $this->Log($Log_content);

                $message = Lang('Busy network');
                return output(ERROR_CODE_GXGWCSB,$message);
            }
            else
            {
                $message = Lang('Success');
                return output(200,$message);
            }
        } 
        else
        {
            $message = Lang('Please log in');
            return output(ERROR_CODE_QDL,$message);
        }
    }

    // 删除购物车指定商品
    public function delcart()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::post('access_id')); // 授权id
        $cart_id = Request::post('cart_id');
        if (!empty($access_id))
        { // 存在
            $r0_0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
            if ($r0_0)
            {
                $user_id = $r0_0[0]['user_id'];
            }
        } 
        else
        {
            $message = Lang('Please log in');
            return output(ERROR_CODE_QDL,$message);
        }

        $typeArr = array();
        if (!empty($access_id))
        { // 存在
            if (!empty($cart_id))
            {
                if (is_array($cart_id))
                { // 是数组
                    foreach ($cart_id as $key => $value)
                    {
                        $typeArr[$key] = $value;
                    }
                } 
                else if (is_string($cart_id))
                { // 是字符串
                    $typestr = trim($cart_id, ','); // 移除两侧的逗号
                    $typeArr = explode(',', $typestr); // 字符串打散为数组
                }

                //循环删除指定的购物车商品
                foreach ($typeArr as $key => $value)
                {
                    if (empty($user_id))
                    {
                        $res = CartModel::where(['store_id'=>$store_id,'id'=>$value,'token'=>$access_id])->delete();
                    } 
                    else
                    {
                        $res = CartModel::where(['store_id'=>$store_id,'id'=>$value,'user_id'=>$user_id])->delete();
                    }
                    if($res <= 0)
                    {
                    	if (empty($user_id))
	                    {
	                        $Log_content = __METHOD__ . '->' . __LINE__ . '游客删除购物车指定商品失败,cart_id:' . $value;
	                    } 
                        else
	                    {
	                        $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '删除购物车指定商品失败,cart_id:' . $value;
	                    }
	                    $this->Log($Log_content);

	                    $message = Lang('Busy network');
                		return output(ERROR_CODE_GXGWCSB,$message);
                    }
                }
                if (empty($user_id))
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '游客删除购物车指定商品成功,cart_id:' . $cart_id;
                } 
                else
                {
                    $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '删除购物车指定商品成功,cart_id:' . $cart_id;
                }
                $this->Log($Log_content);

                $message = Lang('Success');
                return output(200,$message);
            }
        } 
        else
        {
            $message = Lang('Please log in');
            return output(ERROR_CODE_QDL,$message);
        }
    }

    // 修改购物车商品属性
    public function modify_attribute()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::post('access_id')); // 授权id
        $cart_id = Request::post('cart_id'); // 传过来的购物车id
        $attribute_id = Request::post('attribute_id'); // 传过来的修改后的属性id
        $user_id = null;
        if (empty($cart_id) || empty($attribute_id))
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW,$message);
        }
        if (!empty($access_id))
        { // 存在
            $r0_0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id')->select()->toArray();
            if ($r0_0)
            {
                $user_id = $r0_0[0]['user_id'];
            }
            // 根据购物车ID，查询购物车里的数据
            if (empty($user_id))
            {
                $r_1 = CartModel::where(['store_id'=>$store_id,'token'=>$access_id,'id'=>$cart_id])->select()->toArray();
            } 
            else
            {
                $r_1 = CartModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'id'=>$cart_id])->select()->toArray();
            }
            if ($r_1)
            {
                $Goods_id1 = $r_1[0]['Goods_id']; // 传过来的购物车商品id
                $Goods_num1 = $r_1[0]['Goods_num']; // 传过来的购物车商品数量
                // 根据操作人信息，查询所以购物车信息
                if (empty($user_id))
                {
                    $r_2 = CartModel::where(['store_id'=>$store_id,'token'=>$access_id])->select()->toArray();
                } 
                else
                {
                    $r_2 = CartModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->select()->toArray();
                }
                foreach ($r_2 as $k => $v)
                {
                    $c_id = $v['id']; // 原购物车id
                    $Goods_id2 = $v['Goods_id']; // 原购物车商品id
                    $Goods_num2 = $v['Goods_num']; // 原购物车商品数量
                    $Size_id = $v['Size_id']; // 原购物车属性id
                    // 根据原购物车属性ID，查询属性库存
                    $r_3 = ConfigureModel::where(['pid'=>$Goods_id2,'id'=>$Size_id,'recycle'=>0])->field('num')->select()->toArray();
                    $num = $r_3[0]['num']; // 库存
                    if ($Size_id == $attribute_id)
                    { // 原购物车属性ID = 修改后的属性ID
                        if ($c_id != $cart_id)
                        { // 原购物车ID != 传过来的购物车ID
                        	$sql_4 = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id])->find();
                        	$sql_4->Size_id = $attribute_id;
                            if ($Goods_num1 + $Goods_num2 > $num)
                            { // 传过来的购物车商品数量 + 原购物车商品数量 > 库存
                                // 根据传过来的购物车ID，修改购物车数量(库存数量)和属性ID
                                $sql_4->Goods_num = $num;
                            }
                            else
                            {	
                                // 根据传过来的购物车ID，修改购物车数量(传过来的购物车商品数量 + 原购物车商品数量)和属性ID
                                $sql_4->Goods_num = Db::raw('Goods_num+'.$Goods_num2);
                            }
                            $r_4 = $sql_4->save();
                            if (!$r_4)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改购物车商品属性,合并购物车商品失败,attribute_id:' . $attribute_id;
                                $this->Log($Log_content);
                                $message = Lang('Parameter error');
                                return output(ERROR_CODE_CSCW,$message);
                            }
                            // 根据原购物车ID，删除数据
                            $sql_5 = CartModel::find($c_id);
							$r_5 = $sql_5->delete();
                            if (!$r_5)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改购物车商品属性,删除购物车需要合并的数据,cart_id:' . $c_id;
                                $this->Log($Log_content);

                                $message = Lang('Parameter error');
                                return output(ERROR_CODE_CSCW,$message);
                            }
                        }
                    }
                    else
                    { // 原购物车属性ID != 修改后的属性ID
                        // 根据传过来的修改后的属性id，查询库存
                        $r_4 = ConfigureModel::where(['pid'=>$Goods_id1,'id'=>$attribute_id,'recycle'=>0])->field('num')->select()->toArray();
                        if ($r_4)
                        {
                            $num = $r_4[0]['num'];// 库存
                            $sql_5 = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id])->find();
                            $sql_5->Size_id = $attribute_id;
                            if ($Goods_num1 > $num)
                            { // 传过来的购物车商品数量 > 库存
                                // 根据传过来的购物车ID，修改购物车数量(库存数量)和属性ID
                                $sql_5->Goods_num = $num;
                            }
                            $r_5 = $sql_5->save();
                            if (!$r_5)
                            {
                                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '修改购物车商品属性失败,cart_id:' . $cart_id;
                                $this->Log($Log_content);

                                $message = Lang('Parameter error');
                                return output(ERROR_CODE_CSCW,$message);
                            }
                        }
                    }
                }

                $message = Lang('Success');
                return output(200,$message);
            }
            else
            {
                $message = Lang('Parameter error');
                return output(ERROR_CODE_CSCW,$message);
            }
        }
        else
        {
            $message = Lang('Please log in');
            return output(ERROR_CODE_QDL,$message);
        }
    }

    // 修改购物车商品属性获取属性
    public function dj_attribute()
    {
        $store_id = trim(Request::param('store_id'));
        $access_id = trim(Request::post('access_id')); // 授权id
        $cart_id = Request::post('cart_id'); // 购物车id
        $is_grade = 0;//非会员

        $currency_id = cache($access_id . '_currency'); // 获取用户默认币种
        if (empty($cart_id))
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW,$message);
        }

        $Member_discount = array('store_id'=>$store_id,'access_id'=>$access_id);
        $grade_list= PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade'];
        // $grade_rate = $grade_list['grade_rate'];
        $grade_rate = 1;
        if (!empty($access_id))
        { // 存在
            $r0 = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->field('user_id,grade')->select()->toArray();
            if ($r0)
            {
                $user_id = $r0[0]['user_id'];
                $r1 = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'user_id'=>$user_id])->field('Goods_id')->select()->toArray();
                if ($grade)
                {
                    $is_grade = 1;//是会员
                }
            }
            else
            {	
            	$r1 = CartModel::where(['store_id'=>$store_id,'id'=>$cart_id,'token'=>$access_id])->field('Goods_id')->select()->toArray();
            }

            if ($r1)
            {
                $pid = $r1[0]['Goods_id'];
                //获取商品active
                $res_p = ProductListModel::where('id',$pid)->field('active')->select()->toArray();
                $active = $res_p[0]['active'];

                $r_size = ConfigureModel::where(['pid'=>$pid,'recycle'=>0])->select()->toArray();
                $skuBeanList = array();
                $attrList_0 = array();
                $attrList_1 = array();
                $attrList_2 = array();
                $attrList_3 = array();
                $attrList_4 = array();
                $attrList = array();
                $attr = array();
                $attribute_list = array();
                $num_0 = 0;
                foreach ($r_size as $key => $value)
                {
                    // // 货币转换
                    // $value['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$value['price']));
                    $value['price'] = round($value['price'],2);
                    if ($active == 1 && $is_grade == 1)
                    {
                        $value['price'] = sprintf("%.2f",round($value['price'] * $grade_rate, 2));
                    }
                    $attribute_list[$key]['SkuID'] = $value['id'];
                    $attribute_list[$key]['Stock'] = $value['num'];
                    $attribute_list[$key]['Price'] = $value['price'];
                    $attribute_list[$key]['Pic'] = ServerPath::getimgpath($value['img'],0);
                    $attribute = unserialize($value['attribute']);
                    foreach ($attribute as $k => $v)
                    {
                        if (strpos($k, '_LKT_') !== false)
                        {
                            $k = substr($k, 0, strrpos($k, "_LKT"));
                            $v = substr($v, 0, strrpos($v, "_LKT"));
                        }
                        if($num_0 == 0)
                        {
                            $attrList_1[$k][] = $k . '-' .$v;
                        }
                        else
                        {
                            $str = $k . '-' .$v;
                            if(!in_array($str,$attrList_1[$k]))
                            {
                                $attrList_1[$k][] = $k . '-' .$v;
                            }
                        }
                        $attribute_list[$key][$k] = $v;
                    }
                }
                foreach ($attrList_1 as $k0 => $v0)
                {
                    $attrList_2[] = $v0;
                }
                $attrList_3 = PC_Tools::getArrSet($attrList_2);
                foreach ($attrList_3 as $k1 => $v1)
                {
                    foreach ($v1 as $k2 => $v2)
                    {
                        $v2_arr = explode('-',$v2);
                        if(count($v2_arr) == 2)
                        {
                            $attrList_4[$k1][$v2_arr[0]] = $v2_arr[1];
                        }
                        else if(count($v2_arr) > 2)
                        {
                            $attrList_4[$k1][$v2_arr[0]] = '';
                            for ($i = 1;$i < count($v2_arr);$i++)
                            {
                                $attrList_4[$k1][$v2_arr[0]] .= $v2_arr[$i] . '-';
                            }
                            $attrList_4[$k1][$v2_arr[0]] = trim($attrList_4[$k1][$v2_arr[0]],'-');
                        }
                    }
                }
                foreach ($attribute_list as $k3 => $v3)
                {
                    foreach ($attrList_4 as $k4 => $v4)
                    {
                        $diff = array_diff($v3, $v4);
                        if(count($diff) == 4 && isset($diff['SkuID']) && isset($diff['Stock']) && isset($diff['Price']) && isset($diff['Pic']))
                        {
                            $attrList_4[$k4] = $v3;
                        }
                    }
                }
                foreach ($attrList_4 as $k4 => $v4)
                {
                    if(!isset($v4['SkuID']))
                    {
                        $attrList_4[$k4]['SkuID'] = 0;
                    }
                    if(!isset($v4['Stock']))
                    {
                        $attrList_4[$k4]['Stock'] = 0;
                    }
                    if(!isset($v4['Price']))
                    {
                        $attrList_4[$k4]['Price'] = 0;
                    }
                    if(!isset($v4['Pic']))
                    {
                        $attrList_4[$k4]['Pic'] = '';
                    }
                }
                $attribute_list = $attrList_4;

                foreach ($r_size as $ke => $va)
                {
                    $attribute = unserialize($va['attribute']);
                    $attnum = 0;
                    $arrayName = array();
                    foreach ($attribute as $k => $v)
                    {
                        if (!in_array($k, $arrayName))
                        {
                            array_push($arrayName, $k);
                            $kkk = $attnum++;
                            if (strpos($k, '_LKT_') !== false)
                            {
                                $k = substr($k, 0, strrpos($k, "_LKT"));
                            }
                            $attrList[$kkk] = array('attrName' => $k, 'attrType' => '1', 'id' => md5($k), 'attr' => [], 'all' => []);
                        }
                    }
                }
                foreach ($r_size as $ke => $va)
                {
                    $attribute = unserialize($va['attribute']);
                    $attributes = array();
                    $name = '';
                    foreach ($attribute as $k => $v)
                    {
                        if (strpos($k, '_LKT_') !== false)
                        {
                            $k = substr($k, 0, strrpos($k, "_LKT"));
                            $v = substr($v, 0, strrpos($v, "_LKT"));
                        }
                        $attributes[] = array('attributeId' => md5($k), 'attributeValId' => md5($v));
                        $name .= $v;
                    }

                    $cimgurl = ServerPath::getimgpath($va['img'],0);
                    $unit = $va['unit'];

                    // // 货币转换
                    // $va['price'] = Tools::Currency_Conversion(array('store_id'=>$store_id,'currency_id'=>$currency_id,'money'=>$va['price']));
                    $va['price'] = round($va['price'],2);
                    $skuBeanList[] = array('name' => $name, 'imgurl' => $cimgurl, 'cid' => $va['id'], 'price' => $va['price'], 'count' => $va['num'], 'unit' => $unit, 'attributes' => $attributes);
                    for ($i = 0; $i < count($attrList); $i++)
                    {
                        $attr = $attrList[$i]['attr'];
                        $all = $attrList[$i]['all'];
                        foreach ($attribute as $k => $v)
                        {
                            if (strpos($k, '_LKT_') !== false)
                            {
                                $k = substr($k, 0, strrpos($k, "_LKT"));
                                $v = substr($v, 0, strrpos($v, "_LKT"));
                            }

                            if ($attrList[$i]['attrName'] == $k)
                            {
                                $attr_array = array('attributeId' => md5($k), 'id' => md5($v), 'attributeValue' => $v, 'enable' => false, 'select' => false);

                                if (empty($attr))
                                {
                                    array_push($attr, $attr_array);
                                    array_push($all, $v);
                                } 
                                else
                                {
                                    if (!in_array($v, $all))
                                    {
                                        array_push($attr, $attr_array);
                                        array_push($all, $v);
                                    }
                                }
                            }
                        }
                        $attrList[$i]['all'] = $all;
                        $attrList[$i]['attr'] = $attr;
                    }
                }
                $arr[] = array('attrList' => $attrList, 'skuBeanList' => $skuBeanList,'attribute_list'=>$attribute_list);
                $message = Lang('Success');
                return output(200,$message,$arr);
            }
            else
            {
                $message = Lang('Parameter error');
                return output(ERROR_CODE_CSCW,$message);
            }
        }
        else
        {
            $message = Lang('Please log in');
            return output(ERROR_CODE_QDL,$message);
        }
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/cart.log",$Log_content);
        return;
    }
}