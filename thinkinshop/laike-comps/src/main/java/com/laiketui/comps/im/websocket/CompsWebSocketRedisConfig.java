package com.laiketui.comps.im.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;

@Configuration
public class CompsWebSocketRedisConfig
{
    @Bean
    public RedisMessageListenerContainer webSocketRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            CompsWebSocketClusterDispatcher webSocketClusterDispatcher)
    {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener((message, pattern) -> {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            webSocketClusterDispatcher.consumeClusterMessage(payload);
        }, new ChannelTopic(CompsWebSocketClusterDispatcher.CLUSTER_CHANNEL));
        return container;
    }
}
