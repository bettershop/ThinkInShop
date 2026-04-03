package com.laiketui.common.config;

import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class FreeMarkerGlobalConfig
{

    @Autowired
    private freemarker.template.Configuration configuration;

    @PostConstruct
    public void registerXxlJobUtils() throws TemplateModelException
    {
        // 使用 XXL-JOB 自带的 FtlUtil 包装静态类
        Object i18nUtilModel = com.xxl.job.admin.core.util.FtlUtil
                .generateStaticModel("com.xxl.job.admin.core.util.I18nUtil");

        configuration.setSharedVariable("I18nUtil", i18nUtilModel);
    }
}
