package com.laiketui.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 1. 当一个任务被提交到线程池时，首先查看线程池的核心线程是否都在执行任务，否就选择一条线程执行任务，是就执行第二步。
 * 2. 查看核心线程池是否已满，不满就创建一条线程执行任务，否则执行第三步。
 * 3. 查看任务队列是否已满，不满就将任务存储在任务队列中(SynchronousQueue同步队直接执行第四步)，否则执行第四步。
 * 4. 查看线程池是否已满，不满就创建一条线程执行任务，否则就按照策略处理无法执行的任务。
 *
 * @author wangxian
 */
@Configuration
public class ThreadPoolTaskConfig
{

    @Bean
    public Executor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int core = Runtime.getRuntime().availableProcessors();
        // 核心线程
        executor.setCorePoolSize(core);
        // 高并发 I/O 任务可调大
        executor.setMaxPoolSize(core * 4);
        // 队列容量
        executor.setQueueCapacity(100);
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("thread-execute-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }


}
