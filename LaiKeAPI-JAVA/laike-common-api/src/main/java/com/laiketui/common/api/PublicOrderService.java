package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.order.ExpressDeliveryModel;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.order.ReturnOrderModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.config.AddPluginOrderConfigVo;
import com.laiketui.domain.vo.mch.FrontDeliveryVo;
import com.laiketui.domain.vo.mch.MchOrderDetailVo;
import com.laiketui.domain.vo.mch.MchOrderIndexVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.domain.vo.pc.MchPcOrderIndexVo;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单通用服务
 *
 * @author wangxian
 */
public interface PublicOrderService
{

    /**
     * 计算运费
     *
     * @param freightMap
     * @param products
     * @param userAddress
     * @param storeId
     * @param productType
     * @return
     */
    Map<String, Object> getFreight(Map<String, List<Map<String, Object>>> freightMap, List<Map<String, Object>> products, UserAddress userAddress, int storeId, String productType);

    /**
     * 计算运费
     * 【根据商品设置的运费模板计算运费】
     *
     * @param goodsAddressId - 商品运费模板id
     * @param userAddress    - 当前用户选择的运费模型数据 - 可选
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/26 18:15
     */
    BigDecimal getFreight(Integer goodsAddressId, UserAddress userAddress) throws LaiKeAPIException;

    /**
     * 计算运费
     * 【根据商品设置的运费模板计算运费】
     *
     * @param goodsAddressId - 商品运费模板id
     * @param userAddress    - 当前用户选择的运费模型数据 - 可选
     * @param productNum     - 商品件数
     * @param weightNum      - 商品重量
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/26 18:15
     */
    BigDecimal getFreight(Integer goodsAddressId, UserAddress userAddress, int productNum, BigDecimal weightNum) throws LaiKeAPIException;

    /**
     * 创建订单号码
     *
     * @param orderType
     * @return
     */
    String createOrderNo(String orderType) throws LaiKeAPIException;

    /**
     * 订单列表（pc后台）
     * 【php DeliveryHelper.order_index】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-07-19 15:03:25
     */
    Map<String, Object> pcMchOrderIndex(AdminOrderListVo vo) throws LaiKeAPIException;

    /**
     * 订单列表（移动店铺端）
     * 【php DeliveryHelper.a_mch_order_index】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/24 17:06
     */
    Map<String, Object> aMchOrderIndex(MchOrderIndexVo vo) throws LaiKeAPIException;


    /**
     * pc店铺端-订单列表
     * 【php DeliveryHelper.b_mch_order_index】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/3 9:51
     */
    Map<String, Object> bMchOrderIndex(MchPcOrderIndexVo vo) throws LaiKeAPIException;

    /**
     * 支付后修改订单信息，暂时只处理虚拟商品和实物商品
     *
     * @param storeId -
     * @param sNo     -
     * @param tradeNo - 第三方支付订单号
     * @param userId  -
     * @param payType - 支付方式
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/6 14:10
     */
    Map<String, Object> orderPayment(int storeId, String sNo, String tradeNo, String userId, String payType,String oType) throws LaiKeAPIException;

    /**
     * 支付后修改订单信息除虚拟商品和实物商品
     *
     * @param storeId -
     * @param sNo     -
     * @param tradeNo - 第三方支付订单号
     * @param userId  -
     * @param payType - 支付方式
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/6 14:10
     */
    BigDecimal orderPaymentForOther(int storeId, String sNo, String tradeNo, String userId, String payType) throws LaiKeAPIException;

    /**
     * 拆单失败回退订单信息
     * 【php DeliveryHelper.frontDelivery】
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 16:43
     */
    void reBackOrder(PaymentVo vo) throws LaiKeAPIException;

    /**
     * 发货
     * 【php DeliveryHelper.frontDelivery】
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 16:43
     */
    void frontDelivery(FrontDeliveryVo vo) throws LaiKeAPIException;


    /**
     * 订单详情/订单列表-确认收货
     *
     * @param vo       -
     * @param tokenKey - 登录模块key
     * @param orderNo  -
     * @param rType    -退货状态
     * @return Map
     * @throws LaiKeAPIException -
     * @author wang xian
     * @date 2021/4/6 10:23
     */
    Map<String, Object> okOrder(MainVo vo, String tokenKey, String orderNo, Integer rType) throws LaiKeAPIException;

