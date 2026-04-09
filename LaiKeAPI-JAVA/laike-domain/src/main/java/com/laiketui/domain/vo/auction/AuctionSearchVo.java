package com.laiketui.domain.vo.auction;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Author: liuao
 * @Date: 2025-10-30-11:25
 * @Description:
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class AuctionSearchVo extends MainVo
{
    @ApiModelProperty("商品名称")
    private String name;

    /**
     * 竞拍次数；count
     * 价格：price
     * 开拍时间：date
     * 关注数：focus_num
     * end_date:截拍时间
     */
    @ApiModelProperty("排序条件")
    private String sort_criteria;

    @ApiModelProperty(value = "排序 asc desc", name = "sort")
    private String sort;

    @ApiModelProperty("竞拍价格起始")
    private BigDecimal minPrice;

    @ApiModelProperty("竞拍价格结束")
    private BigDecimal maxPrice;

    @ApiModelProperty("分类id")
    private Integer cid;

    @ApiModelProperty("状态 1=未开始 2=进行中 3=已结束")
    private Integer status;

    @ApiModelProperty("专场类型 1=店铺专场 2=普通专场 3=报名专场")
    private Integer type;

    @ApiModelProperty("店铺id")
    private Integer mch_id;

    @ApiModelProperty("专场id")
    private String id;
}
