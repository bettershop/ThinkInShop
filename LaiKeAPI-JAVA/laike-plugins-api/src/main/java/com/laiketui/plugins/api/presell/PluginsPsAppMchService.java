package com.laiketui.plugins.api.presell;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;

import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 16:35 2023/6/1
 */
public interface PluginsPsAppMchService
{

    /**
     * 添加预售商品
     *
     * @param vo -
     * @throws LaiKeAPIException -
     */
    void addOrUpdateGoods(UploadMerchandiseVo vo) throws LaiKeAPIException;

    /**
     * 上下架预售商品
     *
     * @param vo       -
     * @param goodsIds -
     * @param status   -
     * @throws LaiKeAPIException -
     */
    void upperAndLowerGoods(MainVo vo, String goodsIds, Integer status) throws LaiKeAPIException;

    /**
     * 删除预售商品
     *
     * @param vo       -
     * @param goodsIds - 商品id,支持多个
     * @throws LaiKeAPIException -
     */
    void delete(MainVo vo, String goodsIds) throws LaiKeAPIException;

    /**
     * 根据id获取预售商品信息
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getGoodsInfoById(MainVo vo, Integer goodsId) throws LaiKeAPIException;

    /**
     * 预售商品列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getGoodsList(DefaultViewVo vo) throws LaiKeAPIException;

    /**
     * 预售商品订单信息列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getOrderList(PreSellOrderVo vo) throws LaiKeAPIException;

    /**
     * 预售订单详情
     *
     * @param vo
     * @param orderNo
     * @return
     */
    Map<String, Object> orderDetail(MainVo vo, String orderNo);

    /**
     * 删除预售订单
     *
     * @param vo
     * @param orderList
     * @throws LaiKeAPIException
     */
    void delOrder(MainVo vo, String orderList) throws LaiKeAPIException;

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
     * 发货页面数据
     *
     * @param vo
     * @param orderNo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> deliveryView(MainVo vo, String orderNo) throws LaiKeAPIException;

    /**
     * 订单发货
     *
     * @param vo             -
     * @param exId           -
     * @param exNo           -
     * @param orderDetailIds -
     * @throws LaiKeAPIException-
     */
    void deliverySave(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException;

    /**
     * 搜索快递公司
     *
     * @param express
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> searchExpress(String express) throws LaiKeAPIException;

    /**
     * 编辑订单信息
     *
     * @param orderVo -
     * @throws LaiKeAPIException-
     */
    void saveEditOrder(EditOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 订单结算
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     */
    Map<String, Object> orderSettlement(OrderSettlementVo vo) throws LaiKeAPIException;

    /**
     * 售后列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getRefundList(RefundQueryVo vo) throws LaiKeAPIException;

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
     * 售后审核 通过/拒绝
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     */
    boolean examine(RefundVo vo) throws LaiKeAPIException;

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


}
