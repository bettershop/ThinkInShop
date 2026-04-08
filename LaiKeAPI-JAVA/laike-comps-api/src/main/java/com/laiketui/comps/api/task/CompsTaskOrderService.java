package com.laiketui.comps.api.task;

import com.laiketui.core.exception.LaiKeAPIException;

/**
 * 订单定时任务接口
 *
 * @author Trick
 * @date 2020/12/14 15:25
 */
public interface CompsTaskOrderService
{


    /**
     * 删除过期的订单
     * 【php test.order_failure】
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/14 15:27
     */
    void orderFailure() throws LaiKeAPIException;


    /**
     * 自动收货任务
     * 【php ReceiveGoodsUtils.timeReceive】
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/15 9:15
     */
    void receivingGoods() throws LaiKeAPIException;


    /**
     * 拼团订单定时任务
     * 【php go_group.dotask】
     *
     * @throws LaiKeAPIException -
     */
    void ptOrderTask() throws LaiKeAPIException;

    /**
     * 订单报表定时任务
     *
     * @throws LaiKeAPIException
     */
    void saveReportData() throws LaiKeAPIException;

    /**
     * 分销订单确认收货佣金结算特殊处理
     *
     * @param fxOrder
     * @throws LaiKeAPIException
     * @author gp
     * @date 2023/09/11
     */
    void distributionSettlement() throws LaiKeAPIException;

    /**
     * 第三方支付订单退款
     */
    void thirdPayOrderReturn() throws LaiKeAPIException;

}
