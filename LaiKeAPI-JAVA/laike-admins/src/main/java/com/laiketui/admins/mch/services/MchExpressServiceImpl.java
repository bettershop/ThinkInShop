package com.laiketui.admins.mch.services;

import com.laiketui.admins.api.mch.MchExpressService;
import com.laiketui.common.api.PublicExpressService;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddExpressSubtableVo;
import com.laiketui.domain.vo.config.GetExpressSubtableListVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 物流管理
 */
@Service
public class MchExpressServiceImpl implements MchExpressService
{
    private final Logger logger = LoggerFactory.getLogger(MchExpressServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PublicExpressService publicExpressService;

    @Override
    public void addAndUpdateExpressSubtable(AddExpressSubtableVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
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
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
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
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
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
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
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
    public Map<String, Object> getExpressInfo(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            return publicExpressService.getExpressInfoByMchId(vo, user.getMchId());
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
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
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
}
