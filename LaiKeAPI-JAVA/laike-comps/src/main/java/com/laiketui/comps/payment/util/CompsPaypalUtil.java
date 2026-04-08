package com.laiketui.comps.payment.util;

import com.paypal.sdk.Environment;
import com.paypal.sdk.PaypalServerSDKClient;
import com.paypal.sdk.authentication.ClientCredentialsAuthModel;
import org.slf4j.event.Level;

public class CompsPaypalUtil
{
    //API 客户端可以按如下方式初始化
    public PaypalServerSDKClient getPaypalClient(String OAuthClientId, String OAuthClientSecret)
    {
        PaypalServerSDKClient client = new PaypalServerSDKClient.Builder()
                .loggingConfig(builder -> builder
                        .level(Level.DEBUG)
                        .requestConfig(logConfigBuilder -> logConfigBuilder.body(true))
                        .responseConfig(logConfigBuilder -> logConfigBuilder.headers(true)))
                .httpClientConfig(configBuilder -> configBuilder
                        .timeout(0))
                .clientCredentialsAuth(new ClientCredentialsAuthModel.Builder(
//                    "OAuthClientId",
//                    "OAuthClientSecret"
                        OAuthClientId, OAuthClientSecret
                )
                        .build())
                .environment(Environment.SANDBOX)
                .build();

        return client;
    }


}
