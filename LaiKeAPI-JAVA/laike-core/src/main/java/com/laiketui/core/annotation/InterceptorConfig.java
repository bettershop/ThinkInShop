package com.laiketui.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InterceptorConfig {

    /**
     * 拦截路径，默认全部
     */
    String[] includePatterns() default {"/**"};

    /**
     * 排除路径
     */
    String[] excludePatterns() default {};

    /**
     * 执行顺序（越小越先执行）
     */
    int order() default Integer.MAX_VALUE;

}
