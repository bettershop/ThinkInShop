package com.laiketui.admins.admin.services.saas;

import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.saas.AdminCurrencyStoreService;
import com.laiketui.common.mapper.CurrencyModelMapper;
import com.laiketui.common.mapper.CurrencyStoreModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.CurrencyStoreModel;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("adminCurrencyStoreService")
@Slf4j
public class AdminCurrencyStoreServiceImpl implements AdminCurrencyStoreService
{

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Autowired
    private CurrencyModelMapper currencyModelMapper;

    @Override
    public Map<String, Object> index(CurrencyStoreVo vo) throws LaiKeAPIException
    {
        log.info("index入参信息", JSON.toJSONString(vo));
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int                       total = currencyStoreModelMapper.countCurrencyStore(vo);
            List<Map<String, Object>> list  = currencyStoreModelMapper.queryCurrencyStoreList(vo);
            resultMap.put("list", list);
            resultMap.put("total", total);
            resultMap.put("haveDefaultCurrency", currencyStoreModelMapper.getDefaultCurrency(vo.getStoreId()));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            log.error("获取商城币种异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        log.info("index出参信息", JSON.toJSONString(resultMap));
        return resultMap;
    }

    @Transactional
    @Override
    public void addCurrencyStore(CurrencyStoreModel currencyStoreModel) throws LaiKeAPIException
    {
        log.info("addCurrency入参信息", JSON.toJSONString(currencyStoreModel));
        try
        {
            CurrencyStoreModel querycurrencyStoreModel = new CurrencyStoreModel();
            querycurrencyStoreModel.setCurrency_id(currencyStoreModel.getCurrency_id());
            querycurrencyStoreModel.setStore_id(currencyStoreModel.getStore_id());
            querycurrencyStoreModel = currencyStoreModelMapper.selectOne(querycurrencyStoreModel);
            if (querycurrencyStoreModel != null)
            {
                if (querycurrencyStoreModel.getDefault_currency() == CurrencyStoreModel.DefaultCurrency.YES)
                {
                    currencyStoreModel.setExchange_rate(BigDecimal.ONE);
                }
                currencyStoreModelMapper.updateCurrencyExchangerate(currencyStoreModel.getCurrency_id(), currencyStoreModel.getStore_id(), currencyStoreModel.getExchange_rate());
            }
            else
            {
                currencyStoreModelMapper.insertSelective(currencyStoreModel);
            }

        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
    }

    @Override
    public void delCurrencyStore(CurrencyStoreVo vo) throws LaiKeAPIException
    {
        try
        {
            log.info("delCurrency入参", JSON.toJSONString(vo));
            CurrencyStoreModel currencyStoreModel = new CurrencyStoreModel();
            currencyStoreModel.setCurrency_id(vo.getCurrency_id());
            currencyStoreModel.setStore_id(vo.getStoreId());
            currencyStoreModelMapper.delete(currencyStoreModel);
        }
        catch (Exception e)
        {
            log.error("操作失败，{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "delCurrencyStore");
        }
    }

    @Override
    public Map getStoreDefaultCurrency(int storeId) throws LaiKeAPIException
    {
        Map result = new HashMap();
        try
        {
            log.info("getStoreDefaultCurrency入参", storeId);
            return currencyStoreModelMapper.getDefaultCurrency(storeId);
        }
        catch (Exception e)
        {
            log.error("操作失败，{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "getStoreDefaultCurrency");
        }
    }

    @Transactional
    @Override
    public Map setDefaultCurrency(CurrencyStoreModel currencyStoreModel,boolean isCheck) throws LaiKeAPIException
    {
        log.info("addCurrency入参信息:{}", JSON.toJSONString(currencyStoreModel));
        try
        {
            if (isCheck)
            {
                //是否有默认货币
                Map defaultCurrencyMap = currencyStoreModelMapper.getDefaultCurrency(currencyStoreModel.getStore_id());
                if (defaultCurrencyMap != null)
                {
                    log.error("当前货币:{}为商城默认货币，无需设置。", currencyStoreModel.getCurrency_id());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCYSZMRHB, "商城运行过程中不允许修改默认货币", "setDefaultCurrency");
                }
            }
            currencyStoreModelMapper.updateDefaultCurrency(currencyStoreModel.getCurrency_id(), currencyStoreModel.getStore_id(), CurrencyStoreModel.DefaultCurrency.YES, BigDecimal.ONE);
            return currencyStoreModelMapper.getDefaultCurrency(currencyStoreModel.getStore_id());
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
    }

}
