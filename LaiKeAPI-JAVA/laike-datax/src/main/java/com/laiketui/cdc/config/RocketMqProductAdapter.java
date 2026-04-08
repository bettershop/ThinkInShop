package com.laiketui.cdc.config;

import cn.hutool.core.util.IdUtil;
import com.laiketui.domain.datax.RocketMqConst;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 获取生产者
 */
@Configuration
@RequiredArgsConstructor
public class RocketMqProductAdapter {

    private final RocketMQMessageConverter rocketMqMessageConverter;

    @Value("${rocketmq.name-server:}")
    private String nameServer;

    //初始化连接rockerMq的客户端名称
    @PostConstruct
    public void init() {
        System.setProperty("rocketmq.client.name", "AEcru"+ IdUtil.getSnowflakeNextIdStr() + "@" + System.currentTimeMillis());
    }

    //创建生产者
    public RocketMQTemplate getTemplateByTopicName(String topic) {
        RocketMQTemplate mqTemplate = new RocketMQTemplate();
        DefaultMQProducer producer = new DefaultMQProducer(topic);
        producer.setNamesrvAddr(nameServer);
        producer.setRetryTimesWhenSendFailed(2);
        //默认发送消息超时时间
        producer.setSendMsgTimeout((int) RocketMqConst.TIMEOUT);
        //最大消息大小(如需兼容大消息内容，还需修改borker服务配置文件)
        producer.setMaxMessageSize(1024 * 1024 * 1024);
        mqTemplate.setProducer(producer);
        mqTemplate.setMessageConverter(rocketMqMessageConverter.getMessageConverter());
        return mqTemplate;
    }
}
