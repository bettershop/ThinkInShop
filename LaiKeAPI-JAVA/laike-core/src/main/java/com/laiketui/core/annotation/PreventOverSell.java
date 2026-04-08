package com.laiketui.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防超卖注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreventOverSell
{

    /**
     * 插件类型
     *
     * @return
     */
    String type() default "GM";

    /**
     * 已售罄
     */
    String message() default "已售罄";

}
