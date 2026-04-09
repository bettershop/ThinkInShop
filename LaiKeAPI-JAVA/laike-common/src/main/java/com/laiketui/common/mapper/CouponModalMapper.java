package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.coupon.CouponModal;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 优惠卷数据模型
 *
 * @author Trick
 * @date 2020/12/7 12:06
 */
public interface CouponModalMapper extends BaseMapper<CouponModal>
{
    /**
     * 获取用户可使用的优惠券
     *
     * @param map
     * @return
     */
    List<Map<String, Object>> getUsersCoupons(Map<String, Object> map);


    /**
     * 更新优惠券为已经使用：2 类型 0:未使用 1:使用中 2:已使用 3:已过期
     *
     * @param type
     * @param storeId
     * @param userId
     * @param id
     * @return
     */
    @Update("<script> " +
            "update lkt_coupon set type = #{type} where store_id = #{storeId} and id = #{id} " +
            "<if test='userId != \"\" and userId != null '> and user_id = #{userId} and recycle = 0 </if>" +
            "</script>")
    int updateCoupon(int type, int storeId, String userId, int id);

    /**
     * 更新优惠券为已经使用：2 类型 0:未使用 1:使用中 2:已使用 3:已过期
     *
     * @param type
     * @param storeId
     * @param userId
     * @param id
     * @return
     */
    @Update("<script> " +
            "update lkt_coupon set type = #{type},recycle = 0 where store_id = #{storeId} and id = #{id} " +
            "<if test='userId != \"\" and userId != null '> and user_id = #{userId} </if>" +
            "</script>")
    int updateCouponAndRecycle(int type, int storeId, String userId, int id);

    /**
     * 获取优惠券相关信息
     *
     * @param storeId
     * @param userId
     * @param id
     * @return
     */
    @Select("select b.mch_id,b.activity_type,b.type,b.money,b.discount,b.product_class_id,b.product_id from lkt_coupon as a " +
            "left join lkt_coupon_activity as b on a.hid = b.id where a.store_id = #{storeId} and a.user_id = #{userId} and a.id = #{id}")
    List<Map<String, Object>> getCouponsInfoList(int storeId, String userId, int id);


    /**
     * 获取优惠卷 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 12:08
     */
    List<Map<String, Object>> getCouponInfoListDynamic(Map<String, Object> map) throws LaiKeAPIException;

    int countCouponInfoListDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取优惠卷 动态sql
     * left join lkt_coupon_activity lkt_order lkt_user
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 11:13
     */
    List<Map<String, Object>> getUserCouponActivityDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态统计 sql
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 17:35
     */
    int countCouponDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 动态统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 11:43
     */
    int countUserCouponActivityDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 11:43
     */
    int countCouponActivityDynamicByUserId(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 删除指定过期的优惠卷
     *
     * @param storeId -
     * @param mchId   -
     * @param sysDate -
     * @param day     -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/11 14:10
     */
    @Update("update lkt_coupon set recycle = 1 where store_id = #{storeId} " +
            " and type in (2,3) and SYSDATE() >= DATE_ADD(expiry_time,INTERVAL 1 day) ")
    int delCoupon(int storeId, Date sysDate, int day) throws LaiKeAPIException;


    /**
     * 删除未使用的优惠卷
     *
     * @param storeId -
     * @param hid     -
     * @param mchId   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/25 16:02
     */
    @Update("<script> " +
            "update lkt_coupon set recycle = 1 where store_id = #{storeId} " +
            " and type in (0,2,3)" +
            " and hid=#{hid}" +
            "<if test='mchId != null '> and mch_id = #{mchId} </if>" +
            "</script>")
    int delNotUsedCoupon(int storeId, int hid, Integer mchId) throws LaiKeAPIException;


    /**
     * 根据活动动id修改优惠卷状态
     *
     * @param type -
     * @param hid  -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/11 14:52
     */
    @Update("update lkt_coupon set type = #{type} where hid = #{hid} ")
    int updateCouponByHid(int type, int hid) throws LaiKeAPIException;


    /**
     * 获取所有 未使用 使用中的优惠卷
     *
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/11 15:01
     */
    @Select("select * from lkt_coupon where type in (0,1) and recycle=0 ")
    List<CouponModal> getEffectiveCouponInfoList() throws LaiKeAPIException;

    /**
     * 查询用户可用会员优惠券
     *
     * @param storeId
     * @param userId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("SELECT c.id,c.user_id,c.type,c.hid,a.store_id, a.mch_id, a.`name`,a.activity_type,a.grade_id,a.money,a.discount,a.z_money,a.shopping,a.type couponType " +
            "FROM `lkt_coupon` c LEFT JOIN lkt_coupon_activity a ON c.hid = a.id " +
            "WHERE c.user_id = #{userId} AND c.type = 0 AND c.recycle = 0 AND a.store_id = #{storeId} AND a.type = 4 AND a.`status` = 1 AND a.recycle = 0")
    List<Map<String, Object>> getUserCoupon(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 给用户赠送过的优惠券张数
     */
    @Select("select count(1) from lkt_coupon where store_id=#{storeId} and hid=#{hid} and user_id=#{userId} and free_or_not = 1 and recycle=0")
    int systemGiveUserCouponNum(int storeId, int hid, String userId);

    //统计优惠券领取的总人数 isFree=是否为赠送 0.不为赠送 1.赠送
    //@Select("select count(1) from lkt_coupon where hid=#{hid} and store_id=#{storeId} and recycle=0 and free_or_not=#{isFree}")
    @Select("select count(distinct user_id) from lkt_coupon where hid=#{hid} and store_id=#{storeId} and recycle=0 and free_or_not=#{isFree}")
    int countPersonNum(int storeId, int hid, int isFree);


    //统计拥有优惠券的总人数
    //@Select("select count(1) from lkt_coupon where hid=#{hid} and store_id=#{storeId} and recycle=0 and free_or_not=#{isFree}")
    @Select("select count(distinct user_id) from lkt_coupon where hid=#{hid} and store_id=#{storeId} and recycle=0")
    int countAllPersonNum(int storeId, int hid);


    //管理后台统计拥有优惠券的总人数
    @Select("select count(distinct user_id) from lkt_coupon where hid=#{hid} and store_id=#{storeId} and recycle=0")
    int adminCountAllPersonNum(int storeId, int hid);

    /**
     * 动态统计 sql
     */
    int countCouponIsUser(Map<String, Object> map) throws LaiKeAPIException;

    @Select("select count(1) from lkt_coupon where hid=#{hid} and store_id=#{storeId} and recycle=0")
    int adminCountAllReceiveNum(int storeId, int hid);
}