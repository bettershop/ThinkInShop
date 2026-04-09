package com.laiketui.plugins.api.group.view;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 开团详情
 *
 * @author Trick
 * @date 2023/3/15 10:53
 */
@Data
public class PluginsGroupOpenDetailsView
{

    @ApiModelProperty(value = "开团编号")
    private String     id;
    @ApiModelProperty(value = "商品编号")
    private Integer    goodsId;
    @ApiModelProperty(value = "商品名称")
    private String     name;
    @ApiModelProperty(value = "商品图片")
    private String     imgUrl;
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
    @ApiModelProperty(value = "参与类型")
    private String     type;
    @ApiModelProperty(value = "零售价格")
    private String     stringPrice;
    @ApiModelProperty(value = "拼团价格")
    private String     stringJoinPrice;

}
