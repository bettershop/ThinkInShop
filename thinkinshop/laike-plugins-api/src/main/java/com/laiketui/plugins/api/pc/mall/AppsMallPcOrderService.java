package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.order.RefundDetailsVo;

import java.util.Map;

public interface AppsMallPcOrderService
{

    /**
     * pc商城结算
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/2/17 13:59
     */
    Map<String, Object> settlement(OrderVo vo) throws LaiKeAPIException;

    /**
     * 验证支付密码
     *
     * @param vo       -
     * @param password -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022-02-17 15:51:54
     */
    boolean paymentPassword(MainVo vo, String password) throws LaiKeAPIException;


    /**
     * 立即购买
     *
     * @param vo          -
     * @param productJson - 商品信息集
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022-02-23 16:21:39
     */
    void immediatelyCart(MainVo vo, String productJson) throws LaiKeAPIException;

    /**
     * 获取自提地址
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/3/2 10:53
     */
    Map<String, Object> getMchStore(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 售后订单详情
     *
     * @param refundDetailsVo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/5/26 16:10
     */
    Map<String, Object> returndetails(RefundDetailsVo refundDetailsVo) throws LaiKeAPIException;

    /**
     * 获取订单商品评论信息
     *
     * @param
     * @return
     */
    Map<String, Object> getCommentsInfo(MainVo vo, Integer orderDetailId, Integer goodsId) throws LaiKeAPIException;
}
