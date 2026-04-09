package com.laiketui.plugins.api.living.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.mch.MchOrderIndexVo;
import com.laiketui.domain.vo.order.CommentsInfoVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.order.UpdateCommentsInfoVo;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;

import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/11
 */
public interface MchLivingOrderService
{
    /**
     * 我的订单
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> myOrder(MchOrderIndexVo vo) throws LaiKeAPIException;

    /**
     * 订单详情
     *
     * @param vo     -
     * @param shopId -
     * @param sNo    -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> orderDetails(MainVo vo, int shopId, String sNo) throws LaiKeAPIException;

    /**
     * 修改订单
     *
     * @param vo          -
     * @param shopId      -
     * @param orderno     -
     * @param orderDetail -
     * @return boolean
     * @throws LaiKeAPIException -
     */
    boolean upOrder(MainVo vo, int shopId, String orderno, String orderDetail) throws LaiKeAPIException;

    /**
     * 关闭订单
     *
     * @param vo      -
     * @param shopId  -
     * @param orderno -
     * @return boolean
     * @throws LaiKeAPIException -
     */
    boolean closingOrder(MainVo vo, int shopId, String orderno) throws LaiKeAPIException;


    /**
     * 发货列表数据
     *
     * @param vo      -
     * @param orderno -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> deliverShow(MainVo vo, String orderno) throws LaiKeAPIException;

    /**
     * 点击发货按钮-弹出填写发货信息
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> intoSend(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 发货
     *
     * @param vo          -
     * @param shopId      -
     * @param sNo         -
     * @param expressId   -
     * @param courierNum  -
     * @param orderListId -
     * @throws LaiKeAPIException -
     */
    void send(MainVo vo, int shopId, String sNo, Integer expressId, String courierNum, String orderListId) throws LaiKeAPIException;


    /**
     * 结算列表及详情
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getSettlementOrderList(OrderSettlementVo vo) throws LaiKeAPIException;

    /**
     * 获取评论列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getCommentsInfo(CommentsInfoVo vo) throws LaiKeAPIException;

    /**
     * 获取评论详细信息
     *
     * @param vo  -
     * @param cid - 评论id
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getCommentsDetailInfoById(GetCommentsDetailInfoVo vo, int cid) throws LaiKeAPIException;

    /**
     * 修改评论信息
     *
     * @param vo -
     * @throws LaiKeAPIException -
     */
    void updateCommentsDetailInfoById(UpdateCommentsInfoVo vo) throws LaiKeAPIException;

    /**
     * 删除评论信息
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void delComments(MainVo vo, String id, int type) throws LaiKeAPIException;

    /**
     * 售后订单详情
     *
     * @param vo     -
     * @param shopId -
     * @param sNo    -
     * @param id     -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> returnOrderDetails(MainVo vo, int shopId, String sNo, int id) throws LaiKeAPIException;

    /**
     * 售后流程
     *
     * @param vo -
     * @throws LaiKeAPIException -
     */
    void examine(RefundVo vo) throws LaiKeAPIException;

    /**
     * 进入发货界面
     *
     * @param vo -
     * @return Map<String, Object>
     * @throws LaiKeAPIException- gp
     *                            2023-07-14
     */
    Map<String, Object> getLogistics(MainVo vo, String express) throws LaiKeAPIException;


    /**
     * 获取订单物流信息
     *
     * @param vo
     * @param orderNo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> logisticsInfo(MainVo vo, String orderNo) throws LaiKeAPIException;

    /**
     * 根据id获取直播商品信息
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getGoodsInfoById(MainVo vo, Integer goodsId) throws LaiKeAPIException;
}
