package com.laiketui.apps.app.services;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.laiketui.apps.api.app.services.AppsCstrFootprintService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.ProductListModelMapper;
import com.laiketui.common.mapper.UserFootprintModelMapper;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserFootprintModel;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 我的历史记录
 * 遗弃
 *
 * @author Trick
 * @date 2020/10/30 15:54
 */
@Service
public class AppsCstrFootprintServiceImpl implements AppsCstrFootprintService
{

    private Logger logger = LoggerFactory.getLogger(AppsCstrFootprintServiceImpl.class);

    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = null;
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                Object userObj = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + vo.getAccessId());
                if (userObj != null)
                {
                    user = JSON.parseObject(userObj.toString(), User.class);
                    //获取今天日期范围
                    String startToDayDate = DateUtil.getStartOfDay(new Date());
                    String endToDayDate   = DateUtil.getEndOfDay(new Date());
                    String time           = FastDateFormat.getInstance(GloabConst.TimePattern.YMD_CN).format(new Date());

                    List<Map<String, Object>> goodsInfoList = new ArrayList<>();

                    //获取用户今天的足迹
                    Map<String, Object> parmaMap = new HashMap<>(16);
                    parmaMap.put("startToDayDate", startToDayDate);
                    parmaMap.put("endToDayDate", endToDayDate);
                    List<UserFootprintModel> userFootprintModelList = userFootprintModelMapper.getFootprintByDate(parmaMap);
                    for (UserFootprintModel userFootprint : userFootprintModelList)
                    {
                        //获取商品信息
                        parmaMap.clear();
                        parmaMap.put("store_id", userFootprint.getStore_id());
                        parmaMap.put("pid", userFootprint.getP_id());
                        List<Map<String, Object>> goodsList = productListModelMapper.getProductList(parmaMap);
                        for (Map<String, Object> goods : goodsList)
                        {
                            Map<String, Object> dataList    = new HashMap<>(16);
                            String              goodsImgUrl = goods.get("img").toString();
                            goodsImgUrl = publiceService.getImgPath(goodsImgUrl, userFootprint.getStore_id().toString());
                            dataList.put("imgurl", goodsImgUrl);
                            dataList.put("footprint_id", userFootprint.getId());
                            goodsInfoList.add(dataList);
                        }
                    }
                    resultMap.put("time", time);
                    resultMap.put("list", goodsInfoList);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取我的历史记录异常 " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }

        return resultMap;
    }

    @Override
    public boolean alldel(MainVo vo, int footprintId) throws LaiKeAPIException
    {
        try
        {

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除我的历史记录异常 " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "alldel");
        }
        return false;
    }

    @Autowired
    PubliceService publiceService;

    @Autowired
    ProductListModelMapper productListModelMapper;

    @Autowired
    UserFootprintModelMapper userFootprintModelMapper;

    @Autowired
    RedisUtil redisUtil;
}

