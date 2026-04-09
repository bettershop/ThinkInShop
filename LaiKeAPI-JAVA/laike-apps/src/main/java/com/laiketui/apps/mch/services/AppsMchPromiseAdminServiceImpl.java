package com.laiketui.apps.mch.services;

import com.laiketui.apps.api.mch.AppsMchPromiseAdminService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.MchPromiseModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 店铺保证金实现
 *
 * @author Trick
 * @date 2021/10/25 16:43
 */
@Service
public class AppsMchPromiseAdminServiceImpl implements AppsMchPromiseAdminService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MchPromiseModelMapper mchPromiseModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> index(MainVo vo, String keyName) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            if (StringUtils.isNotEmpty(keyName))
            {
                parmaMap.put("keyName", keyName);
            }
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            int                       total       = mchPromiseModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> promiseList = new ArrayList<>();
            if (total > 0)
            {
                promiseList = mchPromiseModelMapper.selectDynamic(parmaMap);
                for (Map<String, Object> map : promiseList)
                {
                    String logoUrl = MapUtils.getString(map, "logo");
                    String headImg = MapUtils.getString(map, "head_img");
                    map.put("headimgurl", publiceService.getImgPath(headImg, vo.getStoreId()));
                    map.put("logo", publiceService.getImgPath(logoUrl, vo.getStoreId()));
                }
            }

            resultMap.put("list", promiseList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保证金记录列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> PromisePriceIndex(MainVo vo, String mchName, String status, String startTime, String endTime) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            if (StringUtils.isNotEmpty(mchName))
            {
                parmaMap.put("mchName", mchName);
            }
            if (StringUtils.isNotEmpty(status))
            {
                parmaMap.put("status", status);
            }
            if (StringUtils.isNotEmpty(startTime))
            {
                parmaMap.put("startTime", startTime);
            }
            if (StringUtils.isNotEmpty(endTime))
            {
                parmaMap.put("endTime", endTime);
            }
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            int                       total       = mchPromiseModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> promiseList = new ArrayList<>();
            if (total > 0)
            {
                System.out.println("开始");
                promiseList = mchPromiseModelMapper.selectDynamic(parmaMap);
                for (Map<String, Object> map : promiseList)
                {
                    String logoUrl = MapUtils.getString(map, "logo");
                    map.put("logo", publiceService.getImgPath(logoUrl, vo.getStoreId()));
                }
            }

            resultMap.put("list", promiseList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保证金审核记录列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }
}

