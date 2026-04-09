package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.vo.main.RefundVo;

import java.util.List;
import java.util.Map;

/**
 * 公共售后接口
 * php>RefundUtils
 *
 * @author Trick
 * @date 2020/12/2 11:43
 */
public interface PublicRefundService
{


    /**
     * 售后
     * 【php RefundUtils.refund】
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 12:04
     */
    boolean refund(RefundVo vo) throws LaiKeAPIException;

    /**
     * 急速退款
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    boolean quickRefund(RefundVo vo) throws LaiKeAPIException;

    /**
     * 公共极速退款
     * @param vo
     * @return
     */
    Map<String,Object> adminQuickRefund(RefundVo vo);

    /**
     * 获取售后详细信息
     * 【php RefundUtils.refund_page】
     *
     * @param storeId -
     * @param id      - 售后id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 10:58
     */
    Map<String, Object> refundPageById(int storeId, int id) throws LaiKeAPIException;

    /**
     * 获取售后状态 - 订单
     * 【不推荐使用此方法】
     * 【此方法只能获取单个商品订单售后的最新进展,多个商品请加订单明细id】
     *
     * @param storeId -
     * @param orderNo -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/28 16:56
     */
    String getRefundStatus(int storeId, String orderNo) throws LaiKeAPIException;

    /**
     * 获取售后状态 - 订单
     *
     * @param storeId       -
     * @param orderNo       -
     * @param orderDetailId -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022-05-24 19:51:25
     */
    String getRefundStatus(int storeId, String orderNo, Integer orderDetailId) throws LaiKeAPIException;

    String getRefundStatus(int storeId, Integer recId) throws LaiKeAPIException;

    /**
     * 获取售后状态 - 订单商品
     *
     * @param storeId  -
     * @param detailId -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/28 16:56
     */
    String getRefundStatus(int storeId, int detailId) throws LaiKeAPIException;

    /**
     * 售后按钮是否能显示【申请售后=refund:boolean、仅退款=refundAmt:boolean、退货退款=refundGoodsAmt:boolean、换货=refundGoods:boolean】
     *
     * @param storeId           -
     * @param oType             -
     * @param orderDetailsModel -
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/17 17:35
     */
    Map<String, Boolean> afterSaleButtonShow(int storeId, String oType, OrderDetailsModel orderDetailsModel) throws LaiKeAPIException;

    /**
     * 是否显示申请售后
     * @param storeId
     * @param oType
     * @param orderDetailsModelList
     * @param orderModel
     * @param unFinishShouHouOrderNum
     * @return
     */
    Map<String,Boolean> afterSaleButtonShow(Integer storeId, String oType, List<OrderDetailsModel> orderDetailsModelList, OrderModel orderModel,Integer unFinishShouHouOrderNum);

    /**
     * 获取售后状态最终结果
     *
     * @param rType  - 售后状态
     * @param reType - 售后类型
     * @return int - 1=待审核 2=退款中 3=退款成功 4=退款失败(仅退款) 5=退换中 6=换货成功 7=换货失败 8=退款失败(退货退款) 9=人工审核失败 10=人工审核成功
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/17 11:36
     */
    int getOrderRefundType(int rType, int reType) throws LaiKeAPIException;

    /**
     * 是否可以审核
     *
     * @param storeId  -
     * @param refundId - 售后id
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/17 18:10
     */
    boolean isExamine(int storeId, Integer refundId) throws LaiKeAPIException;

    /**
     * 是否可以人工审核
     *
     * @param storeId  -
     * @param refundId - 售后id
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/17 18:10
     */
    boolean isMainExamine(int storeId, Integer refundId) throws LaiKeAPIException;
}
