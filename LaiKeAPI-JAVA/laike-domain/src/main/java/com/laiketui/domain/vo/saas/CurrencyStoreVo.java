package com.laiketui.domain.vo.saas;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;


@ApiModel(description = "商城币种信息")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyStoreVo extends MainVo implements Serializable
{
    @ApiModelProperty(value = "查询条件", name = "keyword")
    private String keyword;

    /**
     * 货币id
     */
    @ApiModelProperty(value = "货币id", name = "currency_id")
    private Integer currency_id;

    /**
     * 是否展示 0 不展示 1展示
     */
    @ApiModelProperty(value = "是否展示 0 不展示 1展示", name = "is_show")
    private Integer is_show;

    /**
     * 是否商城基础货币【结算货币】默认 0 否 1 是
     */
    @ApiModelProperty(value = "是否商城基础货币【结算货币】默认 0 否 1 是", name = "default_currency")
    private Integer default_currency;

    /**
     * 基础货币汇率
     */
    @ApiModelProperty(value = "基础货币汇率", name = "exchange_rate")
    private BigDecimal exchange_rate;

    /**
     * 是否删除 0 未删除 1已删除
     */
    @ApiModelProperty(value = "是否删除 0 未删除 1已删除 ", name = "recycle")
    private Integer recycle;

}
