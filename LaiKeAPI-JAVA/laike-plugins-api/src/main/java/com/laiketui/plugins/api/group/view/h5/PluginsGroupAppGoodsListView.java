package com.laiketui.plugins.api.group.view.h5;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 获取移动端商品列表 视图
 *
 * @author Trick
 * @date 2023-03-28 14:20:18
 */
@Data
public class PluginsGroupAppGoodsListView
{
    @ApiModelProperty(value = "活动id", name = "acId")
    private String     acId;
    @ApiModelProperty(value = "活动状态", name = "活动状态")
    private Integer    status;
    @ApiModelProperty(value = "商品id", name = "goodsId")
    private Integer    goodsId;
    @ApiModelProperty(value = "商品名称", name = "goodsName")
    private String     goodsName;
    @ApiModelProperty(value = "商品售价", name = "goodsPrice")
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
