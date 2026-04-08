package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.coupon.CouponActivityModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 店铺卡卷活动 sql
 *
 * @author Trick
 * @date 2020/10/14 16:18
 */
public interface CouponActivityModelMapper extends BaseMapper<CouponActivityModel>
{


    /**
     * 获取某商城所有 开启、未启用、禁用  优惠卷
     *
     * @param couponActivityModel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/14 16:19
     */
    @Select("select id,status,end_time,start_time from lkt_coupon_activity where store_id = #{store_id} and recycle = #{recycle} and status in(0,1,2) order by start_time desc")
    List<CouponActivityModel> getCouponActivityAll(CouponActivityModel couponActivityModel) throws LaiKeAPIException;


    /**
     * 插件状态修改
     *
     * @param couponActivityModel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/14 16:33
     */
    @Update("update lkt_coupon_activity set status = #{status} where id = #{id}")
    int updateSwitchCouponActivity(CouponActivityModel couponActivityModel) throws LaiKeAPIException;


    /**
     * 优惠卷数量修改
     *
     * @param id  -
     * @param num -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/8 9:54
     */
    @Update("update lkt_coupon_activity set num = num - #{num} where id = #{id}")
    int updateCouponByNum(int id, int num) throws LaiKeAPIException;

    /**
     * 获取优惠卷活动
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 18:04
     */
    List<Map<String, Object>> getCouponActivityDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 动态统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/8 18:31
     */
    int countCouponActivityDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 清理指定过期的活动
     *
     * @param storeId -
     * @param sysDate -
     * @param day     -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/11 14:35
     */
    @Update("update lkt_coupon_activity set recycle = 1 where store_id = #{storeId} " +
            " and status = 3 and end_time is not null and SYSDATE() >= DATE_ADD(end_time,INTERVAL #{day} day)")
    int delActivity(int storeId, Date sysDate, int day) throws LaiKeAPIException;

    /**
     * 获取
     *
     * @param storeId
     * @param couponId
     * @return
     */
    @Select("select a.activity_type,a.discount from lkt_coupon_activity as a " +
            "left join lkt_coupon as b on a.id = b.hid where a.store_id = #{storeId} and b.id = #{couponId} ")
    CouponActivityModel getOneCouponActivity(int storeId, int couponId);

    /**
     * 删除店铺优惠券
     */
    @Update("update lkt_coupon_activity a inner join lkt_coupon b on a.id=b.hid set a.recycle=1,b.recycle=1 where a.store_id=#{storeId} and end_time is not null and a.mch_id=#{mchId} and a.recycle=0 and b.recycle=0")
    int delCouponByMchId(int storeId, int mchId);

    /**
     * 获取可以更新状态的数据
     * add by trick 2023-02-08 10:28:52
     */
    @Select("select * from lkt_coupon_activity where end_time is not null and status!=3 and recycle=0")
    List<CouponActivityModel> getCouponActivityByNotEnd();

    /**
     * 获取系统发放优惠券活动列表
     *
     * @return List
     */
    @Select("select * FROM lkt_coupon_activity a where a.recycle=0 and a.status=1 and a.receive_type=1 and(a.end_time>#{sysDate} or a.end_time is null ) and a.is_auto_push=0 and a.store_id=#{storeId} ")
    List<CouponActivityModel> getAutoPushActiveList(int storeId, Date sysDate);


    @Update("<script> " +
            "update lkt_coupon_activity a set a.is_display=#{isDisplay} where a.store_id=#{storeId} and a.recycle=0 and mch_id!=0 " +
            "<if test='mchId != null and mchId!=0'>" +
            "  and a.mch_id=#{mchId} " +
            "</if>" +
            " </script> ")
    int setCouponDisplayByMchId(int storeId, int mchId, int isDisplay);
}