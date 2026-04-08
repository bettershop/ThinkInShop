package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.seckill.SecondsTimeModel;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 秒杀时间段
 *
 * @author Trick
 */
public interface SecondsTimeModelMapper extends BaseMapper<SecondsTimeModel>
{


    /**
     * 获取所有秒杀时间段
     *
     * @param storeId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/8 16:38
     */
    @Select("SELECT * FROM lkt_seconds_time WHERE  store_id = #{storeId} and is_delete = 0 ORDER BY starttime ")
    List<Map<String, Object>> selectTimeAll(int storeId) throws LaiKeAPIException;


    /**
     * 获取所有秒杀时间段
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/8 16:38
     */
    @Select("<script>" +
            " SELECT * FROM lkt_seconds_time WHERE  store_id = #{storeId} and is_delete = 0 ORDER BY starttime " +
            "<if test=\"pageStart != null and pageEnd != null \">limit #{pageStart},#{pageEnd}</if>" +
            "</script>")
    List<Map<String, Object>> selectTimeList(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 查询这个时段开启的活动数量
     *
     * @param timeId  -
     * @param sysDate -
     * @return int
     * @author Trick
     * @date 2021/5/7 18:06
     */
    @Select("select count(id) as num from lkt_seconds_pro where time_id = #{timeId} and #{sysDate} < endtime and  is_delete = 0")
    Integer countSecNum(int timeId, Date sysDate);
}