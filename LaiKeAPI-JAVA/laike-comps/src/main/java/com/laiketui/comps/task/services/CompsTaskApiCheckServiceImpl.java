package com.laiketui.comps.task.services;

import com.laiketui.comps.api.task.CompsTaskApiCheckService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.LaiKeGlobleConst;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.xxl.job.core.context.XxlJobHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * redis接口是否可用定时检查服务
 *
 * @author vvx
 * @date 2023/12/04 12:01
 */
@Service(value = "apiCheckService")
public class CompsTaskApiCheckServiceImpl implements CompsTaskApiCheckService
{

    private final Logger logger = LoggerFactory.getLogger(CompsTaskApiCheckServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 最大并发请求数
     */
    private final int MAX_CONCURRENT_REQUESTS = 100;

    @Override
    public void checkApis() throws LaiKeAPIException
    {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS);
        try
        {
            Set<String> keys = redisUtil.keyScan(LaiKeGlobleConst.RedisCacheKeyPre.LAIKE_API + SplitUtils.XX);
            for (String apiCacheKey : keys)
            {
                XxlJobHelper.log("开始检查api:{}", apiCacheKey);
                executor.execute(new CompsTaskApiChecker(redisUtil, apiCacheKey));
                XxlJobHelper.log("检查结束api:{}", apiCacheKey);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("检查接口出错!");
        }
        finally
        {
            // 等待所有任务执行完毕后关闭线程池
            executor.shutdown();
        }
    }


}
