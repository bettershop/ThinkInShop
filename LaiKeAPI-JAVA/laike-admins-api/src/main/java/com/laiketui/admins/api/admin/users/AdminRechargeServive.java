package com.laiketui.admins.api.admin.users;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.vo.SaveAddressVo;
import com.laiketui.domain.vo.user.RechargeVo;
import com.laiketui.domain.vo.user.UserMoneyVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 充值列表
 *
 * @author Trick
 * @date 2021/1/11 15:49
 */
public interface AdminRechargeServive
{


    /**
     * 获取充值列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 15:50
     */
    Map<String, Object> getRechargeInfo(RechargeVo vo) throws LaiKeAPIException;

    /**
     * 获取充值列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 15:50
     */
    Map<String, Object> getupInfo(RechargeVo vo, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 获取用户资金列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:13
     */
    Map<String, Object> getUserMoneyInfo(UserMoneyVo vo, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 获取用户积分列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:13
     */
    Map<String, Object> getUserIntegralInfo(UserMoneyVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 获取用户资金详情列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:13
     */
    Map<String, Object> getUserMoneyInfo_see(UserMoneyVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 获取用户积分详情列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:13
     */
    Map<String, Object> getUserIntegralInfo_see(UserMoneyVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 保存地址
     *
     * @param vo     -
     * @param userId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 17:06
     */
    UserAddress saveAddress(SaveAddressVo vo, String userId) throws LaiKeAPIException;

    /**
     * 获取区号
     * @param keyword
     * @return
     */
    List<Map<String, Object>> getItuList(String keyword);

    /**
     * 生成账号
     * @return
     */
    String generateAccount();
}