    /**
     * 订单详情/订单列表-确认收货
     *
     * @param storeId  -
     * @param accessId -
     * @param orderno  -
     * @param rType    -退货状态
     * @return Map
     * @throws LaiKeAPIException -
     * @author wang xian
     * @date 2021/4/6 10:23
     */
    Map<String, Object> okOrder(int storeId, String accessId, String orderno, Integer rType) throws LaiKeAPIException;

    /**
     * 发货
     * 【php DeliveryHelper.frontDelivery】
     *
     * @param vo             -
     * @param exId           -
     * @param exNo           -
     * @param orderDetailIds -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 16:43
     */
    void adminDelivery(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException;

    /**
     * 插件发货
     * 【php DeliveryHelper.frontDelivery】
     *
     * @param vo             -
     * @param exId           -
     * @param exNo           -
     * @param orderDetailIds -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 16:43
     */
    void pluginsAdminDelivery(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException;

    /**
     * 订单发货
     *
     * @param vo             -
     * @param exId           -
     * @param exNo           -
     * @param orderDetailIds -
     * @throws LaiKeAPIException -
     * @author Administrator
     * @date 2021/8/2 14:42
     */
    void deliverySave(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException;

    /**
     * 保存编辑订单
     *
     * @param orderVo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/8/2 10:21
     */
    void saveEditOrder(com.laiketui.domain.vo.order.EditOrderVo orderVo) throws LaiKeAPIException;


    /**
     * 进入发货界面
     *
     * @param adminDeliveryVo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022-01-12 19:17:30
     */
    Map<String, Object> deliveryView(com.laiketui.domain.vo.order.AdminDeliveryVo adminDeliveryVo) throws LaiKeAPIException;

    /**
     * 搜索快递公司
     *
     * @param express -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022-01-12 19:17:30
     */
    Map<String, Object> searchExpress(String express) throws LaiKeAPIException;


    /**
     * 电子面单发货
     *
     * @param vo             -
     * @param exId           -
     * @param orderDetailIds -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 16:43
     */
    void electronicSheetDelivery(MainVo vo, Integer exId, String orderDetailIds) throws LaiKeAPIException;

    /**
     * 取消电子面单发货
     *
     * @param vo                -
     * @param expressDeliveryId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 16:43
     */
    String cancelElectronicSheetDelivery(MainVo vo, Integer expressDeliveryId) throws LaiKeAPIException;


    /**
     * 获取订单详情
     * 【php DeliveryHelper.mch_order_details】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/27 11:21
     */
    Map<String, Object> mchOrderDetails(MchOrderDetailVo vo) throws LaiKeAPIException;


    /**
     * 订单详情(后台、PC店铺)
     * 【php DeliveryHelper.order_details】
     *
     * @param storeId  -
     * @param ordernno -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/4 9:24
     */
    Map<String, Object> storeOrderDetails(int storeId, String ordernno) throws LaiKeAPIException;


    /**
     * 统一修改订单状态
     * 【如果明细都是一个状态，则修正主表状态】
     *
     * @param storeId -
     * @param orderno -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/27 11:21
     */
    void updateOrderState(int storeId, String orderno,Integer type) throws LaiKeAPIException;


    /**
     * 订单列表(用户个人中心)
     *
     * @param vo
     * @param user
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> orderList(OrderVo vo, User user) throws LaiKeAPIException;

    /**
     * 订单列表 pc管理后台订单列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> orderPcList(AdminOrderVo vo) throws LaiKeAPIException;


    /**
     * 根据订单号获取物流信息
     *
     * @param orderNo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/15 9:39
     */
    Map<String, Object> getLogistics(String orderNo) throws LaiKeAPIException;


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
     * 积分订单详情 h5
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/18 11:11
     */
    Map<String, Object> orderList(OrderVo vo) throws LaiKeAPIException;


    /**
     * 移动端用户个人中心订单详情
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> ucOrderDetails(OrderVo vo) throws LaiKeAPIException;

    /**
     * 获取可以退款的金额
     *
     * @param orderDetailId
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    BigDecimal getOrderPrice(int orderDetailId, int storeId) throws LaiKeAPIException;

    /**
     * 支付完成订单更新
     *
     * @param orderModel
     * @return
     * @throws LaiKeAPIException
     */
    Map payBackUpOrder(OrderModel orderModel) throws LaiKeAPIException;

    /**
     * 支付完成订单更新(充值、续费、升级)
     *
     * @param orderDataModel
     * @return
     * @throws LaiKeAPIException
     */
    Map payBackUpOrderMember(OrderDataModel orderDataModel) throws LaiKeAPIException;

    /**
     * 充值会员回调业务
     *
     * @param orderDataModel
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> payBackForMember(OrderDataModel orderDataModel) throws LaiKeAPIException;

    /**
     * 店铺保证金
     *
     * @param orderDataModel -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/11 15:57
     */
    Map<String, Object> payBackUpOrderMchPromise(OrderDataModel orderDataModel) throws LaiKeAPIException;

    /**
     * 竞拍保证金
     *
     * @param orderDataModel -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/18 11:11
     */
    Map<String, Object> payBackUpOrderAuctionPromise(OrderDataModel orderDataModel) throws LaiKeAPIException;

    /**
     * 预售商品回调业务
     *
     * @param vo
     * @param orderModel
     * @return Map
     * @throws LaiKeAPIException
     * @author Trick
     * @date 2021/11/11 15:57
     */
    Map<String, Object> payBackForPreSell(PaymentVo vo, OrderModel orderModel) throws LaiKeAPIException;


    /**
     * 获取订单信息
     *
     * @param orderNo   -
     * @param paymentVo - 此参数非特殊处理无需传
     * @param userId    - 此参数非特殊处理无需传
     * @return OrderModel
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/12 14:26
     */
    OrderModel getOrderInfo(String orderNo, PaymentVo paymentVo, String userId) throws LaiKeAPIException;

    OrderModel getOrderInfo(String orderNo) throws LaiKeAPIException;

    /**
     * 订单编辑逻辑
     *
     * @param orderVo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/5/12 17:22
     */
    void modifyOrder(EditOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 后台订单详情接口
     *
     * @param adminOrderVo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> orderPcDetails(AdminOrderDetailVo adminOrderVo) throws LaiKeAPIException;


    /**
     * 获取支付配置信息
     * 【php Tool.getPayment】
     *
     * @param storeId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/29 11:34
     */
    Map<String, Object> getPaymentConfig(int storeId) throws LaiKeAPIException;


    /**
     * 根据订单id/订单号获取订单信息
     *
     * @param storeId -
     * @param id      -
     * @param orderno -
     * @return OrderModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/12 15:57
     */
    OrderModel getOrderInfoById(int storeId, Integer id, String orderno) throws LaiKeAPIException;


    /**
     * 根据订单详情id获取订单信息
     *
     * @param storeId -
     * @param did     - 订单详情id
     * @return OrderModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/12 16:11
     */
    OrderModel getOrderInfoByDetailId(int storeId, int did) throws LaiKeAPIException;

    /**
     * 获取订单结算列表
     *
     * @param vo-
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/27 19:08
     */
    Map<String, Object> getSettlementOrderList(OrderSettlementVo vo) throws LaiKeAPIException;

    /**
     * 根据订单类型获取订单配置
     * 【插件订单配置请传店铺id】
     *
     * @param storeId -
     * @param oType   -  订单类型
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/10 11:35
     */
    Map<String, Object> getOrderConfig(int storeId, String oType) throws LaiKeAPIException;

    /**
     * 根据订单类型获取订单配置
     *
     * @param storeId -
     * @param mchId   -
     * @param oType   -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/13 10:20
     */
    Map<String, Object> getOrderConfig(int storeId, Integer mchId, String oType) throws LaiKeAPIException;

    /**
     * annotation
     * 【php xx.xx】
     *
     * @param storeId -
     * @param mchId   -
     * @param oType   -
     * @param isOpen  - 是否只获取开启插件的配置
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/13 10:59
     */
    Map<String, Object> getOrderConfig(int storeId, Integer mchId, String oType, boolean isOpen) throws LaiKeAPIException;

    /**
     * 添加插件配置
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/28 15:32
     */
    void addOrderConfig(AddPluginOrderConfigVo vo) throws LaiKeAPIException;

    /**
     * 订单是否已过售后期
     *
     * @param storeId    -
     * @param mchId      - 店铺id
     * @param oType      - 订单类型
     * @param arriveTime - 收货时间
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/5/26 11:53
     */
    void orderAfterSaleExpire(int storeId, Integer mchId, String oType, Date arriveTime) throws LaiKeAPIException;

    /**
     * 订单是否可以评价 - 0=不评价 1=待评价 2=待追评 3=评论完成
     *
     * @param storeId     -
     * @param userId      -
     * @param orderNo     -
     * @param detailId    - 订单明细id
     * @param sid         - 规格id
     * @param orderStatus - 订单状态
     * @return int
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/29 18:05
     */
    int orderCommentType(int storeId, String userId, String orderNo, int detailId, int sid, int orderStatus) throws LaiKeAPIException;

    /**
     * 订单报表:实时订单数 + 订单金额 + 退款金额
     *
     * @param storeid
     * @return
     */
    List<Map<String, Object>> getReportData(int storeid) throws LaiKeAPIException;

    /**
     * 订单总数统计
     *
     * @param storeid
     * @return
     */
    Map<String, Object> getOrderData(int storeid);

    /**
     * 付款 + 退款订单统计
     *
     * @param storeid
     * @return
     */
    Map<String, Object> getRefundOrderData(int storeid);

    /**
     * 订单报表顶部
     *
     * @param storeid
     * @param status
     * @return
     */
    Map<String, Object> getOrderDataByStatus(int storeid, int status) throws LaiKeAPIException;


    /**
     * 订单是否可以删除按钮显示逻辑
     * 否可以删除订单 只有【非自提】已完成且未在受后期、订单已结算的订单可以删除
     *
     * @param orderId - 订单id
     * @return boolean -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/28 9:46
     */
    boolean isOrderDelButtonShow(int orderId) throws LaiKeAPIException;

    /**
     * 判断用户、商家、平台是否都已经删除订单
     *
     * @param storeId
     * @param sNo
     * @return Boolean
     * gp
     * 2023-12-05
     */
    Boolean allDelOrder(int storeId, String sNo) throws LaiKeAPIException;

    /**
     * 商家自发货
     *
     * @param storeId
     * @param sNo
     * @return Boolean
     * gp
     * 2023-12-05
     */
    void selfSend(FrontDeliveryVo vo) throws LaiKeAPIException;

    /**
     * 客服页面获取订单列表
     * @param vo
     * @return
     */
    Map<String, Object> customerOrderIndex(OrderVo vo);

    /**
     * 根据id获取订单号
     * @param orderId
     * @return
     */
    String getsNoByOrderId(Integer orderId);

    /**
     * 根据订单号获取订单状态
     */
    Integer getStatusByOrderNo(String sNo);

    /**
     * 订单退款
     * @param returnOrderModelList
     */
    void thirdPayOrderReturn(List<ReturnOrderModel> returnOrderModelList) throws LaiKeAPIException;

    /**
     * 获取支付配置信息
     * @param type
     * @param storeId
     * @return
     */
    Map<String, Object> getPayConfig(String type, int storeId) throws LaiKeAPIException;


    /**
     * 快递100电子面单回调
     * @param request
     * @throws LaiKeAPIException
     */
    void kuaidi100CouldNotify(HttpServletRequest request) throws  LaiKeAPIException;

    /**
     * 快递100复打
     * @param configModel
     * @param model
     * @throws LaiKeAPIException
     */
    void overridePrint(ConfigModel configModel, ExpressDeliveryModel model) throws LaiKeAPIException;

    /**
     * 线下支付上传凭证
     * @param vo
     * @throws LaiKeAPIException
     */
    void uploadCredentials(OrderVo vo) throws LaiKeAPIException;

    Boolean trackDelivery(Integer storeId,String courierNum) throws LaiKeAPIException;

    /**
     * 17track快递回调
     * @param request
     * @throws LaiKeAPIException
     */
    void trackNotify(HttpServletRequest request) throws LaiKeAPIException;
}
