package com.laiketui.comps.freight.services;

import com.laiketui.common.api.PublicFreightService;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.comps.api.freight.CompsMallFreightService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.mch.AddFreihtVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class CompsMallFreightServiceImp implements CompsMallFreightService
{

    private final Logger logger = LoggerFactory.getLogger(CompsMallFreightServiceImp.class);

    @Autowired
    private RedisUtil            redisUtil;
    @Autowired
    private PublicFreightService publicFreightService;

    @Override
    public Map<String, Object> getMallFreightInfo(MainVo vo, Integer mchId, Integer fid, Integer status, Integer otype, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN, true);

            if (user != null)
            {
                PageModel pageModel = new PageModel(vo.getPageNo(), vo.getPageSize(), vo.getPageNum());
                if (fid != null)
                {
                    //根据id获取
                    resultMap = publicFreightService.FreightAndModifyShow(vo.getStoreId(), mchId, fid);
                }
                else if (otype != null || status != null || !StringUtils.isEmpty(name))
                {
                    //根据条件获取模板列表
                    resultMap = publicFreightService.GetFreightList(vo.getStoreId(), mchId, name, status, otype, pageModel);
                }
                else
                {
                    //获取全部
                    resultMap = publicFreightService.GetFreightList(vo.getStoreId(), mchId, null, null, null, pageModel);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取运费信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getFreightInfo");
        }
        return resultMap;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addMallFreight(AddFreihtVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN, true);
            if (user != null)
            {
                vo.setShopId(user.getMchId());
                return publicFreightService.FreightToAdd(vo);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改运费信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addFreight");
        }
        return false;
    }

    @Override
    public void MallFreightSetDefault(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            User        user        = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN, true);
            AddFreihtVo addFreihtVo = new AddFreihtVo();
            addFreihtVo.setFid(id);
            addFreihtVo.setStoreId(vo.getStoreId());
            addFreihtVo.setShopId(user.getMchId());
            publicFreightService.setDefaultFreight(addFreihtVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("运费设置默认开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightSetDefault");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delMallFreight(MainVo vo, String freightIds) throws LaiKeAPIException
    {
        try
        {
            publicFreightService.FreightToDel(vo.getStoreId(), freightIds);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除运费信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addFreight");
        }
    }
}
