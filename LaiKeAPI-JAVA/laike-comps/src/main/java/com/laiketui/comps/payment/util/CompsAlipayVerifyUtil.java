package com.laiketui.comps.payment.util;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 支付宝回调验证工具类
 * 提供签名验证、幂等校验等功能
 *
 * @author System
 * @date 2026-03-15
 */
public class CompsAlipayVerifyUtil
{
    private static final Logger logger = LoggerFactory.getLogger(CompsAlipayVerifyUtil.class);

    /**
     * 验证支付宝回调签名
     *
     * @param params 回调参数
     * @param publicKey 支付宝公钥
     * @param charset 编码
     * @param signType 签名类型 (RSA/RSA2)
     * @return true:验证通过 false:验证失败
     */
    public static boolean verifyNotify(Map<String, String> params, String publicKey, String charset, String signType)
    {
        try
        {
            if (params == null || params.isEmpty())
            {
                logger.error("支付宝回调参数为空");
                return false;
            }

            String sign = params.get("sign");
            if (publicKey == null || publicKey.trim().isEmpty())
            {
                logger.error("支付宝公钥为空");
                return false;
            }

            if (sign == null || sign.isEmpty())
            {
                logger.error("支付宝回调签名参数为空");
                return false;
            }

            boolean verified = AlipaySignature.rsaCheckV1(
                    Maps.newHashMap(params), publicKey, charset, signType);

            if (!verified)
            {
                logger.error("支付宝回调签名验证失败, params={}", params);
            }
            else
            {
                logger.info("支付宝回调签名验证成功");
            }

            return verified;
        }
        catch (AlipayApiException e)
        {
            logger.error("支付宝签名验证异常: {}", e.getMessage(), e);
            return false;
        }
        catch (Exception e)
        {
            logger.error("支付宝签名验证未知异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证支付宝回调的必要参数
     *
     * @param params 回调参数
     * @return true:参数完整 false:参数缺失
     */
    public static boolean validateRequiredParams(Map<String, String> params)
    {
        if (params == null)
        {
            logger.error("支付宝回调参数为null");
            return false;
        }

        String[] requiredParams = {"app_id", "out_trade_no", "trade_no", "trade_status",
                                   "total_amount", "notify_time", "sign", "sign_type"};

        for (String param : requiredParams)
        {
            if (!params.containsKey(param) || params.get(param) == null || params.get(param).isEmpty())
            {
                logger.error("支付宝回调缺少必要参数: {}", param);
                return false;
            }
        }

        return true;
    }
}
