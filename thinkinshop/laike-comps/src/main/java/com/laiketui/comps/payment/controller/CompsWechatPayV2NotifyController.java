package com.laiketui.comps.payment.controller;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Maps;
import com.laiketui.common.api.PublicPaymentConfigService;
import com.laiketui.common.factory.WechatPayServiceFactory;
import com.laiketui.common.utils.weixin.WXPaySignatureCertificateUtil;
import com.laiketui.comps.payment.util.CompsAesUtil;
import com.laiketui.comps.payment.util.CompsWechatPayUtils;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.vo.pay.PaymentVo;
import github.wxpay.sdk.WXPayConstants;
import github.wxpay.sdk.WXPayUtil;
import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author wangxian
 * 微信回调逻辑
 */
@RestController
@RequestMapping("/comps/wechat")
public class CompsWechatPayV2NotifyController
{

    private final Logger logger = LoggerFactory.getLogger(CompsWechatPayV2NotifyController.class);

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Autowired
    private WechatPayServiceFactory factory;


    @RequestMapping("/weChatNotify")
    public void weChatNotify(HttpServletRequest request, HttpServletResponse response)
    {
        boolean jsonValid = false;
        String contentType = "text/xml;charset=UTF-8";
        try
        {
            Map params = Maps.newHashMap();
            String body = CompsWechatPayUtils.getRequestBody(request);

            NotifyData notifyData;

            jsonValid = isJsonValid(body);
            logger.info("返回格式是否是json:{}",jsonValid);

            contentType = jsonValid ? "application/json;charset=UTF-8" : "text/xml;charset=UTF-8";

            if (jsonValid)
            {
                notifyData = weChatNotifyV3(body,request);
            }
            else
            {
                notifyData = weChatNotifyV2(body,request);
            }
            String payNo = notifyData.getPayNo();
            logger.info("payNo:{}",payNo);
            if (StringUtils.isEmpty(payNo))
            {
                logger.info("微信回调无需处理（订单为空或状态非成功），原样返回成功");
                sendSuccess(response, jsonValid, contentType);
                return;
            }
            BigDecimal total = notifyData.getTotal();
            if (total == null)
            {
                total = BigDecimal.ZERO;
            }
            String appid = notifyData.getAppid();
            String mchId = notifyData.getMchId();
            String transactionId = notifyData.getTransactionId();
            String openid = notifyData.getOpenid();
            String notify = notifyData.getNotifyData();

            if (StringUtils.isNotEmpty(openid))
            {
                params.put("openId",openid);
                params.put("openid",openid);
            }

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
            if (StringUtils.isEmpty(payConfigStr))
            {
                throw new RuntimeException("微信回调获取支付配置失败");
            }
            payConfigStr = URLDecoder.decode(payConfigStr, GloabConst.Chartset.UTF_8);
            JSONObject payConfigJson = JSONObject.parseObject(payConfigStr);
            params.put("type", payConfigJson.getString("payType"));
            params.put("storeId", payConfigJson.getString("storeId"));
            JSONObject wechatPayConfigJson = getPayConfigObject(payConfigJson);
            logger.info("wechatPayConfigJson:{}", payConfigJson.getJSONObject("payConfig"));
            if (!jsonValid && StringUtils.isEmpty(wechatPayConfigJson.getString("mch_key")))
            {
                throw new RuntimeException("微信回调配置错误，缺少mch_key");
            }
            params.put("key", wechatPayConfigJson.getString("mch_key"));
            params.put("certPath", wechatPayConfigJson.getString("sslcert_path"));
            params.put("notifyData", notify);
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
            params.put("total", total);
            params.put("appid", appid);
            params.put("mchID", mchId);
            params.put("payNo", payNo);
            params.put("transactionId", transactionId);

            String status = jsonValid ? "v3" : "v2";
            factory.getService(status).payBack(params);
            sendSuccess(response,jsonValid,contentType);
        }
        catch (Exception e)
        {
            logger.error("微信回调失败:{}", e.getMessage(), e);
            String failureMsg = jsonValid ? "{\"code\": \"FAIL\", \"message\": \"失败\"}" : "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[系统异常]]></return_msg></xml>";
            sendFailure(response,failureMsg,contentType);
        }
    }

    private NotifyData weChatNotifyV2(String body,HttpServletRequest request) throws Exception
    {
        NotifyData  notifyData = new NotifyData();
        String weChatXml = request.getParameter("weChat_xml");
        logger.info(weChatXml);
        if (StringUtils.isEmpty(weChatXml) && !StringUtils.isEmpty(body))
        {
            weChatXml = body;
        }
        if (StringUtils.isEmpty(weChatXml))
        {
            return notifyData;
        }
        Map<String, String> notifyMap  = WXPayUtil.xmlToMap(weChatXml);
        String              returnCode = MapUtils.getString(notifyMap, "return_code");
        String              resultCode = MapUtils.getString(notifyMap, "result_code");
        if (WXPayConstants.SUCCESS.equals(returnCode) && WXPayConstants.SUCCESS.equals(resultCode))
        {
            String payNo = MapUtils.getString(notifyMap, "out_trade_no");
            BigDecimal total = new BigDecimal(MapUtils.getString(notifyMap, "total_fee"));
            String appid = MapUtils.getString(notifyMap, "appid");
            String mchId = MapUtils.getString(notifyMap, "mch_id");;

            notifyData.setPayNo(payNo);
            notifyData.setTotal(total);
            notifyData.setAppid(appid);
            notifyData.setMchId(mchId);
            notifyData.setTransactionId(MapUtils.getString(notifyMap, "transaction_id"));
            notifyData.setOpenid(MapUtils.getString(notifyMap, "openid"));
            notifyData.setNotifyData(weChatXml);
        }
        return notifyData;
    }

