package com.laiketui.core.config;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OkHttpConfig
{

    private ConnectionPool buildConnectionPool(int maxIdle, long keepAliveMinutes)
    {
        return new ConnectionPool(maxIdle, keepAliveMinutes, TimeUnit.MINUTES);
    }

    private Dispatcher buildDispatcher(int maxRequests, int maxPerHost)
    {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(maxRequests);
        dispatcher.setMaxRequestsPerHost(maxPerHost);
        return dispatcher;
    }

    @Bean("apiClient")
    public OkHttpClient apiClient()
    {
        return new OkHttpClient.Builder()
                .connectionPool(buildConnectionPool(100, 5))
                .dispatcher(buildDispatcher(200, 100))
                // 连接超时适当延长
                .connectTimeout(10, TimeUnit.SECONDS)
                // 读取超时拉长到 2 分钟
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                // 整个调用超时拉到 3 分钟
                .callTimeout(180, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Bean("streamClient")
    public OkHttpClient streamClient()
    {
        return new OkHttpClient.Builder()
                .connectionPool(buildConnectionPool(50, 10))
                .dispatcher(buildDispatcher(50, 25))
                .connectTimeout(10, TimeUnit.SECONDS)
                //  有限超时
                .readTimeout(10, TimeUnit.MINUTES)
                //  有限超时
                .writeTimeout(10, TimeUnit.MINUTES)
                // 兜底
                .callTimeout(30, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .addInterceptor(chain ->
                {
                    Request request = chain.request().newBuilder()
                            .removeHeader("Accept-Encoding") // 关闭 GZIP
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }

    @Bean("wsClient")
    public OkHttpClient wsClient()
    {
        return new OkHttpClient.Builder()
                .connectionPool(buildConnectionPool(20, 30))
                .dispatcher(buildDispatcher(50, 50))
                .connectTimeout(10, TimeUnit.SECONDS)
                // 单次读等待
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                // 防泄漏
                .callTimeout(24, TimeUnit.HOURS)
                .retryOnConnectionFailure(true)
                .build();
    }
}
