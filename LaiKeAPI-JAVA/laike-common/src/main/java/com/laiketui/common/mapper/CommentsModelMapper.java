package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.CommentsModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


/**
 * 评论
 *
 * @author Trick
 * @date 2020/10/23 16:47
 */
public interface CommentsModelMapper extends BaseMapper<CommentsModel>
{


    /**
     * 根据商品id获取数据
     * 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/23 16:47
     */
    List<CommentsModel> getCommentsDynamicByPid(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取评论数据
     *
     * @param map -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/23 17:41
     */
    List<Map<String, Object>> getCommentsUserDynamicByPid(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 统计评论数量
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/14 11:24
     */
    int countCommentsUserDynamicByPid(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取订单评论信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 16:51
     */
    List<Map<String, Object>> getCommentsOrderDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取订单评论信息 - 统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 16:51
     */
    int countCommentsOrderDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Select("<script>" +
            "select count(1) from lkt_order a where a.store_id=#{storeId}  " +
            "and status = 5 and recycle = 0 " +
            " <if test='mchId != null '> " +
            "AND CONVERT(REPLACE(mch_id, ',', ''),unsigned INTEGER) = #{mchId} " +
            " </if> " +
            "and not EXISTS(select x.id from lkt_comments x where x.store_id=a.store_id and x.oid=a.sno)" +
            "</script>")
    int countCommentsOrderNum(int storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 获取商品追评数量
     *
     * @param storeId -
     * @param goodsId -
     * @return int
     * @author Trick
     * @date 2022/2/17 17:19
     */
    @Select("select count(1) from lkt_comments where store_id=#{storeId} and pid = #{goodsId} and review is not null")
    int countCommentsReview(int storeId, int goodsId);

    /**
     * 查询有图的评论数量
     *
     * @param storeId
     * @param goodId
     * @author sunH_
     * @date 2022/05/31 17:19
     */
    @Select("SELECT COUNT(DISTINCT a.id) FROM lkt_comments a RIGHT JOIN lkt_comments_img b ON a.id = b.comments_id WHERE store_id = #{storeId} AND a.pid = #{goodId}")
    int countCommentsImg(int storeId, int goodId);

    //获取评论详情
    @Select("select a.id,a.content,a.add_time,a.anonymous,b.product_title,b.mch_id,u.user_name,u.headimgurl,b.imgurl,b.id goodsId,a.review,d.arrive_time,d.size from lkt_comments a " +
            " left join lkt_product_list b on a.pid=b.id left join lkt_user u on u.user_id=a.uid " +
            " LEFT JOIN lkt_order_details d ON d.id = a.order_detail_id " +
            "where a.id=#{cid} ")
    Map<String, Object> getCommentsDetailByCid(int cid) throws LaiKeAPIException;


    /**
     * 标记评价以读
     *
     * @param commentId
     */
    @Update("update lkt_comments set is_look = 1 where id = #{commentId}")
    void updateIsLookById(int commentId);
}