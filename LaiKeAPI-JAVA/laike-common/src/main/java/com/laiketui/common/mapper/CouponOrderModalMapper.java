package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.coupon.CouponOrderModal;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


/**
 * 优惠券订单
 *
 * @author wangxian
 */
public interface CouponOrderModalMapper extends BaseMapper<CouponOrderModal>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 更新优惠券订单信息
     *
     * @param storeId
     * @param userId
     * @param couponId
     * @return
     */
    @Update("update lkt_coupon_sno set recycle = 1 where store_id = #{storeId} and user_id = #{userId} and coupon_id =#{couponId} ")
    int updateCouponOrder(int storeId, String userId, int couponId);


    /**
     * 根据订单号回收优惠券关联记录
     *
     * @param storeId -
     * @param orderNo -
     * @return int
     * @author Trick
     * @date 2022/6/29 17:33
     */
    @Update("update lkt_coupon_sno set recycle = 1 where store_id = #{storeId} and sno=#{orderNo} ")
    int delCouponOrder(int storeId, String orderNo);

    /**
     * 根据订单号将拆单时的优惠券关联记录
     *
     * @param storeId -
     * @param orderNo -
     * @return int
     * @author Trick
     * @date 2022/6/29 17:33
     */
    @Update("update lkt_coupon_sno set recycle = 0 where store_id = #{storeId} and sno=#{orderNo} and user_id=#{userId}")
    int reBackCouponOrder(int storeId, String orderNo, String userId);

    //获取用户优惠券统计信息
    @Select("select count(a.hid) receiveNum from lkt_coupon a where a.user_id=#{userId} and a.hid=#{hid}")
    Map<String, Integer> getUserCouponCount(String userId, Integer hid);

    //获取用户优惠券统计信息
    @Select("select count(x.hid) as useNum from lkt_coupon x LEFT JOIN lkt_coupon_sno s on x.id = s.coupon_id  where  x.user_id = #{userId} and x.hid=#{hid}  and s.recycle = 0")
    Map<String, Integer> getUserCouponCount2(String userId, Integer hid);

    //统计多少人领取了店铺发布的券
    @Select("select count(distinct user_id) from lkt_coupon a where a.hid=#{hid} and a.recycle=0 and a.free_or_not=0 ")
    int countUserNum(Integer hid);

    //管理后台统计多少人领取了店铺发布的券
    @Select("select count(distinct user_id) from lkt_coupon a where a.hid=#{hid} and a.recycle=0 and a.free_or_not=0 ")
    int adminCountUserNum(Integer hid);

}
