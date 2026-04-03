package com.laiketui.comps;

import com.laiketui.comps.gateway.loadbalance.GatewayProperties;
import com.laiketui.root.common.LaiKeInitFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
        value = {"com.laiketui.comps"
                , "com.laiketui.common"
                , "com.laiketui.domain"
                , "com.laiketui.core"
                , "com.laiketui.root.filter"
                , "com.laiketui.root.gateway"
                , "com.laiketui.root.provider"
                , "com.laiketui.root.license"
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
@EnableDiscoveryClient
@EnableScheduling
@EnableConfigurationProperties(GatewayProperties.class)
public class LaikeCompsApplication extends SpringBootServletInitializer
{

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)
    {
        return builder.sources(LaikeCompsApplication.class);
    }

    public static void main(String[] args)
    {
        SpringApplication.run(LaikeCompsApplication.class, args);
    }

    /*
    import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettySpringBootApplication implements CommandLineRunner {

    @Autowired
    private NettyServer nettyServer;

    public static void main(String[] args) {
        SpringApplication.run(NettySpringBootApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        nettyServer.start(8080);
    }
}
     */

}