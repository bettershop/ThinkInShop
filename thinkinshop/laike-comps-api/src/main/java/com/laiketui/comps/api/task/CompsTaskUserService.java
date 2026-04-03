package com.laiketui.comps.api.task;

import com.laiketui.core.exception.LaiKeAPIException;

/**
 * 用户定时任务接口
 *
 * @author ganpeng
 * @date 2023/05/12 17:50
 */
public interface CompsTaskUserService
{
    /***
     * 定时缓存平台新增用户信息
     * @throws LaiKeAPIException
     * @author ganpeng
     * @date 2023/05/12 17:50
     */
    void AdditionUserData() throws LaiKeAPIException;
}
