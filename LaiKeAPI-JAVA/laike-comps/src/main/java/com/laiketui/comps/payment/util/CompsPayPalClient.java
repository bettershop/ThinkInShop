package com.laiketui.comps.payment.util;


import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompsPayPalClient
{
    public PayPalHttpClient client(String mode, String clientId, String clientSecret)
    {
        log.info("mode={}, clientId={}, clientSecret={}", mode, clientId, clientSecret);
        PayPalEnvironment environment = mode.equals("live") ? new PayPalEnvironment.Live(clientId, clientSecret) : new PayPalEnvironment.Sandbox(clientId, clientSecret);
        return new PayPalHttpClient(environment);
    }
}


