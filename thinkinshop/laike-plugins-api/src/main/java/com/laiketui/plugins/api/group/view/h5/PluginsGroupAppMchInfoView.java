package com.laiketui.plugins.api.group.view.h5;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 店铺信息
 *
 * @author Trick
 * @date 2023/3/30 10:52
 */
@Data
public class PluginsGroupAppMchInfoView
{
    @ApiModelProperty(value = "店铺id", name = "mchId")
    private Integer mchId;
    @ApiModelProperty(value = "店铺名称", name = "mchName")
    private String  mchName;
    @ApiModelProperty(value = "店铺头像", name = "mchHeadImg")
    private String  mchHeadImg;
    @ApiModelProperty(value = "在售商品数量", name = "quantityOnSaleNum")
    private Integer quantityOnSaleNum;
    @ApiModelProperty(value = "已售数量", name = "quantitySoldNum")
    private Integer quantitySoldNum;
    @ApiModelProperty(value = "关注数量", name = "collectionNum")
    private Integer collectionNum;
    @ApiModelProperty(value = "是否已打烊 1营业 2已打烊", name = "collectionNum")
    private String  is_open;
}
