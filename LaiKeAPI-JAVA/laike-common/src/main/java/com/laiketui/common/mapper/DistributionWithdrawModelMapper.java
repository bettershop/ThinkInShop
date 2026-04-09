package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.distribution.DistributionWithdrawModel;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 提现列表
 *
 * @author Trick
 * @date 2021/2/7 14:26
 */
public interface DistributionWithdrawModelMapper extends BaseMapper<DistributionWithdrawModel>
{


    /**
     * 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 15:10
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态sql-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 15:10
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取提现列表记录
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 14:27
     */
    List<Map<String, Object>> selectUserWithdrawInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取提现列表记录-统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 14:27
     */
    int countUserWithdrawInfo(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取用户提现未审核id
     *
     * @param storeId -
     * @param userId  -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 11:02
     */
    @Select("select id from lkt_distribution_withdraw where store_id = #{storeId} and user_id=#{userId} and status=0 order by add_date desc LIMIT 0,1")
    Integer selectNotExaineToId(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 获取用户成功提现金额
     *
     * @param storeId -
     * @param userId  -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 15:07
     */
    @Select("select ifnull(sum(money), 0) as total from lkt_distribution_withdraw where store_id = #{storeId} and user_id = #{userId} and status=1")
    BigDecimal sumWithdrawSuccessAmt(int storeId, String userId) throws LaiKeAPIException;

}