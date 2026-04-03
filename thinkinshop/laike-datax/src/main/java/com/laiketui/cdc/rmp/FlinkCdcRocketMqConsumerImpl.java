package com.laiketui.cdc.rmp;

import com.alibaba.fastjson2.JSON;
import com.laiketui.cdc.api.rocketmq.MessageHandleService;
import com.laiketui.cdc.services.api.rocketmq.CartModelMessageHandleServiceImpl;
import com.laiketui.cdc.services.api.rocketmq.ProductListModelMessageHandleServiceImpl;
import com.laiketui.cdc.services.api.rocketmq.UserModelMessageHandleServiceImpl;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.utils.help.SpringHelper;
import com.laiketui.domain.datax.DataChangeInfo;
import com.laiketui.domain.datax.RocketMqConst;
import com.laiketui.domain.mch.CartModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.PARAMATER_ERROR;

/**
 * 消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = RocketMqConst.FLINK_CDC_RMQ_MAIN_TOPIC, consumerGroup = RocketMqConst.FLINK_CDC_RMQ_MAIN_TOPIC, messageModel = MessageModel.CLUSTERING)
public class FlinkCdcRocketMqConsumerImpl implements RocketMQListener<String>{

    public static void main(String[] args) {
        // demo
        System.out.println("#####################");
        // 配置RedissonClient
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6339").setPassword("laiketui18").setDatabase(2);
        RedissonClient redisson = Redisson.create(config);
        // 获取桶
        RBucket<CartModel> bucket = redisson.getBucket("SHOP_CART_1_547");
        // 清空桶
        System.out.println(JSON.toJSONString(bucket.get()));
        System.out.println("######2###############");

        //
        RAtomicLong proStock = redisson.getAtomicLong("productStockKey");
        proStock.set(12);
        System.out.println(proStock.get());
        proStock.delete();//删除之后不为空
        System.out.println(proStock.get());
        System.out.println("######1###############");

        // 关闭Redisson客户端
        redisson.shutdown();
    }

    @Autowired
    private SpringHelper springHelper;

    /**
     * 获取消息处理对象
     * @param table
     * @return
     */
    private MessageHandleService getMessageHandleService(String table){
        switch (table){
            case RocketMqConst.WatchTablesName.LKT_PRODUCT_LIST:
                return (ProductListModelMessageHandleServiceImpl) springHelper.getBean("productListModelMessageHandleService");
            case RocketMqConst.WatchTablesName.LKT_CART:
                return (CartModelMessageHandleServiceImpl) springHelper.getBean("cartModelMessageHandleService");
            case RocketMqConst.WatchTablesName.LKT_USER:
                return (UserModelMessageHandleServiceImpl) springHelper.getBean("userModelMessageHandleService");
        }
        throw new LaiKeAPIException(PARAMATER_ERROR, "参数错误：不支持处理表【"+table+"】消息！");
    }

    @Override
    public void onMessage(String message) {
        try {
            DataChangeInfo dataChangeInfo = JSON.parseObject(message, DataChangeInfo.class);
            String tableName = dataChangeInfo.getTableName();
            String eventType = dataChangeInfo.getEventType();
            String beforeData = dataChangeInfo.getBeforeData();
            String afterData = dataChangeInfo.getAfterData();
            String database = dataChangeInfo.getDatabase();
            log.info("库名.表名:{}.{}", database, tableName);
            log.info("前:{}", beforeData);
            log.info("后:{}", afterData);
            log.info("事件类型:{}", eventType);
            MessageHandleService messageHandleService = null;
            messageHandleService = getMessageHandleService(tableName);
            messageHandleService.handleMessage(dataChangeInfo);
        } catch (Exception e){
            e.printStackTrace();
            log.error("消息监听异常:{}",e.getMessage());
        }
    }

}


