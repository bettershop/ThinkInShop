package com.laiketui.plugins.api.flashsale.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.flashsale.FlashLabelVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.mch.MchOrderIndexVo;
import com.laiketui.domain.vo.order.AdminOrderDetailVo;
import com.laiketui.domain.vo.order.CommentsInfoVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.order.RefundQueryVo;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author sjw
 * @create 2024/6/18
 */
public interface PluginsFlashSaleMchDataService
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
    Map<String, Object> getLabelList(MainVo vo, String status, String startDate, String endDate) throws LaiKeAPIException;

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
     * 移动店铺端-进入管理页面数据查询
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> manage(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加修改活动
     *
     * @param addLabelVo
     * @throws LaiKeAPIException
     */
    void addAndUpdateLabel(FlashLabelVo addLabelVo) throws LaiKeAPIException;

    /**
     * 获取商品详情
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    Map<String, Object> getProDetail(MainVo vo, Integer goodId);

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
     * 加购商品列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getFsConfig(MainVo vo, String key) throws LaiKeAPIException;

    /**
     * 保存活动设置
     *
     * @param vo
     * @param goodsJson
     * @throws LaiKeAPIException
     */
    void setFsConfig(MainVo vo, String goodsJson, Integer id) throws LaiKeAPIException;

    /**
     * 订单列表
     *
     * @param vo
     * @param
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getFlashSalOrderList(MchOrderIndexVo vo) throws LaiKeAPIException;

    /**
     * 跳转编辑订单
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> Order_details(MainVo vo, int shopId, String sNo) throws LaiKeAPIException;

    /**
     * 保存编辑订单
     *
     * @param orderVo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/8/2 10:21
     */
    void saveEditOrder(MainVo vo, int shopId, String sNo, String orderDetail) throws LaiKeAPIException;

    /**
     * 删除订单
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 17:07
     */
    void delOrder(MainVo vo, int shopId, String sNo) throws LaiKeAPIException;

    /**
     * 获取发货物流公司信息
     *
     * @param vo
     * @param express
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> GetLogistics(MainVo vo, String express) throws LaiKeAPIException;

    /**
     * 订单发货列表
     *
     * @param vo      -
     * @param orderNo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/3 17:19
     */
    Map<String, Object> deliverList(MainVo vo, String orderNo) throws LaiKeAPIException;

    /**
     * 订单发货
     *
     * @param vo          -
     * @param sNo         -
     * @param expressId   -
     * @param courierNum  -
     * @param orderListId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/3 17:49
     */
    Map<String, Object> deliver(MainVo vo, String sNo, Integer expressId,
                                String courierNum, String orderListId) throws LaiKeAPIException;

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
     * 平台订单详情
     *
     * @param orderVo
     * @return
     */
    Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo);

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
     * 获取评论列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 16:07
     */
    Map<String, Object> getCommentsInfo(CommentsInfoVo vo) throws LaiKeAPIException;

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
    Map<String, Object> getCommentsDetailInfoById(GetCommentsDetailInfoVo vo) throws LaiKeAPIException;

    /**
     * 获取评论详情回复列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/6 14:44
     */
    Map<String, Object> getCommentReplyList(GetCommentsDetailInfoVo vo) throws LaiKeAPIException;

    /**
     * 回复
     *
     * @param vo          -
     * @param commentId   -
     * @param commentText -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 9:30
     */
    boolean replyComments(MainVo vo, int commentId, String commentText) throws LaiKeAPIException;

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
    Map<String, Object> getRefundById(MainVo vo, int shopId, String sNo, int id) throws LaiKeAPIException;

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
     * 删除加购商品
     *
     * @param cid
     * @return
     * @throws LaiKeAPIException
     */
    void deleteFsConfig(MainVo vo, Integer id);

    /**
     * 删除折扣商品
     *
     * @param cid
     * @return
     * @throws LaiKeAPIException
     */
    void deleteFsLabelGoods(MainVo vo, Integer id);

    /**
     * 折扣商品编辑设置单个商品
     *
     * @param cid
     * @return
     * @throws LaiKeAPIException
     */
    void updateFsLabelGoodsConfig(MainVo vo, Integer id, BigDecimal discount, Integer buylimit);

    /**
     * 根据id获取限时折扣商品信息
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getGoodsInfoById(MainVo vo, Integer goodsId);
}
