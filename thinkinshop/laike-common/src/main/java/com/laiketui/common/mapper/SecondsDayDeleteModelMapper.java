package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.plugin.seckill.SecondsDayDeleteModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Trick
 */
@Deprecated
public interface SecondsDayDeleteModelMapper extends BaseMapper<SecondsDayDeleteModel>
{


    /**
     * 获取被删除的天数
     *
     * @param aid - 活动id
     * @return List
     * @author Trick
     * @date 2021/5/8 9:49
     */
    @Select("SELECT distinct day FROM lkt_seconds_day_delete  WHERE activity_id = #{aid} and day not in(SELECT DISTINCT left(add_time,10)  " +
            " FROM `lkt_seconds_record` WHERE is_delete = 0 and sec_id = #{aid}) order by day desc")
    List<String> getSecondsDayDel(int aid);
}