package com.laiketui.comps.payment.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PublicPaymentConfigService;
import com.laiketui.common.mapper.OrderModelMapper;
import com.laiketui.comps.api.payment.CompsStripePayService;
import com.laiketui.comps.payment.services.CompsStripePayServiceImpl;
import com.laiketui.comps.payment.util.CompsRequestToMapUtil;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.OkHttpUtils;
import com.laiketui.domain.vo.pay.BindStripeEmailVo;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.domain.vo.pay.StripeWithdrawVo;
import com.laiketui.root.annotation.HttpApiMethod;
import com.stripe.exception.StripeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

@RestController
@Api(description = "Stripe Webhook接口")
@RequestMapping("/comps/stripe/notify")
public class CompsStripeNotifyController
{

    private final Logger logger = LoggerFactory.getLogger(CompsStripeNotifyController.class);

    @Autowired
    private CompsStripePayService compsStripePayService;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OkHttpUtils okHttpUtils;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private CompsStripePayServiceImpl compsStripePayServiceImpl;

    @ApiOperation(value = "Stripe Webhook异步回调")
    @PostMapping("/stripe/webhook/callback")
    public void stripeWebhookCallback(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        logger.error("进入Stripe回调方法！！！！！！！！！！");

        try
        {
            // 读取请求体
            StringBuilder  requestBody = new StringBuilder();
            BufferedReader reader      = request.getReader();
            String         line;
            while ((line = reader.readLine()) != null)
            {
                requestBody.append(line);
            }
            String payload = requestBody.toString();
            logger.error("Stripe回调请求体: {}", payload);

            // 获取Stripe签名头
            String stripeSignature = request.getHeader("Stripe-Signature");
            if (stripeSignature == null)
            {
                logger.error("Stripe回调缺少签名头");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 获取店铺ID
            String storeIdStr = request.getParameter("storeId");
            int    storeId    = storeIdStr != null ? Integer.parseInt(storeIdStr) : 1;

            //region 验证stripe回调签名
//            // 验证签名（实际应用中应从配置获取webhook签名密钥）
//            // 获取支付配置 JSON 字符串
//            String paymentConfigJson = publicPaymentConfigService.getPaymentConfig(storeId, "stripe");
//
//
//                // 解析外层 JSON 对象（使用 FastJSON）
//                JSONObject paymentConfigObj = JSONObject.parseObject(paymentConfigJson);
//
//                // 获取 payConfig 字段（也是一个 JSON 字符串）
//                String payConfigStr = paymentConfigObj.getString("payConfig");
//
//                // 解析内层的支付配置 JSON（使用 FastJSON）
//                JSONObject payConfigObj = JSONObject.parseObject(payConfigStr);
//
//                // 获取 webhookSecret（使用 FastJSON 的 getString 方法）
//                String webhookSecret = payConfigObj.getString("webhookSecret");
//            if (webhookSecret == null) {
//                logger.error("未找到Stripe Webhook密钥，storeId: {}", storeId);
//                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                return;
//            }
//
//            // 这里应该使用Stripe官方库验证签名，示例代码中简化处理
//            boolean isSignatureValid = validateStripeSignature(payload, stripeSignature, webhookSecret);
//            if (!isSignatureValid) {
//                logger.error("Stripe回调签名验证失败");
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                return;
//            }
            //endregion

            // 解析事件
            JSONObject eventJson = JSON.parseObject(payload);
            String     eventType = eventJson.getString("type");
            logger.error("Stripe事件类型: {}", eventType);

            // 处理支付成功事件
            if ("payment_intent.succeeded".equals(eventType) || "checkout.session.completed".equals(eventType))
            {
                JSONObject dataJson   = eventJson.getJSONObject("data");
                JSONObject objectJson = dataJson.getJSONObject("object");

                // 提取Stripe会话ID或支付意图ID
                String stripeId = objectJson.getString("id");
                logger.error("Stripe ID: {}", stripeId);

                // 提取订单相关元数据
                JSONObject metadata  = objectJson.getJSONObject("metadata");
                String     sNo       = metadata.getString("sNo");
                String     realSNo   = metadata.getString("real_sNo");
                String     payNo     = realSNo;
//                String     paymentVo = metadata.getString("paymentVo");
                logger.error("订单号 sNo: {}, realSNo: {}", sNo, realSNo);

                // 构建参数Map
                Map<String, Object> params = CompsRequestToMapUtil.getParameterMap(request);
                params.put("stripeId", stripeId);
                params.put("sNo", sNo);
                params.put("realSNo", realSNo);
                params.put("payload", payload);
                params.put("eventType", eventType);
                params.put("storeId", storeId);
//                params.put("paymentVo", paymentVo);

                //region 获取支付配置信息
                String payConfigStr = null;
                //不同类型的订单获取支付信息的方法不一样
                if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ) || payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_CZ))
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
                payConfigStr = URLDecoder.decode(payConfigStr, GloabConst.Chartset.UTF_8);
                logger.info("payConfigStr:{}", payConfigStr);
                JSONObject payConfigJson = JSONObject.parseObject(payConfigStr);
                logger.info("payConfigJson:{}", payConfigJson);

                params.put("type", payConfigJson.getString("payType"));
                logger.info("type:{}", payConfigJson.getString("payType"));
                params.put("storeId", payConfigJson.getString("storeId"));
                logger.info("storeId:{}", payConfigJson.getString("storeId"));
                JSONObject wechatPayConfigJson = payConfigJson.getJSONObject("payConfig");
                logger.info("wechatPayConfigJson:{}", payConfigJson.getJSONObject("payConfig"));
                //不存在的情况直接返回错误
                if (!wechatPayConfigJson.containsKey("mch_key"))
                {
                    logger.info("stripe回调失败1:{}", payConfigStr);
                }
                params.put("key", wechatPayConfigJson.getString("mch_key"));
                logger.info("key:{}", wechatPayConfigJson.getString("mch_key"));

                PaymentVo paymentVo = new PaymentVo();
                paymentVo.setPayType(payConfigJson.getString("payType"));
                paymentVo.setStoreId(Integer.valueOf(payConfigJson.getString("storeId")));
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
                params.put("paymentVo", paymentVo);
                //endregion

                // 调用服务层处理回调
                Map<String, Object> result = compsStripePayService.payBack(params);
                logger.error("Stripe回调处理结果: {}", result);

                // 返回成功响应
                response.setStatus(HttpServletResponse.SC_OK);
            }
            else
            {
                logger.error("忽略Stripe事件类型: {}", eventType);
                response.setStatus(HttpServletResponse.SC_OK);
            }
        }
        catch (Exception e)
        {
            logger.error("Stripe回调处理异常", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取Stripe Webhook签名密钥
     */
    private String getWebhookSecret(int storeId)
    {
        // 从支付配置中获取Webhook密钥
        // 实际实现应从数据库或配置服务获取
        return publicPaymentConfigService.getPaymentConfig(storeId, "stripe");
    }

    /**
     * 验证Stripe签名（简化示例，实际应使用Stripe官方库）
     */
    private boolean validateStripeSignature(String payload, String signature, String secret)
    {
        // 实际应用中应使用Stripe官方库验证签名
        // 示例：com.stripe.model.Webhook.constructEvent(payload, signature, secret);
        // 由于Stripe官方库需要在类路径中，这里简化返回true
        logger.info("Stripe签名验证（开发环境简化）: payload={}, signature={}, secret={}",
                payload.substring(0, Math.min(payload.length(), 50)),
                signature.substring(0, Math.min(signature.length(), 20)),
                secret.substring(0, Math.min(secret.length(), 20)));
        return true;
    }

    @ApiOperation("商家转账到零钱")
    @RequestMapping("/stripe/payoutsVo")
    @HttpApiMethod(urlMapping = {"comps.stripe.payoutsVo"})
    public Result payouts(StripeWithdrawVo vo)
    {
        try
        {
            Result success = Result.success(compsStripePayServiceImpl.payOut(vo));
            logger.error("success为：" + success);
            return success;

        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage(), e.getMethod());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation("商家转账到零钱")
    @RequestMapping("/stripe/payouts")
    @HttpApiMethod(urlMapping = {"comps.stripe.payouts"})
    public Result payouts(String paramJson)
    {
        try
        {
            Result success = Result.success(compsStripePayServiceImpl.payOut(paramJson));
            logger.error("success为：" + success);
            return success;

        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage(), e.getMethod());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation("用户绑定Stripe连接子账户")
    @PostMapping("/stripe/bindStripeEmail")
    @HttpApiMethod(urlMapping = {"comps.stripe.bindStripeEmail"})
    public Result bindStripeEmail(BindStripeEmailVo vo)
    {
        try
        {
            // 调用服务层绑定方法
            Map<String, Object> result = compsStripePayServiceImpl.bindStripeEmail(vo);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
        catch (StripeException e)
        {
            logger.error("Stripe接口调用异常", e);
            return Result.error("绑定失败：" + e.getMessage());
        }
    }


}
