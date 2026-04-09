package com.laiketui.plugins.api.group.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 拼团规则
 *
 * @author Trick
 * @date 2023/3/15 11:14
 */
@Data
public class PluginsGroupRuleVo
{

    @ApiModelProperty(value = "参团折扣", name = "canDiscount")
    private BigDecimal canDiscount;
    @ApiModelProperty(value = "团长折扣", name = "openDiscount")
    private BigDecimal openDiscount;

}
