package com.laiketui.common.utils;

import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.service.dubbo.order.OrderDubboServiceImpl;
import com.laiketui.common.service.dubbo.plugin.distribution.DistributionOrderServiceImpl;
import com.laiketui.common.service.dubbo.plugin.group.PtGroupOrderServiceImpl;
import com.laiketui.common.service.dubbo.plugin.integral.IntegralOrderServiceImpl;
import com.laiketui.common.service.dubbo.plugin.presell.PreSellOrderServiceImpl;
import com.laiketui.common.service.dubbo.plugin.seconds.PtSecondsOrderSerciceImpl;
import com.laiketui.common.service.dubbo.plugin.seconds.SecondsOrderSerciceImpl;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.utils.help.SpringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.PARAMATER_ERROR;

/**
 * 订单流程工厂
 *
 * @author Trick
 * @date 2021/4/12 15:21
 */
@Component
public class OrderServiceFactory
{

    @Autowired
    private SpringHelper springHelper;

    /**
     * 获取订单实现
     *
     * @param orderType -
     * @return OrderDubboService
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/12 15:24
     */
    public OrderDubboService getOrderService(String orderType) throws LaiKeAPIException
    {
        OrderDubboService orderDubboService;
        switch (orderType)
        {
            case DictionaryConst.OrdersType.ORDERS_HEADER_GM:
                orderDubboService = springHelper.getBean(OrderDubboServiceImpl.class);
                break;
            case DictionaryConst.OrdersType.PTHD_ORDER_HEADER + DictionaryConst.OrdersType.ORDERS_HEADER_PT:
                orderDubboService = springHelper.getBean(PtGroupOrderServiceImpl.class);
                break;
            case DictionaryConst.OrdersType.PTHD_ORDER_PM:
                orderDubboService = springHelper.getBean(PtSecondsOrderSerciceImpl.class);
                break;
            case DictionaryConst.OrdersType.ORDERS_HEADER_MS:
                orderDubboService = springHelper.getBean(SecondsOrderSerciceImpl.class);
                break;
            case DictionaryConst.OrdersType.ORDERS_HEADER_IN:
                orderDubboService = springHelper.getBean(IntegralOrderServiceImpl.class);
                break;
            case DictionaryConst.OrdersType.ORDERS_HEADER_FX:
                orderDubboService = springHelper.getBean(DistributionOrderServiceImpl.class);
                break;
            case DictionaryConst.OrdersType.ORDERS_HEADER_PS:
                orderDubboService = springHelper.getBean(PreSellOrderServiceImpl.class);
                break;
            default:
                throw new LaiKeAPIException(PARAMATER_ERROR, "参数错误");
        }
        return orderDubboService;
    }

}
