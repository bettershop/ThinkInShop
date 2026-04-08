package com.laiketui.comps.gateway.controller;

import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.domain.LaiKeApi;
import com.laiketui.core.utils.tool.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Objects;


/**
 * @description: API服务是否可用检查
 * @author: wx
 */
public class CompsLaiKeApiChecker implements Runnable
{

    private final Logger logger = LoggerFactory.getLogger(CompsLaiKeApiChecker.class);

    private RedisUtil redisUtil;

    private String fullApiKey;


    public CompsLaiKeApiChecker(RedisUtil redisUtil, String fullApiKey)
    {
        this.redisUtil = redisUtil;
        this.fullApiKey = fullApiKey;
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
                            if (newApis.size() == 0)
                            {
                                redisUtil.del(fullApiKey);
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