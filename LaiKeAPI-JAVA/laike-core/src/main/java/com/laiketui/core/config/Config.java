package com.laiketui.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 配置注入
 *
 * @author Trick
 * @date 2021/11/30 16:05
 */
@Component
@RefreshScope
public class Config
{

    @Value("${uploadFile.path}")
    private String uploadPath;

    @Value("${uploadAuthFile.path}")
    private String authPath;

    public String getAuthPath() {
        return authPath;
    }

    public void setAuthPath(String authPath) {
        this.authPath = authPath;
    }

    public String getUploadPath()
    {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath)
    {
        this.uploadPath = uploadPath;
    }
}
