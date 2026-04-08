package com.laiketui.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PayMappingUtils
{

    public static final Map<String, String> PAY_CLASSNAME_MAPPING;

    static
    {
        Map<String, String> map = new HashMap<>();
        map.put("app_wechat", "wxPay");
        map.put("h5_wechat", "wxPay");
        map.put("pc_wechat", "wxPay");
        map.put("mini_wechat", "wxPay");
        map.put("wechat_v3_withdraw", "wxPay");
        map.put("jsapi_wechat", "wxPay");

        map.put("alipay", "aliPay");
        map.put("pc_alipay", "aliPay");
        map.put("alipay_mobile", "aliPay");
        map.put("tt_alipay", "aliPay");
        map.put("alipay_minipay", "aliPay");

        map.put("baidu_pay", "baidupayStatue");
        map.put("wallet_pay", "balance");
        map.put("stripe", "stripe");
        map.put("paypal", "paypal");

        PAY_CLASSNAME_MAPPING = Collections.unmodifiableMap(map);
    }

    public static String getPayName(String className)
    {
        if (className == null) return null;
        String key = className.toLowerCase();
        return PAY_CLASSNAME_MAPPING.getOrDefault(key, null);
    }
}
