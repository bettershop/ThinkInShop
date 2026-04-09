package com.laiketui.cdc.services.api.rocketmq;

import com.alibaba.fastjson2.JSON;
import com.laiketui.cdc.api.rocketmq.MessageHandleService;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.datax.DataChangeInfo;
import com.laiketui.domain.datax.RocketMqConst;
import com.laiketui.domain.user.User;
import org.redisson.api.RAtomicDouble;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 用户表消息处理
 */
@Service("userModelMessageHandleService")
public class UserModelMessageHandleServiceImpl implements MessageHandleService {


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
            String userKeytail = null ;
            //用户余额
            BigDecimal money = BigDecimal.ZERO;
            User userBefore = null;
            User userAfter = null;
            String userKey = RocketMqConst.SellOverRedisKeys.USER_MONEY_KEY  ;
            RAtomicDouble userMoneyAtomicDouble = null;
            switch (eventType){
                case RocketMqConst.MySqlEventType.READ:
                case RocketMqConst.MySqlEventType.CREATE:
                case RocketMqConst.MySqlEventType.UPDATE:
                    userAfter = JSON.parseObject(afterData, User.class);
                    money = userAfter.getMoney();
                    userKeytail = userAfter.getStore_id() + SplitUtils.UNDERLIEN + userAfter.getAccess_id();
                    userKey = userKey + userKeytail;
                    userMoneyAtomicDouble = redissonClient.getAtomicDouble(userKey);
                    userMoneyAtomicDouble.set(money.doubleValue());
                    break;
                case RocketMqConst.MySqlEventType.DELETE:
                    userBefore = JSON.parseObject(beforeData, User.class);
                    userKeytail = userBefore.getStore_id() + SplitUtils.UNDERLIEN + userBefore.getAccess_id();
                    userKey = userKey + userKeytail;
                    userMoneyAtomicDouble = redissonClient.getAtomicDouble(userKey);
                    userMoneyAtomicDouble.delete();
                    break;
            }
            userKeytail = null;
        } catch (LaiKeAPIException e){
            throw e;
        }
    }

}