    private NotifyData weChatNotifyV3(String body,HttpServletRequest request) throws Exception
    {
        NotifyData  notifyData = new NotifyData();
        //微信支付公钥id/的微信支付平台证书
        String wechatPaySerial    = request.getHeader("Wechatpay-Serial");
        if (StringUtils.isEmpty(wechatPaySerial))
        {
            throw new RuntimeException("微信回调缺少平台证书序列号");
        }
        List<String> configList = publicPaymentConfigService.getV3config(wechatPaySerial);
        logger.info("支付配置信息：{}",JSONObject.toJSONString(configList));
        if (configList == null || configList.isEmpty())
        {
            throw new RuntimeException("未匹配到微信V3配置");
        }
        if (configList != null && !configList.isEmpty())
        {
            String config = configList.get(0);
            //这里要注意，数据库存的格式是否带%2B %2F，如果带的话就要编码
            config = URLDecoder.decode(config, GloabConst.Chartset.UTF_8);
            JSONObject configJson = JSONObject.parseObject(config);

            //微信支付公钥
            String pubPem = configJson.getString("pub_pem");
            String mch_id = configJson.getString("mch_id");
            //v3支付密钥
            String apIv3Key = configJson.getString("APIv3_key");

            logger.info("pubPem::::{}",pubPem);
            logger.info("mchId::::{}",mch_id);
            logger.info("apIv3Key::::{}",apIv3Key);
            try
             {
                 boolean verify = WXPaySignatureCertificateUtil.verifySign(request, body,pubPem);
                 if (!verify)
                 {
                     logger.error("微信验签失败");
                     throw new RuntimeException("验签失败");
                 }
             }
             catch (Exception e)
             {
                 logger.error("微信验签失败，错误信息：{}",e.getMessage());
                 throw new RuntimeException("验签失败");
             }

            //解密回调报文信息
            CompsAesUtil aesUtil       = new CompsAesUtil(apIv3Key.getBytes(StandardCharsets.UTF_8));
            JSONObject   notifyJsonObj = JSONObject.parseObject(body);

            JSONObject   resourceJson  = notifyJsonObj.getJSONObject("resource");
            if (resourceJson == null)
            {
                throw new RuntimeException("微信V3回调缺少resource字段");
            }
            String resultJsonStr = aesUtil.decryptToString(resourceJson.getString("associated_data").getBytes(StandardCharsets.UTF_8),
                    resourceJson.getString("nonce").getBytes(StandardCharsets.UTF_8), resourceJson.getString("ciphertext"));
            JSONObject jsonObject = JSONObject.parseObject(resultJsonStr);
            logger.info("jsonObject:{}",jsonObject);

            String trade_state = jsonObject.getString("trade_state");
            if (WXPayConstants.SUCCESS.equals(trade_state))
            {
                String payNo = jsonObject.getString("out_trade_no");
                JSONObject amount = JSONObject.parseObject(JSONObject.toJSONString(jsonObject.get("amount")));
                BigDecimal total = new BigDecimal(MapUtils.getString(amount, "total"));
                String appid = jsonObject.getString("appid");
                String mchId = jsonObject.getString("mchid");
                //微信支付微信生成的订单号
                String transactionId = jsonObject.getString("transaction_id");
                //支付者
                JSONObject payer = JSONObject.parseObject(JSONObject.toJSONString(jsonObject.get("payer")));
                String openid = MapUtils.getString(payer, "openid");
                String notify = JSONObject.toJSONString(jsonObject);

                notifyData.setPayNo(payNo);
                notifyData.setTotal(total);
                notifyData.setAppid(appid);
                notifyData.setMchId(mchId);
                notifyData.setTransactionId(transactionId);
                notifyData.setOpenid(openid);
                notifyData.setNotifyData(notify);
            }
        }
        return notifyData;
    }

    /**
     * @param response
     */
    private void sendSuccess(HttpServletResponse response,boolean jsonValid,String contentType)
    {
        // 设置响应状态码为200，表示成功
        response.setStatus(HttpServletResponse.SC_OK);
        String successMsg = null;
        if (jsonValid)
        {
            successMsg = "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        }
        else
        {
            successMsg = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        }
        writeResponseData(response, successMsg,contentType);
    }

    /**
     * @param response
     */
    private void sendFailure(HttpServletResponse response,String failureMsg,String contentType)
    {
        // 设置响应状态码为非200的值，比如500，表示失败
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        // 返回微信要求的失败响应XML格式数据
        writeResponseData(response, failureMsg,contentType);
    }

    /**
     * @param response
     * @param msg
     */
    private void writeResponseData(HttpServletResponse response, String msg,String contentType)
    {
        PrintWriter writer = null;
        try
        {
            logger.info("数据格式：{}",contentType);
            response.setContentType(contentType);
            writer = response.getWriter();
            if (StringUtils.isNotEmpty(msg))
            {
                writer.write(msg);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (writer != null)
            {
                writer.close();
            }
        }
    }


    /**
     * 判断是否是json格式
     * @param str
     * @return
     */
    private boolean isJsonValid(String str)
    {
        if (str == null || str.isEmpty())
        {
            return false;
        }
        try
        {
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(str);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private JSONObject getPayConfigObject(JSONObject payConfigJson)
    {
        JSONObject payConfigObj = payConfigJson.getJSONObject("payConfig");
        if (payConfigObj == null)
        {
            String payConfigStr = payConfigJson.getString("payConfig");
            if (StringUtils.isNotEmpty(payConfigStr))
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


    @Data
    public static class NotifyData
    {
        private String payNo;
        private BigDecimal total;
        private String appid;
        private String mchId;
        private String transactionId;
        private String openid;
        private String notifyData;
    }
}
