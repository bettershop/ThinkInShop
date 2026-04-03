package com.laiketui.comps.api.task.plugin;

import com.laiketui.core.exception.LaiKeAPIException;

/**
 * 签到插件异步任务
 *
 * @author Trick
 * @date 2021/4/8 11:21
 */
public interface CompsTaskSignService
{
    /**
     * 签到任务
     *
     * @param storeId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/8 11:07
     */
    void task(int storeId) throws LaiKeAPIException;
}
