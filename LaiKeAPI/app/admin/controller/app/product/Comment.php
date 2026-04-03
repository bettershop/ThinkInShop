<?php
namespace app\admin\controller\app\product;

use think\facade\Db;
use think\facade\Request;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;

use app\admin\model\ProductListModel;
use app\admin\model\CommentsImgModel;
use app\admin\model\ReplyCommentsModel;
use app\admin\model\ConfigureModel;
use app\admin\model\CommentsModel;
use app\admin\model\OrderDetailsModel;
use app\admin\model\UserModel;

class Comment
{
    // 评价列表
    public function getcomment()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id
        $pid = addslashes(trim(Request::param('pid'))); // 商品id
        $type = addslashes(trim(Request::param('type'))); // 类型
        $page = addslashes(trim(Request::param('page'))); // 页码

        $start = 0;
        $end = 10;
        if($page)
        {
            $start = ($page - 1) * $end;
        }

        $mch_id = 0;
        $comments_total = 0; // 全部评论
        $comments_hao = 0; // 好评
        $comments_zhong = 0; // 中评
        $comments_cha = 0; // 差评
        $comments_image = 0; // 有图
        $comments = array();
        if ($pid)
        {
            $r_p = ProductListModel::where(['store_id'=>$store_id,'id'=>$pid])->field('mch_id')->select()->toArray();
            if($r_p)
            {
                $mch_id = $r_p[0]['mch_id'];
            }

            $r_0 = CommentsModel::where(['store_id'=>$store_id,'pid'=>$pid])->where('type','<>','KJ')->order('add_time','desc')->field('id,CommentType')->select()->toArray();
            if($r_0)
            {
                $comments_total = count($r_0);
                foreach ($r_0 as $k_0 => $v_0)
                {
                    if ($v_0['CommentType'] == 1 || $v_0['CommentType'] == 2 || $v_0['CommentType'] == 'bad')
                    {
                        $comments_cha++;
                    }
                    else if ($v_0['CommentType'] == 3 || $v_0['CommentType'] == 'NOTBAD')
                    {
                        $comments_zhong++;
                    }
                    else if ($v_0['CommentType'] == 4 || $v_0['CommentType'] == 5 || $v_0['CommentType'] == 'GOOD')
                    {
                        $comments_hao++;
                    }
                    $r_1 = CommentsImgModel::where('comments_id',$v_0['id'])->field('id')->select()->toArray();
                    if ($r_1)
                    {
                        $comments_image++;
                    }
                }
            }

            $str = "a.id,a.pid,a.attribute_id,a.content,a.CommentType,a.anonymous,a.add_time,a.review,a.review_time,a.order_detail_id,m.user_name,m.headimgurl";
            if ($type == 0)
            {
                $sql_c = "select $str from lkt_comments AS a LEFT JOIN lkt_user AS m ON a.uid = m.user_id and a.store_id = m.store_id where a.store_id = '$store_id' and a.pid = '$pid' order by add_time desc limit $start,$end";
            }
            else if ($type == 1)
            {
                $sql_c = "select $str from lkt_comments AS a LEFT JOIN lkt_user AS m ON a.uid = m.user_id and a.store_id = m.store_id where a.store_id = '$store_id' and a.pid = '$pid' and CommentType = 4 or a.pid = '$pid' and CommentType = 5 or a.pid = '$pid' and CommentType='GOOD' order by add_time desc limit $start,$end";
            }
            else if ($type == 2)
            {
                $sql_c = "select $str from lkt_comments AS a LEFT JOIN lkt_user AS m ON a.uid = m.user_id and a.store_id = m.store_id where a.store_id = '$store_id' and a.pid = '$pid' and CommentType = 3 or a.pid = '$pid' and CommentType='NOTBAD' order by add_time desc limit $start,$end";
            }
            else if ($type == 3)
            {
                $sql_c = "select $str from lkt_comments AS a LEFT JOIN lkt_user AS m ON a.uid = m.user_id and a.store_id = m.store_id where a.store_id = '$store_id' and a.pid = '$pid' and CommentType = 1 or a.pid = '$pid' and CommentType = 2 or a.pid = '$pid' and CommentType='bad' order by add_time desc limit $start,$end";
            }
            else if ($type == 4)
            {
                $sql_c = "select $str from lkt_comments AS a LEFT JOIN lkt_user AS m ON a.uid = m.user_id and a.store_id = m.store_id where a.store_id = '$store_id' and a.pid = '$pid' and (a.id in (select comments_id from lkt_comments_img where comments_id = a.id)) order by add_time desc limit $start,$end";
            }
            $r_c = Db::query($sql_c);
            if ($r_c)
            {
                foreach ($r_c as $k_c => $v_c)
                {
                    $add_time = $v_c['add_time']; // 评论时间
                    $review_time = $v_c['review_time']; // 追评时间
                    $order_detail_id = $v_c['order_detail_id']; // 订单详情ID
                    $attribute_id = $v_c['attribute_id'];
                    $attribute_str = '';
                    $v_c['mch_id'] = $mch_id;

                    $r_shu = ConfigureModel::where(['pid'=>$pid,'id'=>$attribute_id,'recycle'=>0])->field('attribute')->select()->toArray();
                    if ($r_shu)
                    {
                        $attribute = unserialize($r_shu[0]['attribute']);
                        foreach ($attribute as $k => $v)
                        {
                            if (strpos($k, '_LKT_') !== false)
                            {
                                $k = substr($k, 0, strrpos($k, "_LKT"));
                                $v = substr($v, 0, strrpos($v, "_LKT"));
                            }
                            $attribute_str .= $k . ":" . $v . ',';
                        }
                        $attribute_str = rtrim($attribute_str, ',');
                    }
                    $v_c['attribute_str'] = $attribute_str;

                    $v_c['time'] = substr($v_c['add_time'], 0, 10); // 评论时间

                    $comments_id = $v_c['id']; // 评论id

                    $v_c['review_day'] = 0;
                    if($review_time != '0000-00-00 00:00:00')
                    {
                        $time0 = strtotime($review_time) - strtotime($add_time);
                        $v_c['review_day'] = abs(round($time0 / 86400));
                    }

                    $v_c['images'] = array();
                    // 根据评论id,查询评论图片表
                    $comment_res = CommentsImgModel::where(['comments_id'=>$comments_id,'type'=>0])->field('comments_url')->select()->toArray();
                    if ($comment_res)
                    {
                        $array_c = array();
                        foreach ($comment_res as $kc => $vc)
                        {
                            $url = $vc['comments_url']; // 评论图片
                            $array_c[$kc] = array('url' => ServerPath::getimgpath($url));
                        }
                        $v_c['images'] = $array_c;
                    }

                    $v_c['review_images'] = array();
                    // 根据评论id,查询追评评论图片表
                    $comment_res1 = CommentsImgModel::where(['comments_id'=>$comments_id,'type'=>1])->field('comments_url')->select()->toArray();
                    if ($comment_res1)
                    {
                        $array_c1 = array();
                        foreach ($comment_res1 as $kc1 => $vc1)
                        {
                            $url = $vc1['comments_url']; // 评论图片
                            $array_c1[$kc1] = array('url' => ServerPath::getimgpath($url));
                        }
                        $v_c['review_images'] = $array_c1;
                    }

                    if ($v_c['anonymous'] == 1)
                    {
                        $v_c['user_name'] = Lang('anonymous');
                        $v_c['headimgurl'] = '';
                    }

                    $reply_admin = '';
                    // 根据评论ID，查询回复评论表
                    $ad_res = ReplyCommentsModel::where(['store_id'=>$store_id,'cid'=>$comments_id,'type'=>1])->field('content')->select()->toArray();
                    if ($ad_res)
                    {
                        $reply_admin = $ad_res[0]['content'];
                    }
                    $v_c['replyAdmin'] = $reply_admin;

                    $replyNum = 0;
                    // 根据评论ID，查询回复评论表
                    $ad_res = ReplyCommentsModel::where(['store_id'=>$store_id,'cid'=>$comments_id,'type'=>0])->field('count(id) as total')->select()->toArray();
                    if ($ad_res)
                    {
                        $replyNum = $ad_res[0]['total'];
                    }
                    $v_c['replyNum'] = $replyNum;

                    $arrive_time = ''; // 到货时间
                    $r_d = OrderDetailsModel::where(['id'=>$order_detail_id])->field('arrive_time')->select()->toArray();
                    if ($r_d)
                    {
                        $arrive_time = $r_d[0]['arrive_time']; // 到货时间
                    }
                    $v_c['arrive_time'] = $arrive_time; // 到货时间
                    
                    if($v_c['CommentType'] == 1 || $v_c['CommentType'] == 2)
                    {
                        $v_c['plType'] = 1;
                    }
                    else if($v_c['CommentType'] == 4 || $v_c['CommentType'] == 5)
                    {
                        $v_c['plType'] = 5;
                    }
                    else
                    {
                        $v_c['plType'] = 3;
                    }
                    $comments[] = $v_c;
                }
            }

            $datas = array('comments_total' => $comments_total, 'comments_hao' => $comments_hao, 'comments_zhong' => $comments_zhong, 'comments_cha' => $comments_cha, 'comments_image' => $comments_image,"data" => $comments);

            $message = Lang('Success');
            return output(200,$message, $datas);
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . '商品ID错误!pid:' . $pid;
            $this->Log($Log_content);
            ob_clean();
            $message = Lang('Parameter error');
            return output(109,$message);
        }
    }

    // 获取回复数据
    public function getCommentReplyList()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id

        $commentId = addslashes(trim(Request::param('commentId'))); // 评论id
        $page = addslashes(trim(Request::param('pageNo'))); // 页码
        $pagesize = addslashes(trim(Request::param('pageSize'))); // 每页多少条数据
        $pagesize = $pagesize ? $pagesize : 10;

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $total = 0;
        $list = array();
        $sql0 = "select count(a.id) as total from lkt_reply_comments as a left join lkt_user as b on a.uid = b.user_id where a.store_id = '$store_id' and a.cid = '$commentId' and a.type = 0 ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $str = "a.id,a.cid,a.content,a.add_time,b.headimgurl,b.user_name as replyName,b.user_id as replyUserId";

        $sql1 = "select $str from lkt_reply_comments as a left join lkt_user as b on a.uid = b.user_id where a.store_id = '$store_id' and a.cid = '$commentId' and a.type = 0 order by a.add_time desc limit $start,$pagesize ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k => $v)
            {
                $v['addTime'] = date('Y-m-d',strtotime($v['add_time']));
                $v['replyHeadimgurl'] = $v['headimgurl'];
                $list[] = $v;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang('Success');
        return output(200,$message, $data);
    }
    
    // 获取评论数据
    public function getCommentDetail()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id

        $commentId = addslashes(trim(Request::param('commentId'))); // 评论id
        
        $commentImgList = array();
        $detailInfo = array();
        // 根据评论id,查询评论图片表
        $comment_res = CommentsImgModel::where(['comments_id'=>$commentId,'type'=>0])->field('comments_url')->select()->toArray();
        if ($comment_res)
        {
            $array_c = array();
            foreach ($comment_res as $kc => $vc)
            {
                $url = $vc['comments_url']; // 评论图片
                $array_c[$kc] = ServerPath::getimgpath($url);
            }
            $commentImgList = $array_c;
        }

        $str = "a.id,a.pid as goodsId,a.content,a.add_time,a.review_time,a.review,p.product_title,p.mch_id,p.imgurl,d.arrive_time,d.size,u.headimgurl,u.user_name,a.anonymous";

        $sql0 = "select $str from lkt_comments as a left join lkt_product_list as p on a.pid = p.id left join lkt_order_details as d on a.order_detail_id = d.id left join lkt_user as u on a.uid = u.user_id where a.store_id = '$store_id' and a.id = '$commentId' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $add_time = $r0[0]['add_time']; // 评论时间
            $review_time = $r0[0]['review_time']; // 追评时间
            $r0[0]['imgurl'] = ServerPath::getimgpath($r0[0]['imgurl']);
            $r0[0]['addTime'] = date('Y-m-d',strtotime($r0[0]['add_time']));

            $reply_admin = '';
            // 根据评论ID，查询回复评论表
            $ad_res = ReplyCommentsModel::where(['store_id'=>$store_id,'cid'=>$commentId,'type'=>1])->field('content')->select()->toArray();
            if ($ad_res)
            {
                $reply_admin = $ad_res[0]['content'];
            }
            $r0[0]['replyAdmin'] = $reply_admin;

            $r0[0]['reviewDay'] = 0;
            if($review_time != '0000-00-00 00:00:00')
            {
                $r0[0]['reviewDay'] = strtotime($add_time) - strtotime($review_time);
            }

            if ($r0[0]['anonymous'] == 1)
            {
                $r0[0]['user_name'] = Lang('anonymous');
                $r0[0]['headimgurl'] = '';
            }

            $r0[0]['replyImgList'] = array();
            // 根据评论id,查询追评评论图片表
            $comment_res1 = CommentsImgModel::where(['comments_id'=>$commentId,'type'=>1])->field('comments_url')->select()->toArray();
            if ($comment_res1)
            {
                $array_c1 = array();
                foreach ($comment_res1 as $kc1 => $vc1)
                {
                    $url = $vc1['comments_url']; // 评论图片
                    $array_c1[$kc1] = array('url' => ServerPath::getimgpath($url));
                }
                $r0[0]['replyImgList'] = $array_c1;
            }
            
            $detailInfo = $r0[0];
        }

        $data = array('commentImgList'=>$commentImgList,'detailInfo'=>$detailInfo);
        $message = Lang('Success');
        return output(200,$message, $data);
    }
    
    // 回复
    public function sendComment()
    {
        $store_id = addslashes(trim(Request::param('store_id')));
        $store_type = addslashes(trim(Request::param('store_type'))); // 来源
        $access_id = addslashes(trim(Request::param('access_id'))); // 授权id

        $commentId = addslashes(trim(Request::param('commentId'))); // 评论id
        $text = addslashes(trim(Request::param('text'))); // 回复内容
        
        $time = date('Y-m-d H:i:s');
        // 根据授权id,查询用户信息(被邀请人)
        $r_user = UserModel::where(['store_id'=>$store_id,'access_id'=>$access_id])->select()->toArray();
        if ($r_user)
        { // 是会员
            $user_id = $r_user[0]['user_id']; // 用户id
        }
        else
        {
            $message = Lang('Please log in');
            return output(203,$message);
        }

        if($text == '')
        {
            $message = Lang('comments.1');
            return output(203,$message);
        }

        $sql_insert = array('store_id'=>$store_id,'cid'=>$commentId,'uid'=>$user_id,'content'=>$text,'add_time'=>$time);
        $r = Db::name('reply_comments')->insert($sql_insert);
        if($r > 0)
        {
            $message = Lang('Success');
            return output(200,$message);
        }
        else
        {
            $message = Lang('Busy network');
            return output(109,$message);
        }
    }
    
    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/product.log",$Log_content);
        return;
    }
}

?>

