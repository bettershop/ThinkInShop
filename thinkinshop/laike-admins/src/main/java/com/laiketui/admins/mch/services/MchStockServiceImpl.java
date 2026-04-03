package com.laiketui.admins.mch.services;

import com.laiketui.admins.api.mch.MchStockService;
import com.laiketui.common.api.PublicStockService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.ConfiGureModelMapper;
import com.laiketui.common.mapper.StockModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataSerializeHelp;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.AddStockVo;
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
 * 库存管理
 *
 * @author Trick
 * @date 2021/6/2 11:40
 */
@Service
public class MchStockServiceImpl implements MchStockService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Override
    public Map<String, Object> index(MainVo vo, String goodsName) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", user.getMchId());
            if (StringUtils.isNotEmpty(goodsName))
            {
                parmaMap.put("product_title_like", goodsName);
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            Integer                   total = stockModelMapper.stockInfoCountDynamic(parmaMap);
            List<Map<String, Object>> list  = stockModelMapper.getStorckInfoDynamic(parmaMap);
            for (Map<String, Object> map : list)
            {
                //图片处理
                String imgUrl = map.get("imgurl").toString();
                imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                map.put("imgurl", imgUrl);
                //属性处理
                String attribute = map.get("attribute").toString();
                attribute = DataSerializeHelp.getAttributeStr(attribute);
                map.put("specifications", attribute);
            }

            resultMap.put("list", list);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("库存管理 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> addStock(AddStockVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publicStockService.addGoodsStock(vo, user.getZhanghao());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加库存 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addStock");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getStockInfoById(MainVo vo, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User           user           = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(id);
            if (confiGureModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBCZ, "库存不存在");
            }
            resultMap.put("num", confiGureModel.getNum());
            resultMap.put("total_num", confiGureModel.getTotal_num());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取库存信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStockInfoById");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getStockDetail(MainVo vo, int id, int pid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("product_id", pid);
            parmaMap.put("attribute_id", id);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total = stockModelMapper.goodsStockInfoCountDynamic(parmaMap);
            List<Map<String, Object>> list  = stockModelMapper.getGoodsStorckInfoDynamic(parmaMap);
            for (Map<String, Object> map : list)
            {
                map.put("specifications", GoodsDataUtils.getProductSkuValue(map.get("attribute").toString()));
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取库存明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStockDetail");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> getStockWarningInfo(MainVo vo, Integer goodsId, String goodsName, String startDate, String endDate) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pid", goodsId);
            parmaMap.put("mch_id", user.getMchId());
            if (StringUtils.isNotEmpty(goodsName))
            {
                parmaMap.put("goodsName", goodsName);
            }
            if (StringUtils.isNotEmpty(startDate))
            {
                parmaMap.put("startDate", startDate);
            }
            if (StringUtils.isNotEmpty(endDate))
            {
                parmaMap.put("endDate", endDate);
            }
            parmaMap.put("warningDate_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("group_cid", "group_cid");
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total = confiGureModelMapper.countStockInfo(parmaMap);
            List<Map<String, Object>> list  = confiGureModelMapper.selectStockInfo(parmaMap);
            for (Map<String, Object> map : list)
            {
                map.put("specifications", GoodsDataUtils.getProductSkuValue(map.get("attribute").toString()));
                map.put("imgurl", publiceService.getImgPath(MapUtils.getString(map, "imgurl"), vo.getStoreId()));
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取库存列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStockWarningInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getStockInOutInfo(MainVo vo, String goodsName, String startDate, String endDate, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("stockType", type);
            if (StringUtils.isNotEmpty(goodsName))
            {
                parmaMap.put("product_title_like", goodsName);
            }
            if (StringUtils.isNotEmpty(startDate))
            {
                parmaMap.put("stockStartDate", startDate);
            }
            if (StringUtils.isNotEmpty(endDate))
            {
                parmaMap.put("stockEndDate", endDate);
            }
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("pageStart", vo.getPageNo());

            int                       total = stockModelMapper.goodsStockInfoCountDynamic(parmaMap);
            List<Map<String, Object>> list  = stockModelMapper.getGoodsStorckInfoDynamic(parmaMap);
            for (Map<String, Object> map : list)
            {
                map.put("specifications", GoodsDataUtils.getProductSkuValue(map.get("attribute").toString()));
                map.put("imgurl", publiceService.getImgPath(MapUtils.getString(map, "imgurl"), vo.getStoreId()));
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取库存 出/入库信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStockInOutInfo");
        }
        return resultMap;
    }
}

