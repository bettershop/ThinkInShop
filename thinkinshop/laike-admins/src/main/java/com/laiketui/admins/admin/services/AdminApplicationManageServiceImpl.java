package com.laiketui.admins.admin.services;

import com.laiketui.admins.api.admin.AdminApplicationManageService;
import com.laiketui.common.mapper.EditionModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.EditionModel;
import com.laiketui.domain.vo.app.AppConfigInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * app管理
 *
 * @author Trick
 * @date 2021/1/22 9:39
 */
@Service
public class AdminApplicationManageServiceImpl implements AdminApplicationManageService
{
    private final Logger logger = LoggerFactory.getLogger(AdminApplicationManageServiceImpl.class);

    @Override
    public Map<String, Object> getVersionConfigInfo(int storeId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            EditionModel editionModel = new EditionModel();
            editionModel.setStore_id(storeId);
            editionModel = editionModelMapper.selectOne(editionModel);
            if (editionModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BBPZXXBCZ, "版本配置信息不存在");
            }
            resultMap.put("data", editionModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取版本配置信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getVersionConfigInfo");
        }
        return resultMap;
    }

    @Override
    public boolean addVersionConfigInfo(AppConfigInfoVo vo) throws LaiKeAPIException
    {
        try
        {
            int count;
            if (StringUtils.isEmpty(vo.getAppName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MCBNWK, "app名称不能为空");
            }
            if (StringUtils.isEmpty(vo.getAppVersion()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BBHBNWK, "版本号不能为空");
            }
            else if (!StringUtils.isInteger(vo.getAppVersion()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BBHBXWZZS, "版本号必须为正整数");
            }
            if (StringUtils.isEmpty(vo.getAndroidDownloadUrl()) || StringUtils.isEmpty(vo.getIosDownloadUrl()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XZDZBNWK, "下载地址不能为空");
            }
            if (vo.getIsAutoUpdate() == null || vo.getIsAutoUpdate() < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SFZDGXBNWK, "是否自动更新不能为空");
            }
            if (StringUtils.isEmpty(vo.getUpdateLogger()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GXNRBNWK, "更新内容不能为空");
            }

            EditionModel editionModelOld = new EditionModel();
            editionModelOld.setStore_id(vo.getStoreId());
            editionModelOld = editionModelMapper.selectOne(editionModelOld);

            EditionModel editionModelSave = new EditionModel();
            editionModelSave.setEdition_url("");
            editionModelSave.setAppname(vo.getAppName());
            editionModelSave.setType(vo.getIsAutoUpdate());
            editionModelSave.setEdition(vo.getAppVersion());
            editionModelSave.setContent(vo.getUpdateLogger());
            editionModelSave.setIos_url(vo.getIosDownloadUrl());
            editionModelSave.setAndroid_url(vo.getAndroidDownloadUrl());

            if (editionModelOld != null)
            {
                if (Integer.parseInt(vo.getAppVersion()) < Integer.parseInt(editionModelOld.getEdition()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DQBBHBNDYZQBBH, "当前版本号不能低于之前版本号");
                }
                editionModelSave.setId(editionModelOld.getId());
                count = editionModelMapper.updateByPrimaryKeySelective(editionModelSave);
            }
            else
            {
                editionModelSave.setStore_id(vo.getStoreId());
                editionModelSave.setAdd_date(new Date());
                count = editionModelMapper.insertSelective(editionModelSave);
            }

            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("添加/编辑版本配置 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addVersionConfigInfo");
        }
    }


    @Autowired
    private EditionModelMapper editionModelMapper;


}

