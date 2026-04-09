package com.laiketui.plugins.diy.dubbo;

import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.ConfigModelMapper;
import com.laiketui.common.mapper.DiyModelMapper;
import com.laiketui.common.mapper.DiyPageBindModelMapper;
import com.laiketui.common.mapper.JumpPathModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.plugin.DiyModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyVo;
import com.laiketui.plugins.api.diy.PluginsDiyMchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: liuao
 * @Date: 2025-06-18-13:56
 * @Description:
 */
@Service
public class PluginsDiyMchServiceImpl implements PluginsDiyMchService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DiyModelMapper diyModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private DiyPageBindModelMapper diyPageBindModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private JumpPathModelMapper jumpPathModelMapper;


    @Override
    public Map<String, Object> getDiyList(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User userCache = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            DiyModel diyModel = new DiyModel();
            diyModel.setMch_id(userCache.getMchId());
            diyModel.setStore_id(vo.getStoreId());
            diyModel.setIs_del(DictionaryConst.WhetherMaven.WHETHER_NO);
            if (!Objects.isNull(id))
            {
                diyModel.setId(id);
            }
            List<DiyModel> select = diyModelMapper.select(diyModel);
            if (select.size() > 0)
            {
                resultMap.put("total", select.size());
                resultMap.put("list", select);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取diy模板首页列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "actGoodsMove");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String,Object> addOrUpdateDiy(DiyVo vo) throws LaiKeAPIException
    {
        try
        {
            User userCache = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Integer mchId = userCache.getMchId();
            vo.setMchId(mchId);
            return publiceService.addOrUpdateDiy(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "actGoodsMove");
        }
    }

    @Override
    public void diyStatus(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            User userCache = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publiceService.diyStatus(vo,id,userCache.getMchId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置diy模板 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDiy");
        }
    }

    @Override
    public void delDiy(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publiceService.delDiy(vo,id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除diy模板 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delDiy");
        }
    }


    @Override
    public Map<String, Object> index(MainVo vo) {
        Map<String, Object> result = new HashMap<>();
        try {
            String h5Config = "";
            User userCache = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //获取h5配置信息
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null) {
                h5Config = org.apache.commons.lang3.StringUtils.join(configModel.getH5_domain(), "/pagesB/store/store?shop_id=", userCache.getMchId());
            }
            result.put("H5_domain", h5Config);
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return result;
    }

    @Override
    public Map<String, Object> bannerPathList(MainVo vo, Integer type, String lang_code) throws LaiKeAPIException {
        Map<String, Object> result = new HashMap<>();
        try {
            User userCache = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Integer mchId = userCache.getMchId();
            lang_code = StringUtils.isEmpty(lang_code) ? "zh_CN" : lang_code;
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("type0", type);
            parmaMap.put("status", 1);
            parmaMap.put("type", 1);
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("mch_id", mchId);
            parmaMap.put("lang_code", lang_code);
            List<Map<String, Object>> list = jumpPathModelMapper.selectDynamic(parmaMap);
            result.put("list", list);
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerPathList");
        }
        return result;
    }
}
