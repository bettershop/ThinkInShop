package com.laiketui.plugins.api.group.appMch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.AdminOrderListVo;
import com.laiketui.domain.vo.order.CommentsInfoVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.order.UpdateCommentsInfoVo;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 拼团订单管理
 * <p>
 * gp
 * 2023-07-10
 */
public interface PluginsGroupAppMchOrderService
{
    /**
     * 订单首页
     *
     * @param adminOrderVo -
     * @param response     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/10/20 15:02
     */
    Map<String, Object> index(AdminOrderListVo adminOrderVo, HttpServletResponse response) throws LaiKeAPIException;

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
     * 售后退款通过特殊处理
     *
     * @param refundId -  售后id
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/20 15:09
     */
    void refundSuccessBack(Integer refundId) throws LaiKeAPIException;

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
    Map<String, Object> getCommentsDetailInfoById(GetCommentsDetailInfoVo vo) throws LaiKeAPIException;

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
     * 搜索快递公司
     *
     * @param express
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> searchExpress(String express) throws LaiKeAPIException;

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
    Map<String, Object> returnOrderDetails(MainVo vo, int id) throws LaiKeAPIException;
}
