package com.laiketui.core.config;

import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * 通过请求参数 language=zh_CN 自动解析语言
 */
public class CustomLocaleResolver implements LocaleResolver
{

    private static final Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;

    @Override
    public Locale resolveLocale(HttpServletRequest request)
    {

        String lang = request.getHeader("language");

        if (lang == null || lang.isEmpty())
        {
            lang = request.getParameter("language");
        }

        if (lang == null || lang.isEmpty())
        {
            return DEFAULT_LOCALE;
        }

        try
        {
            return Locale.forLanguageTag(lang.replace("_", "-"));
        }
        catch (Exception e)
        {
            return DEFAULT_LOCALE;
        }
    }

    @Override
    public void setLocale(
            HttpServletRequest request,
            HttpServletResponse response,
            Locale locale)
    {
        // 通常不用
    }
}


