<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Plugin\Plugin;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;

use app\admin\model\MchModel;
use app\admin\model\UserCollectionModel;
use app\admin\model\ConfigureModel;
use app\admin\model\ProLabelModel;
use app\admin\model\ProductListModel;


/**
 * 功能：用户收藏类
 * 修改人：PJY
 */
class AddFavorites extends BaseController
{   
    //收藏列表
    public function collection()
    {
        $store_id = $this->request->param('store_id');
        $access_id = $this->request->post('access_id');
        $language = $this->request->param('language'); // 语言
        $type = $this->request->post('type');

        $user_id = $this->user_list['user_id'];

        $lang_code = Tools::get_lang($language);

        $arr = array();

        $Member_discount = array('store_id'=>$store_id,'access_id'=>$access_id);
        $grade_list= PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade'];
        $grade_rate = $grade_list['grade_rate'];
        $mch_status = false;
        $Plugin_arr = new Plugin();
        $Plugin_arr1 = $Plugin_arr->front_Plugin($store_id);
        foreach ($Plugin_arr1 as $k => $v)
        {
            if ($k == 'MchPublicMethod' && $v == 1)
            {
                $mch_status = true;
            }
        }
        $data = array();
        if ($type == 1)
        {
            // 根据用户id、收藏表中的产品id与产品表中的id,查询收藏表(id,产品id)、产品表(产品名称)
            $sql = "select c.id,c.p_id,a.product_title,a.mch_id,a.recycle,a.status,a.num,a.is_presell,a.s_type from lkt_user_collection as c left join lkt_product_list as a on c.p_id = a.id where a.store_id = '$store_id' and c.type=1 and c.user_id = '$user_id' and a.mch_id != 0 and a.lang_code = '$lang_code' order by c.add_time desc";
            $r_1 = Db::query($sql);
            if ($r_1)
            {
                foreach ($r_1 as $k => $v)
                {

                    $mch_id = $v['mch_id'];
                    // 根据商城ID、用户ID、店铺审核状态通过，查询是否有店铺
                    $r0 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id,'recovery'=>0])->select()->toArray();
                    if ($r0)
                    {
                        $v['mch_name'] = $r0[0]['name'];
                    }
                    else
                    {
                        $v['mch_name'] = '';
                    }

                    $pid = $v['p_id'];
                    $rr = ConfigureModel::where('pid',$pid)->field('img,price,yprice')->select()->toArray();

                    $v['vip_yprice'] = (float)$rr[0]['price'];
                    $v['vip_price'] = round($rr[0]['price'] * $grade_rate , 2);
                    $v['price'] = (float)$rr[0]['price'];
                    $v['yprice'] = (float)$rr[0]['yprice'];
                    $v['imgurl'] = ServerPath::getimgpath($rr[0]['img']);
                    switch ($v['is_presell']) 
                    {
                        case '1':
                            $v['isPreGood'] = true;
                            break;
                        
                        default:
                            $v['isPreGood'] = false;
                            break;
                    }
                    $s_type = $v['s_type'];
                    $s_type_arr = explode(',',trim($s_type,','));
                    $v['s_type_list'] = array();
                    if($s_type_arr)
                    {
                        foreach ($s_type_arr as $key => $value) 
                        {
                            $r_sp_type = ProLabelModel::where(['store_id'=>$store_id,'id'=>$value])->field('id,name,color')->order('add_time','desc')->select()->toArray();
                            if($r_sp_type)
                            {
                                $v['s_type_list'][$key]['id'] = $r_sp_type[0]['id'];
                                $v['s_type_list'][$key]['color'] = $r_sp_type[0]['color'];
                                $v['s_type_list'][$key]['name'] = $r_sp_type[0]['name'];
                            } 
                        }
                    }
                    $arr[$k] = $v;
                }
            }
            $message = Lang('Success');
            return output(200,$message,array('list' => $arr,'grade' =>$grade,'mch_status'=>$mch_status));
        }
        else
        {
            // 根据用户id、收藏表中的产品id与产品表中的id,查询收藏表(id,产品id)、产品表(产品名称)
            $sql = "select a.id as shopid,c.id,c.mch_id,a.head_img as img,a.name as mch_name,a.collection_num from lkt_user_collection as c left join lkt_mch as a on c.mch_id = a.id where a.store_id = '$store_id' and a.recovery = 0 and c.type=1 and c.user_id = '$user_id' order by c.add_time desc";
            $r_1 = Db::query($sql);
            if ($r_1)
            {
                foreach ($r_1 as $k => $v)
                {
                    $shop_id = $v['shopid'];
                    $status = PC_Tools::getProductSettings(array('store_id' => $store_id,'type'=>2));
                    $mch_list = PC_Tools::StoreData($store_id,$shop_id,$status);
                    $v['collection_num'] = $mch_list['collection_num'];
                    $v['quantity_on_sale'] = $mch_list['quantity_on_sale'];
                    $v['quantity_sold'] = $mch_list['quantity_sold'];
                    $v['img'] = ServerPath::getimgpath($v['img']);
                    //查询是否上新
                    $v['isUploadNewDate'] = false;
                    $time = date("Y-m-d 00:00:00",strtotime("-6 day"));
                    $res_q = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'recycle'=>0,'lang_code'=>$lang_code])->where('add_date','>=',$time)->field('id')->select()->toArray();
                    if($res_q)
                    {
                        $v['isUploadNewDate'] = true;
                    }
                    $arr[$k] = $v;
                }
            }
            $message = Lang('Success');
            return output(200,$message,array('list' => $arr,'grade' =>$grade,'mch_status'=>$mch_status));
        }
        return;
    }

    //点击收藏
    public function index()
    {
        $store_id = $this->request->param('store_id');
        $type = $this->request->has('type')?$this->request->post('type'):1;
        $pro_id = $this->request->post('pro_id');

        $user_id = $this->user_list['user_id'];

        if (empty($pro_id))
        {
            $message = Lang('Parameter error');
            return output(ERROR_CODE_CSCW, $message);
        }
        // 根据用户id,产品id,查询收藏表
        if ($type == 2)
        {
            $r = UserCollectionModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pro_id,'type'=>2])->select()->toArray();
        }
        else
        {
            $r = UserCollectionModel::where(['store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pro_id])->select()->toArray();
        }
        if ($r)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '收藏商品ID为' . $pro_id . '时失败,已经收藏!';
            $this->Log($Log_content);
            $message = Lang('favorites.0');
            return output(ERROR_CODE_SZSB_001,$message);
        }
        else
        {   
            // 在收藏表里添加一条数据
            $sql = array('store_id'=>$store_id,'user_id'=>$user_id,'p_id'=>$pro_id,'add_time'=>date('Y-m-d H:i:s'),'type'=>$type);
            $r = Db::name('user_collection')->insertGetId($sql);
            if ($r > 0)
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '收藏商品ID为' . $pro_id . '成功!';
                $this->Log($Log_content);
                $data = array('collection_id' => $r);
                $message = Lang('Success');
                return output(200,$message,$data);
            }
            else
            {
                $Log_content = __METHOD__ . '->' . __LINE__ . '用户' . $user_id . '收藏商品ID为' . $pro_id . '失败!';
                $this->Log($Log_content);
                $message = Lang('Busy network');
                return output(ERROR_CODE_SZSB_001,$message);
            }
        }
        return;
    }

    // 取消收藏
    public function removeFavorites()
    {
        $store_id = $this->request->param('store_id');
        $collection = $this->request->post('collection');// 收藏商品id
        $type = $this->request->has('type')?$this->request->post('type'):1;
        $user_id = $this->user_list['user_id'];

        $coll = explode(',', $collection);
        $collection = array();
        foreach ($coll as $k => $v)
        {
            if (!empty($v))
            {
                $collection[] = $v;
            }
        }

        $typeArr = array();
        if (!empty($collection))
        {
            if (is_array($collection))
            { // 是数组
                foreach ($collection as $key => $value)
                {
                    $typeArr[$key] = $value;
                }
            }
            else if (is_string($collection))
            { // 是字符串
                $typestr = trim($collection, ','); // 移除两侧的逗号
                $typeArr = explode(',', $typestr); // 字符串打散为数组
            }
        }
        //进入正式添加---开启事物
        //事务开启
        Db::startTrans();
        try
        {
            foreach ($typeArr as $k => $v)
            {   
                if($type == 2)
                {   
                    $r0 = UserCollectionModel::where(['store_id'=>$store_id,'id'=>$v,'user_id'=>$user_id,'type'=>2])->field('mch_id')->select()->toArray();
                }
                else
                {
                    $r0 = UserCollectionModel::where(['store_id'=>$store_id,'id'=>$v,'user_id'=>$user_id])->field('mch_id')->select()->toArray();
                }
                if ($r0)
                {
                    $mch_id = $r0[0]['mch_id'];
                    if ($mch_id != 0)
                    {
                        $MchModel = MchModel::where('id',$mch_id)->where('store_id',$store_id)->find();
                        $MchModel->collection_num     = 'collection_num - 1';
                        $MchModel->save();
                    }
                    if($type ==2)
                    {
                        UserCollectionModel::where(['store_id'=>$store_id,'id'=>$v,'user_id'=>$user_id,'type'=>2])->delete();
                    }
                    else
                    {
                        UserCollectionModel::where(['store_id'=>$store_id,'id'=>$v,'user_id'=>$user_id])->delete();
                    }
                    
                }
            }

            Db::commit();
            $message = Lang('favorites.1');
            return output(200,$message);   
        }
        catch (\Exception $e) 
        {
            // 回滚事务
            Db::rollback();
            $Log_content = $e->getMessage();
            $this->Log($Log_content);
            $message = Lang('Busy network');
            return output(ERROR_CODE_QXSZ,$message);
        }
        return;
    }

    // 找相似
    public function similar()
    {
        $store_id = $this->request->post('store_id');
        $access_id = $this->request->post('access_id');
        $pro_id = $this->request->post('pro_id'); // 商品id
        $list = array();
        $list_list = array();
        $id_list = array();
        
        // 获取会员
        $Member_discount = array('store_id'=>$store_id,'access_id'=>$access_id);
        $grade_list= PC_Tools::Member_discount($Member_discount);
        $grade = $grade_list['grade'];
        $grade_rate = $grade_list['grade_rate'];

        $sql_t = "select a.id,a.product_title,a.product_class,a.brand_id,a.imgurl,(a.volume + a.virtual_sales) as volume,a.keyword,a.mch_id,min(c.price) over (partition by c.pid) as price,c.yprice from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.id = '$pro_id' and a.mch_id != 0 ";
        $r_t = Db::query($sql_t);
        if ($r_t)
        {
            $mch_id = $r_t[0]['mch_id'];
            $keyword = $r_t[0]['keyword']; // 品牌id
            $keyword = explode(',', $keyword);
            $r_t[0]['imgurl'] = ServerPath::getimgpath($r_t[0]['imgurl']);

            // 根据商城ID、用户ID、店铺审核状态通过，查询是否有店铺
            $r0 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id,'recovery'=>0])->field('name')->select()->toArray();
            if ($r0)
            {
                $r_t[0]['mch_name'] = $r0[0]['name'];
            }
            else
            {
                $r_t[0]['mch_name'] = '';
            }
            foreach ($keyword as $k => $v)
            {
                $v_0 = Tools::FuzzyQueryConcatenation($v);
                $sql_t1 = "select tt.* from (select a.id,a.sort,a.add_date,a.product_title,a.imgurl,(a.volume + a.virtual_sales) as volume,a.mch_id,min(c.price) over (partition by c.pid) as price,c.yprice,row_number () over (partition by c.pid) as top from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.status in (2,3) and a.recycle = 0 and a.mch_status = 2 and a.active = 1 and a.keyword like $v_0 and a.mch_id != 0 ) as tt where tt.top<2 order by tt.sort,tt.add_date DESC LIMIT 0,20";
                $r_t1 = Db::query($sql_t1);
                if ($r_t1)
                {
                    foreach ($r_t1 as $k => $v)
                    {
                        $mch_id1 = $v['mch_id'];
                        $v['imgurl'] = ServerPath::getimgpath($v['imgurl']);

                        // 根据商城ID、用户ID、店铺审核状态通过，查询是否有店铺
                        $r0 = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id1,'recovery'=>0])->field('name')->select()->toArray();
                        if ($r0)
                        {
                            $v['mch_name'] = $r0[0]['name'];
                        }
                        else
                        {
                            $v['mch_name'] = '';
                        }
                        if($v['volume'] < 0)
                        {
                            $v['volume'] = 0;
                        }
                        if(!in_array($v['id'],$id_list))
                        {
                            $id_list[] = $v['id'];
                            $list_list[] = $v;
                        }
                    }
                }
            }

            foreach ($list_list as $k => $v)
            {
                $v['vip_yprice'] = $v['price'];
                $v['vip_yprice'] = sprintf("%.2f", round($v['price'] * $grade_rate,2));

                if (count($list) < 20)
                {
                    $list[] = $v;
                }
            }

            $data = array('product' => $r_t,'list' => $list,'grade'=>$grade);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        else
        {
            $data = array('list' => $list,'grade'=>$grade);
            $message = Lang('Success');
            return output(200,$message,$data);
        }
        return;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/favorites.log",$Log_content);
        return;
    }
}
