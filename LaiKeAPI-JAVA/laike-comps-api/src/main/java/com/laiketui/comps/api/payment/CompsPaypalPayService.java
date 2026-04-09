package com.laiketui.comps.api.payment;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.paypal.orders.OrderRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface CompsPaypalPayService extends CompsPayService
{
    Map<String, Object> payBackLocal(MainVo vo, String payNo) throws LaiKeAPIException, UnsupportedEncodingException;

    /**
     * 生成订单主体信息
     */
    OrderRequest buildRequestBody(Map params);

    /**
     * 创建订单的方法
     *
     * @throws
     */
    String createOrder(String mode, Map params) throws IOException;

}
