package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.flashsale.FlashsaleActivityModel;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface FlashsaleActivityModelMapper extends BaseMapper<FlashsaleActivityModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 删除活动
     *
     * @param storeId
     * @param LabelId
     * @return
     */
    @Update("update lkt_flashsale_activity set is_delete = 1 where store_id = #{storeId} and label_id = #{LabelId}")
    int deleteByLabelId(Integer storeId, Integer LabelId);

    /**
     * 活动状态 未开始 -> 进行中
     *
     * @param nowTime
     * @return
     */
    @Update("update lkt_flashsale_activity set status = 2 where status = 1 and is_delete = 0 and starttime <= #{nowTime}")
    int updateActivityStatus(String nowTime);

    /**
     * 活动状态 进行中 ->结束
     *
     * @param nowTime
     * @return
     */
    @Update("update lkt_flashsale_activity set status = 3 where status = 2 and is_delete = 0 and endtime <=  #{nowTime}")
    int updateActivityStatus2(String nowTime);

}
