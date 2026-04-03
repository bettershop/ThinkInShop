package com.laiketui.apps.api.app.group;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 拼团
 *
 * @author Trick
 * @date 2021/2/20 11:20
 */
public interface AppsCstrGroupBuyService
{

    /**
     * 拼团首页
     * 【php groupbuy.grouphome】
     *
     * @param vo      -
     * @param navType - 查询类型 1=查询活动结束+进行中的活动
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/20 11:22
     */
    Map<String, Object> grouphome(MainVo vo, Integer navType) throws LaiKeAPIException;


    /**
     * 获取商品详情数据
     *
     * @param vo                 -
     * @param goodsId            -
     * @param activityId         -
     * @param platformActivityId - 平台活动id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/20 17:30
     */
    Map<String, Object> getgoodsdetail(MainVo vo, int goodsId, int activityId, int platformActivityId) throws LaiKeAPIException;


    /**
     * 拼团订单
     * 【php groupbuy.grouphome】
     *
     * @param vo        -
     * @param orderType -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/20 11:22
     */
    Map<String, Object> grouporder(MainVo vo, String orderType) throws LaiKeAPIException;


    /**
     * 拼团订单明细
     *
     * @param vo      -
     * @param orderno -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/24 15:15
     */
    Map<String, Object> grouporderDetail(MainVo vo, String orderno) throws LaiKeAPIException;


    /**
     * 选择支付方式/拼团订单明细
     * 【php groupbuy.order_details】
     *
     * @param vo      -
     * @param orderId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/29 18:17
     */
    Map<String, Object> orderDetails(MainVo vo, int orderId) throws LaiKeAPIException;

    /**
     * 关闭待付款的订单
     *
     * @param vo      -
     * @param orderId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/31 17:59
     */
    boolean removeGroupOrder(MainVo vo, int orderId) throws LaiKeAPIException;


    /**
     * 发货
     *
     * @param vo         -
     * @param sNo        -
     * @param expressId  -
     * @param courierNum -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/6 9:37
     */
    void deliveryGood(MainVo vo, String sNo, Integer expressId, String courierNum) throws LaiKeAPIException;


    /**
     * 订单详情/订单列表-确认收货
     *
     * @param storeId  -
     * @param accessId -
     * @param orderno  -
     * @param rType    -退货状态
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/6 10:23
     */
    Map<String, Object> okOrder(int storeId, String accessId, String orderno, Integer rType) throws LaiKeAPIException;
}
