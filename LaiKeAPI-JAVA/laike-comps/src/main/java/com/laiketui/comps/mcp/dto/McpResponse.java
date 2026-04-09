package com.laiketui.comps.mcp.dto;

import java.util.Map;

/**
 * MCP响应DTO
 * 符合 JSON-RPC 2.0 和 MCP 2024-11-05 标准
 *
 * @author wangxian
 * @date 2026-03-18
 */
public class McpResponse {

    /**
     * JSON-RPC版本，必须为"2.0"
     */
    private String jsonrpc;

    /**
     * 请求ID，与请求中的ID对应
     */
    private Object id;

    /**
     * 响应结果
     */
    private Result result;

    /**
     * 错误信息（如果请求失败）
     */
    private ErrorInfo error;

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

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public ErrorInfo getError() {
        return error;
    }

    public void setError(ErrorInfo error) {
        this.error = error;
    }

    /**
     * 响应结果内部类
     */
    public static class Result {
        /**
         * 服务器信息
         */
        private ServerInfo serverInfo;

        /**
         * 服务器能力
         */
        private Map<String, Object> capabilities;

        /**
         * 工具列表
         */
        private java.util.List<ToolInfo> tools;

        /**
         * 资源列表
         */
        private java.util.List<ResourceInfo> resources;

        /**
         * 提示列表
         */
        private java.util.List<PromptInfo> prompts;

        /**
         * 内容
         */
        private java.util.List<ContentItem> content;

        public ServerInfo getServerInfo() {
            return serverInfo;
        }

        public void setServerInfo(ServerInfo serverInfo) {
            this.serverInfo = serverInfo;
        }

        public Map<String, Object> getCapabilities() {
            return capabilities;
        }

        public void setCapabilities(Map<String, Object> capabilities) {
            this.capabilities = capabilities;
        }

        public java.util.List<ToolInfo> getTools() {
            return tools;
        }

        public void setTools(java.util.List<ToolInfo> tools) {
            this.tools = tools;
        }

        public java.util.List<ResourceInfo> getResources() {
            return resources;
        }

        public void setResources(java.util.List<ResourceInfo> resources) {
            this.resources = resources;
        }

        public java.util.List<PromptInfo> getPrompts() {
            return prompts;
        }

        public void setPrompts(java.util.List<PromptInfo> prompts) {
            this.prompts = prompts;
        }

        public java.util.List<ContentItem> getContent() {
            return content;
        }

        public void setContent(java.util.List<ContentItem> content) {
            this.content = content;
        }
    }

    /**
     * 服务器信息
     */
    public static class ServerInfo {
        private String name;
        private String version;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    /**
     * 工具信息
     */
    public static class ToolInfo {
        private String name;
        private String description;
        private Map<String, Object> inputSchema;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, Object> getInputSchema() {
            return inputSchema;
        }

        public void setInputSchema(Map<String, Object> inputSchema) {
            this.inputSchema = inputSchema;
        }
    }

    /**
     * 资源信息
     */
    public static class ResourceInfo {
        private String uri;
        private String name;
        private String description;

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * 提示信息
     */
    public static class PromptInfo {
        private String name;
        private String description;
        private java.util.List<ArgumentInfo> arguments;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public java.util.List<ArgumentInfo> getArguments() {
            return arguments;
        }

        public void setArguments(java.util.List<ArgumentInfo> arguments) {
            this.arguments = arguments;
        }
    }

    /**
     * 参数信息
     */
    public static class ArgumentInfo {
        private String name;
        private String description;
        private boolean required;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }
    }

    /**
     * 内容项
     */
    public static class ContentItem {
        private String type;
        private String text;
        private Map<String, Object> data;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }

    /**
     * 错误信息
     */
    public static class ErrorInfo {
        private int code;
        private String message;
        private Object data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
