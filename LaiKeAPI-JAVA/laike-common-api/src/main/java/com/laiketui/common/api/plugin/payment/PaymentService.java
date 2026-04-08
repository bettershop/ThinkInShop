package com.laiketui.common.api.plugin.payment;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.pay.PayCallBackVo;
import com.laiketui.domain.vo.pay.PayVo;

import java.util.Map;

/**
 * 通用支付接口
 *
 * @author Trick
 * @date 2021/4/1 9:57
 */
public interface PaymentService
{

    /**
     * 支付
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/1 9:58
     */
    Map<String, Object> payment(PayVo vo) throws LaiKeAPIException;

    /**
     * 支付前数据校验
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/1 10:07
     */
    boolean payValidata(PayVo vo) throws LaiKeAPIException;

    /**
     * 支付回调
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/1 9:58
     */
    Map<String, Object> callBack(PayCallBackVo vo) throws LaiKeAPIException;


}
