package com.laiketui.apps.api.mchson;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.son.LoginUserVo;

import java.util.Map;

/**
 * 门店核销登录移动端api
 * gp
 * 2024-02-01
 */
public interface AppsAppMchSonService
{

    /**
     * 获取店铺门店列表
     *
     * @param vo
     * @param mch_id
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getStore(MainVo vo, Integer mch_id) throws LaiKeAPIException;

    /**
     * 门店核销登录
     *
     * @param vo -
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> LoginUser(LoginUserVo vo) throws LaiKeAPIException;

    /**
     * 门店核销主页数据
     *
     * @param vo -
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> appHome(MainVo vo) throws LaiKeAPIException;

    /**
     * 订单列表
     *
     * @param vo
     * @return
     */
    Map<String, Object> orderIndex(MainVo vo, Integer type, String otype, String keyWord) throws LaiKeAPIException;

    /**
     * 订单核销
     *
     * @param vo
     * @param orderId        -订单id
     * @param extractionCode -核销码
     * @throws LaiKeAPIException
     */
    Map<String, Object> orderInfoForCode(MainVo vo, Integer orderId, String extractionCode) throws LaiKeAPIException;

    /**
     * 只通过核销码来进行订单核销
     *
     * @param vo
     * @param extractionCode -核销码
     * @throws LaiKeAPIException
     */
    Map<String, Object> orderInfoByCode(MainVo vo, String orderId, String extractionCode) throws LaiKeAPIException;

    /**
     * 订单核销
     *
     * @param vo
     * @param orderId        -订单id
     * @param extractionCode -核销码
     * @throws LaiKeAPIException
     */
    Map<String, Object> verificationExtractionCode(MainVo vo, Integer orderId, String extractionCode, Integer pid) throws LaiKeAPIException;


    /**
     * 退出登录
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void userExit(MainVo vo) throws LaiKeAPIException;

    /**
     * 核销订单详情
     *
     * @param vo
     * @param sNo
     * @return
     */
    Map<String, Object> EditeOrderInfo(MainVo vo, String sNo);


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

    /**
     * 查询核销记录
     *
     * @param id
     * @return
     */
    Map<String, Object> getWriteRecord(Integer id);

    /**
     * 查询可核销门店
     *
     * @param vo
     * @param mchId
     * @return
     */
    Map<String, Object> getMchStore(MainVo vo, Integer mchId);
}
