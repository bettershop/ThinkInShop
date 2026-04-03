package com.laiketui.plugins.api.distribution.order;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 分销订单
 *
 * @author Trick
 * @date 2023/3/8 11:22
 */
public interface PluginsDistributionOrderService
{

    /**
     * 后台订单列表
     *
     * @param adminOrderVo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2023-03-08 11:24:22
     * @since Trick v1.0
     */
    Map<String, Object> index(AdminOrderListVo adminOrderVo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 订单统计 - 普通订单
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023-03-08 11:24:22
     */
    Map<String, Object> orderCount(MainVo vo) throws LaiKeAPIException;

    /**
     * 订单结算
     *
     * @param vo       -
     * @param response -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/11 14:13
     */
    Map<String, Object> settlement(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 获取订单物流信息
     *
     * @param vo      -
     * @param orderno -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2023-03-08 11:24:22
     */
    Map<String, Object> kuaidishow(MainVo vo, String orderno) throws LaiKeAPIException;

    /**
     * 关闭订单
     *
     * @param orderVo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2023-03-08 11:24:22
     */
    Map<String, Object> close(AdminOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 删除订单
     *
     * @param vo     -
     * @param orders -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023-03-08 11:24:22
     */
    Map<String, Object> del(MainVo vo, String orders) throws LaiKeAPIException;

    /**
     * 订单打印
     *
     * @param orderVo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023-03-08 11:24:22
     */
    List<Map<String, Object>> orderPrint(AdminOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 搜索快递公司 express
     *
     * @param express -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023-03-08 11:24:22
     */
    Map<String, Object> searchExpress(String express) throws LaiKeAPIException;

    /**
     * 进入发货界面
     *
     * @param adminDeliveryVo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023-03-08 11:24:22
     */
    Map<String, Object> deliveryView(AdminDeliveryVo adminDeliveryVo) throws LaiKeAPIException;

    /**
     * 订单发货
     *
     * @param vo             -
     * @param exId           -
     * @param exNo           -
     * @param orderDetailIds -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023-03-08 11:24:22
     */
    void deliverySave(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException;

    /**
     * 编辑订单界面
     *
     * @param orderVo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023-03-08 11:24:22
     */
    Map<String, Object> editOrderView(OrderModifyVo orderVo) throws LaiKeAPIException;

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
     * 代客下单
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/2 17:36
     */
    Map<String, Object> helpOrder(HelpOrderVo vo) throws LaiKeAPIException;

    /**
     * 代客下单-结算
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/28 13:13
     */
    Map<String, Object> valetOrderSettlement(HelpOrderVo vo) throws LaiKeAPIException;

    /**
     * 平台订单详情
     *
     * @param orderVo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/28 13:13
     */
    Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo) throws LaiKeAPIException;

}
