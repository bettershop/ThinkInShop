package com.laiketui.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiLimit
{
    // 最大请求数
    int limit() default 1;
    // 时间窗口（秒）
    int time() default 10;

    TimeUnit unit() default TimeUnit.SECONDS;
}
