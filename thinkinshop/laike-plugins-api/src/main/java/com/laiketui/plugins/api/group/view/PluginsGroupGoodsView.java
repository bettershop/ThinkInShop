package com.laiketui.plugins.api.group.view;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品列表视图
 *
 * @author Trick
 * @date 2023/3/15 10:53
 */
@Data
public class PluginsGroupGoodsView
{

    @ApiModelProperty(value = "商品编号")
    private Integer    id;
    @ApiModelProperty(value = "商品名称")
    private String     name;
    @ApiModelProperty(value = "商品图片")
    private String     imgUrl;
    @ApiModelProperty(value = "分类")
    private String     className;
    @ApiModelProperty(value = "品牌")
    private String     brandName;
    @ApiModelProperty(value = "库存")
    private Integer    stockNum;
    @ApiModelProperty(value = "零售价")
    private BigDecimal price;
}
