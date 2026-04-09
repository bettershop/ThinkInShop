package com.laiketui.common.process.OrderShowValueProcess;

import com.laiketui.common.consts.OrderShowValueEnum;
import org.apache.commons.collections.MapUtils;

import java.util.Map;
import java.util.Objects;

import static com.laiketui.common.process.OrderShowValueProcess.OrderProcessor.addActions;
import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.*;

/**
 * @author 16640
 * @version 1.0
 * @description: liuao
 * @date 2025/1/23 11:10
 */
public class PresellOrderStrategy implements OrderProcessingStrategy
{

    @Override
    public void processOrder(Map<String, Object> resMap, Map<String, Object> param)
    {
        Integer sellType = MapUtils.getInteger(param, "sellType");
        Integer status = MapUtils.getInteger(param,"status");
        //预售订单 订货模式
        /*
         *1：待付款：取消订单、立即付款
         *2：待发货：提醒发货
         *3：待收货：查看物流、确认收货
         *4:已完成：查看物流、立即评价（立即评价后隐藏）、追加评价（立即评价过后显示，追加后隐藏）
         */
        if (Objects.nonNull(sellType) && sellType == 2)
        {
            if (status == ORDERS_R_STATUS_COMPLETE)
            {
                addActions(resMap, OrderShowValueEnum.SQSH);
            }
        }
    }
}
