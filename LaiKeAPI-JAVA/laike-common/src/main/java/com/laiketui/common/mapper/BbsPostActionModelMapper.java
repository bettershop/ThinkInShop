package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.plugin.bbs.BbsPostActionModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-09-29-10:05
 * @Description:
 */
public interface BbsPostActionModelMapper extends BaseMapper<BbsPostActionModel>
{
    /**
     * 取消点赞
     * @param id
     */
    @Update("update lkt_bbs_post_action set recycle = 1 where id = #{id}")
    void del(Long id);

    /**
     * 获取数量，根据类型
     * @param id
     * @param userId
     * @param type 1:点赞 2：收藏
     * @return
     */
    BbsPostActionModel getCountByPostId(@Param("post_id") Long id, @Param("user_id") String userId,@Param("type") Integer type);

    /**
     * 查看是否关注种草官
     * @param id
     * @param user_id
     * @param type
     * @return
     */
    @Select("SELECT * FROM lkt_bbs_post_action WHERE forum_id = #{id} AND user_id = #{user_id}  AND action_type = #{type} and post_id is null and recycle = 0")
    BbsPostActionModel getCountByUserId(Long id, String user_id, Integer type);

    /**
     * 我的粉丝数
     * @param id
     * @return
     */
    @Select("select count(1) from lkt_bbs_post_action where  forum_id = #{id} and action_type = 4 and recycle = 0" )
    int getFansNumById(Long id);

    /**
     * 我的关注数
     * @param user_id
     * @return
     */
    @Select("select count(1) from lkt_bbs_post_action where user_id = #{user_id} and action_type = 4 and recycle = 0")
    int getFollowNum(String user_id);

    @Select("select post_id from lkt_bbs_post_action where user_id = #{user_id} and action_type = #{type} and recycle = 0 and post_id is not null")
    List<Long> getPostList(String user_id,Integer type);

    /**
     * 粉丝列表统计
     * @param map
     * @return
     */
    int focusCount(Map<String, Object> map);

    /**
     * 获取关注列表
     * @param map
     * @return
     */
    List<Map<String, Object>> getFocusList(Map<String, Object> map);

    Map<String, Object> isCount(@Param("post_id") Long id, @Param("user_id") String userId);

    @Select("SELECT COUNT(1) FROM lkt_bbs_post_action WHERE user_id = #{userId} AND forum_id = #{forumId} AND action_type = 4 AND recycle = 0")
    int isFollow(String userId,Long forumId);

}
