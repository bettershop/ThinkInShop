package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.mch.IntegralGoodsModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 竞拍相关sql-不要用xml
 *
 * @author Trick
 * @date 2022/7/25 16:46
 */
public interface AuctionMapper extends BaseMapper<IntegralGoodsModel>
{

    /**
     * 获取竞拍商品信息
     *
     * @author Trick
     * @date 2022/7/25 16:47
     */
    @Select("select g.id,g.session_id sessionId,g.goods_id,g.attr_id,g.price goodsPrice,g.starting_amt startingAmt,a.content,a.mch_id,g.user_id,a.product_title,a.volume,a.`status`,attr.img imgurl,a.freight,a.initial,attr.attribute,a.weight from lkt_auction_product as g " +
            "left join lkt_product_list as a on g.goods_id=a.id inner join lkt_configure attr on attr.id=g.attr_id where g.id = #{acId}")
    Map<String, Object> getGoodsDetailInfo(int acId);

    /**
     * 获取竞拍商品最新出价
     *
     * @param acId -
     * @return BigDecimal
     * @author Trick
     * @date 2022/7/25 17:22
     */
    @Select("SELECT IFNULL(MAX(price), 0) AS max_price FROM (SELECT price FROM lkt_auction_record WHERE auction_id = #{acId} ORDER BY add_time DESC) AS temp")
    BigDecimal getGoodsMaxPrice(int acId);

    //专场结束
    @Update("update lkt_auction_special a left join lkt_auction_session b on a.id=b.special_id and a.recovery=b.recovery set a.`status`=#{status},b.`status`=#{status} where a.recovery=0 and a.is_settlement=0 " +
            "and a.`status`!=#{status} and a.end_date<=#{sysDate} and a.store_id=#{storeId}")
    int invalidSpecial(Integer storeId, Date sysDate, Integer status);

    //专场进行中
    @Update("update lkt_auction_special a left join lkt_auction_session b on a.id=b.special_id and a.recovery=b.recovery set a.`status`=#{status} where a.recovery=0 and a.is_settlement=0 " +
            "and a.`status`!=#{status} and a.end_date>=#{sysDate} and a.start_date<=#{sysDate} and a.store_id=#{storeId}")
    int startedSpecial(Integer storeId, Date sysDate, Integer status);

    //专场未开始
    @Update("update lkt_auction_special a left join lkt_auction_session b on a.id=b.special_id and a.recovery=b.recovery set a.`status`=#{status} where a.recovery=0 and a.is_settlement=0 " +
            "and a.start_date>=#{sysDate} and a.store_id=#{storeId}")
    int notStartedSpecial(Integer storeId, Date sysDate, Integer status);


    //场次结束
    @Update("update lkt_auction_special a,lkt_auction_session b set b.`status`=#{status} where a.id=b.special_id and  a.is_settlement=0 " +
            "and b.`status`!=#{status} and b.end_date<=#{sysDate} and a.store_id=#{storeId} and a.is_settlement=1")
    int invalidSession(Integer storeId, Date sysDate, Integer status);

    //场次进行中
    @Update("update lkt_auction_special a,lkt_auction_session b set b.`status`=#{status} where a.id=b.special_id and  a.is_settlement=0 " +
            "and b.`status`!=#{status} and b.end_date>=#{sysDate} and b.start_date<=#{sysDate} and a.store_id=#{storeId}")
    int startedSession(Integer storeId, Date sysDate, Integer status);

    /**
     * 场次开始时同步竞拍商品状态为进行中
     */
    @Update("update lkt_auction_product p " +
            "inner join lkt_auction_session s on p.session_id = s.id and p.recovery = s.recovery " +
            "inner join lkt_auction_special sp on s.special_id = sp.id and sp.recovery = s.recovery " +
            "set p.`status` = #{newStatus}, p.update_date = #{sysDate} " +
            "where sp.store_id = #{storeId} and sp.is_settlement = 0 and p.recovery = 0 " +
            "and s.`status` = #{sessionStatus} and p.`status` = #{oldStatus}")
    int startedGoodsBySession(Integer storeId, Date sysDate, Integer sessionStatus, Integer oldStatus, Integer newStatus);

    //场次未开始
    @Update("update lkt_auction_special a,lkt_auction_session b set b.`status`=#{status} where a.id=b.special_id and  a.is_settlement=0 " +
            "and b.start_date>=#{sysDate} and a.store_id=#{storeId}")
    int notStartedSession(Integer storeId, Date sysDate, Integer status);

    //删除已报名的商品
    @Update("update lkt_product_list a,lkt_auction_session b,lkt_auction_product c set c.recovery=1 where a.mch_id=#{mchId} and b.special_id=#{specialId} and a.id=c.goods_id and c.session_id=b.id and c.recovery=0 and c.recovery=b.recovery ")
    int delSignGoodsBySpeicalId(String specialId, Integer mchId);

    //店铺专场是否显示
    @Select("select b.is_show from lkt_auction_special a,lkt_auction_session b where a.id=b.special_id and a.type=1 and a.id=#{specialId} limit 1")
    int specialIsShow(String specialId);


    //根据订单号获取相关信息
    @Select("select a.id acId,b.id sessionId,c.id specialId,od.user_id userId from lkt_auction_product a inner join lkt_order_details od on od.p_id=a.id inner join lkt_auction_session b on a.session_id=b.id inner join lkt_auction_special c on b.special_id=c.id where od.store_id=#{storeId} and od.r_sno=#{sno}")
    Map<String, Object> getOrderInfo(int storeId, String sno);

    //关联订单
    @Update("update lkt_auction_product a set a.sNo=#{sno} where a.id=#{acId}")
    int innerOrder(int acId, String sno);

    /**
     * 获取当前专场有竞拍得主且最后结束的时间
     */
    @Select("select c.add_time from lkt_auction_product a inner join lkt_auction_session b on a.session_id=b.id inner join lkt_order c on c.sno=a.sno and c.`status`=0 where a.`status`=2 and a.user_id is not null and b.special_id=#{specialId} order by b.end_date desc limit 1 ")
    String getGoodsOrderTime(String specialId);

    /**
     * 竞拍订单结算-获取可以结算的佣金
     * 只要支付了就可以结算佣金
     */
    @Select("select e.sNo,d.type,e.z_price zPrice,f.after_discount price,f.freight,e.id orderId,f.id detailId,e.mch_id mchId,IFNULL(d.commission,0) commission from lkt_auction_product b inner join lkt_auction_session c on b.session_id=c.id inner join lkt_auction_special d on c.special_id=d.id inner join lkt_order e on e.sno=b.sno inner join lkt_order_details f on e.sno=f.r_sno " +
            " where d.id=#{specialId} and e.status>=1 and e.status!=7 and b.`status`=2 and d.is_settlement=0 ")
    List<Map<String, Object>> getGoodsOrderPrice(String specialId);


}
