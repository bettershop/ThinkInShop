package com.laiketui.common.api.plugin.member;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.plugin.member.BuyMemberVo;
import com.laiketui.domain.vo.plugin.member.MemberOrderVo;

import java.math.BigDecimal;

/**
 * @Author: sunH_
 * @Date: Create in 19:08 2022/7/5
 */
public interface PubliceMemberService
{

    /**
     * 充会员送积分
     *
     * @param user
     * @param memberType
     * @param storeId
     * @param orderDataModel
     * @throws LaiKeAPIException
     */
    void sendPoints(User user, Integer memberType, Integer storeId, OrderDataModel orderDataModel) throws LaiKeAPIException;

    /**
     * 会员生日特权奖励
     *
     * @param userId
     * @param storeId
     * @param price
     * @param sNo
     * @throws LaiKeAPIException
     */
    void doublePoints(String userId, Integer storeId, BigDecimal price, String sNo) throws LaiKeAPIException;

    /**
     * 会员价格
     *
     * @param storeId
     * @param price
     * @return
     * @throws LaiKeAPIException
     */
    BigDecimal vipPrice(Integer storeId, BigDecimal price) throws LaiKeAPIException;

    /**
     * 生成orderData
     *
     * @param user
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    MemberOrderVo getOrderData(User user, BuyMemberVo vo) throws LaiKeAPIException;

}
