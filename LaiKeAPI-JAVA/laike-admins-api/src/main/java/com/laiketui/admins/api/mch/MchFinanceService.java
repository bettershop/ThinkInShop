package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Withdrawals1Vo;
import com.laiketui.domain.vo.mch.FinanceVo;
import com.laiketui.domain.vo.user.AddBankVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 资金管理
 *
 * @author Trick
 * @date 2021/6/7 9:39
 */
public interface MchFinanceService
{


    /**
     * 提现明细
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/7 9:45
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 提现明细
     *
     * @param vo        -
     * @param status    -
     * @param startDate -
     * @param endDate   -
     * @param response  -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/7 10:35
     */
    Map<String, Object> withdrawList(MainVo vo, Integer status, Integer id, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 账户收支记录列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/10 16:05
     */
    Map<String, Object> revenueRecords(FinanceVo vo) throws LaiKeAPIException;

    /**
     * 提现页面
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/7 10:55
     */
    Map<String, Object> withdrawPage(MainVo vo) throws LaiKeAPIException;


    /**
     * 申请提现
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/7 11:13
     */
    Map<String, Object> withdrawals1(Withdrawals1Vo vo) throws LaiKeAPIException;


    /**
     * 发送短信
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/23 16:37
     */
    Map<String, Object> withdrawalsSms(MainVo vo) throws LaiKeAPIException;


    /**
     * 银行卡列表
     *
     * @param vo        -
     * @param id        -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/7 16:35
     */
    Map<String, Object> bankList(MainVo vo, Integer id, String startDate, String endDate) throws LaiKeAPIException;

    /**
     * 添加银行卡
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/7 17:01
     */
    void addBank(AddBankVo vo) throws LaiKeAPIException;

    /**
     * 设置默认银行卡
     *
     * @param vo     -
     * @param bankId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/20 16:20
     */
    void setDefault(MainVo vo, int bankId) throws LaiKeAPIException;

    /**
     * 验证银行卡与银行名称是否匹配
     *
     * @param vo             -
     * @param bankName       -
     * @param bankCardNumber -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/7 17:04
     */
    @Deprecated
    void verificationBank(MainVo vo, String bankName, String bankCardNumber) throws LaiKeAPIException;

    /**
     * 删除银行卡
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/7 17:22
     */
    void delBank(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 获取售后明细
     *
     * @param vo        -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/20 10:27
     */
    Map<String, Object> returnDetail(MainVo vo, String startDate, String endDate) throws LaiKeAPIException;

    /**
     * 保证金管理,获取记录
     *
     * @param vo
     * @param startDate
     * @param endDate
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023/11/14
     */
    Map<String, Object> PromiseList(MainVo vo, String startDate, String endDate, HttpServletResponse response, String type, String proStatus) throws LaiKeAPIException;

    /**
     * 添加保证金审核
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023/11/14
     */
    void insertPromisePrice(MainVo vo);

    /**
     * 验证银行卡与银行名称是否匹配
     *
     * @param vo             -
     * @param bankName       -
     * @param bankCardNumber -
     * @return Map
     * @throws LaiKeAPIException -
     * @author gp
     * @date 2023/11/21 18:21
     */
    Map<String, Object> MchVerificationBank(MainVo vo, String bankName, String bankCardNumber) throws LaiKeAPIException;
}
