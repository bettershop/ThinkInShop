package com.laiketui.admins.api.admin.saas;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.CurrencyStoreModel;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;

import java.util.Map;

public interface AdminCurrencyStoreService
{
    /**
     * 获取商城币种列表
     *
     * @param vo   -
     * @param name -
     * @return Map
     * @throws LaiKeAPIException -
     * @author wx
     * @date 2025/4/14
     */
    Map<String, Object> index(CurrencyStoreVo vo) throws LaiKeAPIException;

    /**
     * 添加/编辑商城币种信息
     *
     * @param currencyModel -
     * @throws LaiKeAPIException-
     * @author wx
     * @date 2025/4/14
     */
    void addCurrencyStore(CurrencyStoreModel currencyModel) throws LaiKeAPIException;

    /**
     * 设置商城默认币种
     *
     * @param currencyModel -
     * @throws LaiKeAPIException-
     * @author wx
     * @date 2025/4/14
     */
    Map setDefaultCurrency(CurrencyStoreModel currencyModel,boolean isCheck) throws LaiKeAPIException;

    /**
     * 删除商城币种信息
     *
     * @param id -
     * @throws LaiKeAPIException-
     * @author wx
     * @date 2025/4/14
     */
    void delCurrencyStore(CurrencyStoreVo vo) throws LaiKeAPIException;


    Map getStoreDefaultCurrency(int storeId) throws LaiKeAPIException;


}
