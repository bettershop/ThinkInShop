package com.laiketui.comps.task.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.mapper.OrderReportModelMapper;
import com.laiketui.comps.api.task.CompsTaskService;
import com.laiketui.comps.api.task.CompsTaskUserService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.report.OrderReportModel;
import com.xxl.job.core.context.XxlJobHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户定时任务实现类
 *
 * @author ganpeng
 * @date 2023/05/12 17:50
 */
@Service
public class CompsTaskUserServiceImpl implements CompsTaskUserService
{
    @Autowired
    private PublicUserService      publicUserService;
    @Autowired
    private RedisUtil              redisUtil;
    @Autowired
    private OrderReportModelMapper orderReportModelMapper;

    @Autowired
    private CompsTaskService taskService;

    /***
     * 定时缓存平台新增用户信息
     * @throws LaiKeAPIException
     * @author ganpeng
     * @date 2023/05/12 17:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void AdditionUserData() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("缓存平台新增用户信息 开始执行!");
            //获取所有商城id缓存
            if (redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST) != null)
            {
                storeIdList = JSON.parseObject(redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST).toString(), new TypeReference<List<Integer>>()
                {
                });
            }
            if (storeIdList == null || storeIdList.isEmpty())
            {
                XxlJobHelper.log("商城id缓存为空");
                storeIdList = taskService.getStoreIdAll();
            }
            storeIdList.forEach(storeId ->
            {
                OrderReportModel model = new OrderReportModel();
                orderReportModelMapper.deleteByType(storeId, 5);
                model.setType(5);
                model.setNum(0);
                model.setStoreId(storeId);
                model.setData(JSON.toJSONString(publicUserService.getAdditionUserData(storeId)));
                int insert = orderReportModelMapper.insert(model);
                if (insert < 1)
                {
                    XxlJobHelper.log("缓存平台新增用户信息失败 商城id:{}", storeId);
                }
                else
                {
                    XxlJobHelper.log("缓存平台新增用户信息完成 商城id:{}", storeId);
                }
            });

            XxlJobHelper.log("缓存平台新增用户信息 完成!");
        }
        catch (Exception e)
        {
            XxlJobHelper.log("缓存平台新增用户信息 异常: " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            storeIdList = null;
            XxlJobHelper.handleSuccess("缓存平台新增用户信息 开始执行!");
        }

    }
}
