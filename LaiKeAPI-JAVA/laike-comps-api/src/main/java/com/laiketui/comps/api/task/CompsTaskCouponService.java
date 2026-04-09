package com.laiketui.comps.api.task;

import com.laiketui.core.exception.LaiKeAPIException;

/**
 * 优惠卷定时任务接口
 *
 * @author Trick
 * @date 2020/12/11 13:46
 */
public interface CompsTaskCouponService
{


    /**
     * 定时处理优惠卷
     * 【php coupon.timing】
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/11 13:48
     */
    void couponTimingTask() throws LaiKeAPIException;

    /**
     * 系统自动发放优惠券
     *
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/20 15:50
     */
    void autoPushCoupon() throws LaiKeAPIException;
}
