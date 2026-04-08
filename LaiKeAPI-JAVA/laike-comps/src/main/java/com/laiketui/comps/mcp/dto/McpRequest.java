package com.laiketui.comps.mcp.dto;

import java.util.Map;

/**
 * MCP请求DTO
 * 符合 JSON-RPC 2.0 和 MCP 2024-11-05 标准
 *
 * @author wangxian
 * @date 2026-03-18
 */
public class McpRequest {

    /**
     * JSON-RPC版本，必须为"2.0"
     */
    private String jsonrpc;

    /**
     * 请求ID，用于匹配响应
     */
    private Object id;

    /**
     * 方法名称
     * 支持的方法：initialize, tools/list, tools/call, resources/list, prompts/list
     */
    private String method;

    /**
     * 参数对象
     */
    private Map<String, Object> params;

    /**
     * 认证信息（仅在initialize时使用）
     */
    private AuthInfo auth;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public AuthInfo getAuth() {
        return auth;
    }

    public void setAuth(AuthInfo auth) {
        this.auth = auth;
    }

    /**
     * 认证信息内部类
     */
    public static class AuthInfo {
        /**
         * 账号
         */
        private String account;

        /**
         * 密码
         */
        private String password;

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
