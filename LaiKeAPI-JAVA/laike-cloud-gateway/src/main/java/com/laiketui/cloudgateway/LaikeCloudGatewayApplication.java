package com.laiketui.cloudgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = "com.laiketui.cloudgateway")
@EnableDiscoveryClient
@EnableAutoConfiguration(exclude = {RedisAutoConfiguration.class, RabbitAutoConfiguration.class})
public class LaikeCloudGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaikeCloudGatewayApplication.class, args);
    }

}
