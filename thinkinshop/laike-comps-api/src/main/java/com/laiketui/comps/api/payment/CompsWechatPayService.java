package com.laiketui.comps.api.payment;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 微信支付
 *
 * @author wangxian
 */
public interface CompsWechatPayService extends CompsPayService
{

    /**
     * 微信app支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> wechatAppPay(Map<String, Object> params) throws LaiKeAPIException;

    /**
     * 微信手机浏览器支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> weChatWapPay(Map<String, Object> params) throws LaiKeAPIException;

    /**
     * 微信pc支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> wechatPcQrPay(Map<String, Object> params) throws LaiKeAPIException;

    /**
     * 微信小程序支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> wechatMiniPay(Map<String, Object> params) throws LaiKeAPIException;

    /**
     * 微信公众号支付
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> wechatJsapiPay(Map<String, Object> params) throws LaiKeAPIException;

    /**
     * 商家转账到零钱
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    boolean MerchantTransfersToChange(String paramJson) throws LaiKeAPIException;

    /**
     * 商家转账到零钱查询批次转账单结果
     *
     * @param vo
     */
    void QueryBatchTransferOrder(MainVo vo);
}
