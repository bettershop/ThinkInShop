package com.laiketui.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 货币计算工具类
 */
public final class CurrencyUtils
{

    // 缓存常用的汇率值，避免重复计算
    private static final ConcurrentMap<String, BigDecimal> RATE_CACHE = new ConcurrentHashMap<>();

    // 默认精度：小数点后两位
    private static final int DEFAULT_SCALE = 2;

    // 默认舍入模式：四舍五入
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * 计算基于基础价格和汇率的转换后价格
     *
     * @param basePrice    基础价格（ BigDecimal 类型，避免精度丢失）
     * @param exchangeRate 汇率（ BigDecimal 类型）
     * @return 转换后的价格（四舍五入，保留两位小数）
     */
    public static BigDecimal calculatePrice(BigDecimal basePrice, BigDecimal exchangeRate) throws IllegalArgumentException
    {
        // 空值检查
        if (basePrice == null || exchangeRate == null)
        {
            throw new IllegalArgumentException("基础价格和汇率不能为空");
        }
        // 计算价格：基础价格 × 汇率
        return basePrice.multiply(exchangeRate).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 重载方法：支持 double 类型的基础价格
     */
    public static BigDecimal calculatePrice(double basePrice, BigDecimal exchangeRate)
    {
        return calculatePrice(BigDecimal.valueOf(basePrice), exchangeRate);
    }

    /**
     * 重载方法：支持 double 类型的汇率
     */
    public static BigDecimal calculatePrice(BigDecimal basePrice, double exchangeRate)
    {
        return calculatePrice(basePrice, BigDecimal.valueOf(exchangeRate));
    }

    /**
     * 重载方法：支持 double 类型的基础价格和汇率
     */
    public static BigDecimal calculatePrice(double basePrice, double exchangeRate)
    {
        return calculatePrice(BigDecimal.valueOf(basePrice), BigDecimal.valueOf(exchangeRate));
    }

    /**
     * TODO  从缓存中获取或计算汇率 [不可用]
     *
     * @param fromCurrency 源货币代码
     * @param toCurrency   目标货币代码
     * @return 汇率（ BigDecimal 类型）
     */
    public static BigDecimal getExchangeRate(String fromCurrency, String toCurrency) throws IllegalArgumentException
    {
        // 空值检查
        if (fromCurrency == null || toCurrency == null)
        {
            throw new IllegalArgumentException("货币代码不能为空");
        }

        // 构建缓存键
        String cacheKey = fromCurrency + "_" + toCurrency;

        // 从缓存获取汇率（线程安全的原子操作）
        BigDecimal rate = RATE_CACHE.get(cacheKey);

        if (rate == null)
        {
            // 缓存未命中，计算新汇率（实际场景中可能从外部服务获取）
            rate = fetchExchangeRateFromExternalService(fromCurrency, toCurrency);

            // 使用 putIfAbsent 原子性地更新缓存，避免多线程重复计算
            BigDecimal existingRate = RATE_CACHE.putIfAbsent(cacheKey, rate);
            if (existingRate != null)
            {
                rate = existingRate; // 其他线程已更新缓存，使用最新值
            }
        }

        return rate;
    }

    /**
     * TODO 从外部服务获取实时汇率 [不可用]
     */
    private static BigDecimal fetchExchangeRateFromExternalService(String fromCurrency, String toCurrency)
    {
        // 调用第三方汇率服务[收费]
        return new BigDecimal("6.8521");
    }

    /**
     * 设置自定义精度和舍入模式
     */
    public static BigDecimal calculatePrice(BigDecimal basePrice, BigDecimal exchangeRate, int scale, RoundingMode roundingMode)
    {
        return basePrice.multiply(exchangeRate)
                .setScale(scale, roundingMode);
    }
}