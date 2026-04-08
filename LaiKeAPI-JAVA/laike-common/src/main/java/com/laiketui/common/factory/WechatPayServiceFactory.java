package com.laiketui.common.factory;

import com.laiketui.comps.api.payment.CompsWechatPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-09-03-14:43
 * @Description:
 */
@Component
public class WechatPayServiceFactory
{
    private final Map<String, CompsWechatPayService> serviceMap;

    @Autowired
    public WechatPayServiceFactory(Map<String, CompsWechatPayService> serviceMap)
    {
        this.serviceMap = serviceMap;
    }

    /**
     * 根据 status 获取对应的支付服务
     */
    public CompsWechatPayService getService(String status)
    {
        // status 和 beanName 对应关系，可以用配置文件或者枚举映射
        String beanName;
        switch (status)
        {
            case "v2":
                beanName = "compsWechatPayServiceV2DubboImpl";
                break;
            case "v3":
                beanName = "compsWechatPayServiceV3DubboImpl";
                break;
            default:
                throw new IllegalArgumentException("不支持的支付版本: " + status);
        }
        return serviceMap.get(beanName);
    }
}
