package com.laiketui.common.utils.weixin;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: liuao
 * @Date: 2025-08-18-11:24
 * @Description:获取设备类型
 */
public class DeviceUtils
{

    public static String getUserAgent(HttpServletRequest request)
    {
        return request.getHeader("User-Agent");
    }

    public static String getDeviceType(HttpServletRequest request)
    {
        String userAgent = getUserAgent(request);
        if (userAgent.contains("Android"))
        {
            //安卓
            return "Android";
        }
        else if (userAgent.contains("iPhone") || userAgent.contains("iPad"))
        {
            //苹果
            return "iOS";
        }
        else if (userAgent.contains("Mobi"))
        {
            return "Wap";
        }
        else
        {
            return "Unknown";
        }
    }
}
