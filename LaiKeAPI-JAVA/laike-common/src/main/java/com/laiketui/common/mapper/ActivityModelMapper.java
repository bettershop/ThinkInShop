package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.ActivityModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 活动表
 *
 * @author Trick
 * @date 2021/2/23 18:00
 */
public interface ActivityModelMapper extends BaseMapper<ActivityModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    @Select("select IFNULL(MAX(sort + 1),0) as sort from lkt_activity where store_id = #{storeId}")
    int maxActivity(int storeId);


    @Update("update lkt_activity a,lkt_activity b set a.sort=b.sort,b.sort = a.sort where a.id=#{id} and b.id=#{id1}")
    int move(int id, int id1) throws LaiKeAPIException;

    @Select("select * from lkt_activity where store_id=#{storeId} and plug_type=#{pluginType} and is_display=1 and activity_type=1")
    ActivityModel getActivityModel(int storeId, int pluginType);

    //营销插件是否打开
    @Select("select count(1) from lkt_activity where store_id=#{storeId} and plug_type=#{pluginType} and is_display=1 and activity_type=1")
    int pluginSwitch(int storeId, int pluginType);
}