package com.laiketui.plugins.api.group.view;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 参团记录视图
 *
 * @author Trick
 * @date 2023/3/15 10:53
 */
@Data
public class PluginsGroupJoinRcordView
{

    @ApiModelProperty(value = "记录id")
    private String     id;
    @ApiModelProperty(value = "商品编号")
    private Integer    goodsId;
    @ApiModelProperty(value = "商品图片")
    private String     imgUrl;
    @ApiModelProperty(value = "商品名称")
    private String     name;
    @ApiModelProperty(value = "规格")
    private String     attrName;
    @ApiModelProperty(value = "零售价格")
    private BigDecimal price;
    @ApiModelProperty(value = "拼团价格")
    private BigDecimal joinPrice;
    @ApiModelProperty(value = "参团时间")
    private String     joinDate;
    @ApiModelProperty(value = "用户名称")
    private String     userName;
    @ApiModelProperty(value = "团长名称")
    private String     teamName;
    @ApiModelProperty(value = "拼团类型")
    private String     groupType;
    @ApiModelProperty(value = "拼团状态")
    private Integer    status;
    @ApiModelProperty(value = "状态名称")
    private String     statusName;

}
