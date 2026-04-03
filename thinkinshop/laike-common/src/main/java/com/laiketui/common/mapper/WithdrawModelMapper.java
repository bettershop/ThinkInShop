package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.WithdrawModel;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * 提现表 sql
 *
 * @author Trick
 * @date 2020/11/3 15:35
 */
public interface WithdrawModelMapper extends BaseMapper<WithdrawModel>
{


    /**
     * 动态统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 15:40
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取店铺提现信息 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/30 11:44
     */
    List<Map<String, Object>> getWithdrawLeftUserBankAndMch(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取店铺提现信息 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/30 11:44
     */
    BigDecimal getWithdrawLeftUserBankAndMchMoney(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取提现信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 14:21
     */
    List<Map<String, Object>> getWithdrawLeftUserBank(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 统计提现信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 14:21
     */
    int countWithdrawLeftUserBank(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取提现信息 - 店铺
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 14:21
     */
    List<Map<String, Object>> getWithdrawLeftMchBank(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 统计提现信息 - 店铺
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 14:21
     */
    int countWithdrawLeftMchBank(Map<String, Object> map) throws LaiKeAPIException;

    BigDecimal getTotallMoney(int storeid, String date, int index);

    @Select("select * from lkt_withdraw where user_id=#{userId} and is_mch =#{isMch} and status=0 and recovery=0 order by add_date desc limit 1")
    WithdrawModel getWithdrawInfoByUserId(String userId, int isMch);
}