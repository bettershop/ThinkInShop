package com.laiketui.domain.vo.plugin.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 会员赠送积分设置(会员设置)
 *
 * @author sunH
 * @date 2022/06/29 16:06
 */
@ApiModel(description = "会员赠送积分设置(会员设置)")
public class BonusPointsConfigVo
{
    @ApiModelProperty(name = "openMethod", value = "开通方式")
    private String  openMethod;
    @ApiModelProperty(name = "points", value = "积分")
    private Integer points;

    public String getOpenMethod()
    {
        return openMethod;
    }

    public void setOpenMethod(String openMethod)
    {
        this.openMethod = openMethod;
    }

    public Integer getPoints()
    {
        return points;
    }

    public void setPoints(Integer points)
    {
        this.points = points;
    }
}
