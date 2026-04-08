package com.laiketui.comps.task.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.PublicExpressService;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.comps.api.task.CompsTaskWxService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.xxl.job.core.context.XxlJobHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信定时任务实现类
 *
 * @author ganpeng
 * @date 2024/03/15
 */
@Service
public class CompsTaskWxServiceImpl implements CompsTaskWxService
{

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Autowired
    private PublicExpressService publicExpressService;

    @Override
    public void V3QueryBatchTransferOrder() throws LaiKeAPIException
    {
        List<Integer>       storeIdList = null;
        Map<String, Object> parmaMap    = new HashMap<>();
        try
        {
            XxlJobHelper.log("商家转账到零钱查询批次转账单结果 开始执行!");
            if (!redisUtil.hasKey(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST))
            {
                XxlJobHelper.log("商城id未缓存,任务停止,等待下次执行....");
                return;
            }
            String json = redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST).toString();
            storeIdList = JSON.parseObject(json, new TypeReference<List<Integer>>()
            {
            });
            for (Integer id : storeIdList)
            {
                XxlJobHelper.log("商城id{}，开始执行", id);
                parmaMap.put("storeId", id);
                try
                {
                    httpApiUtils.executeApi("app.v3.QueryBatchTransferOrder", JSON.toJSONString(parmaMap));
                }
                catch (Exception e)
                {
                    //失败
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log("商家转账到零钱查询批次转账单结果 异常: " + e.getMessage());
        }
        finally
        {
            storeIdList = null;
            XxlJobHelper.handleSuccess("商家转账到零钱查询批次转账单结果 执行结束!");
        }
    }

    @Override
    public void synchronizationWxDelivery() throws LaiKeAPIException
    {
        try
        {
            XxlJobHelper.log("同步微信发货信息录入的物流公司信息 开始执行!");
            publicExpressService.synchronizationWxDelivery();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log("同步微信发货信息录入的物流公司信息 异常: " + e.getMessage());
        }
        finally
        {
            XxlJobHelper.handleSuccess("同步微信发货信息录入的物流公司信息 执行结束!");
        }
    }


    @Override
    public void synchronizationWxOrderStatus() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("同步微信发货订单状态 开始执行!");
            if (!redisUtil.hasKey(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST))
            {
                XxlJobHelper.log("商城id未缓存,任务停止,等待下次执行....");
                return;
            }
            String json = redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST).toString();
            storeIdList = JSON.parseObject(json, new TypeReference<List<Integer>>()
            {
            });
            for (Integer id : storeIdList)
            {
                XxlJobHelper.log("商城id{}执行....", id);
                publicExpressService.synchronizationWxOrderStatus(id);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log("同步微信发货订单状态 异常: " + e.getMessage());
        }
        finally
        {
            XxlJobHelper.handleSuccess("同步微信发货订单状态 执行结束!");
        }
    }
}
