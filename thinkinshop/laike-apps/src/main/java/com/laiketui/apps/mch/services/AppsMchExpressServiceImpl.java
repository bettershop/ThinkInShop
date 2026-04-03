package com.laiketui.apps.mch.services;


import com.laiketui.apps.api.mch.AppsMchExpressService;
import com.laiketui.common.api.PublicExpressService;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddExpressSubtableVo;
import com.laiketui.domain.vo.config.GetExpressSubtableListVo;
import com.laiketui.domain.vo.order.GetExpressDeliveryListVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 物流管理
 */
@Service
public class AppsMchExpressServiceImpl implements AppsMchExpressService
{
    private final Logger logger = LoggerFactory.getLogger(AppsMchExpressServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PublicExpressService publicExpressService;

    @Autowired
    private PublicOrderService publicOrderService;

    @Override
    public void addAndUpdateExpressSubtable(AddExpressSubtableVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            vo.setMch_id(user.getMchId());
            publicExpressService.addAndUpdateExpressSubtable(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加修改快递公司子表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }

    @Override
    public Map<String, Object> getExpressSubtableList(GetExpressSubtableListVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            vo.setMch_id(user.getMchId());
            return publicExpressService.getExpressSubtableList(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取快递公司子表列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }


    @Override
    public Map<String, Object> getExpressSubtableById(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            return publicExpressService.getExpressSubtableById(vo, id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取快递公司子表详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }


    @Override
    public void delExpressSubtableById(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            publicExpressService.delExpressSubtableById(vo, id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除快递公司子表详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }

    @Override
    public Map<String, Object> getExpressInfo(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            return publicExpressService.getExpressInfo(vo, user.getMchId(), true, id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取所有物流公司信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }

    @Override
    public Map<String, Object> GetLogistics(MainVo vo, String sNo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            return publicExpressService.getExpressInfoBySNo(vo, sNo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取所有物流公司信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }

    @Override
    public Map<String, Object> ShippingRecords(GetExpressDeliveryListVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            vo.setMch_id(user.getMchId());
            resultMap = publicExpressService.getExpressDeliveryList(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单发货列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "ShippingRecords");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> expressGetPro(MainVo vo, Integer id, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            resultMap = publicExpressService.getGoodsByExpressDeliveryId(vo, id, name);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看发货记录商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "Override");
        }
        return resultMap;
    }

    @Override
    public String CancelElectronicWaybill(MainVo vo, Integer id) throws LaiKeAPIException
    {
        String msg;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            msg = publicOrderService.cancelElectronicSheetDelivery(vo, id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看发货记录商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "expressGetPro");
        }
        return msg;
    }

    @Override
    public void FaceSheetSend(MainVo vo, Integer exId, String orderDetailIds) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            publicOrderService.electronicSheetDelivery(vo, exId, orderDetailIds);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看发货记录商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "expressGetPro");
        }
    }

}
