package com.laiketui.comps.mcp.service;

import com.laiketui.core.cache.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.util.StringUtil;

/**
 * MCP认证服务
 * 支持account+password自动认证
 *
 * @author wangxian
 * @date 2026-03-18
 */
@Service
public class McpAuthService {

    private static final Logger logger = LoggerFactory.getLogger(McpAuthService.class);

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 认证方法
     *
     * @param account 账号
     * @param password 密码
     * @return 认证是否成功
     */
    public boolean authenticate(String account, String password) {
        if (StringUtil.isEmpty(account) || StringUtil.isEmpty(password)) {
            logger.warn("MCP认证失败：账号或密码为空");
            return false;
        }

        try {
            // 这里可以根据实际需求实现认证逻辑
            // 1. 从数据库查询管理员账号密码
            // 2. 或者从Nacos配置读取认证信息
            // 3. 或者使用Redis缓存认证信息

            // 示例：从Redis获取认证配置
            String cacheKey = "mcp:auth:" + account;
            String cachedPassword = (String) redisUtil.get(cacheKey);

            if (cachedPassword != null && cachedPassword.equals(password)) {
                logger.info("MCP认证成功：{}", account);
                return true;
            }

            // 如果Redis中没有，可以查询数据库验证
            // TODO: 实现数据库查询逻辑
            // boolean dbAuth = authenticateFromDatabase(account, password);

            logger.warn("MCP认证失败：账号密码不匹配 - {}", account);
            return false;

        } catch (Exception e) {
            logger.error("MCP认证异常", e);
            return false;
        }
    }

    /**
     * 设置认证信息到缓存
     *
     * @param account 账号
     * @param password 密码
     */
    public void setAuthCache(String account, String password) {
        String cacheKey = "mcp:auth:" + account;
        redisUtil.set(cacheKey, password, 3600); // 缓存1小时
    }

    /**
     * 删除认证缓存
     *
     * @param account 账号
     */
    public void removeAuthCache(String account) {
        String cacheKey = "mcp:auth:" + account;
        redisUtil.del(cacheKey);
    }
}
