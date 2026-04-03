package com.laiketui.apps.app.services;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.apps.api.app.AppsCstrLiveBroadcastService;
import com.laiketui.apps.app.common.consts.AppsCstrConst;
import com.laiketui.common.api.PubliceService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.HttpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.vo.MainVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 直播
 *
 * @author Trick
 * @date 2020/12/16 16:23
 */
@Service
public class AppsCstrLiveBroadcastServiceImpl implements AppsCstrLiveBroadcastService
{
    private final Logger logger = LoggerFactory.getLogger(AppsCstrLiveBroadcastServiceImpl.class);

    @Autowired
    private PubliceService publiceService;


    @Override
    public Map<String, Object> getLiveBroadcastList(MainVo vo, String roomId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String liveToken = publiceService.getWeiXinToken(vo.getStoreId());
            //获取直播列表
            String              url         = String.format(GloabConst.WeiXinUrl.LIVE_BROADCAST_LIST_ACCESSTOKEN_GET_URL, liveToken);
            Map<String, Object> apiParmaMap = new HashMap<>(16);
            apiParmaMap.put("start", vo.getPageNo());
            apiParmaMap.put("limit", vo.getPageSize());
            if (!StringUtils.isEmpty(roomId))
            {
                apiParmaMap.put("action", "get_replay");
                apiParmaMap.put("room_id", roomId);
            }
            String     json          = HttpUtils.post(url, JSON.toJSONString(apiParmaMap));
            JSONObject apiResultData = JSON.parseObject(json, JSONObject.class);
            if (GloabConst.ManaValue.MANA_VALUE_OK.equals(apiResultData.get("errmsg")))
            {
                resultMap.put("list", apiResultData);
            }
            else
            {
                resultMap.put("list", apiResultData);
                logger.debug("获取直播列表失败:{}", json);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WHQDSJ, "未获取到数据", "getLiveBroadcastList");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取直播列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC_001, "网路异常", "getLiveBroadcastList");
        }
        return resultMap;
    }

}

