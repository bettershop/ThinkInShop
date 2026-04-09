package com.laiketui.comps.api.payment;

import com.laiketui.core.exception.LaiKeAPIException;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author wangxian
 */
public interface CompsPayService
{
    /**
     * 支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> pay(Map params) throws LaiKeAPIException;

    /**
     * 退款
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> refund(Map params) throws LaiKeAPIException;


    /**
     * 退款 场景：刷卡支付、公共号支付、扫码支付、APP支付、h5（可能不支持，待测）
     *
     * @param storeId     - 商城id
     * @param oid         - 订单id
     * @param refundAmt   - 退款金额
     * @param isTempOrder - 是否临时订单
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/12 21:01
     */
    default Map<String, Object> refund(int storeId, Integer oid, BigDecimal refundAmt, boolean isTempOrder) throws LaiKeAPIException
    {
        return null;
    }


    /**
     * 回调
     *
     * @param params
     * @return
     */
    Map<String, Object> payBack(Map params) throws LaiKeAPIException;

    /**
     * 获取支付信息
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getPayConfig(Map params) throws LaiKeAPIException;


}
