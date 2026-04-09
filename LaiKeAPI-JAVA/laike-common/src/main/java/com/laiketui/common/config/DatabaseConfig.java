package com.laiketui.common.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseConfig
{
    @Value("${spring.shardingsphere.datasource.lkt-ds-master-0.url}")
    private String url;

    @Value("${spring.shardingsphere.datasource.lkt-ds-master-0.username}")
    private String username;

    @Value("${spring.shardingsphere.datasource.lkt-ds-master-0.password}")
    private String password;
}
