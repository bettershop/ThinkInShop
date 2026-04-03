package com.laiketui.common.process.OrderShowValueProcess;

import com.laiketui.common.consts.OrderShowValueEnum;
import com.laiketui.core.lktconst.DictionaryConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

import static com.laiketui.common.process.OrderShowValueProcess.OrderProcessor.addActions;
import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.*;

/**
 * @author 物流订单
 * @version 1.0
 * @description: liuao
 * @date 2025/1/23 10:23
 */
@Slf4j
public class LogisticsOrderStrategy implements OrderProcessingStrategy
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void processOrder(Map<String, Object> resMap, Map<String,Object> param)
    {

        getDeliveryShowValue(resMap,param);
    }

    private void getDeliveryShowValue(Map<String, Object> resMap,Map<String,Object> param)
    {
        Integer status = MapUtils.getInteger(param,"status");
        Boolean haveExpress = MapUtils.getBoolean(param,"haveExpress");
        Integer self_lifting = MapUtils.getInteger(param,"self_lifting");
        Integer unFinishShouHouOrderNum = MapUtils.getInteger(param,"unFinishShouHouOrderNum");
        Boolean invoiceTimeout = MapUtils.getBoolean(param, "invoiceTimeout");
        String oType = MapUtils.getString(param, "oType");
        Boolean refund = MapUtils.getBoolean(param, "refund");
        Boolean refundShowBtn = MapUtils.getBoolean(param, "refundShowBtn");
        Boolean isInvoice = MapUtils.getBoolean(param, "isInvoice");
        Integer count            = MapUtils.getInteger(param,"count");
        //纯积分订单商品价格为0 不开发票
        Integer invoicePrice = MapUtils.getInteger(param, "invoicePrice");

        //说明：商家配送订单与物流订单的呈现一致即可。
        switch (status)
        {
            //待发货
            case ORDERS_R_STATUS_CONSIGNMENT:
                if (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM)
                        || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_ZB)
                        || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN)
                        || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_MS)
                        || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FS)
                        || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_PS)
                        || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
                {
                    if (unFinishShouHouOrderNum == 0)
                    {
                        addActions(resMap,OrderShowValueEnum.JSTK);
                    }
                }
                if (self_lifting != 1)
                {
                    if (Boolean.TRUE.equals(haveExpress))
                    {
                        resMap.put(OrderShowValueEnum.CKWL.getName(), OrderShowValueEnum.CKWL.getShowValue());
                    }
                }
                if (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
                {
                   if (refundShowBtn)
                   {
                       addActions(resMap,OrderShowValueEnum.CKSH);
                   }
                }
                break;
            //待收货
            case ORDERS_R_STATUS_DISPATCHED:
                //自提订单待收货显示提取码、极速退款
                if (self_lifting != null && self_lifting == 1)
                {
                    if (unFinishShouHouOrderNum == 0)
                    {
                        addActions(resMap,OrderShowValueEnum.JSTK);
                    }
                    addActions(resMap,OrderShowValueEnum.TQM);
                    return;
                }
                if (refund)
                {
                    addActions(resMap,OrderShowValueEnum.SQSH);
                }
                if (refundShowBtn && count == 1)
                {
                    addActions(resMap,OrderShowValueEnum.CKSH);
                }
                break;
            //已完成
            case ORDERS_R_STATUS_COMPLETE:
                //自提订单已完成显示：查看提取码、立即评价
                boolean flag = (!invoiceTimeout && isInvoice) && (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) ||
                        oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM) || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN));
                if (self_lifting != null && self_lifting == 1)
                {
                    resMap.put(OrderShowValueEnum.CKTQM.getName(), OrderShowValueEnum.CKTQM.getShowValue());
                    //商品价格为0 不能开发票
                    if (invoicePrice != null && invoicePrice == 0)
                    {
                        flag = false;
                    }
                    if (flag)
                    {
                        addActions(resMap,OrderShowValueEnum.SQKP);
                    }
                }
                else
                {
                    //商品价格为0 不能开发票
                    if (invoicePrice != null && invoicePrice == 0)
                    {

                        flag = false;
                    }
                    if (flag)
                    {

                        addActions(resMap,OrderShowValueEnum.SQKP);
                    }
                    if (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM))
                    {
                        addActions(resMap,OrderShowValueEnum.ZCGM);
                    }
                    if (haveExpress)
                    {
                        addActions(resMap,OrderShowValueEnum.CKWL);
                    }
//                    if (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN) || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_JP))return;
                    if (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_JP)) return;
                    log.info("是否显示申请售后********************{}，是否显示查看售后*****************{}",refund,refundShowBtn);
                    if (refund)
                    {
                        addActions(resMap,OrderShowValueEnum.SQSH);
                    }
                    if (Objects.nonNull(self_lifting) && self_lifting == 3)
                    {
                        resMap.remove(OrderShowValueEnum.SQSH.getName());
                    }
                    if (refundShowBtn  && count == 1)
                    {
                        addActions(resMap,OrderShowValueEnum.CKSH);
                    }
                }

                break;
            case ORDERS_R_STATUS_CLOSE:
                if (!oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_JP) && !oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_PT)) {
                    addActions(resMap, OrderShowValueEnum.SCDD);
                }
                if (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM) || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                {
                    addActions(resMap, OrderShowValueEnum.ZCGM);
                }
                break;
        }
    }
}
