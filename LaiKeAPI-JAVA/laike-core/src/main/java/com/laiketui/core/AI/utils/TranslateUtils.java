package com.laiketui.core.AI.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.utils.okhttp.HttpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TranslateUtils
{

    static String API_URL = "https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s";

    public String getTranslate(String source, String fromLang, String toLang) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(source))
            {
                return source;
            }
            String result = HttpUtils.get(String.format(API_URL, source, fromLang, toLang));
            log.info("接口调用结果： {} ", result);
            JSONObject jsonObject     = JSON.parseObject(result);
            String     translatedText = jsonObject.getJSONObject("responseData").getString("translatedText");
            log.info("翻译结果： {} ", translatedText);
            return translatedText;
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            return source;
        }
    }

    public String getTranslate(String source) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(source))
            {
                return source;
            }
            String result = HttpUtils.get(String.format(API_URL, source, "zh-CN", "en-US"));
            log.info("接口调用结果： {} ", result);
            JSONObject jsonObject     = JSON.parseObject(result);
            String     translatedText = jsonObject.getJSONObject("responseData").getString("translatedText");
            log.info("翻译结果： {} ", translatedText);
            return translatedText;
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            return source;
        }
    }


    public static void main(String[] args)
    {
//        TranslateUtils.getTranslate("遮住我们眼睛的亮光，对我们无异于黑暗。唯有我们清醒的时候，天光才大亮。天光大亮的日子多着呢", "zh-CN", "en-US");
    }

}
