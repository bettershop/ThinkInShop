package com.laiketui.apps.app.services;

import com.alibaba.druid.util.StringUtils;
import com.laiketui.apps.api.app.AppsCstrUrlService;
import com.laiketui.apps.app.common.consts.AppsCstrConst;
import com.laiketui.common.mapper.ConfigModelMapper;
import com.laiketui.common.mapper.ThirdModelMapper;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.home.ThirdModel;
import com.laiketui.domain.vo.MainVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * 用户接口实现
 *
 * @author Trick
 * @date 2020/9/23 9:22
 */
@Service
public class AppsCstrUrlServiceImpl implements AppsCstrUrlService
{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ThirdModelMapper thirdModelMapper;

    @Autowired
    ConfigModelMapper configModelMapper;

    @Autowired
    RedisUtil redisUtil;


    @Override
    public Map<String, Object> getUrl(@ParamsMapping("store_id") int storeId, String language, @ParamsMapping("access_id") String accessId, String get) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> cacheThirdModel = DataUtils.cast(redisUtil.get(GloabConst.RedisHeaderKey.LKT_URLMODAL));
            if (cacheThirdModel != null)
            {
                return resultMap;
            }
            ThirdModel thirdModel = new ThirdModel();
            thirdModel.setId(1);
            thirdModel = thirdModelMapper.selectOne(thirdModel);
            if (thirdModel != null)
            {
                if (!StringUtils.isEmpty(thirdModel.getKefu_url()))
                {
                    //如果客服地址存在则返回客服地址
                    String url = "&store_id=%s?&access_id=%s?&language=%s";
                    thirdModel.setKefu_url(String.format(url, storeId, accessId, language));
                }
                else if (!StringUtils.isEmpty(thirdModel.getMini_url()))
                {
                    //如果小程序接口地址存在，则返回小程序地址
                    thirdModel.setKefu_url("?store_id=" + storeId);
                }
                //判断参数中是否包含h5字符,包含则设置h5地址
                if (get.contains("H5"))
                {
                    ConfigModel configModel = new ConfigModel();
                    configModel.setStore_id(storeId);
                    configModel = configModelMapper.selectOne(configModel);
                    if (configModel != null && !StringUtils.isEmpty(configModel.getH5_domain()))
                    {
                        thirdModel.setH5(configModel.getH5_domain());
                    }
                }
                resultMap.put("mini_url", thirdModel.getMini_url());
                resultMap.put("H5", thirdModel.getH5());
                resultMap.put("endurl", thirdModel.getEndurl());
                //缓存起来
                redisUtil.set(GloabConst.RedisHeaderKey.LKT_URLMODAL, resultMap);
                return resultMap;
            }
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZYC, "操作异常", "getUrl");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUrl");
        }
    }

    @Override
    public Map<String, Object> getServerUrl(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String      serverUrl   = "";
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                serverUrl = configModel.getCustomer_service();
            }
            resultMap.put("kefu_url", serverUrl);
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getServerUrl");
        }
    }
}

