package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.auction.AuctionRecordModel;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 竞拍出价记录
 *
 * @author Trick
 * @date 2022/7/21 15:34
 */
public interface AuctionRecordModelMapper extends BaseMapper<AuctionRecordModel>
{


    /**
     * 获取竞拍记录信息
     *
     * @param map -
     * @return List
     * @author Trick
     * @date 2021/4/22 16:27
     */
    List<Map<String, Object>> selectAuctionRecordInfo(Map<String, Object> map) throws LaiKeAPIException;

    int countAuctionRecordInfo(Map<String, Object> map);


    /**
     * 获取下次可加价时间
     *
     * @param storeId -
     * @param aid     -
     * @param userId  -
     * @return String
     * @author Trick
     * @date 2021/4/22 17:39
     */
    @Select("select add_time from lkt_auction_record where store_id = #{storeId} and auction_id = #{aid} and user_id = #{userId} order by add_time desc limit 1")
    String selectAuctionTime(int storeId, int aid, String userId);


    /**
     * 获取最大竞拍价
     *
     * @param storeId -
     * @param aid     -
     * @return BigDecimal
     * @author Trick
     * @date 2021/4/23 16:00
     */
    @Select("select max(price) from lkt_auction_record where store_id = #{storeId} and auction_id = #{aid}")
    BigDecimal selectMaxPrice(int storeId, int aid);


    /**
     * 获取最高得主
     *
     * @param storeId -
     * @param aid     -
     * @return BigDecimal
     * @author Trick
     * @date 2021/5/17 10:44
     */
    @Select("select price,user_id from lkt_auction_record where store_id = #{storeId} and auction_id = #{aid}  ORDER BY price desc,add_time desc,id desc limit 1")
    Map<String, String> selectMaxUser(int storeId, int aid);


    /**
     * 获取最大竞拍价信息
     *
     * @param storeId -
     * @param aid     -
     * @return AuctionRecordModel
     * @author Trick
     * @date 2021/4/24 10:29
     */
    @Select("SELECT * FROM lkt_auction_record WHERE store_id = #{storeId} and auction_id = #{aid} ORDER BY price DESC  LIMIT 1")
    AuctionRecordModel selectMaxPriceInfo(int storeId, int aid);


    List<Map<String, Object>> selectAuctionRecordList(Map<String, Object> map) throws LaiKeAPIException;

    List<Map<String, Object>> countAuctionRecordList(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取当前竞拍商品拍卖人数
     */
    @Select("select count(distinct user_id) from lkt_auction_record where store_id =#{storeId} and auction_id=#{acId} ")
    int getRecordNumByUserId(int storeId, int acId);
}