package com.laiketui.plugins.api.group.view.h5;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品信息 视图
 *
 * @author Trick
 * @date 2023-03-28 14:20:18
 */
@Data
public class PluginsGroupAppGroupGoodsInfoView
{
    @ApiModelProperty(value = "开团id", name = "openId")
    private String                         openId;
    @ApiModelProperty(value = "活动id", name = "acId")
    private String                         acId;
    @ApiModelProperty(value = "活动距离开始/结束时间(毫秒) 如果是未开始则为负数 ", name = "timingNum")
    private Long                           timingNum;
    @ApiModelProperty(value = "活动状态", name = "status")
    private Integer                        status;
    @ApiModelProperty(value = "商品id", name = "goodsId")
    private Integer                        goodsId;
    @ApiModelProperty(value = "商品状态", name = "goodsStatus")
    private Integer                        goodsStatus;
    @ApiModelProperty(value = "商品名称", name = "goodsName")
    private String                         goodsName;
    @ApiModelProperty(value = "拼团商品名称", name = "groupGoodsId")
    private String                         groupGoodsId;
    @ApiModelProperty(value = "商品详情", name = "goodsContext")
    private String                         goodsContext;
    @ApiModelProperty(value = "商品售价", name = "goodsPrice")
    private BigDecimal                     goodsPrice;
    @ApiModelProperty(value = "商品运费", name = "goodsFreight")
    private BigDecimal                     goodsFreight;
    @ApiModelProperty(value = "拼团价格", name = "groupPrice")
    private BigDecimal                     groupPrice;
    @ApiModelProperty(value = "开团价格", name = "groupOpenPrice")
    private BigDecimal                     groupOpenPrice;
    @ApiModelProperty(value = "商品轮播图", name = "goodsImgUrls")
    private List<String>                   goodsImgUrls;
    @ApiModelProperty(value = "团队人数", name = "teamNum")
    private Integer                        teamNum;
    @ApiModelProperty(value = "已团件数", name = "buyNum")
    private Integer                        buyNum;
    @ApiModelProperty(value = "商品库存", name = "stockNum")
    private Integer                        stockNum;
    @ApiModelProperty(value = "团长是否需要参团", name = "isTeamLimit")
    private Boolean                        isTeamLimit = false;
    @ApiModelProperty(value = "店铺信息", name = "mchInfo")
    private PluginsGroupAppMchInfoView     mchInfo;
    @ApiModelProperty(value = "评论信息", name = "commentInfo")
    private PluginsGroupAppCommentInfoView commentInfo;
    @ApiModelProperty(value = "评论数量", name = "commentTotal")
    private Integer                        commentTotal;
    @ApiModelProperty(value = "规格信息", name = "attrInfo")
    private Map<String, Object>            attrInfo;
    @ApiModelProperty(value = "是否可以开团", name = "isJoin")
    private Boolean                        isOpen      = false;
    @ApiModelProperty(value = "运费计算", name = "yunfei")
    private BigDecimal                     yunfei;
    @ApiModelProperty(value = "视频文件", name = "yunfei")
    private String                         video;
    @ApiModelProperty(value = "商品视频", name = "yunfei")
    private String                         proVideo;

}
