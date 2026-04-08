package com.laiketui.domain.vo.plugin.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 开通设置(会员设置)
 *
 * @author sunH
 * @date 2022/06/29 16:06
 */
@ApiModel(description = "开通设置(会员设置)")
public class OpenConfigVo
{
    @ApiModelProperty(name = "openMethodName", value = "开通方式名称")
    private String     openMethodName;
    @ApiModelProperty(name = "openMethod", value = "开通方式")
    private String     openMethod;
    @ApiModelProperty(name = "price", value = "价格")
    private BigDecimal price;
    @ApiModelProperty(name = "points", value = "积分")
    private Integer    points;

    @ApiModelProperty(name = "day", value = "天数")
    private Integer    day;
    @ApiModelProperty(name = "priceForDay", value = "价格/天")
    private BigDecimal priceForDay;

    public Integer getPoints()
    {
        return points;
    }

    public void setPoints(Integer points)
    {
        this.points = points;
    }

    public String getOpenMethodName()
    {
        return openMethodName;
    }

    public void setOpenMethodName(String openMethodName)
    {
        this.openMethodName = openMethodName;
    }

    public String getOpenMethod()
    {
        return openMethod;
    }

    public void setOpenMethod(String openMethod)
    {
        this.openMethod = openMethod;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public Integer getDay()
    {
        return day;
    }

    public void setDay(Integer day)
    {
        this.day = day;
    }

    public BigDecimal getPriceForDay()
    {
        return priceForDay;
    }

    public void setPriceForDay(BigDecimal priceForDay)
    {
        this.priceForDay = priceForDay;
    }
}
