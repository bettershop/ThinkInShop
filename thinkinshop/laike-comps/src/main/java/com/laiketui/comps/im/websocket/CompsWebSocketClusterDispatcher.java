package com.laiketui.comps.im.websocket;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * WebSocket集群分发器：
 * 1. 先尝试当前节点本地投递。
 * 2. 本地未命中时发布到Redis频道，由其他节点消费并投递。
 */
@Component
public class CompsWebSocketClusterDispatcher
{
    public static final String CLUSTER_CHANNEL = "laike:im:websocket:dispatch";

    private static final Logger LOG = LoggerFactory.getLogger(CompsWebSocketClusterDispatcher.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final String nodeId = UUID.randomUUID().toString().replace("-", "");

    public CompsWebSocketClusterDispatcher(StringRedisTemplate stringRedisTemplate)
    {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void dispatchToKey(String key, String message)
    {
        if (CompsWebSocketServer.sendLocalMessage(key, message))
        {
            return;
        }
        publish(DispatchMode.KEY, key, message);
    }

    public void dispatchToReceiverId(String receiveId, String message)
    {
        int success = CompsWebSocketServer.sendLocalMessageByReceiver(receiveId, message);
        if (success > 0)
        {
            return;
        }
        publish(DispatchMode.RECEIVER_ID, receiveId, message);
    }

    public void consumeClusterMessage(String payload)
    {
        if (!StringUtils.hasText(payload))
        {
            return;
        }
        try
        {
            DispatchEvent event = JSON.parseObject(payload, DispatchEvent.class);
            if (event == null || !StringUtils.hasText(event.getMode()) || !StringUtils.hasText(event.getTarget()))
            {
                return;
            }
            if (nodeId.equals(event.getSourceNodeId()))
            {
                return;
            }

            if (DispatchMode.KEY.name().equalsIgnoreCase(event.getMode()))
            {
                CompsWebSocketServer.sendLocalMessage(event.getTarget(), event.getMessage());
                return;
            }
            if (DispatchMode.RECEIVER_ID.name().equalsIgnoreCase(event.getMode()))
            {
                CompsWebSocketServer.sendLocalMessageByReceiver(event.getTarget(), event.getMessage());
            }
        }
        catch (Exception e)
        {
            LOG.warn("消费WebSocket集群消息失败, payload:{}, error:{}", payload, e.getMessage());
        }
    }

    private void publish(DispatchMode mode, String target, String message)
    {
        if (!StringUtils.hasText(target))
        {
            return;
        }
        DispatchEvent event = new DispatchEvent();
        event.setMode(mode.name());
        event.setTarget(target);
        event.setMessage(message);
        event.setSourceNodeId(nodeId);
        stringRedisTemplate.convertAndSend(CLUSTER_CHANNEL, JSON.toJSONString(event));
    }

    private enum DispatchMode
    {
        KEY,
        RECEIVER_ID
    }

    public static class DispatchEvent
    {
        private String mode;
        private String target;
        private String message;
        private String sourceNodeId;

        public String getMode()
        {
            return mode;
        }

        public void setMode(String mode)
        {
            this.mode = mode;
        }

        public String getTarget()
        {
            return target;
        }

        public void setTarget(String target)
        {
            this.target = target;
        }

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }

        public String getSourceNodeId()
        {
            return sourceNodeId;
        }

        public void setSourceNodeId(String sourceNodeId)
        {
            this.sourceNodeId = sourceNodeId;
        }
    }
}
