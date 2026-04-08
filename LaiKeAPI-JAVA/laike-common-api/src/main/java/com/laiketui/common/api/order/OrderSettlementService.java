package com.laiketui.common.api.order;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 订单结算
 *
 * @author Trick
 * @date 2021/7/7 11:31
 */
public interface OrderSettlementService
{

    /**
     * 订单结算 列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 11:38
     */
    Map<String, Object> index(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 订单详情
     *
     * @param vo      -
     * @param orderNo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 14:51
     */
    Map<String, Object> detail(MainVo vo, String orderNo) throws LaiKeAPIException;

    /**
     * 删除结算订单
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 15:04
     */
    void del(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 插件-订单结算 列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 11:38
     */
    Map<String, Object> pluginIndex(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 插件-订单详情
     *
     * @param vo      -
     * @param orderNo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 14:51
     */
    Map<String, Object> pluginDetail(MainVo vo, String orderNo) throws LaiKeAPIException;

    /**
     * 插件-删除结算订单
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 15:04
     */
    void pluginDel(MainVo vo, int id) throws LaiKeAPIException;
}
