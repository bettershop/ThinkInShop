package com.laiketui.apps.app.services;

import com.laiketui.apps.api.app.AppsCstrPluginsService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.LivingConfigModelMapper;
import com.laiketui.common.mapper.PluginsModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.PluginsModel;
import com.laiketui.domain.living.LivingConfigModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.PluginsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 插件实现
 *
 * @author sunH_
 * @date 2022/01/11 11:23
 */
@Service
public class AppsCstrPluginsServiceImpl implements AppsCstrPluginsService
{

    private final Logger logger = LoggerFactory.getLogger(AppsCstrPluginsServiceImpl.class);

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private LivingConfigModelMapper livingConfigModelMapper;

    @Override
    public Map<String, Object> getPluginInfo(MainVo vo, String pluginCode) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            PluginsModel pluginInfo = pluginsModelMapper.getPluginInfo(pluginCode, vo.getStoreId());
            if (!Objects.isNull(pluginInfo))
            {
                resultMap.put("pluginInfo", pluginInfo);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getPluginInfo");
        }

        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(PluginsVo vo) throws LaiKeAPIException
    {
        try
        {
            boolean      isUpdate     = false;
            PluginsModel pluginsModel = new PluginsModel();
            if (!Objects.isNull(vo.getId()))
            {
                pluginsModel.setId(vo.getId());
                pluginsModel = pluginsModelMapper.selectOne(pluginsModel);
                if (Objects.isNull(pluginsModel))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CJXXBCZ, "插件信息不存在", "update");
                }
                isUpdate = true;
            }
            pluginsModel.setPlugin_name(vo.getPlugin_name());
            pluginsModel.setPlugin_code(vo.getPlugin_code());
            pluginsModel.setPlugin_img(vo.getPlugin_img());
            pluginsModel.setOptime(new Date());
            pluginsModel.setStatus(vo.getStatus());
            pluginsModel.setFlag(vo.getFlag());
            pluginsModel.setJump_address(vo.getJump_address());
            if (isUpdate)
            {
                pluginsModelMapper.updateByPrimaryKey(pluginsModel);
            }
            else
            {
                pluginsModelMapper.insertSelective(pluginsModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "update");
        }
    }


    @Override
    public Map<String, Object> getPluginAll(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            // todo 未分商城
            PluginsModel pluginsModel = new PluginsModel();
            pluginsModel.setStore_id(vo.getStoreId());
            //获取所有插件
            List<PluginsModel> pluginsModelsList = pluginsModelMapper.select(pluginsModel);
            //禅道
            for (PluginsModel plugin : pluginsModelsList)
            {
                resultMap.put(plugin.getPlugin_code(), plugin.getStatus() == 1);
            }
            //判断店铺插件是否开启
            LivingConfigModel livingConfigModel = new LivingConfigModel();
            livingConfigModel.setStore_id(vo.getStoreId());
            livingConfigModel.setRecycle(0);
            livingConfigModel = livingConfigModelMapper.selectOne(livingConfigModel);

            if (StringUtils.isEmpty(livingConfigModel))
            {
                resultMap.put("mch_is_open", 0);
            }
            else
            {
                resultMap.put("mch_is_open", livingConfigModel.getMch_is_open());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取所有插件-开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getPluginAll");
        }
        return resultMap;
    }


}

