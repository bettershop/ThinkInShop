package com.laiketui.apps.api.app.services;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Withdrawals1Vo;

import java.util.Map;

/**
 * 分销中心
 *
 * @author Trick
 * @date 2021/2/9 9:27
 */
public interface AppsCstrAppDistributionService
{


    /**
     * 获取我的分销信息
     * <p>
     * 【php commcenter.index】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 9:36
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;


    /**
     * 获取我的团队
     * <p>
     * 【php commcenter.mygroup】
     *
     * @param vo     -
     * @param userId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 11:27
     */
    Map<String, Object> mygroup(MainVo vo, String userId) throws LaiKeAPIException;


    /**
     * 获取佣金明细
     * <p>
     * 【php commcenter.comm_list】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 14:30
     */
    Map<String, Object> commList(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取提现明细列表
     * <p>
     * 【php commcenter.cash_list】
     *
     * @param vo     -
     * @param status -
     * @param id     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 14:54
     */
    Map<String, Object> cashList(MainVo vo, Integer status, Integer id) throws LaiKeAPIException;

    /**
     * 获取提现详情
     * <p>
     * 【php commcenter.cash_detail】
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 14:54
     */
    Map<String, Object> cashDetail(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 获取提现页面数据
     * <p>
     * 【php commcenter.intoWithdrawals】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 15:28
     */
    Map<String, Object> intoWithdrawals(MainVo vo) throws LaiKeAPIException;


//    /**
//     * 提现申请
//     * <p>
//     * 【php commcenter.withdrawals】
//     *
//     * @param vo     -
//     * @param pcode  -
//     * @param amt    - 提现金额
//     * @param bankId - 银行卡id
//     * @return Map
//     * @throws LaiKeAPIException -
//     * @author Trick
//     * @date 2021/2/9 16:26
//     */
//    Map<String, Object> withdrawalsApply(MainVo vo, String pcode, String amt, int bankId) throws LaiKeAPIException;

    /**
     * 提现申请
     * <p>
     * 【php commcenter.withdrawals】
     *
     * @param vo     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 16:26
     */
    Map<String, Object> withdrawalsApply(Withdrawals1Vo vo) throws LaiKeAPIException;

    /**
     * 获取佣金排行榜
     * <p>
     * 【php commcenter.getRanking】
     *
     * @param vo   -
     * @param id   -
     * @param type -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 14:25
     */
    Map<String, Object> getRanking(MainVo vo, Integer id, int type) throws LaiKeAPIException;


    /**
     * 推广订单
     * 【php commcenter.promote_order】
     *
     * @param vo     -
     * @param search -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/18 13:41
     */
    Map<String, Object> promoteOrder(MainVo vo, String search, String userId) throws LaiKeAPIException;


    /**
     * 获取礼包列表
     * 【php commproduct.getstart】
     *
     * @param vo            -
     * @param productTitle  -
     * @param distributorId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/18 17:26
     */
    Map<String, Object> getStart(MainVo vo, String productTitle, Integer distributorId) throws LaiKeAPIException;


    /**
     * 获取分销商品列表
     * 【php commproduct.listdetail】
     *
     * @param vo           -
     * @param productTitle -
     * @param sortKey      -
     * @param queue        -
     * @param cid          -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/18 17:26
     */
    Map<String, Object> getGoodsList(MainVo vo, String productTitle, String sortKey, int queue, Integer cid) throws LaiKeAPIException;

    /**
     * 获取分销商品所有类别
     * 【php commproduct.getclass】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/19 9:15
     */
    Map<String, Object> getClass(MainVo vo) throws LaiKeAPIException;
}
