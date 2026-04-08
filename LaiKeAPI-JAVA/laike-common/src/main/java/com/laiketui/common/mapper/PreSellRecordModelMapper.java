package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.presell.PreSellRecordModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PreSellRecordModelMapper extends BaseMapper<PreSellRecordModel>
{

    /**
     * 查询预售订单列表
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getSellOrderList(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询预售订单列表数量
     *
     * @param map
     * @return
     */
    int countSellOrderList(Map<String, Object> map);

    /**
     * 预售待付款订单数量
     *
     * @param storeId
     * @param userId
     * @return
     */
    @Select("SELECT p.id FROM lkt_order o LEFT JOIN lkt_pre_sell_record p ON o.sNo = p.sNo WHERE o.store_id = #{storeId} AND o.user_id = #{userId} AND o.`status` = 0 AND o.otype = 'PS' AND p.is_pay = 0")
    List<Integer> countPending(int storeId, String userId);

    /**
     * 获取定时器需要更新的预售订单
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2020/01/14 15:00
     */
    List<Map<String, Object>> getTimerSellOrder(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取预售商品的预售订单
     *
     * @param productId -
     * @return List
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2020/01/14 15:00
     */
    @Select("SELECT p.* FROM lkt_pre_sell_record p LEFT JOIN lkt_order o ON p.sNo = o.sNo WHERE p.product_id = #{productId} AND o.`status` = 0")
    List<Map<String, Object>> getGoodOrder(Integer productId) throws LaiKeAPIException;

    /**
     * 根据订单号存入贝宝订单号
     *
     * @param sNo
     * @param orderId
     * @return
     * @throws LaiKeAPIException
     */
    @Update("UPDATE lkt_pre_sell_record " +
            "SET paypal_id = #{orderId} " +
            "WHERE sNo = #{sNo}")
    Integer updateBysNo(String sNo, String orderId) throws LaiKeAPIException;

    @Select("select paypal_id from lkt_pre_sell_record where sNo = #{sNo}")
    String getBySNo(String sNo) throws LaiKeAPIException;

    /**
     * 根据sNo获取List
     *
     * @param sNo
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select * from lkt_pre_sell_record where sNo = #{sNo}")
    List<Map<String, Object>> selectListBySNo(String sNo) throws LaiKeAPIException;

    /**
     * 根据订单号修改是否退款状态
     *
     * @param sNo
     * @param
     * @return
     * @throws LaiKeAPIException
     */
    @Update("UPDATE lkt_pre_sell_record SET is_refund = #{is_refund} WHERE sNo = #{sNo}")
    Integer updateIsRefundBySNo(String sNo, int is_refund) throws LaiKeAPIException;

    /**
     * 根据订单号和定金尾款类型查询PayPalID
     *
     * @param sNo
     * @param pay_type
     * @return
     * @throws LaiKeAPIException
     */
    @Select("SELECT paypal_id from lkt_pre_sell_record where sNo = #{sNo} and pay_type = #{pay_type}")
    String selectPaypalIdBySNoAndPayType(String sNo, int pay_type) throws LaiKeAPIException;

    /**
     * 根据订单号和定金尾款类型查询Stripe支付意图ID
     *
     * @param sNo
     * @param pay_type
     * @return
     * @throws LaiKeAPIException
     */
    @Select("SELECT stripe_payment_intent from lkt_pre_sell_record where sNo = #{sNo} and pay_type = #{pay_type}")
    String selectStripePaymentIntentBySNoAndPayType(String sNo, int pay_type) throws LaiKeAPIException;

    /**
     * 根据订单号和定金尾款类型更新PayPalID
     *
     * @param sNo
     * @param orderId
     * @param pay_type
     * @return
     * @throws LaiKeAPIException
     */
    @Update("UPDATE lkt_pre_sell_record " +
            "SET paypal_id = #{orderId} " +
            "WHERE sNo = #{sNo} and pay_type = #{pay_type}")
    Integer updatePaypalIdBySNoAndPayType(String sNo, String orderId, int pay_type) throws LaiKeAPIException;

    /**
     * 根据订单号和定金尾款类型更新StripeID
     *
     * @param sNo
     * @param pay_type
     * @return
     * @throws LaiKeAPIException
     */
    @Update("UPDATE lkt_pre_sell_record " +
            "SET stripe_id = #{stripeId} " +
            "WHERE sNo = #{sNo} and pay_type = #{pay_type}")
    Integer updateStripeIdBySNoAndPayType(String sNo, String stripeId, int pay_type) throws LaiKeAPIException;

    /**
     * 查询预售订单 定金
     *
     * @param sNo
     * @param pay_type
     * @return
     * @throws LaiKeAPIException
     */
    @Select("SELECT deposit from lkt_pre_sell_record where sNo = #{sNo} and pay_type = #{pay_type}")
    BigDecimal selectDepositIdBySNoAndPayType(String sNo, int pay_type) throws LaiKeAPIException;

    /**
     * 查询预售订单 尾款
     *
     * @param sNo
     * @param pay_type
     * @return
     * @throws LaiKeAPIException
     */
    @Select("SELECT balance from lkt_pre_sell_record where sNo = #{sNo} and pay_type = #{pay_type}")
    BigDecimal selectBalanceIdBySNoAndPayType(String sNo, int pay_type) throws LaiKeAPIException;

    /**
     * 根据订单号查询记录数  以区分预售的订货模式和定金模式
     *
     * @param sNo
     * @return
     * @throws LaiKeAPIException
     */
    @Select("SELECT COUNT(*) from lkt_pre_sell_record where sNo = #{sNo}")
    Integer selectCountBySNo(String sNo) throws LaiKeAPIException;

    /**
     * 根据订单号和支付类型更新Stripe支付意图ID
     *
     * @param sNo
     * @param stripePaymentIntent
     * @param i
     * @return
     */
    @Update("UPDATE lkt_pre_sell_record " +
            "SET stripe_payment_intent = #{stripePaymentIntent} " +
            "WHERE sNo = #{sNo} and pay_type = #{i}")
    int updateStripePaymentIntentBySNoAndPayType(String sNo, String stripePaymentIntent, int i);
}
