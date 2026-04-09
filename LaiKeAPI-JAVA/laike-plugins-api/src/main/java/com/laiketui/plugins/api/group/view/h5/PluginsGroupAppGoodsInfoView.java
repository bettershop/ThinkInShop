package com.laiketui.plugins.api.group.view.h5;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品详情 视图
 *
 * @author Trick
 * @date 2023-03-31 14:52:29
 */
@Data
public class PluginsGroupAppGoodsInfoView
{
    @ApiModelProperty(value = "商品id", name = "goodsId")
    private Integer    goodsId;
    @ApiModelProperty(value = "商品名称", name = "goodsName")
    private String     goodsName;
    @ApiModelProperty(value = "拼团商品id", name = "groupGoodsId")
    private String     groupGoodsId;
    @ApiModelProperty(value = "商品规格信息", name = "size")
    private String     size;
    @ApiModelProperty(value = "商品售价(划线)", name = "goodsPrice")
    private BigDecimal goodsPrice;
    @ApiModelProperty(value = "拼团价格", name = "groupPrice")
    private BigDecimal groupPrice;
    @ApiModelProperty(value = "商品图片", name = "goodsImgUrl")
    private String     goodsImgUrl;
    @ApiModelProperty(value = "团队人数", name = "teamNum")
    private Integer    teamNum;
    @ApiModelProperty(value = "已团件数", name = "buyNum")
    private Integer    buyNum;
    @ApiModelProperty(value = "商品库存", name = "stockNum")
    private Integer    stockNum;
}
