package com.laiketui.admins.admin.services.saas;

import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.saas.AdminCountryServcie;
import com.laiketui.common.mapper.CountryModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.vo.saas.CountryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("adminCountryServcie")
@Slf4j
public class AdminCountryServcieImpl implements AdminCountryServcie
{

    @Autowired
    private CountryModelMapper countryModelMapper;

    @Override
    public Map<String, Object> countryList(CountryVo vo) throws LaiKeAPIException
    {
        log.info("countryList入参信息", JSON.toJSONString(vo));
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int                total = countryModelMapper.countCountry(vo);
            List<CountryModel> list  = countryModelMapper.queryCountryList(vo);
            resultMap.put("list", list);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            log.error("获取国际信息列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        log.info("countryList出参信息", JSON.toJSONString(resultMap));
        return resultMap;
    }

    @Override
    public void saveOrEditCountry(CountryModel countryModel) throws LaiKeAPIException
    {
        log.info("saveOrEditCountry入参信息", JSON.toJSONString(countryModel));
        try
        {
            if (countryModel.getId() != null)
            {
                countryModelMapper.updateByPrimaryKeySelective(countryModel);
            }
            else
            {
                CountryModel queryCountryModel = new CountryModel();
                queryCountryModel.setNum3(countryModel.getNum3());
                queryCountryModel = countryModelMapper.selectOne(queryCountryModel);
                if (queryCountryModel != null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GJYCZ, "国家已存在", "saveOrEditCountry");
                }
                countryModelMapper.insertSelective(countryModel);
            }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
    }

    @Override
    public void deleteCountry(CountryVo vo) throws LaiKeAPIException
    {
        try
        {
            log.info("deleteCountry入参信息", JSON.toJSONString(vo));
            CountryModel countryModel = new CountryModel();
            String       ids          = vo.getIds();

            if (StringUtils.isEmpty(ids) && (vo.getId() == null))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "deleteCountry");
            }

            if (StringUtils.isNotEmpty(ids))
            {
                for (String id : ids.split(","))
                {
                    if (StringUtils.isNotEmpty(id))
                    {
                        countryModel.setId(Short.parseShort(id));
                        countryModelMapper.delete(countryModel);
                    }
                }
            }

            if (vo.getId() != null)
            {
                countryModel.setId(vo.getId());
                countryModelMapper.delete(countryModel);
            }
        }
        catch (Exception e)
        {
            log.error("操作失败，{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "deleteCountry");
        }
    }

}