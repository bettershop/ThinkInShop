package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.distribution.DistributionIncomeModel;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

public interface DistributionIncomeModelMapper extends BaseMapper<DistributionIncomeModel>
{

    /**
     * 根据添加时间查询
     *
     * @param storeId
     * @param userId
     * @param Time
     * @return
     */
    @Select("select id from lkt_distribution_income where store_id = #{storeId} and user_id = #{userId} and DATE_FORMAT(add_time,\"%Y-%m-%d\") =#{Time}")
    Integer getIdByUserIdAndAddTime(int storeId, String userId, String Time) throws LaiKeAPIException;

    /**
     * 收益报表统计
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> IncomeStatementStatistics(Map<String, Object> map) throws LaiKeAPIException;
}
