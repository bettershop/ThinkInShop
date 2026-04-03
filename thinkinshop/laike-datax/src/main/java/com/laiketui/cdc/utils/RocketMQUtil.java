package com.laiketui.cdc.utils;

import com.alibaba.fastjson2.JSON;
import com.laiketui.domain.datax.RocketMqConst;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.GenericMessage;

import java.io.Serializable;
import java.util.Objects;

/**
 * 队列消息帮助类
 */
public class RocketMQUtil implements Serializable {

    private transient final static Logger logger = LoggerFactory.getLogger(RocketMQUtil.class);

    /**
     * 发送数据
     *
     * @param rvo
     * @param topic
     * @return
     */
    public static  void syncSendMessage(Object rvo, RocketMQTemplate rocketMqTemplate, String topic, String msg) {
        SendStatus sendStatus = rocketMqTemplate.syncSend(topic, new GenericMessage<>(rvo), RocketMqConst.TIMEOUT).getSendStatus();
        if (!Objects.equals(sendStatus, SendStatus.SEND_OK)) {
            logger.error( "发送消息异常！" + JSON.toJSONString(sendStatus) );
            throw new RuntimeException(msg);
        }
    }

}
