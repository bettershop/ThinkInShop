package com.laiketui.common.service.dubbo.plugin;

import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.plugin.PubliceIntegralService;
import com.laiketui.common.mapper.CustomerModelMapper;
import com.laiketui.common.mapper.IntegralGoodsModelMapper;
import com.laiketui.common.mapper.PluginsModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.domain.config.PluginsModel;
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
 * 公共积分商城
 *
 * @author Trick
 * @date 2021/2/23 10:14
 */
@Service
public class PubliceIntegralServiceImpl implements PubliceIntegralService
{
    private final Logger logger = LoggerFactory.getLogger(PubliceIntegralServiceImpl.class);

    @Autowired
    private IntegralGoodsModelMapper integralGoodsModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Override
    public List<Map<String, Object>> getIntegralGoodsInfo(int storeId, int pageStart, int pageEnd) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            Integer             zyMchId      = customerModelMapper.getStoreMchId(storeId);
            PluginsModel        pluginsModel = pluginsModelMapper.getPluginInfo("integral", storeId);
            Map<String, Object> parmaMap     = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("status", DictionaryConst.GoodsStatus.NEW_GROUNDING);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("page", pageStart);
            parmaMap.put("pageSize", pageEnd);
            //积分插件未开启--只显示自营店商品
            if (pluginsModel == null || pluginsModel.getStatus().equals(DictionaryConst.WhetherMaven.WHETHER_NO))
            {
                parmaMap.put("mchId", zyMchId);
            }
            resultList = integralGoodsModelMapper.getGoodsInfo(parmaMap);
            for (Map<String, Object> map : resultList)
            {
                map.put("imgurl", publiceService.getImgPath(MapUtils.getString(map, "imgurl"), storeId));
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取积分商城商品信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getIntegralGoodsInfo");
        }
        return resultList;
    }
}

