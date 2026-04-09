package com.laiketui.plugins.api.auction.admin;

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
 * 竞拍订单
 *
 * @author Trick
 * @date 2021/10/20 15:02
 */
public interface PluginAuctionAdminOrderService
{
    /**
     * 订单首页
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
     * @param vo  -
     * @param ids -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 17:07
     */
    void delOrder(MainVo vo, String ids) throws LaiKeAPIException;

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

}
