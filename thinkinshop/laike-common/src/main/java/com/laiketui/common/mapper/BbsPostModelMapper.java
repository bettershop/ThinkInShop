package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.bbs.BbsPostModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-09-29-10:04
 * @Description:
 */
public interface BbsPostModelMapper extends BaseMapper<BbsPostModel>
{
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    void del(@Param("ids") List<String> ids) throws LaiKeAPIException;

    /**
     * 获取文章详情
     * @param id
     * @return
     */
    Map<String, Object> getById(@Param("id") Long id);

    /**
     * 更新文章点赞数
     * @param id
     * @param like_num
     */
    @Update("UPDATE lkt_bbs_post SET like_num = like_num + #{like_num} where id = #{id}")
    void updateLikeNum(Long id, int like_num);


    /**
     * 更新文章收藏数
     * @param id
     * @param collect_num
     */
    @Update("UPDATE lkt_bbs_post SET collect_num = collect_num + #{collect_num} where id = #{id}")
    void updateCollectNum(Integer collect_num, Long id);

    /**
     * 获取文章详情
     * @param id
     * @return
     */
    Map<String, Object> details(@Param("id") Long id);

    @Select("SELECT forum_id FROM lkt_bbs_post_action WHERE user_id = #{user_id} AND action_type = 4 AND post_id IS NULL and recycle = 0")
    List<Long> getFocusList(String user_id);

    /**
     * 根据当前用户关注关系，反查可用的种草官ID列表（按被关注用户ID映射）。
     * 兼容历史关注数据 forum_id 缺失的场景。
     */
    List<Long> getFocusForumIdsByUserId(@Param("user_id") String userId, @Param("store_id") Integer storeId);

    @Select("select user_id from lkt_bbs_post where id = #{id}")
    String getUserIdById(Long id);

    @Select("select forum_id from lkt_bbs_post where id = #{id}")
    Long getForumId(Long id);

    /**
     * 获取文章收藏数和点赞数
     * @param postIds
     * @return
     */
    Map<String, Object> getLikeNumAndCollectNum(@Param("postIds") List<String> postIds);

    /**
     * 转发数+1
     * @param id
     */
    @Update("update lkt_bbs_post set forward_num = forward_num + 1 where id = #{id}")
    void updateForwardNum(Long id);

    /**
     * 更新评论数
     * @param id
     * @param num
     */
    @Update("update lkt_bbs_post set comment_num = comment_num + #{num} where id = #{id}")
    void updateCommentNum(int num,Long id);

    List<Long> getIdByForumIds(@Param("focusList") List<Long> focusList);

    /**
     * 统计商品是否有关联的种草内容（审核通过且未删除、未隐藏）
     */
    @Select("select id from lkt_bbs_post where store_id = #{storeId} and recycle = 0 and is_hide = 0 and status = 2 and FIND_IN_SET(#{goodsId}, pro_ids) order by create_time desc, id desc limit 1")
    Integer countByGoodsId(@Param("storeId") int storeId, @Param("goodsId") int goodsId);

}
