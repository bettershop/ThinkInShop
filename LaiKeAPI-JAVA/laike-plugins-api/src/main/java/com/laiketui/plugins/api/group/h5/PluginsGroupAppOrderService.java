package com.laiketui.plugins.api.group.h5;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;

import java.util.Map;

/**
 * 订单相关接口
 *
 * @author Trick
 * @date 2023/3/30 20:40
 */
public interface PluginsGroupAppOrderService
{


    /**
     * 结算页面
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2023-03-30 20:40:18
     */
    Map<String, Object> settlement(OrderVo vo) throws LaiKeAPIException;

    /**
     * 生成临时订单
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/24 16:27
     */
    Map<String, Object> paymentData(OrderVo vo) throws LaiKeAPIException;

    /**
     * 下单
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023-03-30 20:40:18
     */
    Map<String, Object> placeOrder(OrderVo vo) throws LaiKeAPIException;


    /**
     * 确认收货
     *
     * @param orderVo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/19 19:42
     */
    Map<String, Object> okOrder(OrderVo orderVo) throws LaiKeAPIException;

    /**
     * 我的拼团订单
     *
     * @param vo         -
     * @param type       - 1=开团，2=参团 默认开团
     * @param openStatus - 1=开团，2=参团 默认开团
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/3 15:12
     */
    Map<String, Object> myOrderList(MainVo vo, Integer type, Integer openStatus, String startDate, String endDate, Integer isSettlement) throws LaiKeAPIException;


    void test();

    Map<String, Object> isOutOfRange(OrderVo vo);
}
