package com.laiketui.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock
{
    // 锁名称
    String lockName() default "";

    // 释放时间
    long releaseTime() default 3 * 1000;

    // 时间单位
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
