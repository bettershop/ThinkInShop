package com.laiketui.domain.vo.plugin.bbs;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author: liuao
 * @Date: 2025-09-29-14:05
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BbsCategoryVo extends MainVo
{
    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("分类等级")
    private Integer level;

    @ApiModelProperty("1=查询下级,3=查询子类")
    private Integer type;

    @ApiModelProperty("上级id")
    private Long sid;

    @ApiModelProperty("未选中分类图片")
    private String bimg;

    @ApiModelProperty("选中分类图片")
    private String img;

    @ApiModelProperty("id")
    private Long id;
}
