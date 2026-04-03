package com.laiketui.plugins.api.flashsale.pcmch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.flashsale.FlashLabelVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.order.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 限时折扣api
 */
public interface PluginsFlashSaleAdminMchService
{

    /**
     * 获取活动列表
     *
     * @param vo
     * @param key
     * @param startDate
     * @param endDate
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getLabelList(MainVo vo, String key, String startDate, String endDate) throws LaiKeAPIException;

    /**
     * 获取商品列表
     *
     * @param vo
     * @param key
     * @param classId
     * @param brandId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getProList(MainVo vo, String key, Integer classId, Integer brandId, String proNotInId) throws LaiKeAPIException;

    /**
     * 添加修改活动
     *
     * @param addLabelVo
     * @throws LaiKeAPIException
     */
    void addAndUpdateLabel(FlashLabelVo addLabelVo) throws LaiKeAPIException;

    /**
     * 获取活动详情
     *
     * @param vo
     * @param id
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> fsLabelGoodsList(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 删除活动
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void delLabel(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 订单列表
     *
     * @param vo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getFlashSalOrderList(AdminOrderListVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 平台订单详情
     *
     * @param orderVo
     * @return
     */
    Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo);

    /**
     * 跳转编辑订单
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> editOrderView(OrderModifyVo vo) throws LaiKeAPIException;


    /**
     * 保存编辑订单
     *
     * @param orderVo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/8/2 10:21
     */
    void saveEditOrder(EditOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 关闭订单
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 15:30
     */
    void closeOrder(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 删除订单
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 17:07
     */
    void delOrder(MainVo vo, String id) throws LaiKeAPIException;

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
     * 删除结算订单
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 15:04
     */
    void del(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 搜后列表
     *
     * @param vo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getRefundList(RefundQueryVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 获取售后详情
     *
     * @param vo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getRefundById(RefundQueryVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 售后审核 通过/拒绝
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/4 14:19
     */
    boolean examine(RefundVo vo) throws LaiKeAPIException;

    /**
     * 发货界面
     *
     * @param adminDeliveryVo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> deliveryView(AdminDeliveryVo adminDeliveryVo) throws LaiKeAPIException;

    //发货mch.Mch.Order.Deliver

    /**
     * 发货提交
     *
     * @param vo
     * @param exId
     * @param exNo
     * @param orderDetailIds
     * @throws LaiKeAPIException
     */
    void deliverySave(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException;


    /**
     * 获取物流信息
     *
     * @param vo
     * @param orderno
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> kuaidishow(MainVo vo, String orderno) throws LaiKeAPIException;

    /**
     * 活动设置页面
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getFsConfig(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加加购商品列表
     *
     * @param vo
     * @param key
     * @param classId
     * @param brandId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getProAttrList(MainVo vo, String key, Integer classId, Integer brandId, String proNotInId) throws LaiKeAPIException;


    /**
     * 保存活动设置
     *
     * @param vo
     * @param goodsJson
     * @throws LaiKeAPIException
     */
    void setFsConfig(MainVo vo, String goodsJson) throws LaiKeAPIException;
}
