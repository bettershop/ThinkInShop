package com.laiketui.plugins.api.presell.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 19:45 2023/6/7
 */
public interface PluginsPsMchService
{

    /**
     * 添加预售商品
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2020/12/20 15:00
     */
    void addOrUpdateGoods(UploadMerchandiseVo vo) throws LaiKeAPIException;

    /**
     * 上下架预售商品
     *
     * @param vo       -
     * @param goodsIds -
     * @param status   -
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2021/12/20 16:30
     */
    void upperAndLowerGoods(MainVo vo, String goodsIds, Integer status) throws LaiKeAPIException;

    /**
     * 删除预售商品
     *
     * @param vo       -
     * @param goodsIds - 商品id,支持多个
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2021/12/20 17:00
     */
    void delete(MainVo vo, String goodsIds) throws LaiKeAPIException;

    /**
     * 根据id获取预售商品信息
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2020/12/20 18:00
     */
    Map<String, Object> getGoodsInfoById(MainVo vo, Integer goodsId) throws LaiKeAPIException;

    /**
     * 预售商品列表
     *
     * @param vo       -
     * @param response -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2020/12/20 18:00
     */
    Map<String, Object> getGoodsList(DefaultViewVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 预售订单详情
     *
     * @param vo
     * @param orderNo
     * @return
     * @author sunH_
     * @date 2020/12/20 18:00
     */
    Map<String, Object> orderDetail(MainVo vo, String orderNo);

    /**
     * 预售商品订单信息列表
     *
     * @param vo       -
     * @param response -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2020/12/20 18:00
     */
    Map<String, Object> getOrderList(PreSellOrderVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 删除预售订单
     *
     * @param vo
     * @param orderList
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2021/12/22 17:00
     */
    void delOrder(MainVo vo, String orderList) throws LaiKeAPIException;

    /**
     * 获取订单物流信息
     *
     * @param vo
     * @param orderNo
     * @return
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2021/12/22 18:00
     */
    Map<String, Object> logisticsInfo(MainVo vo, String orderNo) throws LaiKeAPIException;

    /**
     * 发货页面数据
     *
     * @param vo
     * @param orderNo
     * @return
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2021/12/22 18:00
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
     * @author sunH_
     * @date 2021/12/22 18:00
     */
    void deliverySave(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException;

    /**
     * 搜索快递公司
     *
     * @param express
     * @return
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2021/12/22 18:00
     */
    Map<String, Object> searchExpress(String express) throws LaiKeAPIException;

    /**
     * 编辑订单信息
     *
     * @param orderVo -
     * @throws LaiKeAPIException-
     * @author sunH_
     * @date 2021/12/22 18:00
     */
    void saveEditOrder(EditOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 订单打印
     *
     * @param orderVo
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> orderPrint(AdminOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 售后列表
     *
     * @param vo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getRefundList(RefundQueryVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 订单结算
     *
     * @param vo       -
     * @param response -
     * @return Map
     * @throws LaiKeAPIException-
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
     * 获取评论列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 16:07
     */
    Map<String, Object> getCommentsInfo(CommentsInfoVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 获取评论详细信息
     *
     * @param vo  -
     * @param cid - 评论id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 17:23
     */
    Map<String, Object> getCommentsDetailInfoById(GetCommentsDetailInfoVo vo, int cid) throws LaiKeAPIException;

    /**
     * 修改评论信息
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 18:07
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
     * 回复
     *
     * @param vo          -
     * @param commentId   -
     * @param commentText -
     * @return boolean
     * @throws LaiKeAPIException -
     */
    boolean replyComments(MainVo vo, int commentId, String commentText) throws LaiKeAPIException;


}
