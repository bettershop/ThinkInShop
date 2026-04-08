package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.UserCollectionModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * 我的收藏 sql
 *
 * @author Trick
 * @date 2020/11/9 9:15
 */
public interface UserCollectionModelMapper extends BaseMapper<UserCollectionModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    List<Map<String, Object>> selectMchDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取用户收藏的商品
     *
     * @param userCollectionModel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/9 9:17
     */
    List<Map<String, Object>> getUserGoodsCollection(UserCollectionModel userCollectionModel) throws LaiKeAPIException;


    /**
     * 获取店铺收藏
     *
     * @param userCollectionModel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/9 9:41
     */
    List<Map<String, Object>> getMchGoodsCollection(UserCollectionModel userCollectionModel) throws LaiKeAPIException;


    /**
     * 统计关注的客户
     *
     * @param storeId
     * @param mchId
     * @return
     */
    @Select("select count(1) from (select count(1) from lkt_user_collection where store_id = #{storeId} and mch_id = #{mchId} group by user_id) a")
    int getGzKhNum(Integer storeId, Integer mchId);

    @Select("select count(1) from lkt_user_collection where p_id = #{acid} and type = 3 ")
    int getNumByAcId(Integer acid);

    @Select("select count(1) from lkt_user_collection where user_id = #{userId} and mch_id = #{mchId} and type = 3 and p_id is null")
    int isCollected(String userId, Integer mchId);


    /**
     * 获取竞拍商品收藏列表
     * @param userCollectionModel
     * @return
     */
    List<Map<String, Object>> getAuctionGoodsCollection(UserCollectionModel userCollectionModel);

    /**
     * 获取竞拍店铺收藏列表
     * @param userCollectionModel
     * @return
     */
    List<Map<String, Object>> getAuctionMchCollection(UserCollectionModel userCollectionModel);

    /**
     * 获取店铺竞拍收藏数量
     * @param mchId
     * @return
     */
    @Select("SELECT COUNT(1) FROM lkt_user_collection WHERE mch_id = #{mchId} AND type = 3 AND p_id IS NULL")
    int getAuctionMchCollectionNum(Integer mchId);

    @Select("SELECT * FROM lkt_user_collection WHERE mch_id = #{mchId} and user_id = #{user_id} AND type = 3 AND p_id IS NULL")
    UserCollectionModel getAuctionMchCollectionOne(Integer mchId,String user_id);

}