package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.invoice.InvoiceHeaderVo;

import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 14:39 2022/7/28
 */
public interface AppsMallInvoiceHeaderService
{

    /**
     * 查询我的发票抬头
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getList(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 添加/修改发票抬头信息
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void addOrUpdate(InvoiceHeaderVo vo) throws LaiKeAPIException;

    /**
     * 删除发票抬头信息
     *
     * @param vo
     * @param ids
     * @throws LaiKeAPIException
     */
    void del(MainVo vo, String ids) throws LaiKeAPIException;

    /**
     * 获得默认发票抬头信息
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getDefault(MainVo vo) throws LaiKeAPIException;
}
