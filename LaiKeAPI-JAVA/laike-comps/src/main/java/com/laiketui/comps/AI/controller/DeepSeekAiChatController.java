package com.laiketui.comps.AI.controller;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.laiketui.core.domain.Result;
import com.laiketui.root.annotation.HttpApiMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class DeepSeekAiChatController
{
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String API_KEY;
    static {
        String key = System.getenv("DEEPSEEK_API_KEY");
        API_KEY = key != null && !key.isEmpty() ? key : "请配置DEEPSEEK_API_KEY环境变量";
    }

    public DeepSeekAiChatController()
    {

    }

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/chat")
    @HttpApiMethod(apiKey = "laike.chat.tap")
    public Result chat(@RequestBody String userMessage)
    {
        try
        {
            // 1. 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(API_KEY);
            // 2. 使用fastjson2安全构建JSON，避免XSS/JSON注入
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("model", "deepseek-chat");
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", userMessage);
            messages.add(message);
            requestBodyMap.put("messages", messages);
            String requestBody = JSON.toJSONString(requestBodyMap);
            // 3. 发送请求
            HttpEntity<String>     request  = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(DEEPSEEK_API_URL, request, String.class);
            return Result.success(response.getBody().toString());
        }
        catch (Exception e)
        {
            e.getMessage();
            return Result.error(e.getMessage());
        }

    }
}
