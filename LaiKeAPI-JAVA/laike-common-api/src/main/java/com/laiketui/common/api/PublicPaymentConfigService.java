package com.laiketui.common.api;

import com.laiketui.domain.payment.PaymentModel;

import java.util.List;
import java.util.Map;

/**
 * 支付配置信息
 *
 * @author wangxian
 */
public interface PublicPaymentConfigService
{

    /**
     * 根据商户id获取支付配置
     *
     * @param storeId
     * @return
     */
    String getPaymentConfig(int storeId, String payType);

    /**
     * 根据订单支付号获取支付配置信息
     *
     * @param payNo
     * @return
     */
    String getPaymentConfigByPayNo(String payNo);

    String getPaymentConfigBySNo(String sNo);

    /**
     * 根据订单支付号获取支付配置信息
     *
     * @param payNo
     * @return
     */
    String getPaymentConfigByMemberPayNo(String payNo);


    /**
     * 获取平台所有支付类型
     *
     * @return
     */
    List<PaymentModel> getPayment();

    /**
     * 获取平台所有支付类型
     *
     * @return
     */
    Map<String, String> getPaymentMap();

    /**
     * 获取商城支付方式信息和默认支付类型
     *
     * @param storeId
     * @return
     */
    Map<String, Object> getPaymentInfos(int storeId);

    /**
     * 根据微信支付公钥id模糊查询
     * @param wechatPaySerial
     * @return
     */
    List<String> getV3config(String wechatPaySerial);

}
