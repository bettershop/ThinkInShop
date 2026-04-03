package com.laiketui.domain.vo.saas;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "国家信息参数")
public class CountryVo extends MainVo implements Serializable
{

    @ApiModelProperty(value = "国家id逗号分割，单个id不加逗号", name = "ids")
    private String ids;

    @ApiModelProperty(value = "国家id", name = "id")
    private Short id;

    @ApiModelProperty(value = "查询条件", name = "keyword")
    private String keyword;

    @ApiModelProperty(value = "国家名称", name = "name")
    private String name;

    @ApiModelProperty(value = "中文名称", name = "zh_name")
    private String zh_name;

    @ApiModelProperty(value = "编码(英文)", name = "code")
    private String code;

    @ApiModelProperty(value = "ITU电话代码", name = "code2")
    private String code2;

    @ApiModelProperty(value = "国家数字编码", name = "num3")
    private Integer num3;

    @ApiModelProperty(value = "国旗")
    private String national_flag;

    /**
     * 是否显示 1 显示 0 不显示
     */
    @ApiModelProperty(value = "是否显示 1 显示 0 不显示", name = "is_show")
    private Integer is_show;

}
