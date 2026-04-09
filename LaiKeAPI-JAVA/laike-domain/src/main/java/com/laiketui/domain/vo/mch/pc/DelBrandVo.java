package com.laiketui.domain.vo.mch.pc;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 16640
 * @version 1.0
 * @description: liuao
 * @date 2024/12/24 16:44
 */
@AllArgsConstructor
@Getter
@Setter
public class DelBrandVo extends MainVo
{

    @ApiModelProperty("品牌id")
    private Integer brandId;
}
