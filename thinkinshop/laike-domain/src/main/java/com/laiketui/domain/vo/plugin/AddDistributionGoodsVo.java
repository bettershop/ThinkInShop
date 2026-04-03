package com.laiketui.domain.vo.plugin;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 添加/编辑分销商品
 *
 * @author Trick
 * @date 2021/2/8 10:23
 */
@ApiModel(description = "添加/编辑分销等级")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddDistributionGoodsVo extends MainVo
{
    @ApiModelProperty(name = "id", value = "分销商品id")
    private Integer id;
    @ApiModelProperty(name = "sid", value = "商品规格id")
    private Integer    sid;
    @ApiModelProperty(name = "uplevel", value = "晋升等级id")
    private Integer    uplevel = 0;
    @ApiModelProperty(name = "distributionRule", value = "分佣规则设置 1等级2自定义")
    private Integer    distributionRule;
    @ApiModelProperty(name = "directType", value = "直推分销比例发放模式 0.百分比 1.固定金额")
    private Integer    directType;
    @ApiModelProperty(name = "directM", value = "直推分佣金额")
    private BigDecimal directM;
    @ApiModelProperty(name = "indirectType", value = "间推分销比例发放模式 0.百分比 1.固定金额")
    private Integer    indirectType;
    @ApiModelProperty(name = "indirectM", value = "间推分佣金额")
    private BigDecimal indirectM;
    @ApiModelProperty(name = "pv", value = "pv值")
    private BigDecimal pv;
    @ApiModelProperty(name = "customerDistributteSet", value = "分佣配置")
    private String customerDistributteSet;
}
