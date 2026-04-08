package com.laiketui.cdc.config;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * 获取消费者
 */
@Configuration
@RequiredArgsConstructor
@RefreshScope
public class RocketMqConsumeAdapter {

    private final RocketMQMessageConverter rocketMqMessageConverter;

    @Value("${rocketmq.name-server:}")
    private String nameServer;

    //消费者
    public RocketMQTemplate getTemplateByConsumeTopicName(String topic) {
        RocketMQTemplate mqTemplate = new RocketMQTemplate();
        DefaultLitePullConsumer sender = new DefaultLitePullConsumer(topic);
        sender.setMessageModel(MessageModel.CLUSTERING);
        sender.setNamesrvAddr(nameServer);
        mqTemplate.setConsumer(sender);
        mqTemplate.setMessageConverter(rocketMqMessageConverter.getMessageConverter());
        return mqTemplate;
    }

}
