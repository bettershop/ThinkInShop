<?php
namespace app\common\Plugin;
use think\facade\Db;
use app\common\LaiKeLogUtils;
use app\common\ServerPath;
use app\common\ExcelUtils;
use app\common\Tools;
use app\common\PC_Tools;
use app\common\Jurisdiction;

use app\admin\model\CouponConfigModel;
use app\admin\model\CouponActivityModel;
use app\admin\model\CouponModel;
use app\admin\model\ProductListModel;
use app\admin\model\OrderModel;
use app\admin\model\CouponSnoModel;
use app\admin\model\MchModel;
use app\admin\model\ProductClassModel;
use app\admin\model\BrandClassModel;
use app\admin\model\UserModel;
use app\admin\model\ConfigureModel;
use app\admin\model\AdminModel;

class BbsPublicMethod
{
    // 获取互动
    public static function getInteraction($store_id,$id,$user_id,$action_type)
    {
        $time = date("Y-m-d H:i:s");

        $is_status = 0; // 没有互动
        if($action_type == 4)
        {
            $sql = "select id from lkt_bbs_post_action where post_id = '$id' and recycle = 0 and action_type = '$action_type' and forum_id = '$user_id' ";
        }
        else
        {
            $sql = "select id from lkt_bbs_post_action where post_id = '$id' and recycle = 0 and action_type = '$action_type' and user_id = '$user_id' ";
        }
        $r = Db::query($sql);
        if($r)
        {
            $is_status = 1; // 有互动
        }

        return $is_status;
    }

    // 获取互动数量
    public static function ObtainInteractionCount($user_id,$action_type)
    {
        $total = 0;
        $sql = "select count(id) as total from lkt_bbs_post_action where recycle = 0 and action_type = '$action_type' and user_id = '$user_id' ";
        $r = Db::query($sql);
        if($r)
        {
            $total = $r[0]['total'];
        }

        return $total;
    }

    // 获取粉丝数
    public static function ObtainTheNumberOfFans($user_id,$action_type)
    {
        $total = 0;
        $sql = "select count(id) as total from lkt_bbs_post_action where recycle = 0 and action_type = '$action_type' and be_user_id = '$user_id' ";
        $r = Db::query($sql);
        if($r)
        {
            $total = $r[0]['total'];
        }

        return $total;
    }

    // 互动
    public static function interaction($store_id,$id,$user_id,$action_type)
    {
        $time = date("Y-m-d H:i:s");
        $res = 0; // 0.互动 1.取消

        $forum_id = 0;
        $sql0 = "select forum_id from lkt_bbs_post where id = '$id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $forum_id = $r0[0]['forum_id']; // 种草官ID
        }

        if($action_type == 3)
        {
            $sql2 = array('post_id'=>$id,'forum_id'=>0,'user_id'=>$user_id,'action_type'=>$action_type,'create_time'=>$time);
            $r2 = Db::name('bbs_post_action')->insertGetId($sql2);

            $sql_update = array('forward_num'=>Db::raw("forward_num+1"));
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $r = Db::name('bbs_post')->where($sql_where)->update($sql_update);

            $sql_where_f = array('id'=>$forum_id);
            $r_f = Db::name('bbs_forum')->where($sql_where_f)->update($sql_update);
        }
        else
        {
            if($action_type == 1)
            {
                $update_str = "like_num";
            }
            else if($action_type == 2)
            {
                $update_str = "collect_num";
            }
            else if($action_type == 4)
            {
                $update_str = "follow_num";
            }

            $sql1 = "select id from lkt_bbs_post_action where post_id = '$id' and forum_id = 0 and user_id = '$user_id' and action_type = '$action_type' and recycle = 0 ";
            $r1 = Db::query($sql1);
            if($r1)
            {
                $sql_update2 = array('recycle'=>1);
                $sql_where2 = array('post_id'=>$id,'forum_id'=>0,'user_id'=>$user_id,'action_type'=>$action_type);
                $r2 = Db::name('bbs_post_action')->where($sql_where2)->update($sql_update2);

                $sql_update = array($update_str=>Db::raw("$update_str-1"));
                $res = 1;
            }
            else
            {
                $sql2 = array('post_id'=>$id,'forum_id'=>0,'user_id'=>$user_id,'action_type'=>$action_type,'create_time'=>$time);
                $r2 = Db::name('bbs_post_action')->insertGetId($sql2);

                $sql_update = array($update_str=>Db::raw("$update_str+1"));
            }
            $sql_where = array('store_id'=>$store_id,'id'=>$id);
            $r = Db::name('bbs_post')->where($sql_where)->update($sql_update);

            $sql_where_f = array('id'=>$forum_id);
            $r_f = Db::name('bbs_forum')->where($sql_where_f)->update($sql_update);
        }

        return $res;
    }

    // 日志
    public function Log($Log_content)
    {
        $lktlog = new LaiKeLogUtils();
        $lktlog->log("app/bbs.log",$Log_content);
        return;
    }
}
