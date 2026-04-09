package com.laiketui.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: liuao
 * @Date: 2025-08-19-10:37
 * @Description:
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WxConfig
{
    private List<PaymentConfig> paymentConfigs;
}
