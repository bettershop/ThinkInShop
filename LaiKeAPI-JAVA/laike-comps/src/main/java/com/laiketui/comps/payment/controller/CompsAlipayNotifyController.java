package com.laiketui.comps.payment.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Maps;
import com.laiketui.common.api.PublicPaymentConfigService;
import com.laiketui.comps.api.payment.CompsAlipayService;
import com.laiketui.comps.payment.util.CompsAlipayVerifyUtil;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.root.annotation.HttpApiMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Map;

/**
 * @author wangxian
 * 支付宝回调逻辑
 * 优化：
 * 1. 添加签名验证
 * 2. 添加幂等校验（Redis）
 * 3. 添加详细日志记录
 */
@RestController
@RequestMapping("/comps/ali/notify")
public class CompsAlipayNotifyController
{

    private final Logger logger = LoggerFactory.getLogger(CompsAlipayNotifyController.class);

    /**
     * 支付宝签名验证锁定Redis Key前缀
     */
    private static final String ALIPAY_NOTIFY_LOCK_PREFIX = "alipay:notify:lock:";

    /**
     * 支付宝签名验证锁定时间（秒）
     */
    private static final long NOTIFY_LOCK_TIMEOUT = 300L; // 5分钟

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CompsAlipayService compsAlipayService;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    /**
     * 支付宝公钥（从配置读取）
     */
    @Value("${alipay.publicKey:}")
    private String alipayPublicKey;

    /**
     * 字符编码
     */
    @Value("${alipay.charset:UTF-8}")
    private String alipayCharset;

    /**
     * 签名类型
     */
    @Value("${alipay.signType:RSA2}")
    private String alipaySignType;

    @RequestMapping("/alipayNotify")
    @HttpApiMethod(urlMapping = {"app.v2.alipayNotify"})
    public String alipayNotify(HttpServletRequest request)
    {
        long startTime = System.currentTimeMillis();
        Map<String, String> params = Maps.newHashMap();
        Map<String, String[]> requestParams = request.getParameterMap();

        try
        {
            // 解析请求参数
            for (String name : requestParams.keySet())
            {
                String[] values = requestParams.get(name);
                if (values != null && values.length > 0)
                {
                    params.put(name, values[0]);
                }
            }

            logger.info("支付宝异步回调开始, params={}", JSON.toJSONString(params));

            // 验证必要参数
            if (!CompsAlipayVerifyUtil.validateRequiredParams(params))
            {
                logger.error("支付宝回调缺少必要参数, params={}", JSON.toJSONString(params));
                return "fail";
            }

            // 检查交易状态
            String tradeStatus = params.get("trade_status");
            String payNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");

            if (payNo == null || payNo.isEmpty())
            {
                logger.error("支付宝回调缺少订单号, params={}", JSON.toJSONString(params));
                return "fail";
            }

            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus))
            {
                logger.warn("支付宝交易状态非成功, tradeStatus:{}, payNo:{}, tradeNo:{}",
                        tradeStatus, payNo, tradeNo);
                return "success"; // 非成功状态也返回success避免重复通知
            }

