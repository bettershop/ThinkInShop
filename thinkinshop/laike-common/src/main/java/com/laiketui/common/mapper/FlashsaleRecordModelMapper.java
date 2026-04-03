package com.laiketui.common.mapper;


import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.flashsale.FlashsaleRecordModel;
import org.apache.ibatis.annotations.Select;

public interface FlashsaleRecordModelMapper extends BaseMapper<FlashsaleRecordModel>
{

    @Select("select IFNULL(sum(a.num),0) from lkt_flashsale_record a left join lkt_order o on a.sNo = o.sNo where a.store_id = #{storeId} and  a.user_id = #{userId} and activity_id = #{activityId} and o.status != 7")
    Integer getUserPayNum(Integer storeId, String userId, Integer activityId);
}
