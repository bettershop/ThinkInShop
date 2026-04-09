package com.xxl.job.admin.controller.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * web mvc config
 *
 * @author xuxueli 2018-04-02 20:48:20
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private PermissionInterceptor permissionInterceptor;
    @Resource
    private CookieInterceptor cookieInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截 XXL-JOB 原有路径 + 你的业务任务路径
        String[] xxlJobPaths = {
                "/toLogin",
                "/login",
                "/logout",
                "/task",
                "/jobinfo",
                "/joblog",
                "/jobgroup",
                "/help",
                "/chartInfo",
                "/api/**",
                "/static/**",

                // 👇 新增：放行给拦截器处理的业务任务路径（关键！）
                "/plugin/**",
                "/admin/**",
                "/saas/**",
                "/pc/**",
                "/app/**",
                "/comps/**"
        };

        registry.addInterceptor(permissionInterceptor).addPathPatterns(xxlJobPaths);
        registry.addInterceptor(cookieInterceptor).addPathPatterns(xxlJobPaths);
    }
}