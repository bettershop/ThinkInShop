package com.laiketui.common.process;

import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.api.order.PublicTaskService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HandlerOrderProcessor implements ApplicationContextAware
{

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        //获取所有策略注解的Bean
        Map<String, Object> orderStrategyMap = applicationContext.getBeansWithAnnotation(HandlerOrderType.class);
        orderStrategyMap.forEach((k, v) ->
        {
            Class<OrderDubboService> orderStrategyClass = (Class<OrderDubboService>) v.getClass();
            //异步任务
            Class<PublicTaskService> taskStrategyClass = (Class<PublicTaskService>) v.getClass();
            String[]                 types             = orderStrategyClass.getAnnotation(HandlerOrderType.class).type();
            //将class加入map中,type作为key
            for (String type : types)
            {
                HandlerOrderContext.orderStrategyBeanMap.put(type, orderStrategyClass);
                //异步任务
                HandlerOrderTaskContext.taskServiceBeanMap.put(type, taskStrategyClass);
            }
        });
    }
}
