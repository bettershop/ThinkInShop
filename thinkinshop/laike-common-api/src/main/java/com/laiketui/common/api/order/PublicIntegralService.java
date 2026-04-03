package com.laiketui.common.api.order;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.plugin.integral.AddScoreVo;

import java.math.BigDecimal;

/**
 * 积分相关
 *
 * @author Trick
 * @date 2021/11/9 17:49
 */
public interface PublicIntegralService
{

    /**
     * 增减积分
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/9 17:50
     */
    void addScore(AddScoreVo vo) throws LaiKeAPIException;

    /**
     * 赠送积分
     *
     * @param storeId    -
     * @param userId     -
     * @param orderPrice -
     * @param orderNo    -
     * @param userScore  - 用户当前积分
     * @param type       - 0 = 收货后  1=支付后
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/10 15:01
     */
    void giveScore(int storeId, String userId, String orderNo, BigDecimal orderPrice, int userScore, int type,Integer self_lifting) throws LaiKeAPIException;
}
