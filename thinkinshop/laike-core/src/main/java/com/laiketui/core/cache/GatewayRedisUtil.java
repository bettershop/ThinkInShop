package com.laiketui.core.cache;

import com.alibaba.fastjson2.JSON;
import com.laiketui.core.domain.LaiKeApi;
import com.laiketui.core.domain.LaiKeApiParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GatewayRedisUtil
{

    // ================= Hash 注册 Lua =================
    private static final DefaultRedisScript<Long> HASH_REGISTER_LUA;

    // ================= Hash 心跳 Lua =================
    private static final DefaultRedisScript<Long> HASH_HEARTBEAT_LUA;

    // ================= Hash 实例删除 Lua =================
    private static final DefaultRedisScript<Long> HASH_REMOVE_LUA;

    private static final DefaultRedisScript<Long> ROUND_ROBIN_LUA;

    // ================= 限流 Lua =================
    private static final DefaultRedisScript<Long> RATE_LIMIT_LUA =
            new DefaultRedisScript<>(
                    "local c = redis.call('INCR', KEYS[1]); " +
                            "if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[2]); end " +
                            "if c > tonumber(ARGV[1]) then return 0 else return 1 end;",
                    Long.class
            );


    private static final String GW_WHITE_IP_SET = "gw:whitelist:ip";
    private static final String GW_BLACK_IP_SET = "gw:blacklist:ip";

    // ================= 幂等 Lua =================
    private static final DefaultRedisScript<Long> IDEMPOTENT_LUA =
            new DefaultRedisScript<>(
                    "if redis.call('SETNX', KEYS[1], 1) == 1 then " +
                            " redis.call('EXPIRE', KEYS[1], ARGV[1]); return 1 " +
                            "else return 0 end",
                    Long.class
            );

    // ================= 熔断统计 Lua 脚本 =================
    private static final DefaultRedisScript<Long> CIRCUIT_STAT_LUA;

    // ================= 熔断判断 Lua 脚本 =================
    private static final DefaultRedisScript<Long> CIRCUIT_CHECK_LUA;

    static
    {
        ROUND_ROBIN_LUA = new DefaultRedisScript<>();
        ROUND_ROBIN_LUA.setScriptText(
                "local v = redis.call('INCR', KEYS[1]); " +
                        "if tonumber(v) == 1 then " +
                        "  redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1])); " +
                        "end " +
                        "return v;"
        );
        ROUND_ROBIN_LUA.setResultType(Long.class);
    }

    static
    {
        // 原子注册：HSET + EXPIRE，不解析 JSON
        HASH_REGISTER_LUA = new DefaultRedisScript<>();
        HASH_REGISTER_LUA.setScriptText(
                "redis.call('HSET', KEYS[1], ARGV[1], ARGV[2]); " +
                        "redis.call('EXPIRE', KEYS[1], tonumber(ARGV[3])); " +
                        "return 1;"
        );
        HASH_REGISTER_LUA.setResultType(Long.class);

        // 心跳：只更新 TTL，不解析 JSON
        HASH_HEARTBEAT_LUA = new DefaultRedisScript<>();
        HASH_HEARTBEAT_LUA.setScriptText(
                "if redis.call('HEXISTS', KEYS[1], ARGV[1]) == 1 then " +
                        "  redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2])); " +
                        "  return 1 " +
                        "else return 0 end"
        );
        HASH_HEARTBEAT_LUA.setResultType(Long.class);

        // 删除 Hash 中某个字段
        HASH_REMOVE_LUA = new DefaultRedisScript<>();
        HASH_REMOVE_LUA.setScriptText(
                "redis.call('HDEL', KEYS[1], ARGV[1]); " +
                        "if redis.call('HLEN', KEYS[1]) == 0 then " +
                        "  redis.call('DEL', KEYS[1]); " +
                        "end " +
                        "return 1;"
        );
        HASH_REMOVE_LUA.setResultType(Long.class);
    }

    static
    {
        // 熔断统计脚本：total/error/slow 原子递增 + 设置过期
        CIRCUIT_STAT_LUA = new DefaultRedisScript<>();
        CIRCUIT_STAT_LUA.setScriptText(
                "-- KEYS[1] = stat key\n" +
                        "-- ARGV[1] = window seconds\n" +
                        "-- ARGV[2] = isError (0/1)\n" +
                        "-- ARGV[3] = isSlow (0/1)\n" +
                        "\n" +
                        "redis.call('HINCRBY', KEYS[1], 'total', 1)\n" +
                        "\n" +
                        "if ARGV[2] == '1' then\n" +
                        "    redis.call('HINCRBY', KEYS[1], 'error', 1)\n" +
                        "end\n" +
                        "\n" +
                        "if ARGV[3] == '1' then\n" +
                        "    redis.call('HINCRBY', KEYS[1], 'slow', 1)\n" +
                        "end\n" +
                        "\n" +
                        "redis.call('EXPIRE', KEYS[1], ARGV[1])\n" +
                        "return 1"
        );
        CIRCUIT_STAT_LUA.setResultType(Long.class);

        // 熔断判断脚本：检查是否需要熔断
        CIRCUIT_CHECK_LUA = new DefaultRedisScript<>();
        CIRCUIT_CHECK_LUA.setScriptText(
                "-- KEYS[1] = stat key\n" +
                        "-- KEYS[2] = open key\n" +
                        "-- ARGV[1] = minRequest\n" +
                        "-- ARGV[2] = errorRate\n" +
                        "-- ARGV[3] = slowRate\n" +
                        "-- ARGV[4] = openSeconds\n" +
                        "\n" +
                        "-- 已经熔断，直接返回 0\n" +
                        "if redis.call('EXISTS', KEYS[2]) == 1 then\n" +
                        "    return 0\n" +
                        "end\n" +
                        "\n" +
                        "local total = tonumber(redis.call('HGET', KEYS[1], 'total') or '0')\n" +
                        "if total < tonumber(ARGV[1]) then\n" +
                        "    return 1\n" +
                        "end\n" +
                        "\n" +
                        "local error = tonumber(redis.call('HGET', KEYS[1], 'error') or '0')\n" +
                        "local slow  = tonumber(redis.call('HGET', KEYS[1], 'slow')  or '0')\n" +
                        "\n" +
                        "local errRate  = error / total\n" +
                        "local slowRate = slow / total\n" +
                        "\n" +
                        "if errRate >= tonumber(ARGV[2]) or slowRate >= tonumber(ARGV[3]) then\n" +
                        "    redis.call('SET', KEYS[2], '1', 'EX', ARGV[4])\n" +
                        "    redis.call('DEL', KEYS[1])\n" +
                        "    return 0\n" +
                        "end\n" +
                        "\n" +
                        "return 1"
        );
        CIRCUIT_CHECK_LUA.setResultType(Long.class);
    }

    private final StringRedisTemplate stringRedisTemplate;

    public GatewayRedisUtil(StringRedisTemplate stringRedisTemplate)
    {
        this.stringRedisTemplate = stringRedisTemplate;
        log.info("Redis KeySerializer: {}", stringRedisTemplate.getKeySerializer().getClass());
        log.info("Redis HashValueSerializer: {}", stringRedisTemplate.getHashValueSerializer().getClass());
    }

    /**
     * 安全生成 JSON 字符串：annotation / aliasNames 永远安全
     */
    public static String toSafeJson(Object obj)
    {
        if (obj == null) return "{}";
        // 对 LaiKeApiParams 或 List<LaiKeApiParams> 做兜底
        if (obj instanceof LaiKeApiParams)
        {
            LaiKeApiParams param = (LaiKeApiParams) obj;
            if (param.getAnnotation() == null) param.setAnnotation(new String[0]);
            if (param.getAliasNames() == null) param.setAliasNames("{}");
        }
        if (obj instanceof LaiKeApi)
        {
            LaiKeApi api = (LaiKeApi) obj;
            if (api.getParamsList() != null)
            {
                api.getParamsList().forEach(p ->
                {
                    if (p.getAnnotation() == null) p.setAnnotation(new String[0]);
                    if (p.getAliasNames() == null) p.setAliasNames("{}");
                });
            }
        }
        return JSON.toJSONString(obj);
    }

    /**
     * 上报熔断统计
     */
    public void circuitStat(String statKey,
                            int windowSeconds,
                            boolean isError,
                            boolean isSlow)
    {

        stringRedisTemplate.execute(
                CIRCUIT_STAT_LUA,
                Collections.singletonList(statKey),
                String.valueOf(windowSeconds),
                isError ? "1" : "0",
                isSlow ? "1" : "0"
        );
    }

    /**
     * 判断是否允许请求（未熔断）
     *
     * @return true = 放行，false = 熔断
     */
    public boolean circuitCheck(String statKey,
                                String openKey,
                                int minRequest,
                                double errorRate,
                                double slowRate,
                                int openSeconds)
    {
        Long result = stringRedisTemplate.execute(
                CIRCUIT_CHECK_LUA,
                Arrays.asList(statKey, openKey),
                String.valueOf(minRequest),
                String.valueOf(errorRate),
                String.valueOf(slowRate),
                String.valueOf(openSeconds)
        );

        return result != null && result == 1L;
    }

    /**
     * 原子轮询计数，用于 Round-Robin 负载均衡
     *
     * @param key        计数 Key，一般按接口唯一
     * @param ttlSeconds 计数 Key 的过期时间（秒），推荐 10 分钟
     * @return 当前计数值（从 1 开始）
     */
    public long incrementRoundRobin(String key, long ttlSeconds)
    {
        if (key == null || key.isEmpty()) return 0;
        Long result = stringRedisTemplate.execute(
                ROUND_ROBIN_LUA,
                Collections.singletonList(key),
                String.valueOf(ttlSeconds)
        );
        return result == null ? 0 : result;
    }

    // ======================== Hash 原子注册 ========================
    public void hashRegister(String key, String field, String value, long ttlSeconds)
    {
        stringRedisTemplate.execute(
                HASH_REGISTER_LUA,
                Collections.singletonList(key),
                field,
                value,
                String.valueOf(ttlSeconds)
        );
    }

    public boolean hashHeartbeat(String key, String field, String value, long ttlSeconds)
    {
        Long r = stringRedisTemplate.execute(
                HASH_HEARTBEAT_LUA,
                Collections.singletonList(key),
                field,
                String.valueOf(ttlSeconds)
        );
        return r != null && r == 1L;
    }

    public void hashRemove(String key, String field)
    {
        stringRedisTemplate.execute(
                HASH_REMOVE_LUA,
                Collections.singletonList(key),
                field
        );
    }

    // ======================== Hash 查询 ========================
    public Map<String, String> hmget(String redisKey)
    {
        if (redisKey == null || redisKey.isEmpty()) return Collections.emptyMap();
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(redisKey);
        if (entries == null || entries.isEmpty()) return Collections.emptyMap();

        Map<String, String> result = new HashMap<>(entries.size());
        for (Map.Entry<Object, Object> entry : entries.entrySet())
        {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }

    public String hget(String redisKey, String hashKey)
    {
        Object value = stringRedisTemplate.opsForHash().get(redisKey, hashKey);
        return value != null ? value.toString() : null;
    }

    public void hset(String redisKey, String hashKey, String value)
    {
        stringRedisTemplate.opsForHash().put(redisKey, hashKey, value);
    }

    public void hset(String redisKey, String hashKey, String value, long expireSeconds)
    {
        stringRedisTemplate.opsForHash().put(redisKey, hashKey, value);
        if (expireSeconds > 0)
        {
            stringRedisTemplate.expire(redisKey, expireSeconds, TimeUnit.SECONDS);
        }
    }

    public void hmset(String redisKey, Map<String, String> map)
    {
        if (map == null || map.isEmpty() || redisKey == null || redisKey.isEmpty()) return;
        stringRedisTemplate.opsForHash().putAll(redisKey, map);
    }

    public void hmset(String redisKey, Map<String, String> map, long expireSeconds)
    {
        hmset(redisKey, map);
        if (expireSeconds > 0)
        {
            stringRedisTemplate.expire(redisKey, expireSeconds, TimeUnit.SECONDS);
        }
    }

    // ======================== JSON 兜底工具 ========================

    public void hdel(String redisKey, String... hashKeys)
    {
        if (redisKey == null || redisKey.isEmpty() || hashKeys == null || hashKeys.length == 0) return;
        stringRedisTemplate.opsForHash().delete(redisKey, (Object[]) hashKeys);
    }

    // ======================== 安全版心跳更新 lastHeartbeatTime ========================

    public boolean hexists(String redisKey, String hashKey)
    {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForHash().hasKey(redisKey, hashKey));
    }

    public void del(String redisKey)
    {
        if (redisKey != null && !redisKey.isEmpty()) stringRedisTemplate.delete(redisKey);
    }


    // ======================== 白名单 / 黑名单 ========================

    public boolean exists(String redisKey)
    {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey));
    }

    /**
     * 原 hashHeartbeatUpdateLastTime 的安全替代版
     * Java 层更新 lastHeartbeatTime，Lua 不解析 JSON
     */
    public boolean hashHeartbeatUpdateLastTime(String key, String field, long timestamp, long ttlSeconds)
    {
        String json = hget(key, field);
        if (json == null) return false;

        LaiKeApi api;
        try
        {
            api = JSON.parseObject(json, LaiKeApi.class);
        }
        catch (Exception e)
        {
            log.error("解析 Redis JSON 失败", e);
            return false;
        }

        // 更新心跳时间
        api.setLastHeartbeatTime(timestamp);

        // 兜底 annotation / aliasNames
        if (api.getParamsList() != null)
        {
            api.getParamsList().forEach(p ->
            {
                if (p.getAnnotation() == null) p.setAnnotation(new String[0]);
                if (p.getAliasNames() == null) p.setAliasNames("{}");
            });
        }

        // 写回 Redis
        hashRegister(key, field, toSafeJson(api), ttlSeconds);
        return true;
    }

    /**
     * 原子限流
     */
    public boolean rateLimit(String key, int max, int seconds)
    {
        Long r = stringRedisTemplate.execute(
                RATE_LIMIT_LUA,
                Collections.singletonList(key),
                String.valueOf(max),
                String.valueOf(seconds)
        );
        return r != null && r == 1L;
    }

    /**
     * 添加白名单 IP（运行期生效）
     */
    public void addWhiteIp(String ip)
    {
        stringRedisTemplate.opsForSet().add(GW_WHITE_IP_SET, ip);
    }

    /**
     * 添加黑名单 IP（运行期生效）
     */
    public void addBlackIp(String ip)
    {
        stringRedisTemplate.opsForSet().add(GW_BLACK_IP_SET, ip);
    }

    /**
     * 移除白名单 IP
     */
    public void removeWhiteIp(String ip)
    {
        stringRedisTemplate.opsForSet().remove(GW_WHITE_IP_SET, ip);
    }

    /**
     * 是否白名单 IP（Redis）
     */
    public boolean isWhiteIp(String ip)
    {
        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForSet().isMember(GW_WHITE_IP_SET, ip)
        );
    }

    /**
     * 是否黑名单 IP（Redis）
     */
    public boolean isBlackIp(String ip)
    {
        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForSet().isMember(GW_BLACK_IP_SET, ip)
        );
    }

    public boolean idempotent(String key, long seconds)
    {
        Long r = stringRedisTemplate.execute(
                IDEMPOTENT_LUA,
                Collections.singletonList(key),
                String.valueOf(seconds)
        );
        return r != null && r == 1L;
    }


    public void deleteIdempotentKey(String key)
    {
        stringRedisTemplate.delete(key);
    }


}
