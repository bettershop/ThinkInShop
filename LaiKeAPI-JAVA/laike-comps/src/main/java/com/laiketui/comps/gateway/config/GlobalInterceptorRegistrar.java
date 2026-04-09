package com.laiketui.comps.gateway.config;

import com.laiketui.core.annotation.InterceptorConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Comparator;
import java.util.Map;

/**
 * 拦截器同意注册
 */
@Component
public class GlobalInterceptorRegistrar implements WebMvcConfigurer, ApplicationContextAware
{

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        // 获取所有带 @InterceptorConfig 注解的 HandlerInterceptor Bean
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(InterceptorConfig.class);

        beans.values().stream()
                .filter(bean -> bean instanceof HandlerInterceptor)
                .map(bean -> (HandlerInterceptor) bean)
                .sorted(Comparator.comparingInt(this::getOrder))
                .forEach(interceptor -> {
                    InterceptorConfig config = interceptor.getClass().getAnnotation(InterceptorConfig.class);
                    registry.addInterceptor(interceptor)
                            .addPathPatterns(config.includePatterns())
                            .excludePathPatterns(config.excludePatterns());
                });
    }

    private int getOrder(HandlerInterceptor interceptor)
    {
        InterceptorConfig config = interceptor.getClass().getAnnotation(InterceptorConfig.class);
        return config != null ? config.order() : Integer.MAX_VALUE;
    }
}