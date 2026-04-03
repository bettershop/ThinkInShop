<?php
namespace app\admin\controller\mch\Mch;

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
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $send_id = trim($this->request->param('send_id')); // 发送人id
        $receive_id = trim($this->request->param('receive_id'));//接收客服id
        $content = $this->request->param('content');//发送内容
        $img_list = trim($this->request->param('img_list'));//发送图片
        $is_mch_send = trim($this->request->param('is_mch_send'));//是否店铺0否1是
        
        $user_id = cache($access_id.'_uid'); // 用户user_id

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
        if(empty($content) && empty($img_list))
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
            $text_list = $content;
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
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));

        $user_id = trim($this->request->param('userId')); // 用户id
        $mch_id = trim($this->request->param('mchId'));//店铺id
        $type = trim($this->request->param('type'));//0用户端1店铺端

        if($type)
        {
            $res = MchModel::where(['store_id'=>$store_id,'id'=>$mch_id])->field('logo,name,head_img')->select()->toArray();
            $headimgurl = ServerPath::getimgpath($res[0]['head_img'],$store_id);
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
        $store_id = addslashes($this->request->param('storeId'));
        $store_type = addslashes($this->request->param('storeType'));
        $access_id = addslashes($this->request->param('accessId'));
        $mch_id = trim($this->request->param('mchId'));
        $userName = trim($this->request->param('userName'));
        
        $list0 = array();
        $list = array();

        $content = " a.receive_id = '$mch_id' ";
        if($userName != '')
        {
            $userName_0 = Tools::FuzzyQueryConcatenation($userName);
            $content .= " and b.user_name like $userName_0 ";
        }
        $sql = "select tt.* from (select a.*,b.headimgurl,b.user_name,row_number () over (PARTITION BY a.send_id) AS top from lkt_online_message as a left join lkt_user as b on a.send_id = b.user_id where $content ) as tt where top <2 ";
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
                $list[$key]['headimgurl'] = $value['headimgurl'];
                $list[$key]['user_name'] = $value['user_name'];
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
}

?>