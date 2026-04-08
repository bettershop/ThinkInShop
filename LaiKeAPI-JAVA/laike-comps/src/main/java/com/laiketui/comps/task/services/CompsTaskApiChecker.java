package com.laiketui.comps.task.services;

import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.domain.LaiKeApi;
import com.laiketui.core.utils.tool.NetworkUtils;
import com.xxl.job.core.context.XxlJobHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Objects;


/**
 * @description: API服务是否可用检查
 * @author: wx
 */
public class CompsTaskApiChecker implements Runnable
{

    private final Logger logger = LoggerFactory.getLogger(CompsTaskApiChecker.class);

    private final RedisUtil redisUtil;

    private LinkedList<LaiKeApi> apis;

    private String fullApiKey;

    public CompsTaskApiChecker(RedisUtil redisUtil, String fullApiKey)
    {
        this.redisUtil = redisUtil;
        this.fullApiKey = fullApiKey;
    }

    public static void main(String[] args)
    {
        boolean isAvailability = NetworkUtils.isHostConnectable("47.107.123.240", 9850, 10000);
        System.out.println(isAvailability);
    }

    @Override
    public void run()
    {
        LinkedList<LaiKeApi> newApis = null;
        LinkedList<LaiKeApi> apis    = null;
        try
        {
            apis = (LinkedList<LaiKeApi>) redisUtil.get(fullApiKey);
            if (apis != null)
            {
                newApis = new LinkedList<>(apis);
                for (LaiKeApi api : apis)
                {
                    boolean isAvailability = NetworkUtils.isHostConnectable(api.getNodeIp(), api.getPort(), api.getTimeout());
                    if (!isAvailability)
                    {
                        if (!Objects.isNull(newApis))
                        {
                            newApis.remove(api);
                            XxlJobHelper.log("剩下未检查接口:", newApis);
                            if (newApis.size() == 0)
                            {
                                redisUtil.del(fullApiKey);
                                XxlJobHelper.log("api:【{}】服务未启动或不可用", api.getApiKey());
                            }
                            else
                            {
                                redisUtil.set(fullApiKey, newApis);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("检查接口是否可用异常:{}", e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            newApis = null;
            apis = null;
            fullApiKey = null;
        }
    }
}