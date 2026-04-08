package com.laiketui.common.utils.alipay;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoAuthRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoAuthResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.root.common.BuilderIDTool;
import com.laiketui.root.license.Base64Utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sunH_
 */
public final class AliPayUtil
{

    /***
     * 支付宝用户授权，获取用户信息
     * @author Mark
     * @param code
     * @return
     */
    public static String getALiPayUserInfo(String code, String appId, String signType, String rsaPrivateKey, String alipayrsaPublicKey)
    {
        String result = "";
        try
        {
            //根据code获取accessToken
            rsaPrivateKey = rsaPrivateKey.replaceAll("%2B", "\\+");
            rsaPrivateKey = rsaPrivateKey.replaceAll("%2F", "\\/");
            rsaPrivateKey = rsaPrivateKey.replaceAll("%3D", "\\=");
            alipayrsaPublicKey = alipayrsaPublicKey.replaceAll("%2B", "\\+");
            alipayrsaPublicKey = alipayrsaPublicKey.replaceAll("%2F", "\\/");
            alipayrsaPublicKey = alipayrsaPublicKey.replaceAll("%3D", "\\=");
            AlipayClient                  alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, rsaPrivateKey, "json", "GBK", alipayrsaPublicKey, signType);
            AlipaySystemOauthTokenRequest request      = new AlipaySystemOauthTokenRequest();
            request.setGrantType("authorization_code");
            request.setCode(code);
            AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(request);
            result = oauthTokenResponse.toString();
            // 在使用token去支付宝获取用户信息
            if (oauthTokenResponse.isSuccess())
            {
                AlipayUserInfoShareResponse userInfoShareResponse = alipayClient.execute(new AlipayUserInfoShareRequest(), oauthTokenResponse.getAccessToken());
                if (userInfoShareResponse.isSuccess())
                {
                    JSONObject resultData  = JSONObject.parseObject(userInfoShareResponse.getBody());
                    JSONObject aliUserInfo = resultData.getJSONObject("alipay_user_info_share_response");
                    if (!aliUserInfo.isEmpty() && aliUserInfo.getInteger("code") == 10000)
                    {
                        result = aliUserInfo.toJSONString();
                    }
                    else
                    {
                        System.out.println("获取支付宝用户信息失败！");
                    }
                }
                else
                {
                    System.out.println("调用【alipayUserInfoShare】接口失败！");
                }
            }
            else
            {
                System.out.println("调用【alipaySystemOauthToken】接口失败！");
            }
        }
        catch (AlipayApiException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 用户登录授权APP(非小程序场景)
     *
     * @param appId           - appId
     * @param signType        - 签名方式
     * @param privateKey      - 私钥
     * @param alipayPublicKey - 公钥
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/10 11:06
     */
    public static String getALiPayUserInfoByApp(String appId, String signType, String privateKey, String alipayPublicKey) throws LaiKeAPIException
    {
        String result = "";
        try
        {
            privateKey = privateKey.replaceAll("%2B", "\\+");
            privateKey = privateKey.replaceAll("%2F", "\\/");
            privateKey = privateKey.replaceAll("%3D", "\\=");
            alipayPublicKey = alipayPublicKey.replaceAll("%2B", "\\+");
            alipayPublicKey = alipayPublicKey.replaceAll("%2F", "\\/");
            alipayPublicKey = alipayPublicKey.replaceAll("%3D", "\\=");
            AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, privateKey, "json", "GBK", alipayPublicKey, signType);

            AlipayUserInfoAuthRequest request = new AlipayUserInfoAuthRequest();
            Map<String, Object>       parma   = new HashMap<>(16);
            //scopes参数
            List<Map<String, Object>> scopesList = new ArrayList<>();
            Map<String, Object>       scopesMap  = new HashMap<>(16);
            scopesMap.put("auth_base", "");
            scopesList.add(scopesMap);
            parma.put("scopes", scopesList);
            parma.put("state", "init");
            request.setBizContent(JSON.toJSONString(parma));
            AlipayUserInfoAuthResponse response = alipayClient.pageExecute(request, "get");
            result = response.getBody();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 用户登录授权h5(非小程序场景)
     *
     * @param appId  - appId
     * @param domain - h5域名
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/10 11:06
     */
    public static String getAliPayUserInfoByWeb(String appId, Integer storeType, String domain) throws LaiKeAPIException
    {
        String result = "";
        try
        {
            String state       = Base64Utils.encode(BuilderIDTool.getGuid().getBytes(StandardCharsets.UTF_8));
            String redirectUri = URLEncoder.encode(domain + "pages/tabBar/my?state=" + state, "GBK");
            if (GloabConst.StoreType.STORE_TYPE_APP == storeType)
            {
                redirectUri = URLEncoder.encode(domain + "/za.html?state=" + state, "GBK");
            }
            String apiUrl = String.format(GloabConst.AlibabaApiUrl.ALIPAY_GET_URL_OAUTH2_AUTHORIZE_H5, appId, "auth_user", redirectUri);
            result = String.format(GloabConst.AlibabaApiUrl.ALIPAY_GET_URL_OAUTH2_AUTHORIZE_H5_SCHEME, URLEncoder.encode(apiUrl, "GBK"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

}
