package com.laiketui.admins.api.supplier;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.order.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 14:24 2022/9/27
 */
public interface SupplierOrderService
{

    /**
     * 订单列表
     *
     * @param adminOrderVo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> index(AdminOrderListVo adminOrderVo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 订单详情
     *
     * @param orderVo
     * @return
     */
    Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo);

    /**
     * 获取订单物流信息
     *
     * @param vo
     * @param orderNo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> logistics(MainVo vo, String orderNo) throws LaiKeAPIException;

    /**
     * 进入发货界面
     *
     * @param vo
     * @param sNo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> GetLogistics(MainVo vo, String sNo) throws LaiKeAPIException;

    /**
     * 进入发货界面
     *
     * @param adminDeliveryVo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> deliveryView(AdminDeliveryVo adminDeliveryVo) throws LaiKeAPIException;

    /**
     * 搜索快递公司 express
     *
     * @return
     */
    Map<String, Object> searchExpress(String express) throws LaiKeAPIException;

    /**
     * 订单发货
     *
     * @param vo
     * @param exId
     * @param exNo
     * @param orderDetailIds
     * @throws LaiKeAPIException
     */
    void deliverySave(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException;

    /**
     * 删除订单
     *
     * @param vo
     * @param orders
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> del(MainVo vo, String orders) throws LaiKeAPIException;

    /**
     * 订单结算 列表
     *
     * @param vo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> settlement(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 删除结算订单
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     */
    void settlementDel(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 获取售后列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getRefundList(RefundQueryVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 售后审核 通过/拒绝
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     */
    boolean examine(RefundVo vo) throws LaiKeAPIException;

    /**
     * 订单打印
     *
     * @param orderVo
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> orderPrint(AdminOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 订单发货
     *
     * @param vo
     * @param orderDetailIds
     * @throws LaiKeAPIException
     */
    void deliverySaveNew(MainVo vo, String orderDetailIds);
}
