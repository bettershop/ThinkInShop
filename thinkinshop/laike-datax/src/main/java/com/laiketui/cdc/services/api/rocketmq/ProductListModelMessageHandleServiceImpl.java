package com.laiketui.cdc.services.api.rocketmq;

import com.alibaba.fastjson2.JSON;
import com.laiketui.cdc.api.rocketmq.MessageHandleService;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.datax.DataChangeInfo;
import com.laiketui.domain.datax.RocketMqConst;
import com.laiketui.domain.product.ProductListModel;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 商品消息处理
 */
@Service(value="productListModelMessageHandleService")
public class ProductListModelMessageHandleServiceImpl implements MessageHandleService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void handleMessage(DataChangeInfo dataChangeInfo) throws LaiKeAPIException {
        try{
            String tableName = dataChangeInfo.getTableName();
            String eventType = dataChangeInfo.getEventType();
            String beforeData = dataChangeInfo.getBeforeData();
            String afterData = dataChangeInfo.getAfterData();
            String database = dataChangeInfo.getDatabase();
            //剩余库存
            long remainProductNum = 0;
            String productTail = null ;
            ProductListModel productListModelBefore = null;
            ProductListModel productListModelAfter = null;
            String productStockKey = RocketMqConst.SellOverRedisKeys.PRODUCT_STOCK_KEY  ;
            String productStatusKey  = RocketMqConst.SellOverRedisKeys.PRODUCT_STATUS_KEY   ;
            RAtomicLong proStock  = null;
            RAtomicLong proStatus  = null;
            switch (eventType){
                case RocketMqConst.MySqlEventType.READ:
                case RocketMqConst.MySqlEventType.CREATE:
                case RocketMqConst.MySqlEventType.UPDATE:
                    productListModelAfter = JSON.parseObject(afterData, ProductListModel.class);
                    Integer pid = productListModelAfter.getId();
                    productTail = productListModelAfter.getStore_id() + SplitUtils.UNDERLIEN + pid;
                    //商品库存
                    productStockKey = productStockKey + productTail;
                    remainProductNum = productListModelAfter.getNum();
                    proStock = redissonClient.getAtomicLong(productStockKey);
                    proStock.set(remainProductNum);

                    //商品状态
                    productStatusKey = productStatusKey + productTail;
                    String status = productListModelAfter.getStatus();
                    proStatus = redissonClient.getAtomicLong(productStatusKey);
                    //状态 1:待上架 2:上架 3:下架
                    proStatus.set(Long.parseLong(status));
                    //逻辑删除
                    break;
                case RocketMqConst.MySqlEventType.DELETE:
                    productListModelBefore = JSON.parseObject(beforeData, ProductListModel.class);
                    productTail = productListModelBefore.getStore_id() + SplitUtils.UNDERLIEN + productListModelBefore.getId();
                    productStockKey = productStockKey + productTail ;
                    proStock = redissonClient.getAtomicLong(productStockKey);
                    proStock.delete();
                    break;
            }

            productTail = null;
        } catch (LaiKeAPIException e){
            throw e;
        }
    }

}
