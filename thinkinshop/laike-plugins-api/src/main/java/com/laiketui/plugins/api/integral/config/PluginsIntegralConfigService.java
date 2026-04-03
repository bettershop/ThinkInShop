package com.laiketui.plugins.api.integral.config;

import com.laiketui.core.exception.LaiKeAPIException;

/**
 * 积分插件配置
 *
 * @author Trick
 * @date 2022/6/13 15:12
 */
public interface PluginsIntegralConfigService
{

    /**
     * 设置默认积分配置
     *
     * @param storeId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/6/13 15:12
     */
    void setIntegralConfig(int storeId) throws LaiKeAPIException;

}
