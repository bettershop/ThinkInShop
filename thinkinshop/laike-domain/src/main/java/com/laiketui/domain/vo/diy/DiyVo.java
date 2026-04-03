package com.laiketui.domain.vo.diy;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: sunH_
 * @Date: Create in 16:17 2022/4/28
 */
@Getter
@Setter
@ApiModel(description = "添加/编辑diy模板")
public class DiyVo extends MainVo
{
    @ApiModelProperty(name = "id", value = "diy模板id")
    private Integer id;

    @ApiModelProperty(name = "name", value = "页面名称")
    private String name;

    @ApiModelProperty(name = "value", value = "页面数据")
    private String value;

    @ApiModelProperty(name = "cover", value = "封面图")
    private String cover;

    @ApiModelProperty(value = "主题类型 1：系统主题 2：自定义主题")
    private Integer theme_type;

    @ApiModelProperty(name = "主题类型code")
    private String theme_type_code;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "店铺id")
    private Integer mchId;

    @ApiModelProperty(value = "导航")
    private String tabBar;

    @ApiModelProperty(value = "查询")
    private String keyWord;

    @ApiModelProperty("导航配置信息")
    private String tabberinfo;

    @ApiModelProperty("页面数据")
    private String pageInfo;
}
