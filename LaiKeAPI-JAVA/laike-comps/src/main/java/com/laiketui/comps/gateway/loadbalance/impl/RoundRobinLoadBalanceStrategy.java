package com.laiketui.comps.gateway.loadbalance.impl;

import com.laiketui.comps.gateway.loadbalance.LoadBalanceStrategy;
import com.laiketui.core.cache.GatewayRedisUtil;
import com.laiketui.core.domain.LaiKeApi;
import com.laiketui.core.exception.LaiKeAPIException;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
public class RoundRobinLoadBalanceStrategy implements LoadBalanceStrategy
{

    private final GatewayRedisUtil gatewayRedisUtil;

    public RoundRobinLoadBalanceStrategy(GatewayRedisUtil gatewayRedisUtil)
    {
        this.gatewayRedisUtil = gatewayRedisUtil;
    }

    @Override
    public LaiKeApi select(LinkedList<LaiKeApi> apis, String apiKey) throws LaiKeAPIException
    {
        if (apis == null || apis.isEmpty()) return null;
        // 使用 Redis 原子计数
        long index = gatewayRedisUtil.incrementRoundRobin("GW_ROUND_ROBIN:" + apiKey, 600);
        // -1 保证从 0 开始
        int i = (int) ((index - 1) % apis.size());
        return apis.get(i);
    }

}

