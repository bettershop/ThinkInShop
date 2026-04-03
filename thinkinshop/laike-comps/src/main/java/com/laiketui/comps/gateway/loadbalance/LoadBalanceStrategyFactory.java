package com.laiketui.comps.gateway.loadbalance;

import com.laiketui.comps.gateway.loadbalance.impl.RandomLoadBalanceStrategy;
import com.laiketui.comps.gateway.loadbalance.impl.RoundRobinLoadBalanceStrategy;
import com.laiketui.core.cache.GatewayRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoadBalanceStrategyFactory
{

    @Autowired
    private GatewayRedisUtil gatewayRedisUtil;

    @Autowired
    private GatewayProperties gatewayProperties;

    @Autowired
    private RandomLoadBalanceStrategy randomStrategy;

    @Autowired
    private RoundRobinLoadBalanceStrategy roundRobinStrategy;

    /**
     * 获取当前负载策略（运行期动态）
     * <p>
     * 优先级：
     * 1️⃣ Redis（运行期秒级切换）
     * # 切到轮询
     * HSET gw:config loadBalanceStrategy roundRobin
     * # 切回随机
     * HSET gw:config loadBalanceStrategy random
     *
     * 2️⃣ 配置文件（refresh 生效）
     *
     * 3️⃣ 默认 random
     */
    public LoadBalanceStrategy getStrategy()
    {

        // 1️⃣ Redis 动态控制（ 运维 / 控制台 ）
        String strategy = gatewayRedisUtil.hget("gw:config", "loadBalanceStrategy");

        // 2️⃣ Redis 没有，用配置文件
        if (strategy == null || strategy.isEmpty())
        {
            strategy = gatewayProperties.getLoadBalanceStrategy();
        }

        // 3️⃣ 根据策略名返回实现
        if ("roundRobin".equalsIgnoreCase(strategy))
        {
            return roundRobinStrategy;
        }

        // 默认 random
        return randomStrategy;
    }
}


