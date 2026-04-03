package com.laiketui.plugins.api.auction.pay;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.pay.PaymentVo;

import java.util.Map;

/**
 * 竞拍支付
 *
 * @author Trick
 * @date 2022/8/9 17:27
 */
public interface PluginAuctionPayService
{
    /**
     * 支付保证金
     *
     * @param vo -
     * @return Map -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/9 17:28
     */
    Map<String, Object> payPromise(PaymentVo vo) throws LaiKeAPIException;

    /**
     * 保证金退款
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/15 9:53
     */
    Map<String, Object> backPromise(PaymentVo vo) throws LaiKeAPIException;

    /**
     * 竞拍订单支付成功回调
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/10 17:37
     */
    Map<String, Object> payCallBack(PaymentVo vo) throws LaiKeAPIException;
}
