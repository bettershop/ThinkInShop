package com.laiketui.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RefreshScope
public class RedissionConfig
{
    private static final String REDIS_PREFIX = "redis://";
    private static final String REDISS_PREFIX = "rediss://";

    private final RedisProperties redisProperties;

    public RedissionConfig(RedisProperties redisProperties)
    {
        this.redisProperties = redisProperties;
    }

    @Bean
    public RedissonClient redissonClient()
    {
        Config config = new Config();
        if (buildClusterConfig(config) || buildSentinelConfig(config))
        {
            return Redisson.create(config);
        }
        buildSingleConfig(config);
        return Redisson.create(config);
    }

    private boolean buildClusterConfig(Config config)
    {
        RedisProperties.Cluster cluster = redisProperties.getCluster();
        if (cluster == null || CollectionUtils.isEmpty(cluster.getNodes()))
        {
            return false;
        }
        List<String> nodes = cluster.getNodes().stream()
                .filter(StringUtils::hasText)
                .map(this::normalizeAddress)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(nodes))
        {
            return false;
        }
        ClusterServersConfig clusterServersConfig = config.useClusterServers()
                .addNodeAddress(nodes.toArray(new String[0]));
        applyCommonConfig(clusterServersConfig);
        return true;
    }

    private boolean buildSentinelConfig(Config config)
    {
        RedisProperties.Sentinel sentinel = redisProperties.getSentinel();
        if (sentinel == null || !StringUtils.hasText(sentinel.getMaster()) || CollectionUtils.isEmpty(sentinel.getNodes()))
        {
            return false;
        }
        List<String> nodes = sentinel.getNodes().stream()
                .filter(StringUtils::hasText)
                .map(this::normalizeAddress)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(nodes))
        {
            return false;
        }
        SentinelServersConfig sentinelServersConfig = config.useSentinelServers()
                .setMasterName(sentinel.getMaster())
                .addSentinelAddress(nodes.toArray(new String[0]))
                .setDatabase(redisProperties.getDatabase());
        applyCommonConfig(sentinelServersConfig);
        return true;
    }

    private void buildSingleConfig(Config config)
    {
        String host = StringUtils.hasText(redisProperties.getHost()) ? redisProperties.getHost() : "127.0.0.1";
        int port = redisProperties.getPort();
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(normalizeAddress(host + ":" + port))
                .setDatabase(redisProperties.getDatabase());
        applyCommonConfig(singleServerConfig);
    }

    private void applyCommonConfig(SingleServerConfig singleServerConfig)
    {
        if (StringUtils.hasText(redisProperties.getPassword()))
        {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }
        Duration timeout = redisProperties.getTimeout();
        if (timeout != null)
        {
            singleServerConfig.setTimeout((int) timeout.toMillis());
        }
    }

    private void applyCommonConfig(ClusterServersConfig clusterServersConfig)
    {
        if (StringUtils.hasText(redisProperties.getPassword()))
        {
            clusterServersConfig.setPassword(redisProperties.getPassword());
        }
        Duration timeout = redisProperties.getTimeout();
        if (timeout != null)
        {
            clusterServersConfig.setTimeout((int) timeout.toMillis());
        }
    }

    private void applyCommonConfig(SentinelServersConfig sentinelServersConfig)
    {
        if (StringUtils.hasText(redisProperties.getPassword()))
        {
            sentinelServersConfig.setPassword(redisProperties.getPassword());
        }
        Duration timeout = redisProperties.getTimeout();
        if (timeout != null)
        {
            sentinelServersConfig.setTimeout((int) timeout.toMillis());
        }
    }

    private String normalizeAddress(String value)
    {
        if (!StringUtils.hasText(value))
        {
            return value;
        }
        if (value.startsWith(REDIS_PREFIX) || value.startsWith(REDISS_PREFIX))
        {
            return value;
        }
        return REDIS_PREFIX + value;
    }
}
