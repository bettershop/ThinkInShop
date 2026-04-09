package com.laiketui.apps.api.mchson;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.son.LoginUserVo;
import com.laiketui.domain.vo.order.AdminOrderListVo;

import java.util.Map;

/**
 * 门店核销登录Pc端api
 * gp
 * 2024-02-01
 */
public interface AppsPcMchSonService
{

    /**
     * 获取店铺门店列表
     *
     * @param vo
     * @param mch_id
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getStore(MainVo vo, String mch_id) throws LaiKeAPIException;


    /**
     * 门店核销登录
     *
     * @param vo -
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> LoginUser(LoginUserVo vo) throws LaiKeAPIException;

    /**
     * 核销订单列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getOrderList(AdminOrderListVo vo) throws LaiKeAPIException;


    /**
     * 获取核销订单详情
     *
     * @param vo
     * @param sNo -订单号
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> editeOrderInfo(MainVo vo, String sNo) throws LaiKeAPIException;

    /**
     * 验证码提货（自提）获取其商品详情
     *
     * @param vo
     * @param orderId
     * @param extractionCode
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getGoodsInfoByExtractionCode(MainVo vo, Integer orderId, String extractionCode) throws LaiKeAPIException;

    /**
     * 订单核销
     *
     * @param vo
     * @param orderId        -订单id
     * @param extractionCode -核销码
     * @throws LaiKeAPIException
     */
    Map<String, Object> verificationExtractionCode(MainVo vo, Integer orderId, String extractionCode) throws LaiKeAPIException;

    /**
     * 门店核销统计
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> statistics(MainVo vo) throws LaiKeAPIException;

    /**
     * 退出登录
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void userExit(MainVo vo) throws LaiKeAPIException;

    /**
     * 标记公告以读
     *
     * @param vo
     * @param tell_id 公告id
     */
    void markToRead(MainVo vo, Integer tell_id) throws LaiKeAPIException;

    /**
     * 获取平台维护公告
     *
     * @param vo
     * @return
     */
    Map<String, Object> getUserTell(MainVo vo);
}
