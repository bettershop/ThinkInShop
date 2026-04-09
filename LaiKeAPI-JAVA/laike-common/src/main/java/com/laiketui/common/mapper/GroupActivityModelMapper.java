package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.group.GroupActivityModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 拼团活动
 *
 * @author Trick
 * @date 2023/3/20 16:17
 */
public interface GroupActivityModelMapper extends BaseMapper<GroupActivityModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    //@Override
    int delDynamic(Map<String, Object> map);

    //拼团活动开始
    @Update("update lkt_group_activity a set a.status=1,a.is_show=1 where a.store_id=#{storeId} and a.start_date <=#{sysDate} and a.end_date>=#{sysDate} and a.status=0 and a.recycle=0 ")
    int startedSpecial(Integer storeId, Date sysDate);

    //拼团活动结束
    String ACTIVITY_END_SQL = "update lkt_group_activity a left join lkt_group_open b on a.id=b.activity_id and b.recycle=0 set a.status=2,b.status=2,b.team_commission=0,a.is_show=0 where a.store_id=#{storeId} and a.end_date <=#{sysDate} and a.status!=2 and b.status!=1 and a.recycle=0 ";

    @Update(ACTIVITY_END_SQL)
    int invalidSpecial(Integer storeId, Date sysDate);

    //拼团活动未开始
    @Update("update lkt_group_activity a set a.status=0 where a.store_id=#{storeId} and a.start_date >#{sysDate} and a.recycle=0 ")
    int notStartedSpecial(Integer storeId, Date sysDate);

    //拼团活动结束
    @Update(ACTIVITY_END_SQL + " and a.id=#{acId}")
    int invalidSpecialById(Integer storeId, String acId, Date sysDate);


    //获取活动最新状态
    @Select("select status from lkt_group_activity a where a.id=#{acId} ")
    int getAcNewStatus(String acId);

    @Update("update lkt_group_activity a set a.status=2,a.is_show=0 where a.store_id=#{storeId} and a.end_date < #{sysDate} and a.recycle=0")
    int invalidSpecialNoOpen(Integer storeId, Date sysDate);

    int countActivityNumByIdAndStatus(Map<String, Object> map);
}