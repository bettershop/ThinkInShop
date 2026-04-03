package com.laiketui.plugins.api.auction.task;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.Map;

/**
 * 竞拍任务
 *
 * @author Trick
 * @date 2022/7/27 16:18
 */
public interface PluginAuctionTaskService
{

    /**
     * 竞拍主任务
     *
     * @return Map -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/27 16:20
     */
    Map<String, Object> execute(Integer storeId) throws LaiKeAPIException;
}
