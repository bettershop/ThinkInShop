package com.laiketui.admins.api.admin.report;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.report.OrderReportModel;
import com.laiketui.domain.vo.MainVo;

import java.util.List;


public interface AdminOrderReportService
{

    List<OrderReportModel> getOrderData(MainVo vo);

    OrderReportModel getOrderAmount(MainVo vo);

    /**
     * +
     * 订单总数统计
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Object getTotalAmount(MainVo vo) throws LaiKeAPIException;

    /**
     * 付款 + 退款订单统计
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Object getRefundData(MainVo vo) throws LaiKeAPIException;
}
