package com.laiketui.common.process.OrderShowValueProcess;

import com.laiketui.common.consts.OrderShowValueEnum;
import org.apache.commons.collections.MapUtils;

import java.util.Map;

import static com.laiketui.common.process.OrderShowValueProcess.OrderProcessor.addActions;
import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE;
import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT;

/**
 * @author 16640
 * @version 1.0
 * @description: liuao
 * @date 2025/1/23 14:18
 */
public class OrderCommStrategy implements OrderProcessingStrategy
{
    @Override
    public void processOrder(Map<String, Object> resMap, Map<String, Object> params)
    {
        Integer status = MapUtils.getInteger(params,"status");

        Integer unFinishShouHouOrderNum = MapUtils.getInteger(params,"unFinishShouHouOrderNum");

        /*
         *1：已完成：删除订单
         */
        switch (status)
        {
            case ORDERS_R_STATUS_COMPLETE:
                addActions(resMap, OrderShowValueEnum.SCDD);
                if ( unFinishShouHouOrderNum == null || unFinishShouHouOrderNum == 0)
                {
                    addActions(resMap,OrderShowValueEnum.SQSH);
                }
                if ( unFinishShouHouOrderNum == null || unFinishShouHouOrderNum == 0)
                {
                    addActions(resMap,OrderShowValueEnum.SQSH);
                }
                break;
            case ORDERS_R_STATUS_CONSIGNMENT:
                    if ( unFinishShouHouOrderNum == null || unFinishShouHouOrderNum == 0)
                    {
                        addActions(resMap,OrderShowValueEnum.SQSH);
                    }
                break;
        }
    }
}
