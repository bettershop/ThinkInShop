package com.laiketui.plugins.api.group.view;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 开团记录列表视图
 *
 * @author Trick
 * @date 2023/3/15 10:53
 */
@Data
public class PluginsGroupGoodsOpenRecordView
{

    @ApiModelProperty(value = "开团编号")
    private String     id;
    @ApiModelProperty(value = "商品编号")
    private Integer    goodsId;
    @ApiModelProperty(value = "商品名称")
    private String     name;
    @ApiModelProperty(value = "商品图片")
    private String     imgUrl;
    @ApiModelProperty(value = "零售价")
    private BigDecimal price;
    @ApiModelProperty(value = "拼团类型")
    private String     groupType;
    @ApiModelProperty(value = "团长价格")
    private String     teamPrice;
    @ApiModelProperty(value = "团长名称")
    private String     teamName;
    @ApiModelProperty(value = "开始时间")
    private String     startDate;
    @ApiModelProperty(value = "结束时间")
    private String     endDate;
    @ApiModelProperty(value = "状态")
    private int        status;
    @ApiModelProperty(value = "状态名称")
    private String     statusName;
    @ApiModelProperty(value = "开团时间")
    private String     openStartDate;
    @ApiModelProperty(value = "开团结束时间")
    private String     openEndDate;
    @ApiModelProperty(value = "零售价")
    private String     stringPrice;
    @ApiModelProperty(value = "团长价格")
    private String     stringTeamPrice;
    @ApiModelProperty(value = "团长是否参团")
    private String     teamLimit;
}
