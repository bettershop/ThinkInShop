package com.laiketui.core.config;


import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.MultipartConfigElement;

/**
 * @author Administrator
 */
@Configuration
public class CommonConfig
{

    private final FileUploadAndDownProperties fileUploadAndDownProperties;

    // 通过构造函数注入（推荐）
    public CommonConfig(FileUploadAndDownProperties fileUploadAndDownProperties)
    {
        this.fileUploadAndDownProperties = fileUploadAndDownProperties;
    }

    @Bean
    public LocaleResolver localeResolver()
    {
        return new CustomLocaleResolver();
    }

    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer swaggerProperties()
    {
        PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
        p.setIgnoreUnresolvablePlaceholders(true);
        return p;
    }

    @Bean
    public ServletWebServerFactory webServerFactory()
    {
        TomcatServletWebServerFactory fa = new TomcatServletWebServerFactory();
        fa.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> connector.setProperty("relaxedQueryChars", "[]{}"));
        return fa;
    }

    @Bean
    public MultipartConfigElement multipartConfigElement()
    {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(fileUploadAndDownProperties.getUpload().getMaxFileSize());
        factory.setMaxRequestSize(fileUploadAndDownProperties.getUpload().getMaxRequestSize());
        return factory.createMultipartConfig();
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory factory)
    {
        return new StringRedisTemplate(factory);
    }



}
