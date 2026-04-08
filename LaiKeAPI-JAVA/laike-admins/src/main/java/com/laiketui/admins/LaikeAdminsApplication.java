package com.laiketui.admins;

import com.laiketui.root.common.LaiKeInitFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * PC后台管理接口：管理后台、主播后台、pc买家端、供应商端
 */
@SpringBootApplication
@ComponentScan(
        value = {"com.laiketui.admins"
                , "com.laiketui.common"
                , "com.laiketui.domain"
                , "com.laiketui.core.cache"
                , "com.laiketui.core.config"
                , "com.laiketui.core.domain"
                , "com.laiketui.core.exception"
                , "com.laiketui.core.db"
                , "com.laiketui.core.utils"
                , "com.laiketui.core.annotation"
                , "com.laiketui.root.filter"
                , "com.laiketui.root.gateway"
                , "com.laiketui.root.license"
                , "com.laiketui.root.provider"
                , "com.laiketui.root.service"
                , "com.laiketui.root.annotation"
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM,
                classes = LaiKeInitFilter.class)
)
@MapperScan("com.laiketui.common.mapper")
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableDiscoveryClient
@EnableScheduling
public class LaikeAdminsApplication extends SpringBootServletInitializer
{

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)
    {
        return builder.sources(LaikeAdminsApplication.class);
    }

    public static void main(String[] args)
    {
        SpringApplication.run(LaikeAdminsApplication.class, args);
    }

}