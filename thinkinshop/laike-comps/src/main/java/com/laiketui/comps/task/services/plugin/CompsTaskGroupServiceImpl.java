package com.laiketui.comps.task.services.plugin;

import com.alibaba.fastjson2.JSON;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.order.PublicTaskService;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.comps.api.task.CompsTaskService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.xxl.job.core.context.XxlJobHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 竞拍异步任务
 *
 * @author Trick
 * @date 2021/4/24 9:00
 */
@Service
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_PT + DictionaryConst.TaskType.TASK)
public class CompsTaskGroupServiceImpl implements PublicTaskService
{

    @Autowired
    private CompsTaskService taskServer;

    @Autowired
    private HttpApiUtils httpApiUtils;

    interface ApiKey
    {
        String EXECUTE_API_MAIN = "plugin.group.task.execute";
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void execute() throws LaiKeAPIException
    {
        Map<String, Object> params      = null;
        Map<String, Object> resultMap   = null;
        List<Integer>       storeIdList = null;
        try
        {
            XxlJobHelper.log("拼团异步任务 开始执行!");
            storeIdList = taskServer.getStoreIdAll();
            XxlJobHelper.log("====================== 拼团异步任务开始执行 ======================");
            for (int storeId : storeIdList)
            {
                params = new HashMap<>(16);
                params.put("storeId", storeId);
                resultMap = httpApiUtils.executeApi(ApiKey.EXECUTE_API_MAIN, JSON.toJSONString(params));
                XxlJobHelper.log(resultMap.get("logo").toString());
            }
            XxlJobHelper.log("====================== 拼团异步任务 执行完毕! ======================");
        }
        catch (Exception e)
        {
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail("拼团任务 异常: " + e.getMessage());
        }
        finally
        {
            params = null;
            resultMap = null;
            storeIdList = null;
        }
    }
}

