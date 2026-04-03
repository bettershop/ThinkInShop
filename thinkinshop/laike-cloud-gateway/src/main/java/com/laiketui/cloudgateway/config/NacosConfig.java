package com.laiketui.cloudgateway.config;

import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import java.lang.management.ManagementFactory;
import java.util.Set;

@Component
public class NacosConfig implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired(required = false)
    private NacosAutoServiceRegistration registration;

    @Value("${server.port}")
    Integer port;

    @Override
    public void run(ApplicationArguments args) {
        if (registration != null && port != null) {
            //如果getTomcatPort()端口获取异常,就采用配置文件中配置的端口
            Integer tomcatPort = port;
            try {
                String tp = getTomcatPort();
                if (tp != null && !"".equals(tp)) {
                    tomcatPort = new Integer(tp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            registration.setPort(tomcatPort);
            registration.start();
        }
    }

    /**
     * 获取外置tomcat端口
     */
    public String getTomcatPort() throws Exception {
        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"), Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
        if (objectNames != null && objectNames.size() > 0) {
            String port = objectNames.iterator().next().getKeyProperty("port");
            return port;
        }
        return null;

    }
}
