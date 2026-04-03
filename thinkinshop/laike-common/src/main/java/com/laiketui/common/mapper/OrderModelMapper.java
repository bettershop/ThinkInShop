package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.order.OrderModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 订单 sql
 *
 * @author Trick
 * @date 2020/11/4 15:09
 */
public interface OrderModelMapper extends BaseMapper<OrderModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态统计店铺营业额
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> sumMchTurnoverReport(Map<String, Object> map);

    int countMchTurnoverReport(Map<String, Object> map);


    /**
     * 动态统计店铺-订单数量
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> sumMchTurnoverOrderReport(Map<String, Object> map);

    int countMchTurnoverOrderReport(Map<String, Object> map);

    /**
     * 统计未评论的订单数量
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 14:44
     */
    @Select("<script>" +
            "SELECT count(1) FROM lkt_order a WHERE a.store_id = #{store_id} AND a.user_id = #{user_id} " +
            " AND a.STATUS = 5 AND a.recycle = 0 and a.user_recycle=1" +
            " <if test='typeList != null '> " +
            "   <foreach collection=\"typeList\" item=\"type\" separator=\",\" open=\"and a.otype in(\" close=\")\"> " +
            "        #{type,jdbcType=INTEGER}" +
            "   </foreach> " +
            "</if> " +
            " AND not EXISTS(select x.id from lkt_comments x where x.store_id=a.store_id and x.oid=a.sno)" +
            " <if test='user_recycle!= null'> and a.user_recycle = #{user_recycle} </if> " +
            "</script>")
    int countNotCommentNum(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 统计订单数量动态
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/9 17:30
     */
    int countOrdersNumDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取订单表数据
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/24 18:16
     */
    List<Map<String, Object>> getOrdersNumDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取未关闭的订单信息
     *
     * @param orderno -
     * @return List
     * @author Trick
     * @date 2021/4/16 16:28
     */
    @Select("SELECT id,status from lkt_order where status != '6' and status != '7' and status != '8' and  real_sno = #{orderno}")
    OrderModel getNotCloseOrdersByNo(String orderno);

    /**
     * 更新订单备注
     *
     * @param remarks
     * @param sNo
     * @param storeId
     * @return
     */
    @Update("update lkt_order set remarks= #{remarks} where sNo = #{sNo} and store_id = #{storeId}")
    int updateOrderRemark(String remarks, String sNo, int storeId);


    /**
     * 修改订单状态
     */
    @Update("update lkt_order set status= #{status} where sNo = #{orderNo} and store_id = #{storeId}")
    int updateOrderStatusByOrderNo(int storeId, int status, String orderNo);

    /**
     * 订单结算
     */
    @Update("update lkt_order set status=5,settlement_status=1,arrive_time=#{sysDate} where sNo = #{orderNo} and store_id = #{storeId}")
    int updateOrderSettlementByOrderNo(int storeId, String orderNo, Date sysDate);

    /**
     * 根据订单号，查询订单商品ID
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Select("select a.id,b.p_id,d.id goodsId,d.supplier_superior,b.num,a.offset_balance,c.img,b.p_name from lkt_order as a inner join lkt_order_details as b on a.sno=b.r_sno inner join lkt_configure as c on c.id = b.sid inner join lkt_product_list as d on d.id = c.pid " +
            " where a.store_id = #{storeId} and a.sno = #{sNo} ")
    List<Map<String, Object>> getOrderProductInfo(int storeId, String sNo);

    /**
     * 优惠券拆分订单获取订单详情
     *
     * @param storeId
     * @param sNo
     * @param mchId
     * @return
     */
    @Select("select a.id,a.p_id,c.price p_price,c.id as cid,a.num,a.freight,b.product_class,a.r_sNo,a.after_discount,b.write_off_settings from lkt_order_details as a " +
            " left join lkt_product_list as b on a.p_id = b.id left join lkt_configure c on c.id=a.sid where a.store_id = #{storeId} and a.r_sNo = #{sNo} and b.mch_id = #{mchId}")
    List<Map<String, Object>> getOrderDetails(int storeId, String sNo, int mchId);

    /**
     * 优惠券拆分订单获取订单详情
     *
     * @param storeId
     * @param sNo
     * @param mchId
     * @return
     */
    @Select("select a.id,a.p_id,c.price p_price,a.num,a.freight,b.product_class,a.r_sNo,a.after_discount from lkt_order_details as a " +
            " left join lkt_product_list as b on a.p_id = b.id left join lkt_configure c on c.id=a.sid where  a.r_sNo = #{sNo}")
    List<Map<String, Object>> getOrderDetailsBySno(int storeId, String sNo, int mchId);

    /**
     * 更新订单状态
     *
     * @param storeId
     * @param sNo
     * @param userId
     * @return
     */
    @Update("update lkt_order set status = #{status} where store_id = #{storeId} and sNo = #{sNo} and user_id = #{userId} ")
    int updateOrderStatus(int storeId, String sNo, String userId, int status);


    /**
     * 关闭过期的订单
     *
     * @param id         -
     * @param arriveTime -
     * @return int -
     * @author Trick
     * @date 2020/12/14 16:19
     */
    @Update("update lkt_order set status = 7,settlement_status = 1,arrive_time = #{arriveTime} where id=#{id} and status = 0 ")
    int updateOrderStatusById(int id, Date arriveTime);


    /**
     * 重置发货提醒
     *
     * @param id             -
     * @param deliveryStatus -
     * @param readd          -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/15 15:56
     */
    @Update("UPDATE lkt_order SET delivery_status = #{deliveryStatus},readd = #{readd},remind=null WHERE id = #{id} ")
    int updateDeliveryReset(int id, int deliveryStatus, int readd) throws LaiKeAPIException;

    /**
     * 动态获取订单信息
     *
     * @param map -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 19:23
     */
    List<Map<String, Object>> getOrderInfoListDynamce(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 动态获取订单详情信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 9:08
     */
    List<Map<String, Object>> getOrderInfoLeftDetailDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 查看提货码
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select a.z_price,a.sNo,a.status,a.mch_id,a.extraction_code,a.extraction_code_img,a.otype,c.pid,b.p_name,b.p_price,b.num,b.size,b.sid,b.write_off_num,b.mch_store_write_id,b.write_time,b.id as dId,b.after_write_off_num " +
            "from lkt_order as a " +
            "left join lkt_order_details as b on a.sNo = b.r_sNo " +
            "left join lkt_configure as c on b.sid = c.id " +
            "where a.store_id = #{store_id} and a.id = #{id} and a.user_id = #{user_id} ")
    List<Map<String, Object>> seeExtractionCode(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 根据订单号修改订单信息 动态sql
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 17:31
     */
    int updateByOrdernoDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 删除订单信息
     *
     * @param orderNo -
     * @return int
     * @author Trick
     * @date 2021/7/21 11:12
     */
    @Update("update lkt_order a,lkt_order_details b set a.recycle=1,b.recycle=1 where a.sno=b.r_sno and a.sno=#{orderNo}")
    int delOrderById(String orderNo);

    /**
     * 更新订单金额
     *
     * @param storeId      -
     * @param orderno      -
     * @param orderPrice   - 增加/减去 订单总金额
     * @param orderFreight - 增加/减去 订单运费
     * @param goodsPrice   - 增加/减去 商品价格
     * @return int
     * @throws LaiKeAPIException -
     */
    @Update("update lkt_order set z_price = z_price - #{orderPrice},z_freight = z_freight - #{orderFreight},spz_price = spz_price - #{goodsPrice} " +
            "where store_id = #{storeId} and sNo = #{orderno} ")
    int updateOrderPrice(int storeId, String orderno, BigDecimal orderPrice, BigDecimal orderFreight, BigDecimal goodsPrice) throws LaiKeAPIException;

    /**
     * 获取订单状态数量
     *
     * @param param
     * @return
     */
    int getUserCenterOrderNumByStatus(Map<String, Object> param);

    /**
     * 个人中心获取订单列表信息
     *
     * @param param
     * @return
     */
    List<Map<String, Object>> getUserCenterOrderList(Map<String, Object> param);

    /**
     * 个人中心获取订单列表数量
     *
     * @param param
     * @return
     */
    int getUserCenterOrderListCount(Map<String, Object> param);

    /**
     * 更新提醒发货
     *
     * @param storeId
     * @param userId
     * @param orderId
     * @return
     */
    @Update("update lkt_order set delivery_status='1',readd='0',remind=#{sysDate} where store_id = #{storeId} and user_id = #{userId} and id = #{orderId} ")
    int updateDeliveryRemind(int storeId, String userId, int orderId, Date sysDate);


    /**
     * 更新订单信息
     *
     * @param map
     * @return
     */
    int updateOrderInfo(Map<String, Object> map);

    /**
     * 删除订单
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Update(" update lkt_order set recycle = 1  where store_id = #{storeId} and sNo = #{sNo} ")
    int delOrder(int storeId, String sNo);

    /**
     * 删除订单
     */
    @Update(" update lkt_order set recycle = #{delType}  where store_id = #{storeId} and sNo = #{sNo} ")
    int delOrder1(int storeId, int delType, String sNo);


    /**
     * 判断用户、商家、平台是否都已经删除订单
     */
    @Select(" SELECT count(0) FROM lkt_order " +
            " WHERE store_id = #{storeId} and sNo = #{sNo} and user_recycle = 2 and mch_recycle = 2 and store_recycle = 2 and recycle = 0")
    int getAllDelOrder(int storeId, String sNo);

    /**
     * 用户删除订单
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Update("update lkt_order set user_recycle = 2 where store_id = #{storeId} and sNo = #{sNo} ")
    int userDelOrder(int storeId, String sNo);

    /**
     * 商户删除订单
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Update("update lkt_order set mch_recycle = 2 where store_id = #{storeId} and sNo = #{sNo} ")
    int mchDelOrder(int storeId, String sNo);

    /**
     * 商城删除订单
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Update("update lkt_order set store_recycle = 2 where store_id = #{storeId} and sNo = #{sNo} ")
    int storeDelOrder(int storeId, String sNo);

    /**
     * 进入退款界面获取订单退款信息
     *
     * @param storeId
     * @param returnOrderDetailsId
     * @return
     */
    @Select("select a.currency_code,a.currency_symbol,a.exchange_rate,a.id,m.freight,a.trade_no,m.num,a.sNo,a.pay,a.z_price,a.user_id,a.spz_price,m.p_price," +
            "  a.consumer_money,m.express_id,a.z_freight,m.p_id,m.after_discount,m.after_write_off_num,m.write_off_num " +
            "  from lkt_order as a LEFT JOIN lkt_order_details AS m ON a.sNo = m.r_sNo where a.store_id = #{storeId} and m.id = #{returnOrderDetailsId} ")
    Map<String, Object> getReturnOrderInfo(int storeId, int returnOrderDetailsId);

    /**
     * 余额支付更新订单
     *
     * @param params
     * @return
     */
    int wallectPayUpdateOrder(Map<String, Object> params);

    /**
     * 支付成功后更新订单信息
     *
     * @param pay
     * @param tradeNo
     * @param sNo
     * @param userId
     * @return
     */
    @Update("update lkt_order set status = 1,pay = #{pay},trade_no=#{tradeNo}," +
            "pay_time=CURRENT_TIMESTAMP where sNo = #{sNo} and user_id = #{userId} ")
    int payBackUpOrder(String pay, String tradeNo, String sNo, String userId);


    /**
     * 分佣标记已结算
     *
     * @param storeId -
     * @param orderno -
     * @return int
     * @author Trick
     * @date 2021/5/25 10:38
     */
    @Update("update lkt_order set commission_type = 1 where store_id = #{storeId} and sNo = #{orderno}")
    int disSettlement(int storeId, String orderno);


    /**
     * 标记订单是否已读
     *
     * @param storeId -
     * @param sNo     -
     * @param readd   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 14:26
     */
    @Update("update lkt_order set readd = #{readd} where store_id = #{storeId} and sNo = #{sNo}")
    int updateIsReadd(int storeId, String sNo, int readd) throws LaiKeAPIException;


    /**
     * 统计用户订单金额
     *
     * @param storeId -
     * @param userId  -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 11:36
     */
    @Select("select ifnull(SUM(z_price),0) as z_price from lkt_order where store_id = #{storeId} " +
            "and user_id=#{userId} and status > 0 and pay_time is not null ")
    BigDecimal sumUserOrderPrice(int storeId, String userId) throws LaiKeAPIException;


    /**
     * 统计用户订单退款金额
     *
     * @param storeId -
     * @param userId  -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 11:36
     */
    @Select("SELECT ifnull( SUM( a.real_money ), 0 )  FROM lkt_return_order a LEFT JOIN lkt_order b ON a.sNo = b.sNo " +
            "WHERE b.STATUS = 7 and b.store_id = #{storeId} and b.user_id=#{userId} ")
    BigDecimal sumUserOrderRefundPrice(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 统计用户未退款的订单数量
     *
     * @param storeId -
     * @param userId  -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 14:56
     */
    @Select("SELECT count(1) FROM lkt_order WHERE store_id = #{storeId} AND user_id = #{userId} AND STATUS > 0 " +
            "AND STATUS NOT IN (4, 7, 11) AND pay_time is not null " +
            "AND sno NOT IN ( SELECT sNo FROM lkt_return_record " +
            "WHERE store_id = #{storeId} AND user_id = #{userId} AND r_type NOT IN (1, 4, 9, 11) )")
    int countUserEffectiveOrdernoNum(int storeId, String userId) throws LaiKeAPIException;


    /**
     * 统计用户未完成的订单
     *
     * @param storeId -
     * @param userId  -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/8 9:29
     */
    @Select("select count(1) from lkt_order as a ,lkt_user as b  where a.user_id = b.user_id and b.user_id = #{userId} and a.recycle=0 " +
            " and b.store_id=#{storeId} and a.store_id = b.store_id and (a.status in (0,1,2,3) or (a.status = 5 and a.settlement_status = 0) )")
    int countUserUnfinishedOrder(int storeId, String userId) throws LaiKeAPIException;


    @Select("select count(1) from lkt_order as a,lkt_order_details b where a.sno = b.r_sno and b.p_id = #{goodsId} and a.recycle=0 and b.recycle=0 " +
            "and b.store_id=#{storeId} and a.otype = 'IN' and a.store_id = b.store_id and (a.status in (0,1,2,3) or (a.status = 5 and a.settlement_status = 0) )")
    int countUserUnfinishedOrderByGoodsId(int storeId, int goodsId) throws LaiKeAPIException;

    //订单除了当前商品未完成的商品数量 >0则表示还有其它未完成的订单
    @Select("select count(1) from lkt_order_details where r_sno=#{sno} and  and r_status not in(5,7) and id<> #{detailId}")
    int orderIsSettlement(int storeId, String sno, int detailId);

    /**
     * 统计店铺总销售额
     *
     * @param storeId -
     * @param mchId   -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/28 10:58
     */
    @Select("<script>" +
            "SELECT IFNULL(SUM(z_price), 0) FROM `lkt_order` WHERE store_id = #{storeId}  and recycle = 0 " +
            " <if test='mchId != null '> " +
            " AND mch_id = CONCAT(',',#{mchId},',') " +
            " </if> " +
            " AND status in(1,2,5,7) and z_price>0 and pay_time is not null " +
            " </script> ")
    BigDecimal countMchOrderSale(int storeId, Integer mchId) throws LaiKeAPIException;


    /**
     * 统计店铺总支付订单量
     *
     * @param storeId -
     * @param mchId   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/28 10:58
     */
    @Select("<script>" +
            "SELECT IFNULL(count(id), 0) FROM `lkt_order` WHERE store_id = #{storeId}  and recycle = 0 " +
            " <if test='mchId != null '> " +
            " AND mch_id = CONCAT(',',#{mchId},',') " +
            " </if> " +
            " AND status in(1,2,5,7) and z_price>0 and pay_time is not null " +
            " </script> ")
    int countMchOrderNum(int storeId, Integer mchId) throws LaiKeAPIException;


    /**
     * 订单报表
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 17:25
     */
    List<Map<String, Object>> selectOrdersReportDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 是否可以结算
     */
    @Select("select count(1) from lkt_order where store_id=#{storeId} and sno=#{orderNo} and settlement_status=0 and recycle=0 and status>0")
    int isSettement(int storeId, String orderNo) throws LaiKeAPIException;

    /**
     * 统计订单数量
     *
     * @param storeId   -
     * @param mchId     -
     * @param startDate -
     * @param endDate   -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/17 13:40
     */
    @Select("<script>" +
            "SELECT DATE_FORMAT(add_time,'%Y-%m') rdate,IFNULL(count(distinct id),0) sum FROM `lkt_order` WHERE store_id = #{storeId} " +
            " <if test='mchId != null '> " +
            " AND mch_id = CONCAT('%,',#{mchId},',%') " +
            " </if> " +
            " AND add_time <![CDATA[  >=  ]]> #{startDate} and add_time <![CDATA[  <=  ]]> #{endDate} " +
            " AND status in(1,2,5,7) and z_price>0 and pay_time is not null " +
            " and recycle = 0 " +
            " group by DATE_FORMAT(add_time,'%Y-%m')" +
            " </script> ")
    List<Map<String, Object>> getOrdersNumReportByMonth(int storeId, Integer mchId, String startDate, String endDate) throws LaiKeAPIException;

    @Select("<script>" +
            "SELECT DATE_FORMAT(add_time,'%Y-%m-%d') rdate,IFNULL(count(distinct id),0) sum FROM `lkt_order` WHERE store_id = #{storeId} " +
            " <if test='mchId != null '> " +
            " AND mch_id = CONCAT('%,',#{mchId},',%') " +
            " </if> " +
            " AND add_time <![CDATA[  >=  ]]> #{startDate} and add_time <![CDATA[  <=  ]]> #{endDate} " +
            " AND status in(1,2,5,7) and z_price>0 and pay_time is not null " +
            " and recycle = 0 " +
            " group by DATE_FORMAT(add_time,'%Y-%m-%d') " +
            " </script> ")
    List<Map<String, Object>> getOrdersNumReportByDay(int storeId, Integer mchId, String startDate, String endDate) throws LaiKeAPIException;


    /**
     * 管理后台订单列表 - 订单信息
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> adminOrderList(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取全部已结算/待结算的金额
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    BigDecimal getOrderAllPrice(Map<String, Object> map) throws LaiKeAPIException;

    int countAdminOrderList(Map<String, Object> map);

    /**
     * 管理后台订单总数 -
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    int getAdminOrderCount(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 管理后台订单列表 - 商品信息
     *
     * @return
     */
    List<Map<String, Object>> adminOrderListProductInfo(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取订单发货列表信息
     *
     * @param storeId
     * @param orderNo
     * @return
     */
    List<Map<String, Object>> getDeleiveryOrders(int storeId, String orderNo) throws LaiKeAPIException;


    /**
     * 根据订单详情id 获取订单信息
     *
     * @param storeId
     * @param orderDetailsId
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getDeliveryOrderInfo(int storeId, String orderDetailsId) throws LaiKeAPIException;

    /**
     * 获取一次性消费金额【非礼包商品的订单】
     * 【如果是礼包则直接晋升】
     *
     * @param storeId    -
     * @param userId     -
     * @param orderNo    -
     * @param statusList -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/19 14:22
     */
    @Select("<script>" +
            " select a.z_price from lkt_order a,lkt_order_details b,lkt_distribution_goods c where a.store_id = #{storeId} " +
            " and a.sno = b.r_sno " +
            " and b.sid = c.s_id " +
            " and c.recycle = 0 " +
            " and c.uplevel=0 " +
            " and a.user_id=#{userId} " +
            " <if test='orderNo != null '> " +
            " and a.sno=#{orderNo} " +
            " </if> " +
            " <if test='statusList != null '> " +
            "   <foreach collection=\"statusList\" item=\"status\" separator=\",\" open=\"and a.status in(\" close=\")\"> " +
            "        #{status,jdbcType=INTEGER}" +
            "   </foreach> </if> " +
            " order by a.add_time desc limit 1" +
            "</script>")
    BigDecimal getDistributionPrice(int storeId, String userId, String orderNo, List<Integer> statusList) throws LaiKeAPIException;


    List<Map<String, Object>> getOrderInfoListUser(Map<String, Object> map) throws LaiKeAPIException;

    int countOrderInfoListUser(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 更具日期跨度获取店铺每天订单数量
     *
     * @param storeId   -
     * @param mchId     -
     * @param startDate -
     * @param endDate   -
     * @return List
     * @author Trick
     * @date 2021/5/28 11:31
     */
   /* @Select("<script>" +
            "SELECT DATE_FORMAT(add_time,'%m-%d') time,count(1) value FROM `lkt_order` WHERE store_id = #{storeId}  and recycle = 0 " +
            " <if test='mchId != null '> " +
            "AND mch_id = CONCAT(',',#{mchId},',') " +
            " </if> " +
            "and STATUS IN(1,2,5,7) and z_price>0 and pay_time is not null " +
            " and DATE_FORMAT(add_time,'%Y-%m-%d') <![CDATA[  >=  ]]>#{startDate} and DATE_FORMAT(add_time,'%Y-%m-%d') <![CDATA[  <=  ]]> #{endDate} " +
            " group by DATE_FORMAT(add_time,'%Y-%m-%d')" +
            " </script> ")*/
    List<Map<String, String>> getOrderNumByDay(@Param("storeId") int storeId, @Param("mchId") Integer mchId, @Param("startDate") String startDate, @Param("endDate") String endDate);


    /**
     * 统计成功支付的用户量
     *
     * @param storeId -
     * @param mchId   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/28 13:49
     */
    @Select("select count(distinct a.user_id) from lkt_order as a INNER JOIN   lkt_user as b on a.user_id = b.user_id and a.status in (1,2,5,7) and a.z_price>0 and a.pay_time is not null " +
            " AND mch_id = CONCAT(',',#{mchId},',') and b.store_id = #{storeId} and a.recycle = 0")
    int countOrderUserNum(int storeId, int mchId) throws LaiKeAPIException;

    /**
     * 获取最早下单的订单的日期
     *
     * @param mchId -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/28 13:49
     */
    @Select("select add_time from lkt_order where  CONVERT(REPLACE(mch_id, ',', ''),unsigned INTEGER) = #{mchId}  and recycle = 0 order by add_time limit 0,1")
    String getOldOrderTime(int mchId);


    /**
     * 更具订单时间统计已支付的订单金额
     *
     * @param storeId   -
     * @param mchId     -
     * @param startDate -
     * @param endDate   -
     * @return BigDecimal
     * @author Trick
     * @date 2021/5/28 15:48
     */
    @Select("<script>" +
            "select IFNULL(SUM(z_price),0)  from lkt_order where store_id = #{storeId}  and recycle = 0" +
            " <if test='mchId != null '> " +
            "AND CONVERT(REPLACE(mch_id, ',', ''),unsigned INTEGER) = #{mchId} " +
            " </if> " +
            "and STATUS IN(1,2,5,7) and pay_time is not null " +
            "<if test='startDate != null '> " +
            "   and add_time <![CDATA[  >=  ]]> #{startDate}" +
            "</if>" +
            "<if test='endDate != null '> " +
            "   and add_time <![CDATA[  <=  ]]> #{endDate}" +
            "</if>" +
            "</script>")
    BigDecimal sumSaleByDate(int storeId, Integer mchId, Date startDate, Date endDate);


    /**
     * 更具订单时间统计已支付的用户数量
     *
     * @param storeId   -
     * @param mchId     -
     * @param startDate -
     * @param endDate   -
     * @return BigDecimal
     * @author Trick
     * @date 2021/5/28 15:48
     */
    @Select("<script>" +
            "select IFNULL(count(id),0)  from lkt_order where store_id = #{storeId} and recycle = 0" +
            " <if test='mchId != null '> " +
            "AND CONVERT(REPLACE(mch_id, ',', ''),unsigned INTEGER) = #{mchId}" +
            " </if> " +
            " and STATUS IN(1,2,5,7) and z_price>0 and pay_time is not null " +
            "<if test='startDate != null '> " +
            "   and add_time <![CDATA[  >=  ]]> #{startDate}" +
            "</if>" +
            "<if test='endDate != null '> " +
            "   and add_time <![CDATA[  <=  ]]> #{endDate}" +
            "</if>" +
            "</script>")
    BigDecimal sumSaleNumByDate(int storeId, Integer mchId, Date startDate, Date endDate);


    /**
     * 统计某天每小时销售额
     *
     * @param storeId -
     * @param mchId   -
     * @param date    -
     * @return BigDecimal
     * @author Trick
     * @date 2021/5/28 16:30
     */
   /* @Select("<script>" +
            "SELECT CONCAT('',HOUR(add_time),'时') time,IFNULL(SUM(z_price),0) value FROM `lkt_order` WHERE store_id = #{storeId}  and recycle = 0 " +
            " <if test='mchId != null '> " +
            "AND mch_id = CONCAT(',',#{mchId},',')" +
            " </if> " +
            " AND TO_DAYS(add_time) = TO_DAYS(#{date}) AND STATUS IN(1, 2,5,7) AND pay_time is not null group by HOUR(add_time) " +
            "</script>")*/
    List<Map<String, BigDecimal>> getSaleNumByHour(@Param("storeId") int storeId, @Param("mchId") Integer mchId, @Param("date") Date date);


    /**
     * 统计某月每天销售额
     *
     * @param storeId   -
     * @param mchId     -
     * @param startDate -
     * @param endDate   -
     * @return BigDecimal
     * @author Trick
     * @date 2021/5/28 16:30
     */
   /* @Select("<script>" +
            "SELECT DATE_FORMAT(add_time,'%m/%d') time,IFNULL(SUM(z_price),0) value FROM `lkt_order` WHERE store_id = #{storeId}  and recycle = 0 " +
            " <if test='mchId != null '> " +
            "AND mch_id = CONCAT(',',#{mchId},',') " +
            " </if> " +
            " AND add_time <![CDATA[  >=  ]]> #{startDate} and add_time <![CDATA[  <  ]]> #{endDate} AND STATUS IN(1, 2,5,7) and pay_time is not null group by DATE_FORMAT(add_time,'%Y-%m-%d')" +
            "</script>")*/
    List<Map<String, BigDecimal>> getSaleNumByMonth(@Param("storeId") int storeId, @Param("mchId") Integer mchId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);


    /**
     * pc-店铺订单列表
     *
     * @param map -
     * @return List
     * @author Trick
     * @date 2021/6/3 10:07
     */
    List<Map<String, Object>> selectbMchOrderIndex(Map<String, Object> map);

    int countbMchOrderIndex(Map<String, Object> map);

    Map<String, Object> sumbMchOrderIndexByPrice(Map<String, Object> map);

    //关闭订单
    @Update("update lkt_order a inner join lkt_order_details b on a.sno=b.r_sno set a.status=7,b.r_status=7 where a.sno=#{orderNo} and a.store_id=#{storeId} ")
    int closeOrder(int storeId, String orderNo);

    /**
     * 根据支付时间统计已完成的订单金额
     *
     * @param mchId     -
     * @param startDate -
     * @param endDate   -
     * @return BigDecimal
     * @author Trick
     * @date 2022/7/20 19:47
     */
    @Select("select ifnull(sum(z_price),0) from lkt_order where mch_id=concat(',',#{mchId},',') and status in(5) and pay_time >=#{startDate} and pay_time<=#{endDate} and recycle=0")
    BigDecimal mchToDayMoney(int mchId, String startDate, String endDate);

    //获取订单收货时间
    @Select("select arrive_time from lkt_order_details where r_sNo=#{orderNo} and store_id=#{storeId} and r_status>=2 and r_status!=7 and recycle=0 order by arrive_time desc  limit 1")
    Date getOrderArriveTime(int storeId, String orderNo);

    @Select("SELECT o.z_price,o.z_freight ,o.pay_time FROM lkt_order AS o " +
            "LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id " +
            "RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo " +
            "RIGHT JOIN lkt_configure attr ON attr.id = d.sid " +
            "RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid " +
            "RIGHT JOIN lkt_mch AS m ON p.mch_id = m.id  WHERE o.store_id = #{storeId} AND o.recycle = 0 AND o.settlement_status = #{settlementStatus} AND o.`status` in (5,7) AND o.mch_id = concat(',',#{mchId},',') AND ( p.gongyingshang IS NULL OR p.gongyingshang = '' )")
    List<OrderModel> settlementPrice(Map<String, Object> map);


    @Select("SELECT ifnull(SUM(o.z_price), 0) z_price,ifnull(SUM(o.z_freight), 0) z_freight FROM lkt_order AS o " +
            "LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id " +
            "RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo " +
            "RIGHT JOIN lkt_configure attr ON attr.id = d.sid " +
            "RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid " +
            "RIGHT JOIN lkt_mch AS m ON p.mch_id = m.id  WHERE o.store_id = #{storeId} AND o.recycle = 0 AND o.settlement_status = #{settlementStatus} AND o.`status` in (5) AND o.mch_id = concat(',',#{mchId},',') AND ( p.gongyingshang IS NULL OR p.gongyingshang = '' ) and o.otype='ZB'")
    Map<String, Object> settlementZbPrice(Map<String, Object> map);

    /**
     * 统计供应商代发货订单
     *
     * @param storeId
     * @param supplierId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("SELECT COUNT(*) FROM lkt_order o RIGHT JOIN lkt_order_details d on o.sNo = d.r_sNo RIGHT JOIN lkt_product_list p ON d.p_id = p.id WHERE o.store_id = #{storeId} AND o.recycle = 0 AND d.recycle = 0 AND p.recycle = 0 AND o.`status` IN (1,2) AND p.gongyingshang = #{supplierId}")
    int countForSupplier(int storeId, String supplierId) throws LaiKeAPIException;


    List<Map<String, Object>> countByUser(int storeid, Date date);

    List<Map<String, Object>> countByUser1(int storeid);

    @Select("select  count(1) num,status from  lkt_order where   store_id=#{storeid} group by  status ")
    List<Map<String, Object>> getReportData(int storeid);


    @Select(" select count(distinct o.sNo)\n" +
            "        from lkt_order as o\n" +
            "        LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id " +
            "        right join lkt_order_details as d on o.sNo = d.r_sNo\n" +
            "        right join lkt_product_list as p on p.id = d.p_id " +
            "   where o.store_id=#{storeid} and lu.store_id = #{storeid} and (p.gongyingshang is null or p.gongyingshang = '') " +
            "   and DATE_FORMAT(o.add_time,'%Y-%m-%d')=#{date} and o.otype='GM' and o.recycle = 0")
    Integer countByDate(int storeid, String date);


    @Select(" select count(distinct o.sNo)\n" +
            "        from lkt_order as o\n" +
            "        LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id " +
            "        right join lkt_order_details as d on o.sNo = d.r_sNo\n" +
            "        right join lkt_product_list as p on p.id = d.p_id " +
            "   where o.store_id=#{storeid} and  lu.store_id = #{storeid} and (p.gongyingshang is null or p.gongyingshang = '') " +
            "   and DATE_FORMAT(o.add_time,'%Y-%m')=#{date} and o.otype='GM' and o.recycle = 0")
    Integer countByModel(int storeid, String date);

    @Select("SELECT " +
            "IFNULL(SUM( o.z_price ) ,0) " +
            "FROM lkt_order o " +
            "WHERE " +
            "o.sNo  IN(" +
            "SELECT" +
            " DISTINCT o.sNo " +
            "FROM " +
            "lkt_order AS o " +
            "LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id " +
            "RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo " +
            "RIGHT JOIN lkt_configure attr ON attr.id = d.sid " +
            "RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid " +
            "WHERE " +
            "o.store_id = #{storeid} " +
            "AND lu.store_id = #{storeid} " +
            "AND o.recycle != 1 " +
            "AND o.otype IN ( 'GM' ) " +
            "AND ( p.gongyingshang IS NULL OR p.gongyingshang = '' ) " +
            ") and status != 0  and DATE_FORMAT(add_time,'%Y-%m-%d')=#{date}")
    BigDecimal sumByDate(int storeid, String date);

    @Select("SELECT " +
            "IFNULL(SUM( o.z_price ) ,0) " +
            "FROM lkt_order o " +
            "WHERE " +
            "o.sNo  IN( " +
            "SELECT " +
            " DISTINCT o.sNo " +
            "FROM " +
            "lkt_order AS o " +
            "LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id " +
            "RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo " +
            "RIGHT JOIN lkt_configure attr ON attr.id = d.sid " +
            "RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid " +
            "WHERE " +
            "o.store_id = #{storeid} " +
            "AND lu.store_id = #{storeid} " +
            "AND o.recycle != 1 " +
            "AND o.otype IN ( 'GM' ) " +
            "AND ( p.gongyingshang IS NULL OR p.gongyingshang = '' )" +
            ") and status != 0  and DATE_FORMAT(add_time,'%Y-%m')=#{date} ")
    BigDecimal sumByModel(int storeid, String date);


    @Select("SELECT " +
            "count( DISTINCT o.sNo ) " +
            "FROM " +
            "lkt_order AS o " +
            "LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id " +
            "RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo " +
            "RIGHT JOIN lkt_configure attr ON attr.id = d.sid " +
            "RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid " +
            "RIGHT JOIN lkt_mch AS m ON p.mch_id = m.id " +
            "WHERE " +
            "o.store_id = #{storeid} " +
            "AND lu.store_id = #{storeid} " +
            "AND o.recycle != 1 " +
            "AND o.otype IN ( 'GM' ) " +
            "AND ( p.gongyingshang IS NULL OR p.gongyingshang = '' )")
    int countAllOrder(int storeid);


    @Select("select  count(1) num from  lkt_order where   store_id=#{storeId} AND DATE_FORMAT(pay_time,\"%Y-%m-%d\")=#{date}   group by  DATE_FORMAT(pay_time,\"%Y-%m-%d\") ")
    Integer getOrderNumByDate(int storeId, String date);

    @Select("select  count(1) num from  lkt_order where   store_id=#{storeId} AND DATE_FORMAT(pay_time,\"%Y-%m\")=#{date}   group by  DATE_FORMAT(pay_time,\"%Y-%m\") ")
    Integer getOrderNumByMonth(int storeId, String date);

    /**
     * 日订单总额
     *
     * @param storeid
     * @param date
     * @return
     */
    BigDecimal getCurrentVolume(int storeid, String date);

    /**
     * 月交易
     *
     * @param storeid
     * @param date
     * @return
     */
    BigDecimal getCurrentVolumeByMonth(int storeid, String date);

    /**
     * 总交易不包括退款
     *
     * @param storeid
     * @return
     */
    BigDecimal sumAllMoeny(int storeid);

    @Select("SELECT " +
            "IFNULL(SUM( o.z_price ) ,0) " +
            "FROM lkt_order o " +
            "WHERE " +
            "o.sNo  IN( " +
            "SELECT " +
            " DISTINCT o.sNo " +
            "FROM " +
            "lkt_order AS o " +
            "LEFT JOIN lkt_user AS lu ON o.user_id = lu.user_id " +
            "RIGHT JOIN lkt_order_details AS d ON o.sNo = d.r_sNo " +
            "RIGHT JOIN lkt_configure attr ON attr.id = d.sid " +
            "RIGHT JOIN lkt_product_list AS p ON p.id = attr.pid " +
            "WHERE " +
            "o.store_id = #{storeid} " +
            "AND lu.store_id = #{storeid} " +
            "AND o.recycle != 1 " +
            "AND o.otype IN ( 'GM' ) " +
            "AND ( p.gongyingshang IS NULL OR p.gongyingshang = '' ) " +
            ") and status != 0")
    BigDecimal AllMoeny(int storeid);

    /**
     * @param storeid
     * @param status
     * @param startDate
     * @param endDate
     * @return
     */
    Integer getOrderNumByPeriod(int storeid, int status, String startDate, String endDate);

    /**
     * 店铺交易金额
     *
     * @param storeid
     * @param startDate
     * @return
     */
    List<Map<String, Object>> getChargeMoney(Integer storeid, String startDate);

    /**
     * 商品指标---已售商品数量
     *
     * @param param
     * @return
     */
    Long getSaledGoodsNum(Map<String, Object> param);

    /**
     * 根据调起支付所用订单号获取主订单号(未支付先拆单，子订单调起支付所用订单号和总订单相同)
     *
     * @param realSno
     * @return
     */
    @Select("SELECT sNo from lkt_order where real_sno = #{realSno} and (p_sNo = '' or p_sNo is null)")
    String getOrderByRealSno(String realSno);

    @Select("SELECT real_sno from lkt_order where sNo = #{sNo}")
    String getOrderBySno(String sNo);

    /**
     * 根据调起支付所用订单号获取订单号，先拆单后支付
     *
     * @param realSno
     * @return
     */
    @Select("SELECT sNo from lkt_order where real_sno = #{realSno}")
    String getOrdersNoByRealSno(String realSno);

    /**
     * 根据调起支付所用订单号获取订单号，先拆单后支付
     *
     * @param realSno
     * @return
     */
    @Select("SELECT sNo from lkt_order where real_sno = #{realSno}")
    String getOrderMchIdAndPNameBySno(Integer storeId, String sNo);

    /**
     * pc门店核销获取订单列表
     *
     * @param paramMap -
     * @return
     */
    List<Map<String, Object>> getMchSonOrderList(Map<String, Object> paramMap);


    /**
     * pc门店核销获取订单列表
     *
     * @param paramMap -
     * @return
     */
    Integer getMchSonOrderCount(Map<String, Object> paramMap);

    /**
     * 虚拟订单pc门店核销获取订单列表
     *
     * @param paramMap -
     * @return
     */
    List<Map<String, Object>> getMchSonOrderCountVI(Map<String, Object> paramMap);

    /**
     * 获取已发货状态支付订单
     *
     * @param storeId
     * @return
     */
    @Select("select real_sno from lkt_order where store_id = #{storeId} and wx_order_status = 2 group by real_sno")
    List<Map<String, Object>> getRealSnoByStatusAndWxOrderStatus(Integer storeId);

    /**
     * 修改微信订单状态
     *
     * @param storeId
     * @param realSno
     * @param wxOrderStatus
     * @return
     */
    @Select("update lkt_order set wx_order_status = #{wxOrderStatus} where store_id = #{storeId} and real_sno = #{realSno}")
    List<Map<String, Object>> UpdateWxOrderStatusByRealSno(Integer storeId, String realSno, Integer wxOrderStatus);

    List<Map<String, Object>> cleanVIWrite(Map<String, Object> paramMap);

    @Select("select * from lkt_order where sNo = #{sNo}")
    OrderModel selectBySno(String sNo);

    @Select("select count(1) from lkt_order where store_id = #{storeId} and mch_id=CONCAT(',',#{mchId},',')  and user_id=#{userId}")
    int countNewOrderNum(Integer storeId, String mchId, String userId);

    @Update("update lkt_order set transaction_id = #{transaction_id},dividend_status=#{status} where sNo=#{sno}")
    int updateIdAndStatusBySno(String sno, String transaction_id, Integer status);

    /**
     * 更新订单的paypal_id
     *
     * @param sNo
     * @param paypal_id
     * @return
     */
    @Update("UPDATE lkt_order SET paypal_id = #{paypal_id} WHERE sNo = #{sNo}")
    int updatePaypalIdBySno(String sNo, String paypal_id);

    /**
     * 更新订单的stripe_id
     *
     * @param sNo
     * @param stripe_id
     * @return
     */
    @Update("UPDATE lkt_order SET stripe_id = #{stripe_id} WHERE sNo = #{sNo}")
    int updateStripeIdBySno(String sNo, String stripe_id);

    /**
     * 获取核销码
     * @param sNo
     * @return
     */
    @Select("select extraction_code from lkt_order where sNo = #{sNo}")
    String getWriteCode(String sNo);

    /**
     * 根据订单id获取订单编号
     * @param orderId
     * @return
     */
    @Select("select sNo from lkt_order where id = #{orderId}")
    String getsNoByOrderId(Integer orderId);

    /**
     * 获取订单状态
     * @param sNo
     * @return
     */
    @Select("select status from lkt_order where sNo = #{sNo}")
    Integer getStatusByOrderNo(String sNo);

    /**
     * 更新订单的stripe_payment_intent
     *
     * @param sNo
     * @param stripePaymentIntent
     * @return
     */
    @Update("UPDATE lkt_order SET stripe_payment_intent = #{stripePaymentIntent} WHERE sNo = #{sNo}")
    int updateStripePaymentIntentBySno(String sNo, String stripePaymentIntent);

    List<Map<String, Object>> statistics(Map<String, Object> paramMap1);

    List<OrderModel> selectByIds(@Param("ids") List<Integer> ids);
}
