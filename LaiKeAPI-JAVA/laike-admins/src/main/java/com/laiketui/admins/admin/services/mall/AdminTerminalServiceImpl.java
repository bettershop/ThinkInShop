package com.laiketui.admins.admin.services.mall;


import com.laiketui.admins.api.admin.terminal.AdminTerminalService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.ConfigModelMapper;
import com.laiketui.common.mapper.EditionModelMapper;
import com.laiketui.common.mapper.GuideModelMapper;
import com.laiketui.common.mapper.NoticeModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.config.EditionModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.order.NoticeModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.mall.SaveTerminalAppVo;
import com.laiketui.domain.vo.admin.mall.SaveTerminalWeiXinVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 终端管理
 *
 * @author Trick
 * @date 2021/7/23 9:32
 */
@Service
public class AdminTerminalServiceImpl implements AdminTerminalService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private EditionModelMapper editionModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private GuideModelMapper guideModelMapper;

    @Autowired
    private NoticeModelMapper noticeModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> index(MainVo vo, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (DictionaryConst.StoreSource.LKT_LY_002.equals(type + ""))
            {
                resultMap.put("appInfo", getAppPage(vo.getStoreId()));
            }
            else
            {
                resultMap.put("weiXinInfo", getWeiXinPage(vo.getStoreId()));
            }

            //获取app域名
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                //安卓下载链接
                resultMap.put("android_download_link",configModel.getAndroidDownloadLink());
                //苹果下载链接
                resultMap.put("ios_download_link",configModel.getIosDownloadLink());
                resultMap.put("edition", configModel.getApp_domain_name());

                if (!DictionaryConst.StoreSource.LKT_LY_002.equals(type + ""))
                {
                    resultMap.put("appSecret", configModel.getAppsecret());
                    resultMap.put("appId", configModel.getAppid());
                    resultMap.put("Hide_your_wallet", configModel.getHide_your_wallet());
                    if (DictionaryConst.StoreSource.LKT_LY_003.equals(type + ""))
                    {
                        resultMap.put("appLogo", configModel.getHtml_icon());
                    }
                    else
                    {
                        resultMap.put("appLogo", configModel.getApp_logo());
                    }
                    resultMap.put("appTitle", configModel.getApp_title());

                }
            }
            //获取引导图
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total     = guideModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> guideList = guideModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : guideList)
            {
                map.put("image", publiceService.getImgPath(MapUtils.getString(map, "image"), vo.getStoreId()));
            }
            resultMap.put("guide_total", total);
            resultMap.put("guide_list", guideList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("终端管理界面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApp(SaveTerminalAppVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //保存安卓、苹果下载链接
            configModelMapper.updateDownloadLink(vo.getAndroidDownloadLink(),vo.getIosDownloadLink(),vo.getStoreId());
            int        row;
            if (StringUtils.isNotEmpty(vo.getAppDomainName()))
            {
                row = configModelMapper.updateConfigDomain(vo.getAppDomainName(), vo.getStoreId());
            }
            EditionModel editionOld = null;
            if (vo.getId() != null)
            {
                editionOld = editionModelMapper.selectByPrimaryKey(vo.getId());
            }
            EditionModel editionSave = new EditionModel();
            editionSave.setAppname(vo.getAppName());
            editionSave.setEdition(vo.getEdition());
            editionSave.setAndroid_url(vo.getAndroidUrl());
            editionSave.setIos_url(vo.getIosUrl());
            editionSave.setType(vo.getType());
            editionSave.setContent(vo.getContent());
            editionSave.setAdd_date(new Date());


            if (editionOld != null)
            {
                editionSave.setId(editionOld.getId());
                row = editionModelMapper.updateByPrimaryKeySelective(editionSave);
            }
            else
            {
                editionSave.setStore_id(vo.getStoreId());
                row = editionModelMapper.insertSelective(editionSave);
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了APP的配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保存终端配置(APP) 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveApp");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWeiXin(SaveTerminalWeiXinVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel  user      = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ConfigModel configOld = new ConfigModel();
            configOld.setStore_id(vo.getStoreId());
            configOld = configModelMapper.selectOne(configOld);
            if (configOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZBCZ, "配置不存在");
            }
            if (StringUtils.isEmpty(vo.getAppSecret()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSZBTX, "参数错误");
            }
            else if (vo.getAppSecret().length() > 32)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSZBTX, "小程序密钥为32位");
            }
            if (StringUtils.isEmpty(vo.getAppTitle()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSZBTX, "参数错误 小程序首页标题不能为空");
            }
            if (StringUtils.isEmpty(vo.getAppLogo()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSZBTX, "参数错误 小程序授权登录logo不能为空");
            }
            ConfigModel configSave = new ConfigModel();
            configSave.setId(configOld.getId());
            configSave.setAppid(vo.getAppId());
            configSave.setAppsecret(vo.getAppSecret());
            configSave.setHide_your_wallet(vo.getHideWallet());
            configSave.setApp_title(vo.getAppTitle());
            configSave.setApp_logo(vo.getAppLogo());
            configSave.setModify_date(new Date());
            int row = configModelMapper.updateByPrimaryKeySelective(configSave);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGPZSB, "修改配置失败");
            }
            NoticeModel noticeSave = new NoticeModel();
            noticeSave.setStore_id(vo.getStoreId());
            noticeSave.setPay_success(vo.getPaySuccess());
            noticeSave.setDelivery(vo.getDelivery());
            noticeSave.setRefund_res(vo.getRefundRes());

            NoticeModel noticeOld = new NoticeModel();
            noticeOld.setStore_id(vo.getStoreId());
            noticeOld = noticeModelMapper.selectOne(noticeOld);
            if (noticeOld == null)
            {
                row = noticeModelMapper.insertSelective(noticeSave);
            }
            else
            {
                noticeSave.setId(noticeOld.getId());
                noticeSave.setUpdate_time(new Date());
                row = noticeModelMapper.updateByPrimaryKeySelective(noticeSave);
            }

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了微信小程序的序配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保存小程序配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveWeiXin");
        }
    }

    private EditionModel getAppPage(int storeId) throws LaiKeAPIException
    {
        EditionModel editionModel = new EditionModel();
        editionModel.setStore_id(storeId);
        try
        {
            //获取商城版本配置信息
            List<EditionModel> editionModelList = editionModelMapper.select(editionModel);
            for (EditionModel edition : editionModelList)
            {
                return edition;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取app页面数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAppPage");
        }
        return editionModel;
    }

    private NoticeModel getWeiXinPage(int storeId) throws LaiKeAPIException
    {
        NoticeModel noticeModel = new NoticeModel();
        noticeModel.setStore_id(storeId);
        try
        {
            //获取商城版本配置信息
            List<NoticeModel> noticeModelList = noticeModelMapper.select(noticeModel);
            for (NoticeModel notice : noticeModelList)
            {
                return notice;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取微信配置页面数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getWeiXinPage");
        }
        return noticeModel;
    }

}
