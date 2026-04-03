package com.laiketui.comps.api.task.plugin;

import com.laiketui.core.exception.LaiKeAPIException;

public interface CompsTaskFlashsaleService
{

    /**
     * 限时折扣活动定时任务
     *
     * @throws LaiKeAPIException
     */
    void activityTask() throws LaiKeAPIException;
}
