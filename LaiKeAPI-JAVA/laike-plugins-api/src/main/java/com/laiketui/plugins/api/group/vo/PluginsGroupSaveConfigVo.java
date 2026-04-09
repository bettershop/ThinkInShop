package com.laiketui.plugins.api.group.vo;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * 添加拼团设置参数
 *
 * @author Trick
 * @date 2020/11/4 17:27
 */
@ApiModel(description = "添加拼团设置参数")
public class PluginsGroupSaveConfigVo extends MainVo
{

    @ApiModelProperty(value = "主键")
    private Integer id;
    @ApiModelProperty(value = "成团倒计时(秒) 0=不计时")
    private Integer endTime;
    @ApiModelProperty(value = "开团限制 0=不限制")
    private Integer openLimit;
    @ApiModelProperty(value = "参团限制 0=不限制")
    private Integer joinLimit;
    @ApiModelProperty(value = "规则说明")
    private String  rule;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Integer endTime)
    {
        this.endTime = endTime;
    }

    public Integer getOpenLimit()
    {
        return openLimit;
    }

    public void setOpenLimit(Integer openLimit)
    {
        this.openLimit = openLimit;
    }

    public Integer getJoinLimit()
    {
        return joinLimit;
    }

    public void setJoinLimit(Integer joinLimit)
    {
        this.joinLimit = joinLimit;
    }

    public String getRule()
    {
        return rule;
    }

    public void setRule(String rule)
    {
        this.rule = rule;
    }
}
