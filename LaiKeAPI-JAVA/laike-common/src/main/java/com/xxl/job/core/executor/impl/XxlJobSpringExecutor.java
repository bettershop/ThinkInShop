package com.xxl.job.core.executor.impl;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.handler.impl.MethodJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * xxl-job executor (for spring) - 延迟启动版本（适配 2.3.x）
 * 解决：启动时 Admin 回调执行器端口未监听导致的 Connection refused
 */
public class XxlJobSpringExecutor extends XxlJobExecutor implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(XxlJobSpringExecutor.class);

    // ---------------------- applicationContext ----------------------
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // 延迟调度器，只用于延迟启动 registry/心跳线程
    private final ScheduledExecutorService delayScheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        XxlJobSpringExecutor.applicationContext = applicationContext;
    }

    // start - 核心改动：延迟调用 super.start()
    @Override
    public void afterSingletonsInstantiated() {

        // init JobHandler Repository (for method) - 使用 2.3.x 兼容写法
        initJobHandlerMethodRepository(applicationContext);

        // refresh GlueFactory
        GlueFactory.refreshInstance(1);

        logger.info("XXL-Job Spring Executor 初始化完成，延迟 5 秒后启动 registry、心跳等线程...");

        // 延迟 5 秒再启动（给 Web 容器 bind 端口足够时间）
        delayScheduler.schedule(() -> {
            try {
                logger.info("延迟 5 秒结束，开始执行 XXL-Job super.start()...");
                super.start();  // 现在端口基本已监听，可以安全注册
            } catch (Exception e) {
                logger.error("延迟启动 XXL-Job 执行器失败", e);
                throw new RuntimeException("XXL-Job 启动失败", e);
            }
        }, 5, TimeUnit.SECONDS);  // 可调整为 3000~8000 ms
    }

    // destroy
    @Override
    public void destroy() {
        if (delayScheduler != null && !delayScheduler.isShutdown()) {
            delayScheduler.shutdown();
        }
        super.destroy();
    }

    /**
     * 适配 XXL-Job 2.3.x 的方法注解扫描（不使用 MethodIntrospector）
     */
    private void initJobHandlerMethodRepository(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }

        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);

            // 遍历所有方法，查找 @XxlJob 注解（2.3.x 兼容方式）
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method executeMethod : methods) {
                XxlJob xxlJob = AnnotationUtils.findAnnotation(executeMethod, XxlJob.class);
                if (xxlJob == null) {
                    continue;
                }

                String name = xxlJob.value();
                if (name.trim().length() == 0) {
                    throw new RuntimeException("xxl-job method-jobhandler name invalid, for[" + bean.getClass() + "#" + executeMethod.getName() + "] .");
                }
                if (loadJobHandler(name) != null) {
                    throw new RuntimeException("xxl-job jobhandler[" + name + "] naming conflicts.");
                }

                executeMethod.setAccessible(true);

                // init and destroy 方法
                Method initMethod = null;
                Method destroyMethod = null;

                if (xxlJob.init().trim().length() > 0) {
                    try {
                        initMethod = bean.getClass().getDeclaredMethod(xxlJob.init());
                        initMethod.setAccessible(true);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("xxl-job method-jobhandler initMethod invalid, for[" + bean.getClass() + "#" + executeMethod.getName() + "] .");
                    }
                }
                if (xxlJob.destroy().trim().length() > 0) {
                    try {
                        destroyMethod = bean.getClass().getDeclaredMethod(xxlJob.destroy());
                        destroyMethod.setAccessible(true);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("xxl-job method-jobhandler destroyMethod invalid, for[" + bean.getClass() + "#" + executeMethod.getName() + "] .");
                    }
                }

                // 注册 MethodJobHandler
                registJobHandler(name, new MethodJobHandler(bean, executeMethod, initMethod, destroyMethod));
            }
        }
    }
}