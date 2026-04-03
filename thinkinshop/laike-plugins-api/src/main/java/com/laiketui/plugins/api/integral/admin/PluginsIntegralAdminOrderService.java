package com.laiketui.plugins.api.integral.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.order.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 积分订单
 *
 * @author Trick
 * @date 2021/11/2 15:16
 */
public interface PluginsIntegralAdminOrderService
{
    /**
     * 秒杀订单首页
     *
     * @param adminOrderVo -
     * @param response     -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 15:02
     */
    Map<String, Object> index(AdminOrderListVo adminOrderVo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 关闭订单
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 15:30
     */
    void closeOrder(OrderVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 删除订单
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 17:07
     */
    void delOrder(OrderVo vo) throws LaiKeAPIException;

    /**
     * 订单结算
     *
     * @param vo       -
     * @param response -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/21 15:30
     */
    Map<String, Object> orderSettlement(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 订单统计 -
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/19 17:24
     */
    Map<String, Object> orderCount(MainVo vo) throws LaiKeAPIException;


    /**
     * 平台订单详情
     *
     * @param orderVo
     * @return
     */
    Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo);


    /**
     * 保存编辑订单
     *
     * @param orderVo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/2 10:21
     */
    void saveEditOrder(EditOrderVo orderVo) throws LaiKeAPIException;


    /**
     * 编辑订单界面
     *
     * @param orderVo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> editOrderView(OrderModifyVo orderVo) throws LaiKeAPIException;


    /**
     * 获取订单物流信息
     *
     * @param vo      -
     * @param orderno -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/4 15:29
     */
    Map<String, Object> kuaidishow(MainVo vo, String orderno) throws LaiKeAPIException;

    /**
     * 订单发货
     *
     * @param vo             -
     * @param exId           -
     * @param exNo           -
     * @param orderDetailIds -
     * @throws LaiKeAPIException-
     * @author Administrator
     * @date 2021/8/2 14:42
     */
    void deliverySave(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException;


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


}
