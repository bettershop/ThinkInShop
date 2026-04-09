package com.laiketui.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class MessageSourceConfig
{

    @Value("${spring.messages.basenames:i18n/tips,i18n/messages}")
    private String basenames;

    @Value("${spring.messages.encoding:UTF-8}")
    private String encoding;

    @Value("${spring.messages.fallbackToSystemLocale:false}")
    private boolean fallbackToSystemLocale;

    @Bean
    public MessageSource messageSource()
    {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        // 动态拆分 basename（支持 nacos 配置多个以逗号分隔）
        String[] baseArray = basenames.split("\\s*,\\s*");
        for (int i = 0; i < baseArray.length; i++)
        {
            String base = baseArray[i];
            if (!base.startsWith("classpath:") && !base.startsWith("file:"))
            {
                baseArray[i] = "classpath:" + (base.startsWith("/") ? base.substring(1) : base);
            }
        }

        messageSource.setBasenames(baseArray);
        messageSource.setDefaultEncoding(encoding);
        messageSource.setFallbackToSystemLocale(fallbackToSystemLocale);
        return messageSource;
    }
}
