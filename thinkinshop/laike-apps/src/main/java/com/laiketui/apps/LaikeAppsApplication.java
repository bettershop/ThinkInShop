package com.laiketui.apps;

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
 * 后台管理
 *
 * @author Trick
 */
@SpringBootApplication
@ComponentScan(value = {"com.laiketui.apps"
        , "com.laiketui.common"
        , "com.laiketui.domain"
        , "com.laiketui.core"
        , "com.laiketui.root.filter"
        , "com.laiketui.root.gateway"
        , "com.laiketui.root.provider"
        , "com.laiketui.root.license"
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
public class LaikeAppsApplication extends SpringBootServletInitializer
{

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)
    {
        return builder.sources(LaikeAppsApplication.class);
    }

    public static void main(String[] args)
    {
        SpringApplication.run(LaikeAppsApplication.class, args);
    }

}
