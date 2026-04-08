package com.laiketui.cdc.services;

import com.alibaba.fastjson2.JSON;
import com.laiketui.core.cache.RedisUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class FlinkCdcService implements InitializingBean {
    // 该队列专门用来临时保存变化的数据（实际生产环境，你应该使用MQ相关的产品）
    public static final LinkedBlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>() ;

    private RedisUtil redisUtil ;
    // 保存到redis中key的前缀
    private final String PREFIX = "users:" ;

    public FlinkCdcService(RedisUtil redisUtil ) {
        this.redisUtil = redisUtil ;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 启动异步线程，实时处理队列中的数据
        new Thread(() -> {
            while(true) {
                try {
                    Map<String, Object> result = queue.take();
                    this.doAction(result) ;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start() ;
    }

    @SuppressWarnings("unchecked")
    private void doAction(Map<String, Object> result) throws Exception {
        Map<String, Object> payload = (Map<String, Object>) result.get("payload") ;
        String op = (String) payload.get("op") ;
        Map<String, Object> after = null;
        String id = null;
        switch (op) {
            // 更新和插入操作
            case "u":
            case "c":
                after = (Map<String, Object>) payload.get("after") ;
                id = after.get("id").toString();
                System.out.printf("操作：%s, ID: %s%n", op, id) ;
                redisUtil.set(PREFIX + id, JSON.toJSONString(after)) ;
                break;
            // 删除操作
            case "d" :
                after = (Map<String, Object>) payload.get("before") ;
                id = after.get("id").toString();
                System.out.printf("操作：%s, ID: %s%n", op, id) ;
                redisUtil.del(PREFIX + id);
                break;

            case "r" :
                after = (Map<String, Object>) payload.get("before") ;
                System.out.println("======>"+JSON.parseObject(JSON.toJSONString(after)).getString("user_name"));
                break;
        }
    }
}
