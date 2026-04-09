package com.laiketui.comps.gateway.loadbalance;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;
import java.util.regex.Pattern;

@RefreshScope
@Data
@ConfigurationProperties(prefix = "lkt-gateway")
public class GatewayProperties
{
    /**
     * 慢接口阈值（毫秒）
     */
    private long slowThresholdMs = 2000;

    /**
     * 熔断统计窗口（秒）
     */
    private int circuitWindowSeconds = 60;

    /**
     * 触发熔断的最小请求数
     */
    private int minRequest = 50;

    /**
     * 错误率阈值（0.5 = 50%）
     */
    private double errorRate = 0.5;

    /**
     * 慢请求比例阈值
     */
    private double slowRate = 0.5;

    /**
     * 熔断打开时间（秒）
     */
    private int openSeconds = 30;

    /**
     * 负载策略类型：random / roundRobin
     */
    private String loadBalanceStrategy = "random";

    /**
     * 请求安全关键字（正则）
     */
    private String regx =
            "insert|into|update|delete|from|where|and|trancate|drop|execute|grant|use|union|sleep|select";

    /**
     * 编译后的正则（运行期使用，避免重复 Pattern.compile）
     */
    private transient Pattern regxPattern;

    /**
     * IP 限流配置
     */
    private RequestLimit requestLimitPerIp = new RequestLimit();

    /**
     * 黑名单 IP 列表
     */
    private List<String> blacklist;

    private List<String> whitelist;

    public void setRegx(String regx)
    {
        this.regx = regx;
        // 配置刷新时重新编译
        this.regxPattern = null;
    }

    public Pattern getRegxPattern()
    {
        if (regxPattern == null && regx != null)
        {
            regxPattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE);
        }
        return regxPattern;
    }

    public static class RequestLimit
    {
        /**
         * 最大请求数
         */
        private int limit = 50000;

        /**
         * 时间窗口（秒）
         */
        private int time = 60;

        public int getLimit()
        {
            return limit;
        }

        public void setLimit(int limit)
        {
            this.limit = limit;
        }

        public int getTime()
        {
            return time;
        }

        public void setTime(int time)
        {
            this.time = time;
        }
    }


}
