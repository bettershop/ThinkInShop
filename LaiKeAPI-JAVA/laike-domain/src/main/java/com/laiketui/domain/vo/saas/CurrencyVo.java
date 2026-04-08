package com.laiketui.domain.vo.saas;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;


@ApiModel(description = "平台基本币种")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyVo extends MainVo implements Serializable
{
    @ApiModelProperty(value = "id逗号分割，单个id不加逗号", name = "ids")
    private String ids;

    @ApiModelProperty(value = "id主键", name = "id")
    private Integer id;

    /**
     * ISO货币代码(如USD)
     */
    @ApiModelProperty(value = "ISO货币代码(如USD)", name = "currency_code")
    private String currency_code;

    /**
     * 货币名称
     */
    @ApiModelProperty(value = "货币名称", name = "currency_name")
    private String currency_name;

    /**
     * 是否展示 0 不展示 1展示
     */
    @ApiModelProperty(value = "是否展示 0 不展示 1展示 默认 1", name = "is_show")
    private Integer is_show;

    /**
     * 货币符号($)
     */
    @ApiModelProperty(value = "货币符号($)", name = "currency_symbol")
    private String currency_symbol;

    /**
     * 基础货币汇率
     */
    @ApiModelProperty(value = "基础货币汇率", name = "exchange_rate")
    private BigDecimal exchange_rate;

    /**
     * 是否删除 0 未删除 1已删除
     */
    @ApiModelProperty(value = "是否删除 0 未删除 1已删除", name = "recycle")
    private Integer recycle;

    @ApiModelProperty(value = "查询条件", name = "keyword")
    private String keyword;


}
