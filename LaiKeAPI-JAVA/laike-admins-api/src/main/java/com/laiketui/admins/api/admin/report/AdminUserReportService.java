package com.laiketui.admins.api.admin.report;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AdminUserReportService
{
    /**
     * 活跃用户统计
     *
     * @param vo
     * @return
     */
    Map<String, Object> getUserData(MainVo vo) throws LaiKeAPIException;

    /**
     * 各用户端用户统计
     *
     * @param vo
     * @return
     */
    List<Map<String, Object>> getUserAmount(MainVo vo) throws LaiKeAPIException;

    Map<String, Object> getAdditionUserData(MainVo vo) throws LaiKeAPIException;

    /**
     * 用户会员统计
     *
     * @param vo
     * @return
     */
    HashMap<String, Object> getMembershipStatistics(MainVo vo) throws LaiKeAPIException;

    /**
     * 用户消费排行
     *
     * @param vo
     * @return
     */
    Map<String, Object> getMoneyTop(MainVo vo) throws LaiKeAPIException;

    /**
     * 新增用户
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> additionData(MainVo vo) throws LaiKeAPIException;
}
