package com.laiketui.admins.api.supplier.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.order.AdminOrderDetailVo;
import com.laiketui.domain.vo.order.AdminOrderListVo;
import com.laiketui.domain.vo.order.AdminOrderVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.pc.MchPcReturnOrderVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 16:21 2023/2/14
 */
public interface SupplierAdminMchOrderService
{

    /**
     * 订单列表
     *
     * @param adminOrderVo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> orderIndex(AdminOrderListVo adminOrderVo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 订单详情
     *
     * @param orderVo
     * @return
     */
    Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo);

    /**
     * 关闭订单
     *
     * @param orderVo
     * @return
     * @throws LaiKeAPIException
     */
    void close(AdminOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 获取订单物流信息
     *
     * @param vo      -
     * @param orderno -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> kuaidishow(MainVo vo, String orderno) throws LaiKeAPIException;

    /**
     * 删除订单
     *
     * @param vo     -
     * @param orders -
     * @return Map
     * @throws LaiKeAPIException-
     */
    void del(MainVo vo, String orders) throws LaiKeAPIException;

    /**
     * 退款售后
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> returnList(MchPcReturnOrderVo vo, HttpServletResponse response) throws LaiKeAPIException;

    interface OrderStatusEnum
    {
        /**
         * 待审核
         */
        Integer TO_BE_REVIEWED   = 0;
        /**
         * 退款中
         */
        Integer IN_REFUND        = 1;
        /**
         * 退款成功
         */
        Integer REFUND_SUCCESS   = 2;
        /**
         * 退款失败
         */
        Integer REFUND_FAILED    = 3;
        /**
         * 换货中
         */
        Integer IN_EXCHANGE      = 4;
        /**
         * 换货成功
         */
        Integer EXCHANGE_SUCCESS = 5;
        /**
         * 换货失败
         */
        Integer EXCHANGE_FAILED  = 6;
    }

    /**
     * 售后订单详情
     *
     * @param vo      -
     * @param id      -
     * @param orderNo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> refundPageById(MainVo vo, Integer id, String orderNo) throws LaiKeAPIException;

    /**
     * 售后审核
     *
     * @param vo -
     * @throws LaiKeAPIException -
     */
    void examine(RefundVo vo) throws LaiKeAPIException;

    /**
     * 订单结算列表
     *
     * @param vo       -
     * @param response -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> settlementIndex(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 查看订单明细
     *
     * @param vo  -
     * @param sNo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> settlementDetail(MainVo vo, String sNo) throws LaiKeAPIException;


    /**
     * 删除结算订单
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     */
    void settlementDel(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 一键代发
     *
     * @param vo
     * @param orders
     * @throws LaiKeAPIException
     */
    void oneClickDistribution(MainVo vo, String orders) throws LaiKeAPIException;
}
