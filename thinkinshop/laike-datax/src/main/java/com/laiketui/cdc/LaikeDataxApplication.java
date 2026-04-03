package com.laiketui.cdc;

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
 * app端接口
 * @author Trick
 */
@SpringBootApplication
@ComponentScan(
        value = {"com.laiketui.cdc"
                , "com.laiketui.common"
                , "com.laiketui.domain"
                , "com.laiketui.core.cache"
                , "com.laiketui.core.config"
                , "com.laiketui.core.domain"
                , "com.laiketui.core.exception"
                , "com.laiketui.core.tc"
                , "com.laiketui.core.db"
                , "com.laiketui.core.utils"
                , "com.laiketui.core.annotation"
                , "com.laiketui.root.filter"
                , "com.laiketui.root.gateway"
                , "com.laiketui.root.provider"
                , "com.laiketui.root.service"
                , "com.laiketui.root.annotation"
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM,
                classes = LaiKeInitFilter.class)
)
@MapperScan("com.laiketui.common.mapper")
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass=true)
@EnableScheduling
@EnableDiscoveryClient
public class LaikeDataxApplication extends SpringBootServletInitializer  {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(LaikeDataxApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(LaikeDataxApplication.class, args);
    }




}
