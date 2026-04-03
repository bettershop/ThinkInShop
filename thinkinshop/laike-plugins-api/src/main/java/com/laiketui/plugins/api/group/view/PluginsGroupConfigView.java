package com.laiketui.plugins.api.group.view;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 拼团设置视图
 *
 * @author Trick
 * @date 2023/3/15 10:53
 */
@Data
public class PluginsGroupConfigView
{

    @ApiModelProperty(value = "主键")
    private Integer id;
    @ApiModelProperty(value = "成团倒计时(秒) 0=不计时")
    private Integer endTime = 0;
    @ApiModelProperty(value = "开团限制 0=不限制")
    private Integer openLimit;
    @ApiModelProperty(value = "参团限制 0=不限制")
    private Integer joinLimit;
    @ApiModelProperty(value = "规则说明")
    private String  rule;

}
