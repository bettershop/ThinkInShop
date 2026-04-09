package com.laiketui.core.common;

import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 核心代码为其IdWorker这个类实现，其原理结构如下，我分别用一个0表示一位，用—分割开部分的作用：
 * 1||0---0000000000 0000000000 0000000000 0000000000 0 --- 00000 ---00000 ---000000000000
 * 在上面的字符串中，第一位为未使用（实际上也可作为long的符号位），接下来的41位为毫秒级时间，
 * 然后5位datacenter标识位，5位机器ID（并不算标识符，实际是为线程标识），
 * 然后12位该毫秒内的当前毫秒内的计数，加起来刚好64位，为一个Long型。
 * 这样的好处是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞（由datacenter和机器ID作区分），
 * 并且效率较高，经测试，snowflake每秒能够产生26万ID左右，完全满足需要。
 * <p>
 * 64位ID (42(毫秒)+5(机器ID)+5(业务编码)+12(重复累加))
 *
 * @description: 分布式自增长ID
 * @author: wx
 * @date: Created in 2019/10/18 14:55
 * @version: 1.0
 * @modified By:
 */

@Component
public class LKTSnowflakeIdWorker
{

    /**
     * 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动）
     */
    private final static long twepoch            = 1288834974657L;
    /*
     * 机器标识位数
     */
    private final static long workerIdBits       = 5L;
    /*
     * 数据中心标识位数
     */
    private final static long datacenterIdBits   = 5L;
    /**
     * 机器ID最大值
     */
    private final static long maxWorkerId        = -1L ^ (-1L << workerIdBits);
    /**
     * 数据中心ID最大值
     */
    private final static long maxDatacenterId    = -1L ^ (-1L << datacenterIdBits);
    /**
     * 毫秒内自增位
     */
    private final static long sequenceBits       = 12L;
    /**
     * 机器ID偏左移12位
     */
    private final static long workerIdShift      = sequenceBits;
    /**
     * 数据中心ID左移17位
     */
    private final static long datacenterIdShift  = sequenceBits + workerIdBits;
    /**
     * 时间毫秒左移22位
     */
    private final static long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    private final static long sequenceMask = -1L ^ (-1L << sequenceBits);

    private final long workerId;
    /**
     * 数据标识id部分
     */
    private final long datacenterId;

    // 关键：使用 AtomicLong 管理状态，避免 synchronized
    /**
     * 并发控制
     */
    private final AtomicLong sequence      = new AtomicLong(0L);
    /**
     * 上次生产id时间戳
     */
    private final AtomicLong lastTimestamp = new AtomicLong(-1L);

    public LKTSnowflakeIdWorker()
    {
        this.datacenterId = getDatacenterId(maxDatacenterId);
        this.workerId = getMaxWorkerId(this.datacenterId, maxWorkerId);
    }

    /**
     * @param workerId     工作机器ID
     * @param datacenterId 序列号
     */
    public LKTSnowflakeIdWorker(long workerId, long datacenterId)
    {
        if (workerId > maxWorkerId || workerId < 0)
        {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0)
        {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 生成下一个全局唯一、趋势递增的 Snowflake ID。
     * <p>
     * 算法基于时间戳（41位）+ 数据中心ID（5位）+ 机器ID（5位）+ 序列号（12位），
     * 确保在分布式环境下无冲突、高性能（理论每节点每秒约 409.6 万 ID）。
     * </p>
     *
     * @return 64 位 long 类型的唯一 ID
     * @throws RuntimeException 当系统时钟发生回拨（当前时间早于上次生成 ID 的时间）时抛出异常，防止 ID 重复
     */
    public long nextId()
    {
        // 获取当前系统时间戳（毫秒）
        long timestamp = System.currentTimeMillis();
        // 读取上一次生成 ID 的时间戳（volatile 保证可见性）
        long lastTs = lastTimestamp.get();

        // ⚠️ 检查时钟回拨：如果当前时间小于上次记录的时间，说明系统时间被调回
        if (timestamp < lastTs)
        {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (lastTs - timestamp) + " ms");
        }

        long seq;
        // 判断是否仍在同一毫秒内
        if (timestamp == lastTs)
        {
            // 同一毫秒内，序列号自增（使用原子操作保证线程安全） sequenceMask = 4095 (0xFFF)，确保序列号在 0～4095 范围内
            seq = sequence.incrementAndGet() & sequenceMask;

            // 如果序列号溢出归零（即本毫秒已生成 4096 个 ID），需等待进入下一毫秒
            if (seq == 0)
            {
                // 忙等待直到系统时间进入新的毫秒
                while ((timestamp = System.currentTimeMillis()) <= lastTs)
                {
                    Thread.yield(); // 礼貌地让出 CPU 时间片，减少空转消耗
                }
                // 进入新毫秒后，重置序列号为 0（下一轮从 0 开始）
                sequence.set(0L);
                seq = 0L;
            }
        }
        else
        {
            // 进入全新毫秒，重置序列号为 0
            sequence.set(0L);
            seq = 0L;
        }

        // 使用 CAS（Compare-And-Swap）原子更新 lastTimestamp，确保多线程下时间戳状态一致
        while (!lastTimestamp.compareAndSet(lastTs, timestamp))
        {
            // 如果 CAS 失败（说明有其他线程已更新时间戳），重新读取最新值
            lastTs = lastTimestamp.get();
            // 再次校验是否发生时钟回拨（极端并发下可能在 CAS 间隙出现）
            if (timestamp < lastTs)
            {
                throw new RuntimeException("Clock moved backwards during CAS.");
            }
            // 注意：此处不重试生成逻辑，因为 timestamp 已是最新的，可直接用于拼接 ID
            // 实际上，只要时间不回拨，CAS 最终会成功
        }

        // 拼接最终 ID：
        // (时间戳 - 起始时间) << 22 | datacenterId << 17 | workerId << 12 | sequence
        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | seq;
    }

    /**
     * <p>
     * 获取 maxWorkerId
     * </p>
     */
    protected long getMaxWorkerId(long datacenterId, long maxWorkerId)
    {
        try
        {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            String pid  = name.split("@")[0];
            return ((datacenterId + pid.hashCode()) & 0xffff) % (maxWorkerId + 1);
        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
        }
        return 0L; // fallback
    }

    /**
     * <p>
     * 数据标识id部分
     * </p>
     */
    protected long getDatacenterId(long maxDatacenterId)
    {
        try
        {
            InetAddress      ip      = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network != null)
            {
                byte[] mac = network.getHardwareAddress();
                if (mac != null)
                {
                    long id = ((0xFF & mac[mac.length - 1]) | (0xFF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
                    return id % (maxDatacenterId + 1);
                }
            }
        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
        }
        return 1L; // fallback
    }

}
