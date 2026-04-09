package com.laiketui.common.utils.tool.plugin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.coupon.CouponModal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 优惠券数据处理
 *
 * @author Trick
 * @date 2021/9/10 14:48
 */
public class CouponDataUtils
{
    private static final Logger logger = LoggerFactory.getLogger(CouponDataUtils.class);

    /**
     * 获取优惠券类型
     *
     * @param couponType -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/10 14:49
     */
    public static String getCouponTypeName(String couponType) throws LaiKeAPIException
    {
        String typeName = "";
        try
        {
            switch (couponType)
            {
                case CouponModal.COUPON_TYPE_MJ:
                    typeName = "满减券";
                    break;
                case CouponModal.COUPON_TYPE_ZK:
                    typeName = "折扣券";
                    break;
                case CouponModal.COUPON_TYPE_MY:
                    typeName = "免邮券";
                    break;
                default:
                    typeName = "未知优惠券";
                    break;
            }
        }
        catch (Exception e)
        {
            logger.error("优惠券类型获取名称 异常  ", e);
            typeName = "";
        }
        return typeName;
    }

    public static String getCouponTypeName(int couponType) throws LaiKeAPIException
    {
        return getCouponTypeName(couponType + "");
    }
}
