package com.laiketui.core.domain;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.lktconst.gwconst.LaiKeGWConst;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: 来客接口返回结果类（Fastjson2 兼容版）
 * @author: wx
 * @version: 2.1
 * @date: 2025/11/28
 */
public final class Result
{

    private static final Logger logger = LoggerFactory.getLogger(Result.class);

    private String code;
    private String message;
    private Object data;

    public Result(String code, String message, Object data)
    {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 成功构造器 ==========
    public static Result success()
    {
        return new Result(LaiKeGWConst.GW_SUCCESS, "操作成功", GloabConst.ManaValue.MANA_VALUE_SUCCESS);
    }

    public static Result success(Object data)
    {
        return new Result(LaiKeGWConst.GW_SUCCESS, "操作成功", data);
    }

    public static Result success(String code, String msg, Object data)
    {
        return new Result(code, msg, data);
    }

    public static Result success(String code, Object data)
    {
        return new Result(code, "操作成功", data);
    }

    public static Result success(String code, String message)
    {
        return new Result(code, message, null);
    }

    // ========== 错误构造器 ==========
    public static Result error()
    {
        return new Result(LaiKeGWConst.GW_ERROR, "操作失败", null);
    }

    public static Result error(Object data)
    {
        return new Result(LaiKeGWConst.GW_ERROR, "操作失败", data);
    }

    public static Result error(String code, String message, Object data)
    {
        return new Result(code, message, data);
    }

    public static Result error(String code, Object data)
    {
        return new Result(code, "操作失败", data);
    }

    public static Result error(String code, String message)
    {
        return new Result(code, message, null);
    }

    /**
     * 导出文件时返回 null（由网关直接写 response）
     */
    public static Result exportFile()
    {
        return null;
    }

    // ========== 智能解析响应 ==========
    public static Result exchange(Object data)
    {
        if (data == null)
        {
            return Result.error(LaiKeGWConst.GW_ERROR, "响应为空");
        }

        String text = data.toString().trim();
        if (text.isEmpty())
        {
            return Result.error(LaiKeGWConst.GW_ERROR, "响应内容为空");
        }

        // 1. 尝试解析为标准 JSON 对象（Result 格式）
        if ((text.startsWith("{") && text.endsWith("}")))
        {
            try
            {
                // 安全解析：不启用 AutoType，限制深度
                JSONObject json = JSON.parseObject(text);

                // 如果包含 code 字段，视为标准 Result
                if (json.containsKey("code"))
                {
                    String code      = json.getString("code");
                    String msg       = json.getString("message");
                    Object innerData = json.get("data");
                    return new Result(
                            StringUtils.defaultIfBlank(code, LaiKeGWConst.GW_ERROR),
                            StringUtils.defaultIfBlank(msg, "未知错误"),
                            innerData
                    );
                }

                // 兼容 {"error": "..."} 格式
                if (json.containsKey("error"))
                {
                    return Result.error(LaiKeGWConst.GW_ERROR, json.getString("error"));
                }

                // 否则视为纯数据对象
                return Result.success(LaiKeGWConst.GW_SUCCESS, "OK", json);
            }
            catch (JSONException e)
            {
                logger.warn("JSON 解析异常，原始内容前200字符: {}", abbreviate(text, 200), e);
                return Result.error(LaiKeGWConst.GW_ERROR, "返回结果JSON格式异常");
            }
        }

        // 2. 数组响应（如 [{"id":1}]）
        if (text.startsWith("[") && text.endsWith("]"))
        {
            try
            {
                return Result.success(LaiKeGWConst.GW_SUCCESS, "OK", JSON.parseArray(text));
            }
            catch (JSONException e)
            {
                logger.warn("JSON数组解析异常: {}", abbreviate(text, 200), e);
                return Result.error(LaiKeGWConst.GW_ERROR, "返回结果JSON格式异常");
            }
        }

        // 3. HTML 页面（后端崩溃）
        if (text.startsWith("<!DOCTYPE html") || text.startsWith("<html"))
        {
            logger.warn("后端返回HTML页面，可能服务异常。前200字符: {}", abbreviate(text, 200));
            return Result.error(LaiKeGWConst.GW_ERROR, "接口返回HTML页面，可能后端异常");
        }

        // 4. 纯文本错误
        if (text.toLowerCase().startsWith("error") || text.contains("Exception"))
        {
            return Result.error(LaiKeGWConst.GW_ERROR, text);
        }

        // 5. 默认视为成功文本
        return Result.success(LaiKeGWConst.GW_SUCCESS, "OK", text);
    }

    /**
     * 安全截断字符串
     */
    private static String abbreviate(String str, int maxLen)
    {
        if (str == null) return "";
        return str.length() <= maxLen ? str : str.substring(0, maxLen) + "...";
    }

    // ========== 工具方法 ==========
    public boolean isOk()
    {
        return LaiKeGWConst.GW_SUCCESS.equals(this.code);
    }

    public boolean isError()
    {
        return !isOk();
    }

    // ========== Getter / Setter ==========
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }

    /**
     * 泛型辅助方法
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T getData(Class<T> clazz)
    {
        if (data == null) return null;
        if (clazz.isInstance(data))
        {
            return clazz.cast(data);
        }
        if (data instanceof JSONObject)
        {
            return ((JSONObject) data).toJavaObject(clazz);
        }
        if (data instanceof String)
        {
            try
            {
                return JSON.parseObject((String) data, clazz);
            }
            catch (Exception e)
            {
                logger.warn("getData 泛型转换失败", e);
                return null;
            }
        }
        return null;
    }

    public <T> T getData(TypeReference<T> typeRef)
    {
        if (data == null) return null;
        if (data instanceof String)
        {
            return JSON.parseObject((String) data, typeRef);
        }
        // 注意：JSONObject 不支持 TypeReference 直接转，需先转 JSON 字符串
        String jsonStr = JSON.toJSONString(data);
        return JSON.parseObject(jsonStr, typeRef);
    }
}