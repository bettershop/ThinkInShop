package com.laiketui.domain.vo.plugin.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 会员权益(会员设置)
 *
 * @author sunH
 * @date 2022/06/29 16:06
 */
@ApiModel(description = "会员权益(会员设置)")
public class MemberEquityVo
{
    @ApiModelProperty(name = "equityName", value = "会员权益名称")
    private String equityName;
    @ApiModelProperty(name = "englishName", value = "英文名称")
    private String englishName;
    @ApiModelProperty(name = "icon", value = "图标")
    private String icon;

    public String getEquityName()
    {
        return equityName;
    }

    public void setEquityName(String equityName)
    {
        this.equityName = equityName;
    }

    public String getEnglishName()
    {
        return englishName;
    }

    public void setEnglishName(String englishName)
    {
        this.englishName = englishName;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }
}
