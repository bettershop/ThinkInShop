package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.flashsale.FlashsaleLabelModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface FlashsaleLabelModelMapper extends BaseMapper<FlashsaleLabelModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询当前时间段是否又进行中/未开始的活动
     *
     * @param storeId
     * @param startTime
     * @param endTime
     * @param id
     * @return
     */
    @Select("select count(*) from lkt_flashsale_label where store_id = #{storeId} " +
            " and starttime <= #{endTime} and endtime >= #{startTime} " +
            " and mch_id = #{mchId} " +
            " and  `status` IN (1,2) and recovery = 0  and id != #{id}")
    Integer getLabelNumByStartTimeAndEndTimeNotId(Integer storeId, Integer mchId, String startTime, String endTime, Integer id);

    @Select("select count(*) from lkt_flashsale_label where store_id = #{storeId} " +
            " and starttime <= #{endTime} and endtime >= #{startTime} " +
            " and mch_id = #{mchId} " +
            " and  `status` IN (1,2) and recovery = 0 ")
    Integer getLabelNumByStartTimeAndEndTime(Integer storeId, Integer mchId, String startTime, String endTime);


    List<Map<String, Object>> getActivityListByMchId(Integer storeId, Integer mchId, String key);

    /**
     * 活动状态 未开始 -> 进行中
     *
     * @param nowTime
     * @return
     */
    @Update("update lkt_flashsale_label set status = 2 where status = 1 and recovery = 0 and starttime <= #{nowTime}")
    int updateActivityStatus(String nowTime);

    /**
     * 活动状态 进行中 ->结束
     *
     * @param nowTime
     * @return
     */
    @Update("update lkt_flashsale_label set status = 3 where status = 2 and recovery = 0 and endtime <= #{nowTime}")
    int updateActivityStatus2(String nowTime);
}
