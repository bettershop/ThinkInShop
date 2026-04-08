package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.CurrencyModel;
import com.laiketui.domain.vo.saas.CurrencyVo;

import java.util.List;

public interface CurrencyModelMapper extends BaseMapper<CurrencyModel>
{

    public int countCurrency(CurrencyVo vo);

    public List<CurrencyModel> queryCurrencyList(CurrencyVo vo);

}