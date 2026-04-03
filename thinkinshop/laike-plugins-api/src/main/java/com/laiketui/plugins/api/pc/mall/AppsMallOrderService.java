package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 订单接口
 *
 * @author Trick
 * @date 2021/6/25 10:21
 */
public interface AppsMallOrderService
{

    /**
     * 我的订单
     *
     * @param vo        -
     * @param orderType -
     * @param keyWord   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/22 16:08
     */
    Map<String, Object> myIndex(MainVo vo, String orderType, String keyWord,String order) throws LaiKeAPIException;


    /**
     * 订单详情
     *
     * @param vo      -
     * @param orderId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-25 11:33:00
     */
    Map<String, Object> myOrderDetail(MainVo vo, Integer orderId,String trade_no) throws LaiKeAPIException;

    /**
     * 获取支付结果
     *
     * @param vo
     * @param tradeNo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getCzOrder(MainVo vo, String tradeNo) throws LaiKeAPIException;

}
