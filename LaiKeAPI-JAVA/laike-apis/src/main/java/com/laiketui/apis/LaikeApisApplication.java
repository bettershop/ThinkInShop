package com.laiketui.apis;

import com.laiketui.root.common.LaiKeInitFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;


@ComponentScan(value = {"com.laiketui"
        , "com.laiketui.common"
        , "com.laiketui.domain"
        , "com.laiketui.core"
        , "com.laiketui.root.filter"
        , "com.laiketui.root.gateway"
        , "com.laiketui.root.provider"
        , "com.laiketui.root.service"
        , "com.laiketui.root.annotation"
        , "com.xxl.job"
},
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM,
                classes = LaiKeInitFilter.class)
)
@MapperScan({"com.laiketui.common.mapper","com.xxl.job.admin.dao"})
@EnableTransactionManagement

@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication(exclude = {
        com.alibaba.cloud.nacos.NacosConfigAutoConfiguration.class,
        com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration.class,
        com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration.class,
        com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration.class
})
public class LaikeApisApplication extends SpringBootServletInitializer
{

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)
    {
        return builder.sources(LaikeApisApplication.class);
    }

    public static void main(String[] args)
    {
        SpringApplication.run(LaikeApisApplication.class, args);
    }

}
