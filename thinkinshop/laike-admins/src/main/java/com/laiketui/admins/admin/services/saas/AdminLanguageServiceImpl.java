package com.laiketui.admins.admin.services.saas;

import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.saas.AdminLanguageService;
import com.laiketui.common.mapper.LangModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.LangModel;
import com.laiketui.domain.vo.saas.LanguageVo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("adminLanguageService")
@Slf4j
public class AdminLanguageServiceImpl implements AdminLanguageService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LangModelMapper langModelMapper;

    @Override
    public Map<String, Object> index(LanguageVo vo) throws LaiKeAPIException
    {
        log.info("index入参信息", JSON.toJSONString(vo));
        logger.info("index入参信息", JSON.toJSONString(vo));
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int             total = langModelMapper.count(vo);
            List<LangModel> list  = langModelMapper.queryList(vo);
            resultMap.put("list", list);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取语种列表 异常", e);
            log.error("获取语种列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        log.info("index出参信息", JSON.toJSONString(resultMap));
        logger.info("index出参信息", JSON.toJSONString(resultMap));
        return resultMap;
    }

    @Override
    public void addLanguage(LangModel langModel) throws LaiKeAPIException
    {
        try
        {
            if (langModel.getId() != null)
            {
                langModelMapper.updateByPrimaryKeySelective(langModel);
            }
            else
            {
                LangModel queryLangModel = new LangModel();
                queryLangModel.setLang_code(langModel.getLang_code());
                queryLangModel = langModelMapper.selectOne(queryLangModel);
                if (queryLangModel != null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZYCZ, "语种已存在", "addLanguage");
                }
                langModelMapper.insertSelective(langModel);
            }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }

    }

    @Override
    public void delLanguage(Integer id) throws LaiKeAPIException
    {
        try
        {
            LangModel langModel = new LangModel();
            langModel.setId(id);
            langModelMapper.delete(langModel);
        }
        catch (Exception e)
        {
            logger.error("操作失败，{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "delLanguage");
        }
    }
}