package com.laiketui.domain.vo.user;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加/修改用户配置信息参数
 *
 * @author Trick
 * @date 2021/1/8 18:25
 */
@ApiModel(description = "添加/修改用户配置信息参数")
public class AddUserRuleVo extends MainVo
{

    @ApiModelProperty(value = "微信头像", name = "wxImgUrl")
    private String wxImgUrl;
    @ApiModelProperty(value = "微信名称", name = "wxName")
    private String wxName;


    public String getWxImgUrl()
    {
        return wxImgUrl;
    }

    public void setWxImgUrl(String wxImgUrl)
    {
        this.wxImgUrl = wxImgUrl;
    }

    public String getWxName()
    {
        return wxName;
    }

    public void setWxName(String wxName)
    {
        this.wxName = wxName;
    }
}
