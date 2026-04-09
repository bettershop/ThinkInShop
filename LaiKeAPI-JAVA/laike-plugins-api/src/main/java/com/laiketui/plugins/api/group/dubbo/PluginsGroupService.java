package com.laiketui.plugins.api.group.dubbo;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.Map;

/**
 * 拼团对外接口
 *
 * @author Trick
 * @date 2023/3/29 16:07
 */
public interface PluginsGroupService
{
    /**
     * 拼团订单设置
     *
     * @param params - PluginsGroupAddOrderConfigVo
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2023/3/28 15:48
     */
    void addOrderConfig(String params) throws LaiKeAPIException;

    /**
     * 获取订单设置
     *
     * @param storeId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/4 18:05
     */
    Map<String, Object> getOrderConfig(int storeId) throws LaiKeAPIException;

    /**
     * 拼团团队结算
     *
     * @param storeId -
     * @param openId  -
     * @param logger  - 日志-可选
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/17 15:39
     */
    void groupSettlement(int storeId, String openId, StringBuilder logger) throws LaiKeAPIException;

    void groupSettlement(int storeId, String openId) throws LaiKeAPIException;
}
