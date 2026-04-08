package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.supplier.SupplierWithdrawModel;

import java.util.List;
import java.util.Map;

public interface SupplierWithdrawModelMapper extends BaseMapper<SupplierWithdrawModel>
{

    /**
     * 获取提现信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     */
    List<Map<String, Object>> getWithdraw(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计提现信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     */
    int countWithdraw(Map<String, Object> map) throws LaiKeAPIException;

}