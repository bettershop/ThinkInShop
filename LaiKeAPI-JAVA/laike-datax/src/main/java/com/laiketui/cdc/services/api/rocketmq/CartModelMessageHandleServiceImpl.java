package com.laiketui.cdc.services.api.rocketmq;

import com.alibaba.fastjson2.JSON;
import com.laiketui.cdc.api.rocketmq.MessageHandleService;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.datax.DataChangeInfo;
import com.laiketui.domain.datax.RocketMqConst;
import com.laiketui.domain.mch.CartModel;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 购物车消息处理类
 */
@Service("cartModelMessageHandleService")
public class CartModelMessageHandleServiceImpl implements MessageHandleService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void handleMessage(DataChangeInfo dataChangeInfo) throws LaiKeAPIException {
        try {

            String tableName = dataChangeInfo.getTableName();
            String eventType = dataChangeInfo.getEventType();
            String beforeData = dataChangeInfo.getBeforeData();
            String afterData = dataChangeInfo.getAfterData();
            String database = dataChangeInfo.getDatabase();

            String cartTail = null ;
            CartModel cartModelBefore = null;
            CartModel cartModelAfter = null;
            String shopCartKey = RocketMqConst.SellOverRedisKeys.SHOP_CART_KEY  ;
            RBucket<CartModel> cartModelRBucket = null;
            switch (eventType){
                case RocketMqConst.MySqlEventType.READ:
                case RocketMqConst.MySqlEventType.CREATE:
                case RocketMqConst.MySqlEventType.UPDATE:
                    cartModelAfter = JSON.parseObject(afterData, CartModel.class);
                    cartTail = cartModelAfter.getStore_id() + SplitUtils.UNDERLIEN + cartModelAfter.getId();
                    shopCartKey = shopCartKey + cartTail;
                    cartModelRBucket = redissonClient.getBucket(shopCartKey);
                    cartModelRBucket.set(cartModelAfter);
                    break;
                case RocketMqConst.MySqlEventType.DELETE:
                    cartModelBefore = JSON.parseObject(beforeData, CartModel.class);
                    cartTail = cartModelBefore.getStore_id() + SplitUtils.UNDERLIEN + cartModelBefore.getId();
                    shopCartKey = shopCartKey + cartTail;
                    cartModelRBucket = redissonClient.getBucket(shopCartKey);
                    cartModelRBucket.delete();
                    break;
            }
            cartTail = null;

        }catch (LaiKeAPIException e){
            throw  e;
        }

    }
}
