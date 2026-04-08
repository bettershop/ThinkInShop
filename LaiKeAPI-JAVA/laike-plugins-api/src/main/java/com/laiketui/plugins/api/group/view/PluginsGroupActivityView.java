package com.laiketui.plugins.api.group.view;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 商品列表视图
 *
 * @author Trick
 * @date 2023/3/15 10:53
 */
@Data
public class PluginsGroupActivityView
{

    @ApiModelProperty(value = "活动编号")
    private String  id;
    @ApiModelProperty(value = "活动名称")
    private String  name;
    @ApiModelProperty(value = "开始时间")
    private String  startDate;
    @ApiModelProperty(value = "结束时间")
    private String  endDate;
    @ApiModelProperty(value = "状态")
    private Integer status;
    @ApiModelProperty(value = "状态名称")
    private String  statusName;
    @ApiModelProperty(value = "是否显示")
    private int     isShow;

}
