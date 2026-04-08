package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.user.AddBankVo;

import java.util.List;
import java.util.Map;

/**
 * 银行卡
 *
 * @author Trick
 * @date 2021/6/18 16:23
 */
public interface AppsMallBankService
{


    /**
     * 获取我的银行卡列表
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author List
     * @date 2021/5/19 17:57
     */
    List<Map<String, Object>> bankList(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 添加银行卡
     * 【php MchBank.add_bank】
     *
     * @param vo    -
     * @param isMch -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/5 15:42
     */
    void addBank(AddBankVo vo, boolean isMch) throws LaiKeAPIException;

    /**
     * 解绑银行卡
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/27 19:21
     */
    void delBank(MainVo vo, int bankId) throws LaiKeAPIException;

    /**
     * 验证银行卡与银行名称是否匹配
     *
     * @param vo             -
     * @param bankName       -
     * @param bankCardNumber -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 18:21
     */
    Map<String, Object> verificationBank(MainVo vo, String bankName, String bankCardNumber) throws LaiKeAPIException;

    /**
     * 设置默认银行卡
     *
     * @param vo     -
     * @param bankId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/18 16:32
     */
    void setDefault(MainVo vo, int bankId) throws LaiKeAPIException;
}
