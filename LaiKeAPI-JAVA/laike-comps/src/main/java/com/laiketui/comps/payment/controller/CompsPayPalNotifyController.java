package com.laiketui.comps.payment.controller;


import com.alibaba.fastjson2.JSONObject;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PublicPaymentConfigService;
import com.laiketui.common.mapper.OrderModelMapper;
import com.laiketui.common.service.dubbo.PublicPaymentConfigServiceImpl;
import com.laiketui.comps.api.payment.CompsPaypalPayService;
import com.laiketui.comps.payment.services.CompsPaypalPayServiceImpl;
import com.laiketui.comps.payment.util.CompsRequestToMapUtil;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.OkHttpUtils;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Map;

@RestController
@Api(description = "PayPalCheckout接口")
@RequestMapping("/comps/palpay/notify")
public class CompsPayPalNotifyController
{
    private final Logger logger = LoggerFactory.getLogger(PublicPaymentConfigServiceImpl.class);

    @Autowired
    private CompsPaypalPayService compsPaypalPayService;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OkHttpUtils okHttpUtils;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private CompsPaypalPayServiceImpl compsPaypalPayServiceImpl;

    @ApiOperation(value = "ipn异步回调")
    @PostMapping(value = "/paypal/ipn/back")
    public void callback(HttpServletRequest request, HttpServletResponse response)
    {
        System.out.println("进入贝宝回调方法！！！！！！！！！！");

        try
        {
            /**贝宝返回的请求体*/
            StringBuffer url    = request.getRequestURL();
            String       domain = url.delete(url.length() - request.getRequestURI().length(), url.length()).append("/").toString();
            Map          params = CompsRequestToMapUtil.getParameterMap(request);
            logger.info("贝宝回调参数" + params.toString());


            //获取real_sno
//            String     sNo        = DataUtils.getStringVal(params, "custom");
////            OrderModel orderModel = orderModelMapper.selectBySno(sNo);
//            OrderModel       orderModel = publicOrderService.getOrderInfo(sNo);
//            String     payNo      = orderModel.getReal_sno();
//            String payNo = DataUtils.getStringVal(params, "custom");
            String payNo = DataUtils.getStringVal(params, "reference_id");


            BigDecimal total = new BigDecimal(MapUtils.getString(params, "payment_gross"));
            params.put("total", total);

            params.put("payNo", payNo);


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
                logger.info("贝宝v2回调失败1:{}", payConfigStr);
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
            Map map = compsPaypalPayService.payBack(params);
            logger.info("贝宝v2回调成功 处理结果：{}", map);

        }
        catch (Exception e)
        {
            logger.error("贝宝v2回调失败", e);

        }
    }

    @ApiOperation("商家转账到零钱")
    @RequestMapping("/v1/payouts")
    @HttpApiMethod(urlMapping = {"v1.payouts"})
    public Result payouts(String paramJson)
    {
        try
        {
            Result success = Result.success(compsPaypalPayServiceImpl.payOut(paramJson));
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
}


