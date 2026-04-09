package com.laiketui.plugins.api.group.dubbo.task;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.Map;

/**
 * 任务
 *
 * @author Trick
 * @date 2023-04-07 10:58:41
 */
public interface PluginsGroupTaskService
{

    /**
     * 主任务
     *
     * @param storeId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/7 10:58
     */
    Map<String, Object> execute(Integer storeId) throws LaiKeAPIException;
}
