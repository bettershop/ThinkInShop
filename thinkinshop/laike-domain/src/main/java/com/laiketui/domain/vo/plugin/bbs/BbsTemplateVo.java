package com.laiketui.domain.vo.plugin.bbs;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author: liuao
 * @Date: 2025-10-27-11:11
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BbsTemplateVo extends MainVo
{


    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("模板名称")
    private String name;

    @ApiModelProperty("是否启用 0：未启用 1：启用")
    private Integer status;

    @ApiModelProperty("模板图标")
    private String icon;

    @ApiModelProperty("模板主图")
    private String img;
}
