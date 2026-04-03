package com.laiketui.plugins.api.group.view.h5;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 我的拼团订单 视图
 *
 * @author Trick
 * @date 2023-03-31 14:52:29
 */
@Data
public class PluginsGroupAppMyOrderInfoView
{
    @ApiModelProperty(value = "开团id", name = "openId")
    private String                       openId;
    @ApiModelProperty(value = "活动id", name = "acId")
    private String                       acId;
    @ApiModelProperty(value = "订单id", name = "orderId")
    private Integer                      orderId;
    @ApiModelProperty(value = "下单时间", name = "orderDate")
    private String                       orderDate;
    @ApiModelProperty(value = "预计佣金 ", name = "dueMoney")
    private BigDecimal                   dueMoney;
    @ApiModelProperty(value = "已结算金额 ", name = "settleMoney")
    private BigDecimal                   settleMoney;
    @ApiModelProperty(value = "结算时间", name = "settleDate")
    private String                       settleDate;
    @ApiModelProperty(value = "规格名称", name = "attrName")
    private String                       attrName;
    @ApiModelProperty(value = "团队状态(开团 0=拼团中 1=拼团成功 2=拼团失败)", name = "status")
    private Integer                      status;
    @ApiModelProperty(value = "团队状态名称", name = "statusName")
    private String                       statusName;
    @ApiModelProperty(value = "店铺信息", name = "mchInfo")
    private PluginsGroupAppMchInfoView   mchInfo;
    @ApiModelProperty(value = "商品信息", name = "goodsInfo")
    private PluginsGroupAppGoodsInfoView goodsInfo;

}
