package com.laiketui.comps.api.task;

import com.laiketui.core.exception.LaiKeAPIException;

public interface CompsTaskWxService
{

    /**
     * 同步微信发货信息录入的物流公司信息
     *
     * @throws LaiKeAPIException
     */
    void synchronizationWxDelivery() throws LaiKeAPIException;

    /**
     * 商家转账到零钱查询批次转账单结果
     */
    void V3QueryBatchTransferOrder() throws LaiKeAPIException;

    /**
     * 同步微信发货订单状态
     */
    void synchronizationWxOrderStatus() throws LaiKeAPIException;

}
