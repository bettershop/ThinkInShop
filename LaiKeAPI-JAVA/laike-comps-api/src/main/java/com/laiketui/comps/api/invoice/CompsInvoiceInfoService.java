package com.laiketui.comps.api.invoice;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.invoice.InvoiceInfoVo;

import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 17:14 2022/7/28
 */
public interface CompsInvoiceInfoService
{

    /**
     * 查询可以开票的订单
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getToInvoiced(MainVo vo) throws LaiKeAPIException;

    /**
     * 查询用户发票列表
     *
     * @param vo
     * @param status
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getInvoiceInfo(MainVo vo, Integer status) throws LaiKeAPIException;

    /**
     * 申请开票
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void applyInvoicing(InvoiceInfoVo vo) throws LaiKeAPIException;

    /**
     * 撤销
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void revoke(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 查询用户发票详情
     *
     * @param vo
     * @param id
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getDetails(MainVo vo, Integer id) throws LaiKeAPIException;


}
