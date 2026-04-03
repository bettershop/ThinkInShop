package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.log.MchAccountLogModel;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 入驻商户账户收支记录 sql
 *
 * @author Trick
 * @date 2020/11/10 9:37
 */
public interface MchAccountLogModelMapper extends BaseMapper<MchAccountLogModel>
{


    /**
     * 获取店铺收支记录信息 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/10 9:39
     */
    List<Map<String, Object>> getMchAccountLogDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/7 10:01
     */
    int countMchAccountLogDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 统计金额
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/7 10:01
     */
    BigDecimal countMchAccountMoney(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 动态统计收支金额
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/10 9:46
     */
    BigDecimal sumMchAccountLogDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取店铺账户余额趋势
     * (中途日期没有出入账，则获取上一次记录的余额)
     *
     * @param storeId -
     * @param mchId   -
     * @param endDate -
     * @return Map
     * @author Trick
     * @date 2021/5/31 13:55
     */
    @Select("SELECT account_money FROM lkt_mch_account_log where store_id = #{storeId} and mch_id = #{mchId} and TO_DAYS(addtime) <= TO_DAYS(#{endDate}) order by addtime desc limit 1")
    Map<String, BigDecimal> getMchMoneyByDate(int storeId, int mchId, Date endDate);

    /**
     * 获取店铺 提现 趋势
     *
     * @param storeId -
     * @param mchId   -
     * @param endDate -
     * @return Map
     * @author Trick
     * @date 2021/5/31 13:55
     */
    @Select("select SUM(price) as money from lkt_mch_account_log where store_id = #{storeId} and mch_id = #{mchId} and status = 2 and type = 3 and TO_DAYS(#{endDate})  = TO_DAYS('$v') ")
    Map<String, BigDecimal> getMchWithdrawByDate(int storeId, int mchId, Date endDate);


    /**
     * 获取店铺 退款 趋势
     *
     * @param storeId -
     * @param mchId   -
     * @param endDate -
     * @return Map
     * @author Trick
     * @date 2021/5/31 13:55
     */
    @Select("select SUM(price) as money from lkt_mch_account_log where store_id = #{storeId} and mch_id = #{mchId} and status = 2 and type = 2 and TO_DAYS(#{endDate})  = TO_DAYS('$v') ")
    Map<String, BigDecimal> getMchReturnMoneyByDate(int storeId, int mchId, Date endDate);

}
