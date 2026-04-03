package com.laiketui.plugins.api.auction.order;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.OrderVo;

import java.util.Map;

/**
 * 订单接口
 *
 * @author Trick
 * @date 2022/7/27 14:28
 */
public interface PluginAuctionOrderService
{


    /**
     * 结算页面
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/27 14:43
     */
    Map<String, Object> settlement(OrderVo vo) throws LaiKeAPIException;

    /**
     * 下单
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/27 14:43
     */
    Map<String, Object> placeOrder(OrderVo vo) throws LaiKeAPIException;

    /**
     * 获取插件订单配置信息
     *
     * @param storeId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/18 19:30
     */
    Map<String, Object> getPluginConfig(int storeId) throws LaiKeAPIException;
}
