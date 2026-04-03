package com.laiketui.comps.mcp;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laiketui.comps.mcp.service.McpAuthService;
import com.laiketui.comps.mcp.service.McpToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * MCP Controller - Model Context Protocol
 * 接口地址: /outapi/mcp
 * 协议标准: MCP 2024-11-05 + JSON-RPC 2.0
 * 认证方式: account+password自动认证
 *
 * @author wangxian
 * @date 2026-03-18
 */
@RestController
@RequestMapping("/outapi")
public class McpController {

    private static final Logger logger = LoggerFactory.getLogger(McpController.class);

    private static final Map<String, String> TOOLS = new HashMap<String, String>() {{
        put("get_user", "获取用户信息");
        put("list_users", "查询用户列表");
        put("get_order", "获取订单信息");
        put("list_orders", "查询订单列表");
        put("get_product", "获取商品信息");
        put("list_products", "查询商品列表");
    }};

    private static final Map<String, String> RESOURCES = new HashMap<String, String>() {{
        put("user", "用户资源");
        put("order", "订单资源");
        put("product", "商品资源");
    }};

    @Autowired
    private McpAuthService mcpAuthService;

    @Autowired
    private McpToolService mcpToolService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * MCP主接口入口
     * 支持 JSON-RPC 2.0 协议
     */
    @PostMapping("/mcp")
    public String handleMcpRequest(@RequestBody String requestBody, HttpServletRequest request) {
        try {
            JsonNode jsonNode = objectMapper.readTree(requestBody);

            String jsonrpc = jsonNode.has("jsonrpc") ? jsonNode.get("jsonrpc").asText() : "2.0";
            String id = jsonNode.has("id") ? jsonNode.get("id").asText() : null;
            String method = jsonNode.has("method") ? jsonNode.get("method").asText() : null;
            JsonNode params = jsonNode.has("params") ? jsonNode.get("params") : null;

            logger.info("MCP请求 - method: {}, id: {}", method, id);

            // 处理不同的MCP方法
            Object result = handleMethod(method, params, request);

            return JSON.toJSONString(buildSuccessResponse(id, result));
        } catch (Exception e) {
            logger.error("MCP请求处理异常", e);
            return JSON.toJSONString(buildErrorResponse(null, -32603, "Internal error: " + e.getMessage()));
        }
    }

    /**
     * 处理MCP方法
     */
    private Object handleMethod(String method, JsonNode params, HttpServletRequest request) throws Exception {
        if (method == null) {
            throw new IllegalArgumentException("Method is not specified");
        }

        switch (method) {
            case "initialize":
                return handleInitialize(params, request);
            case "tools/list":
                return handleToolsList();
            case "tools/call":
                return handleToolsCall(params, request);
            case "resources/list":
                return handleResourcesList();
            case "prompts/list":
                return handlePromptsList();
            default:
                throw new IllegalArgumentException("Method not found: " + method);
        }
    }

    /**
     * 处理initialize方法 - MCP协议握手
     */
    private Map<String, Object> handleInitialize(JsonNode params, HttpServletRequest request) {
        // 认证检查
        if (params != null && params.has("auth")) {
            JsonNode auth = params.get("auth");
            if (auth.has("account") && auth.has("password")) {
                String account = auth.get("account").asText();
                String password = auth.get("password").asText();
                boolean authenticated = mcpAuthService.authenticate(account, password);
                if (!authenticated) {
                    throw new RuntimeException("Authentication failed");
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "LaikeShop MCP Server");
        serverInfo.put("version", "1.0.0");
        result.put("serverInfo", serverInfo);

        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("tools", new HashMap<>());
        capabilities.put("resources", new HashMap<>());
        capabilities.put("prompts", new HashMap<>());
        result.put("capabilities", capabilities);

        return result;
    }

    /**
     * 处理tools/list方法 - 返回可用工具列表
     */
    private Map<String, Object> handleToolsList() {
        List<Map<String, Object>> tools = new ArrayList<>();

        for (Map.Entry<String, String> entry : TOOLS.entrySet()) {
            Map<String, Object> tool = new HashMap<>();
            tool.put("name", entry.getKey());
            tool.put("description", entry.getValue());
            tools.add(tool);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tools", tools);
        return result;
    }

    /**
     * 处理tools/call方法 - 执行工具调用
     */
    private Map<String, Object> handleToolsCall(JsonNode params, HttpServletRequest request) throws Exception {
        if (params == null || !params.has("name")) {
            throw new IllegalArgumentException("Tool name is required");
        }

        String toolName = params.get("name").asText();
        Map<String, Object> arguments = new HashMap<>();
        if (params.has("arguments")) {
            JsonNode argsNode = params.get("arguments");
            Iterator<Map.Entry<String, JsonNode>> fields = argsNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getValue().isTextual()) {
                    arguments.put(field.getKey(), field.getValue().asText());
                } else if (field.getValue().isInt()) {
                    arguments.put(field.getKey(), field.getValue().asInt());
                } else if (field.getValue().isLong()) {
                    arguments.put(field.getKey(), field.getValue().asLong());
                } else if (field.getValue().isBoolean()) {
                    arguments.put(field.getKey(), field.getValue().asBoolean());
                } else {
                    arguments.put(field.getKey(), field.getValue().toString());
                }
            }
        }

        String content = mcpToolService.executeTool(toolName, arguments, request);

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("type", "text");
        contentMap.put("text", content);
        result.put("content", Collections.singletonList(contentMap));
        return result;
    }

    /**
     * 处理resources/list方法 - 返回可用资源列表
     */
    private Map<String, Object> handleResourcesList() {
        List<Map<String, Object>> resources = new ArrayList<>();

        for (Map.Entry<String, String> entry : RESOURCES.entrySet()) {
            Map<String, Object> resource = new HashMap<>();
            resource.put("uri", "mcp://laike/" + entry.getKey());
            resource.put("name", entry.getValue());
            resources.add(resource);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("resources", resources);
        return result;
    }

    /**
     * 处理prompts/list方法 - 返回可用提示列表
     */
    private Map<String, Object> handlePromptsList() {
        List<Map<String, Object>> prompts = new ArrayList<>();

        Map<String, Object> prompt1 = new HashMap<>();
        prompt1.put("name", "analyze_user_data");
        prompt1.put("description", "分析用户数据");
        prompt1.put("arguments", Collections.emptyList());
        prompts.add(prompt1);

        Map<String, Object> prompt2 = new HashMap<>();
        prompt2.put("name", "analyze_order_data");
        prompt2.put("description", "分析订单数据");
        prompt2.put("arguments", Collections.emptyList());
        prompts.add(prompt2);

        Map<String, Object> prompt3 = new HashMap<>();
        prompt3.put("name", "analyze_product_data");
        prompt3.put("description", "分析商品数据");
        prompt3.put("arguments", Collections.emptyList());
        prompts.add(prompt3);

        Map<String, Object> result = new HashMap<>();
        result.put("prompts", prompts);
        return result;
    }

    /**
     * 构建成功响应
     */
    private Map<String, Object> buildSuccessResponse(String id, Object result) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        if (id != null) {
            response.put("id", id);
        }
        response.put("result", result);
        return response;
    }

    /**
     * 构建错误响应
     */
    private Map<String, Object> buildErrorResponse(String id, int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        if (id != null) {
            response.put("id", id);
        }
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        response.put("error", error);
        return response;
    }
}
