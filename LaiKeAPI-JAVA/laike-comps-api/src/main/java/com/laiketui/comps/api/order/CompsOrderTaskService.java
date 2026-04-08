package com.laiketui.comps.api.order;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.Map;

/**
 * 订单任务
 * 定时任务计划抽离-有时间弄
 *
 * @author Trick
 * @date 2023/4/17 16:54
 */
public interface CompsOrderTaskService
{

    /**
     * 订单结算
     *
     * @param storeId   -
     * @param mchId     -
     * @param orderType -
     * @return Map-
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/17 19:37
     */
    Map<String, Object> orderSettlement(int storeId, int mchId, String orderType) throws LaiKeAPIException;


}
