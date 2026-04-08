package com.laiketui.admins.admin.services.saas;

import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.saas.AdminCurrencyService;
import com.laiketui.common.mapper.CurrencyModelMapper;
import com.laiketui.common.mapper.CurrencyStoreModelMapper;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.CurrencyModel;
import com.laiketui.domain.CurrencyStoreModel;
import com.laiketui.domain.vo.saas.CurrencyVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("adminCurrencyService")
@Slf4j
public class AdminCurrencyServiceImpl implements AdminCurrencyService
{

    @Autowired
    private CurrencyModelMapper currencyModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreMapper;

    @Override
    public Map<String, Object> index(CurrencyVo vo) throws LaiKeAPIException
    {
        log.info("index入参信息", JSON.toJSONString(vo));
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int                 total = currencyModelMapper.countCurrency(vo);
            List<CurrencyModel> list  = currencyModelMapper.queryCurrencyList(vo);
            resultMap.put("list", list);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            log.error("获取币种异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        log.info("index出参信息", JSON.toJSONString(resultMap));
        return resultMap;
    }

    @Override
    public void addCurrency(CurrencyModel currencyModel) throws LaiKeAPIException
    {
        log.info("addCurrency入参信息", JSON.toJSONString(currencyModel));
        try
        {
            if (currencyModel.getId() != null)
            {
                currencyModelMapper.updateByPrimaryKeySelective(currencyModel);
            }
            else
            {
                CurrencyModel queryCurrencyModel = new CurrencyModel();
                queryCurrencyModel.setCurrency_code(currencyModel.getCurrency_code());
                queryCurrencyModel = currencyModelMapper.selectOne(queryCurrencyModel);
                if (queryCurrencyModel != null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BZYCZ, "币种已存在", "addCurrency");
                }
                currencyModelMapper.insertSelective(currencyModel);
            }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
    }

    @Override
    public String delCurrency(CurrencyVo vo) throws LaiKeAPIException
    {
        try
        {
            log.info("delCurrency入参: {}", JSON.toJSONString(vo));

            // 参数校验
            if ((StringUtils.isEmpty(vo.getIds())) && vo.getId() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "deleteCountry");
            }

            // 合并 ids 和 vo.getId() 生成 idList
            List<Integer> idList = new ArrayList<>();
            if (StringUtils.isNotEmpty(vo.getIds()))
            {
                idList.addAll(Arrays.stream(vo.getIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::valueOf)
                        .collect(Collectors.toList()));
            }
            if (vo.getId() != null)
            {
                idList.add(vo.getId());
            }

            // 记录不能删除的币种
            StringJoiner currencyNames = new StringJoiner(SplitUtils.DH);

            for (Integer cid : idList)
            {
                // 检查是否被使用
                CurrencyStoreModel storeQuery = new CurrencyStoreModel();
                storeQuery.setCurrency_id(cid);
                boolean inUse = currencyStoreMapper.select(storeQuery).size() > 0;

                if (inUse)
                {
                    // 查询币种编码
                    CurrencyModel model = new CurrencyModel();
                    model.setId(cid);
                    CurrencyModel dbModel = currencyModelMapper.selectOne(model);
                    if (dbModel != null)
                    {
                        currencyNames.add(dbModel.getCurrency_code());
                    }
                    continue;
                }

                // 删除币种
                CurrencyModel delModel = new CurrencyModel();
                delModel.setId(cid);
                currencyModelMapper.delete(delModel);
            }

            return currencyNames.toString();

        }
        catch (Exception e)
        {
            log.error("操作失败: {}", e.getMessage(), e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "deleteCountry");
        }
    }
}
