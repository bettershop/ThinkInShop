package com.laiketui.comps.task.services.plugin;

import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.order.PublicTaskService;
import com.laiketui.common.api.plugin.PublicAuctionService;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.comps.api.task.CompsTaskService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.xxl.job.core.context.XxlJobHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_JP + DictionaryConst.TaskType.TASK)
public class CompsTaskAuctionServiceImpl implements PublicTaskService
{

    @Autowired
    private CompsTaskService taskServer;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Autowired
    private PublicAuctionService publicAuctionService;

    interface ApiKey
    {
        String EXECUTE_API_MAIN = "plugin.auction.task.execute";
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void execute() throws LaiKeAPIException
    {
        Map<String, Object> params      = null;
        List<Integer>       storeIdList = null;
        Map<String, Object> resultMap   = null;
        try
        {
            XxlJobHelper.log("竞拍异步任务 开始执行!");
            storeIdList = taskServer.getStoreIdAll();
            XxlJobHelper.log("====================== 竞拍异步任务开始执行 ======================");
            for (int storeId : storeIdList)
            {
                params = new HashMap<>(16);
                params.put("storeId", storeId);
                //httpApiUtils.executeHttpApi(ApiKey.EXECUTE_API_MAIN, params, MediaType.MULTIPART_FORM_DATA_VALUE)
                resultMap = publicAuctionService.execute(storeId);
                XxlJobHelper.log(resultMap.get("logo").toString());
            }
            XxlJobHelper.log("====================== 竞拍异步任务 执行完毕! ======================");
            XxlJobHelper.handleSuccess("竞拍任务执行成功");
        }
        catch (Exception e)
        {
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail("竞拍任务 异常: " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC);
        }
        finally
        {
            params = null;
            storeIdList = null;
            resultMap = null;
        }
    }
}

