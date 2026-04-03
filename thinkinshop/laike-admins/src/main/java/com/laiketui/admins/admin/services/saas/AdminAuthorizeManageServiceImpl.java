package com.laiketui.admins.admin.services.saas;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.admins.api.admin.saas.AdminAuthorizeManageService;
import com.laiketui.common.api.third.PublicThirdService;
import com.laiketui.common.mapper.ConfigModelMapper;
import com.laiketui.common.mapper.ThirdMiniInfoModelMapper;
import com.laiketui.common.mapper.ThirdModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.HttpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.home.ThirdModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.weixin.AddThridVo;
import com.laiketui.domain.weixin.ThirdMiniInfoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 授权管理
 *
 * @author Trick
 * @date 2021/2/3 16:23
 */
@Service
public class AdminAuthorizeManageServiceImpl implements AdminAuthorizeManageService
{
    private final Logger logger = LoggerFactory.getLogger(AdminAuthorizeManageServiceImpl.class);

    @Autowired
    private ThirdMiniInfoModelMapper thirdMiniInfoModelMapper;

    @Autowired
    private PublicThirdService publicThirdService;

    @Autowired
    private ThirdModelMapper thirdModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Override
    public Map<String, Object> getThirdInfo(Integer examineStatus, Integer releaseStatus, String appName, PageModel pageModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            if (StringUtils.isEmpty(appName))
            {
                parmaMap.put("nick_name", appName);
            }
            if (examineStatus != null)
            {
                parmaMap.put("review_mark", examineStatus);
            }
            if (releaseStatus != null)
            {
                parmaMap.put("issue_mark", releaseStatus);
            }

            parmaMap.put("pageStart", pageModel.getPageNo());
            parmaMap.put("pageEnd", pageModel.getPageSize());

            int                       total = thirdMiniInfoModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = thirdMiniInfoModelMapper.selectDynamic(parmaMap);

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取小程序发布列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getThirdInfo");
        }
        return resultMap;
    }

    @Override
    public boolean release(Integer id) throws LaiKeAPIException
    {
        try
        {
            ThirdMiniInfoModel thirdMiniInfoModel = thirdMiniInfoModelMapper.selectByPrimaryKey(id);
            if (thirdMiniInfoModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XCXBCZ, "小程序不存在");
            }
            //获取token
            String token = publicThirdService.authorizerAccessToken(thirdMiniInfoModel.getStore_id());
            //设置业务域名
            if (publicThirdService.setServeDomain(thirdMiniInfoModel.getStore_id(), token))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SZYWYMSB, "设置业务域名失败");
            }

            ThirdMiniInfoModel thirdMiniInfoModelUpdate = new ThirdMiniInfoModel();
            thirdMiniInfoModelUpdate.setId(thirdMiniInfoModel.getId());

            String url        = String.format(GloabConst.WeiXinUrl.RELEASE_APP_POST, token);
            String resultJson = HttpUtils.post(url);
            Map<String, String> resultJsonMap = JSON.parseObject(resultJson, new TypeReference<Map<String, String>>()
            {
            });
            if ("0".equals(resultJsonMap.get("errcode")))
            {
                thirdMiniInfoModelUpdate.setIssue_mark(ThirdMiniInfoModel.ISSUE_MARK_SUCCESS);
                return true;
            }
            else
            {
                thirdMiniInfoModelUpdate.setIssue_mark(ThirdMiniInfoModel.ISSUE_MARK_FAIL);
                thirdMiniInfoModelUpdate.setAuditid("");
                logger.debug("小程序发布失败:{}", resultJson);
            }
            return false;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("小程序发布 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "release");
        }
    }

    @Override
    public Map<String, Object> getThridParmate(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            ThirdModel  thirdModelSave = new ThirdModel();
            ThirdModel  thirdModel     = thirdModelMapper.selectOne(thirdModelSave);
            ConfigModel configModel    = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            thirdModel.setWork_domain(configModel.getH5_domain());
            resultMap.put("list", thirdModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取小程序配置参数信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getThridParmate");
        }
        return resultMap;
    }

    @Override
    public boolean addThridParmate(AddThridVo vo) throws LaiKeAPIException
    {
        try
        {
            int count;
            if (StringUtils.isEmpty(vo.getAppid()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BNWK, "appid不能为空");
            }
            if (StringUtils.isEmpty(vo.getAppsecret()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MYBNWK, "密钥不能为空");
            }
            ThirdModel thirdModelOld = null;
            if (vo.getId() != null)
            {
                thirdModelOld = thirdModelMapper.selectByPrimaryKey(vo.getId());
            }
            ThirdModel thirdModelSave = new ThirdModel();
            thirdModelSave.setAppid(vo.getAppid());
            thirdModelSave.setAppsecret(vo.getAppsecret());
            thirdModelSave.setCheck_token(vo.getCheckToken());
            thirdModelSave.setEncrypt_key(vo.getEncryptKey());
            thirdModelSave.setServe_domain(vo.getServeDomain());
            thirdModelSave.setWork_domain(vo.getWorkDomain());
            thirdModelSave.setRedirect_url(vo.getRedirectUrl());
            thirdModelSave.setMini_url(vo.getMiniUrl());
            thirdModelSave.setQr_code(vo.getQrCode());
            thirdModelSave.setEndurl(vo.getEndurl());

            if (thirdModelOld != null)
            {
                thirdModelSave.setId(thirdModelOld.getId());
                count = thirdModelMapper.updateByPrimaryKeySelective(thirdModelSave);
            }
            else
            {
                count = thirdModelMapper.insertSelective(thirdModelSave);
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
            logger.error("添加/编辑小程序配置参数 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addThridParmate");
        }
    }
}

