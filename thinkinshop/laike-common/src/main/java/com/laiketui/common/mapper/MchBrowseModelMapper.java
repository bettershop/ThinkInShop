package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.MchBrowseModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 商城浏览 sql
 *
 * @author Trick
 * @date 2020/10/29 11:59
 */
public interface MchBrowseModelMapper extends BaseMapper<MchBrowseModel>
{


    /**
     * 动态查询
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/10 14:49
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/10 14:49
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    Integer countDynamicByUser(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 保存浏览记录
     *
     * @param mchBrowseModel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/29 12:00
     */
    @Insert("insert into lkt_mch_browse(store_id,token,mch_id,user_id,event,add_time) " +
            " values (#{store_id},#{token},#{mch_id},#{user_id},#{event},#{add_time})")
    int saveBrowse(MchBrowseModel mchBrowseModel) throws LaiKeAPIException;


    /**
     * 统计店铺访问量
     *
     * @param mchBrowseModel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/9 18:32
     */
    int countMchBrowseNum(@Param("model") MchBrowseModel mchBrowseModel) throws LaiKeAPIException;

    /**
     * 获取店铺访问记录动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/24 15:47
     */
    List<Map<String, Object>> getMchBrowseListDynamic(Map<String, Object> map) throws LaiKeAPIException;

    int countMchBrowseListDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取用户足迹明细
     *
     * @param storeId -
     * @param mchId   -
     * @param userId  -
     * @return List
     * @author Trick
     * @date 2021/5/27 10:58
     */
    @Select("select event,DATE_FORMAT(add_time,'%H:%i:%s') AS add_time from lkt_mch_browse " +
            " where store_id = #{storeId} and mch_id = #{mchId} and user_id = #{userId} and TO_DAYS(add_time) = TO_DAYS(NOW()) order by add_time desc")
    List<Map<String, Object>> getMchBrowseByUser(int storeId, int mchId, String userId);

    @Select("select count(1) AS add_time from lkt_mch_browse " +
            " where store_id = #{storeId} and mch_id = #{mchId} and user_id = #{userId} and TO_DAYS(add_time) = TO_DAYS(NOW()) order by add_time desc")
    int countMchBrowseByUser(int storeId, int mchId, String userId);

    /**
     * 删除用户足迹
     *
     * @param storeId -
     * @param mchId   -
     * @param userId  -
     * @return int
     * @author Trick
     * @date 2021/5/27 11:09
     */
    @Delete("delete from lkt_mch_browse where store_id = #{storeId} and mch_id = #{mchId} and user_id = #{userId}")
    int delMchBrowseByUserId(int storeId, int mchId, String userId);


    /**
     * 更具订单时间统计用户访问数量
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
            "select IFNULL(count(distinct user_id),0)  from lkt_mch_browse where store_id = #{storeId} " +
            "<if test='mchId != null '> " +
            " AND mch_id = concat(',',#{mchId},',') " +
            "</if>" +
            "<if test='startDate != null '> " +
            "   and add_time <![CDATA[  >=  ]]> #{startDate}" +
            "</if>" +
            "<if test='endDate != null '> " +
            "   and add_time <![CDATA[  <=  ]]> #{endDate}" +
            "</if>" +
            " </script> ")
    BigDecimal sumBrowseByDate(int storeId, Integer mchId, Date startDate, Date endDate);


    /**
     * 统计某天每小时访问数
     *
     * @param storeId -
     * @param mchId   -
     * @param date    -
     * @return BigDecimal
     * @author Trick
     * @date 2021/5/28 16:30
     */
    List<Map<String, Object>> getBrowseNumByHour(@Param("storeId") int storeId, @Param("mchId") Integer mchId, @Param("date") Date date);


    /**
     * 统计某月每天访问数
     *
     * @param storeId   -
     * @param mchId     -
     * @param startDate -
     * @param endDate   -
     * @return BigDecimal
     * @author Trick
     * @date 2021/5/28 16:30
     */
    List<Map<String, Object>> getBrowseNumByMonth(@Param("storeId") int storeId, @Param("mchId") Integer mchId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询店铺访问数
     *
     * @param storeId
     * @param mchId
     * @return
     */
    @Select("select count(1) from (select count(1) from lkt_mch_browse where store_id = #{storeId} and mch_id = #{mchId} group by user_id) a")
    int getFwKhNum(Integer storeId, Integer mchId);
}