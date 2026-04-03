package com.laiketui.comps.api.payment;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.Map;


public interface CompsAlipayService extends CompsPayService
{

    /**
     * 支付宝app支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> aliAppPay(Map params) throws LaiKeAPIException;

    /**
     * 支付宝手机浏览器支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> aliWapPay(Map params) throws LaiKeAPIException;

    /**
     * 支付宝pc电脑支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> aliPcQrPay(Map params) throws LaiKeAPIException;

    /**
     * 支付宝小程序支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> aliMiNiPay(Map params) throws LaiKeAPIException;

}
