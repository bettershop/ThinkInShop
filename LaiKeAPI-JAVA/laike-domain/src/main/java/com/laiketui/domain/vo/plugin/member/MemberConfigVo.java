package com.laiketui.domain.vo.plugin.member;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 会员配置
 *
 * @author sunH
 * @date 2022/06/29 16:06
 */
@ApiModel(description = "会员配置")
public class MemberConfigVo extends MainVo
{

    @ApiModelProperty(name = "isOpen", value = "会员插件开关 0.关 1.开")
    private Integer    isOpen;
    @ApiModelProperty(value = "用户端入口是否展示 0.否 1.是")
    private Integer    userEntrance;
    @ApiModelProperty(name = "openConfig", value = "开通设置")
    private String     openConfig;
    @ApiModelProperty(name = "birthdayOpen", value = "会员生日特权开关 0.关 1.开")
    private Integer    birthdayOpen;
    @ApiModelProperty(name = "pointsMultiple", value = "积分倍数")
    private Integer    pointsMultiple;
    @ApiModelProperty(name = "bonusPointsOpen", value = "会员赠送积分开关 0.关 1.开")
    private Integer    bonusPointsOpen;
    @ApiModelProperty(name = "bonusPointsConfig", value = "会员赠送积分设置")
    private String     bonusPointsConfig;
    @ApiModelProperty(name = "memberDiscount", value = "会员打折率")
    private BigDecimal memberDiscount;
    @ApiModelProperty(name = "renewOpen", value = "续费提醒开关 0.关 1.开")
    private Integer    renewOpen;
    @ApiModelProperty(name = "renewDay", value = "开始续费提醒天数")
    private Integer    renewDay = 7;
    @ApiModelProperty(name = "memberEquity", value = "会员权益设置")
    private String     memberEquity;

    public Integer getUserEntrance()
    {
        return userEntrance;
    }

    public void setUserEntrance(Integer userEntrance)
    {
        this.userEntrance = userEntrance;
    }

    public Integer getIsOpen()
    {
        return isOpen;
    }

    public void setIsOpen(Integer isOpen)
    {
        this.isOpen = isOpen;
    }

    public String getOpenConfig()
    {
        return openConfig;
    }

    public void setOpenConfig(String openConfig)
    {
        this.openConfig = openConfig;
    }

    public Integer getBirthdayOpen()
    {
        return birthdayOpen;
    }

    public void setBirthdayOpen(Integer birthdayOpen)
    {
        this.birthdayOpen = birthdayOpen;
    }

    public Integer getPointsMultiple()
    {
        return pointsMultiple;
    }

    public void setPointsMultiple(Integer pointsMultiple)
    {
        this.pointsMultiple = pointsMultiple;
    }

    public Integer getBonusPointsOpen()
    {
        return bonusPointsOpen;
    }

    public void setBonusPointsOpen(Integer bonusPointsOpen)
    {
        this.bonusPointsOpen = bonusPointsOpen;
    }

    public String getBonusPointsConfig()
    {
        return bonusPointsConfig;
    }

    public void setBonusPointsConfig(String bonusPointsConfig)
    {
        this.bonusPointsConfig = bonusPointsConfig;
    }

    public BigDecimal getMemberDiscount()
    {
        return memberDiscount;
    }

    public void setMemberDiscount(BigDecimal memberDiscount)
    {
        this.memberDiscount = memberDiscount;
    }

    public Integer getRenewOpen()
    {
        return renewOpen;
    }

    public void setRenewOpen(Integer renewOpen)
    {
        this.renewOpen = renewOpen;
    }

    public Integer getRenewDay()
    {
        return renewDay;
    }

    public void setRenewDay(Integer renewDay)
    {
        this.renewDay = renewDay;
    }

    public String getMemberEquity()
    {
        return memberEquity;
    }

    public void setMemberEquity(String memberEquity)
    {
        this.memberEquity = memberEquity;
    }
}
