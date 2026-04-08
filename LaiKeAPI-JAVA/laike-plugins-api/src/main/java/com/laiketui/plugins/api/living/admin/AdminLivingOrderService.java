package com.laiketui.plugins.api.living.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/7
 */
public interface AdminLivingOrderService
{

    /**
     * 直播订单首页
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
     * 平台订单详情
     *
     * @param orderVo
     * @return
     */
    Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo);

    /**
     * 编辑订单界面
     *
     * @param orderVo
     * @return
     * @throws LaiKeAPIException
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
     * 搜索快递公司 express
     *
     * @return
     */
    Map<String, Object> searchExpress(String express) throws LaiKeAPIException;

    /**
     * 进入发货界面
     *
     * @param adminDeliveryVo
     * @return
     * @throws LaiKeAPIException
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
     * @author Administrator
     * @date 2021/8/2 14:42
     */
    void deliverySave(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException;

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
     * 订单结算 列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 11:38
     */
    Map<String, Object> settlementIndex(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 订单详情
     *
     * @param vo      -
     * @param orderNo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 14:51
     */
    Map<String, Object> settlementDetail(MainVo vo, String orderNo) throws LaiKeAPIException;

    /**
     * 删除结算订单
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 15:04
     */
    void settlementDel(MainVo vo, int id) throws LaiKeAPIException;


    Map<String, Object> del(MainVo vo, String orders) throws LaiKeAPIException;
}
