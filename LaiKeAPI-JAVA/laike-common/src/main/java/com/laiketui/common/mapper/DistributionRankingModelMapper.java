package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.DistributionRankingModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 分佣排行表
 *
 * @author Trick
 * @date 2021/2/8 14:11
 */
public interface DistributionRankingModelMapper extends BaseMapper<DistributionRankingModel>
{
    /**
     * 用户分佣排行动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 14:12
     */
    List<Map<String, Object>> selectUserDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 用户分佣排行动态sql-统计
     *
     * @param map -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 14:12
     */
    Integer countUserDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取用户排名信息
     *
     * @param userId  -
     * @param type    -排行类型 1今日2本周3本月4本年
     * @param storeId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/18 10:21
     */
    @Select("SELECT orderNo,commission FROM ( SELECT (@rowNum := @rowNum + 1) orderNo,a.* FROM lkt_distribution_ranking a," +
            "(SELECT (@rowNum := 0)) b where a.type = #{type} and a.store_id = #{storeId} ORDER BY a.commission DESC limit 0,10) t" +
            " WHERE t.user_id = #{userId}")
    Map<String, Object> selectUserRankingInfo(String userId, int type, int storeId) throws LaiKeAPIException;


    /**
     * 获取用户真实排名
     *
     * @param userId    -
     * @param storeId   -
     * @param startDate -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/18 10:26
     */
    @Select("select orderNo,commission from (select (@rowNum := @rowNum + 1) orderNo,user_id,commission" +
            " from (select SUM(money) AS commission,user_id from lkt_distribution_record where store_id = #{storeId} and type = 1 " +
            " and status = 1 and add_date > #{startDate} group by user_id) a," +
            "(SELECT (@rowNum := 0)) b ORDER BY a.commission DESC ) t where t.user_id = #{userId}")
    Map<String, Object> selectUserRealRanking(String userId, int storeId, String startDate) throws LaiKeAPIException;

    /**
     * 批量删除
     *
     * @param ids -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 15:51
     */
    int batchDelById(List<Integer> ids) throws LaiKeAPIException;
}