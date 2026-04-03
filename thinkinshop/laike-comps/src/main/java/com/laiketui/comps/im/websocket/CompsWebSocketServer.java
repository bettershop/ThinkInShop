package com.laiketui.comps.im.websocket;

import com.laiketui.comps.im.common.consts.CompsOnlineMessageConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * websocket 控制层
 * 优化：
 * 1. 统一key格式，避免不一致
 * 2. 添加心跳机制
 * 3. 改进错误处理和资源清理
 * 4. 添加连接超时检测
 * 5. 使用异步发送的回调处理
 */
@Component
@ServerEndpoint("/onlineMessage/{sendId}/{receiveId}/{storeType}")
public class CompsWebSocketServer
{
    private static final Logger LOG = LoggerFactory.getLogger(CompsWebSocketServer.class);

    // 使用线程安全的ConcurrentHashMap管理连接
    public static final ConcurrentHashMap<String, CompsWebSocketServer> webSocketMap =
            new ConcurrentHashMap<>();

    // 心跳定时器
    private static final ScheduledExecutorService HEARTBEAT_EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "websocket-heartbeat");
        thread.setDaemon(true);
        return thread;
    });

    // 心跳检测间隔（秒）
    private static final int HEARTBEAT_INTERVAL = 30;

    static {
        // 启动心跳检测线程
        HEARTBEAT_EXECUTOR.scheduleAtFixedRate(CompsWebSocketServer::checkHeartbeat,
                HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    private Session session;
    private String sendId;
    private String receiveId;
    private String storeType;
    private String connectionKey; // 保存连接key，避免重复计算
    private volatile long lastActiveTime; // 最后活跃时间

    /**
     * 心跳检测，清理过期连接
     */
    private static void checkHeartbeat()
    {
        long currentTime = System.currentTimeMillis();
        long timeout = HEARTBEAT_INTERVAL * 2 * 1000L; // 2倍心跳间隔作为超时

        webSocketMap.entrySet().removeIf(entry -> {
            CompsWebSocketServer server = entry.getValue();
            if (currentTime - server.lastActiveTime > timeout)
            {
                if (server.session != null && server.session.isOpen())
                {
                    try
                    {
                        server.session.close();
                    }
                    catch (IOException e)
                    {
                        // ignore
                    }
                }
                return true;
            }
            return false;
        });
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("sendId") String sendId,
                       @PathParam("receiveId") String receiveId, @PathParam("storeType") String storeType)
    {
        this.session = session;
        this.sendId = sendId;
        this.receiveId = receiveId;
        this.storeType = getStoreType(storeType);
        if (this.storeType.isEmpty())
        {
            LOG.warn("WebSocket连接来源无效, sendId:{}, receiveId:{}, storeType:{}", sendId, receiveId, storeType);
            try
            {
                session.close();
            }
            catch (IOException e)
            {
                LOG.warn("关闭无效来源连接失败: {}", e.getMessage());
            }
            return;
        }
        this.lastActiveTime = System.currentTimeMillis();

        // 统一使用逗号作为分隔符
        this.connectionKey = this.storeType + sendId + "," + receiveId;

        LOG.info("WebSocket连接建立, sendId:{}, receiveId:{}, storeType:{}, key:{}",
                sendId, receiveId, storeType, connectionKey);

        CompsWebSocketServer existingServer = webSocketMap.put(connectionKey, this);
        if (existingServer != null && existingServer != this)
        {
            try
            {
                if (existingServer.session != null && existingServer.session.isOpen())
                {
                    existingServer.session.close();
                }
            }
            catch (IOException e)
            {
                LOG.warn("关闭旧连接失败: {}", e.getMessage());
            }
        }
        LOG.info("当前连接数:{}", webSocketMap.size());
    }

    @OnClose
    public void onClose(Session session)
    {
        String key = connectionKey != null ? connectionKey :
                (storeType != null ? storeType + sendId + "," + receiveId : "unknown");

        webSocketMap.remove(key, this);
        LOG.info("WebSocket连接关闭, key:{}, 剩余连接数:{}", key, webSocketMap.size());

        // 清理资源
        this.session = null;
    }

    @OnMessage
    public void onMessage(String message, Session session)
    {
        // 更新活跃时间
        lastActiveTime = System.currentTimeMillis();

        LOG.debug("WebSocket收到消息, key:{}, message:{}", connectionKey, message);

        // 处理心跳消息
        if ("ping".equals(message) || "heartbeat".equals(message))
        {
            try
            {
                session.getAsyncRemote().sendText("pong");
            }
            catch (Exception e)
            {
                LOG.warn("发送心跳响应失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 配置错误信息处理
     *
     * @param session websocket session
     * @param t 异常
     */
    @OnError
    public void onError(Session session, Throwable t)
    {
        String key = connectionKey != null ? connectionKey : "unknown";
        LOG.error("WebSocket错误, key:{}, 错误信息:{}", key, t.getMessage(), t);

        // 清理异常连接
        webSocketMap.remove(key, this);

        // 关闭会话
        if (session != null && session.isOpen())
        {
            try
            {
                session.close();
            }
            catch (IOException e)
            {
                LOG.warn("关闭异常会话失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 发送消息（兼容老调用方）
     *
     * @param key 连接key
     * @param text 消息内容
     */
    public void sendMessage(String key, String text)
    {
        if (!sendLocalMessage(key, text))
        {
            LOG.warn("WebSocket通道不存在或连接已关闭, key:{}", key);
        }
    }

    /**
     * 仅向当前节点本地连接发送消息。
     *
     * @param key 连接key
     * @param text 消息内容
     * @return true 表示当前节点命中并发起发送
     */
    public static boolean sendLocalMessage(String key, String text)
    {
        CompsWebSocketServer webSocketServer = webSocketMap.get(key);
        if (webSocketServer == null)
        {
            LOG.debug("WebSocket本地通道不存在, key:{}", key);
            return false;
        }

        Session currentSession = webSocketServer.session;
        if (currentSession == null || !currentSession.isOpen())
        {
            LOG.debug("WebSocket本地连接已关闭, key:{}", key);
            webSocketMap.remove(key, webSocketServer);
            return false;
        }

        // 更新活跃时间
        webSocketServer.lastActiveTime = System.currentTimeMillis();

        try
        {
            RemoteEndpoint.Async asyncRemote = currentSession.getAsyncRemote();
            asyncRemote.sendText(text, new SendHandler()
            {
                @Override
                public void onResult(SendResult result)
                {
                    if (!result.isOK())
                    {
                        Throwable ex = result.getException();
                        LOG.error("WebSocket消息发送失败, key:{}, 错误:{}", key,
                                ex != null ? ex.getMessage() : "unknown");
                        // 发送失败，移除连接
                        webSocketMap.remove(key, webSocketServer);
                    }
                }
            });
            return true;
        }
        catch (Exception e)
        {
            LOG.error("WebSocket消息发送异常, key:{}, error:{}", key, e.getMessage(), e);
            webSocketMap.remove(key, webSocketServer);
            return false;
        }
    }

    /**
     * 按 receiveId 向当前节点所有匹配连接推送。
     *
     * @param receiveId 接收方ID
     * @param text 消息
     * @return 成功发送的连接数
     */
    public static int sendLocalMessageByReceiver(String receiveId, String text)
    {
        if (!StringUtils.hasText(receiveId))
        {
            return 0;
        }
        AtomicInteger successCount = new AtomicInteger(0);
        webSocketMap.forEach((key, server) -> {
            String currentReceiver = extractReceiveId(key);
            if (receiveId.equals(currentReceiver) && sendLocalMessage(key, text))
            {
                successCount.incrementAndGet();
            }
        });
        return successCount.get();
    }

    private static String extractReceiveId(String key)
    {
        if (!StringUtils.hasText(key))
        {
            return "";
        }
        int index = key.indexOf(',');
        if (index < 0 || index >= key.length() - 1)
        {
            return "";
        }
        return key.substring(index + 1);
    }

    /**
     * 广播消息给所有连接
     *
     * @param text 消息内容
     */
    public static void broadcast(String text)
    {
        webSocketMap.forEach((key, server) -> sendLocalMessage(key, text));
    }

    /**
     * 获取当前连接数
     */
    public static int getConnectCount()
    {
        return webSocketMap.size();
    }

    /**
     * 消息来源
     */
    private String getStoreType(String storeType)
    {
        switch (storeType)
        {
            case CompsOnlineMessageConst.StoreSource.LKT_LY_001:
                return CompsOnlineMessageConst.StoreType.APPLET;
            case CompsOnlineMessageConst.StoreSource.LKT_LY_002:
                return CompsOnlineMessageConst.StoreType.APP;
            case CompsOnlineMessageConst.StoreSource.LKT_LY_006:
                return CompsOnlineMessageConst.StoreType.PcStore;
            case CompsOnlineMessageConst.StoreSource.LKT_LY_007:
                return CompsOnlineMessageConst.StoreType.H5;
            case CompsOnlineMessageConst.StoreSource.LKT_LY_008:
                return CompsOnlineMessageConst.StoreType.PcMch;
            default:
                return "";
        }
    }
}
