package com.laiketui.plugins.api.group.view.h5;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 拼团列表 视图
 *
 * @author Trick
 * @date 2023-04-04 11:26:40
 */
@Data
public class PluginsGroupAppOpenInfoView
{
    @ApiModelProperty(value = "开团id", name = "openId")
    private String  openId;
    @ApiModelProperty(value = "活动id", name = "acId")
    private String  acId;
    @ApiModelProperty(value = "团长名称", name = "teamName")
    private String  teamName;
    @ApiModelProperty(value = "团长头像", name = "teamImg")
    private String  teamImg;
    @ApiModelProperty(value = "活动状态", name = "status")
    private Integer status;
    @ApiModelProperty(value = "团队人数", name = "teamNum")
    private Integer teamNum;
    @ApiModelProperty(value = "购买人数", name = "buyNum")
    private Integer buyNum;
}
