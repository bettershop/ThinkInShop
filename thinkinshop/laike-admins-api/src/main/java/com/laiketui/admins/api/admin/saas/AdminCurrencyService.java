package com.laiketui.admins.api.admin.saas;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.CurrencyModel;
import com.laiketui.domain.vo.saas.CurrencyVo;

import java.util.Map;

public interface AdminCurrencyService
{
    /**
     * 获取币种列表
     *
     * @param vo   -
     * @param name -
     * @return Map
     * @throws LaiKeAPIException -
     * @author wx
     * @date 2025/4/14
     */
    Map<String, Object> index(CurrencyVo vo) throws LaiKeAPIException;

    /**
     * 添加/编辑币种
     *
     * @param currencyModel -
     * @throws LaiKeAPIException-
     * @author wx
     * @date 2025/4/14
     */
    void addCurrency(CurrencyModel currencyModel) throws LaiKeAPIException;

    /**
     * 删除币种
     *
     * @param id -
     * @throws LaiKeAPIException-
     * @author wx
     * @date 2025/4/14
     */
    String delCurrency(CurrencyVo vo) throws LaiKeAPIException;

}
