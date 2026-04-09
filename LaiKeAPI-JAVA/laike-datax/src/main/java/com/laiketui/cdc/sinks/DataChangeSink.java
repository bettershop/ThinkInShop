package com.laiketui.cdc.sinks;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laiketui.cdc.utils.RocketMQUtil;
import com.laiketui.domain.datax.DataChangeInfo;
import com.laiketui.domain.datax.RocketMqConst;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

public class DataChangeSink extends RichSinkFunction<DataChangeInfo> {

    private ObjectMapper mapper = new ObjectMapper();

    /**
     *  transient 不能去掉
     */
    private static transient RocketMQTemplate rocketMQTemplate;

    public DataChangeSink(RocketMQTemplate rocketMQTemplate ){
        DataChangeSink.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void invoke(DataChangeInfo changeInfo, Context context) throws Exception {
        System.out.printf("数据发生变化: %s%n", JSON.toJSONString(changeInfo));
        String op = changeInfo.getEventType();
        System.out.println(">>>>:" + op);
        //除了读操作的数据 其他的都需要监听
//        if (!RocketMqConst.MySqlEventType.READ.equals(op)) {
        RocketMQUtil.syncSendMessage(JSON.toJSONString(changeInfo), DataChangeSink.rocketMQTemplate, RocketMqConst.FLINK_CDC_RMQ_MAIN_TOPIC, "备注");
//        }
    }
}
