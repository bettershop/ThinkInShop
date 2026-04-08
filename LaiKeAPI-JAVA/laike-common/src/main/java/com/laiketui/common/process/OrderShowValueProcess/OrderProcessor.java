package com.laiketui.common.process.OrderShowValueProcess;

import com.laiketui.common.consts.OrderShowValueEnum;
import com.laiketui.core.lktconst.DictionaryConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.*;

/**
 * @author 订单状态按钮
 * @version 1.0
 * @description: liuao
 * @date 2025/1/23 10:24
 */
@Component
@Slf4j
public class OrderProcessor
{
    private final Map<String, OrderProcessingStrategy> strategies;

    public OrderProcessor()
    {
        strategies = new HashMap<>();
        strategies.put(DictionaryConst.OrdersType.ORDERS_HEADER_GM, new LogisticsOrderStrategy());
        strategies.put(DictionaryConst.OrdersType.ORDERS_HEADER_PS, new LogisticsOrderStrategy());
        strategies.put(DictionaryConst.OrdersType.ORDERS_HEADER_MS, new LogisticsOrderStrategy());
        strategies.put(DictionaryConst.OrdersType.ORDERS_HEADER_FS, new LogisticsOrderStrategy());
        strategies.put(DictionaryConst.OrdersType.ORDERS_HEADER_IN, new LogisticsOrderStrategy());
        strategies.put(DictionaryConst.OrdersType.ORDERS_HEADER_JP, new LogisticsOrderStrategy());
        strategies.put(DictionaryConst.OrdersType.ORDERS_HEADER_ZB, new LogisticsOrderStrategy());
        strategies.put(DictionaryConst.OrdersType.ORDERS_HEADER_FX, new LogisticsOrderStrategy());
        strategies.put(DictionaryConst.OrdersType.ORDERS_HEADER_PT, new LogisticsOrderStrategy());
        strategies.put(DictionaryConst.OrdersType.ORDERS_HEADER_VI, new LogisticsOrderStrategy());
    }

    public void processOrder(String oType, Map<String, Object> resMap, Map<String, Object> params)
    {
        OrderProcessingStrategy strategy = strategies.get(oType);
        if (strategy != null)
        {
            Integer status           = MapUtils.getInteger(params, "status");
            Integer self_lifting     = MapUtils.getInteger(params, "self_lifting");
            Integer orderCommentType = MapUtils.getInteger(params, "orderCommentType");
            Boolean haveExpress      = MapUtils.getBoolean(params, "haveExpress");
            Boolean canPay           = MapUtils.getBoolean(params, "canPay");
            Integer sellType         = MapUtils.getInteger(params, "sellType");
            Integer writeOffSettings = MapUtils.getInteger(params, "writeOffSettings");
            Boolean refund           = MapUtils.getBoolean(params, "refund");
            Integer count            = MapUtils.getInteger(params,"count");
            Boolean refundShowBtn    = MapUtils.getBoolean(params, "refundShowBtn");
            Integer unFinishShouHouOrderNum = MapUtils.getInteger(params,"unFinishShouHouOrderNum");
            String payType = MapUtils.getString(params, "payType");
            Integer review_status = MapUtils.getInteger(params, "review_status");
            params.put("oType", oType);
            //待付款
            if (status == ORDERS_R_STATUS_UNPAID )
            {
                if (Objects.nonNull(sellType) && sellType == 1)
                {
                    resMap.put(OrderShowValueEnum.QXDD.getName(), OrderShowValueEnum.QXDD.getNoShowValue());
                }else
                {
                    resMap.put(OrderShowValueEnum.QXDD.getName(), OrderShowValueEnum.QXDD.getShowValue());
                }
                if (Objects.nonNull(canPay) && !canPay)
                {
                  return;
                }
                int ljfkValue = Objects.equals(payType,"offline_payment") ? 0 :1;
                int scpzValue =  Objects.equals(payType,"offline_payment") && (review_status == 0 || review_status == 3) ? 1 : 0;
                int pzshzValue =  Objects.equals(payType,"offline_payment") && review_status == 1 ? 1 : 0;
                resMap.put(OrderShowValueEnum.LJFK.getName(),ljfkValue);
                resMap.put(OrderShowValueEnum.SCPZ.getName(), scpzValue);
                resMap.put(OrderShowValueEnum.PZSHZ.getName(), pzshzValue);
              return;
            }
            //待发货
            else if (status == ORDERS_R_STATUS_CONSIGNMENT)
            {
                if (self_lifting == 1)return;
                resMap.put(OrderShowValueEnum.TXFH.getName(), OrderShowValueEnum.TXFH.getShowValue());
            }
            //待收货
            else if (status == ORDERS_R_STATUS_DISPATCHED)
            {
                if (self_lifting == 1)
                {
                    addActions(resMap, OrderShowValueEnum.TQM);
                    if (unFinishShouHouOrderNum == 0)
                    {
                        addActions(resMap,OrderShowValueEnum.JSTK);
                    }
                    return;
                }
                if (haveExpress)
                {
                    resMap.put(OrderShowValueEnum.CKWL.getName(), OrderShowValueEnum.CKWL.getShowValue());
                }
                resMap.put(OrderShowValueEnum.QRSH.getName(), OrderShowValueEnum.QRSH.getShowValue());
            }
            //已完成
            else if (status == ORDERS_R_STATUS_COMPLETE)
            {
                if (orderCommentType == 1)
                {
                    resMap.put(OrderShowValueEnum.LJPJ.getName(), OrderShowValueEnum.LJPJ.getShowValue());
                }
            }else if (status == ORDERS_R_STATUS_TOBEVERIFIED)
            {
               if (refund)
               {
                   addActions(resMap,OrderShowValueEnum.SQSH);
               }else if(refundShowBtn && count == 1)
               {
                   addActions(resMap,OrderShowValueEnum.CKSH);
               }
                if (writeOffSettings == 1)
                {
                    addActions(resMap,OrderShowValueEnum.HXM);
                }
            }
            if (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) && writeOffSettings == 1)
            {
                addActions(resMap,OrderShowValueEnum.HXM);
            }
            strategy.processOrder(resMap, params);
        }
    }

    public static void addActions(Map<String, Object> map, OrderShowValueEnum... actions)
    {
        for (OrderShowValueEnum action : actions)
        {
            map.put(action.getName(), action.getShowValue());
        }
    }
}
