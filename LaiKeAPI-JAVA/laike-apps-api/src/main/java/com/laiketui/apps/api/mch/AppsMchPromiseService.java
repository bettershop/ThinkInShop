package com.laiketui.apps.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 保证金
 *
 * @author Trick
 * @date 2021/10/26 10:41
 */
public interface AppsMchPromiseService
{
    /**
     * 缴纳保证金页面
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 10:43
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 是否缴纳保证金
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 10:49
     */
    boolean isPromisePay(MainVo vo) throws LaiKeAPIException;

    /**
     * 店铺是否可以操作
     *
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    boolean examineStatus(int mchId) throws LaiKeAPIException;

    /**
     * 获取支付方式列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/27 10:51
     */
    Map<String, Object> getPaymentList(MainVo vo) throws LaiKeAPIException;

    /**
     * 支付保证金/下单
     *
     * @param vo      -
     * @param payType - 支付方式 wallet_pay=钱包支付...
     * @param pwd     -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 10:47
     */
    Map<String, Object> payment(MainVo vo, String payType, String pwd) throws LaiKeAPIException;

    /**
     * 保证金管理页面
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 10:51
     */
    Map<String, Object> promiseManage(MainVo vo) throws LaiKeAPIException;

    /**
     * 保证金记录页面
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2023/02/21 10:51
     */
    Map<String, Object> promiseList(MainVo vo) throws LaiKeAPIException;


    /**
     * 退还保证金
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 10:52
     */
    void returnPromisePrice(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加保证金审核
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 10:52
     */
    void insertPromisePrice(MainVo vo) throws LaiKeAPIException;

    /**
     * 保证金审核列表
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 10:52
     */
    Map<String, Object> selectPromisePrice(MainVo vo, String title) throws LaiKeAPIException;

    /**
     * 保证金审核
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 10:52
     */
    void passOrRefused(MainVo vo, int id, int isPass, String refusedWhy) throws LaiKeAPIException;

    /**
     * 保证金删除
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 10:52
     */
    void deletePromisePrice(MainVo vo, int id) throws LaiKeAPIException;


}
