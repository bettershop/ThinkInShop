package com.laiketui.admins.mch.services;

import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.mch.MchFreightService;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.mapper.FreightModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.freight.FreightRuleVO;
import com.laiketui.domain.vo.mch.AddFreihtVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运费管理
 *
 * @author Trick
 * @date 2021/6/8 10:20
 */
@Service
public class MchFreightServiceImpl implements MchFreightService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private FreightModelMapper freightModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Override
    public Map<String, Object> index(MainVo vo, Integer fid, Integer status) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", user.getMchId());
            if (status != null)
            {
                if (status.equals(IndexStatus.IN_USE))
                {
                    parmaMap.put("isNotNullId", "isNotNullId");
                }
                else if (status.equals(IndexStatus.NOT_USED))
                {
                    parmaMap.put("isNullId", "isNullId");
                }
            }
            parmaMap.put("is_default_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            parmaMap.put("lang_code", vo.getLang_code());

            int                       total       = freightModelMapper.countFreightInfoLeftGoodsDynamic(parmaMap);
            List<Map<String, Object>> freightList = freightModelMapper.getFreightInfoLeftGoodsDynamic(parmaMap);
            if (fid != null && fid > 0)
            {
                //获取明细
                for (Map<String, Object> map : freightList)
                {
                    //运费规则
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "freight")))
                    {
                        List<FreightRuleVO> freightRuleVOS = JSON.parseArray(MapUtils.getString(map, "freight"), FreightRuleVO.class);
//                    Map<Integer, LinkedHashMap<String, Object>> freight = SerializePhpUtils.getUnSerializeByFreight(MapUtils.getString(map, "freight"));
                        map.put("details", freightRuleVOS);
                    }
                }
            }

            resultMap.put("total", total);
            resultMap.put("list", freightList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("运费列表 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addFreight(AddFreihtVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            vo.setShopId(user.getMchId());
            if (!publicMchService.freightAdd(vo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YFTJSB, "运费添加失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑运费 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addFreight");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void freightDel(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publicMchService.freightDel(vo.getStoreId(), ids,user.getMchId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除运费 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightDel");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setDefault(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            User        user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            AddFreihtVo freihtVo = new AddFreihtVo();
            freihtVo.setStoreId(vo.getStoreId());
            freihtVo.setFid(id);
            freihtVo.setShopId(user.getMchId());
            publicMchService.setDefault(freihtVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置默认运费 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightDel");
        }
    }
}

