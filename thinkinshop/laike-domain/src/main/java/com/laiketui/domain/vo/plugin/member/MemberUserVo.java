package com.laiketui.domain.vo.plugin.member;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 会员用户
 *
 * @author sunH
 * @date 2022/06/29 16:06
 */
@ApiModel(description = "会员用户条件")
public class MemberUserVo extends MainVo
{
    @ApiModelProperty(name = "userId", value = "会员id")
    private String  userId;
    @ApiModelProperty(name = "phone", value = "手机号")
    private String  phone;
    @ApiModelProperty(name = "condition", value = "条件")
    private String  condition;
    @ApiModelProperty(name = "isOver", value = "是否过期 0.否 1.是")
    private Integer isOver;

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public Integer getIsOver()
    {
        return isOver;
    }

    public void setIsOver(Integer isOver)
    {
        this.isOver = isOver;
    }
}
