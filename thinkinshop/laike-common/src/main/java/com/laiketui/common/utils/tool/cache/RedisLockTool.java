package com.laiketui.common.utils.tool.cache;


import com.laiketui.core.cache.RedisUtil;
import com.laiketui.root.common.BuilderIDTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * redis 分布式锁
 *
 * @author Trick
 * @date 2022/7/14 18:39
 */
@Component
public class RedisLockTool
{
    @Autowired
    public               RedisUtil redisUtil;
    /**
     * 解锁原子性操作脚本
     */
    private static final String    UNLOCK_SCRIPT = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n"
            + "then\n"
            + "    return redis.call(\"del\",KEYS[1])\n"
            + "else\n"
            + "    return 0\n"
            + "end";

    /**
     * 加锁，有阻塞
     *
     * @param name    - 标识
     * @param expire  - 过期时间(毫秒)
     * @param timeout - 超时时间(毫秒)
     * @return String
     * @author Trick
     * @date 2022/7/14 18:37
     */
    public String lock(String name, long expire, long timeout)
    {
        long   startTime = System.currentTimeMillis();
        String token;
        do
        {
            token = tryLock(name, expire);
            if (token == null)
            {
                //设置等待时间，若等待时间过长则获取锁失败
                if ((System.currentTimeMillis() - startTime) > (timeout - 50))
                {
                    break;
                }
                try
                {
                    //try it again per 50
                    Thread.sleep(50);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

        }
        while (token == null);
        return token;
    }

    /**
     * 解锁
     *
     * @param name  - 标识
     * @param token -
     * @return String
     * @author Trick
     * @date 2022/7/14 18:35
     */
    public Boolean unlock(String name, String token)
    {
        byte[][] keyArgs = new byte[2][];
        keyArgs[0] = name.getBytes(StandardCharsets.UTF_8);
        keyArgs[1] = token.getBytes(StandardCharsets.UTF_8);
        RedisConnectionFactory connectionFactory = redisUtil.getConnectionFactory();
        RedisConnection        connection        = connectionFactory.getConnection();
        try
        {
            Long result = connection.scriptingCommands().eval(UNLOCK_SCRIPT.getBytes(StandardCharsets.UTF_8), ReturnType.INTEGER, 1, keyArgs);
            if (result != null && result > 0)
            {
                return true;
            }

        }
        finally
        {
            RedisConnectionUtils.releaseConnection(connection, connectionFactory, false);
        }
        return false;
    }

    /**
     * 加锁，无阻塞
     *
     * @param name   - 标识
     * @param expire - 过期时间 (毫秒)
     * @return String
     * @author Trick
     * @date 2022/7/14 18:35
     */
    public String tryLock(String name, long expire)
    {
        String                 token             = BuilderIDTool.getSnowflakeId() + "";
        RedisConnectionFactory connectionFactory = redisUtil.getConnectionFactory();
        RedisConnection        connection        = connectionFactory.getConnection();
        try
        {
            Boolean result = connection.set(name.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8),
                    Expiration.from(expire, TimeUnit.MINUTES), RedisStringCommands.SetOption.SET_IF_ABSENT);
            if (result != null && result)
            {
                return token;
            }
        }
        finally
        {
            RedisConnectionUtils.releaseConnection(connection, connectionFactory, false);
        }
        return null;
    }
}
