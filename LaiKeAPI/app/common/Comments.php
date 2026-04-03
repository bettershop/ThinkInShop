<?php

namespace app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\GETUI\LaikePushTools;
use app\common\Jurisdiction;
use app\common\Tools;

use app\admin\model\CommentsImgModel;
use app\admin\model\ReplyCommentsModel;
use app\admin\model\CommentsModel;

class Comments
{
    // 获取评价数量
    public static function getCommentsCount($array)
    {
        $store_id = $array['store_id'];
        $shop_id = $array['shop_id'];
        $otype = $array['otype'];

        $proNum = 0;

        $sql0 = "select ifnull(count(1) ,0) as c_total from lkt_comments AS a LEFT JOIN lkt_product_list AS p ON a.pid = p.id and a.store_id = p.store_id left join lkt_mch as m on p.mch_id = m.id where a.store_id = '$store_id' and m.id = '$shop_id' and a.is_look = 0 and a.oid like '%$otype%'";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $proNum = $r0[0]['c_total'];
        }

        return $proNum;
    }

    // 获取评价列表
    public static function CommentsList($array)
    {
        $store_id = $array['store_id'];
        $shop_id = $array['shop_id'];
        $cid = $array['cid'];
        $type = $array['type'];
        $orderType = $array['orderType'];
        $search = $array['search'];
        $startDate = $array['startDate'];
        $endDate = $array['endDate'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $mchName = '';
        if(isset($array['mchName']))
        {
            $mchName = $array['mchName'];
        }
        $source = '';
        if(isset($array['source']))
        {
            $source = $array['source'];
        }
        
        if($orderType == 'FX')
        {
            $data = self::CList($array);
            return $data;
        }
        else
        {
            if($cid != '')
            {
                $data = self::Details($array);
                return $data;
            }
            else
            {
                $data = self::CList($array);
                return $data;
            }
        }
    }

    // 评价列表
    public static function CList($array)
    {
        $store_id = $array['store_id'];
        $shop_id = $array['shop_id'];
        $cid = $array['cid'];
        $type = $array['type'];
        $orderType = $array['orderType'];
        $search = $array['search'];
        $startDate = $array['startDate'];
        $endDate = $array['endDate'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $mchName = '';
        if(isset($array['mchName']))
        {
            $mchName = $array['mchName'];
        }
        $source = '';
        if(isset($array['source']))
        {
            $source = $array['source'];
        }
        
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $list = array();

        $condition = " where c.store_id = '$store_id' and a.r_status in (5,12) AND a.sid = c.attribute_id ";

        if($cid != '')
        {
            $condition .= " and c.id = '$cid' ";
        }
        if($source != 1)
        { // 不是管理平台
            $condition .= " and lm.id = '$shop_id'  ";
        }
        if ($type)
        {
            if ($type == 'GOOD')
            {
                $condition .= " and c.CommentType > '3' ";
            }
            else if ($type == 'NOTBAD')
            {
                $condition .= " and c.CommentType = '3' ";
            }
            else if ($type == 'BAD')
            {
                $condition .= " and c.CommentType < '3' ";
            }
            else
            {
                $condition .= " and (c.id in (select comments_id from lkt_comments_img where comments_id = c.id)) ";
            }
        }
        if($orderType != '')
        {
            if($orderType == 'GM')
            {
                $condition .= " and (o.otype = 'GM' or o.otype = 'VI') ";
            }
            else
            {
                $condition .= " and o.otype = '$orderType' ";
            }
        }

        if ($search != '')
        {
            $search = Tools::FuzzyQueryConcatenation($search);
            $condition .= " and (a.r_sNo like $search or a.p_name like $search ) ";
        }

        if ($mchName != '')
        {
            $mchName = Tools::FuzzyQueryConcatenation($mchName);
            $condition .= " and lm.name like $mchName ";
        }

        if ($startDate != '')
        {
            $condition .= " and c.add_time >= '$startDate' ";
        }

        if ($endDate != '')
        {
            $condition .= " and c.add_time <= '$endDate' ";
        }

        $total = 0;
        $sql0 = "select tt.* from (select c.id,row_number () over (PARTITION BY o.sNo) AS top
                    from lkt_order_details AS a
                    LEFT JOIN lkt_comments AS c ON a.r_sNo = c.oid
                    LEFT JOIN lkt_order as o on a.r_sNo = o.sNo
                    LEFT JOIN lkt_configure as con on a.sid = con.id
                    LEFT JOIN lkt_product_list as lpl on lpl.id = con.pid
                    LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id
                    left join lkt_user as u on c.uid = u.user_id
                    " . $condition . ") as tt where tt.top < 2";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = count($r0);
        }

        $str = "a.id as odid,a.p_name,a.p_price,a.r_sNo,a.r_status,a.size as attrSize,c.id,c.size,c.store_id,c.oid,c.uid,c.pid,c.attribute_id,c.content,c.CommentType,c.anonymous,c.add_time,c.type,o.z_price,o.otype,o.mch_id,lm.name as shop_name,u.user_name,u.headimgurl,row_number () over (PARTITION BY o.sNo) AS top ";

        $sql1 = "select tt.* from (
                    select $str
                    from lkt_order_details AS a
                    LEFT JOIN lkt_comments AS c ON a.r_sNo = c.oid
                    LEFT JOIN lkt_order as o on a.r_sNo = o.sNo
                    LEFT JOIN lkt_configure as con on a.sid = con.id
                    LEFT JOIN lkt_product_list as lpl on con.pid = lpl.id
                    LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id
                    left join lkt_user as u on c.uid = u.user_id
                    " . $condition . " order by c.add_time desc
        ) as tt where tt.top < 2 limit $start,$pagesize";
        $r = Db::query($sql1);
        if($r)
        {
            foreach ($r as $k => $v)
            {
                $v['commentsId'] = $v['id'];
                $v['order_detail_id'] = $v['odid'];
                $v['z_price'] = (float)$v['z_price'];
                $v['p_price'] = (float)$v['p_price'];

                $id = $v['id'];
                $uid = $v['uid'];
                $mch_id = $v['mch_id'];

                $v['isMain'] = true; // 查看
                if($shop_id == $mch_id)
                {
                    $v['isMain'] = false; // 修改
                }

                $commentImgList = array();
                // 根据评论ID，查询评论图片
                $r2 = CommentsImgModel::where(['comments_id'=>$id,'type'=>0])->field('comments_url')->select()->toArray();
                if($r2)
                {
                    foreach($r2 as $k2 => $v2)
                    {
                        $commentImgList[] =  ServerPath::getimgpath($v2['comments_url'],$store_id);
                    }
                }
                $v['commentImgList'] = $commentImgList;

                $r5 = ReplyCommentsModel::where(['cid'=>$id])->field('content')->select()->toArray();
                if($r5)
                {
                    $v['replyText'] = $r5[0]['content'];
                }
                $list[] = $v;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        return $data;
    }

    // 评价详情
    public static function Details($array)
    {
        $store_id = $array['store_id'];
        $shop_id = $array['shop_id'];
        $cid = $array['cid'];
        $type = $array['type'];
        $orderType = $array['orderType'];
        $search = $array['search'];
        $startDate = $array['startDate'];
        $endDate = $array['endDate'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        $mchName = '';
        if(isset($array['mchName']))
        {
            $mchName = $array['mchName'];
        }
        $source = '';
        if(isset($array['source']))
        {
            $source = $array['source'];
        }
        
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $list = array();

        $condition = " a.store_id = '$store_id' and a.id = '$cid' ";
        if ($type)
        {
            if ($type == 'GOOD')
            {
                $condition .= " and a.CommentType > '3' ";
            }
            else if ($type == 'NOTBAD')
            {
                $condition .= " and a.CommentType = '3' ";
            }
            else if ($type == 'BAD')
            {
                $condition .= " and a.CommentType < '3' ";
            }
            else
            {
                $condition .= " and (a.id in (select comments_id from lkt_comments_img where comments_id = a.id)) ";
            }
        }

        $str = " a.id,a.uid as user_id,a.pid,a.attribute_id,a.content,a.CommentType,a.anonymous,a.add_time,a.review,a.review_time,b.user_name,b.headimgurl,c.r_status as plType,lpl.mch_id,a.oid as r_sNo ";
        $sql0 = "select $str from lkt_comments as a left join lkt_user as b on a.uid = b.user_id left join lkt_order_details as c on a.order_detail_id = c.id LEFT JOIN lkt_product_list as lpl on lpl.id = a.pid where $condition ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            foreach ($r0 as $k => $v)
            {
                $v['commentsId'] = $v['id'];
                $v['cid'] = $v['id'];
                $id = $v['id'];
                $user_id = $v['user_id'];
                $v['time'] = date("Y-m-d",strtotime($v['add_time']));
                $v['plType'] = $v['CommentType'];

                $commentImgList = array();
                $images = array();
                $sql1 = "select comments_url from lkt_comments_img where comments_id = '$id' and type = 0 ";
                $r1 = Db::query($sql1);
                if($r1)
                {
                    foreach($r1 as $k1 => $v1)
                    {
                        $images[] = array('url'=>ServerPath::getimgpath($v1['comments_url'], $store_id));
                        $commentImgList[] =  ServerPath::getimgpath($v1['comments_url'],$store_id);
                    }
                }
                $v['images'] = $images;
                $v['commentImgList'] = $commentImgList;
                if($v['review'] == null)
                {
                    $v['review'] = '';
                }
                
                $review_images = array();
                $sql2 = "select comments_url from lkt_comments_img where comments_id = '$id' and type = 1 ";
                $r2 = Db::query($sql2);
                if($r2)
                {
                    foreach($r2 as $k2 => $v2)
                    {
                        $review_images[] = array('url'=>ServerPath::getimgpath($v2['comments_url'], $store_id));
                    }
                }
                $v['review_images'] = $review_images;

                $v['replyAdmin'] = '';
                $sql3 = "select uid,content from lkt_reply_comments where cid = '$id' and type = 1 ";
                $r3 = Db::query($sql3);
                if($r3)
                {
                    $replyAdmin_id = $r3[0]['uid'];
                    $v['replyAdmin'] = $r3[0]['content'];

                    // $sql4 = "select user_name from lkt_user where store_id = '$store_id' and user_id = '$replyAdmin_id' ";
                    // $r4 = Db::query($sql4);
                    // if($r4)
                    // {
                    //     $v['replyAdmin'] = $r4[0]['user_name'];
                    // }
                }

                if($v['anonymous'] == 1)
                {
                    $v['user_name'] = "匿名";
                }

                if($shop_id == $v['mch_id'])
                { // 店主查看评论详情
                    $sql4 = "update lkt_comments set is_look = 1 where id = '$id' ";
                    Db::execute($sql4);
                }

                $list[] = $v;
            }
        }

        $data = array('list'=>$list);
        return $data;
    }

    // 查看明细
    public static function getCommentsDetailInfoById($array)
    {
        $store_id = $array['store_id'];
        $cid = $array['cid'];
        $search = $array['search'];
        $startdate = $array['startdate'];
        $enddate = $array['enddate'];
        $page = $array['page'];
        $pagesize = $array['pagesize'];
        
        $start = 0;
        if ($page)
        {
            $start = ($page - 1) * $pagesize;
        }
        $list = array();

        $condition = " a.store_id = '$store_id' and a.cid = '$cid' ";
        if($search != '')
        {
            $condition .= " and a.uid = '$search' ";
        }
        if ($startdate != '')
        {
            $condition .= " and a.add_time >= '$startdate' ";
        }
        if ($enddate != '')
        {
            $condition .= " and a.add_time <= '$enddate' ";
        }
        
        $total = 0;
        $sql_num = "select ifnull(count(a.id),0) as num from lkt_reply_comments a left join lkt_user b on a.uid=b.user_id left join lkt_user c on c.user_id=a.to_uid where $condition ";
        $res_num = Db::query($sql_num);
        if($res_num)
        {
            $total = $res_num[0]['num'];
        }
        $list = array();
        if($total > 0)
        {
            $sql = "select a.id,a.uid,a.cid,a.content,b.user_name replyName,b.headimgurl,b.headimgurl replyHeadimgurl,b.user_id replyUserId,a.add_time
            from lkt_reply_comments a left join lkt_user b on a.uid=b.user_id left join lkt_user c on c.user_id=a.to_uid where $condition order by a.add_time desc limit $start,$pagesize";
            $res = Db::query($sql);
            if($res)
            {
                foreach ($res as $k => $v)
                {
                    //查不到user表用户时，找admin用户
                    if (!$v['replyUserId'] && $v['uid']) 
                    {
                        $res[$k]['replyUserId'] = $v['uid'];
                        $aName = Db::name('admin')->where('name', $v['uid'])->value('nickname');
                        $res[$k]['replyName'] = $aName;
                    }
                }
                $list = $res;
            }
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang("Success");
        echo json_encode(array('code' => 200,'message' => $message,'data' => $data));
        exit();
    }

    // 修改评论
    public static function updateCommentsDetailInfoById($array)
    {
        $Jurisdiction = new Jurisdiction();

        $store_id = $array['store_id'];
        $id = $array['id'];
        $comment_input = $array['comment_input'];
        $comment_type = $array['comment_type'];
        $review = $array['review'];
        $imgurls = $array['imgurls'];
        $review_time_images = $array['review_time_images'];
        $shop_id = $array['shop_id'];
        $operator = $array['operator'];
        $source = $array['source'];
        $operator_id = 0;
        if(isset($action['operator_id']))
        {
            $operator_id = $action['operator_id'];
        }
        
        Db::startTrans();
        Db::table('lkt_comments_img')->where('comments_id',$id)->delete();
        $res = null;
        if ($imgurls)
        {   
            $imgurls = explode(",",$imgurls);
            if (count($imgurls) <= 5)
            {
                foreach ($imgurls as $key => $value)
                {
                    $imgURL_name = preg_replace('/.*\//', '', $value);
                    $sql = new CommentsImgModel();
                    $sql->comments_url = $imgURL_name;
                    $sql->comments_id = $id;
                    $sql->add_time = date("Y-m-d H:i:s");
                    $sql->type = 0;
                    $res = $sql->save();
                }
            }
        }

        if ($review_time_images)
        {   
            $review_time_images = explode(",",$review_time_images);
            if (count($review_time_images) <= 5)
            {
                foreach ($review_time_images as $key => $value)
                {
                    $imgURL_name = preg_replace('/.*\//', '', $value);
                    $sql = new CommentsImgModel();
                    $sql->comments_url = $imgURL_name;
                    $sql->comments_id = $id;
                    $sql->add_time = date("Y-m-d H:i:s");
                    $sql->type = 1;
                    $res = $sql->save();
                }
            }
        }
        $sql = CommentsModel::where(['store_id'=>$store_id,'id'=>$id])->find();
        $sql->content = $comment_input;
        $sql->CommentType = $comment_type;
        $sql->review_time = date("Y-m-d H:i:s");
        if ($review)
        {
            $sql->review = $review;
        }
        $up = $sql->save();
        if ($up || $res)
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改评论id为' . $id . '的信息成功';
            self::Log($Log_content);
            Db::commit();
            $Jurisdiction->admin_record($store_id, $operator, '修改了评论ID：'.$id.'，的评论信息',2,$source,$shop_id,$operator_id);
            $message = Lang("Success");
            echo json_encode(array('code' => 200,'message' => $message));
            exit();
        }
        else
        {
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 修改评论id为' . $id . '的信息失败';
            self::Log($Log_content);
            Db::rollback();
            $Jurisdiction->admin_record($store_id, $operator, '修改了评论ID：'.$id.'，的评论信息失败',2,$source,$shop_id,$operator_id);
            $message = Lang("operation failed");
            echo json_encode(array('code' => ERROR_CODE_CZSB,'message' => $message));
            exit();
        }
    }

    // 回复
    public static function replyComments($array)
    {
        $Jurisdiction = new Jurisdiction();

        $admin_list = $array['admin_list'];
        $store_id = $array['store_id'];
        $id = $array['id'];
        $comment_input = $array['comment_input'];
        $shop_id = $array['shop_id'];
        $operator = $array['operator'];
        $source = $array['source'];
        $operator_id = 0;
        if(isset($action['operator_id']))
        {
            $operator_id = $action['operator_id'];
        }
        
        $sql_admin = "select b.user_id from lkt_admin as a left join lkt_mch as b on a.shop_id = b.id where a.store_id = '$store_id' and a.type = 1 and a.recycle = 0 ";
        $r_admin = Db::query($sql_admin);
        if($r_admin)
        {
            $admin_name = $r_admin[0]['user_id'];
        }
        if(isset($admin_list['user_id']) && $admin_list['user_id'])
        {
            $admin_name = $admin_list['user_id'];
        }

        $r1 = CommentsModel::where(['id'=>$id])->field('id')->select()->toArray();
        if($r1)
        {
            $sql_insert = array('store_id'=>$store_id,'cid'=>$id,'uid'=>$admin_name,'content'=>$comment_input,'add_time'=>date("Y-m-d H:i:s"),'type'=>1);
            $r_insert = Db::name('reply_comments')->insert($sql_insert);
            if ($r_insert > 0)
            {
                $Jurisdiction->admin_record($store_id, $operator, '回复了评论ID：'.$id.'，的评论信息',2,$source,$shop_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 回复评论ID为' . $id . '成功';
                self::Log($Log_content);
                $message = Lang("Success");
                echo json_encode(array('code' => 200,'message' => $message));
                exit();
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '回复了评论ID：'.$id.'，的评论信息失败',2,$source,$shop_id,$operator_id);
                $Log_content = __METHOD__ . '->' . __LINE__ . ' 回复评论ID为' . $id . '失败';
                self::Log($Log_content);
                $message = Lang("operation failed");
                echo json_encode(array('code' => ERROR_CODE_CZSB,'message' => $message));
                exit();
            }
        }
        else
        {
            $message = Lang("Parameter error");
            return output(109,$message);
        }
    }

    // 删除评论
    public static function delComments($array)
    {
        $Jurisdiction = new Jurisdiction();

        $store_id = $array['store_id'];
        $id = $array['id'];
        $shop_id = $array['shop_id'];
        $operator = $array['operator'];
        $source = $array['source'];
        $operator_id = 0;
        if(isset($action['operator_id']))
        {
            $operator_id = $action['operator_id'];
        }
        
        // 接收信息
        $sql = CommentsModel::where(['store_id'=>$store_id,'id'=>$id])->find();
        $res = $sql->delete();
        if ($res)
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了评论ID：'.$id.'，的评论信息',3,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除评论id为' . $id . '的信息成功';
            self::Log($Log_content);
            $message = Lang("Success");
            echo json_encode(array('code' => 200,'message' => $message));
            exit();
        }
        else
        {
            $Jurisdiction->admin_record($store_id, $operator, '删除了评论ID：'.$id.'，的评论信息失败',3,$source,$shop_id,$operator_id);
            $Log_content = __METHOD__ . '->' . __LINE__ . ' 删除评论id为' . $id . '的信息失败';
            self::Log($Log_content);
            $message = Lang("operation failed");
            echo json_encode(array('code' => ERROR_CODE_CZSB,'message' => $message));
            exit();
        }
    }

    // 删除评论明细
    public static function delCommentReply($array)
    {
        $Jurisdiction = new Jurisdiction();

        $store_id = $array['store_id'];
        $id = $array['id'];
        $shop_id = $array['shop_id'];
        $operator = $array['operator'];
        $source = $array['source'];
        $operator_id = 0;
        if(isset($action['operator_id']))
        {
            $operator_id = $action['operator_id'];
        }
        
        $sql = ReplyCommentsModel::find($id);
        if($sql)
        {
            $res = $sql->delete();
            if(!$res)
            {
                $Jurisdiction->admin_record($store_id, $operator, '删除了评论明细ID：'.$id.'，的评论信息失败',3,$source,$shop_id,$operator_id);
                $message = Lang("operation failed");
                return output(ERROR_CODE_CZSB,$message);
            }
            else
            {
                $Jurisdiction->admin_record($store_id, $operator, '删除了评论明细ID：'.$id.'，的评论信息',3,$source,$shop_id,$operator_id);
                $message = Lang("Success");
                echo json_encode(array('code' => 200,'message' => $message));
                exit();
            }
        }
        else
        {
            $message = Lang("operation failed");
            return output(ERROR_CODE_CZSB,$message);
        }
    }

    // 日志
    public static function Log($Log_content)
    {
        $time = date("Y-m-d");
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("common/comments.log",$Log_content);
        return;
    }
}
