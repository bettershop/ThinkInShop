package com.laiketui.comps.gateway.loadbalance;

import com.laiketui.core.domain.LaiKeApi;
import com.laiketui.core.exception.LaiKeAPIException;

import java.util.LinkedList;

public interface LoadBalanceStrategy
{
    /**
     * 从存活实例列表中选择一个
     */
    LaiKeApi select(LinkedList<LaiKeApi> aliveApis,String key) throws LaiKeAPIException;

}
