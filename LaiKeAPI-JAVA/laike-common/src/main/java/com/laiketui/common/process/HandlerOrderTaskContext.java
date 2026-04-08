package com.laiketui.common.process;

import com.laiketui.common.api.order.PublicTaskService;
import com.laiketui.core.exception.LaiKeAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.PARAMATER_ERROR;

/**
 * 异步任务适配器
 *
 * @author Trick
 * @date 2022/7/29 10:12
 */
@Component
public class HandlerOrderTaskContext
{

    @Autowired
    private ApplicationContext applicationContext;

    //存放所有策略类Bean的map
    public static Map<String, Class<PublicTaskService>> taskServiceBeanMap = new HashMap<>();

    public PublicTaskService getOrderService(String orderType) throws LaiKeAPIException
    {
        Class<PublicTaskService> strategyClass = taskServiceBeanMap.get(orderType);
        if (strategyClass == null)
        {
            throw new LaiKeAPIException(PARAMATER_ERROR, "参数错误：不支持的订单类型！");
        }
        //从容器中获取对应的策略Bean
        return applicationContext.getBean(strategyClass);
    }

}
