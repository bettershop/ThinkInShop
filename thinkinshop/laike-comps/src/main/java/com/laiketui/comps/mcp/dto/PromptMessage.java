package com.laiketui.comps.mcp.dto;

import java.util.List;
import java.util.Map;

/**
 * Prompt消息DTO
 * 用于MCP prompts/get方法
 *
 * @author wangxian
 * @date 2026-03-18
 */
public class PromptMessage {

    /**
     * 提示名称
     */
    private String name;

    /**
     * 提示描述
     */
    private String description;

    /**
     * 参数
     */
    private List<Argument> arguments;

    /**
     * 消息内容
     */
    private List<Message> messages;

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

    public List<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * 参数信息
     */
    public static class Argument {
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
     * 消息信息
     */
    public static class Message {
        private String role;
        private Content content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }
    }

    /**
     * 内容信息
     */
    public static class Content {
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
}
