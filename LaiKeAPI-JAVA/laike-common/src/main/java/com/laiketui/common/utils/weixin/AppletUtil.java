package com.laiketui.common.utils.weixin;


import com.alibaba.fastjson2.JSONObject;
import com.laiketui.core.utils.okhttp.HttpUtils;

import java.util.Map;

/**
 * @author sunH_
 */
public final class AppletUtil
{

    public static String sendMessage(String accessToken, String openId, String templateId, Map<String, Object> map)
    {
        String     url  = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
        JSONObject json = new JSONObject();
        json.put("touser", openId);
        json.put("template_id", templateId);
        json.put("data", map);
        // 发起请求并返回
        return HttpUtils.post(url, json.toJSONString());
    }
}
