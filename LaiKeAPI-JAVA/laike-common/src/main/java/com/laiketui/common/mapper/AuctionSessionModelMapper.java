package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.auction.AuctionSessionModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 竞拍场次表
 *
 * @author Trick
 * @date 2022/7/1 18:52
 */
public interface AuctionSessionModelMapper extends BaseMapper<AuctionSessionModel>
{

    /**
     * 获取竞拍场次列表
     *
     * @author Trick
     * @date 2022/7/4 11:15
     */
    List<Map<String, Object>> selectAuctionSessionList(Map<String, Object> map);

    int countAuctionSessionList(Map<String, Object> map);

    /**
     * 获取竞拍商品
     *
     * @param map -
     * @return List
     * @author Trick
     * @date 2022/7/4 17:47
     */
    List<Map<String, Object>> selectAuctionGoodsList(Map<String, Object> map);

    int countAuctionGoodsList(Map<String, Object> map);

    //场次名称是否重复
    @Select("select count(a.id) from lkt_auction_session a,lkt_auction_special b where a.special_id=b.id and a.recovery=0 and a.recovery=b.recovery and a.special_id=#{specialId} " +
            " and a.end_date > #{sysDate} and b.end_date>#{sysDate} and a.name=#{name} ")
    int isAuctionName(Date sysDate, String specialId, String name);

    //场次商品是否重复
    @Select("<script>" +
            "select count(a.id) from lkt_auction_session a,lkt_auction_special b,lkt_auction_product c where a.special_id=b.id and a.recovery=0 and a.recovery=b.recovery and c.recovery=a.recovery and a.special_id=#{specialId} and a.id=c.session_id " +
            " and a.end_date > #{sysDate} and b.end_date>#{sysDate} and c.attr_id=#{attrId} " +
            "<if test='currentSessionId != null '> " +
            " and a.id!=#{currentSessionId}" +
            "</if> " +
            "</script>")
    int isAuctionGoods(Date sysDate, String specialId, String currentSessionId, Integer attrId);

    /**
     * 手动开始竞拍活动
     *
     * @param sysDate   -
     * @param status    -
     * @param specialId -
     * @return int
     * @author Trick
     * @date 2022/7/4 17:34
     */
    @Update("update lkt_auction_session a set a.status=#{status},a.start_date=#{sysDate},a.update_date=#{sysDate} where a.special_id=#{specialId} and a.recovery=0")
    int startAuction(Date sysDate, Integer status, String specialId);

    //删除专场下所有场次
    @Update("update lkt_auction_session a set a.recovery=1 where a.special_id=#{specialId} and a.recovery=0 and a.recovery=0")
    int delSessionByAuctionId(String specialId);

    //获取店铺专场场次信息
    @Select("select * from lkt_auction_session where special_id = #{specialId} limit 1")
    AuctionSessionModel getMchSessionBySpecialId(String specialId);

    @Select("select b.id,d.id attrId,c.product_title goodsName,e.pname className,d.attribute,b.starting_amt startingAmt,b.mark_up_amt markUpAmt,d.price,d.img from lkt_auction_session a inner join lkt_auction_product b on a.id = b.session_id and a.recovery=b.recovery " +
            " left join lkt_product_list c on c.id = b.goods_id left join lkt_configure d on d.id=b.attr_id left join lkt_product_class e on e.cid= substring_index(substring_index(c.product_class, '-', 2), '-', -1) " +
            " where a.recovery=0 and a.id=#{sessionId} ")
    List<Map<String, Object>> getSessionGoodsInfo(String sessionId) throws LaiKeAPIException;
}