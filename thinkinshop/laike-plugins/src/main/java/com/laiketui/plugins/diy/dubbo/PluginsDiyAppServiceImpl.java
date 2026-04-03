package com.laiketui.plugins.diy.dubbo;

import com.laiketui.common.mapper.ConfigModelMapper;
import com.laiketui.common.mapper.DiyModelMapper;
import com.laiketui.common.mapper.DiyPageModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.diy.DiyPageModel;
import com.laiketui.domain.plugin.DiyModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.plugins.api.diy.PluginsDiyAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * diy移动端接口
 *
 * @author Trick
 * @date 2022/5/9 11:23
 */
@Service
public class PluginsDiyAppServiceImpl implements PluginsDiyAppService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DiyModelMapper diyModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private DiyPageModelMapper diyPageModelMapper;

    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {

            DiyModel diyModel = new DiyModel();
            diyModel.setStore_id(vo.getStoreId());
            diyModel.setIs_del(DictionaryConst.WhetherMaven.WHETHER_NO);
            diyModel.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);
            diyModel.setMch_id(0);
            diyModel = diyModelMapper.selectOne(diyModel);
            if (diyModel != null)
            {
                resultMap.put("data", diyModel.getValue());
                resultMap.put("tab_bar", diyModel.getTab_bar());
                resultMap.put("tabber_info", diyModel.getTabber_info());
            }

            //首页标题
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "请先配置商城");
            }
            resultMap.put("appTitle", configModel.getApp_title());

            // hideMchFlag = true的时候隐藏店铺跟小程序钱包一同隐藏
            boolean mpwechathidesthflag = configModel.getHide_your_wallet() == 1;
            resultMap.put("hideMchFlag", mpwechathidesthflag);
            resultMap.put("hideWalletFlag", mpwechathidesthflag);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("diy首页接口 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getPageInfoById(Integer id)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
       try
       {
           DiyPageModel pageModel = diyPageModelMapper.selectByPrimaryKey(id);
           if (Objects.nonNull(pageModel) && StringUtils.isNotEmpty(pageModel.getPage_context()))
           {
               resultMap.put("data", pageModel.getPage_context());
           }
       }
       catch (LaiKeAPIException l)
       {
           throw l;
       }
       catch (Exception e)
       {
           logger.error("获取页面数据失败", e);
           throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQYMSJSB, "获取页面数据失败", "getPageInfoById");
       }
       return resultMap;
    }
}

