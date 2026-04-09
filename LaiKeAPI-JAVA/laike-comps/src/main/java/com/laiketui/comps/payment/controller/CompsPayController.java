package com.laiketui.comps.payment.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Maps;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.factory.WechatPayServiceFactory;
import com.laiketui.comps.api.payment.*;
import com.laiketui.comps.payment.services.CompsPaypalPayServiceImpl;
import com.laiketui.core.annotation.Idempotency;
import com.laiketui.core.annotation.PreventOrderResubmit;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/comps/pay")
public class CompsPayController
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CompsAlipayService compsAlipayService;

    @Autowired
    private CompsWalletPayService compsWalletPayService;

   /* @Autowired
    private CompsWechatPayService compsWechatPayServiceV3DubboImpl;*/

    @Autowired
    private WechatPayServiceFactory factory;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private CompsPayService payTransferService;

    @Autowired
    private CompsPaypalPayServiceImpl compsPaypalPayServiceImpl;

    @Autowired
    private CompsStripePayService compsStripePayService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 支付宝支付
     *
     * @param vo 用户所选支付类型 wallet：钱包 ali：支付宝 wx：微信
     * @return
     */
    @PostMapping("/wallet_pay")
    @ApiOperation("支付入口")
    @HttpApiMethod(urlMapping = {"app.pay.wallet_pay", "app.pay.index", "mall.Pay.walletPay", "mall.Pay.index"})
    @Idempotency
    @PreventOrderResubmit(keyExpr = "#vo.accessId + ',' + #vo.sNo")
    public Result index(PaymentVo vo)
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            Map<String, Object> params = new HashMap<>(16);
            params.put("paymentVo", vo);
            Integer orderId = vo.getOrder_id();
            logger.info("orderId{}",orderId);
            params.put("sNo", vo.getsNo());
            switch (vo.getPayType())
            {
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_MINIPAY:
                    resultMap = compsAlipayService.pay(params);
                    return Result.success(resultMap);
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_TMP:
                    resultMap = compsAlipayService.pay(params);
                    return Result.success(resultMap.get("body"));
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_ALIPAY:
                    resultMap = compsAlipayService.pay(params);
                    return Result.success(resultMap.get("qr_code"));
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_WAP:
                    resultMap = compsAlipayService.pay(params);
                    return Result.success(resultMap.get("data"));
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_H5_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_JSAPI_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_APP_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_WECHAT:

                    //根据是否设置v3 key判断调用v2还是v3支付
                    Map<String, Object> payConfig = publicOrderService.getPayConfig(vo.getPayType(), vo.getStoreId());
                    JSONObject jsonObject = JSON.parseObject((String) payConfig.get("data"));
                    String apIv3Key = jsonObject.getString("APIv3_key");
                    String status = StringUtils.isEmpty(apIv3Key) ? "v2" : "v3";
                    logger.info("调用{}微信支付",status);
                    CompsWechatPayService wechatPayService = factory.getService(status);
                    resultMap = wechatPayService.pay(params);
                    resultMap = (Map<String, Object>) resultMap.get("data");
                    if (vo.getPayType().equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_WECHAT))
                    {
                        return Result.success(resultMap.get("code_url"));
                    }
                    break;
                //PayPal 贝宝支付
                case DictionaryConst.OrderPayType.PAYPAL_PAY:
                    resultMap = compsPaypalPayServiceImpl.pay(params);
                    return Result.success(resultMap);
                //Stripe 支付
                case DictionaryConst.OrderPayType.STRIPE_PAY:
                    resultMap = compsStripePayService.pay(params);
                    return Result.success(resultMap);
                //钱包支付
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY:
                    resultMap = compsWalletPayService.pay(params);
                    break;
                default:
            }
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            redisUtil.del("lock:order:" + vo.getsNo());
            publicOrderService.reBackOrder(vo);
            return Result.error(e.getCode(), e.getMessage(), e.getMethod());
        }
    }

    @PostMapping("/taskRefund")
    @ApiOperation("定时任务退款")
    @HttpApiMethod(apiKey = "task.order.refund")
    @Idempotency
    public Result taskRefund(String params)
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            logger.error("定时任务退款-params：" + JSON.toJSONString(params));
            Map<String, Object> paramMap = new HashMap<>(1);
            try
            {
                paramMap = JSON.parseObject(params, new TypeReference<Map<String, Object>>()
                {
                });
            }
            catch (JSONException ignored)
            {
                logger.error("微信回调parameter参数错误:{}", params);
            }
            return Result.success(payTransferService.refund(paramMap));
        }
        catch (LaiKeAPIException e)
        {

            return Result.error(e.getCode(), e.getMessage(), e.getMethod());
        }
    }

    @PostMapping("getPayConfig")
    @ApiOperation("获取支付配置")
    @HttpApiMethod(urlMapping = {"app.order.get_config"})
    public Result getPayConfig(PaymentVo paymentVo)
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            // 接收参数
            String url  = URLEncoder.encode(paymentVo.getUrl(), Charsets.UTF_8.name()); // 链接
            String type = paymentVo.getType();// 支付类名称
            // 返回参数
            resultMap.put("url", url);
            Map<String, Object> data = publicOrderService.getPayConfig(type,paymentVo.getStoreId());
            JSONObject jsonObject = JSON.parseObject((String) data.get("data"));
            resultMap.put("config", jsonObject);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage(), e.getMethod());
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return Result.error(ErrorCode.SysErrorCode.FUNCTION_NOT_EXISTS, e.getMessage(), "getPayConfig");
        }
    }

    @PostMapping("/capture")
    @ApiOperation("贝宝执行扣款")
    @HttpApiMethod(apiKey = "app.pay.capture")
    public Result captureOrder(MainVo vo, String orderId, String sNo) throws IOException
    {
        try
        {
            return Result.success(compsPaypalPayServiceImpl.captureOrder(vo, orderId, sNo));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


}
