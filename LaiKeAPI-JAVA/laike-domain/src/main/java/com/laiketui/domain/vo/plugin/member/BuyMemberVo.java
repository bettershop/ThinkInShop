package com.laiketui.domain.vo.plugin.member;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * @Author: sunH_
 * @Date: Create in 14:18 2022/7/4
 */
@ApiModel(description = "充值会员")
public class BuyMemberVo extends MainVo
{

    @ApiModelProperty(name = "memberType", value = "会员类型 1.月卡 2.季卡 3.年卡")
    private Integer    memberType;
    @ApiModelProperty(name = "payType", value = "支付方式")
    private String     payType;
    @ApiModelProperty(name = "couponId", value = "用户优惠券id")
    private String     couponId;
    @ApiModelProperty(name = "amount", value = "支付金额")
    private BigDecimal amount;

    public Integer getMemberType()
    {
        return memberType;
    }

    public void setMemberType(Integer memberType)
    {
        this.memberType = memberType;
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

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }
}
