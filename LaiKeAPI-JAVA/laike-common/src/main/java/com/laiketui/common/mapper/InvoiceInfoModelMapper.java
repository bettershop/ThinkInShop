package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.invoice.InvoiceInfoModel;

import java.util.List;
import java.util.Map;

public interface InvoiceInfoModelMapper extends BaseMapper<InvoiceInfoModel>
{

    /**
     * 获取发票信息列表
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getList(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取发票信息条数
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    Integer countList(Map<String, Object> map) throws LaiKeAPIException;
}