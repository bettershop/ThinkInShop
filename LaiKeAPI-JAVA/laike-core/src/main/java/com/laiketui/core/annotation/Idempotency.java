package com.laiketui.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 幂等
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotency
{

    /**
     * key的过期时间 等于请求最小间隔时间，也就是限制用户端在一秒之内不允许同一个客户端对同一个方法发送相同的请求参数
     * 秒杀除外
     */
    long timeout() default 2L;

    /**
     * 超过请求幂等性提示信息
     */
    String message() default "请勿重复操作";

    /**
     * 请求结束后是否删除key 推荐 false 让他自动过期 true 则变相认为不适用幂等性拦截
     */
    boolean delKey() default false;

}
