package com.laiketui.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.common.service.dubbo.PublicOrderServiceImpl;
import com.laiketui.core.common.SplitUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author: liuao
 * @Date: 2025-12-30-10:02
 * @Description:
 */
public class UrlParamUtils
{

    private static final Logger logger = LoggerFactory.getLogger(UrlParamUtils.class);


    public static Map<String, String> parseUrlParams(String fullParams)
    {
        Map<String, String> paramMap = new HashMap<>();
        if (fullParams == null || fullParams.isEmpty())
        {
            return paramMap;
        }

        // 按 & 拆分参数项
        String[] paramItems = fullParams.split("&");
        for (String item : paramItems)
        {
            if (item.isEmpty())
            {
                continue;
            }
            int equalIndex = item.indexOf("=");
            if (equalIndex == -1)
            {
                paramMap.put(item, "");
            }
            else
            {
                String key = item.substring(0, equalIndex);
                String value = item.substring(equalIndex + 1);
                paramMap.put(key, value);
            }
        }
        return paramMap;
    }

    public static String getParamValue(String fullParams)
    {
        Map<String, String> paramMap = parseUrlParams(fullParams);
        // 2. 提取param的原始值（可能是编码/未编码的）
        String paramRawValue = paramMap.getOrDefault("param", "");
        // 3. 解码（兼容编码/未编码两种情况：未编码时解码后内容不变）
        return decodeUrlParam(paramRawValue);
    }

    public static String decodeUrlParam(String encodedStr)
    {
        if (encodedStr == null || encodedStr.isEmpty())
        {
            return "";
        }
        try
        {
            return URLDecoder.decode(encodedStr, StandardCharsets.UTF_8.name());
        }
        catch (Exception e)
        {
            throw new RuntimeException("URL解码失败", e);
        }
    }


    public static List<Object> splitJsonArraysBySeparator(String originalStr)
    {
        List<Object> resultList = new ArrayList<>();
        if (StringUtils.isEmpty(originalStr))
        {
            return resultList;
        }

        // 1. 预处理：统一格式，消除换行/中文逗号/多余空格
        String cleanStr = originalStr
                .replaceAll("\\s+", " ")
                .replace("，", ",")
                .trim();

        // 2. 按数组间分隔符 "] , [" 拆分（仅拆分数组之间的逗号）
        String[] arrayParts = cleanStr.split("]\\s*,\\s*\\[");

        // 3. 补全JSON数组边界，解析为JSONArray（确保元素是JSONObject而非String）
        for (int i = 0; i < arrayParts.length; i++) {
            String part = arrayParts[i].trim();
            // 补全[]，保证是合法的JSON数组格式
            if (i == 0) {
                part = part.endsWith("]") ? part : part + "]";
            } else if (i == arrayParts.length - 1) {
                part = part.startsWith("[") ? part : "[" + part;
            } else {
                part = "[" + part + "]";
            }

            // 关键：用fastjson2解析为JSONArray，确保元素是JSONObject
            try
            {
                JSONArray jsonArray = JSON.parseArray(part);
                // 逐个添加JSONObject到结果列表（避免直接addAll导致类型问题）
                for (int j = 0; j < jsonArray.size(); j++) {
                    resultList.add(jsonArray.getJSONObject(j));
                }
            } catch (Exception e) {
                System.err.println("解析JSON数组片段失败：" + part + "，异常：" + e.getMessage());
            }
        }
        return resultList;
    }

    /**
     * 17 track物流字段转成快递100需要的字段
     * @param originalList
     * @return
     */
    public static List<Object> convertLogisticsFields(List<Object> originalList)
    {
        List<Object> convertedList = new ArrayList<>();
        if (originalList.isEmpty())
        {
            return convertedList;
        }

        for (Object obj : originalList)
        {
            JSONObject jsonObj = (JSONObject) obj;
            JSONObject newObj = new JSONObject();

            // 1. 转换time_raw：date+time拼接为time字段，如果时间不对可以拿time_utc，UTC 格式
            JSONObject timeRaw = jsonObj.getJSONObject("time_raw");
            if (timeRaw != null)
            {
                String date = timeRaw.getString("date") == null ? "" : timeRaw.getString("date");
                String time = timeRaw.getString("time") == null ? "" : timeRaw.getString("time");
                newObj.put("time", date + " " + time);
            }

            // 2. description重命名为context
            newObj.put("context", jsonObj.getString("description"));

            // 3. location（如"长沙市, 湖南省"，这里已经是反转过后的，不需要重新反转）
            String location = jsonObj.getString("location");
            if (location != null)
            {
                List<String> locParts = Arrays.asList(location.split(SplitUtils.DH));
                //Collections.reverse(locParts);
                newObj.put("location", String.join(", ", locParts));
            }

            // 4. address拼接为areaName（state+city+street）
            JSONObject address = jsonObj.getJSONObject("address");
            if (address != null)
            {
                String state = address.getString("state") == null ? "" : address.getString("state");
                String city = address.getString("city") == null ? "" : address.getString("city");
                String street = address.getString("street") == null ? "" : address.getString("street");
                newObj.put("areaName", street + city + state); // 国外地址反过来，拼接街道，市，省
            }
            convertedList.add(newObj);
        }
        return convertedList;
    }
}
