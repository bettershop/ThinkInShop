package com.laiketui.comps.im.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置类
 * 优化：
 * 1. 增加WebSocket消息推送专用线程池
 * 2. 优化线程池参数，适应高并发场景
 */
@Configuration
public class CompsThreadPoolConfig
{
    /**
     * Excel上传异步线程池
     */
    @Bean("asyncUploadExcelExecutor")
    public ThreadPoolTaskExecutor asyncUploadExcelExecutor()
    {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(4);
        threadPoolTaskExecutor.setMaxPoolSize(8);
        threadPoolTaskExecutor.setQueueCapacity(999);
        threadPoolTaskExecutor.setKeepAliveSeconds(30);
        threadPoolTaskExecutor.setThreadNamePrefix("async-upload-excel");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setAwaitTerminationSeconds(60);
        return threadPoolTaskExecutor;
    }

    /**
     * WebSocket消息推送专用线程池
     * 核心线程数 = CPU核心数 * 2
     * 最大线程数 = CPU核心数 * 4
     * 队列容量 = 10000
     */
    @Bean("webSocketMessageExecutor")
    public ThreadPoolTaskExecutor webSocketMessageExecutor()
    {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        int cpuCore = Runtime.getRuntime().availableProcessors();
        threadPoolTaskExecutor.setCorePoolSize(cpuCore * 2);
        threadPoolTaskExecutor.setMaxPoolSize(cpuCore * 4);
        threadPoolTaskExecutor.setQueueCapacity(10000);
        threadPoolTaskExecutor.setKeepAliveSeconds(60);
        threadPoolTaskExecutor.setThreadNamePrefix("websocket-message-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setAwaitTerminationSeconds(60);
        return threadPoolTaskExecutor;
    }
}
