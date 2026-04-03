package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.distribution.DistributionRecordModel;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 佣金日志表
 *
 * @author Trick
 * @date 2021/2/7 10:31
 */
public interface DistributionRecordModelMapper extends BaseMapper<DistributionRecordModel>
{


    /**
     * 查询
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 10:32
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 10:32
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 推广订单
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/18 14:18
     */
    List<Map<String, Object>> selectRecordDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 推广订单-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/18 14:18
     */
    int countRecordDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取用户预计佣金
     *
     * @param storeId -
     * @param userId  -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 10:50
     */
    @Select("select ifnull(sum(a.money),0) as sum from lkt_distribution_record a left join lkt_order b on a.sNo=b.sNo " +
            "where a.store_id=#{storeId} and a.type=1 and a.user_id=#{userId} and b.status in (1,2,5) and a.status=0")
    BigDecimal sumEstimateAmt(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 订单佣金计算
     */
    @Select("select ifnull(sum(money),0) as total from lkt_distribution_record where type = #{type} and sNo = #{orderno} ")
    BigDecimal sumEstimateAmtByType(int type, String orderno) throws LaiKeAPIException;

    /**
     * 获取用户单日佣金
     *
     * @param storeId -
     * @param userId  -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 10:50
     */
    @Select("select IFNULL(sum(a.money), 0) as sum from lkt_distribution_record a " +
            "where a.store_id=#{storeId} and a.type=1 and a.user_id=#{userId} and DATE_FORMAT(a.add_date,\"%Y-%m-%d\") = #{time}")
    BigDecimal sumUserEstimateAmtByAddDate(int storeId, String userId, String time) throws LaiKeAPIException;

    /**
     * 获取用户单日有效订单数
     *
     * @param storeId -
     * @param userId  -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 10:50
     */
    @Select("select count(*) FROM (select count(*) as sum from lkt_distribution_record a left join lkt_order b on a.sNo=b.sNo " +
            "where a.store_id=#{storeId} and a.type=1 and a.user_id=#{userId} and DATE_FORMAT(a.add_date,\"%Y-%m-%d\") = #{time} and b.status in (1,2,5) group by a.sNo) as aaas")
    int sumUserOrderNumByAddDate(int storeId, String userId, String time) throws LaiKeAPIException;

    /**
     * 获取用户单日有效订金额
     *
     * @param storeId -
     * @param userId  -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 10:50
     */
    @Select("select IFNULL(sum(b.old_total),0) as sum from lkt_distribution_record a left join lkt_order b on a.sNo=b.sNo " +
            "where a.store_id=#{storeId} and a.type=1 and a.user_id=#{userId} and DATE_FORMAT(a.add_date,\"%Y-%m-%d\") = #{time} and b.status in (1,2,5)")
    BigDecimal sumUserOrderTotalByAddDate(int storeId, String userId, String time) throws LaiKeAPIException;

    /**
     * 查询
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 10:32
     */
    List<Map<String, Object>> selectUserTell(Map<String, Object> map) throws LaiKeAPIException;
}