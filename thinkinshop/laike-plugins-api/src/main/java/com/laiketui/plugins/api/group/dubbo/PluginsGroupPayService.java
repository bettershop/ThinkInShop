package com.laiketui.plugins.api.group.dubbo;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.Map;

/**
 * 拼团支付
 *
 * @author Trick
 * @date 2023/4/4 15:43
 */
public interface PluginsGroupPayService
{

    /**
     * 支付回调接口
     *
     * @param paramJson - PaymentVo
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/4 15:44
     */
    Map<String, Object> payCallBack(String paramJson) throws LaiKeAPIException;

}
