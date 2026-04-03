package com.laiketui.core.config.alipay;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayConfig;
import com.alipay.easysdk.kernel.Config;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author wangxian
 */
public final class AlipayConfigInfo
{

    public static Config config = new Config();

    public static Config getAlipayConfig()
    {
        return config;
    }


    /**
     * 获取支付宝支付信息
     *
     * @param configStr
     * @return
     */
    public static Config getOptions(String configStr) throws LaiKeAPIException
    {
        try
        {
            configStr = URLDecoder.decode(configStr, GloabConst.Chartset.UTF_8);
            JSONObject payJsonObj = JSONObject.parseObject(configStr);

            config.protocol = "https";
            config.gatewayHost = "openapi.alipay.com";
            config.signType = "RSA2";
            config.appId = payJsonObj.getString("appid");
            config.merchantPrivateKey = payJsonObj.getString("rsaPrivateKey");
            config.alipayPublicKey = payJsonObj.getString("alipayrsaPublicKey");
            config.notifyUrl = payJsonObj.getString("notify_url");
            config.encryptKey = payJsonObj.getString("encryptKey");
            return config;
        }
        catch (LaiKeAPIException | UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static AlipayConfig getAlipayConfig(String json)
    {
        try
        {
            json = URLDecoder.decode(json, GloabConst.Chartset.UTF_8);
            JSONObject payJsonObj = JSONObject.parseObject(json);

            AlipayConfig alipayConfig = new AlipayConfig();
            alipayConfig.setAppId(payJsonObj.getString("appid"));
            alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
            alipayConfig.setPrivateKey(payJsonObj.getString("rsaPrivateKey"));
            alipayConfig.setAlipayPublicKey(payJsonObj.getString("alipayrsaPublicKey"));
            alipayConfig.setCharset("UTF-8");
            alipayConfig.setSignType(payJsonObj.getString("signType"));
            return alipayConfig;
        }
        catch (LaiKeAPIException | UnsupportedEncodingException e)
        {
            ExceptionUtils.getStackTrace(e);
        }
       return null;
    }
}
