package com.laiketui.common.api.order;

import com.laiketui.core.exception.LaiKeAPIException;

/**
 * 异步任务
 *
 * @author Trick
 * @date 2021/4/13 16:26
 */
public interface PublicTaskService
{


    /**
     * 主任务
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/13 16:27
     */
    void execute() throws LaiKeAPIException;

    /**
     * 处理库存
     *
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/17 20:42
     */
    default void stock() throws LaiKeAPIException
    {
    }

    /**
     * 任务助手 - 建议用来更新缓存等优化任务
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/1/24 17:41
     */
    default void aideExecute() throws LaiKeAPIException
    {
    }

    /**
     * 缓存插件信息
     *
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-10-07
     */
    default void Cache() throws LaiKeAPIException
    {
    }
}
