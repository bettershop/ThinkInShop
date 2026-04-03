package com.laiketui.core.cache;


import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @description:
 * @author: wx
 * @date: Created in 2019/10/16 19:22
 * @version: 1.0
 * @modified By:
 */
public final class RedisUtil
{

    private RedisTemplate<String, Object> redisTemplate;

    public static void main(String[] args)
    {
        Jedis jedis = new Jedis("127.0.0.1", 6339);
        jedis.auth("laiketui18");
        jedis.select(5);
        Set<String> keys = jedis.keys("*LAIKE_API*");
        for (String key : keys)
        {
            jedis.del(key);
        }
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time)
    {
        try
        {
            if (time > 0)
            {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key)
    {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key)
    {
        try
        {
            return redisTemplate.hasKey(key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除 Redis 缓存（支持单个或多个 key 可变参数）
     *
     * @param keys 可变参数 key
     * @return 实际删除的 key 数量
     */
    public Long del(String... keys)
    {
        if (keys == null || keys.length == 0)
        {
            return 0L;
        }

        // 转换为 List 并调用上面的批量方法（复用逻辑）
        return del(Arrays.asList(keys));
    }

    /**
     * 批量删除 Redis 中的缓存（支持 List<String>）
     *
     * @param keys 要删除的 key 列表
     * @return 实际删除的 key 数量（Redis 6+ 返回 Long，低于 6 返回 null 时返回 0）
     */
    public Long del(List<String> keys)
    {
        if (keys == null || keys.isEmpty())
        {
            return 0L;
        }

        // 过滤掉 null 和空字符串（可选，但更健壮）
        List<String> validKeys = keys.stream()
                .filter(key -> key != null && !key.trim().isEmpty())
                .collect(Collectors.toList());

        if (validKeys.isEmpty())
        {
            return 0L;
        }

        Long deletedCount = redisTemplate.delete(validKeys);
        return deletedCount != null ? deletedCount : 0L;
    }

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key)
    {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value)
    {
        try
        {
            redisTemplate.opsForValue().set(key, value);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time)
    {
        try
        {
            if (time > 0)
            {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            }
            else
            {
                set(key, value);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, String value, long time)
    {
        try
        {
            if (time > 0)
            {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            }
            else
            {
                set(key, value);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta)
    {
        if (delta < 0)
        {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta)
    {
        if (delta < 0)
        {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hget(String key, String item)
    {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Map<String, Object> map)
    {
        try
        {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time)
    {
        try
        {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0)
            {
                expire(key, time);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value)
    {
        try
        {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time)
    {
        try
        {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0)
            {
                expire(key, time);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item)
    {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item)
    {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public double hincr(String key, String item, double by)
    {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    public double hdecr(String key, String item, double by)
    {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<Object> sGet(String key)
    {
        try
        {
            return redisTemplate.opsForSet().members(key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value)
    {
        try
        {
            return redisTemplate.opsForSet().isMember(key, value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values)
    {
        try
        {
            return redisTemplate.opsForSet().add(key, values);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values)
    {
        try
        {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0)
            {
                expire(key, time);
            }
            return count;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key)
    {
        try
        {
            return redisTemplate.opsForSet().size(key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values)
    {
        try
        {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end)
    {
        try
        {
            return redisTemplate.opsForList().range(key, start, end);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lGetListSize(String key)
    {
        try
        {
            return redisTemplate.opsForList().size(key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index)
    {
        try
        {
            return redisTemplate.opsForList().index(key, index);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value)
    {
        try
        {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value, long time)
    {
        try
        {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0)
            {
                expire(key, time);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value)
    {
        try
        {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time)
    {
        try
        {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0)
            {
                expire(key, time);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value)
    {
        try
        {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value)
    {
        try
        {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 实现redis keys 模糊查询
     *
     * @param pattern
     * @return
     * @author hq
     */
    private <T> List<T> redisKeys(String pattern)
    {

        return null;
    }

    /**
     * 获取指定前缀的 Redis-Key
     *
     * @param keyPrefix
     * @return
     */
    public Set<String> keyScan(String keyPrefix)
    {
        return redisTemplate.keys(keyPrefix);
    }

    /**
     * 获取当前连接
     */
    public RedisConnectionFactory getConnectionFactory()
    {
        return redisTemplate.getConnectionFactory();
    }

    /**
     * 批量获取多个 key 的值（mget）
     *
     * @param keys key 集合
     * @return Map<key, value>，不存在的 key 对应 null
     */
    public Map<String, String> mget(Collection<String> keys)
    {
        if (keys == null || keys.isEmpty())
        {
            return Collections.emptyMap();
        }

        // 转换为 List
        List<String> keyList = new ArrayList<>(keys);

        // 使用 RedisTemplate 的 multiGet 批量获取
        List<Object> values = redisTemplate.opsForValue().multiGet(keyList);

        // 构建结果 Map（保持顺序）
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < keyList.size(); i++)
        {
            String key   = keyList.get(i);
            Object value = values.get(i);
            // value 可能为 null（key 不存在）
            result.put(key, value != null ? value.toString() : null);
        }

        return result;
    }

    /**
     * 批量获取多个 key 的值（支持任意类型）
     *
     * @param keys key 集合
     * @param <T>  值类型
     * @return Map<key, value>，不存在的 key 对应 null
     */
    public <T> Map<String, T> mget(Collection<String> keys, Class<T> clazz)
    {
        if (keys == null || keys.isEmpty())
        {
            return Collections.emptyMap();
        }

        List<String> keyList = new ArrayList<>(keys);
        List<Object> values  = redisTemplate.opsForValue().multiGet(keyList);

        Map<String, T> result = new LinkedHashMap<>();
        for (int i = 0; i < keyList.size(); i++)
        {
            String key   = keyList.get(i);
            Object value = values.get(i);
            if (value != null)
            {
                try
                {
                    result.put(key, (T) value);
                }
                catch (ClassCastException e)
                {
                    result.put(key, null);
                }
            }
            else
            {
                result.put(key, null);
            }
        }
        return result;
    }

    /**
     * 安全地按 pattern 批量删除 key（使用 SCAN 避免阻塞）
     *
     * @param pattern 匹配模式，例如 "admin:async_routes:*"
     * @return 删除的 key 数量
     */
    public Long delByPattern(String pattern)
    {
        if (StringUtils.isBlank(pattern))
        {
            return 0L;
        }

        long deletedCount = 0;
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection())
        {
            Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions()
                            .match(pattern)
                            .count(100)  // 每次扫描 100 条
                            .build()
            );

            List<byte[]> keysToDelete = new ArrayList<>();
            while (cursor.hasNext())
            {
                keysToDelete.add(cursor.next());
                // 每收集 500 条就批量删除一次，避免内存占用过大
                if (keysToDelete.size() >= 500)
                {
                    deletedCount += connection.del(keysToDelete.toArray(new byte[0][]));
                    keysToDelete.clear();
                }
            }

            // 删除剩余的
            if (!keysToDelete.isEmpty())
            {
                deletedCount += connection.del(keysToDelete.toArray(new byte[0][]));
            }

            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return deletedCount;
    }

}
