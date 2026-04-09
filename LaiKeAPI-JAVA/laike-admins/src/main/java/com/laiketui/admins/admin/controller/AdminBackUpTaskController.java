package com.laiketui.admins.admin.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.admins.api.admin.AdminBackUpService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.lktconst.GloabConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
@EnableScheduling
public class AdminBackUpTaskController implements SchedulingConfigurer
{

    @Autowired
    private AdminBackUpService adminBackUpService;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar)
    {
        taskRegistrar.setTaskScheduler(taskScheduler);
    }

    @Bean
    public TaskScheduler taskScheduler()
    {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setDaemon(true);
        return scheduler;
    }

    @Autowired
    private TaskScheduler taskScheduler;

    @PostConstruct
    public void initScheduledTask()
    {
        //获取所有商城id缓存
        List<Integer> storeIdList = null;
        if (redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST) == null)
        {
            adminBackUpService.taskStoreAll();
        }
        if (redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST) != null)
        {
            storeIdList = JSON.parseObject(redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST).toString(), new TypeReference<List<Integer>>()
            {
            });
        }
        if (storeIdList == null || storeIdList.size() == 0)
        {
            return;
        }
        storeIdList.stream().forEach(storeId ->
        {
            // 初始时从数据库读取cron表达式并设置定时任务
            String cronExpression = adminBackUpService.getNewCronByStoreId(storeId);
            if ("".equals(cronExpression))
            {
                return;
            }
            taskScheduler.schedule(adminBackUpService.immediatelyTask(storeId), new CronTrigger(cronExpression));
        });
    }

}
