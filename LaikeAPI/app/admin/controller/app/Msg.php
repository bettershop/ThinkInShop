<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Cache;
use app\common\Tools;
use app\common\ServerPath;

use app\admin\model\UserModel;
use app\admin\model\MchModel;
/**
 * 功能：客服类
 * 修改人：PJY
 */
class Msg extends BaseController
{
    //发送消息
    public function addMessage()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));

        $send_id = trim($this->request->param('send_id')); // 发送人id
        $receive_id = trim($this->request->param('receive_id'));//接收客服id
        $content = $this->request->param('content');//发送内容
        $img_list = trim($this->request->param('img_list'));//发送图片
        $orderId = trim($this->request->param('orderId')); // 订单ID
        $pId = trim($this->request->param('pId')); // 商品ID
        $is_mch_send = trim($this->request->param('is_mch_send'));//是否店铺0否1是
        $user_id = $this->user_list['user_id'];
        $headimgurl = $this->user_list['headimgurl'];
        $name = $this->user_list['user_name'];

        if($is_mch_send)
        {
            $res = MchModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('logo,name,head_img')->select()->toArray();
            $headimgurl = ServerPath::getimgpath($res[0]['head_img'],$store_id);
            $name = $res[0]['name'];
        }
        if(empty($send_id) || empty($receive_id))
        {
            ob_clean();
            $message = Lang('Parameter error');
            return output(110,$message);
        }
        if(empty($content) && empty($img_list) && empty($orderId) && empty($pId))
        {
            ob_clean();
            $message = Lang('Parameter error');
            return output(111,$message);
        }
        
        Db::startTrans();
        if($img_list)
        {
            $img_arr = explode(',',$img_list);
            foreach ($img_arr as $key => $value) 
            {
                //添加消息记录
                $sql = "insert into lkt_online_message (store_id,send_id,receive_id,content,is_mch_send,add_date) values ('$store_id','$send_id','$receive_id','$value','$is_mch_send',CURRENT_TIMESTAMP)";
                $res = Db::execute($sql);
                if($res == -1)
                {
                    Db::rollback();
                    ob_clean();
                    $message = Lang('operation failed');
                    return output(112,$message);
                }
                //推送消息
                //组装数据
                $data[$key] = array('add_date'=>date('Y-m-d H:i:s'),'content'=>$value,'is_mch_send'=>$is_mch_send,'receive_id'=>$receive_id,'send_id'=>$send_id,'img'=>$headimgurl,'nike_name'=>$name,'type'=>'message');
            }
        }
        else
        {
            $order_list = array();
            $pro_list = array();
            $text_list = '';
            if($orderId != '' && $orderId != 0)
            {
                $sql_o = "select a.id as orderId,a.num,a.sNo as orderNo,a.add_time as addTime,a.z_price,a.old_total,b.p_name as orderName,c.img as imgUrl from lkt_order as a left join lkt_order_details as b on a.sNo = b.r_sNo left join lkt_configure as c on b.sid = c.id where a.id = '$orderId' ";
                $r_o = Db::query($sql_o);
                if($r_o)
                {
                    if($r_o[0]['old_total'] == '0.00' || $r_o[0]['old_total'] == '')
                    {
                        $r_o[0]['old_total'] = (float)$r_o[0]['z_price'];
                    }
                    $r_o[0]['imgUrl'] = ServerPath::getimgpath($r_o[0]['imgUrl'], $store_id);

                    $order_list = array('addTime'=>$r_o[0]['addTime'],'imgUrl'=>$r_o[0]['imgUrl'],'num'=>$r_o[0]['num'],'orderId'=>$r_o[0]['orderId'],'orderName'=>$r_o[0]['orderName'],'orderNo'=>$r_o[0]['orderNo'],'price'=>$r_o[0]['old_total']);
                }

            }
            else if($pId != '' && $pId != 0)
            {
                $sql_p = "select tt.* from (select a.product_title,a.cover_map,a.imgurl,min(c.price) over (partition by c.pid) as price,row_number () over (partition by c.pid) as top from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.id = '$pId') as tt where tt.top<2 ";
                $r_p = Db::query($sql_p);
                if($r_p)
                {
                    $r_p[0]['cover_map'] = ServerPath::getimgpath($r_p[0]['cover_map'], $store_id);
                    $r_p[0]['imgurl'] = ServerPath::getimgpath($r_p[0]['imgurl'], $store_id);
                    $r_p[0]['payPeople'] = 0;

                    $sql_3 = "select count(DISTINCT r_sNo) as total from lkt_order_details where r_status != 0 and p_id = '$pId' ";
                    $r_3 = Db::query($sql_3);
                    if($r_3)
                    {
                        $r_p[0]['payPeople'] = $r_3[0]['total'];
                    }
                    
                    $pro_list = array('id'=>$pId,'product_title'=>$r_p[0]['product_title'],'cover_map'=>$r_p[0]['cover_map'],'imgurl'=>$r_p[0]['imgurl'],'price'=>$r_p[0]['price'],'payPeople'=>$r_p[0]['payPeople']);
                }
            }
            else
            {
                $text_list = $content;
            }
            $content = json_encode(array('order'=>$order_list,'pro'=>$pro_list,'text'=>$text_list),JSON_UNESCAPED_UNICODE);
            //添加消息记录
            $sql = "insert into lkt_online_message (store_id,send_id,receive_id,content,is_mch_send,add_date) values ('$store_id','$send_id','$receive_id','$content','$is_mch_send',CURRENT_TIMESTAMP)";
            $res = Db::execute($sql);
            if($res == -1)
            {
                Db::rollback();
                ob_clean();
                $message = Lang('operation failed');
                return output(112,$message);
            }
            //推送消息
            $data[] = array('add_date'=>date('Y-m-d H:i:s'),'content'=>$content,'is_mch_send'=>$is_mch_send,'receive_id'=>$receive_id,'send_id'=>$send_id,'img'=>$headimgurl,'nike_name'=>$name,'type'=>'message');
        }
        Db::commit();
        ob_clean();
        $message = Lang('Success');
        return output(200,$message,$data);
    }

    //获取历史记录
    public function getMessageList()
    {
        $store_id = trim($this->request->param('store_id'));
        $access_id = trim($this->request->param('access_id'));

        $user_id = trim($this->request->param('userId')); // 用户id
        $mch_id = trim($this->request->param('mchId'));//店铺id
        $type = trim($this->request->param('type'));//0用户端1店铺端

        $headimgurl = $this->user_list['headimgurl'];
        $name = $this->user_list['user_name'];

        if($type)
        {
            $res = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('logo,name')->select()->toArray();
            $headimgurl = ServerPath::getimgpath($res[0]['logo'],$store_id);
            $name = $res[0]['name'];
        }
   
        $list = array();
        $sql = "select e.* from (select * from lkt_online_message where store_id = '$store_id' and send_id = '$user_id' and receive_id = '$mch_id' 
            UNION
            select * from lkt_online_message where store_id = '$store_id' and send_id = '$mch_id' and receive_id = '$user_id') as e order by e.add_date asc ";
        $res = Db::query($sql);
        if($res)
        {   
            foreach ($res as $key => $value) 
            {   
                $id = $value['id'];
                if($value['is_read'] == 0 && $value['is_mch_send'] == 1 && $type == 0)
                {
                    $sql_u = "update lkt_online_message set is_read = 1 where id = '$id' ";
                    $res_u = Db::execute($sql_u);
                }
                elseif($value['is_mch_send'] == 0 && $type == 1 && $value['is_read'] == 0)
                {   
                    $sql_m = "update lkt_online_message set is_read = 1 where id = '$id' ";
                    $res_m = Db::execute($sql_m);                    
                }
                if($value['is_mch_send'] == 1)
                {
                    $res[$key]['img'] = '';
                    //獲取供應商頭像
                    $res_s = MchModel::where(['store_id'=>$store_id,'id'=>$value['send_id']])->field('logo')->select()->toArray();
                    if($res_s)
                    {
                        $res[$key]['img'] = ServerPath::getimgpath($res_s[0]['logo'],$store_id);
                    }
                }
                else
                {
                    $res[$key]['img'] = '';
                    //獲取供應商頭像
                    $res_s = UserModel::where(['store_id'=>$store_id,'user_id'=>$value['send_id']])->field('headimgurl')->select()->toArray();
                    if($res_s)
                    {
                        $res[$key]['img'] = $res_s[0]['headimgurl'];
                    }
                }
            }
            $list = $res;
        }

        ob_clean();
        $message = Lang('Success');
        if($type)
        {
            $data = array('list'=>$list,'userImg'=>$headimgurl,'userId'=>$user_id,'is_mch_send'=>$type,'mchId'=>$mch_id,'send_id'=>$mch_id,'receive_id'=>$user_id);
        }
        else
        {
            $data = array('list'=>$list,'userImg'=>$headimgurl,'userId'=>$user_id,'is_mch_send'=>$type,'mchId'=>$mch_id,'send_id'=>$user_id,'receive_id'=>$mch_id);
        }
        
        return output(200,$message,$data);
    }

    //获取对话列表
    public function mch_userList()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $mch_id = trim($this->request->param('mchId'));

        $list0 = array();
        $list = array();
        $sql = "select tt.* from (select *,row_number () over (PARTITION BY send_id) AS top from lkt_online_message where receive_id = '$mch_id') as tt where top <2 ";
        $res = Db::query($sql);
        $is_mch_send = false;
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $user_id = $value['send_id'];
                $mchId = $value['receive_id'];

                $list[$key]['user_id'] = $user_id;
                $list[$key]['mch_id'] = $mchId;
                if($mch_id == $mchId)
                {
                    $is_mch_send = true;
                }
                //店铺名称
                $mchName = Db::name('mch')->where('id', $mchId)->value('name');
                $list[$key]['name'] = $mchName;
                //获取用户信息
                $res_u = UserModel::where(['store_id'=>$store_id,'user_id'=>$user_id])->field('headimgurl,user_name')->select()->toArray();
                if($res_u)
                {
                    $list[$key]['headimgurl'] = $res_u[0]['headimgurl'];
                    $list[$key]['user_name'] = $res_u[0]['user_name'];
                }
                //获取在线状态
                $KEY = $user_id.'_0';
                $client = cache($KEY); 
                if($client)
                {
                    $list[$key]['is_online'] = 1;
                }
                else
                {
                    $list[$key]['is_online'] = 0;
                }
                $list[$key]['receive_id'] = $mch_id;
                $list[$key]['no_read_num'] = 0;
                //获取未读数量（用户发送的）
                $sql_num = "select ifnull(count(id),0) as num from lkt_online_message where receive_id = '$mch_id' and send_id = '$user_id' and is_mch_send = 0 and is_read = '0' ";
                $res_num = Db::query($sql_num);
                if($res_num)
                {
                    $list[$key]['no_read_num'] = $res_num[0]['num'];
                }

                $sql_u = "select id from lkt_user where user_id = '$user_id' ";
                $r_u = Db::query($sql_u);
                if($r_u)
                {
                    $list0[] = $list[$key];
                }
            }
        }
        ob_clean();
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message,'data'=>array('list'=>$list0)));
        exit;       
    }

    public function user_mchList()
    {
        $store_id = trim($this->request->param('store_id'));
        $store_type = trim($this->request->param('store_type'));
        $mch_id = trim($this->request->param('mchId'));
        $user_id = trim($this->request->param('userId'));
        $mch_name = trim($this->request->param('mchName'));

        $condition = " store_id = '$store_id' ";
        if($user_id != '')
        {
            $condition .= " and send_id = '$user_id' ";
        }
        $orderBy = '';
        if($mch_id != '')
        {
            $orderBy = " order by (CASE WHEN receive_id = '$mch_id' THEN 1 ELSE 2 END) ";
        }
        if($mch_name != '')
        {
            $mch_name_0 = Tools::FuzzyQueryConcatenation($mch_name);
            $condition .= " and EXISTS (SELECT 1 FROM lkt_mch WHERE id = lkt_online_message.receive_id and name like $mch_name_0 ) ";
        }

        $list = array();
        $is_mch_send = false;
        $sql = "select tt.* from (select *,row_number () over (PARTITION BY receive_id) AS top from lkt_online_message where $condition $orderBy ) as tt where top <2 ";
        $res = Db::query($sql);
        if($res)
        {
            foreach ($res as $key => $value) 
            {
                $userId = $value['send_id'];
                $mchId = $value['receive_id'];
                if($mch_id == $mchId)
                {
                    $is_mch_send = true;
                }
                $list[$key]['user_id'] = $userId;
                $list[$key]['mch_id'] = $mchId;
                $list[$key]['addTime'] = date('i:s',strtotime($value['add_date']));
                $list[$key]['content'] = $value['content'];
                //获取用户信息
                $res_u = UserModel::where(['store_id'=>$store_id,'user_id'=>$userId])->field('headimgurl,user_name')->select()->toArray();
                //店铺名称
                $mchName = Db::name('mch')->where('id', $mchId)->value('name');
                $headImg = Db::name('mch')->where('id', $mchId)->value('head_img');
                $list[$key]['name'] = $mchName;
                $list[$key]['headimgurl'] = ServerPath::getimgpath($headImg,$store_id);
                if($res_u)
                {
                    $list[$key]['user_name'] = $res_u[0]['user_name'];
                }
                //获取在线状态
                $KEY = $userId.'_0';
                $client = cache($KEY); 
                if($client)
                {
                    $list[$key]['is_online'] = 1;
                }
                else
                {
                    $list[$key]['is_online'] = 0;
                }
                $list[$key]['receive_id'] = $mchId;
                $list[$key]['no_read_num'] = 0;
                //获取未读数量（店铺发送的）
                $sql_num = "select ifnull(count(id),0) as num from lkt_online_message where receive_id = '$mchId' and send_id = '$userId' and is_mch_send = 1 and is_read = '0' ";
                $res_num = Db::query($sql_num);
                if($res_num)
                {
                    $list[$key]['no_read_num'] = $res_num[0]['num'];
                }
            }
        }
        if($mch_id && !$is_mch_send)
        {
            $res_m = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('head_img,name')->select()->toArray();
            $headImg = ServerPath::getimgpath($res_m[0]['head_img'],$store_id);
            $noList = array(0=>array('headimgurl'=>$headImg,'is_online'=>0,'mch_id'=>$mch_id,'name'=>$res_m[0]['name'],'no_read_num'=>0,'receive_id'=>$mch_id,'user_id'=>$user_id,'user_name'=>$user_id));
            $list = array_merge($noList,$list);
        }
        ob_clean();
        $message = Lang('Success');
        echo json_encode(array('code' => 200, 'message' => $message,'data'=>array('list'=>$list)));
        exit;       
    }

}

?>