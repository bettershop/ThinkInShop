package com.laiketui.cdc.controller;

import com.laiketui.cdc.utils.RocketMQUtil;
import com.laiketui.domain.datax.RocketMqConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rmq-test")
public class RocketMqDemoController {


    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送消息到mq
     * @param msg 发送消息内容
     */
    @PostMapping("/send")
    public void send(@RequestBody String msg){
        RocketMQUtil.syncSendMessage(msg, rocketMQTemplate, RocketMqConst.FLINK_CDC_RMQ_MAIN_TOPIC, "备注");
    }


}

