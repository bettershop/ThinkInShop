package com.laiketui.core.utils.okhttp;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求参数处理
 *
 * @author Trick
 * @date 2023/4/18 10:57
 */
public class ParameterUtils
{

    public static Map<String, Object> getHttpInputStreamParams(InputStream inputStream)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)))
            {
                String        str      = "";
                StringBuilder wholeStr = new StringBuilder();
                //一行一行的读取body体里面的内容；
                while ((str = reader.readLine()) != null)
                {
                    wholeStr.append(str);
                }
                str = wholeStr.toString();
                if (StringUtils.isEmpty(str))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
                }
                resultMap = JSONObject.parseObject(str, new TypeReference<Map<String, Object>>()
                {
                });
            }
            catch (IOException e)
            {
                e.printStackTrace();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
        }
        return resultMap;
    }

}
