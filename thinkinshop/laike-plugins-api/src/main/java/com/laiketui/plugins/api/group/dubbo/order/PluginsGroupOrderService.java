package com.laiketui.plugins.api.group.dubbo.order;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.OrderVo;

import java.util.Map;

/**
 * 拼团订单
 *
 * @author Trick
 * @date 2023/4/24 15:15
 */
public interface PluginsGroupOrderService
{

    /**
     * 下单
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/24 15:16
     */
    Map<String, Object> placeOrder(OrderVo vo) throws LaiKeAPIException;
}
