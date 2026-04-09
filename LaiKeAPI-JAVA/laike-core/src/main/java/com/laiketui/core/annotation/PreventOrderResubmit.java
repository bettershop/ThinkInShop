package com.laiketui.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreventOrderResubmit
{
    /** 锁定时间（默认3分钟） */
    int lockedTime() default 180;
    /** 锁等待时间 */
    long waitTime() default 1;
    /** 锁持有时间 */
    long leaseTime() default 3;

    /**
     * SpEL表达式，用于动态获取订单ID
     * 示例："#vo.orderId" 或 "#arg0.id"
     */
    String keyExpr() ;

    /** 是否自动释放锁（支付成功后立即释放） */
    boolean autoRelease() default false;
}
