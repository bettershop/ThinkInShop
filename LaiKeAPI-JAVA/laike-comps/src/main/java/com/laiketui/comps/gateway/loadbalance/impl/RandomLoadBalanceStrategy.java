package com.laiketui.comps.gateway.loadbalance.impl;

import com.laiketui.comps.gateway.loadbalance.LoadBalanceStrategy;
import com.laiketui.core.domain.LaiKeApi;
import com.laiketui.core.exception.LaiKeAPIException;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RandomLoadBalanceStrategy implements LoadBalanceStrategy {

    @Override
    public LaiKeApi select(LinkedList<LaiKeApi> apis, String key) throws LaiKeAPIException
    {
        if (apis == null || apis.isEmpty()) return null;
        return apis.get(ThreadLocalRandom.current().nextInt(apis.size()));
    }
}


