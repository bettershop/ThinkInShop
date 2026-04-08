package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.auction.AuctionProductModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 竞拍商品
 *
 * @author Trick
 * @date 2021/2/23 9:50
 */
public interface AuctionProductModelMapper extends BaseMapper<AuctionProductModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    //删除之前场次商品
    @Update("update lkt_auction_product a set a.recovery=1 where a.session_id=#{sessionId} and a.recovery=0")
    int delAuctionGoodsBySessionId(String sessionId);

    @Delete("delete a.* from lkt_auction_product a where a.session_id=#{sessionId} and a.recovery=0")
    int delAuctionGoodsBySessionId1(String sessionId);

    @Delete("delete a.* from lkt_auction_product a inner join lkt_product_list b on a.goods_id=b.id and b.mch_id=#{mchId} where a.session_id=#{sessionId} and a.recovery=0 ")
    int delAuctionGoodsBySessionIdByMchId(String sessionId, int mchId);


    /**
     * 获取准备竞拍的商品信息
     *
     * @param storeId   -
     * @param pageStart -
     * @param pageEnd   -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/22 14:29
     */
    @Select("select a.id,a.title,a.imgurl,DATE_FORMAT(a.starttime,'%Y-%m-%d %H:%i:%S') starttime,a.price,a.add_price,a.pepole,a.status " +
            " from lkt_auction_product as a left join lkt_mch as b on a.mch_id = b.id " +
            "    where b.store_id = #{storeId} and a.status = 0 and a.recycle = 0 and a.is_show = 1 and b.is_open = 1 LIMIT #{pageStart}, #{pageEnd}")
    List<Map<String, Object>> selectAuctionReadyGoodsInfo(int storeId, int pageStart, int pageEnd) throws LaiKeAPIException;

    int countAuctionGoodsList(Map<String, Object> map);

    /**
     * 获取竞拍商品
     *
     * @param map -
     * @return List
     * @author Trick
     * @date 2022/7/4 17:47
     */
    List<Map<String, Object>> pluginSelectAuctionGoodsList(Map<String, Object> map);

    int pluginCountAuctionGoodsList(Map<String, Object> map);


    //根据场次获取商品信息
        String GET_GOODS_JOINCONFIGUREBYID_SQL = " FROM lkt_auction_product AS a INNER JOIN lkt_configure AS c ON a.attr_id = c.id left join lkt_product_list goods on goods.id=c.pid left join lkt_user_collection d on d.p_id=a.id and type=3 WHERE a.session_id=#{sessionId} and a.recovery=0 and a.is_show=1 and goods.recycle = 0 ";

    @Select(" select c.id,a.id acId,(select count(1) from lkt_auction_record x where x.auction_id=a.id) outNum,c.img,a.price,d.id collectionId,goods.mch_id as mchId,c.attribute,goods.product_title,a.starting_amt " + GET_GOODS_JOINCONFIGUREBYID_SQL + " order by a.price ${priceSort} limit #{start},#{end} ")
    List<Map<String, Object>> getGoodsJoinConfigureById(int storeId, String sessionId, String priceSort, int start, int end) throws LaiKeAPIException;

    @Select(" select count(1) " + GET_GOODS_JOINCONFIGUREBYID_SQL)
    Integer countGoodsJoinConfigureById(int storeId, String sessionId) throws LaiKeAPIException;

    //根据专场获取商品信息 参数: storeId、specialId、orderBy、start、end
    List<Map<String, Object>> getGoodsInfoList(Map<String, Object> map) throws LaiKeAPIException;

    //参数: storeId、specialId
    Integer countGoodsInfoList(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取竞拍商品详情
     */
    @Select("select a.id,a.attr_id,d.content,b.special_id,b.status,d.mch_id as mchId,b.name,b.end_date,b.start_date,c.promise_amt,a.price,a.starting_amt,a.mark_up_amt,c.commission,d.product_title,d.imgurl,e.freight,e.default_freight,e.supplier_id,d.content,a.user_id,d.id goodsId,c.id specialId " +
            "from lkt_auction_product a,lkt_auction_session b,lkt_auction_special c,lkt_product_list d,lkt_freight e " +
            "where a.session_id = b.id and b.special_id=c.id and a.recovery=b.recovery and a.recovery=c.recovery and a.recovery=d.recycle " +
            "and d.id=a.goods_id and d.freight=e.id and a.id=#{acid}")
    Map<String, Object> getGoodsDetail(int acid);

    //获取我的竞拍商品列表
    String SELECTMYAUCTIONGOODSLIST_SQL = " from lkt_auction_record a,lkt_auction_product b,lkt_product_list c,lkt_auction_session d,lkt_configure e where b.recovery = 0 and a.auction_id=b.id <if test='userId != null '>and a.user_id=#{userId} </if> <if test='goodsStatus != null '><foreach collection=\"goodsStatus\" item=\"status\" separator=\",\" open=\"and b.status in(\" close=\")\">#{status}</foreach></if> and d.status=#{sessionStatus} <if test='finalUserId != null '>and b.user_id=#{finalUserId} </if> <if test='notMy !=null '>and b.user_id!=#{notMy} </if> <if test='notMy02 !=null '>and (b.user_id!=#{notMy02} or(b.status = 3 and passed_status = 2) )</if> <if test='passedStatus !=null '>and b.passed_status=#{passedStatus} </if> and b.goods_id=c.id and b.session_id=d.id and b.attr_id=e.id and c.recycle=0 and c.recycle=b.recovery and c.recycle=d.recovery and c.recycle=e.recycle ";

    //获取我的竞拍收藏列表
    String SELECTMYCOLLECTIONAUCTIONGOODSLIST_SQL = " from lkt_user_collection a,lkt_auction_product b,lkt_product_list c,lkt_auction_session d,lkt_configure e where b.recovery = 0 and a.store_id=#{storeId} and a.user_id=#{userId} and a.p_id=b.id and a.type=3 and b.goods_id=c.id and b.session_id=d.id and b.attr_id=e.id and c.recycle=0 and c.recycle=b.recovery and c.recycle=d.recovery and c.recycle=e.recycle";

    /**
     * 根据竞拍商品id获取信息,字段只能增不能减少
     */
    @Select("select c.id special_id,a.mark_up_amt,a.starting_amt,b.id sessionId,a.price from lkt_auction_product a,lkt_auction_session b,lkt_auction_special c where a.session_id = b.id and b.special_id=c.id and a.recovery=b.recovery and a.recovery=c.recovery and a.id=#{acId} ")
    Map<String, Object> getGoodsInfo(int acId);

    /**
     * 获取当前商品最高出价
     */
    @Select("select ifnull(max(price),0) from lkt_auction_record a where a.auction_id=#{acId} order by a.add_time desc ")
    BigDecimal getGoodsMaxPrice(int acId);

    /**
     * 获取当前商品最高出价人
     */
    @Select("select a.user_id from lkt_auction_record a where a.auction_id=#{acId} order by a.price desc limit 1")
    String getGoodsMaxUserId(int acId);

    /**
     * 获取当前商品最高出id
     */
    @Select("select a.id from lkt_auction_record a where a.auction_id=#{acId} group by a.auction_id order by a.price desc ")
    Integer getGoodsMaxAcId(int acId);

    /**
     * 更新最终得主
     */
    @Update("update lkt_auction_product a set a.user_id=#{mainUserId},a.update_date=#{sysDate} where a.id=#{acId} ")
    int setGoodsMaxUserId(int acId, String mainUserId, Date sysDate);

    /**
     * 获取店铺我的拍品列表
     */
    List<Map<String, Object>> selectMchAuctionGoodsList(Map<String, Object> map);

    int countMchAuctionGoods(Map<String, Object> map);

    //获取订单信息
    @Select("select a.id,a.user_id,a.p_id acId,a.r_status,b.goods_id,b.attr_id from lkt_order_details a inner join lkt_auction_product b on a.p_id=b.id where a.r_sno=#{orderNo} and a.store_id=#{storeId} and a.recycle=0 ")
    Map<String, Object> getAuctionOrderInfo(int storeId, String orderNo);

    //获取拍品得主的订单号
    @Select("select b.r_sno from lkt_auction_product a inner join lkt_order_details b on a.id=b.p_id and b.user_id=a.user_id and b.r_status=0 where a.id=#{acId} and a.user_id=#{userId} order by a.add_date desc limit 1 ")
    String getAuctionOrder(int acId, String userId);

    //商品是否有进行中的竞拍专场
    @Select("SELECT COUNT(1) FROM `lkt_auction_product` a RIGHT JOIN lkt_auction_session b ON a.session_id = b.id RIGHT JOIN lkt_auction_special c ON b.special_id = c.id WHERE a.goods_id = #{proId} AND c.status = 2 AND c.recovery = 0 AND c.recovery = b.recovery")
    int getStartAuctionPro(int proId);

    /**
     * 获取我的竞拍收藏列表
     */
    @Select("select b.id acId,c.product_title,b.`status` goodsStatus,d.`status` sessionStatus,d.special_id,d.end_date,d.start_date,b.price,b.starting_amt,e.img,e.attribute  " + SELECTMYCOLLECTIONAUCTIONGOODSLIST_SQL + " order by a.add_time desc limit #{start},#{end}")
    List<Map<String, Object>> selectMyCollectionAuctionGoodsList(int storeId, String userId, int start, int end);

    @Select("select count(1) " + SELECTMYCOLLECTIONAUCTIONGOODSLIST_SQL)
    int countMyCollectionAuctionGoods(int storeId, String userId);

    /**
     * 增减竞拍数量
     *
     * @param storeId -
     * @param id      -
     * @param num     -
     * @return int
     * @author Trick
     * @date 2021/4/23 15:04
     */
    @Update("update lkt_auction_product set pepole = pepole+#{num} where store_id = #{storeId} and id = #{id}")
    int addAuctionNumById(int storeId, int id, int num);

    /**
     * 获取我的竞拍列表
     *
     * @param map -
     */
    //@Select("<script>select distinct a.price currenPrice,b.passed_status,b.id acId,c.product_title,b.`status` goodsStatus,d.special_id,d.`status` sessionStatus,d.end_date,d.start_date,b.price,b.starting_amt,e.img,b.sNo,e.attribute,c.mch_id mchId " + SELECTMYAUCTIONGOODSLIST_SQL + " group by a.auction_id order by a.add_time desc limit #{start},#{end} </script>")
    List<Map<String, Object>> selectMyAuctionGoodsList(Map<String, Object> map);

    @Select("<script> select count(distinct b.id) " + SELECTMYAUCTIONGOODSLIST_SQL + " </script>")
    int countMyAuctionGoods(Map<String, Object> map);

    /**
     * 更新当前商品竞拍价
     *
     * @param currentPrice -
     * @param storeId      -
     * @param id           -
     * @return int
     * @author Trick
     * @date 2021/4/23 16:17
     */
    @Update("update lkt_auction_product set current_price = #{currentPrice} where store_id = #{storeId} and id = #{id} and status = 1")
    int updateAuctionPrice(BigDecimal currentPrice, int storeId, int id);


    /**
     * 开始竞拍活动
     *
     * @param stroeId   -
     * @param id        -
     * @param startTime -
     * @return int
     * @author Trick
     * @date 2021/4/24 9:09
     */
    @Update("update lkt_auction_product set status = 1,starttime = #{startTime} where id = #{id} and store_id = #{stroeId} and recycle = 0")
    int startAuction(int stroeId, int id, Date startTime);


    /**
     * 更新竞拍得主,竞拍人数必须大于最低参与人数
     *
     * @param storeId -
     * @param id      -
     * @param price   -
     * @param userId  -
     * @return int
     * @author Trick
     * @date 2021/4/24 10:37
     */
    @Update("update lkt_auction_product set status = 2,current_price = #{price},user_id = '$user_id' where store_id = #{storeId} and id = #{id} and pepole >= low_pepole")
    int updateAuctionUser(int storeId, int id, BigDecimal price, String userId);


    /**
     * 流拍
     *
     * @param storeId -
     * @param id      -
     * @return int
     * @author Trick
     * @date 2021/4/24 14:57
     */
    @Update("update lkt_auction_product set status = 3 where store_id = #{storeId} and id = #{id} and pepole < low_pepole")
    int updateAuctionStreaming(int storeId, int id);

    /**
     * 修改竞拍订单
     *
     * @param orderno -
     * @param isBuy   -
     * @param storeId -
     * @param aid     -
     * @param userId  -
     * @return int
     * @author Trick
     * @date 2021/4/24 12:19
     */
    @Update("update lkt_auction_product set trade_no = #{orderno},is_buy = #{isBuy},user_id = #{userId} where store_id = #{storeId} and id = #{aid}")
    int updateAuctionOrder(String orderno, int isBuy, int storeId, int aid, String userId);

    //根据竞拍id获取店铺信息
    @Select("select b.id mchId,b.account_money money from lkt_auction_special a inner join lkt_mch b on a.mch_id=b.id where a.id=#{specialId}")
    Map<String, Object> getMchInfoBySpecialId(String specialId);


    //获取店铺拍品数
    int getMchGoodsTotal(@Param("mchId") Integer mchId);

    List<String> getImgListById(@Param("specialId") String specialId);

}