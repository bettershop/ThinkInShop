package com.laiketui.domain.vo.saas;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 地区、行政区划
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "地区、行政区划")
public class DistrictVo extends MainVo implements Serializable
{

    @ApiModelProperty(value = "地区id逗号分割，单个id不加逗号", name = "ids")
    private String ids;

    @ApiModelProperty(value = "地区id", name = "id")
    private Integer id;

    @ApiModelProperty(value = "地区名称", name = "district_name")
    private String district_name;

    @ApiModelProperty(value = "地区父id", name = "district_pid")
    private Integer district_pid;

    @ApiModelProperty(value = "显示排序", name = "district_show_order")
    private Integer district_show_order;

    @ApiModelProperty(value = "层级", name = "district_level")
    private Integer district_level;

    @ApiModelProperty(value = "子地区数（未用）", name = "district_childcount")
    private Integer district_childcount;

    @ApiModelProperty(value = "是否删除", name = "district_delete")
    private Integer district_delete;

    @ApiModelProperty(value = "（未用）", name = "district_num")
    private Integer district_num;

    @ApiModelProperty(value = "国家编码", name = "district_country_num")
    private Integer district_country_num;

}
