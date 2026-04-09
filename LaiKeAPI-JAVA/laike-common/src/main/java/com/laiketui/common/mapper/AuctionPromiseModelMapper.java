package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.auction.AuctionPromiseModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 竞拍保证金
 *
 * @author Trick
 * @date 2022/7/12 14:42
 */
public interface AuctionPromiseModelMapper extends BaseMapper<AuctionPromiseModel>
{

    /**
     * 专场是否支付了保证金
     */
    @Select("select count(1) from lkt_auction_promise a where a.is_pay=1 and a.is_back is null and a.store_id=#{storeId} and a.special_id=#{specialId} and a.user_id=#{userId}")
    int isPayPromiseByAcId(int storeId, String specialId, String userId);

    /**
     * 竞拍支付保证金
     *
     * @param storeId -
     * @param orderNo -
     * @param isPay   -
     * @return int
     * @author Trick
     * @date 2022-07-18 14:20:43
     */
    @Update("update lkt_auction_promise set is_pay = #{isPay} where store_id = #{storeId} and trade_no = #{orderNo} ")
    int updateStatus(int storeId, String orderNo, int isPay);

    /**
     * 竞拍押金置为，暂不退款，只有付款后，才置为可退款，并进行退款
     *
     * @param storeId -
     * @param aid     -
     * @param userId  -
     * @param status  -0 未达到退款标准
     * @return int
     * @author Trick
     * @date 2021/4/24 10:45
     */
    @Update("update lkt_auction_promise set allow_back = #{status} where store_id = #{storeId} and a_id = #{aid} and user_id = #{userId}")
    int updateAllowBack(int storeId, int aid, String userId, int status);


    /**
     * 获取保证金记录列表
     *
     * @param map -
     * @return int
     * @author Trick
     * @date 2022-07-12 14:42:35
     */
    List<Map<String, Object>> selectAuctionPromiseList(Map<String, Object> map);

    int countAuctionPromiseList(Map<String, Object> map);

    /**
     * 获取用户可以退还保证金的信息
     */
    @Select("select * from lkt_auction_promise where store_id=#{storeId} and special_id=#{specialId} and is_pay=1 and back_time is null and is_deduction=0 ")
    List<AuctionPromiseModel> selectAuctionPromiseUserIdList(int storeId, String specialId);

    /**
     * 退款
     */
    //@Update("update lkt_auction_promise a set a.is_back=1 where store_id=#{storeId} and special_id=#{specialId} and a.trade_no =#{orderNo} and is_pay=1 and is_back is null  ")
    @Update("update lkt_auction_promise a set a.back_time =#{back_time} where store_id=#{storeId} and special_id=#{specialId} and a.trade_no =#{orderNo} and is_pay=1 and is_back is null  ")
    int refundPromise(int storeId, String specialId, String orderNo, Date back_time);

    /**
     * 扣除保证金
     */
    @Update("update lkt_auction_promise a set a.back_time=#{back_time} where id=#{id} and is_pay=1 and is_back is null  ")
    int deductionPromise(int id, Date back_time);

    /**
     * 是否可以退还保证金
     */
    @Select("select count(1) from lkt_order a inner join lkt_auction_product b on a.sno=b.sno inner join lkt_auction_session c on c.id=b.session_id " +
            " where a.store_id=#{storeId} and a.otype='JP' and a.status in(0,7) and a.user_id=#{userId}  " +
            " and c.special_id=#{specialId} ")
    int isRefundPromise(int storeId, String specialId, String userId);

    /**
     * 获取需要扣除保证金的信息
     */
    @Select("select distinct b.user_id,d.id specialId from lkt_auction_product b inner join lkt_auction_session c on b.session_id=c.id inner join lkt_auction_special d on c.special_id=d.id " +
            " where d.id=#{specialId} and b.status=3 and b.sno is not null LIMIT 1")
    Map<String, Object> isDeductionPromise(String specialId);
}