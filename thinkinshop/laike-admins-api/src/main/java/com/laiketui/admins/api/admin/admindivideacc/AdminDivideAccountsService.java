package com.laiketui.admins.api.admin.admindivideacc;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.divideAccount.DivideAccountVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 11:31 2023/9/14
 */
public interface AdminDivideAccountsService
{

    /**
     * 获取店铺分账信息
     *
     * @param vo
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> divideAccountInfo(MainVo vo, Integer mchId) throws LaiKeAPIException;

    /**
     * 保存店铺分账信息
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void saveDivideAccount(DivideAccountVo vo) throws LaiKeAPIException;

    /**
     * 申请分账账单
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> applyBilling(MainVo vo, String date) throws LaiKeAPIException;

    /**
     * 下载分账账单
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    String downloadBilling(MainVo vo, String url, HttpServletResponse httpServletResponse) throws LaiKeAPIException;

    /**
     * 分账记录
     *
     * @param vo
     * @param mchId
     * @param condition
     * @param startDate
     * @param endDate
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> divideRecord(MainVo vo, Integer mchId, String condition, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException;

}
