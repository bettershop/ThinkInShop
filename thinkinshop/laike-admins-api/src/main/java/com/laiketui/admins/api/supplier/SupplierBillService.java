package com.laiketui.admins.api.supplier;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.supplier.ApplyWithdrawalVo;
import com.laiketui.domain.vo.user.AddBankVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 14:57 2022/10/9
 * 账单管理
 */
public interface SupplierBillService
{

    /**
     * 提现记录
     *
     * @param vo
     * @param status
     * @param startTime
     * @param endTime
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> withdraw(MainVo vo, Integer status, String startTime, String endTime, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 提现页面
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2022/10/09 11:13
     */
    Map<String, Object> withdrawPage(MainVo vo) throws LaiKeAPIException;

    /**
     * 申请提现
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2022/10/09 11:13
     */
    void apply(ApplyWithdrawalVo vo) throws LaiKeAPIException;

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
     * 获取银行卡列表
     *
     * @param vo
     * @param id
     * @param startTime
     * @param endTime
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> bankCard(MainVo vo, Integer id, String startTime, String endTime) throws LaiKeAPIException;

    /**
     * 添加/修改银行卡
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void addBankCard(AddBankVo vo) throws LaiKeAPIException;

    /**
     * 删除银行卡
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void delBankCard(MainVo vo, Integer id) throws LaiKeAPIException;

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
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> verificationBankName(MainVo vo, String bankName, String bankCardNumber) throws LaiKeAPIException;

    /**
     * 出入账记录
     *
     * @param vo
     * @param sNo
     * @param status
     * @param startTime
     * @param endTime
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> accountLog(MainVo vo, String sNo, Integer status, String startTime, String endTime) throws LaiKeAPIException;
}
