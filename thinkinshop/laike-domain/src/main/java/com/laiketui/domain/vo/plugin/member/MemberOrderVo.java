package com.laiketui.domain.vo.plugin.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * @Author: sunH_
 * @Date: Create in 14:18 2022/7/4
 */
@ApiModel(description = "会员订单信息")
public class MemberOrderVo
{
    @ApiModelProperty(name = "storeId", value = "商城id")
    private Integer    storeId;
    @ApiModelProperty(name = "userId", value = "用户id")
    private String     userId;
    @ApiModelProperty(name = "amount", value = "订单金额")
    private BigDecimal amount;
    @ApiModelProperty(name = "memberType", value = "会员类型 1.月卡 2.季卡 3.年卡")
    private Integer    memberType;
    @ApiModelProperty(name = "startTime", value = "开始时间")
    private String     startTime;
    @ApiModelProperty(name = "endTime", value = "结束时间")
    private String     endTime;
    @ApiModelProperty(name = "payType", value = "支付方式")
    private String     payType;
    @ApiModelProperty(name = "couponId", value = "用户优惠券id")
    private String     couponId;
    @ApiModelProperty(name = "is_renew", value = "是否是续费会员")
    private Integer    is_renew;

    public Integer getStoreId()
    {
        return storeId;
    }

    public Integer getIs_renew()
    {
        return is_renew;
    }

    public void setIs_renew(Integer is_renew)
    {
        this.is_renew = is_renew;
    }

    public void setStoreId(Integer storeId)
    {
        this.storeId = storeId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public Integer getMemberType()
    {
        return memberType;
    }

    public void setMemberType(Integer memberType)
    {
        this.memberType = memberType;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    public String getPayType()
    {
        return payType;
    }

    public void setPayType(String payType)
    {
        this.payType = payType;
    }

    public String getCouponId()
    {
        return couponId;
    }

    public void setCouponId(String couponId)
    {
        this.couponId = couponId;
    }
}
