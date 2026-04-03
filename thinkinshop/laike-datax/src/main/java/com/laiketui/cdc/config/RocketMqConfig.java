package com.laiketui.cdc.config;

import com.laiketui.domain.datax.RocketMqConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * mq配置文件
 */
@Configuration
public class RocketMqConfig {

    @Autowired
    private RocketMqProductAdapter rocketMqProductAdapter;


    //初始化生产者
    @Lazy
    @Bean(destroyMethod = "destroy")
    public RocketMQTemplate productionPackagingConfirmMqTemplate() {
        return rocketMqProductAdapter.getTemplateByTopicName(RocketMqConst.FLINK_CDC_RMQ_MAIN_TOPIC);
    }
}
