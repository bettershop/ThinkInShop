package com.laiketui.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface HandlerOrderType
{
    /**
     * 策略类型 GM MS PT PS KJ
     *
     * @return
     */
    String[] type();
}