            // 获取支付配置
            String payConfigStr;
            if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ) ||
                    payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_CZ))
            {
                payConfigStr = publicPaymentConfigService.getPaymentConfigByMemberPayNo(payNo);
            }
            else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
            {
                payConfigStr = publicPaymentConfigService.getPaymentConfigByMemberPayNo(payNo);
            }
            else
            {
                payConfigStr = publicPaymentConfigService.getPaymentConfigByPayNo(payNo);
            }
            if (payConfigStr == null || payConfigStr.isEmpty())
            {
                logger.error("支付宝回调获取支付配置失败, payNo:{}, tradeNo:{}", payNo, tradeNo);
                return "fail";
            }
            payConfigStr = URLDecoder.decode(payConfigStr, GloabConst.Chartset.UTF_8);
            JSONObject payConfigJson = JSONObject.parseObject(payConfigStr);
            JSONObject payConfigObj = getPayConfigObject(payConfigJson);
            String publicKey = resolveAlipayPublicKey(payConfigObj);
            String charset = payConfigObj.getString("charset");
            if (charset == null || charset.isEmpty())
            {
                charset = alipayCharset;
            }
            String signType = payConfigObj.getString("signType");
            if (signType == null || signType.isEmpty())
            {
                signType = alipaySignType;
            }

            // 验证签名
            boolean signVerified = CompsAlipayVerifyUtil.verifyNotify(
                    params, publicKey, charset, signType);

            if (!signVerified)
            {
                logger.error("支付宝回调签名验证失败, payNo:{}, tradeNo:{}, params={}",
                        payNo, tradeNo, JSON.toJSONString(params));
                return "fail";
            }

            logger.info("支付宝回调签名验证成功, payNo:{}, tradeNo:{}", payNo, tradeNo);

            // 幂等校验（原子）：incr 首次为 1
            String lockKey = ALIPAY_NOTIFY_LOCK_PREFIX + payNo;
            long lockCount = redisUtil.incr(lockKey, 1L);
            if (lockCount == 1L)
            {
                redisUtil.expire(lockKey, NOTIFY_LOCK_TIMEOUT);
            }
            else
            {
                logger.warn("支付宝回调幂等校验失败，订单已处理, payNo:{}, tradeNo:{}", payNo, tradeNo);
                return "success"; // 已处理过，返回success
            }
            logger.info("支付宝回调支付配置, payNo:{}, config:{}", payNo, payConfigJson);

            // 构造支付对象
            PaymentVo paymentVo = new PaymentVo();
            paymentVo.setPayType(payConfigJson.getString("payType"));
            paymentVo.setStoreId(payConfigJson.getInteger("storeId"));
            paymentVo.setsNo(payNo);

            if (payConfigJson.getString("userId") != null)
            {
                paymentVo.setUserId(payConfigJson.getString("userId"));
            }
            if (payConfigJson.getString("map") != null)
            {
                paymentVo.setParameter(payConfigJson.getString("map"));
            }
            if (payConfigJson.getString("real_sno") != null)
            {
                paymentVo.setReal_sno(payConfigJson.getString("real_sno"));
            }
            if (payConfigJson.getString("storeType") != null)
            {
                paymentVo.setStoreType(payConfigJson.getInteger("storeType"));
            }

            // 执行支付回调处理
            Map<String, Object> callbackParams = Maps.newHashMap();
            callbackParams.putAll(params);
            callbackParams.put("paymentVo", paymentVo);

            compsAlipayService.payBack(callbackParams);

            long endTime = System.currentTimeMillis();
            logger.info("支付宝异步回调成功处理完成, payNo:{}, tradeNo:{}, 耗时:{}ms",
                    payNo, tradeNo, (endTime - startTime));

            // 锁定5分钟后自动释放，不需手动释放
            return "success";
        }
        catch (Exception e)
        {
            long endTime = System.currentTimeMillis();
            logger.error("支付宝回调处理异常, 耗时:{}ms, error:{}",
                    (endTime - startTime), e.getMessage(), e);

            try
            {
                // 异常情况下释放锁
                String payNo = params.get("out_trade_no");
                if (payNo != null)
                {
                    redisUtil.del(ALIPAY_NOTIFY_LOCK_PREFIX + payNo);
                }
            }
            catch (Exception ex)
            {
                logger.error("释放幂等锁失败: {}", ex.getMessage());
            }

            return "fail";
        }
    }

    private JSONObject getPayConfigObject(JSONObject payConfigJson)
    {
        JSONObject payConfigObj = payConfigJson.getJSONObject("payConfig");
        if (payConfigObj == null)
        {
            String payConfigStr = payConfigJson.getString("payConfig");
            if (payConfigStr != null && !payConfigStr.isEmpty())
            {
                payConfigObj = JSONObject.parseObject(payConfigStr);
            }
        }
        if (payConfigObj == null)
        {
            payConfigObj = new JSONObject();
        }
        return payConfigObj;
    }

    private String resolveAlipayPublicKey(JSONObject payConfigObj)
    {
        String publicKey = payConfigObj.getString("alipayrsaPublicKey");
        if (publicKey == null || publicKey.isEmpty())
        {
            publicKey = payConfigObj.getString("alipay_public_key");
        }
        if (publicKey == null || publicKey.isEmpty())
        {
            publicKey = alipayPublicKey;
        }
        if (publicKey == null)
        {
            return "";
        }
        return publicKey.replace("%2B", "+")
                .replace("%2F", "/")
                .replace("%3D", "=");
    }
}
